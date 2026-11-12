import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { parseJwt } from './jwt'
import { getTokens, loginWithPassword, logout as doLogout, subscribe } from './tokenStore'

const AuthContext = createContext(null)

function userFromTokens(tokens) {
    if (!tokens) return null
    try {
        const c = parseJwt(tokens.accessToken)
        return {
            email: c.email ?? c.preferred_username,
            name: c.name ?? c.given_name ?? c.email,
            tenantId: c.tenant_id ?? null,
            roles: c.realm_access?.roles ?? [],
        }
    } catch {
        return null
    }
}

export function AuthProvider({ children }) {
    const [user, setUser] = useState(() => userFromTokens(getTokens()))

    // Token bị xoá (hết hạn, đăng xuất ở tab khác...) thì cập nhật giao diện.
    useEffect(() => subscribe(() => setUser(userFromTokens(getTokens()))), [])

    const login = useCallback((email, password) => loginWithPassword(email, password), [])
    const logout = useCallback(() => doLogout(), [])

    const value = useMemo(
        () => ({ user, isAuthenticated: user !== null, login, logout }),
        [user, login, logout],
    )
    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
    const ctx = useContext(AuthContext)
    if (!ctx) throw new Error('useAuth phải nằm trong AuthProvider')
    return ctx
}