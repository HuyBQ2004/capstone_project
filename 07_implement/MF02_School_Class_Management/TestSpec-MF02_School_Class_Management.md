# TEST SPECIFICATION — MF02 School & Class Management (+ Academic Year)

| Field | Value |
|-------|-------|
| **Document ID** | `SFMS-MF02-TEST-001` |
| **Version** | `1.0` |
| **Date** | `2026-07-30` |
| **Status** | `Approved` |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 — Software Testing Part 3: Test Documentation |
| **Author** | Nguyễn Văn Nam — Leader / Backend (chủ MF02) |
| **Reviewed by** | Nguyễn Phương Thảo — Lead Test/QA Reviewer · Nguyễn Đức Mạnh — Backend & DB · Bùi Quang Huy — Traceability — cả nhóm duyệt 2026-07-31 |
| **DPO Sign-off** | `[ ] Pending` *(dữ liệu trường học `Confidential`; không chứa PII học sinh — HS ở MF03)* |
| **Approved by** | Nguyễn Văn Nam — Final Approve, 2026-07-31 |
| **Classification** | `Internal — Confidential` |

**References (thứ tự ưu tiên khi xung đột — cao xuống thấp):**

0. `07_implement/00_Common_Conventions.md` — **D1–D7, Approved 2026-07-30**. Mọi expected value về **định dạng** theo file này.
1. `07_implement/MF02_School_Class_Management/TDS-MF02_Backend.md` (`SFMS-MF02-IMP-001` v1.0) — **nguồn chính**: §13 (xương sống), §9 (API), §10 (mã lỗi), §16 (auth matrix), §5.2 (DDL/CHECK).
2. `02_Requirements/00_BusinessRules/business_rules.md` §3 (BR-020…028) + §19 (BR-191…195) — **oracle nghiệp vụ**.
3. `02_Requirements/00_BusinessRules/error_message.md` **v1.5** — nội dung MSG nguyên văn làm expected message.
4. `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` **§1.2** — mỗi test case truy về ≥1 UC ID.
5. `03_Design/08_FrontEnd/Web/scripts/semantic-audit.ts` — **không có luật MF02** (xem §3 TDS-02 và Phụ lục C).
6. `03_Design/08_FrontEnd/Web/src/lib/mock-data.ts` — **chỉ** dùng làm nguồn SEED/FIXTURE (mã bản ghi thật).
7. `07_implement/00_Team_Work_Assignment_Plan.md` §2b — hợp đồng phân trang/search/filter · §2c-3b — prompt sinh tài liệu này.

> **Quy ước TDD:** Tài liệu này mô tả test case **TRƯỚC** khi viết production code.
> Thứ tự bắt buộc: viết test (JUnit 5) → chạy → xác nhận **FAIL 🔴** → implement → **PASS 🟢** → refactor 🔵.
> Không mark ✅ nếu `./mvnw verify` chưa xanh.
> **Stack test:** Java 25 LTS · JUnit 5 · Mockito · Spring Boot Test (`@WebMvcTest`/`@SpringBootTest`) · **Testcontainers PostgreSQL 16** · Flyway · REST Assured / `MockMvc` · JaCoCo.
> ⚠️ Template `PHASE-4_Test-Spec.md` minh họa bằng TypeScript/Prisma/`npm test` — **đã dịch toàn bộ sang Java/Maven/Flyway** theo ràng buộc stack (`00_Team_Work_Assignment_Plan.md` §2c-2). Không có `npx prisma`, không có `.spec.ts` trong tài liệu này.
> **Test data:** SYNTHETIC, seed từ `mock-data.ts`. Không dùng PII thật.

> ⭐ **MF02 là "vertical slice mẫu" (P0).** Đây là **TestSpec đầu tiên** của dự án (12 module còn lại chưa có) — cấu trúc §3 TDS-05 (fixture), §4 (ID/format test case), §5.1 (Red Gate), Phụ lục C (khai báo luật semantic-audit) là **convention cho 12 module còn lại copy**.

---

## CHANGELOG

> **Policy 4.4 — Immutable History:** Không bao giờ xóa thông tin cũ.

