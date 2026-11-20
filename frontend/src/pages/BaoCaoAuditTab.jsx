import { useCallback, useEffect, useState } from 'react'
import {
    layBaoCaoDoanhThu,
    layBaoCaoChuyenCan,
    layAuditLog,
    chayThuNhacNo,
    layLichSuNhacNo,
    xuatExcelDoanhThu,
    xuatAuditLog,
} from '../api/giaiDoan3Api'

const HANH_DONG_LABEL = {
    XAC_NHAN_NOP_HOC_PHI: 'Xác nhận nộp học phí',
    HUY_XAC_NHAN_NOP_HOC_PHI: 'Hủy xác nhận nộp học phí',
    PHAT_SINH_HOA_DON: 'Phát sinh hóa đơn',
    SUA_HOA_DON_HOC_PHI: 'Sửa hóa đơn học phí',
    GHI_DIEM_DANH: 'Điểm danh buổi học',
    SUA_DIEM_DANH: 'Sửa điểm danh',
    XUAT_BAO_CAO_HOC_PHI: 'Xuất báo cáo học phí',
    XUAT_NHAT_KY_KIEM_TOAN: 'Xuất nhật ký kiểm toán',
}

const VAI_TRO_LABEL = { CENTER_ADMIN: 'Quản trị', TEACHER: 'Giáo viên', PARENT: 'Phụ huynh' }

const TRANG_THAI_NHAC = {
    DA_GUI: { label: 'Đã gửi', cls: 'badge-success' },
    DANG_GUI: { label: 'Đang gửi', cls: 'badge-info' },
    LOI: { label: 'Lỗi', cls: 'badge-danger' },
}

const KENH_LABEL = { EMAIL: 'Email', ZALO_ZNS: 'Zalo ZNS' }

const FIELD_LABEL = {
    trangThai: 'trạng thái',
    soTien: 'số tiền',
    soBuoi: 'số buổi',
    hanDong: 'hạn đóng',
    ngayThanhToan: 'ngày thanh toán',
    ghiChu: 'ghi chú',
    kyHocPhi: 'kỳ',
    thang: 'tháng',
    dinhDang: 'định dạng',
    soBanGhi: 'số bản ghi',
    soDong: 'số dòng',
}

const tien = (v) => `${Number(v ?? 0).toLocaleString('vi-VN')} đ`

const thangHienTai = () => {
    const d = new Date()
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}

const gio = (iso) =>
    iso ? new Date(iso).toLocaleString('vi-VN', { timeZone: 'Asia/Ho_Chi_Minh' }) : '-'

const hienGiaTri = (k, v) => {
    if (v === null || v === undefined || v === '') return '—'
    if (k === 'soTien') return tien(v)
    return String(v)
}

/** Tóm tắt thay đổi cũ -> mới thành một dòng dễ đọc. */
function tomTat(cu, moi) {
    if (Array.isArray(moi)) return `${moi.length} học viên được điểm danh`
    if (cu && moi && typeof cu === 'object' && typeof moi === 'object') {
        const diff = Object.keys(moi)
            .filter((k) => JSON.stringify(cu[k]) !== JSON.stringify(moi[k]))
            .map((k) => `${FIELD_LABEL[k] ?? k}: ${hienGiaTri(k, cu[k])} → ${hienGiaTri(k, moi[k])}`)
        return diff.length ? diff.join('; ') : 'Không đổi'
    }
    if (moi && typeof moi === 'object') {
        return Object.entries(moi)
            .filter(([k]) => FIELD_LABEL[k] || k === 'soHoaDonMoi')
            .map(([k, v]) => `${k === 'soHoaDonMoi' ? 'số hóa đơn mới' : FIELD_LABEL[k]}: ${hienGiaTri(k, v)}`)
            .join('; ')
    }
    return '-'
}

