# TEST-DRIVEN DEVELOPMENT SPECIFICATION
# MF08 — Food Sample Retention (Lưu mẫu Thực phẩm)

**Document ID:** `SFMS-MF08-TDD-001`
**Version:** `1.1`
**Date:** `2026-08-03`
**Status:** **`Approved`** *(sau Bước 0 + Bước 1 của `02_Module_Completion_Workflow.md`, 2026-08-03)*
**Standard:** ISO/IEC/IEEE 29119-3:2021 — Software Testing Part 3: Test Documentation
**Author:** Nguyễn Đức Mạnh (ManhNDHE160692) — Lead Backend & DB Reviewer, chủ MF08
**Reviewed by:** `[ ] Nguyễn Phương Thảo — Lead Test/QA (Pending)` · `[ ] Bùi Quang Huy — Traceability (Pending)` · `[ ] Trần Tuấn Minh — FE↔BE contract (Pending)`
**DPO Sign-off:** `[ ] Pending` — test chạm **ảnh bằng chứng + tọa độ GPS + họ tên nhân sự bếp/y tế**; không chạm PII học sinh
**Approved by:** `[ ] Nguyễn Văn Nam — Final Approve (Pending)`
**Classification:** `Internal — Confidential`

**References:**
- `07_implement/MF08_Food_Sample_Retention/TDS-MF08_Backend.md` (`SFMS-MF08-IMP-001`) — Technical Specification
- `07_implement/00_Common_Conventions.md` — **D1–D7 + D9/D10 Approved**, D8 chờ approve *(ưu tiên cao hơn ADR module — xem §2)*
- `07_implement/00_Team_Work_Assignment_Plan.md` §2a (bộ tài liệu), §2b (hợp đồng phân trang/search/filter), §7 (nghiệm thu)
- `07_implement/01_MVP_Backend_Implementation_Plan.md` §3 (stack, nền chung), §5 (Definition of Done)
- `07_implement/review.md` §A (mismatch xuyên module) + **§F (9 mismatch riêng MF08 — 5 blocker)**
- `07_implement/review_fix_plan.md` Phase 0 (D1–D7), T1.1 (mã lỗi cục bộ), dòng 142–143 (F1–F7 MF08), dòng 185 (danh sách sửa FE)
- `02_Requirements/02_UseCases/MF08_Food_Sample_Retention.md` — 5 UC
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` **§1.8** — nguồn UC↔BR↔MSG chuẩn
- `02_Requirements/00_BusinessRules/business_rules.md` §9 (BR-105…BR-114, BR-203, BR-212, BR-215-deprecated) · `error_message.md`
- `03_Design/08_FrontEnd/Web/scripts/semantic-audit.ts` — **10 luật chạm MF08** (mục 3: dòng 78–99 · mục 4: dòng 101–113 · mục 5: dòng 115–141)
- `03_Design/02_ADR/ADR-000.md` — stub GPS (no-op ở dev), `./data/uploads`
- **Quyết định 1246/QĐ-BYT** Điều 1.2 / 6 / 7 / 8 + **Mẫu số 4, Mẫu số 5** (Phụ lục 2) — căn cứ pháp lý của BR-105…BR-114, BR-203
- **Thông tư 14/2011/TT-BYT** Điều 5 khoản 2 — căn cứ pháp lý của BR-212 (bằng chứng bàn giao mẫu)

> **Quy ước TDD:** Tài liệu này mô tả test cases **TRƯỚC** khi viết production code.
> Thứ tự bắt buộc: viết test (`*Test.java`) → chạy → xác nhận **FAIL 🔴** → implement → **PASS 🟢** → refactor 🔵.
> Không mark ✅ nếu `./mvnw test` chưa xanh.
> **Stack test (KHÔNG dùng Jest/Prisma — sai stack):** JUnit 5 · AssertJ · Mockito · Spring Boot Test (`@WebMvcTest`/`@SpringBootTest`) · **Testcontainers PostgreSQL 16** · Flyway · RestAssured (tùy chọn cho nhóm INT).
> **Test data:** SYNTHETIC, seed từ `mock-data.ts` (`FOOD_SAMPLES`, `SAMPLE_LAB_REQUESTS`, `RECEIVING_AUTHORITIES`) → `V{n}__seed_mf08.sql`. **Không dùng PII thật**; ảnh bằng chứng test là ảnh sinh tổng hợp.

---

## CHANGELOG

> **Policy 4.4 — Immutable History:** Không bao giờ xóa thông tin cũ.

| Ngày | Người thực hiện | Nội dung thay đổi |
|------|-----------------|-------------------|
| 2026-08-03 | Nguyễn Đức Mạnh | **v1.1 — Bước 1.** Bốn nhóm sửa, tất cả **đo từ code đã merge** chứ không suy từ tài liệu: **(1) Package `com.sfms.sample` → `com.sfms.mf08`** ở **40 chỗ** (7 tên package + 33 đường dẫn `src/test/java/com/sfms/sample/…`) — §0-BIS khuôn `com.sfms.mf<NN>`; ⚠️ bảng D §2.1 của `02_Module_Completion_Workflow.md` xếp MF08 vào nhóm *"sạch cả 3 cột"* vì lệnh grep của nó **chỉ quét TDS**, mà TDS-MF08 không nhắc package lần nào. **(2) Thêm `L14`–`L17`** — D9 (TDS tự khai cột audit), D11/D12/D13 (không mục nào được nhắc trong v1.0: `grep "D11\|D12\|D13"` ra **0 kết quả**), và mã MSG đã có sẵn mà TDS ghi là thiếu. **(3) Sửa `FX-014`/`FX-015`/`FX-016`** — 3 fixture JWT trỏ **sai role** so với `V900__test_fixtures.sql` (`U-0021` là *school-admin* chứ không phải bếp; `U-0009` là *teacher* chứ không phải y tế; `U-0050`/`U-0033` **không tồn tại**). **(4) Thêm 2 TC bắt buộc** `MF08-TC-SEC-009` (D11 parity) và `MF08-TC-021` (D12) ⇒ **45 → 47 TC**. |
| 2026-08-02 | Nguyễn Đức Mạnh | Khởi tạo — TDD spec cho MF08 (5 UC, 12 BR, 6 MSG). **13 Logic Issue (L1–L13)** encode hành vi đã sửa theo `00_Common_Conventions.md` D1–D10 + `review.md` §F, **không** theo bản TDS-MF08 v1.0 ở những điểm bị ghi đè. **45 test case**: 20 unit · 8 security · 17 integration — trong đó **10 luật `semantic-audit.ts`** chạm MF08 được port (`MF08-TC-INT-011`). |

---

## MỤC LỤC

1. [Thông tin Module](#1-thông-tin-module) · 2. [Logic Issues Resolved](#2-logic-issues-resolved) · 3. [Test Design Specification](#3-test-design-specification-tds) · 4. [Test Case Specification](#4-test-case-specification) · 5. [Red-Green-Refactor Tracker](#5-red-green-refactor-tracker) · 6. [Entry / Exit Criteria](#6-entry--exit-criteria) · 7. [Rollback Plan](#7-rollback-plan) · 8. [CASE 2.0 Anti-Pattern Detection](#8-case-20-anti-pattern-detection-ai-assisted-tcs)

---

## 1. Thông tin Module

| Field | Value |
|-------|-------|
| **Feature / Gap ID** | `MF08` — Food Sample Retention (5 UC theo traceability §1.8) |
| **Module** | `com.sfms.mf08` — Bounded Context: **Food Safety Evidence & Chain of Custody** |
| **Spec gốc** | `SFMS-MF08-IMP-001` (TDS-MF08_Backend.md) + `00_Common_Conventions.md` D1–D10 |
| **Priority** | 🟠 **P1** — mắt xích cuối của trục xương sống (`00_Team_Work_Assignment_Plan.md` §3: *"Mẫu lưu + link `incidentId`/`batchIds`"*) |
| **Sprint** | Tuần 2–3 theo `01_MVP_Backend_Implementation_Plan.md` §4 (sau nút cổ chai MF01/MF02 của Nam) |
| **Milestone** | Cổng cuối Tuần 2 — *"mỗi module P0/P1 CRUD được qua Swagger; trục ID thông giữa các module"* |
| **Data Classification** | **`Confidential`** — hồ sơ lưu mẫu là **bằng chứng pháp lý ATTP** (thanh tra/CDC yêu cầu xuất trình); chứa họ tên nhân sự + tọa độ GPS bếp, **không** chứa PII học sinh |
| **Compliance Scope** | **QĐ 1246/QĐ-BYT** Điều 1.2 (bữa ≥30 suất), Điều 6 (dụng cụ), Điều 7 (khối lượng/niêm phong/thời điểm), Điều 8.1 (2–8°C), Điều 8.2 (lưu ≥24h, chặn hủy khi có sự cố), **Mẫu số 4/5** · **TT 14/2011/TT-BYT** Điều 5.2 · PDPD |
| **Upstream Dependencies** | **MF01** (JWT `AuthUser{userId, fullName, role, schoolId}`; `common/domain/BaseEntity`; `common/storage/StorageService`; `common/enums/`) · **MF02** (`GeofenceQueryPort` — tọa độ trường + `geofence_radius`, BR-109) · **MF05** (`dishId`) · **MF07** (`InspectionGatePort` — phiên PASSED 3/3, **cổng BR-105**) · **MF11** (`IncidentStatusPort` — sự cố mở + cửa sổ điều tra, BR-112/113/114) |
| **Downstream Consumers** | **MF11** (mẫu của ca sự cố + phiếu bàn giao lab), **MF13** (sổ Mẫu số 5 + tỉ lệ tuân thủ), **MF07** (`fridgeTemp` làm nguồn giám sát nhiệt độ tủ lưu sau khi `FridgeTempLog` bị xóa — CF-M08-3) |

### 1.1 AI Generation Context (CASE 2.0)

| Field | Value |
|-------|-------|
| **AI Assisted?** | `Yes` |
| **Constraint Source** | `TDS-MF08_Backend.md` §17.1 (C1–C6) + §10 (bảng mã lỗi `SMP-`) + §16 (auth matrix); `00_Common_Conventions.md` D1–D10; `00_Team_Work_Assignment_Plan.md` §2b |
| **Constraints Injected** | C1 PagedResponse 5 field + `SEARCH_FIELDS` cố định · C2 hạn lưu **24h** (không 72h) + `hoursRemaining` runtime · C3 chỉ Medical Staff bàn giao, 1 transaction, `SENT_TO_LAB` terminal · C4 chuỗi cổng đăng ký BR-105 → BR-109 → BR-203 → BR-107 → BR-110 · C5 khóa hủy BR-112 + khóa bàn giao BR-212 · C6 danh tính/GPS từ JWT+server, tham chiếu bằng ID, append-only; **cộng thêm** D1 (id = mã nghiệp vụ), D2 (envelope lỗi), D4 (ISO-8601), D5 (client-supplied id), D6 (enum chung), D7 (message tiếng Anh), D9 (BaseEntity), D10 (StorageService) |
| **Model** | `claude-opus-5` (theo `00_Team_Work_Assignment_Plan.md` §2c-4: TestSpec dùng Opus vì oracle phải khớp BR/MSG) |
| **Trust Level** | `T2 → T3 (pending Red Gate §5.1)` |

> **Oracle policy (Team Plan §2a):** *"mọi expected value PHẢI trích từ BR/UC/MSG — không dùng giả định AI."* Mỗi test case dưới đây có trường **Oracle Source** trỏ đúng mã BR/UC/MSG/Điều luật/D-decision. Test case nào không trỏ được nguồn → **không viết**, ghi vào §8.1 thay vì bịa kỳ vọng.

---

## 2. Logic Issues Resolved

> **Bắt buộc điền trước khi viết test.** Test encode hành vi **đã sửa**, không phải hành vi trong spec gốc.
> Nguồn sai lệch: `00_Common_Conventions.md` (thứ tự ưu tiên: **file quy ước chung > ADR module trong TDS §3 > hợp đồng mock FE**), `review.md` §F, và **BR-111/BR-113 đã sửa 26/07/2026** (mock FE chưa áp).
> ⚠️ **L1–L6 là chỗ TDS-MF08 v1.0 bị GHI ĐÈ; L7–L11 là chỗ mock FE sai còn TDS đúng** — ai đọc TDS hoặc đọc mock mà bỏ qua bảng này sẽ viết test sai hợp đồng.

| # | Spec gốc (sai / thiếu) | Thực tế (quy ước / BR / policy đã chốt) | Fix áp dụng trong test |
|---|------------------------|------------------------------------------|------------------------|
| **L1** | TDS §5.2: `food_samples` có `id UUID PRIMARY KEY` + `code VARCHAR(32) UNIQUE` nhưng **JSON §9.2 trả `"code": "SMP-20260706-01"` song song với `"id"`** — chưa chốt cái nào là `id` công khai. `review.md` A1 xếp MF08 vào nhóm *"`id` UUID nhưng ví dụ JSON lại trả mã nghiệp vụ"* = **blocker** | **D1 (Approved 30/07)**: `id` công khai **= mã nghiệp vụ** (`code`); path param dùng `code`; FK trong JSON dùng `code` của bảng đích; UUID chỉ là PK nội bộ, **không lộ ra API**. `00_Common_Conventions.md` §D1 "Việc phải làm": *"MF08 — §5.2 thêm `code` + `UNIQUE(school_id, code)`; thêm `@JsonProperty("id")`"* | Mọi test gọi `GET /api/v1/samples/SMP-20260706-01` (**không** UUID). Assert `response.id == "SMP-20260706-01"`, `labRequestId == "LABREQ-2026-001"`, `incidentId == "INC-2026-014"`. **MF08-TC-INT-008** khóa chặn: không JSON nào có UUID ở field `id`/`*Id`. |
| **L2** | TDS §5.2/§9.2 trả ngày dạng chuỗi Việt: `"collectedAt": "06/07/2026 10:15"`, `"retentionDeadline": "07/07/2026 10:15"`, `"disposedAt": "01/07/2026 11:00"`. `review.md` A4 gọi đích danh: *"MF08/MF09/MF11 cam kết giữ `dd/MM/yyyy HH:mm` qua `@JsonFormat`"* = **blocker** | **D4**: JSON **luôn ISO-8601** — ngày `"2026-07-06"` (`LocalDate`), ngày giờ `"2026-07-06T03:15:00Z"` (`Instant`). **D4 ghi đè ADR-M11-5**: *"MF08, MF09, MF11 phải **bỏ `@JsonFormat`** khỏi các field ngày"* | Mọi assert ngày dùng ISO. **MF08-TC-018** khóa chặn: regex `\d{2}/\d{2}/\d{4}` **không** xuất hiện trong bất kỳ response nào của MF08 (kể cả `retentionDeadline`, `sentToLabAt`, `disposedAt`, `uploadedAt` của attachment). |
| **L3** | TDS §5.2 khai `meal_label VARCHAR(40)` + `meal_shift VARCHAR(60)` là **chuỗi tự do** (`"Trưa 06/07"`, `"Bữa trưa — 10:45"`) — không có enum loại bữa; mock FE dùng `MEAL_OPTIONS = ["Bữa sáng","Bữa trưa","Bữa chiều"]` | **D6**: `MealType` chốt **`BREAKFAST` / `LUNCH` / `SNACK`**, SCREAMING_SNAKE trong JSON và DB; nhãn FE ở `lib/labels.ts`. ⚠️ D6 ghi rõ: **"Bữa phụ" ≠ "Bữa chiều"** — chốt nhãn **"Bữa phụ"** cho `SNACK` | Thêm `mealType` (enum) bên cạnh `mealShift` (giờ ăn, text tự do — Mẫu số 5 cột 3 yêu cầu ghi *"bữa ăn (giờ ăn...)"*). **MF08-TC-017** assert serialize `"LUNCH"`, **từ chối** `"Bữa trưa"`/`"Trưa"` gửi lên. Bộ lọc `mealPrefix` của mock được thay bằng `?mealType=LUNCH` (xem **MF08-TC-INT-007**). |
| **L4** | TDS §10: 9 mã cục bộ `SMP-105/107/109/111/112/113/030/203/212` viết **message tiếng Việt** (*"Chưa hết hạn lưu tối thiểu 24 giờ — còn [N] giờ."*). `review.md` A-dòng-27 + `review_fix_plan.md` T1.1 liệt kê đúng 9 mã này | **D7**: backend trả `code` + `message` **tiếng Anh**, nguyên văn `error_message.md`; mã dẫn xuất BR **phải dịch sang tiếng Anh** khi được cấp MSG chính thức. FE tra `lib/messages.ts` map `code` → nhãn tiếng Việt | Test assert theo **`code`** (`SMP-111`), **không** assert chuỗi tiếng Việt. **MF08-TC-INT-009** assert `message` là tiếng Anh (regex không chứa ký tự tiếng Việt có dấu). Khi Huy cấp MSG chính thức → chỉ đổi bảng ánh xạ §4, **không** viết lại test. |
| **L5** | TDS im lặng về việc client tự sinh `id` | **D5** áp cho **cả 13 module**: body có `id` → dùng làm `code`, trùng → **409 `SMP-409`**; vắng → server sinh theo `code_sequences`; **luôn** trả `id` trong 201. ⚠️ D5 nêu đích danh `SMP-${Date.now()}` là 1 trong 6 resource FE đang tự sinh id | **MF08-TC-INT-010** phủ 2 nhánh. Mock `service-caterers/samples/page.tsx:210` dùng `` `SMP-${String(Date.now()).slice(-6)}` `` rồi `router.push` ngay tới id đó → nhánh "client gửi id" là **luồng thật**, không phải giả định. |
| **L6** | TDS §10 dùng envelope `{ "error": { "code", "message" } }`; chưa chốt `details` | **D2**: `{ "error": { "code", "message", "details" } }`; `details` là **mảng, có thể rỗng, KHÔNG bao giờ `null`** | **MF08-TC-INT-009** assert đủ 3 field trên mọi mã lỗi MF08; `details` là array kể cả khi rỗng. |
| **L7** | **Mock FE gửi lên** `retentionDeadline: "72 giờ kể từ thời điểm lấy mẫu (luật tối thiểu 24h — Điều 8.2)"` — **một câu văn**, không phải mốc thời gian — và `hoursRemaining: 72` **cứng** (`service-caterers/samples/page.tsx:225-226`). `review.md` F1 = **blocker** | **BR-111 sửa 26/07/2026 = 24 giờ**; TDS ADR-M08-3: `retention_deadline` do **server** chốt = `collectedAt + 24h` (snapshot), `hoursRemaining` **tính runtime, không lưu cột**. `review.md` F1 kết luận: *"FE sai, TDS đúng"* | Server **bỏ qua** 2 field này nếu client gửi. **MF08-TC-008** assert `retentionDeadline == collectedAt + 24h` kể cả khi body chứa câu văn 72h; **MF08-TC-009** assert không có cột `hours_remaining` và giá trị đổi theo `Clock`. |
| **L8** | **Mock FE chỉ gửi** `weight: "120 g"` (chuỗi đã format); validate ≥100/≥150 **chỉ ở client** (`samples/page.tsx:170-176`). `review.md` F2 = **blocker** | TDS §9.2 + `01_MVP…` §3 (*"lượng = number + đơn vị riêng"*): request có `"amount": 120, "unit": "G"` để enforce **BR-203** ở server. `review.md` F2: *"Không có 2 field này, SMP-019 không thể enforce — gate BR-203 lách được bằng gọi API trực tiếp"* | **MF08-TC-001** gọi API **trực tiếp** (không qua FE) với `amount=90` → **422 SMP-019/MSG-019**. Response vẫn trả thêm `weight: "120 g"` cho FE hiện tại (TDS CF-M08-8). |
| **L9** | **Mock FE có** state `sessionId` (phiên kiểm thực) nhưng **không đưa vào payload** (reset ở `onSuccess`). `review.md` F3 = **blocker**: *"cổng BR-105 hiện **không có dữ liệu để chạy**"* | TDS §9.2: request **bắt buộc** `"inspectionId": "INS-2026-0706-T"`; **SMP-105** chặn nếu phiên chưa PASSED 3/3 (BR-105). Cùng lớp lỗi với MF07 CF-20 | **MF08-TC-003** assert thiếu `inspectionId` → **422**, và `inspectionId` trỏ phiên chưa PASSED → **409 SMP-105**. Không có test nào giả định "FE luôn gửi đúng". |
| **L10** | **Mock FE không gửi GPS**; ảnh bằng chứng là **checkbox boolean** `photosAttached`, không upload file; `interface FoodSample` **không có** `evidencePhotos`. `review.md` F4 = **blocker**: *"BR-107/BR-109 hiện chỉ là hộp tick"* | TDS §9.2: multipart `evidencePhotos[]` (**SMP-107**, BR-107) + `gpsLat`/`gpsLng` (**SMP-109**, BR-109); **BR-108** GPS/thiết bị/timestamp do **server** ghi. **D10**: file lưu qua `common/storage/StorageService` | **MF08-TC-005** (0 ảnh → 422 SMP-107), **MF08-TC-004** (ngoài geofence → 422 SMP-109), **MF08-TC-006** (server ghi GPS/device/timestamp, bỏ qua giá trị client gửi cho timestamp), **MF08-TC-019** (upload đi qua `StorageService`, không chạm filesystem). |
| **L11** | **Mock FE nhét nhân chứng vào chuỗi**: `disposalNote = "${note} — nhân chứng: ${witness}"` (`samples/[id]/page.tsx:81`); `FoodSample` **không có** `disposalWitness`. `review.md` F5 = **blocker**: *"seed AUD-98994 ghi 'có nhân chứng' nhưng không có chỗ lưu — dữ liệu tuân thủ pháp lý bị mất"* | TDS CF-M08-9 + §5.2: cột riêng **`disposal_witness` NOT NULL khi DISPOSED**; `disposal_note` giữ đúng nghĩa "ghi chú chất lượng" (Mẫu số 5 cột 10). CHECK `ck_smp_dispose_complete` | **MF08-TC-012** assert hủy thiếu 1 trong 4 (`disposedAt`/`disposedBy`/`disposalWitness`/`disposalNote`) → **422 SMP-030**; **MF08-TC-INT-013** assert bản xuất Mẫu số 5 ghép nhân chứng vào cột (10) mà **không** làm mất cột riêng trong DB. |
| **L12** | TDS §10 gán **MSG-023** (*"Incident report incomplete: Symptoms, date of ingestion, and dishes are required."*) cho trường hợp **"Incident ID không tồn tại/đã đóng"** — TDS tự ghi nhận sai lệch ở CF-M08-6; `review.md` F9 + `review_fix_plan.md` dòng 115 yêu cầu **MSG mới** | Traceability **§1.8 vẫn liệt kê MSG-023** cho UC-076 → giữ ánh xạ hiện tại **cho tới khi Huy cấp MSG mới**, nhưng test **không** khóa vào nội dung văn bản MSG-023 | Test assert `error.code == "SMP-023"` + HTTP 409. **Không** assert `message` của SMP-023 khớp `error_message.md` (là ngoại lệ duy nhất của **MF08-TC-INT-009**, ghi rõ trong test). Khi có MSG mới → đổi 1 dòng bảng §4. |
| **L13** | TDS §9.1 ghi *"path thật thêm tiền tố `/api/v1`"* | **D3**: chốt `baseURL = "/api/v1"` | Mọi URL trong test dùng `/api/v1/...`. |
| **L14** *(mới 03/08)* | **TDS §5.2 v1.0 tự khai `@Id private UUID id` + `created_at` + `updated_at` ở cả 4 entity, và `sample_attachments` còn thêm `uploaded_at`/`uploaded_by`.** Bảng D của `02_Module_Completion_Workflow.md` §2.1 ghi MF08 **`—`** ở cột "Cột audit tự khai" — **sai**, đếm sót đúng như đã sót ở MF07. | **D9** — `common/domain/BaseEntity` giữ **5 cột reserved** (`id`, `created_at`, `updated_at`, `created_by`, `updated_by`) qua `AuditingEntityListener` + `AuditorAware`. Khai lại ⇒ **Hibernate duplicate column, build đỏ ngay** (ca `issue_slips.created_by` của MF06 — Huy xếp D9 **đắt hơn D1** vì D1 chỉ sai hợp đồng mà vẫn chạy). DDL thì **ngược lại: vẫn phải tạo đủ 4 cột**. TDS §5.2 đã sửa (`M08-D02`). | `MF08-TC-INT-017` **đã có sẵn oracle** cho `created_by`/`updated_by` — mở rộng thêm 1 assert: `grep` toàn bộ entity MF08 **không** khớp `@Column(name = "created_at"\|"updated_at"\|"created_by"\|"updated_by")`, và `information_schema.columns` **có đủ 4 cột** trên cả 4 bảng. Bỏ assert `uploaded_at`/`uploaded_by`: DTO map từ `created_at`/`created_by` (`M08-D02`). |
| **L15** *(mới 03/08)* | **D11/D12/D13 chưa từng được nhắc** — `grep -n "D11\|D12\|D13"` trên **cả** TDS lẫn TestSpec v1.0 ra **0 kết quả**. Cả bộ 45 TC viết **trước khi D11–D13 tồn tại** (§6 của `02_Module_Completion_Workflow.md`, 02/08). Hit `oneOf` duy nhất trong file là `ZoneOffset` — **dương tính giả**, đúng như §6 cảnh báo. | **D11** *không áp dụng* cho MF08 (`M08-D13`): 3 vai trò đọc được nhận **cùng tập field**, khác biệt là **tập DÒNG** (`schoolId` từ JWT) và **quyền gọi** (§16) ⇒ **không khai `oneOf`**, nhưng **vẫn bắt buộc có TC parity** (khuôn MF06 `TC-SEC-014`). **D12** *áp dụng nặng*: 6 field dẫn xuất + 1 biến thể "cùng tên khác kiểu" (`weight`). **D13** 3/3 role có seed ACTIVE, còn 1 lỗ hổng cross-school. | **Thêm 2 TC:** `MF08-TC-SEC-009` (D11 parity — 3 vai trò, tập field giống hệt + `/v3/api-docs` khai `PagedResponseFoodSampleResponse.items` **đúng kiểu**, không `{}`, giữ tiền tố `PagedResponse` theo S7) và `MF08-TC-021` (D12 — gửi field dẫn xuất + field sai kiểu, assert **DB không đổi**). Đếm D11 toàn dự án: **3/13 → 4/13**. |
| **L16** *(mới 03/08)* | **3 fixture JWT trỏ SAI người so với seed thật.** `FX-014` ghi `U-0021 → service-caterers`, `FX-015` ghi `U-0009 → medical-staff`, `FX-016` ghi `U-0050`/`U-0033`. | `V900__test_fixtures.sql` (đọc thật): **`U-0021` = school-admin** (SCH-HN-0063, `account_scope='SCHOOL_STAFF'`) · **`U-0009` = teacher** · **`U-0050`/`U-0033` KHÔNG TỒN TẠI**. Đúng người: bếp = `U-0003`/**`U-0013`**/`U-0014` · y tế = **`U-0002`** (tên *Ngô Thị Hà* — trùng đúng tên TestSpec đang dùng!) /`U-0012` · BGH = `U-0001` ✅. **Tất cả ACTIVE**, mật khẩu chung `P@ssw0rd123` (V900 dòng 11 — **không** phải `Passw0rd123`; sai 4 lần khoá tài khoản, BR-002). | Sửa `FX-014`→`U-0013`, `FX-015`→`U-0002`, `FX-016`→`U-0001` + `U-0018` (cross-school) + `U-0004` (teacher). ⚠️ **D13 — lỗ hổng thật còn lại:** `V900` **không có tài khoản `medical-staff` nào ở trường thứ hai** ⇒ `MF08-TC-SEC-003` tạm dùng `U-0018` (school-admin SCH-HN-0051). Xin Nam thêm 1 dòng — TDS §2.5 mục 1. **Không dùng `@WithMockUser`** cho TC phụ thuộc `schoolId`: nó không mang claim đó. |
| **L17** *(mới 03/08)* | **TDS §10 ghi chú *"`error_message.md` hiện chỉ cấp cho MF08 đúng 4 mã"*** ⇒ TestSpec v1.0 §TDS-02 liệt kê Test Basis chỉ **6 MSG** và `L4` viết *"khi Huy cấp MSG chính thức → đổi bảng ánh xạ"* như việc còn treo. | **Grep `error_message.md` v1.10: 9 mã đó ĐÃ CÓ MSG chính thức từ v1.5 (29/07)** — MSG-119…MSG-127 + MSG-128, tiếng Anh, khớp **nguyên văn 100%**. Thêm 3 mã **dùng chung** MF08 đang bỏ sót: MSG-138 (`SMP-422`), MSG-139 (`SMP-409`, D5), MSG-141 (`SMP-429`). **Tổng 20 MSG, 0 mã phải xin cấp.** | `L4` **hết treo** — assert được cả `code` **lẫn** `message` nguyên văn tiếng Anh. Ngoại lệ của `L12` cũng gỡ: `SMP-023` nay assert nguyên văn **MSG-128** (*"Incident ID does not exist or the incident is already closed."*), không còn phải né nội dung MSG-023. Đúng bài học MF07: **"chưa có MSG" thường là CHƯA GREP** — nhưng grep theo **ngữ nghĩa**, và **đọc nguyên văn chuỗi**, vì MSG-020/MSG-120 ghi cứng ngữ cảnh MF08 nên module khác mượn là sai. |

> **Ghi chú cho reviewer:** 10 mâu thuẫn **CF-M08-1…10** trong TDS §2.3 **vẫn còn hiệu lực** (D1–D10 không gỡ được vì chúng là mâu thuẫn giữa UC file / traceability / BR / mock, không phải giữa các TDS). Ba cái ảnh hưởng trực tiếp tới test:
> - **CF-M08-1 (24h vs 72h):** TestSpec bám **BR-111 = 24h**. Hai luật `semantic-audit.ts` còn tên `..._72H` → xem `MF08-TC-INT-011` và **F1** (§8.1).
> - **CF-M08-2 (BR-215 Deprecated):** chỉ **Medical Staff** bàn giao mẫu → `MF08-TC-SEC-001` chặn Service Caterers. Khối `LabRequestTasks` phía bếp trong mock là quy trình cũ → **F2** (§8.1).
> - **CF-M08-3 (MSG-059 / nhật ký nhiệt độ):** BR-110 đã bỏ cảnh báo tự động và `FridgeTempLog` đã bị xóa khỏi mock → **không viết test cho MSG-059** (sẽ là AP-AI-003 nếu bịa cơ chế). Ghi vào **F3** (§8.1).

---

## 3. Test Design Specification (TDS)

### TDS-01 — Scope / Phạm vi

```
MF08 (com.sfms.mf08) — các layer được kiểm thử:
├── Domain          RetentionPolicy (24h), SampleAmountValidator (BR-203),           → unit, không deps
│                   FridgeTempRange (2–8°C), FoodSampleStatus/LabRequestStatus FSM
├── Service         FoodSampleService, SampleLabRequestService,                      → unit (Mockito)
│                   SampleRegisterExportService, RetentionDeadlineScheduler
├── Web/Controller  FoodSampleController, SampleLabRequestController,                → @WebMvcTest (mock service)
│                   ReceivingAuthorityController
├── Repository/Spec FoodSampleSpec (SEARCH_FIELDS + dateFrom/dateTo trên collected_at) → @DataJpaTest + Testcontainers
├── Integration     API → DB → event, Flyway migration thật, 8 CHECK constraint      → @SpringBootTest + Testcontainers PG16
└── Contract        /v3/api-docs (D8 — chỉ chạy khi Nam approve D8)                   → @SpringBootTest

