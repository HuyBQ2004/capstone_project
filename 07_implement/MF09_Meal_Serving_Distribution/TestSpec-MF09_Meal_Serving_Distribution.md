# TEST-DRIVEN DEVELOPMENT SPECIFICATION
# MF09 — Meal Serving & Distribution (Phục vụ & Phân phát Bữa ăn)

| Field | Value |
|-------|-------|
| **Document ID** | `SFMS-MF09-TDD-001` |
| **Version** | `2.0` |
| **Date** | `2026-08-03` |
| **Status** | **`Approved`** |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 — Software Testing Part 3: Test Documentation |
| **Document Owner** | Trần Tuấn Minh (MinhTTHE186258) |
| **Author** | Trần Tuấn Minh — UI/UX & Frontend, chủ module MF09/MF11, Lead FE↔BE contract Reviewer |
| **Reviewed by** | `[ ] Nguyễn Phương Thảo — Lead Test/QA — Pending` · `[ ] Bùi Quang Huy — §2 Traceability (UC/BR/MSG) — Pending` · `[ ] Nguyễn Đức Mạnh — §5 Schema + §9 API — Pending` |
| **DPO Sign-off** | `[ ] Pending` — **bắt buộc**: module chứa **PII trẻ em** (họ tên HS, lớp, hồ sơ dị ứng đã xác minh, lịch sử tiêu thụ từng bữa) |
| **Approved by** | `[ ] Nguyễn Văn Nam — Final Approval Pending` |
| **Classification** | `Internal — Confidential` |

**References (nguồn oracle — mọi expected value trích từ đây, KHÔNG dùng giả định):**

| # | Nguồn | Dùng cho |
|---|-------|----------|
| 0 | **`07_implement/00_Common_Conventions.md` — D1–D7 ✅ Approved 30/07/2026 · D9/D10 ✅ Approved 31/07/2026 · D8 ⏳ chờ Nam** | **Quy ước ưu tiên CAO NHẤT** — ghi đè ADR module khi xung đột (xem §2.1 L1–L8) |
| 1 | `07_implement/MF09_Meal_Serving_Distribution/TDS-MF09_Backend.md` (`SFMS-MF09-IMP-001`) | Spec kỹ thuật gốc — §5.2 schema/CHECK, §8 interface + 6 port, §9 API, §10 error code, §13 kịch bản, §16 auth matrix, §17 constraint C1–C8 |
| 2 | `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` **§1.9** | Nguồn CHUẨN duy nhất của UC↔BR↔MSG cho MF09 (**13 UC**) — không nhận mã ngoài bảng này |
| 3 | `02_Requirements/02_UseCases/MF09_Meal_Serving_Distribution.md` | Normal/Alternative Flow (đặc biệt **UC-016 AF1**, **UC-103 AF1**). ⚠️ **không có** UC spec cho UC-079 (CF-M09-1) |
| 4 | `02_Requirements/00_BusinessRules/business_rules.md` §4 (BR-031/BR-201), §5 (BR-051/BR-060), §7 (BR-065/BR-066), §8 (BR-094), §10 (BR-120…BR-135) | Nội dung BR làm **oracle** |
| 5 | `02_Requirements/00_BusinessRules/error_message.md` **v1.8** | Chuỗi MSG **nguyên văn tiếng Anh** (oracle của assert message). MF09 dùng MSG-004/005/014/021/045 + **MSG-129…133** + dùng chung MSG-137/138/139/141 |
| 6 | `03_Design/08_FrontEnd/Web/scripts/semantic-audit.ts` §6 (sổ phát suất), §7 (kiểm thực trượt) | **6 luật** port sang test — tên luật copy **nguyên văn** từ script |
| 7 | `03_Design/08_FrontEnd/Web/src/lib/mock-data.ts` | **Nguồn fixture/seed** (§3 TDS-05) — mã bản ghi THẬT (`DSTU-0706A-002`, `MR-TD-3A-004`, `HS-3A-001`, `CAT-001`, `INSP-20260707-L`), **không bịa** |
| 8 | `03_Design/08_FrontEnd/Web/src/lib/api/mock-server.ts` + `db.ts` | Hợp đồng REST hiện tại (generic `PUT`, `paginate()`) — chỉ dùng để biết FE đang gọi gì, **không** dùng làm oracle định dạng |
| 9 | `07_implement/00_Team_Work_Assignment_Plan.md` §2b (2b-1…2b-7) | Hợp đồng phân trang/search/filter dùng chung |
| 10 | `03_Design/01_Diagrams/01_Class_Diagram/data_model.puml` — ràng buộc **C3** | Phát suất phải trỏ thực đơn có thật của đúng (ngày, khối, bữa) **và** bữa đó có phiên kiểm thực ĐẠT |
| 11 | `03_Design/02_ADR/ADR-000.md` | Stack + stub external service ở dev (SMTP/Redis tắt → job BR-133 test bằng event, không gửi thật) |
| 12 | PDPD — Nghị định 13/2023/NĐ-CP (theo ADR-000) | Căn cứ pháp lý của **BR-051 cấm sinh trắc học trẻ em** + cách ly phạm vi đọc (ADR-M09-6) |

> **⚠️ RÀNG BUỘC STACK — tài liệu này KHÔNG dùng cú pháp mẫu của `PHASE-4_Test-Spec.md`.**
> Template gốc minh hoạ bằng TypeScript/Jest/Prisma. Dự án dùng **Java 25 LTS · Spring Boot 4.1.x · PostgreSQL 16 · Flyway · Spring Security + JWT · MapStruct · springdoc-OpenAPI**.
> Test = **JUnit 5 + AssertJ + Mockito** (unit) · **`@SpringBootTest` + Testcontainers `PostgreSQLContainer` + MockMvc** (integration/security/contract) · fixture = **Java builder** (KHÔNG `makeEntity()` TypeScript) · rollback = **Flyway migration bù** (KHÔNG `npx prisma migrate reset`).

> **⚠️ ÁP DỤNG `00_Common_Conventions.md` — thứ tự ưu tiên khi xung đột: `00_Common_Conventions.md` > ADR module (TDS §3) > hợp đồng mock FE.**
> Hệ quả cho tài liệu này: **D1** JSON `id` = `code`, PK là UUID → DB assertion dùng `WHERE code = ?` · **D2** lỗi trong envelope `{"error":{"code","message","details"}}` → assert `$.error.code` · **D3** mọi path `/api/v1/...` · **D4** ngày/giờ JSON **ISO-8601** → **ghi đè** ADR-M09-5 + constraint **C8** + AP-M09-08 của TDS · **D5** nhận `id` client, trùng → 409 MSG-139 · **D6** enum **SCREAMING_SNAKE** (`ConsumptionStatus` đủ **4** giá trị kể cả `PENDING`; `AllergySeverity` = `MILD|MODERATE|SEVERE`, **không** `"Severe"`) · **D7** `message` **tiếng Anh** nguyên văn `error_message.md` → ghi đè các message tiếng Việt trong TDS §9.2 · **D9** `created_by`/`updated_by` reserved cho `BaseEntity` · **D10** không áp dụng (MF09 không upload file).

> **Quy ước TDD:** tài liệu này mô tả test cases **TRƯỚC** khi viết production code.
> Thứ tự: viết test (`*Test.java`) → chạy → xác nhận **FAIL 🔴** → implement → **PASS 🟢** → refactor 🔵.
> Không mark ✅ nếu `mvn verify` chưa xanh. Test data = **SYNTHETIC**, school `SCH-001`, caterer `CAT-001` — **không dùng PII thật của trẻ em**.

---

## CHANGELOG

> **Policy 4.4 — Immutable History:** không xóa thông tin cũ.

| Ngày | Người thực hiện | Nội dung thay đổi |
|------|-----------------|-------------------|
| 2026-08-01 | Trần Tuấn Minh | Khởi tạo v1.0 — TDD spec cho MF09. Nguồn: `TDS-MF09_Backend.md` (§13 làm xương sống, §9/§10/§16/§17), **`00_Common_Conventions.md` D1–D10**, traceability **§1.9** (13 UC), `business_rules.md` §4/§5/§7/§8/§10, `error_message.md` **v1.8**, `semantic-audit.ts` §6/§7 (**6 luật**), fixture từ `mock-data.ts`. Tổng **56 test case**: 16 unit · 13 security · 18 integration · 5 contract · 4 E2E. Ghi nhận **26 Logic Issue**: **L1–L8** là D1–D9 **ghi đè** ADR/constraint của TDS (nặng nhất: **L3** — D4 ISO ghi đè ADR-M09-5/C8/AP-M09-08 đang bắt giữ `dd/MM/yyyy`), **L9–L22** kế thừa CF-M09-1…14, **L23–L26** là lệch mới phát hiện khi soạn TestSpec (mã phiên kiểm thực trong ví dụ §9.2 sai; §10 ghi "chưa có MSG chính thức" đã lỗi thời; thiếu mã cho HTTP 429; thiếu luật `TEN_KHONG_KHOP_HOC_SINH`). |
| 2026-08-03 | Trần Tuấn Minh | **v2.0 — Bước 1 (`02_Module_Completion_Workflow.md` §7), Status Draft → Approved.** (1) **L1–L8 và L23–L26 nay ĐÃ ĐÓNG** — `TDS-MF09` v2.0 đã sửa theo, ghi thành `M09-D01`…`M09-D10` ở §3; TestSpec **không phải đổi expected value nào** vì đã viết theo D1–D9 từ đầu (đúng thứ tự ưu tiên `00_Common_Conventions.md` > ADR module). (2) **Bổ sung 2 test case bắt buộc (`M09-D10`)**: `MF09-TC-CON-006` cho **D11** và `MF09-TC-INT-019` cho **D12** — `grep "oneOf" ` trên file này trước đó ra **0 kết quả**, đúng lớp lỗi mà §6 workflow cảnh báo là **cả 13 TestSpec đều không phủ**. (3) Package `com.sfms.…serving` → **`com.sfms.mf09`** (18 chỗ, `M09-D03`). (4) 5 port bị gỡ theo `M09-D08` — TC nào mock port cũ chuyển sang mock `ReferenceResolver`. Tổng test case: **56 → 58**. |

---

## MỤC LỤC

