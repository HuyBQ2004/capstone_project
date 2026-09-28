# Kế hoạch chỉnh sửa theo `review.md` — bản 2 (đóng băng mock, cắt sang backend theo module)

> **Ngày lập:** 29/07/2026 · **Nguồn:** `07_implement/review.md`
> **Bản 1** (commit `8d09914`) sửa cả ba phía kể cả phần mock sẽ bị xóa. Bản này giữ nguyên mục tiêu nhưng bỏ phần làm hai lần.
> **Nguyên tắc phân xử:** theo cột "Đề xuất" của review; quyết định lớn gom vào `00_Common_Conventions.md` để duyệt một lần.

---

## 0. Nhận định nền: cái gì sống, cái gì chết cùng mock

| Sống tiếp sau khi có backend | Chết cùng mock |
|---|---|
| 168 file `page.tsx` — payload, form, render | `mock-server.ts` (379 dòng) — **toàn bộ** |
| `interface` TypeScript (Student, Feedback, FoodSample…) | `db.ts` (200 dòng) — registry resource |
| `client.ts`, `hooks.ts` | **Giá trị seed** trong `mock-data.ts` (7.144 dòng) |
| `lib/*.ts` helper nghiệp vụ | |

Theo Team Plan §2a, `mock-data.ts` là **"nguồn seed Flyway"** — *dữ liệu* sống tiếp, *file TypeScript* thì không. Sửa `"Nặng"` → `SEVERE` và `"07/07/2026"` → ISO trong 7.144 dòng TS, rồi vài tuần sau chuyển đúng đám dữ liệu đó sang SQL seed, **là làm hai lần**. D4 + D6 chạm phần lớn file đó — đây là khối việc lớn nhất và vô ích nhất của bản 1.

**Hệ quả:** đổi *thứ tự*, không đổi *mục tiêu*. Sửa hợp đồng (TDS) đủ và đúng → sinh backend → cắt FE sang backend thật theo từng module, sửa FE **một lần** với oracle là backend thật thay vì mock.

Cách này trùng đúng tiêu chí nghiệm thu §7 của Team Plan: *"Minh đổi baseURL FE sang backend thật, các màn vẫn chạy"*.

### Số mục thực tế: 76, không phải 69

Đếm lại 9 bảng của review: MF01 7 dòng (tổng kết ghi 6), MF08 9 (ghi 8), MF09 6 (ghi 4), MF10 10 (ghi 9), mục I 14 (ghi 12). Tổng **76 dòng, 36 blocker**. Bảng tổng kết của review cần sửa số khi đóng việc.

### Phân loại 76 mục theo cách xử lý

| Nhóm | Số mục | Cách xử lý |
|---|---|---|
| **N1** — Mâu thuẫn **nội tại TDS** (không dính mock) | ~16 | Sửa ngay, rẻ, giá trị cao — backend sinh từ đây |
| **N2** — Quyết định quy ước (D1–D7) | ~24 | Chốt 1 lần ở `00_Common_Conventions.md`, áp cơ học vào TDS |
| **N3** — FE thiếu field/gate, nằm trong `page.tsx` | ~20 | Sửa lúc cắt sang backend (Phase 3) — code sống |
| **N4** — Type/interface FE lệch TDS | ~30 (chồng N2/N3) | **`openapi-typescript` sinh tự động** → thành lỗi biên dịch |
| **N5** — Thuần mock, chết cùng mock | ~8 | **Bỏ hẳn** |
| **N6** — Backlog FE (màn chưa có) | ~6 | Tách sprint riêng |

---

## Phase 0 — Chốt 7 quyết định xuyên module

**Sản phẩm:** `07_implement/00_Common_Conventions.md`. Đặt ở `07_implement/` chứ không ở `03_Design/02_ADR/` vì nó ràng buộc tầng implement (DTO/JSON/enum Java), không phải kiến trúc hệ thống.