NGOÀI phạm vi (thuộc module khác, chỉ mock qua port):
  - InspectionGatePort  / MF07  → phiên kiểm thực PASSED 3/3 (BR-105); stub trả true/false
  - IncidentStatusPort  / MF11  → sự cố còn mở + cửa sổ điều tra 24h (BR-112/149); stub
  - GeofenceQueryPort   / MF02  → tọa độ trường + bán kính (BR-109); ADR-000: dev no-op
  - StorageService      / common→ mock, assert có gọi (D10)
  - Bản in Mẫu số 4 / Mẫu số 5 (React) → FE, backend chỉ kiểm dữ liệu nguồn của 12 cột
```

### TDS-02 — Test Basis / Cơ sở Kiểm thử

| Source | Items Derived |
|--------|--------------|
| `traceability_matrix.md` **§1.8** | **5 UC** — UC-069, UC-042, UC-030, UC-076, UC-118. Nguồn chuẩn duy nhất cho cặp UC↔BR↔MSG. Không lấy UC/BR từ `Report 3.md` phụ lục. |
| `business_rules.md` §9 | **BR-105** (bắt buộc lưu mẫu sau 3 bước kiểm thực, **tất cả món**), **BR-106** (nhãn Mẫu số 4: bữa ăn · tên mẫu · thời gian lấy · người lấy), **BR-107** (bắt buộc chụp ảnh), **BR-108** (tự ghi GPS + thiết bị + timestamp), **BR-109** (xác minh vị trí bếp/kho), **BR-110** (**2–8°C**; cơ chế cảnh báo tự động **đã bỏ 11/07/2026**), **BR-111** (**24 giờ**, sửa 26/07/2026), **BR-112** (hủy sau hạn + **ngoại lệ chặn hủy** khi có sự cố), **BR-113** (**Medical Staff tự thu hồi & bàn giao**, sửa 26/07/2026), **BR-114** (liên kết thực đơn/kiểm tra/lô; `authorizedBy` readonly từ tài khoản), **BR-203** (≥100 g đặc / ≥150 ml lỏng; dụng cụ nắp kín, tiệt trùng, niêm phong riêng), **BR-212** (chỉ tiêu kiểm nghiệm + phiếu nhận của lab; hệ thống **không tự sinh** biên bản) |
| `error_message.md` | Error: **MSG-004**, **MSG-005**, **MSG-019**, **MSG-020**, **MSG-023** · Success: **MSG-044**. ⚠️ Ngoài §1.8: MSG-008/MSG-009 (upload — dùng ở `TC-SEC-005`). ❌ **Không dùng**: MSG-059 (CF-M08-3), MSG-015 (CF-M08-5) |
| `00_Common_Conventions.md` | **D1** định danh · **D2** envelope lỗi · **D3** `/api/v1` · **D4** ISO-8601 (**ghi đè `@JsonFormat` của MF08**) · **D5** client-supplied id · **D6** enum chung (`MealType`) · **D7** message tiếng Anh · **D9** BaseEntity · **D10** StorageService (**MF08 có tên trong danh sách**) · *(D8 contract test — pending)* |
| `TDS-MF08_Backend.md` §17.1 | **C1** paged shape + SEARCH_FIELDS · **C2** hạn lưu 24h + `hoursRemaining` runtime · **C3** chỉ Medical Staff bàn giao, 1 transaction, `SENT_TO_LAB` terminal · **C4** chuỗi cổng đăng ký · **C5** khóa hủy BR-112 + khóa bàn giao BR-212 · **C6** danh tính/GPS server-side, tham chiếu ID, append-only |
| `00_Team_Work_Assignment_Plan.md` §2b | Hợp đồng phân trang: 5 field, `pageCount ≥ 1`, `page` clamp, tập rỗng → **200** không 404, `search` chỉ trên `SEARCH_FIELDS`; **§2b-5**: `samples.collectedAt` là field lọc `dateFrom`/`dateTo`, kiểu `Instant` |
| `semantic-audit.ts` | **10 luật** chạm MF08: `MAU_SAI_BUA`, `HAN_LUU_KHONG_DU_72H`→24H, `GUI_LAB_TRUOC_KHI_LAY`, `HUY_MAU_TRUOC_HAN_72H`→24H, `MAU_NGOAI_KHUNG_SU_CO`, `PHIEU_TRO_MAU_KHONG_TON_TAI`, `BAN_GIAO_TRUOC_KHI_LAY_MAU`, `PHIEU_KHAC_SU_CO_VOI_MAU`, `MAU_KHONG_GAN_NGUOC_VE_PHIEU`, `PHIEU_THU_GIU_THIEU_CO_QUAN` |
| **QĐ 1246/QĐ-BYT** | Điều 1.2 (≥30 suất) · Điều 6.1/6.2 (nắp kín, tiệt trùng) · Điều 7.1 (mỗi món 1 dụng cụ, niêm phong, lấy **trước** khi ăn) · Điều 7.2 (100 g / 150 ml) · Điều 8.1 (2–8°C) · Điều 8.2 (≥24h, chặn hủy khi nghi ngộ độc) · **Mẫu số 4** (4 trường nhãn) · **Mẫu số 5** (12 cột) |
| **TT 14/2011/TT-BYT** Điều 5.2 | Bàn giao mẫu cho phòng kiểm nghiệm "ngay, trong thời gian sớm nhất" → bằng chứng là **phiếu nhận của lab**, hệ thống không tự sinh biên bản (BR-212) |

### TDS-03 — Test Conditions and Coverage Items

| Condition ID | Test Condition | Coverage Item | Test Cases |
|-------------|---------------|---------------|-----------|
| TC-COND-001 | Khối lượng mẫu ≥100 g (đặc) / ≥150 ml (lỏng); server enforce, không tin client (BR-203, **L8**) | `SampleAmountValidator` · `ck_smp_amount` | `MF08-TC-001`, `MF08-TC-INT-012` |
| TC-COND-002 | Dụng cụ nắp kín, đã tiệt trùng, niêm phong riêng từng món (BR-203, Điều 6.2/7.1) | `FoodSampleService.register()` · `ck_smp_container_state` | `MF08-TC-002` |
| TC-COND-003 | Chỉ lưu mẫu sau khi phiên kiểm thực PASSED 3/3 (BR-105, **L9**) | `InspectionGatePort` + `register()` | `MF08-TC-003`, `MF08-TC-INT-001` |
| TC-COND-004 | Vị trí lưu mẫu trong khu vực bếp/kho được chỉ định (BR-109, **L10**) | `GeofenceQueryPort` + `register()` | `MF08-TC-004` |
| TC-COND-005 | Bắt buộc ≥1 ảnh bằng chứng; file lưu qua `StorageService` (BR-107, **D10/L10**) | `register()` + `SampleAttachmentService` | `MF08-TC-005`, `MF08-TC-019`, `MF08-TC-SEC-005` |
| TC-COND-006 | GPS / thiết bị / timestamp do **server** ghi, không tin client (BR-108, C6) | `register()` | `MF08-TC-006` |
| TC-COND-007 | Nhiệt độ ngoài 2–8°C là **cảnh báo mềm** (`tempOutOfRange` + MSG-020), **không chặn**; không có job cảnh báo 30 phút (BR-110, **CF-M08-3**) | `FridgeTempRange` + `register()` | `MF08-TC-007`, `MF08-TC-INT-014` |
| TC-COND-008 | `retentionDeadline = collectedAt + 24h` do server chốt; bỏ qua giá trị client gửi (BR-111, **L7**) | `RetentionPolicy` + `register()` | `MF08-TC-008` |
| TC-COND-009 | `hoursRemaining` là **dẫn xuất** — tính runtime, không có cột (ADR-M08-3) | `FoodSampleMapper` · `information_schema` | `MF08-TC-009`, `MF08-TC-INT-012` |
| TC-COND-010 | Chặn hủy trước hạn lưu 24h (BR-111) | `FoodSampleService.dispose()` · `ck_smp_dispose_after_deadline` | `MF08-TC-010`, `MF08-TC-INT-005` |
| TC-COND-011 | Chặn hủy khi có sự cố đang mở liên kết — **ngoại lệ bắt buộc** (BR-112) | `IncidentStatusPort` + `dispose()` | `MF08-TC-011`, `MF08-TC-INT-005` |
| TC-COND-012 | Hủy phải ghi đủ thời gian / người hủy / **nhân chứng** / ghi chú (BR-112, Mẫu số 5 cột 9-10-12, **L11**) | `DisposeSampleRequest` · `ck_smp_dispose_complete` | `MF08-TC-012`, `MF08-TC-INT-013` |
| TC-COND-013 | Danh tính (`collectedBy`/`disposedBy`/`requestedBy`/`deliveredBy`) lấy từ JWT, readonly (BR-114, C6) | `register()` / `dispose()` / `handOver()` | `MF08-TC-013`, `MF08-TC-SEC-003` |
| TC-COND-014 | Bàn giao: `testCriteria` bắt buộc khi `LAB`; `authorityId` bắt buộc khi `AUTHORITY`; ≥1 `HANDOVER_RECORD` (BR-212) | `handOver()` · `ck_labreq_criteria` · `ck_labreq_authority` | `MF08-TC-014`, `MF08-TC-INT-002` |
| TC-COND-015 | Chỉ thu hồi mẫu đang `RETAINED`; `SENT_TO_LAB` là trạng thái cuối (BR-113, ADR-M08-2) | `handOver()` + FSM | `MF08-TC-015`, `MF08-TC-016`, `MF08-TC-INT-004` |
| TC-COND-016 | FSM `FoodSample`: `RETAINED` → `SENT_TO_LAB` hoặc `DISPOSED`; không transition ngược; `DISPOSED` chỉ nhận metadata AF1 | `FoodSampleStatus` transition guard | `MF08-TC-016`, `MF08-TC-INT-006` |
| TC-COND-017 | Enum serialize SCREAMING_SNAKE; `MealType` = BREAKFAST/LUNCH/SNACK, từ chối chuỗi hiển thị (**D6/L3**) | `MealType`, `SampleUnit`, `HandoverType`, `FoodSampleStatus` | `MF08-TC-017` |
| TC-COND-018 | Mọi field ngày trong JSON là ISO-8601; không còn `@JsonFormat("dd/MM/yyyy")` (**D4/L2**) | Jackson config + DTO | `MF08-TC-018`, `MF08-TC-INT-008` |
| TC-COND-019 | Upload đi qua `common/storage/StorageService`, whitelist loại + kích thước, kiểm magic bytes (**D10/L10**) | `SampleAttachmentService` | `MF08-TC-019`, `MF08-TC-SEC-005` |
| TC-COND-020 | `id` công khai = mã nghiệp vụ; `SEARCH_FIELDS` map qua `@JsonProperty` (**D1/L1**, `review.md` F7) | Toàn bộ DTO + `FoodSampleSpec` | `MF08-TC-020`, `MF08-TC-INT-007`, `MF08-TC-INT-008` |
| TC-COND-021 | Đăng ký mẫu end-to-end: 201 + MSG-044 + timer 24h + attachment + audit (UC-069) | `POST /samples` | `MF08-TC-INT-001` |
| TC-COND-022 | Bàn giao lô = **1 transaction** (phiếu + N mẫu + attachments); lỗi → rollback sạch (ADR-M08-1, `review.md` F8) | `SampleLabRequestService.handOver()` | `MF08-TC-INT-002`, `MF08-TC-INT-015` |
| TC-COND-023 | Sự cố không tồn tại/đã đóng → chặn bàn giao và chặn liên kết AF1 (BR-114, MSG-023 — **L12**) | `IncidentStatusPort` | `MF08-TC-INT-003`, `MF08-TC-INT-006` |
| TC-COND-024 | Hợp đồng phân trang §2b trên **3 resource** MF08; `dateFrom/dateTo` lọc trên `collected_at` | `PagedResponse<T>` + `FoodSampleSpec` | `MF08-TC-INT-007` |
| TC-COND-025 | Envelope lỗi đủ 3 field, `details` không null, message tiếng Anh (**D2/D7/L4/L6**) | `@RestControllerAdvice` | `MF08-TC-INT-009` |
| TC-COND-026 | Client gửi `id` (`SMP-…`) → dùng làm `code`, trùng → 409 (**D5/L5**) | `POST /samples` | `MF08-TC-INT-010` |
| TC-COND-027 | 10 luật `semantic-audit.ts` chạm MF08 không tái phát ở tầng DB/API | CHECK constraints + service | `MF08-TC-INT-011` |
| TC-COND-028 | Không tồn tại cột dẫn xuất; **có** đủ cột mà `review.md` §F đòi (`code`, `amount`/`unit`, `inspection_id`, `disposal_witness`, `temp_out_of_range`) | Flyway migration | `MF08-TC-INT-012` |
| TC-COND-029 | Xuất sổ Mẫu số 5 đúng **12 cột** Phụ lục 2 theo filter đang áp dụng (UC-118) | `SampleRegisterExportService` | `MF08-TC-INT-013` |
| TC-COND-030 | Job quét hạn lưu phát `FoodSampleRetentionExpired`; **không** job nhiệt độ (ADR-M08-6, CF-M08-3) | `RetentionDeadlineScheduler` | `MF08-TC-INT-014` |
| TC-COND-031 | Mỗi món của một phiên kiểm thực chỉ lưu **1** mẫu (Điều 7.1) | `uq_smp_inspection_dish` | `MF08-TC-INT-016` |
| TC-COND-032 | RBAC 7 role đúng §16: bếp đăng ký/hủy · y tế thu hồi/liên kết · BGH đọc/xuất (BR-007, **BR-113/CF-M08-2**) | `@PreAuthorize` | `MF08-TC-SEC-001`, `MF08-TC-SEC-002`, `MF08-TC-SEC-006` |
| TC-COND-033 | Cách ly theo trường: mọi truy vấn lọc `schoolId` từ JWT | `FoodSampleSpec` + `@PreAuthorize` | `MF08-TC-SEC-003` |
| TC-COND-034 | Bằng chứng **bất biến**: attachment và hồ sơ lưu mẫu append-only, không xóa vật lý (§4.2 TDS) | Controller + repository | `MF08-TC-SEC-004` |
| TC-COND-035 | Không rò `storage_key` / đường dẫn nội bộ ra response và log | `FoodSampleMapper` + logging | `MF08-TC-SEC-007` |
| TC-COND-036 | *(D8 — pending)* `/v3/api-docs` không lộ UUID, mọi 4xx trỏ `ErrorResponse`, enum khớp D6 | OpenAPI spec | `MF08-TC-SEC-008` |
| TC-COND-037 | `created_by`/`updated_by` do `AuditorAware` điền, service không set tay; **entity KHÔNG tự khai 5 cột reserved** nhưng **DDL có đủ 4 cột audit** (**D9**, `L14`) | `BaseEntity` + `information_schema` | `MF08-TC-INT-017` |
| TC-COND-038 *(mới 03/08)* | **D11** — 3 vai trò đọc được thấy **cùng tập field**; `PagedResponseFoodSampleResponse.items` gõ đúng kiểu trong `/v3/api-docs`; **0 `oneOf`** (`M08-D13`, `L15`) | `FoodSampleController#list` + OpenAPI spec | `MF08-TC-SEC-009` |
| TC-COND-039 *(mới 03/08)* | **D12** — field dẫn xuất và field **cùng tên khác kiểu** gửi lên bị bỏ qua, **DB không đổi**; mọi field sửa-được khớp tên+kiểu hai chiều (`M08-D14`, `L15`) | 3 cặp Request/Response của MF08 | `MF08-TC-021` |

### TDS-03b — Bản đồ phủ 5 UC (đối chiếu traceability §1.8)

> Bảng để **Huy** kiểm nhanh: mọi UC ở §1.8 đều có ít nhất một test case, và mọi BR ở cột Business Rules của UC đó đều có oracle.

| UC ID | Tên UC | BR (§1.8) | Test case phủ |
|---|---|---|---|
| **UC-069** | Register Food Sample | BR-105, 106, 107, 108, 109, 114, 203 | `TC-001` (BR-203), `TC-002` (BR-203/Điều 6-7), `TC-003` (BR-105), `TC-004` (BR-109), `TC-005` (BR-107), `TC-006` (BR-108), `TC-013` (BR-114 danh tính), `TC-020` (BR-106 nhãn Mẫu 4), `TC-INT-001` (end-to-end + MSG-044), `TC-INT-010` (D5), `TC-INT-016` (Điều 7.1) |
| **UC-042** | Manage Food Sample Retention | BR-105, 110, 111, 112, 113, 114 | `TC-007` (BR-110 + MSG-020), `TC-008` (BR-111 24h), `TC-009` (`hoursRemaining` runtime), `TC-INT-007` (danh sách/lọc), `TC-INT-014` (job hạn lưu). ⚠️ **AF1 30 phút / MSG-059 KHÔNG có test** — xem **F3** (§8.1) |
| **UC-030** | Dispose Food Sample | BR-112, 113 | `TC-010` (chặn trước hạn), `TC-011` (chặn khi có sự cố mở), `TC-012` (đủ cột 9/10/12 + nhân chứng), `TC-INT-005` (mở khóa sau khi sự cố đóng), `TC-INT-013` (sổ Mẫu số 5) |
| **UC-076** | Retrieve Food Sample Records | BR-113, 114, 212 | `TC-014` (BR-212), `TC-015` (chỉ mẫu RETAINED), `TC-016` (FSM), `TC-INT-002` (1 transaction), `TC-INT-003` (sự cố đã đóng → SMP-023), `TC-INT-004` (không bàn giao lần 2), `TC-INT-006` (**AF1** liên kết mẫu đã hủy), `TC-INT-015` (race), `TC-SEC-001` (bếp bị chặn — BR-113) |
| **UC-118** | View Food Sample Records | BR-114 | `TC-INT-007` (phân trang/lọc ngày/bữa/món/trạng thái), `TC-INT-008` (định danh D1), `TC-INT-013` (export Mẫu số 5), `TC-SEC-002`/`TC-SEC-003` (ai được xem) |

