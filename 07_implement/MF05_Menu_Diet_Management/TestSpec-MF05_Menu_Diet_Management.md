# TEST-DRIVEN DEVELOPMENT SPECIFICATION
# MF05 — Menu & Diet Management

| Field | Value |
|-------|-------|
| **Document ID** | `SFMS-MF05-TDD-001` |
| **Version** | `1.2` |
| **Date** | `2026-08-03` |
| **Status** | **`Approved`** — 2026-08-03 (đồng bộ với `TDS-MF05_Backend.md` v1.2 §2.4; toàn bộ ⛔ đã đóng) |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 — Software Testing Part 3: Test Documentation |
| **Document Owner** | Nguyễn Phương Thảo (ThaoNP) |
| **Author** | Nguyễn Phương Thảo — chủ module MF05/MF07/MF12, Lead Test/QA Reviewer |
| **Reviewed by** | `[ ] Bùi Quang Huy — §2 Traceability + 4 MSG mới — Pending (TDS §2.5 mục 1/2/3/7)` · `[ ] Nguyễn Đức Mạnh — §5 Schema + §9 API — Pending` · `[ ] Trần Tuấn Minh — §8/§9 JSON shape + 3 việc Bước 3 — Pending (TDS §2.5 mục 6)` |
| **DPO Sign-off** | `[ ] Pending` *(module chạm PII gián tiếp: hồ sơ dị ứng học sinh dùng cho BR-063; MSG-051 chỉ được dùng `[Number]`, KHÔNG in danh tính HS — khoá bằng chữ ký `verifiedAllergenCountsByGrade()` chỉ trả số đếm, **M05-D14**)* |
| **Approved by** | Nguyễn Phương Thảo (chủ MF05) — 2026-08-03, theo tiền lệ MF02/MF03/MF04. Mục treo ở TDS §2.5 **không chặn Approve** (không đổi schema/hợp đồng API). |
| **Classification** | `Internal — Confidential` |

**References (nguồn oracle — mọi expected value trong tài liệu này trích từ đây, KHÔNG dùng giả định):**

| # | Nguồn | Dùng cho |
|---|-------|----------|
| 1 | `07_implement/MF05_Menu_Diet_Management/TDS-MF05_Backend.md` (`SFMS-MF05-IMP-001` v1.1) | Spec kỹ thuật gốc — §5 schema, §8 interface, §9 API, §10 error code, §13 kịch bản, §16 auth matrix, §17 constraints |
| 2 | `02_Requirements/02_UseCases/MF05_Menu_Diet_Management.md` | Tên UC nguyên văn, Normal/Alternative Flow |
| 3 | `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` **§1.6** | Nguồn CHUẨN duy nhất của UC↔BR↔MSG cho MF05 |
| 4 | `02_Requirements/00_BusinessRules/business_rules.md` §7 (+ BR-156/157 §14, BR-201 §4) | Nội dung BR |
| 5 | `02_Requirements/00_BusinessRules/error_message.md` | Chuỗi MSG nguyên văn (oracle của assert message) |
| 6 | `03_Design/08_FrontEnd/Web/src/lib/api/mock-server.ts` + `db.ts` + `hooks.ts` + `client.ts` | Hợp đồng REST bất biến (shape, PATCH-semantics, timeout 10s) |
| 7 | `03_Design/08_FrontEnd/Web/src/lib/mock-data.ts` | **Nguồn fixture** (§3 TDS-05) — mọi seed dùng ID thật, không bịa |
| 8 | `03_Design/08_FrontEnd/Web/scripts/semantic-audit.ts` §11, §11b | 8 luật nghiệp vụ port thành test |
| 9 | `03_Design/02_ADR/ADR-000.md` | Stack + chiến lược stub external service (Gemini) |
| 10 | `00_Team_Work_Assignment_Plan.md` §2b | Hợp đồng phân trang/search/filter dùng chung |
| 11 | **`07_implement/00_Common_Conventions.md` (D1–D7) — ✅ Approved 2026-07-30** | **Quy ước ưu tiên CAO NHẤT.** Ghi đè ADR module/constraint TDS khi xung đột — xem **§2.0 L15–L18** |

> **⚠️ ÁP DỤNG `00_Common_Conventions.md` D1–D7 (Approved 2026-07-30, sau khi bản v1.0 của tài liệu này được soạn).**
> Thứ tự ưu tiên khi xung đột: **`00_Common_Conventions.md` > ADR module (TDS §3) > hợp đồng mock FE**.
> Hệ quả cho tài liệu này: **D1** JSON `id` = `code` (PK là UUID) → DB assertion dùng `WHERE code = ?` · **D2** lỗi trong envelope `{"error":{"code","message","details"}}` → assert `$.error.code` · **D3** mọi path `/api/v1/...` *(đã đúng)* · **D4** JSON ngày **ISO-8601** → **ghi đè** CF-10/C9 (`dd/MM/yyyy`) · **D5** nhận `id` client, trùng → 409 *(đã đúng — D5 lấy chuẩn từ chính MF05 CF-11)* · **D6** enum dùng chung ở `common/enums/` · **D7** `message` tiếng Anh nguyên văn `error_message.md` *(đã đúng)*.
> Chi tiết 4 điểm ghi đè ở **§2.0**.

> **⚠️ RÀNG BUỘC STACK — tài liệu này KHÔNG dùng cú pháp mẫu của `PHASE-4_Test-Spec.md`.**
> Template gốc minh hoạ bằng TypeScript/Jest/Prisma. Dự án dùng **Java 25 LTS · Spring Boot 4.1.x · PostgreSQL 16 · Flyway · Spring Security + JWT · MapStruct**.
> Vì vậy: test = **JUnit 5 + AssertJ + Mockito** (unit), **`@SpringBootTest` + Testcontainers `PostgreSQLContainer` + MockMvc** (integration), rollback = **Flyway migration bù** (KHÔNG `npx prisma migrate reset`), fixture factory = **Java builder** (KHÔNG `makeEntity()` TypeScript).

> **Quy ước TDD:** tài liệu này mô tả test cases **TRƯỚC** khi viết production code.
> Thứ tự bắt buộc: viết test (`*Test.java`) → chạy → xác nhận **FAIL 🔴** → implement → **PASS 🟢** → refactor 🔵.
> Không mark test là ✅ nếu `mvn verify` chưa xanh. Test data dùng school `SCH-001` (QA seed), **không dùng PII thật**.

---

## CHANGELOG

> **Policy 4.4 — Immutable History:** không xóa thông tin cũ.

| Ngày | Người thực hiện | Nội dung thay đổi |
|------|-----------------|-------------------|
| 2026-08-03 | Nguyễn Phương Thảo | **v1.2 — Bước 1 (`02_Module_Completion_Workflow.md` §7): đồng bộ với `TDS-MF05_Backend.md` v1.2, Status → Approved.** Thêm **§2.-1 nhóm C** với **10 Logic Issue mới L19–L28** — lệch giữa tài liệu và **code thật đã merge**, lớp lỗi mà rà tài liệu thuần không bắt được (§1 của workflow). Nặng nhất **L19**: `special_meal_requests` + `/api/v1/special-meals` **là của MF03, đã chạy** ⇒ **4 TC bị gỡ/chuyển chủ** (`TC-013`, `TC-SEC-011`, phần `special-meals` của `TC-INT-009` và `TC-E2E-001`), **`FX-014`/`FX-015` bỏ**, và **L14 đóng** (danh mục chế độ ăn là **cấu hình** `StandardDietCatalog` + BR-220, không phải enum — không TC nào khoá enum nữa). Kèm: **L20** oracle `MENU-066` **không còn tồn tại** (đổi sang MSG-144 của MF03) · **L21** DB assertion phải join qua **UUID PK**, `WHERE code = ?` (D1) · **L22** entity `extends BaseEntity` ⇒ fixture builder **không set** `createdAt/createdBy` · **L23** `MealType` import từ `common/enums` · **L24** thêm TC cho **D11** (`MenuPublicResponse` — Parent/Teacher không thấy giá) · **L25** thêm TC cho **D12** (`dishes` là **số**, gửi mảng ⇒ bỏ qua, không 200-mà-không-lưu) · **L26** seed **đã đủ 7 role** (D13) ⇒ `@WithMockUser` thay bằng tài khoản thật `U-0022` cho luật chủ sở hữu · **L27** migration test dùng **V8/V9**, rollback **V10**, **không drop `special_meal_requests`** · **L28** package test đổi `com.sfms.menu` → **`com.sfms.mf05`**. Số TC: **56 → 54** (gỡ 2 TC thuộc MF03) **+ 2 TC mới** (D11, D12) = **56**. |
| 2026-07-30 | Nguyễn Phương Thảo | **v1.1 — áp `00_Common_Conventions.md` D1–D7 (Approved 2026-07-30).** Bổ sung **§2.0 nhóm A** với 4 Logic Issue mới **L15–L18** (D1 định danh · D2 envelope lỗi · D4 ngày ISO · D6 enum dùng chung). **Viết lại `MF05-TC-024`**: response trả **ISO-8601** thay vì `dd/MM/yyyy` (D4 ghi đè CF-10/C9). Đổi mọi DB assertion sang **`WHERE code = ?`** và join qua `menus.id` (UUID) cho bảng con (D1). Thêm `COND-30`…`COND-32`. Ghi rõ **L10/CF-10 đã được giải quyết** bởi D4. Số TC **không đổi (56)** — chỉ đổi oracle. |
| 2026-07-30 | Nguyễn Phương Thảo | Khởi tạo v1.0 — TDD spec cho MF05 Menu & Diet Management. Nguồn: TDS-MF05_Backend.md v1.1 (§13 kịch bản, §16 auth matrix, §17 constraints C1–C12), traceability §1.6 (10 UC), `business_rules.md` §7, `error_message.md`, `semantic-audit.ts` §11/§11b (8 luật), fixture từ `mock-data.ts`. Tổng **56 test case**: 26 unit · 14 integration · 14 security · 2 E2E. Ghi nhận **13 Logic Issue** (L1–L13) kế thừa CF-1…CF-13 của TDS + **L14** (lệch danh mục `STANDARD_DIETS`). |

---

## MỤC LỤC