| # | Quyết định | Nội dung chốt | Gỡ kẹt cho |
|---|---|---|---|
| **D1** | **Định danh** | `id` trong JSON **và** path param = **mã nghiệp vụ** (`HS-3A-001`, `SUP-001`). UUID chỉ là PK nội bộ. Entity: `@Id UUID id` + `@Column(unique) String code` + `@JsonProperty("id")`. Theo ADR-M11-5 — quy ước duy nhất khớp FE 100%. | A1, B2, B4, C1, E3, F6, I2, I10 |
| **D2** | **Envelope lỗi** | `{ "error": { "code", "message", "details" } }` ở `@RestControllerAdvice` trong `common/`. FE: interceptor **khoan dung cả hai shape** (`data.error?.message ?? data.message`) → chạy được với mock cũ lẫn backend mới, không phải sửa lại. | A2, A6, A7 |
| **D3** | **Đường dẫn** | `baseURL: "/api/v1"` | A3 |
| **D4** | **Ngày/giờ** | JSON **luôn ISO-8601**. FE thêm `lib/format.ts`, gọi ở **tầng render**. Bỏ `@JsonFormat("dd/MM/yyyy")` ở MF08/09/11. ⚠️ **Không** sửa seed `mock-data.ts` — chuyển vào bước sinh Flyway seed. | A4, C3, E5, H8 |
| **D5** | **ID client sinh** | Theo MF05 CF-11, áp cả 12 module: nhận `body.id` → kiểm trùng → **409**; vắng thì server sinh. | A5, C3, I2 |
| **D6** | **8 enum dùng chung** | `common/enums/`, tất cả SCREAMING_SNAKE. Bảng chi tiết bên dưới. FE giữ nhãn tiếng Việt ở `lib/labels.ts`. | D1, D2, E1, E4, G2, H1, H5, H6 |
| **D7** | **Ngôn ngữ lỗi** | Backend trả `code` + `message` **tiếng Anh** (nguyên văn `error_message.md`); FE tra `lib/messages.ts` → nhãn tiếng Việt. Không dùng `Accept-Language` ở MVP. | A6 |

### Bảng D6 — 8 enum (hiện có 13+ bộ giá trị rải rác)

| Enum | Giá trị chốt | Đang lệch ở đâu |
|---|---|---|
| `MealType` | `BREAKFAST, LUNCH, SNACK` | MF05 đúng; MF07 `"Bữa sáng/trưa/chiều"`; MF10 FE `"Bữa sáng/trưa/phụ"`. Nhãn chốt: `SNACK = "Bữa phụ"`. |
| `AllergySeverity` | `MILD, MODERATE, SEVERE` | MF03 §9.3 `"NANG"`; MF03 §9.2 + MF09 `"Severe"`; FE `"Nhẹ/Trung bình/Nặng"` **và** `"Mild/Moderate/Severe"` ở 2 field khác nhau. |
| `PartnerType` | `RAW_FOOD_SUPPLIER, SERVICE_CATERER` | MF04 §5.1 đúng, §9.2 dùng chuỗi hiển thị. Phân biệt: **role** = `"Service Caterers"` (số nhiều, BR-007), **loại đối tác** = `SERVICE_CATERER`. |
| `RoleKey` | kebab-case: `system-admin, school-admin, teacher, parent, medical-staff, service-caterers, supplier` | TDS dùng nhãn BR-007; FE dùng kebab-case (đồng thời là URL segment). |
| `NotificationType` | `MENU_PUBLISHED, INSPECTION_ALERT, CAPA_ASSIGNED, EXPIRY_ALERT, INCIDENT_ALERT, FOOD_RECALL, ALLERGY_ALERT` | MF10 `GET /notifications` dùng 4 giá trị khác hẳn; FE cũng 4. Chốt 7. |
| `HealthHistoryEventType` | `KHAI_BAO, XAC_MINH, GHI_CHU_LAM_SANG, SU_CO, CAP_NHAT_THUOC` | MF03 §9.3 SNAKE_CASE nhưng thiếu 2 giá trị; FE dùng tiếng Việt có dấu làm nhãn trực tiếp. |
| `ConsumptionStatus` | `PENDING, FULLY, PARTIALLY, NOT_CONSUMED` | MF09 §5.2 đúng; §9.2 message SRV-001 bỏ sót `PENDING`. |
| `BiometricStatus` | `NOT_REGISTERED, PENDING_REGISTRATION, REGISTERED` | MF04 §9.2 chỉ khai 2; seed `DP-03` đang ở `PENDING_REGISTRATION`. |

---

## Phase 1 — Tài liệu nghiệp vụ (Huy)

**T1.1 — `error_message.md` → v1.5.** File dừng ở **MSG-103**; cấp mới **từ MSG-104** cho ~40 mã "cục bộ TDS":