function TheSoLieu({ nhan, giaTri, phu, mau }) {
    return (
        <div className="stat-card" style={mau ? { borderTopColor: mau } : undefined}>
            <div className="stat-label">{nhan}</div>
            <div className="stat-value">{giaTri}</div>
            {phu && <div className="stat-sub">{phu}</div>}
        </div>
    )
}

export default function BaoCaoAuditTab() {
    const [thang, setThang] = useState(thangHienTai())
    const [doanhThu, setDoanhThu] = useState(null)
    const [chuyenCan, setChuyenCan] = useState(null)
    const [loiBaoCao, setLoiBaoCao] = useState('')

    const [nhac, setNhac] = useState([])
    const [ketQuaNhac, setKetQuaNhac] = useState('')
    const [dangChayNhac, setDangChayNhac] = useState(false)

    const [hanhDong, setHanhDong] = useState('')
    const [tuNgay, setTuNgay] = useState('')
    const [denNgay, setDenNgay] = useState('')
    const [trang, setTrang] = useState(0)
    const [audit, setAudit] = useState({ items: [], page: 0, size: 10, total: 0 })
    const [loiAudit, setLoiAudit] = useState('')

    const [bom, setBom] = useState(false)
    const [dangXuat, setDangXuat] = useState(false)
    const [thongBaoXuat, setThongBaoXuat] = useState('')

    const thongBaoLoi = (e) =>
        e.status === 403 ? 'Chỉ Quản trị trung tâm mới xem được mục này' : e.message

    const taiBaoCao = useCallback(() => {
        if (!thang) return
        setLoiBaoCao('')
        Promise.all([layBaoCaoDoanhThu(thang), layBaoCaoChuyenCan(thang)])
            .then(([dt, cc]) => {
                setDoanhThu(dt)
                setChuyenCan(cc)
            })
            .catch((e) => setLoiBaoCao(thongBaoLoi(e)))
    }, [thang])

    const taiAudit = useCallback(() => {
        setLoiAudit('')
        layAuditLog({ hanhDong, tuNgay, denNgay, page: trang, size: 10 })
            .then(setAudit)
            .catch((e) => setLoiAudit(thongBaoLoi(e)))
    }, [hanhDong, tuNgay, denNgay, trang])

    const taiLichSuNhac = useCallback(() => {
        layLichSuNhacNo(20).then(setNhac).catch(() => setNhac([]))
    }, [])

    useEffect(() => { taiBaoCao() }, [taiBaoCao])
    useEffect(() => { taiAudit() }, [taiAudit])
    useEffect(() => { taiLichSuNhac() }, [taiLichSuNhac])

    const chayNhacNo = async () => {
        setDangChayNhac(true)
        setKetQuaNhac('')
        try {
            const kq = await chayThuNhacNo()
            setKetQuaNhac(
                `Hóa đơn đến hạn: ${kq.soHoaDonDenHan} | Đã gửi: ${kq.daGui} | Bỏ qua (đã nhắc): ${kq.boQuaDaNhac} | ` +
                `Thiếu thông tin liên lạc: ${kq.boQuaKhongCoLienLac} | Lỗi: ${kq.loi}`,
            )
            taiLichSuNhac()
        } catch (e) {
            setKetQuaNhac(`Lỗi: ${thongBaoLoi(e)}`)
        } finally {
            setDangChayNhac(false)
        }
    }

    const xuatHocPhi = async () => {
        setDangXuat(true)
        setThongBaoXuat('')
        setLoiBaoCao('')
        try {
            const kq = await xuatExcelDoanhThu(thang)
            setThongBaoXuat(`Đã tải ${kq.tenFile}. SHA-256: ${kq.sha256}`)
            taiAudit() // lần xuất cũng được ghi vào nhật ký
        } catch (e) {
            setLoiBaoCao(thongBaoLoi(e))
        } finally {
            setDangXuat(false)
        }
    }

    const xuatNhatKy = async (dinhDang) => {
        setDangXuat(true)
        setThongBaoXuat('')
        setLoiAudit('')
        try {
            const kq = await xuatAuditLog({ dinhDang, bom, hanhDong, tuNgay, denNgay })
            setThongBaoXuat(`Đã tải ${kq.tenFile}. SHA-256: ${kq.sha256}`)
            taiAudit()
        } catch (e) {
            setLoiAudit(thongBaoLoi(e))
        } finally {
            setDangXuat(false)
        }
    }

    const tongTrang = Math.max(1, Math.ceil(audit.total / audit.size))

    return (
        <section className="card">
            <div className="sub-header">
                <h2>Báo cáo tổng hợp & Nhật ký kiểm toán</h2>
                <label style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <span className="muted">Tháng:</span>
                    <input type="month" value={thang} onChange={(e) => setThang(e.target.value)} />
                </label>
            </div>

            {loiBaoCao && <div className="form-error">{loiBaoCao}</div>}

            {/* ---- 3.1 Doanh thu & học phí ---- */}
            {doanhThu && (
                <>
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 8 }}>
                        <h3>Học phí tháng {doanhThu.thang}</h3>
                        <button className="btn-sm btn-success" style={{ width: 'auto' }} disabled={dangXuat} onClick={xuatHocPhi}>
                            ⬇ Xuất Excel (.xlsx)
                        </button>
                    </div>
                    <div className="stat-grid">
                        <TheSoLieu nhan="Học phí phát sinh" giaTri={tien(doanhThu.tongHocPhiPhatSinh)}
                            phu={`${doanhThu.soHoaDon} hóa đơn`} mau="#4f46e5" />
                        <TheSoLieu nhan="Đã thu" giaTri={tien(doanhThu.tongDaThu)}
                            phu={`${doanhThu.soHoaDonDaThu} hóa đơn · ${doanhThu.tiLeThuPhanTram}% giá trị`} mau="#059669" />
                        <TheSoLieu nhan="Còn nợ" giaTri={tien(doanhThu.tongConNo)}
                            phu={`${doanhThu.soHoaDonConNo} hóa đơn · ${doanhThu.soHoaDonQuaHanChuaThu} quá hạn`} mau="#dc2626" />
                        <TheSoLieu nhan="Tỉ lệ đóng đúng hạn" giaTri={`${doanhThu.tiLeDungHanPhanTram}%`}
                            phu={`${doanhThu.soHoaDonDungHan}/${doanhThu.soHoaDon} hóa đơn`} mau="#d97706" />
                    </div>
                </>
            )}

            {/* ---- 3.2 Chuyên cần ---- */}
            {chuyenCan && (
                <>
                    <h3>Chuyên cần tháng {chuyenCan.thang}</h3>
                    <div className="stat-grid">
                        <TheSoLieu nhan="Số buổi dạy" giaTri={chuyenCan.tongSoBuoiDay}
                            phu={`${chuyenCan.tongSoBuoiDaDiemDanh} buổi đã điểm danh`} mau="#4f46e5" />
                        <TheSoLieu nhan="Có mặt" giaTri={`${chuyenCan.tiLeCoMatPhanTram}%`}
                            phu={`${chuyenCan.coMat}/${chuyenCan.tongLuotDiemDanh} lượt`} mau="#059669" />
                        <TheSoLieu nhan="Vắng có phép" giaTri={`${chuyenCan.tiLeVangCoPhepPhanTram}%`}
                            phu={`${chuyenCan.vangCoPhep} lượt`} mau="#d97706" />
                        <TheSoLieu nhan="Vắng không phép" giaTri={`${chuyenCan.tiLeVangKhongPhepPhanTram}%`}
                            phu={`${chuyenCan.vangKhongPhep} lượt`} mau="#dc2626" />
                    </div>
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>Lớp</th>
                                <th>Số buổi dạy</th>
                                <th>Đã điểm danh</th>
                                <th>Có mặt</th>
                                <th>Vắng có phép</th>
                                <th>Vắng không phép</th>
                            </tr>
                        </thead>
                        <tbody>
                            {chuyenCan.theoLop.length === 0 ? (
                                <tr><td colSpan="6" className="muted" style={{ textAlign: 'center' }}>Không có buổi học nào trong tháng này</td></tr>
                            ) : chuyenCan.theoLop.map((l) => (
                                <tr key={l.lopId}>
                                    <td><strong>{l.tenLop}</strong></td>
                                    <td>{l.soBuoiDay}</td>
                                    <td>{l.soBuoiDaDiemDanh}/{l.soBuoiDay}</td>
                                    <td>{l.tiLeCoMatPhanTram}% <span className="muted">({l.coMat})</span></td>
                                    <td>{l.tiLeVangCoPhepPhanTram}% <span className="muted">({l.vangCoPhep})</span></td>
                                    <td>{l.tiLeVangKhongPhepPhanTram}% <span className="muted">({l.vangKhongPhep})</span></td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </>
            )}

            {/* ---- 1. Nhắc học phí tự động ---- */}
            <h3 style={{ marginTop: 28 }}>Nhắc học phí tự động</h3>
            <div className="form-box">
                <p className="muted" style={{ marginBottom: 10 }}>
                    Hệ thống tự quét lúc 08:00 mỗi ngày và nhắc phụ huynh trước hạn 2–3 ngày; mỗi hóa đơn chỉ được nhắc một lần
                    qua mỗi kênh. Nút dưới đây chạy ngay một lượt quét để kiểm thử.
                </p>
                <button className="btn-sm" style={{ width: 'auto' }} disabled={dangChayNhac} onClick={chayNhacNo}>
                    {dangChayNhac ? 'Đang quét...' : '⏰ Chạy quét nhắc nợ ngay'}
                </button>
                {ketQuaNhac && <div className="notice" style={{ marginTop: 10 }}>{ketQuaNhac}</div>}
            </div>
            <table className="data-table">
                <thead>
                    <tr>
                        <th>Thời gian</th>
                        <th>Học viên</th>
                        <th>Lớp / Kỳ</th>
                        <th>Kênh</th>
                        <th>Người nhận</th>
                        <th>Trạng thái</th>
                    </tr>
                </thead>
                <tbody>
                    {nhac.length === 0 ? (
                        <tr><td colSpan="6" className="muted" style={{ textAlign: 'center' }}>Chưa có lần nhắc nào</td></tr>
                    ) : nhac.map((n) => {
                        const tt = TRANG_THAI_NHAC[n.trangThai] ?? { label: n.trangThai, cls: 'badge-info' }
                        return (
                            <tr key={n.id}>
                                <td>{gio(n.ngayTao)}</td>
                                <td><strong>{n.tenHocVien}</strong></td>
                                <td>{n.tenLop} · {n.kyHocPhi}</td>
                                <td>{KENH_LABEL[n.kenhGui] ?? n.kenhGui}</td>
                                <td>{n.nguoiNhan ?? '-'}</td>
                                <td>
                                    <span className={`badge ${tt.cls}`} title={n.chiTietLoi ?? ''}>{tt.label}</span>
                                </td>
                            </tr>
                        )
                    })}
                </tbody>
            </table>

            {/* ---- 2.3 Audit log ---- */}
            <h3 style={{ marginTop: 28 }}>Nhật ký thao tác (Audit Log)</h3>
            <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 12, alignItems: 'flex-end' }}>
                <label className="field" style={{ minWidth: 220 }}>
                    <span>Hành động</span>
                    <select value={hanhDong} onChange={(e) => { setHanhDong(e.target.value); setTrang(0) }}>
                        <option value="">Tất cả</option>
                        {Object.entries(HANH_DONG_LABEL).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
                    </select>
                </label>
                <label className="field">
                    <span>Từ ngày</span>
                    <input type="date" value={tuNgay} onChange={(e) => { setTuNgay(e.target.value); setTrang(0) }} />
                </label>
                <label className="field">
                    <span>Đến ngày</span>
                    <input type="date" value={denNgay} onChange={(e) => { setDenNgay(e.target.value); setTrang(0) }} />
                </label>
                <button className="btn-sm secondary" style={{ width: 'auto' }} onClick={taiAudit}>↻ Làm mới</button>
            </div>

            <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', alignItems: 'center', marginBottom: 12 }}>
                <span className="muted">Xuất theo bộ lọc hiện tại:</span>
                <button className="btn-sm secondary" style={{ width: 'auto' }} disabled={dangXuat} onClick={() => xuatNhatKy('csv')}>⬇ CSV (UTF-8)</button>
                <button className="btn-sm secondary" style={{ width: 'auto' }} disabled={dangXuat} onClick={() => xuatNhatKy('json')}>⬇ JSON</button>
                <button className="btn-sm secondary" style={{ width: 'auto' }} disabled={dangXuat} onClick={() => xuatNhatKy('xlsx')}>⬇ Excel</button>
                <label style={{ display: 'flex', alignItems: 'center', gap: 4, fontSize: '0.85rem' }}>
                    <input type="checkbox" checked={bom} onChange={(e) => setBom(e.target.checked)} />
                    CSV thêm BOM (để bấm đúp mở bằng Excel không lỗi tiếng Việt)
                </label>
            </div>
            {thongBaoXuat && <div className="notice" style={{ marginBottom: 12, wordBreak: 'break-all' }}>{thongBaoXuat}</div>}

            {loiAudit && <div className="form-error">{loiAudit}</div>}

            <table className="data-table">
                <thead>
                    <tr>
                        <th>Thời gian</th>
                        <th>Người thực hiện</th>
                        <th>Hành động</th>
                        <th>Thay đổi</th>
                    </tr>
                </thead>
                <tbody>
                    {audit.items.length === 0 ? (
                        <tr><td colSpan="4" className="muted" style={{ textAlign: 'center' }}>Chưa có thao tác nào được ghi nhận</td></tr>
                    ) : audit.items.map((n) => (
                        <tr key={n.id}>
                            <td style={{ whiteSpace: 'nowrap' }}>{gio(n.thoiGian)}</td>
                            <td>
                                <strong>{n.nguoiThucHienTen ?? 'Không rõ'}</strong>
                                {n.vaiTro && (
                                    <span className="muted"> ({n.vaiTro.split(',').map((r) => VAI_TRO_LABEL[r] ?? r).join(', ')})</span>
                                )}
                            </td>
                            <td><span className="badge badge-info">{HANH_DONG_LABEL[n.hanhDong] ?? n.hanhDong}</span></td>
                            <td>
                                {tomTat(n.duLieuCu, n.duLieuMoi)}
                                <details style={{ marginTop: 4 }}>
                                    <summary className="muted" style={{ cursor: 'pointer', fontSize: '0.8rem' }}>Dữ liệu gốc</summary>
                                    <pre style={{ fontSize: '0.75rem', whiteSpace: 'pre-wrap', margin: '4px 0 0' }}>
                                        {JSON.stringify({ cu: n.duLieuCu, moi: n.duLieuMoi, doiTuong: `${n.doiTuongLoai}:${n.doiTuongId}` }, null, 2)}
                                    </pre>
                                </details>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>

            <div style={{ display: 'flex', gap: 12, alignItems: 'center', justifyContent: 'flex-end', marginTop: 12 }}>
                <span className="muted">Tổng {audit.total} bản ghi · Trang {audit.page + 1}/{tongTrang}</span>
                <button className="btn-sm secondary" style={{ width: 'auto' }} disabled={trang === 0}
                    onClick={() => setTrang((t) => Math.max(0, t - 1))}>← Trước</button>
                <button className="btn-sm secondary" style={{ width: 'auto' }} disabled={trang + 1 >= tongTrang}
                    onClick={() => setTrang((t) => t + 1)}>Sau →</button>
            </div>
        </section>
    )
}