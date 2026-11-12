// Địa chỉ Keycloak mà TRÌNH DUYỆT truy cập được (không phải địa chỉ nội bộ trong Docker).
export const KEYCLOAK_URL = import.meta.env.VITE_KEYCLOAK_URL ?? 'http://localhost:8081'
export const REALM = 'edunest'
export const CLIENT_ID = 'edunest-web'