| TDS | Mã cần cấp MSG |
|---|---|
| MF01 | `IAM-006policy`, `IAM-009` |
| MF02 | `SCH-013`, `SCH-024`, `SCH-404` — ⚠️ sửa luôn ghi chú sai: **MSG-013 CÓ tồn tại** (chứng nhận ATTP hết hạn, thuộc MF04), không liên quan GPS |
| MF03 | `STU-201/030/031/032/066/094/172` |
| MF04 | `SUP-048/050/409/404` |
| MF08 | `SMP-105/107/109/111/112/113/030/203/212` + MSG mới thay `MSG-023` (đang mượn sai ngữ nghĩa, CF-M08-6) |
| MF09 | `SRV-001/022/094` |
| MF10 | `FBA-140/REQ/DUP/STATE/404` |

Viết **tiếng Anh** (nhất quán file hiện tại, theo D7). Mã dẫn xuất BR đang viết tiếng Việt trong TDS §10 phải dịch khi đưa vào.

**T1.2 — `traceability_matrix.md`:** ánh xạ MSG-104+ về UC/BR gốc; ghi nhận CF-1/CF-2 của MF01 đang treo.

**T1.3 — `business_rules.md`: KHÔNG sửa.** BR-111 (24h), BR-047 (180/30), BR-145, ADR-M03-3 đã rõ, review đã phân xử **FE sai**.

---

## Phase 2 — Sửa 12 TDS

### T2a — 16 mâu thuẫn nội tại TDS (làm trước, rẻ nhất, không phụ thuộc gì)

Không dính mock. Nếu để nguyên, lập trình viên copy thẳng vào code thành bug thật.

| Mục | TDS | Sửa gì |
|---|---|---|
| C4 | MF02 | §9.2 `POST /classrooms` ghi **400** cho MSG-010, §10 ghi **409** cho cùng `SCH-010` → sửa §9.2 thành **409** |
| C5 | MF02 | Ghi chú §10 sai — MSG-013 tồn tại (MF04), không liên quan GPS |
| C6 | MF02 | Cột "Mock?" của `/promote` ghi ❌ nhưng route FE **có tồn tại** → sửa thành "UI có, chưa nối API" |
| E1 | MF04 | §5.1 `enum PartnerType` ↔ §9.2 dùng `?type=Raw%20Food%20Supplier` — tự mâu thuẫn |
| E8 | MF04 | §9.2 thiếu hẳn schema `GET /supplier-partnerships` (có ở §9.1) → bổ sung, ghi `docStatus` là dẫn xuất |
| G1 | MF09 | `"catererSupplierId": "PTN-0007"` **trỏ nhầm entity** — `PTN-` là `SupplierPartnership.id`, nhà thầu là `Partner.id = "CAT-001"` → đổi thành `"catererPartnerId": "CAT-001"`, sửa cả ADR-M09-8 |
| G2 | MF09 | §9.2 SRV-001 `"must be one of FULLY, PARTIALLY, NOT_CONSUMED"` bỏ sót `PENDING` (có trong §5.2 + CHECK) |
| G3 | MF09 | `PATCH /meal-records/{id}` response thiếu `specialMealServed` (BR-125, FE đang hiển thị) |
| B6 | MF01 | §9.2 thiếu hẳn schema `/roles` → bổ sung; `users: long` tính runtime COUNT, ghi rõ "dẫn xuất, không lưu" |
| B3 | MF01 | §5.2 `user_accounts` thiếu `partner_id` → thêm `partner_id UUID NULL`; JWT claim thêm `partnerId`. Không có nó thì cascade đình chỉ NCC (MF04 ADR-M04-6) không chạy được |
| D8 | MF03+MF09 | Hai endpoint song song cho cùng dữ liệu dị ứng → chốt: MF03 sở hữu *dữ liệu*, MF09 sở hữu *ngữ cảnh bữa ăn*; MF09 gọi MF03 qua port, dùng chung enum. Ghi ADR ở §3 **cả hai** |
| I3 | MF05 | §9.2 ghi rõ backend **bỏ qua** `dishes`/`allergyConflicts` client gửi, tự tính |
| I8 | MF07 | §9.2 thêm dòng: `description` là HTML từ `contentEditable` → **bắt buộc sanitize XSS** |
| I5 | MF06 | `IssueSlip` là dữ liệu mồ côi → chốt **giữ**, bổ sung §5.2 + `data_model.puml` |
| I4 | MF06 | `InventoryBatch.status` dùng `ACTIVE` trùng nhãn "Hoạt động" của đối tác → đổi `IN_STOCK` |
| F9 | MF08 | §10 SMP-023 dùng MSG mới (T1.1) thay vì mượn MSG-023 |

