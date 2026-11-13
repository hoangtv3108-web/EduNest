package com.edunest.backend.controller;

import com.edunest.backend.dto.*;
import com.edunest.backend.service.LopService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lop")
public class LopController {

    private final LopService lopService;

    public LopController(LopService lopService) {
        this.lopService = lopService;
    }

    /**
     * Tạo lớp học mới.
     */
    @PostMapping
    public ResponseEntity<LopResponse> taoLop(@Valid @RequestBody TaoLopRequest req) {
        LopResponse res = lopService.taoLop(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    /**
     * Lấy danh sách toàn bộ lớp học của trung tâm.
     */
    @GetMapping
    public ResponseEntity<List<LopResponse>> layDanhSachLop() {
        return ResponseEntity.ok(lopService.layDanhSachLop());
    }

    /**
     * Lấy thông tin chi tiết một lớp học.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LopResponse> layChiTietLop(@PathVariable UUID id) {
        return ResponseEntity.ok(lopService.layChiTietLop(id));
    }

    /**
     * Cập nhật thông tin lớp học.
     */
    @PutMapping("/{id}")
    public ResponseEntity<LopResponse> capNhatLop(@PathVariable UUID id, @Valid @RequestBody CapNhatLopRequest req) {
        return ResponseEntity.ok(lopService.capNhatLop(id, req));
    }

    /**
     * Xóa lớp học.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> xoaLop(@PathVariable UUID id) {
        lopService.xoaLop(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Tự động sinh danh sách buổi học theo lịch tuần.
     */
    @PostMapping("/{id}/sinh-buoi-hoc")
    public ResponseEntity<List<BuoiHocResponse>> sinhBuoiHoc(
            @PathVariable UUID id,
            @Valid @RequestBody SinhBuoiHocRequest req) {
        List<BuoiHocResponse> res = lopService.sinhBuoiHoc(id, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    /**
     * Lấy danh sách các buổi học của lớp.
     */
    @GetMapping("/{id}/buoi-hoc")
    public ResponseEntity<List<BuoiHocResponse>> layDanhSachBuoiHoc(@PathVariable UUID id) {
        return ResponseEntity.ok(lopService.layDanhSachBuoiHoc(id));
    }
}

