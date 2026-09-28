# TEST-DRIVEN DEVELOPMENT SPECIFICATION (TestSpec)
# MF01 — User Management & Access Control

| Field | Value |
|-------|-------|
| **Document ID** | `SFMS-MF01-TEST-001` |
| **Version** | `1.0` |
| **Date** | `2026-07-30` |
| **Status** | `Approved` |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 — Software Testing Part 3: Test Documentation |
| **Author** | Nguyễn Văn Nam (NamNVHE180863) — Leader, chủ module MF01 |
| **Reviewed by** | Nguyễn Phương Thảo — Lead Test/QA Reviewer · Bùi Quang Huy — Traceability · Nguyễn Đức Mạnh — Backend/DB — cả nhóm duyệt 2026-07-31 |
| **DPO Sign-off** | `[ ] Pending` *(module PII: email, SĐT, mật khẩu hash, googleSub)* |
| **Approved by** | Nguyễn Văn Nam — Final Approve, 2026-07-31 |
| **Classification** | `Internal — Confidential` |

**References (thứ tự ưu tiên khi xung đột — cao xuống thấp):**

| # | Nguồn | Vai trò trong tài liệu này |
|---|---|---|
| 0 | `07_implement/00_Common_Conventions.md` (D1–D7, Approved 2026-07-30) | **Ưu tiên cao nhất.** Mọi expected value về ĐỊNH DẠNG: ngày ISO-8601 (D4), enum SCREAMING_SNAKE (D6), envelope lỗi (D2), `id` = mã nghiệp vụ (D1), path `/api/v1` (D3), `body.id` → 409 (D5), message tiếng Anh (D7) |
| 1 | `07_implement/MF01_User_Management_Access_Control/TDS-MF01_Backend.md` (SFMS-MF01-IMP-001 v2.0) | **Nguồn chính.** §13 = xương sống (mở rộng, không viết lại) · §9 = expected request/response · §10 = expected error path · §16 = ma trận security · §5.2 = assertion tầng DB |
| 2 | `02_Requirements/00_BusinessRules/business_rules.md` (v1.9) | **ORACLE** nghiệp vụ (BR-001…BR-013, BR-218) |
| 3 | `02_Requirements/00_BusinessRules/error_message.md` (v1.5) | **ORACLE** nội dung thông điệp — nguyên văn tiếng Anh |
| 4 | `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` §1.1 | Mỗi TC truy được ≥1 UC ID |
| 5 | `02_Requirements/02_UseCases/MF01_User_Management_Access_Control.md` | Tên UC nguyên văn + Normal/Alternative Flow |
| 6 | `03_Design/08_FrontEnd/Web/scripts/semantic-audit.ts` | **0 luật liên quan MF01** — xem §2 L11 (có bằng chứng grep) |
| 7 | `03_Design/08_FrontEnd/Web/src/lib/mock-data.ts` | **CHỈ** dùng làm SEED/FIXTURE (`U-0001`, `ROLE-08`, `STF-009`, `SEC-DEFAULT`…). KHÔNG lấy định dạng ngày / giá trị enum từ đây |

> **Quy ước TDD:** Tài liệu này mô tả test cases **TRƯỚC** khi viết production code.
> Thứ tự bắt buộc: viết test (`*Test.java`) → chạy → xác nhận **FAIL 🔴** (§5.1 Red Gate) → implement → **PASS 🟢** → refactor 🔵.
> Không mark ✅ nếu `./mvnw verify` chưa xanh.
> **Test data = `SYNTHETIC`.** Không dùng PII thật. Seed từ `mock-data.ts` là dữ liệu giả lập của dự án.

> **⚠️ Ràng buộc stack — template `PHASE-4_Test-Spec.md` chỉ là mẫu minh họa.** Template viết bằng TypeScript/Prisma/Jest; dự án dùng **Java 25 LTS · Spring Boot 4.1.x · JUnit 5 · Spring Boot Test · Testcontainers (PostgreSQL 16) · Flyway · WireMock (stub Google OAuth2) · GreenMail (stub SMTP) · JaCoCo**. Mọi ví dụ code trong tài liệu này đã được dịch sang Java/SQL. **KHÔNG** có `prisma`, `npm test`, `.spec.ts`.

---

## CHANGELOG

> **Policy 4.4 — Immutable History:** Không bao giờ xóa thông tin cũ.

| Ngày | Người thực hiện | Nội dung thay đổi |
|------|-----------------|-------------------|
| 2026-07-30 | Nguyễn Văn Nam | Khởi tạo (v1.0) — cùng `TestSpec-MF02` là **cặp bản mẫu convention** cho 11 module còn lại (Team Plan §4b: MF01/MF02 là nút cổ chai). Mở rộng TDS-MF01 §13 (4 unit + 2 integration + 3 e2e ⇒ **24 unit + 16 integration + 14 security**). Ghi nhận **15 Logic Issue** (§2), trong đó 8 mục là mâu thuẫn **mới** chưa có trong TDS §2.3. ⚠️ Hai mục **chặn implement** cần Nam chốt trước: **L3** (shape `details` cho `attemptsLeft`) và **L15** (bổ sung MSG-137/138 vào TDS §10). |
| 2026-07-31 | Nguyễn Văn Nam | **Chuyển Status → Approved** sau khi cả nhóm (Huy/Mạnh/Minh/Thảo) review OK. Đóng **L3** và **L15** (§2): TDS-MF01 §9.2 đã có shape `details` cho `attemptsLeft`; TDS-MF01 §10 đã bổ sung `IAM-404`/MSG-137 + `IAM-422`/MSG-138. Mở khóa `MF01-TC-INT-002`, `INT-015`, `INT-016` — không còn TC nào ở trạng thái `BLOCKED`/`BLOCKING-ORACLE`. |

---

## MỤC LỤC

