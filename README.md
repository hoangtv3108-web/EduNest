# EduNest: Nền tảng SaaS quản lý học viên và học phí cho trung tâm nhỏ
**Câu định vị:** Đồ án giải quyết vấn đề điểm danh và nhắc học phí thủ công cho các trung tâm quy mô nhỏ, đo bằng tỉ lệ đóng học phí đúng hạn và thời gian điểm danh, tổng hợp cuối tháng.

![CI](https://github.com/hoangtv3108-web/EduNest/actions/workflows/ci.yml/badge.svg)

## Chạy dự án

### Yêu cầu
- Docker Desktop (có Docker Compose v2) và Git
- Cổng trống: 3000, 3443, 8080, 8081, 5432
- Chỉ khi chạy test ngoài Docker: JDK 21, Maven 3.9+, Node 20

### Chạy nhanh
```bash
git clone https://github.com/hoangtv3108-web/EduNest.git
cd EduNest

# 1. Chứng chỉ HTTPS tự ký cho môi trường dev (Windows: chạy trong Git Bash)
mkdir -p certs
openssl req -x509 -nodes -newkey rsa:2048 -days 365 \
  -keyout certs/edunest.key -out certs/edunest.crt \
  -subj "/CN=localhost" -addext "subjectAltName=DNS:localhost"

# 2. Build và chạy toàn bộ hệ thống
docker compose up -d --build

# 3. Kiểm tra (lần đầu Keycloak mất 1-2 phút khởi động)
docker compose ps
curl http://localhost:8080/api/hello
```

### Địa chỉ
| Thành phần | URL |
|---|---|
| Ứng dụng web | https://localhost:3443 (trình duyệt cảnh báo chứng chỉ tự ký: chọn tiếp tục) |
| API | http://localhost:8080/api/hello |
| Keycloak (quản trị) | http://localhost:8081, tài khoản `admin` / `admin` (chỉ dev) |

Tạo tài khoản dùng thử: mở `/register` để đăng ký trung tâm mới, sau đó đăng nhập.

### Chạy kiểm thử
```bash
docker compose up -d db
cd backend && mvn -B test
```

### Dừng và dọn dẹp
```bash
docker compose down        # dừng, giữ dữ liệu
docker compose down -v     # dừng và xóa cả dữ liệu (cần khi đổi mật khẩu DB)
```

### Lỗi thường gặp
| Triệu chứng | Cách xử lý |
|---|---|
| `port is already allocated` | Tắt chương trình đang dùng cổng đó, hoặc đổi cổng trong `docker-compose.yml` |
| Frontend thoát ngay, log báo thiếu `edunest.crt` | Chưa tạo thư mục `certs` (bước 1) |
| Đăng nhập lỗi ngay sau khi chạy | Keycloak chưa sẵn sàng, đợi 1-2 phút rồi thử lại |
| Đổi mật khẩu DB nhưng không có tác dụng | Script `db/init` chỉ chạy lần đầu, chạy `docker compose down -v` rồi up lại |

### Biến môi trường
Xem bảng biến trong `backend/src/main/resources/application.yml`. Bản triển khai thật phải tạo `.env` từ `.env.example` và dùng `docker-compose.prod.yml`:
```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```

## Bảng đo lường hiệu quả (Trước – Sau)
Đồ án đo lường hiệu quả của hệ thống dựa trên 2 chỉ số cốt lõi tại trung tâm pilot.
| Chỉ số | Cách đo | Trước (Quy trình Excel/Zalo) | Sau (Hệ thống EduNest) | Ngày đo | Bằng chứng |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **1. Tỉ lệ đóng học phí đúng hạn (%)** | Số học viên đóng trước hạn ÷ Tổng số học viên phải đóng (tính theo kỳ học) | *Kế hoạch:* Xin file số liệu học kỳ trước của trung tâm pilot, đếm số người đóng đúng hạn chia cho tổng số học viên để ra tỉ lệ % hiện tại. | | Trước: 20/10/2026<br>Sau: 04/12/2026 | File Excel/Sổ sách thu học phí cũ (đã che tên học viên), lưu tại thư mục doc |
| **2. Thời gian điểm danh và tổng hợp (phút)** | Bấm giờ trực tiếp người phụ trách khi họ làm báo cáo tổng hợp cuối tháng | *Kế hoạch:* Quan sát trực tiếp, dùng đồng hồ bấm giờ quá trình nhân viên lật sổ điểm danh, cộng Excel và rà soát tin nhắn Zalo. | | Trước: 20/10/2026<br>Sau: 04/12/2026 | Nhật ký bấm giờ / Ảnh chụp màn hình quá trình làm việc, lưu tại thư mục doc |
