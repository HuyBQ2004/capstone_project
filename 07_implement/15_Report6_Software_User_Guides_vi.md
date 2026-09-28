# Capstone Project Report

## Report 6 – Software User Guides

**Project:** SFMS — School Food Management System
**Group:** SEP490_G8

> **Ghi chú cho Nam:** file này viết theo Markdown để bạn đọc/sửa nhanh trong Claude Code, sau đó copy nội dung + dán ảnh vào file Word gốc `Report6_Software User Guides.docx`. Cấu trúc mục lục (I / II.1 / II.2 / II.3.x) giữ **nguyên số thứ tự** như file mẫu Word — không đổi tên mục để khi paste qua không lệch Table of Contents. Mỗi chỗ cần chụp ảnh màn hình được đánh dấu rõ:
>
> 📸 **[ẢNH CẦN CHỤP — mô tả]**
>
> Tất cả route/URL trong tài liệu này lấy từ code thật (`03_Design/08_FrontEnd/Web/src/app/(app)/`) tại thời điểm 2026-08-12 — nếu FE đổi route sau này thì phải cập nhật lại.

---

## I. Record of Changes

| Date | A\*M, D | In charge | Change Description |
|---|---|---|---|
| 2026-08-12 | A | Nam | Khởi tạo Report 6 theo mẫu, dựng khung 13 workflow theo 13 module SFMS |
| | | | |
| | | | |

*A - Added, M - Modified, D - Deleted*

---

## II. Release Package & User Guides

### 1. Deliverable Package

| No. | Deliverable Item | Description |
|---|---|---|
| 1 | Project Schedule/Tracking | `07_implement/00_Team_Work_Assignment_Plan.md` |
| 2 | Project Backlog | `02_Requirements/` (Use Case, Business Rules, Traceability Matrix) |
| 3 | Source Codes | `04_Development/Backend/` (Spring Boot 4.1, Java 25) + `03_Design/08_FrontEnd/Web/` (Next.js 15) |
| 4 | Database Script(s) | Flyway migrations `04_Development/Backend/src/main/resources/db/migration/` |
| 5 | Final Report Document | *(điền theo mẫu chung của môn)* |
| 6 | Test Cases Document | `07_implement/MFxx_*/TestSpec-MFxx_*.md` (13 file, 1/module) + `05_Testing/02_IT/MFxx_*/*.md` |
| 7 | Defects List | `07_implement/14_TC_Completion_Status.md` (mục "Gap/bug thật phát hiện ở code chính" từng module) |
| 8 | Issues List | *(điền nếu có tracker riêng — Jira/Trello/GitLab Issues)* |
| 9 | Slide | *(điền khi có slide thuyết trình)* |

---

### 2. Installation Guides

#### 2.1 System Requirements

| Thành phần | Yêu cầu |
|---|---|
| OS | Windows 10/11, hoặc Linux/macOS bất kỳ hỗ trợ Docker |
| Java | JDK 25 (Backend build bằng Maven, `pom.xml` khai `<java.version>25</java.version>`) |
| Maven | 3.9.x trở lên (`mvn -v`) |
| Node.js | ≥ 20.x (Next.js 15 yêu cầu Node ≥ 18.18, khuyến nghị 20 LTS) |
| Docker Desktop | Bắt buộc — chạy PostgreSQL 15, Mailpit (SMTP giả), và các service phụ trợ qua `docker-compose.yml` |
| PostgreSQL | 15 (chạy qua container `postgres:15-alpine`, không cần cài riêng) |
| Trình duyệt | Chrome/Edge bản mới nhất (để test Frontend) |
| RAM tối thiểu | 8GB (Docker + Java + Node chạy đồng thời) |

📸 **[ẢNH CẦN CHỤP — tuỳ chọn]:** Output của `java -version`, `node -v`, `docker -v` trên máy thật để chứng minh môi trường đã cài đúng.

#### 2.2 Installation Instruction

**Bước 1 — Clone mã nguồn**

```bash
git clone https://gitlab.com/manhnc2/su26_sep490_g8.git
cd su26_sep490_g8
```

**Bước 2 — Khởi động hạ tầng (PostgreSQL + Mailpit) bằng Docker**

