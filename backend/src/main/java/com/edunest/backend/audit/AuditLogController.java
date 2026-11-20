package com.edunest.backend.audit;

import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Quản lý trung tâm xem lại lịch sử thao tác (SecurityConfig: CENTER_ADMIN).
 * Chỉ đọc, không có API sửa/xoá.
 */
@RestController
@RequestMapping("/api/audit-log")
public class AuditLogController {

    private final NhatKyKiemToanService service;

    public AuditLogController(NhatKyKiemToanService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<TrangNhatKy> tim(
            @RequestParam(required = false) String hanhDong,
            @RequestParam(required = false) UUID nguoiThucHienId,
            @RequestParam(required = false) UUID doiTuongId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.tim(hanhDong, nguoiThucHienId, doiTuongId, tuNgay, denNgay, page, size));
    }
}