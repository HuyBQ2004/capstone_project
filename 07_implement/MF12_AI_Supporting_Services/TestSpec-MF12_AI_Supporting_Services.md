# TEST-DRIVEN DEVELOPMENT SPECIFICATION
# MF12 — AI & Supporting Services

| Field | Value |
|-------|-------|
| **Document ID** | `SFMS-MF12-TDD-001` |
| **Version** | `1.1` |
| **Date** | `2026-08-04` |
| **Status** | `Approved` |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 — Software Testing Part 3: Test Documentation |
| **Document Owner** | Nguyễn Phương Thảo (ThaoNP) |
| **Author** | Nguyễn Phương Thảo — chủ module MF05/MF07/MF12, Lead Test/QA Reviewer |
| **Reviewed by** | `[ ] Bùi Quang Huy — §2 Traceability (UC/BR/MSG) — Pending` · `[ ] Nguyễn Đức Mạnh — §5 Schema + §9 API — Pending` · `[ ] Trần Tuấn Minh — §8/§9 JSON shape — Pending` |
| **DPO Sign-off** | `[ ] Pending` — ⚠️ **module có mức PII/Secret cao nhất hệ thống**: `audit_logs` chứa actor + IP (BR-173), nội dung hội thoại AI có thể chứa dữ liệu HS, và `external_integrations` chứa **credential dịch vụ ngoài** (BR-164) |
| **Approved by** | `[ ] Nguyễn Văn Nam — Final Approval Pending` |
| **Classification** | `Internal — Confidential` |

**References (nguồn oracle — mọi expected value trích từ đây, KHÔNG dùng giả định):**

| # | Nguồn | Dùng cho |
|---|-------|----------|
| 1 | `07_implement/MF12_AI_Supporting_Services/TDS-MF12_Backend.md` (`SFMS-MF12-IMP-001`) | Spec kỹ thuật gốc — §5 schema, §8 interface, §9 API, §10 error code, §13 kịch bản, §16 auth matrix, §17 constraints C1–C14 |
| 2 | **`07_implement/00_Common_Conventions.md` (D1–D7) — ✅ Approved 2026-07-30** | **Quy ước ưu tiên CAO NHẤT.** Ghi đè ADR/constraint module khi xung đột (§2.0 L1–L4) |
| 3 | `02_Requirements/02_UseCases/MF12_AI_Supporting_Services.md` | Normal/Alternative Flow. ⚠️ UC file **không có** UC-185…UC-190 và **có thừa** UC-058/079/088/136 — xem **L5/CF-1** |
| 4 | `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` **§1.12** | Nguồn CHUẨN duy nhất của UC↔BR↔MSG cho MF12 (**15 UC**) |
| 5 | `02_Requirements/00_BusinessRules/business_rules.md` §1, §12, §13, §14, §15, §16 | Nội dung BR |
| 6 | `02_Requirements/00_BusinessRules/error_message.md` | Chuỗi MSG nguyên văn (oracle của assert message) |
| 7 | `03_Design/02_ADR/ADR-000.md` §Mock Strategy | *"Gemini AI: trả mock JSON đúng schema"*; *"Mock external services PHẢI trả response đúng format contract"* |
| 8 | `03_Design/08_FrontEnd/Web/src/lib/api/mock-server.ts` + `db.ts` + `hooks.ts` | Hợp đồng REST (PATCH-semantics; generic handler dòng 347–376 — nguồn của CF-9) |
| 9 | `03_Design/08_FrontEnd/Web/src/lib/mock-data.ts` | **Nguồn fixture** (§3 TDS-05) — dùng ID thật, không bịa |
| 10 | `07_implement/00_Team_Work_Assignment_Plan.md` §2b | Hợp đồng phân trang/search/filter dùng chung |
| 11 | PDPD (**Luật 91/2025**) | BR-173 (không log nội dung hội thoại), BR-181 (báo cáo không lộ danh tính HS), BR-164 (credential) |

> **⚠️ RÀNG BUỘC STACK — tài liệu này KHÔNG dùng cú pháp mẫu của `PHASE-4_Test-Spec.md`.**
> Template gốc minh hoạ bằng TypeScript/Jest/Prisma. Dự án dùng **Java 25 LTS · Spring Boot 4.1.x · PostgreSQL 16 · Flyway · Spring Security + JWT · MapStruct · Spring Boot Actuator · PDFBox + Apache POI** (BR-178).
> Test = **JUnit 5 + AssertJ + Mockito** (unit) · **`@SpringBootTest` + Testcontainers `PostgreSQLContainer` + MockMvc** (integration) · rollback = **Flyway migration bù** (KHÔNG `npx prisma migrate reset`) · fixture = **Java builder** (KHÔNG `makeEntity()` TypeScript).

> **⚠️ ÁP DỤNG `00_Common_Conventions.md` D1–D7 (Approved 2026-07-30).**
> Thứ tự ưu tiên khi xung đột: **`00_Common_Conventions.md` > ADR module (TDS §3) > hợp đồng mock FE** — đúng như TDS-MF12 §3 tự khai.
> Hệ quả: **D1** JSON `id` = `code` (PK UUID) → DB assertion dùng `WHERE code = ?` · **D2** lỗi trong envelope `{"error":{"code","message","details"}}` → assert `$.error.code` · **D3** mọi path `/api/v1/...` · **D4** JSON ngày **ISO-8601** → **ghi đè** C13/CF-26 (`dd/MM/yyyy`) · **D5** nhận `id` client, trùng → 409 · **D6** enum dùng chung ở `common/enums/` — ⚠️ `RoleKey` **kebab-case** ảnh hưởng UC-150; `ReportPeriod` **không** nằm trong 8 enum của D6 · **D7** `message` **tiếng Anh** nguyên văn `error_message.md` → ảnh hưởng 6 mã `AIS-*`/`IAM-*` hiện viết tiếng Việt.
> Chi tiết 4 điểm ghi đè ở **§2.0**.

> **Quy ước TDD:** tài liệu này mô tả test cases **TRƯỚC** khi viết production code.
> Thứ tự: viết test (`*Test.java`) → chạy → xác nhận **FAIL 🔴** → implement → **PASS 🟢** → refactor 🔵.
> Không mark ✅ nếu `mvn verify` chưa xanh. Test data dùng school `SCH-001` (QA seed), **không dùng PII thật**, **không dùng credential thật** (`FX-026`).

---

## CHANGELOG

> **Policy 4.4 — Immutable History:** không xóa thông tin cũ.

| Ngày | Người thực hiện | Nội dung thay đổi |
|------|-----------------|-------------------|
| 2026-08-04 | Nguyễn Phương Thảo | **v1.1 — Bước 0 + Bước 1 của `02_Module_Completion_Workflow.md`. Status `Draft` → `Approved`.** Nguồn mới, ưu tiên **cao nhất**: `04_Development/Backend` đã merge (V1–V21) — đo bằng 5 lệnh Bước 0, không suy từ tài liệu. **(1) Thêm §2.3 Nhóm D (L30–L36)** — bảy chỗ **code đã merge ghi đè TDS**, khác Nhóm A/B/C ở chỗ không có gì để chờ chốt. **(2) Đóng 2 mục treo bằng bằng chứng, không bằng họp:** `L7`/CF-3 → MF07 sở hữu checklist (`M07-D10` đã merge cả bảng `V12:209/223` lẫn controller 4 verb) ⇒ `TC-017`/`TC-018`/`TC-INT-011`/`TC-SEC-011` chuyển `@Disabled`; `sent_reports` → MF12 giữ (`ADR-M13-2` của TDS-MF13 đã tự gỡ) ⇒ **CF-M11-20 khép lại**. **(3) `L13`/`L35` — bỏ mọi assert dựa trên `REVOKE`/`GRANT`**: role `sfms_app` không tồn tại, assert theo nó pass giả. Thay bằng 3 lớp kiểm được (`@Immutable` · 405 `IAM-162` · chuỗi hash). **(4) `L34` — 92 chỗ `SYS-*` → `AIS-*`**, riêng `IAM-162`/`IAM-184` giữ `IAM` (ModuleCodeResolver đã gán `audit-logs`/`security-policies` → IAM). **(5) 14 chỗ `/reports/*/send` → `/sent-reports/*`** (`M12-D06`). **(6) Thêm 5 TC:** `TC-INT-022` (bean `AiAssistPort` gỡ fallback 503 của MF05 — TC liên module đo được), `TC-SEC-019` (405 thay `REVOKE`), **`TC-CON-007` (D11)**, **`TC-CON-008` (D12)**, `TC-CON-009` (Bước 0: migration không chạm bảng module khác + app khởi động được). D11/D12 là **bắt buộc theo §7 Bước 1 việc 3** — grep trước 04/08 ra 0 kết quả thật. **73 → 78 TC** (4 TC checklist chuyển `@Disabled`, còn **74 chạy được**). |
| 2026-07-30 | Nguyễn Phương Thảo | Khởi tạo v1.0 — TDD spec cho MF12. Nguồn: TDS-MF12_Backend.md (§13, §16, §17 C1–C14), **`00_Common_Conventions.md` D1–D7 (Approved 30/07)**, traceability §1.12 (**15 UC**), `business_rules.md` §1/§12/§13/§14/§15/§16, `error_message.md`, ADR-000 §Mock Strategy, fixture từ `mock-data.ts`. Tổng **73 test case**: 31 unit · 18 security · 21 integration · 3 E2E. Ghi nhận **29 Logic Issue** (L1–L29): L1–L4 là **D1/D2/D4/D7 ghi đè** constraint TDS; L5–L26 kế thừa CF-1…CF-29; L27–L29 (§2.2) là 3 mâu thuẫn **cần Nam/Huy chốt** trước khi implement. |

---

## MỤC LỤC

1. [Thông tin Module](#1-thông-tin-module) · 1.1 [AI Generation Context](#11-ai-generation-context-case-20)
2. [Logic Issues Resolved](#2-logic-issues-resolved)
3. [Test Design Specification](#3-test-design-specification)
4. [Test Case Specification](#4-test-case-specification) · [Unit](#41-unit-test-cases) · [Security](#42-security-test-cases) · [Integration](#43-integration-test-cases) · [E2E](#44-e2e-test-cases)
5. [Red-Green-Refactor Tracker](#5-red-green-refactor-tracker) · 5.4 [Red Gate Protocol](#54-red-gate-protocol-case-20--gate-2)
6. [Entry / Exit Criteria](#6-entry--exit-criteria)
7. [Rollback Plan](#7-rollback-plan)
8. [CASE 2.0 Anti-Pattern Detection](#8-case-20-anti-pattern-detection)
9. [Phụ lục — Danh sách mã UC/BR/MSG đã trích dẫn](#9-phụ-lục--danh-sách-mã-ucbrmsg-đã-trích-dẫn)

---

## 1. Thông tin Module

| Field | Value |
|-------|-------|
| **Feature / Module ID** | `MF12` — AI & Supporting Services |
| **Bounded Context** | Platform Services (AI Gateway · Observability · System Configuration) |
| **Spec gốc** | `SFMS-MF12-IMP-001` (`TDS-MF12_Backend.md`) |
| **Priority** | 🟡 **P2** — nhưng **`AuditLogPort` + `SecurityPolicy` là nền chung**, cần có sớm cùng MF01/MF02 |
| **Số UC trong phạm vi** | **15** — UC-012, UC-119, UC-040, UC-057, UC-106, UC-111, UC-149, UC-150, UC-184, UC-185, UC-186, UC-187, UC-188 *(Deprecated)*, UC-189, UC-190. ⚠️ `00_` §3 ghi **12 UC**, UC file có **13 UC** khác danh sách — xem **L5/CF-1** |
| **Data Classification** | `Internal` + **`Secret`** (API key / OAuth credential — BR-164) + **`PII`** (audit actor + IP — BR-173; nội dung hội thoại AI) |
| **Compliance Scope** | **PDPD (Luật 91/2025)** — BR-173, BR-181 · **BR-162/171/172** (audit bất biến, retention, toàn vẹn) · BR-164 (mã hoá credential) |
| **Upstream Dependencies** | MF01 (JWT/RBAC, `users`, **sở hữu bảng `security_policies`** — CF-6), MF02 (`schools`), **Gemini AI** (stub dev), **Firebase FCM / SMTP** (stub dev) |
| **Downstream Consumers** | **MF05** (gọi `AiAssistPort` cho UC-075 — BR-156/157) · **MF07** (đọc `checklist_groups` chấm điểm UC-014; nguồn dữ liệu báo cáo tuân thủ) · **MF01** (đọc `SecurityPolicy` cho BR-002/004/006) · **MF10** (`NotificationPort`) · **toàn bộ 13 module** (ghi `audit_logs` theo BR-170) |
| **Package Java** | `com.sfms.mf12.{ai, audit, integration, policy, checklist, report, metrics}` |
| **Test module** | `src/test/java/com/sfms/mf12/**` |
| **Migration** | Flyway `V22__mf12_platform_services.sql` · `V23__mf12_seed_integrations.sql` (TDS §11.3, `M12-D12` — **V1–V21 đã dùng**; v1.0 ghi V16/V17 là số cấp từ 28/07) |

**4 nhóm nghiệp vụ trong 1 module — mỗi nhóm có rủi ro test khác nhau:**

| Nhóm | Nội dung | UC | Rủi ro chính khi test sai |
|---|---|---|---|
| **(A) Trợ lý AI** | Chat Gemini + **lưu trữ hội thoại bắt buộc** | UC-012, UC-119 | Archive lỗi mà vẫn trả phản hồi ⇒ vi phạm BR-157, mất dấu vết tuân thủ. ⚠️ **Chưa có gì trong mock** (L6/CF-2) |
| **(B) Quản trị hệ thống** | Tích hợp ngoài, audit log, error log, trạng thái/thống kê, chính sách bảo mật | UC-040, UC-057, UC-106, UC-111, UC-149, UC-150, UC-184 | **Audit sửa được** ⇒ mất bằng chứng vận hành (BR-162). **Credential lộ** ⇒ rò khoá dịch vụ ngoài (BR-164) |
| **(C) Checklist bếp động** | Medical Staff tự CRUD | UC-186 | Xoá cứng ⇒ mất dữ liệu lịch sử; thiếu `school_id` ⇒ **checklist trường này lộ sang trường khác** (L23/CF-25) |
| **(D) Báo cáo ATTP chủ động** | Soạn & gửi báo cáo định kỳ + lịch sử | UC-185, UC-187, UC-188, UC-189, UC-190 | Gửi báo cáo **rỗng** (chặn chỉ ở client — L22/CF-24); báo cáo **lộ danh tính HS** (BR-181) |

> ⚠️ Nhóm **(C)** và **(D)** thuộc **nghiệp vụ ATTP của MF07** nhưng traceability xếp ở §1.12 — xem **L7/CF-3**, **cần Nam chốt ranh giới** trước khi 2 module cùng gen entity (đối chiếu `TDS-MF07` §2.3 CF-9).

**Coverage mục tiêu:** **≥ 90% line** cho `com.sfms.mf12.{ai, audit, integration}`; **100% branch** cho `AiArchiveService` (BR-157 — rollback), `AuditLogService` (BR-162/172 — append-only + hash chain), `CredentialCipher` (BR-164), `IntegrationService.save()` (BR-163 — gate test-connection). Ba lớp này là **nền chung của 13 module**, lỗi ở đây lan ra toàn hệ thống.

### 1.1 AI Generation Context (CASE 2.0)

| Field | Value |
|-------|-------|
| **AI Assisted?** | `Yes` — soạn thảo có AI hỗ trợ, oracle do người xác nhận |
| **Constraint Source** | `TDS-MF12_Backend.md` §17.1 (C1–C14), §10 (error codes), §16 (auth matrix + bảng field-level), §13 (kịch bản); **`00_Common_Conventions.md` D1–D7** |
| **Constraints Injected** | C1 AI qua `AiAssistPort` + archive **cùng transaction** · C2 `audit_logs` append-only + hash chain · C3 credential AES-256-GCM, chỉ trả `credentialMasked` · C4 `save()` chỉ khi test-connection PASS ≤10 phút · C5 checklist xoá = ẩn · C6 kỳ rỗng → chặn gửi + PDF/XLSX + không lộ HS + snapshot bất biến · C7 UC-188 deprecated → 409 · C8 `security_policies` do MF01 sở hữu bảng · C9 metric từ Actuator, không bảng riêng · C10 `PUT` partial merge · C11 không tin dữ liệu client quyết định · C12 `school_id` từ JWT · C13 tên field JSON *(phần ngày đã bị D4 ghi đè — L3)* · C14 `GET /error-logs` không lược `stackTrace`/`sessionInfo` |
| **Model** | Claude Opus (`00_` §2c-4 — TestSpec = Opus vì oracle phải khớp BR/MSG) |
| **Trust Level** | `T2 → T3` (pending Red Gate §5.4) |
| **Oracle policy** | Mọi `Expected Result` **phải** có dòng `Oracle Source` trỏ BR/UC/MSG/D1–D7/ADR. TC không có oracle → reject (AP-AI-001). |

---

## 2. Logic Issues Resolved

> **Bắt buộc điền trước khi viết test.** Test cases encode hành vi **đã sửa**.
> **L1–L4** là các chỗ `00_Common_Conventions.md` (D1–D7, Approved 30/07) **ghi đè** constraint trong TDS-MF12 — loại quan trọng nhất vì TDS chưa cập nhật lại.
> **L5–L26** kế thừa CF-1…CF-29 của TDS §2.3 — **không tự chọn bên đúng**. **L27–L29 (§2.2)** là 3 mâu thuẫn cần Nam/Huy chốt.

### 2.0. Nhóm A — `00_Common_Conventions.md` ghi đè TDS (ưu tiên xử lý trước)

| # | Quy ước | TDS-MF12 hiện viết | Hành vi được TEST | TC liên quan |
|---|---|---|---|---|
| **L1** | **D1** — JSON `id` = mã nghiệp vụ (`code`), PK **UUID**; path `{code}`; FK trong JSON dùng `code` | §5.2 khai PK `VARCHAR` = chính mã nghiệp vụ (D1 xếp MF12 vào nhóm *"PK là `VARCHAR(32/40/64)`"*) | Entity có `@Id UUID id` **+** `code VARCHAR(32) NOT NULL` + `UNIQUE(school_id, code)`; JSON trả `code` dưới tên `id`. **Mọi DB assertion dùng `WHERE code = ?`.** Path `GET /api/v1/audit-logs/AUD-99120` **không đổi**. ⚠️ **`audit_logs` là bảng của MF01** (`V1:278`, đã có sẵn `id UUID` + `code VARCHAR(32)`) ⇒ MF12 **không** migrate lại; assert theo cột thật, và tên nhãn actor là **`actor`** chứ không phải `actor_label` (`M12-D01`). ⚠️ Riêng `external_integrations` **không có `school_id`** (L11/CF-7) ⇒ `UNIQUE(code)` thay vì `UNIQUE(school_id, code)`; `code` = `"gps"`/`"gmail"`/`"firebase"`/`"gemini"` (mock đang dùng làm `id`) | `TC-INT-021`, mọi TC-INT-* |
| **L2** | **D2** — envelope lỗi `{"error":{"code","message","details"}}` | §10 liệt kê code + message, không khai envelope | Assert **`$.error.code == "AIS-163"`**, `$.error.message`, `$.error.details` là **mảng** (không bao giờ `null`). ⚠️ `TC-015` (IAM-184) cần `details` chứa khoảng cho phép (`allowed: "3–10"`) ⇒ `details` là chỗ duy nhất chở thông tin đó | mọi TC error path |
| **L3** | **D4** — JSON ngày **luôn ISO-8601**; **bỏ** mọi `@JsonFormat("dd/MM/yyyy")` | §17.1 **C13** ghi *"Ngày format `dd/MM/yyyy [HH:mm[:ss]]`"*; **CF-26** chốt *"format ra `dd/MM/yyyy` ở tầng response"* | **Assert ISO** cho `timestamp` (audit/error log), `sentAt`, `date`, `updatedAt`, `lastTestedAt`. Backend **vẫn parse cả 2 dạng** khi NHẬN (mock đang gửi locale `vi-VN`) nhưng **TRẢ VỀ ISO**. ⚠️ D4 ghi rõ *"không sửa seed `mock-data.ts` ở đợt này"* ⇒ Minh thêm `lib/format.ts` ở tầng render | `TC-026`, `TC-E2E-003` |
| **L4** | **D7** — backend trả `message` **tiếng Anh** nguyên văn `error_message.md`; *"Mã dẫn xuất BR đang viết **tiếng Việt** trong TDS §10 **phải dịch sang tiếng Anh** khi được cấp MSG chính thức"* | §10 có **6 mã viết tiếng Việt**: `AIS-157`, `AIS-163`, `AIS-164`, `IAM-184`, `AIS-188`, `IAM-162` (đều *"không có MSG riêng"*) | TC assert **`$.error.code`** (mã kỹ thuật — ổn định), **KHÔNG** assert chuỗi `message` tiếng Việt như oracle cuối cùng. Với mã **có** MSG nguồn (`AIS-004/005/073/092/094/096/097`) thì assert **nguyên văn tiếng Anh**. Ghi rõ 6 mã trên **chờ Huy cấp MSG chính thức** rồi mới khoá assert chuỗi | `TC-002`, `TC-006`, `TC-009b`, `TC-015`, `TC-023`, `TC-INT-004` |

> ⚠️ **D6 — hai lưu ý riêng của MF12** (không phải ghi đè, nhưng dễ sai):
> · **`RoleKey` (kebab-case)** nằm trong 8 enum của D6 ⇒ màn UC-150 *(usage-statistics)* nhóm theo role **phải** dùng `system-admin`/`school-admin`/`teacher`/`parent`/`medical-staff`/`service-caterers`/`supplier`. Mock khai `ROLE_KEYS` hardcode trong `.tsx` (L20/CF-19) — khi backend trả, phải đúng 7 khoá này.
> · **`ReportPeriod` KHÔNG nằm trong 8 enum của D6** ⇒ quy ước SCREAMING_SNAKE của D6 **không tự động** áp cho nó. C13/CF-27 (`period` = `"Weekly"`/`"Daily"`, parse case-insensitive) **vẫn có hiệu lực**. Tuy nhiên đây là ngoại lệ duy nhất còn lại trong module ⇒ **đề nghị Nam bổ sung `ReportPeriod` vào bảng D6** để nhất quán; tới lúc đó `TC-027` test theo C13.

> **Ghi chú cho Mạnh & Minh:** L1–L4 nghĩa là **TDS-MF12 §5.2/§9/§10/§17 cần một bản cập nhật** theo checklist cuối `00_Common_Conventions.md`. TestSpec này viết theo **D1–D7**. **Không** sửa TestSpec để khớp TDS cũ.

### 2.1. Nhóm B — mâu thuẫn nguồn kế thừa từ TDS §2.3

| # | Nguồn CF | Spec gốc (sai / thiếu / lệch) | Thực tế | Fix áp dụng trong test |
|---|---|---|---|---|
| **L5** | CF-1 | **Số & danh sách UC lệch 3 nguồn:** `00_` §3 = **12 UC**; §1.12 = **15 UC**; UC file = **13 UC** nhưng **không có** UC-185…190 và **có thừa** UC-058, UC-079, UC-088, UC-136 | traceability §1.12 xếp UC-058/088/136 ở §1.10 (MF10) và UC-079 ở §1.9 (MF09) | Test bám **§1.12** (15 UC). **Không** TC nào gán UC-058/079/088/136. Ghi rõ để MF09/MF10 không bỏ sót. Chờ **Huy** chốt và **Nam** cập nhật `00_` §3 (12 → 15). |
| **L6** | CF-2 | §1.12 có UC-012 + UC-119 (BR-157, MSG-073); `00_` §2b-3 đã đặt sẵn `SEARCH_FIELDS` cho *"MF12 AI → `AIRequest` → `prompt`, `module`"* | **mock KHÔNG có gì**: không resource `ai-chats`/`ai-requests`, không route AI Chat | TC-001…005 test **phần MỚI** theo BR-157 + tên entity `AIRequest` mà plan đã đặt; đánh dấu **blocked** cho tới khi Minh bổ sung route + resource mock (`00_` §0 chống lệch). |
| **L7** | CF-3 | §1.12 xếp UC-185/186/187/188/189/190 vào **MF12**; nhưng `error_message.md` §4 xếp MSG-091/092/094/096/097/098/099 vào nhóm **"Food Safety Inspection"** (MF07) | bản chất nghiệp vụ là ATTP của MF07 | ✅ **CHỐT 2026-08-04 (Bước 0, `M12-D02`) — ngược với bản 1.0.** MF07 **đã merge** `V12:209/223` + `mf07/web/ChecklistGroupController` (4 verb, `MEDICAL_STAFF`). **MF07 sở hữu checklist, MF12 sở hữu `sent_reports`.** Mọi TC checklist của MF12 (`TC-017`, `TC-018`, `TC-INT-011`, `TC-SEC-011`…) **chuyển thành `@Disabled("M12-D02 — thuộc MF07")`** hoặc gỡ; `TestSpec-MF07` đã phủ. **Không** viết TC cho bảng mình không sở hữu — chạy song song sẽ tranh fixture. Không cần Nam chốt. |
| **L8** | CF-4 | §1.12 vẫn liệt kê **UC-187** và **UC-188** như UC còn hiệu lực; mock còn route `report-center` + type `IncidentSummarySentReport` | **BR-213 ghi UC-187 + UC-188 Deprecated 26/07/2026** | `TC-023` assert `POST /sent-reports/incidents` → **409 `AIS-188`**; **không** cho tạo `reportType='INCIDENT_SUMMARY'` mới. UC-187 giữ endpoint hub nhưng chỉ gom UC-185 + UC-189 (`TC-INT-019`). Đọc lịch sử báo cáo cũ **vẫn phải chạy** (`TC-024`). |
| **L9** | CF-5 | BR-165 mô tả *"báo cáo sức khỏe hệ thống **hàng tuần** tự động"*; §1.12 gán BR-165 cho UC-149/UC-150 là **màn xem theo yêu cầu** | UC file cho UC-057/149/150 lại liệt kê BR-158…164 + BR-177/178/179 (không khớp §1.12) | Test **cả hai**: endpoint xem theo yêu cầu (`TC-INT-017`) **và** `@Scheduled` weekly (`TC-020`). Oracle của phần job là **BR-165 nguyên văn**, của phần endpoint là **§1.12 + §9.2**. |
| **L10** | CF-6 | §1.12 gán UC-184 cho MF12 nhưng **BR-002/004/006 là quy tắc của MF01**; `TDS-MF01` §11.1 đã ghi seed `SEC-DEFAULT` | ranh giới sở hữu bảng chưa chốt | **Ranh giới test:** MF01 **sở hữu bảng + seed** `security_policies` (V1/V2 do Nam giữ); MF12 **sở hữu endpoint đọc/sửa** (UC-184). `TC-INT-013` assert MF12 migration **KHÔNG** tạo lại bảng. Hiệu lực liên module test ở `TC-INT-014`. **Cần Nam xác nhận.** |
| **L11** | CF-7 | `data_model.puml`: `ExternalIntegration n --> 1 SchoolRecord` (theo trường) | mock là danh sách **toàn hệ thống** (4 dịch vụ, không `schoolId`), actor = System Admin | Fixture + assert theo **mock**: `external_integrations` **không** có `school_id`. ⚠️ Kết hợp **L1/D1**: `UNIQUE(code)` chứ không `UNIQUE(school_id, code)`. Cần Mạnh cập nhật `data_model.puml`. |
| **L12** | CF-8 + CF-19 | Màn `system-admin/{monitoring,system-status,usage-statistics,backup,dashboard}` **có thật** | `db.ts` **không có** resource; 3 UC (UC-057/149/150) dùng **hằng số hardcode trong `.tsx`** (`WEEK_PERIOD`, `WEEKLY_SUMMARY`, `ROLE_KEYS`, `RANGES`, `BACKUPS`, `INTERNAL_SERVICES`) | Test endpoint mới `GET /system/{activities,status,usage,backups}` lấy từ **Actuator** (ADR-M12-04); `TC-INT-018` assert **không** tạo bảng `system_metrics`. ⚠️ **3 UC này hiện chưa có gì để nghiệm thu** ⇒ đánh dấu blocked chờ Minh thay hằng số. |
| **L13** | CF-9 | mock cho phép `PUT`/`DELETE` **mọi** resource qua generic handler (dòng 347–376), **kể cả `audit-logs`** | **BR-162** (audit bất biến) + **BR-172** (toàn vẹn) | `TC-013` + `TC-INT-004` + `TC-INT-003` assert backend **KHÔNG** expose `PUT`/`DELETE` cho `/audit-logs` (405 `IAM-162`). ⚠️ **Vế "DB `REVOKE UPDATE, DELETE`" đã gỡ (`M12-D13`)** — role `sfms_app` không tồn tại trong dự án, assert theo nó sẽ **fail vĩnh viễn**. Thay bằng: `TC-INT-003` sửa thẳng 1 dòng bằng SQL rồi assert `GET /audit-logs/integrity` trả **`valid=false`** — chuỗi hash BR-172 bắt được cả trường hợp `REVOKE` không bắt được. Đây là **giới hạn có chủ đích của mock**, không phải hợp đồng — Minh không được kỳ vọng CRUD đầy đủ. |
| **L14** | CF-10 | 2 màn từng nói ngược nhau về Gemini (*"Chưa kết nối"* vs *"đang chạy, chậm 2.1s"*) | mock đã gộp về **1 nguồn** `EXTERNAL_INTEGRATIONS` (22/07/2026) | `TC-INT-016` assert **một nguồn duy nhất**: mọi màn đọc cùng bảng `external_integrations` (`connected`, `statusNote`, `lastTestedAt`); `GET /system/status` **không** tự sinh trạng thái Gemini riêng. |
| **L15** | CF-11 | `SENT_REPORTS` là **union type** 3 loại báo cáo, mỗi loại shape field khác nhau | PostgreSQL không có union type | §5.2 dùng **single-table + `JSONB payload`** (ADR-M12-05). `TC-024`/`TC-INT-020` assert shape JSON trả FE **giữ nguyên union của mock** (không bọc `payload` ra ngoài). |
| **L16** | CF-14 | BR-163 (*"kiểm tra kết nối thành công **trước khi** lưu"*) | **CHƯA TỪNG CHẠY:** `testConnection()` là `setTimeout(600ms)` **thuần client**, luật thành/bại là *"key chứa chuỗi `invalid` → fail"*; `save()` set `connected: true` **vô điều kiện** | `TC-006`/`TC-007`/`TC-008` + `TC-INT-016` assert backend mở `POST /integrations/{code}/test-connection` (handshake thật, timeout 5s) và **chặn `PUT`** khi test gần nhất chưa PASS **hoặc quá 10 phút** → **409 `AIS-163`**; backend **bỏ qua** `connected` FE gửi, tự suy từ `last_test_passed`. |
| **L17** | CF-15 | BR-164 (mã hoá credential) | **`apiKey` KHÔNG BAO GIỜ rời trình duyệt** — FE giữ trong `useState` rồi vứt; payload `PUT` **không có** field credential nào ⇒ **BR-164 hiện không có gì để mã hoá** | `TC-009`/`TC-010` + `TC-SEC-006` test theo **hành vi đích**: nhận `apiKey`, mã hoá AES-256-GCM, **chỉ trả `credentialMasked`**. ⚠️ Đánh dấu **blocked** chờ Minh bổ sung `apiKey` vào payload. **Ưu tiên cao.** |
| **L18** | CF-16 | BR-006 quy định `requireMixedCase` là thành phần chính sách mật khẩu | FE gửi **7/8 field**; `requireMixedCase` **đóng băng ở giá trị seed** (`true`), System Admin không đổi được | `TC-016b`*/`TC-INT-013` assert **partial merge (C10)**: field vắng ⇒ **giữ giá trị cũ**, **không** set null/false. Minh bổ sung checkbox. *(`TC-016b` = phần assert partial-merge trong `MF12-TC-016`.)* |
| **L19** | CF-17 | UC-106 bước 4 (*"System Admin applies filters: **Date range**…"*) + BR-171/174 | FE khai state `from`/`to` và render 2 ô `<input type="date">` nhãn *"Từ ngày (BR-171)"* nhưng object `filters` **chỉ nhận `ip` và `result`** ⇒ 2 giá trị ngày **không bao giờ được truyền** | `TC-INT-008` assert backend **hỗ trợ đầy đủ** `dateFrom`/`dateTo` trên cột `timestamp` theo `00_` §2b. Đây là **lỗi FE**, không phải thiếu backend — ghi rõ để Minh thêm 2 field vào `filters`. |
| **L20** | CF-18 | §9.2 giữ `GET /error-logs/{code}` là ⬜ (FE chưa gọi) | FE render **`stackTrace` + `sessionInfo`** trực tiếp ở bảng danh sách, **không** gọi endpoint detail | `TC-INT-007` assert `GET /error-logs` **KHÔNG được lược** 2 field nặng này. Lược để "tối ưu payload" ⇒ **màn chi tiết lỗi trống** (UC-111 bước 5). |
| **L21** | CF-20 | §16 + BR-175 xếp `security-policies` **chỉ System Admin** | `(auth)/login/page.tsx:38` và `(auth)/reset-password/page.tsx:28` gọi `GET /security-policies` **khi CHƯA đăng nhập** | `TC-SEC-013` assert: `GET /security-policies` **vẫn 401/403** khi không có JWT (**không hạ quyền**), và endpoint mới **`GET /api/v1/public/password-policy`** trả **tập con không nhạy cảm** (không `sessionTimeoutMinutes`, không `updatedBy`/`updatedAt`). Minh đổi 2 màn `(auth)`. **Ưu tiên cao.** |
| **L22** | CF-24 | BR-209/BR-214 ghi rõ *"**hệ thống chặn** gửi"* khi kỳ rỗng | Chặn **chỉ ở client** (disable nút) ⇒ **gọi thẳng API là lách được**, tạo báo cáo rỗng | `TC-021`/`TC-022` + `TC-SEC-014` assert backend chặn **422 `AIS-092`/`AIS-097`** server-side; **không** tạo dòng `sent_reports`. |
| **L23** | CF-25 | BR-210 quy định checklist phạm vi **theo từng trường** (*"không ảnh hưởng tới checklist của trường khác"*) | `checklist/page.tsx:183–187` **không gửi `schoolId`** | ⛔ **CHUYỂN SANG MF07 (`M12-D02`)** — `TestSpec-MF07` giữ 2 TC này. Ràng buộc `school_id` từ JWT **vẫn áp cho MF12** nhưng ở resource **`sent-reports`** (BR-176), test bằng `TC-SEC-014`. |
| **L24** | CF-22 | UC-188/BR-213 **Deprecated 26/07/2026** | Dialog gửi báo cáo sự cố **vẫn tồn tại** và vẫn `POST /sent-reports` với `reportType:"INCIDENT_SUMMARY"` (`send-incident-summary-dialog.tsx:58`) | `TC-023` assert **409 `AIS-188`** cho **cả hai** đường: endpoint mới `/sent-reports/incidents` **và** đường cũ `POST /sent-reports` với `reportType='INCIDENT_SUMMARY'` (nếu chỉ chặn 1 đường thì FE hiện tại lách được). Minh gỡ dialog cùng đợt. |
| **L25** | CF-23 | BR-178 (xuất **PDF + XLSX** kèm tiêu đề/kỳ/tên trường/timestamp) | **chưa hiện thực ở đâu**: payload không gửi `filePdfUrl`/`fileXlsxUrl`, không nút tải file. Comment mock ghi thẳng *"Mock: bản thật xuất PDF/Excel (BR-178)"* | `TC-INT-015` assert backend **sinh file lúc gửi** (PDFBox + Apache POI) và `GET /sent-reports/{code}/download?format=` trả đúng content-type + 4 thành phần bắt buộc. Minh thêm nút Tải về. |
| **L26** | CF-28 + CF-12 | mock gửi `counts.partnerCount` (**ngoài interface**) và `staffCount` (**tàn dư BR-184 đã Deprecated**); `ComplianceSentReport` còn `latestPestControl` (**BR-209 sửa 27/07 đã bỏ**) | interface `ViolationSentReport` chỉ khai 3 khoá | Cột `payload JSONB` **dung nạp** được ⇒ `TC-022`/`TC-024` assert: báo cáo **mới** **không** sinh `latestPestControl`, `staffCount` giữ **`0`** (không sinh dữ liệu cá nhân mới); báo cáo **cũ** vẫn **đọc lại được** đủ field. Minh đồng bộ interface. |

### 2.2. Nhóm C — mâu thuẫn cần Nam chốt trước khi implement (không tự quyết)

| # | Nguồn CF | Vấn đề | Policy test |
|---|---|---|---|
| **L27** | CF-21 | **School Administration đọc `audit-logs`**: `school-admin/partnerships/page.tsx:189` dùng `<DataTable resource="audit-logs">`, trong khi §16 + UC-106 xếp audit log **chỉ System Admin** | `TC-SEC-002` test **hành vi khoan dung**: assert System Admin **luôn** đọc được; phần School Admin viết thành **2 TC loại trừ nhau**, cả hai `@Disabled("Chờ CF-21 — Nam chốt")`: (a) 403 nếu chốt "chỉ SysAdmin", (b) 200 scope trường mình nếu chốt theo BR-176. **Không** khoá một bên. |
| **L28** | CF-29 | **Thông báo phát từ client**: `lib/notify.ts:31` gọi `POST /notifications` ngay trong `onSuccess` ⇒ rớt mạng giữa 2 request thì thao tác thành công mà **không ai được báo** (cùng loại lỗi với CF-14 của MF07) | `TC-INT-012` assert backend phát thông báo từ **sự kiện domain** (§7.1) khi gửi báo cáo. ⚠️ Resource `notifications` thuộc **MF10 (Mạnh)** ⇒ MF12 chỉ assert **event được phát**, logic gửi test ở MF10. |
| **L29** | CF-13 | **BR-015 không có mục canonical riêng** trong `business_rules.md` (chỉ được BR-006 dẫn chiếu *"không trùng 3 mật khẩu gần nhất (BR-015)"*) — trùng CF-1 của `TDS-MF01` | Cột `noReuseLast3` test như **hiện thực của mệnh đề trong BR-006**; `Oracle Source` ghi **BR-006**, **không** ghi BR-015. Chờ Huy bổ sung mục BR-015 hoặc xác nhận là dẫn chiếu nội bộ. |

**Cách xử lý mâu thuẫn trong test (policy — giống MF05/MF07):** nơi nào 2 nguồn nói khác nhau và chưa có người chốt → test **hành vi khoan dung** + `@Disabled("Chờ CF-x")` cho hành vi nghiêm ngặt. **Không** khoá một bên rồi coi là chuẩn.
**Ngoại lệ:** **L1–L4** đã có người chốt (`00_Common_Conventions.md` Approved bởi Nam) ⇒ test khoá theo D1–D7.

### 2.3. Nhóm D — **CODE ĐÃ MERGE ghi đè TDS** *(thêm 2026-08-04, kết quả Bước 0)*

> **Nhóm này khác ba nhóm trên ở một điểm quyết định: không có gì để "chờ chốt".** L1–L4 là tài liệu ghi đè tài liệu; L5–L29 là hai nguồn nói khác nhau. **L30–L36 là tài liệu nói khác với `04_Development/Backend` đang chạy** — và theo `00_Team_Work_Assignment_Plan.md` §2c-2 nguồn `-1`: *"tài liệu mô tả một đằng mà code làm một nẻo ⇒ **CODE ĐÚNG**, sửa tài liệu."*
>
> Bảy mục dưới đây **đều làm build đỏ / Flyway đỏ / app không khởi động** nếu Bước 2 sinh code theo TDS v1.1. Chúng không phải rủi ro tiềm tàng — chúng là lỗi chắc chắn xảy ra ở lần chạy đầu tiên.

| # | Quyết định | TDS/TestSpec v1.0 viết | Code đã merge (đo 2026-08-04) | Hành vi được TEST | TC |
|---|---|---|---|---|---|
| **L30** | `M12-D01` | `CREATE TABLE audit_logs (id VARCHAR(32) …, actor_label VARCHAR(160), action VARCHAR(500))` | `V1:278` — `id UUID`, `code VARCHAR(32)`, **`actor VARCHAR(180)`**, `action VARCHAR(255)`; entity `iam/domain/AuditLog extends BaseEntity`; `iam/service/AuditService` đang ghi | `V22` chỉ **`ALTER ADD`** 3 cột (`actor_user_id`, `prev_hash`, `entry_hash`, **đều nullable**). Assert DB dùng cột **`actor`**, không phải `actor_label`. Assert `V22` **không chứa** chuỗi `CREATE TABLE audit_logs` | `TC-INT-003`, `TC-CON-009` |
| **L31** | `M12-D02` | MF12 sở hữu `checklist_groups`/`checklist_items` + 4 endpoint `/checklist-groups` | `V12:209/223` + `mf07/web/ChecklistGroupController` (`GET`·`POST`·`PUT /{code}`·`DELETE /{code}`, `MEDICAL_STAFF`) **đã merge** | **Gỡ khỏi phạm vi test MF12.** `TC-017`/`TC-018`/`TC-INT-011`/`TC-SEC-011` → `@Disabled("M12-D02 — thuộc MF07")`. Thêm assert **phòng vệ**: `V22` không tạo 2 bảng đó, và context Spring khởi động được (2 `@RequestMapping` cùng path ⇒ fail ngay ở `@SpringBootTest`) | `TC-CON-009` |
| **L32** | `M12-D06` | `POST /reports/{compliance,violations,incidents}/send` | `TDS-MF13` sở hữu root `/api/v1/reports` (10 path, có `{slug}` **biến**) | Path đổi sang **`POST /sent-reports/{compliance,violations,incidents}`**. Cả 3 chưa có call-site FE ⇒ **0 công sửa màn**. Assert đúng path mới trong `TC-021`/`TC-022`/`TC-023` | `TC-021`…`TC-023` |
| **L33** | `M12-D08`/`M12-D09` | MF12 khai `AuditLogPort` **và** `AiAssistPort` | `iam/service/AuditService` (11 module đang gọi) và **`com.sfms.mf05.port.AiAssistPort`** (MF05 khai hộ, `MenuRecommendationServiceImpl` inject `Optional<>`) **đều đã merge** | **Không khai interface mới.** Test bean adapter: `MF12` cung cấp `@Component implements com.sfms.mf05.port.AiAssistPort` ⇒ `Optional` của MF05 **có giá trị** ⇒ `POST /menus/{code}/ai-suggest` **thôi trả `503 MENU-073`**. Đây là **TC liên module thật**, đo được | `TC-INT-022` *(mới)* |
| **L34** | `M12-D10` | Tiền tố mã lỗi `SYS-` | `ModuleCodeResolver` — comment đặt chỗ *"(**AIS**, RPT)"*; `audit-logs`→`IAM`, `security-policies`→`IAM` **đã đăng ký** (dòng 32–33) | Mọi assert `$.error.code` đổi `SYS-` → **`AIS-`**, **trừ** 2 resource của MF01 giữ **`IAM-`** (`IAM-162`, `IAM-184`, `IAM-404`). Resolver tra theo **segment đầu của path** ⇒ assert `SYS-162` là assert một hành vi **không thể xảy ra** | mọi TC error path |
| **L35** | `M12-D13` | *"DB `REVOKE UPDATE, DELETE` cho role `sfms_app`"* — có trong ADR-M12-02, §11.1, §13 IT-12-03, §14.1 | Role **`sfms_app` không tồn tại** (kiểm `V1`/`V2` + `application.yml`) | Bỏ mọi assert dựa trên `GRANT`/`\dp`. Thay bằng 3 lớp kiểm được: `@Immutable` · 405 `IAM-162` · **chuỗi hash** (`integrity.valid=false` sau khi sửa thẳng SQL). Lớp thứ ba **mạnh hơn** `REVOKE` — nó bắt được cả DBA | `TC-SEC-019`, `TC-INT-003` |
| **L36** | `M12-D12` | Migration `V22`/`V23` | `ls db/migration/` — **V1–V21 đã dùng**, MF11 giữ V20/V21 | `V22`/`V23`. Assert tên file trong `TC-CON-009`; và **chạy lại `ls` trước khi đặt file** — số này lạc hậu nhanh, chính TestSpec v1.0 là nạn nhân | `TC-CON-009` |

> **Vì sao nhóm D đáng có mục riêng thay vì trộn vào Nhóm A.** Nhóm A (`L1–L4`) là **quy ước** ghi đè: TDS viết `VARCHAR(32)` PK, D1 nói `UUID` — cả hai đều là tài liệu, và người viết TDS *có thể* đã đọc đúng rồi diễn giải khác. Nhóm D là **sự thật đã biên dịch được**: `mf07/web/ChecklistGroupController.java` tồn tại hay không tồn tại, không có chỗ cho diễn giải.
>
> Hệ quả cho người review: **Nhóm A tranh luận được, Nhóm D thì không.** Nếu reviewer thấy một mục L30–L36 đáng ngờ, cách phản bác duy nhất là **chạy lại lệnh** ở §2.1 Bước 0 — không phải đọc lại TDS.
>
> **Và đây là lớp lỗi TestSpec v1.0 không thể tự bắt được**, dù nó đã làm rất tốt Nhóm A/B/C: cả 29 mục L1–L29 đều rút ra từ *đối chiếu tài liệu với tài liệu*. `checklist_groups` bị MF07 chiếm mất là chuyện xảy ra **sau khi** TestSpec này viết xong (30/07 → `M07-D10` ngày 03/08). Không có Bước 0 thì nó chỉ lộ ra lúc `mvn verify` của Bước 2, sau khi đã sinh xong một controller vô dụng.

---

## 3. Test Design Specification

### TDS-01 — Scope / Phạm vi

```
MF12 (com.sfms.mf12) — các layer được kiểm thử:
├── ai/          AiConversation, AiRequest + AiChatService · AiArchiveService
│                GeminiClient (stub dev) · AiAssistPort (cổng AI duy nhất — ADR-M12-01)
├── audit/       AuditLog (@Immutable) + AuditLogService · AuditLogQueryService
│                AuditLogPort (13 module dùng chung) · hash chain (ADR-M12-02)
├── integration/ ExternalIntegration + IntegrationService · CredentialCipher (AES-256-GCM)
├── policy/      SecurityPolicy (bảng do MF01 sở hữu — L10) + SecurityPolicyService
├── checklist/   ChecklistGroup/Item + ChecklistAdminService (soft-delete)
├── report/      SentReport (JSONB payload) + ComplianceReportSendService
│                ViolationReportSendService · SentReportQueryService · ReportExporter
├── metrics/     SystemStatusService · SystemMetricsService (Actuator — ADR-M12-04)
│                → UNIT TEST: Mockito mock repository + GeminiClient + Actuator endpoints
├── web/         6 controller
│                → SECURITY TEST: MockMvc + JWT giả lập (7 role)
└── integration/ @SpringBootTest + Testcontainers PostgreSQL 16 + Flyway V22/V23
                 → API → DB, hash chain (M12-D13), file export
```

**NGOÀI phạm vi (test ở module khác, chỉ **stub/seed** ở đây):**

| Thành phần | Chủ | Cách xử lý trong test MF12 |
|---|---|---|
| JWT/RBAC filter, `users` | MF01 (Nam) | JWT giả lập; **không** test thuật toán token |
| **Bảng + seed `security_policies`** | **MF01 (Nam)** — L10/CF-6 | Seed `SEC-DEFAULT` qua `V1`/`V2`; `TC-INT-013` assert **MF12 migration KHÔNG tạo lại bảng** |
| Áp dụng chính sách lúc login (BR-002/004/006) | MF01 | `TC-INT-014` chỉ assert **event `SecurityPolicyUpdated` được phát** + hiệu lực end-to-end; logic khoá tài khoản test ở MF01 |
| `schools` | MF02 (Nam) | Seed `SCH-001`, `SCH-002` cho test scope checklist (L23) |
| **Gemini AI** | Bên ngoài | `GeminiClient` **fake** theo ADR-000 (*"trả mock JSON đúng schema"*); `FX-024` fake delay 11s |
| **FCM / SMTP** | Bên ngoài | Fake port; `TC-006`…`TC-008` chỉ test **handshake có được gọi**, không test giao thức thật |
| `notifications` (gửi thông báo) | **MF10 (Mạnh)** — L28/CF-29 | MF12 assert **event được phát**; logic gửi/retry/giờ yên tĩnh (BR-158/159) test ở MF10 |
| **Chấm điểm checklist** (UC-014) | **MF07 (Thảo)** — L7/CF-3 | MF12 test **CRUD + soft-delete**; chấm điểm test ở `MF07-TC-011`. ⚠️ `TC-INT-011` phải khớp `MF07-TC-INT-010` |
| Dữ liệu nguồn báo cáo (`KitchenInspection`, `ViolationRecord`) | **MF07** | Seed trực tiếp bảng để tính `avgScore`/`counts`; **không** qua API MF07 |
| Nội dung mẫu báo cáo | BR-178 | `ReportExporter` test **thật** (PDFBox + POI) — bản in phải có 4 thành phần bắt buộc |

### TDS-02 — Test Basis / Cơ sở Kiểm thử

| Source | Items Derived |
|--------|--------------|
| `traceability_matrix.md` **§1.12** | 15 UC + tập BR/MSG hợp lệ (nguồn CHUẨN — không nhận mã ngoài bảng này) |
| `MF12_AI_Supporting_Services.md` (UC file) | Normal/Alternative Flow. ⚠️ **UC-040 AF1** (test connection fail) là oracle của `TC-008`; UC-106 bước 4 (filter) của `TC-INT-008`; UC-111 bước 5 (detail lỗi) của `TC-INT-007`; UC-184 bước 4 (validate) của `TC-015`; UC-012 AF1 (AI lỗi) của `TC-003` |
| `business_rules.md` §14 | **BR-157** archive **toàn bộ** hội thoại · **BR-162** audit **bất biến** · **BR-163** kiểm kết nối **trước khi lưu** · **BR-164** **mã hoá** credential · **BR-165** báo cáo sức khỏe hệ thống **hàng tuần** |
| `business_rules.md` §15 | **BR-170** audit kèm actor/thời gian/đối tượng · **BR-171** chính sách retention · **BR-172** **bảo vệ toàn vẹn** nhật ký · **BR-174** truy vấn lịch sử vận hành |
| `business_rules.md` §1 | **BR-002** 5 lần sai → khoá 15 phút · **BR-004** (v1.9) token 30 phút + refresh 14 ngày · **BR-006** chính sách độ mạnh mật khẩu (+ dẫn chiếu BR-015 — L29) |
| `business_rules.md` §12 | **BR-209** báo cáo tuân thủ (kỳ rỗng → **chặn**, MSG-092) · **BR-210** checklist theo **từng trường**, xoá = ẩn, **không cần phê duyệt** |
| `business_rules.md` §16 | **BR-175** quyền xem báo cáo theo vai trò · **BR-177** lưu ≥ **12 tháng** + audit trail mỗi lần xuất · **BR-178** xuất **PDF + XLSX** kèm tiêu đề/kỳ/tên trường/timestamp · **BR-181** **không lộ danh tính HS**, đánh dấu "Confidential" · **BR-213** *(deprecated)* · **BR-214** báo cáo vi phạm (kỳ rỗng → chặn, MSG-097) |
| `business_rules.md` §13 | **BR-158** giờ yên tĩnh 22:00–06:00 · **BR-159** retry push → email dự phòng *(test ở MF10 — L28)* |
| `error_message.md` | Chuỗi assert nguyên văn: MSG-004, MSG-005, MSG-025, MSG-029, MSG-061, MSG-068, MSG-069, MSG-070, MSG-073, MSG-091, MSG-092, MSG-094, MSG-096, MSG-097, MSG-098, MSG-099 |
| `TDS-MF12_Backend.md` §10 | Error code: `AIS-004/005/025/061/068/069/070/073/092/094/096/097/157/162/163/164/184/188` |
| `TDS-MF12_Backend.md` §16 | Ma trận 7 role × 15 nhóm endpoint **+ bảng field-level 4 nhóm** + 2 điểm không khớp FE (CF-20, CF-21) → 18 security TC |
| `TDS-MF12_Backend.md` §17.1 | C1–C14 → mỗi constraint ≥ 1 TC |
| **`00_Common_Conventions.md` D1–D7** | Định danh, envelope, path, ISO ngày, id client, enum chung, ngôn ngữ message → L1–L4 + `TC-026`, `TC-027`, `TC-INT-021` |
| `TDS-MF12_Backend.md` §14.1/§14.2 | Truy vấn kiểm chứng bắt buộc: ~~GRANT của `sfms_app`~~ → **chuỗi hash `audit_logs`** (`M12-D13`), credential không lộ trong log, nội dung AI không ra file log, báo cáo không chứa mã HS, audit đủ actor/IP |
| `00_Team_Work_Assignment_Plan.md` §2b | Shape 5 field · clamp `page` · tập rỗng vẫn 200 · `search` chỉ trên `SEARCH_FIELDS` · `dateFrom`/`dateTo` |
| `ADR-000.md` §Mock Strategy | Dev = stub: Gemini/FCM/SMTP trả mock JSON **đúng schema contract** |
| **PDPD (Luật 91/2025)** | BR-173 (không log hội thoại), BR-181 (báo cáo không lộ HS), BR-164 (credential) → `TC-SEC-006`…`TC-SEC-009` |

### TDS-03 — Test Conditions and Coverage Items

| Condition ID | Test Condition | Coverage Item | Constraint | Test Cases |
|-------------|---------------|---------------|-----------|-----------|
| `COND-01` | Chat AI thành công **phải** sinh 1 dòng `ai_requests` trong cùng transaction | `AiChatService.ask()` + `AiArchiveService` | C1 | `TC-001` |
| `COND-02` | Archive lỗi ⇒ **rollback**, KHÔNG trả phản hồi chưa lưu vết | `AiArchiveService` | C1 | `TC-002` |
| `COND-03` | AI timeout 10s → MSG-073 **và vẫn** ghi dòng `status=TIMEOUT` | `GeminiClient` + archive | C1 | `TC-003` |
| `COND-04` | Prompt gửi AI **redact** mã/tên học sinh | `PromptRedactor` | C1 | `TC-004` |
| `COND-05` | User chỉ xem hội thoại **của mình** | `AiChatService.history()` | C1 | `TC-005`, `TC-SEC-005` |
| `COND-06` | `save()` integration chỉ khi test-connection **PASS ≤ 10 phút** | `IntegrationService.save()` | C4 | `TC-006`, `TC-007` |
| `COND-07` | Test connection FAIL ⇒ không lưu credential, `last_test_passed=false` | `testConnection()` | C4 | `TC-008` |
| `COND-08` | Credential **không** lộ qua API — chỉ `credentialMasked` | `IntegrationMapper` | C3 | `TC-009`, `TC-SEC-006` |
| `COND-09` | Mã hoá/giải mã AES-256-GCM round-trip; sai `key_version` → `AIS-164` | `CredentialCipher` | C3 | `TC-010`, `TC-011` |
| `COND-10` | `audit_logs` hash chuỗi: `entryHash(n) = SHA256(entryHash(n−1) ‖ nội dung)` | `AuditLogService` | C2 | `TC-012` |
| `COND-11` | Phá vỡ chuỗi hash **bị phát hiện** kèm `brokenAt` | `verifyChain()` | C2 | `TC-012b`, `TC-INT-009` |
| `COND-12` | `audit_logs` **không sửa được** ở tầng entity + dữ liệu + API | `@Immutable` + **chuỗi hash BR-172** + không expose `PUT`/`DELETE` *(~~REVOKE~~ gỡ — `M12-D13`)* | C2 | `TC-013`, `TC-INT-003`, `TC-INT-004`, **`TC-SEC-019`** |
| `COND-13` | Ghi audit lỗi ⇒ **fail transaction nghiệp vụ** | `AuditLogPort` | C2 | `TC-014` |
| `COND-14` | Chính sách bảo mật ngoài khoảng → `IAM-184` kèm `details.allowed` | `SecurityPolicyService.update()` | — (UC-184 bước 4) | `TC-015` |
| `COND-15` | `PUT` partial merge: field vắng ⇒ **giữ giá trị cũ** (`requireMixedCase`) | `SecurityPolicyService` | C10 | `TC-016`, `TC-INT-013` |
| `COND-16` | Checklist xoá = **ẩn**; đã tham chiếu → MSG-094 chờ `confirmed=true` | `ChecklistAdminService.delete()` | C5 | `TC-018`, `TC-019`, `TC-INT-011` |
| `COND-17` | `school_id` checklist **luôn từ JWT**, không nhận từ body | `ChecklistAdminService` | C12 | `TC-017`, `TC-SEC-011` |
| `COND-18` | Báo cáo tuân thủ kỳ **rỗng** → chặn `AIS-092` | `ComplianceReportSendService` | C6 | `TC-021`, `TC-SEC-014` |
| `COND-19` | Báo cáo vi phạm kỳ **rỗng** → chặn `AIS-097` | `ViolationReportSendService` | C6 | `TC-022` |
| `COND-20` | UC-188 deprecated → **409 `AIS-188`** ở **cả 2 đường** | 2 endpoint | C7 | `TC-023` |
| `COND-21` | Snapshot báo cáo **bất biến** — detail đọc lại số cũ | `SentReportQueryService` | C6 | `TC-024` |
| `COND-22` | Báo cáo **không lộ danh tính HS**; `confidential=true` khi nhạy cảm | `ReportExporter` | C6 | `TC-025`, `TC-SEC-008` |
| `COND-23` | Xuất **cả PDF và XLSX** kèm tiêu đề/kỳ/tên trường/timestamp | `ReportExporter` | C6 | `TC-INT-015` |
| `COND-24` | JSON ngày **ISO-8601** (**D4** ghi đè C13) | mapper | **D4**, L3 | `TC-026` |
| `COND-25` | `period` parse case-insensitive, trả `"Weekly"` (`ReportPeriod` ngoài D6) | mapper | C13, CF-27 | `TC-027` |
| `COND-26` | Metric từ **Actuator**, KHÔNG bảng `system_metrics` | `SystemMetricsService` | C9 | `TC-INT-018` |
| `COND-27` | Báo cáo sức khỏe hệ thống **hàng tuần** tự động (BR-165) | `@Scheduled` job | C9 | `TC-020` |
| `COND-28` | `GET /error-logs` **không lược** `stackTrace` + `sessionInfo` | `ErrorLogQueryService` | C14 | `TC-INT-007` |
| `COND-29` | Một nguồn duy nhất cho trạng thái tích hợp | `external_integrations` | — (L14/CF-10) | `TC-INT-016` |
| `COND-30` | JSON `id` = `code`, PK UUID; `external_integrations` `UNIQUE(code)` | entity | **D1**, L1, L11 | `TC-INT-021` |
| `COND-31` | Lỗi trong envelope `{"error":{...}}`; `details` là mảng | `@RestControllerAdvice` | **D2**, L2 | mọi TC error path |
| `COND-32` | Nhận `id` client, trùng → 409 | `create()` | **D5** | `TC-028` |
| `COND-33` | `RoleKey` **kebab-case** trong thống kê UC-150 | `SystemMetricsService.usage()` | **D6** | `TC-029` |
| `COND-34` | 6 mã `AIS-*`/`IAM-*` tiếng Việt: assert **code**, không assert chuỗi | `@RestControllerAdvice` | **D7**, L4 | `TC-002`, `TC-006`, `TC-015`, `TC-023` |
| `COND-35` | Hợp đồng phân trang 5 field, clamp `page`, rỗng vẫn 200 | `PagedResponse.of()` | `00_` §2b | `TC-INT-005`, `TC-INT-006` |
| `COND-36` | `search` chỉ trên `SEARCH_FIELDS`; filter `ip`, `dateFrom/dateTo` | `AuditLogSpec` | `00_` §2b, L19 | `TC-INT-007b`, `TC-INT-008` |
| `COND-37` | Không JWT → 401; sai role → 403 toàn bộ endpoint | filter chain | §16 | `TC-SEC-001`…`TC-SEC-004` |
| `COND-38` | Credential **không** xuất hiện trong log lẫn response | logger + mapper | C3, BR-164 | `TC-SEC-006` |
| `COND-39` | Nội dung hội thoại AI **không** ghi ra file log (BR-173) | logger | PDPD | `TC-SEC-007` |
| `COND-40` | Rate limit AI **20/min/user** | rate limiter | §9.2 #21 | `TC-SEC-010` |
| `COND-41` | Actuator **không** lộ ra ngoài | Spring Security | — (§13 SEC-12-09) | `TC-SEC-012` |
| `COND-42` | `GET /public/password-policy` trả **tập con không nhạy cảm** | `PublicPolicyController` | L21/CF-20 | `TC-SEC-013` |
| `COND-43` | Ma trận 7 role × 31 endpoint khớp §16 | toàn bộ controller | §16 | `TC-SEC-015` |
| `COND-44` | Audit ghi đủ actor + IP cho mọi thao tác ghi (BR-170) | audit aspect | C2 | `TC-INT-010` |
| `COND-45` | Retention ≥ 12 tháng (BR-171/BR-177) | cấu hình + query | BR-171 | `TC-INT-014b` |
| `COND-46` | Hiệu lực liên module: MF12 sửa policy → MF01 áp | event | C8 | `TC-INT-014` |
| `COND-47` | MF12 migration **KHÔNG** tạo lại **4** bảng của module khác: `security_policies`, **`audit_logs`**, **`checklist_groups`**, **`checklist_items`** | `V22` | C8, L10, **L30/L31** | `TC-INT-013b`, **`TC-CON-009`** |
| `COND-48` | MF05 gọi AI qua port → sinh `ai_requests` `module='MF05_MENU'` | `AiAssistPort` | C1 | `TC-INT-002` |
| `COND-49` | UC-187 hub chỉ gom UC-185 + UC-189 (không UC-188) | `ReportCenterService` | C7 | `TC-INT-019` |
| `COND-50` | `sent_reports` JSONB dung nạp field ngoài interface, không mất dữ liệu cũ | `payload` | C6, L15, L26 | `TC-INT-020` |

**Tổng:** 50 condition → 73 test case. Mỗi condition có ≥ 1 TC ⇒ **không có condition mồ côi** (kiểm ở §6).

### TDS-04 — Test Techniques / Kỹ thuật Kiểm thử

| Technique (ISO 29119-4) | Applied To | Rationale |
|------------------------|------------|-----------|
| **Boundary Value Analysis** | `test-connection` tuổi ∈ {9m59s, **10m**, 10m01s} · `maxLoginAttempts` ∈ {2, **3**, 10, **11**} · `sessionTimeoutMinutes`/`passwordMinLength` biên · AI timeout ∈ {9.9s, **10s**, 11s} · rate limit ∈ {20, **21**} · retention ∈ {11, **12**, 13} tháng | BR-163 nói "chưa quá 10 phút", UC-184 validate khoảng, §9.2 ghi `20/min/user`. Off-by-one ở cửa sổ 10 phút cho phép lưu cấu hình bằng một lần test đã cũ |
| **Equivalence Partitioning** | Role: {SysAdmin} = quản trị hệ thống · {Medical} = checklist + báo cáo · {mọi role} = AI chat · {Caterer, Teacher, Parent, Supplier} = chỉ AI chat · Trạng thái integration: {chưa test} / {test PASS còn hiệu lực} / {test PASS đã cũ} / {test FAIL} | §16 chia miền truy cập rõ. Đặc biệt **UC-012 actor là "User (mọi role)"** — dễ bị over-block thành "chỉ admin" |
| **State Transition Testing** | `external_integrations`: `NOT_TESTED → TESTED_PASS → SAVED`, và `TESTED_PASS` **hết hiệu lực sau 10 phút** → về `NOT_TESTED` · `checklist_item`: `ACTIVE → INACTIVE` (một chiều, không xoá) · `sent_reports`: tạo → **bất biến** | §6.4 TDS có state machine cho cả hai; ADR-M12-05/06 làm bản ghi bất biến |
| **Decision Table Testing** | Phân quyền **theo FIELD** (§16, 4 nhóm) · `save()` integration: (đã test? × PASS? × tuổi ≤10m?) → allow/409 · gửi báo cáo: (loại × kỳ có dữ liệu? × deprecated?) → 200/422/409 | `save()` có 3 biến đầu vào; gửi báo cáo có 3 loại × 2 trạng thái kỳ. Bảng quyết định là cách duy nhất chứng minh không sót ô |
| **Error Guessing** | Bỏ JWT · gọi thẳng API để lách gate kỳ rỗng (nút bị disable — L22) · `PUT /audit-logs` qua generic handler (mock cho phép — L13) · đọc conversation của người khác · sửa `audit_logs` bằng SQL ở tầng DBA · `GET /actuator/env` · gửi `schoolId` khác trong body checklist (L23) | Các lỗ hổng này **đều đã hiện diện** trong mock (L13, L16, L21, L22, L23), không phải giả thuyết |
| **Cryptographic / Integrity Testing** | AES-256-GCM round-trip, sai `key_version` → `AIS-164` · hash chain `entry_hash`/`prev_hash` phát hiện sửa 1 dòng giữa chuỗi | BR-164 + BR-172 là **yêu cầu mật mã**, không test được bằng assert nghiệp vụ thường. Phá vỡ chuỗi giữa (không phải cuối) là ca dễ sót nhất |
| ~~Privilege / GRANT Testing~~ → **Tamper-detection Testing** | Sửa/xoá thẳng bằng SQL rồi assert `GET /audit-logs/integrity` trả `valid=false` + `brokenAt` đúng dòng | ⛔ **`M12-D13`** — role `sfms_app` không tồn tại ⇒ GRANT testing test một thứ không có. Chuỗi hash **mạnh hơn**: `REVOKE` chỉ chặn ứng dụng, hash bắt được **cả người có quyền DB**. Nhận định *"chặn ở service là chưa đủ"* của v1.0 vẫn đúng — chỉ chọn sai cơ chế |
| **Use Case Testing** | E2E-01 vòng đời AI (MF05 dùng AI → audit → UC-119) · E2E-02 vòng đời checklist → báo cáo (UC-186 → UC-014 MF07 → UC-185 → thông báo → UC-190) | 2 chuỗi liên module trong §13 TDS; chứng minh MF12 thật sự là nền chung |
| **Contract Testing** | Shape response vs `mock-server.ts` + 24 tên field bất biến (C13) + shape phân trang + union `sent_reports` | Đổi tên field là 16 file FE gãy; §14.3 TDS đã đặt sẵn lệnh kiểm |
| **Data-driven (Parameterized)** | Ma trận 7 role × 31 endpoint (§16) · 4 dịch vụ integration × 4 trạng thái test · 3 loại báo cáo × 2 trạng thái kỳ | 217 tổ hợp cho ma trận role — viết tay bất khả thi |

### TDS-05 — Test Data Requirements

> **Nguồn fixture: `mock-data.ts` — dùng ID THẬT của seed.** Trường QA = `SCH-001`.
> Fixture dựng bằng **Java builder** trong `Mf12TestFixtures.java`; seed DB bằng `@Sql` hoặc Flyway test migration.
> ⚠️ Theo **D1**, các giá trị `AUD-…`/`ERR-…`/`RPTS-…`/`SEC-…`/`gps|gemini` là **`code`**, không phải PK.
> ⚠️ **TUYỆT ĐỐI KHÔNG dùng credential thật** — mọi khoá trong fixture là chuỗi test (`FX-026`).

| Fixture ID | Type | Value / Logic | Mục đích |
|-----------|------|---------------|---------|
| `FX-001` | DB seed `audit_logs` | `code="AUD-99120"` — `timestamp=2026-07-02T03:18:44Z`, `actor="hungtv@anbinhcatering.vn"`, `ip="10.20.1.44"`, `action="CREATE FoodSample SMP-20260702-02"`, `resource="Food Sample Retention"`, `result="SUCCESS"` | Dòng audit chuẩn — nguồn của `TC-013`, `TC-INT-003`, `TC-INT-004` |
| `FX-002` | DB seed `audit_logs` | `code="AUD-99119"` — `action="SUBMIT Inspection Step 2 INSP-20260702-L"`, cùng actor/IP với FX-001 | Dòng thứ 2 của chuỗi hash (`TC-012`) |
| `FX-003` | DB seed `audit_logs` | `code="AUD-99101"` — `actor="toanpq@dtd.edu.vn"`, `ip="10.20.3.12"`, `action="LOGIN"`, `resource="Authentication"`, `result="SUCCESS"` | Dòng thứ 3 — kiểm hash chain 3 dòng liên tiếp (`TC-012`) |
| `FX-004` | DB seed `audit_logs` | `code="AUD-99087"` — `ip="203.113.99.87"`, `result="BLOCKED"`, `action` chứa *"LOGIN (5 lần thất bại — khóa 15 phút, BR-002)"* | **Oracle của filter theo IP** (UC-106 bước 4) — `TC-INT-008` |
| `FX-005` | DB seed `error_logs` | `code="ERR-5521"` — `message="Gemini AI request timeout sau 10s"`, `severity=HIGH`, `module="MenuRecommendationService"`, `path="/api/menus/recommendations"`, có **`stackTrace`** + **`sessionInfo`** | Oracle của `TC-INT-007` (không lược field) — và là **bằng chứng timeout 10s** khớp `TC-003` |
| `FX-006` | DB seed `error_logs` | `code="ERR-5518"` — `severity=MEDIUM`, `module="AuthService"` · `ERR-5515` — `severity=LOW`, `module="NotificationService"` | Phân trang + filter `severity` |
| `FX-007` | DB seed `external_integrations` | `code="gemini"` — `label="Gemini AI Service"`, `endpoint="https://generativelanguage.googleapis.com/v1beta"`, **`connected=false`**, `statusNote="Chưa kết nối"` | Dịch vụ **chưa kết nối** — nguồn của `TC-006` (save khi chưa test) |
| `FX-008` | DB seed `external_integrations` | `code="gps"` — `connected=true`, `lastTestedAt` gần đây · `code="gmail"` · `code="firebase"` — cả 3 `connected=true` | 4 dịch vụ đủ theo mock (không có `school_id` — **L11**) |
| `FX-009` | DB row | `external_integrations` với `last_test_passed=true`, `last_tested_at = now − 9m` | Biên **trong** cửa sổ 10 phút → `save()` PASS (`TC-007`) |
| `FX-010` | DB row | như FX-009 nhưng `last_tested_at = now − 11m` | Biên **ngoài** cửa sổ → `AIS-163` (`TC-007`) |
| `FX-011` | DB seed `security_policies` | `code="SEC-DEFAULT"` — `maxLoginAttempts=5`, `lockoutMinutes=15`, `sessionTimeoutMinutes=30`, `passwordMinLength=8`, `requireSpecialChar=true`, `requireNumber=true`, **`requireMixedCase=true`**, `noReuseLast3=true`, `updatedBy="Nguyễn Hải Đăng (System Admin)"` | ⚠️ Seed do **MF01** giữ (L10). Oracle của `TC-015`, `TC-016` (partial merge `requireMixedCase`), `TC-SEC-013` |
| `FX-012` | DB seed `checklist_groups`/`items` | `CLG-1`…`CLG-4` + items `CLI-1-1`, `CLI-1-2`, `CLI-2-1`, `CLI-2-2`, `CLI-3-8`, `CLI-4-6` kèm `legalNote` — **`school_id = SCH-001`** | CRUD + soft-delete (`TC-018`, `TC-019`); ⚠️ **cùng fixture với `MF07-FX-012`** — hai TestSpec phải khớp (L7) |
| `FX-013` | DB seed `checklist_groups` | Bản sao `CLG-1` với **`school_id = SCH-002`** | Test scope theo trường (L23) — `TC-017`, `TC-SEC-011` |
| `FX-014` | DB seed `checklist_item_results` | Kết quả trỏ `CLI-3-8` (từ 1 phiên kiểm bếp đã lưu của MF07) | Xoá mục **đã tham chiếu** → MSG-094 (`TC-019`) |
| `FX-015` | DB seed `sent_reports` | `code="RPTS-2026-001"` — `reportType=COMPLIANCE`, `period="Weekly"`, `rangeLabel="Thứ 2 06/07/2026 – Chủ nhật 12/07/2026"`, `sentAt=2026-07-12T09:40:00Z`, `sentBy="Ngô Thị Hà (Y tế)"`, `recipient="School Administration"`, payload: **`avgScore=90`**, `threeStepSummary={done:5,total:5}`, `failedSessions=[]`, `staffHealthValid=true` | **Snapshot bất biến** (`TC-024`); oracle của `TC-INT-020` (JSONB giữ nguyên union) |
| `FX-016` | DB seed `kitchen_inspections` (MF07) | 5 phiên trong kỳ 06–12/07/2026, điểm trung bình đúng **90** | Nguồn dữ liệu để `TC-INT-014`* tính lại `avgScore` khớp `FX-015` |
| `FX-017` | DB — **rỗng** | Kỳ **không có** phiên kiểm bếp nào | Kỳ rỗng → `AIS-092` (`TC-021`) |
| `FX-018` | DB — **rỗng** | Kỳ **không có** `violation_records` nào | Kỳ rỗng → `AIS-097` (`TC-022`) |
| `FX-019` | DB seed `violation_records` (MF07) | 3 biên bản trong kỳ, `bySeverity={LOW:1,MEDIUM:2}` | Báo cáo vi phạm có dữ liệu (`TC-022`); ⚠️ `staffCount` phải ra **`0`** (L26) |
| `FX-020` | DB seed `ai_conversations`/`ai_requests` | Conversation của `user=caterer-001` với 2 `ai_requests` `status=SUCCESS`, `module='MF05_MENU'` | UC-119 lịch sử (`TC-005`); `TC-INT-002` |
| `FX-021` | DB seed `ai_conversations` | Conversation của `user=medical-001` | Cross-user → `AIS-005` (`TC-005`, `TC-SEC-005`) |
| `FX-022` | Fake port | `GeminiClient` fake — trả JSON **đúng schema ADR-000** sau 50ms | Happy path AI (`TC-001`) |
| `FX-023` | Fake port | `GeminiClient` fake — ném lỗi 401/kết nối | `TC-008` (test-connection FAIL) |
| `FX-024` | Fake port | `GeminiClient` fake — **delay 11s** (> timeout 10s) | `TC-003` → `AIS-073` + dòng `status=TIMEOUT` |
| `FX-025` | Mock repo | `AiRequestRepository` **ném lỗi khi INSERT** | `TC-002` → `AIS-157` + rollback |
| `FX-026` | env (**test only**) | `SFMS_CRED_MASTER_KEY=dGVzdC1rZXktMzItYnl0ZXMtZm9yLWFlcy1nY20=` (32 byte base64, **chuỗi test**), `SFMS_CRED_KEY_VERSION=1` · biến thể `key_version=2` không có khoá | `TC-010` round-trip · `TC-011` sai `key_version` → `AIS-164`. ⚠️ **Không** dùng khoá thật |
| `FX-027` | Payload | `apiKey = "AIzaTEST-not-a-real-key-000000000000000"` | `TC-009`/`TC-SEC-006` — chuỗi có tiền tố `AIza` để test grep phát hiện lộ khoá |
| `FX-028` | Prompt | `"Học sinh HS-3A-001 Nguyễn Minh Khang bị dị ứng tôm, gợi ý món thay thế"` | `TC-004` redact — phải **không còn** `HS-3A-001` lẫn tên HS trong prompt gửi đi |
| `FX-029` | JWT | `{sub:"sysadmin-001", role:"system-admin", schoolId:null}` | Actor quản trị hệ thống (`RoleKey` kebab-case theo **D6**) |
| `FX-030` | JWT | `{sub:"medical-001", role:"medical-staff", schoolId:"SCH-001"}` | Checklist + báo cáo |
| `FX-031` | JWT | `{sub:"medical-002", role:"medical-staff", schoolId:"SCH-002"}` | Cross-school checklist (L23) — `TC-SEC-011` |
| `FX-032` | JWT | `{sub:"admin-001", role:"school-admin", schoolId:"SCH-001"}` · `{role:"service-caterers"}` · `{role:"teacher"}` · `{role:"parent"}` · `{role:"supplier"}` | Đủ 7 role cho ma trận §16 (`TC-SEC-015`) |
| `FX-033` | Testcontainer | `PostgreSQLContainer("postgres:16-alpine")` + Flyway **`V1`…`V21` (toàn bộ schema đã merge)** + **`V22`/`V23`** (MF12). ⛔ **KHÔNG** tạo role `sfms_app` — `M12-D13` | Nền integration. Phải nạp **cả `V12` của MF07** để `TC-CON-009` phát hiện được va chạm `checklist_groups`; và `V22` `ALTER` bảng `audit_logs` của `V1` nên **không thể chạy `V1` một mình** |
| `FX-034` | Actuator | `/actuator/health`, `/actuator/metrics` bật ở profile test | `TC-INT-018`, `TC-SEC-012` |

**Nguyên tắc cách ly dữ liệu:** mỗi integration test `@Transactional` + rollback, **hoặc** `@Sql("/fixtures/mf12-reset.sql")`.
⚠️ **Ngoại lệ quan trọng — `audit_logs`:** ~~không thể `TRUNCATE` bằng role `sfms_app`~~ — `M12-D13`, role đó không tồn tại nên **`TRUNCATE` chạy được**. Vì vậy hàng rào giờ là **kỷ luật của người viết test**, không phải quyền của Postgres: chỉ `TRUNCATE audit_logs` trong `@BeforeEach` của **Testcontainers**, tuyệt đối không trỏ vào DB dev/thật. Trên môi trường thật, `audit_logs` là **bằng chứng vận hành — CẤM xoá** (BR-162, xem §7 R1). *(Đây là mặt trái thật của việc gỡ `REVOKE` — ghi ra thay vì giấu.)*

---

## 4. Test Case Specification

> **TC ID format:** `MF12-TC-[NNN]` (unit) · `MF12-TC-SEC-[NNN]` (security) · `MF12-TC-INT-[NNN]` (integration) · `MF12-TC-E2E-[NNN]`
> **Severity:** CRITICAL / HIGH / MEDIUM / LOW (theo CVSS) · **Status:** 🔴 Not written / 🟡 Written-failing / 🟢 Passing
> **Oracle Source:** bắt buộc — chỉ nhận BR / UC bước / MSG / **D1–D7** / ADR / hằng trong mock.
> **Assert lỗi:** theo **D2** — `$.error.code` + `$.error.message`. ⚠️ Theo **D7/L4**: với 6 mã `AIS-157/163/164/184/188/162` (hiện viết tiếng Việt, chưa có MSG chính thức) **chỉ assert `$.error.code`**, không khoá assert chuỗi.

### Fixture Isolation Boilerplate (CASE 2.0 — bản Java)

> ⭐ **CASE 2.0 Rule:** mỗi test tạo instance **mới** qua factory. Không shared mutable state (chống **AP-AI-002**).
> ⚠️ **D1:** builder set `code`, **không** set `id` (UUID do JPA sinh).

```java
package com.sfms.mf12;

import com.sfms.mf12.audit.domain.AuditLog;
import com.sfms.mf12.integration.domain.ExternalIntegration;
import com.sfms.mf12.report.domain.SentReport;
import java.time.Instant;
import java.util.function.Consumer;

/**
 * Fixture factory cho MF12 — baseline đồng bộ FX-001…FX-034 (§3 TDS-05).
 * D1: code = mã nghiệp vụ; id (UUID) do JPA sinh, KHÔNG set tay.
 * ⚠️ external_integrations KHÔNG có school_id (L11/CF-7) → UNIQUE(code).
 * ⚠️ TUYỆT ĐỐI KHÔNG đặt credential thật vào fixture (FX-026/FX-027 là chuỗi test).
 */
public final class Mf12TestFixtures {

    public static final String SCHOOL_A = "SCH-001";
    public static final String SCHOOL_B = "SCH-002";

    /** Baseline = FX-001 (dòng audit chuẩn). entryHash/prevHash do service TÍNH, không set tay. */
    public static AuditLog newAuditLog(Consumer<AuditLog>... overrides) {
        AuditLog a = new AuditLog();
        a.setCode("AUD-TEST-" + java.util.UUID.randomUUID().toString().substring(0, 6));
        a.setTimestamp(Instant.parse("2026-07-02T03:18:44Z"));   // D4: Instant, không phải chuỗi
        a.setActor("hungtv@anbinhcatering.vn");
        a.setIp("10.20.1.44");
        a.setAction("CREATE FoodSample SMP-20260702-02");
        a.setResource("Food Sample Retention");
        a.setResult("SUCCESS");
        for (Consumer<AuditLog> o : overrides) o.accept(a);
        return a;                                               // C2: hash do AuditLogService sinh
    }

    /** Baseline = FX-007 (Gemini, chưa kết nối). connected do service SUY từ lastTestPassed (C11). */
    public static ExternalIntegration newIntegration(Consumer<ExternalIntegration>... o) {
        ExternalIntegration e = new ExternalIntegration();
        e.setCode("gemini");                                     // D1 + L11: UNIQUE(code), không school_id
        e.setLabel("Gemini AI Service");
        e.setEndpoint("https://generativelanguage.googleapis.com/v1beta");
        e.setPurpose("Trợ lý AI & gợi ý thực đơn (BR-156/157)");
        e.setLastTestPassed(false);
        e.setLastTestedAt(null);
        for (Consumer<ExternalIntegration> c : o) c.accept(e);
        return e;                                                // KHÔNG set connected tay
    }

    /** Baseline = FX-015 (báo cáo tuân thủ tuần 06–12/07). payload là JSONB (ADR-M12-05). */
    public static SentReport newComplianceReport(Consumer<SentReport>... o) {
        SentReport r = new SentReport();
        r.setCode("RPTS-TEST-" + java.util.UUID.randomUUID().toString().substring(0, 6));
        r.setSchoolId(SCHOOL_A);                                 // C12: từ JWT ở luồng thật
        r.setReportType("COMPLIANCE");
        r.setPeriod("WEEKLY");                                   // DB: enum; JSON trả "Weekly" (C13/CF-27)
        r.setRangeLabel("Thứ 2 06/07/2026 – Chủ nhật 12/07/2026");
        r.setSentAt(Instant.parse("2026-07-12T09:40:00Z"));
        r.setSentBy("Ngô Thị Hà (Y tế)");
        r.setRecipient("School Administration");
        r.setPayloadJson("""
            {"avgScore":90,"threeStepSummary":{"done":5,"total":5},
             "failedSessions":[],"staffHealthValid":true}""");
        for (Consumer<SentReport> c : o) c.accept(r);
        return r;
    }
}
```

**Kiểm tự động chống shared state** (CI, §6):

```bash
grep -rnE "^\s*(private )?static\s+(final\s+)?(AuditLog|ExternalIntegration|SentReport|SecurityPolicy|ChecklistGroup)\s+" \
     src/test/java/com/sfms/mf12/ | grep -v Mf12TestFixtures.java
# Expected: no output
```

---

### 4.1. UNIT TEST CASES

> Layer `com.sfms.mf12.*`. Repository + `GeminiClient` + `NotificationPort` + Actuator = **Mockito mock/fake**. Không chạm DB.

---

#### MF12-TC-001 — Chat AI thành công PHẢI sinh 1 dòng ai_requests

**Severity:** `CRITICAL`
**Feature Under Test:** `AiChatService.ask()` + `AiArchiveService.archive()`
**Test File:** `ai/AiChatServiceTest.java`
**TDD Phase:** 🔴 RED · ⚠️ **BLOCKED bởi L6/CF-2** — UC-012 chưa có gì trong mock
**Condition Ref:** `COND-01` · **Constraint Ref:** `TDS §17.1 C1`
**Oracle Source:** **BR-157** nguyên văn — *"**Toàn bộ** hội thoại giữa người dùng và AI (gồm cả đề xuất thực đơn) phải được **lưu trữ (archive)** phục vụ tuân thủ & kiểm tra"* + `ADR-M12-01` (`AiAssistPort` là cổng AI duy nhất) + `ADR-000` §Mock Strategy (*"Gemini AI: trả mock JSON đúng schema"*) + `TDS` §13 hàng `UT-12-01`
**Kỹ thuật:** Use Case Testing

**Preconditions:** `FX-022` fake Gemini trả JSON đúng schema sau 50ms; `FX-029`/`FX-030` JWT bất kỳ role (UC-012 actor = **User, mọi role**).

**Test Steps:**
1. **Arrange:** inject `FX-022`.
2. **Act:** `service.ask(new ChatRequest("Gợi ý món cho khối 3", null), actor)`.
3. **Assert:** dòng archive + nội dung + quan hệ conversation.

**Expected Result (PASS):**
- Trả `answer` khác `null`
- Đúng **1** dòng `ai_requests` với `status = SUCCESS`, `response_text` khác `null`, `prompt` khác `null`, `module` khác `null`
- `ai_conversations` được tạo (nếu `conversationId` vắng) và `last_message_at` = thời điểm gọi
- Archive nằm **trong cùng transaction** với lời gọi — verify bằng `TransactionSynchronizationManager.isActualTransactionActive()` hoặc `@Transactional` propagation test
- `AiAssistPort` là đường duy nhất: `verify(geminiClient, times(1)).generate(any())`

**Expected Result (FAIL):**
- Không có dòng `ai_requests` ⇒ vi phạm BR-157 trực tiếp; hội thoại AI không có dấu vết tuân thủ để kiểm tra sau
- Archive ghi **sau** khi trả response (fire-and-forget) ⇒ mất bản ghi khi process chết giữa — đúng lớp lỗi mà `TC-002` chặn

**Current Status:** 🔴 Not written
**Implementation Note:** ⚠️ **Minh phải bổ sung route + resource mock** cho UC-012/UC-119 cùng lúc backend mở endpoint (`00_` §0 chống lệch). Tới đó TC này giữ trạng thái blocked, **không** tính vào coverage gate.

---

#### MF12-TC-002 — Archive lỗi PHẢI rollback, KHÔNG trả phản hồi chưa lưu vết

**Severity:** `CRITICAL`
**Feature Under Test:** `AiArchiveService` + transaction boundary
**Test File:** `ai/AiArchiveServiceTest.java`
**TDD Phase:** 🔴 RED · ⚠️ BLOCKED bởi L6
**Condition Ref:** `COND-02` · **Constraint Ref:** `TDS §17.1 C1`
**Oracle Source:** **BR-157** (*"**toàn bộ** hội thoại phải được lưu trữ"* ⇒ không có ngoại lệ nào cho phép trả phản hồi mà không lưu) + `TDS` §10 → **`AIS-157`**, HTTP **500** + `TDS` §13 hàng `UT-12-02` (*"`AIS-157`, **không** trả `answer` cho client"*)
**⚠️ D7/L4:** `AIS-157` **không có MSG nguồn** ⇒ assert **`$.error.code == "AIS-157"`**, **không** khoá assert chuỗi tiếng Việt.
**Kỹ thuật:** Error Guessing (mô phỏng lỗi tầng lưu trữ)

**Preconditions:** `FX-022` (Gemini OK) + `FX-025` (`AiRequestRepository` **ném lỗi khi INSERT**).

**Test Steps:**
1. **Arrange:** Gemini thành công, repo archive ném `DataAccessException`.
2. **Act + Assert:** `assertThatThrownBy(() -> service.ask(...))`.
3. **Assert phụ:** trạng thái DB sau lỗi.

**Expected Result (PASS):**
- Ném exception; `$.error.code == "AIS-157"`, HTTP **500**
- **KHÔNG** trả `answer` cho client (kể cả khi Gemini đã trả nội dung thành công)
- Transaction **rollback**: `ai_conversations` cũng không còn dòng mới (nếu conversation vừa được tạo trong cùng lệnh gọi)
- ⚠️ Phân biệt then chốt: đây **không** phải "lỗi AI" mà là **lỗi lưu vết** ⇒ khác `TC-003` (timeout vẫn ghi dòng)

**Expected Result (FAIL):**
- Trả `answer` kèm log warning ⇒ **có phản hồi AI tới người dùng mà hệ thống không có dấu vết** — vi phạm BR-157 và không thể kiểm tra sau; đây là ca nguy hiểm nhất của nhóm (A)
- Rollback nhưng vẫn trả 200 ⇒ client tưởng thành công, dữ liệu không có

**Current Status:** 🔴 Not written
**Implementation Note:** archive **cùng transaction**, không `@Async`, không `TransactionSynchronization.afterCommit`. Nếu cần chịu tải, dùng **outbox trong cùng transaction** thay vì fire-and-forget.

---

#### MF12-TC-003 — AI timeout 10s → AIS-073 VÀ vẫn ghi dòng status=TIMEOUT

**Severity:** `HIGH`
**Feature Under Test:** `GeminiClient` timeout + `AiArchiveService`
**Test File:** `ai/AiChatServiceTest.java`
**TDD Phase:** 🔴 RED · ⚠️ BLOCKED bởi L6
**Condition Ref:** `COND-03` · **Constraint Ref:** `TDS §17.1 C1`
**Oracle Source:** **MSG-073** nguyên văn = `"Gemini AI Service is currently unavailable. Please try again later."` + `TDS` §10 → `AIS-073`, HTTP **503** + `TDS` §13 hàng `UT-12-03` (*"`AIS-073` (MSG-073) **và** vẫn có dòng `ai_requests(status=TIMEOUT)`"*) + **BR-157** + fixture thật `FX-005` (`ERR-5521` = *"Gemini AI request timeout sau 10s"* — bằng chứng ngưỡng 10s có trong hệ thống) + UC-012 AF1
**Kỹ thuật:** Boundary Value Analysis (9.9s / **10s** / 11s)

**Preconditions:** `FX-024` fake delay 11s; `FX-026` cấu hình timeout 10s.

**Test Steps:** `@ParameterizedTest` với delay ∈ {9.9s, 10s, 11s} → `service.ask(...)`.

**Expected Result (PASS):**
- delay **9.9s** → thành công, dòng `status=SUCCESS`
- delay **11s** → `$.error.code == "AIS-073"`, `$.error.message` = **MSG-073 nguyên văn tiếng Anh** (D7 — mã này **có** MSG nguồn), HTTP **503**
- **VÀ** vẫn có **1 dòng** `ai_requests` với `status = TIMEOUT` — ⚠️ khác `TC-002`: timeout là *"AI không trả lời"* (vẫn lưu vết được), còn `TC-002` là *"không lưu được vết"* (phải rollback)
- Người dùng vẫn dùng được chức năng thủ công sau đó (không rollback state khác)

**Expected Result (FAIL):**
- Không ghi dòng `TIMEOUT` ⇒ mất số liệu tỉ lệ lỗi AI, và `ERR-5521` không có nguồn đối chiếu
- Rollback toàn bộ như `TC-002` ⇒ **hiểu sai hai loại lỗi**; timeout là kết quả hợp lệ cần lưu

**Current Status:** 🔴 Not written
**Implementation Note:** ghi dòng `TIMEOUT` trong **transaction riêng** (`REQUIRES_NEW`) để không bị rollback cùng exception; nhưng `TC-002` (lỗi archive) thì **phải** rollback. Hai nhánh khác nhau — dễ implement gộp rồi sai cả hai.

---

#### MF12-TC-004 — Prompt gửi AI PHẢI redact mã/tên học sinh

**Severity:** `HIGH`
**OWASP:** `A02:2021 — Cryptographic Failures (data exposure)` · **CWE:** `CWE-201 — Insertion of Sensitive Information Into Sent Data`
**Legal:** **PDPD (Luật 91/2025)** — BR-173, BR-181
**Feature Under Test:** `PromptRedactor` trong `AiChatService`
**Test File:** `ai/PromptRedactorTest.java`
**TDD Phase:** 🔴 RED · ⚠️ BLOCKED bởi L6
**Condition Ref:** `COND-04` · **Constraint Ref:** `TDS §17.1 C1`
**Oracle Source:** **BR-173** (bảo vệ dữ liệu cá nhân trong log/audit) + **BR-181** (*"không tiết lộ thông tin nhận diện cá nhân học sinh"*) + `TDS` §13 hàng `UT-12-04` (*"prompt gửi đi **không** chứa mã/tên HS"*) + `TDS` §14.2 (*"BR-173: nội dung hội thoại AI KHÔNG ghi ra file log"*)
**Kỹ thuật:** Error Guessing (dữ liệu PII lọt vào prompt)

**Preconditions:** `FX-028` prompt = `"Học sinh HS-3A-001 Nguyễn Minh Khang bị dị ứng tôm, gợi ý món thay thế"`.

**Test Steps:**
1. `service.ask(prompt, actor)` với `FX-028`.
2. Bắt prompt thực gửi qua `ArgumentCaptor<String>` trên `geminiClient.generate()`.
3. Kiểm dòng `ai_requests.prompt` lưu trong DB.

**Expected Result (PASS):**
- Prompt gửi tới Gemini **không** chứa `"HS-3A-001"` lẫn `"Nguyễn Minh Khang"` (thay bằng placeholder kiểu `[STUDENT]`)
- **Ngữ nghĩa được giữ**: cụm `"dị ứng tôm"` vẫn còn ⇒ AI vẫn trả lời được (redact không được làm mất thông tin nghiệp vụ)
- Dòng `ai_requests.prompt` lưu **bản đã redact** (dữ liệu tại chỗ cũng sạch, không chỉ sạch lúc gửi ra ngoài)
- Mã HS ở các khuôn khác (`HS-5B-007`) cũng bị redact — test bằng regex `HS-\d+[A-Z]-\d+`

**Expected Result (FAIL):**
- Gửi nguyên mã/tên HS ⇒ **dữ liệu sức khỏe của trẻ được gửi sang dịch vụ AI của bên thứ ba** — sự cố PDPD phải báo cáo
- Redact quá tay (xoá cả "dị ứng tôm") ⇒ AI trả lời vô nghĩa, người dùng bỏ dùng chức năng

**Current Status:** 🔴 Not written
**Implementation Note:** redact ở **tầng port** (`AiAssistPort`) để **mọi** module gọi AI đều được bảo vệ, không chỉ MF12. MF05 gọi qua cùng port (`TC-INT-002`).

---

#### MF12-TC-005 — User chỉ xem được hội thoại của chính mình

**Severity:** `HIGH`
**OWASP:** `A01:2021 (IDOR)` · **CWE:** `CWE-639`
**Legal:** PDPD — hội thoại có thể chứa dữ liệu nghiệp vụ nhạy cảm
**Feature Under Test:** `AiChatService.history()`
**Test File:** `ai/AiChatServiceTest.java`
**TDD Phase:** 🔴 RED · ⚠️ BLOCKED bởi L6
**Condition Ref:** `COND-05` · **Constraint Ref:** `TDS §17.1 C1`
**Oracle Source:** `TDS` §16 hàng `GET /ai/conversations` — **`✅ Own`** cho cả 7 role + `TDS` §10 → `AIS-005` (MSG-005), HTTP **403** + `TDS` §13 hàng `UT-12-05`

**Preconditions:** `FX-020` conversation của `caterer-001`; `FX-021` conversation của `medical-001`.

**Test Steps:**
1. JWT `caterer-001` → `GET /ai/conversations` → assert.
2. JWT `caterer-001` → `GET /ai/conversations/{code của medical-001}` → assert.
3. JWT `system-admin` → conversation của người khác → assert.

**Expected Result (PASS):**
- (1) → chỉ conversation của `caterer-001`
- (2) → **403** `AIS-005`, message = **MSG-005 nguyên văn**
- (3) → **403** — §16 ghi `✅ Own` **cả** ở cột SYS ADMIN, tức **không** có ngoại lệ "admin xem được tất". TC khẳng định rõ điều này vì đây là chỗ dễ nới lỏng sai.
- Filter áp ở **repository/Specification** theo `user_id`, không phải kiểm sau khi load

**Expected Result (FAIL):**
- (3) trả 200 ⇒ System Admin đọc được hội thoại AI của mọi người dùng — vượt phạm vi §16 và là rủi ro PDPD

**Current Status:** 🔴 Not written

---

#### MF12-TC-006 — Save integration khi CHƯA test connection → AIS-163

**Severity:** `CRITICAL`
**Feature Under Test:** `IntegrationService.save()`
**Test File:** `integration/IntegrationServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-06` · **Constraint Ref:** `TDS §17.1 C4`
**Oracle Source:** **BR-163** nguyên văn — *"Khi cập nhật cấu hình tích hợp ngoài (GPS, Gmail, Firebase, Gemini AI) phải **kiểm tra kết nối thành công trước khi lưu**"* + `TDS` §10 → **`AIS-163`**, HTTP **409** + `TDS` §13 hàng `UT-12-06` + **L16/CF-14**
**⚠️ D7/L4:** `AIS-163` **không có MSG nguồn** ⇒ assert **code**, không khoá chuỗi.
**⚠️ Logic Issue:** **L16/CF-14** — BR-163 **CHƯA TỪNG CHẠY**: `testConnection()` là `setTimeout(600ms)` thuần client với luật *"key chứa `invalid` → fail"*; `save()` set `connected: true` **vô điều kiện**.

**Preconditions:** `FX-007` `code="gemini"`, `last_test_passed = false`, `last_tested_at = null`.

**Test Steps:**
1. `save(code, {endpoint, apiKey})` khi chưa test → assert.
2. Kiểm DB.
3. Biến thể: FE gửi kèm `connected: true` → assert.

**Expected Result (PASS):**
- (1) → `IntegrationNotTestedException`, `$.error.code == "AIS-163"`, HTTP **409**
- (2) → DB **không** đổi: `endpoint` cũ, **không** lưu credential nào
- (3) → **vẫn 409**; backend **bỏ qua** `connected` client gửi và **tự suy** từ `last_test_passed` (C11) — đây là điểm mấu chốt: nếu tin `connected` của client thì BR-163 vô hiệu hoàn toàn

**Expected Result (FAIL):**
- Lưu được ⇒ **cấu hình sai được lưu vào hệ thống**, dịch vụ ngoài chết âm thầm; và BR-163 tiếp tục không có hiệu lực như hiện trạng L16
- Tin `connected` client gửi ⇒ lách bằng 1 field trong payload

**Current Status:** 🔴 Not written
**Implementation Note:** ⚠️ **Minh phải thay hàm `testConnection()` giả** bằng lệnh gọi `POST /integrations/{code}/test-connection`. Backend handshake thật, timeout 5s.

---

#### MF12-TC-007 — Cửa sổ hiệu lực test connection: 10 phút (biên)

**Severity:** `HIGH`
**Feature Under Test:** `IntegrationService.save()` — kiểm tuổi `last_tested_at`
**Test File:** `integration/IntegrationServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-06` · **Constraint Ref:** `TDS §17.1 C4` (*"PASS và **chưa quá 10 phút**"*)
**Oracle Source:** **BR-163** + `TDS` §17.1 **C4** + `TDS` §13 hàng `UT-12-07` (*"`last_tested_at = now−11m` → `AIS-163`"*)
**Kỹ thuật:** Boundary Value Analysis (**on/off-point** của cửa sổ 10 phút)

**Test Steps:** `@ParameterizedTest` với `last_tested_at` ∈ {`now−9m59s`, `now−10m`, `now−10m01s`, `now−11m`} và `last_test_passed=true`.

**Expected Result (PASS):**
- `now−9m59s` → **lưu được**
- `now−10m` → **lưu được** (*"chưa quá 10 phút"* ⇒ đúng 10 phút vẫn hợp lệ)
- `now−10m01s` và `now−11m` → **409 `AIS-163`**
- Sau khi lưu thành công, `connected` được set **true** (suy từ `last_test_passed`, không từ client)
- `last_test_passed = false` + tuổi 1 phút → **vẫn 409** (PASS là điều kiện độc lập với tuổi)

**Expected Result (FAIL):**
- Chặn ở `now−10m` ⇒ off-by-one, System Admin phải test lại liên tục
- Cho qua ở `now−11m` ⇒ cấu hình được lưu dựa trên một lần test đã cũ; endpoint có thể đã đổi trong khoảng đó

**Current Status:** 🔴 Not written
**Implementation Note:** dùng `Clock` inject được; hằng `TEST_VALIDITY = Duration.ofMinutes(10)` ở **một chỗ**.

---

#### MF12-TC-008 — Test connection FAIL: không lưu credential, cờ = false

**Severity:** `HIGH`
**Feature Under Test:** `IntegrationService.testConnection()`
**Test File:** `integration/IntegrationServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-07` · **Constraint Ref:** `TDS §17.1 C4`
**Oracle Source:** `MF12_AI_Supporting_Services.md` **UC-040 AF1** (test connection thất bại) + **BR-163** + `TDS` §10 → `AIS-163` HTTP **422** *(khác `TC-006`: 422 = test thất bại, 409 = chưa test)* + `TDS` §13 hàng `UT-12-08` (*"`last_test_passed=false`, **không** lưu credential"*)

**Preconditions:** `FX-023` fake trả 401/lỗi kết nối; `FX-027` `apiKey` test.

**Test Steps:**
1. `testConnection(code, {endpoint, apiKey})` với fake lỗi.
2. Kiểm DB.
3. Biến thể: fake timeout > 5s.
4. Biến thể: fake thành công.

**Expected Result (PASS):**
- (1) → `$.error.code == "AIS-163"`, HTTP **422**
- (2) → `last_test_passed = false`, `last_tested_at` **được cập nhật** (có ghi nhận lần thử), **KHÔNG** có credential nào được lưu vào DB
- (3) → timeout 5s → cũng `AIS-163` 422, không treo request
- (4) → `last_test_passed = true`, `last_tested_at = now`; credential mã hoá **chỉ** lưu ở bước `save()`, không lưu ở bước test
- `statusNote` phản ánh nguyên nhân (để màn UC-149 hiển thị — L14)

**Expected Result (FAIL):**
- Lưu credential khi test FAIL ⇒ khoá sai/khoá lộ nằm trong DB không cần thiết; tăng bề mặt rủi ro BR-164
- Không cập nhật `last_tested_at` khi FAIL ⇒ không phân biệt được "chưa từng test" với "test rồi nhưng fail"

**Current Status:** 🔴 Not written

---

#### MF12-TC-009 — Credential KHÔNG lộ qua API — chỉ credentialMasked

**Severity:** `CRITICAL`
**OWASP:** `A02:2021 — Cryptographic Failures` · **CWE:** `CWE-522 — Insufficiently Protected Credentials`
**Feature Under Test:** `IntegrationMapper` + `GET /integrations`
**Test File:** `integration/CredentialExposureTest.java`
**TDD Phase:** 🔴 RED · ⚠️ **BLOCKED phần input bởi L17/CF-15** (FE chưa gửi `apiKey`)
**Condition Ref:** `COND-08` · **Constraint Ref:** `TDS §17.1 C3`
**Oracle Source:** **BR-164** nguyên văn — *"Mọi thông tin xác thực dịch vụ ngoài (API key, OAuth credential) phải được **mã hóa** khi lưu trữ"* + `ADR-M12-03` (mã hoá **envelope**, không lưu plaintext) + `TDS` §13 hàng `UT-12-09` (*"response chỉ có `credentialMasked`, **không** field `apiKey`"*) + `TDS` §14.2 (*"BR-164: credential KHÔNG xuất hiện trong log — Expected: No output"*)
**⚠️ Logic Issue:** **L17/CF-15** — hiện `apiKey` **không bao giờ rời trình duyệt** ⇒ BR-164 **chưa có gì để mã hoá**. TC test theo **hành vi đích**.

**Test Steps:**
1. `save(code, {apiKey: FX-027})`.
2. `GET /integrations` → kiểm JSON.
3. `GET /integrations/{code}` → kiểm JSON.
4. Kiểm cột DB.
5. Bắt log toàn bộ luồng.

**Expected Result (PASS):**
- (2)(3) → JSON có `credentialMasked` (kiểu `"AIza••••0000"`), **KHÔNG** có khoá `apiKey`/`credential`/`secret`/`token` ở bất kỳ cấp nào (kiểm đệ quy tập khoá JSON)
- (4) → cột lưu là **ciphertext**, khác plaintext, có `key_version` và IV/nonce riêng mỗi bản ghi
- (5) → log **không** chứa chuỗi `AIzaTEST-not-a-real-key-000000000000000` lẫn tiền tố `AIza`
- Kiểm tự động (CI, theo §14.2):
  ```bash
  grep -rniE "AIza|sk-[A-Za-z0-9]{20,}|password=" target/*.log
  # Expected: no output (BR-164)
  ```

**Expected Result (FAIL):**
- Trả `apiKey` trong response ⇒ **bất kỳ System Admin (hoặc ai chiếm được session) đọc được khoá dịch vụ ngoài**; khoá Gemini/FCM bị lạm dụng gây chi phí và rủi ro pháp lý
- Lưu plaintext trong DB ⇒ một lần rò backup là rò toàn bộ khoá
- Log ghi khoá ⇒ khoá nằm trong log tập trung, ngoài phạm vi kiểm soát

**Current Status:** 🔴 Not written
**Implementation Note:** ⚠️ **Minh bổ sung `apiKey` vào payload `PUT /integrations/{code}`** — không có bước này thì BR-164 không có gì để mã hoá (L17). **Ưu tiên cao** (§9.5 mục 3 của TDS).

---

#### MF12-TC-010 — Mã hoá/giải mã AES-256-GCM round-trip

**Severity:** `CRITICAL`
**CWE:** `CWE-327 — Use of a Broken or Risky Cryptographic Algorithm`
**Feature Under Test:** `CredentialCipher`
**Test File:** `integration/CredentialCipherTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-09` · **Constraint Ref:** `TDS §17.1 C3`
**Oracle Source:** **BR-164** + `ADR-M12-03` (*credential mã hoá **envelope**, khoá gốc từ **ENV**, không lưu plaintext*) + `TDS` §13 hàng `UT-12-10` (*"plaintext khôi phục đúng; cipher khác plaintext"*) + `TDS` §11.3 bước 5 (*"Kiểm tra biến môi trường credential — KHÔNG hardcode"*)
**Kỹ thuật:** Cryptographic Testing

**Preconditions:** `FX-026` — `SFMS_CRED_MASTER_KEY` (32 byte base64, **chuỗi test**), `SFMS_CRED_KEY_VERSION=1`.

**Test Steps:**
1. `encrypt(plaintext)` → `decrypt(cipher)`.
2. `encrypt` **cùng** plaintext **2 lần** → so 2 ciphertext.
3. Sửa 1 byte ciphertext → `decrypt`.
4. Kiểm khoá không hardcode trong source.

**Expected Result (PASS):**
- (1) → `decrypt(encrypt(x)) == x` với chuỗi ASCII, Unicode có dấu, và chuỗi 4KB
- (2) → **2 ciphertext KHÁC nhau** (IV/nonce random mỗi lần) — nếu giống nhau là dùng IV cố định, phá tính an toàn của GCM
- (3) → `decrypt` **ném** (GCM auth tag phát hiện sửa), **không** trả plaintext sai âm thầm
- (4) → khoá **không** xuất hiện trong source:
  ```bash
  grep -rnE "SFMS_CRED_MASTER_KEY\s*=\s*\"" src/main/java/   # Expected: no output
  ```
- `key_version` được lưu cùng ciphertext (chuẩn bị cho rotate — §7 R4)

**Expected Result (FAIL):**
- (2) 2 ciphertext giống nhau ⇒ IV cố định ⇒ tấn công so sánh bản mã; vi phạm nguyên tắc dùng GCM
- (3) trả plaintext sai mà không ném ⇒ không phát hiện được ciphertext bị sửa; mất tính toàn vẹn
- Khoá hardcode ⇒ khoá nằm trong git history, rotate vô nghĩa

**Current Status:** 🔴 Not written

---

#### MF12-TC-011 — Sai key_version → AIS-164, không trả plaintext

**Severity:** `HIGH`
**Feature Under Test:** `CredentialCipher.decrypt()`
**Test File:** `integration/CredentialCipherTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-09` · **Constraint Ref:** `TDS §17.1 C3`
**Oracle Source:** `TDS` §10 → **`AIS-164`**, HTTP **500** (*"Không giải mã được thông tin xác thực dịch vụ ngoài (BR-164)"* — trigger: *"Sai `key_version`/khoá ENV"*) + `ADR-M12-03` + §7 R4 (rotate credential)
**⚠️ D7/L4:** assert **code**, không khoá chuỗi tiếng Việt.

**Test Steps:**
1. Ciphertext ghi với `key_version=1`, môi trường chỉ có khoá `version=2` → `decrypt`.
2. Thiếu hẳn biến `SFMS_CRED_MASTER_KEY` → khởi tạo service.
3. Sau khi rotate (thêm khoá v2, **giữ** v1) → `decrypt` bản ghi v1.

**Expected Result (PASS):**
- (1) → `$.error.code == "AIS-164"`, HTTP **500**; **không** trả plaintext, **không** trả chuỗi rác
- (2) → **fail-fast lúc khởi động** (không chạy được app), **không** âm thầm chạy với khoá mặc định
- (3) → giải mã **thành công** — rotate phải **giữ khoá cũ** để đọc bản ghi cũ (§7 R4: *"tăng `key_version`, mã hoá lại toàn bộ"* ⇒ trong lúc chuyển đổi cần đọc được cả hai)
- Log lỗi **không** chứa ciphertext lẫn khoá

**Expected Result (FAIL):**
- Trả plaintext rác ⇒ credential sai được gửi tới dịch vụ ngoài, gây khoá tài khoản dịch vụ
- Chạy được với khoá mặc định ⇒ toàn bộ credential mã hoá bằng khoá ai cũng biết

**Current Status:** 🔴 Not written

---

#### MF12-TC-012 — Audit hash chuỗi: entryHash(n) = SHA256(entryHash(n−1) ‖ nội dung)

**Severity:** `CRITICAL`
**CWE:** `CWE-353 — Missing Support for Integrity Check`
**Feature Under Test:** `AuditLogService.append()`
**Test File:** `audit/AuditHashChainTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-10` · **Constraint Ref:** `TDS §17.1 C2`
**Oracle Source:** **BR-172** nguyên văn — *"Phải có cơ chế **bảo vệ toàn vẹn** nhật ký, ngăn chỉnh sửa trái phép"* + `ADR-M12-02` (*append-only + hash chuỗi*) + `TDS` §17.1 **C2** (*"`entry_hash = SHA256(prev_hash ‖ nội dung)`"*) + `TDS` §13 hàng `UT-12-11`
**Kỹ thuật:** Cryptographic / Integrity Testing

**Preconditions:** `FX-001`, `FX-002`, `FX-003` — 3 dòng audit liên tiếp.

**Test Steps:**
1. Ghi 3 dòng liên tiếp qua `append()`.
2. Tính lại hash bằng công thức độc lập **trong test** (không gọi hàm của production — chống tautology).
3. So từng dòng.
4. Kiểm dòng đầu tiên (`prev_hash` khi chuỗi rỗng).

**Expected Result (PASS):**
- Dòng 1: `prev_hash` = giá trị khởi tạo xác định (genesis, ví dụ 64 ký tự `0`), **không** `null`
- Dòng 2: `entry_hash(2) == SHA256(entry_hash(1) ‖ canonical(row2))`
- Dòng 3: tương tự với dòng 2
- `canonical(row)` là **thứ tự field xác định** (không phụ thuộc thứ tự map/JSON) — test bằng cách đổi thứ tự field trong DTO rồi assert hash **không đổi**
- Hash tính **trong cùng transaction** với INSERT (2 request song song không tạo 2 dòng cùng `prev_hash`)

**Expected Result (FAIL):**
- `prev_hash` null ở dòng đầu ⇒ chuỗi không có điểm neo, xoá dòng đầu không phát hiện được
- Hash phụ thuộc thứ tự field ⇒ nâng cấp thư viện JSON làm toàn bộ chuỗi "vỡ" giả
- Tính hash ngoài transaction ⇒ 2 dòng cùng `prev_hash` ⇒ chuỗi phân nhánh, `verifyChain` vô nghĩa

**Current Status:** 🔴 Not written
**Implementation Note:** dùng khoá tuần tự (`BIGSERIAL seq`) + lock hàng cuối, hoặc `INSERT … RETURNING` với advisory lock. Đừng dựa vào `created_at` để xác định "dòng trước".

---

#### MF12-TC-012b — Phá vỡ chuỗi hash PHẢI bị phát hiện kèm brokenAt

**Severity:** `CRITICAL`
**CWE:** `CWE-354 — Improper Validation of Integrity Check Value`
**Feature Under Test:** `AuditLogService.verifyChain()`
**Test File:** `audit/AuditHashChainTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-11` · **Constraint Ref:** `TDS §17.1 C2`
**Oracle Source:** **BR-172** + `TDS` §13 hàng `UT-12-12` (*"sửa 1 dòng ở tầng DBA → `verifyChain` trả `valid=false`, `brokenAt` đúng id"*) + §9.2 #4 (`GET /audit-logs/integrity`)
**Kỹ thuật:** Error Guessing (tấn công từ tầng DBA — vượt qua service)

**Test Steps:**
1. Ghi 50 dòng → `verifyChain()` → assert.
2. Sửa dòng **giữa chuỗi** (dòng 25) bằng SQL với role owner → `verifyChain()`.
3. **Xoá** dòng giữa chuỗi → `verifyChain()`.
4. Sửa dòng **cuối** → `verifyChain()`.

**Expected Result (PASS):**
- (1) → `{valid: true, checked: 50}`
- (2) → `valid = false`, **`brokenAt` = code của dòng 25** (không phải dòng 1 hay dòng cuối)
- (3) → `valid = false`, `brokenAt` = dòng **26** (dòng có `prev_hash` không còn khớp)
- (4) → `valid = false` — ⚠️ ca này dễ sót nếu chỉ verify `prev_hash` mà không verify `entry_hash` của chính dòng đó
- `checked` phản ánh đúng số dòng đã kiểm

**Expected Result (FAIL):**
- (2) trả `valid=true` ⇒ **sửa audit giữa chuỗi không bị phát hiện** — vô hiệu hoá BR-172; đây là ca nguy hiểm nhất (sửa cuối chuỗi thì dễ thấy hơn)
- `brokenAt` chỉ ra dòng sai ⇒ điều tra viên tìm sai chỗ

**Current Status:** 🔴 Not written

---

#### MF12-TC-013 — audit_logs KHÔNG sửa được ở tầng entity

**Severity:** `CRITICAL`
**CWE:** `CWE-284 — Improper Access Control`
**Feature Under Test:** `AuditLog` `@Immutable` + repository
**Test File:** `audit/AuditImmutabilityTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-12` · **Constraint Ref:** `TDS §17.1 C2`
**Oracle Source:** **BR-162** nguyên văn — *"Nhật ký audit phải **bất biến (immutable)** — **không ai** chỉnh sửa/xóa bản ghi audit"* + `TDS` §13 hàng `UT-12-13` (*"gọi repo `save()` với id đã tồn tại → `@Immutable` → không sinh UPDATE"*) + `TDS` §16 ghi chú (*"**không role nào** — kể cả System Admin — có endpoint sửa/xoá"*)

**Test Steps:**
1. Load `FX-001`, đổi field, `repository.save()` → bắt SQL sinh ra.
2. `repository.delete()` → assert.
3. Kiểm annotation entity.

**Expected Result (PASS):**
- (1) → **KHÔNG** có câu `UPDATE audit_logs` nào được sinh (assert qua log `org.hibernate.SQL` hoặc `StatementInspector`); Hibernate bỏ qua do `@Immutable`
- (2) → ném exception hoặc không sinh `DELETE`
- (3) → entity có `@Immutable`; **không** có setter public cho field nội dung
- Ba lớp bảo vệ đều có: **entity** (`TC-013`) + **DB GRANT** (`TC-INT-003`) + **API** (`TC-INT-004`)

**Expected Result (FAIL):**
- Sinh `UPDATE` ⇒ chỉ còn 2 lớp bảo vệ; một đoạn code nghiệp vụ vô tình `save()` lại entity đã load là sửa audit — âm thầm và khó phát hiện

**Current Status:** 🔴 Not written
**Implementation Note:** `@Immutable` **im lặng bỏ qua** UPDATE (không ném) ⇒ test phải assert **SQL sinh ra**, không phải assert exception. Đây là chỗ dễ viết test sai thành "không ném là pass".

---

#### MF12-TC-014 — Ghi audit lỗi PHẢI fail transaction nghiệp vụ

**Severity:** `CRITICAL`
**Feature Under Test:** `AuditLogPort` trong transaction nghiệp vụ
**Test File:** `audit/AuditPortTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-13` · **Constraint Ref:** `TDS §17.1 C2`
**Oracle Source:** **BR-170** nguyên văn — *"**Mọi** hành động quan trọng (tạo/cập nhật/xóa/phê duyệt/từ chối) **phải** ghi audit kèm: người thực hiện, thời gian, đối tượng bị tác động"* ⇒ không ghi được audit thì hành động **không được** coi là hoàn tất + `TDS` §17.1 **C2** (*"Ghi audit lỗi ⇒ **fail transaction nghiệp vụ**"*) + `TDS` §13 hàng `UT-12-14`
**Kỹ thuật:** Error Guessing

**Test Steps:**
1. Mock `AuditLogRepository` ném lỗi khi INSERT.
2. Thực hiện 1 thao tác nghiệp vụ có audit (ví dụ `PUT /security-policies`).
3. Kiểm cả 2 bảng.

**Expected Result (PASS):**
- Thao tác nghiệp vụ **rollback**: `security_policies` **không** đổi
- Trả lỗi cho client (không âm thầm thành công)
- **Không** có dòng audit mồ côi
- Áp cho **mọi** module dùng `AuditLogPort` — test bằng 2 thao tác ở 2 module khác nhau

**Expected Result (FAIL):**
- Nghiệp vụ thành công mà không có audit ⇒ **hành động quan trọng không có dấu vết** — vi phạm BR-170; khi điều tra sẽ thấy dữ liệu đổi mà không biết ai đổi
- Chỉ log warning rồi tiếp tục ⇒ lỗi tích tụ âm thầm, phát hiện khi đã mất nhiều dấu vết

**Current Status:** 🔴 Not written
**Implementation Note:** đây là quyết định **có chủ đích đánh đổi khả dụng vì tính truy vết**. Nếu team muốn khác (log-and-continue), phải sửa **C2 trong TDS trước**, không sửa test.

---

#### MF12-TC-015 — Chính sách bảo mật ngoài khoảng → IAM-184 kèm details.allowed

**Severity:** `HIGH`
**Feature Under Test:** `SecurityPolicyService.update()`
**Test File:** `policy/SecurityPolicyServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-14` · **Constraint Ref:** **D2** (envelope `details`)
**Oracle Source:** `MF12_AI_Supporting_Services.md` **UC-184 bước 4** (validate khoảng giá trị) + `TDS` §10 → **`IAM-184`**, HTTP **422** + `TDS` §13 hàng `UT-12-15` (*"`maxLoginAttempts = 20` → `IAM-184` (422), liệt kê `allowed: "3–10"`"*) + **D2** (*`details` là mảng, không bao giờ `null`*)
**⚠️ D7/L4:** `IAM-184` không có MSG nguồn ⇒ assert **code** + **`details`**, không khoá chuỗi `message`.
**Kỹ thuật:** Boundary Value Analysis + Decision Table

**Preconditions:** `FX-011` `SEC-DEFAULT`.

**Test Steps:** `@ParameterizedTest` theo bảng:

| # | Field | Giá trị | Expected |
|---|---|---|---|
| a | `maxLoginAttempts` | `2` | **422** `IAM-184`, `details[0].allowed == "3–10"` |
| b | `maxLoginAttempts` | `3` | **200** (biên dưới hợp lệ) |
| c | `maxLoginAttempts` | `10` | **200** (biên trên hợp lệ) |
| d | `maxLoginAttempts` | `20` | **422** — khớp `UT-12-15` |
| e | `passwordMinLength` | `6` | **422** (BR-006 tối thiểu 8) |
| f | 4 field hợp lệ `5/15/30/8` | — | **200**, **MSG-029**, phát `SecurityPolicyUpdated` |

**Expected Result (PASS):**
- Đúng 6 hàng; `$.error.details` là **mảng** chứa `{field, value, allowed}` cho **từng** field sai (gửi 2 field sai → 2 phần tử)
- (f) → message = **MSG-029** nguyên văn `"Profile updated successfully."` *(§1.12 gán MSG-029 cho UC-184)* + event được phát
- Ở mọi ca 422, DB **không** đổi field nào (reject cả request, không ghi một phần)

**Expected Result (FAIL):**
- `details` rỗng/null ⇒ System Admin không biết khoảng cho phép là gì, phải đoán (D2 nói `details` không bao giờ null)
- Ghi một phần khi có field sai ⇒ chính sách ở trạng thái nửa vời, MF01 áp giá trị lẫn lộn

**Current Status:** 🔴 Not written
**Implementation Note:** ⚠️ **khoảng cho phép của từng field chưa có trong BR-002/004/006** — chỉ `UT-12-15` nêu `"3–10"`. Ghi rõ vào TC và **cần Huy xác nhận** khoảng của 4 field còn lại trước khi hardcode.

---

#### MF12-TC-016 — PUT partial merge: requireMixedCase vắng ⇒ giữ giá trị cũ

**Severity:** `HIGH`
**Feature Under Test:** `SecurityPolicyService.update()` — partial merge
**Test File:** `policy/SecurityPolicyServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-15` · **Constraint Ref:** `TDS §17.1 C10`
**Oracle Source:** `TDS` §17.1 **C10** nguyên văn — *"`PUT /{resource}/{id}` là **merge một phần (PATCH-semantics)**… Field vắng ⇒ **giữ nguyên giá trị cũ** (vd `requireMixedCase`, CF-16), **không** set null"* + **L18/CF-16** (*FE gửi 7/8 field; `requireMixedCase` đóng băng ở giá trị seed*) + **BR-006**
**Kỹ thuật:** Contract Testing

**Preconditions:** `FX-011` — `requireMixedCase = true`, `noReuseLast3 = true`.

**Test Steps:**
1. `PUT` với **7 field** (thiếu `requireMixedCase`) → assert.
2. `PUT` với `{"requireMixedCase": false}` (1 field) → assert.
3. `PUT` với `{"requireMixedCase": null}` (gửi **null tường minh**) → assert.

**Expected Result (PASS):**
- (1) → **200**; `requireMixedCase` **vẫn `true`** (không bị set `false`/`null`); 7 field kia cập nhật đúng
- (2) → `requireMixedCase = false`; **các field khác không đổi** (`noReuseLast3` vẫn `true`)
- (3) → **422** — phân biệt rõ *"không gửi"* (giữ nguyên) với *"gửi null"* (không hợp lệ cho boolean bắt buộc)
- `updatedBy`/`updatedAt` được cập nhật từ JWT + server clock, **không** tin client

**Expected Result (FAIL):**
- (1) set `requireMixedCase = false` ⇒ **chính sách mật khẩu bị nới lỏng âm thầm mỗi lần System Admin lưu bất kỳ thay đổi nào** — hậu quả bảo mật thật, và người dùng không hề biết
- (3) chấp nhận null ⇒ cột NOT NULL vỡ hoặc giá trị mặc định sai

**Current Status:** 🔴 Not written
**Implementation Note:** DTO dùng `Optional<Boolean>`/`JsonNullable<Boolean>`. Record thường sẽ điền `null` cho field vắng ⇒ không phân biệt được — đây chính là cách bug L18 phát sinh.

---

#### ~~MF12-TC-017~~ — school_id của checklist LUÔN lấy từ JWT

> ⛔ **`@Disabled("M12-D02 — thuoc MF07")` tu 2026-08-04.** Bang `checklist_groups`/`checklist_items` (`V12:209/223`) va endpoint `/api/v1/checklist-groups` da merge o MF07 (`M07-D10`); `TestSpec-MF07` da phu. Giu nguyen van theo Policy 4.4, **khong hien thuc trong `mf12`**.

**Severity:** `CRITICAL`
**OWASP:** `A01:2021 — Broken Access Control` · **CWE:** `CWE-639`
**Feature Under Test:** `ChecklistAdminService.create()/update()`
**Test File:** `checklist/ChecklistScopeTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-17` · **Constraint Ref:** `TDS §17.1 C12`
**Oracle Source:** **BR-210** nguyên văn — *"phạm vi **theo từng trường**"* (*"mỗi trường có checklist riêng… không ảnh hưởng tới checklist của trường khác"*) + `TDS` §17.1 **C12** (*"`school_id` **luôn lấy từ JWT**, không nhận từ body"*) + `TDS` §16 bảng field-level + **L23/CF-25** (*FE không gửi `schoolId`*)
**Kỹ thuật:** Error Guessing (gửi `schoolId` khác trong body)

**Preconditions:** `FX-012` checklist của `SCH-001`; `FX-013` checklist của `SCH-002`; `FX-030` JWT Medical `SCH-001`.

**Test Steps:**
1. `POST /checklist-groups` **không** gửi `schoolId` (hành vi FE hiện tại) → assert.
2. `POST` **có** gửi `{"schoolId": "SCH-002"}` → assert.
3. `GET /checklist-groups` bằng `FX-030` → assert.
4. `PUT` nhóm thuộc `SCH-002` bằng `FX-030` → assert.

**Expected Result (PASS):**
- (1) → **201**; DB `school_id = "SCH-001"` (từ JWT)
- (2) → `school_id` **vẫn** `"SCH-001"` — body bị **bỏ qua**; TC chấp nhận cả 201 (bỏ qua im lặng) hoặc 422 (từ chối tường minh) nhưng phải **nhất quán** và ghi rõ vào TDS
- (3) → chỉ nhóm của `SCH-001`; **không** thấy `FX-013`
- (4) → **403/404** (không sửa được checklist trường khác)

**Expected Result (FAIL):**
- (2) ghi `school_id = SCH-002` ⇒ **Medical Staff trường A tạo/sửa checklist cho trường B** — vi phạm BR-210 trực tiếp
- (3) trả cả 2 trường ⇒ **checklist trường này lộ sang trường khác**, và MF07 chấm điểm sai bộ mục (đúng hậu quả L23 cảnh báo)

**Current Status:** 🔴 Not written

---

#### ~~MF12-TC-018~~ — Xoá checklist chưa dùng: ẩn, không cảnh báo

> ⛔ **`@Disabled("M12-D02 — thuoc MF07")` tu 2026-08-04.** Bang `checklist_groups`/`checklist_items` (`V12:209/223`) va endpoint `/api/v1/checklist-groups` da merge o MF07 (`M07-D10`); `TestSpec-MF07` da phu. Giu nguyen van theo Policy 4.4, **khong hien thuc trong `mf12`**.

**Severity:** `MEDIUM`
**Feature Under Test:** `ChecklistAdminService.delete()`
**Test File:** `checklist/ChecklistLifecycleTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-16` · **Constraint Ref:** `TDS §17.1 C5`
**Oracle Source:** **BR-210** (*"Xóa mục **đang được tham chiếu** → **không xóa dữ liệu lịch sử**, chỉ ẩn khỏi phiên mới"*) + `ADR-M12-06` + `TDS` §13 hàng `UT-12-17` (*"item mới → `isActive=false`, **không** cảnh báo"*)

**Test Steps:** `DELETE /checklist-groups/{code}` với nhóm **chưa** có `checklist_item_results` nào.

**Expected Result (PASS):**
- **200**; `is_active = false`
- **KHÔNG** trả MSG-094 (không cảnh báo khi chưa ai dùng)
- **KHÔNG** có câu `DELETE FROM` nào chạy — kể cả khi chưa được tham chiếu (BR-210 áp cho **mọi** trường hợp)
- Phiên kiểm bếp mới (MF07) **không** còn nhóm này

**Expected Result (FAIL):**
- Xoá cứng khi "chưa ai dùng" ⇒ hiểu sai BR-210 thành điều kiện; sau này có phiên tham chiếu vào nhóm đã bị xoá vĩnh viễn thì không phục hồi được
- Trả MSG-094 khi chưa cần ⇒ System Admin phải bấm xác nhận vô nghĩa mọi lần

**Current Status:** 🔴 Not written

---

#### MF12-TC-019 — Xoá checklist ĐÃ dùng: MSG-094 chờ confirmed, dữ liệu lịch sử còn nguyên

**Severity:** `HIGH`
**Feature Under Test:** `ChecklistAdminService.delete()`
**Test File:** `checklist/ChecklistLifecycleTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-16` · **Constraint Ref:** `TDS §17.1 C5`
**Oracle Source:** **BR-210** + **MSG-094** nguyên văn = `"This checklist item/group has been used in saved inspection records. Deleting it will not remove historical data — it will only be hidden from new inspections. Continue?"` + `TDS` §10 → `AIS-094`, HTTP **409**, ghi rõ *"**cảnh báo xác nhận, không phải chặn cứng**"* + `TDS` §13 hàng `UT-12-18`, `UT-12-19`
**⚠️ Ranh giới (L7/CF-3):** TC này **phải khớp** `MF07-TC-016` / `MF07-TC-INT-010` — hai TestSpec test cùng bảng từ hai phía. Nếu lệch, ghi vào biên bản review cho Nam.

**Preconditions:** `FX-012` + `FX-014` (kết quả trỏ `CLI-3-8`).

**Test Steps:**
1. `DELETE /checklist-groups/CLG-3` (chưa `confirmed`) → assert.
2. Gọi lại `?confirmed=true` → assert.
3. Đếm `checklist_item_results`.
4. Đọc phiên cũ của MF07.

**Expected Result (PASS):**
- (1) → **409** + `$.error.code == "AIS-094"`, `$.error.message` = **MSG-094 nguyên văn** (mã này **có** MSG nguồn ⇒ khoá assert chuỗi được); `is_active` **vẫn true**
- (2) → **200**; `is_active = false`
- (3) → `checklist_item_results` **giữ nguyên số dòng**
- (4) → phiên cũ **vẫn** render đủ mục; phiên **mới** không còn nhóm đã ẩn
- ⚠️ Ngữ nghĩa: `AIS-094` là **cảnh báo xác nhận**, không phải chặn cứng — sau `confirmed=true` phải thành công. Đừng implement thành 403 vĩnh viễn.

**Expected Result (FAIL):**
- Xoá cứng + CASCADE ⇒ **phiên kiểm bếp lịch sử mất mục** ⇒ hồ sơ định kỳ tháng (BR-101, MF07) không tái tạo được
- `confirmed=true` vẫn 409 ⇒ hiểu sai cảnh báo thành chặn cứng, System Admin không bao giờ ẩn được mục

**Current Status:** 🔴 Not written

---

#### MF12-TC-020 — Báo cáo sức khỏe hệ thống hàng tuần tự động (BR-165)

**Severity:** `MEDIUM`
**Feature Under Test:** `@Scheduled` weekly job → `SystemHealthReport`
**Test File:** `metrics/SystemHealthJobTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-27` · **Constraint Ref:** `TDS §17.1 C9`
**Oracle Source:** **BR-165** nguyên văn — *"Hệ thống **tự động biên soạn** báo cáo sức khỏe hệ thống và số liệu người dùng hoạt động **hàng tuần** cho System Admin"* + `TDS` §17.1 **C9** (*"Báo cáo tuần BR-165 lưu vào `sent_reports(report_type='SYSTEM_HEALTH')`"*) + **L9/CF-5**
**⚠️ Logic Issue:** **L9/CF-5** — §1.12 gán BR-165 cho UC-149/UC-150 là **màn xem theo yêu cầu**, không phải job. Test **cả hai** (job ở đây, endpoint ở `TC-INT-017`).

**Test Steps:**
1. Trigger job thủ công (`@Scheduled` method gọi trực tiếp).
2. Kiểm `sent_reports`.
3. Kiểm cron expression.

**Expected Result (PASS):**
- 1 dòng `sent_reports` với `report_type = 'SYSTEM_HEALTH'`, `period = 'WEEKLY'`
- Payload có **cả hai** thành phần BR-165 yêu cầu: sức khỏe hệ thống **và** số liệu người dùng hoạt động
- Cron đúng chu kỳ **tuần** (assert biểu thức, không assert thời gian thực)
- Job **idempotent**: chạy 2 lần cùng tuần → **không** tạo 2 dòng cho cùng kỳ
- Số liệu lấy từ **Actuator + aggregate `audit_logs`**, không từ bảng metric riêng (`TC-INT-018`)

**Expected Result (FAIL):**
- Không có job ⇒ BR-165 chỉ được thoả một nửa (có màn xem, không có báo cáo tự động)
- Tạo trùng mỗi lần chạy ⇒ lịch sử báo cáo nhiễu, `TC-024` (snapshot) không đối chiếu được

**Current Status:** 🔴 Not written

---

#### MF12-TC-021 — Báo cáo tuân thủ kỳ RỖNG → chặn AIS-092 server-side

**Severity:** `CRITICAL`
**Feature Under Test:** `ComplianceReportSendService.send()`
**Test File:** `report/ComplianceReportSendServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-18` · **Constraint Ref:** `TDS §17.1 C6`
**Oracle Source:** **BR-209** nguyên văn — *"**Không có phiên nào trong kỳ → chặn gửi (MSG-092)**"* + **MSG-092** = `"No kitchen inspection data available for the selected period. Cannot generate compliance report."` + `TDS` §10 → `AIS-092`, HTTP **422** + `TDS` §13 hàng `UT-12-20` (*"**không** tạo `sent_reports`"*) + **L22/CF-24**
**⚠️ Logic Issue:** **L22/CF-24** — chặn hiện **chỉ ở client** (disable nút) ⇒ **gọi thẳng API là lách được**.
**Kỹ thuật:** Error Guessing (bypass UI control)

**Preconditions:** `FX-017` — kỳ **không có** phiên kiểm bếp nào.

**Test Steps:**
1. `POST /sent-reports/compliance` cho kỳ rỗng → assert.
2. Đếm `sent_reports`.
3. Biến thể `FX-016` (5 phiên, avg 90) → assert.

**Expected Result (PASS):**
- (1) → **422**, `$.error.code == "AIS-092"`, `$.error.message` = **MSG-092 nguyên văn** (có MSG nguồn)
- (2) → `sent_reports` **không** tăng dòng; **không** file PDF/XLSX nào được sinh
- (3) → **200**, message **MSG-091**; `payload.avgScore == 90`; có `filePdfUrl` **và** `fileXlsxUrl` (BR-178)
- **Không** thông báo nào được phát khi bị chặn (đừng báo cho School Admin về báo cáo không tồn tại)

**Expected Result (FAIL):**
- (1) tạo được ⇒ **báo cáo rỗng gửi tới School Administration**, làm sai số liệu tuân thủ và gây mất tin cậy — đúng lỗ hổng L22 đang tồn tại
- Sinh file rồi mới chặn ⇒ rác file trên storage

**Current Status:** 🔴 Not written

---

#### MF12-TC-022 — Báo cáo vi phạm kỳ RỖNG → chặn AIS-097; staffCount phải là 0

**Severity:** `HIGH`
**Feature Under Test:** `ViolationReportSendService.send()`
**Test File:** `report/ViolationReportSendServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-19` · **Constraint Ref:** `TDS §17.1 C6`
**Oracle Source:** **BR-214** nguyên văn — *"rollup số liệu + điều hướng, **không** lặp nội dung biên bản đã ký (BR-211). Không có vi phạm trong kỳ → **chặn gửi (MSG-097)**; thành công báo MSG-099"* + **MSG-097** = `"No violations recorded for the selected period. Cannot generate violation summary report."` + `TDS` §13 hàng `UT-12-22` + **L26/CF-28** (`staffCount` là **tàn dư BR-184 Deprecated**)

**Preconditions:** `FX-018` (kỳ rỗng) và `FX-019` (3 biên bản, `bySeverity={LOW:1,MEDIUM:2}`).

**Test Steps:**
1. Kỳ rỗng → assert.
2. `FX-019` → assert nội dung `counts`.
3. Kiểm **không** lặp nội dung biên bản.

**Expected Result (PASS):**
- (1) → **422** `AIS-097`, message MSG-097 nguyên văn; **không** tạo dòng
- (2) → **200**, message **MSG-099**; `counts.processCount == 3`, `counts.bySeverity == {LOW:1, MEDIUM:2}`
- (2) → **`counts.staffCount == 0`** — BR-184 đã Deprecated 26/07/2026, **không** sinh dữ liệu cá nhân mới (L26)
- (3) → payload **chỉ** có số liệu rollup + link điều hướng; **không** chứa `description`/ảnh/nội dung biên bản đã ký (BR-214 nói rõ *"không lặp nội dung biên bản đã ký (BR-211)"*)
- `counts.partnerCount` (field ngoài interface) được **dung nạp** trong JSONB, không làm fail request (L26)

**Expected Result (FAIL):**
- `staffCount > 0` ⇒ tái tạo dữ liệu nhắm cá nhân sau khi BR-184 đã bỏ — rủi ro PDPD
- Lặp nội dung biên bản ⇒ báo cáo tổng hợp chứa bản sao văn bản đã ký, gây nhầm lẫn về bản gốc có hiệu lực

**Current Status:** 🔴 Not written

---

#### MF12-TC-023 — UC-188 deprecated → 409 AIS-188 ở CẢ HAI đường

**Severity:** `HIGH`
**Feature Under Test:** `POST /sent-reports/incidents` **và** `POST /sent-reports` với `reportType='INCIDENT_SUMMARY'`
**Test File:** `report/DeprecatedReportTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-20` · **Constraint Ref:** `TDS §17.1 C7`
**Oracle Source:** **BR-213 Deprecated 26/07/2026** + `TDS` §10 → **`AIS-188`**, HTTP **409** + `TDS` §17.1 **C7** (*"**KHÔNG** sinh bản ghi `reportType='INCIDENT_SUMMARY'` mới"*) + **L8/CF-4**, **L24/CF-22**
**⚠️ D7/L4:** `AIS-188` không có MSG nguồn ⇒ assert **code**.
**⚠️ Logic Issue:** **L24/CF-22** — dialog gửi báo cáo sự cố **vẫn tồn tại** trong FE và vẫn `POST /sent-reports` với `reportType:"INCIDENT_SUMMARY"`.

**Test Steps:**
1. `POST /sent-reports/incidents` → assert.
2. `POST /sent-reports` với `{"reportType":"INCIDENT_SUMMARY", ...}` (**đường FE hiện tại**) → assert.
3. `GET /sent-reports?reportType=INCIDENT_SUMMARY` (đọc lịch sử) → assert.
4. `GET /sent-reports/{code cũ}` → assert.

**Expected Result (PASS):**
- (1)(2) → **409** `$.error.code == "AIS-188"`; **không** tạo dòng mới
- ⚠️ (2) là ca then chốt: nếu **chỉ** chặn endpoint mới thì FE hiện tại **lách được hoàn toàn** — dialog vẫn dùng đường `POST /sent-reports`
- (3) → **200** với báo cáo `INCIDENT_SUMMARY` **cũ** (đọc lịch sử **vẫn phải chạy** — C7 nói *"chỉ cho đọc lịch sử"*)
- (4) → **200**, trả đủ field của bản ghi cũ (kể cả field đã deprecated — L26)

**Expected Result (FAIL):**
- Chỉ chặn 1 đường ⇒ luật deprecated không có hiệu lực thực tế
- Chặn cả đọc lịch sử ⇒ mất truy cập báo cáo đã gửi trước 26/07 — vi phạm BR-177 (lưu ≥ 12 tháng)

**Current Status:** 🔴 Not written
**Implementation Note:** **Minh gỡ dialog** trong cùng đợt (`00_` §0). Chặn ở **service** để cả 2 controller đi qua cùng gate.

---

#### MF12-TC-024 — Snapshot báo cáo BẤT BIẾN: detail đọc lại số cũ

**Severity:** `HIGH`
**Feature Under Test:** `SentReportQueryService.detail()`
**Test File:** `report/SentReportSnapshotTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-21` · **Constraint Ref:** `TDS §17.1 C6`
**Oracle Source:** `ADR-M12-05` (single-table + JSONB payload) + **BR-177** (*báo cáo lưu ≥ 12 tháng*) + `TDS` §13 hàng `UT-12-24` (*"đổi dữ liệu nguồn sau khi gửi → `GET /sent-reports/{id}` trả **số cũ**"*) + comment nguyên văn trong `mock-data.ts` (*"Snapshot đúng thứ đã chốt lúc gửi — Detail view đọc lại nguyên văn, **KHÔNG tính lại**"*)
**Kỹ thuật:** State Transition (bản ghi bất biến)

**Preconditions:** `FX-015` (`RPTS-2026-001`, `avgScore=90`) + `FX-016` (5 phiên nguồn, avg 90).

**Test Steps:**
1. `GET /sent-reports/RPTS-2026-001` → ghi lại payload.
2. **Đổi dữ liệu nguồn**: thêm 1 phiên điểm 50 vào cùng kỳ (avg thật giờ là ~83).
3. `GET` lại → so sánh.
4. Thử `PUT /sent-reports/{code}` → assert.

**Expected Result (PASS):**
- (3) → `payload.avgScore` **vẫn 90** (số **tại thời điểm gửi**), **không** phải 83
- `threeStepSummary`, `failedSessions`, `staffHealthValid` cũng giữ nguyên
- (4) → **405/409** — báo cáo đã gửi là bất biến
- File PDF/XLSX đã sinh **không** bị tạo lại với số mới

**Expected Result (FAIL):**
- Tính lại ⇒ **báo cáo đã gửi cho School Administration nói khác với bản họ đã nhận** — mất tính đối chứng của văn bản; và nếu dữ liệu nguồn bị xoá thì báo cáo trống rỗng
- Cho `PUT` ⇒ sửa được báo cáo sau khi gửi

**Current Status:** 🔴 Not written

---

#### MF12-TC-025 — Báo cáo KHÔNG lộ danh tính học sinh; confidential khi nhạy cảm

**Severity:** `HIGH`
**OWASP:** `A01:2021` · **CWE:** `CWE-359 — Exposure of Private Personal Information`
**Legal:** **PDPD (Luật 91/2025)** — BR-181
**Feature Under Test:** `ReportExporter` + payload báo cáo
**Test File:** `report/ReportPrivacyTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-22` · **Constraint Ref:** `TDS §17.1 C6`
**Oracle Source:** **BR-181** nguyên văn — *"Báo cáo **không tiết lộ thông tin nhận diện cá nhân học sinh**; báo cáo xuất ra ngoài chứa thông tin nhạy cảm phải đánh dấu **"Confidential""* + `TDS` §14.2 (*"BR-181: file báo cáo xuất ra không chứa mã học sinh — Expected: No output"*) + `TDS` §13 hàng `UT-12-25`

**Preconditions:** dữ liệu nguồn **có** HS (`HS-3A-001`, phiên có xung đột dị ứng của HS cụ thể).

**Test Steps:**
1. Gửi báo cáo tuân thủ có dữ liệu nguồn chứa HS.
2. Kiểm payload JSON.
3. Kiểm **nội dung file** PDF + XLSX (extract text).
4. Kiểm cờ `confidential`.

**Expected Result (PASS):**
- (2) → payload **chỉ** có số liệu tổng hợp; **không** chứa `studentId`, tên HS, hay `HS-\d+[A-Z]-\d+`
- (3) → text extract từ **cả PDF và XLSX** không khớp regex `HS-\d+[A-Z]-\d+`:
  ```bash
  # §14.2 TDS — kiểm tự động
  pdftotext target/test-reports/*.pdf - | grep -E "HS-[0-9]+[A-Z]-[0-9]+"
  # Expected: no output
  ```
- (4) → báo cáo chứa thông tin nhạy cảm có **`confidential = true`** và **nhãn "Confidential" in trên file**
- Số lượng vẫn đúng (ví dụ *"2 học sinh bị ảnh hưởng"*) — ẩn danh tính **không** làm mất số liệu

**Expected Result (FAIL):**
- File báo cáo chứa mã/tên HS ⇒ báo cáo gửi School Administration (và có thể gửi ra ngoài) chứa dữ liệu sức khỏe của trẻ — sự cố PDPD phải báo cáo
- Thiếu nhãn "Confidential" ⇒ người nhận không biết cần bảo mật, dễ chuyển tiếp ra ngoài

**Current Status:** 🔴 Not written

---

#### MF12-TC-026 — JSON ngày là ISO-8601 (D4 ghi đè C13)

**Severity:** `HIGH`
**Feature Under Test:** mapper của `audit_logs`, `error_logs`, `sent_reports`, `security_policies`, `external_integrations`
**Test File:** `mapper/DateFormatContractTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-24` · **Constraint Ref:** **`00_Common_Conventions.md` D4** (ghi đè `TDS §17.1 C13` phần ngày)
**Oracle Source:** **D4** nguyên văn — *"Ngày → `"2026-07-29"` (`LocalDate`); Ngày giờ → `"2026-07-29T02:15:00Z"` (`Instant`)"*; *"FE thêm `lib/format.ts` và gọi ở **tầng render**"*; *"**Không sửa seed `mock-data.ts` ở đợt này**"* ⇒ backend vẫn phải **parse** `dd/MM/yyyy` + **L3**
**⚠️ Logic Issue:** **L3** — C13 và CF-26 nói `dd/MM/yyyy`. **D4 (Approved 30/07) ghi đè.**

**Test Steps:**
1. `PUT` với `{"lastTestedAt": "02/07/2026 09:41"}` (dạng mock) → assert parse.
2. `PUT` với ISO → assert parse.
3. `GET` cả 5 resource → assert **định dạng trả về**.
4. Kiểm không còn `@JsonFormat`.

**Expected Result (PASS):**
- (1)(2) → cùng ra một `Instant` (timezone `Asia/Ho_Chi_Minh` cho dạng locale) — parse **khoan dung cả 2**
- (3) → **ISO-8601** cho: `timestamp` (audit/error log), `sentAt`, `date`, `updatedAt`, `lastTestedAt`
- (4) →
  ```bash
  grep -rn "@JsonFormat" src/main/java/com/sfms/mf12/
  # Expected: no output (D4)
  ```
- DB: các cột là `timestamptz`/`date`, **không** `varchar`

**Expected Result (FAIL):**
- Trả `dd/MM/yyyy` ⇒ vi phạm D4; MF12 là **nền chung**, nếu nó lệch thì `audit_logs` (13 module ghi vào) có định dạng khác cả hệ thống
- Không parse được `dd/MM/yyyy` ⇒ FE hiện tại gãy ngay
- Lưu chuỗi vào DB ⇒ filter `dateFrom`/`dateTo` (`TC-INT-008`) sai. ⚠️ D4 cảnh báo: `paginate()` của mock parse ngày bằng regex `dd/MM/yyyy` — seed chuyển ISO mà quên sửa thì lọc **im lặng trả rỗng**

**Current Status:** 🔴 Not written
**Implementation Note:** converter **dùng chung ở `common/`** (Nam sở hữu) cho cả 13 module.

---

#### MF12-TC-027 — period parse case-insensitive, trả "Weekly" (ReportPeriod ngoài D6)

**Severity:** `MEDIUM`
**Feature Under Test:** mapper `SentReport.period`
**Test File:** `mapper/ReportPeriodContractTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-25` · **Constraint Ref:** `TDS §17.1 C13` + **CF-27**
**Oracle Source:** `TDS` §17.1 **C13** (*`period` = **`"Weekly"`/`"Daily"`**) + **CF-27** (*"Backend **parse case-insensitive**, trả về đúng dạng FE đang đọc (`"Weekly"`)"*) + `FX-015` (`period: "Weekly"` trong seed thật)
**⚠️ Lưu ý D6:** **`ReportPeriod` KHÔNG nằm trong 8 enum của D6** ⇒ quy ước SCREAMING_SNAKE **không tự động** áp. C13 vẫn có hiệu lực. **Đề nghị Nam bổ sung `ReportPeriod` vào D6** — tới lúc đó TC test theo C13 (xem §2.0).

**Test Steps:** `POST` với `period` ∈ {`"Weekly"`, `"weekly"`, `"WEEKLY"`, `"Daily"`, `"Monthly"`} → assert parse + assert dạng trả về.

**Expected Result (PASS):**
- 4 giá trị đầu → parse thành công; DB lưu enum `WEEKLY`/`DAILY`
- Response trả **`"Weekly"`**/**`"Daily"`** (chữ đầu hoa) — dạng FE đang đọc
- `"Monthly"` → **422** (ngoài 2 giá trị)
- ⚠️ TC ghi rõ: nếu Nam bổ sung `ReportPeriod` vào D6 thì **sửa TC này trước** (assert `"WEEKLY"`), rồi mới sửa code

**Expected Result (FAIL):**
- Trả `"WEEKLY"` khi D6 chưa bao gồm `ReportPeriod` ⇒ FE hiện tại render sai nhãn (mock đọc `"Weekly"`)
- Case-sensitive ⇒ FE gửi `"weekly"` bị 422 dù đúng nghĩa

**Current Status:** 🔴 Not written

---

#### MF12-TC-028 — Nhận id client, trùng → 409 (D5)

**Severity:** `MEDIUM`
**Feature Under Test:** `create()` của `checklist-groups` và `sent-reports`
**Test File:** `platform/CodePolicyTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-32`, `COND-30` · **Constraint Ref:** **D5 + D1**
**Oracle Source:** **D5** nguyên văn — *"Nếu request body có `id` → dùng làm **`code`**, kiểm trùng → trùng thì **409** với mã `<MODULE>-409`… Nếu vắng → server tự sinh theo `code_sequences` (D1)… Backend **luôn** trả `id` trong response 201"* + **D1** (`INSERT … RETURNING` chống trùng khi song song)

**Test Steps:**
1. `POST /checklist-groups` với `{"id":"CLG-9"}` → lần 1.
2. Lặp lại cùng `id` → lần 2.
3. `POST` **không** có `id`.
4. 2 request **song song** không có `id`.

**Expected Result (PASS):**
- (1) → **201**, response `"id": "CLG-9"`; DB `code = "CLG-9"`, `id` (UUID) do JPA sinh
- (2) → **409** mã `AIS-409`; **không** tạo bản ghi thứ 2, **không** ghi đè
- (3) → **201** với `code` server sinh; response **luôn có** `id`
- (4) → 2 `code` **khác nhau**
- ⚠️ Trùng `code` xét trong phạm vi **`UNIQUE(school_id, code)`** cho `checklist_groups`/`sent_reports`, nhưng **`UNIQUE(code)`** cho `external_integrations` (L11) — TC kiểm cả hai kiểu

**Expected Result (FAIL):**
- Đổi `id` client gửi ⇒ FE điều hướng ngay sau `onSuccess` bằng id mình gửi → 404
- 2 request song song ra cùng `code` ⇒ trùng mã

**Current Status:** 🔴 Not written

---

#### MF12-TC-029 — Thống kê UC-150 dùng RoleKey kebab-case (D6)

**Severity:** `MEDIUM`
**Feature Under Test:** `SystemMetricsService.usage()`
**Test File:** `metrics/UsageStatisticsTest.java`
**TDD Phase:** 🔴 RED · ⚠️ **BLOCKED bởi L12/CF-19** (màn hardcode, chưa có API)
**Condition Ref:** `COND-33` · **Constraint Ref:** **`00_Common_Conventions.md` D6**
**Oracle Source:** **D6** — `RoleKey` = `system-admin` · `school-admin` · `teacher` · `parent` · `medical-staff` · `service-caterers` · `supplier` (**kebab-case**, *"đồng thời là URL segment, nên chốt kebab-case"*); ⚠️ D6 ghi rõ *"FE không có key cho `"Raw Food Supplier"` — chốt `supplier` = NCC nguyên liệu"* + **L12/CF-19** (`ROLE_KEYS` hiện hardcode trong `usage-statistics/page.tsx:26`)

**Test Steps:**
1. `GET /system/usage` → kiểm tập khoá của nhóm theo role.
2. So với `RoleKey` của D6.

**Expected Result (PASS):**
- Đúng **7** khoá, **kebab-case**, khớp 100% bảng D6
- **Không** dùng nhãn BR-007 (`"Medical Staff"`, `"School Administration"`) làm khoá
- **Không** dùng `SCREAMING_SNAKE` cho `RoleKey` (D6 chốt kebab-case cho enum này, khác 7 enum còn lại)
- Enum lấy từ **`common/enums/RoleKey`** (Nam sở hữu):
  ```bash
  grep -rn "enum RoleKey" src/main/java/com/sfms/mf12/   # Expected: no output (D6)
  ```

**Expected Result (FAIL):**
- Dùng nhãn tiếng Anh có khoảng trắng ⇒ FE không map được, và khoá không dùng làm URL segment được
- Khai lại enum trong module ⇒ 2 bộ giá trị sẽ lệch (đúng vấn đề D6 ra đời để giải quyết)

**Current Status:** 🔴 Not written

---

#### MF12-TC-030 — Retention audit ≥ 12 tháng (BR-171/BR-177)

**Severity:** `MEDIUM`
**Feature Under Test:** cấu hình `audit.retention-months` + job dọn dẹp (nếu có)
**Test File:** `audit/AuditRetentionTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-45`
**Oracle Source:** **BR-171** (*"Phải có **chính sách lưu trữ nhật ký** xác định thời gian tối thiểu lưu giữ"*) + **BR-177** (*"Báo cáo đã tạo lưu tối thiểu **12 tháng**"*) + `TDS` §2.2 (*"Cấu hình `audit.retention-months` (mặc định **12**, khớp BR-177)"*)
**Kỹ thuật:** Boundary Value Analysis

**Test Steps:**
1. Đọc cấu hình mặc định.
2. Seed dòng audit `11 tháng` tuổi và `13 tháng` tuổi.
3. Chạy job dọn dẹp (nếu có) → assert.
4. Thử cấu hình `retention-months = 6` → assert.

**Expected Result (PASS):**
- (1) → mặc định **12** (không phải 1, 3, hay 24)
- (3) → dòng **11 tháng** vẫn còn; dòng **13 tháng**: **giữ hoặc archive**, nhưng ⚠️ **KHÔNG DELETE** khỏi `audit_logs` (BR-162 nói *"không ai chỉnh sửa/**xóa**"*) — TC assert xoá bằng **archive sang bảng/lưu trữ ngoài** hoặc **không làm gì**, tuyệt đối không `DELETE`
- (4) → cấu hình < 12 bị **từ chối lúc khởi động** (fail-fast), vì BR-177 quy định **tối thiểu** 12
- ⚠️ **Xung đột nội tại cần ghi rõ:** BR-171 nói có chính sách retention (hàm ý xoá sau hạn) nhưng BR-162 nói audit **bất biến, không xoá**. TC này khoá theo **BR-162** (không DELETE) và **ghi vào biên bản review cho Huy** để chốt cơ chế archive.

**Expected Result (FAIL):**
- Job `DELETE FROM audit_logs` ⇒ vi phạm BR-162 và **phá vỡ chuỗi hash** (`TC-012b` sẽ báo `valid=false` sau mỗi lần dọn) — hai luật đánh nhau, phải chốt trước khi implement
- Cấu hình 6 tháng chạy được ⇒ vi phạm BR-177

**Current Status:** 🔴 Not written
**Implementation Note:** ⚠️ **Cần Huy chốt** cơ chế: archive sang cold storage (giữ hash chain) hay giữ vĩnh viễn. Không tự implement `DELETE`.

---

### 4.2. SECURITY TEST CASES

> Layer `com.sfms.mf12.web` qua **MockMvc** + JWT giả lập (`RoleKey` **kebab-case** theo **D6**).
> Oracle phân quyền duy nhất: **`TDS-MF12_Backend.md` §16** (7 role × 15 nhóm endpoint + bảng field-level 4 nhóm + **2 điểm không khớp FE**: CF-20, CF-21).

---

#### MF12-TC-SEC-001 — Không JWT → 401 trên MỌI endpoint (trừ public)

**Severity:** `CRITICAL` · **OWASP:** `A07:2021` · **CWE:** `CWE-306`
**Legal:** PDPD — audit chứa actor + IP; integration chứa credential
**Feature Under Test:** filter chain × **31 endpoint** §9.2
**Test File:** `web/PlatformSecurityTest.java` · **TDD Phase:** 🔴 RED · **Condition Ref:** `COND-37`
**Oracle Source:** `TDS` §4.3 + **MSG-004** = `"Session expired. Please log in again."` + `TDS` §10 → `AIS-004`, HTTP **401** + **D2** envelope + §9.2 #12 (**`GET /public/password-policy` là endpoint public duy nhất**)

**Test Steps (Attack Simulation):** `@ParameterizedTest @MethodSource("allMf12Endpoints")` — 31 endpoint, gọi không header `Authorization`.

**Expected Result (PASS = hệ thống an toàn):**
- **401** cho **30/31** endpoint; `$.error.code == "AIS-004"`, `$.error.message` = MSG-004 nguyên văn
- **`GET /api/v1/public/password-policy` → 200** (public duy nhất — CF-20/L21)
- Đối chứng: cùng endpoint **có** JWT hợp lệ trả ≠ 401 (chống Red Gate bẫy #3)

**Expected Result (FAIL = lỗ hổng tồn tại):**
- Bất kỳ endpoint trả 200 ⇒ `audit_logs` (actor + IP của toàn hệ thống), `error_logs` (stack trace), `integrations` (credential) đọc được ẩn danh — MF12 là module lộ nhiều nhất nếu authn hỏng
- `GET /security-policies` trả 200 khi không JWT ⇒ chính là lỗ hổng CF-20 mà `TC-SEC-013` chặn

**Current Status:** 🔴 Not written
**Implementation Note:** `allMf12Endpoints()` là **một** danh sách hằng dùng chung với `TC-SEC-015`.

---

#### MF12-TC-SEC-002 — Non-SystemAdmin bị chặn nhóm quản trị hệ thống

**Severity:** `CRITICAL` · **OWASP:** `A01:2021` · **CWE:** `CWE-862`
**Test File:** `web/PlatformAuthorizationMatrixTest.java` · **TDD Phase:** 🔴 RED · **Condition Ref:** `COND-37`
**Oracle Source:** `TDS` §16 — `audit-logs`, `error-logs`, `integrations`, `security-policies`, `system/*` đều **chỉ ✅ ở cột SYS ADMIN** + **BR-175** + `TDS` §13 hàng `SEC-12-02` + `AIS-005` (MSG-005), HTTP **403**
**⚠️ Logic Issue:** **L27/CF-21** — `school-admin/partnerships/page.tsx:189` dùng `<DataTable resource="audit-logs">` ⇒ School Admin **đang** đọc audit log ở FE, trái §16.

**Test Steps:** 6 role ≠ `system-admin` → gọi lần lượt `GET /audit-logs`, `/error-logs`, `/integrations`, `/security-policies`, `/system/activities`, `/system/status`, `/system/usage`, `/system/backups`.

**Expected Result (PASS):**
- **403** `AIS-005` cho **6 role × 8 endpoint = 48 tổ hợp**
- `system-admin` → **200** (đối chứng)
- ⚠️ **Ca School Admin × `audit-logs` tách riêng thành 2 TC loại trừ nhau, cả hai `@Disabled("Chờ CF-21 — Nam chốt")`:**
  - `TC-SEC-002a`: assert **403** — nếu Nam chốt *"chỉ System Admin"* (theo §16 + UC-106)
  - `TC-SEC-002b`: assert **200 nhưng chỉ scope trường mình** — nếu Nam chốt theo BR-176
  - Bỏ `@Disabled` cho **đúng một** TC sau khi Nam quyết; TC còn lại chuyển `status=deleted` kèm ghi chú

**Expected Result (FAIL):**
- Medical/Caterer/Teacher/Parent/Supplier đọc được `integrations` ⇒ thấy được endpoint + `credentialMasked` của dịch vụ ngoài
- Khoá cứng một bên của CF-21 mà chưa có Nam chốt ⇒ vi phạm policy §2 (không tự chọn bên đúng)

**Current Status:** 🔴 Not written

---

#### MF12-TC-SEC-003 — Non-MedicalStaff bị chặn checklist + gửi báo cáo

**Severity:** `HIGH` · **OWASP:** `A01:2021` · **CWE:** `CWE-863`
**Test File:** `web/PlatformSecurityTest.java` · **TDD Phase:** 🔴 RED · **Condition Ref:** `COND-37`
**Oracle Source:** `TDS` §16 — ~~`POST/PUT/DELETE checklist`~~ *(→ MF07, `M12-D02`)* và `POST /sent-reports/{compliance,violations}` **chỉ ✅ ở cột MEDICAL STAFF** *(kể cả **SYS ADMIN là ❌**)* + **BR-210** (*Medical Staff CRUD **không cần** phê duyệt của School Administration hay System Admin*) + `TDS` §13 hàng `SEC-12-03`

**Test Steps:** 6 role ≠ `medical-staff` → `POST /checklist-groups`, `PUT`, `DELETE`, `POST /sent-reports/compliance`, `POST /sent-reports/violations`.

**Expected Result (PASS):**
- **403** `AIS-005` cho tất cả, **bao gồm `system-admin`** — §16 ghi ❌ cho SYS ADMIN ở hàng CRUD checklist. TC khẳng định rõ vì đây là chỗ dễ nới lỏng sai thành "admin làm được tất"
- `medical-staff` → 200/201
- `GET /checklist-groups` thì `system-admin` **✅ được đọc** (§16 hàng đọc) — TC khẳng định ranh giới **đọc được, không CRUD được**

**Expected Result (FAIL):**
- `system-admin` CRUD được checklist ⇒ trái BR-210 (*không cần phê duyệt* nghĩa là **quyền thuộc Medical Staff**, không phải quyền chia sẻ với admin)
- School Admin gửi được báo cáo ⇒ báo cáo mất tính "Medical Staff chủ động gửi cho School Administration"

**Current Status:** 🔴 Not written

---

#### MF12-TC-SEC-004 — Parent PHẢI gọi được POST /ai/chat (không over-block)

**Severity:** `HIGH` · **OWASP:** `A01:2021`
**Test File:** `web/PlatformAuthorizationMatrixTest.java` · **TDD Phase:** 🔴 RED · ⚠️ BLOCKED bởi L6
**Condition Ref:** `COND-37`
**Oracle Source:** `traceability_matrix.md` §1.12 — UC-012 actor = **"User"** (mọi role) + `TDS` §16 hàng `POST /ai/chat` — **✅ ở CẢ 7 cột** + `TDS` §13 hàng `SEC-12-04` (*"Parent gọi `POST /ai/chat` → **✅ 200**"*)
**Kỹ thuật:** Equivalence Partitioning (ca **dương** — chống over-block)

**Test Steps:** 7 role lần lượt `POST /ai/chat`.

**Expected Result (PASS):**
- **7/7 role → 200**, kể cả `parent`, `teacher`, `supplier`
- Mỗi role chỉ thấy conversation **của mình** (`TC-SEC-005`)
- Rate limit 20/min áp **theo user**, không theo role (`TC-SEC-010`)

**Expected Result (FAIL):**
- Chặn Parent/Supplier ⇒ **over-block**: hiểu sai actor "User" thành "nhân viên nội bộ"; phụ huynh mất chức năng UC-012 mà traceability đã cấp
- ⚠️ Đây là TC **ngược chiều** với hầu hết TC bảo mật khác — dễ bị implement sai theo hướng "an toàn hơn thì chặn hết"

**Current Status:** 🔴 Not written

---

#### MF12-TC-SEC-005 — User A đọc conversation của B → 403

**Severity:** `HIGH` · **OWASP:** `A01:2021 (IDOR)` · **CWE:** `CWE-639`
**Test File:** `web/AiAccessSecurityTest.java` · **TDD Phase:** 🔴 RED · ⚠️ BLOCKED bởi L6
**Condition Ref:** `COND-05`
**Oracle Source:** `TDS` §16 (`✅ Own` **cả 7 cột**, gồm SYS ADMIN) + `TDS` §13 hàng `SEC-12-05` + **MSG-005** → `AIS-005`, HTTP **403**

**Test Steps:** `FX-020` (caterer) vs `FX-021` (medical); và `system-admin` đọc của người khác.

**Expected Result (PASS):** **403** `AIS-005` ở cả 2 ca; filter theo `user_id` ở Specification. `system-admin` **cũng 403**.
**Expected Result (FAIL):** admin đọc được ⇒ vượt §16, rủi ro PDPD (hội thoại có thể chứa dữ liệu nghiệp vụ nhạy cảm).

**Current Status:** 🔴 Not written

---

#### MF12-TC-SEC-006 — Grep response + log: KHÔNG có credential

**Severity:** `CRITICAL` · **OWASP:** `A02:2021` · **CWE:** `CWE-532` + `CWE-522`
**Legal:** BR-164
**Test File:** `web/CredentialLeakTest.java` · **TDD Phase:** 🔴 RED · **Condition Ref:** `COND-38`
**Oracle Source:** **BR-164** + `TDS` §14.2 nguyên văn — *"BR-164: credential KHÔNG xuất hiện trong log — **Expected: No output**"* + `TDS` §13 hàng `SEC-12-06` (*"không xuất hiện `AIza`/`sk-`/password"*) + `ADR-M12-03`

**Test Steps (Attack Simulation):**
1. Chạy toàn bộ luồng UC-040: `test-connection` → `save` → `GET /integrations` → `GET /integrations/{code}`.
2. Bắt **toàn bộ** response body + log (`LogCaptor` + file log).
3. Kiểm cả **error response** (gây lỗi `AIS-164` rồi kiểm message).
4. Kiểm `GET /error-logs` (stack trace có thể chứa credential).

**Expected Result (PASS):**
- Không response nào chứa `"AIzaTEST-not-a-real-key-000000000000000"`, tiền tố `AIza`, `sk-`, hay `password=`
- Log **không** chứa các chuỗi trên
- (3) → message lỗi `AIS-164` **không** chứa ciphertext lẫn khoá
- (4) → `stackTrace` trong `error_logs` **không** chứa credential (⚠️ ca dễ sót: exception của HTTP client thường in cả URL + header có khoá)
- Kiểm tự động (CI, theo §14.2):
  ```bash
  grep -rniE "AIza[A-Za-z0-9_-]{10,}|sk-[A-Za-z0-9]{20,}|password=" target/*.log target/test-responses/
  # Expected: no output
  ```

**Expected Result (FAIL):**
- Bất kỳ chỗ nào có khoá ⇒ **khoá dịch vụ ngoài rò ra log tập trung**; log thường có nhiều người đọc hơn DB. Với Gemini/FCM là rủi ro chi phí + lạm dụng thật.

**Current Status:** 🔴 Not written
**Implementation Note:** cấu hình logger **mask** header `Authorization`/`X-Api-Key` và sanitize URL query trước khi log exception của HTTP client.

---

#### MF12-TC-SEC-007 — Grep log: KHÔNG có nội dung hội thoại AI (BR-173)

**Severity:** `HIGH` · **OWASP:** `A09:2021` · **CWE:** `CWE-532`
**Legal:** **PDPD (Luật 91/2025)** — BR-173
**Test File:** `web/AiPrivacyTest.java` · **TDD Phase:** 🔴 RED · ⚠️ BLOCKED bởi L6
**Condition Ref:** `COND-39`
**Oracle Source:** `TDS` §14.2 nguyên văn — *"BR-173: nội dung hội thoại AI **KHÔNG** ghi ra file log (chỉ lưu DB có kiểm soát) — **Expected: No output**"* + `TDS` §13 hàng `SEC-12-07` + **BR-157** (*lưu trong DB — nơi có phân quyền*, khác với log)

**Test Steps:**
1. Gọi `POST /ai/chat` với prompt đặc trưng (`FX-028`).
2. Bắt log (kể cả `DEBUG`/`TRACE` nếu bật).
3. Kiểm `error_logs` khi AI lỗi (`FX-024` timeout).

**Expected Result (PASS):**
- Log **không** chứa nội dung `prompt` lẫn `response_text`
- Log **được** chứa: `conversationId`, `module`, `status`, `latencyMs`, `actorId` (metadata — cần cho vận hành)
- (3) → khi timeout, `error_logs.message` = *"Gemini AI request timeout sau 10s"* (khớp `FX-005` `ERR-5521`), **không** kèm prompt
- ⚠️ Phân biệt then chốt: **BR-157 nói lưu (vào DB có phân quyền)**, **BR-173 nói không log (ra file)** — hai luật không xung đột, nhưng dễ implement lẫn thành "log cho dễ debug"

**Expected Result (FAIL):**
- Log chứa prompt ⇒ dữ liệu người dùng (có thể gồm thông tin HS trước khi redact) nằm trong log tập trung, ngoài phạm vi phân quyền của `ai_requests`

**Current Status:** 🔴 Not written

---

#### MF12-TC-SEC-008 — File báo cáo xuất ra KHÔNG chứa mã học sinh

**Severity:** `HIGH` · **CWE:** `CWE-359`
**Legal:** **PDPD** — BR-181
**Test File:** `web/ReportExportPrivacyTest.java` · **TDD Phase:** 🔴 RED · **Condition Ref:** `COND-22`
**Oracle Source:** **BR-181** + `TDS` §14.2 nguyên văn — *"BR-181: file báo cáo xuất ra **không chứa mã học sinh** — **Expected: No output**"* + `TDS` §13 hàng `SEC-12-06`-adjacent

**Test Steps:** gửi báo cáo với dữ liệu nguồn có HS → tải PDF + XLSX → extract text → grep.

**Expected Result (PASS):**
- **Cả 2 định dạng** không khớp `HS-\d+[A-Z]-\d+`; không chứa tên HS trong `FX-028`
- File **có** nhãn "Confidential" khi payload có `confidential=true`
- Metadata file (PDF `/Author`, `/Title`; XLSX `docProps/core.xml`) **cũng** không chứa PII — ⚠️ ca dễ sót nhất
- Kiểm tự động (§14.2):
  ```bash
  pdftotext target/test-reports/*.pdf - | grep -E "HS-[0-9]+[A-Z]-[0-9]+"   # Expected: no output
  unzip -p target/test-reports/*.xlsx xl/sharedStrings.xml | grep -E "HS-[0-9]+[A-Z]-[0-9]+"  # Expected: no output
  ```

**Expected Result (FAIL):** file gửi School Administration chứa mã HS ⇒ sự cố PDPD; file này có thể được chuyển tiếp ra ngoài trường.

**Current Status:** 🔴 Not written

---

#### MF12-TC-SEC-009 — audit_logs chứa PII: chỉ System Admin đọc, không lộ qua endpoint khác

**Severity:** `HIGH` · **OWASP:** `A01:2021` · **CWE:** `CWE-359`
**Legal:** **PDPD** — BR-173 (`actor` + `ip` là dữ liệu cá nhân)
**Test File:** `web/AuditPrivacyTest.java` · **TDD Phase:** 🔴 RED · **Condition Ref:** `COND-37`
**Oracle Source:** `TDS` §1 Data Classification (*"`PII` (audit actor, IP — BR-173)"*) + §16 (`GET /audit-logs` = **✅ All** chỉ ở SYS ADMIN) + **BR-173**

**Test Steps (Attack Simulation):**
1. Non-SysAdmin → `GET /audit-logs` → assert.
2. Kiểm `GET /system/activities` (UC-057 aggregate từ `audit_logs`) — có lộ `actor`/`ip` chi tiết không?
3. Kiểm `GET /sent-reports` payload — có nhúng dòng audit không?

**Expected Result (PASS):**
- (1) → 403 (đã cover `TC-SEC-002`)
- (2) → **chỉ số liệu tổng hợp** (đếm theo action/thời gian); **không** trả `actor` email và `ip` từng dòng. ⚠️ Đây là đường rò gián tiếp: UC-057 tổng hợp **từ** `audit_logs` nên dễ vô tình trả cả chi tiết
- (3) → payload báo cáo **không** nhúng dòng audit
- `GET /audit-logs` (SysAdmin) **được** trả `actor` + `ip` đầy đủ — đó là mục đích của UC-106

**Expected Result (FAIL):**
- (2) trả email + IP từng dòng ⇒ **PII rò qua endpoint thống kê** cho role được phép xem thống kê nhưng không được xem audit; và endpoint này §16 cũng chỉ cấp SysAdmin nên hậu quả nhỏ hơn, nhưng vẫn là nguyên tắc tối thiểu hoá dữ liệu

**Current Status:** 🔴 Not written

---

#### MF12-TC-SEC-010 — Rate limit AI: 20 request/phút/user

**Severity:** `MEDIUM` · **OWASP:** `A04:2021 — Insecure Design` · **CWE:** `CWE-770`
**Test File:** `web/RateLimitTest.java` · **TDD Phase:** 🔴 RED · ⚠️ BLOCKED bởi L6
**Condition Ref:** `COND-40`
**Oracle Source:** `TDS` §9.2 #21 — cột `Rate limit` = **`20/min/user`** (in đậm, khác các endpoint khác) + `TDS` §13 hàng `SEC-12-08` (*"21 request/phút → request thứ 21 → **429**"*) + **BR-157** (mọi lần gọi đều archive ⇒ chi phí thật)
**Kỹ thuật:** Boundary Value Analysis

**Test Steps:** 21 request trong 60s bằng **cùng** JWT; rồi 21 request bằng **2 JWT khác nhau** (mỗi user 11).

**Expected Result (PASS):**
- Request 1–20 → 200/503; request **21 → 429** kèm `Retry-After`
- Bộ đếm theo **user** (`sub` trong JWT), **không** theo IP — 2 user × 11 request đều thành công (nhiều người dùng sau cùng NAT trường học)
- Endpoint khác vẫn theo giới hạn §9.2 (`300/60min` GET, `30/min` POST integration, `10/min` PUT security-policies, `10/min` gửi báo cáo)
- Request bị 429 **không** sinh dòng `ai_requests` (không tốn quota Gemini)

**Expected Result (FAIL):**
- Không giới hạn ⇒ một vòng lặp FE làm cạn quota Gemini của cả trường (chi phí thật) và làm phình `ai_requests`
- Đếm theo IP ⇒ cả trường dùng chung 1 IP công cộng bị chặn oan sau 20 request

**Current Status:** 🔴 Not written

---

#### ~~MF12-TC-SEC-011~~ — Medical Staff trường A KHÔNG chạm được checklist trường B

> ⛔ **`@Disabled("M12-D02 — thuoc MF07")` tu 2026-08-04.** Bang `checklist_groups`/`checklist_items` (`V12:209/223`) va endpoint `/api/v1/checklist-groups` da merge o MF07 (`M07-D10`); `TestSpec-MF07` da phu. Giu nguyen van theo Policy 4.4, **khong hien thuc trong `mf12`**.

**Severity:** `CRITICAL` · **OWASP:** `A01:2021 (BOLA)` · **CWE:** `CWE-639`
**Test File:** `web/ChecklistScopeSecurityTest.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-17` · **Constraint Ref:** `TDS §17.1 C12`
**Oracle Source:** **BR-210** (*phạm vi **theo từng trường***) + `TDS` §16 bảng field-level (*"`school_id` **luôn lấy từ JWT**, không nhận từ body (CF-25)"*) + **L23/CF-25**

**Preconditions:** `FX-012` (`SCH-001`), `FX-013` (`SCH-002`), `FX-030` (Medical `SCH-001`), `FX-031` (Medical `SCH-002`).

**Test Steps (Attack Simulation):**
1. `FX-030` → `GET /checklist-groups` → assert.
2. `FX-030` → `PUT /checklist-groups/{code của SCH-002}` → assert.
3. `FX-030` → `DELETE` nhóm của `SCH-002` → assert.
4. `FX-030` → `POST` với body `{"schoolId":"SCH-002"}` → assert.
5. `FX-030` → `GET /checklist-groups?schoolId=SCH-002` → assert.

**Expected Result (PASS):**
- (1) → chỉ nhóm `SCH-001`; `total` khớp
- (2)(3) → **403/404**; DB của `SCH-002` **không** đổi
- (4) → `school_id` ghi là **`SCH-001`** (từ JWT), body bị bỏ qua
- (5) → `schoolId` trong query **bị bỏ qua**; kết quả vẫn chỉ `SCH-001`; `schoolId` nằm trong nhóm key bị loại của Specification
- Filter áp ở **repository**, assert SQL chứa `school_id = ?`

**Expected Result (FAIL):**
- (1) hoặc (5) trả cả 2 trường ⇒ **checklist trường này lộ sang trường khác**; và vì MF07 dùng checklist để chấm điểm bếp, hậu quả lan sang **điểm vệ sinh sai bộ mục** ⇒ Báo cáo Tuân thủ ATTP (gửi cơ quan chức năng) dựa trên checklist không phải của trường mình
- (2) sửa được ⇒ Medical Staff trường A đổi tiêu chí kiểm tra của trường B

**Current Status:** 🔴 Not written

---

#### MF12-TC-SEC-012 — Actuator KHÔNG lộ ra ngoài

**Severity:** `HIGH` · **OWASP:** `A05:2021 — Security Misconfiguration` · **CWE:** `CWE-497 — Exposure of Sensitive System Information`
**Test File:** `web/ActuatorExposureTest.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-41`
**Oracle Source:** `TDS` §13 hàng `SEC-12-09` (*"`GET /actuator/env` với JWT thường → **403**"*) + `ADR-M12-04` (metric đọc từ Actuator ⇒ Actuator **phải** được bảo vệ) + **BR-164** (`/actuator/env` có thể lộ biến môi trường chứa credential)

**Test Steps (Attack Simulation):**
1. JWT `parent` → `GET /actuator/env`, `/actuator/configprops`, `/actuator/heapdump` → assert.
2. **Không** JWT → cùng các endpoint → assert.
3. JWT `system-admin` → `GET /actuator/health` → assert.
4. Kiểm `/actuator/env` có mask giá trị credential không.

**Expected Result (PASS):**
- (1)(2) → **403/401**; `/actuator/heapdump` **không** truy cập được với bất kỳ role qua HTTP
- (3) → 200 (health cần cho `TC-INT-018`)
- (4) → nếu `/actuator/env` bật cho SysAdmin thì giá trị của `SFMS_CRED_MASTER_KEY`, `GEMINI_API_KEY` phải là `"******"`
- Danh sách endpoint Actuator expose là **allow-list tường minh**, không phải `include: "*"`

**Expected Result (FAIL):**
- `/actuator/env` trả plaintext biến môi trường ⇒ **lộ toàn bộ credential** trong một request — vô hiệu hoá BR-164 và toàn bộ `TC-009`…`TC-011`
- `/actuator/heapdump` truy cập được ⇒ dump bộ nhớ chứa khoá đã giải mã

**Current Status:** 🔴 Not written

---

#### MF12-TC-SEC-013 — GET /public/password-policy trả tập con không nhạy cảm

**Severity:** `HIGH` · **OWASP:** `A01:2021` · **CWE:** `CWE-200 — Exposure of Sensitive Information`
**Test File:** `web/PublicPolicySecurityTest.java` · **TDD Phase:** 🔴 RED · ⚠️ endpoint **MỚI** (§9.2 #12)
**Condition Ref:** `COND-42`
**Oracle Source:** **L21/CF-20** nguyên văn — *"Backend mở **`GET /public/password-policy`** trả **tập con không nhạy cảm** (không có `sessionTimeoutMinutes`, `updatedBy`, `updatedAt`); Minh đổi 2 màn `(auth)` sang endpoint mới"* + `TDS` §16 (`security-policies` chỉ SysAdmin) + **BR-006**, **BR-002**
**⚠️ Logic Issue:** **L21/CF-20** — `(auth)/login/page.tsx:38` và `(auth)/reset-password/page.tsx:28` **đang** gọi `GET /security-policies` khi **chưa có JWE**. Hai điều này mâu thuẫn trực tiếp với §16.

**Test Steps:**
1. **Không** JWT → `GET /api/v1/public/password-policy` → assert.
2. Kiểm tập khoá trả về.
3. **Không** JWT → `GET /api/v1/security-policies` → assert.
4. Rate limit endpoint public.

**Expected Result (PASS):**
- (1) → **200** (public)
- (2) → **có**: `passwordMinLength`, `requireSpecialChar`, `requireNumber`, `requireMixedCase`, `noReuseLast3`, `maxLoginAttempts`, `lockoutMinutes` *(2 field cuối cần để màn login hiển thị "còn N lần thử" theo BR-002)*
  **KHÔNG có**: `sessionTimeoutMinutes`, `updatedBy`, `updatedAt`, `id`/`code`
- (3) → **401** — resource gốc **KHÔNG bị hạ quyền** (đây là điểm mấu chốt: giải pháp là thêm endpoint public, **không** mở resource quản trị)
- (4) → rate limit **60/min/IP** (§9.2 #12) — endpoint public cần chống dò

**Expected Result (FAIL):**
- Hạ quyền `GET /security-policies` thành public ⇒ lộ `sessionTimeoutMinutes` (thông tin hữu ích cho tấn công session) và `updatedBy` (định danh System Admin) — cách "sửa" sai mà CF-20 cảnh báo
- Endpoint public trả full object ⇒ same problem
- Không có endpoint public ⇒ 2 màn `(auth)` gãy khi đổi `baseURL`

**Current Status:** 🔴 Not written
**Implementation Note:** ⚠️ **Minh đổi 2 màn `(auth)`** sang endpoint mới trong cùng đợt. **Ưu tiên cao** (§9.5 mục 5 của TDS).

---

#### MF12-TC-SEC-014 — Lách gate kỳ rỗng bằng gọi API trực tiếp

**Severity:** `HIGH` · **OWASP:** `A04:2021` · **CWE:** `CWE-602 — Client-Side Enforcement of Server-Side Security`
**Test File:** `web/ReportGateBypassTest.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-18`, `COND-19` · **Constraint Ref:** `TDS §17.1 C6`
**Oracle Source:** **BR-209** / **BR-214** (*"**hệ thống chặn** gửi"*) + **L22/CF-24** nguyên văn (*"Chặn gửi khi kỳ rỗng **chỉ có ở client**… **gọi thẳng API là lách được**, tạo báo cáo rỗng"*)
**Kỹ thuật:** Error Guessing (bypass UI control)

**Test Steps (Attack Simulation):**
1. Kỳ rỗng → `POST /sent-reports/compliance` **trực tiếp** (không qua UI) → assert.
2. Kỳ rỗng → `POST /sent-reports/violations` → assert.
3. Kỳ rỗng → **đường cũ** `POST /sent-reports` với `reportType='COMPLIANCE'` → assert.
4. Gửi kèm `avgScore: 100` bịa sẵn trong payload → assert.

**Expected Result (PASS):**
- (1) → **422** `AIS-092`; (2) → **422** `AIS-097`
- (3) → **cũng 422** — nếu chỉ chặn endpoint mới thì FE hiện tại (đang dùng `POST /sent-reports`) lách được hoàn toàn
- (4) → số liệu **tính lại từ nguồn**, bỏ qua giá trị client gửi (C11); kỳ rỗng vẫn 422 dù client gửi `avgScore`
- Không dòng `sent_reports` nào, không file nào, không thông báo nào

**Expected Result (FAIL):**
- Bất kỳ đường nào tạo được ⇒ **báo cáo rỗng/bịa số gửi tới School Administration**; đây là lỗ hổng **đang tồn tại**, không phải giả thuyết

**Current Status:** 🔴 Not written
**Implementation Note:** gate ở **service** (`ComplianceReportSendService`/`ViolationReportSendService`) để cả 2 controller đi qua cùng chỗ.

---

#### MF12-TC-SEC-015 — Ma trận 7 role × 31 endpoint khớp §16 100%

**Severity:** `CRITICAL` · **OWASP:** `A01:2021` · **CWE:** `CWE-863`
**Test File:** `web/PlatformAuthorizationMatrixTest.java` · **TDD Phase:** 🔴 RED · **Condition Ref:** `COND-43`
**Oracle Source:** **`TDS-MF12_Backend.md` §16** (15 hàng × 7 cột + hàng public) dẫn từ **BR-007** (RBAC) và **BR-175** (quyền xem báo cáo) + `TDS` §13 hàng `SEC-12-10`
**Kỹ thuật:** Data-driven / Parameterized (**217 tổ hợp**)

**Test Steps:**
1. Khai bảng §16 thành `Map<Endpoint, Set<RoleKey>>` **trong test** (oracle nằm trong test, **không** đọc từ production — chống tautology).
2. `@ParameterizedTest` mọi (role × endpoint).
3. role ∈ allowed → **không** 403; role ∉ allowed → **403**.

**Expected Result (PASS):**
- 217/217 khớp §16; in bảng lệch `role | endpoint | expected | actual` nếu có
- **`SYS_ADMIN` bị ❌** ở: `POST /sent-reports/{compliance,violations}`, `GET /report-center` — least privilege. *(~~CRUD checklist~~ → ma trận của MF07, `M12-D02`)*
- **`SUPPLIER`/`TEACHER`/`PARENT`** chỉ ✅ ở `POST /ai/chat` + `GET /ai/conversations` (Own) + endpoint public
- **`POST /sent-reports/incidents` = ❌ cho MỌI role** (409 Deprecated — `TC-023`)
- Hàng `GET /public/password-policy` = 🌐 cho tất cả (kể cả không JWT — `TC-SEC-013`)
- 2 ô CF-20/CF-21 xử lý theo `TC-SEC-013` / `TC-SEC-002a-b`

**Expected Result (FAIL):** bất kỳ ô lệch ⇒ ghi vào §8 và **chặn** Nam final approve (§6).

**Current Status:** 🔴 Not written
**Implementation Note:** nếu §16 đổi, **sửa bảng trong test trước** (đỏ) rồi mới sửa `@PreAuthorize`.

---

#### MF12-TC-SEC-016 — Phân quyền THEO FIELD (4 nhóm §16)

**Severity:** `HIGH` · **OWASP:** `A01:2021` · **CWE:** `CWE-863`
**Test File:** `web/FieldLevelAuthorizationTest.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-17`, `COND-15` · **Constraint Ref:** `TDS §17.1 C11, C12`
**Oracle Source:** `TDS` **§16 bảng "Phân quyền theo FIELD"** (4 nhóm) nguyên văn
**Kỹ thuật:** Decision Table Testing

| # | Resource | Field trong body | Role | Expected |
|---|---|---|---|---|
| a | `integrations` | `endpoint`, `apiKey` | `system-admin` | **200** |
| b | `integrations` | `connected: true` | `system-admin` | **200** nhưng **bỏ qua** — suy từ `last_test_passed` (C11) |
| c | `integrations` | bất kỳ | `medical-staff` | **403** |
| d | `security-policies` | mọi field | `system-admin` | **200** |
| e | `security-policies` | mọi field | `school-admin` | **403** |
| f | `checklist-groups` | `schoolId: "SCH-002"` | Medical `SCH-001` | `school_id` = **`SCH-001`** (từ JWT), body bỏ qua |
| g | `sent-reports` | `sentBy: "người khác"` | Medical | **bỏ qua** — lấy từ JWT |
| h | `sent-reports` | `schoolId: "SCH-002"` | Medical `SCH-001` | **bỏ qua** — lấy từ JWT |

**Expected Result (PASS):** đúng 8 hàng; ở mọi ca "bỏ qua", DB ghi giá trị **từ JWT/suy luận**, không từ body.

**Expected Result (FAIL):**
- (b) tin `connected` client ⇒ **BR-163 vô hiệu hoàn toàn** bằng 1 field
- (g) tin `sentBy` ⇒ **báo cáo mạo danh người khác** gửi tới School Administration — mất tính chịu trách nhiệm
- (f)/(h) tin `schoolId` ⇒ cross-tenant (đã cover `TC-SEC-011`)

**Current Status:** 🔴 Not written

---

#### MF12-TC-SEC-017 — Injection qua field lọc động của audit-logs

**Severity:** `HIGH` · **OWASP:** `A03:2021 — Injection` · **CWE:** `CWE-943`
**Test File:** `web/AuditSpecInjectionTest.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-36`
**Oracle Source:** `00_Team_Work_Assignment_Plan.md` §2b-2 (danh sách param hợp lệ + nhóm `CONTROL`) + §2b-4 (mẫu `Specification`) + `TDS` §9.2 (`SEARCH_FIELDS` của `audit-logs` = `{actorLabel, action, resource, ip}`)

**Test Steps (Attack Simulation):**
1. `GET /audit-logs?entryHash=abc` (lọc theo cột toàn vẹn nội bộ) → assert.
2. `GET /audit-logs?nonExistentColumn=x` → assert.
3. `GET /audit-logs?search=' OR '1'='1` → assert.
4. `GET /audit-logs?prevHash=` → assert.
5. `GET /error-logs?search=<chuỗi chỉ có trong stackTrace>` → assert.

**Expected Result (PASS):**
- (1)(4) → **400** — `entry_hash`/`prev_hash` **không** thuộc allow-list field lọc (lọc theo hash cho phép dò chuỗi toàn vẹn)
- (2) → **400** (không phải 500 `IllegalArgumentException` thô từ `root.get()`)
- (3) → **200**, không trả toàn bảng; SQL dùng **parameter binding** (assert log `org.hibernate.SQL` không chứa chuỗi nội suy)
- (5) → `total == 0` — `search` **chỉ** trên `{message, module, path}` (§9.2), **không** quét `stackTrace`. ⚠️ Lưu ý: `stackTrace` **được trả về** (C14/`TC-INT-007`) nhưng **không được search** — hai việc khác nhau
- Nhóm `CONTROL` (`page`, `pageSize`, `search`, `dateFrom`, `dateTo`) bị loại khỏi lọc field

**Expected Result (FAIL):**
- Lọc theo `entry_hash` được ⇒ hỗ trợ tấn công dò/xác nhận nội dung audit
- (5) khớp ⇒ search quét `stackTrace` ⇒ full table scan trên cột text lớn + rò rỉ nội dung nội bộ qua tìm kiếm
- 500 kèm stacktrace ⇒ lộ tên cột/schema

**Current Status:** 🔴 Not written

---

#### MF12-TC-SEC-018 — GET /error-logs KHÔNG lộ dữ liệu vượt phạm vi qua stackTrace

**Severity:** `MEDIUM` · **OWASP:** `A01:2021` · **CWE:** `CWE-209 — Generation of Error Message Containing Sensitive Information`
**Test File:** `web/ErrorLogPrivacyTest.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-28`, `COND-38`
**Oracle Source:** **C14** (*`GET /error-logs` **phải** trả đủ `stackTrace` + `sessionInfo`*) **×** **BR-164** (credential không lộ) **×** **BR-173** (không lộ PII) — ba luật giao nhau tại đúng endpoint này
**⚠️ Đây là TC "giao điểm luật":** C14 buộc **trả** stack trace đầy đủ, còn BR-164/BR-173 buộc **không lộ** credential/PII. Cách duy nhất thoả cả ba là **sanitize lúc GHI** `error_logs`, không phải lúc đọc.

**Test Steps:**
1. Gây lỗi HTTP client tới Gemini (URL có `?key=AIza...`) → kiểm `error_logs.stackTrace`.
2. Gây lỗi khi xử lý dữ liệu HS → kiểm `stackTrace` có mã HS không.
3. `GET /error-logs` bằng SysAdmin → kiểm **có** đủ `stackTrace` + `sessionInfo` (C14).
4. Non-SysAdmin → assert 403.

**Expected Result (PASS):**
- (1) → `stackTrace` **đã sanitize**: URL query có `key=******`; **không** chứa `AIza...`
- (2) → `stackTrace` **không** chứa `HS-\d+[A-Z]-\d+`
- (3) → **vẫn có đủ** `stackTrace` (đã sanitize) + `sessionInfo` — không lược field (C14/L20)
- (4) → 403
- ⚠️ Cân bằng: sanitize **giữ được** thông tin gỡ lỗi (tên class, số dòng, loại exception) — chỉ mask giá trị nhạy cảm

**Expected Result (FAIL):**
- `stackTrace` chứa credential ⇒ **BR-164 bị phá qua đường error log**, và error log thường được xuất ra ngoài khi báo lỗi nhà cung cấp
- Lược `stackTrace` để "cho an toàn" ⇒ vi phạm C14, màn chi tiết lỗi (UC-111 bước 5) trống

**Current Status:** 🔴 Not written
**Implementation Note:** sanitize ở **`ErrorLogWriter`** (lúc ghi), dùng cùng bộ mask với logger của `TC-SEC-006`.

---

#### MF12-TC-SEC-019 — audit_logs bất biến qua HTTP: mọi verb ghi đều 405 *(mới — L35/`M12-D13`)*

**Severity:** `CRITICAL`
**Feature Under Test:** `/api/v1/audit-logs**` — bề mặt HTTP
**Test File:** `web/AuditImmutabilityTest.java` · **TDD Phase:** 🔴 RED
**Constraint Ref:** `TDS §17.1 C2` · **Oracle Source:** **BR-162** nguyên văn (*"không ai có thể chỉnh sửa hoặc xóa bản ghi audit"*) + **BR-172** + `TDS-MF12` §9.2 hàng 3 + `ADR-M12-02` *(bản sửa 2026-08-04)* + **L13/CF-9**

**⚠️ TC này THAY THẾ vế `REVOKE` đã gỡ.** `TestSpec` v1.0 dựa lớp bảo vệ số 1 vào `REVOKE UPDATE, DELETE ON audit_logs FROM sfms_app`. Bước 0 cho thấy role `sfms_app` **không tồn tại** ⇒ assert theo nó **fail vĩnh viễn** (hoặc tệ hơn: pass giả vì query trả 0 dòng và test viết là `assertThat(grants).isEmpty()`). Bất biến giờ được chốt ở **tầng HTTP** (TC này) và **tầng dữ liệu** (`TC-INT-003`, chuỗi hash).

**Preconditions:** tài khoản seed `SYSTEM_ADMIN` thật (vai trò **cao nhất** — chốt rằng đây không phải chuyện phân quyền); ≥1 dòng `audit_logs` có `code`.

**Test Steps:**
1. `PUT /api/v1/audit-logs/{code}` body hợp lệ.
2. `PATCH /api/v1/audit-logs/{code}`.
3. `DELETE /api/v1/audit-logs/{code}`.
4. `POST /api/v1/audit-logs` body một dòng audit hợp lệ.
5. `DELETE /api/v1/audit-logs` *(xoá hàng loạt)*.
6. Sau cả 5 bước: `SELECT count(*)` và `SELECT entry_hash` của dòng bị nhắm.

**Expected Result (PASS):**
- (1)–(5) → **405 Method Not Allowed**, `$.error.code == "IAM-162"` *(**`IAM-`**, không phải `AIS-` — `ModuleCodeResolver` gán `audit-logs → IAM` vì bảng thuộc MF01; `M12-D10`)*
- (4) → 405 **kể cả với System Admin**: FE **không được** ghi audit qua HTTP, backend tự ghi qua `AuditService` (`M12-D08`)
- (6) → `count(*)` **không đổi**, `entry_hash` của dòng bị nhắm **không đổi**
- **405, không phải 403.** 403 nói *"bạn không có quyền"* — hàm ý ai đó **có** quyền. 405 nói *"thao tác này không tồn tại"*, đúng BR-162

**Expected Result (FAIL):**
- Trả **403** ⇒ mô tả sai bản chất: gợi ý rằng cấp quyền đủ cao là sửa được audit
- Trả **404** ⇒ Spring không có handler và không có `@RequestMapping` bắt — vẫn an toàn, nhưng FE không phân biệt được "sai đường dẫn" với "cấm theo thiết kế"; và mã lỗi sẽ là `IAM-404` chứ không phải `IAM-162`
- Bất kỳ verb nào trả **2xx** ⇒ vi phạm BR-162 trực tiếp

**Current Status:** 🔴 Not written

---

### 4.3. INTEGRATION TEST CASES

> Nền: `@SpringBootTest(webEnvironment = RANDOM_PORT)` + **Testcontainers `PostgreSQLContainer("postgres:16-alpine")`** (`FX-033`) + Flyway **`V1`…`V21` (toàn bộ schema đã merge) + `V22`/`V23` của MF12**. Timeout khởi động **120s**.
> ⚠️ **`M12-D12`/L36:** số V là **V22/V23**, không phải V22/V23 — và IT của MF12 **phải chạy trên toàn bộ V1–V21**, không chỉ `V1`: `V22` `ALTER` bảng `audit_logs` của `V1`, còn `TC-CON-009` cần cả `V12` của MF07 có mặt để phát hiện va chạm `checklist_groups`.
> ⚠️ **D1:** DB assertion dùng **`WHERE code = ?`**.
> ⛔ ~~Bắt buộc dùng role `sfms_app` hạn chế~~ — **`M12-D13`**: role không tồn tại. `TC-INT-003` đã viết lại theo chuỗi hash, không phụ thuộc quyền DB; đối chứng dương của nó là bước (1) *(chuỗi lành ⇒ `valid=true`)*.
> ⚠️ **KHÔNG** dùng `prisma db push`/`prisma.client` — assert bằng `JdbcTemplate`/repository.

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
abstract class AbstractMf12IT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:16-alpine").withReuse(true);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        // M12-D13: dự án KHÔNG có role hạn chế `sfms_app` — dùng role mặc định của container.
        r.add("spring.datasource.url",      POSTGRES::getJdbcUrl);
        // r.add("spring.datasource.username", …) — BỎ: Testcontainers tự cấp (M12-D13)
        r.add("spring.flyway.user",         POSTGRES::getUsername);   // owner chạy migration
        r.add("spring.flyway.locations",    () -> "classpath:db/migration");
    }

    @Autowired protected MockMvc      mvc;
    @Autowired protected JdbcTemplate jdbc;        // role mặc định của container (M12-D13)

    /** JdbcTemplate quyền OWNER — chỉ dùng để reset fixture và mô phỏng tấn công DBA. */
    protected JdbcTemplate ownerJdbc() { /* ... */ return null; }

    /**
     * Cách ly: audit_logs CHỈ TRUNCATE trên Testcontainers — quy ước, không phải quyền (M12-D13)
     * → phải dùng ownerJdbc(). CHỈ áp cho DB Testcontainers.
     * ⚠️ Trên môi trường thật, audit_logs là bằng chứng vận hành — CẤM xoá (BR-162, §7 R1).
     */
    @BeforeEach
    void resetMf12() {
        ownerJdbc().execute("""
            TRUNCATE ai_requests, ai_conversations, error_logs,
                     checklist_item_results, checklist_items, checklist_groups,
                     sent_reports, external_integrations, audit_logs CASCADE
            """);
    }
}
```

---

#### MF12-TC-INT-001 — Chat trọn vòng: 2 lượt cùng conversationId

**Severity:** `HIGH`
**Feature Under Test:** `POST /api/v1/ai/chat` ×2
**Test File:** `integration/AiChatIT.java` · **TDD Phase:** 🔴 RED · ⚠️ BLOCKED bởi L6
**Condition Ref:** `COND-01` · **Constraint Ref:** `TDS §17.1 C1`
**Oracle Source:** **BR-157** + `TDS` §13 hàng `IT-12-01` (*"200 ×2; `ai_requests` 2 dòng; `ai_conversations.last_message_at` cập nhật"*)

**Test Steps:** `POST /ai/chat` (không `conversationId`) → lấy `conversationId` → `POST` lần 2 với cùng id → assert DB.

**Expected Result (PASS):**
- 200 ×2; **1** dòng `ai_conversations`, **2** dòng `ai_requests` trỏ cùng conversation
- `last_message_at` = thời điểm lượt 2 (ISO — **D4**)
- `ai_requests.prompt` đã redact (`TC-004`)
- `GET /ai/conversations/{code}` trả **2** lượt theo thứ tự thời gian

**DB Assertion:**
```java
assertThat(jdbc.queryForObject("""
    SELECT count(*) FROM ai_requests r JOIN ai_conversations c ON r.conversation_id = c.id
     WHERE c.code = ?""", Integer.class, convCode)).isEqualTo(2);   // D1: WHERE code
```

**Expected Result (FAIL):** tạo 2 conversation ⇒ lịch sử UC-119 phân mảnh, người dùng không thấy mạch hội thoại.

**Current Status:** 🔴 Not written

---

#### MF12-TC-INT-002 — MF05 gọi AI qua port → sinh ai_requests module='MF05_MENU'

**Severity:** `CRITICAL`
**Feature Under Test:** `AiAssistPort` dùng chung — MF05 `POST /menus/{code}/ai-suggest`
**Test File:** `integration/AiPortSharedIT.java` · **TDD Phase:** 🔴 RED · ⚠️ BLOCKED bởi L6 + MF05 CF-13
**Condition Ref:** `COND-48` · **Constraint Ref:** `TDS §17.1 C1`
**Oracle Source:** **BR-157** (*"**Toàn bộ** hội thoại… **gồm cả đề xuất thực đơn**"* — nêu đích danh MF05) + `ADR-M12-01` (`AiAssistPort` là **cổng AI duy nhất**) + `TDS` §13 hàng `IT-12-02` (*"`ai_requests` có dòng `module='MF05_MENU'`"*) + `MF05` C6/`ADR-M05-03`
**⚠️ Ranh giới:** TC này **giao** MF12 ↔ MF05. Đối chiếu `MF05-TC-014`/`MF05-TC-015` — MF05 test *fallback khi AI lỗi*, MF12 test *archive bắt buộc*. Hai TestSpec phải khớp.

**Test Steps:**
1. Gọi endpoint AI của **MF05** (`POST /api/v1/menus/{code}/ai-suggest`).
2. Kiểm `ai_requests` của MF12.
3. Static check: MF05 **không** gọi Gemini trực tiếp.

**Expected Result (PASS):**
- 1 dòng `ai_requests` với **`module = 'MF05_MENU'`**, `prompt` đã redact, `status=SUCCESS`
- MF05 **không** import SDK/`GeminiClient`:
  ```bash
  grep -rniE "generativelanguage|GeminiClient|GEMINI_API_KEY" src/main/java/com/sfms/menu/
  # Expected: no output (ADR-M05-03 + ADR-M12-01)
  ```
- Prompt của MF05 **cũng** được redact bởi port (không phải mỗi module tự redact)
- AI lỗi ở MF05 → vẫn có dòng `status=TIMEOUT` (khớp `TC-003`) **và** MF05 vẫn cho lập thủ công (khớp `MF05-TC-014`)

**Expected Result (FAIL):**
- MF05 gọi Gemini trực tiếp ⇒ **BR-157 mất hiệu lực cho chính use case mà BR-157 nêu đích danh**; và credential Gemini bị nhân bản sang MF05 (vi phạm BR-164 "mã hoá tập trung")
- Không có dòng `module='MF05_MENU'` ⇒ không phân biệt được nguồn gọi AI trong báo cáo/chi phí

**Current Status:** 🔴 Not written

---

#### MF12-TC-INT-003 — audit_logs bất biến ở tầng DỮ LIỆU: chuỗi hash bắt được cả sửa thẳng SQL

> ### ⚠️ VIẾT LẠI HOÀN TOÀN 2026-08-04 — `M12-D13` / L35
>
> **Bản v1.0 test một thứ không tồn tại.** Nó kiểm `GRANT` của role **`sfms_app`**; Bước 0 cho thấy role đó **chưa bao giờ được tạo** (kiểm `V1`, `V2`, `application.yml` — ứng dụng kết nối bằng một role duy nhất, không tách quyền ghi/đọc).
>
> **Và bản v1.0 đã tự cảnh báo đúng cái bẫy mà chính nó sập vào.** Bẫy #2 ở §5.4 viết: *"nếu `sfms_app` **chưa được tạo** hoặc **không có quyền gì**, `UPDATE` cũng fail → **PASS vì lý do sai**"*. Đó chính xác là tình trạng thật của dự án. Đối chứng dương mà nó đề ra (`INSERT`+`SELECT` bằng `sfms_app` phải thành công) là thứ **duy nhất** ngăn TC này pass giả — nhưng đối chứng đó cũng fail, nên TC sẽ **đỏ vĩnh viễn vì lý do không liên quan tới BR-162**.
>
> **Bài học rộng hơn ba dòng này:** viết TestSpec từ TDS thì test kế thừa **cả giả định sai** của TDS. Ở đây giả định là *"dự án có role DB riêng cho ứng dụng"* — một câu **không ai từng viết ra**, nó chỉ ngầm nằm trong một lệnh `REVOKE`. Bước 0 bắt được vì nó buộc phải **mở file V1 ra đọc**, thay vì đọc mô tả về file V1.
>
> **Thay thế bằng gì.** `REVOKE` chỉ chặn *ứng dụng*. Chuỗi hash chặn **cả người có quyền `UPDATE` trên DB** — nó luôn là vế mạnh của `ADR-M12-02` Option C, chỉ là v1.0 xếp nó thứ hai. TC này giờ test đúng vế đó. Vế HTTP tách ra `TC-SEC-019`.

**Severity:** `CRITICAL`
**Feature Under Test:** chuỗi hash `prev_hash → entry_hash` · `GET /api/v1/audit-logs/integrity`
**Test File:** `integration/AuditIntegrityIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-12` · **Constraint Ref:** `TDS §17.1 C2`
**Oracle Source:** **BR-162** *("không ai có thể chỉnh sửa hoặc xóa bản ghi audit")* + **BR-172** *(toàn vẹn)* + `ADR-M12-02` **bản sửa 2026-08-04** + `TDS` §11.3 bước 4 *(bản sửa)* + `TDS` §13 hàng `IT-12-03` *(bản sửa)*
**Kỹ thuật:** Data-integrity / tamper-detection *(không còn là Privilege/GRANT Testing)*

**Preconditions:** ≥5 dòng `audit_logs` sinh **qua `AuditService`** (không `INSERT` tay — phải có `entry_hash` đúng). Chạy bằng **role thật của ứng dụng**; **không cần** role hạn chế.

**Test Steps:**
1. `GET /audit-logs/integrity` → **đối chứng dương**: chuỗi lành thì phải báo lành.
2. `UPDATE audit_logs SET action='TAMPERED' WHERE code='AUD-99120'` — **thẳng bằng SQL**, mô phỏng DBA hoặc script sửa dữ liệu.
3. `GET /audit-logs/integrity` → assert.
4. Khôi phục `action` về giá trị cũ → `GET /audit-logs/integrity` → assert.
5. `DELETE FROM audit_logs WHERE code='AUD-99121'` (dòng **giữa** chuỗi) → `GET /audit-logs/integrity`.
6. Qua JPA: nạp một `AuditLog`, đổi field, `flush()` → bắt SQL sinh ra.

**Expected Result (PASS):**
- (1) → `valid: true`, `brokenAt: null` — ⚠️ **đối chứng dương bắt buộc**: không có bước này thì TC pass cả khi endpoint luôn trả `false`
- (3) → **`valid: false`**, `brokenAt` = **`"AUD-99120"`** *(dòng đầu tiên gãy, không phải dòng cuối)* — chỉ đúng dòng bị sửa mới có ý nghĩa điều tra
- (4) → `valid: true` trở lại — chứng minh phát hiện dựa trên **nội dung**, không phải cờ trạng thái nào đó
- (5) → `valid: false` — **xoá cũng bị bắt**, vì `prev_hash` của dòng kế tiếp không còn khớp
- (6) → **không câu `UPDATE` nào được sinh** (`@Immutable` trên `iam/domain/AuditLog`); nếu Hibernate ném exception thì cũng đạt
- Test này **không** đụng `information_schema.role_table_grants`, **không** cần role riêng, **không** cần superuser

**Expected Result (FAIL):**
- (3) trả `valid: true` ⇒ **BR-172 không có gì bảo vệ**: sửa bằng chứng vận hành mà hệ thống không biết. Đây là hậu quả thật, và nó **nặng hơn** ca `REVOKE` — vì `REVOKE` chưa bao giờ chặn được người có quyền DB
- (1) trả `valid: false` trên chuỗi lành ⇒ endpoint vô dụng (báo động giả liên tục thì không ai đọc)
- (3) đúng nhưng `brokenAt` trỏ **dòng cuối** thay vì dòng gãy đầu tiên ⇒ điều tra sự cố phải dò tay toàn bảng
- (6) sinh `UPDATE` ⇒ thiếu `@Immutable`; code nghiệp vụ vô tình `save()` một `AuditLog` đã nạp là **âm thầm ghi đè** bằng chứng

**Current Status:** 🔴 Not written
**Implementation Note:** `V22` **KHÔNG** chứa `REVOKE`/`GRANT` (`TC-CON-009` assert điều này). `FX-033` **không** cần tạo role `sfms_app`. Hàm verify chuỗi nằm trong `AuditService` (`M12-D08`), không ở một service riêng của `mf12`.

---

#### MF12-TC-INT-004 — Không có route sửa/xoá audit qua API

**Severity:** `CRITICAL`
**Feature Under Test:** `PUT`/`DELETE`/`PATCH /api/v1/audit-logs/*`
**Test File:** `integration/AuditApiSurfaceIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-12` · **Constraint Ref:** `TDS §17.1 C2`
**Oracle Source:** `TDS` §9.2 hàng #3 — *"`POST`/`PUT`/`DELETE /api/v1/audit-logs*` | BR-162 | **KHÔNG AI** | ⬜ **CẤM** — backend trả **405 `IAM-162`**"* + `TDS` §13 hàng `IT-12-04` + `TDS` §14.3 (*"Xác nhận KHÔNG có PUT/DELETE cho audit-logs — Expected: `"not-exposed"`"*) + **L13/CF-9**
**⚠️ D7/L4:** `IAM-162` không có MSG nguồn ⇒ assert **code**.
**⚠️ Logic Issue:** **L13/CF-9** — mock cho phép `PUT`/`DELETE` **mọi** resource qua generic handler ⇒ Minh **không** được kỳ vọng CRUD đầy đủ.

**Test Steps:**
1. `PUT /api/v1/audit-logs/AUD-99120` bằng `system-admin` → assert.
2. `DELETE`, `PATCH` → assert.
3. `POST /api/v1/audit-logs` (tạo tay) → assert.
4. Kiểm OpenAPI spec.

**Expected Result (PASS):**
- (1)(2) → **405** `$.error.code == "IAM-162"` (hoặc 404 — TC chấp nhận nhưng phải **nhất quán**; ghi rõ vào §9.2)
- (3) → **405** — audit chỉ sinh **qua `AuditLogPort` nội bộ**, không qua API công khai
- (4) → OpenAPI **không** khai `put`/`delete`/`patch` cho path `/audit-logs`:
  ```bash
  curl -s localhost:8080/v3/api-docs | jq '.paths["/api/v1/audit-logs/{code}"] | keys'
  # Expected: ["get"]  (§14.3 — "not-exposed")
  ```
- `GET` vẫn 200 cho SysAdmin (đối chứng dương)

**Expected Result (FAIL):**
- Route tồn tại (dù có `@PreAuthorize` chặn) ⇒ bề mặt tấn công không cần thiết; và một lần cấu hình quyền sai là sửa được audit
- OpenAPI khai `put` ⇒ Minh sinh client có hàm update, dùng vào là 405 lúc runtime

**Current Status:** 🔴 Not written

---

#### MF12-TC-INT-005 — Hợp đồng phân trang: 5 field, clamp page

**Severity:** `HIGH`
**Feature Under Test:** `GET /audit-logs` + `PagedResponse.of()`
**Test File:** `integration/PaginationContractIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-35`
**Oracle Source:** `00_` **§2b-1** (5 field, không wrapper) + **§2b-6** (clamp `page`; `pageSize` ≤ 0 → 1) + `TDS` §13 hàng `IT-12-05` + `TDS` §14.3 (*"Kiểm hợp đồng phân trang"*)
**Kỹ thuật:** Boundary Value Analysis

**Preconditions:** seed **20** dòng audit.

**Test Steps:** `?page=999&pageSize=8` · `?pageSize=0` · `?pageSize=-5` · *(không param)*.

**Expected Result (PASS):**
- `page=999` → **200**, `pageCount == 3`, **`page == 3`** (clamp), `items.size() == 4`
- `pageSize` ∈ {0, −5} → clamp **1**; không 500
- Không param → `page=1`, `pageSize=8`
- Khoá JSON đúng 5: `["items","page","pageCount","pageSize","total"]`; **không** wrapper
- Áp cho **cả 5 resource** phân trang của MF12 (`audit-logs`, `error-logs`, `sent-reports`, `integrations`, `checklist-groups`)

**Expected Result (FAIL):** thiếu 1 field ⇒ `Paged<T>` của `client.ts` không parse được → mọi màn danh sách MF12 gãy.

**Current Status:** 🔴 Not written

---

#### MF12-TC-INT-006 — Tập rỗng sau lọc → 200, không 404

**Severity:** `HIGH`
**Feature Under Test:** `GET /error-logs`
**Test File:** `integration/PaginationContractIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-35`
**Oracle Source:** `00_` **§2b-6** (*"Tập rỗng sau lọc → `pageCount=1`, `page=1`, `items=[]`, `total=0`. **Không trả 404**"*) + `TDS` §13 hàng `IT-12-06`

**Test Steps:** `GET /error-logs?severity=CRITICAL&dateFrom=2099-01-01`.

**Expected Result (PASS):** **200** `{"items":[],"total":0,"page":1,"pageSize":8,"pageCount":1}`; `pageCount` là **1**.
**Expected Result (FAIL):** 404 ⇒ `usePagedList` coi là lỗi mạng, màn hiện lỗi thay vì "không có kết quả".

**Current Status:** 🔴 Not written

---

#### MF12-TC-INT-007 — GET /error-logs trả ĐỦ stackTrace + sessionInfo

**Severity:** `HIGH`
**Feature Under Test:** `GET /api/v1/error-logs`
**Test File:** `integration/ErrorLogContractIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-28` · **Constraint Ref:** `TDS §17.1 C14`
**Oracle Source:** `TDS` §17.1 **C14** nguyên văn — *"`GET /error-logs` **phải trả đủ `stackTrace` + `sessionInfo`** ngay trong danh sách — FE render trực tiếp ở bảng, không gọi endpoint detail. **Không** lược field để tối ưu payload"* + `MF12_AI_Supporting_Services.md` **UC-111 bước 5** + **L20/CF-18**

**Preconditions:** `FX-005` `ERR-5521` (có `stackTrace` + `sessionInfo`).

**Test Steps:**
1. `GET /error-logs?pageSize=50` → kiểm từng item.
2. So với `GET /error-logs/{code}` (detail).
3. Kiểm sanitize (`TC-SEC-018`).

**Expected Result (PASS):**
- Mỗi item trong **danh sách** có `stackTrace` **khác null/khác rỗng** và `sessionInfo` khác null
- Tập khoá của item trong list **bằng** tập khoá của detail (không lược)
- `stackTrace` đã sanitize credential/PII (`TC-SEC-018`) nhưng **vẫn** có tên class + số dòng
- `message` của `ERR-5521` = *"Gemini AI request timeout sau 10s"* — khớp `TC-003`

**Expected Result (FAIL):**
- Lược `stackTrace` khỏi list ⇒ **màn chi tiết lỗi trống** (FE không gọi detail) ⇒ UC-111 bước 5 vô dụng — đúng hậu quả L20 cảnh báo
- Trả `stackTrace` chưa sanitize ⇒ vi phạm BR-164/BR-173 (`TC-SEC-018`)

**Current Status:** 🔴 Not written

---

#### MF12-TC-INT-008 — Filter audit: ip, dateFrom/dateTo, search trên SEARCH_FIELDS

**Severity:** `HIGH`
**Feature Under Test:** `AuditLogSpec.build()`
**Test File:** `integration/AuditFilterIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-36`
**Oracle Source:** `MF12_AI_Supporting_Services.md` **UC-106 bước 4** (*"System Admin applies filters: **Date range**, actor, action, IP"*) + **BR-171**, **BR-174** + `TDS` §9.2 (`SEARCH_FIELDS` = `{actorLabel, action, resource, ip}`) + `TDS` §13 hàng `IT-12-07`, `IT-12-08` + `00_` §2b-2/2b-3 + **L19/CF-17**
**⚠️ Logic Issue:** **L19/CF-17** — FE khai 2 ô `<input type="date">` nhãn *"Từ ngày (BR-171)"* nhưng object `filters` **chỉ nhận `ip` và `result`** ⇒ 2 giá trị ngày **không bao giờ được truyền**. Đây là **lỗi FE**, backend phải hỗ trợ sẵn.

**Preconditions:** `FX-001`…`FX-004` (4 dòng, gồm `FX-004` `ip=203.113.99.87`, `result=BLOCKED`).

**Test Steps:**
1. `?ip=203.113.99.87` → assert.
2. `?result=BLOCKED` → assert.
3. `?dateFrom=2026-07-01&dateTo=2026-07-02` → assert.
4. `?search=LOGIN` → assert.
5. `?search=<chuỗi chỉ có trong entry_hash>` → assert.
6. Kết hợp `?ip=…&dateFrom=…&search=…`.

**Expected Result (PASS):**
- (1) → đúng **1** dòng `FX-004` (khớp `IT-12-08`)
- (2) → chỉ dòng `BLOCKED`
- (3) → lọc trên cột **`timestamp`**; ⚠️ ranh giới: `dateTo=2026-07-02` **bao gồm** cả ngày 02/07 (đến `23:59:59`), không cắt ở `00:00:00` — nếu cắt sai thì `FX-001` (10:18:44 ngày 02/07) bị loại oan
- (4) → khớp cột `action` (`FX-003` `action="LOGIN"`), **case-insensitive**
- (5) → `total == 0` — `search` **không** quét `entry_hash`/`prev_hash` (`TC-SEC-017`)
- (6) → thứ tự áp lọc đúng §2b-3: exact → khoảng ngày → `search` **cuối**

**Expected Result (FAIL):**
- (3) không hỗ trợ ⇒ UC-106 bước 4 không thực hiện được, và BR-171/BR-174 (truy vấn lịch sử theo thời gian) không có phương tiện
- (3) cắt sai ranh giới ngày ⇒ System Admin điều tra sự cố bị mất dòng của ngày cuối kỳ

**Current Status:** 🔴 Not written

---

#### MF12-TC-INT-009 — Endpoint integrity: verify 50 dòng

**Severity:** `CRITICAL`
**Feature Under Test:** `GET /api/v1/audit-logs/integrity`
**Test File:** `integration/AuditIntegrityIT.java` · **TDD Phase:** 🔴 RED · ⚠️ endpoint **MỚI** (§9.2 #4)
**Condition Ref:** `COND-11` · **Constraint Ref:** `TDS §17.1 C2`
**Oracle Source:** **BR-172** + `ADR-M12-02` + `TDS` §13 hàng `IT-12-09` (*"Ghi 50 dòng rồi verify → `{valid:true, checked:50}`"*) + §9.2 #4 (rate limit **5/min** — thao tác nặng)

**Test Steps:**
1. Ghi 50 dòng qua `AuditLogPort` → `GET /audit-logs/integrity` → assert.
2. Sửa dòng 25 bằng **`ownerJdbc()`** (mô phỏng tấn công DBA) → verify lại.
3. Gọi 6 lần/phút → assert rate limit.
4. Non-SysAdmin → assert 403.

**Expected Result (PASS):**
- (1) → **200** `{valid: true, checked: 50}`
- (2) → `valid = false`, `brokenAt` = code dòng **25** (khớp `TC-012b`)
- (3) → lần thứ 6 → **429** (rate limit 5/min)
- (4) → **403** (§16: chỉ SysAdmin)
- Verify **không** sửa gì trong DB (chỉ đọc)

**Expected Result (FAIL):**
- `valid=true` sau khi sửa ⇒ endpoint kiểm toàn vẹn **vô dụng** — tệ hơn không có, vì tạo cảm giác an toàn giả
- Không rate limit ⇒ verify 50k dòng liên tục làm nghẽn DB

**Current Status:** 🔴 Not written

---

#### MF12-TC-INT-010 — Audit ghi đủ actor + IP cho mọi thao tác ghi

**Severity:** `HIGH`
**Feature Under Test:** audit aspect × các thao tác của MF12
**Test File:** `integration/AuditCoverageIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-44` · **Constraint Ref:** `TDS §17.1 C2`
**Oracle Source:** **BR-170** nguyên văn (*"người thực hiện, thời gian, đối tượng bị tác động"*) + `TDS` §14.2 (*"BR-170: audit có đủ actor/time/resource + IP — **Expected: 0**"* dòng thiếu) + `TDS` §13 hàng `IT-12-16` + **BR-177** (*mỗi lần xuất báo cáo ghi audit trail*)

**Test Steps:**
1. Chạy chuỗi: `test-connection` → `save integration` → `PUT security-policies` → `POST checklist-groups` → `DELETE checklist` → `POST /sent-reports/compliance` → `GET /sent-reports/{code}/download`.
2. Truy vấn `audit_logs`.

**Expected Result (PASS):**
- ≥ **7** dòng, mỗi thao tác ≥ 1 dòng
- Mỗi dòng có `actor` ≠ null, `ip` ≠ null, `timestamp` ≠ null, `resource` ≠ null
- **Lượt tải file** cũng có dòng audit (**BR-177**: *"mỗi lần xuất (ai, khi nào, loại) ghi audit trail"*)
- Truy vấn kiểm (§14.2) trả **0**:
  ```sql
  SELECT count(*) FROM audit_logs
   WHERE actor IS NULL OR ip IS NULL OR resource IS NULL OR timestamp IS NULL;
  -- Expected: 0
  ```
- `GET` thuần (đọc danh sách) **không** sinh audit — chỉ *"hành động quan trọng"* (BR-170), tránh phình bảng

**Expected Result (FAIL):**
- Thiếu dòng cho lượt tải file ⇒ vi phạm BR-177; không biết ai đã tải báo cáo có dữ liệu nhạy cảm
- `ip` null ⇒ audit không truy được nguồn truy cập (UC-106 bước 4 lọc theo IP vô dụng)

**Current Status:** 🔴 Not written

---

#### ~~MF12-TC-INT-011~~ — Checklist soft-delete · ⛔ `@Disabled("M12-D02 — thuộc MF07")`

> **Gỡ khỏi phạm vi MF12 ngày 2026-08-04 (`M12-D02`, Bước 0).** Bảng `checklist_groups`/`checklist_items` (`V12:209/223`) và endpoint `/api/v1/checklist-groups` đã merge ở MF07 (`M07-D10`); `TestSpec-MF07` **đã có** TC tương đương. Giữ nguyên văn bên dưới theo Policy 4.4, nhưng **không hiện thực trong `mf12`**.
>
> **Vì sao `@Disabled` chứ không xoá:** ghi chú *"phải khớp `MF07-TC-INT-010`"* ở dòng dưới là thông tin còn giá trị — nó nói cho người đọc MF12 biết hành vi này **được test ở đâu**. Xoá trắng thì lần sau lại có người viết lại nó.
>
> **Một TC thay thế cần có ở MF12:** khi báo cáo tuân thủ (UC-185) đọc dữ liệu chấm điểm checklist qua `ComplianceDataPort`, assert **snapshot trong `sent_reports.payload` không đổi** dù sau đó MF07 ẩn một mục — đó mới là ranh giới thật giữa hai module. Gộp vào `MF12-TC-INT-012`.

**Severity:** `HIGH`
**Feature Under Test:** `DELETE /api/v1/checklist-groups/{code}`
**Test File:** `integration/ChecklistLifecycleIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-16` · **Constraint Ref:** `TDS §17.1 C5`
**Oracle Source:** **BR-210** + **MSG-094** + `ADR-M12-06` + `TDS` §13 hàng `IT-12-12` (*"`POST /checklist-groups` → MF07 `POST /kitchen-inspections`: phiên mới thấy mục mới; phiên cũ không đổi"*)
**⚠️ Ranh giới (L7/CF-3):** TC này **phải khớp `MF07-TC-INT-010`** — cùng bảng, hai phía. Nếu lệch, ghi vào biên bản review cho Nam.

**Test Steps:**
1. Seed `FX-012` + `FX-014` (kết quả trỏ `CLI-3-8`).
2. `DELETE /checklist-groups/CLG-3` → assert 409 + MSG-094.
3. `?confirmed=true` → assert.
4. Đếm `checklist_item_results`.
5. Tạo phiên kiểm bếp **mới** qua MF07 → assert.
6. Đọc phiên **cũ** của MF07 → assert.

**Expected Result (PASS):**
- (2) → **409** `AIS-094` + message **MSG-094 nguyên văn**
- (3) → **200**; `is_active = false`; **không** câu `DELETE FROM` nào chạy (assert SQL log)
- (4) → `checklist_item_results` **giữ nguyên số dòng**
- (5) → phiên mới **không** còn `CLG-3`
- (6) → phiên cũ **vẫn** render đủ mục
- `POST` nhóm mới → phiên mới **thấy** nhóm mới (khớp `IT-12-12`)

**Expected Result (FAIL):**
- Xoá cứng + CASCADE ⇒ **phiên kiểm bếp lịch sử của MF07 mất mục** ⇒ hồ sơ định kỳ tháng (BR-101) không tái tạo được, và Báo cáo Tuân thủ (BR-104) tính trên bộ mục khác

**Current Status:** 🔴 Not written

---

#### MF12-TC-INT-012 — Gửi báo cáo: sent_reports + event tới MF10

**Severity:** `HIGH`
**Feature Under Test:** `POST /api/v1/sent-reports/compliance`
**Test File:** `integration/ReportSendIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-18`, `COND-23` · **Constraint Ref:** `TDS §17.1 C6`
**Oracle Source:** **BR-209** + **MSG-091** + **BR-158/BR-159** (*thông báo*) + `TDS` §13 hàng `IT-12-13` (*"200 MSG-091; `sent_reports` 1 dòng; sự kiện tới MF10"*) + **L28/CF-29**
**⚠️ Ranh giới (L28/CF-29):** MF12 assert **event được phát**; logic gửi/retry/giờ yên tĩnh (BR-158/159) test ở **MF10 (Mạnh)**. FE hiện phát thông báo từ client (`lib/notify.ts:31`) — Minh gỡ dần.

**Preconditions:** `FX-016` (5 phiên, avg 90), `FX-030` JWT Medical `SCH-001`.

**Test Steps:**
1. `POST /sent-reports/compliance` với kỳ có dữ liệu.
2. Kiểm `sent_reports` + payload.
3. Kiểm event.
4. Kiểm file sinh ra.

**Expected Result (PASS):**
- **200**, message **MSG-091**
- 1 dòng `sent_reports`: `report_type='COMPLIANCE'`, `school_id='SCH-001'` (**từ JWT** — C12), `sent_by` từ JWT (không tin client — `TC-SEC-016` ca g)
- `payload.avgScore == 90` (tính lại từ `FX-016`, khớp `FX-015`)
- Event `ComplianceReportSent` được phát **1 lần** với `recipient='School Administration'`
- `filePdfUrl` + `fileXlsxUrl` khác null (BR-178 — `TC-INT-015`)
- **Không** gọi trực tiếp `POST /notifications` từ MF12 (thông báo là việc của MF10 qua event)

**Expected Result (FAIL):**
- Không phát event ⇒ School Administration **không được báo** ⇒ mô hình "Medical Staff chủ động gửi" mất một nửa giá trị (đúng lý do UC-185 ra đời: *mô hình pull hiện tại chưa có route triển khai thật ở phía School Administration*)
- Phát 2 event (backend + client cũ) ⇒ School Admin nhận thông báo trùng (L28)

**Current Status:** 🔴 Not written

---

#### MF12-TC-INT-013 — MF12 migration KHÔNG tạo lại bảng security_policies

**Severity:** `HIGH`
**Feature Under Test:** Flyway `V22`/`V23`
**Test File:** `integration/SchemaOwnershipIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-47` · **Constraint Ref:** `TDS §17.1 C8`
**Oracle Source:** `TDS` §17.1 **C8** nguyên văn — *"`security_policies` **do MF01 sở hữu bảng + seed**; MF12 chỉ đọc/ghi qua UC-184, **không** khai lại bảng trong migration"* + `TDS-MF01_Backend.md` §11.1 (*seed `SEC-DEFAULT`*) + **L10/CF-6** + `00_` §4b (*"Flyway migration là tài nguyên chung… Nam giữ V1 = toàn bộ schema"*)

**Test Steps:**
1. Grep nội dung `V22`/`V23`.
2. Kiểm bảng tồn tại sau `V1` (trước `V22`).
3. Kiểm seed `SEC-DEFAULT` có sau `V1`.
4. Partial merge (`TC-016`) chạy trên bảng do MF01 tạo.

**Expected Result (PASS):**
- (1) →
  ```bash
  grep -niE "CREATE TABLE .*security_policies|DROP TABLE .*security_policies" \
       src/main/resources/db/migration/V22*.sql src/main/resources/db/migration/V23*.sql
  # Expected: no output (C8)
  ```
- (2) → bảng đã tồn tại sau `V1`
- (3) → `SEC-DEFAULT` có sẵn với đúng giá trị `FX-011`
- (4) → MF12 `PUT` thành công trên bảng của MF01
- ⚠️ Đối chứng dương: `V22` **có** tạo **4** bảng thuộc MF12 (`ai_conversations`, `ai_requests`, `error_logs`, `external_integrations`, `sent_reports`) — nếu grep rỗng vì `V22` trống thì test PASS giả (bẫy #4 §5.4). ⛔ **`audit_logs` và `checklist_groups` KHÔNG nằm trong danh sách này** (`M12-D01`/`M12-D02`) — `V22` chỉ `ALTER` `audit_logs`, không chạm `checklist_*`

**Expected Result (FAIL):**
- `V22` tạo lại bảng ⇒ **conflict cứng với `V1` của Nam** (`00_` §4b nêu đúng rủi ro này); hoặc tạo bảng thứ hai cùng tên ở schema khác ⇒ MF01 và MF12 đọc 2 nguồn khác nhau ⇒ chính sách bảo mật không có hiệu lực

**Current Status:** 🔴 Not written

---

#### MF12-TC-INT-014 — Liên module: MF12 sửa policy → MF01 áp ở lần kiểm tra kế tiếp

**Severity:** `CRITICAL`
**Feature Under Test:** `PUT /security-policies` → event `SecurityPolicyUpdated` → MF01 login
**Test File:** `integration/SecurityPolicyEffectIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-46` · **Constraint Ref:** `TDS §17.1 C8`
**Oracle Source:** **BR-002** nguyên văn (*"Sau **5 lần** đăng nhập sai liên tiếp → **khóa tài khoản 15 phút**; ghi audit"*) + `TDS` §17.1 **C8** (*"Cập nhật phải phát `SecurityPolicyUpdated` để MF01 áp ở lần kiểm tra kế tiếp"*) + `TDS` §13 hàng `IT-12-11` (*"`maxLoginAttempts=3` → MF01 login sai 3 lần → tài khoản bị khoá ở lần thứ 3"*)
**⚠️ Ranh giới:** logic khoá tài khoản test ở **MF01**; TC này test **hiệu lực xuyên module** — sửa ở MF12 thì MF01 phải thấy.

**Test Steps:**
1. `PUT /security-policies` với `maxLoginAttempts = 3` → assert 200 + event.
2. Gọi `POST /auth/login` sai mật khẩu **3 lần**.
3. Lần thứ 3 → assert.
4. Đổi lại `maxLoginAttempts = 5`, mở khoá, thử lại → lần 3 **không** khoá, lần 5 mới khoá.
5. Kiểm audit của cả 2 thao tác.

**Expected Result (PASS):**
- (1) → event `SecurityPolicyUpdated` phát 1 lần
- (3) → tài khoản **bị khoá ở lần thứ 3** (không phải 5) — chính sách mới có hiệu lực **không cần restart**
- (4) → sau khi đổi về 5, khoá ở lần **5**
- (5) → mỗi lần đổi policy có dòng audit (BR-170); lần khoá tài khoản cũng có dòng audit (BR-002 nói *"ghi audit"*) — khớp `FX-004` (`AUD-99087` với `result=BLOCKED`)
- MF01 **đọc** từ bảng, **không** cache vĩnh viễn (hoặc cache có invalidation qua event)

**Expected Result (FAIL):**
- MF01 vẫn khoá ở lần 5 ⇒ **UC-184 không có tác dụng thực tế**; System Admin sửa chính sách mà hệ thống không đổi hành vi — cả use case trở thành trang trí
- Cần restart mới có hiệu lực ⇒ không dùng được trong vận hành

**Current Status:** 🔴 Not written

---

#### MF12-TC-INT-015 — Tải file báo cáo: PDF + XLSX đủ 4 thành phần BR-178

**Severity:** `HIGH`
**Feature Under Test:** `GET /api/v1/sent-reports/{code}/download?format=pdf|xlsx`
**Test File:** `integration/ReportDownloadIT.java` · **TDD Phase:** 🔴 RED · ⚠️ endpoint **MỚI** (L25/CF-23)
**Condition Ref:** `COND-23` · **Constraint Ref:** `TDS §17.1 C6`
**Oracle Source:** **BR-178** nguyên văn — *"Xuất báo cáo 2 định dạng **PDF** và **Excel (XLSX)**; mỗi file gồm: **tiêu đề, khoảng thời gian dữ liệu, tên trường, timestamp xuất**"* + `TDS` §13 hàng `IT-12-15` (*"200 `application/vnd.openxmlformats-...`"*) + **BR-181** (nhãn Confidential) + **L25/CF-23**

**Test Steps:**
1. Gửi báo cáo (`TC-INT-012`) → `GET .../download?format=pdf`.
2. `?format=xlsx`.
3. `?format=csv` (không hỗ trợ).
4. Extract nội dung → kiểm 4 thành phần.
5. Kiểm audit của lượt tải.

**Expected Result (PASS):**
- (1) → **200**, `Content-Type: application/pdf`, body bắt đầu `%PDF-`
- (2) → **200**, `Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`, body bắt đầu `PK` (zip)
- (3) → **400** (chỉ 2 định dạng theo BR-178)
- (4) → **cả 2 file** có: **tiêu đề** báo cáo · **khoảng thời gian dữ liệu** (`rangeLabel` = *"Thứ 2 06/07/2026 – Chủ nhật 12/07/2026"*) · **tên trường** · **timestamp xuất**
- (4) → nhãn **"Confidential"** khi `confidential=true` (BR-181)
- (4) → **không** chứa mã HS (`TC-SEC-008`)
- (5) → 1 dòng audit cho lượt tải (BR-177)
- Non-authorized role → 403 (§16 hàng `/download`)

**Expected Result (FAIL):**
- Thiếu bất kỳ 1 trong 4 thành phần ⇒ file không đủ tính pháp lý/đối chứng; đặc biệt thiếu **khoảng thời gian** thì báo cáo vô nghĩa (không biết nói về kỳ nào)
- Chỉ có PDF ⇒ BR-178 chỉ thoả một nửa; School Administration không dùng được số liệu để phân tích

**Current Status:** 🔴 Not written

---

#### MF12-TC-INT-016 — Luồng UC-040 trọn vẹn + một nguồn trạng thái duy nhất

**Severity:** `CRITICAL`
**Feature Under Test:** `POST /integrations/{code}/test-connection` → `PUT /integrations/{code}` → `GET /system/status`
**Test File:** `integration/IntegrationFlowIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-06`, `COND-08`, `COND-29` · **Constraint Ref:** `TDS §17.1 C3, C4, C11`
**Oracle Source:** **BR-163** + **BR-164** + `TDS` §13 hàng `IT-12-10` (*"`test-connection` PASS → `PUT` → 200 → 200 `connected:true`; DB có cipher, **không có plaintext**"*) + **L14/CF-10** (một nguồn duy nhất) + `TDS` §11.3 bước 7 (smoke test)

**Test Steps:**
1. `POST /integrations/gemini/test-connection` với `FX-022` (fake OK) → assert.
2. `PUT /integrations/gemini` với `apiKey` → assert.
3. Kiểm DB: cipher vs plaintext.
4. `GET /integrations` → assert `connected`.
5. `GET /system/status` → assert **cùng** trạng thái Gemini.

**Expected Result (PASS):**
- (1) → **200**, `last_test_passed=true`, `last_tested_at=now`
- (2) → **200**, `connected: true` (**suy từ** `last_test_passed`, không từ client — C11)
- (3) → cột credential là **ciphertext**; truy vấn tìm plaintext trả 0:
  ```sql
  SELECT count(*) FROM external_integrations WHERE credential_cipher LIKE 'AIza%';
  -- Expected: 0  (đã mã hoá, không còn tiền tố plaintext)
  ```
- (4) → `credentialMasked` có, `apiKey` **không** có (`TC-009`)
- (5) → **`GET /system/status` đọc CÙNG bảng** — trạng thái Gemini khớp 100% với `GET /integrations`; **không** tự sinh trạng thái riêng (L14/CF-10)
- Smoke test §11.3 bước 7 xanh

**Expected Result (FAIL):**
- (5) hai endpoint nói khác nhau ⇒ tái hiện đúng lỗi CF-10 (*"Tích hợp ngoài & AI" ghi "Chưa kết nối", "Trạng thái hệ thống" ghi "đang chạy"*) — System Admin không biết tin màn nào
- (3) còn plaintext ⇒ BR-164 vô hiệu

**Current Status:** 🔴 Not written

---

#### MF12-TC-INT-017 — Endpoint system/* trả số liệu từ Actuator

**Severity:** `MEDIUM`
**Feature Under Test:** `GET /api/v1/system/{activities,status,usage,backups}`
**Test File:** `integration/SystemMetricsIT.java` · **TDD Phase:** 🔴 RED · ⚠️ **BLOCKED bởi L12/CF-19** (3 UC hiện hardcode ở FE)
**Condition Ref:** `COND-26` · **Constraint Ref:** `TDS §17.1 C9`
**Oracle Source:** `ADR-M12-04` (*metric lấy từ Actuator, **không** tạo bảng metric riêng*) + `traceability_matrix.md` §1.12 (UC-057, UC-149, UC-150 còn hiệu lực) + **BR-165** + **L12/CF-8+CF-19**

**Test Steps:** gọi 4 endpoint bằng `FX-029` (SysAdmin); non-SysAdmin → 403.

**Expected Result (PASS):**
- 4 endpoint → **200** với dữ liệu **thật** (không hằng số)
- `GET /system/status` gồm cả **dịch vụ ngoài** (từ `external_integrations` — `TC-INT-016` ca 5) **và** dịch vụ nội bộ (từ `/actuator/health`)
- `GET /system/usage` nhóm theo **`RoleKey` kebab-case** (`TC-029`)
- `GET /system/activities` tổng hợp từ `audit_logs` — **không** trả `actor`/`ip` từng dòng (`TC-SEC-009` ca 2)
- Non-SysAdmin → 403
- ⚠️ **3 UC này hiện chưa nghiệm thu được** ⇒ TC đánh dấu blocked, **không** tính vào coverage gate cho tới khi Minh thay hằng số

**Expected Result (FAIL):**
- Trả hằng số hardcode ở backend ⇒ chỉ chuyển vấn đề từ FE sang BE, 3 UC vẫn không có giá trị
- Tạo bảng `system_metrics` ⇒ vi phạm ADR-M12-04 (`TC-INT-018`)

**Current Status:** 🔴 Not written

---

#### MF12-TC-INT-018 — KHÔNG tạo bảng system_metrics

**Severity:** `LOW`
**Feature Under Test:** schema `V22`
**Test File:** `integration/SchemaShapeIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-26` · **Constraint Ref:** `TDS §17.1 C9`
**Oracle Source:** `ADR-M12-04` nguyên văn — *"Metric hệ thống lấy từ Actuator, **không** tạo bảng metric riêng"* + `TDS` §17.1 **C9**

**Expected Result (PASS):**
```sql
SELECT table_name FROM information_schema.tables
 WHERE table_name IN ('system_metrics','usage_statistics','backups');
-- Expected: 0 rows
```
- ⚠️ **Đối chứng dương bắt buộc:** cùng test assert `audit_logs`, `ai_requests`, `external_integrations`, `sent_reports`, `checklist_groups` **CÓ** tồn tại — nếu không, DB trống cũng PASS (bẫy #4 §5.4)
- Báo cáo tuần BR-165 lưu vào **`sent_reports(report_type='SYSTEM_HEALTH')`**, không bảng riêng (`TC-020`)

**Expected Result (FAIL):** bảng tồn tại ⇒ vi phạm ADR-M12-04; schema phình và số liệu có 2 nguồn (Actuator vs bảng) dễ lệch.

**Current Status:** 🔴 Not written

---

#### MF12-TC-INT-019 — Report Center hub chỉ gom UC-185 + UC-189

**Severity:** `MEDIUM`
**Feature Under Test:** `GET /api/v1/report-center`
**Test File:** `integration/ReportCenterIT.java` · **TDD Phase:** 🔴 RED · ⚠️ endpoint **MỚI** (§9.2 #28)
**Condition Ref:** `COND-49` · **Constraint Ref:** `TDS §17.1 C7`
**Oracle Source:** **BR-213** (*UC-187 + UC-188 **Deprecated 26/07/2026**) + `TDS` §2.3 CF-4 (*"**UC-187** giữ endpoint hub (route mock còn sống) nhưng **chỉ gom UC-185 + UC-189**"*) + **BR-175**
**⚠️ Logic Issue:** **L8/CF-4** — §1.12 vẫn liệt kê UC-187 như còn hiệu lực; mock vẫn còn route `medical-staff/report-center`.

**Test Steps:**
1. `GET /report-center` bằng `FX-030` → kiểm danh sách loại báo cáo.
2. Non-Medical → assert 403.

**Expected Result (PASS):**
- Trả **2** loại có thể gửi: `COMPLIANCE` (UC-185) và `VIOLATION_SUMMARY` (UC-189)
- **KHÔNG** có `INCIDENT_SUMMARY` trong danh sách "có thể gửi" (deprecated — `TC-023`)
- **CÓ** phần lịch sử gồm cả báo cáo `INCIDENT_SUMMARY` **cũ** (đọc lịch sử vẫn chạy — BR-177)
- Non-Medical → **403** (§16: `GET /report-center` chỉ ✅ MEDICAL STAFF, **kể cả SysAdmin là ❌**)

**Expected Result (FAIL):**
- Hiển thị `INCIDENT_SUMMARY` là loại gửi được ⇒ Medical Staff bấm gửi rồi nhận 409 — trải nghiệm hỏng, và mâu thuẫn với `TC-023`
- Ẩn cả lịch sử ⇒ mất truy cập báo cáo đã gửi (BR-177)

**Current Status:** 🔴 Not written

---

#### MF12-TC-INT-020 — sent_reports JSONB: union giữ nguyên, dữ liệu cũ đọc được

**Severity:** `HIGH`
**Feature Under Test:** `GET /api/v1/sent-reports` + `/{code}` — single-table + JSONB
**Test File:** `integration/SentReportShapeIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-50`, `COND-21` · **Constraint Ref:** `TDS §17.1 C6`
**Oracle Source:** `ADR-M12-05` (*single-table + `JSONB payload`; **shape JSON trả về FE giữ nguyên union của mock***) + **L15/CF-11**, **L26/CF-28+CF-12** + `TDS` §13 hàng `IT-12-14` (*"trả đúng loại, sort `sentAt DESC`"*)

**Test Steps:**
1. Seed `FX-015` (`COMPLIANCE` với `avgScore`, `threeStepSummary`, `staffHealthValid`) + 1 `VIOLATION_SUMMARY` (`counts`) + 1 `INCIDENT_SUMMARY` **cũ** (có `latestPestControl` — field đã bỏ).
2. `GET /sent-reports?reportType=COMPLIANCE` → kiểm shape.
3. `GET /sent-reports` (tất cả) → kiểm sort.
4. `GET /sent-reports/{code của INCIDENT_SUMMARY cũ}` → kiểm field cũ.
5. Gửi báo cáo **mới** → kiểm **không** có `latestPestControl`.

**Expected Result (PASS):**
- (2) → item `COMPLIANCE` có `avgScore`, `threeStepSummary`, `failedSessions`, `staffHealthValid` **ở cấp gốc** của JSON — **không** bọc trong `payload` (giữ nguyên union của mock, ADR-M12-05)
- (3) → sort `sentAt DESC`; 3 loại cùng danh sách, mỗi loại có field riêng của nó
- (4) → **vẫn trả** `latestPestControl` của bản ghi cũ (JSONB dung nạp — L26/CF-12)
- (5) → báo cáo **mới không** sinh `latestPestControl` (BR-209 sửa 27/07 đã bỏ)
- `counts.partnerCount` (field ngoài interface) được dung nạp, không làm fail (L26/CF-28)

**Expected Result (FAIL):**
- Bọc field đặc thù trong `payload` ⇒ **16 file FE gãy** (FE đọc `report.avgScore`, không `report.payload.avgScore`)
- Mất field cũ khi đọc lại ⇒ báo cáo lịch sử hiển thị trống, vi phạm BR-177
- Sinh `latestPestControl` cho báo cáo mới ⇒ tái tạo field đã bỏ theo BR-209 bản 27/07

**Current Status:** 🔴 Not written

---

#### MF12-TC-INT-021 — D1: JSON id là code, PK UUID; external_integrations UNIQUE(code)

**Severity:** `HIGH`
**Feature Under Test:** định danh toàn module
**Test File:** `integration/IdentityContractIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-30` · **Constraint Ref:** **`00_Common_Conventions.md` D1**
**Oracle Source:** **D1** — *"**Path param** dùng `code`… **Khóa ngoại trong JSON** dùng `code`… **Khóa ngoại trong DB** vẫn là `UUID REFERENCES`"* + `@JsonProperty("id")` + **L1**, **L11** (`external_integrations` **không** có `school_id`)

**Test Steps:**
1. `GET /audit-logs/AUD-99120` (path bằng **code**) → assert.
2. Kiểm schema 5 bảng chính.
3. `GET /integrations` → kiểm `id` = `"gemini"`.
4. Kiểm ràng buộc UNIQUE của `external_integrations`.
5. `GET /audit-logs/{UUID}` → assert.

**Expected Result (PASS):**
- (1) → **200**; response có `"id": "AUD-99120"`, **không** field `code` riêng, **không** lộ UUID
- (2) →
  ```sql
  SELECT table_name, data_type FROM information_schema.columns
   WHERE column_name = 'id'
     AND table_name IN ('audit_logs','error_logs','sent_reports','checklist_groups','external_integrations');
  -- Expected: tất cả data_type = uuid
  ```
- (3) → `"id": "gemini"` (mock dùng `"gps"`/`"gmail"`/`"firebase"`/`"gemini"` làm id ⇒ đó là `code`)
- (4) → `external_integrations` có **`UNIQUE(code)`**, **KHÔNG** phải `UNIQUE(school_id, code)` — vì bảng này **không có** `school_id` (L11/CF-7); các bảng khác có `UNIQUE(school_id, code)`
- (5) → **404** (path chỉ nhận code — nhất quán)
- FK trong DB vẫn `UUID REFERENCES`

**Expected Result (FAIL):**
- Trả UUID trong `id` ⇒ FE gãy (nó dùng `"gemini"` làm key của tab tích hợp)
- `external_integrations` có `UNIQUE(school_id, code)` mà không có cột `school_id` ⇒ migration lỗi; hoặc thêm `school_id` ⇒ trái L11 và mock (cấu hình cấp nền tảng, không theo trường)

**Current Status:** 🔴 Not written
**Implementation Note:** ~~đây là **thay đổi so với TDS-MF12 §5.2 hiện tại** (PK `VARCHAR`)~~ ✅ **TDS §5.2 đã cập nhật 2026-08-04** (`M12-D03`) — không còn lệch.

---

#### MF12-TC-INT-022 — Bean `AiAssistPort` của MF12 gỡ fallback 503 của MF05 *(mới — L33/`M12-D09`)*

**Severity:** `HIGH`
**Feature Under Test:** `com.sfms.mf12.service.GeminiAiAssistAdapter implements com.sfms.mf05.port.AiAssistPort`
**Test File:** `integration/AiAssistPortWiringIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-01` · **Constraint Ref:** `TDS §17.1 C1`
**Oracle Source:** `com.sfms.mf05.port.AiAssistPort` javadoc **nguyên văn** (*"Ở Bước 2 của MF05, MF12 CHƯA implement bean này — `Optional<AiAssistPort>` rỗng ⇒ `POST /menus/{code}/ai-suggest` luôn trả `503 MENU-073`/MSG-073 … khi MF12 lên, họ implement `@Component` class riêng, **MF05 không cần sửa gì**"*) + **BR-156/BR-157** + `ADR-M05-03` + `ADR-M12-01`

**⚠️ Vì sao TC này tồn tại:** đây là **TC liên module duy nhất của MF12 đo được bằng máy trước và sau**. `MenuRecommendationServiceImpl` của MF05 đã merge và đang trả `503` — không phải giả định, mà là hành vi hiện tại của `dev`. TC này biến "MF12 lên thì MF05 hết fallback" từ một câu trong tài liệu thành một assertion.

**Preconditions:** tài khoản seed `MEDICAL_STAFF`/`SCHOOL_ADMIN` có quyền `POST /menus/{code}/ai-suggest`; 1 menu seed của MF05; Gemini ở chế độ **stub** (ADR-000).

**Test Steps:**
1. `@SpringBootTest` toàn ứng dụng → `applicationContext.getBeanProvider(AiAssistPort.class)` → assert **có đúng 1 bean**.
2. Gọi `POST /api/v1/menus/{code}/ai-suggest` bằng tài khoản seed thật.
3. Đếm `ai_requests` trước/sau bước 2.
4. Đọc dòng `ai_requests` vừa sinh.
5. Giả lập adapter ném lỗi lúc ghi archive → gọi lại bước 2.

**Expected Result (PASS):**
- (1) → **1** bean, kiểu `GeminiAiAssistAdapter`, package **`com.sfms.mf12`** *(không phải `mf05` — MF05 chỉ khai interface)*
- (2) → **KHÔNG** còn `503 MENU-073`; trả `200` với `suggestions[]` đúng schema `AiSuggestionResponse`
- (3) → `ai_requests` **tăng đúng 1** — C1: *"mọi lời gọi AI của mọi module sinh 1 dòng"*
- (4) → dòng mới có `module = "MF05_MENU"`, `code` khớp `AIQ-<năm>-<số>` (`M12-D14`), `redacted = true` (BR-173/BR-181), `status = SUCCESS` **và** `response_text IS NOT NULL` (ràng buộc `ck_ai_req_success_has_response`)
- (5) → **rollback**: trả lỗi, **và** `ai_requests` **không** tăng, **và** MF05 **không** trả gợi ý nào (C1 — không có phản hồi AI chưa lưu vết)

**Expected Result (FAIL):**
- Bean nằm trong `mf12/port/AiAssistPort` **mới** thay vì implement interface của MF05 ⇒ `Optional<>` của MF05 **vẫn rỗng** ⇒ `503` vẫn còn, và hệ thống có **hai** interface cùng tên ⇒ đúng lỗi `M12-D09` chặn
- (3) tăng 0 ⇒ có đường gọi AI **không qua archive** ⇒ vi phạm BR-157, `AP-AI-015`
- (5) trả gợi ý dù archive lỗi ⇒ vi phạm C1 — đây là ca *"best-effort async"* mà §17.4 xếp là anti-pattern

**Current Status:** 🔴 Not written

---

### 4.4-BIS. CONTRACT TEST CASES *(mới 2026-08-04 — D11, D12 và Bước 0)*

> **`02_Module_Completion_Workflow.md` §7 Bước 1 việc 3 bắt buộc mục này.** Cả 13 TestSpec của dự án viết **trước khi** D11–D13 tồn tại ⇒ không bộ nào phủ. Kiểm bằng `grep -n "oneOf\|PagedResponse{Tên}" TestSpec-MF12_*.md` — trước 04/08 ra **0 kết quả thật** (2 hit là `PagedResponse.of()` ở `COND-35`, không phải TC D11).
>
> Sau MF05 (`TC-SEC-015`, `TC-027`), MF06 (`TC-SEC-014`), MF07 (`TC-SEC-017`) và MF09 (`TC-CON-006`), MF12 là module **thứ năm** có TC D11 thật.

---

#### MF12-TC-CON-007 — D11: `GET /sent-reports` đổi tập FIELD theo vai trò, `oneOf` khai đúng

**Severity:** `CRITICAL`
**Feature Under Test:** `GET /api/v1/sent-reports` · `SentReportDtos.PagedResponseSentReportResponse`
**Test File:** `contract/SentReportRoleShapeContractIT.java` · **TDD Phase:** 🔴 RED
**Constraint Ref:** `TDS §17.1 C12` · **Oracle Source:** `02_Module_Completion_Workflow.md` **§6 D11** + **BR-175** (*"Service Caterers chỉ xem báo cáo liên quan hoạt động của mình"*) + **BR-176** (phạm vi trường) + **BR-181** (ẩn danh học sinh trong báo cáo tổng hợp) + **S7** (`OpenApiMetaContractIT` — tên schema giữ tiền tố `PagedResponse`)

**⚠️ D11 áp hay không áp — MF12 khác MF06/MF07:** hai module đó kết luận *"không áp dụng"* vì khác biệt giữa vai trò là **tập DÒNG** (`WHERE`), không phải tập field. MF12 **có cả hai**, và phải phân biệt rạch ròi:

| Vai trò | Khác nhau ở | Cần `oneOf`? |
|---|---|---|
| **School Admin** vs **System Admin** | tập **DÒNG** — School Admin chỉ thấy `school_id` của mình (BR-176) | ❌ — một DTO + TC parity |
| **Medical Staff** vs **School Admin** | tập **DÒNG** — cùng trường, cùng field | ❌ |
| **Service Caterers** *(nếu BR-175 được Huy cấp Actor — xem `L27`/CF-M13-3)* | tập **FIELD** — `notes`, `payload.*` chứa nhận định về nhà cung cấp và, với `VIOLATION_SUMMARY`, dữ liệu vi phạm của **chính họ** | ✅ **bắt buộc** |

**Preconditions (D13 — tài khoản seed THẬT, không `@WithMockUser`):** `@WithMockUser` **không mang claim `schoolId`** ⇒ không tái hiện được lọc BR-176. Đăng nhập thật bằng `V900__test_fixtures.sql`, mật khẩu **`P@ssw0rd123`**. Seed ≥2 `sent_reports` khác `school_id`, ≥1 loại `VIOLATION_SUMMARY` có `payload.notes` không rỗng.

**Test Steps:**
1. Đăng nhập `SYSTEM_ADMIN` → `GET /sent-reports?pageSize=50` → ghi tập khoá của `items[0]`.
2. Đăng nhập `SCHOOL_ADMIN` (trường A) → gọi lại → ghi tập khoá + tập `id`.
3. Đăng nhập `MEDICAL_STAFF` (trường A) → gọi lại.
4. `GET /v3/api-docs` → đọc schema của operation `GET /sent-reports`.
5. *(nếu Huy đã cấp Actor cho Service Caterers)* đăng nhập `SERVICE_CATERERS` → gọi lại.

**Expected Result (PASS):**
- (2) vs (1) — **tập khoá GIỐNG HỆT nhau** (parity), chỉ khác **tập `id`**: School Admin không thấy báo cáo trường B. Đây là vế *"lọc dòng không cần `oneOf`"* của D11
- (3) — tập khoá **bằng** (2)
- (4) — schema tên **`PagedResponseSentReportResponse`** (giữ tiền tố `PagedResponse` — S7), và **`items` ĐÚNG KIỂU** `SentReportResponse`, **không phải `{}`**
- (4) — **KHÔNG** có `name=` đặt cho `@Schema` generic *(xem chú thích trong `common/dto/PagedResponse.java` — đặt `name` làm `items` tụt xuống `{}` cho **cả 11 endpoint danh sách** của dự án, không riêng MF12)*
- (5) — nếu bật: `items` khai **`oneOf: [SentReportResponse, SentReportCatererView]`**, và response thật **không** chứa `notes`/`payload.notes`

**Expected Result (FAIL):**
- (4) `items` là `{}` ⇒ `npm run gen:api` sinh type rỗng ⇒ `contract-check.ts` **không bắt được gì**, FE lại phải curl tay từng vai trò — đúng hai phiên làm việc mà MF03 đã tốn
- (2) tập khoá **khác** (1) mà **không** khai `oneOf` ⇒ FE build được, chạy mới lộ `undefined`
- (2) trả cả báo cáo trường B ⇒ vi phạm BR-176 — **rò rỉ giữa các trường**, nặng hơn lỗi hợp đồng
- Dùng `@WithMockUser(roles="SCHOOL_ADMIN")` thay tài khoản seed ⇒ không có claim `schoolId` ⇒ TC **pass giả** trong khi production lọc sai (D13)

**Current Status:** 🔴 Not written

---

#### MF12-TC-CON-008 — D12: field DẪN XUẤT và field SAI KIỂU bị Jackson bỏ im lặng

**Severity:** `CRITICAL`
**Feature Under Test:** `PUT /api/v1/integrations/{code}` · `PUT /api/v1/security-policies/{code}` · `POST /api/v1/sent-reports/compliance`
**Test File:** `contract/WriteDtoContractIT.java` · **TDD Phase:** 🔴 RED
**Oracle Source:** `02_Module_Completion_Workflow.md` **§6 D12** *("`GET` trả `class` mà `PATCH` chỉ nhận `classroomId` ⇒ Jackson bỏ field không khớp ⇒ **200 nhưng KHÔNG lưu**. Không lỗi, không cảnh báo — form báo thành công.")* + **BR-163/BR-164** + **L18/CF-16** (partial merge) + mẫu `TestSpec-MF05` **TC-027**

**⚠️ MF12 có ĐỦ cả hai biến thể của D12, và biến thể thứ hai nguy hiểm hơn hẳn:**

| Biến thể | Ở đâu trong MF12 | Hậu quả thật |
|---|---|---|
| **(a) field dẫn xuất** — FE gửi lên field mà backend tự tính | `connected` (CF-14 — FE set `true` **vô điều kiện**), `lastTestedAt`, `lastTestPassed`, `updatedAt`/`updatedBy` (D9 — `BaseEntity` tự điền), `sentAt`, `filePdfUrl`/`fileXlsxUrl` | Nếu backend **nhận** `connected` ⇒ **BR-163 bị vô hiệu hoàn toàn**: System Admin đánh dấu "đã kết nối" mà chưa hề test. Đây không phải lỗi lưu trữ, là lỗi **bảo mật vận hành** |
| **(b) đúng tên, SAI KIỂU** — biến thể mà review bằng mắt **không thấy** | `credentialMasked` (đọc: `String`; ghi: FE gửi `apiKey` — **tên khác hẳn**, CF-15) · `period` (`"Weekly"` vs enum `WEEKLY` — CF-27) · `counts` của báo cáo vi phạm (`payload` nhận object, có lúc FE gửi số — CF-28) | `200 OK`, form báo thành công, **DB không đổi**. Chính là ca `PATCH /students {"class":"4A"}` của MF03 |

**Preconditions:** `FX-INT-001` = `external_integrations` `code='gemini'`, `connected=false`, `last_test_passed=false`, `status_note='Chưa kết nối — thiếu API key hợp lệ'`. Tài khoản seed `SYSTEM_ADMIN` thật.

**Test Steps:**
1. **(a-1)** `PUT /integrations/gemini` body `{"endpoint":"https://x","connected":true}` *(chưa test kết nối bao giờ)*.
2. **(a-2)** `PUT /integrations/gemini` body `{"endpoint":"https://x","lastTestPassed":true,"lastTestedAt":"2026-08-04T00:00:00Z"}`.
3. **(a-3)** `PUT /security-policies/SEC-DEFAULT` body `{"maxLoginAttempts":5,"updatedBy":"attacker@x.vn","updatedAt":"2020-01-01T00:00:00Z"}`.
4. **(b-1)** `PUT /integrations/gemini` body `{"endpoint":"https://x","credentialMasked":"AIzaSy…REAL_KEY"}`.
5. **(b-2)** `POST /sent-reports/compliance` body `{…,"period":123}` *(số thay vì `"Weekly"`)*.
6. **(b-3)** `PUT /security-policies/SEC-DEFAULT` body `{"maxLoginAttempts":"năm"}` *(chuỗi thay vì int)*.
7. Sau **mỗi** bước: đọc lại bản ghi từ **DB** (không qua API) và so từng cột.

**Expected Result (PASS):**
- (a-1) → **409 `AIS-163`**; DB `connected` **vẫn `false`**. *(Chấp nhận cả 200 + bỏ qua `connected`, miễn là DB không đổi — nhưng **409 tốt hơn**: nó nói cho FE biết thao tác không có hiệu lực, thay vì im lặng.)*
- (a-2) → 200; `last_test_passed` và `last_tested_at` **KHÔNG đổi** — chỉ `POST /integrations/{code}/test-connection` được sửa 2 cột này
- (a-3) → 200; `max_login_attempts = 5` (**có** áp dụng), nhưng `updated_by` = **email của tài khoản đang đăng nhập** (do `AuditorAware` điền — D9), **không** phải `attacker@x.vn`; `updated_at` là **now()**, không phải 2020
- (b-1) → **400/422**, hoặc 200 mà `credential_cipher` **KHÔNG đổi**. **Tuyệt đối không** được lưu chuỗi đó vào `credential_cipher` dưới dạng plaintext (BR-164) — và **không** ghi nó vào log/`error_logs` (`TC-SEC-018`)
- (b-2) → **400** với `$.error.code` và `$.error.details` nêu tên field `period`; **không** tạo dòng `sent_reports` nào
- (b-3) → **400**; `max_login_attempts` **giữ giá trị cũ**
- **Mọi bước:** phản hồi là D2 envelope `{"error":{"code","message","details"}}`, `details` là **mảng**, không bao giờ `null`

**Expected Result (FAIL):**
- Bất kỳ bước nào trả **200 mà DB không đổi và không có tín hiệu gì cho FE** ⇒ **chính là bẫy D12**. Đây là kết quả FAIL **khó nhận ra nhất**: test viết cẩu thả (chỉ assert HTTP 200) sẽ **PASS**. Vì vậy bước 7 — đọc lại từ DB — **không được bỏ**, và không được thay bằng gọi lại `GET`
- (a-1) trả 200 **và** `connected=true` ⇒ **BR-163 chưa từng chạy** vẫn nguyên như mô tả CF-14, chỉ là chuyển chỗ hỏng từ FE sang BE
- (b-1) lưu key thật ⇒ vi phạm BR-164, và key nằm plaintext trong cột đáng lẽ là ciphertext — **không ai grep ra được**

**Current Status:** 🔴 Not written

---

#### MF12-TC-CON-009 — Bước 0: migration KHÔNG chạm bảng của module khác, app khởi động được

**Severity:** `CRITICAL`
**Feature Under Test:** `V22__mf12_platform_services.sql` · `V23__mf12_seed_integrations.sql` · Spring context
**Test File:** `contract/Mf12OwnershipContractIT.java` · **TDD Phase:** 🔴 RED
**Oracle Source:** `02_Module_Completion_Workflow.md` **§2.1 Bước 0** + **L30/L31/L36** (`M12-D01`, `M12-D02`, `M12-D12`) + `00_Common_Conventions.md` §0-BIS

**⚠️ Vì sao cần một TC cho chuyện "hiển nhiên":** cả 3 lỗi mà TC này bắt đều **không có triệu chứng** cho tới lần chạy đầu tiên **sau khi merge** — tức là nổ vào máy người khác, không phải máy người viết. `M12-D02` là ca thật: TDS v1.1 khai 2 `CREATE TABLE IF NOT EXISTS` + 4 endpoint mà MF07 đã sở hữu; `IF NOT EXISTS` khiến Flyway **vẫn xanh** trong khi hai người có hai schema khác nhau.

**Test Steps:**
1. Đọc nội dung `V22__mf12_platform_services.sql` dưới dạng text.
2. `@SpringBootTest(webEnvironment = RANDOM_PORT)` — khởi động toàn ứng dụng với Flyway thật (Testcontainers PostgreSQL 16).
3. `RequestMappingHandlerMapping.getHandlerMethods()` → gom mọi path bắt đầu `/api/v1/` kèm class khai báo.
4. Truy vấn `information_schema.columns` cho `audit_logs`.
5. `ls` thư mục migration trong classpath.

**Expected Result (PASS):**
- (1) → **KHÔNG** khớp regex `CREATE TABLE\s+(IF NOT EXISTS\s+)?(audit_logs|security_policies|checklist_groups|checklist_items)` — 4 bảng của module khác
- (1) → **KHÔNG** chứa chuỗi `REVOKE` (`M12-D13` — role `sfms_app` không tồn tại, lệnh này làm Flyway đỏ)
- (1) → **KHÔNG** chứa `REFERENCES users(` — bảng tên thật là `user_accounts` (C2)
- (2) → context **khởi động thành công**. *(Đây là toàn bộ nội dung của assert: hai `@RequestMapping` cùng path làm `IllegalStateException: Ambiguous mapping` ngay lúc boot — không cần assert gì thêm.)*
- (3) → `/api/v1/checklist-groups` map về **đúng 1** class, và class đó là **`com.sfms.mf07.web.ChecklistGroupController`**
- (4) → `audit_logs` có **đủ** `id`(uuid) · `code` · `actor` · `actor_user_id`(uuid) · `prev_hash` · `entry_hash`; và `actor_user_id`/`prev_hash`/`entry_hash` đều **`is_nullable = YES`**
- (5) → có `V22__*` và `V23__*`; **không** có `V16__mf12_*`/`V17__mf12_*`; **không** file nào trùng số V

**Expected Result (FAIL):**
- `CREATE TABLE audit_logs` ⇒ **Flyway đỏ** (bảng đã có từ `V1`) — nổ với mọi người đã chạy `dev`
- `CREATE TABLE IF NOT EXISTS checklist_groups` ⇒ **Flyway XANH nhưng schema phụ thuộc thứ tự merge** — lớp lỗi tệ nhất: hai máy hai schema, không lệnh nào báo
- Controller thứ hai trên `/api/v1/checklist-groups` ⇒ **app không khởi động** cho **cả nhóm**
- `entry_hash CHAR(64) NOT NULL` ⇒ `ALTER` fail vì `V1`/`V2` đã sinh dòng audit trước khi chuỗi hash tồn tại

**Current Status:** 🔴 Not written

---

### 4.4. E2E TEST CASES

---

#### MF12-TC-E2E-001 — Vòng đời AI: MF05 dùng AI → audit → UC-119

**Severity:** `HIGH`
**Feature Under Test:** `Caterer dùng AI Assist ở MF05 → System Admin mở /audit-logs thấy dòng tương ứng → người dùng mở UC-119 thấy lịch sử hội thoại của mình`
**Test File:** `integration/AiLifecycleE2EIT.java` · **TDD Phase:** 🔴 RED · ⚠️ BLOCKED bởi L6 + MF05 CF-13
**Condition Ref:** `COND-01`, `COND-05`, `COND-44`, `COND-48`
**Oracle Source:** `TDS` §13 hàng `E2E-12-01` (nguyên văn) + **BR-157** + **BR-170** + `ADR-M12-01`

**Preconditions:** DB sạch + seed MF01 (3 user), MF02 (`SCH-001`), MF05 (`MENU-2026-30`), `FX-022` fake Gemini.

**Test Steps:**
1. Login Caterer → `POST /api/v1/menus/{code}/ai-suggest` (MF05).
2. Login System Admin → `GET /audit-logs?search=AI` → assert.
3. Login **cùng** Caterer → `GET /ai/conversations` → assert.
4. Login **Medical** → `GET /ai/conversations` → assert.
5. Kiểm hash chain sau chuỗi.

**Expected Result (PASS):**
- (1) → 200; 1 dòng `ai_requests` `module='MF05_MENU'`, prompt **đã redact**
- (2) → có dòng audit cho lượt gọi AI kèm `actor` = caterer, `ip` khác null (BR-170)
- (3) → Caterer thấy **đúng** hội thoại của mình (nối bằng **user id**, không bằng tên)
- (4) → Medical **không** thấy hội thoại của Caterer (`TC-SEC-005`)
- (5) → `GET /audit-logs/integrity` → `valid: true` (chuỗi hash không vỡ sau chuỗi thao tác thật)
- Không có prompt/response trong file log (`TC-SEC-007`)

**Expected Result (FAIL):**
- Không có dòng audit ⇒ không truy được ai đã dùng AI và cho việc gì (BR-170); và chi phí AI không quy được về người dùng
- Hash chain vỡ sau chuỗi thao tác đồng thời ⇒ `prev_hash` tính ngoài transaction (`TC-012`)

**Current Status:** 🔴 Not written

---

#### MF12-TC-E2E-002 — Vòng đời checklist → chấm điểm → báo cáo → lịch sử

**Severity:** `CRITICAL`
**Feature Under Test:** `Medical Staff sửa checklist (UC-186) → chấm điểm bếp (UC-014, MF07) → gửi báo cáo tuần (UC-185) → School Admin nhận thông báo → xem lại ở UC-190`
**Test File:** `integration/ChecklistReportE2EIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-16`, `COND-17`, `COND-18`, `COND-21`, `COND-23`
**Oracle Source:** `TDS` §13 hàng `E2E-12-02` (nguyên văn) + **BR-210** → **BR-100/104** (MF07) → **BR-209** → **BR-158/159** → **BR-177**
**⚠️ Ranh giới (L7/CF-3):** đây là chuỗi **xuyên MF12 ↔ MF07**. Bước chấm điểm test chi tiết ở `MF07-TC-011`; ở đây chỉ assert **liên kết thông suốt**.

**Test Steps:**
1. Medical `SCH-001` → `POST /checklist-groups` thêm nhóm mới + `DELETE` (ẩn) 1 nhóm cũ.
2. MF07 → `POST /kitchen-inspections` chấm điểm với bộ mục **mới**.
3. Medical → `POST /sent-reports/compliance` cho kỳ chứa phiên đó.
4. Kiểm event tới MF10.
5. Medical → `GET /sent-reports` (UC-190) → `GET /{code}` → `download`.
6. Kiểm dữ liệu lịch sử của nhóm đã ẩn.

**Expected Result (PASS):**
- (1) → nhóm mới `is_active=true`, nhóm cũ `is_active=false`, `checklist_item_results` cũ **còn nguyên**
- (2) → phiên mới dùng **bộ mục mới** (không có nhóm đã ẩn); `score` tính **server-side** (MF07 C11)
- (3) → 200 MSG-091; `payload.avgScore` tính **từ phiên thật** ở bước (2)
- (4) → event `ComplianceReportSent` phát 1 lần (L28 — không phát từ client)
- (5) → thấy báo cáo, detail trả **snapshot** (`TC-024`), tải được **cả** PDF + XLSX (`TC-INT-015`); mỗi lượt tải có audit (BR-177)
- (6) → phiên **cũ** (trước bước 1) **vẫn** render đủ mục đã ẩn
- **Nối bằng ID xuyên suốt**: `checklist_items.code` → `checklist_item_results.item_id` → `kitchen_inspections.code` → `sent_reports.payload`. Assert **không** chỗ nào join bằng **tên nhóm/tên mục**

**Expected Result (FAIL):**
- (2) phiên mới vẫn có nhóm đã ẩn ⇒ BR-210 không có hiệu lực; Medical Staff sửa checklist mà kiểm tra thực tế không đổi
- (6) phiên cũ mất mục ⇒ xoá cứng (`TC-INT-011`); hồ sơ định kỳ tháng của MF07 không tái tạo được
- (3) `avgScore` không khớp phiên thật ⇒ Báo cáo Tuân thủ ATTP gửi School Administration sai số liệu
- Join bằng tên mục ⇒ đổi tên mục là đứt liên kết lịch sử

**Current Status:** 🔴 Not written

---

#### MF12-TC-E2E-003 — Hợp đồng FE↔BE: đổi baseURL là chạy

**Severity:** `HIGH`
**Feature Under Test:** shape response của 6 resource MF12 vs `mock-server.ts`
**Test File:** `integration/ContractSnapshotIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-24`, `COND-25`, `COND-35`
**Constraint Ref:** `TDS §17.1 C13` *(phần tên field)* + **D3/D4**
**Oracle Source:** `TDS` §9 mở đầu (*hợp đồng bất biến*) + §17.1 **C13** (**24 tên field** bất biến) + `TDS` §14.3 + **D3** (`baseURL: "/api/v1"`) + **D4** (ghi đè phần định dạng ngày của C13 — L3)
**Kỹ thuật:** Contract Testing (snapshot **khoá**, bỏ qua giá trị)

**Test Steps:**
1. Gọi `GET` cho `audit-logs`, `error-logs`, `integrations`, `security-policies`, `sent-reports`, `checklist-groups`.
2. Trích **tập khoá** JSON (đệ quy) mỗi response.
3. So với snapshot khoá xuất từ `mock-server.ts` (`src/test/resources/contracts/mf12-mock-keys.json` — Minh cập nhật khi mock đổi).
4. Kiểm định dạng giá trị theo D4 + C13.

**Expected Result (PASS):**
- Tập khoá backend ⊇ tập khoá mock — **không thiếu** khoá nào
- Có đủ **24** tên field C13: `actor`, `ip`, `action`, `resource`, `result`, `timestamp`, `severity`, `module`, `path`, `stackTrace`, `sessionInfo`, `endpoint`, `endpointPlaceholder`, `connected`, `statusNote`, `purpose`, `lastTestedAt`, `reportType`, `period`, `rangeLabel`, `sentAt`, `sentBy`, `avgScore`, `threeStepSummary`, `counts`
- 5 khoá phân trang đúng; **không** wrapper
- **D4:** `timestamp`/`sentAt`/`lastTestedAt`/`updatedAt` khớp ISO instant; `date` khớp `^\d{4}-\d{2}-\d{2}$` — **không** `dd/MM/yyyy`
- **C13:** `period` = `"Weekly"`/`"Daily"` (`ReportPeriod` ngoài D6 — `TC-027`)
- **D3:** mọi path dưới `/api/v1`
- `security-policies` **không** trả trong endpoint public (`TC-SEC-013`)

**Expected Result (FAIL):**
- Thiếu/đổi tên khoá ⇒ **16 file FE gãy** — TC này chạy **mỗi PR**
- Ngày `dd/MM/yyyy` ⇒ vi phạm D4; và vì `audit_logs` là nền chung của 13 module, định dạng lệch ở đây lan ra cả hệ thống

**Current Status:** 🔴 Not written
**Implementation Note:** snapshot chỉ so **khoá**. ⚠️ D4 đổi **định dạng giá trị** (không đổi khoá) ⇒ bước (4) là **bắt buộc**, không thay được bằng so khoá.

---

## 5. Red-Green-Refactor Tracker

> Cột 🟢 GREEN điền **commit hash** khi test xanh. Không điền trước khi Red Gate (§5.4) PASS.
> Cột ⚠️ = blocked/disabled kèm lý do.

### 5.1. Unit (31)

| TC ID | Test File | 🔴 RED | 🟢 GREEN | ⚠️ / 🔵 REFACTOR |
|-------|-----------|--------|----------|------------------|
| `MF12-TC-001` | `ai/AiChatServiceTest.java` | `[ ]` | | ⚠️ blocked L6 (mock chưa có AI) |
| `MF12-TC-002` | `ai/AiArchiveServiceTest.java` | `[ ]` | | ⚠️ blocked L6 · outbox trong cùng transaction |
| `MF12-TC-003` | `ai/AiChatServiceTest.java` | `[ ]` | | ⚠️ blocked L6 · tách `REQUIRES_NEW` cho dòng TIMEOUT |
| `MF12-TC-004` | `ai/PromptRedactorTest.java` | `[ ]` | | ⚠️ blocked L6 · redact ở tầng port (dùng chung 13 module) |
| `MF12-TC-005` | `ai/AiChatServiceTest.java` | `[ ]` | | ⚠️ blocked L6 |
| `MF12-TC-006` | `integration/IntegrationServiceTest.java` | `[ ]` | | |
| `MF12-TC-007` | `integration/IntegrationServiceTest.java` | `[ ]` | | hằng `TEST_VALIDITY=10m` + `Clock` inject |
| `MF12-TC-008` | `integration/IntegrationServiceTest.java` | `[ ]` | | |
| `MF12-TC-009` | `integration/CredentialExposureTest.java` | `[ ]` | | ⚠️ blocked phần input L17 (FE chưa gửi `apiKey`) |
| `MF12-TC-010` | `integration/CredentialCipherTest.java` | `[ ]` | | |
| `MF12-TC-011` | `integration/CredentialCipherTest.java` | `[ ]` | | giữ khoá cũ khi rotate (§7 R4) |
| `MF12-TC-012` | `audit/AuditHashChainTest.java` | `[ ]` | | `canonical(row)` thành hàm riêng, thứ tự field xác định |
| `MF12-TC-012b` | `audit/AuditHashChainTest.java` | `[ ]` | | |
| `MF12-TC-013` | `audit/AuditImmutabilityTest.java` | `[ ]` | | ⚠️ assert **SQL sinh ra**, không assert exception |
| `MF12-TC-014` | `audit/AuditPortTest.java` | `[ ]` | | |
| `MF12-TC-015` | `policy/SecurityPolicyServiceTest.java` | `[ ]` | | ⚠️ khoảng cho phép 4 field chờ Huy |
| `MF12-TC-016` | `policy/SecurityPolicyServiceTest.java` | `[ ]` | | `JsonNullable<T>` cho toàn bộ DTO update |
| `MF12-TC-017` | `checklist/ChecklistScopeTest.java` | `[ ]` | | |
| `MF12-TC-018` | `checklist/ChecklistLifecycleTest.java` | `[ ]` | | |
| `MF12-TC-019` | `checklist/ChecklistLifecycleTest.java` | `[ ]` | | ⚠️ phải khớp `MF07-TC-016` (L7) |
| `MF12-TC-020` | `metrics/SystemHealthJobTest.java` | `[ ]` | | job idempotent theo tuần |
| `MF12-TC-021` | `report/ComplianceReportSendServiceTest.java` | `[ ]` | | |
| `MF12-TC-022` | `report/ViolationReportSendServiceTest.java` | `[ ]` | | ⚠️ `staffCount` phải là 0 (L26) |
| `MF12-TC-023` | `report/DeprecatedReportTest.java` | `[ ]` | | gate ở service để chặn cả 2 đường |
| `MF12-TC-024` | `report/SentReportSnapshotTest.java` | `[ ]` | | |
| `MF12-TC-025` | `report/ReportPrivacyTest.java` | `[ ]` | | |
| `MF12-TC-026` | `mapper/DateFormatContractTest.java` | `[ ]` | | converter dùng chung `common/` (Nam) |
| `MF12-TC-027` | `mapper/ReportPeriodContractTest.java` | `[ ]` | | ⚠️ đề nghị Nam bổ sung `ReportPeriod` vào D6 |
| `MF12-TC-028` | `platform/CodePolicyTest.java` | `[ ]` | | |
| `MF12-TC-029` | `metrics/UsageStatisticsTest.java` | `[ ]` | | ⚠️ blocked L12 · enum `common/enums/RoleKey` |
| `MF12-TC-030` | `audit/AuditRetentionTest.java` | `[ ]` | | ⚠️ **xung đột BR-171 vs BR-162 — chờ Huy** |

### 5.2. Security (18)

| TC ID | Test File | 🔴 RED | 🟢 GREEN | ⚠️ / 🔵 REFACTOR |
|-------|-----------|--------|----------|------------------|
| `MF12-TC-SEC-001` | `web/PlatformSecurityTest.java` | `[ ]` | | danh sách 31 endpoint dùng chung SEC-015 |
| `MF12-TC-SEC-002` | `web/PlatformAuthorizationMatrixTest.java` | `[ ]` | | ⚠️ `002a`/`002b` cả hai `@Disabled` — chờ CF-21 (Nam) |
| `MF12-TC-SEC-003` | `web/PlatformSecurityTest.java` | `[ ]` | | |
| `MF12-TC-SEC-004` | `web/PlatformAuthorizationMatrixTest.java` | `[ ]` | | ⚠️ blocked L6 · **TC ngược chiều** — chống over-block |
| `MF12-TC-SEC-005` | `web/AiAccessSecurityTest.java` | `[ ]` | | ⚠️ blocked L6 |
| `MF12-TC-SEC-006` | `web/CredentialLeakTest.java` | `[ ]` | | mask `Authorization`/`X-Api-Key` ở logger |
| `MF12-TC-SEC-007` | `web/AiPrivacyTest.java` | `[ ]` | | ⚠️ blocked L6 |
| `MF12-TC-SEC-008` | `web/ReportExportPrivacyTest.java` | `[ ]` | | kiểm cả metadata PDF/XLSX |
| `MF12-TC-SEC-009` | `web/AuditPrivacyTest.java` | `[ ]` | | |
| `MF12-TC-SEC-010` | `web/RateLimitTest.java` | `[ ]` | | ⚠️ blocked L6 · đếm theo user, không IP |
| `MF12-TC-SEC-011` | `web/ChecklistScopeSecurityTest.java` | `[ ]` | | rút `SchoolScopeGuard` dùng lại MF07 |
| `MF12-TC-SEC-012` | `web/ActuatorExposureTest.java` | `[ ]` | | allow-list endpoint Actuator, không `include: "*"` |
| `MF12-TC-SEC-013` | `web/PublicPolicySecurityTest.java` | `[ ]` | | ⚠️ endpoint mới · cần Minh đổi 2 màn `(auth)` |
| `MF12-TC-SEC-014` | `web/ReportGateBypassTest.java` | `[ ]` | | |
| `MF12-TC-SEC-015` | `web/PlatformAuthorizationMatrixTest.java` | `[ ]` | | 217 tổ hợp — `@MethodSource` |
| `MF12-TC-SEC-016` | `web/FieldLevelAuthorizationTest.java` | `[ ]` | | 4 nhóm field §16 thành enum |
| `MF12-TC-SEC-017` | `web/AuditSpecInjectionTest.java` | `[ ]` | | allow-list field lọc, loại `entry_hash`/`prev_hash` |
| `MF12-TC-SEC-018` | `web/ErrorLogPrivacyTest.java` | `[ ]` | | sanitize ở `ErrorLogWriter` (lúc ghi) |

### 5.3. Integration + E2E (24)

| TC ID | Test File | 🔴 RED | 🟢 GREEN | ⚠️ / 🔵 REFACTOR |
|-------|-----------|--------|----------|------------------|
| `MF12-TC-INT-001` | `integration/AiChatIT.java` | `[ ]` | | ⚠️ blocked L6 |
| `MF12-TC-INT-002` | `integration/AiPortSharedIT.java` | `[ ]` | | ⚠️ blocked L6 + MF05 CF-13 · giao MF12↔MF05 |
| `MF12-TC-INT-003` | `integration/AuditIntegrityIT.java` | `[ ]` | | ⚠️ **viết lại 04/08 (`M12-D13`)** — chuỗi hash thay GRANT; đối chứng dương ở bước (1) |
| `MF12-TC-INT-004` | `integration/AuditApiSurfaceIT.java` | `[ ]` | | chốt 405 vs 404 → ghi vào §9.2 |
| `MF12-TC-INT-005` | `integration/PaginationContractIT.java` | `[ ]` | | dùng lại cho 12 module (nền chung Nam) |
| `MF12-TC-INT-006` | `integration/PaginationContractIT.java` | `[ ]` | | |
| `MF12-TC-INT-007` | `integration/ErrorLogContractIT.java` | `[ ]` | | |
| `MF12-TC-INT-008` | `integration/AuditFilterIT.java` | `[ ]` | | ⚠️ ranh giới `dateTo` bao gồm cả ngày |
| `MF12-TC-INT-009` | `integration/AuditIntegrityIT.java` | `[ ]` | | ⚠️ endpoint mới §9.2 #4 |
| `MF12-TC-INT-010` | `integration/AuditCoverageIT.java` | `[ ]` | | audit aspect ở `common/` (Nam) |
| `MF12-TC-INT-011` | `integration/ChecklistLifecycleIT.java` | `[ ]` | | ⚠️ phải khớp `MF07-TC-INT-010` (L7) |
| `MF12-TC-INT-012` | `integration/ReportSendIT.java` | `[ ]` | | ⚠️ event → MF10 (L28) |
| `MF12-TC-INT-013` | `integration/SchemaOwnershipIT.java` | `[ ]` | | ⚠️ cần đối chứng dương (bẫy #4) |
| `MF12-TC-INT-014` | `integration/SecurityPolicyEffectIT.java` | `[ ]` | | giao MF12↔MF01 |
| `MF12-TC-INT-015` | `integration/ReportDownloadIT.java` | `[ ]` | | ⚠️ endpoint mới L25 |
| `MF12-TC-INT-016` | `integration/IntegrationFlowIT.java` | `[ ]` | | |
| `MF12-TC-INT-017` | `integration/SystemMetricsIT.java` | `[ ]` | | ⚠️ blocked L12 (3 UC hardcode ở FE) |
| `MF12-TC-INT-018` | `integration/SchemaShapeIT.java` | `[ ]` | | ⚠️ cần đối chứng dương (bẫy #4) |
| `MF12-TC-INT-019` | `integration/ReportCenterIT.java` | `[ ]` | | ⚠️ endpoint mới §9.2 #28 |
| `MF12-TC-INT-020` | `integration/SentReportShapeIT.java` | `[ ]` | | |
| `MF12-TC-INT-021` | `integration/IdentityContractIT.java` | `[ ]` | | ⚠️ **D1** — TDS §5.2 cần cập nhật (L1) |
| `MF12-TC-E2E-001` | `integration/AiLifecycleE2EIT.java` | `[ ]` | | ⚠️ blocked L6 · CI nightly |
| `MF12-TC-E2E-002` | `integration/ChecklistReportE2EIT.java` | `[ ]` | | CI nightly · xuyên MF12↔MF07 |
| `MF12-TC-E2E-003` | `integration/ContractSnapshotIT.java` | `[ ]` | | chạy **mỗi PR** — hàng rào hợp đồng |

**Bổ sung 2026-08-04 (Bước 1) — 5 TC mới, `73 → 78`:**

| TC ID | File | 🔴 | ✅ | Ghi chú |
|---|---|---|---|---|
| `MF12-TC-INT-022` | `integration/AiAssistPortWiringIT.java` | `[ ]` | | **L33/`M12-D09`** — bean adapter gỡ fallback `503 MENU-073` của MF05. TC liên module **đo được trước/sau** |
| `MF12-TC-SEC-019` | `web/AuditImmutabilityTest.java` | `[ ]` | | **L35/`M12-D13`** — thay vế `REVOKE` đã gỡ. 5 verb ghi → 405 `IAM-162` |
| **`MF12-TC-CON-007`** | `contract/SentReportRoleShapeContractIT.java` | `[ ]` | | **D11 bắt buộc** (§7 Bước 1 việc 3). Dùng **tài khoản seed thật** — `@WithMockUser` không mang claim `schoolId` (D13) |
| **`MF12-TC-CON-008`** | `contract/WriteDtoContractIT.java` | `[ ]` | | **D12 bắt buộc**. ⚠️ Bước 7 (đọc lại từ **DB**) không được bỏ — test chỉ assert HTTP 200 sẽ **PASS giả** |
| `MF12-TC-CON-009` | `contract/Mf12OwnershipContractIT.java` | `[ ]` | | **L30/L31/L36** — migration không chạm 4 bảng của module khác; Spring context khởi động được |

**Bốn TC chuyển `@Disabled("M12-D02 — thuộc MF07")`:** `TC-017`, `TC-018`, `TC-INT-011`, `TC-SEC-011` — `TestSpec-MF07` đã phủ. Không xoá (Policy 4.4): ghi chú "phải khớp `MF07-TC-*`" trong đó là thông tin còn giá trị, xoá đi thì lần sau có người viết lại.

### 5.4. Red Gate Protocol (CASE 2.0 — GATE-2)

> ⭐ **Gate QUAN TRỌNG NHẤT.** Chạy toàn bộ suite với stub **ném exception**. Mọi test PHẢI **FAIL**. Test PASS ngay → **AP-AI-002** → reject và rewrite.

**Stub cho Red Phase (Java):**

```java
package com.sfms.mf12.impl;

import com.sfms.mf12.ai.AiChatService;
import com.sfms.mf12.audit.AuditLogService;
import com.sfms.mf12.integration.IntegrationService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/** RED PHASE STUB — mọi method ném. Xoá trước khi merge (§7 bước 6). */
@Service @Profile("red-gate")
class AiChatServiceRedStub implements AiChatService {
    private static final String MSG = "Not implemented — Red Phase stub";
    @Override public ChatResponse ask(ChatRequest r, String actor) { throw new UnsupportedOperationException(MSG); }
    @Override public java.util.List<ConversationResponse> history(String actor) { throw new UnsupportedOperationException(MSG); }
}

/** ⚠️ verifyChain PHẢI ném, KHÔNG được trả valid=false mặc định (bẫy #1). */
@Service @Profile("red-gate")
class AuditLogServiceRedStub implements AuditLogService {
    @Override public void append(AuditEntry e) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public IntegrityResult verifyChain() { throw new UnsupportedOperationException("Not implemented"); }
}

/** ⚠️ save() PHẢI ném, KHÔNG được từ chối mặc định (bẫy #3). */
@Service @Profile("red-gate")
class IntegrationServiceRedStub implements IntegrationService {
    @Override public IntegrationResponse save(String code, SaveRequest r) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public TestResult testConnection(String code, TestRequest r) { throw new UnsupportedOperationException("Not implemented"); }
}
```

```bash
mvn test -Dspring.profiles.active=red-gate -Dtest='com.sfms.mf12.**' \
         2>&1 | tee target/red-gate-evidence.log
grep -E "Tests run:.*Failures" target/red-gate-evidence.log
```

**Red Gate Verification:**

| Nhóm TC | Stub Result | Expected | Actual | Root Cause (nếu PASS bất thường) |
|-------|-------------|----------|--------|----------------------------------|
| `TC-001…030` + `TC-012b` (unit) | `throw UnsupportedOperationException` | 🔴 FAIL (31/31) | ☐ FAIL ☐ PASS | ☐ Tautology ☐ Shared state ☐ Assert quá yếu ☐ Hallucinated contract |
| `TC-SEC-001…018` | filter chain rỗng | 🔴 FAIL (16/18, trừ 2 `@Disabled` của CF-21) | ☐ FAIL ☐ PASS | ☐ Test chỉ assert `status != 500` |
| `TC-INT-001…021` | controller trả `501` | 🔴 FAIL (21/21) | ☐ FAIL ☐ PASS | ☐ Không assert DB, chỉ assert HTTP |
| `TC-E2E-001…003` | như trên | 🔴 FAIL (3/3) | ☐ FAIL ☐ PASS | |

**Bẫy Red Gate riêng của MF12 — kiểm THỦ CÔNG (bắt buộc):**

| # | Bẫy | Vì sao dễ PASS giả | Cách chống |
|---|---|---|---|
| **1** | **`verifyChain` (TC-012b, TC-INT-009)** — ca phát hiện phá vỡ | Stub trả `valid=false` hoặc chưa implement → **mọi ca "phát hiện được" PASS ngay**. Đây là bẫy nguy hiểm nhất: test toàn vẹn xanh giả trong khi cơ chế chưa tồn tại. | Mỗi test **phải có ≥ 1 ca `valid=true`** trong cùng method; và assert **`brokenAt` chính xác**, không chỉ `valid==false`. Stub **ném**, không trả `false`. |
| **2** | **`TC-INT-003`** — ⚠️ **bẫy này ĐÃ SẬP THẬT; đây là ghi chép về nó** | v1.0 cảnh báo: *"nếu `sfms_app` **chưa được tạo** hoặc **không có quyền gì**, `UPDATE` cũng fail → **PASS vì lý do sai**"*. Bước 0 ngày 04/08: **role đó chưa bao giờ tồn tại** — tức TC-INT-003 bản cũ không thể đúng dù viết cẩn thận tới đâu. Cảnh báo đúng, nhưng người viết không có cách nào biết nó đang mô tả chính mình: thông tin *"role không tồn tại"* chỉ nằm trong `V1`/`application.yml`, **không có trong bất kỳ tài liệu nào**. | `TC-INT-003` viết lại theo **chuỗi hash** (`M12-D13`) — không phụ thuộc quyền DB nên bẫy biến mất. Đối chứng dương chuyển thành bước (1): chuỗi **lành** phải trả `valid=true`. **Quy tắc rút ra: mọi TC dựa vào hạ tầng (role DB, extension, ENV) phải có một bước xác minh hạ tầng đó CÓ THẬT — hoặc đừng dựa vào nó.** |
| **3** | **`TC-006`/`TC-007` (gate BR-163)** | Stub chưa implement → `save()` ném → mọi ca "chặn" PASS | Phải có **≥ 1 ca `save()` thành công** (`FX-009`, test PASS trong 10 phút) trong cùng test class |
| **4** | **`TC-INT-013`/`TC-INT-018`** (bảng/câu lệnh **không** tồn tại) | `V22` trống hoặc DB trống ⇒ "không tồn tại" đúng tự nhiên ⇒ PASS vô nghĩa | **Đối chứng dương bắt buộc**: assert các bảng của MF12 (`audit_logs`, `ai_requests`, `external_integrations`, `sent_reports`, `checklist_groups`) **CÓ** tồn tại; và `V22` **có** nội dung `CREATE TABLE` cho chúng |
| **5** | **`TC-SEC-006`/`TC-SEC-007`/`TC-SEC-008`** (grep log/file → no output) | Log rỗng / file chưa sinh ⇒ grep rỗng ⇒ PASS | Assert **log có nội dung** (grep thấy `conversationId`, `module`) trước khi assert **không** thấy credential/prompt. Với file: assert file **tồn tại và có nội dung** trước khi grep PII |
| **6** | **`TC-SEC-004`** (Parent **PHẢI** gọi được `/ai/chat`) | Đây là ca **dương**; stub ném ⇒ FAIL đúng. Nhưng nếu ai đó "sửa cho an toàn" bằng cách chặn hết thì TC này là **cái duy nhất** phát hiện | Đưa `TC-SEC-004` vào **cùng test class** với `TC-SEC-015` để mọi thay đổi phân quyền đều chạy qua nó |
| **7** | **`TC-002`** (archive lỗi → rollback) | Nếu chưa có transaction nào, "không có dữ liệu" đúng tự nhiên ⇒ PASS | Assert **hai chiều**: ca thành công (`TC-001`) **có** dòng `ai_requests`, ca lỗi **không** có — cả hai trong cùng class |
| **8** | **`TC-016`** (partial merge) | "Field giữ nguyên" có thể do **chưa implement gì cả** | Assert **hai chiều**: field **được gửi** đã đổi **và** field **không gửi** giữ nguyên |

**Red Gate Evidence:**

- Stub commit hash: `___`
- Tất cả FAIL? ☐ Yes → **GATE-2 PASS** (T2→T3) → tiếp tục implement
- Log: `target/red-gate-evidence.log`
- **8 bẫy trên đã kiểm thủ công?** ☐ Yes (người kiểm: `_______`, ngày: `______`)

> **Nếu bất kỳ test PASS:** dừng lại, xác định root cause, rewrite test từ TC-ID spec với Fixture Isolation Pattern (§4).

---

## 6. Entry / Exit Criteria

### Entry Criteria

- [ ] `TDS-MF12_Backend.md` đã **Approved** — hiện `Draft`
- [ ] **TDS-MF12 đã cập nhật theo `00_Common_Conventions.md` D1–D7**: §5.2 có `code` + PK UUID (⚠️ `external_integrations` `UNIQUE(code)` — L11); §9.2 ngày ISO, không `@JsonFormat`; §10 ghi envelope D2 + dịch 6 mã tiếng Việt sang tiếng Anh khi có MSG. ⚠️ **Nếu chưa, TestSpec này vẫn đúng** (D1–D7 ưu tiên cao hơn) — ghi vào biên bản review cho Mạnh (L1–L4)
- [ ] **Nút cổ chai `00_` §4b:** MF01 (auth/JWT/RBAC + **bảng `security_policies` + seed `SEC-DEFAULT`**) và MF02 (`PagedResponse`, `Specification`) chạy được
- [ ] Flyway **V1** đã chạy, gồm `users`, `schools`, **`security_policies`** (L10 — MF12 **không** tạo lại)
- [x] ~~**Role DB `sfms_app` đã được tạo với GRANT hạn chế**~~ ⛔ **GỠ — `M12-D13`.** Role không tồn tại trong dự án; `TC-INT-003` đã viết lại theo chuỗi hash và **không cần** hạ tầng này
- [ ] `common/enums/RoleKey` (D6) đã có ở module `common` (Nam sở hữu)
- [ ] `ObjectStoragePort`, `NotificationPort`, `AiAssistPort` đã có **interface**
- [ ] Logic Issues §2 đã xác nhận: **Nam** cho L1–L4 (đã Approved) + **L7/CF-3** (ranh giới MF12↔MF07) + **L10/CF-6** (MF12↔MF01) + **L27/CF-21** (School Admin đọc audit) + `ReportPeriod` vào D6 (`TC-027`) · **Huy** cho L5/L8/L9/L29 + khoảng cho phép policy (`TC-015`) + **xung đột BR-171 vs BR-162** (`TC-030`) · **Mạnh** cho L11 + `V22` chỉ `ALTER` `audit_logs` và không tạo lại 4 bảng của module khác (~~GRANT~~ — `M12-D13`) · **Minh** cho L6/L12/L16/L17/L18/L19/L20/L21/L22/L23/L24/L25/L26/L28
- [ ] Fixtures `FX-001`…`FX-034` đã tạo (⚠️ **không** dùng credential thật)
- [ ] Testcontainers + Docker chạy được trên dev + CI
- [ ] `SFMS_CRED_MASTER_KEY` test đã cấu hình (`FX-026`)

### Exit Criteria (Definition of Done)

**Bắt buộc:**

- [ ] `mvn test` — unit xanh; `@Disabled` **chỉ** gồm `TC-SEC-002a`/`002b` (một trong hai, chờ CF-21)
- [ ] `mvn verify` — integration xanh (Failsafe + Testcontainers, **Flyway `V1`…`V23`**)
- [ ] Coverage: **≥ 90% line** cho `com.sfms.mf12.{ai, audit, integration}`; **100% branch** cho `AiArchiveService`, `AuditLogService`, `CredentialCipher`, `IntegrationService.save()`
- [ ] **Mỗi condition §3 TDS-03 có ≥ 1 TC xanh** — **50/50**
- [ ] **Mỗi hàng §13 của TDS được cover:** `UT-12-01…25` → 25/25 · `IT-12-01…16` → 16/16 · `SEC-12-01…10` → 10/10 · `E2E-12-01…02` → 2/2
- [ ] **Mỗi constraint C1–C14 có ≥ 1 TC:**

| Constraint | TC cover |
|---|---|
| C1 (AI qua port + archive cùng transaction) | TC-001, TC-002, TC-003, TC-004, TC-005, TC-INT-001, TC-INT-002 |
| C2 (audit append-only + hash chain) | TC-012, TC-012b, TC-013, TC-014, TC-INT-003, TC-INT-004, TC-INT-009, TC-INT-010 |
| C3 (credential AES-256-GCM, chỉ masked) | TC-009, TC-010, TC-011, TC-SEC-006, TC-INT-016 |
| C4 (save chỉ khi test PASS ≤10 phút) | TC-006, TC-007, TC-008, TC-INT-016 |
| C5 (checklist xoá = ẩn) | TC-018, TC-019, TC-INT-011 |
| C6 (kỳ rỗng chặn + PDF/XLSX + không lộ HS + snapshot) | TC-021, TC-022, TC-024, TC-025, TC-INT-012, TC-INT-015, TC-INT-020 |
| C7 (UC-188 deprecated → 409) | TC-023, TC-INT-019 |
| C8 (`security_policies` do MF01 sở hữu) | TC-INT-013, TC-INT-014 |
| C9 (metric từ Actuator, không bảng riêng) | TC-020, TC-INT-017, TC-INT-018 |
| C10 (`PUT` partial merge) | TC-016 |
| C11 (không tin dữ liệu client) | TC-006 (ca 3), TC-SEC-016, TC-SEC-014 (ca 4) |
| C12 (`school_id` từ JWT) | TC-017, TC-SEC-011, TC-SEC-016, TC-INT-012 |
| C13 (tên field JSON + `period`) | TC-027, TC-E2E-003 · *(phần ngày đã bị D4 ghi đè → TC-026)* |
| C14 (`error-logs` không lược field) | TC-INT-007, TC-SEC-018 |

- [ ] **Mỗi quyết định D1–D7 có ≥ 1 TC:** D1 → `TC-INT-021` · D2 → mọi TC error path + `TC-015` (`details`) · D3 → `TC-E2E-003` · D4 → `TC-026` · D5 → `TC-028` · D6 → `TC-029` · D7 → `TC-003` (MSG tiếng Anh) + `TC-002`/`TC-006`/`TC-015`/`TC-023` (chỉ assert code)
- [ ] **Oracle Source** — mọi TC có dòng `Oracle Source`:
  ```bash
  grep -c "^\*\*Oracle Source:\*\*" TestSpec-MF12_AI_Supporting_Services.md   # >= 73
  ```
- [ ] **Không mã bịa:** mọi UC/BR/MSG tồn tại đúng nguồn (§9 phụ lục — Huy xác nhận)
- [ ] **Contract Existence:** `mvn -q test-compile` → BUILD SUCCESS
- [ ] **Fixture Isolation:** grep static entity → no output (§4)
- [ ] **Không cú pháp sai stack:**
  ```bash
  grep -rnE "export interface|prisma\.|npx prisma|model [A-Z][A-Za-z]* \{" \
       TestSpec-MF12_AI_Supporting_Services.md src/test/java/com/sfms/mf12/
  # Expected: no output
  ```
- [ ] **Không còn `@JsonFormat`** (D4) · **Không khai lại enum D6** (`RoleKey`)
- [ ] **§11.3 bước 4 (BẮT BUỘC) xanh** — sửa thẳng SQL vào `audit_logs` ⇒ `GET /audit-logs/integrity` trả `valid=false` + `brokenAt` đúng dòng (`TC-INT-003`); và 5 verb ghi qua HTTP đều **405 `IAM-162`** (`TC-SEC-019`)
- [ ] **§14.2 — 4 truy vấn/lệnh kiểm chứng đều "No output" / 0:**
  ```bash
  grep -rniE "AIza[A-Za-z0-9_-]{10,}|sk-[A-Za-z0-9]{20,}|password=" target/*.log   # BR-164
  # + grep prompt/response AI trong log                                             # BR-173
  # + pdftotext/unzip báo cáo tìm HS-\d+[A-Z]-\d+                                   # BR-181
  ```
  ```sql
  SELECT count(*) FROM audit_logs
   WHERE actor IS NULL OR ip IS NULL OR resource IS NULL OR timestamp IS NULL;   -- BR-170, Expected: 0
  ```
- [ ] **`GET /audit-logs/integrity` trả `valid: true`** sau toàn bộ suite (chuỗi hash không vỡ)
- [ ] **§14.3 — `audit-logs` không expose `put`/`delete`** trong OpenAPI (`"not-exposed"`)
- [ ] **Red Gate (§5.4)** — tất cả FAIL với stub; **8 bẫy MF12** đã kiểm thủ công (ký tên)
- [ ] Swagger đối chiếu §9.2: **31 endpoint** khớp path/method (Minh xác nhận)
- [ ] Smoke test §11.3 bước 7 xanh

**Nghiệm thu chéo theo RACI (`00_` §1):**

- [ ] **Nam** (Architecture + Final Approve): xác nhận L1–L4 phản ánh đúng D1–D7; **chốt 4 ranh giới liên module** — MF12↔MF07 (L7/CF-3: checklist + báo cáo ATTP), MF12↔MF01 (L10/CF-6: `security_policies`), MF12↔MF10 (L28/CF-29: thông báo), CF-21 (School Admin đọc audit); chốt `ReportPeriod` vào D6 hay không; chốt 405 vs 404 cho `audit-logs`
- [ ] **Huy** (Traceability): §2 + §9 phụ lục không mã bịa; chốt chênh **12 vs 15 vs 13 UC** (L5); cập nhật §1.12 cho **UC-187/188 Deprecated** (L8); xác nhận **BR-165 vừa là job vừa là màn xem** (L9); cấp **MSG chính thức cho 6 mã `AIS-*`/`IAM-*` tiếng Việt** (L4/D7); xác nhận **khoảng cho phép** của 4 field policy (`TC-015`); **giải quyết xung đột BR-171 (retention) vs BR-162 (không xoá)** (`TC-030`); xác nhận BR-015 (L29)
- [ ] **Mạnh** (Backend/DB): DB assertion đúng bảng/cột §5.2 *(TDS đã cập nhật D1 ngày 04/08)*; **`V22` chỉ `ALTER` `audit_logs`, KHÔNG `CREATE`, KHÔNG `REVOKE`** (`M12-D01`/`M12-D13`) và **không** tạo lại `security_policies`/`checklist_groups`/`checklist_items` (`M12-D02`); `external_integrations` `UNIQUE(code)` không `school_id` (L11); cập nhật `data_model.puml` (L11 `ExternalIntegration`)
- [ ] **Minh** (FE↔BE): `TC-E2E-003` xanh với snapshot khoá; **bổ sung route + resource mock cho AI Chat** (L6 — chặn 11 TC); **bổ sung `apiKey` vào payload** (L17 — chặn BR-164); **thay `testConnection()` giả** (L16); **đổi 2 màn `(auth)` sang `/public/password-policy`** (L21); thêm `dateFrom`/`dateTo` vào `filters` audit (L19); thêm checkbox `requireMixedCase` (L18); **gỡ dialog gửi báo cáo sự cố** (L24); thêm nút Tải về PDF/XLSX (L25); thay hằng số 3 màn system (L12); gỡ lệnh gọi `POST /notifications` ở client (L28); đồng bộ interface `ViolationSentReport` (L26)
- [ ] **Thảo** (chủ module + Lead QA): tự review, ký `Author`

### Suspension Criteria

| # | Điều kiện | Hành động |
|---|---|---|
| S1 | MF01 chưa có bảng + seed `security_policies` | Dừng `TC-015`, `TC-016`, `TC-INT-013`, `TC-INT-014`, `TC-SEC-013`. **KHÔNG** tự tạo bảng trong `V22` để "chạy tạm" — sẽ conflict với `V1` (L10). Cùng lý do với `audit_logs` (`M12-D01`) |
| S2 | ~~Role `sfms_app` chưa được tạo~~ → **`V22` chứa `REVOKE`/`GRANT`, hoặc `CREATE TABLE` một trong 4 bảng của module khác** | `TC-CON-009` đỏ ⇒ **Flyway sẽ đỏ / app không khởi động trên máy cả nhóm**. Dừng, gỡ lệnh đó khỏi migration rồi mới chạy tiếp (`M12-D01`/`M12-D02`/`M12-D13`) |
| S3 | **Mock chưa có AI Chat** (L6/CF-2) | **11 TC blocked**: TC-001…005, TC-SEC-004/005/007/010, TC-INT-001/002, TC-E2E-001. **Không** tính vào coverage gate |
| S4 | **FE chưa gửi `apiKey`** (L17/CF-15) | `TC-009` blocked phần input; vẫn test được `TC-010`/`TC-011` (cipher) và phần response masking |
| S5 | 3 màn system còn hardcode (L12/CF-19) | `TC-029`, `TC-INT-017` blocked — 3 UC (UC-057/149/150) **chưa nghiệm thu được** |
| S6 | MF07 chưa có `kitchen_inspections`/`violation_records` | `TC-021`/`TC-022` dùng seed trực tiếp bảng; `TC-E2E-002` blocked ở bước chấm điểm |
| S7 | MF05 chưa có endpoint AI | `TC-INT-002` blocked (giao MF12↔MF05) |
| S8 | MF10 chưa có `notifications` | `TC-INT-012` chỉ assert **event phát**, không assert gửi (L28) |
| S9 | CF-21 chưa được Nam chốt | `TC-SEC-002a` **và** `002b` giữ `@Disabled`; **không** khoá một bên |
| S10 | **Xung đột BR-171 vs BR-162** chưa được Huy chốt | `TC-030` khoá theo **BR-162** (không DELETE) và ghi vào biên bản; **không** implement job `DELETE FROM audit_logs` |
| S11 | Docker/Testcontainers không chạy trên CI | Dừng tầng integration. **KHÔNG** đổi sang H2 — mất `JSONB`, mất `uuid`/`gen_random_uuid()`, mất `GIN index` → `TC-INT-020`, `TC-INT-021`, `TC-CON-009` vô nghĩa |
| S12 | TDS-MF12 **chưa** cập nhật theo D1–D7 | **Vẫn viết test theo D1–D7**; ghi vào biên bản review; **không** sửa TestSpec để khớp TDS cũ |

---

## 7. Rollback Plan

> ⚠️ **KHÔNG dùng `npx prisma migrate reset`** — dự án dùng **Flyway**, Community **không** auto-undo.
> ⚠️⚠️ **MF12 có ràng buộc nghiêm ngặt nhất trong 13 module:** `audit_logs` là **bằng chứng vận hành bất biến (BR-162)**, và `external_integrations` chứa **credential** cần rotate khi nghi lộ (BR-164).

```bash
# ── DEV / TEST ONLY ────────────────────────────────────────────────
# 1) Xoá dữ liệu test MF12 — ⚠️ CHỈ trên DB test. audit_logs là bằng chứng vận hành (BR-162)
psql "$SFMS_DB_URL_OWNER" -c "TRUNCATE ai_requests, ai_conversations, error_logs,
    checklist_item_results, checklist_items, checklist_groups,
    sent_reports, external_integrations, audit_logs CASCADE;"

# 2) Hạ schema MF12 — migration BÙ
#    src/main/resources/db/migration/V18__mf12_rollback.sql
#      DROP TABLE IF EXISTS ai_requests, ai_conversations, error_logs,
#                           external_integrations, sent_reports CASCADE;
#      -- ⚠️ audit_logs: GIỮ NGUYÊN (BR-162 — nhật ký bất biến là bằng chứng vận hành)
#      -- ⚠️ security_policies: KHÔNG chạm (thuộc MF01 — L10/C8)
mvn flyway:migrate

# 3) Dọn file test (báo cáo PDF/XLSX)
rm -rf ./target/test-reports/*

# 4) ROTATE CREDENTIAL nếu nghi lộ (BR-164) — §12.2 bước 4 của TDS
#    - Đổi GEMINI_API_KEY / FCM / SMTP ở nhà cung cấp
#    - Tăng SFMS_CRED_KEY_VERSION, mã hoá lại toàn bộ external_integrations
#    - GIỮ khoá cũ tới khi mã hoá lại xong (xem TC-011 ca 3)

# 5) Testcontainers
docker rm -f $(docker ps -aq --filter "label=org.testcontainers=true") 2>/dev/null || true

# 6) Revert code test + gỡ Red Phase stub
git checkout -- src/test/java/com/sfms/mf12/ src/test/resources/fixtures/
git rm src/main/java/com/sfms/mf12/impl/*RedStub.java
```

**Quy tắc rollback — MF12 nghiêm ngặt nhất:**

| # | Quy tắc | Lý do |
|---|---|---|
| **R1** | ⛔ **TUYỆT ĐỐI KHÔNG `DROP`/`TRUNCATE`/`DELETE` `audit_logs` trên môi trường có dữ liệu thật.** | `TDS` §12.2 bước 2 nguyên văn: *"⚠️ **TUYỆT ĐỐI KHÔNG DROP `audit_logs`** nếu đã có dữ liệu thật… **GIỮ NGUYÊN** (BR-162 — nhật ký bất biến là bằng chứng vận hành)"*. Ngoài ra xoá dòng giữa chuỗi **phá vỡ hash chain** ⇒ `TC-012b` báo `valid=false` vĩnh viễn, mất khả năng chứng minh toàn vẹn cho **toàn bộ** lịch sử còn lại. |
| **R2** | ⛔ **KHÔNG chạm `security_policies`** — bảng thuộc **MF01** (L10/C8) | Rollback MF12 mà drop bảng của MF01 ⇒ toàn bộ đăng nhập của hệ thống hỏng |
| **R3** | Rollback **phải giữ traffic đọc** `audit_logs`: dừng traffic **ghi**, không dừng đọc | Điều tra sự cố cần tra audit trong lúc rollback |
| **R4** | **Rotate credential** khi nghi lộ, và **giữ khoá cũ** tới khi mã hoá lại xong | §12.2 bước 4. Xoá khoá cũ trước khi mã hoá lại ⇒ **mất toàn bộ credential** (không giải mã được bản ghi cũ) — xem `TC-011` ca 3 |
| **R5** | Số hiệu migration MF12 là `V22`/`V23`, rollback `V18` — nếu đụng module khác, **hỏi Nam** | `00_` §4b: migration là tài nguyên chung có thứ tự |
| **R6** | **Smoke test bắt buộc sau rollback** (§12.2 bước 5): `GET /audit-logs/integrity` → `valid: true`, **và** 3 cột `actor_user_id`/`prev_hash`/`entry_hash` vẫn còn | ~~GRANT của `sfms_app`~~ — `M12-D13`. Rủi ro thật của rollback MF12 **không** phải mất quyền mà là **`DROP COLUMN entry_hash`**: gỡ cột rồi thêm lại thì `integrity` báo gãy **toàn bảng** dù không ai sửa gì. ⇒ Rollback `V22` **chỉ được gỡ 3 index**, không gỡ cột |
| **R7** | Rollback test **không** kéo theo rollback TDS | TDS là tài liệu nộp report |

---

## 8. CASE 2.0 Anti-Pattern Detection

| AP-ID | Anti-Pattern | Dấu hiệu | Check | Gate chặn |
|-------|-------------|----------|-------|-----------|
| AP-AI-001 | **Unconstrained Generation** | TC không reference constraint/BR/D1–D7 nào | ☐ | G-0 — grep `Oracle Source` (§6) |
| AP-AI-002 | **Green-from-Birth** | Test PASS với stub ném exception (§5.4) | ☐ | **G-2 ★** — Red Gate + **8 bẫy MF12** |
| AP-AI-003 | **Implicit Decision** | Test assume quyết định kiến trúc không có ADR | ☐ | G-1 — trỏ `ADR-M12-01…06`, `ADR-000`, hoặc **D1–D7** |
| AP-AI-004 | **Layer Violation** | Test verify controller có business logic | ☐ | G-4 — §3 TDS-01 phân tầng |
| AP-AI-005 | **Hallucinated Contract** | Test import service/type không tồn tại | ☐ | G-3 — `mvn test-compile` |
| **AP-AI-011** | **Crypto Theater** *(riêng MF12)* | Test "đã mã hoá" chỉ assert `cipher != plaintext`; không kiểm IV random, không kiểm auth tag, không kiểm khoá từ ENV | ☐ | **G-6 ★** — `TC-010` bắt buộc 4 ca (round-trip · 2 lần khác nhau · sửa byte ném · khoá không hardcode) |
| **AP-AI-012** | **Integrity Theater** *(riêng MF12)* | Test hash chain chỉ assert `valid==true` trên chuỗi sạch; không có ca **phá vỡ giữa chuỗi** | ☐ | **G-7 ★** — `TC-012b` bắt buộc 4 ca (sạch · sửa giữa · xoá giữa · sửa cuối) |

**Rủi ro riêng của MF12 — reviewer kiểm thêm:**

| # | Rủi ro | Dấu hiệu | Cách kiểm |
|---|---|---|---|
| M1 | **Bịa mã BR/MSG** — MF12 có nhiều BR/UC deprecated (BR-184, BR-213, UC-187/188) và nhiều MSG chưa vào traceability | Dùng BR-213 làm oracle cho báo cáo **mới** · dùng BR-184 cho `staffCount` · gán MSG-091/092/094 vào MF07 (nhóm của `error_message.md` §4) | Đối chiếu §9 phụ lục với `traceability_matrix.md` §1.12 — **Huy** |
| M2 | **Tự cấp MSG mới** — **6 mã** `AIS-157/163/164/184/188/162` **không** có MSG tương ứng | TC assert một chuỗi MSG không tồn tại trong `error_message.md` | `grep` từng MSG ID; error code kỹ thuật thì OK, **MSG mới thì không** (L4/D7) |
| M3 | **Vượt ranh giới module** | TC test logic chấm điểm (MF07), khoá tài khoản (MF01), gửi/retry thông báo (MF10), fallback AI của MF05 | §3 TDS-01 bảng "NGOÀI phạm vi"; L7, L10, L28 |
| M4 | **Tự chọn bên đúng cho CF** | TC khoá cứng một bên của CF-21 hoặc xung đột BR-171/BR-162 mà không `@Disabled` + ghi chú | Policy cuối §2.2. **Ngoại lệ hợp lệ:** L1–L4 (D1–D7 Approved) |
| M5 | **Khôi phục hành vi đã descope** | Xuất hiện `latestPestControl` trong báo cáo **mới** · `staffCount > 0` · tạo `INCIDENT_SUMMARY` mới · bảng `system_metrics`/`pest_control_logs` | `TC-022`, `TC-023`, `TC-INT-018`, `TC-INT-020` |
| M6 | **Cú pháp sai stack** | `export interface`, `prisma.`, `makeEntity()`, `npm test`, `npx prisma` | grep gate §6 |
| M7 | **Bỏ sót D1–D7** | Còn `@JsonFormat`, `dd/MM/yyyy` trong assert, PK `VARCHAR`, assert lỗi ở `$.message`, khai lại `RoleKey`, `details` null | grep gate §6 + `TC-026`, `TC-029`, `TC-INT-021`, `TC-015` |
| M8 | **Test một hạ tầng không tồn tại** *(nghiêm trọng nhất, riêng MF12 — đã xảy ra thật)* | v1.0 test `GRANT` của role `sfms_app`; role đó **chưa bao giờ được tạo** ⇒ TC đỏ vĩnh viễn **hoặc** pass giả, và **cả hai đều không nói gì về BR-162** | `M12-D13`: TC dựa vào hạ tầng (role DB, extension, ENV) phải **xác minh hạ tầng đó có thật** trước — hoặc chọn cơ chế không cần nó. `TC-INT-003` đã đổi sang chuỗi hash |
| M9 | **Credential thật trong fixture** | `FX-026`/`FX-027` chứa khoá thật thay vì chuỗi test | `grep -rniE "AIza[A-Za-z0-9_-]{30,}" src/test/` — chỉ được thấy chuỗi có `TEST` |
| M10 | **Grep-based TC xanh giả** | `TC-SEC-006/007/008` PASS vì log/file rỗng | Bẫy #5 §5.4 — assert log/file **có nội dung** trước khi grep |

**Kết quả review:**

- [ ] Không phát hiện anti-pattern nào → TDD spec **approved**
- [ ] Phát hiện AP → ghi vào bảng dưới → fix trước khi implement

| AP detected | TC ID | Mô tả | Fix action | Fixed? |
|------------|-------|-------|------------|--------|
| `AP-AI-___` | `MF12-TC-___` | | | ☐ |

---

## 9. Phụ lục — Danh sách mã UC/BR/MSG đã trích dẫn

> Bảng đối chiếu cho **Huy (Lead Traceability)**. Mọi mã phải tồn tại đúng nguồn; **không có mã nào do AI đặt ra**.

### 9.1. Use Case (15 — đúng `traceability_matrix.md` §1.12)

| UC ID | Tên UC (nguyên văn §1.12) | Actor | TC cover |
|---|---|---|---|
| UC-012 | Chat with AI | **User (mọi role)** | TC-001…005, TC-SEC-004, TC-SEC-007, TC-SEC-010, TC-INT-001, TC-E2E-001 ⚠️ blocked L6 |
| UC-119 | View History Chat | User | TC-005, TC-SEC-005, TC-INT-001 ⚠️ blocked L6 |
| UC-040 | Manage External Integrations | System Admin | TC-006…011, TC-SEC-006, TC-INT-016 |
| UC-057 | Monitor System Activities | System Admin | TC-INT-017, TC-SEC-009 ⚠️ blocked L12 |
| UC-106 | View Audit Logs | System Admin | TC-012…014, TC-SEC-002, TC-SEC-009, TC-SEC-017, TC-INT-003…005, TC-INT-008…010 |
| UC-111 | View Error Logs | System Admin | TC-INT-007, TC-SEC-018 |
| UC-149 | View System Status | System Admin | TC-INT-016 (ca 5), TC-INT-017 ⚠️ blocked L12 |
| UC-150 | View Usage Statistics | System Admin | TC-029, TC-INT-017 ⚠️ blocked L12 |
| UC-184 | Configure Security Policies | System Admin | TC-015, TC-016, TC-SEC-013, TC-INT-013, TC-INT-014 |
| UC-185 | Send Daily/Weekly Compliance Report | Medical Staff | TC-021, TC-025, TC-SEC-014, TC-INT-012, TC-INT-015, TC-E2E-002 |
| UC-186 | Manage Kitchen Inspection Checklist | Medical Staff | TC-017…019, TC-SEC-011, TC-INT-011, TC-E2E-002 |
| UC-187 | ATTP Report Center (hub) | Medical Staff | TC-INT-019 — ⚠️ **Deprecated 26/07/2026** (L8/CF-4), §1.12 chưa cập nhật |
| UC-188 | Send Incident Summary Report | Medical Staff | TC-023, TC-INT-019, TC-INT-020 — ⚠️ **Deprecated 26/07/2026**, chỉ đọc lịch sử |
| UC-189 | Send Violation Summary Report | Medical Staff | TC-022, TC-SEC-014, TC-INT-019 |
| UC-190 | View Sent Reports History | Medical Staff | TC-024, TC-INT-015, TC-INT-020, TC-E2E-002 |

> ⚠️ **L5/CF-1:** UC file MF12 có **thừa** UC-058, UC-079, UC-088, UC-136 — traceability xếp UC-058/088/136 ở **§1.10 (MF10)** và UC-079 ở **§1.9 (MF09)** ⇒ **không** TC nào gán 4 mã này. Ngược lại UC file **không có** UC-185…190. `00_` §3 ghi **12 UC**, §1.12 có **15**. Chờ Huy chốt, Nam cập nhật `00_` §3.
> ⚠️ **UC-186 liên quan MF07:** `MF07-TC-016`/`MF07-TC-INT-010` test cùng bảng từ phía đọc — xem L7/CF-3.

### 9.2. Business Rules

**Trong cột BR của §1.12 (oracle chính thức):**

| BR | Nội dung (tóm, nguyên văn `business_rules.md`) | § | TC cover |
|---|---|---|---|
| BR-157 | **Toàn bộ** hội thoại AI (gồm đề xuất thực đơn) phải được **lưu trữ** | §14 | TC-001, TC-002, TC-003, TC-INT-001, TC-INT-002, TC-SEC-007 |
| BR-163 | Kiểm tra kết nối **thành công trước khi lưu** cấu hình tích hợp | §14 | TC-006, TC-007, TC-008, TC-INT-016 |
| BR-164 | Credential dịch vụ ngoài phải **mã hóa** khi lưu | §14 | TC-009, TC-010, TC-011, TC-SEC-006, TC-SEC-012, TC-SEC-018, TC-INT-016 |
| BR-165 | **Tự động** biên soạn báo cáo sức khỏe hệ thống **hàng tuần** | §14 | TC-020, TC-INT-017 |
| BR-162 | Nhật ký audit **bất biến** — không ai chỉnh sửa/xóa | §14 | TC-013, TC-030, TC-INT-003, TC-INT-004 |
| BR-170 | Mọi hành động quan trọng ghi audit kèm **người thực hiện, thời gian, đối tượng** | §15 | TC-014, TC-INT-010, TC-E2E-001 |
| BR-171 | Phải có **chính sách lưu trữ** nhật ký (thời gian tối thiểu) | §15 | TC-030 — ⚠️ **xung đột với BR-162**, chờ Huy |
| BR-172 | Cơ chế **bảo vệ toàn vẹn** nhật ký | §15 | TC-012, TC-012b, TC-INT-009 |
| BR-174 | Hỗ trợ **truy vấn lịch sử vận hành** theo thời gian | §15 | TC-INT-008 |
| BR-002 | **5 lần** sai → khóa **15 phút**; ghi audit | §1 | TC-015, TC-INT-014, TC-SEC-013 |
| BR-004 | **(v1.9)** token **30 phút** + refresh sliding **14 ngày** | §1 | TC-015, TC-SEC-013 (`sessionTimeoutMinutes` **không** lộ ra public) |
| BR-006 | Chính sách độ mạnh mật khẩu (+ dẫn chiếu BR-015 — L29) | §1 | TC-015, TC-016, TC-SEC-013 |
| BR-209 | Báo cáo tuân thủ; **kỳ rỗng → chặn gửi (MSG-092)**; xuất PDF/Excel; MSG-091 | §12 | TC-021, TC-SEC-014, TC-INT-012, TC-INT-015 |
| BR-210 | Medical Staff CRUD checklist **không cần phê duyệt**, phạm vi **theo từng trường**; xoá = **ẩn** | §12 | TC-017, TC-018, TC-019, TC-SEC-003, TC-SEC-011, TC-INT-011, TC-E2E-002 |
| BR-175 | Quyền xem báo cáo theo vai trò | §16 | TC-SEC-002, TC-SEC-003, TC-SEC-015, TC-INT-019 |
| BR-177 | Báo cáo lưu tối thiểu **12 tháng**; mỗi lần xuất ghi **audit trail** | §16 | TC-024, TC-030, TC-INT-010, TC-INT-015, TC-INT-020 |
| BR-178 | Xuất **PDF + XLSX**; mỗi file gồm **tiêu đề, khoảng thời gian, tên trường, timestamp** | §16 | TC-021, TC-INT-015 |
| BR-181 | Báo cáo **không lộ danh tính HS**; đánh dấu **"Confidential"** | §16 | TC-004, TC-025, TC-SEC-008 |
| BR-158 | **Giờ yên tĩnh** 22:00–06:00 cho push không khẩn cấp | §13 | TC-INT-012 (chỉ assert event) — logic ở **MF10** (L28) |
| BR-159 | Push thất bại → **thử lại** → **email dự phòng** | §13 | như trên |
| BR-213 | *(Deprecated 26/07/2026)* Báo cáo tổng hợp sự cố (UC-188) | §16 | TC-023, TC-INT-019, TC-INT-020 — **không** sinh bản ghi mới |
| BR-214 | Báo cáo tổng hợp vi phạm; **kỳ rỗng → chặn (MSG-097)**; MSG-099; **không lặp** nội dung biên bản đã ký | §16 | TC-022, TC-SEC-014 |

**Nhắc tới nhưng KHÔNG dùng làm oracle chính thức:**

| BR | Vì sao nhắc | Xử lý |
|---|---|---|
| BR-007 | RBAC 7 role — nền của §16 | oracle của TC-SEC-* là **§16**; BR-007 là nguồn gián tiếp. ⚠️ **`RoleKey` của D6 là kebab-case**, khác nhãn BR-007 (`TC-029`) |
| BR-015 | BR-006 dẫn chiếu *"không trùng 3 mật khẩu gần nhất (BR-015)"* | **L29/CF-13** — **không có mục canonical riêng**; `noReuseLast3` test với `Oracle Source = BR-006`, **không** ghi BR-015 |
| BR-156 | Gemini đề xuất thực đơn | Thuộc **MF05**; MF12 chỉ cung cấp port (`TC-INT-002`). Oracle của archive là **BR-157** |
| BR-173 | PDPD — bảo vệ dữ liệu cá nhân trong log | oracle của `TC-004`, `TC-SEC-007`, `TC-SEC-009` là **TDS §14.2 + §1 Data Classification**; BR-173 nêu trong TDS nhưng **không** ở cột BR của §1.12 — ghi rõ để Huy đối chiếu |
| BR-176 | Giới hạn báo cáo trong phạm vi trường | Nhắc ở C12 và CF-21; **không** ở §1.12 → oracle của `TC-SEC-011` là **BR-210 + C12** |
| BR-182 | Snapshot báo cáo | Nhắc ở `UT-12-24`; oracle của `TC-024` là **ADR-M12-05 + BR-177** |
| BR-184 | Biên bản nhắm cá nhân | **Deprecated 26/07/2026** — `TC-022` assert `staffCount == 0` (L26) |
| BR-100 / BR-101 / BR-103 / BR-104 | Điểm vệ sinh, kiểm định kỳ, sức khỏe nhân viên | Thuộc **MF07**; MF12 dùng làm **nguồn dữ liệu** báo cáo (`TC-021`, `TC-E2E-002`) |
| BR-160 / BR-161 | Backup | §9.2 #27 (`GET /system/backups`); **không** có UC ở §1.12 → `TC-INT-017` ghi rõ nguồn là **§9.2 + MSG-070** |
| BR-207 | Checklist gốc | **BR-210 thay thế điều khoản tương ứng** (ghi chú §1.12 UC-186) ⇒ oracle là **BR-210** |
| BR-211 | Biên bản ký tay | Thuộc **MF07**; BR-214 dẫn chiếu (*"không lặp nội dung biên bản đã ký (BR-211)"*) → `TC-022` ca 3 |

### 9.3. System Messages (16 — đều tồn tại trong `error_message.md`)

| MSG ID | Nội dung nguyên văn | TC cover |
|---|---|---|
| MSG-004 | `Session expired. Please log in again.` | TC-SEC-001 |
| MSG-005 | `Access denied: You do not have permission to access this resource.` | TC-005, TC-SEC-002…005, TC-SEC-011, TC-SEC-015 |
| MSG-025 | `Database transaction failed. Rollback executed.` | TC-002 *(nhánh lỗi DB chung — `AIS-025`)* |
| MSG-029 | `Profile updated successfully.` | TC-015 ca (f) *(§1.12 gán MSG-029 cho UC-184)* |
| MSG-061 | `System load threshold reached. Auto-scaling initiated.` | TC-INT-017 *(warning UC-057)* |
| MSG-068 | `Push notification delivery queue is backed up. Retrying failed targets.` | TC-INT-012 *(warning BR-159 — logic ở MF10)* |
| MSG-069 | `External API response latency is high (exceeds 2000ms).` | TC-INT-016 *(warning UC-149)* |
| MSG-070 | `Backup space alert: Backup storage utilization is at 92% capacity.` | TC-INT-017 *(warning BR-160/161)* |
| MSG-073 | `Gemini AI Service is currently unavailable. Please try again later.` | TC-003 ⚠️ blocked L6 |
| MSG-091 | `Compliance report sent successfully to School Administration.` | TC-021, TC-INT-012, TC-E2E-002 |
| MSG-092 | `No kitchen inspection data available for the selected period. Cannot generate compliance report.` | TC-021, TC-SEC-014 |
| MSG-094 | `This checklist item/group has been used in saved inspection records. Deleting it will not remove historical data — it will only be hidden from new inspections. Continue?` | TC-019, TC-INT-011 |
| MSG-096 | `No incidents recorded for the selected period. Cannot generate incident summary report.` | TC-023 *(chỉ giá trị lịch sử — L8)* |
| MSG-097 | `No violations recorded for the selected period. Cannot generate violation summary report.` | TC-022, TC-SEC-014 |
| MSG-098 | `Incident summary report sent successfully to School Administration.` | TC-INT-020 *(đọc lịch sử — UC-188 deprecated)* |
| MSG-099 | `Violation summary report sent successfully to School Administration.` | TC-022 |

> ⚠️ **MSG-024 KHÔNG dùng ở MF12** — thuộc cơ chế khóa bếp đã descope của MF07 (xem `TestSpec-MF07` L6).
> ⚠️ **`error_message.md` §4 xếp MSG-091/092/094/096/097/098/099 vào nhóm "Food Safety Inspection" (MF07)** nhưng §1.12 gán cho MF12 — **L7/CF-3**, chờ Nam chốt ranh giới.

### 9.4. Error code kỹ thuật (TDS §10 — KHÔNG phải mã MSG mới)

`AIS-004` (401) · `AIS-005` (403) · `AIS-025` (500) · `AIS-061` (200/warn) · `AIS-068` (200/warn) · `AIS-069` (200/warn) · `AIS-070` (200/warn) · `AIS-073` (503) · `AIS-092` (422) · `AIS-094` (409) · `AIS-096` (422) · `AIS-097` (422) · **`AIS-157`** (500) · **`IAM-162`** (405) · **`AIS-163`** (422/409) · **`AIS-164`** (500) · **`IAM-184`** (422) · **`AIS-188`** (409) · `AIS-409` (409, D5)

> **Ghi chú cho Huy — 6 mã in đậm không có MSG tương ứng** trong `error_message.md` (TDS §10 đã ghi). Theo **D7**, các mã này hiện viết **tiếng Việt** và *"phải dịch sang tiếng Anh khi được cấp MSG chính thức"*.
> ⇒ **TestSpec này chỉ assert `$.error.code`** cho 6 mã đó (**L4**), **không** khoá assert chuỗi `message`. Khi Huy cấp MSG chính thức, cập nhật `TC-002`, `TC-006`, `TC-011`, `TC-015`, `TC-023`, `TC-INT-004` để assert thêm chuỗi.
> Ngoài ra §1.12 gán **MSG-043** cho UC-040 qua UC file (bước 8) nhưng §1.12 chỉ ghi MSG-004/005 — chênh lệch nhỏ, ghi lại để Huy đối chiếu.

### 9.5. Căn cứ pháp lý & ADR

| Nguồn | Nội dung dùng làm oracle | TC cover |
|---|---|---|
| **PDPD (Luật 91/2025)** | BR-173 (không log hội thoại/PII), BR-181 (báo cáo không lộ HS), BR-164 (credential) | TC-004, TC-025, TC-SEC-006…009, TC-SEC-018 |
| `ADR-000` §Mock Strategy | *"Gemini AI: trả mock JSON đúng schema"*; *"Mock external services PHẢI trả response đúng format contract"* | TC-001, `FX-022`…`FX-024` |
| `ADR-M12-01` | `AiAssistPort` là **cổng AI duy nhất** của hệ thống | TC-001, TC-INT-002 |
| `ADR-M12-02` | `audit_logs` append-only + **hash chuỗi** | TC-012, TC-012b, TC-013, TC-INT-003, TC-INT-009 |
| `ADR-M12-03` | Credential mã hoá **envelope**, khoá từ ENV, không plaintext | TC-009, TC-010, TC-011 |
| `ADR-M12-04` | Metric từ **Actuator**, **không** bảng metric riêng | TC-020, TC-INT-017, TC-INT-018 |
| `ADR-M12-05` | `sent_reports` **single-table + JSONB payload**; shape JSON giữ nguyên union | TC-024, TC-INT-020 |
| `ADR-M12-06` | Checklist xoá = **ẩn** | TC-018, TC-019, TC-INT-011 |
| `TDS` §11.3 bước 4 | **"XÁC NHẬN audit_logs bất biến (BR-162) — bước BẮT BUỘC"** | **TC-INT-003** |
| `TDS` §14.2 | 4 lệnh kiểm chứng: credential không log · hội thoại AI không log · báo cáo không chứa mã HS · audit đủ actor/IP | TC-SEC-006, TC-SEC-007, TC-SEC-008, TC-INT-010 |
| `TDS` §14.3 | `audit-logs` **không expose** `put`/`delete` (`"not-exposed"`) | TC-INT-004 |

### 9.6. `00_Common_Conventions.md` D1–D7

| Quyết định | Nội dung áp dụng | TC cover | Ghi đè gì của TDS-MF12 |
|---|---|---|---|
| **D1** | JSON `id` = `code`; PK UUID; path `{code}`; FK trong JSON dùng `code` | `TC-INT-021`, `TC-028` + mọi DB assertion (`WHERE code`) | §5.2 PK `VARCHAR` → **L1**. ⚠️ `external_integrations` `UNIQUE(code)` (không `school_id` — L11) |
| **D2** | Envelope `{"error":{"code","message","details"}}`; `details` **là mảng, không bao giờ null** | mọi TC error path + **`TC-015`** (`details.allowed`) | §10 chưa khai envelope → **L2** |
| **D3** | `baseURL = /api/v1` | `TC-E2E-003` | — (TDS đã dùng `/api/v1`) |
| **D4** | JSON ngày **ISO-8601**; bỏ `@JsonFormat` | `TC-026`, `TC-E2E-003` | §17.1 **C13** + CF-26 (`dd/MM/yyyy`) → **L3** |
| **D5** | Nhận `id` client → `code`, trùng 409; vắng thì server sinh; luôn trả `id` | `TC-028` | — (D5 lấy chuẩn từ MF05 CF-11) |
| **D6** | 8 enum dùng chung `common/enums/`; **`RoleKey` kebab-case** | `TC-029` | ⚠️ **`ReportPeriod` KHÔNG nằm trong 8 enum** ⇒ C13 (`"Weekly"`) vẫn có hiệu lực (`TC-027`); **đề nghị Nam bổ sung vào D6** |
| **D7** | `message` **tiếng Anh** nguyên văn `error_message.md`; mã BR tiếng Việt **phải dịch** khi có MSG | `TC-003` (MSG-073 tiếng Anh) · `TC-002`/`TC-006`/`TC-015`/`TC-023` (chỉ assert code) | §10 có **6 mã tiếng Việt** → **L4** |

> **Việc phải làm ở TDS-MF12** (checklist cuối `00_Common_Conventions.md`): §5.2 đổi PK `VARCHAR` → `UUID` + cột `code` (⚠️ `external_integrations` không `school_id`) · §9 bỏ `@JsonFormat`, ngày ISO · §5.2 không khai lại `RoleKey` · §10 ghi envelope D2 + đánh dấu 6 mã chờ MSG · §17 sửa **C13** (bỏ phần `dd/MM/yyyy`). **Mạnh** thực hiện, **Nam** approve lại.

### 9.7. Tổng kết coverage

| Nhóm | Số TC | Trạng thái ban đầu |
|---|---|---|
| Unit (service layer) | **31** | 🔴 31 (⚠️ 5 blocked L6: TC-001…005) |
| Security (RBAC / OWASP / PDPD / crypto) | **18** | 🔴 16 · ⚠️ 2 `@Disabled` (TC-SEC-002a/002b, CF-21) |
| Integration (API → DB, Testcontainers) | **21** | 🔴 21 (⚠️ 4 blocked: TC-INT-001/002 L6, TC-INT-017 L12) |
| E2E | **3** | 🔴 3 (⚠️ 1 blocked: TC-E2E-001 L6) |
| **Tổng** | **73** | 🔴 71 · ⚠️ 2 `@Disabled` · **11 blocked** chờ Minh/Nam |

| Đối chiếu | Kết quả |
|---|---|
| Condition §3 TDS-03 có TC | **50/50** |
| Constraint C1–C14 (§17.1 TDS) có TC | **14/14** |
| Quyết định D1–D7 có TC | **7/7** |
| Hàng §13 TDS (`UT`/`IT`/`SEC`/`E2E`) có TC | **53/53** |
| UC §1.12 có TC | **15/15** |
| BR trong §1.12 có TC | **23/23** |
| MSG trong §1.12 + §10 có TC | **16/16** |
| Logic Issue L1–L29 được xử lý rõ | **29/29** |

**Blocked / chờ người khác:**

| TC | Chờ ai | Việc gì |
|---|---|---|
| TC-001…005, TC-SEC-004/005/007/010, TC-INT-001/002, TC-E2E-001 (**11 TC**) | **Minh** | Bổ sung **route + resource mock cho AI Chat** (L6/CF-2) — nhóm bị chặn lớn nhất |
| `TC-009` (phần input) | **Minh** | Bổ sung `apiKey` vào payload `PUT /integrations` (L17/CF-15) — không có thì BR-164 không có gì để mã hoá |
| `TC-006`…`TC-008` (đường FE) | **Minh** | Thay `testConnection()` giả bằng lệnh gọi API thật (L16/CF-14) |
| `TC-SEC-013` | **Minh** | Đổi 2 màn `(auth)` sang `GET /public/password-policy` (L21/CF-20) |
| `TC-029`, `TC-INT-017` | **Minh** | Thay hằng số 3 màn system (L12/CF-19) — 3 UC chưa nghiệm thu được |
| `TC-023` (đường cũ) | **Minh** | Gỡ dialog gửi báo cáo sự cố (L24/CF-22) |
| `TC-INT-015` | **Minh** | Thêm nút Tải về PDF/XLSX (L25/CF-23) |
| `TC-INT-008` | **Minh** | Thêm `dateFrom`/`dateTo` vào `filters` audit (L19/CF-17) |
| `TC-016` | **Minh** | Thêm checkbox `requireMixedCase` (L18/CF-16) |
| `TC-INT-012` | **Minh** | Gỡ `POST /notifications` ở client (L28/CF-29) |
| `TC-SEC-002a/002b` | **Nam** | Chốt CF-21 — School Admin có được đọc `audit-logs` scope trường không |
| `TC-019`, `TC-INT-011`, `TC-021`, `TC-E2E-002` | **Nam** | Chốt ranh giới MF12↔MF07 (L7/CF-3) — checklist + báo cáo ATTP |
| `TC-INT-013`, `TC-INT-014` | **Nam** | Chốt ranh giới MF12↔MF01 (L10/CF-6) — `security_policies` |
| `TC-027` | **Nam** | Chốt `ReportPeriod` vào D6 hay giữ C13 |
| `TC-INT-004` | **Nam** | Chốt 405 vs 404 cho `PUT/DELETE /audit-logs` |
| `TC-015` | **Huy** | Xác nhận **khoảng cho phép** của 4 field chính sách (chỉ `maxLoginAttempts: 3–10` có trong `UT-12-15`) |
| `TC-030` | **Huy** | **Giải quyết xung đột BR-171 (retention) vs BR-162 (không xoá)** — archive hay giữ vĩnh viễn |
| `TC-002`, `TC-006`, `TC-011`, `TC-015`, `TC-023`, `TC-INT-004` | **Huy** | Cấp **MSG chính thức tiếng Anh** cho 6 mã `AIS-*`/`IAM-*` (L4/D7) |
| — | **Huy** | Chốt chênh **12 vs 15 vs 13 UC** (L5/CF-1); cập nhật §1.12 cho UC-187/188 Deprecated (L8/CF-4); xác nhận BR-165 vừa job vừa endpoint (L9/CF-5); xác nhận BR-015 (L29/CF-13) |
| `TC-INT-003` | **Mạnh** | `V22` **ALTER** (không `CREATE`) `audit_logs` thêm `prev_hash`/`entry_hash` **nullable**; `AuditService` tính chuỗi hash lúc ghi. ⛔ **không** `REVOKE`, **không** tạo role (`M12-D13`) |
| `TC-INT-013`, `TC-INT-021` | **Mạnh** | Cập nhật §5.2 theo D1 (PK UUID + `code`; `external_integrations` `UNIQUE(code)`); `V22` không tạo lại `security_policies`; cập nhật `data_model.puml` (L11) |

---

*TestSpec MF12 v1.0 — theo `06_Document/Templates/PHASE-4_Test-Spec.md` (ISO/IEC/IEEE 29119-3:2021), tích hợp CASE 2.0 Red Gate + Anti-Pattern Detection (bổ sung **AP-AI-011 Crypto Theater** và **AP-AI-012 Integrity Theater** riêng cho module này).*
*Áp dụng `07_implement/00_Common_Conventions.md` **D1–D7** (Approved 2026-07-30) — ưu tiên cao hơn ADR module; 4 điểm ghi đè ở §2.0 L1–L4.*
*Cú pháp đã dịch sang stack thật: **Java 25 / Spring Boot 4.1.x / JUnit 5 / Testcontainers / Flyway / Actuator / PDFBox + Apache POI** — không dùng TypeScript/Jest/Prisma của template minh hoạ.*