```bash
cd 04_Development/Backend
docker compose up -d
```

Kiểm tra 2 container đã chạy: `postgres` (cổng `5432`, DB `sfms`, user/pass `sfms`/`sfms`) và `mailpit` (SMTP giả, UI xem mail tại cổng mặc định Mailpit).

📸 **[ẢNH CẦN CHỤP]:** `docker ps` hiện 2 container `postgres` và `mailpit` đang `Up`.

**Bước 3 — Cấu hình biến môi trường Backend**

Copy file cấu hình mẫu (nếu có `.env.example`) hoặc set trực tiếp các biến chính mà `application.yml` đọc: `DB_URL` (mặc định `jdbc:postgresql://localhost:5432/sfms`), `SERVER_PORT` (mặc định `8080`), `SMTP_PORT` (mặc định `1025`, trỏ vào Mailpit).

**Bước 4 — Build & chạy Backend**

```bash
cd 04_Development/Backend
mvn clean install -DskipTests
mvn spring-boot:run
```

Flyway sẽ tự động chạy migration + seed dữ liệu test (`V900__test_fixtures.sql`) khi Spring Boot khởi động. Backend chạy tại `http://localhost:8080`.

📸 **[ẢNH CẦN CHỤP]:** Console log dòng cuối cùng kiểu `Started SfmsApplication in ... seconds` — chứng minh Backend khởi động thành công, không lỗi.

**Bước 5 — Kiểm tra API bằng Swagger**

Mở trình duyệt: `http://localhost:8080/swagger-ui/index.html`

📸 **[ẢNH CẦN CHỤP]:** Trang Swagger UI hiện đủ danh sách tag của 13 module (MF01–MF13).

**Bước 6 — Cài & chạy Frontend**

```bash
cd 03_Design/08_FrontEnd/Web
npm install
npm run dev
```

Frontend chạy tại `http://localhost:3000`, gọi API qua `http://localhost:8080/api/v1`.

📸 **[ẢNH CẦN CHỤP]:** Terminal hiện `Ready in ... ms` của `next dev`, và trình duyệt mở `http://localhost:3000` load được trang đăng nhập.

**Bước 7 — Build production (tuỳ chọn, nếu báo cáo yêu cầu demo bản build)**

```bash
# Backend
cd 04_Development/Backend && mvn clean package -DskipTests
java -jar target/*.jar

# Frontend
cd 03_Design/08_FrontEnd/Web && npm run build && npm run start
```

---

### 3. User Manual

#### 3.1 Overview

SFMS (School Food Management System) là hệ thống quản lý bán trú/an toàn thực phẩm cho trường tiểu học, hỗ trợ **7 vai trò** người dùng và **13 module nghiệp vụ**:

| # | Module | Vai trò chính sử dụng |
|---|---|---|
| MF01 | Quản lý người dùng & phân quyền | System Admin, mọi vai trò (đăng nhập) |
| MF02 | Quản lý trường, lớp, năm học | System Admin, School Admin |
| MF03 | Quản lý học sinh & phụ huynh | School Admin, Teacher, Parent |
| MF04 | Quản lý nhà cung cấp & đối tác | School Admin, Supplier, Service Caterers |
| MF05 | Quản lý thực đơn & chế độ ăn | Service Caterers, School Admin, Medical Staff |
| MF06 | Quản lý nguyên liệu & kho | Service Caterers, Supplier |
| MF07 | Kiểm thực ba bước | Medical Staff, Service Caterers |
| MF08 | Lưu mẫu thực phẩm | Medical Staff, Service Caterers |
| MF09 | Phân phát & ghi nhận bữa ăn | Teacher, Parent |
| MF10 | Phản hồi & thông báo | Mọi vai trò |
| MF11 | Quản lý sự cố & khẩn cấp | Medical Staff, School Admin |
| MF12 | Dịch vụ hỗ trợ AI | Backend nội bộ (nhận diện khuôn mặt, RAG...) |
| MF13 | Báo cáo & thống kê | School Admin, System Admin, Medical Staff |

**Sơ đồ luồng nghiệp vụ tổng quát (chuỗi an toàn thực phẩm):**

