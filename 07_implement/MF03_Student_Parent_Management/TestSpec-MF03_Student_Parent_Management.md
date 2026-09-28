# TEST-DRIVEN DEVELOPMENT SPECIFICATION — TestSpec
# MF03 — Student & Parent Management

| Field | Value |
|-------|-------|
| **Document ID** | `SFMS-MF03-TEST-001` |
| **Version** | `1.1` |
| **Date** | `2026-08-02` |
| **Status** | `Approved` |
| **Standard** | ISO/IEC/IEEE 29119-3:2021 — Software Testing Part 3: Test Documentation |
| **Author** | Bùi Quang Huy — chủ module MF03/MF06, Lead Requirements & Traceability Reviewer |
| **Reviewed by** | Nguyễn Phương Thảo (Lead Test/QA) · Nguyễn Đức Mạnh (Backend/DB) · Trần Tuấn Minh (FE↔BE contract) — duyệt 2026-08-02 |
| **DPO Sign-off** | `[ ] Pending` — ⚠️ module chứa **PII trẻ em + dữ liệu sức khỏe**; toàn bộ test data phải **SYNTHETIC** (BR-173). Không chặn `Approved`; bắt buộc ký trước khi lên môi trường thật |
| **Approved by** | Nguyễn Văn Nam — Final Approval, 2026-08-02 |
| **Classification** | `Internal — Confidential` (test data: `Restricted`-shaped nhưng synthetic) |

**References (thứ tự ưu tiên khi xung đột — cao xuống thấp):**
1. `07_implement/00_Common_Conventions.md` — **D1–D7 Approved 2026-07-30**, **D8 — đã áp dụng trong code MF01/MF02, xem L16**
2. `07_implement/MF03_Student_Parent_Management/TDS-MF03_Backend.md` (`SFMS-MF03-IMP-001` v1.0) — §13 xương sống · §9 API · §10 mã lỗi · §16 Auth Matrix · §5.2 DDL
3. `02_Requirements/00_BusinessRules/business_rules.md` — nội dung BR làm **oracle**
4. `02_Requirements/00_BusinessRules/error_message.md` **v1.5** — nội dung MSG **nguyên văn** làm expected message
5. `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` **§1.3** — mọi TC truy được về ≥1 UC ID
6. `03_Design/08_FrontEnd/Web/scripts/semantic-audit.ts` — 4 luật liên quan MF03 (tên **copy nguyên văn từ script**)
7. `03_Design/08_FrontEnd/Web/src/lib/mock-data.ts` — **CHỈ** nguồn seed/fixture (không lấy định dạng ngày / giá trị enum)
8. `07_implement/00_Team_Work_Assignment_Plan.md` §2b (hợp đồng phân trang) · §2c-3b (prompt sinh tài liệu này)

> **Quy ước TDD:** viết test → chạy → xác nhận **FAIL 🔴** → implement → **PASS 🟢** → refactor 🔵.
> Không mark ✅ nếu `mvn verify` chưa xanh. Test data **SYNTHETIC** (seed từ `mock-data.ts`, đã chuyển đổi theo D4/D6) — **tuyệt đối không dùng PII học sinh thật** (BR-173).
> **Stack test:** JUnit 5 · Spring Boot Test 4.1.x (Java 25 LTS) · **Testcontainers PostgreSQL 16** · MockMvc/`@SpringBootTest` · AssertJ · Flyway chạy thật trong container (để CHECK/RULE/TRIGGER của §5.2 được kiểm). **KHÔNG** Jest/Prisma — template `PHASE-4_Test-Spec.md` dùng ví dụ TypeScript/Prisma, đã dịch sang Java/SQL theo ràng buộc stack §2c-2.

---

## CHANGELOG

> **Policy 4.4 — Immutable History:** Không bao giờ xóa thông tin cũ.

| Ngày | Người thực hiện | Nội dung thay đổi |
|------|-----------------|-------------------|
| 2026-08-02 | Bùi Quang Huy | **Chốt nốt Q1/Q3/Q5, chuyển sang `Approved` — 0 TC còn BLOCKED.** Q3 → **MSG-142** (tách khỏi MSG-010 theo D7, `MF03-TC-INT-002` thêm bước 3 assert message hai ca giống hệt nhau và không chứa chuỗi "50" — chốt chặn để không ai lặng lẽ quay về MSG-010). Q5 → **MSG-143** (`MF03-TC-INT-013` nay assert đủ cả message thay vì chỉ `code`). Q1 → **BR-220** + **MSG-144**: chọn phương án *nhận nhưng thẩm định* (`NEEDS_INFO`) thay vì chặn hay nhận bừa — vì BR-066 là danh mục MỞ nên không chống lưng việc chặn, còn nhận bừa thì bếp không nấu được (BR-065/BR-082). `MF03-TC-005` gỡ BLOCKED-ORACLE và **thêm bước 3** (duyệt lần hai kèm `reviewNote` → APPROVED) — thiếu bước này thì test không phân biệt được *cổng thẩm định* với *cổng từ chối*. `MF03-TC-CON-003` thêm kiểm MSG-114 không còn xuất hiện. Exit Criteria: `@Disabled` nay là **0**, gặp cái nào là bỏ sót chứ không phải BLOCKED. |
| 2026-08-02 | Bùi Quang Huy | **v1.1 — đồng bộ với backend đã build.** Bản v1.0 (30/07) viết trước khi MF01/MF02 merge vào `dev` (31/07), nên Entry Criteria và một số giả định đã lạc hậu. Thêm **L20–L23** (§2): L20 vi phạm **D9** (entity tự khai `created_at`/`created_by` ⇒ Hibernate duplicate column); L21 **CF-M03-13** khóa ngoại trong JSON phải là `code` chứ không phải UUID (**D1**) — chốt sửa backend, FE sync sau; L22 hai Port trong Entry Criteria **không tồn tại** (grep `interface .*Port` → 0) ⇒ bỏ `ClassroomCapacityPort` (dùng `ReferenceResolver` + COUNT), giữ yêu cầu tạo `ParentAccountPort`; L23 mô hình Flyway "V1 = toàn schema" đã bị bỏ ⇒ MF03 tự giữ `V4`/`V5`. **Gỡ 2 khóa:** `MF03-TC-INT-014` (Q2 chốt theo D1 + tiền lệ `classrooms`) và `MF03-TC-CON-001..005` (Q4 — D8 đã hiện diện trong code MF01/MF02). Đổi package `com.sfms.student` → **`com.sfms.mf03`**, đường dẫn test ghi đủ từ gốc repo (`04_Development/Backend/src/test/java/...`). **Còn mở:** Q1, Q3, Q5. |
| 2026-07-30 | Bùi Quang Huy | Khởi tạo. Mở rộng TDS-MF03 §13 (5 unit + 8 integration + 4 E2E) thành **37 test case** đủ 4 lớp Unit/Integration/Security/Contract. Ghi nhận **19 Logic Issue** (§2) — trong đó 6 lệch nội dung MSG so với `error_message.md` v1.5, 3 fixture sai so với seed, 1 **thiếu oracle nghiệp vụ** (L14 — BR-066 không phải danh mục đóng). Port 4 luật `semantic-audit.ts` với tên nguyên văn. |

---

## MỤC LỤC

