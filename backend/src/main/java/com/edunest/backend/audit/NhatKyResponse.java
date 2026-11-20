package com.edunest.backend.audit;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.OffsetDateTime;
import java.util.UUID;

public record NhatKyResponse(
        UUID id,
        UUID nguoiThucHienId,
        String nguoiThucHienTen,
        String vaiTro,
        String hanhDong,
        String doiTuongLoai,
        UUID doiTuongId,
        JsonNode duLieuCu,
        JsonNode duLieuMoi,
        OffsetDateTime thoiGian) {
}