import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/http'
import { dangKyTrungTam } from '../api/tenantApi'
import { useAuth } from '../auth/AuthContext'
import AuthLayout from '../components/AuthLayout'
import Field from '../components/Field'

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

// Quy tắc giống RegisterTenantRequest ở backend.
function validate(f) {
    const e = {}
    if (!f.tenTrungTam.trim()) e.tenTrungTam = 'Tên trung tâm không được để trống'
    else if (f.tenTrungTam.length > 200) e.tenTrungTam = 'Tên trung tâm tối đa 200 ký tự'
    if (!f.hoTenQuanTri.trim()) e.hoTenQuanTri = 'Họ tên không được để trống'
    if (!f.emailQuanTri.trim()) e.emailQuanTri = 'Email không được để trống'
    else if (!EMAIL_RE.test(f.emailQuanTri.trim())) e.emailQuanTri = 'Email không hợp lệ'
    if (f.matKhau.length < 8 || f.matKhau.length > 128) e.matKhau = 'Mật khẩu từ 8 đến 128 ký tự'
    if (f.nhapLai !== f.matKhau) e.nhapLai = 'Mật khẩu nhập lại không khớp'
    return e
}

function messageForStatus(err) {
    if (!(err instanceof ApiError)) return 'Có lỗi xảy ra, vui lòng thử lại'
    switch (err.status) {
        case 409:
            return 'Email này đã được đăng ký'
        case 400:
            return 'Thông tin chưa hợp lệ, vui lòng kiểm tra lại'
        case 0:
            return 'Không kết nối được máy chủ'
        default:
            return 'Hệ thống tạm thời chưa tạo được tài khoản, vui lòng thử lại sau'
    }
}

export default function RegisterPage() {
    const { isAuthenticated, login } = useAuth()
    const navigate = useNavigate()
    const [form, setForm] = useState({
        tenTrungTam: '',
        hoTenQuanTri: '',
        emailQuanTri: '',
        matKhau: '',
        nhapLai: '',
    })
    const [errors, setErrors] = useState({})
    const [serverError, setServerError] = useState('')
    const [submitting, setSubmitting] = useState(false)

    if (isAuthenticated) return <Navigate to="/app" replace />

    const set = (name) => (e) => setForm((f) => ({ ...f, [name]: e.target.value }))

    async function onSubmit(e) {
        e.preventDefault()
        const found = validate(form)
        setErrors(found)
        setServerError('')
        if (Object.keys(found).length > 0) return

        setSubmitting(true)
        try {
            await dangKyTrungTam({
                tenTrungTam: form.tenTrungTam.trim(),
                emailQuanTri: form.emailQuanTri.trim(),
                hoTenQuanTri: form.hoTenQuanTri.trim(),
                matKhau: form.matKhau,
            })
        } catch (err) {
            setServerError(messageForStatus(err))
            setSubmitting(false)
            return
        }

        // Đăng ký xong thì đăng nhập luôn bằng chính thông tin vừa nhập.
        try {
            await login(form.emailQuanTri, form.matKhau)
        } catch {
            navigate('/login', {
                replace: true,
                state: { message: 'Đăng ký thành công. Vui lòng đăng nhập.' },
            })
        }
    }

    return (
        <AuthLayout
            title="Đăng ký trung tâm"
            subtitle="Tạo trung tâm và tài khoản quản trị đầu tiên."
            footer={
                <>
                    Đã có tài khoản? <Link to="/login">Đăng nhập</Link>
                </>
            }
        >
            <form onSubmit={onSubmit} noValidate>
                <Field label="Tên trung tâm" value={form.tenTrungTam} onChange={set('tenTrungTam')} error={errors.tenTrungTam} />
                <Field label="Họ và tên quản trị" autoComplete="name" value={form.hoTenQuanTri} onChange={set('hoTenQuanTri')} error={errors.hoTenQuanTri} />
                <Field label="Email (dùng để đăng nhập)" type="email" autoComplete="username" value={form.emailQuanTri} onChange={set('emailQuanTri')} error={errors.emailQuanTri} />
                <Field label="Mật khẩu" type="password" autoComplete="new-password" value={form.matKhau} onChange={set('matKhau')} error={errors.matKhau} />
                <Field label="Nhập lại mật khẩu" type="password" autoComplete="new-password" value={form.nhapLai} onChange={set('nhapLai')} error={errors.nhapLai} />
                {serverError && <p className="form-error" role="alert">{serverError}</p>}
                <button type="submit" disabled={submitting}>
                    {submitting ? 'Đang tạo trung tâm...' : 'Đăng ký'}
                </button>
            </form>
        </AuthLayout>
    )
}