
\set ON_ERROR_STOP off
\pset pager off
\set a '11111111-1111-1111-1111-111111111111'
\set b '22222222-2222-2222-2222-222222222222'

\echo 
SELECT count(*) AS so_hang FROM tenant;

\echo 
BEGIN; SELECT set_config('app.tenant_id', :'a', true);
INSERT INTO tenant(id, ten_trung_tam) VALUES (:'a', 'Trung tâm A (demo)');
INSERT INTO tai_khoan(tenant_id, keycloak_sub, email, ho_ten, vai_tro)
  VALUES (:'a', gen_random_uuid(), 'a@demo.vn', 'Admin A', 'CENTER_ADMIN');
COMMIT;
BEGIN; SELECT set_config('app.tenant_id', :'b', true);
INSERT INTO tenant(id, ten_trung_tam) VALUES (:'b', 'Trung tâm B (demo)');
INSERT INTO tai_khoan(tenant_id, keycloak_sub, email, ho_ten, vai_tro)
  VALUES (:'b', gen_random_uuid(), 'b@demo.vn', 'Admin B', 'CENTER_ADMIN');
COMMIT;

\echo
BEGIN; SELECT set_config('app.tenant_id', :'a', true);
SELECT ten_trung_tam FROM tenant;
SELECT email FROM tai_khoan;
COMMIT;

\echo 
BEGIN; SELECT set_config('app.tenant_id', :'a', true);
INSERT INTO tai_khoan(tenant_id, keycloak_sub, email, ho_ten, vai_tro)
  VALUES (:'b', gen_random_uuid(), 'hack@demo.vn', 'Hacker', 'CENTER_ADMIN');
ROLLBACK;

\echo 
BEGIN; SELECT set_config('app.tenant_id', :'a', true);
UPDATE tai_khoan SET ho_ten = 'bi sua' WHERE email = 'b@demo.vn';
DELETE FROM tai_khoan WHERE email = 'b@demo.vn';
ROLLBACK;

\echo '--- 6. Sau khi kết thúc transaction, tenant không còn bị giữ lại (kỳ vọng 0)'
SELECT count(*) AS so_hang FROM tenant;

\echo '--- 7. Dọn dữ liệu demo'
BEGIN; SELECT set_config('app.tenant_id', :'a', true);
DELETE FROM tai_khoan; DELETE FROM tenant; COMMIT;
BEGIN; SELECT set_config('app.tenant_id', :'b', true);
DELETE FROM tai_khoan; DELETE FROM tenant; COMMIT;
