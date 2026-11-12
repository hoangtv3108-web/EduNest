import { useEffect, useState } from 'react'
import { layTrungTamHienTai } from '../api/tenantApi'
import { useAuth } from '../auth/AuthContext'

const ROLE_LABEL = {
    CENTER_ADMIN: 'Quản trị trung tâm',
    TEACHER: 'Giáo viên',
    PARENT: 'Phụ huynh',
}

export default function DashboardPage() {
    const { user, logout } = useAuth()
    const [tenant, setTenant] = useState(null)
    const [error, setError] = useState('')

    useEffect(() => {
        layTrungTamHienTai()
            .then(setTenant)
            .catch((e) => setError(e.message))
    }, [])

    const roles = user.roles.filter((r) => ROLE_LABEL[r]).map((r) => ROLE_LABEL[r])

    return (
        <main className="app-page">
            <header className="app-header">
                <strong className="brand">EduNest</strong>
                <button className="secondary" onClick={logout}>Đăng xuất</button>
            </header>
            <section className="card">
                <h2>Xin chào, {user.name}</h2>
                <p className="muted">{user.email} · {roles.join(', ') || 'Chưa có vai trò'}</p>
                {error && <p className="form-error">{error}</p>}
                {tenant && (
                    <dl>
                        <dt>Trung tâm</dt>
                        <dd>{tenant.tenTrungTam}</dd>
                        <dt>Trạng thái</dt>
                        <dd>{tenant.trangThai}</dd>
                        <dt>Mã trung tâm (tenant_id)</dt>
                        <dd><code>{tenant.id}</code></dd>
                    </dl>
                )}
            </section>
        </main>
    )
}