> ⚠️ **UC-042 có `BR-113` trong cột §1.8** nhưng Normal Flow của UC lại nói về nhật ký nhiệt độ — nội dung BR-113 (thu hồi mẫu) đã được phủ ở UC-076. Không nhân đôi test; ghi nhận ở **CF-M08-4** (TDS §2.3).
> ⚠️ **BR-106 không có test riêng ở tầng service** — nó là quy định *nội dung nhãn*, được phủ gián tiếp qua `TC-020` (4 trường Mẫu số 4 có mặt trong `GET /samples/{id}/label`) và bản in phía FE. Ai muốn thêm test render nhãn phải nêu BR làm oracle, nếu không rơi vào **AP-AI-001**.

### TDS-04 — Test Techniques / Kỹ thuật Kiểm thử

| Technique (ISO 29119-4) | Applied To | Rationale |
|------------------------|------------|-----------|
| **Boundary Value Analysis** | Ngưỡng BR-203 (**100 g**, **150 ml**), BR-110 (**2.0**, **8.0** °C), BR-111 (**24 giờ** — biên `deadline − 1s` / `deadline` / `deadline + 1s`) | Ba ngưỡng này là toàn bộ phần "chặn cứng" của module; BR-111 vừa đổi 72h→24h nên biên là chỗ dễ sót hằng số cũ nhất |
| **Equivalence Partitioning** | `sampleUnit` × `amount` (đặc/lỏng × dưới/đủ ngưỡng); `handoverType` (LAB / AUTHORITY); trạng thái sự cố (mở / đóng / không tồn tại) | Miền đầu vào chia lớp rõ; test 1 đại diện mỗi lớp thay vì tổ hợp đầy đủ |
| **State Transition Testing** | `FoodSampleStatus` (3 giá trị dùng + 2 legacy), `LabRequestStatus` | Có transition **không hợp lệ** phải chặn (`SENT_TO_LAB → RETAINED`, `DISPOSED → SENT_TO_LAB`); `SENT_TO_LAB` là trạng thái **cuối, không hoàn tác** (ADR-M08-2) — bấm nhầm không sửa được nên phải test cứng |
| **Decision Table Testing** | `dispose()` = f(đã hết hạn lưu?, có sự cố mở?, đủ 4 field cột 9/10/12?) | 3 điều kiện độc lập quyết định 1 kết quả; bảng quyết định gọn hơn 6 test rời |
| **Error Guessing** | Client gửi `retentionDeadline`/`hoursRemaining`/`collectedBy` giả; đổi đuôi file ảnh; gọi `POST /sample-lab-requests` bằng JWT bếp; gửi `?schoolId=` khác | Nhắm đúng lớp lỗi mà mock **không** chặn được (validate chỉ ở client — `review.md` F1/F2/F3/F4) |
| **Concurrency / Race Testing** | 2 request bàn giao cùng một mẫu song song; 2 request đăng ký cùng `(inspectionId, dishId)` | Bất biến "1 mẫu chỉ bàn giao 1 lần" và "mỗi món 1 mẫu/phiên" chỉ chắc chắn khi có `FOR UPDATE` + unique index, không phải check-then-act ở service |

### TDS-05 — Test Data Requirements

> Seed gốc: `mock-data.ts` (**10 `FOOD_SAMPLES`**, **1 `SAMPLE_LAB_REQUESTS`** = `LABREQ-2026-001`, **5 `RECEIVING_AUTHORITIES`**) → `V{n}__seed_mf08.sql`.
> ⚠️ Seed **phải quy đổi** theo TDS §11.2: `retention_deadline = collected_at + 24h` (**không** copy mốc 72h), bỏ `hoursRemaining`, tách `disposal_witness` khỏi chuỗi `disposalNote`, đổi `weight: "120 g"` → `sample_amount=120, sample_unit='G'`, và chuyển mọi mốc thời gian sang **ISO** (D4).

| Fixture ID | Type | Value / Logic | Mục đích |
|-----------|------|---------------|---------|
| `FX-001` | clock | `Clock.fixed("2026-07-07T01:00:00Z", UTC)` → **08:00 ngày 07/07/2026 giờ VN** | Ngưỡng BR-111 phải **tất định**; không dùng `Instant.now()` trực tiếp (test sẽ vỡ theo ngày chạy) |
| `FX-002` | DB seed | `SMP-20260706-01` "Cơm trắng", `LUNCH`, `collected_at=2026-07-06T03:15Z`, `retention_deadline=2026-07-07T03:15Z`, `RETAINED`, `fridge_temp=4.2` | **Còn 2h15 trong hạn lưu** tại `FX-001` → nhánh "chưa được hủy" (SMP-111) |
| `FX-003` | DB seed | `SMP-20260702-01` "Thịt kho trứng cút", `collected_at=2026-07-02T03:15Z`, `RETAINED`, **không** `incident_id` | **Đã quá hạn lưu, không vướng sự cố** → nhánh hủy hợp lệ (UC-030 happy path) |
| `FX-004` | DB seed | `SMP-20260630-01` + `SMP-20260630-02`, `RETAINED`, `incident_id=INC-2026-014` (**OPEN**), đã quá hạn lưu | Nhánh **BR-112 chặn hủy** (SMP-112) + nguồn mẫu cho bàn giao lô |
| `FX-005` | DB seed | `SMP-20260630-03` "Gà rang gừng", **`SENT_TO_LAB`**, `lab_request_id=LABREQ-2026-001`, `sent_to_lab_at=2026-06-30T08:00Z` | Nhánh **SMP-113** (không bàn giao lần hai) + FSM trạng thái cuối |
| `FX-006` | DB seed | `SMP-20260628-01`, **`DISPOSED`**, `disposed_at=2026-07-01T04:00Z`, `disposed_by="Lê Thị Mai"`, `disposal_witness="Ngô Thị Hà (Y tế)"`, `disposal_note="Mẫu đạt, không phát sinh sự cố…"` | **UC-076 AF1** (liên kết mẫu đã hủy) + kiểm cột 9/10/12 của Mẫu số 5 |
| `FX-007` | DB seed | `sample_lab_requests`: `LABREQ-2026-001`, `incident_id=INC-2026-014`, `authority_id=LAB-CDC-HN`, `handover_type=LAB`, `test_criteria="Salmonella, S.aureus, tổng vi sinh vật hiếu khí"`, `status=SENT_TO_LAB` | Phiếu bàn giao có sẵn — nguồn cho luật `MAU_KHONG_GAN_NGUOC_VE_PHIEU` |
| `FX-008` | DB seed | `receiving_authorities`: `LAB-CDC-HN` (LAB, active) · `LAB-NIFC` (LAB) · `AUTH-CA-QUAN` (**AUTHORITY**) · 1 bản ghi `is_active=false` | Hai nhánh `handoverType` + lọc `isActive` |
| `FX-009` | stub | `IncidentStatusPort`: `INC-2026-014` → **OPEN** · `INC-2026-012` → **CLOSED** · `INC-9999` → không tồn tại | BR-112 / BR-114 / MSG-023 (3 lớp tương đương) |
| `FX-010` | stub | `InspectionGatePort`: `INS-2026-0706-T` → PASSED 3/3 · `INS-2026-0706-S` → **FAILED bước 2** | Cổng BR-105 (SMP-105) |
| `FX-011` | stub | `GeofenceQueryPort`: `(21.0146, 105.7639)` → trong khuôn viên · `(21.0500, 105.9000)` → **ngoài** | BR-109 (SMP-109). ADR-000: ở dev là no-op → test dùng stub, **không** phụ thuộc cấu hình môi trường |
| `FX-012` | mock | `StorageService` (Mockito) — assert `store(key, file)` được gọi, key khớp khuôn `"samples/<code>.<ext>"` (D10) | D10/L10 |
| `FX-013` | file | `mau-com-trang.jpg` (300 KB, magic `\xFF\xD8\xFF`) · `oversize.jpg` (12 MB) · `fake.jpg` (magic `MZ`) · `bien-ban-giao-nhan.pdf` (640 KB, magic `%PDF`) | Biên MSG-008/009 + Error Guessing (`TC-SEC-005`) |
| `FX-014` | JWT | **`U-0013`** — `{ sub:"U-0013", role:"service-caterers", schoolId:"SCH-HN-0042", fullName:"Hoàng Minh Đức" }` | Actor bếp — đăng ký/hủy mẫu. ⚠️ **Sửa 03/08 (`L16`)**: v1.0 ghi `U-0021` + tên *"Lê Thị Mai"* — `V900` cho thấy **`U-0021` là school-admin** và **không có tài khoản nào tên "Lê Thị Mai"**. Bếp thật: `U-0003`/`U-0013`/`U-0014` (đều ACTIVE). |
| `FX-015` | JWT | **`U-0002`** — `{ sub:"U-0002", role:"medical-staff", schoolId:"SCH-HN-0042", fullName:"Ngô Thị Hà" }` | Actor y tế — thu hồi & bàn giao (BR-113). ⚠️ **Sửa 03/08 (`L16`)**: v1.0 ghi `U-0009` — thật ra là **teacher**. `U-0002` chính là *Ngô Thị Hà*, đúng tên v1.0 đang dùng. |
| `FX-016` | JWT | `{ sub:"U-0001", role:"school-admin", schoolId:"SCH-HN-0042" }` ✅ · **`U-0018`** `{ role:"school-admin", schoolId:"SCH-HN-0051" }` (cross-school) · **`U-0004`** `{ role:"teacher", schoolId:"SCH-HN-0042" }` | BGH (đọc/xuất) · actor **trường khác** (`TC-SEC-003`) · role không có quyền (`TC-SEC-002`). ⚠️ **Sửa 03/08 (`L16`)**: `U-0050`/`U-0033` của v1.0 **không tồn tại** trong `V900`. ⛔ **D13 — lỗ hổng còn lại:** `V900` **không seed `medical-staff` nào ở SCH-HN-0051**, nên nhánh cross-school tạm chạy bằng school-admin `U-0018`. Xin Nam thêm 1 dòng (TDS §2.5 mục 1) rồi đổi lại cho đúng vai. |
| `FX-017` *(mới 03/08)* | JWT ×3 | Ba vai trò **đọc được** `GET /samples` theo §16: `U-0001` (school-admin) · `U-0002` (medical-staff) · `U-0013` (service-caterers) — **cùng `schoolId=SCH-HN-0042`** | **D11 parity** (`MF08-TC-SEC-009`). Cùng trường để cô lập biến: nếu tập field lệch thì đó là lệch **theo vai trò**, không phải theo dữ liệu. |
| `FX-018` *(mới 03/08)* | payload bẩn | `POST /samples` kèm `hoursRemaining: 999`, `retentionDeadline: "72 giờ kể từ…"`, `weight: "120 g"`, `collectedBy: "Kẻ Giả Mạo"`, `status: "SENT_TO_LAB"`, `servings: "1850"` (chuỗi thay vì số) | **D12** (`MF08-TC-021`). Không phải payload bịa: `service-caterers/samples/page.tsx:225-226` **đang gửi thật** `retentionDeadline` câu văn + `hoursRemaining` cứng, và `weight` chuỗi (`review.md` F1/F2). |

> **Mật khẩu mọi tài khoản seed ACTIVE: `P@ssw0rd123`** (`V900` dòng 11) — **không** phải `Passw0rd123`. Sai 4 lần là khoá tài khoản (BR-002), làm hỏng cả suite.

---

## 4. Test Case Specification

> **TC ID format:** `MF08-TC-[NNN]` (unit) · `MF08-TC-SEC-[NNN]` (security) · `MF08-TC-INT-[NNN]` (integration)
> **Severity:** CRITICAL / HIGH / MEDIUM / LOW
> **Status:** 🔴 Not written / 🟡 Written-failing / 🟢 Passing

### Props Isolation Boilerplate (CASE 2.0 — BẮT BUỘC)

> ⭐ **CASE 2.0 Rule:** Mỗi test PHẢI tạo fresh instance qua factory. Không shared mutable state giữa các test case. Đây là biện pháp chống **AP-AI-002 (Green-from-Birth)**.
> Bản Java tương đương pattern `makeEntity()` của template (template viết bằng TypeScript — **KHÔNG copy nguyên cú pháp đó**, sai stack theo Team Plan §2c-2).

```java
// ═══════════════════════════════════════════════════════════════
// CASE 2.0 — Props Isolation Pattern (Java / JUnit 5)
// Đặt ở src/test/java/com/sfms/mf08/SampleTestFactory.java
// Mỗi @Test gọi factory → object mới, KHÔNG dùng field static mutable
// ═══════════════════════════════════════════════════════════════
public final class SampleTestFactory {

    /** FX-001 — đồng hồ cố định: 08:00 ngày 07/07/2026 giờ VN. Ngưỡng BR-111 phải tất định. */
    public static final Clock FIXED_CLOCK =
        Clock.fixed(Instant.parse("2026-07-07T01:00:00Z"), ZoneOffset.UTC);

    /** BR-111 (sửa 26/07/2026) — 24 giờ. KHÔNG hardcode 24 rải rác trong test. */
    public static final Duration MIN_RETENTION = Duration.ofHours(24);

    private SampleTestFactory() {}

    /** Baseline hợp lệ — đồng bộ FX-002. Mỗi lời gọi trả instance MỚI. */
    public static FoodSample aSample(Consumer<FoodSample> overrides) {
        FoodSample s = new FoodSample();
        s.setCode("SMP-20260706-01");                       // D1 — id công khai là mã nghiệp vụ
        s.setSchoolId(UUID.randomUUID());
        s.setInspectionId(UUID.randomUUID());               // BR-105 (L9)
        s.setDishId(UUID.randomUUID());
        s.setDishName("Cơm trắng");                         // Mẫu số 4 (BR-106)
        s.setMealType(MealType.LUNCH);                      // D6 — SCREAMING_SNAKE (L3)
        s.setMealShift("Bữa trưa — 10:45");                 // Mẫu số 5 cột 3 (giờ ăn)
        s.setServings(1850);                                // cột 4 — Điều 1.2
        s.setSampleAmount(new BigDecimal("120"));           // cột 5 — BR-203 (L8)
        s.setSampleUnit(SampleUnit.G);
        s.setContainer("Hộp inox có nắp kín");              // cột 6 — Điều 6.1
        s.setSanitized(true);                               // Điều 6.2
        s.setSealed(true);                                  // Điều 7.1
        s.setFridgeTemp(new BigDecimal("4.2"));             // cột 7 — BR-110
        s.setTempOutOfRange(false);
        s.setCollectedAt(Instant.parse("2026-07-06T03:15:00Z"));   // cột 8 — D4 ISO
        s.setCollectedBy("Lê Thị Mai");                     // cột 11 — từ JWT (BR-114)
        s.setStorageLocation("Tủ lưu mẫu số 1 — ngăn 1");
        s.setRetentionDeadline(s.getCollectedAt().plus(MIN_RETENTION));  // BR-111 (L7)
        s.setStatus(FoodSampleStatus.RETAINED);
        overrides.accept(s);
        return s;
    }

    /** FX-007 — phiếu bàn giao; sinh ra là đã ở SENT_TO_LAB (ADR-M08-2). */
    public static SampleLabRequest aLabRequest(Consumer<SampleLabRequest> overrides) {
        SampleLabRequest r = new SampleLabRequest();
        r.setCode("LABREQ-2026-001");
        r.setSchoolId(UUID.randomUUID());
        r.setIncidentId(UUID.randomUUID());                 // BR-114 — bắt buộc
        r.setLabDestination("CDC Hà Nội");
        r.setHandoverType(HandoverType.LAB);
        r.setTestCriteria("Salmonella, S.aureus, tổng vi sinh vật hiếu khí");  // BR-212
        r.setRequestedBy("Ngô Thị Hà (Y tế)");              // BR-113/114 — từ JWT
        r.setRequestedAt(Instant.parse("2026-06-30T07:35:00Z"));
        r.setDeliveredBy("Ngô Thị Hà (Y tế)");              // BR-113 — Medical Staff tự mang đi
        r.setSentToLabAt(Instant.parse("2026-06-30T08:00:00Z"));
        r.setStatus(LabRequestStatus.SENT_TO_LAB);
        overrides.accept(r);
        return r;
    }

    /** Payload đăng ký mẫu — mặc định HỢP LỆ; mỗi test chỉ đổi đúng field đang kiểm. */
    public static RegisterSampleRequest aRegisterRequest(Consumer<RegisterSampleRequest> overrides) {
        RegisterSampleRequest r = new RegisterSampleRequest();
        r.setInspectionId("INS-2026-0706-T");               // FX-010 — PASSED 3/3
        r.setDishId("DISH-012");
        r.setMealType("LUNCH");
        r.setMealShift("Bữa trưa — 10:45");
        r.setServings(1850);
        r.setAmount(new BigDecimal("120"));                 // L8 — number + unit tách rời
        r.setUnit("G");
        r.setContainer("Hộp inox có nắp kín");
        r.setSanitized(true);
        r.setSealed(true);
        r.setFridgeTemp(new BigDecimal("4.2"));
        r.setCollectedAt(Instant.parse("2026-07-06T03:15:00Z"));
        r.setStorageLocation("Tủ lưu mẫu số 1 — ngăn 1");
        r.setGpsLat(new BigDecimal("21.014600"));           // FX-011 — trong khuôn viên
        r.setGpsLng(new BigDecimal("105.763900"));
        overrides.accept(r);
        return r;
    }

    /** JWT actor — FX-014…FX-016. */
    public static AuthUser anActor(String role, String schoolCode, String fullName) {
        return new AuthUser("U-TEST", role, schoolCode, fullName);
    }
}
```

```java
// ❌ SAI — shared mutable state (AP-AI-002): sample bị test trước mutate sang DISPOSED
// private final FoodSample sample = SampleTestFactory.aSample(s -> {});

// ✅ ĐÚNG — fresh instance trong từng @Test
// var sample = SampleTestFactory.aSample(s -> s.setStatus(FoodSampleStatus.SENT_TO_LAB));
```

---

### UNIT TEST CASES

---

### MF08-TC-001 — Khối lượng mẫu tối thiểu theo loại món (BR-203)

**Severity:** `CRITICAL`
**Legal:** **QĐ 1246/QĐ-BYT Điều 7.2** — 100 g thức ăn đặc / 150 ml thức ăn lỏng
**Feature Under Test:** `FoodSampleService.register()` · `SampleAmountValidator`
**Test File:** `src/test/java/com/sfms/mf08/service/FoodSampleRegisterTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-001`
**Oracle Source:** **BR-203** — *"Khối lượng mẫu lưu tối thiểu: **100 gam** đối với thức ăn đặc … hoặc **150 ml** đối với thức ăn lỏng … Hệ thống **chặn** xác nhận đăng ký mẫu nếu khối lượng nhập vào thấp hơn ngưỡng"* · **MSG-019** *"Food sample retention weight too low: Must be at least 100g."* · **UC-069 AF1** · mã `SMP-019` (TDS §10)

**Preconditions:** `InspectionGatePort` stub trả PASSED (FX-010); `GeofenceQueryPort` trả `true` (FX-011); 1 ảnh bằng chứng (FX-013).

**Test Steps (Boundary Value Analysis):**
1. Arrange: `var req = SampleTestFactory.aRegisterRequest(r -> { r.setAmount(...); r.setUnit(...); });`
2. Act: `service.register(req, photos, actorCaterer)`
3. Assert theo bảng:

| `unit` | `amount` | Kỳ vọng |
|---|---|---|
| `G` | `99.99` | ❌ `ValidationException` code **`SMP-019`** |
| `G` | `100` | ✅ hợp lệ (biên dưới **được nhận**) |
| `G` | `120` | ✅ hợp lệ |
| `ML` | `149.99` | ❌ `SMP-019` |
| `ML` | `150` | ✅ hợp lệ |
| `ML` | `120` | ❌ `SMP-019` — **120 hợp lệ với G nhưng không hợp lệ với ML** |
| `G` | `0` / `-5` | ❌ `SMP-019` |

**Expected Result (PASS — hành vi đúng):**
- 4 case ❌ ném `ValidationException` với `code = "SMP-019"`, message **tiếng Anh** nguyên văn MSG-019 (D7/L4)
- `verify(repository, never()).save(any())` cho mọi case ❌
- 3 case ✅ đi tiếp tới cổng kế (BR-107)

**Expected Result (FAIL — dấu hiệu lỗi):**
- `ML/120` lọt qua → mẫu canh 120 ml **dưới ngưỡng luật**, khi cơ quan chức năng lấy đi xét nghiệm thì không đủ mẫu → hồ sơ tuân thủ vô hiệu, đúng lớp lỗi mà mock **không** chặn được vì FE chỉ validate theo `kind` do người dùng tự chọn.

**Current Status:** 🔴 Not written
**Implementation Note:** Kiểm ở service để trả đúng `SMP-019`, **bắt buộc** giữ CHECK `ck_smp_amount` ở DB (TDS §5.2) — service có thể bị bỏ qua khi gọi API trực tiếp (`review.md` F2 nêu đích danh rủi ro này).

---

### MF08-TC-002 — Dụng cụ phải tiệt trùng và niêm phong riêng từng món

**Severity:** `HIGH`
**Legal:** **QĐ 1246/QĐ-BYT Điều 6.2** (rửa sạch, tiệt trùng) + **Điều 7.1** (mỗi món 1 dụng cụ riêng, niêm phong)
**Feature Under Test:** `FoodSampleService.register()`
**Test File:** `.../FoodSampleRegisterTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-002`
**Oracle Source:** **BR-203** — *"Dụng cụ lưu mẫu phải có **nắp đậy kín**, đã được **rửa sạch và tiệt trùng** trước khi sử dụng; mỗi món ăn được lấy và lưu vào dụng cụ **riêng biệt, có niêm phong**"* · mã `SMP-203` (TDS §10)

**Test Steps:**
1. `register(req với sanitized=false)` → assert `SMP-203`
2. `register(req với sealed=false)` → assert `SMP-203`
3. `register(req với cả hai = true)` → không ném ở cổng này

**Expected Result (PASS):** 2 case đầu ném `ValidationException("SMP-203")`, không `save()`. Case 3 đi tiếp.
**Expected Result (FAIL):** mẫu không niêm phong vẫn lưu được → chuỗi giám sát đứt ngay từ đầu: không chứng minh được mẫu chưa bị thay giữa lúc lấy và lúc gửi lab (đúng rủi ro mà BR-113 bản 26/07/2026 sinh ra để chặn).

**Current Status:** 🔴 Not written
**Implementation Note:** CHECK `ck_smp_container_state` là chốt chặn cuối; đừng chỉ dựa vào Bean Validation vì 2 field là `boolean` (mặc định `false` — dễ lọt nếu dùng `@AssertTrue` sai chỗ).

---

### MF08-TC-003 — Chỉ lưu mẫu sau khi phiên kiểm thực PASSED cả 3 bước (BR-105)

**Severity:** `CRITICAL`
**Feature Under Test:** `FoodSampleService.register()` + `InspectionGatePort`
**Test File:** `.../FoodSampleRegisterTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-003`
**Oracle Source:** **BR-105** — *"Sau khi lô thức ăn **vượt qua 3 bước kiểm tra**, Service Caterers **bắt buộc** phải thực hiện lưu mẫu … cho **tất cả món ăn** của bữa ăn"* · **UC-069 Precondition** *"Meal batch has passed all three-step inspections"* · mã `SMP-105` (TDS §10) · **`review.md` F3** (blocker: *"cổng BR-105 hiện không có dữ liệu để chạy"*)

**Preconditions:** `InspectionGatePort` stub FX-010.

**Test Steps:**
1. `register(req.inspectionId = "INS-2026-0706-S")` — phiên FAILED bước 2 → assert
2. `register(req.inspectionId = null)` → assert
3. `register(req.inspectionId = "INS-2026-0706-T")` — PASSED 3/3 → đi tiếp
4. `verify(inspectionGatePort).isAllStepsPassed("INS-2026-0706-T", schoolIdFromJwt)` — **schoolId lấy từ JWT**, không từ body

**Expected Result (PASS):**
- Case 1 → `BusinessException` code **`SMP-105`**, HTTP 409
- Case 2 → `ValidationException` (field `inspectionId` bắt buộc), HTTP 422
- Case 3 → qua cổng; cổng BR-105 được gọi **trước** mọi validate khác (thứ tự C4 của TDS §17.1)

**Expected Result (FAIL):** lưu được mẫu cho bữa chưa qua kiểm thực → hồ sơ tuân thủ nói ngược nhau: sổ Mẫu số 5 có mẫu của một bữa mà sổ kiểm thực ba bước ghi FAILED.

**Current Status:** 🔴 Not written
**Implementation Note:** ⚠️ Test này sẽ FAIL 🔴 **hợp lệ** cho tới khi Minh bổ sung `inspectionId` vào payload FE (`review_fix_plan.md` dòng 185) — đây là RED thật, không phải lỗi test. Nếu MF07 chưa xong, `InspectionGatePort` chạy ở mức stub contract.

---

### MF08-TC-004 — Vị trí lưu mẫu ngoài khu vực bếp/kho bị chặn (BR-109)

