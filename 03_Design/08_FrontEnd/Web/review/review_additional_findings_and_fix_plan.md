# Rà soát bổ sung — lỗi mới phát hiện & kế hoạch chỉnh sửa

> **Phạm vi:** đọc code thật `03_Design/08_FrontEnd/Web/src` (không chỉ mock-data), chạy `tsc --noEmit` + `next lint`, đối chiếu với 3 báo cáo có sẵn trong thư mục `review/`.
> **Ngày:** 14/07/2026 · **Người rà:** Nam (School Admin / System Admin).
> **Mục tiêu:** tìm **các lỗi CHƯA có trong 3 báo cáo cũ** — tập trung lỗi chạy thật (runtime/logic), lỗi nguồn dữ liệu, mâu thuẫn ngưỡng nghiệp vụ giữa các màn, và tooling — rồi lên kế hoạch sửa theo giai đoạn.
>
> Ba báo cáo cũ đối chiếu: `review_data_linkage_and_config_audit.md` (data linkage), `review_screen_schooladmin_and_systemadmin.md`, `review_screen_parent_and_teacher.md`.

---

## 0. Bối cảnh: 3 báo cáo cũ đã lỗi thời một phần

Kiểm chứng lại code hiện tại: **nhiều màn 3 báo cáo đánh dấu ❌ "Không có route" NAY ĐÃ tồn tại.** Ví dụ đã dựng xong:

- `parent/safety-concern`, `parent/special-meals`, `parent/special-meal-records`, `parent/health`, `parent/nutrition-history`, `parent/nutrition/[dishId]`, `parent/meal-history/[id]`, `parent/school-meals`.
- `teacher/students/[id]`, `teacher/distribution/[id]`, `teacher/meal-records`, `teacher/special-meals`.
- `school-admin/incidents/[id]/close` (Resolution Report — UC-013), `school-admin/students/[id]` (đã có form sửa UC-099), `school-admin/parents/[id]` (Parent Account + Linked Students), `school-admin/academic-years/[id]` (Detail + AlertDialog Activate/Close), `school-admin/compliance` (Compliance Dashboard UC-027), `school-admin/violations/[id]`, `school-admin/classrooms/[id]` (đã có field Sức chứa + enforce MSG-011).

➡️ **Kết luận:** phần lớn "gap luồng P0/P1" trong 2 báo cáo screen đã được vá. Báo cáo này **không lặp lại** các gap đó; thay vào đó tập trung **lỗi trong chính code mới dựng** + **lỗi hệ thống** chưa ai ghi nhận.

**Trạng thái kiểm thử tự động:** `tsc --noEmit` **pass sạch** (không lỗi type). `next lint` **không chạy được** vì ESLint chưa cấu hình (xem L1).

---

## H. Lỗi NGUỒN DỮ LIỆU — hai nguồn sự thật (mức hệ thống, P1)

### H1. `db` clone tách rời khỏi mảng seed → mutation vô hình giữa các màn
- `db.ts:56-58` clone seed bằng `JSON.parse(JSON.stringify(...))`. Mock API (`mock-server.ts`) chỉ đọc/ghi trên **bản clone `db`**.
- Nhưng **~25 màn import thẳng mảng seed gốc** (giá trị runtime, không phải type) từ `@/lib/mock-data` và render trực tiếp:
  `INCIDENTS`, `CORRECTIVE_ACTIONS`, `SCHOOLS`, `ERROR_LOGS`, `DELIVERIES`, `SUPPLIER_BATCHES`, `SUPPLIER_DOCUMENTS`, `PARTNERSHIP_APPLICATIONS`, `SUPPLIER_PARTNERSHIPS`, `STUDENTS`, `MEAL_HISTORY`, `INSPECTIONS`, `PARTNERS`, `PURCHASE_REQUESTS`, `WEEKLY_MENU_DAYS`, `NUTRITION_HISTORY`, `SPECIAL_MEAL_PLANS`, `SPECIAL_MEAL_RECORDS`, `DISH_BATCHES`…