| Ngày | Người thực hiện | Nội dung thay đổi |
|------|-----------------|-------------------|
| 2026-07-30 | Nguyễn Văn Nam | Khởi tạo (v1.0) — TestSpec cho MF02 theo prompt mẫu §2c-3b. Nguồn: TDS-MF02_Backend.md §5.2/§9/§10/§13/§16, `00_Common_Conventions.md` D1–D7, `business_rules.md` §3+§19, `error_message.md` v1.5, `traceability_matrix.md` §1.2, `mock-data.ts` (fixture), `semantic-audit.ts` (xác nhận **0 luật MF02**). 45 test case: 17 unit · 18 integration · 10 security. Ghi nhận **9 Logic Issue** (§2), trong đó **4 thiếu oracle** và **1 mâu thuẫn nội bộ TDS** cần Nam/Huy/Mạnh chốt trước GREEN. |
| 2026-07-31 | Nguyễn Văn Nam | **Đóng toàn bộ Logic Issues §2 (L1, L3, L7, L8b, L9) + gap riêng của `SEC-005`, chuyển Status → Approved.** (1) **L1** — §5.2 bổ sung cột `code` (D1) cho `schools`/`academic_years`/`classrooms`. (2) **L3** — **ADR-M02-5**: `homeroom_teacher_id` → `user_accounts.id`, không có bảng `staff` riêng. (3) **L7** — cross-school → 404 `SCH-404` (không phải 403), nhất quán MF01 `requireInScope()`. (4) **L9** — bổ sung `SCH-409`/**MSG-139** (`error_message.md` v1.6) cho D5 và `taxId` trùng. (5) **L8b** — quyết định người dùng (Nam): giữ luật "capacity ≥ sĩ số hiện tại", cấp **BR-219** mới (`business_rules.md` v1.10) + **`SCH-012`/MSG-140** (`error_message.md` v1.7). (6) **`SEC-005`** — bổ sung **`SCH-014`/MSG-141** (`error_message.md` v1.8) cho rate-limit 429. Mở khóa toàn bộ 6 test case từng ⛔ (`005b`, `016`, `INT-015`, `SEC-002`, `003`, `SEC-005`) — **45/45 test case sẵn sàng implement**. |
| 2026-08-01 | Nguyễn Văn Nam | **Chốt L2 — cuối cùng còn lại của §2: giữ vĩnh viễn cả `Classroom.code` lẫn `Classroom.name`, không gộp.** Quyết định người dùng (Nam), không cần chờ Minh. `code` = định danh bất biến (D1); `name` = nhãn hiển thị đổi độc lập qua PATCH — khớp đúng code backend MF02 đã sinh sẵn, không cần sửa schema/service/test. Ghi nhận rủi ro liên module còn lại (Phụ lục C.3): MF05/MF09 nên liên kết bằng `code`/`classroomId`, không nên dùng `name` làm khóa vì có thể đổi. |

---

## MỤC LỤC

1. [Thông tin Module](#1-thông-tin-module) · 2. [Logic Issues Resolved](#2-logic-issues-resolved) · 3. [Test Design Specification](#3-test-design-specification) · 4. [Test Case Specification](#4-test-case-specification) · 5. [Red-Green-Refactor Tracker](#5-red-green-refactor-tracker) · 6. [Entry / Exit Criteria](#6-entry--exit-criteria) · 7. [Rollback Plan](#7-rollback-plan) · 8. [Anti-Pattern Detection](#8-case-20-anti-pattern-detection) · [Phụ lục A–D](#phụ-lục-a--danh-sách-uc--br--msg-đã-trích-dẫn)

---

## 1. Thông tin Module

| Field | Value |
|-------|-------|
| **Feature / Module ID** | `MF02` — School & Class Management (+ Academic Year) |
| **Module** | Organization & Academic Structure (Bounded Context) |
| **Spec gốc** | `SFMS-MF02-IMP-001` (TDS-MF02_Backend.md v1.0) |
| **Priority** | 🔴 **P0** — vertical slice mẫu, cả team chờ convention |
| **Sprint** | Ngày 1–2 theo lịch `00_Team_Work_Assignment_Plan.md` §4b |
| **Milestone** | TDS Approved → Backend MF02 chạy được qua Swagger |
| **Data Classification** | `Confidential` (hồ sơ trường, tọa độ GPS, mã số thuế) — **không** PII học sinh |
| **Compliance Scope** | PDPD (theo ADR-000) |
| **Upstream Dependencies** | **MF01** — JWT phát `AuthUser{userId, role, schoolId}`; danh mục `staff` (Teacher) cho phân công GVCN; `common/` (`PagedResponse<T>`, `@RestControllerAdvice`, `common/enums/`) |
| **Downstream Consumers** | **MF03** (Student ← Classroom) · **MF05** (Menu theo khối) · **MF06/07/08** (tọa độ GPS + geofence làm mốc xác minh vị trí — BR-028) · **MF09** (phân suất theo lớp) · **MF13** (thống kê theo trường/lớp/năm học) |
| **Số UC bao phủ** | **16/16** (traceability §1.2) |
| **Số BR bao phủ** | **12/12** (BR-020, 021, 022, 024, 025, 026, 028, 191, 192, 193, 194, 195) |

### 1.1 AI Generation Context (CASE 2.0)

| Field | Value |
|-------|-------|
| **AI Assisted?** | `Yes` |
| **Constraint Source** | `TDS-MF02_Backend.md` §13 (xương sống), §9, §10, §16, §5.2 · `00_Common_Conventions.md` D1–D7 · `00_Team_Work_Assignment_Plan.md` §2b + §2c-3b |
| **Constraints Injected** | (1) Oracle **bắt buộc trích dẫn** BR/UC/MSG — không dùng giả định AI; thiếu oracle thì ghi rõ "thiếu oracle", không bịa expected. (2) Định dạng expected theo **D1–D7**, **không** theo mock (mock cố ý đóng băng ở bản cũ — Team Plan §0). (3) Đủ **3 lớp**: Unit · Integration · Security. (4) Error path phải đúng **envelope D2** + `code` khớp §10 TDS. (5) Bắt buộc có test **"gọi API trực tiếp bỏ qua UI"** cho gate BR-025 capacity. (6) Tên luật `semantic-audit.ts` copy nguyên văn từ script, không lấy từ TDS §13. |
| **Model** | Claude Opus 5 (`claude-opus-5`) — theo §2c-4: TestSpec dùng Opus vì oracle sai là test vô nghĩa |
| **Trust Level** | `T2 → T3 (pending Red Gate §5.1)` |

---

## 2. Logic Issues Resolved

> **Bắt buộc điền trước khi viết test.** Liệt kê mọi sai lệch giữa spec thiết kế (TDS/mock) và hợp đồng đã chốt (D1–D7) hoặc oracle (BR/MSG). **Test case encode hành vi ĐÃ SỬA**, không phải hành vi trong spec gốc.
>
> Cột **Trạng thái**: ✅ = đã tự giải quyết được bằng D1–D7/oracle, test encode hành vi đúng · ⛔ = **thiếu oracle / mâu thuẫn chưa chốt**, test viết ở dạng RED và **chặn GREEN** cho đến khi người phụ trách chốt.

| # | Spec gốc (sai / thiếu / mâu thuẫn) | Thực tế (schema / D1–D7 / oracle) | Fix áp dụng trong test | Trạng thái | Người chốt |
|---|---|---|---|---|---|
| **L1** | ~~TDS §3 bảng D1 tự yêu cầu *"§5.2 phải có `code VARCHAR(32)` + `UNIQUE(school_id, code)`"*, nhưng **§5.2 thực tế không có cột `code`**~~ **✅ ĐÃ CHỐT 2026-07-31.** | **D1**: `id` JSON **và** path param = **mã nghiệp vụ**; UUID chỉ là PK nội bộ. §5.2 đã bổ sung `code` cho `schools` (UNIQUE toàn cục), `academic_years` (`UNIQUE(school_id, code)`), `classrooms` (`UNIQUE(academic_year_id, code)`). `school_config` **không cần** — chỉ địa chỉ hoá qua `/schools/{code}/config`, không có endpoint riêng. | Test encode **D1**: `MF02-TC-INT-011` assert path param nhận `code`, JSON `id` = `code`, **không** rò UUID ra response, và `GET /schools/{uuid}` → **404 MSG-137**. `MF02-TC-INT-009/010` dùng `code` ở mọi assertion. | ✅ **Đã mở khóa hoàn toàn** — §5.2 TDS đã có cột `code` | Nam — đã chốt 2026-07-31 |
| **L2** | ~~**Định danh lớp học lệch 3 chỗ:** mock `Classroom` **không có field `name`** — `id` **chính là** tên lớp (`"3A"`); Team Plan §2b bảng SEARCH_FIELDS ghi MF02/`classrooms` = **`id`, `teacher`** (và ghi rõ *"❌ `name` không tồn tại"*); nhưng TDS §5.2 khai cột `name VARCHAR(20)`, §9.2 trả **cả** `id` **và** `name` (`"3C"`), còn TDS §17.1 C1 ghi SEARCH_FIELDS = **`name`, `teacher`**.~~ **✅ ĐÃ CHỐT 2026-08-01.** | **Chốt: giữ vĩnh viễn cả hai cột, không gộp.** Hòa giải theo **D1**: `Classroom.code` = định danh bất biến, lộ ra JSON dưới tên `id`, dùng ở path/`UNIQUE(academic_year_id, code)`. `name` = nhãn hiển thị, **được phép đổi độc lập** qua `PATCH` (không đụng `code`) — khớp code MF02 đã sinh (`ClassroomService.update`). Hai giá trị = nhau lúc tạo, có thể lệch sau khi đổi tên — **đúng thiết kế**, không phải dư thừa. | `MF02-TC-INT-010` assert `GET /classrooms?search=3A` tìm được lớp 3A **qua mã lớp** và `search=Đào Văn Quân` tìm được qua `teacher` — đúng với cả hai cách đọc SEARCH_FIELDS. `MF02-TC-INT-011` dòng g giữ nguyên: ngay sau khi tạo, `id` (=`code`) và `name` bằng nhau; test **không** cần thêm case "đổi tên xong lệch code" (ngoài phạm vi TestSpec hiện tại). | ✅ **Đã chốt — giữ cả 2 cột, không đổi schema/code** | Nam — đã chốt 2026-08-01 |
| **L3** | ~~**`teacherId` có hai không gian định danh cho cùng một người**~~ — **✅ ĐÃ CHỐT 2026-07-31.** mock `Classroom.teacherId` trỏ **`UserAccount.id`** (`4A` → `"U-0010"`); TDS §9.2 dùng `{"teacherId": "STF-003"}`. Cả hai đều là **"Đào Văn Quân"**. | **ADR-M02-5 (chốt):** `homeroom_teacher_id` → `user_accounts.id`. KHÔNG có bảng `staff` riêng — khớp thiết kế đã triển khai ở MF01 (`user_accounts` có cả cột `code` và `staff_code`, cùng 1 PK). `teacherId` nhận `code` hoặc `staff_code`, cả hai resolve về cùng 1 UUID. | `MF02-TC-005` / `MF02-TC-006` / `MF02-TC-INT-005` assert: bất kể API nhận `STF-003` hay `U-0010`, giá trị đó resolve về đúng một `UserAccount` có `role=Teacher`, `status=ACTIVE`, cùng `schoolId`; bất biến BR-020 kiểm trên `homeroom_teacher_id` (UUID) — **đúng một cột**. `MF02-TC-005b`: gán cùng một người qua **hai mã khác nhau** cho 2 lớp → **phải** 409 SCH-011. | ✅ **Đã mở khóa** — TDS §5.2/§9.2/ADR-M02-5 cập nhật | Nam (ADR-M02-5) — đã chốt 2026-07-31 |
| **L4** | **BR-191 per-school vs mock toàn cục (CF-M02-3):** mock `ACADEMIC_YEARS` **không có `schoolId`**, và handler `POST /academic-years/:id/activate` (`mock-server.ts` dòng ~234) tìm năm ACTIVE **không lọc theo trường** (`db["academic-years"].find(r => r.status === "ACTIVE" && r.id !== id)`). | **BR-191 nguyên văn:** *"Mỗi trường chỉ được có **đúng 1** `AcademicYear` ở trạng thái **Active**"* → per-school. TDS §5.2 đã có `school_id` + `uq_ay_one_active_per_school ON academic_years(school_id) WHERE status='ACTIVE'`. | `MF02-TC-INT-002` (★ CRITICAL) là test **chuyên để bắt lỗi này**: trường B kích hoạt năm học **phải 200** trong khi trường A đang có năm ACTIVE. Logic của mock sẽ **FAIL** test này — đúng mục đích. | ✅ (test theo BR-191) | — (đã chốt ở ADR-M02-4) |
| **L5** | **Kiểu ngày của `registeredAt` lệch giữa §5.2 và §9.2:** §5.2 khai `Instant registeredAt` / `registered_at TIMESTAMPTZ`, nhưng §9.2 (`POST /schools/register` 201) trả `"registeredAt": "2026-07-28"` — **date-only**, là dạng của `LocalDate`. Seed mock thì là `"05/01/2026"` (`dd/MM/yyyy`). | **D4:** `LocalDate` → `"2026-07-29"`; `Instant` → `"2026-07-29T02:15:00Z"`. Kiểu đã chốt ở §5.2 là `Instant` ⇒ JSON **phải** có phần thời gian + `Z`. | `MF02-TC-INT-012` assert **theo kiểu ở §5.2**: `registeredAt`/`createdAt`/`updatedAt` khớp regex `Instant` ISO-8601 UTC; `startDate`/`endDate` khớp regex `LocalDate`. Đồng thời assert **0 kết quả** cho regex `dd/MM/yyyy` trên toàn bộ response body. | ✅ (test theo D4 + kiểu §5.2) + **sửa ví dụ §9.2** | Nam (§9.2) |
| **L6** | **`SCH-013` mang hai nghĩa, hai HTTP status:** §10 có **2 dòng** cùng mã `SCH-013` — `400` + MSG-107 (tọa độ ngoài dải) và `403` + MSG-108 (tọa độ bị khóa). | **D2** quy định FE tra `lib/messages.ts` map **`code` → nhãn tiếng Việt** ⇒ một `code` mang hai nghĩa thì FE **không thể** chọn đúng nhãn. `error_message.md` v1.5 đã cấp **2 MSG riêng** (107, 108) nhưng TDS vẫn dồn về 1 code. | `MF02-TC-007` (400 + MSG-107) và `MF02-TC-009` (403 + MSG-108) assert **cả `httpStatus` lẫn `error.message` nguyên văn** để phân biệt — test vẫn chạy đúng với mã hiện tại. Ghi khuyến nghị: tách `SCH-014` cho trường hợp khóa (403). | ✅ (test phân biệt bằng status + message) + **khuyến nghị tách code** | Nam (§10) + Minh (FE `messages.ts`) |
| **L7** | ~~**Cross-school đọc tài nguyên trường khác: 403 hay 404?**~~ — **✅ ĐÃ CHỐT 2026-07-31.** TDS tự mâu thuẫn — §13/§16 ghi 403 SCH-005, §10 ghi 404 SCH-404. | **Chốt: 404 SCH-404/MSG-137**, không phải 403. Nhất quán với quyết định đã triển khai ở MF01 (`StaffService.requireInScope()` → 404, tránh lộ tồn tại qua mã HTTP — OWASP tenant isolation). `SCH-005`/403 chỉ còn cho sai vai trò **trong cùng trường**. | `MF02-TC-SEC-002` assert đúng một nhánh: `status = 404`, `code = SCH-404`, `message` = MSG-137 nguyên văn; **VÀ** response không chứa field dữ liệu nào của trường khác. | ✅ **Đã mở khóa** — TDS §10/§13/§16 cập nhật | Nam (§10/§16) — đã chốt 2026-07-31 |
| **L8** | **`MSG-010` bị dùng cho 3 điều kiện khác nhau:** (a) `capacity > 50` — **có** BR-025; (b) ~~*"capacity < sĩ số hiện tại"* — không có BR chống lưng~~ **✅ ĐÃ CHỐT 2026-07-31**; (c) `POST /academic-years/{id}/promote` §9.2 trả `"message": "Target classroom capacity would be exceeded."` — **không phải** nguyên văn MSG-010, vi phạm **D7**. | (a) BR-025 nguyên văn: *"Mỗi lớp học có sức chứa tối đa **50 học sinh**."* (b) **BR-219 mới** (`business_rules.md` v1.10) + **`SCH-012`/MSG-140** (`error_message.md` v1.7, TDS §10) — tách khỏi `SCH-010` theo D7. | (a) `MF02-TC-002` — oracle đầy đủ. (b) `MF02-TC-003` assert `error.code = "SCH-012"`, `error.message` = **MSG-140 nguyên văn**. (c) `MF02-TC-INT-008` assert `message` = **MSG-010 nguyên văn** (theo D7), **không** theo chuỗi ở §9.2 → §9.2 phải sửa. | (a) ✅ · (b) ✅ **đã mở khóa** · (c) ✅ (test theo D7) | (b) Nam — đã chốt 2026-07-31 · (c) Nam (§9.2) |
| **L9** | ~~**D5 (client-sinh `id`) không có mã lỗi trong §10**~~ — **✅ ĐÃ CHỐT 2026-07-31.** D5 yêu cầu 409 khi trùng `id`/`code`; §10 không có dòng `SCH-409`; cùng vấn đề với trùng `tax_id`. | `error_message.md` **v1.6** đã cấp **MSG-139** *"A record with this value already exists."* (dùng chung mọi module, giống MSG-137/138). TDS §10 đã thêm `SCH-409`/MSG-139 cho cả 2 tình huống (client-id trùng, `tax_id` trùng). | `MF02-TC-016` (trùng `id` client gửi) và `MF02-TC-INT-015` (trùng `tax_id`) assert `HTTP 409` + `error.code = "SCH-409"` + `error.message` = MSG-139 nguyên văn + bản ghi **không** bị ghi đè (assert DB). | ✅ **Đã mở khóa** — `error_message.md` v1.6 + TDS §10 cập nhật | Nam + Huy — đã chốt 2026-07-31 |

**Tổng hợp việc phải chốt trước khi chuyển GREEN:**

| Cần chốt | Ai | Chặn test case |
|---|---|---|
| ~~Bảng đích của `homeroom_teacher_id`~~ — **L3 ✅ đã chốt 2026-07-31** (`user_accounts.id`, ADR-M02-5) | Nam + Mạnh | Đã mở khóa `MF02-TC-005b` |
| ~~Cross-school = 403 hay 404~~ — **L7 ✅ đã chốt 2026-07-31** (404 SCH-404) | Nam + Huy | Đã mở khóa `MF02-TC-SEC-002` |
| ~~BR mới cho "capacity ≥ sĩ số hiện tại" hoặc bỏ luật~~ — **L8b ✅ đã chốt 2026-07-31** (BR-219, giữ luật) | Nam | Đã mở khóa `MF02-TC-003` |
| ~~Cấp MSG cho "mã nghiệp vụ/`taxId` đã tồn tại" (409)~~ — **L9 ✅ đã chốt 2026-07-31** (MSG-139/SCH-409, `error_message.md` v1.6) | Nam + Huy | Đã mở khóa `MF02-TC-016`, `MF02-TC-INT-015` |
| ~~Bổ sung cột `code` vào §5.2~~ — **L1 ✅ đã chốt 2026-07-31** · ~~giữ/bỏ `name`~~ — **L2 ✅ đã chốt 2026-08-01** (giữ cả 2 cột) · sửa ví dụ ngày §9.2 — **L5** · sửa message promote §9.2 — **L8c** | Nam (sửa TDS) | không chặn (test đã encode hành vi đúng) |

---

## 3. Test Design Specification

### TDS-01 — Scope / Phạm vi

```
MF02 (com.sfms.mf02) — các layer được kiểm thử:
├── domain/       School, AcademicYear, Classroom, SchoolConfig + enum trạng thái
│                 → Unit: bất biến thuần (grade, capacity, lat/lng, geofence)
├── service/      SchoolService, ClassroomService, AcademicYearService, SchoolPolicyService
│                 → Unit: mock repository (Mockito), kiểm BR + mã lỗi + audit
├── repository/   Spring Data JPA + Specification (SchoolSpec, ClassroomSpec)
│                 → Integration: Testcontainers, kiểm bất biến DB + phân trang/search
├── web/          SchoolController, ClassroomController, AcademicYearController
│                 → Integration: endpoint → DB, happy + error path, envelope D2
└── security/     @PreAuthorize + JWT filter (AuthUser từ MF01) + cách ly schoolId
                  → Security: ma trận 7 role × 17 endpoint, cross-school, bypass UI
```

**NGOÀI phạm vi** (khai rõ để không kỳ vọng sai):
- Phát hành/ký JWT — thuộc **MF01**; MF02 chỉ **tin** `AuthUser{userId, role, schoolId}` do MF01 cấp. Test MF02 **stub** JWT (FX-020…FX-026).
- Cascade **thực thi** ở phía MF01/MF03 khi `SchoolDeactivated` (suspend `user_accounts`, `students`) — MF02 chỉ assert **event được publish** + lớp của trường bị tạm dừng (`MF02-TC-INT-017`).
- `costCeiling` (BR-080) — thuộc miền **MF05**; MF02 chỉ lưu/đọc trong `school_config`, không kiểm luật trần chi phí.
- Hồ sơ sức khỏe/dị ứng HS — thuộc **MF03**; test promote chỉ assert **không bị thay đổi** (BR-194).

### TDS-02 — Test Basis / Cơ sở Kiểm thử

| Source | Items Derived |
|--------|--------------|
| `traceability_matrix.md` **§1.2** | 16 UC ID + Actor + cột BR + cột MSG — **nguồn duy nhất** cho ánh xạ UC↔BR↔MSG (đã đối chiếu: khớp 100% với TDS §2.1) |
| `business_rules.md` §3 | **BR-020** (1 GVCN/lớp, gán mới hủy cũ + nhật ký) · **BR-021** (cascade deactivate) · **BR-022** (chỉ xem chính sách trường mình) · **BR-024** (grade 1–12) · **BR-025** (capacity ≤ 50) · **BR-026** (audit phân công GVCN) · **BR-028** (lat/lng dải hợp lệ, `coordinateLocked`, unlock chỉ System Admin + lý do + audit, geofence 200m mặc định / 50–500m) |
| `business_rules.md` §19 | **BR-191** (đúng 1 ACTIVE/trường; cho phép song song 1 DRAFT) · **BR-192** (Classroom thuộc đúng 1 AY; không tạo lớp khi chưa có AY Active) · **BR-193** (close → Classroom read-only, giữ lịch sử) · **BR-194** (copy cấu trúc lớp; "lên lớp" tường minh, loại trừ từng HS; hồ sơ sức khỏe không ảnh hưởng) · **BR-195** (Archived luôn truy vấn read-only) |
| `error_message.md` **v1.5** | Error: MSG-001, 004, 005, 007, 010, 011, 080, 081, **107, 108, 109**, 137, 138 · Success: MSG-029, 032, 033, 034, 082, 083, 084 — **expected message lấy nguyên văn tiếng Anh** (D7) |
| `00_Common_Conventions.md` | **D1** (id = mã nghiệp vụ) · **D2** (envelope lỗi) · **D3** (`/api/v1`) · **D4** (ISO-8601) · **D5** (client id → 409) · **D6** (enum SCREAMING_SNAKE) · **D7** (message tiếng Anh) |
| `TDS-MF02_Backend.md` §5.2 | CHECK constraint + partial unique index làm **assertion tầng DB**: `lat BETWEEN -90 AND 90`, `lng BETWEEN -180 AND 180`, `grade BETWEEN 1 AND 12`, `capacity BETWEEN 1 AND 50`, `geofence_radius BETWEEN 50 AND 500`, `uq_ay_one_active_per_school`, `uq_homeroom_teacher_per_year`, `uq_ay_school_name`, `uq_class_ay_name`, `tax_id UNIQUE` |
| `TDS-MF02_Backend.md` §9 | 17 endpoint + request/response shape → expected của integration test |
| `TDS-MF02_Backend.md` §10 | 12 dòng mã lỗi `SCH-*` → expected của error path |
| `TDS-MF02_Backend.md` §16 | Ma trận 7 role × 11 nhóm endpoint → `MF02-TC-SEC-001` |
| `TDS-MF02_Backend.md` §6.3/§6.4 | State machine `AcademicYear` (DRAFT→ACTIVE→CLOSED→ARCHIVED) và `School` (PENDING→ACTIVE→INACTIVE→ACTIVE) → State Transition Testing |
| `TDS-MF02_Backend.md` §7.1 | 7 domain event → assertion "event được publish" |
| `00_Team_Work_Assignment_Plan.md` §2b | Hợp đồng phân trang (5 field, clamp `page`, `pageCount ≥ 1`), thứ tự áp bộ lọc 9 bước, `SEARCH_FIELDS`, 5 edge case §2b-6 |
| `semantic-audit.ts` | ⚠️ **0 luật thuộc MF02** — xem Phụ lục C (khai báo bắt buộc, kèm bằng chứng grep) |

### TDS-03 — Test Conditions and Coverage Items

| Condition ID | Test Condition | Oracle | Coverage Item | Test Cases |
|---|---|---|---|---|
| `TC-COND-001` | Cấp lớp phải ∈ [1,12] | BR-024 | `ClassroomService.create()/update()`, CHECK `grade` | `MF02-TC-001` |
| `TC-COND-002` | Sức chứa lớp ≤ 50 | BR-025 | `ClassroomService.create()/update()`, CHECK `capacity` | `MF02-TC-002`, `MF02-TC-SEC-007` |
| `TC-COND-003` | Sức chứa mới ≥ sĩ số hiện tại | BR-219 (chốt 2026-07-31), TDS §17.1 C4 | `ClassroomService.update()` | `MF02-TC-003` |
| `TC-COND-004` | Không tạo lớp khi trường chưa có AY ACTIVE | BR-192 | `ClassroomService.create()` | `MF02-TC-004`, `MF02-TC-INT-004` |
| `TC-COND-005` | 1 GVCN chỉ chủ nhiệm 1 lớp trong 1 năm học | BR-020 | `assignHomeroomTeacher()`, `uq_homeroom_teacher_per_year` | `MF02-TC-005`, `MF02-TC-005b` |
| `TC-COND-006` | Gán GVCN mới tự hủy liên kết cũ + ghi nhật ký | BR-020, BR-026 | `assignHomeroomTeacher()` + audit | `MF02-TC-006`, `MF02-TC-INT-017` |
| `TC-COND-007` | Tọa độ GPS trong dải hợp lệ | BR-028 | `register()/update()`, CHECK `lat`/`lng` | `MF02-TC-007` |
| `TC-COND-008` | Mã số thuế đúng định dạng | BR-028, UC-070 | `SchoolService.register()` | `MF02-TC-008` |
| `TC-COND-009` | Tọa độ đã khóa → từ chối cập nhật (trừ System Admin đã unlock) | BR-028 | `SchoolService.update()` | `MF02-TC-009`, `MF02-TC-INT-014` |
| `TC-COND-010` | Unlock tọa độ bắt buộc có lý do văn bản + audit | BR-028 | `unlockCoordinate()` | `MF02-TC-010`, `MF02-TC-SEC-006` |
| `TC-COND-011` | Geofence ∈ [50,500] m, mặc định 200 m | BR-028 | `SchoolConfigService.update()`, CHECK `geofence_radius` | `MF02-TC-011`, `MF02-TC-INT-018` |
| `TC-COND-012` | Đúng 1 AY ACTIVE/trường; 1 DRAFT được song song | BR-191 | `activate()`, `uq_ay_one_active_per_school` | `MF02-TC-012`, `MF02-TC-INT-001`, `-002`, `-003` |
| `TC-COND-013` | copyStructure copy tên/cấp/sức chứa, **không** chuyển HS | BR-194 | `create(copyStructure=true)` | `MF02-TC-013`, `MF02-TC-INT-006` |
| `TC-COND-014` | Close AY → Classroom read-only, không xóa dữ liệu | BR-193 | `close()` | `MF02-TC-014`, `MF02-TC-INT-005` |
| `TC-COND-015` | Không lưu field dẫn xuất — đếm runtime | ADR-M02-2 (CF-M02-6) | `SchoolResponse.classes/staff/students` | `MF02-TC-015` |
| `TC-COND-016` | Client gửi `id` trùng → 409, không ghi đè | D5 | mọi `POST` | `MF02-TC-016`, `MF02-TC-INT-015` |
| `TC-COND-017` | "Lên lớp" là hành động tường minh, cho loại trừ từng HS, không đụng hồ sơ sức khỏe | BR-194, UC-177 | `promoteStudents()` | `MF02-TC-INT-007`, `-008` |
| `TC-COND-018` | Dữ liệu AY Archived luôn truy vấn được, read-only | BR-195 | `list()`, mọi query năm cũ | `MF02-TC-INT-016` |
| `TC-COND-019` | Vô hiệu hóa trường → cascade tạm dừng + audit | BR-021 | `changeStatus(INACTIVE)` | `MF02-TC-INT-017` |
| `TC-COND-020` | Mọi `GET` danh sách trả đúng 5 field, `page` clamp, `pageCount ≥ 1` | Team Plan §2b, §2b-6 | `PagedResponse<T>` | `MF02-TC-INT-009` |
| `TC-COND-021` | `search` chỉ chạy trên `SEARCH_FIELDS` cố định | Team Plan §2b-3, ADR-M02-1 | `SchoolSpec`, `ClassroomSpec` | `MF02-TC-INT-010`, `MF02-TC-SEC-010` |
| `TC-COND-022` | `id` JSON + path param = mã nghiệp vụ, không rò UUID | D1 | mọi endpoint | `MF02-TC-INT-011` |
| `TC-COND-023` | Mọi field ngày/giờ JSON là ISO-8601 | D4 | mọi endpoint | `MF02-TC-INT-012` |
| `TC-COND-024` | Mọi error path trả envelope `{error:{code,message,details}}`, `details` không `null` | D2, D7 | `@RestControllerAdvice` | `MF02-TC-INT-013` |
| `TC-COND-025` | RBAC 7 role đúng ma trận §16 | BR-007, §16 | `@PreAuthorize` | `MF02-TC-SEC-001`, `-008` |
| `TC-COND-026` | Cách ly cross-school theo `schoolId` trong JWT | BR-022 | mọi query lớp/AY/policy | `MF02-TC-SEC-002`, `-009` |
| `TC-COND-027` | Thiếu/hết hạn/sai JWT → 401 | BR-004, MSG-004 | JWT filter | `MF02-TC-SEC-003` |
| `TC-COND-028` | `schoolId`/`role` đọc từ JWT, **không** tin body/query | TDS §17.1 C2 (AP-AI-006) | mọi controller | `MF02-TC-SEC-004` |
| `TC-COND-029` | Đăng ký trường là endpoint public, có rate limit | §9.1, §16 | `POST /schools/register` | `MF02-TC-SEC-005` |
| `TC-COND-030` | Gate nghiệp vụ enforce **phía server**, gọi API trực tiếp không lách được | Prompt §2c-3b (bắt buộc cho MF02/BR-025) | `POST/PATCH /classrooms` | `MF02-TC-SEC-007` |

**Ma trận truy vết ngược UC → test case** (mọi UC ở §1.2 phải có ≥1 test case — kiểm đủ 16/16):

| UC | Test cases |
|---|---|
| UC-070 Register School | `MF02-TC-008`, `MF02-TC-INT-015`, `MF02-TC-SEC-005` |
| UC-001 Active/Deactivate School | `MF02-TC-INT-014`, `MF02-TC-INT-017` |
| UC-095 Update Information School | `MF02-TC-007`, `MF02-TC-009`, `MF02-TC-010`, `MF02-TC-011`, `MF02-TC-INT-014`, `MF02-TC-INT-018` |
| UC-122 View Information School | `MF02-TC-015`, `MF02-TC-INT-011`, `MF02-TC-SEC-001` |
| UC-140 View School List | `MF02-TC-INT-009`, `MF02-TC-INT-010`, `MF02-TC-SEC-001` |
| UC-018 Create Classroom | `MF02-TC-001`, `-002`, `-004`, `MF02-TC-INT-004`, `MF02-TC-SEC-007` |
| UC-090 Update Classroom | `MF02-TC-001`, `-002`, `-003`, `MF02-TC-SEC-007` |
| UC-008 Assign Homeroom Teacher | `MF02-TC-005`, `-005b`, `-006`, `MF02-TC-INT-005`, `MF02-TC-INT-017` |
| UC-037 Manage Classrooms | `MF02-TC-INT-009`, `MF02-TC-INT-010` |
| UC-165 View Classroom Detail | `MF02-TC-015`, `MF02-TC-SEC-002` |
| UC-166 View List Classrooms | `MF02-TC-INT-009`, `MF02-TC-SEC-002` |
| UC-137 View Policy Of School | `MF02-TC-SEC-009`, `MF02-TC-SEC-001` |
| UC-174 Create Academic Year | `MF02-TC-013`, `MF02-TC-016`, `MF02-TC-INT-006` |
| UC-175 Activate/Close Academic Year | `MF02-TC-012`, `-014`, `MF02-TC-INT-001`, `-002`, `-003`, `-005` |
| UC-176 View Academic Year List | `MF02-TC-INT-016`, `MF02-TC-SEC-001` |
| UC-177 Promote Students | `MF02-TC-INT-007`, `MF02-TC-INT-008` |

### TDS-04 — Test Techniques / Kỹ thuật Kiểm thử

| Technique (ISO 29119-4) | Applied To | Rationale |
|---|---|---|
| **Equivalence Partitioning** | `grade` (dưới dải / trong dải / trên dải), `capacity`, `geofenceRadius`, `status` của AY | Miền đầu vào rời rạc có biên rõ ràng ở BR-024/025/028; chia lớp tương đương giảm số case mà vẫn phủ hết hành vi |
| **Boundary Value Analysis** | `grade` ∈ {0,1,12,13} · `capacity` ∈ {0,1,50,51} · `lat` ∈ {-90.000001,-90,90,90.000001} · `lng` ∈ {-180.000001,-180,180,180.000001} · `geofenceRadius` ∈ {49,50,500,501} · `pageSize` ∈ {-1,0,1} · `page` > `pageCount` | Đây là nhóm lỗi off-by-one hay gặp nhất khi port BR sang CHECK constraint; biên là chỗ CHECK ở DB và validate ở service dễ lệch nhau |
| **State Transition Testing** | `AcademicYear`: DRAFT→ACTIVE→CLOSED→ARCHIVED (§6.3) · `School`: PENDING→ACTIVE→INACTIVE→ACTIVE (§6.4) | BR-191/193/195 là luật về **chuyển trạng thái**, không phải về giá trị; phải test cả transition **hợp lệ** và **không hợp lệ** (VD CLOSED→ACTIVE, DRAFT→CLOSED) |
| **Decision Table Testing** | `assignHomeroomTeacher()`: (GV tồn tại?) × (role Teacher?) × (cùng trường?) × (đang chủ nhiệm lớp khác?) × (lớp read-only?) | 5 điều kiện độc lập cùng quyết định 1 kết quả; bảng quyết định tránh bỏ sót tổ hợp |
| **Combinatorial (pairwise) Testing** | Ma trận 7 role × 17 endpoint (`MF02-TC-SEC-001`) | 119 tổ hợp — dùng `@ParameterizedTest` + `@MethodSource` sinh từ bảng §16, không viết tay 119 method |
| **Error Guessing** | SQLi trong `search`, `not:` param với field không tồn tại, `schoolId` giả trong body, gọi API bỏ qua UI, `pageSize` khổng lồ, race 2 request activate | Nhóm lỗi mà review 29/07 xếp nghiêm trọng nhất: gate chỉ tồn tại ở client thì gọi API trực tiếp là lách được toàn bộ luật |
| **Concurrency Testing** | 2 request `activate` song song trên cùng trường (`MF02-TC-INT-003`) | ADR-M02-4 chọn `PESSIMISTIC_WRITE` **vì** race này; không test thì quyết định kiến trúc không được xác minh |

### TDS-05 — Test Data Requirements

> **Nguồn:** `mock-data.ts` — dùng **mã bản ghi thật** để test chạy được trên dữ liệu có thật (`SCH-HN-0042`, `AY-2025`, `3A`, `STF-003`…).
> ⚠️ **KHÔNG** lấy định dạng ngày / giá trị enum từ mock (Team Plan §0 — mock cố ý đóng băng ở bản cũ). Cột "Giá trị (đã chuẩn hóa D1–D7)" dưới đây là giá trị **sau** khi chuyển đổi ở bước sinh Flyway seed.

**Trường học** (`schools`):

| Fixture ID | Type | Giá trị (đã chuẩn hóa D1–D7) | Mục đích |
|---|---|---|---|
| `FX-001` | Flyway seed | `code="SCH-HN-0042"`, name `"TH Đoàn Thị Điểm"`, `status=ACTIVE`, `coordinateLocked=true`, `taxId="0101245678"`, `lat=21.031200`, `lng=105.763800`, `registeredAt="2026-01-05T00:00:00Z"` | Trường chính — happy path, chủ thể của JWT School Admin |
| `FX-002` | Flyway seed | `code="SCH-HN-0051"`, `status=ACTIVE`, `coordinateLocked=true`, `taxId="0102387654"` | **Trường thứ hai** — cross-school isolation (`SEC-002`) + BR-191 per-school (`INT-002`) |
| `FX-003` | Flyway seed | `code="SCH-HN-0063"`, `status=PENDING`, `coordinateLocked=false`, `taxId="0109988776"`, `lat=20.995500`, `lng=105.868300` | Trường **chưa ACTIVE**, tọa độ **chưa khóa** → test BR-028 lock lifecycle + BR-192 (chưa có AY Active) |
| `FX-004` | Flyway seed | `code="SCH-HCM-0107"`, `status=ACTIVE`, `taxId="0312456789"` | Trường thứ ba — phân trang (`total=4`) + ma trận RBAC "All" của System Admin |

**Năm học** (`academic_years` — **có `school_id`**, khác mock: CF-M02-3 / L4):

| Fixture ID | Type | Giá trị | Mục đích |
|---|---|---|---|
| `FX-005` | Flyway seed | `code="AY-2025"`, `schoolId=FX-001`, name `"Năm học 2025–2026"`, `startDate="2025-09-05"`, `endDate="2026-05-31"`, `status=ACTIVE` | Năm ACTIVE của trường A — nền cho BR-192, BR-193 |
| `FX-006` | Flyway seed | `code="AY-2026"`, `schoolId=FX-001`, `startDate="2026-09-05"`, `endDate="2027-05-31"`, `status=DRAFT` | Năm DRAFT song song ACTIVE — **BR-191 cho phép**; đích của `activate` |
| `FX-007` | Flyway seed | `code="AY-2024"`, `schoolId=FX-001`, `startDate="2024-09-05"`, `endDate="2025-05-31"`, `status=CLOSED` | Năm CLOSED — test transition không hợp lệ (CLOSED→ACTIVE) |
| `FX-008` | Flyway seed | `code="AY-2023"`, `schoolId=FX-001`, `status=ARCHIVED` | ⚠️ **Không có trong mock** — tổng hợp mới để test **BR-195** (`INT-016`) |
| `FX-009` | Flyway seed | `code="AY-2025-B"`, `schoolId=FX-002`, `status=ACTIVE` | Năm ACTIVE của **trường B** — bắt buộc cho `INT-002` (BR-191 per-school) |
| `FX-010` | Flyway seed | `code="AY-2026-B"`, `schoolId=FX-002`, `status=DRAFT` | Năm DRAFT của trường B — đích `activate` ở `INT-002` |

**Lớp học** (`classrooms` — `code` = tên lớp, D1/L2; đều thuộc `FX-005`, `schoolId=FX-001`):

| Fixture ID | Type | Giá trị | Mục đích |
|---|---|---|---|
| `FX-011` | Flyway seed | `code="3A"`, `grade=3`, `capacity=45`, `students=38`, `homeroomTeacher=FX-020` (Nguyễn Thu Trang), `readOnly=false` | Lớp chuẩn — happy path, đích của reassign GVCN |
| `FX-012` | Flyway seed | `code="3B"`, `grade=3`, `capacity=45`, `students=40`, `homeroomTeacher=FX-021` (Phan Thị Hồng) | Lớp thứ hai cùng khối — đích của test BR-020 (gán GV đang chủ nhiệm lớp khác) |
| `FX-013` | Flyway seed | `code="4A"`, `grade=4`, `capacity=50`, `students=42`, `homeroomTeacher=FX-022` (**Đào Văn Quân**) | GV **đang** chủ nhiệm — nguồn xung đột BR-020 (`TC-005`) |
| `FX-014` | Flyway seed | `code="5A"`, `grade=5`, `capacity=50`, `students=45` · `code="5B"`, `grade=5`, `capacity=50`, `students=44` | **Khối 5** — nguồn cho test promote "cả khối 5 lên khối 6" (`INT-007`) |
| `FX-015` | Flyway seed | `code="1A","1B","2A","2B","4B"` — `capacity` 45/45/45/45/50 | Đủ **10 lớp** cho phân trang (`pageSize=8` → `pageCount=2`) và `copyStructure` (`INT-006`) |
| `FX-016` | Flyway seed | `code="6A"`, `schoolId=FX-001`, `academicYear=FX-006` (AY-2026 DRAFT), `grade=6`, `capacity=50`, `students=0` | Lớp **đích** của promote (`INT-007`) |
| `FX-017` | Flyway seed | `code="6B"`, `academicYear=FX-006`, `grade=6`, `capacity=45`, `students=44` | Lớp đích **gần đầy** → test vượt sức chứa khi promote (`INT-008`) |
| `FX-018` | Flyway seed | `code="9Z"`, `schoolId=FX-002`, `academicYear=FX-009` | Lớp của **trường B** — đích của cross-school IDOR (`SEC-002`) |

**Nhân sự & cấu hình:**

| Fixture ID | Type | Giá trị | Mục đích |
|---|---|---|---|
| `FX-020` | Flyway seed | `STF-001` "Nguyễn Thu Trang", `role=Teacher`, `status=ACTIVE`, school `FX-001` | GVCN hiện tại của 3A |
| `FX-021` | Flyway seed | `STF-002` "Phan Thị Hồng", `role=Teacher`, `ACTIVE` | GVCN hiện tại của 3B |
| `FX-022` | Flyway seed | `STF-003` "Đào Văn Quân", `role=Teacher`, `ACTIVE` — ⚠️ **cùng người** với `UserAccount` `U-0010` (L3) | Xung đột BR-020 + test hai không gian mã (`TC-005b`) |
| `FX-023` | Flyway seed | `STF-004` "Lưu Thị Bích", `role=Teacher`, `ACTIVE`, **chưa** chủ nhiệm lớp nào | GV **rảnh** — happy path reassign (`TC-006`) |
| `FX-024` | Flyway seed | `STF-006` "Ngô Thị Hà", `role=Medical Staff`, `ACTIVE` | Gán **sai role** làm GVCN → phải bị từ chối |
| `FX-025` | Flyway seed | `STF-009` "Hà Thị Lan", `role=Teacher`, `status=PENDING` | GV **chưa active** → phải bị từ chối |
| `FX-026` | Flyway seed | `school_config` cho `FX-001`: `geofenceRadius=200`, `costCeiling=30000`, `reportEmails=["toanpq@dtd.edu.vn","hant@dtd.edu.vn"]` | Baseline BR-028 geofence + `INT-018` |

**JWT / môi trường:**

| Fixture ID | Type | Giá trị | Mục đích |
|---|---|---|---|
| `FX-030` | JWT | `{sub:"U-0001", role:"system-admin", schoolId:null}` | System Admin — quyền "All" |
| `FX-031` | JWT | `{sub:"U-0002", role:"school-admin", schoolId:"SCH-HN-0042"}` | School Admin **trường A** — actor chính |
| `FX-032` | JWT | `{sub:"U-0003", role:"school-admin", schoolId:"SCH-HN-0051"}` | School Admin **trường B** — cross-school |
| `FX-033` | JWT | `{sub:"U-0004", role:"teacher", schoolId:"SCH-HN-0042"}` | Teacher — chỉ xem policy (UC-137) |
| `FX-034` | JWT | `{sub:"U-0005", role:"medical-staff", schoolId:"SCH-HN-0042"}` | Medical Staff — UC-137 |
| `FX-035` | JWT | `{sub:"U-0006", role:"service-caterers", schoolId:"SCH-HN-0042"}` | Service Caterers — UC-137 |
| `FX-036` | JWT | `{sub:"U-0007", role:"supplier", schoolId:null}` | Supplier — **mọi endpoint MF02 = 403** |
| `FX-037` | JWT | `{sub:"U-0008", role:"parent", schoolId:"SCH-HN-0042"}` | Parent — **mọi endpoint MF02 = 403** |
| `FX-038` | JWT | *(không gửi header `Authorization`)* | Guest / anonymous — chỉ `POST /schools/register` |
| `FX-039` | JWT | token hết hạn (`exp` quá khứ) + token sai chữ ký + token rỗng | `SEC-003` (401 MSG-004) |
| `FX-040` | env | `spring.profiles.active=test`, Testcontainers `postgres:16-alpine`, `spring.flyway.locations=classpath:db/migration` | Chạy migration thật (`V1__init_schema.sql`) → CHECK/unique index là **thật**, không mock |

> **Enum dùng trong fixture — theo D6 (`common/enums/`), SCREAMING_SNAKE:** `SchoolStatus` = `PENDING|ACTIVE|INACTIVE` · `AcademicYearStatus` = `DRAFT|ACTIVE|CLOSED|ARCHIVED` · `RoleKey` = `system-admin|school-admin|teacher|parent|medical-staff|service-caterers|supplier` (**kebab-case** — D6 chốt vì đồng thời là URL segment). MF02 **không** tự khai lại enum nào của D6.

**Props Isolation Boilerplate (CASE 2.0 — BẮT BUỘC)**

> ⭐ Mỗi test PHẢI tạo fresh instance qua factory — **không** shared mutable state giữa các test case. Đây là biện pháp chống **AP-AI-002** (Green-from-Birth). Bản Java thay cho ví dụ TypeScript của template.

```java
// ═══════════════════════════════════════════════════════════════════
// CASE 2.0 — Props Isolation Pattern (Java)
// Đặt trong src/test/java/com/sfms/mf02/fixture/Mf02Fixtures.java
// Mỗi @Test gọi factory → không @BeforeAll với entity mutable dùng chung.
// ═══════════════════════════════════════════════════════════════════
public final class Mf02Fixtures {

  /** Baseline hợp lệ — đồng bộ FX-011 (§3 TDS-05). Mọi override qua Consumer. */
  public static Classroom classroom(Consumer<Classroom> overrides) {
    Classroom c = new Classroom();
    c.setCode("3A");                 // D1: code = mã nghiệp vụ = tên lớp (L2)
    c.setSchoolId(SCHOOL_A_ID);
    c.setAcademicYearId(AY_2025_ID);
    c.setGrade(3);                   // BR-024
    c.setCapacity(45);               // BR-025
    c.setReadOnly(false);            // BR-193
    overrides.accept(c);
    return c;
  }

  public static AcademicYear academicYear(Consumer<AcademicYear> overrides) {
    AcademicYear ay = new AcademicYear();
    ay.setCode("AY-2026");
    ay.setSchoolId(SCHOOL_A_ID);
    ay.setName("Năm học 2026–2027");
    ay.setStartDate(LocalDate.parse("2026-09-05"));   // D4: ISO-8601
    ay.setEndDate(LocalDate.parse("2027-05-31"));
    ay.setStatus(AcademicYearStatus.DRAFT);           // D6: SCREAMING_SNAKE
    overrides.accept(ay);
    return ay;
  }

  public static School school(Consumer<School> overrides) {
    School s = new School();
    s.setCode("SCH-HN-0042");
    s.setName("TH Đoàn Thị Điểm");
    s.setTaxId("0101245678");
    s.setEmail("contact@dtd.edu.vn");
    s.setLat(new BigDecimal("21.031200"));            // BR-028
    s.setLng(new BigDecimal("105.763800"));
    s.setStatus(SchoolStatus.ACTIVE);
    s.setCoordinateLocked(true);                      // BR-028
    overrides.accept(s);
    return s;
  }

  /** AuthUser stub — schoolId/role LUÔN từ JWT, không từ body (TDS §17.1 C2). */
  public static AuthUser actor(String userId, RoleKey role, String schoolCode) {
    return new AuthUser(userId, role, schoolCode);
  }

  private Mf02Fixtures() {}
}
```

---

## 4. Test Case Specification

> **TC ID format:** `MF02-TC-[NNN]` (Unit) · `MF02-TC-INT-[NNN]` (Integration) · `MF02-TC-SEC-[NNN]` (Security)
> **Severity:** CRITICAL / HIGH / MEDIUM / LOW
> **Status:** 🔴 Not written · 🟡 Written-failing · 🟢 Passing
> **Oracle Source:** ⭐ **bắt buộc** — expected value đến từ đâu (BR/UC/MSG/D1–D7). Không dùng giả định AI. Ghi ⛔ nếu thiếu oracle.
> **Expected message:** nguyên văn **tiếng Anh** từ `error_message.md` v1.5 (**D7**). **Expected envelope:** `{"error":{"code","message","details"}}` (**D2**), `details` là mảng — **không bao giờ `null`**.

### 4.1. UNIT TEST CASES — service layer (Mockito, không DB)

---

#### `MF02-TC-001` — BR-024: cấp lớp phải ∈ [1,12]

**Severity:** `HIGH`
**Feature Under Test:** `ClassroomService.create()` · `ClassroomService.update()`
**Test File:** `src/test/java/com/sfms/mf02/service/ClassroomServiceValidationTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-001`
**Oracle Source:** **BR-024** nguyên văn — *"Cấp lớp học phải nằm trong khoảng **1 đến 12** (tương ứng với hệ thống giáo dục phổ thông Việt Nam)"* · mã lỗi **SCH-024** / **MSG-109** (TDS §10) · **UC-018**, **UC-090** (traceability §1.2)
**Technique:** Boundary Value Analysis + Equivalence Partitioning

**Preconditions:**
- `FX-001` (trường A ACTIVE), `FX-005` (AY-2025 ACTIVE) — để không bị BR-192 chặn trước
- Mock `ClassroomRepository.existsByAcademicYearIdAndCodeIgnoreCase()` → `false`

**Test Steps:**
1. **Arrange:** `Mf02Fixtures.classroom(c -> c.setGrade(grade))` với `grade` tham số hóa
2. **Act:** gọi `classroomService.create(req, FX-031)`
3. **Assert:** so kết quả với bảng lớp tương đương dưới

| `grade` | Lớp tương đương | Expected |
|---|---|---|
| `0` | dưới dải | ném `ValidationException`, `code="SCH-024"`, HTTP 422 |
| `1` | **biên dưới hợp lệ** | tạo thành công |
| `12` | **biên trên hợp lệ** | tạo thành công |
| `13` | trên dải | ném `ValidationException`, `code="SCH-024"`, HTTP 422 |
| `-1` | âm | ném `ValidationException`, `code="SCH-024"`, HTTP 422 |

**Expected Result (PASS — hành vi đúng):**
- `grade ∈ {1,12}` → `ClassroomResponse` trả về, `grade` giữ nguyên giá trị đã gửi
- `grade ∈ {0,13,-1}` → `ValidationException` với `code="SCH-024"`, `message="Grade level must be between 1 and 12."` (**MSG-109 nguyên văn**), HTTP 422, `details` chứa `{"field":"grade","value":<giá trị đã gửi>}`
- **Không** bản ghi nào được ghi (`verify(classroomRepository, never()).save(any())`)

**Expected Result (FAIL — dấu hiệu lỗi):**
- `grade=13` được chấp nhận → BR-024 chỉ tồn tại ở CHECK constraint DB, service không kiểm → client nhận **500 DataIntegrityViolation** thay vì **422 SCH-024** (sai mã lỗi, FE không map được nhãn)
- `grade=0` bị từ chối nhưng với `code="SCH-010"` → lẫn mã lỗi capacity với grade

**Current Status:** 🔴 Not written
**Implementation Note:** validate ở service **trước** khi `save()` để trả đúng `SCH-024`; CHECK `grade BETWEEN 1 AND 12` (§5.2) là chốt chặn cuối, không phải nơi kiểm chính.

---

#### `MF02-TC-002` — BR-025: sức chứa lớp ≤ 50

**Severity:** `HIGH`
**Feature Under Test:** `ClassroomService.create()` · `ClassroomService.update()`
**Test File:** `src/test/java/com/sfms/mf02/service/ClassroomServiceValidationTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-002`
**Oracle Source:** **BR-025** nguyên văn — *"Mỗi lớp học có sức chứa tối đa **50 học sinh**"* · **MSG-010** (`error_message.md` v1.5) → **SCH-010**, HTTP 409 (TDS §10) · **UC-018**, **UC-090**
**Technique:** Boundary Value Analysis

**Preconditions:** `FX-001`, `FX-005` (AY ACTIVE); lớp mới, `students=0`

**Test Steps:**
1. **Arrange:** `classroom(c -> c.setCapacity(capacity))`
2. **Act:** `classroomService.create(req, FX-031)`
3. **Assert:** theo bảng biên

| `capacity` | Expected |
|---|---|
| `0` | `ValidationException` (`SCH-010`, 409) — CHECK `capacity BETWEEN 1 AND 50` |
| `1` | **biên dưới hợp lệ** → tạo thành công |
| `50` | **biên trên hợp lệ** → tạo thành công |
| `51` | `ConflictException` `SCH-010`, 409 |
| `999` | `ConflictException` `SCH-010`, 409 |

**Expected Result (PASS):**
- `capacity=51` → `ConflictException`, `code="SCH-010"`, `message="Classroom capacity exceeded. Maximum allowed is 50 students."` (**MSG-010 nguyên văn**), HTTP 409, `details=[{"field":"capacity","value":51}]`
- `capacity=50` → tạo thành công, `capacity=50` trong response
- Không `save()` khi từ chối

**Expected Result (FAIL):**
- `capacity=51` được lưu → vi phạm BR-025, lớp vượt sức chứa trần → ảnh hưởng MF09 (phân suất) và MF05 (số suất ăn)
- Trả 422 thay vì 409 → lệch §10 (MSG-010 được TDS ánh xạ 409)

**Current Status:** 🔴 Not written
**Implementation Note:** dùng **cùng một** `code="SCH-010"` cho cả `create` và `update` (§10 gộp trigger); phân biệt ngữ cảnh bằng `details.field`.

---

#### `MF02-TC-003` — Sức chứa mới không được nhỏ hơn sĩ số hiện tại

**Severity:** `MEDIUM`
**Feature Under Test:** `ClassroomService.update()`
**Test File:** `src/test/java/com/sfms/mf02/service/ClassroomServiceValidationTest.java`
**TDD Phase:** 🔴 RED — ✅ **Oracle chốt 2026-07-31 (BR-219, SCH-012/MSG-140)**
**Condition Ref:** `TC-COND-003`
**Oracle Source:** **BR-219** (`business_rules.md` v1.10, mới 2026-07-31) — *"Khi cập nhật sức chứa của một lớp học đã có học sinh, giá trị mới không được nhỏ hơn số học sinh hiện đang học tại lớp đó."* TDS §10 — `SCH-012`/**MSG-140** (`error_message.md` v1.7), tách khỏi `SCH-010` theo D7.

**Preconditions:** `FX-011` (lớp 3A, `capacity=45`, `students=38`)

**Test Steps:**
1. **Arrange:** mock `studentCountPort.countByClassroom("3A")` → `38`
2. **Act:** `classroomService.update("3A", req(capacity=30), FX-031)`
3. **Assert:** hành vi theo nhánh được chốt

**Expected Result (PASS):**
- `ConflictException`, HTTP 409, bản ghi **không** đổi (`verify(repo, never()).save(any())`)
- `error.code = "SCH-012"`, `error.message` = **MSG-140 nguyên văn** (*"Classroom capacity cannot be reduced below the current number of enrolled students."*) — **không** dùng MSG-010 (mô tả sai tình huống, vi phạm D7 nếu dán vào)

**Expected Result (FAIL):**
- Hạ `capacity=30` khi đang có 38 HS được chấp nhận **và** hệ thống downstream (MF09 phân suất theo lớp) vẫn tính 38 suất → dữ liệu tự mâu thuẫn (sĩ số > sức chứa) mà không có cảnh báo nào

**Current Status:** 🔴 Not written — ✅ đã mở khóa, sẵn sàng implement
**Implementation Note:** nếu chọn (A), sĩ số phải đọc qua **port theo ID** (`studentCountPort`), **không** import entity MF03 (TDS §17.1 C6 / Team Plan §4b).

---

#### `MF02-TC-004` — BR-192: không tạo lớp khi trường chưa có năm học ACTIVE

**Severity:** `HIGH`
**Feature Under Test:** `ClassroomService.create()`
**Test File:** `src/test/java/com/sfms/mf02/service/ClassroomServiceValidationTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-004`
**Oracle Source:** **BR-192** nguyên văn — *"Mỗi `Classroom` phải thuộc về **đúng 1** `AcademicYear`. **Không thể tạo `Classroom` mới khi trường chưa có năm học ở trạng thái Active**"* · **MSG-081** → **SCH-081**, HTTP 409 (TDS §10) · **UC-018** (traceability §1.2 bổ sung BR-192 + MSG-081 so với UC file — **CF-M02-4**)

**Preconditions:** `FX-003` (trường `SCH-HN-0063`, `status=PENDING`, **không** có AY nào ACTIVE); JWT School Admin của trường đó

**Test Steps:**
1. **Arrange:** mock `academicYearRepository.findBySchoolIdAndStatus(SCH-HN-0063, ACTIVE)` → `List.of()`
2. **Act:** `classroomService.create(req(code="1A", grade=1, capacity=40), actor)`
3. **Assert:** exception + không ghi

**Expected Result (PASS):**
- `ConflictException`, `code="SCH-081"`, HTTP 409
- `message="Cannot create classroom: this school has no Active academic year. Please activate an academic year first."` (**MSG-081 nguyên văn**)
- `verify(classroomRepository, never()).save(any())`
- **Thứ tự kiểm:** BR-192 được kiểm **trước** BR-024/BR-025 — gửi đồng thời `grade=13` + không có AY Active vẫn trả `SCH-081` (lỗi chặn cứng ở mức ngữ cảnh trường, không phải lỗi field)

**Expected Result (FAIL):**
- Lớp được tạo với `academicYearId` trỏ năm **DRAFT** → BR-192 bị phá: lớp thuộc năm chưa vận hành, MF03 gán HS vào lớp "không tồn tại về mặt nghiệp vụ"
- Trả 400/422 thay vì 409 SCH-081

**Current Status:** 🔴 Not written
**Implementation Note:** năm học đích **không** được lấy từ `req.academicYearId` do client gửi mà phải là năm **ACTIVE của `actor.schoolId`** — nếu tin body, client gửi năm DRAFT là lách được BR-192 (xem `MF02-TC-SEC-004`).

---

#### `MF02-TC-005` — BR-020: giáo viên đã chủ nhiệm lớp khác trong cùng năm học

**Severity:** `HIGH`
**Feature Under Test:** `ClassroomService.assignHomeroomTeacher()`
**Test File:** `src/test/java/com/sfms/mf02/service/HomeroomTeacherAssignmentTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-005`
**Oracle Source:** **BR-020** nguyên văn — *"Mỗi lớp học chỉ được phép có **một giáo viên chủ nhiệm** tại một thời điểm"* · **MSG-011** → **SCH-011**, HTTP 409 (TDS §10) · **UC-008** · bất biến DB `uq_homeroom_teacher_per_year` (§5.2)
**Technique:** Decision Table Testing

**Preconditions:** `FX-013` (4A đã có GVCN `FX-022` = Đào Văn Quân), `FX-012` (3B), cùng `FX-005` (AY-2025 ACTIVE)

**Test Steps (bảng quyết định — 5 điều kiện):**

| # | GV tồn tại | role=Teacher | cùng trường | đang CN lớp khác | lớp read-only | Expected |
|---|---|---|---|---|---|---|
| a | ✅ | ✅ | ✅ | ❌ | ❌ | **200** — gán thành công (MSG-034) |
| b | ✅ | ✅ | ✅ | **✅** | ❌ | **409 SCH-011** (BR-020) |
| c | ✅ | **❌** (`FX-024` Medical Staff) | ✅ | ❌ | ❌ | **422** — role không hợp lệ, `details.field="teacherId"` |
| d | ✅ | ✅ (nhưng `status=PENDING`, `FX-025`) | ✅ | ❌ | ❌ | **422** — GV chưa ACTIVE |
| e | ✅ | ✅ | **❌** (GV trường B) | ❌ | ❌ | **403 SCH-005** hoặc **404 SCH-404** (BR-022 — xem L7) |
| f | **❌** (`STF-999`) | — | — | — | — | **404 SCH-404** (MSG-137) |
| g | ✅ | ✅ | ✅ | ❌ | **✅** (năm CLOSED) | **409** — lớp read-only (BR-193) |

**Expected Result (PASS):**
- Dòng **b**: `ConflictException`, `code="SCH-011"`, `message="Homeroom teacher is already assigned to another classroom."` (**MSG-011 nguyên văn**), HTTP 409; **cả hai** lớp giữ nguyên GVCN cũ
- Dòng **a**: HTTP 200, response chứa `teacherId` đã gán; audit được ghi (xem `MF02-TC-006`)
- Dòng **c/d**: `code` = mã validation, `details` nêu rõ `field="teacherId"` — **không** dùng `SCH-011` (khác nguyên nhân)

**Expected Result (FAIL):**
- Dòng **b** thành công → một giáo viên chủ nhiệm 2 lớp → vi phạm BR-020; nếu DB có `uq_homeroom_teacher_per_year` thì client nhận **500** thay vì 409 (sai mã lỗi); nếu index bị bỏ thì dữ liệu **hỏng thật**
- Dòng **c** thành công → Medical Staff thành GVCN → phá RBAC BR-007

**Current Status:** 🔴 Not written
**Implementation Note:** kiểm `existsByAcademicYearIdAndHomeroomTeacherId()` (§8.2) **trong cùng transaction** với `save()`; scope kiểm là **năm học**, không phải toàn thời gian — GV chủ nhiệm 4A năm 2025 vẫn được chủ nhiệm 3B năm 2026.

---

#### `MF02-TC-005b` — BR-020 không bị lách qua hai không gian định danh giáo viên

**Severity:** `CRITICAL`
**Feature Under Test:** `ClassroomService.assignHomeroomTeacher()` — tầng resolve định danh
**Test File:** `src/test/java/com/sfms/mf02/service/HomeroomTeacherAssignmentTest.java`
**TDD Phase:** 🔴 RED — ✅ **Oracle chốt 2026-07-31 (ADR-M02-5)**
**Condition Ref:** `TC-COND-005`
**Oracle Source:** **BR-020** (bất biến "một GVCN mỗi lớp" — phải đúng theo **con người**, không theo mã) · **ADR-M02-5 (chốt 2026-07-31):** `homeroom_teacher_id` → `user_accounts.id`; `code` (`U-0010`) và `staff_code` (`STF-003`) là 2 cột trên **cùng một dòng** `user_accounts` — **cùng một người "Đào Văn Quân"**, cùng một UUID.

**Preconditions:** `FX-022` — một dòng `user_accounts` có cả `code=U-0010` và `staff_code=STF-003` (cùng 1 UUID, ADR-M02-5); `FX-012` (3B) và một lớp khác cùng năm `FX-005` đều chưa có GVCN

**Test Steps (mô phỏng lách luật):**
1. **Act 1:** `PATCH /api/v1/classrooms/3B/homeroom-teacher` với `{"teacherId":"STF-003"}` → kỳ vọng **200**
2. **Act 2:** `PATCH /api/v1/classrooms/4B/homeroom-teacher` với `{"teacherId":"U-0010"}` — **cùng người, mã khác**
3. **Assert:** bước 2 **phải** bị từ chối

**Expected Result (PASS = bất biến giữ được):**
- Bước 2 → **409 `SCH-011`** với **MSG-011 nguyên văn** — hệ thống resolve `U-0010` và `STF-003` về **cùng một chủ thể** (cùng `user_accounts.id`) trước khi kiểm bất biến.
- Assert DB: `SELECT COUNT(*) FROM classrooms WHERE academic_year_id=<AY-2025> AND homeroom_teacher_id=<uuid của Đào Văn Quân>` → **= 1**

**Expected Result (FAIL = lỗ hổng tồn tại):**
- **Cả hai** bước trả 200 → một giáo viên chủ nhiệm 2 lớp trong cùng năm học mà `uq_homeroom_teacher_per_year` **không bắt được**, vì hai mã map sang hai giá trị `homeroom_teacher_id` khác nhau. BR-020 bị vô hiệu hóa **hoàn toàn** ở tầng dữ liệu.

**Current Status:** 🔴 Not written — ✅ đã mở khóa, sẵn sàng implement

---

#### `MF02-TC-006` — BR-020 + BR-026: gán GVCN mới tự hủy liên kết cũ và ghi nhật ký

**Severity:** `HIGH`
**Feature Under Test:** `ClassroomService.assignHomeroomTeacher()`
**Test File:** `src/test/java/com/sfms/mf02/service/HomeroomTeacherAssignmentTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-006`
**Oracle Source:** **BR-020** — *"Khi gán giáo viên mới, hệ thống **tự động hủy liên kết giáo viên cũ** và **ghi nhật ký thay đổi**"* · **BR-026** — *"Mỗi lần phân công hoặc thay thế giáo viên chủ nhiệm đều phải được ghi vào **audit trail** kèm **ngày giờ** và **thông tin thực hiện**"* · **MSG-034** (success) · **UC-008** · TDS §7.1 event `HomeroomTeacherAssigned`

**Preconditions:** `FX-011` (3A, GVCN hiện tại = `FX-020` Nguyễn Thu Trang) · `FX-023` (`STF-004` Lưu Thị Bích, Teacher ACTIVE, **chưa** chủ nhiệm lớp nào)

**Test Steps:**
1. **Arrange:** 3A có `homeroomTeacherId = FX-020`
2. **Act:** `assignHomeroomTeacher("3A", FX-023, actor=FX-031)`
3. **Assert:** trạng thái sau + audit + event

**Expected Result (PASS):**
- `3A.homeroomTeacherId == FX-023` (Lưu Thị Bích)
- Liên kết cũ **bị hủy**: không còn bản ghi nào trong năm `AY-2025` gắn `FX-020` với 3A → `FX-020` **rảnh**, gán được cho lớp khác ngay sau đó (assert bằng lần gọi thứ hai trả 200)
- **Toàn bộ trong 1 transaction:** nếu ghi audit lỗi → rollback cả việc gán (assert bằng cách stub `auditPort.write()` ném exception → `3A.homeroomTeacherId` vẫn là `FX-020`)
- **Audit trail (BR-026)** có đúng 1 bản ghi mới với: `actor` = `FX-031.userId` (**từ JWT**, không từ body), `timestamp` ≠ null kiểu `Instant` (**D4**), `action` chứa cả GV cũ và GV mới (đối chiếu khuôn seed mock `"ASSIGN Homeroom ..."`), `resource="School Management"`, `result="SUCCESS"`
- Event `HomeroomTeacherAssigned` được publish (TDS §7.1) — assert qua `ApplicationEvents`
- Response 200, message ánh xạ **MSG-034** *"Homeroom teacher assigned successfully."*

**Expected Result (FAIL):**
- GV cũ vẫn còn liên kết → 3A có **2 GVCN** → vi phạm BR-020 câu 1
- Gán thành công nhưng **không** có bản ghi audit → vi phạm BR-026; mất dấu ai đổi GVCN lớp nào (đây là dữ liệu dùng khi điều tra sự cố ATTP theo trục MF11)
- `actor` trong audit lấy từ request body → giả mạo được người thực hiện

**Current Status:** 🔴 Not written
**Implementation Note:** một `@Transactional`; audit ghi qua `auditPort` (không JOIN entity MF12).

---

#### `MF02-TC-007` — BR-028: tọa độ GPS phải trong dải hợp lệ

**Severity:** `HIGH`
**Feature Under Test:** `SchoolService.register()` · `SchoolService.update()`
**Test File:** `src/test/java/com/sfms/mf02/service/SchoolCoordinateTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-007`
**Oracle Source:** **BR-028** nguyên văn — *"**latitude** trong khoảng **-90.0 đến 90.0**, **longitude** trong khoảng **-180.0 đến 180.0**. Hệ thống **từ chối lưu** nếu tọa độ nằm ngoài phạm vi trên"* · **MSG-107** → **SCH-013**, HTTP **400** (TDS §10) · **UC-070**, **UC-095** · CHECK `lat BETWEEN -90 AND 90`, `lng BETWEEN -180 AND 180` (§5.2)
**Technique:** Boundary Value Analysis

**Preconditions:** không cần seed (validate thuần) — `precision=9, scale=6` nên biên vi phạm nhỏ nhất là `±0.000001`

**Test Steps:** tham số hóa theo bảng:

| `lat` | `lng` | Expected |
|---|---|---|
| `-90.000000` | `0` | ✅ hợp lệ (biên) |
| `90.000000` | `0` | ✅ hợp lệ (biên) |
| `-90.000001` | `0` | ❌ 400 `SCH-013` / MSG-107 |
| `90.000001` | `0` | ❌ 400 `SCH-013` / MSG-107 |
| `0` | `-180.000000` | ✅ hợp lệ (biên) |
| `0` | `180.000000` | ✅ hợp lệ (biên) |
| `0` | `-180.000001` | ❌ 400 `SCH-013` / MSG-107 |
| `0` | `180.000001` | ❌ 400 `SCH-013` / MSG-107 |
| `21.031200` | `105.763800` | ✅ hợp lệ (`FX-001` — Hà Nội) |
| `null` | `105.7` | ❌ 400 — `lat NOT NULL` (§5.2) |

**Expected Result (PASS):**
- Ngoài dải → `ValidationException`, HTTP **400**, `code="SCH-013"`, `message="Invalid GPS coordinates. Latitude must be between -90 and 90, longitude between -180 and 180."` (**MSG-107 nguyên văn**), `details` nêu rõ field vi phạm (`lat` hay `lng`) + giá trị đã gửi
- Trong dải → lưu **nguyên giá trị**, không làm tròn mất chữ số (assert `BigDecimal.compareTo` == 0, `scale=6` giữ nguyên)
- **Phân biệt với `MF02-TC-009`:** cùng `code="SCH-013"` nhưng **status 400** và **message MSG-107** — đây là cách test phân biệt hai nghĩa của SCH-013 (**L6**)

**Expected Result (FAIL):**
- `lat=91` được lưu → geofence của MF06/07/08 tính từ điểm sai → xác minh vị trí giao hàng/kiểm thực sai hoàn toàn (BR-028 nói rõ tọa độ này là **mốc tham chiếu trung tâm** cho 3 module đó)
- Trả 422 thay vì 400 → lệch §10

**Current Status:** 🔴 Not written
**Implementation Note:** validate bằng `@DecimalMin/@DecimalMax` trên DTO **và** kiểm lại ở service (DTO có thể bị bỏ qua khi gọi service nội bộ); CHECK ở DB là lớp thứ ba.

---

#### `MF02-TC-008` — BR-028 / UC-070: mã số thuế sai định dạng

**Severity:** `MEDIUM`
**Feature Under Test:** `SchoolService.register()`
**Test File:** `src/test/java/com/sfms/mf02/service/SchoolRegistrationTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-008`
**Oracle Source:** **MSG-007** nguyên văn — *"Invalid tax ID format. Please verify and try again."* → **SCH-007**, HTTP 400 (TDS §10) · **UC-070** (traceability §1.2: BR-028, MSG-007 + MSG-032)

**Preconditions:** không cần seed

**Test Steps:** tham số hóa `taxId` — khuôn hợp lệ suy từ dữ liệu thật `mock-data.ts` (**10 chữ số**, hoặc 13 số dạng `xxxxxxxxxx-xxx` cho đơn vị trực thuộc):

| `taxId` | Expected |
|---|---|
| `"0101245678"` (`FX-001`) | ✅ hợp lệ |
| `"0109988776"` (`FX-003`) | ✅ hợp lệ |
| `"010124567"` (9 số) | ❌ 400 `SCH-007` |
| `"01012456789"` (11 số) | ❌ 400 `SCH-007` |
| `"0101-24567"` | ❌ 400 `SCH-007` |
| `"abc1234567"` | ❌ 400 `SCH-007` |
| `""` / `null` | ❌ 400 — `tax_id NOT NULL` (§5.2) |

**Expected Result (PASS):**
- Sai định dạng → HTTP 400, `code="SCH-007"`, `message="Invalid tax ID format. Please verify and try again."` (**MSG-007 nguyên văn**), `details=[{"field":"taxId", ...}]`
- Hợp lệ → trường được tạo với `status=PENDING` (**§6.4** state machine + **MSG-032** *"School registered successfully. Pending System Admin approval."*), `coordinateLocked=false`

**Expected Result (FAIL):**
- Chấp nhận `"abc1234567"` → dữ liệu định danh pháp lý của trường không dùng được cho báo cáo cơ quan chức năng
- ⚠️ **Khác** trường hợp `taxId` **đúng định dạng nhưng đã tồn tại** → xem `MF02-TC-INT-015` (**✅ L9 đã chốt 2026-07-31 — `SCH-409`/MSG-139**) — hai tình huống này **không** dùng chung `SCH-007`

**Current Status:** 🔴 Not written
**Implementation Note:** khuôn định dạng phải ghi vào §9.2 dưới dạng regex tường minh — hiện TDS chỉ nói "sai định dạng" mà không nêu khuôn; test này chốt khuôn theo dữ liệu thật của 4 trường trong seed.

---

#### `MF02-TC-009` — BR-028: tọa độ đã khóa → từ chối cập nhật

**Severity:** `HIGH`
**Feature Under Test:** `SchoolService.update()`
**Test File:** `src/test/java/com/sfms/mf02/service/SchoolCoordinateTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-009`
**Oracle Source:** **BR-028** nguyên văn — *"Khi `coordinateLocked = true`, mọi yêu cầu cập nhật tọa độ bị **từ chối** — các thông tin khác (địa chỉ, điện thoại, mô tả) **vẫn được phép** cập nhật bình thường"* · **MSG-108** → **SCH-013**, HTTP **403** (TDS §10) · **UC-095** · ADR-M02-3

**Preconditions:** `FX-001` (`SCH-HN-0042`, `status=ACTIVE`, `coordinateLocked=true`)

**Test Steps:**

| # | Payload `PATCH /schools/SCH-HN-0042` | Actor | Expected |
|---|---|---|---|
| a | `{"lat":21.5}` | School Admin (`FX-031`) | ❌ **403 SCH-013** / MSG-108 |
| b | `{"lng":105.9}` | School Admin | ❌ **403 SCH-013** / MSG-108 |
| c | `{"address":"Địa chỉ mới, Hà Nội"}` | System Admin (`FX-030`) | ✅ **200** — BR-028 cho phép sửa field khác |
| d | `{"emergencyContact":"Nguyễn A — 0900 000 000","operatingHours":"07:00 – 17:00"}` | System Admin | ✅ **200** |
| e | `{"lat":21.5}` | System Admin **chưa** unlock | ❌ **403 SCH-013** / MSG-108 — *khóa là khóa*, System Admin phải đi qua `/coordinate/unlock` |
| f | `{"lat":21.031200}` (**đúng bằng giá trị hiện tại**) | School Admin | ✅ **200** — không phải "thay đổi tọa độ" (no-op), không được từ chối oan |

**Expected Result (PASS):**
- Dòng a/b/e → HTTP **403**, `code="SCH-013"`, `message="School coordinates are locked. Only a System Administrator can change them."` (**MSG-108 nguyên văn**); `lat`/`lng` trong DB **không đổi**
- Dòng c/d → HTTP 200, field tương ứng đổi, `lat`/`lng`/`coordinateLocked` **không đổi**, message ánh xạ **MSG-029** *"Profile updated successfully."*
- **Phân biệt với `MF02-TC-007`:** cùng `SCH-013` nhưng **403 + MSG-108** (khóa) ≠ **400 + MSG-107** (ngoài dải) — **L6**

**Expected Result (FAIL):**
- Dòng c bị từ chối → BR-028 bị hiểu quá rộng: khóa tọa độ chặn luôn cả sửa địa chỉ → School Admin không cập nhật được hồ sơ trường
- Dòng a thành công → tọa độ đổi âm thầm sau khi trường đã ACTIVE → mọi geofence check của MF06/07/08 lệch mà không ai biết
- Dòng f bị từ chối → payload PATCH đầy đủ (FE gửi cả object) luôn bị 403, màn sửa hồ sơ trường **không dùng được**

**Current Status:** 🔴 Not written
**Implementation Note:** so sánh `lat`/`lng` bằng `BigDecimal.compareTo` (không `equals` — `21.0312` vs `21.031200` khác `scale` nhưng cùng giá trị) để dòng f không bị từ chối oan.

---

#### `MF02-TC-010` — BR-028: unlock tọa độ bắt buộc có lý do văn bản

**Severity:** `HIGH`
**Feature Under Test:** `SchoolService.unlockCoordinate()`
**Test File:** `src/test/java/com/sfms/mf02/service/SchoolCoordinateTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-010`
**Oracle Source:** **BR-028** nguyên văn — *"Chỉ **System Admin** mới có quyền đặt lại `coordinateLocked = false`... Thao tác này **bắt buộc phải kèm lý do bằng văn bản** và được ghi vào **audit log** (người thực hiện, thời gian, lý do)"* · **MSG-138** (*"Validation failed: one or more required fields are missing or invalid."* — dùng chung mọi module, `error_message.md` v1.5) · TDS §7.1 event `SchoolCoordinateUnlocked`

**Preconditions:** `FX-001` (`coordinateLocked=true`); actor = System Admin `FX-030`

**Test Steps:**

| # | `reason` | Expected |
|---|---|---|
| a | `"hiệu chỉnh sai số GPS ±40m"` (khuôn từ audit thật `AUD-98955`) | ✅ 200, `coordinateLocked=false`, audit ghi lý do |
| b | `""` (rỗng) | ❌ **422 MSG-138** |
| c | `"   "` (chỉ khoảng trắng) | ❌ **422 MSG-138** |
| d | field `reason` **vắng** | ❌ **422 MSG-138** |
| e | `null` | ❌ **422 MSG-138** |

**Expected Result (PASS):**
- Dòng a: `coordinateLocked=false`; **audit log** có bản ghi với **cả ba** thành phần BR-028 đòi: `actor` (từ JWT), `timestamp` (`Instant`, **D4**), và **`reason` nguyên văn** người dùng nhập; event `SchoolCoordinateUnlocked` được publish
- Dòng b–e: HTTP 422, `message="Validation failed: one or more required fields are missing or invalid."` (**MSG-138 nguyên văn**), `details=[{"field":"reason", ...}]`; `coordinateLocked` **vẫn** `true`; **không** audit log nào được ghi
- Sau khi lưu tọa độ mới → `coordinateLocked` **tự động về `true`** (BR-028 câu cuối) — assert ở `MF02-TC-INT-014`

**Expected Result (FAIL):**
- Unlock được với `reason` rỗng → mất khả năng truy vết **vì sao** tọa độ trường bị đổi; BR-028 đặt ra yêu cầu này chính là để có bằng chứng khi tọa độ (mốc geofence) bị thay đổi
- `reason` bị ghi vào audit dưới dạng đã cắt/đổi (VD trim quá tay, cắt 50 ký tự) → không còn "nguyên văn"

**Current Status:** 🔴 Not written
**Implementation Note:** `@NotBlank` (không phải `@NotNull`) để bắt cả dòng c; audit ghi **trong cùng transaction** với đổi cờ.

---

#### `MF02-TC-011` — BR-028: bán kính geofence ∈ [50,500] m, mặc định 200 m

**Severity:** `MEDIUM`
**Feature Under Test:** `SchoolConfigService.update()`
**Test File:** `src/test/java/com/sfms/mf02/service/SchoolConfigServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-011`
**Oracle Source:** **BR-028** nguyên văn — *"Khu vực hợp lệ được tính theo bán kính **200 mét**... School Administration có thể cấu hình bán kính này trong khoảng **50 m – 500 m**... Mọi thay đổi bán kính geofence **phải được ghi vào audit log**"* · CHECK `geofence_radius BETWEEN 50 AND 500` + `DEFAULT 200` (§5.2) · **UC-095**
**Technique:** Boundary Value Analysis

**Preconditions:** `FX-026` (`school_config` của `FX-001`, `geofenceRadius=200`)

**Test Steps:**

| `geofenceRadius` | Expected |
|---|---|
| `49` | ❌ 422 **MSG-138**, `details.field="geofenceRadius"` |
| `50` | ✅ biên dưới hợp lệ |
| `200` | ✅ mặc định |
| `500` | ✅ biên trên hợp lệ |
| `501` | ❌ 422 **MSG-138** |
| `0` / `-1` | ❌ 422 **MSG-138** |
| *(trường mới, chưa có config)* | ✅ đọc ra **200** (DEFAULT) |

**Expected Result (PASS):**
- Trong dải → lưu thành công, audit log ghi thay đổi bán kính (BR-028 câu cuối), message ánh xạ **MSG-029**
- Ngoài dải → 422 **MSG-138 nguyên văn**, giá trị cũ **không đổi**
- Trường chưa có `school_config` → `GET` trả `geofenceRadius=200` (không phải `null`/`0`) — MF06/07/08 phụ thuộc giá trị này, `null` sẽ làm geofence check của họ vỡ
- `costCeiling` trong cùng payload được lưu **nguyên giá trị**, **không** bị MF02 kiểm luật (BR-080 thuộc **MF05** — xem TDS-01 "ngoài phạm vi")

**Expected Result (FAIL):**
- `geofenceRadius=0` được lưu → mọi xác minh vị trí của MF06/07/08 **luôn thất bại** (không điểm nào nằm trong bán kính 0)
- `geofenceRadius=50000` được lưu → geofence mất tác dụng, giao hàng ở bất kỳ đâu cũng "trong khu vực trường"
- Đổi bán kính mà không có audit → vi phạm BR-028

**Current Status:** 🔴 Not written
**Implementation Note:** `school_config` là **bảng một dòng/trường** (`school_id UNIQUE`) — `PATCH` phải upsert, không tạo dòng thứ hai.

---

#### `MF02-TC-012` — BR-191: đúng 1 năm học ACTIVE mỗi trường; cho phép 1 DRAFT song song

**Severity:** `CRITICAL`
**Feature Under Test:** `AcademicYearService.activate()`
**Test File:** `src/test/java/com/sfms/mf02/service/AcademicYearLifecycleTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-012`
**Oracle Source:** **BR-191** nguyên văn — *"Mỗi trường chỉ được có **đúng 1** `AcademicYear` ở trạng thái **Active** tại một thời điểm. Tạo/chuyển một năm học sang trạng thái Active mới **bắt buộc phải đóng (Close) năm học Active hiện tại trước**. **Được phép tồn tại song song một năm học ở trạng thái Draft** trong khi năm học hiện tại vẫn đang Active"* · **MSG-080** → **SCH-080**, HTTP 409 (TDS §10) · **UC-174**, **UC-175** · state machine §6.3
**Technique:** State Transition Testing

**Preconditions:** `FX-005` (AY-2025 **ACTIVE**), `FX-006` (AY-2026 **DRAFT**), `FX-007` (AY-2024 **CLOSED**) — đều thuộc `FX-001`

**Test Steps (ma trận chuyển trạng thái §6.3):**

| # | Từ | Hành động | Expected |
|---|---|---|---|
| a | `AY-2026` DRAFT | `activate()` khi AY-2025 đang ACTIVE | ❌ **409 SCH-080** / MSG-080 |
| b | `AY-2026` DRAFT | `activate()` **sau khi** AY-2025 → CLOSED | ✅ 200, `status=ACTIVE`, MSG-083 |
| c | `AY-2024` CLOSED | `activate()` | ❌ **409** — transition CLOSED→ACTIVE **không** có trong §6.3 |
| d | `AY-2023` ARCHIVED (`FX-008`) | `activate()` | ❌ **409** — BR-195 read-only |
| e | `AY-2025` ACTIVE | `activate()` (đã ACTIVE rồi) | ❌ **409** — idempotent-noop **không** được coi là hợp lệ vì §6.3 không có self-loop |
| f | tồn tại đồng thời | 1 ACTIVE + 1 DRAFT | ✅ **hợp lệ** — BR-191 câu 3, **không** được báo lỗi |
| g | tạo thêm | DRAFT thứ hai khi đã có 1 ACTIVE + 1 DRAFT | ⚠️ BR-191 nói *"**một** năm học ở trạng thái Draft"* → kỳ vọng **409**; **ghi rõ**: không có MSG riêng cho "quá 1 DRAFT" → dùng `SCH-080` là **suy diễn**, cần Huy xác nhận |

**Expected Result (PASS):**
- Dòng a: `ConflictException`, `code="SCH-080"`, `message="Cannot create Active academic year: an Active academic year already exists for this school. Please close it first."` (**MSG-080 nguyên văn**), HTTP 409; **AY-2026 vẫn DRAFT, AY-2025 vẫn ACTIVE** (assert cả hai — TDS §13 TC-INT-001 đòi điều này)
- Dòng b: 200, `status=ACTIVE`; đúng **1** năm ACTIVE của trường; event `AcademicYearActivated` publish với `previousActiveYearId=AY-2025` (TDS §7.3)
- Dòng f: `list()` trả **cả hai** năm, không lỗi
- Dòng g: ghi nhận là **suy diễn**, không tính vào Exit Criteria cho tới khi Huy chốt

**Expected Result (FAIL):**
- Dòng a thành công → 2 năm ACTIVE cùng trường → MF03 không biết gán HS vào năm nào, MF13 thống kê trùng kỳ, MF05 thực đơn treo ở năm sai
- Dòng f **bị** báo lỗi → không chuẩn bị trước được năm học kế tiếp → phá quy trình vận hành thực tế (trường lập năm mới từ tháng 5)
- Dòng c/d thành công → năm đã đóng/lưu trữ bị mở lại → phá nguyên tắc append-only (BR-193/195)

**Current Status:** 🔴 Not written
**Implementation Note:** kiểm **theo `schoolId` của actor** (`WHERE school_id = ? AND status='ACTIVE'`) — **không** kiểm toàn cục như mock (**AP-AI-010**, CF-M02-3, L4).

---

#### `MF02-TC-013` — BR-194: copyStructure sao chép cấu trúc lớp, **không** chuyển học sinh

**Severity:** `HIGH`
**Feature Under Test:** `AcademicYearService.create(copyStructure=true)`
**Test File:** `src/test/java/com/sfms/mf02/service/AcademicYearCreateTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-013`
**Oracle Source:** **BR-194** nguyên văn — *"cho phép **sao chép cấu trúc lớp** (**tên lớp, cấp lớp, sức chứa**) từ năm học liền trước làm mẫu khởi tạo. Hệ thống **không tự động** chuyển học sinh sang lớp mới"* · **MSG-082** (success) · **UC-174**

**Preconditions:** `FX-005` (AY-2025) có **10 lớp** `FX-011`…`FX-015` với `capacity` 45/45/50/50/50/45/45/45/45/50 và 5 lớp có GVCN

**Test Steps:**
1. **Act:** `create(req(code="AY-2027", name="Năm học 2027–2028", startDate="2027-09-05", endDate="2028-05-31", copyStructure=true), FX-031)`
2. **Assert:** cấu trúc lớp mới

**Expected Result (PASS):**
- Năm mới `status=DRAFT` (§6.3: `[*] → DRAFT`), **không** phải ACTIVE
- Tạo **đúng 10** lớp mới gắn `academicYearId` = năm mới
- **Ba** thuộc tính được copy đúng: `code` (tên lớp), `grade`, `capacity` — assert từng lớp, không chỉ đếm
- **`students` của mỗi lớp mới = 0** — BR-194 câu 2; assert qua đếm runtime (ADR-M02-2)
- **`homeroomTeacherId` của mỗi lớp mới = `null`** — BR-194 **chỉ** liệt kê tên/cấp/sức chứa; GVCN **không** thuộc "cấu trúc lớp"
- Lớp của năm cũ **không bị đụng** (append-only §19): 10 lớp cũ vẫn nguyên `students`, GVCN, `readOnly`
- `copyStructure=false` (hoặc vắng) → **0** lớp được tạo
- Response ánh xạ **MSG-082** *"Academic year created successfully."*

**Expected Result (FAIL):**
- HS được chuyển sang lớp năm mới → phá BR-194 câu 2: "lên lớp" mất tính tường minh, HS lưu ban bị đẩy lên lớp trên **tự động** mà không ai xác nhận
- GVCN được copy → nếu sau đó gán GV đó cho lớp khác ở năm mới thì `uq_homeroom_teacher_per_year` báo trùng ở chỗ người dùng không hiểu tại sao
- Năm mới được tạo ở `ACTIVE` → phá BR-191 (trường đang có AY-2025 ACTIVE)

**Current Status:** 🔴 Not written
**Implementation Note:** "năm liền trước" xác định theo `startDate` **giảm dần** trong cùng `schoolId`, không theo `createdAt`.

---

#### `MF02-TC-014` — BR-193: đóng năm học → lớp chuyển read-only, dữ liệu được giữ

**Severity:** `HIGH`
**Feature Under Test:** `AcademicYearService.close()`
**Test File:** `src/test/java/com/sfms/mf02/service/AcademicYearLifecycleTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-014`
**Oracle Source:** **BR-193** nguyên văn — *"Khi đóng (Close) một năm học, toàn bộ `Classroom` thuộc năm học đó chuyển sang trạng thái **chỉ đọc** (**không tạo học sinh mới, không đổi giáo viên chủ nhiệm**), nhưng **toàn bộ dữ liệu lịch sử vẫn được giữ nguyên** — **không xóa dữ liệu**"* · **MSG-083** (success) · **UC-175** · TDS §7.1 event `AcademicYearClosed`

**Preconditions:** `FX-005` (AY-2025 ACTIVE) với 10 lớp, `readOnly=false`

**Test Steps:**
1. **Act:** `close("AY-2025", FX-031)`
2. **Assert:** trạng thái + bất biến "không xóa"

**Expected Result (PASS):**
- `AY-2025.status = CLOSED`
- **Tất cả 10** lớp thuộc AY-2025 có `readOnly = true`
- **Số lượng lớp không đổi = 10** và `students`/`allergies`/`homeroomTeacherId` của từng lớp **giữ nguyên** — BR-193 "không xóa dữ liệu"
- Lớp của **năm khác** (`FX-016` 6A thuộc AY-2026) **không** bị đặt `readOnly` — cascade phải lọc theo `academicYearId`
- Sau khi close: `assignHomeroomTeacher()` trên lớp của năm đó → **409** (BR-193 "không đổi giáo viên chủ nhiệm"); `update()` capacity → **409**
- Nhưng **đọc** vẫn được: `getDetail()` / `list()` trả đủ dữ liệu (BR-195)
- Event `AcademicYearClosed` publish

**Expected Result (FAIL):**
- Lớp bị **xóa** thay vì đánh dấu read-only → mất toàn bộ lịch sử; MF11 điều tra sự cố cũ không truy được HS thuộc lớp nào; vi phạm trực tiếp BR-193 + BR-195
- `readOnly` chỉ set ở lớp mà **không** enforce khi ghi → cờ trang trí, gọi API vẫn sửa được (xem `MF02-TC-INT-005`)
- Cascade đặt `readOnly` cho **mọi** lớp của trường (không lọc năm) → lớp năm mới cũng bị đóng băng, School Admin không tạo/sửa được gì

**Current Status:** 🔴 Not written
**Implementation Note:** cascade `readOnly` bằng **một** `UPDATE ... WHERE academic_year_id = ?`, trong cùng transaction với đổi `status`.

---

#### `MF02-TC-015` — ADR-M02-2: không lưu field dẫn xuất, đếm runtime

**Severity:** `MEDIUM`
**Feature Under Test:** `SchoolResponse.classes/staff/students` · `ClassroomResponse.students/allergies` · `AcademicYearResponse.classes/students`
**Test File:** `src/test/java/com/sfms/mf02/service/DerivedCountTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-015`
**Oracle Source:** **ADR-M02-2** + **CF-M02-6** (TDS §2.3) — *"`students/classes/staff/allergies` là `COUNT` runtime (không cột vật lý)"* · `data_model.puml` stereotype `<<derived>>` · TDS §17.1 **C6** · **AP-AI-009** (§17.4) · **UC-122**, **UC-165**

**Preconditions:** `FX-001` với **10 lớp** trong seed test — lưu ý mock `SCHOOLS` ghi `classes: 28`, `staff: 86`, `students: 1920` (số **đóng băng**, lệch thực tế)

**Test Steps:**
1. **Act:** `schoolService.getById("SCH-HN-0042")`
2. **Assert:** số trả về là **đếm thật**, không phải giá trị seed

**Expected Result (PASS):**
- `classes = 10` (đếm thật từ `classrooms`), **không phải `28`** (giá trị seed mock)
- `students` = tổng đếm thật qua port MF03; `staff` = đếm thật qua port MF01
- Tạo thêm 1 lớp → gọi lại `getById()` → `classes = 11` **ngay**, không cần job đồng bộ nào
- **Schema assertion:** `information_schema.columns` **không** có cột `classes`/`staff`/`students` trong bảng `schools`, **không** có `students`/`allergies` trong `classrooms`, **không** có `classes`/`students` trong `academic_years`
- Response API **vẫn** trả các số này (khớp hợp đồng FE — CF-M02-6 câu cuối)

**Expected Result (FAIL):**
- Có cột vật lý `classes` → **AP-AI-009**; số liệu đóng băng như mock (28 vs 10 thực tế) → System Admin thấy "28 lớp" trong khi trường chỉ có 10 → báo cáo MF13 sai
- Response **thiếu** các field này → vỡ hợp đồng FE (màn `system-admin/schools` render cột "Số lớp")

**Current Status:** 🔴 Not written
**Implementation Note:** dùng DTO projection / subquery; **không** `@Formula` với subquery nặng trên endpoint danh sách (N+1) — đếm theo batch cho `GET /schools`.

---

#### `MF02-TC-016` — D5: client gửi `id` trùng → 409, không ghi đè

**Severity:** `HIGH`
**Feature Under Test:** `AcademicYearService.create()` · `ClassroomService.create()` · `SchoolService.register()`
**Test File:** `src/test/java/com/sfms/mf02/service/ClientSuppliedIdTest.java`
**TDD Phase:** 🔴 RED — ✅ **Oracle chốt 2026-07-31 (TDS §10 — `SCH-409`/MSG-139)**
**Condition Ref:** `TC-COND-016`
**Oracle Source:** **D5** (`00_Common_Conventions.md`) — *"Nếu request body có `id` → dùng làm `code`, kiểm trùng → trùng thì **409** với mã `<MODULE>-409`. Nếu vắng → server tự sinh theo `code_sequences`. Backend **luôn** trả `id` trong response 201"* · **`error_message.md` v1.6 — MSG-139**, TDS §10 — `SCH-409`

**Preconditions:** `FX-006` (`AY-2026` đã tồn tại ở trường A), `FX-011` (lớp `3A` đã tồn tại trong AY-2025)

**Test Steps:**

| # | Endpoint | Body | Expected |
|---|---|---|---|
| a | `POST /academic-years` | `{"id":"AY-2026", ...}` — **trùng** | 409, bản ghi cũ **nguyên vẹn** |
| b | `POST /academic-years` | `{"id":"AY-2028", ...}` — chưa tồn tại | 201, `response.id == "AY-2028"` (dùng đúng mã client gửi) |
| c | `POST /academic-years` | **vắng** `id` | 201, `response.id` do server sinh, khớp khuôn `code_sequences`, **không null** |
| d | `POST /classrooms` | `{"id":"3A", "academicYearId":"AY-2025"}` — trùng trong cùng năm | 409 |
| e | `POST /classrooms` | `{"id":"3A", "academicYearId":"AY-2026"}` — **trùng tên nhưng khác năm** | **201** — `UNIQUE(academic_year_id, code)` là **per-year** |
| f | `POST /academic-years` | `{"id":"AY-2026"}` gửi bởi **trường B** | **201** — `UNIQUE(school_id, code)` là **per-school** (D1) |

**Expected Result (PASS):**
- a/d: HTTP **409**, `error.code = "SCH-409"`, `error.message` = **MSG-139 nguyên văn**, `details` là mảng, **bản ghi cũ không bị ghi đè** (assert từng field của bản ghi cũ)
- b/c: HTTP 201 và **luôn** có `id` trong response — FE điều hướng ngay sau `onSuccess` bằng giá trị này (D5 câu 3)
- e/f: 201 — chứng minh scope của unique là `(academic_year_id, code)` và `(school_id, code)`, **không** phải toàn cục

**Expected Result (FAIL):**
- a/d trả **200 + ghi đè** bản ghi cũ → mất dữ liệu năm học/lớp đang vận hành chỉ vì client gửi trùng mã
- c trả 201 nhưng `id = null` → FE điều hướng tới `/undefined` (đây là lý do D5 tồn tại)
- e/f trả 409 → hai trường khác nhau không được dùng cùng mã năm học `AY-2026` → vô lý về nghiệp vụ

**Current Status:** 🔴 Not written — ✅ đã mở khóa, sẵn sàng implement
**Implementation Note:** sinh mã qua `code_sequences` với `INSERT ... RETURNING` (D1), **không** đọc-rồi-ghi (trùng mã khi 2 request song song).

---

### 4.2. INTEGRATION TEST CASES — endpoint → DB

> **Hạ tầng:** Testcontainers `postgres:16-alpine` + **Flyway chạy thật** (`V1__init_schema.sql` + `V{n}__seed_mf02.sql`) ⇒ CHECK constraint, partial unique index, FK là **thật**, không mock. Timeout 120s/class, container **dùng lại** trong 1 class (`@Testcontainers` + `@ServiceConnection`), reset dữ liệu giữa các test bằng `@Sql` + `@Transactional` rollback.
> **Base path:** `/api/v1` (**D3**). Mọi assertion lỗi kiểm envelope **D2**; mọi assertion ngày kiểm **D4**; mọi `id` là mã nghiệp vụ (**D1**).

```java
// Boilerplate chung — src/test/java/com/sfms/mf02/AbstractMf02IT.java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
abstract class AbstractMf02IT {
  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");
  // Flyway tự chạy khi context khởi động → schema + seed MF02 (FX-001..FX-026)
  @Autowired protected MockMvc mvc;
  @Autowired protected JdbcTemplate jdbc;   // DB assertion trực tiếp
}
```

---

#### `MF02-TC-INT-001` — BR-191: activate bị chặn khi trường đã có năm ACTIVE

**Severity:** `CRITICAL`
**Feature Under Test:** `POST /api/v1/academic-years/{code}/activate` → DB
**Test File:** `src/test/java/com/sfms/mf02/web/AcademicYearActivateIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-012`
**Oracle Source:** **BR-191** · **MSG-080** → `SCH-080` (TDS §10) · **UC-175** · TDS §13 TC-INT-001 (xương sống) · partial unique index `uq_ay_one_active_per_school` (§5.2)

**Preconditions:** Flyway seed: `FX-001` có `FX-005` (AY-2025 **ACTIVE**) + `FX-006` (AY-2026 **DRAFT**); JWT `FX-031` (School Admin trường A)

**Test Steps:**
1. `POST /api/v1/academic-years/AY-2026/activate` với `Authorization: Bearer <FX-031>`
2. Assert response
3. Assert DB state

**Expected Result (PASS):**
- HTTP **409**; body = `{"error":{"code":"SCH-080","message":"Cannot create Active academic year: an Active academic year already exists for this school. Please close it first.","details":[]}}` — **MSG-080 nguyên văn**, `details` là **mảng** (rỗng được, `null` **không** được — D2)
- **DB assertion:**
```sql
SELECT code, status FROM academic_years WHERE school_id = (SELECT id FROM schools WHERE code='SCH-HN-0042') ORDER BY code;
-- Expected: AY-2024|CLOSED, AY-2025|ACTIVE, AY-2026|DRAFT  (KHÔNG đổi)
SELECT COUNT(*) FROM academic_years WHERE status='ACTIVE' AND school_id=<A>;  -- Expected: 1
```
- Sau đó `POST /academic-years/AY-2025/close` → 200, rồi `POST /academic-years/AY-2026/activate` → **200**, body `{"id":"AY-2026","status":"ACTIVE",...}` (`id` = mã nghiệp vụ — D1; `status` SCREAMING_SNAKE — D6)

**Expected Result (FAIL):**
- 200 ở bước 1 → 2 năm ACTIVE; nếu index tồn tại thì thay vào đó là **500** `DataIntegrityViolationException` lộ ra client (sai mã lỗi, lộ chi tiết DB)

**Current Status:** 🔴 Not written

---

#### `MF02-TC-INT-002` — ★ BR-191 là bất biến **per-school**, không phải toàn cục

**Severity:** `CRITICAL`
**Feature Under Test:** `POST /api/v1/academic-years/{code}/activate` — phạm vi kiểm bất biến
**Test File:** `src/test/java/com/sfms/mf02/web/AcademicYearActivateIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-012`
**Oracle Source:** **BR-191** nguyên văn — *"**Mỗi trường** chỉ được có đúng 1 `AcademicYear` ở trạng thái Active"* · **CF-M02-3** / **L4** · **ADR-M02-4** · **AP-AI-010** (TDS §17.4)

> ★ **Đây là test chuyên để bắt lỗi CF-M02-3.** Logic của mock (`mock-server.ts`: `db["academic-years"].find(r => r.status === "ACTIVE" && r.id !== id)` — **không lọc `schoolId`**) sẽ **FAIL** test này. Nếu backend port nguyên logic mock thì trường thứ hai **không bao giờ** kích hoạt được năm học nào.

**Preconditions:** `FX-001` có `FX-005` (AY-2025 **ACTIVE**) · `FX-002` (**trường B**) có `FX-009` (AY-2025-B **ACTIVE**) + `FX-010` (AY-2026-B **DRAFT**); JWT `FX-032` (School Admin **trường B**)

**Test Steps:**
1. `POST /api/v1/academic-years/AY-2025-B/close` bằng `FX-032` → kỳ vọng 200
2. `POST /api/v1/academic-years/AY-2026-B/activate` bằng `FX-032`
3. Assert: **trường A không bị ảnh hưởng**

**Expected Result (PASS):**
- Bước 2 → **HTTP 200**, `AY-2026-B.status = ACTIVE` — mặc dù trường A **vẫn đang** có `AY-2025` ACTIVE
- **DB assertion:**
```sql
SELECT s.code, COUNT(*) FROM academic_years ay JOIN schools s ON s.id = ay.school_id
WHERE ay.status='ACTIVE' GROUP BY s.code ORDER BY s.code;
-- Expected: SCH-HN-0042|1 , SCH-HN-0051|1   → tổng 2 năm ACTIVE toàn hệ thống là ĐÚNG
SELECT status FROM academic_years WHERE code='AY-2025';  -- Expected: ACTIVE (trường A không đụng)
```
- Bổ sung: partial unique index cho phép trạng thái này — assert index tồn tại và đúng dạng partial:
```sql
SELECT indexdef FROM pg_indexes WHERE indexname='uq_ay_one_active_per_school';
-- Expected: chứa "(school_id)" VÀ "WHERE (status = 'ACTIVE'::text)"  → KHÔNG phải UNIQUE(status) toàn cục
```

**Expected Result (FAIL):**
- Bước 2 trả **409 SCH-080** → **AP-AI-010**: bất biến bị kiểm toàn cục. Hệ quả: hệ thống **chỉ chạy được cho 1 trường**; trường thứ 2 trở đi không tạo được lớp nào (BR-192 đòi có năm ACTIVE) → sản phẩm multi-tenant bị vô hiệu hóa hoàn toàn.
- Index là `UNIQUE(status) WHERE status='ACTIVE'` (thiếu `school_id`) → DB tự chặn trường thứ hai

**Current Status:** 🔴 Not written
**Implementation Note:** đây là test **bắt buộc pass** trước khi Nam final-approve backend MF02 — nó chốt chặn lỗi mà 11 module sau sẽ copy nếu MF02 làm sai.

---

#### `MF02-TC-INT-003` — BR-191 dưới tải song song (race 2 request activate)

**Severity:** `CRITICAL`
**Feature Under Test:** `AcademicYearService.activate()` — `@Transactional` + `PESSIMISTIC_WRITE`
**Test File:** `src/test/java/com/sfms/mf02/web/AcademicYearConcurrencyIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-012`
**Oracle Source:** **BR-191** (bất biến phải giữ **mọi lúc**, kể cả khi song song) · **ADR-M02-4** — *"Enforce ở tầng Service **trong transaction** (`SELECT ... FOR UPDATE`) để tránh race 2 request activate song song"* · `uq_ay_one_active_per_school` (§5.2)
**Technique:** Concurrency Testing

**Preconditions:** trường A **không** có năm ACTIVE nào (AY-2025 đã CLOSED); **hai** năm DRAFT: `AY-2026` và `AY-2027`

**Test Steps:**
1. **Arrange:** `ExecutorService` 2 thread, `CountDownLatch` để phát lệnh đồng thời
2. **Act:** đồng thời `POST /academic-years/AY-2026/activate` và `POST /academic-years/AY-2027/activate`
3. **Assert:** đúng một thắng

**Expected Result (PASS):**
- **Đúng 1** response **200**, **đúng 1** response **409 `SCH-080`** (MSG-080 nguyên văn)
- **DB assertion:** `SELECT COUNT(*) FROM academic_years WHERE school_id=<A> AND status='ACTIVE'` → **= 1**
- Request thua **không** trả 500 — nếu chỉ dựa vào unique index thì client nhận `DataIntegrityViolationException`; `PESSIMISTIC_WRITE` phải làm request thứ hai **chờ**, đọc lại trạng thái, rồi trả **409** đúng nghĩa
- Lặp test 10 lần liên tiếp → kết quả **ổn định** (không flaky)

**Expected Result (FAIL):**
- Cả hai trả 200 → 2 năm ACTIVE (nếu thiếu index) — vi phạm BR-191 ở mức **dữ liệu hỏng thật**
- Một request trả **500** thay vì 409 → bất biến giữ được nhưng ADR-M02-4 (`PESSIMISTIC_WRITE`) **chưa** được hiện thực; FE hiện "Lỗi hệ thống" thay vì thông báo nghiệp vụ MSG-080

**Current Status:** 🔴 Not written

---

#### `MF02-TC-INT-004` — BR-192: tạo lớp khi trường chưa có năm ACTIVE

**Severity:** `HIGH`
**Feature Under Test:** `POST /api/v1/classrooms` → DB
**Test File:** `src/test/java/com/sfms/mf02/web/ClassroomCreateIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-004`
**Oracle Source:** **BR-192** · **MSG-081** → `SCH-081` (TDS §10) · **UC-018** · **CF-M02-4** · TDS §13 TC-INT-002

**Preconditions:** `FX-003` (`SCH-HN-0063`, `status=PENDING`) — có 1 năm học **DRAFT**, **không** có ACTIVE; JWT School Admin của trường đó

**Test Steps:**
1. `POST /api/v1/classrooms` body `{"id":"1A","grade":1,"capacity":40,"academicYearId":"<năm DRAFT>"}`
2. Assert response + DB

**Expected Result (PASS):**
- HTTP **409**, `{"error":{"code":"SCH-081","message":"Cannot create classroom: this school has no Active academic year. Please activate an academic year first.","details":[]}}` (**MSG-081 nguyên văn**)
- **DB assertion:** `SELECT COUNT(*) FROM classrooms WHERE school_id=<0063>` → **0**
- Sau khi `activate` năm DRAFT đó → cùng request → **201** với `{"id":"1A","grade":1,"capacity":40,"students":0,"allergies":0,"teacher":null}` và **MSG-033**

**Expected Result (FAIL):**
- 201 ở bước 1 → lớp treo ở năm chưa vận hành; MF03 gán HS vào lớp đó → dữ liệu HS thuộc năm học "chưa mở"
- Trả 400/422 thay vì 409 → lệch §10

**Current Status:** 🔴 Not written

---

#### `MF02-TC-INT-005` — BR-193: `readOnly` được **enforce khi ghi**, không chỉ là cờ

**Severity:** `HIGH`
**Feature Under Test:** `POST /academic-years/{code}/close` → `PATCH /classrooms/{code}/homeroom-teacher` · `PATCH /classrooms/{code}`
**Test File:** `src/test/java/com/sfms/mf02/web/AcademicYearCloseIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-014`
**Oracle Source:** **BR-193** · **MSG-083** (success của close) · **UC-175** · TDS §13 TC-INT-003 · **BR-195** (đọc vẫn được)

**Preconditions:** `FX-005` (AY-2025 ACTIVE) + 10 lớp `readOnly=false`; `FX-016` (6A thuộc AY-2026 DRAFT) làm nhóm đối chứng

**Test Steps:**
1. `POST /api/v1/academic-years/AY-2025/close` (JWT `FX-031`) → 200
2. `PATCH /api/v1/classrooms/3A/homeroom-teacher` body `{"teacherId":"STF-004"}`
3. `PATCH /api/v1/classrooms/3A` body `{"capacity":48}`
4. `GET /api/v1/classrooms/3A`
5. `PATCH /api/v1/classrooms/6A` body `{"capacity":48}` — lớp năm **DRAFT**, đối chứng

**Expected Result (PASS):**
- Bước 1: 200, message ánh xạ **MSG-083**; **DB:** `SELECT COUNT(*) FROM classrooms WHERE academic_year_id=<AY-2025> AND read_only = false` → **0**; `SELECT COUNT(*) FROM classrooms WHERE academic_year_id=<AY-2025>` → **10** (không xóa gì)
- Bước 2 và 3: **409** — ghi bị chặn (BR-193 "không đổi giáo viên chủ nhiệm", "không tạo học sinh mới"); DB không đổi
- Bước 4: **200** — đọc vẫn được, trả đủ `students`/`allergies`/`teacher` cũ (**BR-195**)
- Bước 5: **200** — lớp của năm khác **không** bị đóng băng (cascade lọc đúng `academic_year_id`)
- **DB:** dữ liệu lịch sử nguyên vẹn — `SELECT code, students_count_snapshot IS NULL FROM ...` không áp dụng (không lưu dẫn xuất); assert `homeroom_teacher_id` của cả 10 lớp **không đổi** so với trước bước 1

**Expected Result (FAIL):**
- Bước 2/3 trả **200** → `readOnly` là **cờ trang trí**: UI ẩn nút nhưng gọi API trực tiếp vẫn sửa được lớp của năm đã đóng → phá append-only §19, sửa được dữ liệu lịch sử đã dùng để báo cáo
- Bước 4 trả 403/404 → vi phạm BR-195 (Archived/Closed phải **luôn truy vấn được**)
- Bước 5 trả 409 → cascade quá rộng

**Current Status:** 🔴 Not written
**Implementation Note:** kiểm `readOnly` ở **service** trước mọi mutation trên `Classroom`; đừng dựa vào FE ẩn nút.

---

#### `MF02-TC-INT-006` — BR-194: copyStructure end-to-end trên DB thật

**Severity:** `HIGH`
**Feature Under Test:** `POST /api/v1/academic-years` (`copyStructure=true`) → DB
**Test File:** `src/test/java/com/sfms/mf02/web/AcademicYearCreateIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-013`
**Oracle Source:** **BR-194** · **MSG-082** · **UC-174** · TDS §13 TC-INT-004 · `UNIQUE(academic_year_id, code)` (§5.2)

**Preconditions:** `FX-005` (AY-2025) có **10 lớp** (5 lớp có GVCN, `students` 34–45)

**Test Steps:**
1. `POST /api/v1/academic-years` body `{"id":"AY-2027","name":"Năm học 2027–2028","startDate":"2027-09-05","endDate":"2028-05-31","copyStructure":true}` (JWT `FX-031`)
2. Assert response + DB

**Expected Result (PASS):**
- HTTP **201**, body `{"id":"AY-2027","name":"Năm học 2027–2028","status":"DRAFT","copyStructure":true,"classes":10,"students":0}` — `startDate`/`endDate` ISO (**D4**), `status` SCREAMING_SNAKE (**D6**)
- **DB assertion:**
```sql
SELECT COUNT(*) FROM classrooms WHERE academic_year_id=(SELECT id FROM academic_years WHERE code='AY-2027');
-- Expected: 10
SELECT c_new.code, c_new.grade, c_new.capacity, c_new.homeroom_teacher_id, c_new.read_only
FROM classrooms c_new JOIN academic_years ay ON ay.id=c_new.academic_year_id
WHERE ay.code='AY-2027' ORDER BY c_new.code;
-- Expected: code/grade/capacity KHỚP TỪNG DÒNG với 10 lớp của AY-2025
--           homeroom_teacher_id = NULL cho CẢ 10 dòng (BR-194 chỉ copy tên/cấp/sức chứa)
--           read_only = false
SELECT COUNT(*) FROM classrooms WHERE academic_year_id=(SELECT id FROM academic_years WHERE code='AY-2025');
-- Expected: 10 (năm cũ KHÔNG bị đụng — append-only)
```
- `students` của mỗi lớp mới (đếm runtime) = **0**
- Lặp lại request với `copyStructure=false` cho `AY-2028` → **0** lớp mới

**Expected Result (FAIL):**
- `homeroom_teacher_id` được copy → lệch BR-194
- Lớp cũ bị **move** sang năm mới (`UPDATE` thay vì `INSERT`) → năm cũ mất lớp, phá BR-195
- `students > 0` ở lớp mới → HS bị chuyển tự động, phá BR-194 câu 2

**Current Status:** 🔴 Not written

---

#### `MF02-TC-INT-007` — UC-177 / BR-194: promote hàng loạt theo khối, loại trừ từng HS, không đụng hồ sơ sức khỏe

**Severity:** `HIGH`
**Feature Under Test:** `POST /api/v1/academic-years/{code}/promote` → DB
**Test File:** `src/test/java/com/sfms/mf02/web/AcademicYearPromoteIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-017`
**Oracle Source:** **BR-194** nguyên văn — *"'Lên lớp' (promote) là một hành động riêng, tường minh... hỗ trợ chuyển **hàng loạt theo khối** kèm khả năng **loại trừ/giữ lại từng học sinh** cụ thể để xử lý trường hợp lưu ban. **Hồ sơ sức khỏe/dị ứng học sinh (`StudentHealthRecord`, `AllergyProfile`) không bị ảnh hưởng** bởi thao tác lên lớp — không cần Medical Staff xác nhận lại"* · **MSG-084** *"Students promoted to next academic year successfully."* · **UC-177**

> ⚠️ **Ghi nhận từ TDS §9.1:** endpoint promote *"UI có, chưa nối API"* — route `school-admin/academic-years/promote` tồn tại nhưng chưa gọi endpoint nào; **cần Minh xác nhận** contract. Test này viết theo **§9.2 của TDS** (nguồn chính); nếu Minh chốt shape khác thì cập nhật cùng lúc cả TDS §9.2 và test này.

**Preconditions:** `FX-014` (5A `students=45`, 5B `students=44` thuộc AY-2025) · `FX-016` (6A thuộc AY-2026, `capacity=50`, `students=0`) · AY-2026 đã ACTIVE · 1 HS trong 5A được đánh dấu loại trừ (lưu ban)

**Test Steps:**
1. `POST /api/v1/academic-years/AY-2026/promote` body:
```json
{ "sourceAcademicYearId": "AY-2025",
  "mappings": [{ "sourceClassroomId": "5A", "targetClassroomId": "6A" }],
  "excludeStudentIds": ["HS-5A-014"] }
