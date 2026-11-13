package com.edunest.backend.controller;

import com.edunest.backend.dto.CapNhatDiemDanhRequest;
import com.edunest.backend.dto.DiemDanhRowResponse;
import com.edunest.backend.service.DiemDanhService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/diem-danh")
public class DiemDanhController {

    private final DiemDanhService diemDanhService;

    public DiemDanhController(DiemDanhService diemDanhService) {
        this.diemDanhService = diemDanhService;
    }

    /**
     * Lấy bảng điểm danh của buổi học.
     */
    @GetMapping("/buoi-hoc/{buoiHocId}")
    public ResponseEntity<List<DiemDanhRowResponse>> layBangDiemDanh(@PathVariable UUID buoiHocId) {
        return ResponseEntity.ok(diemDanhService.layBangDiemDanh(buoiHocId));
    }

    /**
     * Cập nhật danh sách điểm danh cho buổi học.
     */
    @PostMapping
    public ResponseEntity<List<DiemDanhRowResponse>> capNhatDiemDanh(@Valid @RequestBody CapNhatDiemDanhRequest req) {
        return ResponseEntity.ok(diemDanhService.capNhatDiemDanh(req));
    }
}

