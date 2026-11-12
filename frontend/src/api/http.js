import { getAccessToken } from '../auth/tokenStore'

export class ApiError extends Error {
    constructor(status, message, data = null) {
        super(message)
        this.status = status // 0 = không kết nối được
        this.data = data
    }
}

export async function api(path, { method = 'GET', body, auth = true } = {}) {
    const headers = { Accept: 'application/json' }
    if (body !== undefined) headers['Content-Type'] = 'application/json'
    if (auth) {
        const token = await getAccessToken()
        if (!token) throw new ApiError(401, 'Phiên đăng nhập đã hết hạn')
        headers.Authorization = `Bearer ${token}`
    }

    let res
    try {
        res = await fetch(path, {
            method,
            headers,
            body: body === undefined ? undefined : JSON.stringify(body),
        })
    } catch {
        throw new ApiError(0, 'Không kết nối được máy chủ')
    }

    if (!res.ok) {
        const data = await res.json().catch(() => null)
        throw new ApiError(res.status, data?.message ?? res.statusText, data)
    }
    return res.status === 204 ? null : res.json()
}