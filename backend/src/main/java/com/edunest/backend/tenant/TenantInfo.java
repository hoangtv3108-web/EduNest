package com.edunest.backend.tenant;

import java.time.LocalDateTime;
import java.util.UUID;

public record TenantInfo(UUID id, String tenTrungTam, String trangThai, LocalDateTime ngayTao) {}