```
Nhà cung cấp giao hàng (MF04/MF06)
      ↓
Lập thực đơn & duyệt (MF05)
      ↓
Kiểm thực ba bước trước khi nấu/chia (MF07)
      ↓
Lưu mẫu thức ăn 24h (MF08)
      ↓
Phân phát bữa ăn cho học sinh (MF09)
      ↓
Ghi nhận tiêu thụ + phản hồi phụ huynh (MF09/MF10)
      ↓
Nếu có sự cố → điều tra bằng dữ liệu đã lưu (MF11)
      ↓
Báo cáo tổng hợp cho BGH/Sở (MF13)
```

📸 **[ẢNH CẦN CHỤP]:** Vẽ lại sơ đồ trên bằng draw.io/Figma cho đẹp, chèn vào đây thay vì dùng khối text.

**Đăng nhập hệ thống (dùng chung cho mọi workflow bên dưới):**

1. Mở `http://localhost:3000/login`.
2. Nhập email + mật khẩu tài khoản tương ứng vai trò cần demo (School Admin/System Admin/Teacher/Parent/Medical Staff/Service Caterers/Supplier).
3. Bấm "Đăng nhập" → hệ thống chuyển hướng tới dashboard riêng của vai trò đó.

📸 **[ẢNH CẦN CHỤP]:** Màn hình `/login` trống, và màn hình dashboard sau khi đăng nhập thành công (chụp riêng cho từng vai trò nếu report cần minh hoạ đủ 7 vai trò).

---

#### 3.2 Workflow 1 — Quản lý người dùng & phân quyền (MF01)

**Mục đích:** System Admin/School Admin tạo tài khoản nhân sự, phân vai trò, quản lý phiên đăng nhập; người dùng tự quản lý hồ sơ và mật khẩu.

**Vai trò:** System Admin, School Admin (tạo/quản lý tài khoản); mọi vai trò (tự phục vụ hồ sơ/mật khẩu).

**Các bước:**

1. **System Admin tạo tài khoản cấp hệ thống:** đăng nhập System Admin → vào `/system-admin/users` → bấm "Thêm..." → chọn vai trò, nếu là vai trò cấp trường thì bắt buộc chọn "Trường liên kết" → điền thông tin → "Tạo tài khoản".
   📸 **[ẢNH]:** Form tạo user ở `/system-admin/users`, và dòng user mới xuất hiện trong bảng với trạng thái "chờ xác minh".
2. **School Admin tạo nhân viên trường:** đăng nhập School Admin → `/school-admin/staff` → "Thêm nhân viên" → chỉ được chọn vai trò cấp trường (Teacher/Medical Staff/Service Caterers).
   📸 **[ẢNH]:** Form `/school-admin/staff`, chú ý dropdown vai trò không có System/School Admin.
3. **Kích hoạt tài khoản:** người dùng mới mở email mời (Mailpit UI khi test local) → vào link `/activate-account?token=...` → đặt mật khẩu → kích hoạt.
   📸 **[ẢNH]:** Hộp thư Mailpit hiện email mời, và màn hình `/activate-account` sau khi kích hoạt thành công.
4. **Đăng nhập / quên mật khẩu:** `/login` → "Quên mật khẩu?" → `/forgot-password` → `/verify-otp` → `/reset-password`.
   📸 **[ẢNH]:** 3 màn hình forgot/verify-otp/reset-password.
5. **Quản lý vai trò tuỳ chỉnh (System Admin):** `/system-admin/roles` → tạo/sửa role, gán quyền chi tiết.
   📸 **[ẢNH]:** Màn hình `/system-admin/roles` với danh sách quyền.
6. **Tự quản lý hồ sơ:** bấm avatar ở topbar (mọi vai trò) → "Hồ sơ cá nhân" / "Đổi mật khẩu".
   📸 **[ẢNH]:** Menu avatar mở ra + dialog đổi mật khẩu.

---

#### 3.3 Workflow 2 — Quản lý trường, lớp & năm học (MF02)

**Mục đích:** System Admin quản lý danh sách trường trong hệ thống; School Admin quản lý lớp học và năm học của trường mình.

**Vai trò:** System Admin (danh sách trường), School Admin (lớp/năm học).

**Các bước:**

