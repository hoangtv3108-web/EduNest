package com.edunest.backend.nhacno;

import com.edunest.backend.tenant.TenantContext;
import com.edunest.backend.tenant.TenantLister;
import com.edunest.backend.util.ThoiGianVN;
import java.time.LocalDate;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Cron job nhắc học phí: mỗi ngày (mặc định 08:00 giờ Việt Nam) duyệt từng
 * trung tâm đang hoạt động.
 * Tắt bằng edunest.nhac-no.bat-tu-dong=false (ví dụ trong test). Chạy nhiều
 * instance vẫn an toàn nhờ
 * ràng buộc UNIQUE ở lich_su_nhac_no (xem NhacNoService); nếu cần có thể thêm
 * ShedLock để tránh quét thừa.
 */
@Component
@ConditionalOnProperty(name = "edunest.nhac-no.bat-tu-dong", havingValue = "true", matchIfMissing = true)
public class NhacNoScheduler {

    private static final Logger log = LoggerFactory.getLogger(NhacNoScheduler.class);

    private final TenantLister tenantLister;
    private final NhacNoService nhacNoService;

    public NhacNoScheduler(TenantLister tenantLister, NhacNoService nhacNoService) {
        this.tenantLister = tenantLister;
        this.nhacNoService = nhacNoService;
    }

    @Scheduled(cron = "${edunest.nhac-no.cron:0 0 8 * * *}", zone = "Asia/Ho_Chi_Minh")
    public void chayHangNgay() {
        LocalDate homNay = ThoiGianVN.homNay();
        for (UUID tenantId : tenantLister.layTenantDangHoatDong()) {
            TenantContext.set(tenantId);
            try {
                nhacNoService.quetVaNhac(homNay);
            } catch (RuntimeException e) {
                log.error("Quét nhắc nợ lỗi cho tenant {}", tenantId, e); // lỗi một trung tâm không chặn trung tâm khác
            } finally {
                TenantContext.clear();
            }
        }
    }
}