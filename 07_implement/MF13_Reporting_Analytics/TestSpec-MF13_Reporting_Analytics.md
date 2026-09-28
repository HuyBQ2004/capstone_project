# TEST SPECIFICATION — MF13 Reporting & Analytics

| Field | Value |
|-------|-------|
| **Document ID** | `SFMS-MF13-TEST-001` |
| **Version** | `1.1` |
| **Date** | `2026-07-30` (v1.0) · `2026-08-04` (v1.1 — Bước 1) |
| **Status** | `Approved` |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 — Software Testing Part 3: Test Documentation |
| **Author** | Nguyễn Văn Nam — Leader / Backend (chủ MF13) |
| **Reviewed by** | `[ ]` Nguyễn Phương Thảo — Lead Test/QA Reviewer — Pending · `[ ]` Nguyễn Đức Mạnh — Backend & DB — Pending · `[ ]` Bùi Quang Huy — Traceability — Pending · `[ ]` Trần Tuấn Minh — FE↔BE contract — Pending |
| **DPO Sign-off** | `[ ] Pending` — ⚠️ **bắt buộc**: BR-181 đòi báo cáo **không** chứa PII học sinh; file xuất mang nhãn `Confidential`. Đây là module duy nhất **xuất dữ liệu ra khỏi hệ thống** (file + email). |
| **Approved by** | Nguyễn Văn Nam — `2026-08-04` (chủ module + Final Approve theo RACI §1) |
| **Classification** | `Internal — Confidential` |

**References (thứ tự ưu tiên khi xung đột — cao xuống thấp):**

0. `07_implement/00_Common_Conventions.md` — **D1–D7 Approved 2026-07-30** (+ **D8** ⏳ chờ Nam approve, dùng cho lớp Contract §4.4). Mọi expected value về **định dạng** theo file này.
1. `07_implement/MF13_Reporting_Analytics/TDS-MF13_Backend.md` (`SFMS-MF13-IMP-001` v1.0) — **nguồn chính**: §13 (xương sống), §9 (API), §10 (mã lỗi `RPT-*`), §16 (auth matrix), §5.2 (DDL + CHECK + trigger), §3 (ADR-M13-1…7), §2.3 (CF-M13-1…9).
2. `02_Requirements/00_BusinessRules/business_rules.md` **§16 (BR-175…182)** — **oracle nghiệp vụ**.
3. `02_Requirements/00_BusinessRules/error_message.md` **v1.5** — nội dung MSG nguyên văn làm expected message (**MSG-004, MSG-005, MSG-137, MSG-138**).
4. `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` **§1.13** — mỗi test case truy về ≥1 UC ID (UC-171/172/173).
5. `03_Design/08_FrontEnd/Web/scripts/semantic-audit.ts` — **0 luật thuộc MF13** (xem §3 TDS-02 và Phụ lục C, kèm bằng chứng grep).
6. `03_Design/08_FrontEnd/Web/src/lib/mock-data.ts` + `mock-server.ts` — **chỉ** dùng làm nguồn SEED/FIXTURE (mã bản ghi thật) và để **đối chiếu điểm lệch có chủ đích**; **không** lấy định dạng ngày/enum/shape lỗi từ đây.
7. `07_implement/00_Team_Work_Assignment_Plan.md` §2b (hợp đồng phân trang/search/filter) · §2c-3b (prompt sinh tài liệu này).

> **Quy ước TDD:** Tài liệu này mô tả test case **TRƯỚC** khi viết production code.
> Thứ tự bắt buộc: viết test (JUnit 5) → chạy → xác nhận **FAIL 🔴** → implement → **PASS 🟢** → refactor 🔵. Không mark ✅ nếu `./mvnw verify` chưa xanh.
> **Stack test:** Java 25 LTS · JUnit 5 · Mockito · Spring Boot Test (`@WebMvcTest`/`@SpringBootTest`) · **Testcontainers PostgreSQL 16** · Flyway · `MockMvc` / REST Assured · **Apache PDFBox** (`PDFTextStripper` — đọc lại PDF vừa xuất) · **Apache POI** (đọc lại XLSX) · **GreenMail** (SMTP giả, thay Mailpit trong test) · JaCoCo.
> ⚠️ Template `PHASE-4_Test-Spec.md` minh họa bằng TypeScript/Prisma/`npm test` — **đã dịch toàn bộ sang Java/Maven/Flyway** theo ràng buộc stack (`00_Team_Work_Assignment_Plan.md` §2c-2). Không có `npx prisma`, không có `.spec.ts` trong tài liệu này.
> **Test data:** SYNTHETIC, seed từ `mock-data.ts`. Không dùng PII thật.

> ⭐ **Điểm khác biệt của MF13 so với 12 TestSpec còn lại:** MF13 là module **read-side + xuất bản**, nên trọng tâm test **không** phải "chặn ghi sai" mà là **ba nhóm rò rỉ/bịa số**:
> 1. **Rò PII học sinh** ra JSON/PDF/XLSX/email (BR-181) — lớp Security + `PdfTextAssert`.
> 2. **Rò dữ liệu trường khác** (BR-176) — lớp Security, gồm cả đường tải file theo mã đoán được.
> 3. **Số liệu bịa** — hằng số thay cho tính từ nguồn gốc (BR-182); mock **đang** làm đúng lỗi này (`rate: 100` viết cứng), nên test phải bắt được nếu ai copy mock.

---

## CHANGELOG

> **Policy 4.4 — Immutable History:** Không bao giờ xóa thông tin cũ.

| Ngày | Người thực hiện | Nội dung thay đổi |
|------|-----------------|-------------------|
| 2026-07-30 | Nguyễn Văn Nam | Khởi tạo (v1.0) — TestSpec cho MF13 theo prompt mẫu §2c-3b. Nguồn: `TDS-MF13_Backend.md` §5.2/§9/§10/§13/§16 + §2.3 (CF-M13-1…9), `00_Common_Conventions.md` D1–D8, `business_rules.md` §16, `error_message.md` v1.5, `traceability_matrix.md` §1.13, `mock-data.ts`/`mock-server.ts` (fixture + điểm lệch), `semantic-audit.ts` (xác nhận **0 luật MF13**). **37 test case**: 12 unit · 12 integration · 9 security · 4 contract (D8). Ghi nhận **11 Logic Issue** (§2), trong đó **4 ⛔ chặn GREEN** cần Nam/Huy chốt. |
| 2026-08-04 | Nguyễn Văn Nam | **v1.1 — Bước 1** (`02_Module_Completion_Workflow.md` §7), sau khi chạy Bước 0 trên code đã merge. **4/5 mục ⛔ đóng, 3 trong đó đóng bằng CODE chứ không bằng họp:** **L1** ⛔→✅ (`M13-D03` — `sent_reports` + `SentReportController` của MF12 đã merge ở `V23`, `CF-M11-20` đóng) · **L4** ⛔→✅ (`M13-D04` — MF02 đã chốt 31/07 và đang chạy: cross-school ⇒ **404**; siết `TC-SEC-003` về một nhánh) · **L3** ⛔→✅ (`M13-D08` — Nam chốt không cấp MSG mới; gỡ `__PENDING__`) · **L11** ⛔→✅ (`M13-D05` — 5 module nguồn đã merge, đối chiếu ra **7/7 view sai, 23 cột không tồn tại**; không còn gì để "Mạnh điều phối"). **L9** ✅ chấp nhận (TTL 240s, `M13-D09`). **L5** vẫn treo Huy nhưng **không chặn** (bật nhánh có sẵn là 1 dòng config). **Thêm 3 TC** theo §7 Bước 1 việc 3 + `M13-D12`: `MF13-TC-CON-005` (**D11**), `MF13-TC-INT-013` (**D12**), `MF13-TC-INT-014` (append-only × `BaseEntity`) ⇒ **40 test case**. ⚠️ **Đánh số `-013`/`-014` chứ không `-011`/`-012`** — hai ID đó **đã dùng** cho purge job (BR-177) và danh mục báo cáo (UC-171); trùng ID là lỗi im lặng, tracker §5 sẽ có 2 dòng cùng tên và một trong hai không ai chạy. Status → **Approved**. |

---

## MỤC LỤC

