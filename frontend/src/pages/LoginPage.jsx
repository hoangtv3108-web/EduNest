import { useState } from 'react'
import { Link, Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import AuthLayout from '../components/AuthLayout'
import Field from '../components/Field'

export default function LoginPage() {
    const { isAuthenticated, login } = useAuth()
    const location = useLocation()
    const redirectTo = location.state?.from ?? '/app'

    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState('')
    const [submitting, setSubmitting] = useState(false)

    // Đăng nhập xong, AuthContext cập nhật và dòng này tự chuyển trang.
    if (isAuthenticated) return <Navigate to={redirectTo} replace />

    async function onSubmit(e) {
        e.preventDefault()
        if (!email.trim() || !password) {
            setError('Vui lòng nhập email và mật khẩu')
            return
        }
        setError('')
        setSubmitting(true)
        try {
            await login(email, password)
        } catch (err) {
            setError(err.message)
            setSubmitting(false)
        }
    }

    return (
        <AuthLayout
            title="Đăng nhập"
            footer={
                <>
                    Chưa có trung tâm? <Link to="/register">Đăng ký trung tâm</Link>
                </>
            }
        >
            {location.state?.message && <p className="notice">{location.state.message}</p>}
            <form onSubmit={onSubmit} noValidate>
                <Field
                    label="Email"
                    type="email"
                    autoComplete="username"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                />
                <Field
                    label="Mật khẩu"
                    type="password"
                    autoComplete="current-password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                />
                {error && <p className="form-error" role="alert">{error}</p>}
                <button type="submit" disabled={submitting}>
                    {submitting ? 'Đang đăng nhập...' : 'Đăng nhập'}
                </button>
            </form>
        </AuthLayout>
    )
}