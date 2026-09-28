# TEST-DRIVEN DEVELOPMENT SPECIFICATION
# MF04 — Supplier & Partner Management

**Document ID:** `SFMS-MF04-TDD-001`
**Version:** `1.1`
**Date:** `2026-08-02`
**Status:** **`Approved`** — 2026-08-02 (toàn bộ 13 Logic Issue §2 đã chốt; xem §2.1)
**Standard:** ISO/IEC/IEEE 29119-3:2021 — Software Testing Part 3: Test Documentation
**Author:** Nguyễn Đức Mạnh (ManhNDHE160692) — Lead Backend & DB Reviewer, chủ MF04
**Reviewed by:** `[ ] Nguyễn Phương Thảo — Lead Test/QA (Pending)` · `[ ] Bùi Quang Huy — Traceability (Pending — 3 mục §2.2)` · `[ ] Trần Tuấn Minh — FE↔BE contract (Pending — 5 mục §9.3 TDS)`
**DPO Sign-off:** `[ ] Pending` — **bắt buộc**: test chạm dữ liệu **sinh trắc học khuôn mặt** + **số CCCD** (PDPD, dữ liệu cá nhân nhạy cảm)
**Approved by:** Nguyễn Đức Mạnh (chủ MF04) — 2026-08-02, cùng đợt với `TDS-MF04_Backend.md` v1.1. Theo tiền lệ MF02 (Approved 2026-07-31 khi D8 còn pending): mục treo ở §2.2 **không chặn Approve** vì không mục nào đổi oracle của test đã viết.
**Classification:** `Internal — Confidential`

**References:**
- `07_implement/MF04_Supplier_Partner_Management/TDS-MF04_Backend.md` (`SFMS-MF04-IMP-001`) — Technical Specification
- `07_implement/00_Common_Conventions.md` — **D1–D7 + D9/D10 Approved**, D8 chờ approve *(ưu tiên cao hơn ADR module — xem §2)*
- `07_implement/00_Team_Work_Assignment_Plan.md` §2a (bộ tài liệu), §2b (hợp đồng phân trang/search/filter), §7 (nghiệm thu)
- `07_implement/01_MVP_Backend_Implementation_Plan.md` §3 (stack, nền chung), §5 (Definition of Done)
- `07_implement/review.md` §A (mismatch xuyên module) + **§E (9 mismatch riêng MF04)**
- `07_implement/review_fix_plan.md` Phase 0 / T2c / dòng 278 (*"thêm integration test RBAC"* cho rủi ro đa-tenant)
- `02_Requirements/02_UseCases/MF04_Supplier_Partner_Management.md` — 25 UC
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` **§1.4** — nguồn UC↔BR↔MSG chuẩn
- `02_Requirements/00_BusinessRules/business_rules.md` §5 (BR-040…BR-060) · `error_message.md`
- `03_Design/08_FrontEnd/Web/scripts/semantic-audit.ts` — 5 luật chạm MF04 (dòng 254–258, 310–320, 321–328, 341–343, 347–358)
- `03_Design/02_ADR/ADR-000.md` — stub Face Recognition Service, `./data/uploads`
- Luật An toàn thực phẩm 2010 **Điều 37** — căn cứ pháp lý ngưỡng BR-047 (180/30 ngày)

> **Quy ước TDD:** Tài liệu này mô tả test cases **TRƯỚC** khi viết production code.
> Thứ tự bắt buộc: viết test (`*Test.java`) → chạy → xác nhận **FAIL 🔴** → implement → **PASS 🟢** → refactor 🔵.
> Không mark ✅ nếu `mvn test` chưa xanh.
> **Stack test (KHÔNG dùng Jest/Prisma — sai stack):** JUnit 5 · AssertJ · Mockito · Spring Boot Test (`@WebMvcTest`/`@SpringBootTest`) · **Testcontainers PostgreSQL 16** · Flyway · RestAssured (tùy chọn cho §4 INT).
> **Test data:** SYNTHETIC, seed từ `mock-data.ts` → `V{n}__seed_mf04.sql`. **Không dùng PII thật**; ảnh khuôn mặt test là ảnh sinh tổng hợp, không phải ảnh người thật.

---

## CHANGELOG

> **Policy 4.4 — Immutable History:** Không bao giờ xóa thông tin cũ.

| Ngày | Người thực hiện | Nội dung thay đổi |
|------|-----------------|-------------------|
| 2026-08-02 | Bùi Quang Huy | **Cập nhật §2.2 theo 5 mục treo đã được cấp (P2/P3/P4/P6/P7).** Chỉ **P4 ảnh hưởng test**: BR-060 nay là **mã hóa AES-256**, không còn là GPS ⇒ Exit Criteria **bỏ "BR-060 test ở mức port GPS"**, thay bằng *assert ảnh khuôn mặt không chạm đĩa/DB và chỉ `faceId` được lưu* — trùng khớp **M04-D11** vốn đã chốt, nên **không phải viết thêm test case nào**, chỉ đổi câu chữ Exit Criteria. P2/P3/P6/P7 không đụng oracle: test vẫn assert theo `error.code` (P2), `TC-010` giữ nguyên oracle nay có BR-041 chống lưng thay vì chỉ C5 (P6), `FX-005` giữ nguyên giá trị vì hai bản ATTP cùng hạn `2027-03-15` (P7). **P5 giữ nguyên** hướng "cố ý không viết test". |
| 2026-08-02 | Bùi Quang Huy | **Sửa cơ học 3 chỗ lệch với `00_Common_Conventions.md` §0-BIS và hạ tầng thật — KHÔNG đụng oracle, quyết định hay test case nào của Mạnh.** (1) **Package `com.sfms.partner` → `com.sfms.mf04`** (7 chỗ): §0-BIS chốt khuôn `com.sfms.mf<NN>`, ngoại lệ duy nhất là `com.sfms.iam` (MF01, đặt trước quy ước). Ảnh hưởng cả dòng Coverage ở Exit Criteria. (2) **Lệnh build**: repo **không có Maven wrapper** (`ls 04_Development/Backend/` → chỉ `pom.xml`) và **`pom.xml` không khai profile nào** ⇒ `./mvnw` và `-Pintegration` đều không chạy được. Đổi sang `mvn` (từ thư mục Backend, hoặc `mvn -f 04_Development/Backend/pom.xml` theo §0-BIS); `mvn verify` đã chạy failsafe sẵn nên không cần profile. Bỏ `-pl app` — dự án single-module, không có module `app`. (3) **Thêm Exit Criteria — Bước 2b (`npm run gen:api`)** theo `02_Module_Completion_Workflow.md` §2: sinh `schema.d.ts` từ `/v3/api-docs` và kiểm mọi endpoint danh sách trỏ `PagedResponse{TênDto}` với `items` đúng kiểu. Lý do: `@Schema(name = "PagedResponse")` từng làm **cả 11 endpoint danh sách** của MF01–MF03 mất kiểu `items` — đã sửa ở `dev`, ghi lại đây để MF04 không tái phát. Kèm nhắc D11 (`oneOf` khi response đổi shape theo vai trò). **Còn treo cho Mạnh quyết** (không sửa hộ vì đụng schema + thêm endpoint): `file_key` §5.2 đang ghi *"giá trị `StorageService.store()` trả về"* — mà `store()` trả **URL công khai** và key theo khuôn đoán được (`partner-documents/DOC-GF-05.pdf`) ⇒ bucket public + key đoán được = liệt kê được giấy phép/chứng nhận ATTP của mọi đối tác. Xem `00_Common_Conventions.md` **D10-BIS** mục 2. |
| 2026-08-02 | Nguyễn Đức Mạnh | **Đóng toàn bộ Logic Issues §2 (L1–L13) + 4/7 mục §8.1 (F1/F3/F4/F5), chuyển Status → Approved.** 15 quyết định `M04-D01…D15` ghi ở **TDS §2.5**, tóm tắt tại **§2.1** dưới đây. Điểm đáng chú ý: (1) **L11 hết blocker** — MF01 đã merge `user_accounts.partner_id` + JWT claim `partnerId` + `AccountCascadeHandler`, nên `INT-003/004` và `SEC-001/003` chạy được ⇒ Entry Criteria §6 và Suspension Criteria cập nhật. (2) **Đổi oracle của `SEC-001`/`SEC-008`: cross-partner/cross-school → 404 `SUP-404`, KHÔNG 403** (M04-D10 — theo tiền lệ đã chạy ở MF01 `StaffService.requireInScope()` và MF02 §2 L7). (3) `INT-010` đổi mã kỳ vọng **`SUP-409` → `SUP-139`/MSG-139** (D5 tách khỏi lỗi nghiệp vụ, D7). (4) `TC-002` có oracle mã lỗi **`SUP-138`/MSG-138**; `SEC-007` có **`SUP-141`/MSG-141** cho 429 — hai mã cuối đóng 2 test trước đây assert HTTP status mà không có `error.code`. (5) `INT-014` bỏ nhãn *"mã cục bộ"* cho `SUP-048/050/404`: từ `error_message.md` v1.5 chúng đã là MSG-113/117/137 ⇒ assert byte-for-byte được. (6) **F1 đóng bằng làm rõ ngữ nghĩa, không nới `ck_dp_face`**: `face_id` do FRS cấp nên `PENDING_REGISTRATION` vẫn `face_id IS NULL` — tiền đề của F1 sai. **45/45 test case sẵn sàng implement** (trừ `SEC-009` giữ `@Disabled` chờ Nam approve D8). |
| 2026-08-02 | Nguyễn Đức Mạnh | Khởi tạo — TDD spec cho MF04 (25 UC, 8 BR, 11 MSG). **13 Logic Issue (L1–L13)** encode hành vi đã sửa theo `00_Common_Conventions.md` D1–D10 + `review.md` §E, **không** theo bản TDS-MF04 v1.0 ở những điểm bị ghi đè. **45 test case**: 19 unit · 9 security · 17 integration — trong đó 5 luật `semantic-audit.ts` chạm MF04 được port (`MF04-TC-INT-015`). |

---

## MỤC LỤC

1. [Thông tin Module](#1-thông-tin-module) · 2. [Logic Issues Resolved](#2-logic-issues-resolved) · 3. [Test Design Specification](#3-test-design-specification-tds) · 4. [Test Case Specification](#4-test-case-specification) · 5. [Red-Green-Refactor Tracker](#5-red-green-refactor-tracker) · 6. [Entry / Exit Criteria](#6-entry--exit-criteria) · 7. [Rollback Plan](#7-rollback-plan) · 8. [CASE 2.0 Anti-Pattern Detection](#8-case-20-anti-pattern-detection-ai-assisted-tcs)

---

## 1. Thông tin Module

| Field | Value |
|-------|-------|
| **Feature / Gap ID** | `MF04` — Supplier & Partner Management (25 UC theo traceability §1.4) |
| **Module** | `com.sfms.mf04` — Bounded Context: **Partner Onboarding & Compliance** |
| **Spec gốc** | `SFMS-MF04-IMP-001` (TDS-MF04_Backend.md) + `00_Common_Conventions.md` D1–D10 |
| **Priority** | 🟠 **P1** — trục xương sống (`00_Team_Work_Assignment_Plan.md` §3: *"NCC Approved + lô cho chuỗi truy xuất"*) |
| **Sprint** | Tuần 2–3 theo `01_MVP_Backend_Implementation_Plan.md` §4 (sau nút cổ chai MF01/MF02 của Nam) |
| **Milestone** | Cổng cuối Tuần 2 — *"mỗi module P0/P1 CRUD được qua Swagger; trục ID thông giữa các module"* |
| **Data Classification** | **`Restricted`** — sinh trắc học khuôn mặt + số CCCD (nhạy cảm hơn MF02/MF03); hồ sơ doanh nghiệp = `Confidential` |
| **Compliance Scope** | **PDPD** (NĐ 13/2023 — sinh trắc học là dữ liệu cá nhân nhạy cảm) · **Luật ATTP 2010 Điều 37** (căn cứ ngưỡng BR-047) |
| **Upstream Dependencies** | **MF01** (JWT `AuthUser{userId, role, schoolId, partnerId}` — ⚠️ `partnerId` là **blocker**, xem L11; `common/domain/BaseEntity`; `common/storage/StorageService`; `common/enums/`) · **MF02** (`schools`) |
| **Downstream Consumers** | **MF06** (BR-070 chặn nhận hàng), **MF07/MF11** (`assigneeSupplierId`), **MF13** (`GET /compliance-report` — module `supplier-certificates`) |

### 1.1 AI Generation Context (CASE 2.0)

| Field | Value |
|-------|-------|
| **AI Assisted?** | `Yes` |
| **Constraint Source** | `TDS-MF04_Backend.md` §17.1 (C1–C7) + §10 (bảng mã lỗi) + §16 (auth matrix); `00_Common_Conventions.md` D1–D10; `00_Team_Work_Assignment_Plan.md` §2b |
| **Constraints Injected** | C1 PagedResponse 5 field + `SEARCH_FIELDS` cố định · C2 `partnerId`/`schoolId` từ JWT · C3 trạng thái tài liệu dẫn xuất theo BR-047 · C4 transaction duyệt 3 bảng + reason bắt buộc · C5 CCCD mã hóa + chỉ lưu `faceId` · C6 `partners` là nguồn sự thật duy nhất · C7 whitelist upload; **cộng thêm** D1 (id = mã nghiệp vụ), D2 (envelope lỗi), D4 (ISO), D5 (client-supplied id), D6 (enum chung), D10 (StorageService) |
| **Model** | `claude-opus-5` (theo `00_Team_Work_Assignment_Plan.md` §2c-4: TestSpec dùng Opus vì oracle phải khớp BR/MSG) |
| **Trust Level** | `T2 → T3 (pending Red Gate §5.1)` |

> **Oracle policy (Team Plan §2a):** *"mọi expected value PHẢI trích từ BR/UC/MSG — không dùng giả định AI."* Mỗi test case dưới đây có trường **Oracle Source** trỏ đúng mã BR/UC/MSG/D-decision. Test case nào không trỏ được nguồn → **không viết**, ghi vào §8 thay vì bịa kỳ vọng.

---

## 2. Logic Issues Resolved

> **Bắt buộc điền trước khi viết test.** Test encode hành vi **đã sửa**, không phải hành vi trong spec gốc.
> Nguồn sai lệch: `00_Common_Conventions.md` (thứ tự ưu tiên: **file quy ước chung > ADR module trong TDS §3 > hợp đồng mock FE**) và `review.md` §E.
> ⚠️ **Bảy dòng đầu (L1–L7) là chỗ TDS-MF04 v1.0 bị GHI ĐÈ** — ai đọc TDS mà bỏ qua bảng này sẽ viết test sai hợp đồng.

| # | Spec gốc (sai / thiếu) | Thực tế (quy ước / schema / policy đã chốt) | Fix áp dụng trong test |
|---|------------------------|--------------------------------------------|------------------------|
| **L1** | TDS §3 **ADR-M04-1**: PK UUID + cột `code`, **path param dùng UUID**, `code` chỉ để hiển thị; §9.3.5 yêu cầu FE đổi `{p.id}` → `{p.code}` | **D1 (Approved 30/07)**: `id` công khai **= mã nghiệp vụ**; path param dùng `code`; FK trong JSON dùng `code` của bảng đích; UUID chỉ là PK nội bộ, **không lộ ra API**. `00_Common_Conventions.md` §D1 "Việc phải làm": *"MF04 — sửa ADR-M04-1: path param dùng `code` thay UUID; bỏ yêu cầu §9.3.5"* | Mọi test gọi `GET /api/v1/partners/SUP-001` (**không** UUID). Assert `response.id == "SUP-001"`, `partnerId == "SUP-001"`. **MF04-TC-INT-013** khóa chặn: không JSON nào chứa UUID ở field `id`/`*Id`. |
| **L2** | TDS §9.2 dùng chuỗi hiển thị: `?type=Raw%20Food%20Supplier`, `"type": "Raw Food Supplier"` — **mâu thuẫn với chính §5.1** (`enum PartnerType { RAW_FOOD_SUPPLIER, SERVICE_CATERER }`). `review.md` E1 = **blocker** | **D6**: enum dùng chung ở `common/enums/`, **SCREAMING_SNAKE trong JSON và DB**; FE giữ nhãn tiếng Việt ở `lib/labels.ts`. Phân biệt: **vai trò** = `"Service Caterers"` (số nhiều, BR-007) ≠ **loại đối tác** = `SERVICE_CATERER` (số ít) | Test filter dùng `?type=RAW_FOOD_SUPPLIER`; assert JSON trả `"type": "RAW_FOOD_SUPPLIER"`. **MF04-TC-018** kiểm serialize/deserialize enum + từ chối chuỗi hiển thị. |
| **L3** | TDS §6.5 + §9.2: `POST /delivery-persons/{id}/face` → nhảy **thẳng** `NOT_REGISTERED → REGISTERED`; §9.2 chỉ khai **2** giá trị `biometricStatus`. `review.md` E4 = **blocker** (seed `DP-03` đang ở `PENDING_REGISTRATION` → không có chỗ chứa) | **D6**: `BiometricStatus` chốt **3** giá trị + định nghĩa chuyển trạng thái: `POST /delivery-persons/{id}/face` → **`PENDING_REGISTRATION`**; **xác thực sinh trắc thành công → `REGISTERED`** (`review_fix_plan.md` dòng 139) | **MF04-TC-008** tách **2 bước**: (1) POST /face → `PENDING_REGISTRATION`; (2) FRS xác thực xong → `REGISTERED` + `MSG-038`. Ràng buộc `ck_dp_face` phải nới cho `PENDING_REGISTRATION` (xem Implementation Note). |
| **L4** | TDS §9.2 trả **cả hai** định dạng trong cùng một mục: `"appliedAt": "2026-07-28T02:15:00Z"` **và** `"appliedAt": "28/06/2026"`, `"issuedDate": "01/07/2026"`. `review.md` A4 gọi đích danh MF04 là *"1 TDS nói cả hai"* | **D4**: JSON **luôn ISO-8601** — ngày `"2026-07-29"` (`LocalDate`), ngày giờ `"2026-07-29T02:15:00Z"` (`Instant`). Bỏ `@JsonFormat("dd/MM/yyyy")`. **E5**: `expiry_date DATE **NULL**` (NULL = "Không thời hạn"), **BR-047 bỏ qua bản ghi NULL** | Mọi assert ngày dùng ISO. **MF04-TC-019** khóa chặn regex `\d{2}/\d{2}/\d{4}` **không** xuất hiện trong bất kỳ response nào. **MF04-TC-004** có case `expiryDate = null → VALID` và **không** phát MSG-053. |
| **L5** | TDS §10 dùng envelope `{ "error": { "code", "message" } }` — đúng hướng nhưng chưa chốt `details` | **D2**: `{ "error": { "code", "message", "details" } }`; `details` là **mảng, có thể rỗng, KHÔNG bao giờ `null`**; `message` **tiếng Anh nguyên văn** `error_message.md` (**D7**) | **MF04-TC-INT-014** assert đủ 3 field trên mọi mã lỗi MF04; `details` là array kể cả khi rỗng; `message` khớp **byte-for-byte** với `error_message.md`. |
| **L6** | TDS im lặng về việc client tự sinh `id` (chỉ MF05 CF-11 nêu) | **D5** áp cho **cả 13 module**: body có `id` → dùng làm `code`, trùng → **409 `SUP-409`**; vắng → server sinh theo `code_sequences`; **luôn** trả `id` trong 201 | **MF04-TC-INT-010** phủ 2 nhánh. Lưu ý FE hiện gửi `id: "DOC-GF-" + Date.now().slice(-4)` (mock `supplier/certificates`) và `id: "DP-" + …` (mock `supplier/delivery-staff`) → nhánh "client gửi id" là **luồng thật**, không phải giả định. |
| **L7** | TDS §10 có 4 mã cục bộ `SUP-048/050/409/404` viết **tiếng Anh** (đã đúng), nhưng chưa chốt cơ chế hiển thị | **D7**: backend trả `code` + `message` **tiếng Anh**; FE tra `lib/messages.ts` map `code` → nhãn tiếng Việt. `review_fix_plan.md` dòng 79 liệt kê `SUP-048/050/409/404` vào nhóm "mã cục bộ chờ Huy cấp MSG chính thức" | Test assert theo **`code`**, không assert chuỗi tiếng Việt. Nếu Huy cấp MSG chính thức, chỉ đổi bảng ánh xạ ở §4 — **không** phải viết lại test. |
| **L8** | TDS §9.2 `POST /partnership-applications` trả `"school": "TH Nguyễn Bỉnh Khiêm"` (chỉ tên) | **`review_fix_plan.md` dòng 141 (E7)**: response trả **cả `schoolId` lẫn `school`** để bảng FE vẫn hiện tên trường | **MF04-TC-INT-005** assert response có **đồng thời** `schoolId` (mã trường, D1) và `school` (tên hiển thị). |
| **L9** | TDS §5.2 lưu `file_key VARCHAR(255)` + §11.1 tự quản `SFMS_UPLOAD_DIR`; ADR-M04-5 mô tả đẩy ảnh sang FRS | **D10**: mọi module lưu file/ảnh **phải** inject `common/storage/StorageService`, **không** tự gọi `MultipartFile.transferTo()`/AWS SDK. ⚠️ Danh sách module ở D10 (MF03/07/08/10/11) **bỏ sót MF04** dù MF04 có **2 luồng upload** (tài liệu tuân thủ UC-101/102/091, ảnh khuôn mặt UC-068) | **MF04-TC-017** mock `StorageService` và assert service **gọi nó**, không chạm filesystem. Ghi vào §8 để Nam bổ sung MF04 vào danh sách D10. |
| **L10** | TDS §5.2 khai `created_at`/`updated_at` thủ công trên từng entity | **D9**: `created_by`/`updated_by` là **cột reserved** của `common/domain/BaseEntity`, tự điền qua `AuditorAware`; entity `extends BaseEntity` **không** tự khai lại | Entity MF04 `extends BaseEntity`. **MF04-TC-INT-001** assert `created_by` = email actor **mà service không set tay** (bằng chứng `AuditorAware` chạy đúng). |
| **L11** | TDS §11.1 ghi *"`partnerId` là field MỚI — cần Nam bổ sung vào JWT claim"* nhưng coi là prerequisite thông thường | **`review.md` B3 = blocker**: `user_accounts` §5.2 của MF01 **không có** `partner_id`; `review_fix_plan.md` dòng 109 yêu cầu thêm `partner_id UUID NULL` + JWT claim. **Không có nó thì ADR-M04-6 (cascade đình chỉ) không chạy được** | Đưa lên **Entry Criteria §6** dạng blocker. **MF04-TC-INT-003** sẽ FAIL 🔴 hợp lệ cho tới khi MF01 bổ sung — đây là RED thật, không phải lỗi test. |
| **L12** | TDS §9.1 có `GET /supplier-partnerships` nhưng §9.2 **không có schema** (`review.md` E8) | Bổ sung schema; `docStatus` là **dẫn xuất** từ `partner_documents` (ADR-M04-4), không phải cột | **MF04-TC-INT-011** phủ resource này trong hợp đồng phân trang; **MF04-TC-011** phủ tính dẫn xuất của `docStatus`. |
| **L13** | TDS §9.1 ghi *"path thật thêm tiền tố `/api/v1`"* | **D3**: chốt `baseURL = "/api/v1"` | Mọi URL trong test dùng `/api/v1/...`. |

### 2.1 Trạng thái chốt (2026-08-02) — 13/13 Logic Issue ĐÃ ĐÓNG

> Ký hiệu: ✅ = đã chốt, test encode hành vi đúng, **không còn chặn GREEN**. ⛔ = còn thiếu oracle/chưa chốt.
> Quyết định đầy đủ (căn cứ + tiền lệ) ở **`TDS-MF04_Backend.md` §2.5**, mã `M04-D01…D15`.

| # | Trạng thái | Chốt gì | Test case bị ảnh hưởng |
|---|---|---|---|
| **L1** | ✅ | **M04-D01** — ADR-M04-1 viết lại: `id` JSON **là** mã, path param dùng mã, không lộ UUID. Bỏ yêu cầu §9.3.5. | `INT-013` giữ nguyên kỳ vọng; §9.2 TDS đã hết ví dụ trả song song `id`+`code` |
| **L2** | ✅ | **M04-D02** — enum SCREAMING_SNAKE; `PartnerType`/`BiometricStatus`/`RoleKey` lấy từ `common/enums` (**đã có trong code**), enum riêng module khai tại `com.sfms.mf04` | `TC-018` — thêm assert `PartnerType` là **cùng một type** với `common/enums`, không phải bản sao trong module |
| **L3** | ✅ | **M04-D03** — 3 giá trị, 2 bước; `face_id` **chỉ có ở `REGISTERED`** ⇒ `ck_dp_face` **giữ nguyên công thức**, chỉ siết thêm `biometric_registered_at IS NULL` ở nhánh ≠ REGISTERED | `TC-008` — bỏ ghi chú *"cần sửa `ck_dp_face` trước"*; bước 1 assert `faceId == null` khi `PENDING_REGISTRATION` |
| **L4** | ✅ | **M04-D04** — TDS §9.2 đã bỏ 3 chỗ `dd/MM/yyyy` cuối cùng (`documents[].expiry`, `issuedDate`/`expiryDate`, `submittedAt`) | `TC-019`, `INT-013` giữ nguyên |
| **L5** | ✅ | **M04-D05** — envelope 3 khóa, `details` luôn mảng; `common/dto/ErrorResponse` đã hiện thực đúng | `INT-014` giữ nguyên + mở rộng bảng (xem L6/L7) |
| **L6** | ✅ | **M04-D06** — D5 dùng **`SUP-139`/MSG-139**, **không** dùng `SUP-409` (mã đó mang nghĩa nghiệp vụ MSG-118). Tiền lệ MF02 `SCH-409`/MSG-139 | `INT-010` — **đổi kỳ vọng `SUP-409` → `SUP-139`** |
| **L7** | ✅ | **M04-D09** — `SUP-048/050/409/404` **đã có** MSG chính thức từ `error_message.md` v1.5 (MSG-113/117/118/137) | `INT-014` — 4 dòng bỏ nhãn *(mã cục bộ)*, assert byte-for-byte |
| **L8** | ✅ | Giữ nguyên — response trả **cả** `schoolId` (mã) lẫn `school` (tên); TDS §9.2 đã bổ sung `schoolId` vào ví dụ 201 | `INT-005` giữ nguyên |
| **L9** | ✅ | **M04-D11** — tài liệu lưu qua `StorageService` key `partner-documents/<code>.<ext>`; **ảnh khuôn mặt KHÔNG qua StorageService** (đẩy thẳng FRS, PDPD) ⇒ MF04 có **1** luồng lưu trữ, không phải 2 | `TC-017` giữ nguyên; `SEC-004` bước 4 đúng khuôn key |
| **L10** | ✅ | **M04-D12** — entity `extends BaseEntity`; DDL giữ 4 cột audit; migration `ALTER partners` phải **thêm** `created_by`/`updated_by` (stub V1 thiếu) | `INT-001` giữ nguyên |
| **L11** | ✅ **HẾT BLOCKER** | **M04-D13** — MF01 **đã merge**: `user_accounts.partner_id` (`V1` dòng 141), `JwtService.CLAIM_PARTNER_ID`, `AuthUser.partnerId`, `AccountCascadeHandler.onPartnerSuspended()` | `INT-003`, `INT-004`, `SEC-001`, `SEC-003` — **bỏ trạng thái blocked**; `INT-003` publish đúng type `IamEvents.PartnerSuspended(partnerCode, occurredAt)` |
| **L12** | ✅ | Giữ nguyên + **M04-D04**: `startDate`←`since`, `endDate`←cột `until` **mới thêm** (bản v1.0 trả `endDate` mà không có cột nào chứa) | `INT-011`, `TC-011` |
| **L13** | ✅ | D3 — mọi URL `/api/v1/...` | toàn bộ |

**Quyết định mới phát sinh khi rà (không có trong bảng L1–L13):**

| # | Chốt gì | Test case |
|---|---|---|
| **M04-D07** | Rate limit 429 → **`SUP-141`/MSG-141** (`error_message.md` v1.8, dùng chung mọi module; tiền lệ MF02 `SCH-014`) | `SEC-007` — trước chỉ assert *"429 Too Many Requests"*, nay có `error.code` để assert |
| **M04-D08** | Bean Validation 422 → **`SUP-138`/MSG-138** | `TC-002` — trước chỉ assert `ConstraintViolation` ở tầng DTO, nay có mã lỗi cho tầng API |
| **M04-D10** | **Cross-partner / cross-school → 404 `SUP-404`, không 403.** 403 `SUP-005` **chỉ** cho sai vai trò | `SEC-001` (bước 3–5), `SEC-008` (bước 3), `INT-014` — **đổi oracle**, xem ghi chú tại từng TC |
| **M04-D14** | Bảng `partners` **đã tồn tại** ở `V1__init_schema.sql` (stub cho FK `user_accounts.partner_id`) ⇒ migration MF04 **`ALTER`**, không `CREATE`; giữ tên cột `partner_type`, `code VARCHAR(32)`, `name VARCHAR(200)` của stub | Entry Criteria §6 (Flyway); `INT-016` vẫn assert **không** có cột `cert_status`/`doc_status` |
| **M04-D15** | D8 vẫn pending ⇒ `SEC-009` giữ `@Disabled` | `SEC-009` |

### 2.2 Việc của người khác — **không** tự quyết (chi tiết ở TDS §2.6)

| # | Việc | Ai | Có chặn test không? |
|---|---|---|---|
| ~~**P1**~~ | ✅ **XONG 2026-08-02** — MF04 đã có trong danh sách D10 (`00_Common_Conventions.md`, commit `67dd1f0`) | Nam/Huy | — |
| ~~**P2**~~ | ✅ **ĐÓNG 2026-08-02** — §1.4 nay có MSG-013→`UC-101`, MSG-014→`UC-068`, MSG-007→`UC-158`. **MSG-025 cố ý không gắn UC** (lỗi hạ tầng dùng chung). Test không đổi (assert theo `error.code`). | Huy | — |
| ~~**P3**~~ | ✅ **ĐÓNG 2026-08-02** — BR-010 nay gồm `UC-067`, `UC-071` trong ma trận | Huy | — |
| ~~**P4**~~ | ✅ **ĐÓNG 2026-08-02** — `business_rules.md` BR-060 đổi thành **mã hóa AES-256**, bỏ câu GPS (không hiện thực được: nhân viên giao hàng không phải người dùng hệ thống; GPS nhận hàng đã có BR-028+BR-073). | Huy | ⚠️ **CÓ** — Exit Criteria đổi: **bỏ port GPS**, BR-060 test ở mức *ảnh không chạm đĩa/DB, chỉ lưu `faceId`* |
| **P5** | **F7 / CF-M04-4** — không có BR định nghĩa `performanceScore` | **Huy** + BA | ✅ **GIỮ NGUYÊN quyết định 2026-08-02**: backend chỉ lưu/trả giá trị, **không** hiện thực cảnh báo hiệu suất, **không** viết test. Bịa công thức = **AP-AI-003**. Mở lại khi BA cấp BR thật. |
| ~~**P6**~~ | ✅ **ĐÓNG 2026-08-02** — BR-041 nay liệt kê cả `Suspended` + ngữ nghĩa cascade. `TC-010` giữ nguyên oracle, nay có BR chống lưng thay vì chỉ C5. | Huy | ❌ Không |
| ~~**P7**~~ | ✅ **ĐÓNG 2026-08-02** — chốt **`ATTP-HN-2024-1182`** (bản `SUPPLIER_DOCUMENTS`, đủ 8 field). `FX-005` giữ nguyên giá trị đang dùng; cùng hạn `2027-03-15` nên lớp tương đương BR-047 không đổi. | Huy | ❌ Không |
| **P8** | Vị trí record `PartnerSuspended` (đang ở `com.sfms.iam.event.IamEvents`) | **Nam** | ❌ Không — `INT-003` publish đúng type MF01 đang nghe |
| **P9** | 5 mục sync mock FE (TDS §9.3) | **Minh** | ❌ Không |
| **P10** | Duyệt **D8** | **Nam** | ⚠️ `SEC-009` giữ `@Disabled` cho tới khi duyệt |

---

> **Ghi chú cho reviewer:** 9 mâu thuẫn **CF-M04-1…9** trong TDS §2.4 **vẫn còn hiệu lực** (chưa được D1–D10 gỡ) — chúng là mâu thuẫn giữa UC file / traceability / mock, không phải giữa các TDS. TestSpec này bám **traceability §1.4** làm chuẩn, và các mã ngoài §1.4 (MSG-013/014/007/025) được test theo đúng nội dung `error_message.md` nhưng đánh dấu ⚠️ *(ngoài §1.4 — chờ Huy)*.

---

## 3. Test Design Specification (TDS)

### TDS-01 — Scope / Phạm vi

```
MF04 (com.sfms.mf04) — các layer được kiểm thử:
├── Domain          DocumentStatusCalculator, PartnerStatus/BiometricStatus FSM   → unit, không deps
├── Service         PartnerApprovalService, ComplianceDocumentService,            → unit (Mockito)
│                   DeliveryPersonService, PartnershipApplicationService
├── Web/Controller  PartnerController, DeliveryPersonController, …                → @WebMvcTest (mock service)
├── Repository/Spec PartnerSpec, DocumentSpec, DeliveryPersonSpec (SEARCH_FIELDS) → @DataJpaTest + Testcontainers
├── Integration     API → DB → event, Flyway migration thật                       → @SpringBootTest + Testcontainers PG16
└── Contract        /v3/api-docs (D8 — chỉ chạy khi Nam approve D8)                → @SpringBootTest