1. [Thông tin Module](#1-thông-tin-module) · 2. [Logic Issues Resolved](#2-logic-issues-resolved) · 3. [Test Design Specification](#3-test-design-specification) · 4. [Test Case Specification](#4-test-case-specification) — [Unit](#41-unit-test-cases--service-layer) · [Integration](#42-integration-test-cases--endpoint--db) · [Security](#43-security-test-cases--rbac--phạm-vi-dữ-liệu--pdpd) · [Contract](#44-contract-test-cases--openapi-d8) · 5. [Red-Green-Refactor Tracker](#5-red-green-refactor-tracker) · 6. [Entry/Exit Criteria](#6-entry--exit-criteria) · 7. [Rollback Plan](#7-rollback-plan) · 8. [Anti-Pattern Detection](#8-case-20-anti-pattern-detection) · [Phụ lục: danh sách mã đã trích](#phụ-lục--danh-sách-mã-đã-trích-dẫn)

---

## 1. Thông tin Module

| Field | Value |
|-------|-------|
| **Feature / Module ID** | `MF03` — Student & Parent Management |
| **Module** | Bounded Context: **Student Profile & Health Records** |
| **Spec gốc** | `SFMS-MF03-IMP-001` (TDS-MF03_Backend.md v1.0) |
| **Priority** | 🟠 **P1** — trục xương sống (`00_Team_Work_Assignment_Plan.md` §3) |
| **Chủ module / Người viết test** | Bùi Quang Huy (chủ) · Nguyễn Phương Thảo (Lead Test reviewer) |
| **Data Classification** | `Restricted` — **PII trẻ em + dữ liệu sức khỏe** (cao nhất trong SFMS) |
| **Compliance Scope** | **BR-173 (PDPD)** — không sinh trắc học HS; tài liệu y tế không public URL; cấm log tên HS/allergen/tên tệp ở INFO |
| **Upstream Dependencies** | **MF01** (JWT `userId/role/schoolId`; `ParentAccountPort` — BR-010/BR-012) · **MF02** (bảng `classrooms.capacity` — BR-025; `read_only` năm học CLOSED — BR-193; đọc qua `ReferenceResolver` + COUNT, **không** qua port — L22) |
| **Downstream Consumers** | **MF05** (BR-063/BR-065) · **MF09** (BR-123 qua `AllergyQueryPort`) · **MF11** · **MF10** · **MF13** |
| **Số UC phủ** | **15/15** UC của traceability §1.3 |
| **Tổng test case** | **37** — Unit 9 · Integration 14 · Security 9 · Contract 5 · *(**0 BLOCKED** — Q1–Q5 đã chốt hết ngày 02/08, xem §2)* |

### 1.1 AI Generation Context (CASE 2.0)

| Field | Value |
|-------|-------|
| **AI Assisted?** | `Yes` |
| **Constraint Source** | `TDS-MF03_Backend.md` §13 (xương sống) · §9/§10/§16/§5.2 (expected value) · §17.1 C1–C9 (AI Prompt Constraints) · `00_Common_Conventions.md` D1–D8 |
| **Constraints Injected** | (a) mọi expected **định dạng** theo D1–D7, **không** theo mock; (b) mọi expected **message** nguyên văn `error_message.md` v1.5; (c) mọi TC truy về ≥1 UC ID của §1.3; (d) tên luật `semantic-audit.ts` copy nguyên văn từ script, không lấy từ TDS §13; (e) mock chỉ là nguồn seed/fixture; (f) bắt buộc đủ 4 lớp Unit/Integration/Security/Contract |
| **Model** | Claude Opus (theo `00_Team_Work_Assignment_Plan.md` §2c-4 — TestSpec dùng model mạnh nhất) |
| **Trust Level** | `T2 → T3 (pending Red Gate §5.1)` |

---

## 2. Logic Issues Resolved

> **Bắt buộc điền trước khi viết test.** Test case encode hành vi **đã sửa** ở cột 4, **không** phải hành vi trong spec gốc.
> ⚠️ 19 mục dưới đây là kết quả đối chiếu TDS §13/§9/§10/§15 ↔ `error_message.md` v1.5 ↔ seed `mock-data.ts` ↔ `semantic-audit.ts` ↔ D1–D8. **Mục L14 là thiếu oracle nghiệp vụ thật sự** — cần Huy + Thảo quyết định, không được "test cho xanh".

| # | Spec gốc (sai / thiếu) | Thực tế (nguồn chuẩn) | Fix áp dụng trong test |
|---|------------------------|------------------------|------------------------|
| **L1** | TDS §9.3 + §15.2: `STU-201` message = *"Parent account is already linked to 5 students. School Administration verification required."* | `error_message.md` v1.5 **MSG-110** = *"**This** parent account is already linked to 5 students. School Administration verification **is** required."* | Expected message = **MSG-110 nguyên văn**. (TDS §10 đã ghi đúng; chỉ §9.3/§15.2 lệch — Huy sửa TDS.) |
| **L2** | TDS §15.2: `STU-031` = *"Severe allergy declaration requires medical documentation."* | **MSG-112** = *"Severe allergy declaration requires **supporting** medical documentation."* | Expected = MSG-112 nguyên văn. |
| **L3** | TDS §15.2: `STU-172` = *"Health history is append-only and cannot be modified."* | **MSG-116** = *"Health history is append-only and cannot be modified **or deleted**."* | Expected = MSG-116 nguyên văn. |
| **L4** | TDS §9.3 `/request-info`: message = *"Review reason is required when requesting more information."* | **MSG-113** = *"A reason is required for this action."* (dùng chung `STU-032`, `SUP-048`) | Expected = MSG-113 nguyên văn; chi tiết ngữ cảnh đặt trong `error.details`, **không** viết lại `message`. |
| **L5** | TDS §9.3 `/approve`: message = *"Dietary type is not in the standard supported list **(Halal, Vegetarian, Gluten-Free, Nut-Free, Lactose-Free)**."* | **MSG-114** = *"Dietary type is not in the standard supported list."* | **Cập nhật 02/08 (Q1):** MSG-114 **ngừng dùng**, thay bằng **MSG-144** (BR-220). Expected = **MSG-144 nguyên văn**; danh mục 5 giá trị đặt trong `error.details.standardDiets` (D2 cho phép `details` là mảng). Nhận xét gốc của L5 — *không nhồi danh mục vào `message`* — vẫn đúng và vẫn áp dụng. |
| **L6** | TDS §15.2 dùng **ALG-004** cho ca *"xác minh mức Nặng thiếu tài liệu → 422"* | Seed: `ALG-004` = HS-3A-005, **`severity: "Trung bình"` (MODERATE)**, `status: NEEDS_INFO`. **Không seed nào** vừa SEVERE vừa thiếu `proofFile` (ALG-001/002/003 đều có tệp) | Tạo fixture mới **`FX-006`** (`ALG-901`, SEVERE, `proofFileId=null`, `PENDING`). ALG-004 chỉ dùng cho ca `NEEDS_INFO`. |
| **L7** | TDS §13 TC-UNIT-003 scenario 2 ghi *"ALG-005 seed"* cho ca MODERATE-không-tài-liệu | Seed `ALG-005` đã ở `status: VERIFIED` — không thể dùng làm input của `verify()` | Fixture **`FX-005`** reset `ALG-005` về `PENDING` trước khi Act (Props Isolation §4.0). |
| **L8** | TDS §13 dùng email PH `"binhnv@parent.vn"` | Seed `USER_ACCOUNTS`: `U-0005` Nguyễn Văn Bình = **`binhnv.parent@gmail.com`**, role `Parent`, school TH Đoàn Thị Điểm | Mọi TC dùng **`binhnv.parent@gmail.com`** (`FX-002`). |
| **L9** | TDS §13 TC-INT-002: *"classroom 3A có **50** HS ACTIVE"* | Seed `3A`: `capacity: 45`, `students: 38`. TDS-MF02 §5.2 + §13: invariant **`capacity ≤ 50`** *và* **`students ≤ capacity`** → gate thật là `students < capacity`, không phải hằng 50 | Test 2 ca: (a) lớp `4A` (`capacity=50`) đầy 50 → 409; (b) lớp `3A` (`capacity=45`) đầy 45 → 409 **cùng** mã `STU-010`/MSG-010. ⚠️ MSG-010 ghi *"Maximum allowed is 50 students."* → **sai ngữ nghĩa ở ca (b)**. **✅ CHỐT 02/08 (Q3):** cấp **MSG-142** riêng *"This classroom has reached its capacity."* (`error_message.md` v1.9), tách khỏi MSG-010 theo **D7** — cùng lý do MF02 đã tách MSG-140 khỏi MSG-010 ngày 31/07. Cả hai ca dùng `STU-010`/**MSG-142**, sĩ số thật đặt trong `error.details` `{field:"classroomId", capacity, current}`. |
| **L10** | TDS §5.2: `students.student_code VARCHAR(32) NOT NULL **UNIQUE**` (unique toàn hệ thống) | **D1** "Việc phải làm ở từng TDS" yêu cầu MF03 thêm `code VARCHAR(32)` + **`UNIQUE(school_id, code)`** | Test theo **hợp đồng công khai** (D1): JSON `id` = mã nghiệp vụ (`HS-3A-001`), path param = mã nghiệp vụ, **không** UUID (`MF03-TC-CON-001`, `MF03-TC-INT-011`). **✅ CHỐT 2026-08-02:** cột tên `code`, `UNIQUE(school_id, code)` — theo D1 và tiền lệ đã code `classrooms.code`/`uq_class_ay_code` (`V3:106,118`). `registration_no` giữ unique **toàn hệ thống** (UC-025 b6). `MF03-TC-INT-014` **đã gỡ BLOCKED**, viết đủ 5 bước. |
| **L11** | TDS §13 TC-E2E-002 expect `allergySeverity="Severe"` | **D6**: `AllergySeverity = MILD \| MODERATE \| SEVERE` (SCREAMING_SNAKE), áp đồng thời MF03+MF09 | Expected = **`"SEVERE"`**. Ba từ vựng cũ (`NANG`, `"Severe"`, `"Nặng"`) đều là **FAIL** — xem `MF03-TC-007`. |
| **L12** | Mock `interface Student` **không có** `dob`; TDS §9.2 trả `"dob": "2018-03-12"` | `students.dob DATE NOT NULL` (TDS §5.2) — field **mới** so với mock | Seed Flyway phải cấp `dob` cho 20 HS (`FX-001`); `MF03-TC-INT-010` assert `dob` là ISO `yyyy-MM-dd`, **không** `dd/MM/yyyy`. Minh bổ sung field khi sync mock. |
| **L13** | TDS §13 trích tên luật `semantic-audit.ts` | Script `semantic-audit.ts` §12 (dòng 450–461) + §6 (dòng 157–163): 4 tên **nguyên văn** `KHAI_DI_UNG_TRO_HOC_SINH_KHONG_TON_TAI`, `XAC_MINH_MUC_NANG_KHONG_CO_MINH_CHUNG`, `HOC_SINH_SAI_LOP`, `TEN_KHONG_KHOP_HOC_SINH`. Script điều kiện trên `a.severity === "Nặng"` và `a.proofFile` (tên FE) | Dùng **tên luật nguyên văn** (đã đối chiếu script, không lấy từ TDS). Điều kiện port sang `severity = SEVERE` + `proof_file_id` (D6/§5.2). |
| **L14** | TDS §9.3/§10 `STU-066` + TC-UNIT-005: reject `dietaryType="Keto"` vì *"ngoài danh mục chuẩn"*, gán oracle **BR-066** | `business_rules.md` **BR-066 nguyên văn**: *"Hệ thống hỗ trợ các loại chế độ ăn đặc biệt: **Halal, Vegetarian (Chay), Gluten-Free, Nut-Free**, **và các loại khác**."* → BR-066 **KHÔNG phải danh mục đóng**, không chống lưng cho hành vi *chặn*. Danh mục 5 giá trị chỉ tồn tại ở mock `mock-data.ts:2862` `STANDARD_DIETS = ["Halal","Vegetarian","Gluten-Free","Nut-Free","Lactose-Free"]` | **✅ CHỐT 02/08 (Q1) — cấp oracle mới thay vì chọn bừa một bên.** BR-066 **giữ nguyên là danh mục MỞ** (không sửa BR đã duyệt để hợp thức hóa hành vi). Hành vi được chống lưng bằng **BR-220 mới**: `dietaryType` ngoài 5 giá trị chuẩn **không bị từ chối** nhưng **không duyệt thẳng** — chuyển **`NEEDS_INFO`** chờ Y tế bổ sung căn cứ. Lý do chọn hướng này: từ chối thẳng thì trái BR-066 và loại oan chế độ ăn hợp lệ hiếm gặp; nhận bừa thì bếp không nấu được (BR-065/BR-082). Cấp **MSG-144**; **MSG-114 ngừng dùng**. `MF03-TC-005` gỡ BLOCKED, thêm bước 3 chứng minh đây là cổng *thẩm định* chứ không phải cổng *từ chối*. |
| **L15** | `error_message.md` §Module coverage xếp **MSG-064** vào Student & Parent Management | Không UC nào của traceability §1.3 tham chiếu MSG-064 (CF-M03-12) | **Không** TC nào dùng MSG-064. `MF03-TC-CON-002` assert spec **không** khai MSG-064 ở endpoint MF03. |
| **L16** | TDS §9.2/§9.3 **không có** annotation springdoc nào (`@Tag`/`@Operation`/`@ApiResponse`); §10 **không có** cột HTTP-status↔`@ApiResponse` | **D8** S1–S8 yêu cầu đủ; §10 của TDS **đã có** cột HTTP nên S3 đối chiếu được. **✅ 2026-08-02: D8 đã được áp dụng trong code thật** — `common/config/OpenApiConfig.java` tồn tại, và mọi controller MF02 đã có `@Tag(name="MF02 — School & Class Management")` + `@Operation` + `@ApiResponse` trỏ `ErrorResponse` | 5 TC lớp Contract (§4.4) **gỡ BLOCKED-D8**, viết và chạy bình thường. MF03 bám mẫu `com/sfms/mf02/web/AcademicYearController.java`. Vẫn cần Huy bổ sung annotation vào TDS §9.2 để §10 ↔ `@ApiResponse` đối chiếu 1-1. |
| **L17** | Mock `HEALTH_HISTORY.type` = nhãn tiếng Việt có dấu (`"Khai báo"`, `"Xác minh"`, `"Ghi chú lâm sàng"`, `"Sự cố"`, `"Cập nhật thuốc"`) | **D6** `HealthHistoryEventType` = `KHAI_BAO \| XAC_MINH \| GHI_CHU_LAM_SANG \| SU_CO \| CAP_NHAT_THUOC` | Expected trong DB/JSON = **SCREAMING_SNAKE**; seed chuyển đổi một lần ở bước sinh Flyway (D4/D6 §"Không sửa seed mock"). |
| **L18** | TDS §13 TC-INT-005 scenario 2: *"`repository.save()` → bản ghi trong DB KHÔNG đổi"* | `CREATE RULE hh_no_update ... DO INSTEAD NOTHING` khiến `UPDATE` trả **0 row affected** → Hibernate ném `OptimisticLockingFailureException`/`StaleStateException` **trước khi** commit, không "im lặng thành công" | Assert **hai điều**: (a) nội dung row không đổi, **và** (b) một trong hai — ném `OptimisticLockingFailureException` **hoặc** `@Immutable` khiến Hibernate không sinh UPDATE nào (kiểm qua SQL statement counter). Pin hành vi thực tế ở lần chạy Red Gate, ghi vào cột "Actual". |
| **L19** | TDS §10 `STU-500` message = *"System error while saving **student** data. Please try again."*, MSG nguồn ghi **MSG-001** | **MSG-001 nguyên văn** = *"System error while saving **school** data. Please try again."* (thuộc School & Class Management) | **✅ CHỐT 02/08 (Q5):** cấp **MSG-143** *"System error while saving student data. Please try again."* (`error_message.md` v1.9). Chuỗi mà TDS v1.0 viết ra hóa ra đúng nội dung cần — chỉ là chưa có mã chính thức. `MF03-TC-INT-013` nay assert **đủ** `code="STU-500"` + message = MSG-143 nguyên văn + envelope D2. |

> 🆕 **L20–L23 bổ sung 2026-08-02** — kết quả đối chiếu TestSpec/TDS v1.0 ↔ **code backend đã build thật** (`04_Development/Backend/`, MF01 + MF02 merge vào `dev` ngày 31/07). Bốn mục này không tồn tại lúc viết bản v1.0 vì lúc đó chưa có dòng code nào.

| # | Spec gốc (sai / thiếu) | Thực tế (nguồn chuẩn) | Fix áp dụng trong test |
|---|------------------------|------------------------|------------------------|
| **L20** | TDS §5.2 v1.0: `Student` tự khai `@Column(name="created_at") private Instant createdAt` + `created_by`; DDL `students` khai `created_at`/`created_by`; `student_clinical_files` khai `updated_by_user_id`, `updated_at` | **D9** (Approved 31/07) + `common/domain/BaseEntity.java`: `id`/`created_at`/`updated_at`/`created_by`/`updated_by` do `BaseEntity` + `AuditingEntityListener` giữ. Entity khai lại ⇒ **Hibernate duplicate column, build đỏ ngay** | TDS v1.1 đã gỡ. Test: `MF03-TC-INT-011` assert sau khi POST thì `created_by` **tự động** = username trong JWT (không do service gán); và không có cột `updated_by_user_id` trong `student_clinical_files` (`information_schema`). |
| **L21** | TDS §9.2 v1.0 trả `"classroomId": "8f1c…"`, `"parentId": "b2a7…"`, `parents[].parentId` = **UUID nội bộ**; §9.3/§15 còn ghi `"id": "…"` / `"id": "c3f1…"` | **D1** *"Khóa ngoại trong JSON dùng `code` của bảng đích… không phải UUID"*; tiền lệ đã code: `ClassroomResponse.academicYearId` = `"AY-2025"`. UUID trong mock FE là **giá trị điền tạm**, không phải hợp đồng | **CF-M03-13, chốt 2026-08-02**: JSON dùng `code` (`classroomId:"3A"`, `parentId:"U-0005"` = `user_accounts.code`). `MF03-TC-CON-001` mở rộng: assert **không** field nào trong spec MF03 có `format: uuid`, kể cả `*Id` khóa ngoại. FE sync khi tích hợp. |
| **L22** | §6 Entry Criteria đòi `ParentAccountPort` (MF01) + `ClassroomCapacityPort` (MF02); §4.2 hạ tầng dùng `ParentAccountPort` làm stub | Grep `interface .*Port` trong `04_Development/Backend/src/main/java` → **0 kết quả**. Cơ chế cross-module thật mà MF02 chốt (ADR-M02-5) là `common/reference/ReferenceResolver` — native query, không import entity module khác | Tra cứu thuần (classroom code↔UUID, user code↔UUID) đi qua **`ReferenceResolver`** — **Huy tự bổ sung method**, Mạnh review (§4b nói Nam *sở hữu* `common/` để chống trùng lặp, không phải để chặn người khác gõ). **`ParentAccountPort` vẫn tạo mới** nhưng **không phải viết logic**: `iam/service/UserService.create()` đã làm đủ BR-010/011/012 + sinh code + audit; Port chỉ là adapter mỏng truyền `RoleKey.PARENT`, khai interface ở `iam/port/` để MF03 inject mà **không** import `UserService` (ADR-M02-5). `ClassroomCapacityPort` **bỏ** — dùng `ReferenceResolver` + `COUNT` trực tiếp. Suspension Criteria cập nhật theo. |
| **L23** | §6 Entry Criteria: *"Flyway `V1__init_schema.sql` (Nam) đã có phần MF03: 9 bảng + CHECK + TRIGGER + RULE + REVOKE"* | `V1` = MF01 (12 bảng, không có bảng MF03 nào), `V2` = seed role/permission, `V3` = MF02. Mô hình "V1 = toàn schema" **đã bị bỏ từ MF02** | MF03 tự viết **`V4__mf03_student_parent.sql`** + **`V5__seed_mf03.sql`**. Entry Criteria sửa lại; sửa V1/V3 ⇒ **Flyway checksum mismatch** trên DB của Nam/Mạnh. |

**Tổng kết cần quyết định trước khi chuyển TestSpec sang Approved:**

| # | Câu hỏi | Người quyết | TC bị chặn | Trạng thái |
|---|---------|-------------|-----------|-----------|
| Q1 | BR-066 là danh mục **mở** ("và các loại khác") — có chặn `dietaryType` lạ hay không? Nếu chặn thì oracle là gì? | **Huy** (Traceability) + Thảo | `MF03-TC-005` | ✅ **chốt 02/08** — không chặn, không nhận bừa: chuyển `NEEDS_INFO` chờ thẩm định. Cấp **BR-220** + **MSG-144**; BR-066 giữ nguyên; MSG-114 ngừng dùng |
| Q2 | `student_code` unique **toàn hệ thống** hay `UNIQUE(school_id, code)` theo D1? | **Mạnh** (Backend/DB) + Nam | `MF03-TC-INT-014` | ✅ **chốt 02/08** — cột `code`, `UNIQUE(school_id, code)`, theo D1 + tiền lệ `classrooms` (L10) |
| Q3 | "Đạt `capacity` của lớp (<50)" có cần MSG riêng, hay dùng MSG-010 với text sai ngữ nghĩa? | **Huy** + Mạnh | `MF03-TC-INT-002` (ca b) | ✅ **chốt 02/08** — cấp **MSG-142** riêng, theo đúng tiền lệ MF02 tách MSG-140 khỏi MSG-010 ngày 31/07 (D7: một `error.code` không mang 2 nghĩa) |
| Q4 | Nam approve **D8**? TDS §9.2 bổ sung annotation? | **Nam** | `MF03-TC-CON-001..005` | ✅ **đã trả lời bằng thực hành** — `OpenApiConfig` + `@Tag`/`@Operation`/`@ApiResponse` đã có trong code MF02 (L16). Còn lại: Huy bổ sung annotation vào TDS §9.2 |
| Q5 | `STU-500` dùng MSG-001 (text "school data") hay cấp MSG mới? | **Huy** | `MF03-TC-INT-013` | ✅ **chốt 02/08** — cấp **MSG-143** riêng cho student data |

---

## 3. Test Design Specification

### TDS-01 — Scope / Phạm vi

```
MF03 (com.sfms.mf03) — các layer được kiểm thử:
├── domain/       Entity JPA + enum (Student, StudentParent, AllergyDeclaration,
│                 ChronicConditionDeclaration, SpecialMealRequest,
│                 StudentHealthRecord, StudentClinicalFile, HealthHistoryEvent, HealthDocument)
├── service/      ParentLinkService · StudentService · AllergyDeclarationService
│                 HealthReviewService · SpecialMealRequestService · StudentHealthFileService
│                 HealthHistoryService · StudentImportService · ClassHealthRegistryService
│                 → Unit test: mock repository + mock `ParentAccountPort` (chưa tồn tại — L22) + mock `ReferenceResolver`
├── web/          StudentController · AllergyDeclarationController · ChronicConditionController
│                 SpecialMealRequestController · StudentHealthController · HealthHistoryController
│                 HealthDocumentController
│                 → Integration test: MockMvc + Testcontainers PostgreSQL 16 (Flyway thật)
├── DB layer      CHECK ck_severe_needs_proof · TRIGGER check_parent_link_limit
│                 RULE hh_no_update/hh_no_delete · REVOKE UPDATE,DELETE
│                 → Integration test chạy SQL trực tiếp bằng role `sfms_app`
└── OpenAPI       /v3/api-docs  → Contract test (D8, S1–S8)
```

**Ngoài phạm vi (out of scope):** logic của MF01 (`ParentAccountPort` = stub theo ADR-000), logic MF02 (unit test mock `ReferenceResolver`; integration dùng bảng `classrooms` thật — L22), màn hình FE (thuộc `TDS-MF03_Frontend_Web.md`), MF09 render cảnh báo (chỉ test **hợp đồng** `GET /students/{id}/allergy-alerts`).

### TDS-02 — Test Basis / Cơ sở Kiểm thử

| Source | Items Derived |
|--------|--------------|
| `traceability_matrix.md` §1.3 | 15 UC ID — mọi TC ánh xạ ≥1 UC (bảng TDS-03) |
| `business_rules.md` BR-030 | ≥1 PH/HS; dị ứng & chế độ ăn phải được khai báo và lưu trong hồ sơ HS |
| `business_rules.md` BR-031 | Gate hiệu lực: Medical Staff **xem xét và xác nhận trước khi có hiệu lực** |
| `business_rules.md` BR-032 | Bulk import qua template + **quy tắc kiểm tra dữ liệu** |
| `business_rules.md` BR-201 | ≤5 HS/tài khoản PH; đạt giới hạn → **chặn**, yêu cầu School Administration xác minh |
| `business_rules.md` BR-065 | HS có dị ứng đã xác minh / chế độ ăn đặc biệt → **bắt buộc** kế hoạch bữa ăn riêng |
| `business_rules.md` BR-123 | Quét ID HS có dị ứng **đã đăng ký** → **tự động** cảnh báo trực quan + âm thanh |
| `business_rules.md` BR-172 | **Bảo vệ toàn vẹn** nhật ký, ngăn **mọi** hình thức chỉnh sửa trái phép |
| `business_rules.md` BR-173 | PDPD — không sinh trắc học trẻ em; test data synthetic |
| `business_rules.md` BR-025 (MF02) | Lớp tối đa 50 HS *(+ invariant `students ≤ capacity` của TDS-MF02 — xem L9)* |
| `business_rules.md` BR-066 (MF05) | Danh mục chế độ ăn **mở** — xem **L14 / Q1** |
| `error_message.md` v1.5 | MSG-004/005/010/012/029/031/035/036/110/111/112/113/114/115/116/137/138 — expected message **nguyên văn** |
| `00_Common_Conventions.md` D1–D7 | Expected **định dạng**: `id` = mã nghiệp vụ · envelope lỗi · `/api/v1` · ISO-8601 · client `id`→409 · enum SCREAMING_SNAKE · message tiếng Anh |
| `00_Common_Conventions.md` D8 (S1–S8) | Lớp Contract (§4.4) — đã gỡ block 02/08 (D8 hiện diện trong code MF01/MF02) |
| `TDS-MF03_Backend.md` §9/§10/§16/§5.2/§17.1 | Request/response shape · mã lỗi + HTTP status · ma trận 7 role × 20 endpoint · CHECK/TRIGGER/RULE · C1–C9 |
| `semantic-audit.ts` §12 + §6 | 4 luật (xem TDS-06) |
| `00_Team_Work_Assignment_Plan.md` §2b | Hợp đồng phân trang bất biến (5 field, clamp, `pageCount ≥ 1`, rỗng → 200) |

### TDS-03 — Test Conditions and Coverage Items

| Condition ID | Test Condition | Oracle | Coverage Item | UC | Test Cases |
|-------------|---------------|--------|---------------|----|-----------|
| `COND-001` | ≤5 HS / tài khoản PH, chặn ở cả service lẫn DB | BR-201 / MSG-110 | `ParentLinkService.linkOrCreateParent()`, TRIGGER `check_parent_link_limit` | UC-025, UC-093, UC-050 | `MF03-TC-001`, `MF03-TC-INT-007` |
| `COND-002` | ≥1 PH / HS — chặn tạo HS thiếu PH và gỡ PH cuối | BR-030 / MSG-111 | `StudentService.create()`, `ParentLinkService.unlink()` | UC-025, UC-099 | `MF03-TC-002` |
| `COND-003` | SEVERE + VERIFIED bắt buộc tài liệu y tế; MILD/MODERATE không | BR-031 / MSG-112 + `semantic-audit` §12 | `HealthReviewService.verifyAllergy()`, CHECK `ck_severe_needs_proof` | UC-077, UC-084 | `MF03-TC-003`, `MF03-TC-SEC-008` |
| `COND-004` | `healthStatus` là **dẫn xuất**, công thức `NEEDS_INFO > PENDING > VERIFIED`, rỗng ⇒ `VERIFIED` | ADR-M03-3 + seed | `StudentMapper.toResponse()` | UC-147, UC-050 | `MF03-TC-004`, `MF03-TC-INT-008` |
| `COND-005` | `dietaryType` ngoài danh mục chuẩn ⇒ **NEEDS_INFO** chờ thẩm định (không reject) | **BR-220** + MSG-144 (chốt 02/08, **L14**) | `SpecialMealRequestService.approve()` | UC-078, UC-036 | `MF03-TC-005` |
| `COND-006` | Reject / request-info **bắt buộc** lý do | BR-031 (UC-077/078 Exceptions) / MSG-113 | `HealthReviewService.requestInfo()`, `SpecialMealRequestService.reject()` | UC-077, UC-078 | `MF03-TC-006`, `MF03-TC-SEC-008` |
| `COND-007` | Enum `AllergySeverity` chỉ nhận `MILD/MODERATE/SEVERE` | **D6** / MSG-138 | DTO binding + `@Enumerated(STRING)` | UC-084, UC-093 | `MF03-TC-007`, `MF03-TC-CON-003` |
| `COND-008` | `specialDiet` = `dietaryType` của SMR APPROVED **mới nhất** | ADR-M03-3 / CF-M03-11 | `StudentMapper` (`latestApprovedMeal()`) | UC-050, UC-036 | `MF03-TC-008` |
| `COND-009` | `PATCH /health-file` chỉ nhận 4 field lâm sàng | BR-031+BR-172 / MSG-115 | `UpdateHealthFileRequest` DTO | UC-094 | `MF03-TC-009`, `MF03-TC-INT-008` |
| `COND-010` | Tạo HS ⇒ liên kết/ tạo tài khoản PH qua MF01 + ghi `KHAI_BAO` | BR-030+BR-010(MF01) / MSG-031 | `POST /api/v1/students` | UC-025 | `MF03-TC-INT-001` |
| `COND-011` | Sức chứa lớp khi tạo / chuyển lớp | BR-025 (MF02) / **MSG-142** (chốt 02/08, **L9**) | `POST`/`PATCH /students` + COUNT trên `classrooms.capacity` | UC-025, UC-099 | `MF03-TC-INT-002` |
| `COND-012` | Xác minh ⇒ cảnh báo BR-123 xuất hiện; trước xác minh ⇒ rỗng | BR-031 → BR-123 / MSG-036 | `POST /allergy-declarations/{id}/verify` → `GET /students/{id}/allergy-alerts` | UC-077, UC-145 | `MF03-TC-INT-003` ★ |
| `COND-013` | Khai báo phải trỏ HS có thật | C4 `data_model.puml` + `KHAI_DI_UNG_TRO_HOC_SINH_KHONG_TON_TAI` / MSG-137 | FK `allergy_declarations.student_id` | UC-084 | `MF03-TC-INT-004` |
| `COND-014` | `health_history_events` append-only — chặn ở **DB** | BR-172 / MSG-116 | RULE + REVOKE + `@Immutable` + không có PUT/DELETE | UC-094, UC-146, UC-051 | `MF03-TC-INT-005` |
| `COND-015` | Tên/lớp render theo **join `studentId`**, không theo chuỗi lưu | C6 + `HOC_SINH_SAI_LOP`, `TEN_KHONG_KHOP_HOC_SINH` | `PATCH /students/{id}` + `GET /students/{id}` | UC-099, UC-147 | `MF03-TC-INT-006` |
| `COND-016` | Bulk import 2 pha: validate **không ghi**, commit **all-or-nothing** | BR-032 / MSG-035 + MSG-012 | `/import/validate`, `/import/{importId}/commit` | UC-010 | `MF03-TC-INT-007` |
| `COND-017` | Hợp đồng phân trang bất biến (5 field, clamp, rỗng → 200) + `search` chỉ trên `SEARCH_FIELDS` | Team Plan §2b + §2b-6 | `StudentSpec` + `PagedResponse` | UC-147, UC-104, UC-036 | `MF03-TC-INT-009` |
| `COND-018` | Ngày/giờ JSON **luôn ISO-8601** | **D4** | Jackson config toàn cục (không `@JsonFormat`) | UC-147, UC-146 | `MF03-TC-INT-010`, `MF03-TC-CON-004` |
| `COND-019` | `id` client gửi ⇒ kiểm trùng ⇒ 409; vắng ⇒ server sinh; 201 **luôn** trả `id` | **D5** / MSG-012 | `POST /students`, `POST /allergy-declarations` | UC-025, UC-084 | `MF03-TC-INT-011` |
| `COND-020` | `student_health_records` (resource MỚI) + CHECK height/weight | CF-M03-9 + §5.2 | `GET/PUT /students/{id}/health-record` | UC-050, UC-094 | `MF03-TC-INT-012` |
| `COND-021` | Mọi lỗi trả envelope D2 đầy đủ; `details` **không bao giờ** `null` | **D2** | `@RestControllerAdvice` (common) | tất cả | `MF03-TC-INT-013`, `MF03-TC-CON-002` |
| `COND-022` | Phạm vi dữ liệu Parent = chỉ HS trong `student_parents` | BR-030 / MSG-005 | service scope check (không chỉ `@PreAuthorize`) | UC-050, UC-084, UC-104 | `MF03-TC-SEC-001`, `MF03-TC-SEC-009` |
| `COND-023` | Phạm vi Teacher = lớp chủ nhiệm; **không** tải tài liệu y tế | BR-123 + ADR-M03-5 / MSG-005 | `ClassHealthRegistryService`, `HealthDocumentController` | UC-145 | `MF03-TC-SEC-002` |
| `COND-024` | Cross-school isolation theo `schoolId` trong JWT | BR-022 (MF02) / MSG-005 | mọi service | UC-147, UC-051 | `MF03-TC-SEC-003` |
| `COND-025` | System Admin **không** có quyền nghiệp vụ; Supplier/Guest không quyền nào | §16 + BR-173 / MSG-005 | ma trận `@PreAuthorize` | tất cả | `MF03-TC-SEC-004`, `MF03-TC-SEC-005` |
| `COND-026` | Không JWT ⇒ 401 | BR-004 (MF01) / MSG-004 | Spring Security filter chain | tất cả | `MF03-TC-SEC-006` |
| `COND-027` | PDPD: không log tên HS/allergen/tên tệp; tài liệu qua `storage_key` có RBAC | BR-173 / ADR-M03-5 | logging config + `HealthDocumentController` | UC-051, UC-094 | `MF03-TC-SEC-007` |
| `COND-028` | **Gate nghiệp vụ enforce ở server** — gọi API trực tiếp bỏ qua UI vẫn bị chặn | BR-031 (gate `docsValid()` của mock chỉ ở client) | verify / reject / health-file | UC-077, UC-078, UC-094 | `MF03-TC-SEC-008` ★ |
| `COND-029` | OpenAPI spec không tái tạo lệch D1/D2/D4/D6 | **D8** S1–S8 | `/v3/api-docs` | tất cả | `MF03-TC-CON-001..005` 🔴 |

### TDS-04 — Test Techniques (ISO 29119-4)

| Technique | Applied To | Rationale |
|-----------|-----------|-----------|
| **Boundary Value Analysis** | BR-201 (4→5 OK, 5→6 chặn); BR-025 (`capacity-1`, `capacity`, `capacity+1`); `height_cm ∈ [50,220]`, `weight_kg ∈ [5,150]`; `pageSize=0/1`, `page > pageCount` | Ràng buộc đếm được — lỗi off-by-one ở BR-201 nghĩa là liên kết được HS thứ 6 mà không ai thấy |
| **Equivalence Partitioning** | `severity` (MILD/MODERATE/SEVERE × có/không `proofFileId`); `status` khai báo (PENDING/VERIFIED/NEEDS_INFO); `dietaryType` (trong/ngoài danh mục) | Ma trận 3×2 của BR-031 là nơi sinh lỗi "mất cảnh báo dị ứng" |
| **Decision Table Testing** | Công thức `healthStatus` dẫn xuất (ADR-M03-3) — 5 tổ hợp ở `MF03-TC-004` | Công thức ưu tiên `NEEDS_INFO > PENDING > VERIFIED` + ca rỗng ⇒ `VERIFIED` dễ hiện thực sai chiều |
| **State Transition Testing** | `AllergyDeclaration`: PENDING→VERIFIED / PENDING→NEEDS_INFO / NEEDS_INFO→PENDING (UC-093 resubmit); `SpecialMealRequest`: PENDING→APPROVED/REJECTED/NEEDS_INFO | §6.4/§6.5 TDS — chuyển trạng thái không hợp lệ phải bị chặn |
| **Error Guessing / Attack** | IDOR (`studentId` trong body ≠ con mình); gọi endpoint hành động bỏ qua UI gate; `PUT /health-history/{id}`; `DELETE` bằng role `sfms_app`; field lạ ở `/health-file` | AP-AI-006/013/015 của TDS §17.4 — đúng nhóm lỗi review 29/07 xếp nghiêm trọng nhất |
| **Contract Testing** | `/v3/api-docs` × 8 luật S1–S8 (D8) | Thay checklist tay; chặn lệch quay lại qua `openapi-typescript` (T3.4) |
| **Data-Integrity Testing (SQL trực tiếp)** | 7 truy vấn §14.1 TDS chạy như assertion sau seed | Bất biến phải đúng ở **DB**, không chỉ ở service |

### TDS-05 — Test Data Requirements

> Nguồn seed: `mock-data.ts` — **đã chuyển đổi** ngày sang ISO (D4) và enum sang SCREAMING_SNAKE (D6) ở bước sinh Flyway `V5__seed_mf03.sql`. Mọi giá trị dưới đây là **SYNTHETIC** (BR-173).

| Fixture ID | Type | Value / Logic | Mục đích |
|-----------|------|---------------|---------|
| `FX-001` | DB seed | 20 HS: `HS-3A-001…010`, `HS-3B-001…003`, `HS-4A-001…003`, `HS-5A-002`, `HS-1B-007`… kèm **`dob`** (field mới — L12), `school_id` = TH Đoàn Thị Điểm, `status=ACTIVE` | Dữ liệu nền mọi TC |
| `FX-002` | DB seed | Tài khoản PH (MF01): `U-0005` Nguyễn Văn Bình `binhnv.parent@gmail.com` → liên kết **HS-3A-001** + **HS-1B-007** (2 HS); `U-0013` Trần Quốc Đạt; `U-0014` Lê Hoàng Nam; `U-0016` Vũ Đình Phong (`INACTIVE`) | Phạm vi Parent (`MF03-TC-SEC-001`), BR-030/201 |
| `FX-003` | DB seed | PH `U-0901` (synthetic) đã liên kết **đúng 5** HS (`HS-5A-003…007`) | Biên BR-201 (`MF03-TC-001`) |
| `FX-004` | DB seed | PH `U-0902` đã liên kết **đúng 4** HS | Biên hợp lệ BR-201 (liên kết thứ 5) |
| `FX-005` | DB seed | `ALG-005` (HS-3A-009, Trứng, **MODERATE**, `proof_file_id=NULL`) — **reset về `PENDING`** trước Act (**L7**) | BR-031: MODERATE không cần tài liệu |
| `FX-006` | DB seed | **`ALG-901`** (HS-3A-002, Tôm, **SEVERE**, `proof_file_id=NULL`, `PENDING`) — fixture MỚI (**L6**) | BR-031: SEVERE thiếu tài liệu → 422 |
| `FX-007` | DB seed | `ALG-007` (HS-4A-001, Đậu nành, MODERATE, **PENDING**) — HS-4A-001 **không có** khai báo VERIFIED nào | `MF03-TC-INT-003` ★ (alerts rỗng → có) |
| `FX-008` | DB seed | `ALG-001`/`ALG-002` (HS-3A-001, Tôm/Cua, **SEVERE**, `VERIFIED`, `proof_file_id` = `doc-771`) | Cảnh báo đỏ BR-123, `hasSevereAllergy=true` |
| `FX-009` | DB seed | `ALG-004` (HS-3A-005, Sữa bò, MODERATE, **`NEEDS_INFO`**) | Công thức `healthStatus=NEEDS_INFO` |
| `FX-010` | DB seed | `SMR-2026-019` (HS-3A-001, `Nut-Free`, **APPROVED**, 2026-01-15) + `SMR-2026-903` (HS-3A-001, `Gluten-Free`, **APPROVED**, 2026-06-20) | `specialDiet` = APPROVED **mới nhất** (`MF03-TC-008`) |
| `FX-011` | DB seed | `SMR-2026-045` (HS-3A-005, `Lactose-Free`, **PENDING**) · `SMR-2026-902` (HS-3A-010, **`Keto`**, **PENDING**) — bản PENDING của ca Keto (seed gốc `SMR-2026-035` đã `REJECTED`) | `MF03-TC-005` (L14) |
| `FX-012` | DB seed | `HH-101…105` của HS-3A-001, `type` đã chuyển `KHAI_BAO`/`XAC_MINH`/`GHI_CHU_LAM_SANG`/`SU_CO`/`CAP_NHAT_THUOC` (**L17**), `at` ISO `Instant` | Append-only BR-172, timeline UC-146 |
| `FX-013` | DB seed | Lớp `4A` (`capacity=50`) làm **đầy đúng 50** HS ACTIVE · lớp `3A` (`capacity=45`) làm **đầy đúng 45** (**L9**) | BR-025 (`MF03-TC-INT-002`) |
| `FX-014` | DB seed | Lớp `3A` gắn `homeroom_teacher_id = U-0004` (Nguyễn Thu Trang); lớp `3B` gắn `U-0009` | Phạm vi Teacher (`MF03-TC-SEC-002`) |
| `FX-015` | DB seed | Trường thứ hai `SCH-HN-0099` + 1 HS `HS-9Z-001` thuộc trường đó | Cross-school isolation (`MF03-TC-SEC-003`) |
| `FX-016` | DB seed | `health_documents` `doc-771` (`storage_key='mf03/2026/07/doc-771.pdf'`, **không** public URL) | ADR-M03-5, `MF03-TC-SEC-007` |
| `FX-017` | file | `import_5rows.xlsx`: 3 dòng hợp lệ · 1 dòng `code = "HS-3A-001"` (trùng trong cùng trường) · 1 dòng email `"chin.parent@"` (sai định dạng BR-012) | `MF03-TC-INT-007` |
| `FX-018` | file | `import_rollback.xlsx`: 3 dòng hợp lệ, dòng 3 dùng PH `U-0901` (đã đủ 5 HS — `FX-003`) | Rollback all-or-nothing |
| `FX-019` | JWT | 7 token theo `RoleKey` (**D6**): `system-admin`(U-0001) · `school-admin`(U-0002) · `teacher`(U-0004, lớp 3A) · `parent`(U-0005) · `medical-staff`(U-0003) · `service-caterers`(U-0006) · `supplier`(U-0007); mỗi token mang `userId/role/schoolId` | Ma trận §16 (`MF03-TC-SEC-005`) |
| `FX-020` | JWT | Token `parent` của `U-0016` (thuộc trường khác — `FX-015`) + token `teacher` lớp 3B | IDOR / cross-scope |
| `FX-021` | env | `SPRING_PROFILES_ACTIVE=test`; `ParentAccountPort` = stub đếm số lần gọi (ADR-000); sức chứa lớp đọc từ bảng `classrooms` thật trong integration (L22) | Không gửi email thật, không gọi MF01 thật |
| `FX-022` | DB role | Role `sfms_app` (đã `REVOKE UPDATE, DELETE ON health_history_events`) — connection riêng, **không** dùng superuser | `MF03-TC-INT-005` scenario 3 |

**⚠️ Quy tắc test data (BR-173 / PDPD):** tên/email/SĐT trong fixture là dữ liệu mock của dự án, **không** phải HS thật. Không được import dữ liệu từ trường thật vào môi trường test, kể cả đã "che một phần". Vi phạm ⇒ chặn ở Exit Criteria §6.

### TDS-06 — Luật `semantic-audit.ts` được port (tên **nguyên văn** từ script)

| Tên luật (copy nguyên văn) | Vị trí script | Điều kiện gốc (FE) | Port sang backend | TC |
|---|---|---|---|---|
| `XAC_MINH_MUC_NANG_KHONG_CO_MINH_CHUNG` | §12, dòng ~456–459 | `a.severity === "Nặng" && a.status === "VERIFIED" && !a.proofFile` | `severity=SEVERE AND status=VERIFIED AND proof_file_id IS NULL` — chặn bởi CHECK `ck_severe_needs_proof` + service `STU-031` | `MF03-TC-003`, `MF03-TC-SEC-008` |
| `KHAI_DI_UNG_TRO_HOC_SINH_KHONG_TON_TAI` | §12, dòng ~452–453 | `!stuById.has(a.studentId)` | FK `allergy_declarations.student_id NOT NULL REFERENCES students(id)` + 404 `STU-404` | `MF03-TC-INT-004` |
| `HOC_SINH_SAI_LOP` | §6, dòng ~158–160 | `st.class !== d.class` | Lớp trả về **luôn** join từ `students.classroom_id`, không đọc chuỗi lưu ở sổ MF09 (C6) | `MF03-TC-INT-006` |
| `TEN_KHONG_KHOP_HOC_SINH` | §6, dòng ~161–162 | `st.name !== d.name` | Tên trả về **luôn** join từ `students.name` (C6) | `MF03-TC-INT-006` |

> Script đăng ký **54 luật qua `check()`** + 3 luật chỉ có `add()` (`PHIEU_TRO_MAU_KHONG_TON_TAI`, `MOC_TRO_SU_CO_KHONG_TON_TAI`, `LO_TRO_MAT_HANG_KHONG_TON_TAI`) → **57 luật thật**; 4 luật trên là toàn bộ phần liên quan MF03. Ba luật `add()`-only thuộc MF08/MF11/MF06 — **không** thuộc phạm vi TestSpec này.

---

## 4. Test Case Specification

> **TC ID format:** `MF03-TC-[NNN]` (unit) · `MF03-TC-INT-[NNN]` (integration) · `MF03-TC-SEC-[NNN]` (security) · `MF03-TC-CON-[NNN]` (contract)
> **Severity:** CRITICAL / HIGH / MEDIUM / LOW · **Status:** 🔴 Not written / 🟡 Written-failing / 🟢 Passing
> **Oracle Source:** bắt buộc — BR/UC/MSG/D-rule cụ thể. **Không** dùng giả định AI.

### 4.0 Props Isolation Boilerplate (CASE 2.0 — BẮT BUỘC)

> ⭐ Mỗi test tạo **fresh instance** qua factory; không shared mutable state. Chống AP-AI-002 (Green-from-Birth).
> Bản Java tương đương "Props Isolation Pattern" của template (template viết bằng TypeScript — đã dịch sang stack thật).

```java
// ═══════════════════════════════════════════════════════════════════
// CASE 2.0 — Props Isolation (Java / JUnit 5)
// 04_Development/Backend/src/test/java/com/sfms/mf03/fixture/Mf03Fixtures.java
// Giá trị baseline đồng bộ với §3 TDS-05 (FX-001..FX-022)
// ═══════════════════════════════════════════════════════════════════
public final class Mf03Fixtures {

  /** FX-001 — HS baseline hợp lệ. dob là field MỚI so với mock (L12). */
  public static Student.Builder student() {
    return Student.builder()
        .studentCode("HS-3A-001")                 // D1: id công khai = mã nghiệp vụ
        .registrationNo("DTD-2026-0001")
        .name("Nguyễn Minh Khang")
        .dob(LocalDate.parse("2018-03-12"))       // D4: ISO, KHÔNG "12/03/2018"
        .schoolId(SCHOOL_DTD)
        .classroomId(CLASS_3A)
        .status(StudentStatus.ACTIVE);
  }

  /** FX-006 — SEVERE thiếu tài liệu (fixture MỚI, xem L6). */
  public static AllergyDeclaration.Builder severeWithoutProof() {
    return AllergyDeclaration.builder()
        .studentId(HS_3A_002)
        .category("Hải sản (tôm, cua, sò, ốc)")
        .allergen("Tôm")
        .severity(AllergySeverity.SEVERE)         // D6: SCREAMING_SNAKE, KHÔNG "Nặng"/"NANG"/"Severe"
        .status(DeclarationStatus.PENDING)
        .proofFileId(null);
  }

  /** FX-005 — MODERATE không tài liệu, RESET về PENDING (seed gốc đã VERIFIED — L7). */
  public static AllergyDeclaration.Builder moderateWithoutProof() {
    return severeWithoutProof()
        .studentId(HS_3A_009)
        .allergen("Trứng")
        .severity(AllergySeverity.MODERATE);
  }

  /** FX-019 — JWT theo RoleKey của D6 (kebab-case, đồng thời là URL segment). */
  public static String jwt(String roleKey, UUID userId, UUID schoolId) { … }

  private Mf03Fixtures() {}
}
```

```java
// Mỗi @Test tự dựng dữ liệu của mình — KHÔNG @BeforeAll ghi DB dùng chung.
@BeforeEach void isolate() { txTemplate.execute(s -> { truncateMf03Tables(); seedBaseline(); return null; }); }
```

**Helper assert envelope D2 (dùng ở mọi error-path TC — không copy-paste chuỗi rời rạc):**

```java
/** D2 + D7: code đúng, message NGUYÊN VĂN error_message.md, details KHÔNG BAO GIỜ null. */
static ResultMatcher errorEnvelope(String code, String messageFromErrorMessageMd) {
  return result -> {
    jsonPath("$.error.code").value(code).match(result);
    jsonPath("$.error.message").value(messageFromErrorMessageMd).match(result);
    jsonPath("$.error.details").exists().match(result);      // mảng, có thể rỗng, không null
    jsonPath("$.message").doesNotExist().match(result);      // KHÔNG shape phẳng của mock
  };
}
```

---

### 4.1 Unit Test Cases — service layer

---

#### `MF03-TC-001` — BR-201: chặn liên kết học sinh thứ 6 vào một tài khoản phụ huynh

**Severity:** `CRITICAL`
**Feature Under Test:** `ParentLinkService.linkOrCreateParent()` / `ParentLinkService.link()`
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/service/ParentLinkServiceTest.java`
**TDD Phase:** 🔴 RED
**Condition Ref:** `COND-001`
**Oracle Source:** **BR-201** *"Một tài khoản phụ huynh được liên kết tối đa **5 hồ sơ học sinh**. Khi đã đạt giới hạn, hệ thống chặn thao tác liên kết thêm và yêu cầu School Administration xác minh trước khi xử lý ngoại lệ."* · **MSG-110** *"This parent account is already linked to 5 students. School Administration verification is required."* (nguyên văn — **L1**) · TDS §10 `STU-201` = HTTP **409**
**Traceability:** UC-025, UC-093, UC-050 (§1.3)

