package com.edunest.backend.controller;

import com.edunest.backend.dto.HoaDonHocPhiResponse;
import com.edunest.backend.dto.TinhHocPhiRequest;
import com.edunest.backend.service.HocPhiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/hoa-don")
public class HoaDonController {

    private final HocPhiService hocPhiService;

    public HoaDonController(HocPhiService hocPhiService) {
        this.hocPhiService = hocPhiService;
    }

    /**
     * Kích hoạt tiến trình tự động tính học phí cho lớp theo kỳ.
     */
    @PostMapping("/tinh-hoc-phi")
    public ResponseEntity<List<HoaDonHocPhiResponse>> tinhHocPhi(@Valid @RequestBody TinhHocPhiRequest req) {
        List<HoaDonHocPhiResponse> res = hocPhiService.tinhHocPhiTuDong(req);
        return ResponseEntity.ok(res);
    }

    /**
     * Lấy danh sách hóa đơn học phí của trung tâm, có thể lọc theo lớp hoặc kỳ học phí.
     */
    @GetMapping
    public ResponseEntity<List<HoaDonHocPhiResponse>> layDanhSachHoaDon(
            @RequestParam(value = "lopId", required = false) UUID lopId,
            @RequestParam(value = "kyHocPhi", required = false) String kyHocPhi) {
        return ResponseEntity.ok(hocPhiService.layDanhSachHoaDon(lopId, kyHocPhi));
    }

    /**
     * Cập nhật trạng thái hóa đơn (ví dụ: DA_THANH_TOAN, CHUA_THANH_TOAN).
     */
    @PatchMapping("/{id}/trang-thai")
    public ResponseEntity<Void> capNhatTrangThai(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String trangThai = body.get("trangThai");
        if (trangThai == null || trangThai.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        hocPhiService.capNhatTrangThai(id, trangThai);
        return ResponseEntity.noContent().build();
    }
}

