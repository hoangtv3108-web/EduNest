package com.edunest.backend.audit;

import com.edunest.backend.tenant.TenantContext;
import com.edunest.backend.util.ThoiGianVN;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ghi và tra cứu nhật ký kiểm toán.
 *
 * Ghi: gọi trực tiếp từ service nghiệp vụ, TRONG CÙNG transaction với thao tác
 * (Propagation.MANDATORY).
 * Nhờ vậy thao tác và dấu vết của nó thành công hoặc thất bại cùng nhau (không
 * có thao tác "không dấu vết").
 * Bảng chỉ cho INSERT/SELECT đối với tài khoản ứng dụng (xem V4).
 */
@Service
public class NhatKyKiemToanService {

    private static final int SIZE_TOI_DA = 100;

    /**
     * Số bản ghi tối đa cho một lần xuất file; vượt quá thì yêu cầu thu hẹp bộ lọc
     * (không cắt cụt im lặng).
     */
    public static final int GIOI_HAN_XUAT = 50_000;

    private static final String CAC_COT = """
            id, nguoi_thuc_hien_id, nguoi_thuc_hien_ten, vai_tro, hanh_dong, doi_tuong_loai,
            doi_tuong_id, du_lieu_cu::text AS du_lieu_cu, du_lieu_moi::text AS du_lieu_moi, thoi_gian""";

    private final JdbcClient jdbc;
    private final ObjectMapper json;

