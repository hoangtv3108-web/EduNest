package com.edunest.backend.controller;

import com.edunest.backend.dto.GhiDanhRequest;
import com.edunest.backend.dto.GhiDanhResponse;
import com.edunest.backend.service.GhiDanhService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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
     * Hủy/kết thúc ghi danh học viên (bảo toàn lịch sử hóa đơn).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> huyGhiDanh(@PathVariable UUID id) {
        ghiDanhService.huyGhiDanh(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Đổi trạng thái ghi danh (DANG_HOC, TAM_NGHI, DA_NGHI, HOAN_THANH).
     */
    @PatchMapping("/{id}/trang-thai")
    public ResponseEntity<Void> doiTrangThai(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String trangThai = body.get("trangThai");
        if (trangThai == null || trangThai.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        ghiDanhService.doiTrangThai(id, trangThai);
        return ResponseEntity.noContent().build();
    }
}