1. System Admin → `/system-admin/schools` → xem danh sách trường, bấm vào 1 trường (`/system-admin/schools/[id]`) để xem chi tiết/cấu hình.
   📸 **[ẢNH]:** Danh sách trường + trang chi tiết 1 trường.
2. School Admin → `/school-admin/academic-years` → tạo năm học mới, hoặc `/school-admin/academic-years/promote` để lên lớp hàng loạt cuối năm.
   📸 **[ẢNH]:** Màn hình danh sách năm học + màn hình "lên lớp" (promote).
3. School Admin → `/school-admin/classrooms` → tạo lớp, gán giáo viên chủ nhiệm, xem chi tiết lớp (`/school-admin/classrooms/[id]`).
   📸 **[ẢNH]:** Danh sách lớp + trang chi tiết 1 lớp (sĩ số, học sinh trong lớp).
4. School Admin → `/school-admin/config` → cấu hình chung của trường (email nhận báo cáo, ngưỡng cảnh báo...).
   📸 **[ẢNH]:** Trang cấu hình trường.

---

#### 3.4 Workflow 3 — Quản lý học sinh & phụ huynh (MF03)

**Mục đích:** School Admin quản lý hồ sơ học sinh, khai báo dị ứng/bệnh mãn tính; phụ huynh xem thông tin con em.

**Vai trò:** School Admin, Teacher (xem), Parent (khai báo/xem con mình).

**Các bước:**

1. School Admin → `/school-admin/students` → "Thêm học sinh" (hoặc `/school-admin/students/import` để nhập hàng loạt bằng file Excel) → điền hồ sơ.
   📸 **[ẢNH]:** Danh sách học sinh + form thêm học sinh + màn hình import Excel.
2. Xem chi tiết 1 học sinh: `/school-admin/students/[id]` — hồ sơ, lớp, phụ huynh liên kết, dị ứng.
   📸 **[ẢNH]:** Trang chi tiết học sinh.
3. Quản lý phụ huynh: `/school-admin/parents` → xem/liên kết phụ huynh với học sinh (`/school-admin/parents/[id]`).
   📸 **[ẢNH]:** Danh sách phụ huynh + trang chi tiết liên kết con em.
4. Duyệt khai báo dị ứng: `/school-admin/allergy-review` — School Admin/Medical Staff xác nhận thông tin dị ứng phụ huynh khai.
   📸 **[ẢNH]:** Màn hình duyệt dị ứng.
5. Phụ huynh khai báo dị ứng/bệnh mãn tính: đăng nhập Parent → `/parent/allergies`, `/parent/chronic-conditions`.
   📸 **[ẢNH]:** 2 màn hình khai báo phía Parent.

---

#### 3.5 Workflow 4 — Quản lý nhà cung cấp & đối tác (MF04)

**Mục đích:** School Admin ký kết đối tác với nhà cung cấp/đơn vị suất ăn; Supplier tự quản lý hồ sơ, sản phẩm, chứng nhận.

**Vai trò:** School Admin, Supplier, Service Caterers.

**Các bước:**

1. School Admin → `/school-admin/partnerships` → xem danh sách đối tác, chi tiết 1 đối tác (`/school-admin/partnerships/[id]`).
   📸 **[ẢNH]:** Danh sách đối tác + trang chi tiết.
2. Supplier đăng nhập → `/supplier/partnerships` → xem lời mời hợp tác từ trường, chấp nhận.
   📸 **[ẢNH]:** Màn hình lời mời hợp tác phía Supplier.
3. Supplier quản lý sản phẩm: `/supplier/products` — thêm/sửa danh mục sản phẩm cung cấp.
   📸 **[ẢNH]:** Danh sách sản phẩm Supplier.
4. Supplier quản lý chứng nhận an toàn thực phẩm: `/supplier/certificates` — tải lên, theo dõi hạn.
   📸 **[ẢNH]:** Màn hình chứng nhận, chú ý cảnh báo khi gần hết hạn.

---

#### 3.6 Workflow 5 — Quản lý thực đơn & chế độ ăn (MF05)

**Mục đích:** Service Caterers lập thực đơn tuần, School Admin/Medical Staff duyệt; hệ thống hỗ trợ suất ăn đặc biệt theo dị ứng.