**Preconditions:** `FX-003` (PH `U-0901` đã liên kết đúng 5 HS) · `FX-004` (PH `U-0902` đã liên kết đúng 4 HS) · repository mock trả đúng số đếm.

**Test Steps (BVA — 4→5 hợp lệ, 5→6 chặn):**
1. *Arrange:* `parentUserId = U-0901` (5 liên kết), HS mới `HS-9Z-002`.
2. *Act:* gọi `linkOrCreateParent(student, "…")`.
3. *Assert:* ném `ConflictException` với `code = "STU-201"`, message = MSG-110 nguyên văn; **không** dòng `student_parents` nào được tạo (verify `repo.save()` **không** được gọi).
4. Lặp với `U-0902` (4 liên kết) → thành công, `countLinkedStudents(U-0902) == 5`.

**Expected Result (PASS):** ca 6 → `ConflictException(STU-201)`, `verify(studentParentRepo, never()).save(any())`; ca 5 → link thành công, đếm = 5.
**Expected Result (FAIL — dấu hiệu lỗi):** liên kết thứ 6 thành công (off-by-one `> 5` thay vì `>= 5`) ⇒ BR-201 bị vô hiệu, School Administration mất chốt xác minh ngoại lệ.

**Current Status:** 🔴 Not written
**Implementation Note:** service kiểm **trước** để trả đúng mã lỗi, nhưng chốt chặn cuối là TRIGGER `check_parent_link_limit` (§5.2) — xem `MF03-TC-INT-007` cho tầng DB. Đếm phải `SELECT … FOR UPDATE` (race 2 request song song).

---

#### `MF03-TC-002` — BR-030: mỗi học sinh phải có ≥1 phụ huynh (tạo & gỡ liên kết)

**Severity:** `CRITICAL`
**Feature Under Test:** `StudentService.create()` · `ParentLinkService.unlink()`
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/service/StudentServiceTest.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-002`
**Oracle Source:** **BR-030** *"Mỗi học sinh phải được liên kết với ít nhất một tài khoản phụ huynh."* · **MSG-111** *"Each student must be linked to at least one parent account."* · TDS §10 `STU-030` = HTTP **422**
**Traceability:** UC-025, UC-099

**Preconditions:** `FX-001`; HS-3A-002 chỉ có **1** PH liên kết (`FX-002`).

**Test Steps:**
1. *Act 1:* `create({name, classroomId, parent: null})` → **Assert** `ValidationException(STU-030)`, message = MSG-111, `details` chứa `{field: "parent.email"}` (TDS §9.3).
2. *Act 2:* `unlink(HS-3A-002, U-0013)` (PH cuối cùng) → **Assert** `ValidationException(STU-030)`; dòng `student_parents` **vẫn còn**.
3. *Act 3 (ca hợp lệ):* HS-3A-001 có 2 PH → `unlink` 1 PH → thành công, còn 1 dòng.

**Expected Result (PASS):** 2 ca đầu ném `STU-030`; ca 3 thành công.
**Expected Result (FAIL):** tạo được HS "mồ côi" hồ sơ ⇒ không ai nhận thông báo dị ứng/sự cố về HS đó; hoặc gỡ hết PH ⇒ BR-030 bị vi phạm im lặng (truy vấn đối soát §14.1 câu 3 sẽ ra > 0 dòng).

**Current Status:** 🔴 Not written
**Implementation Note:** BR-030 **không** biểu diễn được bằng CHECK đơn lẻ (ràng buộc liên bảng chiều ngược) → enforce trong **cùng transaction** của `create()` + job đối soát định kỳ (TDS §5.2 ghi chú, §14.1).

---

#### `MF03-TC-003` — BR-031: SEVERE bắt buộc tài liệu y tế; MODERATE/MILD thì không

**Severity:** `CRITICAL`
**Feature Under Test:** `HealthReviewService.verifyAllergy()`
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/service/HealthReviewServiceTest.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-003`
**Oracle Source:** **BR-031** *"Thông tin sức khỏe và dị ứng do phụ huynh khai báo phải được Medical Staff xem xét và xác nhận trước khi có hiệu lực."* · chú thích `semantic-audit.ts` §12 dòng ~454–455: *"BR-031 chỉ bắt buộc tài liệu y tế với mức NẶNG — mức nhẹ/trung bình Y tế xác minh trực tiếp với phụ huynh, không có tệp vẫn hợp lệ."* · **MSG-112** *"Severe allergy declaration requires supporting medical documentation."* (nguyên văn — **L2**) · TDS §10 `STU-031` = HTTP **422**
**Luật port:** `XAC_MINH_MUC_NANG_KHONG_CO_MINH_CHUNG` (tên nguyên văn — TDS-06)
**Traceability:** UC-077, UC-084

**Preconditions:** `FX-006` (`ALG-901` SEVERE, `proofFileId=null`, PENDING — **L6**) · `FX-005` (`ALG-005` MODERATE, không tệp, reset PENDING — **L7**).

**Test Steps (Equivalence Partitioning 3 severity × 2 trạng thái tệp):**
1. *Act:* `verifyAllergy(ALG-901)` → **Assert** `ValidationException(STU-031)`, message = MSG-112, **`status` vẫn `PENDING`** (không được đổi trước khi validate).
2. *Act:* `verifyAllergy(ALG-005)` (MODERATE, không tệp) → **Assert** `status = VERIFIED`.
3. *Act:* `verifyAllergy(ALG-901')` với `proofFileId = doc-771` → **Assert** `status = VERIFIED`, `verifiedAt != null`, `verifiedByUserId` = Medical Staff trong JWT.
4. *Act:* MILD không tệp → `VERIFIED` (đối xứng ca 2).

**Expected Result (PASS):** chỉ tổ hợp `SEVERE × không tệp` bị chặn; 3 tổ hợp còn lại xác minh được.
**Expected Result (FAIL):** (a) chặn cả MODERATE ⇒ Y tế không xác minh được ca thường, dữ liệu ứ ở PENDING và **không xuống được MF09** ⇒ mất cảnh báo; (b) cho qua SEVERE không tệp ⇒ vi phạm `ck_severe_needs_proof`, `semantic-audit` báo vi phạm.

**Current Status:** 🔴 Not written
**Implementation Note:** service phải kiểm **trước** để trả `STU-031`/MSG-112; CHECK `ck_severe_needs_proof` là chốt chặn cuối (nếu chỉ dựa CHECK, client nhận 500 chứ không phải 422).

---

#### `MF03-TC-004` — ADR-M03-3: công thức `healthStatus` dẫn xuất

**Severity:** `HIGH`
**Feature Under Test:** `StudentMapper.toResponse()` / `HealthStatusCalculator`
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/mapper/HealthStatusCalculatorTest.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-004`
**Oracle Source:** **ADR-M03-3** (TDS §3): *"`NEEDS_INFO` nếu có ≥1 khai báo NEEDS_INFO; ngược lại `PENDING` nếu có ≥1 PENDING; ngược lại `VERIFIED` nếu có ≥1 VERIFIED; nếu không có khai báo nào → `VERIFIED`"* · đối chiếu seed (`FX-001/005/007/009`) · **D6** `DeclarationStatus` SCREAMING_SNAKE
**⚠️ Ghi chú oracle:** đây là oracle **kiến trúc (ADR)**, **không** phải BR — BR-030/031 không quy định giá trị tổng hợp. Hợp lệ theo §2c-3b (oracle = TDS §13 + ADR), nhưng nếu Huy muốn oracle nghiệp vụ thì cần BR mới.
**Traceability:** UC-147, UC-050, UC-104

**Test Steps (Decision Table — `@ParameterizedTest`):**

| # | declarations | expected `healthStatus` | Đối chiếu seed |
|---|---|---|---|
| 1 | *(rỗng)* | `VERIFIED` | `HS-3A-002` (không khai báo) = VERIFIED ✔ |
| 2 | `[VERIFIED, VERIFIED]` | `VERIFIED` | `HS-3A-001` (ALG-001/002) ✔ |
| 3 | `[VERIFIED, PENDING]` | `PENDING` | `HS-4A-001` (ALG-007 PENDING) ✔ |
| 4 | `[VERIFIED, NEEDS_INFO]` | `NEEDS_INFO` | `HS-3A-005` (ALG-004 NEEDS_INFO) ✔ |
| 5 | `[PENDING, NEEDS_INFO]` | `NEEDS_INFO` | — (ưu tiên `NEEDS_INFO` > `PENDING`) |
| 6 | `[PENDING]` + 1 `chronic_condition_declarations` `NEEDS_INFO` | `NEEDS_INFO` | công thức tính **cả hai** bảng khai báo (ADR-M03-2) |

**Expected Result (PASS):** 6/6 hàng đúng.
**Expected Result (FAIL):** ca 1 trả `PENDING` ⇒ **toàn bộ HS không dị ứng** hiện ở tab "Chờ xử lý", Y tế phải duyệt thứ không cần duyệt; ca 5 trả `PENDING` ⇒ mất dấu HS cần bổ sung tài liệu.

**Current Status:** 🔴 Not written
**Implementation Note:** tính runtime bằng `EXISTS` subquery trong `StudentSpec` khi lọc, và bằng join khi map response — **không** thêm cột (AP-AI-009). Ca 6 nhắc: công thức phải quét **cả** `chronic_condition_declarations`.

---

#### `MF03-TC-005` — Chế độ ăn ngoài danh mục chuẩn khi duyệt (✅ **ĐÃ GỠ BLOCK 2026-08-02** — L14/Q1)

**Severity:** `HIGH`
**Feature Under Test:** `SpecialMealRequestService.approve()`
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/service/SpecialMealRequestServiceTest.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-005`
**Oracle Source:** **BR-220** *(mới 2026-08-02)* — *"`dietaryType` ngoài danh mục chuẩn **không bị từ chối** nhưng **không được duyệt thẳng**: chuyển **`NEEDS_INFO`**, Y tế bổ sung căn cứ rồi mới duyệt"* · **BR-066** giữ nguyên là danh mục **MỞ** (không dùng làm oracle cho việc chặn — đây chính là lỗi L14) · **MSG-144** *"This dietary type is outside the standard list and requires medical justification before approval."* (nguyên văn) · TDS §10 `STU-066` = HTTP **422** · danh mục chuẩn 5 giá trị: `mock-data.ts:2862` `STANDARD_DIETS`
**Traceability:** UC-078, UC-036