1. [Thông tin Module](#1-thông-tin-module) · 1.1 [AI Generation Context](#11-ai-generation-context-case-20)
2. [Logic Issues Resolved](#2-logic-issues-resolved)
3. [Test Design Specification](#3-test-design-specification)
4. [Test Case Specification](#4-test-case-specification) · [Unit](#41-unit-test-cases-service-layer) · [Security](#42-security-test-cases) · [Integration](#43-integration-test-cases) · [E2E](#44-e2e-test-cases)
5. [Red-Green-Refactor Tracker](#5-red-green-refactor-tracker) · 5.1 [Red Gate Protocol](#51-red-gate-protocol-case-20--gate-2)
6. [Entry / Exit Criteria](#6-entry--exit-criteria)
7. [Rollback Plan](#7-rollback-plan)
8. [CASE 2.0 Anti-Pattern Detection](#8-case-20-anti-pattern-detection)
9. [Phụ lục — Danh sách mã UC/BR/MSG đã trích dẫn](#9-phụ-lục--danh-sách-mã-ucbrmsg-đã-trích-dẫn)

---

## 1. Thông tin Module

| Field | Value |
|-------|-------|
| **Feature / Module ID** | `MF05` — Menu & Diet Management |
| **Bounded Context** | Menu Planning & Nutrition |
| **Spec gốc** | `SFMS-MF05-IMP-001` (`TDS-MF05_Backend.md` **v1.2, Approved 2026-08-03** — §2.4 M05-D01…D16) |
| **Priority** | 🟠 **P1** — trục xương sống (`Login theo role → Menu theo khối → phân suất & thống kê theo món`) |
| **Số UC trong phạm vi** | **10** — UC-020, UC-096, UC-075, UC-023, UC-087, UC-006, UC-048, UC-128, UC-134, UC-135 (đúng `traceability_matrix.md` §1.6) |
| **Data Classification** | `Internal` + tham chiếu `PII` gián tiếp (hồ sơ dị ứng HS khi chạy BR-063) |
| **Compliance Scope** | PDPD — thực đơn công bố **không** được lộ danh tính HS; MSG-051 chỉ dùng `[Number]`. BR-062 mục tiêu dinh dưỡng theo nhóm tuổi. |
| **Upstream Dependencies** | MF01 (JWT/RBAC), MF02 (`Classroom` → sĩ số/khối, `SchoolConfig.costCeiling`), MF03 (`Student.allergies`, `Student.specialDiet`), MF06 (`InventoryBatch`), MF12 (`AiAssistPort` — Gemini stub) |
| **Downstream Consumers** | MF06 (dự trù nguyên liệu), MF07 (kiểm thực gắn `dishId`), MF08 (mẫu lưu), MF09 (phát suất — ràng buộc C3), MF13 (báo cáo dinh dưỡng/chi phí) |
| **Package Java** | **`com.sfms.mf05.{domain, repository, dto, mapper, service, web, port}`** *(đổi 2026-08-03 — L28/M05-D12)* |
| **Test module** | `src/test/java/com/sfms/mf05/**` *(L28)* |
| **Milestone** | Ngày 3–4 theo lịch `00_Team_Work_Assignment_Plan.md` §4b (Backend song song — Thảo MF05) |

**Coverage mục tiêu (Exit Criteria §6):** ≥ **85%** line coverage cho `com.sfms.mf05.service`, **100%** branch coverage cho `MenuWorkflowService` (state machine BR-068) và `AllergyConflictChecker` (BR-063 — luật chặn cứng).

### 1.1 AI Generation Context (CASE 2.0)

| Field | Value |
|-------|-------|
| **AI Assisted?** | `Yes` — soạn thảo có AI hỗ trợ, oracle do người xác nhận |
| **Constraint Source** | `TDS-MF05_Backend.md` §17.1 (C1–C12), §10 (error codes), §16 (auth matrix), §13 (kịch bản); `00_Team_Work_Assignment_Plan.md` §2b (hợp đồng phân trang) |
| **Constraints Injected** | C1 state machine BR-068 · C2 bất khả sửa BR-067 · C3 dị ứng runtime BR-063 · C4 lead-time 3 ngày BR-061 + khai báo BOM BR-069 · C5 `audience` thay `menu_variants` · C6 `AiAssistPort` timeout 10s → MSG-073 · C7 shape phân trang 5 field · C8 `PUT` = partial merge · C9 giữ nguyên tên field JSON + **ngày ISO-8601** *(sửa v1.2 — D4 ghi đè `dd/MM/yyyy`)* · C10 `id` client + khuôn `{menuCode}--{dietGroupCode}` · C11 tự tính lại field dẫn xuất · C12 phân quyền theo FIELD trong body · **C13 định danh D1 + BaseEntity D9 + enum D6 + package `com.sfms.mf05`** · **C14 `special-meals` KHÔNG thuộc MF05** · **C15 `oneOf` theo vai trò D11 + key ghi≠đọc D12** |
| **Model** | Claude Opus (theo `00_Team_Work_Assignment_Plan.md` §2c-4 — TestSpec = Opus vì oracle phải khớp BR/MSG) |
| **Trust Level** | `T2 → T3` (pending Red Gate §5.1) |
| **Oracle policy** | Mọi `Expected Result` **phải** có dòng `Oracle Source` trỏ tới BR/UC/MSG/luật `semantic-audit.ts`. TC không có oracle → reject (AP-AI-001). |

---

## 2. Logic Issues Resolved

> **Bắt buộc điền trước khi viết test.** Test cases encode hành vi **đã sửa**, không phải hành vi trong spec gốc.
> **L15–L18 (§2.0)** là các chỗ `00_Common_Conventions.md` (D1–D7, Approved 2026-07-30) **ghi đè** ADR/constraint trong TDS-MF05 — bổ sung ở bản v1.1 của tài liệu này.
> **L1–L14** kế thừa các mâu thuẫn CF-1…CF-13 đã ghi ở `TDS-MF05_Backend.md` §2.3 + L14 (danh mục `STANDARD_DIETS`) — **không tự chọn bên đúng**, chỉ ghi rõ hành vi nào được test.

### 2.-1. Nhóm C — CODE THẬT ghi đè tài liệu (⚠️ **ưu tiên cao nhất**, bổ sung v1.2 · 2026-08-03)

> Từ 2026-08-02 thứ tự ưu tiên đổi: **code đã merge ở `04_Development/Backend/` đứng TRÊN tài liệu** (`00_Common_Conventions.md` §0-BIS). 10 mục dưới đây tìm được bằng cách **mở file thật**, không phải bằng đọc TDS — đúng lớp lỗi mà `02_Module_Completion_Workflow.md` §1 kết luận *"rà tài liệu không bắt được"*.
> Mỗi mục đã có **quyết định chốt** ở `TDS-MF05_Backend.md` **§2.4** (M05-D01…M05-D16). Test khoá theo cột "Hành vi được TEST" — **không** áp policy khoan dung của nhóm B.

| # | Lệch với code thật | Bằng chứng (file:dòng) | Hành vi được TEST (v1.2) | TC ảnh hưởng | Quyết định |
|---|---|---|---|---|---|
| **L19** | ⛔ **`special_meal_requests` + `/api/v1/special-meals` là của MF03 và ĐÃ MERGE.** TestSpec v1.1 test chúng như tài nguyên của MF05 | `V4__mf03_student_parent.sql:120` · `mf03/web/SpecialMealRequestController.java:36` · `mf03/service/SpecialMealRequestServiceImpl` · `ModuleCodeResolver` (`special-meals → STU`) | **Gỡ khỏi phạm vi MF05.** `TC-013` và `TC-SEC-011` **chuyển chủ sang MF03** (Huy — TestSpec-MF03 đã phủ). MF05 chỉ test **`DietCoverageValidator`** đọc qua `SpecialMealLookupPort` **được mock** trong unit test. Không TC nào của MF05 gọi `/api/v1/special-meals`, không fixture nào `INSERT INTO special_meal_requests` | `TC-013` ⛔gỡ · `TC-SEC-011` ⛔gỡ · `TC-INT-009` (bỏ vế `special-meals`) · `TC-E2E-001` (bỏ resource khỏi danh sách smoke) · `FX-014`/`FX-015` ⛔bỏ | **M05-D04** |
| **L20** | Oracle `MENU-066` **không còn tồn tại** | `error_message.md` v1.9 dòng 88: *"Nếu MF05 đang dùng MSG-114 thì chuyển sang MSG-144"* · `mf03/service/StandardDietCatalog.java` | `dietaryType` ngoài danh mục ⇒ **`NEEDS_INFO` + MSG-144** (không phải 422 từ chối), và **do MF03 assert**. **L14 ĐÓNG**: danh mục là **cấu hình** (`sfms.mf03.standard-diets`), *danh mục MỞ* theo **BR-220** ⇒ **không TC nào khoá enum**, không cần Huy chốt thêm | `TC-013` (đã gỡ) · `COND-09` chuyển chủ | **M05-D05** |
| **L21** | DB assertion giả định `id` là PK | `V4`/`V6` đều dùng `UUID PRIMARY KEY DEFAULT gen_random_uuid()` + cột `code` | Mọi assertion tra bản ghi bằng **`WHERE code = ?`**; join bảng con bằng **UUID PK** (`menu_dishes.menu_id = menus.id`), **không** join bằng chuỗi mã. Assert `$.id` trong JSON = **mã nghiệp vụ**, `format: uuid` **không được** xuất hiện ở bất kỳ field `id` nào trong `/v3/api-docs` | `TC-INT-001`, `TC-INT-010`, `TC-023`, `TC-025`, `TC-026` (đã sửa từ v1.1) | **M05-D01** |
| **L22** | Fixture builder set tay `createdAt`/`createdBy` | `common/domain/BaseEntity.java` + `AuditorAwareConfig` tự điền qua `AuditingEntityListener` | Fixture **KHÔNG** set 4 field audit — chúng do Spring Data điền (`created_by` = email actor, hoặc `"system"` khi không có request context). Test nào assert `createdBy` của **nghiệp vụ** thì đọc `createdBySupplierId`/nhãn dẫn xuất, **không** đọc cột audit | mọi fixture `Menu`/`Dish`/`MenuTemplate` (§4 boilerplate) | **M05-D02** |
| **L23** | Test import `MealType` từ package module | `common/enums/MealType.java` đã tồn tại | Import **`com.sfms.common.enums.MealType`**. Test nào so nhãn thì dùng `SNACK` = **"Bữa phụ"** (D6 chốt, **không** phải "Bữa chiều") | `TC-003`, `TC-004`, `TC-INT-008` | **M05-D03** |
| **L24** | 🆕 **Không TC nào** kiểm shape đổi theo vai trò | D11 (`02_Module_Completion_Workflow.md` §6) — đã tái diễn ở `GET /schools` và `GET /students` | **TC MỚI `MF05-TC-SEC-015`**: Parent và Teacher gọi `GET /menus` **không** thấy `estimatedCost`, `costCeiling`, `createdBySupplierId`, `approvalSignature`, `rejectReason`, và chỉ nhận bản ghi `PUBLISHED`; School Admin thấy đủ. Kèm assert `/v3/api-docs` có **cả hai** schema `PagedResponseMenuResponse` và `PagedResponseMenuPublicResponse` với `items` **đúng kiểu** (không phải `{}`) | 🆕 `TC-SEC-015` | **M05-D08** |
| **L25** | 🆕 **Không TC nào** kiểm bẫy "200 mà không lưu" | D12 — `PATCH /students/{code}` của MF03 từng trả **200 mà không lưu gì** | **TC MỚI `MF05-TC-027`**: `POST /menus` gửi `dishes: [{...}]` (mảng) ⇒ backend **bỏ qua**, `dishes` trong response là **số đếm tính lại** từ `blocksData`; gửi field dẫn xuất (`allergyConflicts`, `createdBy`, `approvedAt`) ⇒ **không** ghi vào DB. Đối chiếu từng cặp Request/Response: mọi field **sửa được** phải **cùng tên JSON hai chiều** | 🆕 `TC-027` | **M05-D09** |
| **L26** | Security TC dùng `@WithMockUser` cho mọi thứ, kể cả luật **chủ sở hữu** | `V900__test_fixtures.sql` có đủ 7 role, và **`U-0022` service-caterers có `partner_id = CAT-001`** | Luật "Caterer chỉ sửa thực đơn đơn vị mình" phải test bằng **tài khoản thật** (`U-0022`, mật khẩu `P@ssw0rd123`) vì nó phụ thuộc claim `partnerId` trong JWT — `@WithMockUser` **không** mang claim đó. Các TC chỉ kiểm role thì `@WithMockUser` vẫn dùng được. **Không phải thêm tài khoản seed nào** (D13 đã đạt) | `TC-SEC-002`, `TC-SEC-003`, `TC-SEC-012` | **M05-D10** |
| **L27** | §7 Rollback drop cả `special_meal_requests`; migration đánh số `V7`/`V8`/`V9` | `ls db/migration/` → `V1`…`V7` đã dùng (`V6`/`V7` = MF04) | Migration test **`V8`/`V9`**, rollback **`V10`**. Câu `DROP TABLE` **KHÔNG** được chứa `special_meal_requests` — drop bảng của MF03 sẽ **xoá hồ sơ chế độ ăn thật** của học sinh | §7 Rollback Plan · Entry Criteria | **M05-D11** |
| **L28** | Test nằm ở `src/test/java/com/sfms/menu/**` | §0-BIS — khuôn `com.sfms.mf<NN>` | **`src/test/java/com/sfms/mf05/**`**. `com.sfms.iam` (MF01) là ngoại lệ lịch sử, không bắt chước | toàn bộ đường dẫn Test File | **M05-D12** |

> **Vì sao nhóm C tồn tại:** `02_Module_Completion_Workflow.md` §1 đo được — MF01/MF02 có TDS Approved thì **6/7 type FE khớp**, còn MF03 cũng Approved mà vẫn lệch ~12 chỗ phải curl tay từng endpoint mới tìm ra. Rà tài liệu bắt lệch **thiết kế**; lệch **hiện thực** chỉ bắt được khi mở code thật. **L19 là ví dụ đắt nhất**: nếu không mở `V4` ra đọc, Bước 2 của MF05 sẽ `CREATE TABLE` một bảng đã tồn tại — Flyway đỏ trên máy cả nhóm, và hai controller cùng map `/api/v1/special-meals` thì **ứng dụng không khởi động được**.

### 2.0. Nhóm A — `00_Common_Conventions.md` ghi đè TDS (ưu tiên xử lý trước)

> 4 hàng dưới đây **đã có người chốt** (Nam Final Approve 2026-07-30) ⇒ test khoá theo D1–D7, **không** áp policy "khoan dung + `@Disabled`" như nhóm B.

| # | Quy ước | TDS-MF05 / TestSpec v1.0 hiện viết | Hành vi được TEST (v1.1) | TC ảnh hưởng |
|---|---|---|---|---|
| **L15** | **D1** — JSON `id` = mã nghiệp vụ (`code`), PK là **UUID**; path param dùng `{code}`; khoá ngoại trong JSON dùng `code` | TDS §5.2 khai PK `VARCHAR(32)` = chính mã nghiệp vụ (D1 xếp MF05 vào nhóm *"PK là `VARCHAR(32/40/64)`"*) | Entity có `@Id UUID id` **+** `code VARCHAR(32) NOT NULL` + `UNIQUE(school_id, code)`; JSON trả `code` dưới tên `id` qua `@JsonProperty("id")`. **Mọi DB assertion dùng `WHERE code = ?`.** Path `GET /api/v1/menus/MENU-2026-30` **không đổi**. ⚠️ D1 ghi rõ khuôn **`{menuId}--{dietGroupId}`** của MF05 *"không vỡ"* — nó chỉ hợp lệ **vì** `id` vẫn là mã nghiệp vụ (D1 lý do #4) | `TC-INT-001`, `TC-INT-010`, `TC-025`, `TC-026`, `TC-023` |
| **L16** | **D2** — envelope lỗi `{"error":{"code","message","details"}}` | v1.0 viết *"body code `MENU-051`"* không nói envelope | Assert theo JSON path **`$.error.code == "MENU-051"`**, `$.error.message` = chuỗi nguyên văn `error_message.md`, `$.error.details` là **mảng** (không bao giờ `null`) | mọi TC error path |
| **L17** | **D4** — JSON ngày **luôn ISO-8601**; **bỏ** mọi `@JsonFormat("dd/MM/yyyy")` | **L10/CF-10** + TDS §17.1 **C9** chốt *"format ra `dd/MM/yyyy` ở tầng response"* | **Assert ISO** cho `submittedAt`, `approvedAt`, `date`, `createdAt`. Backend **vẫn parse cả 2 dạng** khi NHẬN (mock đang gửi `dd/MM/yyyy`) nhưng **TRẢ VỀ ISO**. `menuDateIso` giữ ISO như trước. ⚠️ D4 ghi rõ *"không sửa seed `mock-data.ts` ở đợt này"* ⇒ FE thêm `lib/format.ts` ở tầng render (việc của Minh) | **`TC-024` (viết lại)**, `TC-E2E-002` |
| **L18** | **D6** — 8 enum dùng chung ở `common/enums/`, **SCREAMING_SNAKE**; `MealType` = `BREAKFAST\|LUNCH\|SNACK`, nhãn `SNACK` = **"Bữa phụ"** | TDS §5.2 khai `MealType` **trong** `com.sfms.menu.domain` | Enum lấy từ **`common/enums/MealType`** (Nam sở hữu) — module **không** khai lại. `AllergySeverity` cũng theo D6 (`MILD\|MODERATE\|SEVERE`) — ⚠️ ảnh hưởng `TC-003` vì fixture `FX-006` dùng `allergySeverity="Severe"` (D6 ghi rõ đây là *"ba từ vựng cho một khái niệm"* và *"sai enum = **mất cảnh báo dị ứng nặng**"*) | `TC-003`, `TC-004`, `TC-INT-008` |

> **Ghi chú cho Mạnh & Minh:** L15–L18 nghĩa là **TDS-MF05 §5.2/§9/§17 cần một bản cập nhật** theo checklist cuối `00_Common_Conventions.md`. TestSpec này (v1.1) viết theo **D1–D7** — nguồn ưu tiên cao hơn. **Không** sửa TestSpec để khớp TDS cũ.

### 2.1. Nhóm B — mâu thuẫn nguồn kế thừa từ TDS §2.3

| # | Nguồn CF | Spec gốc (sai / thiếu / lệch) | Thực tế (schema / mock / policy) | Fix áp dụng trong test |
|---|---|---|---|---|
| **L1** | CF-1 | UC file MF05 dùng BR-070…BR-075 với nghĩa Menu (BR-071 = "chế biến riêng", BR-073 = "món thay thế top-5 allergen") | `business_rules.md` §6: BR-071 = chứng nhận lô, BR-073 = GPS nhận hàng (thuộc MF06) | Test **không** assert nội dung BR-071/BR-073 theo nghĩa UC file. TC của UC-023 chỉ assert BR-065, BR-066 (nghĩa canonical rõ ràng). Ghi chờ **Huy** xác nhận. |
| **L2** | CF-2 | BR-080 (trần chi phí) **không** có trong cột BR của §1.6 | mock enforce thật (`Menu.costCeiling`, luật `DA_DUYET_MA_VAN_VUOT_TRAN`) | `MF05-TC-017` test **ràng buộc dữ liệu** (nguồn oracle = luật `semantic-audit.ts`, **không** ghi BR-080 là oracle chính thức). |
| **L3** | CF-3 | UC-023 Normal Flow bước 6 yêu cầu "validate alternative dishes cho top 5 allergen" (BR-081) | **BR-081 đã XOÁ 2026-07-01** | **KHÔNG** có TC nào cho luật top-5-allergen. Thay bằng `MF05-TC-004` (suất thay thế cấp món) + `MF05-TC-012` (BR-065 diet coverage). |
| **L4** | CF-4 | `data_model.puml` còn `MenuVariant` / `MenuVariantDish` | mock gộp vào `Menu.audience` từ 22/07/2026 | Fixture & assert dùng **một bảng `menus`** + cột `audience`. **Không** có TC nào chạm `menu_variants`. |
| **L5** | CF-5 | `MenuTemplate` không có entity trong `data_model.puml` | mock có `MENU_TEMPLATES` + màn `menu-templates` | `MF05-TC-INT-011` assert trên bảng `menu_templates` + `menu_template_dishes` (theo TDS §5.2). |
| **L6** | CF-6 | `data_model.puml` còn `Dish.supplier` / `Dish.kitchen` | mock bỏ 23/07/2026; thêm `Dish.defaultSubstituteId` | Fixture `Dish` **không** set `supplier`/`kitchen`; `MF05-TC-INT-011` assert `defaultSubstituteId`. |
| **L7** | CF-7 | Plan §2b-3 ghi `SEARCH_FIELDS` MF05 = `{name, description}` | entity không có `description` | `MF05-TC-INT-005` assert `SEARCH_FIELDS = {name, grade, createdBy, weekLabel}` (TDS §9.2). |
| **L8** | CF-8 | BR-068 quy định `APPROVED` **rồi mới** `PUBLISHED`; FE nhảy thẳng `PENDING_APPROVAL → PUBLISHED`, riêng màn suất riêng lại set `APPROVED` | mock 2 màn 2 kiểu | **Test cả hai** đường: `MF05-TC-021` (nhảy thẳng `PUBLISHED` — hợp lệ giai đoạn 1, backend tự sinh `approvedAt` + ghi log lệch) và `MF05-TC-022` (đủ 4 bước BR-068). Không TC nào **chặn** đường nhảy thẳng cho tới khi Huy/Nam chốt. |
| **L9** | CF-9 | FE **gửi** `dishes` (đếm món) và `allergyConflicts` (đếm xung đột) trong payload | `data_model.puml` đánh dấu `<<derived>>`; ADR-M05-02 tính runtime | `MF05-TC-023` assert backend **bỏ qua** số FE gửi, **tự tính lại**, nhưng response **vẫn có** đúng 2 tên field đó. |
| **L10** | CF-10 | Quy ước `01_MVP…` §3: "Ngày = ISO/Instant" | mock gửi/đọc `"28/07/2026"`, `"05/08/2026, 10:30:00"` (locale `vi-VN`) | ⚠️ **ĐÃ ĐƯỢC GIẢI QUYẾT bởi D4 — xem L17.** Bản v1.0 chốt *"format ra `dd/MM/yyyy` ở response"*; **D4 (Approved 30/07) ghi đè**: response trả **ISO**, backend **parse cả 2 dạng** khi nhận. `MF05-TC-024` đã viết lại theo D4. |
| **L11** | CF-11 | Quy ước `01_MVP…` §3: `nextId()` tập trung | mock: `body.id ?? nextId(...)`; suất riêng bắt buộc `{menuId}--{dietGroupId}` | `MF05-TC-025` assert nhận `id` client, trùng → **409**; `MF05-TC-026` assert khuôn 2 gạch ngang. |
| **L12** | CF-12 | `blocksData` / `servings` không có trong `data_model.puml` lẫn interface `MenuItem` | FE lưu & render trực tiếp từ `blocksData` | Fixture dựng qua `menu_dishes` + `menu_dish_substitutes`; assert response **luôn có** `blocksData` (map hai chiều). |
| **L13** | CF-13 | §1.6 liệt kê UC-075 (BR-156, BR-064, MSG-073) | **mock không có call-site nào** cho AI | `MF05-TC-014` test `POST /menus/{id}/ai-suggest` như **endpoint MỚI** theo TDS §9.3.10; đánh dấu blocked cho tới khi Minh bổ sung nút mock (`00_` §0 chống lệch). |
| **L14** | *(mới — phát hiện khi soạn TestSpec)* | BR-066 liệt kê danh mục: **Halal, Vegetarian, Gluten-Free, Nut-Free** "và các loại khác" | `mock-data.ts:2862` `STANDARD_DIETS = [...5 phần tử...]`; `DIET_GROUPS` còn có `DIABETIC` | ✅ **ĐÓNG 2026-08-03 — xem L19/L20.** Huy **đã chốt**: **BR-220** (2026-08-02) + `mf03/service/StandardDietCatalog.java` — danh mục là **CẤU HÌNH** (`sfms.mf03.standard-diets`), **MỞ**, ngoài danh mục ⇒ `NEEDS_INFO` + **MSG-144** (không phải từ chối). Việc enforce thuộc **MF03**. MF05 **không** khai enum, **không** viết TC cho luật này. `MF05-TC-013` gỡ khỏi tài liệu này. |

**Cách xử lý mâu thuẫn trong test (policy):** nơi nào 2 nguồn nói khác nhau và chưa có người chốt → test **hành vi khoan dung** (accept cả hai) + để lại `@Disabled("Chờ CF-x — Huy/Nam chốt")` cho TC của hành vi nghiêm ngặt. **Không** viết test khoá một bên rồi coi là chuẩn.

---

## 3. Test Design Specification

### TDS-01 — Scope / Phạm vi

```
MF05 (com.sfms.mf05) — các layer được kiểm thử:          ← đổi từ com.sfms.menu (L28)
├── domain/       Menu, MenuDish, MenuDishSubstitute, Dish, MenuTemplate,
│                 DietGroup + enum MenuStatus/DishCategory
│                 (MealType lấy từ com.sfms.common.enums — L23; SpecialMealRequest là của MF03 — L19)
│                 → pure logic, không dependency (state machine, assertEditable)
├── service/      MenuService · MenuWorkflowService · AllergyConflictChecker
│                 DietCoverageValidator · MenuRecommendationService · ParentMenuService
│                 → UNIT TEST: Mockito mock repository + AiAssistPort + SpecialMealLookupPort
├── web/          MenuController · DishController · MenuTemplateController
│                 → SECURITY TEST: MockMvc + @WithMockUser (7 role);
│                   luật CHỦ SỞ HỮU dùng tài khoản thật U-0022 (claim partnerId — L26)
└── integration/  @SpringBootTest + Testcontainers PostgreSQL 16 + Flyway (V8/V9 — L27)
                  → API → DB, hợp đồng phân trang §2b, audit log
```

**NGOÀI phạm vi (test ở module khác, chỉ **stub** ở đây):**

| Thành phần | Chủ | Cách xử lý trong test MF05 |
|---|---|---|
| Phát hành/verify JWT, RBAC filter | MF01 (Nam) | `@WithMockUser(roles=...)` + JWT giả lập; **không** test thuật toán token |
| `Classroom` (sĩ số/khối), `SchoolConfig.costCeiling` | MF02 (Nam) | seed trực tiếp bảng, không qua API MF02 |
| `Student.allergies` / `specialDiet`, `AllergyDeclaration` | MF03 (Huy) | seed bảng `students` + `allergy_declarations` theo fixture FX-006/FX-007 |
| **`SpecialMealRequest` + `/api/v1/special-meals`** *(mới — L19)* | **MF03 (Huy) — ĐÃ MERGE** | **KHÔNG test ở đây.** Mock `SpecialMealLookupPort` trong unit test của `DietCoverageValidator`; **không** `INSERT INTO special_meal_requests`, **không** gọi endpoint |
| `InventoryBatch` (BR-064 gợi ý theo tồn kho) | MF06 (Huy) | Mockito stub `InventoryQueryPort` |
| Gemini AI | MF12 (Thảo) | **`AiAssistPort` fake** — trả JSON đúng schema ADR-000; TC-014 dùng fake delay 11s |

### TDS-02 — Test Basis / Cơ sở Kiểm thử

| Source | Items Derived |
|--------|--------------|
| `traceability_matrix.md` **§1.6** | 10 UC trong phạm vi + tập BR/MSG hợp lệ (nguồn CHUẨN — không nhận mã ngoài bảng này) |
| `MF05_Menu_Diet_Management.md` (UC file) | Normal Flow / Alternative Flow → bước nghiệp vụ của từng TC (UC-006 bước 5 = chữ ký duyệt; UC-006 AF1 = lý do từ chối; UC-075 AF1 = AI lỗi vẫn cho lập tay; UC-135 AF2 = suất riêng của con) |
| `business_rules.md` §7 | **BR-061** lead-time ≥ 3 ngày · **BR-062** dinh dưỡng theo nhóm tuổi · **BR-063** xung đột dị ứng chặn cứng · **BR-064** AI ưu tiên tồn kho + trần chi phí · **BR-065** bắt buộc kế hoạch bữa ăn riêng · **BR-066** danh mục chế độ ăn · **BR-067** APPROVED/PUBLISHED bất khả sửa · **BR-068** state machine + chỉ School Admin duyệt · **BR-069** khai đủ nguyên liệu/allergen |
| `business_rules.md` §14 · §4 | **BR-156** Gemini đề xuất thực đơn · **BR-201** PH ≤ 5 hồ sơ HS |
| `error_message.md` | Chuỗi assert nguyên văn: MSG-004, MSG-005, MSG-029, MSG-039, MSG-040, MSG-051, MSG-067, MSG-071, MSG-072, MSG-073, MSG-074, MSG-075, MSG-090 |
| `TDS-MF05_Backend.md` §10 | Error code kỹ thuật: `MENU-004/005/051/061/065/066/067/067c/069/073/075/090` |
| `TDS-MF05_Backend.md` §16 | Ma trận 7 role × 13 nhóm endpoint → 14 security TC |
| `TDS-MF05_Backend.md` §17.1 | C1–C12 → mỗi constraint ≥ 1 TC (chống AP-AI-001) |
| `semantic-audit.ts` §11 / §11b | 8 luật: `GUI_DUYET_SAU_NGAY_PHUC_VU`, `DA_DUYET_MA_THIEU_NGUOI_DUYET`, `DA_DUYET_MA_VAN_VUOT_TRAN`, `NOP_DUYET_MA_NHOM_CHE_DO_AN_CHUA_CO_GI_AN`, `SUAT_RIENG_TRO_THUC_DON_KHONG_TON_TAI`, `SUAT_BO_SUNG_KHONG_RO_NGUON_GOC`, `SUAT_RIENG_KHONG_CO_NGUOI_AN`, `BANG_NAU_MON_VUOT_SI_SO` |
| `00_Team_Work_Assignment_Plan.md` §2b-1…§2b-6 | Shape 5 field · clamp `page` · `pageCount ≥ 1` · tập rỗng vẫn 200 · `search` chỉ trên `SEARCH_FIELDS` · `not:field` |
| `ADR-000.md` | Dev profile = stub external service → `AiAssistPort` fake |
| PDPD (Luật 91/2025 — bảo vệ dữ liệu cá nhân) | `MF05-TC-SEC-009` — log/thông báo dị ứng **không** chứa tên/mã HS |

### TDS-03 — Test Conditions and Coverage Items

> Mỗi condition map sang ≥ 1 test case cụ thể. Cột "Constraint" trỏ về `TDS-MF05_Backend.md` §17.1 để chống AP-AI-001.

| Condition ID | Test Condition | Coverage Item | Constraint | Test Cases |
|-------------|---------------|---------------|-----------|-----------|
| `COND-01` | Nộp duyệt phải cách ngày phục vụ ≥ 3 ngày (BR-061) | `MenuWorkflowService.submit()` | C4 | `MF05-TC-001`, `MF05-TC-002`, `MF05-TC-016` |
| `COND-02` | Xung đột dị ứng không có suất thay thế → chặn submit/approve (BR-063) | `AllergyConflictChecker.hasBlockingConflict()` | C3 | `MF05-TC-003`, `MF05-TC-004`, `MF05-TC-INT-006` |
| `COND-03` | `APPROVED`/`PUBLISHED` bất khả sửa/xóa; `DRAFT`/`REJECTED` được sửa (BR-067) | `MenuService.assertEditable()`, `delete()` | C2 | `MF05-TC-005`, `MF05-TC-006`, `MF05-TC-INT-007` |
| `COND-04` | Chỉ School Administration được approve/reject (BR-068) | `MenuWorkflowService.approve()/reject()` + `@PreAuthorize` | C1, C12 | `MF05-TC-007`, `MF05-TC-SEC-003`, `MF05-TC-SEC-008` |
| `COND-05` | Duyệt bắt buộc `approvalSignature`; từ chối bắt buộc `rejectReason` | `approve()` / `reject()` validation | — (UC-006 bước 5 / AF1) | `MF05-TC-008`, `MF05-TC-009` |
| `COND-06` | Món chính không lặp > 2 lần/tuần (MSG-075) | `MenuRepository.countDishOccurrencesInWeek()` + `DishRotationValidator` | C1 | `MF05-TC-010` |
| `COND-07` | Mọi món phải khai đủ nguyên liệu + allergen trước khi nộp (BR-069) | `IngredientDeclarationValidator` | C4 | `MF05-TC-011` |
| `COND-08` | Mọi nhóm chế độ ăn có HS trong khối phải "có gì ăn" (BR-065) | `DietCoverageValidator.findGaps()` | C5 | `MF05-TC-012`, `MF05-TC-018` |
| ~~`COND-09`~~ | ~~`dietaryType` phải thuộc danh mục chuẩn (BR-066)~~ | ⛔ **CHUYỂN CHỦ SANG MF03** (L19/L20) — `mf03/service/SpecialMealRequestServiceImpl` + `StandardDietCatalog`, oracle là **BR-220/MSG-144**, không phải BR-066/MENU-066 | — | ~~`MF05-TC-013`~~ |
| `COND-09b` | `DietCoverageValidator` chặn submit khi còn nhóm chế độ ăn "không có gì ăn" (BR-065) | `DietCoverageValidator.validate()` đọc `SpecialMealLookupPort` (**mock**) | C14 | `MF05-TC-012` |
| `COND-10` | AI lỗi/timeout 10s → MSG-073, **không** chặn lập tay (BR-156, UC-075 AF1) | `MenuRecommendationService.suggest()` | C6 | `MF05-TC-014`, `MF05-TC-015` |
| `COND-11` | `APPROVED`/`PUBLISHED` phải có `approvedBy` | CHECK constraint + `approve()` | C1 | `MF05-TC-019` |
| `COND-12` | Đã duyệt thì `estimatedCost` không được vượt `costCeiling` | `CostCeilingValidator` | — (luật `DA_DUYET_MA_VAN_VUOT_TRAN`) | `MF05-TC-017` |
| `COND-13` | Suất riêng bổ sung (`audience≠ALL`, `PENDING_APPROVAL`) bắt buộc `reason` | `SpecialMenuService` | C5 | `MF05-TC-018`, `MF05-TC-020` |
| `COND-14` | State machine chỉ đi theo BR-068; chuyển sai → 409 | `MenuWorkflowService` FSM | C1 | `MF05-TC-021`, `MF05-TC-022` |
| `COND-15` | Field dẫn xuất `dishes`/`allergyConflicts` tự tính lại, bỏ qua số FE gửi | `MenuMapper` + `MenuService` | C11 | `MF05-TC-023` |
| `COND-16` | Ngày `submittedAt`/`approvedAt`/`date` trả **ISO-8601** (**D4** ghi đè C9); `menuDateIso` ISO | `MenuMapper` (MapStruct) | **D4**, L17 | `MF05-TC-024` |
| `COND-30` | JSON `id` = `code`, PK UUID; DB assertion dùng `WHERE code` (**D1**) | entity + `@JsonProperty("id")` | **D1**, L15 | `MF05-TC-INT-001`, `MF05-TC-025`, `MF05-TC-026` |
| `COND-31` | Lỗi trả trong envelope `{"error":{...}}` (**D2**) | `@RestControllerAdvice` | **D2**, L16 | mọi TC error path |
| `COND-32` | Enum dùng chung ở `common/enums/`, module không khai lại (**D6**) | `MealType`, `AllergySeverity` | **D6**, L18 | `MF05-TC-003`, `MF05-TC-004` |
| `COND-17` | Chấp nhận `id` client gửi, trùng → 409; suất riêng đúng khuôn `{menuId}--{dietGroupId}` | `MenuService.createDraft()` | C10 | `MF05-TC-025`, `MF05-TC-026` |
| `COND-18` | `GET` danh sách trả đúng 5 field, clamp `page`, `pageCount ≥ 1`, rỗng vẫn 200 | `MenuController.list()` + `PagedResponse.of()` | C7 | `MF05-TC-INT-002`, `MF05-TC-INT-003` |
| `COND-19` | `search` chỉ chạy trên `SEARCH_FIELDS`; hỗ trợ `not:field` | `MenuSpec.build()` | C7 | `MF05-TC-INT-004`, `MF05-TC-INT-005` |
| `COND-20` | `PUT` là **partial merge**, không replace | `MenuService.update()` | C8 | `MF05-TC-INT-012` |
| `COND-21` | Parent chỉ thấy `PUBLISHED` của khối con mình, ≤ 5 con (BR-201) | `ParentMenuService.publishedForChild()` | C12 | `MF05-TC-INT-008`, `MF05-TC-INT-009`, `MF05-TC-SEC-005` |
| `COND-22` | Mọi thao tác ghi có audit log kèm actor + IP (BR-170) | `@RestControllerAdvice` + audit aspect | — (BR-170, §4.3 TDS) | `MF05-TC-INT-010` |
| `COND-23` | Không JWT → 401; sai role → 403 trên toàn bộ endpoint | Spring Security filter chain | C12 | `MF05-TC-SEC-001` … `MF05-TC-SEC-007` |
| `COND-24` | `schoolId` lấy từ JWT, bỏ qua query param (chống horizontal escalation) | `SecurityContext` → `MenuSpec` | — (§4.3 TDS) | `MF05-TC-SEC-006` |
| `COND-25` | Caterer chỉ thao tác thực đơn đơn vị mình lập | `created_by_supplier_id` = partnerId trong JWT | C12 | `MF05-TC-SEC-004` |
| `COND-26` | Cảnh báo dị ứng **không** lộ danh tính HS (PDPD) | `AllergyConflictMapper` + logger | — (MSG-051, PDPD) | `MF05-TC-SEC-009` |
| `COND-27` | Áp thực đơn mẫu tự gắn `defaultSubstituteId` của món | `MenuService.applyTemplate()` | C5 | `MF05-TC-INT-011` |
| `COND-28` | Parent xem tuần chưa công bố → MSG-090 | `ParentMenuService` | — (UC-135, MSG-090) | `MF05-TC-INT-009` |
| `COND-29` | Xóa món/mẫu đang được thực đơn tham chiếu → 409 | `DishService.delete()` | — (TDS §9.5 mục 11) | `MF05-TC-INT-013` |
| `COND-30` | Nộp duyệt hàng loạt N ≤ 15 request tuần tự vẫn đúng | `PUT /menus/{id}` ×N | C8 | `MF05-TC-INT-014` |

**Tổng:** 30 condition → 56 test case. Mỗi condition có ≥ 1 TC ⇒ **không có condition mồ côi** (kiểm ở §6 Exit Criteria).

### TDS-04 — Test Techniques / Kỹ thuật Kiểm thử

| Technique (ISO 29119-4) | Applied To | Rationale |
|------------------------|------------|-----------|
| **Boundary Value Analysis** | BR-061 lead-time: `menuDate − now` ∈ {2, **3**, 4} ngày · BR-068 dish rotation: {2, **3**} lần/tuần · `pageSize` ∈ {0, 1, 8} · `page` ∈ {1, pageCount, pageCount+1} | Luật nghiệp vụ nói "ít nhất 3 ngày" và "không quá 2 lần/tuần" → lỗi off-by-one là dạng lỗi có xác suất cao nhất, phải test đúng mốc |
| **Equivalence Partitioning** | `MenuStatus` × thao tác: {DRAFT, REJECTED} = **sửa được**; {APPROVED, PUBLISHED} = **khoá**; {PENDING_APPROVAL} = chỉ School Admin ghi · `dietaryType` ∈ / ∉ `STANDARD_DIETS` | BR-067 chia miền trạng thái thành 2 lớp tương đương rõ rệt — test 1 đại diện mỗi lớp thay vì 5 trạng thái × 4 thao tác |
| **State Transition Testing** | `MenuStatus` FSM: `DRAFT → PENDING_APPROVAL → APPROVED/REJECTED → PUBLISHED` (BR-068, TDS §6.3) — kể cả **transition không hợp lệ** (`DRAFT → PUBLISHED`, `PUBLISHED → DRAFT`) | BR-068 là ràng buộc cứng của module; lỗi FSM cho phép Caterer tự công bố thực đơn chưa duyệt |
| **Decision Table Testing** | Phân quyền **theo field trong body** ở giai đoạn 1 (C12): (role × field gửi) → allow/deny | `PUT /menus/{id}` nhận mọi field; chỉ gác theo path thì Caterer tự duyệt được thực đơn của mình (vi phạm BR-068) — cần bảng quyết định đầy đủ |
| **Error Guessing** | Attack vector: bỏ JWT · đổi `schoolId` trong query · Caterer A sửa menu của Caterer B · Parent truyền `studentId` không phải con mình · gửi `status` kèm `approvedBy` từ role Caterer | Các lỗ hổng IDOR/BOLA thường không xuất hiện trong happy-path spec |
| **Use Case Testing** | E2E trục xương sống: Login Caterer → lập thực đơn Khối 3 → Login School Admin duyệt → Login Parent thấy thực đơn + cảnh báo dị ứng | `00_Team_Work_Assignment_Plan.md` §3 yêu cầu trục xương sống P1 "nối thông bằng ID" |
| **Contract Testing** | Response shape vs `mock-server.ts`: 5 field phân trang + 12 tên field JSON bất biến (C9) | Đổi một chữ tên field là **14 màn FE gãy** — phải có test chặn hồi quy hợp đồng |
| **Data-driven (Parameterized)** | `@ParameterizedTest` cho ma trận 7 role × endpoint (§16) và 5 `MenuStatus` × 4 thao tác | 91 tổ hợp — viết tay từng `@Test` là bất khả thi và dễ sót |

### TDS-05 — Test Data Requirements

> **Nguồn fixture: `mock-data.ts` — dùng ID THẬT của seed, không bịa.** Trường QA = `SCH-001`. Không dùng PII thật.
> Fixture dựng bằng **Java builder trong `MenuTestFixtures`** (§4 Fixture Isolation), seed DB bằng **Flyway test migration** hoặc `@Sql`.

| Fixture ID | Type | Value / Logic | Mục đích |
|-----------|------|---------------|---------|
| `FX-001` | DB seed `menus` | `MENU-2026-28` — `grade="Khối 3"`, `menuDateIso="2026-07-20"`, `status=PENDING_APPROVAL`, `estimatedCost=29200`, `costCeiling=30000`, `submittedAt="10/07/2026"`, `createdBy="Cty TNHH Suất ăn An Bình"`, `allergyConflicts=1` | Menu chờ duyệt — đầu vào của approve/reject, TC-007…TC-009 |
| `FX-002` | DB seed `menus` | `MENU-2026-30` — `status=PUBLISHED`, `approvedBy="Nguyễn Thị Lan (Hiệu phó)"`, `approvalSignature="SIG-2630"`, `approvedAt="29/06/2026 16:20"` | Menu đã công bố — test BR-067 khoá sửa/xóa (TC-005, TC-INT-007) và luồng Parent (TC-INT-008) |
| `FX-003` | DB seed `menus` | `MENU-2026-28C` — `grade="Khối 1"`, `menuDateIso="2026-07-21"`, `status=DRAFT`, `estimatedCost=26400` | Menu nháp — đầu vào submit (TC-001, TC-002) và update (TC-006) |
| `FX-004` | DB seed `menus` | `MENU-2026-1042` (dựng mới) — `status=DRAFT`, `menuDate = today + 3`, `grade="Khối 3"`, `mealType=LUNCH` | Mốc biên BR-061 **đúng 3 ngày** → submit PHẢI thành công (TC-001) |
| `FX-005` | DB seed `menus` | `MENU-2026-1043` — như FX-004 nhưng `menuDate = today + 2` | Mốc biên BR-061 **thiếu 1 ngày** → submit PHẢI bị chặn `MENU-061` (TC-002) |
| `FX-006` | DB seed `students` | `HS-3A-001` — `name="Nguyễn Minh Khang"`, `class="3A"`, `allergies=["Tôm","Cua"]`, `healthStatus="VERIFIED"`, `allergySeverity="Severe"` | HS dị ứng đã xác minh — oracle của BR-063 (TC-003) |
| `FX-007` | DB seed `students` | `HS-3A-004` — `class="3A"`, dị ứng **Tôm** đã `VERIFIED` | HS thứ hai cùng allergen → `affectedStudentCount = 2` trong MSG-051 (TC-003) |
| `FX-008` | DB seed `dishes` | `ca-basa-sot-ca-chua` — `name="Cá basa sốt cà chua"`, `kcal=190`, `isSpecialDish=true`, `defaultSubstituteId="dau-phu-sot-ca-chua"`, `ingredients=[{ING-CA-BASA,"Cá basa phi lê",60,"g"}]` | Món có suất thay thế mặc định — TC-004, TC-INT-011 |
| `FX-009` | DB seed `dishes` | `dau-phu-sot-ca-chua` — `manualAllergens=["Đậu nành"]`, `kcal=170` | Suất thay thế (BR-065) — TC-004 |
| `FX-010` | DB seed `dishes` | `dish-no-bom` (dựng mới) — **không** có dòng nào trong `dish_ingredients` | Vi phạm BR-069 → `MENU-069` (TC-011) |
| `FX-011` | DB seed `diet_groups` | `VEGETARIAN` (`matchDiet="Vegetarian"`), `DIABETIC`, `HALAL`, `NUT_FREE`, `LACTOSE_FREE` — nguyên văn `mock-data.ts:5521` | Danh mục nhóm chế độ ăn — TC-012, TC-018, TC-026 |
| `FX-012` | DB seed `students` | 1 HS `class="3A"`, `specialDiet="Vegetarian"` | Có người ăn nhóm VEGETARIAN → `DietGap` khi thiếu suất riêng (TC-012) |
| `FX-013` | DB seed `menus` | `MENU-2026-1042--VEGETARIAN` — `audience="VEGETARIAN"`, `dietGroupId="VEGETARIAN"`, `servings=6`, `reason=null`, `status=PENDING_APPROVAL` | Suất riêng bổ sung **thiếu `reason`** → 422 (TC-018, luật `SUAT_BO_SUNG_KHONG_RO_NGUON_GOC`) |
| ~~`FX-014`~~ | ⛔ **BỎ** (L19) — `INSERT INTO special_meal_requests` là **ghi vào bảng của MF03**; `SMR-2026-035` đã nằm trong fixture của TestSpec-MF03 | | |
| ~~`FX-015`~~ | ⛔ **BỎ** (L19) — cùng lý do | | |
| `FX-014b` | **Mock** `SpecialMealLookupPort` | `approvedDietsByGrade(SCH-001, "Khối 3", 2026-08-05)` → `[DietGapView("VEGETARIAN", 4 HS), DietGapView("HALAL", 2 HS)]` | Đầu vào của `DietCoverageValidator` (BR-065) mà **không** chạm bảng MF03 — L19 |
| `FX-016` | DB seed `menu_templates` | `TPL-STD-B` — 3 món, `mealType=LUNCH`, `audience="ALL"`, `isActive=true` | Áp mẫu (UC-096 AF) — TC-INT-011 |
| `FX-017` | DB seed `school_config` | `SCH-001` → `costCeiling=30000` | Trần chi phí (TC-017) |
| `FX-018` | DB seed `classrooms` | `3A`, `3B` thuộc `grade="Khối 3"`, tổng sĩ số `320` | Suy `servings` mặc định + sĩ số cho `DietCoverageValidator` |
| `FX-019` | JWT | `{ sub:"caterer-001", role:"SERVICE_CATERERS", schoolId:"SCH-001", partnerId:"SUP-001" }` | Actor hợp lệ của POST/PUT `/menus` |
| `FX-020` | JWT | `{ sub:"caterer-002", role:"SERVICE_CATERERS", schoolId:"SCH-001", partnerId:"SUP-002" }` | Caterer **khác đơn vị** → test cross-tenant (TC-SEC-004) |
| `FX-021` | JWT | `{ sub:"admin-001", role:"SCHOOL_ADMIN", schoolId:"SCH-001" }` | Actor duyệt/từ chối (BR-068) |
| `FX-022` | JWT | `{ sub:"parent-001", role:"PARENT", schoolId:"SCH-001", childIds:["HS-3A-001"] }` | Parent hợp lệ (BR-201) |
| `FX-023` | JWT | `{ sub:"parent-002", role:"PARENT", childIds:["HS-5B-007"] }` | Parent **không phải** phụ huynh HS-3A-001 → IDOR (TC-SEC-005) |
| `FX-024` | JWT | `{ sub:"teacher-001", role:"TEACHER", classId:"3A" }` · `{ role:"MEDICAL_STAFF" }` · `{ role:"SUPPLIER" }` · `{ role:"SYS_ADMIN" }` | Đủ 7 role cho ma trận §16 (TC-SEC-007) |
| `FX-025` | Fake port | `AiAssistPort` fake — profile `test`: trả JSON đúng schema ADR-000 sau 50ms | Happy path UC-075 (TC-015) |
| `FX-026` | Fake port | `AiAssistPort` fake — **delay 11s** (> timeout 10s của `client.ts`) | UC-075 AF1 → `MENU-073` (TC-014) |
| `FX-027` | env | `spring.profiles.active=test`, `SFMS_AI_TIMEOUT_MS=10000`, `SFMS_JWT_SECRET=test-secret-32-chars-minimum-len` | Cấu hình test — timeout đúng mốc hợp đồng §9.0 |
| `FX-028` | Testcontainer | `PostgreSQLContainer("postgres:16-alpine")`, Flyway `V1` (schema Nam) + `V7`/`V8` (MF05) | Nền integration test; timeout khởi động 120s |

**Nguyên tắc cách ly dữ liệu:** mỗi integration test chạy trong `@Transactional` + rollback, **hoặc** `@Sql(scripts="/fixtures/mf05-reset.sql", executionPhase=BEFORE_TEST_METHOD)`. **Không** để test này phụ thuộc bản ghi do test khác tạo (chống AP-AI-002 shared state).

---

## 4. Test Case Specification

> **TC ID format:** `MF05-TC-[NNN]` (unit) · `MF05-TC-SEC-[NNN]` (security) · `MF05-TC-INT-[NNN]` (integration) · `MF05-TC-E2E-[NNN]`
> **Severity:** CRITICAL / HIGH / MEDIUM / LOW (theo CVSS)
> **Status:** 🔴 Not written / 🟡 Written-failing / 🟢 Passing
> **Oracle Source:** bắt buộc — chỉ nhận BR / UC bước / MSG / luật `semantic-audit.ts` / hằng trong mock. **Không nhận** "theo thông lệ", "hợp lý".

### Fixture Isolation Boilerplate (CASE 2.0 — BẮT BUỘC, bản Java)

> ⭐ **CASE 2.0 Rule:** mỗi test PHẢI tạo instance **mới** qua factory. Không shared mutable state giữa các test case — đây là biện pháp chống **AP-AI-002 (Green-from-Birth)**.
> Bản Java tương đương `makeEntity()` của template TypeScript: **static factory + `with*()` override**, đặt trong `src/test/java/com/sfms/menu/MenuTestFixtures.java`.

```java
package com.sfms.mf05;

import com.sfms.mf05.domain.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

/**
 * Fixture factory cho MF05 — giá trị baseline đồng bộ FX-001…FX-018 (§3 TDS-05).
 * MỌI test gọi newMenu()/newDish() để có instance RIÊNG; KHÔNG khai @BeforeAll static Menu.
 *
 * ⚠️ D1 (00_Common_Conventions.md, Approved 2026-07-30 — xem L15):
 *    builder set `code` (mã nghiệp vụ, VD "MENU-2026-30"); `id` là UUID do JPA sinh, KHÔNG set tay.
 *    JSON trả `code` dưới tên `id` qua @JsonProperty("id").
 * ⚠️ D6 (xem L18): MealType / AllergySeverity lấy từ `common/enums/`, module KHÔNG khai lại.
 */
public final class MenuTestFixtures {

    public static final String SCHOOL_ID   = "SCH-001";
    public static final String SUPPLIER_ID = "SUP-001";
    public static final String CATERER     = "Cty TNHH Suất ăn An Bình";

    /** Baseline = FX-003 (MENU-2026-28C, DRAFT, Khối 1). Override bằng Consumer. */
    public static Menu newMenu(Consumer<Menu>... overrides) {
        Menu m = new Menu();
        m.setCode("MENU-TEST-" + java.util.UUID.randomUUID().toString().substring(0, 8)); // D1: code, không set id
        m.setSchoolId(SCHOOL_ID);
        m.setWeekLabel("Thứ 3 — 21/07/2026");
        m.setName("Thực đơn Thứ 3 21/07 — Khối 1");
        m.setGrade("Khối 1");
        m.setMenuDate(LocalDate.now().plusDays(3));        // FX-004 — mốc biên BR-061
        m.setMealType(MealType.LUNCH);
        m.setStatus(MenuStatus.DRAFT);
        m.setAudience("ALL");
        m.setEstimatedCost(new BigDecimal("26400"));
        m.setCostCeiling(new BigDecimal("30000"));         // FX-017
        m.setCreatedBy(CATERER);
        m.setCreatedBySupplierId(SUPPLIER_ID);
        for (Consumer<Menu> o : overrides) o.accept(m);
        return m;
    }

    /** Baseline = FX-008 (ca-basa-sot-ca-chua, có suất thay thế mặc định). */
    public static Dish newDish(Consumer<Dish>... overrides) {
        Dish d = new Dish();
        d.setCode("ca-basa-sot-ca-chua");   // D1: code = slug nghiệp vụ FE đang dùng
        d.setName("Cá basa sốt cà chua");
        d.setCategory(DishCategory.MON_MAN);
        d.setKcal(190);
        d.setActive(true);
        d.setDefaultSubstituteId("dau-phu-sot-ca-chua"); // FX-009
        for (Consumer<Dish> o : overrides) d.accept(d);
        return d;
    }

    public static MenuDish newMenuDish(Menu menu, String dishId, int order, int portions) {
        MenuDish md = new MenuDish();
        md.setMenu(menu);
        md.setDishId(dishId);          // C6: tham chiếu dishId, KHÔNG lưu tên món
        md.setServingOrder(order);
        md.setPortions(portions);
        return md;
    }
}
```

**Kiểm tra tự động chống shared state** (chạy trong CI, §6 Exit Criteria):

```bash
# Không được khai entity ở field tĩnh của test class — mọi instance phải nằm trong @Test hoặc factory
grep -rnE "^\s*(private |)static\s+(final\s+)?(Menu|Dish|MenuTemplate|SpecialMealRequest)\s+" \
     src/test/java/com/sfms/menu/ | grep -v MenuTestFixtures.java
# Expected: no output
```

---

### 4.1. UNIT TEST CASES (service layer)

> Layer: `com.sfms.mf05.service`. Repository + `AiAssistPort` = **Mockito mock**. Không chạm DB.
> Test file mặc định: `src/test/java/com/sfms/menu/service/<Class>Test.java`.

---

#### MF05-TC-001 — Nộp duyệt đúng mốc biên 3 ngày PHẢI thành công

**Severity:** `HIGH`
**Feature Under Test:** `MenuWorkflowService.submit(String menuId, String actor)`
**Test File:** `service/MenuWorkflowServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-01`
**Constraint Ref:** `TDS §17.1 C4`
**Oracle Source:** **BR-061** (`business_rules.md` §7) — *"Thực đơn phải được nộp để phê duyệt **ít nhất 3 ngày** trước tuần sử dụng dự kiến"* → "ít nhất 3" ⇒ **đúng 3 ngày là HỢP LỆ**. MSG-039 = `"Menu draft submitted for approval successfully."`
**Kỹ thuật:** Boundary Value Analysis (mốc dưới, giá trị **on-point**)

**Preconditions:**
- `FX-004` — menu `DRAFT`, `menuDate = today + 3`, mọi món đã khai đủ BOM, không xung đột dị ứng
- `FX-019` — JWT Service Caterers, `partnerId=SUP-001` khớp `created_by_supplier_id`
- Mock `AllergyConflictChecker.hasBlockingConflict()` → `false`; `DietCoverageValidator.findGaps()` → `List.of()`

**Test Steps:**
1. **Arrange:** `Menu m = MenuTestFixtures.newMenu(x -> x.setMenuDate(LocalDate.now().plusDays(3)));` stub `menuRepository.findById` trả `m`.
2. **Act:** `service.submit(m.getId(), "caterer-001")`.
3. **Assert:** trạng thái + mốc thời gian + message.

**Expected Result (PASS — hành vi đúng):**
- `m.getStatus() == MenuStatus.PENDING_APPROVAL`
- `m.getSubmittedAt()` khác `null`
- Response message = `MSG-039` (`"Menu draft submitted for approval successfully."`)
- **Không** ném exception

**Expected Result (FAIL — dấu hiệu lỗi):**
- Ném `LeadTimeViolationException` ⇒ implement dùng `> 3` thay vì `>= 3` (off-by-one BR-061)
- `status` vẫn `DRAFT` ⇒ FSM không chuyển trạng thái

**Current Status:** 🔴 Not written
**Implementation Note:** điều kiện phải là `ChronoUnit.DAYS.between(LocalDate.now(), menuDate) >= 3`. Dùng `Clock` inject được để test không phụ thuộc ngày thật.

---

#### MF05-TC-002 — Nộp duyệt trễ hạn (2 ngày) PHẢI bị chặn MENU-061

**Severity:** `HIGH`
**Feature Under Test:** `MenuWorkflowService.submit()`
**Test File:** `service/MenuWorkflowServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-01`
**Constraint Ref:** `TDS §17.1 C4`
**Oracle Source:** **BR-061** + `TDS-MF05_Backend.md` §10 → code `MENU-061`, HTTP **422**, message *"Thực đơn phải được nộp ít nhất 3 ngày trước ngày phục vụ (BR-061)."*
**Kỹ thuật:** Boundary Value Analysis (giá trị **off-point** ngay dưới mốc)

**Preconditions:**
- `FX-005` — menu `DRAFT`, `menuDate = today + 2`
- `FX-019`

**Test Steps:**
1. **Arrange:** `newMenu(x -> x.setMenuDate(LocalDate.now().plusDays(2)))`.
2. **Act + Assert:** `assertThatThrownBy(() -> service.submit(id, actor))`.

**Expected Result (PASS):**
- Ném `LeadTimeViolationException`
- `exception.getErrorCode() == "MENU-061"`, HTTP mapping **422**
- `menuRepository.save()` **không** được gọi (`verify(menuRepository, never()).save(any())`)
- Trạng thái giữ nguyên `DRAFT`

**Expected Result (FAIL):**
- Submit thành công ⇒ Caterer có thể nộp thực đơn sát ngày, BGH không đủ thời gian duyệt (vi phạm BR-061)

**Current Status:** 🔴 Not written
**Implementation Note:** ném exception **trước** khi ghi DB; đừng set `submittedAt` rồi mới validate.

---

#### MF05-TC-003 — Xung đột dị ứng đã VERIFIED PHẢI chặn nộp duyệt (MENU-051)

**Severity:** `CRITICAL`
**CWE:** `CWE-754 — Improper Check for Unusual or Exceptional Conditions`
**Legal:** PDPD — cảnh báo chỉ dùng `[Number]`, **không** in danh tính HS (xem `MF05-TC-SEC-009`)
**Feature Under Test:** `AllergyConflictChecker.check()` + `MenuWorkflowService.submit()`
**Test File:** `service/AllergyConflictCheckerTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-02`
**Constraint Ref:** `TDS §17.1 C3`
**Oracle Source:** **BR-063** — *"Bắt buộc kiểm tra xung đột dị ứng … xung đột nghiêm trọng → **chặn** nộp"*; **MSG-051** nguyên văn = `"Allergy conflict detected: Dish [Dish Name] contains allergen [Allergen] affecting [Number] students."`; fixture `FX-006`/`FX-007` (2 HS lớp 3A dị ứng **Tôm**, `healthStatus="VERIFIED"`)
**Kỹ thuật:** Equivalence Partitioning (miền "có xung đột chặn" vs "không")

**Preconditions:**
- `FX-006` `HS-3A-001` — `allergies=["Tôm","Cua"]`, `VERIFIED`, `Severe`
- `FX-007` `HS-3A-004` — dị ứng **Tôm**, `VERIFIED`
- Menu `DRAFT` `grade="Khối 3"` chứa món có allergen **Tôm**, món đó **không** có `substitutes`
- `FX-018` — `3A`, `3B` thuộc Khối 3

**Test Steps:**
1. **Arrange:** seed 2 HS trên; menu Khối 3 gồm 1 món `allergens=["Tôm"]`, `substitutes = []`.
2. **Act:** `checker.check(menuId)` rồi `service.submit(menuId, actor)`.
3. **Assert:** danh sách xung đột + exception.

**Expected Result (PASS):**
- `check()` trả **1** `AllergyConflict` với `allergen="Tôm"`, **`affectedStudentCount == 2`**
- `hasBlockingConflict()` == `true`
- `submit()` ném `AllergyConflictException`, `errorCode == "MENU-051"`, HTTP **409**
- Message theo khuôn MSG-051 với `[Number]` = `2`
- Trạng thái giữ `DRAFT`

**Expected Result (FAIL):**
- Submit thành công ⇒ **thực đơn có allergen tới được bàn ăn của HS dị ứng nặng** — rủi ro an toàn cao nhất của module
- `affectedStudentCount == 1` ⇒ chỉ đếm HS đầu tiên, không group theo allergen
- Đếm cả HS `healthStatus != "VERIFIED"` ⇒ chặn oan (BR-063 chỉ tính hồ sơ **đã xác minh**)

**Current Status:** 🔴 Not written
**Implementation Note:** join `AllergyDeclaration(status='VERIFIED')` theo **đúng khối** của menu (không phải toàn trường). Allergen của món = `Dish.allergens ∪ Dish.manualAllergens`.

---

#### MF05-TC-004 — Có suất thay thế thì KHÔNG chặn nộp duyệt

**Severity:** `HIGH`
**Feature Under Test:** `AllergyConflictChecker.hasBlockingConflict()`
**Test File:** `service/AllergyConflictCheckerTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-02`
**Constraint Ref:** `TDS §17.1 C3`
**Oracle Source:** **BR-063** (chỉ *xung đột nghiêm trọng* mới chặn) + **BR-065** (*HS dị ứng đã xác minh → bắt buộc có kế hoạch bữa ăn riêng*) ⇒ có suất thay thế = đã có kế hoạch riêng = **không chặn**. Fixture `FX-008`/`FX-009` (`ca-basa-sot-ca-chua` → `dau-phu-sot-ca-chua`)
**Kỹ thuật:** Equivalence Partitioning (miền đối của TC-003)

**Preconditions:**
- Hệt `MF05-TC-003` **cộng thêm** món có `substitutes = [{ dishId: "dau-phu-sot-ca-chua", allergenIds: ["Cá"] }]` phủ đúng allergen gây xung đột

**Test Steps:**
1. **Arrange:** như TC-003, thêm `MenuDishSubstitute` cho món xung đột.
2. **Act:** `hasBlockingConflict(menuId)` rồi `submit()`.
3. **Assert:**

**Expected Result (PASS):**
- `hasBlockingConflict()` == `false`
- `check()` vẫn trả xung đột đó nhưng `blocking == false` (giữ để hiển thị **warning** MSG-051 cho BGH)
- `submit()` thành công → `PENDING_APPROVAL`

**Expected Result (FAIL):**
- Vẫn chặn ⇒ Caterer đã làm đúng BR-065 mà không nộp được ⇒ nghẽn nghiệp vụ
- `check()` trả rỗng ⇒ BGH mất thông tin cảnh báo khi duyệt

**Current Status:** 🔴 Not written
**Implementation Note:** phân biệt rõ **conflict** (luôn báo) và **blocking conflict** (chặn). Chỉ `blocking` mới ném exception.

---

#### MF05-TC-005 — Sửa thực đơn APPROVED PHẢI bị khoá (MENU-067)

**Severity:** `CRITICAL`
**Feature Under Test:** `MenuService.assertEditable()` / `update()`
**Test File:** `service/MenuServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-03`
**Constraint Ref:** `TDS §17.1 C2`
**Oracle Source:** **BR-067** — *"Thực đơn Approved/Published không thể sửa/xóa; chỉ Draft/Rejected mới được sửa"*; `TDS` §10 → `MENU-067`, HTTP **409**
**Kỹ thuật:** Equivalence Partitioning + State Transition (lớp trạng thái "khoá")

**Preconditions:**
- Menu `status = APPROVED` (và biến thể `PUBLISHED` — `FX-002`)
- `FX-019`

**Test Steps:**
1. **Arrange:** `@ParameterizedTest @EnumSource(names = {"APPROVED","PUBLISHED"})`.
2. **Act + Assert:** `update()` và `delete()` với từng trạng thái.

**Expected Result (PASS):**
- Cả `update()` và `delete()` ném `MenuLockedException`, `errorCode == "MENU-067"`, HTTP **409**
- `menuRepository.save()` / `delete()` **không** được gọi
- Áp cho **mọi role**, kể cả `SYS_ADMIN` (§16: *"BR-067 bất khả sửa với mọi role"*)

**Expected Result (FAIL):**
- Sửa được ⇒ thực đơn đã ký duyệt bị đổi nội dung sau lưng BGH; mất tính bất biến phục vụ thanh tra (TDS §4.2)

**Current Status:** 🔴 Not written
**Implementation Note:** `assertEditable()` gọi ở **đầu** `update()`/`delete()`, **trước** merge field. Ở giai đoạn 1 (FE đổi status qua `PUT`) phải cho phép `PUT` **chỉ chứa** field duyệt của School Admin — xem `MF05-TC-SEC-008`.

---

#### MF05-TC-006 — Sửa thực đơn REJECTED được phép và quay về DRAFT

**Severity:** `MEDIUM`
**Feature Under Test:** `MenuService.update()`
**Test File:** `service/MenuServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-03`
**Constraint Ref:** `TDS §17.1 C2`
**Oracle Source:** **BR-067** (*"chỉ Draft/Rejected mới được sửa"*) + **BR-068** (vòng đời: từ `REJECTED` Caterer sửa rồi nộp lại) + `TDS` §13 hàng `UT-05-06`; **MSG-074** = `"Menu updated successfully."`
**Kỹ thuật:** State Transition Testing

**Preconditions:**
- Menu `status = REJECTED`, `rejectReason = "Thiếu suất riêng cho nhóm ăn chay Khối 3."`

**Test Steps:**
1. **Arrange:** `newMenu(x -> { x.setStatus(MenuStatus.REJECTED); x.setRejectReason("..."); })`.
2. **Act:** `update(id, req, actor)` với `blocksData` mới.
3. **Assert:**

**Expected Result (PASS):**
- Không ném exception; nội dung mới được ghi
- `status` chuyển về `DRAFT`
- `rejectReason` được **giữ lại** (lịch sử) — không xoá, theo TDS §4.2 *"lịch sử duyệt/từ chối lưu vĩnh viễn"*
- Message = MSG-074

**Expected Result (FAIL):**
- Ném `MenuLockedException` ⇒ hiểu sai BR-067, Caterer không sửa được thực đơn bị từ chối ⇒ tắc quy trình
- `rejectReason` bị set `null` ⇒ mất vết từ chối

**Current Status:** 🔴 Not written
**Implementation Note:** không nhầm "được sửa" với "xoá lịch sử". Cân nhắc thêm cột lịch sử nếu cần nhiều vòng từ chối.

---

#### MF05-TC-007 — Chỉ School Administration được duyệt (BR-068)

**Severity:** `CRITICAL`
**CWE:** `CWE-863 — Incorrect Authorization`
**OWASP:** `A01:2021 — Broken Access Control`
**Feature Under Test:** `MenuWorkflowService.approve()`
**Test File:** `service/MenuWorkflowServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-04`
**Constraint Ref:** `TDS §17.1 C1, C12`
**Oracle Source:** **BR-068** — *"chỉ **School Administration** được duyệt/từ chối"*; **MSG-005** = `"Access denied: You do not have permission to access this resource."`; `TDS` §10 → `MENU-005`, HTTP **403**; `TDS` §16 (hàng `POST /menus/{id}/approve`: chỉ ✅ ở cột SCHOOL ADMIN)
**Kỹ thuật:** Decision Table Testing (role × thao tác)

**Preconditions:**
- `FX-001` menu `PENDING_APPROVAL`
- `@ParameterizedTest` với 6 role **không** phải School Admin: `SYS_ADMIN`, `SERVICE_CATERERS`, `MEDICAL_STAFF`, `TEACHER`, `PARENT`, `SUPPLIER`

**Test Steps:**
1. **Arrange:** SecurityContext = role đang xét.
2. **Act + Assert:** `approve()` và `reject()`.

**Expected Result (PASS):**
- Mọi role ≠ `SCHOOL_ADMIN` → `AccessDeniedException`, `MENU-005`, HTTP **403**
- Riêng `SCHOOL_ADMIN` (`FX-021`) → duyệt thành công
- ⚠️ **`SYS_ADMIN` cũng bị 403** — §16 ghi ❌ cho cột SYS ADMIN ở hàng approve/reject (least privilege, không có ngoại lệ "admin làm được tất")

**Expected Result (FAIL):**
- Service Caterers duyệt được ⇒ **tự duyệt thực đơn của chính mình**, phá toàn bộ mục đích BR-068
- `SYS_ADMIN` duyệt được ⇒ lệch §16

**Current Status:** 🔴 Not written
**Implementation Note:** `@PreAuthorize("hasRole('SCHOOL_ADMIN')")` trên method service (không chỉ controller) để test unit bắt được.

---

#### MF05-TC-008 — Duyệt thiếu chữ ký PHẢI bị từ chối (422)

**Severity:** `HIGH`
**Feature Under Test:** `MenuWorkflowService.approve(ApproveMenuRequest)`
**Test File:** `service/MenuWorkflowServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-05`
**Constraint Ref:** `TDS §17.1 C1`
**Oracle Source:** `MF05_Menu_Diet_Management.md` **UC-006 bước 5** (BGH ký xác nhận duyệt) + `TDS` §13 hàng `UT-05-08`; cột `approval_signature` tồn tại trong schema §5.2 và mock luôn gửi `approvalSignature: "SIG-2630"` (§9.3.4-C)
**Kỹ thuật:** Equivalence Partitioning (null / blank / hợp lệ)

**Preconditions:**
- `FX-001` `PENDING_APPROVAL`, `FX-021` School Admin

**Test Steps:**
1. **Arrange:** `@ParameterizedTest @NullAndEmptySource @ValueSource(strings = {"   "})` cho `approvalSignature`.
2. **Act + Assert:** `approve()`.

**Expected Result (PASS):**
- Ném `ValidationException` → HTTP **422**
- `status` giữ `PENDING_APPROVAL`, `approvedAt` vẫn `null`
- Với `approvalSignature = "SIG-2630"` → duyệt thành công, message **MSG-040** = `"Menu approved and published successfully. Notifications sent to parents."`

**Expected Result (FAIL):**
- Duyệt được không chữ ký ⇒ không truy được ai chịu trách nhiệm khi thanh tra (TDS §4.2)

**Current Status:** 🔴 Not written
**Implementation Note:** `@NotBlank` trên DTO là đủ, nhưng vẫn cần test service-level vì FE có thể gọi qua `PUT` (giai đoạn 1) không đi qua DTO đó.

---

#### MF05-TC-009 — Từ chối thiếu lý do PHẢI bị từ chối (422)

**Severity:** `HIGH`
**Feature Under Test:** `MenuWorkflowService.reject(RejectMenuRequest)`
**Test File:** `service/MenuWorkflowServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-05`
**Constraint Ref:** `TDS §17.1 C1`
**Oracle Source:** `MF05_Menu_Diet_Management.md` **UC-006 AF1** (từ chối kèm lý do để Caterer sửa) + `TDS` §9.3.4-D (*"Backend **bắt buộc** `rejectReason` không rỗng → **422** nếu thiếu"*) + `TDS` §13 hàng `UT-05-09`
**Kỹ thuật:** Equivalence Partitioning

**Preconditions:**
- `FX-001` `PENDING_APPROVAL`, `FX-021`

**Test Steps:**
1. **Arrange:** `rejectReason` ∈ {`null`, `""`, `"   "`}.
2. **Act + Assert:** `reject()`.

**Expected Result (PASS):**
- HTTP **422**, `status` giữ `PENDING_APPROVAL`
- Với `rejectReason = "Thiếu suất riêng cho nhóm ăn chay Khối 3."` → `REJECTED`, message **MSG-074**

**Expected Result (FAIL):**
- Từ chối rỗng lý do ⇒ Caterer không biết sửa gì, vòng lặp duyệt vô nghĩa (vi phạm UC-006 AF1)

**Current Status:** 🔴 Not written

---

#### MF05-TC-010 — Món chính lặp 3 lần/tuần PHẢI báo MENU-075

**Severity:** `MEDIUM`
**Feature Under Test:** `DishRotationValidator.validate()` + `MenuRepository.countDishOccurrencesInWeek()`
**Test File:** `service/DishRotationValidatorTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-06`
**Constraint Ref:** `TDS §17.1 C1`
**Oracle Source:** **MSG-075** nguyên văn = `"Dish rotation limit exceeded: [Dish Name] cannot be served more than twice a week."` (`error_message.md` gán về **BR-068**); `traceability_matrix.md` §1.6 UC-096 có MSG-075; `TDS` §10 → `MENU-075`, HTTP **409**
**Kỹ thuật:** Boundary Value Analysis (2 lần = hợp lệ · 3 lần = vi phạm)

**Preconditions:**
- 2 menu cùng tuần, cùng `grade="Khối 3"`, cùng `dishId="ca-basa-sot-ca-chua"`, `status ≠ REJECTED`
- Menu thứ 3 đang lập cũng chứa món đó

**Test Steps:**
1. **Arrange:** stub `countDishOccurrencesInWeek()` → `2` (đã có 2 lần), rồi → `1`.
2. **Act:** `validate(menu)`.
3. **Assert:**

**Expected Result (PASS):**
- Với 2 lần đã có + lần thứ 3 → ném `DishRotationException`, `MENU-075`, HTTP **409**, `[Dish Name]` = `"Cá basa sốt cà chua"`
- Với 1 lần đã có + lần thứ 2 → **không** ném (đúng "twice a week" là hợp lệ)
- Menu `status = REJECTED` **không** được tính vào bộ đếm (theo JPQL `m.status <> 'REJECTED'` — TDS §8.2)

**Expected Result (FAIL):**
- Chặn ở lần thứ 2 ⇒ off-by-one, Caterer không xếp được thực đơn hợp lệ
- Tính cả menu `REJECTED` ⇒ bộ đếm phồng, chặn oan

**Current Status:** 🔴 Not written
**Implementation Note:** phạm vi tuần = `weekStart`/`weekEnd` theo tuần ISO của `menuDate`, lọc theo cả `schoolId` + `grade` (khối khác ăn cùng món không tính).

---

#### MF05-TC-011 — Nộp duyệt khi món thiếu khai báo nguyên liệu → MENU-069

**Severity:** `HIGH`
**Feature Under Test:** `IngredientDeclarationValidator`
**Test File:** `service/IngredientDeclarationValidatorTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-07`
**Constraint Ref:** `TDS §17.1 C4`
**Oracle Source:** **BR-069** — *"Phải khai báo **toàn bộ nguyên liệu và chất gây dị ứng tiềm ẩn** của mọi món trước khi nộp duyệt"*; `TDS` §10 → `MENU-069`, HTTP **422**
**Kỹ thuật:** Equivalence Partitioning

**Preconditions:**
- `FX-010` — món `dish-no-bom` **không** có dòng nào trong `dish_ingredients`
- Menu `DRAFT` chứa món đó

**Test Steps:**
1. **Arrange:** menu gồm 1 món đủ BOM (`FX-008`) + 1 món `FX-010`.
2. **Act + Assert:** `submit()`.

**Expected Result (PASS):**
- Ném `IncompleteDeclarationException`, `MENU-069`, HTTP **422**
- Message nêu **đúng tên món thiếu** (`[Dish Name]`), không phải danh sách toàn bộ món
- `status` giữ `DRAFT`
- Món **có** BOM nhưng `manualAllergens` rỗng thì **KHÔNG** vi phạm (rỗng = "không có allergen", khác với "chưa khai") — phân biệt bằng cờ `allergensDeclared`

**Expected Result (FAIL):**
- Nộp được ⇒ `AllergyConflictChecker` (BR-063) chạy trên dữ liệu khuyết ⇒ **âm tính giả**: không phát hiện xung đột chỉ vì món chưa khai allergen. Đây là lỗi liên hoàn nguy hiểm nhất giữa BR-069 và BR-063.

**Current Status:** 🔴 Not written
**Implementation Note:** cần cột/cờ phân biệt "khai rồi và rỗng" vs "chưa khai". Nếu schema chưa có, ghi vào §2 Logic Issues và báo Mạnh trước khi implement.

---

#### MF05-TC-012 — Nhóm chế độ ăn "không có gì ăn" PHẢI chặn nộp duyệt (MENU-065)

**Severity:** `CRITICAL`
**Feature Under Test:** `DietCoverageValidator.findGaps(grade, menuDate, mealType)`
**Test File:** `service/DietCoverageValidatorTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-08`
**Constraint Ref:** `TDS §17.1 C5`
**Oracle Source:** **BR-065** — *"HS có dị ứng đã xác minh/chế độ ăn đặc biệt → **bắt buộc** Service Caterers tạo **kế hoạch bữa ăn riêng**"*; port nguyên luật **`NOP_DUYET_MA_NHOM_CHE_DO_AN_CHUA_CO_GI_AN`** (`semantic-audit.ts` §11b — chỉ soi menu `audience="ALL"` ở trạng thái `PENDING_APPROVAL`/`APPROVED`/`PUBLISHED`); `TDS` §10 → `MENU-065`, HTTP **422**
**Kỹ thuật:** Equivalence Partitioning + Use Case Testing

**Preconditions:**
- `FX-011` — `DIET_GROUPS` gồm `VEGETARIAN` (`matchDiet="Vegetarian"`, `excludeIngredientKeywords` có `"gà"`, `"cá"`, `"thịt"`, `"nước mắm"`…)
- `FX-012` — ≥ 1 HS lớp 3A có `specialDiet = "Vegetarian"`
- Menu Khối 3, `mealType=LUNCH`, `audience="ALL"`, **toàn món mặn**, **không** tồn tại menu `audience="VEGETARIAN"` cho cùng ngày/buổi

**Test Steps:**
1. **Arrange:** seed như trên.
2. **Act:** `findGaps("Khối 3", menuDate, "LUNCH")` rồi `submit()`.
3. **Assert:**

**Expected Result (PASS):**
- `findGaps()` trả **1** `DietGap`: `group.id == "VEGETARIAN"`, `mealType == "LUNCH"`, `students >= 1`
- `submit()` ném `DietCoverageException`, `MENU-065`, HTTP **422**, message nêu `[Diet Group]` + `[Grade]`
- Khi thêm menu `audience="VEGETARIAN"` cùng ngày/buổi ⇒ `findGaps()` rỗng, submit thành công
- Nhóm chế độ ăn **không có HS nào** trong khối ⇒ **không** tạo gap (khớp luật `SUAT_RIENG_KHONG_CO_NGUOI_AN`: không đòi suất cho nhóm không có người ăn)

**Expected Result (FAIL):**
- Nộp được ⇒ HS ăn chay/dị ứng **không có gì ăn** trong ngày đã công bố — đúng lớp lỗi mà `semantic-audit.ts` được viết ra để bắt
- Tạo gap cho nhóm không có HS ⇒ Caterer phải lập suất rỗng vô nghĩa

**Current Status:** 🔴 Not written
**Implementation Note:** match HS↔nhóm qua `Student.specialDiet == DietGroup.matchDiet` (đúng như mock `dietCoverageGaps`), và khối suy từ `gradeOfClass(student.class)`.

---

#### ~~MF05-TC-013~~ — ⛔ **GỠ KHỎI MF05 (2026-08-03)** — dietaryType ngoài danh mục chuẩn

> **Lý do gỡ:** L19 + L20 (§2.-1) và **M05-D04/D05** của `TDS-MF05_Backend.md` §2.4.
>
> Luật này **thuộc MF03 và đã hiện thực xong**: `mf03/service/SpecialMealRequestServiceImpl` +
> `mf03/service/StandardDietCatalog.java` (danh mục là **cấu hình** `sfms.mf03.standard-diets`).
> Oracle của TC v1.1 cũng **không còn đúng**:
>
> - `MENU-066` **không tồn tại** — `error_message.md` v1.9 dòng 88 ghi rõ *"Nếu MF05 đang dùng
>   MSG-114 thì chuyển sang MSG-144"*, và **MSG-114 đã NGỪNG DÙNG**.
> - Hành vi đúng **không phải từ chối 422** mà là **`NEEDS_INFO` + MSG-144** — vì **BR-066 là danh
>   mục MỞ** (*"và các loại khác"*), không chống lưng được cho việc từ chối. Đây chính là **BR-220**
>   mà Huy cấp ngày 2026-08-02.
>
> ⇒ **L14 đóng** (không còn cần Huy chốt danh mục canonical), và TC này **không viết lại ở MF05**.
> Bản gốc giữ trong lịch sử git; TestSpec-MF03 đã phủ luật này.
>
> **Thứ MF05 vẫn phải test** là `DietCoverageValidator` (BR-065) — đã có ở **`MF05-TC-012`**, nay đọc
> qua `SpecialMealLookupPort` **được mock** (`FX-014b`), **không** chạm bảng `special_meal_requests`.

---

#### MF05-TC-014 — AI timeout 10s → MSG-073 và KHÔNG rollback bản nháp

**Severity:** `HIGH`
**Feature Under Test:** `MenuRecommendationService.suggest()` qua `AiAssistPort`
**Test File:** `service/MenuRecommendationServiceTest.java`
**TDD Phase:** 🔴 RED · ⚠️ **BLOCKED bởi L13 (CF-13)** — endpoint chưa có call-site trong mock; test viết trước theo TDS §9.3.10
**Condition Ref:** `COND-10`
**Constraint Ref:** `TDS §17.1 C6`
**Oracle Source:** **BR-156** (Gemini phân tích dinh dưỡng/dị ứng/tồn kho/trần chi phí để đề xuất) + `MF05_Menu_Diet_Management.md` **UC-075 AF1** (*AI lỗi → vẫn cho lập thủ công*) + **MSG-073** nguyên văn = `"Gemini AI Service is currently unavailable. Please try again later."` + `TDS` §4.1 (*timeout **10s***, khớp `ERROR_LOGS` ERR-5521) + `ADR-M05-03` (fallback bắt buộc)
**Kỹ thuật:** Error Guessing + Boundary Value Analysis (10s là mốc hợp đồng của `client.ts`)

**Preconditions:**
- `FX-026` — `AiAssistPort` fake **delay 11s**
- `FX-027` — `SFMS_AI_TIMEOUT_MS=10000`
- Menu `DRAFT` đã có 2 món do Caterer tự nhập

**Test Steps:**
1. **Arrange:** inject fake port chậm.
2. **Act + Assert:** `suggest(request, actor)`.
3. **Assert phụ:** trạng thái bản nháp sau lỗi.

**Expected Result (PASS):**
- Ném `AiUnavailableException`, `errorCode == "MENU-073"`, HTTP **503**
- Message = MSG-073 nguyên văn
- **Bản nháp KHÔNG bị rollback**: menu vẫn `DRAFT`, vẫn còn **2 món** Caterer đã nhập
- Thao tác lập thủ công tiếp theo (`update()`) vẫn chạy được → đúng UC-075 AF1
- Sự kiện `AiMenuSuggestionRequested` **vẫn** được phát (để MF12 archive theo BR-157)

**Expected Result (FAIL):**
- Ném `TimeoutException` thô / HTTP 500 ⇒ FE không map được message cho người dùng
- Transaction rollback xoá món đã nhập ⇒ Caterer mất việc đang làm chỉ vì AI chết — vi phạm trực tiếp UC-075 AF1
- Không phát event ⇒ MF12 mất log hội thoại (BR-157)

**Current Status:** 🔴 Not written
**Implementation Note:** gọi AI **ngoài** transaction ghi menu (hoặc `REQUIRES_NEW`), timeout ở tầng `AiAssistPort`, **không** nhúng SDK/API key Gemini trong MF05 (ADR-M05-03).

---

#### MF05-TC-015 — AI happy path: đề xuất ưu tiên tồn kho và trong trần chi phí

**Severity:** `MEDIUM`
**Feature Under Test:** `MenuRecommendationService.suggest()`
**Test File:** `service/MenuRecommendationServiceTest.java`
**TDD Phase:** 🔴 RED · ⚠️ BLOCKED bởi L13
**Condition Ref:** `COND-10`
**Constraint Ref:** `TDS §17.1 C6`
**Oracle Source:** **BR-064** — *"Đề xuất AI phải ưu tiên nguyên liệu **đang có trong kho** và tuân thủ **giới hạn chi phí**"*; **BR-156**; **MSG-067** = `"AI menu analysis: Nutritional diversity is below the required 3-group minimum."` (cảnh báo, **không chặn** — `TDS` §10 `MENU-067c` HTTP 422 ở dạng warning payload)
**Kỹ thuật:** Use Case Testing

**Preconditions:**
- `FX-025` — fake port trả JSON đúng schema ADR-000 sau 50ms
- Stub `InventoryQueryPort` → 3 nguyên liệu còn tồn, 1 lô bị cách ly
- `FX-017` — `costCeiling = 30000`

**Test Steps:**
1. **Arrange:** stub tồn kho + trần chi phí.
2. **Act:** `suggest()`.
3. **Assert:** nội dung prompt gửi đi + kết quả trả về.

**Expected Result (PASS):**
- Prompt gửi tới `AiAssistPort` **chứa** danh sách tồn kho và giá trị `costCeiling = 30000` (verify qua `ArgumentCaptor`) — đây là cách kiểm BR-064 ở tầng unit
- Lô **bị cách ly** không xuất hiện trong prompt
- `AiSuggestionResponse` có `nutritionAnalysis`, `ingredients[]`, `allergyFlags[]` (3 thành phần BR-156 yêu cầu hiển thị)
- Nếu đề xuất < 3 nhóm dinh dưỡng → trả **warning** MSG-067, **không** ném exception (BR-062 là cảnh báo)

**Expected Result (FAIL):**
- Prompt không có tồn kho/trần chi phí ⇒ vi phạm BR-064, AI đề xuất món không nấu được
- MSG-067 làm fail cả request ⇒ hiểu sai "cảnh báo" thành "chặn"

**Current Status:** 🔴 Not written

---

#### MF05-TC-016 — Gửi duyệt SAU ngày phục vụ PHẢI bị chặn (422)

**Severity:** `HIGH`
**Feature Under Test:** `MenuWorkflowService.submit()`
**Test File:** `service/MenuWorkflowServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-01`
**Constraint Ref:** `TDS §17.1 C4`
**Oracle Source:** port nguyên luật **`GUI_DUYET_SAU_NGAY_PHUC_VU`** (`semantic-audit.ts` §11: `if (ts(m.submittedAt) > sd) add(...)`) + **BR-061**; `TDS` §13 hàng `UT-05-16`
**Kỹ thuật:** Boundary Value Analysis (miền âm của lead-time)

**Preconditions:**
- Menu `DRAFT`, `menuDate = today − 1` (ngày phục vụ đã qua)

**Test Steps:**
1. **Arrange:** `newMenu(x -> x.setMenuDate(LocalDate.now().minusDays(1)))`.
2. **Act + Assert:** `submit()`.

**Expected Result (PASS):**
- HTTP **422** (`MENU-061` — cùng họ vi phạm lead-time)
- Sau khi chạy toàn bộ test suite, **không** bản ghi nào trong DB có `submitted_at > menu_date` (assert bằng truy vấn ở `MF05-TC-INT-010` / §14.1 TDS)

**Expected Result (FAIL):**
- Nộp được ⇒ dữ liệu tự mâu thuẫn (nộp duyệt bữa ăn đã xảy ra), `semantic-audit.ts` sẽ đỏ ở CI

**Current Status:** 🔴 Not written
**Implementation Note:** đây là **hệ quả** của BR-061 (`>= 3 ngày` đã bao hàm), nhưng vẫn cần test riêng vì trường hợp nhập tay `submittedAt` (CF-10 parse 2 định dạng) có thể lách qua.

---

#### MF05-TC-017 — Duyệt khi vượt trần chi phí PHẢI bị chặn ở bước approve

**Severity:** `HIGH`
**Feature Under Test:** `CostCeilingValidator` trong `MenuWorkflowService.approve()`
**Test File:** `service/CostCeilingValidatorTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-12`
**Constraint Ref:** *(ràng buộc dữ liệu — xem **L2/CF-2**)*
**Oracle Source:** port nguyên luật **`DA_DUYET_MA_VAN_VUOT_TRAN`** (`semantic-audit.ts` §11, kèm comment nguyên văn: *"Vượt trần khi đang PENDING_APPROVAL là **HỢP LỆ** — đó chính là ca School Admin phải thấy cảnh báo. Chỉ sai khi đã duyệt mà vẫn vượt"*) + `TDS` §13 hàng `UT-05-17`
**⚠️ Logic Issue:** **L2** — BR-080 không có trong `traceability_matrix.md` §1.6, nên oracle chính thức của TC này là **luật semantic-audit + cột `cost_ceiling`**, không phải BR-080.
**Kỹ thuật:** Boundary Value Analysis + State Transition (trạng thái × điều kiện chi phí)

**Preconditions:**
- `FX-017` — `costCeiling = 30000`
- 3 biến thể: `estimatedCost` ∈ {`29999`, `30000`, `30001`}

**Test Steps:**
1. **Arrange:** menu `PENDING_APPROVAL` với từng `estimatedCost`.
2. **Act:** giữ `PENDING_APPROVAL` (không assert chặn) → rồi `approve()`.
3. **Assert:**

**Expected Result (PASS):**
- `PENDING_APPROVAL` + `estimatedCost = 30001` → **KHÔNG** ném exception (hợp lệ, chỉ cảnh báo cho BGH)
- `approve()` với `estimatedCost = 30001` → **chặn**, HTTP **422**
- `approve()` với `estimatedCost = 30000` → **thành công** (bằng trần là trong trần)
- `approve()` với `estimatedCost = 29999` → thành công
- `estimatedCost`/`costCeiling` **giữ nguyên số tại thời điểm duyệt** (là cột LƯU, không tính lại — C11)

**Expected Result (FAIL):**
- Chặn ngay lúc nộp ⇒ mất chức năng "BGH thấy cảnh báo rồi quyết định" mà mock cố ý thiết kế
- Chặn ở mốc `= 30000` ⇒ off-by-one
- Duyệt được khi vượt ⇒ luật `DA_DUYET_MA_VAN_VUOT_TRAN` đỏ ở CI

**Current Status:** 🔴 Not written

---

#### MF05-TC-018 — Suất riêng bổ sung không nêu lý do PHẢI bị chặn (422)

**Severity:** `HIGH`
**Feature Under Test:** `SpecialMenuService` / `MenuService.update()` cho menu `audience ≠ "ALL"`
**Test File:** `service/SpecialMenuServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-13`
**Constraint Ref:** `TDS §17.1 C5`
**Oracle Source:** port nguyên luật **`SUAT_BO_SUNG_KHONG_RO_NGUON_GOC`** (`semantic-audit.ts` §11b — 3 nhánh: `PENDING_APPROVAL && !reason` · `APPROVED && !approvedBy` · thực đơn chung còn `DRAFT`/`REJECTED` mà suất riêng lại `PENDING_APPROVAL`) + `TDS` §9.3.4-E (*"bổ sung sau khi thực đơn chung đã công bố → `PENDING_APPROVAL` + **bắt buộc `reason`**"*) + `TDS` §13 hàng `UT-05-18`
**Kỹ thuật:** Decision Table Testing (trạng thái suất riêng × trạng thái thực đơn chung × có/không `reason`)

**Preconditions:**
- `FX-013` — `MENU-2026-1042--VEGETARIAN`, `audience="VEGETARIAN"`, `status=PENDING_APPROVAL`, `reason=null`
- Thực đơn chung `MENU-2026-1042` `status=PUBLISHED`

**Test Steps:**
1. **Arrange:** 3 tổ hợp theo bảng dưới.
2. **Act + Assert:**

| # | Thực đơn chung | Suất riêng | `reason` | Expected |
|---|---|---|---|---|
| a | `PUBLISHED` | `PENDING_APPROVAL` | `null` | **422** |
| b | `PUBLISHED` | `PENDING_APPROVAL` | `"Bổ sung sau khi thực đơn chung đã công bố"` | **OK** |
| c | `DRAFT` | `PENDING_APPROVAL` | bất kỳ | **422** — *"thực đơn còn DRAFT thì suất đi kèm thực đơn, không duyệt riêng"* (nguyên văn luật) |
| d | `PUBLISHED` | `APPROVED` | — nhưng `approvedBy = null` | **422** |

**Expected Result (PASS):** đúng 4 hàng bảng trên; trạng thái không đổi ở các ca 422.

**Expected Result (FAIL):**
- Ca (a) qua ⇒ có suất ăn "từ trên trời" gắn vào thực đơn đã ký, không truy được nguồn gốc
- Ca (c) qua ⇒ suất riêng đi duyệt lẻ trước thực đơn chung, phá thứ tự BR-068

**Current Status:** 🔴 Not written

---

#### MF05-TC-019 — APPROVED/PUBLISHED mà thiếu approvedBy PHẢI bị chặn

**Severity:** `HIGH`
**Feature Under Test:** `MenuWorkflowService.approve()` + CHECK constraint DB
**Test File:** `service/MenuWorkflowServiceTest.java` (unit) · `MenuPersistenceIT.java` (DB constraint)
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-11`
**Constraint Ref:** `TDS §17.1 C1`
**Oracle Source:** port nguyên luật **`DA_DUYET_MA_THIEU_NGUOI_DUYET`** (`semantic-audit.ts` §11: `if ((status==="APPROVED"||status==="PUBLISHED") && !approvedBy)`) + `TDS` §4.2 (*lịch sử duyệt lưu vĩnh viễn phục vụ thanh tra*) + `TDS` §13 hàng `UT-05-15`
**Kỹ thuật:** Error Guessing (ghi trực tiếp trạng thái, bỏ qua service)

**Preconditions:**
- Menu `PENDING_APPROVAL`

**Test Steps:**
1. **Arrange:** cố set `status = APPROVED` (và `PUBLISHED`) mà **không** set `approvedBy`.
2. **Act:** `menuRepository.saveAndFlush(m)` (integration) / `approve()` với `actor` rỗng (unit).
3. **Assert:**

**Expected Result (PASS):**
- Tầng service: HTTP **422** trước khi ghi
- Tầng DB: `DataIntegrityViolationException` do CHECK constraint
  ```sql
  ALTER TABLE menus ADD CONSTRAINT ck_menus_approved_needs_approver
    CHECK (status NOT IN ('APPROVED','PUBLISHED') OR approved_by IS NOT NULL);
  ```
- Khi duyệt hợp lệ: `approvedBy` = nhãn actor, `approvedAt` khác `null`

**Expected Result (FAIL):**
- Ghi được ⇒ thực đơn "tự duyệt", không truy được người chịu trách nhiệm khi có sự cố (liên hệ MF11)

**Current Status:** 🔴 Not written
**Implementation Note:** CHECK constraint đặt trong Flyway `V7__mf05_menu_diet.sql` — **hai lớp bảo vệ** (service + DB), vì giai đoạn 1 FE ghi `status` trực tiếp qua `PUT`.

---

#### MF05-TC-020 — Suất riêng REJECTED khi sửa PHẢI quay về PENDING_APPROVAL

**Severity:** `MEDIUM`
**Feature Under Test:** `MenuService.update()` cho menu `audience ≠ "ALL"`
**Test File:** `service/SpecialMenuServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-13`
**Constraint Ref:** `TDS §17.1 C5`
**Oracle Source:** `TDS` §9.3.4-E — *"Quy tắc trạng thái của suất riêng (`special-meal-dialog.tsx:286–292`): đang `REJECTED` mà sửa → về `PENDING_APPROVAL`; bổ sung sau khi thực đơn chung đã công bố → `PENDING_APPROVAL` + bắt buộc `reason`; còn lại → `DRAFT`"*
**Kỹ thuật:** State Transition Testing

**Preconditions:**
- Suất riêng `MENU-2026-1042--VEGETARIAN`, `status=REJECTED`

**Test Steps:**
1. **Arrange:** suất riêng `REJECTED`, thực đơn chung `PUBLISHED`.
2. **Act:** `update()` đổi `blocksData`.
3. **Assert:**

**Expected Result (PASS):**
- `status` → `PENDING_APPROVAL` (không phải `DRAFT`) — khác với thực đơn chung ở `MF05-TC-006`
- Với thực đơn chung còn `DRAFT` → suất riêng về `DRAFT` (đi kèm thực đơn chung đi duyệt)

**Expected Result (FAIL):**
- Về `DRAFT` khi thực đơn chung đã `PUBLISHED` ⇒ suất riêng "mất tích", không ai duyệt, HS nhóm chế độ ăn không có suất

**Current Status:** 🔴 Not written
**Implementation Note:** đây là điểm **khác biệt cố ý** giữa menu chung và suất riêng — dễ bị implement gộp thành một nhánh rồi sai.

---

#### MF05-TC-021 — Chuyển trạng thái không hợp lệ PHẢI trả 409

**Severity:** `CRITICAL`
**Feature Under Test:** `MenuWorkflowService` FSM
**Test File:** `service/MenuStateMachineTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-14`
**Constraint Ref:** `TDS §17.1 C1`
**Oracle Source:** **BR-068** — *"Quy trình **Draft → Pending Approval → Approved / Rejected → Published**"* + `ADR-M05-04` + `TDS` §6.3 (state machine)
**Kỹ thuật:** State Transition Testing — bảng chuyển trạng thái **đầy đủ** 5×5 (`@ParameterizedTest` × `@MethodSource`)

**Preconditions:** menu ở từng trạng thái nguồn.

**Bảng oracle (transition hợp lệ = ✅, còn lại → 409 `MENU-067`/`IllegalStateTransition`):**

| Từ \ Đến | DRAFT | PENDING_APPROVAL | APPROVED | PUBLISHED | REJECTED |
|---|---|---|---|---|---|
| **DRAFT** | ✅ (sửa) | ✅ submit | ❌ | ⚠️ **xem L8** | ❌ |
| **PENDING_APPROVAL** | ❌ | — | ✅ approve | ⚠️ **L8 — FE đang dùng, chấp nhận giai đoạn 1** | ✅ reject |
| **APPROVED** | ❌ | ❌ | — | ✅ publish | ❌ |
| **PUBLISHED** | ❌ | ❌ | ❌ | — | ❌ |
| **REJECTED** | ✅ (sửa → DRAFT) | ✅ nộp lại | ❌ | ❌ | — |

**Expected Result (PASS):**
- Mọi ô ❌ → ném `IllegalStateTransitionException`, HTTP **409**, trạng thái **không đổi**
- Ô `PENDING_APPROVAL → PUBLISHED` (⚠️): **cho phép** ở giai đoạn 1, backend **tự sinh** `approvedAt` và **ghi WARN log** `"CF-8: FE bỏ qua trạng thái APPROVED"` (assert log qua `LogCaptor`)
- Ô `DRAFT → PUBLISHED` (⚠️): **KHÔNG** cho phép (chưa từng qua duyệt) → 409

**Expected Result (FAIL):**
- `DRAFT → PUBLISHED` qua được ⇒ Caterer tự công bố thực đơn chưa ai duyệt — lỗ hổng nghiệp vụ nặng nhất của FSM
- `PUBLISHED → DRAFT` qua được ⇒ vi phạm BR-067 + tính bất biến §4.2

**Current Status:** 🔴 Not written
**Implementation Note:** khai FSM bằng `EnumMap<MenuStatus, Set<MenuStatus>>` một chỗ, đừng rải `if` trong từng method.

---

#### MF05-TC-022 — Vòng đời đủ 4 bước theo BR-068 (đường nghiêm ngặt)

**Severity:** `HIGH`
**Feature Under Test:** `submit() → approve() → publish()`
**Test File:** `service/MenuStateMachineTest.java`
**TDD Phase:** 🔴 RED · `@Disabled("Chờ CF-8 — Huy/Nam chốt việc FE bỏ qua APPROVED")`
**Condition Ref:** `COND-14`
**Constraint Ref:** `TDS §17.1 C1`
**Oracle Source:** **BR-068** nguyên văn (4 bước) + `TDS` §9.5 mục 2 (*giai đoạn 2: tách `approve` ↔ `publish`*)
**⚠️ Logic Issue:** **L8** — test này mô tả hành vi **đích** (giai đoạn 2). Giữ `@Disabled` để **không** khoá sai một bên khi CF-8 chưa có kết luận (policy §2).

**Test Steps:** `submit()` → assert `PENDING_APPROVAL` → `approve()` → assert `APPROVED` + `approvedAt` → `publish()` → assert `PUBLISHED`.

**Expected Result (PASS):**
- 3 lần chuyển đúng thứ tự, mỗi lần sinh 1 dòng audit (BR-170)
- `approve()` **không** tự động set `PUBLISHED`
- Message tương ứng: MSG-039 → MSG-040

**Expected Result (FAIL):**
- `approve()` set thẳng `PUBLISHED` ⇒ mất bước "đã duyệt nhưng chưa công bố cho phụ huynh", không có cửa sổ để BGH rút lại

**Current Status:** 🔴 Not written (disabled)
**Implementation Note:** khi CF-8 được chốt: bỏ `@Disabled`, và nếu chốt theo BR-068 thì `MF05-TC-021` phải đổi ô ⚠️ thành ❌ **cùng lúc** Minh sửa mock (`00_` §0 chống lệch).

---

#### MF05-TC-023 — Field dẫn xuất PHẢI tự tính lại, bỏ qua số FE gửi

**Severity:** `HIGH`
**Feature Under Test:** `MenuService.createDraft()/update()` + `MenuMapper`
**Test File:** `service/MenuDerivedFieldsTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-15`
**Constraint Ref:** `TDS §17.1 C11`
**Oracle Source:** `ADR-M05-02` (*xung đột dị ứng tính **runtime**, không lưu cột dẫn xuất*) + `data_model.puml` quy ước `/field <<derived>>` (*"KHÔNG tạo cột, tính lúc đọc"*) + `TDS` §9.5 mục 3 (*"Backend **nhận nhưng bỏ qua** giá trị FE gửi, **tự tính lại**, và **trả về** đúng tên field"*) + **L9/CF-9**
**Kỹ thuật:** Contract Testing + Error Guessing (client gửi số sai)

**Preconditions:**
- Payload FE gửi `"dishes": 99`, `"allergyConflicts": 0`
- Thực tế menu có **4** `MenuDish`, và có **1** xung đột dị ứng (`FX-006`)

**Test Steps:**
1. **Arrange:** payload với số dẫn xuất **sai lệch cố ý**.
2. **Act:** `createDraft()` rồi đọc lại `findById()`.
3. **Assert:**

**Expected Result (PASS):**
- Response có `dishes == 4` (đếm thật), `allergyConflicts == 1` (tính runtime) — **không** phải 99 và 0
- **Không** tồn tại cột `dishes` / `allergy_conflicts` trong bảng `menus`:
  ```sql
  SELECT column_name FROM information_schema.columns
   WHERE table_name = 'menus' AND column_name IN ('dishes','allergy_conflicts');
  -- Expected: 0 rows
  ```
- Response **vẫn có đủ** 2 tên field `dishes` và `allergyConflicts` (FE 14 màn đang đọc — C9)
- Ngược lại `estimatedCost`/`costCeiling` **được lưu đúng số FE gửi**, không tính lại (C11)

**Expected Result (FAIL):**
- Trả `dishes: 99` ⇒ FE hiển thị sai số món, và số này lan vào báo cáo MF13
- Tạo cột lưu ⇒ 2 nguồn sự thật, HS khai dị ứng mới không cập nhật được cảnh báo (đúng nhược điểm Option A của ADR-M05-02)
- Bỏ 2 field khỏi response ⇒ 14 màn FE gãy

**Current Status:** 🔴 Not written

---

#### MF05-TC-024 — Định dạng ngày: parse 2 dạng, TRẢ VỀ ISO-8601 (D4 ghi đè C9)

> 🔄 **Viết lại ở v1.1 (2026-07-30).** Bản v1.0 assert response trả `dd/MM/yyyy` theo CF-10/C9. **D4 của `00_Common_Conventions.md` (Approved 30/07) ghi đè** — response phải là ISO-8601. Xem **L17**.

**Severity:** `HIGH`
**Feature Under Test:** `MenuMapper` (MapStruct) + `MenuService.update()`
**Test File:** `mapper/MenuDateFormatTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-16`
**Constraint Ref:** **`00_Common_Conventions.md` D4** (ghi đè `TDS §17.1 C9` phần định dạng ngày)
**Oracle Source:** **D4** nguyên văn — *"Ngày → `"2026-07-29"` (`LocalDate`); Ngày giờ → `"2026-07-29T02:15:00Z"` (`Instant`)"*; *"FE thêm `lib/format.ts` (`formatVnDate`, `formatVnDateTime`) và gọi ở **tầng render** — không lưu chuỗi đã format vào state hay payload"*; và *"**Không sửa seed `mock-data.ts` ở đợt này**"* ⇒ backend vẫn phải **parse** `dd/MM/yyyy` + **L17**
**Kỹ thuật:** Equivalence Partitioning (2 lớp định dạng đầu vào) + Contract Testing

**Preconditions:** payload theo 2 biến thể định dạng.

**Test Steps:**
1. **Act (parse):** `PUT` với `{"submittedAt": "28/07/2026"}` và `{"submittedAt": "2026-07-28T09:00:00Z"}`.
2. **Act (format):** đọc lại qua `GET`.
3. **Assert** định dạng trả về + schema DB.
4. Assert **không còn** `@JsonFormat` trong code.

**Expected Result (PASS):**
- Cả 2 định dạng đầu vào parse thành **cùng một** `Instant` (so theo ngày, timezone `Asia/Ho_Chi_Minh`) — backend **parse khoan dung** vì D4 chưa yêu cầu FE đổi payload ở đợt này
- Response (**ISO-8601 theo D4**): `submittedAt` = `"2026-07-28T09:00:00Z"` · `approvedAt` = `"2026-08-05T03:30:00Z"` · `createdAt` cùng dạng · `date` (nếu là `LocalDate`) = `"2026-07-28"`
- Response: `menuDateIso` = `"2026-08-05"` (**ISO** — không đổi so với v1.0)
- DB: cột `submitted_at` là `timestamptz`, **không** phải `varchar`
- Không còn annotation format ngày:
  ```bash
  grep -rn "@JsonFormat" src/main/java/com/sfms/menu/
  # Expected: no output (D4)
  ```

**Expected Result (FAIL):**
- Trả `dd/MM/yyyy` ⇒ **vi phạm D4** (hợp đồng đã Approved); và khi 12 module khác chuyển ISO thì MF05 thành ngoại lệ duy nhất
- Không parse được `dd/MM/yyyy` ⇒ FE hiện tại gãy ngay (mock vẫn gửi locale `vi-VN`)
- Format `menuDateIso` thành `dd/MM/yyyy` ⇒ FE mất khả năng so sánh/sort ngày, lịch tuần hỏng
- Lưu thẳng chuỗi vào DB ⇒ không lọc được `dateFrom`/`dateTo` (`00_` §2b-2). ⚠️ D4 cảnh báo thêm: `paginate()` của mock parse ngày bằng regex `^(\d{2})\/(\d{2})\/(\d{4})` — khi seed chuyển ISO mà quên sửa chỗ này, lọc sẽ **im lặng trả rỗng**, không lỗi không cảnh báo

**Current Status:** 🔴 Not written
**Implementation Note:** một converter **dùng chung ở `common/`** (Nam sở hữu) cho cả 13 module — không viết `@Named` riêng cho MF05, và đừng dùng `SimpleDateFormat` (không thread-safe). ⚠️ **Việc của Minh:** thêm `lib/format.ts` và gọi ở tầng render; nếu FE vẫn render trực tiếp chuỗi từ API thì sẽ hiện ISO cho người dùng.

---

#### MF05-TC-025 — Chấp nhận id do client gửi, trùng id → 409

**Severity:** `MEDIUM`
**Feature Under Test:** `MenuService.createDraft()`
**Test File:** `service/MenuIdPolicyTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-17`, `COND-30`
**Constraint Ref:** `TDS §17.1 C10` + **`00_Common_Conventions.md` D5 + D1**
**Oracle Source:** `TDS` §9.5 mục 5 + **L11/CF-11** (*"Backend **chấp nhận `id` client gửi** (khớp mock `body.id ?? nextId(...)`), **kiểm trùng → 409**, và **tự sinh** khi vắng"*) + **D5** (*"Nếu body có `id` → dùng làm **`code`**, kiểm trùng → **409** với mã `<MODULE>-409`… Backend **luôn** trả `id` trong response 201"*) — ⚠️ D5 lấy chuẩn **từ chính MF05 CF-11** và áp cho cả 13 module + **D1** (sinh mã qua `code_sequences` + `INSERT … RETURNING`, chống trùng khi 2 request song song)
**Kỹ thuật:** Equivalence Partitioning (id có / vắng / trùng)

**Test Steps:**
1. `POST` với `id = "MENU-2026-1042"` → lần 1.
2. `POST` lại **cùng** `id` → lần 2.
3. `POST` **không** có `id`.

**Expected Result (PASS):**
- Lần 1: **201**, response có `"id": "MENU-2026-1042"`; DB **`code`** = giá trị đó, `id` (UUID) do JPA sinh — **D1/L15**
- Lần 2: **409** (mã `MENU-409` theo **D5**), **không** tạo bản ghi thứ hai, **không** ghi đè bản ghi cũ
- Lần 3: **201** với `code` do server sinh qua `code_sequences`, đúng khuôn `MENU-<year>-<n>`; response **luôn có** `id` (D5 — FE đọc từ response)
- 2 request **song song** không có `id` → 2 `code` **khác nhau** (`INSERT … RETURNING`, không đọc-rồi-ghi — D1)

**Expected Result (FAIL):**
- Server đổi `id` ⇒ FE vừa tạo xong đã không tra được bản ghi (nó dùng `id` tự sinh để điều hướng)
- Lần 2 trả 200 và ghi đè ⇒ **mất trắng** thực đơn cũ khi 2 Caterer bấm cùng lúc (`Date.now()` có thể trùng)

**Current Status:** 🔴 Not written

---

#### MF05-TC-026 — ID suất riêng PHẢI đúng khuôn {menuId}--{dietGroupId}

**Severity:** `HIGH`
**Feature Under Test:** `MenuService.createDraft()` cho `audience ≠ "ALL"`
**Test File:** `service/MenuIdPolicyTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-17`
**Constraint Ref:** `TDS §17.1 C10`
**Oracle Source:** `TDS` §9.3.4-E — *"**Quy ước ID bắt buộc:** `{menuId}--{dietGroupId}` (dấu **2 gạch ngang**). FE dùng chính chuỗi này để tra thực đơn riêng của một ngày (`special-meal-dialog.tsx:275`) — backend **không được** tự sinh ID khác"* + `ADR-M05-01`
**Kỹ thuật:** Contract Testing + Error Guessing (1 gạch / 3 gạch / sai thứ tự)

**Preconditions:** `FX-011` — `dietGroupId ∈ {VEGETARIAN, DIABETIC, HALAL, NUT_FREE, LACTOSE_FREE}`

**Test Steps:**
1. `POST` `{"id":"MENU-2026-1042--VEGETARIAN","audience":"VEGETARIAN","dietGroupId":"VEGETARIAN"}`.
2. `POST` với `id` **sai khuôn**: `"MENU-2026-1042-VEGETARIAN"` (1 gạch).
3. `POST` `audience="VEGETARIAN"` mà **không** gửi `id`.
4. `POST` `id` khớp khuôn nhưng `dietGroupId` **khác** phần sau `--`.

**Expected Result (PASS):**
- (1) **201**, `id` giữ nguyên; `audience == dietGroupId == "VEGETARIAN"`
- (2) **422** — sai khuôn hợp đồng
- (3) **201** với `id` **server tự sinh đúng khuôn** `{menuId}--{dietGroupId}`
- (4) **422** — `id` và `dietGroupId` không nhất quán
- Menu chung (`audience="ALL"`) **không** bị áp khuôn này; `dietGroupId` phải `null`
- Suất riêng trỏ `menuId` **không tồn tại** → **404** (port luật `SUAT_RIENG_TRO_THUC_DON_KHONG_TON_TAI`)

**Expected Result (FAIL):**
- Server sinh id khác khuôn ⇒ `special-meal-dialog` không tra được suất riêng của ngày ⇒ Caterer tạo trùng lặp mỗi lần mở dialog

**Current Status:** 🔴 Not written

---

#### MF05-TC-027 — 🆕 Key ghi ≠ key đọc: `dishes` là SỐ, gửi mảng phải bị bỏ qua (D12)

**Severity:** `HIGH`
**Feature Under Test:** `MenuService.create()` / `update()` — ánh xạ Request DTO ↔ Response DTO
**Test File:** `service/MenuWriteContractTest.java` + `web/MenuContractIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-34` *(mới)*
**Constraint Ref:** `TDS §17.1 C15, C16`
**Oracle Source:** **D12** (`02_Module_Completion_Workflow.md` §6) — *"`GET` trả `class` mà `PATCH` chỉ nhận `classroomId` ⇒ Jackson bỏ field không khớp ⇒ **200 nhưng KHÔNG lưu**. Không lỗi, không cảnh báo — form báo thành công"* + `TDS-MF05_Backend.md` §9.3.1 bảng D12 (**M05-D09**) + **C11** (tự tính lại field dẫn xuất)
**Kỹ thuật:** Contract testing + Negative testing

**Preconditions:** Caterer `U-0022` (có `partner_id=CAT-001` — L26); `blocksData` hợp lệ với 2 món.

**Test Steps:**
1. `POST /api/v1/menus` với body chứa **cả** `blocksData` (2 món) **và** `dishes: [{dishId:"com-trang",portions:320}]` (mảng — sai kiểu).
2. `POST` lần 2 với các field **dẫn xuất**: `allergyConflicts: 99`, `createdBy: "Kẻ mạo danh"`, `approvedAt: "2020-01-01T00:00:00Z"`, `costCeiling: 999999`.
3. Đối chiếu **từng cặp** field của `CreateMenuRequest`/`UpdateMenuRequest` với `MenuResponse`.

**Expected Result (PASS):**
- (1) **201**, `dishes` trong response = **`2`** (số, tính từ `blocksData`), **không** phải mảng; DB có đúng 2 dòng `menu_dishes`
- (2) **201**, DB: `allergy_conflicts` **không tồn tại như cột** (tính runtime — ADR-M05-02); `created_by_supplier_id` = `CAT-001` **lấy từ JWT**, không phải từ body; `approved_at` = `NULL`; `cost_ceiling` = giá trị chép từ `school_config` của MF02, **không** phải `999999`
- (3) **Mọi field sửa được có cùng tên JSON ở cả hai chiều** — danh sách khớp bảng D12 ở `TDS` §9.3.1; nếu có field nào chỉ xuất hiện một phía ⇒ **FAIL và phải sửa DTO**, không được sửa test

**Expected Result (FAIL):**
- `dishes` trả về là mảng FE gửi ⇒ vi phạm **C11/AP-AI-012**, BR-063 mất hiệu lực (số xung đột do FE tự khai)
- `costCeiling`/`createdBy` nhận giá trị từ body ⇒ Caterer **tự nâng trần chi phí của chính mình** và mạo danh đơn vị khác
- Field sửa được lệch tên hai chiều ⇒ đúng lỗi **200-mà-không-lưu** đã tốn của MF03 gần hai phiên làm việc

**Current Status:** 🔴 Not written
**Implementation Note:** viết assertion này **trước** khi implement mapper. Đây là loại lỗi mà `mvn verify` xanh vẫn không phát hiện được nếu chỉ test happy path.

---

### 4.2. SECURITY TEST CASES

> Layer: `com.sfms.mf05.web` qua **MockMvc** + `@WithMockUser` / JWT giả lập.
> Test file mặc định: `src/test/java/com/sfms/menu/web/MenuSecurityTest.java`.
> Oracle phân quyền duy nhất: **`TDS-MF05_Backend.md` §16 Authorization Matrix** (7 role, dẫn từ BR-007 RBAC + BR-068 + BR-201).

---

#### MF05-TC-SEC-001 — Không JWT → 401 trên MỌI endpoint

**Severity:** `CRITICAL`
**OWASP:** `A07:2021 — Identification and Authentication Failures`
**CWE:** `CWE-306 — Missing Authentication for Critical Function`
**Legal:** PDPD — truy cập dữ liệu (kể cả gián tiếp hồ sơ dị ứng) mà không xác thực
**Feature Under Test:** Spring Security filter chain × **26 endpoint** §9.2
**Test File:** `web/MenuSecurityTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-23`
**Oracle Source:** `TDS` §4.3 (*"Mọi endpoint (trừ health) yêu cầu JWT hợp lệ"*) + **MSG-004** = `"Session expired. Please log in again."` + `TDS` §10 → `MENU-004`, HTTP **401**

**Preconditions:** không set header `Authorization`.

**Test Steps (Attack Simulation):**
1. `@ParameterizedTest @MethodSource("allMf05Endpoints")` — 26 endpoint (method + path) từ §9.2.
2. Gọi không header.
3. Kiểm status + body.

**Expected Result (PASS = hệ thống an toàn):**
- **401** cho cả 26 endpoint, body chứa code `MENU-004`
- Header `WWW-Authenticate` có mặt
- **Không** endpoint nào trả 200/403 (403 = đã qua authn, sai lớp)

**Expected Result (FAIL = lỗ hổng tồn tại):**
- Bất kỳ endpoint trả 200 ⇒ thực đơn + cảnh báo dị ứng (PII gián tiếp) đọc được ẩn danh

**Current Status:** 🔴 Not written
**Implementation Note:** `allMf05Endpoints()` sinh từ **một** danh sách hằng dùng chung với `MF05-TC-SEC-007`, để thêm endpoint mới là tự động có test.

---

#### MF05-TC-SEC-002 — Parent gọi POST /menus → 403

**Severity:** `CRITICAL`
**OWASP:** `A01:2021 — Broken Access Control`
**CWE:** `CWE-862 — Missing Authorization`
**Feature Under Test:** `MenuController#create`
**Test File:** `web/MenuSecurityTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-23`
**Oracle Source:** `TDS` §16 hàng `POST /menus (UC-020)` — chỉ ✅ ở cột **SERVICE CATERERS**, ❌ ở 6 cột còn lại + **MSG-005** → `MENU-005`, HTTP **403**

**Test Steps:** `FX-022` (Parent) → `POST /api/v1/menus` với payload hợp lệ.

**Expected Result (PASS):** **403**, code `MENU-005`, `menus` table **không** tăng bản ghi.
**Expected Result (FAIL):** 201 ⇒ phụ huynh tự lập thực đơn cho trường.

**Current Status:** 🔴 Not written

---

#### MF05-TC-SEC-003 — Teacher gọi POST /menus/{id}/approve → 403

**Severity:** `CRITICAL`
**OWASP:** `A01:2021` · **CWE:** `CWE-863 — Incorrect Authorization`
**Feature Under Test:** `MenuController#approve`
**Test File:** `web/MenuSecurityTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-04`, `COND-23`
**Oracle Source:** **BR-068** (*chỉ School Administration duyệt/từ chối*) + `TDS` §16 hàng approve/reject

**Test Steps:** `FX-024` (Teacher) → `POST /api/v1/menus/MENU-2026-28/approve`.

**Expected Result (PASS):** **403** `MENU-005`; DB `status` giữ `PENDING_APPROVAL`, `approved_by` vẫn `NULL`.
**Expected Result (FAIL):** duyệt được ⇒ bất kỳ giáo viên công bố được thực đơn toàn trường.

**Current Status:** 🔴 Not written

---

#### MF05-TC-SEC-004 — Caterer A sửa thực đơn của Caterer B → 403 (cross-tenant)

**Severity:** `CRITICAL`
**OWASP:** `A01:2021 — Broken Access Control (BOLA)`
**CWE:** `CWE-639 — Authorization Bypass Through User-Controlled Key`
**Feature Under Test:** `MenuService.update()` — kiểm `created_by_supplier_id` vs `partnerId` trong JWT
**Test File:** `web/MenuSecurityTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-25`
**Oracle Source:** `TDS` §4.3 (*"Service Caterers chỉ thao tác thực đơn do **đơn vị mình** lập (`created_by_supplier_id` = partnerId trong JWT)"*) + `TDS` §16 (`✅ Own`) + `TDS` §13 hàng `SEC-05-04`

**Preconditions:** `FX-001` có `created_by_supplier_id = "SUP-001"`; `FX-020` = JWT `partnerId = "SUP-002"`.

**Test Steps (Attack Simulation):**
1. Đăng nhập bằng `FX-020`.
2. `PUT /api/v1/menus/MENU-2026-28` với `{"name":"đã bị đổi"}`.
3. Kiểm response + DB.

**Expected Result (PASS):**
- **403** `MENU-005`
- DB: `name` **không** đổi
- Áp cho cả `DELETE` và `POST /submit` của menu không thuộc đơn vị mình
- `GET` cùng menu: theo §16 hàng đầu, Caterer được đọc `Own+School` ⇒ **200 vẫn hợp lệ** (chỉ ghi mới bị chặn) — test khẳng định rõ ranh giới này để không over-block

**Expected Result (FAIL):**
- 200 ⇒ nhà cung cấp cạnh tranh sửa/xoá thực đơn của nhau; audit trail vô nghĩa

**Current Status:** 🔴 Not written

---

#### MF05-TC-SEC-005 — Parent xem studentId không phải con mình → 403 (BR-201)

**Severity:** `CRITICAL`
**OWASP:** `A01:2021 (IDOR)` · **CWE:** `CWE-639`
**Legal:** PDPD — truy cập dữ liệu sức khỏe/chế độ ăn của trẻ khác
**Feature Under Test:** `ParentMenuService.publishedForChild()` *(bước `GET /special-meals` chuyển sang TestSpec-MF03 — L19)*
**Test File:** `web/ParentAccessSecurityTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-21`
**Oracle Source:** **BR-201** — *"Một tài khoản phụ huynh liên kết tối đa **5 hồ sơ học sinh**"* → chỉ đọc được trong tập đã liên kết + `TDS` §16 hàng `GET /parent/menus` (`✅ Child (≤5, BR-201)`) + §4.3

**Preconditions:** `FX-023` — Parent có `childIds=["HS-5B-007"]`, **không** có `HS-3A-001`.

**Test Steps (Attack Simulation):**
1. `GET /api/v1/parent/menus?studentId=HS-3A-001`.
2. ~~`GET /api/v1/special-meals?studentId=HS-3A-001`~~ → **chuyển sang MF03** (L19); thay bằng `GET /api/v1/menus?grade=Khối 3` với vai trò Parent để kiểm **D11** (không lộ field giá — xem `TC-SEC-015`).
3. Thử liên kết con thứ **6** (biên BR-201).

**Expected Result (PASS):**
- (1)(2) → **403** `MENU-005`; response **không** chứa bất kỳ dữ liệu của `HS-3A-001` (kể cả tên, allergen)
- (3) → **422** khi vượt 5 hồ sơ
- Với `FX-022` (Parent hợp lệ của `HS-3A-001`) → 200

**Expected Result (FAIL):**
- 200 với dữ liệu HS khác ⇒ rò rỉ thông tin dị ứng/sức khỏe của trẻ — sự cố PDPD phải báo cáo
- Lọc ở FE mà backend trả full ⇒ vẫn là rò rỉ (đúng cảnh báo `TDS` §9.5 mục 9: *"đây là lỗ hổng, không phải tùy chọn"*)

**Current Status:** 🔴 Not written

---

#### MF05-TC-SEC-006 — Truyền schoolId khác trong query PHẢI bị bỏ qua

**Severity:** `CRITICAL`
**OWASP:** `A01:2021` · **CWE:** `CWE-639`
**Feature Under Test:** `MenuController.list()` → `MenuSpec` lấy `schoolId` từ `SecurityContext`
**Test File:** `web/MenuSecurityTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-24`
**Oracle Source:** `TDS` §4.3 — *"`schoolId` lấy từ JWT, **không** tin query param"* + `TDS` §13 hàng `SEC-05-06`

**Preconditions:** seed menu của `SCH-001` và của `SCH-002`; JWT `schoolId=SCH-001`.

**Test Steps (Attack Simulation):**
1. `GET /api/v1/menus?schoolId=SCH-002&pageSize=200`.
2. `GET /api/v1/menus?school_id=SCH-002` (biến thể snake_case).
3. `GET /api/v1/menus/{id-của-SCH-002}`.

**Expected Result (PASS):**
- (1)(2) → **200** nhưng `items` **chỉ** chứa menu của `SCH-001`; `total` khớp số bản ghi của `SCH-001`
- (3) → **404** (không phải 403 — tránh lộ sự tồn tại của tài nguyên trường khác)
- `schoolId` **không** bị coi là filter field hợp lệ trong `Map<String,String> allParams`

**Expected Result (FAIL):**
- Trả menu của `SCH-002` ⇒ rò rỉ chéo trường (multi-tenant escalation), nghiêm trọng nhất khi hệ thống chạy nhiều trường

**Current Status:** 🔴 Not written
**Implementation Note:** thêm `schoolId` vào **danh sách key bị loại** cùng nhóm `CONTROL` của `MenuSpec` (`00_` §2b-2), để không ai lọc theo nó từ query.

---

#### MF05-TC-SEC-007 — Ma trận 7 role × 26 endpoint khớp §16 100%

**Severity:** `CRITICAL`
**OWASP:** `A01:2021` · **CWE:** `CWE-863`
**Feature Under Test:** toàn bộ controller MF05
**Test File:** `web/MenuAuthorizationMatrixTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-23`
**Oracle Source:** **`TDS-MF05_Backend.md` §16** — bảng 13 hàng × 7 cột, dẫn từ **BR-007** (RBAC 7 role), **BR-068**, **BR-201**
**Kỹ thuật:** Data-driven / Parameterized (182 tổ hợp)

**Test Steps:**
1. Khai bảng §16 thành `Map<Endpoint, Set<Role>>` **trong test** (nguồn oracle nằm trong test, không đọc từ production code — tránh tautology AP-AI-002).
2. `@ParameterizedTest` chạy mọi (role × endpoint).
3. Assert: role ∈ allowed → **không** 403; role ∉ allowed → **403**.

**Expected Result (PASS):**
- 182/182 tổ hợp khớp §16
- Báo cáo cuối test in bảng lệch (nếu có) dạng `role | endpoint | expected | actual`
- `SUPPLIER` bị ❌ ở **toàn bộ** endpoint MF05 (cột cuối §16 toàn ❌)

**Expected Result (FAIL):**
- Bất kỳ ô lệch ⇒ ghi vào bảng §8 anti-pattern và **chặn** Nam final approve (§6 Exit Criteria)

**Current Status:** 🔴 Not written
**Implementation Note:** nếu §16 thay đổi, **sửa bảng trong test trước** (đỏ) rồi mới sửa `@PreAuthorize` — đúng thứ tự TDD.

---

#### MF05-TC-SEC-008 — Phân quyền THEO FIELD trong body (giai đoạn 1)

**Severity:** `CRITICAL`
**OWASP:** `A01:2021 — Broken Access Control` · **CWE:** `CWE-863`
**Feature Under Test:** `MenuController#update` — kiểm quyền theo **field** trong payload
**Test File:** `web/MenuFieldLevelAuthorizationTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-04`, `COND-20`
**Constraint Ref:** `TDS §17.1 C12`
**Oracle Source:** `TDS` **§16 ghi chú cảnh báo** nguyên văn: *"Nếu chỉ gác ở mức 'role nào được gọi `PUT /menus`' thì Service Caterers **tự duyệt được thực đơn của chính mình** — vi phạm BR-068"* + `TDS` §17.1 **C12** + **BR-068**
**Kỹ thuật:** Decision Table Testing

**Preconditions:** `FX-001` `PENDING_APPROVAL`; `FX-019` Caterer chủ sở hữu; `FX-021` School Admin.

**Bảng quyết định (oracle §16 ghi chú):**

| # | Role | Field trong body | Expected |
|---|---|---|---|
| a | SERVICE_CATERERS (Own) | `{status:"PENDING_APPROVAL", submittedAt}` | **200** |
| b | SERVICE_CATERERS (Own) | `{status:"APPROVED", approvedBy, approvalSignature}` | **403** ← ca tấn công chính |
| c | SERVICE_CATERERS (Own) | `{status:"PUBLISHED", approvedBy}` | **403** |
| d | SERVICE_CATERERS (Own) | `{status:"REJECTED", rejectReason}` | **403** |
| e | SCHOOL_ADMIN | `{status:"PUBLISHED", approvedBy, approvalSignature}` | **200** |
| f | SCHOOL_ADMIN | `{status:"REJECTED", rejectReason}` | **200** |
| g | SCHOOL_ADMIN | `{blocksData:[…], name:"…"}` (sửa nội dung) | **403** — nội dung chỉ Caterer sửa |
| h | SERVICE_CATERERS (Own) | `{blocksData:[…]}` khi menu `PUBLISHED` | **409** `MENU-067` (BR-067) |

**Expected Result (PASS = hệ thống an toàn):** đúng 8 hàng; ở mọi ca 403/409, DB **không** thay đổi field nào.

**Expected Result (FAIL = lỗ hổng tồn tại):**
- Ca (b)/(c) trả 200 ⇒ **Service Caterers tự duyệt và công bố thực đơn của chính mình** — vô hiệu hoá toàn bộ quy trình kiểm soát BR-068. Đây là lỗ hổng nghiêm trọng nhất mà giai đoạn 1 (`PUT` tự do) mở ra.

**Current Status:** 🔴 Not written
**Implementation Note:** tách danh sách field theo nhóm chủ sở hữu: `CATERER_FIELDS`, `APPROVER_FIELDS`, `SUBMIT_FIELDS`. Từ chối request có field thuộc nhóm ngoài quyền của role — **trước** khi merge. Khi chuyển sang giai đoạn 2 (action endpoint), test này vẫn giữ để chặn hồi quy.

---

#### MF05-TC-SEC-009 — Cảnh báo dị ứng KHÔNG được lộ danh tính học sinh

**Severity:** `HIGH`
**OWASP:** `A09:2021 — Security Logging and Monitoring Failures`
**CWE:** `CWE-532 — Insertion of Sensitive Information into Log File`
**Legal:** **PDPD (Luật 91/2025)** — dữ liệu sức khỏe của trẻ; `TDS` §1 Compliance Scope (*"không lộ danh tính HS ra thực đơn công bố"*)
**Feature Under Test:** `AllergyConflictMapper` + logger của `AllergyConflictChecker`
**Test File:** `web/AllergyPrivacyTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-26`
**Oracle Source:** **MSG-051** nguyên văn dùng `[Number] students` — **không** có placeholder tên HS + `TDS` §14.2 (*"log KHÔNG chứa tên/mã học sinh cụ thể trong cảnh báo dị ứng"*, expected: no output)

**Preconditions:** `FX-006` (`HS-3A-001` / `"Nguyễn Minh Khang"`), menu có xung đột.

**Test Steps (Attack Simulation):**
1. Chạy `check(menuId)` và `submit()` (ném MENU-051).
2. Bắt log bằng `LogCaptor`.
3. Bắt response body của cả `GET /menus/{id}` và lỗi 409.

**Expected Result (PASS):**
- Log **không** chứa `"HS-3A-001"` lẫn `"Nguyễn Minh Khang"`
- Response 409 và `allergyConflicts` chỉ có **số lượng** + tên allergen + tên món, **không** danh sách HS
- Endpoint `GET /menus/{id}/allergy-conflicts` (§9.2 #26) cũng chỉ trả số đếm cho role Caterer/SchoolAdmin
- Kiểm tự động (đưa vào CI):
  ```bash
  grep -rE "HS-[0-9]+[A-Z]-[0-9]+" logs/app-test.log
  # Expected: no output
  ```

**Expected Result (FAIL):**
- Log/response có mã hoặc tên HS ⇒ nhà cung cấp suất ăn biết đích danh trẻ nào dị ứng gì — vượt phạm vi cần biết, sự cố PDPD

**Current Status:** 🔴 Not written
**Implementation Note:** nếu nghiệp vụ **cần** danh sách HS (ví dụ Y tế), tạo endpoint riêng có phân quyền `MEDICAL_STAFF` và ghi vào §16 trước — không nới lỏng payload chung.

---

#### MF05-TC-SEC-010 — SUPPLIER bị chặn toàn bộ endpoint MF05

**Severity:** `HIGH`
**OWASP:** `A01:2021` · **CWE:** `CWE-862`
**Feature Under Test:** toàn bộ controller MF05
**Test File:** `web/MenuAuthorizationMatrixTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-23`
**Oracle Source:** `TDS` §16 — cột **SUPPLIER** là ❌ ở **cả 13 hàng**

**Test Steps:** `FX-024` role `SUPPLIER` → gọi lần lượt 26 endpoint.

**Expected Result (PASS):** **403** ở toàn bộ 26 endpoint (401 nếu không có JWT — đã cover TC-SEC-001).
**Expected Result (FAIL):** bất kỳ 200 ⇒ NCC nguyên liệu đọc/sửa được thực đơn — lệch §16.

**Current Status:** 🔴 Not written

---

#### ~~MF05-TC-SEC-011~~ — ⛔ **CHUYỂN CHỦ SANG MF03 (2026-08-03)** — chỉ Medical Staff được duyệt yêu cầu bữa ăn đặc biệt

> **Lý do:** L19 (§2.-1) + **M05-D04**. Luật này **đã được hiện thực và gác đúng** trong code đã merge:
> `mf03/web/SpecialMealRequestController.java` — `POST /{id}/approve` và `POST /{id}/reject` đều mang
> `@PreAuthorize("hasRole('MEDICAL_STAFF')")`, `POST` (gửi yêu cầu) mang `hasRole('PARENT')`.
>
> ⚠️ Đồng thời **verb trong TC v1.1 đã sai**: code thật dùng **`POST /special-meals/{id}/approve|reject`**
> (action endpoint), **không** phải `PUT /special-meals/{id}`. Đúng cảnh báo ở workflow §7 Bước 3 mục 3
> — *"CÓ BA LOẠI verb, đừng mặc định PATCH/PUT"*.
>
> ⇒ Test RBAC cho resource này thuộc **TestSpec-MF03** (Huy). MF05 không viết lại.
>
> **Thay thế trong MF05:** `MF05-TC-SEC-015` (mới, L24) — kiểm **D11**: Parent/Teacher gọi `GET /menus`
> không nhận được field giá.

---

#### MF05-TC-SEC-015 — 🆕 Parent/Teacher KHÔNG thấy field giá trong `GET /menus` (D11)

**Severity:** `HIGH`
**OWASP:** `A01:2021 — Broken Access Control` · **CWE:** `CWE-213 — Exposure of Sensitive Information Due to Incompatible Policies`
**Feature Under Test:** `MenuController.list()` — hai shape `MenuResponse` / `MenuPublicResponse`
**Test File:** `web/MenuRoleShapeSecurityTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-33` *(mới)*
**Constraint Ref:** `TDS §17.1 C15`
**Oracle Source:** **D11** (`02_Module_Completion_Workflow.md` §6) + `TDS-MF05_Backend.md` §9.3.1 khối *"D11 — hai shape theo vai trò"* (**M05-D08**) + §9.5 mục 9 (*"lọc theo role ở server là **lỗ hổng, không phải tùy chọn**"*)
**Kỹ thuật:** Equivalence Partitioning (2 lớp vai trò) + Contract test trên `/v3/api-docs`

**Preconditions:** tài khoản seed thật — `U-0005` (parent), `U-0004` (teacher), `U-0001` (school-admin); menu `PUBLISHED` và menu `DRAFT` cùng khối.

**Test Steps:**
1. Đăng nhập từng vai trò (mật khẩu `P@ssw0rd123`), gọi `GET /api/v1/menus?grade=Khối 3`.
2. Đọc `/v3/api-docs`, lấy schema của response `GET /menus`.

**Expected Result (PASS):**
- Parent/Teacher: **không** có key `estimatedCost`, `costCeiling`, `createdBySupplierId`, `approvalSignature`, `rejectReason` trong bất kỳ phần tử `items` nào; **mọi** phần tử có `status = "PUBLISHED"`
- School Admin: có đủ 5 field trên
- `/v3/api-docs` khai `oneOf` gồm **`PagedResponseMenuResponse`** và **`PagedResponseMenuPublicResponse`**, `items` **đúng kiểu** (không phải `{}`), và **giữ tiền tố `PagedResponse`** (S7 — `OpenApiMetaContractIT`)

**Expected Result (FAIL):**
- Parent thấy `estimatedCost`/`costCeiling` ⇒ lộ **giá thoả thuận giữa trường và đơn vị cung cấp** cho toàn bộ phụ huynh
- `items` là `{}` trong spec ⇒ `npm run gen:api` sinh type vô dụng, FE lại phải curl tay từng vai trò như đợt MF03

**Current Status:** 🔴 Not written

---

#### MF05-TC-SEC-012 — Rate limit endpoint AI (5 req/min)

**Severity:** `MEDIUM`
**OWASP:** `A04:2021 — Insecure Design` · **CWE:** `CWE-770 — Allocation of Resources Without Limits`
**Feature Under Test:** `POST /menus/{id}/ai-suggest`
**Test File:** `web/RateLimitTest.java`
**TDD Phase:** 🔴 RED · ⚠️ BLOCKED bởi L13
**Condition Ref:** `COND-10`
**Oracle Source:** `TDS` §9.2 #24 — cột `Rate limit` = **`5/min`** (in đậm trong TDS, khác các endpoint khác) + `BR-157` (mọi lần gọi AI đều archive → chi phí thật)

**Test Steps:** gọi 6 lần trong 60s bằng cùng JWT.

**Expected Result (PASS):**
- Lần 1–5 → 200/503 (tuỳ AI), lần 6 → **429 Too Many Requests** kèm `Retry-After`
- Bộ đếm theo **user**, không theo IP (nhiều Caterer sau cùng NAT)
- Các endpoint khác vẫn theo giới hạn §9.2 (`300/60min` cho GET, `60/min` cho POST/PUT, `30/min` cho DELETE)

**Expected Result (FAIL):**
- Không giới hạn ⇒ 1 vòng lặp FE làm cạn quota Gemini của cả trường (chi phí thật) và làm chậm mọi endpoint khác

**Current Status:** 🔴 Not written

---

#### MF05-TC-SEC-013 — Payload injection qua field lọc động không được chạm cột lạ

**Severity:** `HIGH`
**OWASP:** `A03:2021 — Injection` · **CWE:** `CWE-89 / CWE-943 — Improper Neutralization in Data Query Logic`
**Feature Under Test:** `MenuSpec.build(search, allParams)` — lọc `<field>=<value>` và `not:<field>=<value>`
**Test File:** `web/MenuSpecInjectionTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-19`, `COND-24`
**Oracle Source:** `00_Team_Work_Assignment_Plan.md` §2b-2 (danh sách param hợp lệ + nhóm `CONTROL`) + §2b-4 (mẫu `Specification` dùng `root.get(key)`) + `TDS` §4.3

**Test Steps (Attack Simulation):**
1. `GET /menus?approvalSignature=SIG-2630` (lọc theo cột nội bộ).
2. `GET /menus?nonExistentColumn=x` (cột không tồn tại).
3. `GET /menus?search=' OR '1'='1` (SQLi cổ điển).
4. `GET /menus?not:schoolId=SCH-001` (phủ định trên cột tenant).
5. `GET /menus?password=x` (cột bảng khác).

**Expected Result (PASS):**
- (2)(5) → **400 Bad Request** (field không thuộc allow-list của entity), **không** phải 500 `IllegalArgumentException` thô từ `root.get()`
- (3) → **200**, `items` rỗng hoặc không khớp; **không** trả toàn bảng; câu SQL dùng **parameter binding** (assert bằng log `org.hibernate.SQL` không chứa chuỗi tấn công nội suy)
- (4) → `schoolId` bị bỏ qua (đã cover TC-SEC-006), kết quả vẫn chỉ trong `SCH-001`
- (1) → tuỳ quyết định: nếu cho lọc thì **chỉ** role có quyền đọc field đó; mặc định khuyến nghị **400** (allow-list)

**Expected Result (FAIL):**
- 500 với stacktrace ⇒ lộ tên cột/schema (information disclosure)
- Lọc được `schoolId` ⇒ escalation chéo trường

**Current Status:** 🔴 Not written
**Implementation Note:** dựng **allow-list field lọc** cho từng entity (đừng tin `root.get(key)` bắt lỗi hộ); bắt `IllegalArgumentException` của Criteria API → map 400.

---

#### MF05-TC-SEC-014 — Menu PUBLISHED bất khả sửa với MỌI role, kể cả SYS_ADMIN

**Severity:** `CRITICAL`
**OWASP:** `A01:2021` · **CWE:** `CWE-284 — Improper Access Control`
**Feature Under Test:** `MenuService.assertEditable()` × 7 role
**Test File:** `web/MenuImmutabilityTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-03`
**Oracle Source:** `TDS` §16 ghi chú — *"**BR-067:** APPROVED/PUBLISHED **bất khả sửa với mọi role**"* + `TDS` §4.2 (*thực đơn `PUBLISHED` **bất biến** — sửa = tạo bản mới*)
**Kỹ thuật:** Error Guessing (dùng role cao nhất để lách luật nghiệp vụ)

**Test Steps:** `FX-002` (`PUBLISHED`) → `PUT` và `DELETE` bằng lần lượt 7 role, gồm `SYS_ADMIN`.

**Expected Result (PASS):**
- `SYS_ADMIN` cũng nhận **409** `MENU-067` (không phải 200, không phải 403) — phân biệt rõ: đây là ràng buộc **nghiệp vụ**, không phải ràng buộc quyền
- DB: `updated_at` **không** đổi
- Kiểm bất biến toàn cục (§14.1 TDS):
  ```sql
  SELECT id FROM menus WHERE status='PUBLISHED' AND updated_at > approved_at;
  -- Expected: 0 rows
  ```

**Expected Result (FAIL):**
- `SYS_ADMIN` sửa được ⇒ thực đơn đã công bố cho phụ huynh bị đổi âm thầm; mất chứng cứ khi điều tra sự cố (MF11 truy vết `Menu` → `dishId` → lô)

**Current Status:** 🔴 Not written

---

### 4.3. INTEGRATION TEST CASES

> Nền: `@SpringBootTest(webEnvironment = RANDOM_PORT)` + **Testcontainers `PostgreSQLContainer("postgres:16-alpine")`** (`FX-028`) + Flyway `V1` (schema) + `V7`/`V8` (MF05). Timeout khởi động **120s**.
> Test file mặc định: `src/test/java/com/sfms/menu/integration/*IT.java` (Failsafe plugin — `mvn verify`).
> ⚠️ **KHÔNG** dùng `prisma db push` / `prisma.client` như template gốc — assert DB bằng `JdbcTemplate` hoặc repository.

```java
// Base class dùng chung — container tái sử dụng, KHÔNG chia sẻ dữ liệu
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
abstract class AbstractMf05IT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:16-alpine").withReuse(true);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url",      POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);
        r.add("spring.flyway.locations",    () -> "classpath:db/migration");
    }

    @Autowired protected MockMvc mvc;
    @Autowired protected JdbcTemplate jdbc;

    /** Cách ly: xoá sạch dữ liệu MF05 trước MỖI test (chống AP-AI-002 shared state). */
    @BeforeEach
    void resetMf05() {
        jdbc.execute("""
            TRUNCATE menu_dish_substitutes, menu_dishes, menu_template_dishes,
                     menu_templates, menus CASCADE
            """);
        // ⚠️ KHÔNG TRUNCATE special_meal_requests — bảng của MF03 (V4), L19/M05-D04.
        // Xoá nó sẽ phá fixture của TestSpec-MF03 khi hai suite chạy chung container.

    }
}
```

---

#### MF05-TC-INT-001 — Happy path trọn vòng đời (create → submit → approve)

**Severity:** `CRITICAL`
**Feature Under Test:** `POST /menus` → `POST /menus/{id}/submit` → `POST /menus/{id}/approve`
**Test File:** `integration/MenuLifecycleIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-01`, `COND-04`, `COND-14`, `COND-22`, `COND-30`
**Oracle Source:** `TDS` §13 hàng `IT-05-01` + `TDS` §15.1 (mẫu curl thật) + **MSG-071 → MSG-039 → MSG-040** + **BR-068** + **D1** (JSON `id` = `code`, DB assertion dùng `WHERE code`)

**Preconditions:**
- PostgreSQL container running; Flyway đã chạy `V1` + `V7` + `V8`
- Seed: `FX-008`, `FX-009` (dishes), `FX-011` (diet groups), `FX-017` (school config), `FX-018` (classrooms)
- JWT: `FX-019` (Caterer) và `FX-021` (School Admin)

**Test Steps:**
1. `POST /api/v1/menus` — payload §9.3.3 đầy đủ, `menuDateIso = today + 5`, `status="DRAFT"`.
2. `POST /api/v1/menus/{id}/submit` bằng `FX-019`.
3. `POST /api/v1/menus/{id}/approve` bằng `FX-021` với `approvalSignature="SIG-TEST-01"`.
4. Assert DB sau mỗi bước.

**Expected Result (PASS):**
- Bước 1 → **201**, message MSG-071, DB `status='DRAFT'`
- Bước 2 → **200**, message MSG-039, DB `status='PENDING_APPROVAL'`, `submitted_at IS NOT NULL`
- Bước 3 → **200**, message MSG-040, DB `status` ∈ {`APPROVED`,`PUBLISHED`} (tuỳ kết luận CF-8), `approved_at IS NOT NULL`, `approved_by = 'admin-001'`
- `menu_dishes` có đúng số dòng bằng số món trong `blocksData`
- Mỗi món có `dish_id` (**không** lưu tên món — C6)

**DB Assertion:**
```java
Map<String,Object> row = jdbc.queryForMap(
    "SELECT status, submitted_at, approved_at, approved_by FROM menus WHERE code = ?", menuCode); // D1
assertThat(row.get("status")).isIn("APPROVED", "PUBLISHED");
assertThat(row.get("approved_at")).isNotNull();
assertThat(row.get("approved_by")).isEqualTo("admin-001");
// D1: menu_dishes.menu_id là FK UUID → join qua bảng menus theo code
assertThat(jdbc.queryForObject("""
    SELECT count(*) FROM menu_dishes d JOIN menus m ON d.menu_id = m.id
     WHERE m.code = ?""", Integer.class, menuCode)).isEqualTo(4);
```

**Expected Result (FAIL):**
- `approved_at` null ⇒ không có mốc thời gian duyệt cho thanh tra
- `menu_dishes` rỗng ⇒ `blocksData` không được map xuống bảng chuẩn hoá (L12/CF-12) ⇒ MF06/MF09 không lấy được `dishId`

**Current Status:** 🔴 Not written

---

#### MF05-TC-INT-002 — Hợp đồng phân trang: page vượt pageCount PHẢI clamp

**Severity:** `HIGH`
**Feature Under Test:** `GET /api/v1/menus` + `PagedResponse.of()`
**Test File:** `integration/MenuPaginationContractIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-18`
**Constraint Ref:** `TDS §17.1 C7`
**Oracle Source:** `00_Team_Work_Assignment_Plan.md` **§2b-6** (*"`page` vượt `pageCount` → clamp về `pageCount`, trả trang cuối"*) + **§2b-1** (5 field bắt buộc) + `TDS` §13 hàng `IT-05-02`
**Kỹ thuật:** Boundary Value Analysis

**Preconditions:** seed **20** menu cùng trường.

**Test Steps:**
1. `GET /menus?page=999&pageSize=8`.
2. `GET /menus?pageSize=0`.
3. `GET /menus?pageSize=-5`.
4. `GET /menus` (không param).

**Expected Result (PASS):**
- (1) → **200**, `pageCount == 3` (`ceil(20/8)`), **`page == 3`** (đã clamp), `items.size() == 4`
- (2)(3) → `pageSize` clamp về **1** (§2b-6), không lỗi 500, không chia cho 0
- (4) → default `page=1`, `pageSize=8` (§2b-2)
- Mọi response có **đủ 5 field** `items`, `total`, `page`, `pageSize`, `pageCount`; **không** wrapper `data`/`result` (§2b-1)
- `total == 20` không phụ thuộc `page`

**Expected Result (FAIL):**
- Trả `page: 999` ⇒ FE hiện "trang 999/3", điều hướng hỏng
- Thiếu 1 field ⇒ `Paged<T>` của `client.ts` không parse được → mọi màn danh sách gãy
- 500 khi `pageSize=0` ⇒ `ArithmeticException` chia cho 0

**Current Status:** 🔴 Not written

---

#### MF05-TC-INT-003 — Tập rỗng sau lọc PHẢI trả 200, không 404

**Severity:** `HIGH`
**Feature Under Test:** `GET /api/v1/menus`
**Test File:** `integration/MenuPaginationContractIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-18`
**Constraint Ref:** `TDS §17.1 C7`
**Oracle Source:** `00_` **§2b-6** (*"Tập rỗng sau lọc → `pageCount=1`, `page=1`, `items=[]`, `total=0`. **Không trả 404** — vẫn trả 200 với shape đầy đủ"*) + `TDS` §13 hàng `IT-05-03`

**Test Steps:** `GET /menus?grade=Khối%209` (khối không tồn tại).

**Expected Result (PASS):**
- **200** với body chính xác: `{ "items": [], "total": 0, "page": 1, "pageSize": 8, "pageCount": 1 }`
- `pageCount` là **1** (không phải 0) — `Math.max(1, ...)`

**Expected Result (FAIL):**
- **404** ⇒ FE ném lỗi thay vì hiện "không có kết quả" (`usePagedList` coi 404 là lỗi mạng)
- `pageCount: 0` ⇒ FE tính `page/pageCount` sinh `NaN`

**Current Status:** 🔴 Not written

---

#### MF05-TC-INT-004 — Lọc phủ định not:field

**Severity:** `MEDIUM`
**Feature Under Test:** `MenuSpec.build()` — nhánh `key.startsWith("not:")`
**Test File:** `integration/MenuFilterIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-19`
**Constraint Ref:** `TDS §17.1 C7`
**Oracle Source:** `00_` **§2b-2** (*"`not:<field>=<value>` — **Lọc phủ định**"*) + §2b-4 (mẫu `cb.notEqual`) + `TDS` §13 hàng `IT-05-04`

**Preconditions:** seed menu ở đủ 5 `MenuStatus`.

**Test Steps:**
1. `GET /menus?not:status=PUBLISHED`.
2. `GET /menus?status=DRAFT` (exact, để đối chiếu).
3. `GET /menus?not:status=` (value rỗng).

**Expected Result (PASS):**
- (1) → **không** item nào có `status == "PUBLISHED"`; `total` = tổng − số `PUBLISHED`
- (2) → chỉ `DRAFT`
- (3) → param bị **bỏ qua** (§2b-6: *"`<field>=` value rỗng → bỏ qua"*), trả như không lọc
- Thứ tự áp lọc đúng §2b-3: exact/not **trước**, `search` **cuối**

**Expected Result (FAIL):**
- Trả cả `PUBLISHED` ⇒ tab "Chờ xử lý" của FE hiện sai dữ liệu
- 500 với `not:status=` ⇒ không guard value rỗng

**Current Status:** 🔴 Not written

---

#### MF05-TC-INT-005 — search CHỈ chạy trên SEARCH_FIELDS, không full scan

**Severity:** `HIGH`
**Feature Under Test:** `MenuSpec.build()` nhánh `search`
**Test File:** `integration/MenuSearchIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-19`
**Constraint Ref:** `TDS §17.1 C7`
**Oracle Source:** `TDS` §9.2 — *"`SEARCH_FIELDS`: `menus` = `{name, grade, createdBy, weekLabel}`"* (**L7/CF-7** — không dùng `description` như plan §2b-3 gợi ý) + `00_` **§2b-3 bước 5** + cảnh báo §2b-3 (*"OR ILIKE trên 10–20 cột = full table scan"*) + `TDS` §13 hàng `IT-05-05`

**Preconditions:** seed menu có `createdBy = "Cty TNHH Suất ăn An Bình"`, `rejectReason = "Từ khoá bí mật XYZ"`.

**Test Steps:**
1. `GET /menus?search=An%20Bình` → khớp `createdBy`.
2. `GET /menus?search=AN%20BÌNH` (chữ hoa) → phải vẫn khớp.
3. `GET /menus?search=XYZ` → **không** được khớp (`rejectReason` ∉ `SEARCH_FIELDS`).
4. `GET /menus?search=` → bỏ qua.
5. `EXPLAIN` câu SQL sinh ra.

**Expected Result (PASS):**
- (1)(2) → khớp; case-insensitive (`cb.like(cb.lower(...))`)
- (3) → `total == 0` — chứng minh search **không** quét mọi cột String
- (4) → trả như không lọc (`if (!search.isBlank())`)
- (5) → mệnh đề `OR ILIKE` xuất hiện đúng **4 lần** (4 field), không nhiều hơn

**Expected Result (FAIL):**
- (3) khớp ⇒ search quét cả `rejectReason`/`reason` ⇒ full table scan + rò rỉ nội dung nội bộ qua tìm kiếm
- Case-sensitive ⇒ người dùng gõ chữ thường không tìm được

**Current Status:** 🔴 Not written

---

#### MF05-TC-INT-006 — Error path dị ứng: submit 409 và DB giữ DRAFT

**Severity:** `CRITICAL`
**Feature Under Test:** `POST /menus/{id}/submit` (và `PUT` với `status=PENDING_APPROVAL` — giai đoạn 1)
**Test File:** `integration/MenuAllergyGuardIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-02`
**Constraint Ref:** `TDS §17.1 C3`
**Oracle Source:** **BR-063** + **MSG-051** + `TDS` §13 hàng `IT-05-06` + `TDS` §15.2 (*"Xung đột dị ứng (BR-063) → 409 MENU-051"*)

**Preconditions:** `FX-006`, `FX-007` (2 HS Khối 3 dị ứng Tôm, VERIFIED); menu Khối 3 chứa món có allergen Tôm, không có suất thay thế.

**Test Steps:**
1. `POST /menus/{id}/submit`.
2. Đọc DB.
3. Thử lại qua `PUT /menus/{id}` với `{"status":"PENDING_APPROVAL"}` (đường FE giai đoạn 1).

**Expected Result (PASS):**
- (1) → **409**, body code `MENU-051`, message theo khuôn MSG-051 với `[Number] = 2`
- (2) → DB `status = 'DRAFT'`, `submitted_at IS NULL` (rollback sạch)
- (3) → **cũng 409** — chặn ở **cả hai** đường vào (nếu chỉ chặn ở action endpoint thì FE hiện tại lách được hoàn toàn)
- Kiểm tương tự ở bước `approve` (BR-063 chạy ở **cả** submit và approve — C3)

**Expected Result (FAIL):**
- (3) trả 200 ⇒ **luật an toàn quan trọng nhất bị lách qua đường FE đang dùng thật** — đây là ca dễ bỏ sót nhất của giai đoạn 1

**Current Status:** 🔴 Not written

---

#### MF05-TC-INT-007 — BR-067: PUT/DELETE trên PUBLISHED → 409, DB không đổi

**Severity:** `CRITICAL`
**Feature Under Test:** `PUT` / `DELETE /api/v1/menus/{id}`
**Test File:** `integration/MenuImmutabilityIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-03`
**Constraint Ref:** `TDS §17.1 C2`
**Oracle Source:** **BR-067** + `TDS` §13 hàng `IT-05-07` + `TDS` §9.3.5 (*"**409 `MENU-067`** nếu `status ∈ {APPROVED, PUBLISHED}`"*) + §14.1 (truy vấn kiểm bất biến)

**Preconditions:** `FX-002` `MENU-2026-30` `PUBLISHED`, có 4 `menu_dishes`.

**Test Steps:**
1. Ghi lại snapshot toàn hàng + `menu_dishes`.
2. `PUT /menus/MENU-2026-30` với `{"name":"hacked","blocksData":[]}`.
3. `DELETE /menus/MENU-2026-30`.
4. So snapshot.

**Expected Result (PASS):**
- (2)(3) → **409** `MENU-067`
- Hàng `menus` **giống hệt** snapshot (kể cả `updated_at`)
- `menu_dishes` vẫn **4** dòng (không bị `orphanRemoval` xoá do merge rỗng)
- Truy vấn §14.1 trả 0 dòng:
  ```sql
  SELECT id FROM menus WHERE status IN ('APPROVED','PUBLISHED') AND updated_at > approved_at;
  ```

**Expected Result (FAIL):**
- `blocksData: []` được merge ⇒ **xoá trắng món của thực đơn đã công bố** — hậu quả nặng nhất của việc quên `assertEditable()` trước merge

**Current Status:** 🔴 Not written

---

#### MF05-TC-INT-008 — Parent chỉ thấy PUBLISHED của khối con mình, kèm cờ xung đột

**Severity:** `CRITICAL`
**Feature Under Test:** `GET /api/v1/parent/menus?studentId=…` (§9.3.11)
**Test File:** `integration/ParentMenuIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-21`
**Constraint Ref:** `TDS §17.1 C12`
**Oracle Source:** **BR-201** (≤ 5 con) + `TDS` §4.3 (*"Parent chỉ đọc thực đơn `PUBLISHED` của **khối con mình**"*) + `TDS` §13 hàng `IT-05-08` + `MF05_Menu_Diet_Management.md` **UC-135** (+ AF2 suất riêng của con)

**Preconditions:**
- `FX-006` `HS-3A-001` (Khối 3, dị ứng Tôm/Cua VERIFIED), `FX-022` Parent của HS này
- Seed: 1 menu Khối 3 `PUBLISHED` (có món chứa Tôm), 1 menu Khối 3 `DRAFT`, 1 menu Khối 5 `PUBLISHED`

**Test Steps:**
1. `GET /api/v1/parent/menus?studentId=HS-3A-001`.
2. Kiểm nội dung từng item.

**Expected Result (PASS):**
- `items` chỉ có **1** menu: Khối 3 `PUBLISHED`
- **Không** có menu `DRAFT` (dù cùng khối) và **không** có menu Khối 5
- Item có cờ `conflictsWithChild == true` cho món chứa Tôm (UC-135 giá trị người dùng chính)
- Suất riêng (`audience ≠ "ALL"`) áp cho `specialDiet` của con **có** xuất hiện (UC-135 AF2)
- Payload **không** chứa `estimatedCost`/`costCeiling` (thông tin thương mại, không phải việc của PH) — nếu TDS chưa quy định thì ghi thành câu hỏi review cho Minh, **không** tự quyết

**Expected Result (FAIL):**
- Thấy `DRAFT` ⇒ phụ huynh xem thực đơn chưa duyệt, khiếu nại về món chưa chốt
- Thấy khối khác ⇒ rò rỉ chéo + gây nhầm lẫn
- Thiếu `conflictsWithChild` ⇒ mất giá trị chính của UC-135

**Current Status:** 🔴 Not written

---

#### MF05-TC-INT-009 — Parent xem tuần chưa công bố → MSG-090

**Severity:** `MEDIUM`
**Feature Under Test:** `ParentMenuService.publishedForChild()`
**Test File:** `integration/ParentMenuIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-28`
**Oracle Source:** **MSG-090** nguyên văn = `"No menu has been published for the selected week yet. Please check back later."`; `error_message.md` ghi rõ MSG-090 thuộc **UC-135** (*bổ sung 2026-07-01, thay cho citation sai "MSG-076"*); `TDS` §10 → `MENU-090`, HTTP **404**; `TDS` §13 hàng `IT-05-09`

**Test Steps:** `GET /api/v1/parent/menus?studentId=HS-3A-001&from=2099-01-01&to=2099-01-07`.

**Expected Result (PASS):**
- **404** với code `MENU-090`, message MSG-090 nguyên văn
- ⚠️ **Lưu ý mâu thuẫn có chủ ý:** đây là **ngoại lệ** so với §2b-6 (*tập rỗng → 200*), vì UC-135 cần thông điệp riêng cho phụ huynh. Endpoint **danh sách chung** `GET /menus` vẫn phải trả **200 rỗng** (`MF05-TC-INT-003`). Test này khẳng định 2 hành vi khác nhau ở 2 endpoint khác nhau là **cố ý**.

**Expected Result (FAIL):**
- Trả 200 rỗng ⇒ FE hiện lịch trắng không giải thích, PH tưởng lỗi ứng dụng
- Trả 404 cho `GET /menus` ⇒ phá hợp đồng §2b

**Current Status:** 🔴 Not written
**Implementation Note:** nếu Minh muốn thống nhất 200 cho cả hai, phải sửa `error_message.md`/UC-135 trước — **không** tự quyết trong code.

---

#### MF05-TC-INT-010 — Audit log ghi đủ actor + IP cho mọi thao tác ghi

**Severity:** `HIGH`
**Feature Under Test:** audit aspect trên `create/update/delete/submit/approve/reject/publish`
**Test File:** `integration/MenuAuditIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-22`
**Oracle Source:** `TDS` §4.3 (*"Ghi audit (**BR-170**) cho: create/update/delete/submit/approve/reject/publish + mọi lần gọi AI (**BR-157**)"*) + `TDS` §13 hàng `IT-05-10` + §14.2 (*"BR-170: mọi thao tác duyệt đều có audit kèm actor + IP"*)
**⚠️ Ghi chú traceability:** BR-170/BR-157 **không** nằm trong cột BR của §1.6 cho MF05 (BR-156 thì có). Oracle của TC này là **TDS §4.3/§14.2** — ghi rõ để Huy đối chiếu, không nâng BR-170 thành BR của MF05.

**Test Steps:** chạy lại chuỗi `MF05-TC-INT-001` rồi truy vấn `audit_logs`.

**Expected Result (PASS):**
- Đúng **3** dòng cho `CREATE`, `SUBMIT`, `APPROVE` của cùng `entity_id`
- Mỗi dòng có `actor` khác `null`, `ip_address` khác `null`, `occurred_at` tăng dần
- `actor` của dòng `APPROVE` là `admin-001` (School Admin), **không** phải `caterer-001`

**DB Assertion:**
```java
List<Map<String,Object>> logs = jdbc.queryForList(
    "SELECT action, actor, ip_address FROM audit_logs WHERE entity_code = ? ORDER BY occurred_at", menuCode); // D1
assertThat(logs).hasSize(3)
    .extracting("action").containsExactly("CREATE", "SUBMIT", "APPROVE");
assertThat(logs).allSatisfy(l -> {
    assertThat(l.get("actor")).isNotNull();
    assertThat(l.get("ip_address")).isNotNull();
});
```

**Expected Result (FAIL):**
- Thiếu dòng `APPROVE` ⇒ không truy được ai duyệt khi có sự cố ngộ độc (MF11 cần chuỗi này)
- `actor` null ⇒ audit vô dụng

**Current Status:** 🔴 Not written

---

#### MF05-TC-INT-011 — Áp thực đơn mẫu tự gắn defaultSubstituteId

**Severity:** `MEDIUM`
**Feature Under Test:** `POST /api/v1/menus/{id}/apply-template` / `MenuService.applyTemplate()`
**Test File:** `integration/MenuTemplateIT.java`
**TDD Phase:** 🔴 RED · ⚠️ endpoint **MỚI** (§9.2 #25 — FE hiện áp mẫu client-side)
**Condition Ref:** `COND-27`
**Oracle Source:** `TDS` §13 hàng `IT-05-11` (*"`POST /apply-template` với `TPL-STD-B` → 3 món của mẫu được đổ vào, món `ca-basa-sot-ca-chua` tự gắn `defaultSubstituteId = dau-phu-sot-ca-chua`"*) + `TDS` §8.1 (`applyTemplate` javadoc) + **L6/CF-6** (`Dish.defaultSubstituteId` có trong mock 25/07)

**Preconditions:** `FX-016` `TPL-STD-B` (3 món, LUNCH); `FX-008`/`FX-009`; menu đích `DRAFT` rỗng.

**Test Steps:**
1. `POST /api/v1/menus/{id}/apply-template` body `{"templateId":"TPL-STD-B"}`.
2. Đọc `menu_dishes` + `menu_dish_substitutes`.

**Expected Result (PASS):**
- **200**; `menu_dishes` có đúng **3** dòng, `serving_order` = 1,2,3 theo thứ tự mẫu
- Món `ca-basa-sot-ca-chua` có **1** dòng `menu_dish_substitutes` với `dish_id = "dau-phu-sot-ca-chua"` (tự đổ từ `Dish.defaultSubstituteId`)
- Response có `blocksData` đầy đủ (map hai chiều — L12)
- Áp mẫu vào menu **đã có món** → theo hành vi FE (§9.1 #5: *áp mẫu vào ô đã có*) là **thay thế** buổi đó, không nhân đôi món
- Áp mẫu lên menu `PUBLISHED` → **409** `MENU-067`

**Expected Result (FAIL):**
- Không đổ `defaultSubstituteId` ⇒ Caterer phải nhập tay suất thay thế mỗi lần ⇒ dễ quên ⇒ vi phạm BR-065 ở bước submit
- Món bị nhân đôi ⇒ `dishes` (đếm) sai, chi phí sai

**Current Status:** 🔴 Not written

---

#### MF05-TC-INT-012 — PUT là partial merge, KHÔNG replace

**Severity:** `CRITICAL`
**Feature Under Test:** `PUT /api/v1/menus/{id}` — `MenuService.update()`
**Test File:** `integration/MenuPartialMergeIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-20`
**Constraint Ref:** `TDS §17.1 C8`
**Oracle Source:** `TDS` §9.0 nguyên văn — *"Hook **tách `id` ra khỏi body** rồi gửi phần còn lại … **`PUT` ở đây có ngữ nghĩa PATCH/merge**, không phải replace. Backend **bắt buộc** merge, nếu replace sẽ **xóa sạch** các field không gửi"* + §17.1 **C8** (*"gửi `{status, rejectReason}` mà xóa `blocksData` là mất trắng thực đơn"*) + `mock-server.ts:354` (`Object.assign(row, body)`)
**Kỹ thuật:** Contract Testing

**Preconditions:** menu `PENDING_APPROVAL` đầy đủ: 4 món, `estimatedCost=29200`, `servings=320`, `weekLabel`, `createdBy`.

**Test Steps:**
1. `PUT` với body **chỉ có 2 field**: `{"status":"REJECTED","rejectReason":"Thiếu suất chay"}` (bằng `FX-021`).
2. Đọc toàn bộ hàng + `menu_dishes`.
3. `PUT` với `{"servings": 300}` (1 field).
4. Đọc lại.

**Expected Result (PASS):**
- Sau (1): `status='REJECTED'`, `reject_reason` đã ghi; **mọi field khác giữ nguyên** — `estimated_cost=29200`, `servings=320`, `week_label`, `created_by` **không** bị null
- `menu_dishes` vẫn **4** dòng
- Sau (3): chỉ `servings` đổi thành 300; `reject_reason` của bước (1) **vẫn còn**
- Body **không** được chứa `id` (hook đã tách) — nếu có `id` khác path thì **400**

**Expected Result (FAIL):**
- Field không gửi bị set `null` ⇒ mỗi lần từ chối là **xoá trắng nội dung thực đơn** — Caterer phải nhập lại từ đầu. Đây là lỗi thầm lặng nguy hiểm nhất của hợp đồng §9.0.
- `menu_dishes` bị `orphanRemoval` xoá khi body không có `blocksData` ⇒ mất món

**Current Status:** 🔴 Not written
**Implementation Note:** DTO update dùng `Optional<T>`/`JsonNullable<T>` hoặc `Map<String,Object>` để phân biệt **"không gửi"** với **"gửi null"**. Đừng dùng record thường — Jackson sẽ điền `null` và bạn không phân biệt được.

---

#### MF05-TC-INT-013 — Xóa món/mẫu đang được thực đơn tham chiếu → 409

**Severity:** `MEDIUM`
**Feature Under Test:** `DELETE /api/v1/dishes/{id}` · `DELETE /api/v1/menu-templates/{id}`
**Test File:** `integration/DishLifecycleIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-29`
**Oracle Source:** `TDS` §9.5 mục 11 — *"Giữ `DELETE` nhưng **khuyến nghị không dùng**; backend **chặn** xóa món/mẫu đã được thực đơn tham chiếu → **409**"* + §9.2 #10/#15 (*"FE **cố tình KHÔNG dùng** — ngừng dùng bằng `isActive=false` để không đứt truy xuất thực đơn cũ"*)

**Preconditions:** `FX-008` `ca-basa-sot-ca-chua` đang được 1 menu `PUBLISHED` tham chiếu; thêm 1 món **không** được tham chiếu.

**Test Steps:**
1. `DELETE /dishes/ca-basa-sot-ca-chua`.
2. `DELETE /dishes/{món-không-tham-chiếu}`.
3. `PUT /dishes/ca-basa-sot-ca-chua` với `{"isActive": false}`.
4. Đọc lại menu `PUBLISHED`.

**Expected Result (PASS):**
- (1) → **409**, món **không** bị xoá
- (2) → **200/204**, xoá được
- (3) → **200**, `is_active = false`
- (4) → menu cũ **vẫn** hiển thị đủ món (truy xuất lịch sử không đứt); `GET /dishes` mặc định vẫn trả món `isActive=false` (FE tự lọc — §9.1 #21 dùng `isActive` như toggle "Ngừng dùng / Dùng lại")
- `GET /dishes` **không** loại món inactive khỏi response, vì menu cũ cần tra tên món

**Expected Result (FAIL):**
- Xoá được ⇒ thực đơn đã công bố mất tên món ⇒ **đứt chuỗi truy xuất** khi điều tra sự cố (MF11: sự cố → món → lô → NCC)

**Current Status:** 🔴 Not written

---

#### MF05-TC-INT-014 — Nộp duyệt hàng loạt N=15 request tuần tự vẫn đúng

**Severity:** `MEDIUM`
**Feature Under Test:** `PUT /api/v1/menus/{id}` ×15
**Test File:** `integration/MenuBulkSubmitIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-30`
**Constraint Ref:** `TDS §17.1 C8`
**Oracle Source:** `TDS` §9.3.4-B — *"Nộp hàng loạt: FE gọi **N request tuần tự**, không có endpoint bulk. Backend cần chịu được **N ≤ 15** request/lần bấm"* + §9.5 mục 7 + §4.1 (p99 < 300ms cho GET; POST/PUT giới hạn `60/min` §9.2)

**Preconditions:** 15 menu `DRAFT` cùng tuần, cùng khối, `menuDate = today + 5`, tất cả hợp lệ BR-061/063/065/069.

**Test Steps:**
1. Gọi tuần tự 15 lần `PUT /menus/{id}` với `{"status":"PENDING_APPROVAL","submittedAt":"..."}`.
2. Đo tổng thời gian.
3. Đọc DB.
4. Biến thể: 1 trong 15 menu có xung đột dị ứng.

**Expected Result (PASS):**
- 15/15 trả **200**; DB: 15 bản ghi `PENDING_APPROVAL`
- **Không** vướng rate limit (`60/min` cho PUT — 15 < 60)
- Tổng thời gian < **10s** (timeout của `client.ts` áp cho **từng** request, nhưng UX bấm 1 lần nên đo tổng)
- Biến thể (4): 14 menu chuyển `PENDING_APPROVAL`, menu có xung đột trả **409** và giữ `DRAFT` — **không** rollback 14 menu kia (mỗi request là 1 transaction độc lập)
- `MF05-TC-010` (dish rotation) không báo lỗi sai do 15 menu cùng tuần — bộ đếm tính theo `dishId`, không theo số menu

**Expected Result (FAIL):**
- Request thứ N bị 429 ⇒ Caterer bấm "Nộp cả tuần" thì nộp thiếu ngày mà không biết
- 1 lỗi làm rollback cả 15 ⇒ hành vi bất ngờ, không khớp mô hình N-request của FE

**Current Status:** 🔴 Not written
**Implementation Note:** khi làm `POST /menus/bulk-submit` ở giai đoạn 2 (§9.5 mục 7), giữ test này để so sánh hành vi lỗi cục bộ.

---

### 4.4. E2E TEST CASES

---

#### MF05-TC-E2E-001 — Trục xương sống P1 nối thông bằng ID

**Severity:** `CRITICAL`
**Feature Under Test:** `Login Caterer → lập thực đơn Khối 3 → Login School Admin duyệt → Login Parent thấy thực đơn của con + cảnh báo dị ứng`
**Test File:** `integration/MenuBackboneE2EIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-01`, `COND-02`, `COND-04`, `COND-14`, `COND-21`
**Oracle Source:** `00_Team_Work_Assignment_Plan.md` **§3** (*"Trục xương sống (P1) phải thông: Login theo role → **Menu theo khối** → phân suất & thống kê theo món → … — nối bằng **ID**, không phải tên"*) + `TDS` §13 hàng `E2E-05-01` + `TDS` §15.1 (chuỗi curl thật)

**Preconditions:** DB sạch + seed MF01 (3 user), MF02 (`SCH-001`, lớp 3A/3B), MF03 (`HS-3A-001` dị ứng Tôm VERIFIED, phụ huynh liên kết), MF05 (`FX-008`, `FX-009`, `FX-011`, `FX-016`, `FX-017`).

**Test Steps:**
1. `POST /api/v1/auth/login` role Service Caterers → lấy JWT.
2. `POST /menus` Khối 3, `menuDate = today + 5`, gồm món có Tôm **và** suất thay thế.
3. `POST /menus/{id}/submit` → `PENDING_APPROVAL`.
4. Login School Admin → `GET /menus?status=PENDING_APPROVAL` thấy menu vừa nộp.
5. `POST /menus/{id}/approve` với chữ ký.
6. Login Parent của `HS-3A-001` → `GET /parent/menus?studentId=HS-3A-001`.

**Expected Result (PASS):**
- Bước 4: menu xuất hiện trong danh sách chờ duyệt của **đúng trường**, kèm `allergyConflicts >= 1` để BGH thấy cảnh báo
- Bước 6: Parent thấy **đúng menu đó** (so bằng **`id`**, không so tên), `status="PUBLISHED"`, cờ `conflictsWithChild = true`, và suất thay thế xuất hiện
- **Mọi liên kết dùng ID:** `menu.id` → `menu_dishes.dish_id` → `dishes.id`; `menu.grade` ↔ `classrooms.grade`; `parent.childIds` → `students.id`. Assert **không** chỗ nào join bằng tên món/tên lớp.
- Chuỗi audit đủ 3 dòng (`MF05-TC-INT-010`)

**Expected Result (FAIL):**
- Parent không thấy menu ⇒ trục xương sống đứt ở mắt cuối (đúng mắt mà demo/report dựa vào)
- Join bằng tên món ⇒ đổi tên món là mất truy xuất, vi phạm quy ước dữ liệu `00_` §5 (*"món tham chiếu `dishId` không phải tên"*)

**Current Status:** 🔴 Not written

---

#### MF05-TC-E2E-002 — Hợp đồng FE↔BE: đổi baseURL là chạy

**Severity:** `HIGH`
**Feature Under Test:** shape response của 4 resource MF05 vs `mock-server.ts`
**Test File:** `integration/MenuContractSnapshotIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-18`, `COND-15`, `COND-16`
**Constraint Ref:** `TDS §17.1 C7, C9, C11`
**Oracle Source:** `TDS` §9 mở đầu — *"liệt kê **đúng những gì FE đang gọi** … để *đổi `baseURL` là chạy*"* + §17.1 **C9** (12 tên field bất biến) + `TDS` §14.3 (*"So sánh shape response với mock FE — **Expected: không khác biệt**"*)
**Kỹ thuật:** Contract Testing (snapshot so sánh khoá, bỏ qua giá trị)

**Test Steps:**
1. Gọi `GET /menus`, `GET /menus/{id}`, `GET /dishes`, `GET /menu-templates`. *(bỏ `GET /special-meals` — resource của MF03, L19)*
2. Trích **tập khoá** JSON (đệ quy) của mỗi response.
3. So với snapshot khoá xuất từ `mock-server.ts` (file `src/test/resources/contracts/mf05-mock-keys.json`, do Minh cập nhật khi mock đổi).

**Expected Result (PASS):**
- Tập khoá của backend ⊇ tập khoá của mock — **không thiếu** khoá nào
- Có đủ 12 tên field C9: `week`, `dishes`, `allergyConflicts`, `menuDateIso`, `createdBy`, `blocksData`, `servings`, `audience`, `dietGroupId`, `approvalSignature`, `rejectReason`, `reason`
- 5 khoá phân trang đúng, **không** wrapper `data`/`result`
- `submittedAt`/`approvedAt` khớp regex `^\d{2}/\d{2}/\d{4}`; `menuDateIso` khớp `^\d{4}-\d{2}-\d{2}$`

**Expected Result (FAIL):**
- Thiếu/đổi tên bất kỳ khoá ⇒ **14 màn FE gãy** (C9) — test này là hàng rào chống hồi quy hợp đồng, phải chạy trong CI mỗi PR

**Current Status:** 🔴 Not written
**Implementation Note:** snapshot chỉ so **khoá**, không so **giá trị** (giá trị phụ thuộc seed). Khi mock đổi có chủ ý, Minh cập nhật file snapshot **trong cùng PR** với thay đổi mock (`00_` §0 chống lệch).

---

## 5. Red-Green-Refactor Tracker

> Cột 🟢 GREEN điền **commit hash** khi test xanh. Không được điền trước khi Red Gate (§5.1) PASS.

### 5.1. Unit (26)

| TC ID | Test File | 🔴 RED confirmed | 🟢 GREEN (commit) | 🔵 REFACTOR note |
|-------|-----------|-----------------|-------------------|------------------|
| `MF05-TC-001` | `service/MenuWorkflowServiceTest.java` | `[ ]` | | inject `Clock` để bỏ phụ thuộc `LocalDate.now()` |
| `MF05-TC-002` | `service/MenuWorkflowServiceTest.java` | `[ ]` | | gộp với TC-001 thành `@ParameterizedTest` biên {2,3,4} |
| `MF05-TC-003` | `service/AllergyConflictCheckerTest.java` | `[ ]` | | tách `AllergenResolver` (union `allergens` + `manualAllergens`) |
| `MF05-TC-004` | `service/AllergyConflictCheckerTest.java` | `[ ]` | | tách `blocking` thành thuộc tính của `AllergyConflict` |
| `MF05-TC-005` | `service/MenuServiceTest.java` | `[ ]` | | `assertEditable()` → guard clause dùng chung cho update/delete/applyTemplate |
| `MF05-TC-006` | `service/MenuServiceTest.java` | `[ ]` | | |
| `MF05-TC-007` | `service/MenuWorkflowServiceTest.java` | `[ ]` | | `@PreAuthorize` ở service, không chỉ controller |
| `MF05-TC-008` | `service/MenuWorkflowServiceTest.java` | `[ ]` | | |
| `MF05-TC-009` | `service/MenuWorkflowServiceTest.java` | `[ ]` | | |
| `MF05-TC-010` | `service/DishRotationValidatorTest.java` | `[ ]` | | rút `WeekRange` value object (dùng lại ở MF13) |
| `MF05-TC-011` | `service/IngredientDeclarationValidatorTest.java` | `[ ]` | | |
| `MF05-TC-012` | `service/DietCoverageValidatorTest.java` | `[ ]` | | port trực tiếp `dietCoverageGaps()` của mock để so kết quả 1-1 |
| ~~`MF05-TC-013`~~ | ⛔ **gỡ** — chuyển chủ sang MF03 (L19/L20); L14 đã đóng | — | | |
| `MF05-TC-027` 🆕 | `service/MenuWriteContractTest.java` + `web/MenuContractIT.java` | `[ ]` | | D12 — key ghi ≠ key đọc (L25) |
| `MF05-TC-014` | `service/MenuRecommendationServiceTest.java` | `[ ]` | | ⚠️ blocked L13 |
| `MF05-TC-015` | `service/MenuRecommendationServiceTest.java` | `[ ]` | | ⚠️ blocked L13 |
| `MF05-TC-016` | `service/MenuWorkflowServiceTest.java` | `[ ]` | | |
| `MF05-TC-017` | `service/CostCeilingValidatorTest.java` | `[ ]` | | |
| `MF05-TC-018` | `service/SpecialMenuServiceTest.java` | `[ ]` | | bảng quyết định → `@CsvSource` |
| `MF05-TC-019` | `service/MenuWorkflowServiceTest.java` + `MenuPersistenceIT` | `[ ]` | | CHECK constraint trong `V7` |
| `MF05-TC-020` | `service/SpecialMenuServiceTest.java` | `[ ]` | | |
| `MF05-TC-021` | `service/MenuStateMachineTest.java` | `[ ]` | | FSM thành `EnumMap` một chỗ |
| `MF05-TC-022` | `service/MenuStateMachineTest.java` | `[ ]` (disabled) | | ⚠️ `@Disabled` — chờ CF-8 |
| `MF05-TC-023` | `service/MenuDerivedFieldsTest.java` | `[ ]` | | cache theo `(menuId, updatedAt)` như ADR-M05-02 |
| `MF05-TC-024` | `mapper/MenuDateFormatTest.java` | `[ ]` | | 1 `@Named` converter dùng chung |
| `MF05-TC-025` | `service/MenuIdPolicyTest.java` | `[ ]` | | |
| `MF05-TC-026` | `service/MenuIdPolicyTest.java` | `[ ]` | | regex khuôn ID thành hằng dùng chung với FE snapshot |

### 5.2. Security (14)

| TC ID | Test File | 🔴 RED confirmed | 🟢 GREEN (commit) | 🔵 REFACTOR note |
|-------|-----------|-----------------|-------------------|------------------|
| `MF05-TC-SEC-001` | `web/MenuSecurityTest.java` | `[ ]` | | danh sách 26 endpoint dùng chung với SEC-007/010 |
| `MF05-TC-SEC-002` | `web/MenuSecurityTest.java` | `[ ]` | | |
| `MF05-TC-SEC-003` | `web/MenuSecurityTest.java` | `[ ]` | | |
| `MF05-TC-SEC-004` | `web/MenuSecurityTest.java` | `[ ]` | | rút `OwnershipGuard` dùng lại cho MF04/MF06 |
| `MF05-TC-SEC-005` | `web/ParentAccessSecurityTest.java` | `[ ]` | | |
| `MF05-TC-SEC-006` | `web/MenuSecurityTest.java` | `[ ]` | | thêm `schoolId` vào nhóm key bị loại của `MenuSpec` |
| `MF05-TC-SEC-007` | `web/MenuAuthorizationMatrixTest.java` | `[ ]` | | 182 tổ hợp — `@MethodSource` |
| `MF05-TC-SEC-008` | `web/MenuFieldLevelAuthorizationTest.java` | `[ ]` | | 3 nhóm field thành enum |
| `MF05-TC-SEC-009` | `web/AllergyPrivacyTest.java` | `[ ]` | | |
| `MF05-TC-SEC-010` | `web/MenuAuthorizationMatrixTest.java` | `[ ]` | | |
| ~~`MF05-TC-SEC-011`~~ | ⛔ **gỡ** — RBAC `special-meals` do MF03 gác (`@PreAuthorize` đã có trong code merged), L19 | — | | |
| `MF05-TC-SEC-015` 🆕 | `web/MenuRoleShapeSecurityTest.java` | `[ ]` | | D11 — Parent/Teacher không thấy field giá (L24) |
| `MF05-TC-SEC-012` | `web/RateLimitTest.java` | `[ ]` | | ⚠️ blocked L13 |
| `MF05-TC-SEC-013` | `web/MenuSpecInjectionTest.java` | `[ ]` | | allow-list field lọc dùng chung `common/` |
| `MF05-TC-SEC-014` | `web/MenuImmutabilityTest.java` | `[ ]` | | |

### 5.3. Integration + E2E (16)

| TC ID | Test File | 🔴 RED confirmed | 🟢 GREEN (commit) | 🔵 REFACTOR note |
|-------|-----------|-----------------|-------------------|------------------|
| `MF05-TC-INT-001` | `integration/MenuLifecycleIT.java` | `[ ]` | | |
| `MF05-TC-INT-002` | `integration/MenuPaginationContractIT.java` | `[ ]` | | dùng lại cho 12 module khác (`PagedResponse` là nền chung) |
| `MF05-TC-INT-003` | `integration/MenuPaginationContractIT.java` | `[ ]` | | |
| `MF05-TC-INT-004` | `integration/MenuFilterIT.java` | `[ ]` | | |
| `MF05-TC-INT-005` | `integration/MenuSearchIT.java` | `[ ]` | | thêm index cho `SEARCH_FIELDS` nếu `EXPLAIN` xấu |
| `MF05-TC-INT-006` | `integration/MenuAllergyGuardIT.java` | `[ ]` | | |
| `MF05-TC-INT-007` | `integration/MenuImmutabilityIT.java` | `[ ]` | | |
| `MF05-TC-INT-008` | `integration/ParentMenuIT.java` | `[ ]` | | |
| `MF05-TC-INT-009` | `integration/ParentMenuIT.java` | `[ ]` | | |
| `MF05-TC-INT-010` | `integration/MenuAuditIT.java` | `[ ]` | | audit aspect nằm ở `common/` (Nam sở hữu) |
| `MF05-TC-INT-011` | `integration/MenuTemplateIT.java` | `[ ]` | | ⚠️ endpoint mới §9.2 #25 |
| `MF05-TC-INT-012` | `integration/MenuPartialMergeIT.java` | `[ ]` | | `JsonNullable<T>` cho toàn bộ DTO update |
| `MF05-TC-INT-013` | `integration/DishLifecycleIT.java` | `[ ]` | | |
| `MF05-TC-INT-014` | `integration/MenuBulkSubmitIT.java` | `[ ]` | | giữ lại khi làm `bulk-submit` giai đoạn 2 |
| `MF05-TC-E2E-001` | `integration/MenuBackboneE2EIT.java` | `[ ]` | | chạy trong CI nightly (chậm) |
| `MF05-TC-E2E-002` | `integration/MenuContractSnapshotIT.java` | `[ ]` | | chạy **mỗi PR** — hàng rào hợp đồng |

### 5.4. Red Gate Protocol (CASE 2.0 — GATE-2)

> ⭐ **Gate QUAN TRỌNG NHẤT.** Trước khi implement, chạy toàn bộ test suite với stub **ném exception**.
> Mọi test PHẢI **FAIL**. Nếu test PASS ngay → **AP-AI-002 (Green-from-Birth)** → reject và rewrite.

**Stub cho Red Phase (Java — thay cho bản TypeScript của template):**

```java
package com.sfms.mf05.service.impl;

import com.sfms.mf05.service.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * RED PHASE STUB — mọi method ném. Dùng để chứng minh test đang thật sự kiểm hành vi.
 * Xoá/thay bằng implementation thật sau khi GATE-2 PASS.
 */
@Service
@Profile("red-gate")
public class MenuWorkflowServiceRedStub implements MenuWorkflowService {

    private static final String MSG = "Not implemented — Red Phase stub";

    @Override public MenuDetailResponse submit(String menuId, String actor) {
        throw new UnsupportedOperationException(MSG);
    }
    @Override public MenuDetailResponse approve(String menuId, ApproveMenuRequest r, String actor) {
        throw new UnsupportedOperationException(MSG);
    }
    @Override public MenuDetailResponse reject(String menuId, RejectMenuRequest r, String actor) {
        throw new UnsupportedOperationException(MSG);
    }
}
```

```bash
# Chạy Red Gate
mvn test -Dspring.profiles.active=red-gate -Dtest='com.sfms.mf05.**' \
         2>&1 | tee target/red-gate-evidence.log

# Số test PASS phải bằng 0 (trừ TC bị @Disabled)
grep -E "Tests run:.*Failures" target/red-gate-evidence.log
```

**Red Gate Verification:**

| Nhóm TC | Stub Result | Expected | Actual | Root Cause (nếu PASS bất thường) |
|-------|-------------|----------|--------|----------------------------------|
| `MF05-TC-001…026` (unit) | `throw UnsupportedOperationException` | 🔴 FAIL (26/26) | ☐ FAIL ☐ PASS | ☐ Tautology ☐ Shared state ☐ Hallucinated contract ☐ Assert quá yếu |
| `MF05-TC-SEC-001…014` | filter chain rỗng | 🔴 FAIL (14/14) | ☐ FAIL ☐ PASS | ☐ Test chỉ assert `status != 500` (quá yếu) |
| `MF05-TC-INT-001…014` | controller trả `501` | 🔴 FAIL (14/14) | ☐ FAIL ☐ PASS | ☐ Không assert DB, chỉ assert HTTP |
| `MF05-TC-E2E-001…002` | như trên | 🔴 FAIL (2/2) | ☐ FAIL ☐ PASS | |

**Bẫy Red Gate riêng của MF05 — kiểm thủ công:**

| # | Bẫy | Vì sao dễ PASS giả | Cách chống |
|---|---|---|---|
| 1 | `MF05-TC-003` (dị ứng) | Nếu assert *"submit ném exception"* mà không assert **loại** exception, stub ném `UnsupportedOperationException` cũng PASS | Assert đúng `AllergyConflictException` **và** `errorCode == "MENU-051"` **và** `affectedStudentCount == 2` |
| 2 | `MF05-TC-021` (FSM) | Test bảng 5×5: mọi ô ❌ đều "ném exception" → stub PASS toàn bộ ô ❌ | Ô ❌ assert đúng `IllegalStateTransitionException` + HTTP 409; **và** phải có ≥ 1 ô ✅ trong cùng test để stub FAIL |
| 3 | `MF05-TC-SEC-001` (401) | Nếu Spring Security chưa cấu hình, mọi request 401 sẵn → test PASS mà chưa có gì | Thêm assert **đối chứng**: cùng endpoint **có** JWT hợp lệ phải trả ≠ 401 |
| 4 | `MF05-TC-INT-003` (tập rỗng) | DB rỗng thì `items=[]` tự nhiên đúng | Seed 20 bản ghi trước, rồi lọc `grade=Khối 9` — rỗng phải là **kết quả của lọc**, không phải của DB trống |
| 5 | `MF05-TC-INT-012` (partial merge) | Nếu DTO là record thường, "giữ nguyên field" có thể do chưa implement gì cả | Assert field **được gửi** đã đổi **và** field **không gửi** giữ nguyên — cả hai chiều trong cùng một test |

**Red Gate Evidence:**

- Stub commit hash: `___`
- Tất cả FAIL? ☐ Yes → **GATE-2 PASS** (T2→T3) → tiếp tục implement
- Log file: `target/red-gate-evidence.log`
- 5 bẫy trên đã kiểm thủ công? ☐ Yes (người kiểm: `_______`)

> **Nếu bất kỳ test PASS:** dừng lại. Xác định root cause từ bảng trên. Rewrite test từ TC-ID spec với Fixture Isolation Pattern (§4).

---

## 6. Entry / Exit Criteria

### Entry Criteria (điều kiện bắt đầu viết test)

- [x] `TDS-MF05_Backend.md` (`SFMS-MF05-IMP-001`) đã **Approved** — **v1.2, 2026-08-03** (§2.4 M05-D01…D16)
- [ ] **Nút cổ chai `00_` §4b đã qua:** TDS + backend MF01 (auth/JWT/RBAC) và MF02 (vertical slice, `PagedResponse`, `Specification`) chạy được
- [x] Bảng phụ thuộc đã tồn tại trong migration đã merge: `schools`/`classrooms` (V1/V3), `students` (V4), `partners` (V1/V6), `user_accounts` (V1). ⚠️ **`catalog_ingredients` CHƯA có** (MF06) ⇒ `dish_ingredients` dùng `ingredient_code` không FK (**M05-D15**). Mô hình *"V1 = toàn bộ schema"* **đã bị bỏ** — MF05 lấy **V8/V9** (L27)
- [x] Logic Issues §2 **L1–L28** đã có kết luận: L14 **đóng** (BR-220/MSG-144), L15–L18 khoá theo D1–D7, **L19–L28 khoá theo code thật** (§2.-1). 7 mục còn chờ người khác nằm ở `TDS-MF05_Backend.md` **§2.5** và **không chặn** viết test
- [ ] Test fixtures §3 TDS-05 (`FX-001`…`FX-028`) đã tạo trong `MenuTestFixtures.java` + `src/test/resources/fixtures/`
- [ ] Testcontainers chạy được trên máy dev + CI (Docker daemon khả dụng)
- [x] `AiAssistPort` (MF12) — **không chặn**: chưa có bean ⇒ endpoint trả `503 MENU-073`, đúng nhánh fallback bắt buộc của ADR-M05-03 (**M05-D16**). `TC-014` assert đúng nhánh này
- [ ] `SpecialMealLookupPort` (MF03, Huy) — chỉ cần **interface** để mock; chưa có thì mock bằng chữ ký ở `TDS` §8.1 (**M05-D04**)

### Exit Criteria (Definition of Done)

**Bắt buộc — không có ngoại lệ:**

- [ ] `mvn test` — **toàn bộ unit test xanh**, không `@Disabled` nào ngoài `MF05-TC-022` (kèm lý do CF-8) và 3 TC blocked L13 (`TC-014`, `TC-015`, `TC-SEC-012`)
- [ ] `mvn verify` — **toàn bộ integration test xanh** (Failsafe + Testcontainers)
- [ ] Coverage: **≥ 85% line** cho `com.sfms.mf05.service`; **100% branch** cho `MenuWorkflowService` (FSM BR-068) và `AllergyConflictChecker` (BR-063)
  ```bash
  mvn jacoco:report && \
  python3 -c "import sys,xml.etree.ElementTree as T; ..."  # gate trong CI
  ```
- [ ] **Mỗi condition §3 TDS-03 có ≥ 1 TC xanh** — 33/33 condition, không condition mồ côi
- [ ] **Mỗi quyết định D1–D13 có ≥ 1 TC** (§9.6): D1 → `TC-INT-001`/`TC-025` · D2 → mọi TC error path · D3 → `TC-E2E-002` · D4 → `TC-024` · D5 → `TC-025`/`TC-026` · D6 → `TC-003` · D7 → `TC-003`/`TC-SEC-001` · **D8 → contract test `/v3/api-docs`** (`TC-E2E-002`) · **D9 → `TC-027`** (fixture không set cột audit) · **D11 → `TC-SEC-015`** 🆕 · **D12 → `TC-027`** 🆕 · **D13 → `TC-SEC-015`** (dùng tài khoản seed thật, L26). *D10 không áp dụng — MF05 không có UC upload*
- [ ] **Không còn `@JsonFormat`** (D4): `grep -rn "@JsonFormat" src/main/java/com/sfms/menu/` → no output
- [ ] **Không khai lại enum D6:** `grep -rn "enum MealType\|enum AllergySeverity" src/main/java/com/sfms/menu/` → no output
- [ ] **Mỗi hàng §13 của TDS được cover:** `UT-05-01…18` → TC-001…020 · `IT-05-01…11` → TC-INT-001…011 · `SEC-05-01…07` → TC-SEC-001…007 · `E2E-05-01` → TC-E2E-001
- [ ] **Mỗi constraint C1–C12 (§17.1 TDS) có ≥ 1 TC** — bảng đối chiếu:

| Constraint | TC cover |
|---|---|
| C1 (FSM BR-068) | TC-007, TC-019, TC-021, TC-022 |
| C2 (BR-067 bất khả sửa) | TC-005, TC-006, TC-INT-007, TC-SEC-014 |
| C3 (dị ứng runtime BR-063) | TC-003, TC-004, TC-INT-006, TC-023 |
| C4 (BR-061 + BR-069) | TC-001, TC-002, TC-011, TC-016 |
| C5 (`audience`, không `menu_variants`) | TC-012, TC-018, TC-020, TC-026 |
| C6 (`AiAssistPort`, MSG-073) | TC-014, TC-015 |
| C7 (shape phân trang + `SEARCH_FIELDS`) | TC-INT-002, TC-INT-003, TC-INT-004, TC-INT-005 |
| C8 (`PUT` = partial merge) | TC-INT-012, TC-INT-014 |
| C9 (tên field + định dạng ngày) | TC-024, TC-E2E-002 |
| C10 (`id` client + khuôn `--`) | TC-025, TC-026 |
| C11 (tự tính lại field dẫn xuất) | TC-023 |
| C12 (phân quyền theo FIELD) | TC-SEC-008, TC-SEC-004, TC-SEC-005, TC-SEC-006 |

- [ ] **Oracle Source** — mọi TC có dòng `Oracle Source` trỏ BR/UC/MSG/luật cụ thể (grep kiểm):
  ```bash
  # Mỗi block TC phải có đúng 1 dòng Oracle Source
  grep -c "^\*\*Oracle Source:\*\*" TestSpec-MF05_Menu_Diet_Management.md   # >= 50
  ```
- [ ] **Không mã bịa:** mọi UC/BR/MSG trong tài liệu này tồn tại trong nguồn (§9 phụ lục — Huy xác nhận)
- [ ] **Contract Existence** — mọi class/method test import đều compile:
  ```bash
  mvn -q test-compile
  # Expected: BUILD SUCCESS, không "cannot find symbol"
  ```
- [ ] **Fixture Isolation** — không shared mutable state:
  ```bash
  grep -rnE "^\s*(private )?static\s+(final\s+)?(Menu|Dish|MenuTemplate|SpecialMealRequest)\s+" \
       src/test/java/com/sfms/menu/ | grep -v MenuTestFixtures.java
  # Expected: no output
  ```
- [ ] **Không cú pháp sai stack:** không còn TypeScript/Prisma trong test và trong tài liệu này:
  ```bash
  grep -rnE "export interface|prisma\.|npx prisma|model [A-Z][A-Za-z]* \{" \
       TestSpec-MF05_Menu_Diet_Management.md src/test/java/com/sfms/menu/
  # Expected: no output
  ```
- [ ] **PDPD:** không mã/tên HS trong log test (`MF05-TC-SEC-009`):
  ```bash
  grep -rE "HS-[0-9]+[A-Z]-[0-9]+" target/*.log
  # Expected: no output
  ```
- [ ] **Red Gate (§5.4)** — tất cả test FAIL với stub, 5 bẫy MF05 đã kiểm thủ công
- [ ] `semantic-audit.ts` chạy trên dữ liệu backend sinh ra: **0 vi phạm** ở 8 luật §11/§11b
- [ ] Swagger đối chiếu §9.2: **26 endpoint** khớp path/method (Minh xác nhận)

**Nghiệm thu chéo theo RACI (`00_` §1):**

- [ ] **Huy** (Lead Traceability): §2 Logic Issues + §9 phụ lục — không mã bịa, L1/L3/L8/L14 đã có kết luận
- [ ] **Mạnh** (Lead Backend/DB): DB assertion dùng đúng bảng/cột §5.2; CHECK constraint `MF05-TC-019` đã vào `V7`
- [ ] **Minh** (Lead FE↔BE): `MF05-TC-E2E-002` xanh với snapshot khoá xuất từ mock hiện tại
- [ ] **Thảo** (chủ module + Lead QA): tự review, ký `Author`
- [ ] **Nam** (Final Approval): chuyển trạng thái tài liệu sang **Approved**

### Suspension Criteria (điều kiện tạm dừng)

| # | Điều kiện | Hành động |
|---|---|---|
| S1 | MF01 (auth/RBAC) hoặc MF02 (`PagedResponse`) chưa xong | Dừng toàn bộ TC-SEC-* và TC-INT-*; vẫn viết được unit TC-001…026 với Mockito |
| S2 | Flyway `V1` chưa có bảng `students`/`classrooms`/`allergy_declarations` | Dừng TC-003, TC-012, TC-INT-008, TC-E2E-001 (thiếu nền dữ liệu BR-063/065) |
| S3 | CF-8 chưa có kết luận | `MF05-TC-022` giữ `@Disabled`; **không** siết `MF05-TC-021` |
| S4 | CF-13 (AI) — mock chưa có nút | 3 TC blocked (`TC-014`, `TC-015`, `TC-SEC-012`) giữ nguyên trạng thái 🔴, **không** tính vào coverage gate |
| ~~S5~~ | ✅ **GỠ 2026-08-03** — L14 đã đóng bằng **BR-220** + `StandardDietCatalog` của MF03; `MF05-TC-013` không còn thuộc MF05 (L19/L20) | — |
| S6 | Docker/Testcontainers không chạy được trên CI | Dừng tầng integration; báo Nam (CI/CD owner) — **không** đổi sang H2 in-memory (mất `ILIKE`, mất CHECK constraint, test vô nghĩa) |
| S7 | Phát hiện lệch schema mới giữa `data_model.puml` và mock | Ghi thêm hàng vào §2 Logic Issues, báo Mạnh; **không** tự đổi entity |

---

## 7. Rollback Plan

> ⚠️ **KHÔNG dùng `npx prisma migrate reset`** như template gốc — dự án dùng **Flyway**, và Flyway Community **không** auto-undo.

```bash
# ── DEV / TEST ONLY ────────────────────────────────────────────────
# 1) Xoá dữ liệu test MF05 (không chạm schema)
# ⚠️ KHÔNG chạm special_meal_requests / dishes-của-module-khác — L19/M05-D04
psql "$SFMS_DB_URL" -c "TRUNCATE menu_dish_substitutes, menu_dishes,
    menu_template_dishes, menu_templates, menus CASCADE;"

# 2) Nếu cần hạ cả schema MF05 — migration BÙ, không undo
#    src/main/resources/db/migration/V10__mf05_rollback.sql        ← V8/V9 là của MF05 (L27)
#      DROP TABLE IF EXISTS menu_dish_substitutes, menu_dishes,
#                           menu_template_dishes, menu_templates,
#                           dish_ingredients, dishes, diet_groups, menus CASCADE;
#      ⚠️ TUYỆT ĐỐI KHÔNG drop special_meal_requests — bảng của MF03 (V4), chứa hồ sơ
#         chế độ ăn THẬT của học sinh.
mvn -f 04_Development/Backend/pom.xml flyway:migrate

# 3) Testcontainers không cần rollback — container bị huỷ sau mỗi lần chạy
#    (chỉ xoá khi dùng .withReuse(true))
docker rm -f $(docker ps -aq --filter "label=org.testcontainers=true") 2>/dev/null || true

# 4) Revert code test
git checkout -- src/test/java/com/sfms/mf05/ src/test/resources/fixtures/

# 5) Gỡ Red Phase stub trước khi merge
git rm src/main/java/com/sfms/mf05/service/impl/*RedStub.java
```

**Quy tắc rollback quan trọng:**

| # | Quy tắc | Lý do |
|---|---|---|
| R1 | **Không** chạy bước 2 trên môi trường có dữ liệu thật | Flyway không undo được; `DROP CASCADE` xoá cả `menu_dishes` mà MF06/MF09 tham chiếu qua `dishId` |
| R2 | Flyway `V1` (schema chung) **do Nam sở hữu** — không rollback một phía | `00_` §4b: migration là tài nguyên chung có thứ tự |
| R3 | Số hiệu migration MF05 là `V7`/`V8` (theo TDS §11.3) — nếu đụng module khác, **hỏi Nam** trước khi đổi | Hai người cùng tạo `V7__` là conflict cứng |
| R4 | Rollback test **không** kéo theo rollback TDS — TDS là tài liệu nộp report | `00_` §0 |

---

## 8. CASE 2.0 Anti-Pattern Detection

> Checklist cho reviewer khi test cases được AI hỗ trợ generate (§1.1: AI Assisted = Yes).

| AP-ID | Anti-Pattern | Dấu hiệu trong TDD spec | Check | Gate chặn |
|-------|-------------|--------------------------|-------|-----------|
| AP-AI-001 | **Unconstrained Generation** | TC không reference constraint/BR nào | ☐ | G-0 — mọi TC đã có `Oracle Source` + `Constraint Ref` (§6 grep gate) |
| AP-AI-002 | **Green-from-Birth** | Test PASS với stub ném exception (§5.4) | ☐ | **G-2 ★** — Red Gate + 5 bẫy MF05 |
| AP-AI-003 | **Implicit Decision** | Test assume quyết định kiến trúc không có ADR | ☐ | G-1 — mọi giả định kiến trúc trỏ `ADR-M05-01…04` hoặc `ADR-000` |
| AP-AI-004 | **Layer Violation** | Test verify controller có business logic | ☐ | G-4 — §3 TDS-01 phân tầng rõ: BR chỉ test ở `service/`, controller chỉ test authz/shape |
| AP-AI-005 | **Hallucinated Contract** | Test import service/type không tồn tại | ☐ | G-3 — `mvn test-compile` gate (§6) |

**Rủi ro riêng của MF05 — reviewer kiểm thêm:**

| # | Rủi ro | Dấu hiệu | Cách kiểm |
|---|---|---|---|
| M1 | **Bịa mã BR/MSG** — MF05 dễ nhất vì §1.6 lệch thứ tự với MFxx (`MF05 → §1.6`, không phải §1.5) và CF-1 làm BR-070…075 lẫn nghĩa | Xuất hiện BR-070…BR-075 với nghĩa Menu, hoặc BR-080/BR-081/BR-082 trong cột oracle chính thức | Đối chiếu §9 phụ lục với `traceability_matrix.md` §1.6 — Huy |
| M2 | **Đặt MSG mới** — 6 error code (`MENU-061/065/066/067/069`) **không** có MSG tương ứng | TC assert một chuỗi MSG không tồn tại trong `error_message.md` | `grep` từng MSG ID trong `error_message.md`; error code kỹ thuật thì OK, **MSG mới thì không** |
| M3 | **Lẫn nguồn MF05 ↔ MF06** | Fixture dùng `ReceivingSession`/`InventoryBatch` như entity chính | TDS §5.2 chỉ có 6 bảng MF05 — bất kỳ bảng khác là dấu hiệu lẫn module |
| M4 | **Tự chọn bên đúng cho CF** | TC khoá cứng một bên của CF-8/L14 mà không có `@Disabled` + ghi chú | Đối chiếu policy cuối §2 |
| M5 | **Test tautology ở FSM** | `MF05-TC-021` chỉ assert "ném exception" cho mọi ô ❌ | Bẫy #2 §5.4 |
| M6 | **Cú pháp sai stack** (template gốc là TypeScript/Prisma) | `export interface`, `prisma.client`, `makeEntity()`, `npm test`, `npx prisma` | Gate `grep` §6 |

**Kết quả review:**

- [ ] Không phát hiện anti-pattern nào → TDD spec **approved**
- [ ] Phát hiện AP → ghi vào bảng dưới → fix trước khi implement

| AP detected | TC ID | Mô tả | Fix action | Fixed? |
|------------|-------|-------|------------|--------|
| `AP-AI-___` | `MF05-TC-___` | | | ☐ |

---

## 9. Phụ lục — Danh sách mã UC/BR/MSG đã trích dẫn

> Bảng đối chiếu nhanh cho **Huy (Lead Traceability)**. Mọi mã dưới đây phải tồn tại đúng nguồn; **không có mã nào do AI đặt ra**.

### 9.1. Use Case (10 — đúng `traceability_matrix.md` §1.6, không thiếu không thừa)

| UC ID | Tên UC (nguyên văn) | Actor | TC cover |
|---|---|---|---|
| UC-020 | Create Menu | Service Caterers | TC-011, TC-023, TC-025, TC-INT-001 |
| UC-096 | Update Menu | Service Caterers | TC-006, TC-010, TC-INT-012 |
| UC-075 | Request AI Menu Recommendations | Service Caterers | TC-014, TC-015, TC-SEC-012 ⚠️ blocked L13 |
| UC-023 | Create Special Meal Plan | Service Caterers | TC-012, TC-018, TC-020, TC-026 *(TC-013/TC-SEC-011 chuyển sang MF03 — L19)* |
| UC-087 | Submit Menu For Approval | Service Caterers | TC-001, TC-002, TC-003, TC-016, TC-INT-006, TC-INT-014 |
| UC-006 | Approval / Reject Menu | School Administration | TC-007, TC-008, TC-009, TC-021, TC-022, TC-SEC-003, TC-SEC-008 |
| UC-048 | Manage Menus | Service Caterers | TC-005, TC-INT-002…005, TC-INT-013 |
| UC-128 | View List Menu | School Administration | TC-INT-002, TC-INT-003, TC-SEC-006 |
| UC-134 | View Menu Detail | School Administration | TC-023, TC-E2E-002 |
| UC-135 | View Menu Information | Parent | TC-INT-008, TC-INT-009, TC-SEC-005, TC-E2E-001 |

### 9.2. Business Rules

**Trong cột BR của §1.6 (oracle chính thức):**

| BR | Nội dung (tóm, nguyên văn `business_rules.md`) | § nguồn | TC cover |
|---|---|---|---|
| BR-061 | Nộp duyệt ít nhất **3 ngày** trước tuần sử dụng | §7 | TC-001, TC-002, TC-016 |
| BR-062 | Đáp ứng chỉ tiêu dinh dưỡng theo nhóm tuổi | §7 | TC-015 (MSG-067 cảnh báo) |
| BR-063 | Bắt buộc kiểm xung đột dị ứng; nghiêm trọng → **chặn** | §7 | TC-003, TC-004, TC-INT-006, TC-SEC-009 |
| BR-064 | AI ưu tiên nguyên liệu **trong kho** + giới hạn chi phí | §7 | TC-015 |
| BR-065 | HS dị ứng xác minh/chế độ ăn đặc biệt → **bắt buộc** kế hoạch bữa ăn riêng | §7 | TC-004, TC-012, TC-018 |
| BR-066 | Danh mục chế độ ăn: Halal, Vegetarian, Gluten-Free, Nut-Free… | §7 | TC-013 ⚠️ **L14** |
| BR-067 | Approved/Published **không** sửa/xóa | §7 | TC-005, TC-006, TC-INT-007, TC-SEC-014 |
| BR-068 | `Draft → Pending Approval → Approved/Rejected → Published`; chỉ School Admin duyệt | §7 | TC-007, TC-010, TC-019, TC-021, TC-022, TC-SEC-003, TC-SEC-008 |
| BR-069 | Khai **toàn bộ** nguyên liệu + allergen trước khi nộp | §7 | TC-011 |
| BR-071 | *(canonical: chứng nhận lô hàng — thuộc MF06)* | §6 | ⚠️ **L1/CF-1** — **không** dùng làm oracle |
| BR-073 | *(canonical: GPS + timestamp nhận hàng — thuộc MF06)* | §6 | ⚠️ **L1/CF-1** — **không** dùng làm oracle |
| BR-156 | Gemini AI đề xuất thực đơn tối ưu (dinh dưỡng/dị ứng/tồn kho/trần chi phí) | §14 | TC-014, TC-015 |
| BR-201 | PH liên kết tối đa **5** hồ sơ HS | §4 | TC-INT-008, TC-SEC-005 |

**Nhắc tới nhưng KHÔNG dùng làm oracle chính thức (ngoài §1.6 — chờ Huy):**

| BR | Vì sao nhắc | Xử lý |
|---|---|---|
| BR-080 (trần chi phí) | mock enforce thật (`cost_ceiling`, luật `DA_DUYET_MA_VAN_VUOT_TRAN`) | **L2/CF-2** — oracle của `TC-017` là **luật semantic-audit**, không phải BR-080 |
| BR-081 (món thay thế top-5 allergen) | UC-023 Normal Flow bước 6 còn nhắc | **L3/CF-3** — **đã XOÁ 2026-07-01**, không có TC nào |
| BR-082 (chế biến riêng, tránh nhiễm chéo) | xuất hiện trong `reviewNote` mẫu của `SMR-2026-035`-family | **L1/CF-1** — không enforce tự động, không TC |
| BR-007 (RBAC 7 role) | nền của §16 Authorization Matrix | oracle của TC-SEC-* là **§16**, BR-007 là nguồn gián tiếp |
| BR-157 (archive hội thoại AI) | TDS §4.3 yêu cầu audit lần gọi AI | oracle của `TC-014` (phát event) là **TDS §4.3 + ADR-M05-03** |
| BR-170 (audit actor + IP) | TDS §4.3/§14.2 | **oracle của `TC-INT-010` là TDS §4.3/§14.2** — ghi rõ để Huy đối chiếu |

### 9.3. System Messages (13 — đều tồn tại trong `error_message.md`)

| MSG ID | Nội dung nguyên văn | TC cover |
|---|---|---|
| MSG-004 | `Session expired. Please log in again.` | TC-SEC-001 |
| MSG-005 | `Access denied: You do not have permission to access this resource.` | TC-007, TC-SEC-002…006, TC-SEC-008, TC-SEC-010, TC-SEC-015 |
| MSG-029 | `Profile updated successfully.` | *(§1.6 gán cho UC-096; MF05 dùng MSG-074 cho cập nhật thực đơn — ghi nhận để Huy xác nhận việc §1.6 liệt kê MSG-029 ở UC-096)* |
| MSG-039 | `Menu draft submitted for approval successfully.` | TC-001, TC-INT-001 |
| MSG-040 | `Menu approved and published successfully. Notifications sent to parents.` | TC-008, TC-022, TC-INT-001 |
| MSG-051 | `Allergy conflict detected: Dish [Dish Name] contains allergen [Allergen] affecting [Number] students.` | TC-003, TC-INT-006, TC-SEC-009 |
| MSG-067 | `AI menu analysis: Nutritional diversity is below the required 3-group minimum.` | TC-015 |
| MSG-071 | `Menu draft was saved successfully.` | TC-INT-001 |
| MSG-072 | `Special meal plan saved successfully.` | TC-018, TC-020 *(UC-023 phía **thực đơn riêng**; TC-013 đã chuyển sang MF03 — L19)* |
| MSG-073 | `Gemini AI Service is currently unavailable. Please try again later.` | TC-014 |
| MSG-074 | `Menu updated successfully.` | TC-006, TC-009 |
| MSG-075 | `Dish rotation limit exceeded: [Dish Name] cannot be served more than twice a week.` | TC-010 |
| MSG-090 | `No menu has been published for the selected week yet. Please check back later.` | TC-INT-009 |

### 9.4. Error code kỹ thuật (TDS §10 — cập nhật 2026-08-03 theo **M05-D07**)

**Đã có MSG chính thức:** `MENU-004`/MSG-004 (401) · `MENU-005`/MSG-005 (403) · `MENU-051`/MSG-051 (409) · `MENU-067c`/MSG-067 (422, warning) · `MENU-073`/MSG-073 (503) · `MENU-075`/MSG-075 (409) · `MENU-090`/MSG-090 (404)

**Dùng MSG dùng-chung-mọi-module** *(mới — trước đây thiếu hẳn)*: `MENU-404`/**MSG-137** (404, gồm **ngoài phạm vi dữ liệu** — Caterer chạm thực đơn đơn vị khác, Parent xem con không phải của mình, theo tiền lệ MF01/MF04 M04-D10) · `MENU-422`/**MSG-138** (422 validate) · `MENU-409`/**MSG-139** (409, D5 trùng `id`) · `MENU-429`/**MSG-141** (429 rate limit AI).

**Chờ Huy cấp số (4 mã):** `MENU-067` (409) · `MENU-061` (422) · `MENU-069` (422) · `MENU-065` (422) — chuỗi tiếng Anh đã soạn ở `TDS-MF05_Backend.md` §10.

**Đã xoá:** ~~`MENU-066`~~ — `dietaryType` thuộc MF03 (`STU-066`/**MSG-144**/BR-220), xem **L19/L20**.

> **Cách test trong lúc chờ:** assert **`$.error.code`** (đã chốt, không đổi) + `$.error.details` là mảng; **chưa** assert chuỗi `message` của 4 mã đang chờ. Khi Huy cấp MSG, thêm assert chuỗi — **không** phải viết lại TC. Đây là lý do 4 mã này **không chặn** Approve.

### 9.5. Luật `semantic-audit.ts` đã port (8/8 luật §11 + §11b liên quan MF05)

| Tên luật gốc | § | TC cover |
|---|---|---|
| `GUI_DUYET_SAU_NGAY_PHUC_VU` | §11 | TC-016 |
| `DA_DUYET_MA_THIEU_NGUOI_DUYET` | §11 | TC-019 |
| `DA_DUYET_MA_VAN_VUOT_TRAN` | §11 | TC-017 |
| `NOP_DUYET_MA_NHOM_CHE_DO_AN_CHUA_CO_GI_AN` | §11b | TC-012 |
| `SUAT_RIENG_TRO_THUC_DON_KHONG_TON_TAI` | §11b | TC-026 (ca menu không tồn tại → 404) |
| `SUAT_BO_SUNG_KHONG_RO_NGUON_GOC` | §11b | TC-018 (3 nhánh), TC-020 |
| `SUAT_RIENG_KHONG_CO_NGUOI_AN` | §11b | TC-012 (ca nhóm không có HS → không tạo gap) |
| `BANG_NAU_MON_VUOT_SI_SO` | §11c | *(cook-sheet — đọc MF05 nhưng thuộc phạm vi MF09; ghi nhận để Minh cover ở TestSpec-MF09)* |

### 9.6. `00_Common_Conventions.md` D1–D10 + workflow §6 D11–D13 *(v1.1 → mở rộng v1.2)*

| Quyết định | Nội dung áp dụng | TC cover | Ghi đè gì |
|---|---|---|---|
| **D1** | JSON `id` = `code`; PK UUID; path `{code}`; FK trong JSON dùng `code` | `TC-INT-001`, `TC-025`, `TC-026`, `TC-INT-010` | TDS §5.2 PK `VARCHAR(32)` → **L15**. ⚠️ D1 lý do #4 ghi rõ khuôn `{menuId}--{dietGroupId}` của MF05 *"không vỡ"* vì `id` vẫn là mã nghiệp vụ |
| **D2** | Envelope `{"error":{"code","message","details"}}` | mọi TC error path (`$.error.code`) | v1.0 chỉ nói "body code" → **L16** |
| **D3** | `baseURL = /api/v1` | `TC-E2E-002` | — (v1.0 đã dùng `/api/v1`) |
| **D4** | JSON ngày **ISO-8601**; bỏ `@JsonFormat` | **`TC-024` (viết lại v1.1)**, `TC-E2E-002` | CF-10 + TDS §17.1 **C9** (`dd/MM/yyyy`) → **L17** |
| **D5** | Nhận `id` client → dùng làm `code`, trùng 409; vắng thì server sinh; luôn trả `id` | `TC-025`, `TC-026` | — ⚠️ **D5 lấy chuẩn TỪ MF05 CF-11** rồi áp cho 13 module |
| **D6** | 8 enum dùng chung `common/enums/`, SCREAMING_SNAKE | `TC-003`, `TC-004` | TDS §5.2 khai `MealType` trong package module → **L18/L23** |
| **D7** | `message` **tiếng Anh** nguyên văn `error_message.md` | `TC-003`, `TC-SEC-001` | — (v1.0 đã dùng nguyên văn tiếng Anh) |
| **D8** *(v1.2)* | Annotation springdoc S1–S8; contract test đọc `/v3/api-docs` | `TC-E2E-002`, `TC-SEC-015` | — (đang áp dụng thực tế trên 11/11 controller MF01+MF02) |
| **D9** *(v1.2)* | `created_by`/`updated_by` reserved cho `BaseEntity` | `TC-027` | TDS §5.2 khai `created_by VARCHAR(128) NOT NULL` → **L22/M05-D02** |
| **D10** *(v1.2)* | Upload qua `StorageService` | *(không áp dụng)* | MF05 **không có UC upload** — tài liệu đính kèm thuộc MF03 |
| **D11** *(v1.2)* | Response đổi shape theo vai trò phải khai `oneOf` | 🆕 `TC-SEC-015` | Chưa TC nào phủ → **L24/M05-D08** |
| **D12** *(v1.2)* | Key JSON khớp hai chiều; field dẫn xuất read-only | 🆕 `TC-027` | Bẫy `dishes` số-vs-mảng → **L25/M05-D09** |
| **D13** *(v1.2)* | Seed có tài khoản cho mọi role trong `@PreAuthorize` | `TC-SEC-015` (dùng `U-0022`, `U-0005`, `U-0004`) | ✅ đã đạt — `V900` đủ 7 role → **L26/M05-D10** |

> ✅ **ĐÃ LÀM XONG 2026-08-03** — `TDS-MF05_Backend.md` **v1.2**: §5.2 viết lại (PK UUID + `code`, `extends BaseEntity`, FK UUID, bỏ `special_meal_requests`) · §9 ngày ISO + khối D11/D12 · §10 ánh xạ MSG · §16 D13 · §17 sửa C9/C10 + thêm C13–C15. Quyết định ở **§2.4 (M05-D01…M05-D16)**, việc còn treo ở **§2.5**.

### 9.7. Tổng kết coverage

| Nhóm | Số TC | Trạng thái ban đầu |
|---|---|---|
| Unit (service layer) | **26** | −1 gỡ (`TC-013` → MF03) +1 mới (`TC-027`, D12) · 🔴 24 · ⚠️ 2 blocked (L13) |
| Security (RBAC / OWASP) | **14** | −1 gỡ (`TC-SEC-011` → MF03) +1 mới (`TC-SEC-015`, D11) · 🔴 13 · ⚠️ 1 blocked (L13) |
| Integration (API → DB, Testcontainers) | **14** | 🔴 14 |
| E2E | **2** | 🔴 2 |
| **Tổng** | **56** | 🔴 53 · ⚠️ 3 blocked (L13/M05-D16 — AI, trả 503 fallback) · 1 `@Disabled` (TC-022, CF-8) |

| Đối chiếu | Kết quả |
|---|---|
| Condition §3 TDS-03 có TC | **34/34** *(v1.1 thêm COND-30…32; v1.2 gỡ COND-09 → MF03, thêm COND-09b/33/34)* |
| Quyết định D1–D13 có TC | **12/12** *(v1.2; D10 không áp dụng — MF05 không có UC upload)* |
| Constraint C1–C15 (§17.1 TDS) có TC | **15/15** *(v1.2 thêm C13 → `TC-027`/`TC-INT-001`, C14 → `TC-012`, C15 → `TC-SEC-015`/`TC-027`)* |
| Hàng §13 TDS (`UT`/`IT`/`SEC`/`E2E`) có TC | **37/37** |
| UC §1.6 có TC | **10/10** |
| Luật `semantic-audit.ts` §11/§11b có TC | **7/7** (luật thứ 8 thuộc MF09) |
| Logic Issue L1–L28 được xử lý rõ | **28/28** *(v1.1 thêm L15–L18; v1.2 thêm **L19–L28** từ code thật, và **đóng L14**)* |

---

*TestSpec MF05 v1.2 (Approved 2026-08-03) — theo `06_Document/Templates/PHASE-4_Test-Spec.md` (ISO/IEC/IEEE 29119-3:2021), tích hợp CASE 2.0 Red Gate + Anti-Pattern Detection.*
*Cú pháp đã dịch sang stack thật của dự án: **Java 25 / Spring Boot 4.1.x / JUnit 5 / Testcontainers / Flyway** — không dùng TypeScript/Jest/Prisma của template minh hoạ.*
