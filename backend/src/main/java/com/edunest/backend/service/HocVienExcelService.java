package com.edunest.backend.service;

import com.edunest.backend.dto.*;
import com.edunest.backend.entity.HocVien;
import com.edunest.backend.entity.PhuHuynh;
import com.edunest.backend.repository.HocVienRepository;
import com.edunest.backend.repository.PhuHuynhRepository;
import com.edunest.backend.tenant.TenantContext;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class HocVienExcelService {

    private final PhuHuynhRepository phuHuynhRepository;
    private final HocVienRepository hocVienRepository;

    private static final DateTimeFormatter[] DATE_FORMATTERS = new DateTimeFormatter[]{
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy")
    };

    public HocVienExcelService(PhuHuynhRepository phuHuynhRepository, HocVienRepository hocVienRepository) {
        this.phuHuynhRepository = phuHuynhRepository;
        this.hocVienRepository = hocVienRepository;
    }

    /**
     * Đọc file Excel và trả về danh sách xem trước (Preview) kèm trạng thái hợp lệ.
     * Không ghi dữ liệu vào CSDL.
     */
    public ExcelPreviewResponse previewExcel(MultipartFile file) {
        List<ExcelPreviewRow> rows = new ArrayList<>();
        int validCount = 0;
        int errorCount = 0;

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            int lastRow = sheet.getLastRowNum();
            for (int r = 1; r <= lastRow; r++) { // bỏ qua hàng tiêu đề row 0
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) continue;

                String hoTenPhuHuynh = getCellValue(row.getCell(0), formatter).trim();
                String sdtZalo = getCellValue(row.getCell(1), formatter).trim();
                String email = getCellValue(row.getCell(2), formatter).trim();
                String hoTenHocVien = getCellValue(row.getCell(3), formatter).trim();
                String ngaySinhStr = getCellValue(row.getCell(4), formatter).trim();

                List<String> errors = new ArrayList<>();
                if (hoTenPhuHuynh.isEmpty()) errors.add("Tên phụ huynh trống");
                if (sdtZalo.isEmpty()) errors.add("SĐT/Zalo trống");
                if (hoTenHocVien.isEmpty()) errors.add("Tên học viên trống");

                LocalDate parsedDob = parseLocalDate(row.getCell(4), ngaySinhStr);
                if (!ngaySinhStr.isEmpty() && parsedDob == null) {
                    errors.add("Ngày sinh sai định dạng (chuẩn: dd/MM/yyyy)");
                }

                boolean hopLe = errors.isEmpty();
                if (hopLe) validCount++; else errorCount++;

                rows.add(new ExcelPreviewRow(
                        r + 1,
                        hoTenPhuHuynh,
                        sdtZalo,
                        email,
                        hoTenHocVien,
                        parsedDob != null ? parsedDob.toString() : ngaySinhStr,
                        hopLe,
                        String.join("; ", errors)
                ));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Không thể đọc file Excel: " + e.getMessage(), e);
        }

        return new ExcelPreviewResponse(rows.size(), validCount, errorCount, rows);
    }

    /**
     * Nhập hàng loạt phụ huynh và học viên từ file Excel, gán tenant_id hiện tại từ TenantContext.
     */
    @Transactional
    public ImportExcelResponse importExcel(MultipartFile file) {
        UUID tenantId = TenantContext.get();
        if (tenantId == null) {
            throw new IllegalStateException("Không xác định được tenant_id của phiên làm việc");
        }

        int phuHuynhMoi = 0;
        int hocVienMoi = 0;

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            int lastRow = sheet.getLastRowNum();
            for (int r = 1; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) continue;

                String hoTenPhuHuynh = getCellValue(row.getCell(0), formatter).trim();
                String sdtZalo = getCellValue(row.getCell(1), formatter).trim();
                String email = getCellValue(row.getCell(2), formatter).trim();
                String hoTenHocVien = getCellValue(row.getCell(3), formatter).trim();
                String ngaySinhStr = getCellValue(row.getCell(4), formatter).trim();

                if (hoTenPhuHuynh.isEmpty() || sdtZalo.isEmpty() || hoTenHocVien.isEmpty()) {
                    continue; // bỏ qua hàng thiếu trường bắt buộc
                }

                LocalDate ngaySinh = parseLocalDate(row.getCell(4), ngaySinhStr);

                // Tìm hoặc tạo phụ huynh theo SĐT Zalo trong cùng tenant (RLS tự động lọc)
                PhuHuynh phuHuynh = phuHuynhRepository.findBySdtZalo(sdtZalo).orElse(null);
                if (phuHuynh == null) {
                    phuHuynh = new PhuHuynh(tenantId, hoTenPhuHuynh, sdtZalo, email.isEmpty() ? null : email);
                    phuHuynh = phuHuynhRepository.save(phuHuynh);
                    phuHuynhMoi++;
                }

                // Tạo học viên mới gắn với phụ huynh và tenant hiện tại
                HocVien hocVien = new HocVien(tenantId, phuHuynh.getId(), hoTenHocVien, ngaySinh);
                hocVienRepository.save(hocVien);
                hocVienMoi++;
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi nhập dữ liệu từ Excel: " + e.getMessage(), e);
        }

        return new ImportExcelResponse(phuHuynhMoi, hocVienMoi,
                String.format("Nhập thành công %d học viên và %d phụ huynh mới", hocVienMoi, phuHuynhMoi));
    }

    /**
     * Lấy danh sách toàn bộ học viên kèm thông tin phụ huynh của tenant hiện tại.
     */
    @Transactional(readOnly = true)
    public List<HocVienChiTietResponse> layDanhSachHocVien() {
        List<HocVien> hocViens = hocVienRepository.findAll();
        Map<UUID, PhuHuynh> phuHuynhMap = new HashMap<>();

        for (PhuHuynh ph : phuHuynhRepository.findAll()) {
            phuHuynhMap.put(ph.getId(), ph);
        }

        List<HocVienChiTietResponse> res = new ArrayList<>();
        for (HocVien hv : hocViens) {
            PhuHuynh ph = hv.getPhuHuynhId() != null ? phuHuynhMap.get(hv.getPhuHuynhId()) : null;
            res.add(new HocVienChiTietResponse(
                    hv.getId(),
                    hv.getHoTen(),
                    hv.getNgaySinh(),
                    hv.getPhuHuynhId(),
                    ph != null ? ph.getHoTen() : null,
                    ph != null ? ph.getSdtZalo() : null,
                    ph != null ? ph.getEmail() : null
            ));
        }
        return res;
    }

    private boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }

    private String getCellValue(Cell cell, DataFormatter formatter) {
        if (cell == null) return "";
        return formatter.formatCellValue(cell);
    }

    private LocalDate parseLocalDate(Cell cell, String text) {
        if (cell != null && cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            Date date = cell.getDateCellValue();
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        if (text == null || text.isBlank()) return null;
        for (DateTimeFormatter fmt : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(text, fmt);
            } catch (Exception ignored) {}
        }
        return null;
    }
}