**Preconditions:** `FX-011` — `SMR-2026-902` (`dietaryType="Keto"`, **PENDING**) và `SMR-2026-045` (`Lactose-Free`, PENDING).

**Test Steps:**
1. *Act:* `approve(SMR-2026-902)` → **Assert** `ValidationException(STU-066)`, message = **MSG-144 nguyên văn**; `details.standardDiets` = đúng 5 giá trị (danh mục nằm trong `details`, **không** nhồi vào `message` — L5); **`status` chuyển `NEEDS_INFO`**, không phải `PENDING` và **không phải** `REJECTED` (BR-220).
2. *Act:* `approve(SMR-2026-045)` (`Lactose-Free`) → **Assert** `status = APPROVED`, `reviewedByUserId`/`reviewedAt` được ghi. *(Ca này chứng minh danh mục là **5** giá trị của mock, không phải **4** giá trị liệt kê trong BR-066.)*
3. *Act:* sau bước 1, Y tế gọi `approve(SMR-2026-902)` lần hai **kèm `reviewNote`** (căn cứ y khoa) → **Assert** `status = APPROVED`. *(Bước này chứng minh BR-220 là **cổng thẩm định**, không phải cổng từ chối — thiếu nó thì test không phân biệt được (c) với (b).)*

**Expected Result (PASS):** ca 1 → 422 `STU-066` + `NEEDS_INFO`; ca 2 → APPROVED; ca 3 → APPROVED.
**Expected Result (FAIL):** duyệt thẳng `Keto` ⇒ Service Caterers nhận yêu cầu chế biến không có quy trình chuẩn (BR-065/BR-082). **Hoặc** chuyển `REJECTED` ⇒ trái BR-066, loại oan chế độ ăn hợp lệ hiếm gặp. **Hoặc** chặn `Lactose-Free` ⇒ mâu thuẫn seed `SMR-2026-045`/`SMR-2026-019`.

**Current Status:** 🔴 Not written — **đã gỡ BLOCKED-ORACLE**: Q1 chốt phương án (c) *"nhận nhưng thẩm định"*, cấp **BR-220** + **MSG-144** ngày 02/08. **MSG-114 ngừng dùng** (`error_message.md` v1.9) vì nó cite BR-066 làm oracle cho việc từ chối.
**Implementation Note:** danh mục 5 giá trị là **cấu hình**, không hardcode (BR-220) — test phải đọc từ nguồn cấu hình, đừng nhúng mảng chuỗi vào assertion.

---

#### `MF03-TC-006` — BR-031: reject / request-info bắt buộc lý do

**Severity:** `HIGH`
**Feature Under Test:** `HealthReviewService.requestInfo()` · `SpecialMealRequestService.reject()`
**Test File:** `.../service/HealthReviewServiceTest.java`, `.../service/SpecialMealRequestServiceTest.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-006`
**Oracle Source:** **BR-031** (gate xét duyệt) + UC-077/UC-078 Exceptions *(UC file: hệ thống chặn khi thiếu lý do)* · **MSG-113** *"A reason is required for this action."* (nguyên văn — **L4**; MSG dùng chung `STU-032`/`SUP-048`) · TDS §10 `STU-032` = HTTP **422**
**Traceability:** UC-077, UC-078

**Test Steps (Equivalence: `null` / `""` / `"   "` / hợp lệ):**
1. `requestInfo(ALG-004, reason=null)` → `ValidationException(STU-032)`, message = MSG-113.
2. `requestInfo(ALG-004, reason="   ")` → `STU-032` *(chuỗi trắng cũng là thiếu — mock chặn bằng `rejectReason.trim() === ""`)*.
3. `reject(SMR-2026-045, reason="")` → `STU-032`.
4. `requestInfo(ALG-004, reason="Thiếu đơn bác sĩ xác nhận mức độ dị ứng")` → `status = NEEDS_INFO`, `reviewNote` được lưu nguyên văn.

**Expected Result (PASS):** 3 ca thiếu lý do → 422 `STU-032`; ca hợp lệ → `NEEDS_INFO` + `reviewNote`.
**Expected Result (FAIL):** lưu `NEEDS_INFO` không kèm lý do ⇒ phụ huynh không biết phải bổ sung gì, khai báo treo vô thời hạn.

**Current Status:** 🔴 Not written
**Implementation Note:** `@NotBlank` (không phải `@NotNull`) trên DTO; gate này ở mock **chỉ tồn tại phía client** → xem `MF03-TC-SEC-008`.

---

#### `MF03-TC-007` — D6: `AllergySeverity` chỉ nhận `MILD | MODERATE | SEVERE`

**Severity:** `CRITICAL`
**Feature Under Test:** `CreateAllergyDeclarationRequest` (DTO binding) · `AllergySeverity` (`common/enums/`)
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/dto/AllergySeverityBindingTest.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-007`
**Oracle Source:** **D6** bảng 8 enum: `AllergySeverity = MILD | MODERATE | SEVERE`, *"tất cả SCREAMING_SNAKE trong JSON và DB"*, nhãn tiếng Việt chỉ ở `lib/labels.ts` · **MSG-138** *"Validation failed: one or more required fields are missing or invalid."* · TDS §9.3 ghi chú *"ba từ vựng cho một khái niệm"* (**L11**)
**Traceability:** UC-084, UC-093

**Test Steps (`@ParameterizedTest` — mọi biến thể lịch sử phải FAIL):**

| Input `severity` | Nguồn lịch sử | Expected |
|---|---|---|
| `"SEVERE"` / `"MODERATE"` / `"MILD"` | **D6** | ✅ bind thành công |
| `"NANG"` | MF03 §5.2/§9.3 bản cũ | ❌ 400, MSG-138 |
| `"Severe"` / `"Moderate"` / `"Mild"` | MF03 §9.2 items[], MF09 `/allergy-alerts` bản cũ | ❌ 400 (case-sensitive) |
| `"Nặng"` / `"Trung bình"` / `"Nhẹ"` | FE `AllergyDeclaration.severity`, `semantic-audit.ts` | ❌ 400 |
| `null` / `""` | — | ❌ 400 |

**Expected Result (PASS):** 3 giá trị D6 bind được; **mọi** biến thể khác → 400 + envelope D2 với MSG-138.
**Expected Result (FAIL):** chấp nhận `"Severe"` ⇒ hai từ vựng cùng tồn tại trong DB ⇒ **MF09 so `severity == SEVERE` luôn sai** ⇒ HS dị ứng nặng không được cảnh báo và **không có exception nào được ném** (đúng lý do D6 xếp việc này là blocker, không phải chuyện đặt tên).

**Current Status:** 🔴 Not written
**Implementation Note:** `AllergySeverity` khai **một lần** ở `common/enums/` (Nam sở hữu) — MF03 **không** tự khai lại (D6). Không dùng `@JsonCreator` "khoan dung" để nhận nhãn tiếng Việt: khoan dung ở đây = tái tạo lỗi.

---

#### `MF03-TC-008` — ADR-M03-3 / CF-M03-11: `specialDiet` = SMR **APPROVED mới nhất**

**Severity:** `MEDIUM`
**Feature Under Test:** `StudentMapper.toResponse()` (tương đương `latestApprovedMeal()` của FE)
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/mapper/StudentMapperTest.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-008`
**Oracle Source:** **ADR-M03-3** + **CF-M03-11** (`Student.specialDiet` đã `@deprecated`, nguồn duy nhất là `SpecialMealRequest` **APPROVED gần nhất**) · `src/lib/health-docs.ts` `latestApprovedMeal()`
**Traceability:** UC-050, UC-036

**Preconditions:** `FX-010` — HS-3A-001 có `SMR-2026-019` (`Nut-Free`, APPROVED, 2026-01-15) **và** `SMR-2026-903` (`Gluten-Free`, APPROVED, 2026-06-20); HS-3A-005 chỉ có `SMR-2026-045` (`Lactose-Free`, **PENDING**).

**Test Steps:**
1. HS-3A-001 → **Assert** `specialDiet == "Gluten-Free"` (bản APPROVED mới nhất, **không** phải bản cũ hơn).
2. HS-3A-005 → **Assert** `specialDiet == null` **và** `hasSpecialDiet == "no"` (PENDING **không** tính).
3. HS không có SMR nào → `specialDiet == null`, `hasSpecialDiet == "no"`.
4. **Assert schema:** entity `Student` **không** có field/cột `specialDiet` (reflection: `assertThat(Student.class.getDeclaredFields()).noneMatch(f -> f.getName().equals("specialDiet"))`).

**Expected Result (PASS):** 4/4.
**Expected Result (FAIL):** lấy bản APPROVED **cũ nhất** hoặc tính cả PENDING ⇒ Caterer chuẩn bị sai suất riêng (BR-065/BR-082); có cột `special_diet` ⇒ AP-AI-009.

**Current Status:** 🔴 Not written

---

#### `MF03-TC-009` — UC-094: `PATCH /health-file` chỉ nhận 4 field lâm sàng

**Severity:** `HIGH`
**Feature Under Test:** `UpdateHealthFileRequest` DTO + `StudentHealthFileService.appendClinical()`
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/dto/UpdateHealthFileRequestTest.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-009`
**Oracle Source:** **BR-172** + **BR-031** (gate hiệu lực chỉ ở UC-077/078) · TDS §9.3 + §17.1 **C8** · **MSG-115** *"This field cannot be edited through this endpoint."* · TDS §10 `STU-094` = HTTP **400**
**Traceability:** UC-094

**Test Steps:**
1. Payload đủ 4 field cho phép (`medicalConditions`, `medications`, `emergencyContact`, `emergencyInstructions`) → bind OK. *(`emergencyContact` là field FE **đang gửi** và cột `emergency_contact` đã có ở §5.2 — TDS §9.3 ghi chú sửa 2026-07-29.)*
2. Payload kèm `allergies` → `ValidationException(STU-094)`, message = MSG-115, `details` chỉ ra tên field bị từ chối.
3. Tương tự với `specialDiet`, `healthStatus`, `allergySeverity`, **`medicalDocuments`** → `STU-094`. *(`medicalDocuments` bị từ chối **có chủ đích** — CF-M03-10: tài liệu thuộc **từng** tác nhân dị ứng.)*
4. **Assert không im lặng bỏ qua:** với payload ca 2, `verify(clinicalFileRepo, never()).save(any())`.

**Expected Result (PASS):** ca 1 OK; ca 2–3 → 400 `STU-094`; ca 4 không ghi gì.
**Expected Result (FAIL):** nhận field lạ rồi **bỏ qua im lặng** (đúng lỗi `kitchen-inspections` từng mắc trong mock) ⇒ Y tế tưởng đã sửa dị ứng nhưng không có gì thay đổi — AP-AI-015.

**Current Status:** 🔴 Not written
**Implementation Note:** bật `spring.jackson.deserialization.fail-on-unknown-properties=true` cho DTO này (hoặc `@JsonIgnoreProperties(ignoreUnknown = false)`), **không** dùng default khoan dung của Jackson.

---

### 4.2 Integration Test Cases — endpoint → DB

> **Hạ tầng chung:** `@SpringBootTest(webEnvironment = RANDOM_PORT)` + **Testcontainers `PostgreSQLContainer("postgres:16")`**, Flyway chạy thật (V1 MF01 + V3 MF02 + `V4__mf03_student_parent.sql` + `V5__seed_mf03.sql`) để **CHECK / TRIGGER / RULE / REVOKE của §5.2 được kiểm thật**. Timeout 120s/class. `ParentAccountPort` = stub đếm lời gọi (`FX-021`), **không** gửi email thật (ADR-000).
> **Bắt buộc mọi TC dưới đây:** response lỗi assert bằng helper `errorEnvelope(...)` (§4.0) — đúng envelope **D2**, message **nguyên văn** `error_message.md`.

---

#### `MF03-TC-INT-001` — UC-025: tạo HS + liên kết/tạo tài khoản PH + ghi lịch sử `KHAI_BAO`

**Severity:** `CRITICAL`
**Feature Under Test:** `POST /api/v1/students` (full flow: controller → service → `ParentAccountPort` → DB)
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/web/StudentCreateIT.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-010`
**Oracle Source:** **BR-030** (≥1 PH/HS) · **BR-010/BR-012 (MF01)** — tài khoản PH + email kích hoạt, gọi qua port · **BR-172** (ghi lịch sử tại thời điểm hành động) · **MSG-031** *"User account created successfully."* · **MSG-012** *"Student admission code is invalid or has already been used."* · TDS §9.3 shape 201
**Traceability:** UC-025

**Preconditions:** `FX-001`, `FX-002`, `FX-021` · lớp `2A` còn chỗ · **không** có user nào với email `saunv.parent@gmail.com`.

**Test Steps:**
1. `POST /api/v1/students` (JWT `school-admin`) body TDS §9.3: `{name, dob:"2019-03-12", classroomId, parent:{fullName, email:"saunv.parent@gmail.com", phone, relationship:"Bố"}}`.
2. *Assert response 201:* `id` = **mã nghiệp vụ** (khớp `^HS-\d[A-Z]-\d{3}$`, **không** UUID — D1) · `studentCode`, `registrationNo` có giá trị · `dob == "2019-03-12"` (ISO — D4) · `healthStatus == "VERIFIED"` (chưa có khai báo nào — ADR-M03-3) · `allergies == []` · `parentAccountCreated == true`.
3. *Assert DB:* đúng **1** dòng `student_parents` trỏ `parent_user_id` mới (BR-030) · đúng **1** dòng `health_history_events` `type = 'KHAI_BAO'` cùng `student_id` (BR-172, **D6/L17**) · **0** cột dẫn xuất trên `students` (§14.1 câu 6).
4. *Assert port:* `parentAccountPortStub.createCallCount() == 1` (MF01 gửi mail — BR-010); MF03 **không** tự insert vào `user_accounts`.
5. *Scenario 2 — email PH đã tồn tại:* body `parent.email = "binhnv.parent@gmail.com"` (`U-0005`, đang liên kết 2 HS) → 201, `parentAccountCreated == false`, **không** user mới, `countLinkedStudents(U-0005) == 3`.
6. *Scenario 3 — `studentCode` trùng:* body kèm `"id": "HS-3A-001"` → **409** `STU-012` + MSG-012 (D5 — xem `MF03-TC-INT-011`).

**Expected Result (PASS):** 201 + đủ 3 tác dụng phụ (link, history, port) · scenario 2 không tạo user trùng · scenario 3 → 409.
**Expected Result (FAIL):** MF03 tự tạo `user_accounts`/tự gửi mail (vi phạm C4 + §4b "tham chiếu bằng ID") · thiếu dòng `KHAI_BAO` ⇒ timeline UC-146 mất mốc đầu tiên · tạo user PH trùng email ⇒ BR-201 đếm sai vì liên kết rải trên 2 tài khoản.

**DB Assertion:**
```sql
SELECT COUNT(*) FROM student_parents  WHERE student_id = :sid;                    -- 1
SELECT type      FROM health_history_events WHERE student_id = :sid ORDER BY at;  -- {'KHAI_BAO'}
SELECT COUNT(*) FROM user_accounts    WHERE email = 'binhnv.parent@gmail.com';    -- 1 (scenario 2)
```

**Current Status:** 🔴 Not written

---

#### `MF03-TC-INT-002` — BR-025: sức chứa lớp khi tạo & khi chuyển lớp (✅ Q3 chốt 02/08 — MSG-142)

**Severity:** `HIGH`
**Feature Under Test:** `POST /api/v1/students` · `PATCH /api/v1/students/{id}` + COUNT trên `classrooms.capacity`
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/web/StudentCapacityIT.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-011`
**Oracle Source:** **BR-025 (MF02)** *"Mỗi lớp học có sức chứa tối đa **50 học sinh**."* + invariant **TDS-MF02** §4/§5.2 *"`capacity ≤ 50`, `students ≤ capacity`"* · **MSG-142** *"This classroom has reached its capacity."* (`error_message.md` v1.9, chốt 02/08 — **không** còn mượn MSG-010 của MF02) · TDS §10 `STU-010` = HTTP **409**
**Traceability:** UC-025, UC-099

**Preconditions:** `FX-013` — `4A` (`capacity=50`) đầy **50**; `3A` (`capacity=45`) đầy **45**; `3B` (`capacity=45`) có **40**.

**Test Steps (BVA):**
1. `POST /students {classroomId: "4A"}` (`capacity=50`, đầy 50) → **409** `STU-010`, message = **MSG-142 nguyên văn**; `details` = `{field:"classroomId", capacity:50, current:50}`.
2. `POST /students {classroomId: "3A"}` (`capacity=45`, đầy 45 < 50) → **409** `STU-010`, message = **MSG-142 nguyên văn** (giống hệt ca 1 — đây là điểm của Q3: message **không** nhắc con số nào nên đúng cho mọi `capacity`); `details` = `{field:"classroomId", capacity:45, current:45}`.
3. **Assert message của ca 1 và ca 2 giống nhau từng ký tự**, và **không** chứa chuỗi `"50"` — chốt chặn để không ai lặng lẽ quay về MSG-010.
3. `POST /students {classroomId: 3B}` (40/45) → **201** (biên hợp lệ).
4. `PATCH /students/HS-4A-001 {classroomId: <4A đầy>}` → **409** `STU-010`; **assert DB `classroom_id` không đổi** (không được ghi nửa vời).
5. Lớp thuộc năm học `CLOSED` → **409** `STU-193` *"Classroom belongs to a closed academic year and is read-only."* (BR-193 — MF02). ⚠️ TDS §10 ghi cột "MSG nguồn" của `STU-193` là **`—`** ⇒ **thiếu MSG** → báo Huy; assert chỉ `code` + HTTP 409, **không** assert message.

**Expected Result (PASS):** 3 ca vượt/đầy → 409, DB không đổi; ca còn chỗ → 201.
**Expected Result (FAIL):** tạo được HS thứ 51 (hoặc thứ 46 ở lớp capacity 45) ⇒ vi phạm invariant `students ≤ capacity` của MF02, và §14.1 của TDS-MF02 sẽ ra dòng vi phạm.

**Current Status:** 🔴 Not written
**Implementation Note:** đếm sĩ số phải trong **cùng transaction** + `SELECT … FOR UPDATE` trên `classrooms` (hai đơn tạo song song ở lớp còn 1 chỗ = cùng lọt).

---

#### `MF03-TC-INT-003` — ★ BR-031 → BR-123: xác minh xong thì cảnh báo mới xuất hiện

**Severity:** `CRITICAL` — **test quan trọng nhất của MF03**
**Feature Under Test:** `POST /api/v1/allergy-declarations/{id}/verify` → `GET /api/v1/students/{id}/allergy-alerts`
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/web/AllergyVerifyAlertIT.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-012`
**Oracle Source:** **BR-031** *"…phải được Medical Staff xem xét và xác nhận **trước khi có hiệu lực**"* · **BR-123** *"Khi quét ID học sinh có dị ứng thực phẩm **đã đăng ký**…, hệ thống **tự động kích hoạt cảnh báo trực quan và âm thanh**…"* · **MSG-036** *"Allergy profile updated successfully and alerts set."* · **D6** `AllergySeverity` · TDS §9.3 shape `/allergy-alerts`
**Traceability:** UC-077, UC-145, UC-104

**Preconditions:** `FX-007` — `ALG-007` (HS-4A-001, Đậu nành, **MODERATE**, `PENDING`); HS-4A-001 **không** có khai báo `VERIFIED` nào.

**Test Steps:**
1. *Arrange-assert (trạng thái TRƯỚC):* `GET /students/HS-4A-001/allergy-alerts` → 200, **`alerts == []`** (chưa xác minh ⇒ **chưa** cảnh báo — đây là nửa quan trọng nhất của test).
2. *Act:* `POST /allergy-declarations/ALG-007/verify` (JWT `medical-staff`) → **200**, body `status == "VERIFIED"`, `verifiedAt` là ISO `date-time` (D4), `verifiedBy` có giá trị.
3. *Assert sau xác minh:* `GET /students/HS-4A-001/allergy-alerts` → `alerts[*].allergen` chứa `"Đậu nành"`, `alerts[0].severity == "MODERATE"` (**D6**), `hasSevereAllergy == false` (MODERATE **không** bật cảnh báo đỏ).
4. *Assert domain event:* `AllergyVerified` được publish đúng **1** lần (MF05/MF09 tiêu thụ — TDS §7.1).
5. *Assert lịch sử:* thêm đúng **1** dòng `health_history_events` `type='XAC_MINH'` (BR-172), ghi trong **cùng transaction**.
6. *Ca SEVERE:* xác minh `ALG-001` (Tôm, SEVERE, có `proof_file_id`) → `hasSevereAllergy == true` ⇒ FE bật cảnh báo đỏ + âm thanh.
7. *Ca âm:* khai báo `NEEDS_INFO` (`ALG-004`) **không** xuất hiện trong `alerts`.

**Expected Result (PASS):** alerts rỗng trước, đúng nội dung sau; event + history đúng 1 dòng; `hasSevereAllergy` đúng theo severity.
**Expected Result (FAIL):** alerts trả cả `PENDING`/`NEEDS_INFO` ⇒ cảnh báo dựa trên dữ liệu **chưa được Y tế xác nhận** (ngược BR-031); **hoặc** alerts vẫn rỗng sau khi verify ⇒ tái tạo đúng sự cố ghi trong `mock-data.ts` dòng 3258–3272: *"em dị ứng Trứng vẫn được giao Thịt kho trứng cút"* vì bên phát suất tra ra danh sách rỗng.

**DB Assertion:**
```sql
SELECT status, verified_at IS NOT NULL FROM allergy_declarations WHERE id = :algId;     -- VERIFIED, true
SELECT COUNT(*) FROM health_history_events WHERE student_id = :sid AND type='XAC_MINH'; -- 1
```

**Current Status:** 🔴 Not written
**Implementation Note:** `/allergy-alerts` **chỉ** đọc `WHERE status='VERIFIED'` (ADR-M03-2). MF09 gọi qua `AllergyQueryPort`, **không** query bảng của MF03 — không sinh bản sao thứ hai của logic "khai báo nào được tính là cảnh báo" (chốt chủ sở hữu TDS §9.3, ADR-M03-6 / ADR-M09-9).

---

