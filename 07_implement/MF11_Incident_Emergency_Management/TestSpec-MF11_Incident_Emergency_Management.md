# TEST-DRIVEN DEVELOPMENT SPECIFICATION
# MF11 — Incident & Emergency Management (Quản lý Sự cố & Khẩn cấp, gộp Violation Management)

| Field | Value |
|-------|-------|
| **Document ID** | `SFMS-MF11-TDD-001` |
| **Version** | `1.1` |
| **Date** | `2026-08-04` |
| **Status** | `Approved` |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 — Software Testing Part 3: Test Documentation |
| **Document Owner** | Trần Tuấn Minh (MinhTTHE186258) |
| **Author** | Trần Tuấn Minh — UI/UX & Frontend, chủ module MF09/MF11, Lead FE↔BE contract Reviewer |
| **Reviewed by** | `[ ] Nguyễn Phương Thảo — Lead Test/QA — Pending` · `[ ] Bùi Quang Huy — §2 Traceability (UC/BR/MSG) — Pending` · `[ ] Nguyễn Đức Mạnh — §5 Schema + §9 API — Pending` |
| **DPO Sign-off** | `[ ] Pending` — **bắt buộc**: module chứa **dữ liệu sức khoẻ trẻ em** (triệu chứng, sơ cứu, đánh giá rủi ro lâm sàng, kết quả xét nghiệm) — nhóm dữ liệu nhạy cảm **nhất toàn hệ thống**, cao hơn MF09 một bậc |
| **Approved by** | `[ ] Nguyễn Văn Nam — Final Approval Pending` |
| **Classification** | `Internal — Confidential` |

**References (nguồn oracle — mọi expected value trích từ đây, KHÔNG dùng giả định):**

| # | Nguồn | Dùng cho |
|---|-------|----------|
| 0 | **`07_implement/00_Common_Conventions.md` — D1–D7 ✅ Approved 30/07/2026 · D9/D10 ✅ Approved 31/07/2026 · D8 ⏳ chờ Nam** | **Quy ước ưu tiên CAO NHẤT** — ghi đè ADR module khi xung đột (xem §2.1 L1–L10). D4 **nêu đích danh ADR-M11-5** để ghi đè |
| 1 | `07_implement/MF11_Incident_Emergency_Management/TDS-MF11_Backend.md` **v1.1** (`SFMS-MF11-IMP-001`) | Spec kỹ thuật gốc — §5.2 schema/CHECK/RULE, §7 event catalog, §8 interface + **8 port**, §9 API, §10 error code, §13 kịch bản (34 unit + 18 integration + 12 E2E), §16 auth matrix, §17 constraint **C1–C15** |
| 2 | `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` **§1.11** | Nguồn CHUẨN duy nhất của UC↔BR↔MSG cho MF11 (**20 UC**) — không nhận mã ngoài bảng này |
| 3 | `02_Requirements/02_UseCases/MF11_Incident_Emergency_Management.md` | Normal/Alternative Flow. ⚠️ UC-073/074 và UC-082 EX1 **lạc hậu so với BR** (L15, L24) |
| 4 | `02_Requirements/00_BusinessRules/business_rules.md` §11 (BR-146…BR-155, **BR-216**, **BR-217**) · §18 (BR-183…BR-190, **BR-211**) | Nội dung BR làm **oracle** |
| 5 | `02_Requirements/00_BusinessRules/error_message.md` **v1.11** *(v1.1 — trước là v1.8; v1.11 cấp **MSG-149…158** cho 10 mã `IEM-*` + **MSG-159/160/161** thay MSG-029)* | Chuỗi MSG **nguyên văn tiếng Anh**. MF11 dùng MSG-004/005/023/024\*/085/086/087/088/089/095/100/101/029/048/049 + dùng chung **MSG-137/138/139/141** + MSG-008/009 (file) |
| 6 | `03_Design/08_FrontEnd/Web/scripts/semantic-audit.ts` §4 (mẫu ↔ sự cố), §5 (phiếu bàn giao), §9 (sự cố/dòng thời gian/CAPA), §9b (cách ly lô) | **12 luật port** sang test — tên luật copy **nguyên văn** từ script. `DONG_SU_CO_MA_CON_LO_KHOA` **KHÔNG port** (L21) |
| 7 | `03_Design/08_FrontEnd/Web/src/lib/mock-data.ts` | **Nguồn fixture/seed** (§3 TDS-05) — mã bản ghi THẬT (`RPT-2026-030`, `REC-2026-021`, `INC-2026-014`, `CAPA-2026-033`, `VIO-2026-008`, `IEV-014-01`, `BATCH-MP-8820`, `INSP-20260630-L`, `SMP-20260630-03`, `U-0002`, `CAT-001`), **không bịa**. ⚠️ 5 nhóm dòng seed **vi phạm bất biến** — xem L36/L37 |
| 8 | `03_Design/08_FrontEnd/Web/src/lib/api/mock-server.ts` + `db.ts` | Hợp đồng REST hiện tại (generic `PUT`, `paginate()`) — chỉ để biết FE đang gọi gì, **không** dùng làm oracle định dạng |
| 9 | `07_implement/00_Team_Work_Assignment_Plan.md` §2b (2b-1…2b-7) | Hợp đồng phân trang/search/filter dùng chung |
| 10 | `03_Design/01_Diagrams/01_Class_Diagram/data_model.puml` — ràng buộc **C1** | *"Sự cố chỉ đóng được khi mọi CAPA đã COMPLETED"* — đã từng bị vi phạm ngay trong dữ liệu mẫu |
| 11 | `03_Design/02_ADR/ADR-000.md` | Stack + stub external service ở dev (SMTP/Redis tắt ⇒ BR-148 test bằng outbox event, **không** gửi thật) |
| 12 | `00_Policies/39_2006_QD-BYT_...md` · `00_Policies/specialreport2173_1.md` · PDPD (NĐ 13/2023/NĐ-CP) | Căn cứ pháp lý của **cửa sổ điều tra 24h** (BR-149), **mẫu biên bản ký tay** (BR-211/BR-154), và **cách ly dữ liệu sức khoẻ trẻ em** (§16, C14) |

> **⚠️ RÀNG BUỘC STACK — tài liệu này KHÔNG dùng cú pháp mẫu của `PHASE-4_Test-Spec.md`.**
> Template gốc minh hoạ bằng TypeScript/Jest/Prisma. Dự án dùng **Java 25 LTS · Spring Boot 4.1.x · PostgreSQL 16 · Flyway · Spring Security + JWT · MapStruct · springdoc-OpenAPI**.
> Test = **JUnit 5 + AssertJ + Mockito** (unit) · **`@SpringBootTest` + Testcontainers `PostgreSQLContainer` + MockMvc** (integration/security/contract) · fixture = **Java builder** · rollback = **Flyway migration bù** (KHÔNG `npx prisma migrate reset`).

> **⚠️ ÁP DỤNG `00_Common_Conventions.md` — thứ tự ưu tiên khi xung đột: `00_Common_Conventions.md` > ADR module (TDS §3) > hợp đồng mock FE.**
> Hệ quả cho tài liệu này: **D1** JSON `id` = `code` → DB assertion dùng `WHERE code = ?` · **D2** lỗi trong envelope `{"error":{"code","message","details"}}`, `details` luôn là **mảng** · **D3** mọi path `/api/v1/...` · **D4** ngày/giờ JSON **ISO-8601** → **ghi đè ADR-M11-5 (nửa sau) + C12 + §17.2 mục 12** của TDS · **D5** nhận `id` client, trùng → 409 **MSG-139** · **D6** enum **SCREAMING_SNAKE** · **D7** `message` **tiếng Anh** nguyên văn `error_message.md` → ghi đè 10 message tiếng Việt trong §9.2/§10 · **D9** MF11 **đã đúng khuôn** (`created_by_id`, không đụng cột reserved) · **D10** MF11 có **3 luồng upload** (ảnh bằng chứng điều tra, ảnh bằng chứng CAPA, **ảnh biên bản đã ký** BR-211) ⇒ bắt buộc đi qua `common/storage/StorageService`.

> **Quy ước TDD:** tài liệu này mô tả test cases **TRƯỚC** khi viết production code.
> Thứ tự: viết test (`*Test.java`) → chạy → xác nhận **FAIL 🔴** → implement → **PASS 🟢** → refactor 🔵.
> Không mark ✅ nếu `mvn verify` chưa xanh. Test data = **SYNTHETIC**, trường `SCH-001`, nhà thầu `CAT-001` — **không dùng dữ liệu sức khoẻ thật của trẻ em**.

---

## CHANGELOG

> **Policy 4.4 — Immutable History:** không xóa thông tin cũ.

| Ngày | Người thực hiện | Nội dung thay đổi |
|------|-----------------|-------------------|
| 2026-08-01 | Trần Tuấn Minh | Khởi tạo v1.0 — TDD spec cho MF11. Nguồn: `TDS-MF11_Backend.md` **v1.1** (§13 làm xương sống, §5.2/§7/§9/§10/§16/§17), **`00_Common_Conventions.md` D1–D10**, traceability **§1.11** (20 UC), `business_rules.md` §11 + §18, `error_message.md` **v1.11** *(v1.1 — trước là v1.8)*, `semantic-audit.ts` §4/§5/§9/§9b (**12 luật port**, 1 luật **không** port), fixture từ `mock-data.ts`. Tổng **77 test case**: 27 unit · 16 security · 23 integration · 5 contract · 6 E2E. Ghi nhận **41 Logic Issue**: **L1–L10** là D1–D10 **ghi đè/bổ sung** ADR-M11-5 và C12 (nặng nhất: **L4** — D4 ISO ghi đè ADR-M11-5 + C12 + toàn bộ ví dụ §9.2 đang là `dd/MM/yyyy`; **L7** — 10 mã lỗi cục bộ của MF11 **vẫn chưa có MSG chính thức** ở v1.8 dù MF08/MF09 đã được cấp ⇒ **thiếu oracle message**), **L11–L31** kế thừa CF-M11-1…21, **L32–L41** là lệch **mới phát hiện** khi soạn TestSpec (mã đối tác `PTN-0007`/`PTN-0003` **không tồn tại**; `dish-ga-rang-gung` **không tồn tại** vì Dish trong seed **không có id**; `STF-007` **không tồn tại**; 5 nhóm dòng seed vi phạm `ck_vio_resolution`/`ck_inc_closed`/C1; traceability §1.11 ghi cửa sổ **48h** trong khi BR-149 bản 27/07 là **24h**). |
| 2026-08-04 | Trần Tuấn Minh | **v1.1 — đồng bộ với `TDS-MF11_Backend.md` v1.2 (Bước 0 + Bước 1). Status `Draft` → `Approved`.** (1) **Bổ sung 2 TC bắt buộc** theo `02_Module_Completion_Workflow.md` §7 Bước 1 việc 3 — cả 13 TestSpec viết TRƯỚC khi D11–D13 tồn tại nên không bộ nào phủ; `grep -n "oneOf" TestSpec-MF11` trước bản này ra **0**: **`TC-CON-006`** (D11 — `GET /incidents` đổi **tập field** theo vai, assert `/v3/api-docs` khai `oneOf` và `items` **đúng kiểu**, tên schema giữ tiền tố `PagedResponse` theo S7) và **`TC-030`** (D12 — gửi field **dẫn xuất** + field đúng tên **sai kiểu**, assert **DB không đổi**). (2) **Package `com.sfms.incident` → `com.sfms.mf11`** (10 chỗ). (3) **L33 ĐÓNG** — không còn là điểm chặn: `V8__mf05_menu_diet.sql:35` đã có `dishes.code VARCHAR(64) UNIQUE` và `ReferenceResolver.dishIdByCode()` đang chạy (`M11-D12`). (4) **L32/L34 ĐÓNG bằng ràng buộc DB** thay vì bằng kỷ luật fixture: `M11-D10` thêm FK `assignee_supplier_id → partners(id)` ⇒ mã bịa `PTN-0007` **fail ở tầng DB**. (5) **L36/L37 có phương án chốt** (`M11-D18`) — không còn "chờ Mạnh quyết" mà là "Mạnh veto được khi review". (6) **L35 vẫn treo nhưng đã có bản soạn**: `error_message.md` v1.9 grep `IEM-` ra **0**; TDS §10.1 nay kèm **MSG-149…158** tiếng Anh chờ Huy ký ⇒ phần `@Disabled` thu hẹp từ "10 nhóm assert message" xuống "chờ 1 chữ ký". (7) **Bổ sung `TC-INT-024`** — kiểm `M11-D08`: MF11 phải expose bean thật cho `com.sfms.mf08.port.IncidentStatusPort` **và** xoá `IncidentStatusPortStub`; đây là lỗi **không làm test đỏ** nếu bỏ sót (stub chỉ `log.warn`) nên phải có TC riêng. |
| 2026-08-04 *(chiều)* | Trần Tuấn Minh | **v1.1 đợt 2 — nhóm duyệt toàn bộ 7 mục treo. Không còn `@Disabled` nào vì thiếu oracle hoặc vì chờ người chốt.** **(1) `error_message.md` → v1.11**: cấp **MSG-149…158** cho 10 mã `IEM-*` và **MSG-159/160/161** thay `MSG-029`. ⇒ **11 chỗ `@Disabled` gỡ bỏ**; mọi TC error-path nay assert **cả ba** (`code` + HTTP + `message` nguyên văn tiếng Anh) thay vì 2/3 — đóng **L7**, **L16**, **L35**. **(2) `traceability_matrix.md` → v1.4** §1.11: thêm **BR-216/BR-217/BR-188**, **BR-184 → Deprecated**, **UC-179 đổi tên** *"Record Staff Violation"* → *"Record **Unit** Violation"*, **cửa sổ món nghi vấn 48h → 24h**. ⇒ oracle của MF11 **không còn dòng nào phải ghi *"chưa có ở §1.11"*** — đóng **L11**, **L13**, **L38**, một phần **L12**. **(3) `M11-D19` (Nam)** — `TC-SEC-014` **bỏ `@Disabled`**, test cả 2 vế `OR` của phạm vi Service Caterers: bằng chứng schema `BATCH-MP-8820 → SUP-003` vs CAPA `→ CAT-001` cho thấy vế "lô cách ly thuộc NCC" **không dư** — đóng **L28**. **(4) `M11-D20` (đội dự án)** — `scope = quarantineBatches`; nhánh *"toàn sự cố"* **loại bỏ hẳn**, không chuyển thành `@Disabled` — đóng **L23**. **(5) `M11-D18` (Mạnh)** duyệt nguyên phương án sửa seed — đóng **L36/L37**. **Còn lại đều là hạng mục Bước 2/Bước 3:** 3 port `M11-D08` (`TC-INT-024` đã phủ) · mock FE (Minh) · `semantic-audit.ts` (Thảo) · `data_model.puml` (Mạnh). |

---

## MỤC LỤC

