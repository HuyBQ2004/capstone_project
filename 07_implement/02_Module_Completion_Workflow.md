# 02 — Quy trình hoàn thiện Backend + Frontend cho MF04 → MF13

> **Ngày lập:** 2026-08-02 · **Cập nhật:** 2026-08-04 (MF12 → §2.2; **MF13 → §2.3 — module cuối cùng, 13/13 đã qua Bước 1**) · **Trạng thái:** 🟡 Đang áp dụng — §6 (D11–D13) cần Nam ký theo RACI §1
> **Thay thế:** bản nháp "Định hướng hoàn thiện Backend + Frontend cho 5 thành viên" (soạn 2026-08-02, chưa đánh số)
> **Nguồn sửa đổi:** đợt nối FE của MF03 (02/08/2026) — mọi con số dưới đây đo bằng cách gọi backend đang chạy, không suy từ tài liệu
> **Quan hệ:** `00_Common_Conventions.md` D1–D10 vẫn là hợp đồng gốc. File này chỉ nói **quy trình**, và đề xuất thêm D11–D13.

---

## §1 — Vấn đề (đã hiệu chỉnh so với bản nháp)

Bản nháp chẩn đoán: *"nguyên nhân gốc là D1–D10 được vá dần trong lúc build thay vì chốt trước"*.

Chẩn đoán đó **đúng nhưng chưa đủ**. Bằng chứng từ đợt nối MF03:

| Đo được 02/08/2026 | Ý nghĩa |
|---|---|
| MF01+MF02 có TDS Approved → **6/7 type FE khớp hoàn toàn** với DTO backend | Quy trình rà tài liệu **có tác dụng thật** |
| MF03 cũng Approved → vẫn lệch ~12 chỗ, phải curl tay từng endpoint để tìm | Rà tài liệu **không bắt được** lớp lỗi này |

Ba ví dụ cho thấy vì sao tài liệu không thể bắt:

- `StudentResponse` trả JSON key `clazz` vì `class` là **từ khoá Java** — không TDS nào viết ra được chi tiết này.
- `allergySeverity` trả `SEVERE` trong khi FE hiển thị `"Nặng"` — lệch xảy ra ở tầng render, TDS chỉ ghi "mức độ dị ứng".
- `PATCH /students/{code}` với body `{"class":"4A"}` trả **200 mà không lưu gì** — đó là hành vi mặc định của Jackson khi field không khớp, không phải điều ai thiết kế.

> **Kết luận đưa vào quy trình:** rà tài liệu bắt lệch **thiết kế**; lệch **hiện thực** chỉ bắt được bằng artefact sinh ra từ code thật (OpenAPI → type TypeScript). **Cần cả hai** — §2 Bước 2 vì thế có thêm một việc cuối.

Chi phí thật của việc thiếu vế thứ hai: MF03 tốn **gần hai phiên làm việc** chỉ để dò lệch hợp đồng, backend phải ra **3 commit sửa** (`4075002`, `a7ff1bd`, và đợt sửa `clazz`), mà cuối cùng vẫn chỉ nối được **1 trong 4 resource**. Còn 9 module nữa.

---

## §2 — Vòng lặp chuẩn cho MỌI module (bắt buộc đúng thứ tự)

```
Bước 0 — Rà chủ sở hữu resource    ← MỚI 2026-08-03, ĐIỀU KIỆN VÀO Bước 1
Bước 1 — Rà & chốt tài liệu       (bắt buộc trước khi code)
Bước 2 — Sinh code backend         (chỉ chạy khi Bước 1 Status: Approved)
   └─ 2b. npm run gen:api          ← không được bỏ
Bước 3 — Nối frontend thật         (chỉ chạy khi Bước 2 xong + test pass)
```

Thứ tự này giữ nguyên từ bản nháp — MF01/MF02 là bằng chứng nó có tác dụng. Thêm **Bước 2b** (02/08) và **Bước 0** (03/08).

### §2.1 — Bước 0: rà chủ sở hữu resource (2 lệnh, ~15 phút)

**Chạy TRƯỚC khi mở TDS của mình.** Hai lệnh:

```bash
# (1) Bảng nào ĐÃ TỒN TẠI trong migration đã merge
grep -ho "CREATE TABLE [a-z_]*" 04_Development/Backend/src/main/resources/db/migration/*.sql | sort -u

# (2) Path nào ĐÃ CÓ controller
grep -rho '"/api/v1/[a-z-]*' 04_Development/Backend/src/main/java --include=*.java | sort -u
```

**Luật:** cái gì đã có tên trong hai danh sách đó thì module của bạn **không `CREATE TABLE`, không `@RequestMapping`**. Bảng đã có mà thiếu cột ⇒ `ALTER TABLE`. Cần **đọc** dữ liệu ⇒ `common/reference/ReferenceResolver` (tra cứu thuần) hoặc **Port** do module sở hữu expose (hành vi). Xem `00_Common_Conventions.md` §0-BIS "Cross-module".

**Vì sao thành bước riêng, không phải một dòng trong checklist Bước 1:** đây là lớp lỗi **rẻ nhất để bắt** (2 lệnh) và **đắt nhất khi bỏ sót** — `CREATE TABLE` trùng làm **Flyway đỏ trên máy cả nhóm**, hai controller cùng path làm **ứng dụng không khởi động được**. Cả hai đều nổ ở *lần chạy đầu tiên sau khi merge*, tức là nổ vào mặt người khác chứ không phải người viết.

**Kết quả chạy Bước 0 ngày 2026-08-03 cho 8 module chưa code** *(bản đã soi từng chỗ, sửa 2 dương tính giả của lần rà đầu)*:

**A. Trùng bảng / trùng path — sửa trước khi vào Bước 1**

| Module | Vấn đề | Sự thật trong code | Trạng thái |
|---|---|---|---|
| **MF05** | `special_meal_requests` + `/special-meals` | MF03 sở hữu — `V4:120`, `mf03/web/SpecialMealRequestController` | ✅ **đã sửa** (`M05-D04`) |
| **MF12** | `audit_logs` + 5 FK `REFERENCES users(id)` | MF01 sở hữu — `V1:278` (PK **UUID**, cột `actor VARCHAR(180)` chứ không phải FK), `iam/service/AuditService` đang ghi | ✅ **đã sửa 2026-08-04** (`M12-D01`/`M12-D04`) — `CREATE` → `ALTER ADD` 3 cột **nullable** (`V1`/`V2` đã sinh dòng audit trước khi có chuỗi hash ⇒ `NOT NULL` là fail ngay); 5 FK → `UUID REFERENCES user_accounts(id)`. **Nhưng đây KHÔNG phải ca nặng nhất của MF12** — xem hàng `checklist_*` ở A-BIS |
| **MF11** | `CREATE TABLE code_sequences (school_id, prefix, year, next_value)` | `V1:31` — **`(scope VARCHAR(64) PK, next_value BIGINT)`**, KHÔNG có `school_id`/`year`. `common/code/CodeGenerator` `INSERT … ON CONFLICT (scope)` đang chạy cho MF01/02/03/04 | ✅ **đã sửa 2026-08-04** (`M11-D01`/`M11-D02`) — gỡ `CREATE TABLE`, dùng `scope = "mf11_<resource>:<năm>"` theo tiền lệ MF03/MF05/MF06 |

**A-BIS. Trùng giữa hai module CHƯA MERGE — hai lệnh grep của Bước 0 KHÔNG bắt được** *(thêm 2026-08-03, khi làm Bước 0 của MF07)*

| Resource | Ai cùng khai | Hậu quả nếu để nguyên | Trạng thái |
|---|---|---|---|
| `violation_records` + `/api/v1/violations` + `/override-abuse` | **MF07** §5.2 · **MF11** `TDS-MF11` dòng **984** + §9 | **Flyway đỏ** *(2 migration cùng tạo 1 bảng)* **CỘNG** **app không khởi động** *(2 controller 1 path)* | ✅ **đã sửa** — `M07-D07`: MF07 gỡ, MF11 giữ |
| `corrective_actions` + `/api/v1/corrective-actions` | **MF07** §9.2 · **MF11** dòng **938** | như trên | ✅ **đã sửa** — `M07-D08`: MF07 gọi `CapaPort` của MF11 |
| `GET /api/v1/compliance-report` | **MF07** §9.2 hàng 21 · **MF13** §9.1 hàng 1 (UC-171) | app không khởi động; và MF13 mất **endpoint duy nhất có trong mock** | ✅ **đã sửa** — `M07-D09`: MF07 gỡ, expose `ComplianceDataPort` |
| `checklist_groups` / `checklist_items` | **MF07** §5.2 · **MF12** dòng **623/631** (`IF NOT EXISTS`) | ~~`IF NOT EXISTS` làm schema **phụ thuộc thứ tự merge** — Flyway vẫn **xanh**~~ **CỘNG app không khởi động** — xem ghi chú | ✅ **đã gỡ 2026-08-04** — `M07-D10` + `M12-D02`. ⚠️ **Ô "Hậu quả" của hàng này ghi THIẾU một nửa, và MF12 phát hiện khi tới lượt:** MF07 không chỉ tạo bảng, nó đã merge **`mf07/web/ChecklistGroupController`** với **đúng 4 verb** (`GET` · `POST` · `PUT /{code}` · `DELETE /{code}`, cả 4 cùng `@PreAuthorize("hasRole('MEDICAL_STAFF')")`) mà `TDS-MF12` §9.2 hàng 17–20 khai. Tức hậu quả thật **giống hệt 2 hàng đầu bảng**, không nhẹ hơn. MF12 gỡ **2 khối DDL + 4 endpoint + `ChecklistAdminService` + `ChecklistItemRepository` + 4 TC** |
| `POST /api/v1/notifications` | **MF10** §9.1 *(chủ)* · **MF12** §9.3.9 | **KHÔNG phải va chạm** — TDS-MF12 đã tự ghi *"resource của MF10 · MF12 **không** sở hữu, chỉ gọi qua interface"*. Ghi vào bảng này làm **mẫu ngược**: đây là cách A-BIS **nên** kết thúc — module tiêu thụ ghi rõ chủ sở hữu **ngay trong TDS của mình**, không cần ai họp | ✅ **không phải sửa** — `M10-D06` xác nhận MF10 expose `NotificationPort`; 4 module chờ (MF05/06/07/11) |

> **Vì sao cần mục A-BIS riêng:** hai lệnh grep của §2.1 liệt kê thứ **đã có trong code đã merge**. MF07 sạch tuyệt đối ở cả hai (0 bảng trùng trên 53 bảng, 0 path trùng trên 31 path) — nhưng vẫn đụng **4 chỗ** với 3 module chưa merge. Lớp lỗi này nổ **muộn hơn** và **đau hơn**: không phải lúc bạn merge, mà lúc **người thứ hai** merge, và lúc đó cả hai bên đều đã code xong.
>
> **Lệnh thứ tư và thứ năm — chạy trên `07_implement/`, không phải trên code:**
> ```bash
> # path bị ≥2 TDS cùng khai
> grep -rho '/api/v1/[a-z-]*' 07_implement/*/TDS-*.md | sort | uniq -d
> # ai còn khai bảng của bạn
> grep -l "CREATE TABLE <tên_bảng>" 07_implement/*/TDS-*.md
> ```
> Cộng lệnh `REFERENCES` của Huy, Bước 0 thành **5 lệnh** — vẫn dưới 20 phút.
>
> **Cách xử lý đúng là GỠ SCOPE CỦA CHÍNH MÌNH, không phải giành.** Cả 4 ca trên đều giải bằng cách MF07 bỏ bớt việc: nghiệp vụ **không mất gì** (BR-095 vẫn tự tạo CAPA trong cùng transaction, chỉ đổi từ *mở endpoint* sang *gọi Port*), mà không cần ai họp.

**B. Không phải lỗi, nhưng cần thoả thuận trước**

| Module | Việc | Vì sao không tính là lỗi |
|---|---|---|
| **MF09** | Thêm `/students/{id}/meal-history`, `/students/{id}/special-meal-records` — **root `/students` là của MF03** | Spring cho phép 2 `@RequestMapping("/api/v1/students")` khác sub-path, nên **không vỡ**. Nhưng nó chia quyền sở hữu một resource cho 2 module ⇒ **Minh + Huy chốt** ai giữ controller. ✅ **đã xử lý 2026-08-03** — `M09-D07`: MF09 mở `MealHistoryController` riêng, **đổi `{studentId}` → `{code}`** cho khớp D1 + khuôn của 2 controller MF03 đang chạy; chờ Huy xác nhận. <br>⚠️ **Ô `GET /special-meals` "không phải lỗi" là SAI — đúng một nửa.** Nội dung ghi chú của TDS-MF09 đúng, nhưng dòng đó vẫn **nằm trong bảng endpoint §9.1 của MF09**, tức vẫn tuyên bố sở hữu ⇒ Bước 2 sinh code sẽ tạo controller trùng `mf03/web/SpecialMealRequestController` và **app không khởi động**. ✅ **đã sửa** — `M09-D06`: gỡ khỏi §9.1 (15 → 14 endpoint). **Bài học: đọc bảng §9.1 chứ đừng đọc ghi chú §9.2 — bảng mới là thứ sinh code.** |
| **MF11** | `/api/v1/auth` | Chỉ là lệnh `curl … /auth/login` lấy JWT trong §15 mẫu thử — **không phải endpoint MF11 định mở**. Tôi flag nhầm ở lần rà đầu |
| **MF12** | `POST /api/v1/reports/{compliance,violations,incidents}/send` — root `/api/v1/reports` là của **MF13** (10 path: `/reports/{slug}/rows`, `/reports/{slug}/export`, …) | Spring khởi động được — `send` ≠ `export` nên không path nào trùng chính xác. **Nhưng `{slug}` của MF13 là BIẾN**: ngày MF13 thêm slug `compliance` thì hai bên tranh nhau, và lúc đó cả hai đều đã code xong. ✅ **đã xử lý 2026-08-04** — `M12-D06`: đổi sang `POST /sent-reports/{compliance,violations,incidents}`, gom về root MF12 sở hữu, cũng đúng nghiệp vụ hơn (tạo bản ghi `sent_reports`, không xuất file). <br>💡 **Chi phí đổi = 0** vì cả 3 endpoint đều 🆕 chưa có call-site FE. **Đây là cách nhóm B nên kết thúc:** bắt lúc còn 🆕 thì đổi path là một dòng; bắt sau Bước 3 thì là cả một đợt nối FE |

**C. Ba lỗi XUYÊN MODULE — không module nào tự sửa được một mình**

| # | Lỗi | Ai dính | Phải làm |
|---|---|---|---|
| **C1** | `code_sequences` thật là **counter phẳng theo `scope`**, không có `school_id`/`year`. Nhưng D1 và ADR-M11-5 chốt khuôn mã `<PREFIX>-<năm>-<số trong năm>` (`INC-2026-014`, `MENU-2026-30`, `RPT-2026-033`, `AIC-2026-0007`) ⇒ **`CodeGenerator` hiện tại không sinh được khuôn đó** | **MF05, MF11, MF12, MF13** (mọi module có năm trong mã) | **Không đổi bảng dùng chung.** MF03 đã có lời giải và đang chạy: `codeGenerator.next("mf03_registration_no:" + Year.now().getValue(), …)` — nhét năm **vào chính chuỗi `scope`** (`VARCHAR(64)`, tự do). Lấy đó làm quy ước chung, khuôn `scope = "<resource>:<năm>"`. Zero thay đổi schema, zero ảnh hưởng MF01/02/04 |
| **C2** | **21 chỗ `REFERENCES users(id)`** — bảng tên thật là **`user_accounts`**, PK **UUID** | ~~MF07 (9)~~ ✅ · ~~MF10 (7)~~ ✅ *(sửa 2026-08-03, `M10-D04`)* · **MF12 (5)** | Đổi hết thành `UUID REFERENCES user_accounts(id)`. Bảng `users` **chưa bao giờ tồn tại** ⇒ migration fail ở lệnh đầu tiên. MF07 còn sai cả kiểu (`VARCHAR(32)`). **MF07 đã sửa 2026-08-03** (`M07-D03`) — và tiện thể tìm thêm **13 FK sai KIỂU** tới bảng có thật (`schools`/`partners`/`menus`/`dishes`/`inventory_batches` đều PK `UUID`, MF07 khai `VARCHAR`). ⇒ **C2 rộng hơn tên bảng: phải rà cả kiểu của mọi FK ngoại module**, không riêng `users` |
| **C3** | **`catalog_ingredients` không TDS nào tạo** nhưng MF05 và MF06 đều `REFERENCES` nó | **MF06** (chủ hợp lý) | ✅ **ĐÃ CHỐT 2026-08-03 (`M06-D04`, Bước 1 của MF06).** `V10__mf06_ingredient_inventory.sql` tạo bảng (`code UNIQUE`, `unit`, `storage_zone`, `refuse_factor` — ràng buộc C7), rồi `ALTER TABLE dish_ingredients ADD CONSTRAINT fk_dish_ing_catalog FOREIGN KEY (ingredient_code) REFERENCES catalog_ingredients(code)`. ⚠️ FK trỏ **`code`**, **không** trỏ `id`: cột `dish_ingredients.ingredient_code` đã merge là `VARCHAR(64)` chứa mã, không phải UUID — đổi kiểu cột đó = sửa `V8` đã merge = Flyway đỏ. Điều kiện: **mọi** `ingredient_code` trong `V9__seed_mf05.sql:42` phải có bản ghi tương ứng, thiếu một mã là `ALTER` fail (`M06-TC-INT-047`) |

**D. Còn lệch D1/D9/§0-BIS (đếm máy, sửa trong Bước 1 của từng module)**

