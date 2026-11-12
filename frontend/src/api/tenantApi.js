import { api } from './http'

/** payload: { tenTrungTam, emailQuanTri, hoTenQuanTri, matKhau } */
export const dangKyTrungTam = (payload) =>
    api('/api/public/tenants', { method: 'POST', body: payload, auth: false })

export const layTrungTamHienTai = () => api('/api/tenant/current')