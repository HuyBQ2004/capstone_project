# SFMS — Database Schema Review

## Rà soát lược đồ CSDL (Flyway V1 → V33)

> **Stack:** PostgreSQL 16 + pgcrypto · Flyway · Spring Boot (Hibernate/JPA)
> **Phạm vi:** `04_Development/Backend/src/main/resources/db/migration/` — 31 file, 105 bảng, 1.419 cột, 7 view
> **Đối chiếu với:** `04_Development/Database_file/SFMS_Database.dbml`
> **Ngày rà soát:** 05/08/2026
> **Trạng thái file DBML:** ✅ khớp 100% với migration (105/105 bảng, 1.419/1.419 cột) — các phát hiện dưới đây là về **thiết kế CSDL**, không phải về file DBML.

---

## 0. Tóm tắt

| # | Vấn đề | Mức | Bảng / file |
|---|--------|-----|-------------|
| 1 | `uq_fb_once` không chặn được phản hồi trùng (NULLS DISTINCT) | 🔴 Cao | `feedbacks` — V18 |
| 2 | Role `sfms_app` có mật khẩu hardcode, `LOGIN`, tạo ở mọi môi trường | 🔴 Cao | V4 |
| 3 | V23 kết luận sai vì kiểm thiếu → `audit_logs` mất 1 lớp bảo vệ | 🔴 Cao | `audit_logs` — V23 |
| 4 | BR-091 (nhiệt độ giữ ấm ≥60°C) bị comment out ở tầng DB | 🔴 Cao | `inspection_sessions` — V12 |
| 5 | Hai chuẩn append-only khác nhau; chuẩn phổ biến hơn thì thất bại im lặng | 🟡 Trung bình | 9 bảng — V4/16/18/20 |
| 6 | Khoá ngoại giả bằng mảng / JSONB | 🟡 Trung bình | `issue_slips`, `roles` |
| 7 | Chính sách FK chéo module không nhất quán | 🟡 Trung bình | MF08 vs MF11 |
| 8 | Không có ràng buộc chặn tồn kho âm | 🟡 Trung bình | `inventory_transactions` |
| 9 | Kiểu dữ liệu sai bản chất + `updated_at` không nhất quán | 🟢 Thấp | MF07, MF08, MF11 |

**Đề xuất ưu tiên:** xử lý #1 và #2 trong một migration `V34` (sửa được ngay, rủi ro thấp). #3 và #4 cần nhóm thống nhất trước khi sửa.

---

## 1. 🔴 `uq_fb_once` không chặn được phản hồi trùng

**Vị trí:** `V18__mf10_feedback_announcement.sql:46`

```sql
-- Một người chỉ phản hồi MỘT lần cho một (phiên ăn × món); sửa dùng PATCH (UC-046 AF1)
CONSTRAINT uq_fb_once UNIQUE (submitter_user_id, meal_session_id, dish_id, student_id)
```

**Vấn đề.** `dish_id` là NULL khi `scope` = `SESSION`/`CLASS`; `student_id` là NULL khi `scope` = `CLASS`. PostgreSQL mặc định coi hai NULL là **khác nhau** (`NULLS DISTINCT`), nên UNIQUE không kích hoạt. Ràng buộc chỉ thực sự có tác dụng với `scope = DISH`.

**Hệ quả.** Phụ huynh/giáo viên gửi phản hồi cấp phiên ăn hoặc cấp lớp có thể tạo **bao nhiêu bản ghi trùng cũng được** — làm sai lệch điểm trung bình, đếm số phản hồi, và dashboard của MF10/MF13.

**Ghi chú.** Chính MF10 đã nhận ra đúng cái bẫy này ở chỗ khác — xem comment `V18:148-149` giải thích tại sao `notifications.code` phải UNIQUE trên một cột thay vì `(school_id, code)`. Chỉ là bỏ sót ở đây.

**Đề xuất sửa** (PostgreSQL 15+, stack đang dùng PG16):

```sql
ALTER TABLE feedbacks DROP CONSTRAINT uq_fb_once;
CREATE UNIQUE INDEX uq_fb_once ON feedbacks
  (submitter_user_id, meal_session_id, dish_id, student_id) NULLS NOT DISTINCT;
```