NGOÀI phạm vi (thuộc module khác, chỉ mock qua port):
  - FaceRecognitionPort   → stub theo ADR-000 { match:true, faceId:"mock-uuid" }
  - UserAccountPort/MF01  → cascade khóa tài khoản (kiểm qua event, không gọi DB MF01)
  - StorageService/common → mock, assert có gọi (D10)
  - supplier-products / supplier-batches / deliveries / purchase-requests → MF06 (traceability §1.5)
```

### TDS-02 — Test Basis / Cơ sở Kiểm thử

| Source | Items Derived |
|--------|--------------|
| `traceability_matrix.md` **§1.4** | **25 UC** — nguồn chuẩn duy nhất cho cặp UC↔BR↔MSG. Không lấy UC/BR từ `Report 3.md` phụ lục. |
| `business_rules.md` §5 | **BR-040** (hồ sơ đầy đủ + School Admin duyệt), **BR-041** (Pending→Approved/Rejected; chỉ Approved mới giao hàng), **BR-046** (duy trì tài liệu hợp lệ), **BR-047** (**180 ngày** Expiring / **30 ngày** Urgent), **BR-048** (từ chối phải ghi lý do + thông báo), **BR-050** (CCCD duy nhất), **BR-051** (đăng ký/xóa sinh trắc học), **BR-060** (xác minh GPS — *nội dung canonical*, xem CF-M04-3) |
| `error_message.md` | Error: MSG-004, MSG-005, MSG-006, MSG-008, MSG-009 · Success: MSG-028, MSG-029, MSG-031, MSG-037, MSG-038 · Warning: MSG-053. ⚠️ Ngoài §1.4: MSG-007, MSG-013, MSG-014, MSG-025 |
| `00_Common_Conventions.md` | **D1** định danh · **D2** envelope lỗi · **D3** `/api/v1` · **D4** ISO-8601 · **D5** client-supplied id · **D6** enum chung · **D7** message tiếng Anh · **D9** BaseEntity · **D10** StorageService · *(D8 contract test — pending)* |
| `TDS-MF04_Backend.md` §17.1 | C1…C7 — ràng buộc kỹ thuật (paged shape, JWT isolation, derived status, transaction, PII, single source of truth, upload whitelist) |
| `00_Team_Work_Assignment_Plan.md` §2b | Hợp đồng phân trang: 5 field, `pageCount ≥ 1`, `page` clamp, tập rỗng → **200** không 404, `search` chỉ trên `SEARCH_FIELDS` |
| `semantic-audit.ts` | 5 luật chạm MF04: `MUA_HANG_SAU_KHI_GIAY_TO_HET_HAN`, `NHAN_HANG_SAU_KHI_CHUNG_NHAN_HET_HAN`, `PHIEU_GHI_SAI_TRANG_THAI_NCC`, `CAPA_GIAO_NCC_KHONG_TON_TAI`, `NCC_LO_KHAC_NCC_PHIEU_NHAN` |
| **Luật ATTP 2010 Điều 37** | Giấy chứng nhận ATTP hiệu lực **3 năm**, nộp hồ sơ gia hạn **trước 6 tháng** → căn cứ pháp lý của ngưỡng 180 ngày (BR-047) |
| **PDPD (NĐ 13/2023)** | Sinh trắc học = dữ liệu cá nhân **nhạy cảm** → mã hóa at-rest, che khi trả API, xóa được theo yêu cầu (UC-028) |

### TDS-03 — Test Conditions and Coverage Items

| Condition ID | Test Condition | Coverage Item | Test Cases |
|-------------|---------------|---------------|-----------|
| TC-COND-001 | CCCD nhân viên giao hàng duy nhất toàn hệ thống (BR-050) | `DeliveryPersonService.create()` · `ux_dp_nid_hash` | `MF04-TC-001`, `MF04-TC-INT-012` |
| TC-COND-002 | CCCD đúng định dạng 12 chữ số; SĐT/biển số đúng khuôn (BR-050, BR-012) | `CreateDeliveryPersonRequest` validation | `MF04-TC-002` |
| TC-COND-003 | CCCD mã hóa at-rest, API trả dạng che (PDPD, UC-110) | `DeliveryPersonMapper` · cột `national_id_enc` | `MF04-TC-003`, `MF04-TC-SEC-005` |
| TC-COND-004 | Trạng thái tài liệu tính đúng ngưỡng 180/30 ngày; NULL = không thời hạn (BR-047, L4) | `DocumentStatusCalculator.statusOf()` | `MF04-TC-004`, `MF04-TC-INT-009` |
| TC-COND-005 | Từ chối / đình chỉ bắt buộc có lý do (BR-048, ADR-M04-6) | `PartnerApprovalService.reject()/suspend()` · `ck_partner_*_reason` | `MF04-TC-005`, `MF04-TC-006`, `MF04-TC-INT-003` |
| TC-COND-006 | Đăng ký sinh trắc học qua 2 bước; FRS lỗi không đổi trạng thái (BR-051, **D6/L3**) | `DeliveryPersonService.registerFace()` | `MF04-TC-007`, `MF04-TC-008` |
| TC-COND-007 | Xóa sinh trắc học gọi FRS **trước**; FRS lỗi → giữ nguyên (BR-051, UC-028 AF2) | `DeliveryPersonService.deleteFace()` | `MF04-TC-009`, `MF04-TC-INT-011b` |
| TC-COND-008 | Vòng đời đối tác đúng FSM (BR-041 + SUSPENDED) | `PartnerStatus` transition guard | `MF04-TC-010` |
| TC-COND-009 | `certExpiry`/`certStatus`/`docStatus` là **dẫn xuất**, không lưu cột (ADR-M04-4, L12) | `PartnerMapper` · `information_schema` | `MF04-TC-011`, `MF04-TC-INT-016` |
| TC-COND-010 | Chặn upload / nộp đơn khi chứng nhận ATTP đã hết hạn (BR-046/048, UC-101 AF1, UC-152 AF1) | `ComplianceDocumentService.upload()` | `MF04-TC-012`, `MF04-TC-INT-002` |
| TC-COND-011 | Chứng nhận ATTP bắt buộc có `expiryDate` (BR-047, UC-101) | `ck_doc_cert_expiry` | `MF04-TC-013` |
| TC-COND-012 | Portal state đúng 3 nhánh theo UC-155/159 | `PartnershipApplicationService.portalState()` | `MF04-TC-014` |
| TC-COND-013 | Email đối tác duy nhất (MSG-006, BR-012) | `PartnerRegistrationService` · `PartnerProfileService` | `MF04-TC-015`, `MF04-TC-SEC-007` |
| TC-COND-014 | `taxId` đúng định dạng (MSG-007 ⚠️ ngoài §1.4) | `SubmitApplicationRequest` validation | `MF04-TC-016` |
| TC-COND-015 | Upload chỉ nhận PDF/JPG/PNG ≤10MB (ảnh mặt JPG/PNG ≤5MB), kiểm magic bytes, lưu qua `StorageService` (MSG-008/009, **D10/L9**) | `ComplianceDocumentService.upload()` | `MF04-TC-017`, `MF04-TC-SEC-004` |
| TC-COND-016 | Enum serialize SCREAMING_SNAKE, từ chối chuỗi hiển thị (**D6/L2**) | `PartnerType`, `BiometricStatus`, `DocumentType` | `MF04-TC-018` |
| TC-COND-017 | Mọi field ngày trong JSON là ISO-8601 (**D4/L4**) | Jackson config + DTO | `MF04-TC-019`, `MF04-TC-INT-013` |
| TC-COND-018 | Duyệt đơn cập nhật **đồng thời** 3 bảng trong 1 transaction (CF-M04-9, ADR-M04-2) | `PartnerApprovalService.approve()` | `MF04-TC-INT-001`, `MF04-TC-INT-002` |
| TC-COND-019 | Đình chỉ cascade khóa tài khoản `users.partnerId` (ADR-M04-6, ràng buộc **C5** data_model) | `PartnerApprovalService.suspend()` + event | `MF04-TC-INT-003`, `MF04-TC-INT-004` |
| TC-COND-020 | Nộp đơn: 1 đơn đang mở / (partner, school); nộp lại giữ mã đơn (UC-152/156 Precondition, UC-170/158) | `uq_app_open_per_partner_school` · `resubmit()` | `MF04-TC-INT-006`, `MF04-TC-INT-007` |
| TC-COND-021 | Cập nhật tài liệu tạo bản mới + giữ bản cũ (`superseded_by_document_id`, UC-091) | `ComplianceDocumentService.update()` | `MF04-TC-INT-008` |
| TC-COND-022 | Client gửi `id` → dùng làm `code`, trùng → 409 (**D5/L6**) | Mọi endpoint POST của MF04 | `MF04-TC-INT-010` |
| TC-COND-023 | Hợp đồng phân trang §2b trên **5 resource** MF04 | `PagedResponse<T>` | `MF04-TC-INT-011` |
| TC-COND-024 | `id` công khai = mã nghiệp vụ, không UUID (**D1/L1**) | Toàn bộ DTO | `MF04-TC-INT-013`, `MF04-TC-SEC-009` |
| TC-COND-025 | Envelope lỗi đủ 3 field, `details` không null, message tiếng Anh (**D2/D7/L5**) | `@RestControllerAdvice` | `MF04-TC-INT-014` |
| TC-COND-026 | NCC chỉ đọc/ghi dữ liệu của chính mình (ADR-M04-3, C2) — **rủi ro đa-tenant số 1** | `PartnerSpec` + `@PreAuthorize` | `MF04-TC-SEC-001`, `MF04-TC-SEC-003` |
| TC-COND-027 | RBAC 7 role × endpoint đúng §16 (BR-007) | `@PreAuthorize` | `MF04-TC-SEC-002`, `MF04-TC-SEC-006`, `MF04-TC-SEC-008` |
| TC-COND-028 | `partners` là nguồn sự thật duy nhất của trạng thái đối tác (ADR-M04-7, C6) | `PartnerLookupPort` + schema check | `MF04-TC-INT-015` (5 luật semantic-audit) |
| TC-COND-029 | *(D8 — pending)* Spec `/v3/api-docs` không lộ UUID, mọi 4xx trỏ `ErrorResponse`, enum khớp D6 | OpenAPI spec | `MF04-TC-SEC-009` |

### TDS-03b — Bản đồ phủ 25 UC (đối chiếu traceability §1.4)

> Bảng này để **Huy** kiểm nhanh: mọi UC ở §1.4 đều có ít nhất một test case. 12 UC nghiệp vụ có test riêng; 13 UC còn lại là **đọc/điều hướng** (`View List`, `View Detail`, `Track Status`, `Manage …`) — được phủ bằng hợp đồng phân trang §2b, ma trận RBAC §16 và test định danh D1, vì bản thân chúng không có nhánh nghiệp vụ nào ngoài "trả đúng dữ liệu, đúng người được xem".

| UC ID | Tên UC | Test case phủ |
|---|---|---|
| UC-071 | Register Supplier Account | `TC-015` (MSG-006), `TC-SEC-007` (rate limit + chống dò), `TC-017` (MSG-008/009) |
| UC-067 | Register Service Caterers Account | `TC-015`, `TC-SEC-007` |
| UC-152 | Submit Supplier Application | `TC-016` (MSG-007), `TC-INT-005`, `TC-INT-006` |
| UC-156 | Submit Catering Application | `TC-INT-005`, `TC-INT-006` *(cùng service, khác `PartnerType`)* |
| UC-169 | Track Supplier Application Status | `TC-014` (portalState `TRACKING`), `TC-INT-011` (paging `partnership-applications`), `TC-SEC-001` (chỉ thấy đơn của mình) |
| UC-157 | Track Catering Application Status | `TC-014`, `TC-INT-011`, `TC-SEC-001` |
| UC-170 | Update/Resubmit Supplier Application | `TC-INT-007` |
| UC-158 | Update/Resubmit Catering Application | `TC-INT-007` *(cùng endpoint)* |
| UC-155 | Manage Supplier Application | `TC-014` (đủ 3 nhánh Normal Flow bước 3) |
| UC-159 | Manage Catering Application | `TC-014` |
| UC-007 | Approval / Reject Supplier | `TC-005`, `TC-006`, `TC-010`, `TC-INT-001`, `TC-INT-002`, `TC-SEC-002`, `TC-SEC-008` |
| UC-004 | Approval / Reject Service Caterers | `TC-INT-001`, `TC-SEC-002` *(nhánh `SERVICE_CATERER`, FX-004)* |
| UC-019 | Create Info Delivery Person | `TC-001`, `TC-002`, `TC-003`, `TC-INT-012` |
| UC-068 | Register Face Data | `TC-007`, `TC-008`, `TC-SEC-005` |
| UC-028 | Delete Face Data | `TC-009`, `TC-INT-011b` |
| UC-167 | Manage Compliance Documents | `TC-004`, `TC-INT-009`, `TC-INT-011` (paging `supplier-documents`) |
| UC-101 | Upload Food Safety Certificates | `TC-012`, `TC-013`, `TC-017`, `TC-SEC-004` |
| UC-102 | Upload Product Origin Documents | `TC-012` *(case `PRODUCT_ORIGIN` được phép hạn quá khứ)*, `TC-017` |
| UC-091 | Update Compliance Documents | `TC-INT-008` |
| UC-049 | Manage Profile | `TC-015` (`updateMine` — MSG-006/AF2) |
| UC-110 | View Delivery Person List | `TC-003` (CCCD dạng che — Normal Flow bước 3), `TC-INT-011`, `TC-SEC-001` |
| UC-131 | View List Supplier | `TC-018` (lọc `?type=RAW_FOOD_SUPPLIER`), `TC-INT-011`, `TC-SEC-002` |
| UC-127 | View List Service Caterers | `TC-018` (lọc `?type=SERVICE_CATERER`), `TC-INT-011` |
| UC-148 | View Supplier Detail | `TC-011` (field dẫn xuất), `TC-INT-013` (path param = mã), `TC-SEC-002` |
| UC-108 | View Service Caterers Detail | `TC-011` (`siteInspectionScore` — field chỉ caterer có, FX-004), `TC-INT-013` |

> ⚠️ **UC-131/UC-148/UC-127/UC-108 có cột Business Rules để trống ở §1.4** — không có luật nghiệp vụ nào để đặt oracle ngoài "trả đúng dữ liệu, đúng quyền". Vì vậy chúng **không** có test case riêng; ai muốn thêm phải nêu được BR/MSG làm oracle, nếu không sẽ rơi vào **AP-AI-001** (§8).
> ⚠️ Phần "Performance Warning" mô tả trong UC-131/UC-148 **không được kiểm thử** — xem **F7** (§8.1): chưa có BR nào định nghĩa công thức `performanceScore`.

### TDS-04 — Test Techniques / Kỹ thuật Kiểm thử

| Technique (ISO 29119-4) | Applied To | Rationale |
|------------------------|------------|-----------|
| **Equivalence Partitioning** | `documentType` × `expiryDate` (có hạn / không hạn / đã hết hạn); role × endpoint | Miền đầu vào chia lớp rõ ràng; test 1 đại diện mỗi lớp thay vì tổ hợp đầy đủ |
| **Boundary Value Analysis** | Ngưỡng BR-047 (**180** và **30** ngày), kích thước file (**10MB**, **5MB**), CCCD (**12** chữ số) | Ngưỡng là chỗ lỗi off-by-one hay xảy ra nhất; BR-047 vừa bị FE hiểu sai (30 thay vì 180) → phải test cả 2 biên |
| **State Transition Testing** | `PartnerStatus` (5 trạng thái), `ApplicationStatus` (4), `BiometricStatus` (**3** — D6/L3) | Ba FSM có transition **không hợp lệ** cần chặn (vd `REJECTED → SUSPENDED`); mock từng để `SUSPENDED` không có đường vào |
| **Decision Table Testing** | `approve()` = f(trạng thái đơn, tình trạng tài liệu, trường của actor) | 3 điều kiện độc lập quyết định 1 kết quả — bảng quyết định gọn hơn viết rời |
| **Error Guessing** | Upload đổi đuôi file, `partnerId` giả trong body, JWT thiếu/hết hạn, enum sai case | Nhắm đúng lớp lỗi mà mock **không** chặn được (validate chỉ ở client) |
| **Concurrency / Race Testing** | 2 request `create delivery-person` cùng CCCD; 2 School Admin duyệt song song | Bất biến BR-050 và "1 đơn mở/(partner,school)" chỉ chắc chắn khi có ràng buộc DB, không phải check-then-act ở service |

### TDS-05 — Test Data Requirements

> Seed gốc: `mock-data.ts` (6 `PARTNERS`, 4 `SUPPLIER_DOCUMENTS`, 3 `DELIVERY_PERSONS`, 3 `SUPPLIER_PARTNERSHIPS`, 4 `PARTNERSHIP_APPLICATIONS`) → `V{n}__seed_mf04.sql`.
> ⚠️ Seed **phải bổ sung** `partner_id`/`school_id` mà mock thiếu (CF-M04-6/9) và chuyển ngày sang **ISO** (D4).

| Fixture ID | Type | Value / Logic | Mục đích |
|-----------|------|---------------|---------|
| `FX-001` | DB seed | `partners`: `SUP-001` GreenFarm, `type=RAW_FOOD_SUPPLIER`, `status=APPROVED`, `tax_id=0107712345` | Happy path, chủ sở hữu của mọi resource con |
| `FX-002` | DB seed | `partners`: `SUP-005` Tây Bắc, `status=PENDING`, tài liệu đều VALID, có đơn `APP-2026-05` PENDING | Duyệt hồ sơ hợp lệ (UC-007) |
| `FX-003` | DB seed | `partners`: `SUP-004` Đại Dương, `status=SUSPENDED`, `suspend_reason` có sẵn, chứng nhận ATTP **hết hạn `2026-06-22`** | Chặn duyệt/khôi phục (BR-046/048), cascade tài khoản |
| `FX-004` | DB seed | `partners`: `CAT-001` An Bình, `type=SERVICE_CATERER`, `site_inspection_score=92` | Nhánh Service Caterer (UC-004/108) — field chỉ caterer mới có |
| `FX-005` | DB seed | `partner_documents`: `DOC-GF-01` ATTP hạn `2027-03-15` (VALID) · `DOC-GF-02` GPKD **`expiry_date = NULL`** (không thời hạn) · `DOC-GF-04` VietGAP hạn `2026-07-28` (URGENT tại `TODAY`) | 3 lớp tương đương của BR-047 + case NULL (L4/E5) |
| `FX-006` | DB seed | `delivery_persons`: `DP-01` `REGISTERED` (`face_id` có) · `DP-03` **`PENDING_REGISTRATION`** (`face_id` NULL) | Chứng minh D6/L3 — trạng thái thứ 3 có chỗ chứa |
| `FX-007` | DB seed | `partnership_applications`: `APP-2026-01` `REJECTED` + `comment` (BR-048) · `APP-2026-04` `PENDING` | Nộp lại (UC-170/158) + chặn đơn trùng |
| `FX-008` | DB seed | `users` (MF01): `U-0006` ACTIVE `partner_id=SUP-001` · `U-0007` `partner_id=SUP-004` | Cascade đình chỉ (ADR-M04-6) — **phụ thuộc L11** |
| `FX-009` | JWT | `{ sub:"U-0006", role:"supplier", partnerId:"SUP-001" }` | Actor NCC — kiểm cách ly đa-tenant |
| `FX-010` | JWT | `{ sub:"U-0099", role:"supplier", partnerId:"SUP-003" }` | Actor NCC **khác** — attacker trong SEC-001 |
| `FX-011` | JWT | `{ sub:"U-0001", role:"school-admin", schoolId:"SCH-HN-0042" }` | School Admin trường A (duyệt hợp lệ) |
| `FX-012` | JWT | `{ sub:"U-0050", role:"school-admin", schoolId:"SCH-HN-0051" }` | School Admin trường B — cross-school trong SEC-008 |
| `FX-013` | file | `cert-valid.pdf` (200KB, magic `%PDF`) · `cert-oversize.pdf` (12MB) · `fake.pdf` (magic `MZ`) · `face.jpg` (300KB) · `face-oversize.jpg` (6MB) | Biên MSG-008/009 + Error Guessing (SEC-004) |
| `FX-014` | clock | `Clock.fixed("2026-07-28T00:00:00Z", UTC)` inject vào `DocumentStatusCalculator` | Ngưỡng BR-047 phải **tất định** — không dùng `LocalDate.now()` trực tiếp (test sẽ vỡ theo ngày chạy) |
| `FX-015` | stub | `FaceRecognitionPortStub`: `enroll()` → `"mock-uuid"` (ADR-000); biến thể `enrollThrows()`, `deleteThrows()` | UC-068 AF1 / UC-028 AF2 |
| `FX-016` | mock | `StorageService` (Mockito) — assert `store(key, file)` được gọi, không chạm đĩa | D10/L9 |

---

## 4. Test Case Specification

> **TC ID format:** `MF04-TC-[NNN]` (unit) · `MF04-TC-SEC-[NNN]` (security) · `MF04-TC-INT-[NNN]` (integration)
> **Severity:** CRITICAL / HIGH / MEDIUM / LOW
> **Status:** 🔴 Not written / 🟡 Written-failing / 🟢 Passing

### Props Isolation Boilerplate (CASE 2.0 — BẮT BUỘC)

> ⭐ **CASE 2.0 Rule:** Mỗi test PHẢI tạo fresh instance qua factory. Không shared mutable state giữa các test case. Đây là biện pháp chống **AP-AI-002 (Green-from-Birth)**.
> Bản Java tương đương pattern `makeEntity()` của template (template viết bằng TypeScript — **KHÔNG copy nguyên cú pháp đó**, sai stack theo Team Plan §2c-2).

```java
// ═══════════════════════════════════════════════════════════════
// CASE 2.0 — Props Isolation Pattern (Java / JUnit 5)
// Đặt ở src/test/java/com/sfms/partner/PartnerTestFactory.java
// Mỗi @Test gọi factory → object mới, KHÔNG dùng field static mutable
// ═══════════════════════════════════════════════════════════════
public final class PartnerTestFactory {