**Severity:** `HIGH`
**Feature Under Test:** `FoodSampleService.register()` + `GeofenceQueryPort`
**Test File:** `.../FoodSampleRegisterTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-004`
**Oracle Source:** **BR-109** — *"Hệ thống xác minh việc lưu mẫu được thực hiện trong **khu vực bếp hoặc kho được chỉ định**"* · **UC-069 AF2** (*"GPS Location Verification Fails … System blocks registration"*) · mã **`SMP-109`** (TDS §10). ⚠️ **KHÔNG dùng MSG-015** — xem **CF-M08-5/L12**: MSG-015 thuộc BR-074/BR-079 (MF06/MF07) và **không có** trong §1.8 của UC-069

**Test Steps:**
1. `register(gps = (21.0500, 105.9000))` — ngoài geofence (FX-011) → assert `SMP-109`, HTTP 422
2. `register(gps = (21.0146, 105.7639))` — trong khuôn viên → đi tiếp
3. `register(gps = null)` → assert `SMP-109` (không có tọa độ = không xác minh được, **không** mặc định cho qua)
4. `verify(geofencePort).isInsideKitchenArea(schoolIdFromJwt, lat, lng)` — bán kính lấy từ `school_config` của MF02, không hardcode

**Expected Result (PASS):** case 1 và 3 ném `SMP-109`; case 2 qua; port được gọi đúng 1 lần với `schoolId` từ JWT.
**Expected Result (FAIL):** mẫu đăng ký từ ngoài trường vẫn được nhận → mất giá trị của toàn bộ bằng chứng GPS (BR-108), và AF2 của UC-069 không tồn tại trên thực tế.

**Current Status:** 🔴 Not written
**Implementation Note:** ADR-000 quy định GPS ở dev là **no-op**; vì vậy test **phải** dùng stub `GeofenceQueryPort` chứ không phụ thuộc cấu hình môi trường — nếu không, test sẽ xanh giả ở dev và không ai biết luật không chạy.

---

### MF08-TC-005 — Ảnh bằng chứng là bắt buộc (BR-107)

**Severity:** `HIGH`
**Feature Under Test:** `FoodSampleService.register()` + `SampleAttachmentService`
**Test File:** `.../FoodSampleRegisterTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-005`
**Oracle Source:** **BR-107** — *"Nhân viên **bắt buộc phải chụp ảnh** các mẫu thực phẩm và nhãn mẫu làm bằng chứng vận hành"* · mã `SMP-107` (TDS §10) · **`review.md` F4** (blocker: *"BR-107/BR-109 hiện chỉ là hộp tick"*)

**Test Steps:**
1. `register(req, photos = [])` → assert `SMP-107`, HTTP 422, **không** `save()`
2. `register(req, photos = [mau-com-trang.jpg])` → tạo được, và có **đúng 1** bản ghi `sample_attachments` với `kind = EVIDENCE_PHOTO`
3. Assert `verify(storageService).store(startsWith("samples/"), any())` (D10)

**Expected Result (PASS):** ảnh rỗng bị chặn; ảnh hợp lệ tạo attachment đúng `kind` và đi qua `StorageService`.
**Expected Result (FAIL):** một `boolean photosAttached = true` là đủ để qua (đúng hành vi mock hiện tại) → hồ sơ có "đã chụp ảnh" nhưng không có ảnh nào để trưng ra khi thanh tra.

**Current Status:** 🔴 Not written

---

### MF08-TC-006 — GPS, thiết bị và timestamp do server ghi, không tin client (BR-108)

**Severity:** `HIGH`
**CWE:** `CWE-807 — Reliance on Untrusted Inputs in a Security Decision`
**Feature Under Test:** `FoodSampleService.register()`
**Test File:** `.../FoodSampleRegisterTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-006`
**Oracle Source:** **BR-108** — *"Hệ thống **tự động ghi nhận** GPS, thông tin thiết bị, và timestamp trong quá trình lưu mẫu"* · TDS §17.1 **C6**

**Test Steps:**
1. Gửi body chứa `deviceInfo: "giả-mạo"`, `createdAt: "2020-01-01T00:00:00Z"`, `collectedBy: "Nguyễn Văn X"`
2. Act: `register(req, photos, actorCaterer)` với JWT `fullName = "Lê Thị Mai"` (FX-014)
3. Assert bản ghi đã lưu

**Expected Result (PASS):**
- `collectedBy == "Lê Thị Mai"` (từ JWT — BR-114), **không phải** `"Nguyễn Văn X"`
- `deviceInfo` = giá trị server đọc từ `User-Agent`, không phải chuỗi client gửi
- `createdAt` = `Instant.now(FIXED_CLOCK)`, không phải 2020
- `gpsLat/gpsLng` được **lưu** (client gửi tọa độ là hợp lệ — thiết bị mới đo được), nhưng đã **qua kiểm** `GeofenceQueryPort` ở `TC-004`

**Expected Result (FAIL):** ai gọi API trực tiếp cũng ghi được tên người khác vào cột 11 của Mẫu số 5 — chữ ký pháp lý mất giá trị.

**Current Status:** 🔴 Not written
**Implementation Note:** Phân biệt rõ: `collectedAt` (**thời điểm lấy mẫu**, do người dùng nhập — Mẫu số 5 cột 8, có thể lệch vài phút so với lúc bấm nút) ≠ `createdAt` (**thời điểm ghi nhận**, server). Đừng gộp hai field.

---

### MF08-TC-007 — Nhiệt độ ngoài 2–8°C là cảnh báo mềm, không chặn (BR-110)

**Severity:** `MEDIUM`
**Legal:** **QĐ 1246/QĐ-BYT Điều 8.1**
**Feature Under Test:** `FoodSampleService.register()` · `FridgeTempRange`
**Test File:** `src/test/java/com/sfms/mf08/domain/FridgeTempRangeTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-007`
**Oracle Source:** **BR-110** — *"Tủ lạnh lưu mẫu phải duy trì nhiệt độ trong khoảng **2°C – 8°C**"* + **sửa 11/07/2026**: *"**bỏ cơ chế cảnh báo tự động** … **giữ nguyên giới hạn 2–8°C** làm tiêu chí kiểm tra thủ công"* · **MSG-020** *"Retention refrigerator temperature is out of safe range (2-8°C)."* · **CF-M08-3**

**Test Steps (Boundary Value Analysis):**

| `fridgeTemp` | `tempOutOfRange` | Có lưu được? | Cảnh báo |
|---|---|---|---|
| `1.9` | `true` | ✅ có | MSG-020 |
| `2.0` | `false` | ✅ có | — |
| `4.2` | `false` | ✅ có | — |
| `8.0` | `false` | ✅ có | — |
| `8.1` | `true` | ✅ có | MSG-020 |

**Expected Result (PASS):**
- **Mọi** case đều lưu được (`status = RETAINED`) — BR-110 **không** phải điều kiện chặn
- 2 case ngoài dải: `tempOutOfRange = true` + phát `FoodSampleTemperatureOutOfRange` (MSG-020) đúng 1 lần
- **KHÔNG** có scheduler/job nào được đăng ký cho ngưỡng 30 phút → xem `MF08-TC-INT-014`

**Expected Result (FAIL):**
- Chặn đăng ký khi nhiệt độ lệch → mẫu **không được lưu** vào lúc đúng ra phải lưu (bếp mất bằng chứng vì tủ hơi ấm), trái BR-105 (*bắt buộc lưu mẫu*)
- Hoặc: sinh job cảnh báo 30 phút → tái lập cơ chế IoT đã bị BR-110 bỏ (**AP-AI-003**)

**Current Status:** 🔴 Not written
**Implementation Note:** Ngưỡng so sánh dùng `BigDecimal.compareTo`, **không** `double` (4.2 không biểu diễn chính xác nhị phân — biên 8.0 dễ trượt).

---

### MF08-TC-008 — Đồng hồ lưu mẫu là **24 giờ**, do server chốt (BR-111)

**Severity:** `CRITICAL`
**Legal:** **QĐ 1246/QĐ-BYT Điều 8.2** — lưu tối thiểu 24 giờ
**Feature Under Test:** `RetentionPolicy` + `FoodSampleService.register()`
**Test File:** `src/test/java/com/sfms/mf08/domain/RetentionPolicyTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-008`
**Oracle Source:** **BR-111** — *"khởi động đồng hồ đếm ngược **24 giờ** kể từ khi đăng ký lưu mẫu"* (**sửa 26/07/2026**: 72h → 24h) · TDS **ADR-M08-3** (snapshot) · **`review.md` F1** (*"FE sai, TDS đúng"*) · **L7**

**Test Steps:**
1. Arrange: body cố tình chứa dữ liệu rác của FE hiện tại:
   ```json
   { "retentionDeadline": "72 giờ kể từ thời điểm lấy mẫu (luật tối thiểu 24h — Điều 8.2)",
     "hoursRemaining": 72, "collectedAt": "2026-07-06T03:15:00Z" }
   ```
2. Act: `register(req, photos, actorCaterer)`
3. Assert bản ghi đã lưu

**Expected Result (PASS — hành vi đúng):**
- `retentionDeadline == Instant.parse("2026-07-07T03:15:00Z")` = `collectedAt + 24h` — **bỏ qua hoàn toàn** câu văn client gửi
- Response trả `retentionDeadline` dạng **ISO** (D4/L2), không phải `"07/07/2026 10:15"`
- `hoursRemaining` trong response = `2` (tại `FIXED_CLOCK` 08:00 ngày 07/07 → còn 2h15 → làm tròn xuống 2)
- Grep mã nguồn: **không** hằng số `72` nào gắn với hạn lưu

**Expected Result (FAIL — dấu hiệu lỗi):**
- Nhận `retentionDeadline` từ body → cột `retention_deadline` chứa **một câu văn**, mọi so sánh thời gian sau đó (chặn hủy BR-112, job nhắc hạn) im lặng sai
- Hoặc dùng 72h → mẫu bị giữ quá 3 lần thời hạn luật, chiếm chỗ tủ lưu và **mất giá trị xét nghiệm** (lý do BR-111 được sửa)

**Current Status:** 🔴 Not written
**Implementation Note:** Đặt hằng số **một chỗ** (`RetentionPolicy.MIN_RETENTION_HOURS = 24`). `retention_deadline` vẫn **lưu** vào DB (snapshot của luật tại thời điểm lấy mẫu — ADR-M08-3), khác với `hoursRemaining` (dẫn xuất, `TC-009`).

---

### MF08-TC-009 — `hoursRemaining` là dẫn xuất, tính runtime

**Severity:** `MEDIUM`
**Feature Under Test:** `FoodSampleMapper.toResponse()`
**Test File:** `src/test/java/com/sfms/mf08/mapper/DerivedFieldTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-009`
**Oracle Source:** **ADR-M08-3** · `01_MVP_Backend_Implementation_Plan.md` §3 (*"**Không** lưu field dẫn xuất (`hoursRemaining`, …) — tính runtime"*) · **AP-AI-009** (TDS §17.4)

**Test Steps:** cùng một entity `FX-002`, map với 3 mốc `Clock` khác nhau.

| Thời điểm | `hoursRemaining` kỳ vọng | Ghi chú |
|---|---|---|
| `2026-07-06T03:20Z` (5 phút sau khi lấy) | `23` | làm tròn xuống |
| `2026-07-07T01:00Z` (FX-001) | `2` | còn 2h15 |
| `2026-07-07T05:00Z` (quá hạn) | `0` | **không âm** |

**Expected Result (PASS):** 3 giá trị khác nhau từ **cùng một** bản ghi DB; `information_schema` không có cột `hours_remaining` (khẳng định lại ở `TC-INT-012`).
**Expected Result (FAIL):** giá trị cố định theo cột đã lưu → sổ lưu mẫu hiện "còn 24h" cho mẫu lấy từ hôm kia; đúng lỗi mà seed mock đang có (`hoursRemaining: 20` ghi cứng).

**Current Status:** 🔴 Not written

---

### MF08-TC-010 — Chặn hủy mẫu trước hạn lưu tối thiểu (BR-111)

**Severity:** `CRITICAL`
**Legal:** **QĐ 1246/QĐ-BYT Điều 8.2** — *"Mẫu **không được hủy** trước khi hết 24 giờ"*
**Feature Under Test:** `FoodSampleService.dispose()`
**Test File:** `src/test/java/com/sfms/mf08/service/FoodSampleDisposeTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-010`
**Oracle Source:** **BR-111** (*"Mẫu không được hủy trước khi hết 24 giờ này"*) · **UC-030 Precondition** (*"Sample's retention period has elapsed"*) · mã `SMP-111` (TDS §10)

**Test Steps (Boundary Value Analysis quanh `retentionDeadline` = `2026-07-07T03:15Z`):**

| `Clock` hiện tại | Kỳ vọng |
|---|---|
| `03:14:59Z` (trước 1 giây) | ❌ `SMP-111`, HTTP 409 |
| `03:15:00Z` (đúng hạn) | ✅ hủy được |
| `03:15:01Z` | ✅ hủy được |

**Expected Result (PASS):** case 1 ném `BusinessException("SMP-111")` và mẫu **vẫn** `RETAINED`; 2 case sau chuyển `DISPOSED`.
**Expected Result (FAIL):** hủy sớm được → mẫu biến mất đúng lúc sự cố chưa kịp phát hiện (triệu chứng ngộ độc thường xuất hiện sau vài giờ) — hỏng chính mục đích của việc lưu mẫu.

**Current Status:** 🔴 Not written
**Implementation Note:** So sánh `!now.isBefore(retentionDeadline)` (bao gồm mốc bằng), khớp CHECK `ck_smp_dispose_after_deadline` dùng `>=`. Nếu service dùng `isAfter` mà DB dùng `>=` thì biên đúng hạn sẽ lệch giữa 2 tầng.

---

### MF08-TC-011 — Chặn hủy khi mẫu đang gắn sự cố mở (BR-112 — ngoại lệ bắt buộc)

**Severity:** `CRITICAL`
**Legal:** **QĐ 1246/QĐ-BYT Điều 8.2** (câu cuối)
**Feature Under Test:** `FoodSampleService.dispose()` + `IncidentStatusPort`
**Test File:** `.../FoodSampleDisposeTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-011`
**Oracle Source:** **BR-112** — *"**Ngoại lệ bắt buộc**: hệ thống **chặn hủy mẫu** (dù đã hết 24 giờ) nếu có sự cố an toàn thực phẩm/nghi ngờ ngộ độc đang liên kết với lô mẫu … hoặc có yêu cầu từ cơ quan quản lý, cho đến khi sự cố được đóng"* · **UC-030 AF1** (*"Disposal Suspended by Incident"*) · mã `SMP-112`

**Test Steps (Decision Table):**

| Đã hết hạn lưu? | Sự cố liên kết | Kỳ vọng |
|---|---|---|
| ✅ | `INC-2026-014` **OPEN** (FX-009) | ❌ `SMP-112`, HTTP 409, giữ `RETAINED` |
| ✅ | `INC-2026-012` **CLOSED** | ✅ hủy được |
| ✅ | không có `incidentId` | ✅ hủy được |
| ❌ | `INC-2026-014` OPEN | ❌ `SMP-111` — **kiểm hạn lưu trước**, thông báo đúng nguyên nhân gần nhất |

**Expected Result (PASS):** 4 nhánh đúng mã lỗi; `verify(incidentPort).hasOpenIncidentLinked(...)` **không** được gọi ở nhánh 4 (đã chặn từ BR-111).
**Expected Result (FAIL):** hủy mất mẫu của ca đang điều tra → **không phục hồi được** (§12.1 TDS xếp đây là điều kiện rollback + báo DPO).

**Current Status:** 🔴 Not written

---

### MF08-TC-012 — Hủy mẫu phải ghi đủ cột 9 / 10 / 12 + nhân chứng (BR-112)

**Severity:** `HIGH`
**Legal:** **Mẫu số 5** (Phụ lục 2 QĐ 1246) — cột (9) thời gian hủy, (10) ghi chú chất lượng, (12) người hủy mẫu *(ký và ghi rõ họ tên)*
**Feature Under Test:** `DisposeSampleRequest` (Bean Validation) + `dispose()`
**Test File:** `.../FoodSampleDisposeTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-012`
**Oracle Source:** **BR-112** (*"hệ thống ghi nhận thông tin hủy mẫu, nhân viên chịu trách nhiệm, và timestamp"*) · **UC-030 Normal Flow bước 4** (*"disposal date/time, disposal method, and signs off as the person disposing along with a **witness name**"*) · mã `SMP-030` · **L11** / **`review.md` F5**

**Test Steps:** bỏ lần lượt từng field bắt buộc.

| Thiếu field | Kỳ vọng |
|---|---|
| `disposedAt` | ❌ `SMP-030`, HTTP 422 |
| `witness` | ❌ `SMP-030` — **field này FE hiện KHÔNG có (L11)** |
| `note` | ❌ `SMP-030` |
| *(không thiếu gì)* | ✅ lưu, `disposedBy` = JWT `fullName` |

**Expected Result (PASS):**
- 3 case thiếu → 422 `SMP-030`, mẫu giữ `RETAINED`
- Case đủ → `disposal_witness` lưu vào **cột riêng**, `disposal_note` **không** chứa chuỗi `"— nhân chứng:"` (khác hẳn cách mock ghép chuỗi)
- `disposedBy` lấy từ JWT, bỏ qua giá trị body (BR-114)

**Expected Result (FAIL):** nhân chứng bị nhét vào ghi chú → không truy vấn/thống kê được ai làm chứng; audit log seed `AUD-98994` ghi *"có nhân chứng"* mà hồ sơ không chứng minh được.

**Current Status:** 🔴 Not written
**Implementation Note:** ⚠️ Sẽ FAIL 🔴 hợp lệ cho tới khi Minh thêm `disposalWitness` vào FE (`review_fix_plan.md` dòng 185).

---

### MF08-TC-013 — Danh tính trong hồ sơ luôn lấy từ JWT (BR-114)

**Severity:** `CRITICAL`
**CWE:** `CWE-807`
**Feature Under Test:** `register()` · `dispose()` · `handOver()`
**Test File:** `src/test/java/com/sfms/mf08/service/ActorIdentityTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-013`
**Oracle Source:** **BR-114 (sửa 12/07/2026)** — *"field `authorizedBy` đổi từ nhập tay tự do sang **tự động = tài khoản Medical Staff đang đăng nhập, readonly** — … đảm bảo audit trail chính xác 100%, không cho phép ghi nhầm/ghi thay tên người khác"* · TDS §17.1 **C6**

**Test Steps:** với mỗi thao tác, gửi body chứa tên giả rồi assert giá trị lưu.

| Thao tác | Field | JWT (FX-014/FX-015) | Body gửi lên | Kỳ vọng lưu |
|---|---|---|---|---|
| `register()` | `collectedBy` | `"Lê Thị Mai"` | `"Nguyễn Văn X"` | `"Lê Thị Mai"` |
| `dispose()` | `disposedBy` | `"Lê Thị Mai"` | `"Trần Văn Hùng"` | `"Lê Thị Mai"` |
| `handOver()` | `requestedBy` | `"Ngô Thị Hà (Y tế)"` | `"ai đó"` | `"Ngô Thị Hà (Y tế)"` |
| `handOver()` | `deliveredBy` | `"Ngô Thị Hà (Y tế)"` | `"Trần Văn Hùng (bếp)"` | `"Ngô Thị Hà (Y tế)"` — **BR-113: y tế tự mang đi** |

**Expected Result (PASS):** 4/4 field khớp JWT; DTO request **không có** setter cho các field này (hoặc có `@JsonIgnore`).
**Expected Result (FAIL):** ghi được tên người khác vào chuỗi giám sát → đúng rủi ro **tráo mẫu** mà BR-113 bản 26/07/2026 sinh ra để chặn; và cột 11/12 của Mẫu số 5 mất giá trị pháp lý.

**Current Status:** 🔴 Not written
**Implementation Note:** `deliveredBy` là chỗ dễ sai nhất: mock cũ cho bếp nhập tay (`deliveredBy = "Trần Văn Hùng (bếp)"`). Sau BR-215 Deprecated, field này **bằng chính** actor y tế — không có đường nhập tay nào.

---

### MF08-TC-014 — Bằng chứng bàn giao là điều kiện chặn cứng (BR-212)

**Severity:** `CRITICAL`
**Legal:** **TT 14/2011/TT-BYT Điều 5 khoản 2**
**Feature Under Test:** `SampleLabRequestService.handOver()`
**Test File:** `src/test/java/com/sfms/mf08/service/SampleHandOverTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-014`
**Oracle Source:** **BR-212** — *"hệ thống yêu cầu: **Chỉ tiêu yêu cầu kiểm nghiệm** (nhập khi chỉ định — **bắt buộc**) và **Ảnh/scan phiếu nhận mẫu do phòng kiểm nghiệm cấp** (tải lên khi xác nhận đã gửi — **bắt buộc**), có thể kèm **Đại diện phòng lab** (**tùy chọn**)"* + *"hệ thống **không tự sinh** một 'Biên bản bàn giao mẫu' nào khác"* · mã `SMP-212`

**Test Steps (Equivalence Partitioning theo `handoverType`):**

| `handoverType` | `testCriteria` | `authorityId` | `HANDOVER_RECORD` | Kỳ vọng |
|---|---|---|---|---|
| `LAB` | có | có | 1 file | ✅ tạo phiếu |
| `LAB` | **rỗng** | có | 1 file | ❌ `SMP-212`, 422 |
| `LAB` | có | có | **0 file** | ❌ `SMP-212`, 422 |
| `AUTHORITY` | **rỗng** | có | 1 file | ✅ — cơ quan thu giữ **không** đặt chỉ tiêu (TDS §5.2 `ck_labreq_criteria`) |
| `AUTHORITY` | rỗng | **null** | 1 file | ❌ 422 (`ck_labreq_authority`) |
| `LAB` | có | có | 1 file, **`labRepresentative` = null** | ✅ — trường **tùy chọn** |

**Expected Result (PASS):** 3 case ✅ tạo phiếu `status = SENT_TO_LAB`; 3 case ❌ **không** tạo phiếu và **không** đổi trạng thái mẫu nào. Hệ thống **không** sinh file biên bản nào (assert `storageService` chỉ được gọi cho file người dùng tải lên).
**Expected Result (FAIL):** mẫu chuyển `SENT_TO_LAB` (trạng thái **cuối, không hoàn tác**) mà không có bằng chứng bàn giao → không chứng minh được mẫu tới nơi nhận; TDS §4.2 xếp đây là bất biến 100%.

**Current Status:** 🔴 Not written

---

### MF08-TC-015 — Chỉ thu hồi được mẫu đang `RETAINED` (BR-113)

**Severity:** `HIGH`
**Feature Under Test:** `SampleLabRequestService.handOver()`
**Test File:** `.../SampleHandOverTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-015`
**Oracle Source:** **BR-113** (*"Medical Staff chỉ định mẫu thực phẩm **đang lưu** cần thu hồi"*) · **UC-076 Precondition** (*"The selected food sample is **currently retained** (not yet disposed of)"*) · mã `SMP-113`

**Test Steps:**
1. Lô gồm `FX-004` (RETAINED) + `FX-005` (**SENT_TO_LAB**) → assert `SMP-113`, HTTP 409, nêu đích danh mã mẫu vi phạm trong `details`
2. Lô gồm `FX-004` + `FX-006` (**DISPOSED**) → assert `SMP-113`
3. Lô toàn `RETAINED` → ✅
4. Lô rỗng `sampleIds = []` → ❌ 422

**Expected Result (PASS):** 3 nhánh ❌ đúng mã; **không** phiếu nào được tạo (kiểm ở `TC-INT-002` mức transaction). `details` chứa mã mẫu vi phạm để FE chỉ đúng dòng.
**Expected Result (FAIL):** bàn giao lần hai cùng một mẫu → hai phiếu cùng trỏ một hộp mẫu vật lý; đúng lớp lỗi `MAU_KHONG_GAN_NGUOC_VE_PHIEU` của `semantic-audit.ts`.

**Current Status:** 🔴 Not written

---

### MF08-TC-016 — Máy trạng thái mẫu lưu (ADR-M08-2)

**Severity:** `HIGH`
**Feature Under Test:** `FoodSampleStatus` transition guard
**Test File:** `src/test/java/com/sfms/mf08/domain/FoodSampleStatusTransitionTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-015`, `TC-COND-016`
**Oracle Source:** TDS **§6.4 State Machine** + **ADR-M08-2** (*"`SENT_TO_LAB` là trạng thái **cuối**, không hoàn tác"*) · **BR-112** (`DISPOSED` là điểm cuối của nhánh hủy) · **UC-076 AF1** (mẫu `DISPOSED` chỉ **liên kết metadata**, không đổi trạng thái)

**Test Steps (State Transition Testing — bảng đầy đủ):**