#### `MF03-TC-INT-004` — C4: khai báo phải trỏ học sinh có thật

**Severity:** `HIGH`
**Feature Under Test:** `POST /api/v1/allergy-declarations` + FK `allergy_declarations.student_id`
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/web/AllergyDeclarationIT.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-013`
**Oracle Source:** ràng buộc **C4** `data_model.puml` *"Xác minh theo TỪNG tác nhân (BR-031)"* + FK bắt buộc · luật **`KHAI_DI_UNG_TRO_HOC_SINH_KHONG_TON_TAI`** (`semantic-audit.ts` §12 — tên nguyên văn) · **MSG-137** *"Resource not found."* · TDS §10 `STU-404` = HTTP **404**
**Traceability:** UC-084

**Test Steps:**
1. `POST /allergy-declarations {studentId:"HS-KHONG-CO", …}` (JWT `parent`) → **404** `STU-404`, message = MSG-137.
2. *Assert DB:* **0** dòng `allergy_declarations` được thêm.
3. *Đối soát toàn bảng sau seed* (§14.1 câu 1):
   ```sql
   SELECT a.id FROM allergy_declarations a
   LEFT JOIN students s ON s.id = a.student_id WHERE s.id IS NULL;   -- Expected: 0 rows
   ```
   Lặp cho `chronic_condition_declarations`, `special_meal_requests`, `health_history_events`, `student_health_records`, `student_clinical_files`, `health_documents`.
4. *Ca DB-level:* `INSERT` trực tiếp bằng role `sfms_app` với `student_id` ngẫu nhiên → thất bại vì FK (không chỉ dựa service).

**Expected Result (PASS):** 404 + không ghi; 7/7 truy vấn đối soát trả 0 dòng; FK chặn insert thô.
**Expected Result (FAIL):** khai báo "mồ côi" tồn tại ⇒ `semantic-audit` báo vi phạm và cảnh báo dị ứng không bao giờ tới được HS nào.

**Current Status:** 🔴 Not written
**Implementation Note:** 404 (không phải 403) vì `studentId` **không tồn tại**; nếu tồn tại nhưng **ngoài phạm vi** của PH ⇒ **403** `STU-005` (xem `MF03-TC-SEC-001`). Hai ca này phải phân biệt được: MSG-137 ghi *"không tồn tại **hoặc nằm ngoài phạm vi dữ liệu được phép đọc**"* (dùng cho `GET` để không rò tồn tại), còn TDS §13 TC-E2E-001 chốt **403** cho `POST` khai báo.

---

#### `MF03-TC-INT-005` — BR-172: `health_history_events` append-only, chặn ở tầng DB

**Severity:** `CRITICAL`
**Feature Under Test:** `HealthHistoryController` (không có PUT/DELETE) · RULE `hh_no_update`/`hh_no_delete` · `REVOKE UPDATE, DELETE` · `@Immutable`
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/web/HealthHistoryAppendOnlyIT.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-014`
**Oracle Source:** **BR-172** *"Hệ thống phải có cơ chế **bảo vệ toàn vẹn** của nhật ký, ngăn chặn **mọi** hình thức chỉnh sửa trái phép."* · **MSG-116** *"Health history is append-only and cannot be modified or deleted."* (nguyên văn — **L3**) · TDS §10 `STU-172` = HTTP **405** · ADR-M03-4
**Traceability:** UC-094, UC-146, UC-051

**Preconditions:** `FX-012` (`HH-101…105`) · `FX-022` (connection dùng role `sfms_app`, **không** superuser).

**Test Steps (3 tầng phòng thủ — phải chặn ở **cả ba**):**
1. **Tầng API:** `PUT /api/v1/health-history/HH-101 {event:"sửa nội dung"}` → **405** `STU-172`, message = MSG-116. Tương tự `DELETE /health-history/HH-101` → 405; `PATCH` → 405.
2. **Tầng ORM:** load entity, đổi `event`, `repository.save(entity)` → **Assert (a)** nội dung row trong DB **không đổi**; **Assert (b)** một trong hai: ném `OptimisticLockingFailureException`/`StaleStateException`, **hoặc** SQL statement counter cho thấy Hibernate **không sinh** câu `UPDATE` nào (`@Immutable`). ⚠️ **L18** — pin hành vi thực tế ở lần chạy Red Gate rồi ghi vào cột "Actual" §5.1; **không** viết assertion đoán trước.
3. **Tầng SQL:** bằng role `sfms_app` chạy `UPDATE health_history_events SET event='x' WHERE id=:id` → **0 row affected** (RULE `DO INSTEAD NOTHING`); `DELETE FROM health_history_events WHERE id=:id` → **thất bại / 0 dòng bị xoá** (REVOKE).
4. **Assert quyền** (§14.1 câu 5):
   ```sql
   SELECT grantee, privilege_type FROM information_schema.role_table_grants
   WHERE table_name='health_history_events' AND privilege_type IN ('UPDATE','DELETE');  -- 0 rows cho sfms_app
   ```
5. **Assert cơ chế đính chính:** sửa sai ⇒ service append **dòng mới** `type='GHI_CHU_LAM_SANG'`; số dòng **tăng**, dòng cũ **còn nguyên** (hệ quả ADR-M03-4).

**Expected Result (PASS):** 405 ở API · row không đổi ở ORM · 0 row affected + REVOKE ở SQL · 0 dòng grant · đính chính bằng bản ghi mới.
**Expected Result (FAIL):** một `@Transactional` quên `readOnly` là đủ phá nếu chỉ chặn ở service ⇒ nhật ký y tế sửa được ⇒ mất giá trị pháp lý (BR-172), UC-051/UC-146 không còn "read-only timeline".

**Current Status:** 🔴 Not written
**Implementation Note:** test **phải** dùng datasource role `sfms_app`; nếu container chạy bằng superuser thì REVOKE vô nghĩa và test **xanh giả** — đúng ca AP-AI-002, kiểm ở Red Gate §5.1.

---

#### `MF03-TC-INT-006` — C6: tên/lớp luôn join theo `studentId`, không theo chuỗi lưu

**Severity:** `HIGH`
**Feature Under Test:** `PATCH /api/v1/students/{id}` → `GET /api/v1/students/{id}` (+ hợp đồng cho MF09)
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/web/StudentClassChangeIT.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-015`
**Oracle Source:** ràng buộc **C6** `data_model.puml` *"Truy xuất nối bằng KHOÁ, không bằng tên"* · luật **`HOC_SINH_SAI_LOP`** + **`TEN_KHONG_KHOP_HOC_SINH`** (`semantic-audit.ts` §6 — tên nguyên văn) · **MSG-029** *"Profile updated successfully."*
**Traceability:** UC-099, UC-147, UC-145

**Preconditions:** HS-3A-001 đang lớp `3A`, có bản ghi `student_distributions` cũ (MF09) ghi `class="3A"`, `name="Nguyễn Minh Khang"`; lớp `3B` còn chỗ.

**Test Steps:**
1. `PATCH /students/HS-3A-001 {classroomId: <3B>}` → **200** MSG-029.
2. `GET /students/HS-3A-001` → `class == "3B"` (dẫn xuất từ join `classroom_id`).
3. `PATCH /students/HS-3A-001 {name:"Nguyễn Minh Khang B"}` → `GET` trả tên mới.
4. *Assert C6:* đọc lại dữ liệu MF09 qua port/`GET` — tên & lớp hiển thị **theo join `studentId`**, không theo chuỗi lưu trong sổ ⇒ 2 luật `HOC_SINH_SAI_LOP` / `TEN_KHONG_KHOP_HOC_SINH` **không** phát sinh vi phạm sau khi đổi lớp.
5. *Assert schema:* `students` **không** có cột `class`/`parent` (§14.1 câu 6).

**Expected Result (PASS):** 5/5.
**Expected Result (FAIL):** `class`/`name` lưu thành cột hoặc copy sang bảng MF09 ⇒ sau khi chuyển lớp, sổ phát suất sai lệch âm thầm — đúng 2 luật mà `semantic-audit.ts` §6 tồn tại để bắt.

**Current Status:** 🔴 Not written

---

#### `MF03-TC-INT-007` — UC-010: bulk import 2 pha, commit all-or-nothing

**Severity:** `HIGH`
**Feature Under Test:** `POST /api/v1/students/import/validate` → `POST /api/v1/students/import/{importId}/commit`
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/web/StudentImportIT.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-016`, `COND-001`
**Oracle Source:** **BR-032** *"Hệ thống hỗ trợ nhập hàng loạt… qua template được định nghĩa sẵn với **quy tắc kiểm tra dữ liệu**."* · **BR-012 (MF01)** định dạng email · **BR-201** · **MSG-035** *"Student profiles and parent links imported successfully."* · **MSG-012** · **MSG-110** · ADR-M03-6
**Traceability:** UC-010

**Preconditions:** `FX-017` (file 5 dòng: 3 hợp lệ, 1 trùng `HS-3A-001`, 1 email `chin.parent@`) · `FX-018` (file rollback) · `FX-003` (PH `U-0901` đã đủ 5 HS).

**Test Steps:**
1. *Pha 1:* `POST /import/validate` (multipart, JWT `school-admin`) → **200**, `total=5, validCount=3, invalidCount=2`, `duplicateCount` khớp; `rows` chứa dòng `code="STU-012"` (message = **MSG-012 nguyên văn**) và dòng lỗi định dạng email.
2. *Assert pha 1 KHÔNG ghi:* `SELECT COUNT(*) FROM students` **không đổi**; **0** dòng `student_parents` mới.
3. *Pha 2:* `POST /import/{importId}/commit` → **200** MSG-035, `imported=3, skipped=2`, `newParentAccounts` khớp số PH mới.
4. *Assert DB:* đúng **3** `students` + **3** `student_parents` + **3** dòng `health_history_events` `KHAI_BAO`.
5. *Rollback:* dùng `FX-018` (dòng 3 gán PH đã đủ 5 HS) → `commit` trả **409** `STU-201` (MSG-110) **và** `SELECT COUNT(*) FROM students` **không đổi** — **không** HS nào được tạo (all-or-nothing).
6. *Ca `importId` lạ:* `commit` với `importId` không tồn tại → **404** `STU-404` (MSG-137).
7. *Template:* `GET /students/template` → 200, content-type spreadsheet (UC-010 bước 1).

**Expected Result (PASS):** validate không ghi · commit đúng 3 · rollback sạch · 404 cho importId lạ.
**Expected Result (FAIL):** commit ghi từng dòng (như mock gọi `create.mutate()` trong vòng lặp) ⇒ lỗi giữa chừng để lại dữ liệu **nửa vời**, School Admin không biết đã nhập tới đâu (CF-M03-8).

**Current Status:** 🔴 Not written
**Implementation Note:** `commit` chạy **một** `@Transactional`; TRIGGER `check_parent_link_limit` bắn ở dòng 3 phải làm **rollback toàn bộ** — kiểm bằng đếm trước/sau, không tin log.

---

#### `MF03-TC-INT-008` — UC-094: cập nhật lâm sàng **không** đổi trạng thái xác minh

**Severity:** `HIGH`
**Feature Under Test:** `PATCH /api/v1/students/{id}/health-file` · `GET /api/v1/students/{id}/health-file`
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/web/HealthFileIT.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-009`, `COND-004`
**Oracle Source:** **BR-172** · **BR-031** (gate hiệu lực **chỉ** ở UC-077/078) · **MSG-036** / **MSG-029** · **MSG-115** (`STU-094`, HTTP **400**) · TDS §17.1 **C8**
**Traceability:** UC-094, UC-051

**Preconditions:** HS-3A-005 có `ALG-004` `NEEDS_INFO` ⇒ `healthStatus` dẫn xuất = `NEEDS_INFO` (`FX-009`).

**Test Steps:**
1. `PATCH /students/HS-3A-005/health-file {medicalConditions, medications, emergencyContact, emergencyInstructions}` (JWT `medical-staff`) → **200**.
2. *Assert bất biến:* `GET /students/HS-3A-005` → `healthStatus` **vẫn** `"NEEDS_INFO"`; `allergy_declarations.status` của ALG-004 **không đổi**.
3. *Assert lịch sử:* thêm đúng **1** dòng `health_history_events` `type='GHI_CHU_LAM_SANG'`.
4. *Ca field lạ:* `PATCH … {allergies:["Tôm"]}` → **400** `STU-094` + MSG-115; **0** dòng lịch sử mới; `student_clinical_files` **không đổi**.
5. *Ca `medicalDocuments`:* `PATCH … {medicalDocuments:["x.pdf"]}` → **400** `STU-094` (từ chối **có chủ đích** — CF-M03-10).
6. *Ca đọc:* `GET /students/{id}/health-file` **có** trả `medicalDocuments[]` dạng **dẫn xuất** (join từ `allergy_declarations.proof_file_id`) — đọc được, ghi không được.

**Expected Result (PASS):** 6/6.
**Expected Result (FAIL):** `healthStatus` bị đẩy về `VERIFIED` sau khi ghi chú lâm sàng ⇒ khai báo chưa được duyệt bỗng "có hiệu lực" (ngược BR-031) — đúng lỗi mà bản sửa mock 27/07/2026 đã dọn (*"`saveEdit()` KHÔNG còn set `healthStatus:'VERIFIED'` vô điều kiện"*).

**Current Status:** 🔴 Not written

---

#### `MF03-TC-INT-009` — Hợp đồng phân trang / search / filter (Team Plan §2b)

**Severity:** `HIGH`
**Feature Under Test:** `GET /api/v1/students` (`StudentSpec` + `PagedResponse<T>`)
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/web/StudentListContractIT.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-017`
**Oracle Source:** `00_Team_Work_Assignment_Plan.md` **§2b-1** (5 field bất biến, không wrapper) · **§2b-3** (thứ tự áp lọc) · **§2b-6** (edge cases) · TDS §9.2 (bảng param) · TDS §17.1 **C1** · `SEARCH_FIELDS` = `students.name`, `students.code`, `classrooms.name` (join)
**Traceability:** UC-147, UC-104, UC-036, UC-050

**Test Steps:**
1. `GET /students?page=1&pageSize=8` → **200**; `keys == ["items","page","pageCount","pageSize","total"]` — đủ **5 field**, **không** wrapper `data`/`result`.
2. *Clamp:* `?page=99&pageSize=8` → `page == pageCount`, `items` = trang cuối (§2b-6).
3. *Tập rỗng:* `?class=KHONG-TON-TAI` → **200** với `total=0, pageCount=1, page=1, items=[]` — **không** 404.
4. *`pageSize` phi lý:* `?pageSize=0` và `?pageSize=-5` → clamp về **1**.
5. *`search` chỉ trên `SEARCH_FIELDS`:* `?search=Khang` khớp `name`; `?search=HS-3A-001` khớp `students.code`; `?search=3A` khớp `classrooms.name`; `?search=<giá trị chỉ tồn tại ở cột khác, vd `registration_no`>` → **0 kết quả** (chứng minh **không** OR mọi cột String).
6. *Case-insensitive:* `?search=khang` và `?search=KHANG` cho cùng kết quả (`ILIKE`).
7. *Lọc dẫn xuất:* `?healthStatus=VERIFIED`, `?not:healthStatus=VERIFIED`, `?hasAllergy=yes`, `?hasSpecialDiet=yes` khớp công thức `EXISTS` (ADR-M03-3); tổng 2 tab `healthStatus=VERIFIED` + `not:healthStatus=VERIFIED` == `total` khi không lọc.
8. *Lọc theo khoá:* `?parentId=<U-0005>` → đúng **2** HS (`HS-3A-001`, `HS-1B-007`); `?classroomId=<3A>` → mọi item lớp 3A. *(Mock dùng `?parent=<tên>` — CF-M03-7; backend **không** hỗ trợ lọc theo tên.)*
9. *Giá trị rỗng bị bỏ qua:* `?class=` → như không truyền (§2b-6).
10. *Không rò cột điều khiển:* `page/pageSize/search/dateFrom/dateTo/mealPrefix` **không** bị hiểu thành lọc field (§2b-2 "các key điều khiển").

**Expected Result (PASS):** 10/10.
**Expected Result (FAIL):** thiếu `pageCount` ⇒ FE mất phân trang; `search` OR mọi cột ⇒ full table scan trên PostgreSQL (§2b-3 bước 5); tập rỗng trả 404 ⇒ FE hiện lỗi thay vì "0 kết quả".

**Current Status:** 🔴 Not written
**Implementation Note:** `SEARCH_FIELDS` khai theo **tên field JSON**; Specification map sang cột mã ở DB (D1 ghi chú). Dùng chung `PagedResponse.of()` của Nam — **không** tự viết lại (§2b-4).

---

#### `MF03-TC-INT-010` — D4: mọi ngày/giờ trong JSON là ISO-8601

**Severity:** `HIGH`
**Feature Under Test:** Jackson config toàn cục + mọi DTO của MF03
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/web/DateFormatContractIT.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-018`
**Oracle Source:** **D4** — *Ngày → `"2026-07-29"` (`LocalDate`); Ngày giờ → `"2026-07-29T02:15:00Z"` (`Instant`)*; **"bỏ mọi `@JsonFormat("dd/MM/yyyy")`"**; D4 **ghi đè** phần định dạng của ADR-M11-5
**Traceability:** UC-147, UC-146, UC-084, UC-077

**Test Steps:**
1. `GET /students/HS-3A-001` → `dob` khớp `^\d{4}-\d{2}-\d{2}$`; **không** khớp `^\d{2}/\d{2}/\d{4}` (mock đang `dd/MM/yyyy` — cố ý đóng băng, §0).
2. `GET /allergy-declarations?studentId=HS-3A-001` → `declaredAt`, `verifiedAt` parse được bằng `Instant.parse(...)`.
3. `GET /health-history?studentId=HS-3A-001` → `at` là ISO date-time; timeline sắp theo `at DESC`.
4. *Đầu vào:* `POST /students` với `dob="12/03/2019"` → **400** + MSG-138 (không "khoan dung" nhận `dd/MM/yyyy`).
5. *Assert tài liệu-hoá:* grep source MF03 → **0** kết quả `@JsonFormat` (checklist D1–D7, Team Plan §2c-3).

**Expected Result (PASS):** 5/5.
**Expected Result (FAIL):** một endpoint còn `dd/MM/yyyy` ⇒ FE `lib/format.ts` format hai lần / parse lỗi, và lọc `dateFrom/dateTo` **im lặng trả rỗng** (cảnh báo D4).

**Current Status:** 🔴 Not written

---

#### `MF03-TC-INT-011` — D5 + D1: `id` do client gửi, và `id` công khai là mã nghiệp vụ

**Severity:** `HIGH`
**Feature Under Test:** `POST /api/v1/students`, `POST /api/v1/allergy-declarations`, `POST /api/v1/special-meals`
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/web/PublicIdContractIT.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-019`
**Oracle Source:** **D5** *(1) body có `id` → dùng làm `code`, kiểm trùng → **409**; (2) vắng → server sinh theo `code_sequences`; (3) response 201 **luôn** trả `id`* · **D1** *(`id` JSON + path param = mã nghiệp vụ; UUID chỉ là PK nội bộ)* · **MSG-012** (`STU-012`, 409)
**Traceability:** UC-025, UC-084

**Test Steps:**
1. `POST /students {"id":"HS-2A-011", …}` (chưa tồn tại) → **201**, response `id == "HS-2A-011"`.
2. `POST /students {"id":"HS-3A-001", …}` (đã tồn tại) → **409** `STU-012` + MSG-012 nguyên văn.
3. `POST /students` **không** có `id` → **201**, `id` do server sinh, khớp khuôn mã module, **không** UUID.
4. *Path param dùng mã nghiệp vụ:* `GET /students/HS-3A-001` → 200; `GET /students/<uuid nội bộ>` → **404** (UUID **không** phải định danh công khai — D1).
5. *Không rò UUID:* response **không** chứa UUID ở field `id`, **trừ** các FK đang được TDS §9.2 khai là UUID (`classroomId`, `parentId`, `proofFileId`). ⚠️ D1 nói *"khoá ngoại trong JSON dùng `code` của bảng đích"* ⇒ **lệch chưa chốt**: assert theo TDS hiện tại và **báo Mạnh/Minh** (gắn với **Q2**).
6. *Sinh mã song song:* 20 request `POST /students` đồng thời (không gửi `id`) → **20** mã khác nhau (`code_sequences` + `INSERT … RETURNING`, D1).

**Expected Result (PASS):** 6/6.
**Expected Result (FAIL):** server bỏ qua `id` client gửi rồi tự cấp mã khác ⇒ FE điều hướng ngay sau `onSuccess` tới mã sai → 404 (đúng lý do D5 tồn tại); mã trùng khi tạo song song ⇒ vỡ `UNIQUE`.

**Current Status:** 🔴 Not written

---

#### `MF03-TC-INT-012` — CF-M03-9: resource MỚI `student_health_records`

**Severity:** `MEDIUM`
**Feature Under Test:** `GET /api/v1/students/{id}/health-record` · `PUT /api/v1/students/{id}/health-record`
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/web/HealthRecordIT.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-020`
**Oracle Source:** **CF-M03-9** (resource MỚI so với mock; `HealthRecord` thiếu `studentId <<FK>>` trong `data_model.puml`) · TDS §5.2 CHECK `height_cm BETWEEN 50 AND 220`, `weight_kg BETWEEN 5 AND 150` · **ADR-M03-3** (`bmi` **không** lưu) · **MSG-029** · **MSG-138**
**Traceability:** UC-050, UC-094

**Test Steps (BVA trên CHECK constraint):**
1. `PUT /students/HS-3A-001/health-record {height:120.5, weight:24.0, bloodType:"O"}` (JWT `medical-staff`) → **200** MSG-029.
2. *Assert dẫn xuất:* response có `bmi` = `weight/(height/100)^2` nhưng `information_schema.columns` **không** có cột `bmi`.
3. *Biên:* `height=50`/`220` → OK; `49.9`/`220.1` → **400** MSG-138. `weight=5`/`150` OK; `4.9`/`150.1` → 400.
4. *Unique 1-1:* `student_id UNIQUE` ⇒ `PUT` lần 2 **cập nhật**, không tạo dòng thứ hai (`COUNT(*) = 1`).
5. *Ca chưa có bản ghi:* `GET /students/{id}/health-record` khi HS chưa có `student_health_records` → **200** + `null` body. **Chốt 02/08 (Huy)**: theo §2b-6 *"tập rỗng là 200, không phải 404"* — 404 ở đây sẽ lẫn với "học sinh không tồn tại" (`STU-404`/MSG-137), tức một mã hai nghĩa, trái **D7**. TDS §9.1 bổ sung dòng đặc tả tương ứng.

**Expected Result (PASS):** 1–4 đúng; 5 theo quyết định.
**Expected Result (FAIL):** thêm cột `bmi` ⇒ AP-AI-009; cho phép 2 dòng/HS ⇒ số liệu tăng trưởng phân mảnh.

**Current Status:** 🔴 Not written
**Implementation Note:** Entry criteria của TC này: Minh bổ sung resource vào `db.ts`/`mock-server.ts`; Mạnh thêm `+studentId : String <<FK>>` vào `data_model.puml` (CF-M03-9).

---

#### `MF03-TC-INT-013` — D2 + D7: envelope lỗi đồng nhất trên **mọi** error path

**Severity:** `HIGH`
**Feature Under Test:** `@RestControllerAdvice` dùng chung (`common/`) + `ErrorResponse`
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/web/ErrorEnvelopeIT.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-021`
**Oracle Source:** **D2** *(`{ "error": { code, message, details } }`; `details` là mảng, **không bao giờ `null`**)* · **D7** *(message **tiếng Anh**, nguyên văn `error_message.md`)* · TDS §10 (13 mã `STU-*` + cột HTTP)
**Traceability:** tất cả 15 UC

