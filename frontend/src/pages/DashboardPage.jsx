import { useEffect, useState } from 'react'
import { layTrungTamHienTai } from '../api/tenantApi'
import BaoCaoAuditTab from './BaoCaoAuditTab'
import { useAuth } from '../auth/AuthContext'
import {
    previewExcelHocVien,
    importExcelHocVien,
    layDanhSachHocVien,
    layDanhSachHocVienKhaDung,
    layDanhSachLop,
    taoLop,
    xoaLop,
    taoBuoiHocThuCong,
    sinhBuoiHoc,
    layDanhSachBuoiHoc,
    ghiDanhHocVien,
    layDanhSachGhiDanh,
    huyGhiDanh,
    doiTrangThaiGhiDanh,
    layBangDiemDanh,
    capNhatDiemDanh,
    tinhHocPhi,
    layDanhSachHoaDon,
    capNhatTrangThaiHoaDon
} from '../api/giaiDoan2Api'

const ROLE_LABEL = {
    CENTER_ADMIN: 'Quản trị trung tâm',
    TEACHER: 'Giáo viên',
    PARENT: 'Phụ huynh',
}

const THU_IN_WEEK = [
    { label: 'Thứ 2', val: 1 },
    { label: 'Thứ 3', val: 2 },
    { label: 'Thứ 4', val: 3 },
    { label: 'Thứ 5', val: 4 },
    { label: 'Thứ 6', val: 5 },
    { label: 'Thứ 7', val: 6 },
    { label: 'Chủ Nhật', val: 7 },
]

