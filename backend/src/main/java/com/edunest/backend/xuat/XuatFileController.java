package com.edunest.backend.xuat;

import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.edunest.backend.baocao.BaoCaoService;
import java.nio.charset.StandardCharsets;

/**
 * Tải file báo cáo / nhật ký. Nằm dưới /api/bao-cao/** và /api/audit-log/** nên
 * SecurityConfig
 * đã giới hạn cho CENTER_ADMIN. Header X-SHA256 là mã băm của nội dung file để
 * đối chiếu toàn vẹn.
 */
@RestController
@RequestMapping("/api")
public class XuatFileController {

    private final XuatBaoCaoService service;

    public XuatFileController(XuatBaoCaoService service) {
        this.service = service;
    }

    /** 3.4: GET /api/bao-cao/doanh-thu/xuat-excel?thang=2026-11 */
    @GetMapping("/bao-cao/doanh-thu/xuat-excel")
    public ResponseEntity<byte[]> xuatDoanhThu(@RequestParam(required = false) String thang) {
        return phanHoi(service.xuatDoanhThuExcel(BaoCaoService.parseThang(thang)));
    }

    /**
     * 3.5: GET
     * /api/audit-log/xuat?dinhDang=csv|json|xlsx&bom=false&hanhDong=&tuNgay=&denNgay=...
     */
    @GetMapping("/audit-log/xuat")
    public ResponseEntity<byte[]> xuatNhatKy(
            @RequestParam(defaultValue = "csv") String dinhDang,
            @RequestParam(defaultValue = "false") boolean bom,
            @RequestParam(required = false) String hanhDong,
            @RequestParam(required = false) UUID nguoiThucHienId,
            @RequestParam(required = false) UUID doiTuongId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        XuatBaoCaoService.DinhDang dd = XuatBaoCaoService.DinhDang.tu(dinhDang);
        return phanHoi(service.xuatNhatKy(dd, bom, hanhDong, nguoiThucHienId, doiTuongId, tuNgay, denNgay));
    }

    private static ResponseEntity<byte[]> phanHoi(XuatFile f) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(f.tenFile(), StandardCharsets.UTF_8).build()
                                .toString())
                .header("X-SHA256", f.sha256())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .contentType(MediaType.parseMediaType(f.contentType()))
                .contentLength(f.noiDung().length)
                .body(f.noiDung());
    }
}