### T2b — Áp D1–D7 vào cả 12 TDS

Với **mỗi** file:
1. **§3** — thêm dòng trỏ `00_Common_Conventions.md` D1–D7
2. **§5.2** — module khai `id UUID PK` không có `code` (MF01/02/03/08/09/10): thêm `code VARCHAR(32) UNIQUE` + `@JsonProperty("id")`. Module khai PK `VARCHAR(32/40/64)` (MF05/06/07/12): đổi UUID PK + cột `code`
3. **§9.2** — path param → `{code}`; ngày → ISO
4. **§10** — mã cục bộ → MSG-104+; ghi envelope D2 ở đầu bảng
5. **§17** — bỏ ràng buộc mâu thuẫn D1–D7

### T2c — Việc riêng từng module (ngoài D1–D7)

| Mục | Việc |
|---|---|
| B1 MF01 `status` | Giữ TDS (`PENDING_VERIFICATION` đúng ADR-M03 + BR-010/011) → FE sửa |
| B2 MF01 trường học | §9.2 `POST /users`: request nhận `schoolId`; response trả **cả** `schoolId` lẫn `school` (tên) |
| B4 MF01 `/staff` | Chốt `school_staff` là **bảng riêng** (có `emailVerified`, `invitedAt`, role 3 giá trị) + cột `user_account_id`. Ghi rõ ánh xạ `STF-003 ↔ U-0010` để `Classroom.teacherId` của MF02 không vỡ |
| C1 MF02 `Classroom.name` | Giữ `name` trong TDS, `id` = mã lớp (D1). §9.2 `PATCH /homeroom-teacher` đang trả `{"id":"3A"}` → `{"id":"CLS-3A","name":"3A"}` |
| C3 MF02 `academic-years` | Response 201 **bổ sung** `startDate`+`endDate` (hiện thiếu → bảng FE trống 2 cột) |
| D1 MF03 `severity` | Áp `AllergySeverity` cả MF03 (2 chỗ) lẫn MF09 — điều khiển cảnh báo đỏ BR-123 |
| D3 MF03 health-file | `emergencyInstructions` → **`emergencyContact`**; thêm `medicalDocuments` vào DTO — nếu không, gate STU-094 làm **mọi** PATCH của FE 400 cứng |
| D4 MF03 `proofFile` | Response trả **cả** `proofFileId` (khóa) lẫn `proofFileName` (hiển thị) |
| D5 MF03 `GET /students` | items[] bổ sung `medicalDocuments`, `emergencyContact` để không mất field FE đang render |
| E4 MF04 `biometricStatus` | Chốt 3 giá trị + định nghĩa chuyển: `POST /face` → `PENDING_REGISTRATION`; xác thực xong → `REGISTERED` |
| E5 MF04 `expiryDate` | §5.2 `expiry_date DATE **NULL**` (NULL = "Không thời hạn"); BR-047 bỏ qua bản ghi NULL |
| E7 MF04 đơn hợp tác | Response trả **cả** `schoolId` lẫn `school` để bảng FE vẫn hiện tên trường |
| F1/F2/F3/F5/F6 MF08 | Giữ TDS toàn bộ. §9.2 ghi rõ `hoursRemaining` **tính runtime**; nhấn: thiếu `amount`/`unit` thì BR-203 không enforce được, thiếu `inspectionId` thì BR-105 **không có dữ liệu để chạy** |
| F7 MF08 `SEARCH_FIELDS` | §17 map qua `@JsonProperty`: `dish_name`↔`dish`, `meal_label`↔`meal`, `collected_by`↔`collectedBy` |
| H2 MF10 ẩn danh | Giữ TDS (BR-145 + ADR-M10-5 — **FE vi phạm quyền riêng tư**). §9.2 tách rõ `FeedbackListItem` (ẩn danh) ↔ `FeedbackDetail` (chủ sở hữu) |
| H3 MF10 `illnessKeyword` | §9.2 ghi rõ **backend tính lại, bỏ qua giá trị client gửi**. Notify Medical Staff (MSG-058/BR-142) chuyển hẳn về server |
| H4 MF10 khóa liên kết | Thiếu `mealSessionId` → BR-139 (cửa sổ 24h) không tính được ở server; thiếu `dishId` → không truy xuất món khi điều tra |
| H5/H6/H7 MF10 | Áp `NotificationType` 7 giá trị, `RoleKey` kebab-case; giữ `audienceCodes` — không có nó thì **mọi vai thấy mọi thông báo** |
| H9 MF10 `revisionCount` | List trả `revisionCount`; `revisions[]` chỉ ở detail cho School Administration (BR-144 + phân quyền) |
| I6 MF07 4 gate ATTP | **Đánh dấu P0 khi sinh backend.** BR-094/095/100/211 hiện chỉ enforce phía client — gọi API trực tiếp là bỏ qua được mọi luật ATTP. Không xếp cùng nhóm cosmetic |
| I9 MF11 events | Giữ TDS (đúng phạm vi + phân trang) → FE đổi khi cắt |
| I10 MF11 `{code}` | **Khớp 100% — không sửa.** Là nguồn của D1 |
| I12 MF12 | Giữ TDS → FE sửa form gửi thiếu `requireMixedCase` |
| I2 MF05 khuôn `--` | Giữ nguyên. Vì D1 giữ `id` = mã nghiệp vụ nên khuôn `{menuId}--{dietGroupId}` **không vỡ**. Thêm test §13 khóa ràng buộc |