export default function DashboardPage() {
    const { user, logout } = useAuth()
    const [tenant, setTenant] = useState(null)
    const [activeTab, setActiveTab] = useState('hoc-vien')
    const [error, setError] = useState('')
    const [successMsg, setSuccessMsg] = useState('')

    // Module 1 states
    const [hocViens, setHocViens] = useState([])
    const [excelFile, setExcelFile] = useState(null)
    const [previewData, setPreviewData] = useState(null)
    const [loadingExcel, setLoadingExcel] = useState(false)

    // Module 2 states
    const [lops, setLops] = useState([])
    const [tenLopMoi, setTenLopMoi] = useState('')
    const [hocPhiMoi, setHocPhiMoi] = useState('150000')
    const [selectedLopForSchedule, setSelectedLopForSchedule] = useState(null)
    const [cheDoNhapLich, setCheDoNhapLich] = useState('dinh-ky') // 'dinh-ky' hoặc 'thu-cong'

    // Buổi học: Định kỳ
    const [ngayBatDau, setNgayBatDau] = useState(new Date().toISOString().split('T')[0])
    const [soBuoi, setSoBuoi] = useState(12)
    const [thuSelected, setThuSelected] = useState([1, 3, 5]) // Thứ 2, 4, 6
    const [gioBatDau, setGioBatDau] = useState('18:00')
    const [thoiLuongDinhKy, setThoiLuongDinhKy] = useState(90)
    const [boQuaKhiTrungLich, setBoQuaKhiTrungLich] = useState(true)
    const [choPhepQuaKhuDinhKy, setChoPhepQuaKhuDinhKy] = useState(false)
    const [dsBiBoQua, setDsBiBoQua] = useState([])

    // Buổi học: Thủ công
    const [ngayHocThuCong, setNgayHocThuCong] = useState(new Date().toISOString().split('T')[0])
    const [gioBatDauThuCong, setGioBatDauThuCong] = useState('18:00')
    const [thoiLuongThuCong, setThoiLuongThuCong] = useState(90)
    const [choPhepQuaKhuThuCong, setChoPhepQuaKhuThuCong] = useState(false)

    const [buoiHocs, setBuoiHocs] = useState([])

    // Module 3 states (Ghi danh)
    const [selectedLopGhiDanh, setSelectedLopGhiDanh] = useState('')
    const [dsGhiDanh, setDsGhiDanh] = useState([])
    const [hocViensKhaDung, setHocViensKhaDung] = useState([])
    const [selectedHvIds, setSelectedHvIds] = useState([])

    // Module 4 states (Điểm danh)
    const [selectedLopDiemDanh, setSelectedLopDiemDanh] = useState('')
    const [buoiHocDiemDanhList, setBuoiHocDiemDanhList] = useState([])
    const [selectedBuoiHocId, setSelectedBuoiHocId] = useState('')
    const [bangDiemDanh, setBangDiemDanh] = useState([])

    // Module 5 states (Học phí)
    const [selectedLopHocPhi, setSelectedLopHocPhi] = useState('')
    const [kyHocPhi, setKyHocPhi] = useState('Tháng 11/2026')
    const [hanDong, setHanDong] = useState('2026-11-30')
    const [hoaDons, setHoaDons] = useState([])

    // Clear notifications on tab change
    const switchTab = (tab) => {
        setActiveTab(tab)
        setError('')
        setSuccessMsg('')
        setDsBiBoQua([])
    }

    // Load initial tenant info
    useEffect(() => {
        layTrungTamHienTai().then(setTenant).catch((e) => setError(e.message))
    }, [])

    // Refresh functions
    const refreshHocViens = () => layDanhSachHocVien().then(setHocViens).catch(e => setError(e.message))
    const refreshHocViensKhaDung = () => layDanhSachHocVienKhaDung().then(setHocViensKhaDung).catch(e => setError(e.message))
    const refreshLops = () => layDanhSachLop().then(setLops).catch(e => setError(e.message))
    const refreshHoaDons = () => layDanhSachHoaDon().then(setHoaDons).catch(e => setError(e.message))

    useEffect(() => {
        if (activeTab === 'hoc-vien') refreshHocViens()
        if (activeTab === 'lop' || activeTab === 'ghi-danh' || activeTab === 'diem-danh' || activeTab === 'hoc-phi') {
            refreshLops()
        }
        if (activeTab === 'ghi-danh') {
            refreshHocViensKhaDung()
        }
        if (activeTab === 'hoc-phi') refreshHoaDons()
    }, [activeTab])

    // --- MODULE 1: Excel handlers ---
    const handlePreviewExcel = async () => {
        if (!excelFile) { setError('Vui lòng chọn file Excel .xlsx'); return }
        setError(''); setSuccessMsg(''); setLoadingExcel(true)
        try {
            const data = await previewExcelHocVien(excelFile)
            setPreviewData(data)
            setSuccessMsg(`Đã phân tích xong: ${data.soDongHopLe}/${data.tongSoDong} dòng hợp lệ`)
        } catch (e) {
            setError(e.message)
        } finally {
            setLoadingExcel(false)
        }
    }

    const handleImportExcel = async () => {
        if (!excelFile) return
        setError(''); setSuccessMsg(''); setLoadingExcel(true)
        try {
            const res = await importExcelHocVien(excelFile)
            setSuccessMsg(res.thongBao)
            setPreviewData(null)
            setExcelFile(null)
            refreshHocViens()
        } catch (e) {
            setError(e.message)
        } finally {
            setLoadingExcel(false)
        }
    }

    // --- MODULE 2: Lớp học handlers ---
    const handleTaoLop = async (e) => {
        e.preventDefault()
        if (!tenLopMoi.trim()) return
        setError(''); setSuccessMsg('')
        try {
            await taoLop({ tenLop: tenLopMoi, hocPhiMoiBuoi: Number(hocPhiMoi) })
            setTenLopMoi('')
            setSuccessMsg('Tạo lớp học thành công!')
            refreshLops()
        } catch (e) { setError(e.message) }
    }

    const handleXoaLop = async (id) => {
        if (!confirm('Bạn có chắc chắn muốn xóa lớp học này?')) return
        try {
            await xoaLop(id)
            setSuccessMsg('Đã xóa lớp học!')
            refreshLops()
        } catch (e) { setError(e.message) }
    }

    const handleXemBuoiHoc = async (lop) => {
        setSelectedLopForSchedule(lop)
        setDsBiBoQua([])
        const list = await layDanhSachBuoiHoc(lop.id).catch(() => [])
        setBuoiHocs(list)
    }

    // Chế độ 1: Tạo buổi học thủ công
    const handleTaoBuoiHocThuCong = async (e) => {
        e.preventDefault()
        if (!selectedLopForSchedule) return
        setError(''); setSuccessMsg('')
        try {
            await taoBuoiHocThuCong(selectedLopForSchedule.id, {
                lopId: selectedLopForSchedule.id,
                ngayHoc: ngayHocThuCong,
                gioBatDau: gioBatDauThuCong.length === 5 ? `${gioBatDauThuCong}:00` : gioBatDauThuCong,
                thoiLuongPhut: Number(thoiLuongThuCong),
                choPhepNgayQuaKhu: choPhepQuaKhuThuCong
            })
            setSuccessMsg(`Tạo thành công buổi học ngày ${ngayHocThuCong}!`)
            const list = await layDanhSachBuoiHoc(selectedLopForSchedule.id)
            setBuoiHocs(list)
            refreshLops()
        } catch (e) { setError(e.message) }
    }

    // Chế độ 2: Sinh buổi học định kỳ
    const handleSinhBuoiHocDinhKy = async (e) => {
        e.preventDefault()
        if (!selectedLopForSchedule) return
        setError(''); setSuccessMsg(''); setDsBiBoQua([])
        try {
            const res = await sinhBuoiHoc(selectedLopForSchedule.id, {
                ngayBatDau,
                soBuoi: Number(soBuoi),
                thuTrongTuan: thuSelected,
                gioBatDau: gioBatDau.length === 5 ? `${gioBatDau}:00` : gioBatDau,
                thoiLuongPhut: Number(thoiLuongDinhKy),
                boQuaKhiTrungLich,
                choPhepNgayQuaKhu: choPhepQuaKhuDinhKy
            })

            const list = await layDanhSachBuoiHoc(selectedLopForSchedule.id)
            setBuoiHocs(list)
            setDsBiBoQua(res.danhSachBiBoQua || [])

            if (res.soBuoiBiBoQua > 0) {
                setSuccessMsg(`Đã sinh thành công ${res.soBuoiThanhCong} buổi học. Có ${res.soBuoiBiBoQua} buổi bị bỏ qua do trùng lịch/nghỉ!`)
            } else {
                setSuccessMsg(`Tự động sinh thành công trọn vẹn ${res.soBuoiThanhCong} buổi học!`)
            }
            refreshLops()
        } catch (e) { setError(e.message) }
    }

    // --- MODULE 3: Ghi danh handlers ---
    useEffect(() => {
        if (selectedLopGhiDanh) {
            layDanhSachGhiDanh(selectedLopGhiDanh).then(setDsGhiDanh).catch(e => setError(e.message))
            refreshHocViensKhaDung()
        } else {
            setDsGhiDanh([])
        }
    }, [selectedLopGhiDanh])

    const handleGhiDanh = async () => {
        if (!selectedLopGhiDanh || selectedHvIds.length === 0) {
            setError('Vui lòng chọn lớp và ít nhất một học viên')
            return
        }
        setError(''); setSuccessMsg('')
        try {
            const res = await ghiDanhHocVien({
                lopId: selectedLopGhiDanh,
                hocVienIds: selectedHvIds,
                ngayBatDau: new Date().toISOString().split('T')[0]
            })
            setDsGhiDanh(res)
            setSelectedHvIds([])
            setSuccessMsg('Ghi danh học viên thành công!')
            refreshHocViensKhaDung()
            refreshLops()
        } catch (e) { setError(e.message) }
    }

    const handleHuyGhiDanh = async (id) => {
        if (!confirm('Bạn có muốn hủy ghi danh học viên này? (Nếu đã có hóa đơn học phí, hệ thống sẽ tự động chuyển sang trạng thái ĐÃ NGHỈ để bảo toàn sổ sách)')) return
        try {
            await huyGhiDanh(id)
            setSuccessMsg('Đã cập nhật trạng thái ghi danh!')
            layDanhSachGhiDanh(selectedLopGhiDanh).then(setDsGhiDanh)
            refreshHocViensKhaDung()
            refreshLops()
        } catch (e) { setError(e.message) }
    }

    const handleDoiTrangThaiGhiDanh = async (id, trangThai) => {
        try {
            await doiTrangThaiGhiDanh(id, trangThai)
            setSuccessMsg(`Đã chuyển trạng thái ghi danh thành ${trangThai}`)
            layDanhSachGhiDanh(selectedLopGhiDanh).then(setDsGhiDanh)
            refreshHocViensKhaDung()
            refreshLops()
        } catch (e) { setError(e.message) }
    }

    // --- MODULE 4: Điểm danh handlers ---
    useEffect(() => {
        if (selectedLopDiemDanh) {
            layDanhSachBuoiHoc(selectedLopDiemDanh)
                .then(list => {
                    setBuoiHocDiemDanhList(list)
                    if (list.length > 0) setSelectedBuoiHocId(list[0].id)
                    else { setSelectedBuoiHocId(''); setBangDiemDanh([]) }
                })
                .catch(() => setBuoiHocDiemDanhList([]))
        }
    }, [selectedLopDiemDanh])

    useEffect(() => {
        if (selectedBuoiHocId) {
            layBangDiemDanh(selectedBuoiHocId).then(setBangDiemDanh).catch(e => setError(e.message))
        } else {
            setBangDiemDanh([])
        }
    }, [selectedBuoiHocId])

    const updateDiemDanhState = (hocVienId, field, val) => {
        setBangDiemDanh(prev => prev.map(row => row.hocVienId === hocVienId ? { ...row, [field]: val } : row))
    }

    const handleLuuDiemDanh = async () => {
        if (!selectedBuoiHocId) return
        setError(''); setSuccessMsg('')
        const payload = {
            buoiHocId: selectedBuoiHocId,
            danhSach: bangDiemDanh.map(r => ({
                hocVienId: r.hocVienId,
                trangThai: r.trangThai || 'CO_MAT',
                ghiChu: r.ghiChu || ''
            }))
        }
        try {
            const res = await capNhatDiemDanh(payload)
            setBangDiemDanh(res)
            setSuccessMsg('Lưu kết quả điểm danh thành công!')
        } catch (e) { setError(e.message) }
    }

    // --- MODULE 5: Tính học phí handlers ---
    const handleTinhHocPhi = async (e) => {
        e.preventDefault()
        if (!selectedLopHocPhi) { setError('Vui lòng chọn lớp học để tính phí'); return }
        setError(''); setSuccessMsg('')
        try {
            const res = await tinhHocPhi({
                lopId: selectedLopHocPhi,
                kyHocPhi,
                hanDong
            })
            setHoaDons(res)
            setSuccessMsg(`Phát sinh và tính học phí thành công cho ${res.length} học viên!`)
        } catch (e) { setError(e.message) }
    }

    const toggleTrangThaiHoaDon = async (hd) => {
        const nextStatus = hd.trangThai === 'DA_THANH_TOAN' ? 'CHUA_THANH_TOAN' : 'DA_THANH_TOAN'
        try {
            await capNhatTrangThaiHoaDon(hd.id, nextStatus)
            refreshHoaDons()
        } catch (e) { setError(e.message) }
    }

    const roles = user.roles.filter((r) => ROLE_LABEL[r]).map((r) => ROLE_LABEL[r])

    return (
        <main className="app-page">
            <header className="app-header">
                <div>
                    <strong className="brand">EduNest SaaS</strong>
                    <span className="muted" style={{ marginLeft: 12 }}>
                        {tenant ? `${tenant.tenTrungTam}` : ''} ({roles.join(', ') || 'Admin'})
                    </span>
                </div>
                <button className="secondary" onClick={logout}>Đăng xuất</button>
            </header>

            {error && <div className="form-error" style={{ padding: 10, background: '#fee2e2', borderRadius: 8 }}>{error}</div>}
            {successMsg && <div className="notice" style={{ marginBottom: 16 }}>{successMsg}</div>}

            <nav className="tabs-nav">
                <button className={`tab-btn ${activeTab === 'hoc-vien' ? 'active' : ''}`} onClick={() => switchTab('hoc-vien')}>
                    1. Học viên & Excel
                </button>
                <button className={`tab-btn ${activeTab === 'lop' ? 'active' : ''}`} onClick={() => switchTab('lop')}>
                    2. Lớp học & Buổi học
                </button>
                <button className={`tab-btn ${activeTab === 'ghi-danh' ? 'active' : ''}`} onClick={() => switchTab('ghi-danh')}>
                    3. Ghi danh
                </button>
                <button className={`tab-btn ${activeTab === 'diem-danh' ? 'active' : ''}`} onClick={() => switchTab('diem-danh')}>
                    4. Điểm danh
                </button>
                <button className={`tab-btn ${activeTab === 'hoc-phi' ? 'active' : ''}`} onClick={() => switchTab('hoc-phi')}>
                    5. Tính học phí
                </button>
                <button className={`tab-btn ${activeTab === 'bao-cao' ? 'active' : ''}`} onClick={() => switchTab('bao-cao')}>
                    6. Báo cáo & Audit Log
                </button>
            </nav>

            {/* TAB 6: BÁO CÁO & AUDIT LOG */}
            {activeTab === 'bao-cao' && <BaoCaoAuditTab />}

            {/* TAB 1: HỌC VIÊN & EXCEL */}
            {activeTab === 'hoc-vien' && (
                <section className="card">
                    <div className="sub-header">
                        <h2>Quản lý Học viên & Nhập Excel (Apache POI)</h2>
                    </div>

                    <div className="form-box">
                        <h3>Tải lên tệp Excel (.xlsx) danh sách học viên & phụ huynh</h3>
                        <p className="muted" style={{ marginBottom: 10 }}>
                            Cột mẫu: <code>Họ tên PH | SĐT Zalo | Email | Họ tên Học Viên | Ngày sinh (dd/MM/yyyy)</code>
                        </p>
                        <div style={{ display: 'flex', gap: 10, alignItems: 'center', flexWrap: 'wrap' }}>
                            <input
                                type="file"
                                accept=".xlsx"
                                onChange={(e) => { setExcelFile(e.target.files[0]); setPreviewData(null); }}
                            />
                            <button
                                className="btn-sm"
                                style={{ width: 'auto' }}
                                disabled={!excelFile || loadingExcel}
                                onClick={handlePreviewExcel}
                            >
                                {loadingExcel ? 'Đang đọc...' : 'Xem trước (Preview)'}
                            </button>
                            {previewData && (
                                <button
                                    className="btn-sm btn-success"
                                    style={{ width: 'auto' }}
                                    disabled={previewData.soDongHopLe === 0 || loadingExcel}
                                    onClick={handleImportExcel}
                                >
                                    Xác nhận nhập ({previewData.soDongHopLe} dòng)
                                </button>
                            )}
                        </div>

                        {previewData && (
                            <div className="preview-box">
                                <strong>Xem trước kết quả đọc từ Excel:</strong>
                                <span style={{ marginLeft: 10 }}>
                                    Tổng: {previewData.tongSoDong} | Hợp lệ: <span style={{ color: '#059669' }}>{previewData.soDongHopLe}</span> | Lỗi: <span style={{ color: '#dc2626' }}>{previewData.soDongLoi}</span>
                                </span>
                                <table className="data-table">
                                    <thead>
                                        <tr>
                                            <th>Dòng</th>
                                            <th>Phụ huynh</th>
                                            <th>SĐT/Zalo</th>
                                            <th>Học viên</th>
                                            <th>Ngày sinh</th>
                                            <th>Trạng thái</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {previewData.chiTiet.map((r, i) => (
                                            <tr key={i} style={{ background: r.hopLe ? '#f0fdf4' : '#fef2f2' }}>
                                                <td>{r.rowNumber}</td>
                                                <td>{r.hoTenPhuHuynh}</td>
                                                <td>{r.sdtZalo}</td>
                                                <td>{r.hoTenHocVien}</td>
                                                <td>{r.ngaySinh}</td>
                                                <td>
                                                    {r.hopLe ? (
                                                        <span className="badge badge-success">Hợp lệ</span>
                                                    ) : (
                                                        <span className="badge badge-danger">{r.loi}</span>
                                                    )}
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </div>

                    <h3>Danh sách học viên hiện tại ({hocViens.length})</h3>
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>Họ tên học viên</th>
                                <th>Ngày sinh</th>
                                <th>Phụ huynh</th>
                                <th>SĐT / Zalo</th>
                                <th>Email</th>
                            </tr>
                        </thead>
                        <tbody>
                            {hocViens.length === 0 ? (
                                <tr><td colSpan="5" className="muted" style={{ textAlign: 'center' }}>Chưa có học viên nào</td></tr>
                            ) : (
                                hocViens.map(hv => (
                                    <tr key={hv.id}>
                                        <td><strong>{hv.hoTen}</strong></td>
                                        <td>{hv.ngaySinh || '-'}</td>
                                        <td>{hv.tenPhuHuynh || '-'}</td>
                                        <td>{hv.sdtZalo || '-'}</td>
                                        <td>{hv.email || '-'}</td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </section>
            )}

            {/* TAB 2: LỚP HỌC & BUỔI HỌC */}
            {activeTab === 'lop' && (
                <section className="card">
                    <div className="sub-header">
                        <h2>Quản lý Lớp học & Buổi học</h2>
                    </div>

                    <div className="grid-2">
                        <div>
                            <form className="form-box" onSubmit={handleTaoLop}>
                                <h3>Thêm Lớp Học Mới</h3>
                                <label className="field">
                                    <span>Tên lớp học</span>
                                    <input value={tenLopMoi} onChange={e => setTenLopMoi(e.target.value)} placeholder="Ví dụ: Tiếng Anh Giao Tiếp K01" required />
                                </label>
                                <label className="field">
                                    <span>Học phí mỗi buổi (VNĐ)</span>
                                    <input type="number" value={hocPhiMoi} onChange={e => setHocPhiMoi(e.target.value)} step="10000" min="0" required />
                                </label>
                                <button type="submit" className="btn-sm" style={{ width: 'auto' }}>+ Tạo lớp học</button>
                            </form>

                            <h3>Danh sách Lớp học ({lops.length})</h3>
                            <table className="data-table">
                                <thead>
                                    <tr>
                                        <th>Tên lớp</th>
                                        <th>Học phí/buổi</th>
                                        <th>Buổi học</th>
                                        <th>Học viên</th>
                                        <th>Thao tác</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {lops.length === 0 ? (
                                        <tr><td colSpan="5" className="muted" style={{ textAlign: 'center' }}>Chưa có lớp nào</td></tr>
                                    ) : (
                                        lops.map(l => (
                                            <tr key={l.id} style={{ background: selectedLopForSchedule?.id === l.id ? '#eef2ff' : 'transparent' }}>
                                                <td><strong>{l.tenLop}</strong></td>
                                                <td>{Number(l.hocPhiMoiBuoi).toLocaleString('vi-VN')} đ</td>
                                                <td><span className="badge badge-info">{l.soBuoiHoc} buổi</span></td>
                                                <td><span className="badge badge-success">{l.soHocVien} bạn</span></td>
                                                <td>
                                                    <div className="btn-group">
                                                        <button className="btn-sm secondary" onClick={() => handleXemBuoiHoc(l)}>Lịch học</button>
                                                        <button className="btn-sm btn-danger" onClick={() => handleXoaLop(l.id)}>Xóa</button>
                                                    </div>
                                                </td>
                                            </tr>
                                        ))
                                    )}
                                </tbody>
                            </table>
                        </div>

                        <div>
                            {selectedLopForSchedule ? (
                                <div className="form-box">
                                    <h3>Thiết lập Lịch học: {selectedLopForSchedule.tenLop}</h3>
                                    
                                    {/* Toggle 2 chế độ nhập ngày */}
                                    <div style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
                                        <button
                                            type="button"
                                            className={`btn-sm ${cheDoNhapLich === 'dinh-ky' ? 'btn-success' : 'secondary'}`}
                                            onClick={() => setCheDoNhapLich('dinh-ky')}
                                        >
                                            ⚡ 1. Theo lịch định kỳ (Auto)
                                        </button>
                                        <button
                                            type="button"
                                            className={`btn-sm ${cheDoNhapLich === 'thu-cong' ? 'btn-success' : 'secondary'}`}
                                            onClick={() => setCheDoNhapLich('thu-cong')}
                                        >
                                            ✍️ 2. Nhập thủ công (Manual)
                                        </button>
                                    </div>

                                    {/* CHẾ ĐỘ 1: THEO LỊCH ĐỊNH KỲ */}
                                    {cheDoNhapLich === 'dinh-ky' && (
                                        <form onSubmit={handleSinhBuoiHocDinhKy}>
                                            <label className="field">
                                                <span>Ngày bắt đầu</span>
                                                <input type="date" value={ngayBatDau} onChange={e => setNgayBatDau(e.target.value)} required />
                                            </label>
                                            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
                                                <label className="field">
                                                    <span>Số buổi cần sinh</span>
                                                    <input type="number" value={soBuoi} onChange={e => setSoBuoi(e.target.value)} min="1" max="100" required />
                                                </label>
                                                <label className="field">
                                                    <span>Thời lượng (phút)</span>
                                                    <input type="number" value={thoiLuongDinhKy} onChange={e => setThoiLuongDinhKy(e.target.value)} min="15" step="15" required />
                                                </label>
                                            </div>
                                            <label className="field">
                                                <span>Giờ bắt đầu</span>
                                                <input type="time" value={gioBatDau} onChange={e => setGioBatDau(e.target.value)} required />
                                            </label>
                                            <div style={{ marginBottom: 12 }}>
                                                <span style={{ fontSize: '0.875rem', fontWeight: 500, display: 'block', marginBottom: 4 }}>Thứ trong tuần:</span>
                                                <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                                                    {THU_IN_WEEK.map(t => (
                                                        <label key={t.val} style={{ fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: 4 }}>
                                                            <input
                                                                type="checkbox"
                                                                checked={thuSelected.includes(t.val)}
                                                                onChange={e => {
                                                                    if (e.target.checked) setThuSelected([...thuSelected, t.val])
                                                                    else setThuSelected(thuSelected.filter(v => v !== t.val))
                                                                }}
                                                            />
                                                            {t.label}
                                                        </label>
                                                    ))}
                                                </div>
                                            </div>
                                            <div style={{ display: 'flex', flexDirection: 'column', gap: 6, marginBottom: 14 }}>
                                                <label style={{ fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: 6 }}>
                                                    <input type="checkbox" checked={boQuaKhiTrungLich} onChange={e => setBoQuaKhiTrungLich(e.target.checked)} />
                                                    Tự động bỏ qua và cảnh báo nếu ngày bị trùng lịch giáo viên/lớp
                                                </label>
                                                <label style={{ fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: 6 }}>
                                                    <input type="checkbox" checked={choPhepQuaKhuDinhKy} onChange={e => setChoPhepQuaKhuDinhKy(e.target.checked)} />
                                                    Cho phép sinh ngày trong quá khứ (nhập bù dữ liệu cũ)
                                                </label>
                                            </div>
                                            <button type="submit" className="btn-sm btn-success" style={{ width: 'auto' }}>
                                                ⚡ Tự động sinh {soBuoi} buổi học
                                            </button>
                                        </form>
                                    )}

                                    {/* CHẾ ĐỘ 2: NHẬP THỦ CÔNG */}
                                    {cheDoNhapLich === 'thu-cong' && (
                                        <form onSubmit={handleTaoBuoiHocThuCong}>
                                            <label className="field">
                                                <span>Ngày học cụ thể</span>
                                                <input type="date" value={ngayHocThuCong} onChange={e => setNgayHocThuCong(e.target.value)} required />
                                            </label>
                                            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
                                                <label className="field">
                                                    <span>Giờ bắt đầu</span>
                                                    <input type="time" value={gioBatDauThuCong} onChange={e => setGioBatDauThuCong(e.target.value)} required />
                                                </label>
                                                <label className="field">
                                                    <span>Thời lượng (phút)</span>
                                                    <input type="number" value={thoiLuongThuCong} onChange={e => setThoiLuongThuCong(e.target.value)} min="15" step="15" required />
                                                </label>
                                            </div>
                                            <label style={{ fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: 6, marginBottom: 14 }}>
                                                <input type="checkbox" checked={choPhepQuaKhuThuCong} onChange={e => setChoPhepQuaKhuThuCong(e.target.checked)} />
                                                Cho phép nhập ngày trong quá khứ (nhập bù)
                                            </label>
                                            <button type="submit" className="btn-sm btn-success" style={{ width: 'auto' }}>
                                                + Lưu buổi học thủ công
                                            </button>
                                        </form>
                                    )}

                                    {/* Cảnh báo các buổi bị bỏ qua do xung đột */}
                                    {dsBiBoQua.length > 0 && (
                                        <div style={{ marginTop: 14, padding: 10, background: '#fffbeb', border: '1px solid #fde68a', borderRadius: 8 }}>
                                            <strong style={{ color: '#b45309' }}>⚠️ Danh sách buổi bị bỏ qua do xung đột:</strong>
                                            <ul style={{ margin: '6px 0 0', paddingLeft: 20, fontSize: '0.85rem', color: '#92400e' }}>
                                                {dsBiBoQua.map((item, i) => (
                                                    <li key={i}>Ngày {item.ngayHoc} ({item.gioBatDau} - {item.gioKetThuc}): {item.lyDo}</li>
                                                ))}
                                            </ul>
                                        </div>
                                    )}

                                    <h4 style={{ marginTop: 18, marginBottom: 8 }}>Lịch các buổi học hiện có ({buoiHocs.length})</h4>
                                    <div style={{ maxHeight: 220, overflowY: 'auto' }}>
                                        <table className="data-table">
                                            <thead>
                                                <tr>
                                                    <th>STT</th>
                                                    <th>Ngày học</th>
                                                    <th>Khung giờ</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {buoiHocs.length === 0 ? (
                                                    <tr><td colSpan="3" className="muted" style={{ textAlign: 'center' }}>Chưa có buổi học nào</td></tr>
                                                ) : (
                                                    buoiHocs.map((bh, idx) => (
                                                        <tr key={bh.id}>
                                                            <td>Buổi {idx + 1}</td>
                                                            <td>{bh.ngayHoc}</td>
                                                            <td>{bh.gioBatDau} - {bh.gioKetThuc || '90p'}</td>
                                                        </tr>
                                                    ))
                                                )}
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            ) : (
                                <div className="form-box muted" style={{ textAlign: 'center', padding: 40 }}>
                                    Chọn một lớp học ở bên trái để thiết lập lịch học và xem các buổi học.
                                </div>
                            )}
                        </div>
                    </div>
                </section>
            )}

            {/* TAB 3: GHI DANH HỌC VIÊN */}
            {activeTab === 'ghi-danh' && (
                <section className="card">
                    <div className="sub-header">
                        <h2>Ghi danh Học viên vào Lớp học</h2>
                    </div>

                    <div className="grid-2">
                        <div className="form-box">
                            <h3>1. Chọn Lớp Học</h3>
                            <select
                                style={{ width: '100%', padding: '10px 12px', borderRadius: 8, border: '1px solid #d1d5db', marginBottom: 16 }}
                                value={selectedLopGhiDanh}
                                onChange={e => setSelectedLopGhiDanh(e.target.value)}
                            >
                                <option value="">-- Chọn một lớp học --</option>
                                {lops.map(l => (
                                    <option key={l.id} value={l.id}>{l.tenLop} ({l.soHocVien} học viên)</option>
                                ))}
                            </select>

                            <h3>2. Chọn Học Viên Khả Dụng ({hocViensKhaDung.length})</h3>
                            <p className="muted" style={{ fontSize: '0.8rem', marginBottom: 8 }}>
                                🔒 <strong>Quy tắc nghiệp vụ:</strong> Học viên đang theo học bất kỳ lớp nào trong trung tâm sẽ tự động được ẩn khỏi danh sách dưới đây để chống trùng/chéo lớp.
                            </p>
                            <div style={{ maxHeight: 250, overflowY: 'auto', border: '1px solid #e5e7eb', borderRadius: 8, padding: 8 }}>
                                {hocViensKhaDung.length === 0 ? (
                                    <div className="muted" style={{ textAlign: 'center', padding: 20 }}>
                                        Không có học viên nào đang rảnh (tất cả học viên đều đã được ghi danh vào các lớp).
                                    </div>
                                ) : (
                                    hocViensKhaDung.map(hv => (
                                        <label key={hv.id} style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '6px 4px', borderBottom: '1px solid #f3f4f6' }}>
                                            <input
                                                type="checkbox"
                                                checked={selectedHvIds.includes(hv.id)}
                                                onChange={e => {
                                                    if (e.target.checked) setSelectedHvIds([...selectedHvIds, hv.id])
                                                    else setSelectedHvIds(selectedHvIds.filter(id => id !== hv.id))
                                                }}
                                            />
                                            <span>
                                                <strong>{hv.hoTen}</strong> {hv.sdtZalo ? `(${hv.sdtZalo})` : ''}
                                            </span>
                                        </label>
                                    ))
                                )}
                            </div>

                            <button
                                style={{ marginTop: 16, width: 'auto' }}
                                disabled={!selectedLopGhiDanh || selectedHvIds.length === 0}
                                onClick={handleGhiDanh}
                            >
                                + Ghi danh ({selectedHvIds.length}) học viên
                            </button>
                        </div>

                        <div>
                            <h3>Học viên trong lớp ({dsGhiDanh.length})</h3>
                            <table className="data-table">
                                <thead>
                                    <tr>
                                        <th>Học viên</th>
                                        <th>Phụ huynh</th>
                                        <th>Trạng thái</th>
                                        <th>Thao tác</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {dsGhiDanh.length === 0 ? (
                                        <tr><td colSpan="4" className="muted" style={{ textAlign: 'center' }}>Chưa có học viên nào trong lớp này</td></tr>
                                    ) : (
                                        dsGhiDanh.map(gd => (
                                            <tr key={gd.id}>
                                                <td><strong>{gd.tenHocVien}</strong></td>
                                                <td>{gd.tenPhuHuynh || '-'} {gd.sdtZalo ? `(${gd.sdtZalo})` : ''}</td>
                                                <td>
                                                    {gd.trangThai === 'DANG_HOC' ? (
                                                        <span className="badge badge-success">Đang học</span>
                                                    ) : (
                                                        <span className="badge" style={{ background: '#e5e7eb', color: '#374151' }}>{gd.trangThai}</span>
                                                    )}
                                                </td>
                                                <td>
                                                    <div className="btn-group">
                                                        {gd.trangThai === 'DANG_HOC' && (
                                                            <button 
                                                                className="btn-sm secondary" 
                                                                title="Chuyển trạng thái sang ĐÃ NGHỈ để bảo toàn lịch sử hóa đơn"
                                                                onClick={() => handleDoiTrangThaiGhiDanh(gd.id, 'DA_NGHI')}
                                                            >
                                                                Nghỉ học
                                                            </button>
                                                        )}
                                                        <button 
                                                            className="btn-sm btn-danger" 
                                                            title="Xóa ghi danh"
                                                            onClick={() => handleHuyGhiDanh(gd.id)}
                                                        >
                                                            Hủy
                                                        </button>
                                                    </div>
                                                </td>
                                            </tr>
                                        ))
                                    )}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </section>
            )}

            {/* TAB 4: ĐIỂM DANH */}
            {activeTab === 'diem-danh' && (
                <section className="card">
                    <div className="sub-header">
                        <h2>Điểm danh Buổi học</h2>
                    </div>

                    <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap', marginBottom: 16 }}>
                        <div style={{ minWidth: 220 }}>
                            <span style={{ fontSize: '0.85rem', fontWeight: 600, display: 'block', marginBottom: 4 }}>Chọn Lớp:</span>
                            <select
                                style={{ width: '100%', padding: '8px 12px', borderRadius: 8, border: '1px solid #d1d5db' }}
                                value={selectedLopDiemDanh}
                                onChange={e => setSelectedLopDiemDanh(e.target.value)}
                            >
                                <option value="">-- Chọn lớp học --</option>
                                {lops.map(l => (
                                    <option key={l.id} value={l.id}>{l.tenLop}</option>
                                ))}
                            </select>
                        </div>

                        <div style={{ minWidth: 220 }}>
                            <span style={{ fontSize: '0.85rem', fontWeight: 600, display: 'block', marginBottom: 4 }}>Chọn Buổi Học:</span>
                            <select
                                style={{ width: '100%', padding: '8px 12px', borderRadius: 8, border: '1px solid #d1d5db' }}
                                value={selectedBuoiHocId}
                                disabled={buoiHocDiemDanhList.length === 0}
                                onChange={e => setSelectedBuoiHocId(e.target.value)}
                            >
                                {buoiHocDiemDanhList.map((bh, idx) => (
                                    <option key={bh.id} value={bh.id}>
                                        Buổi {idx + 1}: Ngày {bh.ngayHoc} ({bh.gioBatDau})
                                    </option>
                                ))}
                            </select>
                        </div>
                    </div>

                    {selectedBuoiHocId && (
                        <div>
                            <table className="data-table">
                                <thead>
                                    <tr>
                                        <th>STT</th>
                                        <th>Tên học viên</th>
                                        <th>Trạng thái điểm danh</th>
                                        <th>Ghi chú</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {bangDiemDanh.length === 0 ? (
                                        <tr><td colSpan="4" className="muted" style={{ textAlign: 'center' }}>Lớp này chưa có học viên ghi danh</td></tr>
                                    ) : (
                                        bangDiemDanh.map((row, idx) => (
                                            <tr key={row.hocVienId}>
                                                <td>{idx + 1}</td>
                                                <td><strong>{row.tenHocVien}</strong></td>
                                                <td>
                                                    <label className="status-radio">
                                                        <input
                                                            type="radio"
                                                            name={`status-${row.hocVienId}`}
                                                            checked={row.trangThai === 'CO_MAT' || !row.trangThai}
                                                            onChange={() => updateDiemDanhState(row.hocVienId, 'trangThai', 'CO_MAT')}
                                                        />
                                                        <span className="badge badge-success">Có mặt</span>
                                                    </label>
                                                    <label className="status-radio">
                                                        <input
                                                            type="radio"
                                                            name={`status-${row.hocVienId}`}
                                                            checked={row.trangThai === 'CO_PHEP'}
                                                            onChange={() => updateDiemDanhState(row.hocVienId, 'trangThai', 'CO_PHEP')}
                                                        />
                                                        <span className="badge badge-warning">Có phép</span>
                                                    </label>
                                                    <label className="status-radio">
                                                        <input
                                                            type="radio"
                                                            name={`status-${row.hocVienId}`}
                                                            checked={row.trangThai === 'VANG'}
                                                            onChange={() => updateDiemDanhState(row.hocVienId, 'trangThai', 'VANG')}
                                                        />
                                                        <span className="badge badge-danger">Vắng</span>
                                                    </label>
                                                </td>
                                                <td>
                                                    <input
                                                        type="text"
                                                        value={row.ghiChu || ''}
                                                        placeholder="Lý do..."
                                                        onChange={e => updateDiemDanhState(row.hocVienId, 'ghiChu', e.target.value)}
                                                        style={{ padding: '4px 8px', borderRadius: 4, border: '1px solid #d1d5db', width: '100%' }}
                                                    />
                                                </td>
                                            </tr>
                                        ))
                                    )}
                                </tbody>
                            </table>

                            {bangDiemDanh.length > 0 && (
                                <button
                                    style={{ marginTop: 16, width: 'auto' }}
                                    className="btn-success"
                                    onClick={handleLuuDiemDanh}
                                >
                                    💾 Lưu kết quả điểm danh
                                </button>
                            )}
                        </div>
                    )}
                </section>
            )}

            {/* TAB 5: TÍNH HỌC PHÍ TỰ ĐỘNG */}
            {activeTab === 'hoc-phi' && (
                <section className="card">
                    <div className="sub-header">
                        <h2>Tính học phí tự động & Quản lý Hóa đơn</h2>
                    </div>

                    <form className="form-box" onSubmit={handleTinhHocPhi}>
                        <h3>Tự động sinh Hóa đơn theo lớp & kỳ học</h3>
                        <p className="muted" style={{ marginBottom: 12 }}>
                            Công thức tự động: <code>Số tiền = (Số buổi học của lớp) × (Học phí mỗi buổi)</code>
                        </p>
                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 12 }}>
                            <label className="field">
                                <span>Lớp học</span>
                                <select
                                    value={selectedLopHocPhi}
                                    onChange={e => setSelectedLopHocPhi(e.target.value)}
                                    required
                                >
                                    <option value="">-- Chọn lớp học --</option>
                                    {lops.map(l => (
                                        <option key={l.id} value={l.id}>{l.tenLop} ({Number(l.hocPhiMoiBuoi).toLocaleString()} đ/buổi, {l.soBuoiHoc} buổi)</option>
                                    ))}
                                </select>
                            </label>
                            <label className="field">
                                <span>Kỳ học phí</span>
                                <input value={kyHocPhi} onChange={e => setKyHocPhi(e.target.value)} placeholder="Ví dụ: Tháng 11/2026" required />
                            </label>
                            <label className="field">
                                <span>Hạn đóng học phí</span>
                                <input type="date" value={hanDong} onChange={e => setHanDong(e.target.value)} required />
                            </label>
                        </div>
                        <button type="submit" className="btn-sm btn-success" style={{ width: 'auto', marginTop: 8 }}>
                            ⚡ Kích hoạt tính học phí tự động
                        </button>
                    </form>

                    <h3>Danh sách Hóa đơn học phí ({hoaDons.length})</h3>
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>Học viên</th>
                                <th>Lớp học</th>
                                <th>Kỳ học</th>
                                <th>Số buổi</th>
                                <th>Số tiền</th>
                                <th>Hạn đóng</th>
                                <th>Trạng thái</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            {hoaDons.length === 0 ? (
                                <tr><td colSpan="8" className="muted" style={{ textAlign: 'center' }}>Chưa có hóa đơn nào được phát sinh</td></tr>
                            ) : (
                                hoaDons.map(hd => (
                                    <tr key={hd.id}>
                                        <td><strong>{hd.tenHocVien}</strong></td>
                                        <td>{hd.tenLop}</td>
                                        <td>{hd.kyHocPhi}</td>
                                        <td>{hd.soBuoi} buổi</td>
                                        <td><strong style={{ color: '#4f46e5' }}>{Number(hd.soTien).toLocaleString('vi-VN')} đ</strong></td>
                                        <td>{hd.hanDong}</td>
                                        <td>
                                            {hd.trangThai === 'DA_THANH_TOAN' ? (
                                                <span className="badge badge-success">Đã thanh toán</span>
                                            ) : (
                                                <span className="badge badge-warning">Chưa thanh toán</span>
                                            )}
                                        </td>
                                        <td>
                                            <button
                                                className="btn-sm secondary"
                                                onClick={() => toggleTrangThaiHoaDon(hd)}
                                            >
                                                {hd.trangThai === 'DA_THANH_TOAN' ? 'Hoàn chưa nộp' : 'Đánh dấu đã nộp'}
                                            </button>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </section>
            )}
        </main>
    )
}
