package com.edunest.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class GhiDanhRequest {

    @NotNull(message = "Lớp học không được để trống")
    private UUID lopId;

    @NotEmpty(message = "Vui lòng chọn ít nhất một học viên để ghi danh")
    private List<UUID> hocVienIds;

    private LocalDate ngayBatDau;

    public GhiDanhRequest() {}

    public GhiDanhRequest(UUID lopId, List<UUID> hocVienIds, LocalDate ngayBatDau) {
        this.lopId = lopId;
        this.hocVienIds = hocVienIds;
        this.ngayBatDau = ngayBatDau;
    }

    public UUID getLopId() { return lopId; }
    public void setLopId(UUID lopId) { this.lopId = lopId; }

    public List<UUID> getHocVienIds() { return hocVienIds; }
    public void setHocVienIds(List<UUID> hocVienIds) { this.hocVienIds = hocVienIds; }

    public LocalDate getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(LocalDate ngayBatDau) { this.ngayBatDau = ngayBatDau; }
}