    /** Đồng hồ cố định — ngưỡng BR-047 phải tất định (FX-014). */
    public static final Clock FIXED_CLOCK =
        Clock.fixed(Instant.parse("2026-07-28T00:00:00Z"), ZoneOffset.UTC);
    public static final LocalDate TODAY = LocalDate.now(FIXED_CLOCK);   // 2026-07-28

    private PartnerTestFactory() {}

    /** Baseline hợp lệ — đồng bộ FX-001. Mỗi lời gọi trả instance MỚI. */
    public static Partner aPartner(Consumer<Partner> overrides) {
        Partner p = new Partner();
        p.setCode("SUP-001");                       // D1 — id công khai là mã nghiệp vụ
        p.setName("Cty CP Thực phẩm sạch GreenFarm");
        p.setType(PartnerType.RAW_FOOD_SUPPLIER);   // D6 — SCREAMING_SNAKE
        p.setStatus(PartnerStatus.APPROVED);
        p.setTaxId("0107712345");
        p.setAddress("KCN Phú Nghĩa, Chương Mỹ, Hà Nội");
        p.setContact("Lý Thái Sơn — 0913 222 333");
        p.setEmail("sonlt@greenfarm.vn");
        overrides.accept(p);
        return p;
    }

    /** FX-005 — tài liệu tuân thủ; expiryDate = null nghĩa là "Không thời hạn" (L4/E5). */
    public static PartnerDocument aDocument(Consumer<PartnerDocument> overrides) {
        PartnerDocument d = new PartnerDocument();
        d.setCode("DOC-GF-01");
        d.setPartnerId(UUID.randomUUID());
        d.setName("Giấy chứng nhận cơ sở đủ điều kiện ATTP");
        d.setDocumentType(DocumentType.FOOD_SAFETY_CERT);
        d.setNumber("ATTP-HN-2024-1182");
        d.setIssuedBy("Chi cục ATVSTP Hà Nội");
        d.setIssuedDate(LocalDate.parse("2024-03-15"));   // D4 — ISO
        d.setExpiryDate(LocalDate.parse("2027-03-15"));
        overrides.accept(d);
        return d;
    }

    /** FX-006 — nhân viên giao hàng. */
    public static DeliveryPerson aDeliveryPerson(Consumer<DeliveryPerson> overrides) {
        DeliveryPerson dp = new DeliveryPerson();
        dp.setCode("DP-01");
        dp.setPartnerId(UUID.randomUUID());
        dp.setFullName("Đỗ Văn Tám");
        dp.setPhone("0912345678");
        dp.setVehiclePlate("29C-123.45");
        dp.setBiometricStatus(BiometricStatus.NOT_REGISTERED);   // D6 — 3 giá trị
        overrides.accept(dp);
        return dp;
    }

    /** JWT actor — FX-009…FX-012. */
    public static AuthUser anActor(String role, String partnerCode, String schoolCode) {
        return new AuthUser("U-TEST", role, schoolCode, partnerCode);
    }
}
```

```java
// ❌ SAI — shared mutable state (AP-AI-002): partner bị test trước mutate
// private final Partner partner = PartnerTestFactory.aPartner(p -> {});