**Vai trò:** Service Caterers (lập), School Admin (duyệt), Medical Staff (rà dinh dưỡng), Parent (xem/đăng ký suất đặc biệt).

**Các bước:**

1. Service Caterers → `/service-caterers/menus` → "Tạo thực đơn" (`/service-caterers/menus/create`) → chọn món cho từng ngày/bữa trong tuần.
   📸 **[ẢNH]:** Form tạo thực đơn theo lưới ngày × bữa.
2. Có thể dùng mẫu có sẵn: `/service-caterers/menu-templates`.
   📸 **[ẢNH]:** Danh sách mẫu thực đơn.
3. Quản lý danh mục món ăn: `/service-caterers/dishes`.
   📸 **[ẢNH]:** Danh sách món ăn + thông tin dinh dưỡng từng món.
4. School Admin duyệt thực đơn: `/school-admin/menu-approval` → chọn 1 thực đơn chờ duyệt (`/school-admin/menu-approval/[id]`) → Duyệt/Từ chối.
   📸 **[ẢNH]:** Màn hình danh sách chờ duyệt + trang chi tiết duyệt.
5. Phụ huynh xem thực đơn tuần: Parent → `/parent/weekly-menu`.
   📸 **[ẢNH]:** Màn hình thực đơn tuần phía Parent.
6. Phụ huynh đăng ký suất ăn đặc biệt (dị ứng/tôn giáo...): `/parent/special-meal-requests` → School Admin duyệt tại `/school-admin/special-meals`.
   📸 **[ẢNH]:** Form đăng ký của Parent + màn duyệt của School Admin.

---

#### 3.7 Workflow 6 — Quản lý nguyên liệu & kho (MF06)

**Mục đích:** Supplier giao hàng, Service Caterers nhận hàng/kiểm tra chứng từ và quản lý tồn kho.

**Vai trò:** Supplier (giao), Service Caterers (nhận, xuất kho).

**Các bước:**

1. Supplier tạo lô hàng giao: `/supplier/deliveries` → xem chi tiết (`/supplier/deliveries/[id]`); quản lý lô sản phẩm tại `/supplier/batches`.
   📸 **[ẢNH]:** Danh sách giao hàng phía Supplier.
2. Service Caterers nhận hàng: `/service-caterers/receiving` → "Nhận hàng mới" (`/service-caterers/receiving/new`) → đối chiếu chứng từ, kiểm tra chất lượng, xác nhận nhập kho.
   📸 **[ẢNH]:** Form nhận hàng — có phần chụp ảnh chứng từ/hàng hóa.
3. Xuất kho cho bếp: `/service-caterers/inventory/issue`.
   📸 **[ẢNH]:** Form xuất kho theo thực đơn trong ngày.
4. Xem lịch sử giao dịch kho: `/service-caterers/inventory-transactions`, tồn kho hiện tại: `/service-caterers/inventory`.
   📸 **[ẢNH]:** Bảng tồn kho + lịch sử nhập/xuất.

---

#### 3.8 Workflow 7 — Kiểm thực ba bước (MF07)

**Mục đích:** Bắt buộc kiểm tra 3 bước (nguyên liệu → chế biến → trước khi ăn) trước khi cho phép phát suất ăn (cổng BR-094).

**Vai trò:** Medical Staff (thực hiện kiểm tra), Service Caterers (xem kết quả).

**Các bước:**

1. Medical Staff → `/medical-staff/conduct-inspection` → chọn bữa cần kiểm tra → làm theo checklist 3 bước (`/medical-staff/conduct-inspection/checklist`).
   📸 **[ẢNH]:** Checklist 3 bước, mỗi bước có ô nhập kết quả PASS/FAIL + ảnh minh chứng.
2. Ghi nhận kết quả chi tiết: `/medical-staff/conduct-inspection/[id]/record`.
   📸 **[ẢNH]:** Màn hình ghi kết quả từng bước.
3. Xem lịch sử kiểm tra: `/medical-staff/conduct-inspection/history`, hoặc danh sách tổng hợp `/medical-staff/inspections` → chi tiết `/medical-staff/inspections/[id]`.
   📸 **[ẢNH]:** Lịch sử kiểm tra + trang chi tiết 1 phiên.