⚠️ **Cần quyết định kèm theo:** `feedbacks` có cột `deleted_at` (xoá mềm). Unique ở trên tính cả dòng đã xoá mềm ⇒ người dùng xoá rồi gửi lại sẽ bị chặn. Nếu không muốn vậy thì thêm `WHERE deleted_at IS NULL`. Cần chốt hành vi mong muốn trước khi viết migration.

---

## 2. 🔴 Role `sfms_app` có mật khẩu hardcode và quyền đăng nhập

**Vị trí:** `V4__mf03_student_parent.sql:194-198`

```sql
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'sfms_app') THEN
        CREATE ROLE sfms_app LOGIN PASSWORD 'sfms_app_only_for_rule_test';
    END IF;
END
$$;
```

**Vấn đề.** Comment ghi "Mật khẩu cố định CHỈ để test/dev", nhưng đây là **Flyway migration** — nó chạy trên mọi môi trường, kể cả production. Kết quả là hệ thống luôn tồn tại một role:

- có `LOGIN` (đăng nhập trực tiếp vào Postgres được),
- mật khẩu nằm công khai trong Git history,
- được `GRANT SELECT, INSERT, UPDATE, DELETE` trên toàn bộ 8 bảng MF03 (`V4:209-212`) — trong đó có `allergy_declarations`, `chronic_condition_declarations`, `student_clinical_files`, tức **hồ sơ sức khoẻ học sinh**.

**Hệ quả.** Nếu cổng 5432 lộ ra ngoài (hoặc kẻ tấn công đã vào được mạng nội bộ), đây là đường vào dữ liệu nhạy cảm nhất của hệ thống mà không cần khai thác lỗ hổng nào.

**Đề xuất sửa.** Role này chỉ tồn tại để chứng minh RULE append-only hoạt động (`MF03-TC-INT-005`, `HealthHistoryAppendOnlyIT`) — nó **không cần đăng nhập** từ migration production:

```sql
-- V34: role kiểm thử không được phép đăng nhập ở môi trường thật
ALTER ROLE sfms_app NOLOGIN;
```

Rồi cho test tự cấp `LOGIN` + mật khẩu ngẫu nhiên trong `db/testseed/` (chạy sau migration chính, chỉ ở profile test). Nếu đã từng deploy production với migration này, **đổi/huỷ mật khẩu ngay** — sửa migration thôi không đủ vì role đã tồn tại.

---

## 3. 🔴 V23 kết luận sai do kiểm thiếu → `audit_logs` bỏ mất một lớp bảo vệ

**Vị trí:** `V23__mf12_platform_services.sql:154-156`

```sql
-- M12-D13: KHÔNG `REVOKE UPDATE, DELETE ON audit_logs FROM sfms_app`.
-- Role `sfms_app` KHÔNG tồn tại trong dự án (kiểm V1/V2 + application.yml) ⇒ lệnh REVOKE
-- làm Flyway đỏ ngay trên máy cả nhóm.
```

**Vấn đề.** Kết luận này **sai**: role `sfms_app` được tạo ở `V4:197`, tức là 19 migration **trước** V23. Người viết chỉ kiểm V1/V2 và `application.yml` rồi suy ra role không tồn tại, nên đã bỏ luôn lệnh `REVOKE`.

**Hệ quả thực tế.** Ở mức hiện tại thì **chưa thủng**, vì `sfms_app` không được `GRANT` gì trên `audit_logs` (V4 chỉ cấp quyền trên các bảng MF03), và ba lớp bảo vệ mà M12-D13 liệt kê vẫn hoạt động:

1. `@Immutable` trên `iam/domain/AuditLog` — JPA không sinh `UPDATE`
2. Không expose `PUT`/`DELETE /api/v1/audit-logs`
3. Chuỗi hash `prev_hash → entry_hash`