- **Hệ quả:** mọi thay đổi qua API (tạo/sửa/xóa → ghi vào `db`) **không phản chiếu** sang các màn đọc mảng seed gốc, và ngược lại. Đây là **hai nguồn sự thật song song** — nguồn lỗi âm thầm khi demo (sửa ở màn A, mở màn B thấy dữ liệu cũ).
- **Đề xuất:** thống nhất **mọi màn đọc qua hook API** (`usePagedList`/`useDetail`) thay vì import mảng giá trị. Chỉ cho phép import **type** và **hằng cấu hình bất biến** (danh mục, nhãn) từ `mock-data`; cấm import mảng dữ liệu động để render.

### H2. (Cụ thể của H1) Màn Đóng sự cố đọc seed tĩnh nhưng ghi qua API
- `school-admin/incidents/[id]/close/page.tsx:25,36,54` — render bằng `INCIDENTS.find(...)` và gate CAPA bằng `CORRECTIVE_ACTIONS.filter(...)` (seed **tĩnh**), nhưng đóng sự cố bằng `useUpdate("incidents")` (ghi vào **db**).
- **Hệ quả thực tế:**
  1. Nếu một CAPA được cập nhật `COMPLETED` ở màn Corrective Actions (qua API → db), màn Đóng sự cố **vẫn đọc seed cũ** → nút "Đóng sự cố" bị khóa sai (`capaDone` = false dù đã xong) — hoặc mở sai chiều ngược lại.
  2. Sau khi bấm Đóng (ghi db), quay lại chính màn này vẫn thấy trạng thái **OPEN** (đọc seed).
- **Đề xuất:** thay `INCIDENTS.find` → `useDetail<Incident>("incidents", id)`; `CORRECTIVE_ACTIONS` → `usePagedList("corrective-actions", { pageSize: 100 })`. Cùng chuẩn với các màn detail khác.

---

## I. Mâu thuẫn NGƯỠNG nghiệp vụ giữa các màn (hiện thực cụ thể của C1 — P1, chạm an toàn)

### I1. Ngưỡng nhiệt độ lõi: Compliance Dashboard dùng **60°C**, phần còn lại dùng **75°C**
- `school-admin/compliance/page.tsx:22-47,131` — đánh giá `tempOk` với ngưỡng **60°C**: 65°C và 68°C bị coi là **ĐẠT** (`tempOk: true`), chỉ 58°C mới "Dưới 60°C". Badge ghi `BR-091`.
- Nhưng ngưỡng chuẩn toàn hệ thống là **75°C theo BR-090** (`service-caterers/inspections/[id]/page.tsx:389`, `components/app/send-compliance-report-dialog.tsx:120` — đã dẫn trong báo cáo cũ mục C1).
- **Hệ quả:** cùng một quy tắc "nhiệt độ lõi tối thiểu", hai màn cho **hai kết quả pass/fail trái ngược** ở dải 60–74°C. Trên bảng tuân thủ của BGH, 68°C hiển thị "an toàn" trong khi kiểm thực coi là **KHÔNG đạt** — sai lệch chạm an toàn thực phẩm, không chỉ là bất tiện cấu hình.
- **Sai luôn mã BR:** dùng `BR-091` cho nhiệt độ lõi, trong khi các màn khác dùng `BR-090`.
- **Đề xuất:** đưa ngưỡng vào `BUSINESS_RULES.CORE_TEMP_MIN = 75` (đề xuất C1), mọi màn import từ đó; sửa dữ liệu mẫu `BATCHES` cho nhất quán.

---

## J. Lỗi LOGIC trong màn School Admin mới dựng (P1/P2)