```
2. Assert response + DB + **bất biến cross-module**

**Expected Result (PASS):**
- HTTP **200**, body `{"promoted":44,"excluded":1,"targetAcademicYearId":"AY-2026"}` — `45 = 44 + 1`, message ánh xạ **MSG-084**
- **DB assertion (enrollment):** 44 HS của 5A có bản ghi enrollment mới trỏ lớp `6A`; HS `HS-5A-014` **vẫn** thuộc `5A` của AY-2025, **không** có enrollment ở AY-2026
- **DB assertion (bất biến BR-194 — quan trọng nhất):**
```sql
-- Hồ sơ sức khỏe/dị ứng KHÔNG bị đụng: so checksum trước/sau
SELECT md5(string_agg(id::text || COALESCE(updated_at::text,''), '|' ORDER BY id))
FROM student_health_records;   -- Expected: KHỚP snapshot trước khi promote
SELECT md5(string_agg(id::text || severity || COALESCE(updated_at::text,''), '|' ORDER BY id))
FROM allergy_profiles;         -- Expected: KHỚP snapshot trước khi promote
```
- Lớp cũ 5A **vẫn** tồn tại với dữ liệu lịch sử (append-only) — **không** bị xóa/đổi tên thành 6A
- Gọi lại **cùng** request lần hai → **không** nhân đôi (`promoted=0` hoặc 409) — promote phải idempotent theo cặp (HS, năm đích)

**Expected Result (FAIL):**
- `promoted=45` (không loại trừ) → HS lưu ban bị đẩy lên khối 6 → BR-194 câu 2 bị phá ở đúng trường hợp mà nó được viết ra để xử lý
- `updated_at` của `allergy_profiles` bị đổi → hồ sơ dị ứng bị "chạm"; nếu kèm reset trạng thái xác minh thì **mất cảnh báo dị ứng nặng** (BR-123, miền MF03) — đây là rủi ro an toàn thực phẩm nghiêm trọng, không chỉ là lỗi dữ liệu
- Lớp 5A bị đổi thành 6A (in-place) → mất lịch sử năm cũ

**Current Status:** 🔴 Not written
**Implementation Note:** enrollment ghi qua **port theo ID** sang MF03, **không** import entity `Student`; hồ sơ sức khỏe **chỉ đọc** — không có đường ghi nào từ MF02.

---

#### `MF02-TC-INT-008` — BR-025 khi promote: lớp đích vượt sức chứa

**Severity:** `HIGH`
**Feature Under Test:** `POST /api/v1/academic-years/{code}/promote`
**Test File:** `src/test/java/com/sfms/mf02/web/AcademicYearPromoteIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-002`, `TC-COND-017`
**Oracle Source:** **BR-025** (*"sức chứa tối đa 50 học sinh"*) · **MSG-010** → `SCH-010` (TDS §10 ghi rõ trigger áp cho **UC-177**) · **D7** (message tiếng Anh **nguyên văn** `error_message.md`)

> ⚠️ **L8c:** TDS §9.2 ví dụ promote ghi `"message": "Target classroom capacity would be exceeded."` — **không phải** nguyên văn MSG-010. Test assert theo **D7** (MSG-010 nguyên văn) ⇒ **§9.2 phải sửa**.

**Preconditions:** `FX-017` (6B `capacity=45`, `students=44`) · 5B có 44 HS

**Test Steps:**
1. `POST /api/v1/academic-years/AY-2026/promote` với mapping `5B → 6B` (44 HS vào lớp còn 1 chỗ)
2. Assert response + DB

**Expected Result (PASS):**
- HTTP **409**, `error.code = "SCH-010"`, `error.message = "Classroom capacity exceeded. Maximum allowed is 50 students."` (**MSG-010 nguyên văn — D7**), `details` nêu `targetClassroomId="6B"` + số chỗ còn lại
- **DB assertion:** **0** enrollment mới được ghi — promote là **all-or-nothing** trong 1 transaction, **không** chuyển được 1 HS rồi dừng
- Biến thể: mapping `5B → 6A` (`capacity=50`, trống) → **200**, `promoted=44`
- Biến thể biên: lớp đích `capacity=50`, đã có `6` HS, promote **44** → `50` → **200** (biên trên hợp lệ); promote **45** → `51` → **409**

**Expected Result (FAIL):**
- 200 với `promoted=44` → 6B có 88 HS trong lớp sức chứa 45 → phá BR-025 ở quy mô hàng loạt (nguy hiểm hơn tạo lớp lẻ vì một lệnh làm sai cả khối)
- Partial commit (chuyển được 1 HS rồi lỗi) → dữ liệu nửa vời, không rollback được bằng tay
- Message = chuỗi ở §9.2 thay vì MSG-010 → vi phạm D7, FE không map được nhãn tiếng Việt

**Current Status:** 🔴 Not written

---

#### `MF02-TC-INT-009` — Hợp đồng phân trang §2b: 5 field, clamp `page`, `pageCount ≥ 1`

**Severity:** `HIGH`
**Feature Under Test:** `GET /api/v1/schools` · `/classrooms` · `/academic-years` → `PagedResponse<T>`
**Test File:** `src/test/java/com/sfms/mf02/web/PagedResponseContractIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-020`
**Oracle Source:** **Team Plan §2b-1** (shape bất biến 5 field, không wrapper) · **§2b-2** (default `page=1`, `pageSize=8`) · **§2b-3** (thứ tự 9 bước) · **§2b-6** (5 edge case) · **ADR-M02-1** · **UC-140**, **UC-037**, **UC-166**, **UC-176**

> ⭐ **MF02 là bản mẫu** — test này là **hợp đồng chung cho 11 module còn lại copy** (Team Plan §2b-7 checklist của Mạnh).

**Preconditions:** `FX-001`…`FX-004` (**4 trường**) · 10 lớp của AY-2025

**Test Steps + bảng expected:**

| # | Request | Expected |
|---|---|---|
| a | `GET /schools` (không param) | 200; **đúng 5 key**: `items,total,page,pageSize,pageCount`; `page=1`, `pageSize=8` (default §2b-2), `total=4`, `pageCount=1` |
| b | `GET /classrooms?page=1&pageSize=8` | `total=10`, `pageCount=2`, `items.length=8` |
| c | `GET /classrooms?page=2&pageSize=8` | `items.length=2`, `page=2` |
| d | `GET /classrooms?page=99&pageSize=8` | **`page=2`** (clamp về `pageCount` — §2b-6), `items.length=2`, **không** 404, **không** `items=[]` |
| e | `GET /classrooms?pageSize=0` | `pageSize=1` (clamp §2b-6), `pageCount=10` |
| f | `GET /classrooms?pageSize=-5` | `pageSize=1` |
| g | `GET /schools?search=khong-ton-tai-xyz` | **200** `{"items":[],"total":0,"page":1,"pageSize":8,"pageCount":1}` — `pageCount` **tối thiểu 1** kể cả khi rỗng; **không** 404 |
| h | `GET /schools?status=` (value rỗng) | bỏ qua filter → `total=4` (§2b-6) |
| i | `GET /schools?status=ACTIVE` | `total=3` (exact filter, enum SCREAMING_SNAKE — D6) |
| j | `GET /schools?not:status=ACTIVE` | `total=1` (lọc phủ định — §2b-2), trả `SCH-HN-0063` |
| k | `GET /classrooms?grade=3` | `total=2` (3A, 3B) — exact filter áp **trước** search (§2b-3 bước 1) |
| l | `GET /classrooms?grade=3&search=3A` | `total=1` — kết hợp đúng thứ tự |
| m | `GET /schools` | `items[0]` **không** bọc trong `data`/`result`/`content` — **không wrapper** (§2b-1) |

**Expected Result (PASS):**
- Mọi response danh sách có **chính xác** 5 key (assert `keys()` = `["items","page","pageCount","pageSize","total"]` sau khi sort — khớp TDS §14.3)
- `pageCount == max(1, ceil(total/pageSize))` với mọi tổ hợp
- `page == min(requestedPage, pageCount)`
- Dòng d và g là hai edge case dễ sai nhất — **bắt buộc** có

**Expected Result (FAIL):**
- Thiếu `pageCount` hoặc bọc `{"data":{...}}` → FE `usePagedList` vỡ ở **cả 52 resource** (shape này dùng chung) — đây là lý do MF02 phải chốt đúng
- Dòng d trả `items=[]` (Spring mặc định khi `page` vượt) → FE hiện "không có kết quả" dù có 10 lớp
- Dòng g trả **404** → FE hiện lỗi thay vì "0 kết quả"

**Current Status:** 🔴 Not written
**Implementation Note:** `PageRequest.of(page - 1, pageSize)` — Spring **0-indexed** (Team Plan §2b-7); clamp `page` **sau** khi đếm `total`.

---

#### `MF02-TC-INT-010` — `search` **chỉ** chạy trên `SEARCH_FIELDS` cố định

**Severity:** `MEDIUM`
**Feature Under Test:** `SchoolSpec.build()` · `ClassroomSpec.build()`
**Test File:** `src/test/java/com/sfms/mf02/repository/SearchFieldsIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-021`
**Oracle Source:** **Team Plan §2b-3** — *"Mỗi module khai danh sách `SEARCH_FIELDS` cố định; search **chỉ** chạy trên các cột đó"* (lý do: `OR ILIKE` trên 10–20 cột = full table scan) · **§2b bảng SEARCH_FIELDS (bản sửa 30/07/2026, verify từ `mock-data.ts`)**: School = `name, address, email`; Classroom = `id, teacher` · **ADR-M02-1** (bổ sung `taxId` cho School — cột có thật) · **L2**

**Preconditions:** `FX-001`…`FX-004`; 10 lớp

**Test Steps + bảng expected:**

| # | Request | Trường/lớp khớp qua field | Expected |
|---|---|---|---|
| a | `GET /schools?search=Đoàn Thị Điểm` | `name` | `total=1` → `SCH-HN-0042` |
| b | `GET /schools?search=Nam Từ Liêm` | `address` | `total=1` |
| c | `GET /schools?search=contact@dtd.edu.vn` | `email` | `total=1` |
| d | `GET /schools?search=0101245678` | `taxId` (ADR-M02-1) | `total=1` |
| e | `GET /schools?search=ĐOÀN THỊ ĐIỂM` | `name`, **khác hoa/thường** | `total=1` — `ILIKE`, không case-sensitive (§2b-7) |
| f | `GET /schools?search=1850` | ⛔ `mealsPerDay` — **KHÔNG** trong `SEARCH_FIELDS` | **`total=0`** |
| g | `GET /schools?search=07:00 – 17:30` | ⛔ `operatingHours` — không trong `SEARCH_FIELDS` | **`total=0`** |
| h | `GET /schools?search=Phạm Quốc Toản` | ⛔ `emergencyContact` — không trong `SEARCH_FIELDS` | **`total=0`** |
| i | `GET /classrooms?search=3A` | **mã lớp** (`id`/`code` — L2) | `total=1` |
| j | `GET /classrooms?search=Đào Văn Quân` | `teacher` | `total=1` → 4A |
| k | `GET /classrooms?search=` (rỗng) | — | bỏ qua search → `total=10` (§2b-6) |

**Expected Result (PASS):**
- Dòng a–e, i–k đúng như bảng
- Dòng **f/g/h trả `total=0`** — đây là phần chứng minh `SEARCH_FIELDS` **thật sự cố định**, không phải `OR` toàn bộ cột String như mock in-memory
- **Assertion hiệu năng (tùy chọn, HIGH value):** bật `spring.jpa.show-sql` và assert SQL sinh ra có **đúng 4** vị từ `lower(...) like ?` cho `/schools` (name, address, email, taxId) — không nhiều hơn

**Expected Result (FAIL):**
- Dòng f/g/h trả `total=1` → search OR toàn bộ cột → **full table scan**, không dùng index; với ~15k `classrooms` (§4.4 TDS) endpoint danh sách vượt SLA p99 < 300ms
- Dòng i trả 0 → `SEARCH_FIELDS` khai `name` trong khi cột thực là `code` (**L2**) → search lớp **im lặng không tìm ra gì**, hoặc lỗi runtime "column does not exist" (đúng hậu quả mà Team Plan §2b cảnh báo cho 13 dòng SEARCH_FIELDS sai)

**Current Status:** 🔴 Not written

---

#### `MF02-TC-INT-011` — D1: `id` JSON và path param là mã nghiệp vụ, không rò UUID

**Severity:** `HIGH`
**Feature Under Test:** mọi endpoint MF02 — tầng serialize + resolve path param
**Test File:** `src/test/java/com/sfms/mf02/web/IdentityContractIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-022`
**Oracle Source:** **D1** (`00_Common_Conventions.md`) — *"`id` trong JSON và path param = **mã nghiệp vụ**; UUID chỉ là **PK nội bộ — KHÔNG lộ ra API**"* · **L1** (§5.2 hiện **thiếu** cột `code`) · **L2** · **MSG-137** → `SCH-404`

**Preconditions:** `FX-001` (`SCH-HN-0042`), `FX-005` (`AY-2025`), `FX-011` (`3A`)

**Test Steps + expected:**

| # | Request | Expected |
|---|---|---|
| a | `GET /api/v1/schools/SCH-HN-0042` | **200** — path param nhận **mã**, `body.id == "SCH-HN-0042"` |
| b | `GET /api/v1/schools/<uuid nội bộ của trường A>` | **404 `SCH-404`** / MSG-137 — UUID **không** phải định danh công khai |
| c | `GET /api/v1/classrooms/3A` | 200, `body.id == "3A"` |
| d | `GET /api/v1/academic-years` | mỗi item `id` khớp `^AY-\d{4}(-[A-Z])?$`, **không** phải UUID |
| e | `GET /api/v1/schools/SCH-HN-0042` | **toàn bộ** response body **không** khớp regex UUID `[0-9a-f]{8}-[0-9a-f]{4}-...` ở **bất kỳ** field nào |
| f | `GET /api/v1/classrooms/3A` | `academicYearId == "AY-2025"` — **khóa ngoại trong JSON dùng `code` của bảng đích** (D1) |
| g | `GET /api/v1/schools/SCH-HN-0042` | **không** có **hai** field trùng nghĩa `id` + `code`; với classroom: **không** đồng thời có `id` và `name` khác nhau (**L2**) |
| h | `POST /api/v1/classrooms` → 201 | `body.id` **có mặt** và **không null** (D5 câu 3) |

**Expected Result (PASS):**
- **DB assertion (chứng minh UUID vẫn là PK nội bộ, không mất tính toàn vẹn):**
```sql
SELECT data_type FROM information_schema.columns WHERE table_name='classrooms' AND column_name='id';
-- Expected: uuid
SELECT data_type, character_maximum_length FROM information_schema.columns
WHERE table_name='classrooms' AND column_name='code';
-- Expected: character varying | 32     ← L1: cột này PHẢI được thêm vào §5.2
SELECT indexdef FROM pg_indexes WHERE tablename='schools' AND indexdef LIKE '%code%';
-- Expected: UNIQUE ... (school_id, code) hoặc UNIQUE(code) cho bảng schools
SELECT confrelid::regclass FROM pg_constraint WHERE conname LIKE '%classrooms%academic_year%';
-- Expected: academic_years  ← FK trong DB vẫn là UUID REFERENCES (D1 câu 3)
```

**Expected Result (FAIL):**
- Dòng a trả 404 và dòng b trả 200 → API dùng UUID làm định danh công khai → **mọi URL FE vỡ** (`/school-admin/students/HS-3A-001` là khuôn FE đang dùng ở mọi nơi)
- Dòng e tìm thấy UUID trong response → rò PK nội bộ; với `partnerId` ở MF04 điều tương tự là **rủi ro rò dữ liệu chéo giữa nhà cung cấp** (D1 lý do 3)
- Dòng f trả `academicYearId` là UUID → FE không join được dữ liệu

**Current Status:** 🔴 Not written
**Implementation Note:** ✅ L1 đã chốt 2026-07-31 — §5.2 của TDS đã có cột `code`, test a/c/f pass được.

---

#### `MF02-TC-INT-012` — D4: mọi field ngày/giờ JSON là ISO-8601

**Severity:** `HIGH`
**Feature Under Test:** Jackson serialization toàn module
**Test File:** `src/test/java/com/sfms/mf02/web/DateFormatContractIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-023`
**Oracle Source:** **D4** — bảng kiểu: `LocalDate` → `"2026-07-29"`, `Instant` → `"2026-07-29T02:15:00Z"`; *"**bỏ** mọi `@JsonFormat("dd/MM/yyyy")`"* · TDS §3 bảng D4 · **L5** (§9.2 ví dụ `registeredAt` date-only nhưng §5.2 khai `Instant`)

**Preconditions:** seed đã chuẩn hóa ISO (`FX-001`: `registeredAt="2026-01-05T00:00:00Z"`; `FX-005`: `startDate="2025-09-05"`)

**Test Steps:**
1. Gọi **toàn bộ** endpoint `GET` của MF02 (`/schools`, `/schools/{code}`, `/schools/{code}/config`, `/schools/me/policy`, `/classrooms`, `/classrooms/{code}`, `/academic-years`)
2. Duyệt **đệ quy** mọi field của mọi response, phân loại theo kiểu ở §5.2

**Expected Result (PASS):**
- Field kiểu `LocalDate` (`startDate`, `endDate`) khớp `^\d{4}-\d{2}-\d{2}$`
- Field kiểu `Instant` (`registeredAt`, `createdAt`, `updatedAt`) khớp `^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d+)?Z$` — **có** phần thời gian và hậu tố `Z`
- **Toàn bộ** body: **0** kết quả cho regex `\d{2}/\d{2}/\d{4}` (dd/MM/yyyy) — assert trên chuỗi JSON thô
- **Grep tài liệu/code:** `@JsonFormat` xuất hiện **0 lần** trong `com.sfms.mf02` (checklist §2c-3 của Team Plan)
- Payload **gửi lên** cũng phải nhận ISO: `POST /academic-years` với `startDate="05/09/2027"` → **400 MSG-138**; với `"2027-09-05"` → 201

**Expected Result (FAIL):**
- `registeredAt` trả `"05/01/2026"` → mock format lọt vào backend; FE `lib/format.ts` (`formatVnDate`) parse ISO sẽ ra `Invalid Date`
- `registeredAt` trả `"2026-01-05"` (date-only) trong khi §5.2 khai `Instant` → **L5**: mất thông tin giờ; nếu FE gửi lại giá trị này thì Jackson không deserialize được về `Instant`
- ⚠️ **Cảnh báo liên đới (D4 mục "Kèm theo"):** khi seed chuyển sang ISO mà `paginate()` của mock vẫn parse `^(\d{2})\/(\d{2})\/(\d{4})` thì lọc `dateFrom`/`dateTo` **im lặng trả rỗng** — MF02 không dùng `dateFrom`/`dateTo` (§2b-5 không liệt kê resource MF02) nên **không bị ảnh hưởng**; ghi lại để module MF06/07/08/09/13 chú ý.

**Current Status:** 🔴 Not written

---

#### `MF02-TC-INT-013` — D2 + D7: envelope lỗi thống nhất trên **mọi** error path

**Severity:** `HIGH`
**Feature Under Test:** `@RestControllerAdvice` ở `common/` (**MF02 là bản mẫu** — D2)
**Test File:** `src/test/java/com/sfms/mf02/web/ErrorEnvelopeContractIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-024`
**Oracle Source:** **D2** — *"`{"error":{"code","message","details"}}`; `details` — mảng, có thể rỗng; **không bao giờ `null`**"* · **D7** — *"`message` **tiếng Anh**, nguyên văn `error_message.md`"* · TDS §10 (12 dòng `SCH-*`)

> ⭐ MF02 là **bản mẫu envelope cho cả 13 module** (D2 ghi rõ *"`@RestControllerAdvice` ở `common/`, MF02 là bản mẫu"*).

**Preconditions:** toàn bộ fixture

**Test Steps:** kích hoạt **từng** dòng của TDS §10 rồi assert envelope:

| `code` | HTTP | Cách kích hoạt | Message expected (nguyên văn `error_message.md` v1.5) |
|---|---|---|---|
| `SCH-004` | 401 | gọi `GET /schools` **không** header `Authorization` | `Session expired. Please log in again.` (MSG-004) |
| `SCH-005` | 403 | Parent (`FX-037`) gọi `GET /classrooms` | `Access denied: You do not have permission to access this resource.` (MSG-005) |
| `SCH-007` | 400 | `POST /schools/register` với `taxId="abc"` | `Invalid tax ID format. Please verify and try again.` (MSG-007) |
| `SCH-010` | 409 | `POST /classrooms` với `capacity=51` | `Classroom capacity exceeded. Maximum allowed is 50 students.` (MSG-010) |
| `SCH-011` | 409 | gán GV đang chủ nhiệm lớp khác | `Homeroom teacher is already assigned to another classroom.` (MSG-011) |
| `SCH-013` | 400 | `PATCH /schools/{code}` với `lat=91` (trường chưa khóa) | `Invalid GPS coordinates. Latitude must be between -90 and 90, longitude between -180 and 180.` (MSG-107) |
| `SCH-013` | 403 | `PATCH /schools/SCH-HN-0042` với `lat=21.5` (đã khóa) | `School coordinates are locked. Only a System Administrator can change them.` (MSG-108) |
| `SCH-024` | 422 | `POST /classrooms` với `grade=13` | `Grade level must be between 1 and 12.` (MSG-109) |
| `SCH-080` | 409 | activate khi trường đã có năm ACTIVE | `Cannot create Active academic year: an Active academic year already exists for this school. Please close it first.` (MSG-080) |
| `SCH-081` | 409 | `POST /classrooms` khi chưa có năm ACTIVE | `Cannot create classroom: this school has no Active academic year. Please activate an academic year first.` (MSG-081) |
| `SCH-404` | 404 | `GET /classrooms/KHONG-TON-TAI` | `Resource not found.` (MSG-137) |
| *(validation)* | 422 | `PATCH /schools/{code}/config` với `geofenceRadius=501` | `Validation failed: one or more required fields are missing or invalid.` (MSG-138) |
| `SCH-001` | 500 | inject lỗi lưu (stub repo ném `DataAccessException`) | `System error while saving school data. Please try again.` (MSG-001) |

**Expected Result (PASS):**
- **Mọi** response lỗi có **đúng** cấu trúc `{"error":{"code","message","details"}}` — không field lạ ở ngoài, **không** shape phẳng `{"message":"..."}` như mock
- `details` **luôn** là mảng JSON (`[]` khi không có chi tiết) — assert `isArray()`, **không** `null`
- `message` **khớp từng ký tự** với `error_message.md` v1.5 (**D7**) — assert bằng so sánh chuỗi chính xác, không `contains`
- `code` khớp cột "Code" của TDS §10
- **Không** rò stack trace / tên class Java / câu SQL vào `message` hoặc `details` (kể cả dòng `SCH-001`)
- Response 500 **không** làm lộ `DataIntegrityViolationException`, tên constraint, tên bảng

**Expected Result (FAIL):**
- Shape phẳng `{"message":"..."}` → FE interceptor đọc `data?.error?.message` ra `undefined`; D2 đã cho interceptor khoan dung hai shape nên **không** vỡ ngay, nhưng `code` mất ⇒ FE **không** map được nhãn tiếng Việt (`lib/messages.ts`) → người dùng thấy tiếng Anh
- `details: null` → FE `.map()` trên `null` → crash màn hình
- `message` tiếng Việt tự viết → vi phạm **D7**, lệch nguồn chuẩn duy nhất
- Tên constraint DB (`uq_ay_one_active_per_school`) lọt vào `message` → rò chi tiết schema

**Current Status:** 🔴 Not written

---

#### `MF02-TC-INT-014` — BR-028: vòng đời khóa tọa độ (ACTIVE → khóa → unlock → lưu → **tự khóa lại**)

**Severity:** `HIGH`
**Feature Under Test:** `PATCH /schools/{code}/status` · `POST /schools/{code}/coordinate/unlock` · `PATCH /schools/{code}`
**Test File:** `src/test/java/com/sfms/mf02/web/SchoolCoordinateLifecycleIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-009`, `TC-COND-010`
**Oracle Source:** **BR-028** nguyên văn — *"Sau khi tọa độ GPS được xác nhận (trường chuyển sang trạng thái **ACTIVE**), trường `coordinateLocked` được đặt thành `true`"* + *"**Sau khi tọa độ mới được lưu, `coordinateLocked` tự động trả về `true`**"* · **§6.4** state machine · **ADR-M02-3** · TDS §13 TC-E2E-002 · audit thật `AUD-98955` làm khuôn

**Preconditions:** `FX-003` (`SCH-HN-0063`, `status=PENDING`, `coordinateLocked=false`)

**Test Steps (chuỗi 6 bước — State Transition):**
1. `PATCH /schools/SCH-HN-0063` với `{"lat":20.9960}` bằng System Admin → kỳ vọng **200** (chưa khóa)
2. `PATCH /schools/SCH-HN-0063/status` với `{"status":"ACTIVE"}` bằng `FX-030` → 200
3. `GET /schools/SCH-HN-0063` → assert `coordinateLocked == true` (**tự khóa sau ACTIVE**)
4. `PATCH /schools/SCH-HN-0063` với `{"lat":21.5}` → kỳ vọng **403 SCH-013** / MSG-108
5. `POST /schools/SCH-HN-0063/coordinate/unlock` với `{"reason":"hiệu chỉnh sai số GPS ±40m"}` bằng `FX-030` → 200, `coordinateLocked=false`
6. `PATCH /schools/SCH-HN-0063` với `{"lat":21.5,"lng":105.87}` → **200**; rồi `GET` lại

**Expected Result (PASS):**
- Bước 3: `coordinateLocked = true` — **tự động**, không cần lệnh riêng
- Bước 4: 403 với **MSG-108 nguyên văn**; `lat` trong DB **không đổi**
- Bước 5: `coordinateLocked = false`; **audit log** có bản ghi: `actor` = `admin@sfms.vn` (từ JWT `FX-030`), `action` chứa `"UNLOCK coordinateLocked SCH-HN-0063"` + **lý do nguyên văn**, `result="SUCCESS"`, `timestamp` kiểu `Instant` (D4) — khớp khuôn `AUD-98955` của seed thật
- Bước 6: tọa độ mới được lưu **và** `coordinateLocked` **tự về `true`** (BR-028 câu cuối) → gọi `PATCH lat` lần nữa → **403** lại
- **DB assertion:**
```sql
SELECT lat, lng, coordinate_locked FROM schools WHERE code='SCH-HN-0063';
-- Expected: 21.500000 | 105.870000 | true
SELECT COUNT(*) FROM audit_logs WHERE action LIKE '%UNLOCK coordinateLocked SCH-HN-0063%';  -- Expected: 1
```

**Expected Result (FAIL):**
- Bước 3 `coordinateLocked` vẫn `false` → cửa sổ sửa tọa độ mở vô thời hạn sau khi trường đã vận hành → mốc geofence của MF06/07/08 có thể bị đổi bất cứ lúc nào
- Bước 6 để `coordinateLocked=false` → **unlock một lần = mở khóa mãi mãi**; đây là lỗi hay gặp nhất khi hiện thực BR-028 và là lý do câu cuối của BR-028 tồn tại
- Bước 5 không ghi audit hoặc ghi thiếu `reason` → không truy vết được (xem `MF02-TC-010`)

**Current Status:** 🔴 Not written

---

#### `MF02-TC-INT-015` — UC-070: đăng ký trường + trùng mã số thuế

**Severity:** `HIGH`
**Feature Under Test:** `POST /api/v1/schools/register` → DB
**Test File:** `src/test/java/com/sfms/mf02/web/SchoolRegisterIT.java`
**TDD Phase:** 🔴 RED — phần happy path ✅ · phần trùng `taxId` ✅ **Oracle chốt 2026-07-31 (`SCH-409`/MSG-139)**
**Condition Ref:** `TC-COND-016`
**Oracle Source:** **MSG-032** *"School registered successfully. Pending System Admin approval."* · **§6.4** (`[*] → PENDING`) · **UC-070** · `tax_id VARCHAR(20) NOT NULL UNIQUE` (§5.2) · **`error_message.md` v1.6 — MSG-139**, TDS §10 — `SCH-409`

**Preconditions:** DB có `FX-001` (`taxId="0101245678"`)

**Test Steps:**
1. `POST /api/v1/schools/register` **không** JWT, body theo §9.2:
```json
{ "name": "TH Thực nghiệm Thạch Bàn", "taxId": "0108888999", "email": "admin@tb.edu.vn",
  "address": "Long Biên, Hà Nội", "lat": 21.0245, "lng": 105.9012,
  "operatingHours": "07:30 – 17:30 (T2–T6)", "emergencyContact": "Vũ Thị Nga — 0988 111 222" }
