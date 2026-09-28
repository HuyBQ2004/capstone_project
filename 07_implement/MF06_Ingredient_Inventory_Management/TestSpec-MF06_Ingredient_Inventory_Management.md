# TEST-DRIVEN DEVELOPMENT SPECIFICATION
# MF06 — Ingredient & Inventory Management

**Document ID:** `SFMS-MF06-TEST-001`
**Version:** `1.1`
**Date:** `2026-08-03`
**Status:** ✅ **`Approved`** *(2026-08-03 — sau Bước 0 + Bước 1 của `02_Module_Completion_Workflow.md`)*
**Standard:** ISO/IEC/IEEE 29119-3:2021 — Software Testing Part 3: Test Documentation
**Author:** Bùi Quang Huy — Chủ module MF06, Lead Requirements & Traceability Reviewer
**Reviewed by:** `[ ]` Nguyễn Phương Thảo — Lead Test/QA Reviewer — Pending · `[ ]` Nguyễn Đức Mạnh — Backend & DB — Pending · `[ ]` Trần Tuấn Minh — FE↔BE contract — Pending
**Approved by:** `[ ]` Nguyễn Văn Nam — Final Approve — Pending
**Classification:** `Internal — Confidential`

**References (thứ tự ưu tiên khi xung đột — cao xuống thấp):**

| # | Nguồn | Vai trò trong tài liệu này |
|---|---|---|
| 0 | `07_implement/00_Common_Conventions.md` — **D1–D7 Approved 2026-07-30 · D9–D10 Approved 2026-07-31**, D8 🟡 đang áp dụng thực tế · **D11–D13** (`02_Module_Completion_Workflow.md` §6) 🟡 chờ Nam ký | Mọi expected value về **định dạng**: `id` = mã nghiệp vụ (D1), envelope lỗi (D2), path `/api/v1` (D3), ngày ISO-8601 (D4), `body.id` → 409 (D5), enum SCREAMING_SNAKE (D6), message tiếng Anh (D7), annotation OpenAPI S1–S8 (D8) |
| 1 | `07_implement/MF06_Ingredient_Inventory_Management/TDS-MF06_Backend.md` (SFMS-MF06-IMP-001) | **Nguồn chính**: §13 (xương sống kịch bản), §9 (request/response), §10 (mã lỗi), §16 (ma trận quyền), §5.2 (DDL + CHECK) |
| 2 | `02_Requirements/00_BusinessRules/business_rules.md` | Nội dung BR làm **oracle** nghiệp vụ |
| 3 | `02_Requirements/00_BusinessRules/error_message.md` **v1.5** | Nội dung MSG nguyên văn làm expected message |
| 4 | `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` **§1.5** | Mỗi test case truy được về ≥1 UC ID |
| 5 | `03_Design/08_FrontEnd/Web/scripts/semantic-audit.ts` | Luật nghiệp vụ port sang Gherkin — **tên luật copy nguyên văn từ script** |
| 6 | `03_Design/08_FrontEnd/Web/src/lib/mock-data.ts` | **CHỈ** nguồn seed/fixture (mã bản ghi thật). **Không** lấy định dạng ngày / giá trị enum từ đây |
| 7 | `07_implement/00_Team_Work_Assignment_Plan.md` §2b | Hợp đồng phân trang/search/filter |
| 8 | `03_Design/01_Diagrams/01_Class_Diagram/data_model.puml` | Ràng buộc **C6** (nối bằng khoá) · **C7** (hệ số thải bỏ) |

> **Quy ước TDD:** test viết TRƯỚC production code. Thứ tự: viết test → chạy → xác nhận **FAIL 🔴** (§5.1 Red Gate) → implement → **PASS 🟢** → refactor 🔵.
> Không mark ✅ nếu `mvn test` chưa xanh.
>
> **⚠️ Stack — bản dịch từ template.** `06_Document/Templates/PHASE-4_Test-Spec.md` minh hoạ bằng **TypeScript/Jest/Prisma**. Dự án dùng **Java 25 LTS / Spring Boot 4.1.x / JUnit 5 / AssertJ / MockMvc / Testcontainers PostgreSQL 16 / Flyway** (`01_MVP_Backend_Implementation_Plan.md` §Stack, ADR-000). Mọi ví dụ code dưới đây đã dịch sang Java/SQL; **không** còn `prisma.client.*`, `npm test`, `.spec.ts`.
> Test data dùng trường QA `TH Đoàn Thị Điểm` (`schoolId` seed). MF06 **không** chứa PII học sinh (§1 TDS — Data Classification `Internal`).

---

## CHANGELOG

> **Policy 4.4 — Immutable History:** không xóa thông tin cũ.

| Ngày | Người thực hiện | Nội dung thay đổi |
|------|-----------------|-------------------|
| **2026-08-03** | **Bùi Quang Huy** | **v1.1 — Bước 1 → Status `Approved`.** Thêm **§2.-1 với L9–L18**: 10 lỗi chỉ tìm được bằng cách **mở code đã merge** (`V1`…`V9` + `com.sfms.common/`), không mục nào rà tài liệu bắt được — xác nhận lại §1 của `02_Module_Completion_Workflow.md`. **Sáu trong mười làm migration fail hoặc build đỏ ngay lệnh đầu tiên:** `issue_slips.created_by UUID` đụng cột reserved của `BaseEntity` (**L9**, D9 gọi đích danh MF06) · FK trỏ `supplier_documents`/`supplier_products` — **hai bảng chưa bao giờ tồn tại** (**L10/L11**) · `catalog_ingredients` chưa có mà 5 bảng tham chiếu, và MF05 `V8:103` **đang chờ MF06 tạo** (**L12**, lỗi C3) · tiền tố `INV-` sai so với `ModuleCodeResolver` đã merge (**L14**) · Flyway `V12` + mô hình *"V1 = toàn schema"* đã bị bỏ, số trống thật là **V10** (**L16**). Thêm **9 TC** (88 → 97): nhóm G `INT-045`…`INT-051` neo vào L9–L18, trong đó **`INT-051` là TC bắt buộc D12** (field dẫn xuất + field đúng tên sai kiểu → assert **DB không đổi**, mẫu `TestSpec-MF05` TC-027) và **`SEC-014` là TC bắt buộc D11** (assert tập field **giống nhau** giữa 4 role + `items` đúng kiểu trong `/v3/api-docs` — MF06 không có ca đổi shape, đây là **test hồi quy** vì MF02/MF03 đều *về sau* mới thêm nhánh rút gọn); thêm `SEC-016` cho D10-BIS (ảnh bằng chứng ATTP là tài liệu **riêng tư**, `store()` trả URL công khai). **Đóng L6** — 9 mã chặn cứng nay có **MSG-145…MSG-153** (chờ Huy commit `error_message.md` v2.0); đồng thời phát hiện `error_message.md` **đã ở v1.9 chứ không phải v1.5**, nên 4 mã dùng chung (MSG-137/138/139/141) **assert nguyên văn được ngay**. L1–L5, L7, L8 chuyển từ *"test đi trước TDS"* thành ✅ *"TDS v1.1 đã sync"*. Đổi toàn cục: `INV-` → **`ING-`** (65 chỗ), `com.sfms.inventory` → **`com.sfms.mf06`**, `V12` → **`V10`/`V11`**. |
| 2026-07-30 | Bùi Quang Huy | Khởi tạo (v1.0). Sinh theo prompt mẫu `00_Team_Work_Assignment_Plan.md` **§2c-3b**, `{§ TRACE_SECTION}` = **§1.5** (đúng bảng §2c-0 — *không* phải §1.6). Mở rộng TDS §13: 16 unit → **23**, 26 integration → **44**, 9 security → **13**, thêm **6 contract test (D8)** và 2 E2E — tổng **88 test case**. Port **24 luật** `semantic-audit.ts` (TDS §13.0 ghi 19 — thiếu 4 luật §8 "Sổ chế biến" + 1 luật cross-module §9, xem **L7**). Ghi nhận **8 Logic Issue** ở §2, trong đó **L6 là oracle gap chặn nghiệm thu** (MF06 không được cấp MSG nào ở `error_message.md` v1.5). |

---

## MỤC LỤC