| Module | PK `VARCHAR` | Package sai | Cột audit tự khai |
|---|---|---|---|
| ~~MF06~~ | ~~6~~ ✅ | ~~`com.sfms.inventory`~~ ✅ | ~~1~~ ✅ — **cả 3 cột đã sửa 2026-08-03** (`M06-D01`/`M06-D13`/`M06-D02`). Cột audit là ca đắt nhất: `issue_slips.created_by UUID` đụng tên reserved của `BaseEntity` ⇒ **duplicate column, build đỏ ngay**, trong khi lệch PK chỉ sai hợp đồng mà vẫn chạy ⇒ **rà cột audit TRƯỚC** |
| ~~MF08~~ | ~~—~~ ✅ | ~~—~~ → **`com.sfms.sample`** ✅ | ~~—~~ → **4 bảng** ✅ — **cả 2 ô ghi `—` đều SAI, sửa 2026-08-03** (`M08-D02`/`M08-D11`). Cột audit: cả 4 bảng tự khai `created_at`/`updated_at`, `sample_attachments` thêm `uploaded_at`/`uploaded_by` — **lần thứ hai bảng này đếm sót cột audit**, lý do khác MF07: MF08 khai ở **cả** khối ```java **lẫn** ```sql, nhưng grep đếm theo `CREATE TABLE` **của từng bảng** nên 4 lần khai gộp thành 0. Cột package: `TDS-MF08` **không nhắc package lần nào** ⇒ grep sạch, trong khi **`TestSpec-MF08` ghi `com.sfms.sample` ở 40 chỗ**. ⇒ **Lệnh của bảng D phải quét CẢ TestSpec, không chỉ TDS.** |
| ~~MF07~~ | ~~6~~ ✅ | ~~`com.sfms.inspection`~~ ✅ | ~~—~~ → **3** ✅ — **cả 3 cột đã sửa 2026-08-03** (`M07-D01`/`M07-D05`/`M07-D02`). ⚠️ **Ô "Cột audit tự khai" ghi `—` là SAI**: `inspection_sessions`, `kitchen_inspections`, `violation_records` đều tự khai `created_at`/`updated_at`. Bảng này đếm bằng grep trong khối `CREATE TABLE`, mà MF07 khai chúng **ở tầng Entity trước, DDL sau** ⇒ lần đếm đầu trượt. **Bài học: đếm cả `@Column(name = "created_at")` trong khối ```java, không chỉ trong khối ```sql.** |
| ~~MF10~~ | ~~—~~ ✅ *(đúng — cả 9 bảng PK `UUID`; **nhưng KHÔNG bảng nào có cột `code`** trong khi §9.2 trả `"id": "FB-8809"` — cột này không đo được lệch đó)* | ~~`com.sfms.feedback`~~ → **3 package**: `com.sfms.feedback` + **`com.sfms.announcement`** + **`com.sfms.notification`** ✅ | ~~—~~ → **5 chỗ** ✅ — **cả 3 ô đều thiếu hoặc sai, sửa 2026-08-03** (`M10-D01`/`M10-D03`/`M10-D02`). ⚠️ **Đây là lần thứ TƯ ô "Cột audit tự khai" sai** (MF07, MF08, MF09, nay MF10) và lần này sai vì **lý do thứ tư**: MF10 khai `created_at`/`updated_at` ở **cả** khối ```java **lẫn** khối ```sql, cùng 5 bảng — grep đếm theo `CREATE TABLE` của từng bảng nên 10 lần khai gộp thành 0. Ô "Package sai" cũng **đếm thiếu 2/3**: danh sách cứng `inventory\|inspection\|feedback\|incident\|platform` bắt được `feedback` nhưng **không** bắt `announcement` và `notification`. ⇒ **Kết luận sau 4 lần: bảng D không đáng tin ở BẤT KỲ ô nào. Tự grep, và grep cả `code VARCHAR` để bắt lệch D1 mà cột "PK `VARCHAR`" bỏ sót.** |
| ~~MF11~~ | ~~—~~ ✅ *(đúng — cả 11 bảng PK `UUID` + `code`)* | ~~`com.sfms.incident`~~ ✅ | ~~—~~ ✅ *(đúng — `created_by_id`/`recorded_by_id`/`quarantined_by_id`/`closed_by_id`/`handled_by_id` là tên **nghiệp vụ riêng**, không đụng cột reserved của `BaseEntity`)* — **sửa 2026-08-04** (`M11-D04`). ⚠️ **Lần đầu bảng D đúng cả 3 ô**, sau 4 lần sai liên tiếp (MF07/08/09/10). Nhưng cột "PK `VARCHAR`" vẫn **không đo được** thứ thật sự hỏng ở MF11: bảng `code_sequences` **trùng + sai cấu trúc** (nhóm A) và **8 port trong đó 4 là viết lại `ReferenceResolver`, 1 trùng cửa duy nhất của MF10** (`M11-D07`) — **cả hai lớp lỗi này bảng D không có cột nào bắt được** |
| ~~MF12~~ | ~~8~~ ✅ | ~~`com.sfms.platform`~~ ✅ | ~~1~~ ✅ — **cả 3 ô đã sửa 2026-08-04** (`M12-D03`/`M12-D11`/`M12-D05`) và **cả 3 đều ĐÚNG** — lần thứ hai bảng D đúng trọn vẹn, sau MF11. Cột audit: `external_integrations.updated_at`/`updated_by` **đúng là** cột reserved của `BaseEntity`; `updatedBy` mà UC-040 hiển thị chính là cột đó do `AuditorAware` điền, **không** phải FK người dùng như TDS khai. ⚠️ **Nhưng bảng D vẫn không đo được thứ đắt nhất ở MF12:** 4 bảng + 4 endpoint thuộc module khác (`M12-D01`/`M12-D02`), 2 interface viết lại hạ tầng đã merge (`M12-D08`/`M12-D09`), tiền tố mã lỗi sai (`M12-D10`), và một lệnh `REVOKE` trỏ role **không tồn tại** (`M12-D13`). **7 lớp lỗi, 0 cột nào của bảng D bắt được** |
| ~~MF09~~ | ~~—~~ ✅ *(đúng — cả 4 bảng PK `UUID`)* | ~~—~~ → **tên cũ ở 18 chỗ** ✅ | ~~—~~ → **2 entity** ✅ — **cả 2 ô ghi `—` đều SAI, sửa 2026-08-03** (`M09-D03`/`M09-D02`). ⚠️ **Đây là lần thứ BA bảng này sai** (MF07, MF08, nay MF09) và lần này sai vì **lý do thứ ba**: grep cột "Package sai" khớp theo **danh sách cứng** `inventory\|inspection\|feedback\|incident\|platform` — MF09 dùng một tên **không có trong danh sách đó** nên lọt. Cột audit lọt vì `StudentDistribution`/`ServingDish` khai `created_at` ở **tầng Entity** (`@Column(name="created_at")`), giống hệt ca MF07. ⇒ **Lệnh của bảng D phải grep `created_at\|updated_at\|created_by\|updated_by` trong CẢ khối ```java lẫn ```sql, và grep package bằng `com\.sfms\.(?!mf\d\d\|common)` chứ không bằng danh sách cứng.** |
| MF13 | — | — | — | *(grep ra sạch cả 3 cột — **nhưng 3/3 module gần nhất đều có ô sai**; tự grep lại, đừng tin dòng này)* |

> MF01–MF04 cũng "CREATE TABLE bảng đã tồn tại" khi grep, nhưng đó là **bảng của chính họ** — không tính.

### §2.2 — MF12 (Thảo) — ✅ **ĐÃ CHẠY XONG Bước 0 + Bước 1 ngày 2026-08-04**

> **Trạng thái:** `TDS-MF12_Backend.md` v1.2 và `TestSpec-MF12_AI_Supporting_Services.md` v1.1 đều **Status: Approved**. 14 quyết định `M12-D01`…`M12-D14`. Mục này giữ nguyên phần dự báo viết ngày 03/08 (Policy 4.4) và ghi kết quả đo được vào cột cuối.
>
> #### Dự báo của mục này chính xác tới đâu — đối chiếu sau khi chạy thật
>
> | | Số mục | Ghi chú |
> |---|---|---|
> | Dự báo **đúng** | **6/6** ở bảng dưới + **3/3** nhóm C | Cả 6 đều là lỗi thật, không có dương tính giả |
> | Dự báo **thiếu** | **5 lớp lỗi nữa** | Xem bảng ngay dưới |
>
> **Năm thứ mục này KHÔNG dự báo được — và vì sao:**
>
> | # | Lỗi bỏ sót | Hậu quả nếu để nguyên | Vì sao §2.2 không thấy |
> |---|---|---|---|
> | 1 | **`/api/v1/checklist-groups` đã có controller** ở `mf07/web/ChecklistGroupController` — trùng **chính xác** 4 endpoint §9.2 hàng 17–20 | **App không khởi động** | §2.2 viết 03/08, `M07-D10` merge sau đó. Nó chỉ nói về **bảng** `checklist_*`; vế **endpoint** chỉ hiện ra khi chạy lệnh grep path của Bước 0 |
> | 2 | **`REVOKE UPDATE, DELETE … FROM sfms_app`** — role `sfms_app` **chưa bao giờ tồn tại** | **Flyway đỏ**, và `TC-INT-003` của TestSpec **đỏ vĩnh viễn** | Thông tin này chỉ có trong `V1`/`application.yml`. **Không tài liệu nào từng viết ra giả định "dự án có role DB riêng"** — nó ngầm nằm trong một lệnh SQL |
> | 3 | **`AiAssistPort` + `AuditLogPort`** — cả hai đã có trong code (`mf05/port/AiAssistPort` do MF05 khai hộ; `iam/service/AuditService` 11 module đang gọi) | Hai interface cùng tên; MF05 **vẫn trả `503 MENU-073`** vì bean không khớp kiểu | Đây là mục **6 của prompt Bước 0** *(thêm 03/08 khi làm MF08)*, không phải mục nào của §2.2 |
> | 4 | **Tiền tố mã lỗi `SYS-` không tồn tại** — `ModuleCodeResolver` đặt chỗ **`AIS`** cho MF12, và `audit-logs`/`security-policies` **đã** → `IAM` | §10 mô tả hành vi **không thể xảy ra** (resolver tra theo segment đầu của path) | §2.2 mục 2 hỏi đúng câu *"`IAM-` hay `AIS-`?"* nhưng gọi nó là *"quyết định 5 phút cần chốt với Nam"*. **Câu trả lời đã nằm trong code từ trước** — không cần chốt, cần đọc |
> | 5 | **`V16`/`V17` lạc hậu** → `V22`/`V23` | Flyway checksum mismatch | §2.2 không nhắc số V |
>
> **Điểm chung của cả 5:** không cái nào rút ra được bằng cách **đọc tài liệu kỹ hơn**. Cả 5 đều cần **mở file trong `04_Development/Backend` ra đọc** — đúng vế mà §1 của file này gọi là *"lệch hiện thực"*.
>
> **Hai mục §2.2 xếp là "cần chốt CHỦ" thì cả hai đều tự giải, không cần họp:**
>
> | Mục treo | Cách nó khép lại | |
> |---|---|---|
> | `sent_reports` — *"CF-M11-20 kết luận thuộc MF13. **Nam + Huy** chốt một lần cho cả hai module"* | `TDS-MF13_Backend.md` **ADR-M13-2** đã tự gỡ: *"MF13 **không** sở hữu `sent_reports`; sở hữu `report_exports` + `report_schedules`"*, và `CF-M13-1` truy nguyên đủ 4 nguồn để kết luận theo traceability §1.12 = MF12 | ✅ `M12-D07` |
> | `checklist_*` — *"Chốt **một chủ** (MF07 hay MF12)"* | MF07 **đã merge cả bảng lẫn controller** (`M07-D10`) — code đã trả lời trước khi có ai họp | ✅ `M12-D02` |
>
> **Bài học cho 1 module còn lại (MF13):** hai mục trên treo suốt từ 30/07 tới 04/08 chờ hai người ngồi lại, trong khi **câu trả lời đã nằm sẵn** — một trong `TDS-MF13` (viết 30/07), một trong code (merge 03/08). **Chạy Bước 0 trước khi lên lịch họp.**

---

Sáu điểm dưới đây là bản dự báo viết ngày 2026-08-03, **giữ nguyên văn**; cột "Kết quả" thêm 04/08:

| # | Chỗ sai trong TDS-MF12 | Thực tế trong code đã merge | Phải làm | **Kết quả 2026-08-04** |
|---|---|---|---|---|
| 1 | §5.2 `CREATE TABLE audit_logs (id VARCHAR(32) PRIMARY KEY, actor_user_id VARCHAR(32) REFERENCES users(id), …)` | `V1__init_schema.sql:278` — **`id UUID`**, cột **`actor VARCHAR(180)`** (không phải FK), đã có `code VARCHAR(32)`. Entity `iam/domain/AuditLog`, service `iam/service/AuditService` **đang ghi vào bảng này** | **Bỏ khối `CREATE TABLE audit_logs`.** Thiếu cột thì `ALTER`. ⓘ Nam đã để sẵn comment ở `V1:280`: *"`code VARCHAR(32)` — D1 (tùy chọn, **MF12 sẽ dùng**)"* — tức là đã tính trước cho MF12 **đọc**, không phải tạo lại | ✅ `M12-D01`. Thêm phát hiện: cột nhãn actor tên thật là **`actor`** (không phải `actor_label`), `action` là `VARCHAR(255)` (không phải 500 — **giữ 255**, nới cột đang có dữ liệu là sửa hợp đồng MF01), và 3 cột `ALTER ADD` phải **nullable** |
| 2 | `GET /api/v1/audit-logs` (UC-106) | Path chưa có controller, **nhưng** `ModuleCodeResolver` đã gán `audit-logs → IAM` (prefix lỗi của MF01) | Chốt với **Nam**: MF12 mở controller đọc thì prefix lỗi là `IAM-` hay `AIS-`? Một trong hai, không được cả hai. Đây là quyết định 5 phút, nhưng để mở thì mã lỗi trả về sẽ mâu thuẫn với `ModuleCodeResolver` | ✅ `M12-D10` — **không cần chốt, code đã trả lời**: `audit-logs`/`security-policies` **đã** → `IAM` (dòng 32–33), và comment khối `static` đặt chỗ *"(**AIS**, RPT)"* cho MF12/MF13. ⇒ **`IAM-` cho 2 resource của MF01, `AIS-` cho 5 resource của MF12**; chỉ THÊM 5 dòng, không sửa dòng nào. Tiền tố `SYS-` mà TDS tự đặt **không tồn tại** — 63 chỗ trong TDS + 92 chỗ trong TestSpec đã đổi |
| 3 | **5 chỗ** `REFERENCES users(id)` (`ai_conversations`, `ai_requests`, `audit_logs`, `external_integrations`, `sent_reports`) | Bảng tên **`user_accounts`**, PK **UUID** | Đổi `UUID REFERENCES user_accounts(id)`. Bảng `users` **chưa bao giờ tồn tại** — migration sẽ fail ở lệnh đầu tiên | ✅ `M12-D04`. **Rộng hơn dự báo** — đúng cảnh báo C2 *"phải rà cả KIỂU của mọi FK ngoại module"*: ngoài 5 `users(id)`, còn **2 `REFERENCES schools(id)` khai `VARCHAR(32)`** trong khi `schools.id` là `UUID`. Một trong hai nằm ở `checklist_groups` nên tự biến mất cùng `M12-D02` |
| 4 | Mọi PK là `VARCHAR(32)` = mã nghiệp vụ | **D1** — PK `UUID` + cột `code` + `@JsonProperty("id")` | Giống hệt việc MF05 vừa làm (`M05-D01`) — chép cách làm ở `TDS-MF05_Backend.md` §5.2 | ✅ `M12-D03` — 8 bảng khai PK `VARCHAR(32)`, còn **5** sau khi gỡ `audit_logs` + `checklist_*`; cả 5 đổi `UUID` + `code`. ⚠️ `external_integrations` là ngoại lệ `UNIQUE(code)` **không** `UNIQUE(school_id, code)` (CF-7 — cấu hình cấp nền tảng, không có `school_id`) |
| 5 | `external_integrations.updated_by VARCHAR(32) REFERENCES users(id)` | **D9** — `updated_by` là **tên cột reserved** của `BaseEntity` (`VARCHAR(180)`, email actor, tự điền) | Đổi tên thành `reviewed_by_user_id`/`configured_by_user_id` nếu là FK nghiệp vụ; nếu chỉ là audit thì **bỏ**, kế thừa `BaseEntity`. D9 gọi đích danh MF12 | ✅ `M12-D05` — **là audit, không phải FK nghiệp vụ**: `updatedBy` mà màn UC-040 hiển thị chính là cột `updated_by VARCHAR(180)` do `AuditorAware` điền (email actor). Gỡ khỏi entity, **DDL vẫn tạo đủ 4 cột** |
| 6 | `package com.sfms.platform.*` | §0-BIS — khuôn `com.sfms.mf<NN>` | `com.sfms.mf12` | ✅ `M12-D11` — 2 chỗ trong TDS + 15 chỗ trong TestSpec |

**Hai chỗ chồng lấn cần chốt CHỦ, không chỉ sửa cú pháp:**

- ~~`sent_reports` — `TDS-MF12` §5.2 tạo bảng này, nhưng **CF-M11-20 đã kết luận nó thuộc MF13**. **Nam + Huy** chốt một lần cho cả hai module.~~ ✅ **KHÉP LẠI 2026-08-04 (`M12-D07`), không cần họp:** `TDS-MF13_Backend.md` **ADR-M13-2** đã tự gỡ khỏi phạm vi MF13 từ 30/07, và `CF-M13-1` truy đủ 4 nguồn rồi kết luận theo traceability §1.12 ⇒ **MF12 giữ `sent_reports`**; MF13 sở hữu `report_exports` + `report_schedules` — hai khái niệm khác. **CF-M11-20 đóng.**
- ~~`checklist_groups` / `checklist_items` — chốt **một chủ** (MF07 hay MF12), người còn lại `ALTER` hoặc chỉ đọc.~~ ✅ **KHÉP LẠI 2026-08-04 (`M12-D02`), không cần họp — MF07 đã merge cả bảng lẫn controller.** Chẩn đoán về `IF NOT EXISTS` **đúng nhưng chưa đủ**: hậu quả thật không dừng ở "hai schema khác nhau mà Flyway vẫn xanh", nó là **app không khởi động** vì `mf07/web/ChecklistGroupController` đã chiếm đúng 4 verb trên cùng path. MF12 gỡ 2 khối DDL + 4 endpoint + 1 service + 1 repository + 4 TC.

**Thêm 3 việc từ nhóm C ở §2.1** (MF12 dính cả ba):

- **C1** — mã của MF12 có năm (`AIC-2026-0007`, `AIQ-2026-0042`) ⇒ dùng `scope = "mf12_<resource>:<nam>"` theo tiền lệ MF03/MF05/MF11, **không** de xuat sua bang `code_sequences`. ✅ `M12-D14`. ⚠️ **`ERR-5521` bi liet nham vao day: no KHONG co nam** ⇒ scope phang `"mf12_error_logs"`. Va `external_integrations.code` (`gps`/`gmail`/`firebase`/`gemini`) la ma co dinh, **khong qua `CodeGenerator`**.
- **C2** — 5 chỗ `REFERENCES users(id)` (đã nằm ở mục 3 bảng trên). ✅ `M12-D04` — **và thêm 2 FK sai kiểu tới `schools`**, đúng như C2 mở rộng cảnh báo.
- **C3** — không liên quan MF12.

**Ba việc phát sinh cho Bước 2 của MF12** *(ghi ở đây để không rơi)*:

1. **`mf07/port/ComplianceDataPort` chưa tồn tại.** §2.1 A-BIS ghi `M07-D09` *"MF07 gỡ `/compliance-report`, expose `ComplianceDataPort`"* — nhưng `ls mf07/` ra `domain dto error repository service web`, **không có `port/`**, và grep `ComplianceDataPort` toàn backend ra **0**. MF12 cần nó cho BR-209/BR-214. **Không phải việc chốt với ai** — MF07 và MF12 cùng chủ (Thảo); thêm file mới vào module đã merge là hợp §5 *"chỉ thêm dòng"*.
2. **Bean `AiAssistPort`** — implement `com.sfms.mf05.port.AiAssistPort` trong `com.sfms.mf12`, **không** tạo interface mới. Đây là việc **4 module đang chờ gián tiếp**: MF05 hiện trả `503 MENU-073` cho `POST /menus/{code}/ai-suggest` chỉ vì bean này chưa có. `MF12-TC-INT-022` đo trước/sau.
3. **`AuditService` mở rộng** (chuỗi hash BR-172 + điền `actor_user_id`) — sửa **bên trong**, **không đổi chữ ký** 2 method công khai ⇒ 11 module đang gọi không phải sửa dòng nào.