| Từ | Tới | Hợp lệ? | Ghi chú |
|---|---|---|---|
| `RETAINED` | `SENT_TO_LAB` | ✅ | UC-076, có bằng chứng BR-212 |
| `RETAINED` | `DISPOSED` | ✅ | UC-030, qua 2 cổng BR-111 + BR-112 |
| `RETAINED` | `RETAINED` | ✅ | cập nhật vị trí/nhiệt độ (UC-042 AF2) |
| `SENT_TO_LAB` | `RETAINED` | ❌ | trạng thái cuối — không hoàn tác |
| `SENT_TO_LAB` | `DISPOSED` | ❌ | mẫu không còn ở trường |
| `DISPOSED` | `SENT_TO_LAB` | ❌ | không thu hồi vật lý được (UC-076 AF1) |
| `DISPOSED` | `RETAINED` | ❌ | không "hồi sinh" bản ghi tuân thủ |
| `DISPOSED` | `DISPOSED` + `incidentId` | ✅ | **AF1 — chỉ ghi metadata**, `status` không đổi |

**Expected Result (PASS):** 4 transition ❌ ném `IllegalStateException`/`ConflictException`; 2 giá trị legacy (`RETRIEVAL_REQUESTED`, `PENDING_LAB_DELIVERY`) **không** có transition nào **đi tới** — chỉ đọc được từ dữ liệu cũ (CF-M08-2).
**Expected Result (FAIL):** cho phép quay ngược `SENT_TO_LAB → RETAINED` → hồ sơ nói mẫu vẫn ở tủ trong khi nó đang ở CDC; hoặc sinh mới trạng thái legacy → tái lập quy trình BR-215 đã bị bãi bỏ (**AP-AI-012** — TDS §17.4).

**Current Status:** 🔴 Not written

---

### MF08-TC-017 — Enum serialize SCREAMING_SNAKE, từ chối chuỗi hiển thị (D6/L3)

**Severity:** `HIGH`
**Feature Under Test:** `MealType`, `SampleUnit`, `HandoverType`, `FoodSampleStatus`
**Test File:** `src/test/java/com/sfms/mf08/dto/EnumContractTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-017`
**Oracle Source:** **D6** — *"tất cả **SCREAMING_SNAKE** trong JSON và DB. FE giữ bảng nhãn tiếng Việt ở `lib/labels.ts` — nhãn hiển thị **không bao giờ** là giá trị lưu trữ"*; bảng D6 chốt `MealType = BREAKFAST | LUNCH | SNACK`, nhãn **"Bữa phụ"** cho `SNACK` (⚠️ *"Bữa phụ" ≠ "Bữa chiều"*)

**Test Steps:**

| Input JSON | Kỳ vọng |
|---|---|
| `"mealType": "LUNCH"` | ✅ deserialize `MealType.LUNCH` |
| `"mealType": "Bữa trưa"` | ❌ 422 — chuỗi hiển thị không phải giá trị lưu trữ |
| `"mealType": "Trưa"` | ❌ 422 — tiền tố của mock (`MEAL_PREFIX`) |
| `"mealType": "lunch"` | ❌ 422 — sai case |
| `"mealType": "DINNER"` | ❌ 422 — **không** thuộc D6 (mock có "Bữa chiều", D6 chốt `SNACK`) |
| `"unit": "G"` / `"ML"` | ✅ |
| `"unit": "g"` / `"gam"` | ❌ 422 |
| serialize `FoodSampleStatus.SENT_TO_LAB` | `"SENT_TO_LAB"`, **không** `"Đã gửi phòng kiểm nghiệm"` |

**Expected Result (PASS):** 5 case ❌ trả 422 với envelope D2; case ✅ round-trip đúng.
**Expected Result (FAIL):** nhận `"Bữa trưa"` → dữ liệu lưu là nhãn hiển thị; đổi nhãn FE là vỡ dữ liệu cũ, và lọc `?mealType=` im lặng trả rỗng (đúng lớp lỗi D6 sinh ra để chặn).

**Current Status:** 🔴 Not written
**Implementation Note:** `MealType` thuộc **`common/enums/`** do **Nam sở hữu** (D6) — MF08 **không** tự khai lại. Nếu `common/enums` chưa có, test này FAIL 🔴 hợp lệ (Suspension Criteria §6).

---

### MF08-TC-018 — Mọi field ngày trong JSON là ISO-8601 (D4/L2)

**Severity:** `HIGH`
**Feature Under Test:** Jackson config + toàn bộ DTO của MF08
**Test File:** `src/test/java/com/sfms/mf08/dto/DateFormatContractTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-018`
**Oracle Source:** **D4** — *"JSON luôn ISO-8601 … **D4 ghi đè ADR-M11-5**. **MF08**, MF09, MF11 phải **bỏ `@JsonFormat`** khỏi các field ngày"* · `01_MVP_Backend_Implementation_Plan.md` §3 (*"ngày = ISO/Instant"*) · **`review.md` A4**

**Test Steps:**
1. Serialize `FoodSampleResponse`, `FoodSampleDetailResponse`, `SampleLabRequestResponse`, `UploadedFileResponse`
2. Assert regex `\d{2}/\d{2}/\d{4}` **không khớp** ở bất kỳ đâu trong JSON
3. Assert từng field: `collectedAt`, `retentionDeadline`, `disposedAt`, `retrievedAt`, `sentToLabAt`, `requestedAt`, `uploadedAt` khớp `^\d{4}-\d{2}-\d{2}T.*Z$`
4. Grep mã nguồn: `@JsonFormat` **không** xuất hiện trong package `com.sfms.mf08`

**Expected Result (PASS):** 0 kết quả cho cả 2 phép grep/regex; 7 field đều ISO.
**Expected Result (FAIL):** giữ `dd/MM/yyyy` → FE dùng `new Date(...)` parse ra `Invalid Date`, và `paginate()` lọc ngày trả rỗng **im lặng** (D4 cảnh báo đúng bẫy này).

**Current Status:** 🔴 Not written
**Implementation Note:** Field `mealShift` (`"Bữa trưa — 10:45"`) là **text hiển thị của Mẫu số 5 cột 3**, không phải field ngày — regex phải loại trừ nó, nhưng nó **không** chứa `dd/MM/yyyy` nên không đụng bước 2.

---

### MF08-TC-019 — Upload đi qua `StorageService`, whitelist loại và kích thước (D10)

**Severity:** `HIGH`
**Feature Under Test:** `SampleAttachmentService.store()`
**Test File:** `src/test/java/com/sfms/mf08/service/SampleAttachmentServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-005`, `TC-COND-019`
**Oracle Source:** **D10** — *"bất kỳ module nào cần lưu file/ảnh (… **MF08 ảnh mẫu lưu** …) **phải** inject `StorageService` có sẵn ở `common/storage`, **không** tự gọi `MultipartFile.transferTo()`/AWS SDK riêng"*; khuôn key `"<module>/<mã-nghiệp-vụ>.<ext>"` · **MSG-008** (>10MB) · **MSG-009** (sai định dạng) · ADR-000 (dev: `./data/uploads`)

**Test Steps:**

| File (FX-013) | Kỳ vọng |
|---|---|
| `mau-com-trang.jpg` 300 KB | ✅ `storageService.store("samples/SMP-…-01.jpg", file)` được gọi |
| `oversize.jpg` 12 MB | ❌ **MSG-008**, không gọi `store()` |
| `note.txt` | ❌ **MSG-009** |
| `bien-ban-giao-nhan.pdf` | ✅ hợp lệ cho `kind = HANDOVER_RECORD` (BR-212 cho phép ảnh **hoặc** PDF) |

**Expected Result (PASS):** `verify(storageService).store(...)` đúng khuôn key; `checksumSha256` được tính và lưu; **không** lời gọi `transferTo()` nào trong package.
**Expected Result (FAIL):** service tự ghi đĩa → vỡ khi bật `sfms.storage.provider=r2` ở production, và ảnh bằng chứng nằm ngoài vòng đời backup.

**Current Status:** 🔴 Not written
**Implementation Note:** Thêm ArchUnit rule cấm `MultipartFile.transferTo` và `software.amazon.awssdk` trong `com.sfms.mf08` — rẻ hơn nhiều so với phát hiện lúc deploy.

---

### MF08-TC-020 — `id` công khai là mã nghiệp vụ; `SEARCH_FIELDS` map qua `@JsonProperty` (D1/L1)

**Severity:** `HIGH`
**Feature Under Test:** `FoodSampleResponse` · `FoodSampleSpec`
**Test File:** `src/test/java/com/sfms/mf08/dto/PublicIdContractTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-020`
**Oracle Source:** **D1** (*"`id` công khai **= mã nghiệp vụ**; path param dùng `code`; UUID chỉ là PK nội bộ, **không lộ ra API**"*) · **`review.md` F7** (*"`SEARCH_FIELDS` MF08 dùng `code`/`dish_name`/`collected_by`/`meal_label`, FE dùng `dish`/`meal`/`collectedBy` → cần map rõ trong `@JsonProperty`"*) · `review_fix_plan.md` dòng 143

**Test Steps:**
1. Serialize `FoodSampleResponse` của FX-002 → assert JSON có `"id": "SMP-20260706-01"`, **không** có field `"code"` riêng, **không** UUID
2. Assert ánh xạ tên: `dish_name` → `"dish"`, `collected_by` → `"collectedBy"` (khớp `interface FoodSample` của FE); `"meal"` là **nhãn dẫn xuất** từ `mealType` + `collectedAt` (`M08-D05`), không map thẳng từ cột
3. Assert 4 trường **Mẫu số 4** (BR-106) có mặt ở `GET /samples/{id}/label`: bữa ăn · tên mẫu · thời gian lấy · người lấy mẫu
4. Assert `FoodSampleSpec.SEARCH_FIELDS` đúng **3 cột** `{code, dish_name, collected_by}` — **không** OR toàn bộ cột String. ⚠️ **Sửa 03/08 (`M08-D12`)**: v1.0 ghi 4 cột kể cả `meal_label`; `M08-D05` đã biến nó thành nhãn dẫn xuất ⇒ không còn cột để `ILIKE` (cùng lớp với MF07 phải rút `SEARCH_FIELDS` còn `staffNotes`).

**Expected Result (PASS):** JSON khớp hợp đồng FE; `search` chạy đúng cột người dùng nhìn thấy.
**Expected Result (FAIL):** `search=Đức` không ra mẫu nào vì backend tìm trên `collected_by` còn FE gửi theo `collectedBy` — lỗi **im lặng**, không exception nào. Hoặc `search` sinh SQL trên `meal_label` — **cột không tồn tại** ⇒ lỗi runtime.

**Current Status:** 🔴 Not written

---

### MF08-TC-021 — **D12:** field dẫn xuất & field sai kiểu bị BỎ QUA, DB không đổi *(mới 2026-08-03 — bắt buộc theo §7 Bước 1 việc 3)*

**Severity:** `CRITICAL`
**Feature Under Test:** `RegisterSampleRequest` · `DisposeSampleRequest` · `HandOverSamplesRequest` (mọi cặp Request/Response của MF08)
**Test File:** `src/test/java/com/sfms/mf08/web/DerivedFieldRejectionIT.java`
**TDD Phase:** 🔴 Not written
**Condition Ref:** `TC-COND-039` *(mới)*
**Oracle Source:** **D12** (`02_Module_Completion_Workflow.md` §6) + **`M08-D14`** (TDS §2.4) + **BR-108/BR-111/BR-114** (danh tính, GPS, hạn lưu do **server** chốt) · Mẫu: `TestSpec-MF05` **TC-027**
**Fixtures:** `FX-018` (payload bẩn), `FX-014` (JWT bếp `U-0013`), `FX-001` (clock cố định)

> **Vì sao MF08 là ca NẶNG của D12, không phải ca hình thức.** D12 nói về cái bẫy *"200 mà không lưu gì"*: Jackson gặp field không khớp thì **bỏ qua im lặng** — không lỗi, không cảnh báo, form báo thành công. MF08 có **hai** biến thể cùng lúc:
> - **(a) 6 field DẪN XUẤT** mà response trả ra nhưng request **không** được nhận: `hoursRemaining`, `weight`, `meal`, `dish`, `retentionDeadline`, `sampleIds`. Mock FE **đang gửi thật** hai trong số đó (`service-caterers/samples/page.tsx:225-226` gửi `retentionDeadline` là **một câu văn** *"72 giờ kể từ thời điểm lấy mẫu…"* và `hoursRemaining: 72` cứng — `review.md` **F1 = blocker**).
> - **(b) CÙNG TÊN, KHÁC KIỂU** — biến thể nguy hiểm hơn vì **review bằng mắt thấy hai bên trông như khớp**: response trả `weight: "120 g"` (**chuỗi**), request nhận `amount: 120` (**số**) + `unit: "G"`. Nếu ai đó "tiện tay" thêm `weight` vào request DTO, cổng **BR-203** (`SMP-019`) lách được bằng cách gọi API trực tiếp — đúng thứ `review.md` **F2** đã cảnh báo. Cùng khuôn lỗi MF05 mắc với `dishes` (số đếm ở response vs mảng ở payload).

**Test Steps:**
1. `POST /api/v1/samples` bằng `FX-014`, payload = `FX-018` — **hợp lệ về mọi mặt khác** (đủ ảnh, đúng geofence, `amount=120 unit=G`, `inspectionId` PASSED 3/3) nhưng **kèm 6 field bẩn**.
2. Assert **201** (không phải 422 — field thừa bị bỏ qua, không phải lỗi validate; đây chính là điều làm bẫy nguy hiểm).
3. Đọc **thẳng DB** (không qua response) bản ghi vừa tạo, assert **từng cột**:
   - `retention_deadline` == `collected_at + 24h` tính bằng `FX-001` — **không** phải bất cứ thứ gì suy từ chuỗi *"72 giờ…"* client gửi (BR-111, ADR-M08-3)
   - `collected_by` == `"Hoàng Minh Đức"` **từ JWT**, **không** phải `"Kẻ Giả Mạo"` trong body (BR-114)
   - `status` == `'RETAINED'`, **không** phải `'SENT_TO_LAB'` client gửi (FSM chỉ đổi qua endpoint nghiệp vụ)
   - `servings` == `1850` (**số**) — client gửi chuỗi `"1850"`; assert cột `INT` nhận đúng, hoặc **422 `SMP-422`** nếu DTO khai `int` chặt (cả hai đều đúng, **nhưng phải chọn một và test khoá lại**)
   - `information_schema.columns` **không có** cột `hours_remaining` và **không có** cột nào tên `weight` (ADR-M08-3, AP-AI-009)
4. Lặp bước 1–3 cho `POST /samples/{code}/dispose` với `disposedBy` giả trong body ⇒ DB phải ghi actor **từ JWT**; và cho `POST /sample-lab-requests` với `sampleIds` **khác** danh sách thật ⇒ chỉ mẫu trong `sampleIds` **của request** đổi trạng thái, `requestedBy`/`deliveredBy` lấy từ JWT.
5. **Chiều ngược lại (rà cặp Request/Response):** với mỗi cặp DTO của MF08, assert mọi field **SỬA ĐƯỢC** có **cùng tên VÀ cùng kiểu** ở cả hai chiều — `amount`/`unit`/`container`/`sanitized`/`sealed`/`fridgeTemp`/`storageLocation`/`mealShift`/`mealType`. Field nào chỉ có ở response thì phải nằm trong danh sách dẫn xuất đã khai ở TDS §9.2.

**Expected Result (PASS):** 201, và **mọi cột DB đúng giá trị server chốt**; 0 cột dẫn xuất tồn tại; danh sách field sửa-được khớp hai chiều.
**Expected Result (FAIL):** bất kỳ giá trị nào từ body lọt vào DB ⇒ BR-108/111/114 chỉ là **trang trí**. Trường hợp tệ nhất là im lặng: `PATCH`/`POST` trả **200 mà không lưu gì** (ca `PATCH /students/{code}` với `{"class":"4A"}` của MF03 — mất **hai phiên làm việc** mới tìm ra).

**Current Status:** 🔴 Not written

---

### SECURITY TEST CASES

---

### MF08-TC-SEC-001 — Service Caterers KHÔNG được bàn giao mẫu đi xét nghiệm (BR-113)

**Severity:** `CRITICAL`
**OWASP:** `A01:2021 — Broken Access Control`
**CWE:** `CWE-863 — Incorrect Authorization`
**Legal:** Chuỗi giám sát mẫu (chain of custody) — **TT 14/2011/TT-BYT Điều 5.2**
**Feature Under Test:** `POST /api/v1/sample-lab-requests` + `@PreAuthorize`
**Test File:** `src/test/java/com/sfms/mf08/security/SampleRbacIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-032`
**Oracle Source:** **BR-113 (sửa 26/07/2026)** — *"**Medical Staff** chỉ định … **và tự mình thực hiện** việc lấy mẫu vật lý tại tủ lưu, vận chuyển đến phòng kiểm nghiệm"*; lý do ghi trong BR: *"Nhà thầu là bên có thể bị quy trách nhiệm qua kết quả xét nghiệm, nếu để chính họ tự tay mang mẫu đi gửi lab … thì hoàn toàn có thể **tráo mẫu** trước khi nộp"* · **BR-215 Deprecated 26/07/2026** · TDS §16 · **CF-M08-2**

**Preconditions:** JWT `role = service-caterers` (FX-014); tồn tại `INC-2026-014` OPEN và 2 mẫu `RETAINED` của ca đó.

**Test Steps (Attack Simulation):**
1. Đăng nhập bằng tài khoản bếp (đúng trường, đúng mẫu — mọi thứ hợp lệ **trừ vai trò**)
2. `POST /api/v1/sample-lab-requests` với payload hợp lệ hoàn toàn
3. Kiểm response **và** trạng thái DB

**Expected Result (PASS = hệ thống an toàn):**
- **HTTP 403** với `error.code = "SMP-005"` (MSG-005) — **không phải 404, không phải 422**
- **Không** phiếu nào được tạo; **không** mẫu nào đổi trạng thái
- Tương tự với `PUT /api/v1/samples/{code}` mang `status=SENT_TO_LAB` (đường vòng qua endpoint cũ của mock) → **403** hoặc **405** (endpoint generic không tồn tại ở backend thật)

**Expected Result (FAIL = lỗ hổng tồn tại):** bếp tự mang mẫu đi gửi lab → đúng kịch bản tráo mẫu mà phỏng vấn cán bộ y tế nêu ra; kết quả xét nghiệm mất giá trị đối chứng, và việc đảo ngược BR-215 trở nên vô nghĩa.

**Current Status:** 🔴 Not written
**Implementation Note:** ⚠️ Mock FE **vẫn còn** khối `LabRequestTasks` cho bếp (`service-caterers/samples/page.tsx:424-555`) — test này cố tình **ngược** với mock. Xem **F2** (§8.1): Minh cần gỡ khối đó.

---

### MF08-TC-SEC-002 — RBAC 7 role trên toàn bộ endpoint MF08

**Severity:** `CRITICAL`
**OWASP:** `A01:2021`
**Feature Under Test:** `@PreAuthorize` trên 3 controller
**Test File:** `.../SampleRbacIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-032`
**Oracle Source:** **TDS §16 Authorization Matrix** · **BR-007** (RBAC 7 role) · **UC-069/030** Primary Actor = Service Caterers · **UC-076** Primary Actor = Medical Staff · **UC-118** = Medical Staff / School Admin / Service Caterers

**Test Steps:** ma trận `endpoint × role` (parameterized test, 7 role × 9 endpoint).

| Endpoint | SYS ADMIN | SCHOOL ADMIN | MEDICAL | CATERER | TEACHER / PARENT / SUPPLIER |
|---|---|---|---|---|---|
| `GET /samples`, `GET /samples/{code}` | 403 | 200 | 200 | 200 | 403 |
| `POST /samples` | 403 | 403 | **403** | **201** | 403 |
| `GET /samples/{code}/label` | 403 | 403 | 403 | 200 | 403 |
| `PATCH /samples/{code}/storage` | 403 | 403 | 403 | 200 | 403 |
| `POST /samples/{code}/dispose` | 403 | 403 | **403** | **200** | 403 |
| `POST /samples/{code}/incident-link` | 403 | 403 | **200** | **403** | 403 |
| `GET /samples/export` | 403 | 200 | 200 | 403 | 403 |
| `POST /sample-lab-requests` | 403 | 403 | **201** | **403** (SEC-001) | 403 |
| `GET /receiving-authorities` | 403 | 200 | 200 | 403 | 403 |

**Expected Result (PASS):** 63 tổ hợp đúng mã; mọi 403 đều mang `error.code = "SMP-005"`.
**Expected Result (FAIL):** System Admin đọc được sổ lưu mẫu → vi phạm least privilege của BR-007 (System Admin giám sát qua `audit-logs` của MF01, không đọc hồ sơ nghiệp vụ ATTP — TDS §16 ghi rõ).

**Current Status:** 🔴 Not written
**Implementation Note:** ⚠️ **Bẫy Red Gate:** khi endpoint chưa tồn tại, request trả **404** chứ không phải 403 và test có thể "xanh giả" nếu chỉ assert `isNot2xx()`. Phải assert **đúng mã 403 + `error.code`** (xem §5.1).

---

### MF08-TC-SEC-003 — Cách ly theo trường: `schoolId` chỉ lấy từ JWT

**Severity:** `CRITICAL`
**OWASP:** `A01:2021` · **CWE:** `CWE-639 — Authorization Bypass Through User-Controlled Key`
**Feature Under Test:** `FoodSampleSpec` + `findByCodeAndSchoolId()`
**Test File:** `src/test/java/com/sfms/mf08/security/SampleTenantIsolationIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-033`
**Oracle Source:** **BR-007** · **BR-022** (nguyên tắc chỉ xem dữ liệu trường mình) · TDS §4.3 (*"Mọi truy vấn mẫu lọc theo `schoolId` từ JWT"*) · §17.1 **C6**

**Test Steps (Attack Simulation):**
1. JWT y tế trường **B** (`SCH-HN-0051` — FX-016), mẫu thuộc trường **A** (`SCH-HN-0042`)
2. `GET /api/v1/samples/SMP-20260706-01` → kiểm
3. `GET /api/v1/samples?schoolId=SCH-HN-0042` — **tham số schoolId trong query** → kiểm
4. `POST /api/v1/samples` với body chứa `"schoolId": "SCH-HN-0042"` (JWT là bếp trường B) → kiểm
5. `POST /api/v1/sample-lab-requests` gom mẫu của **cả 2 trường** → kiểm

**Expected Result (PASS = an toàn):**
- Bước 2: **404** (ẩn tồn tại) hoặc **403 `SMP-005`** — chọn một và **nhất quán** toàn module
- Bước 3: 200 nhưng `items` **rỗng** — tham số `schoolId` bị **bỏ qua**, không dùng làm bộ lọc (`schoolId` không nằm trong danh sách field lọc hợp lệ)
- Bước 4: bản ghi lưu với `school_id` của **trường B** (từ JWT), không phải giá trị body
- Bước 5: **409/422** — một phiếu bàn giao không được gom mẫu của 2 trường

**Expected Result (FAIL):** y tế trường B đọc/gửi được mẫu trường A → rò dữ liệu tuân thủ giữa các trường; và một phiếu bàn giao "hợp lệ" trỏ tới hộp mẫu ở trường khác.

**Current Status:** 🔴 Not written

---

### MF08-TC-SEC-004 — Hồ sơ lưu mẫu và bằng chứng là **append-only**

**Severity:** `HIGH`
**OWASP:** `A08:2021 — Software and Data Integrity Failures` · **CWE:** `CWE-284`
**Legal:** **Mẫu số 5** là sổ theo dõi phải xuất trình khi thanh tra — xóa dòng = mất hồ sơ tuân thủ
**Feature Under Test:** `FoodSampleController`, `SampleAttachmentController`
**Test File:** `.../SampleRbacIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-034`
**Oracle Source:** TDS **§4.2** (*"Hồ sơ lưu mẫu **append-only** — không xóa vật lý kể cả sau khi hủy mẫu"*) + **§4.3** (*"File bằng chứng **immutable**: chỉ thêm, không sửa/xóa; lưu `checksum SHA-256`"*) · TDS §12.2 (*"**Không** rollback bằng cách xóa dòng `food_samples`"*)

**Test Steps (Attack Simulation):** với **mọi** role, gọi:
1. `DELETE /api/v1/samples/SMP-20260706-01`
2. `DELETE /api/v1/samples/SMP-…/attachments/{id}`
3. `PUT /api/v1/samples/SMP-…/attachments/{id}` (thay file bằng ảnh khác)
4. `POST /samples/{code}/dispose` **lần thứ hai** trên mẫu đã `DISPOSED`

**Expected Result (PASS):** 1–3 trả **405 Method Not Allowed** (endpoint không tồn tại) hoặc **403**; 4 trả **409** (FSM — `TC-016`). Sau cả 4 bước, `SELECT count(*) FROM food_samples` và `sample_attachments` **không đổi**; `checksum_sha256` của attachment giữ nguyên.
**Expected Result (FAIL):** xóa được bản ghi/ảnh → có đường "dọn dẹp" hồ sơ sau khi xảy ra sự cố; đây chính là kịch bản mà kiểm tra pháp lý muốn ngăn.

**Current Status:** 🔴 Not written

---

