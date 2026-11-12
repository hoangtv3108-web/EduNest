/** Đọc phần payload của JWT (chỉ để hiển thị; việc kiểm tra chữ ký do backend làm). */
export function parseJwt(token) {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    const json = decodeURIComponent(
        atob(base64)
            .split('')
            .map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
            .join(''),
    )
    return JSON.parse(json)
}