### J1. `parents/[id]` suy "xác minh email" từ trạng thái hồ sơ **sức khỏe** của con
- `school-admin/parents/[id]/page.tsx:82` — `verified = linked.every(s => s.healthStatus === "VERIFIED")`, rồi hiển thị StatCard **"Xác minh email (BR-010)"** và badge "Verified/Pending" theo biến này.
- **Sai khái niệm:** xác minh **email tài khoản phụ huynh** (BR-010) khác hoàn toàn xác minh **hồ sơ sức khỏe học sinh** (BR-031). Một phụ huynh đã xác minh email vẫn hiện "Chờ xác minh" chỉ vì một đứa con có `healthStatus = PENDING`.
- **Đề xuất:** không suy từ `healthStatus`. Prototype nên gắn cờ `emailVerified` trên bản ghi phụ huynh (hoặc bỏ StatCard này tới khi có dữ liệu tài khoản phụ huynh thật), tránh hiển thị sai trạng thái tuân thủ.

### J2. Checklist hồi phục khi Đóng sự cố là học sinh "ảo" + gate rỗng khi đếm = 0
- `school-admin/incidents/[id]/close/page.tsx:43-50` — sinh danh sách hồi phục từ **con số** `incident.affectedStudents` thành các dòng `Học sinh bị ảnh hưởng #1..#N` (không phải HS thật — đúng gap A2 của báo cáo cũ, nhưng đây là **chỗ nó gây lỗi vận hành**).
- **Gate rỗng:** `allRecovered = affected.every(...)`; nếu `affectedStudents === 0` thì `every` trên mảng rỗng = **true** → điều kiện BR-155 "toàn bộ HS đã hồi phục" **tự động đạt** mà không kiểm gì. Với sự cố khai báo 0 HS ảnh hưởng, checklist an toàn bị vô hiệu.
- **Đề xuất:** khi có `affectedStudentIds` (đề xuất A2) thì render HS thật; trước mắt: nếu `affectedStudents === 0` vẫn phải hiển thị ghi chú và không để gate mặc định pass do mảng rỗng.

---

## K. Xác nhận các mục báo cáo cũ VẪN CHƯA sửa (không phải lỗi mới, nhưng đã kiểm lại còn tồn tại)

### K1. Create Academic Year: text hứa nhưng không có checkbox "Sao chép cấu trúc lớp" + thiếu validate Start<End
- `academic-years/page.tsx:80-83` mô tả "Có thể sao chép cấu trúc lớp từ năm liền trước (BR-194)" nhưng **không có checkbox** nào; `submit()` (57-73) chỉ validate rỗng, **không validate `start < end`** (đúng gap 42 của báo cáo screen).

### K2. `Classroom` seed thiếu field `capacity` → sức chứa luôn mặc định 50
- Type có `capacity?: number` (`mock-data.ts:1449`) nhưng **10 bản ghi `CLASSROOMS` không set `capacity`** → `classroom.capacity ?? 50` luôn ra 50.
- `students/[id]/page.tsx:34,64` hardcode `CAPACITY = 50` khi kiểm đổi lớp, **không đọc sức chứa thật của lớp đích** → nếu sau này có lớp sức chứa 40, việc đổi lớp vẫn cho tới 50.
- **Đề xuất:** thêm `capacity` vào seed `CLASSROOMS`; các màn đọc `target.capacity` thay vì hằng 50.

### K3. Sinh ID phía client `Date.now().slice(-n)` vẫn còn ~20 chỗ (E5)
- Xác nhận còn ở: `students/page.tsx:96`, `staff/page.tsx:132`, `staff/upload/page.tsx:62`, `academic-years/page.tsx:65`, `system-admin/users/page.tsx:153`, `system-admin/roles/page.tsx:105`, và ~14 màn service-caterers/supplier. Hậu tố ngắn `slice(-2)/(-3)` dễ trùng (đúng E5).

### K4. Bất nhất số liệu dẫn xuất trong seed (E4)
- `AcademicYear "AY-2025"` khai `classes: 28` nhưng chỉ seed **10** `CLASSROOMS` cho AY-2025 → màn `academic-years/[id]` hiện StatCard "Số lớp = 28" trong khi bảng dưới chỉ 10 dòng. Field dẫn xuất lưu cứng, lệch nguồn thật.

---