1. [Thông tin Module](#1-thông-tin-module) · 1.1 [AI Generation Context](#11-ai-generation-context-case-20)
2. [Logic Issues Resolved](#2-logic-issues-resolved)
3. [Test Design Specification](#3-test-design-specification)
4. [Test Case Specification](#4-test-case-specification) · [Unit](#41-unit-test-cases) · [Security](#42-security-test-cases) · [Integration](#43-integration-test-cases) · [Contract](#44-contract-test-cases--openapi-v3api-docs-d8) · [E2E](#45-e2e-test-cases)
5. [Red-Green-Refactor Tracker](#5-red-green-refactor-tracker)
6. [Entry / Exit Criteria](#6-entry--exit-criteria)
7. [Rollback Plan](#7-rollback-plan)
8. [CASE 2.0 Anti-Pattern Detection](#8-case-20-anti-pattern-detection)
9. [Phụ lục — Danh sách mã UC/BR/MSG đã trích dẫn](#9-phụ-lục--danh-sách-mã-ucbrmsg-đã-trích-dẫn)

---

## 1. Thông tin Module

| Field | Value |
|-------|-------|
| **Feature / Module ID** | `MF11` — Incident & Emergency Management (+ Violation Management) |
| **Bounded Context** | Food Safety Incident & Non-Conformance |
| **Spec gốc** | `SFMS-MF11-IMP-001` v1.1 (`TDS-MF11_Backend.md`) |
| **Priority** | 🟠 **P1** — **mắt xích CUỐI** của trục xương sống (`Login → Menu → phân suất & thống kê theo món → **tạo sự cố** → **điều tra: sự cố → HS → món → lô → NCC → mẫu**`) |
| **Số UC trong phạm vi** | **20** theo traceability §1.11 — khớp đúng kế hoạch §3. Mọi UC đều có spec trong UC file (khác MF09, nơi UC-079 thiếu spec) |
| **Data Classification** | **PII trẻ em + DỮ LIỆU SỨC KHOẺ**: họ tên HS, lớp, triệu chứng, sơ cứu, đánh giá rủi ro lâm sàng, kết quả xét nghiệm, tình trạng hồi phục |
| **Compliance Scope** | **PDPD (NĐ 13/2023/NĐ-CP)** · **QĐ 39/2006/QĐ-BYT** (cửa sổ điều tra ngộ độc) · mẫu biên bản `00_Policies/specialreport2173_1.md` (BR-211, BR-154) |
| **Upstream Dependencies** | MF01 (JWT `userId/role/schoolId`), MF02 (`Classroom` — lớp chủ nhiệm), MF03 (`Student`), MF04 (`Partner` — nhà thầu + đại diện ký), MF05 (`Dish` — món nghi vấn), MF06 (`InventoryBatch` — cách ly + truy xuất), MF07 (`InspectionSession` — CAPA từ BR-095), MF08 (`FoodSample`/`SampleLabRequest`), MF09 (`ServingDish` — *"em nào đã ăn món nghi nhiễm?"*) |
| **Downstream Consumers** | MF10 (cảnh báo BR-148, trạng thái CAPA), MF13 (`GET /violations/summary` cấp số liệu cho **UC-189/BR-214**), MF06 (lệnh cách ly), MF08 (thu hồi mẫu theo `incidentId`) |
| **Package Java** | `com.sfms.mf11.{domain, repository, dto, mapper, service, web, port, event, job}` |
| **Test module** | `src/test/java/com/sfms/mf11/**` |
| **Migration** | *(sửa v1.1 — `M11-D05`)* **`V20__mf11_incident.sql`** (**13** bảng — v1.0 ghi "11", đếm máy ra 13) + **`V21__seed_mf11.sql`**. Ô cũ ghi *"nằm trong `V1__init_schema.sql` (Nam giữ)"* — **sai**: mô hình "V1 = toàn schema" đã bỏ từ khi MF02 dùng `V3`. ⚠️ **KHÔNG** `CREATE TABLE code_sequences` (`M11-D01` — đã tồn tại `V1:31`) |

**Đặc thù rủi ro của module — quyết định mức độ nghiêm ngặt của test:**

> MF11 có **ba lớp hậu quả** khác nhau, và test phải phân biệt được:
> 1. **Hành động không thể hoàn tác.** Cách ly lô là **một chiều** (BR-217): Caterers trả lô về NCC ngay, không chờ kết quả lab. Một lần tick nhầm = một lô nguyên liệu bị trả về nhà cung cấp vĩnh viễn, kèm hệ quả **hợp đồng**. Vì vậy `reason` bắt buộc phải enforce **ở server**, không phải ở dialog FE.
> 2. **Giá trị pháp lý của hồ sơ.** `incident_events`, `violation_records`, ảnh biên bản đã ký là **bằng chứng giữ 10 năm** (§4.2 TDS) và có thể là căn cứ chế tài hợp đồng với nhà thầu. Nếu sổ sự kiện UPDATE/DELETE được, hoặc `reportExported` mở khoá được, thì **toàn bộ hồ sơ mất giá trị chứng minh** — không sửa lại được sau sự cố.
> 3. **Dữ liệu sức khoẻ trẻ em.** Đây là module duy nhất chứa triệu chứng + chẩn đoán lâm sàng + kết quả xét nghiệm của học sinh. Rò rỉ sang Service Caterers (bên **bị điều tra**) là vi phạm PDPD, và về nghiệp vụ là để bên bị điều tra đọc hồ sơ nạn nhân.
>
> Vì vậy coverage mục tiêu: **≥ 90% line** cho `com.sfms.mf11.service`; **100% branch** cho `QuarantineService.quarantineBatch()`, `IncidentClosureService.close()` (3 điều kiện gate), `CapaService` (6 chuyển trạng thái BR-216), `ViolationResolutionService.submit()` (gate MSG-095 + phân nhánh BR-186), và **projection cắt theo vai** (`detailForCaterers()`, nhánh Teacher của `detail()`) — hai nhánh projection này lẫn nhau là **rò dữ liệu sức khoẻ**.

### 1.1 AI Generation Context (CASE 2.0)

| Field | Value |
|-------|-------|
| **AI Assisted?** | `Yes` — soạn thảo có AI hỗ trợ, oracle do người xác nhận (Minh) |
| **Constraint Source** | `TDS-MF11_Backend.md` §17.1 (**C1–C15**), §10 (error code), §16 (auth matrix 7 role × 30 nhóm endpoint), §13 (34 unit + 18 integration + 12 E2E gốc); **`00_Common_Conventions.md` D1–D10** |
| **Constraints Injected** | C1 hợp đồng phân trang + `SEARCH_FIELDS` cố định · C2 **BR-146 chỉ cảnh báo, không tự hành động** · C3 chuỗi `RPT → REC → INC` · C4 `incident_events` **append-only** · C5 cách ly **một chiều**, lý do bắt buộc · C6 CAPA 6 trạng thái, **1 Incident = 1 CAPA** · C7 gate đóng sự cố 3 điều kiện · C8 gate ảnh ký BR-211 · C9 vi phạm **cấp đơn vị** · C10 `reportExported` khoá một chiều · C11 tham chiếu module khác **bằng ID qua port** · C12 định danh & dẫn xuất *(phần định dạng ngày đã bị **D4** ghi đè — xem L4)* · C13 cảnh báo HIGH **bất đồng bộ** · C14 **PDPD** cắt dữ liệu sức khoẻ ở projection · C15 phạm vi đọc **dẫn xuất**, không thêm cột chủ sở hữu |
| **Model** | Claude Opus (`00_Team_Work_Assignment_Plan.md` §2c-4 — TestSpec = Opus vì oracle phải khớp BR/MSG) |
| **Trust Level** | `T2 → T3` (pending Red Gate §5.4) |
| **Oracle policy** | Mọi `Expected Result` **phải** có dòng `Oracle Source` trỏ BR / bước UC / MSG nguyên văn / luật `semantic-audit.ts` / **D1–D10**. TC không có oracle → reject (**AP-AI-001**). Kịch bản nào TDS §13 nêu mà **không** có BR/MSG chống lưng thì ghi rõ **"thiếu oracle"** và báo lại, **không** tự bịa expected — xem **L7** (10 mã cục bộ MF11 chưa có MSG). |

---

## 2. Logic Issues Resolved

> **Bắt buộc điền trước khi viết test.** Test cases encode hành vi **đã sửa**, không phải hành vi trong spec gốc.
> **L1–L10** = `00_Common_Conventions.md` **ghi đè / bổ sung** ADR + constraint trong TDS-MF11 — loại quan trọng nhất vì TDS viết 28/07, trước khi D4/D9/D10 được duyệt.
> **L11–L31** = kế thừa **CF-M11-1…21** của TDS §2.3 — **không tự chọn bên đúng**, chỉ ghi rõ hành vi nào được test.
> **L32–L41** = lệch **mới phát hiện** khi soạn TestSpec này (đối chiếu TDS ↔ `mock-data.ts` ↔ `error_message.md` v1.8 ↔ `traceability_matrix.md`).

### 2.1. Nhóm A — `00_Common_Conventions.md` ghi đè TDS (ưu tiên xử lý trước)

| # | Quy ước | TDS-MF11 hiện viết | Hành vi được TEST | TC liên quan |
|---|---|---|---|---|
| **L1** | **D1** — JSON `id` + path param = **mã nghiệp vụ** (`code`), UUID là PK nội bộ | ✅ **TDS đúng** — ADR-M11-5 chính là **nguồn** của D1; §5.2 mọi bảng đã có `code VARCHAR(24)` + `UNIQUE(school_id, code)` | Mọi DB assertion dùng `WHERE code = ?`; path `POST /api/v1/incidents/INC-2026-014/assess`; khoá ngoại JSON (`incidentId`, `batchCode`, `caterersRepresentativeId`) là **code/ID nghiệp vụ**, FK trong DB vẫn UUID. Response **không** lộ UUID ở bất kỳ field nào | `TC-INT-010`, và mọi `TC-INT-*` |
| **L2** | **D2** — envelope `{"error":{"code","message","details"}}`, `details` là **mảng, không bao giờ `null`** | §9.2/§10 dùng envelope (đúng), nhưng ví dụ 403/409 có chỗ **bỏ hẳn** `details` | Assert `$.error.code`, `$.error.message`, và `$.error.details` **là array** (rỗng thì `[]`) ở **mọi** error path | `TC-INT-012` + toàn bộ TC error path |
| **L3** | **D3** — `baseURL = /api/v1` | §9 đã ghi *"path thật thêm tiền tố `/api/v1`"* ✅ | Mọi MockMvc request dùng `/api/v1/...`; contract test assert `servers[0].url` hoặc path prefix | `TC-CON-001` |
| **L4** ⚠️ | **D4** — JSON ngày **luôn ISO-8601**; **bỏ** mọi `@JsonFormat("dd/MM/yyyy")` | **Xung đột trực diện, D4 nêu đích danh MF11:** ADR-M11-5 chốt *"Ngày/giờ giữ định dạng `dd/MM/yyyy [HH:mm]` ở JSON qua `@JsonFormat`"*; **C12** (§17.1) nhắc lại; **§17.2 mục 12** của Constraint Injection Block còn ghi *"JSON giữ ngày dd/MM/yyyy qua @JsonFormat"* — block này được **inject thẳng vào AI prompt sinh code**; toàn bộ ví dụ §9.2 dùng `"reportedAt": "28/07/2026 15:40"`, `"date": "28/07/2026"` | **Assert ISO**: `"reportedAt": "2026-07-28T08:40:00Z"` (`Instant`), `"mealDate": "2026-06-30"` (`LocalDate`), `"date"`, `"occurredAt"`, `"quarantinedAt"`, `"closedAt"`, `"at"` (sự kiện) tương tự. Backend **parse khoan dung cả 2 dạng khi NHẬN** (mock đang gửi `dd/MM/yyyy`) nhưng **TRẢ VỀ ISO**. ⚠️ **C12 và §17.2 mục 12 phải được sửa** — như đang viết thì prompt sinh code sẽ tái tạo đúng thứ D4 vừa dẹp | `TC-INT-011`, `TC-CON-003`, `TC-E2E-006` |
| **L5** | **D5** — client gửi `body.id` ở `POST` → dùng làm `code`, trùng → **409 MSG-139** | TDS **không nhắc D5** | MF11 **không có endpoint tạo mới nhận `id` từ client** (mọi mã `RPT`/`REC`/`INC`/`CAPA`/`VIO` sinh qua `code_sequences` — ADR-M11-5). ⇒ Test assert **không `requestBody` nào của MF11 khai field `id`**; nếu về sau có, phải kèm 409 + **MSG-139** *"A record with this value already exists."* | `TC-CON-005` |
| **L6** | **D6** — 8 enum dùng chung ở `common/enums/`, SCREAMING_SNAKE | MF11 khai **9 enum riêng** (`IncidentStatus`, `IncidentSeverity`, `CapaStatus`, `ViolationSeverity`, `ResolutionStatus`, `ViolationStatus`, `ViolationType`, `UnitSanctionLevel`, `IncidentEventType`) — **không** trùng 8 enum của D6 ⇒ **hợp lệ**, không phải vi phạm | Assert mọi giá trị enum trả về là **SCREAMING_SNAKE**; `RoleKey` dùng **kebab-case** (`medical-staff`, `school-admin`, `service-caterers`) theo D6. ⚠️ Ca đặc biệt: `"severity": "PENDING"` là **giá trị hiển thị** khi cột `severity IS NULL` (CF-M11-12/L22) — assert JSON = `"PENDING"` **và** DB = `NULL` cùng lúc | `TC-008`, `TC-INT-012`, `TC-CON-004` |
| **L7** ✅ **ĐÓNG 04/08** *(`error_message.md` **v1.11** cấp MSG-149…158 — assert `message` nay BẬT đầy đủ cho cả 10 mã; nội dung gốc giữ theo Policy 4.4)* | **D7** — `message` **tiếng Anh** nguyên văn `error_message.md`; mã dẫn xuất BR phải được cấp MSG chính thức | §9.2/§10 trả message **tiếng Việt** cho **10 mã cục bộ**: `IEM-114`, `IEM-147`, `IEM-149`, `IEM-149B`, `IEM-155`, `IEM-216`, `IEM-217`, `IEM-211`, `IEM-186`, `IEM-RO`. ⚠️ **Khác MF08/MF09:** `error_message.md` **v1.5 đã cấp MSG cho mã cục bộ của MF08 (MSG-119…128) và MF09 (MSG-129…133) nhưng KHÔNG cấp cho MF11** — rà toàn bộ v1.8, 10 mã trên **vẫn chưa có MSG nào** | **Thiếu oracle cho `message`.** Xử lý theo policy: (a) assert **`$.error.code`** + **HTTP status** (2 thứ này **có** oracle: §10 TDS + BR); (b) assertion **nội dung `message`** để `@Disabled("Thiếu oracle — 10 mã cục bộ MF11 chưa có MSG, chờ Huy cấp")`; (c) **KHÔNG** tự dịch tiếng Việt sang tiếng Anh rồi coi là chuẩn. Với các mã **có** MSG (`IEM-005`/MSG-005, `IEM-023`/MSG-023, `IEM-085`/MSG-085, `IEM-095`/MSG-095, `IEM-404`/**MSG-137**, `IEM-001`/**MSG-138**) → assert **nguyên văn tiếng Anh**, bật bình thường | `TC-002`, `TC-022`, `TC-024`, `TC-INT-012` |
| **L8** | **D8** *(⏳ chờ Nam approve — vẫn làm vì không xung đột D1–D7)* — annotation springdoc S1–S8 | §9.2 **không có** annotation nào; §10 **có** cột HTTP ✅ | Nhóm **Contract test** §4.4 đọc `/v3/api-docs` assert S1–S8. Nếu endpoint MF11 chưa annotate → test **FAIL** và ghi rõ *"thiếu annotation D8"*, **không** nới assert | `TC-CON-001…005` |
| **L9** | **D9** — `created_by`/`updated_by` là **tên cột reserved** của `BaseEntity` | ✅ **TDS đúng** — D9 nêu tên MF11 trong nhóm *"không cần sửa"*: §5.2 dùng `created_by_id`, `recorded_by_id`, `quarantined_by_id`, `handled_by_id`, `closed_by_id` (field nghiệp vụ, tên riêng) | Assert: (a) **13** entity MF11 `extends BaseEntity` và **không tự khai lại** `createdBy`/`updatedBy`; (b) `created_by` được `AuditorAware` tự điền = email actor, service **không** set tay; (c) 5 cột nghiệp vụ trên giữ nguyên | `TC-INT-013` |
| **L10** | **D10** — upload file/ảnh **phải** qua `common/storage/StorageService` | TDS §9 mô tả `evidenceFiles`, `signedRecordFiles`, `violation_files.storage_key` nhưng **không nhắc `StorageService`** — D10 nêu đích danh MF11 (*"ảnh hiện trường sự cố"*) | Assert 3 luồng upload (`POST /incidents/{code}/evidence`, `/corrective-actions/{code}/submit-evidence`, `/violations/{code}/signed-record`) gọi `StorageService.store()`, key theo khuôn `"incident/<mã>.<ext>"` / `"violation/<mã>-signed.<ext>"`; service **không** tự gọi `MultipartFile.transferTo()`/AWS SDK. Giới hạn 10MB + PDF/JPG/PNG → **MSG-008/MSG-009** (BR-013) | `TC-SEC-013` |

> **Ghi chú bắt buộc cho Mạnh & Nam:** **L4** và **L7** nghĩa là **TDS-MF11 §9.2/§10/§17.1-C12/§17.2-mục-12 cần một bản cập nhật**. Đặc biệt **§17.2 là Constraint Injection Block** — nó được copy-paste thẳng vào prompt sinh code, nên để nguyên là **mọi controller sinh ra sẽ có `@JsonFormat("dd/MM/yyyy")`**, đúng thứ D4 đã Approved cấm. TestSpec này viết theo **D1–D10** (nguồn ưu tiên cao hơn). **Không** sửa TestSpec để khớp TDS cũ.

### 2.2. Nhóm B — mâu thuẫn nguồn kế thừa từ TDS §2.3 (CF-M11-1…21)

| # | Nguồn CF | Spec gốc (sai / thiếu / lệch) | Fix áp dụng trong test |
|---|---|---|---|
| **L11** ✅ **ĐÓNG 04/08** *(ma trận v1.4 §1.11 đã bổ sung **BR-216** vào UC-005/038/082/168 — oracle nay trỏ thẳng §1.11)* | CF-M11-1 | **BR-216** (vòng đời CAPA 6 trạng thái, 1 Incident = 1 CAPA) là quy tắc trung tâm của UC-005/038/082/168 nhưng **không có** ở cột BR của §1.11 (chỉ BR-152/153/155) | **Vẫn test đầy đủ BR-216**, ghi oracle là *"`business_rules.md` §11 BR-216 — chưa có ở §1.11"*. Không có nó thì vòng đời CAPA không có định nghĩa. Chờ **Huy** bổ sung §1.11 |
| **L12** ✅ *(một phần — ma trận v1.4 bổ sung **BR-217** vào UC-041/013; phần "mock còn field `quarantineReleaseLog`" là việc sửa FE của Minh)* | CF-M11-2 | **BR-217** (cách ly → trả lô về NCC → **bỏ mở khoá**) cùng tình trạng; mock vẫn còn field `quarantineReleaseLog` (thêm 19/07, trước khi BR-217 bỏ mở khoá 27/07) | Test theo **BR-217 bản mới**: `quarantineBatch()` **một chiều**; `TC-011`/`TC-SEC-015` assert **không tồn tại** endpoint/field/method `release`/`unquarantine`/`releasedAt`. Bảng `incident_quarantine_release_log` chỉ **đọc** dữ liệu lịch sử — `TC-INT-002` assert **0 dòng ghi mới** sau khi chạy toàn bộ suite |
| **L13** ✅ **ĐÓNG 04/08** *(ma trận v1.4: **BR-188** thêm vào UC-179/180/182, **BR-184** đánh dấu Deprecated, **UC-179 đổi tên** → "Record **Unit** Violation")* | CF-M11-3 | Kế hoạch §2 ghi MF11 gộp *"BR-184, 186, 188"*: **BR-184 đã Deprecated 26/07** (vẫn còn ở §1.11 dưới UC-179); **BR-188 không có** ở §1.11 | **Không** TC nào hiện thực BR-184 như một loại biên bản riêng (`TC-023` assert DTO **không** có `staffName`/`staffPosition`/`disciplinaryLevel`). **Vẫn** test audit bất biến theo BR-188 (`TC-SEC-012`) với oracle ghi rõ *"BR-188 — §18, không có ở §1.11"*. Chờ **Huy + Nam** |
| **L14** | CF-M11-4 | **UC-179 vẫn tên "Record Staff Violation"**, UC file mô tả `staffName`/`staffPosition`/`disciplinaryLevel`; MSG-088 vẫn ghi *"staff name matches"* | Test theo **BR-183/185/187 bản 26/07** (cấp đơn vị): `TC-023` có **2 nhánh** — cùng `caterersRepresentativeId` ⇒ `repeatSuspect=true`; cùng `staffName` nhưng khác đại diện ⇒ `repeatSuspect=false`. **MSG-088 giữ nguyên văn** (không sửa MSG), chỉ ghi lệch |
| **L15** | CF-M11-5 | **UC-073/074 (UC file) vẫn ghi** *"automatically creates a food safety incident and quarantines suspected batches"*; **BR-146/BR-149 bản 20/07 đã bãi bỏ** cơ chế tự động | Test theo **BR** (nguồn chuẩn theo §2c): `TC-001` **CRITICAL** — `POST /source-reports` chỉ tạo `RPT`, `verify(incidentRepository, never()).save(...)`, `verify(inventoryBatchPort, never()).markQuarantined(...)`, `traceabilityMapJob` **không** được kích hoạt. UC-073/074 cần **Huy** viết lại |
| **L16** ✅ **ĐÓNG 04/08** *(ma trận v1.4 gỡ MSG-029 khỏi UC-005/009/013, thay bằng **MSG-159/160/161**; TC nay assert cả `message`, không chỉ `code`)* | CF-M11-6 | **MSG gán sai ngữ nghĩa ở §1.11:** UC-005 (duyệt CAPA), UC-009 (phân loại), UC-013 (đóng sự cố) đều → **MSG-029** = *"Profile updated successfully."*; mock lại dùng MSG-043/MSG-046 của module khác | Response trả **đúng mã §1.11** (MSG-029) để không bịa mã, nhưng test **chỉ assert `code`**, và ghi 1 dòng *"nội dung MSG-029 không mô tả đúng hành động — chờ Huy cấp MSG mới"*. `TC-CON-002` assert **không** endpoint MF11 nào trả MSG-043/MSG-046 |
| **L17** | CF-M11-7 | **MSG-024** (*"Kitchen quarantine is active…"*, BR-105) được §1.11 gán cho UC-041, nhưng khoá bếp **đã descope 05/07/2026** | **Không endpoint MF11 nào phát MSG-024** — `TC-CON-005` assert chuỗi `IEM-024`/`MSG-024` **không xuất hiện** trong `/v3/api-docs` của MF11. Giữ ở §10 để lưu vết §1.11 |
| **L18** | CF-M11-8 | `SEARCH_FIELDS` gợi ý ở Team Plan §2b-3 cho MF11 = `title`, `description`, `reporterName` — **`description` và `reporterName` không tồn tại** trên `Incident` | `SEARCH_FIELDS` test theo TDS §9.1: `incidents` → `title`, `reportedByLabel`, `suspectedMealText` · `source_reports` → `reportedByLabel`, `className`, `suspectedMealText`, `description` · `violations` → `subject`, `recordedByLabel`, `caterersRepresentativeLabel` · `corrective_actions` → `description`, `assigneeLabel`, `sourceLabel` · `incident_records` → `code`, `createdByLabel`, `location`. `TC-027`/`TC-INT-020` assert search **không** khớp cột ngoài danh sách. Đề nghị **Nam** sửa §2b-3 |
| **L19** | CF-M11-9 | Mock dùng **generic `PUT /:resource/:id`** cho **mọi** chuyển trạng thái ⇒ không gate nào chạy phía server (BR-153/155/186/211/149 đang chặn ở client bằng `toast.error`/`disabled`) | Test **endpoint nghiệp vụ**. Nhóm **"gọi API trực tiếp bỏ qua UI"** là bắt buộc theo prompt §2c-3b: `TC-SEC-002` (Caterers gọi `/assess`), `TC-SEC-003` (Caterers tự duyệt CAPA), `TC-SEC-004` (`PUT` generic đặt `status=COMPLETED` → **404/405, endpoint không tồn tại**), `TC-SEC-005` (Medical Staff đóng sự cố) |
| **L20** | CF-M11-10 | `override-abuse` là **bảng seed tĩnh** (`OVERRIDE_ABUSE_REPORT`, 3 dòng, khoá theo `staffName`) trong khi **BR-190** nói *tự động tổng hợp hằng tuần* từ `violation_records` cấp đơn vị | **Không tạo bảng** `override_abuse`: `TC-INT-021` assert `information_schema.tables` **không** có bảng đó; `TC-026` assert báo cáo **dẫn xuất runtime** `GROUP BY` tuần ISO × `caterers_representative_id` |
| **L21** | CF-M11-11 | Luật audit **`DONG_SU_CO_MA_CON_LO_KHOA`** coi *"sự cố đã đóng mà `quarantineBatches` còn phần tử"* là lỗi — nhưng **BR-217 bỏ hẳn nhả lô**, nên danh sách **luôn còn** phần tử khi đóng | Luật này **KHÔNG port** (ghi rõ ở §9.4). Thay bằng bất biến mới: `Incident.CLOSED` ⇒ mọi lô ở trạng thái cuối bên MF06 (`RETURNED_TO_SUPPLIER`/`DISPOSED`) → **409 `IEM-217`** (`TC-INT-017`). Cần **Thảo** cập nhật script audit |
| **L22** | CF-M11-12 | Mock khởi tạo `severity = "PENDING"` (sentinel dùng chung kiểu với `status`), BR-147 chỉ có 3 mức | 2 enum tách bạch: DB `severity` **nullable** (NULL = chưa phân loại), JSON **vẫn trả `"PENDING"`**. `TC-008` assert giá trị ngoài `{LOW,MEDIUM,HIGH}` → **400 `IEM-147`**; `TC-INT-012` assert cặp (JSON `"PENDING"` ↔ DB `NULL`) |
| **L23** ✅ **ĐÓNG 04/08** *(`M11-D20`: `scope = quarantineBatches` chốt chính thức; nhánh "toàn sự cố" **loại bỏ**, không giữ `@Disabled`)* | CF-M11-13 | **BR-151** phạm vi *"liên quan"* **chưa chốt** (footnote 15/07); mock đặt `traceabilityMapStatus` thủ công, **không** có job 4h nào | Test theo đề xuất footnote: `scope = "quarantineBatches"` (`TC-INT-007`), SLA `COMPILING → READY ≤ 4h` bằng **clock giả**. Đánh dấu **quyết định tạm** — nhánh "scope toàn sự cố" để `@Disabled("Chờ đội dự án chốt footnote BR-151")` |
| **L24** | CF-M11-14 | **UC-082 EX1** đòi gate *"Health Department Clearance Certificate"* dựa trên cơ chế **đóng cửa bếp đã descope** | **Không** TC nào cho gate đó. Giữ validate **ảnh bằng chứng bắt buộc** khi `submitEvidence()` (`ck_capa_evidence`: `evidence_photos > 0`) — `TC-019`, `TC-INT-008` |
| **L25** | CF-M11-15 | **UC-168 EX1** yêu cầu hiện **MSG-004** (*"Session expired"*) khi còn HS chưa hồi phục — sai ngữ nghĩa, sẽ đá người dùng ra màn đăng nhập giữa lúc xử lý sự cố | `TC-018` **CRITICAL**: assert `code = "IEM-155"`, HTTP **409**, và assert **KHÔNG** trả MSG-004/HTTP 401. Đây là nhóm lỗi nguy hiểm nhất về UX vận hành khẩn cấp |
| **L26** | CF-M11-16 | `incident-events` (sổ append-only) **không** có BR/UC cấp phép — là quyết định kiến trúc ADR-M11-2 | Vẫn test đầy đủ (`TC-INT-001` RULE append-only), oracle ghi **ADR-M11-2**, **không** nhận là BR |
| **L27** | CF-M11-17 | §16 bản v1.0 **chặn nhầm Teacher** khỏi `GET /source-reports` và `GET /incidents` — 2 màn Teacher đang gọi cả hai | `TC-SEC-008` là **regression guard** (`AP-M11-15`): Teacher **phải** đọc được `RPT` lớp mình + `INC` có ≥1 HS lớp mình; `TC-SEC-007` assert projection Teacher **cắt** `clinicalRiskAssessment`/`investigationLabResult`/`rootCause` và `affectedStudents[]` **chỉ giữ HS lớp mình** |
| **L28** ✅ **ĐÓNG 04/08** *(`M11-D19`: Nam chốt **GIỮ cả 2 vế `OR`** — `TC-SEC-014` bỏ `@Disabled`, test cả hai nhánh)* | CF-M11-18 | **Phạm vi `Liên đới` của Service Caterers chưa chốt** — `Incident` không có cột nào trỏ nhà thầu; ADR-M11-10 đề xuất 2 vế `OR` nhưng **chờ Nam** | `TC-SEC-014` test **hành vi khoan dung** (sự cố có CAPA giao đơn vị mình ⇒ đọc được — vế chắc chắn đúng ở cả 2 phương án). Nhánh **nghiêm ngặt** (vế "lô cách ly thuộc NCC mình") để `@Disabled("Chờ Nam chốt CF-M11-18")`. **Không** khoá test vào một bên |
| **L29** | CF-M11-19 | FE đang **`POST /incident-events` từ client** (5 chỗ) và **`useUpdate("inventory")` ghi thẳng bảng kho MF06** | Backend **không mở** 2 đường đó: `TC-INT-019` assert `POST /api/v1/incident-events` → **404/405** và `GET /api/v1/incident-events` **không tồn tại** (canonical là `GET /incidents/{code}/events`); `TC-INT-006` assert cách ly đi **qua `InventoryBatchPort`**, không UPDATE trực tiếp bảng MF06. Breaking change FE — **Minh** chịu trách nhiệm (F1/F2 §11.2b) |
| **L30** | CF-M11-20 | Màn `medical-staff/violations/reports` nằm trong cây MF11 nhưng thực hiện **UC-189/BR-214** (thuộc MF13); **UC-188/BR-213 đã Deprecated** | MF11 **không sở hữu** `sent-reports`: `TC-CON-005` assert spec MF11 **không** khai path `/sent-reports`. Chỉ test `GET /violations/summary` (cấp số liệu). **Không** TC nào cho UC-188/BR-213 |
| **L31** | CF-M11-21 | 5 chỗ mock FE lạc hậu: (a) *"lô đã **tự động** chuyển Quarantine"*, (b) *"hoàn thành CAPA trong **12 giờ**"*, (c) *"**Nhân viên** vượt quá 3 lần"*, (d) cột **"Hạn" = `capa.deadline`**, (e) ô **"Sơ cứu đã thực hiện" hiện cho `service-caterers`** | Backend theo BR bản mới. (d): cột `deadline` giữ **nullable, deprecated, read-only** — `TC-INT-008` assert nó **luôn `null`** với CAPA tạo mới. (e): `TC-SEC-006` assert `firstAidNotes` **bị cắt** khỏi projection Caterers ⇒ ô đó trắng — **có chủ ý**, không phải lỗi |

### 2.3. Nhóm C — lệch mới phát hiện khi soạn TestSpec (đối chiếu TDS ↔ seed ↔ `error_message.md` v1.8 ↔ `traceability_matrix.md`)

| # | Phát hiện | Bằng chứng | Fix áp dụng trong test |
|---|---|---|---|
| **L32** ⚠️ | **Mã đối tác trong ví dụ §9.2 KHÔNG TỒN TẠI.** TDS dùng `assigneeSupplierId: "PTN-0007"` (CAPA) và `supplierId: "PTN-0003"` + `supplierName: "HTX Rau sạch Green Farm"` (bản đồ truy xuất) | `mock-data.ts` `PARTNERS` chỉ có **`SUP-001…SUP-005`** và **`CAT-001`**. Không có tiền tố `PTN-` ở bất kỳ đâu. Đơn vị suất ăn thật = **`CAT-001`** (Cty TNHH Suất ăn An Bình); NCC của `BATCH-MP-8820` (thịt gà) = **`SUP-003`** (Cty TNHH Thịt sạch Minh Phát, qua phiếu `RCV-20260629-02`) — **không phải** một HTX rau | Fixture dùng **`CAT-001`** cho `assigneeSupplierId` và **`SUP-003`** cho nút truy xuất. Nếu để nguyên mã sai, luật audit **`CAPA_GIAO_NCC_KHONG_TON_TAI`** sẽ bắn ngay ở dòng seed đầu tiên, và bản đồ truy xuất trỏ **sai nhà cung cấp** — tức là quy trách nhiệm hợp đồng cho bên không liên quan. Đề nghị **Mạnh** sửa 2 ví dụ §9.2 |
| **L33** ✅ **ĐÓNG 04/08** | ~~**`dishId` không tồn tại trong mô hình hiện tại.**~~ **Bằng chứng ngược từ CODE THẬT (`M11-D12`):** `V8__mf05_menu_diet.sql:35` khai `dishes.code VARCHAR(64) NOT NULL UNIQUE` (slug `ca-basa-sot-ca-chua`) và `common/reference/ReferenceResolver.dishIdByCode()/dishCode()/dishName()` **đang chạy**. L33 viết 01/08 khi chỉ đọc `mock-data.ts` — đúng lớp lỗi mà nguồn `-1 = CODE THẬT` của §2c được thêm để chặn. `POST /assess` nhận `suspectedDishIds[]` = **`dishes.code`**, không tra được → **400 `IEM-001`**. **Không còn điểm chặn nào cho MF05/Mạnh.** *(Nội dung gốc giữ nguyên bên dưới theo Policy 4.4.)* — **`dishId` không tồn tại trong mô hình hiện tại.** TDS §5.2 khai `incident_suspected_dishes.dish_id UUID NOT NULL` và §9.2 dùng `"suspectedDishIds": ["dish-ga-rang-gung"]` | Trong `mock-data.ts`, món được dựng bằng `dish(name, kcal, ingredients)` — **không có field `id`**; `Dish` chỉ có `name`/`category` (đúng như `SEARCH_FIELDS` của MF05). Seed `INC-2026-014` lưu `suspectedDishIds: ["Gà rang gừng"]` — **là TÊN, không phải khoá**. Chuỗi `INSPECTIONS.dish`, `FOOD_SAMPLES.dish` cũng nối bằng tên | Test khoá vào **hợp đồng**: `POST /assess` nhận `suspectedDishIds[]` là **mã món của MF05** và service phải resolve qua `DishQueryPort.byCode()`; mã không tra được → **400 `IEM-001`**. `TC-INT-022` assert `studentsWhoAte` join **bằng ID**, không bằng tên. ⚠️ **Chặn cứng:** MF05 phải cấp `Dish.code` trước — nếu chưa có, seed MF11 sẽ nối bằng tên và trục truy xuất **đứt mà không báo lỗi** (đúng loại lỗi `semantic-audit.ts` §9 sinh ra để bắt). Cần **Thảo (MF05)** + **Mạnh** chốt |
| **L34** | **`caterersRepresentativeId: "STF-007"` không tồn tại.** §9.2 `POST /violations` dùng mã này | `USERS` seed: đại diện nhà thầu thật là **`U-0003`** (Trần Văn Hùng, role `Service Caterers`, thuộc `CAT-001`); `VIOLATIONS` seed chỉ lưu **nhãn** `caterersRepresentative: "Trần Văn Hùng (Cty TNHH Suất ăn An Bình)"`, không lưu ID | Fixture dùng **`U-0003`**. `TC-023` (BR-187) đối chiếu theo `caterersRepresentativeId = U-0003`. Đề nghị **Mạnh** sửa ví dụ §9.2 |
| **L35** ✅ **ĐÓNG 04/08 chiều** — **`error_message.md` v1.11 đã cấp MSG-149…158** (+ MSG-159/160/161 cho L16). Diễn biến trong ngày: sáng kiểm lại trên **v1.9** thì `IEM-` **vẫn ra 0** và MF11 là module cuối cùng còn thiếu; TDS §10.1 soạn sẵn bản tiếng Anh đúng khuôn v1.5; chiều Huy ký nguyên bản. ⇒ **assert `message` bật đầy đủ**, hết `@Disabled` vì thiếu oracle. *(Nội dung gốc giữ theo Policy 4.4.)* — **Kiểm lại trên `error_message.md` v1.9: `IEM-` vẫn ra 0.** Cùng đợt đã cấp thêm **MSG-145…148 cho MF07** ⇒ MF11 là module duy nhất còn thiếu. `TDS-MF11` §10.1 nay kèm **bản soạn sẵn MSG-149…MSG-158** (tiếng Anh, khuôn v1.5, placeholder `[…]`, số động đặt trong `error.details`) — Huy chỉ cần **ký hoặc sửa chữ**. `IEM-001`/`IEM-404`/`IEM-429` **không xin mới**, dùng chung **MSG-138/137/141**. Phần `@Disabled` vì thế thu hẹp từ *"10 nhóm assert message"* xuống *"chờ 1 chữ ký"*. — nội dung gốc: **10 mã lỗi cục bộ của MF11 vẫn CHƯA được cấp MSG** ở `error_message.md` v1.8, trong khi cùng đợt v1.5 đã cấp cho MF08 (MSG-119…128) và MF09 (MSG-129…133) | Grep v1.8: `IEM-` xuất hiện **0 lần**; bảng §1.1 (MSG-104…141) không có dòng nào cho `IEM-114/147/149/149B/155/216/217/211/186/RO`. Ghi chú §10 TDS (*"chờ Huy quyết định"*) **vẫn đúng**, khác MF09 (ghi chú đã lỗi thời) | Xem **L7**: assert `code` + HTTP, `@Disabled` phần assert `message`. Đây là **việc chặn Exit Criteria** — ghi vào §6 và §9.7. Đề nghị **Huy** cấp 10 MSG theo khuôn v1.5 (dịch sang tiếng Anh theo D7) |
| **L36** ⚠️ | **Seed `INC-2026-013` tự mâu thuẫn — không nạp được vào schema mới.** Sự cố này có `status: "CAPA_APPROVED"` nhưng sổ sự kiện lại có `IEV-013-05` `type: "CLOSED"` (*"Đóng sự cố sau khi rà soát hồ sơ (BR-155)"*, 28/06 10:00), trong khi CAPA của nó (`CAPA-2026-030`) đang ở `SUBMITTED_EVIDENCE` | 3 nguồn nói 3 chuyện: cột `status` (chưa đóng) ↔ sổ sự kiện (đã đóng) ↔ ràng buộc **C1** `data_model.puml` (*"chỉ đóng khi mọi CAPA COMPLETED"*). Ngoài ra `ck_inc_closed` đòi `closed_at`/`closed_by_id`/`closing_statement` — seed **không có** field nào trong 3 | Bước sinh Flyway seed phải **chọn một**: hoặc bỏ `IEV-013-05`, hoặc đẩy `CAPA-2026-030 → COMPLETED` + điền 3 cột đóng. `TC-INT-023` assert **sau khi seed**, không dòng nào vi phạm `ck_inc_closed` và không `INC` nào có event `CLOSED` mà `status <> 'CLOSED'`. **Không tự chọn** — ghi để **Mạnh** quyết khi viết `V{n}__seed_mf11_incident.sql` |
| **L37** ⚠️ | **4 dòng `VIOLATIONS` seed vi phạm `ck_vio_resolution` và/hoặc gate BR-211.** `VIO-2026-006` (LOW, `resolution=APPROVED`, `status=CLOSED`) — **không có** `unitSanctionLevel`, **không có** `signedRecordPhoto`, **không có** `locationOfIncident`/`occurredAt`/`caterersRepresentative`; `VIO-2026-004`, `VIO-2026-005` — `APPROVED` nhưng **0 ảnh ký**; `VIO-2026-009` — `PENDING_APPROVAL` nhưng **0 ảnh ký** và `pdfExported` vắng | §5.2 `ck_vio_resolution`: `resolution <> 'PENDING'` ⇒ **bắt buộc** `action_taken` + `handled_by_id` + `unit_sanction_level`. §4.2 bất biến: `resolution IN ('APPROVED','PENDING_APPROVAL')` ⇒ **≥ 1** `signed_record_file` (BR-211). 4 dòng trên **INSERT sẽ fail** hoặc lọt qua rồi phá bất biến | Seed phải **bổ sung** `unit_sanction_level` + `signed_record_file` cho 4 dòng, **hoặc** hạ `resolution` về `PENDING`. `TC-INT-023` assert 2 bất biến trên đúng **0 vi phạm** sau seed. `TC-024`/`TC-028` dùng fixture **đã sửa**, không dùng seed thô |
| **L38** ✅ **ĐÓNG 04/08** *(`traceability_matrix.md` v1.4 sửa ghi chú 4 dưới §1.11: 48h → **24h**)* | **Traceability §1.11 ghi cửa sổ món nghi vấn là 48 giờ; BR-149 bản 27/07 là 24 giờ.** Ghi chú 4 dưới bảng §1.11: *"Chọn 'món ăn nghi vấn' (BR-149) nay mặc định lọc theo món chế biến trong **48 giờ**"* | `business_rules.md` BR-149 (sửa 27/07/2026): *"đổi cửa sổ 72 giờ → **24 giờ**, áp thống nhất cho CẢ danh sách món nghi vấn lẫn danh sách mẫu"*, đồng bộ BR-111 (hạn lưu mẫu 24h). Ghi chú §1.11 viết 15/07 — **lạc hậu 12 ngày** | Test theo **BR-149 bản mới nhất = 24h** (`TC-012`, boundary ±1 phút). Ghi 1 dòng để **Huy** cập nhật ghi chú §1.11 — nếu để nguyên, người đọc traceability sẽ hiện thực 48h và danh sách món **rộng gấp đôi** khoảng còn tồn mẫu đối chứng |
| **L39** | **`IEV-014-02` ghi actor = "Hệ thống" cho sự kiện `QUARANTINE`** (*"Cách ly lô nghi vấn BATCH-MP-8820 (BR-149)"*, 30/06 13:05) | BR-149 bản 20/07 **bỏ cách ly tự động** — cách ly là thao tác **thủ công của Medical Staff**. Seed còn giữ dấu vết cơ chế cũ | Seed chuyển `actor_label` → `"Ngô Thị Hà (Y tế)"`. `TC-001` assert: sau `POST /source-reports`, **không** sinh `incident_events` loại `QUARANTINE` nào; `TC-INT-006` assert event `QUARANTINE` luôn mang `quarantined_by_id` là **người thật**, không phải `"Hệ thống"` |
| **L40** | **MSG-048 chứa vế *"Investigation map generated"* nhưng §1.11 gán nó cho cả UC-073/074** (gửi `RPT`) — nơi **không** có bản đồ nào được sinh (BR-146/BR-151) | `error_message.md`: MSG-048 = *"Suspected food safety incident report logged. Investigation map generated."* Sau khi BR-146 bỏ tự động hoá, vế thứ hai chỉ đúng ở **`open-investigation`** | `TC-001` assert response `POST /source-reports` trả MSG-048 (đúng §1.11) **nhưng** `traceabilityMapStatus` **không xuất hiện** và `TraceabilityMapJob` **không** được gọi. Ghi 1 dòng đề nghị **Huy** tách MSG cho 2 ngữ cảnh |
| **L41** | **`GET /violations/summary` chưa có oracle cho ca "kỳ rỗng".** MSG-097 (*"No violations recorded for the selected period…"*) tồn tại nhưng thuộc **UC-189/BR-214 = MF13** (CF-M11-20) | `error_message.md` v1.3 cấp MSG-096/097 cho UC-188/189 — cả 2 nằm ở §1.13 | `TC-INT-020` assert `GET /violations/summary` kỳ rỗng trả **200** với các con số `0` (đúng hợp đồng §2b-6: tập rỗng **không** phải 404/lỗi); **không** trả MSG-097 — việc quyết định hiển thị thông điệp đó là của **MF13** khi dựng báo cáo |

**Cách xử lý mâu thuẫn trong test (policy):** nơi nào 2 nguồn nói khác nhau và **chưa có người chốt** → test **hành vi khoan dung** (chấp nhận cả hai) + để `@Disabled("Chờ CF-M11-x")` cho TC của hành vi nghiêm ngặt. **Không** viết test khoá một bên rồi coi là chuẩn.
**✅ Cập nhật 2026-08-04:** policy này **không còn điểm áp dụng nào trong MF11** — cả 7 mục treo đã có người chốt (`M11-D18`…`M11-D22`), **0 `@Disabled` còn lại**. Giữ nguyên đoạn trên làm quy tắc chung cho module sau.
**Ngoại lệ:** **L1–L10** đã có người chốt (`00_Common_Conventions.md`, Nam approve) ⇒ test **khoá theo D1–D10**, không khoan dung.
**Ngoại lệ 2:** **L36, L37** là **dữ liệu seed sai bất biến của chính TDS** — không phải mâu thuẫn giữa 2 nguồn ngang hàng. Test **khoá theo bất biến**, seed phải sửa.

---

## 3. Test Design Specification

### TDS-01 — Scope / Phạm vi

```
MF11 (com.sfms.mf11) — các layer được kiểm thử:
├── domain/       IncidentSourceReport (RPT) · IncidentRecordDraft (REC) · Incident (INC, aggregate root)
│                 + IncidentAffectedStudent · IncidentSuspectedDish · IncidentQuarantineBatch
│                 + InvestigationEvidence · IncidentEvent <<Append-Only>> (ADR-M11-2)
│                 + CorrectiveAction (BR-216) · ViolationRecord · ViolationFile
│                 + enum IncidentStatus{SEVERITY_PENDING,INVESTIGATING,CAPA_PENDING,CAPA_APPROVED,
│                   VALIDATED,CLOSED} · IncidentSeverity{LOW,MEDIUM,HIGH} (nullable = chưa phân loại)
│                   CapaStatus{DRAFT,PENDING_ADMIN_APPROVAL,APPROVED_FOR_IMPLEMENTATION,IN_PROGRESS,
│                   SUBMITTED_EVIDENCE,COMPLETED} · ResolutionStatus · ViolationType · UnitSanctionLevel
│                 → pure logic: cửa sổ 24h (BR-149), khử trùng HS khi gộp RPT, state machine CAPA
├── service/      SourceReportService · ClusterSignalService (BR-146 — CHỈ gợi ý)
│                 · IncidentRecordService (draft/finalize/openInvestigation/closeNotFoodSafety)
│                 · IncidentInvestigationService (assessAndStart/quarantine/addEvidence/saveRootCause)
│                 · QuarantineService (một chiều — BR-217) · TraceabilityMapService (async, SLA 4h)
│                 · CapaService (6 chuyển trạng thái) · IncidentClosureService (gate C1)
│                 · IncidentReportService (ADR-M11-8) · ViolationService · ViolationResolutionService
│                 · ViolationPdfService · RepeatViolationDetector (BR-187)
│                 · OverrideAbuseReportService + OverrideAbuseWeeklyJob (BR-190)
│                 · EmergencyAlertDispatcher (BR-148, outbox)
│                 → UNIT TEST: Mockito mock repository + 8 port (§8.3)
├── web/          SourceReportController · IncidentRecordController · IncidentController
│                 · CorrectiveActionController · ViolationController
│                 → SECURITY TEST: MockMvc + JWT giả lập 7 role + phạm vi dẫn xuất (ADR-M11-10)
├── integration/  @SpringBootTest + Testcontainers PostgreSQL 16 + Flyway V1 + seed MF11
│                 → API → DB, UNIQUE/CHECK/RULE, outbox, projection, port, job
└── contract/     GET /v3/api-docs → D8 (S1–S8) + D1/D2/D4/D6
```

**NGOÀI phạm vi (test ở module khác, chỉ **stub/seed** ở đây):**

| Thành phần | Chủ | Cách xử lý trong test MF11 |
|---|---|---|
| JWT/RBAC filter, `AuthUser`, `BaseEntity`, `AuditorAware` | MF01 (Nam) | JWT giả lập; **không** test thuật toán token. `TC-INT-013` chỉ assert MF11 **dùng đúng** BaseEntity |
| `Classroom.homeroomTeacherId` (lớp chủ nhiệm) | MF02 (Nam) | seed `3A`(`U-0004`)/`3B`(`U-0009`)/`4A`(`U-0010`); `ClassroomPort` stub |
| `Student` + liên kết phụ huynh (BR-201) | MF03 (Huy) | `StudentProfilePort` stub; seed `HS-3A-001`, `HS-3A-004`, `HS-3B-001`, `HS-3B-002`, `HS-4A-002` |
| `Dish` (món nghi vấn) | MF05 (Thảo) | `DishQueryPort` stub. ⚠️ **L33** — `Dish.code` **chưa tồn tại**, đây là phụ thuộc chặn |
| `InventoryBatch` + trạng thái lô | MF06 (Huy) | `InventoryBatchPort` stub (`markQuarantined`, `supplierOf`, `receivingSessionOf`); **không** UPDATE bảng MF06 |
| `InspectionSession` (CAPA từ BR-095) | MF07 (Thảo) | `InspectionPort` stub; seed `INSP-20260630-L` (`step2=FAILED`, `coreTemp=68`) |
| `FoodSample` / `SampleLabRequest` (BR-113) | MF08 (Mạnh) | `FoodSamplePort` stub; seed `SMP-20260630-03`, `LABREQ-2026-001` |
| `ServingDish` — *"em nào đã ăn món X?"* | MF09 (Minh) | `ServingDishPort` stub; MF11 assert **join bằng ID** (`TC-INT-022`) |
| `Partner` (nhà thầu, đại diện ký) | MF04 (Mạnh) | `PartnerPort` stub trả `CAT-001`/`SUP-003`; **không** `@ManyToOne` |
| Gửi thông báo/cảnh báo khẩn (BR-148/153/159) | **MF10 (Mạnh)** | MF11 chỉ assert **phát event vào outbox**; SMTP là stub ở dev (ADR-000) — **không** test nội dung email |
| Báo cáo tổng hợp vi phạm theo kỳ (UC-189/BR-214) | **MF13 (Nam)** | MF11 chỉ test `GET /violations/summary` cấp số liệu; **không** sở hữu `sent-reports` (L30) |
| Khoá vận hành bếp (BR-105/MSG-024) | — | **Đã descope 05/07/2026** — assert MF11 **không** phát MSG-024 (L17) |

### TDS-02 — Test Basis / Cơ sở Kiểm thử

| Source | Items Derived |
|--------|--------------|
| `traceability_matrix.md` **§1.11** | **20 UC** + tập BR/MSG hợp lệ (nguồn CHUẨN — không nhận mã ngoài bảng này). ⚠️ ghi chú "48h" của §1.11 **lạc hậu** (L38) |
| `MF11_Incident_Emergency_Management.md` (UC file) | Normal Flow → bước nghiệp vụ của TC; **UC-168 EX1** → `TC-018`; **UC-114** (soạn biên bản thủ công) → `TC-005`. ⚠️ **UC-073/074** và **UC-082 EX1** lạc hậu — không dùng làm oracle (L15, L24) |
| `business_rules.md` §11 | **BR-146** chỉ gợi ý · **BR-147** Medical Staff phân loại · **BR-148** HIGH → cảnh báo 3 nhóm · **BR-149** cách ly theo lô + lý do bắt buộc + cửa sổ **24h** · **BR-150** RCA + bằng chứng · **BR-151** bản đồ truy xuất ≤ 4h · **BR-152** CAPA tách khỏi biện pháp khẩn cấp, **không SLA cứng** · **BR-153** duyệt kế hoạch **trước** khi thực hiện · **BR-154** báo cáo chính thức · **BR-155** chỉ School Admin đóng · **BR-216** vòng đời CAPA 6 trạng thái, 1 INC = 1 CAPA · **BR-217** cách ly một chiều |
| `business_rules.md` §18 | **BR-183** biên bản cấp đơn vị · **BR-185** `unitSanctionLevel` 4 mức · **BR-186** LOW ↔ MEDIUM/HIGH phân cấp duyệt · **BR-187** trùng đại diện ≥ MEDIUM/90 ngày → cảnh báo **không chặn** · **BR-188** audit bất biến · **BR-189** registry · **BR-190** override abuse hằng tuần cấp đơn vị · **BR-211** in–ký tay–chụp ảnh + **gate chặn cứng** |
| BR trích chéo | **BR-095/097** (MF07 — CAPA đường sinh 2) · **BR-113** (MF08 — thu hồi mẫu) · **BR-201** (MF03 — parent ≤ 5 HS) · **BR-013** (file ≤ 10MB, MSG-008/009) · **BR-007** (RBAC 7 role) · **BR-170** (audit) |
| `error_message.md` **v1.11** *(v1.1 — trước là v1.8)* | Chuỗi assert nguyên văn: MSG-004, MSG-005, MSG-008, MSG-009, MSG-023, MSG-024\*, MSG-029, MSG-048, MSG-049, MSG-085, MSG-086, MSG-087, MSG-088, MSG-089, MSG-095, MSG-100, MSG-101, **MSG-137**, **MSG-138**, **MSG-139**, **MSG-141**. ⚠️ **10 mã `IEM-` cục bộ chưa có MSG** (L7/L35) |
| `TDS-MF11_Backend.md` §10 | Error code: `IEM-004/005/023/024*/085/088/095/114/147/149/149B/155/186/211/216/217/RO/001/404/500` |
| `TDS-MF11_Backend.md` §16 | Ma trận **7 role × 30 nhóm endpoint** + 5 nhãn phạm vi → nhóm security test |
| `TDS-MF11_Backend.md` §17.1 | **C1–C15** → **mỗi constraint ≥ 1 TC** (chống AP-AI-001) |
| `TDS-MF11_Backend.md` §5.2 | DDL: `uq_inc_record`, `ck_inc_severity_set`, `ck_inc_closed`, `ck_iqb_reason`, `iqb_no_delete`, `incident_events_no_update/no_delete`, `uq_capa_one_per_incident`, `ck_capa_source`, `ck_capa_approved`, `ck_capa_evidence`, `ck_vio_resolution`, `ck_vio_closed`, `ck_vio_reject`, `uq_ias_inc_student` → assertion tầng DB |
| **`00_Common_Conventions.md` D1–D10** | Định danh, envelope, path, ISO ngày, id client, enum, ngôn ngữ message, cột reserved, storage → L1–L10 + `TC-INT-010…013` + `TC-CON-001…005` |
| `semantic-audit.ts` §4, §5, §9, §9b | **12 luật port** (tên nguyên văn): `MAU_NGOAI_KHUNG_SU_CO` · `PHIEU_TRO_MAU_KHONG_TON_TAI` · `PHIEU_KHAC_SU_CO_VOI_MAU` · `BAO_CAO_TRUOC_BUA_AN` · `CACH_LY_LO_KHONG_LIEN_QUAN_BUA` · `SU_CO_TRO_BAO_CAO_KHONG_TON_TAI` · `BAO_CAO_NGUON_KHAC_BUA` · `MOC_TRO_SU_CO_KHONG_TON_TAI` · `MOC_TRUOC_KHI_SU_CO_XAY_RA` · `DONG_THOI_GIAN_LUI` · `CAPA_TRUOC_SU_CO` · `CAPA_GIAO_NCC_KHONG_TON_TAI` · `CACH_LY_KHONG_RO_SU_CO`. **KHÔNG port:** `DONG_SU_CO_MA_CON_LO_KHOA` (L21) |
| `data_model.puml` ràng buộc **C1** | *"Sự cố chỉ đóng được khi mọi CAPA đã COMPLETED"* → `TC-020`, `TC-INT-003` |
| `00_Team_Work_Assignment_Plan.md` §2b | Shape 5 field · clamp `page` · tập rỗng vẫn 200 · `search` chỉ trên `SEARCH_FIELDS` · `dateFrom`/`dateTo` (`reported_at` cho INC/RPT, `date` cho VIO) · **không** `step`/`stepStatus`/`mealPrefix` |
| **PDPD + QĐ 39/2006/QĐ-BYT** | Cắt dữ liệu sức khoẻ theo vai; sai phạm vi → **403 không phải 404**; cửa sổ điều tra **24h** |

*(*) MSG-024 / `IEM-024` liệt kê để giữ vết §1.11 — assert **không endpoint MF11 nào phát ra** (L17).*

### TDS-03 — Test Conditions and Coverage Items

| Condition ID | Test Condition | Coverage Item | Constraint | Test Cases |
|---|---|---|---|---|
| `COND-01` | Gửi `RPT` **không** tạo `INC`, **không** cách ly lô, **không** sinh bản đồ truy xuất | `SourceReportService.create()` | C2 / **BR-146** | `TC-001`, `TC-INT-019` |
| `COND-02` | Tín hiệu cụm ≥ 3 HS/24h là **gợi ý**, không hành động | `ClusterSignalService` | C2 / **BR-146** | `TC-003` |
| `COND-03` | Parent chỉ tạo `RPT` cho HS **đã liên kết** (≤ 5) | guard `SourceReportService` + `StudentProfilePort.isLinkedToParent()` | **BR-201** | `TC-004`, `TC-SEC-009`, `TC-E2E-002` |
| `COND-04` | `INC` **chỉ** sinh từ `REC` ở `COMPLETED`; `incident_record_id` NOT NULL + UNIQUE | `IncidentRecordService.openInvestigation()` + `uq_inc_record` | C3 / **ADR-M11-1** | `TC-005`, `TC-INT-023` |
| `COND-05` | Khử trùng HS khi gộp nhiều `RPT` (theo `studentId`) | `uniqueAffected()` + `uq_ias_inc_student` | ADR-M11-1 | `TC-006` |
| `COND-06` | Chỉ **Medical Staff** phân loại mức độ; giá trị ngoài `{LOW,MEDIUM,HIGH}` → 400 | `assessAndStart()` | **BR-147**, D6 | `TC-007`, `TC-008`, `TC-SEC-002` |
| `COND-07` | `severity=HIGH` ⇒ cảnh báo 3 nhóm, **async**, không chặn giao dịch; MEDIUM/LOW ⇒ không cảnh báo | `EmergencyAlertDispatcher` + outbox | C13 / **BR-148** | `TC-009`, `TC-INT-005` |
| `COND-08` | Cách ly lô: `reason` **bắt buộc**; cách ly lại → 409; **không có** đường nhả lô | `QuarantineService.quarantineBatch()` + `ck_iqb_reason` + RULE `iqb_no_delete` | C5 / **BR-149, BR-217** | `TC-010`, `TC-011`, `TC-INT-002`, `TC-SEC-015` |
| `COND-09` | Cửa sổ món nghi vấn = **24h**, mốc gốc `occurredAt → mealDate → reportedAt` | `suspectedDishCandidates()` | **BR-149** (27/07) | `TC-012` |
| `COND-10` | Bản đồ truy xuất: `COMPILING → READY ≤ 4h`, scope = **lô đã cách ly** | `TraceabilityMapService` | **BR-151**, L23 | `TC-INT-007`, `TC-INT-022` |
| `COND-11` | **1 Incident = 1 CAPA**; CAPA từ kiểm thực **không** dính unique | `uq_capa_one_per_incident` + `ck_capa_source` | C6 / **BR-216, BR-095** | `TC-013`, `TC-014`, `TC-INT-008` |
| `COND-12` | Chuyển trạng thái CAPA **chỉ** qua endpoint nghiệp vụ, kiểm role + trạng thái nguồn | `CapaService` (6 hành động) | C6 / **BR-153, BR-216** | `TC-015`, `TC-016`, `TC-017`, `TC-SEC-003`, `TC-SEC-004` |
| `COND-13` | Đóng sự cố: chỉ School Admin; chặn khi còn CAPA ≠ `COMPLETED` / HS chưa hồi phục / lô chưa xử lý xong | `IncidentClosureService.close()` | C7 / **BR-155**, C1 | `TC-018`, `TC-019`, `TC-020`, `TC-INT-003`, `TC-INT-017`, `TC-E2E-003` |
| `COND-14` | `reportExported=true` ⇒ mọi ghi trên `Incident` → 409; ngoại lệ **duy nhất** `close` + ghi `incident_events` | guard `IncidentReportService` | C10 / **ADR-M11-8** | `TC-021`, `TC-E2E-005` |
| `COND-15` | `incident_events` **append-only**, ghi **cùng transaction** với đổi state | RULE `no_update`/`no_delete` | C4 / **ADR-M11-2** | `TC-INT-001`, `TC-INT-004`, `TC-INT-015` |
| `COND-16` | Lập `VIO` thiếu trường bắt buộc (địa điểm/đại diện/mô tả/ảnh) → 400 | `ViolationService.record()` | C9 / **BR-183, BR-211** | `TC-022` |
| `COND-17` | BR-187 đối chiếu theo **đại diện đơn vị**, cảnh báo **không chặn**; DTO **không** có field cá nhân | `RepeatViolationDetector` | C9 / **BR-187** | `TC-023`, `TC-CON-005` |
| `COND-18` | Gate ảnh ký: chặn `resolution`, **không** chặn lưu biên bản; chưa xuất PDF → chặn tải bản ký | `ViolationResolutionService.submit()` + `ViolationPdfService` | C8 / **BR-211** | `TC-024`, `TC-INT-018` |
| `COND-19` | LOW → Medical Staff tự đóng; MEDIUM/HIGH → bắt buộc School Admin | `ViolationResolutionService` phân nhánh | **BR-186** | `TC-025`, `TC-E2E-004` |
| `COND-20` | Báo cáo override abuse là **dẫn xuất** theo tuần ISO × đơn vị; **không** bảng | `OverrideAbuseReportService.weekly()` | C12 / **BR-190** | `TC-026`, `TC-INT-021` |
| `COND-21` | Hợp đồng list: 5 field, clamp `page`, rỗng vẫn 200; `search` chỉ trên `SEARCH_FIELDS` | `Specification` + `PagedResponse` | C1 / §2b | `TC-027`, `TC-INT-020` |
| `COND-22` | **PDPD:** projection Caterers **không** có dữ liệu sức khoẻ + danh tính HS; Teacher chỉ thấy HS lớp mình | `detailForCaterers()` / nhánh Teacher của `detail()` | C14 / **UC-120**, PDPD | `TC-SEC-006`, `TC-SEC-007`, `TC-E2E-002` |
| `COND-23` | Phạm vi đọc **dẫn xuất**, không cột chủ sở hữu; sai phạm vi → **403 không 404** | `IncidentSpec` (`EXISTS`) | C15 / **ADR-M11-10** | `TC-SEC-008`, `TC-SEC-010`, `TC-SEC-014` |
| `COND-24` | Tham chiếu module khác **bằng ID qua port**; không `@ManyToOne` liên module | 8 port §8.3 | C11 / **ADR-M11-7** | `TC-INT-006`, `TC-INT-016`, `TC-INT-022` |
| `COND-25` | Audit bất biến + IP cho mọi thao tác hồ sơ vi phạm | `@RestControllerAdvice` + audit writer | **BR-170, BR-188** | `TC-SEC-012` |
| `COND-26` | Upload file qua `StorageService`; > 10MB / sai định dạng → MSG-008/009 | `common/storage/StorageService` | **D10**, BR-013 | `TC-SEC-013` |
| `COND-27` | Sinh mã nghiệp vụ **có khoá DB**, không trùng khi song song | `code_sequences` | ADR-M11-5 / D1 | `TC-INT-009` |
| `COND-28` | Spec OpenAPI thoả S1–S8 và **không** lộ bề mặt bị cấm | `/v3/api-docs` | **D8** | `TC-CON-001…005` |

### TDS-04 — Test Techniques / Kỹ thuật Kiểm thử

| Kỹ thuật | Áp cho | Lý do chọn |
|---|---|---|
| **State Transition Testing** | CAPA 6 trạng thái (BR-216), `Incident` 6 trạng thái, `RPT` 5 trạng thái, `ViolationRecord` (resolution × status) | Phần lớn luật MF11 là *"chỉ được đi từ trạng thái A sang B"*; bug thật nằm ở **transition không hợp lệ**, không ở giá trị field |
| **Boundary Value Analysis** | Cửa sổ **24h** (BR-149) ±1 phút · **90 ngày** (BR-187) ±1 ngày · **3 HS/24h** (BR-146) · **4 giờ** (BR-151) · **60 giây** (BR-148) · file **10MB** (BR-013) | Mọi ngưỡng của module đều là **con số có căn cứ pháp lý/nghiệp vụ** — lệch 1 đơn vị là sai luật, không phải sai làm tròn |
| **Decision Table** | BR-186 (`severity` × người duyệt × có/không ảnh ký) · gate đóng sự cố (3 điều kiện độc lập) | Nhiều điều kiện kết hợp; bảng quyết định chống bỏ sót tổ hợp (VD: CAPA xong nhưng HS chưa hồi phục) |
| **Negative / Abuse-case Testing** | Nhóm *"gọi API trực tiếp bỏ qua UI"* (§4.2) | Bắt buộc theo prompt §2c-3b: gate chỉ tồn tại ở client là **lách được toàn bộ luật ATTP** (CF-M11-9) |
| **Concurrency Testing** | `assess` song song · 100 `POST /violations` đồng thời | ADR-M11-5 dùng `code_sequences`; ADR-M11-2 đòi 1 event/1 hành động |
| **Contract Testing** | `/v3/api-docs` (D8) | Chặn lệch D1/D2/D4/D6 quay lại qua file type FE sinh tự động |
| **Property-based (nhẹ)** | Bất biến DB sau khi chạy toàn suite (`TC-INT-023`) | 5 bất biến §4.2 phải đúng với **mọi** dòng, không chỉ dòng test chạm tới |

### TDS-05 — Test Data Requirements

> **Nguồn fixture: `mock-data.ts` — dùng ID THẬT của seed** (Team Plan §2c-3b nguồn 6). Trường QA = `SCH-001`, nhà thầu = `CAT-001`.
> Fixture dựng bằng **Java builder** trong `IncidentTestFixtures.java`; seed DB bằng `@Sql` hoặc Flyway test migration.
> ⚠️ Theo **D1**, mọi giá trị `RPT-…`/`REC-…`/`INC-…`/`CAPA-…`/`VIO-…`/`IEV-…` là **`code`**, không phải PK.
> ⚠️ Theo **D4**, cột ngày trong fixture là `LocalDate`/`Instant`; chuỗi `dd/MM/yyyy` của mock chuyển đổi **một lần** khi sinh seed SQL.
> ⚠️ **L32/L33/L34** — fixture **KHÔNG** dùng `PTN-0007`, `PTN-0003`, `dish-ga-rang-gung`, `STF-007` (4 mã này không tồn tại trong seed).

| Fixture ID | Type | Value / Logic | Mục đích |
|---|---|---|---|
| `FX-001` | DB seed `incident_source_reports` | `RPT-2026-030` (GV 3B `U-0009`, `2026-06-30T06:50Z`, 2 HS 3B, `MERGED`→`INC-2026-014`) · `RPT-2026-031` (GV 3A `U-0004`, 2 HS 3A, `MERGED`) · `RPT-2026-032` (**Parent** `U-0013`, `PENDING_TRIAGE`) · `RPT-2026-033` (GV 4A `U-0010`, `PENDING_TRIAGE`) · `RPT-2026-029` (`CLOSED_NOT_FOOD_SAFETY`) | Hàng chờ triage đủ 4 trạng thái — nền của `TC-003`, `TC-005`, `TC-SEC-007`, `TC-SEC-008` |
| `FX-002` | DB seed `incident_records` | `REC-2026-021` (`status=OPENED`, `incidentId=INC-2026-014`, `occurredAt=2026-06-30T04:15Z`, `location="Nhà ăn tầng 1 — khu lớp 3"`) + `incident_record_sources` trỏ `RPT-2026-030`/`031` | Chuỗi `RPT → REC → INC` (ADR-M11-1) — `TC-005`, `TC-INT-023` |
| `FX-003` | DB seed `incidents` | **`INC-2026-014`**: `severity=HIGH`, `status=CAPA_APPROVED`, `mealDate=2026-06-30`, `occurredAt=2026-06-30T04:15Z`, `reportedAt=2026-06-30T07:20Z`, 4 HS (2×3B, 2×3A) `recovered=false`, `traceabilityMapStatus=READY`, `reportExported=false` | Sự cố chủ đạo — nền của phần lớn TC điều tra/CAPA/đóng |
| `FX-004` | DB seed `incidents` | **`INC-2026-012`**: `severity=MEDIUM`, `status=CLOSED`, CAPA `COMPLETED`, 2 HS `recovered=true`, `closedAt`/`closedById`/`closingStatement` **đã điền** | Hồ sơ đã đóng hợp lệ — đối chứng dương của `ck_inc_closed`, nguồn của `TC-INT-015` (dòng thời gian đủ 5 mốc) |
| `FX-005` ⚠️ | DB seed `incidents` — **ĐÃ SỬA** | **`INC-2026-013`**: seed gốc tự mâu thuẫn (`status=CAPA_APPROVED` + event `CLOSED` + CAPA `SUBMITTED_EVIDENCE`) — **L36**. Fixture chọn nhánh **"chưa đóng"**: **bỏ** `IEV-013-05`, giữ `status=CAPA_APPROVED` | Ca gate đóng sự cố (`TC-020`, `TC-INT-003`): còn CAPA `SUBMITTED_EVIDENCE` ⇒ `close` phải **409** |
| `FX-006` | DB seed `incident_events` | `INC-2026-014`: `IEV-014-01` (`REPORTED`, 12:15) → `IEV-014-02` (`QUARANTINE`, 13:05, actor **`Ngô Thị Hà (Y tế)`** — sửa theo **L39**) → `IEV-014-03` (`SEVERITY_SET`, 14:20) → `IEV-014-04` (`SAMPLE_SENT`, 15:00) → `IEV-014-05` (`TRACE_READY`, 16:20) → `IEV-014-06` (`CAPA_SUBMITTED`, 01/07 09:00) | Dòng thời gian tăng dần — oracle của `MOC_TRUOC_KHI_SU_CO_XAY_RA`, `DONG_THOI_GIAN_LUI` (`TC-INT-015`) |
| `FX-007` | DB seed `incident_quarantine_batches` | 1 dòng: `INC-2026-014` × `BATCH-MP-8820`, `reason="Nghi nhiễm — nhiệt độ lõi 68°C < 75°C (INSP-20260630-L)"`, `quarantinedAt=2026-06-30T06:05Z`, `quarantinedById=U-0002` | Ca "cách ly lại → 409" (`TC-011`) và RULE `iqb_no_delete` (`TC-INT-002`) |
| `FX-008` | DB seed `corrective_actions` | `CAPA-2026-033` (`INC-2026-014`, `APPROVED_FOR_IMPLEMENTATION`, assignee **`CAT-001`**) · `CAPA-2026-030` (`INC-2026-013`, `SUBMITTED_EVIDENCE`, `evidencePhotos=4`) · `CAPA-2026-028` (`INC-2026-012`, `COMPLETED`) · **`CAPA-2026-031`** (`inspectionId=INSP-20260630-L`, `incidentId=NULL`, `IN_PROGRESS`) | 4 trạng thái CAPA + **đường sinh 2** (BR-095) cho `TC-014`. ⚠️ `assigneeSupplierId` = `CAT-001`, **không** `PTN-0007` (L32) |
| `FX-009` ⚠️ | DB seed `violation_records` — **ĐÃ SỬA** | `VIO-2026-008` (MEDIUM, `PENDING_APPROVAL`, `pdfExported=true`, **1 ảnh ký**, đại diện `U-0003`, `relatedIncidentId=INC-2026-014`) — dòng **hợp lệ nguyên bản**. 4 dòng `VIO-2026-004/005/006/009` vi phạm `ck_vio_resolution`/BR-211 (**L37**) ⇒ bổ sung `unit_sanction_level` + `signed_record_file`, hoặc hạ `resolution=PENDING` | `TC-024`, `TC-025`, `TC-INT-018`, `TC-INT-023`, `TC-E2E-004` |
| `FX-010` | DB seed `violation_files` | `VIO-2026-008`: 2 `EVIDENCE` (`bang_chung_01.jpg` 1.6MB, `bang_chung_02.jpg` 2.0MB) + 1 `SIGNED_RECORD` (`bien_ban_da_ky_01.jpg` 2.4MB) | Gate BR-211 nhánh **dương** (`TC-024` đối chứng) |
| `FX-011` | Stub `StudentProfilePort` | `HS-3A-001`/`HS-3A-004`/`HS-3B-001`/`HS-3B-002` tồn tại, thuộc lớp tương ứng · `isLinkedToParent(HS-3B-001, U-0013)=true` · `isLinkedToParent(HS-4A-002, U-0013)=**false**` | BR-201 (`TC-004`, `TC-E2E-002`) |
| `FX-012` | Stub `InventoryBatchPort` | `BATCH-MP-8820` → `supplierOf = SUP-003` (Cty TNHH Thịt sạch Minh Phát), `receivingSessionOf = RCV-20260629-02` (`deliveryDate=2026-06-29`), trạng thái `AVAILABLE` → sau cách ly `QUARANTINED` · `BATCH-GF-4410` → `SUP-001` | Bản đồ truy xuất (`TC-INT-022`) + cách ly (`TC-INT-006`). ⚠️ NCC thật là **`SUP-003`**, không phải `PTN-0003`/"HTX Rau sạch Green Farm" (L32) |
| `FX-013` | Stub `InspectionPort` | `INSP-20260630-L` → `step2=FAILED`, `coreTemp=68`, `overall=FAILED`, `dish="Gà rang gừng"` | Nguồn CAPA đường 2 (BR-095) + nút kiểm thực của bản đồ truy xuất |
| `FX-014` | Stub `FoodSamplePort` | `SMP-20260630-03` → `incidentId=INC-2026-014`, `collectedAt=2026-06-30T03:25Z`, `state=SENT_TO_LAB`, `labRequestId=LABREQ-2026-001` | `TC-INT-018` (luật `MAU_NGOAI_KHUNG_SU_CO`, `PHIEU_KHAC_SU_CO_VOI_MAU`) |
| `FX-015` | Stub `ServingDishPort` | `studentsWhoAte(dishCode "Gà rang gừng", 2026-06-30, LUNCH)` → **4 HS** (`HS-3A-001`, `HS-3A-004`, `HS-3B-001`, `HS-3B-002`) | Mắt xích khép kín trục xương sống (`TC-INT-022`) |
| `FX-016` | Stub `DishQueryPort` | Trả `DishView(code, name)` cho *"Gà rang gừng"*; **`Optional.empty()`** cho mã không tồn tại | `TC-INT-022` + nhánh 400 của `assess` (**L33** — phụ thuộc MF05 cấp `Dish.code`) |
| `FX-017` | Stub `PartnerPort` | `CAT-001` (Service Caterer, `APPROVED`) · `SUP-003` (Raw Food Supplier) · đại diện **`U-0003`** thuộc `CAT-001` | BR-183/187 (`TC-023`), luật `CAPA_GIAO_NCC_KHONG_TON_TAI` (`TC-INT-016`) |
| `FX-018` | Stub `ClassroomPort` | `homeroomOf(U-0004)="3A"` · `homeroomOf(U-0009)="3B"` · `homeroomOf(U-0010)="4A"` | Phạm vi `Own class` (`TC-SEC-007`, `TC-SEC-008`) |
| `FX-019` | Stub `NotificationPort` | Ghi nhận lời gọi, **không** gửi thật; có biến thể **ném `RuntimeException`** để test BR-148 không chặn giao dịch | `TC-009` nhánh (b) — ADR-M11-9 |
| `FX-020` | JWT | `{sub:"U-0002", role:"medical-staff", schoolId:"SCH-001"}` | Actor chính của điều tra (`RoleKey` kebab-case theo **D6**) |
| `FX-021` | JWT | `{sub:"U-0001", role:"school-admin", schoolId:"SCH-001"}` | Duyệt CAPA + đóng sự cố (BR-153/155) |
| `FX-022` | JWT | `{sub:"U-0003", role:"service-caterers", schoolId:"SCH-001", partnerId:"CAT-001"}` | Nhận CAPA + nộp bằng chứng; **cấm** assess/close/approve |
| `FX-023` | JWT | `{sub:"U-0004", role:"teacher", schoolId:"SCH-001", classId:"3A"}` · `{sub:"U-0010", role:"teacher", classId:"4A"}` | Phạm vi `Own class` + đối chứng lớp khác (`TC-SEC-007`) |
| `FX-024` | JWT | `{sub:"U-0013", role:"parent", schoolId:"SCH-001"}` liên kết `HS-3B-001` | BR-201 (`TC-004`, `TC-SEC-009`) |
| `FX-025` | JWT | `{role:"system-admin"}` · `{role:"supplier", partnerId:"SUP-003"}` · JWT `schoolId="SCH-002"` | Đủ 7 role cho ma trận §16 + ca **khác trường** (`TC-SEC-010`, `TC-SEC-016`) |
| `FX-026` | Testcontainer | `PostgreSQLContainer("postgres:16-alpine")` + Flyway `V20__mf11_incident.sql` (13 bảng MF11) + `V21__seed_mf11.sql` | Nền integration; timeout khởi động 120s |
| `FX-027` | env | `spring.profiles.active=test`, `SFMS_JWT_SECRET=test-secret-32-chars-minimum-len`, `TZ=Asia/Ho_Chi_Minh` | `ck_inc_after_meal` so `reported_at >= meal_date::timestamptz` — sai TZ là test đỏ/xanh ngẫu nhiên quanh nửa đêm |
| `FX-028` | **Clock** | `Clock.fixed(2026-06-30T06:00:00Z, Asia/Ho_Chi_Minh)` inject vào service | Cửa sổ 24h (BR-149) và 90 ngày (BR-187) tính từ "bây giờ" — **không ghim clock thì boundary test đỏ ngẫu nhiên** theo ngày chạy CI |
| `FX-029` | Fake outbox / event bus | Thu `SourceReportFiled`, `SymptomClusterDetected`, `IncidentOpened`, `IncidentSeverityAssigned`, **`HighSeverityAlertRaised`**, `BatchQuarantined`, `TraceabilityMapCompiled`, `SampleRetrievalRequested`, `CapaSubmittedForApproval`, `CapaApproved/Rejected`, `CapaEvidenceSubmitted`, `CapaCompleted/ReworkRequested`, `IncidentReportSubmitted`, `IncidentClosed`, `ViolationRecorded`, `ViolationResolutionSubmitted/Approved/Rejected`, `OverrideAbuseWeeklyCompiled` | §7.1 — assert event phát ra, **không** gửi thật (SMTP stub, ADR-000) |
| `FX-030` | ❌ **KHÔNG seed** | Mảng tĩnh `OVERRIDE_ABUSE_REPORT` (`OVR-W27-01/02`, `OVR-W26-01`) của `mock-data.ts` | **Cố ý bỏ** — L20/CF-M11-10: báo cáo là **dẫn xuất**; seed nó vào DB là tái tạo đúng lỗi *"hai bảng đếm hai kiểu"* |
| `FX-031` | ❌ **KHÔNG seed** | Bảng `incident_quarantine_release_log` (dữ liệu nhả lô 19–27/07) | Không có dòng lịch sử nào trong phạm vi test; bảng tồn tại **rỗng, read-only** — `TC-INT-002` assert **0 dòng ghi mới** |

**Nguyên tắc cách ly dữ liệu:** mỗi integration test `@Transactional` + rollback, **hoặc** `@Sql("/fixtures/mf11-reset.sql", executionPhase = BEFORE_TEST_METHOD)`.
⚠️ **Ngoại lệ quan trọng:** `TRUNCATE` chỉ áp cho DB test của Testcontainers. Trên môi trường có dữ liệu thật, `incident_events`, `violation_records`, `incident_reports` là **append-only, giữ tối thiểu 10 năm** (§4.2 TDS — có thể là căn cứ pháp lý/chế tài hợp đồng) — xem §7 Rollback **R1**.

---

## 4. Test Case Specification

> **TC ID format:** `MF11-TC-[NNN]` (unit) · `MF11-TC-SEC-[NNN]` (security) · `MF11-TC-INT-[NNN]` (integration) · `MF11-TC-CON-[NNN]` (contract OpenAPI) · `MF11-TC-E2E-[NNN]`
> **Severity:** CRITICAL / HIGH / MEDIUM / LOW
> **Status:** 🔴 Not written / 🟡 Written-failing / 🟢 Passing
> **Oracle Source:** bắt buộc — chỉ nhận BR / bước UC / MSG nguyên văn / luật `semantic-audit.ts` / ADR module / **D1–D10**. TC không có oracle → reject (**AP-AI-001**).
> **Assert lỗi:** theo **D2** — `$.error.code` + `$.error.details` là **mảng**. `$.error.message` assert **nguyên văn tiếng Anh** (D7) — ✅ **từ 04/08 áp cho MỌI mã của MF11**, vì `error_message.md` v1.11 đã cấp MSG cho cả 10 mã cục bộ. *(Điều kiện "chỉ khi mã đó có MSG thật" bên dưới giữ lại làm quy tắc chung cho module khác.)* Điều kiện gốc: **chỉ khi** mã đó có MSG thật; 10 mã cục bộ MF11 → phần assert message `@Disabled` (**L7/L35**).
> **Assert ngày:** theo **D4** — ISO-8601 (`"2026-06-30"` / `"2026-06-30T07:20:00Z"`), **không** `dd/MM/yyyy` (ghi đè ADR-M11-5/C12 — xem **L4**).

### Fixture Isolation Boilerplate (CASE 2.0 — bản Java)

> ⭐ **CASE 2.0 Rule:** mỗi test tạo instance **mới** qua factory. Không shared mutable state (chống **AP-AI-002**).
> ⚠️ Theo **D1**: builder set `code` (mã nghiệp vụ), **không** set `id` (UUID do JPA sinh).
> ⚠️ `IncidentEvent` **không có setter public** (ADR-M11-2) — factory dựng qua constructor, đúng như production code phải làm.

```java
package com.sfms.mf11;

import com.sfms.mf11.domain.*;
import java.time.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Fixture factory cho MF11 — baseline đồng bộ FX-001…FX-031 (§3 TDS-05).
 * D1: code = mã nghiệp vụ; id (UUID) do JPA sinh, KHÔNG set tay.
 * D4: ngày là LocalDate/Instant, KHÔNG chuỗi "dd/MM/yyyy".
 * L32/L34: đối tác = CAT-001 / SUP-003, đại diện = U-0003 (PTN-0007 và STF-007 KHÔNG tồn tại).
 * MỌI test gọi newIncident()/newCapa()/newViolation() để có instance RIÊNG.
 */
public final class IncidentTestFixtures {

    public static final String SCHOOL_CODE   = "SCH-001";
    public static final String CATERER_CODE  = "CAT-001";   // đơn vị suất ăn (PARTNERS)
    public static final String MEAT_SUPPLIER = "SUP-003";   // NCC của BATCH-MP-8820
    public static final String MEDICAL_STAFF = "U-0002";    // Ngô Thị Hà (Y tế)
    public static final String SCHOOL_ADMIN  = "U-0001";    // Phạm Quốc Toản (BGH)
    public static final String CATERER_REP   = "U-0003";    // Trần Văn Hùng — đại diện nhà thầu
    public static final String TEACHER_3A    = "U-0004";
    public static final String TEACHER_3B    = "U-0009";
    public static final String PARENT_3B     = "U-0013";
    /** FX-028 — ghim clock; cửa sổ 24h (BR-149) và 90 ngày (BR-187) tính từ "bây giờ". */
    public static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-06-30T06:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));

    /** Baseline = FX-003 (INC-2026-014, HIGH, đang điều tra, chưa xuất báo cáo). */
    @SafeVarargs
    public static Incident newIncident(Consumer<Incident>... overrides) {
        Incident i = new Incident();
        i.setCode("INC-TEST-" + java.util.UUID.randomUUID().toString().substring(0, 6));
        i.setSchoolCode(SCHOOL_CODE);
        i.setIncidentRecordCode("REC-2026-021");            // C3 — NOT NULL (ADR-M11-1)
        i.setTitle("Nghi ngộ độc sau bữa trưa 30/06");
        i.setMealDate(LocalDate.of(2026, 6, 30));           // D4: LocalDate
        i.setOccurredAt(Instant.parse("2026-06-30T04:15:00Z"));
        i.setReportedAt(Instant.parse("2026-06-30T07:20:00Z"));
        i.setReportedByLabel("Ngô Thị Hà (Y tế) — biên bản REC-2026-021");
        i.setSeverity(IncidentSeverity.HIGH);               // D6: SCREAMING_SNAKE
        i.setStatus(IncidentStatus.INVESTIGATING);
        i.setTraceabilityMapStatus(TraceMapStatus.READY);
        i.setReportExported(false);                         // C10 — chưa khoá
        for (Consumer<Incident> o : overrides) o.accept(i);
        return i;
    }

    /** FX-008 — CAPA-2026-033: đường sinh 1 (BR-152), đã được BGH duyệt kế hoạch. */
    @SafeVarargs
    public static CorrectiveAction newCapa(Consumer<CorrectiveAction>... overrides) {
        CorrectiveAction c = new CorrectiveAction();
        c.setCode("CAPA-TEST-" + java.util.UUID.randomUUID().toString().substring(0, 6));
        c.setSchoolCode(SCHOOL_CODE);
        c.setIncidentCode("INC-2026-014");                  // ck_capa_source: XOR với inspectionCode
        c.setInspectionCode(null);
        c.setDescription("Vệ sinh khử khuẩn khu sơ chế; tập huấn lại kiểm soát nhiệt độ lõi.");
        c.setAssigneeLabel("Cty TNHH Suất ăn An Bình");
        c.setAssigneeSupplierCode(CATERER_CODE);            // L32 — KHÔNG dùng "PTN-0007"
        c.setStatus(CapaStatus.APPROVED_FOR_IMPLEMENTATION);
        c.setSubmittedForApprovalAt(Instant.parse("2026-07-01T02:00:00Z"));
        c.setApprovedAt(Instant.parse("2026-07-01T03:05:00Z"));
        c.setDeadline(null);                                // L31(d) — deprecated, luôn null với bản ghi mới
        for (Consumer<CorrectiveAction> o : overrides) o.accept(c);
        return c;
    }

    /** FX-009 — VIO-2026-008: MEDIUM, đã xuất PDF + đã có ảnh ký (nhánh DƯƠNG của gate BR-211). */
    @SafeVarargs
    public static ViolationRecord newViolation(Consumer<ViolationRecord>... overrides) {
        ViolationRecord v = new ViolationRecord();
        v.setCode("VIO-TEST-" + java.util.UUID.randomUUID().toString().substring(0, 6));
        v.setSchoolCode(SCHOOL_CODE);
        v.setSubject("Khu sơ chế — không tách thớt sống/chín");
        v.setDescription("<p>Dùng chung thớt cho thịt sống và rau đã chần.</p>");
        v.setSeverity(ViolationSeverity.MEDIUM);
        v.setViolationType(ViolationType.HYGIENE_VIOLATION);
        v.setOccurredAt(Instant.parse("2026-06-30T02:15:00Z"));
        v.setLocationOfIncident("Khu sơ chế, bếp trung tâm");   // BR-211 — bắt buộc
        v.setCaterersRepresentativeCode(CATERER_REP);           // L34 — KHÔNG dùng "STF-007"
        v.setRecordedById(MEDICAL_STAFF);
        v.setDate(LocalDate.of(2026, 6, 30));
        v.setResolution(ResolutionStatus.PENDING);
        v.setStatus(ViolationStatus.OPEN);
        v.setPdfExported(true);
        // C9/BR-183 (26/07): KHÔNG set staffName/staffPosition/disciplinaryLevel — 3 cột chỉ đọc dữ liệu cũ
        for (Consumer<ViolationRecord> o : overrides) o.accept(v);
        return v;
    }
}
```

**Kiểm tự động chống shared state + chống field cá nhân** (CI, §6):

```bash
# (a) CASE 2.0 — không static mutable fixture
grep -rnE "^\s*(private )?static\s+(final\s+)?(Incident|CorrectiveAction|ViolationRecord|IncidentEvent)\s+" \
     src/test/java/com/sfms/mf11/ | grep -v IncidentTestFixtures.java
# Expected: no output

# (b) C9/BR-183 — không DTO/request nào ghi field cá nhân (AP-M11-07)
grep -rnE "staffName|staffPosition|disciplinaryLevel" src/main/java/com/sfms/mf11/dto/
# Expected: no output

# (c) C5/BR-217 — không có đường nhả lô (AP-M11-03)
grep -rniE "release|unquarantine|releasedAt" src/main/java/com/sfms/mf11/ \
     | grep -viE "QuarantineReleaseLog|// lịch sử"
# Expected: no output
```

---

### 4.1. UNIT TEST CASES

> Layer `com.sfms.mf11.service`. Repository + **8 port** (`StudentProfilePort`, `DishQueryPort`, `InventoryBatchPort`, `InspectionPort`, `FoodSamplePort`, `ServingDishPort`, `PartnerPort`, `NotificationPort`) = **Mockito mock**. Không chạm DB.

---

#### MF11-TC-001 — BR-146: gửi báo cáo thô KHÔNG tạo sự cố, KHÔNG cách ly, KHÔNG sinh bản đồ

**Severity:** `CRITICAL`
**Feature Under Test:** `SourceReportService.createByTeacher()` / `.createByParent()`
**Test File:** `service/SourceReportCreateTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-01`
**Constraint Ref:** `TDS §17.1 C2` · `AP-M11-01` (Auto-create incident)
**Oracle Source:** **BR-146** (sửa 20/07/2026) nguyên văn: *"Hệ thống **không tự động gộp báo cáo, không tự động khởi tạo** Incident dựa trên ngưỡng này — quyết định gộp nhiều `RPT-xxx` thành 1 `INC-xxx` … **hoàn toàn thủ công** do Medical Staff thực hiện."* + **BR-149** (sửa 20/07: bỏ cách ly tự động) + TDS §13 `TC-U-002` + **CF-M11-5/L15**
**Kỹ thuật:** Negative testing (kiểm **việc KHÔNG xảy ra**)

**Preconditions:**
- `FX-023` JWT GVCN 3A (`U-0004`) · `FX-011` `HS-3A-001` thuộc 3A
- Repository `incidentRepository`, `incidentRecordRepository` mock; port `InventoryBatchPort`, `DishQueryPort` mock

**Test Steps:**
1. **Arrange:** payload hợp lệ (`className="3A"`, 1 HS, `symptoms=["Đau bụng"]`, `mealDate=2026-06-30`, `description` ≠ rỗng).
2. **Act:** `createByTeacher(payload, teacher3A)`.
3. **Assert:** chỉ `RPT` được tạo; mọi tác dụng phụ **không** xảy ra.

**Expected Result (PASS):**
- Trả `RPT` mới: `status = PENDING_TRIAGE`, `code` khớp `^RPT-\d{4}-\d{3}$`, `clusterSignal = null` (chưa đủ 3 HS)
- `verify(incidentRepository, never()).save(any())` — **không** `INC` nào sinh ra
- `verify(inventoryBatchPort, never()).markQuarantined(any(), any(), any())` — **không** lô nào bị cách ly
- `verify(traceabilityMapService, never()).compile(any())` — **không** bản đồ truy xuất nào được sinh (**L40**: MSG-048 có vế *"Investigation map generated"* nhưng ở ngữ cảnh này **không** đúng)
- **Không** dòng `incident_events` nào loại `QUARANTINE` được ghi (**L39**)
- Event phát ra **đúng 1**: `SourceReportFiled`; **không** có `IncidentOpened`, `BatchQuarantined`
- `message` = **MSG-048** nguyên văn `"Suspected food safety incident report logged. Investigation map generated."` (đúng §1.11, kèm ghi chú L40)

**Expected Result (FAIL — dấu hiệu lỗi):**
- Có `INC` sinh tự động ⇒ **AP-M11-01, reject cứng**: Medical Staff mất quyền quyết định mở sự cố, và mỗi báo cáo nhầm của phụ huynh sẽ tạo một hồ sơ điều tra rác **không xoá được** (bảng có RULE chặn DELETE)
- Có lô bị cách ly tự động ⇒ tái tạo đúng thiệt hại mà BR-149 bản 20/07 được viết ra để dẹp (cách ly toàn bộ 50kg thịt vì nghi 1 món)

**Current Status:** 🔴 Not written

---

#### MF11-TC-002 — MSG-023: gửi `RPT` thiếu trường bắt buộc → 400

**Severity:** `HIGH`
**Feature Under Test:** `SourceReportService.create()` — DTO validation
**Test File:** `service/SourceReportCreateTest.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-01` · **Constraint Ref:** `C2`
**Oracle Source:** **MSG-023** nguyên văn = `"Incident report incomplete: Symptoms, date of ingestion, and dishes are required."` + §10 TDS → `IEM-023`, HTTP **400** + UC-074 bước 4 + TDS §13 `TC-U-001`
**Kỹ thuật:** Equivalence Partitioning (3 phân vùng thiếu: `symptoms` rỗng · `mealDate` null · `description` rỗng)

**Test Steps:** parameterized 3 payload thiếu → `create()`.

**Expected Result (PASS):**
- `ValidationException` → `code = "IEM-023"`, HTTP **400**
- `message` = **MSG-023 nguyên văn tiếng Anh** (mã này **có** MSG thật ⇒ assert bật, không `@Disabled`)
- `$.error.details` là **mảng** ≥ 1 phần tử, mỗi phần tử có `field` + `message`
- `verify(sourceReportRepository, never()).save(any())`

**Current Status:** 🔴 Not written

---

#### MF11-TC-003 — BR-146: tín hiệu cụm ≥ 3 HS/24h chỉ **gợi ý**, không hành động

**Severity:** `HIGH`
**Feature Under Test:** `ClusterSignalService.detect()`
**Test File:** `service/ClusterSignalTest.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-02` · **Constraint Ref:** `C2`
**Oracle Source:** **BR-146**: *"tự động phát hiện và hiển thị thông báo gợi ý … khi có **từ 3 học sinh trở lên** báo triệu chứng tương đồng trong vòng **24 giờ**"* + TDS §13 `TC-U-003` + §7.1 event `SymptomClusterDetected`
**Kỹ thuật:** Boundary Value Analysis (2 HS / **3 HS** / 4 HS; trong 20h / **đúng 24h** / 24h+1phút)

**Expected Result (PASS):**
- **2 HS** trong 24h → **không** signal; **3 HS** → có `ClusterSignal{symptomKey, reportCodes[], studentCount=3, window="24h"}`
- **3 HS nhưng trải 24h + 1 phút** → **không** signal (biên đóng ở 24h)
- Với mọi nhánh: `verify(incidentRepository, never()).save(any())` — signal **không** tạo sự cố
- Event `SymptomClusterDetected` phát đúng 1 lần khi vượt ngưỡng, consumer là MF10 (**gợi ý**)

**Implementation Note:** ngưỡng "3 HS/24h" là **quy ước nội bộ dự án**, BR-146 ghi rõ *"không tìm thấy căn cứ pháp luật"* — vẫn là oracle hợp lệ vì BR đã chốt, nhưng **không** được tự đổi số.

**Current Status:** 🔴 Not written

---

#### MF11-TC-004 — BR-201: Parent báo cáo cho HS không liên kết → 403

**Severity:** `CRITICAL`
**Feature Under Test:** `SourceReportService.createByParent()` — guard `StudentProfilePort.isLinkedToParent()`
**Test File:** `service/SourceReportCreateTest.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-03` · **Constraint Ref:** `C14`, `C15`
**Oracle Source:** **BR-201** (MF03 — phụ huynh chỉ thao tác trên HS đã liên kết, ≤ 5) + §16 TDS (`POST /source-reports` — Parent `✅ Own child`) + **MSG-005** nguyên văn = `"Access denied: You do not have permission to access this resource."` → `IEM-005`, HTTP **403** + TDS §13 `TC-E2E-012`

**Expected Result (PASS):**
- Parent `U-0013` (liên kết `HS-3B-001`) gửi `RPT` cho `HS-4A-002` → `code = "IEM-005"`, HTTP **403**, `message` = **MSG-005 nguyên văn**
- `verify(sourceReportRepository, never()).save(any())`
- Nhánh dương: cùng Parent gửi cho `HS-3B-001` → **201**
- **403, không phải 404** — nhưng response **không** tiết lộ HS đó có tồn tại hay không (`details` rỗng)

**Current Status:** 🔴 Not written

---

#### MF11-TC-005 — ADR-M11-1: `INC` chỉ sinh từ `REC` hợp lệ; `RPT` sai trạng thái → 409

**Severity:** `HIGH`
**Feature Under Test:** `IncidentRecordService.openInvestigation()`
**Test File:** `service/IncidentRecordServiceTest.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-04` · **Constraint Ref:** `C3`
**Oracle Source:** **ADR-M11-1** (*"`Incident` **không** tạo được nếu không có `incident_record_id` trỏ tới một `REC` ở trạng thái `COMPLETED`"*) + §10 → `IEM-114`, HTTP **409** + **MSG-048** (thành công) / **MSG-101** (finalize) + TDS §13 `TC-U-004`
**Kỹ thuật:** State Transition Testing

**Expected Result (PASS):**
- Gộp `RPT` đang ở `DRAFTING` → `code = "IEM-114"`, HTTP **409**, `details[0].field = "RPT-2026-030"`, assert `message` = **MSG-149** nguyên văn *(v1.1 — đã cấp ở `error_message.md` v1.11; **bỏ `@Disabled`**)*
- `REC` ở `DRAFT` (chưa `COMPLETED`) → cũng **409**, `INC` **không** được tạo
- Nhánh dương: `REC-2026-021` `COMPLETED` + 2 `RPT` `PENDING_TRIAGE` → **201**, `incidentRecordCode` **NOT NULL**, `severity = null` (JSON hiện `"PENDING"` — **L22**), `status = SEVERITY_PENDING`, `traceabilityMapStatus = COMPILING`, `message` = **MSG-048** nguyên văn
- Event `IncidentOpened` phát 1 lần; `incident_events` có mốc `REPORTED`

**Current Status:** 🔴 Not written

---

#### MF11-TC-006 — Khử trùng HS bị ảnh hưởng khi gộp nhiều `RPT`

**Severity:** `MEDIUM`
**Feature Under Test:** `IncidentRecordService.openInvestigation()` → `uniqueAffected()`
**Test File:** `service/IncidentRecordServiceTest.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-05` · **Constraint Ref:** `C3`
**Oracle Source:** `uq_ias_inc_student UNIQUE (incident_id, student_id)` (§5.2) + hàm `uniqueAffected()` của mock + TDS §13 `TC-U-005`

**Expected Result (PASS):**
- Gộp `RPT-2026-030` (`HS-3B-001`, `HS-3B-002`) + `RPT-2026-032` (`HS-3B-001`) → `affectedStudents.size() == 2`, **không** trùng `HS-3B-001`
- Khử trùng theo **`studentId`**, không theo `studentName` (2 HS trùng tên vẫn là 2 dòng)
- `studentName`/`className` là **snapshot lúc gộp** (ADR-M11-7) — đổi tên HS bên MF03 sau đó **không** làm đổi hồ sơ sự cố

**Current Status:** 🔴 Not written

---

#### MF11-TC-007 — BR-147: chỉ Medical Staff phân loại mức độ

**Severity:** `CRITICAL`
**Feature Under Test:** `IncidentInvestigationService.assessAndStart()`
**Test File:** `service/AssessIncidentTest.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-06` · **Constraint Ref:** `C14` · **AP-M11-05**
**Oracle Source:** **BR-147** nguyên văn: *"**Medical Staff** chịu trách nhiệm **phân loại mức độ nghiêm trọng** (Severity) của sự cố."* + §16 TDS (`POST /incidents/{code}/assess` — chỉ Medical Staff `✅`) + **MSG-005** → `IEM-005`, HTTP **403**
**Kỹ thuật:** Decision Table (7 role × 1 endpoint)

**Expected Result (PASS):**
- `school-admin`, `service-caterers`, `teacher`, `parent`, `supplier`, `system-admin` → **403 `IEM-005`**, `message` = MSG-005 nguyên văn, `severity` trong DB **không đổi**
- `medical-staff` → **200**, `severity` được set, event `IncidentSeverityAssigned` phát 1 lần
- Với `school-admin`: **403 không phải 404** — School Admin **thấy** được sự cố (§16) nhưng **không** phân loại được ⇒ trả 404 sẽ khiến FE hiểu nhầm là hồ sơ biến mất

**Current Status:** 🔴 Not written

---

#### MF11-TC-008 — BR-147/D6: `severity` ngoài enum → 400

**Severity:** `HIGH`
**Feature Under Test:** `assessAndStart()` — DTO enum binding
**Test File:** `service/AssessIncidentTest.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-06` · **Constraint Ref:** `C12` / **D6**
**Oracle Source:** **BR-147** (3 mức Low/Medium/High) + §5.2 `CHECK (severity IN ('LOW','MEDIUM','HIGH'))` + §10 → `IEM-147`, HTTP **400** + **D6** (SCREAMING_SNAKE) + **CF-M11-12/L22**

**Expected Result (PASS):**
- Parameterized: `"CRITICAL"`, `"Cao"`, `"high"`, `""`, `"PENDING"` → **400 `IEM-147`** (⚠️ **`"PENDING"` cũng bị từ chối** — nó là **giá trị hiển thị** khi `severity IS NULL`, không phải giá trị nhận vào)
- `LOW`/`MEDIUM`/`HIGH` → chấp nhận
- `details[0].field = "severity"`; assert `message` = **MSG-150** nguyên văn *(v1.1 — đã cấp; **bỏ `@Disabled`**)*
- DB: sau khi rời `SEVERITY_PENDING`, `ck_inc_severity_set` đảm bảo `severity IS NOT NULL`

**Current Status:** 🔴 Not written

---

#### MF11-TC-009 — BR-148: `HIGH` ⇒ cảnh báo 3 nhóm, **bất đồng bộ**, không chặn giao dịch

**Severity:** `CRITICAL`
**Feature Under Test:** `assessAndStart()` → `EmergencyAlertDispatcher` (outbox)
**Test File:** `service/EmergencyAlertTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-07` · **Constraint Ref:** `TDS §17.1 C13` · `AP-M11-11` (Sync emergency alert)
**Oracle Source:** **BR-148** nguyên văn: *"Sự cố được phân loại **mức độ cao (High Severity)** phải kích hoạt **ngay lập tức** cảnh báo đến: Ban giám hiệu nhà trường · Công ty catering · Cơ quan y tế địa phương."* + **ADR-M11-9** (async, SLA < 60s) + §4.1 TDS + §7.1 `HighSeverityAlertRaised` + TDS §13 `TC-U-008`/`TC-U-009`
**Kỹ thuật:** Decision Table (severity × trạng thái MF10)

**Test Steps:**
1. **(a)** `severity = HIGH`, `NotificationPort` bình thường → assert event + recipients.
2. **(b)** `severity = HIGH`, `NotificationPort` **ném `RuntimeException`** (`FX-019`) → assert giao dịch phân loại **vẫn thành công**.
3. **(c)** `severity = MEDIUM` và `LOW` → assert **không** event khẩn.

**Expected Result (PASS):**
- (a) đúng **1** `HighSeverityAlertRaised` trong outbox, `recipients = ["SCHOOL_BOARD", "CATERER", "LOCAL_HEALTH_AUTHORITY"]` (đủ **3 nhóm** BR-148), `severity = HIGH`
- (a) `emergencyAlert.dispatched = true` trong response; MF11 **không** tự gọi SMTP/SMS: `verify(mailSender, never())`
- (b) `assess` trả **200**, `severity` đã ghi, event vẫn nằm trong outbox chờ retry — **giao dịch không rollback** vì MF10 lỗi
- (c) **0** event `HighSeverityAlertRaised` với MEDIUM/LOW

**Expected Result (FAIL — dấu hiệu lỗi):**
- Gửi email **đồng bộ** trong `assess()` ⇒ **AP-M11-11**: đúng lúc đang xử lý cấp cứu, thao tác phân loại mức độ bị treo theo hàng đợi thông báo — trái tinh thần *"ngay lập tức"* của BR-148 (nghịch lý: làm đồng bộ để "ngay lập tức" lại khiến nó chậm nhất)
- Thiếu 1 trong 3 nhóm nhận ⇒ vi phạm trực tiếp BR-148, cơ quan y tế địa phương không được báo

**Current Status:** 🔴 Not written
**Implementation Note:** SLA < 60s được đo ở `TC-INT-005` (integration, transactional outbox), không đo ở unit.

---

#### MF11-TC-010 — BR-149: cách ly lô thiếu lý do → 400, **không** gọi port

**Severity:** `CRITICAL`
**Feature Under Test:** `QuarantineService.quarantineBatch()`
**Test File:** `service/QuarantineServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-08` · **Constraint Ref:** `TDS §17.1 C5` · `AP-M11-03`
**Oracle Source:** **BR-149** (sửa 27/07/2026) nguyên văn: *"Medical Staff **tự chọn từng lô nguyên liệu cụ thể** … hệ thống đặt trạng thái Quarantine **ngay khi chọn**, kèm **lý do cách ly bắt buộc** nhập ngay tại dòng lô đó"* + §5.2 `ck_iqb_reason CHECK (length(btrim(reason)) > 0)` + §10 → `IEM-149`, HTTP **400** + TDS §13 `TC-U-010`

**Expected Result (PASS):**
- Parameterized `reason ∈ {"", "   ", null}` → **400 `IEM-149`**, `details[0].field = "reason"`
- `verify(inventoryBatchPort, never()).markQuarantined(any(), any(), any())` — **không** lô nào bị khoá
- **Không** event `BatchQuarantined`, **không** dòng `incident_events`
- assert `message` nguyên văn theo bảng §10 *(v1.1 — MSG đã cấp; **bỏ `@Disabled`**)*

**Expected Result (FAIL — dấu hiệu lỗi):**
- Cho phép cách ly không lý do ⇒ hành động **không thể hoàn tác** (BR-217) mà **không có ghi vết lý do** — sau này không giải trình được với nhà cung cấp khi bị khiếu nại, và vi phạm BR-170 (audit)

**Current Status:** 🔴 Not written

---

#### MF11-TC-011 — BR-217: cách ly là **một chiều** — cách ly lại → 409, không có đường nhả lô

**Severity:** `CRITICAL`
**Feature Under Test:** `QuarantineService.quarantineBatch()` + bề mặt API của service
**Test File:** `service/QuarantineServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-08` · **Constraint Ref:** `C5` · `AP-M11-03` (Release endpoint)
**Oracle Source:** **BR-217** (sửa 27/07/2026) — bước 5–6 (*"mở khoá"*) **bị bỏ**: *"không còn 'lô đang khoá vật lý' nào để mở, vì đã trả về NCC ở bước 1b từ trước khi có kết quả"* + **ADR-M11-3** (*"Gọi lại với lô đã cách ly → **409 `IEM-149B`**, không no-op im lặng"*) + TDS §13 `TC-U-011`

**Test Steps:**
1. Cách ly `BATCH-MP-8820` lần 1 → 200.
2. Cách ly **cùng lô, cùng sự cố** lần 2 → assert 409.
3. Reflection: quét `QuarantineService` + `IncidentController` tìm method/route chứa `release`/`unquarantine`.

**Expected Result (PASS):**
- Lần 2 → `code = "IEM-149B"`, HTTP **409**; `details` nêu `quarantinedAt` của lần 1 (ISO — **D4**)
- **Không** ghi đè `reason`/`quarantinedAt` của lần 1 (dữ liệu lần đầu là bằng chứng)
- Reflection: **0** method public nào khớp `(?i)release|unquarantine`; **0** field `releasedAt`/`releaseReason` trên `IncidentQuarantineBatch`
- Nhánh hợp lệ: **cùng lô, sự cố KHÁC** → **200** (unique là `(incident_id, batch_id)`)

**Expected Result (FAIL — dấu hiệu lỗi):**
- No-op im lặng trả 200 ⇒ Medical Staff tưởng vừa cách ly thêm; hoặc sinh endpoint nhả lô ⇒ **AP-M11-03 reject cứng** (lô đã trả về NCC rồi, "mở khoá" trong hệ thống là **nói dối về trạng thái vật lý**)

**Current Status:** 🔴 Not written

---

#### MF11-TC-012 — BR-149: cửa sổ món nghi vấn = **24 giờ**, mốc gốc `occurredAt → mealDate → reportedAt`

**Severity:** `HIGH`
**Feature Under Test:** `IncidentInvestigationService.suspectedDishCandidates()`
**Test File:** `service/SuspectedDishWindowTest.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-09` · **Constraint Ref:** `C5`
**Oracle Source:** **BR-149** (sửa 27/07/2026): *"đổi cửa sổ 72 giờ → **24 giờ**, áp thống nhất cho CẢ danh sách 'món ăn nghi vấn' lẫn danh sách mẫu gửi xét nghiệm"*, căn cứ **QĐ 39/2006/QĐ-BYT** + ghi chú thực thi 27/07 (*"mốc gốc … `occurredAt` → `mealDate` → `reportedAt`"*) + TDS §13 `TC-U-012`/`TC-U-013`. ⚠️ **KHÔNG** dùng "48h" của ghi chú §1.11 (**L38**)
**Kỹ thuật:** Boundary Value Analysis

**Expected Result (PASS):**
- `occurredAt = 2026-06-30T04:15Z` ⇒ cửa sổ `[2026-06-29T04:15Z, 2026-06-30T04:15Z]`
- Món phục vụ lúc `04:16Z` ngày 29/06 → **có**; lúc `04:14Z` ngày 29/06 → **không**; lúc `04:15Z` (đúng biên) → **có** (biên đóng)
- Thiếu `occurredAt` → dùng `mealDate` (00:00 giờ VN của ngày đó); thiếu cả hai → `reportedAt`
- ⚠️ Assert **không** dùng 48h/72h: một món phục vụ 46h trước `occurredAt` phải **không** xuất hiện

**Expected Result (FAIL — dấu hiệu lỗi):**
- Dùng `reportedAt` làm mốc gốc mặc định ⇒ với cửa sổ hẹp 24h, danh sách rỗng rất nhanh (BR-149 ghi rõ: *"sai mốc gây rỗng danh sách nhanh hơn hẳn"*) — Medical Staff không chọn được món nghi vấn nào, điều tra đứng

**Current Status:** 🔴 Not written

---

#### MF11-TC-013 — BR-216: 1 Incident = 1 CAPA → tạo CAPA thứ 2 → 409

**Severity:** `HIGH`
**Feature Under Test:** `CapaService.create()`
**Test File:** `service/CapaLifecycleTest.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-11` · **Constraint Ref:** `C6`
**Oracle Source:** **BR-216**: *"1 Incident = 1 CAPA (có thể gộp nhiều hành động khắc phục khác nhau trong cùng 1 bản ghi) — không tách nhiều CAPA song song trong cùng 1 Incident."* + §5.2 `uq_capa_one_per_incident` + §10 → `IEM-216`, HTTP **409** + TDS §13 `TC-U-014`

**Expected Result (PASS):**
- `INC-2026-014` đã có `CAPA-2026-033` → tạo CAPA thứ 2 → **409 `IEM-216`**, `details[0].message` gợi ý *"bổ sung hành động vào CAPA hiện có"*
- assert `message` nguyên văn theo bảng §10 *(v1.1 — MSG đã cấp)*; assert **`code`** và **409** bật bình thường
- Nhánh dương: `INC` chưa có CAPA → **201**, `status = PENDING_ADMIN_APPROVAL`, `message` = **MSG-049** nguyên văn `"Corrective actions plan submitted successfully for review."`

**Current Status:** 🔴 Not written

---

#### MF11-TC-014 — BR-095: CAPA sinh từ kiểm thực **không** dính unique index

**Severity:** `MEDIUM`
**Feature Under Test:** `CapaService.createFromInspection()` (consumer `InspectionStepFailed`)
**Test File:** `service/CapaLifecycleTest.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-11` · **Constraint Ref:** `C6`
**Oracle Source:** **BR-095/BR-097** (MF07 — trích chéo, ghi rõ nguồn) + **ADR-M11-4** *"Ngoại lệ"* + §5.2 `ck_capa_source CHECK ((incident_id IS NOT NULL) <> (inspection_id IS NOT NULL))` + `uq_capa_one_per_incident … WHERE incident_id IS NOT NULL` + TDS §13 `TC-U-015`

**Expected Result (PASS):**
- Tạo **2 CAPA** cùng `inspectionCode = INSP-20260630-L`, `incidentCode = null` → **cả hai thành công** (partial unique không bắt)
- Tạo CAPA có **cả hai** `incidentCode` và `inspectionCode` → **400** (vi phạm XOR `ck_capa_source`)
- Tạo CAPA **không có nguồn nào** → **400**
- CAPA từ kiểm thực khởi tạo ở `PENDING_ADMIN_APPROVAL` (§7.2), **không** tự mở sự cố (BR-097 — chỉ *gợi ý*)

**Current Status:** 🔴 Not written

---

#### MF11-TC-015 — BR-153/BR-216: duyệt CAPA sai trạng thái nguồn → 409

**Severity:** `HIGH`
**Feature Under Test:** `CapaService.approvePlan()` / `.rejectPlan()`
**Test File:** `service/CapaLifecycleTest.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-12` · **Constraint Ref:** `C6` · `AP-M11-04`
**Oracle Source:** **BR-153** (sửa 20/07: *"phê duyệt hoặc từ chối các hành động khắc phục **được đề xuất**, **trước khi** Service Caterers thực hiện"*) + **BR-216** bước 2 + **ADR-M11-4** + §10 → `IEM-216`, HTTP **409** + TDS §13 `TC-U-016`
**Kỹ thuật:** State Transition Testing — bảng đầy đủ 6 trạng thái × 6 hành động

**Expected Result (PASS):**
- Ma trận transition hợp lệ **duy nhất**: `DRAFT →(submit-for-approval)→ PENDING_ADMIN_APPROVAL →(approve)→ APPROVED_FOR_IMPLEMENTATION →(accept)→ IN_PROGRESS →(submit-evidence)→ SUBMITTED_EVIDENCE →(validate)→ COMPLETED`; nhánh phụ `PENDING_ADMIN_APPROVAL →(reject)→ DRAFT` và `SUBMITTED_EVIDENCE →(rework)→ IN_PROGRESS`
- **Mọi** cặp (trạng thái, hành động) ngoài bảng trên → **409 `IEM-216`**, trạng thái **không đổi**. Ví dụ điểm: `approve` khi đang `IN_PROGRESS` · `accept` khi đang `DRAFT` · `validate` khi đang `APPROVED_FOR_IMPLEMENTATION` · `submit-evidence` khi chưa `accept`
- `COMPLETED` là **trạng thái cuối** — mọi hành động tiếp theo → 409

**Current Status:** 🔴 Not written

---

#### MF11-TC-016 — BR-216: `reject` / `rework` thiếu lý do → 400

**Severity:** `MEDIUM`
**Feature Under Test:** `CapaService.rejectPlan()` / `.requestRework()`
**Test File:** `service/CapaLifecycleTest.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-12` · **Constraint Ref:** `C6`
**Oracle Source:** **BR-216** bước 2 (*"Không duyệt → về `DRAFT`, **bắt buộc lý do**"*) và bước 5 (*"yêu cầu làm lại kèm **lý do bắt buộc**"*) + UC-168 AF1 + §5.2 `ck_capa_reject` + §10 → `IEM-001`, HTTP **400** + TDS §13 `TC-U-017`

**Expected Result (PASS):**
- `reason ∈ {"", "  ", null}` → **400 `IEM-001`**, `message` = **MSG-138** nguyên văn `"Validation failed: one or more required fields are missing or invalid."` (mã này **có** MSG dùng chung ⇒ assert bật)
- `details[0].field = "reason"`; trạng thái CAPA **không đổi**

**Current Status:** 🔴 Not written

---

#### MF11-TC-017 — BR-216 b5: `rework` giữ nguyên kế hoạch đã duyệt

**Severity:** `MEDIUM`
**Feature Under Test:** `CapaService.requestRework()`
**Test File:** `service/CapaLifecycleTest.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-12` · **Constraint Ref:** `C6`
**Oracle Source:** **BR-216** bước 5: *"Chưa đạt → gửi lại **đúng phương án đã được duyệt** (**không cần trình School Administration duyệt lại** vì phương án không đổi)"* + UC-168 AF1 + TDS §13 `TC-U-018`

**Expected Result (PASS):**
- `SUBMITTED_EVIDENCE` + `rework(reason)` → `status = IN_PROGRESS` (**không** về `PENDING_ADMIN_APPROVAL`, **không** về `DRAFT`)
- `approvedAt` **không đổi**, `submittedForApprovalAt` **không đổi**, `description` **không đổi**
- `reworkReason` + `reworkRequestedAt` được ghi; event `CapaReworkRequested` phát 1 lần
- `incident_events` thêm mốc mới, **không** sửa mốc cũ (C4)

**Expected Result (FAIL — dấu hiệu lỗi):** đẩy về `PENDING_ADMIN_APPROVAL` ⇒ bắt BGH duyệt lại một kế hoạch **không hề đổi** — vòng lặp thủ tục mà BR-216 b5 viết ra để chặn.

**Current Status:** 🔴 Not written

---

#### MF11-TC-018 — BR-155/UC-168 EX1: còn HS chưa hồi phục → `IEM-155`, **KHÔNG** MSG-004

**Severity:** `CRITICAL`
**Feature Under Test:** `CapaService.validate()` · `IncidentClosureService.close()`
**Test File:** `service/IncidentClosureTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-13` · **Constraint Ref:** `TDS §17.1 C7` · `AP-M11-13` (Wrong MSG code)
**Oracle Source:** **BR-155** (*"sau khi **xác nhận hoàn thành** các hành động khắc phục"*) + §10 TDS → `IEM-155`, HTTP **409** + **CF-M11-15/L25** (*"**không** trả MSG-004 (sai ngữ nghĩa, sẽ khiến FE đá người dùng ra màn đăng nhập)"*) + TDS §13 `TC-U-019`

**Expected Result (PASS):**
- 1 HS `recovered = false` → `code = "IEM-155"`, HTTP **409**
- `details` là **mảng** liệt kê **từng HS** chưa hồi phục: `[{field:"HS-3A-001", message:"Nguyễn Minh Khang — chưa xác nhận hồi phục"}, …]`
- ⚠️ Assert **phủ định tường minh**: `$.error.code != "IEM-004"`, HTTP **≠ 401**, `message` **không chứa** `"Session expired"` — đây là điểm cả UC file lẫn §1.11 đang sai
- assert nội dung `message` = **MSG-153** nguyên văn, và **`error.details`** mang `{field, capaOpen, studentsNotRecovered}` (D2) *(v1.1 — đã cấp; **bỏ `@Disabled`**)*
- Nhánh dương: mọi HS `recovered = true` (kèm `recovered_at` — `ck_ias_recovered`) → cho phép

**Expected Result (FAIL — dấu hiệu lỗi):**
- Trả **MSG-004/401** ⇒ FE (theo `client.ts`) sẽ **logout người dùng** giữa lúc đang xử lý sự cố ATTP có trẻ nhập viện. Đây là lý do `IEM-155` được tạo ra thay vì mượn MSG-004

**Current Status:** 🔴 Not written

---

#### MF11-TC-019 — BR-155: chỉ School Administration được đóng sự cố

**Severity:** `CRITICAL`
**Feature Under Test:** `IncidentClosureService.close()`
**Test File:** `service/IncidentClosureTest.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-13` · **Constraint Ref:** `C7` · `AP-M11-05`
**Oracle Source:** **BR-155** nguyên văn: *"Chỉ **School Administration** mới có quyền **đóng (Close)** một sự cố sau khi xác nhận hoàn thành các hành động khắc phục."* + §16 TDS + **MSG-005** → `IEM-005`, HTTP **403** + TDS §13 `TC-U-020`

**Expected Result (PASS):**
- **Medical Staff** (người điều tra chính, có mọi quyền khác) → **403 `IEM-005`**, `message` = MSG-005 nguyên văn — đây là **separation of duty**, không phải thiếu quyền kỹ thuật
- `system-admin` cũng → **403** (§16: System Admin **không** có quyền ghi nghiệp vụ nào trong MF11)
- `school-admin` → **200**, `status = CLOSED`, `closedAt`/`closedById`/`closingStatement` được ghi (`ck_inc_closed`)
- Event `IncidentClosed` + mốc `CLOSED` trong `incident_events`

**Current Status:** 🔴 Not written

---

#### MF11-TC-020 — BR-155 + ràng buộc C1: còn CAPA chưa `COMPLETED` → 409 kèm danh sách

**Severity:** `CRITICAL`
**Feature Under Test:** `IncidentClosureService.close()` + `.closureReadiness()`
**Test File:** `service/IncidentClosureTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-13` · **Constraint Ref:** `C7` · ràng buộc **C1** `data_model.puml`
**Oracle Source:** ràng buộc **C1** (*"Sự cố chỉ đóng được khi mọi CAPA đã COMPLETED"* — *đã từng bị vi phạm ngay trong dữ liệu mẫu*, xem **L36**) + **BR-155** + §10 → `IEM-155`, HTTP **409** + TDS §13 `TC-U-021`
**Kỹ thuật:** Decision Table (3 điều kiện gate độc lập)

| # | CAPA | HS hồi phục | Lô cách ly (MF06) | Kết quả |
|---|---|---|---|---|
| 1 | `SUBMITTED_EVIDENCE` | đủ | trạng thái cuối | **409 `IEM-155`** + liệt kê CAPA |
| 2 | `COMPLETED` | còn 2 HS chưa | trạng thái cuối | **409 `IEM-155`** + liệt kê HS |
| 3 | `COMPLETED` | đủ | còn `QUARANTINED` | **409 `IEM-217`** (L21) |
| 4 | `COMPLETED` | đủ | `RETURNED_TO_SUPPLIER` | **200 CLOSED** |
| 5 | 2 điều kiện hỏng cùng lúc | — | — | **409**, `details` gộp **cả hai** nhóm |

**Expected Result (PASS):**
- Ca 1: `details[0] = {field:"CAPA-2026-030", message:"đang ở trạng thái SUBMITTED_EVIDENCE"}`; `incidents.status` giữ nguyên
- Ca 5: `details` **không** dừng ở lỗi đầu tiên — người dùng phải thấy **toàn bộ** việc còn thiếu trong 1 lần
- `GET /closure-readiness` trả **cùng danh sách** đó dưới dạng checklist (200) — FE dùng để hiện nút disabled kèm lý do

**Current Status:** 🔴 Not written

---

#### MF11-TC-021 — ADR-M11-8: `reportExported` khoá một chiều, ngoại lệ duy nhất là `close`

**Severity:** `CRITICAL`
**Feature Under Test:** guard read-only trên toàn bộ `IncidentInvestigationService` / `QuarantineService`
**Test File:** `service/ReportLockTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-14` · **Constraint Ref:** `TDS §17.1 C10` · `AP-M11-08` (Unlock exported report)
**Oracle Source:** **ADR-M11-8** (quyết định 18/07/2026, QĐ 2+5): *"sau khi 'Export & Submit Incident Report' thành công, **toàn bộ** hồ sơ chuyển read-only với **mọi** actor, **không có cơ chế mở khoá lại**"*; ngoại lệ *"**duy nhất**: `POST /incidents/{code}/close` … và ghi `incident_events`"* + §10 → `IEM-RO`, HTTP **409** + TDS §13 `TC-U-022`/`TC-U-023`
**Kỹ thuật:** Negative testing (quét **toàn bộ** hành động ghi)

**Test Steps:** với `reportExported = true`, gọi lần lượt: `assess`, `quarantine-batches`, `evidence`, `evidence/finalize`, `root-cause`, `assessment-draft`, `sample-retrieval`, `validate-capa`, `request-rework`, `report` (lần 2), `close`.

**Expected Result (PASS):**
- **10 hành động đầu** → **409 `IEM-RO`** với **mọi role**, kể cả `system-admin`
- **`close`** → **200** (ngoại lệ đúng ADR-M11-8: đóng sự cố là hành động **sau** khi hồ sơ khoá)
- Ghi `incident_events` **vẫn được phép** (append-only, luôn cho ghi) — mốc `CLOSED` xuất hiện bình thường
- Reflection/route-scan: **0** endpoint nào đặt `reportExported = false`
- assert `message` = **MSG-158** nguyên văn *(v1.1 — đã cấp; **bỏ `@Disabled`**)*

**Expected Result (FAIL — dấu hiệu lỗi):**
- Sinh endpoint mở khoá ⇒ **AP-M11-08**: hồ sơ đã nộp cơ quan chức năng bị sửa sau lưng — mất toàn bộ giá trị pháp lý của báo cáo (BR-154)
- Chặn luôn `close` ⇒ sự cố **không bao giờ đóng được** sau khi xuất báo cáo (deadlock nghiệp vụ)

**Current Status:** 🔴 Not written

---

#### MF11-TC-022 — BR-183/BR-211: lập biên bản vi phạm thiếu trường bắt buộc → 400

**Severity:** `HIGH`
**Feature Under Test:** `ViolationService.record()`
**Test File:** `service/ViolationRecordTest.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-16` · **Constraint Ref:** `C9`, `C8`
**Oracle Source:** **BR-183** (field bắt buộc: người phát hiện, mô tả, **ảnh bằng chứng**, `severityLevel`, `caterersRepresentativeId`) + **BR-211** (*"Trường dữ liệu: `locationOfIncident`, `occurredAt`, `caterersRepresentativeId`, `signedRecordPhotoUrl` (bắt buộc)"*) + **MSG-085** nguyên văn = `"Violation record submission blocked: required fields (description, evidence photo) are missing."` → `IEM-085`, HTTP **400** + TDS §13 `TC-U-024`

**Expected Result (PASS):**
- Parameterized thiếu từng field: `locationOfIncident` · `occurredAt` · `caterersRepresentativeId` · `description` · `evidenceFiles` rỗng → **400 `IEM-085`**, `message` = **MSG-085 nguyên văn** (mã này **có** MSG ⇒ assert bật)
- ⚠️ **Không** chặn vì thiếu `signedRecordPhotoUrl` ở bước **lưu** — BR-211: *"Việc lưu Biên bản Vi phạm **không bị chặn** bởi việc chưa có ảnh ký"* (gate nằm ở `resolution`, xem `TC-024`)
- `details` liệt kê **mọi** field thiếu, không chỉ field đầu

**Current Status:** 🔴 Not written

---

#### MF11-TC-023 — BR-187: nghi tái diễn đối chiếu theo **đại diện đơn vị**, cảnh báo **không chặn**

**Severity:** `HIGH`
**Feature Under Test:** `RepeatViolationDetector.check()`
**Test File:** `service/RepeatViolationDetectorTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-17` · **Constraint Ref:** `TDS §17.1 C9` · `AP-M11-07` (Individual sanction)
**Oracle Source:** **BR-187** (sửa 26/07/2026): *"trùng khớp với một Biên bản Vi phạm khác cùng mức **≥ MEDIUM** trong vòng **90 ngày** … cơ chế **best-effort**, **không tự động escalate**"*; *"đổi căn cứ đối chiếu từ `staffName` … sang `caterersRepresentativeId`/đơn vị Nhà thầu"* + **MSG-088** nguyên văn (warning) + TDS §13 `TC-U-025`/`TC-U-026`
**Kỹ thuật:** Decision Table + Boundary (90 ngày)

| # | Đại diện | Mức biên bản cũ | Khoảng cách | `repeatSuspect` | Cảnh báo |
|---|---|---|---|---|---|
| 1 | trùng `U-0003` | MEDIUM | 89 ngày | `true` | MSG-088 |
| 2 | trùng `U-0003` | MEDIUM | **91 ngày** | `false` | — |
| 3 | trùng `U-0003` | **LOW** | 10 ngày | `false` | — |
| 4 | **khác** đại diện, **cùng `staffName`** | HIGH | 10 ngày | `false` | — |
| 5 | trùng `U-0003` | HIGH | 90 ngày (đúng biên) | `true` | MSG-088 |

**Expected Result (PASS):**
- Mọi ca → HTTP **201** (cảnh báo **không chặn** lưu — BR-187 best-effort)
- Ca có cảnh báo: `warnings[0].code = "MSG-088"`, `message` = **MSG-088 nguyên văn** *"Possible repeat violation detected: staff name matches a prior violation within the lookback window. Manual verification required."* ⚠️ **Giữ nguyên văn dù nội dung nói "staff name"** — CF-M11-4/L14: sửa MSG là việc của Huy, test **không** tự dịch lại
- Ca 4 chứng minh **không** đối chiếu theo `staffName`
- **Không** tự escalate: `severity` của biên bản mới **không** bị nâng, **không** event nào ngoài `ViolationRecorded`
- Request DTO **không có** field `staffName`/`staffPosition`/`disciplinaryLevel` (kiểm bằng grep (b) ở boilerplate)

**Current Status:** 🔴 Not written

---

#### MF11-TC-024 — BR-211: gate ảnh biên bản đã ký (chặn cứng) + chưa xuất PDF → chặn tải bản ký

**Severity:** `CRITICAL`
**Feature Under Test:** `ViolationResolutionService.submit()` · `ViolationPdfService.uploadSignedRecord()`
**Test File:** `service/ViolationResolutionTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-18` · **Constraint Ref:** `TDS §17.1 C8` · `AP-M11-06` (Signature gate bypass)
**Oracle Source:** **BR-211** nguyên văn: *"**Gate bắt buộc**: … nút 'Đóng biên bản' (LOW) và 'Gửi School Administration phê duyệt' (MEDIUM/HIGH) **bị vô hiệu hóa** cho đến khi biên bản đã có `signedRecordPhotoUrl`"* + **MSG-095** nguyên văn = `"Cannot close/submit this violation resolution: the signed 3-party record photo has not been uploaded yet. Please print, collect signatures, and upload the signed record first."` → `IEM-095`, HTTP **409** + §10 → `IEM-211` (chưa xuất PDF) + TDS §13 `TC-U-027`/`TC-U-028`

**Test Steps:**
1. `VIO` MEDIUM, `pdfExported = false` → gọi `POST /violations/{code}/signed-record` → assert 409 `IEM-211`.
2. `VIO` MEDIUM, `pdfExported = true`, **0** file `SIGNED_RECORD` → gọi `POST /resolution` → assert 409 `IEM-095`.
3. Cùng `VIO`, **lưu/sửa nội dung biên bản** → assert **vẫn cho phép** (200).
4. Thêm 1 file `SIGNED_RECORD` → gọi lại `POST /resolution` → assert 200.

**Expected Result (PASS):**
- Bước 1: `code = "IEM-211"`, HTTP **409**; assert `message` = **MSG-156** nguyên văn *(v1.1 — đã cấp; ⚠️ **khác MSG-095**: MSG-095 chặn *đóng/gửi duyệt khi thiếu ảnh ký*, MSG-156 chặn *tải ảnh ký khi chưa xuất PDF*)*
- Bước 2: `code = "IEM-095"`, HTTP **409**, `message` = **MSG-095 nguyên văn** (mã này **có** MSG ⇒ assert bật), `details[0] = {field:"signedRecordFiles", message:"0 tệp — cần ≥ 1 (BR-211)"}`
- Bước 3: **200** — chứng minh gate **không** chặn việc lưu (BR-211 đoạn 3). Đây là nửa dễ bị hiện thực sai nhất
- Bước 4: **200**, `resolution = PENDING_APPROVAL` (MEDIUM — BR-186), `message` = **MSG-087** nguyên văn

**Expected Result (FAIL — dấu hiệu lỗi):**
- Chặn cả việc **lưu** biên bản ⇒ mất dữ liệu số phục vụ cảnh báo tái diễn (BR-187) và audit (BR-188) trong lúc chờ ký giấy
- Không chặn `resolution` ⇒ hồ sơ có chế tài hợp đồng với nhà thầu mà **không có chữ ký tay nào** — mất giá trị pháp lý (BR-211 là quyết định người dùng 27/07 sau khi đảo ngược cơ chế xác nhận điện tử)

**Current Status:** 🔴 Not written

---

#### MF11-TC-025 — BR-186: LOW → Y tế tự đóng; MEDIUM/HIGH → bắt buộc School Admin

**Severity:** `HIGH`
**Feature Under Test:** `ViolationResolutionService.submit()` / `.approve()`
**Test File:** `service/ViolationResolutionTest.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-19` · **Constraint Ref:** `C9`
**Oracle Source:** **BR-186** nguyên văn: *"Vi phạm mức **LOW**: **Medical Staff** được phép trực tiếp phê duyệt/đóng … Vi phạm mức **MEDIUM** hoặc **HIGH**: bắt buộc **School Administration** phê duyệt hoặc từ chối trước khi đóng"* + **MSG-089** (LOW đóng) / **MSG-087** (MEDIUM-HIGH gửi duyệt) + §10 → `IEM-186`, HTTP **403** + TDS §13 `TC-U-029`/`TC-U-030`/`TC-U-031`
**Kỹ thuật:** Decision Table (severity × actor)

**Expected Result (PASS):**
- `LOW` + Medical Staff → `resolution = APPROVED`, `status = CLOSED`, `message` = **MSG-089** nguyên văn `"Violation resolution approved. Disciplinary action applied."`
- `MEDIUM`/`HIGH` + Medical Staff `submit` → `resolution = PENDING_APPROVAL`, `status = OPEN`, `message` = **MSG-087** nguyên văn
- `MEDIUM`/`HIGH` + Medical Staff cố `approve` → **403 `IEM-186`**, assert `message` = **MSG-157** nguyên văn *(v1.1 — đã cấp)*
- `MEDIUM`/`HIGH` + School Admin `approve` → `APPROVED`/`CLOSED`, `message` = **MSG-089**
- Mọi nhánh `resolution ≠ PENDING` đều có `action_taken` + `handled_by_id` + **`unit_sanction_level`** (`ck_vio_resolution`) — thiếu `unitSanctionLevel` → **400**
- `unitSanctionLevel` chỉ nhận 4 giá trị BR-185: `WRITTEN_REMINDER`, `CONTRACT_WARNING`, `CONTRACT_PENALTY`, `TERMINATION_PROPOSAL`; **không** nhận `disciplinaryLevel` cũ (`SUSPENSION`, `WRITTEN_WARNING`)

**Current Status:** 🔴 Not written

---

#### MF11-TC-026 — BR-190: báo cáo override abuse là **dẫn xuất** theo tuần ISO × đơn vị

**Severity:** `MEDIUM`
**Feature Under Test:** `OverrideAbuseReportService.weekly()`
**Test File:** `service/OverrideAbuseReportTest.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-20` · **Constraint Ref:** `C12` · `AP-M11-10`
**Oracle Source:** **BR-190** (sửa 26/07): *"tự động tổng hợp **hàng tuần** … thể hiện dưới dạng Biên bản Vi phạm (BR-183, **cấp đơn vị Nhà thầu**) với `violationType = OVERRIDE_ABUSE`"* + **ADR-M11-6** (dẫn xuất, không bảng) + TDS §13 `TC-U-032` + **CF-M11-10/L20**

**Expected Result (PASS):**
- 2 `VIO` `OVERRIDE_ABUSE` cùng đại diện `U-0003` trong **tuần 2026-W27** → **1 dòng**, `overrideCount = 2`, `threshold = 3`
- 1 `VIO` khác đại diện cùng tuần → dòng **riêng** (nhóm theo **đơn vị/đại diện**, không theo `staffName` — seed cũ `OVR-W27-01` khoá theo `staffName` là sai, L20)
- Bản ghi tuần khác → **không** lọt vào kết quả `?week=2026-W27`
- Response giữ shape `PagedResponse` 5 field (§2b-1)
- Job `OverrideAbuseWeeklyJob` sinh **`ViolationRecord`**, **không** sinh dòng báo cáo riêng; event `OverrideAbuseWeeklyCompiled` phát 1 lần/tuần

**Current Status:** 🔴 Not written

---

#### MF11-TC-027 — C1: hợp đồng phân trang + `search` chỉ trên `SEARCH_FIELDS`

**Severity:** `MEDIUM`
**Feature Under Test:** `IncidentSpec` / `ViolationSpec` / `SourceReportSpec` + `PagedResponse.of()`
**Test File:** `service/IncidentSpecTest.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-21` · **Constraint Ref:** `TDS §17.1 C1`
**Oracle Source:** Team Plan **§2b-1** (5 field bất biến), **§2b-3 bước 5** (*"search chỉ chạy trên `SEARCH_FIELDS` … không OR toàn bộ cột String"*), **§2b-6** (edge cases) + TDS §9.1 (danh sách `SEARCH_FIELDS` đã sửa theo **CF-M11-8/L18**) + TDS §13 `TC-U-033`/`TC-U-034`

**Expected Result (PASS):**
- Tập rỗng sau lọc → `{items:[], total:0, page:1, pageSize:8, pageCount:1}` — **200**, không 404
- `page` vượt `pageCount` → clamp về `pageCount`; `pageSize ≤ 0` → clamp về 1
- `search="Gà rang"` khớp `suspectedMealText` (trong `SEARCH_FIELDS`) → có kết quả
- `search="<một chuỗi chỉ có trong `firstAidNotes`>"` → **0 kết quả** (cột **ngoài** `SEARCH_FIELDS`)
- ⚠️ Assert **tường minh** rằng `description`/`reporterName` **không** được dùng cho `incidents` (2 tên này ở Team Plan §2b-3 **không tồn tại** trên entity — L18): build spec với chúng → `IllegalArgumentException` ở tầng test, **không** sinh SQL trên cột không có
- `not:status=CLOSED` lọc phủ định đúng; `mealPrefix`/`step`/`stepStatus` **không** ảnh hưởng kết quả

**Current Status:** 🔴 Not written

---

### 4.2. SECURITY TEST CASES

> `@SpringBootTest` + MockMvc + JWT giả lập **7 role** (BR-007). Nguồn: **§16 Authorization Matrix** (7 role × 30 nhóm endpoint) + **ADR-M11-10** (phạm vi dẫn xuất) + **C14** (PDPD).
> ⚠️ Nhóm **"gọi API trực tiếp bỏ qua UI"** (`TC-SEC-002/003/004/005`) là **bắt buộc** theo prompt §2c-3b: mock đang chặn mọi gate bằng `toast.error`/`disabled` ở client (CF-M11-9/L19) ⇒ nếu server không chặn thì **toàn bộ luật ATTP lách được bằng một lệnh `curl`**.
> ⚠️ Mọi từ chối phạm vi trả **403**, **không 404** — §16: *"tránh lộ sự tồn tại hồ sơ chứa dữ liệu sức khoẻ trẻ em"*.

---

#### MF11-TC-SEC-001 — Ma trận RBAC 7 role × endpoint ghi (parameterized)

**Severity:** `CRITICAL` · **Test File:** `web/AuthorizationMatrixTest.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-06`, `COND-13`, `COND-19` · **Constraint Ref:** `C14`, `C15`
**Oracle Source:** **§16 TDS** (bảng đầy đủ) + **BR-007** (RBAC 7 role) + **BR-147/153/155/186** + **MSG-005** → `IEM-005`, HTTP **403**

**Test Steps:** parameterized `(role × endpoint)` cho **12 endpoint ghi** trọng yếu: `/source-reports`, `/incident-records/{c}/open-investigation`, `/incidents/{c}/assess`, `/quarantine-batches`, `/evidence`, `/root-cause`, `/report`, `/close`, `/corrective-actions`, `/corrective-actions/{c}/approve`, `/corrective-actions/{c}/submit-evidence`, `/violations/{c}/resolution/approve`.

**Expected Result (PASS):**
- Mỗi ô `❌` trong §16 → **403 `IEM-005`**, `message` = **MSG-005 nguyên văn**, `details` = `[]` (**không** tiết lộ lý do chi tiết)
- Mỗi ô `✅` → **không** 403 (2xx hoặc lỗi nghiệp vụ khác)
- **System Admin ❌ ở mọi endpoint ghi** — §16: *"chỉ đọc để vận hành (least privilege với dữ liệu sức khoẻ)"*
- `supplier` (NCC nguyên liệu) → **403 ở mọi endpoint MF11**, kể cả đọc

**Current Status:** 🔴 Not written

---

#### MF11-TC-SEC-002 — Bỏ qua UI: Service Caterers gọi thẳng `POST /incidents/{code}/assess`

**Severity:** `CRITICAL` · **Test File:** `web/DirectApiBypassTest.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-06` · **Constraint Ref:** `C14` · `AP-M11-05`
**Oracle Source:** **BR-147** + §16 + **CF-M11-9/L19** (*"mock dùng `PUT` generic ⇒ không kiểm tra nào chạy phía server"*) + TDS §13 `TC-E2E-004`

**Expected Result (PASS):**
- JWT `service-caterers` (`FX-022`) → **403 `IEM-005`**; `severity` trong DB **không đổi**; **không** event nào phát
- Bên **bị điều tra** không được tự phân loại mức độ sự cố của chính mình — đây là xung đột lợi ích trực tiếp, không chỉ là phân quyền hình thức

**Current Status:** 🔴 Not written

---

#### MF11-TC-SEC-003 — Bỏ qua UI: Service Caterers tự duyệt CAPA của chính mình

**Severity:** `CRITICAL` · **Test File:** `web/DirectApiBypassTest.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-12` · **Constraint Ref:** `C6`, `C14` · `AP-M11-04`
**Oracle Source:** **BR-153** (*"**School Administration** phê duyệt … **trước khi** Service Caterers thực hiện"*) + **BR-216** (3 vai, 3 quyền rời) + §16 + TDS §13 `TC-E2E-006`

**Expected Result (PASS):**
- `service-caterers` gọi `POST /corrective-actions/CAPA-2026-033/approve` → **403 `IEM-005`**
- `medical-staff` (người **soạn** CAPA) gọi `approve` → cũng **403** — separation of duty: soạn ≠ duyệt ≠ thực hiện
- Chỉ `school-admin` → 200
- DB: `status` và `approvedAt` **không đổi** ở 2 ca đầu

**Current Status:** 🔴 Not written

---

#### MF11-TC-SEC-004 — Bỏ qua UI: `PUT` generic đặt `status=COMPLETED` phải **không tồn tại**

**Severity:** `CRITICAL` · **Test File:** `web/DirectApiBypassTest.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-12` · **Constraint Ref:** `C6` · `AP-M11-04` (Generic status merge)
**Oracle Source:** **ADR-M11-4** (*"chuyển trạng thái qua **endpoint nghiệp vụ**, không `PUT` generic"*) + **CF-M11-9**: mock cho phép `PUT /:resource/:id` merge mọi field ⇒ *"Service Caterers **có thể tự đặt** `status=COMPLETED` nếu gọi API trực tiếp"*

**Expected Result (PASS):**
- `PUT /api/v1/corrective-actions/CAPA-2026-033` body `{"status":"COMPLETED"}` → **404** hoặc **405** (endpoint **không tồn tại**), **không** 200
- `PATCH /api/v1/incidents/{code}/assessment-draft` (endpoint soạn thảo được phép) với body chứa `status`/`severity`/`reportExported` → các field nhạy cảm bị **bỏ qua**, DB không đổi
- `PUT /api/v1/incidents/{code}` với `{"status":"CLOSED"}` → **404/405**
- `PUT /api/v1/violations/{code}` với `{"resolution":"APPROVED"}` → **404/405**

**Current Status:** 🔴 Not written

---

#### MF11-TC-SEC-005 — Bỏ qua UI: Medical Staff gọi thẳng `POST /incidents/{code}/close`

**Severity:** `CRITICAL` · **Test File:** `web/DirectApiBypassTest.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-13` · **Constraint Ref:** `C7` · `AP-M11-05`
**Oracle Source:** **BR-155** + §16 + TDS §13 `TC-E2E-005`

**Expected Result (PASS):** **403 `IEM-005`**; `incidents.status` không đổi; **không** mốc `CLOSED` nào vào `incident_events` (nếu có mốc mà status không đổi ⇒ sổ sự kiện nói dối, đúng lỗi của seed `INC-2026-013` — **L36**).

**Current Status:** 🔴 Not written

---

#### MF11-TC-SEC-006 — PDPD/UC-120: projection Service Caterers **không** chứa dữ liệu sức khoẻ

**Severity:** `CRITICAL` · **Test File:** `web/IncidentProjectionTest.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-22` · **Constraint Ref:** `TDS §17.1 C14` · `AP-M11-12` (Health data leak)
**Oracle Source:** **§16 TDS**: *"Service Caterers thấy `INC` được gán nhưng response **loại bỏ** `symptoms`, `firstAidNotes`, `clinicalRiskAssessment`, `investigationLabResult` và `affectedStudents[].studentName` — chỉ còn **số lượng** HS bị ảnh hưởng. Cắt ở **tầng projection**, không ở FE."* + **UC-120** + **PDPD (NĐ 13/2023/NĐ-CP)** + TDS §13 `TC-E2E-003` + **CF-M11-21(e)/L31**

**Expected Result (PASS):**
- `GET /incidents/INC-2026-014` với JWT `service-caterers` → **200**, JSON **không có** (assert `jsonPath().doesNotExist()`): `symptoms`, `firstAidNotes`, `clinicalRiskAssessment`, `investigationLabResult`, `investigationLabResultFiles`, `affectedStudents[].studentName`, `affectedStudents[].studentId`
- **Có**: `affectedStudentCount = 4`, `title`, `mealDate`, `severity`, `status`, `quarantineBatches[]`, CAPA của đơn vị mình
- Cùng sự cố với JWT `medical-staff` → **có đủ** các field trên (đối chứng dương — chứng minh việc cắt là **theo vai**, không phải field bị bỏ quên)
- Response body **không chứa** tên HS nào: assert regex phủ định trên toàn body
- ⚠️ Ô *"Sơ cứu đã thực hiện"* của FE sẽ **trắng** với Caterers — **có chủ ý** (L31e), FE phải bỏ ô đó khỏi nhánh Caterers

**Expected Result (FAIL — dấu hiệu lỗi):** cắt ở FE thay vì server ⇒ dữ liệu vẫn nằm trong response, đọc bằng DevTools/`curl` là thấy — bên **bị điều tra** đọc được hồ sơ sức khoẻ của nạn nhân. **Reject cứng (AP-M11-12).**

**Current Status:** 🔴 Not written

---

#### MF11-TC-SEC-007 — Teacher `Own class`: phạm vi đọc + projection cắt theo lớp

**Severity:** `CRITICAL` · **Test File:** `web/TeacherScopeTest.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-22`, `COND-23` · **Constraint Ref:** `C15`, `C14`
**Oracle Source:** **ADR-M11-10** (bảng phạm vi: *"`Incident` có **≥ 1** `incident_affected_students.class_name` = lớp đó"*) + §16 ghi chú Teacher (v1.1): *"giữ `symptoms` … nhưng **loại bỏ** `clinicalRiskAssessment`, `investigationLabResult`, `rootCause`, và `affectedStudents[]` **chỉ giữ HS thuộc lớp mình**"* + **CF-M11-17/L27**

**Expected Result (PASS):**
- GVCN 3A (`U-0004`) đọc `INC-2026-014` (có HS 3A **và** 3B) → **200**; `affectedStudents[]` **chỉ 2 HS lớp 3A**, **không** có `HS-3B-001`/`HS-3B-002`
- Cùng response: **không có** `clinicalRiskAssessment`, `investigationLabResult`, `rootCause`; **có** `symptoms` (cần để nhận biết dấu hiệu trong lớp)
- GVCN 4A (`U-0010`) đọc `INC-2026-014` (không HS lớp 4A) → **403 `IEM-005`**, **không phải 404**
- `GET /source-reports` với GVCN 3A → chỉ `RPT` có `className = "3A"`; `RPT-2026-033` (lớp 4A) **không** xuất hiện, và `total` phản ánh **sau** lọc phạm vi (không phải tổng toàn trường)

**Current Status:** 🔴 Not written

---

#### MF11-TC-SEC-008 — Regression guard: Teacher **KHÔNG** bị chặn khỏi `source-reports` / `incidents`

**Severity:** `HIGH` · **Test File:** `web/TeacherScopeTest.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-23` · **Constraint Ref:** `C15` · `AP-M11-15` (Blocked role regression)
**Oracle Source:** **CF-M11-17/L27** — bản v1.0 của TDS chặn nhầm Teacher, trong khi `teacher/incidents/page.tsx` dựng khối *"Lịch sử báo cáo"* bằng `DataTable resource="source-reports"` và `teacher/dashboard/page.tsx` đọc cả `incidents` lẫn `source-reports`

**Expected Result (PASS):**
- `GET /api/v1/source-reports?className=3A&status=PENDING_TRIAGE` với JWT Teacher → **200** (**không** 403)
- `GET /api/v1/incidents` với JWT Teacher → **200**, danh sách đã lọc theo `EXISTS` lớp chủ nhiệm
- `GET /api/v1/incidents/{code}/events` với Teacher trong phạm vi → **200**
- ⚠️ Test này tồn tại để **chặn hồi quy**: nếu ai đó code theo §16 bản v1.0, **2 màn hình Teacher sẽ 403 trắng** mà không ai biết cho tới lúc demo

**Current Status:** 🔴 Not written

---

#### MF11-TC-SEC-009 — Parent chỉ **tạo** `RPT`, không đọc `INC`/`RPT`

**Severity:** `HIGH` · **Test File:** `web/ParentScopeTest.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-03`, `COND-23` · **Constraint Ref:** `C15`
**Oracle Source:** **§16 TDS**: *"Parent **không có** quyền đọc `INC`/`RPT` nào (xác nhận: `parent/safety-concern` chỉ có `useCreate`, không có màn xem lại)"* + **BR-201**

**Expected Result (PASS):**
- `POST /source-reports` cho HS đã liên kết → **201**
- `GET /source-reports`, `GET /source-reports/{code}` (kể cả `RPT` **do chính mình gửi**), `GET /incidents`, `GET /incidents/{code}`, `GET /violations` → **403 `IEM-005`**
- Ghi rõ: đây **không** phải thiếu sót UX — phụ huynh không được đọc hàng chờ triage chứa triệu chứng của con nhà khác

**Current Status:** 🔴 Not written

---

#### MF11-TC-SEC-010 — Danh tính & phạm vi lấy từ JWT; sự cố khác trường → 403 (không 404)

**Severity:** `CRITICAL` · **Test File:** `web/JwtScopeTest.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-23` · **Constraint Ref:** `C14`, `C15` · `AP-AI-006` (Trust FE identity)
**Oracle Source:** §16 chú thích (*"❌ = từ chối (**403 IEM-005**, không phải 404 — tránh lộ sự tồn tại hồ sơ chứa dữ liệu sức khoẻ trẻ em)"*) + **§17.2 mục 15** (*"Lọc ở repository, KHÔNG lọc ở client"*) + TDS §13 `TC-E2E-008`

**Expected Result (PASS):**
- `POST /incidents/{code}/assess` với body chứa `"schoolId":"SCH-002"`, `"actorId":"U-0001"`, `"role":"school-admin"` → 3 field bị **bỏ qua hoàn toàn**; service dùng JWT
- JWT `schoolId = SCH-002` đọc `INC-2026-014` (thuộc `SCH-001`) → **403 `IEM-005`** (không 404, không 200)
- Sự cố **không tồn tại** thật → **404 `IEM-404`** + `message` = **MSG-137** nguyên văn `"Resource not found."` — phân biệt được 2 ca này là yêu cầu bắt buộc

**Current Status:** 🔴 Not written

---

#### MF11-TC-SEC-011 — Rate limit `POST /incidents/{code}/close` (15/min) → 429

**Severity:** `MEDIUM` · **Test File:** `web/RateLimitTest.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** — · **Constraint Ref:** §9.1 (bảng Rate Limit)
**Oracle Source:** §9.1 TDS (`POST /close` = **15/min**) + **MSG-141** nguyên văn = `"Too many requests. Please try again later."` (v1.8 — *"dùng chung mọi module … MF11 đóng sự cố"*) + TDS §13 `TC-E2E-010`

**Expected Result (PASS):**
- Request thứ **16** trong 1 phút → HTTP **429**, `message` = **MSG-141 nguyên văn**
- ⚠️ **Không** assert `error.code`: MF11 **chưa được cấp mã cục bộ cho 429** (§10 không có dòng nào cho 429). Ghi rõ *"thiếu mã cục bộ — đề nghị `IEM-429`"*, chờ Huy/Nam. Đây là cách xử lý **khoan dung** đúng policy (không tự đặt mã mới)
- Header `Retry-After` có mặt

**Current Status:** 🔴 Not written

---

#### MF11-TC-SEC-012 — BR-170/BR-188: audit bất biến + IP cho mọi thao tác hồ sơ vi phạm

**Severity:** `HIGH` · **Test File:** `web/ViolationAuditTest.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-25` · **Constraint Ref:** §4.3 TDS (Audit)
**Oracle Source:** **BR-188** (*"Mọi Biên bản Vi phạm … và Biên bản Xử lý Sai phạm phải được ghi vào audit log **bất biến**"*) + **BR-170** + §14.2 TDS (truy vấn đối soát *"mọi thao tác hồ sơ vi phạm phải có audit kèm IP"*). ⚠️ **BR-188 không có ở §1.11** — ghi rõ nguồn `business_rules.md` §18 (**CF-M11-3/L13**)

**Expected Result (PASS):**
- Sau `POST /violations`, `POST /{code}/resolution`, `POST /{code}/resolution/approve`: mỗi thao tác sinh **đúng 1** dòng audit có `actor`, **`ip`**, `at`, `before`, `after`
- Truy vấn §14.2 (*audit thiếu IP*) trả **rỗng**
- `UPDATE`/`DELETE` trực tiếp lên bảng audit → **không đổi dữ liệu** (bất biến)
- Audit **không** chứa PII thô của HS (chỉ mã) — PDPD §4.3

**Current Status:** 🔴 Not written

---

#### MF11-TC-SEC-013 — D10 + BR-013: upload qua `StorageService`, giới hạn 10MB / định dạng

**Severity:** `HIGH` · **Test File:** `web/EvidenceUploadTest.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-26` · **Constraint Ref:** **D10**
**Oracle Source:** **D10** (`00_Common_Conventions.md`, Approved 31/07 — nêu đích danh *"MF11 ảnh hiện trường sự cố"*) + **BR-013** + **MSG-008** nguyên văn `"File upload failed: File size exceeds the maximum limit of 10MB."` + **MSG-009** nguyên văn `"File upload failed: Unsupported file type. Only PDF, JPG, and PNG are allowed."` + TDS §13 `TC-E2E-011`

**Expected Result (PASS):**
- 3 luồng upload (`/incidents/{c}/evidence`, `/corrective-actions/{c}/submit-evidence`, `/violations/{c}/signed-record`) đều gọi `StorageService.store(key, file)`; `verify(storageService, times(1))`
- Key theo khuôn: `"incident/INC-2026-014-<n>.jpg"`, `"violation/VIO-2026-008-signed.jpg"`
- File **10MB + 1 byte** → **400**, `message` = **MSG-008 nguyên văn**; file `.exe`/`.docx` → **400**, **MSG-009 nguyên văn**
- Grep: **0** lời gọi `MultipartFile.transferTo(` hoặc `S3Client` trong `com.sfms.mf11` (D10 — *"không tự triển khai I/O"*)
- URL file bằng chứng ký có **hạn < 15 phút** (§4.3 TDS)

**Current Status:** 🔴 Not written

---

#### MF11-TC-SEC-014 — Phạm vi `Liên đới` của Service Caterers (⚠️ khoan dung, chờ CF-M11-18)

**Severity:** `HIGH` · **Test File:** `web/CatererScopeTest.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-23` · **Constraint Ref:** `C15` · `AP-M11-14` (Owner column smell)
**Oracle Source:** **ADR-M11-10** — ⚠️ **Trạng thái: "Chờ Nam chốt"** (CF-M11-18/L28)

**Expected Result (PASS) — nhánh khoan dung (bật):**
- Sự cố có **CAPA giao cho `CAT-001`** → Caterers `U-0003` đọc được (**200**, projection hẹp theo `TC-SEC-006`) — vế này **đúng ở cả hai phương án** Nam có thể chọn
- Sự cố **không liên đới gì** với `CAT-001` (không CAPA, không lô của đơn vị) → **403**
- DB: **không** cột `caterer_id`/`owner_id`/`assigned_to` trên bảng `incidents` (assert `information_schema.columns`) — **AP-M11-14**
- Lọc ở **repository** (`EXISTS`), không trả hết rồi lọc client: assert `total` **nhỏ hơn** tổng sự cố toàn trường

**Nhánh thứ hai — ✅ BẬT (v1.1, `M11-D19`: Nam chốt GIỮ cả 2 vế `OR`, bỏ `@Disabled`). Vế "lô cách ly thuộc NCC/đơn vị":**
- Sự cố **chỉ** liên đới qua **lô cách ly** thuộc `CAT-001` (không có CAPA) → 200 hay 403? **Chưa có người chốt** ⇒ không khoá test vào một bên

**Current Status:** 🔴 Not written

---

#### MF11-TC-SEC-015 — Bề mặt API **không** có đường nhả lô / mở khoá hồ sơ

**Severity:** `CRITICAL` · **Test File:** `web/ForbiddenSurfaceTest.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-08`, `COND-14` · **Constraint Ref:** `C5`, `C10` · `AP-M11-03`, `AP-M11-08`
**Oracle Source:** **BR-217** (bỏ hẳn mở khoá) + **ADR-M11-3** + **ADR-M11-8** (*"**không có** cơ chế mở khoá lại"*) + **§17.2 mục 5, 10**

**Expected Result (PASS):** quét `RequestMappingHandlerMapping` của toàn ứng dụng — **0** route nào khớp:
- `(?i)/incidents/.*/(release|unquarantine)` · `(?i)/quarantine-batches/.*/(release|delete)`
- `(?i)/incidents/.*/(unlock|reopen)` · bất kỳ route nào nhận body chứa `reportExported`
- `DELETE /api/v1/incidents/{code}` · `DELETE /api/v1/incident-events/**`
- Gọi thử `POST /api/v1/incidents/INC-2026-014/release-batches` → **404**

**Current Status:** 🔴 Not written

---

#### MF11-TC-SEC-016 — System Admin: đọc để vận hành, **không** ghi nghiệp vụ

**Severity:** `MEDIUM` · **Test File:** `web/AuthorizationMatrixTest.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-23` · **Constraint Ref:** `C14`
**Oracle Source:** **§16 TDS** ghi chú: *"**System Admin** không có quyền ghi nghiệp vụ nào trong MF11 — chỉ đọc để vận hành (đúng tinh thần least privilege với dữ liệu sức khoẻ)"*

**Expected Result (PASS):**
- `GET /incidents`, `/incidents/{code}`, `/violations`, `/corrective-actions` với `system-admin` → **200** (`✅ All`)
- **Mọi** `POST` của MF11 với `system-admin` → **403 `IEM-005`**, kể cả `close`
- Sau `reportExported = true`: System Admin cũng nhận **409 `IEM-RO`** ở endpoint ghi (TDS §13 `TC-E2E-009` — *"với mọi role, kể cả System Admin"*)

**Current Status:** 🔴 Not written

---

### 4.3. INTEGRATION TEST CASES

> `@SpringBootTest(webEnvironment = RANDOM_PORT)` + **Testcontainers PostgreSQL 16** (`FX-026`) + Flyway `V1` + `V{n}__seed_mf11_incident.sql` + MockMvc.
> Nhóm này kiểm **bất biến ở tầng DB** (RULE/CHECK/UNIQUE), **transactional outbox**, **port sang module khác**, và **12 luật `semantic-audit.ts`**.

---

#### MF11-TC-INT-001 — ADR-M11-2: `incident_events` append-only ở **tầng DB**

**Severity:** `CRITICAL` · **Test File:** `integration/IncidentEventsAppendOnlyIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-15` · **Constraint Ref:** `TDS §17.1 C4` · `AP-M11-02` (Frozen record mutated)
**Oracle Source:** **ADR-M11-2** (*"APPEND-ONLY — chặn UPDATE/DELETE ở tầng DB bằng RULE"*) + §5.2 `CREATE RULE incident_events_no_update/no_delete … DO INSTEAD NOTHING` + §11.3 bước 7 (*"xác minh RULE append-only có hiệu lực"*) + TDS §13 `TC-INT-004`

**Test Steps:** `UPDATE incident_events SET description='x' WHERE code='IEV-014-01'` · `DELETE FROM incident_events WHERE code='IEV-014-01'` (chạy bằng JdbcTemplate, **không** qua JPA).

**Expected Result (PASS):**
- Sau cả hai lệnh: `description` **nguyên vẹn**, số dòng **không đổi** (RULE `DO INSTEAD NOTHING` — lệnh "thành công" nhưng không tác dụng)
- Không có setter public trên `IncidentEvent` (reflection); repository **không** expose `delete*`
- ⚠️ Kiểm **cả hai tầng**: JPA có thể chặn, nhưng nếu RULE thiếu thì một script vận hành/migration lỗi vẫn xoá được lịch sử điều tra 10 năm

**Current Status:** 🔴 Not written

---

#### MF11-TC-INT-002 — BR-217: `iqb_no_delete` + bảng nhả lô lịch sử **không có dòng mới**

**Severity:** `CRITICAL` · **Test File:** `integration/QuarantineOneWayIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-08` · **Constraint Ref:** `C5` · `AP-M11-03`
**Oracle Source:** §5.2 `CREATE RULE iqb_no_delete AS ON DELETE TO incident_quarantine_batches DO INSTEAD NOTHING` + **BR-217** + **CF-M11-2/L12** + TDS §13 `TC-INT-005`

**Expected Result (PASS):**
- `DELETE FROM incident_quarantine_batches WHERE batch_code='BATCH-MP-8820'` → dòng **vẫn còn**
- Bảng `incident_quarantine_release_log`: **0 dòng** sau khi chạy toàn bộ suite (`FX-031`) — backend **không ghi mới**, chỉ đọc dữ liệu lịch sử
- `information_schema.columns`: `incident_quarantine_batches` **không có** cột `released_at`/`released_by`

**Current Status:** 🔴 Not written

---

#### MF11-TC-INT-003 — BR-155 + C1: gate đóng sự cố ở tầng API → DB không đổi

**Severity:** `CRITICAL` · **Test File:** `integration/IncidentClosureIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-13` · **Constraint Ref:** `C7`, ràng buộc **C1**
**Oracle Source:** ràng buộc **C1** `data_model.puml` + **BR-155** + §5.2 `ck_inc_closed` + TDS §13 `TC-INT-002`

**Expected Result (PASS):**
- `FX-005` (`INC-2026-013` + `CAPA-2026-030` `SUBMITTED_EVIDENCE`) → `POST /close` bằng School Admin → **409 `IEM-155`**
- `SELECT status, closed_at FROM incidents WHERE code='INC-2026-013'` → vẫn `CAPA_APPROVED`, `closed_at IS NULL`
- **Không** mốc `CLOSED` nào vào `incident_events` (chống lặp lại lỗi seed **L36**)
- Sau khi đẩy CAPA → `COMPLETED`, HS `recovered=true`, lô ở trạng thái cuối → `POST /close` → **200**, `ck_inc_closed` thoả (3 cột đóng NOT NULL)

**Current Status:** 🔴 Not written

---

#### MF11-TC-INT-004 — Đồng thời: 2 request `assess` song song → 1 thành công, 1 mốc `SEVERITY_SET`

**Severity:** `HIGH` · **Test File:** `integration/ConcurrentAssessIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-06`, `COND-15` · **Constraint Ref:** `C4`
**Oracle Source:** **ADR-M11-2** (1 hành động = 1 mốc) + §8.2 `findByCodeForUpdate()` (`PESSIMISTIC_WRITE`) + TDS §13 `TC-INT-001`

**Expected Result (PASS):**
- 2 thread cùng gọi `POST /assess` trên `INC-2026-014`: **1** trả 200, **1** trả **409**
- `SELECT count(*) FROM incident_events WHERE incident_id=… AND type='SEVERITY_SET'` = **1**
- **Đúng 1** `HighSeverityAlertRaised` trong outbox — không gửi cảnh báo khẩn 2 lần tới cơ quan y tế địa phương

**Current Status:** 🔴 Not written

---

#### MF11-TC-INT-005 — BR-148: outbox **cùng transaction** — rollback thì event biến mất

**Severity:** `CRITICAL` · **Test File:** `integration/EmergencyOutboxIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-07` · **Constraint Ref:** `C13` · `AP-M11-11`
**Oracle Source:** **ADR-M11-9** (transactional outbox) + **BR-148** + §4.1 (SLA dispatch **< 60s**) + TDS §13 `TC-INT-008`

**Expected Result (PASS):**
- `assess(HIGH)` thành công → `outbox_events` có **1** `HighSeverityAlertRaised` **cùng transaction** với `incidents.severity`
- Ép rollback (lỗi sau khi ghi state) → **cả hai** biến mất: `severity` chưa đổi **và** outbox rỗng (không có event "mồ côi" gửi cảnh báo cho một phân loại chưa từng xảy ra)
- Đo `dispatchedAt − occurredAt` (clock giả) → **< 60s** (§14.2 truy vấn BR-148)

**Current Status:** 🔴 Not written

---

#### MF11-TC-INT-006 — ADR-M11-7: cách ly đi **qua port**, không UPDATE bảng MF06

**Severity:** `CRITICAL` · **Test File:** `integration/QuarantinePortIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-08`, `COND-24` · **Constraint Ref:** `C11` · `AP-M11-09` (Cross-module entity import)
**Oracle Source:** **ADR-M11-7** (*"8 port đọc-only … `@ManyToOne` **chỉ** trong nội bộ package"*) + **CF-M11-19/L29** (FE đang `useUpdate("inventory")` ghi thẳng bảng kho) + §7.1 `BatchQuarantined` + TDS §13 `TC-INT-006`

**Expected Result (PASS):**
- `POST /quarantine-batches` → `verify(inventoryBatchPort).markQuarantined("BATCH-MP-8820", incidentCode, reason)`; event `BatchQuarantined` trong outbox (consumer **MF06** + **MF10**)
- 1 dòng `incident_quarantine_batches` + 1 mốc `QUARANTINE` — **cùng transaction**
- `quarantined_by_id` = **người thật** (`U-0002`), actor **không** phải `"Hệ thống"` (**L39**)
- Grep: **0** `@ManyToOne` trỏ `InventoryBatch`/`Student`/`Dish`/`FoodSample`/`InspectionSession`/`Partner` trong `com.sfms.mf11`; **0** câu SQL nào của MF11 `UPDATE` bảng của MF06

**Current Status:** 🔴 Not written

---

#### MF11-TC-INT-007 — BR-151: bản đồ truy xuất `COMPILING → READY ≤ 4h`, scope = lô đã cách ly

**Severity:** `HIGH` · **Test File:** `integration/TraceabilityMapIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-10` · **Constraint Ref:** `C12` · **L23**
**Oracle Source:** **BR-151** (*"Trong vòng **4 giờ** kể từ khi báo cáo sự cố, hệ thống **tự động biên soạn bản đồ truy xuất**"*) + footnote 15/07 (**đề xuất** scope theo `quarantineBatchIds[]`, *"chưa khoá"*) + §4.1 TDS + TDS §13 `TC-INT-009`

**Expected Result (PASS):**
- Ngay sau `open-investigation`: `traceability_map_status = COMPILING`
- Chạy job với clock giả tiến 4h → `READY`, event `TraceabilityMapCompiled` phát 1 lần
- `GET /traceability-map` → `scope = "quarantineBatches"`; số node = số lô **đã cách ly** (1), **không** tràn ra mọi lô của bữa
- Quá 4h vẫn `COMPILING` → test **FAIL** (vi phạm SLA BR-151)
- Nhánh *"scope toàn sự cố"* → **LOẠI BỎ** (v1.1, `M11-D20`: đội dự án chốt `scope = quarantineBatches`). Không giữ `@Disabled` — nhánh này không còn là phương án nào cả

**Current Status:** 🔴 Not written

---

#### MF11-TC-INT-008 — BR-216: `uq_capa_one_per_incident` + `ck_capa_evidence` ở tầng DB

**Severity:** `HIGH` · **Test File:** `integration/CapaConstraintIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-11` · **Constraint Ref:** `C6`
**Oracle Source:** §5.2 (`uq_capa_one_per_incident … WHERE incident_id IS NOT NULL`, `ck_capa_source`, `ck_capa_approved`, `ck_capa_evidence`) + **BR-216**

**Expected Result (PASS):**
- `INSERT` CAPA thứ 2 cùng `incident_id` **bằng JdbcTemplate** (bỏ qua service) → **`DataIntegrityViolationException`** — bất biến nằm ở **DB**, không chỉ service
- 2 CAPA cùng `inspection_id`, `incident_id = NULL` → **thành công** (partial index)
- `status='SUBMITTED_EVIDENCE'` với `evidence_photos = 0` → vi phạm `ck_capa_evidence` (BR-216 b4)
- `approved_at < submitted_for_approval_at` → vi phạm `ck_capa_approved`
- Cột `deadline` của CAPA tạo qua API → **luôn `NULL`** (**L31d** — deprecated, read-only)

**Current Status:** 🔴 Not written

---

#### MF11-TC-INT-009 — ADR-M11-5: sinh mã nghiệp vụ có khoá, 100 request đồng thời không trùng

**Severity:** `HIGH` · **Test File:** `integration/CodeSequenceIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-27` · **Constraint Ref:** `C12` / **D1**
**Oracle Source:** **ADR-M11-5** (*"dùng `INSERT … RETURNING` trên bảng `code_sequences` thay vì đọc-rồi-ghi (tránh trùng mã khi 2 request song song)"*) + §5.2 `uq_vio_code` + TDS §13 `TC-INT-015`

**Expected Result (PASS):**
- 100 `POST /violations` đồng thời → **100 mã `VIO-2026-xxx` khác nhau**, 0 lỗi unique
- Mã đúng khuôn `^VIO-\d{4}-\d{3}$`, đánh số **theo năm, theo trường**
- Dãy số **liên tục** trong phạm vi test (không nhảy cóc do rollback đọc-rồi-ghi)

**Current Status:** 🔴 Not written

---

#### MF11-TC-INT-010 — D1: JSON `id` là mã nghiệp vụ, **không** lộ UUID

**Severity:** `CRITICAL` · **Test File:** `integration/ConventionD1IT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** — · **Constraint Ref:** **D1**
**Oracle Source:** **D1** (`00_Common_Conventions.md`) + **ADR-M11-5** (nguồn của D1) + §5.2 `uq_*_code`

**Expected Result (PASS):**
- `GET /incidents/INC-2026-014` → `$.id == "INC-2026-014"`; **không** field nào trong body khớp regex UUID
- Áp cho **cả 5 resource** + mảng con (`quarantineBatches[].batchCode`, `affectedStudents[].studentId`, `events[].id`)
- DB assertion dùng `WHERE code = ?`; UUID nội bộ **không** xuất hiện ở API
- Path param dùng `{code}`; gọi bằng UUID → **404**

**Current Status:** 🔴 Not written

---

#### MF11-TC-INT-011 — D4: mọi field ngày trả **ISO-8601**, nhận khoan dung 2 dạng

**Severity:** `CRITICAL` · **Test File:** `integration/ConventionD4IT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** — · **Constraint Ref:** **D4** — ghi đè `ADR-M11-5` + `C12` + `§17.2 mục 12` (**L4**)
**Oracle Source:** **D4** (*"Ngày `"2026-07-29"` / Ngày giờ `"2026-07-29T02:15:00Z"`"*; mục *"⚠️ Ghi đè ADR-M11-5"*: *"MF08, MF09, MF11 phải **bỏ `@JsonFormat`**"*)

**Expected Result (PASS):**
- Response của **mọi** endpoint MF11: `mealDate`/`date` khớp `^\d{4}-\d{2}-\d{2}$`; `reportedAt`/`occurredAt`/`quarantinedAt`/`closedAt`/`submittedForApprovalAt`/`approvedAt`/`evidenceSubmittedAt`/`at`/`uploadedAt`/`pdfGeneratedAt` khớp ISO instant
- **0** giá trị nào khớp `^\d{2}/\d{2}/\d{4}` trong toàn bộ body (quét đệ quy JSON)
- Grep source: **0** `@JsonFormat` trong `com.sfms.mf11`
- **Nhận khoan dung:** `POST /source-reports` với `"mealDate":"30/06/2026"` (mock đang gửi) → **201**, response trả `"2026-06-30"`

**Current Status:** 🔴 Not written

---

#### MF11-TC-INT-012 — D2/D6/D7: envelope lỗi, enum, và ca `severity` NULL ↔ `"PENDING"`

**Severity:** `HIGH` · **Test File:** `integration/ConventionD2D6IT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** — · **Constraint Ref:** **D2**, **D6**, **D7** · **L7**, **L22**
**Oracle Source:** **D2** (envelope 3 field) + **D6** (SCREAMING_SNAKE) + **D7** (message tiếng Anh) + **CF-M11-12** (JSON trả `"PENDING"` khi `severity IS NULL`)

**Expected Result (PASS):**
- Mọi error response có đủ `$.error.code`, `$.error.message`, `$.error.details`; `details` **là mảng**, **không bao giờ `null`**
- Mọi giá trị enum khớp `^[A-Z][A-Z0-9_]*$` — **ngoại lệ có chủ đích**: `ViolationRecord.type` là nhãn hiển thị `"Quy trình"` (§5.2 `DEFAULT`), không phải enum
- `INC` vừa mở: `$.severity == "PENDING"` **và** `SELECT severity` = **NULL** — hai điều cùng đúng
- 6 mã **có MSG thật** (`IEM-005`, `IEM-023`, `IEM-085`, `IEM-095`, `IEM-404`→MSG-137, `IEM-001`→MSG-138): assert `message` **nguyên văn tiếng Anh**
- **10 mã cục bộ** (`IEM-114/147/149/149B/155/186/211/216/217/RO`): ✅ **assert ĐỦ CẢ BA** — `code` + HTTP + **`message` nguyên văn** (**MSG-149…158**, `error_message.md` **v1.11**). *(v1.0 ghi: chỉ assert `code` + HTTP, block `message` gắn `@Disabled("Thiếu oracle — chờ Huy cấp MSG")` — **L7/L35 đã đóng 04/08**.)*

**Current Status:** 🔴 Not written

---

#### MF11-TC-INT-013 — D9: `BaseEntity` audit tự điền, không đụng cột reserved

**Severity:** `MEDIUM` · **Test File:** `integration/ConventionD9IT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** — · **Constraint Ref:** **D9**
**Oracle Source:** **D9** (`created_by`/`updated_by` là cột reserved `VARCHAR(180)`, tự điền qua `AuditorAware`; MF11 thuộc nhóm *"không cần sửa"*)

**Expected Result (PASS):**
- **13** entity MF11 `extends BaseEntity`; reflection: **không** entity nào tự khai lại `createdBy`/`updatedBy`
- Sau `POST /violations` với JWT `U-0002`: `created_by` = **email actor** (`hant@dtd.edu.vn`), service **không** set tay
- 5 cột nghiệp vụ giữ tên riêng: `created_by_id`, `recorded_by_id`, `quarantined_by_id`, `handled_by_id`, `closed_by_id`
- Job nền (`OverrideAbuseWeeklyJob`) → `created_by = "system"`

**Current Status:** 🔴 Not written

---

#### MF11-TC-INT-014 — `semantic-audit.ts` §9: 3 luật về báo cáo nguồn

**Severity:** `HIGH` · **Test File:** `integration/SemanticAuditIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-04` · **Constraint Ref:** §4.2 TDS (bất biến)
**Oracle Source:** `semantic-audit.ts` §9 — **tên nguyên văn**: `BAO_CAO_TRUOC_BUA_AN` · `SU_CO_TRO_BAO_CAO_KHONG_TON_TAI` (add-only) · `BAO_CAO_NGUON_KHAC_BUA` (`rd < md || rd > md + 7*24h`) + §5.2 `ck_rpt_after_meal`, `ck_inc_after_meal` + TDS §13 `TC-INT-014`

**Expected Result (PASS):**
- `INSERT` `INC` với `reported_at < meal_date` → vi phạm `ck_inc_after_meal` (**DB chặn**) — port của `BAO_CAO_TRUOC_BUA_AN`
- Mọi `RPT` trong `sourceReportIds` của `INC` **tồn tại** — port `SU_CO_TRO_BAO_CAO_KHONG_TON_TAI`
- `RPT.reported_at ∈ [meal_date, meal_date + 7 ngày]` — port `BAO_CAO_NGUON_KHAC_BUA`, **boundary đúng 7 ngày**
- Chạy trên **toàn bộ dữ liệu sau seed**, không chỉ dòng test tạo ra

**Current Status:** 🔴 Not written

---

#### MF11-TC-INT-015 — `semantic-audit.ts` §9: 3 luật về dòng thời gian

**Severity:** `HIGH` · **Test File:** `integration/SemanticAuditIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-15` · **Constraint Ref:** `C4`
**Oracle Source:** `semantic-audit.ts` §9 — **tên nguyên văn**: `MOC_TRO_SU_CO_KHONG_TON_TAI` (add-only) · `MOC_TRUOC_KHI_SU_CO_XAY_RA` (`t < base`, `base = mealDate ?? reportedAt`) · `DONG_THOI_GIAN_LUI` (`t < prev`) + TDS §13 `TC-INT-003`

**Expected Result (PASS):**
- `FX-004` (`INC-2026-012` đã đóng): `GET /incidents/{code}/events` → **5 mốc**, thứ tự **tăng dần**, mốc đầu **≥** `meal_date`
- **0** dòng `incident_events` trỏ `incident_id` không tồn tại (FK + port `MOC_TRO_SU_CO_KHONG_TON_TAI`)
- Ghi mốc có `at` **trước** `meal_date` qua service → **400**; qua JdbcTemplate → truy vấn đối soát §14.1 **bắt được** ⇒ test FAIL có thông báo rõ
- ⚠️ Tên luật copy **nguyên văn script**, không lấy từ TDS

**Current Status:** 🔴 Not written

---

#### MF11-TC-INT-016 — `semantic-audit.ts` §9: 2 luật về CAPA

**Severity:** `MEDIUM` · **Test File:** `integration/SemanticAuditIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-24` · **Constraint Ref:** `C11`
**Oracle Source:** `semantic-audit.ts` §9 — `CAPA_TRUOC_SU_CO` · `CAPA_GIAO_NCC_KHONG_TON_TAI` + TDS §13 `TC-INT-012`/`TC-INT-013`

**Expected Result (PASS):**
- CAPA có `submittedForApprovalAt` **trước** `INC.reportedAt` → **400** (không lập kế hoạch khắc phục trước khi sự cố được báo)
- `assigneeSupplierCode` không tra ra `Partner` qua `PartnerPort` → **400**
- ⚠️ **Ca hồi quy L32:** dùng `"PTN-0007"` (mã trong ví dụ §9.2) → **400** vì không tồn tại; dùng `"CAT-001"` → **201**. Test này **chính là** thứ bắt được lỗi ví dụ trong TDS

**Current Status:** 🔴 Not written

---

#### MF11-TC-INT-017 — `semantic-audit.ts` §9/§9b: 2 luật cách ly + bất biến thay `DONG_SU_CO_MA_CON_LO_KHOA`

**Severity:** `HIGH` · **Test File:** `integration/SemanticAuditIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-08`, `COND-13` · **Constraint Ref:** `C5`, `C7` · **L21**
**Oracle Source:** `semantic-audit.ts` §9 `CACH_LY_LO_KHONG_LIEN_QUAN_BUA` · §9b `CACH_LY_KHONG_RO_SU_CO` + **BR-217** (bất biến mới thay luật `DONG_SU_CO_MA_CON_LO_KHOA` — **CF-M11-11/L21**) + §10 → `IEM-217`

**Expected Result (PASS):**
- Cách ly lô **không có phiếu xuất** cho bữa của sự cố → cảnh báo dữ liệu (query §14.1 bắt được); nghiệp vụ **vẫn cho** cách ly (Medical Staff có thể có lý do thực địa) nhưng phải ghi vết
- Mọi dòng `incident_quarantine_batches` trỏ `incident_id` **tồn tại** — port `CACH_LY_KHONG_RO_SU_CO`
- ⚠️ **`DONG_SU_CO_MA_CON_LO_KHOA` KHÔNG port.** Thay bằng: `close` khi lô chưa ở trạng thái cuối bên MF06 → **409 `IEM-217`**; khi lô `RETURNED_TO_SUPPLIER` → **200**, và **không** xoá `incident_quarantine_batches` (hồ sơ lịch sử)

**Current Status:** 🔴 Not written

---

#### MF11-TC-INT-018 — `semantic-audit.ts` §4/§5: mẫu lưu gắn sự cố đúng khung

**Severity:** `MEDIUM` · **Test File:** `integration/SampleLinkIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-24` · **Constraint Ref:** `C11`
**Oracle Source:** `semantic-audit.ts` §4 `MAU_NGOAI_KHUNG_SU_CO` (*"`c > md + 24h` hoặc `c < md − 72h`"*) · §5 `PHIEU_KHAC_SU_CO_VOI_MAU`, `PHIEU_TRO_MAU_KHONG_TON_TAI` (add-only) + **BR-113** (MF08, trích chéo) + TDS §13 `TC-INT-010`/`TC-INT-011`

**Expected Result (PASS):**
- `SMP-20260630-03` (`collectedAt = 2026-06-30T03:25Z`) gắn `INC-2026-014` (`mealDate = 2026-06-30`) → **hợp lệ**
- Mẫu lấy sau `mealDate + 24h1m` → cảnh báo `MAU_NGOAI_KHUNG_SU_CO`
- Phiếu `LABREQ-2026-001` và mẫu của nó **cùng `incidentId`** — port `PHIEU_KHAC_SU_CO_VOI_MAU`
- `GET /incidents/{code}/samples` và `/lab-requests` đọc **qua `FoodSamplePort`**, không JOIN bảng MF08
- ⚠️ Khung 24h/72h ở đây là của **script audit** (mẫu lưu), khác cửa sổ **24h** chọn món của BR-149 — không trộn 2 con số

**Current Status:** 🔴 Not written

---

#### MF11-TC-INT-019 — CF-M11-19: đổi đường đọc sự kiện, `incident-events` phẳng **không tồn tại**

**Severity:** `HIGH` · **Test File:** `integration/EventRouteIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-15` · **Constraint Ref:** `C4` · **L29**
**Oracle Source:** **ADR-M11-2** (*"sổ sự kiện chỉ do service ghi trong cùng transaction"*) + §9.1 ghi chú v1.1 (*"backend **không** mở resource `incident-events` phẳng vì như vậy là để client tải sổ sự kiện của mọi sự cố trong trường"*)

**Expected Result (PASS):**
- `POST /api/v1/incident-events` → **404/405** (FE `useAppendIncidentEvent` phải bỏ — F1 §11.2b)
- `GET /api/v1/incident-events?pageSize=200` → **404**
- `GET /api/v1/incidents/INC-2026-014/events` → **200**, chỉ sự kiện của sự cố đó, lọc **ở repository**
- Teacher ngoài phạm vi gọi endpoint trên → **403**

**Current Status:** 🔴 Not written

---

#### MF11-TC-INT-020 — §2b: hợp đồng list/filter/date-range trên 5 resource

**Severity:** `MEDIUM` · **Test File:** `integration/PagingContractIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-21` · **Constraint Ref:** `C1`
**Oracle Source:** Team Plan **§2b-1/2b-2/2b-5/2b-6** + TDS §9.1 (query params MF11) + **L41**

**Expected Result (PASS):**
- 5 `GET` danh sách đều trả **đúng 5 field**, không wrapper
- `dateFrom`/`dateTo` lọc trên `reported_at` (INC/RPT) và `date` (VIO)
- `status`, `severity`, `violationType`, `not:status=CLOSED`, `selfReported=true` lọc đúng
- `mealPrefix`/`step`/`stepStatus` gửi kèm → **không** làm sai tập kết quả (khoan dung, không 400)
- `GET /violations/summary?dateFrom=&dateTo=` kỳ **rỗng** → **200** với các số `0`, **không** MSG-097 (**L41** — MSG-097 thuộc MF13)

**Current Status:** 🔴 Not written

---

#### MF11-TC-INT-021 — ADR-M11-6: không bảng `override_abuse`; `?week=` nhận cả 2 dạng

**Severity:** `MEDIUM` · **Test File:** `integration/OverrideAbuseIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-20` · **Constraint Ref:** `C12` · `AP-M11-10`
**Oracle Source:** **ADR-M11-6** + **CF-M11-10/L20** + §9.1 ghi chú v1.1 về tham số `week` (*"Backend **nhận cả hai** … "* — **giải pháp tạm**)

**Expected Result (PASS):**
- `information_schema.tables`: **không có** `override_abuse`, **không có** bảng bản đồ truy xuất
- `?week=2026-W27` → 200; `?week=Tuần 27 (29/06–05/07)` (chuỗi FE đang gửi) → **200 cùng kết quả**
- `?week=` sai định dạng hoàn toàn → **400 `IEM-001`** + MSG-138
- Ghi 1 dòng: sau khi Minh sửa FE sang ISO, **bỏ nhánh parse nhãn** — test nhánh nhãn chuyển `@Disabled`

**Current Status:** 🔴 Not written

---

#### MF11-TC-INT-022 — Trục xương sống: bản đồ truy xuất nối bằng **ID** qua 4 module

**Severity:** `CRITICAL` · **Test File:** `integration/TraceabilitySpineIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-10`, `COND-24` · **Constraint Ref:** `C11` · **L33**
**Oracle Source:** Team Plan **§3** (*"Trục xương sống (P1) phải thông … nối bằng **ID**, không phải tên"*) + **BR-151** + §9.2 `traceability-map` + TDS §13 `TC-INT-016`

**Expected Result (PASS):**
- `GET /incidents/INC-2026-014/traceability-map` → node của `BATCH-MP-8820` có đủ: `supplierId = "SUP-003"` (**không** `PTN-0003` — **L32**), `supplierName`, `receivingSessionId = "RCV-20260629-02"`, `deliveryDate = "2026-06-29"` (ISO), `inspection.sessionId = "INSP-20260630-L"` + `step2 = "FAILED"` + `coreTempC = 68`, `samples[0].sampleId = "SMP-20260630-03"`, **`studentsWhoAte = 4`**
- `studentsWhoAte` lấy qua **`ServingDishPort`** của MF09 bằng **`dishId`**, không bằng tên món
- ⚠️ **Ca chặn (L33):** nếu `DishQueryPort.byCode()` trả rỗng cho mọi mã (vì MF05 **chưa cấp `Dish.code`**), test **FAIL với thông báo rõ**: *"trục truy xuất đang nối bằng TÊN — chờ MF05 cấp `Dish.code`"*. **Không** nới assert sang so tên

**Current Status:** 🔴 Not written

---

#### MF11-TC-INT-023 — Bất biến toàn cục sau seed (bắt lỗi seed L36/L37)

**Severity:** `CRITICAL` · **Test File:** `integration/SeedInvariantIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-04`, `COND-13`, `COND-18` · **Constraint Ref:** §4.2 TDS (bảng bất biến)
**Oracle Source:** §4.2 TDS (11 bất biến) + ràng buộc **C1** + **BR-211** + §5.2 (`ck_inc_closed`, `ck_vio_resolution`) + **L36**, **L37**
**Kỹ thuật:** Property-based (áp cho **mọi** dòng, không chỉ dòng test chạm tới)

**Test Steps:** chạy **ngay sau khi Flyway seed xong**, trước mọi test khác.

**Expected Result (PASS):** 6 truy vấn dưới đây đều trả **0 dòng**:

```sql
-- (1) C1/BR-155: đóng sự cố mà CAPA chưa COMPLETED
SELECT i.code FROM incidents i JOIN corrective_actions c ON c.incident_id = i.id
WHERE i.status = 'CLOSED' AND c.status <> 'COMPLETED';

-- (2) L36: sổ sự kiện nói "đã đóng" nhưng cột status chưa đóng
SELECT i.code FROM incidents i JOIN incident_events e ON e.incident_id = i.id
WHERE e.type = 'CLOSED' AND i.status <> 'CLOSED';

-- (3) ADR-M11-1: INC không trỏ REC
SELECT code FROM incidents WHERE incident_record_id IS NULL;

-- (4) BR-211: đã xử lý / gửi duyệt mà chưa có ảnh biên bản đã ký
SELECT v.code FROM violation_records v
WHERE v.resolution IN ('APPROVED','PENDING_APPROVAL')
  AND NOT EXISTS (SELECT 1 FROM violation_files f
                  WHERE f.violation_id = v.id AND f.kind = 'SIGNED_RECORD');

-- (5) BR-185: đã xử lý mà thiếu chế tài đơn vị / người xử lý
SELECT code FROM violation_records
WHERE resolution <> 'PENDING'
  AND (action_taken IS NULL OR handled_by_id IS NULL OR unit_sanction_level IS NULL);

-- (6) BR-186: Medical Staff duyệt biên bản MEDIUM/HIGH
SELECT v.code FROM violation_records v JOIN user_accounts u ON u.id = v.handled_by_id
WHERE v.severity IN ('MEDIUM','HIGH') AND v.resolution = 'APPROVED' AND u.role = 'medical-staff';
```

**Expected Result (FAIL — dấu hiệu lỗi):**
- Truy vấn (2) trả `INC-2026-013` ⇒ seed đang chép nguyên dữ liệu mock **tự mâu thuẫn** (**L36**)
- Truy vấn (4)/(5) trả `VIO-2026-004/005/006/009` ⇒ seed vi phạm gate BR-211 và `ck_vio_resolution` (**L37**) — nghĩa là **hồ sơ chế tài nhà thầu không có chữ ký nào** ngay trong dữ liệu demo

> **Phương án sửa đã chốt ở `M11-D18`** *(v1.1 — trước đây TC này để ngỏ "không tự chọn, Mạnh quyết")*: bỏ `IEV-013-05` *(không đẩy `CAPA-2026-030` lên `COMPLETED` — sổ sự kiện append-only mà mang event không phản ánh trạng thái thật là hỏng chính giá trị pháp lý của sổ)*; hạ 4 dòng `VIO` về `resolution = PENDING` *(không bịa `unit_sanction_level` + ảnh ký không tồn tại — seed là dữ liệu minh hoạ, bịa bằng chứng ký để lách CHECK là dạy sai người đọc)*. **Mạnh vẫn veto được khi review `V21`.**

**Current Status:** 🔴 Not written

---

#### MF11-TC-030 — **D12**: gửi field **DẪN XUẤT** và field **đúng tên sai KIỂU** ⇒ DB **không đổi** *(mới v1.1 — bắt buộc theo workflow §7 Bước 1 việc 3)*

**Severity:** `CRITICAL` · **Test File:** `integration/DerivedFieldWriteGuardIT.java` · **TDD Phase:** 🔴 RED
**Constraint Ref:** **D12** (`02_Module_Completion_Workflow.md` §6) · **M11-D16** · `C10` · **L19, L31(d)**
**Oracle Source:** **D12** (*"Read DTO ≠ Write DTO, nhưng key JSON phải khớp hai chiều"*) · **ADR-M11-8** (`reportExported` khoá một chiều) · **BR-187** (`repeatSuspect` do server tự dò — CF-M11-16) · **BR-216** (`deadline` deprecated — CF-M11-21(d)). Mẫu: `TestSpec-MF05` **TC-027**

> **Bẫy mà TC này bắt:** Jackson **bỏ qua im lặng** field không khớp DTO ⇒ request trả **200 mà không lưu gì**. Không lỗi, không cảnh báo, **form báo thành công**. Đã xảy ra thật ở MF03 (`PATCH /students/{code}` với `{"class":"4A"}`).
> Biến thể thứ hai **nguy hiểm hơn vì review bằng mắt không thấy**: **cùng tên, khác KIỂU** — `symptoms` là **mảng** ở response nhưng mock FE gửi lên **chuỗi**; hai bên *trông như khớp*.
> MF11 nhiều field dẫn xuất hơn mức trung bình vì **mọi chuyển trạng thái đi qua endpoint nghiệp vụ** (L19/CF-M11-9): nếu `PATCH` generic nhận `status`/`severity` thì **toàn bộ gate BR-153/155/186/211/149 bị vòng qua** bằng một request.

**Test Steps:**
1. `PATCH /api/v1/incidents/INC-2026-014/assessment-draft` *(endpoint soạn thảo — endpoint "khoan dung" nhất của MF11)* với body chứa **toàn bộ** field dẫn xuất `M11-D16`:
   `{"severity":"LOW","status":"CLOSED","traceabilityMapStatus":"READY","reportExported":true,"affectedStudents":[],"events":[],"quarantineBatches":[]}`
2. `PATCH /api/v1/corrective-actions/CAPA-2026-033` với `{"status":"COMPLETED","deadline":"2026-08-10"}`
3. `POST /api/v1/violations` với `{"repeatSuspect":true,"pdfExported":true,"pdfVersion":9,"staffName":"Nguyễn Văn X","staffPosition":"Bếp trưởng","disciplinaryLevel":"Cảnh cáo", …các field hợp lệ…}`
4. **Sai KIỂU:** `PATCH /api/v1/incidents/INC-2026-014/assessment-draft` với `{"symptoms":"đau bụng, nôn"}` *(chuỗi thay vì mảng)* và `{"affectedStudents":"2"}`

**Expected Result (PASS):**
- Bước 1–3: HTTP **200/201** *(hoặc **400 `IEM-001`** nếu chọn `FAIL_ON_UNKNOWN_PROPERTIES` — **cả hai đều đạt**, miễn DB không đổi; ghi rõ Mạnh chọn cách nào)*, và **DB KHÔNG ĐỔI** ở mọi cột dẫn xuất:
  `incidents.severity = 'HIGH'` *(giá trị seed, **không** thành `LOW`)* · `status = 'CAPA_APPROVED'` · `traceability_map_status = 'READY'` · `report_exported = false`
  `corrective_actions.status = 'APPROVED_FOR_IMPLEMENTATION'` · `deadline IS NULL` *(**L31(d)** — CAPA tạo mới luôn `null`)*
  `violation_records.repeat_suspect` = **giá trị server tự dò** *(BR-187: `U-0003` có tiền lệ ≥ MEDIUM trong 90 ngày ⇒ `true` **vì server dò ra**, không phải vì client gửi — đổi tài khoản sang đại diện chưa có tiền lệ phải ra `false` dù client vẫn gửi `true`)* · `pdf_exported = false` · `pdf_version = 0` · `staff_name`/`staff_position`/`disciplinary_level` **đều `NULL`**
- Bước 4: **400 `IEM-001`** *(MSG-138)*, `error.details` là **mảng** (D2) chỉ đúng tên field sai kiểu; `incidents.symptoms` giữ nguyên mảng seed
- **0 dòng** `incident_events` được ghi thêm trong toàn bộ TC *(không thao tác nghiệp vụ nào thực sự xảy ra)*
- Assert **cấu trúc DTO**, không chỉ hành vi: `grep` request-DTO của MF11 **không khai** 12 field ở `M11-D16`

**Current Status:** 🔴 Not written

---

#### MF11-TC-INT-024 — **`M11-D08`**: 3 port MF11 nợ module khác đã có bean thật *(mới v1.1)*

**Severity:** `HIGH` · **Test File:** `integration/ExposedPortWiringIT.java` · **TDD Phase:** 🔴 RED
**Constraint Ref:** **M11-D08** · `M07-D07`/`M07-D08` · `M08-D08` · TDS §8.4
**Oracle Source:** `TDS-MF07_Backend.md` `M07-D07`/`M07-D08` · `com/sfms/mf08/port/IncidentStatusPortStub.java` Javadoc · `mf07/service/InspectionServiceImpl:316`

> **Vì sao cần TC riêng cho một việc "chỉ là wiring":** cả 3 thứ dưới đây **không làm test nào đỏ** nếu bỏ sót — chúng chỉ `log.warn`. MF08 sẽ chạy tiếp với stub *im lặng không khoá nghiệp vụ*, và **BR-095 vẫn không tạo CAPA**. Đây đúng loại lỗi mà workflow §2.1 gọi là *"nổ vào mặt người khác chứ không phải người viết"*.

**Expected Result (PASS):**
- `ApplicationContext` có **đúng 1** bean kiểu `com.sfms.mf08.port.IncidentStatusPort`, và **không phải** `IncidentStatusPortStub` — file stub **đã bị xoá** *(`grep -r IncidentStatusPortStub src/` → **0**; để lại 2 bean cùng type ⇒ app **không khởi động**, stub cố ý **không** dùng `@ConditionalOnMissingBean`)*
- `incidentStatusPort.hasOpenIncidentLinked(INC-2026-014)` → **`true`** *(stub luôn trả `false`)*; `windowOf()` trả cửa sổ tính từ `occurredAt` **thật** *(`2026-06-30T04:15Z`)*, không phải quanh `now()`
- Ghi một bước kiểm thực **TRƯỢT** qua API MF07 ⇒ **1 dòng `corrective_actions` mới** với `inspection_id` trỏ đúng phiên, `incident_id IS NULL` *(`ck_capa_source`)*; gọi lại **cùng** `(inspectionId, step)` ⇒ **vẫn 1 dòng** *(idempotency `M07-D08`)*; log **không còn** chuỗi `"CapaPort chưa tồn tại"`
- `violationQueryPort.bySourceInspection(...)` trả biên bản có `source_inspection_id` khớp

**Current Status:** 🔴 Not written

---

### 4.4. CONTRACT TEST CASES — OpenAPI `/v3/api-docs` (**D8**)

> ⏳ **D8 đang chờ Nam approve** nhưng vẫn làm (không xung đột D1–D7). Nhóm này là **cổng máy kiểm** thay cho checklist tay: nó chặn lệch D1/D2/D4/D6 quay lại qua **file type FE sinh tự động** (`review_fix_plan.md` T3.4 `openapi-typescript`).
> ⚠️ TDS-MF11 §9.2 **chưa có annotation nào** (L8) ⇒ nhóm này sẽ **đỏ toàn bộ** ở lần chạy đầu. **Không nới assert** — ghi rõ *"thiếu annotation D8"* và báo Mạnh.

---

#### MF11-TC-CON-001 — S1 + S2: `@Tag` đúng tên module, `@Operation.description` chứa **UC + BR**

**Severity:** `HIGH` · **Test File:** `contract/OpenApiContractIT.java` · **TDD Phase:** 🔴 RED
**Constraint Ref:** **D8 S1, S2** · **D3**
**Oracle Source:** **D8** luật **S1** (*"`@Tag(name = "MFxx — <Tên module>")`, dùng đúng tên module ở Team Plan §2c-0"*) và **S2** (*"`description` **PHẢI chứa UC ID + BR ID** mà endpoint hiện thực"*) + Team Plan §2c-0 (tên module = *"Incident & Emergency Management"*)

**Expected Result (PASS):**
- Mọi controller MF11 mang `@Tag(name = "MF11 — Incident & Emergency Management")` — **một** tag duy nhất, không tách nhỏ
- **Mọi** operation của MF11 có `description` khớp cả 2 regex: `UC-\d{3}` **và** `BR-\d{3}`
- Ví dụ điểm: `POST /incidents/{code}/assess` → chứa `UC-009`, `UC-041`, `BR-147`, `BR-148`; `POST /incidents/{code}/close` → `UC-013`, `BR-155`; `POST /violations/{code}/resolution` → `UC-180`, `BR-185`, `BR-186`, `BR-211`
- Mọi path bắt đầu bằng `/api/v1` (**D3**)
- ⚠️ Assert **phủ định**: `description` **không** được chứa mã ngoài §1.11 (VD `BR-213` đã Deprecated, `UC-188`, `UC-178`)

**Current Status:** 🔴 Not written

---

#### MF11-TC-CON-002 — S3: mọi 4xx/5xx `$ref` tới `ErrorResponse`, khớp 1-1 với §10

**Severity:** `HIGH` · **Test File:** `contract/OpenApiContractIT.java` · **TDD Phase:** 🔴 RED
**Constraint Ref:** **D8 S3** · **D2**
**Oracle Source:** **D8 S3** (*"Mỗi mã lỗi ở §10 của TDS có một `@ApiResponse` tương ứng, `content` trỏ **`ErrorResponse` dùng chung**"*) + **D2** + §10 TDS (cột HTTP)

**Expected Result (PASS):**
- **Mọi** response `4xx`/`5xx` trong spec MF11 có `content.application/json.schema.$ref = "#/components/schemas/ErrorResponse"` — **0** ngoại lệ
- Mỗi mã ở §10 xuất hiện ít nhất 1 lần: `IEM-004(401)`, `IEM-005(403)`, `IEM-023(400)`, `IEM-085(400)`, `IEM-095(409)`, `IEM-114(409)`, `IEM-147(400)`, `IEM-149(400)`, `IEM-149B(409)`, `IEM-155(409)`, `IEM-186(403)`, `IEM-211(409)`, `IEM-216(409)`, `IEM-217(409)`, `IEM-RO(409)`, `IEM-001(400)`, `IEM-404(404)`, `IEM-500(500)`
- ⚠️ Assert **phủ định (L17)**: chuỗi `IEM-024` / `MSG-024` **không** xuất hiện ở bất kỳ đâu trong spec (khoá bếp đã descope)
- ⚠️ Assert **phủ định (L16)**: `MSG-043` và `MSG-046` **không** xuất hiện (MSG của module khác, mock dùng nhầm)
- `ErrorResponse` khai đủ 3 field `code`/`message`/`details`, `details` kiểu **array** (**D2**)

**Current Status:** 🔴 Not written

---

#### MF11-TC-CON-003 — S4 + S6: ngày `format: date/date-time`, `example` của `id` **không** là UUID

**Severity:** `CRITICAL` · **Test File:** `contract/OpenApiContractIT.java` · **TDD Phase:** 🔴 RED
**Constraint Ref:** **D8 S4, S6** · **D1**, **D4** · **L4**
**Oracle Source:** **D8 S4** (*"Field ngày khai `format = "date"` hoặc `"date-time"` + `example` viết dạng ISO"*) và **S6** (*"`example` của `id` là **mã nghiệp vụ**; không có UUID trong bất kỳ `example`/`@Schema` công khai nào"*) + **D1**, **D4**

**Expected Result (PASS):**
- **Mọi** property tên khớp `(?i)(date|at|deadline)$` có `type: string` + `format ∈ {date, date-time}`; **0** field ngày kiểu `string` trần
- **Mọi** `example` của field ngày khớp ISO; **0** example khớp `^\d{2}/\d{2}/\d{4}` — đây là **cổng chặn chính** của L4: nếu ai đó code theo §17.2 mục 12, test này đỏ ngay
- **Mọi** property tên `id`/`*Id`/`*Code` có `example` là **mã nghiệp vụ** (`INC-2026-014`, `CAPA-2026-033`, `VIO-2026-008`, `BATCH-MP-8820`, `CAT-001`, `U-0003`); **0** example khớp regex UUID; **0** field `id` nào có `format: uuid`
- ⚠️ Assert **phủ định (L32/L34)**: **0** example nào chứa `PTN-0007`, `PTN-0003`, `STF-007` (3 mã không tồn tại trong seed)

**Current Status:** 🔴 Not written

---

#### MF11-TC-CON-004 — S5 + S7: enum khai bằng kiểu thật, mọi `GET` danh sách là `PagedResponse`

**Severity:** `HIGH` · **Test File:** `contract/OpenApiContractIT.java` · **TDD Phase:** 🔴 RED
**Constraint Ref:** **D8 S5, S7** · **D6** · §2b-1
**Oracle Source:** **D8 S5** (*"Enum khai bằng **kiểu enum thật** — **KHÔNG** `@Schema(allowableValues = "…")` viết tay"*), **S7** (*"Mọi `GET` danh sách khai response là `PagedResponse<T>`"*) + **D6** + Team Plan §2b-1

**Expected Result (PASS):**
- **0** chỗ dùng `allowableValues` viết tay trong spec (grep `@Schema(allowableValues` trong source → 0)
- Enum của MF11 trong spec khớp **chính xác** bộ giá trị §5.2: `IncidentStatus` 6 · `IncidentSeverity` 3 · `CapaStatus` 6 · `ResolutionStatus` 4 · `ViolationStatus` 2 · `ViolationType` 5 · `UnitSanctionLevel` 4 · `IncidentEventType` 12
- Enum **dùng chung** xuất hiện trong MF11 (nếu có) khớp bảng **D6** — không khai lại bản sao cục bộ
- **5** `GET` danh sách trả schema `PagedResponse` với đủ 5 property `items`/`total`/`page`/`pageSize`/`pageCount`
- ⚠️ Assert **phủ định (L22)**: `IncidentSeverity` **không** chứa `"PENDING"` (đó là giá trị **hiển thị** khi NULL, không phải giá trị enum) — nhưng schema response phải cho phép `nullable`/`"PENDING"` ở tầng DTO đọc, ghi rõ chỗ này để Mạnh chọn cách khai

**Current Status:** 🔴 Not written

---

#### MF11-TC-CON-005 — S8 + bề mặt bị cấm: spec **không** khai thứ MF11 không được có

**Severity:** `CRITICAL` · **Test File:** `contract/OpenApiSurfaceIT.java` · **TDD Phase:** 🔴 RED
**Constraint Ref:** **D8 S8** · **D5** · `C5`, `C9`, `C10` · **L5, L12, L17, L29, L30**
**Oracle Source:** **D8 S8** (`@SecurityScheme` bearer JWT toàn cục) + **D5** + **BR-217/ADR-M11-3** + **BR-183** (26/07) + **ADR-M11-8** + **CF-M11-19/20**

**Expected Result (PASS):**
- **S8:** có đúng **1** `securityScheme` `bearerAuth` (HTTP bearer, JWT) khai toàn cục; **0** endpoint MF11 nào khai `@SecurityRequirements` rỗng (MF11 **không có** endpoint public)
- **D5 (L5):** **0** `requestBody` nào của MF11 khai field `id` — MF11 không nhận `id` từ client (mọi mã sinh qua `code_sequences`). *(Nếu về sau có, phải kèm `409` + **MSG-139**.)*
- **C9 (L14):** **0** `requestBody`/schema nào có property `staffName`, `staffPosition`, `disciplinaryLevel`
- **C5 (L12):** **0** path khớp `(?i)(release|unquarantine)`; **0** property `releasedAt`/`releaseReason`
- **C10:** **0** requestBody nào nhận `reportExported`
- **L29:** **0** path `/incident-events` (phẳng) — chỉ có `/incidents/{code}/events`
- **L30:** **0** path `/sent-reports` trong spec MF11 (thuộc MF13)
- **L17:** **0** path/response nào gắn `MSG-024`
- **L21 (CF-M11-13):** **0** path `/students/{id}/special-meal-arrangements`, `/meal-service/**` (thuộc module khác)

**Current Status:** 🔴 Not written

---

#### MF11-TC-CON-006 — **D11**: `GET /incidents` đổi **tập FIELD** theo vai ⇒ spec phải khai `oneOf` *(mới v1.1 — bắt buộc theo workflow §7 Bước 1 việc 3)*

**Severity:** `CRITICAL` · **Test File:** `contract/OpenApiRoleShapeIT.java` · **TDD Phase:** 🔴 RED
**Constraint Ref:** **D11** (`02_Module_Completion_Workflow.md` §6) · **D8 S7** · **D13** · `C14` · **M11-D15**
**Oracle Source:** **D11** (*"Response đổi shape theo vai trò phải khai `oneOf`"*) · **S7** (tên schema giữ tiền tố `PagedResponse` — `OpenApiMetaContractIT`) · **ADR-M11-10** + **C14** (PDPD, NĐ 13/2023/NĐ-CP) · TDS §16

> **Vì sao TC này tồn tại:** đây là **lần thứ tư** hệ thống gặp pattern D11 (`GET /schools`, `GET /students`, `GET /student-distributions` của MF09, nay `GET /incidents`), và **hai trong bốn lần là cùng một loại dữ liệu** — PII/sức khoẻ trẻ em. Lần `GET /students` đã tốn **hai phiên curl** mới tìm ra tập field bị cắt, vì `PagedResponse<?>` xoá sạch kiểu và springdoc sinh `items: {}`.
> Khác MF06 (`TC-SEC-014`) và MF07 (`TC-SEC-017`) — hai module đó kết luận *"D11 không áp dụng"* **đúng**, vì khác biệt giữa vai trò của họ là **tập DÒNG** (`WHERE`). MF11 khác biệt ở **tập FIELD** ⇒ `oneOf` **bắt buộc**.

**Tiền điều kiện:** dùng **tài khoản seed thật** cho cả 2 vai (**D13** / `M11-D17`) — `@WithMockUser` **không** mang claim `schoolId`/`partnerId` nên không tái hiện được phạm vi đọc. Mật khẩu `P@ssw0rd123`.

**Expected Result (PASS):**

*Phần A — hành vi runtime (2 vai, cùng 1 endpoint):*
- Medical Staff gọi `GET /api/v1/incidents/INC-2026-014` → **200**, thấy đủ `symptoms`, `firstAidNotes`, `clinicalRiskAssessment`, `investigationLabResult`, `rootCause`, `officialClinicalNotes`, `affectedStudents[].studentName`
- **Service Caterers** (`CAT-001`, có CAPA `CAPA-2026-033` giao đơn vị mình ⇒ **trong** phạm vi đọc) gọi **cùng path** → **200**, và **7 field trên KHÔNG xuất hiện** trong JSON (assert **vắng mặt key**, không phải `null` — `null` vẫn rò cấu trúc)
- **Teacher** (chủ nhiệm `3B`, có HS liên đới) → **200**, `affectedStudents[]` **chỉ** chứa HS lớp `3B` *(2/4 bản ghi của `INC-2026-014`)*, và **không** thấy `clinicalRiskAssessment`/`investigationLabResult`/`rootCause`
- Cả 3 vai: `GET /api/v1/incidents` (danh sách) giữ đúng shape `{items,total,page,pageSize,pageCount}`

*Phần B — spec `/v3/api-docs` (phần D11 thật sự, và là phần dễ bỏ sót nhất):*
- Response của `GET /incidents` khai **`oneOf`** với **≥ 2** schema
- Tên schema **giữ tiền tố `PagedResponse`** (S7): `PagedResponseIncidentResponse` · `PagedResponseIncidentCaterersResponse`
- `items` trong **mỗi** nhánh `oneOf` là **`$ref` tới schema có property**, ⚠️ **KHÔNG phải `{}`** — đây chính là triệu chứng của `PagedResponse<?>`
- `IncidentCaterersResponse` **không có** 7 property nhạy cảm ở phần A
- ⚠️ **Không** đặt lại `name=` cho `@Schema` trên `common/dto/PagedResponse.java` (workflow §5) — ghim tên sẽ làm `items` tụt xuống `{}` cho **cả 11 endpoint danh sách** của mọi module

**Current Status:** 🔴 Not written

---

### 4.5. E2E TEST CASES

> `@SpringBootTest` + Testcontainers + `TestRestTemplate`, chạy **theo kịch bản nghiệp vụ thật**, nhiều role nối tiếp. Nguồn: TDS §15 (mẫu thử thực tế) + §13.3.

---

#### MF11-TC-E2E-001 — Trục xương sống đầy đủ: `RPT → REC → INC → CAPA → đóng`

**Severity:** `CRITICAL` · **Test File:** `e2e/IncidentSpineE2E.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** toàn bộ · **Constraint Ref:** C2, C3, C4, C5, C6, C7, C10, C11
**Oracle Source:** TDS **§15.1** (15 bước) + Team Plan **§3** (trục xương sống) + TDS §13 `TC-E2E-001`

**Test Steps (14 bước, mỗi bước đổi actor đúng vai):**
1. Teacher `U-0004` `POST /source-reports` → 201 `RPT`, **MSG-048**
2. Medical Staff `U-0002` `POST /incident-records` (gộp 2 `RPT`) → 201 `REC`, **MSG-100**
3. `PATCH /incident-records/{code}` (soạn nội dung) → 200 · 4. `POST /finalize` → **MSG-101**
5. `POST /open-investigation` → 201 `INC`, `severity: "PENDING"`, **MSG-048**
6. `GET /suspected-dish-candidates` → chỉ món trong **24h** trước `occurredAt`
7. `POST /quarantine-batches` (reason bắt buộc) → 200, lô `QUARANTINED` bên MF06
8. `POST /assess` (`HIGH`) → 200, **MSG-029**, cảnh báo 3 nhóm vào outbox
9. `GET /traceability-map` → `READY`, node nối `lô → NCC → món → kiểm thực → mẫu → HS`
10. `POST /incidents/{code}/root-cause` (PRELIMINARY) → 200
11. Medical Staff `POST /corrective-actions` → 201 `CAPA`, **MSG-049**
12. School Admin `POST /corrective-actions/{code}/approve` → 200; Caterers `accept` → `IN_PROGRESS`; `submit-evidence` (≥1 ảnh) → `SUBMITTED_EVIDENCE`
13. Medical Staff `POST /validate-capa` (mọi HS `recovered=true`) → `COMPLETED`; `POST /report` → `reportExported = true`
14. School Admin `POST /close` → 200 `CLOSED`, **MSG-029**

**Expected Result (PASS):**
- Mọi bước **2xx**, đúng actor; sai actor ở bất kỳ bước nào → 403 (đã test riêng ở §4.2)
- `GET /incidents/{code}/events` cuối luồng có **≥ 8 mốc** đúng thứ tự thời gian tăng dần: `REPORTED → QUARANTINE → SEVERITY_SET → TRACE_READY → CAPA_SUBMITTED → CAPA_APPROVED → CAPA_EVIDENCE → CAPA_VALIDATED → REPORT_SENT → CLOSED`
- Chuỗi `INC → studentId → dishId → batchId → supplierId → sampleId` nối **bằng ID** ở mọi mắt (không mắt nào nối bằng tên)
- Mọi ngày trong mọi response là **ISO** (D4)

**Current Status:** 🔴 Not written

---

#### MF11-TC-E2E-002 — Cách ly dữ liệu theo vai trong cùng một sự cố

**Severity:** `CRITICAL` · **Test File:** `e2e/RoleIsolationE2E.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-22`, `COND-03` · **Constraint Ref:** `C14`, `C15` · `AP-M11-12`
**Oracle Source:** **§16** (projection theo vai) + **UC-120** + **BR-201** + **PDPD** + TDS §13 `TC-E2E-003`/`TC-E2E-012`

**Expected Result (PASS):** cùng `INC-2026-014`, 5 actor đọc ra 5 kết quả khác nhau — assert **đồng thời** trong một test:
| Actor | Kết quả |
|---|---|
| Medical Staff | 200, **đầy đủ** mọi field |
| School Admin | 200, đầy đủ (trừ quyền ghi) |
| Teacher 3A | 200, chỉ 2 HS lớp 3A, **không** `clinicalRiskAssessment`/`rootCause` |
| Teacher 4A | **403 `IEM-005`** |
| Service Caterers | 200, **không** `symptoms`/`firstAidNotes`/`studentName`; chỉ `affectedStudentCount` |
| Parent | **403 `IEM-005`** (kể cả với con mình) |

**Current Status:** 🔴 Not written

---

#### MF11-TC-E2E-003 — Gate đóng sự cố: 3 điều kiện, gỡ từng cái một

**Severity:** `CRITICAL` · **Test File:** `e2e/ClosureGateE2E.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-13` · **Constraint Ref:** `C7` · ràng buộc **C1**
**Oracle Source:** **BR-155** + ràng buộc **C1** + **BR-217** (lô ở trạng thái cuối) + §10 (`IEM-155`, `IEM-217`)

**Test Steps:** bắt đầu với cả 3 điều kiện **hỏng**, gỡ từng cái, gọi `close` sau mỗi lần gỡ.

**Expected Result (PASS):**
- Lần 1 (cả 3 hỏng) → **409**, `details` liệt kê **cả 3 nhóm** (CAPA + HS + lô), không dừng ở lỗi đầu
- Lần 2 (CAPA `COMPLETED`) → **409**, còn 2 nhóm
- Lần 3 (HS `recovered`) → **409 `IEM-217`** (chỉ còn lô)
- Lần 4 (lô `RETURNED_TO_SUPPLIER`) → **200 CLOSED**
- `GET /closure-readiness` trước mỗi lần trả **đúng** danh sách còn thiếu → FE dựng checklist từ đây, không tự tính

**Current Status:** 🔴 Not written

---

#### MF11-TC-E2E-004 — Vòng đời biên bản vi phạm MEDIUM: lập → PDF → ký → xử lý → BGH duyệt

**Severity:** `HIGH` · **Test File:** `e2e/ViolationLifecycleE2E.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-16`, `COND-18`, `COND-19` · **Constraint Ref:** `C8`, `C9`
**Oracle Source:** **BR-183** → **BR-211** → **BR-185** → **BR-186** (chuỗi đầy đủ) + MSG-086/095/087/089

**Test Steps:**
1. Medical Staff `POST /violations` (MEDIUM, đại diện `U-0003`, 2 ảnh bằng chứng) → 201, **MSG-086**, `repeatSuspect` theo BR-187
2. `POST /resolution` **ngay** → **409 `IEM-095`** (chưa PDF, chưa ảnh ký)
3. `POST /{code}/signed-record` **trước khi** xuất PDF → **409 `IEM-211`**
4. `POST /{code}/pdf` → 200, `pdfExported=true`, `pdfVersion=1`
5. `POST /{code}/signed-record` (ảnh ký) → 200, biên bản **Đã khoá** (không sửa nội dung được)
6. `POST /resolution` (`unitSanctionLevel=CONTRACT_WARNING`) → 200, `PENDING_APPROVAL`, **MSG-087**
7. Medical Staff `POST /resolution/approve` → **403 `IEM-186`** (MEDIUM phải BGH)
8. School Admin `POST /resolution/approve` → 200, `APPROVED`/`CLOSED`, **MSG-089**

**Expected Result (PASS):** đúng 8 kết quả trên; audit có **8 dòng** kèm IP (BR-188); sau bước 5, `PUT`/`PATCH` sửa `description` → **409** (đã khoá).

**Current Status:** 🔴 Not written

---

#### MF11-TC-E2E-005 — `reportExported`: khoá hồ sơ với **mọi** role, chỉ `close` đi qua

**Severity:** `CRITICAL` · **Test File:** `e2e/ReportLockE2E.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-14` · **Constraint Ref:** `C10` · `AP-M11-08`
**Oracle Source:** **ADR-M11-8** + TDS §13 `TC-E2E-009` + §15.2 `E8`

**Expected Result (PASS):**
- Sau `POST /incidents/{code}/report`: 10 endpoint ghi × 4 role (`medical-staff`, `school-admin`, `service-caterers`, `system-admin`) → **40/40 trả 409 `IEM-RO`**
- `POST /close` (School Admin) → **200**
- Sau khi `CLOSED`: `GET` vẫn 200, `GET /report?format=pdf` tải được (UC-121)
- **Không** đường nào đặt lại `reportExported = false` (kể cả DB script trong test — nếu ai đó thêm, `TC-SEC-015` bắt)

**Current Status:** 🔴 Not written

---

#### MF11-TC-E2E-006 — Đổi `baseURL` FE mock → backend thật: 6 màn chính render đúng

**Severity:** `HIGH` · **Test File:** `e2e/FeContractSmokeE2E.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-21` · **Constraint Ref:** `C1`, **D1–D4**
**Oracle Source:** Team Plan **§7** (*"Minh xác nhận §9 API Spec: đổi baseURL FE sang backend thật, các màn vẫn chạy"*) + TDS §13 `TC-E2E-012`

**Expected Result (PASS):** gọi đúng bộ request mà 6 màn hình phát ra, assert shape khớp `Paged<T>`/DTO mà FE đang đọc:
- `medical-staff/incidents` (`GET /source-reports?status=`, `GET /incidents`)
- `medical-staff/incidents/[id]` (`GET /incidents/{code}` + `/events` + `/samples` + `/lab-requests` + `/traceability-map`)
- `school-admin/corrective-actions` (`GET /corrective-actions?status=PENDING_ADMIN_APPROVAL`)
- `medical-staff/violations` (`GET /violations`) · `school-admin/violations/override-report` (`GET /violations/override-abuse-report?week=`)
- `teacher/incidents` (`GET /source-reports?className=3A`)
- ⚠️ **Ghi rõ 3 chỗ FE buộc phải sửa trước** (không phải lỗi backend): bỏ `POST /incident-events`, bỏ `useUpdate("inventory")`, đổi `PUT` generic → endpoint nghiệp vụ (**L29/L19** — F1/F2/F3 §11.2b)
- ⚠️ Ngày trả về là **ISO** ⇒ FE **phải** có `lib/format.ts` mới hiển thị đúng (**L4**) — nếu FE chưa có, màn hiện chuỗi ISO thô: **đúng hợp đồng**, là việc của FE

**Current Status:** 🔴 Not written

---

## 5. Red-Green-Refactor Tracker

> Cập nhật **mỗi lần chạy `mvn verify`**. Không mark 🟢 nếu build chưa xanh.

### 5.1. Unit (27)

| TC | Tên rút gọn | Severity | 🔴 Red | 🟢 Green | 🔵 Refactor |
|---|---|---|---|---|---|
| `TC-001` | BR-146 không tự tạo INC/cách ly | CRITICAL | ☐ | ☐ | ☐ |
| `TC-002` | MSG-023 thiếu trường `RPT` | HIGH | ☐ | ☐ | ☐ |
| `TC-003` | Cluster signal chỉ gợi ý | HIGH | ☐ | ☐ | ☐ |
| `TC-004` | BR-201 parent không liên kết | CRITICAL | ☐ | ☐ | ☐ |
| `TC-005` | ADR-M11-1 chuỗi RPT→REC→INC | HIGH | ☐ | ☐ | ☐ |
| `TC-006` | Khử trùng HS khi gộp | MEDIUM | ☐ | ☐ | ☐ |
| `TC-007` | BR-147 role guard assess | CRITICAL | ☐ | ☐ | ☐ |
| `TC-008` | severity ngoài enum | HIGH | ☐ | ☐ | ☐ |
| `TC-009` | BR-148 cảnh báo async 3 nhóm | CRITICAL | ☐ | ☐ | ☐ |
| `TC-010` | BR-149 thiếu lý do cách ly | CRITICAL | ☐ | ☐ | ☐ |
| `TC-011` | BR-217 cách ly một chiều | CRITICAL | ☐ | ☐ | ☐ |
| `TC-012` | Cửa sổ 24h + mốc gốc | HIGH | ☐ | ☐ | ☐ |
| `TC-013` | BR-216 1 INC = 1 CAPA | HIGH | ☐ | ☐ | ☐ |
| `TC-014` | CAPA từ kiểm thực (XOR nguồn) | MEDIUM | ☐ | ☐ | ☐ |
| `TC-015` | State machine CAPA đầy đủ | HIGH | ☐ | ☐ | ☐ |
| `TC-016` | reject/rework thiếu lý do | MEDIUM | ☐ | ☐ | ☐ |
| `TC-017` | rework giữ `approvedAt` | MEDIUM | ☐ | ☐ | ☐ |
| `TC-018` | HS chưa hồi phục → IEM-155 (≠ MSG-004) | CRITICAL | ☐ | ☐ | ☐ |
| `TC-019` | BR-155 chỉ School Admin đóng | CRITICAL | ☐ | ☐ | ☐ |
| `TC-020` | Gate đóng: CAPA/HS/lô | CRITICAL | ☐ | ☐ | ☐ |
| `TC-021` | `reportExported` khoá 1 chiều | CRITICAL | ☐ | ☐ | ☐ |
| `TC-022` | MSG-085 thiếu trường `VIO` | HIGH | ☐ | ☐ | ☐ |
| `TC-023` | BR-187 theo đại diện, không chặn | HIGH | ☐ | ☐ | ☐ |
| `TC-024` | BR-211 gate ảnh ký + PDF | CRITICAL | ☐ | ☐ | ☐ |
| `TC-025` | BR-186 phân cấp duyệt | HIGH | ☐ | ☐ | ☐ |
| `TC-026` | BR-190 override abuse dẫn xuất | MEDIUM | ☐ | ☐ | ☐ |
| `TC-027` | C1 paging + SEARCH_FIELDS | MEDIUM | ☐ | ☐ | ☐ |

### 5.2. Security (16)

| TC | Tên rút gọn | Severity | 🔴 | 🟢 | 🔵 |
|---|---|---|---|---|---|
| `TC-SEC-001` | Ma trận RBAC 7 role × 12 endpoint | CRITICAL | ☐ | ☐ | ☐ |
| `TC-SEC-002` | Caterers gọi `/assess` | CRITICAL | ☐ | ☐ | ☐ |
| `TC-SEC-003` | Caterers/Medical tự duyệt CAPA | CRITICAL | ☐ | ☐ | ☐ |
| `TC-SEC-004` | `PUT` generic không tồn tại | CRITICAL | ☐ | ☐ | ☐ |
| `TC-SEC-005` | Medical Staff gọi `/close` | CRITICAL | ☐ | ☐ | ☐ |
| `TC-SEC-006` | PDPD projection Caterers | CRITICAL | ☐ | ☐ | ☐ |
| `TC-SEC-007` | Teacher `Own class` + cắt field | CRITICAL | ☐ | ☐ | ☐ |
| `TC-SEC-008` | Regression: Teacher không bị chặn | HIGH | ☐ | ☐ | ☐ |
| `TC-SEC-009` | Parent chỉ tạo, không đọc | HIGH | ☐ | ☐ | ☐ |
| `TC-SEC-010` | JWT là nguồn danh tính; 403 ≠ 404 | CRITICAL | ☐ | ☐ | ☐ |
| `TC-SEC-011` | Rate limit 429 + MSG-141 | MEDIUM | ☐ | ☐ | ☐ |
| `TC-SEC-012` | Audit bất biến + IP | HIGH | ☐ | ☐ | ☐ |
| `TC-SEC-013` | D10 StorageService + MSG-008/009 | HIGH | ☐ | ☐ | ☐ |
| `TC-SEC-014` | Caterers `Liên đới` (khoan dung) | HIGH | ☐ | ☐ | ☐ |
| `TC-SEC-015` | Không có route nhả lô/mở khoá | CRITICAL | ☐ | ☐ | ☐ |
| `TC-SEC-016` | System Admin chỉ đọc | MEDIUM | ☐ | ☐ | ☐ |

### 5.3. Integration (23) + Contract (5) + E2E (6)

| TC | Tên rút gọn | Severity | 🔴 | 🟢 | 🔵 |
|---|---|---|---|---|---|
| `TC-INT-001` | `incident_events` append-only (RULE) | CRITICAL | ☐ | ☐ | ☐ |
| `TC-INT-002` | `iqb_no_delete` + release log rỗng | CRITICAL | ☐ | ☐ | ☐ |
| `TC-INT-003` | Gate đóng → DB không đổi | CRITICAL | ☐ | ☐ | ☐ |
| `TC-INT-004` | assess song song → 1 mốc | HIGH | ☐ | ☐ | ☐ |
| `TC-INT-005` | Outbox cùng transaction (BR-148) | CRITICAL | ☐ | ☐ | ☐ |
| `TC-INT-006` | Cách ly qua port, không đụng MF06 | CRITICAL | ☐ | ☐ | ☐ |
| `TC-INT-007` | Bản đồ truy xuất ≤ 4h | HIGH | ☐ | ☐ | ☐ |
| `TC-INT-008` | Ràng buộc CAPA ở tầng DB | HIGH | ☐ | ☐ | ☐ |
| `TC-INT-009` | `code_sequences` 100 request | HIGH | ☐ | ☐ | ☐ |
| `TC-INT-010` | D1 — id là mã nghiệp vụ | CRITICAL | ☐ | ☐ | ☐ |
| `TC-INT-011` | D4 — ISO-8601 | CRITICAL | ☐ | ☐ | ☐ |
| `TC-INT-012` | D2/D6/D7 — envelope, enum, message | HIGH | ☐ | ☐ | ☐ |
| `TC-INT-013` | D9 — BaseEntity audit | MEDIUM | ☐ | ☐ | ☐ |
| `TC-INT-014` | audit §9 — báo cáo nguồn (3 luật) | HIGH | ☐ | ☐ | ☐ |
| `TC-INT-015` | audit §9 — dòng thời gian (3 luật) | HIGH | ☐ | ☐ | ☐ |
| `TC-INT-016` | audit §9 — CAPA (2 luật) | MEDIUM | ☐ | ☐ | ☐ |
| `TC-INT-017` | audit §9/§9b — cách ly (2 luật) | HIGH | ☐ | ☐ | ☐ |
| `TC-INT-018` | audit §4/§5 — mẫu lưu (3 luật) | MEDIUM | ☐ | ☐ | ☐ |
| `TC-INT-019` | Route sự kiện đổi | HIGH | ☐ | ☐ | ☐ |
| `TC-INT-020` | Hợp đồng list §2b | MEDIUM | ☐ | ☐ | ☐ |
| `TC-INT-021` | Override abuse dẫn xuất | MEDIUM | ☐ | ☐ | ☐ |
| `TC-INT-022` | Trục xương sống nối bằng ID | CRITICAL | ☐ | ☐ | ☐ |
| `TC-INT-023` | Bất biến toàn cục sau seed | CRITICAL | ☐ | ☐ | ☐ |
| `TC-CON-001` | D8 S1/S2 — Tag + UC/BR | HIGH | ☐ | ☐ | ☐ |
| `TC-CON-002` | D8 S3 — ErrorResponse | HIGH | ☐ | ☐ | ☐ |
| `TC-CON-003` | D8 S4/S6 — ngày ISO + id không UUID | CRITICAL | ☐ | ☐ | ☐ |
| `TC-CON-004` | D8 S5/S7 — enum thật + PagedResponse | HIGH | ☐ | ☐ | ☐ |
| `TC-CON-005` | D8 S8 + bề mặt bị cấm | CRITICAL | ☐ | ☐ | ☐ |
| `TC-E2E-001` | Trục xương sống 14 bước | CRITICAL | ☐ | ☐ | ☐ |
| `TC-E2E-002` | Cách ly dữ liệu 6 vai | CRITICAL | ☐ | ☐ | ☐ |
| `TC-E2E-003` | Gate đóng 3 điều kiện | CRITICAL | ☐ | ☐ | ☐ |
| `TC-E2E-004` | Vòng đời vi phạm MEDIUM | HIGH | ☐ | ☐ | ☐ |
| `TC-E2E-005` | `reportExported` khoá mọi role | CRITICAL | ☐ | ☐ | ☐ |
| `TC-E2E-006` | FE contract smoke 6 màn | HIGH | ☐ | ☐ | ☐ |

### 5.4. Red Gate Protocol (CASE 2.0 — GATE-2)

> **Không** viết production code trước khi qua cổng này.

1. **Viết test trước** — commit riêng, message `test(MF11): red — <TC-ID>`.
2. **Chạy `mvn -Dtest=<Class> test` → phải FAIL** với lý do đúng (assertion fail / `NoSuchMethodError`), **không** phải fail vì compile lỗi cấu hình hay Testcontainers không khởi động.
3. **Ghi lại output đỏ** vào PR description — bằng chứng test **thực sự** kiểm được thứ nó nói.
4. **Implement tối thiểu** để xanh; **không** sửa test cho khớp code (nếu thấy test sai oracle → sửa §2 Logic Issues trước, có người duyệt).
5. **Refactor** khi xanh; chạy lại toàn suite.
6. **Trust Level:** `T2 → T3` chỉ khi **toàn bộ TC CRITICAL** đã 🟢 và §6 Exit Criteria thoả.

**Nhóm phải đỏ ngay lần chạy đầu (kỳ vọng, không phải lỗi hạ tầng):**
- `TC-CON-001…005` — §9.2 TDS **chưa có annotation D8** (L8)
- `TC-INT-011` — nếu code sinh theo §17.2 mục 12 sẽ có `@JsonFormat` (L4)
- `TC-INT-023` — nếu seed chép nguyên `mock-data.ts` sẽ đỏ ở truy vấn (2), (4), (5) (L36/L37)
- `TC-INT-016`, `TC-INT-022` — nếu dùng mã `PTN-0007`/`dish-ga-rang-gung` từ ví dụ §9.2 (L32/L33)

---

## 6. Entry / Exit Criteria

### Entry Criteria (điều kiện bắt đầu viết test)

- [ ] `TDS-MF11_Backend.md` v1.1 đã đọc hết; §5.2 / §9 / §10 / §16 / §17 ổn định.
- [ ] **`00_Common_Conventions.md` D1–D10** đã đọc; hiểu rõ **D4 ghi đè ADR-M11-5** và **D7** yêu cầu message tiếng Anh.
- [ ] Nền chung của Nam đã có: `BaseEntity` + `AuditorAware`, `PagedResponse<T>`, `@RestControllerAdvice` + `ErrorResponse`, `outbox_events`, `code_sequences`, `common/storage/StorageService`, `common/config/OpenApiConfig`.
- [ ] Flyway **`V20__mf11_incident.sql`** đã có **13 bảng MF11** + 3 RULE + các CHECK/UNIQUE §5.2.
- [ ] **8 port** §8.3 có bản hiện thực **hoặc** stub `@Profile("dev")` trả rỗng.
- [ ] Testcontainers chạy được trên máy dev + CI (Docker sẵn sàng).
- [ ] Fixture `FX-001…FX-031` đã dựng; **4 mã không tồn tại** (`PTN-0007`, `PTN-0003`, `STF-007`, `dish-ga-rang-gung`) **không** xuất hiện trong bất kỳ file test nào.

### Exit Criteria (Definition of Done)

- [ ] **77/77 TC** ở trạng thái 🟢, `mvn verify` xanh.
- [ ] Coverage: **≥ 90% line** cho `com.sfms.mf11.service`; **100% branch** cho `QuarantineService.quarantineBatch()`, `IncidentClosureService.close()`, `CapaService` (6 transition), `ViolationResolutionService.submit()`, `detailForCaterers()`.
- [ ] **Mỗi constraint C1–C15** có ≥ 1 TC 🟢 (chống AP-AI-001).
- [ ] **Mỗi UC trong 20 UC** của §1.11 truy được về ≥ 1 TC (bảng §9.6).
- [ ] **12 luật `semantic-audit.ts`** đã port và xanh; luật `DONG_SU_CO_MA_CON_LO_KHOA` được ghi rõ là **không port** kèm lý do (L21).
- [ ] `TC-INT-023` xanh ⇒ **seed đã được sửa** theo L36/L37 (không còn dòng vi phạm bất biến).
- [ ] 3 grep ở boilerplate (`shared state`, `staffName`, `release`) đều **rỗng**.
- [ ] `/v3/api-docs` tải được và **5 TC contract xanh** ⇒ D8 nghiệm thu được; file spec lưu kèm tài liệu nộp (§11 TDS).

**✅ 6 việc từng chặn "Approved" — ĐÃ ĐÓNG HẾT 2026-08-04** *(bảng giữ nguyên theo Policy 4.4)*

| # | Việc | Người chốt | ✅ Kết quả |
|---|---|---|---|
| 1 | Cấp **10 MSG** cho `IEM-114/147/149/149B/155/186/211/216/217/RO` (L7/L35) | **Huy** | ✅ **`error_message.md` v1.11 cấp MSG-149…158** + **MSG-159/160/161** thay MSG-029 (L16/CF-M11-6). **Hết `@Disabled` vì thiếu oracle** — mọi TC error-path nay assert đủ `code` + HTTP + `message` nguyên văn |
| 2 | Chốt vế phạm vi **Service Caterers** (CF-M11-18/L28) | **Nam** | ✅ **`M11-D19` — GIỮ cả 2 vế `OR`.** `TC-SEC-014` bỏ `@Disabled`, test cả hai nhánh. Bằng chứng schema: `BATCH-MP-8820` → `SUP-003` còn CAPA → `CAT-001` ⇒ vế 2 **không dư** |
| 3 | Sửa **C12 + §17.2 mục 12** cho khớp D4 (L4) | **Mạnh** | ✅ **`M11-D13`** — TDS v1.2 sửa cả hai sang ISO-8601; §17.2 thêm mục 12b/12c/12d (D11/D12/D9) |
| 4 | Sửa **3 mã ví dụ không tồn tại** ở §9.2 (L32/L34) | **Mạnh** | ✅ **`M11-D10`** — đóng bằng **FK thật** `assignee_supplier_id → partners(id)` thay vì bằng kỷ luật fixture: mã bịa `PTN-0007` nay **fail ở tầng DB** |
| 5 | Cấp **`Dish.code`** cho MF05 (L33) | **Thảo** + Mạnh | ✅ **Không cần cấp — đã có sẵn**: `V8__mf05_menu_diet.sql:35` `dishes.code VARCHAR(64) UNIQUE` + `ReferenceResolver.dishIdByCode()` đang chạy (`M11-D12`). L33 viết khi chỉ đọc `mock-data.ts` |
| 6 | Cập nhật luật audit `DONG_SU_CO_MA_CON_LO_KHOA` (L21) + ghi chú **48h→24h** ở §1.11 (L38) | **Thảo** / **Huy** | ✅ **48h→24h đã sửa** (`traceability_matrix.md` v1.4, ghi chú 4 dưới §1.11) — đóng L38. ⏳ **Luật audit còn lại là việc Bước 2 của Thảo**, không chặn Approved: script hiện báo false-positive nhưng TestSpec đã ghi rõ **không port** luật đó (L21) |

> **Hai mục treo cuối cùng cũng đóng cùng ngày** (TDS §2.5): **H3** — `M11-D20` chốt `scope = quarantineBatches` cho bản đồ truy xuất BR-151 ⇒ nhánh *"toàn sự cố"* **loại bỏ**, không giữ `@Disabled`. **H7** — Mạnh duyệt `M11-D18` (bỏ `IEV-013-05`; hạ 4 dòng `VIO` về `PENDING`).
>
> **Còn lại đều là hạng mục Bước 2/Bước 3, không chặn Approved:** 3 port `M11-D08` (`TC-INT-024` phủ) · sửa mock FE (Minh — §11.2b F1…F9) · `semantic-audit.ts` (Thảo) · `data_model.puml` (Mạnh).

### Suspension Criteria (điều kiện tạm dừng)

- Testcontainers không khởi động được trên CI → dừng nhóm integration/contract, giữ unit.
- **`V20__mf11_incident.sql`** chưa chạy / thiếu bảng → dừng toàn bộ integration (không "chữa" bằng `ddl-auto=update`). *(v1.0 ghi "Flyway `V1` chưa có bảng MF11" — mô hình V1-toàn-schema đã bỏ, xem `M11-D05`.)*
- Nếu **> 30%** TC đỏ vì **cùng một** nguyên nhân spec (VD chưa annotate D8) → dừng, báo Mạnh/Nam, **không** nới assert để cho xanh.

---

## 7. Rollback Plan

```bash
# ── DEV / TEST ONLY ────────────────────────────────────────────────
# 1) Xoá dữ liệu test MF11 (KHÔNG chạm schema)
#    ⚠️ CHỈ chạy trên DB Testcontainers / DB dev — xem R1 dưới
#    TRUNCATE violation_files, violation_records, corrective_actions,
#             incident_events, incident_evidence, incident_quarantine_batches,
#             incident_suspected_dishes, incident_affected_students,
#             incidents, incident_record_sources, incident_records,
#             incident_source_reports CASCADE;

# 2) Hạ schema MF11 — migration BÙ, KHÔNG flyway:undo trên môi trường có dữ liệu
#    src/main/resources/db/migration/V{n}__mf11_rollback.sql
#      DROP RULE  IF EXISTS incident_events_no_update ON incident_events;
#      DROP RULE  IF EXISTS incident_events_no_delete ON incident_events;
#      DROP RULE  IF EXISTS iqb_no_delete ON incident_quarantine_batches;
#      DROP TABLE IF EXISTS violation_files, violation_records, corrective_actions,
#                           incident_events, incident_evidence,
#                           incident_quarantine_release_log, incident_quarantine_batches,
#                           incident_suspected_dishes, incident_affected_students,
#                           incidents, incident_record_sources, incident_records,
#                           incident_source_reports CASCADE;

# 3) Testcontainers: container bị huỷ sau mỗi lần chạy (chỉ giữ khi .withReuse(true))

# 4) Revert code test
git revert <commit-test-mf11>

# 5) Gỡ Red Phase stub trước khi merge
grep -rn "TODO: Red Phase\|@Disabled(\"stub\")" src/main/java/com/sfms/mf11/
```

**R1 — ⚠️ Cấm tuyệt đối trên môi trường có dữ liệu thật:**
`incident_events`, `violation_records`, `violation_files`, `incident_quarantine_batches` là **hồ sơ giữ tối thiểu 10 năm** (§4.2 TDS) và có thể là **căn cứ pháp lý / chế tài hợp đồng**. Không `TRUNCATE`, không `DROP`, không `DELETE`. Nếu migration MF11 hỏng trên môi trường đã có dữ liệu: **tắt endpoint ghi bằng feature flag + giữ nguyên dữ liệu** (§12.2 bước 4 TDS), rollback bằng **revert code**, không bằng hạ schema.

**R2 — Rollback không hoàn tác được hệ quả vật lý:** lô đã cách ly đã được **trả về nhà cung cấp** ngoài đời (BR-217). Revert code **không** mang lô về; nếu phải hoàn tác nghiệp vụ, xử lý bằng **biên bản bổ sung ngoài hệ thống** (ADR-M11-8 §Hệ quả).

---

## 8. CASE 2.0 Anti-Pattern Detection

| AP-ID | Anti-Pattern | Dấu hiệu trong **test** | TC canh |
|---|---|---|---|
| `AP-AI-001` | Unconstrained Gen | TC không có dòng `Oracle Source`, hoặc oracle là "theo logic thông thường" | Review §4 |
| `AP-AI-002` | Shared mutable fixture | `static Incident INC = …` dùng chung giữa test | grep (a) boilerplate |
| `AP-AI-005` | Hallucinated Contract | Test gọi service/field ngoài §8 TDS (VD `IncidentSessionEntity`) | Compile fail |
| `AP-AI-006` | Trust FE identity | Test truyền `schoolId`/`role` qua body rồi assert theo body | `TC-SEC-010` |
| **`AP-M11-01`** | **Auto-create incident** | Test chấp nhận `POST /source-reports` tạo `INC` | `TC-001` |
| **`AP-M11-02`** | **Frozen record mutated** | Test gọi `save()` lần 2 trên `IncidentEvent`, hoặc bỏ qua khi UPDATE không đổi dữ liệu | `TC-INT-001` |
| **`AP-M11-03`** | **Release endpoint** | Test nào tồn tại cho `release`/`unquarantine` | `TC-SEC-015`, `TC-CON-005` |
| **`AP-M11-04`** | **Generic status merge** | Test dùng `PUT /:resource/:id` để đổi trạng thái | `TC-SEC-004` |
| **`AP-M11-05`** | **Close gate bypass** | Test `close` mà không dựng đủ 3 điều kiện | `TC-020`, `TC-E2E-003` |
| **`AP-M11-06`** | **Signature gate bypass** | Test bỏ qua nhánh *"lưu vẫn được"* của BR-211 | `TC-024` |
| **`AP-M11-07`** | **Individual sanction** | Fixture set `staffName`/`disciplinaryLevel` | grep (b) boilerplate |
| **`AP-M11-08`** | **Unlock exported report** | Test đặt `reportExported=false` để dọn state | `TC-E2E-005` |
| **`AP-M11-09`** | **Cross-module entity import** | Test import `Student`/`InventoryBatch` entity | `TC-INT-006` |
| **`AP-M11-10`** | **Derived table created** | Test seed bảng `override_abuse` | `TC-INT-021` |
| **`AP-M11-11`** | **Sync emergency alert** | Test assert email đã gửi trong `assess()` | `TC-009` |
| **`AP-M11-12`** | **Health data leak** | Test Caterers chỉ assert status 200, không assert field bị cắt | `TC-SEC-006` |
| **`AP-M11-13`** | **Wrong MSG code** | Test chấp nhận MSG-004 cho lỗi nghiệp vụ, MSG-043/046 cho CAPA | `TC-018`, `TC-CON-002` |
| **`AP-M11-14`** | **Owner column smell** | Test dựa vào cột `caterer_id` trên `incidents` | `TC-SEC-014` |
| **`AP-M11-15`** | **Blocked role regression** | Test assert Teacher nhận 403 ở `source-reports`/`incidents` | `TC-SEC-008` |
| **`AP-M11-16`** *(mới)* | **Seed-driven oracle** | Test lấy expected từ `mock-data.ts` thay vì BR/MSG — đặc biệt nguy hiểm vì **5 nhóm dòng seed đang vi phạm bất biến** (L36/L37) | `TC-INT-023` |
| **`AP-M11-17`** *(mới)* | **Invented identifier** | Test dùng `PTN-0007`/`PTN-0003`/`STF-007`/`dish-ga-rang-gung` copy từ ví dụ §9.2 | `TC-INT-016`, `TC-CON-003` |

---

## 9. Phụ lục — Danh sách mã UC/BR/MSG đã trích dẫn

### 9.1. Use Case (20 — đúng `traceability_matrix.md` §1.11)

| UC ID | Tên UC (nguyên văn) | Actor | TC cover |
|---|---|---|---|
| UC-073 | Report Food Safety Concern | Parent | `TC-001`, `TC-004`, `TC-SEC-009`, `TC-E2E-002` |
| UC-074 | Report Food Safety Incident | Teacher | `TC-001`, `TC-002`, `TC-SEC-008`, `TC-E2E-001` |
| UC-041 | Manage Food Safety Incidents | Medical Staff | `TC-010`, `TC-011`, `TC-012`, `TC-INT-006`, `TC-INT-007` |
| UC-009 | Assign Incident Severity | Medical Staff | `TC-007`, `TC-008`, `TC-009`, `TC-INT-004`, `TC-SEC-002` |
| UC-038 | Manage Corrective Actions | Service Caterers | `TC-015`, `TC-INT-008`, `TC-E2E-001` |
| UC-082 | Submit Corrective Action Results | Service Caterers | `TC-015`, `TC-SEC-013`, `TC-E2E-001` |
| UC-168 | Record Corrective Actions Report | Medical Staff | `TC-017`, `TC-018` |
| UC-005 | Approval / Reject Corrective Action | School Administration | `TC-015`, `TC-016`, `TC-SEC-003` |
| UC-013 | Close Incident | School Administration | `TC-019`, `TC-020`, `TC-INT-003`, `TC-E2E-003` |
| UC-033 | Generate Incident Report | Medical Staff | `TC-021`, `TC-E2E-005` |
| UC-031 | Export Incident Reports | School Administration | `TC-SEC-001`, `TC-CON-001` |
| UC-114 | View Food Poisoning Incident List *(mở rộng 15/07/2026)* | School Admin, Medical Staff | `TC-003`, `TC-005`, `TC-006`, `TC-E2E-001` |
| UC-116 | View Food Safety Incident Detail | School Admin, Medical Staff | `TC-SEC-007`, `TC-INT-010`, `TC-E2E-002` |
| UC-120 | View Incident Information | Service Caterers | `TC-SEC-006`, `TC-SEC-014`, `TC-E2E-002` |
| UC-121 | View Incident Report | School Administration | `TC-E2E-005` |
| UC-179 | Record Staff Violation *(dialog hợp nhất)* | Medical Staff, Service Caterers | `TC-022`, `TC-023`, `TC-E2E-004` |
| UC-180 | Record Violation Resolution | Medical Staff, School Admin | `TC-024`, `TC-025`, `TC-INT-018`\* |
| UC-181 | View Violation Registry | School Admin, Medical Staff | `TC-027`, `TC-INT-020` |
| UC-182 | Approve/Reject Violation Resolution | School Administration | `TC-025`, `TC-E2E-004` |
| UC-183 | View Weekly Override Abuse Report | School Administration | `TC-026`, `TC-INT-021` |

*(\*) `TC-INT-018` cover phần liên kết mẫu; phần resolution của UC-180 nằm ở `TC-024`/`TC-025`.*
*(**UC-178** "Record Staff Violation" bản cũ đã **Deprecated 11/07/2026**, gộp vào UC-179 — không trích dẫn. **UC-188** đã Deprecated 26/07/2026, **UC-189** thuộc **MF13** — không TC nào cover ở đây, xem L30.)*

### 9.2. Business Rules

**Thuộc §1.11 (18 mã, có `BR-184` đã Deprecated):**
BR-146, BR-147, BR-148, BR-149, BR-150, BR-151, BR-152, BR-153, BR-154, BR-155, BR-183, **BR-184\***, BR-185, BR-186, BR-187, BR-189, BR-190, BR-211.

*(\*) **BR-184 Deprecated 26/07/2026** (gộp vào BR-183) nhưng **vẫn còn** ở cột BR của UC-179 trong §1.11 → trích để giữ vết, **không** TC nào hiện thực nó như một loại biên bản riêng (L13/L14).*

**Bắt buộc dùng nhưng KHÔNG có ở §1.11 (ghi rõ nguồn, chờ Huy bổ sung):**
- **BR-216** — vòng đời CAPA 6 trạng thái, 1 Incident = 1 CAPA (`business_rules.md` §11) → `TC-013`…`TC-017`, `TC-INT-008` — **L11**
- **BR-217** — cách ly → trả lô về NCC → bỏ mở khoá (§11) → `TC-011`, `TC-INT-002`, `TC-INT-017` — **L12**
- **BR-188** — audit log bất biến cho hồ sơ vi phạm (§18) → `TC-SEC-012` — **L13**

**BR trích chéo module (KHÔNG tính là BR của MF11):**
**BR-095/BR-097** (MF07 — CAPA đường sinh 2) → `TC-014` · **BR-113** (MF08 — thu hồi mẫu) → `TC-INT-018` · **BR-201** (MF03 — parent ≤ 5 HS) → `TC-004`, `TC-SEC-009` · **BR-013** (file ≤ 10MB) → `TC-SEC-013` · **BR-007** (RBAC 7 role) → `TC-SEC-001` · **BR-004** (phiên đăng nhập, MSG-004) → `IEM-004` · **BR-170** (audit chung) → `TC-SEC-012` · **BR-214/UC-189** (MF13 — MF11 chỉ cấp số liệu) → `TC-INT-020`.

**BR/UC đã Deprecated hoặc descope, KHÔNG hiện thực:** **BR-213/UC-188** (Deprecated 26/07) · **UC-187** (Deprecated) · **BR-105** (khoá bếp — descope 05/07, kéo theo MSG-024 và gate *"Health Department Clearance"* của UC-082 EX1) — `TC-CON-002` assert **không xuất hiện** trong spec.

### 9.3. System Messages

**Có MSG thật trong `error_message.md` v1.8 → assert `message` nguyên văn tiếng Anh (D7):**

| MSG | Nội dung (nguyên văn) | Mã MF11 | TC |
|---|---|---|---|
| MSG-004 | `Session expired. Please log in again.` | `IEM-004` | (MF01) |
| MSG-005 | `Access denied: You do not have permission to access this resource.` | `IEM-005` | `TC-004`, `TC-007`, `TC-019`, `TC-SEC-001…010` |
| MSG-008 | `File upload failed: File size exceeds the maximum limit of 10MB.` | — | `TC-SEC-013` |
| MSG-009 | `File upload failed: Unsupported file type. Only PDF, JPG, and PNG are allowed.` | — | `TC-SEC-013` |
| MSG-023 | `Incident report incomplete: Symptoms, date of ingestion, and dishes are required.` | `IEM-023` | `TC-002` |
| MSG-024\* | `Kitchen quarantine is active: Operation blocked due to safety violations.` | `IEM-024` | `TC-CON-002` (assert **không** phát ra) |
| MSG-029 | `Profile updated successfully.` | — | `TC-009`, `TC-019`, `TC-E2E-001` *(ngữ nghĩa sai — L16)* |
| MSG-048 | `Suspected food safety incident report logged. Investigation map generated.` | — | `TC-001` *(vế "map" không đúng ở RPT — L40)*, `TC-005` |
| MSG-049 | `Corrective actions plan submitted successfully for review.` | — | `TC-013`, `TC-E2E-001` |
| MSG-085 | `Violation record submission blocked: required fields (description, evidence photo) are missing.` | `IEM-085` | `TC-022` |
| MSG-086 | `Violation record saved successfully.` | — | `TC-023`, `TC-E2E-004` |
| MSG-087 | `Violation resolution submitted for approval.` | — | `TC-024`, `TC-025` |
| MSG-088 | `Possible repeat violation detected: staff name matches a prior violation within the lookback window. Manual verification required.` | `IEM-088` (warning, HTTP 200) | `TC-023` *(giữ nguyên văn dù nói "staff name" — L14)* |
| MSG-089 | `Violation resolution approved. Disciplinary action applied.` | — | `TC-025`, `TC-E2E-004` |
| MSG-095 | `Cannot close/submit this violation resolution: the signed 3-party record photo has not been uploaded yet. Please print, collect signatures, and upload the signed record first.` | `IEM-095` | `TC-024`, `TC-E2E-004` |
| MSG-100 | `Incident record draft saved successfully.` | — | `TC-E2E-001` |
| MSG-101 | `Incident record finalized successfully. Ready for review.` | — | `TC-005`, `TC-E2E-001` |
| **MSG-137** | `Resource not found.` | `IEM-404` | `TC-SEC-010` |
| **MSG-138** | `Validation failed: one or more required fields are missing or invalid.` | `IEM-001` | `TC-016`, `TC-INT-021` |
| **MSG-139** | `A record with this value already exists.` | *(D5 — MF11 chưa dùng)* | `TC-CON-005` |
| **MSG-141** | `Too many requests. Please try again later.` | *(chưa có mã cục bộ)* | `TC-SEC-011` |

*(\*) MSG-024 liệt kê để giữ vết §1.11 (UC-041) — **không endpoint MF11 nào phát ra** (L17).*

**✅ 10 mã cục bộ ĐÃ ĐƯỢC CẤP MSG (L7/L35 đóng 04/08 — `error_message.md` v1.11):** `IEM-114`→**MSG-149** · `IEM-147`→**MSG-150** · `IEM-149`→**MSG-151** · `IEM-149B`→**MSG-152** · `IEM-155`→**MSG-153** · `IEM-216`→**MSG-154** · `IEM-217`→**MSG-155** · `IEM-211`→**MSG-156** · `IEM-186`→**MSG-157** · `IEM-RO`→**MSG-158**. Thêm **MSG-159/160/161** thay `MSG-029` ở UC-009/005/013. ⇒ **assert `message` bật đầy đủ, không còn `@Disabled` nào vì thiếu oracle.**
*(Danh sách gốc v1.0 giữ lại bên dưới theo Policy 4.4.)*

~~**⚠️ 10 mã cục bộ CHƯA có MSG chính thức (L7/L35) — assert `message` để `@Disabled`, chờ Huy cấp:**~~
`IEM-114` (gộp RPT sai trạng thái) · `IEM-147` (severity ngoài enum) · `IEM-149` (thiếu lý do cách ly) · `IEM-149B` (cách ly lại) · `IEM-155` (gate đóng sự cố) · `IEM-186` (Y tế duyệt vi phạm ≥ MEDIUM) · `IEM-211` (tải bản ký khi chưa xuất PDF) · `IEM-216` (chuyển trạng thái CAPA sai) · `IEM-217` (lô chưa xử lý xong) · `IEM-RO` (hồ sơ đã khoá).
Nội dung hiện tại trong TDS §10 đang là **tiếng Việt** — khi được cấp MSG phải **dịch sang tiếng Anh** theo **D7** (giống cách v1.5 đã làm cho MF08/MF09).

**`IEM-429`** (HTTP 429) → **MSG-141** *(dùng chung mọi module, đã có từ v1.8 và ghi đích danh MF11 trong cột trigger — không xin mã mới)* — `TC-SEC-011`.

### 9.4. Luật `semantic-audit.ts` đã port (12 — tên **nguyên văn từ script**)

| # | Tên luật | §script | TC |
|---|---|---|---|
| 1 | `MAU_NGOAI_KHUNG_SU_CO` | §4 | `TC-INT-018` |
| 2 | `PHIEU_TRO_MAU_KHONG_TON_TAI` *(add-only, không có `check()`)* | §5 | `TC-INT-018` |
| 3 | `PHIEU_KHAC_SU_CO_VOI_MAU` | §5 | `TC-INT-018` |
| 4 | `BAO_CAO_TRUOC_BUA_AN` | §9 | `TC-INT-014` |
| 5 | `CACH_LY_LO_KHONG_LIEN_QUAN_BUA` | §9 | `TC-INT-017` |
| 6 | `SU_CO_TRO_BAO_CAO_KHONG_TON_TAI` *(add-only)* | §9 | `TC-INT-014` |
| 7 | `BAO_CAO_NGUON_KHAC_BUA` | §9 | `TC-INT-014` |
| 8 | `MOC_TRO_SU_CO_KHONG_TON_TAI` *(add-only)* | §9 | `TC-INT-015` |
| 9 | `MOC_TRUOC_KHI_SU_CO_XAY_RA` | §9 | `TC-INT-015` |
| 10 | `DONG_THOI_GIAN_LUI` | §9 | `TC-INT-015` |
| 11 | `CAPA_TRUOC_SU_CO` | §9 | `TC-INT-016` |
| 12 | `CAPA_GIAO_NCC_KHONG_TON_TAI` | §9 | `TC-INT-016` |
| 13 | `CACH_LY_KHONG_RO_SU_CO` | §9b | `TC-INT-017` |

> Bảng có **13 dòng** vì `CACH_LY_KHONG_RO_SU_CO` được gộp cùng nhóm cách ly; con số "12 luật" ở §3 TDS-02 đếm theo **nhóm nghiệp vụ**. Cả 13 tên đều copy **nguyên văn** từ `semantic-audit.ts`, không lấy từ TDS.
>
> ⚠️ **`DONG_SU_CO_MA_CON_LO_KHOA` — KHÔNG port.** Luật này giả định có bước nhả lô; **BR-217 (27/07/2026) đã bỏ hẳn**, nên `incident.quarantineBatches` **luôn còn phần tử** khi đóng sự cố ⇒ luật sẽ báo false-positive trên **mọi** sự cố đã đóng. Thay bằng bất biến *"lô ở trạng thái cuối bên MF06"* → `IEM-217` (`TC-INT-017`). **Thảo** cần cập nhật script (CF-M11-11/L21).

### 9.5. `00_Common_Conventions.md` — áp dụng và ghi đè

| D | Trạng thái với MF11 | TC canh |
|---|---|---|
| **D1** | ✅ TDS **đã đúng** (ADR-M11-5 là nguồn của D1) | `TC-INT-010` |
| **D2** | ✅ áp dụng; bổ sung yêu cầu `details` **luôn là mảng** | `TC-INT-012` |
| **D3** | ✅ TDS đã đúng | `TC-CON-001` |
| **D4** | ⚠️ **GHI ĐÈ** ADR-M11-5 (nửa sau) + **C12** + **§17.2 mục 12** | `TC-INT-011`, `TC-CON-003` |
| **D5** | ✅ không áp dụng (MF11 không nhận `id` từ client) — assert bề mặt | `TC-CON-005` |
| **D6** | ✅ 9 enum riêng của MF11 hợp lệ; ca `severity` NULL ↔ `"PENDING"` | `TC-008`, `TC-INT-012`, `TC-CON-004` |
| **D7** | ⚠️ **GHI ĐÈ** 10 message tiếng Việt ở §9.2/§10 — nhưng **thiếu MSG** để dịch (L7/L35) | `TC-INT-012` |
| **D8** | ⏳ chờ Nam; §9.2 **chưa có annotation** ⇒ 5 TC contract đỏ lần đầu | `TC-CON-001…005` |
| **D9** | ✅ TDS **đã đúng khuôn** (`created_by_id`) — D9 nêu tên MF11 trong nhóm "không cần sửa" | `TC-INT-013` |
| **D10** | ⚠️ **BỔ SUNG** — TDS §9 không nhắc `StorageService` dù MF11 có 3 luồng upload | `TC-SEC-013` |

### 9.6. Tổng kết coverage

| Nhóm | Số TC | CRITICAL | HIGH | MEDIUM |
|---|---|---|---|---|
| Unit | 27 | 10 | 12 | 5 |
| Security | 16 | 8 | 5 | 3 |
| Integration | 23 | 9 | 8 | 6 |
| Contract (D8) | 5 | 2 | 3 | 0 |
| E2E | 6 | 4 | 2 | 0 |
| **Tổng** | **77** | **33** | **30** | **14** |

**Đối chiếu constraint C1–C15 (mỗi constraint ≥ 1 TC — chống AP-AI-001):**

| C | TC | C | TC |
|---|---|---|---|
| C1 | `TC-027`, `TC-INT-020` | C9 | `TC-022`, `TC-023`, `TC-CON-005` |
| C2 | `TC-001`, `TC-003` | C10 | `TC-021`, `TC-E2E-005` |
| C3 | `TC-005`, `TC-006`, `TC-INT-023` | C11 | `TC-INT-006`, `TC-INT-016`, `TC-INT-022` |
| C4 | `TC-INT-001`, `TC-INT-004`, `TC-INT-015` | C12 | `TC-INT-009`, `TC-INT-011`, `TC-INT-021` |
| C5 | `TC-010`, `TC-011`, `TC-INT-002`, `TC-SEC-015` | C13 | `TC-009`, `TC-INT-005` |
| C6 | `TC-013`…`TC-017`, `TC-INT-008` | C14 | `TC-SEC-006`, `TC-SEC-010` |
| C7 | `TC-018`, `TC-019`, `TC-020`, `TC-E2E-003` | C15 | `TC-SEC-007`, `TC-SEC-008`, `TC-SEC-014` |
| C8 | `TC-024`, `TC-E2E-004` | | |

**Đối chiếu 34 unit + 18 integration + 12 E2E của TDS §13:** mọi `TC-U-*`, `TC-INT-*`, `TC-E2E-*` gốc đều được **mở rộng**, không viết lại — bảng ánh xạ nằm ở dòng `Oracle Source` của từng TC (VD `TC-U-002` → `MF11-TC-001`; `TC-INT-004` → `MF11-TC-INT-001`; `TC-E2E-003` → `MF11-TC-SEC-006`).

### 9.7. Việc cần người khác chốt (tổng hợp — gửi kèm khi review)

| # | Người | Việc | Logic Issue | Mức chặn |
|---|---|---|---|---|
| 1 | **Huy** | Cấp **10 MSG** cho mã cục bộ MF11 (`IEM-114/147/149/149B/155/186/211/216/217/RO`) + 1 mã cho HTTP **429** | L7, L35 | ⛔ chặn "Approved" |
| 2 | **Huy** | Bổ sung **BR-216**, **BR-217**, **BR-188** vào §1.11 (đang quyết định hành vi thật nhưng không có trong traceability) | L11, L12, L13 | ⛔ chặn "Approved" |
| 3 | **Huy** | Sửa ghi chú §1.11 mục 4: cửa sổ món nghi vấn **48h → 24h** theo BR-149 bản 27/07 | **L38** | ⚠️ cao — người đọc traceability sẽ hiện thực sai |
| 4 | **Huy** | Viết lại **UC-073/074** (bỏ tự tạo sự cố + tự cách ly) và **UC-082 EX1** (bỏ Health Department Clearance) | L15, L24 | ⚠️ |
| 5 | **Huy** | Đổi tên **UC-179** ("Record Staff Violation" → cấp đơn vị) + cập nhật **MSG-088**; gỡ **MSG-024** khỏi UC-041; cấp MSG riêng cho "CAPA approved"/"Incident closed"/"Severity assigned" thay MSG-029 | L14, L16, L17 | ⚠️ |
| 6 | **Nam** | Chốt vế phạm vi **Service Caterers** (CAPA **hoặc** lô cách ly) — ADR-M11-10 | L28 | ⛔ chặn code |
| 7 | **Nam** | Sửa bảng `SEARCH_FIELDS` §2b-3 cho MF11 (`description`/`reporterName` **không tồn tại**) | L18 | ⚠️ |
| 8 | **Mạnh** | Sửa **C12** (§17.1) và **§17.2 mục 12** cho khớp **D4** — đang bắt giữ `dd/MM/yyyy` qua `@JsonFormat` | **L4** | ⛔ chặn code (prompt sinh sai) |
| 9 | **Mạnh** | Sửa 3 mã ví dụ **không tồn tại** ở §9.2: `PTN-0007` → `CAT-001`, `PTN-0003`/"HTX Rau sạch Green Farm" → `SUP-003`/"Cty TNHH Thịt sạch Minh Phát", `STF-007` → `U-0003` | **L32, L34** | ⛔ chặn code |
| 10 | **Mạnh** | Quyết cách sửa **seed vi phạm bất biến**: `INC-2026-013` (event `CLOSED` vs `status`), `VIO-2026-004/005/006/009` (thiếu ảnh ký + `unit_sanction_level`) | **L36, L37** | ⛔ chặn `TC-INT-023` |
| 11 | **Thảo (MF05)** + Mạnh | Cấp **`Dish.code`** — hiện `Dish` trong seed **không có id**, `dish-ga-rang-gung` là mã bịa; MF11 §5.2 lại đòi `dish_id UUID NOT NULL` | **L33** | ⛔ chặn trục truy xuất |
| 12 | **Thảo (QA)** | Cập nhật `semantic-audit.ts`: bỏ/sửa **`DONG_SU_CO_MA_CON_LO_KHOA`** theo BR-217 | L21 | ⚠️ |
| 13 | **Minh** *(chính mình)* | PR mock FE: bỏ `useAppendIncidentEvent`, bỏ `useUpdate("inventory")`, chuyển `PUT` generic → endpoint nghiệp vụ, sửa `override-abuse` sang dẫn xuất, sửa 5 mô tả lạc hậu (F1–F8 §11.2b), thêm `lib/format.ts` cho ngày ISO | L19, L20, L29, L31, L4 | ⛔ merge backend trước khi có PR này là **màn hình vỡ** |
| 14 | **Nam** | Approve **D8** để nhóm contract test có hiệu lực nghiệm thu | L8 | ⚠️ |

---

**Hết tài liệu.** `SFMS-MF11-TDD-001` **v1.1** — **80 test case** *(v1.0: 77 · +`TC-CON-006` D11 · +`TC-030` D12 · +`TC-INT-024` port `M11-D08`)*, 41 Logic Issue *(L33 **đóng**, L32/L34 đóng bằng FK, L35 còn treo nhưng đã có bản soạn MSG-149…158)*, 13 luật audit port, **7 việc chờ chốt** *(TDS §2.5 — từ 14, sau khi §2.4 chốt 18 quyết định `M11-D01`…`M11-D18`)*.
