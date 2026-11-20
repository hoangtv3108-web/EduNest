package com.edunest.backend.baocao;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Báo cáo cuối tháng cho quản lý (SecurityConfig: CENTER_ADMIN). Tham số thang
 * dạng yyyy-MM, mặc định tháng này.
 */
@RestController
@RequestMapping("/api/bao-cao")
public class BaoCaoController {

    private final BaoCaoService service;

    public BaoCaoController(BaoCaoService service) {
        this.service = service;
    }

    @GetMapping("/doanh-thu")
    public ResponseEntity<DoanhThuThangResponse> doanhThu(@RequestParam(required = false) String thang) {
        return ResponseEntity.ok(service.doanhThuThang(BaoCaoService.parseThang(thang)));
    }

    @GetMapping("/chuyen-can")
    public ResponseEntity<ChuyenCanThangResponse> chuyenCan(@RequestParam(required = false) String thang) {
        return ResponseEntity.ok(service.chuyenCanThang(BaoCaoService.parseThang(thang)));
    }
}