// ✅ ĐÚNG — fresh instance trong từng @Test
// var partner = PartnerTestFactory.aPartner(p -> p.setStatus(PartnerStatus.PENDING));
```

---

### UNIT TEST CASES

---

### MF04-TC-001 — CCCD nhân viên giao hàng phải duy nhất toàn hệ thống

**Severity:** `HIGH`
**Legal:** PDPD — định danh cá nhân
**Feature Under Test:** `DeliveryPersonService.create()`
**Test File:** `src/test/java/com/sfms/partner/service/DeliveryPersonServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-001`
**Oracle Source:** **BR-050** — *"Mã CMND/CCCD phải là duy nhất trong hệ thống"* · **UC-019 AF1** *"Duplicate National ID: … System displays an error and prompts for correction"* · mã lỗi `SUP-050` (TDS §10)

**Preconditions:**
- FX-006: `DP-01` đã tồn tại với `nationalIdHash = sha256("001085912345")`
- `DeliveryPersonRepository.existsByNationalIdHash()` được mock trả `true`

**Test Steps:**
1. Arrange: `var req = new CreateDeliveryPersonRequest("Nguyễn Văn X", "001085912345", "0912345678", "29C-123.45");`
2. Act: gọi `service.create(req, actorSup001)`
3. Assert: exception ném ra và **không** có lời gọi `repository.save()`

**Expected Result (PASS — hành vi đúng):**
- Ném `ConflictException` với `code = "SUP-050"`
- `verify(repository, never()).save(any())` — không ghi bản ghi rác
- Hash được tính **trước** khi query (không gửi CCCD thô xuống DB dạng plaintext)

**Expected Result (FAIL — dấu hiệu lỗi):**
- Tạo được người thứ 2 cùng CCCD → hai nhân viên khác nhau dùng chung định danh, xác thực cổng trường không phân biệt được ai

**Current Status:** 🔴 Not written
**Implementation Note:** Kiểm ở service để trả đúng `SUP-050`, nhưng **bắt buộc** giữ `UNIQUE ux_dp_nid_hash` ở DB — check-then-act ở service không chống được race (xem `MF04-TC-INT-012`).

---

### MF04-TC-002 — Validate định dạng CCCD / SĐT / biển số xe

**Severity:** `MEDIUM`
**Feature Under Test:** `CreateDeliveryPersonRequest` (Bean Validation)
**Test File:** `src/test/java/com/sfms/partner/dto/CreateDeliveryPersonRequestTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-002`
**Oracle Source:** **BR-050** (họ tên, CCCD, SĐT, biển số là trường bắt buộc) · **BR-012** (*"Số điện thoại phải theo định dạng hợp lệ"*) · regex lấy từ mock `supplier/delivery-staff/page.tsx:30-32` — hợp đồng FE hiện hành

**Preconditions:** Không cần DB.

**Test Steps (Boundary Value Analysis):**
1. Arrange: `Validator` chuẩn của Jakarta
2. Act + Assert theo bảng:

| Input | Giá trị | Kỳ vọng |
|---|---|---|
| `nationalId` | `"001099212345"` (12 số) | ✅ hợp lệ |
| `nationalId` | `"00109921234"` (11 số) | ❌ vi phạm |
| `nationalId` | `"0010992123456"` (13 số) | ❌ vi phạm |
| `phone` | `"0912345678"` / `"+84912345678"` | ✅ hợp lệ |
| `phone` | `"12345"` | ❌ vi phạm (BR-012) |
| `vehiclePlate` | `"29C-123.45"` | ✅ hợp lệ |
| `vehiclePlate` | `"XX-YY"` | ❌ vi phạm |
| `fullName` | `""` / `"   "` | ❌ vi phạm (@NotBlank) |

**Expected Result (PASS):** đúng 5 case ❌ sinh `ConstraintViolation`, 3 case ✅ không sinh vi phạm nào.
Gọi qua API (`POST /api/v1/delivery-persons`) thì 5 case ❌ trả **422** `error.code = "SUP-138"`, `error.message` = **MSG-138 nguyên văn** (*"Validation failed: one or more required fields are missing or invalid."*), `details` liệt kê **đúng field** vi phạm — **chốt 2026-08-02, M04-D08** (trước đây TC này không có mã lỗi để assert ở tầng API).
**Expected Result (FAIL):** biên 11/13 chữ số lọt qua → CCCD sai độ dài vào DB, `nationalIdHash` vẫn unique nên không có tầng nào chặn tiếp.

**Current Status:** 🔴 Not written

---

### MF04-TC-003 — CCCD mã hóa at-rest và luôn trả về dạng che

**Severity:** `CRITICAL`
**CWE:** `CWE-311 — Missing Encryption of Sensitive Data`
**Legal:** **PDPD (NĐ 13/2023)** — dữ liệu cá nhân nhạy cảm
**Feature Under Test:** `DeliveryPersonMapper.toResponse()` + `DeliveryPersonService.create()`
**Test File:** `src/test/java/com/sfms/partner/service/DeliveryPersonPiiTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-003`
**Oracle Source:** **UC-110** Normal Flow bước 3 — *"System displays each delivery person's name, **national ID (masked)**, phone number, biometric registration status"* · TDS §17.1 **C5** · ADR-M04-5

**Preconditions:** `EncryptionService` thật (pgcrypto binding) hoặc fake tương đương; FX-006.

**Test Steps:**
1. Arrange: `create(nationalId = "001099212345")`
2. Act: đọc entity vừa lưu **và** DTO trả về
3. Assert: 3 mệnh đề dưới

**Expected Result (PASS):**
- `response.nationalId()` == `"0010992xxxxx"` — giữ 7 ký tự đầu, che phần còn lại (đúng khuôn seed mock `"0010859xxxxx"`)
- `entity.getNationalIdEnc()` **không** chứa chuỗi `"001099212345"` khi decode UTF-8
- `entity.getNationalIdHash()` == SHA-256 của giá trị thô (đủ để enforce BR-050 mà không cần giải mã)

**Expected Result (FAIL):**
- DTO trả CCCD đầy đủ → lộ định danh cá nhân qua API cho bất kỳ ai đọc được danh sách nhân viên giao hàng

**Current Status:** 🔴 Not written
**Implementation Note:** Che ở **mapper**, không ở tầng controller — nếu che ở controller thì service khác gọi lại DTO sẽ lộ.

---

### MF04-TC-004 — Ngưỡng cảnh báo hạn tài liệu theo BR-047 (180 / 30 ngày)

**Severity:** `HIGH`
**Legal:** **Luật ATTP 2010 Điều 37** — giấy chứng nhận hiệu lực 3 năm, nộp gia hạn trước 6 tháng
**Feature Under Test:** `DocumentStatusCalculator.statusOf(PartnerDocument, LocalDate)`
**Test File:** `src/test/java/com/sfms/partner/domain/DocumentStatusCalculatorTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-004`
**Oracle Source:** **BR-047** nguyên văn — *"'Expiring Soon' khi … còn **dưới 6 tháng (180 ngày)**"*, *"nâng lên 'Urgent' khi còn **dưới 30 ngày**"* · **L4/E5** (NULL = không thời hạn) · ADR-M04-4

**Preconditions:** FX-014 `Clock` cố định tại `2026-07-28`. **Không** dùng `LocalDate.now()`.

**Test Steps (Boundary Value Analysis — @ParameterizedTest):**

| # | `expiryDate` | Khoảng cách | Kỳ vọng | Ghi chú biên |
|---|---|---|---|---|
| 1 | `null` | — | `VALID` | "Không thời hạn" (`DOC-GF-02`) — **E5/L4** |
| 2 | `2026-07-27` | −1 ngày | `EXPIRED` | quá hạn |
| 3 | `2026-07-28` | 0 ngày | `URGENT` | biên dưới của URGENT (hết hạn **hôm nay** chưa phải EXPIRED) |
| 4 | `2026-08-27` | +30 ngày | `URGENT` | **biên 30** — BR-047 "dưới 30 ngày" tính bao gồm mốc |
| 5 | `2026-08-28` | +31 ngày | `EXPIRING` | ngay ngoài biên URGENT |
| 6 | `2027-01-24` | +180 ngày | `EXPIRING` | **biên 180** |
| 7 | `2027-01-25` | +181 ngày | `VALID` | ngay ngoài biên EXPIRING |
| 8 | `2027-03-15` | +595 ngày | `VALID` | `DOC-GF-01` |

**Expected Result (PASS):** đủ 8 case đúng nhãn.
**Expected Result (FAIL):**
- Case 5–6 trả `VALID` → đang dùng ngưỡng **30 ngày cho cả hai** như mock FE (`supplier/certificates`), vi phạm BR-047 → NCC mất 5 tháng chuẩn bị hồ sơ gia hạn theo Điều 37
- Case 1 ném `DateTimeParseException` → chưa xử lý NULL, đúng lỗi E5 mà review chỉ ra

**Current Status:** 🔴 Not written
**Implementation Note:** `Clock` phải là **dependency inject**, không gọi tĩnh — nếu không test sẽ xanh/đỏ theo ngày chạy CI.

---

### MF04-TC-005 — Từ chối đối tác không kèm lý do bị chặn

**Severity:** `HIGH`
**Feature Under Test:** `PartnerApprovalService.reject()`
**Test File:** `src/test/java/com/sfms/partner/service/PartnerApprovalServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-005`
**Oracle Source:** **BR-048** — *"Khi từ chối, **lý do từ chối phải được ghi lại** trong hệ thống và gửi thông báo cho nhà cung cấp"* · `SUP-048` (TDS §10)

**Preconditions:** FX-002 (`SUP-005` PENDING), actor FX-011.

**Test Steps:** gọi `reject("SUP-005", reason, actor)` với `reason ∈ { null, "", "   " }`.

**Expected Result (PASS):**
- Cả 3 giá trị đều ném `ValidationException` `code = "SUP-048"`
- `partner.status` **vẫn** `PENDING`; `verify(eventPublisher, never()).publish(any(PartnerRejected.class))`

**Expected Result (FAIL):** đối tác chuyển `REJECTED` với `reject_reason = null` → email gửi đi không nêu lý do, NCC không biết phải sửa gì để nộp lại (UC-170 Normal Flow bước 2 dựa vào chính chuỗi này).

**Current Status:** 🔴 Not written

---

### MF04-TC-006 — Từ chối hợp lệ ghi lý do nguyên văn và phát sự kiện

**Severity:** `HIGH`
**Feature Under Test:** `PartnerApprovalService.reject()`
**Test File:** `.../PartnerApprovalServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-005`
**Oracle Source:** **BR-048** · **UC-007** Normal Flow bước 8 — *"System updates the status to 'Rejected' and sends a rejection notification email with the stated reason"*

**Test Steps:** `reject("SUP-005", "Giấy chứng nhận ATTP đính kèm đã hết hạn — bổ sung bản còn hiệu lực trước khi nộp lại.", actorSchoolAdmin)`

**Expected Result (PASS):**
- `partner.status == REJECTED`, `rejectReason` lưu **nguyên văn** (không cắt, không escape mất dấu tiếng Việt)
- Phát đúng **1** `PartnerRejected` chứa `reason`
- `application.status == REJECTED` và `application.comment == reason` (để UC-170/158 hiển thị lại)

**Expected Result (FAIL):** `comment` của đơn không được set → màn "Nộp lại" của NCC hiện `"—"`, mất thông tin cần sửa.

**Current Status:** 🔴 Not written

---

### MF04-TC-007 — Face Recognition Service lỗi thì KHÔNG đổi trạng thái sinh trắc học

**Severity:** `HIGH`
**Feature Under Test:** `DeliveryPersonService.registerFace()`
**Test File:** `src/test/java/com/sfms/partner/service/DeliveryPersonBiometricTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-006`
**Oracle Source:** **UC-068 AF1** — *"Biometric Quality Failure: Face capture fails quality validation. System displays MSG-014 and prompts for retry"* · **MSG-014** ⚠️ *(ngoài §1.4 — CF-M04-2)* · TDS §17.1 **C5**

**Preconditions:** FX-006 `DP-03` (`PENDING_REGISTRATION`), FX-015 `enrollThrows()`.

**Test Steps:**
1. Arrange: `FaceRecognitionPort.enroll()` ném `ExternalServiceException`
2. Act: `registerFace("DP-03", faceImage, actor)`
3. Assert: trạng thái không đổi

**Expected Result (PASS):**
- Ném `ExternalServiceException` `code = "SUP-014"`, HTTP 502
- `DP-03.biometricStatus` **vẫn** `PENDING_REGISTRATION`, `faceId` vẫn `null`
- `verify(repository, never()).save(argThat(d -> d.getBiometricStatus() == REGISTERED))`

**Expected Result (FAIL):** đặt `REGISTERED` trước khi FRS xác nhận → nhân viên được phân công chuyến giao nhưng cổng trường không nhận diện được (BR-051).

**Current Status:** 🔴 Not written

---

### MF04-TC-008 — Đăng ký sinh trắc học đi qua 2 bước (D6/L3)

**Severity:** `HIGH`
**Feature Under Test:** `DeliveryPersonService.registerFace()` + `confirmEnrollment()`
**Test File:** `.../DeliveryPersonBiometricTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-006`
**Oracle Source:** **D6** bảng enum — *"`BiometricStatus` chốt 3 giá trị, kèm định nghĩa chuyển trạng thái: `POST /delivery-persons/{id}/face` → `PENDING_REGISTRATION`; xác thực sinh trắc thành công → `REGISTERED`"* · `review_fix_plan.md` dòng 139 · **MSG-038** *"Delivery staff face biometrics registered successfully."* · **BR-051**

**Preconditions:** `DP-03` ở `NOT_REGISTERED`; FX-015 stub thành công.

**Test Steps (State Transition Testing):**
1. Act 1: `registerFace("DP-03", image, actor)` → assert bước trung gian
2. Act 2: FRS callback/xác thực xong → `confirmEnrollment("DP-03", faceId)`
3. Assert transition **không hợp lệ**: gọi `confirmEnrollment()` khi đang `NOT_REGISTERED` → từ chối

**Expected Result (PASS):**
- Sau Act 1: `biometricStatus == PENDING_REGISTRATION`, **`faceId == null`** và `biometricRegisteredAt == null` (faceId do FRS cấp, chưa có ở bước này — **chốt M04-D03**), **chưa** phát `FaceDataRegistered`
- Sau Act 2: `biometricStatus == REGISTERED`, `faceId != null`, `biometricRegisteredAt != null`, phát `FaceDataRegistered`, thông điệp `MSG-038`
- Nhánh FRS lỗi (`enrollThrows()` của FX-015): quay về `NOT_REGISTERED`, **502 `SUP-014`**, `faceId` vẫn `null`
- Transition `NOT_REGISTERED → REGISTERED` **trực tiếp** bị từ chối (`IllegalStateTransitionException`)

**Expected Result (FAIL):**
- Nhảy thẳng `NOT_REGISTERED → REGISTERED` (đúng như TDS-MF04 v1.0 §6.5 mô tả) → **giá trị `PENDING_REGISTRATION` không có đường vào**, seed `DP-03` trở thành trạng thái mồ côi — đúng lỗi `review.md` E4 nêu

**Current Status:** 🔴 Not written
**Implementation Note:** ✅ **Chốt 2026-08-02 (M04-D03) — F1 đóng, KHÔNG nới `ck_dp_face`.** Ghi chú cũ (giữ lại để truy vết) cho rằng ràng buộc *"cấm `PENDING_REGISTRATION` có `face_id`"* là lỗi phải sửa. Rà lại thì **tiền đề đó sai**: `faceId` là chuỗi tham chiếu **do FRS cấp khi enroll thành công** (ADR-M04-5) ⇒ ở `PENDING_REGISTRATION` (đã gửi ảnh, FRS **chưa** trả) thì **chưa tồn tại giá trị nào để lưu**. Công thức giữ nguyên, chỉ siết thêm `biometric_registered_at IS NULL` ở nhánh ≠ `REGISTERED` để không có bản ghi "đã đăng ký lúc X nhưng không có faceId". TDS §5.2 + §6.5 đã cập nhật; **test này chạy được trên schema hiện tại, không chờ migration nào**.

---

### MF04-TC-009 — Xóa dữ liệu khuôn mặt gọi FRS trước, DB sau

**Severity:** `CRITICAL`
**CWE:** `CWE-212 — Improper Removal of Sensitive Information`
**Legal:** **PDPD** — quyền xóa dữ liệu cá nhân
**Feature Under Test:** `DeliveryPersonService.deleteFace()`
**Test File:** `.../DeliveryPersonBiometricTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-007`
**Oracle Source:** **BR-051** — *"Dữ liệu sinh trắc học … có thể bị xóa bởi nhà cung cấp, dẫn đến mất quyền truy cập cổng"* · **UC-028 AF2** — *"Face Recognition Service Unavailable: … System displays MSG-014, **aborts the deletion**"* · UC-028 Other Information — *"Deletion of biometric data is **irreversible**"*

**Preconditions:** `DP-01` `REGISTERED`, `faceId = "mock-uuid"`.

**Test Steps:**
1. Case A (FRS lỗi): `FaceRecognitionPort.delete()` ném → assert trạng thái giữ nguyên
2. Case B (thành công): assert thứ tự gọi bằng `InOrder`

**Expected Result (PASS):**
- Case A: ném `SUP-014`; `DP-01` **vẫn** `REGISTERED`, `faceId` không đổi
- Case B: `InOrder` xác nhận `frsPort.delete()` chạy **trước** `repository.save()`; sau đó `biometricStatus == NOT_REGISTERED`, `faceId == null`, `biometricRegisteredAt == null`
- Audit ghi `actor + timestamp + deliveryPersonId`; payload sự kiện `FaceDataDeleted` **không** chứa `faceId`/ảnh (PDPD)

**Expected Result (FAIL):**
- Xóa DB trước rồi FRS lỗi → DB nói "chưa đăng ký" nhưng FRS vẫn giữ template khuôn mặt → **dữ liệu sinh trắc học tồn tại ngoài tầm kiểm soát**, không còn `faceId` để xóa lại

**Current Status:** 🔴 Not written

---

### MF04-TC-010 — Máy trạng thái vòng đời đối tác (BR-041 + SUSPENDED)

**Severity:** `HIGH`
**Feature Under Test:** `PartnerStatusTransition.assertAllowed(from, to)`
**Test File:** `src/test/java/com/sfms/partner/domain/PartnerStatusTransitionTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-008`
**Oracle Source:** **BR-041** — *"Pending (Chờ duyệt) → Approved (Đã duyệt) / Rejected (Từ chối)"* · **ADR-M04-6** (SUSPENDED, ràng buộc **C5** `data_model.puml`) · TDS §6.3

**Test Steps (State Transition Testing — bảng đầy đủ):**

| From → To | Hợp lệ? | Nguồn |
|---|---|---|
| `PENDING → APPROVED` | ✅ | BR-041, UC-007/004 |
| `PENDING → REJECTED` | ✅ | BR-041, BR-048 |
| `REJECTED → PENDING` | ✅ | UC-170/158 (nộp lại) |
| `APPROVED → SUSPENDED` | ✅ | ADR-M04-6, C5 |
| `SUSPENDED → APPROVED` | ✅ | "Khôi phục hợp tác" (mock detail screen) |
| `PENDING → SUSPENDED` | ❌ | chưa từng được duyệt thì không có gì để đình chỉ |
| `REJECTED → APPROVED` | ❌ | phải nộp lại (`REJECTED → PENDING → APPROVED`) |
| `REJECTED → SUSPENDED` | ❌ | vô nghĩa |
| `APPROVED → PENDING` | ❌ | không lùi về chờ duyệt |
| `SUSPENDED → REJECTED` | ❌ | hai quyết định khác bản chất |

**Expected Result (PASS):** 5 transition ✅ đi qua; 5 transition ❌ ném `IllegalStateTransitionException`.
**Expected Result (FAIL):** transition tùy ý → `PUT /partners/{id}` với `status` bất kỳ (đúng cách mock đang làm) sẽ đưa dữ liệu vào trạng thái không đạt được qua nghiệp vụ.

**Current Status:** 🔴 Not written

---

### MF04-TC-011 — `certStatus` / `certExpiry` / `docStatus` là dẫn xuất, không phải cột

**Severity:** `MEDIUM`
**Feature Under Test:** `PartnerMapper.toResponse()` · `SupplierPartnershipMapper.toResponse()`
**Test File:** `src/test/java/com/sfms/partner/mapper/DerivedFieldTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-009`
**Oracle Source:** **ADR-M04-4** · **ADR-M02-2** (*"không lưu field dẫn xuất — tính runtime"*) · `01_MVP_Backend_Implementation_Plan.md` §3 — *"**Không** lưu field dẫn xuất"* · **L12** (`docStatus`)

**Preconditions:** partner có 3 tài liệu FX-005 (VALID `2027-03-15`, NULL, URGENT `2026-07-28`).

**Test Steps:**
1. Act: map entity → `PartnerDetailResponse` với `Clock` FX-014
2. Assert giá trị dẫn xuất

**Expected Result (PASS):**
- `certExpiry == "2027-03-15"` — hạn **xa nhất** trong nhóm `FOOD_SAFETY_CERT` còn hiệu lực, định dạng **ISO** (D4)
- `certStatus == "VALID"` (suy từ chính tài liệu đó)
- `documents[].status` tính runtime: `["VALID", "VALID", "URGENT"]` (bản NULL = VALID)
- `SupplierPartnershipResponse.docStatus == "URGENT"` — mức xấu nhất trong hồ sơ của partner
- Class `Partner` **không** có setter `setCertStatus`/`setCertExpiry` (kiểm bằng reflection)

**Expected Result (FAIL):** đọc từ cột lưu sẵn → sau khi tài liệu hết hạn theo thời gian, `certStatus` vẫn `VALID` cho tới khi có ai đó cập nhật tay — đúng loại "số liệu đóng băng" mà ADR-M02-2 sinh ra để chặn.

**Current Status:** 🔴 Not written

---

### MF04-TC-012 — Chặn tải lên chứng nhận ATTP đã hết hạn

**Severity:** `HIGH`
**Feature Under Test:** `ComplianceDocumentService.upload()`
**Test File:** `src/test/java/com/sfms/partner/service/ComplianceDocumentServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-010`
**Oracle Source:** **UC-101 AF1** — *"Expired Certificate Uploaded: The expiration date entered is already past. System displays MSG-013 and blocks the upload"* · **MSG-013** ⚠️ *(ngoài §1.4 — CF-M04-2)* · **BR-046/BR-048**

**Test Steps:** upload `documentType = FOOD_SAFETY_CERT`, `expiryDate = 2026-07-27` (hôm qua theo FX-014).

**Expected Result (PASS):**
- Ném `ConflictException` `code = "SUP-013"`, HTTP 409
- `message` **nguyên văn** `error_message.md`: `"Supplier food safety certificate has expired. Onboarding blocked."` (D7)
- `verify(storageService, never()).store(any(), any())` — **không** ghi file rác lên storage khi validate fail
- Tài liệu loại khác (`PRODUCT_ORIGIN`) với hạn quá khứ → **cho phép** (UC-102 không có AF chặn)

**Expected Result (FAIL):** nhận chứng nhận hết hạn → NCC nộp đơn hợp tác với giấy tờ vô hiệu, BR-046 mất hiệu lực ngay tại cửa vào.

**Current Status:** 🔴 Not written

---

### MF04-TC-013 — Chứng nhận ATTP bắt buộc có ngày hết hạn

**Severity:** `MEDIUM`
**Feature Under Test:** `ComplianceDocumentService.upload()` + `ck_doc_cert_expiry`
**Test File:** `.../ComplianceDocumentServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-011`
**Oracle Source:** **BR-047** (chỉ theo dõi được hạn nếu có hạn) · mock `supplier/certificates/page.tsx:89-90` — *"Chứng nhận ATTP bắt buộc có ngày hết hạn — BR-047"*

**Test Steps:** upload `FOOD_SAFETY_CERT` với `expiryDate = null`.

**Expected Result (PASS):** `ValidationException` HTTP 422; thông điệp nêu rõ field `expiryDate`; `details` chứa `{ "field": "expiryDate" }` (D2).
**Expected Result (FAIL):** lưu được cert không hạn → job BR-047 bỏ qua vĩnh viễn (vì NULL = "không thời hạn"), giấy chứng nhận 3 năm trở thành vô thời hạn trong hệ thống.

**Current Status:** 🔴 Not written

---

### MF04-TC-014 — Portal state đúng 3 nhánh của UC-155 / UC-159

**Severity:** `MEDIUM`
**Feature Under Test:** `PartnershipApplicationService.portalState()`
**Test File:** `src/test/java/com/sfms/partner/service/PartnershipApplicationServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-012`
**Oracle Source:** **UC-155** Normal Flow bước 3 — *"'Submit Application' (if no application exists), 'View Application Status' (if an application is pending or approved), or 'Edit and Resubmit Application' (if rejected or action required)"* · **UC-159** (bản Service Caterers, cùng 3 nhánh)

**Test Steps (Equivalence Partitioning):**

| Trạng thái đơn hiện có | `state` kỳ vọng | `availableActions` |
|---|---|---|
| không có đơn nào | `NO_APPLICATION` | `["SUBMIT"]` |
| `PENDING` | `TRACKING` | `["TRACK"]` |
| `APPROVED` | `TRACKING` | `["TRACK"]` |
| `REJECTED` | `CAN_RESUBMIT` | `["RESUBMIT", "TRACK"]` |
| `ACTION_REQUIRED` | `CAN_RESUBMIT` | `["RESUBMIT", "TRACK"]` |

**Expected Result (PASS):** đủ 5 case; `applicationId` trả về là **mã đơn** (`APP-2026-01`, D1), null ở case đầu.
**Expected Result (FAIL):** gộp `APPROVED` vào `CAN_RESUBMIT` → NCC nộp đè lên quan hệ hợp tác đang hiệu lực.

**Current Status:** 🔴 Not written

---

### MF04-TC-015 — Email đối tác phải duy nhất (MSG-006)

**Severity:** `MEDIUM`
**Feature Under Test:** `PartnerRegistrationService.registerSupplier()` · `PartnerProfileService.updateMine()`
**Test File:** `src/test/java/com/sfms/partner/service/PartnerRegistrationServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-013`
**Oracle Source:** **MSG-006** *"Registration failed: The email address is already in use."* (§1.4 gán cho UC-071/UC-067) · **BR-012** *"Email phải là địa chỉ email hợp lệ và **duy nhất trong hệ thống**"* · **UC-049 AF2** *"Email Already Registered"*

**Test Steps:** đăng ký với `email = "sonlt@greenfarm.vn"` (đã thuộc `SUP-001`); và `updateMine()` đổi sang email của partner khác.

**Expected Result (PASS):** cả 2 luồng ném `ConflictException` `code = "SUP-006"`, message nguyên văn MSG-006; so sánh email **case-insensitive** (`SONLT@GreenFarm.VN` cũng trùng).
**Expected Result (FAIL):** cho trùng → 2 hồ sơ đối tác cùng email, tài khoản MF01 liên kết sai partner, kéo theo cách ly `partnerId` sai (SEC-001).

**Current Status:** 🔴 Not written

---

### MF04-TC-016 — Định dạng mã số thuế (MSG-007 ⚠️ ngoài §1.4)

**Severity:** `MEDIUM`
**Feature Under Test:** `SubmitApplicationRequest` validation
**Test File:** `src/test/java/com/sfms/partner/dto/SubmitApplicationRequestTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-014`
**Oracle Source:** **MSG-007** *"Invalid tax ID format. Please verify and try again."* · **UC-152** Normal Flow bước 5 — *"valid tax ID format per MSG-007"* · regex `^\d{10}(-\d{3})?$` từ mock `supplier/partnerships/page.tsx:58` (MST Việt Nam: 10 số, hoặc 10 số + 3 số chi nhánh)
> ⚠️ MSG-007 **không** nằm trong cột System Messages của §1.4 cho MF04 (CF-M04-2) — test theo nội dung `error_message.md`, chờ Huy quyết định bổ sung.

**Test Steps (BVA):** `"0109988776"` ✅ · `"0109988776-001"` ✅ · `"010998877"` (9 số) ❌ · `"01099887766"` (11 số) ❌ · `"0109988776-01"` ❌ · `"ABC0109988"` ❌

**Expected Result (PASS):** 2 hợp lệ, 4 sinh vi phạm với `code = "SUP-007"` HTTP 422.
**Expected Result (FAIL):** MST rác vào DB → hồ sơ pháp lý của đối tác không tra cứu được ở cơ quan thuế khi thanh tra.

**Current Status:** 🔴 Not written

---

### MF04-TC-017 — Upload đi qua `StorageService`, whitelist loại và kích thước file

**Severity:** `HIGH`
**Feature Under Test:** `ComplianceDocumentService.upload()`
**Test File:** `.../ComplianceDocumentServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-015`
**Oracle Source:** **MSG-008** *"File upload failed: File size exceeds the maximum limit of 10MB."* · **MSG-009** *"File upload failed: Unsupported file type. Only PDF, JPG, and PNG are allowed."* (cả hai có trong §1.4 cho UC-152/167/101/102) · **D10** — *"phải inject `StorageService` có sẵn ở `common/storage`, **không** tự gọi `MultipartFile.transferTo()`/AWS SDK riêng"*

**Preconditions:** FX-013 (bộ file), FX-016 (`StorageService` mock).

**Test Steps (BVA + Equivalence Partitioning):**

| File | Kích thước / magic | Kỳ vọng |
|---|---|---|
| `cert-valid.pdf` | 200KB / `%PDF` | ✅ 201, `storageService.store()` được gọi **đúng 1 lần** |
| biên 10MB | 10.485.760 byte | ✅ chấp nhận (biên trên bao gồm) |
| `cert-oversize.pdf` | 10.485.761 byte | ❌ `SUP-008` HTTP 413 |
| `face-oversize.jpg` | 6MB (ảnh mặt) | ❌ 413 — giới hạn **5MB** cho ảnh khuôn mặt |
| `notes.txt` | text/plain | ❌ `SUP-009` HTTP 415 |
| `fake.pdf` | đuôi `.pdf`, magic `MZ` | ❌ `SUP-009` — **kiểm magic bytes**, không tin đuôi |

**Expected Result (PASS):**
- 2 case ✅ / 4 case ❌ đúng mã lỗi
- Mọi case ❌: `verify(storageService, never()).store(any(), any())`
- Không có lời gọi `MultipartFile.transferTo()` hay AWS SDK trong `ComplianceDocumentService` (kiểm bằng ArchUnit hoặc review — xem Implementation Note)

**Expected Result (FAIL):** tự ghi đĩa → khi bật `sfms.storage.provider=r2` (prod, ADR-001) tài liệu tuân thủ vẫn nằm ở container và **mất khi redeploy**.

**Current Status:** 🔴 Not written
**Implementation Note:** ⚠️ D10 liệt kê module cần StorageService là *"MF03, MF07, MF08, MF10, MF11"* — **bỏ sót MF04** dù MF04 có 2 luồng upload. Đã ghi vào §8 để Nam bổ sung. Bổ sung ArchUnit rule: `noClasses().that().resideInAPackage("..partner..").should().callMethod(MultipartFile.class, "transferTo", ..)`.

---

### MF04-TC-018 — Enum serialize SCREAMING_SNAKE, từ chối chuỗi hiển thị (D6/L2)

**Severity:** `HIGH`
**Feature Under Test:** `PartnerType`, `BiometricStatus`, `RoleKey` (**`common/enums/`**) · `DocumentType`, `PartnerStatus`, `ApplicationStatus`, `PartnershipStatus` (**`com.sfms.mf04`** — ngoài 8 enum D6)
**Test File:** `src/test/java/com/sfms/partner/dto/EnumContractTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-016`
**Oracle Source:** **D6** — *"tất cả **SCREAMING_SNAKE** trong JSON và DB. FE giữ bảng nhãn tiếng Việt ở `lib/labels.ts` — nhãn hiển thị **không bao giờ** là giá trị lưu trữ"* · bảng D6: `PartnerType = RAW_FOOD_SUPPLIER | SERVICE_CATERER`; `BiometricStatus = NOT_REGISTERED | PENDING_REGISTRATION | REGISTERED` · `review.md` **E1** (blocker)

**Test Steps:**
1. Serialize `PartnerResponse(type = RAW_FOOD_SUPPLIER)` → assert JSON
2. Deserialize `{"type":"Raw Food Supplier"}` → assert bị từ chối
3. Assert `PartnerType.values().length == 2`, `BiometricStatus.values().length == 3`

**Expected Result (PASS):**
- JSON chứa `"type":"RAW_FOOD_SUPPLIER"`, **không** chứa `"Raw Food Supplier"`
- Chuỗi hiển thị (`"Raw Food Supplier"`, `"Service Caterer"`, `"Supplier"`) đều ném `InvalidFormatException` → HTTP 400
- `BiometricStatus` có **đủ 3** giá trị (D6/L3)
- **Chốt 2026-08-02 (M04-D02):** assert **cùng một type**, không phải trùng tên —
  `assertThat(PartnerType.class.getPackageName()).isEqualTo("com.sfms.common.enums")` cho `PartnerType`, `BiometricStatus`, `RoleKey` (cả 3 **đã có** trong code). Enum **riêng của MF04** (`PartnerStatus`, `DocumentType`, `ApplicationStatus`, `PartnershipStatus`) **không** nằm trong 8 enum của D6 ⇒ khai ở `com.sfms.mf04`, assert package tương ứng — khai nhầm chúng vào `common/` là lấn quyền sở hữu của Nam
- `RoleKey.from("School Administration")` và `RoleKey.from("Teacher")` **ném lỗi**; chỉ kebab-case hợp lệ (D6)

**Expected Result (FAIL):**
- Chấp nhận `"Raw Food Supplier"` → FE gửi `?type=Supplier` (giá trị chết đang có trong `TYPE_FILTER`) sẽ **im lặng trả rỗng**, người dùng tưởng không có nhà cung cấp nào

**Current Status:** 🔴 Not written

---

### MF04-TC-019 — Mọi field ngày trong JSON là ISO-8601 (D4/L4)

**Severity:** `HIGH`
**Feature Under Test:** Jackson config + toàn bộ DTO của MF04
**Test File:** `src/test/java/com/sfms/partner/dto/DateFormatContractTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-017`
**Oracle Source:** **D4** — *"Ngày → `"2026-07-29"` (`LocalDate`); Ngày giờ → `"2026-07-29T02:15:00Z"` (`Instant`)"* · `01_MVP_Backend_Implementation_Plan.md` §3 (*"ngày = ISO/Instant"*) · **L4** (TDS-MF04 tự mâu thuẫn, `review.md` A4)

**Test Steps:**
1. Serialize lần lượt `PartnerResponse`, `PartnerDetailResponse`, `DocumentResponse`, `DeliveryPersonResponse`, `ApplicationResponse`, `SupplierPartnershipResponse`
2. Assert bằng regex trên **toàn bộ** chuỗi JSON

**Expected Result (PASS):**
- Không JSON nào khớp `\d{2}/\d{2}/\d{4}` (định dạng `dd/MM/yyyy`)
- `appliedAt`, `submittedAt`, `uploadedAt`, `biometricRegisteredAt` khớp `\d{4}-\d{2}-\d{2}T.*Z`
- `issuedDate`, `expiryDate`, `since` khớp `^\d{4}-\d{2}-\d{2}$`
- `expiryDate` của tài liệu không thời hạn = **`null`** (JSON `null`, không phải chuỗi `"Không thời hạn"` — E5)
- Không DTO nào còn annotation `@JsonFormat(pattern = "dd/MM/yyyy")`

**Expected Result (FAIL):** trả `"28/06/2026"` như §9.2 hiện tại → FE parse bằng `new Date()` ra `Invalid Date`, và bộ lọc `dateFrom`/`dateTo` (§2b) **im lặng trả rỗng**.

**Current Status:** 🔴 Not written

---

### SECURITY TEST CASES

> `review_fix_plan.md` dòng 278 xếp **rủi ro số 1** của MF04: *"`partnerId` là khóa cách ly dữ liệu giữa NCC — sót một chỗ là rò dữ liệu chéo … thêm integration test RBAC"*. `MF04-TC-SEC-001` và `SEC-003` là hai test được yêu cầu ở đó.

---

### MF04-TC-SEC-001 — NCC không đọc được dữ liệu của NCC khác (đa-tenant)

**Severity:** `CRITICAL`
**OWASP:** `A01:2021 — Broken Access Control`
**CWE:** `CWE-639 — Authorization Bypass Through User-Controlled Key (IDOR)`
**Legal:** **PDPD** — dữ liệu nhân viên giao hàng của NCC khác; bí mật kinh doanh (hồ sơ tuân thủ)
**Feature Under Test:** `PartnerSpec`/`DocumentSpec`/`DeliveryPersonSpec` + `@PreAuthorize` trên 4 resource NCC
**Test File:** `src/test/java/com/sfms/partner/security/PartnerTenantIsolationIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-026`
**Oracle Source:** **TDS §17.1 C2 / ADR-M04-3** (bản v1.1) — *"mọi Specification của resource NCC thêm predicate `partner_id = jwt.partnerId`; **cross-partner/cross-school → 404 `SUP-404`**"* · **MSG-137** *"Resource not found."* · **CF-M04-6** (mock thiếu `partnerId` → đây chính là lỗ hổng nếu bê nguyên mock)
> ⚠️ **ĐỔI ORACLE 2026-08-02 (M04-D10).** Bản v1.0 của TestSpec này assert **403 `SUP-005`** và ghi *"không phải 404"*. Đã đảo lại: **404 `SUP-404`/MSG-137**. Lý do: 403 **xác nhận bản ghi tồn tại** ⇒ NCC quét mã (`DOC-GF-01`, `DP-02`…) là dò được danh mục đối thủ — đúng rủi ro D1 §Lý do mục 3. Nhất quán với **tiền lệ đã triển khai**: MF01 `StaffService.requireInScope()` (*"Ngoài phạm vi dữ liệu được phép đọc ⇒ 404 MSG-137, không 403"*) và MF02 §2 **L7** (chốt 2026-07-31). `SUP-005`/403 ở MF04 nay **chỉ** dành cho sai vai trò (`SEC-002`, `SEC-006`).

**Preconditions:**
- FX-001 `SUP-001` sở hữu `DOC-GF-01`, `DP-01`, `PTN-01`, `APP-2026-01`
- FX-010: JWT của **NCC khác** (`partnerId = SUP-003`)

**Test Steps (Attack Simulation):**
0. **Tiền đề chống Green-from-Birth (§5.1 bẫy 1):** gọi `GET /api/v1/supplier-documents/DOC-GF-01` bằng JWT **đúng chủ sở hữu** `SUP-001` (FX-009) → phải **200**. Không có bước này, mọi assert 404 dưới đây xanh ngay cả khi route chưa tồn tại.
1. Đăng nhập bằng JWT `SUP-003` (hợp lệ, không giả mạo)
2. `GET /api/v1/supplier-documents` → xem có lọt tài liệu của `SUP-001` không
3. `GET /api/v1/supplier-documents/DOC-GF-01` → truy cập **trực tiếp** bằng mã đã biết
4. `PUT /api/v1/supplier-documents/DOC-GF-01` → thử **ghi đè**
5. Lặp bước 3–4 cho `/delivery-persons/DP-01`, `/partnership-applications/APP-2026-01`, `/supplier-partnerships/PTN-01`

**Expected Result (PASS = hệ thống an toàn):**
- Bước 2: `200` với `items = []`, `total = 0`, `pageCount = 1` (§2b — tập rỗng vẫn 200)
- Bước 3–5: **`404`** với `error.code = "SUP-404"`, `error.message` = **MSG-137 nguyên văn** — **không** `403` (lộ sự tồn tại), tuyệt đối không `200`
- Response body **không chứa field dữ liệu nào** của `SUP-001` (kể cả trong `details`) — cùng assert mà `MF02-TC-SEC-002` dùng
- Phân biệt được với "bản ghi thật sự không tồn tại": cả hai đều `404 SUP-404` — **đó là mục đích**; test khẳng định điều này bằng cách gọi thêm một mã không tồn tại (`DOC-GF-99`) và assert **response giống hệt** (cùng status, cùng body)
- DB không đổi: `DOC-GF-01` giữ nguyên `expiry_date`
- Log ghi nhận cảnh báo truy cập chéo (`partnerId` thực tế ≠ chủ sở hữu) — log là nơi phân biệt, **không phải** response

**Expected Result (FAIL = lỗ hổng tồn tại):**
- Bất kỳ bước nào trả `200` kèm dữ liệu của `SUP-001` → **rò rỉ dữ liệu chéo giữa các nhà cung cấp**: hồ sơ tuân thủ (bí mật kinh doanh) và CCCD nhân viên (PII) của đối thủ. Đây là kịch bản D1 §"Lý do chọn phương án này" mục 3 cảnh báo.

**Current Status:** 🔴 Not written

---

### MF04-TC-SEC-002 — RBAC 7 role trên endpoint phê duyệt đối tác

**Severity:** `CRITICAL`
**OWASP:** `A01:2021 — Broken Access Control`
**CWE:** `CWE-862 — Missing Authorization`
**Feature Under Test:** `@PreAuthorize` trên `PartnerController#approve/reject/suspend/reinstate`
**Test File:** `src/test/java/com/sfms/partner/security/PartnerRbacIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-027`
**Oracle Source:** **BR-007** (RBAC 7 role) · **TDS §16 Authorization Matrix** — chỉ `SCHOOL ADMIN` được approve/reject/suspend/reinstate; *"MF04 **không** cấp quyền nào cho Teacher / Medical Staff / Parent"* · **UC-007/UC-004** Primary Actor = School Administration · **MSG-005**