**Nhưng** lập luận nền thì sai, và nếu sau này có ai `GRANT` thêm quyền cho `sfms_app` (rất dễ xảy ra vì role này đã có sẵn) thì tuyến phòng thủ ở tầng DB không tồn tại. Cần sửa lại comment cho đúng sự thật và cân nhắc bổ sung `REVOKE` — lệnh này giờ chạy được vì role có thật.

---

## 4. 🔴 BR-091 bị vô hiệu hoá ở tầng DB

**Vị trí:** `V12__mf07_inspection.sql:56-60`

```sql
-- BR-091: đã chốt Bước 3 PASSED thì phải có nhiệt độ giữ ấm >= 60.
-- ⚠️ CF-24: FE Bước 3 hiện KHÔNG có ô nhập holdingTemp — CHƯA enable CHECK này ở bản này,
--    chỉ enforce ở service layer (InspectionSessionServiceImpl) để không chặn phiên hợp lệ hiện tại.
-- CONSTRAINT ck_insp_holding_temp
--   CHECK (step3 <> 'PASSED' OR (holding_temp IS NOT NULL AND holding_temp >= 60)),
```

**Vấn đề.** Đây là bất biến **an toàn thực phẩm** (nhiệt độ giữ ấm trước khi ăn, QĐ 1246). Mọi bất biến BR cùng loại trong dự án đều được chốt ở tầng DB — kể cả BR-090 ngay bên trên nó (`ck_insp_core_temp`, nhiệt độ lõi ≥75°C) vẫn đang bật. Riêng BR-091 chỉ còn ở service.

**Hệ quả.** Bất kỳ đường ghi nào không đi qua `InspectionSessionServiceImpl` (batch job, script sửa dữ liệu, module khác, import) đều có thể tạo phiên kiểm thực `step3 = PASSED` mà không có nhiệt độ giữ ấm — tức là hồ sơ pháp lý sai mà DB không cản.

**Đề xuất.** Đây là nợ kỹ thuật có chủ đích (CF-24), không phải lỗi ẩn. Cần: (a) mở ticket theo dõi gắn với việc FE bổ sung ô nhập `holdingTemp` ở Bước 3, (b) khi FE xong thì backfill dữ liệu cũ rồi bật lại CHECK trong một migration mới. Không nên để trạng thái này trôi qua giai đoạn nghiệm thu mà không ai biết.

---

## 5. 🟡 Hai chuẩn append-only khác nhau

**Cách A — `RULE ... DO INSTEAD NOTHING` (9 chỗ):**

| Bảng | File |
|---|---|
| `health_history_events` (UPDATE + DELETE) | V4 |
| `serving_dishes` (UPDATE + DELETE) | V16 |
| `announcement_revisions` (UPDATE + DELETE) | V18 |
| `incident_events` (UPDATE + DELETE) | V20 |
| `incident_quarantine_batches` (DELETE) | V20 |

**Cách B — `TRIGGER ... RAISE EXCEPTION` (1 chỗ):** `report_exports` — `V29:42-49`.

**Vấn đề với cách A.** `DO INSTEAD NOTHING` khiến câu lệnh **thành công nhưng không làm gì**: `UPDATE` trả về `UPDATE 0` rồi commit bình thường. Code sai trông y hệt code đúng. Qua Hibernate thì lại nổ `StaleStateException: unexpected row count from update [0]` — một lỗi hạ tầng khó hiểu, không nói được rằng "bảng này append-only".

Cách B ngược lại: fail ngay, thông điệp rõ (`'report_exports is append-only (BR-182)'`).

**Đề xuất.** Thống nhất về cách B. Chuyển 9 RULE sang trigger `RAISE EXCEPTION` kèm mã BR tương ứng (BR-172, ADR-M09-1, BR-144, ADR-M11-2, BR-217). Việc này không đổi ngữ nghĩa nghiệp vụ, chỉ đổi *cách thất bại* từ im lặng sang ồn ào — và làm test dễ viết hơn (assert exception thay vì assert row count).

---

## 6. 🟡 Khoá ngoại giả bằng mảng / JSONB

| Vị trí | Khai báo | Trỏ tới |
|---|---|---|
| `V10:259` | `issue_slips.menu_ids UUID[] NOT NULL` | `menus.id` |
| `V1:81` | `roles.permissions JSONB NOT NULL DEFAULT '[]'` | `permissions.code` |

