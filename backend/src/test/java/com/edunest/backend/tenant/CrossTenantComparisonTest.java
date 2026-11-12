package com.edunest.backend.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * So sánh CH1: cùng một bộ kịch bản truy cập chéo, chạy trên hai cấu hình.
 * - CHI_LOC_UNG_DUNG: bảng KHÔNG có RLS, mã ứng dụng "quên" lọc theo tenant
 * (lỗi lọc cố tình).
 * - RLS: bảng có RLS (dùng đúng thủ tục enable_tenant_rls của dự án), mã ứng
 * dụng vẫn "quên" lọc.
 * Dùng hai bảng thử riêng (kt_ho_so_*) nên không đụng tới dữ liệu thật. Kết quả
 * ghi ra
 * target/bao-cao-cach-ly.md để làm bằng chứng cho Bảng 3.3.
 */
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CrossTenantComparisonTest {

    private static final String BANG_APP = "kt_ho_so_app";
    private static final String BANG_RLS = "kt_ho_so_rls";

    @Autowired
    private JdbcTemplate jdbc; // kết nối edunest_app (chịu RLS)
    @Autowired
    private TransactionTemplate tx;
    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.flyway.user}")
    private String ownerUser;
    @Value("${spring.flyway.password}")
    private String ownerPassword;

    private JdbcTemplate owner; // edunest_owner: chỉ dùng tạo/xoá bảng thử
    private final UUID tenantA = UUID.randomUUID();
    private final UUID tenantB = UUID.randomUUID();
    private final UUID idA = UUID.randomUUID();
    private final UUID idB = UUID.randomUUID();

    private record KichBan(String ten, java.util.function.Function<String, Boolean> chay) {
    }

    @BeforeAll
    void taoBangThu() {
        owner = new JdbcTemplate(new DriverManagerDataSource(url, ownerUser, ownerPassword));
        for (String bang : List.of(BANG_APP, BANG_RLS)) {
            owner.execute("DROP TABLE IF EXISTS " + bang);
            owner.execute("CREATE TABLE " + bang
                    + " (id UUID PRIMARY KEY DEFAULT gen_random_uuid(), tenant_id UUID NOT NULL, ten TEXT NOT NULL)");
        }
        owner.execute("CALL enable_tenant_rls('" + BANG_RLS + "', 'tenant_id')");
    }

    @AfterAll
    void xoaBangThu() {
        owner.execute("DROP TABLE IF EXISTS " + BANG_APP);
        owner.execute("DROP TABLE IF EXISTS " + BANG_RLS);
        TenantContext.clear();
    }

    @Test
    @DisplayName("CH1/H1: RLS chặn 100% kịch bản truy cập chéo; chỉ lọc ở ứng dụng (có lỗi lọc) thì có kịch bản lọt")
    void soSanhChiLocUngDungVoiRls() throws Exception {
        List<KichBan> kichBan = List.of(
                new KichBan("K1 Đọc không có WHERE tenant_id",
                        t -> thu(tenantA,
                                () -> jdbc.queryForList("SELECT tenant_id FROM " + t, UUID.class).contains(tenantB))),
                new KichBan("K2 Đọc theo tenant_id do client gửi (= B)", t -> thu(tenantA, () -> !jdbc.queryForList(
                        "SELECT id FROM " + t + " WHERE tenant_id = ?", UUID.class, tenantB).isEmpty())),
                new KichBan("K3 Đọc hàng của B theo id (IDOR)", t -> thu(tenantA, () -> !jdbc.queryForList(
                        "SELECT id FROM " + t + " WHERE id = ?", UUID.class, idB).isEmpty())),
                new KichBan("K4 INSERT với tenant_id của B", t -> thu(tenantA, () -> {
                    try {
                        jdbc.update("INSERT INTO " + t + " (tenant_id, ten) VALUES (?, 'hack')", tenantB);
                        return true; // ghi được vào dữ liệu của B
                    } catch (DataAccessException e) {
                        return false; // bị chặn (WITH CHECK)
                    }
                })),
                new KichBan("K5 UPDATE hàng của B",
                        t -> thu(tenantA,
                                () -> jdbc.update("UPDATE " + t + " SET ten = 'bi sua' WHERE id = ?", idB) > 0)),
                new KichBan("K6 DELETE hàng của B",
                        t -> thu(tenantA, () -> jdbc.update("DELETE FROM " + t + " WHERE id = ?", idB) > 0)),
                new KichBan("K7 Không có tenant trong ngữ cảnh (fail-closed)",
                        t -> thu(null, () -> jdbc.queryForObject("SELECT count(*) FROM " + t, Integer.class) > 0)),
                new KichBan("K8 Thống kê count(*) khi là A (chỉ được thấy 1 hàng)",
                        t -> thu(tenantA, () -> jdbc.queryForObject("SELECT count(*) FROM " + t, Integer.class) > 1)));

        Map<String, boolean[]> ketQua = new LinkedHashMap<>();
        int lotUngDung = 0;
        int lotRls = 0;
        for (KichBan kb : kichBan) {
            datLaiDuLieu(BANG_APP);
            boolean lotApp = kb.chay().apply(BANG_APP);
            datLaiDuLieu(BANG_RLS);
            boolean lotRlsKb = kb.chay().apply(BANG_RLS);
            ketQua.put(kb.ten(), new boolean[] { lotApp, lotRlsKb });
            if (lotApp)
                lotUngDung++;
            if (lotRlsKb)
                lotRls++;
        }

        String baoCao = dungBaoCao(ketQua, kichBan.size(), lotUngDung, lotRls);
        System.out.println(baoCao);
        Files.writeString(Path.of("target", "bao-cao-cach-ly.md"), baoCao);

        assertThat(lotRls).as("H1: với RLS, số kịch bản lọt phải là 0").isZero();
        assertThat(lotUngDung).as("H1: chỉ lọc ở ứng dụng và có lỗi lọc thì phải có kịch bản lọt").isPositive();
    }

    /**
     * Chạy thử trong một transaction rồi luôn rollback; trả về true nếu kịch bản
     * "lọt".
     */
    private boolean thu(UUID tenant, Supplier<Boolean> thaoTac) {
        try {
            if (tenant == null)
                TenantContext.clear();
            else
                TenantContext.set(tenant);
            return Boolean.TRUE.equals(tx.execute(status -> {
                try {
                    return thaoTac.get();
                } finally {
                    status.setRollbackOnly();
                }
            }));
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Xoá sạch rồi nạp lại mỗi trung tâm một hàng (nạp qua kết nối ứng dụng nên
     * cũng đi qua RLS).
     */
    private void datLaiDuLieu(String bang) {
        owner.execute("TRUNCATE " + bang);
        nap(bang, tenantA, idA, "Hồ sơ của A");
        nap(bang, tenantB, idB, "Hồ sơ của B");
    }

    private void nap(String bang, UUID tenant, UUID id, String ten) {
        try {
            TenantContext.set(tenant);
            tx.executeWithoutResult(s -> jdbc.update(
                    "INSERT INTO " + bang + " (id, tenant_id, ten) VALUES (?, ?, ?)", id, tenant, ten));
        } finally {
            TenantContext.clear();
        }
    }

    private static String dungBaoCao(Map<String, boolean[]> ketQua, int tong, int lotApp, int lotRls) {
        List<String> dong = new ArrayList<>();
        dong.add("# Kết quả kiểm thử cách ly dữ liệu (CH1)\n");
        dong.add("| Kịch bản | Chỉ lọc ở ứng dụng (có lỗi lọc) | RLS |");
        dong.add("|---|---|---|");
        ketQua.forEach((ten, kq) -> dong
                .add("| " + ten + " | " + (kq[0] ? "LỌT" : "chặn") + " | " + (kq[1] ? "LỌT" : "chặn") + " |"));
        dong.add("| **Số kịch bản lọt** | **" + lotApp + "/" + tong + "** | **" + lotRls + "/" + tong + "** |");
        return String.join("\n", dong) + "\n";
    }
}