**Test Steps (@ParameterizedTest — 7 role × 4 endpoint = 28 tổ hợp):**

| Role (`RoleKey` — D6) | approve | reject | suspend | reinstate |
|---|---|---|---|---|
| `school-admin` | ✅ 200 | ✅ 200 | ✅ 200 | ✅ 200 |
| `system-admin` | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 |
| `supplier` | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 |
| `service-caterers` | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 |
| `teacher` | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 |
| `medical-staff` | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 |
| `parent` | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 |

**Expected Result (PASS):** 4 tổ hợp `200`, **24 tổ hợp `403`** với `error.code = "SUP-005"`; không tổ hợp `403` nào làm đổi `partners.status`.
**Expected Result (FAIL):** `supplier` tự duyệt hồ sơ của chính mình → BR-040 (*"phải được School Administration xem xét và phê duyệt"*) bị vô hiệu hoàn toàn.

**Current Status:** 🔴 Not written
**Implementation Note:** Dùng `RoleKey` kebab-case theo **D6** (`school-admin`, `service-caterers`, `supplier`), không dùng nhãn BR-007 (`"School Administration"`).

---

### MF04-TC-SEC-003 — `partnerId` gửi kèm trong body/query bị bỏ qua

**Severity:** `CRITICAL`
**OWASP:** `A01:2021 — Broken Access Control`
**CWE:** `CWE-915 — Improperly Controlled Modification of Dynamically-Determined Object Attributes (Mass Assignment)`
**Feature Under Test:** `ComplianceDocumentService.upload()` · `DeliveryPersonService.create()` · `PartnershipApplicationService.submit()`
**Test File:** `.../PartnerTenantIsolationIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-026`
**Oracle Source:** **TDS §17.1 C2** — *"`partnerId`/`schoolId`/`role`/`userId` **PHẢI** đọc từ JWT (`AuthUser`) — KHÔNG tin body/query"* · **AP-AI-006** (TDS §17.4 *"Trust FE identity"*)

**Test Steps (Attack Simulation):**
1. JWT `SUP-003` (FX-010)
2. `POST /api/v1/delivery-persons` với body **cố ý** chèn `{"partnerId":"SUP-001", "fullName":"X", ...}`
3. `POST /api/v1/supplier-documents?partnerId=SUP-001` (query)
4. Assert quyền sở hữu bản ghi vừa tạo

**Expected Result (PASS = an toàn):**
- Bản ghi được tạo với `partner_id` = **`SUP-003`** (từ JWT), **không** phải `SUP-001`
- Hoặc: `400` vì DTO không khai field `partnerId` (phương án chặt hơn — khuyến nghị)
- `SUP-001` truy vấn danh sách của mình → **không** thấy bản ghi lạ

**Expected Result (FAIL = lỗ hổng):**
- Ghi được bản ghi vào không gian dữ liệu của `SUP-001` → NCC A cấy nhân viên giao hàng/tài liệu giả vào hồ sơ NCC B; kết hợp với BR-041 có thể khiến NCC B bị đình chỉ oan

**Current Status:** 🔴 Not written

---

### MF04-TC-SEC-004 — Upload file: kiểm nội dung thật, không tin phần mở rộng

**Severity:** `HIGH`
**OWASP:** `A03:2021 — Injection` / `A08:2021 — Software and Data Integrity Failures`
**CWE:** `CWE-434 — Unrestricted Upload of File with Dangerous Type`
**Feature Under Test:** `FileValidator` + `ComplianceDocumentController`
**Test File:** `src/test/java/com/sfms/partner/security/FileUploadSecurityIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-015`
**Oracle Source:** **MSG-009** *"Only PDF, JPG, and PNG are allowed."* · **MSG-008** (10MB) · **TDS §17.1 C7** — *"kiểm **magic bytes**, không tin đuôi/`Content-Type`"*

**Test Steps (Attack Simulation):**
1. Upload `fake.pdf`: đuôi `.pdf`, `Content-Type: application/pdf`, **magic bytes `MZ`** (Windows PE)
2. Upload `shell.php.png`: đuôi kép, magic `PNG`
3. Upload `huge.pdf`: 12MB, magic `%PDF`
4. Upload file có tên `../../etc/passwd.pdf` (path traversal trong `originalFilename`)

**Expected Result (PASS = an toàn):**
- (1) `415` `SUP-009` — phát hiện qua magic bytes dù `Content-Type` hợp lệ
- (2) `415` hoặc lưu với key do server sinh, **không** giữ tên gốc
- (3) `413` `SUP-008`
- (4) Key lưu trữ do server sinh theo khuôn `"partner-documents/<code>.<ext>"` (D10) — **không** chứa `..`; không file nào bị ghi ngoài thư mục storage
- Mọi case: `verify(storageService, never()).store(...)` khi validate fail

**Expected Result (FAIL = lỗ hổng):** file thực thi được lưu lên storage và phục vụ qua `/media/**` (D10) → lưu trữ tệp độc hại trên hạ tầng của trường.

**Current Status:** 🔴 Not written

---

### MF04-TC-SEC-005 — Không rò CCCD / dữ liệu sinh trắc học ra log và response

**Severity:** `CRITICAL`
**OWASP:** `A09:2021 — Security Logging and Monitoring Failures`
**CWE:** `CWE-532 — Insertion of Sensitive Information into Log File`
**Legal:** **PDPD** — dữ liệu cá nhân nhạy cảm
**Feature Under Test:** `DeliveryPersonService`, `FaceDataDeleted` event, log appender
**Test File:** `src/test/java/com/sfms/partner/security/PiiLeakIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-003`
**Oracle Source:** **TDS §14.2** — *"log **KHÔNG** được chứa CCCD thô (12 số liền) hay base64 ảnh khuôn mặt"* · **TDS §7.3** `FaceDataDeleted` — *"KHÔNG kèm `faceId`/ảnh trong payload — PDPD"* · **AP-AI-011** (TDS §17.4)

**Test Steps:**
1. Bắt log bằng `ListAppender<ILoggingEvent>` trên toàn package `com.sfms.mf04`
2. Chạy chuỗi: `create` → `registerFace` → `confirmEnrollment` → `deleteFace` → `GET /delivery-persons`
3. Grep toàn bộ log + toàn bộ response body

**Expected Result (PASS = an toàn):**
- Log **không** khớp `[^0-9][0-9]{12}[^0-9]` (CCCD thô) và **không** chứa `data:image/` hay base64 ảnh
- Response `GET /delivery-persons` chỉ chứa CCCD dạng che
- Payload `FaceDataDeleted` không có field `faceId`
- Exception stack trace khi FRS lỗi **không** in kèm ảnh/CCCD

**Expected Result (FAIL = lỗ hổng):** CCCD hoặc ảnh khuôn mặt xuất hiện trong log tập trung → dữ liệu nhạy cảm bị nhân bản sang hệ thống log, ngoài phạm vi kiểm soát PDPD và **không xóa được** khi UC-028 chạy.

**Current Status:** 🔴 Not written

---

### MF04-TC-SEC-006 — Truy cập không có JWT

**Severity:** `HIGH`
**OWASP:** `A07:2021 — Identification and Authentication Failures`
**CWE:** `CWE-306 — Missing Authentication for Critical Function`
**Feature Under Test:** Spring Security filter chain
**Test File:** `.../PartnerRbacIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-027`
**Oracle Source:** **MSG-004** *"Session expired. Please log in again."* (§1.4 — mọi UC "View…" của MF04) · **TDS §16** — 2 endpoint public duy nhất là `POST /partners/register/{supplier,caterer}` (Guest, UC-071/067)

**Test Steps:** gọi **không** header `Authorization` lần lượt: `GET /partners`, `GET /supplier-documents`, `POST /partners/SUP-005/approve`, `POST /delivery-persons`, và 2 endpoint đăng ký công khai.

**Expected Result (PASS):**
- 4 endpoint nội bộ → `401` `error.code = "SUP-004"`
- 2 endpoint đăng ký → **không** 401 (public theo §16; vẫn có thể 400/409 do validate)

**Expected Result (FAIL):** endpoint nội bộ trả `200` → toàn bộ ma trận phân quyền §16 vô nghĩa.

**Current Status:** 🔴 Not written

---

### MF04-TC-SEC-007 — Đăng ký công khai: chống dò tài khoản + rate limit

**Severity:** `MEDIUM`
**OWASP:** `A07:2021 — Identification and Authentication Failures`
**CWE:** `CWE-204 — Observable Response Discrepancy`
**Feature Under Test:** `PartnerRegistrationService` + rate limiter
**Test File:** `.../PartnerRegistrationSecurityIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-013`
**Oracle Source:** **TDS §4.3** — *"Đăng ký công khai (UC-071/067) rate-limit **5/min/IP**; trả **MSG-006** thống nhất, không tiết lộ tài khoản nào tồn tại"* · **MSG-006**

**Test Steps (Attack Simulation):**
1. Đăng ký với email **đã tồn tại** (`sonlt@greenfarm.vn`) và email **chưa tồn tại nhưng MST trùng** — so sánh response
2. Gửi 6 request đăng ký trong 1 phút từ cùng IP

**Expected Result (PASS = an toàn):**
- Hai trường hợp ở bước 1 trả **cùng** mã `SUP-006` và cùng message MSG-006; **thời gian phản hồi** không lệch đáng kể (không dùng `existsByEmail` sớm rồi return ngay)
- Request thứ 6 → **`429`** với `error.code = "SUP-141"`, `error.message` = **MSG-141 nguyên văn** (*"Too many requests. Please try again later."*), envelope D2 đủ 3 khóa — **chốt 2026-08-02 (M04-D07)**; bản v1.0 chỉ assert *"429 Too Many Requests"* mà không có mã lỗi nào để đối chiếu. Tiền lệ: MF02 `SCH-014`/MSG-141 (`error_message.md` v1.8, dùng chung mọi module)

**Expected Result (FAIL = lỗ hổng):** message khác nhau ("email đã dùng" vs "MST đã dùng") → dò được danh sách đối tác/MST đang hợp tác với trường.

**Current Status:** 🔴 Not written

---

### MF04-TC-SEC-008 — School Admin trường A không duyệt được đơn nộp vào trường B

**Severity:** `CRITICAL`
**OWASP:** `A01:2021 — Broken Access Control`
**CWE:** `CWE-639 — Authorization Bypass Through User-Controlled Key`
**Feature Under Test:** `PartnerApprovalService.approve()` — `schoolId` lấy từ JWT
**Test File:** `.../PartnerRbacIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-027`
**Oracle Source:** **TDS §6.1** — *"`findOpenApplication(partnerId, schoolId)` lấy `schoolId` **từ JWT**, không từ body — School Admin trường A không duyệt được đơn nộp vào trường B (ADR-M04-3)"* · **BR-022** (nguyên tắc cách ly theo trường, MF02)

**Test Steps:**
1. FX-007: `APP-2026-04` là đơn của `SUP-001` nộp vào **TH Nguyễn Bỉnh Khiêm**
2. Đăng nhập FX-012 (School Admin **trường khác**)
3. `POST /api/v1/partners/SUP-001/approve`
4. Thử biến thể: thêm `{"schoolId":"<trường của đơn>"}` vào body

**Expected Result (PASS = an toàn):**
- Bước 3: **`404` `SUP-404`** + `message` = MSG-137 nguyên văn — **chốt 2026-08-02 (M04-D10)**, bản v1.0 để ngỏ *"404 hoặc 403"*; nay chỉ **một** nhánh đúng, vì "hoặc" nghĩa là test không bắt được lỗi nào
- Bước 4: body `schoolId` bị **bỏ qua** (không phải báo lỗi), kết quả không đổi
- `APP-2026-04.status` vẫn `PENDING`; `partners.SUP-001` không đổi

**Expected Result (FAIL = lỗ hổng):** trường B duyệt hộ đơn của trường A → đối tác được cấp quyền giao hàng cho một trường **chưa từng xét duyệt hồ sơ** (vi phạm BR-040).

**Current Status:** 🔴 Not written

---

### MF04-TC-SEC-009 — Contract test trên `/v3/api-docs` *(D8 — chỉ chạy khi Nam approve)*