---

## Phase 3 — FE (chỉ phần sống tiếp)

### T3.1 — Hạ tầng (làm ngay, ~30 dòng)

| File | Việc |
|---|---|
| `client.ts` | `baseURL` → `/api/v1` (D3). Thêm `class ApiError { code, message, details }` + response interceptor **khoan dung hai shape**: `data.error?.message ?? data.message` |

Interceptor khoan dung là mấu chốt: chạy với mock hiện tại **và** backend mới, không phải viết lại lúc cắt.

### T3.2 — File mới (nền chung)

| File | Nội dung |
|---|---|
| `lib/format.ts` | `formatVnDate(iso)`, `formatVnDateTime(iso)`, `parseVnDate(s)` — dùng ở tầng render (D4) |
| `lib/labels.ts` | Nhãn tiếng Việt cho 8 enum của D6 |
| `lib/messages.ts` | Map mã lỗi → nhãn tiếng Việt (D7) |

### T3.3 — Payload/form trong `page.tsx` (N3 — code sống, ~20 mục)

Làm **lúc cắt sang backend từng module**, không làm trước:

| Module | Việc |
|---|---|
| MF01 | Bỏ `status: "INACTIVE"` cứng khỏi payload tạo user; `STATUS_FILTER` `"PENDING"` → `"PENDING_VERIFICATION"`; `<option value={s.name}>` → `value={s.id}`, gửi `schoolId`; `useUpdate` → `useAction` cho đổi status |
| MF02 | Thêm `capacity` vào payload (hiện validate ở client nhưng **không gửi** → BR-025 không có dữ liệu để enforce); thêm `name` cho Classroom; `start`/`end` → `startDate`/`endDate` |
| MF03 | `emergencyInstructions` → `emergencyContact`; bỏ `healthStatus: "PENDING"` cứng; lọc bằng `parentId` thay tên |
| MF04 | `TYPE_FILTER` `"Supplier"` → `RAW_FOOD_SUPPLIER` (giá trị chết, không khớp bản ghi nào); ngưỡng chứng nhận 30/30 → **180/30** (BR-047); form đơn hợp tác gửi đủ payload |
| MF08 | **Nặng nhất:** bỏ `retentionDeadline` (đang là *câu văn*) + `hoursRemaining: 72` cứng khỏi payload, đọc từ response (BR-111 = 24h); `weight: "120 g"` → `amount`+`unit`; đưa `sessionId` → `inspectionId` vào payload; thêm `disposalWitness`, `tempOutOfRange`; `submitBatch()` gộp 1 lời gọi |
| MF09 | Gộp N `PUT /student-distributions/{id}` → 1 `POST /confirm` |
| MF10 | `studentId`/`student` thành **optional** + tách `FeedbackListItem`/`FeedbackDetail` (BR-145); bỏ `illnessKeyword` + `notify()` khỏi client; thêm `mealSessionId`/`scope`/`dishId`/`portionSize`; thêm `audienceCodes`; list dùng `revisionCount` |
| MF11 | `GET /incident-events?pageSize=200` + lọc client → `GET /incidents/{code}/events` |
| MF12 | Form gửi thiếu `requireMixedCase`; `audit-logs` filter `from`/`to` → `dateFrom`/`dateTo` |