**Test Steps (`@ParameterizedTest` — quét **toàn bộ** bảng §10):**

| Mã | HTTP | MSG nguồn | Cách kích hoạt |
|---|---|---|---|
| `STU-004` | 401 | MSG-004 | gọi không JWT / token hết hạn |
| `STU-005` | 403 | MSG-005 | Parent chạm HS không liên kết |
| `STU-010` | 409 | **MSG-142** | lớp đầy theo `capacity` của chính lớp (`FX-013`) |
| `STU-012` | 409 | MSG-012 | `id` trùng khi `POST /students` |
| `STU-030` | 422 | MSG-111 | tạo HS không có PH |
| `STU-031` | 422 | MSG-112 | verify SEVERE thiếu tài liệu (`FX-006`) |
| `STU-032` | 422 | MSG-113 | request-info không lý do |
| `STU-066` | 422 | **MSG-144** | approve `Keto` → `NEEDS_INFO` (BR-220) |
| `STU-094` | 400 | MSG-115 | `PATCH /health-file` field lạ |
| `STU-172` | 405 | MSG-116 | `PUT /health-history/{id}` |
| `STU-193` | 409 | **—** | lớp thuộc năm học CLOSED — ⚠️ **thiếu MSG** (báo Huy); chỉ assert `code` + 409 |
| `STU-201` | 409 | MSG-110 | liên kết HS thứ 6 (`FX-003`) |
| `STU-404` | 404 | MSG-137 | `studentId` không tồn tại |
| `STU-500` | 500 | **MSG-143** | repo ném `DataAccessException` — **L19/Q5 chốt 02/08**: đã cấp MSG-143 *"System error while saving student data. Please try again."*; assert **đủ** `code` + 500 + message nguyên văn + envelope D2 |

Mỗi hàng assert: HTTP status đúng · `$.error.code` đúng · `$.error.message` **nguyên văn MSG** (trừ 2 hàng đã ghi chú) · `$.error.details` **tồn tại** (mảng, có thể rỗng) · **không** có `$.message` phẳng kiểu mock · message là **tiếng Anh** (D7 — assert không chứa ký tự có dấu tiếng Việt).

**Expected Result (PASS):** 14/14 hàng đúng shape; 12/14 khớp message nguyên văn; 2 hàng chờ quyết định (Q5 + `STU-193`).
**Expected Result (FAIL):** một endpoint trả `{message:"…"}` phẳng ⇒ FE đọc `data?.error?.message` ra `undefined` (interceptor D2 khoan dung hai shape là cho giai đoạn chuyển tiếp, backend **phải** dùng envelope); message tiếng Việt ⇒ vi phạm D7.

**Current Status:** 🔴 Not written

---

#### `MF03-TC-INT-014` — D1: phạm vi unique của mã nghiệp vụ (✅ **ĐÃ GỠ BLOCK 2026-08-02** — xem L10/Q2)

**Severity:** `MEDIUM`
**Feature Under Test:** `students.code` + `uq_student_school_code` + `students.registration_no`
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/web/StudentCodeUniquenessIT.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-019`
**Oracle Source:** **D1** *"§5.2 thêm `code VARCHAR(32) NOT NULL` + **`UNIQUE(school_id, code)`**"* · **tiền lệ đã code**: `classrooms.code` + `uq_class_ay_code` (`V3__mf02_school_class_academic_year.sql:106,118`) · TDS-MF03 v1.1 §5.2 · MSG-012
**Traceability:** UC-025, UC-010

**Test Steps:**
1. Tạo HS mã `HS-3A-001` ở **trường A** → **201**.
2. Tạo HS **cùng mã** `HS-3A-001` ở **trường B** (`FX-015`) → **201** (mã chỉ unique trong phạm vi trường).
3. Tạo HS mã `HS-3A-001` lần hai ở **trường A** → **409** `STU-012` + MSG-012 nguyên văn.
4. Tạo HS ở trường B với `registrationNo` đã dùng ở trường A → **409** `STU-012` (registration_no unique **toàn hệ thống**, UC-025 b6 — khác phạm vi với `code`, đây là điểm dễ nhầm nhất của TC này).
5. Assert qua `information_schema`: tồn tại cột `students.code`, **không** tồn tại `students.student_code`; tồn tại constraint `uq_student_school_code` trên `(school_id, code)`; `registration_no` có unique một cột.

**Expected Result:** 5/5 bước đúng như trên.
**Expected Result (FAIL):** nếu implement unique toàn cục cho `code` → bước 2 trả 409, trường thứ hai onboard là vỡ. Nếu implement per-school cho `registration_no` → bước 4 trả 201, trùng số đăng ký giữa các trường.

**Current Status:** 🔴 Not written — **đã gỡ BLOCKED**: Q2 chốt theo D1 + tiền lệ MF02 (xem CHANGELOG TestSpec 2026-08-02).

---

### 4.3 Security Test Cases — RBAC + phạm vi dữ liệu + PDPD

> ⚠️ **Nguyên tắc số 1 của MF03 (TDS §16 + §17.1 C2):** `@PreAuthorize` theo role là **chưa đủ**. Mọi endpoint có cột `Own`/`Class` phải kiểm **phạm vi dữ liệu ở service**; nếu không thì *"đúng role, sai học sinh"* vẫn lọt. Toàn bộ `userId/role/schoolId` đọc **từ JWT**, **không** tin body/query (AP-AI-006).
> Expected mặc định khi sai quyền/sai phạm vi: **403** `STU-005` + **MSG-005** *"Access denied: You do not have permission to access this resource."*

---

#### `MF03-TC-SEC-001` — Phạm vi dữ liệu của Parent (chỉ con mình)

**Severity:** `CRITICAL` · **OWASP:** `A01:2021 — Broken Access Control` · **CWE:** `CWE-639 — Authorization Bypass Through User-Controlled Key`
**Legal:** BR-173 (PDPD) — lộ hồ sơ sức khỏe trẻ em cho phụ huynh khác là sự cố dữ liệu cá nhân
**Feature Under Test:** `StudentService.getById()` · `AllergyDeclarationService.submit()` · `ParentPortalService.listChildrenHealth()`
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/security/ParentScopeIT.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-022`
**Oracle Source:** **BR-030** (quan hệ HS↔PH là cơ sở phạm vi) · TDS **§16** cột PARTNER/PARENT = `Own` · **MSG-005** · TDS §13 TC-E2E-001
**Traceability:** UC-050, UC-084, UC-104, UC-147

**Preconditions:** `FX-002` — Parent `U-0005` (Nguyễn Văn Bình) liên kết **HS-3A-001** + **HS-1B-007**; HS-3B-002 (Lý Gia Bảo) thuộc PH khác.

**Test Steps (Attack Simulation):**
1. `GET /api/v1/students/HS-3B-002` (JWT `parent` U-0005) → **403** `STU-005` + MSG-005.
2. `POST /api/v1/allergy-declarations {studentId:"HS-3B-002", …}` (JWT parent U-0005) → **403** `STU-005`; **0** dòng được ghi.
3. `GET /api/v1/parents/{U-0005}/students` → **200** với **đúng 2** HS (`HS-3A-001`, `HS-1B-007`).
4. `GET /api/v1/parents/{U-0013}/students` (JWT parent U-0005 — hỏi danh sách con của PH khác) → **403**.
5. `GET /api/v1/students` (JWT parent) → chỉ trả HS đã liên kết; `total == 2`, **không** rò tổng số HS toàn trường.
6. `GET /api/v1/health-history?studentId=HS-3B-002` (JWT parent U-0005) → **403**.
7. `GET /api/v1/health-documents/{doc của HS-3B-002}/content` (JWT parent U-0005) → **403** (ADR-M03-5).

**Expected Result (PASS = hệ thống an toàn):** 403 ở mọi truy cập ngoài phạm vi; danh sách bị cắt đúng theo `student_parents`.
**Expected Result (FAIL = lỗ hổng):** đọc được hồ sơ dị ứng/bệnh nền của con nhà khác — dữ liệu `Restricted`, phải báo DPO.

**Current Status:** 🔴 Not written

---

#### `MF03-TC-SEC-002` — Phạm vi Teacher: lớp chủ nhiệm, và **không** tải tài liệu y tế

**Severity:** `CRITICAL` · **OWASP:** `A01:2021` · **CWE:** `CWE-285 — Improper Authorization`
**Feature Under Test:** `ClassHealthRegistryService.listForHomeroom()` · `HealthDocumentController`
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/security/TeacherScopeIT.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-023`
**Oracle Source:** **BR-123** (Teacher cần **cảnh báo** để phục vụ an toàn) · TDS **§16** ghi chú *"Teacher xem được cảnh báo dị ứng nhưng KHÔNG tải được tài liệu y tế gốc — đủ để phục vụ an toàn mà không mở rộng phạm vi PII"* · **ADR-M03-5** · **MSG-005** · **D6** `AllergySeverity` (**L11**)
**Traceability:** UC-145

**Preconditions:** `FX-014` — Teacher `U-0004` chủ nhiệm lớp `3A`; `U-0009` chủ nhiệm `3B`. `FX-008`, `FX-016`.

**Test Steps:**
1. `GET /api/v1/students?class=3A` (JWT `teacher` U-0004) → **200**, mọi item thuộc lớp 3A.
2. HS-3A-001 (ALG-001/002 SEVERE đã VERIFIED) → item có `allergySeverity == "SEVERE"` (**D6**, không phải `"Severe"` — **L11**) ⇒ FE tô đỏ (BR-123).
3. `GET /api/v1/students?class=3B` (JWT teacher U-0004) → **403** `STU-005`.
4. `GET /api/v1/students/HS-3B-002` (JWT teacher U-0004) → **403**.
5. `GET /api/v1/health-documents/{doc-771}/content` (JWT teacher) → **403** `STU-005` — **kể cả** tài liệu của HS lớp mình.
6. `GET /api/v1/students/HS-3A-001/health-file` (JWT teacher) → **403** (§16: chỉ Medical Staff).
7. `GET /api/v1/students/HS-3A-001/allergy-alerts` (JWT teacher) → **200** (§16: Teacher ✅ Class).
8. *Không truyền lớp:* `GET /api/v1/students` (JWT teacher) → chỉ HS lớp 3A (không được trả toàn trường rồi để FE lọc).

**Expected Result (PASS):** 8/8 — Teacher thấy cảnh báo, không thấy hồ sơ y tế gốc, không thấy lớp khác.
**Expected Result (FAIL):** trả toàn trường rồi "để FE lọc" ⇒ dữ liệu `Restricted` đã ra khỏi server, filter phía client không phải kiểm soát truy cập.

**Current Status:** 🔴 Not written

---

#### `MF03-TC-SEC-003` — Cross-school isolation

**Severity:** `CRITICAL` · **OWASP:** `A01:2021` · **CWE:** `CWE-639`
**Feature Under Test:** mọi service của MF03 (bộ lọc `schoolId` từ JWT)
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/security/CrossSchoolIsolationIT.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-024`
**Oracle Source:** **BR-022** (phạm vi theo trường — kế thừa MF02) · TDS §16 (`School` = chỉ trong trường mình, `schoolId` trong JWT) · **MSG-005** · TDS §13 TC-E2E-003
**Traceability:** UC-147, UC-051, UC-104

**Preconditions:** `FX-015` — trường B `SCH-HN-0099` có `HS-9Z-001`.

**Test Steps:**
1. `GET /api/v1/students/HS-9Z-001` (JWT `medical-staff` trường A) → **403** `STU-005`.
2. `GET /api/v1/students` (JWT `school-admin` trường A) → **không** item nào thuộc trường B; `total` chỉ đếm trường A.
3. `POST /api/v1/students {classroomId: <lớp của trường B>}` (JWT school-admin trường A) → **403**.
4. *Tấn công tin body:* gửi `schoolId` của trường B trong body/query → **bị bỏ qua**, vẫn dùng `schoolId` trong JWT (AP-AI-006). Kết quả: 403 hoặc trả dữ liệu trường A, **tuyệt đối không** trả dữ liệu trường B.
5. `GET /api/v1/health-history?studentId=HS-9Z-001` (JWT trường A) → **403**.

**Expected Result (PASS):** 5/5, không đường nào đọc được dữ liệu trường khác.
**Expected Result (FAIL):** một endpoint quên `schoolId` predicate ⇒ rò dữ liệu đa-tenant — cùng nhóm rủi ro mà D1 nêu với `partnerId`.

**Current Status:** 🔴 Not written

---

#### `MF03-TC-SEC-004` — System Admin **không** có quyền nghiệp vụ; Supplier/Guest không quyền nào

**Severity:** `HIGH` · **OWASP:** `A01:2021` · **CWE:** `CWE-269 — Improper Privilege Management`
**Legal:** BR-173 / PDPD — least privilege trên dữ liệu `Restricted`
**Feature Under Test:** `@PreAuthorize` toàn bộ controller MF03
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/security/AdminNoBusinessAccessIT.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-025`
**Oracle Source:** TDS **§16** ghi chú: *"**System Admin KHÔNG có quyền nghiệp vụ trên hồ sơ học sinh** — dữ liệu Restricted (PII trẻ em + sức khỏe), quản trị nền tảng không cần đọc (BR-173/PDPD, least privilege)"* và *"**Supplier / Guest: không có bất kỳ quyền nào** trên MF03"* · **MSG-005** · **D6** `RoleKey`
**Traceability:** tất cả 15 UC

**Test Steps:**
1. JWT `system-admin` gọi **20 endpoint** của §16 → **403** `STU-005` ở **tất cả** (kể cả `GET /students`).
2. JWT `supplier` → **403** ở tất cả.
3. Không có JWT (Guest) → **401** `STU-004` (xem `MF03-TC-SEC-006`).
4. *Assert không có backdoor:* không endpoint nào của MF03 nhận header/param kiểu `impersonate`/`asUser`.

**Expected Result (PASS):** 0 endpoint mở cho `system-admin`/`supplier`.
**Expected Result (FAIL):** để `system-admin` "xem cho tiện" ⇒ mở rộng phạm vi PII trẻ em ngoài quy trình DPO — đúng điều §16 cấm.

**Current Status:** 🔴 Not written
**Implementation Note:** ca điều tra sự cố cần dữ liệu ⇒ đi qua quy trình có DPO duyệt, **không** qua API thường (§16).

---

#### `MF03-TC-SEC-005` — Ma trận đầy đủ 7 role × 20 endpoint (§16)

**Severity:** `CRITICAL` · **OWASP:** `A01:2021` · **CWE:** `CWE-284 — Improper Access Control`
**Feature Under Test:** toàn bộ controller MF03
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/security/AuthMatrixIT.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-025`, `COND-022`, `COND-023`
**Oracle Source:** **TDS §16 Authorization Matrix** (bảng 20 dòng × 7 cột — nguồn duy nhất) · **BR-007** (RBAC 7 role) · **D6** `RoleKey` (kebab-case) · **MSG-005**
**Traceability:** tất cả 15 UC

**Test Steps:** `@ParameterizedTest` sinh **140 ca** (20 endpoint × 7 role) từ bảng §16, mỗi ca assert:
- ô `✅` → **2xx** (kèm điều kiện phạm vi nếu ô ghi `Own`/`Class`/`School`);
- ô `❌` → **403** `STU-005` + MSG-005;
- ô `✅ Own` → 2xx với tài nguyên trong phạm vi **và** 403 với tài nguyên ngoài phạm vi (2 assertion/ô).

Bảng nguồn (rút gọn — bản đầy đủ ở TDS §16): `GET /students` · `POST /students` · `PATCH /students/{id}` · `POST|DELETE /students/{id}/parents` · `GET /students/template` · `POST /students/import/*` · `GET /parents/{id}/students` · `GET|POST|PATCH /allergy-declarations` · `POST /allergy-declarations/{id}/verify|request-info` · `GET|POST /chronic-conditions` · `POST /chronic-conditions/{id}/verify|request-info` · `GET|POST /special-meals` · `POST /special-meals/{id}/approve|reject` · `GET|PATCH /students/{id}/health-file` · `GET|PUT /students/{id}/health-record` · `GET /health-history` · `GET /students/{id}/allergy-alerts` · `GET /health-documents/{id}/content`.

**Expected Result (PASS):** 140/140 khớp bảng §16; **0** ô lệch.
**Expected Result (FAIL):** một ô `❌` trả 2xx ⇒ vượt quyền; một ô `✅` trả 403 ⇒ chặn oan (VD Service Caterers **phải** đọc được `special-meals` + `allergy-alerts` — đầu vào bắt buộc của BR-065/BR-082, chặn nhầm là **mất** kế hoạch suất riêng).

**Current Status:** 🔴 Not written
**Implementation Note:** bảng §16 đưa vào test dưới dạng **bảng dữ liệu** (CSV/`@CsvSource`), không hard-code rải rác — khi §16 đổi, sửa một chỗ.

---

#### `MF03-TC-SEC-006` — Không JWT / token hết hạn → 401

**Severity:** `HIGH` · **OWASP:** `A07:2021 — Identification and Authentication Failures` · **CWE:** `CWE-287`
**Feature Under Test:** Spring Security filter chain (JWT do MF01 phát hành)
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/security/UnauthenticatedIT.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-026`
**Oracle Source:** **BR-004 (MF01)** — phiên 30 phút · **MSG-004** *"Session expired. Please log in again."* · TDS §10 `STU-004` = HTTP **401**
**Traceability:** UC-147, UC-050, UC-051 (MSG-004 trong §1.3)

**Test Steps:**
1. `GET /api/v1/students` **không** header `Authorization` → **401** `STU-004` + MSG-004 (envelope D2).
2. Token hết hạn (`exp` quá khứ) → **401**.
3. Token chữ ký sai / bị sửa payload (`role` đổi thành `school-admin`) → **401**, **không** phải 200.
4. `GET /api/v1/health-documents/{id}/content` không JWT → **401** (tài liệu y tế **không** public — ADR-M03-5).
5. *Assert phân biệt 401 vs 403:* có JWT hợp lệ nhưng sai role → **403**, không phải 401.

**Expected Result (PASS):** 5/5.
**Expected Result (FAIL):** tài liệu y tế tải được không cần JWT ⇒ sự cố PDPD nghiêm trọng.

**Current Status:** 🔴 Not written

---

#### `MF03-TC-SEC-007` — PDPD (BR-173): không lộ PII qua log, URL, hay object storage

**Severity:** `CRITICAL` · **OWASP:** `A09:2021 — Security Logging and Monitoring Failures` · **CWE:** `CWE-532 — Insertion of Sensitive Information into Log File`
**Legal:** **BR-173 / Nghị định PDPD** — dữ liệu sức khỏe trẻ em
**Feature Under Test:** logging config · `HealthDocumentController` · `health_documents.storage_key`
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/security/PdpdLeakageIT.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-027`
**Oracle Source:** **BR-173** *"Dữ liệu sinh trắc học của trẻ em **không được thu thập**"* + TDS §17.1 **C7** *"CẤM log ở mức INFO: tên HS, tên allergen, nội dung khai báo, tên tệp y tế (chỉ log ID)"* · **ADR-M03-5** (storage key, không public URL) · TDS §13 TC-E2E-004, §14.2
**Traceability:** UC-084, UC-051, UC-094

**Test Steps (Attack/Leak Simulation):**
1. Bắt log (`ListAppender` trên root logger, level INFO) trong khi `POST /allergy-declarations` (SEVERE, có tài liệu).
2. *Assert log:* có `studentId`; **không** chứa `"Nguyễn Minh Khang"`, `"Tôm"`, tên tệp `".pdf"`, hay nội dung `justification`/`reviewNote` (§14.2 grep tương đương).
3. *Assert URL:* response của `POST /health-documents` **không** chứa URL public / presigned dài hạn; chỉ trả `id` + metadata; nội dung tải qua `GET /health-documents/{id}/content` có RBAC.
4. *Assert DB:* `health_documents.storage_key` **không** phải URL `http(s)://…` (ADR-M03-5).
5. *Assert audit:* mỗi lượt tải tài liệu ghi **1** dòng audit (định danh bằng ID, không bằng tên) — BR-172 tinh thần + PDPD.
6. *Assert sinh trắc học:* **không** bảng/cột nào của MF03 chứa `face`, `fingerprint`, `biometric`:
   ```sql
   SELECT table_name, column_name FROM information_schema.columns
   WHERE table_schema='public'
     AND (column_name ILIKE '%biometric%' OR column_name ILIKE '%face%' OR column_name ILIKE '%fingerprint%')
     AND table_name IN ('students','student_parents','allergy_declarations',
                        'chronic_condition_declarations','special_meal_requests',
                        'student_health_records','student_clinical_files',
                        'health_history_events','health_documents');   -- Expected: 0 rows
   ```
7. *Assert test data:* fixture không chứa dữ liệu HS thật (checklist thủ công + review Thảo/DPO).

**Expected Result (PASS = an toàn):** 7/7.
**Expected Result (FAIL = lỗ hổng):** tên HS + tên allergen nằm trong log tập trung ⇒ dữ liệu sức khỏe trẻ em rời khỏi vùng kiểm soát qua đường log; tài liệu y tế có URL public ⇒ ai có link đều xem được (AP-AI-014).

**Current Status:** 🔴 Not written

---

#### `MF03-TC-SEC-008` — ★ Gate nghiệp vụ enforce ở **server**: gọi API trực tiếp bỏ qua UI