**Severity:** `MEDIUM`
**Feature Under Test:** OpenAPI spec sinh bởi springdoc
**Test File:** `src/test/java/com/sfms/partner/contract/OpenApiContractIT.java`
**TDD Phase:** ⏸️ **BLOCKED** — D8 đang `⏳ chờ Nam approve` (`00_Common_Conventions.md` changelog 2026-07-30)
**Condition Ref:** `TC-COND-029`
**Oracle Source:** **D8 §"Việc phải làm ở từng TDS"** — *"**§13 / TestSpec** — ≥1 **contract test đọc `/v3/api-docs`** và assert: (a) không field `id` nào có `format: uuid`; (b) mọi response 4xx/5xx trỏ `ErrorResponse`; (c) mọi enum khớp bảng D6"*

**Test Steps:**
1. `GET /v3/api-docs` (phải `permitAll` — D8 §"Lưu ý hạ tầng")
2. Duyệt JSON spec, lọc các path `/api/v1/partners*`, `/supplier-documents*`, `/delivery-persons*`, `/partnership-applications*`, `/supplier-partnerships*`

**Expected Result (PASS):**
- (a) Không schema property tên `id` hoặc `*Id` nào có `"format":"uuid"`; `example` của `id` là mã nghiệp vụ (`"SUP-001"`) — **D1/S6**
- (b) Mọi `responses` mã `4xx`/`5xx` có `content.schema.$ref` trỏ `#/components/schemas/ErrorResponse` — **D2/S3**
- (c) `PartnerType.enum == ["RAW_FOOD_SUPPLIER","SERVICE_CATERER"]`; `BiometricStatus.enum` đúng **3** giá trị D6 — **D6/S5**
- Bổ sung: mọi field ngày có `"format":"date"` hoặc `"date-time"` (**D4/S4**); mọi `@Operation.description` chứa ít nhất một mã `UC-\d{3}` và một mã `BR-\d{3}` (**S2**)

**Expected Result (FAIL):** spec lộ UUID hoặc enum viết tay → `openapi-typescript` (T3.4) sinh type sai cho FE, và lệch D1/D2/D6 **quay lại qua đường file sinh tự động** — đúng rủi ro D8 nêu.

**Current Status:** ⏸️ Blocked — viết sẵn, `@Disabled("D8 pending approve — bật khi Nam approve")`

---

### INTEGRATION TEST CASES

> **Hạ tầng:** `@SpringBootTest(webEnvironment = RANDOM_PORT)` + **Testcontainers `PostgreSQLContainer("postgres:16-alpine")`**, Flyway chạy thật (`V1__init_schema.sql` + `V{n}__seed_mf04.sql`). Timeout 120s. **KHÔNG dùng H2** — các bất biến ở §5.2 dựa vào partial unique index và CHECK constraint của PostgreSQL.

---

### MF04-TC-INT-001 — Duyệt đơn cập nhật đồng thời 3 bảng trong 1 transaction

**Severity:** `CRITICAL`
**Feature Under Test:** `Full flow: POST /api/v1/partners/{code}/approve → application + partnership + partner + audit + event`
**Test File:** `src/test/java/com/sfms/partner/integration/PartnerApprovalIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-018`
**Oracle Source:** **UC-007** Normal Flow bước 7 + 9 — *"System updates the status to 'Approved', activates the supplier account … System logs the action in the audit trail"* · **MSG-037** *"Supplier onboarding application approved successfully."* · **ADR-M04-2** · **CF-M04-9** (mock duyệt `partners` nhưng đơn vẫn PENDING — lỗ hổng test này chốt chặn) · **D9/L10** (audit tự điền)

**Preconditions:**
- Testcontainers PostgreSQL đang chạy, Flyway đã migrate
- Seed FX-002: `SUP-005` `PENDING`, tài liệu đều `VALID`, đơn `APP-2026-05` `PENDING` với `SCH-HN-0042`
- JWT FX-011 (School Admin `SCH-HN-0042`)

**Test Steps:**
1. `POST /api/v1/partners/SUP-005/approve` *(path param = **mã nghiệp vụ**, D1/L1)*
2. Assert HTTP + body
3. Assert DB trên **3 bảng**
4. Assert audit + event

**Expected Result (PASS):**
- `200`, body `{ "id": "SUP-005", "status": "APPROVED" }` — `id` là mã, **không** UUID (D1)
- `partnership_applications` `APP-2026-05`: `status = APPROVED`, `reviewed_by`/`reviewed_at` khác null
- `supplier_partnerships` (`SUP-005`, `SCH-HN-0042`): tồn tại, `status = APPROVED`, `since = 2026-07-28`
- `partners` `SUP-005`: `status = APPROVED`, `reject_reason = NULL`
- `audit_logs`: 1 bản ghi `resource = 'Supplier Management'`, actor = email trong JWT
- **`created_by`/`updated_by` được `AuditorAware` tự điền** = email actor, dù service không set tay (D9)
- Phát đúng 1 `PartnerApproved` với `partnerCode = "SUP-005"`

**Expected Result (FAIL):**
- Chỉ `partners` đổi, đơn vẫn `PENDING` → **tái tạo đúng lỗi CF-M04-9**: NCC nhìn màn "Trạng thái đơn" thấy vẫn chờ duyệt dù đã được duyệt
- `supplier_partnerships` không sinh → MF06 không biết đối tác được phép giao cho trường nào

**DB Assertion:**
```java
var app = jdbc.queryForMap("SELECT status, reviewed_at FROM partnership_applications WHERE code = 'APP-2026-05'");
assertThat(app.get("status")).isEqualTo("APPROVED");
assertThat(app.get("reviewed_at")).isNotNull();

var ptn = jdbc.queryForMap("""
    SELECT sp.status FROM supplier_partnerships sp
      JOIN partners p ON p.id = sp.partner_id
      JOIN schools  s ON s.id = sp.school_id
     WHERE p.code = 'SUP-005' AND s.code = 'SCH-HN-0042'""");
assertThat(ptn.get("status")).isEqualTo("APPROVED");

assertThat(jdbc.queryForObject(
    "SELECT status FROM partners WHERE code = 'SUP-005'", String.class)).isEqualTo("APPROVED");
```

**Current Status:** 🔴 Not written

---

### MF04-TC-INT-002 — Chặn duyệt khi còn tài liệu hết hạn, và rollback sạch

**Severity:** `CRITICAL`
**Feature Under Test:** `Full flow: approve() với hồ sơ có tài liệu EXPIRED → 409 + rollback`
**Test File:** `.../PartnerApprovalIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-010`, `TC-COND-018`
**Oracle Source:** **BR-046** (*"Tài liệu quá hạn sẽ được cảnh báo và **có thể ảnh hưởng đến trạng thái hợp tác**"*) · **UC-007 AF1** *"Expired Certificates: … System displays MSG-013 and **prevents approval** until valid certificates are submitted"* · **MSG-013** ⚠️ *(ngoài §1.4)* · mock `school-admin/partnerships/[id]/page.tsx:88` (nút duyệt `disabled` khi `hasExpiredDoc`)

**Preconditions:** FX-003 `SUP-004` với chứng nhận ATTP hết hạn `2026-06-22`; có đơn mở với trường của actor.

**Test Steps:**
1. `POST /api/v1/partners/SUP-004/approve`
2. Assert response
3. Assert **không** bảng nào bị đổi (transaction rollback)

**Expected Result (PASS):**
- `409` với envelope D2 đầy đủ: `{"error":{"code":"SUP-013","message":"Supplier food safety certificate has expired. Onboarding blocked.","details":[...]}}`
- `partners.SUP-004.status` **vẫn** `SUSPENDED`; `partnership_applications` không đổi; `supplier_partnerships` **không** sinh dòng mới
- Không phát `PartnerApproved`; không có audit "APPROVE"

**Expected Result (FAIL):**
- Duyệt thành công → đối tác có giấy tờ vô hiệu được phép giao nguyên liệu; luật `NHAN_HANG_SAU_KHI_CHUNG_NHAN_HET_HAN` của `semantic-audit.ts` sẽ bắt được ở khâu nhận hàng, nhưng lúc đó thực phẩm **đã vào bếp**
- Duyệt fail nhưng `supplier_partnerships` vẫn có dòng mới → transaction không bao trọn 3 thao tác

**Current Status:** 🔴 Not written

---

### MF04-TC-INT-003 — Đình chỉ đối tác cascade khóa tài khoản (ADR-M04-6 / C5)

**Severity:** `CRITICAL`
**Feature Under Test:** `Full flow: POST /partners/{code}/suspend → partners + users(MF01) + PartnerLookupPort`
**Test File:** `src/test/java/com/sfms/partner/integration/PartnerSuspensionIT.java`
**TDD Phase:** 🔴 RED — ⚠️ **phụ thuộc L11** (MF01 phải có `users.partner_id` + JWT claim)
**Condition Ref:** `TC-COND-019`
**Oracle Source:** **ràng buộc C5 `data_model.puml`** — *"Đối tác bị đình chỉ: tài khoản không đăng nhập được, không nhận đơn mới, báo giá không dùng để ước tính chi phí (BR-070)"* · **ADR-M04-6** · **BR-048** (lý do bắt buộc) · `review.md` B3 (blocker `partner_id`)

**Preconditions:** FX-003 `SUP-004` `APPROVED`; FX-008 `U-0007` (`partner_id = SUP-004`) đang `ACTIVE`.

**Test Steps:**
1. `POST /api/v1/partners/SUP-004/suspend` body `{"reason":"Lô tôm BATCH-DD-1092 không đạt kiểm tra tiếp nhận 30/06."}`
2. Assert `partners` + `users` + port
3. Biến thể: gọi lại **không** `reason` → assert bị chặn

**Expected Result (PASS):**
- `200`, body có `suspendedAccounts >= 1`
- `partners.SUP-004`: `status = SUSPENDED`, `suspend_reason` lưu **nguyên văn**
- `users.U-0007.status = SUSPENDED` — đổi trong **cùng transaction/luồng event**, không phải vòng lặp phía client như mock
- `PartnerLookupPort.isApproved(SUP-004) == false` → MF06 chặn nhận hàng (BR-070)
- Biến thể không `reason` → `422 SUP-048`, không có tài khoản nào bị khóa

**Expected Result (FAIL):**
- `users.U-0007` vẫn `ACTIVE` → đối tác bị đình chỉ **vẫn đăng nhập gửi báo giá bình thường**, đúng lỗ hổng mà ràng buộc C5 mô tả
- Đình chỉ được mà không cần lý do → BR-048 vô hiệu

**DB Assertion:**
```java
assertThat(jdbc.queryForObject(
    "SELECT status FROM users WHERE code = 'U-0007'", String.class)).isEqualTo("SUSPENDED");
assertThat(jdbc.queryForObject(
    "SELECT suspend_reason FROM partners WHERE code = 'SUP-004'", String.class))
    .isEqualTo("Lô tôm BATCH-DD-1092 không đạt kiểm tra tiếp nhận 30/06.");
```

**Current Status:** 🔴 Not written
**Implementation Note:** Nếu MF01 **chưa** bổ sung `users.partner_id` (L11), test này FAIL 🔴 **hợp lệ** — đó là RED thật, không phải lỗi test. Không được "sửa cho xanh" bằng cách bỏ assert `users`.

---

### MF04-TC-INT-004 — Khôi phục hợp tác bị chặn khi hồ sơ chưa hợp lệ

**Severity:** `HIGH`
**Feature Under Test:** `POST /api/v1/partners/{code}/reinstate`
**Test File:** `.../PartnerSuspensionIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-019`, `TC-COND-010`
**Oracle Source:** **ADR-M04-6** — *"`reinstate` đảo ngược, và **chặn** nếu còn tài liệu `URGENT/EXPIRED`"* · mock `school-admin/partnerships/[id]/page.tsx:116` (nút "Khôi phục hợp tác" `disabled` khi `hasExpiredDoc`) · **BR-046**

**Test Steps:**
1. `SUP-004` đang `SUSPENDED`, chứng nhận vẫn hết hạn → `POST /reinstate`
2. Upload chứng nhận mới còn hiệu lực (UC-091) → `POST /reinstate` lần 2

**Expected Result (PASS):**
- Lần 1: `409 SUP-013`, `status` vẫn `SUSPENDED`, `users.U-0007` vẫn `SUSPENDED`
- Lần 2: `200`, `status = APPROVED`, `suspend_reason = NULL`, `users.U-0007` trở lại `ACTIVE`

**Expected Result (FAIL):** khôi phục được khi giấy tờ vẫn hết hạn → vòng lặp "đình chỉ vì hết hạn → khôi phục ngay" khiến BR-046 thành hình thức.

**Current Status:** 🔴 Not written

---

### MF04-TC-INT-005 — Nộp đơn hợp tác lưu đủ payload và trả cả `schoolId` lẫn `school`

**Severity:** `HIGH`
**Feature Under Test:** `Full flow: POST /api/v1/partnership-applications (multipart) → applications + application_documents`
**Test File:** `src/test/java/com/sfms/partner/integration/PartnershipApplicationIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-020`
**Oracle Source:** **UC-152** Normal Flow bước 6 — *"System saves the application with status 'Pending Approval' and logs the event"* · **MSG-028** *"Registration request submitted! A verification email has been sent."* · **CF-M04-9** (mock bỏ mất payload) · **L8** (`review_fix_plan.md` dòng 141: *"Response trả **cả** `schoolId` lẫn `school`"*)

**Test Steps:**
1. `POST /api/v1/partnership-applications` multipart: part `meta` = `{schoolId, companyName, taxId, address, certificateDocumentIds:["DOC-GF-01"]}` + part `businessLicense` = `cert-valid.pdf`
2. Assert response, DB, bảng nối

**Expected Result (PASS):**
- `201`, `MSG-028`; body có **đồng thời** `"schoolId": "SCH-NBK-0001"` **và** `"school": "TH Nguyễn Bỉnh Khiêm"` (L8)
- `partnership_applications`: lưu đủ `company_name`, `tax_id`, `address`, `partner_id`, `school_id` — **không** rơi field nào (CF-M04-9)
- `partnership_application_documents`: đúng 1 dòng nối `APP-*` ↔ `DOC-GF-01`
- File giấy phép lưu qua `StorageService` (D10), `file_key` khác null
- `submitted_at` là **ISO Instant** (D4)

**Expected Result (FAIL):** chỉ lưu `{school, submittedAt, status}` như mock → School Admin mở đơn ra không có gì để xét duyệt, phải hỏi lại NCC qua kênh ngoài hệ thống.

**Current Status:** 🔴 Not written

---

### MF04-TC-INT-006 — Chỉ một đơn đang mở trên mỗi (partner, school)

