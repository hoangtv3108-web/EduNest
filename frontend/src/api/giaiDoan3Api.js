import { api, ApiError } from './http'
import { getAccessToken } from '../auth/tokenStore'

// --- Báo cáo cuối tháng (CENTER_ADMIN) ---
export const layBaoCaoDoanhThu = (thang) => api(`/api/bao-cao/doanh-thu?thang=${encodeURIComponent(thang)}`)

export const layBaoCaoChuyenCan = (thang) => api(`/api/bao-cao/chuyen-can?thang=${encodeURIComponent(thang)}`)

// --- Nhật ký kiểm toán (CENTER_ADMIN) ---
export const layAuditLog = ({ hanhDong = '', tuNgay = '', denNgay = '', page = 0, size = 10 } = {}) => {
    const params = new URLSearchParams()
    if (hanhDong) params.append('hanhDong', hanhDong)
    if (tuNgay) params.append('tuNgay', tuNgay)
    if (denNgay) params.append('denNgay', denNgay)
    params.append('page', page)
    params.append('size', size)
    return api(`/api/audit-log?${params.toString()}`)
}

// --- Nhắc học phí tự động (CENTER_ADMIN) ---
export const chayThuNhacNo = () => api('/api/nhac-no/chay-thu', { method: 'POST' })

export const layLichSuNhacNo = (gioiHan = 20) => api(`/api/nhac-no/lich-su?gioiHan=${gioiHan}`)

// --- Tải file (Excel / CSV / JSON): fetch có Bearer token rồi lưu bằng Blob ---
export async function taiFile(path) {
    const token = await getAccessToken()
    if (!token) throw new ApiError(401, 'Phiên đăng nhập đã hết hạn')

    let res
    try {
        res = await fetch(path, { headers: { Authorization: `Bearer ${token}` } })
    } catch {
        throw new ApiError(0, 'Không kết nối được máy chủ')
    }
    if (!res.ok) {
        const data = await res.json().catch(() => null)
        throw new ApiError(res.status, data?.message ?? res.statusText, data)
    }

    const disposition = res.headers.get('Content-Disposition') ?? ''
    const khop = /filename\*?=(?:UTF-8'')?"?([^";]+)"?/i.exec(disposition)
    const tenFile = khop ? decodeURIComponent(khop[1]) : 'export'

    const url = URL.createObjectURL(await res.blob())
    const a = document.createElement('a')
    a.href = url
    a.download = tenFile
    document.body.appendChild(a)
    a.click()
    a.remove()
    URL.revokeObjectURL(url)

    return { tenFile, sha256: res.headers.get('X-SHA256') }
}

export const xuatExcelDoanhThu = (thang) =>
    taiFile(`/api/bao-cao/doanh-thu/xuat-excel?thang=${encodeURIComponent(thang)}`)

export const xuatAuditLog = ({ dinhDang = 'csv', bom = false, hanhDong = '', tuNgay = '', denNgay = '' } = {}) => {
    const params = new URLSearchParams()
    params.append('dinhDang', dinhDang)
    if (bom) params.append('bom', 'true')
    if (hanhDong) params.append('hanhDong', hanhDong)
    if (tuNgay) params.append('tuNgay', tuNgay)
    if (denNgay) params.append('denNgay', denNgay)
    return taiFile(`/api/audit-log/xuat?${params.toString()}`)
}