4. Service Caterers xem kết quả kiểm tra ảnh hưởng tới bếp: `/service-caterers/inspections` → chi tiết + in phiếu (`/service-caterers/inspections/[id]/print`).
   📸 **[ẢNH]:** Phiếu kiểm thực dạng in.
5. **Minh hoạ cổng chặn:** thử phát suất ăn khi kiểm thực chưa PASSED → hệ thống chặn (xem Workflow 9, bước phát suất).
   📸 **[ẢNH]:** Thông báo lỗi chặn phát suất khi kiểm thực chưa đạt.

---

#### 3.9 Workflow 8 — Lưu mẫu thực phẩm (MF08)

**Mục đích:** Lưu mẫu thức ăn tối thiểu 24h theo quy định, phục vụ truy vết khi có sự cố ngộ độc.

**Vai trò:** Medical Staff / Service Caterers.

**Các bước:**

1. Đăng nhập Medical Staff → `/medical-staff/samples` → "Lưu mẫu mới" → chọn món, khối lượng, giờ lấy mẫu, hạn lưu.
   📸 **[ẢNH]:** Form lưu mẫu.
2. Service Caterers thao tác tương tự tại `/service-caterers/samples` → xem chi tiết/in nhãn (`/service-caterers/samples/[id]`, `/service-caterers/samples/[id]/print`, hoặc in hàng loạt `/service-caterers/samples/print`).
   📸 **[ẢNH]:** Nhãn mẫu in ra (mã mẫu, món, giờ lấy, hạn huỷ).
3. Huỷ mẫu sau khi hết hạn lưu (thao tác trong cùng màn hình chi tiết mẫu).
   📸 **[ẢNH]:** Trạng thái mẫu chuyển "Đã huỷ" kèm thời điểm huỷ.

---

#### 3.10 Workflow 9 — Phân phát & ghi nhận bữa ăn (MF09)

**Mục đích:** Giáo viên xác nhận phát suất ăn cho từng lớp, ghi nhận mức tiêu thụ; phụ huynh xem lại lịch sử ăn của con.

**Vai trò:** Teacher, Parent.

**Các bước:**

1. Teacher đăng nhập → `/teacher/distribution` → xem danh sách học sinh cần phát suất trong lớp hôm nay.
   📸 **[ẢNH]:** Danh sách phát suất theo lớp, trạng thái PENDING.
2. Bấm "Xác nhận phát" cho cả lớp/từng học sinh → xem chi tiết 1 lượt phát (`/teacher/distribution/[id]`).
   📸 **[ẢNH]:** Thao tác xác nhận phát + trạng thái chuyển SERVED. Nếu kiểm thực (Workflow 7) chưa đạt, chụp thêm màn hình bị chặn để minh hoạ cổng BR-094.
3. Ghi nhận mức tiêu thụ sau bữa ăn: `/teacher/meal-records` → chọn FULLY/PARTIALLY/NOT_CONSUMED cho từng học sinh.
   📸 **[ẢNH]:** Form ghi nhận tiêu thụ.
4. Parent xem lịch sử ăn của con: `/parent/meal-history` → chi tiết 1 ngày (`/parent/meal-history/[id]`); xem thông tin dinh dưỡng món: `/parent/nutrition-history`, `/parent/nutrition/[dishId]`.
   📸 **[ẢNH]:** Màn hình lịch sử ăn phía Parent (tên món, mức tiêu thụ, cảnh báo dị ứng nếu có).

---

#### 3.11 Workflow 10 — Phản hồi & thông báo (MF10)

**Mục đích:** Nhà trường gửi thông báo tới phụ huynh/nhân viên; phụ huynh gửi phản hồi/góp ý về bữa ăn.

**Vai trò:** School Admin (gửi thông báo), Parent (phản hồi + nhận thông báo).

**Các bước:**

1. School Admin → `/school-admin/announcements` → "Tạo thông báo" (`/school-admin/announcements/create`) → chọn đối tượng nhận, nội dung.
   📸 **[ẢNH]:** Form tạo thông báo + danh sách đã gửi.