```
2. Lặp lại với `taxId="0101245678"` (**trùng** `FX-001`)

**Expected Result (PASS):**
- Bước 1: **201** — endpoint **public**, **không** cần JWT (§16: Guest ✅). Body: `{"id":"<mã sinh>","name":"TH Thực nghiệm Thạch Bàn","status":"PENDING","coordinateLocked":false,"registeredAt":"<ISO Instant>","classes":0,"staff":0,"students":0}` — `status=PENDING` (§6.4), `coordinateLocked=false` (BR-028: chỉ khóa **sau** ACTIVE), 3 số dẫn xuất **= 0** và **đếm runtime** (ADR-M02-2), `registeredAt` ISO-8601 (**D4** — xem L5)
- `id` **có mặt, không null** (D5)
- **DB:** `SELECT status, coordinate_locked FROM schools WHERE tax_id='0108888999'` → `PENDING | false`
- Audit: event `SchoolRegistered` publish (TDS §7.1)
- Bước 2: **409**, `error.code = "SCH-409"`, `error.message` = **MSG-139 nguyên văn**, envelope D2 hợp lệ; **DB:** `SELECT COUNT(*) FROM schools WHERE tax_id='0101245678'` → **1** (bản ghi cũ **không** bị ghi đè), tên trường cũ **không đổi**. **Không** dùng MSG-007 (nội dung *"Invalid tax ID format"* mô tả **sai** nguyên nhân, vi phạm D7)

**Expected Result (FAIL):**
- Bước 1 trả **401** → endpoint bị đặt sau JWT filter → **không ai đăng ký được trường mới** (luồng Guest ở `(auth)/register/school` chết)
- Bước 1 trả `status=ACTIVE` → bỏ qua bước phê duyệt System Admin (UC-001) + khóa tọa độ sai thời điểm
- Bước 2 trả **500** `DataIntegrityViolationException` → lộ tên constraint DB cho **người chưa đăng nhập** (rò schema qua endpoint public)
- Bước 2 trả 400 MSG-007 → thông báo sai nguyên nhân, người dùng sửa định dạng mãi không được

**Current Status:** 🔴 Not written — ✅ đã mở khóa toàn bộ, sẵn sàng implement

---

#### `MF02-TC-INT-016` — BR-195: dữ liệu năm học ARCHIVED luôn truy vấn được, read-only

**Severity:** `MEDIUM`
**Feature Under Test:** `GET /api/v1/academic-years` · `GET /classrooms` với năm ARCHIVED
**Test File:** `src/test/java/com/sfms/mf02/web/ArchivedAcademicYearIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-018`
**Oracle Source:** **BR-195** nguyên văn — *"Dữ liệu của năm học đã **Archived**... phải **luôn truy vấn được** ở chế độ **read-only** phục vụ audit/báo cáo dài hạn"* · **UC-176** (traceability §1.2: BR-195) · §6.3 (`CLOSED → ARCHIVED`)

**Preconditions:** `FX-008` (`AY-2023`, `status=ARCHIVED`) + ≥1 lớp thuộc năm đó, `readOnly=true`

**Test Steps:**
1. `GET /api/v1/academic-years?status=ARCHIVED` (JWT `FX-031`)
2. `GET /api/v1/academic-years/AY-2023`
3. `GET /api/v1/classrooms?academicYearId=AY-2023`
4. `PATCH /api/v1/classrooms/<lớp của AY-2023>` với `{"capacity":48}`
5. `POST /api/v1/academic-years/AY-2023/activate`

**Expected Result (PASS):**
- Bước 1–3: **200** với dữ liệu đầy đủ (không rỗng, không 403/404) — kể cả sau nhiều năm
- Bước 2: `status == "ARCHIVED"` (**D6** SCREAMING_SNAKE)
- Bước 4: **409** — read-only (BR-195 + BR-193)
- Bước 5: **409** — §6.3 không có transition `ARCHIVED → ACTIVE`
- Năm ARCHIVED **vẫn** xuất hiện trong `GET /academic-years` mặc định (không bị ẩn khỏi danh sách) — hoặc nếu bị ẩn thì **phải** lấy ra được bằng `?status=ARCHIVED`; assert cả hai cách để hợp đồng rõ ràng
- ⚠️ **Ghi nhận thiếu sót thiết kế:** §6.3 ghi transition `CLOSED → ARCHIVED` là *"(định kỳ) lưu trữ dài hạn"* nhưng **§9 không có endpoint nào** và **§11 không có job/cron nào** thực hiện việc này ⇒ trạng thái `ARCHIVED` hiện **chỉ đến được bằng seed/SQL tay**. Test này dùng fixture `FX-008` (seed trực tiếp). **Cần Nam bổ sung** cơ chế (job định kỳ hoặc endpoint System Admin) vào TDS §9/§11.

**Expected Result (FAIL):**
- Bước 1–3 trả rỗng/404 → mất truy vết dài hạn; MF11 điều tra sự cố cũ và MF13 báo cáo nhiều năm không có dữ liệu → vi phạm BR-195 (và BR-171/174 mà BR-195 dẫn chiếu)
- Bước 4/5 thành công → dữ liệu lịch sử đã lưu trữ bị sửa

**Current Status:** 🔴 Not written

---

#### `MF02-TC-INT-017` — BR-021 + BR-026: cascade vô hiệu hóa trường và audit trail

**Severity:** `HIGH`
**Feature Under Test:** `PATCH /api/v1/schools/{code}/status` (`INACTIVE`) → cascade + audit
**Test File:** `src/test/java/com/sfms/mf02/web/SchoolDeactivateCascadeIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-019`, `TC-COND-006`
**Oracle Source:** **BR-021** nguyên văn — *"Khi **vô hiệu hóa** một trường học, hệ thống tự động **tạm dừng** tất cả: **Lớp học** thuộc trường đó, **Tài khoản nhân viên** thuộc trường, **Tài khoản học sinh** thuộc trường. Thao tác này **phải được ghi vào audit log**"* · **MSG-001** (lỗi lưu) · **UC-001** · TDS §7.1 event `SchoolDeactivated` · **BR-026** (audit phân công GVCN)

**Preconditions:** `FX-001` (`SCH-HN-0042` ACTIVE, 10 lớp) · `FX-002` (`SCH-HN-0051` ACTIVE — **nhóm đối chứng**)

**Test Steps:**
1. `PATCH /api/v1/schools/SCH-HN-0042/status` với `{"status":"INACTIVE"}` bằng `FX-030`
2. Assert cascade trong phạm vi MF02
3. Assert event + audit
4. Assert **trường B không bị ảnh hưởng**
5. `PATCH .../status` với `{"status":"ACTIVE"}` → reactivate (§6.4)

**Expected Result (PASS):**
- HTTP 200, `SCH-HN-0042.status = INACTIVE`
- **Trong phạm vi MF02:** toàn bộ 10 lớp của trường bị **tạm dừng** (`readOnly=true` — cùng nguyên tắc BR-193/BR-021 "không xóa"); **DB:** `SELECT COUNT(*) FROM classrooms WHERE school_id=<A> AND read_only=false` → **0**
- **Ngoài phạm vi MF02** (MF01 `user_accounts`, MF03 `students`): assert **event `SchoolDeactivated` được publish** với `schoolId` đúng (TDS §7.1) — MF02 **không** tự UPDATE bảng của module khác (TDS §17.1 **C6**: tham chiếu bằng ID, không JOIN cứng); assert `verify(userAccountPort).suspendBySchool(<A>)` được gọi
- **Audit log (BR-021 câu cuối):** có bản ghi `action` chứa `"DEACTIVATE School SCH-HN-0042"`, `actor` từ JWT, `result="SUCCESS"`
- **Trường B:** `status` vẫn `ACTIVE`, lớp `9Z` (`FX-018`) vẫn `read_only=false` — cascade **lọc đúng `school_id`**
- Bước 5: reactivate → `status=ACTIVE`; **ghi nhận**: BR-021 **không** nói gì về việc gỡ `readOnly` khi reactivate ⇒ đánh dấu ⚠️ **thiếu oracle nhẹ**, test chỉ assert `school.status=ACTIVE`, **không** khẳng định trạng thái lớp; cần Huy xác nhận hành vi mong muốn

**Expected Result (FAIL):**
- Lớp bị **xóa** thay vì tạm dừng → BR-021 nói "tạm dừng", không phải xóa; mất dữ liệu lịch sử của cả trường
- MF02 tự `UPDATE user_accounts` → vi phạm C6 + Team Plan §4b (không import/ghi chéo module); khi MF01 đổi schema thì MF02 vỡ
- Trường B bị ảnh hưởng → **sự cố cấp nghiêm trọng** (TDS §12.1 liệt kê *"Cascade deactivate suspend nhầm trường khác"* là điều kiện rollback + thông báo **DPO**)
- Không audit → vi phạm BR-021

**Current Status:** 🔴 Not written

---

#### `MF02-TC-INT-018` — UC-095: cấu hình cấp trường (geofence) persist đúng, `costCeiling` không bị MF02 kiểm luật

**Severity:** `MEDIUM`
**Feature Under Test:** `GET`/`PATCH /api/v1/schools/{code}/config`
**Test File:** `src/test/java/com/sfms/mf02/web/SchoolConfigIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-011`
**Oracle Source:** **BR-028** (geofence 50–500, mặc định 200, **audit mọi thay đổi**) · **MSG-029** *"Profile updated successfully."* · **UC-095** · `school_config.school_id UNIQUE` (§5.2) · TDS-01 "ngoài phạm vi": `costCeiling` = **BR-080, miền MF05**

**Preconditions:** `FX-026` (config của `FX-001`: `geofenceRadius=200`, `costCeiling=30000`, `reportEmails` 2 địa chỉ) · `FX-003` (trường **chưa có** dòng config)

**Test Steps:**
1. `GET /schools/SCH-HN-0042/config` bằng `FX-031` (School Admin — §16 cho phép ✅ School)
2. `PATCH` với `{"geofenceRadius":350}` → 200
3. `PATCH` với `{"geofenceRadius":501}` → 422
4. `PATCH` với `{"costCeiling":45000}` → 200
5. `GET /schools/SCH-HN-0063/config` (trường **chưa** có dòng config)
6. `PATCH /schools/SCH-HN-0063/config` với `{"geofenceRadius":250}` (upsert)

**Expected Result (PASS):**
- Bước 1: 200, `{"geofenceRadius":200,"costCeiling":30000,"reportEmails":["toanpq@dtd.edu.vn","hant@dtd.edu.vn"],"updatedAt":"<ISO>","updatedBy":"..."}` — `reportEmails` là **mảng JSON** (cột `jsonb`), không phải chuỗi
- Bước 2: 200 + **MSG-029**; **audit log** có bản ghi thay đổi bán kính (BR-028 câu cuối); `SELECT geofence_radius FROM school_config WHERE school_id=<A>` → `350`
- Bước 3: **422 MSG-138**, giá trị vẫn `350`
- Bước 4: 200 — `costCeiling` lưu **nguyên** `45000`, MF02 **không** kiểm luật trần chi phí (đó là việc của MF05 khi soạn thực đơn); **không** vì `costCeiling` mà từ chối request
- Bước 5: 200 với `geofenceRadius=200` (DEFAULT của cột — không phải `null`/`0`)
- Bước 6: 200 và **DB:** `SELECT COUNT(*) FROM school_config WHERE school_id=<0063>` → **1** (upsert, không tạo dòng thứ hai — `school_id UNIQUE`)
- Cách ly: School Admin trường A gọi `GET /schools/SCH-HN-0051/config` → **403/404** (xem `MF02-TC-SEC-009`)

**Expected Result (FAIL):**
- Bước 5 trả `geofenceRadius=null` → MF06/07/08 tính geofence với `null` → NPE hoặc "mọi vị trí đều hợp lệ"
- Bước 6 tạo **2 dòng** config cho cùng trường → đọc ra giá trị không xác định (`school_id UNIQUE` phải chặn)
- `reportEmails` trả `"[\"a@b.c\"]"` (chuỗi JSON escape) → FE không `.map()` được
- Bước 2 không ghi audit → vi phạm BR-028 câu cuối

**Current Status:** 🔴 Not written

---

### 4.3. SECURITY TEST CASES — RBAC 7 role · cách ly cross-school · bypass UI

> Nguồn ma trận: **TDS §16 Authorization Matrix** (7 role × 11 nhóm endpoint) · **BR-007** (7 role) · **BR-022** (cách ly theo `schoolId` trong JWT) · **TDS §17.1 C2** + **AP-AI-006** (không tin identity từ FE).
> Role key theo **D6** `RoleKey`: `system-admin`, `school-admin`, `teacher`, `parent`, `medical-staff`, `service-caterers`, `supplier` (**kebab-case**).

---

#### `MF02-TC-SEC-001` — Ma trận RBAC đầy đủ: 7 role × 17 endpoint

**Severity:** `CRITICAL`
**OWASP:** `A01:2021 — Broken Access Control`
**CWE:** `CWE-862 — Missing Authorization` · `CWE-863 — Incorrect Authorization`
**Feature Under Test:** `@PreAuthorize` trên toàn bộ controller MF02
**Test File:** `src/test/java/com/sfms/mf02/security/AuthorizationMatrixIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-025`
**Oracle Source:** **TDS §16** (ma trận nguyên văn) · **BR-007** (RBAC 7 role) · **MSG-005** → `SCH-005`, HTTP 403 · **UC-122/140/095/001/137/037/166/165/018/090/008/174/175/176/177**
**Technique:** Combinatorial (pairwise) — `@ParameterizedTest` + `@MethodSource` sinh từ bảng §16, **không** viết tay 119 method

**Preconditions:** JWT `FX-030`…`FX-038` (7 role + guest) · toàn bộ fixture dữ liệu

**Test Steps (Attack Simulation):**
1. Sinh tập `(role, endpoint, method)` từ bảng §16 → **119 tổ hợp**
2. Với mỗi tổ hợp: gọi endpoint bằng JWT của role đó, payload hợp lệ (để **không** bị chặn bởi validation trước khi tới guard)
3. Assert theo ô tương ứng của §16

**Ma trận expected (dịch từ TDS §16 — `✅` = 2xx, `❌` = 403, `➖` = không áp dụng):**

| Endpoint | `system-admin` | `school-admin` | `teacher` / `medical-staff` / `service-caterers` | `supplier` | `parent` | guest (no JWT) |
|---|---|---|---|---|---|---|
| `POST /schools/register` | ➖ | ➖ | 403 | 403 | 403 | **✅ 201** |
| `GET /schools` · `GET /schools/{code}` | ✅ **All** | 403 | 403 | 403 | 403 | **401** |
| `PATCH /schools/{code}` · `PATCH /schools/{code}/status` | ✅ All | 403 | 403 | 403 | 403 | 401 |
| `POST /schools/{code}/coordinate/unlock` | ✅ | **403** | 403 | 403 | 403 | 401 |
| `GET`/`PATCH /schools/{code}/config` | ✅ All | ✅ **School** | 403 | 403 | 403 | 401 |
| `GET /schools/me/policy` | ✅ | ✅ School | ✅ **School** | 403 | 403 | 401 |
| `GET /classrooms` · `GET /classrooms/{code}` | ✅ All | ✅ School | 403 | 403 | 403 | 401 |
| `POST /classrooms` · `PATCH /classrooms/{code}` | **403** | ✅ School | 403 | 403 | 403 | 401 |
| `PATCH /classrooms/{code}/homeroom-teacher` | **403** | ✅ School | 403 | 403 | 403 | 401 |
| `GET /academic-years` | ✅ All | ✅ School | 403 | 403 | 403 | 401 |
| `POST /academic-years` · `/activate` · `/close` · `/promote` | **403** | ✅ School | 403 | 403 | 403 | 401 |

**Expected Result (PASS = hệ thống an toàn):**
- **Mọi** ô `❌` trả **403** với `{"error":{"code":"SCH-005","message":"Access denied: You do not have permission to access this resource.","details":[]}}` — **MSG-005 nguyên văn**
- Ô `guest` (không JWT) trả **401** `SCH-004` — **trừ** `POST /schools/register` trả **201**
- Ô `✅ School` **không** đủ để truy cập tài nguyên trường khác — xem `MF02-TC-SEC-002`
- **Không** ô nào trả **404 vì thiếu route** (nghĩa là endpoint chưa tồn tại) — assert phân biệt 404-not-found với 403-forbidden
- 403 xảy ra **trước** khi thực thi nghiệp vụ: assert DB **không đổi** sau mỗi lần 403 (guard đặt ở controller, không phải sau `save()`)
- **Coverage assertion:** số tổ hợp đã chạy == **119** (7 role × 17 endpoint) — không bỏ sót ô nào

**Expected Result (FAIL = lỗ hổng tồn tại):**
- `parent`/`supplier` đọc được `GET /classrooms` → rò danh sách lớp + tên GVCN cho tài khoản ngoài phạm vi
- `system-admin` tạo/sửa được lớp hoặc năm học → phá BR-007 (§16 ghi rõ: quản trị trường toàn nền tảng là System Admin; **lớp + năm học chỉ School Administration**); System Admin thao tác dữ liệu nghiệp vụ của trường mà không ai chịu trách nhiệm
- `school-admin` gọi được `/coordinate/unlock` → phá BR-028 (*"**Chỉ System Admin**"*)
- `teacher` gọi được `GET /schools` → xem hồ sơ **mọi** trường trên nền tảng
- Endpoint thiếu `@PreAuthorize` hoàn toàn → **CWE-862**; mọi JWT hợp lệ đều dùng được

**Current Status:** 🔴 Not written
**Implementation Note:** viết `@MethodSource` đọc **trực tiếp** từ một bảng hằng phản chiếu §16 — khi §16 đổi thì sửa **một** chỗ. Đây là bản mẫu cho 11 module còn lại.

---

#### `MF02-TC-SEC-002` — Cách ly cross-school: School Admin trường A không đọc/ghi được tài nguyên trường B

**Severity:** `CRITICAL`
**OWASP:** `A01:2021 — Broken Access Control` (IDOR)
**CWE:** `CWE-639 — Authorization Bypass Through User-Controlled Key`
**Feature Under Test:** mọi endpoint có tài nguyên thuộc trường — tầng lọc `schoolId`
**Test File:** `src/test/java/com/sfms/mf02/security/CrossSchoolIsolationIT.java`
**TDD Phase:** 🔴 RED — ✅ **Oracle chốt 2026-07-31 (404 SCH-404)**
**Condition Ref:** `TC-COND-026`
**Oracle Source:** **BR-022** nguyên văn — *"Người dùng chỉ có thể xem chính sách của **trường mà họ thuộc về** (theo schoolId trong JWT)... **mọi truy cập cross-school bị từ chối**"* · **Chốt 2026-07-31: 404 `SCH-404`/MSG-137** (TDS §10/§13/§16), nhất quán với MF01 `requireInScope()`

**Preconditions:** `FX-031` (School Admin **trường A** `SCH-HN-0042`) · tài nguyên **trường B**: `FX-002` (school `SCH-HN-0051`), `FX-009` (`AY-2025-B`), `FX-018` (lớp `9Z`), config của trường B

**Test Steps (Attack Simulation — IDOR bằng cách đổi mã trong URL):**

| # | Request bằng JWT `FX-031` (trường A) | Tài nguyên đích |
|---|---|---|
| a | `GET /api/v1/classrooms/9Z` | lớp trường B |
| b | `PATCH /api/v1/classrooms/9Z` `{"capacity":40}` | lớp trường B |
| c | `PATCH /api/v1/classrooms/9Z/homeroom-teacher` `{"teacherId":"STF-004"}` | lớp trường B |
| d | `GET /api/v1/academic-years/AY-2025-B` | năm học trường B |
| e | `POST /api/v1/academic-years/AY-2026-B/activate` | năm học trường B |
| f | `POST /api/v1/academic-years/AY-2026-B/close` | năm học trường B |
| g | `GET /api/v1/schools/SCH-HN-0051/config` | config trường B |
| h | `PATCH /api/v1/schools/SCH-HN-0051/config` `{"geofenceRadius":500}` | config trường B |
| i | `GET /api/v1/classrooms` (không tham số) | assert **chỉ** lớp trường A |
| j | `GET /api/v1/academic-years` | assert **chỉ** năm học trường A |
| k | `GET /api/v1/classrooms?schoolId=SCH-HN-0051` | thử **ép** phạm vi bằng query param |
| l | `POST /api/v1/classrooms` `{"id":"7A","schoolId":"SCH-HN-0051",...}` | thử **ép** phạm vi bằng body |

**Expected Result (PASS = hệ thống an toàn):**
- **Phần bắt buộc (không phụ thuộc L7 — chạy được ngay):**
  - a–h: **không** trả 2xx; response **không chứa bất kỳ field dữ liệu nào** của trường B (assert body không chứa `"SCH-HN-0051"`, `"9Z"`, `"AY-2025-B"`, tên/`taxId`/`email` của trường B)
  - b, c, e, f, h: **DB không đổi** — `SELECT capacity, homeroom_teacher_id, read_only FROM classrooms WHERE code='9Z'` và `status` của `AY-2026-B` giữ nguyên; `geofence_radius` của trường B giữ nguyên
  - i: `total` == số lớp **trường A** (10), `items` **không** chứa `9Z`
  - j: `items` **không** chứa `AY-2025-B`/`AY-2026-B`
  - k: `schoolId` trong query **bị bỏ qua** → kết quả **giống hệt** dòng i (không phải danh sách trường B, cũng không phải rỗng)
  - l: nếu 201 thì lớp `7A` **phải** thuộc **trường A** (`school_id` = A) — `schoolId` trong body **bị bỏ qua** (xem `MF02-TC-SEC-004`)
- **Phần mã lỗi (chốt 2026-07-31):** a–h đều assert đúng một nhánh: `404` + `code="SCH-404"` + `message` = **MSG-137 nguyên văn**

**Expected Result (FAIL = lỗ hổng tồn tại):**
- Bất kỳ dòng a–h trả **2xx** → **rò dữ liệu chéo giữa các trường**; dòng b/c/e/f/h còn là **ghi** dữ liệu trường khác: School Admin trường A đóng được năm học của trường B ⇒ toàn bộ lớp trường B chuyển read-only, trường B **ngưng vận hành**
- Dòng i/j trả lớp/năm của **mọi** trường → mọi màn danh sách rò dữ liệu; đây là lỗi nguy hiểm nhất vì **im lặng** (không ai báo lỗi, chỉ là danh sách dài hơn bình thường)
- Dòng k/l làm đổi phạm vi theo tham số client → **CWE-639**, cách ly multi-tenant bị vô hiệu

**Current Status:** 🔴 Not written — ✅ đã mở khóa, sẵn sàng implement
**Implementation Note:** cách ly phải ở **tầng Specification/repository** (mọi query **luôn** có `AND school_id = :actorSchoolId`), **không** ở tầng controller if/else — bỏ sót một endpoint là rò một endpoint.

---

#### `MF02-TC-SEC-003` — Thiếu / hết hạn / sai chữ ký JWT → 401

**Severity:** `HIGH`
**OWASP:** `A07:2021 — Identification and Authentication Failures`
**CWE:** `CWE-287 — Improper Authentication` · `CWE-345 — Insufficient Verification of Data Authenticity`
**Feature Under Test:** JWT filter (nền chung MF01) áp lên endpoint MF02
**Test File:** `src/test/java/com/sfms/mf02/security/JwtValidationIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-027`
**Oracle Source:** **MSG-004** *"Session expired. Please log in again."* → `SCH-004`, HTTP 401 (TDS §10) · **BR-004** (access token 30 phút) · TDS §15.2 (*"Không JWT → 401"*)

**Preconditions:** `FX-039` (token hết hạn, token sai chữ ký, token rỗng)

**Test Steps (Attack Simulation):**

| # | Header `Authorization` | Expected |
|---|---|---|
| a | *(không gửi)* | 401 `SCH-004` |
| b | `Bearer <token exp ở quá khứ>` | 401 `SCH-004` |
| c | `Bearer <token đúng payload, sai chữ ký>` | **401** — **không** được chấp nhận |
| d | `Bearer <token alg=none, không chữ ký>` | **401** — chống tấn công "alg confusion" |
| e | `Bearer ` (rỗng) | 401 |
| f | `Basic dXNlcjpwYXNz` (sai scheme) | 401 |
| g | `Bearer <token hợp lệ nhưng thiếu claim `schoolId`>` với `GET /classrooms` | **401 hoặc 403** — **không** được coi `schoolId=null` là "xem mọi trường" |
| h | `Bearer <token hợp lệ role=school-admin>` | 200 (đối chứng) |

**Expected Result (PASS = hệ thống an toàn):**
- a–f: HTTP **401**, `{"error":{"code":"SCH-004","message":"Session expired. Please log in again.","details":[]}}` — **MSG-004 nguyên văn**
- c/d: **tuyệt đối không** 2xx — chữ ký phải được xác minh, `alg=none` bị từ chối
- g: **không** trả danh sách lớp của mọi trường; `schoolId=null` với role `school-admin` là token không hợp lệ về mặt nghiệp vụ
- Response 401 **không** rò thông tin: không nêu token sai ở đâu, không echo lại token
- ⚠️ **Ghi nhận (không chặn):** MSG-004 (*"Session expired"*) là **thông điệp duy nhất** có trong `error_message.md` v1.5 cho nhóm 401, nên nó cũng được dùng cho trường hợp **chưa từng có session** (dòng a/e/f) — nội dung hơi lệch ngữ nghĩa. Không tự đặt MSG mới (§2c-3b); ghi lại để Huy quyết định có cần MSG riêng "Authentication required" ở v1.6.

**Expected Result (FAIL = lỗ hổng tồn tại):**
- Dòng c/d trả 2xx → **giả mạo được mọi role**, bao gồm `system-admin` → toàn bộ RBAC vô nghĩa (**CWE-345**)
- Dòng g trả danh sách toàn hệ thống → cách ly multi-tenant bị vô hiệu bằng một token thiếu claim
- Dòng a trả 403 thay vì 401 → FE không phân biệt "cần đăng nhập" với "không đủ quyền" → không tự redirect về trang login

**Current Status:** 🔴 Not written

---

#### `MF02-TC-SEC-004` — Identity **chỉ** đến từ JWT, không tin body/query (AP-AI-006)

**Severity:** `CRITICAL`
**OWASP:** `A01:2021 — Broken Access Control` · `A04:2021 — Insecure Design`
**CWE:** `CWE-807 — Reliance on Untrusted Inputs in a Security Decision`
**Feature Under Test:** mọi controller MF02 — nguồn của `schoolId` / `role` / `userId`
**Test File:** `src/test/java/com/sfms/mf02/security/JwtAuthorityIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-028`
**Oracle Source:** **TDS §17.1 C2** — *"`schoolId`/`role`/`userId` **PHẢI** đọc từ JWT (AuthUser) — **KHÔNG tin body/query**"* · **AP-AI-006** (§17.4: *"Trust FE identity → Reject"*) · **BR-022** · **BR-007**

**Preconditions:** `FX-031` (School Admin trường A) · trường B tồn tại

**Test Steps (Attack Simulation — mỗi dòng là một cách nói dối):**

| # | Request (JWT = `FX-031`, trường A, role `school-admin`) | Ý đồ tấn công |
|---|---|---|
| a | `POST /classrooms` body `{"id":"7A","grade":7,"capacity":40,"schoolId":"SCH-HN-0051"}` | tạo lớp **trong trường B** |
| b | `POST /classrooms` body `{...,"academicYearId":"AY-2025-B"}` | gắn lớp vào **năm học trường B** |
| c | `GET /classrooms?schoolId=SCH-HN-0051` | đọc lớp trường B qua query |
| d | `POST /academic-years` body `{"id":"AY-2030","schoolId":"SCH-HN-0051",...}` | tạo năm học **cho trường B** |
| e | `POST /classrooms` body `{...,"role":"system-admin"}` | leo thang quyền qua body |
| f | `PATCH /classrooms/3A/homeroom-teacher` body `{"teacherId":"STF-004","actor":"admin@sfms.vn"}` | **giả mạo actor** trong audit |
| g | `POST /schools/{code}/coordinate/unlock` body `{"reason":"x","role":"system-admin"}` | vượt guard "chỉ System Admin" |
| h | `POST /classrooms` body `{...,"readOnly":false}` trên lớp thuộc năm CLOSED | ép bỏ cờ read-only |
| i | `POST /classrooms` body `{"id":"7A",...,"students":50,"allergies":9}` | ép **field dẫn xuất** |

**Expected Result (PASS = hệ thống an toàn):**
- a: lớp `7A` được tạo (nếu hợp lệ) với `school_id` = **trường A** — `schoolId` trong body **bị bỏ qua hoàn toàn**; assert `SELECT school_id FROM classrooms WHERE code='7A'` = A
- b: **từ chối** (năm học `AY-2025-B` không thuộc trường A → không tìm thấy trong phạm vi) — **không** tạo lớp trỏ năm học trường khác
- c: kết quả **chỉ** trường A (giống `SEC-002` dòng k)
- d: năm học được tạo cho **trường A**, hoặc bị từ chối — **không bao giờ** thuộc trường B
- e/g: **403** — `role` lấy từ JWT, body không nâng quyền được; `/coordinate/unlock` vẫn 403 với School Admin
- f: audit log ghi `actor` = `FX-031.userId`, **không** phải `"admin@sfms.vn"` từ body → assert nội dung bản ghi audit
- h: cờ `readOnly` do **server** quyết định theo trạng thái năm học; body không ghi đè được → vẫn 409
- i: `students`/`allergies` trong body **bị bỏ qua**; response trả **0** (đếm runtime — ADR-M02-2), **không** phải 50/9
- **Không** endpoint nào nhận `schoolId`/`role`/`userId`/`actor` từ input như một tham số có hiệu lực

**Expected Result (FAIL = lỗ hổng tồn tại):**
- a/d thành công vào trường B → **ghi dữ liệu chéo tenant** bằng một field trong body; đây là lỗ hổng nghiêm trọng nhất trong nhóm này vì không cần token của trường B
- e/g thành công → **leo thang quyền**: bất kỳ School Admin trở thành System Admin, mở được khóa tọa độ (BR-028 vô hiệu)
- f ghi `actor` từ body → **audit trail giả mạo được** ⇒ BR-026/BR-021 mất giá trị pháp lý; khi điều tra sự cố ATTP, nhật ký không còn đáng tin
- i thành công → field dẫn xuất bị client bơm số liệu → phá ADR-M02-2 và làm sai báo cáo MF13

**Current Status:** 🔴 Not written
**Implementation Note:** DTO request **không được** khai các field `schoolId`/`role`/`userId`/`actor`/`students`/`allergies`/`readOnly` (nếu khai, Jackson nhận rồi lập trình viên dễ dùng nhầm). Lấy từ `AuthUser` inject qua `@AuthenticationPrincipal`.

---

#### `MF02-TC-SEC-005` — `POST /schools/register` là public nhưng có rate limit

**Severity:** `MEDIUM`
**OWASP:** `A04:2021 — Insecure Design` (thiếu chống lạm dụng)
**CWE:** `CWE-770 — Allocation of Resources Without Limits or Throttling`
**Feature Under Test:** `POST /api/v1/schools/register`
**Test File:** `src/test/java/com/sfms/mf02/security/RegisterRateLimitIT.java`
**TDD Phase:** 🔴 RED — ✅ **Oracle chốt 2026-07-31 (`SCH-014`/MSG-141)**
**Condition Ref:** `TC-COND-029`
**Oracle Source:** **TDS §9.1** — `POST /api/v1/schools/register` · Auth `Public` · Roles `Guest` · **Rate Limit `5/min/IP`** · **§16** (Guest ✅) · **`error_message.md` v1.8 — MSG-141**, TDS §10 — `SCH-014`

**Preconditions:** không JWT (`FX-038`); rate limiter bật ở profile test với cùng ngưỡng 5/min/IP

**Test Steps (Attack Simulation):**
1. Gửi **5** request `POST /schools/register` hợp lệ (mỗi lần `taxId` khác nhau) từ cùng IP trong 1 phút
2. Gửi request **thứ 6** từ cùng IP
3. Gửi request từ **IP khác** trong cùng phút
4. Sau khi hết cửa sổ 1 phút, gửi lại từ IP ban đầu

**Expected Result (PASS = hệ thống an toàn):**
- Request 1–5: **201** (không cần JWT — đúng §16)
- Request **thứ 6**: **HTTP 429**, `error.code = "SCH-014"`, `error.message` = **MSG-141 nguyên văn**, envelope **D2** hợp lệ (`details` là mảng). **DB:** `SELECT COUNT(*) FROM schools` **không** tăng ở request thứ 6
- Bước 3: IP khác **vẫn** 201 → giới hạn theo **IP**, không phải toàn cục
- Bước 4: sau cửa sổ → 201 trở lại
- Header `Retry-After` có mặt ở response 429 (thực hành tốt, không phải yêu cầu tài liệu)

**Expected Result (FAIL = lỗ hổng tồn tại):**
- Không có giới hạn → endpoint public cho phép tạo **hàng nghìn** bản ghi trường `PENDING` → System Admin bị ngập trong hàng đợi phê duyệt (UC-001); còn là đường tiêu tốn dung lượng DB không kiểm soát
- Giới hạn áp toàn cục (không theo IP) → một người đăng ký làm **mọi người khác** không đăng ký được
- 429 trả shape phẳng `{"message":...}` → lệch D2

**Current Status:** 🔴 Not written — ✅ đã mở khóa toàn bộ, sẵn sàng implement

---

#### `MF02-TC-SEC-006` — BR-028: mở khóa tọa độ **chỉ** System Admin, bắt buộc lý do, bắt buộc audit

**Severity:** `HIGH`
**OWASP:** `A01:2021 — Broken Access Control`
**CWE:** `CWE-285 — Improper Authorization`
**Feature Under Test:** `POST /api/v1/schools/{code}/coordinate/unlock`
**Test File:** `src/test/java/com/sfms/mf02/security/CoordinateUnlockAuthzIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-010`
**Oracle Source:** **BR-028** nguyên văn — *"**Ngoại lệ mở khóa**: **Chỉ System Admin** mới có quyền đặt lại `coordinateLocked = false`... bắt buộc phải kèm **lý do bằng văn bản** và được ghi vào **audit log** (người thực hiện, thời gian, lý do)"* · **TDS §16** (dòng `POST /schools/{id}/coordinate/unlock`: chỉ `system-admin` ✅) · **MSG-005** → `SCH-005` · TDS §13 TC-E2E-002

**Preconditions:** `FX-001` (`coordinateLocked=true`)

**Test Steps (Attack Simulation):**

| # | Actor | Body | Expected |
|---|---|---|---|
| a | `school-admin` (`FX-031`) — **chủ trường đó** | `{"reason":"cần sửa tọa độ"}` | **403 SCH-005** — chủ trường **cũng không** được |
| b | `teacher` (`FX-033`) | `{"reason":"..."}` | 403 |
| c | `medical-staff` (`FX-034`) | `{"reason":"..."}` | 403 |
| d | `service-caterers` (`FX-035`) | `{"reason":"..."}` | 403 |
| e | `supplier` (`FX-036`) | `{"reason":"..."}` | 403 |
| f | `parent` (`FX-037`) | `{"reason":"..."}` | 403 |
| g | guest (không JWT) | `{"reason":"..."}` | 401 |
| h | `system-admin` (`FX-030`) | `{"reason":"hiệu chỉnh sai số GPS ±40m"}` | **200** |
| i | `system-admin` | `{"reason":""}` | 422 MSG-138 (xem `MF02-TC-010`) |

**Expected Result (PASS = hệ thống an toàn):**
- a–f: **403** `SCH-005` với **MSG-005 nguyên văn**; **DB:** `coordinate_locked` vẫn `true`; **không** audit log nào được ghi (thao tác bị chặn trước khi vào service)
- g: 401 `SCH-004`
- h: 200, `coordinate_locked=false`, **đúng 1** bản ghi audit với `actor` từ JWT + `reason` nguyên văn + `timestamp` `Instant` (D4)
- i: 422, `coordinate_locked` vẫn `true`
- Sau h, gọi lại h lần nữa (đã unlock rồi) → **không** sinh thêm bản ghi audit trùng lặp vô nghĩa, hoặc nếu sinh thì phải là bản ghi riêng có lý do riêng — hành vi phải **xác định**

**Expected Result (FAIL = lỗ hổng tồn tại):**
- Dòng a thành công → School Admin tự mở khóa rồi đổi tọa độ trường mình → **BR-028 mất toàn bộ hiệu lực**: mốc geofence mà MF06/07/08 dùng để xác minh vị trí giao hàng/kiểm thực/lấy mẫu có thể bị chính trường đó dịch chuyển ⇒ mọi kiểm tra vị trí trở thành hình thức
- Bị chặn nhưng **vẫn** ghi audit log "SUCCESS" → nhật ký sai sự thật
- Cho phép unlock mà không ghi audit → mất bằng chứng cho thao tác nhạy cảm nhất của module

**Current Status:** 🔴 Not written

---

#### `MF02-TC-SEC-007` — ★ Gate BR-025 phải ở **server**: gọi API trực tiếp, bỏ qua UI

**Severity:** `CRITICAL`
**OWASP:** `A04:2021 — Insecure Design` (client-side enforcement)
**CWE:** `CWE-602 — Client-Side Enforcement of Server-Side Security`
**Feature Under Test:** `POST /api/v1/classrooms` · `PATCH /api/v1/classrooms/{code}` · `POST /api/v1/academic-years/{code}/promote`
**Test File:** `src/test/java/com/sfms/mf02/security/ServerSideGateBypassIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-030`, `TC-COND-002`
**Oracle Source:** **BR-025** (*"sức chứa tối đa **50 học sinh**"*) · **MSG-010** → `SCH-010` · **Prompt §2c-3b** yêu cầu **bắt buộc**: *"Với module có GATE nghiệp vụ enforce phía server, PHẢI có test 'gọi API trực tiếp bỏ qua UI': ... **MF02 (BR-025 capacity)**. Đây là nhóm lỗi mà review 29/07 xếp nghiêm trọng nhất: gate chỉ tồn tại ở client thì gọi API trực tiếp là lách được toàn bộ luật ATTP."*

> ★ Test case này được prompt sinh tài liệu **chỉ định đích danh** cho MF02. Mọi bước dùng **HTTP client thô** (REST Assured / `MockMvc`), **không** đi qua form FE — mô phỏng đúng kẻ tấn công dùng `curl`/Postman với JWT hợp lệ của chính mình.

**Preconditions:** `FX-031` (School Admin trường A — **JWT hoàn toàn hợp lệ**, không phải kẻ ngoài) · `FX-005` (AY-2025 ACTIVE) · `FX-011` (3A `capacity=45`, `students=38`) · `FX-017` (6B `capacity=45`, `students=44`)

**Test Steps (Attack Simulation — 8 đường lách):**

| # | Request thô (JWT hợp lệ, không qua UI) | Đường lách |
|---|---|---|
| a | `POST /classrooms` `{"id":"9X","grade":3,"capacity":51}` | vượt trần 50 trực tiếp |
| b | `POST /classrooms` `{"id":"9Y","grade":3,"capacity":9999}` | giá trị cực lớn |
| c | `POST /classrooms` `{"id":"9Z","grade":3,"capacity":"51"}` | **kiểu chuỗi** thay vì số (type juggling) |
| d | `POST /classrooms` `{"id":"9W","grade":3,"capacity":50.9}` | **số thực** làm tròn lên |
| e | `POST /classrooms` `{"id":"9V","grade":3,"capacity":-1}` | giá trị âm (né `> 50`) |
| f | `PATCH /classrooms/3A` `{"capacity":51}` | vượt trần qua **update** (đường thường bị bỏ sót) |
| g | `POST /academic-years/AY-2026/promote` mapping đẩy 44 HS vào `6B` (còn 1 chỗ) | vượt trần **hàng loạt** |
| h | `POST /classrooms` `{"id":"9U","grade":13,"capacity":40}` | lách luôn BR-024 qua cùng đường |

**Expected Result (PASS = hệ thống an toàn):**
- a, b, f, g: **409 `SCH-010`** với **MSG-010 nguyên văn**; **DB:** không bản ghi nào có `capacity > 50` → `SELECT COUNT(*) FROM classrooms WHERE capacity > 50` = **0**
- c: **không** được chấp nhận — hoặc 400/422 (Jackson từ chối), hoặc coerce về `51` rồi **409**; **tuyệt đối không** lưu
- d: **không** lưu `capacity = 51`; hoặc 422, hoặc từ chối số thực
- e: **422/409** — CHECK `capacity BETWEEN 1 AND 50` chặn; không lưu `-1`
- h: **422 `SCH-024`** (MSG-109) — BR-024 cũng phải enforce ở server
- **DB assertion tổng (bất biến sau toàn bộ tấn công):**
```sql
SELECT COUNT(*) FROM classrooms WHERE capacity > 50 OR capacity < 1;              -- Expected: 0
SELECT COUNT(*) FROM classrooms WHERE grade NOT BETWEEN 1 AND 12;                 -- Expected: 0
-- khớp TDS §14.1 Database Inspection
```
- **Lớp phòng thủ:** kể cả khi validation ở service bị bỏ sót, CHECK constraint ở DB vẫn chặn — nhưng khi đó client nhận **500**, nên test **cũng** assert **mã lỗi đúng** (409/422), không chỉ assert "không lưu được"

**Expected Result (FAIL = lỗ hổng tồn tại):**
- Bất kỳ dòng nào lưu được `capacity > 50` → **BR-025 chỉ tồn tại ở client**: form FE chặn nhưng `curl` thì không. Hệ quả lan xuống chuỗi: MF05 tính số suất theo sĩ số lớp, MF09 phân suất theo lớp → lớp 90 HS trong sức chứa 45 làm sai số suất ăn, sai số mẫu lưu (MF08), sai định mức nguyên liệu (MF06)
- Dòng f lưu được (chỉ chặn ở `create`, quên `update`) → đây là biến thể hay gặp nhất: người viết code kiểm ở `create` rồi tin rằng `update` "chỉ sửa vài field"
- Dòng g lưu được → một request làm sai **cả khối**
- Chặn được nhưng trả **500** → bất biến giữ, nhưng FE hiện "Lỗi hệ thống", người dùng không biết phải sửa gì

**Current Status:** 🔴 Not written
**Implementation Note:** validate ở **service** (không chỉ Bean Validation trên DTO — service có thể được gọi từ event handler/job không qua DTO); CHECK ở DB là lớp cuối. Ba lớp: DTO → service → DB.

---

#### `MF02-TC-SEC-008` — System Admin **không** được ghi dữ liệu nghiệp vụ của trường

**Severity:** `HIGH`
**OWASP:** `A01:2021 — Broken Access Control`
**CWE:** `CWE-863 — Incorrect Authorization`
**Feature Under Test:** `POST/PATCH /classrooms` · `PATCH /classrooms/{code}/homeroom-teacher` · `POST /academic-years` + `/activate` + `/close` + `/promote`
**Test File:** `src/test/java/com/sfms/mf02/security/SystemAdminBoundaryIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-025`
**Oracle Source:** **TDS §16** — các dòng `POST/PATCH /classrooms`, `PATCH /classrooms/{id}/homeroom-teacher`, `POST /academic-years · /activate · /close · /promote` đều ghi **`❌` cho SYS ADMIN** · **§16 ghi chú:** *"**BR-007:** quản trị trường toàn nền tảng chỉ System Admin; **lớp + năm học chỉ School Administration** (trường mình)"* · **MSG-005** → `SCH-005`

> Đây là ô dễ hiện thực sai nhất trong §16: lập trình viên thường mặc định "System Admin làm được mọi thứ". §16 nói **ngược lại** cho nhóm endpoint này.

**Preconditions:** `FX-030` (System Admin, `schoolId=null`) · fixture trường A đầy đủ

**Test Steps (Attack Simulation):**

| # | Request bằng JWT `FX-030` (System Admin) | Expected |
|---|---|---|
| a | `POST /classrooms` `{"id":"8A","grade":8,"capacity":40}` | **403 SCH-005** |
| b | `PATCH /classrooms/3A` `{"capacity":40}` | **403** |
| c | `PATCH /classrooms/3A/homeroom-teacher` `{"teacherId":"STF-004"}` | **403** |
| d | `POST /academic-years` `{"id":"AY-2031",...}` | **403** |
| e | `POST /academic-years/AY-2026/activate` | **403** |
| f | `POST /academic-years/AY-2025/close` | **403** |
| g | `POST /academic-years/AY-2026/promote` `{...}` | **403** |
| h | `GET /classrooms` · `GET /academic-years` · `GET /classrooms/3A` | **200 (All)** — đọc **được** toàn nền tảng (§16) |
| i | `PATCH /schools/SCH-HN-0042` · `/status` · `/coordinate/unlock` · `GET/PATCH /schools/{code}/config` | **200** — đây **là** phạm vi của System Admin |

**Expected Result (PASS = hệ thống an toàn):**
- a–g: **403** `SCH-005` với **MSG-005 nguyên văn**; **DB không đổi** ở từng dòng (assert cụ thể: không có lớp `8A`, `3A.capacity` vẫn 45, `3A.homeroom_teacher_id` không đổi, `AY-2026` vẫn DRAFT, `AY-2025` vẫn ACTIVE)
- h: 200 và **không** bị giới hạn theo trường (`schoolId=null` ⇒ thấy lớp/năm học của **cả 4 trường**) — phân biệt rõ với `school-admin` (chỉ trường mình)
- i: 200 — quyền quản trị trường **vẫn** nguyên

**Expected Result (FAIL = lỗ hổng tồn tại):**
- a–g thành công → phá phân định trách nhiệm của BR-007: System Admin (vai trò kỹ thuật, không thuộc trường nào) sửa được lớp/đóng được năm học của trường ⇒ **không xác định được ai chịu trách nhiệm** khi dữ liệu học vụ sai; đồng thời `schoolId=null` khiến bản ghi tạo ra **không gắn trường nào** → dữ liệu mồ côi
- Dòng e/f thành công → System Admin đóng năm học của một trường bất kỳ → toàn bộ lớp trường đó read-only, trường ngưng vận hành
- h bị 403 → System Admin không xem được dữ liệu để giám sát nền tảng (ngược lại §16)

**Current Status:** 🔴 Not written

---

#### `MF02-TC-SEC-009` — UC-137 / BR-022: `GET /schools/me/policy` chỉ trả trường của chính actor

**Severity:** `HIGH`
**OWASP:** `A01:2021 — Broken Access Control` (IDOR)
**CWE:** `CWE-639 — Authorization Bypass Through User-Controlled Key`
**Feature Under Test:** `GET /api/v1/schools/me/policy` · `GET /api/v1/schools/{code}/config`
**Feature Under Test (guard):** `SchoolPolicyService.getForCurrentUser(AuthUser)`
**Test File:** `src/test/java/com/sfms/mf02/security/SchoolPolicyIsolationIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-026`, `TC-COND-025`
**Oracle Source:** **BR-022** nguyên văn — *"Người dùng chỉ có thể xem chính sách của **trường mà họ thuộc về** (theo schoolId trong JWT). **Teacher, Medical Staff, Service Caterers** có quyền xem chính sách của trường mình để thực hiện công việc hàng ngày"* · **UC-137** (traceability §1.2: BR-022, MSG-004/005) · **TDS §16** (dòng `GET /schools/me/policy`) · **CF-M02-1** (UC-137 thiếu UC spec chi tiết — thiết kế theo BR-022)

**Preconditions:** `FX-033` (Teacher trường A), `FX-034` (Medical Staff trường A), `FX-035` (Service Caterers trường A), `FX-036` (Supplier), `FX-037` (Parent) · `FX-026` (config trường A) · config trường B

**Test Steps (Attack Simulation):**

| # | Request | Expected |
|---|---|---|
| a | `GET /schools/me/policy` bằng `FX-033` (Teacher trường A) | **200** — chỉ dữ liệu trường A |
| b | `GET /schools/me/policy` bằng `FX-034` (Medical Staff) | **200** — trường A |
| c | `GET /schools/me/policy` bằng `FX-035` (Service Caterers) | **200** — trường A |
| d | `GET /schools/me/policy` bằng `FX-036` (Supplier) | **403 SCH-005** (§16: ❌) |
| e | `GET /schools/me/policy` bằng `FX-037` (Parent) | **403 SCH-005** |
| f | `GET /schools/me/policy?schoolId=SCH-HN-0051` bằng `FX-033` | **200** nhưng **vẫn** là trường A — query bị bỏ qua |
| g | `GET /schools/SCH-HN-0051/config` bằng `FX-033` (Teacher) | **403** — §16: Teacher ❌ trên `/config` |
| h | `GET /schools/SCH-HN-0042/config` bằng `FX-033` (Teacher, **trường mình**) | **403** — §16 cho Teacher ❌ trên `/config` **kể cả** trường mình; policy đọc qua `/me/policy` |
| i | `GET /schools/SCH-HN-0051/config` bằng `FX-031` (School Admin trường A) | **403/404** (cross-school — xem `SEC-002`/L7) |
| j | `GET /schools/me/policy` bằng token thiếu claim `schoolId` | **401/403** — không được trả policy của trường bất kỳ |

**Expected Result (PASS = hệ thống an toàn):**
- a–c: 200; body **chỉ** chứa dữ liệu trường A (assert có `"SCH-HN-0042"` hoặc `geofenceRadius=200` của trường A và **không** chứa `"SCH-HN-0051"`); ba role này **đều** đọc được (BR-022 nêu đích danh)
- d/e: 403 `SCH-005` với **MSG-005 nguyên văn**
- f: tham số `schoolId` **không** có hiệu lực — endpoint `/me/policy` lấy trường **duy nhất** từ JWT
- g/h: 403 — endpoint `/config` dành cho System Admin (All) + School Admin (School); Teacher dùng `/me/policy`
- j: **không** trả 200 với dữ liệu của một trường nào đó (VD trường đầu tiên trong DB)
- Body của `/me/policy` **không** rò field nhạy cảm ngoài phạm vi "chính sách": **không** có `taxId`, **không** có `emergencyContact` — assert tường minh (Teacher/Caterers không cần mã số thuế của trường)

**Expected Result (FAIL = lỗ hổng tồn tại):**
- f trả policy trường B → Teacher trường A đọc được cấu hình trường B (bán kính geofence, email nhận báo cáo) → rò thông tin vận hành giữa các trường; với `service-caterers` (nhà thầu phục vụ nhiều trường) đây là **rò dữ liệu giữa các khách hàng cạnh tranh**
- d/e trả 200 → Parent/Supplier đọc cấu hình nội bộ trường
- j trả 200 → token thiếu claim vẫn lấy được dữ liệu
- Body chứa `taxId`/`emergencyContact` → rò dữ liệu `Confidential` cho role không cần biết (nguyên tắc Least Privilege, TDS §16)

**Current Status:** 🔴 Not written

---

#### `MF02-TC-SEC-010` — SQL Injection qua `search` / filter param

**Severity:** `HIGH`
**OWASP:** `A03:2021 — Injection`
**CWE:** `CWE-89 — SQL Injection`
**Feature Under Test:** `SchoolSpec.build()` · `ClassroomSpec.build()` — nơi nhận `Map<String,String> allParams`
**Test File:** `src/test/java/com/sfms/mf02/security/SearchInjectionIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-021`
**Oracle Source:** **ADR-M02-1** + **Team Plan §2b-4** (`Specification` với `cb.like`/`cb.equal` ⇒ **luôn** tham số hóa) · **§2b-2** (`<field>=<value>` và `not:<field>=<value>` — **tên field đến từ client**, đây là bề mặt tấn công) · **§2b-7** checklist của Mạnh
**Technique:** Error Guessing

> ⚠️ Điểm rủi ro đặc thù của hợp đồng §2b: `root.get(key)` nhận **`key` do client gửi**. Chuỗi không hợp lệ không gây SQLi (JPA metamodel sẽ throw) nhưng có thể gây **500 + rò tên field/schema**, và với triển khai native/JPQL nối chuỗi thì **là** SQLi thật.

**Test Steps (Attack Simulation):**

| # | Request | Ý đồ |
|---|---|---|
| a | `GET /schools?search=' OR '1'='1` | bypass điều kiện |
| b | `GET /schools?search=%'; DROP TABLE classrooms; --` | phá hoại schema |
| c | `GET /schools?search=' UNION SELECT tax_id FROM schools --` | rút dữ liệu cột khác |
| d | `GET /classrooms?grade=3 OR 1=1` | injection qua exact filter |
| e | `GET /classrooms?not:grade=3; DELETE FROM classrooms` | injection qua filter phủ định |
| f | `GET /classrooms?homeroomTeacherId=x` | lọc theo field **không** trong `SEARCH_FIELDS` nhưng **có** trong entity |
| g | `GET /classrooms?khong_ton_tai=1` | field **không tồn tại** trong entity |
| h | `GET /classrooms?not:khong_ton_tai=1` | như trên, nhánh `not:` |
| i | `GET /schools?search=<script>alert(1)</script>` | XSS lưu trữ qua tham số |
| j | `GET /classrooms?pageSize=999999999` | cạn tài nguyên |
| k | `GET /schools?search=` + chuỗi 10.000 ký tự | quá tải / lỗi 500 |

