-- ===== 1) Tạo TẤT CẢ role trước =====
CREATE ROLE keycloak      LOGIN PASSWORD 'keycloak';
CREATE ROLE edunest_owner LOGIN PASSWORD 'edunest_owner';
CREATE ROLE edunest_app   LOGIN PASSWORD 'edunest_app'
  NOSUPERUSER NOBYPASSRLS NOCREATEDB NOCREATEROLE;

-- ===== 2) Database =====
CREATE DATABASE keycloak OWNER keycloak;

-- "edunest" đã được Docker tạo sẵn (POSTGRES_DB), chỉ đổi chủ và cấp quyền kết nối
ALTER DATABASE edunest OWNER TO edunest_owner;
GRANT CONNECT ON DATABASE edunest TO edunest_app;

-- ===== 3) Quyền mặc định trong database edunest =====
\connect edunest

ALTER DEFAULT PRIVILEGES FOR ROLE edunest_owner IN SCHEMA public
  GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO edunest_app;