2. Các vai trò khác xem thông báo của mình tại `/<role>/announcements` (VD `/parent/announcements`, `/teacher/announcements`...) → chi tiết `[id]`.
   📸 **[ẢNH]:** Danh sách thông báo phía người nhận (chụp 1 vai trò đại diện, VD Parent).
3. Parent gửi phản hồi về bữa ăn: `/parent/feedback/meal`, hoặc phản hồi chất lượng chung: `/parent/feedback/quality`; xem lại các phản hồi đã gửi tại `/parent/feedback`.
   📸 **[ẢNH]:** Form gửi phản hồi + rating/sao (nếu có).
4. School Admin xem tổng hợp phản hồi: `/school-admin/feedback`.
   📸 **[ẢNH]:** Bảng tổng hợp phản hồi phía trường.
5. Parent báo mối lo an toàn thực phẩm (kênh riêng, khẩn cấp hơn feedback thường): `/parent/safety-concern`.
   📸 **[ẢNH]:** Form báo mối lo an toàn.

---

#### 3.12 Workflow 11 — Quản lý sự cố & khẩn cấp (MF11)

**Mục đích:** Ghi nhận, điều tra và xử lý sự cố an toàn thực phẩm (ngộ độc, dị vật...) truy vết ngược về nguyên liệu/món ăn qua dữ liệu MF06-MF09.

**Vai trò:** Medical Staff (điều tra), School Admin (theo dõi/đóng sự cố), Teacher (báo cáo ban đầu).

**Các bước:**

1. Ghi nhận sự cố ban đầu: Teacher → `/teacher/incidents` (báo nhanh khi phát hiện học sinh có triệu chứng bất thường).
   📸 **[ẢNH]:** Form báo sự cố nhanh phía Teacher.
2. Medical Staff xử lý toàn diện: `/medical-staff/incidents` → chi tiết (`/medical-staff/incidents/[id]`) → thêm minh chứng (`/medical-staff/incidents/[id]/evidence`) → mở điều tra (`/medical-staff/incidents/[id]/investigation`).
   📸 **[ẢNH]:** Trang điều tra — chú ý phần truy vết theo `dish_id` liên kết ngược MF09 (học sinh nào đã ăn món nghi vấn).
3. Ghi hồ sơ y tế liên quan: `/medical-staff/incidents/records` → chi tiết `[id]`.
   📸 **[ẢNH]:** Hồ sơ y tế của học sinh liên quan sự cố.
4. Đưa ra hành động khắc phục (CAPA): `/medical-staff/corrective-actions` → chi tiết `[id]`; School Admin xem CAPA tại `/school-admin/corrective-actions`.
   📸 **[ẢNH]:** Danh sách CAPA + trạng thái xử lý.
5. Ghi nhận vi phạm (nếu có bên thứ 3 sai phạm): `/medical-staff/violations` → tạo mới (`/medical-staff/violations/new`) → chi tiết (`/medical-staff/violations/[id]`); School Admin xem/đóng (`/school-admin/violations`, `/school-admin/violations/[id]`).
   📸 **[ẢNH]:** Form tạo vi phạm + trang chi tiết.
6. School Admin đóng sự cố sau khi xử lý xong: `/school-admin/incidents/[id]/close`.
   📸 **[ẢNH]:** Màn hình đóng sự cố (yêu cầu điền kết luận cuối).

---

#### 3.13 Workflow 12 — Dịch vụ hỗ trợ AI (MF12)

**Mục đích:** Các dịch vụ AI chạy nền hỗ trợ hệ thống — không có UI riêng cho người dùng cuối, minh hoạ qua kết quả xuất hiện ở module khác.

**Vai trò:** Nội bộ hệ thống (Backend service, không có UI trực tiếp).

**Các bước (minh hoạ gián tiếp qua nơi kết quả AI hiển thị):**

1. Nhận diện khuôn mặt hỗ trợ điểm danh/xác thực (nếu bật) — kết quả thể hiện ở luồng phát suất ăn (Workflow 9) hoặc chấm công nhân viên.
   📸 **[ẢNH]:** Nếu có màn hình nào hiển thị kết quả nhận diện, chụp tại đó; nếu chỉ chạy nội bộ qua API thì chụp response Swagger của endpoint AI liên quan thay thế.