1. [Thông tin Module](#1-thông-tin-module) · 2. [Logic Issues Resolved](#2-logic-issues-resolved) · 3. [Test Design Specification](#3-test-design-specification) · 4. [Test Case Specification](#4-test-case-specification) · 5. [Red-Green-Refactor Tracker](#5-red-green-refactor-tracker) · 6. [Entry / Exit Criteria](#6-entry--exit-criteria) · 7. [Rollback Plan](#7-rollback-plan) · 8. [Anti-Pattern Detection](#8-case-20-anti-pattern-detection) · [Phụ lục A–D](#phụ-lục-a--danh-sách-uc--br--msg-đã-trích-dẫn)

---

## 1. Thông tin Module

| Field | Value |
|-------|-------|
| **Feature / Module ID** | `MF13` — Reporting & Analytics |
| **Module** | Reporting & Analytics (read-side + export) — Bounded Context |
| **Spec gốc** | `SFMS-MF13-IMP-001` (`TDS-MF13_Backend.md` v1.0) |
| **Priority** | 🟡 **P2** — làm song song nửa sau (`00_Team_Work_Assignment_Plan.md` §3), **nhưng** phụ thuộc dữ liệu của 7 module khác |
| **Sprint** | Ngày 4–5 theo lịch `00_Team_Work_Assignment_Plan.md` §4b (*"Nam: MF13"*) |
| **Milestone** | TDS Approved → 9 endpoint chạy qua Swagger → `/v3/api-docs` lưu kèm tài liệu nộp (D8) |
| **Data Classification** | `Confidential` — báo cáo tổng hợp vận hành/sự cố; **BR-181** bắt buộc ẩn danh |
| **Compliance Scope** | PDPD (ẩn danh BR-181) · lưu trữ ≥12 tháng (BR-177) |
| **Upstream Dependencies** | **MF01** (JWT `AuthUser{userId, role, schoolId}`; `user_accounts` cho slug `system-usage`) · **MF02** (`schools`, `school_config.report_emails`) · **MF04** (`partners`) · **MF06** (`receiving_sessions`, `inventory_batches`) · **MF07** (`inspections`, `kitchen_inspections`) · **MF08** (`food_samples`) · **MF10** (`NotificationPort`) · `common/` (`PagedResponse<T>`, `ErrorResponse`, `OpenApiConfig`) |
| **Downstream Consumers** | Người dùng cuối (file PDF/XLSX + email) · **MF12** (gọi `ReportExportPort` để dùng chung renderer BR-178 — ADR-M13-2) |
| **Số UC bao phủ** | **3/3** (traceability §1.13: UC-171, UC-172, UC-173) |
| **Số BR bao phủ** | **8/8** (BR-175, 176, 177, 178, 179, 180, 181, 182) |
| **KHÔNG thuộc phạm vi** | `sent_reports` + UC-185/187/188/189/190 (**MF12**, traceability §1.12 — xem **L1**/CF-M13-1) · `GET /system/usage` UC-150 (**MF12**) |

### 1.1 AI Generation Context (CASE 2.0)

| Field | Value |
|-------|-------|
| **AI Assisted?** | `Yes` |
| **Constraint Source** | `TDS-MF13_Backend.md` §13 (xương sống), §9, §10, §16, §5.2, §17.1 C1–C9 · `00_Common_Conventions.md` D1–D8 · `00_Team_Work_Assignment_Plan.md` §2b + §2c-3b |
| **Constraints Injected** | (1) Oracle **bắt buộc trích dẫn** BR/UC/MSG — không dùng giả định AI; thiếu oracle thì ghi rõ "thiếu oracle", **không bịa expected**. (2) Định dạng expected theo **D1–D7**, **không** theo mock (mock cố ý đóng băng — Team Plan §0). (3) Đủ **4 lớp**: Unit · Integration · Security · **Contract (D8, `/v3/api-docs`)**. (4) Error path đúng **envelope D2** + `code` khớp §10 TDS (`RPT-*`). (5) Bắt buộc test **"gọi API trực tiếp bỏ qua UI"** — với MF13 là `?schoolId=` giả, `?allSchools=true`, và đoán mã `RPX-…` của trường khác. (6) Tên luật `semantic-audit.ts` copy nguyên văn từ script (MF13: **0 luật**, khai bằng grep ở Phụ lục C). (7) **KHÔNG mượn MSG-091/092/096/097/098/099** — thuộc §1.12/MF07-MF12 (CF-M13-4). |
| **Model** | Claude Opus 5 (`claude-opus-5`) — theo §2c-4: TestSpec dùng Opus vì oracle sai là test vô nghĩa |
| **Trust Level** | `T2 → T3 (pending Red Gate §5.1)` |

---

## 2. Logic Issues Resolved

> **Bắt buộc điền trước khi viết test.** Liệt kê mọi sai lệch giữa spec thiết kế (TDS/mock) và hợp đồng đã chốt (D1–D8) hoặc oracle (BR/UC/MSG). **Test case encode hành vi ĐÃ SỬA**, không phải hành vi trong spec gốc.
>
> Cột **Trạng thái**: ✅ = giải quyết được bằng D1–D8/oracle, test encode hành vi đúng · ⛔ = **thiếu oracle / mâu thuẫn chưa chốt**, test viết ở dạng RED và **chặn GREEN** cho tới khi người phụ trách chốt.

| # | Spec gốc (sai / thiếu / mâu thuẫn) | Thực tế (D1–D8 / oracle) | Fix áp dụng trong test | Trạng thái | Người chốt |
|---|---|---|---|---|---|
| **L1** | **Ranh giới `sent_reports` chưa chốt (CF-M13-1).** `00_Team_Work_Assignment_Plan.md` §2c-0 dòng 427 ghi *"MF11 đã kết luận `sent-reports` thuộc **MF13**"*; nhưng traceability **§1.12** liệt UC-185/187/188/189/190, `TDS-MF12_Backend.md` **đã khai bảng `sent_reports`** (§5.2) + endpoint `POST /api/v1/reports/compliance/send`, và `TDS-MF07_Backend.md` (dòng 1650) cũng gán resource này cho **MF12**. | Quy tắc §2c: **traceability là nguồn chuẩn** ⇒ `sent_reports` thuộc **MF12**. TDS-MF13 §3 ADR-M13-2 chốt MF13 **không** sở hữu. | TestSpec này **không có test case nào** cho `sent_reports`/UC-185/189/190. Thêm **`MF13-TC-CON-004`** assert `/v3/api-docs` **không** có endpoint MF13 nào chạm `sent-reports` (chống việc gen trùng bảng khi 2 module cùng implement). | ✅ **ĐÓNG 2026-08-04 (`M13-D03`) — bằng CODE, không cần họp.** MF12 đã merge `sent_reports` (`V23:115`) + `mf12/domain/SentReport` + `mf12/web/SentReportController` (6 endpoint `/api/v1/sent-reports`). Kết luận v1.0 (theo traceability §1.12) **đúng** ⇒ §5.2/§9 của TDS **không phải sửa gì**. `CF-M11-20` đóng theo. `MF13-TC-CON-004` giữ nguyên và nay là **regression guard thật**, không còn là placeholder. | ~~Huy + Nam~~ → còn **1 việc dọn tài liệu**: Nam sửa `00_` §2c-0 dòng 427 (đang ghi ngược) |
| **L2** | **Xuất báo cáo cho kỳ RỖNG: chặn hay không?** Ba module khác **chặn** và có MSG riêng (MSG-092 kiểm tra bếp / MSG-096 sự cố / MSG-097 vi phạm) — nhưng cả ba thuộc **§1.12** và gắn BR-209/213/214, **không** phải BR của MF13. §1.13 chỉ gán MSG-004/005 cho UC-172. | **BR-178 nguyên văn** chỉ quy định *định dạng* và *4 field header*, **không** có điều khoản chặn khi rỗng. **BR-182** đòi hiển thị khoảng thời gian — vẫn làm được với 0 dòng. | `MF13-TC-INT-004` assert **201 + `rowCount: 0`** + file **vẫn có đủ 4 field header** BR-178. **Không** dùng MSG-092/096/097 (mượn mã sai module — vi phạm §10 TDS + AP-AI-016). | ✅ (test theo BR-178 nguyên văn) — cần **Huy xác nhận** không có yêu cầu chặn ẩn ở UC-172 | Huy (đối chiếu UC-172) |
| **L3** | **BR-179 "thông báo cho System Admin" không có MSG nào.** BR-179 nguyên văn: *"nếu vẫn thất bại sẽ **ghi nhận lỗi và thông báo cho System Admin**"*. `error_message.md` v1.5: không có mã cho thông báo này. | ⛔ **Thiếu oracle cho nội dung thông báo.** Phần **hành vi** thì có oracle đầy đủ: retry ≤3 (BR-179), ghi nhận lỗi (`report_schedule_runs.error_message`). | `MF13-TC-INT-009` assert **chỉ phần có oracle**: đúng **3** attempt (không có attempt 4), dòng cuối `status=FAILED`, `error_message` khác rỗng, `NotificationPort.notifySystemAdmins(...)` được gọi **đúng 1 lần** với `type="REPORT_SCHEDULE_FAILED"`. Nội dung/mã của thông báo để **`__PENDING__`** — ⛔ **không bịa**. | ✅ **ĐÓNG 2026-08-04 (`M13-D08`).** Nam chốt với tư cách chủ `error_message.md`: **không cấp MSG mới**. File đó đánh mã cho **thông điệp trả về người dùng qua API**; BR-179 thất bại 3/3 không có request nào đang chờ ⇒ là **notification nền** (`NotificationPort` MF10) + `report_schedule_runs.error_message` + audit. Cấp mã cho nó là mở rộng phạm vi `error_message.md` không có lý do. **Gỡ `__PENDING__`** khỏi `TC-INT-009`; assert đủ: 3 attempt, `FAILED`, `error_message` khác rỗng, `publish()` gọi đúng 1 lần. | ✅ Nam (đã chốt) |
| **L4** | **Cross-school: 403 hay 404?** TDS-MF13 §10 chốt **404 `RPT-404`** cho tài nguyên trường khác (*"không phân biệt 403/404 để không rò tồn tại"*) và **403 `RPT-005`** cho `allSchools=true` sai quyền. Nhưng `TDS-MF02_Backend.md` §16 ghi *"cross-school → **403** SCH-005"* — và `TestSpec-MF02` đã ghi nhận đúng mâu thuẫn này ở **L7** của nó, **chưa chốt toàn dự án**. | ⛔ **Mâu thuẫn xuyên module chưa chốt.** BR-176 chỉ nói *"giới hạn trong phạm vi trường"*, không quy định mã HTTP. `error_message.md` v1.5 có **cả** MSG-005 (403) và MSG-137 (404), đều hợp lệ. | `MF13-TC-SEC-003` viết **tham số hóa expected**: assert `status ∈ {403, 404}` **VÀ** (403 ⇒ `code="RPT-005"` + MSG-005 nguyên văn) **HOẶC** (404 ⇒ `code="RPT-404"` + MSG-137 nguyên văn) **VÀ** — phần **bắt buộc, không phụ thuộc quyết định** — response **không chứa** bất kỳ field dữ liệu nào của trường khác và **không** rò `fileUri`/`checksum`. Khi chốt xong thì siết về một nhánh. | ✅ **ĐÓNG 2026-08-04 (`M13-D04`) — bằng CODE.** Câu trả lời đã có từ **31/07**, trước cả ngày viết TestSpec này: `mf02/error/SchExceptions.java:73` *"404 · SCH-404 · MSG-137 — không tồn tại HOẶC cross-school (BR-022, chốt 2026-07-31)"*, và `:67` *"403 · SCH-005 — sai vai trò TRONG CÙNG trường"* — tức **L7 của TestSpec-MF02 đã đóng**, MF13 chỉ áp lại. §10/§16 của TDS-MF13 **vốn đã đúng**. ⇒ **siết `TC-SEC-003` về MỘT nhánh 404 `RPT-404` + MSG-137**, bỏ assert tham số hoá. | ✅ Nam (áp lại quyết định MF02 31/07) |
| **L5** | **CF-M13-3 — Service Caterers có BR nhưng không có UC.** BR-175 nguyên văn cho Service Caterers *"chỉ xem báo cáo liên quan đến hoạt động của mình (nhận hàng, kiểm tra, sự cố)"*; nhưng cột Actor của §1.13 (UC-171/172) **không** liệt họ. | ⛔ **Hai nguồn ngược nhau.** TDS §16 bám Actor §1.13 ⇒ Service Caterers = ❌ ở mọi endpoint MF13; `ReportAccessPolicy` có nhánh sẵn nhưng **tắt** ở MVP. | `MF13-TC-006` (bảng quyền) và `MF13-TC-SEC-001` (ma trận RBAC) assert Service Caterers → **403** theo §16 hiện hành, kèm chú thích ⛔: nếu Huy bổ sung Actor thì **3 ô** của ma trận đổi sang ✅ (`receiving`, `three-step`, `hygiene`, lọc theo `partnerId`). **Không** tự cấp quyền. | ⛔ **CHẶN** việc siết ma trận (test hiện hành chạy được, nhưng có thể phải sửa) | Huy (bổ sung Actor §1.13) |
| **L6** | **Mock trả số liệu VIẾT CỨNG (CF-M13-7a).** `mock-server.ts:286–291`: slug `sample-retention` có `rate: 100, alerts: 0` **hằng số**, không tính từ `db.samples`. | **BR-182 nguyên văn:** *"Dữ liệu báo cáo phải được tính toán **trực tiếp từ nguồn dữ liệu gốc**"*. | `MF13-TC-002` (unit) + `MF13-TC-INT-002` (integration) dùng fixture **48 mẫu, 2 quá hạn** ⇒ expected `rate = 96`, `alerts = 2`. Bất kỳ implement copy mock sẽ trả `100/0` → **FAIL**, đúng mục đích. Thêm grep ở Exit Criteria: `rate = 100` hằng số trong `com.sfms.mf13` → phải rỗng. | ✅ (test theo BR-182) | — (đã chốt ở TDS §17.1 C5) |
| **L7** | **Mock trả ngày `dd/MM/yyyy HH:mm` (CF-M13-7b).** `nowStamp()` (`mock-server.ts:107`) sinh `"30/07/2026 14:05"` cho `generatedAt`; `period` là chuỗi tiếng Việt cứng `"30 ngày gần nhất"`, không có `from`/`to` máy đọc được. | **D4:** JSON ngày/giờ **luôn ISO-8601** (`Instant` → `"2026-07-30T07:05:00Z"`, `LocalDate` → `"2026-07-30"`). UC-172 AF1 (*"export dùng bộ lọc hiện tại"*) cần `periodFrom`/`periodTo` máy đọc. | `MF13-TC-INT-003` assert `generatedAt` khớp regex `Instant` ISO-8601 UTC, `periodFrom`/`periodTo` khớp regex `LocalDate`, và **0 kết quả** cho regex `\d{2}/\d{2}/\d{4}` trên **toàn bộ** response body (gồm cả `periodLabel`). | ✅ (test theo D4) | — (đã chốt ở TDS §9.2) |
| **L8** | **FE lọc phạm vi trường bằng TÊN TRƯỜNG DẠNG CHUỖI (CF-M13-6).** `school-admin/reports/[slug]/page.tsx:207`: `filters={{ school: "TH Đoàn Thị Điểm" }}` — hằng số trong file `.tsx`, không phải `schoolId` từ JWT. | **BR-176** + TDS §17.1 C2: `schoolId` **PHẢI** đọc từ JWT, **không** tin body/query. | `MF13-TC-SEC-002` assert: gửi `?schoolId=<trường B>` **và** `?school=TH X` đều **bị bỏ qua** — dữ liệu trả về vẫn của trường trong JWT; `total` không đổi so với lần gọi không có param. | ✅ (test theo BR-176) + **cần Minh** đổi FE sang `GET /reports/{slug}/rows` | Minh (FE) |
| **L9** | **BR-180 "≤5 phút" vs cache TTL đúng 300s.** ADR-M13-6 chọn TTL = **300s**. Nếu một thay đổi vận hành xảy ra **ngay sau** lần tính, dữ liệu người dùng thấy có thể cũ tới **300s + thời gian truy vấn** ⇒ **vượt** 5 phút ở biên. | **BR-180 nguyên văn:** *"làm mới trong vòng **5 phút** kể từ khi có thay đổi vận hành"* — 300s là **trần**, không phải mục tiêu. | `MF13-TC-INT-005` assert `now - generatedAt ≤ 300s` (hợp đồng tối thiểu) **và** thêm assertion biên: sau khi publish event của module nguồn, lần gọi kế tiếp phải có `generatedAt` **mới** (evict theo event — TDS §7.2) ⇒ không phụ thuộc vào việc TTL chạm đúng biên. **Khuyến nghị:** hạ TTL xuống **240s** để có biên an toàn. | ✅ **CHẤP NHẬN 2026-08-04 (`M13-D09`) — TTL 300s → 240s.** Khuyến nghị của L9 đúng: BR-180 nói *"trong vòng 5 phút"* ⇒ 300s là **trần**, đặt TTL đúng bằng trần là chắc chắn vượt ở biên. ADR-M13-6 + §11.3 của TDS đã sửa. | ✅ Nam (đã chốt) |
| **L10** | **Không thể seed `report_exports` bằng Flyway.** ADR-M13-3 đòi mỗi bản ghi có **file thật + `checksum_sha256` thật**; `ck_rpx_retention` đòi `expires_at ≥ generated_at + 12 months`. Seed giả sẽ hoặc sai checksum, hoặc phải kèm file nhị phân trong migration. | TDS §11.2 đã ghi rõ: *"Không seed `report_exports` bằng dữ liệu giả"*. | Fixture `FX-040`/`FX-041` (bản xuất) **sinh qua API** trong `@BeforeEach` của IT (`POST /reports/{slug}/export`), **không** qua Flyway. Chỉ `report_schedules` được seed (`FX-042`). Ghi ở §3 TDS-05. | ✅ (điều chỉnh chiến lược fixture) | — |
| **L11** | **7 view `v_rpt_*` phụ thuộc tên cột của 7 module khác — chưa module nào implement.** VD view dùng `receiving_sessions.supplier_name`, `inventory_batches.storage_zone`, `user_accounts.role_key`, `user_accounts.last_login_at`; các TDS module nguồn có thể đặt tên khác (MF06 dùng `supplier` hay `supplier_name`?). | TDS §11.2 đặt bước *"đối chiếu tên cột 7 view với schema thật của module nguồn"* làm **pre-migration checklist**. ADR-M13-1: view vỡ ở CI là **cố ý**. | Thêm `MF13-TC-INT-001` (**★ gate hạ tầng**): assert **cả 7 view `SELECT` được** và trả đúng tập cột mà `ReportRowProjection` khai — chạy **đầu tiên** trong suite IT. Nếu FAIL thì 11 IT còn lại vô nghĩa ⇒ ghi vào Suspension Criteria §6. | ✅ **ĐÓNG 2026-08-04 (`M13-D05`) — và tệ hơn dự đoán.** **5 module nguồn đã merge xong** ⇒ không còn gì để "điều phối lịch"; chỉ còn **đọc file và sửa**. Kết quả đối chiếu: **7/7 view sai, 23 cột không tồn tại** — gồm **1 bảng sai tên** (`inspections` → `inspection_sessions`), **1 cột lọc trường không tồn tại** (`partners.school_id` — nguy hiểm nhất, xem `M13-D13`), 3 cột phải đổi thành JOIN, 2 cột phải tính dẫn xuất. TDS §5.2 đã viết lại; bảng đối chiếu ở **TDS §2.4-E**. `TC-INT-001` giữ vai trò ★ gate. | ~~Mạnh điều phối~~ → ✅ Nam (đọc schema đã merge) |

**Tổng hợp việc phải chốt trước khi chuyển GREEN — bản SAU Bước 1 (2026-08-04):**

| Cần chốt | Ai | Chặn | Kết quả |
|---|---|---|---|
| ~~Ranh giới `sent_reports` (MF12 hay MF13) — **L1**~~ | ~~Huy + Nam~~ | ~~Entry Criteria~~ | ✅ **`M13-D03`** — MF12 đã merge `V23`; không cần họp |
| ~~Nội dung/mã thông báo BR-179 thất bại 3/3 — **L3**~~ | ~~Nam + Huy~~ | ~~`TC-INT-009`~~ | ✅ **`M13-D08`** — không cấp MSG mới, là notification nền |
| ~~Cross-school = 403 hay 404 — **L4**~~ | ~~Nam + Huy~~ | ~~`TC-SEC-003`~~ | ✅ **`M13-D04`** — MF02 đã chốt **404** từ 31/07, MF13 áp lại |
| ~~Tên cột thật của 7 module nguồn cho `v_rpt_*` — **L11**~~ | ~~Mạnh~~ | ~~`TC-INT-001` ★~~ | ✅ **`M13-D05`** — 5 module nguồn đã merge; 7/7 view đã sửa |
| ~~Hạ cache TTL 300s → 240s — **L9**~~ | ~~Nam~~ | ~~không chặn~~ | ✅ **`M13-D09`** — chấp nhận |
| **Service Caterers có là Actor của UC-171/172 hay không — L5** | **Huy** | **KHÔNG chặn Approved** — `ReportAccessPolicy` đã có nhánh sẵn, bật là 1 dòng config; nếu Huy bổ sung Actor thì đổi **3 ô** của `TC-006`/`TC-SEC-001` | ⏳ treo |
| **Xác nhận UC-172 không có yêu cầu chặn ẩn khi kỳ rỗng — L2** | **Huy** | **KHÔNG chặn** — BR-178 nguyên văn không có điều khoản chặn (`M13-D10`) | ⏳ treo |
| **Minh nối FE — L8** (`CF-M13-6`: 7 `DataTable` + chuỗi tên trường cứng) | **Minh** | Bước 3, không chặn Bước 2 | ⏳ treo |

> **Đáng ghi lại: 4 trong 5 mục ⛔ đóng mà KHÔNG có cuộc họp nào.** Ba mục được trả lời bởi **code đã merge** (`M13-D03`, `M13-D04`, `M13-D05`), một mục bởi **chủ sở hữu tự quyết trong phạm vi của mình** (`M13-D08` — Nam giữ `error_message.md`). Đặc biệt **L4** treo từ `TestSpec-MF02` L7 với ghi chú *"chốt một lần cho cả 13 module"*: câu trả lời đã nằm trong `mf02/error/SchExceptions.java` từ **31/07**, tức **trước** ngày TestSpec-MF13 được viết. Ba mục còn treo đều là việc **của người khác** và **không mục nào chặn Bước 2**.

---

## 3. Test Design Specification

### TDS-01 — Scope / Phạm vi

```
MF13 (com.sfms.mf13) — các layer được kiểm thử:
├── domain/       ReportExport, ReportSchedule, ReportScheduleRun
│                 + enum ReportSlug / ReportFormat / ReportFrequency / ScheduleRunStatus
│                 → Unit: bất biến thuần (retention ≥12 tháng, attempt ≤3, slug kebab-case)
├── service/      ReportCatalogService, AnalyticsQueryService, ReportExportService,
│                 ReportScheduleService, ReportAccessPolicy, ReportRowProjection
│                 → Unit: mock port (Mockito) — kiểm BR-175/178/179/181/182 + mã lỗi RPT-*
├── render/       PdfReportRenderer, XlsxReportRenderer
│                 → Unit: 4 field header BR-178 + watermark Confidential BR-181
│                   (đọc lại file bằng PDFTextStripper / POI — không assert byte thô)
├── repository/   Spring Data JPA + 7 SQL view v_rpt_* + trigger append-only
│                 → Integration: Testcontainers — bất biến DB, phân trang §2b, view tồn tại
├── scheduler/    ReportScheduleRunner (@Scheduled + FOR UPDATE SKIP LOCKED)
│                 → Integration: retry ≤3, SKIP LOCKED, GreenMail nhận email
├── web/          ReportController, ReportExportController, ReportScheduleController
│                 → Integration: endpoint → DB, happy + error path, envelope D2
└── security/     @PreAuthorize + ReportAccessPolicy + cách ly schoolId từ JWT
                  → Security: 7 role × 9 nhóm endpoint, cross-school, PII leak, bypass UI
   + Contract:    /v3/api-docs (D8 — S1…S8)
```

**NGOÀI phạm vi** (khai rõ để không kỳ vọng sai):
- **Tính đúng đắn của dữ liệu nguồn** — MF13 phản ánh đúng những gì module nguồn ghi. Nếu `inspections` có bản ghi sai luật (VD `PHAT_SUAT_DU_KIEM_THUC_TRUOT`), MF13 **phải** báo cáo đúng bản ghi sai đó; test MF13 **không** assert luật ATTP. Xem Phụ lục C.
- **`sent_reports`, UC-185/187/188/189/190, `GET /system/usage` (UC-150)** — thuộc **MF12** (L1/CF-M13-1).
- **Phát hành/ký JWT** — thuộc **MF01**; MF13 chỉ **tin** `AuthUser`. Test MF13 **stub** JWT (`FX-020…FX-026`).
- **Gửi email thật** — dùng **GreenMail** in-process; Mailpit (ADR-000) chỉ dùng khi verify tay ở §11.4 của TDS.
- **Nội dung trình bày file** (font, layout, logo) — chỉ assert **4 field bắt buộc BR-178** + watermark BR-181, không assert pixel/định dạng thẩm mỹ.
- **`school-admin/dashboard`** tổng hợp phía client (CF-M13-9) — ngoài 3 UC của §1.13.

### TDS-02 — Test Basis / Cơ sở Kiểm thử

| Source | Items Derived |
|--------|--------------|
| `traceability_matrix.md` **§1.13** | 3 UC ID + Actor + cột BR + cột MSG — **nguồn duy nhất** cho ánh xạ UC↔BR↔MSG (đã đối chiếu: khớp 100% với TDS §2.1). Actor: UC-171 = School Administration / Medical Staff / System Admin · UC-172 & UC-173 = School Administration / System Admin |
| `business_rules.md` **§16** | **BR-175** (RBAC theo loại báo cáo, 4 nhóm role) · **BR-176** (phạm vi trường; chỉ System Admin đa trường) · **BR-177** (lưu ≥12 tháng + audit mỗi lần xuất) · **BR-178** (PDF + XLSX; 4 field header: tiêu đề, khoảng thời gian, tên trường, timestamp) · **BR-179** (daily/weekly/monthly; email theo cấu hình School Administration; retry ≤3 rồi báo System Admin) · **BR-180** (làm mới ≤5 phút) · **BR-181** (không PII học sinh; nhãn Confidential) · **BR-182** (tính từ nguồn gốc; không sửa sau khi tạo; hiện ngày giờ tạo + khoảng dữ liệu) |
| `error_message.md` **v1.5** | Error: **MSG-004** (401), **MSG-005** (403), **MSG-137** (404 — dùng chung), **MSG-138** (422/409 — dùng chung) → expected message lấy **nguyên văn tiếng Anh** (D7). ⚠️ **Không** dùng MSG-091/092/096/097/098/099 (§4 của file xếp vào nhóm "Food Safety Inspection", gắn BR-209/213/214 — CF-M13-4) |
| `00_Common_Conventions.md` | **D1** (`id` = mã nghiệp vụ `RPX-`/`RSC-`) · **D2** (envelope lỗi) · **D3** (`/api/v1`) · **D4** (ISO-8601) · **D5** (client `id` → 409) · **D6** (không khai lại 8 enum chung; `ReportSlug` kebab-case theo ADR-M13-5) · **D7** (message tiếng Anh) · **D8** (S1–S8 → lớp Contract §4.4) |
| `TDS-MF13_Backend.md` §5.2 | CHECK + trigger làm **assertion tầng DB**: `ck_rpx_retention` (≥12 tháng), `ck_rpx_format`/`ck_rsc_format` ∈ {PDF, XLSX}, `ck_rpx_period` (`from ≤ to`), `ck_rpx_slug`/`ck_rsc_slug` (7 giá trị kebab), `ck_rsc_weekly`/`ck_rsc_monthly`, `ck_rsr_attempt` (1..3), `uq_rpx_school_code`, `uq_rsc_school_code`, `uq_rsr_schedule_slot_attempt`, trigger `report_exports_no_update` |
| `TDS-MF13_Backend.md` §9 | 9 nhóm endpoint + request/response shape → expected của integration test; **8/9 là 🆕** (chỉ `GET /compliance-report` có trong mock) |
| `TDS-MF13_Backend.md` §10 | 6 dòng mã lỗi `RPT-004/005/404/410/138/409` → expected của error path |
| `TDS-MF13_Backend.md` §16 | Ma trận 7 role × 9 nhóm endpoint → `MF13-TC-SEC-001` (63 tổ hợp, pairwise qua `@ParameterizedTest`) |
| `TDS-MF13_Backend.md` §6.4/§6.5 | State machine `ReportScheduleRun` (PENDING→SUCCESS / PENDING→PENDING→FAILED) và `ReportExport` (GENERATED→PURGED, **không** có EDITED) → State Transition Testing |
| `TDS-MF13_Backend.md` §7.1 | 5 domain event (`ReportExported`, `ReportDownloaded`, `ReportScheduleChanged`, `ReportScheduleRunFailed`, `ReportExportPurged`) → assertion "event được publish" |
| `TDS-MF13_Backend.md` §3 | ADR-M13-1 (view + port) · -2 (không sở hữu `sent_reports`) · -3 (snapshot + checksum + retention) · -4 (whitelist ẩn danh) · -5 (slug kebab-case) · -6 (TTL 300s) · -7 (SKIP LOCKED + retry ≤3) |
| `00_Team_Work_Assignment_Plan.md` §2b | Hợp đồng phân trang (5 field, clamp `page`, `pageCount ≥ 1`), thứ tự áp bộ lọc, `SEARCH_FIELDS` của `report_exports` = `code, report_title, period_label`, 5 edge case §2b-6 |
| `mock-server.ts` | `GET /compliance-report` (dòng 264–316) — **hợp đồng path/method/tên field**; **3 điểm lệch có chủ đích** (L6 hằng số, L7 định dạng ngày, thiếu `periodFrom/To`) |
| `semantic-audit.ts` | ⚠️ **0 luật thuộc MF13** — xem **Phụ lục C** (khai báo bắt buộc, kèm bằng chứng grep 54 tên luật) |

### TDS-03 — Test Conditions and Coverage Items

| Condition ID | Test Condition | Oracle | Coverage Item | Test Cases |
|---|---|---|---|---|
| `TC-COND-001` | Chỉ 2 định dạng xuất: PDF, XLSX | BR-178 | `ReportExportService.export()`, CHECK `ck_rpx_format` | `MF13-TC-001`, `MF13-TC-INT-006` |
| `TC-COND-002` | Mọi số liệu tổng hợp tính từ nguồn gốc — 0 hằng số | BR-182 (L6) | `AnalyticsQueryService.complianceSummary()` | `MF13-TC-002`, `MF13-TC-INT-002` |
| `TC-COND-003` | File xuất có đủ 4 field header | BR-178 | `PdfReportRenderer`, `XlsxReportRenderer` | `MF13-TC-003`, `MF13-TC-INT-006` |
| `TC-COND-004` | File/JSON **không** chứa PII học sinh; có nhãn Confidential | BR-181, ADR-M13-4 | `ReportRowProjection`, renderer | `MF13-TC-004`, `MF13-TC-SEC-004`, `MF13-TC-SEC-005` |
| `TC-COND-005` | Hạn lưu trữ ≥ 12 tháng, không xoá sớm | BR-177 | `export()`, `ck_rpx_retention`, purge job | `MF13-TC-005`, `MF13-TC-INT-011` |
| `TC-COND-006` | Quyền theo **loại** báo cáo (role × slug) | BR-175 (L5) | `ReportAccessPolicy` | `MF13-TC-006`, `MF13-TC-SEC-001` |
| `TC-COND-007` | `nextRunAt` đúng theo frequency + zoneId; WEEKLY/MONTHLY bắt buộc trường tương ứng | BR-179 | `ReportScheduleService.computeNextRun()`, `ck_rsc_weekly/monthly` | `MF13-TC-007`, `MF13-TC-INT-008` |
| `TC-COND-008` | Retry tối đa 3 lần rồi báo System Admin | BR-179 (L3) | `ReportScheduleRunner`, `ck_rsr_attempt` | `MF13-TC-008`, `MF13-TC-INT-009` |
| `TC-COND-009` | Người nhận email **chỉ** từ `school_config.report_emails` | BR-179 | `ReportScheduleRunner` + `SchoolConfigPort` | `MF13-TC-009`, `MF13-TC-SEC-008` |
| `TC-COND-010` | Bản xuất **bất biến**; tải lại khớp checksum | BR-182, ADR-M13-3 | trigger `report_exports_no_update`, `download()` | `MF13-TC-010`, `MF13-TC-INT-007` |
| `TC-COND-011` | `report_slug` chỉ 7 giá trị kebab-case, JSON đúng kebab | ADR-M13-5, D6 | `enum ReportSlug`, `ck_rpx_slug` | `MF13-TC-011`, `MF13-TC-CON-003` |
| `TC-COND-012` | Danh mục báo cáo do API trả (đủ 7), lọc theo quyền | UC-171, BR-175 (CF-M13-7d) | `ReportCatalogService.listForCurrentUser()` | `MF13-TC-012`, `MF13-TC-INT-012` |
| `TC-COND-013` | 7 view `v_rpt_*` tồn tại + đúng tập cột | ADR-M13-1 (L11) | 7 SQL view | `MF13-TC-INT-001` ★ |
| `TC-COND-014` | Ngày/giờ JSON là ISO-8601; có `periodFrom`/`periodTo` | D4 (L7), BR-182 | mọi endpoint | `MF13-TC-INT-003`, `MF13-TC-CON-002` |
| `TC-COND-015` | Tuổi dữ liệu dashboard ≤ 300s; evict theo event | BR-180 (L9), ADR-M13-6 | `@Cacheable` + event listener | `MF13-TC-INT-005` |
| `TC-COND-016` | Kỳ rỗng → 201 + `rowCount=0`, không chặn | BR-178 (L2) | `export()` | `MF13-TC-INT-004` |
| `TC-COND-017` | Mọi `GET` danh sách trả đúng 5 field, `page` clamp, `pageCount ≥ 1` | Team Plan §2b-1/§2b-6 | `PagedResponse<T>` | `MF13-TC-INT-010` |
| `TC-COND-018` | `search` chỉ chạy trên `SEARCH_FIELDS` của `report_exports` | Team Plan §2b-3 | `ReportExportSpec` | `MF13-TC-INT-010`, `MF13-TC-SEC-009` |
| `TC-COND-019` | Client gửi `id` (`RSC-…`) trùng → 409, không ghi đè | D5 | `POST /report-schedules` | `MF13-TC-INT-008` |
| `TC-COND-020` | Mọi error path trả envelope D2, `details` không `null`, message tiếng Anh | D2, D7, §10 | `@RestControllerAdvice` | `MF13-TC-INT-006`, `MF13-TC-CON-001` |
| `TC-COND-021` | RBAC 7 role đúng ma trận §16 | BR-007, §16 | `@PreAuthorize` + policy | `MF13-TC-SEC-001` |
| `TC-COND-022` | `schoolId` đọc từ JWT, bỏ qua query/body | BR-176 (L8), §17.1 C2 | mọi controller | `MF13-TC-SEC-002` |
| `TC-COND-023` | Tài nguyên/`RPX-…` của trường khác không đọc/tải được ⇒ **404 `RPT-404`** | BR-176 · `M13-D04` | `findByCodeAndSchoolId`, `download()` | `MF13-TC-SEC-003`, `MF13-TC-SEC-006` |
| `TC-COND-024` | `allSchools=true` chỉ System Admin | BR-176 | `complianceSummary()` | `MF13-TC-SEC-007` |
| `TC-COND-025` | Thiếu/hết hạn JWT → 401 MSG-004 | BR-004, MSG-004 | JWT filter | `MF13-TC-SEC-001` (dòng cuối) |
| `TC-COND-026` | Không rò PII qua email đính kèm + log | BR-181 | `ReportScheduleRunner`, logging | `MF13-TC-SEC-005`, `MF13-TC-SEC-008` |
| `TC-COND-027` | Spec OpenAPI thoả D1/D2/D4/D6/S2/S7 | **D8** (S1–S8) | `/v3/api-docs` | `MF13-TC-CON-001`…`-004` |
| `TC-COND-028` | MF13 **không** khai endpoint nào chạm `sent-reports` | ~~L1~~ → ✅ `M13-D03` (MF12 đã merge `V23`) | `/v3/api-docs` | `MF13-TC-CON-004` |
| `TC-COND-029` 🆕 | `GET /reports/{slug}/rows` đổi **tập FIELD** theo vai trò ⇒ khai `oneOf`, `items` đúng kiểu | **D11** (§6) · BR-181 · S7 | `/v3/api-docs` + 2 tài khoản seed | `MF13-TC-CON-005` |
| `TC-COND-030` 🆕 | Field **dẫn xuất** (`code`/`nextRunAt`/`createdBy`) và field **sai kiểu** gửi lên đều KHÔNG được lưu | **D12** (§6) · TDS §9.2 | `report_schedules` | `MF13-TC-INT-013` |
| `TC-COND-031` 🆕 | `report_exports` append-only vẫn đúng khi entity `extends BaseEntity` | BR-182 · ADR-M13-3 · `M13-D02` | trigger DB | `MF13-TC-INT-014` |

**Ma trận truy vết ngược UC → test case** (đủ 3/3 UC của §1.13):

| UC | Test cases |
|---|---|
| **UC-171** View Analytics Dashboard | `MF13-TC-002`, `-004`, `-006`, `-012` · `MF13-TC-INT-001` ★, `-002`, `-003`, `-005`, `-010` · `MF13-TC-SEC-001`, `-002`, `-003`, `-004`, `-007`, `-009` · `MF13-TC-CON-001`, `-002`, `-003`, `-005` 🆕 |
| **UC-172** Export Report | `MF13-TC-001`, `-003`, `-004`, `-005`, `-010`, `-011` · `MF13-TC-INT-004`, `-006`, `-007`, `-010`, `-011`, `-014` 🆕 · `MF13-TC-SEC-005`, `-006` · `MF13-TC-CON-001`, `-002` |
| **UC-173** Schedule Automated Report | `MF13-TC-007`, `-008`, `-009` · `MF13-TC-INT-008`, `-009`, `-013` 🆕 · `MF13-TC-SEC-008` · `MF13-TC-CON-004` |

**Ma trận BR → test case** (đủ 8/8):

| BR | Test cases |
|---|---|
| BR-175 | `MF13-TC-006`, `MF13-TC-012`, `MF13-TC-INT-012`, `MF13-TC-SEC-001` |
| BR-176 | `MF13-TC-INT-002`, `MF13-TC-SEC-002`, `-003`, `-006`, `-007` |
| BR-177 | `MF13-TC-005`, `MF13-TC-INT-007`, `-011` |
| BR-178 | `MF13-TC-001`, `-003`, `MF13-TC-INT-004`, `-006` |
| BR-179 | `MF13-TC-007`, `-008`, `-009`, `MF13-TC-INT-008`, `-009`, `MF13-TC-SEC-008` |
| BR-180 | `MF13-TC-INT-005` |
| BR-181 | `MF13-TC-004`, `MF13-TC-SEC-004`, `-005`, `-008` |
| BR-182 | `MF13-TC-002`, `-010`, `MF13-TC-INT-002`, `-003`, `-007` |

### TDS-04 — Test Techniques / Kỹ thuật Kiểm thử

| Technique (ISO 29119-4) | Applied To | Rationale |
|---|---|---|
| **Equivalence Partitioning** | `format` (PDF / XLSX / khác) · `frequency` (DAILY / WEEKLY / MONTHLY) · `slug` (7 hợp lệ / ngoài danh mục) · role (4 nhóm của BR-175 + 3 role không có quyền) | Miền đầu vào rời rạc, có danh mục đóng ở BR-178/179 và ADR-M13-5 |
| **Boundary Value Analysis** | `expiresAt - generatedAt` ∈ {11 tháng 29 ngày, **12 tháng**, 24 tháng} · `attempt` ∈ {1, 3, **4**} · `dayOfWeek` ∈ {0,1,7,8} · `dayOfMonth` ∈ {0,1,28,29} · `pageSize` ∈ {-1,0,1} · `page` > `pageCount` · tuổi cache ∈ {299s, **300s**, 301s} | BR-177 (≥12 tháng) và BR-179 (≤3 lần) là **ràng buộc số** — off-by-one ở đây nghĩa là xoá dữ liệu sớm hoặc spam email lần 4 |
| **State Transition Testing** | `ReportScheduleRun`: PENDING→SUCCESS · PENDING→PENDING(≤3)→FAILED · **FAILED không tự retry** (§6.4) · `ReportExport`: GENERATED→PURGED, **không** có EDITED (§6.5) | BR-179/182 là luật về **chuyển trạng thái**; phải test cả transition **không hợp lệ** (attempt 4, `UPDATE` bản xuất) |
| **Decision Table Testing** | `ReportAccessPolicy.canView/canExport`: (role) × (slug) × (cùng trường?) × (`allSchools`?) | BR-175 + BR-176 là 4 điều kiện độc lập cùng quyết định 1 kết quả; bảng quyết định tránh bỏ sót tổ hợp — đây là chỗ dễ cấp quyền quá tay nhất |
| **Combinatorial (pairwise) Testing** | Ma trận **7 role × 9 nhóm endpoint** = 63 tổ hợp (`MF13-TC-SEC-001`) | Sinh từ bảng §16 bằng `@ParameterizedTest` + `@MethodSource`, không viết tay 63 method |
| **Data Flow / Taint Testing** | Đường đi của PII: view → `ReportRowProjection` → JSON / PDF / XLSX / email / log (`MF13-TC-SEC-004`, `-005`, `-008`) | BR-181 là ràng buộc **trên đường dữ liệu**, không trên một hàm; phải kiểm ở **mọi** cửa ra, kể cả log và email đính kèm |
| **Concurrency Testing** | 2 `ReportScheduleRunner` tick cùng slot (`MF13-TC-INT-009` scenario 3) | ADR-M13-7 chọn `FOR UPDATE SKIP LOCKED` **vì** race này; không test thì quyết định kiến trúc không được xác minh — và hệ quả là **gửi email trùng cho phụ huynh/BGH** |
| **Error Guessing** | Đoán mã `RPX-2026-001` của trường khác · `?schoolId=` giả · `?school=Tên trường` (copy hằng số FE) · `recipients` trong body · SQLi trong `search` · `slug=../../etc/passwd` · tải bản đã purge | Nhóm lỗi mà review 29/07 xếp nghiêm trọng nhất: gate chỉ ở client thì gọi API trực tiếp lách được — với MF13 hệ quả là **rò dữ liệu**, không chỉ sai nghiệp vụ |
| **Contract Testing (D8)** | `/v3/api-docs` — 4 test case §4.4 | Spec sinh từ code là **nguồn sinh type FE** (`review_fix_plan.md` T3.4); sai spec ⇒ lệch D1/D2/D4/D6 quay lại qua file sinh tự động, khó thấy hơn |

### TDS-05 — Test Data Requirements

> **Nguồn:** `mock-data.ts` — dùng **mã bản ghi thật** để test chạy trên dữ liệu có thật (`SCH-HN-0042`, `SMP-20260706-01`, `RPTS-2026-001`…).
> ⚠️ **KHÔNG** lấy định dạng ngày / giá trị enum từ mock (Team Plan §0 — mock cố ý đóng băng). Cột "Giá trị (đã chuẩn hóa D1–D7)" là giá trị **sau** bước chuyển đổi khi sinh Flyway seed.
> ⚠️ **L10:** `report_exports` **không** seed bằng Flyway (đòi file thật + checksum thật + `ck_rpx_retention`) — sinh qua API trong `@BeforeEach`.

**A. Trường học + cấu hình** (`schools`, `school_config` — thuộc MF02, MF13 chỉ đọc):

| Fixture ID | Type | Giá trị (đã chuẩn hóa D1–D7) | Mục đích |
|---|---|---|---|
| `FX-001` | Flyway seed | `code="SCH-HN-0042"`, name `"TH Đoàn Thị Điểm"`, `status=ACTIVE` | Trường chính — chủ thể của mọi JWT School Admin |
| `FX-002` | Flyway seed | `code="SCH-HN-0051"`, `status=ACTIVE` | **Trường thứ hai** — cách ly cross-school (`SEC-003`, `SEC-006`), BR-176 |
| `FX-003` | Flyway seed | `school_config(FX-001).report_emails = ["toanpq@dtd.edu.vn","hant@dtd.edu.vn"]` | Người nhận BR-179 (`INT-009`, `SEC-008`) |
| `FX-004` | Flyway seed | `school_config(FX-002).report_emails = ["admin@sch51.edu.vn"]` | Chứng minh người nhận lấy **theo trường**, không dùng chung |

**B. Dữ liệu nguồn cho 7 báo cáo** (thuộc MF01/04/06/07/08 — MF13 chỉ đọc qua `v_rpt_*`):

| Fixture ID | Bảng nguồn | Giá trị | Mục đích |
|---|---|---|---|
| `FX-010` | `inspections` (MF07) | **25** phiên của `FX-001` trong 01–30/07/2026: **23** `overall=PASSED`, **2** `FAILED` | slug `three-step`: expected `rate=92`, `alerts=2`, `total=25` (BR-182) |
| `FX-011` | `food_samples` (MF08) | **48** mẫu: **46** `RETAINED/DISPOSED` đúng hạn, **2** quá hạn lưu | slug `sample-retention`: expected `rate=96`, `alerts=2` — **bắt lỗi hằng số 100 của mock** (L6) |
| `FX-012` | `receiving_sessions` (MF06) | **17** phiên: **15** không `REJECTED`, **2** `REJECTED` | slug `receiving`: expected `rate=88`, `alerts=2` |
| `FX-013` | `partners` (MF04) | **10** đối tác: **8** `certStatus=VALID`, **1** `URGENT`, **1** `EXPIRED` | slug `supplier-certificates`: expected `rate=80`, `alerts=1` |
| `FX-014` | `kitchen_inspections` (MF07) | **10** phiên: **9** `PASSED`, **1** `FAILED` | slug `hygiene`: expected `rate=90`, `alerts=1` |
| `FX-015` | `inventory_batches` (MF06) | 12 lô của `FX-001` + **3 lô của `FX-002`** | slug `traceability` + kiểm cách ly BR-176 |
| `FX-016` | `user_accounts` (MF01) | 14 tài khoản `FX-001` (7 role) + 5 tài khoản `FX-002` | slug `system-usage` + kiểm `?school=` bị bỏ qua (L8) |
| `FX-017` | `students` (MF03) | 3 HS có `code` `"HS-3A-001"`, tên thật trong seed, kèm dị ứng `SEVERE` | ⚠️ **Fixture "mìn"** — dùng để **quét PII** ở `SEC-004`/`-005`: các mã/tên này **không được** xuất hiện ở bất kỳ cửa ra nào của MF13 (BR-181) |
| `FX-018` | `inspections` (MF07) | 1 phiên của `FX-002` cùng ngày với `FX-010` | Bảo đảm test cách ly không "xanh giả" vì trường B rỗng |

**C. Dữ liệu của chính MF13:**

| Fixture ID | Type | Giá trị | Mục đích |
|---|---|---|---|
| `FX-030` | Flyway seed (`report_schedules`) | `code="RSC-2026-001"`, `schoolId=FX-001`, `reportSlug=hygiene`, `frequency=WEEKLY`, `format=XLSX`, `dayOfWeek=1`, `timeOfDay=07:30`, `zoneId="Asia/Ho_Chi_Minh"`, `active=true`, `nextRunAt` = quá khứ | Lịch **đến hạn** cho `INT-009` (runner nhặt ngay) |
| `FX-031` | Flyway seed (`report_schedules`) | `code="RSC-2026-002"`, `schoolId=FX-002`, `reportSlug=receiving`, `frequency=DAILY`, `active=false` | Lịch **tắt** + **trường khác** — chứng minh runner không chạy lịch `active=false` và không trộn trường |
| `FX-040` | **Sinh qua API** (`@BeforeEach`) | `POST /reports/sample-retention/export {format:"PDF"}` với JWT `FX-021` → `RPX-2026-001` | Bản xuất của `FX-001` — dùng cho `INT-007` (download + checksum), `SEC-006` |
| `FX-041` | **Sinh qua API** | Cùng lời gọi nhưng JWT của **trường B** (`FX-025`) → `RPX-…` của `FX-002` | Đích của `SEC-006` (đoán mã bản xuất trường khác) |
| `FX-042` | SQL trực tiếp (chỉ `INT-011`) | Bản ghi `report_exports` với `generated_at = now() - 25 tháng`, `expires_at = now() - 1 tháng`, file giả trong `./data/uploads` | Test purge job (BR-177) — **chỉ** insert trong test, không seed Flyway (L10) |

**D. JWT stub (7 role — do MF01 phát, MF13 chỉ tin):**

| Fixture ID | Role (`RoleKey`, D6) | `schoolId` | Dùng ở |
|---|---|---|---|
| `FX-020` | `system-admin` | *(không giới hạn)* | `SEC-001`, `SEC-007` (`allSchools=true` → 200) |
| `FX-021` | `school-admin` | `FX-001` | Happy path toàn bộ; `SEC-007` (`allSchools=true` → 403) |
| `FX-022` | `medical-staff` | `FX-001` | `SEC-001` — xem được dashboard, **không** xuất được (Actor §1.13) |
| `FX-023` | `service-caterers` | `FX-001` | `SEC-001` — ❌ mọi endpoint (⛔ L5) |
| `FX-024` | `teacher` · `FX-024b` `parent` · `FX-024c` `supplier` | `FX-001` | `SEC-001` — ❌ mọi endpoint |
| `FX-025` | `school-admin` | **`FX-002`** | `SEC-003`, `SEC-006` — cross-school |
| `FX-026` | *(token hết hạn / thiếu)* | — | `SEC-001` dòng cuối → 401 MSG-004 |

**Fixture builder (Props Isolation — bắt buộc, CASE 2.0):**

```java
// src/test/java/com/sfms/mf13/fixture/Mf13Fixtures.java
// Mọi instance tạo TRONG @Test. KHÔNG static mutable — xem Exit Criteria §6.
public final class Mf13Fixtures {

    public static ReportSchedule schedule(Consumer<ReportSchedule> custom) {
        ReportSchedule s = new ReportSchedule();
        s.setCode("RSC-2026-001");
        s.setSchoolId(FX_001);
        s.setReportSlug(ReportSlug.HYGIENE);
        s.setFrequency(ReportFrequency.WEEKLY);
        s.setFormat(ReportFormat.XLSX);
        s.setDayOfWeek((short) 1);
        s.setTimeOfDay(LocalTime.of(7, 30));
        s.setZoneId("Asia/Ho_Chi_Minh");
        s.setActive(true);
        custom.accept(s);                 // chỉ override cái test cần
        return s;
    }

    /** Hàng dữ liệu nguồn CÓ PII — dùng để chứng minh projection loại được (BR-181). */
    public static Map<String, Object> tantedRow() {
        return new LinkedHashMap<>(Map.of(
            "id", "SMP-20260706-01", "dish", "Canh bí đỏ", "meal", "Trưa 06/07",
            "studentId", "HS-3A-001",                 // ⚠️ PII — phải bị loại
            "studentName", "Nguyễn Minh Anh",         // ⚠️ PII — phải bị loại
            "status", "RETAINED"));
    }

    /** Danh sách chuỗi PII dùng để quét mọi cửa ra (SEC-004/-005/-008). */
    public static List<String> piiNeedles() {
        return List.of("HS-3A-001", "Nguyễn Minh Anh", "studentId", "studentName",
                       "parentName", "diagnosis");
    }
}
```

---

## 4. Test Case Specification

> **TC ID format:** `MF13-TC-[NNN]` (Unit) · `MF13-TC-INT-[NNN]` (Integration) · `MF13-TC-SEC-[NNN]` (Security) · `MF13-TC-CON-[NNN]` (**Contract — D8**)
> **Severity:** CRITICAL / HIGH / MEDIUM / LOW
> **Status:** 🔴 Not written · 🟡 Written-failing · 🟢 Passing
> **Oracle Source:** ⭐ **bắt buộc** — expected value đến từ đâu (BR/UC/MSG/D1–D8). Không dùng giả định AI. Ghi ⛔ nếu thiếu oracle.
> **Expected message:** nguyên văn **tiếng Anh** từ `error_message.md` v1.5 (**D7**). **Expected envelope:** `{"error":{"code","message","details"}}` (**D2**), `details` là mảng — **không bao giờ `null`**.

### 4.1. UNIT TEST CASES — service / render layer (Mockito, không DB)

---

#### `MF13-TC-001` — BR-178: chỉ chấp nhận 2 định dạng xuất PDF / XLSX

**Severity:** `HIGH`
**Feature Under Test:** `ReportExportService.export()`
**Test File:** `src/test/java/com/sfms/mf13/service/ReportExportFormatTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-001`
**Oracle Source:** **BR-178** nguyên văn — *"Hệ thống hỗ trợ xuất báo cáo theo hai định dạng: **PDF** và **Excel (XLSX)**"* · **MSG-138** → `RPT-138`, HTTP 422 (TDS §10) · **UC-172** (traceability §1.13)
**Technique:** Equivalence Partitioning

**Preconditions:** mock `ReportDataPort.rows()` → 5 hàng hợp lệ; `SchoolConfigPort.schoolName(FX-001)` → `"TH Đoàn Thị Điểm"`

**Test Steps:**
1. **Arrange:** `format` tham số hóa
2. **Act:** `reportExportService.export(SAMPLE_RETENTION, format, from, to, filters)`
3. **Assert:** theo bảng lớp tương đương

| `format` | Lớp tương đương | Expected |
|---|---|---|
| `PDF` | hợp lệ | tạo bản xuất, `format="PDF"`, `fileUri` kết thúc `.pdf` |
| `XLSX` | hợp lệ | tạo bản xuất, `format="XLSX"`, `fileUri` kết thúc `.xlsx` |
| `CSV` | ngoài danh mục | `ValidationException`, `code="RPT-138"`, HTTP 422 |
| `csv` (chữ thường) | sai hoa/thường | `ValidationException`, `code="RPT-138"` — **không** tự chuẩn hoá về `CSV` rồi mới báo lỗi |
| `null` / rỗng | thiếu | `ValidationException`, `code="RPT-138"`, `details[0].field="format"` |

**Expected Result (PASS — hành vi đúng):**
- `PDF`/`XLSX` → `ReportExportResponse` với `id` khớp `^RPX-\d{4}-\d{3}$` (**D1**)
- Còn lại → `code="RPT-138"`, `message="Validation failed: one or more required fields are missing or invalid."` (**MSG-138 nguyên văn**, D7), `details=[{"field":"format","value":<giá trị đã gửi>}]`
- **Không** gọi renderer, **không** ghi DB: `verify(pdfRenderer, never()).render(any())`, `verify(reportExportRepository, never()).save(any())`

**Expected Result (FAIL — dấu hiệu lỗi):**
- `CSV` được nhận và renderer xuất ra file `.csv` → vi phạm BR-178 (file rời hệ thống ở định dạng không được duyệt)
- Trả 500 `DataIntegrityViolation` vì chỉ dựa vào CHECK `ck_rpx_format` ở DB → sai mã lỗi, FE không map được nhãn

**Current Status:** 🔴 Not written
**Implementation Note:** validate ở service **trước** khi render; CHECK `ck_rpx_format` (§5.2) là chốt chặn cuối, không phải nơi kiểm chính. Dùng `enum ReportFormat` để springdoc tự sinh schema (**D8/S5**) — **không** `@Schema(allowableValues=...)`.

---

#### `MF13-TC-002` — ★ BR-182: mọi chỉ số tính từ nguồn gốc, KHÔNG hằng số

**Severity:** `CRITICAL`
**Feature Under Test:** `AnalyticsQueryService.complianceSummary()`
**Test File:** `src/test/java/com/sfms/mf13/service/ComplianceSummaryComputationTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-002`
**Oracle Source:** **BR-182** nguyên văn — *"Dữ liệu báo cáo phải được tính toán trực tiếp từ **nguồn dữ liệu gốc** trong hệ thống"* · **UC-171** · Logic Issue **L6** (mock `mock-server.ts:286–291` trả `rate:100, alerts:0` **hằng số** cho `sample-retention`)
**Technique:** Equivalence Partitioning + Error Guessing (chống copy mock)

**Preconditions:** mock `ReportDataPort.complianceModules()` trả số **thô** theo `FX-010`…`FX-014`

**Test Steps:**
1. **Arrange:** `FX-011` = 48 mẫu, 2 quá hạn; `FX-010` = 25 phiên, 2 FAILED; `FX-012` = 17 phiên, 2 REJECTED; `FX-013` = 10 đối tác, 1 URGENT; `FX-014` = 10 phiên bếp, 1 FAILED
2. **Act:** `complianceSummary(from=2026-07-01, to=2026-07-30, allSchools=false)`
3. **Assert:** từng nhóm

| slug | `total` | `rate` | `alerts` | Nguồn |
|---|---|---|---|---|
| `three-step` | 25 | **92** | 2 | `FX-010` — `round(23/25*100)` |
| `sample-retention` | 48 | **96** | 2 | `FX-011` — ⚠️ mock trả cứng `100/0` |
| `receiving` | 17 | **88** | 2 | `FX-012` — `round(15/17*100)` |
| `supplier-certificates` | 10 | **80** | 1 | `FX-013` |
| `hygiene` | 10 | **90** | 1 | `FX-014` |

4. **Act (biên):** tập rỗng → `ReportDataPort` trả 0 bản ghi

**Expected Result (PASS):**
- 5 nhóm đúng bảng trên; `generatedAt` khác `null` và là `Instant`
- Tập rỗng: `total=0`, `alerts=0`, `rate=100` — khớp hành vi `rate()` của mock (`total === 0 ? 100`), là hành vi **duy nhất** copy từ mock có căn cứ (không có phiên nào thì không có phiên nào trượt)
- **Không** hằng số nào trong code: test thứ hai với `FX-011` biến thể (48 mẫu, **10** quá hạn) ⇒ `rate=79`, `alerts=10`

**Expected Result (FAIL):**
- `sample-retention` trả `rate=100, alerts=0` bất chấp fixture → **đã copy mock**, vi phạm BR-182: BGH nhìn báo cáo thấy 100% tuân thủ lưu mẫu trong khi có 2 mẫu quá hạn — đúng loại lỗi mà báo cáo tồn tại để chặn
- `rate` tính bằng trung bình các `rate` con thay vì từ bản ghi gốc → sai khi các nhóm có `total` lệch nhau

**Current Status:** 🔴 Not written
**Implementation Note:** `rate` làm tròn `Math.round` (khớp mock `Math.round(ok/total*100)`), **không** `floor`. Tính trong SQL view/`ReportDataPort`, không tính lại ở service — tránh hai nơi làm tròn khác nhau.

---

#### `MF13-TC-003` — BR-178: file xuất phải có đủ 4 field header

**Severity:** `HIGH`
**Feature Under Test:** `PdfReportRenderer.render()` · `XlsxReportRenderer.render()`
**Test File:** `src/test/java/com/sfms/mf13/render/ReportRendererHeaderTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-003`
**Oracle Source:** **BR-178** nguyên văn — *"Mỗi file xuất phải bao gồm: **tiêu đề báo cáo, khoảng thời gian dữ liệu, tên trường, và timestamp xuất**"* · **BR-181** (nhãn `Confidential`) · **BR-182** (*"hiển thị rõ ngày giờ tạo và khoảng thời gian"*) · **UC-172**
**Technique:** Decision Table (4 field × có/thiếu) + đọc lại file thật

**Preconditions:** 5 hàng đã qua projection; `schoolName="TH Đoàn Thị Điểm"`; `generatedAt=2026-07-30T07:12:03Z`; `periodLabel="01/07/2026 – 30/07/2026"`

**Test Steps:**
1. **Arrange:** `ReportRenderCommand` đủ 4 field
2. **Act:** `render(cmd)` → `byte[]`
3. **Assert (PDF):** `new PDFTextStripper().getText(PDDocument.load(bytes))` chứa cả 4 chuỗi + `"Confidential"`
4. **Assert (XLSX):** `new XSSFWorkbook(...)` — 4 chuỗi nằm trong 5 dòng đầu của sheet đầu, `"Confidential"` ở header hoặc footer
5. **Act (âm):** lần lượt truyền `null` cho từng field trong 4 field

**Expected Result (PASS):**
- Cả 2 renderer: văn bản trích ra chứa **đồng thời** `"Báo cáo Lưu mẫu thực phẩm"`, `"01/07/2026 – 30/07/2026"`, `"TH Đoàn Thị Điểm"`, `"2026-07-30T07:12:03Z"` và `"Confidential"`
- Thiếu **bất kỳ** field nào trong 4 → `IllegalArgumentException` (fail-closed), **không** sinh file
- Số dòng dữ liệu trong file = `rows.size()` (không mất/nhân dòng)

**Expected Result (FAIL):**
- File thiếu tên trường hoặc khoảng thời gian → vi phạm BR-178; file in ra không biết của trường nào, kỳ nào, vô dụng khi thanh tra đối chiếu (đây là lý do BR-178 liệt kê đủ 4 field)
- `timestamp` in dạng `dd/MM/yyyy HH:mm` trong **payload/metadata** → vi phạm D4. *(`periodLabel` là **nhãn hiển thị** do người soạn truyền vào, được phép chứa `dd/MM/yyyy`; chỉ **field ngày trong JSON** mới bắt buộc ISO — test phân biệt hai chỗ này.)*

**Current Status:** 🔴 Not written
**Implementation Note:** dùng **một** lớp `ReportHeader` chung cho 2 renderer để không sửa 2 nơi — cũng là điểm MF12 gọi lại qua `ReportExportPort` (ADR-M13-2).

---

#### `MF13-TC-004` — ★ BR-181: projection loại bỏ PII học sinh (whitelist, fail-closed)

**Severity:** `CRITICAL`
**Feature Under Test:** `ReportRowProjection.project()`
**Test File:** `src/test/java/com/sfms/mf13/service/ReportRowProjectionTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-004`
**Oracle Source:** **BR-181** nguyên văn — *"Báo cáo **không được tiết lộ thông tin nhận diện cá nhân** của học sinh trong dữ liệu tổng hợp"* · **ADR-M13-4** (whitelist theo slug, fail-closed) · **UC-171/172**
**Technique:** Data Flow / Taint Testing

**Preconditions:** `Mf13Fixtures.tantedRow()` (hàng nguồn có `studentId`, `studentName`); `FX-017` (3 HS thật trong seed)

**Test Steps:**
1. **Arrange:** hàng nguồn = whitelist của slug **+ 2 cột PII** (giả lập module nguồn thêm cột sau này)
2. **Act:** `projection.project(SAMPLE_RETENTION, row)`
3. **Assert:** tập key trả về **bằng đúng** whitelist của slug
4. **Act (mọi slug):** lặp 7 slug với hàng nguồn được bơm thêm `studentId`/`studentName`/`parentName`/`diagnosis`

**Expected Result (PASS):**
- `project()` trả **đúng** whitelist `SAMPLE_RETENTION` = `{id, dish, meal, weight, retentionDeadline, fridgeTemp, status}` — không thêm, không thiếu
- **Không** key nào trong `Mf13Fixtures.piiNeedles()` xuất hiện, ở **cả 7 slug**
- Cột lạ (VD `internalNote`) **cũng bị loại** — chứng minh là whitelist chứ không phải blacklist
- Slug `system-usage`: `name` (tên **nhân sự**) **được giữ** — BR-181 chỉ cấm PII **học sinh**; assert tường minh để không siết quá tay thành vô dụng

**Expected Result (FAIL):**
- Dùng blacklist (`row.remove("studentName")`) → cột PII mới ở module nguồn **tự động lọt** vào báo cáo rồi ra file/email; đây là kịch bản rò dữ liệu thật, không phải giả thuyết
- Loại luôn `name` ở `system-usage` → báo cáo mức độ sử dụng rỗng nghĩa

**Current Status:** 🔴 Not written
**Implementation Note:** whitelist khai `static final Map<ReportSlug, List<String>>` **cùng chỗ** với danh sách cột của view (§5.2) để hai nơi không lệch; thêm cột vào view mà quên whitelist ⇒ `INT-001` FAIL (gate).

---

#### `MF13-TC-005` — BR-177: hạn lưu trữ luôn ≥ 12 tháng

**Severity:** `HIGH`
**Feature Under Test:** `ReportExportService.export()` (tính `expiresAt`)
**Test File:** `src/test/java/com/sfms/mf13/service/ReportRetentionTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-005`
**Oracle Source:** **BR-177** nguyên văn — *"Báo cáo đã tạo phải được lưu trữ trong hệ thống **tối thiểu 12 tháng**"* · **ADR-M13-3** (`report.retention-months` mặc định 24, sàn 12) · CHECK `ck_rpx_retention` (§5.2) · **UC-172**
**Technique:** Boundary Value Analysis

| Cấu hình `retention-months` | Expected `expiresAt - generatedAt` |
|---|---|
| `6` (dưới sàn) | **12 tháng** — service kẹp về sàn BR-177, **không** ném lỗi (báo cáo vẫn phải xuất được) |
| `12` (biên) | 12 tháng |
| `24` (mặc định) | 24 tháng |
| `0` / âm / thiếu | 12 tháng (sàn) |

**Expected Result (PASS):**
- Mọi trường hợp: `expiresAt ≥ generatedAt + 12 tháng` (**không ngoại lệ**)
- Log `WARN` khi cấu hình dưới sàn (phát hiện cấu hình sai, không im lặng)

**Expected Result (FAIL):**
- Cấu hình `6` được tôn trọng → bản xuất bị purge sau 6 tháng; khi thanh tra hỏi báo cáo tháng thứ 9 thì không còn gì — vi phạm BR-177 và **không thể khắc phục hồi tố**
- Ném lỗi khi cấu hình sai → chặn cả việc xuất báo cáo vì một tham số vận hành, hại nhiều hơn lợi

**Current Status:** 🔴 Not written
**Implementation Note:** CHECK `ck_rpx_retention` là chốt chặn cuối; kẹp ở service để không rơi vào 500.

---

#### `MF13-TC-006` — BR-175: bảng quyền theo LOẠI báo cáo (role × slug)

**Severity:** `CRITICAL`
**Feature Under Test:** `ReportAccessPolicy.canView()` · `canExport()`
**Test File:** `src/test/java/com/sfms/mf13/service/ReportAccessPolicyTest.java`
**TDD Phase:** 🔴 RED — ⚠️ **liên quan ⛔ L5**
**Condition Ref:** `TC-COND-006`
**Oracle Source:** **BR-175** nguyên văn (4 nhóm role) · **Actor của traceability §1.13** (UC-171 = School Administration / Medical Staff / System Admin; UC-172 = School Administration / System Admin) · TDS **§16** · **D6** `RoleKey` kebab-case
**Technique:** Decision Table Testing

**Test Steps:** `@ParameterizedTest` trên `(role, slug, action)` → `allowed`

| role | slug | `canView` | `canExport` | Oracle |
|---|---|---|---|---|
| `system-admin` | mọi slug | ✅ | ✅ | BR-175 *"xem tất cả báo cáo trên toàn hệ thống"* |
| `school-admin` | mọi slug | ✅ | ✅ | BR-175 *"xem toàn bộ báo cáo vận hành của trường"* + Actor UC-172 |
| `medical-staff` | `three-step`, `sample-retention`, `hygiene`, `receiving`, `supplier-certificates`, `traceability` | ✅ | **❌** | BR-175 *"báo cáo kiểm tra thực phẩm, sự cố y tế, tuân thủ an toàn"*; **không** là Actor của UC-172 |
| `medical-staff` | `system-usage` | **❌** | ❌ | BR-175 không xếp "mức độ sử dụng hệ thống" vào phạm vi Y tế |
| `service-caterers` | mọi slug | **❌** ⛔ | ❌ | §16 bám Actor §1.13. ⛔ **L5** — BR-175 cho họ quyền xem hoạt động của mình nhưng §1.13 không liệt là Actor |
| `teacher`, `parent`, `supplier` | mọi slug | ❌ | ❌ | không có trong BR-175 lẫn Actor §1.13 |

**Expected Result (PASS):**
- Đúng 100% bảng trên; `canExport ⊆ canView` (không role nào xuất được thứ mình không xem được)
- Role lạ / `null` → `false` (**fail-closed**)

**Expected Result (FAIL):**
- `medical-staff` `canExport = true` → vượt Actor §1.13; Y tế xuất file dữ liệu vận hành ra ngoài hệ thống mà UC không cho
- `service-caterers` `canView = true` cho `system-usage` → đơn vị nấu ăn (đối tác **ngoài** trường) xem được danh sách tài khoản nhân sự của trường

**Current Status:** 🔴 Not written — ⚠️ **3 ô của `service-caterers` (`receiving`, `three-step`, `hygiene`) có thể đổi sang ✅ khi L5 được chốt**
**Implementation Note:** policy là **bảng dữ liệu** (`Map<RoleKey, Set<ReportSlug>>`), không phải chuỗi `if`; đổi quyết định L5 = sửa 1 dòng dữ liệu + 1 dòng bảng test.

---

#### `MF13-TC-007` — BR-179: `nextRunAt` theo frequency + zoneId; WEEKLY/MONTHLY bắt buộc trường tương ứng

**Severity:** `HIGH`
**Feature Under Test:** `ReportScheduleService.computeNextRun()` · `create()` validate
**Test File:** `src/test/java/com/sfms/mf13/service/ReportScheduleComputeTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-007`
**Oracle Source:** **BR-179** nguyên văn — *"lên lịch tạo và gửi báo cáo định kỳ (**hàng ngày, hàng tuần, hàng tháng**)"* · **MSG-138** → `RPT-138` (TDS §10) · CHECK `ck_rsc_weekly`/`ck_rsc_monthly` (§5.2) · **UC-173**
**Technique:** Equivalence Partitioning + Boundary Value Analysis

**Preconditions:** `now = 2026-07-30T10:00+07:00` (thứ 5), `timeOfDay=07:30`, `zoneId="Asia/Ho_Chi_Minh"`

| `frequency` | tham số | Expected `nextRunAt` |
|---|---|---|
| `DAILY` | — | `2026-07-31T07:30+07:00` |
| `WEEKLY` | `dayOfWeek=1` (thứ 2) | `2026-08-03T07:30+07:00` |
| `WEEKLY` | `dayOfWeek=4` (thứ 5 — **hôm nay** nhưng đã qua 07:30) | `2026-08-06T07:30+07:00` — **không** lùi về quá khứ |
| `MONTHLY` | `dayOfMonth=5` | `2026-08-05T07:30+07:00` |
| `MONTHLY` | `dayOfMonth=28` (biên trên `ck_rsc_monthly`) | `2026-08-28T07:30+07:00` |
| `WEEKLY` | `dayOfWeek=null` | `ValidationException` `RPT-138`, `details[0].field="dayOfWeek"` |
| `WEEKLY` | `dayOfWeek=0` / `8` | `ValidationException` `RPT-138` |
| `MONTHLY` | `dayOfMonth=null` / `0` / `29` | `ValidationException` `RPT-138` |

**Expected Result (PASS):**
- Đúng bảng trên; `nextRunAt` lưu **UTC** (`Instant`) nhưng tính theo `zoneId` — assert bằng `Instant` tuyệt đối, không so chuỗi
- `nextRunAt > now` trong **mọi** trường hợp hợp lệ

**Expected Result (FAIL):**
- `nextRunAt ≤ now` → runner nhặt lại mỗi phút ⇒ **spam email** cho BGH
- Tính bằng `LocalDateTime` không kèm zone → lệch 7 giờ, báo cáo tuần gửi lúc 00:30 đêm
- `dayOfMonth=29..31` được nhận → tháng 2 không có ngày đó ⇒ lịch **im lặng không chạy** (lý do `ck_rsc_monthly` chốt trần 28)

**Current Status:** 🔴 Not written
**Implementation Note:** dùng `ZonedDateTime` + `TemporalAdjusters.next(DayOfWeek)`; **không** tự cộng 7 ngày.

---

#### `MF13-TC-008` — BR-179: retry tối đa 3 lần, không có lần thứ 4

**Severity:** `CRITICAL`
**Feature Under Test:** `ReportScheduleRunner.tick()` (logic retry, mock `MailSender`)
**Test File:** `src/test/java/com/sfms/mf13/scheduler/ReportScheduleRetryTest.java`
**TDD Phase:** 🔴 RED — ~~⚠️ liên quan ⛔ L3~~ → ✅ **L3 đóng (`M13-D08`)**, không còn phần `__PENDING__`
**Condition Ref:** `TC-COND-008`
**Oracle Source:** **BR-179** nguyên văn — *"Khi gửi thất bại, hệ thống tự động thử lại **tối đa 3 lần**; nếu vẫn thất bại sẽ ghi nhận lỗi và thông báo cho System Admin"* · CHECK `ck_rsr_attempt BETWEEN 1 AND 3` (§5.2) · State machine **§6.4** · **UC-173**
**Technique:** State Transition Testing + Boundary Value Analysis

**Test Steps:**
1. **Arrange:** `MailSender.send()` luôn ném `MailSendException`; `FX-030`; `MutableClock`
2. **Act:** gọi `tick()` 5 lượt (vượt số retry cho phép)
3. **Assert:** trạng thái từng lượt

| Lượt | `attempt` ghi nhận | `status` | Backoff tới lượt sau |
|---|---|---|---|
| 1 | 1 | PENDING | +5 phút |
| 2 | 2 | PENDING | +15 phút |
| 3 | 3 | **FAILED** | — |
| 4 | **không ghi mới** | vẫn FAILED | — |
| 5 | **không ghi mới** | vẫn FAILED | — |

**Expected Result (PASS):**
- Đúng **3** bản ghi `report_schedule_runs`; **không** có `attempt=4`
- Sau lượt 3: `status=FAILED`, `error_message` khác rỗng và chứa nguyên nhân
- `mf10.port.NotificationPort.publish(NotificationCommand{type="REPORT_SCHEDULE_FAILED", target=SYSTEM_ADMIN, payload={scheduleCode, reportSlug, scheduledFor, attempts:3}})` gọi **đúng 1 lần** (không mỗi lượt một lần) — chữ ký thật của MF10, `M13-D07(c)`
- `MailSender.send()` gọi **đúng 3 lần**
- ✅ **Nội dung/mã thông báo (`M13-D08`, gỡ `__PENDING__`):** BR-179 thất bại 3/3 **không phải lỗi API** ⇒ **không có MSG và không cần có**. Assert đủ: `type`, `target=SYSTEM_ADMIN`, và payload có đủ 4 khoá `{scheduleCode, reportSlug, scheduledFor, attempts:3}`. **Không** assert chuỗi hiển thị (thuộc `NotificationCommand` của MF10, không phải oracle của MF13).

**Expected Result (FAIL):**
- Có `attempt=4` → vi phạm BR-179; nếu SMTP hỏng cả ngày, runner mỗi phút gửi lại ⇒ **1.440 email lỗi/ngày**, IP bị đánh dấu spam
- Gọi `publish(...)` mỗi lượt → System Admin nhận 3 thông báo cho 1 sự việc
- `FAILED` tự retry ở lượt 4 → trái §6.4 (*"FAILED KHÔNG tự retry — cần sửa cấu hình rồi `run-now`"*)

**Current Status:** 🔴 Not written
**Implementation Note:** backoff đọc từ cấu hình (`5m,15m,45m`), **không** hardcode; `Clock` inject được để test không `Thread.sleep`.

---

#### `MF13-TC-009` — BR-179: người nhận CHỈ từ `school_config.report_emails`

**Severity:** `CRITICAL`
**Feature Under Test:** `ReportScheduleService.create()` · `ReportScheduleRunner` (nguồn người nhận)
**Test File:** `src/test/java/com/sfms/mf13/service/ReportRecipientSourceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-009`
**Oracle Source:** **BR-179** nguyên văn — *"Báo cáo định kỳ được gửi đến danh sách email **được cấu hình bởi School Administration**"* · TDS **§4.3 Email egress** · **UC-173**
**Technique:** Error Guessing (egress control)

**Test Steps:**
1. **Arrange:** `SchoolConfigPort.reportEmails(FX-001)` → `["toanpq@dtd.edu.vn","hant@dtd.edu.vn"]`
2. **Act:** `create(req)` với body **có** `recipients=["attacker@evil.com"]`
3. **Act:** runner gửi lịch đó
4. **Assert:** người nhận thực tế

**Expected Result (PASS):**
- `ReportScheduleResponse.recipients` = **đúng 2 email của `FX-003`**; `"attacker@evil.com"` **không** xuất hiện
- `MailSender.send()` nhận `to` = 2 email đó, `recipientCount=2`
- Field `recipients` trong request **bị bỏ qua** — assert `report_schedules` **không có cột** người nhận
- Lịch của **trường B** (`FX-031`) dùng `FX-004` (`admin@sch51.edu.vn`) — không trộn hai trường

**Expected Result (FAIL):**
- `recipients` từ body được dùng → biến "lịch báo cáo" thành **kênh phát tán dữ liệu trường ra email bất kỳ**; đây là lỗ hổng rò dữ liệu nghiêm trọng nhất mà MF13 có thể tạo ra
- Người nhận đọc từ cấu hình **toàn hệ thống** thay vì theo trường → BGH trường A nhận báo cáo trường B (vi phạm BR-176)

**Current Status:** 🔴 Not written
**Implementation Note:** đơn giản nhất là **không** có field `recipients` trong `CreateReportScheduleRequest`; nếu giữ để tương thích FE thì đánh `@Schema(accessMode = READ_ONLY)` và bỏ qua ở mapper.

---

#### `MF13-TC-010` — BR-182: bản xuất bất biến (không setter, không đường sửa)

**Severity:** `HIGH`
**Feature Under Test:** `ReportExport` (entity) · `ReportExportService` (không có `update`)
**Test File:** `src/test/java/com/sfms/mf13/domain/ReportExportImmutabilityTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-010`
**Oracle Source:** **BR-182** nguyên văn — *"**không cho phép chỉnh sửa thủ công** sau khi tạo báo cáo"* · **ADR-M13-3** · State machine **§6.5** (*không có trạng thái EDITED*) · **UC-172**
**Technique:** Reflection-based structural test (kiểm hợp đồng lớp)

**Expected Result (PASS):**
- **0** method `public void set*` cho `code`, `schoolId`, `reportSlug`, `rowCount`, `fileUri`, `checksumSha256`, `generatedAt`, `expiresAt`
- `ReportExportService` chỉ có `export`, `history`, `findByCode`, `download` (khớp TDS §8.1)
- `ReportExportRepository` **không** khai `deleteBy*` nào ngoài đường purge (`findByExpiresAtBefore` + xoá có kiểm hạn)

**Expected Result (FAIL):**
- Có `setRowCount()` public → mở đường sửa số liệu sau khi báo cáo đã gửi cho BGH/thanh tra; trigger DB chặn `UPDATE` nhưng người viết code gặp 500 thay vì compile-error, và có thể "sửa" bằng cách xoá-tạo lại
- Có `ReportExportService.update()` → vi phạm BR-182 ở tầng API

**Current Status:** 🔴 Not written
**Implementation Note:** entity dùng constructor + `@Builder`; JPA cần setter thì để **`protected`** (test chỉ soi `public`). Trigger `report_exports_no_update` là lớp chặn thứ hai — kiểm ở `INT-007`.

---

#### `MF13-TC-011` — ADR-M13-5 / D6: `ReportSlug` chỉ 7 giá trị, JSON đúng kebab-case

**Severity:** `MEDIUM`
**Feature Under Test:** `enum ReportSlug` (`@JsonValue`)
**Test File:** `src/test/java/com/sfms/mf13/domain/ReportSlugSerializationTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-011`
**Oracle Source:** **ADR-M13-5** (kebab-case vì là URL segment — **cùng lý do `RoleKey` của D6**) · **D6** · 7 route thật của FE (`REPORT_META` trong `[slug]/page.tsx`) · CHECK `ck_rpx_slug` (§5.2)
**Technique:** Equivalence Partitioning

| Java | JSON | Route FE tương ứng |
|---|---|---|
| `THREE_STEP` | `"three-step"` | `/school-admin/reports/three-step` |
| `SAMPLE_RETENTION` | `"sample-retention"` | `…/sample-retention` |
| `RECEIVING` | `"receiving"` | `…/receiving` |
| `SUPPLIER_CERTIFICATES` | `"supplier-certificates"` | `…/supplier-certificates` |
| `HYGIENE` | `"hygiene"` | `…/hygiene` |
| `TRACEABILITY` | `"traceability"` | `…/traceability` |
| `SYSTEM_USAGE` | `"system-usage"` | `…/system-usage` |

**Expected Result (PASS):**
- `ReportSlug.values().length == 7`; đúng 7 cặp trên
- `"THREE_STEP"`, `"three_step"`, `"../../etc/passwd"`, `""` → `InvalidFormatException` → controller trả **404 `RPT-404`** (MSG-137)
- MF13 **không** khai lại bất kỳ enum nào trong bảng 8 enum của D6 (grep ở Exit Criteria)

**Expected Result (FAIL):**
- Serialize ra `"THREE_STEP"` → FE gọi `/reports/THREE_STEP/rows` không khớp route hiện có ⇒ vỡ hợp đồng với 7 màn báo cáo
- Nhận `slug` tự do (String) → path traversal / injection qua tên view

**Current Status:** 🔴 Not written
**Implementation Note:** `@JsonValue` trên method `slug()`; springdoc tự sinh `enum` trong spec (**D8/S5**) ⇒ `MF13-TC-CON-003` kiểm lại ở tầng spec.

---

#### `MF13-TC-012` — UC-171: danh mục báo cáo trả đủ 7, lọc theo quyền

**Severity:** `MEDIUM`
**Feature Under Test:** `ReportCatalogService.listForCurrentUser()`
**Test File:** `src/test/java/com/sfms/mf13/service/ReportCatalogServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-012`
**Oracle Source:** **UC-171** Normal Flow bước 3 (*"…for the selected report category (Food Sample Retention, Receiving, Partnership, Traceability, or System Usage)"*) · **BR-175** · **CF-M13-7d** (2 báo cáo `traceability` + `system-usage` đang **hardcode trong FE** `page.tsx:119–122`)
**Technique:** Decision Table (role × danh mục)

**Expected Result (PASS):**
- `FX-021` (school-admin) → **7** mục, gồm `traceability` và `system-usage` (**không** để FE hardcode)
- `FX-020` (system-admin) → **7** mục
- `FX-022` (medical-staff) → **6** mục (thiếu `system-usage` — BR-175)
- `FX-023` (service-caterers), `FX-024*` (teacher/parent/supplier) → **0** mục ⛔ (L5)
- Mỗi mục có `slug`, `title`, `group ∈ {COMPLIANCE, ADDITIONAL}`, `exportable`; `exportable` phản ánh `canExport(role, slug)` (medical-staff → `false` ở mọi mục)

**Expected Result (FAIL):**
- Trả 5 mục (chỉ nhóm `COMPLIANCE` như handler mock) → FE phải tiếp tục hardcode 2 dòng ⇒ CF-M13-7d không được gỡ, danh mục sống ở 2 nơi
- `exportable=true` cho medical-staff → FE hiện nút Xuất cho role không có quyền, bấm rồi nhận 403

**Current Status:** 🔴 Not written
**Implementation Note:** danh mục là **nguồn duy nhất** cho cả `/reports` và `modules[]` của `/compliance-report` — không khai hai bảng title khác nhau.

---

### 4.2. INTEGRATION TEST CASES — endpoint → DB (Testcontainers PostgreSQL 16 + Flyway thật)

> **Mock strategy chung:** Testcontainers `postgres:16-alpine`, Flyway chạy **V1 + view + seed** như production; **GreenMail** thay SMTP; `FileStoragePort` trỏ thư mục tạm (`@TempDir`) đúng như ADR-000 (`./data/uploads`). **Không** mock repository ở lớp này — mục đích là kiểm CHECK/trigger/view thật.

---

#### `MF13-TC-INT-001` — ★ GATE hạ tầng: 7 view `v_rpt_*` tồn tại và đúng tập cột

**Severity:** `CRITICAL`
**Feature Under Test:** `V{n}__mf13_report_views.sql` · `ReportDataPort`
**Test File:** `src/test/java/com/sfms/mf13/repository/ReportViewContractIT.java`
**TDD Phase:** 🔴 RED — **chạy ĐẦU TIÊN trong suite IT**
**Condition Ref:** `TC-COND-013`
**Oracle Source:** **ADR-M13-1** (*"view là hợp đồng đọc tường minh… đổi schema module nguồn → view vỡ ở migration/CI"*) · TDS **§5.2** (7 view) · **§11.2** (pre-migration checklist) · Logic Issue **L11**
**Technique:** Contract test tầng DB

**Test Steps:**
1. **Act:** với từng slug, `SELECT * FROM v_rpt_<x> LIMIT 1`
2. **Assert:** truy vấn không lỗi; `ResultSetMetaData` chứa **đúng** tập cột mà `ReportRowProjection` whitelist cho slug đó + cột `school_id`
3. **Act:** `SELECT ... WHERE school_id = ?` chạy được (cột lọc BR-176 tồn tại ở **mọi** view)

**Expected Result (PASS):**
- 7/7 view `SELECT` được sau `flyway:migrate`
- Tập cột của mỗi view ⊇ whitelist của slug tương ứng; **không** view nào chứa cột trong `piiNeedles()` (BR-181 ở tầng DB)
- Mọi view có `school_id`

**Expected Result (FAIL):**
- `column "supplier_name" does not exist` → chủ module MF06 đặt tên khác ⇒ **11 IT phía sau vô nghĩa**; đây chính là lý do test này chạy đầu tiên (L11)
- View thiếu `school_id` → BR-176 không thể enforce ở tầng truy vấn

**Current Status:** 🔴 Not written
**Implementation Note:** nếu FAIL, sửa **view** (thuộc MF13), **không** sửa bảng của module khác — ADR-M13-1. Ghi vào Suspension Criteria §6.

---

#### `MF13-TC-INT-002` — BR-182 + BR-176: `/compliance-report` tính đúng từ DB thật, chỉ trong phạm vi trường

**Severity:** `CRITICAL`
**Feature Under Test:** `GET /api/v1/compliance-report`
**Test File:** `src/test/java/com/sfms/mf13/web/ComplianceReportIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-002`, `TC-COND-013`
**Oracle Source:** **BR-182** (tính từ nguồn gốc) · **BR-176** (phạm vi trường) · **UC-171** · fixture `FX-010`…`FX-016`, `FX-018` · điểm lệch mock **L6**
**Technique:** Equivalence Partitioning

**Test Steps:**
1. **Arrange:** seed `FX-010`…`FX-016` (trường A) **và** `FX-018` (1 phiên trường B cùng ngày)
2. **Act:** `GET /api/v1/compliance-report?dateFrom=2026-07-01&dateTo=2026-07-30` với JWT `FX-021`
3. **Assert:** 5 nhóm khớp bảng của `MF13-TC-002`
4. **Act:** cùng request với JWT `FX-025` (trường B)
5. **Assert:** `three-step.total = 1` (chỉ `FX-018`), các nhóm khác `total = 0`, `rate = 100`

**Expected Result (PASS):**
- Trường A: `three-step` 25/92/2 · `sample-retention` 48/96/2 · `receiving` 17/88/2 · `supplier-certificates` 10/80/1 · `hygiene` 10/90/1
- Trường B: **không** thấy bản ghi nào của trường A (`total` của `sample-retention` = 0, không phải 48)
- `modules[]` đúng 5 nhóm `COMPLIANCE`; `schoolId` trong response = trường của JWT

**Expected Result (FAIL):**
- `total` của trường B = 48 → BR-176 không được enforce ở tầng truy vấn tổng hợp (rò dữ liệu **số liệu** — khó thấy hơn rò dòng dữ liệu nhưng cùng mức nghiêm trọng)
- `sample-retention.rate = 100` → copy hằng số của mock (L6)

**Current Status:** 🔴 Not written

---

#### `MF13-TC-INT-003` — D4 + BR-182: ngày/giờ ISO-8601, có `periodFrom`/`periodTo`

**Severity:** `HIGH`
**Feature Under Test:** `GET /api/v1/compliance-report` · `GET /api/v1/reports/{slug}/rows` · `POST /reports/{slug}/export`
**Test File:** `src/test/java/com/sfms/mf13/web/DateFormatContractIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-014`
**Oracle Source:** **D4** (*"Ngày `"2026-07-29"` / Ngày giờ `"2026-07-29T02:15:00Z"`"*) · **BR-182** (*"hiển thị rõ ngày giờ tạo và khoảng thời gian"*) · **UC-172 AF1** (export dùng bộ lọc hiện tại) · điểm lệch mock **L7** (`nowStamp()` → `dd/MM/yyyy HH:mm`)
**Technique:** Contract test (regex trên toàn body)

**Expected Result (PASS):**
- `generatedAt`, `exportedAt`, `expiresAt`, `nextRunAt` khớp `^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d+)?Z$`
- `periodFrom`, `periodTo`, `retentionDeadline`… khớp `^\d{4}-\d{2}-\d{2}$`
- `/compliance-report` **có** cả `periodFrom` và `periodTo` (máy đọc được) bên cạnh `periodLabel`
- **0 kết quả** cho `\d{2}/\d{2}/\d{4}` trên toàn bộ body **trừ** `periodLabel` (nhãn hiển thị — được phép; assert tường minh sự loại trừ này)
- Grep code: `grep -rn "JsonFormat" com/sfms/mf13` → rỗng

**Expected Result (FAIL):**
- `generatedAt = "30/07/2026 14:05"` → copy `nowStamp()` của mock; `openapi-typescript` (T3.4) sinh type `string` không có format ⇒ FE parse sai, và D4 tái phát qua file sinh tự động
- Thiếu `periodFrom`/`periodTo` → UC-172 AF1 không có gì để truyền, người dùng xuất ra file mang kỳ dữ liệu khác kỳ đang xem

**Current Status:** 🔴 Not written

---

#### `MF13-TC-INT-004` — BR-178 (L2): xuất báo cáo cho kỳ RỖNG vẫn thành công

**Severity:** `MEDIUM`
**Feature Under Test:** `POST /api/v1/reports/{slug}/export`
**Test File:** `src/test/java/com/sfms/mf13/web/ReportExportEmptyPeriodIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-016`
**Oracle Source:** **BR-178** nguyên văn — quy định *định dạng* + *4 field header*, **không** có điều khoản chặn khi rỗng · Logic Issue **L2** · ⚠️ **KHÔNG** dùng MSG-092/096/097 (thuộc §1.12, gắn BR-209/213/214 — CF-M13-4)
**Technique:** Equivalence Partitioning (biên tập rỗng)

**Test Steps:**
1. **Arrange:** kỳ `2026-01-01 … 2026-01-02` (không bản ghi nào trong `FX-010`…`FX-016`)
2. **Act:** `POST /api/v1/reports/three-step/export {format:"PDF", dateFrom, dateTo}` với JWT `FX-021`
3. **Assert:** response + nội dung file

**Expected Result (PASS):**
- **201**, `rowCount = 0`, `id` khớp `^RPX-\d{4}-\d{3}$`
- File PDF sinh ra, đọc lại bằng `PDFTextStripper` **vẫn có đủ 4 field header** BR-178 + `"Confidential"`
- 1 dòng `report_exports` + 1 dòng audit (BR-177) — kỳ rỗng vẫn là một lần xuất, vẫn phải ghi nhận
- **Không** trả MSG-092/096/097 (grep body: không chứa `"No incidents recorded"`, `"No violations recorded"`, `"No kitchen inspection data"`)

**Expected Result (FAIL):**
- Trả 409/422 kèm MSG-092/096/097 → mượn mã sai module (AP-AI-016); đúng loại lỗi mà `error_message.md` v1.5 phải gom lại 35 mã
- Sinh file 0 byte hoặc thiếu header → thanh tra nhận file trắng không biết là "kỳ không có dữ liệu" hay "hệ thống lỗi"

**Current Status:** 🔴 Not written — ⚠️ nếu **Huy** xác nhận UC-172 có yêu cầu chặn ẩn thì đảo expected và **phải cấp MSG mới** (không mượn)

---

#### `MF13-TC-INT-005` — BR-180: tuổi dữ liệu dashboard ≤ 300s, evict theo event

**Severity:** `HIGH`
**Feature Under Test:** `AnalyticsQueryService` (`@Cacheable` TTL 300s) + event listener (§7.2)
**Test File:** `src/test/java/com/sfms/mf13/web/DashboardFreshnessIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-015`
**Oracle Source:** **BR-180** nguyên văn — *"Dashboard Phân tích phải hiển thị dữ liệu được làm mới trong vòng **5 phút** kể từ khi có thay đổi vận hành"* · **ADR-M13-6** · Logic Issue **L9** (biên TTL = 300s)
**Technique:** Boundary Value Analysis + `MutableClock`

**Test Steps:**
1. **Act:** gọi `/compliance-report` lần 1 → ghi `generatedAt₁`
2. **Act:** gọi lần 2 sau 10s (clock ảo) → so `generatedAt`
3. **Act:** đẩy clock +301s, gọi lần 3
4. **Act:** publish `KitchenInspectionSubmitted` rồi gọi lần 4 **ngay** (không đợi TTL)

**Expected Result (PASS):**
- Lần 2: `generatedAt₂ == generatedAt₁` (cache hit) và `now - generatedAt ≤ 300s`
- Lần 3: `generatedAt₃ > generatedAt₁` (TTL hết → tính lại)
- Lần 4: `generatedAt₄ > generatedAt₃` **ngay sau event** (evict theo event — §7.2) ⇒ BR-180 đạt **không phụ thuộc** biên TTL (L9)
- `hygiene.rate` ở lần 4 phản ánh phiên bếp vừa thêm

**Expected Result (FAIL):**
- Lần 4 trả `generatedAt` cũ → dữ liệu có thể cũ tới 300s **cộng** thời gian tính; ở biên là vượt 5 phút, vi phạm BR-180 (đây là lý do khuyến nghị hạ TTL 240s ở L9)
- Không cache gì cả → mỗi request chạy 7 truy vấn tổng hợp; §4.1 NFR (p99 < 300ms) không đạt

**Current Status:** 🔴 Not written

---

#### `MF13-TC-INT-006` — BR-178 + D2/D7: luồng xuất PDF/XLSX + envelope lỗi

**Severity:** `HIGH`
**Feature Under Test:** `POST /api/v1/reports/{slug}/export`
**Test File:** `src/test/java/com/sfms/mf13/web/ReportExportIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-001`, `TC-COND-003`, `TC-COND-020`
**Oracle Source:** **BR-178** · **D2** (envelope) · **D7** (message tiếng Anh nguyên văn) · **MSG-138** → `RPT-138` · **MSG-137** → `RPT-404` (TDS §10) · **UC-172**
**Technique:** Equivalence Partitioning + Error Guessing

| Bước | Request | Expected |
|---|---|---|
| a | `POST /reports/sample-retention/export {format:"PDF", …}` | 201, `rowCount=48`, file `.pdf` có 4 field header |
| b | cùng vậy với `format:"XLSX"` | 201, file `.xlsx` mở được bằng POI, 48 dòng dữ liệu |
| c | `format:"CSV"` | 422 `{"error":{"code":"RPT-138","message":"Validation failed: one or more required fields are missing or invalid.","details":[{"field":"format",…}]}}` |
| d | `dateFrom > dateTo` | 422 `RPT-138` (CHECK `ck_rpx_period` là chốt cuối, service kiểm trước) |
| e | `POST /reports/unknown-slug/export` | 404 `RPT-404`, message = **MSG-137 nguyên văn** (`"Resource not found."`) |
| f | `POST /reports/../../etc/passwd/export` | 404 `RPT-404`, **không** stack trace, **không** tên constraint DB trong body |

**Expected Result (PASS):**
- a–b: 2 dòng `report_exports`, 2 dòng audit; `checksumSha256` khác nhau giữa PDF và XLSX
- c–f: envelope **đủ 3 field** `code`/`message`/`details`; `details` là **mảng**, **không bao giờ `null`** (D2); `message` **tiếng Anh nguyên văn** (D7)
- **0** dòng `report_exports` được ghi cho c–f

**Expected Result (FAIL):**
- Body lỗi dạng `{"message":"..."}` phẳng (như mock) → vi phạm D2; FE interceptor khoan dung nên **không phát hiện được ở FE** — chỉ test này bắt được
- `message` tiếng Việt tự viết → vi phạm D7
- Lộ `ck_rpx_period` hoặc stack trace → rò chi tiết schema

**Current Status:** 🔴 Not written

---

#### `MF13-TC-INT-007` — BR-177 + BR-182: tải lại đúng file, checksum khớp, không sửa được

**Severity:** `HIGH`
**Feature Under Test:** `GET /api/v1/report-exports/{code}/download` · trigger `report_exports_no_update`
**Test File:** `src/test/java/com/sfms/mf13/web/ReportDownloadIntegrityIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-005`, `TC-COND-010`
**Oracle Source:** **BR-177** (audit mỗi lần xuất) · **BR-182** (không sửa sau khi tạo) · **ADR-M13-3** (checksum + snapshot) · §7.1 event `ReportDownloaded` · **UC-172**
**Technique:** State Transition Testing (GENERATED → GENERATED sau n lần tải; **không** có EDITED)

**Test Steps:**
1. **Arrange:** `FX-040` (`RPX-2026-001`, PDF, `rowCount=48`)
2. **Act:** thêm 5 mẫu mới vào `food_samples` (nguồn thay đổi **sau** khi xuất)
3. **Act:** `GET /report-exports/RPX-2026-001/download` 2 lần
4. **Act:** `UPDATE report_exports SET row_count = 0 WHERE code='RPX-2026-001'` (SQL trực tiếp)

**Expected Result (PASS):**
- Hai lần tải trả **byte giống nhau**; `sha256(body) == report_exports.checksum_sha256`
- Nội dung file vẫn là **48 dòng** — **không** phản ánh 5 mẫu mới (snapshot bất biến, ADR-M13-3)
- `Content-Disposition: attachment; filename="RPX-2026-001.pdf"`
- Mỗi lần tải sinh 1 dòng audit + event `ReportDownloaded` (BR-177)
- `UPDATE` → DB raise `report_exports is append-only (BR-182)`

**Expected Result (FAIL):**
- File tải lại có 53 dòng → báo cáo **tự đổi nội dung** sau khi đã gửi cho BGH/thanh tra; hai người mở cùng một mã báo cáo thấy hai số khác nhau — vi phạm BR-182 ở dạng nguy hiểm nhất vì không ai phát hiện
- `UPDATE` thành công → trigger bị bỏ khi rollback/migrate lại (TDS §12.1 xếp đây là điều kiện rollback)

**Current Status:** 🔴 Not written

---

#### `MF13-TC-INT-008` — UC-173 + D5: CRUD lịch báo cáo, `id` client gửi trùng → 409

**Severity:** `HIGH`
**Feature Under Test:** `POST`/`PATCH`/`DELETE /api/v1/report-schedules`
**Test File:** `src/test/java/com/sfms/mf13/web/ReportScheduleCrudIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-007`, `TC-COND-019`
**Oracle Source:** **D5** (*"body có `id` → kiểm trùng → **409**"*) · **BR-179** · CHECK `ck_rsc_weekly`/`ck_rsc_monthly`, `uq_rsc_school_code` (§5.2) · **MSG-138** → `RPT-409`/`RPT-138` (TDS §10) · **UC-173**
**Technique:** Equivalence Partitioning + State Transition

| Bước | Request | Expected |
|---|---|---|
| a | `POST {reportSlug:"hygiene",frequency:"WEEKLY",format:"XLSX",dayOfWeek:1,timeOfDay:"07:30"}` (không `id`) | 201, `id` server sinh khớp `^RSC-\d{4}-\d{3}$`, `active=true`, `nextRunAt` > now |
| b | `POST {id:"RSC-2026-001", …}` (đã có `FX-030`) | **409** `RPT-409`, `details[0].field="id"`; bản ghi cũ **không** bị ghi đè (assert `updatedAt` không đổi) |
| c | `POST {frequency:"WEEKLY"}` thiếu `dayOfWeek` | 422 `RPT-138` |
| d | `PATCH /report-schedules/RSC-2026-001 {active:false}` | 200, `active=false`; runner **không** nhặt nữa (kiểm ở `INT-009`) |
| e | `DELETE /report-schedules/RSC-2026-001` | 204; `report_schedule_runs` của lịch đó bị cascade xoá (`ON DELETE CASCADE`) |
| f | `PATCH /report-schedules/RSC-2026-002` (lịch **trường B**) với JWT `FX-021` | **404** `RPT-404` (BR-176 — xem L4) |

**Expected Result (PASS):**
- Đúng bảng trên; mọi thay đổi sinh event `ReportScheduleChanged` + audit
- `recipients` trong response luôn = `school_config.report_emails` của **trường trong JWT**

**Expected Result (FAIL):**
- b trả 200 và ghi đè `FX-030` → vi phạm D5; FE điều hướng theo `id` mình gửi rồi thấy dữ liệu người khác
- f trả 200 → School Admin trường A sửa/tắt lịch báo cáo của trường B

**Current Status:** 🔴 Not written

---

#### `MF13-TC-INT-009` — BR-179: runner gửi email, retry ≤3, SKIP LOCKED, thông báo System Admin

**Severity:** `CRITICAL`
**Feature Under Test:** `ReportScheduleRunner` (end-to-end với GreenMail + DB thật)
**Test File:** `src/test/java/com/sfms/mf13/scheduler/ReportScheduleRunnerIT.java`
**TDD Phase:** 🔴 RED — ~~⚠️ ⛔ L3~~ → ✅ **L3 đóng (`M13-D08`)**
**Condition Ref:** `TC-COND-008`, `TC-COND-009`
**Oracle Source:** **BR-179** nguyên văn (3 mệnh đề: định kỳ · email theo cấu hình School Administration · retry ≤3 rồi báo System Admin) · **ADR-M13-7** (`FOR UPDATE SKIP LOCKED`) · CHECK `ck_rsr_attempt` · **UC-173**
**Technique:** State Transition + Concurrency Testing

| Scenario | Arrange | Expected |
|---|---|---|
| 1 — thành công | `FX-030` đến hạn, GreenMail nhận được | 1 dòng `SUCCESS`, `attempt=1`, `recipient_count=2`, GreenMail có **1** email tới đúng 2 địa chỉ của `FX-003`, đính kèm `.xlsx`; `report_export_id` khác `null`; `next_run_at` dịch sang thứ 2 kế tiếp |
| 2 — lịch tắt / trường khác | `FX-031` (`active=false`, trường B) | **0** dòng run cho `FX-031`; GreenMail **không** có email nào tới `admin@sch51.edu.vn` |
| 3 — song song | 2 runner `tick()` đồng thời cùng slot của `FX-030` | **đúng 1** email; **đúng 1** dòng run (`uq_rsr_schedule_slot_attempt` + SKIP LOCKED) |
| 4 — SMTP hỏng | GreenMail dừng | 3 dòng `attempt=1,2,3`, dòng cuối `FAILED`, `error_message` khác rỗng; `INSERT attempt=4` bằng SQL → DB raise `ck_rsr_attempt`; `publish(...)` gọi **1 lần** với `type=REPORT_SCHEDULE_FAILED` (L3 đóng — `M13-D08`) |
| 5 — khôi phục | sau scenario 4, sửa cấu hình rồi `POST /report-schedules/RSC-2026-001/run-now` | 1 dòng run mới `SUCCESS`; `next_run_at` **không** bị dịch bởi `run-now` (§9.1) |

**Expected Result (PASS):** đúng 5 scenario trên; email đính kèm đi qua `ReportRowProjection` (kiểm PII ở `SEC-008`).

**Expected Result (FAIL):**
- Scenario 3 gửi 2 email → BGH nhận báo cáo trùng mỗi tuần; và nếu nhân lên theo số instance thì thành spam từ chính hệ thống
- Scenario 2 gửi email cho trường B → rò dữ liệu **và** vi phạm BR-176
- Scenario 4 có `attempt=4` → xem `MF13-TC-008`

**Current Status:** 🔴 Not written

---

#### `MF13-TC-INT-010` — §2b: hợp đồng phân trang + `SEARCH_FIELDS` của `report_exports`

**Severity:** `HIGH`
**Feature Under Test:** `GET /api/v1/report-exports` · `GET /api/v1/reports/{slug}/rows` · `GET /api/v1/reports`
**Test File:** `src/test/java/com/sfms/mf13/web/PagedResponseContractIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-017`, `TC-COND-018`
**Oracle Source:** `00_Team_Work_Assignment_Plan.md` **§2b-1** (5 field bất biến), **§2b-2** (query params), **§2b-3** (search chỉ trên `SEARCH_FIELDS`), **§2b-6** (5 edge case) · TDS **§9.1** (`SEARCH_FIELDS` = `code, report_title, period_label`) · **D8/S7**
**Technique:** Boundary Value Analysis + Error Guessing

| Bước | Request | Expected |
|---|---|---|
| a | `GET /report-exports?page=1&pageSize=8` | keys **đúng** `["items","page","pageCount","pageSize","total"]` — không wrapper, không thiếu field |
| b | `GET /reports/three-step/rows?page=99&pageSize=8` | `page` **đã clamp** = `pageCount`; `items` là trang cuối |
| c | lọc rỗng: `?status=KHONG_TON_TAI` | **200** `{items:[],total:0,page:1,pageSize:8,pageCount:1}` — **không** 404 (§2b-6) |
| d | `?pageSize=0` và `?pageSize=-1` | clamp về **1** |
| e | `?search=` (rỗng) | bỏ qua, kết quả như không truyền |
| f | `?search=RPX-2026` | tìm được qua `code` |
| g | `?search=Lưu mẫu` | tìm được qua `report_title` |
| h | `?search=Tháng 7` | tìm được qua `period_label` |
| i | `?search=<chuỗi chỉ có trong file_uri>` | **0 kết quả** — `search` **không** chạy trên `file_uri`/`checksum_sha256` (§2b-3 + không rò đường dẫn nội bộ) |
| j | `?dateFrom=2026-07-01&dateTo=2026-07-31` | lọc trên `generated_at` (TDS §2b-5 mở rộng cho MF13) |

**Expected Result (PASS):** đúng bảng trên; `pageCount ≥ 1` trong **mọi** trường hợp kể cả `total=0`.

**Expected Result (FAIL):**
- Thiếu `pageCount` hoặc bọc trong `{data:…}` → `usePagedList` của FE vỡ ở **cả 3** màn dùng nó
- i tìm ra kết quả → `search` OR trên mọi cột ⇒ full table scan (§2b lý do kỹ thuật) **và** rò `file_uri` qua kênh tìm kiếm

**Current Status:** 🔴 Not written

---

#### `MF13-TC-INT-011` — BR-177: purge job chỉ xoá SAU hạn, có audit

**Severity:** `HIGH`
**Feature Under Test:** `ReportExportPurgeJob`
**Test File:** `src/test/java/com/sfms/mf13/scheduler/ReportExportPurgeIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-005`
**Oracle Source:** **BR-177** (*"tối thiểu 12 tháng"*) · **ADR-M13-3** (*"KHÔNG xoá trước `expires_at` kể cả khi hết dung lượng"*) · §7.1 event `ReportExportPurged` · State machine **§6.5** · **UC-172**
**Technique:** Boundary Value Analysis

**Test Steps:**
1. **Arrange:** `FX-042` (`expires_at = now() - 1 tháng`, file giả trong `@TempDir`) + `FX-040` (`expires_at` = +24 tháng) + 1 bản `expires_at = now() + 1 giây`
2. **Act:** `purgeJob.run()`
3. **Assert:** bản nào bị xoá

**Expected Result (PASS):**
- `FX-042` bị xoá: dòng DB xoá **và** file trên storage xoá; 1 dòng audit + event `ReportExportPurged`
- `FX-040` và bản `+1 giây` **còn nguyên** (chưa quá hạn → không xoá, dù chỉ còn 1 giây)
- Chạy lại job → idempotent, không lỗi

**Expected Result (FAIL):**
- Xoá bản chưa quá hạn → mất bằng chứng tuân thủ trong thời gian BR-177 bảo vệ, **không khắc phục được**
- Xoá dòng DB mà để file mồ côi (hoặc ngược lại) → dung lượng phình / `download` trả 500 thay vì 410 `RPT-410`

**Current Status:** 🔴 Not written
**Implementation Note:** `download` một bản đã purge → **410 `RPT-410`** (message MSG-137, TDS §10) — assert luôn trong test này.

---

#### `MF13-TC-INT-012` — UC-171 + BR-175: `/reports` trả danh mục theo quyền, qua HTTP thật

**Severity:** `MEDIUM`
**Feature Under Test:** `GET /api/v1/reports`
**Test File:** `src/test/java/com/sfms/mf13/web/ReportCatalogIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-012`
**Oracle Source:** **UC-171** · **BR-175** · **CF-M13-7d** · TDS §9.2 (`GET /api/v1/reports`)
**Technique:** Decision Table (role × danh mục), tầng HTTP

**Expected Result (PASS):**
- `FX-021` → 200, `total=7`, có `traceability` + `system-usage`, `group` đúng (`COMPLIANCE` 5 / `ADDITIONAL` 2)
- `FX-022` (medical-staff) → 200, `total=6`, `exportable=false` ở **mọi** mục
- `FX-023`/`FX-024*` → **403** `RPT-005` (⛔ L5) — hoặc 200 với `total=0` nếu Nam chọn "danh mục rỗng thay vì 403"; **assert tham số hóa** giữa 2 nhánh, phần bắt buộc là **không** mục nào lọt ra
- Response là `PagedResponse` đủ 5 field (S7)

**Expected Result (FAIL):**
- Trả 7 mục cho medical-staff → vượt BR-175 (`system-usage` ngoài phạm vi Y tế)
- Trả 5 mục cho school-admin → CF-M13-7d không được gỡ

**Current Status:** 🔴 Not written

---

### 4.3. SECURITY TEST CASES — RBAC 7 role · cách ly cross-school · rò PII · gọi API bỏ qua UI

> ⚠️ **Bẫy false-green (xem §5.1):** test kỳ vọng 403/401 rất dễ PASS ngay với stub vì Spring Security mặc định chặn hết. **Mỗi** test dưới đây bắt buộc có **≥1 assertion dương** (một ô ✅ trong cùng test method) để Red Gate còn ý nghĩa.

---

#### `MF13-TC-SEC-001` — ★ Ma trận RBAC 7 role × 9 nhóm endpoint (§16)

**Severity:** `CRITICAL`
**Feature Under Test:** `@PreAuthorize` + `ReportAccessPolicy` trên 3 controller
**Test File:** `src/test/java/com/sfms/mf13/security/AuthorizationMatrixIT.java`
**TDD Phase:** 🔴 RED — ⚠️ **liên quan ⛔ L5** (3 ô Service Caterers)
**Condition Ref:** `TC-COND-021`, `TC-COND-025`
**Oracle Source:** TDS **§16** (ma trận đầy đủ) · **BR-175** · **BR-007** (RBAC 7 role) · **Actor traceability §1.13** · **MSG-005** → `RPT-005` · **MSG-004** → `RPT-004`
**Technique:** Combinatorial (pairwise) — `@ParameterizedTest` + `@MethodSource` sinh **63 tổ hợp** từ bảng §16

| # | Endpoint | SYS | SCHOOL | MEDICAL | CATERER | TEACHER | SUPPLIER | PARENT |
|---|---|---|---|---|---|---|---|---|
| a | `GET /compliance-report` | ✅ 200 | ✅ 200 | ✅ 200 | ❌ 403 ⛔ | ❌ 403 | ❌ 403 | ❌ 403 |
| b | `GET /compliance-report?allSchools=true` | ✅ 200 | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 |
| c | `GET /reports` | ✅ 200 | ✅ 200 | ✅ 200 | ❌ 403 ⛔ | ❌ 403 | ❌ 403 | ❌ 403 |
| d | `GET /reports/hygiene/rows` | ✅ 200 | ✅ 200 | ✅ 200 | ❌ 403 ⛔ | ❌ 403 | ❌ 403 | ❌ 403 |
| e | `GET /reports/system-usage/rows` | ✅ 200 | ✅ 200 | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 |
| f | `POST /reports/hygiene/export` | ✅ 201 | ✅ 201 | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 |
| g | `GET /report-exports` | ✅ 200 | ✅ 200 | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 |
| h | `GET /report-exports/{code}/download` | ✅ 200 | ✅ 200 | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 |
| i | `GET`/`POST` `/report-schedules` | ✅ | ✅ | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 | ❌ 403 |

**Assertion dương bắt buộc** (chống false-green): các ô ✅ ở dòng **a** (3 role), **e** (2 role), **f** (2 role) phải trả **200/201 kèm payload đúng shape**, không chỉ "khác 403".

**Expected Result (PASS):**
- 63/63 ô đúng bảng; mọi 403 có `code="RPT-005"`, `message="Access denied: You do not have permission to access this resource."` (**MSG-005 nguyên văn**, D7)
- Không JWT (`FX-026`) trên **mọi** endpoint → **401** `RPT-004`, message = **MSG-004 nguyên văn**
- JWT hết hạn → 401 (không phải 403)

**Expected Result (FAIL):**
- `medical-staff` xuất được (ô f) → vượt Actor UC-172; Y tế đưa file dữ liệu vận hành ra ngoài hệ thống
- `parent`/`teacher` đọc được `/compliance-report` → phụ huynh/giáo viên xem toàn bộ chỉ số vận hành + số cảnh báo ATTP của trường
- 401 bị trả thành 403 (hoặc ngược lại) → FE không phân biệt được "hết phiên" với "không có quyền", người dùng bị kẹt

**Current Status:** 🔴 Not written — ⚠️ 3 ô ⛔ (`CATERER` dòng a/c/d) đổi khi L5 chốt

---

#### `MF13-TC-SEC-002` — ★ BR-176: `schoolId` từ JWT, bỏ qua query/body (bypass UI)

**Severity:** `CRITICAL`
**Feature Under Test:** mọi controller MF13 (`AuthUser` resolver)
**Test File:** `src/test/java/com/sfms/mf13/security/JwtScopeAuthorityIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-022`
**Oracle Source:** **BR-176** nguyên văn (*"giới hạn trong phạm vi trường mà người dùng đang thuộc về"*) · TDS **§17.1 C2** + **AP-AI-006** · Logic Issue **L8** (FE đang lọc bằng **tên trường dạng chuỗi cứng** `filters={{ school: "TH Đoàn Thị Điểm" }}`)
**Technique:** Error Guessing (parameter tampering)

**Test Steps (JWT = `FX-021`, trường A):**

| # | Request | Expected |
|---|---|---|
| a | `GET /reports/system-usage/rows` (không param) | 200, **14** tài khoản của `FX-001` (baseline) |
| b | `GET /reports/system-usage/rows?schoolId=<mã trường B>` | 200, **vẫn 14** — param **bị bỏ qua** |
| c | `GET /reports/system-usage/rows?school=TH%20X` | 200, **vẫn 14** — copy hằng số FE cũng không tác dụng (L8) |
| d | `GET /reports/system-usage/rows?not:schoolId=<trường A>` | 200, **vẫn 14** — không thể phủ định phạm vi trường |
| e | `POST /reports/hygiene/export {"schoolId":"<trường B>", …}` | 201 nhưng bản xuất gắn **trường A**; nội dung file là dữ liệu trường A |
| f | `GET /compliance-report` với JWT thiếu claim `schoolId` (role school-admin) | **403** `RPT-005` — không suy diễn, không mặc định trường đầu tiên |

**Assertion dương bắt buộc:** dòng a trả **đúng 14** bản ghi (không phải "khác lỗi").

**Expected Result (PASS):** b–e trả **cùng** `total` như a; e ghi `report_exports.school_id` = trường A.

**Expected Result (FAIL):**
- b hoặc c đổi kết quả → **rò dữ liệu cross-school qua query param**; kẻ tấn công chỉ cần đổi 1 tham số URL. Đây là kịch bản nghiêm trọng nhất của MF13 vì `system-usage` chứa danh sách tài khoản nhân sự
- f trả 200 với dữ liệu trường nào đó → JWT thiếu claim mà hệ thống tự "đoán" phạm vi

**Current Status:** 🔴 Not written

---

#### `MF13-TC-SEC-003` — BR-176: đọc tài nguyên trường khác ⇒ **404** *(L4 đóng — `M13-D04`)*

**Severity:** `CRITICAL`
**Feature Under Test:** `GET /reports/{slug}/rows` · `GET /report-exports/{code}` · `PATCH /report-schedules/{code}`
**Test File:** `src/test/java/com/sfms/mf13/security/CrossSchoolIsolationIT.java`
**TDD Phase:** 🔴 RED — ~~⛔ CHẶN GREEN nhánh khẳng định mã (L4)~~ → ✅ **L4 đóng 2026-08-04**, TC chạy được trọn vẹn
**Condition Ref:** `TC-COND-023`
**Oracle Source:** **`M13-D04`** — quyết định **đã chốt 2026-07-31 và đang chạy trong code**: `mf02/error/SchExceptions.java:73` *"404 · SCH-404 · MSG-137 — không tồn tại HOẶC cross-school (BR-022)"*; `:67` *"403 · SCH-005 — sai vai trò TRONG CÙNG trường"*. Ba controller MF02 khai `@ApiResponse(404, "…bao gồm cross-school, BR-022")`. ⇒ MF13 **áp lại nguyên**, không quyết lại. `TDS-MF13` §10/§16 vốn đã đúng.
**Technique:** Assertion khẳng định (một nhánh) + assertion bất biến

> **Vì sao L4 không phải là mâu thuẫn thật:** `TDS-MF02_Backend.md` §16 (văn bản) ghi *"cross-school → 403 SCH-005"*, nhưng `TestSpec-MF02` **L7** đã bắt được và **MF02 đã sửa ở CODE ngày 31/07** theo hướng 404. TestSpec-MF13 viết 30/07 nên đọc phải bản tài liệu cũ. **Bài học: khi hai TDS mâu thuẫn mà một trong hai đã có code chạy, code là bên đúng — không cần cuộc họp nào** (`00_` §0-BIS). Việc còn lại là **Mạnh/Huy sync câu văn §16 của `TDS-MF02`** cho khỏi bẫy người tiếp theo.

**Test Steps (JWT = `FX-025`, trường B; tài nguyên thuộc trường A):**
1. `GET /report-exports/RPX-2026-001` (của `FX-040`, trường A)
2. `PATCH /report-schedules/RSC-2026-001 {active:false}` (`FX-030`, trường A)
3. `GET /reports/traceability/rows` — assert chỉ thấy **3 lô của `FX-002`**, không thấy 12 lô của `FX-001` (`FX-015`)

**Expected Result (PASS):**
- **Mã trả về (`M13-D04` — một nhánh, không tham số hoá):** bước 1 và 2 trả **`404`** với `error.code = "RPT-404"` và `error.message` = **MSG-137 nguyên văn**. **Không** chấp nhận 403 ở tình huống cross-school — 403 chỉ dùng cho **sai vai trò trong cùng trường** và cho `allSchools=true` sai quyền (`RPT-005`, phủ ở `TC-SEC-007`).
- **Phần bất biến (độc lập với mã trả về):**
  - response **không** chứa bất kỳ field dữ liệu nào của trường A: không `fileUri`, không `checksumSha256`, không `rowCount`, không `reportTitle`
  - `FX-030.active` **vẫn `true`** sau bước 2 (assert DB) — thao tác không được thực thi
  - bước 3: `total = 3` (chỉ trường B) — **assertion dương**, chống false-green
- Trường A và B **không** thấy `code` của nhau trong `GET /report-exports` (assert cả 2 chiều)

**Expected Result (FAIL):**
- Bước 3 trả 15 lô → BR-176 vỡ ở tầng dữ liệu báo cáo: trường B thấy chuỗi truy xuất nguyên liệu của trường A (thông tin nhà cung cấp, giá trị thương mại)
- Bước 2 đổi `active` dù trả lỗi → thao tác thực thi trước khi kiểm quyền

**Current Status:** 🔴 Not written — **không còn blocked** (`M13-D04`)
**Implementation Note:** dùng **tài khoản seed thật** `U-0018` (school-admin, `SCH-HN-0051` = trường B) — **D13**, `@WithMockUser` không mang claim `schoolId` nên không tái hiện được cách ly. Trường A là `SCH-HN-0042` (`U-0001`). Cả hai đã có trong `V900__test_fixtures.sql`, mật khẩu `P@ssw0rd123`.

---

#### `MF13-TC-SEC-004` — ★ BR-181: JSON của báo cáo không chứa PII học sinh

**Severity:** `CRITICAL`
**Feature Under Test:** `GET /reports/{slug}/rows` · `GET /compliance-report`
**Test File:** `src/test/java/com/sfms/mf13/security/PiiLeakJsonIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-004`, `TC-COND-026`
**Oracle Source:** **BR-181** nguyên văn · **ADR-M13-4** · fixture "mìn" `FX-017` (3 HS thật: `HS-3A-001`, tên thật, dị ứng `SEVERE`) · **UC-171**
**Technique:** Data Flow / Taint Testing (quét toàn body)

**Test Steps:**
1. **Arrange:** `FX-017` tồn tại trong DB và **liên quan** tới dữ liệu nguồn (mẫu lưu của bữa HS đó ăn, phiên phân suất của lớp HS đó)
2. **Act:** gọi **cả 7** `GET /reports/{slug}/rows?pageSize=100` + `GET /compliance-report`
3. **Assert:** quét body thô bằng `Mf13Fixtures.piiNeedles()`

**Expected Result (PASS):**
- **0** lần xuất hiện của: `"HS-3A-001"`, `"Nguyễn Minh Anh"`, `"studentId"`, `"studentName"`, `"parentName"`, `"diagnosis"` trong **8** response
- Số liệu về HS chỉ ở dạng **đếm** (VD `affectedStudents: 12`) nếu có
- **Assertion dương:** slug `system-usage` **có** `name` (tên nhân sự) và `role` — chứng minh test không xanh vì response rỗng
- `sample-retention` trả đúng **48** dòng (dữ liệu vẫn đầy đủ, chỉ thiếu PII)

**Expected Result (FAIL):**
- Bất kỳ needle nào xuất hiện → **vi phạm PDPD + BR-181**; TDS §12.1 xếp đây là điều kiện rollback có **thông báo DPO**, không chỉ hotfix
- Response rỗng ở mọi slug → test "xanh" mà không chứng minh gì (đây là lý do có assertion dương)

**Current Status:** 🔴 Not written

---

#### `MF13-TC-SEC-005` — ★ BR-181: file PDF/XLSX không chứa PII + có nhãn Confidential

**Severity:** `CRITICAL`
**Feature Under Test:** `POST /reports/{slug}/export` → `GET /report-exports/{code}/download`
**Test File:** `src/test/java/com/sfms/mf13/security/PiiLeakFileIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-004`, `TC-COND-026`
**Oracle Source:** **BR-181** nguyên văn (*"Báo cáo xuất ra ngoài hệ thống phải được đánh dấu **Confidential**"*) · **BR-178** · **ADR-M13-4** · **UC-172**
**Technique:** Data Flow Testing trên **cửa ra nguy hiểm nhất** (file rời hệ thống)

**Test Steps:**
1. **Act:** xuất **cả 7** slug × **2** format = 14 file
2. **Act:** đọc lại: PDF bằng `PDFTextStripper`, XLSX bằng POI (quét **mọi** cell + cả `sheetName`, header/footer, và **metadata tài liệu** `PDDocumentInformation`)
3. **Assert:** quét `piiNeedles()` + kiểm nhãn

**Expected Result (PASS):**
- 14/14 file: **0** needle PII, kể cả trong metadata (`Author`, `Subject`, `Keywords`) và tên sheet
- 14/14 file chứa `"Confidential"` + đủ 4 field header BR-178
- **Assertion dương:** file `sample-retention` chứa `"SMP-20260706-01"` và `"Canh bí đỏ"` — dữ liệu nghiệp vụ **có**, PII **không**

**Expected Result (FAIL):**
- PII trong metadata PDF (chỗ dễ bỏ sót nhất — người xem không thấy nhưng `exiftool` thấy) → rò dữ liệu qua file đã phát hành cho bên thứ ba, **không thu hồi được**
- Thiếu nhãn `Confidential` → vi phạm BR-181; file lọt ra ngoài không có cảnh báo phân loại

**Current Status:** 🔴 Not written
**Implementation Note:** đặt `PDDocumentInformation` tường minh (`Author = tên trường`, `Subject = tiêu đề báo cáo`) — **không** để PDFBox tự điền từ dữ liệu hàng.

---

#### `MF13-TC-SEC-006` — BR-176: đoán mã `RPX-…` của trường khác để tải file

**Severity:** `CRITICAL`
**Feature Under Test:** `GET /api/v1/report-exports/{code}/download`
**Test File:** `src/test/java/com/sfms/mf13/security/ReportDownloadAuthzIT.java`
**TDD Phase:** 🔴 RED — ⚠️ liên quan **L4**
**Condition Ref:** `TC-COND-023`
**Oracle Source:** **BR-176** · TDS **§4.3 File access** (*"kiểm quyền + `school_id` **mỗi lần tải**; không public link"*) · **§10** (`RPT-404` — *"trả 404 thay 403 để không rò tồn tại"*) · **UC-172**
**Technique:** Error Guessing (IDOR — mã nghiệp vụ **đoán được** theo thiết kế D1)

**Test Steps:**
1. **Arrange:** `FX-040` (`RPX-2026-001`, trường A), `FX-041` (bản xuất trường B)
2. **Act (JWT `FX-025`, trường B):** `GET /report-exports/RPX-2026-001/download`
3. **Act:** thử `RPX-2026-002`, `RPX-2026-003`, `RPX-2025-001` (đoán tuần tự — mã D1 **cố ý** dễ đọc)
4. **Act:** tải **không** JWT
5. **Act (assertion dương):** JWT `FX-025` tải bản xuất **của chính trường B**

**Expected Result (PASS):**
- Bước 2–3: **404** `RPT-404` cho **mọi** mã không thuộc trường B — **cùng** phản hồi cho "mã không tồn tại" và "mã của trường khác" (không phân biệt được ⇒ không đếm được số bản xuất của trường khác)
- Bước 4: **401** `RPT-404`→ đúng hơn: **401** `RPT-004`; **không** có đường tải nào bỏ qua JWT
- Bước 5: **200** + file đúng của trường B (**assertion dương**)
- **Không** endpoint nào trả URL tải trực tiếp tới storage (`fileUri` **không** xuất hiện trong bất kỳ response nào — assert tường minh)

**Expected Result (FAIL):**
- Bước 2 trả 200 → **IDOR**: mã nghiệp vụ dễ đoán (`RPX-2026-001`) + không kiểm `school_id` ⇒ tải được toàn bộ báo cáo mọi trường. D1 chọn mã đọc được **với điều kiện** quyền được kiểm mỗi lần tải; test này là chỗ điều kiện đó được xác minh
- Trả 403 ở bước 2 và 404 ở bước 3 → phân biệt được "tồn tại nhưng không được xem" ⇒ rò thông tin về số lượng/quy luật mã của trường khác
- `fileUri` lộ trong response → tải trực tiếp từ storage, bỏ qua toàn bộ kiểm quyền

**Current Status:** 🔴 Not written

---

#### `MF13-TC-SEC-007` — BR-176: `allSchools=true` chỉ System Admin

**Severity:** `HIGH`
**Feature Under Test:** `GET /api/v1/compliance-report?allSchools=true`
**Test File:** `src/test/java/com/sfms/mf13/security/MultiSchoolScopeIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-024`
**Oracle Source:** **BR-176** nguyên văn — *"Chỉ **System Admin** được phép xem báo cáo tổng hợp đa trường"* · TDS **§16** · **MSG-005** → `RPT-005`
**Technique:** Decision Table

**Expected Result (PASS):**
- `FX-020` (system-admin) → **200**, số liệu gộp **cả 2 trường**: `three-step.total = 26` (25 của `FX-010` + 1 của `FX-018`) — **assertion dương**
- `FX-021` (school-admin) → **403** `RPT-005`
- `FX-022` (medical-staff) → **403** `RPT-005`
- `?allSchools=1`, `?allSchools=TRUE`, `?allSchools=yes` với school-admin → **403** (không lách bằng biến thể chuỗi)
- school-admin **không** kèm param → 200 nhưng chỉ trường mình (`total = 25`)

**Expected Result (FAIL):**
- school-admin nhận 200 với `total=26` → rò số liệu trường khác qua **một tham số boolean**; đây là chỗ dễ sai nhất vì tham số nhìn "vô hại"
- `allSchools=true` bị **im lặng bỏ qua** cho school-admin (trả 200 với dữ liệu trường mình) → chấp nhận được về bảo mật nhưng **che** lỗi cấu hình client; TDS §10 chốt **403**, test bám TDS

**Current Status:** 🔴 Not written

---

#### `MF13-TC-SEC-008` — BR-179 + BR-181: email đính kèm không rò PII, không gửi ngoài danh sách

**Severity:** `CRITICAL`
**Feature Under Test:** `ReportScheduleRunner` + `MailSender` (GreenMail)
**Test File:** `src/test/java/com/sfms/mf13/security/ScheduledEmailEgressIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-009`, `TC-COND-026`
**Oracle Source:** **BR-179** (người nhận theo cấu hình School Administration) · **BR-181** (không PII, nhãn Confidential) · TDS **§4.3 Email egress** · **UC-173**
**Technique:** Data Flow Testing trên **cửa ra khó thu hồi nhất** (email)

**Test Steps:**
1. **Arrange:** `FX-030` đến hạn; `FX-003` = 2 email; `FX-017` (HS thật) liên quan dữ liệu nguồn
2. **Act:** `runner.tick()`
3. **Assert:** GreenMail — người nhận, tiêu đề, thân thư, **file đính kèm**

**Expected Result (PASS):**
- **Đúng 2** người nhận, đúng địa chỉ của `FX-003`; **0** email tới địa chỉ nào khác (kể cả BCC — assert `getAllRecipients()`)
- Thân thư + tiêu đề **không** chứa needle PII; tiêu đề có tên trường + kỳ dữ liệu
- File đính kèm: mở bằng POI, quét mọi cell → **0** needle PII; **có** `"Confidential"`
- **Assertion dương:** file đính kèm có ≥1 dòng dữ liệu thật (`hygiene` → mã phiên kiểm tra bếp của `FX-014`)
- **Không** đính kèm đường dẫn tải công khai trong thân thư (assert body không chứa `http` tới storage)

**Expected Result (FAIL):**
- PII trong file đính kèm → dữ liệu học sinh **đã ra khỏi hệ thống qua email**, không thu hồi được; đây là hệ quả tệ nhất trong toàn bộ MF13
- Có BCC ẩn / địa chỉ lạ → kênh phát tán
- Thân thư chứa link tải không cần đăng nhập → vô hiệu hoá kiểm quyền của `SEC-006`

**Current Status:** 🔴 Not written

---

#### `MF13-TC-SEC-009` — `search`: injection + không rò cột nội bộ

**Severity:** `HIGH`
**Feature Under Test:** `ReportExportSpec` (Specification cho `GET /report-exports`) · `/reports/{slug}/rows`
**Test File:** `src/test/java/com/sfms/mf13/security/SearchInjectionIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-018`
**Oracle Source:** `00_Team_Work_Assignment_Plan.md` **§2b-3** (search chỉ trên `SEARCH_FIELDS`) + **§2b-7** (checklist reviewer) · TDS **§9.1** (`SEARCH_FIELDS` = `code, report_title, period_label`) · **§4.3**
**Technique:** Error Guessing

| Input | Expected |
|---|---|
| `?search=' OR 1=1 --` | 200, **0** kết quả; DB không lỗi; **không** trả toàn bộ bảng |
| `?search=%' UNION SELECT ...` | 200, 0 kết quả, không lỗi 500 |
| `?not:file_uri=x` | 200, kết quả **không đổi** — không lọc được trên cột ngoài whitelist |
| `?sort=checksum_sha256` | 200, kết quả **không đổi** — không sắp xếp theo cột nội bộ |
| `?search=<đoạn của file_uri>` | **0** kết quả (§2b-3) |
| `?pageSize=1000000` | clamp về trần cấu hình (VD 200), **không** OOM |
| `?search=Lưu mẫu` (**assertion dương**) | ≥1 kết quả qua `report_title` |

**Expected Result (PASS):** đúng bảng trên; không response nào chứa `fileUri`/`checksumSha256`/tên bảng/tên constraint.

**Expected Result (FAIL):**
- `' OR 1=1 --` trả toàn bộ bảng → nối chuỗi SQL thay vì `Specification`/parameter binding
- Lọc được trên `file_uri` → dò đường dẫn storage của trường khác bằng cách thử từng ký tự (blind enumeration)
- `pageSize=1000000` chạy thật → DoS bằng một request

**Current Status:** 🔴 Not written

---

### 4.4. CONTRACT TEST CASES — OpenAPI `/v3/api-docs` (**D8**, bắt buộc theo prompt §2c-3b)

> Nhóm này tồn tại vì `/v3/api-docs` là **nguồn sinh type FE** (`review_fix_plan.md` T3.4 `openapi-typescript`) và là **cổng nghiệm thu** (`01_MVP…` §Nghiệm thu). Spec sai ⇒ D1/D2/D4/D6 tái phát qua file sinh tự động, khó thấy hơn nhiều so với lệch tay.

---

#### `MF13-TC-CON-001` — D1 + D2: không `format: uuid` ở `id`; mọi 4xx/5xx trỏ `ErrorResponse`

**Severity:** `HIGH`
**Feature Under Test:** springdoc annotation của 3 controller MF13
**Test File:** `src/test/java/com/sfms/mf13/contract/OpenApiIdAndErrorContractIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-027`
**Oracle Source:** **D8 luật S6** (*"`example` của `id` là mã nghiệp vụ; không có UUID trong bất kỳ `example`/`@Schema` công khai nào"*) · **S3** (*"mỗi mã lỗi ở §10 có một `@ApiResponse`, `content` trỏ `ErrorResponse` dùng chung"*) · **D1**, **D2** · TDS **§10** (6 mã `RPT-*` + cột HTTP)
**Technique:** Contract Testing (JSON-path trên spec)

**Test Steps:**
1. **Act:** `GET /v3/api-docs` (`permitAll` theo D8 §Lưu ý hạ tầng)
2. **Assert:** lọc các path bắt đầu `/api/v1/compliance-report`, `/api/v1/reports`, `/api/v1/report-exports`, `/api/v1/report-schedules`

**Expected Result (PASS):**
- **0** property tên `id` có `format: "uuid"` (D1/S6)
- **0** `example` nào khớp regex UUID `^[0-9a-f]{8}-[0-9a-f]{4}-...`; `example` của `id` khớp `^(RPX|RSC)-\d{4}-\d{3}$` (S6)
- **Mọi** response `4xx`/`5xx` của endpoint MF13 có `content.application/json.schema.$ref` = `#/components/schemas/ErrorResponse` (S3/D2)
- Số `@ApiResponse` lỗi khai ⊇ 6 mã của §10 (401, 403, 404, 410, 422, 409) — khớp **1-1** với cột HTTP của §10
- `ErrorResponse` có đúng 3 property `code`/`message`/`details`; `details` là **array** (D2)

**Expected Result (FAIL):**
- `id` khai `format: uuid` → `openapi-typescript` sinh `id: string /* uuid */`, FE điều hướng bằng UUID ⇒ D1 tái phát qua file sinh tự động (đúng nguy cơ D8 nêu)
- 4xx không trỏ `ErrorResponse` → type lỗi ở FE là `any`, interceptor không có hợp đồng

**Current Status:** 🔴 Not written

---

#### `MF13-TC-CON-002` — D4 + S4: mọi field ngày có `format` date / date-time

**Severity:** `HIGH`
**Feature Under Test:** DTO của MF13 (`@Schema(type="string", format=...)`)
**Test File:** `src/test/java/com/sfms/mf13/contract/OpenApiDateFormatContractIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-027`, `TC-COND-014`
**Oracle Source:** **D8 luật S4** · **D4** (ISO-8601) · TDS §9.2
**Technique:** Contract Testing

**Expected Result (PASS):**
- Mọi property tên khớp `.*(At|Date|From|To)$` trong schema MF13 có `format ∈ {"date","date-time"}` — cụ thể: `generatedAt`/`expiresAt`/`nextRunAt`/`createdAt`/`updatedAt` = `date-time`; `periodFrom`/`periodTo` = `date`
- Mọi `example` của các field đó là **chuỗi ISO** (không `30/07/2026`)
- `timeOfDay` khai `format: "partial-time"` hoặc pattern `^\d{2}:\d{2}$` (không để `string` trơn)
- **Ngoại lệ tường minh:** `periodLabel` **không** phải field ngày (nhãn hiển thị) — assert nó **không** bị đòi `format`

**Expected Result (FAIL):**
- Field ngày khai `type: string` trơn → FE nhận `string`, tự parse theo locale ⇒ lệch D4 quay lại từ phía FE
- `example` là `dd/MM/yyyy` → developer copy example vào test/dữ liệu thật

**Current Status:** 🔴 Not written

---

#### `MF13-TC-CON-003` — D6 + S5/S7: enum khai bằng kiểu thật; GET danh sách trả `PagedResponse`

**Severity:** `MEDIUM`
**Feature Under Test:** springdoc schema của `ReportSlug`/`ReportFormat`/`ReportFrequency` + response của 3 `GET` danh sách
**Test File:** `src/test/java/com/sfms/mf13/contract/OpenApiEnumAndPagingContractIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-011`, `TC-COND-027`
**Oracle Source:** **D8 luật S5** (*"enum khai bằng kiểu enum thật — KHÔNG `@Schema(allowableValues=…)` viết tay"*) + **S7** (*"mọi `GET` danh sách khai response là `PagedResponse<T>`"*) · **D6** (8 enum chung + ngoại lệ `RoleKey` kebab-case) · **ADR-M13-5**
**Technique:** Contract Testing

**Expected Result (PASS):**
- `components.schemas.ReportSlug.enum` = **đúng 7** giá trị **kebab-case** (`three-step`…`system-usage`) — khớp `MF13-TC-011` và 7 route FE
- `ReportFormat.enum` = `["PDF","XLSX"]`; `ReportFrequency.enum` = `["DAILY","WEEKLY","MONTHLY"]`; `ScheduleRunStatus.enum` = `["PENDING","SUCCESS","FAILED"]` — **SCREAMING_SNAKE** (D6)
- **0** chỗ dùng `allowableValues` trong toàn spec MF13 (S5) — kiểm bằng cách grep chuỗi trong code lẫn spec
- MF13 **không** khai lại schema của 8 enum chung D6 (`MealType`, `AllergySeverity`, `PartnerType`, `RoleKey`, `NotificationType`, `HealthHistoryEventType`, `ConsumptionStatus`, `BiometricStatus`) — nếu tham chiếu thì `$ref` tới schema của `common`
- `GET /reports`, `GET /reports/{slug}/rows`, `GET /report-exports` khai response `$ref` tới `PagedResponse` (5 property) — S7

**Expected Result (FAIL):**
- `ReportSlug` khai SCREAMING_SNAKE trong spec → type FE sinh ra không khớp route hiện có, và ADR-M13-5 mất tác dụng
- Dùng `allowableValues` → danh sách giá trị sống ở 2 nơi (annotation + enum), lệch nhau lúc thêm slug thứ 8
- `GET` danh sách khai `array` thay vì `PagedResponse` → FE `usePagedList` mất `total`/`pageCount`

**Current Status:** 🔴 Not written

---

#### `MF13-TC-CON-004` — S1/S2/S8 + ranh giới module (L1): `@Tag`, UC+BR trong description, không chạm `sent-reports`

**Severity:** `HIGH`
**Feature Under Test:** `@Tag`, `@Operation.description`, `@SecurityRequirement` của 3 controller MF13
**Test File:** `src/test/java/com/sfms/mf13/contract/OpenApiTraceabilityContractIT.java`
**TDD Phase:** 🔴 RED — ~~⚠️ liên quan ⛔ L1~~ → ✅ **L1 đóng (`M13-D03`)**; TC giữ nguyên làm **regression guard**
**Condition Ref:** `TC-COND-027`, `TC-COND-028`
**Oracle Source:** **D8 luật S1** (`@Tag(name="MFxx — <Tên module>")` theo Team Plan §2c-0) · **S2** (*"`description` PHẢI chứa UC ID + BR ID"*) · **S8** (`@SecurityScheme` bearer toàn cục; endpoint public ghi `@SecurityRequirements` rỗng) · **ADR-M13-2** + **CF-M13-1** (MF13 không sở hữu `sent_reports`)
**Technique:** Contract Testing + kiểm ranh giới module

**Expected Result (PASS):**
- Có tag **`"MF13 — Reporting & Analytics"`** (đúng tên module ở Team Plan §2c-0 bảng dòng MF13)
- **Mọi** operation mang tag đó có `description` chứa ≥1 mã khớp `UC-17[123]` **và** ≥1 mã khớp `BR-1(7[5-9]|8[0-2])` (S2) — tức chỉ UC/BR **của MF13**, không mã module khác
- **0** operation nào của MF13 là public: tất cả có `security` bearer (S8); **không** endpoint MF13 nào có `@SecurityRequirements` rỗng
- **Ranh giới (L1):** **0** path của tag MF13 chứa `sent-report`; **0** schema của MF13 tên `SentReport*`; `description` của MF13 **không** chứa `UC-185|UC-187|UC-188|UC-189|UC-190|BR-209|BR-213|BR-214` (những mã đó thuộc **MF12**)
- Trong toàn spec, path `/api/v1/…sent-reports…` (nếu có) mang tag **MF12**, không phải MF13

**Expected Result (FAIL):**
- `description` thiếu UC/BR → truy vết chết ở runtime doc; Mạnh (Lead Backend reviewer) được quyền reject PR theo D8
- Xuất hiện endpoint `sent-reports` dưới tag MF13 → **hai module cùng sở hữu một bảng** (CF-M13-1 chưa chốt mà đã gen code) ⇒ đúng loại lỗi mà file `00_Common_Conventions.md` được lập ra để chặn
- Tag ghi `"MF13 - Reporting and Analytics"` (sai dấu gạch/tên) → `swagger-ui` gom sai nhóm, nghiệm thu 13 nhóm không khớp

**Current Status:** 🔴 Not written

> **Cập nhật 2026-08-04 (`M13-D03`):** ⛔ **L1 đã đóng** — MF12 merge `sent_reports` ở `V23`, `mf12/web/SentReportController` đang chạy. TC này **không còn** là "chờ chốt ranh giới" mà là **regression guard**: nó bắt trường hợp ai đó gen lại MF13 từ bản TDS cũ và tạo bảng trùng. Giữ nguyên mọi assert; bỏ nhãn ⛔.

---

#### `MF13-TC-CON-005` — **D11**: `GET /reports/{slug}/rows` đổi shape theo vai trò ⇒ phải khai `oneOf`

**Severity:** `HIGH`
**Feature Under Test:** `ReportRowController.rows()` — response shape theo vai trò + schema `/v3/api-docs`
**Test File:** `src/test/java/com/sfms/mf13/contract/ReportRowsRoleShapeContractIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-029` *(mới)*
**Oracle Source:** **D11** (`02_Module_Completion_Workflow.md` §6) · **BR-181** (loại PII) · **BR-176** (phạm vi trường) · **S7** (tên schema giữ tiền tố `PagedResponse`) · mẫu: `StudentDtos.PagedResponseStudentResponse`, `MF09-TC-CON-006`
**Technique:** Contract Testing + so tập field giữa vai trò

> **Vì sao MF13 áp dụng D11 chứ không phải "không áp dụng":** phân biệt của §6 là **lọc DÒNG** (không cần `oneOf`, như MF06/MF07) hay **lọc FIELD** (bắt buộc `oneOf`). MF13 lọc **field**: `ReportRowProjection` (ADR-M13-4) whitelist cột **theo slug và theo vai trò** — cùng một slug `receiving`, System Admin thấy đủ cột, vai trò hạn chế bị cắt bớt. Đây là **lần thứ tư** hệ thống gặp pattern này (`GET /schools`, `GET /students`, `GET /student-distributions`, nay `GET /reports/{slug}/rows`), và ba lần trước đều tốn công dò bằng curl.

**Expected Result (PASS):**
- Gọi `GET /api/v1/reports/receiving/rows` bằng **2 tài khoản seed thật** (**D13** — `@WithMockUser` không mang claim `schoolId`): `U-9001` (system-admin) và `U-0001` (school-admin, `SCH-HN-0042`)
- Tập field của `items[0]` **khác nhau** giữa hai vai trò, và vai trò hạn chế **không** có field nào nằm ngoài whitelist của nó
- **Không** vai trò nào thấy field PII học sinh (giao với BR-181 — `MF13-TC-SEC-004` phủ sâu hơn)
- `/v3/api-docs`: schema của response khai **`oneOf`** với ≥2 biến thể; **`items` ĐÚNG KIỂU**, không phải `{}`
- Tên schema **giữ tiền tố `PagedResponse`** (S7 — `OpenApiMetaContractIT` chốt quy ước này)

**Expected Result (FAIL):**
- Chữ ký controller là `PagedResponse<?>` → springdoc **xoá sạch kiểu**, `items` tụt xuống `{}`, `npm run gen:api` sinh type vô dụng, FE phải đăng nhập từng vai trò gọi thử mới biết — đúng chi phí mà D11 được lập ra để tránh
- ⚠️ **KHÔNG** "sửa" bằng cách đặt `name=` cho `@Schema` generic — xem chú thích tại chỗ trong `common/dto/PagedResponse.java`; làm vậy hỏng type của **cả 11 endpoint danh sách** đang chạy

**Current Status:** 🔴 Not written

---

#### `MF13-TC-INT-013` — **D12**: field DẪN XUẤT và field sai KIỂU gửi lên `POST /report-schedules` phải KHÔNG được lưu

**Severity:** `HIGH`
**Feature Under Test:** `ReportScheduleService.create()` — cặp Request/Response DTO
**Test File:** `src/test/java/com/sfms/mf13/web/ReportScheduleWriteContractIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-030` *(mới)*
**Oracle Source:** **D12** (`02_Module_Completion_Workflow.md` §6) · TDS §9.2 · mẫu: `TestSpec-MF05` `TC-027`
**Technique:** Negative testing — bẫy "200 mà không lưu gì" của Jackson

> **Field DẪN XUẤT (read-only) của `ReportSchedule`** — FE **không** được gửi lên, backend **không** đọc từ body: `code` (sinh bởi `CodeGenerator`, `scope="mf13_report_schedules:<năm>"` theo **C1**/`M13-D05` tiền lệ MF03), `nextRunAt` (tính từ `frequency`+`timeOfDay`+`zoneId`), `createdBy`/`createdAt`/`updatedAt` (`BaseEntity` + `AuditorAware`), `runs[]`.
> ⚠️ **Ngoại lệ có chủ đích:** `body.id` **được** nhận — đó là **D5** (client gửi mã, trùng ⇒ **409 `RPT-409`**), khác hẳn với việc nhận `code` như một field sửa được. Hai thứ này dễ lẫn; `TC-INT-008` phủ nhánh D5.

**Expected Result (PASS):**
- (a) `POST /api/v1/report-schedules` với body kèm `"nextRunAt": "2030-01-01T00:00:00Z"` và `"createdBy": "root@sfms.vn"` → **201**, nhưng `report_schedules.next_run_at` là **giá trị backend tự tính** (không phải 2030) và `created_by` là **email của JWT**, không phải giá trị gửi lên
- (b) `POST` với `"frequency": 3` (đúng tên, **sai kiểu** — số thay vì enum string) → **422 `RPT-138`** (MSG-138), **0 dòng** thêm vào `report_schedules`
- (c) Mọi field **sửa được** (`reportSlug`, `frequency`, `format`, `timeOfDay`, `dayOfWeek`, `dayOfMonth`, `zoneId`, `active`) có **cùng tên JSON** ở cả Request lẫn Response — assert bằng cách so 2 tập key, không so thủ công

**Expected Result (FAIL):**
- Backend nhận `nextRunAt` từ body → lịch chạy sai giờ **âm thầm**, không lỗi, form báo thành công
- (b) trả **200/201** thay vì 422 → Jackson đã bỏ qua field sai kiểu; đây là biến thể *"cùng tên, khác KIỂU"* mà §6 D12 cảnh báo là **khó thấy hơn** lệch tên, vì review bằng mắt thì hai bên *trông như khớp*

**Current Status:** 🔴 Not written

---

#### `MF13-TC-INT-014` — `report_exports` append-only (BR-182) hoạt động đúng khi entity `extends BaseEntity`

**Severity:** `MEDIUM`
**Feature Under Test:** trigger `report_exports_no_update` × `@LastModifiedDate` của `BaseEntity`
**Test File:** `src/test/java/com/sfms/mf13/repository/ReportExportImmutabilityIT.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `TC-COND-031` *(mới)*
**Oracle Source:** **BR-182** (*"không cho phép chỉnh sửa thủ công sau khi tạo"*) · **ADR-M13-3** · **D9**/`M13-D02`
**Technique:** Integration — kiểm tương tác giữa ràng buộc DB và hạ tầng audit

> **Vì sao cần TC riêng:** `M13-D02` cho `ReportExport extends BaseEntity`, mà `BaseEntity` có `@LastModifiedDate updated_at`. Hai cơ chế này **chỉ** cùng tồn tại được nếu service **không bao giờ** `save()` lại một bản ghi đã persist. Nếu lỡ làm, trigger raise và transaction rollback — **đúng ý đồ**, nhưng lỗi hiện ra dưới dạng `DataIntegrityViolationException` khó truy. TC này biến "đúng ý đồ" thành thứ đo được.

**Expected Result (PASS):**
- `POST /reports/hygiene/export` → **201**, 1 dòng `report_exports`, `created_at` được điền, `updated_at` **NULL**
- `UPDATE report_exports SET row_count = 999 WHERE …` bằng SQL thô → PostgreSQL raise `report_exports is append-only (BR-182)`
- Gọi lại `GET /report-exports/{code}` sau đó → dữ liệu **không đổi**, checksum khớp file trên storage
- `DELETE` qua API → **405/404**, không có endpoint xoá (retention chỉ do purge job sau `expires_at`)

**Expected Result (FAIL):**
- `updated_at` khác NULL ngay sau khi tạo → có ai đó `save()` hai lần trong cùng transaction; trigger chưa bắt vì `BEFORE UPDATE` chỉ chạy khi Hibernate flush ra `UPDATE` thật
- `UPDATE` thô thành công → trigger chưa được tạo trong `V25` ⇒ BR-182 chỉ còn là quy ước ở tầng service, đúng thứ ADR-M13-3 từ chối

**Current Status:** 🔴 Not written

---

## 5. Red-Green-Refactor Tracker

> **37 test case** — 12 Unit · 12 Integration · 9 Security · 4 Contract. Cột `🔴 RED` phải tick **trước** khi implement (xem §5.1).
> ⛔ = blocked bởi Logic Issue ở §2, **không** đưa vào Exit Criteria cho tới khi được chốt · ★ = bắt buộc pass trước final approve.

| TC ID | Test File | Severity | Oracle | 🔴 RED | 🟢 GREEN (commit) | 🔵 REFACTOR |
|---|---|---|---|---|---|---|
| `MF13-TC-001` | `service/ReportExportFormatTest.java` | HIGH | BR-178 / MSG-138 | `[ ]` | | |
| `MF13-TC-002` ★ | `service/ComplianceSummaryComputationTest.java` | **CRITICAL** | BR-182 (L6) | `[ ]` | | |
| `MF13-TC-003` | `render/ReportRendererHeaderTest.java` | HIGH | BR-178 + BR-181 | `[ ]` | | |
| `MF13-TC-004` ★ | `service/ReportRowProjectionTest.java` | **CRITICAL** | BR-181 / ADR-M13-4 | `[ ]` | | |
| `MF13-TC-005` | `service/ReportRetentionTest.java` | HIGH | BR-177 | `[ ]` | | |
| `MF13-TC-006` ⛔ | `service/ReportAccessPolicyTest.java` | **CRITICAL** | BR-175 · ⛔ **L5** (3 ô Caterer) | `[ ]` | | |
| `MF13-TC-007` | `service/ReportScheduleComputeTest.java` | HIGH | BR-179 / MSG-138 | `[ ]` | | |
| `MF13-TC-008` | `scheduler/ReportScheduleRetryTest.java` | **CRITICAL** | BR-179 · ~~⛔ L3~~ → ✅ `M13-D08` | `[ ]` | | |
| `MF13-TC-009` | `service/ReportRecipientSourceTest.java` | **CRITICAL** | BR-179 / §4.3 | `[ ]` | | |
| `MF13-TC-010` | `domain/ReportExportImmutabilityTest.java` | HIGH | BR-182 / ADR-M13-3 | `[ ]` | | |
| `MF13-TC-011` | `domain/ReportSlugSerializationTest.java` | MEDIUM | ADR-M13-5 / D6 | `[ ]` | | |
| `MF13-TC-012` ⛔ | `service/ReportCatalogServiceTest.java` | MEDIUM | UC-171 / BR-175 · ⛔ **L5** | `[ ]` | | |
| `MF13-TC-INT-001` ★ | `repository/ReportViewContractIT.java` | **CRITICAL** | ADR-M13-1 (L11) — **gate hạ tầng** | `[ ]` | | |
| `MF13-TC-INT-002` ★ | `web/ComplianceReportIT.java` | **CRITICAL** | BR-182 + BR-176 | `[ ]` | | |
| `MF13-TC-INT-003` | `web/DateFormatContractIT.java` | HIGH | **D4** + BR-182 (L7) | `[ ]` | | |
| `MF13-TC-INT-004` | `web/ReportExportEmptyPeriodIT.java` | MEDIUM | BR-178 (L2) | `[ ]` | | |
| `MF13-TC-INT-005` | `web/DashboardFreshnessIT.java` | HIGH | BR-180 / ADR-M13-6 (L9) | `[ ]` | | |
| `MF13-TC-INT-006` | `web/ReportExportIT.java` | HIGH | BR-178 + **D2/D7** / §10 | `[ ]` | | |
| `MF13-TC-INT-007` | `web/ReportDownloadIntegrityIT.java` | HIGH | BR-177 + BR-182 | `[ ]` | | |
| `MF13-TC-INT-008` | `web/ReportScheduleCrudIT.java` | HIGH | **D5** + BR-179 | `[ ]` | | |
| `MF13-TC-INT-009` | `scheduler/ReportScheduleRunnerIT.java` | **CRITICAL** | BR-179 / ADR-M13-7 · ~~⛔ L3~~ → ✅ `M13-D08` | `[ ]` | | |
| `MF13-TC-INT-010` | `web/PagedResponseContractIT.java` | HIGH | Team Plan §2b-1/2/3/6 | `[ ]` | | |
| `MF13-TC-INT-011` | `scheduler/ReportExportPurgeIT.java` | HIGH | BR-177 / ADR-M13-3 | `[ ]` | | |
| `MF13-TC-INT-012` ⛔ | `web/ReportCatalogIT.java` | MEDIUM | UC-171 / BR-175 · ⛔ **L5** | `[ ]` | | |
| `MF13-TC-SEC-001` ★⛔ | `security/AuthorizationMatrixIT.java` | **CRITICAL** | §16 / BR-175 / MSG-004+005 · ⛔ **L5** | `[ ]` | | |
| `MF13-TC-SEC-002` ★ | `security/JwtScopeAuthorityIT.java` | **CRITICAL** | BR-176 / §17.1 C2 (L8) | `[ ]` | | |
| `MF13-TC-SEC-003` | `security/CrossSchoolIsolationIT.java` | **CRITICAL** | BR-176 · ~~⛔ L4~~ → ✅ `M13-D04` (**404**, áp lại MF02 31/07) | `[ ]` | | |
| `MF13-TC-SEC-004` ★ | `security/PiiLeakJsonIT.java` | **CRITICAL** | BR-181 / ADR-M13-4 | `[ ]` | | |
| `MF13-TC-SEC-005` ★ | `security/PiiLeakFileIT.java` | **CRITICAL** | BR-181 + BR-178 | `[ ]` | | |
| `MF13-TC-SEC-006` ★ | `security/ReportDownloadAuthzIT.java` | **CRITICAL** | BR-176 / §4.3 File access | `[ ]` | | |
| `MF13-TC-SEC-007` | `security/MultiSchoolScopeIT.java` | HIGH | BR-176 / §16 | `[ ]` | | |
| `MF13-TC-SEC-008` ★ | `security/ScheduledEmailEgressIT.java` | **CRITICAL** | BR-179 + BR-181 / §4.3 | `[ ]` | | |
| `MF13-TC-SEC-009` | `security/SearchInjectionIT.java` | HIGH | Team Plan §2b-3/§2b-7 | `[ ]` | | |
| `MF13-TC-CON-001` | `contract/OpenApiIdAndErrorContractIT.java` | HIGH | **D8 S3/S6** + D1/D2 | `[ ]` | | |
| `MF13-TC-CON-002` | `contract/OpenApiDateFormatContractIT.java` | HIGH | **D8 S4** + D4 | `[ ]` | | |
| `MF13-TC-CON-003` | `contract/OpenApiEnumAndPagingContractIT.java` | MEDIUM | **D8 S5/S7** + D6 / ADR-M13-5 | `[ ]` | | |
| `MF13-TC-CON-004` | `contract/OpenApiTraceabilityContractIT.java` | HIGH | **D8 S1/S2/S8** · ~~⛔ L1~~ → ✅ `M13-D03`, nay là regression guard | `[ ]` | | |
| `MF13-TC-CON-005` 🆕 | `contract/ReportRowsRoleShapeContractIT.java` | HIGH | **D11** + BR-181/176 · S7 · `M13-D12` | `[ ]` | | |
| `MF13-TC-INT-013` 🆕 | `web/ReportScheduleWriteContractIT.java` | HIGH | **D12** + TDS §9.2 · `M13-D12` | `[ ]` | | |
| `MF13-TC-INT-014` 🆕 | `repository/ReportExportImmutabilityIT.java` | MEDIUM | BR-182 · ADR-M13-3 · `M13-D02` | `[ ]` | | |

**Thống kê (bản v1.1, 2026-08-04):** **40 test case** (12 Unit · **14** Integration · 9 Security · **5** Contract) · **16 CRITICAL** · **17** HIGH · **7** MEDIUM · **~~7 blocked~~ → 4 blocked (⛔)**, tất cả bởi **L5** *(3 ô Service Caterers chờ Huy bổ sung Actor §1.13)* — **L1/L3/L4 đã đóng** (`M13-D03`/`M13-D08`/`M13-D04`) · **10 đánh dấu ★** bắt buộc pass trước final approve.

**Vì sao 10 test là ★** (nhiều hơn mức thường thấy — MF13 là module xuất dữ liệu ra ngoài):
- `INT-001` — **gate hạ tầng**: 7 view sai tên cột thì 11 IT phía sau vô nghĩa (L11).
- `TC-002`, `INT-002` — BR-182: chặn việc copy hằng số của mock; nếu trượt thì **toàn bộ báo cáo là số bịa**.
- `TC-004`, `SEC-004`, `SEC-005`, `SEC-008` — BR-181: 4 cửa ra của PII (projection · JSON · file · email). Rò ở đây là sự cố PDPD phải báo DPO (TDS §12.1/§12.3), **không thu hồi được** với file và email.
- `SEC-002`, `SEC-006` — BR-176: rò dữ liệu cross-school qua **query param** và qua **mã bản xuất đoán được** (IDOR).

### 5.1 Red Gate Protocol (CASE 2.0 — GATE-2)

> ⭐ **Gate quan trọng nhất.** Trước khi implement, chạy toàn bộ suite với stub **ném exception**. **Mọi** test PHẢI FAIL. Nếu test PASS ngay → **AP-AI-002 (Green-from-Birth)** → reject và rewrite.

**Stub cho Red Phase (Java — thay cho ví dụ TypeScript của template):**

```java
// src/main/java/com/sfms/mf13/service/impl/ReportRedStubs.java
// Red Phase — stub PHẢI throw. Xóa sau khi GATE-2 pass.
@Service @Profile("red-gate")
class AnalyticsQueryRedStub implements AnalyticsQueryService {
  private static final String MSG = "Not implemented — Red Phase stub";
  @Override public ComplianceSummaryResponse complianceSummary(LocalDate f, LocalDate t, boolean all) {
    throw new UnsupportedOperationException(MSG);
  }
  @Override public Page<ReportRowResponse> rows(ReportSlug s, LocalDate f, LocalDate t, Pageable p) {
    throw new UnsupportedOperationException(MSG);
  }
}

@Service @Profile("red-gate")
class ReportExportRedStub implements ReportExportService {
  private static final String MSG = "Not implemented — Red Phase stub";
  @Override public ReportExportResponse export(ReportSlug s, ReportFormat f, LocalDate a, LocalDate b, ExportFilters x) { throw new UnsupportedOperationException(MSG); }
  @Override public Page<ReportExportResponse> history(ReportSlug s, Pageable p) { throw new UnsupportedOperationException(MSG); }
  @Override public Optional<ReportExportResponse> findByCode(String c) { throw new UnsupportedOperationException(MSG); }
  @Override public DownloadedFile download(String c) { throw new UnsupportedOperationException(MSG); }
}
// + ReportScheduleRedStub, ReportCatalogRedStub, ReportRowProjectionRedStub (trả rỗng KHÔNG được — phải throw)
```

**Chạy Red Gate:**

```bash
./mvnw test -Dspring.profiles.active=test,red-gate -Dsurefire.failIfNoSpecifiedTests=false \
  | tee target/red-gate-evidence.log
# Kỳ vọng: 37 test, 37 FAIL (0 pass, 0 skip)
```

**Red Gate Verification:**

| Nhóm TC | Stub Result | Expected | Actual | Root Cause (nếu PASS bất thường) |
|---|---|---|---|---|
| `MF13-TC-001` … `-012` (Unit, 12) | `throw UnsupportedOperationException` | 🔴 FAIL | ☐ FAIL ☐ PASS | ☐ Tautology ☐ Shared state ☐ Hallucinated contract |
| `MF13-TC-INT-001` … `-012` (Integration, 12) | controller 501 / service throw | 🔴 FAIL | ☐ FAIL ☐ PASS | ☐ Assert quá lỏng (chỉ `status < 500`) ☐ Không assert DB |
| `MF13-TC-SEC-001` … `-009` (Security, 9) | **⚠️ chú ý đặc biệt** — xem 2 bẫy dưới | 🔴 FAIL | ☐ FAIL ☐ PASS | ☐ **False-green vì stub cũng 403/401** ☐ **False-green vì response rỗng nên không có PII** |
| `MF13-TC-CON-001` … `-004` (Contract, 4) | spec **vẫn sinh được** dù service throw | 🔴 FAIL | ☐ FAIL ☐ PASS | ☐ **False-green: annotation chưa viết nên "không có UUID" là đúng vô nghĩa** |

> ⚠️ **Bẫy Red Gate #1 (chung với MF02):** test kỳ vọng **403/401** dễ PASS ngay vì Spring Security chưa cấu hình có thể chặn **mọi** request. **Cách chống:** mỗi test `SEC-*` có **≥1 assertion dương** đã ghi rõ trong từng TC (`SEC-001` ô ✅ dòng a/e/f · `SEC-002` dòng a = 14 bản ghi · `SEC-003` bước 3 `total=3` · `SEC-004` slug `system-usage` có `name` · `SEC-005` file có `SMP-20260706-01` · `SEC-006` bước 5 tải được file của chính mình · `SEC-007` system-admin `total=26` · `SEC-008` file đính kèm có dữ liệu thật · `SEC-009` `search=Lưu mẫu` ≥1 kết quả).
>
> ⚠️ **Bẫy Red Gate #2 — RIÊNG của MF13, không có ở 12 module khác:** hai nhóm test dạng **"không được có X"** sẽ PASS một cách **vô nghĩa** khi chưa implement:
> - **PII (`SEC-004`/`-005`/`-008`):** chưa có gì để trả ⇒ response/file rỗng ⇒ "0 needle PII" là **đúng nhưng vô nghĩa**.
> - **Contract (`CON-001`…`-004`):** chưa viết annotation ⇒ spec không có `format: uuid`, không có `example` UUID ⇒ assertion âm PASS.
>
> **Cách chống (bắt buộc):** mỗi test hai nhóm này **phải** có assertion dương ĐI KÈM trong **cùng test method** — `SEC-004/-005/-008` assert dữ liệu nghiệp vụ **có mặt** (48 dòng, mã mẫu thật, nhãn `Confidential`); `CON-001`…`-004` assert **sự tồn tại** của path/tag/`ErrorResponse`/`PagedResponse` **trước khi** assert phần "không có". Với stub, các assertion dương FAIL ⇒ test FAIL đúng như Red Gate đòi.

**Red Gate Evidence:**

- Stub commit hash: `___`
- Tất cả FAIL? ☐ Yes → **GATE-2 PASS** (T2→T3) → tiếp tục implement
- Log file: `target/red-gate-evidence.log`
- Số test chạy: `___` / **37** (khớp §5) · Số FAIL: `___` · Số PASS bất thường: `___` (phải = **0**)

> **Nếu bất kỳ test PASS:** dừng lại, xác định root cause từ bảng trên, rewrite test từ TC spec + bổ sung assertion dương.

---

## 6. Entry / Exit Criteria

### Entry Criteria (điều kiện bắt đầu viết test)

- [ ] `TDS-MF13_Backend.md` (`SFMS-MF13-IMP-001`) chuyển **Approved** — Huy (§2), Mạnh (§5+§9), Minh (§8+§9 shape), Thảo (§13), Nam (final)
- [ ] `00_Common_Conventions.md` **D1–D7 Approved** ✅ (đã đạt 2026-07-30) · **D8** ⏳ — 4 test `CON-*` viết theo D8; nếu Nam **không** approve D8 thì nhóm này chuyển "khuyến nghị", không chặn Exit
- [x] ~~**⛔ L1 đã chốt**~~ → ✅ **đóng 2026-08-04 (`M13-D03`)**: `sent_reports` thuộc **MF12** — không phải "chốt" mà là **đã merge** (`V23:115` + `mf12/web/SentReportController`). §5.2/§9 của TDS-MF13 **không phải sửa gì**; `CON-004` giữ nguyên, chuyển vai trò thành regression guard
- [x] **Logic Issues §2 đã confirm** — **L1/L3/L4/L9/L11 đóng 2026-08-04** (`M13-D03`/`M13-D08`/`M13-D04`/`M13-D09`/`M13-D05`); còn **L5** (4 test ⛔, chờ Huy bổ sung Actor §1.13) — **không chặn Bước 2**, chỉ chặn việc siết 3 ô ma trận
- [ ] Nền chung của Nam sẵn sàng: `PagedResponse<T>`, `ErrorResponse` (D2), `OpenApiConfig` + `@SecurityScheme` bearer (D8/S8), JWT filter phát `AuthUser{userId, role, schoolId}` (MF01)
- [ ] **Module nguồn đã migrate** (MF01/02/04/06/07/08): `inspections`, `food_samples`, `receiving_sessions`, `partners`, `kitchen_inspections`, `inventory_batches`, `user_accounts`, `schools`, `school_config` — **và tên cột đã được Mạnh xác nhận** (L11)
- [ ] Flyway `V1__init_schema.sql` chứa 3 bảng MF13 + **toàn bộ** CHECK/trigger ở §5.2 (`ck_rpx_retention`, `ck_rpx_period`, `ck_rpx_format`, `ck_rpx_slug`, `ck_rsc_weekly/monthly`, `ck_rsr_attempt`, `uq_rpx_school_code`, `uq_rsc_school_code`, `uq_rsr_schedule_slot_attempt`, trigger `report_exports_no_update`)
- [ ] `V{n}__mf13_report_views.sql` (7 view) + `V{n+1}__seed_mf13.sql` (`FX-030`, `FX-031`) — **không** seed `report_exports` (L10)
- [ ] Seed nguồn `FX-010`…`FX-018` **đã chuẩn hóa D1–D7**: ngày → ISO (**D4**), enum → SCREAMING_SNAKE (**D6**); riêng `FX-017` (fixture "mìn" PII) và `FX-018` (dữ liệu trường B) **không có trong mock**, phải tổng hợp
- [ ] Docker khả dụng cho Testcontainers (`postgres:16-alpine`); `greenmail-junit5`, `pdfbox`, `poi-ooxml` đã trong `pom.xml` (test scope)

### Exit Criteria (điều kiện kết thúc — DoD)

- [ ] `./mvnw test` — **toàn bộ unit test xanh**, không `@Disabled`, không skip
- [ ] `./mvnw verify -Pintegration` — **toàn bộ integration + security + contract test xanh**
- [ ] **30/37** test case 🟢 PASS (37 trừ 7 test ⛔); mỗi test ⛔ ghi rõ Logic Issue đang chặn + người chịu trách nhiệm
- [ ] **★ 10 test ★ PASS** — đặc biệt:
  - `INT-001` (7 view đúng cột) · `INT-002` + `TC-002` (số liệu tính từ nguồn, BR-182)
  - `TC-004` + `SEC-004` + `SEC-005` + `SEC-008` (**4 cửa ra PII đều sạch**, BR-181)
  - `SEC-002` + `SEC-006` (không rò cross-school qua param và qua mã bản xuất)
- [ ] **`MF13-TC-SEC-001` chạy đủ 63 tổ hợp** (7 role × 9 nhóm endpoint), không bỏ sót ô nào của §16
- [ ] JaCoCo coverage **≥ 80% lines** cho `com.sfms.mf13`; riêng `service/` + `render/` **≥ 90%** (nơi chứa BR-178/181/182)
- [ ] **Truy vết ngược đủ:** 3/3 UC của §1.13 có ≥1 test case · 8/8 BR có ≥1 test case (bảng §3 TDS-03)
- [ ] **Kiểm D1–D7 bằng grep** (checklist Team Plan §2c-3):
  ```bash
  grep -rn "JsonFormat" src/main/java/com/sfms/mf13/                      # Expected: rỗng (D4)
  grep -rnE "[0-9]{2}/[0-9]{2}/[0-9]{4}" src/main/java/com/sfms/mf13/     # Expected: rỗng (D4)
  grep -rn "allowableValues" src/main/java/com/sfms/mf13/                 # Expected: rỗng (D8/S5)
  grep -rn "enum MealType\|enum RoleKey\|enum AllergySeverity\|enum PartnerType" src/main/java/com/sfms/mf13/
                                                                          # Expected: rỗng (D6 — không khai lại)
  grep -rnE "rate\s*=\s*100|alerts\s*=\s*0" src/main/java/com/sfms/mf13/  # Expected: rỗng (BR-182 — L6)
  grep -rn "sent_reports\|SentReport" src/main/java/com/sfms/mf13/        # Expected: rỗng (L1 / ADR-M13-2)
  ```
- [ ] **Bất biến DB xanh** (TDS §14.1 — chạy sau khi toàn bộ test kết thúc):
  ```sql
  SELECT code FROM report_exports WHERE expires_at < generated_at + INTERVAL '12 months';   -- 0 rows (BR-177)
  SELECT schedule_id, scheduled_for FROM report_schedule_runs GROUP BY 1,2 HAVING MAX(attempt) > 3;  -- 0 rows (BR-179)
  SELECT DISTINCT format FROM report_exports;                                               -- chỉ PDF, XLSX (BR-178)
  SELECT table_name, column_name FROM information_schema.columns
   WHERE table_name LIKE 'v_rpt_%' AND column_name ~* 'student|parent|phone|address|diagnos'; -- 0 rows (BR-181)
  UPDATE report_exports SET row_count = 0 WHERE code = 'RPX-2026-001';  -- Expected: ERROR append-only (BR-182)
  ```
- [ ] **Không** `Object`/`Map<String,Object>` thay cho DTO có kiểu trong production code MF13 *(ngoại lệ duy nhất: `filters JSONB` — có schema khai ở §9.2)*
- [ ] Response API **không** rò UUID nội bộ, `fileUri`, `checksumSha256` ra ngoài (assert ở `SEC-006`, `SEC-009`, `CON-001`)
- [ ] **`/v3/api-docs` đã tải và lưu kèm tài liệu nộp** (D8 — TDS §11.4): `docs/openapi-mf13-<ngày>.json`
- [ ] **DPO sign-off** — bắt buộc với MF13 (module duy nhất xuất dữ liệu ra file + email): DPO xem kết quả `SEC-004`/`-005`/`-008` trước khi ký
- [ ] Thảo (Lead Test/QA) xác nhận coverage happy + error path đủ cho **cả 4 lớp**; Mạnh xác nhận checklist §2b-7 + 7 view; Minh xác nhận shape `PagedResponse` khớp `usePagedList`; Nam final approve → TestSpec chuyển **Approved**

**Exit Criteria bổ sung — CASE 2.0:**

- [ ] **Red Gate (§5.1)** — 37/37 test FAIL với stub, `target/red-gate-evidence.log` được commit; **đặc biệt** xác nhận 2 bẫy false-green riêng của MF13 (PII rỗng, contract chưa annotate) đã bị chặn bằng assertion dương
- [ ] **Contract Existence** — mọi import trong test file resolve được:
  ```bash
  ./mvnw -q test-compile   # Expected: BUILD SUCCESS, không "cannot find symbol"
  ```
- [ ] **Props Isolation** — không shared mutable state:
  ```bash
  grep -rnE "^\s*(private|protected)?\s*static\s+.*(ReportExport|ReportSchedule|ReportScheduleRun)\s+\w+\s*=" src/test/java/com/sfms/mf13/
  # Expected: rỗng — mọi instance tạo trong @Test qua Mf13Fixtures.*()
  ```
- [ ] **Oracle Source** — mọi expected value truy được về BR/UC/MSG/D1–D8; **0** test case có expected "tự nghĩ ra"; mọi chỗ thiếu oracle đã đánh ⛔ ở §2 (hiện là **4**: L1, L3, L4, L5)
- [ ] **Không mượn MSG sai module** — grep TestSpec + test code:
  ```bash
  grep -rn "MSG-09[1-9]\|MSG-098\|MSG-099\|BR-209\|BR-213\|BR-214" src/test/java/com/sfms/mf13/
  # Expected: rỗng (CF-M13-4 / AP-AI-016 — các mã đó thuộc §1.12)
  ```
- [ ] **Assertion dương** — mỗi test `SEC-*` và `CON-*` có ≥1 assertion dương (§5.1 bẫy #1 và #2)

### Suspension Criteria (điều kiện tạm dừng)

- **L11 / `INT-001` FAIL** (7 view sai tên cột) → **dừng toàn bộ 11 IT còn lại + 9 SEC**: chúng chạy trên view, kết quả không có giá trị. Đây là suspension khả năng cao nhất của MF13 vì phụ thuộc 5 chủ module khác.
- **L1** (`sent_reports` thuộc module nào) chưa chốt → **không implement**; test vẫn viết được (không TC nào chạm `sent_reports`), nhưng `CON-004` có thể phải viết lại.
- **L4** (403 vs 404 cross-school) chưa chốt → `SEC-003` chỉ chạy phần "không rò dữ liệu" + assertion dương.
- **L5** (Service Caterers có là Actor không) chưa chốt → `TC-006`, `TC-012`, `INT-012`, `SEC-001` có thể phải sửa 3 ô ma trận; không dừng nhưng **không** siết assertion.
- **L3** (MSG cho thông báo BR-179) chưa cấp → `TC-008`, `INT-009` treo nhánh nội dung thông báo.
- Nền chung của Nam (`PagedResponse`, `ErrorResponse`, `OpenApiConfig`, JWT filter) chưa xong → toàn bộ `INT-*`/`SEC-*`/`CON-*` không chạy được.
- Module nguồn chưa có dữ liệu seed đủ (`FX-010`…`FX-016`) → `TC-002`/`INT-002` không có oracle số học; **không** được hạ expected xuống cho khớp dữ liệu thiếu.
- Docker/Testcontainers không khả dụng trên CI → 25 test `INT-*`/`SEC-*`/`CON-*` bị chặn hạ tầng.
- Phát hiện quyết định kiến trúc mới cần Nam review (VD phải tách `ADR-MF13_*.md` riêng, hoặc chuyển sang materialized view cho BR-180).

---

## 7. Rollback Plan

```bash
# ── Hoàn tác migration (CHỈ môi trường dev/test — KHÔNG chạy trên production) ──
./mvnw -pl app flyway:clean          # xóa toàn bộ schema test
./mvnw -pl app flyway:migrate        # dựng lại V1 + view + seed từ đầu
# Nếu chỉ lỗi ở V{n} view MF13: sửa view rồi migrate lại (view là tài sản của MF13 — ADR-M13-1)
# Nếu lỗi seed: khôi phục theo TDS §12.2  →  psql -h [host] -U sfms sfms < backup_20260730.sql

# ── Chặn ngay bề mặt nguy hiểm nếu phát hiện rò dữ liệu (TDS §12.2) ──
kubectl set env deployment/sfms-api REPORT_EXPORT_ENABLED=false REPORT_SCHEDULE_ENABLED=false

# ── Testcontainers + GreenMail: không cần rollback (ephemeral) ──
docker ps -a --filter "label=org.testcontainers" -q | xargs -r docker rm -f

# ── Dọn file test sinh ra (bản xuất PDF/XLSX trong @TempDir + ./data/uploads dev) ──
rm -rf target/test-exports ./data/uploads/RPX-*

# ── Hoàn tác code test ──
git checkout -- src/test/java/com/sfms/mf13/
git checkout -- 07_implement/MF13_Reporting_Analytics/TestSpec-MF13_Reporting_Analytics.md

# ── Xóa stub Red Phase sau khi GATE-2 pass ──
git rm src/main/java/com/sfms/mf13/service/impl/*RedStub*.java
```

> ⚠️ **KHÔNG** `DELETE FROM report_exports` để "dọn dữ liệu test" trên môi trường đã deploy: BR-177 đòi giữ ≥12 tháng và trigger chỉ chặn `UPDATE`, không chặn `DELETE`. Trên dev/test dùng `flyway:clean`; trên môi trường thật, bản xuất lỗi được **thay bằng bản mới** (append-only), không xoá bản cũ.

**Nếu phát hiện vi phạm trên môi trường đã deploy** (theo TDS §12.1):

| Triệu chứng | Kiểm tra | Hành động |
|---|---|---|
| File xuất chứa PII học sinh | `SEC-005` dòng quét metadata + cell | **Sự cố dữ liệu cá nhân** — tắt `REPORT_EXPORT_ENABLED`, báo **Nam + DPO** (TDS §12.3), liệt kê `RPX-…` đã tải (truy được qua event `ReportDownloaded`), **không chỉ** rollback |
| Email báo cáo gửi ra địa chỉ ngoài danh sách | `SEC-008` dòng `getAllRecipients()` | Tắt `REPORT_SCHEDULE_ENABLED`; đối chiếu `report_schedule_runs.recipient_count`; báo DPO |
| Dữ liệu trường khác xuất hiện trong báo cáo | `SEC-002`/`SEC-003`/`SEC-007` | Chặn endpoint, báo Nam + DPO; kiểm `school_id` có bị đọc từ query ở đâu không |
| Tải được `RPX-…` của trường khác | `SEC-006` bước 2–3 | **IDOR** — chặn `/download`, hotfix `findByCodeAndSchoolId`, rà log tải |
| Số liệu dashboard không đổi khi dữ liệu nguồn đổi | `INT-005` | Cache không evict / TTL quá dài → vi phạm BR-180; hạ TTL, kiểm event listener |
| `report_exports` bị `UPDATE` thành công | Exit Criteria dòng SQL cuối | Trigger bị mất khi migrate lại → dựng lại trigger, rà bản ghi bị sửa (so `checksum_sha256` với file) |

> **Gap vẫn OPEN** → giữ nguyên entry trong §2 Logic Issues Resolved; **không** xóa dòng ⛔ khi chưa có quyết định (Policy 4.4 — Immutable History).

---

## 8. CASE 2.0 Anti-Pattern Detection

> Checklist cho reviewer (Thảo — Lead Test/QA) khi test case được AI hỗ trợ generate.

| AP-ID | Anti-Pattern | Dấu hiệu trong TestSpec này | Check | Gate chặn |
|---|---|---|---|---|
| AP-AI-001 | Unconstrained Generation | TC không reference BR/UC/MSG/D1–D8 nào ở dòng **Oracle Source** | ☐ | G-0 |
| AP-AI-002 | **Green-from-Birth** | Test PASS với stub ném exception (§5.1) — **hai bẫy riêng của MF13**: (a) `SEC-004/-005/-008` xanh vì response/file **rỗng** nên không có PII; (b) `CON-*` xanh vì **chưa viết annotation** nên "không có UUID" đúng vô nghĩa | ☐ | **G-2 ★** |
| AP-AI-003 | Implicit Decision | Test assume quyết định không có trong TDS §3 / D1–D8 — VD tự chọn 404 cho cross-school khi **L4** chưa chốt, hoặc tự cấp quyền cho Service Caterers khi **L5** chưa chốt | ☐ | G-1 |
| AP-AI-004 | Layer Violation | Unit test service lại đi qua HTTP; hoặc contract test đi kiểm business logic | ☐ | G-4 |
| AP-AI-005 | Hallucinated Contract | Test import service/DTO không có trong TDS §8 (VD `SentReportService` — **không tồn tại** ở MF13) | ☐ | G-3 |
| AP-AI-006 | **Trust FE identity** | Test chấp nhận `schoolId`/`school` từ query/body là hợp lệ → xem `MF13-TC-SEC-002` | ☐ | **G-1 ★** |
| AP-AI-007 | Wrong stack | Xuất hiện `npm test`, `.spec.ts`, `prisma`, `npx` trong TestSpec/test code | ☐ | G-0 |
| AP-AI-011 | **Fabricated oracle** | Expected message/mã lỗi không có trong `error_message.md` v1.5 mà **không** đánh ⛔ — VD tự đặt `RPT-179` cho thông báo BR-179 (xem L3) | ☐ | G-0 |
| AP-AI-012 | **Assert quá lỏng** | Chỉ assert HTTP status mà **không** assert `error.code` + message nguyên văn + **DB state** / **nội dung file** | ☐ | G-2 |
| AP-AI-016 | **Mượn MSG sai module** *(riêng MF13)* | Dùng MSG-092/096/097 cho "kỳ rỗng" của MF13, hoặc trích BR-209/213/214 — thuộc **§1.12** (CF-M13-4, L2) | ☐ | **G-0 ★** |
| AP-AI-017 | **Hardcoded metric** *(riêng MF13)* | Expected của `TC-002`/`INT-002` được **sửa cho khớp implement** thay vì tính từ fixture; hoặc test chấp nhận `rate=100` cho `sample-retention` (copy mock — L6) | ☐ | **G-2 ★** |
| AP-AI-018 | **Cross-module entity import** *(riêng MF13)* | Test/production code `import com.sfms.mf07.domain.InspectionSession` thay vì đọc qua view/`ReportDataPort` (ADR-M13-1) | ☐ | G-1 |
| AP-AI-019 | **PII test xanh vì rỗng** *(riêng MF13)* | `SEC-004/-005/-008` **không** có assertion dương kèm theo → xem bẫy #2 §5.1 | ☐ | **G-2 ★** |

**Kết quả review:**

- [ ] Không phát hiện anti-pattern nào → TestSpec **approved**
- [ ] Phát hiện AP → ghi vào bảng dưới → fix **trước** khi implement

| AP detected | TC ID | Mô tả | Fix action | Fixed? |
|---|---|---|---|---|
| `AP-AI-___` | `MF13-TC-___` | | | ☐ |

**Tự kiểm khi soạn tài liệu này (Nam):**

| Kiểm | Kết quả |
|---|---|
| Mọi TC có dòng **Oracle Source** trích BR/UC/MSG/D-code cụ thể? | ✅ 37/37 |
| Có TC nào dùng expected "tự nghĩ ra" không đánh dấu? | ✅ Không — **4** chỗ thiếu oracle/mâu thuẫn đều đánh ⛔ kèm người chịu trách nhiệm (L1, L3, L4, L5) |
| Mọi expected message là **tiếng Anh nguyên văn** `error_message.md` v1.5 (D7)? | ✅ — đã đối chiếu từng dòng **MSG-004, MSG-005, MSG-137, MSG-138** (4 mã duy nhất MF13 dùng) |
| Có mượn MSG của module khác? | ✅ Không — MSG-091/092/096/097/098/099 **bị loại tường minh** (CF-M13-4, L2), và có grep chặn ở Exit Criteria |
| Đủ **4 lớp** theo prompt §2c-3b (Unit · Integration · Security · Contract)? | ✅ 12 · 12 · 9 · 4 |
| Có test "gọi API trực tiếp bỏ qua UI"? | ✅ `SEC-002` (param tampering), `SEC-003` (cross-school), `SEC-006` (IDOR mã bản xuất), `SEC-007` (`allSchools`), `SEC-009` (search/sort ngoài whitelist) |
| Tên luật `semantic-audit.ts` copy nguyên văn từ script? | ✅ **0 luật MF13** — khai bằng grep ở Phụ lục C, không trích tên từ TDS |
| Định dạng expected theo D1–D7 (không theo mock)? | ✅ 3 điểm lệch mock được test **bắt** chứ không copy: L6 (hằng số), L7 (`dd/MM/yyyy`), L8 (lọc theo tên trường) |

---

## PHỤ LỤC A — Danh sách UC / BR / MSG đã trích dẫn

**Use Cases (3/3) — nguồn `traceability_matrix.md` §1.13 + UC file `MF13_Reporting_Analytics.md`:**

| UC | Tên (nguyên văn) | Actor (§1.13) | Test cases |
|---|---|---|---|
| **UC-171** | View Analytics Dashboard | School Administration / Medical Staff / System Admin | 19 TC |
| **UC-172** | Export Report | School Administration / System Admin | 15 TC |
| **UC-173** | Schedule Automated Report | School Administration / System Admin | 7 TC *(⚠️ UC còn ở dạng **stub** — L2/CF-M13-2)* |

*(**Không** dùng UC-150, UC-185, UC-187, UC-188, UC-189, UC-190 — traceability xếp ở **§1.12 / MF12**; xem L1.)*

**Business Rules (8/8) — nguồn `business_rules.md` §16:**

| BR | Nội dung rút gọn | Test cases |
|---|---|---|
| **BR-175** | RBAC theo loại báo cáo (4 nhóm role) | `TC-006`, `TC-012`, `INT-012`, `SEC-001` |
| **BR-176** | Phạm vi trường; chỉ System Admin đa trường | `INT-002`, `SEC-002`, `SEC-003`, `SEC-006`, `SEC-007` |
| **BR-177** | Lưu ≥12 tháng + audit mỗi lần xuất | `TC-005`, `INT-007`, `INT-011` |
| **BR-178** | PDF + XLSX; 4 field header | `TC-001`, `TC-003`, `INT-004`, `INT-006` |
| **BR-179** | Daily/Weekly/Monthly; email theo cấu hình; retry ≤3 rồi báo System Admin | `TC-007`, `TC-008`, `TC-009`, `INT-008`, `INT-009`, `SEC-008` |
| **BR-180** | Làm mới ≤5 phút | `INT-005` |
| **BR-181** | Không PII học sinh; nhãn Confidential | `TC-004`, `SEC-004`, `SEC-005`, `SEC-008` |
| **BR-182** | Tính từ nguồn gốc; không sửa sau khi tạo; hiện ngày giờ tạo + khoảng dữ liệu | `TC-002`, `TC-010`, `INT-002`, `INT-003`, `INT-007` |

*(**Không** dùng BR-209/BR-213/BR-214 — thuộc UC-185/188/189 ở §1.12. **BR-151** chỉ được nhắc như mô tả nội dung slug `traceability` ở màn FE, không có trong cột BR của §1.13. **BR-004** (phiên đăng nhập → MSG-004) và **BR-007** (RBAC 7 role) là rule nền chung, dùng ở §10/§16 như 12 TDS khác.)*

**System Messages — nguồn `error_message.md` v1.5 (chỉ 4 mã, đúng theo §1.13 + 2 mã dùng chung):**

| MSG | Nội dung nguyên văn (expected message) | Mã module | HTTP | Test cases |
|---|---|---|---|---|
| **MSG-004** | `Session expired. Please log in again.` | `RPT-004` | 401 | `SEC-001` |
| **MSG-005** | `Access denied: You do not have permission to access this resource.` | `RPT-005` | 403 | `TC-006`, `SEC-001`, `SEC-003`, `SEC-007` |
| **MSG-137** | `Resource not found.` | `RPT-404`, `RPT-410` | 404, 410 | `TC-011`, `INT-006`, `INT-008`, `INT-011`, `SEC-003`, `SEC-006` |
| **MSG-138** | `Validation failed: one or more required fields are missing or invalid.` | `RPT-138`, `RPT-409` | 422, 409 | `TC-001`, `TC-007`, `INT-006`, `INT-008` |

**Success MSG:** **không có** — §1.13 không gán MSG Success cho UC-171/172/173; thao tác trả 200/201 kèm payload.

**⛔ Hai chỗ THIẾU MSG (không tự cấp — CF-M13-4):**
1. ~~Thông báo "lịch báo cáo thất bại 3/3" (BR-179) → `TC-008`/`INT-009` để `__PENDING__`. **Đề nghị cấp MSG ở `error_message.md` v1.6.**~~ → ✅ **ĐÓNG 2026-08-04 (`M13-D08`) — KHÔNG cấp MSG.** `error_message.md` đánh mã cho **thông điệp trả người dùng qua API**; BR-179 thất bại 3/3 không có request nào đang chờ ⇒ là notification nền + `error_message` + audit. Cấp mã cho nó là mở rộng phạm vi file đó không có lý do. `__PENDING__` đã gỡ khỏi cả `TC-008` và `INT-009`.
2. "Kỳ dữ liệu rỗng khi xuất" → **cố ý không cần MSG**: BR-178 không đòi chặn, trả 201 `rowCount=0` (L2). Nếu Huy xác nhận phải chặn thì **phải cấp mã mới**, không mượn MSG-092/096/097.

---

## PHỤ LỤC B — Xung đột nguồn của TDS được kế thừa (CF-M13-*)

> TestSpec kế thừa 9 xung đột đã ghi ở `TDS-MF13_Backend.md` §2.3. Bảng dưới ánh xạ sang Logic Issue của §2 để reviewer đối chiếu nhanh.

| CF (TDS §2.3) | Logic Issue (§2) | Test case bị ảnh hưởng | Trạng thái |
|---|---|---|---|
| **CF-M13-1** — ai sở hữu `sent_reports` (3 nguồn nói khác nhau) | **L1** | `CON-004` (kiểm ranh giới) + chặn Entry | ⛔ Huy + Nam |
| **CF-M13-2** — UC-173 là stub nhưng BR-179 Approved | **L2** *(liên quan)* | `TC-007`, `TC-008`, `TC-009`, `INT-008`, `INT-009` viết đủ, đánh dấu chưa nghiệm thu qua FE | ✅ (test viết đủ) |
| **CF-M13-3** — Service Caterers có BR-175 nhưng không là Actor §1.13 | **L5** | `TC-006`, `TC-012`, `INT-012`, `SEC-001` (3 ô) | ⛔ Huy |
| **CF-M13-4** — 2 tình huống chưa có MSG | **L2** + **L3** | `INT-004` (kỳ rỗng), `TC-008`/`INT-009` (thông báo) | ⛔ phần thông báo |
| **CF-M13-5** — `data_model.puml` không có thực thể báo cáo | *(không tạo Logic Issue)* | không ảnh hưởng test; cần Mạnh/Huy sync puml | 📋 tài liệu |
| **CF-M13-6** — FE đọc thẳng resource module khác, lọc bằng tên trường cứng | **L8** | `SEC-002` | ✅ + cần Minh sửa FE |
| **CF-M13-7** — mock vi phạm BR-182 (hằng số) + D4 (`dd/MM/yyyy`) + thiếu `periodFrom/To` + danh mục hardcode | **L6**, **L7** | `TC-002`, `INT-002`, `INT-003`, `TC-012`, `INT-012` | ✅ test **bắt** lệch |
| **CF-M13-8** — BR-180 chỉ là `refetchInterval` phía client | **L9** | `INT-005` | ✅ + khuyến nghị TTL 240s |
| **CF-M13-9** — `school-admin/dashboard` tổng hợp phía client | *(ngoài phạm vi 3 UC)* | không có test; ghi để Minh xử lý | 📋 FE |
| *(mới ở TestSpec)* — 7 view phụ thuộc tên cột 5 module khác | **L11** | `INT-001` ★ (gate) + toàn bộ IT phía sau | ✅ có gate · Mạnh điều phối |
| *(mới ở TestSpec)* — không seed được `report_exports` bằng Flyway | **L10** | fixture `FX-040`/`FX-041` sinh qua API | ✅ |
| *(liên thông MF02)* — cross-school 403 vs 404 | **L4** | `SEC-003` | ⛔ Nam + Huy — **chốt 1 lần cho 13 module** (`TestSpec-MF02` L7 cùng vấn đề) |

---

## PHỤ LỤC C — Luật `semantic-audit.ts` đã port (khai báo bắt buộc)

> Prompt §2c-3b yêu cầu: *"port luật liên quan {MF} sang Gherkin… **Tên luật PHẢI copy nguyên văn từ script**, KHÔNG lấy từ TDS §13"*. Mục này khai báo kết quả rà soát.

### C.1. Kết quả rà soát: **0 luật thuộc MF13**

```bash
# Đếm luật đăng ký qua check()
grep -oE 'check\("[A-Z_0-9]+"\)' 03_Design/08_FrontEnd/Web/scripts/semantic-audit.ts | sort -u | wc -l
# → 54   (khớp con số "54 luật" ở Team Plan §2a)

# Tìm luật liên quan báo cáo / xuất bản / lịch gửi
grep -oE 'check\("[A-Z_0-9]+"\)' 03_Design/08_FrontEnd/Web/scripts/semantic-audit.ts | sort -u \
  | grep -iE 'BAO_CAO|XUAT|EXPORT|LICH|GUI_MAIL|THONG_KE|DASHBOARD'
# → chỉ ra 2 tên, cả hai KHÔNG thuộc MF13:
#   BAO_CAO_TRUOC_BUA_AN        → Incident.reportedAt < mealDate  (MF11 — báo cáo SỰ CỐ, không phải báo cáo thống kê)
#   BAO_CAO_NGUON_KHAC_BUA      → IncidentSourceReport ngoài khung 7 ngày (MF11)
```

**Kết luận:** 54 luật của script kiểm **tính toàn vẹn dữ liệu nghiệp vụ** (mẫu lưu, kiểm thực, lô hàng, phân suất, sự cố). Không luật nào kiểm **báo cáo tổng hợp / xuất file / lịch gửi**. Điều này hợp lý: MF13 **không ghi** dữ liệu nghiệp vụ nào, nên không có bất biến nghiệp vụ nào để vi phạm. Oracle của MF13 là **BR-175…182** (§3 TDS-02).

### C.2. Quan hệ gián tiếp — ghi chú liên module (không port thành test MF13)

Các luật sau bảo vệ **dữ liệu nguồn** mà MF13 đọc; nếu chúng bị vi phạm thì báo cáo của MF13 **phản ánh đúng dữ liệu sai** — và đó là hành vi **đúng** theo BR-182:

| Tên luật (nguyên văn từ script) | Bảng nguồn | Ảnh hưởng tới báo cáo MF13 |
|---|---|---|
| `HAN_LUU_KHONG_DU_24H` · `HUY_MAU_TRUOC_HAN_24H` | `food_samples` (MF08) | slug `sample-retention` — số `alerts` |
| `PHAT_SUAT_DU_KIEM_THUC_TRUOT` | `student_distributions` (MF09) | không nằm trong 7 slug hiện tại *(ghi nhận: nếu sau này thêm slug "phân suất" thì đây là oracle)* |
| `NHAN_HANG_SAU_KHI_CHUNG_NHAN_HET_HAN` · `MUA_HANG_SAU_KHI_GIAY_TO_HET_HAN` | `receiving_sessions`, `partners` (MF04/06) | slug `receiving`, `supplier-certificates` |
| `LO_KHONG_CO_PHIEU_NHAN` · `NHAP_LO_DA_HET_HAN` · `XUAT_LO_HET_HAN` | `inventory_batches` (MF06) | slug `traceability` — chuỗi lô ← NCC ← phiên nhận |

> ⚠️ **Không** viết test MF13 assert các luật trên **PASS** — đó là việc của TestSpec MF06/MF07/MF08/MF09. Test MF13 chỉ assert **con số phản ánh đúng những gì có trong bảng nguồn** (`TC-002`, `INT-002`).

### C.3. Ghi nhận về 3 luật chỉ có `add()` không có `check()`

Script có 3 luật `PHIEU_TRO_MAU_KHONG_TON_TAI`, `MOC_TRO_SU_CO_KHONG_TON_TAI`, `LO_TRO_MAT_HANG_KHONG_TON_TAI` chỉ gọi `add()` mà không `check()` — theo prompt §2c-3b, **cả 57 đều là luật thật**, con số "54" chỉ là số `check()` đăng ký. Ba luật này thuộc MF06/MF08/MF11 (khoá ngoại trỏ bản ghi không tồn tại), **không** thuộc MF13.

---

## PHỤ LỤC D — Bản đồ nguồn ↔ mục tài liệu (cho reviewer đối chiếu nhanh)

| Nguồn (đường dẫn) | Đã đọc phần nào | Điền vào mục nào của TestSpec |
|---|---|---|
| `00_Common_Conventions.md` | D1–D7 (Approved) + D8 S1–S8 | §1.1 Constraints · §3 TDS-02 · §4.4 (4 TC Contract) · §6 Exit (grep) |
| `TDS-MF13_Backend.md` | §2.1/§2.3 · §3 (ADR-M13-1…7) · §5.2 (DDL/CHECK/trigger/7 view) · §6.4/§6.5 (state machine) · §7.1 (5 event) · §8 (interface) · §9 (9 endpoint) · §10 (6 mã `RPT-*`) · §13 · §16 (63 ô) · §17.1 (C1–C9) | toàn bộ §2, §3, §4 |
| `business_rules.md` §16 | BR-175…182 nguyên văn | §3 TDS-02 · Oracle Source của **mọi** TC · Phụ lục A |
| `error_message.md` v1.5 | MSG-004, 005, 137, 138 (+ **loại tường minh** MSG-091/092/096/097/098/099) | §4 expected message · §8 AP-AI-016 · Phụ lục A |
| `traceability_matrix.md` §1.13 | 3 UC + Actor + cột BR/MSG | §1 · §3 TDS-03 (ma trận truy vết) · Phụ lục A |
| `traceability_matrix.md` §1.12 | UC-150, UC-185…190 — để **loại khỏi phạm vi** | §1 (KHÔNG thuộc phạm vi) · L1 · `CON-004` |
| `mock-server.ts` | `GET /compliance-report` (264–316), `paginate()` (27–92), `nowStamp()` (107) | fixture số học của `TC-002`/`INT-002` · L6/L7 · `INT-010` (§2b) |
| `mock-data.ts` | `SENT_REPORTS`/`SentReport` (2305–2414 — **để loại**), `SCHOOL_CONFIG.reportEmails` (159–169), `STUDENTS` | `FX-003`/`FX-004` · `FX-017` (fixture PII) · L1 |
| `school-admin/reports/page.tsx` | `refetchInterval` 5', `toast.info` cho nút Xuất, 2 dòng hardcode (119–122) | L9 · `INT-005` · `TC-012`/`INT-012` |
| `school-admin/reports/[slug]/page.tsx` | 7 slug + `REPORT_META`, `filters={{school:"TH Đoàn Thị Điểm"}}` (207), 2 badge Confidential | `TC-011` · L8/`SEC-002` · `SEC-004` |
| `semantic-audit.ts` | 54 tên luật `check()` — rà toàn bộ | Phụ lục C (**0 luật MF13**, kèm grep) |
| `00_Team_Work_Assignment_Plan.md` | §2b (phân trang/search) · §2c-0 (tên module cho `@Tag`) · §2c-3b (prompt) · §2c-4 (chọn model) · §3 (P2) · §4b (lịch) | §1 · §3 TDS-02 · `INT-010` · `CON-004` |
| `TestSpec-MF02_School_Class_Management.md` | L7 (403 vs 404), cấu trúc §3/§4/§5.1/Phụ lục C | **L4** (liên thông) · khuôn tài liệu này |
| `TDS-MF12_Backend.md` | §2.3 CF-3/CF-4/CF-23 · §5.2 `sent_reports` | **L1** (ranh giới module) · `CON-004` |
| `ADR-000.md` / `ADR-001.md` | stub SMTP (Mailpit), file `./data/uploads`, PDFBox *"khi có nhu cầu thật"*, Caffeine/Redis | stack test (GreenMail, `@TempDir`) · `INT-005` (cache) · `INT-009` (email) |

---

**HẾT** — `SFMS-MF13-TEST-001` v1.0 · 37 test case · 11 Logic Issue (4 ⛔) · chờ review chéo theo RACI §1 của `00_Team_Work_Assignment_Plan.md`.