**Severity:** `HIGH`
**Feature Under Test:** `uq_app_open_per_partner_school` (partial unique index)
**Test File:** `.../PartnershipApplicationIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-020`
**Oracle Source:** **UC-152 Preconditions** — *"The Raw Food Supplier is authenticated, logged in, and **does not have an active application**"* · **UC-156 Preconditions** (bản caterer) · `SUP-409`/**MSG-118** (*"An open application already exists, or this document has already been submitted."*)
> ⚠️ **Sửa 2026-08-02:** oracle cũ ghi *"**D5** (`SUP-409`)"* — nhầm nguồn. Đây là **lỗi nghiệp vụ** (đơn đang mở, `uq_app_open_per_partner_school`), oracle là UC precondition + MSG-118. Nhánh **D5** (client gửi `id` trùng) là mã **khác**: `SUP-139`/MSG-139, kiểm ở `MF04-TC-INT-010` (**M04-D06**).

**Test Steps:**
1. FX-007: `SUP-001` đã có `APP-2026-04` `PENDING` với TH Nguyễn Bỉnh Khiêm
2. Nộp đơn thứ hai **cùng trường** → assert 409
3. Nộp đơn cho **trường khác** → assert 201 (không bị chặn nhầm)
4. Sau khi `APP-2026-04` bị `REJECTED` → nộp đơn mới cùng trường → assert 201 (index chỉ chặn `PENDING`/`ACTION_REQUIRED`)

**Expected Result (PASS):** 409 `SUP-409` ở bước 2; 201 ở bước 3 và 4.
**Expected Result (FAIL):** bước 2 tạo được → 2 đơn cùng chờ duyệt, School Admin duyệt đơn này từ chối đơn kia, trạng thái đối tác phụ thuộc thứ tự thao tác.

**Current Status:** 🔴 Not written

---

### MF04-TC-INT-007 — Nộp lại đơn giữ nguyên mã đơn và xóa phản hồi cũ

**Severity:** `HIGH`
**Feature Under Test:** `POST /api/v1/partnership-applications/{code}/resubmit`
**Test File:** `.../PartnershipApplicationIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-020`
**Oracle Source:** **UC-170** Normal Flow bước 6 — *"System updates the database record with the new details and **resets status to 'Pending Approval'**"* · **UC-158** (bản caterer) · **MSG-029** *"Profile updated successfully."* · **UC-170 Preconditions** (chỉ áp dụng cho đơn `Rejected`/`Action Required`)

**Test Steps:**
1. FX-007 `APP-2026-01` `REJECTED` + `comment` có nội dung
2. `POST /api/v1/partnership-applications/APP-2026-01/resubmit` với payload đã sửa
3. Biến thể: resubmit đơn đang `PENDING` và đơn `APPROVED`

**Expected Result (PASS):**
- `200` `MSG-029`; `status = PENDING`; **`code` không đổi** (`APP-2026-01`) — đúng nút *"Nộp lại (UC-170)"* của mock, thay vì tạo mã mới
- `comment` được **xóa** (`NULL`) — vòng phản hồi mới bắt đầu sạch
- `submitted_at` cập nhật; phát `PartnershipApplicationResubmitted`
- Biến thể `PENDING`/`APPROVED` → `409`, không đổi gì

**Expected Result (FAIL):** tạo đơn mới mã khác (như mock đang làm) → lịch sử xét duyệt của cùng một hồ sơ bị vỡ thành nhiều dòng rời rạc, không truy được vì sao bị từ chối lần đầu.

**Current Status:** 🔴 Not written

---

### MF04-TC-INT-008 — Cập nhật tài liệu tạo bản mới, giữ bản cũ làm chứng cứ

**Severity:** `HIGH`
**Feature Under Test:** `PUT /api/v1/supplier-documents/{code}`
**Test File:** `src/test/java/com/sfms/partner/integration/ComplianceDocumentIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-021`
**Oracle Source:** **UC-091** Postconditions — *"Certificate expiration date is re-evaluated. **If a previously expired certificate is replaced with a valid one, the supplier's blocked status is reviewed.** The update event is audited."* · **UC-091 AF1** · **TDS §4.2** (*"tài liệu **không xóa vật lý** — chứng cứ thanh tra ATTP"*)

**Test Steps:**
1. `DOC-GF-04` (VietGAP, `URGENT`) → `PUT` với file mới + `expiryDate = 2029-07-01`
2. Assert bản cũ + bản mới
3. Kịch bản gỡ chặn: partner có tài liệu `EXPIRED` → thay bằng bản hợp lệ → assert đánh giá lại

**Expected Result (PASS):**
- `200` `MSG-029`; bản ghi mới có `expiry_date = 2029-07-01`, trạng thái dẫn xuất `VALID`
- **Bản cũ vẫn còn trong DB** với `superseded_by_document_id` trỏ bản mới (`SELECT COUNT(*)` tăng 1, không phải giữ nguyên)
- Job BR-047 **không** quét bản đã superseded (xem `MF04-TC-INT-009`)
- Phát `ComplianceDocumentUpdated`; partner trước đó bị chặn nay `hasValidFoodSafetyCert() == true`

**Expected Result (FAIL):** ghi đè tại chỗ → mất bản chứng nhận cũ; khi thanh tra hỏi "giai đoạn tháng 6 các anh dùng giấy nào" thì không có gì để xuất trình.

**Current Status:** 🔴 Not written

---

### MF04-TC-INT-009 — Job quét hạn tài liệu gửi đúng cảnh báo MSG-053

**Severity:** `MEDIUM`
**Feature Under Test:** `ComplianceExpiryScheduler.run()` → notification
**Test File:** `.../ComplianceDocumentIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-004`
**Oracle Source:** **MSG-053** *"Supplier food safety certificate is expiring in [Number] days."* (§1.4 gán cho UC-167/091/101) · **BR-047** ngưỡng 180/30 · **L4** (bỏ qua `expiry_date NULL`)

**Preconditions:** FX-005 + FX-014 (`TODAY = 2026-07-28`); một tài liệu đã `superseded`.

**Test Steps:** chạy scheduler một lần, thu toàn bộ notification phát ra.

**Expected Result (PASS):**
- Gửi cảnh báo cho `DOC-GF-04` (hạn `2026-07-28` → `URGENT`) với `[Number]` = số ngày thực tế
- **Không** gửi cho `DOC-GF-01` (hạn `2027-03-15`, > 180 ngày)
- **Không** gửi cho `DOC-GF-02` (`expiry_date = NULL` — "Không thời hạn", L4/E5)
- **Không** gửi cho tài liệu đã `superseded_by_document_id != NULL`
- Người nhận gồm **cả** NCC sở hữu **và** School Administration của trường đang hợp tác (BR-046 — *"có thể ảnh hưởng đến trạng thái hợp tác"*)
- Chạy scheduler 2 lần trong ngày → **không** gửi trùng (idempotent theo ngày)

**Expected Result (FAIL):**
- Ném exception ở bản NULL → job chết giữa chừng, các tài liệu sau đó không được quét (đúng lỗi E5 dự báo)
- Gửi cho mọi tài liệu → spam, người dùng tắt thông báo, cảnh báo thật bị bỏ qua

**Current Status:** 🔴 Not written

---

### MF04-TC-INT-010 — Client gửi `id`: dùng làm `code`, trùng thì 409 (D5/L6)

**Severity:** `HIGH`
**Feature Under Test:** `POST /api/v1/supplier-documents`, `POST /api/v1/delivery-persons`
**Test File:** `src/test/java/com/sfms/partner/integration/ClientSuppliedIdIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-022`
**Oracle Source:** **D5** — *"(1) body có `id` → dùng làm `code`, kiểm trùng → trùng thì **409** với mã `<MODULE>-409`; (2) vắng → server tự sinh theo `code_sequences`; (3) backend **luôn** trả `id` trong response 201"*
> Luồng này là **thật**, không giả định: mock `supplier/certificates/page.tsx:103` gửi `id: "DOC-GF-" + Date.now().slice(-4)`; `supplier/delivery-staff/page.tsx:94` gửi `id: "DP-" + …`.

**Test Steps:**
1. `POST /supplier-documents` body có `"id":"DOC-GF-99"` → assert dùng đúng mã đó
2. Lặp lại **cùng** `"id":"DOC-GF-99"` → assert 409
3. `POST` **không** có `id` → assert server sinh mã theo `code_sequences`
4. Chạy 2 request song song không có `id` → assert 2 mã khác nhau

**Expected Result (PASS):**
- (1) `201`, `response.id == "DOC-GF-99"`, DB `code = 'DOC-GF-99'`
- (2) **`409` `SUP-139`**, `error.message` = **MSG-139 nguyên văn** (*"A record with this value already exists."*), `details` là mảng, và **bản ghi cũ không bị ghi đè** (assert từng field) — **chốt 2026-08-02 (M04-D06)**. ⚠️ Bản v1.0 ghi `SUP-409`: sai, vì `SUP-409`/MSG-118 mang nghĩa **nghiệp vụ** (*"An open application already exists, or this document has already been submitted"*) — dùng chung một `code` cho hai nguyên nhân là vi phạm **D7** (FE tra `lib/messages.ts` theo `code` sẽ hiện nhãn sai). Tiền lệ: MF02 tách `SCH-409`/MSG-139 khỏi `SCH-007`
- (3) `201`, `id` khớp khuôn module, khác null
- (4) 2 mã **khác nhau** — `code_sequences` dùng `INSERT ... RETURNING`, không đọc-rồi-ghi (D1)

**Expected Result (FAIL):** server tự cấp mã khác với mã client gửi → FE điều hướng ngay trong `onSuccess` bằng mã cũ → **404 màn chi tiết**, đúng rủi ro D5 nêu.

**Current Status:** 🔴 Not written

---

### MF04-TC-INT-011 — Hợp đồng phân trang §2b trên 5 resource MF04

**Severity:** `HIGH`
**Feature Under Test:** `GET` danh sách của `partners`, `supplier-documents`, `delivery-persons`, `partnership-applications`, `supplier-partnerships`
**Test File:** `src/test/java/com/sfms/partner/integration/PagedContractIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-023`
**Oracle Source:** **`00_Team_Work_Assignment_Plan.md` §2b-1/§2b-6** — 5 field bắt buộc; `pageCount = max(1, ceil(total/pageSize))`; `page = min(page, pageCount)`; *"Tập rỗng sau lọc → trả `200 { items:[], total:0, page:1, pageSize:8, pageCount:1 }`, **không phải 404**"* · **§2b-7** checklist review của Mạnh · **L12** (`supplier-partnerships` thiếu schema)

**Test Steps (@ParameterizedTest trên 5 resource):**
1. `GET /api/v1/{resource}?page=99&pageSize=8` (vượt trang)
2. `GET /api/v1/{resource}?<filter không khớp gì>`
3. `GET /api/v1/{resource}?pageSize=0`
4. `GET /api/v1/partners?search=<chuỗi chỉ có ở cột ngoài SEARCH_FIELDS>`

**Expected Result (PASS):**
- Body có **đúng 5 khóa** `items,total,page,pageSize,pageCount` — không wrapper `data`/`result`
- (1) `page` đã clamp = `pageCount`
- (2) `200` với `items=[] , total=0, pageCount=1` — **không** 404
- (3) `pageSize` clamp về ≥ 1
- (4) Không trả kết quả — `search` chỉ chạy trên `SEARCH_FIELDS` đã khai (TDS §8.3), không OR mọi cột

**Expected Result (FAIL):** tập rỗng trả 404 → FE `usePagedList` ném lỗi, màn danh sách hiện "Đã có lỗi xảy ra" thay vì trạng thái rỗng.

**Current Status:** 🔴 Not written

---

### MF04-TC-INT-011b — Xóa dữ liệu khuôn mặt end-to-end (UC-028)

**Severity:** `HIGH`
**Feature Under Test:** `DELETE /api/v1/delivery-persons/{code}/face`
**Test File:** `src/test/java/com/sfms/partner/integration/DeliveryPersonBiometricIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-007`
**Oracle Source:** **UC-028** Postconditions — *"biometric face data is permanently deleted … gate access authorization is **revoked** … deletion event is logged in the audit trail with timestamp and actor identity"* · **MSG-029** · **BR-051** · **CF-M04-7** (mock chưa có nút — backend vẫn phải có endpoint)

**Test Steps:** `DP-01` `REGISTERED` → `DELETE .../DP-01/face` → assert DB + audit + ràng buộc.

**Expected Result (PASS):**
- `200` `MSG-029`; `biometric_status = NOT_REGISTERED`, `face_id = NULL`, `biometric_registered_at = NULL`
- `audit_logs` có bản ghi kèm **actor + timestamp + mã nhân viên**
- Ràng buộc `ck_dp_face` không bị vi phạm sau thao tác
- Gọi lại lần 2 → `409`/`404` (không có gì để xóa), **không** ném 500

**Expected Result (FAIL):** không có endpoint (đúng hiện trạng mock) → BR-051 (*"dữ liệu có thể bị NCC xóa"*) không thi hành được; NCC không thực hiện được quyền xóa dữ liệu cá nhân theo PDPD.

**Current Status:** 🔴 Not written

---

### MF04-TC-INT-012 — CCCD trùng dưới 2 request song song (race condition)

**Severity:** `HIGH`
**Feature Under Test:** `ux_dp_nid_hash` — bất biến ở tầng DB
**Test File:** `.../DeliveryPersonBiometricIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-001`
**Oracle Source:** **BR-050** (*"duy nhất trong hệ thống"* — bất biến, không phải "thường là duy nhất") · TDS §5.2 *"Service vẫn kiểm trước để trả đúng mã MSG, nhưng **DB là chốt chặn cuối**"*

**Test Steps (Concurrency Testing):**
1. `CountDownLatch` cho 2 thread cùng `POST /api/v1/delivery-persons` với **cùng** `nationalId = "001077712345"`
2. Thả cả hai cùng lúc
3. Assert kết quả và DB

**Expected Result (PASS):**
- Đúng **1** request `201`, đúng **1** request `409 SUP-050`
- `SELECT COUNT(*) FROM delivery_persons WHERE national_id_hash = ?` == **1**
- Request thua cuộc nhận lỗi nghiệp vụ đúng mã, **không** phải `500 DataIntegrityViolationException` thô

**Expected Result (FAIL):**
- Cả hai `201` → check-then-act ở service không có ràng buộc DB đỡ lưng; BR-050 vỡ đúng lúc hệ thống có tải
- Request thua trả `500` → lỗi kỹ thuật lộ ra client, FE không map được thông điệp

**Current Status:** 🔴 Not written

---

### MF04-TC-INT-013 — `id` công khai là mã nghiệp vụ, không UUID (D1/L1)

**Severity:** `CRITICAL`
**Feature Under Test:** Toàn bộ endpoint MF04 (path param + JSON)
**Test File:** `src/test/java/com/sfms/partner/integration/PublicIdContractIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-024`, `TC-COND-017`
**Oracle Source:** **D1** — *"Path param dùng `code`: `GET /api/v1/incidents/INC-2026-014`; Khóa ngoại trong JSON dùng `code` của bảng đích: `"partnerId": "SUP-001"`, không phải UUID"* · `00_Common_Conventions.md` §D1 bảng "Việc phải làm": *"MF04 — sửa ADR-M04-1: path param dùng `code` thay UUID"*

**Test Steps:**
1. Gọi toàn bộ `GET` chi tiết bằng **mã**: `/partners/SUP-001`, `/supplier-documents/DOC-GF-01`, `/delivery-persons/DP-01`, `/partnership-applications/APP-2026-01`, `/supplier-partnerships/PTN-01`
2. Gọi bằng **UUID thật** của cùng bản ghi
3. Regex toàn bộ response body của mọi endpoint MF04

**Expected Result (PASS):**
- (1) `200` cho cả 5
- (2) `404` — UUID **không** phải định danh công khai
- (3) Không giá trị nào của field `id` hoặc `*Id` khớp regex UUID `[0-9a-f]{8}-[0-9a-f]{4}-...`
- `partnerId` trong mọi response = `"SUP-001"` (mã), khớp đúng `UserAccount.partnerId` và `CURRENT_SUPPLIER_ID` phía FE
- Kết hợp `MF04-TC-019`: không field ngày nào ở dạng `dd/MM/yyyy`

**Expected Result (FAIL):** lộ UUID → FE phải map 2 chiều mã↔UUID ở mọi màn; và vì `partnerId` là **khóa cách ly đa-tenant**, sót một chỗ map là rò dữ liệu chéo (D1 §Lý do mục 3).

**Current Status:** 🔴 Not written

---

### MF04-TC-INT-014 — Envelope lỗi D2 và thông điệp tiếng Anh nguyên văn D7

**Severity:** `HIGH`
**Feature Under Test:** `@RestControllerAdvice` (`common/`) áp cho mã lỗi MF04
**Test File:** `src/test/java/com/sfms/partner/integration/ErrorEnvelopeIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-025`
**Oracle Source:** **D2** (3 field, `details` *"mảng, có thể rỗng; **không bao giờ `null`**"*) · **D7** (*"backend trả `code` + `message` **tiếng Anh**, nguyên văn `error_message.md`"*) · `error_message.md` là nguồn chuỗi

**Test Steps (@ParameterizedTest — mỗi mã lỗi §10 của TDS):**

| Kịch bản | Mã | HTTP | Chuỗi đối chiếu |
|---|---|---|---|
| Không JWT | `SUP-004` | 401 | MSG-004 |
| **Sai vai trò** (`supplier` gọi `/approve`) | `SUP-005` | 403 | MSG-005 |
| Email trùng | `SUP-006` | 409 | MSG-006 |
| File > 10MB | `SUP-008` | 413 | MSG-008 |
| Sai định dạng file | `SUP-009` | 415 | MSG-009 |
| Cert hết hạn | `SUP-013` ⚠️ | 409 | MSG-013 |
| FRS lỗi | `SUP-014` ⚠️ | 502 | MSG-014 |
| MST sai | `SUP-007` ⚠️ | 422 | MSG-007 |
| Thiếu lý do từ chối | `SUP-048` | 422 | **MSG-113** |
| CCCD trùng | `SUP-050` | 409 | **MSG-117** |
| Đơn đang mở / tài liệu trùng `(partner,type,number)` | `SUP-409` | 409 | **MSG-118** |
| Không tìm thấy · **ngoài phạm vi dữ liệu** · gọi bằng UUID | `SUP-404` | 404 | **MSG-137** |
| **Validate thất bại** (thiếu field, sai regex) | `SUP-138` | 422 | **MSG-138** |
| **Client gửi `id` trùng (D5)** | `SUP-139` | 409 | **MSG-139** |
| **Vượt rate limit** | `SUP-141` | 429 | **MSG-141** |

> ⚠️ **Cập nhật 2026-08-02:** 4 dòng cuối bảng cũ ghi *(mã cục bộ)* — đã lỗi thời. Từ `error_message.md` **v1.5** (`SUP-048`→MSG-113, `SUP-050`→MSG-117, `SUP-409`→MSG-118, `SUP-404`→MSG-137), **mọi mã lỗi của MF04 đều có chuỗi nguồn** ⇒ assert byte-for-byte áp cho **toàn bộ 14 dòng**, không còn ngoại lệ nào (**M04-D09**). Ba dòng in đậm cuối là mã bổ sung hôm nay (M04-D06/D07/D08) — dùng MSG **có sẵn**, không cấp MSG mới.

**Expected Result (PASS):**
- Mọi response lỗi có đúng cấu trúc `{"error":{"code","message","details"}}`
- `details` là **array** kể cả khi rỗng — `assertThat(json.at("/error/details").isArray()).isTrue()`, không bao giờ `null`
- Với mã ánh xạ MSG: `message` khớp **byte-for-byte** chuỗi trong `error_message.md` (test đọc trực tiếp file nguồn để so, không hardcode)
- HTTP status khớp đúng cột trong TDS §10

**Expected Result (FAIL):** `details: null` → FE `data.error.details.map()` ném `TypeError`; hoặc message bị dịch/thêm chữ → lệch nguồn chuẩn, D7 vô hiệu.

**Current Status:** 🔴 Not written
**Implementation Note:** Test đọc `error_message.md` để lấy chuỗi kỳ vọng ⇒ khi Huy sửa nội dung MSG, test **tự** bắt lệch thay vì âm thầm sai.

---

### MF04-TC-INT-015 — Port 5 luật `semantic-audit.ts` chạm MF04

**Severity:** `HIGH`
**Feature Under Test:** `PartnerLookupPort` + schema-level invariants
**Test File:** `src/test/java/com/sfms/partner/integration/SemanticAuditRulesIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-028`
**Oracle Source:** `03_Design/08_FrontEnd/Web/scripts/semantic-audit.ts` — 5 luật; **`00_Team_Work_Assignment_Plan.md` §2a**: *"§13 Test Scenarios ← `semantic-audit.ts` — 54 luật port thành Gherkin"* · **ADR-M04-7** · ràng buộc **C6** `data_model.puml` (*"Truy xuất nối bằng KHÓA, không bằng tên"*)

```gherkin
Feature: Ràng buộc ngữ nghĩa quanh hồ sơ đối tác (port từ semantic-audit.ts)

  Scenario: MUA_HANG_SAU_KHI_GIAY_TO_HET_HAN (dòng 321-328)
    Given SUP-004 có chứng nhận ATTP hết hiệu lực 2026-06-22
    When MF06 tạo PurchaseRequest(supplierId=SUP-004, deliveryDate=2026-06-30)
    Then PartnerLookupPort.hasValidFoodSafetyCert("SUP-004") trả false
    And  đơn bị chặn (BR-046/BR-070)

  Scenario: NHAN_HANG_SAU_KHI_CHUNG_NHAN_HET_HAN (dòng 347-354, BR-070)
    Given phiên nhận hàng của SUP-004 với deliveryDate sau ngày hết hạn chứng nhận
    When MF06 chuyển phiên sang RECEIVED
    Then bị chặn bởi PartnerLookupPort.isApproved/hasValidFoodSafetyCert

  Scenario: PHIEU_GHI_SAI_TRANG_THAI_NCC (dòng 355-357) — chống sao chép trạng thái
    When truy vấn information_schema tìm cột trạng thái đối tác ngoài bảng partners
      """
      SELECT table_name, column_name FROM information_schema.columns
      WHERE column_name IN ('supplier_status','partner_status') AND table_name <> 'partners'
      """
    Then kết quả rỗng (ADR-M04-7 — partners là nguồn sự thật duy nhất)

  Scenario: CAPA_GIAO_NCC_KHONG_TON_TAI (dòng 254-258)
    When CorrectiveAction gán assigneeSupplierId = "SUP-999" (không tồn tại)
    Then bị từ chối bởi ràng buộc khóa ngoại / PartnerLookupPort.findById rỗng

  Scenario: NCC_LO_KHAC_NCC_PHIEU_NHAN (dòng 341-343) — nối bằng khóa, không bằng tên
    Then không bảng nào của MF04 lưu tên đối tác dạng chuỗi làm khóa nối
    And  mọi tham chiếu tới đối tác đều qua partner_id (UUID FK) / code (JSON)
```

**Expected Result (PASS):** cả 5 scenario xanh; đặc biệt scenario 3 và 5 chạy được **mà không cần MF06** (chỉ đọc `information_schema` + rà schema MF04).
**Expected Result (FAIL):** scenario 3 trả về cột `supplier_status` ở bảng khác → tái tạo đúng lớp lỗi mà `semantic-audit.ts` sinh ra để bắt: khóa đúng bảng nhưng **trỏ sai dòng / dữ liệu sao chép lệch nguồn**.

**Current Status:** 🔴 Not written
**Implementation Note:** Scenario 1–2, 4 phụ thuộc MF06/MF07 — nếu 2 module đó chưa có, chạy ở mức **port contract** (`PartnerLookupPort` trả đúng giá trị) và đánh dấu phần còn lại là cross-module test ở Phase 4 (`review_fix_plan.md` §Phase 4).

---

### MF04-TC-INT-016 — Không tồn tại cột dẫn xuất trong schema

**Severity:** `MEDIUM`
**Feature Under Test:** Flyway migration MF04
**Test File:** `.../SemanticAuditRulesIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-009`
**Oracle Source:** **ADR-M04-4 / ADR-M02-2** · `01_MVP_Backend_Implementation_Plan.md` §3 (*"**Không** lưu field dẫn xuất … tính runtime"*) · **AP-AI-009** (TDS §17.4)

**Test Steps:** sau khi Flyway migrate, truy vấn `information_schema.columns`.

**Expected Result (PASS):**
```sql
SELECT column_name FROM information_schema.columns
 WHERE table_name = 'partners' AND column_name IN ('cert_status','cert_expiry');        -- 0 rows
SELECT column_name FROM information_schema.columns
 WHERE table_name = 'partner_documents' AND column_name = 'status';                     -- 0 rows
SELECT column_name FROM information_schema.columns
 WHERE table_name = 'supplier_partnerships' AND column_name = 'doc_status';             -- 0 rows
```
Đồng thời `partner_documents` **có** cột `partner_id NOT NULL` và `partnership_applications` có `partner_id`, `school_id` (CF-M04-6).

**Expected Result (FAIL):** có cột `cert_status` → trạng thái đóng băng; tài liệu hết hạn hôm nay vẫn hiện `VALID` cho tới khi ai đó chạy job cập nhật, và BR-046 mất hiệu lực im lặng.

**Current Status:** 🔴 Not written

---

## 5. Red-Green-Refactor Tracker

> **45 test case** — 19 unit · 9 security · 17 integration *(gồm INT-011b)*. Cột 🟢 GREEN điền commit hash khi test xanh lần đầu.

| TC ID | Test File | 🔴 RED confirmed | 🟢 GREEN (commit) | 🔵 REFACTOR note |
|-------|-----------|-----------------|-------------------|------------------|
| `MF04-TC-001` | `service/DeliveryPersonServiceTest.java` | `[ ]` | | |
| `MF04-TC-002` | `dto/CreateDeliveryPersonRequestTest.java` | `[ ]` | | tách regex sang `common/validation` nếu MF03 dùng lại |
| `MF04-TC-003` | `service/DeliveryPersonPiiTest.java` | `[ ]` | | |
| `MF04-TC-004` | `domain/DocumentStatusCalculatorTest.java` | `[ ]` | | |
| `MF04-TC-005` | `service/PartnerApprovalServiceTest.java` | `[ ]` | | |
| `MF04-TC-006` | `service/PartnerApprovalServiceTest.java` | `[ ]` | | |
| `MF04-TC-007` | `service/DeliveryPersonBiometricTest.java` | `[ ]` | | |
| `MF04-TC-008` | `service/DeliveryPersonBiometricTest.java` | `[ ]` | | ✅ **hết phụ thuộc** — `ck_dp_face` giữ nguyên (M04-D03) |
| `MF04-TC-009` | `service/DeliveryPersonBiometricTest.java` | `[ ]` | | |
| `MF04-TC-010` | `domain/PartnerStatusTransitionTest.java` | `[ ]` | | cân nhắc tách FSM sang `common` nếu MF11 dùng chung |
| `MF04-TC-011` | `mapper/DerivedFieldTest.java` | `[ ]` | | |
| `MF04-TC-012` | `service/ComplianceDocumentServiceTest.java` | `[ ]` | | |
| `MF04-TC-013` | `service/ComplianceDocumentServiceTest.java` | `[ ]` | | |
| `MF04-TC-014` | `service/PartnershipApplicationServiceTest.java` | `[ ]` | | |
| `MF04-TC-015` | `service/PartnerRegistrationServiceTest.java` | `[ ]` | | |
| `MF04-TC-016` | `dto/SubmitApplicationRequestTest.java` | `[ ]` | | |
| `MF04-TC-017` | `service/ComplianceDocumentServiceTest.java` | `[ ]` | | thêm ArchUnit rule cấm `transferTo` |
| `MF04-TC-018` | `dto/EnumContractTest.java` | `[ ]` | | chuyển sang `common/enums` test khi Nam dựng xong |
| `MF04-TC-019` | `dto/DateFormatContractTest.java` | `[ ]` | | |
| `MF04-TC-SEC-001` | `security/PartnerTenantIsolationIT.java` | `[ ]` | | ⭐ rủi ro số 1 (`review_fix_plan.md` dòng 278) |
| `MF04-TC-SEC-002` | `security/PartnerRbacIT.java` | `[ ]` | | |
| `MF04-TC-SEC-003` | `security/PartnerTenantIsolationIT.java` | `[ ]` | | |
| `MF04-TC-SEC-004` | `security/FileUploadSecurityIT.java` | `[ ]` | | |
| `MF04-TC-SEC-005` | `security/PiiLeakIT.java` | `[ ]` | | |
| `MF04-TC-SEC-006` | `security/PartnerRbacIT.java` | `[ ]` | | |
| `MF04-TC-SEC-007` | `security/PartnerRegistrationSecurityIT.java` | `[ ]` | | |
| `MF04-TC-SEC-008` | `security/PartnerRbacIT.java` | `[ ]` | | |
| `MF04-TC-SEC-009` | `contract/OpenApiContractIT.java` | ⏸️ D8 | | `@Disabled` cho tới khi Nam approve D8 |
| `MF04-TC-INT-001` | `integration/PartnerApprovalIT.java` | `[ ]` | | |
| `MF04-TC-INT-002` | `integration/PartnerApprovalIT.java` | `[ ]` | | |
| `MF04-TC-INT-003` | `integration/PartnerSuspensionIT.java` | `[ ]` | | ✅ **hết blocked** (M04-D13) — publish `IamEvents.PartnerSuspended(partnerCode, occurredAt)` |
| `MF04-TC-INT-004` | `integration/PartnerSuspensionIT.java` | `[ ]` | | |
| `MF04-TC-INT-005` | `integration/PartnershipApplicationIT.java` | `[ ]` | | |
| `MF04-TC-INT-006` | `integration/PartnershipApplicationIT.java` | `[ ]` | | |
| `MF04-TC-INT-007` | `integration/PartnershipApplicationIT.java` | `[ ]` | | |
| `MF04-TC-INT-008` | `integration/ComplianceDocumentIT.java` | `[ ]` | | |
| `MF04-TC-INT-009` | `integration/ComplianceDocumentIT.java` | `[ ]` | | |
| `MF04-TC-INT-010` | `integration/ClientSuppliedIdIT.java` | `[ ]` | | |
| `MF04-TC-INT-011` | `integration/PagedContractIT.java` | `[ ]` | | dùng lại được cho 12 module — cân nhắc đưa lên `common` |
| `MF04-TC-INT-011b` | `integration/DeliveryPersonBiometricIT.java` | `[ ]` | | |
| `MF04-TC-INT-012` | `integration/DeliveryPersonBiometricIT.java` | `[ ]` | | |
| `MF04-TC-INT-013` | `integration/PublicIdContractIT.java` | `[ ]` | | dùng lại được cho 12 module (D1) |
| `MF04-TC-INT-014` | `integration/ErrorEnvelopeIT.java` | `[ ]` | | dùng lại được cho 12 module (D2) |
| `MF04-TC-INT-015` | `integration/SemanticAuditRulesIT.java` | `[ ]` | | scenario 1/2/4 chờ MF06/MF07 |
| `MF04-TC-INT-016` | `integration/SemanticAuditRulesIT.java` | `[ ]` | | |

### 5.1 Red Gate Protocol (CASE 2.0 — GATE-2)

> ⭐ Gate **quan trọng nhất**. Trước khi implement, chạy toàn bộ suite với stub ném lỗi. **Mọi test PHẢI FAIL.** Test nào PASS ngay → **AP-AI-002 detected** → reject và viết lại.

**Stub cho Red Phase (Java — KHÔNG dùng TypeScript như template):**

```java
// Red Phase — implementation stub (PHẢI throw)
@Service
public class PartnerApprovalServiceImpl implements PartnerApprovalService {
    @Override public PartnerResponse approve(String partnerCode, AuthUser actor) {
        throw new UnsupportedOperationException("Not implemented — Red Phase stub");
    }
    @Override public PartnerResponse reject(String partnerCode, String reason, AuthUser actor) {
        throw new UnsupportedOperationException("Not implemented — Red Phase stub");
    }
    // suspend / reinstate — tương tự
}
```

**Red Gate Verification:**

| TC ID | Stub Result | Expected | Actual | Root Cause (nếu PASS bất thường) |
|-------|-------------|----------|--------|----------------------------------|
| `MF04-TC-001` | `throw UnsupportedOperationException` | 🔴 FAIL | ☐ FAIL ☐ PASS | ☐ Tautology ☐ Shared state ☐ Hallucinated import |
| `MF04-TC-004` | `throw` | 🔴 FAIL | ☐ FAIL ☐ PASS | ☐ *(rủi ro cao: nếu assert chỉ so enum với chính nó)* |
| `MF04-TC-008` | `throw` | 🔴 FAIL | ☐ FAIL ☐ PASS | |
| `MF04-TC-018` | enum chưa tồn tại | 🔴 FAIL (compile) | ☐ FAIL ☐ PASS | ☐ Hallucinated contract |
| `MF04-TC-SEC-001` | controller chưa có `@PreAuthorize` | 🔴 FAIL | ☐ FAIL ☐ PASS | ☐ ★ *(rủi ro **tăng** sau M04-D10 — xem bẫy 1 bên dưới)* |
| `MF04-TC-INT-001` | `throw` | 🔴 FAIL | ☐ FAIL ☐ PASS | |
| `MF04-TC-INT-013` | DTO chưa có `@JsonProperty("id")` | 🔴 FAIL | ☐ FAIL ☐ PASS | |
| `MF04-TC-INT-016` | bảng chưa tạo | 🔴 FAIL (SQL) | ☐ FAIL ☐ PASS | |
| *(42 TC còn lại)* | `throw` / chưa có endpoint | 🔴 FAIL | ☐ | |

> ⚠️ **Bẫy riêng của MF04 — hai loại "PASS giả" phải phân biệt:**
> 1. **`MF04-TC-SEC-001`/`SEC-008` PASS vì endpoint chưa tồn tại.** ⚠️ **Bẫy này NGUY HIỂM HƠN sau M04-D10:** kỳ vọng mới là **404**, mà Spring trả **đúng 404** khi route chưa được khai ⇒ test **xanh ngay ở Red Gate** dù chưa có một dòng guard nào. Bắt buộc phân biệt bằng **`error.code`**:
>    - Route chưa tồn tại → 404 **không** có body envelope D2 (hoặc `error.code` khác `SUP-404`) ⇒ **RED hợp lệ**.
>    - Guard đã chạy → 404 + `error.code = "SUP-404"` + `message` = MSG-137 nguyên văn ⇒ GREEN.
>    Thêm **tiền đề bắt buộc**: cùng test phải assert route **có tồn tại** bằng một lần gọi hợp lệ với JWT **đúng chủ sở hữu** (`SUP-001`) trả **200** trước khi assert nhánh 404. Không có tiền đề này thì `SEC-001` là **AP-AI-002 (Green-from-Birth)** kinh điển.
>    Với `SEC-002`/`SEC-006` (kỳ vọng **403**/**401**) giữ nguyên cách cũ: assert **đúng mã** `403` + `error.code = "SUP-005"`, không assert `isNot2xx()`.
> 2. **`MF04-TC-INT-016` PASS vì bảng chưa tạo** (không có cột `cert_status` vì không có bảng nào). → Thêm tiền đề: assert bảng `partners` **tồn tại** trước khi assert cột không tồn tại.

