export default function AuthLayout({ title, subtitle, children, footer }) {
    return (
        <main className="auth-page">
            <section className="auth-card">
                <h1 className="brand">EduNest</h1>
                <h2>{title}</h2>
                {subtitle && <p className="muted">{subtitle}</p>}
                {children}
                {footer && <p className="auth-footer">{footer}</p>}
            </section>
        </main>
    )
}