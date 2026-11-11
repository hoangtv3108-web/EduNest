package com.edunest.backend.tenant;

import java.util.UUID;

public record RegisterTenantResponse(UUID tenantId, String tenTrungTam, String emailQuanTri) {}