### T3.4 — Sinh type từ OpenAPI (N4 — thay ~30 mục sửa tay)

Stack đã có springdoc. Khi backend chạy:

```
npx openapi-typescript http://localhost:8080/v3/api-docs -o src/lib/api/types.gen.ts
```

Mọi mục dạng "interface FE thiếu field / enum lệch giá trị" thành **lỗi biên dịch TypeScript chỉ đúng dòng**, thay vì dò tay 30 mục và tái phát ở lần lệch sau.

### T3.5 — Rà call-site xử lý lỗi

Review chỉ ra **2 call-site** đọc lỗi API trong toàn FE. Sau T3.1, rà toàn bộ `useCreate`/`useUpdate`/`useDelete`/`useAction`, gắn `onError` hiển thị `ApiError` qua `sonner`.

---

## KHÔNG làm (N5 — chết cùng mock)

| Mục | Vì sao bỏ |
|---|---|
| Envelope lỗi trong `mock-server.ts` (10 chỗ) | Interceptor khoan dung (T3.1) đã xử lý cả hai shape |
| `publishedAt` `""` → `null` (H8) | Backend làm đúng; mock sắp xóa |
| Guard chặn ghi `/audit-logs` (I13) | FE không gọi; review tự đánh "ưu tiên thấp" |
| Thêm resource `issue-slips`/`meal-history` vào `db.ts` | Phần thật (TDS §5.2 + `data_model.puml`) đã nằm ở T2a |
| Dọn comment `menu-variants` (I1) | Thuần cosmetic, không call-site thật |
| Đổi ISO + enum trong seed `mock-data.ts` (7.144 dòng) | **Chuyển vào bước sinh Flyway seed** — làm một lần ở nơi dữ liệu thật sự sống |
| `todayStamp()`/`nowStamp()` → ISO | Cùng lý do |

---

## Backlog FE tách sprint riêng (N6)

Các mục review đánh *"không coi mock là hợp đồng"* — cần task riêng kèm màn hình:

- Auth thật: interceptor Bearer + refresh flow + màn Google OAuth (UC-191/BR-218) — B5
- Form đăng ký Guest gọi API thật (UC-071/067) — E9
- Upload file thật + geolocation cho mẫu lưu (BR-107/BR-109) — F4
- 5 endpoint MF10 chưa có màn (UC-046 AF1/AF2, UC-163, UC-164, BR-139) — H10
- 4 màn MF12 chưa nối (UC-012, UC-057, UC-149, UC-150) — I11
- `/student-distributions/summary` bỏ dẫn xuất client — G5

---

## Thứ tự & phụ thuộc

```
Phase 0  00_Common_Conventions.md (D1–D7)        [chặn Phase 2b]
   │
   ├─ Phase 1  T1.1 error_message.md v1.5 → T1.2 traceability   [chặn T2b bước 4]
   │
   ├─ Phase 2  T2a 16 lỗi nội tại  ← làm trước, không phụ thuộc gì
   │           T2b áp D1–D7 vào 12 TDS
   │           T2c việc riêng từng module        (song song theo chủ module)
   │
   ├─ Phase 3  T3.1 + T3.2 hạ tầng FE  ← làm ngay, độc lập
   │           T3.3 payload  ← hoãn tới lúc cắt từng module
   │           T3.4 openapi-typescript  ← cần backend chạy
   │
   └─ Phase 4  xác minh
```

| Người (RACI §1) | Phần việc |
|---|---|
| **Nam** | Duyệt Phase 0; MF01, MF02; dựng `common/enums` + `@RestControllerAdvice` |
| **Mạnh** | MF04, MF08, MF10 |
| **Huy** | **Phase 1 toàn bộ** (quyết định của Lead Traceability); MF03, MF06 |
| **Minh** | **Phase 3 toàn bộ**; MF09, MF11 |
| **Thảo** | MF05, MF07, MF12; Phase 4; test cho I2 (khuôn `--`) và I6 (4 gate ATTP) |

---

## Phase 4 — Xác minh

