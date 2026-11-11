package com.edunest.backend.tenant;

import java.util.UUID;

/**
 * Giữ mã trung tâm (tenant) của request đang xử lý, theo từng luồng (thread).
 * Giá trị chỉ được đặt từ claim "tenant_id" của JWT đã kiểm tra chữ ký (xem TenantContextFilter),
 * hoặc bởi luồng đăng ký trung tâm. Tuyệt đối không đọc từ header, body hay đường dẫn do client gửi.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(UUID tenantId) {
        CURRENT.set(tenantId);
    }

    public static UUID get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
