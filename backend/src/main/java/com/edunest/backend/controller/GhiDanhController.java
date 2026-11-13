package com.edunest.backend.controller;

import com.edunest.backend.dto.GhiDanhRequest;
import com.edunest.backend.dto.GhiDanhResponse;
import com.edunest.backend.service.GhiDanhService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ghi-danh")
public class GhiDanhController {

    private final GhiDanhService ghiDanhService;

    public GhiDanhController(GhiDanhService ghiDanhService) {
        this.ghiDanhService = ghiDanhService;
    }

    /**
     * Ghi danh một hoặc nhiều học viên vào lớp.
     */
    @PostMapping
    public ResponseEntity<List<GhiDanhResponse>> ghiDanh(@Valid @RequestBody GhiDanhRequest req) {
        List<GhiDanhResponse> res = ghiDanhService.ghiDanh(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    /**
     * Lấy danh sách học viên đã ghi danh vào một lớp học.
     */
    @GetMapping("/lop/{lopId}")
    public ResponseEntity<List<GhiDanhResponse>> layDanhSachTheoLop(@PathVariable UUID lopId) {
        return ResponseEntity.ok(ghiDanhService.layDanhSachGhiDanhTheoLop(lopId));
    }

    /**
     * Hủy ghi danh học viên.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> huyGhiDanh(@PathVariable UUID id) {
        ghiDanhService.huyGhiDanh(id);
        return ResponseEntity.noContent().build();
    }
}