## L. Tooling / cấu hình build (P2, nhưng rẻ và nên làm sớm)

### L1. ESLint CHƯA cấu hình — không có lint trong CI
- `next lint` bật prompt "How would you like to configure ESLint?" ⇒ dự án **chưa có** cấu hình ESLint. `package.json` có script `"lint": "next lint"` nhưng chạy interactive, **không dùng được trong pipeline**.
- **Hệ quả:** không bắt được lỗi tĩnh phổ biến (react-hooks/exhaustive-deps, no-unused-vars, key thiếu…). Đây là lý do các lỗi kiểu J vẫn lọt.
- **Đề xuất:** thêm `eslint` + `eslint-config-next` + `.eslintrc`, bật `react-hooks` rules; đưa `lint` vào bước kiểm trước merge.

### L2. Nhiều lockfile → Next suy sai workspace root
- Cảnh báo build: tồn tại cả `03_Design/08_FrontEnd/package-lock.json` **và** `03_Design/08_FrontEnd/Web/package-lock.json` → Next chọn nhầm root, `outputFileTracingRoot` chưa set.
- **Đề xuất:** xóa lockfile thừa ở thư mục cha (hoặc set `outputFileTracingRoot` trong `next.config`), tránh lệch tracing/bundling khi build production.

### L3. `key={index}` trong danh sách động — 36 chỗ / 21 file
- Nhiều nơi dùng `key={i}` khi render list (vd `medical-staff/inspections`, `service-caterers/inspections/[id]`, `menu-form`…). Với danh sách **thêm/xóa/sắp xếp** (không chỉ skeleton tĩnh), key theo index gây lỗi tái sử dụng state/DOM.
- **Đề xuất:** rà soát, đổi sang key ổn định (id/tên) cho các list có thể thay đổi thứ tự; giữ index chỉ cho skeleton/placeholder tĩnh.

---

## M. Bảng tổng hợp & ưu tiên (lỗi MỚI)

| # | Vấn đề | Nhóm | Tác động | Công sức | Ưu tiên |
|---|---|---|---|---|---|
| I1 | Ngưỡng nhiệt độ lõi 60°C vs 75°C giữa Compliance & Inspection | An toàn | Cao (sai pass/fail an toàn TP) | Thấp | **P1** |
| H2 | Màn Đóng sự cố đọc seed tĩnh nhưng ghi qua API → gate CAPA sai | Dữ liệu | Cao (khóa/mở sai nút đóng) | Thấp | **P1** |
| H1 | Hai nguồn sự thật: ~25 màn import mảng seed thay vì API | Dữ liệu | Cao (mutation vô hình) | Vừa–Cao | **P1** |
| J2 | Checklist hồi phục HS "ảo" + gate rỗng khi affected=0 | Logic | Trung bình (BR-155 vô hiệu) | Thấp | P1 |
| J1 | `parents/[id]` suy "xác minh email" từ healthStatus của con | Logic | Trung bình (hiện sai trạng thái) | Thấp | P2 |
| K2 | Classroom seed thiếu `capacity` → luôn = 50 | Dữ liệu | Thấp–TB | Thấp | P2 |
| K1 | Create Academic Year thiếu checkbox BR-194 + validate Start<End | Field | Thấp–TB | Thấp | P2 |
| L1 | ESLint chưa cấu hình (không lint CI) | Tooling | Trung bình (lọt lỗi tĩnh) | Thấp | P2 |
| K3 | ID client `Date.now().slice` còn ~20 chỗ | Dữ liệu | Trung bình (trùng id) | Thấp–Vừa | P2 |
| L2 | Nhiều lockfile → sai workspace root | Tooling | Thấp | Thấp | P2 |
| K4 | Số liệu dẫn xuất seed lệch (classes 28 vs 10 lớp) | Dữ liệu | Thấp | Thấp | P3 |
| L3 | `key={index}` trong list động (36 chỗ) | Chất lượng | Thấp | Vừa | P3 |

---

## N. Kế hoạch chỉnh sửa đề xuất (theo giai đoạn — trọng tâm School Admin / System Admin)