**Red Gate Evidence:**
- Stub commit hash: `___`
- Tất cả FAIL? ☐ Yes → **GATE-2 PASS** (T2→T3) → tiếp tục implement
- Log: `target/red-gate-evidence.log` (sinh bằng `mvn test -Dsurefire.reportFormat=plain`)

---

## 6. Entry / Exit Criteria

### Entry Criteria (Điều kiện bắt đầu)

- [x] ✅ **TDS-MF04_Backend.md** (`SFMS-MF04-IMP-001`) **v1.1 Approved 2026-08-02**; 3 vai còn Pending (Huy/Minh/Thảo) — mục treo của họ ở §2.2 **không** đổi oracle của test nào
- [x] ✅ **Logic Issues §2 (L1–L13) đã chốt 2026-08-02** — xem §2.1; quyết định đầy đủ ở TDS §2.5
- [x] ✅ ~~**BLOCKER L11**~~ — **hết blocker (M04-D13)**: MF01 đã merge `user_accounts.partner_id UUID REFERENCES partners(id)` (`V1__init_schema.sql` dòng 141), claim JWT `partnerId` (`JwtService.CLAIM_PARTNER_ID`), `AuthUser.partnerId`, `AccountCascadeHandler.onPartnerSuspended()`. `INT-003/004` và `SEC-001/003` **chạy được**
- [x] ✅ **Nền chung của Nam đã có trong code**: `common/domain/BaseEntity` (D9) · `common/storage/{StorageService, LocalDiskStorageService, R2StorageService}` (D10) · `common/enums/{PartnerType, BiometricStatus, RoleKey}` (D6) · `common/dto/{PagedResponse, ErrorResponse}` (D2) · `common/error/GlobalExceptionHandler` · `common/code/CodeGenerator` (D1) · `common/reference/ReferenceResolver` (D1 — dịch mã↔UUID)
- [ ] **Migration MF04** (`V{n}__mf04_supplier_partner.sql`): **`ALTER TABLE partners`** (stub đã có ở `V1`) + `CREATE` 5 bảng còn lại — **không** sửa `V1` (đổi checksum, M04-D14). `pgcrypto` **đã bật** ở `V1` dòng 25. Seed `V{n+1}__seed_mf04.sql` với `partner_id`/`school_id` bổ sung và ngày ISO (FX-001…FX-008)
- [ ] Testcontainers chạy được trên máy dev + CI (Docker daemon khả dụng)
- [ ] `FaceRecognitionPortStub` theo ADR-000 đã có

### Exit Criteria (Điều kiện kết thúc — DoD)

- [ ] `mvn test` — **toàn bộ unit test xanh**, không `@Disabled` (trừ `MF04-TC-SEC-009` chờ D8)
- [ ] `mvn verify` — **toàn bộ integration test xanh** với Testcontainers PostgreSQL 16
- [ ] **Coverage ≥ 80% line** cho package `com.sfms.mf04` (JaCoCo) — `01_MVP_Backend_Implementation_Plan.md` §5 yêu cầu ≥1 happy + 1 error path cho module P1; TestSpec này vượt mức đó
- [ ] Không dùng kiểu `Object`/raw type trong DTO và service của MF04
- [ ] **CCCD và ảnh khuôn mặt không xuất hiện plaintext trong log** (`MF04-TC-SEC-005` xanh) — điều kiện DPO sign-off
- [ ] **25/25 UC** của traceability §1.4 có ít nhất 1 test case tham chiếu (bảng TDS-03 phủ hết)
- [ ] **8/8 BR** (BR-040/041/046/047/048/050/051/060) có test; **BR-060 test ở mức mã hóa dữ liệu sinh trắc học** — assert ảnh **không** chạm đĩa/DB và chỉ `faceId` được lưu (M04-D11) *(CF-M04-3 đã đóng 2026-08-02: BR-060 nay là AES-256, **không còn là GPS** — bỏ port GPS)*
- [ ] **5/5 luật `semantic-audit.ts`** chạm MF04 đã port (`MF04-TC-INT-015`)
- [ ] Đối chiếu Swagger: 23 endpoint §9.1 chạy được, JSON khớp hợp đồng mock (Minh xác nhận — Team Plan §7)

**Exit Criteria bổ sung — Bước 2b, sinh type FE** *(`02_Module_Completion_Workflow.md` §2)*:

- [ ] Chạy backend rồi `npm run gen:api` ở `04_Development/Frontend` → cập nhật `src/lib/api/schema.d.ts`; **commit cùng commit backend**
- [ ] `/v3/api-docs`: mọi endpoint danh sách MF04 (`/partners`, `/supplier-documents`, `/delivery-persons`, `/partnership-applications`, `/supplier-partnerships`) trỏ `PagedResponse{TênDto}` với `items` **đúng kiểu** — **không** phải `PagedResponse` trống
  > Lỗi này từng làm hỏng **cả 11 endpoint danh sách** của MF01–MF03: `@Schema(name = "PagedResponse")` ghim tên schema nên springdoc dùng chung một schema không tham số hoá. **Đừng đặt lại `name=`** cho schema generic (chú thích tại chỗ trong `PagedResponse.java`).
- [ ] Endpoint nào trả shape khác nhau theo vai trò phải khai `oneOf` (**D11** — mẫu: `StudentDtos.PagedResponseStudentResponse` / `SchoolDtos.PagedResponseSchoolResponse`)

**Exit Criteria bổ sung — CASE 2.0:**

- [ ] **Red Gate (§5.1)** — mọi test FAIL với stub trước khi implement; evidence log đính kèm
- [ ] **Contract Existence** — mọi import trong test resolve:
  ```bash
  mvn -q test-compile        # Expected: BUILD SUCCESS, không "cannot find symbol"
  ```
- [ ] **Props Isolation** — không shared mutable state:
  ```bash
  # Không được có field entity ở cấp class test (phải tạo trong @Test hoặc qua factory)
  grep -rn "^\s*private final Partner \|^\s*private static Partner " src/test/java/com/sfms/partner/
  # Expected: no output
  ```
- [ ] **Oracle Source** — mọi test case có trường `Oracle Source` trỏ BR/UC/MSG/D-decision cụ thể (48/48 — đã đủ ở §4)

**Exit Criteria bổ sung — D1–D10 (`00_Common_Conventions.md` §Checklist):**

- [ ] D1: `MF04-TC-INT-013` xanh — không UUID nào lộ ra JSON/path param
- [ ] D2: `MF04-TC-INT-014` xanh — envelope 3 field, `details` không null
- [ ] D4: `MF04-TC-019` xanh — không còn `dd/MM/yyyy` trong response
- [ ] D5: `MF04-TC-INT-010` xanh — client-supplied id hoạt động cả 2 nhánh
- [ ] D6: `MF04-TC-018` xanh — enum SCREAMING_SNAKE, `BiometricStatus` đủ 3 giá trị
- [ ] D9: `MF04-TC-INT-001` xanh — `created_by` do `AuditorAware` điền, service không set tay
- [ ] D10: `MF04-TC-017` xanh — upload đi qua `StorageService`
- [ ] *(D8 — chỉ khi Nam approve)* `MF04-TC-SEC-009` bật `@Disabled` → xanh

### Suspension Criteria (Điều kiện tạm dừng)

- ~~**L11 chưa xong**~~ — ✅ **không còn áp dụng từ 2026-08-02** (M04-D13): MF01 đã có `users.partner_id` + JWT claim + `AccountCascadeHandler`
- ~~`common/` chưa có `StorageService`/`BaseEntity`/`enums`~~ — ✅ **không còn áp dụng**: cả 3 đã có trong `04_Development/Backend/src/main/java/com/sfms/common/`
- Docker/Testcontainers không chạy được trên CI — tạm dừng toàn bộ nhóm `IT`, giữ nguyên nhóm unit
- Phát hiện mâu thuẫn mới giữa `00_Common_Conventions.md` và TDS → dừng, bổ sung vào §2 (Logic Issues) trước khi viết tiếp, **không** tự quyết

---

## 7. Rollback Plan

> ⚠️ Template gốc dùng `npx prisma migrate reset` — **sai stack**. MF04 dùng **Flyway + Maven** (Team Plan §2c-2, `01_MVP_Backend_Implementation_Plan.md` §3).

```bash
# 1. Hoàn tác migration MF04 (dev only — KHÔNG chạy trên production)
mvn flyway:undo
#    hoặc, nếu undo không khả dụng (Flyway Community):
psql -h localhost -U sfms -d sfms -f backup_20260802.sql

# 2. Testcontainers không cần dọn — container bị hủy sau mỗi lần chạy suite

# 3. Hoàn tác code
git checkout -- src/main/java/com/sfms/partner/ src/test/java/com/sfms/partner/
git checkout -- src/main/resources/db/migration/V*__seed_mf04.sql

# 4. Xác minh DB sạch
psql -h localhost -U sfms -d sfms -c "\dt partner*"     # Expected: không còn bảng MF04
```

**Không rollback được (ghi rõ để không hứa sai):**
- **Dữ liệu sinh trắc học đã xóa ở FRS** (UC-028) — rollback DB **không** khôi phục `faceId`; phải chạy lại UC-068 cho từng nhân viên
- **Email đã gửi** (MSG-028/MSG-037) — đối tác đã nhận thông báo duyệt/từ chối
- **File đã đẩy lên storage** — nếu `sfms.storage.provider=r2`, cần dọn thủ công theo prefix `partner-documents/`

**Sau rollback:** giữ nguyên trạng thái 🔴 của các TC trong §5; ghi lý do vào changelog §CHANGELOG, **không** xóa dòng cũ (Policy 4.4).

---

## 8. CASE 2.0 Anti-Pattern Detection (AI-Assisted TCs)

> Test case trong tài liệu này **có AI hỗ trợ** (§1.1) → checklist dưới đây **bắt buộc** cho reviewer (Thảo — Lead Test/QA).

| AP-ID | Anti-Pattern | Dấu hiệu trong TDD spec | Check | Gate chặn |
|-------|-------------|--------------------------|-------|-----------|
| AP-AI-001 | Unconstrained Generation | TC không reference BR/UC/MSG/D-decision nào ở trường `Oracle Source` | ☐ | G-0 |
| AP-AI-002 | Green-from-Birth | Test PASS với stub `throw` (§5.1) — **rủi ro cao ở `SEC-*` và `INT-016`**, xem 2 bẫy đã nêu | ☐ | G-2 ★ |
| AP-AI-003 | Implicit Decision | Test giả định quyết định kiến trúc không có trong TDS §3 hoặc `00_Common_Conventions.md` — vd tự đặt công thức `performanceScore` (CF-M04-4 nói **không có BR nào định nghĩa**) | ☐ | G-1 |
| AP-AI-004 | Layer Violation | Test kiểm business logic ở controller thay vì service | ☐ | G-4 |
| AP-AI-005 | Hallucinated Contract | Test import class/method không tồn tại (`FaceRecognitionPort`, `StorageService`, `PartnerLookupPort` phải khớp §8 TDS) | ☐ | G-3 |

**Kết quả review:**

- [ ] Không phát hiện anti-pattern nào → TDD spec approved
- [ ] Phát hiện AP → ghi vào bảng dưới → fix trước khi implement

| AP detected | TC ID | Mô tả | Fix action | Fixed? |
|------------|-------|-------|------------|--------|
| `AP-AI-___` | `TC-___` | | | ☐ |

### 8.1 ⚠️ Việc cần sửa ở tài liệu khác (phát hiện khi viết TestSpec này)

> Đây **không** phải anti-pattern của test, mà là lệch ở nguồn — ghi ra để chủ sở hữu tài liệu xử lý, **không** tự sửa file của người khác.

> ✅ **Cập nhật 2026-08-02:** F1/F3/F4/F5 **đã xử lý xong** (đều thuộc quyền chủ MF04). F2/F6/F7 **vẫn treo**, thuộc người khác — xem §2.2. Không xóa dòng nào (Policy 4.4), chỉ ghi kết quả vào cột cuối.

| # | Phát hiện | Ảnh hưởng | Ai xử lý |
|---|-----------|-----------|----------|
| **F1** | **`ck_dp_face` trong TDS-MF04 §5.2 cấm `PENDING_REGISTRATION` có `face_id`** — công thức hiện tại là `(REGISTERED AND face_id NOT NULL) OR (<> REGISTERED AND face_id IS NULL)`. Nhưng **D6** định nghĩa `POST /face` → `PENDING_REGISTRATION` (đã có `faceId` chờ xác thực) | `MF04-TC-008` không thể xanh nếu giữ nguyên ràng buộc → migration fail hoặc phải bỏ bước trung gian | ✅ **ĐÓNG 2026-08-02 (M04-D03) — Mạnh.** Kết luận **ngược** với đề xuất ban đầu: tiền đề *"PENDING đã có faceId"* **sai** — `faceId` do FRS cấp khi enroll xong (ADR-M04-5), nên ở `PENDING_REGISTRATION` **chưa có giá trị nào để lưu**. **Không nới** ràng buộc; chỉ siết thêm `biometric_registered_at IS NULL` ở nhánh ≠ REGISTERED và làm rõ ngữ nghĩa 3 nhánh ở TDS §5.2/§6.5. `TC-008` chạy được trên schema hiện tại |
| **F2** | **D10 bỏ sót MF04** khỏi danh sách module cần `StorageService` (*"MF03, MF07, MF08, MF10, MF11"*) dù MF04 có **2 luồng upload**: tài liệu tuân thủ (UC-101/102/091) và ảnh khuôn mặt (UC-068) | Nếu không bổ sung, MF04 dễ tự viết I/O → vỡ khi bật R2 ở prod | ⏳ **CÒN TREO — Nam** cập nhật `00_Common_Conventions.md` D10 (§2.2 P1). Không chặn: TDS §5.2/§9.2/§11.1/C7 **đã** ràng buộc MF04 dùng `StorageService` (M04-D11). ⚠️ Đính chính: MF04 có **1** luồng lưu trữ (tài liệu tuân thủ), **không phải 2** — ảnh khuôn mặt đẩy thẳng FRS, không ghi đĩa (ADR-M04-5/PDPD) |
| **F3** | **TDS-MF04 §9.2/§6.5 mô tả `NOT_REGISTERED → REGISTERED` trực tiếp** và chỉ khai 2 giá trị `biometricStatus` — đã bị **D6** ghi đè (3 giá trị + 2 bước) | TDS và TestSpec nói khác nhau ở cùng một luồng | ✅ **ĐÓNG 2026-08-02 (M04-D03) — Mạnh.** TDS §6.5 vẽ lại state machine 2 bước (bỏ cạnh `NOT_REGISTERED → REGISTERED` trực tiếp và cạnh *"đặt lịch đăng ký"*), §9.2 đã khai đủ 3 giá trị + bảng chuyển trạng thái |
| **F4** | **TDS-MF04 §9.2 dùng `?type=Raw%20Food%20Supplier`** và `"appliedAt": "28/06/2026"` — mâu thuẫn với chính §5.1 và với D4/D6 (`review.md` E1, A4) | FE gửi enum sai → filter im lặng trả rỗng | ✅ **ĐÓNG 2026-08-02 (M04-D02/D04) — Mạnh.** §9.2 dùng `?type=RAW_FOOD_SUPPLIER`; 3 chỗ `dd/MM/yyyy` cuối cùng (`documents[].expiry`, `issuedDate`/`expiryDate`, `submittedAt`) đã chuyển ISO; §16 ghi rõ `role` dùng `RoleKey` kebab-case |
| **F5** | **TDS-MF04 ADR-M04-1 (UUID path param)** đã bị **D1** ghi đè; §9.3.5 (*"FE đổi `{p.id}` → `{p.code}`"*) không còn cần | Nếu code theo TDS cũ, FE phải map 2 chiều mã↔UUID ở mọi màn — rủi ro rò dữ liệu đa-tenant | ✅ **ĐÓNG 2026-08-02 (M04-D01) — Mạnh.** ADR-M04-1 viết lại theo D1; §9.3 mục 5 gạch bỏ kèm ghi chú *"FE giữ nguyên `{p.id}`, không phải sửa gì"* để Minh không sửa nhầm; §9.2 hết ví dụ trả song song `id` UUID + `code` |
| **F6** | **`MSG-013`, `MSG-014`, `MSG-007`, `MSG-025` không nằm trong §1.4** của MF04 dù nội dung đúng nghiệp vụ MF04 và `error_message.md` §4 xếp MSG-013/014 vào module này (CF-M04-2) | 4 test case đang đánh dấu ⚠️; nếu Huy quyết định khác, phải đổi mã trong `MF04-TC-INT-014` | ⏳ **CÒN TREO — Huy** (Lead Traceability), §2.2 P2. Không chặn: test assert theo `error.code`, quyết định của Huy không làm phải viết lại test |
| **F7** | **`performanceScore` + ngưỡng 70%/2 tháng liên tiếp không có BR nào định nghĩa** (CF-M04-4) — UC-131/UC-148 mô tả cảnh báo, mock hiển thị badge, MSG-060 tồn tại, nhưng không có công thức | **Không viết test** cho phần này (sẽ là AP-AI-003 nếu bịa công thức). Tính năng cảnh báo hiệu suất **chưa kiểm thử được** | ⏳ **CÒN TREO — Huy/BA** bổ sung BR trước khi code, §2.2 P5. Quyết định giữ nguyên: **không viết test**, backend chỉ lưu và trả `performance_score`, không tự tính, không cảnh báo tự động (CF-M04-4) |

---

*TDD Spec v1.0 cho MF04 — tuân thủ `PHASE-4_Test-Spec.md` (ISO/IEC/IEEE 29119-3:2021) + CASE 2.0 Red Gate & Anti-Pattern Detection.*
*Ví dụ code viết bằng **Java 25 / JUnit 5 / Testcontainers / Flyway** — đã dịch từ mẫu TypeScript/Prisma của template theo ràng buộc stack `00_Team_Work_Assignment_Plan.md` §2c-2.*