### MF08-TC-SEC-005 — Upload: kiểm nội dung thật, không tin phần mở rộng

**Severity:** `HIGH`
**OWASP:** `A03:2021 — Injection` / `A08:2021` · **CWE:** `CWE-434 — Unrestricted Upload of File with Dangerous Type`
**Feature Under Test:** `SampleAttachmentService.store()`
**Test File:** `src/test/java/com/sfms/mf08/security/SampleUploadSecurityIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-019`
**Oracle Source:** **MSG-008** (*"File upload failed: File size exceeds the maximum limit of 10MB."*) · **MSG-009** (*"…Unsupported file type. Only PDF, JPG, and PNG are allowed."*) · **D10** · ⚠️ *(MSG-008/009 **ngoài §1.8** của MF08 — nội dung đúng nghiệp vụ upload chung, đánh dấu chờ Huy — xem **F6** §8.1)*

**Test Steps (Attack Simulation):**
1. `fake.jpg` — đuôi `.jpg` nhưng magic bytes `MZ` (PE executable) → kiểm
2. `oversize.jpg` 12 MB → kiểm
3. `payload.jpg` chứa `<?php ... ?>` trong EXIF comment → kiểm
4. Tên file `../../etc/passwd.jpg` → kiểm key lưu trữ

**Expected Result (PASS):**
1. **422 MSG-009** — từ chối theo **magic bytes**, không theo đuôi file
2. **422 MSG-008**
3. Lưu được (là ảnh hợp lệ) nhưng **không** thực thi ở bất kỳ đâu; response trả tên file đã **sanitize**
4. Key lưu trữ là `"samples/SMP-…-01.jpg"` (sinh từ **mã nghiệp vụ**, D10) — **không** ghép tên file người dùng gửi → path traversal bất khả thi

**Expected Result (FAIL):** tin phần mở rộng → file thực thi nằm trong thư mục `./data/uploads` được phục vụ qua `/media/**` (ADR-000/D10).

**Current Status:** 🔴 Not written

---

### MF08-TC-SEC-006 — Truy cập không có JWT

**Severity:** `HIGH`
**OWASP:** `A07:2021 — Identification and Authentication Failures`
**Feature Under Test:** Spring Security filter chain
**Test File:** `.../SampleRbacIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-032`
**Oracle Source:** **MSG-004** *"Session expired. Please log in again."* · mã `SMP-004` (TDS §10) · **BR-004**

**Test Steps:** gọi 9 endpoint MF08 (1) không header `Authorization`, (2) token hết hạn, (3) token chữ ký sai.

**Expected Result (PASS):** cả 3 trường hợp → **401** với `error.code = "SMP-004"`, message tiếng Anh nguyên văn MSG-004 (D7). **Không** endpoint nào của MF08 là public (khác MF04 có `POST /partners/register/*` cho Guest).
**Expected Result (FAIL):** đọc được sổ lưu mẫu khi chưa đăng nhập → lộ tọa độ GPS bếp, họ tên nhân sự và toàn bộ lịch trình bữa ăn.

**Current Status:** 🔴 Not written

---

### MF08-TC-SEC-007 — Không rò `storage_key` / đường dẫn nội bộ ra response và log

**Severity:** `MEDIUM`
**OWASP:** `A01:2021` · **CWE:** `CWE-200 — Exposure of Sensitive Information`
**Feature Under Test:** `FoodSampleMapper` + logging config
**Test File:** `src/test/java/com/sfms/mf08/security/InfoLeakTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-035`
**Oracle Source:** TDS **§14.2** (*"Không rò rỉ đường dẫn storage nội bộ ra response/log"* — expected: no output) · **D10** (dev `./data/uploads`, prod R2)

**Test Steps:**
1. `GET /samples/{code}` có attachment → assert JSON chỉ có `{name, uploadedAt, size}` (đúng shape `UploadedFile` của mock), **không** `storageKey`, **không** `checksumSha256`, **không** đường dẫn tuyệt đối
2. Chạy suite với log level DEBUG, grep log: `data/uploads`, `r2.cloudflarestorage`, `Bearer ` → **0 kết quả**

**Expected Result (PASS):** 2 phép kiểm sạch.
**Expected Result (FAIL):** lộ `storage_key` → đoán được key của ảnh bằng chứng khác (`samples/SMP-2026….jpg` là chuỗi đoán được) và tải trực tiếp qua `/media/**`.

**Current Status:** 🔴 Not written

---

### MF08-TC-SEC-008 — Contract test trên `/v3/api-docs` *(D8 — chỉ chạy khi Nam approve)*

**Severity:** `MEDIUM`
**Feature Under Test:** springdoc-OpenAPI spec sinh từ code
**Test File:** `src/test/java/com/sfms/mf08/contract/OpenApiContractIT.java`
**TDD Phase:** ⏸️ `@Disabled("D8 chờ Nam approve — 00_Common_Conventions.md changelog 2026-07-30")`
**Condition Ref:** `TC-COND-036`
**Oracle Source:** **D8** luật **S3/S4/S5/S6** + mục *"§13 / TestSpec — ≥1 contract test đọc `/v3/api-docs` và assert: (a) không field `id` nào có `format: uuid`; (b) mọi response 4xx/5xx trỏ `ErrorResponse`; (c) mọi enum khớp bảng D6"*

**Test Steps:** tải `/v3/api-docs`, lọc path bắt đầu bằng `/api/v1/samples`, `/api/v1/sample-lab-requests`, `/api/v1/receiving-authorities`, rồi assert 3 điều kiện + 2 điều kiện riêng của MF08:
4. Mọi `@Operation.description` chứa **UC ID + BR ID** (S2) — vd `"UC-069 · BR-105, BR-203"`
5. Field ngày khai `format: date-time` với `example` ISO (S4/D4)

**Expected Result (PASS):** 5 assertion xanh khi D8 được approve và controller đã annotate.
**Expected Result (FAIL):** spec lộ UUID hoặc enum viết tay bằng `allowableValues` → type FE sinh từ `openapi-typescript` (T3.4) **sai theo**, và lệch D1/D6 quay lại qua đường file sinh tự động.

**Current Status:** ⏸️ Disabled (chờ D8)

---

### MF08-TC-SEC-009 — **D11:** ba vai trò thấy CÙNG tập field *(mới 2026-08-03 — bắt buộc theo §7 Bước 1 việc 3)*

**Severity:** `HIGH`
**Feature Under Test:** `FoodSampleController#list` · `PagedResponseFoodSampleResponse`
**Test File:** `src/test/java/com/sfms/mf08/web/RoleFieldParityIT.java`
**TDD Phase:** 🔴 Not written
**Condition Ref:** `TC-COND-038` *(mới)*
**Oracle Source:** **D11** (`02_Module_Completion_Workflow.md` §6) + **`M08-D13`** (TDS §2.4) + **S7** (tên schema giữ tiền tố `PagedResponse` — `OpenApiMetaContractIT`)
**Fixtures:** `FX-017` (3 JWT cùng `schoolId=SCH-HN-0042`), `FX-002`…`FX-006` (seed mẫu)

> **Vì sao TC này tồn tại dù kết luận là "D11 không áp dụng".** MF08 **không** khai `oneOf`: khác biệt giữa 3 vai trò nằm ở **tập DÒNG** (`schoolId` từ JWT) và **quyền gọi** (§16), không ở **tập field** — đúng khuôn MF06 đã chốt (*lọc dòng không cần `oneOf`, lọc field thì cần*). Nhưng MF02 và MF03 **cũng "không có vấn đề" lúc viết TDS** rồi **về sau** mới thêm nhánh rút gọn theo vai trò, và lúc đó không có test nào bắt được — FE phải đăng nhập từng vai trò gọi thử mới biết. TC này là **hàng rào**: ngày nào có người thêm nhánh rút gọn, nó đỏ ngay và buộc người đó khai `oneOf` tử tế.

**Test Steps:**
1. Gọi `GET /api/v1/samples?page=1&pageSize=8` lần lượt bằng **3 token** của `FX-017`.
2. Với mỗi response, lấy **tập khoá JSON** của `items[0]` (và của mọi phần tử, phòng trường hợp mapper rẽ nhánh theo dòng).
3. Assert **3 tập khoá bằng nhau tuyệt đối** (`assertThat(keysAdmin).isEqualTo(keysMedical).isEqualTo(keysCaterer)`) — so **cả tên lẫn kiểu** của từng field, không chỉ tên.
4. Assert cả 3 response đều đủ **5 field** phân trang và `total` **giống nhau** (cùng trường ⇒ cùng tập dòng; nếu lệch thì đang có lọc theo vai trò **chưa được khai ở §16**).
5. Đọc `/v3/api-docs`: schema của `GET /api/v1/samples` phải tên **`PagedResponseFoodSampleResponse`** (giữ tiền tố `PagedResponse` — S7), `items` gõ **`FoodSampleResponse[]`** — **không** phải `{}`; và **không** có khoá `oneOf` nào trong schema của MF08.

**Expected Result (PASS):** 3 tập khoá giống hệt · `items` đúng kiểu trong `/v3/api-docs` · 0 `oneOf`.
**Expected Result (FAIL):** một vai trò thiếu field ⇒ **hoặc** khai `oneOf` với record chỉ-để-sinh-tài-liệu (mẫu `StudentDtos.PagedResponseStudentResponse`), **hoặc** bỏ nhánh rút gọn. Nếu `items` ra `{}` ⇒ ai đó đã đổi chữ ký thành `PagedResponse<?>` hoặc đặt `name=` cho `@Schema` generic (§5 của `02_Module_Completion_Workflow.md` — **cấm**, nó phá type sinh ra cho **cả 11 endpoint danh sách** của toàn dự án, không riêng MF08).

**Current Status:** 🔴 Not written

---

### INTEGRATION TEST CASES

> Dùng **Testcontainers PostgreSQL 16** + Flyway thật (8 CHECK constraint của TDS §5.2 phải chạy). Timeout 120s.

---

### MF08-TC-INT-001 — Đăng ký mẫu end-to-end (UC-069 happy path)

**Severity:** `HIGH`
**Feature Under Test:** Full flow: `POST /api/v1/samples` → 4 cổng BR → DB → attachment → event → audit
**Test File:** `src/test/java/com/sfms/mf08/integration/FoodSampleRegisterIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-021`
**Oracle Source:** **UC-069 Postconditions** — *"Sample retention records created and linked to inspection records. **72-hour** cold storage timer initiated (BR-111)"* ⚠️ *(UC file còn ghi 72h; **BR-111 sửa 26/07/2026 = 24h** là nguồn chuẩn — CF-M08-1)* · **MSG-044** *"Food sample retention registered. Cold storage timer initiated."* · **BR-114** (liên kết thực đơn/kiểm tra/lô)

**Preconditions:** Testcontainers PG16 + Flyway migrate; seed FX-002…FX-008; stub FX-009/010/011/012; `Clock` FX-001.

**Test Steps:**
1. `POST /api/v1/samples` multipart: `payload` (JSON hợp lệ) + `evidencePhotos=@mau-com-trang.jpg`
2. Assert response 201
3. Assert DB
4. Assert event + audit

**Expected Result (PASS):**
- **201** với body chứa `id` = mã nghiệp vụ (D1), `status: "RETAINED"`, `hoursRemaining` tính runtime, `retentionDeadline` ISO = `collectedAt + 24h`
- Response header/body mang **MSG-044**
- DB: 1 dòng `food_samples` với `school_id` = JWT, `inspection_id` = giá trị gửi lên (BR-105/BR-114), `dish_id` không null (chuỗi truy xuất `mẫu → món → lô`), `temp_out_of_range = false`
- DB: 1 dòng `sample_attachments` `kind = EVIDENCE_PHOTO`, `checksum_sha256` khớp file
- Event `FoodSampleRegistered` phát đúng 1 lần; audit log có dòng `CREATE FoodSample SMP-…` kèm actor + IP (khớp khuôn seed `AUD-99120` của mock)

**DB Assertion:**
```sql
SELECT status, retention_deadline - collected_at AS span, temp_out_of_range
  FROM food_samples WHERE code = 'SMP-20260706-05';
-- Expected: RETAINED | 24:00:00 | false
SELECT count(*) FROM sample_attachments
 WHERE owner_type='SAMPLE' AND kind='EVIDENCE_PHOTO';   -- Expected: 1
```

**Expected Result (FAIL):** `span` ≠ 24h (còn hằng số 72), hoặc `inspection_id` null → cổng BR-105 không có dữ liệu để chạy ở các lần sau (`review.md` F3).

**Current Status:** 🔴 Not written

---

### MF08-TC-INT-002 — Bàn giao lô mẫu là **một transaction** (ADR-M08-1)

**Severity:** `CRITICAL`
**Feature Under Test:** Full flow: `POST /api/v1/sample-lab-requests` → phiếu + N mẫu + attachments
**Test File:** `src/test/java/com/sfms/mf08/integration/SampleHandOverIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-022`
**Oracle Source:** **ADR-M08-1** (*"Bàn giao là **một transaction**: tạo phiếu + chuyển N mẫu sang `SENT_TO_LAB` (không có trạng thái nửa vời)"*) · **BR-113** · **BR-212** · **`review.md` F8** (*"FE gọi POST rồi lặp PUT từng mẫu — mất tính nguyên tử. **Backend đúng**"*)

**Test Steps:**
1. Happy path: 3 mẫu `RETAINED` của `INC-2026-014` → `POST` 1 lần với `handoverRecords=@bien-ban-giao-nhan.pdf`
2. Rollback path: cùng payload nhưng **0 file** `HANDOVER_RECORD`
3. Rollback path 2: mẫu thứ 3 trong lô đang `SENT_TO_LAB` (FX-005)

**Expected Result (PASS):**
- (1) **201**; `sample_lab_requests` +1 dòng `status=SENT_TO_LAB`; **cả 3** mẫu có `status=SENT_TO_LAB`, `lab_request_id` trỏ đúng phiếu, `sent_to_lab_at` = `requestedAt` của phiếu, `incident_id` = `INC-2026-014`; `sample_attachments` +1 `HANDOVER_RECORD` (+ `SEAL_PHOTO` nếu gửi kèm)
- (2) **422 `SMP-212`** và **0 dòng** thêm ở cả 3 bảng — rollback sạch
- (3) **409 `SMP-113`** và **0 dòng** thêm; 2 mẫu hợp lệ **vẫn** `RETAINED` (không đổi một nửa)

**DB Assertion:**
```sql
SELECT count(*) FROM sample_lab_requests;                        -- (2)(3): không tăng
SELECT count(*) FROM food_samples WHERE status='SENT_TO_LAB';    -- (2)(3): giữ nguyên 1 (FX-005)
SELECT count(DISTINCT lab_request_id) FROM food_samples
 WHERE incident_id = (SELECT id FROM incidents WHERE code='INC-2026-014'); -- (1): 1
```

**Expected Result (FAIL):** phiếu tạo xong nhưng vài mẫu chưa đổi trạng thái → sổ lưu mẫu và phiếu bàn giao nói hai chuyện khác nhau về cùng một hộp mẫu (luật `MAU_KHONG_GAN_NGUOC_VE_PHIEU`).

**Current Status:** 🔴 Not written
**Implementation Note:** `@Transactional` phải bao **cả** phần lưu attachment; nếu `StorageService.store()` ném lỗi giữa chừng, file đã ghi cần được dọn (compensating action) — ghi rõ trong Implementation Note của service, test chỉ assert DB sạch.

---

### MF08-TC-INT-003 — Sự cố không hợp lệ / đã đóng chặn bàn giao (BR-114, MSG-023)

**Severity:** `HIGH`
**Feature Under Test:** `handOver()` + `IncidentStatusPort`
**Test File:** `.../SampleHandOverIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-023`
**Oracle Source:** **UC-076 AF2** (*"Invalid Incident ID … System blocks the retrieval authorization and displays error message **MSG-023**"*) · **BR-114** · mã `SMP-023` · ⚠️ **L12/CF-M08-6** — test **không** khóa vào nội dung văn bản MSG-023

**Test Steps (Equivalence Partitioning):**

| `incidentId` | Trạng thái | Kỳ vọng |
|---|---|---|
| `INC-2026-014` | OPEN | ✅ 201 |
| `INC-2026-012` | **CLOSED** | ❌ 409 `SMP-023` |
| `INC-9999` | không tồn tại | ❌ 409 `SMP-023` |
| `null` | — | ❌ 422 (field bắt buộc) |

**Expected Result (PASS):** 3 nhánh ❌ không tạo phiếu, không đổi trạng thái mẫu; assert **chỉ** `error.code == "SMP-023"` + HTTP 409.
**Expected Result (FAIL):** gửi mẫu cho một ca đã đóng → chi phí xét nghiệm cho hồ sơ không còn mở, và mẫu mất khỏi tủ mà không có ca nào nhận kết quả.

**Current Status:** 🔴 Not written
**Implementation Note:** Đây là **ngoại lệ duy nhất** của `MF08-TC-INT-009` (không assert `message` khớp `error_message.md`) vì MSG-023 đang bị mượn sai ngữ nghĩa — khi Huy cấp MSG mới (`review_fix_plan.md` F9), đổi 1 dòng ở đây và bỏ ngoại lệ.

---

### MF08-TC-INT-004 — Không bàn giao lần hai cùng một mẫu (BR-113)

**Severity:** `HIGH`
**Feature Under Test:** `handOver()` + FSM + DB state
**Test File:** `.../SampleHandOverIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-015`
**Oracle Source:** **ADR-M08-2** (`SENT_TO_LAB` là trạng thái cuối) · **BR-113** · mã `SMP-113`

**Test Steps:**
1. Bàn giao thành công lô `[SMP-A, SMP-B]` → phiếu `LABREQ-X`
2. Bàn giao lại đúng `SMP-A` cho phiếu mới (cùng sự cố) → kiểm
3. Bàn giao `SMP-A` cho **sự cố khác** đang mở → kiểm

**Expected Result (PASS):** bước 2 và 3 đều **409 `SMP-113`**; `SMP-A` giữ nguyên `lab_request_id = LABREQ-X` và `incident_id` ban đầu (không bị ghi đè).
**Expected Result (FAIL):** `incident_id` bị ghi đè bởi ca thứ hai → luật `PHIEU_KHAC_SU_CO_VOI_MAU` của `semantic-audit.ts` tái phát ở tầng dữ liệu thật, và truy xuất sự cố cũ mất mẫu.

**Current Status:** 🔴 Not written

---

### MF08-TC-INT-005 — Khóa/mở khóa hủy mẫu theo vòng đời sự cố (BR-112)

**Severity:** `CRITICAL`
**Feature Under Test:** `dispose()` + `IncidentStatusPort` + handler `IncidentClosed`
**Test File:** `src/test/java/com/sfms/mf08/integration/FoodSampleDisposeIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-010`, `TC-COND-011`
**Oracle Source:** **BR-112** (*"chặn hủy … **cho đến khi sự cố được đóng** hoặc có thông báo khác"*) · TDS §7.2 (event `IncidentClosed` → `SampleDisposalLockHandler`) · **UC-030 AF1**

**Test Steps:**
1. Mẫu `FX-004` (quá hạn lưu, gắn `INC-2026-014` OPEN) → `POST /dispose` → **409 `SMP-112`**
2. Bắn event `IncidentClosed(INC-2026-014)`
3. `POST /dispose` lại → **200**, `status=DISPOSED`
4. Mẫu `FX-002` (chưa hết hạn, không gắn sự cố) → `POST /dispose` → **409 `SMP-111`**

**Expected Result (PASS):** 4 bước đúng mã; sau bước 3, DB có đủ `disposed_at`, `disposed_by`, `disposal_witness`, `disposal_note` (CHECK `ck_smp_dispose_complete` không thể bị lách).
**Expected Result (FAIL):** mẫu mở khóa hủy **trước khi** sự cố đóng → mất bằng chứng của ca đang điều tra; TDS §12.3 yêu cầu báo DPO cho tình huống này.

**Current Status:** 🔴 Not written

---

### MF08-TC-INT-006 — UC-076 AF1: liên kết mẫu **đã hủy** với sự cố (chỉ metadata)

**Severity:** `MEDIUM`
**Feature Under Test:** `POST /api/v1/samples/{code}/incident-link`
**Test File:** `src/test/java/com/sfms/mf08/integration/DisposedSampleLinkIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-016`, `TC-COND-023`
**Oracle Source:** **UC-076 AF1** — *"Medical Staff **cannot** authorize physical retrieval, but they can select **'Link Disposed Sample to Incident'** to associate the disposal metadata with the active incident for record-keeping"* · **BR-114** · TDS §6.4 (transition `DISPOSED → DISPOSED` + `incidentId`)

**Test Steps:**
1. `FX-006` (DISPOSED) + `INC-2026-014` OPEN → `POST /incident-link` → kiểm
2. Cùng mẫu + `INC-2026-012` CLOSED → kiểm
3. Mẫu `RETAINED` gọi `/incident-link` → kiểm

**Expected Result (PASS):**
- (1) **200**; `incident_id` được ghi; `status` **vẫn** `DISPOSED`; `lab_request_id` vẫn null; không attachment nào sinh ra; event `DisposedSampleLinkedToIncident` phát 1 lần
- (2) **409 `SMP-023`**
- (3) **409** — mẫu còn lưu phải đi đường bàn giao thật (`POST /sample-lab-requests`), không phải liên kết metadata

**Expected Result (FAIL):** đổi `status` của mẫu đã hủy → hồ sơ nói mẫu đang được gửi lab trong khi nó đã bị tiêu hủy 3 ngày trước.

**Current Status:** 🔴 Not written

---

### MF08-TC-INT-007 — Hợp đồng phân trang §2b trên 3 resource MF08

**Severity:** `HIGH`
**Feature Under Test:** `PagedResponse<T>` + `FoodSampleSpec` + `SampleLabRequestSpec`
**Test File:** `src/test/java/com/sfms/mf08/integration/PagedContractIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-024`
**Oracle Source:** **`00_Team_Work_Assignment_Plan.md` §2b-1/§2b-3/§2b-6** (5 field · thứ tự lọc · edge cases) + **§2b-5** (*"`samples` → field ngày `collectedAt`, kiểu `Instant`"*) + **§2b-7** (checklist review của Mạnh) · TDS §17.1 **C1**

**Test Steps (áp cho `/samples`, `/sample-lab-requests`, `/receiving-authorities`):**

| # | Request | Kỳ vọng |
|---|---|---|
| 1 | `GET /samples?page=1&pageSize=8` | 200, đủ **5 field** `items/total/page/pageSize/pageCount`, không wrapper |
| 2 | `GET /samples?page=99&pageSize=8` | `page == pageCount` (**clamp**), không 404 |
| 3 | `GET /samples?status=DISPOSED&dateFrom=2030-01-01` | `{items:[], total:0, page:1, pageSize:8, pageCount:1}` — **200**, `pageCount ≥ 1` |
| 4 | `GET /samples?pageSize=0` | `pageSize` clamp về **1** |
| 5 | `GET /samples?dateFrom=2026-07-06&dateTo=2026-07-06` | lọc trên **`collected_at`**, trả đúng 4 mẫu bữa trưa 06/07 |
| 6 | `GET /samples?mealType=LUNCH` | lọc enum (**thay `mealPrefix` của mock** — L3) |
| 7 | `GET /samples?search=inox` | `total == 0` — `container` **không** thuộc `SEARCH_FIELDS` |
| 8 | `GET /samples?search=Mai` | ra mẫu của "Lê Thị Mai" — `collected_by` **có** trong `SEARCH_FIELDS`, và tên field JSON là `collectedBy` (F7) |
| 9 | `GET /samples?not:status=DISPOSED` | lọc phủ định theo §2b-2 |
| 10 | `GET /samples?dishName=x&status=` | field rỗng bị **bỏ qua**, không sinh predicate |

**Expected Result (PASS):** 10 case đúng; `search` sinh đúng **1** mệnh đề `OR ILIKE` trên 4 cột (bắt bằng cách bật `spring.jpa.show-sql` và assert số lượng `lower(... ) like ?`).
**Expected Result (FAIL):** case 3 trả **404** (FE hiện trang trắng thay vì "không có kết quả"), hoặc case 7 trả >0 (search OR toàn bộ cột String → full table scan, đúng cảnh báo §2b-3 bước 5).

**Current Status:** 🔴 Not written
**Implementation Note:** Test này gần như **dùng lại nguyên** cho 12 module — cân nhắc đưa lên `common` như `MF04-TC-INT-011` đã ghi.

---

### MF08-TC-INT-008 — `id` công khai là mã nghiệp vụ, không UUID (D1/L1)

**Severity:** `CRITICAL`
**Feature Under Test:** Toàn bộ DTO + path param của MF08
**Test File:** `src/test/java/com/sfms/mf08/integration/PublicIdContractIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-020`
**Oracle Source:** **D1** · **`review.md` A1** (blocker, MF08 nằm trong nhóm bị nêu tên) · `00_Common_Conventions.md` §D1 *"Việc phải làm"* dòng MF08