**Expected Result (PASS = hệ thống an toàn):**
- a–e: **200** với `total=0` (chuỗi tấn công được xử lý như **dữ liệu tìm kiếm bình thường**, không phải SQL); **DB assertion sau toàn bộ tấn công:** `SELECT COUNT(*) FROM classrooms` = **10**, bảng `classrooms`/`schools` **vẫn tồn tại** (`SELECT to_regclass('classrooms') IS NOT NULL` → true)
- c: response **không** chứa giá trị `tax_id` của bất kỳ trường nào
- d/e: **hoặc** 200 `total=0`, **hoặc** 400/422 vì `grade` không parse được — **không** 500, **không** thực thi
- f: **hành vi phải xác định** — theo §2b-2, filter exact áp cho *"field bất kỳ của entity"*, nên `homeroomTeacherId=x` là **hợp lệ về hợp đồng** → 200 `total=0`. Ghi nhận: điều này nghĩa là client **enumerate được** field của entity; không phải lỗ hổng nghiêm trọng nhưng cần biết
- g/h: **400/422** với envelope **D2** — **không** 500, và `error.message`/`details` **không** rò tên cột/bảng/câu SQL/tên class Java (assert body không chứa `"classrooms"`, `"Classroom"`, `"org.hibernate"`, `"IllegalArgumentException"`)
- i: 200 `total=0`; chuỗi **không** được lưu vào DB; nếu có echo lại trong response thì phải là JSON-escaped (Jackson mặc định làm)
- j: `pageSize` bị **clamp** về ngưỡng tối đa hợp lý (hoặc 422) — **không** cố tải toàn bộ bảng vào bộ nhớ
- k: **không** 500; xử lý bình thường hoặc 400 vì vượt độ dài cho phép

