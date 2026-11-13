import { api } from './http'

// --- Module 1: Học viên & Excel ---
export const previewExcelHocVien = (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return api('/api/hoc-vien/excel/preview', { method: 'POST', body: formData })
}

export const importExcelHocVien = (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return api('/api/hoc-vien/excel/import', { method: 'POST', body: formData })
}

export const layDanhSachHocVien = () => api('/api/hoc-vien')

// --- Module 2: Lớp học & Buổi học ---
export const layDanhSachLop = () => api('/api/lop')

export const taoLop = (payload) => api('/api/lop', { method: 'POST', body: payload })

export const capNhatLop = (id, payload) => api(`/api/lop/${id}`, { method: 'PUT', body: payload })

export const xoaLop = (id) => api(`/api/lop/${id}`, { method: 'DELETE' })

export const sinhBuoiHoc = (lopId, payload) =>
    api(`/api/lop/${lopId}/sinh-buoi-hoc`, { method: 'POST', body: payload })

export const layDanhSachBuoiHoc = (lopId) => api(`/api/lop/${lopId}/buoi-hoc`)

// --- Module 3: Ghi danh ---
export const ghiDanhHocVien = (payload) => api('/api/ghi-danh', { method: 'POST', body: payload })

export const layDanhSachGhiDanh = (lopId) => api(`/api/ghi-danh/lop/${lopId}`)

export const huyGhiDanh = (id) => api(`/api/ghi-danh/${id}`, { method: 'DELETE' })

// --- Module 4: Điểm danh ---
export const layBangDiemDanh = (buoiHocId) => api(`/api/diem-danh/buoi-hoc/${buoiHocId}`)

export const capNhatDiemDanh = (payload) => api('/api/diem-danh', { method: 'POST', body: payload })

// --- Module 5: Tính học phí & Hóa đơn ---
export const tinhHocPhi = (payload) => api('/api/hoa-don/tinh-hoc-phi', { method: 'POST', body: payload })

export const layDanhSachHoaDon = (lopId = '', kyHocPhi = '') => {
    const params = new URLSearchParams()
    if (lopId) params.append('lopId', lopId)
    if (kyHocPhi) params.append('kyHocPhi', kyHocPhi)
    const qs = params.toString()
    return api(`/api/hoa-don${qs ? `?${qs}` : ''}`)
}

export const capNhatTrangThaiHoaDon = (id, trangThai) =>
    api(`/api/hoa-don/${id}/trang-thai`, { method: 'PATCH', body: { trangThai } })