1. [Thông tin Module](#1-thông-tin-module) · 1.1 [AI Generation Context](#11-ai-generation-context-case-20)
2. [Logic Issues Resolved](#2-logic-issues-resolved)
3. [Test Design Specification](#3-test-design-specification-tds)
4. [Test Case Specification](#4-test-case-specification) — 4.1 Unit · 4.2 Integration · 4.3 Security · 4.4 Contract (D8) · 4.5 E2E
5. [Red-Green-Refactor Tracker](#5-red-green-refactor-tracker)
6. [Entry / Exit Criteria](#6-entry--exit-criteria)
7. [Rollback Plan](#7-rollback-plan)
8. [CASE 2.0 Anti-Pattern Detection](#8-case-20-anti-pattern-detection-ai-assisted-tcs)
9. [Danh sách mã đã trích dẫn](#9-danh-sách-mã-đã-trích-dẫn-đối-chiếu-nhanh-khi-review)

---

## 1. Thông tin Module

| Field | Value |
|-------|-------|
| **Feature / Module ID** | `MF06` — Ingredient & Inventory Management |
| **Module** | Procurement & Warehouse Traceability (Bounded Context — TDS §1) |
| **Spec gốc** | `SFMS-MF06-IMP-001` (`TDS-MF06_Backend.md`, 17 mục EDS v2.0) |
| **Priority** | 🟠 **P1** — trục xương sống (Team Plan §3) |
| **Chủ module** | Bùi Quang Huy |
| **Số UC phủ** | **21** (traceability §1.5 — nguồn chuẩn; UC file MF06 ghi 19, xem **CF-M06-1**) |
| **Data Classification** | `Internal` — hồ sơ lô hàng, giá hợp đồng, chứng chỉ NCC. **Không** chứa PII học sinh |
| **Compliance Scope** | Luật ATTP 55/2010/QH12 Điều 37 (hiệu lực chứng nhận) · QĐ 1246/QĐ-BYT (Bước 1 kiểm thực dùng lô của module này) |
| **Upstream Dependencies** | MF01 (JWT `userId`/`role`/`schoolId`) · MF02 (`school_config.geofence_radius` + toạ độ trường) · MF04 (`Partner.status`/`certExpiry`, `DeliveryPerson`) · MF05 (`Menu` đã công bố + BOM món) |
| **Downstream Consumers** | MF07 (Bước 1 kiểm thực) · MF08 (mẫu lưu ← món ← lô) · MF09 (phân suất theo món) · MF11 (bản đồ truy xuất + cách ly lô BR-149) · MF13 (báo cáo tỉ lệ nhận hàng đạt) |
| **Test Environment** | Testcontainers `postgres:16` · Flyway `V1`…`V9` (đã merge) + **`V10__mf06_ingredient_inventory.sql`** + **`V11__seed_mf06.sql`** · cờ `sfms.geofence.enforced=true` khi chạy test (ADR-M06-7) |

**Phạm vi 4 lớp test (bắt buộc đủ 4 theo §2c-3b):**

| Lớp | Số TC | Phạm vi | Nguồn |
|---|---|---|---|
| **Unit** (service, không DB) | **23** | `FefoAllocator`, `InventoryService`, `ReceivingService`, `DeliveryService`, `shelfStatus`/`isLocked`, quy đổi BOM + hệ số thải bỏ | TDS §13.1, §8.1 |
| **Integration** (endpoint → DB) | **44 + 7 🆕 = 51** | Transaction phiên nhận, phiếu xuất FEFO, sổ append-only, cách ly, phân trang/search/filter, **24 luật `semantic-audit.ts`** · 🆕 **nhóm G `INT-045`…`INT-051`** — hợp đồng với code đã merge (**L9–L18**), gồm **`INT-051` = TC bắt buộc D12** | TDS §13.2, §13.0, §9, §5.2 · §2.-1 |
| **Security** (RBAC 7 role) | **13 + 2 🆕 = 15** | Ma trận §16 TDS, cross-school / cross-supplier, gate nghiệp vụ gọi API trực tiếp bỏ qua UI · 🆕 **`SEC-014` = TC bắt buộc D11** · 🆕 **`SEC-016`** (D10-BIS — tài liệu riêng tư) | TDS §16, BR-007 · §6 Workflow |
| **Contract** (OpenAPI D8) | 6 | Đọc `/v3/api-docs`, assert D1/D2/D4/D6 + S3/S7 | `00_Common_Conventions.md` D8 |
| **E2E** | 2 | Trục xương sống đặt→nhận→xuất→chế biến→truy ngược; điều tra sự cố MF11↔MF06 | TDS §13.3 |
| | **88 → 97** | *(tổng — hậu tố `b`/`c` là ca cùng nhóm điều kiện, mỗi ca là một `@Test` độc lập)* | |

> **🆕 2026-08-03 — 9 TC thêm ở Bước 1.** Bảy TC nhóm G neo vào **L9–L18** (§2.-1): sáu trong bảy kiểm thứ làm **migration fail / build đỏ ngay lệnh đầu tiên** nếu code theo TDS v1.0. Hai TC còn lại là **bắt buộc theo §7 Bước 1 việc 3** — `SEC-014` (D11) và `INT-051` (D12); `grep -n "oneOf"` trên bản v1.0 ra **0 kết quả**, đúng như đợt đếm ngày 03/08 trên cả 13 TestSpec. MF06 là bộ thứ **2/13** có TC thật cho D11.

### 1.1 AI Generation Context (CASE 2.0)

| Field | Value |
|-------|-------|
| **AI Assisted?** | `Yes` |
| **Constraint Source** | `00_Common_Conventions.md` D1–D8 · `TDS-MF06_Backend.md` §9/§10/§13/§16/§5.2/§17 (C1–C9) · `00_Team_Work_Assignment_Plan.md` §2b + §2c-3b |
| **Constraints Injected** | (1) Expected value về định dạng **chỉ** theo D1–D7, **không** theo mock; (2) mọi expected value phải có ORACLE trích dẫn được (BR/UC/MSG) — thiếu thì ghi *"thiếu oracle"*, không bịa; (3) tên luật `semantic-audit.ts` copy **nguyên văn từ script**, không lấy từ TDS §13; (4) mock chỉ dùng làm seed/fixture; (5) bắt buộc đủ 4 lớp test + nhóm test "gọi API trực tiếp bỏ qua UI" cho GATE nghiệp vụ |
| **Model** | Claude Opus (theo §2c-4 — TestSpec là việc rủi ro cao, oracle sai là test vô nghĩa) |
| **Trust Level** | `T2 → T3` (pending Red Gate §5.1) |
| **Verification đã chạy khi sinh** | `traceability_matrix.md` §1.5 đối chiếu 21 UC ✅ · `error_message.md` v1.5 đối chiếu 16 MSG ✅ (phát hiện **L6**) · `semantic-audit.ts` grep toàn bộ 61 tên luật ✅ (phát hiện **L7**) · `mock-data.ts` đối chiếu 12 mã seed dùng làm fixture ✅ |

---

## 2. Logic Issues Resolved

### 2.-1 Đợt Bước 1 (2026-08-03) — **L9–L18**, tìm bằng cách MỞ CODE ĐÃ MERGE

> **Đây là vế thứ hai mà `02_Module_Completion_Workflow.md` §1 nói tới.** L1–L8 (dưới) tìm được bằng **rà tài liệu**. **10 mục dưới đây thì không** — chúng chỉ lộ ra khi mở `V1`…`V9` và `com.sfms.common/` ra đọc. Cùng kết luận mà MF05 rút ra (10/18 lỗi của họ cũng vậy).
>
> Toàn bộ đã được **sửa vào TDS v1.1 §2.4** dưới mã `M06-D01`…`M06-D16`. Bảng này ghi *test phải encode gì*.

| # | Spec gốc (TDS v1.0) | Sự thật trong code đã merge | Fix áp dụng trong test |
|---|---|---|---|
| **L9** | ⛔ **`issue_slips.created_by UUID NOT NULL REFERENCES user_accounts(id)`** — và entity khai `@Column(name="created_by") private UUID createdBy;` | `common/domain/BaseEntity` giữ **`created_by VARCHAR(180)`** (`@CreatedBy`, email actor, tự điền qua `AuditorAware`). Entity MF06 `extends BaseEntity` mà giữ tên cột này ⇒ **2 định nghĩa cho 1 cột ⇒ Hibernate duplicate column ⇒ build đỏ ngay lần chạy đầu**. **D9 gọi đích danh MF06** trong bảng "Việc phải làm ở từng TDS". | Đổi `issued_by_user_id` (M06-D02). Test DDL assert: mọi bảng MF06 có **đủ 4 cột** `created_at/updated_at/created_by/updated_by` đúng kiểu `TIMESTAMPTZ`/`VARCHAR(180)`, **và** không bảng nào có cột `created_by`/`updated_by` kiểu UUID. → `M06-TC-INT-045` |
| **L10** | ⛔ **`REFERENCES supplier_documents(id)`** (`batch_certificates`) | Grep `V1`…`V9`: bảng thật tên **`partner_documents`** (`V6:61`, PK UUID). `supplier_documents` chỉ là **tên resource** MF04 (`/api/v1/supplier-documents`) — **chưa bao giờ là bảng**. Cùng lớp lỗi **C2** mà MF07 (×9) / MF10 (×7) / MF12 (×5) mắc với `users(id)`. | FK → `partner_documents(id)`. Test migration chạy sạch từ DB trống là oracle (`M06-TC-INT-046`). |
| **L11** | ⛔ **`REFERENCES supplier_products(id)`** (`supplier_batches`) | **0 kết quả** trong `V1`…`V9` — MF04 (`V6`) không tạo bảng này. | MF06 tự tạo (M06-D03). Cùng TC với L10. |
| **L12** | ⛔ **`REFERENCES catalog_ingredients(id)`** ở 5 bảng, và §11.1 ghi *"MF05 đã có `catalog_ingredients`"* | **Sai** — `V8:103` ghi ngược lại: *"catalog_ingredients là bảng của MF06, **CHƯA TỒN TẠI** ⇒ lưu mã, KHÔNG khai FK. MF06 tự ALTER khi bảng của họ lên (Huy)"* (M05-D15). Đây là lỗi **C3** §2.1 Workflow, MF06 là chủ. | MF06 `CREATE TABLE catalog_ingredients` + `ALTER TABLE dish_ingredients ADD CONSTRAINT fk_dish_ing_catalog FOREIGN KEY (ingredient_code) REFERENCES catalog_ingredients(code)`. **FK trỏ `code`, KHÔNG trỏ `id`** — cột bên MF05 là `VARCHAR(64)`. Test: mọi `ingredient_code` trong `V9__seed_mf05.sql` tra được trong `catalog_ingredients` (thiếu 1 mã ⇒ ALTER fail). → `M06-TC-INT-047` |
| **L13** | **`code_sequences (school_id, prefix, year, next_value)`** ngầm định trong khuôn mã `PR-2026-0815` | `V1:31` — bảng thật là **`(scope VARCHAR(64) PRIMARY KEY, next_value BIGINT)`**, **phẳng**, không `school_id`/`year`. `CodeGenerator.next(scope, prefix, width)` (`INSERT … ON CONFLICT (scope)`) đang chạy cho MF01/02/03/04/05. Lỗi **C1** §2.1. | Nhét năm/ngày vào **chuỗi `scope`** theo tiền lệ MF03 (M06-D05). Test assert: sinh 2 mã liên tiếp trong cùng năm → số tăng 1; sang năm mới → đếm lại từ 1; **và** `code_sequences` vẫn đúng 2 cột (không module nào ALTER nó). → `M06-TC-INT-048` |
| **L14** | Tiền tố mã lỗi **`ING-`** (106 chỗ) | `common/error/ModuleCodeResolver.java` dòng cuối ghi sẵn: *"8 module còn lại đăng ký ở đây khi implement (**ING**, INS, SMP, SRV, FBK, INC, AIS, RPT)"* — nhóm **đã chọn `ING` cho MF06**. §0-BIS: code thật > tài liệu. | Mọi assertion `error.code` dùng **`ING-`** (M06-D09). Thêm TC: gọi path lạ của MF06 → `error.code` bắt đầu bằng `ING-`, **không** rơi về `DEFAULT_PREFIX = "IAM"`. → `M06-TC-INT-049` |
| **L15** | `package com.sfms.mf06` (7 chỗ, cả TDS lẫn TestSpec) | §0-BIS khuôn **`com.sfms.mf<NN>`**; `com.sfms.iam` là ngoại lệ lịch sử *"KHÔNG lấy làm mẫu"*. MF05 đã sửa cùng lỗi (`M05-D12`). | `com.sfms.mf06` ở mọi khai báo test (M06-D13). |
| **L16** | §11.1 *"Nam đã hoàn tất `V1__init_schema.sql` (toàn schema, gồm **12 bảng MF06**)"*; seed đặt ở `V12` | Mô hình *"V1 = toàn schema"* **đã bị bỏ** từ MF02/`V3` (§0-BIS). `V1` **không** có bảng nào của MF06. `V1`…`V9` đã merge ⇒ số trống là **V10**. Làm theo bản cũ = **sửa `V1` đã merge = Flyway đỏ trên máy cả nhóm**. | Testcontainers chạy `V1`…`V9` + **`V10`/`V11`** (M06-D14). Sửa mọi chỗ ghi `V12` trong tài liệu này. Thêm TC: `flyway_schema_history` không có dòng nào `Undone`/checksum mismatch. → `M06-TC-INT-050` |
| **L17** | `reject_photo_urls JSONB` lưu thẳng `"/uploads/rcv/2026-07-28/rej-4412-1.jpg"`; request gửi `rejectPhotoUrls` | **D10-BIS.2** xếp *"chứng nhận lô của MF06"* vào nhóm **RIÊNG TƯ**; `R2StorageService.store()` trả **URL công khai** (`cdn.sfsm.io.vn/<key>`) ⇒ dùng nó cho ảnh bằng chứng ATTP = ai có URL cũng xem được. **D10-BIS.3**: JSON tham chiếu file bằng **id tài liệu**, gửi tên file phải **422**. | Cột → `reject_photo_keys` (storageKey); request nhận `rejectDocumentIds` (M06-D15). Test: (a) response **không** chứa chuỗi `http`/`cdn.`/`/uploads/` ở bất kỳ field nào; (b) gửi `rejectDocumentIds: ["rej-4412-1.jpg"]` → **422**, không đoán ý. → `M06-TC-SEC-016` |
| **L18** | Không có TC nào cho **D11/D12** | Cả **13** TestSpec viết **trước khi** D11–D13 tồn tại ⇒ `grep -n "oneOf"` ra **0 kết quả**; MF05 phải **thêm mới** `TC-SEC-015`/`TC-027`. | Thêm **`M06-TC-SEC-014`** (D11, §4.3) và **`M06-TC-INT-051`** (D12, §4.2). |

> **Kết luận §2.-1:** 6/10 mục (**L9–L12, L14, L16**) làm **migration fail hoặc build đỏ ngay lệnh đầu tiên** nếu code theo TDS v1.0 — tức nổ vào mặt cả nhóm ở lần `mvn verify` đầu sau khi merge, không phải lỗi tìm được lúc review. Cả 6 đều bắt được bằng **2 lệnh grep của Bước 0 + mở file V ra đọc**.

### 2.0 Đợt sinh TestSpec (2026-07-30) — L1–L8

> **Bắt buộc điền trước khi viết test.** Test cases encode hành vi **đã sửa**, không phải hành vi trong spec gốc.
> 8 mục dưới đây là chỗ `TDS-MF06_Backend.md` (viết 2026-07-28) **lạc hậu so với** `00_Common_Conventions.md` (Approved 2026-07-30) hoặc so với nguồn được verify lại. Thứ tự ưu tiên §2c-0: `00_Common_Conventions.md` **>** TDS **>** BR/UC/MSG **>** mock.
>
> ✅ **Cập nhật 2026-08-03:** L1–L5, L7, L8 **đã được sửa vào TDS v1.1** (không còn là "TDS lạc hậu, test đi trước") — xem §2.4 của TDS. **L6 đã đóng** bằng 9 MSG mới, xem ghi chú ngay dưới bảng.

| # | Spec gốc (sai / thiếu / lạc hậu) | Thực tế (quy ước / nguồn đã verify) | Fix áp dụng trong test |
|---|---|---|---|
| **L1** | **TDS §5.2 dùng mã nghiệp vụ làm PK.** `inventory_batches.id VARCHAR(40) PRIMARY KEY`, `receiving_sessions.id VARCHAR(40) PRIMARY KEY`, tương tự `purchase_requests`, `deliveries`, `supplier_batches`, `preparation_records`; JPA khai `@Id @Column(length = 40) private String id;` | **D1** — `id` công khai là mã nghiệp vụ, **UUID là PK nội bộ**. Bảng "Việc phải làm ở từng TDS" của D1 nêu đích danh: *"MF05, MF06, MF07, MF12 — §5.2: đổi PK `VARCHAR` → `UUID` + tách cột `code`; giá trị `code` giữ nguyên như PK cũ"* | Assert DB: mỗi bảng MF06 có `id UUID PK` **và** `code VARCHAR(32) NOT NULL` + `UNIQUE(school_id, code)`. Assert JSON: `id` trả **mã nghiệp vụ** (`"BATCH-MP-8842"`), **không** UUID, **không** trả kèm field UUID nào. Path param là `{code}`. → `M06-TC-017`, `M06-TC-INT-027`, `M06-TC-CON-001` |
| **L2** | **`shelfStatus` giá trị thứ ba lạc hậu.** TDS §5.2 (`return ShelfStatus.ACTIVE;`), §17 C4 (`EXPIRED / EXPIRING_SOON ≤2 ngày / ACTIVE`), §9.2 ví dụ JSON (`"status": "ACTIVE"`) và Glossary đều ghi `ACTIVE` | **ADR-M06-2 đã tự sửa 2026-07-29** (review mục I4): giá trị thứ ba đổi `ACTIVE` → **`IN_STOCK`**, vì `STATUS_BADGE` của FE đã dùng khoá `ACTIVE` cho **trạng thái đối tác** với nhãn *"Hoạt động"* → lô còn hạn trả `ACTIVE` bị render sai ngữ cảnh kho **một cách im lặng**. TDS §5.2/§9.2/§17/Glossary **chưa** đồng bộ với chính ADR của nó | Expected là **`IN_STOCK`** ở mọi nơi. → `M06-TC-004`, `M06-TC-005`, `M06-TC-INT-028`. Ghi cho Huy: sửa 4 chỗ trong TDS (§5.2 dòng `return`, §17 C4, §9.2 ví dụ, Glossary) |
| **L3** | **TDS §9.2 ví dụ JSON còn `dd/MM/yyyy`.** `"deliveryDate": "28/07/2026 05:40"`, `"receivedDate": "05/07/2026"`, `"expiryDate": "08/07/2026"`, `"timestamp": "28/07/2026 13:47"`, `"quotedAt": "28/07/2026 09:15"`, `"at": "30/06/2026 14:35"`, `"serviceDate": "28/07/2026"` | **D4** — JSON ngày/giờ **luôn ISO-8601**: ngày `"2026-07-28"` (`LocalDate`), ngày giờ `"2026-07-28T05:40:00Z"` (`Instant`). D4 ghi đè cả `@JsonFormat` của ADR-M11-5. Mock **cố ý đóng băng** ở `dd/MM/yyyy` (Team Plan §0) — không phải nguồn định dạng | Mọi assertion ngày dùng **ISO-8601**. Thêm test riêng grep response: không field ngày nào khớp `^\d{2}/\d{2}/\d{4}`. → `M06-TC-018`, `M06-TC-INT-029`, `M06-TC-CON-004` |
| **L4** | **TDS §9.2 trộn nhãn tiếng Việt / chữ thường vào giá trị enum.** `"type": "Điều chỉnh"`, `"reasonCode": "spoilage"`, `"storageZone": "Chilled"` / `"Dry"`; §13 IT-M06-25 dùng query `not:storageZone=Dry`; mock `type: "Xuất kho (FEFO)"` | **D6** — *"tất cả SCREAMING_SNAKE trong JSON và DB; FE giữ bảng nhãn tiếng Việt ở `lib/labels.ts` — nhãn hiển thị không bao giờ là giá trị lưu trữ"*. Chính DDL §5.2 của TDS cũng khai `CHECK (type IN ('RECEIPT','ISSUE','ADJUSTMENT'))`, `CHECK (storage_zone IN ('DRY','CHILLED','FROZEN'))`, `CHECK (reason_code IN ('SPOILAGE',...))` — **ví dụ JSON tự mâu thuẫn với DDL cùng tài liệu** | Expected: `"type": "ADJUSTMENT"`, `"reasonCode": "SPOILAGE"`, `"storageZone": "CHILLED"`; query lọc `not:storageZone=DRY`. `MealType` dùng đúng 3 giá trị D6 `BREAKFAST`/`LUNCH`/`SNACK`. → `M06-TC-019`, `M06-TC-INT-010`, `M06-TC-INT-025`, `M06-TC-CON-003` |
| **L5** | **TDS §9.2 trả lượng/tiền dạng chuỗi đã format.** `"quantity": "40 kg"`, `"remaining": "17.2 kg"`, `"quantity": "-2 lít"`, `"totalValue": "9.800.000 ₫"` (chép shape mock) | Team Plan §5 Chuẩn dữ liệu: *"tiền/lượng = **number + đơn vị riêng**"*; TDS §5.2 lưu `NUMERIC(12,3)` + cột `unit VARCHAR(12)` tách riêng; `NUMERIC(14,2)` cho `total_value`. Format là việc của tầng render FE (`lib/format.ts`, D4 cùng nguyên tắc) | Expected: `initialQuantity: 40`, `unit: "kg"`, `remaining: 17.2`, `delta: -2`, `totalValue: 9800000`. **Không** assert chuỗi có dấu phân cách/ký hiệu ₫. → `M06-TC-006`, `M06-TC-INT-011`. Ghi cho Minh: `usePagedList` render qua `format.ts`, không đọc chuỗi sẵn |
| **L6** | ⛔ **ORACLE GAP — 9 mã lỗi chặn cứng của MF06 không có MSG.** TDS §10 ghi *"(dẫn xuất BR-xxx)"* cho `ING-070/071/072/076/078/149/202/204/205` và tự nhận ở ghi chú cuối mục: *"chưa có MSG chính thức… Đề nghị cấp MSG mới"* | **Đã kiểm `error_message.md` v1.5 (2026-07-29):** v1.5 cấp MSG-104–138 cho MF01/MF02/MF03/MF04/MF08/MF09/MF10 — **không** cấp mã nào cho MF06. Dòng §4 Tóm tắt của MF06 **không đổi**: `MSG-008, MSG-009, MSG-015, MSG-018, MSG-041, MSG-042, MSG-052, MSG-054, MSG-055, MSG-065`. Vậy 9 mã trên vẫn **không có** nội dung message được nguồn chuẩn chống lưng | **Phân tách oracle theo hai tầng:**<br>• **Có oracle** — *hành vi chặn* + *HTTP status* + *`error.code`*: BR-070/071/072/076/078/202/204/205 nguyên văn `business_rules.md` đều là chặn cứng, `code`/status lấy từ TDS §10 (hợp đồng module). ⇒ **assert bình thường**.<br>• **Thiếu oracle** — *nội dung `error.message`*: **KHÔNG assert nội dung** cho 9 mã này; chỉ assert `message` khác rỗng và là **tiếng Anh** (D7). Mọi TC liên quan mang nhãn `⚠️ ORACLE GAP (L6)`.<br>• Áp mã v1.5 **đã** dùng chung mọi module: `ING-404` → **MSG-137** *"Resource not found."*; lỗi DTO/enum/thiếu field → **MSG-138** *"Validation failed: one or more required fields are missing or invalid."* ⇒ 2 mã này **assert nguyên văn**.<br>→ Báo Huy/Nam: cần cấp MSG cho 9 mã hoặc chốt "dùng MSG-138 + `code` module là đủ". Đây là **điều kiện Exit §6** |
| **L7** | **TDS §13.0 port thiếu luật.** Bảng ghi *"19 luật liên quan MF06"*; §9 danh sách cuối tài liệu cũng liệt kê 19 | **Grep `semantic-audit.ts` (61 tên luật, verify 2026-07-30):** §8 *"Sổ chế biến"* có **4 luật** chạy trên `PREPARATION_RECORDS` + `INVENTORY` + `INVENTORY_TRANSACTIONS` — cả ba là resource **do MF06 sở hữu** (TDS §1: `preparation-records` thuộc MF06): `CHE_BIEN_SAI_NGAY_THUC_DON`, `SO_CHE_BIEN_TRO_LO_KHONG_TON_TAI`, `CHE_BIEN_DUNG_LO_HET_HAN`, `LO_CHE_BIEN_KHONG_CO_PHIEU_XUAT`. Thêm `CACH_LY_LO_KHONG_LIEN_QUAN_BUA` (§9 — chạy trên `INCIDENTS.quarantineBatches` × `INVENTORY_TRANSACTIONS`, cross-module MF11↔MF06, cùng loại với `DONG_SU_CO_MA_CON_LO_KHOA` mà TDS **đã** port) | TestSpec port **24 luật** (19 + 5). → `M06-TC-INT-018` … `M06-TC-INT-022`. Ghi cho Huy/Thảo: sửa TDS §13.0 từ 19 → 24 dòng |
| **L8** | **TDS §9 chưa có annotation D8.** §9.1/§9.2 chỉ có bảng path/method + ví dụ JSON; **không** dòng nào có `@Tag`/`@Operation`/`@ApiResponse`; §10 **không có** cột HTTP status khớp 1-1 `@ApiResponse` *(cột HTTP có, nhưng không có `@ApiResponse` tương ứng để khớp)* | **D8 (S1–S8)** yêu cầu §9.2 mỗi endpoint có `@Operation` với `description` chứa **UC ID + BR ID**, mỗi mã lỗi §10 có một `@ApiResponse` trỏ `ErrorResponse` dùng chung, `example` của `id` là mã nghiệp vụ, enum khai bằng kiểu thật. `00_Common_Conventions.md` D8 §"Việc phải làm ở từng TDS" nêu rõ **§13/TestSpec phải có ≥1 contract test** | Nhóm **§4.4 Contract (6 TC)** viết đủ. Theo đúng chỉ dẫn §2c-3b — *"Nếu endpoint của {MF} chưa có annotation, GHI RÕ là 'thiếu annotation D8' và báo lại"* — nhóm này mang nhãn **`⚠️ THIẾU ANNOTATION D8`**: test **vẫn viết trước** (TDD), sẽ FAIL tới khi TDS §9.2 + controller được bổ sung annotation. Đây là mục đích của Red Gate, không phải lỗi test |

**Kết luận §2 cho reviewer:** L1–L5 là **lệch tài liệu nội bộ** (TDS lạc hậu so với quy ước đã Approved) — test encode quy ước, TDS cần sync. **L6 là gap nguồn thật** — không tự vá bằng cách bịa message. L7–L8 là **thiếu phủ**, TestSpec bù.

> ### ✅ Cập nhật 2026-08-03 (Bước 1) — trạng thái L1–L8
>
> | # | Trạng thái | Ghi chú |
> |---|---|---|
> | L1 | ✅ **Đã sửa TDS** | §5.2 v1.1: 6 PK `VARCHAR(40)` → `UUID` + `code` + `UNIQUE(school_id, code)` (`M06-D01`). Test giữ nguyên assertion, nay **khớp** TDS. |
> | L2 | ✅ **Đã sửa TDS** | `ACTIVE` → `IN_STOCK` ở đủ 4 chỗ (§5.2 `return`, §9.2 ví dụ, §17 C4, Glossary) — `M06-D11`. |
> | L3 | ✅ **Đã sửa TDS** | §9.2 chuyển hết sang ISO-8601; `deliveryDate` đổi thành `receivedAt` vì tên cũ mô tả sai chính nó (`M06-D12`). |
> | L4 | ✅ **Đã sửa TDS** | Enum SCREAMING_SNAKE toàn §9.2 (`M06-D12`). |
> | L5 | ✅ **Đã sửa TDS** | Lượng/tiền là number + `unit`/`currency` riêng (`M06-D06`). |
> | **L6** | 🟡 **Đã có lời giải, chờ commit** | **`M06-D10`:** cấp **9 MSG mới — MSG-145…MSG-153** (không gộp vào MSG-004/005: theo D7 *"một `error.code` không mang hai nghĩa"*, đúng tiền lệ MSG-140 tách khỏi MSG-010 ở v1.7 và MSG-142 ở v1.9). Chuỗi tiếng Anh **đã có sẵn** ở §10 TDS ⇒ không phải soạn lại. **Còn lại:** Huy commit 9 dòng vào `error_message.md` v2.0. **Đồng thời phát hiện `error_message.md` nay đã ở v1.9, không phải v1.5** — 4 mã dùng chung mọi module đã tồn tại và TestSpec v1.0 chưa biết: `ING-404`→**MSG-137**, validate→**MSG-138**, `ING-409` (D5)→**MSG-139**, 429→**MSG-141**. Bốn mã này **assert nguyên văn ngay**, gỡ khỏi danh sách ORACLE GAP. |
> | L7 | ✅ **Đã sửa TDS** | §13.0/§17.2/Phụ lục B: 19 → **24 luật** (`M06-D16`). |
> | L8 | ✅ **Đã sửa TDS** | §9.1/§9.2/§10 đã mang ràng buộc D8; nhóm §4.4 Contract giữ nguyên nhưng **gỡ nhãn** `⚠️ THIẾU ANNOTATION D8` ở phần tài liệu — annotation nay là yêu cầu của TDS v1.1, test FAIL trước khi code là đúng Red Gate, không phải "spec thiếu". |
>
> **Ba mã lỗi đổi tiền tố:** mọi assertion `ING-xxx` trong tài liệu này đổi thành **`ING-xxx`** (`M06-D09`, theo `ModuleCodeResolver` đã merge). Xem **L14**.

---

## 3. Test Design Specification (TDS)

### TDS-01 — Scope / Phạm vi

```
MF06 — package com.sfms.mf06 (Spring Boot 4.1.x / Java 25)
├── domain/        InventoryBatch, InventoryTransaction, ReceivingSession, ReceivingLine,
│                  IssueSlip, PurchaseRequest, Delivery, SupplierBatch, PreparationRecord
│                  + QuarantineInfo (@Embeddable) + enum riêng module (StorageZone,
│                  TransactionType, ReasonCode, ReceivingStatus, IssueSlipStatus, ShelfStatus)
│                  → Unit test thuần: shelfStatus(), isLocked(), remaining()
├── service/       FefoAllocator, InventoryService, ReceivingService, IssueSlipService,
│                  PurchaseRequestService, DeliveryService, SupplierBatchService,
│                  PreparationRecordService, GeofenceValidator, BomQuantityCalculator
│                  → Unit test với port mock (@Mock PartnerStatusPort/MenuPort/FaceRecognitionPort)
├── web/           7 controller REST /api/v1/... (§9.1 — 30 endpoint)
│                  → Integration test qua MockMvc + Testcontainers
└── (ngoài phạm vi module) common/: PagedResponse, ErrorResponse, @RestControllerAdvice,
                           common/enums/ (MealType D6), OpenApiConfig — Nam sở hữu

Tầng phủ:
  Unit        — service + domain, KHÔNG DB, KHÔNG Spring context (@ExtendWith(MockitoExtension))
  Integration — @SpringBootTest + MockMvc + Testcontainers postgres:16 + Flyway V1…V9 + V10 & V11
  Security    — @SpringBootTest + JWT 7 role, kiểm §16
  Contract    — @SpringBootTest(webEnvironment=RANDOM_PORT), GET /v3/api-docs, assert D1/D2/D4/D6
  E2E         — chuỗi nhiều endpoint trong 1 test class, assert nối bằng KHOÁ (C6)
```

**Ngoài phạm vi (ghi rõ để reviewer không tính là thiếu phủ):**

| Không test ở MF06 | Lý do | Thuộc về |
|---|---|---|
| Biểu mẫu Bước 1 / Bước 2 kiểm thực (Mẫu số 1/2 — QĐ 1246) | UC-062/UC-063 dùng chung; **biểu mẫu thuộc MF07**, MF06 chỉ cấp dữ liệu lô + `PreparationRecord.batchesUsed` (CF-M06-1/CF-M06-2) | MF07 (Thảo) |
| Ngưỡng nhiệt độ lõi 75°C (BR-090), MSG-017 | Miền kiểm thực Bước 2 — MF06 không phát mã này (TDS §10 ghi chú 3) | MF07 |
| Cảnh báo nhiệt độ kho tự động, **MSG-055** | Gắn **BR-089 — Deprecated 11/07/2026**, và không có trong traceability §1.5 → **không implement** (CF-M06-3) | — (không làm) |
| Ngưỡng `$5.000` dual authorization (UC-021 UC file) | **Không BR nào** quy định; mock dùng vòng đời duyệt giá 1 vòng (CF-M06-5) | — (không làm) |
| Đăng ký sinh trắc tài xế, hiệu lực chứng nhận NCC | BR-046/047/048/050/051 thuộc **MF04**; MF06 gọi qua `PartnerStatusPort`/`FaceRecognitionPort` bằng **ID** (CF-M06-7/8) — MF06 chỉ test **phía tiêu thụ port** | MF04 (Mạnh) |
| Ngưỡng lệch giao hàng 30′ như **luật** | Không BR chống lưng (BR-135 khác miền — CF-M06-6) → test như **tham số cấu hình** `delivery.deviation-alert-minutes` | — |

### TDS-02 — Test Basis / Cơ sở Kiểm thử

| Source | Items Derived |
|--------|--------------|
| `traceability_matrix.md` **§1.5** (21 UC) | UC-015, UC-021, UC-039, UC-043, UC-044, UC-045, UC-060, UC-061, UC-062, UC-063, UC-064, UC-066, UC-072, UC-081, UC-083, UC-092, UC-100, UC-107, UC-109, UC-123, UC-139 |
| `business_rules.md` §6 (miền MF06) | **BR-070** NCC Approved khi nhận · **BR-071** chứng nhận lô đầy đủ · **BR-072** ảnh bằng chứng · **BR-073** GPS/thiết bị/timestamp tự động · **BR-074** khu vực nhận hàng · **BR-075** FEFO · **BR-076** truy xuất lô ↔ thực đơn/phiên sản xuất · **BR-077** cảnh báo thiếu kho · **BR-078** từ chối lô + lý do + ảnh · **BR-202** khớp khu nhiệt độ · **BR-204** mã lý do + chữ ký quản lý · **BR-205** khoá lô hết hạn |
| `business_rules.md` §8 (trích cho UC-062/063) | BR-079, BR-083, BR-090, BR-092, BR-093, BR-095, BR-096 — *chỉ test phần dữ liệu lô MF06 cấp* |
| `business_rules.md` khác | **BR-007** RBAC 7 role · **BR-028** geofence + dải toạ độ hợp lệ (MF02) · **BR-149** cách ly lô theo lựa chọn Medical Staff (MF11) · **BR-170** audit trail |
| `error_message.md` **v1.5** | MSG-004, MSG-005, MSG-008, MSG-009, MSG-013, MSG-014, MSG-015, MSG-016, MSG-018, MSG-025, MSG-029, MSG-041, MSG-042, MSG-052, MSG-054, MSG-065, **MSG-137**, **MSG-138** |
| `00_Common_Conventions.md` | D1 (định danh) · D2 (envelope) · D3 (`/api/v1`) · D4 (ISO-8601) · D5 (client `id` → 409) · D6 (enum SCREAMING_SNAKE) · D7 (message tiếng Anh) · D8/S1–S8 (OpenAPI) |
| `TDS-MF06_Backend.md` | §5.2 (9 CHECK + 1 UNIQUE) · §9 (30 endpoint) · §10 (22 mã lỗi) · §13 (kịch bản xương sống) · §16 (ma trận quyền) · §17 C1–C9 (constraint) · ADR-M06-1..7 |
| `semantic-audit.ts` | **24 luật** (xem TDS-03 bảng B) |
| `data_model.puml` | **C6** truy xuất nối bằng KHOÁ · **C7** lượng thực mua = ăn được ÷ (1 − hệ số thải bỏ), chỉ cho khối lượng/thể tích |
| `00_Team_Work_Assignment_Plan.md` §2b | Shape phân trang 5 field · 9 bước thứ tự lọc · `SEARCH_FIELDS` · edge case clamp/rỗng |

### TDS-03 — Test Conditions and Coverage Items

**Bảng A — điều kiện nghiệp vụ theo BR/ADR:**

| Condition ID | Test Condition | Coverage Item | Test Cases |
|---|---|---|---|
| `COND-070` | Phiên nhận bị từ chối nếu NCC không `APPROVED` tại thời điểm mở phiên | `ReceivingService.record()` · `PartnerStatusPort` | `M06-TC-013`, `INT-002`, `SEC-010` |
| `COND-071` | Lô phải đủ số lô/NSX/HSD/số lượng; `HSD > NSX`; `đóng gói ≥ NSX` | `SupplierBatchService.create()` · `ck_sb_exp_after_mfg`, `ck_sb_pack_after_mfg` | `INT-014`, `INT-015` |
| `COND-072` | Phiên đã chốt phải có ≥1 ảnh bằng chứng | `record()` · `ck_rcv_photos` | `M06-TC-012b`, `INT-003` |
| `COND-073` | `receivedAt` + `deviceInfo` do **server** ghi, không nhận từ client | `record()` · ADR-M06-7 | `SEC-009`, `INT-030` |
| `COND-074` | Nhận hàng ngoài geofence trường bị chặn | `GeofenceValidator` | `M06-TC-020`, `INT-004` |
| `COND-075` | FEFO: lô HSD sớm nhất xuất trước | `FefoAllocator.allocate()` | `M06-TC-001`, `INT-007` |
| `COND-076` | Dòng `ISSUE` neo được về phiếu xuất + món + thực đơn bằng **khoá** | `IssueSlipService.issue()` · `ck_tx_issue_trace` | `M06-TC-015`, `INT-008`, `INT-016` |
| `COND-077` | Thiếu tồn → `shortfall` + cảnh báo, **không** tự xuất lô khoá | `issuePlan()` | `M06-TC-002`, `INT-011` |
| `COND-078` | Từ chối lô phải có lý do **và** ≥1 ảnh | `record()` · `ck_line_reject` | `M06-TC-012`, `INT-005` |
| `COND-202` | Khu lưu trữ phải khớp nhóm nhiệt độ của nguyên liệu | `InventoryService.storeBatch()` | `M06-TC-011`, `INT-010` |
| `COND-204` | `ADJUSTMENT` phải có `reasonCode` **và** `managerSignature` | `InventoryService.adjust()` · `ck_tx_adjustment` | `M06-TC-009`, `M06-TC-010`, `INT-012` |
| `COND-205` | Lô hết hạn bị **loại khỏi tập ứng viên** xuất kho | `isLocked()` · `FefoAllocator` | `M06-TC-002`, `M06-TC-005`, `INT-017` |
| `COND-149` | Lô đang cách ly không xuất/không điều chỉnh được; gỡ cách ly qua `DELETE` | `quarantine` value object · ADR-M06-2 | `M06-TC-003`, `INT-013`, `INT-023`, `INT-024` |
| `COND-LEDGER` | Sổ giao dịch **append-only**; tồn là số dẫn xuất | ADR-M06-1 | `M06-TC-006`, `INT-009` |
| `COND-ATOMIC` | Phiên nhận là **một** transaction; UPSERT theo mã lô | ADR-M06-5 | `INT-001`, `INT-006` |
| `COND-DUP` | Một (trường, ngày, bữa) chỉ có **một** phiếu xuất | `uq_slip_school_date_meal` | `INT-008b` |
| `COND-C7` | Lượng thực xuất = ăn được ÷ (1 − hệ số thải bỏ), **chỉ** kg/lít | `BomQuantityCalculator` | `M06-TC-007`, `M06-TC-008` |
| `COND-PAGE` | Shape phân trang bất biến 5 field, clamp, rỗng → 200 | Base controller + `Specification` | `INT-025` … `INT-029` |
| `COND-RBAC` | 7 role × endpoint đúng §16; scope theo JWT | `@PreAuthorize` + `Specification` | `SEC-001` … `SEC-013` |
| `COND-D8` | Spec `/v3/api-docs` không tái phát lệch D1/D2/D4/D6 | `OpenApiConfig` + annotation controller | `CON-001` … `CON-006` |

**Bảng B — 24 luật `semantic-audit.ts` port sang test** *(tên **copy nguyên văn từ script**, không lấy từ TDS §13 — theo §2c-3b)*:

| # | Luật (nguyên văn script) | Mục script | Tầng test | BR | Test Case |
|---|---|---|---|---|---|
| 1 | `XUAT_LO_HET_HAN` | §1 | Unit + Integration | BR-205 | `M06-TC-002`, `INT-017` |
| 2 | `XUAT_TRUOC_KHI_NHAP` | §1 | Integration | BR-076 | `INT-017b` |
| 3 | `PHIEU_SAI_NGUYEN_LIEU_CUA_LO` | §1 | Unit + DB invariant | BR-076, C6 | `M06-TC-015`, `INT-016` |
| 4 | `NHAP_LO_DA_HET_HAN` | §2 | DB CHECK `ck_batch_exp_after_recv` | BR-071 | `INT-014b` |
| 5 | `CACH_LY_KHONG_RO_SU_CO` | §9b | Integration | BR-149 | `INT-023` |
| 6 | `DONG_SU_CO_MA_CON_LO_KHOA` | §9b | Integration (MF11 ↔ MF06) | BR-149 | `INT-024b` |
| 7 | `LO_TRO_MAT_HANG_KHONG_TON_TAI` | §10 | DB FK | BR-071 | `INT-015b` |
| 8 | `HSD_TRUOC_NGAY_SX` | §10 | DB CHECK `ck_sb_exp_after_mfg` | BR-071 | `INT-014` |
| 9 | `DONG_GOI_TRUOC_KHI_SX` | §10 | DB CHECK `ck_sb_pack_after_mfg` | BR-071 | `INT-015` |
| 10 | `TEN_LO_KHAC_MAT_HANG` | §10 | Integration | BR-071, C6 | `INT-016b` |
| 11 | `GIAO_TRUOC_KHI_DAT` | §10 | Unit | BR-071 | `M06-TC-014` |
| 12 | `CHUYEN_GIAO_LO_KHAC_NCC` | §10 | Integration | BR-071 | `INT-016c` |
| 13 | `MUA_HANG_SAU_KHI_GIAY_TO_HET_HAN` | §10 | Integration (port MF04) | BR-046/048 | `INT-031` |
| 14 | `LO_KHONG_CO_PHIEU_NHAN` | §10b | Integration | BR-076 | `INT-020b` |
| 15 | `NHAN_LO_TU_PHIEU_BI_TU_CHOI` | §10b | Integration | BR-078 | `INT-005b` |
| 16 | `NGAY_NHAP_LO_KHAC_PHIEU_NHAN` | §10b | Integration | BR-076 | `INT-006b` |
| 17 | `NCC_LO_KHAC_NCC_PHIEU_NHAN` | §10b | Integration | BR-076 | `INT-006c` |
| 18 | `NHAN_HANG_SAU_KHI_CHUNG_NHAN_HET_HAN` | §10c | Integration | BR-070 | `INT-002b` |
| 19 | `PHIEU_GHI_SAI_TRANG_THAI_NCC` | §10c | Integration | BR-070 | `INT-002c` |
| **20** | `CHE_BIEN_DUNG_LO_HET_HAN` ⭐ | §8 | Integration | BR-076, BR-205 | `INT-018` |
| **21** | `LO_CHE_BIEN_KHONG_CO_PHIEU_XUAT` ⭐ | §8 | Integration | BR-076 | `INT-019` |
| **22** | `SO_CHE_BIEN_TRO_LO_KHONG_TON_TAI` ⭐ | §8 | DB FK + Integration | BR-076, C6 | `INT-020` |
| **23** | `CHE_BIEN_SAI_NGAY_THUC_DON` ⭐ | §8 | Integration | BR-076 | `INT-021` |
| **24** | `CACH_LY_LO_KHONG_LIEN_QUAN_BUA` ⭐ | §9 | Integration (MF11 ↔ MF06) | BR-149 | `INT-022` |

⭐ = **5 luật TDS §13.0 chưa port** (xem **L7**).

> **⚠️ Quy tắc đọc kết quả — bắt buộc mọi test nhóm luật:** script ghi rõ *"mỗi luật đếm số dòng THỰC SỰ kiểm được. Luật kiểm 0 dòng là luật **HỎNG** (thường do đổi tên trường), không phải luật sạch — script thoát với mã lỗi trong cả hai trường hợp."*
> ⇒ Mỗi test port luật phải có **hai** assertion, đúng thứ tự: (1) `assertThat(rowsChecked).isGreaterThan(0)` — có dữ liệu để kiểm; (2) `assertThat(violations).isEmpty()`. Bỏ assertion (1) là test **luôn xanh** dù seed đổi tên cột → chính là AP-AI-002 Green-from-Birth.

### TDS-04 — Test Techniques / Kỹ thuật Kiểm thử

| Technique (ISO 29119-4) | Applied To | Rationale |
|---|---|---|
| **Equivalence Partitioning** | `shelfStatus`: {quá hạn} / {≤2 ngày} / {>2 ngày}; `storageZone` × nhóm nhiệt độ nguyên liệu {khớp, lệch}; `TransactionType` × dấu `delta` | 3 lớp tương đương của hạn dùng là toàn bộ miền BR-205; nhiều hơn là dư |
| **Boundary Value Analysis** | Ngưỡng `EXPIRING_SOON` **≤ 2 ngày** (`today+1`, `today+2`, `today+3`); geofence **200m** (`199m`, `200m`, `201m`, `640m`); `photosCount` (`0`, `1`); `pageSize` (`0`, `1`, `8`); `page > pageCount` | Biên là chỗ ADR-M06-2 / BR-074 / §2b-6 dễ off-by-one nhất; `today+2` phải là `EXPIRING_SOON` (dùng `!expiryDate.isAfter(today.plusDays(2))`), `today+3` phải là `IN_STOCK` |
| **State Transition Testing** | `PurchaseRequest`: `PENDING → QUOTED → PRICE_ACCEPTED → APPROVED → COMPLETED` + `CANCELLED`(bắt buộc `cancelReason`)/`REJECTED` (§6.4); `Delivery` 7 trạng thái (§6.5); `ReceivingSession` 4 trạng thái; `IssueSlip` `DRAFT → CONFIRMED → POSTED`; **hai trục độc lập** của lô: `shelfStatus` × `quarantine` (§6.6) | Chuyển trạng thái sai là nguồn của `ING-409`; ma trận 4 tổ hợp hạn × cách ly là lý do ADR-M06-2 tách trục |
| **Decision Table Testing** | BR-204: (`reasonCode` có/không) × (`managerSignature` có/không) → 4 ô, chỉ 1 ô hợp lệ; BR-078: (`rejected`) × (`reason`) × (`photos`) | BR-204/078 nguyên văn là "thiếu **một trong hai** → chặn" — bảng quyết định là cách phủ đúng ngữ nghĩa "một trong hai" |
| **Data-flow / Invariant Testing (SQL)** | 24 luật `semantic-audit.ts` port thành truy vấn `JOIN` trên DB thật | Loại lỗi mục tiêu: *"khoá tồn tại, trỏ đúng bảng, nhưng trỏ vào **dòng sai**"* — chỉ bắt được bằng truy vấn chéo dòng, không bắt được bằng unit test |
| **Error Guessing / Attack** | Gọi API trực tiếp bỏ qua UI (CF-M06-12: mock chỉ kiểm ở FE); giả mạo `schoolId`/`supplierId`/`receivedAt` trong body; `PUT`/`DELETE` sổ giao dịch; gỡ cách ly bằng `PUT {quarantine:null}`; upload `.exe` | CF-M06-12 nói rõ **mọi** BR của MF06 hiện chỉ được kiểm trong component FE ⇒ gọi API trực tiếp là lách được toàn bộ luật ATTP. §2c-3b xếp đây là nhóm lỗi nghiêm trọng nhất của review 29/07 |
| **Concurrency Testing** | 2 phiếu xuất song song cùng nguyên liệu (`PESSIMISTIC_WRITE`); 2 phiên nhận cùng mã lô (UPSERT) | ADR-M06-4/M06-5 nêu đích danh hai ca đua này; hậu quả là trừ/cộng tồn hai lần |
| **Contract / Schema Testing** | `GET /v3/api-docs` assert D1/D2/D4/D6 | D8: thay phần lớn checklist tay, chặn lệch quay lại qua file type FE sinh tự động (`openapi-typescript`) |

### TDS-05 — Test Data Requirements

> **Nguồn fixture:** `mock-data.ts` (nguồn số 6 — **chỉ** mã bản ghi thật, để test chạy trên dữ liệu có thật).
> **⚠️ Chuyển đổi bắt buộc:** seed mock ghi `dd/MM/yyyy` và enum nhãn tiếng Việt vì **cố ý đóng băng** (Team Plan §0). Bước sinh `V11__seed_mf06.sql` chuyển **một lần** sang ISO-8601 + SCREAMING_SNAKE (D4/D6). Cột "Giá trị (sau chuyển đổi D4/D6)" dưới đây là giá trị **test dùng**.
> **⚠️ Bẫy đã biết khi sinh seed** (D4 ghi chú): `paginate()` của mock parse ngày bằng regex `^(\d{2})\/(\d{2})\/(\d{4})`. Chuyển seed sang ISO mà quên sửa chỗ này thì lọc `dateFrom`/`dateTo` **im lặng trả rỗng** — không lỗi, không cảnh báo. `M06-TC-INT-026` tồn tại để bắt đúng ca này.

| Fixture ID | Type | Giá trị (sau chuyển đổi D4/D6) | Mục đích |
|---|---|---|---|
| `FX-BATCH-OK` | DB seed | `BATCH-MP-8842` · `ingredientId=ING-THIT-LON-VAI` · `initialQuantity=40` `unit=kg` · `storageZone=CHILLED` · `receivedDate=2026-07-05` · `expiryDate=2026-07-08` · `quarantine=null` | Lô còn hạn, khu đúng — happy path FEFO, `shelfStatus=IN_STOCK` |
| `FX-BATCH-FEFO-2` | DB seed | `BATCH-GF-4411` · `ING-THIT-LON-VAI` · `45 kg` · `CHILLED` · nhận `2026-07-02` · HSD `2026-07-05` | Lô **thứ hai cùng nguyên liệu, HSD sớm hơn** → oracle thứ tự FEFO (BR-075) |
| `FX-BATCH-EXPIRED` | DB seed | `BATCH-GF-4388` · `ING-DAU-AN` · `40 lít` · `DRY` · nhận `2026-06-20` · HSD `2026-06-28` | Lô **quá hạn** — phải bị loại khỏi tập ứng viên (BR-205). Seed cố ý giữ EXPIRED để demo |
| `FX-BATCH-USABLE-OIL` | DB seed | `BATCH-GF-4420` · `ING-DAU-AN` · `36 lít` · `DRY` · nhận `2026-07-01` · HSD `2027-01-01` | Lô dầu **còn hạn** — nếu không có, mọi món rán/xào báo thiếu và không kiểm được luồng bình thường |
| `FX-BATCH-QUAR-INC` | DB seed | `BATCH-MP-8820` · `ING-THIT-GA` · `25 kg` · `CHILLED` · nhận `2026-06-29` · HSD `2026-07-02` · `quarantine={at:2026-06-30T14:35:00+07:00, by:<medical-staff-uuid>, reason:"Nghi lô gà không đạt nhiệt độ lõi khi chế biến — phục vụ điều tra INC-2026-014 (BR-149).", incidentId:"INC-2026-014"}` | Cách ly **có** `incidentId` — hợp lệ với `CACH_LY_KHONG_RO_SU_CO`; đồng thời `EXPIRED` ⇒ ca `isLocked` do **cả hai** trục |
| `FX-BATCH-QUAR-NOINC` | DB seed | `BATCH-DD-1080` · `ING-TOM-SU` · `18 kg` · `FROZEN` · nhận `2026-06-21` · HSD `2026-09-28` · `quarantine={reason:"Nhà cung cấp Đại Dương bị đình chỉ — giữ lô chờ kết luận.", incidentId:null}` | Cách ly **không** `incidentId` nhưng lý do **tự giải thích** (khớp `/đình chỉ|hết hạn|thu hồi/i`) ⇒ **hợp lệ**. Lô còn hạn mà vẫn `isLocked` ⇒ ca cách ly thuần (BR-149) |
| `FX-BATCH-COUNT-UNIT` | DB seed | `BATCH-MP-8825` · `ING-TRUNG-CUT` · `1200` `unit=quả` · `CHILLED` · HSD `2026-07-13` | Đơn vị **đếm** — chứng minh **không** áp hệ số thải bỏ (C7), dù catalog có `refuseFactor=0.18` |
| `FX-SUP-APPROVED` | DB seed | `SUP-001` Cty CP Thực phẩm sạch GreenFarm · `status=APPROVED` · `certExpiry=2027-03-15` | NCC hợp lệ — happy path BR-070 |
| `FX-SUP-SUSPENDED` | DB seed | `SUP-004` Cty CP Hải sản Đại Dương · `status=SUSPENDED` · `certExpiry=2026-06-22` · `suspendReason` có thật trong seed | Chặn phiên nhận (BR-070) **và** ca nhận hàng sau khi chứng nhận hết hiệu lực |
| `FX-SUP-EXPIRING` | DB seed | `SUP-003` Cty TNHH Thịt sạch Minh Phát · `APPROVED` · `certExpiry=2026-08-05` `certStatus=EXPIRING` | Ranh giới: `APPROVED` nhưng chứng nhận sắp hết — **vẫn nhận được** (BR-070 chỉ xét `status`) |
| `FX-DLV-DELIVERED` | DB seed | `DLV-2026-1105` → `PR-2026-0815` · `scheduledAt=2026-07-02T05:40:00+07:00` · `status=DELIVERED` · `linkedBatches=[BATCH-GF-4411, BATCH-GF-4412, BATCH-GF-4410]` | Chuyến giao đủ điều kiện mở phiên nhận |
| `FX-DLV-SCHEDULED` | DB seed | `DLV-2026-1104` → `PR-2026-0812` (`PENDING`) · `scheduledAt=2026-07-03T05:30:00+07:00` · `SCHEDULED` | Chuyển trạng thái giao hàng (UC-092) + ca lệch giờ → MSG-065 |
| `FX-SB-OK` | DB seed | `BATCH-GF-4411` · `productId=SIP-072` · `lotNumber=GF-TL-260702` · NSX `2026-07-02` · đóng gói `2026-07-02` · HSD `2026-07-05` · `certificates=[DOC-GF-01]` | Lô NCC hợp lệ BR-071 (biên: đóng gói **=** NSX phải PASS vì CHECK là `>=`) |
| `FX-RCV-OK` | DB seed | `RCV-20260702-02` · `supplier=HTX Rau an toàn Vân Nội` · `supplierStatusAtReceipt=APPROVED` · `receivedAt=2026-07-02T06:00:00+07:00` · `RECEIVED` · `photosCount=8` · `batches=[BATCH-VN-2210]` | Phiếu nhận mẫu — gốc chuỗi truy xuất BR-076 |
| `FX-TX-ISSUE` | DB seed | `TXN-20260706-06` · `type=ISSUE` · `batchId=BATCH-GF-4410` · `ingredientId=ING-GAO-TAM` · `delta=-30.4` `unit=kg` · `menuId=MENU-2026-27` · `mealType=LUNCH` · `dishLabel="Cơm trắng"` · `serviceDate=2026-07-06` | Dòng xuất đủ khoá truy xuất — oracle `ck_tx_issue_trace` |
| `FX-PREP-OK` | DB seed | `PREP-20260706-01` · `date=2026-07-06` · `menuId=MENU-2026-27` · `mealType=LUNCH` · `dishLabel="Thịt kho trứng cút"` · `batchesUsed=[BATCH-MP-8842, BATCH-MP-8825, BATCH-GF-4401]` | Sổ chế biến khớp phiếu xuất — oracle 4 luật §8 |
| `FX-PREP-FAILSTEP` | DB seed | `PREP-20260630-01` · `date=2026-06-30` · `dishLabel="Gà rang gừng"` · `batchesUsed=[BATCH-MP-8820]` · `notes` ghi nhiệt lõi 68°C | Mắt xích lịch sử của sự cố `INC-2026-014` — E2E điều tra |
| `FX-CATALOG` | DB seed | `ING-TOM-SU` (`kg`, `refuseFactor=0.45`) · `ING-THIT-LON-VAI` (`kg`, `0.05`) · `ING-THIT-GA` (`kg`, `0.35`) · `ING-TRUNG-CUT` (`quả`, `0.18`) · `ING-CA-BASA` (`kg`, `0.0`) | Oracle C7. `ING-CA-BASA` có hệ số `0.0` ⇒ ca chia cho `(1−0)` không được đặc biệt hoá |
| `FX-SCHOOL-GEO` | DB seed | `school_config`: toạ độ trường `TH Đoàn Thị Điểm` + `geofence_radius=200` (mặc định BR-028/MF02, **không phải 50m** như UC file ghi — CF-M06-2) | Oracle BR-074 |
| `FX-JWT-*` | JWT | 7 token theo `RoleKey` **kebab-case** (D6): `system-admin`, `school-admin`, `teacher`, `parent`, `medical-staff`, `service-caterers`, `supplier` — mỗi token mang `schoolId` hoặc `supplierId` | Ma trận §16. `supplier` = NCC nguyên liệu (D6 chốt) |
| `FX-JWT-CROSS` | JWT | `service-caterers` của **trường B**; `supplier` của **NCC B** (`SUP-002`) | Cross-school / cross-supplier → 403 |
| `FX-ENV` | env / property | `sfms.geofence.enforced=true` · `delivery.deviation-alert-minutes=30` · `spring.jpa.hibernate.ddl-auto=validate` | Test chạy với geofence **bật** (dev là no-op theo ADR-000); ngưỡng lệch là **cấu hình**, không phải luật (CF-M06-6) |
| `FX-CLOCK` | test clock | `Clock.fixed(2026-07-06T03:00:00Z, "Asia/Ho_Chi_Minh")` | `shelfStatus`/`isLocked`/FEFO phụ thuộc "hôm nay" — **cấm** dùng `LocalDate.now()` trong test, seed đóng băng quanh 06–07/2026 |

**Props Isolation Boilerplate (CASE 2.0 — BẮT BUỘC, bản Java):**

```java
// ═══════════════════════════════════════════════════════════════════════
// CASE 2.0 — Props Isolation Pattern (bản Java, thay boilerplate TypeScript
// của template PHASE-4_Test-Spec.md §4)
// Đặt ở đầu mỗi test class. Mỗi @Test dùng factory → KHÔNG shared mutable state.
// Đây là biện pháp chống AP-AI-002 (Green-from-Birth).
// ═══════════════════════════════════════════════════════════════════════
final class Fixtures {

    /** FX-CLOCK — cấm LocalDate.now(): seed đóng băng quanh 06–07/2026. */
    static final LocalDate TODAY = LocalDate.of(2026, 7, 6);
    static final Clock     CLOCK = Clock.fixed(
            Instant.parse("2026-07-06T03:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));

    static final UUID SCHOOL_A   = UUID.fromString("11111111-1111-1111-1111-111111111111");
    static final UUID ING_THIT   = UUID.fromString("22222222-2222-2222-2222-222222222222");
    static final UUID ING_DAU_AN = UUID.fromString("33333333-3333-3333-3333-333333333333");

    /** Giá trị baseline hợp lệ — đồng bộ FX-BATCH-OK (§3 TDS-05). */
    private static InventoryBatch base() {
        var b = new InventoryBatch();
        b.setCode("BATCH-MP-8842");          // D1 — mã nghiệp vụ, KHÔNG phải PK
        b.setSchoolId(SCHOOL_A);
        b.setIngredientId(ING_THIT);
        b.setIngredientLabel("Thịt lợn nạc vai");   // nhãn snapshot, KHÔNG dùng để join (C6)
        b.setInitialQuantity(new BigDecimal("40")); // L5 — number, đơn vị tách riêng
        b.setUnit("kg");
        b.setStorageZone(StorageZone.CHILLED);      // D6 — SCREAMING_SNAKE
        b.setReceivedDate(LocalDate.of(2026, 7, 5));
        b.setExpiryDate(LocalDate.of(2026, 7, 8));  // D4 — LocalDate, không chuỗi dd/MM/yyyy
        b.setQuarantine(null);
        return b;
    }

    /** Factory — mỗi it()/@Test lấy instance MỚI qua đây. */
    static InventoryBatch batch(Consumer<InventoryBatch> overrides) {
        var b = base();
        overrides.accept(b);
        return b;
    }

    static QuarantineInfo quarantine(String reason, String incidentCode) {
        return new QuarantineInfo(CLOCK.instant(), MEDICAL_STAFF_ID, reason, incidentCode);
    }
}
```

> **Cấm trong test MF06** (kiểm ở Exit Criteria §6): `LocalDate.now()` / `Instant.now()` trong assertion · `@DirtiesContext` để che state rò rỉ · field `static` mutable dùng chung giữa `@Test` · `@Sql` chèn dữ liệu mà không dọn.

---

## 4. Test Case Specification

> **TC ID format:** `M06-TC-[NNN]` (Unit) · `M06-TC-INT-[NNN]` (Integration) · `M06-TC-SEC-[NNN]` (Security) · `M06-TC-CON-[NNN]` (Contract) · `M06-TC-E2E-[NNN]` (E2E).
> **Severity:** CRITICAL / HIGH / MEDIUM / LOW.
> **Status:** 🔴 Not written / 🟡 Written-failing / 🟢 Passing. Toàn bộ tài liệu này ở trạng thái 🔴 — chưa có production code (TDD).
> **Oracle Source:** *bắt buộc mọi TC.* Giá trị kỳ vọng đến từ đâu — BR/UC/MSG/ADR/ràng buộc. Không dùng giả định AI.
>
> **Hai định dạng khối TC dùng trong tài liệu này:**
> - **Khối đầy đủ** — dùng cho TC có oracle phức tạp, có tranh chấp nguồn, hoặc là GATE nghiệp vụ. Đủ 14 field theo template.
> - **Khối gọn (bảng)** — dùng cho TC oracle một dòng, không tranh chấp. **Vẫn đủ** các field bắt buộc: ID · Severity · Feature Under Test · Given/When/Then · Oracle Source · Expected FAIL. Rút gọn phần Preconditions (trỏ Fixture ID) và Implementation Note (gộp ở cuối nhóm). Đây là rút gọn **trình bày**, không rút gọn **nội dung kiểm** — mỗi dòng là một `@Test` độc lập.

**Bản đồ file test (Java / Maven — `04_Development/Backend/src/test/java/com/sfms/mf06/`):**

| Nhóm | File |
|---|---|
| Unit — FEFO | `service/FefoAllocatorTest.java` |
| Unit — kho & sổ | `service/InventoryServiceTest.java` |
| Unit — nhận hàng | `service/ReceivingServiceTest.java` |
| Unit — giao hàng / mua hàng | `service/DeliveryServiceTest.java`, `service/PurchaseRequestServiceTest.java` |
| Unit — domain thuần | `domain/InventoryBatchTest.java`, `service/BomQuantityCalculatorTest.java` |
| Unit — geofence & serialize | `service/GeofenceValidatorTest.java`, `web/InventoryResponseSerializationTest.java` (`@JsonTest`) |
| Integration | `web/ReceivingSessionIT.java`, `web/InventoryIT.java`, `web/IssueSlipIT.java`, `web/InventoryTransactionIT.java`, `web/SupplierBatchIT.java`, `web/PreparationRecordIT.java`, `web/PagingContractIT.java` |
| Integration — luật audit | `invariant/SemanticAuditInvariantIT.java` (24 luật, mỗi luật 1 `@Test`) |
| Security | `security/Mf06AuthorizationIT.java`, `security/Mf06GateBypassIT.java` |
| Contract (D8) | `contract/OpenApiContractIT.java` |
| E2E | `e2e/TraceabilityBackboneE2E.java`, `e2e/IncidentQuarantineE2E.java` |

**Base class dùng chung cho Integration/Security/Contract:**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
abstract class Mf06IntegrationBase {

    @Container
    static final PostgreSQLContainer<?> DB = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url",      DB::getJdbcUrl);
        r.add("spring.datasource.username", DB::getUsername);
        r.add("spring.datasource.password", DB::getPassword);
        // Flyway V1…V9 (đã merge) + V10 (schema MF06) + V11 (seed MF06) — §11.3 TDS, M06-D14
        r.add("spring.flyway.locations",    () -> "classpath:db/migration");
        // ADR-M06-7: dev là no-op; TEST bật để kiểm BR-074 thật
        r.add("sfms.geofence.enforced",     () -> "true");
        r.add("delivery.deviation-alert-minutes", () -> "30");   // CF-M06-6 — cấu hình, không phải luật
    }

    @Autowired protected MockMvc      mvc;
    @Autowired protected JdbcTemplate jdbc;

    /** FX-CLOCK — bean Clock cố định, cấm LocalDate.now() trong service lẫn test. */
    @TestConfiguration
    static class FixedClock {
        @Bean @Primary Clock clock() {
            return Clock.fixed(Instant.parse("2026-07-06T03:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
        }
    }

    /** Helper D2 — mọi lỗi phải là envelope { error: { code, message, details } }. */
    protected void assertErrorEnvelope(MvcResult res, int status, String code) throws Exception {
        assertThat(res.getResponse().getStatus()).isEqualTo(status);
        var json = JsonPath.parse(res.getResponse().getContentAsString());
        assertThat(json.read("$.error.code",    String.class)).isEqualTo(code);       // D2
        assertThat(json.read("$.error.message", String.class)).isNotBlank();          // D2 + D7
        assertThat(json.read("$.details", Object.class)).isNull();                    // KHÔNG phẳng
        assertThat((List<?>) json.read("$.error.details")).isNotNull();               // mảng, không null
    }
}
```

---

### 4.1 UNIT TEST CASES (service + domain — không DB, không Spring context)

> Kỹ thuật: Equivalence Partitioning + BVA + Decision Table (§3 TDS-04). Port mock: `@Mock PartnerStatusPort`, `@Mock MenuPort`, `@Mock FaceRecognitionPort` — **không** import entity MF04/MF05/MF11 (constraint C9).

---

#### `M06-TC-001` — FEFO chọn lô hết hạn **sớm nhất** trước

**Severity:** `CRITICAL`
**Legal:** Luật ATTP 55/2010/QH12 — nguyên liệu dùng trong bữa ăn tập thể phải còn hạn
**Feature Under Test:** `FefoAllocator.allocate(ingredientId, needed, candidates, remaining, today)`
**Test File:** `service/FefoAllocatorTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-075`
**Oracle Source:** **BR-075** nguyên văn: *"Hệ thống ưu tiên sử dụng nguyên liệu theo nguyên tắc **FEFO** — lô hàng hết hạn sớm nhất được xuất kho trước."* · UC-045 (traceability §1.5: BR-075, BR-077, BR-205) · ADR-M06-4

**Preconditions:**
- `FX-CLOCK` — `today = 2026-07-06`
- 3 lô cùng `ingredientId = ING-THIT-LON-VAI`, đều `quarantine=null`, đều còn hạn, tổng tồn đủ:
  `L1 = BATCH-GF-4411` HSD `2026-07-08` còn `17.2 kg` · `L2 = BATCH-MP-8842` HSD `2026-07-10` còn `40 kg` · `L3` HSD `2026-08-05` còn `30 kg`
- Danh sách truyền vào **cố tình xếp sai thứ tự** (`L3, L1, L2`) — allocator phải tự sort, không dựa vào thứ tự đầu vào

**Test Steps:**
1. *Arrange*: `var candidates = List.of(L3, L1, L2);` (thứ tự đảo)
2. *Act*: `allocator.allocate(ING_THIT, new BigDecimal("20"), candidates, remainingAll(), TODAY)`
3. *Assert*: thứ tự và số lượng phân bổ

**Expected Result (PASS — hành vi đúng):**
- `result.lines()` có đúng **2** dòng, theo thứ tự `["BATCH-GF-4411", "BATCH-MP-8842"]` (HSD tăng dần)
- `take` của `BATCH-GF-4411` = `17.2` (lấy hết lô sắp hết hạn trước), `take` của `BATCH-MP-8842` = `2.8`
- `result.shortfall()` = `0`
- `L3` (HSD xa nhất) **không** xuất hiện trong `lines()`

**Expected Result (FAIL — dấu hiệu lỗi):**
- Trả theo thứ tự đầu vào (`L3` trước) ⇒ sort chưa chạy — hàng sắp hết hạn tồn lại tới lúc quá hạn rồi phải huỷ
- Sort theo `receivedDate` ⇒ đang làm **FIFO** không phải FEFO (Glossary TDS phân biệt rõ hai thứ)
- Chia đều cho 3 lô ⇒ không có ưu tiên nào

**Current Status:** 🔴 Not written
**Implementation Note:** sort bằng `Comparator.comparing(InventoryBatch::getExpiryDate)` trên **bản copy**, không mutate list đầu vào (hàm thuần — ADR-M06-4). `BigDecimal` so sánh bằng `isEqualByComparingTo`, **không** `equals` (`17.2` ≠ `17.20` với `equals`).

---

#### `M06-TC-002` — Lô **EXPIRED bị loại khỏi tập ứng viên**, không chỉ ẩn trên UI

**Severity:** `CRITICAL`
**CWE:** `CWE-841 — Improper Enforcement of Behavioral Workflow`
**Legal:** Luật ATTP 55/2010/QH12 — cấm dùng nguyên liệu quá hạn
**Feature Under Test:** `FefoAllocator.allocate()` + `InventoryBatch.isLocked(today)`
**Test File:** `service/FefoAllocatorTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-205`, `COND-077`
**Oracle Source:** **BR-205** nguyên văn: *"Nguyên liệu có ngày hết hạn đã qua phải được **tự động khóa**… ngăn không cho chọn để xuất kho hoặc sử dụng trong quá trình chế biến"* + *"**loại chúng khỏi danh sách lựa chọn** trong quy trình xuất kho"* · **BR-077** (thiếu tồn → cảnh báo) · luật `XUAT_LO_HET_HAN` · ADR-M06-4

**Preconditions:**
- `FX-CLOCK` — `today = 2026-07-06`
- `FX-BATCH-EXPIRED` = `BATCH-GF-4388` (`ING-DAU-AN`, HSD `2026-06-28` → **quá hạn 8 ngày**), còn **40 lít**
- Lô còn hạn cùng nguyên liệu: `FX-BATCH-USABLE-OIL` = `BATCH-GF-4420` (HSD `2027-01-01`) nhưng chỉ còn **10 lít**
- `needed = 15 lít` — **cố ý lớn hơn** tồn khả dụng, nhỏ hơn tổng tồn kể cả lô hết hạn

**Test Steps:**
1. *Arrange*: `candidates = List.of(expiredOil, usableOil)`
2. *Act*: `allocator.allocate(ING_DAU_AN, new BigDecimal("15"), candidates, remainingAll(), TODAY)`
3. *Assert*: lô hết hạn tuyệt đối không được dùng, dù có đủ hàng

**Expected Result (PASS):**
- `lines()` chỉ có `["BATCH-GF-4420"]`, `take = 10`
- `shortfall() = 5` — **báo thiếu** thay vì lấy nốt từ lô quá hạn
- `BATCH-GF-4388` **không** xuất hiện ở `lines()` **và không** xuất hiện ở danh sách ứng viên trả về (nếu API trả `candidates`)

**Expected Result (FAIL — lỗ hổng):**
- `shortfall = 0` và `lines` có `BATCH-GF-4388` ⇒ **đã xuất hàng quá hạn để cho đủ số** — đúng loại lỗi BR-205 tồn tại để chặn
- `shortfall = 5` nhưng `BATCH-GF-4388` vẫn nằm trong danh sách ứng viên trả cho FE ⇒ chỉ ẩn ở UI, gọi API trực tiếp là ép xuất được

**Current Status:** 🔴 Not written
**Implementation Note:** lọc `!b.isLocked(today)` **trước** khi sort, không lọc sau khi đã phân bổ. **Cấm** đọc cột `status` để lọc (AP-AI-012 — seed đóng băng quanh 06–07/2026, đọc cột sẽ cho lô quá hạn đi qua). `shortfall` phải là `BigDecimal`, so sánh `isEqualByComparingTo("5")`.

---

#### `M06-TC-003` — Lô **đang cách ly** bị loại, kể cả khi còn hạn

**Severity:** `CRITICAL`
**Feature Under Test:** `FefoAllocator.allocate()` — nhánh `quarantine != null`
**Test File:** `service/FefoAllocatorTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-149`, `COND-205`
**Oracle Source:** **BR-149** nguyên văn: Medical Staff *"tự chọn từng lô nguyên liệu cụ thể cần cách ly… hệ thống đặt trạng thái Quarantine **ngay khi chọn**"* · ADR-M06-2 (`isLocked = shelfStatus == EXPIRED || quarantine != null`) · ADR-M06-4

**Preconditions:**
- `FX-BATCH-QUAR-NOINC` = `BATCH-DD-1080` — `ING-TOM-SU`, **18 kg**, HSD `2026-09-28` (**còn hạn 84 ngày**), `quarantine != null`
- Không có lô tôm nào khác trong kho
- `needed = 10 kg`

**Test Steps:**
1. *Arrange*: `candidates = List.of(quarantinedShrimp)`
2. *Act*: `allocate(ING_TOM_SU, bd("10"), candidates, remainingAll(), TODAY)`
3. *Assert*

**Expected Result (PASS):**
- `lines()` **rỗng**
- `shortfall() = 10` — toàn bộ nhu cầu thành thiếu hụt ⇒ kéo theo `MSG-018` ở tầng trên (`M06-TC-INT-011`)

**Expected Result (FAIL):**
- Lấy 10 kg từ lô đang cách ly ⇒ nguyên liệu đang bị điều tra ATTP đi vào bữa ăn học sinh. Đây là ca mock **đã dính ở tầng dữ liệu**: FE đọc mảng seed clone nên lô Y tế vừa cách ly vẫn hiện trong danh sách xuất kho (TDS §16 ghi chú)

**Current Status:** 🔴 Not written
**Implementation Note:** `isLocked` là **HOẶC** hai trục — không được viết `if (expired && quarantined)`. Lô này còn hạn ⇒ nếu chỉ kiểm hạn thì test FAIL, đúng ý đồ.

---

#### `M06-TC-004` — `shelfStatus` suy từ `expiryDate`, biên **≤ 2 ngày** (BVA)

**Severity:** `HIGH`
**Feature Under Test:** `InventoryBatch.shelfStatus(LocalDate today)` — `@Transient`
**Test File:** `domain/InventoryBatchTest.java` (`@ParameterizedTest`)
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-205`
**Oracle Source:** **BR-205** (3 nhãn: khoá khi quá hạn · badge `"Expired"` · `"Expiring Soon"` khi trong ngưỡng cảnh báo) · **ADR-M06-2** (ngưỡng `EXPIRING_SOON` = **2 ngày**, lấy từ `batchShelfStatus()` của mock) · **CF-M06-11** (không lưu cột `status`) · **L2** (giá trị thứ ba là `IN_STOCK`)

**Preconditions:** `FX-CLOCK` `today = 2026-07-06`. Không cần DB, không cần Spring.

**Test Steps:** `@ParameterizedTest` với 5 mốc biên:

| `expiryDate` | Quan hệ với `today` | Expected `shelfStatus` |
|---|---|---|
| `2026-07-05` | hôm qua | `EXPIRED` |
| `2026-07-06` | **hôm nay** | `EXPIRING_SOON` ⚠️ biên dưới |
| `2026-07-07` | `today + 1` | `EXPIRING_SOON` |
| `2026-07-08` | `today + 2` | `EXPIRING_SOON` ⚠️ **biên trên — phải vẫn là EXPIRING_SOON** |
| `2026-07-09` | `today + 3` | **`IN_STOCK`** |

**Expected Result (PASS):** đúng 5 dòng trên. Hàm **không** đọc bất kỳ cột `status` nào (kiểm bằng cách set `batch.status` nếu field còn tồn tại → kết quả không đổi; hoặc bằng `CON-005` assert schema không có field đó).

**Expected Result (FAIL):**
- `today + 2` trả `IN_STOCK` ⇒ off-by-one: dùng `isBefore(today.plusDays(2))` thay vì `!isAfter(...)` — mất một ngày cảnh báo trên hàng tươi có hạn 3 ngày
- `today` (hết hạn **hôm nay**) trả `EXPIRED` ⇒ khoá sớm một ngày, huỷ hàng còn dùng được
- Trả `ACTIVE` thay `IN_STOCK` ⇒ **L2** — FE render nhãn *"Hoạt động"* (khoá `ACTIVE` của `STATUS_BADGE` dành cho trạng thái đối tác) trên lô nguyên liệu, sai ngữ cảnh kho **và không báo lỗi**

**Current Status:** 🔴 Not written
**Implementation Note:** giữ nguyên công thức TDS §5.2 nhưng đổi giá trị trả về cuối: `if (expiryDate.isBefore(today)) return EXPIRED; if (!expiryDate.isAfter(today.plusDays(2))) return EXPIRING_SOON; return IN_STOCK;`

---

#### `M06-TC-005` — `isLocked` = hết hạn **HOẶC** cách ly (bảng quyết định 4 ô)

**Severity:** `CRITICAL`
**Feature Under Test:** `InventoryBatch.isLocked(LocalDate today)`
**Test File:** `domain/InventoryBatchTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-149`, `COND-205`
**Oracle Source:** **ADR-M06-2** (*"`isLocked = shelfStatus == EXPIRED || quarantine != null`"*) — hợp nhất **BR-205** + **BR-149**; §6.6 TDS (hai trục độc lập)

**Test Steps:** bảng quyết định đủ 4 tổ hợp:

| # | `shelfStatus` | `quarantine` | Expected `isLocked` | Fixture |
|---|---|---|---|---|
| a | `IN_STOCK` | `null` | **`false`** | `FX-BATCH-OK` |
| b | `EXPIRED` | `null` | `true` | `FX-BATCH-EXPIRED` |
| c | `IN_STOCK` | ≠ `null` | `true` | `FX-BATCH-QUAR-NOINC` |
| d | `EXPIRED` | ≠ `null` | `true` | `FX-BATCH-QUAR-INC` |

**Expected Result (PASS):** đúng 4 dòng. Thêm: ô (d) sau khi **gỡ cách ly** vẫn `isLocked = true` (vì còn hết hạn) — chứng minh gỡ cách ly **không** ghi đè `shelfStatus` (ADR-M06-2 điểm 2).
**Expected Result (FAIL):** ô (c) trả `false` ⇒ dùng `&&` thay `||`; ô (d) sau gỡ cách ly trả `false` ⇒ cách ly đã **ghi đè** trạng thái hạn dùng — đúng lỗi ADR-M06-2 tồn tại để dẹp.
**Current Status:** 🔴 Not written

---

#### `M06-TC-006` — Tồn kho là **số dẫn xuất** `initialQuantity + Σ delta`

**Severity:** `HIGH`
**Feature Under Test:** `InventoryService.remaining(batchCode)` / projection tồn
**Test File:** `service/InventoryServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-LEDGER`
**Oracle Source:** **ADR-M06-1** (*"Tồn khả dụng = `initial_quantity + SUM(delta)` tính runtime… **không** cột `remaining_quantity`"*) · **BR-204** (mọi thay đổi tồn có 1 dòng giải trình) · **L5** (number + unit tách riêng)

**Preconditions:** lô `initialQuantity = 45` `unit = kg`; sổ có 2 dòng: `RECEIPT +45` *(dòng nhập gốc)*, `ISSUE −24.1`, `ISSUE −7.6`

**Expected Result (PASS):**
- `remaining()` = `13.3` (`BigDecimal`, `isEqualByComparingTo("13.3")`), `unit = "kg"`
- Trả **number**, không phải chuỗi `"13.3 kg"` (**L5**)
- Không đọc field `remaining` nào trên entity (entity **không có** field đó — nếu có, compile fail là kết quả đúng)

**Expected Result (FAIL):** trả `45` ⇒ chưa trừ sổ; trả `20.9` ⇒ cộng cả dòng `RECEIPT` hai lần (nhập gốc vừa vào `initialQuantity` vừa vào `Σ delta` — **ca đua thật**, xem Implementation Note); trả `"13.3 kg"` ⇒ vi phạm L5.
**Current Status:** 🔴 Not written
**Implementation Note:** ⚠️ Quyết định phải nhất quán: **hoặc** `initialQuantity` đã bao gồm lượng nhập và `Σ delta` chỉ tính `ISSUE`/`ADJUSTMENT`, **hoặc** `initialQuantity = 0` và dòng `RECEIPT` mang toàn bộ. TDS §5.2 chọn cách 1 (`initial_quantity NOT NULL CHECK > 0` = lượng nhập ban đầu, bất biến) **nhưng** ADR-M06-5 lại ghi phiên nhận có tạo `inventory_transactions(RECEIPT)` — hai chỗ cộng cùng một lượng. ⇒ Công thức đúng: `remaining = initial_quantity + SUM(delta WHERE type <> 'RECEIPT')`; hoặc giữ `SUM(delta)` toàn bộ và bỏ `initial_quantity` khỏi công thức. **Cần Mạnh chốt khi review §5.2** — test này FAIL cho tới khi chốt, và đó là giá trị của nó.

---

#### `M06-TC-007` — Quy đổi BOM có **hệ số thải bỏ** (ràng buộc C7) — đơn vị khối lượng

**Severity:** `HIGH`
**Feature Under Test:** `BomQuantityCalculator.toPurchaseQuantity(perServing, servings, ingredient)`
**Test File:** `service/BomQuantityCalculatorTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-C7`
**Oracle Source:** **ràng buộc C7** `data_model.puml` nguyên văn: *"**Lượng thực mua = lượng ăn được ÷ (1 − hệ số thải bỏ)** và chỉ áp cho khối lượng/thể tích."* · `FX-CATALOG`: `ING-TOM-SU.refuseFactor = 0.45` (giá trị **thật** trong `INGREDIENT_CATALOG`)

**Test Steps:** `perServing = 40 g` tôm sú × `380` suất → ăn được `15.2 kg` → `15.2 / (1 − 0.45)`

**Expected Result (PASS):**
- `27.636` kg (`BigDecimal` scale 3, `RoundingMode.HALF_UP` — khớp `NUMERIC(12,3)` của DDL)
- Ca `refuseFactor = 0.0` (`ING-CA-BASA`, giá trị thật trong catalog): kết quả = lượng ăn được, **không** chia đặc biệt, **không** ném `ArithmeticException`
- Ca `refuseFactor = null` (`ING-GAO-TAM`, `ING-PHO-MAI` — catalog **không khai** field này): coi như `0.0`, **không** NPE

**Expected Result (FAIL):**
- `15.2` ⇒ bỏ qua hệ số thải bỏ (**AP-AI-016**) — đặt mua thiếu 45% tôm, bếp thiếu hàng giữa giờ nấu
- `22.04` (= `15.2 × (1 − 0.45)`) ⇒ **nhân** thay vì **chia**: sai chiều công thức, đặt mua thiếu còn nhiều hơn
- `NullPointerException` với `ING-GAO-TAM` ⇒ chưa xử lý `refuseFactor` vắng

**Current Status:** 🔴 Not written
**Implementation Note:** `divide(BigDecimal.ONE.subtract(rf), 3, RoundingMode.HALF_UP)`. Chặn `refuseFactor >= 1` bằng validate ở tầng catalog (chia cho 0) — MF06 chỉ đọc, ghi 1 dòng ghi chú cho MF05/MF04 nếu gặp.

---

#### `M06-TC-008` — Đơn vị **đếm** KHÔNG áp hệ số thải bỏ (C7 — nửa sau của ràng buộc)

**Severity:** `HIGH`
**Feature Under Test:** `BomQuantityCalculator.toPurchaseQuantity(..., unit)`
**Test File:** `service/BomQuantityCalculatorTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-C7`
**Oracle Source:** **C7** nguyên văn: *"và **chỉ áp cho khối lượng/thể tích**. Đơn vị đếm thì công thức đã ghi đúng thứ phải mua."*

**Preconditions:** `FX-BATCH-COUNT-UNIT` / `FX-CATALOG` — `ING-TRUNG-CUT`: `unit = "quả"`, **`refuseFactor = 0.18`** *(giá trị này **có thật** trong catalog và phải bị **bỏ qua** — đó chính là điểm của test)*

**Test Steps:** `3 quả/suất × 380 suất`

**Expected Result (PASS):** `1140 quả` — **không** chia `(1 − 0.18)`. Áp dụng cho toàn bộ tập đơn vị đếm: `quả`, `hộp`, `cái`, `bó`. Chia đúng cho `kg`, `lít`.
**Expected Result (FAIL):** `1390.24 quả` (= `1140 / 0.82`) ⇒ đặt mua thừa 22% trứng cút mỗi lần; ngoài ra **số thập phân cho đơn vị đếm là vô nghĩa về nghiệp vụ** (không mua được 0.24 quả trứng).
**Current Status:** 🔴 Not written
**Implementation Note:** phân loại theo `unit` chứ **không** theo `refuseFactor == null` — catalog **có** khai hệ số cho đơn vị đếm (`ING-TRUNG-CUT` 0.18, `ING-TRUNG-GA` 0.12), nên dựa vào `null` là sai. Tập đơn vị khối lượng/thể tích khai tường minh: `Set.of("kg","g","lít","ml")`.

---

#### `M06-TC-009` / `M06-TC-010` — `ADJUSTMENT` thiếu `reasonCode` **hoặc** `managerSignature` → chặn (bảng quyết định)

**Severity:** `CRITICAL`
**CWE:** `CWE-284 — Improper Access Control` (thiếu non-repudiation)
**Feature Under Test:** `InventoryService.adjust(AdjustRequest)`
**Test File:** `service/InventoryServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-204`
**Oracle Source:** **BR-204** nguyên văn: *"**bắt buộc** phải kèm theo: (1) **mã lý do** được chọn từ danh sách định sẵn… và (2) **xác nhận/chữ ký quản lý** (manager sign-off). Hệ thống **chặn** ghi nhận giao dịch điều chỉnh nếu **thiếu một trong hai** yếu tố trên."* · UC-064 (traceability §1.5: BR-075, BR-076, BR-204) · `ck_tx_adjustment`
**⚠️ ORACLE GAP (L6):** `ING-204` **không có MSG** trong `error_message.md` v1.5 ⇒ assert `status = 400` + `error.code = "ING-204"` + `message` khác rỗng & tiếng Anh; **KHÔNG** assert nội dung message.

**Test Steps:** bảng quyết định 4 ô, `delta = -2` `unit = "lít"` trên `BATCH-GF-4388`:

| # | `reasonCode` | `managerSignature` | Expected | TC |
|---|---|---|---|---|
| a | `SPOILAGE` | `"Lê Thị Mai"` | ✅ ghi nhận, 1 dòng `ADJUSTMENT` | `M06-TC-INT-012` |
| b | `null` | `"Lê Thị Mai"` | ❌ `MissingReasonOrSignatureException` → `400 ING-204` | **`M06-TC-009`** |
| c | `SPOILAGE` | `""` (rỗng/blank) | ❌ `400 ING-204` | **`M06-TC-010`** |
| d | `null` | `null` | ❌ `400 ING-204` | `M06-TC-010b` |

**Expected Result (PASS):** ô b/c/d ném exception **trước khi** ghi bất kỳ dòng nào (`verify(txRepo, never()).save(any())`). Ô c: `""`, `"   "`, `null` đều bị chặn — dùng `isBlank()` **không** `!= null`.
**Expected Result (FAIL):** ô c đi qua với chữ ký rỗng ⇒ có dòng điều chỉnh **không ai chịu trách nhiệm** — mất non-repudiation (§4.3 TDS), đúng thứ BR-204 tồn tại để chặn. Ô b đi qua ⇒ tồn kho bị sửa không lý do, audit trail BR-170 vô nghĩa.
**Current Status:** 🔴 Not written
**Implementation Note:** validate ở service **và** DB (`ck_tx_adjustment`) — hai lớp, vì §5.2 TDS xếp BR-204 vào nhóm *"bất biến đặt ở tầng DB, không trông vào kỷ luật service"*. `reasonCode` phải là **enum** `ReasonCode` (`SPOILAGE`/`COUNT_CORRECTION`/`DAMAGE`/`RETURN_TO_SUPPLIER`), không phải `String` — chuỗi tự do thì "danh sách định sẵn" của BR-204 không được đảm bảo.

---

#### `M06-TC-011` — Lưu kho **sai khu nhiệt độ** → chặn

**Severity:** `HIGH`
**Feature Under Test:** `InventoryService.storeBatch(batchCode, storageZone)`
**Test File:** `service/InventoryServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-202`
**Oracle Source:** **BR-202** nguyên văn: *"khu vực lưu trữ được chọn (Dry/Chilled/Frozen) phải khớp với nhóm nhiệt độ yêu cầu của nguyên liệu; hệ thống **chặn lưu và báo lỗi** nếu không khớp."* · UC-061 (traceability §1.5: BR-075, BR-202)
**⚠️ ORACLE GAP (L6):** `ING-202` không có MSG → assert `422` + `code`, không assert nội dung message.

**Test Steps:** nguyên liệu `ING-THIT-LON-VAI` yêu cầu `CHILLED`; gọi `storeBatch(..., StorageZone.DRY)`
**Expected Result (PASS):** ném `StorageZoneMismatchException` → `422 ING-202`; `details` chỉ rõ field vi phạm và khu yêu cầu: `[{ "field": "storageZone", "message": "Thịt lợn nạc vai requires CHILLED" }]` (D2 — `details` là **mảng**). Ca khớp (`CHILLED`) → lưu thành công.
**Expected Result (FAIL):** cho lưu thịt tươi ở khu khô ⇒ hỏng hàng trong vài giờ, không lỗi nào báo ra cho tới khi bếp mở ra dùng.
**Current Status:** 🔴 Not written
**Implementation Note:** nhóm nhiệt độ yêu cầu của nguyên liệu đọc từ catalog qua port (C9), **không** hard-code map trong MF06. Giá trị enum **SCREAMING_SNAKE** (`DRY`/`CHILLED`/`FROZEN`) khớp DDL — **không** `"Chilled"` như ví dụ §9.2 (**L4**).

---

#### `M06-TC-012` / `M06-TC-012b` — Từ chối lô thiếu lý do/ảnh → chặn · phiên chốt thiếu ảnh → chặn

**Severity:** `CRITICAL`
**Feature Under Test:** `ReceivingService.record(RecordReceivingRequest)`
**Test File:** `service/ReceivingServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-078`, `COND-072`
**Oracle Source:** **BR-078** nguyên văn: *"Lô nguyên liệu không đạt yêu cầu kiểm tra chất lượng có thể bị **từ chối (Rejected)**; **lý do từ chối phải được ghi lại với ảnh bằng chứng**."* · **BR-072** nguyên văn: *"Nhân viên nhận hàng **bắt buộc phải chụp ảnh** nguyên liệu, nhãn, bao bì và tài liệu hỗ trợ làm bằng chứng."* · UC-072, UC-060 · `ck_line_reject`, `ck_rcv_photos`
**⚠️ ORACLE GAP (L6):** `ING-078`, `ING-072` không có MSG → assert `400` + `code`.

**Test Steps (bảng quyết định BR-078):**

| # | `rejected` | `rejectReason` | `rejectPhotoUrls` | Expected | TC |
|---|---|---|---|---|---|
| a | `false` | — | — | ✅ dòng nhận bình thường | `INT-001` |
| b | `true` | `"Rau dập nát, có mùi lạ — không đạt cảm quan"` | 1 ảnh | ✅ `RECEIVED_PARTIAL` | `INT-005` |
| c | `true` | `null` | 1 ảnh | ❌ `400 ING-078` | **`M06-TC-012`** |
| d | `true` | có | `[]` rỗng | ❌ `400 ING-078` | `M06-TC-012c` |
| e | phiên chốt, `photosCount = 0` | — | — | ❌ `400 ING-072` | **`M06-TC-012b`** |

**Expected Result (PASS):** ô c/d/e chặn **trước khi** ghi; ô e chỉ chặn khi **chốt** phiên — phiên `IN_PROGRESS` được phép `photosCount = 0` (`ck_rcv_photos` = `status = 'IN_PROGRESS' OR photos_count >= 1`).
**Expected Result (FAIL):** ô c đi qua ⇒ lô bị từ chối không có căn cứ, NCC khiếu nại thì trường không có bằng chứng; ô e đi qua ⇒ phiếu nhận không ảnh, mất giá trị chứng từ (BR-072 + Retention ≥ 2 năm §4.2 TDS).
**Current Status:** 🔴 Not written

---

#### `M06-TC-013` — NCC không `APPROVED` → chặn phiên nhận, **không ghi bản ghi nào**

**Severity:** `CRITICAL`
**CWE:** `CWE-841 — Improper Enforcement of Behavioral Workflow`
**Legal:** Luật ATTP 55/2010/QH12 Điều 37 — hiệu lực chứng nhận
**Feature Under Test:** `ReceivingService.record()` + `PartnerStatusPort.isEligible(supplierCode, at)`
**Test File:** `service/ReceivingServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-070`
**Oracle Source:** **BR-070** nguyên văn: *"Khi bắt đầu phiên nhận hàng, hệ thống **tự động xác minh** nhà cung cấp đang ở trạng thái **Approved**. Nếu nhà cung cấp không được phê duyệt, hệ thống **từ chối** phiên nhận hàng."* · UC-060/UC-044 · luật `PHIEU_GHI_SAI_TRANG_THAI_NCC`
**⚠️ ORACLE GAP (L6):** `ING-070` không có MSG → assert `409` + `code`.

**Preconditions:** `FX-SUP-SUSPENDED` = `SUP-004` (`SUSPENDED`, `certExpiry = 2026-06-22`). `PartnerStatusPort` mock trả `SUSPENDED`.

**Expected Result (PASS):**
- Ném `SupplierNotApprovedException` → `409 ING-070`
- **Không** dòng nào được ghi: `verify(sessionRepo, never()).save(any())`, `verify(batchRepo, never()).save(any())`, `verify(txRepo, never()).save(any())` — kiểm cả 3 repo vì phiên nhận là **1 transaction 5 bảng** (ADR-M06-5)
- Ca `FX-SUP-EXPIRING` (`SUP-003` `APPROVED` nhưng `certStatus = EXPIRING`) → **cho nhận** (BR-070 chỉ xét `status`; hiệu lực chứng nhận là cổng của MF04 — CF-M06-7)

**Expected Result (FAIL):**
- Cho nhận hàng của NCC bị đình chỉ ⇒ nguyên liệu từ nhà cung cấp đã bị đình chỉ vì lô tôm không đạt (lý do đình chỉ thật trong seed `SUP-004`) đi vào kho trường
- Chặn nhưng **đã ghi** `receiving_sessions` rồi rollback không sạch ⇒ còn phiếu rác, `LO_KHONG_CO_PHIEU_NHAN` và `PHIEU_GHI_SAI_TRANG_THAI_NCC` nhiễu
- Chặn cả `SUP-003` ⇒ chặn quá tay, khoá luôn NCC hợp lệ có chứng nhận sắp hết hạn

**Current Status:** 🔴 Not written
**Implementation Note:** `supplierStatusAtReceipt` ghi trạng thái **THẬT tại thời điểm nhận** (§5.2: *"khai mặc định `APPROVED` thì sau này không biết lô vào kho trong tình trạng nào"*) — **không** hard-code `"APPROVED"`. Gọi port bằng **mã** (`"SUP-004"`), không import entity MF04 (C9).

---

#### `M06-TC-014` … `M06-TC-016` — nhóm gọn: giao hàng & truy xuất khoá

| TC | Severity | Feature Under Test | Given → When → Then | Oracle Source | Expected FAIL |
|---|---|---|---|---|---|
| `M06-TC-014` | HIGH | `DeliveryService.create()` | `FX-DLV-*`: `delivery.scheduledAt = 2026-06-30` **<** `purchaseRequest.requestedDate = 2026-07-01` → `create()` → từ chối, `409 ING-409`, không ghi dòng nào | luật **`GIAO_TRUOC_KHI_DAT`** (nguyên văn script §10) · BR-071 · §6.4 TDS | Nhận chuyến giao có ngày **trước** ngày đặt hàng ⇒ chứng từ mâu thuẫn thời gian, truy xuất mất tin cậy |
| `M06-TC-015` | **CRITICAL** | `IssueSlipService.issue()` | Dòng BOM có nguyên liệu **không tra được** `ingredientId` trong catalog (tên lệch: `"Tôm sú đông lạnh"` vs catalog `"Tôm sú"`) → `issue()` → **từ chối** `422 ING-076`, `unknownIngredient = true`, **không** ghi dòng `ISSUE` nào | **BR-076** (*"ghi nhận số lô… để đảm bảo khả năng **truy xuất nguồn gốc đầy đủ**"*) · **ràng buộc C6** (*"lệch một chữ là đứt chuỗi mà không có lỗi nào báo ra"*) · ADR-M06-3 · luật `PHIEU_SAI_NGUYEN_LIEU_CUA_LO` | **Im lặng bỏ qua** dòng không tra được ⇒ món nấu ra không có lô nào gắn vào; khi có sự cố ATTP thì bản đồ truy xuất **thiếu đúng món nghi vấn**. Mock đã dính: `"Tôm sú đông lạnh" ≠ "Tôm sú"`, `"Su su xào tỏi" ≠ "Su su xào"` |
| `M06-TC-016` | MEDIUM | `DeliveryService.updateStatus()` | `FX-DLV-SCHEDULED` lệch **45′** so với `scheduledAt`, ngưỡng cấu hình `30′` → `updateStatus(DELAYED)` → phát event `DeliveryDeviationDetected` + cảnh báo **MSG-065**; ca lệch **25′** → **không** phát | **MSG-065** nguyên văn: *"Delivery delay alert: Supplier delivery time deviates from schedule."* · UC-092 (traceability §1.5: BR-071, MSG-029/MSG-065) · **CF-M06-6** (ngưỡng 30′ **không có BR** → tham số cấu hình `delivery.deviation-alert-minutes`, **không** dùng BR-135 khác miền) | Hard-code `30` như luật ⇒ không đổi được theo trường, và trích BR-135 (trễ **phục vụ bữa ăn** 20′, miền MF09) vào tài liệu MF06 là ghost-citation |

---

#### `M06-TC-017` — `id` công khai là **mã nghiệp vụ**, UUID không rò ra API (D1)

**Severity:** `HIGH`
**Feature Under Test:** `InventoryBatchMapper` / `InventoryBatchResponse` (MapStruct) · `@JsonProperty("id")`
**Test File:** `web/InventoryResponseSerializationTest.java` (`@JsonTest`)
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-PAGE`, L1
**Oracle Source:** **D1** — *"`id` trong JSON **và** path param = **mã nghiệp vụ**; UUID chỉ là PK nội bộ"*; FE dùng mã nghiệp vụ làm `id` ở **mọi nơi** (URL, khoá ngoại, cột "Mã"). Lý do 3 của D1: `partnerId` là **khoá cách ly dữ liệu giữa các NCC** — đổi sang UUID mà sót một chỗ là **rò dữ liệu chéo giữa các nhà cung cấp**.

**Test Steps:** serialize `InventoryBatch{ id = <UUID>, code = "BATCH-MP-8842", supplierId = <UUID>, supplierCode = "SUP-001" }`

**Expected Result (PASS):**
- `$.id` = `"BATCH-MP-8842"`
- `$.supplierId` = `"SUP-001"` — khoá ngoại trong JSON dùng **code của bảng đích**, không UUID
- **Không** field nào trong JSON khớp regex UUID `^[0-9a-f]{8}-[0-9a-f]{4}-...$` (assert quét toàn bộ cây JSON, không chỉ field `id`)
- Khuôn mã sẵn có **giữ nguyên**, không ép về khuôn mới: `BATCH-MP-8842`, `RCV-20260702-02`, `SUP-001`, `PR-2026-0815`, `MENU-2026-27` (D1: *"khuôn sẵn có của module nếu FE đã dùng — **giữ nguyên**"*)

**Expected Result (FAIL):** `$.id` là UUID ⇒ FE điều hướng `/service-caterers/inventory/<uuid>` khác hoàn toàn URL hiện có, và cột "Mã lô" hiện UUID thay mã in trên bao bì; `$.supplierId` là UUID ⇒ rủi ro rò dữ liệu chéo NCC theo đúng lý do 3 của D1.
**Current Status:** 🔴 Not written
**Implementation Note:** **L1** — entity hiện tại (TDS §5.2) dùng `@Id String id` = mã nghiệp vụ, chưa tách `code`. Test này chỉ pass sau khi §5.2 được sửa theo D1 (`id UUID` + `code VARCHAR(32)` + `UNIQUE(school_id, code)`). Sinh mã dùng bảng `code_sequences` với `INSERT … RETURNING` (D1) — **không** đọc-rồi-ghi, tránh trùng mã khi 2 request song song.

---

#### `M06-TC-018` / `M06-TC-019` / `M06-TC-020` — nhóm gọn: định dạng hợp đồng & geofence

| TC | Severity | Feature Under Test | Given → When → Then | Oracle Source | Expected FAIL |
|---|---|---|---|---|---|
| `M06-TC-018` | HIGH | Jackson config + DTO ngày | Serialize DTO có `receivedDate: LocalDate`, `receivedAt: Instant`, `serviceDate: LocalDate` → `$.receivedDate = "2026-07-05"`, `$.receivedAt = "2026-07-02T06:00:00Z"`; **không** field nào khớp `^\d{2}/\d{2}/\d{4}`; grep `@JsonFormat` trong package `com.sfms.mf06` → **0** kết quả | **D4** (JSON luôn ISO-8601; ghi đè `@JsonFormat` của ADR-M11-5) · **L3** | Trả `"05/07/2026"` ⇒ FE `new Date(...)` parse sai/`Invalid Date` ở locale khác; và lọc `dateFrom`/`dateTo` so sánh chuỗi `dd/MM/yyyy` sẽ **im lặng trả rỗng** (bẫy D4 ghi chú) |
| `M06-TC-019` | HIGH | Enum serialize + deserialize | `TransactionType.ADJUSTMENT` → `"ADJUSTMENT"` (không `"Điều chỉnh"`); `ReasonCode.SPOILAGE` → `"SPOILAGE"` (không `"spoilage"`); `StorageZone.CHILLED` → `"CHILLED"` (không `"Chilled"`); `MealType.LUNCH` → `"LUNCH"`; deserialize `"Chilled"`/`"spoilage"` → **400 MSG-138** *"Validation failed: one or more required fields are missing or invalid."* | **D6** (*"tất cả SCREAMING_SNAKE trong JSON và DB; nhãn hiển thị **không bao giờ** là giá trị lưu trữ"*) · DDL §5.2 (`CHECK … IN ('DRY','CHILLED','FROZEN')`) · **MSG-138** (v1.5, dùng chung mọi module) · **L4** | Nhận `"Chilled"` rồi tự `toUpperCase()` ⇒ hợp đồng có **hai** dạng giá trị hợp lệ, `openapi-typescript` sinh union sai, và `MealType` sẽ nhận cả `"Bữa chiều"` — nhãn D6 **chốt là "Bữa phụ"**, hai chuỗi khác nhau |
| `M06-TC-020` | **CRITICAL** | `GeofenceValidator.verify(lat, lng, schoolConfig)` | BVA quanh `geofence_radius = 200m`: `199m` → PASS · `200m` → PASS (biên **inclusive**) · `201m` → chặn · `640m` → chặn; cờ `sfms.geofence.enforced=false` → **no-op** (dev) | **BR-074** (*"xác minh… trong **khu vực nhận hàng được phép** của trường"*) + **BR-073** · **MSG-015** nguyên văn: *"Receiving action blocked: GPS location verification failed (out of tolerance)."* → `422 ING-015` · **BR-028** (bán kính từ `school_config`, mặc định **200m**) · **CF-M06-2** (**không** dùng 50m như UC file, **không** cite BR-079/BR-080) · ADR-M06-7 | Hard-code `50m` theo UC file ⇒ chặn oan mọi phiên nhận ở trường có cổng cách kho >50m; đọc bán kính từ request ⇒ client tự nới geofence; biên `200m` bị loại ⇒ off-by-one chặn oan đúng ranh giới |

**Implementation Note (chung nhóm §4.1):**
- Không `@SpringBootTest` cho nhóm này — `@ExtendWith(MockitoExtension.class)`, chạy < 2s cả nhóm.
- Mọi so sánh `BigDecimal` bằng `isEqualByComparingTo`, **không** `isEqualTo`.
- Mọi hàm phụ thuộc "hôm nay" nhận `LocalDate today` / `Clock` **qua tham số hoặc bean** — cấm `LocalDate.now()` (FX-CLOCK).
- 3 port (`PartnerStatusPort`, `MenuPort`, `FaceRecognitionPort`) mock bằng `@Mock`, stub trả **mã** không trả entity (C9).

---

### 4.2 INTEGRATION TEST CASES (endpoint → DB)

> `Mf06IntegrationBase` + Testcontainers `postgres:16`. Timeout 120s/class. Mỗi TC chạy trên seed `V11__seed_mf06.sql`, rollback sau mỗi `@Test` (`@Transactional` ở test hoặc `TRUNCATE` có kiểm soát — **không** `@DirtiesContext`).
> Mọi request mang JWT `service-caterers` của trường A trừ khi ghi khác. Mọi path có tiền tố **`/api/v1`** (D3).

#### Nhóm A — Phiên nhận hàng: một transaction, gate server-side

---

#### `M06-TC-INT-001` — Phiên nhận là **MỘT** transaction ghi 5 bảng

**Severity:** `CRITICAL`
**Feature Under Test:** `POST /api/v1/receiving-sessions` — full flow (thay **6 mutation** FE gọi nối tiếp)
**Test File:** `web/ReceivingSessionIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-ATOMIC`, `COND-070`, `COND-072`, `COND-073`, `COND-074`
**Oracle Source:** **MSG-041** nguyên văn: *"Ingredient delivery received. Inventory levels updated."* · UC-060 + UC-044 (traceability §1.5: BR-070/071/072/073/074, MSG-015/MSG-041) · **ADR-M06-5** (*"**một** endpoint… ghi trong **một** `@Transactional`"*)

**Preconditions:**
- `FX-DLV-DELIVERED` = `DLV-2026-1105` (`DELIVERED`) của `FX-SUP-APPROVED` = `SUP-001`
- `FX-SB-OK`: 2 lô NCC `BATCH-GF-4411`, `BATCH-GF-4412` đã khai (`status = ACTIVE`)
- `PR-2026-0815` đang `APPROVED`
- GPS trong geofence (`FX-SCHOOL-GEO`), `photosCount = 8`, cả 2 lô đạt

**Test Steps:**
1. `POST /api/v1/receiving-sessions` với body: `deliveryId: "DLV-2026-1105"`, `driverVerificationId`, `lat`/`lng` trong bán kính, `photosCount: 8`, `lines[2]` cả hai `rejected: false`, `storageZone: "CHILLED"` (D6)
2. Assert response 201
3. Assert DB **5 bảng** trong một lần đếm

**Expected Result (PASS):**
- `201`; `$.id` = mã nghiệp vụ khuôn `RCV-yyyyMMdd-NN` (D1), `$.status = "RECEIVED"`, `$.gpsVerified = true`
- `$.receivedAt` là **ISO-8601** (D4/L3), và **do server ghi** — khác giá trị client gửi (nếu gửi)
- DB: `receiving_sessions` **+1** · `receiving_lines` **+2** · `inventory_batches` **+2** · `inventory_transactions` **+2** dòng `RECEIPT` với `delta > 0` · `deliveries.status = 'COMPLETED'` · `purchase_requests.status = 'COMPLETED'` · `supplier_batches.status = 'DELIVERED'`
- `receiving_sessions.supplier_status_at_receipt = 'APPROVED'` — trạng thái **thật** lúc nhận
- `inventory_batches.receiving_session_id` trỏ đúng phiên vừa tạo (gốc chuỗi truy xuất BR-076)

**Expected Result (FAIL):**
- Chỉ ghi `receiving_sessions`, thiếu `inventory_batches` ⇒ FE vẫn phải orchestrate → quay lại đúng lỗi ADR-M06-5 chặn: đứt giữa chừng thì **lô vào kho mà đơn chưa COMPLETED, hoặc ngược lại**
- Ghi đủ nhưng **không** trong một transaction: mô phỏng lỗi ở bước 4 (`inventory_transactions` ném exception) → phải **rollback toàn bộ**, `SELECT count(*) FROM receiving_sessions` không đổi. Nếu còn dòng ⇒ transaction sai phạm vi

**DB Assertion:**
```java
@Test
@DisplayName("ADR-M06-5 — POST /receiving-sessions ghi 5 bảng trong MỘT transaction (MSG-041)")
void recordReceiving_isAtomicAcross5Tables() throws Exception {
    var before = counts();                       // snapshot 5 bảng

    var res = mvc.perform(post("/api/v1/receiving-sessions")
                    .header(AUTHORIZATION, jwt(SERVICE_CATERERS_SCHOOL_A))
                    .contentType(APPLICATION_JSON)
                    .content(body("receiving/happy-path.json")))
               .andExpect(status().isCreated())
               .andReturn();

    var id = JsonPath.read(res.getResponse().getContentAsString(), "$.id").toString();
    assertThat(id).matches("^RCV-\\d{8}-\\d{2}$");                       // D1 — mã nghiệp vụ
    assertThat(res.getResponse().getContentAsString())
        .doesNotContainPattern("\\d{2}/\\d{2}/\\d{4}");                  // D4/L3 — không dd/MM/yyyy

    var after = counts();
    assertThat(after.sessions()).isEqualTo(before.sessions() + 1);
    assertThat(after.lines()).isEqualTo(before.lines()   + 2);
    assertThat(after.batches()).isEqualTo(before.batches() + 2);
    assertThat(after.txns()).isEqualTo(before.txns()   + 2);

    assertThat(jdbc.queryForObject(
        "SELECT status FROM deliveries WHERE code = 'DLV-2026-1105'", String.class))
        .isEqualTo("COMPLETED");
    assertThat(jdbc.queryForObject(
        "SELECT status FROM purchase_requests WHERE code = 'PR-2026-0815'", String.class))
        .isEqualTo("COMPLETED");
    assertThat(jdbc.queryForObject("""
        SELECT count(*) FROM inventory_transactions t
          JOIN inventory_batches b ON b.id = t.batch_id
         WHERE t.type = 'RECEIPT' AND t.delta > 0
           AND b.receiving_session_id = (SELECT id FROM receiving_sessions WHERE code = ?)
        """, Integer.class, id)).isEqualTo(2);                            // BR-076 gốc truy xuất
}
```

**Current Status:** 🔴 Not written
**Implementation Note:** `@Transactional` ở **service**, không ở controller. UPSERT `inventory_batches` theo `code` (xem `INT-006`). Cấm để FE gửi `receivedAt`/`schoolId`/`supplierId` — lấy từ JWT + server clock (`SEC-006`, `SEC-009`).

---

#### `M06-TC-INT-002` … `M06-TC-INT-006c` — nhóm gọn: gate server-side & tính toàn vẹn phiên nhận

> ⚠️ **Cả nhóm này là nhóm "gọi API trực tiếp bỏ qua UI"** mà §2c-3b yêu cầu bắt buộc: **CF-M06-12** xác nhận mock **không enforce BR nào của MF06 ở "server"** — toàn bộ BR-070/072/073/074/078/202/204/205 hiện chỉ nằm trong component FE (`receiving/new/page.tsx`, `inventory/page.tsx`). Gate chỉ có ở client thì gọi API trực tiếp là lách được **toàn bộ luật ATTP**.

| TC | Sev | Given → When → Then (Gherkin) | Oracle Source |
|---|---|---|---|
| `INT-002` | **CRIT** | **Given** `FX-SUP-SUSPENDED` `SUP-004` (`SUSPENDED`) **When** `POST /receiving-sessions` **Then** `409` + `error.code = "ING-070"` **và** `SELECT count(*) FROM receiving_sessions` **không đổi** **và** `inventory_batches` không đổi | **BR-070** nguyên văn · luật `PHIEU_GHI_SAI_TRANG_THAI_NCC` · ⚠️ **ORACLE GAP (L6)** — không assert nội dung message |
| `INT-002b` | HIGH | **Given** `SUP-004` `certExpiry = 2026-06-22`, phiên nhận `receivedAt = 2026-06-25` **When** chạy truy vấn kiểm **Then** `rowsChecked > 0` **và** `violations = 0` | luật **`NHAN_HANG_SAU_KHI_CHUNG_NHAN_HET_HAN`** (§10c) · BR-070 |
| `INT-002c` | HIGH | **Given** phiên nhận có `supplier_status_at_receipt` **khác** `partners.status` hiện tại, `received_at > 2026-07-01` **When** kiểm **Then** `violations = 0` (phiếu ghi đúng trạng thái tại thời điểm nhận) | luật **`PHIEU_GHI_SAI_TRANG_THAI_NCC`** (§10c) · BR-070 |
| `INT-003` | **CRIT** | **Given** body `photosCount = 0`, chốt phiên (`status = RECEIVED`) **When** `POST /receiving-sessions` **Then** `400 ING-072`; **Given** `photosCount = 0` nhưng `status = IN_PROGRESS` **Then** `201` (CHECK `ck_rcv_photos` cho phép) | **BR-072** nguyên văn · `ck_rcv_photos` · ⚠️ **L6** |
| `INT-004` | **CRIT** | **Given** `sfms.geofence.enforced = true`, toạ độ cách trường **640m** (geofence `200m`) **When** `POST /receiving-sessions` **Then** `422` + `code = "ING-015"` + `message` = **MSG-015 nguyên văn** *"Receiving action blocked: GPS location verification failed (out of tolerance)."*; không ghi dòng nào | **MSG-015** (có MSG thật ⇒ **assert nguyên văn**) · **BR-074** + BR-073 · CF-M06-2 (200m, không 50m) |
| `INT-005` | HIGH | **Given** phiên 2 lô, **1 lô bị từ chối** kèm `rejectReason` + 1 ảnh **When** chốt **Then** `201`, `$.status = "RECEIVED_PARTIAL"`; **chỉ lô đạt** vào `inventory_batches` (+1 không +2); lô bị từ chối: `supplier_batches.status` trở lại `'ACTIVE'`; `receiving_lines` có 2 dòng, 1 dòng `rejected = true` | **BR-078** nguyên văn · UC-072 · MSG-041 |
| `INT-005b` | HIGH | **Given** một lô trong `inventory_batches` trỏ phiên nhận có `status = 'REJECTED'` **When** kiểm **Then** `rowsChecked > 0` **và** `violations = 0` | luật **`NHAN_LO_TU_PHIEU_BI_TU_CHOI`** (§10b) · BR-078 |
| `INT-006` | **CRIT** | **Given** mã lô `BATCH-GF-4411` **đã có** trong kho (tồn 45kg) **When** phiên nhận thứ hai ghi **chính mã lô đó** (thêm 20kg) **Then** `inventory_batches` **KHÔNG** thêm dòng thứ hai (`count` theo `code` = 1); tồn **không** bị cộng hai lần | **ADR-M06-5** nguyên văn: *"**upsert theo mã lô** là bắt buộc — mã lô do NCC gán có thể đã nằm trong kho → tạo dòng thứ hai sẽ khiến FEFO **cộng tồn hai lần**"* · **AP-AI-013** |
| `INT-006b` | MED | **Given** lô có `received_date` **khác** ngày của phiên nhận trỏ tới nó **When** kiểm **Then** `violations = 0` | luật **`NGAY_NHAP_LO_KHAC_PHIEU_NHAN`** (§10b) · BR-076 |
| `INT-006c` | MED | **Given** lô có `supplier_id` **khác** `supplier_id` của phiên nhận trỏ tới nó **When** kiểm **Then** `violations = 0` | luật **`NCC_LO_KHAC_NCC_PHIEU_NHAN`** (§10b) · BR-076 |

#### Nhóm B — Kho, phiếu xuất FEFO, sổ append-only

| TC | Sev | Given → When → Then | Oracle Source |
|---|---|---|---|
| `INT-007` | **CRIT** | **Given** thực đơn `MENU-2026-27` đã công bố, `serviceDate = 2026-07-06`, `mealType = LUNCH`, tồn đủ **When** `GET /api/v1/inventory/issue-plan?serviceDate=2026-07-06&mealType=LUNCH&servings[MENU-2026-27]=380` **Then** `200`; mỗi dòng `allocation[]` sort **`expiryDate` tăng dần**; `unknownIngredients` rỗng; ngày trả **ISO** | **BR-075** · UC-045 · ADR-M06-4 · D4 |
| `INT-008` | **CRIT** | **Given** issue-plan không thiếu hàng **When** `POST /api/v1/inventory/issue-slips` **Then** `201`; `issue_slips` +1 (`status = 'POSTED'`); **mỗi** dòng `ISSUE` có **đủ 5 khoá** `issue_slip_id`, `dish_id`, `menu_id`, `service_date`, `meal_type` **không NULL**; `delta < 0`; `dish_label` chỉ là nhãn | **BR-076** nguyên văn · **`ck_tx_issue_trace`** · ADR-M06-3/M06-6 · **MSG-042** *"Inventory transaction recorded and stock adjusted."* (assert nguyên văn) |
| `INT-008b` | **CRIT** | **Given** đã có phiếu xuất cho `(trường A, 2026-07-06, LUNCH)` **When** `POST /inventory/issue-slips` lần hai cùng bộ khoá **Then** `409 ING-409`; tồn **không** bị trừ hai lần (`SUM(delta)` không đổi) | **`uq_slip_school_date_meal`** · ADR-M06-6 (*"chặn trừ tồn HAI LẦN — mock chỉ chặn ở FE (`issuedMenus`)"*) · ⚠️ **L6** |
| `INT-009` | **CRIT** | **When** `PUT /api/v1/inventory-transactions/{code}` **hoặc** `DELETE /api/v1/inventory-transactions/{code}` **Then** `405` + `code = "ING-405"`; số dòng sổ không đổi. Thêm: grep route table → **không tồn tại** handler `PUT`/`DELETE` cho resource này | **ADR-M06-1** (append-only) · §4.3 TDS Immutability · **AP-AI-014** · ⚠️ **L6** |
| `INT-010` | HIGH | **Given** `BATCH-MP-8842` (`ING-THIT-LON-VAI` yêu cầu `CHILLED`) **When** `PATCH /api/v1/inventory/BATCH-MP-8842/storage-zone` body `{"storageZone":"DRY"}` **Then** `422 ING-202`, `error.details[0].field = "storageZone"`; **When** `{"storageZone":"CHILLED"}` **Then** `200`. **Và** `{"storageZone":"Dry"}` (chữ hoa/thường sai) **Then** `400` **MSG-138** nguyên văn | **BR-202** nguyên văn · **D6**/**L4** · **MSG-138** (assert nguyên văn) · ⚠️ **L6** cho ING-202 |
| `INT-011` | HIGH | **Given** thực đơn cần **5.5 kg** tôm sú, kho chỉ có `BATCH-DD-1080` **đang cách ly** **When** `GET /inventory/issue-plan` **Then** `shortages[]` có dòng `ING-TOM-SU` với `shortfall = 5.5`, `available = 0`, `unit = "kg"` — **number không phải chuỗi** (L5); **When** `POST /inventory/issue-slips` **Then** `409` + `code = "ING-018"` + `message` = **MSG-018 nguyên văn** *"Inventory shortage: Insufficient stock of ingredient [Ingredient Name]."* với `[Ingredient Name]` thay bằng `"Tôm sú"` | **BR-077** nguyên văn · **MSG-018** (có MSG thật ⇒ assert nguyên văn, kiểm cả phép **thay placeholder**) · BR-149 |
| `INT-012` | HIGH | **Given** `BATCH-GF-4420` (dầu ăn còn hạn) **When** `POST /api/v1/inventory-transactions/adjustments` body `{batchId:"BATCH-GF-4420", delta:-2, unit:"lít", reasonCode:"SPOILAGE", managerSignature:"Lê Thị Mai"}` **Then** `201`; `$.type = "ADJUSTMENT"` (**không** `"Điều chỉnh"`), `$.reasonCode = "SPOILAGE"` (**không** `"spoilage"`), `$.delta = -2` + `$.unit = "lít"` (**không** `"-2 lít"`), `$.occurredAt` ISO; DB `inventory_transactions` +1, tồn giảm 2 | **BR-204** · **MSG-042** (assert nguyên văn) · **D4/D6/L3/L4/L5** |
| `INT-013` | **CRIT** | **Given** `FX-BATCH-QUAR-INC` `BATCH-MP-8820` đang cách ly **When** `POST /inventory-transactions/adjustments` trên lô đó **Then** `409 ING-149`; **When** `POST /inventory/issue-slips` chứa lô đó **Then** `409 ING-149`; sổ không thêm dòng nào | **BR-149** nguyên văn · TDS §9.2 (`ING-149`) · ⚠️ **L6** |

#### Nhóm C — Lô phía NCC: bất biến đặt ở tầng DB (BR-071)

> Nhóm này assert **CHECK constraint chạy thật**, không chỉ validate ở service — §5.2 TDS xếp 3 CHECK này vào nhóm *"bất biến đặt ở tầng DB (không trông vào kỷ luật service)"*. Cách kiểm: `INSERT` trực tiếp bằng `jdbc` và **kỳ vọng `DataIntegrityViolationException`**; nếu insert thành công ⇒ migration thiếu constraint.

| TC | Sev | Given → When → Then | Oracle Source |
|---|---|---|---|
| `INT-014` | HIGH | **When** `INSERT supplier_batches` với `expiration_date = manufacturing_date` (bằng nhau) **Then** vi phạm `ck_sb_exp_after_mfg` (CHECK là `>` **strict**); `expiration_date < manufacturing_date` cũng vi phạm | luật **`HSD_TRUOC_NGAY_SX`** (script: `if (exp <= mfg)`) · **BR-071** · `ck_sb_exp_after_mfg` |
| `INT-014b` | HIGH | **When** `INSERT inventory_batches` với `expiry_date <= received_date` **Then** vi phạm `ck_batch_exp_after_recv` | luật **`NHAP_LO_DA_HET_HAN`** (§2) · BR-071 |
| `INT-015` | MED | **When** `INSERT supplier_batches` với `packaging_date < manufacturing_date` **Then** vi phạm `ck_sb_pack_after_mfg`; **biên**: `packaging_date = manufacturing_date` (ca `FX-SB-OK` `BATCH-GF-4411`, cả hai `2026-07-02`) **phải PASS** (CHECK là `>=`) | luật **`DONG_GOI_TRUOC_KHI_SX`** (script: `if (pack < mfg)`) · BR-071 |
| `INT-015b` | MED | **When** `INSERT supplier_batches` với `product_id` không tồn tại **Then** vi phạm FK | luật **`LO_TRO_MAT_HANG_KHONG_TON_TAI`** (§10 — chỉ có `add()`, không `check()`; vẫn là luật thật theo §2c-3b) · BR-071 |
| `INT-016` | **CRIT** | **Given** seed đủ **When** kiểm `tx.ingredient_id <> batch.ingredient_id` **Then** `rowsChecked > 0` **và** `violations = 0`. **Và**: `POST /issue-slips` cố ghi dòng `ISSUE` có `ingredient_id` khác lô → `422 ING-076` | luật **`PHIEU_SAI_NGUYEN_LIEU_CUA_LO`** (§1) · **BR-076** · **C6** · ADR-M06-3 |
| `INT-016b` | MED | **Given** lô NCC có `product_label` khác tên mặt hàng trong catalog **When** kiểm **Then** `violations = 0` | luật **`TEN_LO_KHAC_MAT_HANG`** (§10) · BR-071, C6 |
| `INT-016c` | HIGH | **Given** chuyến giao liên kết lô thuộc **NCC khác** với NCC của đơn mua **When** kiểm **Then** `violations = 0`; **và** `POST /deliveries` với `linkedBatches` chéo NCC → `422` | luật **`CHUYEN_GIAO_LO_KHAC_NCC`** (§10) · BR-071 |
| `INT-017` | **CRIT** | **Given** seed đủ **When** kiểm dòng `ISSUE` có `batch.expiry_date < tx.service_date` **Then** `rowsChecked > 0` **và** `violations = 0` | luật **`XUAT_LO_HET_HAN`** (§1) · **BR-205** |
| `INT-017b` | HIGH | **When** kiểm dòng `ISSUE` có `tx.service_date < batch.received_date` **Then** `rowsChecked > 0` **và** `violations = 0` | luật **`XUAT_TRUOC_KHI_NHAP`** (§1) · BR-076 |
| `INT-031` | HIGH | **Given** `partners.cert_expiry` **When** kiểm đơn mua có `delivery_date > cert_expiry` của NCC đó **Then** `rowsChecked > 0` **và** `violations = 0` | luật **`MUA_HANG_SAU_KHI_GIAY_TO_HET_HAN`** (§10) · BR-046/048 (**MF04** — port bằng ID, CF-M06-7) |

**Mẫu code chuẩn cho toàn nhóm luật `semantic-audit.ts`** (`invariant/SemanticAuditInvariantIT.java`):

```java
/**
 * Port luật semantic-audit.ts sang invariant test trên DB thật.
 *
 * QUY TẮC BẮT BUỘC (script, dòng 17–20): "mỗi luật đếm số dòng THỰC SỰ kiểm được.
 * Luật kiểm 0 dòng là luật HỎNG (thường do đổi tên trường), không phải luật sạch."
 * ⇒ assertion (1) rowsChecked > 0 KHÔNG ĐƯỢC BỎ. Bỏ nó là test luôn xanh dù seed
 *   đổi tên cột — chính là AP-AI-002 Green-from-Birth.
 */
private void assertRule(String ruleName, String checkedSql, String violationSql) {
    Integer rowsChecked = jdbc.queryForObject(checkedSql, Integer.class);
    assertThat(rowsChecked)
        .as("%s — luật kiểm 0 dòng là luật HỎNG, không phải luật sạch", ruleName)
        .isGreaterThan(0);

    List<Map<String, Object>> violations = jdbc.queryForList(violationSql);
    assertThat(violations).as("%s — vi phạm", ruleName).isEmpty();
}

@Test
@DisplayName("XUAT_LO_HET_HAN (BR-205) — không phiếu xuất nào dùng lô đã quá hạn tại ngày phục vụ")
void xuatLoHetHan() {
    assertRule("XUAT_LO_HET_HAN",
        """
        SELECT count(*) FROM inventory_transactions t
          JOIN inventory_batches b ON b.id = t.batch_id
         WHERE t.type = 'ISSUE' AND t.service_date IS NOT NULL AND b.expiry_date IS NOT NULL
        """,
        """
        SELECT t.code, b.code AS batch, t.service_date, b.expiry_date
          FROM inventory_transactions t
          JOIN inventory_batches b ON b.id = t.batch_id
         WHERE t.type = 'ISSUE' AND b.expiry_date < t.service_date
        """);
}

@Test
@DisplayName("PHIEU_SAI_NGUYEN_LIEU_CUA_LO (BR-076, C6) — dòng sổ và lô phải cùng ingredient_id")
void phieuSaiNguyenLieuCuaLo() {
    assertRule("PHIEU_SAI_NGUYEN_LIEU_CUA_LO",
        """
        SELECT count(*) FROM inventory_transactions t
          JOIN inventory_batches b ON b.id = t.batch_id
         WHERE t.ingredient_id IS NOT NULL AND b.ingredient_id IS NOT NULL
        """,
        """
        SELECT t.code, t.ingredient_id, b.code AS batch, b.ingredient_id AS batch_ingredient
          FROM inventory_transactions t
          JOIN inventory_batches b ON b.id = t.batch_id
         WHERE t.ingredient_id <> b.ingredient_id
        """);
}
```

#### Nhóm D — Sổ chế biến ⭐ (4 luật `semantic-audit.ts` §8 mà TDS §13.0 chưa port — xem **L7**)

| TC | Sev | Given → When → Then | Oracle Source |
|---|---|---|---|
| `INT-018` ⭐ | **CRIT** | **Given** `FX-PREP-OK` `PREP-20260706-01` (`date = 2026-07-06`, `batchesUsed = [BATCH-MP-8842, …]`) **When** kiểm `batch.expiry_date < prep.date` **Then** `rowsChecked > 0` **và** `violations = 0`. **Và**: `POST /api/v1/preparation-records` với `batchesUsed` chứa lô đã quá hạn tại `date` → **`409 ING-205`** | luật **`CHE_BIEN_DUNG_LO_HET_HAN`** (§8, nguyên văn script) · **BR-205** nguyên văn (*"ngăn không cho chọn để xuất kho **hoặc sử dụng trong quá trình chế biến**"* — nửa sau của rule này TDS §13 chưa phủ) · ⚠️ **L6** |
| `INT-019` ⭐ | HIGH | **Given** sổ chế biến dùng lô `X` ngày `D` **When** kiểm tồn tại dòng `ISSUE` cho `(batch_id = X, service_date = D)` **Then** `rowsChecked > 0` **và** `violations = 0`. **Và**: `POST /preparation-records` với lô **không có phiếu xuất** tương ứng → `422` kèm `details` chỉ rõ lô, **hoặc** cho phép nhưng đánh dấu `preparation_batches.extra = true` | luật **`LO_CHE_BIEN_KHONG_CO_PHIEU_XUAT`** (§8) · **BR-076** · §5.2 (`preparation_batches.extra` — *"lô đầu bếp thêm tay ngoài phiếu xuất — tách riêng để audit đối chiếu"*) ⇒ **oracle cho phép 2 hành vi**, phải chốt: xem Implementation Note |
| `INT-020` ⭐ | HIGH | **When** `INSERT preparation_batches` với `batch_id` không tồn tại **Then** vi phạm FK; **và** `POST /preparation-records` với `batchesUsed: ["BATCH-KHONG-CO"]` → `404` + **MSG-137** nguyên văn *"Resource not found."* | luật **`SO_CHE_BIEN_TRO_LO_KHONG_TON_TAI`** (§8) · BR-076, C6 · **MSG-137** (v1.5 dùng chung mọi module ⇒ assert nguyên văn) |
| `INT-020b` | HIGH | **Given** mọi lô trong kho **When** kiểm `receiving_session_id IS NULL` **Then** `rowsChecked > 0` **và** `violations = 0` | luật **`LO_KHONG_CO_PHIEU_NHAN`** (§10b) · BR-076 (*"truy xuất ngược… dừng ở kho: không biết ai giao, phiếu nào"* — ghi chú seed) |
| `INT-021` ⭐ | MED | **Given** sổ chế biến có `menu_id` **When** kiểm `prep.date <> menu.menu_date` **Then** `rowsChecked > 0` **và** `violations = 0` | luật **`CHE_BIEN_SAI_NGAY_THUC_DON`** (§8) · BR-076 (`PreparationRecord` phải neo đúng phiên sản xuất) |

**Implementation Note (nhóm D):** ⚠️ `INT-019` có **hai oracle khả dĩ** — luật script coi "dùng lô không có phiếu xuất" là **vi phạm**, còn §5.2 TDS lại thiết kế cột `preparation_batches.extra` để **chứa** đúng ca đó. Hai nguồn không cùng kết luận. **Không tự chọn bên nào** (quy tắc §2c-2): TestSpec ghi cả hai, `INT-019` chạy ở dạng *assert `violations = 0` cho các dòng `extra = false`* — dòng `extra = true` được loại khỏi tập kiểm và **phải có** audit đối chiếu. Cần **Huy + Thảo chốt** trước khi implement; nếu chốt "không cho phép extra" thì bỏ cột đó khỏi §5.2.

#### Nhóm E — Cách ly lô (BR-149, hai trục độc lập)

| TC | Sev | Given → When → Then | Oracle Source |
|---|---|---|---|
| `INT-022` ⭐ | HIGH | **Given** sự cố `INC-2026-014` có `quarantineBatches = [BATCH-MP-8820]` **When** kiểm tồn tại dòng `ISSUE` của lô đó cho `service_date = incident.meal_date` **Then** `rowsChecked > 0` **và** `violations = 0` | luật **`CACH_LY_LO_KHONG_LIEN_QUAN_BUA`** (§9, nguyên văn script) · **BR-149** (Medical Staff chọn lô **liên quan tới món** của bữa nghi vấn) — cross-module MF11↔MF06 |
| `INT-023` | HIGH | **Given** lô cách ly **không** `incidentId` và lý do **không** tự giải thích (không chứa `đình chỉ`/`hết hạn`/`thu hồi`) **When** `PATCH /api/v1/inventory/{code}/quarantine` **Then** `400`; **Given** `FX-BATCH-QUAR-NOINC` (lý do chứa `"đình chỉ"`) **Then** `200` — hợp lệ dù thiếu `incidentId`; **Given** `incidentId` trỏ sự cố không tồn tại **Then** `404` **MSG-137** | luật **`CACH_LY_KHONG_RO_SU_CO`** (§9b — script chấp nhận **một trong hai**: có `incidentId` hợp lệ **hoặc** lý do khớp regex) · **BR-149** · `ck_batch_quarantine_complete` |
| `INT-024` | HIGH | **Given** lô đang cách ly **When** `DELETE /api/v1/inventory/{code}/quarantine` **Then** `200`, `$.quarantine = null`, lô **trở lại** tập ứng viên FEFO (gọi lại `issue-plan` thấy lô đó); **Và** `PUT /inventory/{code}` mang `{"quarantine": null}` **KHÔNG** gỡ được cách ly (endpoint không tồn tại / bị bỏ qua) | **ADR-M06-2** (*"Mock từng mất lệnh gỡ cách ly vì `JSON.stringify` xoá khoá mang `undefined`; tách `DELETE` loại hẳn lớp lỗi đó"*) · **AP-AI-015** |
| `INT-024b` | HIGH | **Given** sự cố `status = 'CLOSED'` **When** kiểm còn lô nào `quarantine_at IS NOT NULL` gắn sự cố đó **Then** `rowsChecked > 0` **và** `violations = 0` | luật **`DONG_SU_CO_MA_CON_LO_KHOA`** (§9b — *"sự cố đã đóng mà lô vẫn khoá thì lô nằm khoá vĩnh viễn, không màn nào nhắc nữa"*) · BR-149 |

#### Nhóm F — Hợp đồng phân trang / search / filter (§2b) + định dạng

| TC | Sev | Given → When → Then | Oracle Source |
|---|---|---|---|
| `INT-025` | HIGH | **When** `GET /api/v1/inventory?status=KHONG_TON_TAI` (tập rỗng) **Then** `200` với **đủ 5 field**: `{items:[], total:0, page:1, pageSize:8, pageCount:1}` — **không** `404`, **không** wrapper `data`/`result`. **Và** `GET /inventory?not:storageZone=DRY` loại đúng các lô `DRY` (giá trị **SCREAMING_SNAKE** — L4, TDS §13 IT-M06-25 ghi `Dry`) | **§2b-1** (shape bất biến) · **§2b-6** (tập rỗng → 200, `pageCount ≥ 1`) · **§2b-2** (lọc phủ định `not:`) · **D6** |
| `INT-026` | **CRIT** | **When** `GET /api/v1/inventory-transactions?dateFrom=2026-07-06&dateTo=2026-07-06` **Then** chỉ trả dòng `service_date = 2026-07-06`, và **`total > 0`** *(seed có `TXN-20260706-06/07/08`)* | **§2b-5** (`inventory-transactions` → `service_date`) · ⚠️ **Đây là test bắt bẫy D4 ghi chú**: nếu seed đã chuyển ISO mà bộ lọc còn parse `dd/MM/yyyy`, kết quả là **rỗng, không lỗi, không cảnh báo**. Assert `total > 0` là thứ duy nhất bắt được ca này |
| `INT-027` | HIGH | **When** truy vấn `information_schema` **Then** **mọi** bảng MF06 (`inventory_batches`, `receiving_sessions`, `purchase_requests`, `deliveries`, `supplier_batches`, `preparation_records`, `issue_slips`) có `id uuid` là PK **và** `code varchar(32) NOT NULL` **và** `UNIQUE(school_id, code)` | **D1** + bảng "Việc phải làm ở từng TDS" (MF06 phải đổi PK `VARCHAR` → `UUID` + tách `code`) · **L1** |
| `INT-028` | HIGH | **When** `GET /api/v1/inventory` **Then** lô còn hạn >2 ngày trả `$.items[?].status = "IN_STOCK"`; lô HSD ≤2 ngày → `"EXPIRING_SOON"`; lô quá hạn → `"EXPIRED"`; **không** giá trị `"ACTIVE"` nào xuất hiện. **Và** `information_schema` xác nhận `inventory_batches` **không có** cột `status` | **ADR-M06-2** (sửa 2026-07-29, review I4) · **CF-M06-11** (không lưu cột `status`) · **L2** · **AP-AI-009/AP-AI-012** |
| `INT-029` | HIGH | **When** gọi **mọi** `GET` danh sách của MF06 (7 resource) **Then** (a) response có đúng 5 field phân trang; (b) `page` đã **clamp** (`GET ?page=99` → `page = pageCount`); (c) `pageSize=0` → clamp về `1`; (d) **không** field nào trong toàn cây JSON khớp `^\d{2}/\d{2}/\d{4}`; (e) `search` chạy `ILIKE` không phân biệt hoa/thường: `?search=THỊT LỢN` trả lô `"Thịt lợn nạc vai"` | **§2b-1/2b-6** · **§2b-7** checklist của Mạnh · **D4** · TDS §8.2 `SEARCH_FIELDS` (`inventory` → `id, ingredient_label, supplier_label` — cột **có thật**, CF-M06-9) |
| `INT-030` | **CRIT** | **Given** body `POST /receiving-sessions` mang `receivedAt: "2026-07-01T00:00:00Z"` (quá khứ) **và** `deviceInfo` giả **When** ghi **Then** DB `received_at` = **thời điểm server** (`FX-CLOCK` `2026-07-06T03:00:00Z`), **không** giá trị client gửi | **BR-073** nguyên văn (*"Hệ thống **tự động ghi nhận** tọa độ GPS, thông tin thiết bị, và timestamp… biện pháp chống gian lận"*) · ADR-M06-7 · **AP-AI-006** |

**Implementation Note (nhóm F):** `search` chỉ chạy trên `SEARCH_FIELDS` khai tường minh (§2b-3: `OR ILIKE` trên 10–20 cột = full table scan). `INT-029(e)` chỉ kiểm **không phân biệt hoa/thường**; **không** kiểm không phân biệt **dấu** (`"thit lon"` → `"Thịt lợn"`) — `unaccent` chưa có trong hợp đồng §2b, nếu muốn thì cần extension PostgreSQL + quyết định mới, ghi lại cho Nam.

---

#### Nhóm G (🆕 2026-08-03) — `INT-045` … `INT-051`: hợp đồng với code đã merge (**L9–L18**)

> Bảy TC này **không** sinh từ §13 của TDS. Chúng là **oracle cho §2.-1** — mỗi TC neo đúng một mục L9–L18, và **sáu trong bảy** kiểm thứ làm *migration fail / build đỏ ngay lệnh đầu tiên* nếu code theo TDS v1.0.

| TC | Sev | Given → When → Then | Oracle Source |
|---|---|---|---|
| `INT-045` | **CRIT** | **When** truy vấn `information_schema.columns` cho **mọi** bảng MF06 **Then** (a) đủ 4 cột `created_at TIMESTAMPTZ NOT NULL`, `updated_at TIMESTAMPTZ` (**không** NOT NULL/DEFAULT), `created_by VARCHAR(180)`, `updated_by VARCHAR(180)`; (b) **không** bảng nào có `created_by`/`updated_by` kiểu `uuid`; (c) `issue_slips` có `issued_by_user_id uuid NOT NULL`, `inventory_transactions` có `performed_by_user_id uuid NOT NULL`, **và** cả hai **không** còn cột `created_by uuid`/`actor_id`. **Và** context Spring khởi động được (`@SpringBootTest`) — duplicate column làm nó chết ngay | **D9** + §0-BIS lỗi #1 (*"Hibernate duplicate column → build đỏ ngay lần chạy đầu"*) · **L9** · `M06-D02` |
| `INT-046` | **CRIT** | **Given** DB **trống** **When** Flyway chạy `V1`…`V11` **Then** thành công, và `batch_certificates.document_id` có FK trỏ **`partner_documents`**; `supplier_batches.product_id` trỏ `supplier_products`. **Và** truy vấn `information_schema.tables` xác nhận **không tồn tại** bảng tên `supplier_documents` | **L10/L11** · `M06-D03` · lỗi **C2** §2.1 Workflow (MF07/MF10/MF12 dính bản `users(id)`) — bảng đích không tồn tại ⇒ **fail ở lệnh đầu tiên**, không phải lỗi lúc chạy |
| `INT-047` | **CRIT** | **When** Flyway chạy `V10` **Then** `catalog_ingredients` tồn tại (có `code UNIQUE`, `unit`, `storage_zone`, `refuse_factor`) **và** `dish_ingredients` có constraint `fk_dish_ing_catalog` trỏ `catalog_ingredients(code)`. **Và** `SELECT COUNT(*) FROM dish_ingredients WHERE ingredient_code IS NOT NULL AND ingredient_code NOT IN (SELECT code FROM catalog_ingredients)` = **0** | **L12** · lỗi **C3** §2.1 · `V8:103` (M05-D15 — MF05 đang chờ) · `M06-D04`. ⚠️ Thiếu **một** mã trong seed MF05 (`V9:42`) ⇒ `ALTER` fail ⇒ migration đỏ |
| `INT-048` | HIGH | **When** sinh 2 mã `PR` liên tiếp trong 2026 **Then** số tăng đúng 1 (`PR-2026-0001` → `PR-2026-0002`); **When** đổi clock sang 2027 **Then** đếm lại từ `PR-2027-0001`. **Và** `information_schema` xác nhận `code_sequences` vẫn **đúng 2 cột** `(scope, next_value)` — không module nào `ALTER`/`CREATE` lại | **L13** · lỗi **C1** §2.1 · `V1:31` · tiền lệ MF03 (`"mf03_registration_no:" + Year.now()`) · `M06-D05`. MF11 định `CREATE TABLE` lại bảng này ⇒ TC này cũng bảo vệ MF01/02/03/04/05 |
| `INT-049` | MED | **When** `GET /api/v1/inventory/KHONG-TON-TAI` **Then** `404` + `error.code = "ING-404"` (**không** phải `IAM-404` — `DEFAULT_PREFIX`); lặp cho cả 7 resource MF06 | **L14** · `common/error/ModuleCodeResolver` (đã merge, ghi sẵn `ING` cho MF06) · **MSG-137** — assert **nguyên văn** *"Resource not found."* |
| `INT-050` | HIGH | **When** Flyway chạy xong **Then** `flyway_schema_history` có `V10`/`V11` = `Success`, **và không dòng nào** `success = false` hoặc checksum mismatch cho `V1`…`V9` | **L16** · §0-BIS (*"tuyệt đối không sửa file V đã merge — sửa file cũ làm `migrate` fail trên máy mọi người khác"*) · `M06-D14` |
| `INT-051` | **CRIT** | 🆕 **D12** — xem TC đầy đủ ngay dưới bảng | **D12** §6 Workflow · `M06-D07` |

---

#### `M06-TC-INT-051` — 🆕 **D12**: field dẫn xuất và field sai kiểu phải **KHÔNG** ghi vào DB

**Severity:** `CRITICAL`
**Feature Under Test:** `POST /api/v1/receiving-sessions` · `PATCH /api/v1/inventory/{code}/storage-zone` · `POST /api/v1/inventory-transactions/adjustments`
**Test File:** `integration/Mf06WriteContractIT.java`
**TDD Phase:** 🔴 RED
**Oracle Source:** **D12** (`02_Module_Completion_Workflow.md` §6) · **TDS §9.2 `M06-D07`** (bảng field dẫn xuất) · mẫu `TestSpec-MF05` **TC-027**

**Bối cảnh:** bẫy Jackson *"200 mà không lưu gì"* — field không khớp bị **bỏ qua im lặng**: không lỗi, không cảnh báo, form báo thành công. MF03 mất gần hai phiên làm việc mới tìm ra (`PATCH /students/{code}` với `{"class":"4A"}`). MF05 gặp **biến thể nguy hiểm hơn**: **cùng tên, khác KIỂU** (`dishes` là số đếm ở response nhưng payload gửi mảng) — review bằng mắt thấy *"trông như khớp"*.
TDS v1.0 của MF06 dính **cả hai** biến thể ở `POST /receiving-sessions` (xem **§2.-1**, `M06-D07`).

**Test Steps:**

| # | Gửi gì | Assert |
|---|---|---|
| a | `POST /receiving-sessions` với `receivedAt`, `gpsVerified: true`, `status: "RECEIVED"`, `supplierStatus: "APPROVED"` (**4 field dẫn xuất**) | `201`, nhưng DB: `received_at` = clock server, `gps_verified` = kết quả tính từ `lat/lng`, `status`/`supplier_status_at_receipt` = giá trị server suy ra — **không** giá trị nào từ client |
| b | `POST /receiving-sessions` với `lines[].batchId` thay vì `lines[].supplierBatchId` (**đúng khái niệm, sai tên** — đúng lỗi v1.0) | `400` + `ING-422`/MSG-138, **không** phải `201` với `lines` rỗng. Đếm `receiving_lines` **không đổi** |
| c | `POST /receiving-sessions` với `lines[].ingredientId: {"code":"ING-THIT-LON-VAI"}` (**đúng tên, sai KIỂU** — object thay vì string) | `400`, **không** `201`. Đếm dòng **không đổi** |
| d | `PATCH /inventory/{code}/storage-zone` với `{"remaining": 99, "status": "IN_STOCK", "isLocked": false}` (**3 field dẫn xuất**, không có `storageZone`) | `400` (thiếu field bắt buộc `storageZone`), **không** `200`. `inventory_batches` **không đổi** — đặc biệt: **không** sinh cột `remaining`/`status` nào |
| e | `POST /inventory-transactions/adjustments` với `{"batchId":"…","delta":"-2 lít","reasonCode":"SPOILAGE","managerSignature":"…"}` (`delta` là **chuỗi đã format**, đúng thứ v1.0 trả ra ở response) | `400`. Đếm `inventory_transactions` **không đổi**. *(Ca này bắt đúng hệ quả của `M06-D06`: nếu response trả `"-2 lít"` thì FE sẽ gửi lại chính chuỗi đó khi sửa)* |
| f | `POST /inventory-transactions/adjustments` với `performedBy: "U-9999"` (dẫn xuất — lấy từ JWT) | `201`, nhưng `performed_by_user_id` = user của **JWT**, không phải `U-9999` |

**Expected Result (PASS):** b–e trả **4xx**; a và f trả `2xx` nhưng **DB mang giá trị server**, không giá trị client. Sau **mỗi** bước, `COUNT(*)` của 4 bảng liên quan bằng đúng giá trị trước đó (trừ a/f đúng +1 dòng hợp lệ).
**Expected Result (FAIL):** bất kỳ bước b–e nào trả `2xx` ⇒ **form FE báo lưu thành công mà DB không đổi** — lớp lỗi mà rà tài liệu không bắt được và người dùng chỉ phát hiện khi số liệu kho sai.
**Current Status:** 🔴 Not written
**Implementation Note:** bật `spring.jackson.deserialization.fail-on-unknown-properties=true` **không** đủ và **không** phải cách làm — nó biến mọi field thừa thành lỗi, kể cả field dẫn xuất mà FE lỡ gửi kèm (ca a/f phải là `2xx`). Đúng cách: DTO **Request** khai **chỉ** field ghi được; field dẫn xuất **không có mặt** trong Request record ⇒ tự bị bỏ; field bắt buộc gắn `@NotNull` ⇒ vắng thì `400`; sai kiểu ⇒ Jackson ném `HttpMessageNotReadableException` → `400` qua `GlobalExceptionHandler`.

---

#### `M06-TC-SEC-016` — 🆕 **D10-BIS**: ảnh bằng chứng là tài liệu RIÊNG TƯ

**Severity:** `HIGH`
**OWASP:** `A01:2021 — Broken Access Control`
**Feature Under Test:** `POST /api/v1/receiving-sessions` (`lines[].rejectDocumentIds`) · `POST /api/v1/supplier-batches/{code}/certificates` · `GET /api/v1/receiving-sessions/{code}`
**Test File:** `security/Mf06DocumentPrivacyIT.java`
**TDD Phase:** 🔴 RED
**Oracle Source:** **D10-BIS.2** (bảng phân loại xếp *"chứng nhận lô của MF06"* vào **riêng tư**) · **D10-BIS.3** (tham chiếu bằng **id tài liệu**, gửi tên file → **422**) · **D10** (`common/storage/StorageService`) · **L17** · `M06-D15`

**Test Steps & Expected:**
1. `POST /receiving-sessions` với `lines[].rejectDocumentIds: ["rej-4412-1.jpg"]` (**tên file**) → **`422`**, không đoán ý (D10-BIS.3).
2. `POST /receiving-sessions` với `rejectDocumentIds` là **id tài liệu hợp lệ** đã upload trước → `201`; DB `reject_photo_keys` chứa **storageKey**, **không** chứa chuỗi bắt đầu bằng `http`.
3. `GET /receiving-sessions/{code}` → duyệt **toàn cây JSON**, assert **không** field nào chứa `"http"`, `"cdn."`, hay `"/uploads/"` — response trả **id tài liệu**, không trả URL/`storageKey` thô.
4. Upload chứng nhận lô `.svg` đổi tên thành `.pdf` (magic bytes là SVG) → **`400`** + **MSG-009 nguyên văn** — lớp chặn SVG/XSS phải chạy **ở backend**, không presign (D10-BIS.1).

**Expected Result (FAIL = lỗ hổng):** bước 3 lộ URL công khai ⇒ ảnh bằng chứng ATTP và chứng nhận lô của nhà cung cấp **đọc được bởi bất kỳ ai có URL** — cùng lớp rủi ro mà D10-BIS phát hiện với `partner_documents` của MF04. Bước 4 trả `2xx` ⇒ mất lớp chặn magic-bytes mà `ProfileService` đang có.
**Current Status:** 🔴 Not written
**Implementation Note:** D10-BIS mục 4 ghi việc dọn kèm cho **module đầu tiên cần upload** — rút `hasAllowedMagicBytes`/`MAGIC_JPEG|PNG|PDF` khỏi `ProfileService` (đang `private`) thành `common/storage/FileValidator`. MF06 **là** module đó trong Sóng 1 ⇒ làm luôn ở Bước 2, đừng copy-paste (chỉ cần **một người quên chép hàm magic-bytes** là mất lớp chặn).

---

---

### 4.3 SECURITY TEST CASES (RBAC 7 role + gate bypass)

> Oracle: **§16 Authorization Matrix** của TDS + **BR-007** (7 role: System Admin, School Administration, Medical Staff, Teacher, Parent, Raw Food Supplier, Service Caterers) + **MSG-004/MSG-005** (có MSG thật ⇒ **assert nội dung nguyên văn**).
> `RoleKey` dùng **kebab-case** theo **D6** (`service-caterers`, `medical-staff`, `supplier`, …) — đồng thời là URL segment của FE. ⚠️ D6 lưu ý: **vai trò** `service-caterers` (số nhiều) ≠ **loại đối tác** `SERVICE_CATERER` (số ít, `PartnerType`); và `supplier` = **NCC nguyên liệu**.

---

#### `M06-TC-SEC-001` — Raw Food Supplier **không** đọc được dữ liệu của NCC khác (tenant isolation)

**Severity:** `CRITICAL`
**OWASP:** `A01:2021 — Broken Access Control`
**CWE:** `CWE-639 — Authorization Bypass Through User-Controlled Key` (IDOR)
**Legal:** BR-007 · Bảo mật dữ liệu thương mại giữa các nhà cung cấp cạnh tranh
**Feature Under Test:** `GET /api/v1/purchase-requests/{code}` · `GET /api/v1/deliveries/{code}` · `GET /api/v1/supplier-batches/{code}`
**Test File:** `security/Mf06AuthorizationIT.java`
**TDD Phase:** 🔴 RED
**Oracle Source:** **§16 TDS** (cột RAW FOOD SUPPLIER = ✅ **Own**) · **BR-007** · **MSG-005** nguyên văn: *"Access denied: You do not have permission to access this resource."* · **D1 lý do 3** (*"`partnerId` là khoá cách ly dữ liệu giữa các nhà cung cấp… sót một chỗ là rò dữ liệu chéo giữa các NCC"*)

**Preconditions:** `FX-JWT-CROSS` — token `supplier` của `SUP-002` (HTX Vân Nội). Dữ liệu mục tiêu: `PR-2026-0815`, `DLV-2026-1105`, `BATCH-GF-4411` — đều thuộc `SUP-001` (GreenFarm).

**Test Steps (Attack Simulation):**
1. Đăng nhập bằng token `supplier` của `SUP-002`
2. `GET /api/v1/purchase-requests/PR-2026-0815` (đơn của `SUP-001`)
3. Lặp cho `/deliveries/DLV-2026-1105` và `/supplier-batches/BATCH-GF-4411`
4. `GET /api/v1/purchase-requests` (danh sách) — kiểm rò rỉ qua danh sách
5. `GET /api/v1/purchase-requests?supplierId=SUP-001` — thử **ép scope qua query param**

**Expected Result (PASS = hệ thống an toàn):**
- Bước 2–3: `403` + `error.code = "ING-005"` + `error.message` = **MSG-005 nguyên văn**. *(Chấp nhận `404` **MSG-137** nếu chọn chiến lược không tiết lộ tồn tại — nhưng phải **nhất quán** cả 3 endpoint; TDS §16 ghi `403 ING-005` ⇒ mặc định là 403)*
- Bước 4: `items` **chỉ** chứa đơn của `SUP-002`; `total` khớp số đơn của `SUP-002`, **không** phải tổng toàn hệ thống
- Bước 5: query param `supplierId` bị **bỏ qua hoàn toàn** — kết quả **giống hệt** bước 4 (`supplierId` lấy từ JWT, §16 ghi chú: *"`schoolId`/`supplierId` **luôn** lấy từ JWT, không nhận từ body/query"*)

**Expected Result (FAIL = lỗ hổng):** trả `200` với dữ liệu của `SUP-001` ⇒ NCC đọc được giá hợp đồng và lịch giao của đối thủ. Bước 5 trả dữ liệu `SUP-001` ⇒ scope điều khiển được từ client — lỗ hổng nghiêm trọng nhất của nhóm này.
**Current Status:** 🔴 Not written
**Implementation Note:** scope đặt trong `Specification` (luôn AND `supplier_id = :jwtSupplierId`), **không** chỉ kiểm ở `@PreAuthorize` — `@PreAuthorize` chặn được role nhưng không chặn được dòng.

---

#### `M06-TC-SEC-002` … `M06-TC-SEC-009` — nhóm gọn: ma trận quyền §16

| TC | Sev | Attack / Given → When → Then | Oracle Source (§16 + BR/MSG) |
|---|---|---|---|
| `SEC-002` | **CRIT** | Token `supplier` → `POST /api/v1/receiving-sessions` → `403 ING-005` (MSG-005 nguyên văn) | §16: `POST /receiving-sessions` chỉ ✅ SERVICE CATERERS. NCC tự lập phiếu nhận cho hàng của mình = tự nghiệm thu hàng của mình |
| `SEC-003` | **CRIT** | Token `service-caterers` → `PATCH /api/v1/inventory/{code}/quarantine` → `403`; → `DELETE .../quarantine` → `403` | §16 ghi chú: *"**Cách ly lô là quyền của Medical Staff, không phải bếp** (BR-149): bếp không được tự gỡ khóa lô đang bị nghi"*. Mock đã dính lỗi này ở tầng dữ liệu |
| `SEC-004` | HIGH | Token `parent`, `teacher` → lần lượt **toàn bộ 30 endpoint** MF06 (`@ParameterizedTest` theo bảng §16) → **tất cả** `403 ING-005` | §16 cột TEACHER/PARENT = ❌ **toàn bộ**. Đây là test tham số hoá để không sót endpoint mới thêm |
| `SEC-005` | **CRIT** | Token `service-caterers` **trường A** → `GET /api/v1/inventory` → `items` **không** chứa lô của trường B; → `GET /inventory?schoolId=<trường B>` → kết quả **không đổi**; → `GET /inventory/{code}` với mã lô của trường B → `403`/`404` | §16 (`School` scope) · BR-007, BR-022 · §4.3 TDS Tenant isolation |
| `SEC-006` | **CRIT** | `POST /receiving-sessions` body mang `schoolId` + `supplierId` + `receivedBy` **giả** (trường B / NCC khác / user khác) → các field bị **bỏ qua**; DB ghi giá trị từ **JWT** | **AP-AI-006** (Trust FE identity) · constraint **C2** (*"PHẢI đọc từ JWT — KHÔNG tin body/query"*) |
| `SEC-007` | HIGH | **Không** header `Authorization` → mọi endpoint MF06 → `401` + `code = "ING-004"` + message = **MSG-004 nguyên văn** *"Session expired. Please log in again."*; token hết hạn → cùng kết quả; token sai chữ ký → `401` | §16 (Auth = JWT toàn bộ) · **MSG-004** · TDS §10 |
| `SEC-008` | MED | `POST /api/v1/supplier-batches/{code}/certificates` upload `.exe` → `400` + **MSG-009 nguyên văn** *"File upload failed: Unsupported file type. Only PDF, JPG, and PNG are allowed."*; upload PDF **20MB** → `400` + **MSG-008 nguyên văn** *"File upload failed: File size exceeds the maximum limit of 10MB."*; thứ tự kiểm: **loại file trước, kích thước sau** | **MSG-008/MSG-009** (UC-081/UC-100, traceability §1.5) · TDS §10 (`ING-008`/`ING-009`) |
| `SEC-009` | **CRIT** | Client gửi `receivedAt` quá khứ + `deviceInfo` giả + `gpsVerified: true` tự khai → **cả ba** bị bỏ qua; server tự ghi `receivedAt`, tự tính `gpsVerified` từ `lat/lng` | **BR-073** nguyên văn · ADR-M06-7 (*"server là nguồn sự thật"*) · trùng oracle với `INT-030` (khác tầng: đây là tầng bảo mật, kia là tầng dữ liệu) |

---

#### `M06-TC-SEC-010` — ⚠️ GATE BYPASS: gọi API trực tiếp bỏ qua UI (nhóm bắt buộc theo §2c-3b)

**Severity:** `CRITICAL`
**OWASP:** `A04:2021 — Insecure Design` · `A01:2021 — Broken Access Control`
**CWE:** `CWE-602 — Client-Side Enforcement of Server-Side Security`
**Legal:** Luật ATTP 55/2010/QH12 · QĐ 1246/QĐ-BYT — các gate này là điều kiện pháp lý của bữa ăn tập thể
**Feature Under Test:** toàn bộ 7 gate nghiệp vụ của MF06
**Test File:** `security/Mf06GateBypassIT.java`
**TDD Phase:** 🔴 RED
**Oracle Source:** **CF-M06-12** nguyên văn: *"Mock **KHÔNG enforce bất kỳ BR nào** của MF06 phía 'server'… Mọi kiểm tra BR-070/072/073/074/078/202/204/205 hiện nằm **trong component FE**"* ⇒ *"Backend thật **phải enforce server-side** toàn bộ các BR trên — FE validation chỉ là lớp UX"* · constraint **C8** · §2c-3b (*"nhóm lỗi mà review 29/07 xếp nghiêm trọng nhất"*)

**Test Steps (Attack Simulation):** với **mỗi** dòng dưới đây, gọi thẳng endpoint bằng `MockMvc` (không qua form FE), payload **hợp lệ về cú pháp** nhưng **vi phạm BR**:

| # | Gate | Payload lách | Expected (server chặn) | BR |
|---|---|---|---|---|
| a | NCC phải `APPROVED` | `deliveryId` của `SUP-004` (`SUSPENDED`) | `409 ING-070`, 0 dòng ghi | BR-070 |
| b | Ảnh bằng chứng | `photosCount: 0`, chốt phiên | `400 ING-072` | BR-072 |
| c | Timestamp server | `receivedAt` do client đặt | Bị bỏ qua, server ghi | BR-073 |
| d | Geofence | `lat/lng` cách 640m | `422 ING-015` (MSG-015 nguyên văn) | BR-074 |
| e | Từ chối lô | `rejected: true`, không lý do/ảnh | `400 ING-078` | BR-078 |
| f | Khu nhiệt độ | `storageZone: "DRY"` cho thịt tươi | `422 ING-202` | BR-202 |
| g | Điều chỉnh tồn | `reasonCode: null` hoặc chữ ký rỗng | `400 ING-204` | BR-204 |
| h | Lô hết hạn / cách ly | `issue-slips` chỉ định thẳng `BATCH-GF-4388` (EXPIRED) / `BATCH-MP-8820` (quarantine) | `409 ING-205` / `409 ING-149` | BR-205, BR-149 |

**Expected Result (PASS = hệ thống an toàn):** **cả 8** dòng bị chặn ở server với đúng status + `code`; sau mỗi lần bị chặn, đếm dòng ở `receiving_sessions` / `inventory_batches` / `inventory_transactions` / `issue_slips` **không đổi**.
**Expected Result (FAIL = lỗ hổng):** bất kỳ dòng nào trả `2xx` ⇒ **toàn bộ luật ATTP của module lách được bằng một lệnh `curl`**: nhận hàng của NCC bị đình chỉ, không ảnh bằng chứng, ngoài khu vực cho phép, xuất lô quá hạn vào bữa ăn học sinh. Đây là ca nghiêm trọng nhất trong tài liệu này.
**Current Status:** 🔴 Not written
**⚠️ ORACLE GAP (L6):** dòng a/b/e/f/g/h dùng mã **chưa có MSG** (`ING-070/072/078/202/204/205/149`) ⇒ assert status + `code` + `message` khác rỗng & tiếng Anh; **không** assert nội dung. Dòng d assert **MSG-015 nguyên văn** (có MSG thật).

---

#### `M06-TC-SEC-011` … `M06-TC-SEC-013` — nhóm gọn: biên quyền dễ nhầm

| TC | Sev | Given → When → Then | Oracle Source |
|---|---|---|---|
| `SEC-011` | HIGH | Token `supplier` → `GET /api/v1/inventory` → `403`; → `GET /api/v1/inventory-transactions` → `403`; → `GET /api/v1/preparation-records` → `403` | §16 ghi chú: *"**Raw Food Supplier không bao giờ thấy `/inventory`** — kho là dữ liệu nội bộ của trường"* |
| `SEC-012` | HIGH | Token `medical-staff` → `GET /api/v1/inventory` → `200` (scope trường) · `GET /api/v1/traceability/dish-batches` → `200` · `PATCH /inventory/{code}/quarantine` → `200`; **nhưng** `POST /inventory/issue-slips` → `403` · `POST /inventory-transactions/adjustments` → `403` · `POST /receiving-sessions` → `403` · `POST /preparation-records` → `403` (§16: Medical Staff `preparation-records` là **chỉ đọc**) | §16 dòng `PATCH`/`DELETE quarantine` = chỉ ✅ MEDICAL STAFF; các dòng ghi khác = ❌. BR-149 |
| `SEC-013` | HIGH | Token `school-admin` → `GET /api/v1/receiving-sessions` → `200`, scope `schoolId` từ JWT (**UC-123**) · `GET /inventory` → `200` · `GET /inventory/issue-slips` → `200`; **nhưng** `POST /receiving-sessions` → `403` · `POST /purchase-requests` → `403` · `PATCH /inventory/{code}/storage-zone` → `403` | §16 (`GET /receiving-sessions` = ✅ **School** cho SCHOOL ADMIN) · **UC-123** + BR-076 · **CF-M06-13** (*chưa có màn FE — backend **vẫn** expose*) |

---

#### `M06-TC-SEC-014` — 🆕 **D11**: shape response KHÔNG được đổi theo vai trò

**Severity:** `HIGH`
**Feature Under Test:** `GET /api/v1/inventory` · `GET /api/v1/receiving-sessions` · `GET /api/v1/inventory-transactions` · `GET /api/v1/inventory/issue-slips` · `GET /v3/api-docs`
**Test File:** `contract/Mf06RoleShapeContractIT.java`
**TDD Phase:** 🔴 RED
**Oracle Source:** **D11** (`02_Module_Completion_Workflow.md` §6) · **S7** (`OpenApiMetaContractIT` — tên schema giữ tiền tố `PagedResponse`) · **TDS §9.2 `M06-D08`** · §5 Workflow (*"KHÔNG đặt lại `name=` cho `@Schema` generic"*)

**Bối cảnh — vì sao TC này tồn tại dù MF06 "không có vấn đề":**
Rà §16 cho thấy MF06 **không** có endpoint nào đổi shape theo role — khác biệt giữa vai trò là **tập dòng** (`All`/`School`/`Own`), không phải tập field. Nhưng **MF02 và MF03 cũng vậy lúc viết TDS**, rồi **về sau** mới thêm nhánh rút gọn (`GET /schools` cho Supplier, `GET /students` cho Service Caterers) — và cả hai lần đều làm `PagedResponse<?>` **xoá sạch kiểu**, FE phải đăng nhập từng vai trò gọi thử mới biết. Đây là **test hồi quy**, không phải test tính năng.
Thêm nữa, đếm ngày 03/08: **0/13 TestSpec** có TC thật cho D11 (MF04 có 1 dòng checklist ở Exit Criteria — checklist không phải test, không ai chạy được nó). Sau MF05 là **1/13**. TC này làm MF06 thành **2/13**.

**Preconditions:** 4 token cùng trường `SCH-HN-0042` (`V900`): `U-0001` (`school-admin`), `U-0003` (`service-caterers`), `U-0002` (`medical-staff`), `U-9001` (`system-admin`).

**Test Steps:**
1. Gọi `GET /api/v1/inventory?page=1&pageSize=8` lần lượt bằng **cả 4** token.
2. Với mỗi response, lấy **tập khoá** của `items[0]` (`JsonNode::fieldNames`, sort).
3. So 4 tập khoá với nhau **và** với danh sách field khai ở TDS §9.2.
4. Lặp bước 1–3 cho `/receiving-sessions`, `/inventory-transactions`, `/inventory/issue-slips` (bỏ role không có quyền theo §16).
5. Tải `GET /v3/api-docs`, tra schema của 4 endpoint trên.

**Expected Result (PASS):**
- Bước 3: **4 tập khoá giống hệt nhau**, không thiếu/thừa field nào.
- Bước 5: mỗi endpoint danh sách trỏ schema tên **`PagedResponse<TênDto>`** (`PagedResponseInventoryBatchResponse`, `PagedResponseReceivingSessionResponse`, `PagedResponseInventoryTransactionResponse`, `PagedResponseIssueSlipResponse`) — **giữ tiền tố `PagedResponse`** (S7); `items` là **array of `<TênDto>`**, **không** phải `{}` và **không** phải `PagedResponse` trống.
- Không schema nào của MF06 khai `oneOf` (MF06 chốt một DTO/resource — `M06-D08`).

**Expected Result (FAIL = hồi quy):** tập khoá lệch giữa 2 role mà **không** có `oneOf` trong spec ⇒ type FE sinh từ `npm run gen:api` **sai với ít nhất một vai trò**, và sai im lặng — `tsc` vẫn xanh vì spec khai một shape duy nhất. Hoặc: `items` ra `{}` ⇒ `contract-check.ts` mất hết khả năng bắt lệch field cho **cả 4** resource.
**Current Status:** 🔴 Not written
**Implementation Note:** nếu về sau MF06 **thật sự** cần rút gọn field theo role (ví dụ ẩn `quoteNote`/`cancelReason` khỏi Supplier), **không** sửa TC này thành "cho phép lệch" — phải khai `oneOf` với record chỉ-để-sinh-tài-liệu, mẫu `StudentDtos.PagedResponseStudentResponse` / `SchoolDtos.PagedResponseSchoolResponse`, rồi đổi assert sang "spec khai đủ 2 nhánh".

---

**Implementation Note (§4.3):** `SEC-004` và `SEC-012`/`SEC-013` viết dạng `@ParameterizedTest` lấy nguồn từ **một bảng dữ liệu duy nhất** dịch nguyên §16 TDS (`Mf06AuthMatrix.java` — 30 endpoint × 7 role = 210 ô). Endpoint mới thêm mà quên khai trong bảng ⇒ test fail vì thiếu ô, không im lặng bỏ qua. Đây là cách duy nhất giữ §16 không lạc hậu.

---

### 4.4 CONTRACT TEST CASES (OpenAPI / D8)

> **⚠️ THIẾU ANNOTATION D8 — trạng thái đã biết.** Grep `@Operation|@Schema|@ApiResponse|@Tag(` trên `07_implement/` ra **0 kết quả** (`00_Common_Conventions.md` D8); TDS-MF06 §9.1/§9.2 **không có** annotation nào, §10 chưa khớp 1-1 với `@ApiResponse` (**L8**). Theo đúng chỉ dẫn §2c-3b — *"Nếu endpoint của {MF} chưa có annotation, GHI RÕ là 'thiếu annotation D8' và **báo lại**"* — nhóm này **vẫn được viết trước** (TDD) và sẽ **FAIL** tới khi TDS §9.2 + controller bổ sung annotation. Đó là Red Gate hoạt động đúng, không phải test sai.
> **⚠️ D8 đang chờ Nam approve** (đề xuất 30/07/2026). Nhóm này vẫn làm vì *"annotation không xung đột gì với D1–D7"* (§2c-2). Nếu Nam **không** approve D8, nhóm `CON-*` chuyển sang `@Disabled("D8 rejected")` — **không xoá**, để lưu vết.
> Giá trị thật của nhóm này: `/v3/api-docs` là **nguồn sinh type FE** (`review_fix_plan.md` T3.4 `openapi-typescript`). Spec sai ⇒ type FE sinh sai theo ⇒ lệch D1/D2/D4/D6 **quay lại bằng đường khó thấy hơn**, nằm trong file sinh tự động.

| TC | Sev | Assertion trên `GET /v3/api-docs` | Luật D8 / Quy ước |
|---|---|---|---|
| `CON-001` | **CRIT** | Quét **toàn bộ** `components.schemas.*.properties`: (a) **không** property tên `id` (hoặc `*Id`) nào có `format: "uuid"`; (b) `example` của mọi field `id`/`*Id` khớp khuôn mã nghiệp vụ (`^[A-Z][A-Z0-9]*(-[A-Z0-9]+)+$` — `BATCH-MP-8842`, `SUP-001`, `RCV-20260702-02`, `MENU-2026-27`); (c) **không** `example` nào trong toàn spec là chuỗi UUID | **S6** · **D1** · Team Plan §2c-3b (a) |
| `CON-002` | **CRIT** | Với **mọi** operation và **mọi** response code `4xx`/`5xx`: `content["application/json"].schema.$ref` = `"#/components/schemas/ErrorResponse"`; `ErrorResponse` có đúng 3 field `code`/`message`/`details` với `details` là **array** (không nullable) | **S3** · **D2** · §2c-3b (b) |
| `CON-003` | **CRIT** | Mọi `enum` trong spec khớp **đúng** bảng 8 enum của **D6**: `MealType = [BREAKFAST, LUNCH, SNACK]` (⚠️ **không** có `"Bữa chiều"`), `PartnerType`, `RoleKey` (kebab-case), `NotificationType` (7 giá trị), `ConsumptionStatus` (4 giá trị **gồm** `PENDING`), `BiometricStatus` (3 giá trị), `AllergySeverity`, `HealthHistoryEventType`. Enum riêng MF06 (`StorageZone`, `TransactionType`, `ReasonCode`, `ReceivingStatus`, `IssueSlipStatus`, `ShelfStatus`) toàn bộ **SCREAMING_SNAKE**; `ShelfStatus` chứa **`IN_STOCK`** không chứa `ACTIVE` (**L2**). **Và** grep source: `@Schema(allowableValues` → **0** kết quả | **S5** · **D6** · §2c-3b (c) · **L2**/**L4** |
| `CON-004` | HIGH | Mọi property tên khớp `.*(Date\|At\|date_.*\|.*_date\|.*_at)$` có `type: "string"` **và** `format` ∈ {`date`, `date-time`}; `example` là chuỗi **ISO-8601**; **không** `example` nào khớp `^\d{2}/\d{2}/\d{4}` | **S4** · **D4** · §2c-3b (d) · **L3** |
| `CON-005` | HIGH | Mọi operation `GET` trả danh sách (7 resource MF06) có response `200` là schema `PagedResponse` với đúng 5 property `items`/`total`/`page`/`pageSize`/`pageCount`; **không** wrapper `data`/`result`; `PagedResponse` khai **một lần** ở `components.schemas` (không nhân bản inline mỗi endpoint) | **S7** · Team Plan **§2b-1** |
| `CON-006` | HIGH | (a) **Mỗi** controller MF06 có `@Tag(name = "MF06 — Ingredient & Inventory Management")` — đúng tên module ở Team Plan **§2c-0**, gom đúng **một** nhóm trong `swagger-ui`; (b) **mọi** operation có `description` chứa **≥1 mã UC** (`UC-\d{3}`) **và** (trừ UC-139 vốn không có BR ở §1.5) **≥1 mã BR** (`BR-\d{3}`); (c) có đúng **một** `securitySchemes.bearerAuth` (`http`/`bearer`/`JWT`); (d) `/v3/api-docs` + `/swagger-ui/index.html` trả `200` **không cần** token (`permitAll`) | **S1**, **S2**, **S8** · D8 "Lưu ý hạ tầng" (*không permitAll thì T3.4 `openapi-typescript` không tải được spec*) |

**Mẫu code (`contract/OpenApiContractIT.java`):**

```java
@Test
@DisplayName("D8/S6 + D1 — không field id nào là UUID trong spec, example là mã nghiệp vụ")
void noUuidInPublicSchema() {
    var spec = JsonPath.parse(restTemplate.getForObject("/v3/api-docs", String.class));

    List<Map<String, Object>> idProps = spec.read(
        "$.components.schemas..properties[?(@.description || true)]");           // quét toàn bộ
    var uuidFormat = spec.read(
        "$.components.schemas..properties[?(@.format == 'uuid')]", List.class);
    assertThat(uuidFormat).as("S6/D1 — UUID là PK nội bộ, KHÔNG lộ ra spec").isEmpty();

    List<String> examples = spec.read("$..example", List.class).stream()
            .filter(String.class::isInstance).map(String.class::cast).toList();
    assertThat(examples)
        .as("S6 — không example nào là UUID")
        .noneMatch(e -> e.matches("(?i)^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));
}

@Test
@DisplayName("D8/S2 — mọi @Operation.description chứa UC ID + BR ID (truy vết sống trong runtime doc)")
void everyOperationCitesUcAndBr() {
    var spec  = JsonPath.parse(restTemplate.getForObject("/v3/api-docs", String.class));
    Map<String, Map<String, Map<String, Object>>> paths = spec.read("$.paths");

    var missing = new ArrayList<String>();
    paths.forEach((path, ops) -> ops.forEach((method, op) -> {
        if (!path.startsWith("/api/v1/")) return;
        var desc = String.valueOf(op.getOrDefault("description", ""));
        boolean hasUc = desc.matches("(?s).*UC-\\d{3}.*");
        boolean hasBr = desc.matches("(?s).*BR-\\d{3}.*");
        if (!hasUc || !hasBr) missing.add(method.toUpperCase() + " " + path + " → \"" + desc + "\"");
    }));

    assertThat(missing)
        .as("S2 — description phải chứa UC ID + BR ID; hiện thiếu annotation D8 (L8)")
        .isEmpty();
}
```

---

### 4.5 E2E TEST CASES

| TC | Sev | Kịch bản end-to-end | Oracle Source | Expected FAIL |
|---|---|---|---|---|
| `E2E-001` | **CRIT** | **Trục xương sống** (Team Plan §3): `POST /purchase-requests` (UC-021) → `POST /purchase-requests/{code}/quote` (NCC báo giá) → `POST .../accept-price` → `POST .../confirm-schedule` (UC-015) → `POST /deliveries` (UC-083) → `PATCH /deliveries/{code}/status` (UC-092) → `POST /receiving-sessions` (UC-060) → `PATCH /inventory/{code}/storage-zone` (UC-061) → `GET /inventory/issue-plan` → `POST /inventory/issue-slips` (UC-045) → `POST /preparation-records` (UC-066) → **truy ngược**: `GET /traceability/dish-batches?dishId=…&serviceDate=…&mealType=LUNCH` trả đúng lô đã xuất và đúng NCC. **Assert từng mắt xích nối bằng KHOÁ** (`ingredient_id`, `dish_id`, `menu_id`, `issue_slip_id`, `receiving_session_id`, `supplier_id`), **không** bằng nhãn | **BR-076** nguyên văn (*"truy xuất nguồn gốc đầy đủ"*) · **ràng buộc C6** · Team Plan §3 (*"nối bằng **ID**, không phải tên"*) · vòng đời §6.4/§6.5 TDS · CF-M06-5 (duyệt giá **1 vòng**, không dual-authorization $5.000) | Đứt bất kỳ mắt xích: `GET /traceability/dish-batches` trả rỗng hoặc thiếu lô ⇒ khi có sự cố ATTP thật, không trả lời được *"món này nấu bằng lô nào, lô nào của NCC nào"* — đúng câu hỏi §1 TDS nói module này tồn tại để trả lời |
| `E2E-002` | **CRIT** | **Điều tra sự cố (MF11 ↔ MF06):** MF11 mở sự cố với món nghi vấn (`"Gà rang gừng"`, bữa `2026-06-30`) → Medical Staff `GET /traceability/dish-batches` thấy `BATCH-MP-8820` → `PATCH /inventory/BATCH-MP-8820/quarantine` kèm `incidentId` → `GET /inventory/issue-plan` **không còn** chọn lô đó (loại khỏi tập ứng viên) → `POST /inventory-transactions/adjustments` trên lô đó → `409 ING-149` → đóng sự cố → `DELETE /inventory/{code}/quarantine` → lô **trở lại** tập ứng viên (nếu còn hạn) → chạy kiểm `DONG_SU_CO_MA_CON_LO_KHOA` → `violations = 0` | **BR-149** nguyên văn · luật **`DONG_SU_CO_MA_CON_LO_KHOA`** + **`CACH_LY_KHONG_RO_SU_CO`** + **`CACH_LY_LO_KHONG_LIEN_QUAN_BUA`** · ADR-M06-2 (hai trục độc lập) · `FX-PREP-FAILSTEP` (mắt xích lịch sử thật trong seed) | Đóng sự cố mà lô **vẫn khoá** ⇒ lô nằm khoá **vĩnh viễn**, không màn nào nhắc nữa (nguyên văn ghi chú script §9b) — hàng còn dùng được bị huỷ oan. Hoặc gỡ cách ly làm lô **quá hạn** trở lại tập ứng viên ⇒ cách ly đã ghi đè `shelfStatus`, đúng lỗi ADR-M06-2 dẹp |

---

## 5. Red-Green-Refactor Tracker

| TC ID | Test File | 🔴 RED confirmed | 🟢 GREEN (commit) | 🔵 REFACTOR note |
|---|---|---|---|---|
| `M06-TC-001` … `003` | `service/FefoAllocatorTest.java` | `[ ]` | | tách `Comparator` FEFO thành hằng dùng chung với `issue-plan` |
| `M06-TC-004` … `006` | `domain/InventoryBatchTest.java` | `[ ]` | | `shelfStatus`/`isLocked` nhận `Clock` thay `LocalDate` nếu service khác cũng cần |
| `M06-TC-007` … `008` | `service/BomQuantityCalculatorTest.java` | `[ ]` | | đưa `Set.of("kg","g","lít","ml")` ra `common/` nếu MF05 dùng lại |
| `M06-TC-009` … `013` | `service/InventoryServiceTest.java`, `ReceivingServiceTest.java` | `[ ]` | | gom 7 gate BR thành `ReceivingGuard` để `SEC-010` chỉ cần 1 điểm vào |
| `M06-TC-014` … `016` | `service/DeliveryServiceTest.java` | `[ ]` | | |
| `M06-TC-017` … `020` | `web/InventoryResponseSerializationTest.java`, `service/GeofenceValidatorTest.java` | `[ ]` | | |
| `M06-TC-INT-001` … `006c` | `web/ReceivingSessionIT.java` | `[ ]` | | `counts()` helper dùng chung cho mọi IT đếm bảng |
| `M06-TC-INT-007` … `013` | `web/InventoryIT.java`, `IssueSlipIT.java`, `InventoryTransactionIT.java` | `[ ]` | | |
| `M06-TC-INT-014` … `021`, `031` | `invariant/SemanticAuditInvariantIT.java` | `[ ]` | | 24 luật dùng chung `assertRule()`; cân nhắc sinh từ bảng để không sót luật mới |
| `M06-TC-INT-022` … `024b` | `web/InventoryIT.java` (nhóm quarantine) | `[ ]` | | |
| `M06-TC-INT-025` … `030` | `web/PagingContractIT.java` | `[ ]` | | base class phân trang dùng lại cho 12 module còn lại (Nam) |
| `M06-TC-SEC-001` … `013` | `security/Mf06AuthorizationIT.java`, `Mf06GateBypassIT.java` | `[ ]` | | `Mf06AuthMatrix.java` = bảng dịch §16, dùng cho `@ParameterizedTest` |
| `M06-TC-CON-001` … `006` | `contract/OpenApiContractIT.java` | `[ ]` | | ⚠️ **kỳ vọng FAIL tới khi bổ sung annotation D8 (L8)** |
| `M06-TC-E2E-001` … `002` | `e2e/TraceabilityBackboneE2E.java`, `IncidentQuarantineE2E.java` | `[ ]` | | |

### 5.1 Red Gate Protocol (CASE 2.0 — GATE-2)

> ⭐ **Gate quan trọng nhất.** Trước khi implement, chạy toàn bộ suite với stub ném exception. **Mọi** test PHẢI FAIL. Test nào PASS ngay → **AP-AI-002 detected** → reject và viết lại.

**Stub cho Red Phase (Java — thay stub TypeScript của template):**

```java
// Red Phase — implementation stub (PHẢI throw)
@Service
public class FefoAllocator {
    public AllocationResult allocate(UUID ingredientId, BigDecimal needed,
                                     List<InventoryBatch> candidates,
                                     Map<String, BigDecimal> remaining, LocalDate today) {
        throw new UnsupportedOperationException("Not implemented — Red Phase stub");
    }
}
```

Với tầng web, stub là controller trả `501 Not Implemented` — **không** trả `200` rỗng (`200` rỗng làm một số assert `isEmpty()` xanh oan).

**Red Gate Verification:**

| TC ID | Stub Result | Expected | Actual | Root Cause (nếu PASS bất thường) |
|---|---|---|---|---|
| `M06-TC-001` | `throw UnsupportedOperationException` | 🔴 FAIL | ☐ FAIL ☐ PASS | ☐ Tautology ☐ Shared state ☐ Hallucinated import |
| `M06-TC-002` | ⟵ nt | 🔴 FAIL | ☐ FAIL ☐ PASS | |
| `M06-TC-003` | ⟵ nt | 🔴 FAIL | ☐ FAIL ☐ PASS | |
| `M06-TC-005` | ⟵ nt | 🔴 FAIL | ☐ FAIL ☐ PASS | ⚠️ **ca dễ PASS oan nhất**: `isLocked` chưa implement trả `false` mặc định thì 1/4 ô (a) xanh — phải kiểm cả 4 ô trong **một** `@Test` hoặc `assertAll` |
| `M06-TC-INT-002` | controller `501` | 🔴 FAIL | ☐ FAIL ☐ PASS | ⚠️ test assert *"count không đổi"* sẽ **PASS oan** với stub (chưa ghi gì thì đúng là không đổi). ⇒ **bắt buộc** assert `status = 409` **trước** rồi mới assert count |
| `M06-TC-INT-009` | controller không có route | 🔴 FAIL | ☐ FAIL ☐ PASS | ⚠️ route chưa tồn tại thì Spring trả `404`, không phải `405` — test đúng phải FAIL ở bước này, **không** được nới thành `assertThat(status).isIn(404, 405)` |
| `M06-TC-INT-014` … `021`, `031` | migration chưa có CHECK | 🔴 FAIL | ☐ FAIL ☐ PASS | ⚠️ nhóm luật: nếu bảng **rỗng** thì `violations` rỗng ⇒ PASS oan. Chính là lý do assertion `rowsChecked > 0` bắt buộc đứng **trước** |
| `M06-TC-SEC-004` | chưa có `@PreAuthorize` | 🔴 FAIL | ☐ FAIL ☐ PASS | ⚠️ nếu Spring Security mặc định `denyAll` thì `403` ra ngay ⇒ PASS oan mà chưa có RBAC thật. Kiểm chéo: cùng lúc phải có ≥1 test role **được phép** trả `2xx` (`SEC-012`/`SEC-013`) |
| `M06-TC-CON-001` … `006` | chưa có annotation | 🔴 FAIL | ☐ FAIL ☐ PASS | Nhóm này FAIL vì **L8**, đúng kỳ vọng — ghi rõ ở Evidence |

> **⚠️ Ba mẫu PASS-oan riêng của MF06** (ghi ra để reviewer soi đúng chỗ):
> 1. **Assert "không đổi" mà không assert status trước** — stub chưa ghi gì thì mọi assert `count không đổi` đều xanh. Áp cho `INT-002`, `INT-003`, `INT-008b`, `INT-013`, `SEC-010`.
> 2. **Nhóm luật audit chạy trên bảng rỗng** — `violations.isEmpty()` xanh vì không có dòng nào. `rowsChecked > 0` là thứ duy nhất chặn.
> 3. **Giá trị mặc định của enum/boolean trùng expected** — `isLocked()` chưa implement trả `false`, `shelfStatus()` trả `null`; ô nào expect `false`/`null` sẽ xanh oan. Viết bảng quyết định đủ ô trong một `assertAll`.

**Red Gate Evidence:**

- Stub commit hash: `___`
- Tất cả FAIL? ☐ Yes → **GATE-2 PASS** (T2→T3) → tiếp tục implement
- Log file: `04_Development/Backend/target/red-gate-evidence.log` (`mvn test -Dsurefire.printSummary=true > …`)
- Ngoại lệ được chấp nhận: nhóm `CON-*` FAIL vì **L8** (thiếu annotation D8), **không** phải vì test sai — ghi kèm hash của TDS §9.2 trước khi bổ sung annotation

---

## 6. Entry / Exit Criteria

### Entry Criteria (điều kiện bắt đầu viết code)

- [x] ✅ `TDS-MF06_Backend.md` **v1.1 Approved 2026-08-03** (Bước 1) — chờ review chéo Mạnh/Minh/Thảo + Final Approve của Nam ký xác nhận
- [x] ✅ **L1–L8 đã xử lý ở Bước 1** *(cập nhật 2026-08-03 — xem bảng trạng thái cuối §2.0)*:
  - [x] **L1** ✅ TDS v1.1 §5.2 đã đổi PK `VARCHAR` → `UUID` + `code` + `UNIQUE(school_id, code)` (`M06-D01`). *Còn lại: Mạnh review chéo (Lead Backend/DB).*
  - [x] **L2** ✅ TDS v1.1 sửa đủ 4 chỗ `ACTIVE` → `IN_STOCK` (`M06-D11`). ⏳ **Còn lại: Minh thêm khoá `IN_STOCK` vào `STATUS_BADGE` FE, nhãn *"Còn hàng"*** — việc phía FE, làm ở Bước 3.
  - [x] **L3/L4/L5** ✅ TDS v1.1 §9.2 đã sang ISO-8601 + SCREAMING_SNAKE + number/`unit` tách riêng (`M06-D06`/`M06-D12`). *Còn lại: Minh review contract FE↔BE.*
  - [x] 🟡 **L6 — đã có lời giải, hết chặn.** `M06-D10`: cấp **MSG-145…MSG-153** (không gộp MSG-004/005 — D7 *"một `error.code` không mang hai nghĩa"*, tiền lệ MSG-140/MSG-142). Chuỗi tiếng Anh đã có sẵn ở TDS §10. **Còn lại: Huy commit 9 dòng vào `error_message.md` v2.0 + Nam ký.** ⚠️ **Không còn là điều kiện chặn Exit** — 4 mã dùng chung (MSG-137/138/139/141) đã tồn tại từ v1.6–v1.9 nên phần lớn error-path có oracle ngay; 9 mã còn lại assert `code` + status + *"message tiếng Anh, khác rỗng"*, gắn nhãn `⚠️ ORACLE GAP (L6)`, gỡ nhãn khi MSG lên.
  - [x] **L7** ✅ TDS v1.1 §13.0/§17.2/Phụ lục B sửa 19 → **24** luật (`M06-D16`). *Còn lại: Thảo review (Lead Test/QA).*
  - [x] **L8** ✅ TDS v1.1 §9/§10 đã mang ràng buộc D8. D8 nay **🟡 đang áp dụng thực tế** (11/11 controller MF01+MF02 đã mang đủ annotation, `OpenApiConfig` đã merge) ⇒ nhóm `CON-*` **chạy bình thường**, không còn kịch bản `@Disabled("D8 rejected")`. Nam chỉ cần ký xác nhận.
- [x] ✅ **L9–L18 (§2.-1) đã sửa vào TDS v1.1** dưới `M06-D01`…`M06-D16`; 7 TC nhóm G + `SEC-016` là oracle. *Còn lại: Mạnh review §5.2 v1.1 vì đây là chỗ đổi nhiều nhất.*
- [ ] Flyway **`V10__mf06_ingredient_inventory.sql` (Huy)** có đủ 14 bảng MF06 + **9 CHECK + UNIQUE(school_id, code)** của §5.2 v1.1 — *sửa 2026-08-03: mô hình "V1 = toàn schema, Nam giữ" đã bị bỏ (§0-BIS), xem **L16***
- [ ] `V11__seed_mf06.sql` đã sinh từ `mock-data.ts` **và đã chuyển đổi** `dd/MM/yyyy` → ISO, nhãn tiếng Việt → enum (D4/D6, Team Plan §0)
- [x] ✅ **Nền chung ĐÃ CÓ trong `dev` (kiểm 2026-08-03)**: `common/dto/PagedResponse` + `ErrorResponse` (D2) · `common/error/GlobalExceptionHandler` + `ModuleCodeResolver` + `ApiException` · `common/domain/BaseEntity` (D9) · `common/code/CodeGenerator` + `code_sequences` · `common/query/{ListQuery,SpecSupport}` · `common/reference/ReferenceResolver` · `common/storage/StorageService` (Local + R2) · `common/config/OpenApiConfig` (D8) · `common/enums/` đủ 8 enum D6 · `iam/security/AuthUser` + `@CurrentUser`. **Dùng, không viết lại** (§0-BIS).
- [ ] ⚠️ **3 port — CHƯA CÓ CÁI NÀO.** §0-BIS: *"tính tới 02/08 **chưa có Port nào tồn tại**; module đầu tiên cần thì tự đề xuất với chủ module đó"*. Đường vòng hợp lệ **không chặn Bước 2** (TDS §2.5 #2): `PartnerStatusPort` → dùng `ReferenceResolver` đọc `partners.status` (tra cứu thuần, đã có `partnerCode`/`partnerIdByCode`); `MenuPort` → `ReferenceResolver` đọc `menus`/`dish_ingredients`; `FaceRecognitionPort` → stub theo ADR-000, MF06 chỉ nhận `driverVerificationId` do MF04 cấp (CF-M06-8). Chỉ cần Port thật khi đòi **hành vi** của module khác, không phải khi chỉ **đọc**.
- [ ] ✅ **D13 — seed đủ role** *(đã kiểm `V900__test_fixtures.sql` ngày 2026-08-03)*: `system-admin` (U-9000/U-9001) · `school-admin` (U-0001) · `service-caterers` (U-0003/U-0013/U-0014) · `supplier` (U-0006 `SUP-001` ACTIVE **và** U-0007 `SUP-004` **SUSPENDED** — fixture sẵn cho BR-070) · `medical-staff` (U-0002/U-0012). **Đủ 5/5** role trong `@PreAuthorize` của §16. Mật khẩu chung: **`P@ssw0rd123`** (không phải `Passw0rd123` — sai 4 lần là khoá tài khoản, BR-002).
- [ ] Fixtures §3 TDS-05 đã chuẩn bị (`FX-*`), `FX-CLOCK` bean cố định

### Exit Criteria (Definition of Done)

- [ ] `mvn test` — **toàn bộ unit test xanh**, không `@Disabled`, không `@Ignore` (trừ nhóm `CON-*` nếu D8 bị reject, kèm lý do trong annotation)
- [ ] `mvn verify -Pintegration` — **toàn bộ integration + security + contract test xanh** trên Testcontainers sạch
- [ ] Coverage ≥ **80% line** cho `com.sfms.mf06.service` và `com.sfms.mf06.domain`; **100%** cho `FefoAllocator`, `GeofenceValidator`, `BomQuantityCalculator` *(3 lớp này là nơi luật ATTP sống)*
- [ ] **24/24 luật `semantic-audit.ts`** có test tương ứng, **mỗi luật có assertion `rowsChecked > 0`** — grep `assertRule(` = 24 lần
- [ ] **Oracle Source** — mọi `@Test` có `@DisplayName` chứa mã **BR / UC / MSG / ADR / tên luật**. Kiểm bằng:
  ```bash
  # Mọi test MF06 phải trích nguồn trong @DisplayName
  grep -rL --include=*Test.java --include=*IT.java -E "BR-[0-9]{3}|UC-[0-9]{3}|MSG-[0-9]{3}|ADR-M06|[A-Z_]{6,}" \
    04_Development/Backend/src/test/java/com/sfms/mf06/
  # Expected: no output
  ```
- [ ] **Props Isolation** — không shared mutable state:
  ```bash
  grep -rnE "^\s*(private |protected )?static [^f]" --include=*Test.java --include=*IT.java \
    04_Development/Backend/src/test/java/com/sfms/mf06/ | grep -v "static final"
  # Expected: no output (mọi field static PHẢI final)
  ```
- [ ] **Không phụ thuộc thời gian thực** — seed đóng băng quanh 06–07/2026:
  ```bash
  grep -rnE "LocalDate\.now\(\)|Instant\.now\(\)|LocalDateTime\.now\(\)" \
    --include=*Test.java --include=*IT.java 04_Development/Backend/src/test/java/com/sfms/mf06/
  # Expected: no output (dùng FX-CLOCK)
  ```
- [ ] **D4 trong test** — không expected value nào là `dd/MM/yyyy`:
  ```bash
  grep -rnE "\"[0-9]{2}/[0-9]{2}/[0-9]{4}" --include=*.java --include=*.json \
    04_Development/Backend/src/test/
  # Expected: no output
  ```
- [ ] **D6 trong test** — không expected value nào là nhãn tiếng Việt của enum:
  ```bash
  grep -rnE "\"(Điều chỉnh|Xuất kho|Chilled|Frozen|Dry|spoilage|Bữa (sáng|trưa|phụ|chiều))\"" \
    --include=*.java --include=*.json 04_Development/Backend/src/test/
  # Expected: no output
  ```
- [ ] **Red Gate (§5.1)** — tất cả test FAIL với stub, evidence log đã lưu
- [ ] **Contract Existence** — mọi import trong test resolve: `mvn -q test-compile` không lỗi *(tương đương `tsc --noEmit` của template)*
- [ ] **D8 evidence** (nếu approve) — `/v3/api-docs` đã tải và lưu kèm tài liệu nộp (TDS §11 bước bổ sung); `CON-001..006` xanh
- [ ] **Tiêu chí nghiệp vụ riêng MF06:**
  - [ ] Chuỗi truy xuất `E2E-001` nối thông **bằng khoá**, không mắt xích nào nối bằng nhãn (`grep` không còn `WHERE b.ingredient_label = t.ingredient_label` — AP-AI-011)
  - [ ] `SEC-010` — **8/8** gate chặn được khi gọi API trực tiếp bỏ qua UI
  - [ ] Không endpoint `PUT`/`DELETE` nào tồn tại trên `/inventory-transactions` (sổ append-only)
  - [ ] `information_schema` xác nhận **không có** cột `remaining_quantity`, `status` (lô), `line_count`, `line_total`, `items`, `batches` — field dẫn xuất không thành cột (AP-AI-009)
  - [ ] 2 phiếu xuất song song cùng nguyên liệu **không** cùng trừ một lô (`PESSIMISTIC_WRITE` — ADR-M06-4)
- [ ] Thảo (Lead Test/QA) ký xác nhận đủ coverage happy + error path; Huy ký §2 oracle không bịa mã

### Suspension Criteria (điều kiện tạm dừng)

- **L6 chưa quyết** — 9 nhóm error-path thiếu oracle cho `message`; vẫn chạy được phần status + `code`, nhưng **không** ký Exit
- **D8 chưa quyết** (Nam) — nhóm `CON-*` treo trạng thái
- **`INT-019` chưa chốt** — hai oracle mâu thuẫn giữa luật `LO_CHE_BIEN_KHONG_CO_PHIEU_XUAT` và cột `preparation_batches.extra` của §5.2
- **`M06-TC-006` chưa chốt** — công thức tồn kho có nguy cơ cộng đôi lượng nhập (`initial_quantity` **và** dòng `RECEIPT`), cần Mạnh quyết ở review §5.2
- Flyway `V1` (Nam) chưa có bảng/CHECK của MF06 ⇒ toàn bộ nhóm Integration bị chặn
- `data_model.puml` chưa bổ sung `IssueSlip` + `inventory_transactions.issue_slip_id` (CF-M06-10) ⇒ `INT-008` không có schema tham chiếu

---

## 7. Rollback Plan

> ⚠️ Bản dịch từ template: **không** dùng `npx prisma migrate reset` (sai stack). MF06 dùng **Flyway**; và **quan trọng hơn**: dữ liệu nhận hàng/sổ giao dịch là **chứng từ append-only** (BR-076, Retention ∞ theo §4.2 TDS) — rollback **không được DROP bảng**.

```bash
# ── Chỉ áp dụng cho môi trường DEV/TEST ──────────────────────────────────────

# 1) Dừng ứng dụng
mvn -f 04_Development/Backend/pom.xml spring-boot:stop 2>/dev/null || true

# 2) Revert migration seed MF06 (V11). Flyway Community không có `undo`
#    → dùng script bù, KHÔNG DROP bảng (dữ liệu là chứng từ):
psql "$SFMS_DB_URL" -f 04_Development/Backend/src/main/resources/db/rollback/R12__undo_seed_mf06.sql

# 3) Với Testcontainers: không cần rollback — container bị xoá sau mỗi run.
#    Nếu chạy trên DB dev dùng chung, kiểm lại trạng thái schema:
mvn -f 04_Development/Backend/pom.xml flyway:info
# Expected: V1…V9 Success · V10/V11 Undone (hoặc Pending)

# 4) Revert code
git checkout -- 04_Development/Backend/src/main/java/com/sfms/mf06/
git checkout -- 04_Development/Backend/src/test/java/com/sfms/mf06/

# 5) Smoke test sau rollback
curl -s localhost:8080/actuator/health          # Expected: {"status":"UP"}
curl -s localhost:8080/api/v1/inventory | jq 'keys'
# Expected: ["items","page","pageCount","pageSize","total"]   (đủ 5 field — §2b-1)

# 6) Gap vẫn OPEN → giữ nguyên entry trong 07_implement/review_fix_plan.md
```

**Điều kiện kích hoạt rollback** (TDS §12.1): transaction phiên nhận ghi thiếu bảng · FEFO xuất lô khoá · sổ giao dịch bị sửa được · lệch tồn giữa projection và sổ.
**Không rollback, mà sửa tiến (forward fix):** sai số tồn kho ⇒ ghi giao dịch `ADJUSTMENT` bù có `reasonCode` + `managerSignature` (BR-204), **không** UPDATE/DELETE dòng cũ (ADR-M06-1).

---

## 8. CASE 2.0 Anti-Pattern Detection (AI-Assisted TCs)

> Checklist cho reviewer — test case của tài liệu này **do AI hỗ trợ sinh** (§1.1).

| AP-ID | Anti-Pattern | Dấu hiệu trong TestSpec này | Check | Gate |
|---|---|---|---|---|
| AP-AI-001 | Unconstrained Generation | TC không reference BR/UC/MSG/ADR/tên luật nào ở `Oracle Source` | ☐ | G-0 |
| AP-AI-002 | **Green-from-Birth** | Test PASS với stub (§5.1). ⚠️ 3 mẫu riêng của MF06: assert "count không đổi" mà không assert status trước · nhóm luật chạy trên bảng rỗng · expected trùng giá trị mặc định của enum/boolean | ☐ | **G-2 ★** |
| AP-AI-003 | Implicit Decision | Test giả định quyết định kiến trúc không có trong §3 TDS (VD tự chọn cách lưu tồn, tự chọn chiến lược khoá) | ☐ | G-1 |
| AP-AI-004 | Layer Violation | Test verify controller chứa business logic (VD assert controller tự tính FEFO) | ☐ | G-4 |
| AP-AI-005 | Hallucinated Contract | Test import service/DTO/endpoint **không có** trong TDS §8/§9. ⚠️ Kiểm riêng: `GET /traceability/dish-batches`, `GET /inventory/issue-plan`, `POST /inventory/issue-slips`, `GET /preparation-records/suggest-batches` là **endpoint MỚI** (❌ trong cột "Mock?" §9.1) — có trong TDS nhưng **chưa có** ở mock, không được coi là hallucination | ☐ | G-3 |
| AP-AI-006 | Trust FE identity | Test chấp nhận `schoolId`/`supplierId`/`receivedAt` từ body | ☐ | G-3 |
| AP-AI-009 | Store derived field | Test assert tồn tại cột `remaining`/`status`/`line_count` | ☐ | G-1 |
| AP-AI-011 | **Join bằng tên** | SQL trong test dùng `WHERE b.ingredient_label = t.ingredient_label` thay `ingredient_id` | ☐ | G-1 |
| AP-AI-012 | **FEFO đọc cột `status`** | Test seed cột `status` rồi assert FEFO theo cột đó | ☐ | G-1 |
| AP-AI-017 | **Oracle bịa** *(riêng MF06)* | Test assert **nội dung** `error.message` cho 9 mã **không có MSG** (`ING-070/071/072/076/078/149/202/204/205`) — nội dung đó **không tồn tại** ở nguồn nào, assert nó là tự bịa oracle (**L6**) | ☐ | **G-0 ★** |
| AP-AI-018 | **Tên luật lạc hậu** *(riêng MF06)* | Tên luật `semantic-audit.ts` lấy từ TDS §13 thay vì từ **script**. Tiền lệ: `HAN_LUU_KHONG_DU_72H` đã đổi thành `HAN_LUU_KHONG_DU_24H` (30/07/2026) mà TDS còn trích tên cũ | ☐ | G-3 |
| AP-AI-019 | **Định dạng theo mock** *(riêng MF06)* | Expected value lấy định dạng từ mock: `dd/MM/yyyy`, `"Chilled"`, `"Điều chỉnh"`, `"40 kg"`, `"9.800.000 ₫"` — mock **cố ý đóng băng** (Team Plan §0), không phải nguồn định dạng | ☐ | G-0 |

**Kết quả review:**

- [ ] Không phát hiện anti-pattern nào → TestSpec approved
- [ ] Phát hiện AP → ghi vào bảng dưới → fix trước khi implement

| AP detected | TC ID | Mô tả | Fix action | Fixed? |
|---|---|---|---|---|
| `AP-AI-___` | `M06-TC-___` | | | ☐ |

---

## 9. DANH SÁCH MÃ ĐÃ TRÍCH DẪN (đối chiếu nhanh khi review)

**Use Cases (21) — nguồn `traceability_matrix.md` §1.5** *(đúng bảng §2c-0: MF06 = **§1.5**, không phải §1.6)*:
UC-015, UC-021, UC-039, UC-043, UC-044, UC-045, UC-060, UC-061, **UC-062**, **UC-063**, UC-064, UC-066, UC-072, UC-081, UC-083, UC-092, UC-100, UC-107, UC-109, UC-123, UC-139.
*(UC-062/UC-063 chỉ có trong §1.5, không có UC spec trong UC file — CF-M06-1. TestSpec chỉ phủ **phần dữ liệu lô MF06 cấp**; biểu mẫu Bước 1/Bước 2 thuộc MF07.)*

**Business Rules (12 — miền MF06, §6 `business_rules.md`):**
BR-070, BR-071, BR-072, BR-073, BR-074, BR-075, BR-076, BR-077, BR-078, BR-202, BR-204, BR-205.

**Business Rules chỉ tham chiếu (không phải BR của MF06):**
BR-007 *(RBAC — §4.3 Security)* · BR-022 *(tenant isolation)* · BR-028 *(geofence + dải toạ độ — MF02)* · BR-046/BR-048 *(hiệu lực chứng nhận NCC — MF04, `INT-031`)* · BR-051 *(sinh trắc tài xế — MF04)* · BR-149 *(cách ly lô — MF11)* · BR-170 *(audit trail)* · BR-079/BR-083/BR-090/BR-092/BR-093/BR-095/BR-096 *(§8 — trích cho UC-062/063, biểu mẫu thuộc MF07)*.

**Business Rules KHÔNG dùng (UC file cite nhầm — §2.3 TDS):**
~~BR-079~~/~~BR-080~~ *(GPS 50m & chuỗi lạnh 5°C — CF-M06-2)* · ~~BR-089~~ *(Deprecated 11/07/2026 — CF-M06-3)* · ~~BR-090~~ *(quét barcode — CF-M06-4)* · ~~BR-135~~ *(lệch lịch giao — CF-M06-6)*.

**System Messages — assert NGUYÊN VĂN (có trong `error_message.md` v1.5):**

| MSG | Nội dung nguyên văn | Dùng ở TC |
|---|---|---|
| MSG-004 | Session expired. Please log in again. | `SEC-007` |
| MSG-005 | Access denied: You do not have permission to access this resource. | `SEC-001` … `SEC-013` |
| MSG-008 | File upload failed: File size exceeds the maximum limit of 10MB. | `SEC-008` |
| MSG-009 | File upload failed: Unsupported file type. Only PDF, JPG, and PNG are allowed. | `SEC-008` |
| MSG-015 | Receiving action blocked: GPS location verification failed (out of tolerance). | `M06-TC-020`, `INT-004`, `SEC-010(d)` |
| MSG-018 | Inventory shortage: Insufficient stock of ingredient [Ingredient Name]. | `INT-011` *(kiểm cả phép thay placeholder)* |
| MSG-041 | Ingredient delivery received. Inventory levels updated. | `INT-001`, `INT-005` |
| MSG-042 | Inventory transaction recorded and stock adjusted. | `INT-008`, `INT-010`, `INT-012` |
| MSG-137 | Resource not found. | `INT-020`, `INT-023` *(v1.5 — dùng chung mọi module)* |
| MSG-138 | Validation failed: one or more required fields are missing or invalid. | `M06-TC-019`, `INT-010` *(v1.5 — dùng chung mọi module)* |
| MSG-029 | Profile updated successfully. | `M06-TC-016` *(UC-092 cập nhật trạng thái giao hàng)* |
| MSG-052 | Ingredient batch [Batch ID] is close to expiration ([Expiration Date]). | *(cảnh báo — `INT-028` nhánh `EXPIRING_SOON`)* |
| MSG-054 | Safety stock level reached for ingredient [Ingredient Name]. Reorder recommended. | *(cảnh báo — `INT-011`)* |
| MSG-065 | Delivery delay alert: Supplier delivery time deviates from schedule. | `M06-TC-016` |
| MSG-016 | Checklist validation failed: Mandatory safety checks are missing. | *(UC-066 sổ chế biến thiếu field bắt buộc)* |
| MSG-025 | Database transaction failed. Rollback executed. | `INT-001` *(nhánh rollback)* |
| MSG-013 / MSG-014 | Supplier food safety certificate has expired… / Face recognition failed… | `INT-031`, `M06-TC-013` *(cross-module MF04)* |

**⛔ Mã lỗi KHÔNG có MSG — chỉ assert status + `code`, KHÔNG assert nội dung message (L6):**
`ING-070`, `ING-071`, `ING-072`, `ING-076`, `ING-078`, `ING-149`, `ING-202`, `ING-204`, `ING-205`, `ING-405`, `ING-409`.
*Đã kiểm `error_message.md` v1.5: MSG-104–138 cấp cho MF01/MF02/MF03/MF04/MF08/MF09/MF10 — **không** cấp cho MF06; dòng §4 Tóm tắt của MF06 không đổi. Cần Huy/Nam quyết (Entry Criteria §6).*

**MSG KHÔNG implement:** ~~MSG-055~~ *(High temperature alert in inventory storage area — gắn BR-089 đã Deprecated, không có trong §1.5 — CF-M06-3)* · ~~MSG-017~~ *(nhiệt độ nấu — miền MF07)*.

**Luật `semantic-audit.ts` đã port (24) — tên copy NGUYÊN VĂN từ script:**
`XUAT_LO_HET_HAN`, `XUAT_TRUOC_KHI_NHAP`, `PHIEU_SAI_NGUYEN_LIEU_CUA_LO`, `NHAP_LO_DA_HET_HAN`, `CACH_LY_KHONG_RO_SU_CO`, `DONG_SU_CO_MA_CON_LO_KHOA`, `LO_TRO_MAT_HANG_KHONG_TON_TAI`, `HSD_TRUOC_NGAY_SX`, `DONG_GOI_TRUOC_KHI_SX`, `TEN_LO_KHAC_MAT_HANG`, `GIAO_TRUOC_KHI_DAT`, `CHUYEN_GIAO_LO_KHAC_NCC`, `MUA_HANG_SAU_KHI_GIAY_TO_HET_HAN`, `LO_KHONG_CO_PHIEU_NHAN`, `NHAN_LO_TU_PHIEU_BI_TU_CHOI`, `NGAY_NHAP_LO_KHAC_PHIEU_NHAN`, `NCC_LO_KHAC_NCC_PHIEU_NHAN`, `NHAN_HANG_SAU_KHI_CHUNG_NHAN_HET_HAN`, `PHIEU_GHI_SAI_TRANG_THAI_NCC`, **`CHE_BIEN_DUNG_LO_HET_HAN`** ⭐, **`LO_CHE_BIEN_KHONG_CO_PHIEU_XUAT`** ⭐, **`SO_CHE_BIEN_TRO_LO_KHONG_TON_TAI`** ⭐, **`CHE_BIEN_SAI_NGAY_THUC_DON`** ⭐, **`CACH_LY_LO_KHONG_LIEN_QUAN_BUA`** ⭐.
⭐ = 5 luật TDS §13.0 chưa port (**L7**). Ghi chú §2c-3b: `LO_TRO_MAT_HANG_KHONG_TON_TAI` là 1 trong 3 luật **chỉ có `add()` không có `check()`** — vẫn là luật thật, con số "54" chỉ là số `check()` đăng ký.

**Quyết định kiến trúc được test (inline §3 TDS, không tách file `ADR-MF06_*.md`):**
ADR-M06-1 *(tồn dẫn xuất từ sổ append-only)* · ADR-M06-2 *(`shelfStatus` runtime + cách ly tách trục + `IN_STOCK`)* · ADR-M06-3 *(món↔lô nối bằng khoá)* · ADR-M06-4 *(FEFO loại lô khoá, khoá bi quan)* · ADR-M06-5 *(phiên nhận là 1 transaction, UPSERT lô)* · ADR-M06-6 *(`IssueSlip` là bảng cha thật)* · ADR-M06-7 *(server là nguồn sự thật cho GPS/timestamp)*.

**Quy ước chung được test:** D1 *(`M06-TC-017`, `INT-027`, `CON-001`)* · D2 *(`assertErrorEnvelope`, `CON-002`)* · D3 *(mọi path `/api/v1`)* · D4 *(`M06-TC-018`, `INT-026`, `INT-029(d)`, `CON-004`)* · D5 *(`INT-006` — `body.id` trùng → 409)* · D6 *(`M06-TC-019`, `INT-010`, `INT-028`, `CON-003`)* · D7 *(mọi message tiếng Anh)* · D8/S1–S8 *(`CON-001` … `CON-006`)*.

**Ràng buộc `data_model.puml`:** **C6** *(truy xuất nối bằng khoá — `M06-TC-015`, `INT-016`, `E2E-001`)* · **C7** *(hệ số thải bỏ — `M06-TC-007`, `M06-TC-008`)*.

---

## 10. Việc cần báo lại (theo yêu cầu §2c-3b — "GHI RÕ và báo lại")

| # | Việc | Người quyết | Chặn gì |
|---|---|---|---|
| 1 | ⛔ **9 mã lỗi chặn cứng của MF06 không có MSG** trong `error_message.md` v1.5 (**L6**) — cấp MSG mới hay chốt dùng `code` module + MSG-138? | **Huy** (Lead Traceability) + **Nam** (approve) | Oracle cho `error.message` của 9 nhóm error-path; **Exit Criteria §6** |
| 2 | **TDS §5.2 chưa áp D1** — PK còn là `VARCHAR` mã nghiệp vụ (**L1**) | **Mạnh** (Lead Backend/DB) | `M06-TC-017`, `INT-027`, `CON-001` |
| 3 | **TDS còn 4 chỗ ghi `ACTIVE`** trong khi ADR-M06-2 đã đổi sang `IN_STOCK` (**L2**) | **Huy** sửa TDS · **Minh** thêm khoá `STATUS_BADGE` FE | `INT-028`, `CON-003` |
| 4 | **TDS §9.2 ví dụ JSON còn `dd/MM/yyyy` + nhãn tiếng Việt + chuỗi đã format** (**L3/L4/L5**) | **Minh** (FE↔BE contract) | `M06-TC-018/019`, `INT-010/012/029` |
| 5 | **TDS §13.0 thiếu 5 luật** `semantic-audit.ts` (**L7**) — sửa 19 → 24 | **Huy** + **Thảo** | `INT-018` … `INT-022` |
| 6 | **D8 chờ Nam approve** (**L8**) — nhóm `CON-*` treo | **Nam** | 6 contract test |
| 7 | **`INT-019` hai oracle mâu thuẫn**: luật `LO_CHE_BIEN_KHONG_CO_PHIEU_XUAT` coi là vi phạm, cột `preparation_batches.extra` của §5.2 lại thiết kế để chứa ca đó | **Huy** + **Thảo** | `INT-019` |
| 8 | **Công thức tồn kho có nguy cơ cộng đôi** lượng nhập (`initial_quantity` **và** dòng `RECEIPT` — `M06-TC-006`) | **Mạnh** (review §5.2) | `M06-TC-006`, `INT-001` |
| 9 | **CF-M06-1 chưa chốt module chủ** cho UC-062/UC-063 (biểu mẫu Bước 1/2) — nguy cơ MF06 và MF07 **cùng** đặc tả một biểu mẫu, hoặc **không ai** test nó | **Huy** + **Thảo** | Ranh giới phủ với TestSpec-MF07 |
| 10 | **CF-M06-13** — UC-123 (School Administration xem hồ sơ nhận hàng) **chưa có màn FE**; `SEC-013` test backend vẫn expose đúng UC | **Minh** bổ sung màn · **hoặc** **Huy** hạ UC-123 xuống "chỉ số dashboard" | `SEC-013` |
| 11 | **`unaccent` ngoài hợp đồng §2b** — `search` hiện chỉ không phân biệt hoa/thường, **không** không phân biệt dấu (`"thit lon"` không tìm ra `"Thịt lợn"`) | **Nam** (chủ hợp đồng §2b) | `INT-029(e)` |

---

*TestSpec MF06 v1.0 — sinh theo `00_Team_Work_Assignment_Plan.md` §2c-3b · template `06_Document/Templates/PHASE-4_Test-Spec.md` (đã dịch stack sang Java/JUnit 5/Testcontainers/Flyway) · tích hợp CASE 2.0 Anti-Pattern Detection & Red Gate Protocol.*
*Trạng thái: **Draft** — chờ review chéo (Thảo §13, Mạnh §5/§9, Minh §8/§9, Huy §2) và Final Approval của Nam.*