1. [Thông tin Module](#1-thông-tin-module) · 1.1 [AI Generation Context](#11-ai-generation-context-case-20)
2. [Logic Issues Resolved](#2-logic-issues-resolved)
3. [Test Design Specification](#3-test-design-specification)
4. [Test Case Specification](#4-test-case-specification) — 4.1 [Unit](#41-unit-test-cases-service-layer) · 4.2 [Integration](#42-integration-test-cases-endpoint--db) · 4.3 [Security](#43-security-test-cases-rbac--attack-vector)
5. [Red-Green-Refactor Tracker](#5-red-green-refactor-tracker) · 5.1 [Red Gate Protocol](#51-red-gate-protocol-case-20--gate-2)
6. [Entry / Exit Criteria](#6-entry--exit-criteria)
7. [Rollback Plan](#7-rollback-plan)
8. [CASE 2.0 Anti-Pattern Detection](#8-case-20-anti-pattern-detection-ai-assisted-tcs)
9. [Danh sách mã đã trích dẫn](#9-danh-sách-mã-đã-trích-dẫn-đối-chiếu-nhanh-khi-review)

---

## 1. Thông tin Module

| Field | Value |
|-------|-------|
| **Feature / Module ID** | `MF01` — User Management & Access Control |
| **Bounded Context** | Identity & Access Management (IAM) — package `com.sfms.iam` |
| **Spec gốc** | `SFMS-MF01-IMP-001` v2.0 (`TDS-MF01_Backend.md`) |
| **Priority** | 🔴 **P0** — nền tảng, cả team chờ (Team Plan §3) |
| **Sprint** | Giai đoạn TDS→Backend, Ngày 1–2 (Team Plan §4b lịch gợi ý) |
| **Data Classification** | `Sensitive-PII` — email, SĐT, `password_hash`, `google_sub`, `last_login`, `audit_logs.actor/ip` |
| **Compliance Scope** | PDPD (Nghị định Bảo vệ Dữ liệu Cá nhân) — theo ADR-000/ADR-001 |
| **Upstream Dependencies** | Email Service (SMTP → **GreenMail** trong test) · Google OAuth2 Provider (→ **WireMock** trong test) · MF02 School (`schoolId`) |
| **Downstream Consumers** | **12 module còn lại** — tin cậy JWT (`uid`, `role`, `schoolId`, `partnerId`) do MF01 phát hành |
| **Số UC phủ** | 20/20 UC của traceability §1.1 |
| **Tổng test case** | **54** — 24 Unit · 16 Integration · 14 Security |

### 1.1 AI Generation Context (CASE 2.0)

| Field | Value |
|-------|-------|
| **AI Assisted?** | `Yes` |
| **Constraint Source** | `TDS-MF01_Backend.md` §9/§10/§13/§16/§17 (C1–C6) · `00_Common_Conventions.md` D1–D7 · Team Plan §2c-3b (prompt TestSpec) |
| **Constraints Injected** | (1) Mọi expected value phải trích BR/UC/MSG — không dùng giả định AI; (2) định dạng theo D1–D7, **không** theo mock; (3) mock chỉ là nguồn seed/fixture; (4) tên luật `semantic-audit.ts` copy nguyên văn từ script; (5) đủ 3 lớp Unit/Integration/Security; (6) error path phải đúng envelope D2 + `code` khớp TDS §10; (7) bắt buộc test "gọi API trực tiếp bỏ qua UI" cho mọi gate enforce phía server; (8) stack Java/JUnit5/Testcontainers — không TypeScript/Prisma |
| **Model** | `claude-opus-5` (theo Team Plan §2c-4: TestSpec ⇒ Opus, vì oracle sai là test vô nghĩa) |
| **Trust Level** | `T2 → T3 (pending Red Gate §5.1)` |
| **Nguồn đã đọc thực tế** | `00_Common_Conventions.md` (218 dòng, toàn bộ) · `TDS-MF01_Backend.md` (1264 dòng, toàn bộ) · `error_message.md` v1.5 (toàn bộ) · `business_rules.md` §1–2 (BR-001–013, BR-218) · `traceability_matrix.md` §1.1 (20 dòng UC) · UC file MF01 (445 dòng, toàn bộ) · `semantic-audit.ts` (grep toàn bộ 57 tên luật) · `mock-data.ts` (`USER_ACCOUNTS`, `ROLE_RECORDS`, `PERMISSIONS`, `SCHOOL_STAFF`, `SECURITY_POLICIES`, `SCHOOLS`) · `mock-server.ts` (guard DELETE dòng 359–376) · `db.ts` (resource key) |

---

## 2. Logic Issues Resolved

> **Bắt buộc điền trước khi viết test.** Test cases encode hành vi **đã sửa**, không phải hành vi trong spec gốc.
> **L1–L7 là mâu thuẫn MỚI** phát hiện khi viết TestSpec (chưa có trong TDS §2.3 CF-1…CF-6). **L8–L15** là ghi chú áp dụng.
> Mỗi mục nêu rõ: bên nào thắng, **vì sao** (theo thứ tự ưu tiên nguồn ở §References), và ai phải xác nhận.

| # | Spec gốc (sai / thiếu / mâu thuẫn) | Thực tế (nguồn ưu tiên cao hơn) | Fix áp dụng trong test | Ai xác nhận |
|---|---|---|---|---|
| **L1** | **TDS §13 TC-UNIT-001** viết: đổi MK trùng 3 MK gần nhất ⇒ *"ném IAM-006policy (MSG-063)"*. | Hai chỗ sai: (a) TDS §10 quy định trùng-MK là **`IAM-006reuse` / MSG-105**, còn `IAM-006policy` là MK không đạt chính sách (MSG-104); (b) **MSG-063 là Warning** *("Weak password rating. Recommend…")* — `error_message.md` §3 — nên **không chặn được** thao tác. Chính `error_message.md` v1.5 ghi rõ lý do cấp MSG-104: *"Trước v1.5 MF01 mượn MSG-063 — nhưng MSG-063 là Warning ('recommend'), không chặn được thao tác"*. | `MF01-TC-003` assert `error.code = "IAM-006reuse"`, `message` = **MSG-105** nguyên văn, HTTP **422**. MSG-063 **không** dùng làm expected của bất kỳ TC chặn nào. | Thảo (QA) |
| **L2** | `error_message.md` v1.5 cột **"Mã TDS"** ghi MSG-104 **và** MSG-105 **cùng** map về `IAM-006policy`. | TDS §10 tách **2 mã**: `IAM-006policy` (MSG-104, chính sách) và `IAM-006reuse` (MSG-105, trùng lịch sử). Hai tình huống khác nhau, FE cần phân biệt để hiện nhãn khác nhau (D7: FE tra `code`). | Test dùng **2 mã riêng** theo TDS §10 (TDS là nguồn của `error.code` theo D2). Ghi lại để sửa cột "Mã TDS" của MSG-105 thành `IAM-006reuse`. | Huy (sửa `error_message.md`) + Nam |
| **L3** | ~~**TDS §9.2** login 401: `{ "error": { "code", "message", "attemptsLeft": 3 } }` — `attemptsLeft` là **sibling** của `code`/`message`.~~ **✅ ĐÃ CHỐT 2026-07-31.** | **D2** chốt envelope **đúng 3 khóa**: `code`, `message`, `details` (mảng, *"có thể rỗng; không bao giờ null"*). TDS §9.2 đã sửa: `details: [ { "field": "attemptsLeft", "value": 3 } ]`, áp dụng cả 2 nhánh email tồn tại/không tồn tại. | `MF01-TC-INT-002` assert `error.details = [ { "field": "attemptsLeft", "value": 3 } ]` và assert **không tồn tại** `error.attemptsLeft`. | **Nam** — đã chốt (TDS-MF01 §9.2, 2026-07-31) |
| **L4** | **TDS §15.1** mẫu response login: `"role":"School Administration"`; **TDS §9.2** mẫu Google: `"role": "Teacher"`. | **D6** `RoleKey` = **kebab-case** (`school-admin`, `teacher`) — *"đồng thời là URL segment"*; chính TDS §9.2 cũng ghi *"`role` dùng RoleKey kebab-case theo D6 (`school-admin`, không phải `"School Administration"`)"*. §15/§9.2-Google là **sót khi sửa**. | Mọi TC assert `role` ∈ 7 giá trị kebab-case của D6. `MF01-TC-INT-001` assert chính xác `"role":"school-admin"` cho `U-0001`. | Minh (FE contract) |
| **L5** | **TDS §6.1 + §6.3** dùng **MSG-003** cho tình huống *email chưa xác minh* (BR-010). | Nội dung MSG-003 = *"Your account has been deactivated…"* — **ngữ nghĩa vô hiệu hóa**, không phải chưa-xác-minh. Đây **cùng loại lỗi với CF-6** (MSG-029 dùng cho Active/Deactivate). `error_message.md` **không có** MSG riêng cho "email chưa xác minh". | Test assert đúng `IAM-003` + MSG-003 **nguyên văn** (bám TDS §10, không tự đặt MSG mới) và **ghi nhận thiếu oracle ngữ nghĩa**: `MF01-TC-006` mang nhãn ⚠️ `SEMANTIC-GAP`. **KHÔNG** tự cấp mã MSG mới. | Huy + Nam |
| **L6** | **BR-015** được TDS, `error_message.md` (MSG-105) và UC-011/032 dẫn chiếu như một BR độc lập. | Grep `business_rules.md`: **BR-015 KHÔNG có mục định nghĩa riêng** — chỉ xuất hiện (a) trong thân **BR-006**: *"Mật khẩu mới **không được trùng** với **3 mật khẩu gần nhất** (BR-015)"*, (b) bảng tóm tắt §cuối. Đây là **mã treo** (dangling ID) — làm nặng thêm CF-1. | **Oracle của `MF01-TC-003` là câu trong thân BR-006** (+ nội dung MSG-105), **không** phải một mục BR-015. Ghi rõ trong TC. | Huy (bổ sung mục BR-015 hoặc bỏ mã) |
| **L7** | TDS §2.3 nêu CF-2 (UC-154). Nhưng còn **5 UC khác** lệch BR giữa UC file và traceability §1.1 mà TDS **không nêu**. | Đối chiếu trực tiếp: **UC-024** UC file `BR-010, BR-012` ↔ trace `BR-010, BR-012, BR-013` · **UC-129** UC file **`None`** ↔ trace `BR-007` · **UC-059** UC file `BR-012, BR-013` ↔ trace `BR-012` · **UC-138** UC file `BR-012, BR-013` ↔ trace **`—`** · **UC-144** UC file `BR-008` ↔ trace `BR-013` · **UC-153** UC file `BR-008` ↔ trace `BR-007`. | Theo quy tắc Team Plan §2c: **traceability §1.1 là chuẩn**. Cột "Oracle Source" của mọi TC dùng BR theo traceability; 6 điểm lệch trên ghi thành bảng riêng ở §9.3 để Huy đối chiếu. **Không** tự sửa file nào. | **Huy** (Lead Traceability) |
| **L8** | Tên UC lệch giữa 2 nguồn: traceability §1.1 ghi *"View Permission List"* / *"View Role List"*; UC file ghi *"View List Permission"* / *"View List Role"* (TDS §2.1 lại ghi *"View List Permission"*). | Team Plan §2c-2 yêu cầu **tên UC nguyên văn nguồn 1 = UC file**. | Dùng tên UC file: **UC-129 View List Permission**, **UC-130 View List Role**. Khác biệt là cosmetic, không ảnh hưởng assertion. | Huy |
| **L9** | **UC-154** Normal Flow bước 7: *"…displays **MSG-031**"* (= "User account created successfully."). Traceability §1.1 UC-154 ⇒ **MSG-028**. TDS chọn MSG-028 (CF-2). | Nội dung MSG-028 = *"Registration request submitted! A verification email has been sent."* — đây là thông điệp **lúc GỬI email**, không phải lúc **kích hoạt xong**. Nghĩa là **cả hai** nguồn đều không có MSG đúng cho "activation success". | `MF01-TC-INT-007` **không** assert success message; chỉ assert HTTP 200 + `user_accounts.status='ACTIVE'` + token `used_at IS NOT NULL` (assertion tầng DB, oracle = UC-154 Postconditions). TC mang nhãn ⚠️ `MISSING-ORACLE (success MSG)`. | Huy + Nam |
| **L10** | Fixture: mock `ROLE_RECORDS[ROLE-01].users = 2` (System Admin có 2 người dùng). | Grep `USER_ACCOUNTS` (U-0001…U-0020): **không có bản ghi nào `role: "System Admin"`**. Mọi security test RBAC cần một System Admin thật để chứng minh happy path. | Bổ sung fixture **SYNTHETIC** `FX-U-SYSADMIN` (`U-9001`, `sysadmin@sfms.vn`, role `system-admin`) + `FX-U-SYSADMIN-PRIMARY` (`U-9000`, cờ primary cho UC-003 exception). Ghi rõ là **fixture mới, không có trong mock**. | Mạnh (seed Flyway) |
| **L11** | Team Plan §2c-3b yêu cầu port luật `semantic-audit.ts` sang Gherkin. | Grep toàn bộ 57 luật (`check()` + 3 luật chỉ `add()`): **0 luật** liên quan MF01 — toàn bộ thuộc lô/mẫu/phân suất/sự cố/CAPA/NCC. TDS §13 cũng đã ghi nhận điều này. | Danh sách luật đã port = **rỗng**, kèm **bằng chứng lệnh grep** ở §9.4. Test MF01 derive 100% từ BR/UC/MSG. | Thảo |
| **L12** | Mock lưu `role` bằng **nhãn BR-007** (`"School Administration"`), `StaffMember.status` có giá trị **`PENDING`**, ngày dạng `dd/MM/yyyy HH:mm`. | D6 ⇒ `RoleKey` kebab-case; ADR-M03 ⇒ `UserStatus.PENDING_VERIFICATION`; D4 ⇒ ISO-8601. Mock **cố ý đóng băng** (Team Plan §0). | Fixture khai **giá trị sau chuyển đổi** (§3 TDS-05 có cột "Mock gốc → Giá trị test"). Việc chuyển đổi làm **một lần** ở bước sinh Flyway seed. | Mạnh |
| **L13** | MSG-076 chứa placeholder `[Minutes]`. | `error_message.md` §Placeholder: `[Minutes]` = *"Số phút còn lại của lockout đăng nhập"*; giá trị lấy từ `SecurityPolicy.lockoutMinutes` (**không hardcode** — TDS §17 C3). | `MF01-TC-004`/`INT-002` assert message đã render với **15** đọc từ `SEC-DEFAULT`, và có 1 TC đổi policy sang **20** để chứng minh không hardcode (`MF01-TC-005`). | Thảo |
| **L14** | **TDS §10 `IAM-010`** ghi message gộp: *"File upload failed: size exceeds 10MB / Unsupported file type (PDF/JPG/PNG)."* | Đây là **paraphrase**, không nguyên văn. Nguồn: MSG-008 = *"File upload failed: File size exceeds the maximum limit of 10MB."*; MSG-009 = *"File upload failed: Unsupported file type. Only PDF, JPG, and PNG are allowed."* D7 ⇒ nguyên văn. | `MF01-TC-023` / `MF01-TC-024` assert **2 message nguyên văn riêng biệt**. (CF-4 vẫn treo: UC-097 nói 5MB, MSG-008 nói 10MB ⇒ TC assert **10MB** theo MSG + ghi chú.) | Huy + Nam |
| **L15** | ~~**TDS §10 thiếu 2 mã**: không có dòng cho *resource không tồn tại* (404) và *DTO sai/thiếu field* (400/422) — dù `GET /users/{code}`, `PATCH /users/{code}/status` chắc chắn gặp.~~ **✅ ĐÃ CHỐT 2026-07-31.** | `error_message.md` v1.5 cấp **MSG-137** *"Resource not found."* và **MSG-138** *"Validation failed: one or more required fields are missing or invalid."* — TDS §10 đã bổ sung `IAM-404`/MSG-137 và `IAM-422`/MSG-138. | `MF01-TC-INT-015`/`INT-016` assert **message nguyên văn MSG-137/MSG-138** + HTTP 404/422 + `error.code = "IAM-404"`/`"IAM-422"`. | **Nam** — đã chốt (TDS-MF01 §10, 2026-07-31) |

**Tổng hợp việc phải làm ngoài TestSpec (blocking / non-blocking):**

| Mục | Ai | Việc | Chặn implement? |
|---|---|---|---|
| L3 | Nam | ~~Chốt shape `details` cho `attemptsLeft` vào TDS §9.2~~ ✅ Đã chốt 2026-07-31 | Đã mở khóa — `MF01-TC-INT-002` |
| L15 | Nam | ~~Thêm 2 dòng TDS §10: MSG-137 (404) + MSG-138 (422) kèm mã `IAM-`~~ ✅ Đã chốt 2026-07-31 | Đã mở khóa — `MF01-TC-INT-015/016` |
| L1, L2 | Huy | Sửa cột "Mã TDS" của MSG-105 → `IAM-006reuse` | Không |
| L6 | Huy | Bổ sung mục định nghĩa BR-015 (hoặc gộp hẳn vào BR-006) | Không |
| L7 | Huy | Đối chiếu 6 UC lệch BR (§9.3) | Không |
| L5, L9 | Huy + Nam | Quyết định có cấp MSG mới cho "email chưa xác minh" và "activation success" | Không |
| L10, L12 | Mạnh | Seed Flyway: 2 fixture System Admin + chuyển đổi nhãn→enum, `dd/MM/yyyy`→ISO | **Có** — mọi Integration/Security TC |
| L14 | Huy + Nam | CF-4 treo: UC-097 5MB ↔ MSG-008 10MB | Không |

---

## 3. Test Design Specification

### TDS-01 — Scope / Phạm vi

```
com.sfms.iam (MF01) — các layer được kiểm thử:
├── domain/       UserAccount, RoleRecord, Permission, SecurityPolicy, RefreshToken,
│                 AccountActivationToken, PasswordResetToken, PasswordHistory, AuditLog
│                 → Unit (thuần logic, không Spring context)
├── service/      AuthService, OAuth2LoginService, AccountService, PasswordResetService,
│                 AccountActivationService, UserService, StaffService, RoleService,
│                 PermissionService, ProfileService, RefreshTokenService
│                 → Unit (Mockito mock repository + PasswordEncoder + Clock)
├── web/          AuthController, UserController, StaffController, RoleController,
│                 PermissionController, ProfileController, AccountController
│                 → Integration (MockMvc + Testcontainers PostgreSQL 16 + Flyway V1/V2)
└── config/       SecurityConfig, JwtAuthenticationFilter, OAuth2LoginSuccessHandler,
                  RestControllerAdvice (envelope D2)
                  → Security (JWT 7 role × endpoint, attack vector)

NGOÀI phạm vi (thuộc module/tài liệu khác):
- Màn hình FE `(auth)/*`, `(app)/system-admin/*` → `TDS-MF01_Frontend_Web.md` (Minh)
- Cascade `SchoolDeactivated` (BR-021) → MF02 · `PartnerSuspended` → MF04
  MF01 chỉ test **handler nhận event** (MF01-TC-INT-014), không test bên phát.
- Nội dung email (template) → Email Service; MF01 chỉ assert "đã gửi 1 email tới đúng địa chỉ".
```

### TDS-02 — Test Basis / Cơ sở Kiểm thử

| Source | Items Derived |
|---|---|
| `traceability_matrix.md` §1.1 | 20 UC: UC-002, UC-003, UC-011, UC-022, UC-024, UC-026, UC-029, UC-032, UC-034, UC-035, UC-059, UC-097, UC-098, UC-129, UC-130, UC-138, UC-144, UC-153, UC-154, UC-191 |
| `business_rules.md` §1 (v1.9) | BR-001 BCrypt · BR-002 lockout 5/15' · BR-003 reset 1h + 1 req/15' · BR-004 access 30' + refresh sliding 14d · BR-005 single session · BR-006 policy + không trùng 3 MK · BR-009 chấp nhận điều khoản lần đầu · BR-010 xác minh email · BR-011 activation 48h · BR-012 email duy nhất + SĐT hợp lệ · BR-218 Google link-only |
| `business_rules.md` §2 | BR-007 RBAC 7 vai trò · BR-008 bảo vệ role hệ thống + xóa khi 0 user · BR-013 phân quyền tạo staff/user |
| `error_message.md` v1.5 | MSG-002/003/004/005/006/008/009/062/076/102/103/104/105/106/137/138 (error) · MSG-026…031 (success) · MSG-063 (warning — **không** dùng làm expected chặn, xem L1) |
| `TDS-MF01_Backend.md` §9 | Path, method, request/response shape, HTTP status |
| `TDS-MF01_Backend.md` §10 | Bảng mã lỗi `IAM-*` ⇒ expected `error.code` |
| `TDS-MF01_Backend.md` §16 | Authorization Matrix ⇒ ma trận security 7 role × 12 nhóm endpoint |
| `TDS-MF01_Backend.md` §5.2 | DDL + UNIQUE/index ⇒ assertion tầng DB |
| `TDS-MF01_Backend.md` §6.3 | State machine + 5 invariant ⇒ state transition test |
| `TDS-MF01_Backend.md` §14 | 5 truy vấn SQL xác minh ⇒ assertion bất biến sau test |
| `00_Common_Conventions.md` D1–D7 | Định dạng mọi expected value (§4.4 nhóm TC hợp đồng) |
| `01_MVP_Backend_Implementation_Plan.md` §Stack + Team Plan §2b | Hợp đồng phân trang 5 field ⇒ `MF01-TC-INT-011` |
| `semantic-audit.ts` | **0 items** — xem §2 L11, bằng chứng §9.4 |

### TDS-03 — Test Conditions and Coverage Items

| Condition ID | Test Condition | Coverage Item | Test Cases |
|---|---|---|---|
| TC-COND-001 | MK lưu ở DB phải là hash BCrypt, không bao giờ plain text | `AccountService.changePassword()`, `AccountActivationService.activate()` | `MF01-TC-001`, `MF01-TC-002` |
| TC-COND-002 | MK mới phải đạt chính sách đọc từ `SecurityPolicy` | `PasswordPolicyValidator.validate()` | `MF01-TC-002` |
| TC-COND-003 | MK mới không trùng 3 MK gần nhất | `PasswordHistoryRepository.findTop3…` | `MF01-TC-003` |
| TC-COND-004 | 5 lần sai liên tiếp ⇒ khóa `lockoutMinutes`, ghi audit | `AuthService.login()` | `MF01-TC-004`, `MF01-TC-005`, `MF01-TC-INT-002` |
| TC-COND-005 | Hết thời gian khóa ⇒ login lại được, counter reset | `AuthService.login()` | `MF01-TC-005` |
| TC-COND-006 | Chỉ tài khoản đã xác minh email + ACTIVE mới login được | `AuthService.login()` | `MF01-TC-006`, `MF01-TC-007` |
| TC-COND-007 | Lần login đầu ⇒ `needTermsAccept=true` | `AuthService.login()` | `MF01-TC-008` |
| TC-COND-008 | Refresh token còn hạn ⇒ cấp access mới + trượt hạn 14 ngày | `RefreshTokenService.rotate()` | `MF01-TC-009`, `MF01-TC-INT-003` |
| TC-COND-009 | Login mới vô hiệu refresh token cũ (single session) | `RefreshTokenService.revokeAllForUser()` | `MF01-TC-010`, `MF01-TC-INT-004` |
| TC-COND-010 | Logout / deactivate / lockout ⇒ revoke refresh ngay | `AuthService.logout()`, `UserService.toggleStatus()` | `MF01-TC-INT-005`, `MF01-TC-INT-006` |
| TC-COND-011 | Role `system=true` bất khả xóa/sửa tên | `RoleService.delete()/update()` | `MF01-TC-011`, `MF01-TC-013`, `MF01-TC-INT-012` |
| TC-COND-012 | Role còn user gán (kể cả INACTIVE) ⇒ không xóa được, trả số lượng | `RoleService.delete()` | `MF01-TC-012`, `MF01-TC-INT-012` |
| TC-COND-013 | Google login **chỉ liên kết**, không tự tạo account | `OAuth2LoginService.linkAndLogin()` | `MF01-TC-014`, `MF01-TC-SEC-013` |
| TC-COND-014 | Google login lần đầu ⇒ set `googleSub`; đã link ⇒ bỏ bước link | `OAuth2LoginService.linkAndLogin()` | `MF01-TC-015`, `MF01-TC-016` |
| TC-COND-015 | Google login không áp BR-001/006/015 | `OAuth2LoginService.linkAndLogin()` | `MF01-TC-017` |
| TC-COND-016 | Token kích hoạt hiệu lực **48h**, dùng 1 lần | `AccountActivationService.activate()` | `MF01-TC-018`, `MF01-TC-019`, `MF01-TC-INT-007` |
| TC-COND-017 | Gửi lại link kích hoạt ⇒ +48h | `AccountActivationService.resend()` | `MF01-TC-020` |
| TC-COND-018 | Token reset MK hiệu lực **1h**, rate-limit 1 req/15'/email, không lộ email tồn tại | `PasswordResetService.request()/reset()` | `MF01-TC-021`, `MF01-TC-022`, `MF01-TC-INT-008`, `MF01-TC-SEC-011` |
| TC-COND-019 | Email duy nhất + SĐT đúng định dạng khi tạo user/staff | `UserService.create()`, `StaffService.create()` | `MF01-TC-INT-009`, `MF01-TC-INT-010` |
| TC-COND-020 | `status` khi tạo do **server** quyết định = `PENDING_VERIFICATION`; không set MK | `UserService.create()` | `MF01-TC-INT-009` |
| TC-COND-021 | Không deactivate primary System Admin | `UserService.toggleStatus()` | `MF01-TC-SEC-008` |
| TC-COND-022 | Ràng buộc ảnh hồ sơ (dung lượng / định dạng) | `ProfileService.update()` | `MF01-TC-023`, `MF01-TC-024` |
| TC-COND-023 | Hợp đồng phân trang 5 field + clamp + tập rỗng 200 | `UserController.list()` | `MF01-TC-INT-011` |
| TC-COND-024 | `roles.users` là field **dẫn xuất**, tính runtime | `RoleService.list()` | `MF01-TC-INT-013` |
| TC-COND-025 | Handler cascade: `PartnerSuspended` ⇒ `SUSPENDED` | `AccountCascadeHandler` | `MF01-TC-INT-014` |
| TC-COND-026 | Resource không tồn tại ⇒ 404; DTO sai ⇒ 422 | `RestControllerAdvice` | `MF01-TC-INT-015`, `MF01-TC-INT-016` |
| TC-COND-027 | RBAC least-privilege đúng §16 cho **7 role** | `SecurityConfig` + `@PreAuthorize` | `MF01-TC-SEC-001` (parameterized 7×12) |
| TC-COND-028 | Không JWT / JWT hết hạn / JWT bị sửa ⇒ 401 | `JwtAuthenticationFilter` | `MF01-TC-SEC-002`, `MF01-TC-SEC-003`, `MF01-TC-SEC-004` |
| TC-COND-029 | Identity đọc từ JWT, không tin body/query | `AuthUser` resolver | `MF01-TC-SEC-005` |
| TC-COND-030 | Cách ly dữ liệu theo `schoolId` / `partnerId` | `StaffService.list()/create()` | `MF01-TC-SEC-006`, `MF01-TC-SEC-007` |
| TC-COND-031 | Refresh token đã rotate/revoke không dùng lại được (replay) | `RefreshTokenService.rotate()` | `MF01-TC-SEC-009` |
| TC-COND-032 | Không lộ MK/token/googleSub ra response & log | toàn module | `MF01-TC-SEC-010`, `MF01-TC-SEC-012` |
| TC-COND-033 | Injection ở input xác thực & search | `AuthService`, `UserSpec` | `MF01-TC-SEC-014` |
| TC-COND-034 | Envelope lỗi D2 + ngày ISO D4 + enum D6 + `id` mã nghiệp vụ D1 | `RestControllerAdvice`, mọi DTO | `MF01-TC-INT-001` (nhóm assertion hợp đồng), `MF01-TC-INT-009` |

### TDS-04 — Test Techniques / Kỹ thuật Kiểm thử (ISO 29119-4)

| Technique | Applied To | Rationale |
|---|---|---|
| **Equivalence Partitioning** | `UserStatus` × `emailVerified` khi login; 7 `RoleKey`; `PartnerType` của tài khoản đối tác | Không gian trạng thái tài khoản là hữu hạn & phân lớp rõ; mỗi lớp có một quyết định login khác nhau (BR-010, ADR-M03) |
| **Boundary Value Analysis** | `failedLoginCount` **4/5/6** (BR-002) · activation **47h59'/48h00'/48h01'** (BR-011) · reset **59'/60'/61'** (BR-003) · rate-limit **14'59"/15'00"** (BR-003) · refresh **13d23h/14d00h/14d01h** (BR-004) · `pageSize` **0/1** và `page > pageCount` (Team Plan §2b-6) · ảnh **10MB ± 1 byte** (MSG-008) | Mọi con số trong BR là ngưỡng cứng; lỗi off-by-one ở đây = khóa oan người dùng hoặc để hở token quá hạn |
| **State Transition Testing** | `UserStatus`: `PENDING_VERIFICATION → ACTIVE → INACTIVE/SUSPENDED → ACTIVE` (TDS §6.3) | State machine có 5 invariant tường minh; cần phủ cả **transition hợp lệ** và **transition bị cấm** (VD `PENDING_VERIFICATION` login trực tiếp) |
| **Decision Table Testing** | Google login: (email tồn tại?) × (`googleSub` đã set?) × (`status`) ⇒ 8 tổ hợp, gộp còn 4 kết quả (BR-218) | BR-218 là AND của nhiều điều kiện; bảng quyết định chống bỏ sót nhánh "tồn tại nhưng INACTIVE" |
| **Error Guessing** | Refresh token replay sau rotate · leo quyền qua `role` trong body · JWT `alg:none` · enumeration email qua thời gian phản hồi lockout · gọi API bỏ qua UI | Nhóm lỗi mà `review.md` 29/07 xếp nghiêm trọng nhất: **gate chỉ tồn tại ở client thì gọi API trực tiếp là lách được** |
| **Combinatorial (pairwise)** | Ma trận **7 role × 12 nhóm endpoint** = 84 tổ hợp ⇒ `@ParameterizedTest` với `@MethodSource` | §16 là bảng đầy đủ; test parameterized cho phép phủ 100% mà không viết 84 block |

### TDS-05 — Test Data Requirements

> **Nguồn seed:** `mock-data.ts` (Team Plan §2c-3b nguồn 6 — **chỉ** lấy mã bản ghi, **không** lấy định dạng ngày/enum).
> Cột **"Mock gốc → Giá trị test"** thể hiện chuyển đổi D4/D6 (xem §2 L12).

| Fixture ID | Type | Value / Logic | Mock gốc → Giá trị test | Mục đích |
|---|---|---|---|---|
| `FX-POLICY` | DB seed | `SEC-DEFAULT`: `maxLoginAttempts=5`, `lockoutMinutes=15`, `sessionTimeoutMinutes=30`, `passwordMinLength=8`, `requireSpecialChar/Number/MixedCase=true`, `noReuseLast3=true` | `updatedAt: "01/07/2026 08:15"` → `2026-07-01T01:15:00Z` (D4) | Tham số BR-002/004/006 — **đọc từ DB, không hardcode** |
| `FX-POLICY-ALT` | DB seed | Bản sao `SEC-DEFAULT` với `maxLoginAttempts=3`, `lockoutMinutes=20` | — | Chứng minh không hardcode (`MF01-TC-005`) |
| `FX-U-SCHOOLADMIN` | DB seed | `U-0001` Phạm Quốc Toản · `toanpq@dtd.edu.vn` · `ACTIVE` · `emailVerified=true` · `schoolId=SCH-HN-0042` | `role:"School Administration"` → `school-admin` (D6); `lastLogin:"02/07/2026 07:45"` → `2026-07-02T00:45:00Z` (D4) | Happy path login; chủ thể BR-013 tạo staff |
| `FX-U-TEACHER` | DB seed | `U-0004` Nguyễn Thu Trang · `trangnt@dtd.edu.vn` · `ACTIVE` · `schoolId=SCH-HN-0042` | `role:"Teacher"` → `teacher` | Chủ thể bị chặn RBAC (§16) |
| `FX-U-MEDICAL` | DB seed | `U-0002` Ngô Thị Hà · `hant@dtd.edu.vn` · `ACTIVE` | `"Medical Staff"` → `medical-staff` | Ma trận 7 role |
| `FX-U-CATERER` | DB seed | `U-0003` Trần Văn Hùng · `hungtv@anbinhcatering.vn` · `ACTIVE` | `"Service Caterers"` → `service-caterers` | Ma trận 7 role |
| `FX-U-PARENT` | DB seed | `U-0005` Nguyễn Văn Bình · `binhnv.parent@gmail.com` · `ACTIVE` | `"Parent"` → `parent` | Ma trận 7 role (quyền hẹp nhất) |
| `FX-U-SUPPLIER` | DB seed | `U-0006` Lý Thái Sơn · `sonlt@greenfarm.vn` · `ACTIVE` · `partnerId=SUP-001` · `schoolId=NULL` | `"Raw Food Supplier"` → `supplier` (D6: *"FE không có key cho Raw Food Supplier — chốt `supplier`"*) | Ma trận 7 role + cách ly `partnerId` |
| `FX-U-SUSPENDED` | DB seed | `U-0007` Đặng Hải Yến · `yendh@daiduong.vn` · **`SUSPENDED`** · `partnerId=SUP-004` | — | BR-010 chặn login; đích cascade `PartnerSuspended` |
| `FX-U-INACTIVE` | DB seed | `U-0016` Vũ Đình Phong · `phongvd.parent@gmail.com` · **`INACTIVE`** | — | BR-010 chặn login ⇒ MSG-003 |
| `FX-U-PENDING` | DB seed | `STF-009` Hà Thị Lan · `lanht@dtd.edu.vn` · `PENDING_VERIFICATION` · `passwordHash=NULL` | `status:"PENDING"` + `emailVerified:"PENDING"` → `PENDING_VERIFICATION` + `email_verified=false`; `invitedAt:"28/06/2026"` → `2026-06-28` | UC-154 activation; invariant "không set MK lúc tạo" |
| `FX-U-SYSADMIN` | DB seed **MỚI** | `U-9001` · `sysadmin@sfms.vn` · `ACTIVE` · role `system-admin` · `schoolId=NULL` | **Không có trong mock** — xem §2 L10 | Happy path mọi endpoint System Admin |
| `FX-U-SYSADMIN-PRIMARY` | DB seed **MỚI** | `U-9000` · `root@sfms.vn` · `ACTIVE` · role `system-admin` · **primary=true** | **Không có trong mock** | UC-003 exception: không được deactivate |
| `FX-U-GOOGLE-LINKED` | DB seed | `U-0009` Phan Thị Hồng · `hongpt@dtd.edu.vn` · `googleSub="google-sub-0009"` | Cột `googleSub` **không có trong mock** (phần mới của backend, TDS §5.1) | BR-218 AF1 "đã link" |
| `FX-ROLE-SYSTEM` | DB seed | `ROLE-01`…`ROLE-07` · `system=true` · đúng `permissions[]` của mock | — | BR-008 chặn xóa/sửa tên; seed 7 role hệ thống |
| `FX-ROLE-CUSTOM-EMPTY` | DB seed | `ROLE-08` Kitchen Auditor · `system=false` · **0 user gán** | — | UC-029 happy path (xóa được) |
| `FX-ROLE-CUSTOM-USED` | DB seed **MỚI** | `ROLE-09` Ad-hoc Auditor · `system=false` · **3 user gán**, trong đó **1 user `INACTIVE`** | Mock chỉ có role custom rỗng | BR-008: *"kể cả đã vô hiệu hóa"* — nhánh mock **không phủ được** |
| `FX-PERM-16` | DB seed | 16 bản ghi `PERM-001`…`PERM-016` (danh mục cố định, chỉ đọc) | — | UC-129 list; kiểm `permissions[]` của role |
| `FX-SCHOOL-A` / `FX-SCHOOL-B` | DB seed | `SCH-HN-0042` TH Đoàn Thị Điểm · `SCH-HN-0051` TH Nguyễn Siêu | `registeredAt:"05/01/2026"` → `2026-01-05` | Test cách ly cross-school (BR-013) |
| `FX-PWD-VALID` | const | `"P@ssw0rd123"` (8+, mixed case, số, ký tự đặc biệt) | — | Đạt `FX-POLICY` |
| `FX-PWD-WEAK` | const | `"password"` (thiếu chữ hoa, số, ký tự đặc biệt) | — | MSG-104 |
| `FX-PWD-HISTORY` | DB seed | 3 bản ghi `password_history` của `U-0001`: hash của `"Old@Pass1"`, `"Old@Pass2"`, `"Old@Pass3"` | — | BR-006/BR-015 ⇒ MSG-105 |
| `FX-TOKEN-ACT-VALID` | DB seed | `account_activation_tokens` của `STF-009`, `expires_at = now + 47h59m`, `used_at=NULL` | — | Biên dưới BR-011 |
| `FX-TOKEN-ACT-EXPIRED` | DB seed | cùng trên, `expires_at = now - 1m` (tạo cách đây 48h01') | — | Biên trên BR-011 ⇒ IAM-008 |
| `FX-TOKEN-RESET-VALID` | DB seed | `password_reset_tokens` của `U-0001`, `expires_at = now + 59m` | — | Biên dưới BR-003 |
| `FX-TOKEN-RESET-EXPIRED` | DB seed | cùng trên, `expires_at = now - 1m` (tạo cách đây 61') | — | Biên trên BR-003 ⇒ IAM-008 |
| `FX-CLOCK` | bean | `Clock` inject được (`java.time.Clock.fixed`) — **mọi TTL đọc qua Clock** | — | Test biên thời gian **không dùng `Thread.sleep`** |
| `FX-ENV-OAUTH` | env | `GOOGLE_CLIENT_ID=test-client-id`, `GOOGLE_CLIENT_SECRET=test-secret-32chars` (BR-164: ENV, không hardcode) | — | Cấu hình OAuth2 Client |
| `FX-WIREMOCK-GOOGLE` | stub | WireMock ghi đè `spring.security.oauth2.client.provider.google.*` → localhost; stub `/token` + `/userinfo` trả `{ "sub": "...", "email": "...", "email_verified": true }` | — | **Không gọi Google thật** trong test |
| `FX-GREENMAIL` | stub | SMTP in-memory cổng động; assert số email + địa chỉ nhận | — | BR-003/010/011 gửi email |
| `FX-JWT-*` | JWT | `FX-JWT-SYSADMIN`, `-SCHOOLADMIN`, `-TEACHER`, `-MEDICAL`, `-CATERER`, `-PARENT`, `-SUPPLIER`: claim `{ sub=email, uid, role=<RoleKey>, schoolId, partnerId }` (TDS §9.2) | — | 7 chủ thể của ma trận §16 |
| `FX-JWT-EXPIRED` | JWT | claim đúng, `exp = now - 1s` | — | `MF01-TC-SEC-003` |
| `FX-JWT-TAMPERED` | JWT | payload đổi `role` → `system-admin`, giữ chữ ký cũ | — | `MF01-TC-SEC-004` |

**Props Isolation Boilerplate (CASE 2.0 — BẮT BUỘC).** Bản Java thay cho khối TypeScript của template:

```java
// ═══════════════════════════════════════════════════════════════════════
// CASE 2.0 — Props Isolation Pattern (Java/JUnit 5)
// Đặt trong src/test/java/com/sfms/iam/fixture/UserFixture.java
// Mỗi @Test gọi factory ⇒ KHÔNG shared mutable state (chống AP-AI-002)
// ═══════════════════════════════════════════════════════════════════════
public final class UserFixture {

    /** Baseline hợp lệ — đồng bộ FX-U-SCHOOLADMIN (§3 TDS-05). */
    public static UserAccount.Builder base() {
        return UserAccount.builder()
            .code("U-0001")                       // D1: id công khai = mã nghiệp vụ
            .name("Phạm Quốc Toản")
            .email("toanpq@dtd.edu.vn")
            .phone("0912345678")                  // BR-012
            .passwordHash(ENCODER.encode("P@ssw0rd123"))   // BR-001
            .roleKey(RoleKey.SCHOOL_ADMIN)         // D6: JSON "school-admin"
            .schoolId(SCH_HN_0042)
            .status(UserStatus.ACTIVE)             // ADR-M03
            .emailVerified(true)                   // BR-010
            .failedLoginCount(0)                   // BR-002
            .termsAcceptedAt(Instant.parse("2026-01-10T02:00:00Z")); // BR-009, D4
    }

    /** Mỗi test tự override phần mình quan tâm — không sửa builder dùng chung. */
    public static UserAccount aUser(Consumer<UserAccount.Builder> overrides) {
        UserAccount.Builder b = base();
        overrides.accept(b);
        return b.build();
    }

    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder(12); // TDS §4.3
    private UserFixture() {}
}
```

```java
// Integration base — Testcontainers + Flyway, KHÔNG dùng H2 (phải test đúng PostgreSQL 16)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
abstract class AbstractIamIntegrationTest {

    @Container @ServiceConnection
    static final PostgreSQLContainer<?> DB = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired protected MockMvc mvc;
    @Autowired protected JdbcTemplate jdbc;

    /** Mỗi test bắt đầu từ trạng thái seed sạch — chống shared state giữa test. */
    @BeforeEach void resetSchema() {
        Flyway.configure().dataSource(DB.getJdbcUrl(), DB.getUsername(), DB.getPassword())
              .cleanDisabled(false).load().clean();   // chỉ container test — xem §7
        Flyway.configure().dataSource(DB.getJdbcUrl(), DB.getUsername(), DB.getPassword())
              .locations("db/migration", "db/testseed").load().migrate();
    }
}
```

---

## 4. Test Case Specification

> **TC ID format:** `MF01-TC-[NNN]` (Unit) · `MF01-TC-INT-[NNN]` (Integration) · `MF01-TC-SEC-[NNN]` (Security)
> **Severity:** CRITICAL / HIGH / MEDIUM / LOW · **Status:** 🔴 Not written / 🟡 Written-failing / 🟢 Passing
> **Oracle Source** là mục **bắt buộc** — mọi expected value phải trích được về BR/UC/MSG/D1–D7. Không có oracle ⇒ TC bị đánh dấu ⚠️ và báo lại (§2 L9, L15).
> **Quy ước envelope lỗi (D2)** — mọi assertion error path dùng chung khuôn này, không lặp lại ở từng TC:
> ```json
> { "error": { "code": "IAM-xxx", "message": "<nguyên văn error_message.md>", "details": [] } }
> ```
> `details` là **mảng**, có thể rỗng, **không bao giờ `null`**. Assert cả 3 khóa; assert **không có** khóa thứ 4.

#### Bảng thông điệp kỳ vọng — **nguyên văn** `error_message.md` v1.5 (D7)

> Các TC bên dưới viết tắt *"MSG-0xx nguyên văn"*. Chuỗi đầy đủ để `assertEquals` nằm ở đây — **một nguồn duy nhất**, khi `error_message.md` đổi thì sửa 1 chỗ.
> Khai thành hằng trong `src/test/java/com/sfms/iam/fixture/ExpectedMessages.java`; test dùng `equalTo(...)`, **không** dùng `contains(...)` (D7 đòi nguyên văn).

| Mã | `error.code` (TDS §10) | HTTP | Message — **nguyên văn, dùng làm expected** |
|---|---|---|---|
| MSG-002 | `IAM-002` | 401 | `Invalid credentials. Please check your email and password.` |
| MSG-003 | `IAM-003` | 403 | `Your account has been deactivated. Please contact your administrator.` |
| MSG-004 | `IAM-005` / `IAM-008` | 401 | `Session expired. Please log in again.` |
| MSG-005 | `IAM-004` | 403 | `Access denied: You do not have permission to access this resource.` |
| MSG-006 | `IAM-006` | 409 | `Registration failed: The email address is already in use.` |
| MSG-008 | `IAM-010` | 422 | `File upload failed: File size exceeds the maximum limit of 10MB.` |
| MSG-009 | `IAM-010` | 422 | `File upload failed: Unsupported file type. Only PDF, JPG, and PNG are allowed.` |
| MSG-076 | `IAM-007` | 423 | `Account temporarily locked due to too many failed login attempts. Please try again after [Minutes] minutes.` — `[Minutes]` ← `SecurityPolicy.lockoutMinutes` (§2 **L13**) |
| MSG-102 | `IAM-102` | 403 | `Google account not linked to any SFMS account. Please contact your administrator to create your account first.` |
| MSG-103 | `IAM-103` | 401 | `Google authentication failed. Please try again.` |
| MSG-104 | `IAM-006policy` | 422 | `Password does not meet the security policy. It must contain uppercase, lowercase, a number, and a special character.` |
| MSG-105 | `IAM-006reuse` | 422 | `You cannot reuse any of your last 3 passwords. Please choose a different one.` (§2 **L1/L2**) |
| MSG-106 | `IAM-009` | 409 | `Cannot delete this role: [Number] user(s) are still assigned to it. Please reassign them first.` — `[Number]` ← `countByRoleId` |
| MSG-137 | ⚠️ chưa có (§2 **L15**) | 404 | `Resource not found.` |
| MSG-138 | ⚠️ chưa có (§2 **L15**) | 422 | `Validation failed: one or more required fields are missing or invalid.` |
| MSG-026 | — (success) | 200 | `Login successful. Welcome back, [Name]!` |
| MSG-027 | — (success) | 200 | `Logout successful. Session terminated safely.` |
| MSG-029 | — (success) | 200 | `Profile updated successfully.` (⚠️ **CF-6** — dùng cho UC-002/003 theo traceability dù ngữ nghĩa là hồ sơ) |
| MSG-030 | — (success) | 200 | `Password changed successfully.` |
| MSG-031 | — (success) | 201 | `User account created successfully.` |

**Hai mã cố ý KHÔNG dùng làm expected:**
- **MSG-063** `Weak password rating. Recommend adding special characters and numbers.` — là **Warning**, không chặn được thao tác ⇒ thay bằng MSG-104 (§2 **L1**).
- **MSG-062** `Suspicious login attempt detected from a new location/device. Verification code required.` — **không có BR/UC định nghĩa "thiết bị lạ"** ⇒ thiếu oracle, không viết TC (§9.5 mục 1).

### 4.1 UNIT TEST CASES (service layer)

> Không nạp Spring context. Mockito mock repository + `PasswordEncoder` + `Clock` (`FX-CLOCK`).
> Thư mục: `src/test/java/com/sfms/iam/service/`

---

#### MF01-TC-001 — Mật khẩu lưu ở dạng hash BCrypt, không plain text

**Severity:** `CRITICAL` · **CWE:** `CWE-256 — Plaintext Storage of a Password`
**Feature Under Test:** `AccountService.changePassword()`, `AccountActivationService.activate()`
**Test File:** `service/AccountServiceTest.java` · **Condition Ref:** `TC-COND-001` · **TDD Phase:** 🔴 RED
**Oracle Source:** **BR-001** *"Tất cả mật khẩu người dùng phải được mã hóa bằng thuật toán BCrypt trước khi lưu vào cơ sở dữ liệu. Hệ thống không bao giờ lưu trữ mật khẩu dưới dạng văn bản thuần (plain text)."* · TDS §4.3 *"BCrypt strength ≥ 12"* · TDS §17 C1 · **UC-011** Normal Flow bước 6

**Preconditions:** `FX-U-SCHOOLADMIN`, `FX-PWD-VALID`, `FX-POLICY`

**Test Steps:**
1. **Arrange:** mock `UserAccountRepository.findByCode("U-0001")` → `UserFixture.aUser(b -> {})`; `ArgumentCaptor<UserAccount>` cho `save()`.
2. **Act:** `changePassword(uid, "P@ssw0rd123", "N3w@Pass456")`.
3. **Assert:** đọc `passwordHash` từ đối tượng đã capture.

**Expected Result (PASS — hành vi đúng):**
- `passwordHash` khớp `^\$2[aby]\$\d{2}\$.{53}$` và **cost = 12** (prefix `$2a$12$` / `$2b$12$`).
- `passwordHash != "N3w@Pass456"`; `matches("N3w@Pass456", hash) == true`.
- `verify(repo, never()).save(argThat(u -> "N3w@Pass456".equals(u.getPasswordHash())))`.

**Expected Result (FAIL — dấu hiệu lỗi):** hash không bắt đầu `$2`, bằng chính chuỗi MK, hoặc cost < 12 ⇒ vi phạm BR-001 / TDS §4.3.

**Current Status:** 🔴 Not written
**Implementation Note:** khai `BCryptPasswordEncoder(12)` thành `@Bean` — không `new` trong service, để test đọc được cùng cost.

---

#### MF01-TC-002 — Chính sách độ mạnh MK đọc từ `SecurityPolicy` (không hardcode)

**Severity:** `HIGH`
**Feature Under Test:** `PasswordPolicyValidator.validate()`
**Test File:** `service/PasswordPolicyValidatorTest.java` · **Condition Ref:** `TC-COND-002` · **TDD Phase:** 🔴 RED
**Oracle Source:** **BR-006** *"Mật khẩu phải đáp ứng chính sách bảo mật tối thiểu (độ dài, ký tự đặc biệt, v.v.)"* · **MSG-104** (nguyên văn): *"Password does not meet the security policy. It must contain uppercase, lowercase, a number, and a special character."* · TDS §10 `IAM-006policy` HTTP **422** · TDS §17 C3 (đọc tham số từ `SecurityPolicy`, không hardcode)

**Preconditions:** `FX-POLICY` (`passwordMinLength=8`, cả 3 cờ = `true`)

**Test Steps (`@ParameterizedTest` — Equivalence Partitioning):**

| # | Input | Vi phạm | Expected |
|---|---|---|---|
| a | `"P@ssw0rd123"` | — | hợp lệ, không ném |
| b | `"password"` (`FX-PWD-WEAK`) | thiếu hoa/số/đặc biệt | `IAM-006policy` + MSG-104 |
| c | `"P@ss1"` | dài 5 < 8 | `IAM-006policy` + MSG-104 |
| d | `"P@SSWORD1"` | thiếu chữ thường | `IAM-006policy` + MSG-104 |
| e | `"Password1"` | thiếu ký tự đặc biệt | `IAM-006policy` + MSG-104 |
| f | `"P@ssword"` | thiếu số | `IAM-006policy` + MSG-104 |

4. Ghi bản ghi policy mới `passwordMinLength=12` (**không** sửa hằng trong code) → `"P@ssw0rd123"` (11 ký tự) **phải** bị từ chối.

**Expected Result (PASS):** đúng cột Expected; `message` **khớp nguyên văn MSG-104**; bước 4 chứng minh giá trị đến từ DB.
**Expected Result (FAIL):** bước 4 vẫn hợp lệ ⇒ độ dài bị hardcode (vi phạm C3). Message = **MSG-063** ⇒ vi phạm §2 **L1** (MSG-063 là Warning, không chặn được thao tác).

**Current Status:** 🔴 Not written

---

#### MF01-TC-003 — MK mới không được trùng 3 MK gần nhất

**Severity:** `HIGH`
**Feature Under Test:** `AccountService.changePassword()` + `PasswordHistoryRepository.findTop3ByUserIdOrderByCreatedAtDesc()`
**Test File:** `service/AccountServiceTest.java` · **Condition Ref:** `TC-COND-003` · **TDD Phase:** 🔴 RED
**Oracle Source:** **BR-006** (thân mục, nguyên văn): *"Mật khẩu mới **không được trùng** với **3 mật khẩu gần nhất** (BR-015)."* · **UC-011** Normal Flow bước 5 *"validates the new password against the password policy and password history (BR-006: must not match last 3 passwords)"* · **MSG-105**: *"You cannot reuse any of your last 3 passwords. Please choose a different one."* · TDS §10 `IAM-006reuse` HTTP **422**
**⚠️ Ghi chú oracle:** BR-015 **không có mục định nghĩa riêng** trong `business_rules.md` (§2 **L6**) ⇒ oracle là câu trong thân **BR-006** + nội dung MSG-105. Liên quan **CF-1** (BR-015 không có trong cột BR của UC-011/UC-032 tại traceability §1.1) — TC vẫn hợp lệ vì UC-011 bước 5 nêu tường minh.

**Preconditions:** `FX-U-SCHOOLADMIN`, `FX-PWD-HISTORY` (`Old@Pass1/2/3`), `FX-POLICY` (`noReuseLast3=true`)

**Test Steps:**
1. Mock `findTop3…` trả 3 bản ghi theo `created_at DESC`.
2. **(a)** đổi sang `"Old@Pass2"` (trùng bản ghi thứ 2).
3. **(b)** đổi sang `"Old@Pass4"` (chưa từng dùng).
4. **(c)** thêm bản ghi thứ 4 (`Old@Pass0`, cũ nhất) → đổi sang `"Old@Pass0"`.
5. **(d)** đặt `noReuseLast3=false` → đổi sang `"Old@Pass2"`.

**Expected Result (PASS):**
- (a) ném `IamException` `code="IAM-006reuse"`, `message` = MSG-105 nguyên văn, HTTP 422; **không** gọi `save()`.
- (b) thành công; `password_history` **thêm 1** bản ghi, giữ **tối đa 3** (bản cũ nhất bị xóa — TDS §5.1 `"0..3 PasswordHistory"`).
- (c) **thành công** ⇒ cửa sổ đúng **3**, không phải toàn bộ lịch sử.
- (d) thành công ⇒ cờ policy được tôn trọng.

**Expected Result (FAIL):** (a) đi qua ⇒ vi phạm BR-006; (c) bị chặn ⇒ off-by-one cửa sổ lịch sử; `code="IAM-006policy"` hoặc message MSG-063 ⇒ vi phạm §2 **L1/L2**.

**Current Status:** 🔴 Not written

---

#### MF01-TC-004 — Khóa tài khoản sau 5 lần sai liên tiếp (Boundary 4/5/6)

**Severity:** `CRITICAL`
**Feature Under Test:** `AuthService.login()`
**Test File:** `service/AuthServiceLockoutTest.java` · **Condition Ref:** `TC-COND-004` · **TDD Phase:** 🔴 RED
**Oracle Source:** **BR-002** *"Sau **5 lần** đăng nhập thất bại liên tiếp, hệ thống tự động khóa tài khoản trong **15 phút**. Sự kiện khóa tài khoản phải được ghi vào audit log."* · **UC-034 AF2** · **MSG-002** / **MSG-076** (`[Minutes]` ← `SecurityPolicy.lockoutMinutes`, §2 **L13**) · TDS §10 `IAM-002` (401) / `IAM-007` (**423**)

**Preconditions:** `FX-U-SCHOOLADMIN` (`failedLoginCount=0`, `lockedUntil=null`), `FX-POLICY`, `FX-CLOCK` = `2026-07-30T02:00:00Z`

**Test Steps (Boundary Value Analysis):**
1. Login sai **lần 1–4**.
2. Login sai **lần 5**.
3. Login lần **6 với MK ĐÚNG** ← điểm quan trọng nhất: khóa phải thắng cả MK đúng.
4. Kiểm `audit_logs`.

**Expected Result (PASS):**
- Lần 1–4: `IAM-002` (401) + MSG-002 nguyên văn; `failedLoginCount` = 1,2,3,4; `lockedUntil == null`.
- Lần 5: `failedLoginCount=5`; `lockedUntil = 2026-07-30T02:15:00Z` (= `now + lockoutMinutes`); trả `IAM-007` (**423**) + MSG-076 render **"…after 15 minutes."**
- Lần 6 (MK đúng): vẫn `IAM-007` (423); **không** cấp token; **không** reset counter.
- `audit_logs` có ≥1 dòng `action` chứa `LOGIN`, `result='BLOCKED'`, `ip` **không null** (TDS §5.1 — `AuditLog.ip` UC-106).

**Expected Result (FAIL):** khóa ở lần 4 (chặn oan) hoặc lần 6 (hở 1 lần thử); lần 6 MK đúng vào được ⇒ lockout vô nghĩa; `lockedUntil` dùng hằng số 15 thay vì đọc policy.

**Current Status:** 🔴 Not written

---

#### MF01-TC-005 — Hết thời gian khóa ⇒ login lại được, counter reset; tham số đọc từ policy

**Severity:** `HIGH`
**Feature Under Test:** `AuthService.login()`
**Test File:** `service/AuthServiceLockoutTest.java` · **Condition Ref:** `TC-COND-004`, `TC-COND-005` · **TDD Phase:** 🔴 RED
**Oracle Source:** **BR-002** (khóa **trong** 15 phút ⇒ hết 15 phút phải mở) · **UC-034** Normal Flow bước 8 *"resets failed login counter"* · TDS §17 C3

**Preconditions:** `FX-U-SCHOOLADMIN` với `failedLoginCount=5`, `lockedUntil = 2026-07-30T02:15:00Z`; `FX-CLOCK`

**Test Steps (Boundary):**
1. `Clock = 02:14:59Z` → login MK đúng.
2. `Clock = 02:15:00Z` → login MK đúng (state fresh).
3. `Clock = 02:15:01Z` → login MK đúng (state fresh).
4. Đổi sang **`FX-POLICY-ALT`** (`maxLoginAttempts=3`, `lockoutMinutes=20`), reset user, login sai 3 lần.

**Expected Result (PASS):**
- (1) `IAM-007` (423) — còn trong thời gian khóa.
- (2)/(3) **thành công**: cấp access + refresh; `failedLoginCount = 0`; `lockedUntil = null`; `lastLogin` cập nhật (`Instant`, ISO — D4).
- (4) khóa ngay ở **lần 3**, `lockedUntil = now + 20'`, message render **"after 20 minutes."** ⇒ cả 2 tham số đọc từ DB.

**Expected Result (FAIL):** (2) vẫn bị chặn ⇒ so sánh `>` thay `>=` (khóa dài hơn quy định); (4) vẫn khóa ở lần 5 / render 15 phút ⇒ hardcode, vi phạm C3.

**Current Status:** 🔴 Not written
**Implementation Note:** dùng `now().isBefore(lockedUntil)`; **phải** inject `Clock` — nếu gọi `Instant.now()` trực tiếp thì TC này không viết được mà không `Thread.sleep`.

---

#### MF01-TC-006 — Email chưa xác minh ⇒ chặn login (BR-010)

**Severity:** `HIGH` · ⚠️ **SEMANTIC-GAP** (§2 **L5**)
**Feature Under Test:** `AuthService.login()`
**Test File:** `service/AuthServiceLoginGuardTest.java` · **Condition Ref:** `TC-COND-006` · **TDD Phase:** 🔴 RED
**Oracle Source:** **BR-010** *"Tài khoản chỉ được phép đăng nhập sau khi xác minh địa chỉ email thành công. Tài khoản chưa xác minh sẽ bị chặn đăng nhập và được nhắc thực hiện xác minh."* · **UC-034** Normal Flow bước 6 · TDS §6.1 (`else MSG-003`) + §10 `IAM-003` (403)
**⚠️ Thiếu oracle ngữ nghĩa:** MSG-003 = *"Your account has been deactivated…"* — không phải "chưa xác minh"; `error_message.md` **không có** MSG riêng cho tình huống này. TC assert theo TDS §10 và **không tự cấp mã mới**.

**Preconditions:** `FX-U-PENDING` (`STF-009`, `PENDING_VERIFICATION`, `email_verified=false`, `passwordHash=NULL`)

**Test Steps:** login `lanht@dtd.edu.vn` với MK bất kỳ.

**Expected Result (PASS):**
- `IAM-003` (403) + MSG-003 nguyên văn.
- **Không** cấp token; **không** ghi `refresh_tokens`.
- **Không** tăng `failedLoginCount` (chưa xác minh ≠ sai MK ⇒ không lockout oan).
- Guard chạy **trước** `bCrypt.matches()` ⇒ `passwordHash=NULL` **không** gây `NullPointerException`.

**Expected Result (FAIL):** NPE khi `passwordHash` null; login qua được ⇒ vi phạm BR-010; trả `IAM-002` ⇒ lệch TDS §6.1.

**Current Status:** 🔴 Not written

---

#### MF01-TC-007 — Tài khoản `INACTIVE` / `SUSPENDED` ⇒ chặn login

**Severity:** `HIGH`
**Feature Under Test:** `AuthService.login()`
**Test File:** `service/AuthServiceLoginGuardTest.java` · **Condition Ref:** `TC-COND-006` · **TDD Phase:** 🔴 RED
**Oracle Source:** **MSG-003** (nguyên văn) · TDS §10 `IAM-003` (403) *"Login tài khoản INACTIVE/SUSPENDED"* · TDS §6.3 ADR-M03 · **UC-034** Normal Flow bước 2 *"verifies that the account exists and is active (MSG-003 if deactivated)"*

**Preconditions:** `FX-U-INACTIVE` (`U-0016`), `FX-U-SUSPENDED` (`U-0007`, `partnerId=SUP-004`), `FX-U-SCHOOLADMIN`

**Test Steps (`@ParameterizedTest` — 4 lớp tương đương của `UserStatus`):**

| `status` | `emailVerified` | Expected |
|---|---|---|
| `ACTIVE` | `true` | 200 — cấp token |
| `PENDING_VERIFICATION` | `false` | `IAM-003` 403 (⇒ `MF01-TC-006`) |
| `INACTIVE` | `true` | `IAM-003` 403 + MSG-003 |
| `SUSPENDED` | `true` | `IAM-003` 403 + MSG-003 |

**Expected Result (PASS):** đúng bảng; `INACTIVE`/`SUSPENDED` **không** cấp token, **không** tăng `failedLoginCount`; `SUSPENDED` giữ nguyên `partnerId`.
**Expected Result (FAIL):** `SUSPENDED` login được — mock FE **không có đường vào** trạng thái này nên rất dễ bị bỏ sót khi implement ⇒ tài khoản NCC đang bị đình chỉ vẫn đọc được dữ liệu.

**Current Status:** 🔴 Not written

---

#### MF01-TC-008 — Lần login đầu ⇒ `needTermsAccept = true` (BR-009)

**Severity:** `MEDIUM`
**Feature Under Test:** `AuthService.login()` → `LoginResult.needTermsAccept`
**Test File:** `service/AuthServiceTest.java` · **Condition Ref:** `TC-COND-007` · **TDD Phase:** 🔴 RED
**Oracle Source:** **BR-009** *"Trong lần đăng nhập **đầu tiên**, người dùng bắt buộc phải chấp nhận Chính sách Bảo mật và Điều khoản Sử dụng. Hệ thống không cho phép truy cập chức năng trước khi người dùng xác nhận."* · **UC-034 AF1** · TDS §8.1 `LoginResult(..., boolean needTermsAccept)` · TDS §9.2 `"needTermsAccept": false`

**Preconditions:** 2 biến thể `FX-U-SCHOOLADMIN`: `termsAcceptedAt = null` / `= 2026-01-10T02:00:00Z`

**Test Steps:** login cả 2 biến thể → gọi luồng chấp nhận điều khoản → login lại biến thể 1.

**Expected Result (PASS):**
- `termsAcceptedAt == null` ⇒ `needTermsAccept = true`, **vẫn cấp token** (BR-009 chặn *truy cập chức năng*, còn UC-034 AF1 mô tả chuyển hướng tới màn chấp nhận).
- `termsAcceptedAt != null` ⇒ `false`.
- Sau khi chấp nhận: `termsAcceptedAt` được set kiểu `Instant` (ISO — D4); login lại ⇒ `false`.

**Expected Result (FAIL):** cờ luôn `false` ⇒ FE không bao giờ hiện màn điều khoản (vi phạm BR-009); hoặc không cấp token ⇒ lệch UC-034 AF1.

**Current Status:** 🔴 Not written

---

#### MF01-TC-009 — Refresh token trượt hạn +14 ngày mỗi lần dùng (Boundary)

**Severity:** `CRITICAL`
**Feature Under Test:** `RefreshTokenService.rotate()`
**Test File:** `service/RefreshTokenServiceTest.java` · **Condition Ref:** `TC-COND-008` · **TDD Phase:** 🔴 RED
**Oracle Source:** **BR-004 (v1.9)** *"Refresh token: … hiệu lực **14 ngày** kể từ lần cấp/gia hạn gần nhất … người dùng không phải đăng nhập lại miễn mở ứng dụng ít nhất 1 lần mỗi 14 ngày. Sau **14 ngày** không mở ứng dụng, refresh token hết hạn."* · TDS ADR-M01 *"Mỗi lần dùng refresh token còn hạn → cấp access token mới và trượt `expiresAt` (sliding)"* · TDS §10 `IAM-008` (401, MSG-004)

**Preconditions:** `refresh_tokens` của `U-0001`: `token_hash=sha256("R1")`, `expires_at=2026-08-13T02:00:00Z`, `revoked_at=NULL`

**Test Steps (Boundary quanh mốc 14 ngày, mỗi nhánh state fresh):**
1. `Clock = 2026-08-12T02:00:00Z` (còn 1 ngày) → `rotate("R1")`.
2. `Clock = 2026-08-13T01:59:59Z` (trước hạn 1s) → `rotate("R1")`.
3. `Clock = 2026-08-13T02:00:01Z` (quá hạn 1s) → `rotate("R1")`.
4. Kiểm bản ghi cũ sau rotate thành công.

**Expected Result (PASS):**
- (1) trả `TokenPair` mới; bản ghi mới `expires_at = 2026-08-26T02:00:00Z` (**= thời điểm gia hạn + 14 ngày**, KHÔNG phải hạn cũ + 14 ngày).
- (2) thành công (còn hạn).
- (3) ném `IAM-008` (401) + **MSG-004** nguyên văn; **không** cấp token.
- (4) bản ghi cũ `revoked_at != null` (rotate = thay thế, không cùng tồn tại).
- **Chỉ lưu hash:** `token_hash != ` chuỗi token trả về; không cột nào chứa token thô.

**Expected Result (FAIL):** `expires_at` mới = hạn cũ + 14 ngày ⇒ token sống vô hạn tiệm tiến; token cũ không revoke ⇒ mở đường replay (`MF01-TC-SEC-009`); lưu token thô ⇒ lộ khi DB bị đọc.

**Current Status:** 🔴 Not written

---

#### MF01-TC-010 — Single session: login mới vô hiệu refresh token cũ (BR-005)

**Severity:** `CRITICAL`
**Feature Under Test:** `AuthService.login()` → `RefreshTokenRepository.revokeAllForUser()`
**Test File:** `service/AuthServiceSessionTest.java` · **Condition Ref:** `TC-COND-009` · **TDD Phase:** 🔴 RED
**Oracle Source:** **BR-005** *"Mỗi tài khoản chỉ được phép đăng nhập trên một thiết bị tại một thời điểm. Đăng nhập mới từ thiết bị khác sẽ vô hiệu hóa phiên hiện tại."* · **BR-004** *"Refresh token bị vô hiệu hóa ngay khi … đăng nhập trên thiết bị khác (BR-005)"* · **UC-034** Normal Flow bước 5 · TDS §14.1 bất biến *"refresh token còn hiệu lực/user ≤ 1"*

**Preconditions:** `FX-U-SCHOOLADMIN` đã có 1 refresh token hợp lệ `R1`

**Test Steps:**
1. Login lần 2 (thiết bị khác) → nhận `R2`.
2. `InOrder` verify: `revokeAllForUser` gọi **trước** `save(R2)`.
3. Đếm refresh token còn hiệu lực của user.

**Expected Result (PASS):**
- `revokeAllForUser(uid, now)` gọi **đúng 1 lần**, **trước** khi insert `R2`.
- Sau login: **đúng 1** bản ghi `revoked_at IS NULL AND expires_at > now` (khớp truy vấn TDS §14.1 trả **0 dòng** vi phạm).
- `R1.revoked_at != null`.

**Expected Result (FAIL):** 2 token cùng hiệu lực ⇒ vi phạm BR-005 (2 thiết bị dùng song song); revoke gọi **sau** insert ⇒ revoke luôn token mới ⇒ người dùng bị đăng xuất ngay khi vừa login.

**Current Status:** 🔴 Not written

---

#### MF01-TC-011 — Chặn xóa role hệ thống (`system=true`)

**Severity:** `HIGH`
**Feature Under Test:** `RoleService.delete()`
**Test File:** `service/RoleServiceGuardTest.java` · **Condition Ref:** `TC-COND-011` · **TDD Phase:** 🔴 RED
**Oracle Source:** **BR-008** *"Các vai trò mặc định của hệ thống không thể bị xóa. Chỉ các vai trò tùy chỉnh (custom) mới có thể bị xóa bởi System Admin."* · **UC-029** Normal Flow bước 4 + Exceptions · TDS §10 `IAM-004` (403, MSG-005) · TDS ADR-M04 (guard khớp `mock-server.ts` dòng 368–372) · TDS §17 C4

**Preconditions:** `FX-ROLE-SYSTEM` (`ROLE-01`…`ROLE-07`), `FX-ROLE-CUSTOM-EMPTY` (`ROLE-08`, 0 user)

**Test Steps (`@ParameterizedTest` 7 role hệ thống + 1 custom, chủ thể = System Admin):**
1. `delete("ROLE-01")` … `delete("ROLE-07")`.
2. `delete("ROLE-08")`.

**Expected Result (PASS):**
- 7 role hệ thống: `IAM-004` (**403**) + **MSG-005** nguyên văn *"Access denied: You do not have permission to access this resource."*; `roles` không giảm bản ghi.
- `ROLE-08`: xóa thành công + audit `DELETE roles ROLE-08`.
- Guard ở **tầng Service** ⇒ test unit (không qua controller) vẫn chặn được.

**Expected Result (FAIL):** xóa được role hệ thống ⇒ mất `School Administration`/`Teacher` ⇒ RBAC 7 role (BR-007) sụp. Nếu guard chỉ có ở controller/UI, TC này FAIL đúng như mong đợi và buộc chuyển guard xuống service.

**Current Status:** 🔴 Not written

---

#### MF01-TC-012 — Chặn xóa role còn user gán, **kể cả user đã vô hiệu hóa**

**Severity:** `HIGH`
**Feature Under Test:** `RoleService.delete()` + `UserAccountRepository.countByRoleId()`
**Test File:** `service/RoleServiceGuardTest.java` · **Condition Ref:** `TC-COND-012` · **TDD Phase:** 🔴 RED
**Oracle Source:** **BR-008** *"Một vai trò chỉ có thể bị xóa khi **không có người dùng nào** (kể cả đã vô hiệu hóa) đang được gán vai trò đó."* · **UC-029 AF1** *"System blocks deletion and shows the count of assigned users"* · **MSG-106**: *"Cannot delete this role: [Number] user(s) are still assigned to it. Please reassign them first."* · TDS §10 `IAM-009` (**409**)

**Preconditions:** `FX-ROLE-CUSTOM-USED` (`ROLE-09`, `system=false`, 3 user: 2 `ACTIVE` + **1 `INACTIVE`**)

**Test Steps:**
1. `delete("ROLE-09")`.
2. Deactivate cả 3 user (tất cả `INACTIVE`) → `delete("ROLE-09")`.
3. Reassign 3 user sang `ROLE-05` → `delete("ROLE-09")`.

**Expected Result (PASS):**
- (1) `IAM-009` (**409**) + MSG-106 render **`[Number]` = 3**.
- (2) **vẫn** `IAM-009` với `[Number] = 3` ⇒ đếm **không** lọc `status` — đúng câu chữ BR-008; đây là nhánh mà mock FE **không phủ được** (`ROLE-08` có `users=0`).
- (3) xóa thành công.
- `countByRoleId` đếm **mọi** `user_accounts.role_id`, không `WHERE status='ACTIVE'`.

**Expected Result (FAIL):** (2) xóa được ⇒ user `INACTIVE` trỏ `role_id` đã bị xóa ⇒ vỡ khóa ngoại/RBAC khi reactivate; `[Number]` không render ⇒ FE hiện chuỗi thô `[Number]` cho người dùng.

**Current Status:** 🔴 Not written

---

#### MF01-TC-013 — Không sửa **tên** role hệ thống; role custom sửa được; tên phải duy nhất

**Severity:** `MEDIUM`
**Feature Under Test:** `RoleService.update()`
**Test File:** `service/RoleServiceGuardTest.java` · **Condition Ref:** `TC-COND-011` · **TDD Phase:** 🔴 RED
**Oracle Source:** **BR-008** (TDS §2.2 diễn giải: *"Role hệ thống mặc định bất khả xóa/sửa tên"*) · **UC-098** Normal Flow bước 5 *"validates that the role is a custom role (BR-008) and name (if modified) is unique"* + Exceptions *"If trying to modify default roles, system blocks the action"* · TDS §10 `IAM-004` (403) · TDS §17 C4

**Test Steps:**
1. `update("ROLE-02", name="BGH")`.
2. `update("ROLE-08", name="Kitchen Auditor v2", description="…", permissions=["PERM-013"])`.
3. `update("ROLE-08", name="Teacher")` — trùng tên `ROLE-05`.

**Expected Result (PASS):**
- (1) `IAM-004` (403) + MSG-005; `ROLE-02.name` **không đổi**.
- (2) thành công; `permissions` lưu đúng danh sách `PERM-*` (JSONB — TDS §5.2).
- (3) bị chặn, **HTTP 409**. ⚠️ **Thiếu oracle mã lỗi:** TDS §10 **không có** mã cho "tên role trùng" ⇒ assert status + ghi nhận cần Nam bổ sung 1 dòng §10 (cùng nhóm việc §2 **L15**).

**Expected Result (FAIL):** đổi được tên `ROLE-02` ⇒ mọi chỗ tra role theo tên bị vỡ; (3) tạo được 2 role cùng tên.

**Current Status:** 🔴 Not written

---

#### MF01-TC-014 — Google login: email chưa có tài khoản ⇒ chặn, **KHÔNG tự tạo account**

**Severity:** `CRITICAL` · **OWASP:** `A01:2021 — Broken Access Control`
**Feature Under Test:** `OAuth2LoginService.linkAndLogin()`
**Test File:** `service/OAuth2LoginServiceTest.java` · **Condition Ref:** `TC-COND-013` · **TDD Phase:** 🔴 RED
**Oracle Source:** **BR-218** *"**Chỉ liên kết, không tự tạo tài khoản mới**: … Nếu email Google **chưa tồn tại** trong hệ thống, đăng nhập bị chặn — hệ thống **không** tự tạo tài khoản mới qua Google (giữ nguyên BR-013: chỉ School Administration/System Admin được tạo tài khoản)."* · **UC-191 Exceptions** *"No matching SFMS account for the Google email: system blocks login and displays MSG-102"* · **MSG-102**: *"Google account not linked to any SFMS account. Please contact your administrator to create your account first."* · TDS §10 `IAM-102` (**403**) · TDS §17 C5 + **AP-AI-008**

**Preconditions:** DB **không** có user email `new.person@gmail.com`; `FX-U-SCHOOLADMIN` tồn tại (chứng minh repo hoạt động)

**Test Steps:**
1. `linkAndLogin("new.person@gmail.com", "sub-x")`.
2. Đếm `user_accounts` trước/sau.
3. Verify không có lời gọi ghi.

**Expected Result (PASS):**
- Ném `IamException` `IAM-102` (**403**) + MSG-102 nguyên văn.
- `count(user_accounts)` **không đổi**; `verify(userRepo, never()).save(any())`.
- **Không** cấp access/refresh; **không** ghi `refresh_tokens`.
- Audit ghi 1 dòng `result='BLOCKED'`.

**Expected Result (FAIL):** tạo user mới — đây là hành vi **mặc định của hầu hết cấu hình OAuth2 auto-provisioning** ⇒ **bất kỳ ai có Gmail cũng tự tạo được tài khoản trong hệ thống ATTP trường học**, vi phạm BR-218 + BR-013 (= AP-AI-008 của TDS §17.4).

**Current Status:** 🔴 Not written
**Implementation Note:** `OAuth2LoginSuccessHandler` **không** dùng `OidcUserService` mặc định kèm auto-create; phải `findByEmail` rồi chặn tường minh.

---

#### MF01-TC-015 — Google login lần đầu ⇒ set `googleSub` cho tài khoản đã tồn tại

**Severity:** `HIGH`
**Feature Under Test:** `OAuth2LoginService.linkAndLogin()`
**Test File:** `service/OAuth2LoginServiceTest.java` · **Condition Ref:** `TC-COND-014` · **TDD Phase:** 🔴 RED
**Oracle Source:** **BR-218** *"nếu email Google trùng với email của một tài khoản SFMS đã tồn tại (được tạo qua UC-024/UC-026), hệ thống tự động liên kết tài khoản đó với Google cho lần đăng nhập đầu tiên"* + *"Email Google được coi là đã xác minh … thỏa điều kiện BR-010"* · **UC-191** Normal Flow bước 6–7 · **MSG-026** *"Login successful. Welcome back, [Name]!"* · TDS §6.2, §9.2, §7.1 (`GoogleIdentityLinked`)

**Preconditions:** `FX-U-SCHOOLADMIN` (`toanpq@dtd.edu.vn`, `ACTIVE`, `googleSub = NULL`)

**Test Steps:** `linkAndLogin("toanpq@dtd.edu.vn", "sub-1")`.

**Expected Result (PASS):**
- `user.googleSub == "sub-1"`.
- Trả `LoginResult` có access + refresh (BR-004) và `role = "school-admin"` (**kebab-case** — D6, §2 **L4**).
- `revokeAllForUser` gọi 1 lần **trước** khi cấp refresh mới (BR-005).
- `GoogleIdentityLinked` publish **đúng 1 lần** (TDS §7.1).
- Audit `LOGIN SUCCESS` ghi rõ nguồn Google.

**Expected Result (FAIL):** `googleSub` không lưu ⇒ lần sau lại đi nhánh "link lần đầu"; `role` trả nhãn `"School Administration"` ⇒ FE routing sai (D6 / §2 L4).

**Current Status:** 🔴 Not written

---

#### MF01-TC-016 — Google login AF1 "đã liên kết" ⇒ bỏ bước link, không ghi đè `googleSub`

**Severity:** `MEDIUM`
**Feature Under Test:** `OAuth2LoginService.linkAndLogin()`
**Test File:** `service/OAuth2LoginServiceTest.java` · **Condition Ref:** `TC-COND-014` · **TDD Phase:** 🔴 RED
**Oracle Source:** **UC-191 AF1** *"Already linked: skip the linking step (step 6), log in directly."* · TDS §6.2 *"(AF1: đã link thì bỏ)"* · TDS §4.2 *"`googleSub` duy nhất — 100%"* (BR-218)

**Preconditions:** `FX-U-GOOGLE-LINKED` (`U-0009`, `googleSub="google-sub-0009"`)

**Test Steps:**
1. `linkAndLogin("hongpt@dtd.edu.vn", "google-sub-0009")` — đúng `sub`.
2. `linkAndLogin("hongpt@dtd.edu.vn", "sub-KHAC")` — email khớp nhưng `sub` khác.

**Expected Result (PASS):**
- (1) login thành công; `googleSub` **không đổi**; trả token.
- (2) `googleSub` **không bị ghi đè** bằng `sub-KHAC`.
- ⚠️ **Thiếu oracle:** BR-218 và UC-191 **không quy định** xử lý "email khớp nhưng `sub` khác". TC chỉ assert bất biến *"`googleSub` không bị ghi đè"* (suy từ NFR §4.2 `googleSub` duy nhất) và **đánh dấu cần Nam/Huy chốt** hành vi (chặn `IAM-103`? hay cho login?).

**Expected Result (FAIL):** `googleSub` bị ghi đè ⇒ liên kết Google cũ mất hiệu lực và mở đường chiếm tài khoản nếu attacker kiểm soát được một tài khoản Google trùng email.

**Current Status:** 🔴 Not written

---

#### MF01-TC-017 — Phiên Google **không** áp BR-001/BR-006/BR-015, nhưng **vẫn** áp BR-002/BR-010

**Severity:** `HIGH`
**Feature Under Test:** `OAuth2LoginService.linkAndLogin()`
**Test File:** `service/OAuth2LoginServiceTest.java` · **Condition Ref:** `TC-COND-015` · **TDD Phase:** 🔴 RED
**Oracle Source:** **BR-218** *"Không áp dụng BR-001/BR-006/BR-015 (hash/chính sách/lịch sử mật khẩu) cho phiên đăng nhập qua Google vì không có mật khẩu cục bộ. Các BR còn lại (BR-002 lockout, BR-004 session/refresh token, BR-005 single-session, BR-007 RBAC) áp dụng như bình thường."* · **UC-191 Other Information** · TDS §17 C1/C5

**Preconditions:** user Google-only (`passwordHash = NULL`, `googleSub="sub-1"`); `FX-U-INACTIVE`

**Test Steps:**
1. `linkAndLogin("toanpq@dtd.edu.vn", "sub-1")` với `passwordHash=NULL`.
2. Verify không tương tác thành phần mật khẩu.
3. `linkAndLogin` cho `FX-U-INACTIVE` (`U-0016`).
4. Đặt `lockedUntil = now + 10'` rồi `linkAndLogin`.

**Expected Result (PASS):**
- (1) thành công dù `passwordHash = NULL` (không NPE).
- (2) `verifyNoInteractions(passwordEncoder, passwordHistoryRepo, passwordPolicyValidator)`.
- (3) `IAM-003` (403) ⇒ BR-010 vẫn hiệu lực với login Google.
- (4) `IAM-007` (423) ⇒ BR-002 vẫn hiệu lực.

**Expected Result (FAIL):** đòi MK/chính sách cho phiên Google ⇒ user Google-only không bao giờ vào được; **hoặc** bỏ qua BR-002/BR-010 ở nhánh Google ⇒ **đường vòng qua toàn bộ guard đăng nhập** (rủi ro lớn nhất của TC này).

**Current Status:** 🔴 Not written

---

#### MF01-TC-018 — Token kích hoạt hiệu lực **48h** (Boundary 47h59'/48h/48h01')

**Severity:** `HIGH`
**Feature Under Test:** `AccountActivationService.activate()`
**Test File:** `service/AccountActivationServiceTest.java` · **Condition Ref:** `TC-COND-016` · **TDD Phase:** 🔴 RED
**Oracle Source:** **BR-011** *"Link kích hoạt tài khoản (Account Activation) chỉ có hiệu lực trong **48 giờ** kể từ khi tạo."* · **UC-154** Normal Flow bước 2 *"verifying that it has not expired (must be within 48 hours of creation per BR-011)"*, bước 5–7 · TDS §10 `IAM-008` (401, MSG-004) · TDS §6.3 (`PENDING_VERIFICATION → ACTIVE`)

**Preconditions:** `FX-U-PENDING`, `FX-TOKEN-ACT-VALID`/`FX-TOKEN-ACT-EXPIRED`, `FX-CLOCK`, `FX-PWD-VALID`

**Test Steps (Boundary):**

| # | Tuổi token | Expected |
|---|---|---|
| a | 47h59' | thành công |
| b | 48h00' (đúng mốc `expires_at`) | thành công — *"trong 48 giờ"* ⇒ **≤** |
| c | 48h01' | `IAM-008` (401) + MSG-004 |

4. Sau (a): kiểm trạng thái DB. 5. MK yếu ở bước đặt MK lần đầu.

**Expected Result (PASS):**
- (a)/(b) `status: PENDING_VERIFICATION → ACTIVE`; `email_verified = true` (hoàn tất BR-010); `password_hash` là BCrypt của MK mới (BR-001); token `used_at != NULL`.
- (c) không đổi trạng thái, không set MK.
- (5) MK yếu ⇒ `IAM-006policy` + MSG-104 (UC-154 bước 5 dẫn BR-006).
- ⚠️ **Không** assert success message — §2 **L9** (`MISSING-ORACLE`: UC file nói MSG-031, traceability nói MSG-028, cả hai đều sai ngữ nghĩa "activation success").

**Expected Result (FAIL):** so sánh `<` tại đúng mốc 48h ⇒ chặn oan người click link đúng hạn; (c) qua được ⇒ token sống mãi.

**Current Status:** 🔴 Not written

---

#### MF01-TC-019 — Token kích hoạt **dùng 1 lần**; tài khoản đã ACTIVE ⇒ không kích hoạt lại

**Severity:** `HIGH` · **CWE:** `CWE-640 — Weak Password Recovery Mechanism`
**Feature Under Test:** `AccountActivationService.activate()`
**Test File:** `service/AccountActivationServiceTest.java` · **Condition Ref:** `TC-COND-016` · **TDD Phase:** 🔴 RED
**Oracle Source:** **UC-154** Postconditions *"The verification token is invalidated"* + Exceptions *"Account Already Active: System redirects the user to the Login screen with an information message"* · TDS ADR-M02 *"reset/activation token lưu **hash**, dùng **1 lần**"* · TDS §10 `IAM-008`

**Test Steps:**
1. `activate(token, "P@ssw0rd123")` → thành công.
2. `activate(token, "Another@Pass1")` — dùng lại đúng token đó.
3. Kiểm `password_hash` sau bước 2.

**Expected Result (PASS):**
- (2) `IAM-008` (401) + MSG-004; **`password_hash` KHÔNG bị đổi** sang `Another@Pass1`.
- Token lưu **hash** ⇒ truy vấn `WHERE token_hash = sha256(input)`; không cột nào chứa token thô.
- Đã `ACTIVE` + `used_at != NULL` ⇒ luôn từ chối.

**Expected Result (FAIL):** token dùng lại được ⇒ ai có link trong hộp thư (hoặc trong log/Referer) **đổi được mật khẩu** của tài khoản đã kích hoạt ⇒ chiếm tài khoản.

**Current Status:** 🔴 Not written

---

#### MF01-TC-020 — Gửi lại link kích hoạt ⇒ gia hạn thêm 48h (UC-154 AF1)

**Severity:** `MEDIUM`
**Feature Under Test:** `AccountActivationService.resend()`
**Test File:** `service/AccountActivationServiceTest.java` · **Condition Ref:** `TC-COND-017` · **TDD Phase:** 🔴 RED
**Oracle Source:** **BR-011** *"Nếu hết hạn, hệ thống cho phép gửi lại link mới, gia hạn thêm 48 giờ."* · **UC-154 AF1** bước 4 *"generates a new activation token, extends it for another 48 hours, sends a new activation email via Email Service"* · TDS §8.1 `resend(UUID userId) // UC-154 AF1: +48h`

**Preconditions:** `FX-U-PENDING`, `FX-TOKEN-ACT-EXPIRED`, `FX-GREENMAIL`, `FX-CLOCK = T0`

**Test Steps:** `resend(uid)` → kiểm token + email → `activate` bằng token **mới**, rồi bằng token **cũ**.

**Expected Result (PASS):**
- Bản ghi token **mới**: `expires_at = T0 + 48h`, `used_at = NULL`.
- Token **cũ** bị vô hiệu ⇒ `activate(token_cũ)` trả `IAM-008`.
- **Đúng 1** email tới `lanht@dtd.edu.vn` (GreenMail).
- `activate(token_mới)` thành công.

**Expected Result (FAIL):** gia hạn bản ghi cũ tại chỗ (`expires_at += 48h`) mà giữ nguyên chuỗi token ⇒ mâu thuẫn *"link mới"* và token cũ vẫn sống; hoặc cả 2 token cùng dùng được ⇒ vi phạm nguyên tắc 1-lần.

**Current Status:** 🔴 Not written

---

#### MF01-TC-021 — Token reset MK hiệu lực **1 giờ** (Boundary 59'/60'/61')

**Severity:** `HIGH`
**Feature Under Test:** `PasswordResetService.reset()`
**Test File:** `service/PasswordResetServiceTest.java` · **Condition Ref:** `TC-COND-018` · **TDD Phase:** 🔴 RED
**Oracle Source:** **BR-003** *"Token đặt lại mật khẩu (Forgot Password) chỉ có hiệu lực trong **1 giờ** kể từ khi tạo."* · **UC-032 AF1 Token Expired** + Normal Flow bước 6 · **MSG-030** *"Password changed successfully."* · TDS §10 `IAM-008` (401, MSG-004)

**Preconditions:** `FX-U-SCHOOLADMIN`, `FX-TOKEN-RESET-VALID`/`FX-TOKEN-RESET-EXPIRED`, `FX-CLOCK`, `FX-PWD-HISTORY`

**Test Steps (Boundary):**

| # | Tuổi token | Expected |
|---|---|---|
| a | 59' | thành công, MSG-030 |
| b | 60' (đúng mốc) | thành công |
| c | 61' | `IAM-008` (401) + MSG-004 |

4. Với (a): đặt MK trùng `Old@Pass2` ⇒ phải bị chặn; đặt MK yếu ⇒ phải bị chặn; kiểm revoke phiên.

**Expected Result (PASS):**
- (a)/(b) `password_hash` đổi (BCrypt); token `used_at != NULL`; MSG-030 nguyên văn; **`revokeAllForUser` được gọi** (UC-032 bước 6 *"invalidates all other active sessions"*).
- (c) MK không đổi.
- (4) trùng MK ⇒ `IAM-006reuse` + MSG-105; MK yếu ⇒ `IAM-006policy` + MSG-104 (UC-032 bước 6 dẫn BR-006/BR-001).

**Expected Result (FAIL):** token quá hạn dùng được; **hoặc** luồng reset bỏ qua BR-006 — lỗi rất thường gặp (policy chỉ kiểm ở `changePassword`, quên ở `reset`) ⇒ qua đường "quên mật khẩu" đặt được MK yếu/trùng MK cũ.

**Current Status:** 🔴 Not written

---

#### MF01-TC-022 — Rate-limit forgot-password **1 req/15'/email** + không lộ email tồn tại

**Severity:** `HIGH` · **CWE:** `CWE-204 — Observable Response Discrepancy`
**Feature Under Test:** `PasswordResetService.request()`
**Test File:** `service/PasswordResetServiceTest.java` · **Condition Ref:** `TC-COND-018` · **TDD Phase:** 🔴 RED
**Oracle Source:** **BR-003** *"Hệ thống giới hạn tối đa **1 yêu cầu** đặt lại mật khẩu mỗi **15 phút** cho mỗi email."* · **UC-032 Other Information** *"Rate-limiting applies: maximum 1 reset request per 15 minutes per email."* · TDS §8.1 *"Không lộ email tồn tại hay không"* · TDS §9.1 rate limit `1/15min/email`

**Preconditions:** `FX-U-SCHOOLADMIN`, `FX-GREENMAIL`, `FX-CLOCK = T0`

**Test Steps (Boundary + Error Guessing):**
1. `request("toanpq@dtd.edu.vn")` tại `T0`.
2. `request` lại tại `T0 + 14'59"`.
3. `request` lại tại `T0 + 15'00"`.
4. `request("khong.ton.tai@gmail.com")`.
5. So sánh response (1) vs (4).

**Expected Result (PASS):**
- (1) 1 email + 1 bản ghi `password_reset_tokens`.
- (2) **không** email thêm, **không** token thêm (đếm GreenMail vẫn = 1).
- (3) gửi được email thứ 2 ⇒ cửa sổ đúng 15 phút.
- (4) **0** email, **0** token.
- (5) status + body của (4) **giống hệt** (1) ⇒ không phân biệt được email có tồn tại.

**Expected Result (FAIL):** (4) trả 404 hoặc message khác ⇒ **enumeration**: dò được danh sách email hợp lệ của trường (PII/PDPD); (2) gửi email thứ 2 ⇒ spam hộp thư người dùng qua endpoint public.

**Current Status:** 🔴 Not written

---

#### MF01-TC-023 — Ảnh hồ sơ vượt dung lượng ⇒ MSG-008 nguyên văn

**Severity:** `LOW`
**Feature Under Test:** `ProfileService.update()`
**Test File:** `service/ProfileServiceTest.java` · **Condition Ref:** `TC-COND-022` · **TDD Phase:** 🔴 RED
**Oracle Source:** **MSG-008** (nguyên văn): *"File upload failed: File size exceeds the maximum limit of 10MB."* · **UC-097 Exceptions** *"If photo exceeds 5MB, system displays MSG-008"* · TDS §10 `IAM-010` (422)
**⚠️ Xung đột treo — CF-4 + §2 L14:** UC-097 nói **5MB**, MSG-008 nói **10MB**. TC assert **10MB** (D7: nội dung message theo `error_message.md`) và **message nguyên văn MSG-008**, không dùng bản paraphrase gộp của TDS §10.

**Test Steps (Boundary):** `10MB − 1B` → OK · `10MB` → OK · `10MB + 1B` → chặn.

**Expected Result (PASS):** `IAM-010` (422) + **MSG-008 nguyên văn**; file không được ghi; hồ sơ không đổi.
**Expected Result (FAIL):** ngưỡng 5MB ⇒ chặn file 6MB trong khi message nói 10MB (người dùng không hiểu vì sao); hoặc message dùng chuỗi gộp *"size exceeds 10MB / Unsupported file type"* ⇒ vi phạm D7.

**Current Status:** 🔴 Not written

---

#### MF01-TC-024 — Ảnh hồ sơ sai định dạng ⇒ MSG-009 nguyên văn

**Severity:** `LOW` · **CWE:** `CWE-434 — Unrestricted Upload of File with Dangerous Type`
**Feature Under Test:** `ProfileService.update()`
**Test File:** `service/ProfileServiceTest.java` · **Condition Ref:** `TC-COND-022` · **TDD Phase:** 🔴 RED
**Oracle Source:** **MSG-009** (nguyên văn): *"File upload failed: Unsupported file type. Only PDF, JPG, and PNG are allowed."* · **UC-097 Exceptions** *"If photo format is unsupported, system displays MSG-009"* · TDS §10 `IAM-010` (422)
**⚠️ Ghi chú:** UC-097 Normal Flow bước 3 nói *"JPG/JPEG/PNG"* (không có PDF), MSG-009 cho phép **PDF, JPG, PNG**. TC theo **MSG-009** (D7) và ghi nhận lệch để Huy đối chiếu — cùng nhóm CF-3/CF-4.

**Test Steps (`@ParameterizedTest`):** `.jpg` OK · `.jpeg` OK · `.png` OK · `.pdf` OK ⚠️(theo MSG-009) · `.gif` chặn · `.svg` chặn · `.exe` đổi tên thành `.jpg` (magic bytes sai) chặn.

**Expected Result (PASS):** đúng bảng; **MSG-009 nguyên văn**; kiểm theo **magic bytes**, không chỉ theo phần mở rộng.
**Expected Result (FAIL):** chấp nhận `.svg` (XSS qua SVG) hoặc `.exe` đổi tên ⇒ lỗ hổng upload; chỉ kiểm extension.

**Current Status:** 🔴 Not written

---

### 4.2 INTEGRATION TEST CASES (endpoint → DB)

> `@SpringBootTest` + `MockMvc` + **Testcontainers PostgreSQL 16** + Flyway (`V1__init_schema.sql` + `V2__seed_roles_permissions_policy.sql` + `db/testseed`). Timeout 120s. Kế thừa `AbstractIamIntegrationTest` (§3 TDS-05).
> Thư mục: `src/test/java/com/sfms/iam/web/`
> **Mọi path có tiền tố `/api/v1` (D3).** Mọi `id` trong JSON và path param là **mã nghiệp vụ** (D1). Mọi field ngày là **ISO-8601** (D4).

---

#### MF01-TC-INT-001 — Login happy path: response shape + hợp đồng D1/D2/D4/D6 + trạng thái DB

**Severity:** `CRITICAL`
**Feature Under Test:** `POST /api/v1/auth/login` — full flow controller → service → DB → audit
**Test File:** `web/AuthControllerIT.java` · **Condition Ref:** `TC-COND-034` · **TDD Phase:** 🔴 RED
**Oracle Source:** **UC-034** Normal Flow bước 8 · **BR-004** (access 30' + refresh 14d) · **MSG-026** · TDS §9.2 (`POST /api/v1/auth/login` 200 OK) · TDS §9.2 chú thích claim JWT (`sub`, `uid`, `role`, `schoolId`, `partnerId`) · **D1** (`id` = mã nghiệp vụ) · **D3** (`/api/v1`) · **D4** (ISO-8601) · **D6** (`RoleKey` kebab-case)

**Preconditions:** Flyway migrate xong; seed `FX-U-SCHOOLADMIN`, `FX-POLICY`, `FX-SCHOOL-A`; `FX-CLOCK`

**Test Steps:**
1. `POST /api/v1/auth/login` body `{"email":"toanpq@dtd.edu.vn","password":"P@ssw0rd123"}`.
2. Assert response body theo TDS §9.2.
3. Giải mã payload JWT (không verify chữ ký) → assert claim.
4. Assert DB: `user_accounts`, `refresh_tokens`, `audit_logs`.

**Expected Result (PASS):**
- HTTP **200**; body **đúng 6 khóa** của TDS §9.2: `accessToken`, `refreshToken`, `role`, `schoolId`, `partnerId`, `needTermsAccept`.
- `role == "school-admin"` (**kebab-case**, D6 — §2 **L4**); `schoolId == "SCH-HN-0042"` (**mã nghiệp vụ**, không UUID — D1); `partnerId == null`.
- Body **không** chứa `passwordHash`, `googleSub`, `id` dạng UUID (D1: UUID không lộ ra API).
- Claim JWT: `sub` = email, `uid`, `role` (kebab-case), `schoolId`, `partnerId`; `exp - iat == 30 * 60` (BR-004, đọc từ `SEC-DEFAULT.sessionTimeoutMinutes`).
- **DB assertion:**
```sql
-- 1 refresh token còn hiệu lực, KHÔNG lưu token thô
SELECT count(*) FROM refresh_tokens
 WHERE user_id = (SELECT id FROM user_accounts WHERE code='U-0001')
   AND revoked_at IS NULL AND expires_at > now();            -- kỳ vọng 1
SELECT count(*) FROM refresh_tokens WHERE token_hash = :rawRefreshToken;  -- kỳ vọng 0
-- expires_at = thời điểm cấp + 14 ngày (BR-004)
SELECT (expires_at - created_at) BETWEEN interval '13 days 23 hours' AND interval '14 days 1 hour'
  FROM refresh_tokens WHERE revoked_at IS NULL;              -- kỳ vọng true
-- lastLogin cập nhật, counter reset
SELECT failed_login_count, locked_until, last_login IS NOT NULL FROM user_accounts WHERE code='U-0001';  -- 0, NULL, true
-- audit
SELECT count(*) FROM audit_logs WHERE result='SUCCESS' AND action LIKE '%LOGIN%';   -- ≥ 1
```
- Mọi field ngày trong response (nếu có) khớp `^\d{4}-\d{2}-\d{2}(T.*Z)?$` — **không** `dd/MM/yyyy` (D4).

**Expected Result (FAIL):** thiếu `partnerId` (12 module downstream mất khóa cách ly đa-tenant — TDS §9.2 review B3); `role` trả nhãn tiếng Việt/`"School Administration"`; `schoolId` trả UUID (FE không nối được); ngày dạng `dd/MM/yyyy`; token thô nằm trong DB.

**Current Status:** 🔴 Not written

---

#### MF01-TC-INT-002 — Login sai MK: envelope D2 + `attemptsLeft` + lockout ở lần 5

**Severity:** `CRITICAL` · ✅ **Oracle chốt 2026-07-31 (TDS-MF01 §9.2)** — §2 **L3**
**Feature Under Test:** `POST /api/v1/auth/login` (error path) + `@RestControllerAdvice`
**Test File:** `web/AuthControllerErrorIT.java` · **Condition Ref:** `TC-COND-004`, `TC-COND-034` · **TDD Phase:** 🔴 RED
**Oracle Source:** **BR-002** · **MSG-002** / **MSG-076** nguyên văn · TDS §9.2 (401/423) · TDS §10 `IAM-002`/`IAM-007` · **D2** envelope `{ error: { code, message, details } }`, `details` là mảng, không bao giờ `null`

**Test Steps:**
1. Login sai 4 lần → assert từng response.
2. Lần 5 → assert.
3. Assert **không** có khóa lạ trong `error`.

**Expected Result (PASS):**
- Lần 1–4: HTTP **401**; `error.code == "IAM-002"`; `error.message` = MSG-002 nguyên văn; **`error.details` là mảng** chứa số lần thử còn lại: `[{"field":"attemptsLeft","value":3}]` (4→3→2→1).
- `error` **chỉ có 3 khóa** — assert `jsonPath("$.error.attemptsLeft").doesNotExist()` (D2 thắng TDS §9.2 — §2 **L3**).
- Lần 5: HTTP **423**; `code == "IAM-007"`; `message` = MSG-076 render *"…after 15 minutes."*
- Response **không** tiết lộ email có tồn tại hay không: login email không tồn tại cũng trả **401 `IAM-002`** với cùng body (không 404).
- ✅ Tên khóa trong `details` đã chốt vào TDS §9.2 (2026-07-31) — không còn chặn implement.

**Expected Result (FAIL):** `attemptsLeft` là sibling của `code`/`message` ⇒ vi phạm D2 và FE `data?.error?.message` vẫn chạy nhưng interceptor không đọc được `details`; `details = null` ⇒ FE `.map()` trên null; email lạ trả 404 ⇒ enumeration.

**Current Status:** 🔴 Not written

---

#### MF01-TC-INT-003 — `POST /auth/refresh`: rotate + trượt hạn; token cũ dùng lại ⇒ 401

**Severity:** `CRITICAL`
**Feature Under Test:** `POST /api/v1/auth/refresh`
**Test File:** `web/AuthRefreshIT.java` · **Condition Ref:** `TC-COND-008` · **TDD Phase:** 🔴 RED
**Oracle Source:** **BR-004 (v1.9)** · TDS §9.2 (`200: {accessToken, refreshToken}` — *"refresh cũ bị thay, hạn trượt +14 ngày"*) · TDS §10 `IAM-008` (401, MSG-004) · TDS §13 TC-INT-001 (mở rộng)

**Test Steps:**
1. Login → nhận `R1`.
2. `POST /api/v1/auth/refresh` body `{"refreshToken":"R1"}` → nhận `R2`.
3. Gọi lại với `R1`.
4. Dùng access token mới gọi 1 endpoint có bảo vệ.
5. Assert DB.

**Expected Result (PASS):**
- (2) HTTP 200, body **đúng 2 khóa** `accessToken`, `refreshToken`; `R2 != R1`.
- (3) HTTP **401** `IAM-008` + MSG-004 nguyên văn ⇒ token đã rotate không dùng lại được.
- (4) access token mới hợp lệ, claim giữ nguyên `uid`/`role`/`schoolId`/`partnerId`.
- **DB:**
```sql
SELECT count(*) FROM refresh_tokens WHERE user_id=:uid AND revoked_at IS NULL;  -- 1 (chỉ R2)
SELECT revoked_at IS NOT NULL FROM refresh_tokens WHERE token_hash=:hashR1;     -- true
```

**Expected Result (FAIL):** `R1` vẫn dùng được sau rotate ⇒ token bị đánh cắp vẫn sống song song (`MF01-TC-SEC-009`); hoặc `R2` không được cấp ⇒ người dùng bị đăng xuất sau 30 phút, phá vỡ mục đích BR-004 v1.9.

**Current Status:** 🔴 Not written

---

#### MF01-TC-INT-004 — Single session end-to-end: login thiết bị 2 ⇒ refresh thiết bị 1 chết

**Severity:** `HIGH`
**Feature Under Test:** `POST /auth/login` × 2 + `POST /auth/refresh`
**Test File:** `web/AuthSessionIT.java` · **Condition Ref:** `TC-COND-009` · **TDD Phase:** 🔴 RED
**Oracle Source:** **BR-005** · **BR-004** *"…hoặc đăng nhập trên thiết bị khác (BR-005 — single session, refresh token cũ bị thay thế)"* · TDS §13 TC-INT-001 (mở rộng) · TDS §14.1 bất biến ≤1 refresh/user

**Test Steps:**
1. Login (thiết bị A) → `R_A`.
2. `refresh(R_A)` → `R_A2` (chứng minh A đang hoạt động).
3. Login (thiết bị B, cùng tài khoản) → `R_B`.
4. `refresh(R_A2)`.
5. `refresh(R_B)`.
6. Assert bất biến DB.

**Expected Result (PASS):**
- (4) HTTP **401** `IAM-008` ⇒ phiên A bị vô hiệu đúng lúc B login.
- (5) thành công.
- (6) truy vấn TDS §14.1 trả **0 dòng vi phạm**:
```sql
SELECT user_id, count(*) FROM refresh_tokens
 WHERE revoked_at IS NULL AND expires_at > now()
 GROUP BY user_id HAVING count(*) > 1;      -- 0 dòng
```
- Access token cũ của A **vẫn hợp lệ tới khi hết 30'** (stateless JWT — TDS ADR-M01 nêu rõ access là stateless). Đây là **giới hạn kiến trúc đã chấp nhận**, TC ghi nhận tường minh chứ không assert sai kỳ vọng.

**Expected Result (FAIL):** (4) thành công ⇒ dùng song song 2 thiết bị, vi phạm BR-005.
**⚠️ Ghi chú cho Nam (kiến trúc):** BR-005 nói *"vô hiệu hóa phiên hiện tại"* nhưng access token JWT stateless **không thể** thu hồi trước hạn. Khoảng hở tối đa = `sessionTimeoutMinutes` (30'). TDS ADR-M01 đã chọn phương án A biết trước điều này; TC này **ghi nhận** khoảng hở để reviewer không hiểu là bug.

**Current Status:** 🔴 Not written

---

#### MF01-TC-INT-005 — Logout ⇒ revoke refresh + MSG-027

**Severity:** `HIGH`
**Feature Under Test:** `POST /api/v1/auth/logout`
**Test File:** `web/AuthControllerIT.java` · **Condition Ref:** `TC-COND-010` · **TDD Phase:** 🔴 RED
**Oracle Source:** **UC-035** Normal Flow bước 4–5 *"invalidates the session token (BR-004) and clears session data … logs the session termination … displays MSG-027"* · **BR-004** *"Refresh token bị vô hiệu hóa ngay khi: người dùng logout thủ công"* · **MSG-027** *"Logout successful. Session terminated safely."* · TDS §8.1 `void logout(UUID userId)`

**Test Steps:**
1. Login → `R1` + access token.
2. `POST /api/v1/auth/logout` với `Authorization: Bearer <access>`.
3. `refresh(R1)`.
4. `POST /auth/logout` lần 2 với cùng access token.
5. Assert audit.

**Expected Result (PASS):**
- (2) HTTP 200 + MSG-027 nguyên văn; **DB:** `refresh_tokens.revoked_at IS NOT NULL` cho `R1`; **0** refresh token còn hiệu lực của user.
- (3) HTTP **401** `IAM-008`.
- (4) **idempotent** — không lỗi 500; vẫn 200 (hoặc 401 nếu access đã hết hạn), không tạo dòng revoke mới.
- (5) `audit_logs` có dòng logout.
- `userId` lấy **từ JWT**, không từ body (TDS §17 C2) — gửi body `{"userId":"<uid khác>"}` **không** logout được người khác (giao với `MF01-TC-SEC-005`).

**Expected Result (FAIL):** refresh token còn sống sau logout ⇒ "logout" chỉ xóa state ở FE, phiên vẫn dùng được từ thiết bị khác — đúng loại lỗ hổng "gate chỉ tồn tại ở client".

**Current Status:** 🔴 Not written

---

#### MF01-TC-INT-006 — Deactivate user ⇒ revoke refresh tức thì + MSG-029 (UC-003)

**Severity:** `CRITICAL`
**Feature Under Test:** `PATCH /api/v1/users/{code}/status`
**Test File:** `web/UserStatusIT.java` · **Condition Ref:** `TC-COND-010` · **TDD Phase:** 🔴 RED
**Oracle Source:** **UC-003** Postconditions *"If deactivated, all active sessions are immediately terminated (BR-007). An email notification is sent to the user. Audit log records the action."* + Normal Flow bước 6 (MSG-029) · **BR-004** *"…hoặc bị vô hiệu hóa (UC-002/UC-003)"* · **MSG-029** *"Profile updated successfully."* (⚠️ **CF-6**: ngữ nghĩa lệch — TDS giữ MSG-029 theo traceability) · TDS §7.1 `UserAccountDeactivated` · TDS §13 TC-INT-002 (mở rộng)

**Test Steps:**
1. `FX-U-TEACHER` login → `R_T`.
2. `FX-JWT-SYSADMIN` gọi `PATCH /api/v1/users/U-0004/status` body `{"active": false}`.
3. `refresh(R_T)`.
4. Login lại bằng `FX-U-TEACHER`.
5. Assert email + audit + event.
6. Reactivate `{"active": true}` → login lại.

**Expected Result (PASS):**
- (2) HTTP 200 + **MSG-029 nguyên văn**; DB `user_accounts.status = 'INACTIVE'`.
- (3) HTTP **401** `IAM-008` ⇒ revoke **tức thì**, không chờ hết hạn.
- (4) HTTP **403** `IAM-003` + MSG-003 (BR-010).
- (5) **đúng 1** email tới `trangnt@dtd.edu.vn` (GreenMail); `audit_logs` có dòng deactivate; event `UserAccountDeactivated` với `payload.revokedRefreshTokens = 1` và `previousStatus = "ACTIVE"` (TDS §7.3).
- (6) login lại **thành công** sau reactivate (transition `INACTIVE → ACTIVE`, TDS §6.3).
- Path param là **mã nghiệp vụ** `U-0004`, không UUID (D1); gọi bằng UUID ⇒ 404.

**Expected Result (FAIL):** refresh token còn sống sau deactivate ⇒ **người bị vô hiệu hóa vẫn truy cập tới 14 ngày** — nghiêm trọng nhất trong nhóm này; hoặc không gửi email/không audit (vi phạm UC-003 Postconditions).

**Current Status:** 🔴 Not written

---

#### MF01-TC-INT-007 — Activation end-to-end: `PENDING_VERIFICATION → ACTIVE` (UC-154)

**Severity:** `HIGH` · ⚠️ **MISSING-ORACLE (success MSG)** — §2 **L9**
**Feature Under Test:** `POST /api/v1/auth/activate` + `POST /api/v1/auth/activate/resend`
**Test File:** `web/AccountActivationIT.java` · **Condition Ref:** `TC-COND-016`, `TC-COND-017` · **TDD Phase:** 🔴 RED
**Oracle Source:** **UC-154** Postconditions *"The user account status is updated to 'ACTIVE'. The initial password is set and encrypted. The verification token is invalidated. The activation is logged in the audit trail."* · **BR-011** (48h) · **BR-001** (BCrypt) · **BR-006** (policy) · **BR-010** · traceability §1.1 UC-154 ⇒ **MSG-028** (xem CF-2)

**Test Steps:**
1. Seed `FX-U-PENDING` + `FX-TOKEN-ACT-VALID`.
2. `POST /api/v1/auth/activate` body `{"token":"<raw>","newPassword":"P@ssw0rd123"}`.
3. Login bằng tài khoản vừa kích hoạt.
4. Gọi lại `activate` với cùng token.
5. Với `FX-TOKEN-ACT-EXPIRED`: `activate` → rồi `activate/resend` → `activate` bằng token mới.

**Expected Result (PASS):**
- (2) HTTP **200**; **DB:** `status='ACTIVE'`, `email_verified=true`, `password_hash LIKE '$2%'`, `account_activation_tokens.used_at IS NOT NULL`; `audit_logs` +1 dòng.
- (3) login thành công, `needTermsAccept = true` (lần đầu — BR-009).
- (4) HTTP **401** `IAM-008`; `password_hash` **không đổi**.
- (5) `activate` hết hạn ⇒ 401; `resend` ⇒ 200 + 1 email + token mới `expires_at = now+48h`; `activate` token mới ⇒ 200.
- ⚠️ **Không assert success message** — UC file nói MSG-031, traceability nói MSG-028, cả hai đều không mang nghĩa "kích hoạt xong" (§2 **L9**). Chờ Huy/Nam.

**Expected Result (FAIL):** tài khoản `ACTIVE` mà `password_hash` vẫn `NULL` ⇒ login không bao giờ được; hoặc `email_verified` không set ⇒ BR-010 chặn login sau khi vừa kích hoạt (bug hay gặp: chỉ đổi `status`, quên cờ email).

**Current Status:** 🔴 Not written

---

#### MF01-TC-INT-008 — Forgot → Reset end-to-end (UC-032)

**Severity:** `HIGH`
**Feature Under Test:** `POST /api/v1/auth/forgot-password` + `POST /api/v1/auth/reset-password`
**Test File:** `web/PasswordResetIT.java` · **Condition Ref:** `TC-COND-018` · **TDD Phase:** 🔴 RED
**Oracle Source:** **UC-032** Normal Flow bước 4–6 + Postconditions · **BR-003** (1h, 1 req/15') · **BR-001**, **BR-006** · **MSG-030** · **MSG-003** (Exceptions: tài khoản deactivated) · TDS §10 `IAM-008`

**Test Steps:**
1. Login → `R1` (phiên đang mở).
2. `POST /auth/forgot-password` `{"email":"toanpq@dtd.edu.vn"}` → lấy token từ email GreenMail.
3. `POST /auth/reset-password` `{"token":"<raw>","newPassword":"N3w@Pass456"}`.
4. `refresh(R1)`.
5. Login bằng MK cũ; rồi bằng MK mới.
6. `reset-password` lại với cùng token.
7. `forgot-password` cho `FX-U-INACTIVE`.

**Expected Result (PASS):**
- (2) HTTP 200; **đúng 1** email; 1 bản ghi `password_reset_tokens` (`expires_at = now + 1h`), lưu **hash**.
- (3) HTTP 200 + **MSG-030 nguyên văn**; `password_hash` đổi; `password_history` +1 (giữ tối đa 3).
- (4) **401** `IAM-008` ⇒ *"invalidates all other active sessions"* (UC-032 bước 6).
- (5) MK cũ ⇒ 401 `IAM-002`; MK mới ⇒ 200.
- (6) **401** `IAM-008` (token 1 lần).
- (7) theo UC-032 Exceptions: **MSG-003**; ⚠️ nhưng phải **không** phân biệt được với email không tồn tại (`MF01-TC-022` / `SEC-011`) ⇒ TC ghi nhận **xung đột thiết kế** giữa *"MSG-003 cho tài khoản deactivated"* (UC-032) và *"không lộ email tồn tại"* (TDS §8.1). **Cần Nam chốt**: chọn không-lộ (khuyến nghị, vì UC-032 Exceptions không phải ràng buộc bảo mật) và ghi 1 dòng vào TDS §17.

**Expected Result (FAIL):** phiên cũ còn sống sau reset (kẻ chiếm phiên vẫn ở trong hệ thống dù chủ tài khoản đã đổi MK); token dùng lại được; token thô nằm trong DB/log.

**Current Status:** 🔴 Not written

---

#### MF01-TC-INT-009 — `POST /users`: server quyết định `status`, email trùng ⇒ 409, D1/D5 (UC-026)

**Severity:** `HIGH`
**Feature Under Test:** `POST /api/v1/users`
**Test File:** `web/UserCreateIT.java` · **Condition Ref:** `TC-COND-019`, `TC-COND-020`, `TC-COND-034` · **TDD Phase:** 🔴 RED
**Oracle Source:** **UC-026** Normal Flow bước 4–5 *"validates email uniqueness (MSG-006 if duplicate) and phone format (BR-012: valid +84 or 0xx format). System creates account, sends verification email (BR-010), logs creation in audit trail, and displays MSG-031"* · **BR-012**, **BR-010**, **BR-008**, **BR-013** · **MSG-006** / **MSG-031** · TDS §9.2 (`POST /api/v1/users` 201 + 409) + ghi chú review **B1** (`status` do server quyết định) và **B2** (trả **cả** `schoolId` và `school`) · **D5** (`body.id` → kiểm trùng → 409) · **D1**

**Test Steps:**
1. `FX-JWT-SYSADMIN` `POST /api/v1/users` body `{"name":"Trần Thị B","email":"b.tran@dtd.edu.vn","phone":"0912345678","role":"teacher","schoolId":"SCH-HN-0042"}`.
2. Gửi lại đúng email đó.
3. Gửi body kèm `"status":"ACTIVE"` (mock FE gửi `status` cứng).
4. Gửi body kèm `"id":"U-0004"` (đã tồn tại) — **D5**.
5. Gửi body kèm `"id":"U-7777"` (chưa tồn tại) — **D5**.
6. Gửi `phone` sai định dạng: `"12345"`, `"+1650123456"`.
7. Assert DB + email.

**Expected Result (PASS):**
- (1) HTTP **201** + MSG-031 nguyên văn; body có **cả** `schoolId: "SCH-HN-0042"` **và** `school: "TH Đoàn Thị Điểm"` (review B2); `status == "PENDING_VERIFICATION"`; `lastLogin == null`; `role == "teacher"` (kebab-case, D6); `id` là **mã nghiệp vụ** dạng `U-00xx` (D1) — **không** UUID.
- **DB:** `password_hash IS NULL` (invariant TDS §6.3: *"không set password khi tạo tài khoản — chỉ UC-154"*); 1 bản ghi `account_activation_tokens` (`expires_at = now+48h`, BR-011); 1 email tới `b.tran@dtd.edu.vn`; `audit_logs` +1.
- (2) HTTP **409** `IAM-006` + MSG-006 nguyên văn; **không** tạo bản ghi thứ 2 (UNIQUE `email` ở DB — TDS §5.2).
- (3) `status` vẫn `PENDING_VERIFICATION` ⇒ giá trị client gửi **bị bỏ qua** (review B1).
- (4) HTTP **409** (D5) — mã lỗi theo D5 là `<MODULE>-409`; ⚠️ TDS §10 **chưa có** dòng này ⇒ assert **status 409** + ghi nhận cần Nam bổ sung (nhóm §2 **L15**).
- (5) HTTP 201 với `id == "U-7777"` ⇒ `body.id` được dùng làm `code` (D5).
- (6) HTTP **422**; oracle nội dung = **MSG-138** *"Validation failed: one or more required fields are missing or invalid."* (⚠️ `code` — xem `MF01-TC-INT-016`).

**Expected Result (FAIL):** response chỉ trả `school` (tên) mà không có `schoolId` ⇒ FE phải gửi ngược lại **tên trường** làm khóa — chính lỗi mà `db.ts findIsolatedNameFields` cảnh báo; tài khoản mới `ACTIVE` ngay ⇒ bỏ qua BR-010/BR-011 và có thể login mà chưa từng đặt MK; `id` trả UUID ⇒ FE điều hướng sai sau `onSuccess`.

**Current Status:** 🔴 Not written

---

#### MF01-TC-INT-010 — `POST /staff`: chỉ School Administration, trong trường mình (UC-024, BR-013)

**Severity:** `CRITICAL` · **Nhóm "gọi API trực tiếp bỏ qua UI"**
**Feature Under Test:** `POST /api/v1/staff`
**Test File:** `web/StaffCreateIT.java` · **Condition Ref:** `TC-COND-019`, `TC-COND-030` · **TDD Phase:** 🔴 RED
**Oracle Source:** **BR-013** *"Chỉ **School Administration** mới có quyền tạo tài khoản nhân viên (staff) thuộc trường của mình. **System Admin** có quyền tạo tài khoản người dùng trên toàn hệ thống."* · **UC-024** Normal Flow bước 4 *"validates that the assigned role is a school-level staff role (Teacher, Medical Staff, Catering staff) and not an administrator or external role"* + bước 5 · **MSG-006**, **MSG-031**, **MSG-005** · TDS §16 (`POST /staff`: SYS ADMIN ❌, SCHOOL ADMIN ✅ School)

**Test Steps:**
1. `FX-JWT-SCHOOLADMIN` (`schoolId=SCH-HN-0042`) tạo staff role `teacher` → assert.
2. Cùng JWT, body kèm `"schoolId":"SCH-HN-0051"` (**trường khác**) → assert.
3. Cùng JWT, tạo staff role `system-admin` → assert.
4. Cùng JWT, tạo staff role `parent` / `supplier` → assert.
5. **`FX-JWT-SYSADMIN`** gọi `POST /staff` → assert (§16: ❌ với System Admin).
6. `FX-JWT-TEACHER` gọi `POST /staff` → assert.

**Expected Result (PASS):**
- (1) HTTP **201** + MSG-031; DB `school_id` = `SCH-HN-0042` **lấy từ JWT**, `status='PENDING_VERIFICATION'`, `email_verified=false`; 1 email mời.
- (2) **`schoolId` trong body bị BỎ QUA**, bản ghi vẫn thuộc `SCH-HN-0042` (TDS §17 C2: identity từ JWT). Nếu implement chọn cách **chặn**, assert **403** `IAM-004` + MSG-005 — **một trong hai**, không được im lặng tạo ở trường khác.
- (3)/(4) **403** `IAM-004` + MSG-005 ⇒ chỉ role cấp trường (`teacher`, `medical-staff`, `service-caterers`) được tạo qua endpoint này (UC-024 bước 4).
- (5) **403** — System Admin dùng `POST /users`, không dùng `POST /staff` (§16).
- (6) **403** `IAM-004`.

**Expected Result (FAIL):** (2) tạo được nhân viên ở trường khác ⇒ **vượt biên tenant**, School Admin trường A cấp tài khoản vào trường B; (3) tạo được `system-admin` ⇒ **leo quyền lên toàn hệ thống chỉ bằng một request** — đây chính là loại lỗ hổng "gate chỉ có ở dropdown UI".

**Current Status:** 🔴 Not written

---

#### MF01-TC-INT-011 — Hợp đồng phân trang `GET /users` (Team Plan §2b)

**Severity:** `HIGH`
**Feature Under Test:** `GET /api/v1/users` — `PagedResponse<T>` + `UserSpec`
**Test File:** `web/UserListIT.java` · **Condition Ref:** `TC-COND-023` · **TDD Phase:** 🔴 RED
**Oracle Source:** **UC-153** Normal Flow bước 3–4 *"displays the platform-wide user account list … can search, filter by role/school/status"* · Team Plan **§2b-1** (5 field bất biến), **§2b-2** (query param), **§2b-3** (thứ tự lọc), **§2b-6** (edge case), **§2b bảng SEARCH_FIELDS**: MF01 `users` ⇒ **`name`, `email`, `phone`** (*"❌ `username`, `fullName` không tồn tại"*) · TDS §4.4 (`pageSize=8` mặc định)

**Preconditions:** seed 20 user `U-0001`…`U-0020` + 2 fixture System Admin

**Test Steps (`@ParameterizedTest` cho các nhóm param):**

| # | Request | Expected |
|---|---|---|
| a | `GET /users` (không param) | `pageSize=8`, `page=1`, `items.length=8`, `total=22`, `pageCount=3` |
| b | `GET /users?page=2&pageSize=5` | `items.length=5`, `page=2`, `pageCount=5` |
| c | `GET /users?page=99` | **clamp**: `page == pageCount`, trả trang cuối, HTTP 200 |
| d | `GET /users?pageSize=0` | clamp `pageSize=1` |
| e | `GET /users?search=toan` | tìm trên `name`/`email`/`phone`, **case-insensitive**, khớp `U-0001` |
| f | `GET /users?search=0912345678` | khớp theo `phone` |
| g | `GET /users?search=ZZZ_KHONG_CO` | HTTP **200** `{items:[], total:0, page:1, pageSize:8, pageCount:1}` — **không 404** |
| h | `GET /users?status=ACTIVE` | exact match theo field |
| i | `GET /users?not:status=ACTIVE` | phủ định — trả `U-0007` (SUSPENDED) + `U-0016` (INACTIVE) |
| j | `GET /users?search=` (rỗng) | bỏ qua, như (a) |
| k | `GET /users?role=teacher` | filter theo `RoleKey` **kebab-case** (D6) |

**Expected Result (PASS):**
- Mọi response có **đúng 5 khóa** `items`, `total`, `page`, `pageSize`, `pageCount` — **không wrapper** `data`/`result` (§2b-1).
- `pageCount = max(1, ceil(total/pageSize))`; `page` đã clamp ≤ `pageCount`.
- `search` **chỉ** chạy trên `name`, `email`, `phone` — assert: user có `school = "TH Đoàn Thị Điểm"` **không** khớp `search=Điểm` (nếu khớp ⇒ đang OR toàn bộ cột, vi phạm §2b-3 bước 5).
- `items[].id` là **mã nghiệp vụ** `U-00xx` (D1); `items[].lastLogin` là **ISO-8601** (D4); `items[].role` **kebab-case** (D6); **không** field `passwordHash`/`googleSub`.
- (g) HTTP 200, **không** 404 (§2b-6).

**Expected Result (FAIL):** trả `{data: {...}}` ⇒ FE `usePagedList` vỡ toàn bộ màn danh sách; `pageCount=0` khi rỗng ⇒ FE chia cho 0; search OR mọi cột ⇒ full table scan (§2b cảnh báo) và khớp sai; `page` không clamp ⇒ trang trắng.

**Current Status:** 🔴 Not written

---

#### MF01-TC-INT-012 — `DELETE /roles/{code}`: 403 role hệ thống · 409 còn user (UC-029)

**Severity:** `HIGH` · **Nhóm "gọi API trực tiếp bỏ qua UI"**
**Feature Under Test:** `DELETE /api/v1/roles/{code}`
**Test File:** `web/RoleControllerIT.java` · **Condition Ref:** `TC-COND-011`, `TC-COND-012` · **TDD Phase:** 🔴 RED
**Oracle Source:** **BR-008** · **UC-029** Normal Flow bước 4 + **AF1** · **MSG-005** / **MSG-106** · TDS §9.2 (`DELETE /api/v1/roles/{id}` → 403 và 409) · TDS ADR-M04 (khớp guard `mock-server.ts` 359–376)

**Test Steps:**
1. `FX-JWT-SYSADMIN` `DELETE /api/v1/roles/ROLE-01` (system).
2. `DELETE /api/v1/roles/ROLE-09` (custom, 3 user gồm 1 INACTIVE).
3. `DELETE /api/v1/roles/ROLE-08` (custom, 0 user).
4. `DELETE /api/v1/roles/ROLE-08` lần 2.
5. `FX-JWT-SCHOOLADMIN` `DELETE /api/v1/roles/ROLE-09`.

**Expected Result (PASS):**
- (1) HTTP **403** `IAM-004` + **MSG-005 nguyên văn**; `SELECT count(*) FROM roles WHERE system=true` vẫn = **7** (TDS §14.1).
- (2) HTTP **409** `IAM-009` + **MSG-106** render `[Number] = 3`; bản ghi role còn nguyên.
- (3) HTTP **200**; DB xóa 1 bản ghi; `audit_logs` +1 dòng `DELETE`.
- (4) HTTP **404** + **MSG-137** *"Resource not found."* (⚠️ `code` — xem `MF01-TC-INT-015`).
- (5) HTTP **403** `IAM-004` (§16: thao tác role chỉ System Admin).
- Path param dùng **`code`** (`ROLE-01`), không UUID (D1).

**Expected Result (FAIL):** xóa được role hệ thống qua HTTP dù service có guard (⇒ guard bị bọc trong nhánh chỉ chạy khi có cờ UI); 409 không kèm số lượng ⇒ FE không hiện được "cần reassign bao nhiêu người".

**Current Status:** 🔴 Not written

---

#### MF01-TC-INT-013 — `GET /roles`: `users` là field **dẫn xuất** tính runtime (UC-130)

**Severity:** `MEDIUM`
**Feature Under Test:** `GET /api/v1/roles`
**Test File:** `web/RoleControllerIT.java` · **Condition Ref:** `TC-COND-024` · **TDD Phase:** 🔴 RED
**Oracle Source:** **UC-130** Normal Flow bước 3 *"displays the list of roles in a tabular view showing Role Name, Description, User Count, and Status"* · **BR-008** · TDS §9.2 `GET /api/v1/roles` (schema bổ sung 2026-07-29) + chú thích *"`users` là field dẫn xuất, tính runtime bằng `COUNT(user_accounts WHERE role_id = ...)` — **không lưu cột**"* · Team Plan §5 *"**không** lưu field dẫn xuất"*

**Test Steps:**
1. `FX-JWT-SYSADMIN` `GET /api/v1/roles` → ghi lại `users` của `ROLE-05` (Teacher).
2. Tạo 1 user mới role `teacher` (qua `POST /users`).
3. `GET /api/v1/roles` lại.
4. Deactivate user vừa tạo → `GET /api/v1/roles` lại.
5. Kiểm schema DB.

**Expected Result (PASS):**
- (1) response có **5 khóa phân trang** + `items[]` với `id`, `name`, `description`, `system`, `users`, `permissions[]` (đúng TDS §9.2).
- (3) `users` của `ROLE-05` **tăng đúng 1** mà không cần thao tác cập nhật nào ⇒ tính runtime.
- (4) `users` **không giảm** ⇒ đếm cả user `INACTIVE` (nhất quán với guard `MF01-TC-012`/`INT-012`).
- (5) **`information_schema.columns` KHÔNG có cột `users` trong bảng `roles`**:
```sql
SELECT count(*) FROM information_schema.columns
 WHERE table_name='roles' AND column_name IN ('users','user_count');   -- kỳ vọng 0
```
- `permissions` trả danh sách `PERM-*`; role `ROLE-01` có đúng 8 mã như seed.

**Expected Result (FAIL):** `users` lưu thành cột ⇒ lệch dần so với thực tế và guard xóa role tin vào số sai (xóa được role vẫn còn người dùng); hoặc `users` đếm chỉ `ACTIVE` ⇒ mâu thuẫn BR-008.

**Current Status:** 🔴 Not written

---

#### MF01-TC-INT-014 — Handler cascade `PartnerSuspended` ⇒ `SUSPENDED` + revoke phiên

**Severity:** `HIGH`
**Feature Under Test:** `AccountCascadeHandler` (TDS §7.2)
**Test File:** `web/AccountCascadeIT.java` · **Condition Ref:** `TC-COND-025` · **TDD Phase:** 🔴 RED
**Oracle Source:** TDS §7.2 (`PartnerSuspended` từ MF04 ⇒ *"`status=SUSPENDED` cho user có `partnerId`"*) · TDS §5.2 chú thích `partner_id` *"là cơ chế DUY NHẤT để cascade đình chỉ đối tác kéo theo khóa tài khoản (MF04 ADR-M04-6, U-0007/SUP-004)"* · TDS §6.3 (`ACTIVE → SUSPENDED`) · **BR-004** (revoke khi bị vô hiệu hóa)
**⚠️ Ghi chú phạm vi:** MF01 chỉ test **phía nhận** event. Bên phát (`MF04`) thuộc TestSpec-MF04. Event được đưa vào bằng `ApplicationEventPublisher` trong test, **không** phụ thuộc MF04 đã implement.

**Test Steps:**
1. `FX-U-SUPPLIER` (`U-0006`, `partnerId=SUP-001`) login → `R_S`; `FX-U-TEACHER` login → `R_T`.
2. Publish `PartnerSuspended(partnerId="SUP-001")`.
3. Assert DB + phiên.
4. Publish `SchoolDeactivated(schoolId="SCH-HN-0042")` (BR-021 cascade từ MF02).

**Expected Result (PASS):**
- (3) `U-0006` và `U-0020` (cùng `partnerId=SUP-001`) ⇒ `status='SUSPENDED'`; `refresh(R_S)` trả **401**; `U-0007` (`SUP-004`) **không** bị ảnh hưởng; `FX-U-TEACHER` (`partnerId=NULL`) **không** bị ảnh hưởng, `refresh(R_T)` vẫn 200.
- Trạng thái là **`SUSPENDED`**, **không** phải `INACTIVE` (ADR-M03: `SUSPENDED` = khóa tạm theo đình chỉ đối tác, gỡ đình chỉ thì mở lại).
- (4) tài khoản thuộc trường `SCH-HN-0042` bị **tạm dừng** (BR-021) — assert cùng khuôn.
- Handler **idempotent**: publish event 2 lần ⇒ không lỗi, không đổi thêm.

**Expected Result (FAIL):** dùng `INACTIVE` cho cascade ⇒ khi gỡ đình chỉ NCC không biết tài khoản nào nên mở lại (mất phân biệt mà ADR-M03 dựng ra); hoặc cascade quét theo **tên** đối tác thay vì `partner_id` ⇒ bỏ sót/khóa oan.

**Current Status:** 🔴 Not written

---

#### MF01-TC-INT-015 — Resource không tồn tại ⇒ 404 + MSG-137

**Severity:** `MEDIUM` · ✅ **Oracle chốt 2026-07-31 (TDS-MF01 §10 — `IAM-404`)** — §2 **L15**
**Feature Under Test:** `@RestControllerAdvice` (`common/`)
**Test File:** `web/ErrorEnvelopeIT.java` · **Condition Ref:** `TC-COND-026` · **TDD Phase:** 🔴 RED
**Oracle Source:** **MSG-137** (nguyên văn): *"Resource not found."* — `error_message.md` v1.5 ghi *"**Dùng chung mọi module** — tài nguyên không tồn tại hoặc nằm ngoài phạm vi dữ liệu được phép đọc"*, mã cục bộ mẫu `SCH-404`, `STU-404`… · **D2** envelope

**Test Steps (`@ParameterizedTest`):**

| Request (JWT System Admin) | Expected |
|---|---|
| `GET /api/v1/users/U-9999` | 404 + MSG-137 |
| `PATCH /api/v1/users/U-9999/status` | 404 + MSG-137 |
| `DELETE /api/v1/roles/ROLE-99` | 404 + MSG-137 |
| `GET /api/v1/users/<UUID hợp lệ>` | 404 + MSG-137 (D1: path param là `code`, UUID **không** tra được) |
| `GET /api/v1/staff/STF-001` với JWT School Admin **trường khác** | 404 + MSG-137 (*"nằm ngoài phạm vi dữ liệu được phép đọc"* — **không** 403, tránh lộ sự tồn tại) |

**Expected Result (PASS):** HTTP **404**; `error.code` = **`IAM-404`** (TDS §10, chốt 2026-07-31); `error.message` = **MSG-137 nguyên văn**; `error.details` là mảng (rỗng được).

**Expected Result (FAIL):** trả 500 (stack trace lộ ra ngoài); hoặc trả 403 cho cross-school ⇒ xác nhận sự tồn tại của bản ghi ở trường khác (rò rỉ thông tin).

**Current Status:** 🔴 Not written

---

#### MF01-TC-INT-016 — DTO sai / thiếu field ⇒ 422 + MSG-138

**Severity:** `MEDIUM` · ✅ **Oracle chốt 2026-07-31 (TDS-MF01 §10 — `IAM-422`)** — §2 **L15**
**Feature Under Test:** `@Valid` DTO + `@RestControllerAdvice`
**Test File:** `web/ErrorEnvelopeIT.java` · **Condition Ref:** `TC-COND-026` · **TDD Phase:** 🔴 RED
**Oracle Source:** **MSG-138** (nguyên văn): *"Validation failed: one or more required fields are missing or invalid."* — *"**Dùng chung mọi module** — DTO sai định dạng, sai giá trị enum, hoặc thiếu field bắt buộc"* · **BR-012** (email/SĐT hợp lệ) · **D2**, **D6**

**Test Steps (`@ParameterizedTest`):**

| Request body (POST /users, JWT System Admin) | Vi phạm | Expected |
|---|---|---|
| `{}` | thiếu toàn bộ | 422 + MSG-138 |
| `{"name":"A","email":"khong-phai-email","role":"teacher"}` | email sai định dạng (BR-012) | 422 |
| `{"name":"A","email":"a@b.vn","role":"teacher","phone":"12345"}` | SĐT sai (BR-012) | 422 |
| `{"name":"A","email":"a@b.vn","role":"Teacher"}` | enum sai — **PascalCase**, D6 đòi `teacher` | 422 |
| `{"name":"A","email":"a@b.vn","role":"khong-ton-tai"}` | `RoleKey` không thuộc 7 giá trị D6 | 422 |
| `{"name":"A","email":"a@b.vn","role":"teacher","lastLogin":"02/07/2026 07:45"}` | ngày `dd/MM/yyyy` — D4 đòi ISO | 422 |

**Expected Result (PASS):**
- HTTP **422**; `error.message` = **MSG-138 nguyên văn**; `error.details` là **mảng** liệt kê từng field lỗi (D2: *"details — mảng, có thể rỗng; không bao giờ null"*), mỗi phần tử có `field`.
- Dòng cuối chứng minh **D4**: chuỗi `dd/MM/yyyy` **bị từ chối**, không được parse "khoan dung" — nếu chấp nhận, seed sai định dạng sẽ lọt vào DB (đúng cảnh báo D4 về `paginate()` im lặng trả rỗng).
- Dòng enum chứng minh **D6**: chỉ nhận SCREAMING_SNAKE/kebab-case theo đúng bảng D6, không nhận nhãn hiển thị.
- `error.code` = **`IAM-422`** (TDS §10, chốt 2026-07-31).

**Expected Result (FAIL):** trả 400 với body mặc định của Spring (`timestamp`/`path`/`trace`) ⇒ vi phạm D2 và FE không đọc được; hoặc chấp nhận `"Teacher"`/`dd/MM/yyyy` ⇒ dữ liệu bẩn vào DB, phá D4/D6 ngay tại module nền.

**Current Status:** 🔴 Not written

---

### 4.3 SECURITY TEST CASES (RBAC + attack vector)

> Nguồn ma trận: **TDS §16 Authorization Matrix** (7 role BR-007). Thư mục: `src/test/java/com/sfms/iam/security/`
> Mọi TC nhóm này có thêm **OWASP** + **CWE**. `Expected PASS = hệ thống an toàn`.

---

#### MF01-TC-SEC-001 — Ma trận RBAC 7 role × 12 nhóm endpoint (parameterized, 84 tổ hợp)

**Severity:** `CRITICAL`
**OWASP:** `A01:2021 — Broken Access Control` · **CWE:** `CWE-862 — Missing Authorization`
**Feature Under Test:** `SecurityConfig` + `@PreAuthorize` trên toàn bộ controller MF01
**Test File:** `security/AuthorizationMatrixIT.java` · **Condition Ref:** `TC-COND-027` · **TDD Phase:** 🔴 RED
**Oracle Source:** **TDS §16** (bảng đầy đủ) · **BR-007** *"quyền truy cập được xác định hoàn toàn bởi vai trò … System Admin, School Administration, Medical Staff, Teacher, Parent, Raw Food Supplier, Service Caterers"* · **BR-013** · **BR-008** · **MSG-005** · **D6** (7 `RoleKey`) · TDS §13 TC-E2E-001 (mở rộng từ 1 kịch bản thành 84)

**Preconditions:** 7 JWT `FX-JWT-*` (mỗi role 1 chủ thể có thật trong DB); `FX-U-SYSADMIN` (fixture mới — §2 **L10**)

**Test Steps:** `@ParameterizedTest` + `@MethodSource("matrix")` — mỗi tổ hợp (role, endpoint) gọi 1 request và assert đúng ô kỳ vọng.

**Ma trận kỳ vọng (dịch từ TDS §16 sang 7 cột `RoleKey` của D6):**

| # | Endpoint | `system-admin` | `school-admin` | `medical-staff` | `teacher` | `service-caterers` | `supplier` | `parent` |
|---|---|---|---|---|---|---|---|---|
| E1 | `POST /auth/login` · `/refresh` · `/logout` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| E2 | `PATCH /account/password` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| E3 | `GET /profile/me` | ✅ Own | ✅ Own | ✅ Own | ✅ Own | ✅ Own | ✅ Own | ✅ Own |
| E4 | `PATCH /profile/me` | ✅ Own | ✅ Own | ✅ Own | ✅ Own | ✅ Own | ✅ Own | ✅ Own |
| E5 | `GET /users` | ✅ All | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 |
| E6 | `POST /users` | ✅ | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 |
| E7 | `PATCH /users/{code}/status` | ✅ (trừ primary admin) | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 |
| E8 | `GET /staff` | ✅ All | ✅ School | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 |
| E9 | `POST /staff` | ❌ 403 | ✅ School | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 |
| E10 | `PATCH /staff/{code}/status` | ❌ 403 | ✅ School | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 |
| E11 | `GET /roles` · `GET /permissions` | ✅ | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 |
| E12 | `POST`/`PATCH`/`DELETE /roles` | ✅ (BR-008) | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 |

**Expected Result (PASS — hệ thống an toàn):**
- **84/84** tổ hợp khớp bảng trên. Ô ❌ ⇒ HTTP **403** + `error.code == "IAM-004"` + **MSG-005 nguyên văn**.
- Ô ✅ ⇒ HTTP 2xx (không 403).
- E9/E10 với `system-admin` ⇒ **403** (đúng §16: System Admin không tạo staff; dùng `POST /users`).
- Phân quyền ở **tầng server** (`@PreAuthorize`), không dựa vào việc FE có hiện menu hay không.

**Expected Result (FAIL — lỗ hổng):** bất kỳ ô ❌ nào trả 2xx. Nguy hiểm nhất: `parent` hoặc `supplier` gọi được `GET /users` ⇒ **rò rỉ toàn bộ danh bạ PII của nền tảng** (email + SĐT ~5.000 tài khoản, PDPD); `teacher` gọi được `POST /users`/`DELETE /roles` ⇒ leo quyền.

**Current Status:** 🔴 Not written
**Implementation Note:** viết `matrix()` sinh 84 `Arguments` từ **một** bảng hằng — khi §16 đổi, sửa 1 chỗ. Không copy-paste 84 `@Test`.

---

#### MF01-TC-SEC-002 — Không có JWT ⇒ 401 trên mọi endpoint có bảo vệ

**Severity:** `CRITICAL` · **OWASP:** `A01:2021` · **CWE:** `CWE-306 — Missing Authentication for Critical Function`
**Feature Under Test:** `JwtAuthenticationFilter` + `SecurityConfig`
**Test File:** `security/JwtFilterIT.java` · **Condition Ref:** `TC-COND-028` · **TDD Phase:** 🔴 RED
**Oracle Source:** **MSG-004** *"Session expired. Please log in again."* · TDS §10 `IAM-005` (401) · TDS §15.2 *"Không JWT → 401 … `IAM-005`"* · TDS §9.1 cột Auth

**Test Steps:** gọi **không** header `Authorization`: `GET /users`, `POST /users`, `GET /staff`, `GET /roles`, `GET /permissions`, `PATCH /users/U-0004/status`, `GET /profile/me`, `PATCH /account/password`, `POST /auth/logout`. Sau đó gọi các endpoint **public**: `POST /auth/login`, `/forgot-password`, `/reset-password`, `/activate`, `/activate/resend`, `GET /oauth2/authorization/google`.

**Expected Result (PASS):**
- 9 endpoint bảo vệ ⇒ HTTP **401** + `IAM-005` + **MSG-004 nguyên văn**; body theo envelope D2.
- 6 endpoint public ⇒ **không** 401 vì thiếu JWT (đúng TDS §9.1 cột *Auth = Public*).
- Header `Authorization: Basic ...` hoặc `Bearer` rỗng ⇒ vẫn 401, không 500.

**Expected Result (FAIL):** endpoint bảo vệ trả 2xx khi không có token (⇒ security filter chain không áp cho path đó — lỗi cấu hình `permitAll()` quá rộng, rất dễ xảy ra khi thêm `/api/v1/**` vào whitelist); hoặc endpoint public bị 401 ⇒ không ai login được.

**Current Status:** 🔴 Not written

---

#### MF01-TC-SEC-003 — Access token hết hạn ⇒ 401 (không gia hạn ngầm ở access token)

**Severity:** `HIGH` · **OWASP:** `A07:2021 — Identification and Authentication Failures` · **CWE:** `CWE-613 — Insufficient Session Expiration`
**Feature Under Test:** `JwtAuthenticationFilter`
**Test File:** `security/JwtFilterIT.java` · **Condition Ref:** `TC-COND-028` · **TDD Phase:** 🔴 RED
**Oracle Source:** **BR-004** *"Access token: tự động hết hạn sau **30 phút**"* · **MSG-004** · TDS §10 `IAM-005` · TDS §17 C3 (TTL đọc từ `SecurityPolicy`)

**Test Steps (Boundary, dùng `FX-CLOCK`):**
1. Login tại `T0` → access token.
2. Gọi `GET /profile/me` tại `T0 + 29'59"`.
3. Gọi tại `T0 + 30'00"`.
4. Gọi tại `T0 + 30'01"` (`FX-JWT-EXPIRED`).
5. Đổi `sessionTimeoutMinutes` = 10 → login mới → gọi tại `T0 + 11'`.

**Expected Result (PASS):**
- (2) 200. (3) 200 hoặc 401 — **assert `exp - iat == 1800` giây** là điều kiện chính; hành vi tại đúng mốc theo `jjwt` mặc định (`exp` là *"không dùng được tại hoặc sau"*) ⇒ chấp nhận 401, ghi rõ.
- (4) **401** + `IAM-005` + MSG-004.
- (5) 401 ⇒ TTL đọc từ `SecurityPolicy`, không hardcode 30.
- Token hết hạn **không** được tự gia hạn khi gọi API — muốn gia hạn phải qua `POST /auth/refresh` (BR-004 v1.9).

**Expected Result (FAIL):** access token hết hạn vẫn dùng được ⇒ vô hiệu hóa toàn bộ ý nghĩa của lockout/deactivate; hoặc TTL hardcode ⇒ đổi policy trong UI System Admin không có tác dụng (màn `security-policies` thành trang trí).

**Current Status:** 🔴 Not written

---

#### MF01-TC-SEC-004 — JWT bị sửa payload / `alg:none` / chữ ký sai ⇒ 401

**Severity:** `CRITICAL` · **OWASP:** `A02:2021 — Cryptographic Failures` · **CWE:** `CWE-347 — Improper Verification of Cryptographic Signature`
**Feature Under Test:** `JwtAuthenticationFilter` — xác minh chữ ký
**Test File:** `security/JwtTamperIT.java` · **Condition Ref:** `TC-COND-028` · **TDD Phase:** 🔴 RED
**Oracle Source:** **BR-007** (quyền xác định bởi vai trò ⇒ vai trò phải không sửa được) · TDS §17 **C2** *"Identity (`sub`=userId, `role`, `schoolId`) PHẢI đọc từ JWT access token **do backend phát hành**"* · TDS §10 `IAM-005` · ADR-000 (JWT)

**Test Steps (Attack Simulation):**
1. Login bằng `FX-U-TEACHER` → access token hợp lệ.
2. **Tấn công A:** decode payload, đổi `role` → `system-admin`, re-encode, **giữ chữ ký cũ** (`FX-JWT-TAMPERED`) → `GET /users`.
3. **Tấn công B:** đổi `alg` header thành `none`, bỏ chữ ký → `GET /users`.
4. **Tấn công C:** ký lại bằng secret khác (`"attacker-secret"`) → `GET /users`.
5. **Tấn công D:** đổi `uid`/`schoolId` sang của School Admin trường khác → `GET /staff`.
6. **Tấn công E:** dùng **refresh token** làm access token (`Authorization: Bearer <R1>`) → `GET /users`.

**Expected Result (PASS — hệ thống an toàn):** cả 5 tấn công ⇒ HTTP **401** + `IAM-005` + MSG-004; **không** có request nào trả 2xx; **không** ghi audit `result='SUCCESS'`; log **không** in nội dung token.
**Expected Result (FAIL — lỗ hổng tồn tại):** (A) hoặc (C) qua ⇒ **bất kỳ giáo viên nào tự cấp cho mình quyền System Admin** ⇒ toàn quyền 13 module; (B) qua ⇒ lỗ hổng `alg:none` kinh điển; (E) qua ⇒ refresh token (sống 14 ngày) dùng thay access token, phá toàn bộ mô hình BR-004.

**Current Status:** 🔴 Not written

---

#### MF01-TC-SEC-005 — Identity lấy từ JWT, **không** tin body/query (leo quyền qua payload)

**Severity:** `CRITICAL` · **OWASP:** `A01:2021` · **CWE:** `CWE-639 — Authorization Bypass Through User-Controlled Key`
**Feature Under Test:** `AuthUser` argument resolver + mọi service nhận `Principal`
**Test File:** `security/IdentityFromJwtIT.java` · **Condition Ref:** `TC-COND-029` · **TDD Phase:** 🔴 RED
**Oracle Source:** TDS §17 **C2** *"KHÔNG tin `userId`/`role` từ body/query"* · TDS §17.4 **AP-AI-006** *"Trust FE identity — Đọc role/userId từ body thay vì JWT ⇒ Reject"* · **BR-007**

**Test Steps (Attack Simulation, JWT = `FX-JWT-TEACHER` của `U-0004`):**

| # | Request | Kỳ vọng |
|---|---|---|
| a | `PATCH /profile/me` body `{"name":"X","userId":"U-0001"}` | sửa hồ sơ **của `U-0004`**, `U-0001` **không đổi** |
| b | `PATCH /account/password` body `{"userId":"U-0001","currentPassword":"...","newPassword":"..."}` | đổi MK **của `U-0004`**; `U-0001.password_hash` **không đổi** |
| c | `POST /auth/logout` body `{"userId":"U-0001"}` | revoke refresh **của `U-0004`**; phiên `U-0001` còn sống |
| d | `GET /users?role=system-admin` | **403** (không phải "trả danh sách rỗng") |
| e | `POST /users` body `{... ,"role":"system-admin"}` | **403** (chặn ở tầng quyền, trước cả validate role) |
| f | `GET /profile/me?userId=U-0001` | trả hồ sơ **`U-0004`** |
| g | Header giả `X-User-Role: system-admin` + JWT teacher → `GET /users` | **403** |

**Expected Result (PASS):** đúng cột kỳ vọng; **không** bản ghi nào của `U-0001` bị thay đổi (assert `updated_at`/`password_hash` trước–sau); mọi ô 403 kèm `IAM-004` + MSG-005.
**Expected Result (FAIL):** (b) qua ⇒ **giáo viên đổi được mật khẩu của School Admin** = chiếm tài khoản quản trị; (a)/(f) qua ⇒ đọc/sửa hồ sơ người khác (PII); (g) qua ⇒ tin header do client đặt.

**Current Status:** 🔴 Not written

---

#### MF01-TC-SEC-006 — Cách ly `schoolId`: School Admin trường A không thấy/không sửa staff trường B

**Severity:** `CRITICAL` · **Nhóm "gọi API trực tiếp bỏ qua UI"**
**OWASP:** `A01:2021` · **CWE:** `CWE-566 — Authorization Bypass Through User-Controlled SQL Primary Key`
**Feature Under Test:** `StaffService.list()/create()/toggleStatus()` — lọc theo `schoolId` trong JWT
**Test File:** `security/TenantIsolationIT.java` · **Condition Ref:** `TC-COND-030` · **TDD Phase:** 🔴 RED
**Oracle Source:** **BR-013** *"Chỉ School Administration mới có quyền tạo tài khoản nhân viên (staff) **thuộc trường của mình**"* · **UC-144** Normal Flow bước 2 *"queries the database to retrieve staff accounts **belonging to the school**"* · TDS §16 (`GET /staff` = ✅ **School**) · **MSG-137** (*"nằm ngoài phạm vi dữ liệu được phép đọc"*)

**Preconditions:** `FX-SCHOOL-A` (`SCH-HN-0042`, có `STF-001`…`STF-011`), `FX-SCHOOL-B` (`SCH-HN-0051`, có `U-0018`, `U-0019`); JWT School Admin của **trường B** (`U-0018`)

**Test Steps (Attack Simulation):**
1. `GET /api/v1/staff` (JWT trường B).
2. `GET /api/v1/staff?schoolId=SCH-HN-0042` (JWT trường B) — cố ý ghi đè filter.
3. `GET /api/v1/staff/STF-001` (nhân viên trường A).
4. `PATCH /api/v1/staff/STF-001/status` body `{"active":false}`.
5. `POST /api/v1/staff` body kèm `"schoolId":"SCH-HN-0042"`.
6. `GET /api/v1/staff?search=Trang` (tên nhân viên trường A).

**Expected Result (PASS — hệ thống an toàn):**
- (1) chỉ trả nhân viên `SCH-HN-0051`; `total` **không** gồm 11 nhân viên trường A.
- (2) param `schoolId` **bị bỏ qua**, kết quả **giống hệt** (1) — `schoolId` là **CONTROL từ JWT**, không phải filter do client đặt.
- (3) **404** + MSG-137 (không 403 — không xác nhận sự tồn tại).
- (4) **404** hoặc **403**; DB: `STF-001.status` **không đổi**.
- (5) bản ghi tạo ra thuộc `SCH-HN-0051` (hoặc bị chặn 403) — **không bao giờ** thuộc `SCH-HN-0042`.
- (6) trả **rỗng**, không lộ tên nhân viên trường khác.

**Expected Result (FAIL — lỗ hổng):** (2) hoặc (6) trả dữ liệu trường A ⇒ **rò rỉ PII chéo giữa các trường** trên nền tảng multi-tenant; (4) đổi được trạng thái ⇒ School Admin trường B vô hiệu hóa nhân viên trường A. Đây là lỗi mà UI không bao giờ tạo ra nhưng `curl` thì có.

**Current Status:** 🔴 Not written

---

#### MF01-TC-SEC-007 — Cách ly `partnerId`: tài khoản NCC chỉ thấy dữ liệu của đối tác mình

**Severity:** `HIGH` · **OWASP:** `A01:2021` · **CWE:** `CWE-639`
**Feature Under Test:** claim `partnerId` trong JWT + filter ở `ProfileService`/`StaffService`
**Test File:** `security/TenantIsolationIT.java` · **Condition Ref:** `TC-COND-030` · **TDD Phase:** 🔴 RED
**Oracle Source:** TDS §9.2 chú thích claim JWT *"với tài khoản thuộc nhà cung cấp/đơn vị nấu ăn, đây là khóa cách ly dữ liệu đa-tenant — mọi truy vấn của MF04/MF06/MF08 lọc theo nó"* · TDS §5.2 `partner_id` (review B3) · **D1** lý do 3 *"Đổi sang UUID mà sót một chỗ là **rò dữ liệu chéo giữa các NCC**"*

**Test Steps:**
1. Login `FX-U-SUPPLIER` (`U-0006`, `SUP-001`) → decode JWT.
2. `GET /profile/me`.
3. `GET /users` / `GET /staff` với JWT này.
4. Login `U-0020` (cũng `SUP-001`) và `U-0007` (`SUP-004`, đang SUSPENDED).

**Expected Result (PASS):**
- (1) claim `partnerId == "SUP-001"` (**mã nghiệp vụ**, không UUID — D1) và `schoolId == null`.
- (2) chỉ trả hồ sơ của chính `U-0006`; **không** chứa `partnerId` của đối tác khác.
- (3) **403** `IAM-004` (§16: `supplier` không được xem `users`/`staff`).
- (4) `U-0020` login được với `partnerId="SUP-001"`; `U-0007` bị chặn `IAM-003` (SUSPENDED).
- MF01 **phát hành đúng** `partnerId` để 12 module downstream lọc được — TC này là **hợp đồng nền** cho MF04/MF06/MF08.

**Expected Result (FAIL):** claim `partnerId` thiếu hoặc là `null` cho tài khoản NCC ⇒ mọi module downstream **không có khóa cách ly** ⇒ NCC A đọc được dữ liệu NCC B (rủi ro số 3 mà D1 nêu tường minh); hoặc `partnerId` trả UUID ⇒ downstream so sánh `"SUP-001"` với UUID → luôn rỗng hoặc luôn qua.

**Current Status:** 🔴 Not written

---

#### MF01-TC-SEC-008 — Không được deactivate primary System Admin (UC-003 exception)

**Severity:** `CRITICAL` · **Nhóm "gọi API trực tiếp bỏ qua UI"** · **CWE:** `CWE-284 — Improper Access Control`
**Feature Under Test:** `UserService.toggleStatus()` → `PATCH /api/v1/users/{code}/status`
**Test File:** `security/PrimaryAdminGuardIT.java` · **Condition Ref:** `TC-COND-021` · **TDD Phase:** 🔴 RED
**Oracle Source:** **UC-003** Preconditions *"The target user account exists and is **not the default primary system admin account**"* + Normal Flow bước 4 *"System verifies the account is not the protected primary default system admin account"* + Exceptions *"If trying to deactivate the primary system admin, system blocks the action and displays a validation error"* · TDS §16 (`PATCH /users/{id}/status` = ✅ **trừ primary admin**) · TDS §6.3 invariant (3) · TDS §8.1 *"@throws IAM-004 nếu là primary System Admin"* · **MSG-005**

**Test Steps (Attack Simulation):**
1. `FX-JWT-SYSADMIN` (`U-9001`) `PATCH /api/v1/users/U-9000/status` `{"active":false}` — vô hiệu hóa primary admin.
2. `U-9000` tự vô hiệu hóa chính mình (JWT của `U-9000`).
3. `PATCH /api/v1/users/U-9001/status` `{"active":false}` bằng JWT của chính `U-9001` (tự khóa admin thường).
4. Sau (1)/(2): `U-9000` login lại.

**Expected Result (PASS):**
- (1) HTTP **403** + `IAM-004` + **MSG-005 nguyên văn**; DB `U-9000.status` vẫn `'ACTIVE'`; refresh token của `U-9000` **không** bị revoke.
- (2) **403** — guard theo **cờ primary**, không theo "ai gọi".
- (3) cho phép (không phải primary) — nhưng ⚠️ **thiếu oracle**: UC-003 không nói về tự-vô-hiệu-hóa. Assert hành vi hiện tại + ghi nhận cần Nam chốt (khuyến nghị chặn, để không tự khóa mất admin cuối cùng).
- (4) login **thành công** ⇒ hệ thống không bao giờ mất đường vào quản trị.

**Expected Result (FAIL):** (1) hoặc (2) thành công ⇒ **hệ thống bị khóa cứng vĩnh viễn**: không còn ai kích hoạt lại được tài khoản admin (không có endpoint nào cho phép), phải can thiệp trực tiếp DB. Đây là sự cố không thể rollback bằng API.

**Current Status:** 🔴 Not written

---

#### MF01-TC-SEC-009 — Refresh token replay sau khi đã rotate/revoke

**Severity:** `CRITICAL` · **OWASP:** `A07:2021` · **CWE:** `CWE-294 — Authentication Bypass by Capture-replay`
**Feature Under Test:** `POST /api/v1/auth/refresh` — kiểm `revoked_at`
**Test File:** `security/RefreshReplayIT.java` · **Condition Ref:** `TC-COND-031` · **TDD Phase:** 🔴 RED
**Oracle Source:** **BR-004** *"Refresh token bị **vô hiệu hóa ngay** khi: logout thủ công; tài khoản bị khóa do đăng nhập sai (BR-002) hoặc bị vô hiệu hóa (UC-002/UC-003); hoặc đăng nhập trên thiết bị khác (BR-005)"* · TDS ADR-M01 · TDS §8.2 `findByTokenHashAndRevokedAtIsNull` · TDS §10 `IAM-008`

**Test Steps (Attack Simulation — 5 đường vô hiệu hóa của BR-004):**

| # | Cách vô hiệu hóa | Sau đó gọi `refresh(R_cũ)` |
|---|---|---|
| a | rotate bình thường (`refresh` 1 lần) | 401 `IAM-008` |
| b | `POST /auth/logout` | 401 |
| c | login 5 lần sai ⇒ lockout (BR-002) | 401 |
| d | System Admin deactivate user (UC-003) | 401 |
| e | login thiết bị khác (BR-005) | 401 |
| f | đổi MK qua `PATCH /account/password` | 401 (UC-011: *"terminates all other active sessions"*) |
| g | reset MK qua `/auth/reset-password` | 401 (UC-032 bước 6) |

**Expected Result (PASS — hệ thống an toàn):**
- **7/7** đường ⇒ HTTP 401 `IAM-008` + MSG-004; **không** cấp access token mới.
- Truy vấn xác minh: `SELECT count(*) FROM refresh_tokens WHERE token_hash=:h AND revoked_at IS NULL` = **0** trong cả 7 trường hợp.
- (c) đặc biệt: **lockout phải revoke refresh** — nếu chỉ chặn `POST /auth/login` mà quên refresh, kẻ tấn công đã có refresh token vẫn tiếp tục cấp access token trong suốt 14 ngày, **hoàn toàn vô hiệu hóa BR-002**.

**Expected Result (FAIL — lỗ hổng):** bất kỳ đường nào còn cho refresh ⇒ token bị đánh cắp sống tới 14 ngày; đường (c)/(d) là nguy hiểm nhất vì người dùng/quản trị **tin rằng** tài khoản đã bị khóa.

**Current Status:** 🔴 Not written

---

#### MF01-TC-SEC-010 — Không lộ MK/token/googleSub trong response

**Severity:** `HIGH` · **OWASP:** `A02:2021` · **CWE:** `CWE-200 — Exposure of Sensitive Information`
**Feature Under Test:** toàn bộ DTO response MF01 (MapStruct mapper)
**Test File:** `security/SensitiveFieldLeakIT.java` · **Condition Ref:** `TC-COND-032` · **TDD Phase:** 🔴 RED
**Oracle Source:** **BR-001** (không lưu/không lộ plain text) · TDS §1 Data Classification `PII` · TDS §14.2 *"KHÔNG lộ MK/token/googleSub (PDPD)"* · TDS §12.1 *"Lộ MK/token/googleSub trong log ⇒ Tech Lead + DPO"* · **D1** (UUID không lộ ra API)

**Test Steps:** gọi **mọi** endpoint MF01 trả body, thu toàn bộ JSON, quét bằng một assertion chung:

```java
private static final List<String> FORBIDDEN_KEYS = List.of(
    "passwordHash", "password_hash", "password",
    "googleSub", "google_sub",
    "tokenHash", "token_hash",
    "clientSecret", "client-secret");

void assertNoSensitiveLeak(String json) {
    FORBIDDEN_KEYS.forEach(k -> assertThat(json).doesNotContain(k));
    // D1: không lộ UUID nội bộ ở bất kỳ field `id`/`*Id`
    assertThat(JsonPath.<List<String>>read(json, "$..id"))
        .noneMatch(v -> v != null && v.matches(UUID_REGEX));
}
```

Endpoint quét: `POST /auth/login`, `/refresh`, `GET /profile/me`, `PATCH /profile/me`, `GET /users`, `POST /users`, `GET /users/{code}`, `GET /staff`, `POST /staff`, `GET /roles`, `GET /permissions`, và **mọi error path** ở §4.2.

**Expected Result (PASS):** 0 lần khớp `FORBIDDEN_KEYS`; 0 field `id` dạng UUID; error body **không** chứa stack trace / tên class Java / câu SQL.
**Expected Result (FAIL):** `GET /users` trả `passwordHash` (điển hình khi map thẳng entity → JSON không qua DTO) ⇒ **rò rỉ toàn bộ hash mật khẩu**, cho phép tấn công offline; `googleSub` lộ ⇒ định danh Google của người dùng bị công khai (PII, PDPD); UUID lộ ⇒ vi phạm D1 và mở đường đoán khóa nội bộ.

**Current Status:** 🔴 Not written

---

#### MF01-TC-SEC-011 — Không lộ sự tồn tại tài khoản (user enumeration)

**Severity:** `HIGH` · **OWASP:** `A07:2021` · **CWE:** `CWE-204 — Observable Response Discrepancy`
**Feature Under Test:** `POST /auth/login`, `POST /auth/forgot-password`
**Test File:** `security/EnumerationIT.java` · **Condition Ref:** `TC-COND-032` · **TDD Phase:** 🔴 RED
**Oracle Source:** TDS §8.1 `/** UC-032 b1 … **Không lộ email tồn tại hay không**. */` · **MSG-002** (dùng chung cho *"Đăng nhập sai thông tin"* — không phân biệt sai email vs sai MK) · TDS §1 Data Classification `PII` / PDPD

**Test Steps (Attack Simulation):**
1. `POST /auth/login` với email **có thật** + MK sai → ghi status, body, mã lỗi.
2. `POST /auth/login` với email **không tồn tại** + MK bất kỳ → so sánh.
3. `POST /auth/forgot-password` với email có thật vs không tồn tại → so sánh.
4. Lặp 20 lần mỗi nhánh, so sánh **thời gian phản hồi** (trung vị).
5. `POST /auth/login` với email của tài khoản **đang bị lockout** vs email không tồn tại.

**Expected Result (PASS):**
- (1) vs (2): **cùng** HTTP 401, **cùng** `error.code = "IAM-002"`, **cùng** `error.message` (MSG-002). Chỉ `details.attemptsLeft` có thể khác — ⚠️ **đây chính là kênh lộ thông tin**: nếu email không tồn tại mà vẫn trả `attemptsLeft` thì phải trả **cùng dãy giá trị giả** như email thật; nếu **không** trả `details` cho email lạ ⇒ phân biệt được. **Cần Nam chốt cùng lúc với §2 L3** (khuyến nghị: luôn trả `details` với cùng khuôn).
- (3) hai nhánh **giống hệt** nhau (đã assert ở `MF01-TC-022`).
- (4) chênh lệch trung vị **< 50ms** ⇒ email không tồn tại **vẫn phải chạy BCrypt** trên một hash giả (nếu bỏ qua, phản hồi nhanh hơn rõ rệt ⇒ timing attack).
- (5) lockout **không** tiết lộ khác biệt vượt mức cần thiết — ⚠️ ghi nhận: MSG-076 (423) bản chất **đã** xác nhận tài khoản tồn tại. Đây là **đánh đổi có chủ ý** của BR-002 (phải cho người dùng biết vì sao bị chặn). TC ghi nhận, **không** đòi sửa BR.

**Expected Result (FAIL):** email lạ trả 404 hoặc message riêng ⇒ dò được danh bạ; thời gian phản hồi chênh > 200ms ⇒ dò bằng timing dù message giống nhau.

**Current Status:** 🔴 Not written

---

#### MF01-TC-SEC-012 — Không ghi MK/token/secret vào log

**Severity:** `HIGH` · **CWE:** `CWE-532 — Insertion of Sensitive Information into Log File`
**Feature Under Test:** logging toàn module + `@RestControllerAdvice`
**Test File:** `security/LogRedactionIT.java` · **Condition Ref:** `TC-COND-032` · **TDD Phase:** 🔴 RED
**Oracle Source:** TDS §14.2 (lệnh grep log, *"kỳ vọng: No output"*) · TDS §12.1 (trigger rollback: *"Lộ MK/token/googleSub trong log — bất kỳ"*) · **BR-164** / ADR-000 (secret trong ENV, không hardcode) · PDPD

**Test Steps:** dùng `OutputCaptureExtension` (Spring Boot Test) hoặc appender in-memory (Logback `ListAppender`):
1. Login thành công + login sai + refresh + reset MK + activate + Google login.
2. Gây 1 exception có chủ ý (DTO sai) để sinh log ERROR.
3. Quét toàn bộ log đã bắt.

**Expected Result (PASS):**
- Log **không** chứa: `"P@ssw0rd123"`, chuỗi refresh token thô, `token_hash`, `sub-1`/`googleSub`, `GOOGLE_CLIENT_SECRET`, `test-secret-32chars`.
- Log DEBUG của request body (nếu bật) đã **redact** field `password`/`newPassword`/`refreshToken` → `"***"`.
- Stack trace **không** đi ra response body (chỉ trong log server).
- Audit log ghi `actor` + `ip` + `action` nhưng **không** ghi giá trị credential.

**Expected Result (FAIL):** MK plain text trong log ⇒ ai đọc được log (DevOps, agent thu log, backup) đọc được mật khẩu người dùng — vi phạm BR-001 về mặt tinh thần dù DB đã hash, và là trigger rollback theo TDS §12.1.

**Current Status:** 🔴 Not written

---

#### MF01-TC-SEC-013 — Google OAuth2: callback lỗi ⇒ MSG-103; không auto-provision khi gọi trực tiếp

**Severity:** `CRITICAL` · **Nhóm "gọi API trực tiếp bỏ qua UI"** · **OWASP:** `A01:2021`
**Feature Under Test:** `GET /oauth2/authorization/google` + `GET /login/oauth2/code/google` + `OAuth2LoginSuccessHandler`
**Test File:** `security/GoogleOAuth2IT.java` · **Condition Ref:** `TC-COND-013` · **TDD Phase:** 🔴 RED
**Oracle Source:** **BR-218** · **UC-191** Exceptions *"OAuth2 token exchange fails: system displays MSG-103"* · **MSG-102** / **MSG-103** nguyên văn · TDS §9.2 (403 `IAM-102` / 401 `IAM-103`) · TDS §6.2 · TDS §17 C5 + **AP-AI-008** · TDS §13 TC-E2E-003 (mở rộng)

**Preconditions:** `FX-WIREMOCK-GOOGLE`, `FX-ENV-OAUTH`, `FX-U-SCHOOLADMIN` (`googleSub=NULL`), `FX-U-INACTIVE`

**Test Steps (Attack Simulation):**
1. WireMock trả `id_token` với `email=toanpq@dtd.edu.vn`, `email_verified=true` → callback.
2. WireMock trả `email=new.person@gmail.com` (không có trong DB) → callback.
3. WireMock trả HTTP **500** ở `/token` (token exchange fail) → callback.
4. WireMock trả `email=phongvd.parent@gmail.com` (`U-0016`, **INACTIVE**) → callback.
5. WireMock trả `email_verified=false` → callback.
6. Callback với `state` sai/thiếu (CSRF) → assert.
7. Callback với `code` đã dùng lại → assert.

**Expected Result (PASS — hệ thống an toàn):**
- (1) HTTP 200 + token; `googleSub` được set; audit `LOGIN SUCCESS`.
- (2) **403** `IAM-102` + **MSG-102 nguyên văn**; `count(user_accounts)` **không đổi** (BR-218 — kiểm ở tầng HTTP, không chỉ tầng service như `MF01-TC-014`).
- (3) **401** `IAM-103` + **MSG-103 nguyên văn**; không tạo user, không cấp token.
- (4) **403** `IAM-003` + MSG-003 (BR-010 vẫn áp — giao với `MF01-TC-017`).
- (5) **chặn** — email chưa được Google xác minh **không** thỏa BR-218 (*"Email Google được coi là đã xác minh"* chỉ đúng khi `email_verified=true`). ⚠️ BR-218 không nói rõ nhánh này ⇒ assert chặn + ghi nhận cần Nam chốt 1 dòng.
- (6) **chặn** (Spring Security OAuth2 Client tự kiểm `state` — TC xác nhận **không** bị tắt đi).
- (7) **chặn** — `code` dùng 1 lần.
- Endpoint đúng chuẩn Spring: `/oauth2/authorization/google` + `/login/oauth2/code/google` (**không** tự viết flow tay — TDS §17 C5, AP-AI-007).

**Expected Result (FAIL — lỗ hổng):** (2) tạo tài khoản mới ⇒ **ai có Gmail cũng vào được hệ thống ATTP trường học** (AP-AI-008); (5) chấp nhận email chưa xác minh ⇒ kẻ tấn công tạo tài khoản Google với email của Hiệu trưởng → **chiếm tài khoản School Admin không cần mật khẩu**; (6) tắt kiểm `state` ⇒ CSRF login.

**Current Status:** 🔴 Not written

---

#### MF01-TC-SEC-014 — Injection ở input xác thực và `search`

**Severity:** `HIGH` · **OWASP:** `A03:2021 — Injection` · **CWE:** `CWE-89 — SQL Injection`
**Feature Under Test:** `AuthService.login()`, `UserSpec.build()` (Specification), `PasswordResetService.request()`
**Test File:** `security/InjectionIT.java` · **Condition Ref:** `TC-COND-033` · **TDD Phase:** 🔴 RED
**Oracle Source:** TDS §13 TC-E2E-003 *"SQL injection ở email login … IAM-002, không lộ dữ liệu (JPA parameterized)"* (mở rộng) · Team Plan §2b-4 (`Specification` + `cb.like`, tham số hóa) · **MSG-002** / **MSG-138**

**Test Steps (Attack Simulation):**

| # | Input | Điểm vào | Kỳ vọng |
|---|---|---|---|
| a | `' OR 1=1 --` | `email` (login) | 401 `IAM-002` (hoặc 422 `MSG-138`), **không** đăng nhập được |
| b | `admin@x.vn'; DROP TABLE user_accounts; --` | `email` (login) | 401/422; bảng `user_accounts` **vẫn tồn tại** |
| c | `' UNION SELECT password_hash FROM user_accounts --` | `search` (GET /users) | 200 với `items` **rỗng hoặc khớp literal**; không cột lạ trong response |
| d | `%` và `_` | `search` | coi là **ký tự thường**, không phải wildcard SQL ⇒ không trả toàn bộ bảng |
| e | `name` = `<script>alert(1)</script>` | `POST /users` | lưu/trả **escaped**, không thực thi; không 500 |
| f | `role` = `teacher'--` | `POST /users` | 422 `MSG-138` (enum không khớp D6) |
| g | payload 10.000 ký tự | `email` | 422, không 500, không treo (DoS nhẹ) |
| h | `%00` (null byte) chèn trong `email` | login | 422/401, không 500 |

**Expected Result (PASS — hệ thống an toàn):**
- Không request nào trả **500**; không request nào trả dữ liệu ngoài phạm vi.
- Sau toàn bộ chuỗi tấn công: `SELECT count(*) FROM user_accounts` **không đổi**; các bảng còn nguyên (assert qua `information_schema.tables`).
- (d) chứng minh `search` **escape** `%`/`_` trước khi đưa vào `LIKE` — điểm rất dễ bỏ sót khi dùng `cb.like(cb.lower(...), "%" + q + "%")`.
- Truy vấn sinh ra là **parameterized** (assert qua log SQL: chứa `?`, không nội suy giá trị).

**Expected Result (FAIL — lỗ hổng):** bất kỳ 500 kèm thông điệp PostgreSQL ⇒ lộ schema; (c) trả thêm cột ⇒ SQLi thực sự; (d) `search=%` trả toàn bộ 5.000 user ⇒ **rò rỉ toàn bộ danh bạ PII chỉ bằng 1 ký tự**.

**Current Status:** 🔴 Not written

---

## 5. Red-Green-Refactor Tracker

> Cột 🔴 tick **sau khi** chạy với stub và xác nhận FAIL (§5.1). Cột 🟢 điền **commit hash** khi test xanh.

| TC ID | Layer | Test File | 🔴 RED confirmed | 🟢 GREEN (commit) | 🔵 REFACTOR note |
|---|---|---|---|---|---|
| `MF01-TC-001` | Unit | `service/AccountServiceTest.java` | `[ ]` | `______` | tách `PasswordEncoder` thành `@Bean` |
| `MF01-TC-002` | Unit | `service/PasswordPolicyValidatorTest.java` | `[ ]` | `______` | policy → record `PasswordPolicy` |
| `MF01-TC-003` | Unit | `service/AccountServiceTest.java` | `[ ]` | `______` | — |
| `MF01-TC-004` | Unit | `service/AuthServiceLockoutTest.java` | `[ ]` | `______` | tách `LockoutPolicy` |
| `MF01-TC-005` | Unit | `service/AuthServiceLockoutTest.java` | `[ ]` | `______` | inject `Clock` (bắt buộc) |
| `MF01-TC-006` | Unit | `service/AuthServiceLoginGuardTest.java` | `[ ]` | `______` | gom guard thành chuỗi `LoginGuard` |
| `MF01-TC-007` | Unit | `service/AuthServiceLoginGuardTest.java` | `[ ]` | `______` | — |
| `MF01-TC-008` | Unit | `service/AuthServiceTest.java` | `[ ]` | `______` | — |
| `MF01-TC-009` | Unit | `service/RefreshTokenServiceTest.java` | `[ ]` | `______` | hằng `REFRESH_TTL = Duration.ofDays(14)` |
| `MF01-TC-010` | Unit | `service/AuthServiceSessionTest.java` | `[ ]` | `______` | — |
| `MF01-TC-011` | Unit | `service/RoleServiceGuardTest.java` | `[ ]` | `______` | — |
| `MF01-TC-012` | Unit | `service/RoleServiceGuardTest.java` | `[ ]` | `______` | — |
| `MF01-TC-013` | Unit | `service/RoleServiceGuardTest.java` | `[ ]` | `______` | ⚠️ chờ mã lỗi "tên role trùng" |
| `MF01-TC-014` | Unit | `service/OAuth2LoginServiceTest.java` | `[ ]` | `______` | — |
| `MF01-TC-015` | Unit | `service/OAuth2LoginServiceTest.java` | `[ ]` | `______` | — |
| `MF01-TC-016` | Unit | `service/OAuth2LoginServiceTest.java` | `[ ]` | `______` | ⚠️ chờ chốt nhánh `sub` khác |
| `MF01-TC-017` | Unit | `service/OAuth2LoginServiceTest.java` | `[ ]` | `______` | — |
| `MF01-TC-018` | Unit | `service/AccountActivationServiceTest.java` | `[ ]` | `______` | — |
| `MF01-TC-019` | Unit | `service/AccountActivationServiceTest.java` | `[ ]` | `______` | gom `OneTimeToken` dùng chung reset/activation |
| `MF01-TC-020` | Unit | `service/AccountActivationServiceTest.java` | `[ ]` | `______` | — |
| `MF01-TC-021` | Unit | `service/PasswordResetServiceTest.java` | `[ ]` | `______` | — |
| `MF01-TC-022` | Unit | `service/PasswordResetServiceTest.java` | `[ ]` | `______` | — |
| `MF01-TC-023` | Unit | `service/ProfileServiceTest.java` | `[ ]` | `______` | ⚠️ CF-4 treo (5MB ↔ 10MB) |
| `MF01-TC-024` | Unit | `service/ProfileServiceTest.java` | `[ ]` | `______` | — |
| `MF01-TC-INT-001` | Int | `web/AuthControllerIT.java` | `[ ]` | `______` | — |
| `MF01-TC-INT-002` | Int | `web/AuthControllerErrorIT.java` | `[ ]` | `______` | ✅ Đã mở khóa — §2 L3 chốt 2026-07-31 |
| `MF01-TC-INT-003` | Int | `web/AuthRefreshIT.java` | `[ ]` | `______` | — |
| `MF01-TC-INT-004` | Int | `web/AuthSessionIT.java` | `[ ]` | `______` | — |
| `MF01-TC-INT-005` | Int | `web/AuthControllerIT.java` | `[ ]` | `______` | — |
| `MF01-TC-INT-006` | Int | `web/UserStatusIT.java` | `[ ]` | `______` | — |
| `MF01-TC-INT-007` | Int | `web/AccountActivationIT.java` | `[ ]` | `______` | ⚠️ thiếu success MSG (L9) |
| `MF01-TC-INT-008` | Int | `web/PasswordResetIT.java` | `[ ]` | `______` | ⚠️ xung đột MSG-003 ↔ không-lộ-email |
| `MF01-TC-INT-009` | Int | `web/UserCreateIT.java` | `[ ]` | `______` | — |
| `MF01-TC-INT-010` | Int | `web/StaffCreateIT.java` | `[ ]` | `______` | — |
| `MF01-TC-INT-011` | Int | `web/UserListIT.java` | `[ ]` | `______` | `PagedResponse` + `UserSpec` → `common/` (Nam dựng 1 lần) |
| `MF01-TC-INT-012` | Int | `web/RoleControllerIT.java` | `[ ]` | `______` | — |
| `MF01-TC-INT-013` | Int | `web/RoleControllerIT.java` | `[ ]` | `______` | — |
| `MF01-TC-INT-014` | Int | `web/AccountCascadeIT.java` | `[ ]` | `______` | — |
| `MF01-TC-INT-015` | Int | `web/ErrorEnvelopeIT.java` | `[ ]` | `______` | ✅ Đã mở khóa — §2 L15 chốt 2026-07-31 |
| `MF01-TC-INT-016` | Int | `web/ErrorEnvelopeIT.java` | `[ ]` | `______` | ✅ Đã mở khóa — §2 L15 chốt 2026-07-31 |
| `MF01-TC-SEC-001` | Sec | `security/AuthorizationMatrixIT.java` | `[ ]` | `______` | bảng §16 → 1 hằng duy nhất |
| `MF01-TC-SEC-002` | Sec | `security/JwtFilterIT.java` | `[ ]` | `______` | — |
| `MF01-TC-SEC-003` | Sec | `security/JwtFilterIT.java` | `[ ]` | `______` | — |
| `MF01-TC-SEC-004` | Sec | `security/JwtTamperIT.java` | `[ ]` | `______` | — |
| `MF01-TC-SEC-005` | Sec | `security/IdentityFromJwtIT.java` | `[ ]` | `______` | — |
| `MF01-TC-SEC-006` | Sec | `security/TenantIsolationIT.java` | `[ ]` | `______` | — |
| `MF01-TC-SEC-007` | Sec | `security/TenantIsolationIT.java` | `[ ]` | `______` | — |
| `MF01-TC-SEC-008` | Sec | `security/PrimaryAdminGuardIT.java` | `[ ]` | `______` | ⚠️ chờ chốt tự-vô-hiệu-hóa |
| `MF01-TC-SEC-009` | Sec | `security/RefreshReplayIT.java` | `[ ]` | `______` | — |
| `MF01-TC-SEC-010` | Sec | `security/SensitiveFieldLeakIT.java` | `[ ]` | `______` | assertion dùng chung cho 12 module |
| `MF01-TC-SEC-011` | Sec | `security/EnumerationIT.java` | `[ ]` | `______` | ⚠️ liên quan L3 |
| `MF01-TC-SEC-012` | Sec | `security/LogRedactionIT.java` | `[ ]` | `______` | — |
| `MF01-TC-SEC-013` | Sec | `security/GoogleOAuth2IT.java` | `[ ]` | `______` | ⚠️ chờ chốt `email_verified=false` |
| `MF01-TC-SEC-014` | Sec | `security/InjectionIT.java` | `[ ]` | `______` | — |

### 5.1 Red Gate Protocol (CASE 2.0 — GATE-2)

> ⭐ **Gate quan trọng nhất.** Trước khi implement, chạy toàn bộ suite với stub **throw**. **Mọi** test PHẢI FAIL.
> Test nào PASS ngay ⇒ **AP-AI-002 (Green-from-Birth)** ⇒ reject và viết lại TC đó.

**Stub cho Red Phase (Java — thay khối TypeScript của template):**

```java
// src/main/java/com/sfms/iam/service/AuthService.java — RED PHASE STUB
@Service
public class AuthService implements IAuthService {
    @Override public LoginResult login(LoginCommand cmd) {
        throw new UnsupportedOperationException("Not implemented — Red Phase stub");
    }
    @Override public void logout(UUID userId) {
        throw new UnsupportedOperationException("Not implemented — Red Phase stub");
    }
    @Override public TokenPair refresh(String refreshToken) {
        throw new UnsupportedOperationException("Not implemented — Red Phase stub");
    }
}
// Áp cùng khuôn cho: OAuth2LoginService, AccountService, PasswordResetService,
// AccountActivationService, UserService, StaffService, RoleService, PermissionService,
// ProfileService, RefreshTokenService  (đúng 11 service của §8.1 TDS)
```

**Red Gate Verification (điền khi chạy):**

| Nhóm TC | Stub Result | Expected | Actual | Root Cause (nếu PASS bất thường) |
|---|---|---|---|---|
| `MF01-TC-001` … `-024` (Unit, 24) | `throw UnsupportedOperationException` | 🔴 **24/24 FAIL** | ☐ FAIL ☐ PASS: `___` | ☐ Tautology ☐ Shared state ☐ Hallucinated contract |
| `MF01-TC-INT-001` … `-016` (Int, 16) | stub + Testcontainers up | 🔴 **16/16 FAIL** | ☐ FAIL ☐ PASS: `___` | ☐ Assert quá lỏng (chỉ `status().is4xxServerError()`) |
| `MF01-TC-SEC-001` … `-014` (Sec, 14) | stub + `SecurityConfig` **chưa** có `@PreAuthorize` | 🔴 **14/14 FAIL** | ☐ FAIL ☐ PASS: `___` | ☐ **Cạm bẫy riêng nhóm Security** — xem cảnh báo dưới |

> ### ⚠️ Cạm bẫy Red Gate của nhóm Security — đọc trước khi tick
>
> TC nhóm ❌ (kỳ vọng **403**) rất dễ **PASS giả**: khi service còn là stub `throw`, `@RestControllerAdvice` có thể trả **500**; nếu assert viết lỏng (`is4xxClientError()` hay `isNotEqualTo(200)`) thì test **xanh mà chưa có bất kỳ guard nào**. Đây đúng là **AP-AI-002**.
>
> **Bắt buộc:** mọi TC security assert **chính xác** `status().isForbidden()` **và** `jsonPath("$.error.code").value("IAM-004")` **và** `jsonPath("$.error.message").value(MSG_005)`. Với 3 assert này, stub trả 500 ⇒ FAIL đúng như mong đợi.
>
> Với `MF01-TC-SEC-001`: ở Red Phase, ô ✅ FAIL (stub throw) **và** ô ❌ cũng FAIL (500 ≠ 403) ⇒ **84/84 FAIL**. Nếu thấy ô ❌ nào PASS ⇒ dừng lại, sửa assert.

**Red Gate Evidence:**

- Stub commit hash: `______`
- Lệnh chạy: `./mvnw -Dtest='MF01*' test` (unit) · `./mvnw -Dtest='*IT' verify -Pintegration` (int+sec)
- Tất cả **54/54** FAIL? ☐ Yes → **GATE-2 PASS** (T2→T3) → được phép implement
- Log file: `target/red-gate-evidence.log` (đính kèm khi xin approve)

> **Nếu bất kỳ test PASS:** dừng. Xác định root cause ở bảng trên. Viết lại TC từ spec §4 với Props Isolation Pattern (§3 TDS-05) và assert chính xác.

---

## 6. Entry / Exit Criteria

### Entry Criteria (điều kiện bắt đầu viết test)

- [x] `TDS-MF01_Backend.md` **Approved** (Nam final approve 2026-07-31 theo Team Plan §4 BƯỚC 3).
- [x] **2 mục BLOCKING của §2 đã đóng (2026-07-31):** L3 (shape `details` cho `attemptsLeft` — TDS §9.2) và L15 (2 dòng MSG-137/MSG-138 trong TDS §10). 3 TC (`INT-002`, `INT-015`, `INT-016`) đã có oracle đầy đủ để assert.
- [ ] Logic Issues §2 đã được **Nam + Huy** confirm (đặc biệt L1, L2, L4 — 3 mục làm expected value **sai** nếu bám TDS nguyên trạng).
- [ ] Flyway `V1__init_schema.sql` (Nam giữ) + `V2__seed_roles_permissions_policy.sql` đã có bảng MF01 theo TDS §5.2, **kèm cột `code` + `UNIQUE(school_id, code)`** (D1 checklist).
- [ ] Seed test (`db/testseed`) có đủ fixture §3 TDS-05, **gồm 3 fixture mới**: `FX-U-SYSADMIN`, `FX-U-SYSADMIN-PRIMARY`, `FX-ROLE-CUSTOM-USED` (§2 L10).
- [ ] `Clock` là `@Bean` inject được (nếu không, 8 TC biên thời gian phải dùng `Thread.sleep` — không chấp nhận).
- [ ] Hạ tầng test sẵn sàng: Docker (Testcontainers), WireMock, GreenMail trong `pom.xml` scope `test`.

### Exit Criteria (DoD — điều kiện kết thúc)

- [ ] `./mvnw verify` **xanh toàn bộ**, không `@Disabled`, không `assumeTrue` bỏ qua test.
- [ ] **54/54** TC ở trạng thái 🟢; bảng §5 điền đủ commit hash.
- [ ] `MF01-TC-SEC-001`: **84/84** tổ hợp khớp §16 (không tổ hợp nào bị skip).
- [ ] **JaCoCo ≥ 80% line coverage** cho `com.sfms.iam.service` và `com.sfms.iam.web`; **100%** cho `AuthService`, `OAuth2LoginService`, `RoleService` (3 lớp chứa guard bảo mật).
- [ ] 5 truy vấn bất biến của **TDS §14.1** chạy sau suite đều trả kết quả kỳ vọng:
```sql
-- 1. BR-001: không plain text
SELECT count(*) FROM user_accounts WHERE password_hash IS NOT NULL AND password_hash NOT LIKE '$2%';   -- 0
-- 2. BR-012: email duy nhất
SELECT count(*) FROM (SELECT email FROM user_accounts GROUP BY email HAVING count(*)>1) x;            -- 0
-- 3. BR-218: googleSub duy nhất
SELECT count(*) FROM (SELECT google_sub FROM user_accounts WHERE google_sub IS NOT NULL
                      GROUP BY google_sub HAVING count(*)>1) x;                                        -- 0
-- 4. BR-008: đúng 7 role hệ thống
SELECT count(*) FROM roles WHERE system = TRUE;                                                        -- 7
-- 5. BR-004/BR-005: ≤ 1 refresh token còn hiệu lực mỗi user
SELECT count(*) FROM (SELECT user_id FROM refresh_tokens WHERE revoked_at IS NULL AND expires_at > now()
                      GROUP BY user_id HAVING count(*)>1) x;                                           -- 0
```
- [ ] **Không** `passwordHash`/`googleSub`/token thô xuất hiện trong response hoặc log (`MF01-TC-SEC-010`, `-012` xanh).
- [ ] Swagger (`/v3/api-docs`) liệt kê đúng 18 dòng endpoint của TDS §9.1 — Minh xác nhận đổi `baseURL` FE sang `/api/v1` là các màn `system-admin/users|roles|permissions` + `school-admin/staff` chạy được.

**Exit Criteria bổ sung — CASE 2.0:**

- [ ] **Red Gate (§5.1)** — 54/54 FAIL với stub, có log đính kèm.
- [ ] **Contract Existence** — mọi import trong test resolve:
```bash
./mvnw -q test-compile   # kỳ vọng: BUILD SUCCESS, không "cannot find symbol"
```
- [ ] **Props Isolation** — không shared mutable state:
```bash
# Mọi entity phải tạo qua factory trong @Test, không phải field static/instance
grep -rnE '^\s*(private|protected)?\s*(static)?\s*(final)?\s*(UserAccount|RoleRecord|RefreshToken)\s+\w+\s*=' src/test/java/com/sfms/iam/
# kỳ vọng: chỉ khớp trong *Fixture.java, KHÔNG khớp trong *Test.java / *IT.java
```
- [ ] **Oracle Source** — mọi TC ở §4 có dòng `Oracle Source` trỏ BR/UC/MSG/D-cụ-thể; các TC mang nhãn ⚠️ đã được Nam/Huy trả lời (không còn `BLOCKED-ORACLE`).
- [ ] **D1–D7 gate** (checklist `00_Common_Conventions.md`) đã được test **chứng minh**, không chỉ khai trong tài liệu:

| Quy ước | TC chứng minh |
|---|---|
| D1 `id` = mã nghiệp vụ, UUID không lộ | `INT-001`, `INT-009`, `INT-011`, `INT-012`, `SEC-010` |
| D2 envelope `{code,message,details}` | `INT-002`, `INT-015`, `INT-016`, `SEC-001` |
| D3 tiền tố `/api/v1` | mọi TC nhóm Int/Sec |
| D4 ngày ISO-8601 | `INT-001`, `INT-011`, `INT-016` |
| D5 `body.id` → 409 | `INT-009` (bước 4–5) |
| D6 enum SCREAMING_SNAKE / `RoleKey` kebab-case | `INT-001`, `INT-011`, `INT-016`, `SEC-001` |
| D7 message tiếng Anh nguyên văn | mọi TC error path (assert `equalTo` chuỗi MSG, không `contains`) |

### Suspension Criteria (điều kiện tạm dừng)

- ~~L3 hoặc L15 (§2) chưa được Nam chốt ⇒ tạm dừng riêng `INT-002`, `INT-015`, `INT-016`~~ — **đã chốt 2026-07-31**, điều kiện này không còn áp dụng.
- Flyway `V1` chưa có cột `code`/`partner_id` ⇒ dừng toàn bộ nhóm Integration/Security (không có schema để migrate).
- Docker không khả dụng trên máy CI ⇒ nhóm Int/Sec chuyển sang chạy nightly; **không** thay Testcontainers bằng H2 (H2 không có `JSONB`, `gen_random_uuid()`, `TIMESTAMPTZ` ⇒ test qua nhưng production vỡ).
- Phát hiện quyết định kiến trúc mới (VD phải thu hồi access token trước hạn) ⇒ cần ADR mới + Nam review trước khi viết tiếp.

---

## 7. Rollback Plan

```bash
# 1. DB test — Testcontainers là ephemeral, container chết là sạch.
#    Nếu chạy trên DB dev cục bộ (KHÔNG khuyến nghị), chỉ clean schema test:
./mvnw flyway:clean -Dflyway.url=jdbc:postgresql://localhost:5432/sfms_test \
                    -Dflyway.cleanDisabled=false
# ⚠️ TUYỆT ĐỐI KHÔNG chạy flyway:clean trên DB dev/prod — Nam giữ V1, xóa là mất schema cả 13 module.
#    Trên môi trường thật, rollback theo TDS §12.2: khôi phục pg_dump + kubectl rollout undo.

# 2. Revert code test
git checkout -- src/test/java/com/sfms/iam/

# 3. Revert stub Red Phase (nếu đã commit stub)
git revert <stub-commit-hash>

# 4. TC vẫn OPEN → giữ nguyên trạng thái 🔴 trong bảng §5, không xóa dòng (Policy 4.4 Immutable History)
```

**Không rollback tài liệu:** nếu test phát hiện TDS sai, **sửa TDS + ghi changelog**, không xóa TC. Mọi mục §2 phải giữ lại kể cả sau khi đã fix (immutable history).

---

## 8. CASE 2.0 Anti-Pattern Detection (AI-Assisted TCs)

> Test cases trong tài liệu này **có** AI hỗ trợ (§1.1) ⇒ mục này **bắt buộc** cho reviewer (Thảo chủ trì, theo Team Plan §1).
> 5 AP gốc của template + **3 AP riêng của MF01** lấy từ TDS §17.4.

| AP-ID | Anti-Pattern | Dấu hiệu trong TestSpec này | Check | Gate chặn |
|---|---|---|---|---|
| AP-AI-001 | Unconstrained Generation | TC không có dòng `Oracle Source`, hoặc oracle ghi chung chung ("theo TDS") mà không chỉ ra BR/UC/MSG cụ thể | ☐ | G-0 |
| AP-AI-002 | **Green-from-Birth** | TC PASS với stub `throw` (§5.1). Rủi ro cao nhất ở nhóm Security ô ❌ — xem cảnh báo §5.1 | ☐ | **G-2 ★** |
| AP-AI-003 | Implicit Decision | TC giả định một quyết định kiến trúc **không có** trong TDS §3 (VD giả định access token revoke được trước hạn — xem ghi chú `INT-004`) | ☐ | G-1 |
| AP-AI-004 | Layer Violation | TC unit lại assert HTTP status; hoặc TC integration assert logic thuần (đáng ra là unit) | ☐ | G-4 |
| AP-AI-005 | Hallucinated Contract | TC gọi service/field **không có** trong TDS §8 hoặc §5.2 (VD `UserService.resetPassword()` — không tồn tại) | ☐ | G-3 |
| **AP-AI-006** | **Trust FE identity** | TC chấp nhận `userId`/`role` từ body làm nguồn identity ⇒ vi phạm TDS §17 C2. Đối trọng: `MF01-TC-SEC-005` | ☐ | G-3 |
| **AP-AI-007** | **Wrong stack** | TestSpec/test code xuất hiện `prisma`, `.spec.ts`, `npm test`, `jest`, `passport`, `next-auth`, H2 thay Testcontainers | ☐ | G-0 |
| **AP-AI-008** | **Auto-create on Google** | Thiếu TC chứng minh Google login **không** tạo account, hoặc TC chỉ kiểm ở tầng service mà không kiểm qua HTTP. Đối trọng: `MF01-TC-014` (unit) **+** `MF01-TC-SEC-013` (HTTP) | ☐ | G-2 |

**Kiểm nhanh AP-AI-007 (chạy được ngay):**
```bash
grep -nEi 'prisma|\.spec\.ts|npm test|jest|passport|next-auth|H2Dialect|jdbc:h2' \
     07_implement/MF01_User_Management_Access_Control/TestSpec-MF01_User_Management.md
# kỳ vọng: chỉ khớp các dòng NÓI VỀ việc cấm dùng chúng, không phải dòng hướng dẫn dùng
```

**Kết quả review:**

- [ ] Không phát hiện anti-pattern nào → TestSpec approved
- [ ] Phát hiện AP → ghi vào bảng dưới → fix trước khi implement

| AP detected | TC ID | Mô tả | Fix action | Fixed? |
|---|---|---|---|---|
| `AP-AI-___` | `MF01-TC-___` | | | ☐ |

**Ghi chú tự đánh giá (tác giả) — 4 điểm tự nhận là rủi ro AP cao nhất, reviewer soi trước:**

1. **`MF01-TC-INT-002` (AP-AI-003)** — shape `details` cho `attemptsLeft` là **đề xuất của tôi**, không có trong TDS. Đã đánh dấu BLOCKING thay vì im lặng assert (§2 L3).
2. **`MF01-TC-INT-015`/`-016` (AP-AI-003)** — mã `IAM-404`/`IAM-422` là **suy từ khuôn** `SCH-404`/`SRV-001`, chưa được chốt. Đã để `BLOCKED-ORACLE`, chỉ assert `message` + HTTP status.
3. **`MF01-TC-SEC-001` (AP-AI-002)** — 84 tổ hợp là nơi dễ PASS giả nhất. Đã ràng buộc assert 3 tầng (status + code + message) ở §5.1.
4. **`MF01-TC-SEC-008` bước 3 / `MF01-TC-016` bước 2 / `MF01-TC-SEC-013` bước 5 (AP-AI-001)** — 3 nhánh mà BR/UC **không** quy định (tự-vô-hiệu-hóa admin thường; email khớp nhưng `sub` khác; `email_verified=false`). Đã ghi rõ "thiếu oracle, cần chốt" thay vì tự đặt expected.

> ✅ **Cập nhật 2026-07-31:** Điểm 1 và 2 ở trên (L3, L15) đã được Nam chốt vào TDS-MF01 §9.2/§10 — không còn `BLOCKING`/`BLOCKED-ORACLE`. Xem changelog cuối tài liệu.

---

## 9. Danh sách mã đã trích dẫn (đối chiếu nhanh khi review)

### 9.1 Use Cases — 20/20 của traceability §1.1 (tên **nguyên văn UC file**)

| UC ID | Tên UC (nguyên văn UC file) | TC phủ |
|---|---|---|
| UC-002 | Active/Deactivate Staff | `SEC-001` (E10), `SEC-006` |
| UC-003 | Active/Deactivate User | `INT-006`, `SEC-001` (E7), `SEC-008` |
| UC-011 | Change Password | `001`, `002`, `003`, `SEC-005`, `SEC-009` (f) |
| UC-022 | Create Role | `SEC-001` (E12) |
| UC-024 | Create Staff | `INT-010`, `SEC-001` (E9) |
| UC-026 | Create User | `INT-009`, `SEC-001` (E6), `SEC-005` (e) |
| UC-029 | Delete Role | `011`, `012`, `INT-012` |
| UC-032 | Forgot Password | `021`, `022`, `INT-008`, `SEC-009` (g), `SEC-011` |
| UC-034 | Login | `004`, `005`, `006`, `007`, `008`, `010`, `INT-001`, `INT-002`, `SEC-011`, `SEC-014` |
| UC-035 | Logout | `INT-005`, `SEC-009` (b) |
| UC-059 | Profile Manage | `SEC-001` (E3) |
| UC-097 | Update Profile | `023`, `024`, `SEC-001` (E4), `SEC-005` (a) |
| UC-098 | Update Role | `013` |
| UC-129 | View List Permission | `SEC-001` (E11) |
| UC-130 | View List Role | `INT-013`, `SEC-001` (E11) |
| UC-138 | View Profile | `SEC-001` (E3), `SEC-005` (f) |
| UC-144 | View Staff Account List | `SEC-001` (E8), `SEC-006` |
| UC-153 | View User Account List | `INT-011`, `SEC-001` (E5) |
| UC-154 | Activate Account & Set Password | `018`, `019`, `020`, `INT-007` |
| UC-191 | Login with Google (OAuth2) | `014`, `015`, `016`, `017`, `SEC-013` |

**Ngoài §1.1:** `UC-106` (audit log có IP) và `UC-070`/`UC-095` chỉ được **dẫn chiếu qua TDS**, **không** dùng làm oracle cho TC nào của MF01.

### 9.2 Business Rules — 14 mã (nguồn `business_rules.md` §1–2 v1.9)

| BR | Nội dung dùng làm oracle | TC |
|---|---|---|
| BR-001 | BCrypt, không plain text | `001`, `018`, `INT-007`, `SEC-010`, `SEC-012` |
| BR-002 | 5 sai ⇒ khóa 15' + audit | `004`, `005`, `017`, `INT-002`, `SEC-009` (c) |
| BR-003 | Reset token 1h · 1 req/15'/email | `021`, `022`, `INT-008` |
| BR-004 (v1.9) | Access 30' + refresh sliding 14d + 4 điều kiện revoke | `009`, `010`, `INT-001`, `INT-003`, `INT-004`, `INT-005`, `INT-006`, `SEC-003`, `SEC-009` |
| BR-005 | Single session | `010`, `INT-004`, `SEC-009` (e) |
| BR-006 | Chính sách MK + không trùng 3 MK gần nhất | `002`, `003`, `018`, `021` |
| BR-007 | RBAC 7 vai trò | `SEC-001`, `SEC-004`, `SEC-005` |
| BR-008 | Role hệ thống bất khả xóa · xóa khi 0 user (kể cả INACTIVE) | `011`, `012`, `013`, `INT-012`, `INT-013` |
| BR-009 | Chấp nhận điều khoản lần login đầu | `008`, `INT-007` |
| BR-010 | Chỉ login sau khi xác minh email | `006`, `007`, `017`, `INT-006`, `INT-009`, `SEC-013` |
| BR-011 | Activation token 48h, gửi lại +48h | `018`, `019`, `020`, `INT-007`, `INT-009` |
| BR-012 | Email duy nhất · SĐT hợp lệ | `INT-009`, `INT-016` |
| BR-013 | Chỉ School Admin tạo staff trường mình · System Admin tạo user toàn hệ thống | `INT-010`, `SEC-001`, `SEC-006`, `SEC-013` |
| BR-218 | Google link-only, không tự tạo account | `014`, `015`, `016`, `017`, `SEC-013` |

**BR-015** — nêu như **ràng buộc con của BR-006** (oracle của `MF01-TC-003` là câu trong thân BR-006). **Không** tính là BR chính vì (a) không có trong cột BR của traceability §1.1 (**CF-1**), (b) **không có mục định nghĩa riêng** trong `business_rules.md` (§2 **L6** — phát hiện mới).
**BR-021** (cascade trường), **BR-164** (secret trong ENV), **BR-162/170/171** (audit) — chỉ **dẫn chiếu**, thuộc module khác; dùng ở `INT-014`, `SEC-012` như ràng buộc phụ, không làm oracle chính.

### 9.3 System Messages — nguồn `error_message.md` v1.5 (nội dung **nguyên văn**)

| MSG | Loại | Dùng ở TC |
|---|---|---|
| MSG-002 | Error | `004`, `INT-002`, `INT-008`, `SEC-011`, `SEC-014` |
| MSG-003 | Error | `006`, `007`, `017`, `INT-006`, `INT-008`, `SEC-013` |
| MSG-004 | Error | `009`, `018`, `019`, `020`, `021`, `INT-003`, `INT-007`, `SEC-002`, `SEC-003`, `SEC-004`, `SEC-009` |
| MSG-005 | Error | `011`, `013`, `INT-010`, `INT-012`, `SEC-001`, `SEC-005`, `SEC-008` |
| MSG-006 | Error | `INT-009`, `INT-010` |
| MSG-008 | Error | `023` |
| MSG-009 | Error | `024` |
| MSG-076 | Error | `004`, `005`, `INT-002`, `SEC-011` |
| MSG-102 | Error | `014`, `SEC-013` |
| MSG-103 | Error | `SEC-013` |
| **MSG-104** | Error (v1.5) | `002`, `018`, `021` |
| **MSG-105** | Error (v1.5) | `003`, `021` |
| **MSG-106** | Error (v1.5) | `012`, `INT-012` |
| **MSG-137** | Error (v1.5, dùng chung) | `INT-012`, `INT-015`, `SEC-006` |
| **MSG-138** | Error (v1.5, dùng chung) | `INT-009`, `INT-016`, `SEC-014` |
| MSG-026 | Success | `015`, `INT-001` |
| MSG-027 | Success | `INT-005` |
| MSG-029 | Success | `INT-006` (⚠️ CF-6 — ngữ nghĩa lệch) |
| MSG-030 | Success | `021`, `INT-008` |
| MSG-031 | Success | `INT-009`, `INT-010` |
| MSG-028 | Success | ⚠️ **không assert** — `INT-007` (§2 **L9**) |
| MSG-062 | Warning | ⚠️ **không có TC** — xem 9.5 |
| MSG-063 | Warning | ⚠️ **cố ý không dùng** làm expected (§2 **L1**: là Warning, không chặn được thao tác) |

### 9.4 Luật `semantic-audit.ts` đã port — **RỖNG (0 luật)**

Team Plan §2c-3b nguồn 5 yêu cầu port luật liên quan module sang Gherkin. Với MF01, danh sách là **rỗng** — không phải bỏ sót. Bằng chứng:

```bash
cd 03_Design/08_FrontEnd/Web
grep -nE 'check\("' scripts/semantic-audit.ts | wc -l          # 54 luật đăng ký qua check()
grep -nE 'add\("(PHIEU_TRO_MAU|MOC_TRO_SU_CO|LO_TRO_MAT_HANG)' scripts/semantic-audit.ts   # 3 luật chỉ có add()
grep -niE 'user|role|permission|login|password|account|auth' scripts/semantic-audit.ts      # 0 kết quả
```

**Kết luận:** 57 luật của script thuộc lô nguyên liệu (`XUAT_LO_HET_HAN`, `NHAP_LO_DA_HET_HAN`…), mẫu lưu (`HAN_LUU_KHONG_DU_24H`, `MAU_SAI_BUA`…), phân suất (`PHAT_SAI_NGAY`, `HOC_SINH_SAI_LOP`…), sự cố/CAPA (`BAO_CAO_TRUOC_BUA_AN`, `CAPA_TRUOC_SU_CO`…), NCC (`GIAO_TRUOC_KHI_DAT`…), thực đơn (`SUAT_RIENG_KHONG_CO_NGUOI_AN`…), dị ứng (`XAC_MINH_MUC_NANG_KHONG_CO_MINH_CHUNG`). **Không luật nào** chạm tài khoản/vai trò/xác thực. TDS-MF01 §13 đã ghi nhận điều này; TestSpec xác nhận lại bằng lệnh trên. MF01 derive 100% từ BR/UC/MSG.

> ⚠️ Nhắc cho 12 TestSpec còn lại (Team Plan §2c-3b): tên luật **copy nguyên văn từ script**, không lấy từ TDS §13 — VD `HAN_LUU_KHONG_DU_72H` trong một số TDS **đã lạc hậu**, tên đúng hiện tại là **`HAN_LUU_KHONG_DU_24H`** (đã grep xác nhận ở dòng 88–89 của script).

### 9.5 Điểm chưa phủ + việc phải làm (tự khai, để reviewer không phải đi tìm)

| # | Chưa phủ / cần chốt | Lý do | Ai |
|---|---|---|---|
| 1 | **MSG-062** *"Suspicious login attempt detected from a new location/device. Verification code required."* — traceability §1.1 gán cho UC-034, TDS §10 ghi `IAM-062` (200/warn) | Không có BR nào định nghĩa **thế nào là "thiết bị lạ"** (BR-002 chỉ nói lockout), cũng không có UC mô tả luồng nhập mã xác minh. **Thiếu oracle hoàn toàn** ⇒ không viết TC thay vì bịa expected | Huy + Nam |
| 2 | 6 UC lệch BR giữa UC file và traceability §1.1 (§2 **L7**): UC-024, UC-059, UC-129, UC-138, UC-144, UC-153 | TestSpec bám traceability theo quy tắc Team Plan §2c; cần Huy đối chiếu và sửa **một** trong hai nguồn | Huy |
| 3 | ~~L3 (shape `details`) và L15 (MSG-137/138 vào TDS §10)~~ | **✅ Đã chốt 2026-07-31** — TDS §9.2/§10 cập nhật, 3 TC (`INT-002`, `INT-015`, `INT-016`) hết bị chặn | **Nam** |
| 4 | 4 nhánh không có BR/UC quy định: tự-vô-hiệu-hóa admin (`SEC-008` b3) · email khớp `sub` khác (`016` b2) · `email_verified=false` (`SEC-013` b5) · tên role trùng (`013` b3) | Đã ghi "thiếu oracle" trong từng TC, không tự đặt expected | Nam + Huy |
| 5 | Xung đột `INT-008` bước 7: UC-032 Exceptions đòi **MSG-003** cho tài khoản deactivated ↔ TDS §8.1 đòi **không lộ email tồn tại** | Hai yêu cầu loại trừ nhau. Khuyến nghị: chọn không-lộ, ghi 1 dòng vào TDS §17 | Nam |
| 6 | Khoảng hở access token 30' sau khi revoke phiên (`INT-004`) | Giới hạn của JWT stateless — ADR-M01 đã chọn biết trước. **Ghi nhận**, không phải bug | Nam (đã quyết) |
| 7 | Test hiệu năng NFR TDS §4.1 (login p99 < 400ms, refresh < 150ms, list < 300ms, 200 req/s) | Thuộc **k6 load test**, không thuộc TestSpec này (ISO 29119 tách performance testing). Cần 1 file `k6/mf01-auth.js` riêng | Thảo |
| 8 | `GET /api/v1/permissions` (UC-129) chỉ được phủ ở `SEC-001` (E11, kiểm quyền) | Danh mục 16 permission là **chỉ đọc, cố định** (TDS ADR-M04) ⇒ rủi ro thấp. Nếu Thảo muốn, thêm 1 TC assert đúng 16 bản ghi `PERM-001…PERM-016` | Thảo (tùy chọn) |

---

## PHỤ LỤC

### A. Ánh xạ TDS §13 → TestSpec (chứng minh "mở rộng, không viết lại")

| TDS §13 (9 kịch bản gốc) | Mở rộng thành | Số TC |
|---|---|---|
| TC-UNIT-001 BCrypt + không trùng 3 MK | `MF01-TC-001`, `-002`, `-003` (tách hash / policy / lịch sử; **sửa** mã lỗi & MSG theo §2 L1) | 3 |
| TC-UNIT-002 Lockout | `MF01-TC-004`, `-005` (thêm biên 4/5/6, mở khóa, chứng minh không hardcode) | 2 |
| TC-UNIT-003 Guard xóa role | `MF01-TC-011`, `-012`, `-013`, `INT-012` (thêm nhánh user INACTIVE — BR-008 nhấn mạnh mà mock không phủ) | 4 |
| TC-UNIT-004 Google link-only | `MF01-TC-014`, `-015`, `-016`, `-017`, `SEC-013` (thêm bảng quyết định + kiểm ở tầng HTTP) | 5 |
| TC-INT-001 Refresh sliding + single session | `MF01-TC-009`, `-010`, `INT-003`, `INT-004` (thêm biên 14 ngày, replay) | 4 |
| TC-INT-002 Deactivate revoke refresh | `INT-006` (thêm email/audit/event/reactivate) | 1 |
| TC-E2E-001 RBAC least privilege | `SEC-001` (1 kịch bản ⇒ **84 tổ hợp**), `SEC-002`…`SEC-005` | 5 |
| TC-E2E-002 Create user trùng email | `INT-009`, `INT-010` (thêm D5, `status` do server, cách ly trường) | 2 |
| TC-E2E-003 Injection / token / Google fail | `SEC-014`, `-004`, `-009`, `-013`, `021` | 5 |
| *(mới — không có trong §13)* | `006`, `007`, `008`, `018`, `019`, `020`, `022`, `023`, `024`, `INT-001`, `INT-002`, `INT-005`, `INT-007`, `INT-008`, `INT-011`, `INT-013`, `INT-014`, `INT-015`, `INT-016`, `SEC-006`, `SEC-007`, `SEC-008`, `SEC-010`, `SEC-011`, `SEC-012` | 25 |
| | **Tổng** | **54** |

### B. Glossary (bổ sung cho TDS Phụ lục A)

| Thuật ngữ | Định nghĩa |
|---|---|
| **Oracle** | Nguồn quy định giá trị kỳ vọng của test. Trong dự án này chỉ được là BR/UC/MSG/D1–D7 — **không** phải suy đoán của người viết test hay của AI |
| **BLOCKED-ORACLE** | TC đã xác định được kịch bản nhưng expected value chưa có nguồn ⇒ không implement tới khi được chốt |
| **SEMANTIC-GAP** | Có mã MSG để assert, nhưng nội dung MSG lệch ngữ nghĩa tình huống (VD MSG-003 dùng cho "chưa xác minh email") |
| **Red Gate (GATE-2)** | Cổng bắt buộc: chạy toàn suite với stub `throw`, mọi test phải FAIL trước khi implement |
| **Green-from-Birth (AP-AI-002)** | Test xanh ngay khi chưa có code — dấu hiệu assert quá lỏng hoặc tautology |
| **Gọi API trực tiếp bỏ qua UI** | Nhóm test mô phỏng `curl` bỏ qua mọi ràng buộc của giao diện, để chứng minh gate nghiệp vụ nằm ở **server** |
| **Testcontainers** | Thư viện chạy PostgreSQL 16 thật trong Docker cho mỗi lần test; thay cho H2 (H2 không có `JSONB`/`TIMESTAMPTZ`/`gen_random_uuid()`) |

### C. Tài liệu tham chiếu

| Document | Path |
|---|---|
| Quy ước chung D1–D7 (**ưu tiên cao nhất**) | `07_implement/00_Common_Conventions.md` |
| TDS Backend MF01 (nguồn chính) | `07_implement/MF01_User_Management_Access_Control/TDS-MF01_Backend.md` |
| Prompt chuẩn sinh TestSpec | `07_implement/00_Team_Work_Assignment_Plan.md` §2c-3b · hợp đồng phân trang §2b |
| Template | `06_Document/Templates/PHASE-4_Test-Spec.md` |
| UC MF01 (20 UC, gồm UC-191) | `02_Requirements/02_UseCases/MF01_User_Management_Access_Control.md` |
| Traceability §1.1 | `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` |
| Business Rules §1–2 (v1.9) | `02_Requirements/00_BusinessRules/business_rules.md` |
| System Messages (v1.5) | `02_Requirements/00_BusinessRules/error_message.md` |
| Nguồn seed/fixture | `03_Design/08_FrontEnd/Web/src/lib/mock-data.ts` · `api/mock-server.ts` · `api/db.ts` |
| Luật nghiệp vụ (0 luật MF01) | `03_Design/08_FrontEnd/Web/scripts/semantic-audit.ts` |
| Review nguồn gốc D1–D7 | `07_implement/review.md` · `review_fix_plan.md` |

---

*TestSpec MF01 v1.0 — ISO/IEC/IEEE 29119-3:2021 · CASE 2.0 (Red Gate §5.1 + Anti-Pattern §8).*
*Stack: Java 25 LTS / Spring Boot 4.1.x / JUnit 5 / Testcontainers PostgreSQL 16 / Flyway / WireMock / GreenMail / JaCoCo.*
*MF01 là module nền — tài liệu này là **bản mẫu convention** cho 12 TestSpec còn lại (Team Plan §4b).*