**Expected Result (FAIL = lỗ hổng tồn tại):**
- Bất kỳ bảng bị xóa/đổi, hoặc `total` = toàn bộ bản ghi ở dòng a/d → **SQLi thật** (**CWE-89**): xảy ra nếu triển khai theo "Option B — JPQL/native" của §2b-4 mà nối chuỗi thay vì bind tham số
- Dòng c trả về `tax_id` → rò dữ liệu `Confidential` qua endpoint danh sách
- Dòng g/h trả **500** kèm `PropertyNotFoundException: could not resolve property 'khong_ton_tai' of 'com.sfms.mf02.domain.Classroom'` → **rò tên package, tên class, tên cột** — bản đồ schema cho kẻ tấn công (đây là hậu quả trực tiếp của việc `root.get(key)` nhận key từ client mà không whitelist)
- Dòng j làm hết bộ nhớ → DoS bằng một request

**Current Status:** 🔴 Not written
**Implementation Note:** **whitelist** tên field được phép lọc (không truyền thẳng `key` từ `allParams` vào `root.get()`); dùng **Option A** (`Specification` + `cb.like`/`cb.equal` — tham số hóa sẵn), tránh Option B nối chuỗi cho MF02; clamp `pageSize` ở ngưỡng trần (VD 200).

---

