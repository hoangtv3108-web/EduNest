package com.edunest.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class BuoiHocResponse {
    private UUID id;
    private UUID lopId;
    private LocalDate ngayHoc;
    private LocalTime gioBatDau;

    public BuoiHocResponse() {}

    public BuoiHocResponse(UUID id, UUID lopId, LocalDate ngayHoc, LocalTime gioBatDau) {
        this.id = id;
        this.lopId = lopId;
        this.ngayHoc = ngayHoc;
        this.gioBatDau = gioBatDau;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getLopId() { return lopId; }
    public void setLopId(UUID lopId) { this.lopId = lopId; }

    public LocalDate getNgayHoc() { return ngayHoc; }
    public void setNgayHoc(LocalDate ngayHoc) { this.ngayHoc = ngayHoc; }

    public LocalTime getGioBatDau() { return gioBatDau; }
    public void setGioBatDau(LocalTime gioBatDau) { this.gioBatDau = gioBatDau; }
}