| Bước | Cách |
|---|---|
| Type check | `cd 03_Design/08_FrontEnd/Web && npx tsc --noEmit -p tsconfig.check.json` |
| Lint | `npm run lint` |
| Audit nghiệp vụ | `npm run audit` (54 luật + seed guard) |
| Đối chiếu lại | Chạy lại quy trình `review.md` trên bản đã sửa; cập nhật bảng tổng kết (76 → 0) |

---

## Rủi ro

| Rủi ro | Vì sao | Giảm thiểu |
|---|---|---|
| **Demo lệch trong lúc mock đóng băng** | FE hiển thị `dd/MM/yyyy` + enum tiếng Việt, không khớp TDS | Nếu có buổi demo/nộp mà người chấm đối chiếu FE↔TDS: sửa seed **riêng màn đem demo**, không cả 7.144 dòng |
| **D1 đụng `partnerId` đa-tenant** (E3) | `partnerId` là khóa cách ly dữ liệu giữa NCC — sót một chỗ là rò dữ liệu chéo | D1 **giữ nguyên** `"SUP-001"` nên FE không đổi — đây chính là lý do chọn D1; thêm integration test RBAC |
| **STU-094 từ chối field lạ** (D3) | Gate làm **mọi** PATCH health-file của FE 400 cứng | Sửa D3 trước khi bật gate |
| **I6 — 4 gate ATTP chỉ ở client** | Gọi API trực tiếp bỏ qua BR-094/095/100/211 | P0 khi sinh backend |
| **`paginate()` parse ngày bằng regex `dd/MM/yyyy`** | Khi seed chuyển ISO, lọc `dateFrom`/`dateTo` **im lặng trả rỗng** | Sửa cùng lúc với bước sinh Flyway seed |

---

## Định nghĩa hoàn tất

- [x] `00_Common_Conventions.md` tồn tại, **Nam approve 30/07/2026**, 12 TDS §3 trỏ tới
- [x] `error_message.md` v1.5 — 0 mã "cục bộ TDS" còn ngoài file (MSG-104–138)
- [x] 12 TDS: không còn mâu thuẫn nội tại (C4, C5, E1, G1, G2); §5.2/§9.2/§10 khớp D1–D7
- [x] FE: `tsc --noEmit` sạch · `npm run audit` sạch (54 luật + 53 bảng seed) · ⚠️ `lint` **không chạy được** — `eslint` chưa cài trong `node_modules` (lỗi có sẵn, cần `npm i` bổ sung)
- [ ] 76 mục đóng hoặc chuyển backlog có chủ — **còn T3.3 (~20 mục payload/form) + T3.4 (openapi-typescript), cả hai chờ backend chạy**

### Trạng thái sau đợt 30/07/2026

| Phase | Trạng thái |
|---|---|
| Phase 0 — quy ước chung D1–D7 | ✅ Approved |
| Phase 1 — `error_message.md` v1.5 + traceability v1.3 | ✅ Xong |
| Phase 2a — mâu thuẫn nội tại TDS | ✅ Xong |
| Phase 2b — áp D1–D7 vào 12 TDS | ✅ Xong |
| Phase 3 — hạ tầng FE (T3.1, T3.2, T3.5 phần 2 call-site) | ✅ Xong |
| Phase 3 — T3.3 payload/form trong `page.tsx` | ⏸ Chờ cắt từng module sang backend |
| Phase 3 — T3.4 `openapi-typescript` | ⏸ Chờ backend chạy |
| Phase 4 — xác minh | ✅ tsc + audit sạch; lint chưa chạy được |

**Việc kế tiếp giờ đã mở khóa** (D1–D7 chốt là điều kiện tiên quyết):
1. **Nam** dựng `common/`: `enums/` (8 enum D6) · `@RestControllerAdvice` (envelope D2) · `PagedResponse<T>` · bảng `code_sequences` + bộ sinh mã `INSERT … RETURNING` (D1) · Flyway V1.
2. Bước sinh Flyway seed từ `mock-data.ts` phải chuyển `dd/MM/yyyy` → ISO và nhãn tiếng Việt → enum (D4/D6) — **và sửa `paginate()` cùng lúc**, nếu quên thì lọc `dateFrom`/`dateTo` im lặng trả rỗng.
3. Chủ module sinh backend theo TDS đã sửa; **4 gate ATTP của MF07 (BR-094/095/100/211) là P0**, không xếp cùng nhóm cosmetic.