> ~~Thảo giữ cả MF05, MF07, MF12 ⇒ ba module này đụng nhau nhiều nhất… Đề nghị rà **chung một buổi**.~~
>
> **Nhận định đúng, đề nghị thì không cần thiết — và đây là điều đáng ghi lại.** Ba chỗ đụng nhau mà mục này liệt kê đều đã tự giải, **không** qua buổi rà chung nào:
> - `checklist_*` MF07↔MF12 → MF07 làm xong trước, code trả lời (`M12-D02`);
> - `AiAssistPort` MF05↔MF12 → MF05 **khai hộ interface** ngay ở Bước 2 của mình kèm `Optional<>` + fallback `503`, để MF12 chỉ việc thả bean vào (`M12-D09`);
> - C2 → MF07 sửa phần của mình 03/08, MF12 sửa phần của mình 04/08, không phần nào chặn phần nào.
>
> **Cái làm ba module đụng nhau ít đi không phải là họp chung, mà là mỗi module chạy Bước 0 đúng lượt và ghi quyết định vào TDS của chính mình.** Ca `AiAssistPort` của MF05 là mẫu tốt nhất trong dự án tới nay: nó **để lại một chỗ trống có hình dạng rõ ràng** cho module chưa tồn tại, kèm hành vi fallback không chặn nghiệp vụ. So sánh với `ComplianceDataPort` — được hứa trong `M07-D09` nhưng không ai viết ra, nên MF12 phát hiện lúc Bước 0 và phải tự làm ở Bước 2.

---

### §2.3 — MF13 (Nam) — ✅ **ĐÃ CHẠY XONG Bước 0 + Bước 1 ngày 2026-08-04** — **module cuối cùng, 13/13**

> **Trạng thái:** `TDS-MF13_Backend.md` **v1.1** và `TestSpec-MF13_Reporting_Analytics.md` **v1.1** đều **Status: Approved**. 13 quyết định `M13-D01`…`M13-D13` (chi tiết ở `TDS-MF13` **§2.4**). Số V cấp: **`V25`/`V26`** (`V24__mf12_seed_integrations.sql` là số cao nhất hiện có).
>
> **Bước 0 sạch tuyệt đối về quyền sở hữu** — 3 bảng + 9 path đều chưa ai chiếm, FK đúng cả tên lẫn kiểu (`user_accounts(id)` UUID, `schools(id)` UUID), package đúng `com.sfms.mf13`, PK `UUID` + `code` đúng D1. Đây là module **duy nhất trong 9 module rà** không dính nhóm A, A-BIS hay B.
>
> **Và đó chính là lý do nó đáng ghi lại:** *"sạch Bước 0"* vẫn để lọt **3 lỗi làm Flyway/build đỏ ngay** — đúng cảnh báo §7 Bước 0 việc 7 (viết ra từ ca MF08).

| # | Lỗi | 5 lệnh Bước 0 có bắt được không? | Hậu quả |
|---|---|---|---|
| 1 | §5.2 ghi *"thêm 3 bảng vào `V1__init_schema.sql`, Nam giữ"* | ❌ — không lệnh nào đọc **câu văn** trong TDS | **Flyway checksum mismatch trên máy cả nhóm** (`V1` đã merge, 12 module đã chạy qua) |
| 2 | Cột audit D9: 2 entity tự khai `@Column("created_at"/"updated_at")`, 3 DDL thiếu `created_by`/`updated_by` | ❌ — và **bảng D của §2.1 cũng ghi `—`** cho MF13 | **Build đỏ ngay** (duplicate column với `BaseEntity`) |
| 3 | **7/7 view `v_rpt_*` đọc 23 cột không tồn tại** | ❌ — grep bắt `CREATE TABLE`/`@RequestMapping`, không bắt `SELECT` | `flyway:migrate` đỏ ở view đầu tiên; 12 IT phía sau vô nghĩa |

**Ba việc đáng rút ra cho lần rà tiếp theo (nếu có module mới):**

1. **Bảng D của §2.1 ghi MF13 `—` cả 3 cột và lần này "đúng" — nhưng vẫn bỏ sót lỗi số 2.** Nó đúng ở nghĩa hẹp (PK không `VARCHAR`, package không sai) và trượt ở cột "Cột audit tự khai" vì MF13 khai chúng ở **tầng Entity** — **đúng lý do đã làm bảng D trượt ở MF07 và MF09**. ⇒ Ghi chú *"3/3 module gần nhất đều có ô sai, tự grep lại"* ở dòng MF13 là **đúng và nên giữ nguyên cho mọi dòng**, kể cả những dòng đã tick ✅.

2. **Module read-only cần một lệnh Bước 0 thứ 6 mà quy trình chưa có.** MF13 không ghi vào bảng nào của ai, nên 2 lệnh về quyền sở hữu ra sạch — trong khi lớp lỗi thật của nó nằm ở chiều **đọc**: view/native query trỏ cột module khác. Đề nghị thêm vào prompt §7 Bước 0:
   ```bash
   # Module có VIEW / @Query(nativeQuery) đọc bảng module khác:
   # dump cột thật của MỌI bảng nguồn, đối chiếu từng cột một.
   psql -c "\d <bảng_nguồn>"     # hoặc: awk "/CREATE TABLE <bảng> \(/,/^\);/" db/migration/*.sql
   ```
   Chi phí: ~10 phút. Chi phí nếu bỏ: `flyway:migrate` đỏ ở CI, và người sửa phải dò 23 cột.

3. **Ca `partners.school_id` là loại lệch nguy hiểm nhất, và nó không giống 22 cột kia.** 22 cột còn lại sai tên ⇒ **migrate đỏ ngay**, ai cũng thấy. Còn `v_rpt_supplier_cert` `SELECT p.school_id FROM partners p`: `partners` là bảng **cấp nền tảng, cố ý không có `school_id`**. Nếu ai đó "sửa cho chạy" bằng cách **bỏ cột đó khỏi view**, migrate xanh, test xanh, và **BR-176 mất chỗ thi hành** — mọi trường thấy nhà cung cấp của nhau. Đường đúng (`M13-D13`) là đi qua `supplier_partnerships`. ⇒ **Khi một cột lọc phạm vi (`school_id`, `partner_id`) không tồn tại ở bảng nguồn, đó là câu hỏi THIẾT KẾ, không phải lỗi chính tả** — đừng gỡ cột, đi tìm bảng nối.

**Ba mục treo từ 30/07 — cả ba tự khép, không cần cuộc họp nào:**

| Treo | Được giao cho | Câu trả lời nằm sẵn ở đâu | |
|---|---|---|---|
| `CF-M13-1` / **`CF-M11-20`** — ai sở hữu `sent_reports`? | Huy + Nam | `V23:115` + `mf12/web/SentReportController` **đã merge** (03–04/08) | `M13-D03` |
| **Cross-school 403 hay 404** — *"chốt một lần cho cả 13 module"*, treo từ `TestSpec-MF02` **L7** | Nam + Huy | `mf02/error/SchExceptions.java:67,73` — **đã chốt 2026-07-31** và đang chạy: cross-school ⇒ **404**, 403 chỉ cho sai vai trò cùng trường | `M13-D04` |
| `L11` — tên cột thật cho 7 view, *"phụ thuộc lịch của 5 chủ module, Mạnh điều phối"* | Mạnh | **cả 5 module nguồn đã merge** ⇒ không còn lịch để điều phối, chỉ còn đọc file | `M13-D05` |

> **Đây là lần thứ ba liên tiếp** (MF11 → MF12 → MF13) một mục *"chờ hai người ngồi lại"* tan ra ngay khi chạy Bước 0. Riêng ca **cross-school** đáng chú ý nhất: câu trả lời có từ **31/07**, tức **trước** ngày cả `TDS-MF13` lẫn `TestSpec-MF13` được viết — hai tài liệu ghi nó là ⛔ chỉ vì không ai mở `mf02/error/` ra đọc. **Quy tắc §2.2 rút ra sau MF12 — *"chạy Bước 0 trước khi lên lịch họp"* — nay có bằng chứng thứ ba, và nên nâng thành luật ở §7.**

**Việc còn treo của MF13 — không mục nào chặn Bước 2:**

| Việc | Ai | Vì sao không chặn |
|---|---|---|
| Service Caterers có là Actor của UC-171/172? (`CF-M13-3`/`L5`) | Huy | `ReportAccessPolicy` đã thiết kế sẵn nhánh, bật là **1 dòng config** |
| UC-172 có yêu cầu chặn ẩn khi kỳ rỗng? (`L2`) | Huy | BR-178 nguyên văn **không có** điều khoản chặn (`M13-D10`) |
| UC-173 là stub — 0 màn FE, 0 resource mock (`CF-M13-2`) | Huy (UC spec) + Minh (màn) | Backend đặc tả đủ, đánh dấu *"chưa nghiệm thu qua FE"* ở §11.4 |
| Nối FE: 7 `DataTable` đọc thẳng resource module khác + chuỗi tên trường cứng (`CF-M13-6`/`L8`) | Minh | Bước 3 |
| `data_model.puml` chưa có 3 bảng MF13 (`CF-M13-5`) | Mạnh/Huy | Tiền lệ `IssueSlip` của ADR-M06-6 |

---

## §3 — Trạng thái (kiểm 2026-08-02, sau đợt nối MF03 · cập nhật MF05 + MF06 2026-08-03)

> ⚠️ **Sửa 2026-08-03 (Huy, khi làm Bước 0 của MF06):** cột **Backend** của **MF04 và MF05** đang ghi `—` là **sai** — cả hai đã merge. Bằng chứng: `ls db/migration/` ra `V6__mf04_supplier_partner.sql`, `V7__seed_mf04.sql`, `V8__mf05_menu_diet.sql`, `V9__seed_mf05.sql`; `ls src/main/java/com/sfms/` ra `mf04/`, `mf05/`; và `grep '"/api/v1/'` ra `/partners`, `/supplier-documents`, `/delivery-persons`, `/partnership-applications`, `/supplier-partnerships`, `/menus`, `/dishes`, `/menu-templates`. Bảng này viết 02/08 và **không được cập nhật khi MF04/MF05 merge** — đúng lớp lỗi mà chính file này cảnh báo. **Ai đọc bảng để quyết định số V hoặc quyền sở hữu resource: chạy 2 lệnh Bước 0, đừng tin bảng.**

| Module | Người | TDS | TestSpec | Backend | Frontend nối thật |
|---|---|---|---|---|---|
| MF01 User/Access | Nam | Approved | Approved | ✅ | ✅ `auth account profile users staff roles permissions` |
| MF02 School/Class | Nam | Approved | Approved | ✅ | ✅ `schools classrooms academic-years` |
| MF03 Student/Parent | Huy | Approved | Approved | ✅ *(+ `POST /students/{code}/health-documents` 04/08, xem §8)* | 🟡 3/5 resource: `special-meals`/`allergy-declarations`/`chronic-conditions` ✅ (2 resource mới bật 04/08, đợt thứ ba — viết lại `medical-staff/health-reviews` để tách khỏi `students`) · `students`/`health-history` còn mock, xem §8 |
| MF04 Supplier/Partner | Mạnh | Draft | Draft | ✅ *(`V6`/`V7`, `com.sfms.mf04`)* | — |
| MF05 Menu/Diet | Thảo | **Approved** *(2026-08-03)* | **Approved** *(2026-08-03)* | ✅ *(`V8`/`V9`, `com.sfms.mf05`)* | — |
| MF06 Ingredient/Inventory | Huy | **Approved** *(2026-08-03)* | **Approved** *(2026-08-03)* | — *(số V đã cấp: `V10`/`V11`)* | — |
| MF07 Food Safety Inspection | Thảo | **Approved** *(2026-08-03)* | **Approved** *(2026-08-03)* | ✅ *(`V12`/`V13`, `com.sfms.mf07`, 10/10 IT `mvn test` pass)* | 🟡 `checklist-groups safety-signoffs` ✅ · `inspections`/`kitchen-inspections` còn mock (lệch kiến trúc GHI, xem `real-resources.ts`) |
| MF08 Food Sample Retention | Mạnh | **Approved** *(2026-08-03)* | **Approved** *(2026-08-03)* | ✅ *(`V14`/`V15`, `com.sfms.mf08`, 1 IT `mvn test` scoped pass, 192/192 unit test toàn project không regression)* | 🟡 `receiving-authorities` ✅ · `samples`/`sample-lab-requests` còn mock (phụ thuộc `inspections`/MF11, xem `real-resources.ts`) |
| MF09 Meal Serving/Distribution | Minh | **Approved** *(2026-08-03)* | **Approved** *(2026-08-03)* | ✅ *(`V16`/`V17`, `com.sfms.mf09`, boot+Flyway+Hibernate-validate pass; `MenuPublished` sinh `student_distributions` tự động qua `DistributionPlanHandler`)* | 🟡 `meal-records` ✅ · `student-distributions`/`allergy-alerts`/`meal-history`/`special-meal-records` còn mock (verb+field+role lệch, xem `real-resources.ts`) |
| MF10 Feedback/Announcement | Mạnh | **Approved** *(2026-08-03)* | **Approved** *(2026-08-03)* — ⚠️ ô cũ ghi *"thiếu TestSpec"* là **SAI**: `TestSpec-MF10_Feedback_Announcement_Management.md` **có tồn tại** (2.100 dòng, 52 TC, viết 02/08), nay 54 TC | ✅ *(`V18`/`V19`, `com.sfms.mf10`, 4/4 IT `mvn verify` pass — MF10-TC-CON-001/002 D11/D12 + flow chính; 4/54 TC TestSpec)* | 🟡 `announcements notification-preferences` ✅ · `feedbacks`/`notifications` còn mock (luồng chọn bữa theo mã + `POST` không tồn tại/taxonomy lệch dùng chung 8 màn ngoài MF10, xem `real-resources.ts`) |
| MF11 Incident/Emergency | Minh | **Approved** *(2026-08-04)* | **Approved** *(2026-08-04)* | — *(số V đã cấp: `V20`/`V21`)* | — |
| MF12 AI Supporting | Thảo | **Approved** *(2026-08-04)* | **Approved** *(2026-08-04)* | — *(số V đã cấp: `V22`/`V23`)* | — |
| MF13 Reporting | Nam | **Approved** *(2026-08-04)* — v1.1. ⚠️ ô cũ ghi *"chưa viết được (treo CF-M11-20)"* là **SAI**: `TDS-MF13_Backend.md` **có tồn tại** (viết 30/07). Phát hiện khi làm Bước 0 của MF12 | **Approved** *(2026-08-04)* — v1.1, 40 TC | — *(số V đã cấp: `V25`/`V26`)* | — |

### Hạ tầng dùng chung đã thêm 02/08/2026

| Thứ | Ở đâu | Dùng làm gì |
|---|---|---|
| `npm run gen:api` | `04_Development/Frontend/package.json` | Sinh `src/lib/api/schema.d.ts` từ `/v3/api-docs` |
| `schema.d.ts` | `src/lib/api/` | Type backend thật — **commit vào repo**, FE không cần backend chạy mới build |
| `types.ts` | `src/lib/api/` | Alias gọn: `Api<"SpecialMealRequestResponse">` |
| `contract-check.ts` | `src/lib/api/` | **Guard biên dịch** — type FE lệch DTO ⇒ `tsc` gãy, in thẳng tên field lệch |
| `seed-read-guard.ts` | `scripts/` | Đã mở rộng quét `src/lib` (trước chỉ `src/app` + `src/components`) |

Guard `contract-check.ts` mạnh cỡ nào — thử với `Student` (resource chưa nối):

```
Type 'true' is not assignable to type
  '"medicalConditions" | "medications" | "emergencyContact" | "medicalDocuments"'
Type 'true' is not assignable to type
  '"name" | "healthStatus" | "parent" | "medicalConditions" | ...'
```

Đúng những field mà đợt MF03 phải mất hai phiên curl mới tìm ra.

---

## §4 — Chia sóng (giữ nguyên bản nháp)

| Sóng | Module | Người | Vì sao |
|---|---|---|---|
| **1 — bắt đầu ngay, song song** | MF04 Supplier/Partner | Mạnh | Nền tảng — MF06/08 tham chiếu tới |
| | MF06 Ingredient/Inventory | Huy | Chỉ cần thống nhất TRƯỚC với Mạnh về mã nhà cung cấp trong lô hàng (1 buổi ngắn) |
| | MF05 Menu/Diet | Thảo | Phụ thuộc MF02 (Approved) — độc lập với MF04/06 |
| **2 — sau khi Sóng 1 chốt tài liệu** | MF08 Food Sample | Mạnh | Phụ thuộc MF05 |
| | MF07 Food Safety Inspection | Thảo | Phụ thuộc MF06 |
| | MF09 Meal Serving/Distribution | Minh | Phụ thuộc MF02/03 (xong) + MF05 |
| **3** | MF10 Feedback/Announcement | Mạnh | Viết TestSpec trước (đang thiếu hẳn) |
| | MF11 Incident/Emergency | Minh | Phụ thuộc MF09 |
| | MF12 AI Supporting | Thảo | Cross-cutting |
| **4 — cuối cùng** | MF13 Reporting | Nam | Chốt CF-M11-20 với Huy trước |

> 4 việc còn treo của MF03 (§8) **không chặn Sóng 1**. Bắt đầu ngay được.

---

## §5 — Quy tắc làm song song (đã bổ sung)

**Chỉ được THÊM DÒNG, không refactor** — áp cho: `common/`, `real-resources.ts`, `mock-server.ts`, `ModuleCodeResolver`, và **thêm mới**: `src/lib/api/contract-check.ts`, `KNOWN_SEED_READS` trong `scripts/seed-read-guard.ts`.

⚠️ **`common/dto/PagedResponse.java` — KHÔNG đặt lại `name=` cho `@Schema`.** Đặt `name` sẽ ghim một tên schema duy nhất, springdoc dùng chung nó cho mọi tham số hoá và `items` tụt xuống `{}`, làm hỏng toàn bộ type sinh ra cho **cả 11 endpoint danh sách**. File có chú thích giải thích tại chỗ — đừng "dọn dẹp" nó.

**Bước 3 tự nó đụng màn của người khác** (vế còn thiếu ở bản nháp). Một resource thường có 5–20 màn tiêu thụ, trải nhiều vai trò: nối `special-meals` của MF03 buộc phải sửa `medical-staff/special-meals` và `parent/special-meal-requests` — cả hai thuộc Minh.
⇒ **Báo chủ màn trước khi merge**, PR liệt kê rõ màn nào của ai.

**Merge về `dev` sau mỗi bước**, không dồn cả module rồi mới merge.

---

## §6 — Đề xuất D11–D13 (cần Nam ký)

Ba mục dưới đây là lớp lỗi mà D1–D10 **không phủ**, và **hai trong ba đã tái diễn ở 2 module** — tức không phải tai nạn riêng của MF03.

### D11 — Response đổi shape theo vai trò phải khai `oneOf`

Đã xảy ra 2 lần: `GET /schools` (System Admin đủ field · Supplier rút gọn) và `GET /students` (các role khác đủ field · Service Caterers rút gọn `id/class/allergies/specialDiet/allergySeverity`).

Chữ ký `PagedResponse<?>` **xoá sạch kiểu** — springdoc không sinh được schema nào có ích, FE phải đăng nhập từng vai trò gọi thử mới biết. Khai `oneOf` với record chỉ-để-sinh-tài-liệu; xem `StudentDtos.PagedResponseStudentResponse` / `SchoolDtos.PagedResponseSchoolResponse` làm mẫu.

Tên schema **phải giữ tiền tố `PagedResponse`** — `OpenApiMetaContractIT` (S7) chốt quy ước đó.

