package com.edunest.backend.nhacno;

import com.edunest.backend.util.ThoiGianVN;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Dành cho quản lý trung tâm (SecurityConfig: CENTER_ADMIN). */
@RestController
@RequestMapping("/api/nhac-no")
public class NhacNoController {

    private final NhacNoService service;

    public NhacNoController(NhacNoService service) {
        this.service = service;
    }

    /**
     * Chạy ngay một lượt quét cho trung tâm của người gọi (để kiểm thử/demo, không
     * cần chờ cron 8h).
     */
    @PostMapping("/chay-thu")
    public ResponseEntity<KetQuaQuetNhacNo> chayThu() {
        return ResponseEntity.ok(service.quetVaNhac(ThoiGianVN.homNay()));
    }

    @GetMapping("/lich-su")
    public ResponseEntity<List<LichSuNhacNoResponse>> lichSu(@RequestParam(defaultValue = "50") int gioiHan) {
        return ResponseEntity.ok(service.layLichSu(gioiHan));
    }
}