## 5. Red-Green-Refactor Tracker

> **45 test case** — 17 Unit · 18 Integration · 10 Security. Cột `🔴 RED confirmed` phải tick **trước** khi implement (xem §5.1).
> Ký hiệu ⛔ = blocked bởi Logic Issue ở §2, **không** đưa vào Exit Criteria cho tới khi được chốt.

| TC ID | Test File | Severity | Oracle | 🔴 RED | 🟢 GREEN (commit) | 🔵 REFACTOR |
|---|---|---|---|---|---|---|
| `MF02-TC-001` | `service/ClassroomServiceValidationTest.java` | HIGH | BR-024 / MSG-109 | `[ ]` | | |
| `MF02-TC-002` | `service/ClassroomServiceValidationTest.java` | HIGH | BR-025 / MSG-010 | `[ ]` | | |
| `MF02-TC-003` | `service/ClassroomServiceValidationTest.java` | MEDIUM | ✅ L8b chốt 2026-07-31 (BR-219/SCH-012) | `[ ]` | | |
| `MF02-TC-004` | `service/ClassroomServiceValidationTest.java` | HIGH | BR-192 / MSG-081 | `[ ]` | | |
| `MF02-TC-005` | `service/HomeroomTeacherAssignmentTest.java` | HIGH | BR-020 / MSG-011 | `[ ]` | | |
| `MF02-TC-005b` | `service/HomeroomTeacherAssignmentTest.java` | **CRITICAL** | ✅ L3 chốt 2026-07-31 (ADR-M02-5) | `[ ]` | | |
| `MF02-TC-006` | `service/HomeroomTeacherAssignmentTest.java` | HIGH | BR-020 + BR-026 / MSG-034 | `[ ]` | | |
| `MF02-TC-007` | `service/SchoolCoordinateTest.java` | HIGH | BR-028 / MSG-107 | `[ ]` | | |
| `MF02-TC-008` | `service/SchoolRegistrationTest.java` | MEDIUM | MSG-007 / UC-070 | `[ ]` | | |
| `MF02-TC-009` | `service/SchoolCoordinateTest.java` | HIGH | BR-028 / MSG-108 | `[ ]` | | |
| `MF02-TC-010` | `service/SchoolCoordinateTest.java` | HIGH | BR-028 / MSG-138 | `[ ]` | | |
| `MF02-TC-011` | `service/SchoolConfigServiceTest.java` | MEDIUM | BR-028 / MSG-138 | `[ ]` | | |
| `MF02-TC-012` | `service/AcademicYearLifecycleTest.java` | **CRITICAL** | BR-191 / MSG-080 | `[ ]` | | |
| `MF02-TC-013` | `service/AcademicYearCreateTest.java` | HIGH | BR-194 / MSG-082 | `[ ]` | | |
| `MF02-TC-014` | `service/AcademicYearLifecycleTest.java` | HIGH | BR-193 / MSG-083 | `[ ]` | | |
| `MF02-TC-015` | `service/DerivedCountTest.java` | MEDIUM | ADR-M02-2 / CF-M02-6 | `[ ]` | | |
| `MF02-TC-016` | `service/ClientSuppliedIdTest.java` | HIGH | D5 · ✅ L9 chốt 2026-07-31 (SCH-409/MSG-139) | `[ ]` | | |
| `MF02-TC-INT-001` | `web/AcademicYearActivateIT.java` | **CRITICAL** | BR-191 / MSG-080 | `[ ]` | | |
| `MF02-TC-INT-002` ★ | `web/AcademicYearActivateIT.java` | **CRITICAL** | BR-191 per-school / CF-M02-3 | `[ ]` | | |
| `MF02-TC-INT-003` | `web/AcademicYearConcurrencyIT.java` | **CRITICAL** | BR-191 / ADR-M02-4 | `[ ]` | | |
| `MF02-TC-INT-004` | `web/ClassroomCreateIT.java` | HIGH | BR-192 / MSG-081 | `[ ]` | | |
| `MF02-TC-INT-005` | `web/AcademicYearCloseIT.java` | HIGH | BR-193 + BR-195 | `[ ]` | | |
| `MF02-TC-INT-006` | `web/AcademicYearCreateIT.java` | HIGH | BR-194 / MSG-082 | `[ ]` | | |
| `MF02-TC-INT-007` | `web/AcademicYearPromoteIT.java` | HIGH | BR-194 / MSG-084 / UC-177 | `[ ]` | | |
| `MF02-TC-INT-008` | `web/AcademicYearPromoteIT.java` | HIGH | BR-025 / MSG-010 / D7 | `[ ]` | | |
| `MF02-TC-INT-009` | `web/PagedResponseContractIT.java` | HIGH | Team Plan §2b-1/2/6 | `[ ]` | | |
| `MF02-TC-INT-010` | `repository/SearchFieldsIT.java` | MEDIUM | Team Plan §2b-3 / ADR-M02-1 | `[ ]` | | |
| `MF02-TC-INT-011` | `web/IdentityContractIT.java` | HIGH | **D1** · ✅ L1 chốt 2026-07-31 (§5.2 có `code`) | `[ ]` | | |
| `MF02-TC-INT-012` | `web/DateFormatContractIT.java` | HIGH | **D4** | `[ ]` | | |
| `MF02-TC-INT-013` | `web/ErrorEnvelopeContractIT.java` | HIGH | **D2 + D7** / §10 | `[ ]` | | |
| `MF02-TC-INT-014` | `web/SchoolCoordinateLifecycleIT.java` | HIGH | BR-028 / §6.4 | `[ ]` | | |
| `MF02-TC-INT-015` | `web/SchoolRegisterIT.java` | HIGH | MSG-032 ✅ · L9 chốt 2026-07-31 (SCH-409/MSG-139) | `[ ]` | | |
| `MF02-TC-INT-016` | `web/ArchivedAcademicYearIT.java` | MEDIUM | BR-195 / UC-176 | `[ ]` | | |
| `MF02-TC-INT-017` | `web/SchoolDeactivateCascadeIT.java` | HIGH | BR-021 / UC-001 | `[ ]` | | |
| `MF02-TC-INT-018` | `web/SchoolConfigIT.java` | MEDIUM | BR-028 / MSG-029 / UC-095 | `[ ]` | | |
| `MF02-TC-SEC-001` | `security/AuthorizationMatrixIT.java` | **CRITICAL** | §16 / BR-007 / MSG-005 | `[ ]` | | |
| `MF02-TC-SEC-002` | `security/CrossSchoolIsolationIT.java` | **CRITICAL** | BR-022 · ✅ L7 chốt 2026-07-31 (404 SCH-404) | `[ ]` | | |
| `MF02-TC-SEC-003` | `security/JwtValidationIT.java` | HIGH | MSG-004 / BR-004 | `[ ]` | | |
| `MF02-TC-SEC-004` | `security/JwtAuthorityIT.java` | **CRITICAL** | §17.1 C2 / AP-AI-006 | `[ ]` | | |
| `MF02-TC-SEC-005` | `security/RegisterRateLimitIT.java` | MEDIUM | §9.1 · ✅ chốt 2026-07-31 (SCH-014/MSG-141) | `[ ]` | | |
| `MF02-TC-SEC-006` | `security/CoordinateUnlockAuthzIT.java` | HIGH | BR-028 / §16 / MSG-005 | `[ ]` | | |
| `MF02-TC-SEC-007` ★ | `security/ServerSideGateBypassIT.java` | **CRITICAL** | BR-025 / prompt §2c-3b | `[ ]` | | |
| `MF02-TC-SEC-008` | `security/SystemAdminBoundaryIT.java` | HIGH | §16 / BR-007 | `[ ]` | | |
| `MF02-TC-SEC-009` | `security/SchoolPolicyIsolationIT.java` | HIGH | BR-022 / UC-137 / §16 | `[ ]` | | |
| `MF02-TC-SEC-010` | `security/SearchInjectionIT.java` | HIGH | §2b-4 / §2b-7 | `[ ]` | | |

**Thống kê:** 45 test case (17 Unit · 18 Integration · 10 Security) · **9 CRITICAL** · 28 HIGH · 8 MEDIUM · **0 blocked** — toàn bộ 6 test case từng ⛔ (`005b`, `016`, `INT-015`, `SEC-002`, `003`, `SEC-005`) đã mở khóa 2026-07-31. **2 đánh dấu ★** (bắt buộc pass trước final approve: `INT-002` chốt bất biến per-school cho 11 module copy, `SEC-007` là gate server-side mà prompt chỉ định đích danh).

### 5.1 Red Gate Protocol (CASE 2.0 — GATE-2)

> ⭐ **Gate quan trọng nhất.** Trước khi implement, chạy toàn bộ suite với stub **ném exception**. **Mọi** test PHẢI FAIL. Nếu test PASS ngay → **AP-AI-002 (Green-from-Birth)** → reject và rewrite.

**Stub cho Red Phase (Java — thay cho ví dụ TypeScript của template):**

```java
// src/main/java/com/sfms/mf02/service/impl/ClassroomServiceRedStub.java
// Red Phase — stub PHẢI throw. Xóa sau khi GATE-2 pass.
@Service @Profile("red-gate")
public class ClassroomServiceRedStub implements ClassroomService {
  private static final String MSG = "Not implemented — Red Phase stub";
  @Override public PagedResponse<ClassroomResponse> list(UUID s, Map<String,String> p) { throw new UnsupportedOperationException(MSG); }
  @Override public ClassroomDetailResponse getDetail(UUID id, UUID s)                  { throw new UnsupportedOperationException(MSG); }
  @Override public ClassroomResponse create(CreateClassroomRequest r, AuthUser a)      { throw new UnsupportedOperationException(MSG); }
  @Override public ClassroomResponse update(UUID id, UpdateClassroomRequest r, AuthUser a) { throw new UnsupportedOperationException(MSG); }
  @Override public ClassroomResponse assignHomeroomTeacher(UUID id, UUID t, AuthUser a){ throw new UnsupportedOperationException(MSG); }
}
```

**Chạy Red Gate:**

```bash
./mvnw test -Dspring.profiles.active=test,red-gate -Dsurefire.failIfNoSpecifiedTests=false \
  | tee target/red-gate-evidence.log
# Kỳ vọng: 45 test, 45 FAIL (0 pass, 0 skip)
```

**Red Gate Verification:**

| Nhóm TC | Stub Result | Expected | Actual | Root Cause (nếu PASS bất thường) |
|---|---|---|---|---|
| `MF02-TC-001` … `-016` (Unit, 17 — gồm `TC-005b`) | `throw UnsupportedOperationException` | 🔴 FAIL | ☐ FAIL ☐ PASS | ☐ Tautology ☐ Shared state ☐ Hallucinated contract |
| `MF02-TC-INT-001` … `-018` (Integration, 18) | controller trả 501 / service throw | 🔴 FAIL | ☐ FAIL ☐ PASS | ☐ Assert quá lỏng (chỉ `status < 500`) ☐ Không assert DB |
| `MF02-TC-SEC-001` … `-010` (Security, 10) | **⚠️ chú ý đặc biệt** — xem dưới | 🔴 FAIL | ☐ FAIL ☐ PASS | ☐ **False-green vì stub cũng 403/401** |

> ⚠️ **Bẫy Red Gate riêng của test security:** test kỳ vọng **403/401** rất dễ **PASS ngay** với stub — vì khi chưa cấu hình `@PreAuthorize`, Spring Security mặc định có thể chặn **mọi** request và trả đúng 403/401 mà test mong đợi. Đây là **AP-AI-002 trá hình**, khó phát hiện nhất trong tài liệu này.
>
> **Cách chống (bắt buộc):** mỗi test security phải có **≥1 assertion dương** — một ô `✅` trong cùng test method (VD `MF02-TC-SEC-001` dòng h, `MF02-TC-SEC-008` dòng h/i, `MF02-TC-SEC-009` dòng a–c). Với stub "chặn tất", assertion dương **FAIL** ⇒ test FAIL đúng như Red Gate đòi. **Không** viết test security chỉ gồm assertion 403.

**Red Gate Evidence:**

- Stub commit hash: `___`
- Tất cả FAIL? ☐ Yes → **GATE-2 PASS** (T2→T3) → tiếp tục implement
- Log file: `target/red-gate-evidence.log`
- Số test chạy: `___` / **45** (khớp §5) · Số FAIL: `___` · Số PASS bất thường: `___` (phải = **0**)

> **Nếu bất kỳ test PASS:** dừng lại, xác định root cause từ bảng trên, rewrite test từ TC spec với Props Isolation Pattern (§3 TDS-05).

---

## 6. Entry / Exit Criteria

### Entry Criteria (điều kiện bắt đầu viết test)

- [ ] `TDS-MF02_Backend.md` (`SFMS-MF02-IMP-001`) chuyển **Approved** — Huy (§2), Mạnh (§5+§9), Minh (§8+§9 shape), Thảo (§13), Nam (final)
- [ ] `00_Common_Conventions.md` D1–D7 ở trạng thái **Approved** ✅ (đã đạt 2026-07-30)
- [x] **Logic Issues §2 đã được confirm** với người phụ trách tương ứng — **L3, L7, L9, L8b đã chốt 2026-07-31**; gap riêng của `SEC-005` (429) cũng đã đóng cùng ngày — **0 test case còn ⛔**
- [ ] **§5.2 TDS bổ sung cột `code`** (L1) — nếu không, `MF02-TC-INT-011` không thể pass
- [ ] Nền chung của Nam sẵn sàng: `PagedResponse<T>`, `@RestControllerAdvice` (envelope D2), `common/enums/` (D6), JWT filter phát `AuthUser{userId, role, schoolId}` (MF01)
- [ ] Flyway `V1__init_schema.sql` chứa 4 bảng MF02 + **toàn bộ** CHECK/partial unique index ở §5.2 (`uq_ay_one_active_per_school`, `uq_homeroom_teacher_per_year`, CHECK grade/capacity/lat/lng/geofence)
- [ ] `V{n}__seed_mf02.sql` sinh từ `mock-data.ts` **đã chuẩn hóa D1–D7**: ngày → ISO (**D4**), enum → SCREAMING_SNAKE (**D6**), `academic_years` **có `school_id`** (CF-M02-3), thêm `FX-008` (ARCHIVED) + `FX-009`/`FX-010` (năm học trường B) + `FX-016`/`FX-017`/`FX-018` — **không** có trong mock, phải tổng hợp
- [ ] Docker khả dụng cho Testcontainers (`postgres:16-alpine` pull được)

### Exit Criteria (điều kiện kết thúc — DoD)

- [ ] `./mvnw test` — **toàn bộ unit test xanh**, không `@Disabled`, không skip
- [ ] `./mvnw verify -Pintegration` — **toàn bộ integration + security test xanh**
- [ ] **45/45** test case 🟢 PASS (0 test còn ⛔ — mọi Logic Issue §2 + gap `SEC-005` đã đóng 2026-07-31)
- [ ] **★ `MF02-TC-INT-002` PASS** — bất biến BR-191 là **per-school**; đây là điều kiện chặn cứng vì 11 module sau copy convention của MF02
- [ ] **★ `MF02-TC-SEC-007` PASS** — gate BR-025 enforce ở server, gọi API trực tiếp không lách được
- [ ] **`MF02-TC-SEC-001` chạy đủ 119 tổ hợp** (7 role × 17 endpoint), không bỏ sót ô nào của §16
- [ ] JaCoCo coverage **≥ 80% lines** cho `com.sfms.mf02` (service + web); riêng `service/` **≥ 90%** (nơi chứa BR)
- [ ] **Truy vết ngược đủ:** 16/16 UC của traceability §1.2 có ≥1 test case (bảng §3 TDS-03) · 12/12 BR có ≥1 test case
- [ ] **Kiểm D1–D7 bằng grep** (checklist Team Plan §2c-3):
  ```bash
  grep -rn "JsonFormat" src/main/java/com/sfms/mf02/   # Expected: rỗng (D4)
  grep -rnE "[0-9]{2}/[0-9]{2}/[0-9]{4}" src/main/java/com/sfms/mf02/  # Expected: rỗng (D4)
  grep -rn "enum MealType\|enum RoleKey\|enum AllergySeverity" src/main/java/com/sfms/mf02/  # Expected: rỗng (D6 — không khai lại)
  ```
- [ ] **Bất biến DB xanh** (TDS §14.1 — chạy sau khi toàn bộ test kết thúc):
  ```sql
  SELECT school_id, COUNT(*) FROM academic_years WHERE status='ACTIVE' GROUP BY school_id HAVING COUNT(*)>1;  -- 0 rows
  SELECT academic_year_id, homeroom_teacher_id, COUNT(*) FROM classrooms WHERE homeroom_teacher_id IS NOT NULL
    GROUP BY 1,2 HAVING COUNT(*)>1;                                                                            -- 0 rows
  SELECT id FROM classrooms WHERE grade NOT BETWEEN 1 AND 12 OR capacity NOT BETWEEN 1 AND 50;                 -- 0 rows
  ```