2. RAG hỗ trợ tra cứu (nếu có chatbot/tìm kiếm thông minh trong hệ thống) — chụp màn hình tính năng đó nếu tồn tại trên FE.
   📸 **[ẢNH]:** Giao diện chatbot/tra cứu AI nếu có; nếu module này thuần backend, ghi rõ trong báo cáo là "dịch vụ nền, không có màn hình riêng" thay vì chụp ảnh giả.

> ⚠️ Nam kiểm tra lại phạm vi thật của MF12 trước khi viết mục này — cần đối chiếu với TDS-MF12 để biết chính xác tính năng nào đã lên UI, tránh ghi khống.

---

#### 3.14 Workflow 13 — Báo cáo & thống kê (MF13)

**Mục đích:** Xem dashboard tổng hợp tuân thủ, xuất báo cáo PDF/XLSX, lên lịch gửi báo cáo định kỳ tự động.

**Vai trò:** School Admin, System Admin, Medical Staff (chỉ xem).

**Các bước:**

1. Đăng nhập School Admin/System Admin → `/school-admin/reports` (hoặc tương đương phía System Admin) → xem dashboard tổng hợp 5 nhóm tuân thủ (lưu mẫu, kiểm thực, nhận hàng, chứng nhận NCC, vệ sinh bếp).
   📸 **[ẢNH]:** Dashboard tổng hợp với biểu đồ tỉ lệ tuân thủ từng nhóm.
2. Xem báo cáo chi tiết 1 loại: `/school-admin/reports/[slug]` (VD `sample-retention`, `three-step`, `receiving`...).
   📸 **[ẢNH]:** Bảng chi tiết dữ liệu 1 loại báo cáo.
3. Xuất file báo cáo: bấm "Xuất báo cáo" trên trang chi tiết → chọn định dạng PDF/XLSX → tải về.
   📸 **[ẢNH]:** Dialog chọn định dạng xuất + file PDF/XLSX đã tải về mở ra xem (chú ý nhãn "Confidential" trên file).
4. Xem tuân thủ toàn trường (Medical Staff, School Admin): `/medical-staff/compliance`, `/medical-staff/compliance/report`, `/medical-staff/compliance/history`, hoặc report-center (`/medical-staff/report-center`, `/medical-staff/report-center/compliance`, `/medical-staff/report-center/history`).
   📸 **[ẢNH]:** 1-2 màn hình đại diện phía Medical Staff.
5. System Admin xem thống kê sử dụng hệ thống toàn trường: `/system-admin/usage-statistics`.
   📸 **[ẢNH]:** Màn hình thống kê sử dụng.
6. (Nếu FE đã nối) Lên lịch gửi báo cáo tự động định kỳ (daily/weekly/monthly) — kiểm tra trong phần cấu hình báo cáo của School Admin.
   📸 **[ẢNH]:** Form tạo lịch gửi báo cáo, nếu màn hình này đã có trên FE.

---

## Ghi chú tổng cho Nam trước khi chuyển sang Word

1. **Ảnh chụp màn hình:** nên chạy đủ Backend + Frontend + Docker theo đúng mục "2.2 Installation Instruction", đăng nhập lần lượt từng vai trò (dùng tài khoản seed có sẵn trong `V900__test_fixtures.sql`, VD `U-0001` School Admin) để chụp cho khớp dữ liệu thật thay vì dùng ảnh dàn dựng.
2. **Mục 3.13 (MF12)** cần xác nhận lại phạm vi UI thật trước khi hoàn thiện — đã ghi chú cảnh báo ngay trong mục đó.
3. Có thể **gộp bớt** một vài workflow nếu báo cáo giới hạn số trang (VD gộp MF07+MF08 thành 1 "chuỗi kiểm thực – lưu mẫu", gộp MF10 vào cuối các workflow khác) — cấu trúc 13 workflow ở trên là đầy đủ nhất theo 13 module, có thể cắt bớt tuỳ yêu cầu thực tế của thầy cô.
4. Toàn bộ route trong tài liệu đã đối chiếu với thư mục `app/(app)/` thật của Frontend — nếu chụp ảnh mà route không khớp (đổi tên, xoá trang), báo lại để cập nhật file này.