**Severity:** `CRITICAL` · **OWASP:** `A04:2021 — Insecure Design` · **CWE:** `CWE-602 — Client-Side Enforcement of Server-Side Security`
**Feature Under Test:** `HealthReviewService.verifyAllergy()` · `SpecialMealRequestService.reject()/approve()` · `StudentHealthFileService`
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/security/ServerSideGateIT.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-028`, `COND-003`, `COND-006`
**Oracle Source:** **BR-031** (gate hiệu lực) · **BR-172** · **MSG-112** / **MSG-113** / **MSG-115** · Team Plan §2c-3b: *"Với module có GATE nghiệp vụ enforce phía server, PHẢI có test 'gọi API trực tiếp bỏ qua UI'… gate chỉ tồn tại ở client thì gọi API trực tiếp là lách được toàn bộ luật"*
**⚠️ Vì sao MF03 thuộc nhóm này:** ba gate của MF03 hiện **chỉ tồn tại ở client** trong mock — `docsValid()` khóa nút Approve khi thiếu tài liệu (TDS §9.3), `rejectReason.trim() === ""` khóa nút Từ chối (TDS §9.3), và dialog FE quyết định field nào gửi lên `/health-file`. Nếu backend không tự kiểm, một lệnh `curl` là lách hết.
**Traceability:** UC-077, UC-078, UC-094

**Test Steps (Attack Simulation — bỏ qua hoàn toàn UI, gọi thẳng API bằng JWT hợp lệ của Medical Staff):**
1. `POST /allergy-declarations/{ALG-901}/verify` (SEVERE, `proofFileId=null` — `FX-006`) → **422** `STU-031` + **MSG-112**; `status` **vẫn** `PENDING`. *(UI khóa nút; API phải khóa thật.)*
2. `POST /allergy-declarations/{id}/request-info {}` (không `reason`) → **422** `STU-032` + **MSG-113**.
3. `POST /special-meals/{id}/reject {reason:"   "}` → **422** `STU-032`.
4. `POST /special-meals/{SMR-2026-902}/approve` (`dietaryType="Keto"`) → **422** `STU-066` + MSG-144, `status` → `NEEDS_INFO` (BR-220 — L14/Q1 chốt 02/08).
5. `PATCH /students/{id}/health-file {healthStatus:"VERIFIED"}` → **400** `STU-094` + **MSG-115**; `healthStatus` dẫn xuất **không đổi**.
6. `POST /allergy-declarations` với `status:"VERIFIED"` ngay từ lúc tạo (bỏ qua bước duyệt) → khai báo được lưu ở **`PENDING`** (server **bỏ qua** `status` client gửi) **hoặc** 400; **tuyệt đối không** lưu `VERIFIED`.
7. `PUT /health-history/{id}` (Medical Staff hợp lệ) → **405** `STU-172`.
8. *Assert DB sau cả 7 ca:* `SELECT * FROM allergy_declarations WHERE severity='SEVERE' AND status='VERIFIED' AND proof_file_id IS NULL` → **0 rows** (§14.1 câu 2 + luật `XAC_MINH_MUC_NANG_KHONG_CO_MINH_CHUNG`).

**Expected Result (PASS = an toàn):** 8/8 — mọi gate chặn ở server, DB giữ nguyên bất biến.
**Expected Result (FAIL = lỗ hổng):** ca 1 lọt ⇒ dị ứng **mức Nặng** được đánh dấu "đã xác minh" mà **không có bằng chứng y tế**, rồi tự động chảy xuống MF05/MF09 dưới dạng cảnh báo hợp lệ — sai lệch nằm ở tầng dữ liệu, không có lỗi nào được ném, và chỉ lộ ra khi có sự cố thật.

**Current Status:** 🔴 Not written

---

#### `MF03-TC-SEC-009` — IDOR: không tin `studentId`/`parentId` từ body hay query

**Severity:** `CRITICAL` · **OWASP:** `A01:2021` · **CWE:** `CWE-639`
**Feature Under Test:** mọi endpoint nhận `studentId`/`parentId`/`schoolId` trong payload
**Test File:** `04_Development/Backend/src/test/java/com/sfms/mf03/security/IdorIT.java`
**TDD Phase:** 🔴 RED · **Condition Ref:** `COND-022`
**Oracle Source:** TDS §17.1 **C2** *"`userId/role/schoolId` đọc **từ JWT**, không tin body/query"* · TDS §17.4 **AP-AI-006 Trust FE identity** · **MSG-005**
**Traceability:** UC-084, UC-093, UC-050

**Test Steps (Attack Simulation):**
1. `POST /allergy-declarations {studentId:"HS-3B-002", declaredByUserId:"<U-0013>"}` (JWT parent U-0005) → **403**; `declared_by_user_id` **không** được lấy từ body mà từ JWT.
2. `POST /special-meals {studentId:"HS-3B-002"}` (JWT parent U-0005) → **403**.
3. `PATCH /allergy-declarations/{ALG-006}` (khai báo của HS lớp khác, JWT parent U-0005) → **403**.
4. `POST /students {schoolId:"<trường B>"}` (JWT school-admin trường A) → `schoolId` trong body **bị bỏ qua**; HS được tạo ở **trường A** (hoặc 400 nếu DTO không khai field này).
5. `POST /allergy-declarations/{id}/verify {verifiedByUserId:"<user khác>"}` (JWT medical-staff) → `verified_by_user_id` = **user trong JWT**, không phải giá trị body.
6. `GET /health-history?studentId=HS-3B-002` (JWT parent U-0005) → **403**.
7. *Ca UC-093 hợp lệ:* Parent sửa khai báo `NEEDS_INFO` **của con mình** → 200 (không chặn oan).

**Expected Result (PASS = an toàn):** 6 ca tấn công bị chặn/bỏ qua; ca hợp lệ vẫn chạy.
**Expected Result (FAIL = lỗ hổng):** ghi khai báo dị ứng **cho học sinh khác** ⇒ vừa là rò dữ liệu, vừa là **đầu độc dữ liệu an toàn thực phẩm** (cảnh báo sai/thiếu ở điểm phát suất).

**Current Status:** 🔴 Not written

---

### 4.4 Contract Test Cases — OpenAPI (D8)

> ✅ **Đã gỡ BLOCKED-D8 ngày 2026-08-02** (xem **L16 / Q4**). Lý do gỡ: D8 **đã hiện diện trong code thật** — `common/config/OpenApiConfig.java` tồn tại và mọi controller MF02 đã mang `@Tag` + `@Operation` + `@ApiResponse(content = @Content(schema = @Schema(implementation = ErrorResponse.class)))`. Câu hỏi "Nam có approve D8 không" đã được trả lời bằng thực hành, không cần chờ chữ ký để viết test.
> **Còn nợ:** TDS-MF03 §9.2 vẫn chưa liệt kê annotation cho từng endpoint MF03 (grep `@Operation|@ApiResponse|@Tag(` trên `07_implement/MF03*` ra **0**) ⇒ Huy bổ sung để §10 ↔ `@ApiResponse` đối chiếu 1-1 (S3). Việc này **không chặn** viết test: test đọc spec runtime, không đọc TDS.
>
> Lớp Contract là **cổng máy kiểm** thay phần lớn checklist tay: 4 assertion dưới bắt lệch **D1/D2/D4/D6** tự động, chặn lệch quay lại qua file type FE sinh bởi `openapi-typescript` (`review_fix_plan.md` T3.4).
> 🆕 **`MF03-TC-CON-001` mở rộng theo L21:** ngoài field tên `id`, phải assert **mọi field khóa ngoại** (`classroomId`, `parentId`, `schoolId`, `studentId`, `proofFileId`) đều **không** có `format: uuid` — vì D1 quy định FK trong JSON là `code` của bảng đích, và bản TDS v1.0 từng trả UUID ở đây.
> **Hạ tầng chung:** `@SpringBootTest` khởi động thật, `GET /v3/api-docs` (phải `permitAll`), parse bằng `swagger-parser`, assert trên cây JSON.

---

#### `MF03-TC-CON-001` — D1/S6: không định danh công khai nào là UUID

**Severity:** `HIGH` · **TDD Phase:** 🔴 RED · **Condition Ref:** `COND-029`
**Feature Under Test:** `/v3/api-docs` — schema của mọi DTO MF03
**Test File:** `src/test/java/com/sfms/common/openapi/OpenApiD1ContractIT.java`
**Oracle Source:** **D1** (`id` công khai = mã nghiệp vụ) · **D8 S6** *"`example` của `id` là **mã nghiệp vụ**; không có UUID trong bất kỳ `example`/`@Schema` công khai nào"* · Team Plan §2c-3b assertion (a)

**Test Steps:**
1. Tải `/v3/api-docs`; duyệt mọi `components.schemas.*.properties`.
2. **Assert (a):** không property tên `id` nào có `format: "uuid"`.
3. **Assert (b):** mọi `example` của property `id` **không** khớp regex UUID; với MF03 phải khớp khuôn mã nghiệp vụ (`HS-…`, `ALG-…`, `SMR-…`, `HH-…`, `IMP-…`).
4. **Assert (c):** path param của mọi endpoint MF03 là `{code}`/mã nghiệp vụ, `example` không phải UUID.

**Expected Result (PASS):** 0 vi phạm.
**Expected Result (FAIL):** spec khai `id: uuid` ⇒ `openapi-typescript` sinh type FE với `id: string /* uuid */` và ví dụ UUID ⇒ lệch D1 **quay lại qua file sinh tự động** — khó thấy hơn lần đầu.
**Current Status:** 🔴 Not written

---

#### `MF03-TC-CON-002` — D2/S3: mọi 4xx/5xx trỏ `ErrorResponse` dùng chung

**Severity:** `HIGH` · **TDD Phase:** 🔴 RED · **Condition Ref:** `COND-029`, `COND-021`
**Test File:** `src/test/java/com/sfms/common/openapi/OpenApiD2ContractIT.java`
**Oracle Source:** **D2** (envelope lỗi) · **D8 S3** *"Mỗi mã lỗi ở §10 của TDS có một `@ApiResponse` tương ứng, `content` trỏ `ErrorResponse` dùng chung"* · TDS §10 (13 mã + cột HTTP)

**Test Steps:**
1. Với mọi operation thuộc `@Tag("MF03 — Student & Parent Management")`: mọi response có mã **4xx/5xx** phải `$ref` tới `#/components/schemas/ErrorResponse`.
2. `ErrorResponse` khai đúng 3 field `code`/`message`/`details` (D2), `details` là mảng.
3. **Đối chiếu ngược §10:** mỗi mã trong bảng §10 (`STU-004…STU-500`) có **ít nhất một** `@ApiResponse` với đúng HTTP status đã khai.
4. **Assert L15:** spec MF03 **không** khai MSG-064 ở đâu (CF-M03-12).

**Expected Result (PASS):** 100% response lỗi trỏ `ErrorResponse`; 14/14 mã §10 có mặt.
**Expected Result (FAIL):** một endpoint khai schema lỗi riêng ⇒ FE sinh 2 kiểu lỗi khác nhau, quay về đúng bài toán 76 mismatch.
**Current Status:** 🔴 Not written

---

#### `MF03-TC-CON-003` — D6/S5: enum khai bằng kiểu thật, khớp bảng 8 enum

**Severity:** `CRITICAL` · **TDD Phase:** 🔴 RED · **Condition Ref:** `COND-029`, `COND-007`
**Test File:** `src/test/java/com/sfms/common/openapi/OpenApiD6ContractIT.java`
**Oracle Source:** **D6** bảng 8 enum · **D8 S5** *"Enum khai bằng **kiểu enum thật** ở `common/enums/` — **KHÔNG** `@Schema(allowableValues = "…")` viết tay"* · Team Plan §2c-3b assertion (c)

**Test Steps:**
1. `AllergySeverity` trong spec = **đúng** `["MILD","MODERATE","SEVERE"]` — không thừa, không thiếu, không biến thể hoa/thường.
2. `HealthHistoryEventType` = `["KHAI_BAO","XAC_MINH","GHI_CHU_LAM_SANG","SU_CO","CAP_NHAT_THUOC"]` (**L17**).
3. `RoleKey` xuất hiện trong spec (nếu có) = 7 giá trị kebab-case của D6.
4. Mọi property kiểu enum trỏ `$ref` tới schema enum dùng chung, **không** khai `enum: [...]` inline lặp lại ở từng DTO.
5. **Assert S5:** grep source MF03 → **0** kết quả `@Schema(allowableValues`.
6. `DeclarationStatus` (`PENDING/VERIFIED/NEEDS_INFO`) và `SpecialMealRequest.status` (`PENDING/APPROVED/REJECTED/NEEDS_INFO`) khớp CHECK constraint §5.2. ⚠️ Hai enum này **không** nằm trong bảng 8 enum của D6 (là enum riêng module) — assert theo §5.2, và **báo Nam** cân nhắc đưa vào `common/enums/` nếu MF05/MF09 cũng dùng.

**Expected Result (PASS):** 6/6.
**Expected Result (FAIL):** spec còn `"Severe"` hoặc `"NANG"` ⇒ type FE sinh sai ⇒ so sánh ở MF09 luôn false ⇒ **mất cảnh báo dị ứng nặng** mà không có exception nào (lý do D6 xếp đây là blocker).
**Current Status:** 🔴 Not written

---

#### `MF03-TC-CON-004` — D4/S4: mọi field ngày có `format: date | date-time`

**Severity:** `HIGH` · **TDD Phase:** 🔴 RED · **Condition Ref:** `COND-029`, `COND-018`
**Test File:** `src/test/java/com/sfms/common/openapi/OpenApiD4ContractIT.java`
**Oracle Source:** **D4** · **D8 S4** *"Field ngày khai `format = "date"/"date-time"` + `example` viết dạng ISO"* · Team Plan §2c-3b assertion (d)

**Test Steps:**
1. Mọi property tên `dob`, `*At` (`declaredAt`, `verifiedAt`, `requestedAt`, `reviewedAt`, `linkedAt`, `uploadedAt`, `updatedAt`, `at`) có `type: string` + `format: "date"` hoặc `"date-time"`.
2. `example` của các field đó parse được bằng `LocalDate.parse` / `OffsetDateTime.parse`; **không** khớp `^\d{2}/\d{2}/\d{4}`.
3. `dob` là `format: "date"` (không phải `date-time`); `declaredAt`/`verifiedAt` là `date-time`.

**Expected Result (PASS):** 3/3, 0 field ngày khai `type: string` trần.
**Expected Result (FAIL):** field ngày khai string trần ⇒ type FE là `string` ⇒ mất kiểm tra định dạng, `dd/MM/yyyy` lọt lại qua đường payload.
**Current Status:** 🔴 Not written

---

#### `MF03-TC-CON-005` — S1/S2/S7/S8: `@Tag`, truy vết UC+BR, `PagedResponse`, bearer JWT

**Severity:** `MEDIUM` · **TDD Phase:** 🔴 RED · **Condition Ref:** `COND-029`
**Test File:** `src/test/java/com/sfms/common/openapi/OpenApiMetaContractIT.java`
**Oracle Source:** **D8 S1** (`@Tag(name = "MFxx — <Tên module>")`, tên module theo Team Plan §2c-0 = **"Student & Parent Management"**) · **S2** (`description` **PHẢI chứa UC ID + BR ID**) · **S7** (mọi `GET` danh sách khai `PagedResponse<T>`) · **S8** (`@SecurityScheme` bearer JWT toàn cục; endpoint public ghi `@SecurityRequirements` rỗng)

**Test Steps:**
1. **S1:** tồn tại đúng tag `"MF03 — Student & Parent Management"`; mọi operation của MF03 gắn tag này.
2. **S2:** mọi operation có `description` khớp regex `UC-\d{3}` **và** `BR-\d{3}` — **và** mã trích dẫn phải **thuộc danh sách 15 UC / 7 BR chính** của §1.3 (chống bịa mã ngay trong runtime doc). Đây là điểm D8 giữ truy vết **sống** ngoài file `.md`.
3. **S7:** `GET /students`, `GET /allergy-declarations`, `GET /special-meals`, `GET /chronic-conditions`, `GET /health-history` khai response 200 là `PagedResponse<T>` với đủ 5 field (§2b-1).
4. **S8:** có đúng **một** `securityScheme` `bearerAuth` (HTTP/bearer/JWT) khai toàn cục; MF03 **không** có endpoint public ⇒ **0** operation ghi `security: []`.
5. **Bằng chứng nghiệm thu (D8 §"Việc phải làm"):** lưu `/v3/api-docs` thành artifact kèm tài liệu nộp (bước §11 Deployment của TDS).

**Expected Result (PASS):** 5/5.
**Expected Result (FAIL):** thiếu UC/BR trong `description` ⇒ mất truy vết ở tầng runtime; `GET` danh sách khai schema riêng ⇒ FE sinh type phân trang không đồng nhất giữa 13 module.
**Current Status:** 🔴 Not written

---

## 5. Red-Green-Refactor Tracker

| TC ID | Test File | Lớp | 🔴 RED confirmed | 🟢 GREEN (commit) | 🔵 REFACTOR note |
|-------|-----------|-----|------------------|-------------------|------------------|
| `MF03-TC-001` | `service/ParentLinkServiceTest.java` | Unit | `[ ]` | | |
| `MF03-TC-002` | `service/StudentServiceTest.java` | Unit | `[ ]` | | |
| `MF03-TC-003` | `service/HealthReviewServiceTest.java` | Unit | `[ ]` | | |
| `MF03-TC-004` | `mapper/HealthStatusCalculatorTest.java` | Unit | `[ ]` | | tách `HealthStatusCalculator` khỏi mapper |
| `MF03-TC-005` | `service/SpecialMealRequestServiceTest.java` | Unit | `[ ]` | | |
| `MF03-TC-006` | `service/HealthReviewServiceTest.java` | Unit | `[ ]` | | |
| `MF03-TC-007` | `dto/AllergySeverityBindingTest.java` | Unit | `[ ]` | | |
| `MF03-TC-008` | `mapper/StudentMapperTest.java` | Unit | `[ ]` | | |
| `MF03-TC-009` | `dto/UpdateHealthFileRequestTest.java` | Unit | `[ ]` | | |
| `MF03-TC-INT-001` | `web/StudentCreateIT.java` | Integration | `[ ]` | | |
| `MF03-TC-INT-002` | `web/StudentCapacityIT.java` | Integration | `[ ]` | | |
| `MF03-TC-INT-003` ★ | `web/AllergyVerifyAlertIT.java` | Integration | `[ ]` | | **bắt buộc theo §17.2 TASK BLOCK** |
| `MF03-TC-INT-004` | `web/AllergyDeclarationIT.java` | Integration | `[ ]` | | |
| `MF03-TC-INT-005` | `web/HealthHistoryAppendOnlyIT.java` | Integration | `[ ]` (pin hành vi **L18**) | | **bắt buộc theo §17.2 TASK BLOCK** |
| `MF03-TC-INT-006` | `web/StudentClassChangeIT.java` | Integration | `[ ]` | | |
| `MF03-TC-INT-007` | `web/StudentImportIT.java` | Integration | `[ ]` | | |
| `MF03-TC-INT-008` | `web/HealthFileIT.java` | Integration | `[ ]` | | |
| `MF03-TC-INT-009` | `web/StudentListContractIT.java` | Integration | `[ ]` | | dùng chung `PagedResponse` của Nam |
| `MF03-TC-INT-010` | `web/DateFormatContractIT.java` | Integration | `[ ]` | | |
| `MF03-TC-INT-011` | `web/PublicIdContractIT.java` | Integration | `[ ]` | | |
| `MF03-TC-INT-012` | `web/HealthRecordIT.java` | Integration | `[ ]` | | chờ Minh/Mạnh sync (CF-M03-9) |
| `MF03-TC-INT-013` | `web/ErrorEnvelopeIT.java` | Integration | `[ ]` | | |
| `MF03-TC-INT-014` | `web/StudentCodeUniquenessIT.java` | Integration | `[ ]` | | |
| `MF03-TC-SEC-001` | `security/ParentScopeIT.java` | Security | `[ ]` | | |
| `MF03-TC-SEC-002` | `security/TeacherScopeIT.java` | Security | `[ ]` | | |
| `MF03-TC-SEC-003` | `security/CrossSchoolIsolationIT.java` | Security | `[ ]` | | |
| `MF03-TC-SEC-004` | `security/AdminNoBusinessAccessIT.java` | Security | `[ ]` | | |
| `MF03-TC-SEC-005` | `security/AuthMatrixIT.java` | Security | `[ ]` | | 140 ca từ bảng §16 dạng `@CsvSource` |
| `MF03-TC-SEC-006` | `security/UnauthenticatedIT.java` | Security | `[ ]` | | |
| `MF03-TC-SEC-007` | `security/PdpdLeakageIT.java` | Security | `[ ]` | | |
| `MF03-TC-SEC-008` ★ | `security/ServerSideGateIT.java` | Security | `[ ]` | | gate "bỏ qua UI" — §2c-3b bắt buộc |
| `MF03-TC-SEC-009` | `security/IdorIT.java` | Security | `[ ]` | | |
| `MF03-TC-CON-001` | `common/openapi/OpenApiD1ContractIT.java` | Contract | `[ ]` | | |
| `MF03-TC-CON-002` | `common/openapi/OpenApiD2ContractIT.java` | Contract | `[ ]` | | |
| `MF03-TC-CON-003` | `common/openapi/OpenApiD6ContractIT.java` | Contract | `[ ]` | | |
| `MF03-TC-CON-004` | `common/openapi/OpenApiD4ContractIT.java` | Contract | `[ ]` | | |
| `MF03-TC-CON-005` | `common/openapi/OpenApiMetaContractIT.java` | Contract | `[ ]` | | |

> Đường dẫn đầy đủ: `04_Development/Backend/src/test/java/com/sfms/mf03/…` (riêng lớp Contract đặt ở `com/sfms/common/openapi/` vì kiểm spec toàn hệ thống — Nam sở hữu, MF03 đóng góp assertion của mình).

### 5.1 Red Gate Protocol (CASE 2.0 — GATE-2)

> ⭐ **Gate quan trọng nhất.** Trước khi implement, chạy toàn bộ suite với stub rỗng/throw. **Mọi test PHẢI FAIL.** Test nào PASS ngay → **AP-AI-002 (Green-from-Birth)** → reject và viết lại.

**Stub cho Red Phase (Java — thay ví dụ TypeScript của template):**

```java
// Red Phase stub — PHẢI throw
@Service
public class HealthReviewServiceImpl implements HealthReviewService {
  @Override public AllergyDeclarationResponse verifyAllergy(String code, AuthUser actor) {
    throw new UnsupportedOperationException("Not implemented — Red Phase stub");
  }
  @Override public AllergyDeclarationResponse requestInfo(String code, String reason, AuthUser actor) {
    throw new UnsupportedOperationException("Not implemented — Red Phase stub");
  }
}
```

**Red Gate Verification (điền khi chạy):**

| TC ID | Stub Result | Expected | Actual | Root Cause (nếu PASS bất thường) |
|-------|-------------|----------|--------|----------------------------------|
| `MF03-TC-001` | `throw` | 🔴 FAIL | ☐ FAIL ☐ PASS | ☐ Tautology ☐ Shared state ☐ Hallucinated import |
| `MF03-TC-003` | `throw` | 🔴 FAIL | ☐ FAIL ☐ PASS | |
| `MF03-TC-004` | `throw` | 🔴 FAIL | ☐ FAIL ☐ PASS | ⚠️ dễ PASS giả nếu assert `healthStatus` mặc định `VERIFIED` mà stub trả `null` → kiểm kỹ ca 1 |
| `MF03-TC-INT-003` ★ | controller stub 501 | 🔴 FAIL | ☐ FAIL ☐ PASS | ⚠️ bước 1 assert `alerts == []` **có thể PASS giả** với stub trả rỗng → **bắt buộc** chạy cả bước 3 trong cùng test, không tách file |
| `MF03-TC-INT-005` | RULE/REVOKE **chưa** tạo | 🔴 FAIL | ☐ FAIL ☐ PASS | ⚠️ **PASS giả nếu container chạy bằng superuser** hoặc migration đã có sẵn RULE → xác nhận datasource dùng role `sfms_app` (`FX-022`) |
| `MF03-TC-SEC-005` | `@PreAuthorize` chưa gắn | 🔴 FAIL | ☐ FAIL ☐ PASS | ⚠️ nếu Spring Security mặc định `denyAll` thì **mọi** ô kể cả `✅` đều 403 ⇒ FAIL đúng hướng, nhưng phải FAIL ở ô `✅`, không phải chỉ ở ô `❌` |
| `MF03-TC-SEC-008` ★ | service stub throw | 🔴 FAIL | ☐ FAIL ☐ PASS | ⚠️ stub throw → 500, test assert 422 ⇒ FAIL đúng. Nếu PASS ⇒ assertion chỉ kiểm "không 2xx" → quá lỏng, viết lại theo mã lỗi cụ thể |
| *(29 TC còn lại)* | `throw` / 501 | 🔴 FAIL | ☐ | |

**Red Gate Evidence:**
- Stub commit hash: `___`
- Tất cả FAIL? ☐ Yes → **GATE-2 PASS** (T2→T3) → tiếp tục implement
- Log: `04_testing/evidence/mf03-red-gate.log`

> **Nếu bất kỳ test PASS ở Red Phase:** dừng, xác định root cause ở bảng trên, viết lại TC từ spec với Props Isolation (§4.0). **Ba TC gạch ★/⚠️ ở trên là nơi PASS-giả dễ xảy ra nhất của MF03** — kiểm tay từng cái.

---

## 6. Entry / Exit Criteria

### Entry Criteria (điều kiện bắt đầu viết test)

- [x] `TDS-MF03_Backend.md` **Approved** 2026-08-02 (v1.1).
- [x] **12 mâu thuẫn CF-M03-1…12** (TDS §2.3) đã có hướng xử lý ghi rõ (+ **CF-M03-13** mới, chốt 02/08); **5/5 câu hỏi Q1–Q5 đã trả lời** — không còn TC nào BLOCKED.
- [x] ~~Nam đã dựng nền chung~~ — **✅ XONG 31/07**, đã verify trong `04_Development/Backend/src/main/java/com/sfms/common/`: `dto/PagedResponse.java` · `error/GlobalExceptionHandler.java` + `dto/ErrorResponse.java` (D2) · `enums/` (D6 — có đủ `AllergySeverity`, `HealthHistoryEventType`) · `code/CodeGenerator.java` + bảng `code_sequences` (D1/D5) · `config/OpenApiConfig.java` (D8) · `domain/BaseEntity.java` (D9) · `storage/StorageService.java` (D10) · `reference/ReferenceResolver.java` (cross-module) · `query/ListQuery.java` + `SpecSupport.java`.
- [x] ~~MF01 JWT filter~~ — **✅ XONG**: `iam/security/` phát `AuthUser{userId, role, schoolId}`, dùng qua `@CurrentUser` (xem `mf02/web/AcademicYearController.java`); role `Parent` seed ở `V2__seed_roles_permissions_policy.sql`.
- [ ] **`ParentAccountPort` chưa tồn tại** (L22) — **Huy tự tạo**, không chờ ai. Logic đã có sẵn ở `iam/service/UserService.create()` (kiểm email trùng BR-012 → `IAM-006`/MSG-006 · `status=PENDING_VERIFICATION` + `emailVerified=false` BR-010 · `activationService.issueAndSend()` token 48h + email BR-011 · sinh `code` qua `CodeGenerator` · `auditService.success(...)`). Việc cần làm: khai `interface ParentAccountPort` ở `iam/port/` + một `@Component` adapter gọi `UserService.create(new CreateUserRequest(..., RoleKey.PARENT, ...))`. MF03 inject **interface**, **không** import `UserService`/`UserAccountRepository` (ADR-M02-5). Báo Nam vì file nằm trong `iam/`.
- [x] ~~MF02 cung cấp `ClassroomCapacityPort`~~ — **BỎ port này** (L22). `classrooms` + `capacity` (BR-025) đã có ở `V3`; `read_only` (BR-193) đã có. MF03 đọc qua `ReferenceResolver` + `COUNT` thay vì port.
- [ ] 🆕 Bổ sung vào `common/reference/ReferenceResolver` 2 cặp method `classroomCode/classroomIdByCode`, `userCode/userIdByCode` (L21 — cần cho `code` trong JSON). **Huy tự viết**, Mạnh review — bổ sung thuần, cùng khuôn với `schoolCode`/`schoolIdByCode` đã có (`iam/service/UserService` đang gọi `refs.schoolIdByCode(...)`). Báo nhóm trước khi đụng vì file dùng chung dễ merge conflict.
- [ ] Flyway **`V4__mf03_student_parent.sql`** (MF03 tự viết, L23): 9 bảng + CHECK `ck_severe_needs_proof` + TRIGGER `check_parent_link_limit` + RULE `hh_no_update`/`hh_no_delete` + `REVOKE`. **KHÔNG sửa `V1`/`V3`** (checksum mismatch trên DB của Nam/Mạnh).
- [ ] Seed **`V5__seed_mf03.sql`** đã chuyển đổi **D4 (ngày ISO)** + **D6 (enum SCREAMING_SNAKE)** và bổ sung `dob` (**L12**); role DB `sfms_app` tồn tại (`FX-022`).
- [ ] Chạy được `mvn -f 04_Development/Backend/pom.xml verify` trên nhánh `dev` **trước khi** thêm code MF03 (xác nhận nền xanh, tránh đổ lỗi nhầm cho MF03).
- [ ] Fixtures `FX-001…FX-022` (§3 TDS-05) đã chuẩn bị, **toàn bộ SYNTHETIC** (BR-173).
- [ ] Testcontainers chạy được trên máy dev + CI (Docker khả dụng).

### Exit Criteria (DoD — điều kiện kết thúc)

- [ ] `mvn -f 04_Development/Backend/pom.xml test` — **toàn bộ Unit xanh**, **0 `@Disabled`** (Q1–Q5 đã chốt hết ⇒ không còn lý do hợp lệ để disable; gặp `@Disabled` nào là dấu hiệu bỏ sót, không phải BLOCKED).
- [ ] `mvn verify` — **toàn bộ Integration + Security xanh** (Testcontainers).
- [ ] **≥1 integration test happy + error path** cho MF03 (yêu cầu tối thiểu của Team Plan §7 với module **P1**) — thực tế đạt 13 integration + 9 security.
- [ ] **Hai test bắt buộc theo TDS §17.2 TASK BLOCK đều xanh:** `MF03-TC-INT-003` (xác minh → cảnh báo BR-123) và `MF03-TC-INT-005` (append-only BR-172).
- [ ] Coverage ≥ **80% line** cho package `com.sfms.mf03.service` và `com.sfms.mf03.web`.
- [ ] **7/7 truy vấn đối soát DB** của TDS §14.1 trả đúng kết quả kỳ vọng sau khi chạy full suite.
- [ ] **Oracle Source:** 100% TC có dòng `Oracle Source` trỏ BR/UC/MSG/D-rule **có thật**; **0** expected value đến từ giả định. TC không có oracle ⇒ đánh dấu BLOCKED, **không** tự bịa (hiện **0 TC** ở trạng thái này — `MF03-TC-005` đã được cấp BR-220/MSG-144 ngày 02/08).
- [ ] **Red Gate (§5.1)** — tất cả test FAIL với stub trước khi implement; log lưu kèm.
- [ ] **Props Isolation** — không shared mutable state: mọi TC dựng dữ liệu trong `@BeforeEach`/factory (§4.0); grep `static` field mang state trong test → 0.
- [ ] **Contract Existence** — mọi import trong test resolve: `mvn -q test-compile` không lỗi "cannot find symbol".
- [ ] **PDPD (BR-173):** `MF03-TC-SEC-007` xanh; xác nhận fixture không chứa dữ liệu HS thật (Thảo + DPO ký).
- [ ] Lớp **Contract (D8)**: 5 TC `MF03-TC-CON-*` xanh (đã gỡ BLOCKED 02/08 — D8 hiện diện trong code MF01/MF02, **L16**). Riêng `MF03-TC-CON-003` phải kiểm **MSG-114 không còn xuất hiện** ở bất kỳ endpoint MF03 nào (v1.9 đã ngừng dùng). `MF03-TC-CON-001` phải bao cả khóa ngoại: không field `*Id` nào có `format: uuid` (**L21**).
- [ ] Thảo (Lead Test) xác nhận **coverage happy + error path** đủ theo quy trình BƯỚC 3 của Team Plan §4.

### Suspension Criteria (tạm dừng)

- `ParentAccountPort` chưa được tạo ⇒ tạm dừng `MF03-TC-INT-001` (không stub bừa rồi coi là xanh). Đây **không phải chờ người khác** — Huy tự viết adapter được, xem Entry Criteria. `ClassroomCapacityPort` không còn là điều kiện — đã bỏ (L22).
- `V4` chưa có RULE/REVOKE ⇒ `MF03-TC-INT-005` **không** được đánh dấu xanh (sẽ là xanh giả).
- 🆕 `mvn verify` trên `dev` **chưa xanh trước khi** MF03 thêm code ⇒ dừng, sửa nền trước; nếu không sẽ không phân biệt được lỗi MF03 với lỗi có sẵn.
- Phát hiện mâu thuẫn oracle mới (kiểu L14) ⇒ dừng TC liên quan, ghi vào §2, báo Huy — **không** tự chọn bên.
- Docker/Testcontainers không chạy được trên CI ⇒ dừng toàn bộ lớp Integration/Security (không hạ xuống H2: CHECK/RULE/TRIGGER của PostgreSQL sẽ không được kiểm ⇒ mất chính thứ cần test).

---

## 7. Rollback Plan

```bash
# 1) Rollback migration MF03 (dev only — KHÔNG chạy trên production)
#    Flyway community không hỗ trợ undo → dùng script hoàn tác thủ công theo TDS §12.2
psql "$DB_URL" -f 04_Development/Backend/db/rollback/V4__undo_mf03.sql
#    (DROP theo thứ tự ngược FK: health_history_events → student_clinical_files →
#     student_health_records → special_meal_requests → chronic_condition_declarations →
#     allergy_declarations → health_documents → student_parents → students)

# 2) Xoá RULE/TRIGGER/FUNCTION riêng của MF03 nếu cần dựng lại sạch
psql "$DB_URL" -c "DROP RULE IF EXISTS hh_no_update ON health_history_events;
                   DROP RULE IF EXISTS hh_no_delete ON health_history_events;
                   DROP TRIGGER IF EXISTS check_parent_link_limit ON student_parents;
                   DROP FUNCTION IF EXISTS trg_check_parent_link_limit();"

# 3) Revert code
git checkout -- 04_Development/Backend/src/main/java/com/sfms/mf03/ 04_Development/Backend/src/test/java/com/sfms/mf03/

# 4) Testcontainers không cần dọn tay (container bị huỷ sau mỗi lần chạy)
```

> ⚠️ **Không** dùng `npx prisma migrate reset` như ví dụ trong template — sai stack (dự án dùng **Flyway**, xem ràng buộc §2c-2).
> ⚠️ Dữ liệu `Restricted`: nếu rollback trên môi trường có dữ liệu thật ⇒ phải có DPO duyệt và sao lưu trước (TDS §12.2/§12.3).

---

## 8. CASE 2.0 Anti-Pattern Detection

> Checklist cho reviewer (Thảo — Lead Test) khi test case do AI hỗ trợ sinh.

| AP-ID | Anti-Pattern | Dấu hiệu trong TestSpec này | Check | Gate |
|-------|-------------|------------------------------|-------|------|
| AP-AI-001 | Unconstrained Generation | TC không reference BR/UC/MSG/D-rule nào ở `Oracle Source` | ☐ | G-0 |
| AP-AI-002 | **Green-from-Birth** | Test PASS với stub (§5.1) — chú ý 3 điểm ★/⚠️: `INT-003` bước 1, `INT-005` superuser, `SEC-005` denyAll | ☐ | **G-2 ★** |
| AP-AI-003 | Implicit Decision | Test giả định một quyết định chưa có ADR (VD chọn bên cho Q1/Q2) | ☐ | G-1 |
| AP-AI-004 | Layer Violation | Test verify controller chứa business logic | ☐ | G-4 |
| AP-AI-005 | Hallucinated Contract | Test import service/type không có trong TDS §8 | ☐ | G-3 |
| **AP-AI-006** | **Trust FE identity** | Test chấp nhận `studentId`/`schoolId` từ body làm căn cứ phân quyền | ☐ | `MF03-TC-SEC-009` |
| **AP-AI-009** | **Store derived field** | Test assert tồn tại cột `allergies`/`health_status`/`special_diet`/`bmi` | ☐ | `MF03-TC-008`, `INT-012` |
| **AP-AI-011** | **Name-as-key** | Fixture/test nối HS↔PH hoặc HS↔lớp bằng **chuỗi tên** (như mock) | ☐ | `MF03-TC-INT-006` |
| **AP-AI-012** | **Student-level verify flag** | Test chỉ đổi `Student.healthStatus` mà không đổi status từng khai báo | ☐ | `MF03-TC-INT-003` |
| **AP-AI-013** | **Mutable history** | Test dùng `PUT/DELETE /health-history` hay `repository.delete()` để dọn dữ liệu | ☐ | `MF03-TC-INT-005` |
| **AP-AI-014** | **PII leak** | Fixture dùng dữ liệu HS **thật**; test in tên HS/allergen ra log/console | ☐ | `MF03-TC-SEC-007` |
| **AP-AI-015** | **Silent field drop** | Test chấp nhận "field lạ bị bỏ qua" thay vì đòi 400 `STU-094` | ☐ | `MF03-TC-009`, `INT-008` |
| **AP-TS-001** | **Oracle bịa** *(mới — phát sinh khi soạn TestSpec này)* | Expected message viết lại cho "đọc xuôi" thay vì **nguyên văn** `error_message.md` (6 ca: L1–L5, L19) | ☐ | `MF03-TC-INT-013` |
| **AP-TS-002** | **Fixture ma** *(mới)* | TC trỏ mã seed **không tồn tại** hoặc **sai thuộc tính** (L6/L7/L8 — `ALG-004` không phải SEVERE, `binhnv@parent.vn` không có trong seed) | ☐ | §3 TDS-05 |

**Kết quả review:**

- [ ] Không phát hiện anti-pattern nào → TestSpec approved
- [ ] Phát hiện AP → ghi bảng dưới → fix trước khi implement

| AP detected | TC ID | Mô tả | Fix action | Fixed? |
|------------|-------|-------|------------|--------|
| `AP-TS-001` | (soạn thảo) | 6 expected message trong TDS §9.3/§15.2 lệch `error_message.md` v1.5 | Đã sửa trong TestSpec (L1–L5, L19); **Huy sửa lại TDS** | ☐ |
| `AP-TS-002` | (soạn thảo) | 3 fixture sai so với seed | Đã thay bằng `FX-005`/`FX-006`/`FX-002` (L6–L8) | ☐ |
| `AP-AI-001` | `MF03-TC-005` | Oracle BR-066 **không** chống lưng hành vi chặn (danh mục mở) | ✅ Đã xử lý 02/08 — **cấp oracle mới** (`BR-220` + `MSG-144`) thay vì sửa BR-066 cho vừa hành vi, cũng không "chọn bên" trong TestSpec | ☑ |

---

## PHỤ LỤC — DANH SÁCH MÃ ĐÃ TRÍCH DẪN

**Use Cases (15/15 — nguồn `traceability_matrix.md` §1.3, tên nguyên văn UC file):**
UC-010 (Bulk Import Student & Parent) · UC-025 (Create Student) · UC-036 (View List Allergy & Special Meals) · UC-050 (View List Student Health Information) · UC-051 (View List Student Health Records) · UC-077 (Review Health Information) · UC-078 (Review Special Meal Requests) · UC-084 (Submit Health & Allergy Information) · UC-093 (Update Health & Allergy Information) · UC-094 (Update Health Records) · UC-099 (Update Student) · UC-104 (View Allergy Information) · UC-145 (View Student Health & Allergy Information) · UC-146 (View Student Health History) · UC-147 (View Student List).
*(Khớp 100% 15 dòng của §1.3 — không thừa, không thiếu. Mọi TC đều truy được về ≥1 UC.)*

**Business Rules chính (7 — đúng cột "Business Rules" của §1.3):**
**BR-030** · **BR-031** · **BR-032** · **BR-065** · **BR-123** · **BR-172** · **BR-201**.

**Business Rules tham chiếu (6 — thuộc module khác, dùng làm ngữ cảnh/oracle phụ, KHÔNG tính là BR của MF03):**
BR-004 (MF01 — phiên đăng nhập, MSG-004) · BR-007 (RBAC 7 role) · BR-010 · BR-012 (MF01 — tài khoản PH, định dạng email) · BR-022 (MF02 — phạm vi trường) · BR-025 · BR-193 (MF02 — sức chứa lớp, năm học đóng) · BR-066 (MF05 — danh mục chế độ ăn, ⚠️ **L14**) · BR-173 (PDPD — ràng buộc thiết kế).
*(**BR-041 KHÔNG dùng** — UC-099 UC file cite nhầm sang "trạng thái nhà cung cấp" của MF04, xem CF-M03-3.)*

**System Messages — nguồn `error_message.md` **v1.5**, expected message dùng **nguyên văn**:**
- **Lỗi/chặn:** MSG-004 · MSG-005 · MSG-010 *(mượn MF02 — CF-M03-2)* · MSG-012 · MSG-110 · MSG-111 · MSG-112 · MSG-113 · MSG-114 · MSG-115 · MSG-116 · MSG-137 · MSG-138 · MSG-001 *(⚠️ L19 — text nói "school data")*
- **Thành công:** MSG-029 · MSG-031 · MSG-035 · MSG-036
- **KHÔNG dùng:** **MSG-064** (không UC nào của §1.3 tham chiếu — CF-M03-12 / **L15**)
- **Thiếu MSG:** `STU-193` (năm học CLOSED) hiện **không có** MSG nguồn trong TDS §10 → báo Huy.

**Mã lỗi module (`STU-*`) được phủ test — 14/14 dòng của TDS §10:**
STU-004 (401) · STU-005 (403) · STU-010 (409) · STU-012 (409) · STU-030 (422) · STU-031 (422) · STU-032 (422) · STU-066 (422) · STU-094 (400) · STU-172 (405) · STU-193 (409) · STU-201 (409) · STU-404 (404) · STU-500 (500).

**Luật `semantic-audit.ts` đã port (tên **copy nguyên văn từ script**, không lấy từ TDS §13):**

| Tên luật | §script | TC |
|---|---|---|
| `XAC_MINH_MUC_NANG_KHONG_CO_MINH_CHUNG` | §12 | `MF03-TC-003`, `MF03-TC-SEC-008` |
| `KHAI_DI_UNG_TRO_HOC_SINH_KHONG_TON_TAI` | §12 | `MF03-TC-INT-004` |
| `HOC_SINH_SAI_LOP` | §6 | `MF03-TC-INT-006` |
| `TEN_KHONG_KHOP_HOC_SINH` | §6 | `MF03-TC-INT-006` |

*(4/4 luật liên quan MF03. Script có 54 luật đăng ký qua `check()` + 3 luật chỉ `add()` — tổng 57 luật thật; 53 luật còn lại thuộc module khác.)*

**Quy ước chung được kiểm bằng test:**
**D1** (`MF03-TC-INT-011`, `CON-001`, `INT-014` 🔴) · **D2** (`INT-013`, `CON-002`) · **D3** (mọi path `/api/v1` — kiểm trong `INT-009/011`) · **D4** (`INT-010`, `CON-004`) · **D5** (`INT-011`) · **D6** (`TC-007`, `CON-003`) · **D7** (`INT-013`) · **D8** (`CON-001…005` — đã gỡ block, L16) · **D9** (`INT-011` — audit tự động, L20) · **D10** (upload tài liệu y tế qua `common/storage/StorageService`).

**Ràng buộc TDS §17.1 được kiểm:** C1 (`INT-009`) · C2 (`SEC-001/002/003/009`) · C3 (`TC-003`, `INT-003`, `SEC-008`) · C4 (`TC-001/002`, `INT-001`) · C5 (`INT-005`) · C6 (`TC-004/008`, `INT-006`) · C7 (`SEC-007`) · C8 (`TC-009`, `INT-008`) · C9 (`INT-007`).

**Mâu thuẫn/thiếu sót phát hiện thêm khi soạn TestSpec (ngoài 12 CF-M03-* của TDS §2.3):**
**19 Logic Issue (L1–L19)** ở §2 + **5 câu hỏi chặn Q1–Q5**. Cần Huy (Traceability/oracle), Mạnh (schema/D1), Nam (D8), Thảo (Test) xác nhận trước khi chuyển TestSpec sang **Approved**.

---

*TestSpec MF03 v1.0 — theo `06_Document/Templates/PHASE-4_Test-Spec.md` (TDD Template v2.0 + CASE 2.0), ví dụ đã dịch từ TypeScript/Prisma sang **Java 25 / Spring Boot 4.1.x / Flyway / Testcontainers** theo ràng buộc stack `00_Team_Work_Assignment_Plan.md` §2c-2.*