> **Bắt buộc có TC riêng** (bổ sung 03/08). Đếm ngày 03/08 trên cả 13 TestSpec: **0 file có TC thật cho D11**. `TestSpec-MF04` có **1 dòng checklist** ở Exit Criteria (tốt, nhưng checklist không phải test — không ai chạy được nó); các hit `oneOf` còn lại là `ZoneOffset` khớp nhầm. Sau 03/08: **4/13** — `TestSpec-MF05` **TC-SEC-015** (có `oneOf` thật), `TestSpec-MF06` **TC-SEC-014** và `TestSpec-MF07` **TC-SEC-017** (cả hai kết luận *"D11 không áp dụng"* nhưng vẫn assert tập field **giống nhau** giữa các vai trò), **`TestSpec-MF09` `TC-CON-006`** (có `oneOf` thật — `M09-D10`). Mỗi module thêm 1 TC theo §7 Bước 1 việc 3.
>
> **Hai module đã kết luận "không áp dụng" — phân biệt được hai lý do khác nhau, cả hai đều hợp lệ:** MF06 vì khác biệt giữa vai trò là **tập DÒNG** chứ không phải tập field; MF07 vì BR-099 lọc row-level theo `inspector_user_id`. Điểm chung: **lọc dòng không cần `oneOf`, lọc field thì cần**. Nếu module của bạn chỉ khác nhau ở `WHERE`, chốt một DTO và viết TC parity — đừng khai `oneOf` cho có.
>
> **MF09 là ca D11 rõ nhất tới nay và đáng làm mẫu:** `GET /student-distributions` cho **Service Caterers** đọc để biết số suất phải nấu, nhưng phải **ẩn** `name`/`allergyWarning`/`specialDiet` — **PII trẻ em** (ADR-M09-6, PDPD). Khác biệt là **tập FIELD**, không phải tập dòng ⇒ `oneOf` bắt buộc. Đây cũng là **lần thứ ba** hệ thống gặp pattern này, và lần trước (`GET /students`) là **cùng loại dữ liệu**, đã tốn hai phiên curl mới tìm ra. `TC-CON-006` dùng **tài khoản seed thật** cho cả 2 vai trò (D13) — `@WithMockUser` không mang claim `schoolId` nên không tái hiện được.

### D12 — Read DTO ≠ Write DTO, nhưng key JSON phải khớp hai chiều

`GET` trả `class` mà `PATCH` chỉ nhận `classroomId` ⇒ Jackson bỏ field không khớp ⇒ **200 nhưng KHÔNG lưu**. Không lỗi, không cảnh báo — form báo thành công.

Rà từng cặp Response/Request: mọi field **sửa được** phải cùng tên ở cả hai.
Field **dẫn xuất** phải ghi rõ read-only trong TDS §9 để FE không gửi lên. MF03 có 8 field loại này: `healthStatus`, `allergies`, `specialDiet`, `allergySeverity`, `hasAllergy`, `hasSpecialDiet`, `student`, `requestedBy`, `date`.

> **Biến thể nguy hiểm hơn — cùng tên, khác KIỂU.** MF05 để `dishes` là **số đếm** ở response nhưng payload mẫu §15.1 lại gửi `dishes: [{...}]` (mảng món). Jackson bỏ qua y hệt trường hợp lệch tên, nhưng khi review bằng mắt thì hai bên *trông như khớp* nên không ai thấy. Rà cặp Request/Response phải so **cả tên lẫn kiểu**.
>
> **Bắt buộc có TC riêng** (bổ sung 03/08) — gửi lên (a) field dẫn xuất và (b) field đúng tên sai kiểu, assert **DB không đổi**. Mẫu: `TestSpec-MF05` **TC-027**.

### D13 — Seed phải có tài khoản cho MỌI role xuất hiện trong `@PreAuthorize`

MF03 cấp `GET /students` cho `SERVICE_CATERERS` nhưng V5 không seed tài khoản nào có role đó ⇒ không test được bằng tài khoản thật, phải tạo tay `U-9909` lúc kiểm.

Kiểm ở §5.2/migration khi rà tài liệu.

> **Ghi chú kèm:** mật khẩu chung của mọi tài khoản seed ACTIVE là **`P@ssw0rd123`** (V900 dòng 11), không phải `Passw0rd123`. Sai 4 lần là khoá tài khoản (BR-002).

---

## §7 — Bốn prompt (bản đã cập nhật 2026-08-03 — thêm Bước 0)

### Bước 0 — Rà chủ sở hữu resource *(mới; chạy TRƯỚC Bước 1, ~15 phút)*

```
Rà xem TDS-{MFxx} có đặc tả lại thứ mà module khác ĐÃ LÀM XONG và ĐÃ MERGE không.
KHÔNG sinh code, KHÔNG sửa quyết định thiết kế — chỉ đối chiếu quyền sở hữu.

CHẠY TRƯỚC, đừng đọc TDS trước:
  grep -ho "CREATE TABLE [a-z_]*" 04_Development/Backend/src/main/resources/db/migration/*.sql | sort -u
  grep -rho '"/api/v1/[a-z-]*' 04_Development/Backend/src/main/java --include=*.java | sort -u
  ls 04_Development/Backend/src/main/resources/db/migration/     # số V kế tiếp còn trống
  grep -n "public " 04_Development/Backend/src/main/java/com/sfms/common/reference/ReferenceResolver.java
  grep -rl "interface.*Port" 04_Development/Backend/src/main/java --include=*.java

ĐỐI CHIẾU với TDS-{MFxx} §5.2 và §9.2, trả lời đúng 5 câu:
1. Bảng nào trong §5.2 ĐÃ TỒN TẠI? → bỏ CREATE, chuyển sang ALTER (hoặc chỉ đọc nếu
   là bảng của module khác). Mở file V tương ứng ra đọc CỘT THẬT — tên cột, kiểu, PK —
   đừng tin mô tả trong TDS.
2. Path nào trong §9.2 ĐÃ CÓ controller? → gỡ khỏi phạm vi module mình. Ghi rõ ai sở hữu
   và MF của mình cần ĐỌC gì từ đó (thường chỉ 1–2 phép đọc → ReferenceResolver hoặc Port).
3. FK nào trỏ tới bảng KHÔNG TỒN TẠI? (lỗi hay gặp: `users` thay vì `user_accounts`,
   `catalog_ingredients` của MF06 chưa lên). → đổi tên bảng, hoặc bỏ FK + để lại comment
   cho module sở hữu tự ALTER khi họ lên.
4. Kiểu PK của bảng đích là gì? UUID hay VARCHAR? → FK của mình phải cùng kiểu (D1).
5. Số V nào còn trống? → đặt tên migration theo đó, KHÔNG sửa file V đã merge.
6. [MỚI 03/08 — MF08] Port nào trong TDS của bạn ĐÃ CÓ SẴN trong `ReferenceResolver`? MF08 khai 3 port,
   2 trong 3 là viết lại hạ tầng đã có (`schoolGeofence()` đã tồn tại; BR-105 chỉ là tra cứu thuần).
   LUẬT: tra cứu thuần (đọc 1-2 cột của module khác) → THÊM DÒNG vào ReferenceResolver, KHÔNG khai Port.
   Chỉ khai Port khi cần HÀNH VI của module kia. Port của module CHƯA MERGE → stub + log.warn (M07-D08),
   và chọn giá trị stub theo hướng KHÔNG khoá nghiệp vụ (M08-D08: trả false, không trả true).
7. [MỚI 03/08 — MF08] "Sạch Bước 0" KHÔNG có nghĩa là vào Bước 2 được. 5 lệnh trên chỉ bắt va chạm
   QUYỀN SỞ HỮU. Lệch QUY ƯỚC với code đã merge là lớp riêng — MF08 sạch tuyệt đối cả 5 lệnh mà vẫn
   có 3 lỗi làm build/Flyway đỏ ngay: số V lỗi thời, cột audit tự khai (D9), thiếu cột `code` (D1).
   Rà chúng ở Bước 1, và ĐỪNG tin bảng D của §2.1 — nó đã sai ô của MF07 và cả hai ô của MF08.

Nếu §9.2 có endpoint đổi trạng thái: kiểm VERB THẬT của module tiền lệ trước khi chép
(`grep -rn "PostMapping\|PatchMapping\|PutMapping" .../mf03/web/`). MF03 dùng
POST /{id}/approve chứ không phải PUT — chép nhầm là viết sai cả TestSpec.

OUTPUT: bảng "thứ tôi tưởng của mình / thực tế thuộc ai / phải làm gì" + danh sách việc
cần chốt với chủ sở hữu. KHÔNG sửa TDS ở bước này — gom vào Bước 1 sửa một lần.
```

> **Bỏ Bước 0 là bỏ thứ rẻ nhất trong cả quy trình.** Xem §2.1 — chạy trên 9 module ngày 03/08 thì **4 module** mắc, mỗi ca đều dẫn tới Flyway đỏ hoặc app không khởi động, và đều tốn **2 lệnh grep** để tránh.

### Bước 1 — Rà & chốt tài liệu

```
Bạn rà lại TDS-{MFxx}_Backend.md + TestSpec-{MFxx}_*.md, mục tiêu: đưa Status từ Draft
sang Approved, KHÔNG sinh code.

ĐIỀU KIỆN VÀO: đã chạy Bước 0 và có bảng kết quả trong tay. Chưa chạy thì DỪNG LẠI,
chạy Bước 0 trước — sửa tài liệu rồi mới phát hiện resource thuộc module khác là phải
sửa lại lần hai.

NGUỒN: `07_implement/00_Common_Conventions.md` D1–D10 (ưu tiên cao nhất) ·
`07_implement/02_Module_Completion_Workflow.md` §6 (D11–D13) · TDS/TestSpec của {MFxx} ·
`business_rules.md`, `error_message.md` · các TDS đã Approved (MF01/MF02/MF03) làm tiền lệ.

VIỆC LÀM:
1. Liệt kê toàn bộ mục ⛔/BLOCKING trong TestSpec §2 Logic Issues — với mỗi mục, đối chiếu
   D1–D13 xem có pattern đã gặp ở module khác chưa (đặc biệt D1: id JSON = mã nghiệp vụ,
   UUID không lộ ra — bảng ở D1 liệt kê MF01/02/03/08/09/10 từng mắc).
2. Kiểm tra riêng:
   · §5.2 (entity/DDL) có cột `code` cho mọi resource lộ qua path param không (D1)
   · §9 API có đúng error envelope D2 không
   · mọi ngày/giờ đúng D4 (Instant vs LocalDate) không
   · D5 (client-supplied id → 409) có mã lỗi trong §10 chưa
   · enum đúng D6 (SCREAMING_SNAKE nội bộ, kebab-case ở JSON role) chưa
   · [MỚI] endpoint nào trả shape khác nhau theo vai trò → D11
   · [MỚI] từng cặp Response/Request: field sửa được có cùng tên JSON không → D12;
     liệt kê rõ field DẪN XUẤT (read-only)
   · [MỚI] mọi role trong @PreAuthorize có tài khoản seed chưa → D13
   · [MỚI] có UC upload file không → D10-BIS: multipart qua backend (KHONG presign),
     phân loại công khai (lưu URL) vs riêng tư (lưu storageKey, JSON trả id tài liệu,
     tải xuống có @PreAuthorize + audit), field tham chiếu nhận ID chứ không nhận tên file
3. [MỚI] **Bổ sung 2 test case bắt buộc vào TestSpec — D11 và D12 không tự có.**
   Cả 13 TestSpec viết TRƯỚC khi D11–D13 tồn tại ⇒ KHÔNG bộ nào phủ. Kiểm bằng
   `grep -n "oneOf\|PagedResponse{Tên}" TestSpec-{MFxx}_*.md` — ra 0 kết quả thì thiếu.
   · **TC cho D11** — gọi endpoint danh sách bằng ≥2 vai trò có quyền khác nhau, assert
     vai trò hạn chế KHÔNG thấy field nhạy cảm; kèm assert `/v3/api-docs` khai `oneOf`
     và `items` ĐÚNG KIỂU (không phải `{}`), tên schema giữ tiền tố `PagedResponse` (S7).
   · **TC cho D12** — POST/PATCH gửi lên (a) field DẪN XUẤT và (b) field đúng tên nhưng
     SAI KIỂU, rồi assert DB KHÔNG đổi. Đây là bẫy "200 mà không lưu gì" của Jackson:
     không lỗi, không cảnh báo, form báo thành công. Mẫu: `TestSpec-MF05` TC-027/TC-SEC-015.
   · **D13** — không cần TC riêng, nhưng TC bảo mật nào phụ thuộc claim ngoài role
     (`partnerId`, `schoolId`) thì phải dùng TÀI KHOẢN SEED THẬT, không `@WithMockUser`
     (nó không mang claim đó). Kiểm seed: `grep -n "role_key" V900__test_fixtures.sql`.
4. Với mỗi mục chưa chốt: đề xuất quyết định cụ thể (không chỉ liệt kê vấn đề), dựa trên
   tiền lệ MF01/02/03. Cần BR/MSG mới thì soạn y hệt cách MF02 đã làm (BR-219/MSG-140).
5. Việc cần người khác xác nhận: liệt kê riêng, ĐỪNG tự quyết thay.
6. Sau khi tất cả mục ⛔ đã chốt: cập nhật TDS + TestSpec, Status → Approved, changelog
   theo Policy 4.4 (không xóa lịch sử, chỉ nối thêm).

OUTPUT: danh sách quyết định đã chốt + lý do, danh sách việc còn treo cần người khác.
KHÔNG sinh bất kỳ code Java/SQL nào ở bước này.
```

### Bước 2 — Sinh code backend

```
Sinh code backend cho {MFxx} trên nền `04_Development/Backend` đã có (KHÔNG dựng lại
common/, KHÔNG import entity/repository của module khác — mọi tham chiếu chéo qua
`common/reference/ReferenceResolver`, mở rộng file đó nếu thiếu method, theo pattern MF02
đã làm cho GVCN).

NGUỒN: TDS-{MFxx}_Backend.md + TestSpec-{MFxx}_*.md (phải đang Status: Approved — nếu
chưa, DỪNG LẠI, báo lại, KHÔNG tự chạy Bước 1 kèm theo).
`00_Common_Conventions.md` D1–D10 + `02_Module_Completion_Workflow.md` §6 (D11–D13).
Package `com.sfms.mf{XX}`.

Kiểm bảng nào đã tồn tại (V1–V{N} hiện có) trước khi viết migration — ALTER chứ không
CREATE lại. Migration mới: `V{N+1}__mf{XX}_*.sql`. Seed phải có tài khoản cho MỌI role
trong @PreAuthorize của module (D13).

Đăng ký prefix mã lỗi mới vào `ModuleCodeResolver` (chỉ thêm dòng).

Sinh test đầy đủ theo TestSpec (không bịa test ngoài spec). Chạy `mvn verify`, báo cáo
kết quả THẬT, không claim pass nếu chưa chạy.

── BƯỚC 2b (bắt buộc, không được bỏ) ──
Sau khi test pass: chạy backend, rồi ở `04_Development/Frontend` chạy `npm run gen:api`
để cập nhật `src/lib/api/schema.d.ts`. COMMIT file đó CÙNG commit backend.

Kiểm spec trước khi báo xong:
  · endpoint danh sách phải trỏ `PagedResponse{TênDto}` với `items` ĐÚNG KIỂU,
    KHÔNG phải `PagedResponse` trống
  · controller trả `PagedResponse<?>` (shape đổi theo vai trò) phải khai `oneOf` — D11
  · KHÔNG đặt `name=` cho schema generic (xem chú thích trong PagedResponse.java)

OUTPUT: cây file + nội dung đầy đủ · kết quả test · danh sách schema mới trong /v3/api-docs.
```

### Bước 3 — Nối frontend thật

```
Nối các trang FE tiêu thụ resource của {MFxx} với backend thật, theo cơ chế ở
`04_Development/Frontend/src/lib/api/real-resources.ts` (đọc file này trước — nguồn sự
thật duy nhất quyết định resource nào đi thật).

LÀM THEO THỨ TỰ NÀY:

1. Thêm một dòng vào `src/lib/api/contract-check.ts` cho mỗi resource sắp migrate, rồi
   chạy `npx tsc --noEmit`. Nó liệt kê THẲNG tên field mà FE đọc còn DTO không có —
   làm việc này TRƯỚC khi sửa từng màn, nó thay phần lớn công dò tay.

2. Grep `@PreAuthorize` của từng endpoint {MFxx} để biết role nào đọc được.
   ⚠️ VIỆC NÀY KHÔNG BỎ ĐƯỢC: OpenAPI KHÔNG xuất thông tin role (kiểm 02/08/2026 —
   operation chỉ có description/operationId/parameters/responses/summary/tags). Type sinh
   tự động không thay thế được bước này.
   Trang NGOÀI phạm vi {MFxx} đọc chung resource mà bị 403 ⇒ liệt kê rõ, ĐỪNG tự mở rộng
   @PreAuthorize nếu không chắc scope dữ liệu an toàn — hỏi lại thay vì đoán.

3. Lấy verb từ controller thật. CÓ BA LOẠI, đừng mặc định PATCH:
   · PATCH /:resource/:id            → usePatch      (VD /students/{code})
   · POST  /:resource/:id/:action    → useAction     (VD /special-meals/{id}/approve|reject)
     — đổi trạng thái qua LUỒNG NGHIỆP VỤ, không phải sửa bản ghi
   · PUT vẫn tồn tại ở vài chỗ (VD /students/{code}/health-record) — kiểm, đừng đổi bừa

4. ⚠️ THỨ TỰ BẮT BUỘC: `mock-server.ts` KHÔNG có handler `onPatch` (chỉ GET/POST/PUT/
   DELETE). Đổi useUpdate→usePatch khi resource CHƯA có trong REAL_RESOURCES ⇒ PATCH rơi
   qua passthrough ra backend thật cho resource chưa migrate ⇒ hỏng.
   ĐỔI VERB VÀ THÊM TÊN VÀO REAL_RESOURCES PHẢI NẰM TRONG CÙNG MỘT COMMIT.

5. Field dẫn xuất (server tự đặt) thì KHÔNG gửi lên trong payload create/update.
   Ngày giờ: API dùng ISO-8601, chỉ format sang dd/MM/yyyy ở tầng render (`formatVnDate`
   trong `lib/format.ts`) — đừng lưu chuỗi đã format vào state hay payload (D4).

KHÔNG sửa `mock-server.ts`/`next.config.ts`/cơ chế auth — đã có sẵn, chỉ thêm resource.

Kiểm trước khi báo xong: `npx tsc --noEmit` · `npm run audit` · thử thật qua Next rewrite
(`curl localhost:3000/api/v1/{resource}` có token → 200; không token → 401, chứng tỏ mock
đã nhả).

OUTPUT: diff từng trang · resource thêm vào REAL_RESOURCES · dòng thêm vào contract-check.ts
· DANH SÁCH FIELD LỆCH (không chỉ 403) · danh sách trang chờ quyết định.
```

---

## §8 — Việc còn treo của MF03 (không chặn Sóng 1)