1. [Thông tin Module](#1-thông-tin-module) · 1.1 [AI Generation Context](#11-ai-generation-context-case-20)
2. [Logic Issues Resolved](#2-logic-issues-resolved)
3. [Test Design Specification](#3-test-design-specification)
4. [Test Case Specification](#4-test-case-specification) · [Unit](#41-unit-test-cases) · [Security](#42-security-test-cases) · [Integration](#43-integration-test-cases) · [Contract](#44-contract-test-cases--openapi-v3api-docs-d8) · [E2E](#45-e2e-test-cases)
5. [Red-Green-Refactor Tracker](#5-red-green-refactor-tracker) · 5.4 [Red Gate Protocol](#54-red-gate-protocol-case-20--gate-2)
6. [Entry / Exit Criteria](#6-entry--exit-criteria)
7. [Rollback Plan](#7-rollback-plan)
8. [CASE 2.0 Anti-Pattern Detection](#8-case-20-anti-pattern-detection)
9. [Phụ lục — Danh sách mã UC/BR/MSG đã trích dẫn](#9-phụ-lục--danh-sách-mã-ucbrmsg-đã-trích-dẫn)

---

## 1. Thông tin Module

| Field | Value |
|-------|-------|
| **Feature / Module ID** | `MF09` — Meal Serving & Distribution |
| **Bounded Context** | Meal Serving & Consumption |
| **Spec gốc** | `SFMS-MF09-IMP-001` (`TDS-MF09_Backend.md`) |
| **Priority** | 🟠 **P1** — mắt xích giữa của trục xương sống (`Menu → phân suất & thống kê theo món → sự cố → điều tra`) |
| **Số UC trong phạm vi** | **13** theo traceability §1.9. ⚠️ `00_Team_Work_Assignment_Plan.md` §3 ghi "12 UC" — bám §1.9 (xem **L9**). Trong 13 UC: **UC-079 không hiện thực** (L9/CF-M09-1), **UC-141 thuộc MF05** (L21/CF-M09-13), **UC-055 chỉ chịu phần BR-133** (L19/CF-M09-11), **UC-047 chỉ đóng góp `list()`** (L22/CF-M09-14) |
| **Data Classification** | **PII — Sensitive (trẻ em)**: họ tên HS, lớp, allergen **đã xác minh**, mức tiêu thụ từng bữa |
| **Compliance Scope** | **PDPD (NĐ 13/2023/NĐ-CP)** · **BR-051** cấm tuyệt đối sinh trắc học với học sinh |
| **Upstream Dependencies** | MF01 (JWT `userId/role/schoolId`), MF02 (`Classroom.homeroomTeacherId`), MF03 (`Student` + allergen **đã xác minh** BR-031, liên kết phụ huynh BR-201), MF04 (`Partner` — NCC suất ăn, ADR-M09-8), MF05 (`Menu`/`Dish`/kcal), MF07 (`InspectionSession` — cổng BR-094) |
| **Downstream Consumers** | MF10 (BR-133 tóm tắt hằng ngày), **MF11** (điều tra: HS → **món đã ăn** → lô → NCC → mẫu qua `serving_dishes.dish_id`), MF13 (thống kê theo món/lớp/ngày) |
| **Package Java** | `com.sfms.mf09.{domain, repository, dto, mapper, service, web}` |
| **Test module** | `src/test/java/com/sfms/mf09/**` |
| **Migration** | 4 bảng MF09 nằm trong Flyway `V1__init_schema.sql` (Nam giữ) + `V{n}__seed_mf09.sql` (TDS §11.3) |

**Đặc thù rủi ro của module — quyết định mức độ nghiêm ngặt của test:**

> MF09 có **hai lớp hậu quả** khác nhau, và test phải phân biệt được:
> 1. **Sức khỏe tức thời:** `POST /serve` là **thao tác cuối cùng trước khi thức ăn tới miệng trẻ**. Nếu cổng BR-094 (kiểm thực trượt) hoặc BR-065 (suất riêng cho HS dị ứng đã xác minh) hỏng, hệ quả là **một đứa trẻ ăn phải món gây dị ứng**. Đây là nhóm test `CRITICAL`, branch coverage **100%**.
> 2. **Mất bằng chứng điều tra:** `serving_dishes` là **nơi duy nhất** trả lời được *"em nào đã ăn món nghi nhiễm?"* (ADR-M09-1). Nếu bảng này bị UPDATE/DELETE, hoặc `SERVED` mà rỗng món, thì **MF11 mất trục điều tra vĩnh viễn** — không sửa lại được sau sự cố.
>
> Vì vậy coverage mục tiêu: **≥ 90% line** cho `com.sfms.mf09.service`; **100% branch** cho `DistributionService.confirmServed()`, `personalize()` (BR-065/BR-123), `InspectionGatePort` gate logic (`blocked` vs `noSession` — **hai nhánh này ngược nhau, lẫn là chặn oan cả trường hoặc mở cổng khi kiểm thực trượt**), và guard phạm vi ADR-M09-6.

### 1.1 AI Generation Context (CASE 2.0)

| Field | Value |
|-------|-------|
| **AI Assisted?** | `Yes` — soạn thảo có AI hỗ trợ, oracle do người xác nhận (Minh) |
| **Constraint Source** | `TDS-MF09_Backend.md` §17.1 (C1–C8), §10 (error code), §16 (auth matrix 7 role × 13 nhóm endpoint), §13 (11 kịch bản gốc); **`00_Common_Conventions.md` D1–D10** |
| **Constraints Injected** | C1 hợp đồng phân trang + `SEARCH_FIELDS` cố định · C2 `serving_dishes` **đóng băng, append-only** · C3 **không lưu field dẫn xuất**, không bảng `meal_history` · C4 BR-130 enforce **DB + service** · C5 danh tính **từ JWT**, sai phạm vi → **403 không phải 404** · C6 cổng BR-094 qua port sang MF07 · C7 **CẤM** sinh trắc học trẻ em · C8 tên field JSON `name`/`class` *(phần định dạng ngày đã bị **D4** ghi đè — xem L3)* |
| **Model** | Claude Opus (`00_Team_Work_Assignment_Plan.md` §2c-4 — TestSpec = Opus vì oracle phải khớp BR/MSG) |
| **Trust Level** | `T2 → T3` (pending Red Gate §5.4) |
| **Oracle policy** | Mọi `Expected Result` **phải** có dòng `Oracle Source` trỏ BR / bước UC / MSG nguyên văn / luật `semantic-audit.ts` / **D1–D9**. TC không có oracle → reject (**AP-AI-001**). Kịch bản nào TDS §13 nêu mà **không** có BR/MSG chống lưng thì ghi rõ **"thiếu oracle"** và báo lại, **không** tự bịa expected. |

---

## 2. Logic Issues Resolved

> **Bắt buộc điền trước khi viết test.** Test cases encode hành vi **đã sửa**, không phải hành vi trong spec gốc.
> **L1–L8** = `00_Common_Conventions.md` **ghi đè** ADR/constraint trong TDS-MF09 — loại quan trọng nhất vì TDS chưa được cập nhật lại.
> **L9–L22** = kế thừa CF-M09-1…14 của TDS §2.3 — **không tự chọn bên đúng**, chỉ ghi rõ hành vi nào được test.
> **L23–L26** = lệch **mới phát hiện** khi soạn TestSpec này (đối chiếu TDS với `mock-data.ts` và `error_message.md` v1.8).

### 2.1. Nhóm A — `00_Common_Conventions.md` ghi đè TDS (ưu tiên xử lý trước)

| # | Quy ước | TDS-MF09 hiện viết | Hành vi được TEST | TC liên quan |
|---|---|---|---|---|
| **L1** | **D1** — JSON `id` + path param = **mã nghiệp vụ** (`code`), PK là UUID; bảng có `code VARCHAR(32)` + `UNIQUE(school_id, code)` | §5.2 khai `@Id UUID id` **không có cột `code`** cho cả 4 bảng, nhưng §9.2 lại trả `"id": "DSTU-0706A-001"` — **tự mâu thuẫn**, đúng nhóm mà bảng D1 đã điểm mặt (`MF01, MF02, MF03, MF08, MF09, MF10`) | 4 bảng MF09 có `code VARCHAR(32) NOT NULL` + `UNIQUE(school_id, code)`; JSON trả `code` dưới tên `id` qua `@JsonProperty("id")`. **Mọi DB assertion dùng `WHERE code = ?`**. Path `POST /api/v1/student-distributions/DSTU-0706A-001/serve` giữ nguyên. Khóa ngoại JSON (`studentId`, `catererPartnerId`, `dishId`) là **code** của bảng đích (`HS-3A-001`, `CAT-001`, `com-trang`), FK trong DB vẫn UUID | `MF09-TC-INT-010`, và mọi `TC-INT-*` |
| **L2** | **D2** — envelope lỗi `{"error":{"code","message","details"}}`, `details` là **mảng, không bao giờ `null`** | §9.2/§10 đã dùng envelope (đúng), nhưng không nói `details` bắt buộc là mảng | Assert theo JSON path `$.error.code`, `$.error.message`, và `$.error.details` **là array** (rỗng thì `[]`) ở **mọi** error path | Toàn bộ TC error path |
| **L3** ⚠️ | **D4** — JSON ngày **luôn ISO-8601**; **bỏ** mọi `@JsonFormat("dd/MM/yyyy")` | **Xung đột trực diện:** ADR-M09-5 chốt *"JSON trả về khớp 100% mock: `date` dạng `dd/MM/yyyy`, `servedAt` dạng `HH:mm dd/MM/yyyy`"*; §17.1 **C8** nhắc lại; **AP-M09-08** còn xếp *"ngày ISO thay vì `dd/MM/yyyy`"* vào loại **phải Reject**. §9.2 ví dụ vẫn `"date": "06/07/2026"`, `"servedAt": "10:53 06/07/2026"` | **Assert ISO**: `"date": "2026-07-06"` (`LocalDate`), `"servedAt": "2026-07-06T03:53:00Z"` (`Instant`), `recordedAt`/`reportedAt` tương tự. Backend **parse khoan dung cả 2 dạng khi NHẬN** (mock đang gửi `dd/MM/yyyy`) nhưng **TRẢ VỀ ISO**. ⚠️ **AP-M09-08 của TDS phải được sửa lại** — như đang viết thì nó bắt reject đúng hành vi mà D4 yêu cầu. FE bù bằng `lib/format.ts` ở tầng render (việc của Minh, D4 §"FE thêm") | `MF09-TC-INT-011`, `MF09-TC-CON-004`, `MF09-TC-E2E-004` |
| **L4** | **D6** — enum **SCREAMING_SNAKE**; `ConsumptionStatus` đủ **4** giá trị kể cả `PENDING`; `AllergySeverity` = `MILD|MODERATE|SEVERE` | §5.2 khai đúng 4 `ConsumptionStatus` + CHECK constraint, **nhưng** §9.2 (bản trước 29/07) từng ghi *"must be one of FULLY, PARTIALLY, NOT_CONSUMED"* — **sót `PENDING`**, đúng trạng thái khởi tạo (D6 nêu đích danh MF09). §9.2 `allergy-alerts` trả `"severity": "SEVERE"` nhưng ghi chú ngay dưới lại viết *"`severity = "Severe"` → FE tô đỏ"* | Assert `consumption` nhận **đủ 4** giá trị (parameterized), giá trị thứ 5 → `SRV-001`/MSG-138; assert `"severity": "SEVERE"` (không `"Severe"`, không `"Nặng"`). ⚠️ Fixture `mock-data.ts` lưu `severity: "Nặng"` (`STUDENT_HEALTH_INFO`) — chuyển đổi **một lần** ở bước sinh Flyway seed (D4 §"Không sửa seed") | `MF09-TC-005`, `MF09-TC-INT-012` |
| **L5** | **D5** — client gửi `id` ở `POST` → dùng làm `code`, trùng → **409 MSG-139** | TDS **không nhắc D5** | MF09 **không có endpoint tạo mới nhận `id` từ client**: phiên phát suất do `DistributionPlanHandler` sinh từ event `MenuPublished`; `/serve`, `/revoke`, `/confirm`, `/error-report` đều thao tác trên bản ghi đã có. ⇒ **D5 không áp dụng**, nhưng contract test assert **không endpoint nào của MF09 khai `requestBody` có field `id`** (nếu về sau có, phải kèm 409/MSG-139) | `MF09-TC-CON-005` |
| **L6** | **D7** — `message` **tiếng Anh** nguyên văn `error_message.md`; FE tra `lib/messages.ts` để hiện tiếng Việt | §9.2 trả message **tiếng Việt** cho `SRV-094` (*"Không được phục vụ Bữa trưa 07/07/2026: Bước 2 — trong chế biến không đạt (BR-094)."*) và `SRV-022` (*"Chưa có thực đơn Bữa trưa 06/07/2026 cho Khối 3…"*) — chính 2 mã mà D7 điểm tên | Assert message = **MSG-129** `"Meal cannot be served: inspection step [Step Name] did not pass."` và **MSG-130** `"No approved or published menu found for [Meal] on [Date] for grade [Grade]. Meal distribution cannot be recorded."` (nguyên văn, placeholder được thay giá trị thật); chi tiết cụ thể (tên bước, mã phiên) nằm ở `$.error.details`, **không** nhét vào `message` | `MF09-TC-007`, `MF09-TC-002` |
| **L7** | **D9** — `created_by`/`updated_by` là **tên cột reserved** cho `common/domain/BaseEntity`; field nghiệp vụ "ai làm" đặt tên `<hành_động>_by_<loại>_id` | §5.2 MF09 dùng `served_by`, `recorded_by`, `reported_by` (UUID, nghiệp vụ) — **không trùng** tên reserved nên **không vỡ mapping**, nhưng lệch khuôn đặt tên D9 | Test assert: (a) 4 entity MF09 `extends BaseEntity` và **không tự khai lại** `createdBy`/`updatedBy`; (b) `created_at`/`created_by` được `AuditorAware` tự điền = email actor, service **không** set tay; (c) 3 cột nghiệp vụ giữ nguyên nhưng **khuyến nghị** đổi thành `served_by_user_id`/`recorded_by_user_id`/`reported_by_user_id` — ghi để Mạnh chốt, **không** khoá test vào tên cột | `MF09-TC-INT-014` |
| **L8** | **D8** *(⏳ chờ Nam approve — vẫn làm vì không xung đột D1–D7)* — annotation springdoc S1–S8 | §9.2 **không có** annotation nào; §10 **không có cột HTTP status** (chỉ có cột "HTTP" ở bảng — có, đã đủ) | Nhóm **Contract test** §4.4 đọc `/v3/api-docs` assert S1/S2/S3/S4/S5/S6/S7/S8. Nếu endpoint MF09 chưa annotate → test **FAIL** và ghi rõ *"thiếu annotation D8"*, **không** nới assert | `MF09-TC-CON-001…005` |

> **Ghi chú bắt buộc cho Mạnh & Nam:** L1, L3, L4 nghĩa là **TDS-MF09 §5.2/§9.2/§17.1 cần một bản cập nhật** theo checklist cuối `00_Common_Conventions.md` — đặc biệt **AP-M09-08** (L3) đang **mâu thuẫn thẳng** với D4 đã Approved. TestSpec này viết theo **D1–D9** (nguồn ưu tiên cao hơn). **Không** sửa TestSpec để khớp TDS cũ.

### 2.2. Nhóm B — mâu thuẫn nguồn kế thừa từ TDS §2.3 (CF-M09-1…14)

| # | Nguồn CF | Spec gốc (sai / thiếu / lệch) | Fix áp dụng trong test |
|---|---|---|---|
| **L9** | CF-M09-1 | §1.9 có **UC-079 "Scan Face Data"** (BR-051, BR-060, MSG-014) nhưng: không có UC spec trong UC file; **BR-051 ghi rõ cấm sinh trắc học với trẻ em**; mock không có luồng quét mặt | **KHÔNG** TC nào hiện thực/kiểm thử quét mặt học sinh. Ngược lại: `MF09-TC-SEC-009` assert **không tồn tại** endpoint/field/cột `face|biometric|scan-face` trong MF09, và `MF09-TC-CON-005` assert `/v3/api-docs` không có path nào như vậy. **MSG-014 không được endpoint MF09 nào phát ra** — assert luôn điều đó. Chờ **Huy** chuyển UC-079 sang §1.4 |
| **L10** | CF-M09-2 | BR-123/BR-130 nói *"quét ID học sinh"*; mock dùng **nút bấm**, không có entity `MealSession` | Test theo **ngữ nghĩa nghiệp vụ**: "phiên phục vụ" = cặp `(date, mealType)`; hợp đồng API không phụ thuộc cổng vào (nút bấm **hoặc** máy quét). `MF09-TC-001`/`TC-INT-001` khoá vào bất biến `UNIQUE(student_id, date, meal_type)`, **không** khoá vào thiết bị |
| **L11** | CF-M09-3 | **BR-135** (trễ >20 phút) có trong `business_rules.md` §10 và FE nhắc tới, nhưng **không** có trong §1.9 | **KHÔNG** TC nào test BR-135 (quy tắc: chỉ dùng mã có trong §1.9). `MF09-TC-INT-017` assert cột `planned_serve_time` **tồn tại nhưng nullable và chưa dùng** (chừa sẵn). Chờ Huy chốt bổ sung BR-135 vào §1.9 hay bỏ khỏi FE |
| **L12** | CF-M09-4 | **BR-094 thuộc miền MF07**, không có trong cột BR của §1.9 — nhưng mock, `semantic-audit.ts` §7 và ràng buộc **C3** đều bắt chặn | Test **vẫn chặn** và ghi rõ oracle là **BR-094 của MF07** (trích chéo, không nhận là BR của MF09). `MF09-TC-007` (unit, 2 nhánh `blocked`/`noSession`), `MF09-TC-INT-002` (integration), `MF09-TC-SEC-010` (gọi API trực tiếp bỏ qua UI), `MF09-TC-E2E-003` |
| **L13** | CF-M09-5 | §2b-2/§2b-3 của Team Plan gán `mealPrefix` cho MF09; thực tế nó lọc `FoodSample.meal` của **MF08**; `student_distributions` **không có** field `meal` | MF09 **không hiện thực** `mealPrefix`. `MF09-TC-INT-007` test lọc bữa bằng **`mealType=LUNCH`** (exact filter). Nhánh nghiêm ngặt (gửi `mealPrefix` → 400 vì không phải field hợp lệ) để **`@Disabled("Chờ Nam chốt CF-M09-5")`** — hành vi khoan dung được test là *"`mealPrefix` không được làm sai tập kết quả"* |
| **L14** | CF-M09-6 | Bảng `SEARCH_FIELDS` của Team Plan ghi `studentName`, `className` — mock dùng `name`, `class`; `class` còn là **từ khóa Java** | `SEARCH_FIELDS` ở tầng JPA = **`studentName`, `className`, `studentId`** (cột `student_name`, `class_name`, `student_id`); **JSON giữ `name`/`class`**. `MF09-TC-INT-006` assert search khớp trên 3 cột đó và **không** khớp trên cột ngoài danh sách (`servedAt`) |
| **L15** | CF-M09-7 | `parent/meal-history` đọc **mảng tĩnh `MEAL_HISTORY`** ngoài `db.ts`, seed lệch ngày với `student-distributions` | **KHÔNG** seed `MEAL_HISTORY` vào DB, **KHÔNG** tạo bảng `meal_history`. `MF09-TC-INT-005` assert lịch sử là **projection** dựng từ `student_distributions ⋈ serving_dishes ⋈ meal_consumption_records ⋈ Dish(kcal)`, và assert bảng `meal_history` **không tồn tại** trong `information_schema` |
| **L16** | CF-M09-8 | Mock lưu `allergyWarning`/`assignedMeal`/`specialMeal`/`isSpecial`/`dietTag` thành **field vật lý**, seed từng tự mâu thuẫn (có `allergyWarning` trong khi `isSpecial:"no"`, thực đơn chay không chạm dị ứng) | `MF09-TC-004` test đúng ca đã sửa 27/07/2026: thực đơn chay + HS có allergen ⇒ `allergyWarning = null`, `isSpecial = "no"`, `allergens` **vẫn hiện**. `MF09-TC-INT-017` assert **không có cột** `assigned_meal`/`special_meal`/`allergy_warning`/`is_special`/`diet_tag` |
| **L17** | CF-M09-9 | Mock dùng **generic `PUT /:resource/:id`** merge mọi field ⇒ không kiểm BR-130/BR-094/lớp chủ nhiệm; FE chặn bằng `toast.error` (client-side) và gọi **N × PUT** | Test **endpoint nghiệp vụ**: `POST /{code}/serve`, `POST /confirm` (batch, 1 transaction), `POST /{code}/revoke`, `POST /{code}/error-report`. `MF09-TC-SEC-002`/`003`/`010` là nhóm **"gọi API trực tiếp bỏ qua UI"** — bắt buộc theo prompt §2c-3b vì gate chỉ có ở client là lách được toàn bộ luật ATTP |
| **L18** | CF-M09-10 | Resource `distributions` (tổng hợp ngày × lớp) đã bỏ 21/07/2026; UC-132 vẫn cần số liệu đó | `MF09-TC-INT-004` assert `GET /student-distributions/summary` **tính runtime** (`GROUP BY date, class_name`) và **phản ánh ngay** việc GV vừa phát; `MF09-TC-011` khoá công thức `status` theo đúng `distributionSummaries()` |
| **L19** | CF-M09-11 | **UC-055**: §1.9 chỉ ghi **BR-133**; UC file ghi BR-069/070/075 (miền MF05) | MF09 chỉ test phần **BR-133** (tóm tắt bữa **đã phục vụ**) — `MF09-TC-INT-015`. Phần thực đơn công bố + allergen **không** test ở đây (thuộc MF05) |
| **L20** | CF-M09-12 | Nhiều BR có ở UC file nhưng thiếu ở §1.9 (UC-016 thêm BR-065/123/125/130; UC-047 thêm **BR-100**; UC-143 thêm **BR-025**; UC-053 thêm BR-031) | Cột BR giữ nguyên **§1.9**. BR-065/123/125/130 vẫn được test vì **có mặt ở §1.9 dưới UC khác**. **BR-100 và BR-025 không có TC nào** (ngoài phạm vi MF09). **MSG-051** (AF1 UC-103) **không** có ở §1.9 ⇒ `MF09-TC-INT-018`… assert response chỉ trả cờ `conflictWithTodayMenu`, **không** mang mã MSG-051 |
| **L21** | CF-M09-13 | **UC-141** gán nhầm cho MF09 ở bản v1.0 — màn `parent/special-meals` dùng `derivedMealPlans()` từ `menus`+`menu-templates` (MF05) | **KHÔNG** TC nào cho UC-141, **không** endpoint `/students/{id}/special-meal-arrangements` trong MF09. `MF09-TC-CON-005` assert spec MF09 **không** khai path đó |
| **L22** | CF-M09-14 | Bản v1.0 tự phát minh `GET /meal-service/overview` + `/meal-service/portions` (UC-047) — trùng `cookSheet()` của MF05 và gate của MF07 | **KHÔNG** TC nào cho 2 endpoint đó; `MF09-TC-CON-005` assert chúng **không tồn tại** trong `/v3/api-docs` của MF09. UC-047 chỉ được cover qua `GET /student-distributions` + `/summary` |

### 2.3. Nhóm C — lệch mới phát hiện khi soạn TestSpec (đối chiếu TDS ↔ seed ↔ `error_message.md` v1.8)

| # | Phát hiện | Bằng chứng | Fix áp dụng trong test |
|---|---|---|---|
| **L23** | **Mã phiên kiểm thực trong ví dụ §9.2 không tồn tại.** TDS §9.2 (403 SRV-094) ghi `"inspectionSessionId": "INS-2026-0707-L"` | `mock-data.ts` `INSPECTIONS` dùng khuôn `INSP-<yyyymmdd>-<L|S>`; phiên trượt thật của 07/07/2026 là **`INSP-20260707-L`** (`step2=FAILED`, `coreTemp=68`, `overall=FAILED`). Chuỗi `INS-2026-0707-L` **không có ở bất kỳ đâu** | Fixture `FX-012` dùng **`INSP-20260707-L`**. Nếu để nguyên mã sai trong TDS, test đối chiếu `$.error.details[0].value` sẽ so với một mã **không tra ra phiên nào** ⇒ điều tra sự cố truy ngược sai phiên kiểm thực. Đề nghị Mạnh sửa ví dụ §9.2 |
| **L24** | **Ghi chú §10 đã lỗi thời.** TDS §10 ghi *"`SRV-094`, `SRV-022`, `SRV-023`, `SRV-024`, `SRV-025` là mã cục bộ TDS — **chưa** có MSG chính thức, chờ Huy quyết định"* | `error_message.md` **v1.5+ đã cấp**: MSG-129→`SRV-094`, MSG-130→`SRV-022`, MSG-131→`SRV-023`, MSG-132→`SRV-024`, MSG-133→`SRV-025` (bảng §1, dòng 102–106) | **Không còn "thiếu oracle"** cho 5 mã này. Mọi TC dùng **message tiếng Anh nguyên văn** của MSG tương ứng (D7). Đề nghị Mạnh xoá ghi chú lỗi thời ở §10 |
| **L25** | **Thiếu mã cho HTTP 429.** §9.1 khai rate limit cho **mọi** endpoint (300/min, 120/min, 30/min…) nhưng §10 **không có dòng nào cho 429** | `error_message.md` v1.8 đã cấp **MSG-141** `"Too many requests. Please try again later."` dùng chung mọi module (MF02 gán `SCH-014`) | `MF09-TC-SEC-013` assert **HTTP 429 + message MSG-141**; **không** assert `code` vì MF09 chưa được cấp mã cục bộ — ghi rõ *"thiếu mã cục bộ, đề nghị `SRV-429`"*, chờ Huy/Nam. Đây là cách xử lý **khoan dung** đúng policy (không tự đặt mã mới) |
| **L26** | **TDS bỏ sót 1 luật `semantic-audit.ts`.** §4.2/§13 nhắc `PHAT_SAI_NGAY`, `TRANG_THAI_VS_GIO_PHAT`, `PHAT_ROI_KHONG_CO_MON`, `HOC_SINH_SAI_LOP`, `PHAT_SUAT_DU_KIEM_THUC_TRUOT` — **thiếu `TEN_KHONG_KHOP_HOC_SINH`** (script §6 dòng 161–162) | `semantic-audit.ts` §6: `if (st.name !== d.name) add("TEN_KHONG_KHOP_HOC_SINH", …)` | `MF09-TC-INT-016` port **đủ 6 luật**. Luật này bảo vệ đúng thứ ADR-M09-1 cần: `student_name` là **snapshot lúc tạo phiên**, nếu lệch tên thật của HS thì hồ sơ phát suất chỉ sai một chỗ là **quy trách nhiệm sai đứa trẻ** khi điều tra |

### 2.4. Nhóm D — phát hiện ở **Bước 0 + Bước 1** (2026-08-03), đã chốt thành `M09-D01`…`M09-D10`

> Bước 0 rà **quyền sở hữu resource** trên code đã merge (V1–V15); Bước 1 rà **quy ước**. Bốn mục dưới đây **không** nằm trong L1–L26 vì chúng chỉ lộ ra khi đối chiếu với **code thật**, không phải khi đọc tài liệu.

| # | Phát hiện | Bằng chứng (đo trên code, không suy từ tài liệu) | Fix áp dụng trong test |
|---|---|---|---|
| **L27** | **TDS-MF09 §9.1 tuyên bố sở hữu `GET /api/v1/special-meals` — MF03 đã sở hữu path này và đã merge.** | `grep -rn '"/api/v1/special-meals'` → `mf03/web/SpecialMealRequestController:36`. Hai `@RequestMapping` cùng path ⇒ `IllegalStateException: Ambiguous mapping`, **app không khởi động** | `M09-D06` gỡ khỏi §9.1 (15 → **14** endpoint). `MF09-TC-CON-005` bước (5) assert **đúng 1** controller khai path đó, và nó thuộc `com.sfms.mf03`. Test này là lưới an toàn nếu ai vô tình thêm lại |
| **L28** | **Bảng D của `02_Module_Completion_Workflow.md` §2.1 ghi MF09 sạch cả 3 cột — SAI 2/3.** | (a) Package: **18 chỗ** dùng tên cũ, không phải `com.sfms.mf09` — grep của bảng D khớp khuôn `inventory\|inspection\|feedback\|incident\|platform`, không bắt được tên MF09. (b) Cột audit: `StudentDistribution` + `ServingDish` **tự khai** `@Column(name="created_at")` ⇒ **duplicate column với `BaseEntity`, build đỏ ngay** | `M09-D02`/`M09-D03`. `MF09-TC-INT-014` (đã có) nay assert thêm: DDL có **đủ 4** cột audit, entity **không** khai lại field nào trong số đó. Đây là **lần thứ ba** bảng D sai (sau MF07, MF08) ⇒ **đừng tin bảng D** |
| **L29** | **TDS-MF09 khai 6 port; 5/6 là viết lại hạ tầng đã có.** | `ReferenceResolver` có **36 method**; dòng 500–504 là comment do **MF08** để lại: *"MF07 đã merge nhưng KHÔNG expose port nào… KHÔNG dựng `InspectionGatePort` riêng (= AP-AI-016)"* — **đúng tên port** MF09 định dựng | `M09-D08`. TC nào mock `InspectionGatePort`/`ClassroomPort`/`StudentProfilePort`/`ParentStudentLinkPort`/`CatererAssignmentPort` **chuyển sang mock `ReferenceResolver`**. `MF09-TC-007` (gate BR-094) đổi 2 nhánh thành `inspectionGate()` trả `blocked=true` / `Optional.empty()` |
| **L30** | **§11.2 ghi *"Flyway V1 do Nam giữ — MF09 thêm 4 bảng vào V1"* — mô hình này đã bị bỏ từ MF02.** | `ls db/migration/` → V1…V15 đã dùng. Sửa V1 đã merge = **Flyway checksum mismatch trên máy cả nhóm**. `00_Team_Work_Assignment_Plan.md` changelog 02/08 mục 3 đã sửa ở 14 file, **sót chỗ này** | `M09-D05` — `V16__mf09_meal_serving.sql` + `V17__seed_mf09.sql`. Mọi TC dùng Testcontainers chạy **toàn bộ** V1→V17, không mock schema |

> **D13 — đã kiểm, KHÔNG phải seed thêm.** `V900__test_fixtures.sql` có tài khoản ACTIVE cho **cả 5 role** mà `@PreAuthorize` của MF09 dùng (`teacher`, `parent`, `medical-staff`, `school-admin`, `service-caterers`). Mật khẩu chung **`P@ssw0rd123`** (V900 dòng 11 — **không** phải `Passw0rd123`; sai 4 lần khoá tài khoản, BR-002).
> ⚠️ **Nhưng `@WithMockUser` KHÔNG dùng được** cho `MF09-TC-CON-006`, `TC-SEC-002/003`, và mọi TC phụ thuộc BR-120/BR-201: nó không mang claim `schoolId`, và **không** tạo được quan hệ `classrooms.homeroom_teacher_id` / `student_parents`. Hai điều kiện dữ liệu phải verify trong fixture: (a) tài khoản `teacher` test **thực sự là GVCN** lớp 3A; (b) tài khoản `parent` test **thực sự đã liên kết** HS-3A-001.

**Cách xử lý mâu thuẫn trong test (policy):** nơi nào 2 nguồn nói khác nhau và **chưa có người chốt** → test **hành vi khoan dung** (chấp nhận cả hai) + để `@Disabled("Chờ CF-M09-x")` cho TC của hành vi nghiêm ngặt. **Không** viết test khoá một bên rồi coi là chuẩn.
**Ngoại lệ:** **L1–L8** đã có người chốt (`00_Common_Conventions.md`, Nam approve) ⇒ test **khoá theo D1–D9**, không khoan dung. **L27–L30** cũng khoá, vì oracle là **code đã merge** — nguồn ưu tiên `-1`, cao hơn cả tài liệu (`00_Team_Work_Assignment_Plan.md` changelog 02/08 mục 1).

---

## 3. Test Design Specification

### TDS-01 — Scope / Phạm vi

```
MF09 (com.sfms.mf09) — các layer được kiểm thử:
├── domain/       StudentDistribution (aggregate root)
│                 + ServingDish <<Immutable Event>>  (append-only, ADR-M09-1)
│                 + MealConsumptionRecord · DistributionErrorReport
│                 + enum MealType{BREAKFAST,LUNCH,SNACK} (D6 — common/enums)
│                   DistributionStatus{PENDING,SERVED} · ConsumptionStatus{PENDING,FULLY,
│                   PARTIALLY,NOT_CONSUMED} (D6 — 4 giá trị)
│                 → pure logic: personalize() suất riêng, dẫn xuất allergyWarning/isSpecial
├── service/      DistributionService (confirmServed · confirmBatch · revokeServed ·
│                   reportError · summary) · MealConsumptionService · MealHistoryService
│                   (projection) · SpecialMealViewService · AllergyAlertService
│                   · DailyParentSummaryJob (BR-133)
│                 → UNIT TEST: Mockito mock repository + 6 port (§8.3)
├── web/          DistributionController · MealRecordController · AllergyAlertController
│                 · MealHistoryController
│                 → SECURITY TEST: MockMvc + JWT giả lập 7 role + guard lớp/con
├── integration/  @SpringBootTest + Testcontainers PostgreSQL 16 + Flyway V1 + seed MF09
│                 → API → DB, UNIQUE/CHECK/RULE, projection, event, audit
└── contract/     GET /v3/api-docs → D8 (S1–S8) + D1/D2/D4/D6
```

**NGOÀI phạm vi (test ở module khác, chỉ **stub/seed** ở đây):**

| Thành phần | Chủ | Cách xử lý trong test MF09 |
|---|---|---|
| JWT/RBAC filter, `AuthUser` | MF01 (Nam) | JWT giả lập; **không** test thuật toán token |
| `Classroom.homeroomTeacherId`, sĩ số, khối | MF02 (Nam) | seed `3A`(`U-0004`)/`3B`(`U-0009`); `ClassroomPort` = Mockito stub ở unit, seed thật ở integration |
| `Student`, allergen **đã xác minh** (BR-031), liên kết phụ huynh (BR-201) | MF03 (Huy) | `StudentProfilePort` / `ParentStudentLinkPort` stub; seed `STUDENTS` + `STUDENT_HEALTH_INFO` |
| `Menu`/`Dish`/`kcal`/suất riêng theo chế độ ăn | MF05 (Thảo) | `MenuQueryPort` stub trả `MenuBlockView`; seed `MENU-2026-27`, `MENU-TD-3`, `MENU-2026-27B` |
| `InspectionSession` + cổng ba bước (BR-094) | MF07 (Thảo) | `InspectionGatePort` stub (**2 nhánh `blocked` / `noSession` test riêng**); seed `INSP-20260706-L`, `INSP-20260707-L` |
| `Partner` NCC suất ăn (ADR-M09-8) | MF04 (Mạnh) | `CatererAssignmentPort` stub trả `CAT-001`; **assert port chỉ được gọi lúc sinh phiên**, không gọi khi đọc lịch sử |
| Gửi thông báo phụ huynh (BR-133) | **MF10 (Mạnh)** | MF09 chỉ assert **phát event** `DailyMealSummaryCompiled`; SMTP là stub ở dev (ADR-000) — **không** test nội dung email |
| Cửa sổ phản hồi 24h (`feedbackOpen`, BR-139) | MF10 (Mạnh) | MF09 **chỉ chuyển tiếp cờ** — assert field có mặt, **không** assert công thức 24h |
| Điều tra sự cố theo `dishId` | MF11 (Minh) | MF09 assert **query truy xuất trả đúng tập HS** (`findServedByDishId`); logic điều tra test ở MF11 |
| Sinh trắc học (UC-079) | **MF04 (Mạnh)** | **KHÔNG test ở đây** — ngược lại, assert MF09 **không có** (BR-051, L9) |

### TDS-02 — Test Basis / Cơ sở Kiểm thử

| Source | Items Derived |
|--------|--------------|
| `traceability_matrix.md` **§1.9** | 13 UC + tập BR/MSG hợp lệ (nguồn CHUẨN — không nhận mã ngoài bảng này) |
| `MF09_Meal_Serving_Distribution.md` (UC file) | Normal Flow → bước nghiệp vụ của TC; **UC-016 AF1** (báo lỗi phân phát → chặn hoàn tất) → `TC-009`; **UC-103 AF1** (xung đột thực đơn) → cờ `conflictWithTodayMenu`; **UC-132 AF2** (highlight lớp chưa xác nhận) → `TC-011` |
| `business_rules.md` §10 | **BR-120** GVCN xác nhận phát suất lớp mình · **BR-121** ghi nhận lịch sử tiêu thụ · **BR-123** cảnh báo dị ứng khi phục vụ · **BR-125** xem yêu cầu bữa ăn đặc biệt trước/trong giờ phục vụ · **BR-130** chặn phát trùng · **BR-133** tóm tắt hằng ngày · *(BR-135 — không ở §1.9, không test, L11)* |
| `business_rules.md` §7 | **BR-065** bắt buộc kế hoạch bữa ăn riêng cho HS dị ứng **đã xác minh**/chế độ ăn đặc biệt · BR-066 danh mục chế độ ăn |
| `business_rules.md` §4 | **BR-201** ≤ 5 hồ sơ HS/tài khoản phụ huynh · **BR-031** thông tin dị ứng phải được Medical Staff **xác nhận** mới có hiệu lực |
| `business_rules.md` §5 | **BR-051** — *"Không áp dụng xác thực sinh trắc học với học sinh (trẻ em) vì PDPD"* → **lệnh cấm**, không phải tính năng |
| `business_rules.md` §8 *(trích chéo MF07)* | **BR-094** bữa ăn **không được phục vụ** nếu bất kỳ bước kiểm thực nào thất bại |
| `error_message.md` **v1.8** | Chuỗi assert nguyên văn: MSG-004, MSG-005, MSG-014*, MSG-021, MSG-045, MSG-129, MSG-130, MSG-131, MSG-132, MSG-133, MSG-137, MSG-138, MSG-141 |
| `TDS-MF09_Backend.md` §10 | Error code: `SRV-001/004/005/014*/021/022/023/024/025/094/404/500` |
| `TDS-MF09_Backend.md` §16 | Ma trận 7 role × 13 nhóm endpoint → nhóm security test |
| `TDS-MF09_Backend.md` §17.1 | C1–C8 → **mỗi constraint ≥ 1 TC** (chống AP-AI-001) |
| **`00_Common_Conventions.md` D1–D9** | Định danh, envelope, path, ISO ngày, id client, enum chung, ngôn ngữ message, cột reserved → L1–L8 + `TC-INT-010…014` + `TC-CON-001…005` |
| `semantic-audit.ts` §6, §7 | **6 luật** (tên nguyên văn): `PHAT_SAI_NGAY` · `TRANG_THAI_VS_GIO_PHAT` · `PHAT_ROI_KHONG_CO_MON` · `HOC_SINH_SAI_LOP` · `TEN_KHONG_KHOP_HOC_SINH` · `PHAT_SUAT_DU_KIEM_THUC_TRUOT` |
| `data_model.puml` ràng buộc **C3** | Phát suất trỏ thực đơn có thật của (ngày, khối, bữa) **và** bữa có phiên kiểm thực ĐẠT → `TC-002`, `TC-007` |
| `00_Team_Work_Assignment_Plan.md` §2b | Shape 5 field · clamp `page` · tập rỗng vẫn 200 · `search` chỉ trên `SEARCH_FIELDS` · `dateFrom`/`dateTo` trên cột `date` (§2b-5) · **không** `mealPrefix`/`step`/`stepStatus` |
| **PDPD (NĐ 13/2023/NĐ-CP)** | Cách ly phạm vi đọc PII trẻ em; sai phạm vi → **403 không phải 404**; log không lộ PII thô |

*(*) MSG-014 / `SRV-014` liệt kê để giữ vết §1.9 — assert **không endpoint MF09 nào phát ra** (L9).*

### TDS-03 — Test Conditions and Coverage Items

| Condition ID | Test Condition | Coverage Item | Constraint | Test Cases |
|---|---|---|---|---|
| `COND-01` | 1 HS ≤ 1 bản ghi phát suất / `(ngày, bữa)`; xác nhận lần 2 → **409**, **không** no-op | `DistributionService.confirmServed()` + `uq_dist_student_date_meal` | C4 / **BR-130** | `TC-001`, `TC-INT-001`, `TC-INT-008` |
| `COND-02` | Hai request song song cùng HS → đúng 1 thành công | `findByIdForUpdate()` (`PESSIMISTIC_WRITE`) | C4, ADR-M09-3 | `TC-INT-001` |
| `COND-03` | `status=SERVED` ⇒ `serving_dishes` ≥ 1; danh sách rỗng → **422**, **không** ghi SERVED | `confirmServed()` + `MenuQueryPort` | C2, ADR-M09-1 | `TC-002`, `TC-INT-008` |
| `COND-04` | Suất riêng phải nêu **món bị thay + lý do**; chỉ dựa trên allergen **đã xác minh** | `personalize()` + `ck_sdish_substitute` | **BR-065**, BR-031 | `TC-003`, `TC-INT-008` |
| `COND-05` | Cảnh báo dị ứng là **dẫn xuất**; thực đơn không chạm allergen ⇒ `allergyWarning=null`, `isSpecial="no"` | `getDetail()` projection | C3, **BR-123** | `TC-004`, `TC-INT-017` |
| `COND-06` | `consumption` nhận **đủ 4** giá trị (kể cả `PENDING`); giá trị thứ 5 → 400 | `MealConsumptionService.record()` + CHECK | **D6**, **BR-121** | `TC-005`, `TC-INT-012` |
| `COND-07` | Chỉ **GVCN của lớp** được `serve/revoke/record`; sai → **403 SRV-005**, không 404 | guard `ClassroomPort.isHomeroomTeacher()` | C5, **BR-120** | `TC-006`, `TC-SEC-002`, `TC-SEC-003` |
| `COND-08` | Cổng kiểm thực: `blocked=true` → **403**; `noSession=true` → **KHÔNG chặn**, chỉ cảnh báo | `InspectionGatePort.gateFor()` | C6, **BR-094** (MF07) | `TC-007`, `TC-INT-002`, `TC-SEC-010` |
| `COND-09` | `revoke` chỉ trong ngày; ngày trước → **409**; `serving_dishes` **giữ nguyên** | `revokeServed()` | ADR-M09-3, C2 | `TC-008`, `TC-INT-009` |
| `COND-10` | Còn `DistributionErrorReport.resolved=false` ⇒ **chặn hoàn tất lớp** (409) | `reportError()` + `confirmBatch()` | **UC-016 AF1**, BR-065 | `TC-009` |
| `COND-11` | Sửa mức tiêu thụ **sau ngày phục vụ** → 409 (đã vào tóm tắt BR-133) | `MealConsumptionService.record()` | BR-121, BR-133 | `TC-010` |
| `COND-12` | Tổng hợp ngày × lớp **tính runtime**: `0→PENDING`, `0<d<p→IN_PROGRESS`, `d=p→COMPLETED` | `summary()` + `summarize()` JPQL | C3, CF-M09-10 | `TC-011`, `TC-INT-004` |
| `COND-13` | `servedAt` cùng ngày với `date`; `SERVED` ⇒ `served_at`+`served_by` NOT NULL | `ck_dist_served_same_day`, `ck_dist_served_at` | audit §6 | `TC-012`, `TC-INT-008`, `TC-INT-016` |
| `COND-14` | Parent chỉ đọc **con đã liên kết** (≤5); ngoài phạm vi → **403** | `ParentStudentLinkPort.isLinked()` | C5, **BR-201** | `TC-013`, `TC-SEC-005` |
| `COND-15` | `caterer` là **snapshot lúc sinh phiên**; port MF04 **không** được gọi khi đọc lịch sử | `DistributionPlanHandler` + `MealHistoryService` | **ADR-M09-8** | `TC-014`, `TC-INT-005` |
| `COND-16` | `confirm` batch: `atomic=true` → rollback toàn bộ; `atomic=false` → kết quả từng dòng | `confirmBatch()` | §9.2 | `TC-015`, `TC-INT-013` |
| `COND-17` | `kcal` tính runtime từ `Dish` (MF05) theo `dish_id` **đã đóng băng** | `MealHistoryService` projection | C3 | `TC-016`, `TC-INT-005` |
| `COND-18` | `serving_dishes` **append-only** — UPDATE/DELETE bị chặn ở **tầng DB** | `RULE serving_dishes_no_update/no_delete` | C2, ADR-M09-1 | `TC-INT-009`, `TC-SEC-011` |
| `COND-19` | Lịch sử **không bị viết lại** khi hồ sơ dị ứng HS thay đổi sau đó | `AllergyProfileHandler` | ADR-M09-1 | `TC-INT-003` |
| `COND-20` | Lịch sử bữa ăn là **projection**; **không** tồn tại bảng `meal_history` | `information_schema` | C3, CF-M09-7 | `TC-INT-005` |
| `COND-21` | **Không** tồn tại cột dẫn xuất (`assigned_meal`/`special_meal`/`allergy_warning`/`is_special`/`diet_tag`) và không bảng tổng hợp | `information_schema` | C3 | `TC-INT-017` |
| `COND-22` | Hợp đồng phân trang: đủ 5 field, `page` clamp, tập rỗng vẫn **200**, `search` chỉ trên `SEARCH_FIELDS` | `PagedResponse.of()` + `StudentDistributionSpec` | C1, §2b | `TC-INT-006` |
| `COND-23` | Lọc exact `date/class/status/mealType/isSpecial/studentId` + `not:` + `dateFrom/dateTo`; **không** `mealPrefix` | `StudentDistributionSpec.build()` | C1, CF-M09-5 | `TC-INT-007` |
| `COND-24` | **D1** — JSON `id` = `code`, PK UUID, không lộ UUID | entity + `@JsonProperty("id")` | **D1** | `TC-INT-010`, `TC-CON-001` |
| `COND-25` | **D4** — mọi field ngày/giờ JSON là **ISO-8601** | mapper MF09 | **D4**, L3 | `TC-INT-011`, `TC-CON-004` |
| `COND-26` | **D6** — enum SCREAMING_SNAKE, `ConsumptionStatus` đủ 4, `severity` = `SEVERE` | `common/enums/` | **D6**, L4 | `TC-INT-012`, `TC-CON-003` |
| `COND-27` | **D2 + D7** — envelope lỗi, `details` là mảng, `message` tiếng Anh nguyên văn | `@RestControllerAdvice` | **D2/D7** | mọi TC error path, `TC-CON-002` |
| `COND-28` | **D9** — entity `extends BaseEntity`, không tự khai `created_by`/`updated_by` | `common/domain/BaseEntity` | **D9**, L7 | `TC-INT-014` |
| `COND-29` | BR-133: job tóm tắt **idempotent** theo `(student_id, date)`; chạy lại không gửi trùng | `DailyParentSummaryJob` | ADR-M09-7 | `TC-INT-015` |
| `COND-30` | 6 luật `semantic-audit.ts` chạy trên DB sau E2E → **0 vi phạm** | truy vấn đối soát §14.1 | audit §6/§7 | `TC-INT-016` |
| `COND-31` | Trục truy xuất MF11: *"HS nào đã ăn món X trong khoảng ngày?"* trả đúng tập | `findServedByDishId()` | ADR-M09-1 | `TC-INT-018`, `TC-E2E-003` |
| `COND-32` | Không JWT → **401**; sai role → **403** trên toàn bộ endpoint MF09 | filter chain + `@PreAuthorize` | §16 | `TC-SEC-001`, `TC-SEC-004`, `TC-SEC-008` |
| `COND-33` | Ma trận 7 role × 13 nhóm endpoint khớp §16 **100%** | controller authz | §16 | `TC-SEC-007` |
| `COND-34` | `schoolId`/`classId`/`studentId` lấy **từ JWT**, bỏ qua giá trị client gửi | `SecurityContext` → Spec | C5, AP-AI-006 | `TC-SEC-006` |
| `COND-35` | **BR-051** — không endpoint/field/cột sinh trắc học nào gắn với học sinh | grep + `/v3/api-docs` | **C7** | `TC-SEC-009`, `TC-CON-005` |
| `COND-36` | Gate nghiệp vụ enforce **phía server** — gọi thẳng API bỏ qua UI vẫn bị chặn | controller → service | L17, prompt §2c-3b | `TC-SEC-002`, `TC-SEC-003`, `TC-SEC-010` |
| `COND-37` | Audit đủ actor + IP cho `serve`/`revoke`/`record`/`error-report`; log **không** lộ PII trẻ em | audit aspect + logger | §4.3 TDS, PDPD | `TC-SEC-012` |
| `COND-38` | Vượt rate limit → **429** + MSG-141 | rate limiter | §9.1, L25 | `TC-SEC-013` |
| `COND-39` | **D8** — spec OpenAPI thoả S1–S8 và không tái tạo lệch D1/D2/D4/D6 | `/v3/api-docs` | **D8** | `TC-CON-001…005` |
| `COND-40` | Danh sách endpoint MF09 trong spec **đúng 15 dòng** §9.1 — không có `/meal-service/*`, không `/special-meal-arrangements` | `/v3/api-docs` | CF-M09-13/14 | `TC-CON-005` |

**Tổng:** 40 condition → **56 test case**. Mỗi condition có ≥ 1 TC ⇒ **không có condition mồ côi** (kiểm ở §6).

### TDS-04 — Test Techniques / Kỹ thuật Kiểm thử

| Technique (ISO 29119-4) | Applied To | Rationale |
|---|---|---|
| **State Transition Testing** | FSM `StudentDistribution`: `PENDING → SERVED` (một chiều trong request) · `SERVED → PENDING` **chỉ** qua `revoke` trong ngày · transition **không hợp lệ**: `SERVED → SERVED` (409), `revoke` ngày trước (409). FSM `MealConsumptionRecord`: `PENDING → FULLY/PARTIALLY/NOT_CONSUMED`, sửa trong ngày, **khoá sau ngày phục vụ** | BR-130 và ADR-M09-3 là ràng buộc **trạng thái**; chỉ bảng chuyển trạng thái mới chứng minh được "gọi lần 2 không phải no-op im lặng" |
| **Decision Table Testing** | Cổng phát suất: (`gate.blocked` × `gate.noSession` × `danh sách món rỗng?` × `đúng GVCN?`) → `200 / 403 SRV-094 / 403 SRV-005 / 422 SRV-022`. Bảng phân quyền 7 role × 13 endpoint (§16) | Cổng có **4 biến đầu vào** và là thao tác cuối trước khi trẻ ăn. `blocked` và `noSession` **ngược chiều nhau** (chặn vs không chặn) — lẫn 2 ô này là hoặc chặn oan cả trường, hoặc mở cổng khi kiểm thực trượt |
| **Equivalence Partitioning** | Vai theo ADR-M09-6: {Teacher} = *own class* · {Parent} = *own child (≤5)* · {Caterers, Medical, School Admin} = *school, read-only* · {Supplier} = **không truy cập**. Trạng thái hồ sơ dị ứng: `VERIFIED` (có hiệu lực, BR-031) vs {`PENDING`, `NEEDS_INFO`} (**không** sinh suất riêng) | BR-031 chia hồ sơ dị ứng thành 2 miền hành vi hoàn toàn khác nhau — seed thật đã có sẵn cả hai (`HS-3A-001` VERIFIED vs `HS-3A-005` NEEDS_INFO) |
| **Boundary Value Analysis** | `page` ∈ {1, `pageCount`, `pageCount+1`} · `pageSize` ∈ {0, 1, 8} · `distributed` ∈ {0, 1, `planned-1`, `planned`} cho công thức `status` · `revoke` tại `T = ngày phát` vs `ngày phát + 1` · số con của Parent ∈ {5, 6} (BR-201) | Công thức `status` tổng hợp có **3 nhánh liền kề**; sai biên là bếp thấy "COMPLETED" khi còn 1 em chưa nhận suất |
| **Error Guessing** | Bỏ JWT · Caterers gọi `/serve` (nút bị ẩn ở UI) · GV lớp khác gọi thẳng API · Parent đổi `studentId` trên URL · gửi `schoolId` giả trong query · `UPDATE serving_dishes` bằng SQL trực tiếp · gọi `/serve` khi kiểm thực FAILED | **Đều là lỗ hổng đang có thật** trong mock (CF-M09-9: guard nằm ở component, chặn bằng `toast.error`) — không phải giả thuyết |
| **Data-driven (Parameterized)** | 7 role × 13 nhóm endpoint (§16) · 4 giá trị `ConsumptionStatus` + 1 giá trị sai · 6 luật `semantic-audit.ts` · 15 endpoint × "không JWT" | 91 tổ hợp cho ma trận role — viết tay là bất khả thi và dễ sót đúng ô nguy hiểm |
| **Concurrency Testing** | 2 request `/serve` song song cùng `distributionId`; 2 request cùng `(studentId, date, mealType)` khác `distributionId` | Giờ ăn là **cao điểm 10:30–11:15, ~30 lớp cùng lúc** (§4.1 TDS) — race ở đây tạo suất trùng, và bản ghi trùng làm hỏng thống kê điều tra |
| **Contract Testing** | Shape 5 field phân trang · tên field JSON `name`/`class`/`mealType`/`allergens`/`isSpecial` · `/v3/api-docs` × S1–S8 | Đổi tên field là 20+ file FE gãy; §14.3 TDS đã đặt sẵn lệnh kiểm bằng `jq` |
| **Constraint-based (DB)** | `uq_dist_student_date_meal` · `ck_dist_served_at` · `ck_dist_served_same_day` · `ck_sdish_substitute` · `ck_mcr_recorded` · `RULE` append-only | TDS §5.2 nói rõ *"Service vẫn kiểm trước để trả đúng mã MSG, nhưng **DB là chốt chặn cuối**"* — hai lớp phải test riêng vì service có thể bị bypass bởi script/migration |

### TDS-05 — Test Data Requirements

> **Nguồn fixture: `mock-data.ts` — dùng ID THẬT của seed** (Team Plan §2c-3b nguồn 6). Trường QA = `SCH-001`, NCC suất ăn = `CAT-001`.
> Fixture dựng bằng **Java builder** trong `MealServingTestFixtures.java`; seed DB bằng `@Sql` hoặc Flyway test migration.
> ⚠️ Theo **D1**, mọi giá trị `DSTU-…`/`MR-…`/`HS-…`/`CAT-…`/`INSP-…` dưới đây là **`code`**, không phải PK.
> ⚠️ Theo **D4**, cột ngày trong fixture là `LocalDate`/`Instant`; chuỗi `dd/MM/yyyy` của mock được chuyển đổi **một lần** khi sinh seed SQL.

| Fixture ID | Type | Value / Logic | Mục đích |
|---|---|---|---|
| `FX-001` | DB seed `student_distributions` | **10 dòng hôm nay lớp 3A**: `DSTU-TD-3A-001…010`, `mealType=LUNCH`, `menuId=MENU-TD-3` (thực đơn **chay**), tất cả `status=PENDING`, `isSpecial` dẫn xuất = `"no"` | Nền của happy path phát suất cả lớp (`TC-E2E-001`) và của `TC-004` (chay ⇒ không cảnh báo dị ứng) |
| `FX-002` | DB seed `student_distributions` + `serving_dishes` | `DSTU-0706A-002` (HS-3A-002, `2026-07-06`, LUNCH) **`SERVED`** lúc `10:47`, **4 dòng** `serving_dishes`: `com-trang`, `thit-kho-trung-cut`, `canh-bi-do-nau-tom`, `su-su-xao` (tất cả `isSubstitute=false`) | Bản ghi **đã phát** — oracle của `TC-001` (409 khi xác nhận lần 2), `TC-INT-003` (lịch sử bất biến), `TC-INT-009` (append-only), `TC-INT-018` (truy xuất theo `dish_id`) |
| `FX-003` | DB seed `student_distributions` | `DSTU-0706A-001` (HS-3A-001, `2026-07-06`) `PENDING`; HS có allergen **Tôm, Cua**; thực đơn `MENU-2026-27` chứa *"Canh bí đỏ nấu tôm"* ⇒ suất riêng *"Canh bí đỏ thịt băm"* | Oracle của `TC-003` (BR-065 — `isSubstitute=true` + `substitutedForDishName` + `reason` chứa "Tôm") và `TC-INT-012` (`severity=SEVERE`) |
| `FX-004` | DB seed `student_distributions` | **10 dòng `2026-07-07` lớp 3A**: `DSTU-0707A-001…010`, `menuId=MENU-2026-27B`, tất cả `PENDING` | Ngày có phiên kiểm thực **FAILED** — oracle của `TC-007`, `TC-INT-002`, `TC-SEC-010`, `TC-E2E-003` nhánh âm |
| `FX-005` | DB seed `student_distributions` | **3 dòng `2026-07-06` lớp 3B**: `DSTU-0706B-001…003`, trong đó `DSTU-0706B-002` **`SERVED`** lúc `10:52` | Cách ly lớp (`TC-SEC-003`) + tổng hợp 2 lớp (`TC-INT-004`: 3A `planned=10/distributed=3`, 3B `planned=3/distributed=1`) |
| `FX-006` | DB seed `meal_consumption_records` | `MR-TD-3A-001…010` hôm nay, `consumption=PENDING`; `MR-TD-3A-004` có `specialDiet="Vegetarian"` | Ghi nhận tiêu thụ trong ngày (`TC-005`, `TC-E2E-001`) |
| `FX-007` | DB seed `meal_consumption_records` | `MR-0702-001` (`2026-07-02`, `consumption=FULLY`, `recordedAt=11:20`), `MR-0702-004` (`PARTIALLY`), `MR-0702-005` (`NOT_CONSUMED`) | Bản ghi **ngày đã qua** — oracle của `TC-010` (409 SRV-025 khi sửa) và của lịch sử phụ huynh |
| `FX-008` | DB seed `classrooms` (MF02) | `3A` — `teacherId=U-0004` (Nguyễn Thu Trang), sĩ số 38, khối 3 · `3B` — `teacherId=U-0009` (Phan Thị Hồng) | Guard BR-120 (`TC-006`, `TC-SEC-003`) |
| `FX-009` | Stub `StudentProfilePort` | `HS-3A-001` → allergen **đã xác minh** `["Tôm","Cua"]`, severity `SEVERE` (mock: `status="VERIFIED"`, `"Nặng"`) · **`HS-3A-005`** → `["Sữa bò"]` nhưng `status="NEEDS_INFO"` ⇒ **KHÔNG** trả về · `HS-3A-004` → `specialDiet="Vegetarian"` | **Ca then chốt BR-031**: chỉ allergen **đã xác minh** mới sinh suất riêng. `HS-3A-005` là đối chứng âm có sẵn trong seed (`TC-003`) |
| `FX-010` | Stub `MenuQueryPort` | `MENU-2026-27` (2026-07-06, Khối 3, `PUBLISHED`, 4 món LUNCH) · `MENU-TD-3` (hôm nay, chay) · `MENU-2026-27B` (2026-07-07) · **`Optional.empty()`** cho `(2026-07-08, Khối 3, LUNCH)` | Nhánh dương + nhánh **rỗng** → 422 SRV-022 (`TC-002`) |
| `FX-011` | Stub `InspectionGatePort` | (a) `2026-07-06/LUNCH` → `{blocked:false, noSession:false, sessionId:"INSP-20260706-L"}` · (b) `2026-07-07/LUNCH` → **`{blocked:true, failedSteps:["Bước 2 — trong chế biến"], sessionId:"INSP-20260707-L"}`** · (c) `2026-07-09/LUNCH` → **`{noSession:true}`** | 3 nhánh cổng BR-094 (`TC-007`) — (c) là nhánh **không chặn, chỉ cảnh báo** |
| `FX-012` | DB seed `inspection_sessions` (MF07) | `INSP-20260706-L` `overall=PASSED` · **`INSP-20260707-L`** `step2=FAILED`, `coreTemp=68`, `overall=FAILED` | Integration của cổng + truy vấn đối soát §14.1 câu 4. ⚠️ Dùng **`INSP-20260707-L`**, không phải `INS-2026-0707-L` như ví dụ §9.2 (xem **L23**) |
| `FX-013` | Stub `CatererAssignmentPort` | `currentCatererOf(SCH-001, *)` → `PartnerView("CAT-001", "Cty TNHH Suất ăn An Bình")`; một ngày trả `Optional.empty()` | ADR-M09-8 (`TC-014`): snapshot lúc sinh phiên; rỗng ⇒ 2 cột `NULL`, response **ẩn field**, **không** chặn phát suất |
| `FX-014` | Stub `ParentStudentLinkPort` | Parent `P1` liên kết **`HS-3A-001`** + **`HS-1B-007`** (2/5 — BR-201); **không** liên kết `HS-3B-002` | Cách ly PII (`TC-013`, `TC-SEC-005`) |
| `FX-015` | Stub `ClassroomPort` | `isHomeroomTeacher("3A", U-0004)=true` · `isHomeroomTeacher("3A", U-0009)=false` | BR-120 (`TC-006`) |
| `FX-016` | Stub `Dish` (MF05) | `com-trang=260 kcal`, `ca-basa-sot-ca-chua=190`, `canh-rau-ngot-thit-bam=75`, `gia-xao=60` ⇒ tổng **695** | `kcal` tính runtime (`TC-016`, `TC-INT-005`) — trùng đúng con số `MEAL_HISTORY` seed ngày `01/07` |
| `FX-017` | JWT | `{sub:"U-0004", role:"teacher", schoolId:"SCH-001", classId:"3A"}` | GVCN lớp 3A — actor chính (`RoleKey` kebab-case theo **D6**) |
| `FX-018` | JWT | `{sub:"U-0009", role:"teacher", schoolId:"SCH-001", classId:"3B"}` | GVCN lớp khác → cross-class (`TC-SEC-003`) |
| `FX-019` | JWT | `{sub:"U-0003", role:"service-caterers", schoolId:"SCH-001", partnerId:"CAT-001"}` | Đơn vị suất ăn — **chỉ đọc**, cấm `/serve` (`TC-SEC-002`) |
| `FX-020` | JWT | `{sub:"P-0001", role:"parent", schoolId:"SCH-001"}` liên kết `FX-014` | Phụ huynh (`TC-SEC-005`) |
| `FX-021` | JWT | `{role:"medical-staff"}` · `{role:"school-admin"}` · `{role:"system-admin"}` · `{role:"supplier"}` | Đủ 7 role cho ma trận §16 (`TC-SEC-007`, `TC-SEC-008`) |
| `FX-022` | Testcontainer | `PostgreSQLContainer("postgres:16-alpine")` + Flyway `V1` (schema Nam) + `V{n}__seed_mf09.sql` | Nền integration; timeout khởi động 120s |
| `FX-023` | env | `spring.profiles.active=test`, `SFMS_JWT_SECRET=test-secret-32-chars-minimum-len`, `TZ=Asia/Ho_Chi_Minh` | `ck_dist_served_same_day` so ngày theo `Asia/Ho_Chi_Minh` — sai TZ là test đỏ/xanh ngẫu nhiên quanh nửa đêm |
| `FX-024` | **Clock** | `Clock.fixed(2026-07-24T03:00:00Z, Asia/Ho_Chi_Minh)` inject vào service | ⚠️ Seed dùng `vnInDays(0)` (ngày chạy) trong khi `INSPECTIONS`/`MENUS` neo ngày cố định. **Không ghim clock thì gate của "hôm nay" trả `noSession` hay `PASSED` tuỳ ngày chạy CI** ⇒ test đỏ ngẫu nhiên. Mọi TC dùng "hôm nay" **bắt buộc** dùng `FX-024` |
| `FX-025` | Fake event bus | Thu `MealServedToStudent`, `MealDistributionRevoked`, `DuplicateDistributionBlocked`, `MealConsumptionRecorded`, `DistributionErrorReported`, `AllergyConflictDetectedAtServing`, `DailyMealSummaryCompiled` | §7.1 — assert event phát ra, **không** gửi thật (SMTP stub, ADR-000) |
| `FX-026` | ❌ **KHÔNG seed** | Mảng tĩnh `MEAL_HISTORY` của `mock-data.ts` | **Cố ý bỏ** — CF-M09-7/L15: lịch sử là projection, seed nó vào DB là tái tạo đúng lỗi "hai nguồn số liệu lệch nhau" |

**Nguyên tắc cách ly dữ liệu:** mỗi integration test `@Transactional` + rollback, **hoặc** `@Sql("/fixtures/mf09-reset.sql", executionPhase = BEFORE_TEST_METHOD)`.
⚠️ **Ngoại lệ quan trọng:** `TRUNCATE` chỉ áp cho DB test của Testcontainers. Trên môi trường có dữ liệu thật, `serving_dishes` + `meal_consumption_records` là **append-only, giữ tối thiểu 5 năm** (§4.2 TDS — phục vụ điều tra sự cố và thanh tra ATTP) — xem §7 Rollback **R1**.

---

## 4. Test Case Specification

> **TC ID format:** `MF09-TC-[NNN]` (unit) · `MF09-TC-SEC-[NNN]` (security) · `MF09-TC-INT-[NNN]` (integration) · `MF09-TC-CON-[NNN]` (contract OpenAPI) · `MF09-TC-E2E-[NNN]`
> **Severity:** CRITICAL / HIGH / MEDIUM / LOW (theo CVSS)
> **Status:** 🔴 Not written / 🟡 Written-failing / 🟢 Passing
> **Oracle Source:** bắt buộc — chỉ nhận BR / bước UC / MSG nguyên văn / luật `semantic-audit.ts` / hằng trong mock / **D1–D9**. TC không có oracle → reject (**AP-AI-001**).
> **Assert lỗi:** theo **D2** — `$.error.code` + `$.error.message` (**tiếng Anh** nguyên văn `error_message.md` v1.8, theo **D7**) + `$.error.details` là **mảng**.
> **Assert ngày:** theo **D4** — ISO-8601 (`"2026-07-06"` / `"2026-07-06T03:47:00Z"`), **không** `dd/MM/yyyy` (ghi đè ADR-M09-5/C8 — xem **L3**).

### Fixture Isolation Boilerplate (CASE 2.0 — bản Java)

> ⭐ **CASE 2.0 Rule:** mỗi test tạo instance **mới** qua factory. Không shared mutable state (chống **AP-AI-002**).
> ⚠️ Theo **D1**: builder set `code` (mã nghiệp vụ), **không** set `id` (UUID do JPA sinh).
> ⚠️ `ServingDish` **không có setter public** (ADR-M09-1) — factory dựng qua constructor, đúng như production code phải làm.

```java
package com.sfms.mf09;

import com.sfms.common.enums.MealType;
import com.sfms.mf09.domain.*;
import java.time.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Fixture factory cho MF09 — baseline đồng bộ FX-001…FX-026 (§3 TDS-05).
 * D1: code = mã nghiệp vụ; id (UUID) do JPA sinh, KHÔNG set tay.
 * D4: ngày là LocalDate/Instant, KHÔNG chuỗi "dd/MM/yyyy".
 * MỌI test gọi newDistribution()/newMealRecord() để có instance RIÊNG.
 */
public final class MealServingTestFixtures {

    public static final String  SCHOOL_CODE  = "SCH-001";
    public static final String  CATERER_CODE = "CAT-001";
    public static final String  CLASS_3A     = "3A";
    public static final String  TEACHER_3A   = "U-0004";   // Nguyễn Thu Trang (FX-008)
    public static final String  TEACHER_3B   = "U-0009";   // Phan Thị Hồng
    /** FX-024 — ghim clock, nếu không gate của "hôm nay" đỏ/xanh tuỳ ngày chạy CI. */
    public static final Clock   CLOCK = Clock.fixed(
            Instant.parse("2026-07-24T03:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));

    /** Baseline = FX-003 (phiên PENDING của HS có dị ứng đã xác minh, 06/07). */
    @SafeVarargs
    public static StudentDistribution newDistribution(Consumer<StudentDistribution>... overrides) {
        StudentDistribution d = new StudentDistribution();
        d.setCode("DSTU-TEST-" + java.util.UUID.randomUUID().toString().substring(0, 6));
        d.setSchoolCode(SCHOOL_CODE);
        d.setDate(LocalDate.of(2026, 7, 6));            // D4: LocalDate
        d.setMealType(MealType.LUNCH);                  // D6: SCREAMING_SNAKE
        d.setStudentCode("HS-3A-001");
        d.setStudentName("Nguyễn Minh Khang");          // snapshot, JSON "name"
        d.setClassName(CLASS_3A);                       // snapshot, JSON "class"
        d.setMenuCode("MENU-2026-27");                  // ràng buộc C3
        d.setCatererPartnerCode(CATERER_CODE);          // ADR-M09-8 — snapshot lúc sinh phiên
        d.setCatererName("Cty TNHH Suất ăn An Bình");
        d.setStatus(DistributionStatus.PENDING);
        d.setServedAt(null);
        d.setServedBy(null);
        for (Consumer<StudentDistribution> o : overrides) o.accept(d);
        return d;
    }

    /** FX-002 — DSTU-0706A-002 đã SERVED với 4 món đóng băng. */
    public static StudentDistribution newServedDistribution() {
        StudentDistribution d = newDistribution(x -> {
            x.setCode("DSTU-0706A-002");
            x.setStudentCode("HS-3A-002");
            x.setStudentName("Trần Bảo An");
            x.setStatus(DistributionStatus.SERVED);
            x.setServedAt(Instant.parse("2026-07-06T03:47:00Z"));   // 10:47 giờ VN
            x.setServedBy(java.util.UUID.randomUUID());
        });
        d.freezeServedDishes(List.of(                  // ADR-M09-1: ghi ĐÚNG MỘT LẦN
            ServingDish.of("com-trang",            "Cơm trắng",            false, null, null),
            ServingDish.of("thit-kho-trung-cut",   "Thịt kho trứng cút",   false, null, null),
            ServingDish.of("canh-bi-do-nau-tom",   "Canh bí đỏ nấu tôm",   false, null, null),
            ServingDish.of("su-su-xao",            "Su su xào",            false, null, null)));
        return d;
    }

    /** Baseline = FX-006 (bản ghi tiêu thụ hôm nay, chưa ghi nhận). */
    @SafeVarargs
    public static MealConsumptionRecord newMealRecord(Consumer<MealConsumptionRecord>... o) {
        MealConsumptionRecord r = new MealConsumptionRecord();
        r.setCode("MR-TEST-" + java.util.UUID.randomUUID().toString().substring(0, 6));
        r.setSchoolCode(SCHOOL_CODE);
        r.setDate(LocalDate.now(CLOCK));
        r.setMealType(MealType.LUNCH);
        r.setStudentCode("HS-3A-004");
        r.setStudentName("Phạm Đức Minh");             // JSON "student"
        r.setClassName(CLASS_3A);
        r.setConsumption(ConsumptionStatus.PENDING);   // D6 — giá trị khởi tạo, KHÔNG bỏ sót
        for (Consumer<MealConsumptionRecord> c : o) c.accept(r);
        return r;                                      // recordedAt do service set, KHÔNG set tay
    }
}
```

**Kiểm tự động chống shared state** (CI, §6):

```bash
grep -rnE "^\s*(private )?static\s+(final\s+)?(StudentDistribution|ServingDish|MealConsumptionRecord|DistributionErrorReport)\s+" \
     src/test/java/com/sfms/mf09/ | grep -v MealServingTestFixtures.java
# Expected: no output
```

---

### 4.1. UNIT TEST CASES

> Layer `com.sfms.mf09.service`. Repository + **6 port** (`MenuQueryPort`, `StudentProfilePort`, `ClassroomPort`, `InspectionGatePort`, `ParentStudentLinkPort`, `CatererAssignmentPort`) = **Mockito mock**. Không chạm DB.

---

#### MF09-TC-001 — BR-130: xác nhận lần 2 trong cùng phiên → 409, KHÔNG no-op

**Severity:** `CRITICAL`
**Feature Under Test:** `DistributionService.confirmServed()`
**Test File:** `service/ConfirmDistributionTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-01`
**Constraint Ref:** `TDS §17.1 C4` · `AP-M09-03` (Silent duplicate)
**Oracle Source:** **BR-130** (*"Hệ thống **chặn phân phát bữa ăn trùng lặp** — một học sinh không thể được quét hai lần trong cùng một phiên phục vụ bữa ăn."*) + **MSG-021** nguyên văn = `"Duplicate meal distribution: Student has already scanned for this session."` + `TDS` §10 → `SRV-021`, HTTP **409** + `TDS` §13 `TC-UNIT-001`
**Kỹ thuật:** State Transition Testing (transition **không** hợp lệ `SERVED → SERVED`)

**Preconditions:**
- `FX-002` — `DSTU-0706A-002` đang `SERVED`, `servedAt = 2026-07-06T03:47:00Z`, 4 dòng `serving_dishes`
- `FX-017` JWT GVCN 3A · `FX-015` `isHomeroomTeacher=true`

**Test Steps:**
1. **Arrange:** mock repo trả `FX-002`.
2. **Act:** `confirmServed("DSTU-0706A-002", teacher3A)`.
3. **Assert:** exception + trạng thái không đổi.

**Expected Result (PASS):**
- `ConflictException` với `code = "SRV-021"`, HTTP **409**, `message` = **MSG-021** nguyên văn
- `$.error.details` chứa dòng nêu mốc đã phát (`field="status"`), là **mảng** (D2)
- `servedAt` **không đổi** (vẫn `2026-07-06T03:47:00Z`)
- `serving_dishes` **không** có dòng nào được thêm — `servingDishRepository.saveAll()` **không** được gọi (`verify(..., never())`)
- Event `DuplicateDistributionBlocked` được phát (§7.1 — dấu hiệu quy trình/thiết bị lỗi)

**Expected Result (FAIL — dấu hiệu lỗi):**
- Trả **200** im lặng (no-op) ⇒ **AP-M09-03**: giáo viên tưởng vừa phát suất lần 2 thành công, số liệu thống kê đúng nhưng **quy trình mất dấu**; và nếu cổng vào đổi sang máy quét thì quét trùng không còn cảnh báo
- Ghi đè `servedAt` ⇒ mất mốc thời gian trao tay thật

**Current Status:** 🔴 Not written
**Implementation Note:** kiểm trạng thái **sau khi** đã `findByIdForUpdate()` (`PESSIMISTIC_WRITE`) — kiểm trước khi lock là còn race (xem `TC-INT-001`).

---

#### MF09-TC-002 — ADR-M09-1: không tra ra thực đơn → 422, KHÔNG ghi SERVED rỗng món

**Severity:** `CRITICAL`
**Feature Under Test:** `DistributionService.confirmServed()` → `MenuQueryPort.publishedBlock()`
**Test File:** `service/ConfirmDistributionTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-03`
**Constraint Ref:** `TDS §17.1 C2` · `AP-M09-01`
**Oracle Source:** **ADR-M09-1** (*"`serving_dishes` rỗng khi `status=SERVED` là **lỗi dữ liệu**"*) + luật `semantic-audit.ts` §6 **`PHAT_ROI_KHONG_CO_MON`** (*"SERVED nhưng servedDishes rỗng — mất dấu món đã trao"*) + ràng buộc **C3** `data_model.puml` + **MSG-130** nguyên văn = `"No approved or published menu found for [Meal] on [Date] for grade [Grade]. Meal distribution cannot be recorded."` → `SRV-022`, HTTP **422**
**Kỹ thuật:** Decision Table (nhánh "danh sách món rỗng")

**Preconditions:**
- `FX-010` — `MenuQueryPort.publishedBlock(2026-07-08, "Khối 3", LUNCH, SCH-001)` trả **`Optional.empty()`**
- Bản ghi `PENDING` của ngày `2026-07-08`
- Cổng kiểm thực **không** chặn (tách biến, chỉ để lộ đúng nhánh menu rỗng)

**Test Steps:**
1. `confirmServed(code, teacher3A)`.
2. Assert exception + trạng thái + tương tác repo.

**Expected Result (PASS):**
- `ValidationException` `code = "SRV-022"`, HTTP **422**, `message` = **MSG-130** với placeholder đã thay: `[Meal]="LUNCH"`, `[Date]="2026-07-08"`, `[Grade]="Khối 3"` *(D7 — tiếng Anh, **không** dùng chuỗi tiếng Việt của TDS §9.2 — xem L6)*
- Bản ghi **vẫn `PENDING`**, `servedAt` null
- **Không** dòng `serving_dishes` nào được tạo (`verify(servingDishRepository, never()).saveAll(any())`)
- **Không** event `MealServedToStudent` nào được phát

**Expected Result (FAIL):**
- Ghi `SERVED` với `servedDishes = []` ⇒ vi phạm invariant §4.2 TDS và làm **MF11 mất trục điều tra vĩnh viễn** cho bữa đó — không sửa lại được sau sự cố vì `serving_dishes` là append-only

**Current Status:** 🔴 Not written

---

#### MF09-TC-003 — BR-065: suất riêng phải nêu món bị thay + lý do; chỉ dựa trên allergen ĐÃ XÁC MINH

**Severity:** `CRITICAL`
**Feature Under Test:** `DistributionService.personalize(menuBlock, student)`
**Test File:** `service/PersonalizeServingTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-04`
**Constraint Ref:** `TDS §17.1 C2` · `ck_sdish_substitute`
**Oracle Source:** **BR-065** (*"Đối với học sinh có **dị ứng đã xác minh** hoặc chế độ ăn đặc biệt, hệ thống **bắt buộc** Service Caterers phải tạo **kế hoạch bữa ăn riêng**."*) + **BR-031** (*"Thông tin sức khỏe và dị ứng do phụ huynh khai báo phải được **Medical Staff xem xét và xác nhận** trước khi có hiệu lực."*) + `TDS` §5.2 `CHECK ck_sdish_substitute` + `TDS` §13 `TC-UNIT-003`
**Kỹ thuật:** Equivalence Partitioning (`VERIFIED` vs {`PENDING`, `NEEDS_INFO`})

**Preconditions:**
- `FX-009` — `HS-3A-001`: allergen **đã xác minh** `["Tôm","Cua"]` · **`HS-3A-005`**: `["Sữa bò"]` nhưng hồ sơ `NEEDS_INFO` ⇒ port **không** trả về
- `FX-010` — `MENU-2026-27` chứa *"Canh bí đỏ nấu tôm"*

**Test Steps:**
1. `personalize(menuBlock, "HS-3A-001")` → assert có suất thay.
2. `personalize(menuBlock, "HS-3A-005")` → assert **không** có suất thay.
3. Assert bất biến trên toàn bộ danh sách trả về.

**Expected Result (PASS):**
- HS-3A-001: có `ServingDish{ isSubstitute=true, dishName="Canh bí đỏ thịt băm", substitutedForDishName="Canh bí đỏ nấu tôm", reason` chứa `"Tôm" }`; 3 món còn lại `isSubstitute=false`
- **HS-3A-005: 4 món chuẩn, `isSubstitute=false` toàn bộ** — hồ sơ chưa xác minh **không** sinh suất riêng (BR-031)
- Bất biến: `∀ d : d.isSubstitute ⇒ d.substitutedForDishName != null ∧ d.reason != null` (khớp `ck_sdish_substitute`)
- Mọi `ServingDish` có `dishId != null` — món tham chiếu bằng **ID**, không phải tên (§5 Team Plan)

**Expected Result (FAIL):**
- Sinh suất riêng từ khai báo **chưa xác minh** ⇒ bếp nấu thừa suất theo lời khai chưa kiểm chứng, và tệ hơn là **tạo cảm giác an toàn giả** cho một hồ sơ y tế chưa ai duyệt
- `isSubstitute=true` mà thiếu `reason` ⇒ khi điều tra sự cố không biết vì sao em đó ăn món khác cả lớp

**Current Status:** 🔴 Not written
**Implementation Note:** `reason` là chuỗi hiển thị (`"Dị ứng Tôm"` / `"Chế độ Vegetarian"`), **không** phải mã lỗi — không đưa vào `error_message.md`.

---

#### MF09-TC-004 — BR-123: cảnh báo dị ứng là DẪN XUẤT, thực đơn chay không sinh cảnh báo

**Severity:** `HIGH`
**Feature Under Test:** `DistributionService.getDetail()` (projection)
**Test File:** `service/DistributionDetailTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-05`
**Constraint Ref:** `TDS §17.1 C3` · `AP-M09-02`
**Oracle Source:** **BR-123** (*"Khi quét ID học sinh có **dị ứng thực phẩm đã đăng ký** trong phiên phục vụ, hệ thống **tự động kích hoạt cảnh báo**… để ngăn phân phát sai suất ăn."*) + **ADR-M09-2** (mọi thứ ngoài `serving_dishes` là dẫn xuất) + hằng seed `mock-data.ts` đã sửa **27/07/2026** (bỏ `allergyWarning` ở `HS-3A-001`/`HS-3A-009` khi thực đơn chay) + `TDS` §13 `TC-UNIT-004`
**Kỹ thuật:** Equivalence Partitioning (giao allergen ∩ món = ∅ vs ≠ ∅)

**Preconditions:** `FX-001` (`DSTU-TD-3A-001`, thực đơn **chay** `MENU-TD-3`) · `FX-009` (HS-3A-001 allergen `["Tôm","Cua"]` đã xác minh) · `FX-024` clock ghim.

**Test Steps:**
1. `getDetail("DSTU-TD-3A-001", teacher3A)`.
2. Assert 3 field dẫn xuất.
3. Lặp với `FX-003` (thực đơn `MENU-2026-27` **có** "Canh bí đỏ nấu tôm") để lộ nhánh đối lập.

**Expected Result (PASS):**
- Thực đơn chay: `allergyWarning == null`, `isSpecial == "no"`, **`allergens == ["Tôm","Cua"]` vẫn hiển thị** (thông tin, không phải cảnh báo đỏ)
- Thực đơn `MENU-2026-27`: `allergyWarning` chứa `"Tôm"`, `isSpecial == "yes"`, `specialMeal` nêu món thay
- **Không** đọc bất kỳ cột `allergy_warning`/`is_special` nào từ repository (dẫn xuất, `verify` không có query tới cột đó)

**Expected Result (FAIL):**
- Đọc từ cột lưu ⇒ tái tạo đúng lỗi seed mock cũ (**có `allergyWarning` trong khi `isSpecial="no"`, thực đơn chay không chạm dị ứng**) — banner đỏ hiện sai cho 2 em, giáo viên quen dần với cảnh báo sai và **bỏ qua cảnh báo thật**

**Current Status:** 🔴 Not written

---

#### MF09-TC-005 — BR-121 + D6: `consumption` nhận ĐỦ 4 giá trị, kể cả `PENDING`

**Severity:** `HIGH`
**Feature Under Test:** `MealConsumptionService.record()`
**Test File:** `service/MealConsumptionTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-06`
**Constraint Ref:** **D6** · `TDS §5.2 CHECK ck_mcr_recorded`
**Oracle Source:** **BR-121** (*"Hệ thống ghi nhận và theo dõi **lịch sử tiêu thụ bữa ăn** của từng học sinh."*) + **D6** bảng `ConsumptionStatus` = `PENDING | FULLY | PARTIALLY | NOT_CONSUMED` (D6 nêu đích danh MF09 bỏ sót `PENDING`) + **MSG-138** = `"Validation failed: one or more required fields are missing or invalid."` → `SRV-001`, HTTP **400** + `TDS` §9.2 (sửa 29/07, mục **G2**)
**Kỹ thuật:** Data-driven (`@ParameterizedTest`) + Error Guessing

**Preconditions:** `FX-006` `MR-TD-3A-004` `consumption=PENDING`, cùng ngày với `FX-024`.

**Test Steps:**
1. `@ParameterizedTest` với `value ∈ {PENDING, FULLY, PARTIALLY, NOT_CONSUMED}` → `record()`.
2. `record(code, "ATE_HALF", teacher3A)` → assert lỗi.
3. `record(code, "Ăn hết", teacher3A)` → assert lỗi (nhãn tiếng Việt **không** phải giá trị lưu trữ — D6).

**Expected Result (PASS):**
- **4/4** giá trị hợp lệ được ghi; với 3 giá trị ≠ `PENDING`: `recordedAt != null` và `recordedBy = JWT.userId`
- `"ATE_HALF"` và `"Ăn hết"` → `ValidationException` `SRV-001`, HTTP **400**, `message` = MSG-138 nguyên văn; `$.error.details[0].field == "consumption"` và liệt kê **đủ 4** giá trị hợp lệ
- Sự kiện `MealConsumptionRecorded` phát ra cho mỗi lần ghi thành công (§7.1)

**Expected Result (FAIL):**
- Validator chỉ chấp nhận 3 giá trị (copy thông điệp lỗi cũ của TDS) ⇒ **mọi bản ghi mới bị 400 ngay khi tạo**, vì `PENDING` chính là trạng thái khởi tạo — đúng hậu quả mà `TDS` §9.2 G2 và D6 đã cảnh báo

**Current Status:** 🔴 Not written

---

#### MF09-TC-006 — BR-120: chỉ GVCN của lớp được xác nhận phát suất

**Severity:** `CRITICAL`
**Feature Under Test:** `DistributionService.confirmServed()` → guard `ClassroomPort.isHomeroomTeacher()`
**Test File:** `service/DistributionScopeTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-07`
**Constraint Ref:** `TDS §17.1 C5` · `ADR-M09-6` · `AP-M09-06`
**Oracle Source:** **BR-120** (*"**Giáo viên (Teacher)** xác nhận việc phân phát bữa ăn cho học sinh **trong lớp của mình**."*) + **MSG-005** nguyên văn = `"Access denied: You do not have permission to access this resource."` → `SRV-005`, HTTP **403** + `ADR-M09-6` (*"sai phạm vi → 403, **không** trả 404 để tránh rò rỉ sự tồn tại của bản ghi"*)
**Kỹ thuật:** Equivalence Partitioning (own class vs other class)

**Preconditions:** `FX-015` — `isHomeroomTeacher("3A", U-0004)=true`, `isHomeroomTeacher("3A", U-0009)=false`; bản ghi lớp 3A `PENDING`.

**Test Steps:**
1. `confirmServed(code, teacher3B)` (JWT `FX-018`).
2. Assert lỗi + trạng thái.
3. Assert `classroomId` được lấy **từ bản ghi + JWT**, không từ tham số client.

**Expected Result (PASS):**
- `ForbiddenException` `SRV-005`, HTTP **403**, `message` = MSG-005 nguyên văn — **không phải 404**
- Bản ghi vẫn `PENDING`; `serving_dishes` không có dòng mới
- `ClassroomPort.isHomeroomTeacher()` được gọi **trước** `MenuQueryPort`/`InspectionGatePort` — không lãng phí gọi port ngoài khi đã sai quyền
- Với `teacher3A`: đi tiếp bình thường (nhánh dương)

**Expected Result (FAIL):**
- Trả **404** ⇒ vẫn rò rỉ: 404 vs 403 phân biệt được bản ghi **có tồn tại hay không** ⇒ dò được lớp/HS nào có suất ăn hôm nay (PII trẻ em)
- Không guard ⇒ **giáo viên bất kỳ xác nhận hộ lớp khác**, hồ sơ phát suất ghi sai người chịu trách nhiệm (BR-120 mất hiệu lực)

**Current Status:** 🔴 Not written

---

#### MF09-TC-007 — BR-094 (MF07): `blocked` → 403; `noSession` → KHÔNG chặn, chỉ cảnh báo

**Severity:** `CRITICAL`
**Feature Under Test:** `DistributionService.confirmServed()` → `InspectionGatePort.gateFor()`
**Test File:** `service/ServingGateTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-08`
**Constraint Ref:** `TDS §17.1 C6` · `ADR-M09-4` · `AP-M09-04` (Gate bypass)
**Oracle Source:** **BR-094** *(trích chéo **MF07** — CF-M09-4/L12)* (*"Bữa ăn **không được phép phục vụ** nếu bất kỳ bước nào trong ba bước kiểm tra thất bại."*) + **MSG-129** nguyên văn = `"Meal cannot be served: inspection step [Step Name] did not pass."` → `SRV-094`, HTTP **403** + **ADR-M09-4** (*"`noSession=true` → **không chặn**, chỉ gắn cờ cảnh báo — quy định chỉ nói tới bước kiểm tra **thất bại**"*) + ràng buộc **C3** `data_model.puml`
**Kỹ thuật:** Decision Table Testing (3 nhánh cổng)

**Preconditions:** `FX-011` — 3 stub: (a) không chặn, (b) `blocked=true` + `failedSteps=["Bước 2 — trong chế biến"]` + `sessionId="INSP-20260707-L"`, (c) `noSession=true`.

**Test Steps:**
1. Ca (a): `confirmServed` trên bản ghi `2026-07-06` → assert 200.
2. Ca (b): `confirmServed` trên `DSTU-0707A-001` → assert 403.
3. Ca (c): `confirmServed` trên ngày không có phiên → assert **200 + warning**.

**Expected Result (PASS):**

| Ca | `gate` | Kết quả |
|---|---|---|
| (a) | `blocked=false, noSession=false` | **200**, `status=SERVED`, `serving_dishes` ≥ 1, message = **MSG-045** |
| (b) | `blocked=true` | **403** `SRV-094`, `message` = **MSG-129** với `[Step Name] = "Bước 2 — trong chế biến"`; `$.error.details` chứa `inspectionSessionId = "INSP-20260707-L"` (**L23** — mã thật, không phải `INS-2026-0707-L`); bản ghi **vẫn `PENDING`** |
| (c) | `noSession=true` | **200** — **KHÔNG chặn**; response có `warning` báo chưa có phiên kiểm thực; `status=SERVED` |

- `gateFor()` được gọi **trước** khi ghi `SERVED` (verify thứ tự tương tác)

**Expected Result (FAIL):**
- Coi `noSession` như `blocked` ⇒ **chặn oan toàn trường** vào ngày MF07 chưa kịp mở phiên — bữa trưa đứng hình, và quy trình giấy dự phòng phải kích hoạt (§12.3 TDS)
- Không gọi `gateFor()` ⇒ **AP-M09-04**: bữa ăn có bước kiểm thực trượt vẫn tới tay học sinh. Đây là ca nghiêm trọng nhất trong toàn module

**Current Status:** 🔴 Not written
**Implementation Note:** đọc MF07 **qua port theo ID** — cấm `@ManyToOne InspectionSession` trong package `com.sfms.mf09` (**AP-M09-07**, §4b Team Plan).

---

#### MF09-TC-008 — ADR-M09-3: `revoke` chỉ trong ngày; `serving_dishes` KHÔNG bị xoá

**Severity:** `HIGH`
**Feature Under Test:** `DistributionService.revokeServed()`
**Test File:** `service/RevokeDistributionTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-09`, `COND-18`
**Constraint Ref:** `TDS §17.1 C2` · `ADR-M09-3` · `AP-M09-01` (Frozen record mutated)
**Oracle Source:** **ADR-M09-3** (*"FE cho phép bỏ đánh dấu; backend cho phép qua endpoint riêng **chỉ GVCN của lớp và trong ngày**, ghi audit"*) + **MSG-132** nguyên văn = `"Cannot revoke a distribution that was confirmed on a previous day."` → `SRV-024`, HTTP **409** + **ADR-M09-1** (*"append-only… kể cả khi `revoke`"*)
**Kỹ thuật:** Boundary Value Analysis (`T = ngày phát` vs `ngày phát + 1`)

**Preconditions:** `FX-002` đã `SERVED` ngày `2026-07-06`; clock `FX-024` cho 2 ca: `2026-07-06` (trong ngày) và `2026-07-07`.

**Test Steps:**
1. Clock = `2026-07-06` → `revokeServed("DSTU-0706A-002", teacher3A)`.
2. Clock = `2026-07-07` → `revokeServed(...)` lần nữa.

**Expected Result (PASS):**
- Trong ngày: `status → PENDING`, `servedAt = null`, `servedBy = null`; **4 dòng `serving_dishes` VẪN còn nguyên**; event `MealDistributionRevoked` phát ra kèm actor (§7.1 — `Async? = No`, audit bắt buộc)
- Ngày hôm sau: `ConflictException` `SRV-024`, HTTP **409**, `message` = **MSG-132** nguyên văn; bản ghi giữ `SERVED`
- **Không** lời gọi `delete`/`update` nào tới `ServingDishRepository` ở cả 2 ca

**Expected Result (FAIL):**
- Xoá `serving_dishes` khi revoke ⇒ **AP-M09-01**: mất bằng chứng món đã trao tay dù đứa trẻ **đã ăn** — revoke chỉ là sửa trạng thái sổ sách, không xoá được sự thật lịch sử
- Cho revoke ngày cũ ⇒ số liệu đã vào tóm tắt gửi phụ huynh (BR-133) bị viết lại sau lưng

**Current Status:** 🔴 Not written

---

#### MF09-TC-009 — UC-016 AF1: còn báo lỗi phân phát chưa xử lý ⇒ chặn hoàn tất lớp

**Severity:** `HIGH`
**Feature Under Test:** `DistributionService.reportError()` + `confirmBatch()`
**Test File:** `service/DistributionErrorReportTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-10`
**Constraint Ref:** `TDS §10 SRV-023`
**Oracle Source:** **UC-016 AF1** nguyên văn (*"Teacher identifies an incorrect meal assignment and reports the error. The system notifies the Service Caterers for immediate correction (BR-065). **Distribution completion is blocked until the issue is resolved.**"*) + **MSG-131** nguyên văn = `"Distribution completion is blocked: an unresolved distribution error was reported."` → `SRV-023`, HTTP **409**
**Kỹ thuật:** State Transition Testing

**Preconditions:** lớp 3A ngày hôm nay có 10 phiên `PENDING`; 1 `DistributionErrorReport{resolved=false}` gắn vào `DSTU-TD-3A-003`.

**Test Steps:**
1. `reportError("DSTU-TD-3A-003", {reason:"Phát nhầm suất chay cho em không đăng ký"}, teacher3A)`.
2. `confirmBatch({ids: 10 mã, atomic:true}, teacher3A)`.
3. Đánh dấu report `resolved=true` → `confirmBatch` lại.

**Expected Result (PASS):**
- Bước 1: tạo báo lỗi, event `DistributionErrorReported` phát tới **Service Caterers** + Audit (§7.1); bản ghi giữ `PENDING`
- Bước 2: `ConflictException` `SRV-023`, HTTP **409**, `message` = **MSG-131** nguyên văn; **không bản ghi nào** chuyển `SERVED` (atomic)
- Bước 3: `confirmBatch` thành công, `confirmed = 10`

**Expected Result (FAIL):**
- Cho hoàn tất khi còn báo lỗi treo ⇒ AF1 mất hiệu lực: lớp được đánh dấu "đã phát xong" trong khi có em **đang cầm sai suất** — đúng loại lỗi mà BR-065 sinh ra để chặn

**Current Status:** 🔴 Not written
**Implementation Note:** ⚠️ **Thiếu oracle cho quy tắc "ai được đánh dấu `resolved`"** — UC-016 AF1 chỉ nói *"Once the meal has been corrected, the **Teacher** verifies the correction"*, không nói Service Caterers có quyền đóng hay không. TC này **không** assert ai đóng được; ghi lại để **Huy** bổ sung.

---

#### MF09-TC-010 — BR-121/BR-133: khoá sửa mức tiêu thụ sau ngày phục vụ

**Severity:** `MEDIUM`
**Feature Under Test:** `MealConsumptionService.record()`
**Test File:** `service/MealConsumptionTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-11`
**Constraint Ref:** `TDS §6.4` (FSM `MealConsumptionRecord`)
**Oracle Source:** **BR-133** (*"Hệ thống **tự động biên soạn** thực đơn đã phục vụ… gửi **thông báo tóm tắt hàng ngày** đến phụ huynh."*) + `TDS` §6.4 note (*"Sau khi hết ngày → khoá sửa (read-only), vì số liệu đã vào tóm tắt gửi phụ huynh"*) + **MSG-133** nguyên văn = `"Consumption record is locked after the serving day."` → `SRV-025`, HTTP **409**
**Kỹ thuật:** Boundary Value Analysis (cùng ngày vs ngày kế tiếp)

**Preconditions:** `FX-007` — `MR-0702-001` (`2026-07-02`, `FULLY`); clock `FX-024` = `2026-07-24`.

**Test Steps:**
1. `record("MR-0702-001", "PARTIALLY", teacher3A)`.
2. Đối chứng: sửa `MR-TD-3A-004` (cùng ngày với clock) từ `FULLY` → `PARTIALLY`.

**Expected Result (PASS):**
- Bản ghi ngày cũ: `ConflictException` `SRV-025`, HTTP **409**, `message` = **MSG-133** nguyên văn; `consumption` giữ `FULLY`, `recordedAt` không đổi
- Bản ghi trong ngày: sửa được, `recordedAt` **được cập nhật** và ghi audit (§6.4 — *"ghi đè `recordedAt` + audit"*)

**Expected Result (FAIL):**
- Cho sửa ngày cũ ⇒ phụ huynh đã nhận tóm tắt "con ăn hết" hôm qua, hôm nay số liệu đổi thành "ăn một phần" mà **không có vết** — mất tính nhất quán của thông báo đã gửi

**Current Status:** 🔴 Not written

---

#### MF09-TC-011 — CF-M09-10: công thức `status` của tổng hợp ngày × lớp (3 nhánh biên)

**Severity:** `MEDIUM`
**Feature Under Test:** `DistributionService.summary()`
**Test File:** `service/DistributionSummaryTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-12`
**Constraint Ref:** `TDS §17.1 C3` · `AP-M09-02`
**Oracle Source:** hằng trong mock `distributionSummaries()` (`mock-data.ts`) — `distributed === 0 ? "PENDING" : distributed < planned ? "IN_PROGRESS" : "COMPLETED"` + **UC-132** Normal Flow bước 2 (*"classroom name, student count, special meal count, and serving status"*) + **UC-132 AF2** (*"System highlights classrooms where distribution confirmation has not been received from the homeroom teacher (BR-120)"*)
**Kỹ thuật:** Boundary Value Analysis (`distributed ∈ {0, 1, planned-1, planned}`)

**Preconditions:** lớp 3A `planned=10`; mock repo trả `distributed` theo tham số.

**Test Steps:** `@ParameterizedTest` với `distributed ∈ {0, 1, 9, 10}`.

**Expected Result (PASS):**

| `distributed` | `status` kỳ vọng |
|---|---|
| 0 | `PENDING` |
| 1 | `IN_PROGRESS` |
| 9 | `IN_PROGRESS` |
| 10 | `COMPLETED` |

- `specialMeals` đếm số bản ghi có `isSpecial` dẫn xuất = `"yes"` (**không** đọc cột)
- `id` của dòng tổng hợp theo khuôn mock `DST-<yyyymmdd>-<class>` (VD `DST-20260706-3A`)

**Expected Result (FAIL):**
- Sai biên `distributed = planned - 1` → `COMPLETED` ⇒ bếp và Ban giám hiệu thấy lớp "đã phát xong" trong khi **còn 1 em chưa nhận suất**
- Đọc từ bảng tổng hợp đóng băng ⇒ tái tạo lỗi mock đã bỏ 21/07/2026 (bếp không thấy việc GV vừa phát)

**Current Status:** 🔴 Not written

---

#### MF09-TC-012 — `servedAt` phải cùng ngày với `date` và đi kèm `servedBy`

**Severity:** `HIGH`
**Feature Under Test:** `DistributionService.confirmServed()`
**Test File:** `service/ConfirmDistributionTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-13`
**Constraint Ref:** `ck_dist_served_at`, `ck_dist_served_same_day`
**Oracle Source:** luật `semantic-audit.ts` §6 **`PHAT_SAI_NGAY`** (*"date=${d.date} nhưng servedAt=${d.servedAt}"*) + **`TRANG_THAI_VS_GIO_PHAT`** (*"status=SERVED nhưng thiếu servedAt"* / *"status≠SERVED nhưng đã có servedAt"*) + `TDS` §4.2 invariant + `TDS` §5.2 CHECK
**Kỹ thuật:** Constraint-based (tầng service; tầng DB ở `TC-INT-008`)

**Preconditions:** clock `FX-024`; bản ghi `PENDING` của **đúng ngày clock**.

**Test Steps:**
1. `confirmServed()` → assert `servedAt`.
2. Thử `confirmServed()` trên bản ghi ngày `2026-07-06` khi clock = `2026-07-24`.

**Expected Result (PASS):**
- `servedAt = Instant.now(CLOCK)`, và `servedAt` quy đổi sang `Asia/Ho_Chi_Minh` **cùng ngày** với `date`
- `servedBy = JWT.userId`, `status = SERVED` — cả 3 field set **cùng lúc**
- Phát suất cho bản ghi của **ngày khác hôm nay** → bị từ chối (không tạo `servedAt` lệch ngày). ⚠️ **Thiếu oracle mã lỗi cho ca này** — TDS §10 không có mã cho "phát suất cho ngày không phải hôm nay". Test assert **có exception** và ghi rõ *"thiếu oracle — đề nghị Huy cấp MSG"*, **không** tự gán mã

**Expected Result (FAIL):**
- `servedAt` lấy giờ máy chủ UTC rồi so ngày theo UTC ⇒ mọi suất phát sau 17:00 giờ VN bị tính sang ngày hôm sau → `ck_dist_served_same_day` đỏ hàng loạt (đây là lý do `FX-023` ghim `TZ`)

**Current Status:** 🔴 Not written

---

#### MF09-TC-013 — BR-201: phụ huynh chỉ đọc con đã liên kết

**Severity:** `CRITICAL`
**Feature Under Test:** `MealHistoryService.listForChild()` → `ParentStudentLinkPort.isLinked()`
**Test File:** `service/MealHistoryScopeTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-14`
**Constraint Ref:** `TDS §17.1 C5` · `ADR-M09-6` · `AP-M09-06`
**Oracle Source:** **BR-201** (*"Một tài khoản phụ huynh được liên kết tối đa **5 hồ sơ học sinh**…"*) + **MSG-005** nguyên văn → `SRV-005`, HTTP **403** + `ADR-M09-6` (*"không trả 404 để tránh rò rỉ sự tồn tại của bản ghi"*) + PDPD
**Kỹ thuật:** Equivalence Partitioning + Error Guessing

**Preconditions:** `FX-014` — Parent `P1` liên kết `HS-3A-001` + `HS-1B-007` (2/5), **không** liên kết `HS-3B-002`.

**Test Steps:**
1. `listForChild("HS-1B-007", filter, parentP1)` → nhánh dương.
2. `listForChild("HS-3B-002", filter, parentP1)` → nhánh âm.
3. `listForChild("HS-KHONG-TON-TAI", filter, parentP1)` → assert **cùng** mã lỗi với (2).

**Expected Result (PASS):**
- (1): **200**, mọi item có `studentId = "HS-1B-007"`
- (2): **403** `SRV-005`, message = MSG-005 nguyên văn
- (3): **403** `SRV-005` — **giống hệt (2)**, không phải 404/MSG-137: từ ngoài nhìn vào **không phân biệt được** HS không tồn tại với HS tồn tại nhưng không phải con mình

**Expected Result (FAIL):**
- (3) trả **404 MSG-137** còn (2) trả 403 ⇒ chênh lệch mã lỗi trở thành **oracle dò danh sách học sinh** của toàn trường bằng cách thử `studentId` — rò rỉ PII trẻ em qua kênh phụ

**Current Status:** 🔴 Not written

---

#### MF09-TC-014 — ADR-M09-8: `caterer` là snapshot lúc sinh phiên, KHÔNG tra cứu sống

**Severity:** `MEDIUM`
**Feature Under Test:** `DistributionPlanHandler` (consume `MenuPublished`) + `MealHistoryService`
**Test File:** `service/CatererSnapshotTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-15`
**Constraint Ref:** `ADR-M09-8`
**Oracle Source:** **ADR-M09-8** (*"gọi port này **một lần** và **ghi cứng** `catererPartnerId`/`catererName`… NCC phục vụ trường có thể đổi theo hợp đồng mới, nhưng lịch sử bữa ăn cũ phải phản ánh đúng NCC **tại thời điểm đó**"* + *"nếu port trả rỗng, 2 cột để `NULL`, response **ẩn field** thay vì báo lỗi, **không chặn** phát suất"*) + fixture `CAT-001` (`mock-data.ts` `PARTNERS`)
**Kỹ thuật:** State Transition (trước/sau khi đổi NCC) + Error Guessing (port rỗng)

**Preconditions:** `FX-013` — port trả `CAT-001`; một ngày khác port trả `Optional.empty()`.

**Test Steps:**
1. Phát event `MenuPublished` → sinh phiên; assert 2 cột snapshot.
2. Đổi stub port sang `CAT-002` → `MealHistoryService.listForChild()`; assert **vẫn** `CAT-001`.
3. `verify(catererAssignmentPort, never()).currentCatererOf(any(), any())` trong luồng đọc lịch sử.
4. Ca port rỗng → sinh phiên; assert không chặn.

**Expected Result (PASS):**
- (1): `catererPartnerId = "CAT-001"`, `catererName = "Cty TNHH Suất ăn An Bình"`; port được gọi **đúng 1 lần**
- (2): lịch sử vẫn trả `CAT-001` — **không** bị viết lại theo NCC hiện tại
- (3): **0 lời gọi** port trong luồng đọc
- (4): 2 cột `NULL`, JSON **không có** key `caterer`/`catererPartnerId` (ẩn field, không phải `null` trần); phiên vẫn sinh bình thường

**Expected Result (FAIL):**
- Tra `Partner` sống mỗi lần đọc ⇒ trường đổi nhà thầu tháng sau thì **toàn bộ lịch sử cũ hiển thị sai đơn vị nấu ăn** — khi điều tra ngộ độc sẽ quy trách nhiệm nhầm nhà thầu

**Current Status:** 🔴 Not written
**Implementation Note:** `catererPartnerId` là **`Partner.code`** (`CAT-001`), **không** phải `SupplierPartnership.id` (`PTN-…`) — TDS §9.2 đã sửa lỗi này ngày 29/07 (mục G1). Assert tiền tố `CAT-`.

---

#### MF09-TC-015 — `confirm` batch: `atomic=true` rollback toàn bộ, `atomic=false` trả kết quả từng dòng

**Severity:** `HIGH`
**Feature Under Test:** `DistributionService.confirmBatch()`
**Test File:** `service/BatchConfirmTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-16`
**Constraint Ref:** `TDS §9.2` (`POST /student-distributions/confirm`)
**Oracle Source:** `TDS` §9.2 (*"`atomic=true` → **toàn bộ rollback** nếu có 1 dòng lỗi (mặc định); `atomic=false` → ghi nhận từng dòng, trả `results`"*) + **MSG-045** nguyên văn = `"Meal distribution list confirmed for class [Class Name]."` + **MSG-021** cho dòng lỗi
**Kỹ thuật:** Decision Table (atomic × có/không dòng lỗi)

**Preconditions:** 3 mã: `DSTU-0706A-001` (PENDING), `DSTU-0706A-003` (PENDING), `DSTU-0706A-002` (**đã SERVED** — sẽ lỗi 409).

**Test Steps:**
1. `confirmBatch({ids:[3 mã], atomic:true})`.
2. `confirmBatch({ids:[3 mã], atomic:false})`.

**Expected Result (PASS):**
- **atomic=true:** ném exception, **0/3** bản ghi đổi trạng thái (rollback), `serving_dishes` không có dòng mới
- **atomic=false:** HTTP **200**, `confirmed=2`, `failed=1`; `results[]` có 2 dòng `status=SERVED` + `servedAt` ISO, 1 dòng giữ `PENDING` kèm `error.code="SRV-021"` và `error.message` = MSG-021 nguyên văn
- Message tổng của ca thành công = **MSG-045** với `[Class Name]` = `"3A"`
- Mặc định (không truyền `atomic`) = **`true`**

**Expected Result (FAIL):**
- `atomic=true` mà ghi một phần rồi mới ném ⇒ lớp ở trạng thái nửa vời, giáo viên bấm lại thì 2 em đầu báo 409 và **không ai biết còn thiếu em nào**

**Current Status:** 🔴 Not written

---

#### MF09-TC-016 — `kcal` tính runtime từ `Dish` (MF05) theo `dish_id` đã đóng băng

**Severity:** `MEDIUM`
**Feature Under Test:** `MealHistoryService.listForChild()` projection
**Test File:** `service/MealHistoryProjectionTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-17`
**Constraint Ref:** `TDS §17.1 C3`
**Oracle Source:** `TDS` §9.2 (*"`kcal` tổng và `kcal` từng món lấy từ **`Dish` (MF05)** theo `dish_id` đã đóng băng"*) + **ADR-M09-2** + fixture `FX-016` (`260+190+75+60 = 695`, trùng đúng `MEAL_HISTORY` seed ngày `01/07`)
**Kỹ thuật:** Contract Testing (giá trị dẫn xuất khớp nguồn)

**Preconditions:** `FX-016` bảng kcal theo `dishId`; 1 phiên `SERVED` với 4 món.

**Test Steps:** `listForChild("HS-3A-001", …)` → assert `kcal` tổng và từng món.

**Expected Result (PASS):**
- `items[0].kcal == 695`; mỗi phần tử `dishes[]` có `kcal` riêng và `dishId != null`
- Đổi kcal của `com-trang` trong stub → gọi lại → tổng đổi theo ⇒ chứng minh **tính runtime**, không lưu cột
- `dishes[]` lấy từ `serving_dishes` (đã đóng băng), **không** tính lại từ thực đơn hiện tại

**Expected Result (FAIL):**
- Lưu `kcal` thành cột ⇒ MF05 sửa định lượng món thì lịch sử cũ vẫn hiện số cũ **hoặc** bị ghi đè — cả hai đều sai theo ranh giới *dẫn xuất / sự kiện* của ADR-M09-2 (`dish_id` là sự kiện, `kcal` là dẫn xuất)

**Current Status:** 🔴 Not written

---

### 4.2. SECURITY TEST CASES

> `@SpringBootTest` + **MockMvc** + JWT giả lập **7 role** (BR-007). Nhóm này hiện thực yêu cầu bắt buộc của prompt §2c-3b: **"gọi API trực tiếp bỏ qua UI"** — vì trong mock, guard nằm ở **component FE** (CF-M09-9), gọi thẳng API là lách được toàn bộ luật ATTP.

---

#### MF09-TC-SEC-001 — Không JWT → 401 trên MỌI endpoint MF09

**Severity:** `CRITICAL`
**OWASP:** `A01:2021 — Broken Access Control`
**CWE:** `CWE-306 — Missing Authentication for Critical Function`
**Feature Under Test:** Spring Security filter chain × **15 endpoint** §9.1
**Test File:** `web/MealServingAuthTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-32`
**Oracle Source:** `TDS` §16 (mọi dòng đều `JWT`) + **MSG-004** nguyên văn = `"Session expired. Please log in again."` → `SRV-004`, HTTP **401**
**Kỹ thuật:** Data-driven (parameterized 15 endpoint)

**Preconditions:** không gửi header `Authorization`.

**Test Steps (Attack Simulation):** `@ParameterizedTest` gọi lần lượt 15 endpoint §9.1 (7 GET, 8 POST/PATCH) không kèm token.

**Expected Result (PASS = hệ thống an toàn):**
- **15/15** trả **401** với `$.error.code == "SRV-004"`, `$.error.message` = MSG-004 nguyên văn (D2/D7)
- **Không** endpoint nào trả 200/403/500
- Body **không** chứa bất kỳ dữ liệu HS nào

**Expected Result (FAIL = lỗ hổng):**
- Bất kỳ endpoint nào trả 200 ⇒ **danh sách họ tên + lớp + hồ sơ dị ứng trẻ em truy cập được ẩn danh** — vi phạm PDPD ở mức nghiêm trọng nhất của module

**Current Status:** 🔴 Not written

---

#### MF09-TC-SEC-002 — Service Caterers gọi `/serve` → 403 (BR-120), dù UI đã ẩn nút

**Severity:** `CRITICAL`
**OWASP:** `A01:2021 — Broken Access Control`
**CWE:** `CWE-862 — Missing Authorization`
**Feature Under Test:** `POST /api/v1/student-distributions/{code}/serve`
**Test File:** `web/DistributionAuthzTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-07`, `COND-36`
**Oracle Source:** **BR-120** (quyền xác nhận thuộc **Teacher**) + `TDS` §16 (dòng `/serve`: chỉ `TEACHER ✅ Own class`, mọi role khác ❌) + ghi chú màn `service-caterers/meal-service` (*"Service Caterers chỉ xem, giáo viên là người quét phát"*) + **MSG-005** → `SRV-005`, HTTP **403**
**Kỹ thuật:** Error Guessing (nút bị ẩn ở UI, gọi thẳng API)

**Preconditions:** `FX-019` JWT `service-caterers` + `partnerId=CAT-001`; bản ghi `DSTU-TD-3A-001` `PENDING`.

**Test Steps (Attack Simulation):**
1. `POST /api/v1/student-distributions/DSTU-TD-3A-001/serve` với JWT Caterers.
2. Lặp cho `/confirm`, `/revoke`, `/error-report`, `PATCH /meal-records/{code}`, `POST /meal-records/batch`.

**Expected Result (PASS):**
- **6/6** trả **403** `SRV-005`; bản ghi **vẫn `PENDING`**; `serving_dishes` không có dòng mới
- Caterers **vẫn** đọc được `GET /student-distributions`, `/summary`, `/allergy-alerts`, `/special-meals` (đúng §16 — read-only phạm vi trường)

**Expected Result (FAIL):**
- Ghi được ⇒ **đơn vị nấu ăn tự xác nhận đã trao suất cho học sinh** — người xác nhận không phải người có mặt lúc trao tay, BR-120 mất hiệu lực và hồ sơ phát suất không còn giá trị đối chiếu khi có sự cố

**Current Status:** 🔴 Not written

---

#### MF09-TC-SEC-003 — Giáo viên lớp khác xác nhận hộ → 403 (cross-class)

**Severity:** `CRITICAL`
**OWASP:** `A01:2021 — Broken Access Control`
**CWE:** `CWE-639 — Authorization Bypass Through User-Controlled Key`
**Feature Under Test:** `POST /{code}/serve` · `GET /meal-records?class=`
**Test File:** `web/DistributionAuthzTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-07`, `COND-36`
**Oracle Source:** **BR-120** (*"trong lớp của mình"*) + `ADR-M09-6` + **MSG-005** + `TDS` §13 `TC-INT-004`
**Kỹ thuật:** Error Guessing

**Preconditions:** `FX-018` JWT GVCN **3B**; `FX-005` bản ghi lớp 3A.

**Test Steps (Attack Simulation):**
1. `POST /student-distributions/DSTU-TD-3A-001/serve` bằng JWT 3B.
2. `GET /meal-records?class=3A` bằng JWT 3B.
3. `GET /student-distributions?class=3A` bằng JWT 3B.

**Expected Result (PASS):**
- (1): **403** `SRV-005`; bản ghi 3A vẫn `PENDING`
- (2)(3): **200 với `items: []`** (đã lọc theo phạm vi) **HOẶC** **403** `SRV-005` — **tuyệt đối không** trả dữ liệu lớp 3A. ⚠️ TDS §13 `TC-E2E-002` cho phép cả 2 hành vi; test viết **khoan dung** đúng như vậy và ghi rõ *"cần Nam chốt 1 hành vi để FE hiển thị nhất quán"*
- `total` trong ca 200 phải là **0**, không phải số của lớp 3A

**Expected Result (FAIL):**
- Trả dữ liệu lớp 3A ⇒ mọi giáo viên đọc được hồ sơ dị ứng + lịch sử ăn uống của toàn bộ học sinh trường — PII trẻ em, ngoài phạm vi công việc

**Current Status:** 🔴 Not written

---

#### MF09-TC-SEC-004 — Parent bị chặn toàn bộ endpoint vận hành

**Severity:** `HIGH`
**OWASP:** `A01:2021` · **CWE:** `CWE-862`
**Feature Under Test:** ma trận §16 — cột `PARENT`
**Test File:** `web/RoleMatrixTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-32`, `COND-33`
**Oracle Source:** `TDS` §16 — Parent chỉ ✅ ở **2 dòng**: `GET /students/{code}/meal-history` và `GET /students/{code}/special-meal-records`; ❌ ở 11 dòng còn lại + **MSG-005**
**Kỹ thuật:** Data-driven

**Preconditions:** `FX-020` JWT Parent P1.

**Test Steps:** gọi đủ 15 endpoint bằng JWT Parent.

**Expected Result (PASS):**
- **2** endpoint cho phép → 200 (với `studentId` là con đã liên kết)
- **13** endpoint còn lại → **403** `SRV-005` (kể cả `GET /student-distributions`, `/summary`, `/allergy-alerts`, `/special-meals`, `/meal-records`)

**Expected Result (FAIL):**
- Parent đọc được `/allergy-alerts` ⇒ **một phụ huynh xem được hồ sơ dị ứng của con nhà khác** — đây là dữ liệu y tế của trẻ, thuộc nhóm nhạy cảm nhất của PDPD

**Current Status:** 🔴 Not written

---

#### MF09-TC-SEC-005 — Parent đọc lịch sử của trẻ không phải con mình → 403 (không 404)

**Severity:** `CRITICAL`
**OWASP:** `A01:2021 — Broken Access Control (IDOR)`
**CWE:** `CWE-639`
**Legal:** **PDPD (NĐ 13/2023/NĐ-CP)** — dữ liệu cá nhân trẻ em
**Feature Under Test:** `GET /api/v1/students/{studentCode}/meal-history` · `/special-meal-records`
**Test File:** `web/MealHistoryAuthzTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-14`, `COND-36`
**Oracle Source:** **BR-201** + `ADR-M09-6` (*"403 SRV-005, không trả 404 để tránh rò rỉ sự tồn tại của bản ghi"*) + **MSG-005** + `TDS` §13 `TC-E2E-002`
**Kỹ thuật:** Error Guessing (IDOR — đổi `studentId` trên URL)

**Preconditions:** `FX-014` Parent P1 liên kết `HS-3A-001`, `HS-1B-007`.

**Test Steps (Attack Simulation):**
1. `GET /students/HS-3B-002/meal-history` (HS có thật, không phải con).
2. `GET /students/HS-9Z-999/meal-history` (HS không tồn tại).
3. `GET /students/HS-1B-007/meal-history` (con mình — đối chứng dương).

**Expected Result (PASS):**
- (1) và (2): **403** `SRV-005`, **cùng** body, **cùng** thời gian phản hồi bậc lớn (không có kênh phụ phân biệt)
- (3): **200**, mọi item `studentId = "HS-1B-007"`
- **Không** endpoint nào trả 404/MSG-137 cho HS ngoài phạm vi

**Expected Result (FAIL):**
- (2) trả 404 còn (1) trả 403 ⇒ dò được **danh sách mã học sinh có thật** của toàn trường bằng vét cạn `HS-<lớp>-<số>` — khuôn mã dễ đoán, nên đây là lỗ hổng khai thác được trong vài phút

**Current Status:** 🔴 Not written

---

#### MF09-TC-SEC-006 — Danh tính lấy TỪ JWT; `schoolId`/`classId`/`studentId` client gửi bị bỏ qua

**Severity:** `CRITICAL`
**OWASP:** `A01:2021` · **CWE:** `CWE-807 — Reliance on Untrusted Inputs in a Security Decision`
**Feature Under Test:** `StudentDistributionSpec.build()` + controller
**Test File:** `web/JwtScopeTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-34`
**Constraint Ref:** `TDS §17.1 C5` · `AP-AI-006` (Trust FE identity)
**Oracle Source:** `ADR-M09-6` (*"tất cả lấy danh tính **từ JWT**, không tin body/query"*) + `TDS` §17.2 mục 5
**Kỹ thuật:** Error Guessing (tham số độc hại)

**Preconditions:** `FX-017` JWT GVCN 3A (`schoolId=SCH-001`).

**Test Steps (Attack Simulation):**
1. `GET /student-distributions?schoolId=SCH-999&class=3B`.
2. `POST /student-distributions/confirm` với body `{ids:[…], "teacherId":"U-0009", "schoolId":"SCH-999"}`.
3. `GET /meal-records?studentId=HS-3B-002`.

**Expected Result (PASS):**
- (1): `schoolId` trong query **bị bỏ qua**; kết quả lọc theo `SCH-001` của JWT; `class=3B` không thuộc phạm vi ⇒ `items: []` hoặc 403 (nhất quán với `TC-SEC-003`)
- (2): `teacherId`/`schoolId` trong body **bị bỏ qua**; `servedBy` ghi = `JWT.userId` (`U-0004`), **không** phải `U-0009`
- (3): trả rỗng — HS không thuộc lớp chủ nhiệm

**Expected Result (FAIL):**
- Đọc `teacherId` từ body ⇒ giáo viên **mạo danh đồng nghiệp**: hồ sơ phát suất ghi tên người khác chịu trách nhiệm cho suất mình vừa trao. Trong điều tra ngộ độc, đây là chứng cứ sai lệch về người thực hiện

**Current Status:** 🔴 Not written

---

#### MF09-TC-SEC-007 — Ma trận 7 role × 13 nhóm endpoint khớp §16 100%

**Severity:** `HIGH`
**OWASP:** `A01:2021` · **CWE:** `CWE-862`
**Feature Under Test:** toàn bộ controller MF09
**Test File:** `web/RoleMatrixTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-33`
**Oracle Source:** `TDS` **§16 Authorization Matrix** (bảng 13 dòng × 7 cột, đọc **nguyên văn**) + **BR-007** (7 role)
**Kỹ thuật:** Data-driven (91 tổ hợp)

**Preconditions:** `FX-017`…`FX-021` — đủ 7 JWT.

**Test Steps:** `@ParameterizedTest` sinh 91 ca từ bảng §16; mỗi ca gọi endpoint tương ứng và so với ô ✅/❌.

**Expected Result (PASS):**
- **91/91** khớp: ✅ → 2xx (hoặc 404/409 nghiệp vụ, **không phải** 403); ❌ → **403 `SRV-005`**
- Riêng 3 dòng ghi `Own class` / `Own child`: ca ✅ chỉ đúng khi tài nguyên **thuộc phạm vi**; ngoài phạm vi → 403 (đã cover ở `TC-SEC-003`/`TC-SEC-005`)
- **Supplier**: ❌ toàn bộ 13 dòng

**Expected Result (FAIL):**
- Bất kỳ ô nào lệch ⇒ ma trận trong tài liệu và hành vi thật khác nhau; §16 mất giá trị làm nguồn review và người duyệt tin nhầm vào tài liệu

**Current Status:** 🔴 Not written
**Implementation Note:** bảng test đọc từ **file CSV sinh từ §16** để tránh chép tay lệch — khi §16 đổi, test đỏ ngay.

---

#### MF09-TC-SEC-008 — Role `supplier` bị chặn toàn bộ MF09

**Severity:** `MEDIUM`
**OWASP:** `A01:2021` · **CWE:** `CWE-862`
**Feature Under Test:** 15 endpoint §9.1
**Test File:** `web/RoleMatrixTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-32`
**Oracle Source:** `TDS` §16 — cột `SUPPLIER` = ❌ **toàn bộ 13 dòng** + **D6** `RoleKey.supplier` = NCC **nguyên liệu**
**Kỹ thuật:** Equivalence Partitioning

**Preconditions:** `FX-021` JWT `{role:"supplier", partnerId:"SUP-001"}`.

**Test Steps:** gọi 15 endpoint bằng JWT supplier.

**Expected Result (PASS):** **15/15** → **403** `SRV-005`; không endpoint nào lọt.

**Expected Result (FAIL):** NCC nguyên liệu đọc được sổ phát suất ⇒ dữ liệu học sinh rò ra **ngoài phạm vi tổ chức trường**, cho một đối tác thương mại.

**Current Status:** 🔴 Not written

---

#### MF09-TC-SEC-009 — BR-051: KHÔNG tồn tại sinh trắc học trẻ em trong MF09

**Severity:** `CRITICAL`
**OWASP:** `A04:2021 — Insecure Design`
**CWE:** `CWE-359 — Exposure of Private Personal Information`
**Legal:** **BR-051** + **PDPD (NĐ 13/2023/NĐ-CP)**
**Feature Under Test:** toàn bộ package `com.sfms.mf09` + schema 4 bảng MF09
**Test File:** `security/NoBiometricPolicyTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-35`
**Constraint Ref:** `TDS §17.1 C7` · `AP-M09-05` (**Reject cứng**)
**Oracle Source:** **BR-051** nguyên văn (*"**Lưu ý**: Không áp dụng xác thực sinh trắc học với học sinh (trẻ em) vì lý do bảo mật dữ liệu cá nhân (PDPD)."*) + **CF-M09-1** + `TDS` §11.4 (*"Grep toàn repo: **0 kết quả** cho `face`/`biometric`"*) + `TDS` §13 `TC-E2E-003`
**Kỹ thuật:** Error Guessing (kiểm sự **vắng mặt** — loại test hiếm nhưng bắt buộc ở đây)

**Preconditions:** ứng dụng khởi động đầy đủ.

**Test Steps:**
1. Quét `RequestMappingHandlerMapping` — lọc path thuộc MF09.
2. Quét metadata JPA của 4 entity MF09 (tên cột + kiểu).
3. Quét dependency của package `com.sfms.mf09`.

**Expected Result (PASS):**
- **0** path chứa `face`, `biometric`, `scan-face`, `faceId`
- **0** cột nào trong `student_distributions`, `serving_dishes`, `meal_consumption_records`, `distribution_error_reports` lưu dữ liệu sinh trắc (kể cả `BYTEA`/`vector` không tên rõ ràng)
- **0** import thư viện nhận diện khuôn mặt trong package
- **MSG-014 / `SRV-014`** không được **bất kỳ** endpoint MF09 nào phát ra (đối chiếu `@RestControllerAdvice` + test gọi hết 15 endpoint)

**Expected Result (FAIL = vi phạm pháp luật, không chỉ vi phạm spec):**
- Tồn tại bất kỳ endpoint/cột nào ⇒ hệ thống **thu thập sinh trắc học của trẻ em** — bị BR-051 cấm tuyệt đối và là vi phạm PDPD; **AP-M09-05 quy định Reject cứng**, không có ngoại lệ kể cả cho System Admin

**Current Status:** 🔴 Not written

---

#### MF09-TC-SEC-010 — Lách cổng kiểm thực bằng cách gọi API trực tiếp

**Severity:** `CRITICAL`
**OWASP:** `A04:2021 — Insecure Design`
**CWE:** `CWE-602 — Client-Side Enforcement of Server-Side Security`
**Feature Under Test:** `POST /{code}/serve` khi MF07 báo `blocked`
**Test File:** `security/ServingGateBypassTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-08`, `COND-36`
**Constraint Ref:** `AP-M09-04`
**Oracle Source:** **BR-094** (MF07) + **CF-M09-9** (*"FE tự chặn bằng `toast.error` (client-side)"*) + luật `semantic-audit.ts` §7 **`PHAT_SUAT_DU_KIEM_THUC_TRUOT`** + **MSG-129** → `SRV-094`, HTTP **403** + prompt §2c-3b (*"Với module có GATE nghiệp vụ enforce phía server, PHẢI có test 'gọi API trực tiếp bỏ qua UI'"*)
**Kỹ thuật:** Error Guessing (mô phỏng tấn công đúng cách mock đang hở)

**Preconditions:** `FX-004` + `FX-011(b)` + `FX-012` — ngày `2026-07-07`, `INSP-20260707-L` `overall=FAILED`.

**Test Steps (Attack Simulation):**
1. `POST /student-distributions/DSTU-0707A-001/serve` — JWT GVCN 3A **hợp lệ** (không phải tấn công quyền, mà là **bỏ qua UI**).
2. Thử `PATCH /student-distributions/DSTU-0707A-001` với body `{"status":"SERVED","servedAt":"…"}` (đường vòng qua endpoint generic).
3. Thử `POST /student-distributions/confirm` với 10 mã của ngày đó.

**Expected Result (PASS):**
- (1): **403** `SRV-094`, message = MSG-129
- (2): `PATCH` generic **không được phép** đổi `status`/`servedAt`/`servedBy` — trả **403/400**, `status` giữ `PENDING`. TDS §9.1 giữ `PATCH` *"cho các field không nhạy cảm"* ⇒ test khoá đúng danh sách field cho phép
- (3): **403** cho toàn bộ, `confirmed=0`
- Truy vấn đối soát §14.1 câu 4 (`SERVED` ∩ phiên `FAILED`) trả **0 dòng**

**Expected Result (FAIL = lỗ hổng):**
- Bất kỳ đường nào ghi được `SERVED` ⇒ **bữa ăn có bước kiểm thực trượt được phát cho học sinh**, và luật audit §7 sẽ đỏ trên dữ liệu thật. Đây chính là nhóm lỗi mà review 29/07 xếp nghiêm trọng nhất: *"gate chỉ tồn tại ở client thì gọi API trực tiếp là lách được toàn bộ luật ATTP"*

**Current Status:** 🔴 Not written

---

#### MF09-TC-SEC-011 — `serving_dishes` không có đường ghi/xoá nào qua API

**Severity:** `HIGH`
**OWASP:** `A08:2021 — Software and Data Integrity Failures`
**CWE:** `CWE-471 — Modification of Assumed-Immutable Data`
**Feature Under Test:** toàn bộ controller + `ServingDishRepository`
**Test File:** `security/AppendOnlyPolicyTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-18`
**Constraint Ref:** `TDS §17.1 C2` · `AP-M09-01`
**Oracle Source:** **ADR-M09-1** (*"Sau khi ghi: **append-only, không UPDATE, không DELETE**"*) + `TDS` §5.2 (`RULE serving_dishes_no_update/no_delete`) + `TDS` §12.2 (*"**TUYỆT ĐỐI KHÔNG** rollback bằng cách xoá `serving_dishes`"*)
**Kỹ thuật:** Error Guessing + phân tích tĩnh

**Preconditions:** `FX-002` (4 dòng `serving_dishes`).

**Test Steps (Attack Simulation):**
1. Quét `RequestMappingHandlerMapping`: có path nào cho `serving-dishes` không?
2. Phân tích `ServingDishRepository`: có method `delete*`/`update*` không?
3. `POST /{code}/revoke` rồi đếm lại số dòng.

**Expected Result (PASS):**
- **0** endpoint public thao tác trực tiếp lên `serving_dishes`
- Repository **không** khai `deleteBy*`/`@Modifying @Query update` (kiểm bằng reflection trên interface)
- Sau `revoke`: **vẫn đủ 4 dòng**

**Expected Result (FAIL):**
- Có `deleteByDistributionId` ⇒ chỉ cần một lệnh gọi nhầm trong lúc "dọn dữ liệu" là **xoá vĩnh viễn bằng chứng ai đã ăn món gì** — dữ liệu này phải giữ 5 năm (§4.2 TDS) và là đầu vào duy nhất của điều tra MF11

**Current Status:** 🔴 Not written

---

#### MF09-TC-SEC-012 — Audit đủ actor + IP; log KHÔNG lộ PII trẻ em

**Severity:** `HIGH`
**OWASP:** `A09:2021 — Security Logging and Monitoring Failures`
**CWE:** `CWE-532 — Insertion of Sensitive Information into Log File`
**Legal:** PDPD
**Feature Under Test:** audit aspect + logger của 4 thao tác ghi
**Test File:** `security/AuditLoggingTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-37`
**Oracle Source:** `TDS` §4.3 (*"Mọi `serve`/`revoke`/`record consumption`/`report error` ghi audit (actor, IP, thời điểm)"*) + `TDS` §14.2 (*"log **KHÔNG** được chứa PII trẻ em ở dạng thô"*) + PDPD
**Kỹ thuật:** Error Guessing

**Preconditions:** log appender test (`ListAppender`); thực hiện đủ 4 thao tác ghi.

**Test Steps:**
1. Gọi `/serve`, `/revoke`, `PATCH /meal-records/{code}`, `/error-report`.
2. Đọc audit store + log output.

**Expected Result (PASS):**
- **4/4** thao tác có bản ghi audit đủ: `actor` (email từ `AuditorAware` — D9), `ip`, `occurredAt` (ISO), `resourceCode`
- Log **không** chứa: họ tên HS dạng thô, danh sách allergen, `faceId`/`biometric` (grep theo `TDS` §14.2)
- Dữ liệu HS trong log chỉ ở dạng **mã** (`HS-3A-001`) — đủ để điều tra, không đủ để nhận dạng trực tiếp

**Expected Result (FAIL):**
- Log in nguyên `"Nguyễn Minh Khang — dị ứng Tôm, Cua"` ⇒ hồ sơ y tế trẻ em nằm trong log tập trung, nơi có nhiều người truy cập hơn hẳn DB, và log thường được giữ ngoài vòng kiểm soát PDPD

**Current Status:** 🔴 Not written

---

#### MF09-TC-SEC-013 — Vượt rate limit → 429 + MSG-141 ⚠️ *(thiếu mã cục bộ)*

**Severity:** `MEDIUM`
**OWASP:** `A04:2021 — Insecure Design` · **CWE:** `CWE-770 — Allocation of Resources Without Limits`
**Feature Under Test:** rate limiter trên `POST /{code}/serve` (120/min) và `GET /student-distributions` (300/min)
**Test File:** `security/RateLimitTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-38`
**Oracle Source:** `TDS` §9.1 cột **Rate Limit** + **MSG-141** nguyên văn = `"Too many requests. Please try again later."` (`error_message.md` **v1.8**, dùng chung mọi module)
**Kỹ thuật:** Boundary Value Analysis (`n = limit`, `n = limit + 1`)

**Preconditions:** `FX-017` JWT GVCN 3A; rate limiter bật ở profile test.

**Test Steps:** gửi `limit` request → assert 2xx; gửi request thứ `limit+1` → assert bị chặn.

**Expected Result (PASS):**
- Request thứ `limit` vẫn 2xx; thứ `limit+1` → **HTTP 429**, `$.error.message` = **MSG-141** nguyên văn, `$.error.details` là mảng
- ⚠️ **KHÔNG assert `$.error.code`** — `TDS` §10 **chưa có mã cục bộ cho 429** (**L25**). Đề nghị Huy/Nam cấp **`SRV-429`** theo tiền lệ MF02 (`SCH-014`). Khi có mã, bổ sung assert và gỡ ghi chú này

**Expected Result (FAIL):**
- Không có giới hạn ⇒ `/serve` gọi được không hạn chế; kết hợp với race (TC-INT-001) là đường tạo tải nặng vào đúng khung giờ ăn cao điểm

**Current Status:** 🔴 Not written — **blocked một phần** (chờ mã lỗi)

---

### 4.3. INTEGRATION TEST CASES

> `@SpringBootTest` + **Testcontainers `PostgreSQLContainer("postgres:16-alpine")`** (`FX-022`) + Flyway `V1` + `V{n}__seed_mf09.sql` + MockMvc. Timeout khởi động 120s.
> Theo **D1**, mọi DB assertion dùng `WHERE code = ?`.

---

#### MF09-TC-INT-001 — BR-130: hai request `/serve` song song → đúng 1 thành công

**Severity:** `CRITICAL`
**Feature Under Test:** `confirmServed()` + `findByIdForUpdate()` + `uq_dist_student_date_meal`
**Test File:** `integration/ConcurrentServeIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-01`, `COND-02`
**Oracle Source:** **BR-130** + `ADR-M09-3` (*"`@Transactional` + `SELECT … FOR UPDATE`… nếu lock bị bỏ qua, `UNIQUE(student_id,date,meal_type)` vẫn chặn ở tầng DB"*) + **MSG-021** + `TDS` §13 `TC-INT-001`
**Kỹ thuật:** Concurrency Testing

**Preconditions:** `FX-001` `DSTU-TD-3A-001` `PENDING`; `FX-024` clock; `CountDownLatch` đồng bộ 2 thread.

**Test Steps:**
1. 2 thread cùng `POST /api/v1/student-distributions/DSTU-TD-3A-001/serve` (cùng JWT GVCN 3A).
2. Chờ cả 2 hoàn tất; đọc DB.

**Expected Result (PASS):**
- Đúng **1** request **200** (MSG-045), đúng **1** request **409** `SRV-021`
- `SELECT count(*) FROM student_distributions WHERE student_id=(HS-3A-001) AND date=<clock> AND meal_type='LUNCH'` = **1**
- `SELECT count(*) FROM serving_dishes WHERE distribution_id=<uuid của DSTU-TD-3A-001>` = **đúng 1 bộ** (4 dòng, không phải 8)

**Expected Result (FAIL):** 2×200 hoặc 8 dòng `serving_dishes` ⇒ suất trùng vào thống kê và trục điều tra MF11 đếm gấp đôi số học sinh đã ăn món nghi nhiễm.

**Current Status:** 🔴 Not written
**Implementation Note:** phải chạy trên **Testcontainers thật** — H2/in-memory không mô phỏng đúng `SELECT … FOR UPDATE` và unique constraint dưới tải song song.

---

#### MF09-TC-INT-002 — BR-094: cổng chặn thật + truy vấn đối soát trả 0 dòng

**Severity:** `CRITICAL`
**Feature Under Test:** `POST /{code}/serve` × `InspectionGatePort` (đọc bảng `inspection_sessions` thật)
**Test File:** `integration/ServingGateIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-08`
**Oracle Source:** **BR-094** (MF07) + luật §7 **`PHAT_SUAT_DU_KIEM_THUC_TRUOT`** + `TDS` §14.1 câu 4 + **MSG-129** + `TDS` §13 `TC-INT-002`
**Kỹ thuật:** Decision Table + Constraint-based

**Preconditions:** `FX-012` seed `INSP-20260707-L` (`overall=FAILED`) và `INSP-20260706-L` (`PASSED`); `FX-004`/`FX-003` phiên phát suất tương ứng.

**Test Steps:**
1. `POST /student-distributions/DSTU-0707A-001/serve` → assert 403.
2. `POST /student-distributions/DSTU-0706A-001/serve` → assert 200.
3. Chạy truy vấn §14.1 câu 4 trên DB.

**Expected Result (PASS):**
- (1) **403** `SRV-094`, `$.error.details` chứa `INSP-20260707-L`; bản ghi `PENDING`
- (2) **200**, `serving_dishes` ≥ 1
- (3) `SELECT d.id FROM student_distributions d JOIN inspection_sessions i ON i.date=d.date AND i.meal_type=d.meal_type WHERE d.status='SERVED' AND i.overall='FAILED'` → **0 rows**

**Expected Result (FAIL):** truy vấn (3) ra ≥1 dòng ⇒ tồn tại suất đã phát trong ngày kiểm thực trượt; đây là dữ liệu phải kích hoạt runbook §12.1 (*"Tech Lead + Medical Staff"*).

**Current Status:** 🔴 Not written

---

#### MF09-TC-INT-003 — ADR-M09-1: lịch sử KHÔNG bị viết lại khi hồ sơ dị ứng đổi

**Severity:** `CRITICAL`
**Feature Under Test:** `AllergyProfileHandler` (consume `AllergyDeclarationVerified`) + `MealHistoryService`
**Test File:** `integration/FrozenHistoryIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-19`
**Oracle Source:** **ADR-M09-1** (*"HS đổi chế độ ăn sau đó thì lịch sử bị viết lại — suất đã phát tháng trước hiện ra theo hồ sơ dị ứng của hôm nay"* — chính vấn đề ADR sinh ra để chặn) + §7.2 (*"**không** đụng bản ghi đã `SERVED`"*) + `TDS` §13 `TC-INT-003`
**Kỹ thuật:** State Transition Testing (sự kiện ngoài làm đổi nguồn dẫn xuất)

**Preconditions:** `FX-002` — `DSTU-0706A-002` đã `SERVED`, có món **"Canh bí đỏ nấu tôm"**; `HS-3A-002` **chưa** có dị ứng.

**Test Steps:**
1. Phát event `AllergyDeclarationVerified(HS-3A-002, allergen="Tôm")`.
2. `GET /students/HS-3A-002/meal-history?dateFrom=2026-07-06&dateTo=2026-07-06`.
3. Đọc `serving_dishes` của bản ghi đó.
4. Kiểm phiên `PENDING` tương lai của cùng HS.

**Expected Result (PASS):**
- (2) **vẫn** hiển thị `"Canh bí đỏ nấu tôm"` — không thay bằng suất riêng hồi tố
- (3) 4 dòng `serving_dishes` **không đổi một byte nào** (so `created_at` + nội dung trước/sau)
- (4) phiên `PENDING` tương lai **được** tính lại suất riêng (nhánh dương của handler)

**Expected Result (FAIL):** lịch sử đổi theo hồ sơ mới ⇒ khi điều tra sự cố, hệ thống báo em đó **chưa từng ăn món chứa tôm** trong khi thực tế đã ăn — kết luận điều tra sai từ gốc.

**Current Status:** 🔴 Not written

---

#### MF09-TC-INT-004 — Tổng hợp ngày × lớp tính runtime, phản ánh ngay việc GV vừa phát

**Severity:** `HIGH`
**Feature Under Test:** `GET /api/v1/student-distributions/summary`
**Test File:** `integration/DistributionSummaryIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-12`
**Oracle Source:** **CF-M09-10** + `ADR-M09-2` + hằng `distributionSummaries()` + **UC-132** bước 2 + `TDS` §13 `TC-INT-005`
**Kỹ thuật:** State Transition + Contract Testing

**Preconditions:** `FX-002`+`FX-003`+`FX-005` — `2026-07-06`: 3A `planned=10, distributed=3`; 3B `planned=3, distributed=1`.

**Test Steps:**
1. `GET /student-distributions/summary?date=2026-07-06&mealType=LUNCH`.
2. `POST /confirm` 7 mã còn lại của 3A.
3. Gọi lại (1).

**Expected Result (PASS):**
- (1): item 3A `{planned:10, distributed:3, specialMeals:3, status:"IN_PROGRESS"}`; item 3B `{planned:3, distributed:1, status:"IN_PROGRESS"}`; `id` = `DST-20260706-3A` / `DST-20260706-3B`; `date` **ISO** `"2026-07-06"` (D4)
- (3): 3A → `{distributed:10, status:"COMPLETED"}` — **không cần** job/refresh nào
- Response bọc trong shape phân trang 5 field (§2b-1)

**Expected Result (FAIL):** số liệu không đổi sau bước 2 ⇒ đọc từ bảng đóng băng, tái tạo đúng lỗi mock đã bỏ 21/07/2026 — bếp nhìn số cũ để quyết định nấu bù.

**Current Status:** 🔴 Not written

---

#### MF09-TC-INT-005 — Lịch sử bữa ăn là PROJECTION; bảng `meal_history` KHÔNG tồn tại

**Severity:** `HIGH`
**Feature Under Test:** `GET /api/v1/students/{code}/meal-history`
**Test File:** `integration/MealHistoryProjectionIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-17`, `COND-20`, `COND-15`
**Constraint Ref:** `AP-M09-02` (Store derived field)
**Oracle Source:** **CF-M09-7** + `ADR-M09-2` + `TDS` §9.2 (shape response) + `TDS` §13 `TC-INT-006` + **ADR-M09-8** (`caterer` đọc từ cột snapshot)
**Kỹ thuật:** Contract Testing + phân tích schema

**Preconditions:** `FX-002` (SERVED, 4 món) + `FX-007` (`consumption`) + `FX-016` (kcal) + `FX-026` (**không** seed `MEAL_HISTORY`).

**Test Steps:**
1. `GET /students/HS-3A-002/meal-history?dateFrom=2026-07-06&dateTo=2026-07-06`.
2. `SELECT to_regclass('public.meal_history')`.
3. `verify` `CatererAssignmentPort` không được gọi.

**Expected Result (PASS):**
- `items[0].dishes` có **đúng 4** phần tử, mỗi phần tử có `dishId != null`
- `items[0].consumption` khớp `meal_consumption_records`; `items[0].kcal` = tổng kcal 4 món (runtime)
- `caterer = "Cty TNHH Suất ăn An Bình"`, `catererPartnerId = "CAT-001"` — đọc từ cột snapshot
- `date`/`servedAt` **ISO** (D4); `meal` = **`"LUNCH"`** (D6, không phải `"Bữa trưa"`)
- (2) trả **NULL** ⇒ bảng `meal_history` **không tồn tại**
- (3) **0 lời gọi** port MF04

**Expected Result (FAIL):** tồn tại bảng `meal_history` ⇒ hai nguồn số liệu cho cùng một sự thật, và mock đã chứng minh chúng **lệch nhau** (seed 26–01/07 vs 02–07/07) — đúng thứ CF-M09-7 ghi nhận.

**Current Status:** 🔴 Not written

---

#### MF09-TC-INT-006 — Hợp đồng phân trang §2b + `SEARCH_FIELDS` cố định

**Severity:** `HIGH`
**Feature Under Test:** `GET /student-distributions` × `PagedResponse.of()` + `StudentDistributionSpec`
**Test File:** `integration/PagingContractIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-22`
**Constraint Ref:** `TDS §17.1 C1` · `AP-M09-08`
**Oracle Source:** `00_Team_Work_Assignment_Plan.md` **§2b-1** (5 field), **§2b-3 bước 5** (*"search chỉ chạy trên các cột đó"*), **§2b-6** (edge cases) + **CF-M09-6** (`SEARCH_FIELDS` = `studentName`, `className`, `studentId`) + `TDS` §13 `TC-E2E-005`
**Kỹ thuật:** Boundary Value Analysis + Contract Testing

**Preconditions:** `FX-001`…`FX-005` (33 phiên).

**Test Steps:**
1. `GET ?date=2026-07-06&class=3A&pageSize=8` → assert shape.
2. `GET …&page=99` → clamp.
3. `GET ?date=2020-01-01` → tập rỗng.
4. `GET ?pageSize=0` → clamp về 1.
5. `GET ?search=Khang` · `?search=3A` · `?search=HS-3A-001` · `?search=10:47`.

**Expected Result (PASS):**
- (1): keys **đúng 5** = `["items","page","pageCount","pageSize","total"]`, **không wrapper**; `total=10`, `pageCount=2`
- (2): `page == pageCount` (không phải 99), `items` = trang cuối
- (3): **200** `{items:[], total:0, page:1, pageSize:8, pageCount:1}` — **không 404**
- (4): `pageSize == 1`
- (5): `Khang` → khớp `studentName`; `3A` → khớp `className`; `HS-3A-001` → khớp `studentId`; **`10:47` → `items` rỗng** (`servedAt` **không** thuộc `SEARCH_FIELDS`)
- Search **không** phân biệt hoa thường (`ILIKE`)

**Expected Result (FAIL):** search khớp trên `servedAt` ⇒ `OR ILIKE` chạy trên mọi cột = full table scan (§2b-3 cảnh báo), và tập kết quả nhiễu khiến giáo viên tìm nhầm học sinh.

**Current Status:** 🔴 Not written

---

#### MF09-TC-INT-007 — Bộ lọc: exact + `not:` + `dateFrom/dateTo`; KHÔNG dùng `mealPrefix`

**Severity:** `MEDIUM`
**Feature Under Test:** `StudentDistributionSpec.build()`
**Test File:** `integration/FilterContractIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-23`
**Oracle Source:** `00_Team_Work_Assignment_Plan.md` **§2b-2** (bảng query param) + **§2b-5** (`student-distributions` → cột `date`, `LocalDate`) + **CF-M09-5** (*"MF09 **không dùng** `mealPrefix`"*)
**Kỹ thuật:** Equivalence Partitioning

**Preconditions:** 33 phiên (`FX-001`…`FX-005`).

**Test Steps:** gọi lần lượt `?status=SERVED` · `?not:status=SERVED` · `?mealType=LUNCH` · `?isSpecial=yes` · `?studentId=HS-3A-001` · `?dateFrom=2026-07-06&dateTo=2026-07-07` · `?mealPrefix=Trưa`.

**Expected Result (PASS):**
- `status=SERVED` → **4** bản ghi (`DSTU-0706A-002/006/007`, `DSTU-0706B-002`); `not:status=SERVED` → **29**
- `dateFrom/dateTo` lọc trên cột `date` (`LocalDate`), bao gồm cả 2 đầu mút → **23** bản ghi (06/07: 13, 07/07: 10)
- `isSpecial=yes` lọc theo **giá trị dẫn xuất** (không cột) — trả đúng tập HS có suất riêng
- **`mealPrefix=Trưa`**: **không** làm sai tập kết quả — hoặc bị bỏ qua (trả như không lọc), hoặc **400** `SRV-001`. ⚠️ Nhánh nghiêm ngặt `@Disabled("Chờ Nam chốt CF-M09-5 — §2b-2/§2b-3 vẫn gán mealPrefix cho MF09")`
- `step`/`stepStatus` **không** được hiện thực (§2b-2 — 0 call-site)

**Expected Result (FAIL):** `mealPrefix` được diễn giải thành lọc exact trên một cột không tồn tại ⇒ lỗi runtime SQL hoặc **im lặng trả rỗng** — đúng loại lỗi §2b cảnh báo.

**Current Status:** 🔴 Not written — 1 nhánh `@Disabled`

---

#### MF09-TC-INT-008 — Bất biến tầng DB: 5 ràng buộc phải chặn khi service bị bỏ qua

**Severity:** `CRITICAL`
**Feature Under Test:** `uq_dist_student_date_meal` · `ck_dist_served_at` · `ck_dist_served_same_day` · `ck_sdish_substitute` · `ck_mcr_recorded`
**Test File:** `integration/DbInvariantIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-01`, `COND-03`, `COND-04`, `COND-13`
**Oracle Source:** `TDS` §5.2 (*"Service vẫn kiểm trước để trả đúng mã MSG, nhưng **DB là chốt chặn cuối**"*) + `TDS` §11.3 (*"Nếu seed vi phạm → migration fail (**đó là mục đích**)"*) + luật audit §6
**Kỹ thuật:** Constraint-based (DB) — chạy **SQL trực tiếp**, không qua service

**Preconditions:** DB Testcontainers đã migrate.

**Test Steps:** 5 lệnh `INSERT`/`UPDATE` cố ý vi phạm, mỗi lệnh 1 assertion.

**Expected Result (PASS):**

| # | Lệnh vi phạm | Ràng buộc chặn |
|---|---|---|
| 1 | `INSERT` bản ghi thứ 2 cho cùng `(student_id, date, meal_type)` | `uq_dist_student_date_meal` — **BR-130** |
| 2 | `UPDATE … SET status='SERVED', served_at=NULL` | `ck_dist_served_at` |
| 3 | `UPDATE … SET served_at = date + interval '2 day'` | `ck_dist_served_same_day` — audit `PHAT_SAI_NGAY` |
| 4 | `INSERT serving_dishes(is_substitute=TRUE, substituted_for_dish_name=NULL)` | `ck_sdish_substitute` — **BR-065** |
| 5 | `UPDATE meal_consumption_records SET consumption='FULLY', recorded_at=NULL` | `ck_mcr_recorded` |

- Cả 5 ném `DataIntegrityViolationException`; **0 dòng** bị ghi

**Expected Result (FAIL):** thiếu ràng buộc nào ⇒ một script sửa dữ liệu tay (hoặc migration của module khác) có thể tạo trạng thái mà service **không bao giờ tạo được** — và những dòng đó chỉ lộ ra khi audit chạy, thường là sau sự cố.

**Current Status:** 🔴 Not written

---

#### MF09-TC-INT-009 — `serving_dishes` append-only ở tầng DB (RULE)

**Severity:** `CRITICAL`
**Feature Under Test:** `RULE serving_dishes_no_update` / `serving_dishes_no_delete`
**Test File:** `integration/AppendOnlyIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-18`
**Constraint Ref:** `AP-M09-01`
**Oracle Source:** `TDS` §5.2 (*"chặn UPDATE/DELETE ở tầng DB, **không trông vào kỷ luật code**"*) + `ADR-M09-1` + `TDS` §13 `TC-E2E-004`
**Kỹ thuật:** Constraint-based (DB)

**Preconditions:** `FX-002` — 4 dòng `serving_dishes`.

**Test Steps:**
1. `UPDATE serving_dishes SET dish_name='X' WHERE distribution_id=<uuid>`.
2. `DELETE FROM serving_dishes WHERE distribution_id=<uuid>`.
3. `POST /{code}/revoke` (GVCN, trong ngày) rồi đếm lại.

**Expected Result (PASS):**
- (1): **0 dòng bị thay đổi**, `dish_name` giữ nguyên (RULE `DO INSTEAD NOTHING` — không ném lỗi nhưng **không** ghi)
- (2): **0 dòng bị xoá**, vẫn đủ 4
- (3): `status → PENDING`, `served_at = NULL`, **4 dòng vẫn còn**; audit có `MealDistributionRevoked` kèm actor

**Expected Result (FAIL):** `UPDATE` thành công ⇒ `serving_dishes` không còn là bằng chứng — ai có quyền DB đều sửa được lịch sử món đã trao mà không để lại vết.

**Current Status:** 🔴 Not written
**Implementation Note:** `RULE … DO INSTEAD NOTHING` **im lặng** — test phải assert bằng cách **đọc lại dữ liệu**, không dựa vào exception.

---

#### MF09-TC-INT-010 — D1: JSON `id` là `code`, PK là UUID, path param dùng `code`

**Severity:** `HIGH`
**Feature Under Test:** 4 entity MF09 + `@JsonProperty("id")`
**Test File:** `integration/IdentifierContractIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-24`
**Oracle Source:** **D1** (`00_Common_Conventions.md`) + **L1**
**Kỹ thuật:** Contract Testing

**Preconditions:** seed MF09 đầy đủ.

**Test Steps:**
1. `GET /student-distributions?pageSize=1` → soi JSON.
2. `information_schema`: kiểu cột `id` + tồn tại cột `code` + `UNIQUE(school_id, code)` cho **cả 4 bảng**.
3. `GET /student-distributions/DSTU-0706A-002` (path bằng `code`).
4. Grep toàn response body tìm chuỗi khuôn UUID.

**Expected Result (PASS):**
- (1): `items[0].id == "DSTU-0706A-002"` (mã nghiệp vụ); khóa ngoại `studentId="HS-3A-001"`, `catererPartnerId="CAT-001"`, `dishId="com-trang"` đều là **code**
- (2): `id` kiểu **`uuid`**; 4/4 bảng có `code VARCHAR(32) NOT NULL` + unique `(school_id, code)`
- (3): **200** — path param là `code`
- (4): **0** chuỗi UUID trong response

**Expected Result (FAIL):** trả UUID ⇒ FE điều hướng bằng `/teacher/distribution/<uuid>` trong khi mọi màn khác dùng mã nghiệp vụ; và `openapi-typescript` sinh type sai theo (D8 §Vấn đề).

**Current Status:** 🔴 Not written

---

#### MF09-TC-INT-011 — D4: mọi field ngày/giờ trong JSON là ISO-8601

**Severity:** `HIGH`
**Feature Under Test:** mapper MF09 (`DistributionResponse`, `MealRecordResponse`, `MealHistoryEntryResponse`, `DistributionSummaryResponse`)
**Test File:** `integration/DateFormatContractIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-25`
**Constraint Ref:** ⚠️ **ghi đè** `TDS §17.1 C8` + `ADR-M09-5` + `AP-M09-08` (xem **L3**)
**Oracle Source:** **D4** (*"Ngày `"2026-07-29"` / Ngày giờ `"2026-07-29T02:15:00Z"`"*; ghi đè ADR-M11-5 và *"MF08, MF09, MF11 phải **bỏ `@JsonFormat`**"*)
**Kỹ thuật:** Contract Testing + Error Guessing (parse đầu vào khoan dung)

**Preconditions:** `FX-002` (`servedAt`), `FX-007` (`recordedAt`).

**Test Steps:**
1. `GET /student-distributions/DSTU-0706A-002` → regex ISO cho `date`, `servedAt`.
2. `GET /meal-records/…` → `date`, `recordedAt`.
3. `GET /students/HS-3A-002/meal-history` → `date`, `servedAt`.
4. `GET /student-distributions/summary` → `date`.
5. Gửi `?dateFrom=06/07/2026` (kiểu mock cũ) → assert **vẫn parse được**.
6. Grep mã nguồn: `@JsonFormat` trong package `com.sfms.mf09`.

**Expected Result (PASS):**
- (1)–(4): `date` khớp `^\d{4}-\d{2}-\d{2}$`; `servedAt`/`recordedAt` khớp ISO-8601 instant (`2026-07-06T03:47:00Z`)
- (5): backend **parse cả 2 dạng khi NHẬN** nhưng **TRẢ VỀ ISO** (khoan dung đầu vào vì mock đang gửi `dd/MM/yyyy` — D4 §"Không sửa seed")
- (6): **0 kết quả** `@JsonFormat`

**Expected Result (FAIL):** trả `"10:53 06/07/2026"` ⇒ vi phạm D4 đã Approved; FE không parse được bằng `Date`/`Intl` chuẩn và mọi so sánh/sắp xếp ngày phải viết tay — đúng lý do D4 tồn tại.

**Current Status:** 🔴 Not written
**Implementation Note:** ⚠️ TC này **cố tình FAIL** nếu người hiện thực tuân theo `AP-M09-08` của TDS. Đó là mục đích: **AP-M09-08 phải được sửa** theo D4 (L3), không phải test nới ra.

---

#### MF09-TC-INT-012 — D6: enum SCREAMING_SNAKE trong JSON và DB

**Severity:** `HIGH`
**Feature Under Test:** `common/enums/MealType` · `ConsumptionStatus` · `AllergySeverity` · `DistributionStatus`
**Test File:** `integration/EnumContractIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-26`
**Oracle Source:** **D6** bảng 8 enum (`MealType`, `ConsumptionStatus`, `AllergySeverity`) + **L4**
**Kỹ thuật:** Data-driven

**Preconditions:** seed đầy đủ; `FX-009` (severity `SEVERE`).

**Test Steps:**
1. `GET /student-distributions` → `mealType`, `status`.
2. `GET /meal-records` → `consumption`.
3. `GET /allergy-alerts` → `severity`.
4. `GET /students/{code}/meal-history` → `meal`.
5. `SELECT DISTINCT meal_type, status, consumption` trên DB.

**Expected Result (PASS):**
- `mealType`/`meal` ∈ `{BREAKFAST, LUNCH, SNACK}` — **không** `"Bữa trưa"`/`"Bữa chiều"`
- `consumption` ∈ **4** giá trị `{PENDING, FULLY, PARTIALLY, NOT_CONSUMED}`
- `severity` ∈ `{MILD, MODERATE, SEVERE}` — **không** `"Severe"`, không `"Nặng"` (ghi đè ghi chú §9.2 của TDS — L4)
- `status` ∈ `{PENDING, SERVED}`; `summary.status` ∈ `{PENDING, IN_PROGRESS, COMPLETED}`
- DB lưu **cùng** chuỗi SCREAMING_SNAKE (không lưu nhãn)

**Expected Result (FAIL):** `severity: "Severe"` ⇒ FE so sánh `=== "SEVERE"` để tô đỏ sẽ **không khớp** ⇒ **mất cảnh báo dị ứng nặng** — đúng hậu quả D6 nêu: *"sai enum = mất cảnh báo dị ứng nặng"*.

**Current Status:** 🔴 Not written

---

#### MF09-TC-INT-013 — `confirm` batch 50 HS trong 1 transaction

**Severity:** `HIGH`
**Feature Under Test:** `POST /student-distributions/confirm`
**Test File:** `integration/BatchConfirmIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-16`
**Oracle Source:** `TDS` §4.1 (SLA *"batch 50 HS, 1 transaction < 1.5s"*) + `TDS` §9.2 + **MSG-045**
**Kỹ thuật:** Boundary + Concurrency

**Preconditions:** lớp mở rộng 50 phiên `PENDING` (sinh từ `FX-001` nhân bản, cùng lớp 3A).

**Test Steps:**
1. `POST /confirm` với 50 mã, `atomic=true` → đo thời gian.
2. Chèn 1 mã đã `SERVED` vào giữa danh sách → gọi lại.

**Expected Result (PASS):**
- (1): **200**, `confirmed=50`; DB có **50** bản ghi `SERVED` + **50 bộ** `serving_dishes`; p95 **< 1.5s**
- (2): **rollback toàn bộ** — `SELECT count(*) WHERE status='SERVED'` **không đổi** so với trước lời gọi
- Đúng **1** transaction (kiểm bằng `TransactionSynchronizationManager` hoặc đếm connection)

**Expected Result (FAIL):** ghi từng dòng ngoài transaction ⇒ lỗi giữa chừng để lớp ở trạng thái nửa vời và giáo viên phải dò tay xem còn thiếu em nào giữa giờ ăn.

**Current Status:** 🔴 Not written

---

#### MF09-TC-INT-014 — D9: entity kế thừa `BaseEntity`, không tự khai `created_by`/`updated_by`

**Severity:** `MEDIUM`
**Feature Under Test:** 4 entity MF09 × `common/domain/BaseEntity` + `AuditorAware`
**Test File:** `integration/BaseEntityAuditIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-28`
**Oracle Source:** **D9** (*"`created_by` và `updated_by` là tên cột **reserved**… module con **không tự set**"*) + **L7**
**Kỹ thuật:** Phân tích tĩnh + Contract Testing

**Preconditions:** JWT `FX-017` (email `trangnt@…`).

**Test Steps:**
1. Reflection: 4 entity `extends BaseEntity`, **không** khai lại field `createdBy`/`updatedBy`.
2. `POST /{code}/serve` → đọc DB.
3. `information_schema`: kiểu + độ dài cột `created_by`/`updated_by`.

**Expected Result (PASS):**
- (1): 4/4 kế thừa, 0 khai trùng
- (2): `created_by`/`updated_by` = **email actor**, do `AuditorAware` điền — service **không** set tay
- (3): `VARCHAR(180)` đúng khuôn `BaseEntity`
- Cột nghiệp vụ `served_by`/`recorded_by`/`reported_by` **vẫn là UUID** và **khác** cột audit (2 câu trả lời khác nhau cho "ai làm", không lẫn)

**Expected Result (FAIL):** module tự khai `created_by UUID` ⇒ Hibernate có 2 định nghĩa cho cùng 1 cột (D9 §Vấn đề, ca MF06) — vỡ mapping lúc khởi động, hoặc tệ hơn là chạy được nhưng **lưu sai người**.

**Current Status:** 🔴 Not written
**Implementation Note:** khuyến nghị đổi 3 cột nghiệp vụ sang `served_by_user_id`/`recorded_by_user_id`/`reported_by_user_id` theo khuôn D9 — **chờ Mạnh chốt**, test **không** khoá vào tên cột.

---

#### MF09-TC-INT-015 — BR-133: job tóm tắt idempotent theo `(student_id, date)`

**Severity:** `MEDIUM`
**Feature Under Test:** `DailyParentSummaryJob` → event `DailyMealSummaryCompiled`
**Test File:** `integration/DailySummaryJobIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-29`
**Oracle Source:** **BR-133** (*"Hệ thống **tự động biên soạn** thực đơn đã phục vụ và cấu trúc dinh dưỡng, gửi **thông báo tóm tắt hàng ngày** đến phụ huynh."*) + `ADR-M09-7` (*"Job **idempotent theo `(student_id, date)`** — chạy lại không gửi trùng"*) + `TDS` §7.3 payload
**Kỹ thuật:** State Transition + Error Guessing (chạy lại job)

**Preconditions:** `FX-025` fake event bus; 1 ngày có 10 phiên `SERVED` + 10 `meal_consumption_records`.

**Test Steps:**
1. Chạy job → đếm event.
2. Chạy **lại** job cùng ngày → đếm event.
3. Soi payload 1 event.

**Expected Result (PASS):**
- (1): **10** event `DailyMealSummaryCompiled` (1/HS)
- (2): **0** event mới — tổng vẫn 10
- (3): payload đủ `studentId`, `date` (**ISO**), `servedDishNames[]`, `totalKcal` (runtime), `consumption` (enum D6), `specialMealNote` (`Optional`, rỗng nếu không có suất riêng)
- MF09 **không** gọi SMTP/push trực tiếp (ADR-M09-7 — việc của MF10)

**Expected Result (FAIL):** job chạy lại gửi trùng ⇒ phụ huynh nhận 2 thông báo cho cùng bữa; và trong sự cố phải chạy lại job để dựng lại số liệu thì spam toàn trường.

**Current Status:** 🔴 Not written

---

#### MF09-TC-INT-016 — Port ĐỦ 6 luật `semantic-audit.ts` sang assertion DB

**Severity:** `CRITICAL`
**Feature Under Test:** toàn bộ dữ liệu MF09 sau khi chạy `TC-E2E-001`
**Test File:** `integration/SemanticAuditRulesIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-30`
**Oracle Source:** `semantic-audit.ts` **§6 + §7** — tên luật **nguyên văn từ script** (không lấy từ TDS §13, xem **L26**) + `TDS` §14.1 (7 truy vấn đối soát)
**Kỹ thuật:** Data-driven (parameterized 6 luật) + Constraint-based

**Preconditions:** DB sau khi chạy trọn `TC-E2E-001` + seed đầy đủ.

**Test Steps:** chạy 6 truy vấn, mỗi truy vấn là 1 assertion độc lập.

**Expected Result (PASS):**

| Luật (nguyên văn script) | Truy vấn | Kỳ vọng |
|---|---|---|
| `PHAT_SAI_NGAY` | `served_at` quy đổi `Asia/Ho_Chi_Minh` ≠ `date` | **0 rows** |
| `TRANG_THAI_VS_GIO_PHAT` | (`SERVED` ∧ `served_at IS NULL`) ∨ (`≠SERVED` ∧ `served_at IS NOT NULL`) | **0 rows** |
| `PHAT_ROI_KHONG_CO_MON` | `SERVED` mà `count(serving_dishes)=0` | **0 rows** |
| `HOC_SINH_SAI_LOP` | `student_distributions.class_name ≠` lớp hiện tại của HS | **0 rows** |
| **`TEN_KHONG_KHOP_HOC_SINH`** | `student_distributions.student_name ≠ students.name` | **0 rows** — **luật TDS bỏ sót (L26)** |
| `PHAT_SUAT_DU_KIEM_THUC_TRUOT` | `SERVED` cùng ngày+bữa với phiên `overall='FAILED'` | **0 rows** |

**Expected Result (FAIL):** bất kỳ luật nào ra >0 dòng ⇒ dữ liệu thật đã ở trạng thái mà `semantic-audit.ts` gọi là lỗi; riêng `PHAT_ROI_KHONG_CO_MON` và `PHAT_SUAT_DU_KIEM_THUC_TRUOT` là **điều kiện kích hoạt rollback** §12.1.

**Current Status:** 🔴 Not written

---

#### MF09-TC-INT-017 — KHÔNG có cột dẫn xuất và KHÔNG có bảng tổng hợp

**Severity:** `HIGH`
**Feature Under Test:** schema 4 bảng MF09
**Test File:** `integration/NoDerivedColumnIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-21`, `COND-11`
**Constraint Ref:** `TDS §17.1 C3` · `AP-M09-02`
**Oracle Source:** `TDS` §14.1 câu 6 + `ADR-M09-2` + **CF-M09-8/10** + `TDS` §5.2 (*"⚠️ **Không có cột** `assigned_meal`, `special_meal`, `allergy_warning`, `is_special`, `diet_tag`"*)
**Kỹ thuật:** Phân tích schema

**Test Steps:**
1. `SELECT column_name FROM information_schema.columns WHERE table_name='student_distributions' AND column_name IN ('assigned_meal','special_meal','allergy_warning','is_special','diet_tag')`.
2. Tìm bảng tổng hợp ngày × lớp (`distributions`, `distribution_summaries`).
3. `to_regclass('public.meal_history')`.
4. Assert cột `planned_serve_time` **tồn tại, nullable, toàn bộ NULL** (L11 — chừa cho BR-135).

**Expected Result (PASS):** (1) **0 rows** · (2) **không tồn tại** · (3) **NULL** · (4) tồn tại, `is_nullable='YES'`, `count(planned_serve_time IS NOT NULL) = 0`.

**Expected Result (FAIL):** thêm cột `allergy_warning` ⇒ **AP-M09-02**; và cột đó sẽ **cũ dần** so với hồ sơ dị ứng thật — đúng lỗi seed mock tự mâu thuẫn đã phải sửa 27/07/2026.

**Current Status:** 🔴 Not written

---

#### MF09-TC-INT-018 — Trục truy xuất MF11: "học sinh nào đã ăn món X?"

**Severity:** `CRITICAL`
**Feature Under Test:** `ServingDishRepository.findServedByDishId()` + index `ix_sdish_dish`
**Test File:** `integration/TraceabilityQueryIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-31`
**Oracle Source:** `TDS` §14.1 câu 7 + `ADR-M09-1` (*"Trục điều tra MF11 nối bằng `dish_id` thật"*) + §1 Downstream Consumers + **UC-103 AF1** (cờ `conflictWithTodayMenu`, **không** mang MSG-051 — L20)
**Kỹ thuật:** Contract Testing

**Preconditions:** `FX-002` + `FX-005` — 4 bản ghi `SERVED` ngày `2026-07-06`, tất cả có món `canh-bi-do-nau-tom`; `DSTU-0706A-001` (`PENDING`, có suất thay) **không** được tính.

**Test Steps:**
1. `findServedByDishId("canh-bi-do-nau-tom", 2026-07-06, 2026-07-06)`.
2. `EXPLAIN` truy vấn.
3. `GET /allergy-alerts?date=2026-07-06&mealType=LUNCH&class=3A` → kiểm cờ.

**Expected Result (PASS):**
- (1): trả **đúng 4** bản ghi (chỉ `SERVED`); `DSTU-0706A-001` **không** có mặt vì em này nhận **suất thay** (`canh-bi-do-thit-bam`) — đây chính là giá trị của `serving_dishes`: phân biệt được ai **thực sự** ăn món nghi nhiễm
- (2): dùng `ix_sdish_dish`, **không** seq scan
- (3): `conflictWithTodayMenu = true` cho HS-3A-001; response **không** chứa mã `MSG-051` (L20)

**Expected Result (FAIL):** trả cả bản ghi `PENDING` hoặc dựa vào `assignedMeal` (chuỗi) ⇒ danh sách trẻ cần theo dõi y tế **sai cả hai chiều**: bỏ sót em đã ăn, và gọi nhầm em ăn suất thay.

**Current Status:** 🔴 Not written

---

#### MF09-TC-INT-019 — **D12**: gửi field dẫn xuất và field sai KIỂU ⇒ **DB không đổi**

> 🔴 **TC BỔ SUNG 2026-08-03 (`M09-D10`)** — `02_Module_Completion_Workflow.md` §6 D12 + §7 Bước 1 việc 3. Trước bản này **chưa phủ**.

**Severity:** `CRITICAL`
**Feature Under Test:** `PATCH /api/v1/meal-records/{code}` × `MealRecordUpdateRequest` ↔ `MealRecordResponse` · Jackson `FAIL_ON_UNKNOWN_PROPERTIES`
**Test File:** `integration/WriteContractIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-43`
**Oracle Source:** **D12** (`02_Module_Completion_Workflow.md` §6) · **ADR-M09-2** (field dẫn xuất **không có cột**) · mẫu `TestSpec-MF05` **TC-027**

**Vì sao MF09 là ca nguy hiểm của D12:** `ADR-M09-2` khai **6 field dẫn xuất** (`assignedMeal`, `specialMeal`, `allergyWarning`, `isSpecial`, `dietTag`, `dishes`) xuất hiện **trong Response** nhưng **không có cột nào** trong DB. FE có sẵn cả 6 field trong tay từ lần `GET`; form sửa gửi nguyên object lên là **kịch bản mặc định**, không phải trường hợp hiếm. Jackson bỏ qua **im lặng**: không lỗi, không cảnh báo, **trả 200** — form báo "Đã lưu" trong khi DB không đổi gì. Đúng bẫy đã xảy ra thật với `PATCH /students {"class":"4A"}` ở MF03.

Thêm **biến thể cùng tên khác KIỂU** (nguy hiểm hơn, MF05 đã dính): `dishes` ở Response là **chuỗi nhãn** (`dishesLabel`), nếu FE gửi lên `dishes: [{...}]` (mảng object) thì hai bên *trông như khớp* khi review bằng mắt.

**Preconditions:** `FX-005` — `MR-TD-3A-004` đang `consumption = PENDING`, `dishes_label = "Cơm trắng, Canh bí đỏ nấu tôm"`.

**Test Steps:**
1. Chụp snapshot toàn bộ dòng `meal_consumption_records WHERE code = 'MR-TD-3A-004'`.
2. `PATCH /api/v1/meal-records/MR-TD-3A-004` với body **hỗn hợp**:
   `{"consumption":"FULLY", "allergyWarning":"Tôm", "isSpecial":"yes", "dietTag":"CHAY", "assignedMeal":"X", "specialMeal":"Y"}` — 1 field hợp lệ + **5 field dẫn xuất**.
3. `PATCH` lần 2 với **đúng tên sai kiểu**: `{"dishes":[{"dishId":"com-trang"}]}` *(Response trả `dishes` là **chuỗi**)*.
4. `PATCH` lần 3 với **field read-only đã ghi**: `{"recordedAt":"2020-01-01T00:00:00Z", "code":"MR-HACKED-001", "id":"MR-HACKED-001"}`.
5. So snapshot sau mỗi lần.

**Expected Result (PASS):**
- (2): **chỉ** `consumption` đổi thành `FULLY` (+ `recorded_at`/`recorded_by_user_id`/`updated_at` do service và `AuditorAware` điền). **Không** cột nào khác đổi — 5 field kia **không có cột để đổi**, đó là điểm mấu chốt của ADR-M09-2
- (3): **400 SRV-001 (MSG-138)** — **không** phải 200 im lặng. `$.error.details` chỉ đích danh `dishes` sai kiểu
- (4): `recorded_at` **giữ nguyên** giá trị thật; `code` và `id` **KHÔNG đổi** (`MR-TD-3A-004`) — D1 cấm client ghi đè mã nghiệp vụ của bản ghi đã tồn tại
- (5): mọi diff snapshot khớp đúng danh sách trên, **không dòng thừa**

**Expected Result (FAIL):**
- (2) trả **200 mà không lưu gì** (hoặc lưu thiếu) ⇒ giáo viên bấm "Lưu", thấy toast xanh, và **mức tiêu thụ bữa ăn của trẻ không được ghi** — số liệu BR-133 gửi cho phụ huynh sai mà không ai biết
- (3) trả **200** ⇒ đúng biến thể "cùng tên khác kiểu" mà review bằng mắt **không bắt được**
- (4) `code` bị ghi đè ⇒ mã nghiệp vụ mất tính duy nhất, trục truy xuất MF11 gãy

**Ghi chú cho Bước 2:** cách chắc chắn nhất để (3) và (2) hành xử đúng là **Request DTO chỉ khai field sửa được** (`consumption`, `specialMealServed`) + Jackson `FAIL_ON_UNKNOWN_PROPERTIES = false` cho field dẫn xuất *(bỏ qua, đúng ý D12)* nhưng **bật kiểm kiểu** cho field có khai. Đừng dùng `Map<String,Object>` rồi merge — đó chính là `PUT` generic của mock mà **CF-M09-9** đã bỏ.

**Danh sách field READ-ONLY của MF09** *(bắt buộc ghi trong TDS §9 để FE không gửi lên — D12)*: `assignedMeal`, `specialMeal`, `allergyWarning`, `isSpecial`, `dietTag`, `dishes`(=`dishesLabel`), `servedDishes`, `caterer`, `catererPartnerId`, `name`, `class`, `servedAt`, `recordedAt`, `reportedAt`, `id`/`code`.

**Current Status:** 🔴 Not written

---

### 4.4. CONTRACT TEST CASES — OpenAPI `/v3/api-docs` (**D8**)

> Nhóm này **bắt buộc theo prompt §2c-3b** và checklist `00_Common_Conventions.md` D8. Lý do tồn tại: `/v3/api-docs` là **nguồn sinh type FE** (`review_fix_plan.md` T3.4 `openapi-typescript`) và là **cổng nghiệm thu** (`01_MVP…` §Nghiệm thu). Spec sai ⇒ lệch D1/D2/D4/D6 **quay lại qua file sinh tự động**, khó thấy hơn nhiều so với lệch tay.
> Hạ tầng: `@SpringBootTest` khởi động thật, `GET /v3/api-docs` (**phải `permitAll`** — D8 §Lưu ý hạ tầng), parse bằng `swagger-parser`, assert trên cây JSON.
> ⏳ D8 **chờ Nam approve** — nhóm này vẫn viết và vẫn chạy; nếu endpoint MF09 chưa annotate thì test **FAIL** kèm thông điệp *"thiếu annotation D8"*, **không** nới assert.

---

#### MF09-TC-CON-001 — D1/S6: không field `id` nào có `format: uuid`, không UUID trong `example`

**Severity:** `HIGH`
**Feature Under Test:** schema của mọi DTO MF09 trong `/v3/api-docs`
**Test File:** `contract/OpenApiIdentifierTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-24`, `COND-39`
**Oracle Source:** **D8 S6** (*"`example` của `id` là **mã nghiệp vụ**; không có UUID trong bất kỳ `example`/`@Schema` công khai nào"*) + **D1** + Team Plan §2c-3b lớp Contract (a)

**Test Steps:**
1. Tải spec; duyệt `components.schemas.*` thuộc MF09.
2. Lọc property tên `id` hoặc kết thúc bằng `Id`.
3. Regex UUID trên toàn bộ `example`.

**Expected Result (PASS):**
- **0** property `id`/`*Id` có `format: "uuid"`
- `example` của `id` là mã nghiệp vụ (`"DSTU-0706A-001"`, `"MR-TD-3A-004"`); của `studentId` là `"HS-3A-001"`; `catererPartnerId` là `"CAT-001"` (tiền tố `CAT-`, **không** `PTN-` — TDS §9.2 G1)
- **0** chuỗi khớp `^[0-9a-f]{8}-[0-9a-f]{4}-…$` trong toàn spec MF09

**Expected Result (FAIL):** `format: uuid` ⇒ `openapi-typescript` sinh type FE là UUID string, FE điều hướng bằng UUID, và D1 vỡ **qua đường file sinh tự động** — không ai review file đó.

**Current Status:** 🔴 Not written

---

#### MF09-TC-CON-002 — D2/S3: mọi response 4xx/5xx `$ref` tới `ErrorResponse`

**Severity:** `HIGH`
**Feature Under Test:** `@ApiResponse` của 15 endpoint MF09
**Test File:** `contract/OpenApiErrorEnvelopeTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-27`, `COND-39`
**Oracle Source:** **D8 S3** (*"Mỗi mã lỗi ở §10 của TDS có một `@ApiResponse` tương ứng, `content` trỏ **`ErrorResponse` dùng chung**"*) + **D2** + Team Plan §2c-3b lớp Contract (b)

**Test Steps:**
1. Duyệt mọi `paths./api/v1/**.responses` thuộc MF09.
2. Lọc mã 4xx/5xx; kiểm `content.application/json.schema.$ref`.
3. Đối chiếu **danh sách mã lỗi §10** với tập `@ApiResponse` khai báo.

**Expected Result (PASS):**
- **100%** response 4xx/5xx `$ref = "#/components/schemas/ErrorResponse"`
- `ErrorResponse` khai đủ 3 field `code`/`message`/`details`; `details` kiểu **array**
- Mỗi mã ở §10 áp dụng cho endpoint nào thì endpoint đó **có** `@ApiResponse` tương ứng: `/serve` phải có **403** (SRV-005 **và** SRV-094), **409** (SRV-021), **422** (SRV-022), **401** (SRV-004)
- **`SRV-014`/MSG-014 không xuất hiện** ở bất kỳ endpoint nào (L9/CF-M09-1)

**Expected Result (FAIL):** thiếu `$ref` ⇒ type lỗi sinh cho FE là `any`, interceptor `data?.error?.message` mất kiểm chứng, và mã lỗi §10 trở thành tài liệu chết.

**Current Status:** 🔴 Not written

---

#### MF09-TC-CON-003 — D6/S5: enum khai bằng kiểu thật, khớp bảng D6

**Severity:** `HIGH`
**Feature Under Test:** schema enum trong spec
**Test File:** `contract/OpenApiEnumTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-26`, `COND-39`
**Oracle Source:** **D8 S5** (*"Enum khai bằng **kiểu enum thật** ở `common/enums/` — **KHÔNG** `@Schema(allowableValues = "…")` viết tay"*) + **D6** bảng 8 enum + Team Plan §2c-3b lớp Contract (c)

**Test Steps:**
1. Trích mọi `enum:` trong schema MF09.
2. So với bảng D6.
3. Grep mã nguồn `@Schema(allowableValues`.

**Expected Result (PASS):**
- `MealType` = `[BREAKFAST, LUNCH, SNACK]` · `ConsumptionStatus` = `[PENDING, FULLY, PARTIALLY, NOT_CONSUMED]` (**đủ 4**) · `AllergySeverity` = `[MILD, MODERATE, SEVERE]`
- Enum module-local (`DistributionStatus` = `[PENDING, SERVED]`, `SummaryStatus` = `[PENDING, IN_PROGRESS, COMPLETED]`) cũng khai bằng **kiểu enum thật**, SCREAMING_SNAKE
- **0** kết quả `@Schema(allowableValues` trong package `com.sfms.mf09` (S5)
- **0** giá trị nhãn tiếng Việt trong bất kỳ `enum:` nào

**Expected Result (FAIL):** khai `allowableValues` tay ⇒ danh sách trong spec và enum Java **trôi xa nhau** theo thời gian; chính là cơ chế đã sinh ra 13+ bộ giá trị cho 8 khái niệm mà D6 phải đi dọn.

**Current Status:** 🔴 Not written

---

#### MF09-TC-CON-004 — D4/S4 + S7 + S1/S2/S8: ngày ISO, `PagedResponse`, `@Tag`/`@Operation`/security

**Severity:** `MEDIUM`
**Feature Under Test:** metadata OpenAPI của 15 endpoint MF09
**Test File:** `contract/OpenApiMetadataTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-25`, `COND-39`
**Oracle Source:** **D8 S1** (`@Tag` đúng tên module §2c-0) · **S2** (*"`description` **PHẢI chứa** UC ID + BR ID"*) · **S4** (field ngày `format: date`/`date-time`) · **S7** (`GET` danh sách trả `PagedResponse<T>`) · **S8** (`@SecurityScheme` bearer JWT) + Team Plan §2c-3b lớp Contract (d)

**Test Steps:** duyệt spec, kiểm 5 nhóm luật.

**Expected Result (PASS):**
- **S1**: mọi controller MF09 có `@Tag(name = "MF09 — Meal Serving & Distribution")` (đúng `{Tên module}` bảng §2c-0)
- **S2**: **15/15** endpoint có `description` chứa ≥1 `UC-\d{3}` **và** ≥1 `BR-\d{3}` — VD `/serve` chứa `UC-016` + `BR-120, BR-130` (và `BR-094` ghi rõ *(MF07)*)
- **S4**: `date` → `format: "date"`, `example: "2026-07-06"`; `servedAt`/`recordedAt` → `format: "date-time"`, example ISO
- **S7**: **7 GET danh sách** khai response là `PagedResponse<T>` (schema generic Nam khai 1 lần), đủ 5 field
- **S8**: có `securitySchemes.bearerAuth` (HTTP/bearer/JWT); **không** endpoint MF09 nào khai `@SecurityRequirements` rỗng (không có endpoint public trong module này)

**Expected Result (FAIL):** `description` thiếu UC/BR ⇒ truy vết chỉ sống trong file `.md`; sang runtime doc — thứ mà người ngoài nhóm thực sự đọc — thì mất dấu.

**Current Status:** 🔴 Not written

---

#### MF09-TC-CON-005 — Phạm vi module: đúng 14 endpoint, không có endpoint của module khác, không có sinh trắc học

**Severity:** `HIGH`
**Feature Under Test:** `paths` của spec, lọc theo `@Tag` MF09
**Test File:** `contract/OpenApiScopeTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-35`, `COND-40`, `COND-41`
**Oracle Source:** `TDS` v2.0 §9.1 (bảng **14 dòng** sau `M09-D06`) + **`M09-D06`** (`/special-meals` thuộc MF03) + **CF-M09-13** (UC-141 thuộc MF05) + **CF-M09-14** (2 endpoint `/meal-service/*` thuộc MF05/MF07) + **BR-051** + **L5** (D5 không áp dụng)

**Test Steps:**
1. Lọc path gắn `@Tag` MF09 → so với **14** dòng §9.1.
2. Tìm path chứa `face|biometric|scan-face`.
3. Tìm `/meal-service/overview`, `/meal-service/portions`, `/special-meal-arrangements`.
4. Duyệt `requestBody` tìm field `id`.
5. **[MỚI `M09-D06`]** Đếm số controller khai `@RequestMapping("/api/v1/special-meals")` trong **toàn bộ** `src/main/java` (reflection trên `RequestMappingHandlerMapping`).
6. **[MỚI `M09-D01`]** Duyệt **mọi** path param của MF09 và **mọi** field tên `id`/`*Id` trong schema response — assert **không** cái nào khai `format: uuid`.

**Expected Result (PASS):**
- (1): **đúng 14** path/method, không thừa không thiếu; mọi path bắt đầu `/api/v1` (**D3**)
- (2): **0** kết quả (BR-051 — L9)
- (3): **0** kết quả — 3 endpoint này **không thuộc MF09** (CF-M09-13/14)
- (4): **0** `requestBody` có field `id` ⇒ **D5 không áp dụng cho MF09** (L5). Nếu về sau xuất hiện, endpoint đó **phải** khai thêm `409` trỏ `ErrorResponse` với MSG-139
- (5): **đúng 1** controller — và nó thuộc package **`com.sfms.mf03`**, không phải `com.sfms.mf09` (`M09-D06`)
- (6): **0** field `format: uuid` (**D1** / `M09-D01`) — `id` là mã nghiệp vụ `DSTU-0706A-001`, `studentId` là `HS-3A-001`

**Expected Result (FAIL):**
- Còn `/meal-service/portions` ⇒ **hai module cùng tính một con số** (MF05 `cookSheet()` và MF09) — khi công thức suất nấu đổi, hai nơi lệch nhau và bếp nấu theo con số sai
- Có path `face` ⇒ vi phạm BR-051/PDPD, **Reject cứng** (AP-M09-05)
- **(5) ra 2 controller ⇒ ứng dụng KHÔNG KHỞI ĐỘNG ĐƯỢC** (`IllegalStateException: Ambiguous mapping`). Đây là lý do `M09-D06` tồn tại; test này là lưới an toàn nếu ai đó vô tình thêm lại
- **(6) ra >0 ⇒ UUID rò ra JSON** (AP-M09-09) — FE sinh type từ `/v3/api-docs` sẽ khoá vào UUID và Bước 3 nối FE sẽ gãy

**Current Status:** 🔴 Not written

---

#### MF09-TC-CON-006 — **D11**: `GET /student-distributions` đổi shape theo vai trò ⇒ phải khai `oneOf`

> 🔴 **TC BỔ SUNG 2026-08-03 (`M09-D10`)** — `02_Module_Completion_Workflow.md` §6 D11 + §7 Bước 1 việc 3.
> Trước bản này, `grep -n "oneOf" TestSpec-MF09*` ra **0 kết quả** ⇒ **chưa phủ**. Đúng lớp lỗi mà §6 ghi: *"cả 13 TestSpec viết TRƯỚC khi D11–D13 tồn tại"*.

**Severity:** `CRITICAL`
**Feature Under Test:** `GET /api/v1/student-distributions` × `PagedResponseDistributionResponse` × `/v3/api-docs`
**Test File:** `contract/RoleShapeContractIT.java` *(IT — cần đăng nhập thật, không `@WithMockUser`)*
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-42`
**Oracle Source:** **D11** (`02_Module_Completion_Workflow.md` §6) · **S7** (tên schema giữ tiền tố `PagedResponse` — `OpenApiMetaContractIT`) · **ADR-M09-6** (cách ly phạm vi đọc, PII trẻ em) · **BR-120/BR-007** · mẫu `StudentDtos.PagedResponseStudentResponse` của MF03

**Vì sao MF09 **CÓ** D11 thật (khác MF06/MF07 kết luận "không áp dụng"):**
`ADR-M09-6` cho **Service Caterers** đọc `student-distributions` để biết số suất phải nấu, nhưng đây là **PII trẻ em** (`name`, `class`, `allergyWarning`, `specialDiet`). Khác biệt giữa vai trò **KHÔNG chỉ là tập DÒNG** (`WHERE`) mà là **tập FIELD** — đúng ranh giới mà §6 vạch: *"lọc dòng không cần `oneOf`, lọc field thì cần"*. Đây cũng là lần **thứ ba** hệ thống gặp pattern này (sau `GET /schools` và `GET /students`), và `GET /students` là **cùng một loại dữ liệu**, đã tốn hai phiên curl để tìm ra.

**Test Steps:**
1. Đăng nhập **tài khoản seed thật** role `teacher` (GVCN lớp 3A, `V900__test_fixtures.sql`, mật khẩu `P@ssw0rd123`) → `GET /api/v1/student-distributions?date=2026-07-06&class=3A`.
2. Đăng nhập **tài khoản seed thật** role `service-caterers` → gọi **cùng** URL.
3. Đọc `/v3/api-docs`, lấy schema response của endpoint đó.
4. Lần theo `items` của schema.

**Expected Result (PASS):**
- (1) Teacher thấy **đủ** field: `id`, `name`, `class`, `studentId`, `status`, `servedAt`, `servedDishes[]`, `allergyWarning`, `specialDiet`, `isSpecial`
- (2) Service Caterers **KHÔNG** thấy `name`, `allergyWarning`, `specialDiet` *(rút gọn còn `id`, `class`, `studentId`, `status`, `isSpecial`, `dietTag`)* — assert bằng `jsonPath().doesNotExist()`, **không** phải assert `== null`
- (3) Schema khai **`oneOf`** với ≥2 nhánh (record chỉ-để-sinh-tài-liệu, mẫu MF03)
- (4) `items` có **kiểu thật**, **không phải `{}`** — và tên schema **giữ tiền tố `PagedResponse`** (**S7**)

**Expected Result (FAIL):**
- (2) trả đủ field cho Service Caterers ⇒ **rò PII trẻ em cho nhà cung cấp suất ăn** — vi phạm ADR-M09-6 + PDPD, nặng hơn nhiều so với ca `GET /schools`
- (4) `items` ra `{}` ⇒ `npm run gen:api` sinh type rỗng, FE phải curl từng vai trò mới biết — **đúng hai phiên làm việc mà MF03 đã mất** (§1 workflow)
- ⚠️ **Nếu đặt `name=` cho `@Schema` trên `PagedResponse.java`** để "sửa" ca này ⇒ hỏng type của **cả 11 endpoint danh sách** toàn hệ thống (§5 workflow). Đừng chạm file đó.

**Current Status:** 🔴 Not written

---

### 4.5. E2E TEST CASES

> `@SpringBootTest(webEnvironment = RANDOM_PORT)` + Testcontainers + `TestRestTemplate`. Mỗi TC đi trọn một luồng nghiệp vụ, có ≥1 mắt nối sang module khác.

---

#### MF09-TC-E2E-001 — UC-016 + UC-080 happy path: phát suất cả lớp rồi ghi tiêu thụ

**Severity:** `CRITICAL`
**Feature Under Test:** `GET /student-distributions` → `POST /confirm` → `POST /meal-records/batch` → `GET /summary` → job BR-133
**Test File:** `e2e/TeacherServingFlowE2E.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-01`, `COND-03`, `COND-12`, `COND-16`, `COND-29`
**Oracle Source:** **UC-016 Normal Flow** bước 1–5 (*"Teacher confirms all distributions and marks the classroom as served… System records confirmation with timestamp (MSG-045)"*) + **UC-080 Normal Flow** bước 3–5 + **MSG-045** nguyên văn + **BR-121**, **BR-133** + `TDS` §13 `TC-E2E-001`
**Kỹ thuật:** Use Case Testing

**Preconditions:** `FX-001` (10 phiên `PENDING` hôm nay) · `FX-006` (10 `meal_records` `PENDING`) · `FX-024` clock · `FX-011(a)` gate mở · `FX-017` JWT GVCN 3A.

**Test Steps:**
1. `GET /student-distributions?date=<clock>&class=3A&mealType=LUNCH`.
2. `POST /student-distributions/confirm` 10 mã (header `X-Correlation-Id`).
3. `GET /student-distributions/summary?date=<clock>`.
4. `POST /meal-records/batch` 10 mức tiêu thụ (mix `FULLY`/`PARTIALLY`/`NOT_CONSUMED`).
5. Chạy `DailyParentSummaryJob`.

**Expected Result (PASS):**
- (1): **200**, `total=10`, tất cả `status="PENDING"`
- (2): **200**, `confirmed=10`, `failed=0`, message = **MSG-045** với `[Class Name]="3A"`; mỗi bản ghi `servedDishes ≥ 1`, `servedAt` **ISO** cùng ngày `date`
- (3): 3A `{planned:10, distributed:10, status:"COMPLETED"}`
- (4): **200**, mọi record `recordedAt != null`, `recordedBy = U-0004`
- (5): **10** event `DailyMealSummaryCompiled`, mỗi event có `totalKcal > 0` và `consumption` khớp bước 4
- Chạy lại 6 truy vấn `TC-INT-016` → **0 vi phạm**

**Expected Result (FAIL):** bất kỳ bước nào đứt ⇒ luồng hằng ngày của giáo viên không dùng được; đây là luồng **chạy 2 lần/ngày × 30 lớp**, hỏng là toàn trường quay lại giấy.

**Current Status:** 🔴 Not written

---

#### MF09-TC-E2E-002 — Cách ly PII trẻ em theo vai (BR-120, BR-201, ADR-M09-6)

**Severity:** `CRITICAL`
**Legal:** **PDPD (NĐ 13/2023/NĐ-CP)**
**Feature Under Test:** toàn bộ endpoint đọc × 4 vai (Teacher, Parent, Caterers, Medical)
**Test File:** `e2e/PiiScopeE2E.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-07`, `COND-14`, `COND-33`
**Oracle Source:** `ADR-M09-6` (3 tầng guard) + **BR-120**, **BR-201**, **BR-007** + `TDS` §16 + `TDS` §13 `TC-E2E-002` + PDPD

**Preconditions:** `FX-014` Parent P1 (`HS-3A-001`, `HS-1B-007`) · `FX-017`/`FX-018` 2 GVCN · `FX-019` Caterers · `FX-021` Medical.

**Test Steps:**
1. Parent P1 đọc `HS-1B-007` (con) và `HS-3B-002` (không phải con).
2. GVCN 3A đọc sổ lớp 3A và lớp 3B.
3. Caterers đọc `/student-distributions` (school) và gọi `/serve`.
4. Medical đọc `/allergy-alerts` (school).

**Expected Result (PASS):**
- (1): con → **200**, mọi item `studentId="HS-1B-007"`; không phải con → **403** `SRV-005`
- (2): lớp mình → 200 đủ 10 bản ghi; lớp khác → `items:[]` **hoặc** 403 (nhất quán `TC-SEC-003`)
- (3): đọc → 200 phạm vi `SCH-001`; ghi → **403** `SRV-005`
- (4): 200 — Medical thấy toàn trường (§16)
- **Không** response nào ở nhánh âm chứa tên/allergen của HS ngoài phạm vi

**Expected Result (FAIL):** rò một trường hợp ⇒ kích hoạt runbook §12.3 — *"Trong 30 phút — nếu lộ PII học sinh → **DPO**, Email (bắt buộc, PDPD)"*.

**Current Status:** 🔴 Not written

---

#### MF09-TC-E2E-003 — Trục xương sống: kiểm thực quyết định phát suất, phát suất nuôi điều tra

**Severity:** `CRITICAL`
**Feature Under Test:** MF05 (menu) → MF09 (phiên + phát) → MF07 (gate) → MF11 (truy xuất theo `dishId`)
**Test File:** `e2e/BackboneE2E.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-08`, `COND-31`
**Oracle Source:** `00_Team_Work_Assignment_Plan.md` §3 (*"Trục xương sống (P1) phải thông: … → **phân suất & thống kê theo món** → tạo sự cố → điều tra: sự cố → HS → món → lô → NCC → mẫu — nối bằng **ID**, không phải tên"*) + **BR-094** (MF07) + `ADR-M09-1` + ràng buộc **C3**

**Preconditions:** seed đủ MF05/MF07/MF09; `FX-012` (1 phiên PASSED `2026-07-06`, 1 phiên FAILED `2026-07-07`).

**Test Steps:**
1. Event `MenuPublished(MENU-2026-27)` → assert phiên `PENDING` được sinh, có `menuId` + snapshot caterer.
2. Ngày `2026-07-06` (gate mở): `POST /confirm` → `SERVED`.
3. Ngày `2026-07-07` (gate `FAILED`): `POST /confirm` → **403**, `confirmed=0`.
4. Truy xuất: `findServedByDishId("canh-bi-do-nau-tom", 06/07, 07/07)`.
5. Đọc lịch sử phụ huynh cho HS trong tập (4).

**Expected Result (PASS):**
- (1): số phiên = số HS của khối × ngày × bữa; `menu_id` trỏ `MENU-2026-27` (**C3**); `caterer_partner_id="CAT-001"`
- (2): `SERVED` + `serving_dishes` đóng băng
- (3): **0** bản ghi `SERVED` ngày 07/07 — cổng BR-094 chặn **toàn bộ lớp**
- (4): trả **đúng** tập HS đã ăn món đó **ngày 06/07**, nối bằng `dish_id`, **không** có bản ghi ngày 07/07
- (5): lịch sử của những HS đó khớp `serving_dishes` (cùng danh sách món, cùng `dishId`)

**Expected Result (FAIL):** trục đứt ở bất kỳ mắt nào ⇒ khi có ngộ độc, câu hỏi *"em nào đã ăn món nghi nhiễm?"* **không trả lời được** — đây là lý do tồn tại của cả module.

**Current Status:** 🔴 Not written

---

#### MF09-TC-E2E-004 — Hợp đồng FE↔BE: đổi `baseURL` là các màn MF09 chạy

**Severity:** `HIGH`
**Feature Under Test:** shape JSON của 15 endpoint vs 9 màn FE
**Test File:** `e2e/FeContractE2E.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-22`, `COND-24`, `COND-25`
**Constraint Ref:** `TDS §17.1 C8` *(phần tên field — phần định dạng ngày do **D4** ghi đè, L3)*
**Oracle Source:** `00_Team_Work_Assignment_Plan.md` §7 (*"Minh xác nhận §9 API Spec: đổi baseURL FE sang backend thật, các màn vẫn chạy"*) + `TDS` §14.3 (lệnh `jq` đối chiếu key) + **ADR-M09-5** (giữ tên field mock) + **D3** (`baseURL = /api/v1`) + **D4**

**Preconditions:** seed đầy đủ; FE trỏ `baseURL: "/api/v1"`.

**Test Steps:**
1. `GET /student-distributions?pageSize=1` → so tập key với `StudentDistribution` của `mock-data.ts`.
2. `GET /meal-records?pageSize=1` → so với `TeacherMealRecord`.
3. `GET /students/{code}/meal-history` → so với `MealHistoryEntry`.
4. Kiểm tra riêng nhóm field ngày.

**Expected Result (PASS):**
- (1): keys chứa `id`, `date`, `mealType`, `studentId`, **`name`**, **`class`**, `assignedMeal`, `specialMeal`, `dietTag`, `allergens`, `allergyWarning`, `isSpecial`, `status`, `servedAt`, `servedDishes` — **giữ `name`/`class`**, không đổi thành `studentName`/`className` (**AP-M09-08** phần tên field vẫn hiệu lực)
- (2): keys chứa `student`, `dishes`, `specialDiet`, **`specialMealServed`** (bổ sung theo review 29/07 mục G3), `consumption`, `recordedAt`
- (3): keys chứa `caterer`, **`catererPartnerId`** (**không** `catererSupplierId` — sửa G1), `dishes[].dishId`, `kcal`, `feedbackOpen`
- (4): **mọi** field ngày là **ISO** ⇒ FE **phải** dùng `lib/format.ts` ở tầng render. ⚠️ **Đây là việc FE còn nợ (Minh)** — ghi rõ trong Exit Criteria §6

**Expected Result (FAIL):**
- Đổi `name`→`studentName` ⇒ 20+ file FE gãy im lặng (hiển thị rỗng, không lỗi)
- Trả `dd/MM/yyyy` ⇒ hết đỏ ở test này nhưng **đỏ ở `TC-INT-011`** (D4). Hai TC này **cố ý** kéo về hai hướng: chỗ nào hợp đồng FE thắng (tên field), chỗ nào quy ước chung thắng (định dạng) — ranh giới ghi ở **L3**

**Current Status:** 🔴 Not written

---

## 5. Red-Green-Refactor Tracker

> Điền `🔴 RED confirmed` **trước** khi implement (§5.4). Cột `🟢 GREEN` ghi commit hash làm test xanh.

### 5.1. Unit (16)

| TC ID | Test File | 🔴 RED confirmed | 🟢 GREEN (commit) | 🔵 REFACTOR note |
|---|---|---|---|---|
| `MF09-TC-001` | `service/ConfirmDistributionTest.java` | `[ ]` | | |
| `MF09-TC-002` | `service/ConfirmDistributionTest.java` | `[ ]` | | |
| `MF09-TC-003` | `service/PersonalizeServingTest.java` | `[ ]` | | tách `AllergyMatcher` nếu logic giao allergen phình |
| `MF09-TC-004` | `service/DistributionDetailTest.java` | `[ ]` | | |
| `MF09-TC-005` | `service/MealConsumptionTest.java` | `[ ]` | | |
| `MF09-TC-006` | `service/DistributionScopeTest.java` | `[ ]` | | gom guard vào `ScopeGuard` dùng chung 4 service |
| `MF09-TC-007` | `service/ServingGateTest.java` | `[ ]` | | |
| `MF09-TC-008` | `service/RevokeDistributionTest.java` | `[ ]` | | |
| `MF09-TC-009` | `service/DistributionErrorReportTest.java` | `[ ]` | | |
| `MF09-TC-010` | `service/MealConsumptionTest.java` | `[ ]` | | |
| `MF09-TC-011` | `service/DistributionSummaryTest.java` | `[ ]` | | |
| `MF09-TC-012` | `service/ConfirmDistributionTest.java` | `[ ]` | | |
| `MF09-TC-013` | `service/MealHistoryScopeTest.java` | `[ ]` | | |
| `MF09-TC-014` | `service/CatererSnapshotTest.java` | `[ ]` | | |
| `MF09-TC-015` | `service/BatchConfirmTest.java` | `[ ]` | | |
| `MF09-TC-016` | `service/MealHistoryProjectionTest.java` | `[ ]` | | |

### 5.2. Security (13)

| TC ID | Test File | 🔴 RED confirmed | 🟢 GREEN | Ghi chú |
|---|---|---|---|---|
| `MF09-TC-SEC-001` | `web/MealServingAuthTest.java` | `[ ]` | | parameterized 15 endpoint |
| `MF09-TC-SEC-002` | `web/DistributionAuthzTest.java` | `[ ]` | | **gọi API bỏ qua UI** |
| `MF09-TC-SEC-003` | `web/DistributionAuthzTest.java` | `[ ]` | | **gọi API bỏ qua UI** |
| `MF09-TC-SEC-004` | `web/RoleMatrixTest.java` | `[ ]` | | |
| `MF09-TC-SEC-005` | `web/MealHistoryAuthzTest.java` | `[ ]` | | IDOR |
| `MF09-TC-SEC-006` | `web/JwtScopeTest.java` | `[ ]` | | |
| `MF09-TC-SEC-007` | `web/RoleMatrixTest.java` | `[ ]` | | 91 tổ hợp, nguồn CSV sinh từ §16 |
| `MF09-TC-SEC-008` | `web/RoleMatrixTest.java` | `[ ]` | | |
| `MF09-TC-SEC-009` | `security/NoBiometricPolicyTest.java` | `[ ]` | | **BR-051 — Reject cứng** |
| `MF09-TC-SEC-010` | `security/ServingGateBypassTest.java` | `[ ]` | | **gọi API bỏ qua UI** |
| `MF09-TC-SEC-011` | `security/AppendOnlyPolicyTest.java` | `[ ]` | | |
| `MF09-TC-SEC-012` | `security/AuditLoggingTest.java` | `[ ]` | | |
| `MF09-TC-SEC-013` | `security/RateLimitTest.java` | `[ ]` | | ⚠️ blocked một phần (**L25** — thiếu mã 429) |

### 5.3. Integration (19) + Contract (6) + E2E (4)

| TC ID | Test File | 🔴 RED confirmed | 🟢 GREEN | Ghi chú |
|---|---|---|---|---|
| `MF09-TC-INT-001` | `integration/ConcurrentServeIT.java` | `[ ]` | | Testcontainers bắt buộc |
| `MF09-TC-INT-002` | `integration/ServingGateIT.java` | `[ ]` | | |
| `MF09-TC-INT-003` | `integration/FrozenHistoryIT.java` | `[ ]` | | |
| `MF09-TC-INT-004` | `integration/DistributionSummaryIT.java` | `[ ]` | | |
| `MF09-TC-INT-005` | `integration/MealHistoryProjectionIT.java` | `[ ]` | | |
| `MF09-TC-INT-006` | `integration/PagingContractIT.java` | `[ ]` | | |
| `MF09-TC-INT-007` | `integration/FilterContractIT.java` | `[ ]` | | 1 nhánh `@Disabled` (**L13**) |
| `MF09-TC-INT-008` | `integration/DbInvariantIT.java` | `[ ]` | | SQL trực tiếp |
| `MF09-TC-INT-009` | `integration/AppendOnlyIT.java` | `[ ]` | | RULE im lặng — assert bằng đọc lại |
| `MF09-TC-INT-010` | `integration/IdentifierContractIT.java` | `[ ]` | | **D1** |
| `MF09-TC-INT-011` | `integration/DateFormatContractIT.java` | `[ ]` | | **D4 — cố ý đỏ nếu theo AP-M09-08 cũ** |
| `MF09-TC-INT-012` | `integration/EnumContractIT.java` | `[ ]` | | **D6** |
| `MF09-TC-INT-013` | `integration/BatchConfirmIT.java` | `[ ]` | | SLA < 1.5s |
| `MF09-TC-INT-014` | `integration/BaseEntityAuditIT.java` | `[ ]` | | **D9** |
| `MF09-TC-INT-015` | `integration/DailySummaryJobIT.java` | `[ ]` | | |
| `MF09-TC-INT-016` | `integration/SemanticAuditRulesIT.java` | `[ ]` | | **6 luật** |
| `MF09-TC-INT-017` | `integration/NoDerivedColumnIT.java` | `[ ]` | | |
| `MF09-TC-INT-018` | `integration/TraceabilityQueryIT.java` | `[ ]` | | mắt nối MF11 |
| **`MF09-TC-INT-019`** | `integration/WriteContractIT.java` | `[ ]` | | 🆕 **D12** (`M09-D10`) — "200 mà không lưu gì" |
| `MF09-TC-CON-001` | `contract/OpenApiIdentifierTest.java` | `[ ]` | | **D8** ⏳ |
| `MF09-TC-CON-002` | `contract/OpenApiErrorEnvelopeTest.java` | `[ ]` | | **D8** ⏳ |
| `MF09-TC-CON-003` | `contract/OpenApiEnumTest.java` | `[ ]` | | **D8** ⏳ |
| `MF09-TC-CON-004` | `contract/OpenApiMetadataTest.java` | `[ ]` | | **D8** ⏳ |
| `MF09-TC-CON-005` | `contract/OpenApiScopeTest.java` | `[ ]` | | **D8** ⏳ · 14 endpoint (`M09-D06`) |
| **`MF09-TC-CON-006`** | `contract/RoleShapeContractIT.java` | `[ ]` | | 🆕 **D11** (`M09-D10`) — `oneOf` + tài khoản seed thật (**D13**) |
| `MF09-TC-E2E-001` | `e2e/TeacherServingFlowE2E.java` | `[ ]` | | |
| `MF09-TC-E2E-002` | `e2e/PiiScopeE2E.java` | `[ ]` | | |
| `MF09-TC-E2E-003` | `e2e/BackboneE2E.java` | `[ ]` | | trục xương sống |
| `MF09-TC-E2E-004` | `e2e/FeContractE2E.java` | `[ ]` | | Minh xác nhận |

### 5.4. Red Gate Protocol (CASE 2.0 — GATE-2)

> ⭐ **Gate quan trọng nhất.** Trước khi implement, chạy toàn bộ suite với stub `throw`. **Mọi test PHẢI FAIL.** Test nào PASS ngay → **AP-AI-002 (Green-from-Birth)** → reject và viết lại.

**Stub cho Red Phase (Java):**

```java
// Red Phase — stub PHẢI throw, không trả giá trị mặc định
@Service
public class DistributionServiceImpl implements DistributionService {
    @Override public PagedResponse<DistributionResponse> list(Map<String,String> p, AuthUser a) {
        throw new UnsupportedOperationException("Not implemented — Red Phase stub");
    }
    @Override public DistributionResponse confirmServed(String code, AuthUser a) {
        throw new UnsupportedOperationException("Not implemented — Red Phase stub");
    }
    // … 5 method còn lại của §8.1 + MealConsumptionService / MealHistoryService /
    //    SpecialMealViewService / AllergyAlertService: cùng khuôn
}
```

⚠️ **Bẫy riêng của MF09 — 4 loại test dễ "xanh từ lúc đẻ":**

| Loại | Vì sao dễ PASS giả | Cách chặn |
|---|---|---|
| Test **kiểm sự vắng mặt** (`TC-SEC-009` không có sinh trắc học, `TC-INT-017` không có cột dẫn xuất, `TC-INT-005` không có bảng `meal_history`) | Khi **chưa có gì cả**, "không tồn tại X" đương nhiên đúng | Red Gate cho nhóm này chạy trên **schema đã migrate** + thêm **positive control**: cố ý tạo cột/bảng cấm trong một migration test riêng → test phải **đỏ**; xoá đi → xanh |
| Test **403/401** | Security chưa cấu hình thì Spring trả 401/403 cho mọi thứ ⇒ test xanh vì sai lý do | Mỗi TC security **bắt buộc** có **nhánh dương** đi kèm (role đúng → 2xx). `TC-SEC-002` phải assert Caterers **vẫn đọc được** `/student-distributions` |
| Test **tập rỗng** (`TC-INT-006` ca (3), `TC-SEC-003` ca `items:[]`) | DB rỗng thì mọi truy vấn đều trả rỗng | Assert kèm **đối chứng dương** trong cùng test: cùng bộ lọc nhưng đúng phạm vi phải ra **>0** bản ghi |
| Test **event** (`TC-INT-015`) | Fake bus rỗng ⇒ "không có event trùng" luôn đúng | Bước (1) phải assert **đúng 10 event**, rồi mới assert bước (2) không tăng |

**Red Gate Verification:**

| Nhóm | Số TC | Stub Result | Expected | Actual | Root Cause (nếu PASS bất thường) |
|---|---|---|---|---|---|
| Unit | 16 | `throw("Not implemented")` | 🔴 FAIL | ☐ FAIL ☐ PASS | ☐ Tautology ☐ Shared state ☐ Hallucinated contract |
| Security | 13 | stub + security chưa cấu hình | 🔴 FAIL | ☐ FAIL ☐ PASS | ☐ **401 vì sai lý do** ☐ thiếu nhánh dương |
| Integration | **19** | stub + schema đã migrate | 🔴 FAIL | ☐ FAIL ☐ PASS | ☐ **Test vắng mặt xanh giả** ☐ DB rỗng |
| Contract | **6** | chưa annotate | 🔴 FAIL | ☐ FAIL ☐ PASS | ☐ spec rỗng nên vòng lặp không chạy assert nào |
| E2E | 4 | stub | 🔴 FAIL | ☐ FAIL ☐ PASS | |

**Red Gate Evidence:**

- Stub commit hash: `___`
- Tất cả **58** FAIL? ☐ Yes → **GATE-2 PASS** (T2→T3) → tiếp tục implement
- Log file: `target/red-gate-mf09-<ngày>.log`

> **Nếu bất kỳ test PASS:** dừng lại, xác định root cause từ bảng trên, viết lại TC từ spec với Fixture Isolation Pattern. Riêng nhóm "kiểm sự vắng mặt", **bắt buộc** chạy positive control trước khi coi là RED hợp lệ.

---

## 6. Entry / Exit Criteria

### Entry Criteria (điều kiện bắt đầu viết test)

- [ ] `TDS-MF09_Backend.md` chuyển trạng thái **Approved** (hiện `Draft`) — hoặc ít nhất §5.2/§9/§10/§16 được Mạnh xác nhận
- [ ] **§2 Logic Issues L1–L8 được Nam/Mạnh xác nhận** — đặc biệt **L3** (`AP-M09-08` của TDS mâu thuẫn D4). Không xác nhận trước thì `TC-INT-011` và `TC-E2E-004` sẽ bị coi là "test sai" thay vì "TDS cần sửa"
- [ ] **MF01 nền chung sẵn sàng:** JWT filter phát `AuthUser{userId, role, schoolId}` · `PagedResponse<T>` · `@RestControllerAdvice` (envelope D2) · `common/domain/BaseEntity` + `AuditorAware` (D9) · `common/enums/` (D6)
- [ ] **Flyway `V1`** đã có 4 bảng MF09 + 5 ràng buộc + 2 RULE (TDS §5.2); `V{n}__seed_mf09.sql` đã chuyển `dd/MM/yyyy` → ISO và nhãn tiếng Việt → enum (**D4 §"Không sửa seed"** — chuyển đổi làm **một lần** ở bước này)
- [ ] **6 port §8.3** có bản hiện thực hoặc stub trả rỗng (ADR-000)
- [ ] Fixture `FX-001`…`FX-026` đã dựng; `FX-024` **Clock ghim** đã inject (nếu không, test "hôm nay" đỏ ngẫu nhiên theo ngày chạy CI)
- [ ] Testcontainers chạy được trên máy dev + CI (Docker khả dụng)

### Exit Criteria (Definition of Done)

**Chạy test:**
- [ ] `mvn test` — **16 unit + 13 security** xanh, không `@Disabled` ngoài danh sách đã khai (`TC-INT-007` nhánh nghiêm ngặt)
- [ ] `mvn verify` — **18 integration + 5 contract + 4 E2E** xanh
- [ ] **Coverage:** ≥ **90% line** cho `com.sfms.mf09.service`; **100% branch** cho `confirmServed()`, `personalize()`, nhánh cổng `InspectionGatePort` (`blocked`/`noSession`), guard `ADR-M09-6`
- [ ] **Red Gate §5.4** — cả 56 TC đã xác nhận FAIL với stub, có log evidence; nhóm "kiểm sự vắng mặt" đã chạy **positive control**

**Bất biến dữ liệu (chạy trên DB sau E2E):**
- [ ] **6 luật `semantic-audit.ts`** → **0 vi phạm** (`TC-INT-016`)
- [ ] 7 truy vấn đối soát `TDS` §14.1 → đúng kỳ vọng (5 câu trả **0 rows**)
- [ ] Không tồn tại: bảng `meal_history`, bảng tổng hợp phân phát, 5 cột dẫn xuất

**Hợp đồng & quy ước:**
- [ ] `grep -rn "@JsonFormat" src/main/java/com/sfms/mf09/` → **0 kết quả** (**D4**)
- [ ] `grep -rn "@Schema(allowableValues" src/main/java/com/sfms/mf09/` → **0 kết quả** (**D8 S5**)
- [ ] `grep -rniE "face|biometric|scan-face" src/main/java/com/sfms/mf09/` → **0 kết quả** (**BR-051**)
- [ ] `grep -rn "@ManyToOne" src/main/java/com/sfms/mf09/` → **không** entity của module khác (**AP-M09-07**)
- [ ] `curl -s "$H/api/v1/student-distributions?pageSize=1" | jq '.items[0] | keys'` chứa `"name"`, `"class"` (**C8** phần tên field)
- [ ] **`/v3/api-docs` đã tải và lưu kèm tài liệu nộp** (**D8**): `docs/openapi-mf09-<ngày>.json`

**Coverage truy vết:**
- [ ] **40/40** condition (§3 TDS-03) có ≥1 TC → **không condition mồ côi**
- [ ] **8/8** constraint C1–C8 (TDS §17.1) có ≥1 TC → chống **AP-AI-001**
- [ ] **13/13** UC của §1.9 được xử lý: có TC, **hoặc** có dòng giải thích vì sao không có TC (UC-079/UC-141/UC-055/UC-047 — §9.1)
- [ ] Mọi `Expected Result` có `Oracle Source` trỏ nguồn thật; **0** expected value là giả định AI

**Việc còn nợ phía FE (Minh — ghi rõ để không rơi):**
- [ ] `lib/format.ts` (`formatVnDate`, `formatVnDateTime`) gọi ở **tầng render** — bắt buộc trước khi cắt sang backend thật, vì backend trả **ISO** (D4/L3)
- [ ] Chuyển `teacher/distribution` từ **N × PUT** sang `POST /student-distributions/confirm` (CF-M09-9)
- [ ] `parent/meal-history` bỏ mảng tĩnh `MEAL_HISTORY`, gọi `GET /students/{code}/meal-history` (CF-M09-7)
- [ ] `lib/messages.ts` map `code` → nhãn tiếng Việt cho 12 mã `SRV-*` (D7)

### Suspension Criteria (điều kiện tạm dừng)

- **MF07 chưa có `serving-gate`** → `TC-INT-002`, `TC-SEC-010`, `TC-E2E-003` không chạy thật được (stub chỉ chứng minh MF09 gọi đúng port, không chứng minh cổng đúng). Không coi module là DONE khi 3 TC này còn chạy trên stub
- **MF05 chưa expose `MenuQueryPort`** → `TC-002`, `TC-003` chỉ ở mức unit
- **MF04 chưa có API cho `CatererAssignmentPort`** (ADR-M09-8 ghi rõ *"Mạnh cần dựng endpoint tương ứng bên MF04"*) → `TC-014` chạy trên stub
- Phát hiện lỗi kiến trúc mới cần Nam review (VD: nếu Nam **không** chấp nhận L3 thì phải sửa D4 hoặc sửa ADR-M09-5 — **không** để hai tài liệu Approved mâu thuẫn nhau)
- CI đỏ vì thay đổi ngoài MF09 (schema `V1` của Nam)

---

## 7. Rollback Plan

```bash
# ── DEV / TEST ONLY ────────────────────────────────────────────────
# 1) Xoá dữ liệu test MF09 (KHÔNG chạm schema)
#    ⚠️ CHỈ chạy trên DB Testcontainers / DB dev — xem R1 dưới
psql -c "TRUNCATE serving_dishes, distribution_error_reports,
                  meal_consumption_records, student_distributions CASCADE;"

# 2) Hạ schema MF09 — migration BÙ, KHÔNG flyway:undo trên môi trường có dữ liệu
#    src/main/resources/db/migration/V{n}__mf09_rollback.sql
#      DROP RULE  IF EXISTS serving_dishes_no_update ON serving_dishes;
#      DROP RULE  IF EXISTS serving_dishes_no_delete ON serving_dishes;
#      DROP TABLE IF EXISTS serving_dishes, distribution_error_reports,
#                           meal_consumption_records, student_distributions CASCADE;

# 3) Testcontainers: container bị huỷ sau mỗi lần chạy (chỉ giữ khi .withReuse(true))

# 4) Revert code test
git checkout -- src/test/java/com/sfms/mf09/

# 5) Gỡ Red Phase stub trước khi merge
grep -rn "Red Phase stub" src/main/java/com/sfms/mf09/   # Expected: no output
```

**R1 — ⚠️ Ràng buộc rollback riêng của MF09 (khác mọi module khác):**

> `TDS` §12.2 ghi rõ: ***"TUYỆT ĐỐI KHÔNG rollback bằng cách xoá `serving_dishes`"*** — đây là bản ghi lịch sử phục vụ điều tra sự cố (ADR-M09-1), đã bị **RULE chặn ở tầng DB**, và §4.2 quy định **giữ tối thiểu 5 năm** (BR-121 + PDPD).
>
> Trên môi trường có dữ liệu thật: nếu buộc phải sửa dữ liệu sai, **ghi bản ghi bù + audit**, **không xoá**. Lệnh `TRUNCATE` ở trên **chỉ** dành cho DB test.
>
> Sau mọi rollback, chạy **3 truy vấn đối soát** §14.1 (câu 1, 2, 3) — **cả 3 phải trả 0 dòng** trước khi mở lại cho người dùng.

**R2 — Trong giờ phục vụ:** nếu `/serve` chết giữa bữa ăn, **không** rollback ngay. Theo §12.3, thông báo Teacher + Service Caterers và kích hoạt **quy trình giấy dự phòng**; nhập bù sau bằng endpoint có `servedAt` thật, đi qua audit. Rollback schema giữa giờ ăn là mất luôn dữ liệu vừa nhập bù.

---

## 8. CASE 2.0 Anti-Pattern Detection

> Checklist cho reviewer (Thảo — Lead Test/QA) khi test cases được AI hỗ trợ generate.

| AP-ID | Anti-Pattern | Dấu hiệu trong TDD spec | Check | Gate chặn |
|---|---|---|---|---|
| AP-AI-001 | Unconstrained Generation | TC không reference constraint C1–C8 / BR / D1–D9 nào | ☐ | G-0 |
| AP-AI-002 | Green-from-Birth | Test PASS với stub `throw` (§5.4) — **rủi ro cao nhất ở MF09** vì có 3 TC "kiểm sự vắng mặt" | ☐ | G-2 ★ |
| AP-AI-003 | Implicit Decision | TC giả định quyết định kiến trúc không có trong ADR-M09-1…8 | ☐ | G-1 |
| AP-AI-004 | Layer Violation | Test kiểm business logic ở controller | ☐ | G-4 |
| AP-AI-005 | Hallucinated Contract | Test import service/type/port ngoài §8 (VD `MealSessionEntity` — TDS §17.4 nêu đích danh) | ☐ | G-3 |
| AP-AI-006 | Trust FE identity | Test truyền `teacherId`/`classId`/`studentId` qua body rồi coi là đúng | ☐ | G-1 |
| **AP-M09-01** | Frozen record mutated | Test cho phép `serving_dishes` bị UPDATE/DELETE, hoặc bị xoá khi `revoke` | ☐ | G-1 |
| **AP-M09-02** | Store derived field | Test assert đọc cột `allergy_warning`/`is_special`, hoặc bảng `meal_history`/bảng tổng hợp | ☐ | G-1 |
| **AP-M09-03** | Silent duplicate | Test chấp nhận 200 khi xác nhận lần 2 | ☐ | G-1 |
| **AP-M09-04** | Gate bypass | Test ghi `SERVED` mà không đi qua `InspectionGatePort`, hoặc coi `noSession` = `blocked` | ☐ | G-1 |
| **AP-M09-05** | Child biometric | Bất kỳ TC nào **kiểm thử** (thay vì **cấm**) luồng sinh trắc học học sinh | ☐ | **Reject cứng** |
| **AP-M09-06** | Cross-scope leak | Test chấp nhận **404** thay vì 403 khi sai phạm vi | ☐ | G-1 |
| **AP-M09-08** | Renamed JSON contract | Test assert `studentName`/`className` trong JSON thay vì `name`/`class` | ☐ | G-3 |

**Kết quả review:**

- [ ] Không phát hiện anti-pattern nào → TDD spec approved
- [ ] Phát hiện AP → ghi vào bảng dưới → fix trước khi implement

| AP detected | TC ID | Mô tả | Fix action | Fixed? |
|---|---|---|---|---|
| `AP-___` | `MF09-TC-___` | | | ☐ |

> ⚠️ **Lưu ý cho reviewer về `AP-M09-08`:** anti-pattern này trong TDS gồm **hai vế** — (a) đổi tên field JSON, (b) trả ngày ISO. TestSpec này **giữ vế (a)** (`TC-E2E-004`) và **cố ý vi phạm vế (b)** (`TC-INT-011`) vì **D4 đã Approved ghi đè**. Đây **không** phải anti-pattern trong tài liệu này — xem **L3**. Nếu reviewer thấy mâu thuẫn, hướng xử lý là **sửa `AP-M09-08` của TDS**, không phải sửa test.

---

## 9. Phụ lục — Danh sách mã UC/BR/MSG đã trích dẫn

### 9.1. Use Case (13 — đúng `traceability_matrix.md` §1.9)

| UC ID | Tên UC (nguyên văn) | Actor | Test coverage |
|---|---|---|---|
| **UC-016** | Confirm Meal Distribution | Teacher | `TC-001`, `TC-002`, `TC-006`, `TC-007`, `TC-008`, `TC-009`, `TC-012`, `TC-015`, `TC-INT-001/002/008/013`, `TC-SEC-002/003/010`, `TC-E2E-001/003` |
| **UC-080** | Student Meal Consumption | Teacher | `TC-005`, `TC-010`, `TC-INT-012/015`, `TC-E2E-001` |
| **UC-132** | View Meal Distribution List | Service Caterers | `TC-011`, `TC-INT-004/006/007` |
| **UC-103** | View Allergy Alerts | Service Caterers / Teacher | `TC-004`, `TC-INT-012/018` |
| **UC-056** | Monitor Student Meal History | Teacher | `TC-INT-006`, `TC-SEC-003` |
| **UC-053** | Monitor Child Meal History | Parent | `TC-013`, `TC-016`, `TC-INT-005`, `TC-SEC-005` |
| **UC-133** | View Meal History | Parent | `TC-014`, `TC-016`, `TC-INT-003/005`, `TC-E2E-002` |
| **UC-142** | View Special Meal Records | Parent | `TC-SEC-004`, `TC-SEC-005` *(qua `/students/{code}/special-meal-records`)* |
| **UC-143** | View Special Meal Requirements | Teacher | `TC-SEC-007` *(quyền đọc `/special-meals`)*; nội dung do **MF05/MF03 sở hữu**, MF09 chỉ đọc |
| **UC-047** | Manage Meal Service | Service Caterers | **Một phần** — chỉ `list()`/`summary()` (`TC-INT-004`). Phần suất nấu/trạng thái sẵn sàng thuộc **MF05/MF07** — **CF-M09-14/L22** |
| **UC-055** | Monitor School Meals | Parent | **Một phần** — chỉ phần **BR-133** (`TC-INT-015`). Thực đơn công bố + allergen thuộc **MF05** — **CF-M09-11/L19** |
| **UC-141** | View Special Meal Arrangements | Parent | ⛔ **Không có TC** — logic thuộc **MF05** (`derivedMealPlans()`), **CF-M09-13/L21**. `TC-CON-005` assert endpoint này **không** thuộc MF09 |
| **UC-079** | Scan Face Data | Service Caterers | ⛔ **Không có TC hiện thực** — **BR-051 cấm sinh trắc học trẻ em**, không có UC spec trong UC file. Thay vào đó `TC-SEC-009` + `TC-CON-005` assert **sự vắng mặt**. **CF-M09-1/L9** |

### 9.2. Business Rules

**BR của MF09 (10 — đúng §1.9):**

| BR | Nội dung (rút gọn, nguyên văn `business_rules.md`) | Test coverage |
|---|---|---|
| **BR-065** | Với HS **dị ứng đã xác minh**/chế độ ăn đặc biệt, hệ thống **bắt buộc** tạo kế hoạch bữa ăn riêng | `TC-003`, `TC-009`, `TC-INT-008` |
| **BR-120** | **Teacher** xác nhận phân phát bữa ăn cho HS **trong lớp của mình** | `TC-006`, `TC-SEC-002/003`, `TC-SEC-007`, `TC-E2E-002` |
| **BR-121** | Hệ thống ghi nhận và theo dõi **lịch sử tiêu thụ bữa ăn** của từng HS | `TC-005`, `TC-010`, `TC-016`, `TC-INT-005/015` |
| **BR-123** | Cảnh báo dị ứng tự động trong phiên phục vụ để ngăn phân phát sai suất | `TC-004`, `TC-INT-012/018` |
| **BR-125** | GV và nhân viên phục vụ **xem được yêu cầu bữa ăn đặc biệt** trước và trong giờ phục vụ | `TC-004`, `TC-SEC-007` |
| **BR-130** | **Chặn phân phát trùng lặp** — 1 HS không được quét 2 lần trong cùng phiên | `TC-001`, `TC-INT-001/008` |
| **BR-133** | **Tự động biên soạn** thực đơn đã phục vụ + dinh dưỡng, gửi tóm tắt hằng ngày cho phụ huynh | `TC-010`, `TC-INT-015`, `TC-E2E-001` |
| **BR-201** | Tài khoản phụ huynh liên kết **tối đa 5** hồ sơ học sinh | `TC-013`, `TC-SEC-005`, `TC-E2E-002` |
| **BR-051** | *"Không áp dụng xác thực sinh trắc học với **học sinh (trẻ em)** vì PDPD"* | `TC-SEC-009`, `TC-CON-005` — **lệnh cấm**, không phải tính năng |
| **BR-060** | Xác minh GPS nhân viên **giao hàng** | ⛔ Không TC — miền **MF04/MF06** (CF-M09-1) |

**BR trích chéo module (KHÔNG tính là BR của MF09, có nêu rõ nguồn):**

| BR | Module gốc | Vì sao xuất hiện ở đây | Test |
|---|---|---|---|
| **BR-094** | **MF07** | Cổng kiểm thực ba bước — mock, `semantic-audit.ts` §7 và ràng buộc C3 đều bắt MF09 chặn (CF-M09-4) | `TC-007`, `TC-INT-002`, `TC-SEC-010`, `TC-E2E-003` |
| **BR-031** | **MF03** | Chỉ allergen **đã xác minh** mới sinh suất riêng | `TC-003` |
| **BR-007** | MF01 | RBAC 7 role | `TC-SEC-007`, `TC-SEC-008` |
| **BR-004** | MF01 | Phiên đăng nhập → MSG-004 | `TC-SEC-001` |
| **BR-139** | **MF10** | Cửa sổ phản hồi 24h — MF09 **chỉ chuyển tiếp** cờ `feedbackOpen` | `TC-E2E-004` (chỉ assert field có mặt) |

**BR bị UC file cite nhưng KHÔNG có ở §1.9 → không có TC nào:** BR-100 (UC-047), BR-025 (UC-143/UC-056), BR-069/BR-070/BR-075 (UC-055/UC-133/UC-141), **BR-135** (không UC nào cite ở §1.9 — CF-M09-3/L11; cột `planned_serve_time` chừa sẵn, assert **chưa dùng** ở `TC-INT-017`).

### 9.3. System Messages (13 — đều tồn tại trong `error_message.md` **v1.8**)

| MSG | Nội dung nguyên văn (oracle của assert) | Mã module | Test |
|---|---|---|---|
| **MSG-004** | `Session expired. Please log in again.` | `SRV-004` | `TC-SEC-001` |
| **MSG-005** | `Access denied: You do not have permission to access this resource.` | `SRV-005` | `TC-006`, `TC-013`, `TC-SEC-002…008`, `TC-E2E-002` |
| **MSG-021** | `Duplicate meal distribution: Student has already scanned for this session.` | `SRV-021` | `TC-001`, `TC-015`, `TC-INT-001` |
| **MSG-045** *(success)* | `Meal distribution list confirmed for class [Class Name].` | — | `TC-007`, `TC-015`, `TC-INT-001`, `TC-E2E-001` |
| **MSG-129** | `Meal cannot be served: inspection step [Step Name] did not pass.` | `SRV-094` | `TC-007`, `TC-INT-002`, `TC-SEC-010` |
| **MSG-130** | `No approved or published menu found for [Meal] on [Date] for grade [Grade]. Meal distribution cannot be recorded.` | `SRV-022` | `TC-002` |
| **MSG-131** | `Distribution completion is blocked: an unresolved distribution error was reported.` | `SRV-023` | `TC-009` |
| **MSG-132** | `Cannot revoke a distribution that was confirmed on a previous day.` | `SRV-024` | `TC-008` |
| **MSG-133** | `Consumption record is locked after the serving day.` | `SRV-025` | `TC-010` |
| **MSG-137** | `Resource not found.` | `SRV-404` | `TC-013` *(assert **không** dùng cho sai phạm vi)* |
| **MSG-138** | `Validation failed: one or more required fields are missing or invalid.` | `SRV-001` | `TC-005`, `TC-INT-007` |
| **MSG-141** | `Too many requests. Please try again later.` | ⚠️ **chưa có mã MF09** | `TC-SEC-013` — **L25**, đề nghị `SRV-429` |
| **MSG-014** | `Face recognition failed: Biometric identity could not be verified.` | `SRV-014` | ⛔ **Không endpoint MF09 nào phát ra** — `TC-CON-002` assert sự vắng mặt (L9) |

**MSG được nhắc nhưng KHÔNG dùng:** **MSG-051** (allergy conflict, AF1 UC-103) — **không** có trong cột System Messages của §1.9 ⇒ response chỉ trả cờ `conflictWithTodayMenu`, không mang mã (CF-M09-12/L20, assert ở `TC-INT-018`). **MSG-139** (D5) — MF09 không có endpoint tạo nhận `id` client (L5, assert ở `TC-CON-005`).

### 9.4. Luật `semantic-audit.ts` đã port (6 — tên **nguyên văn từ script**)

| # | Tên luật | Vị trí script | Port thành | Ghi chú |
|---|---|---|---|---|
| 1 | `PHAT_SAI_NGAY` | §6 (dòng 147–148) | `TC-012`, `TC-INT-016`, CHECK `ck_dist_served_same_day` | |
| 2 | `TRANG_THAI_VS_GIO_PHAT` | §6 (dòng 150–152) | `TC-012`, `TC-INT-016`, CHECK `ck_dist_served_at` | Kiểm **2 chiều** (SERVED thiếu giờ / chưa SERVED mà có giờ) |
| 3 | `PHAT_ROI_KHONG_CO_MON` | §6 (dòng 154–155) | `TC-002`, `TC-INT-016`, §14.1 câu 1 | Điều kiện **kích hoạt rollback** §12.1 |
| 4 | `HOC_SINH_SAI_LOP` | §6 (dòng 159–160) | `TC-INT-016` | |
| 5 | **`TEN_KHONG_KHOP_HOC_SINH`** | §6 (dòng 161–162) | `TC-INT-016` | ⚠️ **TDS §4.2/§13 bỏ sót luật này** — **L26** |
| 6 | `PHAT_SUAT_DU_KIEM_THUC_TRUOT` | §7 (dòng 170–174) | `TC-INT-002`, `TC-INT-016`, `TC-SEC-010`, §14.1 câu 4 | Điều kiện **kích hoạt rollback** §12.1 |

> ⚠️ Tên luật copy **nguyên văn từ script**, không lấy từ TDS §13 (tiền lệ MF07: `HAN_LUU_KHONG_DU_72H` đã đổi thành `HAN_LUU_KHONG_DU_24H` mà TDS còn ghi tên cũ).
> Luật §11b (`SUAT_RIENG_*`) thuộc **MF05** (soi `MENUS`/`MenuVariant`), **không** port vào MF09 dù TDS §13 có nhắc §11b — MF09 chỉ tiêu thụ kết quả qua `MenuQueryPort`.

### 9.5. `00_Common_Conventions.md` — áp dụng và ghi đè

| Quyết định | Trạng thái | Áp dụng trong TestSpec này | TC |
|---|---|---|---|
| **D1** | ✅ Approved | JSON `id` = `code`, PK UUID, 4 bảng thêm `code` + `UNIQUE(school_id, code)` | `TC-INT-010`, `TC-CON-001` |
| **D2** | ✅ Approved | Envelope lỗi, `details` là mảng | mọi TC error path, `TC-CON-002` |
| **D3** | ✅ Approved | `/api/v1` | `TC-CON-005` |
| **D4** | ✅ Approved | ISO-8601 — **ghi đè ADR-M09-5, C8, AP-M09-08** (L3) | `TC-INT-011`, `TC-CON-004`, `TC-E2E-004` |
| **D5** | ✅ Approved | **Không áp dụng** — MF09 không có endpoint tạo nhận `id` client (L5) | `TC-CON-005` |
| **D6** | ✅ Approved | Enum SCREAMING_SNAKE; `ConsumptionStatus` **đủ 4**; `severity` = `SEVERE` (L4) | `TC-005`, `TC-INT-012`, `TC-CON-003` |
| **D7** | ✅ Approved | Message **tiếng Anh** — ghi đè 2 message tiếng Việt ở TDS §9.2 (L6) | `TC-002`, `TC-007` |
| **D8** | ⏳ Chờ Nam | 5 contract test vẫn viết và vẫn chạy | `TC-CON-001…005` |
| **D9** | ✅ Approved | `extends BaseEntity`, không khai lại `created_by`/`updated_by` (L7) | `TC-INT-014` |
| **D10** | — | Không áp dụng (MF09 không upload file/ảnh) | — |

### 9.6. Tổng kết coverage

| Nhóm | Số TC | Trạng thái |
|---|---|---|
| Unit (service layer) | **16** | 🔴 Not written |
| Security (RBAC + gate bypass + PDPD) | **13** | 🔴 Not written *(1 blocked một phần — L25)* |
| Integration (API → DB, Testcontainers) | **18** | 🔴 Not written *(1 nhánh `@Disabled` — L13)* |
| Contract (OpenAPI D8) | **5** | 🔴 Not written *(D8 chờ approve)* |
| E2E | **4** | 🔴 Not written |
| **Tổng** | **56** | |

| Chiều truy vết | Độ phủ |
|---|---|
| Condition (§3 TDS-03) | **40/40** — không mồ côi |
| Constraint TDS §17.1 | **8/8** (C1…C8) |
| UC §1.9 | **13/13** — 9 có TC đầy đủ, 2 một phần (UC-047, UC-055), 2 assert **sự vắng mặt** (UC-079, UC-141) |
| BR của §1.9 | **9/10** có TC; BR-060 ngoài phạm vi (miền MF04/MF06) |
| MSG | **13** mã, tất cả tồn tại trong `error_message.md` v1.8 |
| Luật `semantic-audit.ts` | **6/6** luật liên quan MF09 |
| Kịch bản TDS §13 | **11/11** mở rộng (`TC-UNIT-001…005`, `TC-INT-001…006`, `TC-E2E-001…005` của TDS đều có TC tương ứng, chi tiết hơn) |

### 9.7. Việc cần người khác chốt (tổng hợp — gửi kèm khi review)

> ✅ **CẬP NHẬT 2026-08-03 (Bước 1):** mục **1, 2, 3, 4, 11** đã **ĐÓNG** — `TDS-MF09` v2.0 sửa xong (`M09-D04`, `M09-D09`, `M09-D02`). Mục **12** đóng theo hướng khác: `CatererAssignmentPort` **bị gỡ** (`M09-D08`), MF04 đã merge nên đọc thẳng qua `ReferenceResolver`, **không cần Mạnh dựng endpoint mới**. Còn treo: **5, 6, 7, 8, 9, 10** + **3 mục mới** (13–15) từ Bước 0.

| # | Nội dung | Người chốt | Chặn TC nào |
|---|---|---|---|
| ~~1~~ ✅ | ~~**L3** — `AP-M09-08` + `ADR-M09-5` + `C8` mâu thuẫn **D4**~~ → **ĐÃ SỬA** `M09-D04`: C8 bỏ vế định dạng ngày, AP-M09-08 gỡ vế *"ngày ISO ⇒ Reject"*, §3 ghi rõ D4 ghi đè ADR-M09-5 | ~~Nam~~ | ~~`TC-INT-011`~~ — nay chạy được |
| ~~2~~ ✅ | ~~**L23** — mã phiên `INS-2026-0707-L` không tồn tại~~ → **ĐÃ SỬA** `M09-D09`: dùng `INSP-20260707-L` | ~~Mạnh~~ | ~~`TC-007`~~ |
| ~~3~~ ✅ | ~~**L24** — ghi chú §10 lỗi thời~~ → **ĐÃ GỠ** `M09-D09`; 5 mã đã điền MSG-129…133 vào cột "MSG nguồn" | ~~Mạnh~~ | — |
| ~~4~~ ✅ | ~~**L25** — thiếu mã cho HTTP 429~~ → **ĐÃ THÊM** `M09-D09`: `SRV-429`/MSG-141 (+ `SRV-409`/MSG-139 cho D5) | ~~Huy~~ | `TC-SEC-013` nay assert **cả** `code` lẫn `message` |
| 5 | **L13/CF-M09-5** — §2b-2/§2b-3 Team Plan vẫn gán `mealPrefix` cho MF09 (thực tế thuộc MF08) | **Nam** | `TC-INT-007` nhánh nghiêm ngặt (`@Disabled`) |
| 6 | **CF-M09-1/L9** — UC-079 nên chuyển sang §1.4 (MF04) hay bổ sung UC spec | **Huy** | — (test đã khoá theo BR-051) |
| 7 | **CF-M09-13/L21**, **CF-M09-14/L22** — traceability §1.9 nên ghi UC-141 dưới MF05; UC-047 làm rõ ranh giới MF05/MF07 | **Huy** + Nam | `TC-CON-005` |
| 8 | **TC-SEC-003** — lớp ngoài phạm vi trả `items:[]` **hay** 403? Cần **một** hành vi để FE hiển thị nhất quán | **Nam** | `TC-SEC-003`, `TC-E2E-002` |
| 9 | **TC-009** — ai được đánh dấu `DistributionErrorReport.resolved=true`? UC-016 AF1 không nói rõ (**thiếu oracle**) | **Huy** | `TC-009` (không assert phần này) |
| 10 | **TC-012** — thiếu mã lỗi cho "phát suất cho ngày không phải hôm nay" (**thiếu oracle**) | **Huy** | `TC-012` (chỉ assert có exception) |
| ~~11~~ ✅ | ~~**L7/D9** — có đổi `served_by`/`recorded_by`/`reported_by` không~~ → **ĐÃ CHỐT ĐỔI** `M09-D02`: `served_by_user_id` / `recorded_by_user_id` / `reported_by_user_id`. Test **vẫn không khoá tên cột** (assert qua API, không qua SQL cột) | ~~Mạnh~~ | — |
| ~~12~~ ✅ | ~~**ADR-M09-8** — MF04 cần dựng endpoint thật cho `CatererAssignmentPort`~~ → **KHÔNG CẦN NỮA**: `M09-D08` gỡ port, MF04 đã merge (V6/V7) ⇒ đọc `supplier_partnerships` qua `ReferenceResolver.currentCatererOf()`. `TC-014` **thôi chạy stub** | ~~Mạnh~~ | — |
| **13** 🆕 | **`M09-D07`** — MF09 mở **controller thứ ba** dưới `/api/v1/students/{code}` (`MealHistoryController`) cho `meal-history` + `special-meal-records`. Xác nhận không đụng `StudentController` / `StudentHealthFileController` của MF03 | **Huy** *(chủ MF03)* | `TC-INT-005`, `TC-SEC-004/005`, `TC-CON-005` |
| **14** 🆕 | **`M09-D08`** — `MenuQueryPort` là port **duy nhất** được giữ (truy vấn nhiều dòng qua `menus ⋈ menu_dishes ⋈ dishes ⋈ dish_ingredients` kèm luật chọn bản APPROVED/PUBLISHED = **hành vi** của MF05, không phải tra cứu thuần). Thảo mở port bên MF05, hoặc đồng ý MF09 stub trả `Optional.empty()` (⇒ 422 SRV-022) | **Thảo** *(chủ MF05)* | `TC-002`, `TC-004`, `TC-INT-002` |
| **15** 🆕 | **7 method mới cho `ReferenceResolver`** (`M09-D08`) — additive only, đụng file **dùng chung**. §5 workflow: *"chỉ được THÊM DÒNG, không refactor"*. Cần Nam biết trước để không trùng với module khác đang thêm | **Nam** | mọi `TC-INT-*` |

> **Không mục nào trong 15 mục trên CHẶN Bước 2.** Mục 13/14/15 là *thông báo trước khi merge* (§5 workflow: *"báo chủ màn trước khi merge, PR liệt kê rõ màn nào của ai"*), không phải điều kiện vào. Mục 5–10 đã có hành vi khoan dung + `@Disabled` tương ứng.

---

*TestSpec MF09 v2.0 — ISO/IEC/IEEE 29119-3:2021 · CASE 2.0 (Red Gate + Anti-Pattern Detection)*
*Sinh theo prompt mẫu `00_Team_Work_Assignment_Plan.md` §2c-3b · Model: Claude Opus (§2c-4) · Oracle 100% trích nguồn, 0 giả định AI*
*v2.0 (2026-08-03): Bước 0 + Bước 1 theo `02_Module_Completion_Workflow.md` §7 — Status **Approved**, 58 test case (thêm `TC-CON-006`/**D11** và `TC-INT-019`/**D12**), L27–L30 từ đối chiếu code đã merge*