    public NhatKyKiemToanService(JdbcClient jdbc, ObjectMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void ghi(String hanhDong, String doiTuongLoai, UUID doiTuongId, Object duLieuCu, Object duLieuMoi) {
        UUID tenantId = TenantContext.get();
        if (tenantId == null) {
            throw new IllegalStateException("Không xác định được tenant_id của phiên làm việc");
        }
        NguoiThucHien nguoi = nguoiHienTai();
        jdbc.sql("""
                INSERT INTO nhat_ky_kiem_toan
                    (tenant_id, nguoi_thuc_hien_id, nguoi_thuc_hien_ten, vai_tro,
                     hanh_dong, doi_tuong_loai, doi_tuong_id, du_lieu_cu, du_lieu_moi)
                VALUES (:tenant, :nid, :ten, :vaiTro, :hanhDong, :loai, :oid,
                        CAST(:cu AS jsonb), CAST(:moi AS jsonb))
                """)
                .param("tenant", tenantId)
                .param("nid", nguoi.id())
                .param("ten", nguoi.ten())
                .param("vaiTro", nguoi.vaiTro())
                .param("hanhDong", hanhDong)
                .param("loai", doiTuongLoai)
                .param("oid", doiTuongId)
                .param("cu", toJson(duLieuCu))
                .param("moi", toJson(duLieuMoi))
                .update();
    }

    /**
     * Tra cứu có lọc + phân trang, mới nhất trước. RLS tự giới hạn trong trung tâm
     * của người gọi.
     */
    @Transactional(readOnly = true)
    public TrangNhatKy tim(String hanhDong, UUID nguoiThucHienId, UUID doiTuongId,
            LocalDate tuNgay, LocalDate denNgay, int page, int size) {
        int p = Math.max(0, page);
        int s = Math.max(1, Math.min(size, SIZE_TOI_DA));
        BoLoc loc = boLoc(hanhDong, nguoiThucHienId, doiTuongId, tuNgay, denNgay);

        long total = jdbc.sql("SELECT count(*) FROM nhat_ky_kiem_toan" + loc.where())
                .params(loc.params()).query(Long.class).single();

        List<NhatKyResponse> items = jdbc.sql("SELECT " + CAC_COT + " FROM nhat_ky_kiem_toan" + loc.where()
                + " ORDER BY thoi_gian DESC, id DESC LIMIT :limit OFFSET :offset")
                .params(loc.params())
                .param("limit", s)
                .param("offset", (long) p * s)
                .query(dongNhatKy())
                .list();
        return new TrangNhatKy(items, p, s, total);
    }

    /**
     * Lấy TOÀN BỘ bản ghi khớp bộ lọc để xuất file, thứ tự thời gian tăng dần (cũ
     * trước, mới sau).
     * Vượt GIOI_HAN_XUAT thì báo lỗi thay vì cắt bớt, để file xuất luôn đầy đủ.
     */
    @Transactional(readOnly = true)
    public List<NhatKyResponse> layTatCaDeXuat(String hanhDong, UUID nguoiThucHienId, UUID doiTuongId,
            LocalDate tuNgay, LocalDate denNgay) {
        BoLoc loc = boLoc(hanhDong, nguoiThucHienId, doiTuongId, tuNgay, denNgay);
        long total = jdbc.sql("SELECT count(*) FROM nhat_ky_kiem_toan" + loc.where())
                .params(loc.params()).query(Long.class).single();
        if (total > GIOI_HAN_XUAT) {
            throw new IllegalArgumentException("Có " + total + " bản ghi, vượt giới hạn " + GIOI_HAN_XUAT
                    + " bản ghi mỗi lần xuất. Hãy thu hẹp khoảng ngày hoặc thêm bộ lọc.");
        }
        return jdbc.sql("SELECT " + CAC_COT + " FROM nhat_ky_kiem_toan" + loc.where()
                + " ORDER BY thoi_gian ASC, id ASC")
                .params(loc.params())
                .query(dongNhatKy())
                .list();
    }

    private record BoLoc(String where, Map<String, Object> params) {
    }

    private BoLoc boLoc(String hanhDong, UUID nguoiThucHienId, UUID doiTuongId, LocalDate tuNgay, LocalDate denNgay) {
        List<String> dieuKien = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        if (hanhDong != null && !hanhDong.isBlank()) {
            dieuKien.add("hanh_dong = :hanhDong");
            params.put("hanhDong", hanhDong.trim());
        }
        if (nguoiThucHienId != null) {
            dieuKien.add("nguoi_thuc_hien_id = :nid");
            params.put("nid", nguoiThucHienId);
        }
        if (doiTuongId != null) {
            dieuKien.add("doi_tuong_id = :oid");
            params.put("oid", doiTuongId);
        }
        if (tuNgay != null) {
            dieuKien.add("thoi_gian >= :tu");
            params.put("tu", tuNgay.atStartOfDay(ThoiGianVN.MUI_GIO).toOffsetDateTime());
        }
        if (denNgay != null) {
            dieuKien.add("thoi_gian < :den");
            params.put("den", denNgay.plusDays(1).atStartOfDay(ThoiGianVN.MUI_GIO).toOffsetDateTime());
        }
        return new BoLoc(dieuKien.isEmpty() ? "" : " WHERE " + String.join(" AND ", dieuKien), params);
    }

    private RowMapper<NhatKyResponse> dongNhatKy() {
        return (rs, n) -> new NhatKyResponse(
                rs.getObject("id", UUID.class),
                rs.getObject("nguoi_thuc_hien_id", UUID.class),
                rs.getString("nguoi_thuc_hien_ten"),
                rs.getString("vai_tro"),
                rs.getString("hanh_dong"),
                rs.getString("doi_tuong_loai"),
                rs.getObject("doi_tuong_id", UUID.class),
                docJson(rs.getString("du_lieu_cu")),
                docJson(rs.getString("du_lieu_moi")),
                rs.getObject("thoi_gian", OffsetDateTime.class));
    }

    /** Người thao tác từ JWT; không có JWT (tác vụ nền, test) -> HỆ THỐNG. */
    public static NguoiThucHien nguoiHienTai() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            return NguoiThucHien.HE_THONG;
        }
        Jwt jwt = jwtAuth.getToken();
        String ten = dauTienKhongRong(jwt.getClaimAsString("name"),
                jwt.getClaimAsString("preferred_username"), jwt.getClaimAsString("email"));
        List<String> roles = new ArrayList<>();
        for (GrantedAuthority a : auth.getAuthorities()) {
            String r = a.getAuthority();
            if (r.equals("ROLE_CENTER_ADMIN") || r.equals("ROLE_TEACHER") || r.equals("ROLE_PARENT")) {
                roles.add(r.substring("ROLE_".length()));
            }
        }
        return new NguoiThucHien(parseUuid(jwt.getSubject()), ten, roles.isEmpty() ? null : String.join(",", roles));
    }

    private String toJson(Object value) {
        if (value == null)
            return null;
        try {
            return json.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Không chuyển được dữ liệu kiểm toán sang JSON", e);
        }
    }

    private JsonNode docJson(String s) {
        if (s == null)
            return null;
        try {
            return json.readTree(s);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private static UUID parseUuid(String s) {
        try {
            return s == null ? null : UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String dauTienKhongRong(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank())
                return v;
        }
        return null;
    }
}