| # | Việc | Chặn cái gì | Ai quyết |
|---|---|---|---|
| 1 | `name` cho `StudentDietSummaryResponse` — **✅ ĐÃ THI HÀNH 2026-08-04**: chốt KHÔNG thêm `name` (PDPD/data-minimization, Service Caterers là bên thứ ba), `service-caterers/meal-service/page.tsx` đổi cột "Học sinh" → "Mã HS / Lớp" (`{s.class} · {s.id}`), `tsc --noEmit` sạch | `service-caterers/meal-service` | ✅ đã chốt, đã sửa |
| 2 | 4 field lâm sàng (`medicalConditions`/`medications`/`emergencyContact`/`medicalDocuments`) nằm ở `/students/{code}/health-file`, không có trong `StudentResponse` — **chốt: giữ mock đợt này** | `medical-staff/health-records/*` | Nam + Minh |
| 3 | `PATCH /students/{code}` chỉ `SCHOOL_ADMIN` ⇒ Y tế bấm "Xác minh" nhận **403** — KHÔNG còn áp dụng cho dị ứng/bệnh lý nền: đợt 04/08 (đợt ba) viết lại `medical-staff/health-reviews` để KHÔNG còn gọi `PATCH /students/{code}` cho hai luồng đó nữa (dùng `POST /allergy-declarations/{id}/verify`+`POST /chronic-conditions/{id}/verify`, cả hai `@PreAuthorize("hasRole('MEDICAL_STAFF')")` — đúng vai Y tế, không 403). Mục này ĐÓNG cho phạm vi dị ứng/bệnh lý nền; vẫn còn treo cho các thao tác khác trên `students` (chưa migrate) | ~~`medical-staff/health-reviews`~~ đã đóng cho phạm vi này | ✅ đã sửa (đợt ba, viết lại luồng thay vì đổi RBAC) |
| 4 | `POST /health-history` **KHÔNG THIẾU — CỐ Ý KHÔNG TỒN TẠI** (ADR-M03-4 Approved): `append()` là service nội bộ. **✅ ĐÃ THI HÀNH 2026-08-04 (đợt ba)** — `useAppendHealthHistory()`/`lib/health-history.ts` GỠ BỎ HOÀN TOÀN (file xoá), 3 call site sửa: `medical-staff/health-reviews` (viết lại luồng verify, dùng action thật) + `medical-staff/special-meals` (approve/reject — xác nhận `SpecialMealRequestServiceImpl` đã tự append, gỡ an toàn) an toàn vì backend tự ghi; `parent/special-meal-requests` (submit) phát hiện LỖ HỔNG THẬT — `SpecialMealRequestServiceImpl.create()` KHÔNG tự append KHAI_BAO (khác Allergy/Chronic `submit()`, cả hai đều tự append) — đã gỡ lời gọi FE (endpoint không tồn tại), còn treo: thêm 1 dòng `healthHistoryService.append(KHAI_BAO,...)` vào `SpecialMealRequestServiceImpl.create()` | Đã hết chặn `useAppendHealthHistory()`; còn treo 1 dòng backend cho sự kiện KHAI_BAO của special-meal | Huy — phần FE xong; phần backend (1 dòng) còn treo |
| 5 | **✅ ĐÃ LÀM 2026-08-04** — `POST /api/v1/students/{code}/health-documents` (multipart, PARENT/MEDICAL_STAFF/SCHOOL_ADMIN, scope C2) bổ khuyết đường tạo `proofFileId` UUID thật. Test mới `HealthDocumentUploadIT` 3/3 pass. **✅ ĐÃ NỐI FE (đợt ba, 04/08)** — `ProofUpload` ở `parent/allergies`/`parent/chronic-conditions` đổi từ giữ TÊN file sang upload File thật qua endpoint này, lấy `id` làm `proofFileId` | — | ✅ xong cả backend lẫn FE |
| 6 | `Classroom.lunchTime` — FE có, `ClassroomResponse` không. Chưa vỡ vì `useTeacherClass()` cố ý đọc seed (do `/classrooms` là SCHOOL_ADMIN-only) | sẽ vỡ khi migrate màn Teacher | Nam (MF02) |
| 7 | `medical-staff/health-reviews` tab "Dị ứng" dựng trên `DataTable<Student> resource="students"` với 5 cột dẫn xuất KHÔNG có trên `StudentResponse` thật — **✅ ĐÃ SỬA 2026-08-04 (đợt ba)**: viết lại thành `AllergyModule` (cùng khuôn List→Detail Dialog với `ChronicModule`), lấy dữ liệu chính từ `usePagedList<AllergyDeclarationDto>("allergy-declarations")`, `students` (vẫn mock) chỉ còn tra tên/lớp hiển thị | — | ✅ đã sửa, `allergy-declarations` đã bật vào `REAL_RESOURCES` |
| 8 | `medical-staff/health-reviews` `ChronicModule` verb sai — **✅ ĐÃ SỬA 2026-08-04 (đợt ba)**: đổi `useUpdate("chronic-conditions")` → `useAction("chronic-conditions")` gọi đúng `POST /{id}/verify`+`POST /{id}/request-info`; gỡ `useAppendHealthHistory()` trùng lặp | — | ✅ đã sửa, `chronic-conditions` đã bật vào `REAL_RESOURCES` |
| 9 | `severity` 4 màn dùng nhãn tiếng Việt làm giá trị lưu trữ — **✅ ĐÃ SỬA 2026-08-04 (đợt ba)**: thêm `lib/health-severity.ts` (bảng map DUY NHẤT enum↔nhãn), áp dụng cho cả 6 màn tiêu thụ (`parent/{allergies,chronic-conditions,health}`, `medical-staff/{health-reviews,health-records/[id]}` — 5 màn liệt kê + đã đếm lại đúng 6 vì `health-reviews` có 2 module) | — | ✅ đã sửa |
| 10 | `health-history.type` trả enum không dấu (`KHAI_BAO`/`XAC_MINH`/`GHI_CHU_LAM_SANG`/`SU_CO`/`CAP_NHAT_THUOC`), FE (`medical-staff/health-records/[id]/history/page.tsx`) vẫn đọc mảng tĩnh `HEALTH_HISTORY` (mock, nhãn có dấu), chưa đổi sang `usePagedList` + bảng map hiển thị. **CHƯA làm ở đợt ba** — ưu tiên đóng allergy/chronic trước (có luồng GHI phụ thuộc); `health-history` chỉ có GET, rủi ro thấp hơn nhưng vẫn cần 1 màn sửa + verify qua backend thật trước khi bật | `health-history` vẫn mock | Huy (chủ màn) |
| 11 | `security/IdorIT.java`/`security/ParentScopeIT.java` (KHÔNG phải file `mf03/` động tới đợt hai/ba) — 8 test lỗi `DuplicateKeyException` trên `U-9906`/`U-9907`, xác nhận CÓ SẴN TỪ TRƯỚC (tái xác nhận đợt ba bằng cách chạy riêng 2 class đó sau khi thêm `HealthDocumentUploadIT` — lỗi giữ nguyên, không liên quan) | `mvn test -Dtest="com.sfms.mf03.**"` báo 8 lỗi mỗi lần chạy | Minh (MF09, dải mã `U-99xx`) hoặc Huy |

Mục 6 đang được giữ bằng `Omit<Classroom, "lunchTime">` trong `contract-check.ts` — gỡ `Omit` khi backend thêm field hoặc khi chốt bỏ hẳn.

Bốn khoản nợ đọc seed trực tiếp nằm trong `KNOWN_SEED_READS` (`scripts/seed-read-guard.ts`), mỗi dòng có ghi lý do; ba trong bốn là do MF04 chưa có backend.

---

## §9 — Changelog