PostgreSQL không kiểm được tham chiếu trong mảng/JSONB. Xoá một `menu` hoặc `permission` sẽ để lại ID mồ côi mà không có cảnh báo nào; truy vấn ngược ("phiếu xuất nào dùng menu X") phải dùng `= ANY(menu_ids)` hoặc toán tử JSONB, không tận dụng được index thường.

`roles.permissions` là quyết định có ý thức (ADR-M04: giữ mô hình mock, danh mục 16 permission cố định chỉ đọc) — rủi ro thấp, chấp nhận được. `issue_slips.menu_ids` thì không thấy ADR nào biện minh; nên cân nhắc tách bảng nối `issue_slip_menus(issue_slip_id, menu_id)`.

---

## 7. 🟡 Chính sách FK chéo module không nhất quán

Cùng là tham chiếu tới `inspection_sessions(id)`, hai module xử lý ngược nhau:

| Nơi khai | Có FK? | Lý do ghi trong file |
|---|---|---|
| `food_samples.inspection_id` (V14:65) | ❌ Không | ADR-M08-5 — "tránh khoá thứ tự migrate" |
| `corrective_actions.inspection_id` (V20:202) | ✅ Có | M11-D09 |
| `violation_records.source_inspection_id` (V20:266) | ✅ Có | M11-D09 |

Lý do của ADR-M08-5 (tránh phụ thuộc thứ tự migrate) không còn đúng ở thời điểm hiện tại: V14 chạy **sau** V12, nên FK hoàn toàn khả thi — và MF11 đã chứng minh điều đó ở V20.

Tình trạng tương tự với các ID người dùng của MF11 (`reported_by_id`, `closed_by_id`, `recorded_by_id`, `handled_by_id`… — M11-D06 không khai FK) trong khi mọi module khác đều FK về `user_accounts(id)`.

**Đề xuất.** Không nhất thiết phải sửa hết, nhưng nên chốt **một** nguyên tắc và ghi vào `00_Common_Conventions.md`: hoặc "FK chéo module luôn khai", hoặc "chỉ khai trong cùng bounded context". Hiện tại mỗi module tự quyết, và bản đồ quan hệ trong `SFMS_Database.dbml` phải liệt kê riêng một khối "tham chiếu mềm" vì lý do này.

---

## 8. 🟡 Không có ràng buộc chặn tồn kho âm

`inventory_batches` cố ý không lưu `current_quantity` (ADR-M06-1) — tồn kho là giá trị dẫn xuất:

```
tồn kho = initial_quantity + SUM(inventory_transactions.delta)
```

`inventory_transactions` có `CHECK (delta <> 0)` và `ck_tx_sign` (RECEIPT dương, ISSUE âm), nhưng **không có gì ngăn tổng cộng dồn xuống dưới 0**. DB sẽ vui vẻ nhận lệnh xuất 50kg từ một lô chỉ còn 3kg.

Đây là hệ quả tất yếu của mô hình sổ cái (ledger) — không sai, nhưng cần biết rằng bất biến "không xuất quá tồn" **hoàn toàn nằm ở tầng service**. Nếu muốn chốt ở DB thì phải dùng trigger `BEFORE INSERT` tính lại tổng theo `batch_id` (có index `ix_tx_batch` sẵn), đánh đổi bằng chi phí ghi.

---

## 9. 🟢 Kiểu dữ liệu và tính nhất quán

**(a) Mốc thời gian lưu dạng chuỗi** — trái quy ước D4 của chính nhóm:

| Cột | Kiểu | File |
|---|---|---|
| `step1_fresh_rows.received_at`, `step1_dry_rows.received_at` | `VARCHAR(32)` | V12:80, V12:105 |
| `step2_rows.prep_done_at`, `cook_done_at` | `VARCHAR(32)` | V12:130-131 |
| `step3_rows.divided_done_at`, `eat_start_at` | `VARCHAR(32)` | V12:152-153 |
| `corrective_actions.deadline` | `VARCHAR(40)` | V20:218 (đã deprecated) |

