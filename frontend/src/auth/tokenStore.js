import { CLIENT_ID, KEYCLOAK_URL, REALM } from '../config'

const STORAGE_KEY = 'edunest.tokens'
const BASE = `${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect`
const listeners = new Set()

let tokens = readStorage()
let refreshing = null

export class AuthError extends Error {
    constructor(code, message) {
        super(message)
        this.code = code
    }
}

function readStorage() {
    try {
        return JSON.parse(sessionStorage.getItem(STORAGE_KEY))
    } catch {
        return null
    }
}

function setTokens(next) {
    tokens = next
    if (next) sessionStorage.setItem(STORAGE_KEY, JSON.stringify(next))
    else sessionStorage.removeItem(STORAGE_KEY)
    listeners.forEach((fn) => fn())
}

export function getTokens() {
    return tokens
}

export function subscribe(fn) {
    listeners.add(fn)
    return () => listeners.delete(fn)
}

async function requestToken(params) {
    let res
    try {
        res = await fetch(`${BASE}/token`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({ client_id: CLIENT_ID, ...params }),
        })
    } catch {
        throw new AuthError('network', 'Không kết nối được máy chủ đăng nhập')
    }
    const data = await res.json().catch(() => ({}))
    if (!res.ok) {
        if (data.error === 'invalid_grant') {
            throw new AuthError('invalid_credentials', 'Email hoặc mật khẩu không đúng')
        }
        throw new AuthError('server', 'Không đăng nhập được, vui lòng thử lại sau')
    }
    const now = Date.now()
    return {
        accessToken: data.access_token,
        refreshToken: data.refresh_token,
        accessExp: now + data.expires_in * 1000,
        refreshExp: now + data.refresh_expires_in * 1000,
    }
}

export async function loginWithPassword(email, password) {
    setTokens(
        await requestToken({
            grant_type: 'password',
            username: email.trim().toLowerCase(),
            password,
        }),
    )
}

export async function logout() {
    const refreshToken = tokens?.refreshToken
    setTokens(null)
    if (!refreshToken) return
    try {
        await fetch(`${BASE}/logout`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({ client_id: CLIENT_ID, refresh_token: refreshToken }),
        })
    } catch {
        // Không gọi được Keycloak thì phiên phía trình duyệt vẫn đã bị xoá.
    }
}

/** Trả access token còn hạn; tự làm mới nếu sắp hết hạn; trả null nếu phải đăng nhập lại. */
export async function getAccessToken() {
    if (!tokens) return null
    if (tokens.accessExp - Date.now() > 30_000) return tokens.accessToken
    if (tokens.refreshExp - Date.now() < 5_000) {
        setTokens(null)
        return null
    }
    refreshing ??= requestToken({ grant_type: 'refresh_token', refresh_token: tokens.refreshToken })
        .then((next) => {
            setTokens(next)
            return next.accessToken
        })
        .catch(() => {
            setTokens(null)
            return null
        })
        .finally(() => {
            refreshing = null
        })
    return refreshing
}