| Ngày | Người | Thay đổi |
|---|---|---|
| 2026-08-04 | Huy | **MF03 Bước 3 đợt hai — vẫn 0/4 resource bật thêm (`students`/`allergy-declarations`/`health-history`/`chronic-conditions`), nhưng 2 việc nền đã xong + lý do mỗi resource được re-verify chính xác hơn.** Môi trường: backend đã chạy sẵn trên `:8080` (postgres/mailpit containers có sẵn từ trước) — `npm run gen:api` chạy thật, `schema.d.ts` cập nhật. **(1) Backend mới — `POST /api/v1/students/{code}/health-documents`:** quyết định nhóm 04/08 giao việc bổ khuyết endpoint upload evidence còn thiếu (chỉ `POST /profile/me/photo` của MF01 là multipart thật trước đó) — copy khuôn `ProfileService.updatePhoto` (10MB/PDF-JPG-PNG/magic-bytes) nhưng lưu tài liệu RIÊNG TƯ (D10-BIS mục 2, lưu `key` không lưu URL trả về) và scope theo học sinh (C2, PARENT chỉ con liên kết — dùng lại đúng luật `enforceScope` của `StudentServiceImpl`). Domain `HealthDocument`/`HealthDocumentRepository` đã có sẵn từ trước (chưa ai nối controller) — chỉ thêm DTO + service + controller + 2 mã lỗi mới (`STU-008`/`STU-009`, message y hệt `IamMessages` D7). Test mới `HealthDocumentUploadIT` — 3 TC, PASS cả 3 (chạy `mvn test -Dtest=HealthDocumentUploadIT` thật, Testcontainers Postgres): upload PDF hợp lệ → `id` dùng ngay làm `proofFileId` cho `POST /allergy-declarations` mức SEVERE → 201; file giả magic-bytes (nội dung không phải PDF thật, chỉ đặt tên `.pdf`) → 422 STU-009 đúng message MSG-009; PH không liên kết HS (`U-9908`, liên kết `HS-3A-002` không phải `HS-3A-001`) → 403. Chạy lại **toàn bộ `mvn test -Dtest="com.sfms.mf03.**"` (175 TC)** sau thay đổi: **167 pass, 8 lỗi** — xác nhận cả 8 là lỗi **CÓ SẴN TỪ TRƯỚC** (đã ghi nhận bởi Mạnh ở dòng 2026-08-04 phía dưới, khi chạy `mvn -o verify` không-scoped lần đầu cho MF10) bằng cách chạy riêng `IdorIT`+`ParentScopeIT` không kèm gì khác — lỗi giữ nguyên, không liên quan `HealthDocumentController`/DTO mới. **(2) Frontend — anonymization `meal-service`:** quyết định nhóm 04/08 (`StudentDietSummaryResponse` KHÔNG thêm `name`, PDPD) thi hành: `service-caterers/meal-service/page.tsx` dòng ~410 đổi `{s.name}` → `{s.class} · {s.id}`, cột "Học sinh" → "Mã HS / Lớp". `npx tsc --noEmit` sạch toàn dự án sau đổi (không chỉ file này). **(3) Re-verify 4 resource, KHÔNG bật thêm cái nào — lý do đã thu hẹp, không còn "thiếu tài liệu curl":** `students` — PATCH silent-no-op ĐÃ SỬA từ trước (`@JsonProperty("class")` xác nhận trong `StudentDtos.UpdateStudentRequest`), nhưng KHÔNG đủ: `medical-staff/health-reviews` tab "Dị ứng" dựng nguyên trên `DataTable<Student> resource="students"` với 5 cột dẫn xuất không tồn tại thật — cần viết lại tab, không phải field-remap, ngoài phạm vi phiên này. `allergy-declarations` — verb/field GHI đã khớp đủ (kể cả `proofFileId` nay có đường tạo UUID thật), nhưng KHÔNG bật độc lập được vì `health-reviews` gọi `useUpdate("students")`+vòng lặp PUT `allergy-declarations` thay vì `POST /{id}/verify` thật — đúng lớp lỗi "verb khớp mà LUỒNG NGHIỆP VỤ không khớp". `health-history` — phát hiện quan trọng nhất đợt này: `POST /health-history` **KHÔNG PHẢI THIẾU SÓT** như ghi chú 02/08 từng đoán ("500, cần Nam+Huy quyết") — đọc javadoc `HealthHistoryController` (ADR-M03-4, Approved) thì đây là **CHỦ Ý KIẾN TRÚC**: `append()` là service nội bộ, một `POST` công khai sẽ phá chính BR-172 nó bảo vệ. `useAppendHealthHistory()` phải GỠ HẲN khỏi 3 call site (`parent/special-meal-requests`, `medical-staff/special-meals`, `medical-staff/health-reviews`), không phải "chờ backend thêm endpoint" — nhưng gỡ 3 call site này nằm CHUNG code path với việc viết lại tab "Dị ứng" của `health-reviews` (mục 7/8 ở §8), nên để chung, không tách làm nửa việc. `chronic-conditions` — module MỚI (controller giờ đã có, trước đây không tồn tại), nhưng cả 4 màn tiêu thụ (`parent/chronic-conditions`, `parent/health`, `medical-staff/health-records/[id]`, `medical-staff/health-reviews`) đều cần sửa TRƯỚC KHI bật: severity Việt↔enum map xuyên 4 file (rủi ro cao nếu làm vội không qua trình duyệt thật), `ChronicModule` verb sai (PUT thay vì `POST /verify`), gọi `useAppendHealthHistory()` trùng lặp với ghi vết server-side. Kiểm trước khi báo: `mvn -o compile` sạch, `mvn test -Dtest=HealthDocumentUploadIT` 3/3 pass, `mvn test -Dtest="com.sfms.mf03.**"` 167/175 pass (8 lỗi có sẵn, đã xác nhận độc lập), `npx tsc --noEmit` sạch toàn dự án. **CHƯA làm** (khác với "không làm được"): chưa mở trình duyệt thật để bấm luồng end-to-end (giới hạn môi trường agent, không có browser) — mọi xác nhận ở đây là qua `mvn test`/`curl`/`tsc`, không phải click UI thật; không commit gì, để nguyên working tree cho review. |
| 2026-08-04 | Mạnh | **MF10 Bước 2b + Bước 3 (2/4 resource: `announcements`, `notification-preferences`).** Môi trường trước tiên: `mvn spring-boot:run` local thất bại `password authentication failed for user "sfms"` — port 5432 thật ra bị **native Windows service `postgresql-x64-18`** chiếm trước (không phải container `sfms-postgres` của `docker-compose.yml`, dù `docker ps` từng cho tưởng container đang trả lời — kiểm bằng `pg_hba.conf` bên trong container là `trust` toàn bộ nhưng kết nối TCP host vẫn báo sai mật khẩu ⇒ chứng minh traffic KHÔNG tới container). Khắc phục KHÔNG đụng service Windows (không rõ ai khác đang dùng): tạo lại container với `-p 5433:5432` (giữ nguyên volume `backend_pgdata`), chạy backend với `DB_URL=jdbc:postgresql://localhost:5433/sfms`. `npm run gen:api` thật (backend đang chạy) → `schema.d.ts` cập nhật đủ DTO MF10. **Bước 3 — đọc `real-resources.ts` trước theo đúng quy trình, rồi rà cả 4 resource:** `feedbacks` (4 màn: `parent/feedback[,/meal,/quality]`, `school-admin/feedback`) và `notifications` (chuông + `lib/notify.ts`, dùng bởi 8 màn NGOÀI MF10) đụng lớp lỗi sâu hơn field-rename — feedback cần viết lại luồng chọn bữa từ lọc-theo-tên sang `GET /feedbacks/eligible-meals` (mã `mealSessionId`/`dishId`, BR-139); `notifications` không có `POST` thật (tạo là hệ quả server qua `NotificationPort`, ADR-M10-4) trong khi `useNotify()` đang POST từ 8 module khác, cộng taxonomy `type` lệch 4↔7 giá trị (D6) — cả hai **dừng có chủ đích**, ghi đầy đủ ở `real-resources.ts`. `announcements` VÀ `notification-preferences` đủ điều kiện migrate: sửa `AnnouncementList`/`AnnouncementDetail` (dùng chung 6 vai) + 3 màn School Admin (list/create/edit) sang đọc `AnnouncementListItemDto`/`AnnouncementDto` (`lib/api/types.ts`) thay vì interface `Announcement` cũ; 3 lệch sửa khi nối — (1) list item KHÔNG có `content` (chỉ Detail có) → bỏ dòng preview nội dung ở card danh sách; (2) `audienceCodes` (CF-M10-7, SCREAMING_SNAKE) là field GHI thật thay vì chuỗi tiếng Việt nối dấu phẩy — Create đã multi-select sẵn, đổi payload gửi mã; Edit trước đây chỉ có `<Select>` MỘT lựa chọn (không khớp Create) → đổi Edit sang cùng checkbox-group; (3) `useUpdate`(PUT)→`usePatch` (D12, `AnnouncementController.update` chỉ có `PATCH`). `notification-preferences`: `service-caterers/notification-settings/page.tsx` trước đây thuần `useState` cục bộ (bấm Lưu chỉ toast) — nay GET tải `typeEnabled`/`emailFallback` thật, PUT lưu lại (7 khóa `NOTIFICATION_TYPES` khớp 1:1 D6, không cần map tên); dialog "Cài đặt thông báo" ở `user-menu.tsx` (Topbar) là UI khác — mô hình Push/Email + Quiet Hours sửa trực tiếp, không khớp `typeEnabled` nên CHƯA nối, để nguyên (quyết định UI, không phải field thiếu). Verify thật qua Next rewrite (không mock, không suy đoán): không token → 401; có token (login thật `tranth.admin.synthetic@example.test`) → `GET /announcements` 200 đúng dữ liệu Postgres (`audienceCodes`/`revisionCount` đúng); `PATCH /announcements/{code}` 200, `revisions[]` nối thêm đúng 1 mục; `GET/PUT /notification-preferences/me` 200, xác nhận PUT **THAY THẾ TOÀN BỘ** `typeEnabled` (không merge từng key — `NotificationServiceImpl.updateMyPreference` gán thẳng `req.typeEnabled()`) nên trang FE phải luôn gửi đủ map 7 khóa, không gửi partial — đã kiểm code khớp đúng hành vi này trước khi coi là xong. `npx tsc --noEmit` sạch (1 lỗi `title: string | undefined` phát hiện + sửa ngay ở `announcement-detail.tsx`) · `npm run audit` sạch (54 luật + 53 bảng seed, 0 vi phạm mới). Cập nhật `lib/api/types.ts` (6 alias DTO mới) + `lib/api/contract-check.ts` (ghi rõ vì sao KHÔNG có dòng `MissingOnDto` cho `announcements`/`notification-preferences` — FE đã bỏ hẳn interface mock cũ, không còn gì để so). |
| 2026-08-04 | Mạnh | **MF10 Bước 2 (backend) — xong, chưa sang Bước 2b/Bước 3.** Chi tiết đầy đủ ở `TDS-MF10_Backend.md` CHANGELOG (v1.2). Tóm tắt: package `com.sfms.mf10` (10 entity, `NotificationPort` — port duy nhất MF10 expose, 8 repo, 6 service, 4 controller/13 endpoint), `V18`/`V19`, 4 IT test (`FeedbackAnnouncementFlowIT` + `RoleShapeAndWriteContractIT` — 2 TC D11/D12 bắt buộc) **4/4 pass**. **Chạy `mvn -o verify` TOÀN BỘ project lần đầu tiên kể từ khi MF09/V17 merge** (491 test) — không module nào trước đó làm việc này, chỉ chạy scoped theo module mình. Kết quả: **0 lỗi do MF10**, nhưng lộ ra **9 lỗi CÓ SẴN TRƯỚC** mà scoped-test của MF06/07/08/09 không bắt được vì mỗi module chỉ chạy `-Dtest=com.sfms.mfXX.**` của riêng mình: `UserListIT` (MF01) đếm cứng `total=27` cho `GET /users` toàn nền tảng — đã lệch từ `V11__seed_mf06.sql` (+2) và `V17__seed_mf09.sql` (+1) từ trước, MF10 (+2) chỉ là giọt cuối làm nó hiện ra — **đã sửa `27→32`** (bookkeeping thuần, an toàn, đúng khuôn tự sửa nhiều lần trước đó của chính file). 8 lỗi còn lại **CHƯA sửa, không thuộc MF10**: `InspectionRoleFieldParityIT` (MF07) đếm cứng 3 dòng `/inspections` bị `V15__seed_mf08.sql` (MF08) đẩy lên 8 — cần Thảo xác nhận; `IdorIT`/`ParentScopeIT` (MF03, 8 test) tạo tài khoản `U-9906`/`U-9907` LÚC CHẠY tại `SCH-MF03-0001` nhưng `V17__seed_mf09.sql` **đã tĩnh seed sẵn `U-9907`** ở đúng trường đó (dòng 13) — đụng độ dải mã `U-99xx` giữa seed tĩnh và seed runtime, lỗi từ khi V17 merge — cần Minh (MF09) hoặc Huy (MF03) đổi dải mã. **Bài học cho 3 module còn lại (MF11/12/13):** `mvn -o test`/`verify` **scoped** (`-Dtest=com.sfms.mfXX.**`) không đủ để coi là "không regression" — phải chạy **KHÔNG scoped** ít nhất một lần trước khi báo "xong Bước 2", đúng cảnh báo mà chính MF06 (Huy, 03/08) đã ghi nhưng chưa ai làm triệt để: *"JaCoCo gate chỉ đáng tin ở lần verify full cuối cùng"* — hoá ra không chỉ JaCoCo, cả nghiệp vụ đếm-cứng cũng vậy. Hai lỗ hổng D6 phát hiện MỚI (không phải lỗi code): `common/enums/NotificationType` (7 giá trị) không có mục nào khớp ngữ nghĩa "phản hồi tiêu cực" (MSG-057) hay "công bố thông báo trường" (MSG-047/BR-138) — MF10 để hai nhánh notify này TREO, không tự bịa giá trị enum thứ 8, chờ Huy quyết định mở rộng D6. |
| 2026-08-03 | Minh | **MF09 Bước 2 bịt khoảng trống `MenuPublished` + Bước 2b + Bước 3 (1/4 resource).** Bước 2 (đợt trước) từng ghi nợ: MF05 chưa từng phát event `MenuPublished` nên `student_distributions` chỉ có seed, không có đường sinh tự động. Đóng nợ: `MenuServiceImpl` (MF05) nay phát `Mf05Events.MenuPublished` (field tham chiếu chéo module là mã nghiệp vụ — `schoolCode`/`menuCode`, đúng khuôn `IamEvents`) ở CẢ HAI đường chuyển `PUBLISHED` (`POST /publish` và CF-8 PUT nhảy thẳng `PENDING_APPROVAL→PUBLISHED`), chỉ phát cho `audience="ALL"`. MF09 thêm `DistributionPlanHandler` (`@EventListener`) tiêu thụ event, sinh phiên `PENDING` cho từng HS ACTIVE của khối × ngày × bữa, snapshot caterer 1 lần (ADR-M09-8), idempotent theo `UNIQUE(student_id,date,meal_type)` (ADR-M09-3) — thêm 1 method thuần `ReferenceResolver.classroomIdsOfGrade()` (additive). Verify **thật**, không suy đoán: boot lại Postgres tươi, login caterer thật tạo+submit menu mới (`MENU-2026-0002`, phải lùi ngày ra >3 ngày mới qua BR-061), admin PUT `{"status":"PUBLISHED"}` (CF-8) → query trực tiếp `student_distributions` ra đúng **3 dòng PENDING** cho lớp 3A, đúng `caterer_name` snapshot, đúng `menu_id`. **Bước 2b:** `npm run gen:api` thật (cài `openapi-typescript` + `npm install` FE lần đầu trong phiên này, trước đó chưa có `node_modules`) — `schema.d.ts` +894 dòng, đủ 4 DTO chính (`DistributionResponse`/`DistributionCatererResponse`/`MealRecordResponse`/`MealHistoryEntryResponse`) + 2 `PagedResponse*` không rỗng. **Bước 3 — chỉ `meal-records` migrate được (1/4):** đối chiếu field lộ `MealRecordResponse` thiếu `studentId` (chỉ có `student` tên) — vì đây là module Minh tự sở hữu cả backend lẫn FE, tự quyết bổ sung thẳng field đó (`ReferenceResolver.studentCode()`, D1) thay vì để nợ chờ "chủ màn khác" như các module trước phải làm. Sửa `teacher/meal-records/page.tsx`: đổi `useUpdate`(PUT, không tồn tại ở backend — chỉ có `PATCH`)→`usePatch`; bỏ `recordedAt` (field dẫn xuất) khỏi payload; phát hiện thêm 1 lỗi filter ngày ÂM THẦM không hoạt động — FE gửi `date=dd/MM/yyyy` nhưng `MealRecordSpec` chỉ nhận `dateFrom`/`dateTo` dạng ISO qua `ListQuery`, khoá `date` rơi ra ngoài `FIELD_MAP` nên bị bỏ qua lặng lẽ (không lỗi 4xx) — sửa bằng `toIsoDate()`. Đổi hiển thị ngày (`teacher/meal-records`, `teacher/students/[id]`) sang `formatVnDate`/`formatVnDateTime` (D4, API trả ISO không phải `dd/MM/yyyy`). Verify thật qua Next rewrite (không mock): không token → 401, có token → 200 đúng dữ liệu Postgres (`studentId` xuất hiện đúng), filter `dateFrom=dateTo=2026-07-06` → 1 kết quả, đổi ngày khác → 0; `PATCH` thật chặn đúng luật nghiệp vụ (`SRV-025`, khoá sau ngày phục vụ — không phải lỗi kỹ thuật, xác nhận endpoint thật hoạt động đúng). **3/4 resource còn lại CHỦ ĐỘNG CHƯA nối, dừng có chủ đích (không phải bỏ dở):** `student-distributions` (dùng ở cả 4 màn: `teacher/distribution[/{id}]`, `teacher/dashboard`, `parent/special-meal-records`, `service-caterers/meal-service`) đụng 3 lớp lỗi CÙNG LÚC — verb (`StudentDistributionController` chỉ có 4 action POST, không PUT/PATCH nào, trong khi "Bắt đầu phân phát" đang PUT tuỳ ý qua `useUpdate`), field (cột chính "Suất ăn được phân công" — `assignedMeal` — là preview thực đơn cho phiên PENDING, không có trên `DistributionResponse`; `servedDishes` là sự kiện ĐÓNG BĂNG rỗng tới khi phát, đúng ADR-M09-1, không phải thiếu sót — muốn hiện phải tự join sang `menus` phía client, việc chưa làm), và role (Service Caterers nhận `DistributionCatererResponse` CỐ Ý không có `studentId` theo D11, nhưng `meal-service/page.tsx` đang tra đúng field đó — không sửa được bằng remap, vì DTO hẹp là ĐÚNG thiết kế). `allergy-alerts` (class-wide, UC-103) chưa trang nào gọi — không có gì để bật. `meal-history`/`special-meal-records` lồng dưới `/students/{code}/...`, bị chặn bởi resource `students` (chủ Huy/MF03) còn mock toàn bộ — cơ chế `REAL_RESOURCES` chỉ khớp tiền tố path cấp 1, không bật lẻ được path con. Đủ 4 bước kiểm trước khi báo xong: `npx tsc --noEmit` sạch · `npm run audit` sạch (54 luật ngữ nghĩa + 53 bảng seed, 0 vi phạm mới) · chạy backend thật + `npm run dev` thật, curl qua Next rewrite (không mock) xác nhận cả 401/200/PATCH. Dọn sạch container Postgres + tiến trình Java/Next sau khi xong. |
| 2026-08-03 | Mạnh | **MF08 Bước 2 (backend) — xong, chưa sang Bước 2b/Bước 3.** Package `com.sfms.mf08` đầy đủ: 4 entity (`FoodSample`, `SampleLabRequest`, `ReceivingAuthority`, `SampleAttachment`) + 6 enum, `IncidentStatusPort` + stub (M08-D08), 4 repository, 5 DTO file, 6 service (`FoodSampleService`, `SampleLabRequestService`, `ReceivingAuthorityService`, `SampleRegisterExportService`, `RetentionDeadlineScheduler`, 2 assembler) + 3 controller (12 endpoint đúng §9.1), `SmpError`/`SmpMessages`/`SmpExceptions` (đối chiếu `error_message.md` v1.10 từng chuỗi — khớp 100%, đúng `M08-D10`), `SMP` đăng ký vào `ModuleCodeResolver`, 2 method mới trên `ReferenceResolver` (`inspectionPassedAllSteps`/`inspectionIdByCode` — `M08-D07`, KHÔNG dựng `InspectionGatePort`). `V14`/`V15` (migration + seed 6 nơi tiếp nhận/5 dish mới/5 phiên kiểm thực PASSED/11 mẫu lưu, quy đổi đúng D4/D6/CF-M08-8/9 như TDS §11.2 yêu cầu). Thêm 2 dependency **mới cho dự án** — `poi-ooxml` 5.5.1 + `pdfbox` 3.0.8 (bản mới nhất, theo yêu cầu) — để `SampleRegisterExportService` xuất **XLSX/PDF thật** (không phải CSV giả lập) cho sổ Mẫu số 5. Compile sạch (`mvn -o compile`), **192/192 unit test toàn project vẫn pass** (không regression MF01–MF07). **1 IT test thật** (Testcontainers Postgres 16, `-Dtest=` scoped vì `*IT.java` chạy qua failsafe/`mvn verify`, không phải `mvn test`): `FoodSampleFlowIT` — đăng ký mẫu (đa file multipart) → Điều 7.1 chặn trùng (inspection,dish) → BR-203 chặn khối lượng thấp → hủy trước hạn 409 SMP-111 → RBAC 403 (y tế gọi endpoint bếp) → hủy hợp lệ sau 25h → Mẫu số 5 thiếu witness/note 422 SMP-030 → liên kết sự cố + bàn giao lab đều 409 SMP-023 (đúng thiết kế — `IncidentStatusPort` là stub, MF11 chưa merge) → RBAC 403 (bếp gọi endpoint bàn giao) → danh mục nơi tiếp nhận 200 — **1/1 pass**. **2 lỗi thật tìm thấy VÀ SỬA ngay khi viết IT test (không tìm được cách nào khác ngoài chạy thật):** (1) **Hibernate auto-flush giữa chừng phá CHECK constraint** — `dispose()` gọi `resolveActorName(actor)` (native query qua `ReferenceResolver`) SAU KHI đã `sample.setStatus(DISPOSED)`/`setDisposedAt(...)` nhưng TRƯỚC `setDisposalWitness`/`setDisposalNote`; Hibernate auto-flush trước mọi query đẩy entity dở dang xuống DB, vỡ `ck_smp_dispose_complete`. Sửa: resolve tên actor TRƯỚC khi đụng bất kỳ setter nào của entity đã managed. **Đề nghị các module sau kiểm:** bất kỳ chỗ nào gọi `ReferenceResolver`/native query xen giữa các setter của một entity đã load (không phải entity mới tạo) đều có rủi ro này. (2) **`@NotBlank` trên DTO che mất mã lỗi nghiệp vụ cụ thể** — `DisposeSampleRequest.witness`/`note` khai `@NotBlank` làm Bean Validation trả `SMP-422`/MSG-138 chung chung trước khi code service kịp chạy, trong khi TDS §10 đòi `SMP-030`/MSG-125 (Mẫu số 5 cột 9/10/12) cho đúng trường hợp này. Gỡ `@NotBlank`, để validate ở service. **Bài học:** field nào TDS đã cấp mã lỗi RIÊNG (không phải MSG-138 chung) thì DTO KHÔNG được khai `@NotBlank`/`@NotNull` cho field đó — hai lớp validate sẽ giành nhau, lớp generic luôn thắng vì chạy trước. **Việc CHƯA làm (khác với "không làm được"):** Bước 2b (`npm run gen:api` phía FE) và test coverage mới phủ 1/47 TC của TestSpec-MF08 (đủ chứng minh luồng chính + các lỗi nghiệp vụ nặng nhất chạy đúng trên Postgres thật, KHÔNG đủ để coi là hoàn thành theo TestSpec) — 46 TC còn lại để làm tiếp, đúng tinh thần MF06/MF07 đã làm (dừng có chủ đích, ghi rõ, không claim xong). |
| 2026-08-03 | Mạnh | **MF08 Bước 2b (`npm run gen:api`) — xong.** Chạy backend thật (không mock): DB local qua Docker (`docker-compose.yml` postgres:15) — máy này có sẵn một PostgreSQL cài native trên Windows cũng lắng nghe `0.0.0.0:5432` (xác nhận bằng `Get-NetTCPConnection -LocalPort 5432`, thấy 2 tiến trình: `com.docker.backend` trên `::` và `postgres.exe` PID riêng trên `0.0.0.0`), nên mọi kết nối tới `localhost:5432`/`host.docker.internal:5432` từ container hay từ ứng dụng host đều rơi vào server **native** (sai schema/mật khẩu) chứ không phải container `sfms-postgres` — biểu hiện là `FATAL: password authentication failed` dù mật khẩu đúng, kể cả sau khi xoá volume làm lại từ đầu và đổi `pg_hba.conf` sang `trust`. Sửa cục bộ (không đụng file dùng chung): thêm `docker-compose.override.yml` (KHÔNG commit, chỉ máy này) remap cổng container sang `5433:5432`, chạy `mvn spring-boot:run` với `DB_URL=jdbc:postgresql://localhost:5433/sfms`. 13 migration (tới `V15`) áp sạch, `/v3/api-docs` lên đủ. `npm run gen:api` sinh `schema.d.ts` mới — xác nhận đúng yêu cầu §7 Bước 2b trước khi báo xong: `PagedResponseFoodSampleResponse.items`/`PagedResponseSampleLabRequestResponse.items`/`PagedResponseReceivingAuthorityResponse.items` đều gõ đúng kiểu response tương ứng (không rỗng `{}`), KHÔNG `oneOf` nào cho 3 schema này (đúng M08-D13 — D11 không áp dụng vì lọc theo role là row-level, đã ghi rõ trong TDS §9.2, không phải field-level). Diff `schema.d.ts` ~850 dòng chủ yếu là hiệu ứng cơ học (openapi-typescript đánh số lại `operations["list_N"]`/`operations["detail_N"]` cho toàn bộ operation không có `operationId` tường minh khi chèn 12 endpoint mới của MF08 vào giữa danh sách theo thứ tự alphabet) — không phải lệch hợp đồng thật. Sau `gen:api`: `npx tsc --noEmit` sạch, `mvn -o test` **192/192 pass lại lần nữa** (không regression). **CHƯA sang Bước 3** — chưa nối trang FE nào tiêu thụ `samples`/`sample-lab-requests`/`receiving-authorities` (`medical-staff/samples`, `service-caterers/samples[+/print,+/[id],+/[id]/print]` đang mock) — để lại theo đúng tinh thần dừng có chủ đích. |
| 2026-08-03 | Mạnh | **MF08 Bước 3 (nối FE thật) — 1/3 resource (`receiving-authorities`), 2/3 CHỦ ĐỘNG CHƯA nối, lý do khác lớp lỗi shape/verb của MF06/MF07.** Theo đúng thứ tự §7 Bước 3: (1) thêm dòng `contract-check.ts` cho `receiving-authorities` TRƯỚC khi sửa trang — `MissingOnDto<ReceivingAuthority, ReceivingAuthorityDto>` xanh ngay, field khớp 1:1 (`id`/`name`/`type`/`contact`/`address`/`isActive`/`note`, enum `type` cùng 2 giá trị `LAB`/`AUTHORITY` — xác nhận bằng curl thật, không suy đoán); (2) grep `@PreAuthorize` của `ReceivingAuthorityController` — chỉ `SCHOOL_ADMIN`/`MEDICAL_STAFF`, đúng với nơi tiêu thụ duy nhất (`medical-staff/samples/page.tsx`, datalist gợi ý nơi nhận); (3) chỉ có GET, không verb nào cần đổi. Thêm `receiving-authorities` vào `REAL_RESOURCES`, thêm `ReceivingAuthorityDto` (`types.ts`), đổi import của trang từ `ReceivingAuthority` (`mock-data.ts`) sang `ReceivingAuthorityDto` (`@/lib/api/types`) — cắt hẳn phụ thuộc mock cho resource này. Kiểm trước khi báo xong (đủ cả 4): `npx tsc --noEmit` sạch · `npm run audit` sạch (54 luật ngữ nghĩa + 53 bảng seed, 0 vi phạm mới) · chạy backend thật (`DB_URL` trỏ cổng `5433` như Bước 2b đã ghi) + `npm run dev` thật, curl qua Next rewrite (không mock): không token → 401, có token `ngoth.medical.synthetic@example.test` (MEDICAL_STAFF) → 200, đúng 6 bản ghi seed `V15`. Backend KHÔNG đổi gì ở bước này (chỉ FE) nên không chạy lại `mvn test`.<br>**2/3 resource còn lại — quyết định dừng lại có chủ đích, không phải lệch field:** (a) `samples` — `RegisterSampleRequest.inspectionId` server tra thẳng bằng mã phiên kiểm thực THẬT trong Postgres (`ReferenceResolver.inspectionIdByCode` + BR-105), nhưng dropdown chọn phiên ở `service-caterers/samples/page.tsx` đọc resource `inspections` — resource này KHÔNG có trong `REAL_RESOURCES` (Thảo để lại CHỦ ĐỘNG CHƯA nối vì `InspectionController` không có PUT/PATCH, cần chốt luồng nghiệp vụ "lưu nháp" trước — không phải việc của MF08 tự quyết). Bật `samples` mà `inspections` còn mock ⇒ mọi lượt đăng ký mẫu gửi mã phiên KHÔNG TỒN TẠI thật ⇒ BR-105 chặn 100% — hỏng hẳn tính năng chính. (b) `sample-lab-requests` — `handOver()` cần Incident thật đang mở qua `IncidentStatusPort`, nhưng MF11 chưa merge nên đây là stub luôn trả "không có sự cố mở" (M08-D08, chọn hướng không khoá nghiệp vụ giả) ⇒ mọi lượt bàn giao thật sẽ luôn 409 SMP-023 (đúng hành vi `FoodSampleFlowIT` đã xác nhận) — không có gì để bật cho tới khi MF11 có backend; ngoài ra `medical-staff/samples/page.tsx` còn phụ thuộc `incidents` (MF11) hoàn toàn mock, và model mock `SampleLabRequest` giàu field hơn `SampleLabRequestResponse` thật rất nhiều (`labDestination` tự do, `sealPhotoFiles`/`handoverRecordFiles`, `cancelledAt/By/Reason`…) — cần rà lại toàn bộ trước khi bật, không chỉ chờ MF11. Ghi đầy đủ trong comment `real-resources.ts`/`contract-check.ts`, không lặp lại ở đây. |
| 2026-08-03 | Mạnh | **MF08 xong Bước 0 + Bước 1** — `TDS-MF08_Backend.md` v1.1 + `TestSpec-MF08` v1.1 → **Approved**; 16 quyết định `M08-D01`…`M08-D16` (TDS §2.4), 6 mục treo (TDS §2.5) không chặn. Số V cấp: **`V14`/`V15`**. **Bốn điều đáng lan sang module khác:** **(1) MF08 là module đầu tiên trong 8 module chưa code SẠCH cả ba lớp va chạm của Bước 0** — 0/4 bảng trùng trên 61 bảng đã merge, 0/3 path trùng trên 35 path, 0 path bị TDS khác cùng khai, 0 FK chết, 0 chỗ `REFERENCES users(id)`. **Nhưng "sạch Bước 0" ≠ "vào Bước 2 được": vẫn tìm ra 9 lệch, 3 trong đó làm build/Flyway đỏ ở lần chạy đầu** — số V (TDS còn ghi *"V1__init_schema.sql do Nam giữ, MF08 thêm 4 bảng vào V1"*, mô hình đã bỏ từ MF02), D9 (4 entity tự khai cột audit), D1 (`UNIQUE(code)` toàn cục + `receiving_authorities` thiếu hẳn cột `code` trong khi §9.2 đã trả `authorityId: "LAB-CDC-HN"` từ v1.0). ⇒ **Bước 0 chỉ bắt va chạm QUYỀN SỞ HỮU; lệch QUY ƯỚC với code đã merge là lớp riêng, phải rà riêng trong Bước 1.** **(2) Lời giải rẻ nhất cho tham chiếu chéo là ĐỌC `ReferenceResolver` TRƯỚC KHI KHAI PORT.** TDS-MF08 v1.0 khai **3 port**; đọc code thật thì **2 trong 3 là viết lại hạ tầng đã có**: `GeofenceQueryPort` (BR-109) trùng nguyên `ReferenceResolver.schoolGeofence()` **đã tồn tại** (`ReferenceResolver.java:461-464`, đọc `school_config.geofence_radius` của `V3`), và `InspectionGatePort` (BR-105) là **tra cứu thuần** `inspection_sessions.overall == PASSED` ⇒ thêm 1 method vào `ReferenceResolver` (append-only theo §5) thay vì bắt Thảo mở port — **MF07 đã merge mà không expose port nào**, nếu không kiểm thì đây là một cuộc họp và một vòng chờ. Port thật chỉ còn `IncidentStatusPort` (MF11 **chưa** merge, hành vi chứ không phải tra cứu) ⇒ stub + `log.warn`, tiền lệ `M07-D08`. **Đề nghị thêm vào checklist Bước 0: `grep "public " common/reference/ReferenceResolver.java` trước khi khai bất kỳ Port nào.** **(3) Bảng D §2.1 sai ô của MF08 ở CẢ HAI cột, và hai lý do đều là điểm mù của lệnh grep** — cột audit ghi `—` nhưng thật ra **4 bảng** tự khai (grep đếm theo khối `CREATE TABLE` của từng bảng nên 4 lần khai gộp thành 0; MF07 sót vì lý do khác — khai ở Entity trước, DDL sau); cột package ghi `—` vì **TDS-MF08 không nhắc package lần nào**, trong khi **`TestSpec-MF08` ghi `com.sfms.sample` ở 40 chỗ** (7 tên package + 33 đường dẫn file test). ⇒ **lệnh của bảng D phải quét cả `TestSpec-*.md`, không chỉ `TDS-*.md`.** **(4) "Chưa có MSG" lại là CHƯA GREP — lần thứ hai liên tiếp.** TDS-MF08 §10 có ghi chú *"`error_message.md` hiện chỉ cấp cho MF08 đúng 4 mã… đánh dấu để Huy quyết định"*, trong khi **v1.5 (29/07) đã cấp đủ MSG-119…MSG-128** cho cả 9 mã và **chính bảng §10 phía trên đã trích đúng chúng** — ghi chú mâu thuẫn với bảng ngay trên nó suốt 5 ngày. Nhặt thêm 3 mã dùng chung MF08 bỏ sót (MSG-138/139/141) ⇒ **20 MSG, 0 mã phải xin cấp**. Kèm cảnh báo ngược đã thành khuôn: MSG-020 và MSG-120 **ghi cứng ngữ cảnh MF08** trong chuỗi tiếng Anh, module khác **không** mượn được (MF07 đã suýt mượn cả hai). **D13:** 3/3 role có seed ACTIVE, nhưng **3 fixture JWT của TestSpec trỏ sai người** (`U-0021` là school-admin không phải bếp; `U-0009` là teacher không phải y tế; `U-0050`/`U-0033` không tồn tại) — sửa hết; còn **1 lỗ hổng thật**: `V900` không có `medical-staff` nào ở trường thứ hai ⇒ xin Nam 1 dòng. Đếm D11: **3/13 → 4/13** (`MF08-TC-SEC-009`, kết luận *"không áp dụng"* vì lọc theo **dòng** chứ không theo field — cùng lý do MF06); D12 nay có `MF08-TC-021` (ca **nặng**: 6 field dẫn xuất + biến thể "cùng tên khác kiểu" `weight` chuỗi vs `amount` số, mà mock **đang gửi thật**). |
| 2026-08-03 | Thảo | **MF07 — 2 TC bắt buộc D11/D12 + 5 bug thật tìm lúc viết test, RỒI Bước 3 (2/4 resource nối thật).** Tiếp tục từ dòng "Bước 2 xong, chưa sang Bước 3" bên dưới cùng ngày. **Phần 1 — D11/D12 (§6, trước khi được phép sang Bước 3 theo quyết định của nhóm):** viết `InspectionRoleFieldParityIT` (`MF07-TC-SEC-017`) và `InspectionDerivedFieldRejectionIT` (`MF07-TC-029`), cả hai **sửa lỗi thật trong chính văn bản TestSpec** khi viết: (a) TestSpec ghi "U-0003 dùng partnerId=SUP-001" — sai, `SUP-001` là `RAW_FOOD_SUPPLIER` không phải Caterer, và V900 thật không gán `partnerId` cho U-0003; dùng `U-0022` (partnerId=CAT-001) thay. (b) TestSpec ghi "field lạ/sai kiểu → 422" cho D12 — chạy thật ra **200/201**, xác nhận chéo với `MenuWriteContractIT` (MF05) đang PASS với đúng hành vi này; viết lại test theo THẬT: D12 kiểm bằng response+DB state, không phải HTTP status. Viết test lộ ra **5 bug thật, đã sửa**: (1) `checkGate()` dùng nhầm query loại-trừ-FAILED nên khi phiên FAILED thật vẫn báo `NO_SESSION`; (2)+(3) System Admin (`schoolId=null` trong JWT) bị lọc còn 0 dòng ở CẢ `/inspections` VÀ `/kitchen-inspections` (thiếu nhánh `schoolId == null` trong `InspectionSpec`/`KitchenInspectionServiceImpl.list()`); (4) `ChecklistGroupRepository.findBySchoolIdAndIsActiveTrueOrderByDisplayOrderAsc` sai tên theo Spring Data (getter `isActive()` ⇒ property tên `active`, không phải `isActive`) — app không khởi động được; (5) `nextItemCode()` tính mã hạng mục bằng `itemRepository.count()` TRƯỚC khi flush — nhiều item cùng request ra cùng mã, vỡ `uq_cli_code`. **Phần 2 — Bước 3 (nối FE thật), theo đúng khuôn `real-resources.ts` + `contract-check.ts`:** đối chiếu `schema.d.ts` sinh từ backend đang chạy với DTO Java (§7 Bước 3 việc 1) lộ thêm **2 lỗi thật KHÁC**, cùng lớp Huy đã tìm ở MF06: `ChecklistDtos.ItemRequest`/`ItemResponse` TRÙNG TÊN `mf06.dto.PurchaseRequestDtos.ItemRequest`/`ItemResponse` — springdoc gộp theo simple-name (không theo package) nên `schema.d.ts` chỉ giữ MỘT shape, `tsc` không báo gì; sửa bằng `@Schema(name=...)` tiền tố `Checklist`/`SafetySignoff` cho MỌI record 2 file DTO đó (không chỉ 2 chỗ trùng thật, phòng va chạm MF12 sau này). Và `KitchenInspectionResponse` thiếu `confirmedBy`/`confirmedAt` dù entity đã set — 3 trang đang đọc 2 field đó luôn thấy `undefined`; bổ sung vào DTO + service. Việc mới ở `hooks.ts` (thêm, không refactor — đúng §5): `usePlainList` (GET trả thẳng mảng, không `Paged<T>` — `checklist-groups` là danh mục nhỏ theo trường, không phân trang, cùng khuôn `permissions`) và mở rộng `useDelete` nhận `{id, params}` (cho `?confirmed=true` của CF-15/BR-210). **Nối thật `checklist-groups`** (`medical-staff/conduct-inspection/{checklist,page}.tsx`, đổi 2 chỗ `usePagedList`→`usePlainList`, và chặn rò id tạm `CLI-NEW-n` phía client thành mã vĩnh viễn khi tạo hạng mục mới — backend dùng thẳng `ItemRequest.id` nếu khác rỗng) và **`safety-signoffs`** (`school-admin/compliance/page.tsx`, cắt payload ký chỉ còn field không-dẫn-xuất — trước đó gửi cả 6 field server tự tính, vẫn 200 nhưng vô nghĩa, đúng việc 5 của Bước 3). **`inspections`/`kitchen-inspections` CHỦ ĐỘNG CHƯA nối** — không phải lệch field (2 contract-check đã xanh) mà lệch KIẾN TRÚC ghi: `inspections` không có PUT/PATCH, chỉ 5 action POST, trong khi màn nhập liệu chính (`service-caterers/inspections/[id]/page.tsx`, ~900 dòng) đang dùng PUT tuỳ ý field cho cả "lưu nháp" (khái niệm backend không có) lẫn "chốt bước" — cần quyết định luồng nghiệp vụ, không phải đổi verb, để lại cho chủ màn (Minh, theo §5); `kitchen-inspections` thì luồng chính khớp sạch nhưng tính năng "biên bản ký tay" (`.../record/page.tsx`, 6 field `record*`) chưa có backend nào — bật resource sẽ khiến nút "Tải bản ký" 200-mà-không-lưu-gì (D12) mà người dùng tưởng thành công. Chi tiết đầy đủ từng field/từng quyết định nằm trong comment `real-resources.ts`/`contract-check.ts`, không lặp lại ở đây. **Kiểm trước khi báo xong (đủ cả 4, không bỏ bước nào):** `mvn -o test -Dtest=com.sfms.mf07.**` **10/10 pass** (4 file IT, kể cả sau đổi tên DTO); `npx tsc --noEmit` sạch; `npm run audit` sạch (54 luật ngữ nghĩa + 53 bảng seed, 0 vi phạm mới); chạy backend thật + `npm run gen:api` xác nhận `schema.d.ts` đúng field; curl qua Next rewrite thật (không mock): `GET /checklist-groups`/`GET /safety-signoffs` không token → 401, có token đúng vai trò → 200 với shape thật (`[]` mảng phẳng / `{items,total,page,pageSize,pageCount}`). |
| 2026-08-03 | Thảo | **MF07 Bước 2 (backend) + Bước 2b (gen:api) — xong, chưa sang Bước 3.** Package `com.sfms.mf07` đầy đủ: 12 entity (`InspectionSession` aggregate root + 4 bảng con Mẫu 1/2/3 + `InspectionEvidence`, `KitchenInspection` + `ChecklistGroup`/`Item`/`ItemResult`, `SafetySignoff`), 6 repository, 5 DTO file, 6 service + 4 controller (22 endpoint đúng phạm vi §9.2 sau khi trừ 8 endpoint chuyển MF11/MF13), `V12`/`V13` (migration + seed 6 nhóm/34 mục checklist từ `CHECKLIST_GROUPS_SEED`), `INSP` đăng ký vào `ModuleCodeResolver`, `com.sfms.mf07.error.InspMessages` chép nguyên văn `error_message.md` v1.10 (đã đối chiếu MSG-016/017/056/093/129/145-148 từng chuỗi, khớp 100%). Compile sạch (`mvn -o compile`), `mvn -o test` toàn project **192/192 pass**, không regression MF04/05/06 (đã tự kiểm scoped trước khi full run). **2 IT test thật** (Testcontainers Postgres 16, KHÔNG mock): `InspectionSessionFlowIT` (mở phiên→3 bước→đồng ký, D5 trùng slot 409, BR-079 khoá tuần tự 409, BR-090 nhiệt độ thấp 422, BR-083 thiếu ảnh 422, BR-096 tái kiểm tra, D9 audit columns not-null) + `KitchenInspectionAndChecklistFlowIT` (tạo checklist qua API→chấm điểm bếp tính lại server-side CF-26→BR-221 Caterer xác nhận, BR-207 thiếu ảnh/lý do 422, `uq_signoff_day` 409) — 4/4 pass. **Bước 2b:** chạy backend thật (`docker-compose` reset vì DB dev cũ có checksum lệch V10 từ phiên MF06 trước — không phải lỗi của MF07, xử lý bằng `docker compose down -v` vì chỉ là dữ liệu synthetic dev), 13 migration áp sạch từ DB trống, `npm run gen:api` — xác nhận `PagedResponseInspectionSummaryResponse.items` gõ đúng `InspectionSessionResponse[]` (không rỗng `{}`), không `oneOf` (D11 "không áp dụng", đúng lý do đã ghi ở TDS — BR-099 lọc row-level). **Simplification biết trước, ghi nợ tài liệu thay vì giấu:** (1) BR-095 "tự động tạo CAPA" khi một bước trượt — log warn thay vì gọi thật, vì MF11/`CapaPort` (M07-D08) **chưa tồn tại** trong code đã merge; nối khi MF11 lên bean. (2) `GET /inspections/print` (endpoint #29, dựng bản in server-side) **chưa hiện thực** — không call-site nào phụ thuộc (§9.1), FE tự render tại chỗ. (3) `SEARCH_FIELDS` của `GET /inspections` chỉ còn `staffNotes` — `inspector`/`dish` là nhãn DẪN XUẤT qua `ReferenceResolver`, Criteria API không join thẳng được mà không phá ADR-M02-5; ghi vào TDS §2.5 khi có ai cần. **Việc CHƯA làm ở Bước 2 này (khác với "không làm được"):** test coverage mới phủ 2/62 TC của TestSpec (đủ để xác nhận luồng chính + các lỗi nghiệp vụ nặng nhất chạy đúng, KHÔNG đủ để coi là hoàn thành theo TestSpec) — 60 TC còn lại (đặc biệt `MF07-TC-SEC-017`/`MF07-TC-029` D11/D12 bắt buộc) để làm tiếp, theo đúng tinh thần Huy đã làm với MF06 (dừng có chủ đích, ghi rõ, không claim xong). |
| 2026-08-03 | Huy | **MF06 — xử lý xong 6/6 điểm treo Bước 3 đã liệt kê trước đó** (verb/role/action, không phải field remap phía đọc — mục đó vẫn treo, xem `real-resources.ts`). Tất cả xác nhận qua **curl thật** (login synthetic seed `*.synthetic@example.test`/`P@ssw0rd123`), không suy đoán.<br>**Backend, việc mới ngoài Bước 2:** (1) thêm action `POST /purchase-requests/{code}/reject` [SUPPLIER, PENDING/PRICE_ACCEPTED] — trước đây `REJECTED` không có action nào khớp; (2) `confirm-schedule` nhận thêm `batchAssignments` (map mã nguyên liệu → mã lô), thêm cột `purchase_items.supplier_batch_id` (chưa từng có) để lưu — BR-071 "gán lô hàng" trước đây chỉ tồn tại phía FE, không có chỗ lưu; (3) thêm bảng `receiving_evidence_documents` + `POST /receiving-sessions/reject-documents` (multipart) — ảnh từ chối lô (BR-072/078) trước đây là dataURL client-side, vi phạm D10-BIS.3 (đòi document ID thật); (4) đóng gap `extraBatches`: cột `preparation_batches.extra` **có sẵn trong schema từ Bước 2 nhưng chưa từng được ghi/đọc** (`insert ... values (?, ?, false)` hardcode) — nối nốt cả hai chiều, bỏ được `Omit<>` đã ghi ở Bước 2b.<br>**Lỗi thật tìm thấy + sửa ngay đầu Bước 3** (chặn toàn bộ việc nối FE cho tới khi sửa): 4 DTO request lồng nhau trong MF06 (`PreparationRecordDtos.CreateRequest`, `DeliveryDtos.CreateRequest`, `PurchaseRequestDtos.CreateRequest`, `SupplierBatchDtos.CreateRequest`) đặt tên record y hệt `CreateRequest` → springdoc gộp thành 1 schema OpenAPI, `openapi-typescript` chỉ sinh đúng 1 shape (của `SupplierBatchDtos`), 3 endpoint kia nhận type SAI trong `schema.d.ts` mà **`tsc` không báo lỗi gì** (type vẫn hợp lệ cú pháp, chỉ sai field) — chỉ lộ khi gọi thật. Sửa bằng `@Schema(name=...)` phân biệt. **Đề nghị các module sau kiểm ngay từ Bước 2:** `grep -c "record CreateRequest("` trên toàn bộ DTO của module.<br>**FE, việc chính:** `receiving/new/page.tsx` viết lại từ 5 mutation rời rạc (`inventory`×2, `deliveries`, `purchase-requests`, `supplier-batches`) thành **1 lệnh** `POST /receiving-sessions` — xác nhận server tự làm hết (tạo session+lines, upsert inventory kể cả nhánh UPSERT lô đã tồn tại, chuyển delivery→DELIVERED) trong 1 transaction (ADR-M06-5), KHÔNG đụng purchase_request/supplier_batches như từng nghi ở Bước 3 đợt trước — đo được từ code thật, không phải suy đoán nữa. Luồng xuất kho FEFO (`inventory/issue/page.tsx`) viết lại quanh `POST /inventory/issue-slips` thay vì loop POST từng dòng vào `inventory-transactions` (sổ append-only không có verb tạo trực tiếp cho nghiệp vụ xuất). Quarantine, `/adjustments`, cả 5 action của `purchase-requests` đều đổi từ `useUpdate`(PUT nguyên object) sang đúng verb/subpath thật.<br>**Quyết định dừng lại có chủ đích:** KHÔNG bật `inventory`/`inventory-transactions`/`deliveries`/`supplier-batches` vào `REAL_RESOURCES` phiên này — verb/role/action đã đúng nhưng field phía ĐỌC còn lệch (`quantity` chuỗi ghép đơn vị → cần tách `number`+`unit`, `driver` tên không tồn tại chỉ có `driverId`, `certificates`→`certificateIds`) trải trên **~16 file** (nhiều hơn 4, vì `.quantity` xuất hiện ở các màn không liên quan như dish ingredient/special-meal) — bật công tắc mà chưa rà hết là vỡ hàng loạt trang đang chạy tốt trên mock. Ghi rõ trong `real-resources.ts` để làm tiếp. `tsc --noEmit`/`npm run audit` sạch, scoped `mvn verify -Dtest="com.sfms.mf06.**"` xác nhận **16/16 test MF06 vẫn pass**, không regression (log thật, không suy đoán). Lần chạy này báo `BUILD FAILURE` ở bước JaCoCo coverage-check — **không phải lỗi MF06**: coverage-check tính trên toàn bộ project, mà chạy scoped bỏ qua test của các module khác (`com.sfms.iam.service`/`web` báo 0.21/0.37 vì test IAM không chạy trong lần này) — đúng đánh đổi đã biết của scoped-test (nhanh hơn nhiều nhưng JaCoCo gate chỉ đáng tin ở lần `verify` full cuối cùng, chưa chạy trong phiên này). |
| 2026-08-03 | Huy | **MF06 Bước 2 (backend) + Bước 2b (gen:api) + Bước 3 một phần (2/8 resource).** Backend: package `com.sfms.mf06` đầy đủ (20 entity, 7 nhóm DTO, 11 repository, 10 service, 8 controller) + `V10`/`V11`, `ING` đăng ký vào `ModuleCodeResolver`, compile sạch. **Test CHƯA đủ theo TestSpec** — 16/97 TC (4 file `*IT`), thiếu toàn bộ 23 unit test service (`FefoAllocator`, `InventoryService`...) và phần lớn `INT-`/`SEC-` còn lại; **tạm dừng theo yêu cầu, chuyển sang Bước 3, sẽ làm nốt sau**. **Sự cố ngoài kế hoạch:** agent nền sinh code 2 lần đụng giới hạn chi tiêu API của tài khoản giữa chừng — không phải lỗi kỹ thuật, phải resume thủ công + chuyển sang chạy `mvn` trực tiếp (tìm thấy Maven 3.9.9 tại cache wrapper `~/.m2/wrapper/dists`, JDK 25 tại `C:\Program Files\Java\jdk-25.0.3`) thay vì qua agent để tránh lặp lại. **Đổi cách chạy test cho các module sau:** chạy scoped `-Dtest`/`-Dit.test="com.sfms.mfXX.**"` trong lúc code, chỉ `verify` full toàn project một lần cuối — nhanh hơn nhiều lần so với full verify mỗi vòng sửa.<br><br>**Lỗi thật tìm thấy + đã sửa ở Bước 2b:** 4 DTO request lồng nhau trong MF06 (`PreparationRecordDtos.CreateRequest`, `DeliveryDtos.CreateRequest`, `PurchaseRequestDtos.CreateRequest`, `SupplierBatchDtos.CreateRequest`) đặt tên record y hệt `CreateRequest` — springdoc gộp cả 4 thành MỘT schema OpenAPI, `openapi-typescript` chỉ sinh đúng 1 shape (của `SupplierBatchDtos`) còn 3 endpoint kia nhận type SAI trong `schema.d.ts` mà `tsc` không hề báo lỗi (type vẫn hợp lệ, chỉ sai field) — chỉ lộ ra khi gọi thật. Sửa bằng `@Schema(name = "...")` cho từng record (theo đúng pattern đã dùng cho `PagedResponse*`). **Đề nghị các module sau kiểm ngay từ Bước 2:** `grep -c "record CreateRequest("` trên toàn bộ DTO của module — nếu >1 file cùng tên record, phải đặt `@Schema(name=)` phân biệt trước khi sinh FE.<br><br>**Bước 3 — kết quả đo được 03/08/2026 (research agent + xác minh curl thủ công):** chỉ **`preparation-records`** và **`traceability`** trong 8 resource của MF06 an toàn để nối ngay (verb+role+field khớp, hoặc chưa ai tiêu thụ) — đã thêm vào `REAL_RESOURCES`, `contract-check.ts` (`preparation-records` trừ `Omit<"extraBatches">` — DTO thật không trả field này, ghi không mất dữ liệu vì FE đã gộp vào `batchesUsed` trước khi POST, chỉ mất khả năng highlight lô phát sinh khi đọc), `tsc --noEmit` sạch, `npm run audit` sạch, xác nhận qua Next rewrite thật: có token → 200 (dữ liệu Postgres thật), không token → 401. **6/8 resource còn lại (`inventory`, `inventory-transactions`, `deliveries`, `purchase-requests`, `receiving-sessions`, `supplier-batches`) CHƯA nối** — đều quy về cùng một quyết định kiến trúc chưa ai chốt: luồng `POST /receiving-sessions` phải tự làm hộ việc cập nhật delivery/purchase-request/supplier-batch/inventory ở phía server (FE hiện làm 5 mutation rời rạc, DTO thật `ReceivingSessionCreateRequest` chỉ nhận `{deliveryId, lines[]}` ngụ ý server làm tất cả trong 1 transaction), cộng `purchase-requests` cần chốt state machine cho `REJECTED` (không có action nào khớp), cộng `receiving-sessions` cần đổi tên resource ở FE (`"receiving"` → `"receiving-sessions"`) tại toàn bộ ~6 call site cùng lúc. Chi tiết đầy đủ (từng file, từng dòng, từng field lệch) ghi trong comment `real-resources.ts`. **Việc dev test tay:** DB dev local (`docker-compose` postgres, không chạy `V900` — file đó chỉ áp dụng lúc test) có sẵn tài khoản synthetic từ `V5`/`V11` (`*.synthetic@example.test`, cùng mật khẩu `P@ssw0rd123` với seed test) — dùng `duchm.caterer.synthetic@example.test` để login thử SERVICE_CATERERS. |
| 2026-08-03 | Thảo | **MF07 — Nam + Huy xác nhận, đóng 7/9 mục treo.** `TDS-MF07` **v1.3** + `TestSpec-MF07` **v1.2**. **Hai thứ đáng lan sang module khác:** **(1) "Chưa có MSG" thường là CHƯA GREP, không phải chưa có.** MF07 vào Bước 1 với **8 mã** ghi `*(không có MSG riêng)*`; ra khỏi Bước 1 với **0**, mà chỉ phải xin Huy cấp **4**. Bốn mã kia có sẵn: `INSP-094`/`INSP-098` khớp **nguyên văn MSG-129** (có từ v1.5, chỉ là trước đó chỉ gán cho `SRV-094` của MF09), cộng 3 mã dùng chung MSG-137/138/141 mà MF07 thiếu cả ba. **Đề nghị vào checklist Bước 1: grep `error_message.md` theo NGỮ NGHĨA trước khi kết luận thiếu mã.** Kèm cảnh báo ngược: MF07 **suýt mượn nhầm 2 lần** — MSG-020 (*tủ lưu mẫu 2-8°C*, BR-110/MF08) cho nhiệt độ **giữ ấm 60°C**, và MSG-120 (*"for food sample retention"*) cho **ảnh bằng chứng kiểm thực**. Cả hai ghi cứng ngữ cảnh trong chuỗi tiếng Anh ⇒ mượn là sai hiển thị với người dùng thật, đúng khuôn lỗi đã phải sửa ở MSG-128 (v1.5) và MSG-142 (v1.9). **Đọc nguyên văn chuỗi, đừng chỉ nhìn mã.** `error_message.md` v1.10 cũng **tách** nhóm *"Food Safety Inspection"* thành 2 dòng — 6 mã của ATTP Report Center (MSG-091/092/096/097/098/099) thuộc **MF12 §1.12**, trước đó ai tra "mã nào của MF07" đều lấy nhầm. **(2) Chính thức hoá field "không có BR/UC nguồn" làm TEST MẠNH LÊN, không chỉ đổi nhãn.** CF-10 của MF07 (6 field chỉ có nguồn từ file review) được Huy chốt chính thức hoá ⇒ `business_rules.md` **v2.1** cấp **BR-221**/**BR-222**, **không cấp UC mới** (gắn vào UC-014/UC-112 sẵn có). Điểm mấu chốt: file review chỉ mô tả *field tồn tại*, còn BR mới là chỗ nói rõ field đó **KHÔNG được làm gì** — BR-221 *không* chặn vận hành bếp; BR-222 *không* ghi đè `overall`, *không* đổi kết quả BR-094/098, *không* sinh CAPA. **5 assertion "không" đó trước 03/08 không viết nổi vì thiếu oracle**; nay viết được, và chúng chính là hàng rào ngăn người sau "tiện tay" cho đánh giá của Y tế ghi đè kết luận của đơn vị thực hiện. Module nào còn field dạng "mock có, BR không có": **đừng để treo — hoặc cấp BR, hoặc descope**, vì để treo nghĩa là không có oracle cho các luật *cấm*. **Kèm 1 phát hiện đóng CF-1:** `traceability_matrix.md` **dòng 630** ghi nhóm ATTP *"(gồm Violation, +UC-178, +UC-185, +UC-186)"* = **13 UC** — chênh "11 vs 13" chính là 2 UC đó, và bảng tổng kết của matrix **đã gộp UC-186** vào phạm vi ATTP, khớp `M07-D10`. **Còn treo 2 mục, cả hai của Minh (FE)**, không chặn Bước 2. |
| 2026-08-03 | Thảo | **MF07 xong Bước 0 + Bước 1** — `TDS-MF07_Backend.md` v1.2 + `TestSpec-MF07` v1.1 → **Approved**; 16 quyết định `M07-D01`…`M07-D16` (TDS §2.4), 9 mục treo (TDS §2.5) không chặn. **Bốn điều đáng lan sang module khác:** **(1) Bước 0 có một ĐIỂM MÙ đã đo được — hai lệnh grep chỉ thấy thứ ĐÃ MERGE.** MF07 sạch tuyệt đối ở cả hai lệnh (**0 bảng trùng, 0 path trùng** trên 53 bảng + 31 path đã merge), nhưng lại đụng **ba module chưa merge** ở đúng lớp lỗi Bước 0 sinh ra để chặn: MF07↔**MF11** cùng `CREATE TABLE violation_records` (TDS-MF11 dòng 984) **và** cùng `@RequestMapping("/api/v1/violations")` ⇒ Flyway đỏ **cộng** app không khởi động; MF07↔MF11 cùng `corrective_actions` + `/corrective-actions` (dòng 938); MF07↔**MF13** cùng `GET /api/v1/compliance-report` (TDS-MF13 §9.1 hàng 1 — và đó là **endpoint duy nhất của MF13 có trong mock**); MF07↔**MF12** cùng `checklist_groups`/`checklist_items`. Cả 4 còn là file `.md` nên grep code mù hoàn toàn ⇒ **đề nghị thêm lệnh thứ tư vào §2.1**: `grep -rho '/api/v1/[a-z-]*' 07_implement/*/TDS-*.md \| sort \| uniq -d` (path bị ≥2 TDS khai) và `grep -l "CREATE TABLE <bảng của bạn>" 07_implement/*/TDS-*.md`. Cộng với lệnh `REFERENCES` của Huy, Bước 0 thành **5 lệnh** — vẫn dưới 20 phút. **(2) Lời giải là GỠ SCOPE CỦA CHÍNH MÌNH, không phải giành.** `M07-D07`/`D08`/`D09` bỏ 3 resource khỏi MF07 (`violation_records`→MF11, `corrective_actions`→MF11, `compliance-report`→MF13), **đảo lại** xử lý CF-6/CF-14 của bản v1.0 — nhưng vì **bằng chứng mới**, không phải đổi ý: mở TDS của module đối diện ra đọc thì thấy họ đã khai `CREATE TABLE`. Ba mục từng ghi *"Cần Nam chốt để tránh 2 module cùng gen entity"* nay có lời giải cụ thể, Nam chỉ ký chứ không phải thiết kế lại. Nghiệp vụ **không mất gì**: BR-095 tự tạo CAPA vẫn chạy trong cùng transaction, chỉ đổi từ *mở endpoint* sang *gọi `CapaPort` do MF11 expose* (§0-BIS). **(3) `error_message.md` có mã sẵn mà module không biết.** §10 của MF07 v1.1 ghi **8 dòng** `*(không có MSG riêng)*` + message **tiếng Việt** (trái D7). Grep lại thì `INSP-094`/`INSP-098` khớp **nguyên văn MSG-129** đã tồn tại từ v1.5 (*"Meal cannot be served: inspection step [Step Name] did not pass."*), chỉ là trước đó chỉ được gán cho `SRV-094` của MF09; thêm 4 mã **dùng chung mọi module** mà v1.1 thiếu cả bốn (MSG-137/138/139/141). **8 → 4** mà không xin cấp mã nào. Đề nghị: **grep `error_message.md` trước khi kết luận "không có MSG"** — và sửa bảng phân nhóm cuối file đó (dòng 219 xếp MSG-091/092/094/096/097/098/099 vào *"Food Safety Inspection"* trong khi cả 7 thuộc UC-185…189 ở **§1.12/MF12**). **(4) Bảng D §2.1 ghi SAI cột audit của MF07 (`—`, thật ra là 3).** Bảng đếm bằng grep trong khối ```sql, mà MF07 khai `created_at`/`updated_at` **ở tầng Entity trước, DDL sau**. Đúng lớp lỗi D9 mà Huy đã cảnh báo là **đắt nhất** (duplicate column ⇒ build đỏ ngay). Ai dùng bảng D để ước lượng công việc: **đếm cả khối ```java**. Đếm D11: **2/13 → 3/13** (`MF07-TC-SEC-017`); D12 nay có `MF07-TC-029`. **D13 là mục đầu tiên trong 8 module rà mà KHÔNG phải sửa gì** — `V900` đã có tài khoản ACTIVE thật cho cả 5 role MF07 dùng. |
| 2026-08-03 | Huy | **MF06 xong Bước 0 + Bước 1** — `TDS-MF06_Backend.md` v1.1 + `TestSpec-MF06` v1.1 → **Approved**; 16 quyết định `M06-D01`…`M06-D16` (TDS §2.4), 7 mục treo (TDS §2.5) không chặn. **Sửa §3:** cột Backend của **MF04 và MF05** đang ghi `—` nhưng cả hai **đã merge** (`V6`…`V9`, `com.sfms.mf04`/`mf05`, 8 path controller) — bảng viết 02/08 và không được cập nhật khi họ merge. **Bốn điều đáng lan sang module khác:** (1) **Bước 0 tiếp tục sinh lời, nhưng ở lớp khác MF05/MF12** — MF06 **0 bảng trùng, 0 path trùng**, đổi lại mắc **3 FK trỏ bảng chưa bao giờ tồn tại** (`supplier_documents` — tên thật `partner_documents`; `supplier_products`; `catalog_ingredients`). Hai lệnh grep của Bước 0 **không** bắt được lớp này vì chúng liệt kê bảng *đã có*, không đối chiếu bảng *được tham chiếu* ⇒ **đề nghị thêm lệnh thứ ba vào Bước 0**: `grep -oh "REFERENCES [a-z_]*" <TDS của bạn> | sort -u` rồi so với danh sách `CREATE TABLE`. MF06 dính 3, và §2.1 C2 đã đếm MF07/MF10/MF12 dính thêm 21 chỗ nữa — tổng **24 FK chết** trên 4 module. (2) **`catalog_ingredients` (C3) nay có chủ và có lời giải cụ thể**: MF06 `CREATE TABLE` trong `V10` + `ALTER TABLE dish_ingredients ADD CONSTRAINT fk_dish_ing_catalog FOREIGN KEY (ingredient_code) REFERENCES catalog_ingredients(code)` — FK trỏ **`code`** chứ không `id`, vì cột MF05 đã merge là `VARCHAR(64)`; đổi kiểu cột của MF05 = sửa `V8` đã merge ⇒ không làm. **MF05 hết treo M05-D15.** (3) **D9 đắt hơn D1**: `issue_slips.created_by UUID` đụng cột reserved của `BaseEntity` ⇒ **Hibernate duplicate column, build đỏ ngay** — trong khi lệch D1 chỉ sai hợp đồng, vẫn chạy. 5 module còn lệch cột audit ở bảng D §2.1 nên rà **trước** D1. (4) **D11 có thể kết luận "không áp dụng" mà vẫn phải có TC**: MF06 không endpoint nào đổi shape theo role (khác biệt là **tập dòng**, không phải tập field) ⇒ chốt một DTO/resource, **không** `oneOf`; nhưng vẫn viết `TC-SEC-014` assert tập field **giống nhau** giữa 4 role — vì MF02/MF03 cũng "không có vấn đề" lúc viết TDS rồi **về sau** mới thêm nhánh rút gọn. Đếm D11: **1/13 → 2/13**. |
| 2026-08-03 | Thảo | **Rà đủ 8 module chưa code bằng Bước 0 — kết quả chi tiết ở §2.1 A/B/C/D.** Sửa **2 dương tính giả** của lần rà đầu: MF09 `GET /special-meals` **không phải lỗi** (TDS-MF09 §9.2 dòng 969 đã ghi *"read-only, sở hữu bởi MF05/MF03"*), MF11 `/auth` **không phải lỗi** (chỉ là `curl` login trong §15 mẫu thử). Đổi lại, tìm thêm **3 lỗi xuyên module nặng hơn**: **C1** — `code_sequences` thật là counter **phẳng theo `scope`** (`V1:31`), không có `school_id`/`year`, trong khi D1 + ADR-M11-5 chốt khuôn mã `<PREFIX>-<năm>-<số>` ⇒ **`CodeGenerator` không sinh được khuôn đó**; MF11 định `CREATE TABLE` lại bảng này (sẽ phá `CodeGenerator` của MF01/02/03/04). Lời giải **đã có sẵn trong code**: MF03 nhét năm vào chính chuỗi `scope` (`"mf03_registration_no:" + Year.now()`) — lấy làm quy ước chung, không đổi schema. **C2** — **21 chỗ `REFERENCES users(id)`** (MF07 ×9, MF10 ×7, MF12 ×5) trong khi bảng tên `user_accounts`; MF07 sai cả kiểu (`VARCHAR(32)` vs UUID). **C3** — **`catalog_ingredients` không TDS nào tạo** nhưng MF05 + MF06 đều tham chiếu ⇒ Huy bổ sung vào migration MF06. Kèm bảng D: 5 module còn lệch PK `VARCHAR`/package/cột audit. Đếm D11: **0/13 TestSpec có TC thật** trước 03/08 (MF04 có 1 dòng checklist, không phải test) → **1/13** sau khi MF05 thêm `TC-SEC-015`. |
| 2026-08-03 | Thảo | **Thêm Bước 0 (§2.1) + ghi chú riêng cho MF12 (§2.2) + prompt Bước 0 (§7) + 2 TC bắt buộc D11/D12 vào prompt Bước 1 (§7 việc 3).** Lý do **không phải suy đoán**: chạy 2 lệnh grep của Bước 0 lên 9 module chưa code thì **4/9 mắc** — MF05 (`special_meal_requests` + `/special-meals` của MF03), **MF12** (`audit_logs` + `/audit-logs` của MF01, cộng **5 FK `REFERENCES users(id)`** trong khi bảng tên `user_accounts`), MF09 (`/special-meals`, `/students`), MF11 (`/auth`). Mỗi ca đều dẫn tới **Flyway đỏ trên máy cả nhóm** hoặc **2 controller 1 path ⇒ app không khởi động**, và đều tránh được bằng 2 lệnh grep ⇒ Bước 0 là **điều kiện vào Bước 1**, không phải khuyến nghị. Về D11/D12: `grep "oneOf"` trên 13 TestSpec ra **0 kết quả** — cả bộ viết trước khi D11–D13 tồn tại nên **không TC nào phủ**; MF05 phải **thêm mới** `TC-SEC-015`/`TC-027`, 9 module còn lại cũng phải. §2.2 liệt kê 6 điểm cụ thể MF12 phải sửa + 2 chỗ chồng lấn cần chốt CHỦ (`sent_reports` với MF13 — trùng CF-M11-20; `checklist_*` với MF07 — `CREATE TABLE IF NOT EXISTS` **không** phải giải pháp vì làm schema phụ thuộc thứ tự merge mà Flyway vẫn xanh). |
| 2026-08-03 | Thảo | **MF05 xong Bước 1** — `TDS-MF05_Backend.md` v1.2 + `TestSpec-MF05` v1.2 → **Approved**; 16 quyết định `M05-D01…M05-D16` (TDS §2.4), 8 mục treo (TDS §2.5) không chặn. §3 cập nhật theo. **Ba điều đáng lan sang module khác:** (1) **§1 của file này được xác nhận lần nữa** — 10/18 lỗi của MF05 (`L19`–`L28` trong TestSpec §2.-1) chỉ tìm được bằng cách **mở code đã merge**, không có mục nào trong đó rà tài liệu bắt được; (2) **kiểm "resource này đã có chủ chưa" phải là bước đầu tiên của Bước 1** — MF05 đặc tả lại nguyên `special_meal_requests` + `/api/v1/special-meals` mà MF03 **đã chạy từ `V4`**, nếu code theo tài liệu cũ thì Flyway đỏ và hai controller cùng path làm **app không khởi động được**; lệnh rà: `grep -rn "@RequestMapping" src/main/java/com/sfms/*/web/` + `grep -n "CREATE TABLE" src/main/resources/db/migration/*.sql`; (3) **D11/D12 cần TC riêng, không tự có** — MF05 phải **thêm mới** `TC-SEC-015` (D11) và `TC-027` (D12) vì bộ 56 TC viết trước D11–D13 không phủ mục nào. Đề nghị 9 module còn lại kiểm cả ba điều này ngay đầu Bước 1. |
| 2026-08-02 | Huy | Lập file, thay bản nháp "Định hướng hoàn thiện Backend + Frontend". Hiệu chỉnh §1 (rà tài liệu là cần, không đủ — kèm bằng chứng đo được). Thêm Bước 2b (`gen:api`). Sửa Bước 3: 3 loại verb thay vì "luôn PATCH", ràng buộc verb+REAL_RESOURCES cùng commit, thêm `contract-check.ts`, đổi OUTPUT sang field-lệch. Đề xuất D11–D13. Bổ sung §5 (Bước 3 đụng màn người khác; `PagedResponse` không được đặt lại `name=`). Thêm §8. |