Không sắp xếp, so sánh, hay validate được; báo cáo MF13 muốn lọc theo khoảng thời gian trên các cột này sẽ phải parse chuỗi.

**(b) Hai cột boolean chỉ có thể mang một giá trị** — `V14:106`:

```sql
CONSTRAINT ck_smp_container_state CHECK (sanitized AND sealed)
```

`sanitized` và `sealed` đều `NOT NULL` và bắt buộc `TRUE` ⇒ hai cột này vĩnh viễn chỉ chứa `TRUE`. Vừa thừa dung lượng, vừa khiến hệ thống **không ghi nhận được** ca dụng cụ chưa tiệt trùng / chưa niêm phong — trong khi đó chính là thông tin cần lưu vết khi có sự cố.

**(c) Giá trị enum tiếng Việt có dấu** — `V20:245`:

```sql
type VARCHAR(20) NOT NULL DEFAULT 'Quy trình',
```

Trái D6 (enum lưu `SCREAMING_SNAKE`), lại trùng vai với cột `violation_type` ngay bên dưới (`HYGIENE_VIOLATION | PROCEDURE_SKIP | ...`). Nên gộp hoặc bỏ một trong hai.

**(d) `updated_at` không nhất quán** — 54 khai báo để nullable (`updated_at TIMESTAMPTZ`), 21 khai báo `NOT NULL DEFAULT now()`. MF01/04/05/06/11/12/13 theo kiểu thứ nhất; MF07/09/10 theo kiểu thứ hai. Hệ quả: không viết được một truy vấn "sửa lần cuối lúc nào" dùng chung cho mọi bảng, và ngữ nghĩa "chưa từng sửa" khác nhau giữa các module (`NULL` vs `= created_at`).

---

## 10. Những điểm KHÔNG phải vấn đề

Ghi lại để lần review sau khỏi mất công kiểm lại:

- **Thiếu `V26`, `V27`.** Nam nhường số cho MF13 lúc chưa commit (xem comment đầu `V28`); MF13 cuối cùng dùng V29-V31. Flyway chấp nhận version không liên tục — không ảnh hưởng gì.
- **`partners` không có `school_id`.** Đúng thiết kế: đối tác là thực thể cấp nền tảng, phạm vi trường đến từ `supplier_partnerships` (và view `v_rpt_supplier_cert` đã xử lý đúng chỗ này).
- **`inventory_batches` không có `current_quantity`.** Chủ ý theo ADR-M06-1 (mô hình sổ cái) — xem mục 8 để biết đánh đổi kèm theo.
- **`dish_ingredients.ingredient_code` FK trỏ `catalog_ingredients(code)` thay vì `(id)`.** Hợp lệ (cột đích có UNIQUE), là quyết định của M06-D04, và lý do dời lệnh `ADD CONSTRAINT` từ V10 sang cuối V11 đã ghi rõ trong file.
- **`notifications.code` UNIQUE trên một cột** thay vì `(school_id, code)`. Đúng — `school_id` nullable nên `(school_id, code)` sẽ không chặn được trùng. Đây là ngoại lệ có ý thức của D1, đã ghi ở `V18:148`.

---

## Phụ lục — Cách kiểm chứng

Mọi phát hiện trong tài liệu này được xác nhận bằng cách đọc trực tiếp file migration, không suy đoán từ tài liệu thiết kế. Riêng phần đối chiếu `SFMS_Database.dbml` ↔ migration dùng script so khớp từng cột (parse `CREATE TABLE` + `ALTER TABLE ADD/DROP COLUMN` của cả 31 file, so với AST do `@dbml/core` sinh ra).

⚠️ **Lưu ý khi tự viết script kiểm tra trên repo này:** file migration dùng **CRLF**. Regex cắt comment kiểu `/--.*$/` trong JavaScript sẽ **không khớp**, vì `.` không ăn ký tự `\r` nên `$` không bao giờ chạm tới. Phải chuẩn hoá `\r\n → \n` trước khi xử lý — nếu không, kết quả đối chiếu sẽ sai hàng loạt theo hướng "thiếu cột" giả.