**Test Steps:**
1. `GET /api/v1/samples/SMP-20260706-01` (path param = **mã**) → 200
2. `GET /api/v1/samples/{uuid-thật-của-bản-ghi-đó}` → kiểm
3. Duyệt **toàn bộ** JSON của 6 endpoint đọc, quét regex UUID `[0-9a-f]{8}-[0-9a-f]{4}-...`
4. Kiểm FK trong JSON: `incidentId == "INC-2026-014"`, `labRequestId == "LABREQ-2026-001"`, `dishId` = mã món, `inspectionId` = mã phiên

**Expected Result (PASS):** (1) 200; (2) **404** — UUID không phải định danh công khai; (3) **0 kết quả** regex UUID; (4) 4 FK đều là mã nghiệp vụ.
**Expected Result (FAIL):** FE `useDetail("samples", "SMP-…")` gọi `GET /samples/SMP-…` sẽ 404 vì backend chờ UUID → **mọi màn chi tiết của MF08 hỏng** ngay khi đổi `baseURL`.

**Current Status:** 🔴 Not written

---

### MF08-TC-INT-009 — Envelope lỗi D2 và thông điệp tiếng Anh D7

**Severity:** `HIGH`
**Feature Under Test:** `@RestControllerAdvice` (nền chung) + bảng mã lỗi MF08
**Test File:** `src/test/java/com/sfms/mf08/integration/ErrorEnvelopeIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-025`
**Oracle Source:** **D2** (`{error:{code,message,details}}`, `details` **mảng, không bao giờ null**) · **D7** (message **tiếng Anh** nguyên văn `error_message.md`) · TDS §10 · **L4/L6**

**Test Steps:** kích hoạt lần lượt 12 mã lỗi của MF08 và assert cấu trúc.

| Mã | HTTP | Nguồn message |
|---|---|---|
| `SMP-004` | 401 | MSG-004 (nguyên văn) |
| `SMP-005` | 403 | MSG-005 (nguyên văn) |
| `SMP-019` | 422 | MSG-019 (nguyên văn) |
| `SMP-020` | 200 ⚠️ | MSG-020 — **cảnh báo mềm**, trả trong `warnings[]` chứ không phải `error` |
| `SMP-023` | 409 | ⚠️ *(ngoại lệ — xem `TC-INT-003`/L12)* |
| `SMP-030` · `SMP-105` · `SMP-107` · `SMP-109` · `SMP-111` · `SMP-112` · `SMP-113` · `SMP-203` · `SMP-212` · `SMP-409` | 4xx | **mã dẫn xuất BR** — assert message là **tiếng Anh** (regex `[àáảãạăâđêôơư]` **không khớp**) |

**Expected Result (PASS):**
- Mọi response lỗi có đủ **3 field**; `details` là `[]` khi không có chi tiết (**không** `null`)
- 4 mã có MSG chính thức khớp **byte-for-byte** `error_message.md`
- 9 mã dẫn xuất BR: message tiếng Anh (D7 yêu cầu dịch — TDS §10 hiện viết tiếng Việt, xem **L4**)

**Expected Result (FAIL):** `details: null` → FE `.map()` trên null; hoặc message tiếng Việt → không map được sang `lib/messages.ts` khi có bản dịch chuẩn.

**Current Status:** 🔴 Not written
**Implementation Note:** ⚠️ Test này sẽ FAIL 🔴 hợp lệ cho tới khi **TDS §10 được dịch sang tiếng Anh** (việc của Mạnh — **F5** §8.1). Không sửa test cho xanh bằng cách chấp nhận tiếng Việt.

---

### MF08-TC-INT-010 — Client gửi `id`: dùng làm `code`, trùng thì 409 (D5/L5)

**Severity:** `HIGH`
**Feature Under Test:** `POST /api/v1/samples` + `code_sequences`
**Test File:** `src/test/java/com/sfms/mf08/integration/ClientSuppliedIdIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-026`
**Oracle Source:** **D5** — *"body có `id` → dùng làm `code`, kiểm trùng → trùng thì **409**; vắng → server tự sinh theo `code_sequences`; backend **luôn** trả `id` trong response 201"*; D5 nêu đích danh `SMP-${Date.now()}` là một trong 6 resource FE tự sinh id

**Test Steps:**
1. `POST` với `"id": "SMP-20260707-99"` → 201, `code` lưu đúng giá trị đó
2. `POST` lại **cùng** `"id"` → kiểm
3. `POST` **không** có `id` → 201, server sinh `code` theo khuôn `SMP-yyyyMMdd-NN`
4. `POST` với `"id": "../../etc"` / chuỗi 200 ký tự → kiểm

**Expected Result (PASS):**
- (1) `response.id == "SMP-20260707-99"`
- (2) **409 `SMP-409`**, không tạo bản ghi thứ hai
- (3) `code` khớp regex `^SMP-\d{8}-\d{2}$`, và **duy nhất** trong `(school_id, code)`
- (4) **422** — `code` phải khớp khuôn, không nhận chuỗi tùy ý

**Expected Result (FAIL):** server tự cấp mã khác với mã client gửi → FE `router.push('/service-caterers/samples/' + id)` ngay sau `onSuccess` sẽ điều hướng tới **404** (đúng lý do D5 tồn tại).

**Current Status:** 🔴 Not written

---

### MF08-TC-INT-011 — Port **10 luật** `semantic-audit.ts` chạm MF08

**Severity:** `HIGH`
**Feature Under Test:** CHECK constraints + service invariants
**Test File:** `src/test/java/com/sfms/mf08/integration/SemanticAuditRulesIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-027`
**Oracle Source:** `semantic-audit.ts` mục 3 (dòng 78–99), mục 4 (101–113), mục 5 (115–141) · **`00_Team_Work_Assignment_Plan.md` §2a** (*"§13 Test Scenarios ← `semantic-audit.ts` — 54 luật port thành Gherkin"*) · TDS §13.2 TC-INT-003

```gherkin
Feature: Ràng buộc ngữ nghĩa quanh sổ lưu mẫu (port từ semantic-audit.ts)

  # ── Mục 3: mẫu lưu (QĐ 1246 — Mẫu số 5) ─────────────────────────
  Scenario: MAU_SAI_BUA (dòng 82-85) — nhãn bữa phải khớp ngày lấy mẫu
    When POST /samples với mealShift/mealLabel ghi ngày 02/07 nhưng collectedAt = 2026-07-06
    Then 422 — ngày trong nhãn bữa phải khớp ngày của collected_at

  Scenario: HAN_LUU_KHONG_DU_24H (dòng 86-90 — script còn tên _72H, CF-M08-1)
    When đăng ký mẫu bất kỳ
    Then retention_deadline - collected_at = đúng 24 giờ (BR-111 sửa 26/07/2026)
    And  không bản ghi nào trong DB có span < 24 giờ

  Scenario: GUI_LAB_TRUOC_KHI_LAY (dòng 91-94)
    When cố ghi sent_to_lab_at < collected_at
    Then bị chặn bởi CHECK ck_smp_sent_after_collect

  Scenario: HUY_MAU_TRUOC_HAN_24H (dòng 95-98 — script còn tên _72H)
    When cố ghi disposed_at < retention_deadline
    Then 409 SMP-111 ở service VÀ bị chặn bởi CHECK ck_smp_dispose_after_deadline

  # ── Mục 4: mẫu gắn sự cố ────────────────────────────────────────
  Scenario: MAU_NGOAI_KHUNG_SU_CO (dòng 101-113)
    Given INC-2026-014 có occurredAt = 2026-06-30T04:00Z
    When GET /sample-lab-requests/retrievable?incidentId=INC-2026-014
    Then chỉ trả mẫu có collected_at trong cửa sổ 24 giờ trước occurredAt (BR-149 sửa 27/07/2026)
    And  mẫu bữa 02/07 KHÔNG xuất hiện dù cùng tên món "Thịt kho trứng cút"
    # ⚠️ script dùng khung [-72h, +24h] — lệch, xem CF-M08-10 / F1

  # ── Mục 5: phiếu bàn giao mẫu ───────────────────────────────────
  Scenario: PHIEU_TRO_MAU_KHONG_TON_TAI (dòng 121)
    When POST /sample-lab-requests với sampleIds chứa "SMP-KHONG-CO"
    Then 404 SMP-404 và phiếu KHÔNG được tạo

  Scenario: BAN_GIAO_TRUOC_KHI_LAY_MAU (dòng 122-125)
    When phiếu có requestedAt < collected_at của một mẫu trong lô
    Then 422 — không bàn giao thứ chưa tồn tại

  Scenario: PHIEU_KHAC_SU_CO_VOI_MAU (dòng 126-130)
    Given SMP-A đã gắn INC-2026-014
    When bàn giao SMP-A trong phiếu thuộc INC khác
    Then 409 SMP-113 và incident_id của SMP-A KHÔNG bị ghi đè

  Scenario: MAU_KHONG_GAN_NGUOC_VE_PHIEU (dòng 133-135)
    When bàn giao lô 3 mẫu thành công
    Then mọi mẫu trong sampleIds đều có lab_request_id = id phiếu (quan hệ khớp HAI chiều)
    # Bất khả xảy ra theo ADR-M08-1 (cùng transaction) — scenario này KHẲNG ĐỊNH điều đó

  Scenario: PHIEU_THU_GIU_THIEU_CO_QUAN (dòng 137-140)
    When POST /sample-lab-requests với handoverType=AUTHORITY nhưng thiếu authorityId
    Then 422 và bị chặn bởi CHECK ck_labreq_authority
```

**Expected Result (PASS):** 10 scenario xanh; 6/10 chạy được **hoàn toàn trong MF08** (không cần MF06/MF07/MF11 thật).
**Expected Result (FAIL):** scenario 2 hoặc 4 đỏ → còn hằng số 72h ở đâu đó (CF-M08-1); scenario 9 đỏ → bàn giao không nguyên tử (ADR-M08-1 chưa được hiện thực đúng).

**Current Status:** 🔴 Not written
**Implementation Note:** Scenario `MAU_NGOAI_KHUNG_SU_CO` phụ thuộc `IncidentStatusPort.windowOf()` của MF11; nếu MF11 chưa xong, chạy ở mức **port contract** (stub trả cửa sổ 24h) và đánh dấu phần còn lại là cross-module test ở Phase 4 (`review_fix_plan.md`).

---

### MF08-TC-INT-012 — Schema: không cột dẫn xuất, có đủ cột `review.md` §F đòi

**Severity:** `HIGH`
**Feature Under Test:** Flyway migration MF08
**Test File:** `.../SemanticAuditRulesIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-009`, `TC-COND-028`
**Oracle Source:** **ADR-M08-3/M08-5** · `01_MVP_Backend_Implementation_Plan.md` §3 (*"**Không** lưu field dẫn xuất (`hoursRemaining`, …)"*) · **AP-AI-009** (TDS §17.4) · **`review.md` F2/F3/F5/F6** (các cột thiếu ở FE nhưng **phải có** ở backend)

**Test Steps:** sau khi Flyway migrate, truy vấn `information_schema.columns`.

**Expected Result (PASS):**
```sql
-- (a) KHÔNG có cột dẫn xuất
SELECT column_name FROM information_schema.columns
 WHERE table_name='food_samples' AND column_name IN ('hours_remaining','batch_ids');   -- 0 rows

-- (b) CÓ đủ cột mà review.md §F yêu cầu
SELECT column_name FROM information_schema.columns
 WHERE table_name='food_samples'
   AND column_name IN ('code','sample_amount','sample_unit','inspection_id',
                       'disposal_witness','temp_out_of_range','gps_lat','gps_lng');     -- 8 rows

-- (c) code là mã nghiệp vụ, unique theo trường (D1)
SELECT indexdef FROM pg_indexes
 WHERE tablename='food_samples' AND indexdef LIKE '%school_id%code%';                   -- >= 1 row

-- (d) 8 CHECK constraint của TDS §5.2 tồn tại
SELECT conname FROM pg_constraint
 WHERE conrelid='food_samples'::regclass AND contype='c';   -- gồm ck_smp_amount,
 -- ck_smp_container_state, ck_smp_dispose_after_deadline, ck_smp_dispose_complete,
 -- ck_smp_sent_has_request, ck_smp_sent_after_collect
```
Đồng thời `sample_lab_requests` có `ck_labreq_criteria` và `ck_labreq_authority`.

**Expected Result (FAIL):**
- Có `hours_remaining` → sổ lưu mẫu hiện số giờ đóng băng (đúng lỗi seed mock đang có)
- Thiếu `sample_amount`/`sample_unit` → **BR-203 không enforce được** (`review.md` F2 gọi là blocker)
- Thiếu `inspection_id` → **BR-105 không có dữ liệu để chạy** (F3)
- Thiếu `disposal_witness` → dữ liệu tuân thủ pháp lý bị mất (F5)

**Current Status:** 🔴 Not written
**Implementation Note:** ⚠️ **Bẫy Red Gate:** khi chưa có bảng nào, truy vấn (a) trả 0 rows và test "xanh giả". Thêm tiền đề: assert bảng `food_samples` **tồn tại** trước khi assert cột không tồn tại.

---

### MF08-TC-INT-013 — Xuất sổ Mẫu số 5 đúng 12 cột theo filter (UC-118)

**Severity:** `MEDIUM`
**Legal:** **Mẫu số 5, Phụ lục 2 — QĐ 1246/QĐ-BYT**
**Feature Under Test:** `GET /api/v1/samples/export?format=xlsx|pdf`
**Test File:** `src/test/java/com/sfms/mf08/integration/SampleRegisterExportIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-029`
**Oracle Source:** **UC-118 Normal Flow bước 2** — *"displaying the digital Food Sample Retention and Disposal register conforming to **Mẫu số 5**, displaying **all columns (1 to 12)**"* + bước 4 (*"Export … as Excel or PDF"*) · thứ tự cột lấy từ `components/app/sample-law-print.tsx` (bản in hiện hành) · **BR-114**

**Test Steps:**
1. `GET /samples/export?format=xlsx&dateFrom=2026-06-28&dateTo=2026-07-06`
2. Đọc file XLSX trả về, assert header + nội dung
3. Lặp với `format=pdf` (chỉ assert content-type + số dòng)

**Expected Result (PASS):**
- Đúng **12 cột**, đúng thứ tự Phụ lục 2: (1) TT · (2) Tên mẫu thức ăn · (3) Bữa ăn (giờ ăn) · (4) Số lượng suất ăn · (5) Khối lượng/thể tích · (6) Dụng cụ chứa · (7) Nhiệt độ bảo quản · (8) Thời gian lấy mẫu · (9) Thời gian hủy mẫu · (10) Ghi chú · (11) Người lưu mẫu · (12) Người hủy mẫu
- Chỉ chứa mẫu trong khoảng ngày (**lọc theo `collected_at`**), **không** xuất toàn bộ
- Cột (5) hiển thị `"120 g"` / `"180 ml"` (ghép `amount` + `unit` ở tầng trình bày — dữ liệu vẫn tách rời trong DB, L8)
- Cột (10) ghép **nhân chứng** vào ghi chú để đúng mẫu biểu luật, trong khi DB vẫn giữ `disposal_witness` riêng (L11/CF-M08-9)
- Ngày trong file xuất hiển thị `dd/MM/yyyy HH:mm` — **đây là tầng trình bày, không phải JSON** (không mâu thuẫn D4)

**Expected Result (FAIL):** thiếu cột hoặc sai thứ tự → bản in không đúng mẫu biểu luật, thanh tra từ chối; hoặc xuất toàn bộ dữ liệu bất kể filter (mock hiện chỉ `toast` giả lập nên chưa ai phát hiện).

**Current Status:** 🔴 Not written

---

### MF08-TC-INT-014 — Job hạn lưu chạy, job nhiệt độ **không** tồn tại (ADR-M08-6)

**Severity:** `MEDIUM`
**Feature Under Test:** `RetentionDeadlineScheduler`
**Test File:** `src/test/java/com/sfms/mf08/integration/RetentionSchedulerIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-007`, `TC-COND-030`
**Oracle Source:** **UC-030 Trigger** (*"System generates a disposal alert when a food sample's mandatory retention period expires with no active incident linked"*) · **ADR-M08-6** · **BR-110 sửa 11/07/2026** (*"bỏ cơ chế cảnh báo tự động … không còn gắn với bất kỳ cơ chế cảnh báo/giám sát tự động nào"*) · **CF-M08-3**

**Test Steps:**
1. Đẩy `Clock` tới `2026-07-07T04:00Z` (quá hạn `FX-002`), chạy job
2. Assert event
3. Quét toàn bộ bean `@Scheduled` trong package `com.sfms.mf08`

**Expected Result (PASS):**
- `FoodSampleRetentionExpired` phát cho `FX-002` và `FX-003` (hết hạn, `RETAINED`)
- **Không** phát cho `FX-004` — hoặc phát kèm `disposalLocked = true` (gắn sự cố mở, BR-112); và **không** phát cho mẫu `SENT_TO_LAB`/`DISPOSED`
- Chạy job **lần hai** không phát trùng (idempotent theo `sampleId`)
- **Đúng 1** bean `@Scheduled` trong package; **không** có job nào liên quan `fridgeTemp`/30 phút/MSG-059

**Expected Result (FAIL):** xuất hiện job nhiệt độ → tái lập cơ chế giám sát IoT mà BR-110 đã bỏ và hệ thống không có cảm biến để nuôi (**AP-AI-013**, TDS §17.4).

**Current Status:** 🔴 Not written

---

### MF08-TC-INT-015 — Race: hai request bàn giao cùng một mẫu

**Severity:** `HIGH`
**Feature Under Test:** `handOver()` với `PESSIMISTIC_WRITE` (`lockAllByIdIn`)
**Test File:** `.../SampleHandOverIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-022`
**Oracle Source:** TDS **§8.2** (`@Lock(LockModeType.PESSIMISTIC_WRITE) lockAllByIdIn(...)`) · **ADR-M08-1** · **ADR-M08-2** (`SENT_TO_LAB` không hoàn tác → **không sửa được** nếu ghi đè nhau)

**Test Steps:** 2 thread gửi `POST /sample-lab-requests` **đồng thời**, cùng `sampleIds = [SMP-A]`, khác nơi tiếp nhận (CDC vs NIFC).

**Expected Result (PASS):**
- Đúng **1** request trả 201, request kia trả **409 `SMP-113`**
- `SELECT count(*) FROM sample_lab_requests` tăng đúng **1**
- `SMP-A.lab_request_id` trỏ **duy nhất** phiếu thắng cuộc

**Expected Result (FAIL):** 2 phiếu cùng trỏ một hộp mẫu vật lý → hai nơi tiếp nhận cùng chờ một mẫu; vì `SENT_TO_LAB` là trạng thái cuối nên **không có đường sửa** trong hệ thống.

**Current Status:** 🔴 Not written
**Implementation Note:** Check-then-act ở service **không đủ** — phải khóa hàng bằng `SELECT … FOR UPDATE` trong cùng transaction (cùng lớp lỗi với `MF04-TC-INT-012`).

---

### MF08-TC-INT-016 — Mỗi món của một phiên kiểm thực chỉ lưu **một** mẫu (Điều 7.1)

**Severity:** `MEDIUM`
**Legal:** **QĐ 1246/QĐ-BYT Điều 7.1** — *"mỗi món ăn được lấy và lưu vào dụng cụ **riêng biệt**"*
**Feature Under Test:** `uq_smp_inspection_dish`
**Test File:** `.../FoodSampleRegisterIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-031`
**Oracle Source:** TDS §5.2 (`CREATE UNIQUE INDEX uq_smp_inspection_dish ON food_samples(inspection_id, dish_id)`) · **BR-105** (*lưu mẫu cho **tất cả món ăn**, hàm ý mỗi món **một** mẫu*)

**Test Steps:**
1. Đăng ký mẫu "Cơm trắng" cho `INS-2026-0706-T` → 201
2. Đăng ký **lại** "Cơm trắng" cho **cùng** phiên → kiểm
3. Đăng ký "Canh bí đỏ" cho cùng phiên → 201
4. Đăng ký "Cơm trắng" cho phiên **khác** → 201
5. 2 request song song ở bước 2 → kiểm

**Expected Result (PASS):** (2) **409**; (3)(4) 201; (5) đúng 1 thành công (unique index chặn, không phải check-then-act).
**Expected Result (FAIL):** 2 mẫu cùng món/cùng bữa → sổ Mẫu số 5 có dòng trùng, và khi thu hồi điều tra thì không biết hộp nào là hộp thật.

**Current Status:** 🔴 Not written

---

### MF08-TC-INT-017 — `created_by` do `AuditorAware` điền, service không set tay (D9)

**Severity:** `MEDIUM`
**Feature Under Test:** `FoodSample extends BaseEntity`
**Test File:** `.../FoodSampleRegisterIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-037`
**Oracle Source:** **D9** — *"`created_by`/`updated_by` là **tên cột reserved**: `VARCHAR(180)`, email actor, tự điền qua `AuditorAware`, **không** module nào được khai lại với kiểu/ràng buộc khác"*; *"Entity `extends BaseEntity` **không** tự khai lại `createdBy`/`updatedBy`"*

**Test Steps:**
1. `POST /samples` với JWT `sub = "maile@anbinhcatering.vn"`
2. Assert `created_by` = email actor **mà service không set tay** (grep `setCreatedBy` trong `com.sfms.mf08` → 0 kết quả)
3. `PATCH /samples/{code}/storage` bằng tài khoản khác → `updated_by` đổi, `created_by` **giữ nguyên**
4. Assert MF08 **không** khai cột nào tên `created_by`/`updated_by` với kiểu khác (D9 nêu 4 module vi phạm — MF08 **không** nằm trong đó, test này khóa chặn để không vi phạm về sau)

**Expected Result (PASS):** 4 bước đúng; `collected_by` (nghiệp vụ, cột 11 Mẫu số 5) và `created_by` (audit) là **hai field khác nhau**, cùng tồn tại, không lẫn.
**Expected Result (FAIL):** service tự set `createdBy` → giá trị sai khi thao tác chạy trong job nền (`AuditorAware` trả `"system"`), và audit trail mất tính tin cậy.

**Current Status:** 🔴 Not written
**Implementation Note:** Đây là chỗ dễ nhầm nhất của MF08: **4 khái niệm "ai"** cùng tồn tại — `collected_by` (người lấy mẫu, Mẫu số 5 cột 11) · `disposed_by` (cột 12) · `delivered_by` (người mang mẫu đi, BR-113) · `created_by`/`updated_by` (audit trail, D9). Ba cái đầu là **field nghiệp vụ**, cái cuối là **cột reserved**.

---

## 5. Red-Green-Refactor Tracker

> **47 test case** *(v1.1: 45 + `MF08-TC-021` cho D12 + `MF08-TC-SEC-009` cho D11)* — 21 unit · 9 security · 17 integration. Cột 🟢 GREEN điền commit hash khi test xanh lần đầu.

