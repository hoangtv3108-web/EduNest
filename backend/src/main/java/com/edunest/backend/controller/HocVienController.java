package com.edunest.backend.controller;

import com.edunest.backend.dto.ExcelPreviewResponse;
import com.edunest.backend.dto.HocVienChiTietResponse;
import com.edunest.backend.dto.ImportExcelResponse;
import com.edunest.backend.service.HocVienExcelService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/hoc-vien")
public class HocVienController {

    private final HocVienExcelService hocVienExcelService;

    public HocVienController(HocVienExcelService hocVienExcelService) {
        this.hocVienExcelService = hocVienExcelService;
    }

    /**
     * API xem trước dữ liệu từ file Excel (.xlsx).
     */
    @PostMapping(value = "/excel/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExcelPreviewResponse> previewExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        ExcelPreviewResponse preview = hocVienExcelService.previewExcel(file);
        return ResponseEntity.ok(preview);
    }

    /**
     * API nhập chính thức học viên từ file Excel vào database.
     */
    @PostMapping(value = "/excel/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportExcelResponse> importExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        ImportExcelResponse response = hocVienExcelService.importExcel(file);
        return ResponseEntity.ok(response);
    }

    /**
     * API lấy danh sách học viên hiện có trong trung tâm.
     */
    @GetMapping
    public ResponseEntity<List<HocVienChiTietResponse>> layDanhSachHocVien() {
        return ResponseEntity.ok(hocVienExcelService.layDanhSachHocVien());
    }
}