**Giai đoạn 1 — Quick win an toàn & đúng luồng (P1, chạm ngay, rẻ):**
1. **I1 — Thống nhất ngưỡng nhiệt độ lõi:** tạo `lib/business-rules.ts` với `CORE_TEMP_MIN = 75` (kèm các ngưỡng C1 khác: geofence 200m, lưu mẫu 72h, phản hồi 24h, file 10MB, mẫu 100g/150ml). Sửa `school-admin/compliance` dùng 75°C + đúng `BR-090`; các màn inspection cùng import.
2. **H2 — Màn Đóng sự cố đọc qua API:** thay `INCIDENTS`/`CORRECTIVE_ACTIONS` tĩnh bằng `useDetail("incidents", id)` + `usePagedList("corrective-actions")`. Vá gate CAPA đọc đúng nguồn `db`.
3. **J2 — Vá gate hồi phục:** khi `affectedStudents === 0` không để `every([])` mặc định pass; hiển thị rõ "không có HS cần theo dõi" nhưng vẫn yêu cầu closing statement.

**Giai đoạn 2 — Dọn nguồn dữ liệu & tuân thủ field (P1/P2):**
4. **H1 — Chuẩn hóa nguồn đọc:** rà 25 màn import mảng seed → chuyển sang hook API. Đặt quy ước: `mock-data` chỉ export **type + danh mục cấu hình**; dữ liệu động chỉ qua `db`/hook. (Phối hợp cả nhóm vì chạm nhiều actor — xem ghi chú.)
5. **K2 — Thêm `capacity` vào seed `CLASSROOMS`;** `students/[id]` + `classrooms/[id]` đọc sức chứa thật thay vì hằng 50.
6. **K1 — Create Academic Year:** thêm checkbox "Sao chép cấu trúc lớp" (BR-194) + validate `start < end`.
7. **J1 — `parents/[id]`:** tách cờ `emailVerified` khỏi `healthStatus`, hoặc ẩn StatCard tới khi có dữ liệu tài khoản phụ huynh.

**Giai đoạn 3 — Tooling & chuẩn hóa kỹ thuật (P2/P3):**
8. **L1 — Cấu hình ESLint** (`eslint-config-next` + `react-hooks`), đưa `lint` vào CI trước merge.
9. **K3 — Sinh ID tập trung:** dùng `nextId()` của `mock-server` thay `Date.now().slice` ở ~20 màn (đúng đề xuất E5).
10. **L2 — Dọn lockfile thừa / set `outputFileTracingRoot`.** **L3 — Đổi `key={index}` sang key ổn định** cho list động. **K4 — Tính field dẫn xuất runtime** (classes/students) thay vì seed cứng.

> **Ghi chú phối hợp:** H1 và I1 chạm mock database dùng chung + nhiều actor (Medical Staff, Service Caterers, Supplier, Teacher) — cần thống nhất với cả nhóm trước khi đổi nguồn đọc/ngưỡng, tránh vỡ chéo. Nguyên tắc: đổi dần từng resource, giữ tương thích ngược, ưu tiên các resource School Admin quản (incidents, corrective-actions, classrooms, academic-years, students, parents) trước.

---

## O. Ghi chú giới hạn của rà soát này
- `tsc --noEmit` pass sạch; **chưa chạy `next build` full** (typecheck đã bao phủ phần lớn; build chỉ thêm cảnh báo Suspense/tracing đã nêu ở L2 và commit gần nhất `8b5231a`).
- Không chạy được `next lint` (L1) nên các phát hiện logic (H/I/J) đến từ **đọc code thủ công**, không phải linter.
- Tập trung actor School Admin & System Admin (phạm vi của Nam); các lỗi hệ thống H1/I1/K3 có chạm actor khác đã ghi rõ để phối hợp.
- Rà soát trên mock tĩnh, chưa nối API thật — hành vi điều kiện (gate CAPA, MSG-080…) đánh giá qua code tĩnh.