| TC ID | Test File | 🔴 RED confirmed | 🟢 GREEN (commit) | 🔵 REFACTOR note |
|-------|-----------|-----------------|-------------------|------------------|
| `MF08-TC-001` | `service/FoodSampleRegisterTest.java` | `[ ]` | | |
| `MF08-TC-002` | `service/FoodSampleRegisterTest.java` | `[ ]` | | |
| `MF08-TC-003` | `service/FoodSampleRegisterTest.java` | `[ ]` | | ⚠️ blocked bởi L9 (FE thiếu `inspectionId`) |
| `MF08-TC-004` | `service/FoodSampleRegisterTest.java` | `[ ]` | | |
| `MF08-TC-005` | `service/FoodSampleRegisterTest.java` | `[ ]` | | |
| `MF08-TC-006` | `service/FoodSampleRegisterTest.java` | `[ ]` | | |
| `MF08-TC-007` | `domain/FridgeTempRangeTest.java` | `[ ]` | | |
| `MF08-TC-008` | `domain/RetentionPolicyTest.java` | `[ ]` | | ⭐ chốt chặn hằng số 72h (CF-M08-1) |
| `MF08-TC-009` | `mapper/DerivedFieldTest.java` | `[ ]` | | |
| `MF08-TC-010` | `service/FoodSampleDisposeTest.java` | `[ ]` | | |
| `MF08-TC-011` | `service/FoodSampleDisposeTest.java` | `[ ]` | | |
| `MF08-TC-012` | `service/FoodSampleDisposeTest.java` | `[ ]` | | ⚠️ blocked bởi L11 (FE thiếu `disposalWitness`) |
| `MF08-TC-013` | `service/ActorIdentityTest.java` | `[ ]` | | |
| `MF08-TC-014` | `service/SampleHandOverTest.java` | `[ ]` | | |
| `MF08-TC-015` | `service/SampleHandOverTest.java` | `[ ]` | | |
| `MF08-TC-016` | `domain/FoodSampleStatusTransitionTest.java` | `[ ]` | | cân nhắc tách FSM sang `common` nếu MF11 dùng chung |
| `MF08-TC-017` | `dto/EnumContractTest.java` | `[ ]` | | chuyển sang test của `common/enums` khi Nam dựng xong (D6) |
| `MF08-TC-018` | `dto/DateFormatContractTest.java` | `[ ]` | | dùng lại được cho 12 module (D4) |
| `MF08-TC-019` | `service/SampleAttachmentServiceTest.java` | `[ ]` | | thêm ArchUnit rule cấm `transferTo` |
| `MF08-TC-020` | `dto/PublicIdContractTest.java` | `[ ]` | | |
| `MF08-TC-SEC-001` | `security/SampleRbacIT.java` | `[ ]` | | ⭐ rủi ro số 1 — chain of custody (BR-113) |
| `MF08-TC-SEC-002` | `security/SampleRbacIT.java` | `[ ]` | | |
| `MF08-TC-SEC-003` | `security/SampleTenantIsolationIT.java` | `[ ]` | | |
| `MF08-TC-SEC-004` | `security/SampleRbacIT.java` | `[ ]` | | |
| `MF08-TC-SEC-005` | `security/SampleUploadSecurityIT.java` | `[ ]` | | dùng lại được cho MF03/07/10/11 (D10) |
| `MF08-TC-SEC-006` | `security/SampleRbacIT.java` | `[ ]` | | |
| `MF08-TC-SEC-007` | `security/InfoLeakTest.java` | `[ ]` | | |
| `MF08-TC-SEC-008` | `contract/OpenApiContractIT.java` | ⏸️ D8 | | `@Disabled` cho tới khi Nam approve D8 |
| `MF08-TC-INT-001` | `integration/FoodSampleRegisterIT.java` | `[ ]` | | |
| `MF08-TC-INT-002` | `integration/SampleHandOverIT.java` | `[ ]` | | ⭐ nguyên tử — lõi của ADR-M08-1 |
| `MF08-TC-INT-003` | `integration/SampleHandOverIT.java` | `[ ]` | | đổi 1 dòng khi Huy cấp MSG mới (L12) |
| `MF08-TC-INT-004` | `integration/SampleHandOverIT.java` | `[ ]` | | |
| `MF08-TC-INT-005` | `integration/FoodSampleDisposeIT.java` | `[ ]` | | phụ thuộc event `IncidentClosed` của MF11 |
| `MF08-TC-INT-006` | `integration/DisposedSampleLinkIT.java` | `[ ]` | | |
| `MF08-TC-INT-007` | `integration/PagedContractIT.java` | `[ ]` | | dùng lại được cho 12 module (§2b) |
| `MF08-TC-INT-008` | `integration/PublicIdContractIT.java` | `[ ]` | | dùng lại được cho 12 module (D1) |
| `MF08-TC-INT-009` | `integration/ErrorEnvelopeIT.java` | `[ ]` | | ⚠️ chờ TDS §10 dịch sang tiếng Anh (F5) |
| `MF08-TC-INT-010` | `integration/ClientSuppliedIdIT.java` | `[ ]` | | |
| `MF08-TC-INT-011` | `integration/SemanticAuditRulesIT.java` | `[ ]` | | scenario `MAU_NGOAI_KHUNG_SU_CO` chờ MF11 |
| `MF08-TC-INT-012` | `integration/SemanticAuditRulesIT.java` | `[ ]` | | |
| `MF08-TC-INT-013` | `integration/SampleRegisterExportIT.java` | `[ ]` | | |
| `MF08-TC-INT-014` | `integration/RetentionSchedulerIT.java` | `[ ]` | | |
| `MF08-TC-INT-015` | `integration/SampleHandOverIT.java` | `[ ]` | | |
| `MF08-TC-INT-016` | `integration/FoodSampleRegisterIT.java` | `[ ]` | | |
| `MF08-TC-INT-017` | `integration/FoodSampleRegisterIT.java` | `[ ]` | | |

### 5.1 Red Gate Protocol (CASE 2.0 — GATE-2)

> ⭐ Gate **quan trọng nhất**. Trước khi implement, chạy toàn bộ suite với stub ném lỗi. **Mọi test PHẢI FAIL.** Test nào PASS ngay → **AP-AI-002 detected** → reject và viết lại.

**Stub cho Red Phase (Java — KHÔNG dùng TypeScript như template):**

```java
// Red Phase — implementation stub (PHẢI throw)
@Service
public class FoodSampleServiceImpl implements FoodSampleService {
    @Override public FoodSampleResponse register(RegisterSampleRequest req,
                                                 List<MultipartFile> photos, AuthUser actor) {
        throw new UnsupportedOperationException("Not implemented — Red Phase stub");
    }
    @Override public FoodSampleResponse dispose(String code, DisposeSampleRequest req, AuthUser actor) {
        throw new UnsupportedOperationException("Not implemented — Red Phase stub");
    }
    // linkDisposedToIncident / updateStorage / buildLabel — tương tự
}

@Service
public class SampleLabRequestServiceImpl implements SampleLabRequestService {
    @Override public SampleLabRequestResponse handOver(HandOverSamplesRequest req, ..., AuthUser actor) {
        throw new UnsupportedOperationException("Not implemented — Red Phase stub");
    }
}
```

**Red Gate Verification:**

| TC ID | Stub Result | Expected | Actual | Root Cause (nếu PASS bất thường) |
|-------|-------------|----------|--------|----------------------------------|
| `MF08-TC-001` | `throw UnsupportedOperationException` | 🔴 FAIL | ☐ FAIL ☐ PASS | ☐ Tautology ☐ Shared state ☐ Hallucinated import |
| `MF08-TC-007` | `throw` | 🔴 FAIL | ☐ FAIL ☐ PASS | ☐ *(rủi ro cao: test khẳng định "không chặn" — dễ xanh khi chưa có gì để chặn)* |
| `MF08-TC-008` | `throw` | 🔴 FAIL | ☐ FAIL ☐ PASS | ☐ *(rủi ro: assert `24` bằng chính hằng số của test)* |
| `MF08-TC-016` | FSM chưa tồn tại | 🔴 FAIL (compile) | ☐ FAIL ☐ PASS | ☐ Hallucinated contract |
| `MF08-TC-017` | `MealType` chưa có ở `common/enums` | 🔴 FAIL (compile) | ☐ FAIL ☐ PASS | ☐ Hallucinated contract |
| `MF08-TC-SEC-001` | controller chưa có `@PreAuthorize` | 🔴 FAIL | ☐ FAIL ☐ PASS | ☐ *(rủi ro: 404 do thiếu endpoint chứ không do guard)* |
| `MF08-TC-INT-002` | `throw` | 🔴 FAIL | ☐ FAIL ☐ PASS | |
| `MF08-TC-INT-012` | bảng chưa tạo | 🔴 FAIL (SQL) | ☐ FAIL ☐ PASS | ☐ *(rủi ro: 0 rows vì không có bảng)* |
| `MF08-TC-INT-014` | chưa có scheduler | 🔴 FAIL | ☐ FAIL ☐ PASS | ☐ *(rủi ro: assert "không có job nhiệt độ" xanh vì chưa có job nào)* |
| *(36 TC còn lại)* | `throw` / chưa có endpoint | 🔴 FAIL | ☐ | |

> ⚠️ **Ba loại "PASS giả" riêng của MF08 — phải phân biệt:**
> 1. **Test khẳng định điều KHÔNG xảy ra** (`TC-007` "không chặn", `TC-INT-014` "không có job nhiệt độ", `TC-INT-012` "không có cột dẫn xuất"). Với stub rỗng thì mệnh đề phủ định **đúng sẵn** → xanh giả. **Cách chặn:** mỗi test loại này phải có **tiền đề khẳng định** trước — assert mẫu **được lưu** trước khi assert "không bị chặn"; assert bảng **tồn tại** trước khi assert cột không tồn tại; assert **có đúng 1** bean `@Scheduled` trước khi assert không có job nhiệt độ.
> 2. **`SEC-*` PASS vì endpoint chưa tồn tại** (404 ≠ 403). → Assert **đúng mã 403** + `error.code = "SMP-005"`, không assert `isNot2xx()`.
> 3. **`TC-008` PASS bằng tautology** nếu test tính kỳ vọng bằng chính `RetentionPolicy.MIN_RETENTION_HOURS`. → Viết **giá trị tuyệt đối** `Instant.parse("2026-07-07T03:15:00Z")` trong assert, để nếu ai đổi hằng số về 72 thì test đỏ ngay.

**Red Gate Evidence:**
- Stub commit hash: `___`
- Tất cả FAIL? ☐ Yes → **GATE-2 PASS** (T2→T3) → tiếp tục implement
- Log: `target/red-gate-evidence.log` (sinh bằng `./mvnw test -Dsurefire.reportFormat=plain`)

---

## 6. Entry / Exit Criteria

### Entry Criteria (Điều kiện bắt đầu)

- [ ] **TDS-MF08_Backend.md** (`SFMS-MF08-IMP-001`) đã review chéo đủ 4 vai (Huy §2 · Mạnh §5+§9 · Minh §8+§9 · Thảo §13) và Nam final approve
- [ ] **Logic Issues §2 (L1–L13) đã confirm với Nam** — đặc biệt L1/L2/L3/L4 (TDS bị D1/D4/D6/D7 ghi đè) và L7–L11 (mock FE sai, TDS đúng theo `review.md` §F)
- [ ] Nền chung của Nam sẵn sàng: `common/domain/BaseEntity` (D9), `common/storage/StorageService` (D10), **`common/enums/MealType`** (D6), `common/dto/PagedResponse` + `ErrorResponse` (D2), `@RestControllerAdvice`, `code_sequences` (D1/D5)
- [ ] **Flyway V1** đã có 4 bảng MF08 (§5.2 TDS) + 8 CHECK constraint + `uq_smp_inspection_dish`; `V{n}__seed_mf08.sql` đã **quy đổi 24h / `amount`+`unit` / `disposal_witness` / ISO** (TDS §11.2)
- [ ] Ports sẵn sàng ở mức **stub contract** nếu module thật chưa xong: `InspectionGatePort` (MF07), `IncidentStatusPort` (MF11), `GeofenceQueryPort` (MF02)
- [ ] Testcontainers chạy được trên máy dev + CI (Docker daemon khả dụng)
- [ ] ⚠️ **Không chờ FE:** 5 blocker của `review.md` §F là việc của Minh, nhưng test backend **không** phụ thuộc FE — chúng gọi API trực tiếp. Đây chính là cách chứng minh gate BR-105/BR-203/BR-107/BR-109 chạy ở **server**, không phải ở client

### Exit Criteria (Điều kiện kết thúc — DoD)

- [ ] `./mvnw test` — **toàn bộ unit test xanh**, không `@Disabled` (trừ `MF08-TC-SEC-008` chờ D8)
- [ ] `./mvnw verify -Pintegration` — **toàn bộ integration test xanh** với Testcontainers PostgreSQL 16
- [ ] **Coverage ≥ 80% line** cho package `com.sfms.mf08` (JaCoCo) — vượt mức yêu cầu tối thiểu của `01_MVP_Backend_Implementation_Plan.md` §5 (≥1 happy + 1 error path cho module P1)
- [ ] **5/5 UC** của traceability §1.8 có ≥1 test case (bảng TDS-03b phủ hết)
- [ ] **12/12 BR** có test — trừ **BR-106** phủ gián tiếp qua `TC-020` (ghi rõ lý do ở TDS-03b) và **BR-113** phần "tự vận chuyển" chỉ kiểm được ở mức RBAC (`TC-SEC-001`) vì hành vi vật lý nằm ngoài hệ thống
- [ ] **10/10 luật `semantic-audit.ts`** chạm MF08 đã port (`MF08-TC-INT-011`)
- [ ] **Không hằng số `72`** nào gắn với hạn lưu trong `src/main` lẫn `src/test`:
  ```bash
  grep -rn "72" src/main/java/com/sfms/mf08/ | grep -i "retention\|hour\|deadline"   # Expected: no output
  ```
- [ ] Đối chiếu Swagger: 13 endpoint §9.1 chạy được, JSON khớp hợp đồng mock ở những field **không** bị D1–D10 ghi đè (Minh xác nhận — Team Plan §7)
- [ ] Sổ Mẫu số 5 xuất ra **đúng 12 cột** Phụ lục 2 (`MF08-TC-INT-013` xanh) — điều kiện nghiệm thu pháp lý

**Exit Criteria bổ sung — CASE 2.0:**

- [ ] **Red Gate (§5.1)** — mọi test FAIL với stub trước khi implement; evidence log đính kèm; **3 loại PASS giả** đã được kiểm riêng
- [ ] **Contract Existence** — mọi import trong test resolve:
  ```bash
  ./mvnw -q test-compile        # Expected: BUILD SUCCESS, không "cannot find symbol"
  ```
- [ ] **Props Isolation** — không shared mutable state:
  ```bash
  grep -rn "^\s*private final FoodSample \|^\s*private static FoodSample " src/test/java/com/sfms/mf08/
  # Expected: no output
  ```
- [ ] **Oracle Source** — 47/47 test case có trường `Oracle Source` trỏ BR/UC/MSG/Điều luật/D-decision cụ thể

**Exit Criteria bổ sung — D1–D10 (`00_Common_Conventions.md` §Checklist):**

- [ ] D1: `MF08-TC-INT-008` xanh — không UUID nào lộ ra JSON/path param
- [ ] D2: `MF08-TC-INT-009` xanh — envelope 3 field, `details` không null
- [ ] D4: `MF08-TC-018` xanh — không còn `dd/MM/yyyy` trong JSON, không còn `@JsonFormat`
- [ ] D5: `MF08-TC-INT-010` xanh — client-supplied id hoạt động cả 2 nhánh
- [ ] D6: `MF08-TC-017` xanh — `MealType` lấy từ `common/enums`, SCREAMING_SNAKE
- [ ] D7: `MF08-TC-INT-009` xanh — 9 mã dẫn xuất BR đã dịch sang tiếng Anh
- [ ] D9: `MF08-TC-INT-017` xanh — `created_by` do `AuditorAware` điền
- [ ] D10: `MF08-TC-019` + `MF08-TC-SEC-005` xanh — upload đi qua `StorageService`
- [ ] *(D8 — chỉ khi Nam approve)* `MF08-TC-SEC-008` bỏ `@Disabled` → xanh

### Suspension Criteria (Điều kiện tạm dừng)

- **`common/enums/MealType` chưa có** (D6) — tạm dừng `TC-017`, **không** tự khai enum trong module để test xanh
- **`common/storage/StorageService` / `BaseEntity` chưa có** (D9/D10) — tạm dừng `TC-019`, `TC-SEC-005`, `TC-INT-017`
- **MF07 / MF11 chưa có port** — chạy `TC-003`, `TC-INT-005`, `TC-INT-011` (scenario cửa sổ điều tra) ở mức **stub contract**, đánh dấu phần cross-module cho Phase 4 (`review_fix_plan.md`)
- **TDS §10 chưa dịch sang tiếng Anh** (F5) — `TC-INT-009` giữ 🔴, **không** nới assert để chấp nhận tiếng Việt
- Docker/Testcontainers không chạy trên CI — tạm dừng toàn bộ nhóm `IT`, giữ nguyên nhóm unit
- Phát hiện mâu thuẫn mới giữa `00_Common_Conventions.md` và TDS → dừng, bổ sung vào §2 (Logic Issues) trước khi viết tiếp, **không** tự quyết

---

## 7. Rollback Plan

> ⚠️ Template gốc dùng `npx prisma migrate reset` — **sai stack**. MF08 dùng **Flyway + Maven** (Team Plan §2c-2, `01_MVP_Backend_Implementation_Plan.md` §3).

```bash
# 1. Hoàn tác migration MF08 (dev only — KHÔNG chạy trên production)
./mvnw -pl app flyway:undo
#    hoặc, nếu undo không khả dụng (Flyway Community):
psql -h localhost -U sfms -d sfms -f backup_20260802.sql

# 2. Testcontainers không cần dọn — container bị hủy sau mỗi lần chạy suite

# 3. Hoàn tác code
git checkout -- src/main/java/com/sfms/mf08/ src/test/java/com/sfms/mf08/
git checkout -- src/main/resources/db/migration/V*__seed_mf08.sql

# 4. Xác minh DB sạch
psql -h localhost -U sfms -d sfms -c "\dt food_sample* sample_*"   # Expected: không còn bảng MF08
```

**Không rollback được (ghi rõ để không hứa sai):**
- **Mẫu vật lý đã bị hủy** (UC-030) — rollback DB không mang mẫu trở lại; nếu hủy nhầm mẫu của ca đang điều tra thì bằng chứng **mất vĩnh viễn** (TDS §12.1 xếp là điều kiện báo DPO)
- **Mẫu đã bàn giao cho phòng kiểm nghiệm/cơ quan chức năng** (`SENT_TO_LAB`) — mẫu đã rời khỏi trường; rollback chỉ xóa bản ghi, không đòi lại được hộp mẫu
- **File bằng chứng đã đẩy lên storage** — nếu `sfms.storage.provider=r2`, cần dọn thủ công theo prefix `samples/`

> ⚠️ **Không rollback bằng cách xóa dòng `food_samples`** — sổ Mẫu số 5 là hồ sơ pháp lý **append-only** (TDS §4.2/§12.2). Dữ liệu sai phải sửa bằng bản ghi đính chính + audit.

**Sau rollback:** giữ nguyên trạng thái 🔴 của các TC trong §5; ghi lý do vào §CHANGELOG, **không** xóa dòng cũ (Policy 4.4).

---

## 8. CASE 2.0 Anti-Pattern Detection (AI-Assisted TCs)

> Test case trong tài liệu này **có AI hỗ trợ** (§1.1) → checklist dưới đây **bắt buộc** cho reviewer (Thảo — Lead Test/QA).

| AP-ID | Anti-Pattern | Dấu hiệu trong TDD spec | Check | Gate chặn |
|-------|-------------|--------------------------|-------|-----------|
| AP-AI-001 | Unconstrained Generation | TC không reference BR/UC/MSG/Điều luật/D-decision nào ở `Oracle Source` | ☐ | G-0 |
| AP-AI-002 | Green-from-Birth | Test PASS với stub `throw` (§5.1) — **rủi ro cao ở `TC-007`, `TC-INT-012`, `TC-INT-014`, `SEC-*`**: xem 3 loại PASS giả | ☐ | G-2 ★ |
| AP-AI-003 | Implicit Decision | Test giả định quyết định không có trong TDS §3 / `00_Common_Conventions.md` — vd **tự dựng cơ chế cảnh báo nhiệt độ 30 phút / MSG-059** (BR-110 đã bỏ — CF-M08-3) | ☐ | G-1 |
| AP-AI-004 | Layer Violation | Test kiểm business logic ở controller thay vì service | ☐ | G-4 |
| AP-AI-005 | Hallucinated Contract | Test import class/method không tồn tại (`InspectionGatePort`, `IncidentStatusPort`, `GeofenceQueryPort`, `StorageService` phải khớp TDS §8) | ☐ | G-3 |
| **AP-AI-011** | **Stale 72h** *(riêng MF08 — TDS §17.4)* | Bất kỳ hằng số `72` gắn với hạn lưu trong test hoặc code | ☐ | G-1 |
| **AP-AI-012** | **Revived BR-215** *(riêng MF08)* | Test cho phép Service Caterers thu hồi/gửi lab, hoặc sinh trạng thái `RETRIEVAL_REQUESTED`/`PENDING_LAB_DELIVERY` | ☐ | G-1 |

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
| **F1** | **`semantic-audit.ts` còn 3 chỗ theo mốc cũ:** 2 luật mang tên `HAN_LUU_KHONG_DU_72H` / `HUY_MAU_TRUOC_HAN_72H` (dòng 88, 96) so ngưỡng **72 giờ**, và `MAU_NGOAI_KHUNG_SU_CO` (dòng 109–112) dùng khung `[-72h, +24h]`. BR-111 đã là **24h** (26/07/2026), BR-149 đã là cửa sổ **24h** (27/07/2026) | Chạy `npm run audit:data` trên seed đúng luật mới sẽ **báo sai** hàng loạt; hoặc tệ hơn: ai đó sửa seed cho khớp script → quay về 72h | **Thảo** (Lead Test) + **Minh** (chủ mock) |
| **F2** | **Mock FE phía bếp còn nguyên quy trình BR-215 đã Deprecated**: khối `LabRequestTasks` (`service-caterers/samples/page.tsx:424-555`) cho bếp "Xác nhận đã thu hồi mẫu" và "Xác nhận đã gửi đơn vị xét nghiệm"; 2 màn bếp còn hiển thị **72h** (dòng 71, 187, 225-226, 292, 569) | `MF08-TC-SEC-001` cố tình **ngược** với mock — nếu không gỡ, FE gọi API sẽ nhận 403 và người dùng bếp thấy nút bấm chết | **Minh** (chủ FE) — `review_fix_plan.md` dòng 185 |
| **F3** | **UC-042 AF1 + MSG-059 không còn đường thực thi**: UC yêu cầu log nhiệt độ 2 lần/ngày và alarm sau 30 phút, nhưng **BR-110 đã bỏ cơ chế cảnh báo (11/07/2026)** và entity `FridgeTempLog` đã bị **xóa khỏi mock (26/07/2026)**. Traceability §1.8 vẫn liệt kê MSG-059 cho UC-042 | **Không viết test** cho phần này (sẽ là AP-AI-003). Một MSG trong traceability không có bất kỳ đường code nào — Huy cần quyết định gỡ hay giữ kèm ghi chú | **Huy** (Lead Traceability) |
| **F4** | **MSG-023 bị mượn sai ngữ nghĩa** cho "Incident ID không hợp lệ/đã đóng" (CF-M08-6). Nội dung gốc nói về *biên bản sự cố thiếu field* | `MF08-TC-INT-003` phải đặt **ngoại lệ** không assert nội dung message — điểm yếu duy nhất trong chuỗi assert của `TC-INT-009` | **Huy** — `review_fix_plan.md` F9 (cấp MSG mới) |
| **F5** | **TDS-MF08 §10 viết message của 9 mã dẫn xuất BR bằng tiếng Việt** (`SMP-030/105/107/109/111/112/113/203/212`) — trái **D7** (backend trả message **tiếng Anh** nguyên văn `error_message.md`) | `MF08-TC-INT-009` giữ 🔴 cho tới khi dịch xong; nếu code theo TDS hiện tại thì FE nhận message tiếng Việt lẫn tiếng Anh | **Mạnh** (chủ MF08) sửa TDS §10 |
| **F6** | **`MSG-008` / `MSG-009` (upload) không nằm trong §1.8** của MF08, nhưng MF08 **có** luồng upload bắt buộc (BR-107 ảnh bằng chứng, BR-212 biên bản giao nhận) | `MF08-TC-SEC-005` đang đánh dấu ⚠️; nếu Huy quyết định khác thì phải đổi mã trong test | **Huy** |
| **F7** | **D6 chưa có `MealType` cho ngữ cảnh MF08 đầy đủ**: bảng D6 chốt `BREAKFAST/LUNCH/SNACK`, nhưng MF08 còn 4 enum riêng (`SampleUnit`, `HandoverType`, `AuthorityType`, `AttachmentKind`) chưa được xếp vào `common/enums` hay để ở module | Nếu MF11 dùng lại `HandoverType`/`AuthorityType` (điều tra sự cố cũng chỉ định nơi nhận mẫu) thì sẽ có 2 bản khai — đúng bài toán 76 mismatch | **Nam** (sở hữu `common/enums/`) quyết định vị trí |
| **F8** | **TDS-MF08 §9 chưa ghi câu bắt buộc của D10**: *"lưu qua `common.storage.StorageService`, không tự triển khai I/O"* — D10 §"Việc phải làm" yêu cầu **mọi** module có upload (MF03, MF07, **MF08**, MF10, MF11) ghi dòng này ở §9 | Không có dòng đó, người implement dễ tự viết I/O như §11.1 TDS đang mô tả (`./data/uploads`) | **Mạnh** sửa TDS §9 |
| **F9** | **UC-069 Postconditions (UC file) vẫn ghi "72-hour cold storage timer"** dù BR-111 đã sửa 24h | Oracle của `MF08-TC-INT-001` phải trỏ BR chứ không trỏ UC file — đã ghi rõ trong test, nhưng UC file cần đồng bộ để không ai trích nhầm | **Huy** đồng bộ UC file |

---

*TDD Spec v1.0 cho MF08 — tuân thủ `PHASE-4_Test-Spec.md` (ISO/IEC/IEEE 29119-3:2021) + CASE 2.0 Red Gate & Anti-Pattern Detection.*
*Ví dụ code viết bằng **Java 25 / JUnit 5 / Testcontainers / Flyway** — đã dịch từ mẫu TypeScript/Prisma của template theo ràng buộc stack `00_Team_Work_Assignment_Plan.md` §2c-2.*
*Oracle của mọi test case trỏ **BR / UC / MSG / Điều luật QĐ 1246 / quyết định D1–D10** — không dùng giả định AI (Team Plan §2a).*