- [ ] **Không** `Object`/`Map<String,Object>` thay cho DTO có kiểu trong production code MF02
- [ ] Response API **không** rò UUID nội bộ, stack trace, tên constraint DB (assert trong `INT-011`, `INT-013`, `SEC-010`)
- [ ] Thảo (Lead Test/QA) xác nhận coverage happy + error path đủ cho **cả 3 lớp**; Mạnh xác nhận checklist §2b-7; Nam final approve → TestSpec chuyển **Approved**

**Exit Criteria bổ sung — CASE 2.0:**

- [ ] **Red Gate (§5.1)** — 45/45 test FAIL với stub, `target/red-gate-evidence.log` được commit
- [ ] **Contract Existence** — mọi import trong test file resolve được:
  ```bash
  ./mvnw -q test-compile   # Expected: BUILD SUCCESS, không "cannot find symbol"
  ```
- [ ] **Props Isolation** — không shared mutable state:
  ```bash
  grep -rnE "^\s*(private|protected)?\s*static\s+.*(School|Classroom|AcademicYear)\s+\w+\s*=" src/test/java/com/sfms/mf02/
  # Expected: rỗng — mọi instance phải tạo trong @Test qua Mf02Fixtures.*()
  ```
- [x] **Oracle Source** — mọi expected value trong assert truy được về BR/UC/MSG/D1–D7; **0** test case có expected "tự nghĩ ra"; mọi chỗ thiếu oracle đã đánh ⛔ ở §2 (**L3, L7, L9, L8b đã chốt 2026-07-31**)
- [ ] **Assertion dương trong test security** — mỗi test `SEC-*` có ≥1 ô `✅` (chống false-green, §5.1)

### Suspension Criteria (điều kiện tạm dừng)

- ~~**L3** (bảng đích `homeroom_teacher_id`) chưa chốt~~ ✅ đã chốt 2026-07-31 (`user_accounts.id`) — `TC-005b`/`-005`/`-006`/`INT-005` không còn bị treo
- ~~**L7** (403 vs 404 cross-school) chưa chốt~~ ✅ đã chốt 2026-07-31 (404) — `SEC-002` chạy đầy đủ
- ~~**L9** (MSG cho 409 trùng mã) chưa cấp~~ ✅ đã chốt 2026-07-31 (`SCH-409`/MSG-139) — `TC-016`, `INT-015` không còn treo. `SEC-005` (429) cũng đã đóng cùng ngày (`SCH-014`/MSG-141)
- Nền chung của Nam (`PagedResponse`, `@RestControllerAdvice`, JWT filter) chưa xong → toàn bộ `INT-*`/`SEC-*` không chạy được
- Flyway V1 thiếu partial unique index → `INT-002`, `INT-003` mất lớp chốt chặn DB, kết quả không có giá trị
- Docker/Testcontainers không khả dụng trên CI → 26 test `INT-*`/`SEC-*` bị chặn hạ tầng
- Phát hiện quyết định kiến trúc mới cần Nam review (VD phải tách `ADR-MF02_*.md` riêng)

---

## 7. Rollback Plan

```bash
# ── Hoàn tác migration (CHỈ môi trường dev/test — KHÔNG chạy trên production) ──
./mvnw -pl app flyway:clean          # xóa toàn bộ schema test
./mvnw -pl app flyway:migrate        # dựng lại V1 + seed từ đầu
# Nếu chỉ lỗi ở V{n} seed MF02: khôi phục từ backup theo TDS §12.2
#   psql -h [host] -U sfms sfms < backup_20260728.sql

# ── Testcontainers: không cần rollback (container ephemeral) ──
docker ps -a --filter "label=org.testcontainers" -q | xargs -r docker rm -f

# ── Hoàn tác code test ──
git checkout -- src/test/java/com/sfms/mf02/
git checkout -- 07_implement/MF02_School_Class_Management/TestSpec-MF02_School_Class_Management.md

# ── Xóa stub Red Phase sau khi GATE-2 pass ──
git rm src/main/java/com/sfms/mf02/service/impl/*RedStub.java
```

**Nếu phát hiện vi phạm bất biến trên môi trường đã deploy** (theo TDS §12.1 — điều kiện rollback):

| Triệu chứng | Kiểm tra | Hành động |
|---|---|---|
| Xuất hiện >1 năm ACTIVE/trường | truy vấn §6 Exit Criteria dòng 1 | Rollback theo TDS §12.2; báo Tech Lead; kiểm `uq_ay_one_active_per_school` còn tồn tại không |
| Cascade deactivate suspend nhầm trường khác | `MF02-TC-INT-017` dòng "trường B" | Rollback + **thông báo DPO** (TDS §12.3 — BR-021 ảnh hưởng tài khoản) |
| Lớp có `capacity > 50` trong DB | Exit Criteria dòng 3 | `SEC-007` đã FAIL ở đâu đó → gate chỉ ở client; hotfix service + chạy lại `SEC-007` |
| Dữ liệu trường A xuất hiện trong response của trường B | `SEC-002` dòng i/j | **Sự cố bảo mật** — chặn endpoint, báo Nam + DPO, không chỉ rollback |

> **Gap vẫn OPEN** → giữ nguyên entry trong §2 Logic Issues Resolved; **không** xóa dòng ⛔ khi chưa có quyết định (Policy 4.4 — Immutable History).

---

## 8. CASE 2.0 Anti-Pattern Detection

> Checklist cho reviewer (Thảo — Lead Test/QA) khi test case được AI hỗ trợ generate.

| AP-ID | Anti-Pattern | Dấu hiệu trong TestSpec này | Check | Gate chặn |
|---|---|---|---|---|
| AP-AI-001 | Unconstrained Generation | TC không reference BR/UC/MSG/D1–D7 nào ở dòng **Oracle Source** | ☐ | G-0 |
| AP-AI-002 | **Green-from-Birth** | Test PASS với stub ném exception (§5.1) — **đặc biệt** nhóm `SEC-*` kỳ vọng 403/401: xem bẫy false-green ở §5.1 | ☐ | **G-2 ★** |
| AP-AI-003 | Implicit Decision | Test assume quyết định kiến trúc không có trong TDS §3 / D1–D7 (VD tự chọn 404 cho cross-school khi L7 chưa chốt) | ☐ | G-1 |
| AP-AI-004 | Layer Violation | Test verify controller chứa business logic; hoặc unit test service lại đi qua HTTP | ☐ | G-4 |
| AP-AI-005 | Hallucinated Contract | Test import service/DTO không có trong TDS §8 (VD `SchoolPolicyService` sai chữ ký) | ☐ | G-3 |
| AP-AI-006 | **Trust FE identity** | Test chấp nhận `schoolId`/`role` từ body/query là hợp lệ → xem `MF02-TC-SEC-004` | ☐ | G-1 |
| AP-AI-007 | Wrong stack | Xuất hiện `npm test`, `.spec.ts`, `prisma`, `npx` trong TestSpec/test code | ☐ | G-0 |
| AP-AI-009 | Store derived field | Test assert cột vật lý `students`/`classes` tồn tại → xem `MF02-TC-015` | ☐ | G-1 |
| AP-AI-010 | **Global academic year** | Test chỉ assert "1 năm ACTIVE toàn hệ thống" thay vì per-school → xem `MF02-TC-INT-002` | ☐ | **G-2 ★** |
| AP-AI-011 | **Fabricated oracle** *(bổ sung cho MF02)* | Expected message/mã lỗi **không** có trong `error_message.md` mà không đánh dấu ⛔ — VD dịch MSG sang tiếng Việt, hoặc tự đặt mã ngoài bảng §10 đã chốt. (`SCH-409`/MSG-139, `SCH-012`/MSG-140, `SCH-014`/MSG-141 đều đã chốt 2026-07-31 — không còn là ví dụ fabricated) | ☐ | G-0 |
| AP-AI-012 | **Assert quá lỏng** *(bổ sung cho MF02)* | Chỉ assert HTTP status mà **không** assert `error.code` + `message` nguyên văn + **DB state** — test xanh nhưng không chứng minh gì | ☐ | G-2 |

**Kết quả review:**

- [ ] Không phát hiện anti-pattern nào → TestSpec **approved**
- [ ] Phát hiện AP → ghi vào bảng dưới → fix **trước** khi implement

| AP detected | TC ID | Mô tả | Fix action | Fixed? |
|---|---|---|---|---|
| `AP-AI-___` | `MF02-TC-___` | | | ☐ |

**Tự kiểm khi soạn tài liệu này (Nam):**

| Kiểm | Kết quả |
|---|---|
| Mọi TC có dòng **Oracle Source** trích BR/UC/MSG/D-code cụ thể? | ✅ 45/45 |
| Có TC nào dùng expected "tự nghĩ ra" không đánh dấu? | ✅ Không — 4 chỗ thiếu oracle ban đầu đều đánh ⛔ và ghi người chịu trách nhiệm; **L3, L7, L9, L8b đã chốt 2026-07-31** (Nam) |
| Mọi expected message là **tiếng Anh nguyên văn** `error_message.md` v1.5 (D7)? | ✅ — đã đối chiếu từng dòng MSG-001/004/005/007/010/011/080/081/107/108/109/137/138 và MSG-029/032/033/034/082/083/084 |
| Có dùng `npm`/Prisma/TypeScript của template gốc? | ✅ Không — đã dịch toàn bộ sang Java/Maven/Flyway/Testcontainers |
| Định dạng expected theo **D1–D7**, không theo mock? | ✅ — mock chỉ dùng làm **mã fixture** (`SCH-HN-0042`, `AY-2025`, `3A`, `STF-003`); ngày/enum/envelope/`id` theo D1–D7 |
| Tên luật `semantic-audit.ts` copy nguyên văn từ script? | ✅ — **không có luật MF02 nào**; xem Phụ lục C (kèm bằng chứng grep), **không** trích tên luật từ TDS §13 |
| Có test "gọi API trực tiếp bỏ qua UI" cho gate BR-025 (yêu cầu bắt buộc của §2c-3b)? | ✅ `MF02-TC-SEC-007` (8 đường lách) |
| Đủ 3 lớp Unit · Integration · Security? | ✅ 17 · 18 · 10 |

---

## PHỤ LỤC A — Danh sách UC / BR / MSG đã trích dẫn

> Yêu cầu §2c-3b: *"Cuối tài liệu liệt kê: danh sách UC/BR/MSG đã trích + danh sách tên luật semantic-audit đã port."*
> **Đã đối chiếu:** danh sách dưới đây khớp **100%** với `traceability_matrix.md` §1.2 và với "DANH SÁCH MÃ ĐÃ TRÍCH DẪN" ở cuối `TDS-MF02_Backend.md` — không thêm, không thiếu, không bịa mã.

**Use Cases (16/16 của §1.2)** — mỗi UC có ≥1 test case (ma trận truy vết ngược ở §3 TDS-03):

`UC-001` Active/Deactivate School · `UC-008` Assign Homeroom Teacher · `UC-018` Create Classroom · `UC-037` Manage Classrooms · `UC-070` Register School · `UC-090` Update Classroom · `UC-095` Update Information School · `UC-122` View Information School · **`UC-137`** View Policy Of School · `UC-140` View School List · `UC-165` View Classroom Detail · `UC-166` View List Classrooms · `UC-174` Create Academic Year · `UC-175` Activate/Close Academic Year · `UC-176` View Academic Year List · `UC-177` Promote Students to Next Academic Year

*(⚠️ `UC-137` có trong traceability §1.2 nhưng **thiếu UC spec chi tiết** — **CF-M02-1**; test `MF02-TC-SEC-009` thiết kế theo **BR-022**, chờ Huy bổ sung UC spec.)*

**Business Rules (12/12)** — nguồn `business_rules.md` §3 + §19:

| BR | Dùng làm oracle ở |
|---|---|
| `BR-020` | `TC-005`, `TC-005b`, `TC-006`, `INT-005`, `INT-017` |
| `BR-021` | `INT-017` |
| `BR-022` | `SEC-002`, `SEC-009`, `TC-005` (dòng e) |
| `BR-024` | `TC-001`, `SEC-007` (dòng h) |
| `BR-025` | `TC-002`, `INT-008`, `SEC-007` |
| `BR-026` | `TC-006`, `INT-017` |
| `BR-028` | `TC-007`, `TC-008`, `TC-009`, `TC-010`, `TC-011`, `INT-014`, `INT-018`, `SEC-006` |
| `BR-191` | `TC-012`, `INT-001`, **`INT-002`**, `INT-003` |
| `BR-192` | `TC-004`, `INT-004` |
| `BR-193` | `TC-014`, `INT-005` |
| `BR-194` | `TC-013`, `INT-006`, `INT-007` |
| `BR-195` | `INT-005` (bước 4), `INT-016` |

*(`BR-004` được trích **gián tiếp** ở `SEC-003` vì MSG-004 gắn BR-004 — token 30 phút thuộc **MF01**, MF02 chỉ dùng làm expected của 401. `BR-007` là nguồn của ma trận §16 → `SEC-001`, `SEC-008`. **BR-030 KHÔNG dùng** — bị UC-095 cite nhầm, xem CF-M02-2. **BR-080** `costCeiling` thuộc **MF05**, chỉ tham chiếu ở `INT-018` dưới dạng "MF02 không kiểm luật".)*

**System Messages** — nguồn `error_message.md` **v1.5**, dùng làm **expected message nguyên văn** (D7):

| MSG | Nội dung (nguyên văn, tiếng Anh) | Mã `SCH-*` | Test case |
|---|---|---|---|
| `MSG-001` | System error while saving school data. Please try again. | `SCH-001` | `INT-013` |
| `MSG-004` | Session expired. Please log in again. | `SCH-004` | `SEC-003`, `INT-013` |
| `MSG-005` | Access denied: You do not have permission to access this resource. | `SCH-005` | `SEC-001`, `SEC-002`, `SEC-006`, `SEC-008`, `SEC-009`, `INT-013` |
| `MSG-007` | Invalid tax ID format. Please verify and try again. | `SCH-007` | `TC-008`, `INT-013`, `INT-015` |
| `MSG-010` | Classroom capacity exceeded. Maximum allowed is 50 students. | `SCH-010` | `TC-002`, `INT-008`, `INT-013`, `SEC-007` |
| `MSG-011` | Homeroom teacher is already assigned to another classroom. | `SCH-011` | `TC-005`, `TC-005b`, `INT-013` |
| `MSG-080` | Cannot create Active academic year: an Active academic year already exists for this school. Please close it first. | `SCH-080` | `TC-012`, `INT-001`, `INT-003`, `INT-013` |
| `MSG-081` | Cannot create classroom: this school has no Active academic year. Please activate an academic year first. | `SCH-081` | `TC-004`, `INT-004`, `INT-013` |
| **`MSG-107`** | Invalid GPS coordinates. Latitude must be between -90 and 90, longitude between -180 and 180. | `SCH-013` (400) | `TC-007`, `INT-013` |
| **`MSG-108`** | School coordinates are locked. Only a System Administrator can change them. | `SCH-013` (403) | `TC-009`, `INT-013`, `INT-014` |
| **`MSG-109`** | Grade level must be between 1 and 12. | `SCH-024` | `TC-001`, `INT-013`, `SEC-007` |
| `MSG-137` | Resource not found. | `SCH-404` | `INT-011`, `INT-013`, `SEC-002` (cross-school, chốt 2026-07-31) |
| `MSG-138` | Validation failed: one or more required fields are missing or invalid. | *(validation)* | `TC-010`, `TC-011`, `INT-013`, `INT-018` |
| `MSG-139` *(v1.6, chốt 2026-07-31)* | A record with this value already exists. | `SCH-409` | `TC-016` (client `id` trùng), `INT-015` (`taxId` trùng) |
| `MSG-140` *(v1.7, chốt 2026-07-31)* | Classroom capacity cannot be reduced below the current number of enrolled students. | `SCH-012` | `TC-003` (BR-219) |
| `MSG-141` *(v1.8, chốt 2026-07-31)* | Too many requests. Please try again later. | `SCH-014` | `SEC-005` (rate limit 429) |

**Success messages:** `MSG-029` (Profile updated successfully — `TC-009`, `INT-018`) · `MSG-032` (School registered successfully. Pending System Admin approval — `TC-008`, `INT-015`) · `MSG-033` (Classroom created successfully — `INT-004`) · `MSG-034` (Homeroom teacher assigned successfully — `TC-006`) · `MSG-082` (Academic year created successfully — `TC-013`, `INT-006`) · `MSG-083` (Academic year status updated successfully — `INT-001`, `INT-005`) · `MSG-084` (Students promoted to next academic year successfully — `INT-007`)

> ✅ **Tất cả MSG đã được cấp đủ (2026-07-31).** Còn 1 mục **tùy chọn**, không chặn GREEN: **Chưa đăng nhập** — hiện dùng MSG-004 "Session expired" cho cả trường hợp chưa từng có session (`SEC-003` ghi nhận, không phải lỗi).
>
> ✅ **Đã cấp 2026-07-31:** mã nghiệp vụ/`taxId` đã tồn tại → **MSG-139**/`SCH-409` (`error_message.md` v1.6) — dùng ở `TC-016`, `INT-015`.

**Quyết định chung / ADR đã trích:** `00_Common_Conventions.md` **D1** (`INT-011`, `TC-016`) · **D2** (`INT-013`) · **D3** (mọi path `INT-*`/`SEC-*`) · **D4** (`INT-012`) · **D5** (`TC-016`, `INT-015`) · **D6** (fixture + mọi assert enum) · **D7** (`INT-013` + toàn bộ expected message) · TDS §3 **ADR-M02-1** (`INT-009`, `INT-010`, `SEC-010`) · **ADR-M02-2** (`TC-015`, `INT-015`, `SEC-004` dòng i) · **ADR-M02-3** (`TC-009`, `TC-010`, `INT-014`, `SEC-006`) · **ADR-M02-4** (`TC-012`, `INT-002`, `INT-003`) · **ADR-M02-5** (`TC-005`, `TC-005b`, `INT-017`) · **ADR-000** (stack)

---

## PHỤ LỤC B — Xung đột nguồn của TDS được kế thừa (CF-M02-*)

> TDS §2.3 ghi 6 xung đột. Bảng dưới đối chiếu từng cái với TestSpec này để không có xung đột nào bị bỏ qua khi viết test.

| CF | Nội dung | Xử lý trong TestSpec |
|---|---|---|
| **CF-M02-1** | UC-137 có trong traceability §1.2 nhưng **thiếu UC spec chi tiết** | Test theo **BR-022** (`SEC-009`); Phụ lục A ghi rõ tình trạng. Chờ Huy bổ sung UC spec — **không chặn** test |
| **CF-M02-2** | UC-095 UC file cite **BR-030** (student-parent, sai ngữ cảnh); traceability chỉ có BR-028 | **Không** dùng BR-030 ở bất kỳ test case nào (xác nhận: grep "BR-030" trong tài liệu này = 0 lần ngoài dòng này và Phụ lục A). Test UC-095 chỉ dùng **BR-028** |
| **CF-M02-3** | BR-191 per-school nhưng mock thiếu `schoolId` + kiểm Active **toàn cục** | Nâng thành **L4** (§2) + test chuyên dụng **`MF02-TC-INT-002`** (★ CRITICAL) + fixture `FX-009`/`FX-010` (năm học trường B) + assert `indexdef` của partial unique index |
| **CF-M02-4** | UC-018: traceability bổ sung BR-192 + MSG-081 so với UC file | Test **theo traceability**: `TC-004`, `INT-004` dùng BR-192/MSG-081 |
| **CF-M02-5** | UC-001 UC file dán nhầm AF1 "Reassignment homeroom teacher" (thuộc UC-008) | Test cascade UC-001 (`INT-017`) **không** kiểm logic reassignment; reassignment chỉ ở `TC-006` (UC-008). Không test theo AF1 dán nhầm |
| **CF-M02-6** | Field `<<derived>>` — mock lưu sẵn số (`classes: 28`) | Test **`TC-015`** assert đếm runtime + assert `information_schema.columns` **không** có cột dẫn xuất; `INT-015` assert 3 số = 0 khi trường mới |

---

## PHỤ LỤC C — Luật `semantic-audit.ts` đã port (khai báo bắt buộc)

> Yêu cầu §2c-3b: *"port luật liên quan {MF} sang Gherkin"* + *"Tên luật PHẢI copy nguyên văn từ script, KHÔNG lấy từ TDS §13"*.

### C.1. Kết quả rà soát: **0 luật thuộc MF02**

**Số luật port sang test case MF02: 0.**

Đã rà **toàn bộ** `03_Design/08_FrontEnd/Web/scripts/semantic-audit.ts` bằng cách liệt kê từng tên luật đăng ký qua `check("...")` và đối chiếu entity mà mỗi luật kiểm. Bằng chứng:

```bash
cd 03_Design/08_FrontEnd/Web
grep -c 'check("' scripts/semantic-audit.ts
# → 54   (khớp con số "54 luật" ở Team Plan §2a)
grep -noE 'check\("[A-Z_0-9]+"' scripts/semantic-audit.ts
# → 54 tên luật, không tên nào kiểm School / Classroom / AcademicYear / SchoolConfig
```

**Phân bố 54 luật theo module** (để chứng minh không bỏ sót MF02): MF06 nhập/xuất/tồn kho (`XUAT_LO_HET_HAN`, `XUAT_TRUOC_KHI_NHAP`, `NHAP_LO_DA_HET_HAN`, `LO_KHONG_CO_PHIEU_NHAN`, `NGAY_NHAP_LO_KHAC_PHIEU_NHAN`, …) · MF08 mẫu lưu (`MAU_SAI_BUA`, `HAN_LUU_KHONG_DU_24H`, `GUI_LAB_TRUOC_KHI_LAY`, `HUY_MAU_TRUOC_HAN_24H`, `MAU_NGOAI_KHUNG_SU_CO`, …) · MF09 phân suất (`PHAT_SAI_NGAY`, `TRANG_THAI_VS_GIO_PHAT`, `PHAT_ROI_KHONG_CO_MON`, `HOC_SINH_SAI_LOP`, `TEN_KHONG_KHOP_HOC_SINH`, …) · MF07 kiểm thực/CAPA (`PHAT_SUAT_DU_KIEM_THUC_TRUOT`, `CAPA_TRUOC_SU_CO`, …) · MF11 sự cố (`MOC_TRUOC_KHI_SU_CO_XAY_RA`, `DONG_SU_CO_MA_CON_LO_KHOA`, …) · MF04 NCC/lô (`HSD_TRUOC_NGAY_SX`, `CHUYEN_GIAO_LO_KHAC_NCC`, `NHAN_HANG_SAU_KHI_CHUNG_NHAN_HET_HAN`, …) · MF05 thực đơn/chế độ ăn (`GUI_DUYET_SAU_NGAY_PHUC_VU`, `DA_DUYET_MA_VAN_VUOT_TRAN`, `BANG_NAU_MON_VUOT_SI_SO`, …) · MF03 dị ứng (`KHAI_DI_UNG_TRO_HOC_SINH_KHONG_TON_TAI`, `XAC_MINH_MUC_NANG_KHONG_CO_MINH_CHUNG`, …)

**Kết luận này khớp** với ghi chú cuối TDS §13: *"module MF02 không có luật riêng trong `semantic-audit.ts` (54 luật tập trung MF05/07/09...); các invariant BR-191/192/020/024/025 ở trên là oracle chính, port trực tiếp từ `business_rules.md`"*.

⇒ **Oracle của MF02 đến 100% từ `business_rules.md` + `error_message.md` + D1–D7**, không từ `semantic-audit.ts`. Đây là lý do §2 (Logic Issues) và các dòng "Oracle Source" trong §4 phải chặt: MF02 không có lớp kiểm chéo nào từ script audit.

### C.2. Ghi nhận về 3 luật chỉ có `add()` không có `check()`

§2c-3b nhắc: `PHIEU_TRO_MAU_KHONG_TON_TAI`, `MOC_TRO_SU_CO_KHONG_TON_TAI`, `LO_TRO_MAT_HANG_KHONG_TON_TAI` — *"cả 57 đều là luật thật, con số '54' chỉ là số `check()` đăng ký"*. **Cả 3 thuộc MF08/MF11/MF06** (mẫu lưu, mốc sự cố, lô hàng), **không** thuộc MF02 ⇒ không ảnh hưởng tài liệu này. Ghi lại để chủ module MF06/MF08/MF11 không bỏ sót khi viết TestSpec của họ.

### C.3. Một luật MF09 phụ thuộc định danh lớp của MF02 (ghi chú liên module)

`HOC_SINH_SAI_LOP` (MF09 — so `Student.class` với `StudentDistribution.class`) và `BANG_NAU_MON_VUOT_SI_SO` (MF05 — so số suất với sĩ số) **không phải luật MF02**, nhưng **tiêu thụ** dữ liệu MF02: **tên lớp** và **sĩ số**. Hệ quả cho MF02:

- **L2 đã chốt 2026-08-01** (giữ `code` bất biến + `name` nhãn hiển thị được đổi độc lập). Rủi ro còn lại cho MF05/MF09: nếu hai module đó tham chiếu lớp qua **`name`** (chuỗi hiển thị) thay vì `code`/`id`, một lần đổi tên lớp ở MF02 sẽ làm **lệch dữ liệu ở MF05/MF09 mà không có lỗi nào báo** (vì `name` không unique-enforced xuyên thời gian như `code`). Khuyến nghị cho chủ module MF05/MF09: liên kết bằng `classroomId`/`code`, chỉ dùng `name` để hiển thị.
- `MF02-TC-SEC-007` (không cho `capacity > 50`) và `MF02-TC-INT-008` (promote không vượt sức chứa) là **điều kiện thượng nguồn** để `BANG_NAU_MON_VUOT_SI_SO` có ý nghĩa: nếu sĩ số vượt sức chứa ngay từ MF02 thì luật MF05 đang so với một mốc đã sai.

---

## PHỤ LỤC D — Bản đồ nguồn ↔ mục tài liệu (cho reviewer đối chiếu nhanh)

| Nguồn (theo thứ tự ưu tiên §2c-3b) | Đã đọc / dùng ở |
|---|---|
| `00_Common_Conventions.md` (D1–D7) | §1.1, §2 (L1/L5/L6/L9), §3 TDS-02/TDS-05, `INT-011`/`INT-012`/`INT-013`, `TC-016`, Phụ lục A |
| `TDS-MF02_Backend.md` **§13** (xương sống) | 5 kịch bản gốc được mở rộng: §13.1 TC-UNIT-001 → `TC-001`+`TC-002` · §13.1 TC-UNIT-002 → `TC-005`+`TC-006` · §13.2 TC-INT-001 → `INT-001`+**`INT-002`**+`INT-003` · TC-INT-002 → `INT-004` · TC-INT-003 → `INT-005` · TC-INT-004 → `INT-006` · §13.3 TC-E2E-001 → `SEC-002`+`SEC-009` · TC-E2E-002 → `INT-014`+`SEC-006` |
| `TDS-MF02_Backend.md` **§9** (API Spec) | expected request/response của toàn bộ `INT-*`; §9.1 (17 endpoint, rate limit) → `SEC-001`, `SEC-005`; §9.2 → `INT-004`/`INT-006`/`INT-007`/`INT-008`/`INT-015` |
| `TDS-MF02_Backend.md` **§10** (mã lỗi) | `INT-013` (kích hoạt đủ 12 dòng) + mọi expected error path; phát hiện **L6** (SCH-013 hai nghĩa), **L9** (thiếu SCH-409) |
| `TDS-MF02_Backend.md` **§16** (auth matrix) | `SEC-001` (119 tổ hợp), `SEC-006`, `SEC-008`, `SEC-009` |
| `TDS-MF02_Backend.md` **§5.2** (DDL + CHECK) | assertion tầng DB ở `INT-002`/`INT-003`/`INT-011`/`SEC-007`; §3 TDS-02 (danh sách constraint); phát hiện **L1** (thiếu cột `code`), **L5** (kiểu `Instant` vs ví dụ date-only) |
| `TDS-MF02_Backend.md` §2.3 (CF-M02-*) | Phụ lục B (đối chiếu đủ 6 xung đột) |
| `TDS-MF02_Backend.md` §6.3/§6.4 (state machine) | `TC-012` (ma trận transition), `INT-014` (chuỗi 6 bước), `INT-016` (phát hiện: `CLOSED→ARCHIVED` **không có endpoint/job**) |
| `TDS-MF02_Backend.md` §7.1 (domain event) | assert event publish ở `TC-006`, `TC-012`, `INT-001`, `INT-014`, `INT-015`, `INT-017` |
| `TDS-MF02_Backend.md` §8 (interface Java) | tên method/chữ ký dùng trong toàn bộ `TC-*` unit; §8.2 (repository) → `TC-004`, `TC-005` |
| `TDS-MF02_Backend.md` §14 (xác minh) | Exit Criteria §6 (3 truy vấn bất biến DB) + `SEC-007` (assertion tổng) + `INT-009` (`jq 'keys'` → assert 5 field) |
| `TDS-MF02_Backend.md` §17 (constraint C1–C6) | `INT-009`/`INT-010` (C1) · `SEC-002`/`SEC-004` (C2) · `TC-012`/`INT-002` (C3) · `TC-001`…`TC-006` (C4) · `TC-007`…`TC-011` (C5) · `TC-015`/`INT-017` (C6); §17.4 → §8 (bảng anti-pattern, bổ sung AP-AI-011/012) |
| `business_rules.md` §3 + §19 | §3 TDS-02 (nguyên văn 12 BR) + dòng **Oracle Source** của từng test case + Phụ lục A |
| `error_message.md` **v1.5** | expected message **nguyên văn** (D7) — Phụ lục A bảng MSG; xác định 4 MSG **còn thiếu** |
| `traceability_matrix.md` **§1.2** | §3 TDS-02, ma trận truy vết ngược UC→TC (§3 TDS-03), Phụ lục A. **Đã đối chiếu:** khớp 100% với TDS §2.1 (16 UC, cột BR, cột MSG) |
| `semantic-audit.ts` | **Phụ lục C** — 0 luật MF02, kèm bằng chứng grep (54 `check()`) |
| `mock-data.ts` | **chỉ** làm fixture: §3 TDS-05 (`FX-001`…`FX-026`) — `SCHOOLS` (4 trường), `CLASSROOMS` (10 lớp), `ACADEMIC_YEARS` (3 năm), `SCHOOL_STAFF` (`STF-001`…`STF-009`), `SCHOOL_CONFIG`, `AUDIT_LOGS` (`AUD-98955` làm khuôn audit unlock). **Không** lấy định dạng ngày/enum từ đây |
| `mock-server.ts` | chỉ để **xác nhận L4**: handler `activate` kiểm ACTIVE toàn cục (dòng ~234) → nguồn của `INT-002` |
| `00_Team_Work_Assignment_Plan.md` §2b | `INT-009` (§2b-1/2/6), `INT-010` (§2b-3 + bảng SEARCH_FIELDS bản sửa 30/07), `SEC-010` (§2b-4/§2b-7) |
| `PHASE-4_Test-Spec.md` (template) | cấu trúc §1–§8; **đã dịch** mọi ví dụ TypeScript/Prisma/`npm` sang Java/Maven/Flyway/Testcontainers |

---

*TestSpec MF02 v1.0 — 45 test case (17 Unit · 18 Integration · 10 Security) · 9 Logic Issue (4 thiếu oracle, 1 mâu thuẫn nội bộ TDS) · 6 test blocked chờ chốt.*
*Đây là TestSpec đầu tiên của dự án — cấu trúc §2 (Logic Issues), §3 TDS-05 (fixture), §5.1 (Red Gate + bẫy false-green của test security), Phụ lục C (khai báo semantic-audit) là **convention cho 12 module còn lại copy** (Team Plan §4b: MF02 là vertical slice mẫu).*
