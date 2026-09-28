# 00 — Quy ước chung cho toàn bộ 13 module (D1–D10)

> **Ngày lập:** 2026-07-29 · **Cập nhật:** 2026-08-02 · **Trạng thái:**
> - **D1–D7:** ✅ **Approved** 2026-07-30 — Nguyễn Văn Nam (Final Approve theo RACI §1), xác nhận qua Bùi Quang Huy
> - **D9–D10:** ✅ **Approved** 2026-07-31 — Nam tự phát hiện khi build MF01
> - **D8:** 🟡 **Đang áp dụng trên thực tế, chờ Nam ghi nhận chính thức.** Đề xuất 2026-07-30 (Huy). Tính tới 2026-08-02, D8 **đã hiện diện trong code đã merge vào `dev`**: `common/config/OpenApiConfig.java` tồn tại và **11/11 controller** của MF01+MF02 mang đủ `@Tag` + `@Operation` + `@ApiResponse` trỏ `ErrorResponse`. Vì vậy **coi như ràng buộc** với mọi module mới; Nam chỉ cần ký xác nhận, không cần quyết lại. **Chưa ai ký ⇒ đừng ghi "Approved" ở đây.**
>
> D1–D7 dưới đây là **hợp đồng đã chốt**, không còn là đề xuất. Mọi TDS và mọi module backend phải tuân theo. Thay đổi về sau đi qua quy trình sửa ADR bình thường (ghi changelog + Nam approve lại), **không** sửa trực tiếp.
> **Nguồn:** `07_implement/review.md` mục A (7 mismatch xuyên module) + `review_fix_plan.md` Phase 0
> **Phạm vi ràng buộc:** tầng implement — DTO, JSON, enum Java, migration. **Không** thay thế `ADR-000`/`ADR-001` (kiến trúc hệ thống).

## Tại sao có file này

Review 29/07/2026 đối chiếu 12 TDS Backend với FE mock và tìm ra **76 mismatch**. Khoảng **24 mục** trong đó không phải lệch FE↔BE mà là **lệch giữa các TDS với nhau** — cùng một khái niệm được 12 người viết theo 3 quy ước khác nhau. Sửa từng TDS riêng lẻ sẽ tái phát ở module thứ 13.

File này chốt **một** quy ước cho mỗi khái niệm. Mọi TDS §3 phải có dòng:

> *"Áp dụng `07_implement/00_Common_Conventions.md` D1–D7; ADR module dưới đây chỉ ghi phần riêng."*

*(Khi Nam ký xác nhận **D8**, đổi đồng loạt `D1–D7` → `D1–D10` ở §3 của cả 12 TDS — 24 dòng, xem changelog cuối file.)*

**Thứ tự ưu tiên khi xung đột:** **code thật ở `04_Development/Backend/`** (§0-BIS) **>** `00_Common_Conventions.md` (file này) **>** ADR module trong TDS §3 **>** hợp đồng mock FE.

> ⚠️ Thứ tự trên **đổi từ 2026-08-02**. Trước đó chưa có dòng code nào nên file này đứng đầu. Nay MF01+MF02 đã chạy được: nếu tài liệu mô tả một đằng mà code làm một nẻo thì **code đúng**, tài liệu phải sửa — trừ khi code vi phạm chính D1–D10, lúc đó mở issue chứ đừng bắt chước.

---

## §0-BIS — Trạng thái code thật (đọc TRƯỚC KHI viết bất kỳ dòng nào)

> 🔴 **Mục này dành cho người — và cho AI — sắp code module MF04…MF13.** Bốn lỗi dưới đây MF03 đã mắc đủ cả bốn vì TDS của nó viết ngày 28/07, trước khi backend tồn tại. Tài liệu module của bạn cũng viết trong khoảng đó ⇒ **giả định là bạn cũng đang mắc**, kiểm trước khi code.

### Vị trí & khuôn dự án

| Hạng mục | Giá trị chốt |
|---|---|
| Project root | `04_Development/Backend/` (Maven, `pom.xml` ở đây) |
| Lệnh build | `mvn -f 04_Development/Backend/pom.xml verify` |
| Package module | **`com.sfms.mf<NN>`** — `mf02`, `mf03`, `mf04`… |
| Ngoại lệ duy nhất | **`com.sfms.iam`** = MF01. Đặt trước khi có quy ước, **giữ nguyên, KHÔNG lấy làm mẫu**. Đừng đặt `com.sfms.supplier`, `com.sfms.menu`, `com.sfms.student`… dù TDS của bạn đang ghi vậy. |
| Nền dùng chung | `com.sfms.common` |

### Flyway — mô hình "V1 = toàn bộ schema, Nam giữ" ĐÃ BỊ BỎ

Câu đó còn nằm trong **13 TDS + MVP plan**; nó **sai** kể từ khi MF02 chọn file riêng. Thực tế trong `src/main/resources/db/migration/`:

| File | Nội dung |
|---|---|
| `V1__init_schema.sql` | MF01 — 12 bảng (`code_sequences`, `schools` stub, `partners` stub, `roles`, `permissions`, `user_accounts`, …) |
| `V2__seed_roles_permissions_policy.sql` | seed role/permission/policy |
| `V3__mf02_school_class_academic_year.sql` | MF02 |
| `V4`, `V5` | **đã cấp cho MF03** (`V4__mf03_student_parent.sql`, `V5__seed_mf03.sql`) |

**Luật:** module của bạn lấy **số V kế tiếp còn trống**, đặt tên `V<n>__mf<NN>_<mo_ta>.sql`. **Tuyệt đối không sửa file V đã merge** — Flyway lưu checksum, sửa file cũ làm `migrate` fail trên máy mọi người khác. Bảng của module khác đã tồn tại? `ALTER TABLE`, đừng `CREATE TABLE` lại (xem đầu `V3` — Mạnh đã phải xử đúng tình huống này với `schools`).

**Trước khi đặt số V, chạy:** `ls 04_Development/Backend/src/main/resources/db/migration/`

### Bốn lỗi MF03 đã mắc — kiểm module của bạn ngay

| # | Lỗi | Hậu quả | Cách phát hiện |
|---|---|---|---|
| 1 | Entity/DDL tự khai `created_at`/`created_by`/`updated_at`/`updated_by` | **Hibernate duplicate column → build đỏ ngay lần chạy đầu** | `grep -n "created_by\|updated_by\|createdAt\|createdBy" <TDS của bạn>` |
| 2 | Cột mã nghiệp vụ đặt tên riêng (`student_code`, `supplier_code`, `menu_code`…) và `UNIQUE` toàn cục | Trường thứ hai onboard là vỡ; lệch D1 | `grep -n "_code" <TDS>` |
| 3 | Khóa ngoại **trong JSON** là UUID (`"classroomId": "8f1c…"`) | Vi phạm D1; FE type sinh sai qua `openapi-typescript` | `grep -n '": "[0-9a-f]\{4\}…' <TDS>` |
| 4 | Khai lại enum của D6 trong package module | Hai `AllergySeverity` cùng tồn tại → sai cảnh báo dị ứng nặng | `grep -n "public enum" <TDS>` |

**Lỗi 1 chi tiết:** `common/domain/BaseEntity.java` giữ `id` (UUID PK), `created_at`, `updated_at`, `created_by`, `updated_by` qua `AuditingEntityListener`. Entity chỉ cần `extends BaseEntity`. **DDL thì vẫn phải tạo đủ 4 cột** — đây là chỗ dễ nhầm nhất. Mẫu đúng, chép nguyên:

```sql
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),   -- D9 BaseEntity
    updated_at  TIMESTAMPTZ,                          -- D9 (KHÔNG NOT NULL/DEFAULT)
    created_by  VARCHAR(180),                         -- D9
    updated_by  VARCHAR(180),                         -- D9
```

Cột nghiệp vụ kiểu "ai duyệt/ai xác minh" **được giữ** vì mang ngữ nghĩa, đặt tên `<hành_động>_by_<loại>_id` (`verified_by_user_id`, `reviewed_by_user_id`). Cột **audit thuần** như `updated_by_user_id` thì bỏ — BaseEntity đã làm.

### Nền chung ĐÃ CÓ — dùng, đừng viết lại

Trước khi tự viết bất cứ thứ gì dưới đây, mở file ra đọc: `04_Development/Backend/src/main/java/com/sfms/common/`

| Cần gì | Dùng cái này | Neo |
|---|---|---|
| PK + audit trail | `domain/BaseEntity.java` | D9 |
| Envelope lỗi | `error/GlobalExceptionHandler.java` + `dto/ErrorResponse.java` + `error/ApiException.java` | D2 |
| Trả danh sách | `dto/PagedResponse.java` | §2b-1 |
| Phân trang/lọc/sort | `query/ListQuery.java` + `query/SpecSupport.java` | §2b |
| Sinh mã nghiệp vụ | `code/CodeGenerator.java` + bảng `code_sequences` (`INSERT … ON CONFLICT`, an toàn khi song song) | D1/D5 |
| Enum dùng chung (8 cái) | `enums/` — `AllergySeverity`, `MealType`, `RoleKey`, `PartnerType`, `ConsumptionStatus`, `BiometricStatus`, `NotificationType`, `HealthHistoryEventType` | D6 |
| Tra mã ↔ UUID của module khác | `reference/ReferenceResolver.java` (native query, **không** import entity module khác) | D1, ADR-M02-5 |
| Upload file/ảnh | `storage/StorageService.java` (Local + R2) | D10 |
| Swagger | `config/OpenApiConfig.java` | D8 |
| Danh tính người gọi | `iam/security/AuthUser` + `@CurrentUser` | §16 |

**Cần thêm method vào `common/`?** Nam **sở hữu** `common/` theo §4b, nhưng lý do ghi trong kế hoạch là *"sinh 1 lần… để không mỗi người một bản"* — tức chống **trùng lặp và xung đột**, không phải chặn người khác gõ. Nên:

- **Bổ sung thuần** (thêm method tra cứu vào `ReferenceResolver`, thêm hằng số) → **tự làm**, PR nhỏ, **Mạnh review** (Lead Backend reviewer, §2b-7). Đừng chờ Nam — chờ chỉ làm chậm, không làm đúng thêm.
- **Đổi hành vi cái đã có** (sửa `BaseEntity`, đổi shape `ErrorResponse`/`PagedResponse`, thêm enum vào D6) → **hỏi Nam trước**, vì ảnh hưởng 13 module.
- Dù trường hợp nào: **báo nhóm trước khi đụng**, vì file dùng chung là chỗ dễ merge conflict nhất — đó mới là rủi ro thật.

### Module mẫu để bắt chước

**`com.sfms.mf02`** — vertical slice đầy đủ và mới nhất. Mở 3 file này trước khi viết:

- `mf02/web/AcademicYearController.java` — `@Tag`/`@Operation`/`@ApiResponse`, `@PreAuthorize`, `@CurrentUser`
- `mf02/dto/ClassroomDtos.java` — record DTO, `@Schema` với `example` là **mã nghiệp vụ**, FK là `code` (`academicYearId: "AY-2025"`)
- `mf02/domain/Classroom.java` — `extends BaseEntity`, cột `code`, javadoc ghi rõ ràng buộc nào ở đâu

### Cross-module: đọc dữ liệu module khác thế nào

**Cấm import entity/repository của module khác** (ADR-M02-5). Hai đường hợp lệ:

1. **Tra cứu thuần** (mã ↔ UUID, tên hiển thị) → `ReferenceResolver`, native query.
2. **Hành vi** (tạo tài khoản, gửi mail, phát sự kiện) → **Port interface** do module sở hữu dữ liệu expose. Tính tới 02/08 **chưa có Port nào tồn tại** — module đầu tiên cần thì tự đề xuất với chủ module đó.

Field dẫn xuất thuộc miền module khác thì **trả 0 kèm comment**, không đoán. Xem `mf02/service/ClassroomService.java:214` (`students` chờ MF03) — và khi module bạn xong, **quay lại nối các chỗ đó**, đưa vào DoD.

---

## D1 — Định danh: `id` công khai là **mã nghiệp vụ**, UUID là PK nội bộ

**Vấn đề.** Ba quy ước đang tồn tại song song:

| Nhóm TDS | Quy ước hiện tại |
|---|---|
| MF01, MF02, MF03, MF08, MF09, MF10 | `id UUID PRIMARY KEY`, **không** có cột `code` — nhưng ví dụ JSON §9.2 lại trả `"id": "SCH-HN-0063"`, `"id": "FB-8809"` (tự mâu thuẫn) |
| MF04 | PK UUID **+** cột `code`; path param dùng **UUID**; `code` chỉ để hiển thị |
| MF05, MF06, MF07, MF12 | PK là `VARCHAR(32/40/64)` = chính mã nghiệp vụ |
| **MF11 (ADR-M11-5)** | PK UUID + `code UNIQUE`; **path param và JSON `id` đều dùng `code`** |

FE dùng mã nghiệp vụ làm `id` ở **mọi nơi**: URL (`/school-admin/students/HS-3A-001`), khóa ngoại (`partnerId: "SUP-001"`, `academicYearId: "AY-2025"`), và cột "Mã" trên bảng.

### Quyết định

**Theo MF11 ADR-M11-5** — quy ước duy nhất khớp FE 100%.

```java
@Entity
@Table(name = "incidents", uniqueConstraints = @UniqueConstraint(columnNames = {"school_id", "code"}))
public class Incident {
    @Id @GeneratedValue
    private UUID id;                    // PK nội bộ — KHÔNG lộ ra API

    @Column(nullable = false, length = 32)
    private String code;                // INC-2026-014 — mã nghiệp vụ

    @JsonProperty("id")                 // ⬅ JSON trả `code` dưới tên `id`
    public String getPublicId() { return code; }
}
```

- **Path param** dùng `code`: `GET /api/v1/incidents/INC-2026-014`
- **Khóa ngoại trong JSON** dùng `code` của bảng đích: `"partnerId": "SUP-001"`, không phải UUID
- **Khóa ngoại trong DB** vẫn là `UUID REFERENCES ...` — không đổi tính toàn vẹn tham chiếu

**Khuôn mã:** `<PREFIX>-<năm>-<số thứ tự trong năm, 3 chữ số>` (ADR-M11-5), hoặc khuôn sẵn có của module nếu FE đã dùng (`HS-3A-001`, `AY-2025`, `SUP-001` — **giữ nguyên**, không ép về khuôn mới).

**Sinh mã có khóa:** dùng bảng `code_sequences` với `INSERT ... RETURNING` thay vì đọc-rồi-ghi — tránh trùng mã khi 2 request song song (ADR-M11-5).

### Lý do chọn phương án này

1. **Khớp FE 100%** → đổi `baseURL` là chạy, đúng tiêu chí nghiệm thu §7 Team Plan.
2. **Mã được in ra giấy.** `RPT-2026-033`, `INC-2026-014` xuất hiện trên biên bản ký tay (BR-211) và báo cáo gửi cơ quan chức năng (BR-154). UUID không dùng được ở vai trò đó.
3. **Rủi ro đa-tenant.** `partnerId` là khóa cách ly dữ liệu giữa các nhà cung cấp (`CURRENT_SUPPLIER_ID = "SUP-001"`). Đổi sang UUID mà sót một chỗ là **rò dữ liệu chéo giữa các NCC**. Giữ nguyên `"SUP-001"` là phương án ít rủi ro nhất.
4. **Khuôn `{menuId}--{dietGroupId}` của MF05 không vỡ.** MF05 CF-11/C10 coi khuôn 2 gạch ngang là hợp đồng cứng; nó chỉ còn hợp lệ nếu `id` vẫn là mã nghiệp vụ.

### Việc phải làm ở từng TDS

| TDS | Sửa gì |
|---|---|
| MF01, MF02, MF03, MF08, MF09, MF10 | §5.2: thêm `code VARCHAR(32) NOT NULL` + `UNIQUE(school_id, code)`; thêm `@JsonProperty("id")` |
| MF04 | Sửa ADR-M04-1: path param dùng `code` thay UUID; bỏ yêu cầu §9.3.5 *"đổi `{p.id}` → `{p.code}`"* (không còn cần) |
| MF05, MF06, MF07, MF12 | §5.2: đổi PK `VARCHAR` → `UUID` + tách cột `code`; giá trị `code` giữ nguyên như PK cũ |
| MF11 | **Không sửa phần định danh** — đã đúng |

---

## D2 — Envelope lỗi

**Vấn đề.** 12/12 TDS dùng `{ "error": { "code", "message", "details" } }`. Mock trả `{ "message": "…" }` phẳng. Chỉ **2 call-site** trong toàn FE đọc lỗi API, cả hai đọc `e.response.data.message` → sẽ ra `undefined` với envelope TDS.

### Quyết định

**Backend:** `@RestControllerAdvice` ở `common/`, MF02 là bản mẫu.

```json
{
  "error": {
    "code": "SCH-010",
    "message": "Classroom capacity cannot exceed 50 students.",
    "details": [{ "field": "capacity", "value": 55 }]
  }
}
```

- `code` — mã module (`SCH-010`) hoặc mã MSG (`MSG-010`); xem D7.
- `message` — **tiếng Anh**, nguyên văn `error_message.md`.
- `details` — mảng, có thể rỗng; không bao giờ `null`.

**FE:** interceptor **khoan dung cả hai shape**:

```typescript
const message = data?.error?.message ?? data?.message ?? "Đã có lỗi xảy ra";
```

Nhờ vậy interceptor chạy được với mock hiện tại **lẫn** backend mới — không phải viết lại lúc cắt sang backend thật. Đây là lý do `mock-server.ts` **không** cần sửa envelope (xem `review_fix_plan.md` mục "KHÔNG làm").

---

## D3 — Tiền tố đường dẫn

Mọi TDS ghi *"Path thật thêm tiền tố `/api/v1`"*; FE `client.ts` đặt `baseURL: "/api"`.

**Quyết định:** `baseURL: "/api/v1"`. Một dòng ở `client.ts`. Mock adapter khớp regex `/.*/`  nên không bị ảnh hưởng.

---

## D4 — Ngày/giờ: JSON luôn ISO-8601

**Vấn đề.** 4 TDS nói ISO, 3 TDS nói `dd/MM/yyyy`, 1 TDS (MF04) nói **cả hai** trong cùng một mục §9.2. FE lưu và render trực tiếp chuỗi `dd/MM/yyyy`, payload gửi lên dùng `toLocaleString("vi-VN")`.

### Quyết định

| Kiểu | JSON | Java |
|---|---|---|
| Ngày | `"2026-07-29"` | `LocalDate` |
| Ngày giờ | `"2026-07-29T02:15:00Z"` | `Instant` |

FE thêm `lib/format.ts` (`formatVnDate`, `formatVnDateTime`) và gọi ở **tầng render** — không lưu chuỗi đã format vào state hay payload.

### ⚠️ Ghi đè ADR-M11-5

ADR-M11-5 (MF11) chốt *"Ngày/giờ **giữ định dạng `dd/MM/yyyy [HH:mm]`** ở JSON qua `@JsonFormat`"*. **D4 ghi đè phần này.** MF08, MF09, MF11 phải **bỏ `@JsonFormat`** khỏi các field ngày. Phần định danh của ADR-M11-5 vẫn giữ nguyên (là nguồn của D1).

Lý do ghi đè: `01_MVP_Backend_Implementation_Plan.md` §3 đã quy định *"ngày = ISO/Instant"* làm chuẩn dữ liệu chung; và định dạng hiển thị là việc của tầng trình bày, không phải của hợp đồng API.

### ⚠️ Không sửa seed `mock-data.ts` ở đợt này

7.144 dòng seed đang dùng `dd/MM/yyyy`. Theo Team Plan §2a, file này là **"nguồn seed Flyway"** — dữ liệu sống tiếp dưới dạng SQL, file TypeScript thì không. Việc chuyển đổi định dạng làm **một lần** ở bước sinh Flyway seed, không làm hai lần.

**Kèm theo:** `paginate()` trong `mock-server.ts` parse ngày bằng regex `^(\d{2})\/(\d{2})\/(\d{4})`. Khi seed chuyển ISO mà quên sửa chỗ này, lọc `dateFrom`/`dateTo` sẽ **im lặng trả rỗng** — không lỗi, không cảnh báo. Ghi vào checklist bước sinh seed.

---

## D5 — ID do client sinh

**Vấn đề.** `01_MVP_Backend_Implementation_Plan.md` §3 quy định `nextId()`/sequence tập trung ở server. Nhưng FE sinh ID ở client cho **ít nhất 6 resource**: `clientId("U")`, `clientId("AY")`, `id: name.trim()` (classrooms), `FB-${Date.now()}`, `SMP-${Date.now()}`, `MENU-…`/`TPL-…`/`SMR-…`. Chỉ MF05 CF-11 nêu vấn đề này; 11 TDS còn lại im lặng.

### Quyết định

Theo **MF05 CF-11**, áp cho **cả 13 module**:

1. Nếu request body có `id` → dùng làm `code`, kiểm trùng → trùng thì **409** với mã `<MODULE>-409`.
2. Nếu vắng → server tự sinh theo `code_sequences` (D1).
3. Backend **luôn** trả `id` trong response 201 — FE đọc từ response, không giả định giá trị mình gửi được giữ nguyên.

Nếu không chốt điều này, 5 resource còn lại sẽ vỡ khi backend tự cấp ID khác với ID client đã dùng để điều hướng ngay sau `onSuccess`.

---

## D6 — 8 enum dùng chung (`common/enums/`)

**Vấn đề.** 6 khái niệm hiện có **13+ bộ giá trị** rải trên 12 TDS + FE. Rà kỹ thì có **8** khái niệm, không phải 6 như review nêu.

**Quy ước chung:** tất cả **SCREAMING_SNAKE** trong JSON và DB. FE giữ bảng nhãn tiếng Việt ở `lib/labels.ts` — nhãn hiển thị **không bao giờ** là giá trị lưu trữ.

| Enum | Giá trị chốt | Nhãn FE | Đang lệch ở đâu |
|---|---|---|---|
| `MealType` | `BREAKFAST`<br>`LUNCH`<br>`SNACK` | Bữa sáng<br>Bữa trưa<br>**Bữa phụ** | MF05 đúng. MF07 `InspectionSession.meal` = `"Bữa sáng"/"Bữa trưa"/"Bữa chiều"` (CF-8). MF10 FE = `"Bữa sáng"/"Bữa trưa"/"Bữa phụ"`. ⚠️ Nhãn cũng lệch: **"Bữa phụ" ≠ "Bữa chiều"** — chốt "Bữa phụ". |
| `AllergySeverity` | `MILD`<br>`MODERATE`<br>`SEVERE` | Nhẹ<br>Trung bình<br>Nặng | MF03 §9.3 `"NANG"`; MF03 §9.2 + MF09 §9.2 `"Severe"`; FE `AllergyDeclaration.severity` = `"Nhẹ"/"Trung bình"/"Nặng"` **và** `Student.allergySeverity` = `"Mild"/"Moderate"/"Severe"`. **Ba từ vựng cho một khái niệm.** Đây là dữ liệu điều khiển cảnh báo đỏ BR-123 — sai enum = **mất cảnh báo dị ứng nặng**. |
| `PartnerType` | `RAW_FOOD_SUPPLIER`<br>`SERVICE_CATERER` | Nhà cung cấp nguyên liệu<br>Đơn vị nấu ăn | MF04 §5.1 đúng; §9.2 dùng `?type=Raw%20Food%20Supplier`. ⚠️ Phân biệt: **vai trò** = `"Service Caterers"` (số nhiều, BR-007), **loại đối tác** = `SERVICE_CATERER` (số ít). Hai chuỗi gần giống, khác nghĩa. |
| `RoleKey` | `system-admin`<br>`school-admin`<br>`teacher`<br>`parent`<br>`medical-staff`<br>`service-caterers`<br>`supplier` | theo BR-007 | TDS/MF10 dùng nhãn BR-007 (`"Medical Staff"`, `"School Administration"`); FE dùng kebab-case — **đồng thời là URL segment**, nên chốt kebab-case. ⚠️ FE không có key cho `"Raw Food Supplier"` — chốt `supplier` = NCC nguyên liệu. |
| `NotificationType` | `MENU_PUBLISHED`<br>`INSPECTION_ALERT`<br>`CAPA_ASSIGNED`<br>`EXPIRY_ALERT`<br>`INCIDENT_ALERT`<br>`FOOD_RECALL`<br>`ALLERGY_ALERT` | (7 nhãn) | MF10 §9.2 `GET /notifications` dùng 4 giá trị `ALLERGY/FOOD_SAFETY/NEWS/SYSTEM`; `notification-preferences/me` dùng 7 giá trị trên. **Hai taxonomy trong cùng module.** Người dùng tắt `EXPIRY_ALERT` nhưng thông báo mang `type: "SYSTEM"` → dispatcher không biết lọc gì. Chốt 7. |
| `HealthHistoryEventType` | `KHAI_BAO`<br>`XAC_MINH`<br>`GHI_CHU_LAM_SANG`<br>`SU_CO`<br>`CAP_NHAT_THUOC` | Khai báo<br>Xác minh<br>Ghi chú lâm sàng<br>Sự cố<br>Cập nhật thuốc | MF03 §9.3 dùng SNAKE_CASE nhưng **thiếu `KHAI_BAO` và `SU_CO`**; FE dùng tiếng Việt có dấu làm nhãn hiển thị trực tiếp → lọc/nhóm theo `type` không khớp. |
| `ConsumptionStatus` | `PENDING`<br>`FULLY`<br>`PARTIALLY`<br>`NOT_CONSUMED` | Chưa ghi nhận<br>Ăn hết<br>Ăn một phần<br>Không ăn | MF09 §5.2 khai đúng 4 giá trị + CHECK constraint; nhưng §9.2 message SRV-001 ghi *"must be one of FULLY, PARTIALLY, NOT_CONSUMED"* — **bỏ sót `PENDING`**, đúng là trạng thái khởi tạo. |
| `BiometricStatus` | `NOT_REGISTERED`<br>`PENDING_REGISTRATION`<br>`REGISTERED` | Chưa đăng ký<br>Đang chờ đăng ký<br>Đã đăng ký | MF04 §9.2 chỉ khai 2 giá trị; seed `DP-03` **đang ở** `PENDING_REGISTRATION` → không có chỗ chứa. Chốt 3, kèm định nghĩa chuyển trạng thái: `POST /delivery-persons/{id}/face` → `PENDING_REGISTRATION`; xác thực sinh trắc thành công → `REGISTERED`. |

**Nam sở hữu `common/enums/`** (Team Plan §4b: *"Enum/error-code dùng chung để ở module `common`"*). Module khác **không** tự khai lại.

---

## D7 — Ngôn ngữ thông điệp lỗi

**Vấn đề.** `error_message.md` là **tiếng Anh** cho mọi MSG. TDS §10 giữ nguyên văn tiếng Anh, nhưng các mã "dẫn xuất từ BR" lại viết **tiếng Việt** (SMP-105/107/111/112, SRV-094, SRV-022, STU-…). FE hiển thị hỗn hợp: có chỗ dán nguyên MSG tiếng Anh (`"Invalid credentials. Please check your email and password. Còn N lần thử."`), có chỗ tiếng Việt tự viết.

### Quyết định

- **Backend** trả `code` + `message` **tiếng Anh**, nguyên văn `error_message.md`. Không dùng `Accept-Language` ở MVP.
- **FE** tra `lib/messages.ts` map `code` → nhãn tiếng Việt để hiển thị; dùng `message` từ server làm fallback khi chưa có bản dịch.
- Mã dẫn xuất BR đang viết tiếng Việt trong TDS §10 phải **dịch sang tiếng Anh** khi được cấp MSG chính thức (xem `error_message.md` v1.5).

Lý do: `error_message.md` là nguồn chuẩn duy nhất và đang là tiếng Anh; ép backend đa ngữ ở MVP làm phình `@RestControllerAdvice` mà không có yêu cầu nghiệp vụ nào đòi.

---

## D8 — Tài liệu hoá OpenAPI (Swagger)

> ⏳ **Chờ Nam approve** (đề xuất 2026-07-30). Khác D1–D7: D8 **không** gỡ mục mismatch nào của `review.md` — nó là **cổng máy kiểm** để D1/D2/D4/D6 không tái phát qua đường spec sinh cho FE.

**Vấn đề.** Swagger xuất hiện **47 lần** trong 12 TDS, nhưng chỉ ở hai dạng: dòng *"Stack (ADR-000)"* và câu *"mở `swagger-ui` đối chiếu §9"*. Grep `@Operation|@Schema|@ApiResponse|@Tag(|GroupedOpenApi` trên toàn `07_implement/` → **0 kết quả**. Tức là chưa có chỗ nào quy định **phải annotate cái gì**. 4 module (MF02, MF03, MF04, MF08) thậm chí không có cả bước đối chiếu Swagger — Swagger chỉ được nhắc ở dòng stack và dòng footer.

Trong khi đó Swagger đang bị hai chỗ khác **dựa vào**:

- `01_MVP_Backend_Implementation_Plan.md` §Nghiệm thu lấy nó làm **cổng nghiệm thu**: *"52 resource CRUD qua Swagger khớp hợp đồng mock"*.
- `review_fix_plan.md` T3.4 lấy `/v3/api-docs` làm **nguồn sinh type FE** (`npx openapi-typescript`), thay ~30 mục sửa tay của nhóm N4.

Cả cổng nghiệm thu lẫn công cụ sinh type đều dựa vào một thứ chưa ai đặc tả. Nếu spec sinh ra để `example` của `id` là UUID, hoặc khai enum bằng `allowableValues` viết tay, hoặc 4xx không trỏ schema lỗi chung, thì type FE sinh ra sẽ **sai theo** — và lệch D1/D2/D4/D6 quay lại bằng đường khác, lần này khó thấy hơn vì nó nằm trong file sinh tự động.

### Quyết định

**Phạm vi ràng buộc: cả tài liệu (TDS §9) và code.** Mạnh (Lead Backend reviewer, RACI §1) được quyền reject PR controller thiếu annotation theo bảng dưới.

| # | Luật | Neo vào |
|---|---|---|
| **S1** | Mỗi `@RestController` có `@Tag(name = "MFxx — <Tên module>")`, dùng đúng tên module ở Team Plan §2c-0 → `swagger-ui` gom đúng 13 nhóm | — |
| **S2** | Mỗi endpoint có `@Operation(summary, description)`; **`description` PHẢI chứa UC ID + BR ID** mà endpoint hiện thực | §2 Traceability — truy vết còn sống trong runtime doc, không chỉ trong file .md |
| **S3** | Mỗi mã lỗi ở §10 của TDS có một `@ApiResponse` tương ứng, `content` trỏ **`ErrorResponse` dùng chung** | **D2** |
| **S4** | Field ngày khai `@Schema(type = "string", format = "date"` hoặc `"date-time")` + `example` viết dạng ISO | **D4** |
| **S5** | Enum khai bằng **kiểu enum thật** ở `common/enums/` — **KHÔNG** `@Schema(allowableValues = "…")` viết tay | **D6** |
| **S6** | `example` của `id` là **mã nghiệp vụ**; không có UUID trong bất kỳ `example`/`@Schema` công khai nào | **D1** |
| **S7** | Mọi `GET` danh sách khai response là `PagedResponse<T>` — schema generic, Nam khai một lần | Team Plan §2b-1 |
| **S8** | Một `@SecurityScheme` bearer JWT khai toàn cục; endpoint public ghi `@SecurityRequirements` **rỗng** để ghi đè | §16 Auth Matrix |

**Nam khai một lần ở `common/`, module khác KHÔNG khai lại:**

- `common/config/OpenApiConfig.java` — `@OpenAPIDefinition` + `@SecurityScheme(name = "bearerAuth", type = HTTP, scheme = "bearer", bearerFormat = "JWT")`
- `common/dto/ErrorResponse.java` — envelope D2, có `@Schema` mô tả 3 field `code`/`message`/`details`
- `common/dto/PagedResponse.java` — 5 field của §2b-1

**Ví dụ chuẩn** (MF08, minh hoạ S1–S6):

```java
@Tag(name = "MF08 — Food Sample Retention")
@RestController
@RequestMapping("/api/v1/food-samples")                      // D3
public class FoodSampleController {

    @Operation(
        summary     = "Lưu mẫu thức ăn theo suất ăn",
        description = "UC-108, UC-109 · BR-105, BR-203"       // S2 — BẮT BUỘC có UC + BR
    )
    @ApiResponse(responseCode = "201", description = "Đã lưu mẫu")
    @ApiResponse(responseCode = "409", description = "SMP-104 — mã mẫu đã tồn tại (D5)",
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class)))  // S3
    @PostMapping
    public FoodSampleResponse create(@Valid @RequestBody CreateFoodSampleRequest req) { … }
}

public record FoodSampleResponse(
    @Schema(example = "SMP-20260706-01")            String  id,          // S6 — mã nghiệp vụ, KHÔNG UUID
    @Schema(type = "string", format = "date-time",
            example = "2026-07-06T10:30:00+07:00")  OffsetDateTime retainedAt,   // S4
    ShelfStatus shelfStatus                                               // S5 — enum thật, springdoc tự sinh schema
) {}
```

**KHÔNG làm:** không duy trì một file `openapi.yaml` viết tay song song với annotation. Spec sinh từ code là **bản duy nhất** — có hai bản thì lại quay về đúng bài toán 76 mismatch.

**Lưu ý hạ tầng:** `/v3/api-docs` và `/swagger-ui/index.html` phải `permitAll` trong Spring Security, nếu không T3.4 (`openapi-typescript`) không tải được spec.

### Việc phải làm ở từng TDS

- **§9.2** — mỗi endpoint ghi thêm `@Operation.summary` + danh sách mã HTTP của `@ApiResponse`.
- **§10** — mỗi dòng mã lỗi có cột HTTP status, khớp 1-1 với `@ApiResponse` ở §9.2.
- **§11 Deployment** — thêm 1 bước: tải `/v3/api-docs` lưu kèm tài liệu nộp báo cáo (bằng chứng nghiệm thu).
- **§13 / TestSpec** — ≥1 **contract test đọc `/v3/api-docs`** và assert: (a) không field `id` nào có `format: uuid`; (b) mọi response 4xx/5xx trỏ `ErrorResponse`; (c) mọi enum khớp bảng D6. Đây là chỗ D8 sinh giá trị thật: 3 assertion này bắt lệch D1/D2/D6 tự động, thay cho checklist tay.

---

## D9 — `created_by`/`updated_by` là tên cột dành riêng cho `BaseEntity`

**Vấn đề.** Backend MF01 (Nam, đã merge `namnvhe180863`) dựng xong `common/domain/BaseEntity` với audit trail chung: mọi entity `extends BaseEntity` tự có

```java
@CreatedBy  @Column(name = "created_by", updatable = false, length = 180) private String createdBy;
@LastModifiedBy @Column(name = "updated_by", length = 180) private String updatedBy;
```

giá trị được Spring Data JPA (`AuditorAwareConfig`, bean `auditorAware`) **tự động điền = email actor** (hoặc `"system"` nếu không có request context) — module con **không tự set** hai field này trong service.

Nhưng rà `07_implement/*/TDS-*.md` (viết trước khi có BaseEntity) thấy 4 module đã tự khai `created_by`/`updated_by` với **tên trùng nhưng kiểu/ý nghĩa khác**, hoặc tên gần giống dễ nhầm:

| TDS | Khai hiện tại | Xung đột |
|---|---|---|
| MF05 §5.2 | `created_by VARCHAR(128) NOT NULL` — dùng để check quyền "Caterer chỉ sửa menu của mình" | **Trùng tên** với cột reserved của BaseEntity nhưng khác ràng buộc (128 vs 180, NOT NULL vs nullable) và khác mục đích (business rule, không phải audit im lặng) |
| MF06 §5.2 | `created_by UUID NOT NULL REFERENCES user_accounts(id)` | **Trùng tên, khác kiểu hẳn** (UUID FK bắt buộc vs String email tuỳ chọn) — nếu entity `extends BaseEntity` sẽ vỡ mapping (2 định nghĩa cho cùng 1 cột) |
| MF12 §5.2 | `updated_by VARCHAR(32) REFERENCES users(id)` | **Trùng tên**, độ dài khác (32 vs 180), là FK chứ không phải audit string |
| MF03 §5.2 | Có cả `created_by VARCHAR(180)` (đúng khuôn BaseEntity — nên **xoá, kế thừa thay vì tự khai**) **và** `updated_by_user_id UUID` (field khác, dễ nhầm là bản thay thế của `updated_by`) | Không trùng tên nhưng gây nhầm: 2 field cùng trả lời "ai sửa" |

Nếu không chốt, module nào có entity vừa cần "ai sửa gần nhất" (business — vd hiển thị "Caterer X đã tạo") vừa `extends BaseEntity` sẽ đụng tên cột `created_by`/`updated_by` với Hibernate, hoặc tệ hơn: bịa thêm cột riêng, chạy được nhưng lưu sai người (không set qua `AuditorAware` nên `null`/sai actor).

### Quyết định

1. **`created_by` và `updated_by` là tên cột reserved** — nghĩa cố định: `VARCHAR(180)`, email actor, tự điền qua `AuditorAware`, **không** module nào được khai lại hai tên này với kiểu/ràng buộc khác.
2. Entity nào cần lưu **field nghiệp vụ** "ai làm việc này" (không phải audit trail chung — vd chủ sở hữu bản ghi dùng để phân quyền, hoặc actor bắt buộc NOT NULL) thì đặt **tên khác**, theo khuôn đã đúng của MF05/MF11/MF13: `<hành_động>_by_<loại>_id` — vd `created_by_supplier_id` (MF05, đã đúng), `created_by_id` (MF11, đã đúng), `created_by_user_id` (MF13, đã đúng).
3. Entity `extends BaseEntity` **không** tự khai lại `createdBy`/`updatedBy` trong Java (kể cả getter/setter riêng) — kế thừa thẳng từ `common/domain/BaseEntity`, giống cách MF01 đã sửa cho `UserAccount`/`SecurityPolicy`.

### Việc phải làm ở từng TDS

| TDS | Sửa gì |
|---|---|
| MF03 | §5.2: xoá field `created_by VARCHAR(180)` tự khai trên `Student` (kế thừa BaseEntity thay vì tự khai); giữ `updated_by_user_id UUID` **nhưng đổi tên** thành gợi ý nghiệp vụ rõ hơn nếu nó khác `updated_by` (vd nếu là "nhân viên y tế xác minh gần nhất" thì đặt `verified_by_staff_id`) |
| MF05 | §5.2: đổi tên cột dùng cho check quyền từ `created_by` → `created_by_user_email` hoặc tương đương (giữ nguyên VARCHAR(128) NOT NULL cho business rule); nếu chỉ cần audit thông thường thì bỏ, dùng `created_by` kế thừa từ BaseEntity |
| MF06 | §5.2: đổi `created_by UUID NOT NULL REFERENCES user_accounts(id)` → `performed_by_user_id` (giữ UUID NOT NULL FK, đây là business field — ai thực hiện giao dịch kho, không phải audit im lặng) |
| MF12 | §5.2: đổi `updated_by VARCHAR(32) REFERENCES users(id)` → tên khác (vd `reviewed_by_user_id`) nếu là FK nghiệp vụ; nếu chỉ là audit thường thì bỏ, kế thừa BaseEntity |
| MF01, MF02, MF11, MF13 | Không cần sửa — đã đúng khuôn (MF01/MF02 khớp BaseEntity; MF11/MF13 dùng tên riêng không trùng) |

---

## D10 — Upload file/ảnh: qua `common/storage/StorageService`, không tự viết I/O

**Bối cảnh.** MF01 (avatar upload, UC-097) là module đầu tiên cần lưu file, nên Nam dựng sẵn abstraction dùng chung theo đúng ADR-000 (dev: `./data/uploads`) / ADR-001 mục 6 (prod: Cloudflare R2):

```java
public interface StorageService {
    String store(String key, MultipartFile file);   // trả public URL/path
}
```

- `LocalDiskStorageService` — mặc định (`sfms.storage.provider=local` hoặc bỏ trống), ghi vào `./data/uploads`, phục vụ qua `/media/**`.
- `R2StorageService` — bật bằng `sfms.storage.provider=r2`, dùng AWS SDK v2 (S3-compatible) trỏ endpoint Cloudflare R2, đọc credentials từ `.env` (xem D-mới ở `01_MVP_Backend_Implementation_Plan.md` §3).

**Quyết định:** bất kỳ module nào cần lưu file/ảnh (MF03 hồ sơ y tế/giấy tờ, **MF04 tài liệu tuân thủ đối tác UC-101/102/091**, MF07 ảnh biên bản kiểm tra, MF08 ảnh mẫu lưu, MF11 ảnh hiện trường sự cố, MF10 ảnh đính kèm phản ánh…) **phải** inject `StorageService` có sẵn ở `common/storage`, **không** tự gọi `MultipartFile.transferTo()`/AWS SDK riêng trong service của module. Key gợi ý theo khuôn `"<module>/<mã-nghiệp-vụ>.<ext>"` (vd MF01 dùng `"avatars/" + user.getCode() + ext`).

**Việc phải làm ở từng TDS:** module nào có UC upload ảnh/file (MF03, **MF04**, MF07, MF08, MF10, MF11) — §9 API ghi rõ *"lưu qua `common.storage.StorageService`, không tự triển khai I/O"* thay vì mô tả lưu đĩa/S3 tay.

> **Sửa 2026-08-02:** danh sách trên trước đây **bỏ sót MF04** dù MF04 có **2 luồng upload**. Lỗi này do chính `TestSpec-MF04` §2 **L9** phát hiện — ghi nhận và sửa tại đây.

### D10-BIS — Upload đi qua backend; tách file CÔNG KHAI với file RIÊNG TƯ

> 🟡 Bổ sung 2026-08-02 (Huy) — **chờ Nam ký**. Phát hiện khi nối FE MF03: `proofFileId` (khai
> dị ứng mức SEVERE, BR-031) và `documentId` (chế độ ăn đặc biệt) đều đòi **UUID của tài liệu đã
> upload**, mà toàn hệ thống chưa có endpoint upload nào ngoài `POST /profile/me/photo` của MF01
> (grep `@PostMapping` multipart). Mục 2 dưới đây là **rủi ro lộ dữ liệu, không phải chuyện phong
> cách** — đọc trước khi code bất kỳ UC upload nào.

**1. Upload LUÔN đi qua backend (`multipart/form-data`). KHÔNG dùng presigned URL.**

Không phải vì đơn giản hơn, mà vì presign **làm mất tầng kiểm nội dung**. `ProfileService.updatePhoto()`
đang kiểm ba tầng — dung lượng (MSG-008), content-type, và **magic bytes**:

> `/** Chặn SVG (XSS) và file đổi tên: quyết định theo nội dung thật của file. */`

Với presign, file đáp thẳng vào R2 **trước khi server nhìn thấy byte nào** ⇒ không chặn được SVG/XSS
hay file đổi đuôi. Chấp nhận được với avatar thì cũng không chấp nhận được với **tài liệu y tế**
(MF03) hay **ảnh hiện trường sự cố** (MF11). Presign còn kéo theo cấu hình CORS trên bucket, thêm một
round-trip, và sinh **object mồ côi** khi client xin presign rồi bỏ ngang (có file trong bucket, không
dòng DB nào trỏ tới). Kích thước file của dự án là PDF/ảnh ≤10MB — backend proxy thoải mái; presign chỉ
đáng từ ~100MB hoặc traffic cao.

*(Ngoại lệ được phép cân nhắc SAU: presigned URL cho **tải xuống** file riêng tư — URL hết hạn theo thời
gian, tốt hơn proxy byte qua Spring. Không thuộc phạm vi bây giờ.)*

**2. ⚠️ `store()` trả URL CÔNG KHAI — không dùng nó cho tài liệu riêng tư.**

```java
// R2StorageService.store()
return publicBaseUrl + "/" + key;      // https://cdn.sfsm.io.vn/<key>
```

Trong khi javadoc của `mf03/domain/HealthDocument` ghi ngược lại:

> *"KHÔNG public URL. Tải xuống có RBAC + audit (không expose `storageKey` thô ra JSON)."*

⇒ Ai tái dùng `store()` cho tài liệu y tế là **biến hồ sơ bệnh án của học sinh thành công khai với bất
kỳ ai có URL** (`cdn.sfsm.io.vn` là custom domain, ngụ ý bucket cho phép đọc public — **cần kiểm lại
bucket policy**). Vi phạm PDPD.

| Loại | Ví dụ | Lưu gì vào DB | Trả gì ra JSON |
|---|---|---|---|
| **Công khai** | avatar (MF01) | URL đầy đủ | URL |
| **Riêng tư** | tài liệu y tế (MF03), **tài liệu tuân thủ đối tác (MF04)**, ảnh sự cố (MF11), biên bản kiểm tra (MF07), ảnh mẫu lưu (MF08), chứng nhận lô (MF06) | **`storageKey`** | **id tài liệu**, không bao giờ là URL hay `storageKey` |

> **Loại thứ ba — KHÔNG LƯU GÌ CẢ.** `TDS-MF04` §9.2 (M04-D11) chốt: ảnh khuôn mặt người giao hàng
> (UC-068) **không** đi qua `StorageService`, **không** ghi đĩa/PostgreSQL — đẩy thẳng
> `FaceRecognitionPort`, chỉ giữ `faceId` do FRS trả (ADR-M04-5, PDPD). Dữ liệu sinh trắc học nhạy
> cảm hơn cả hồ sơ y tế; cách an toàn nhất là **không giữ bản gốc**. Module nào nhận ảnh sinh trắc
> hãy theo tiền lệ này thay vì xếp nó vào nhóm "riêng tư".
>
> ⚠️ *Sửa 2026-08-02: bản D10-BIS đầu tiên xếp nhầm ảnh khuôn mặt MF04 vào nhóm "riêng tư" (tức có
> lưu). Sai — MF04 có **1** luồng lưu trữ, không phải 2.*
>
> **MF04 là "consumer thứ hai"** cho câu hỏi ở mục 5: `partner_documents` gắn `partner_id`, không gắn
> `student_id` ⇒ **không** dùng chung được `health_documents`. Khi Mạnh code UC-101/102 là lúc chốt:
> bảng `documents` dùng chung (`owner_type` + `owner_id`) hay mỗi module một bảng.
>
> ⚠️ **Việc phải quyết ngay cho MF04:** `TDS-MF04` §5.2 khai `file_key VARCHAR(255)` với chú thích
> *"giá trị `StorageService.store()` trả về"* — mà `store()` trả **URL công khai**, và key lại theo
> khuôn đoán được (`partner-documents/<code>.<ext>` → `partner-documents/DOC-GF-05.pdf`). Bucket public
> + key đoán được = **liệt kê được toàn bộ giấy phép kinh doanh / chứng nhận ATTP của mọi đối tác**.
> Response hiện KHÔNG lộ `file_key` (tốt), nhưng object vẫn với tới được. Chốt: tài liệu tuân thủ là
> **riêng tư** ⇒ `file_key` lưu **storageKey**, tải xuống qua endpoint có `@PreAuthorize` — đúng như
> tên cột đang gợi ý.

File riêng tư tải xuống qua endpoint riêng có `@PreAuthorize` + ghi audit, **không** qua CDN.

**3. JSON tham chiếu file bằng ID tài liệu, không phải tên file** — nhất quán D1.
`proofFileId`, `documentId` nhận **UUID/mã của bản ghi tài liệu**. Gửi tên file (`"don-thuoc.pdf"`)
phải bị từ chối 422, không được đoán ý. *(FE mock trước đây gửi tên file — mọi màn upload phải sửa
khi nối thật.)*

**4. Việc dọn kèm khi module đầu tiên cần upload code tới:** rút phần validate ra khỏi
`ProfileService` thành `common/storage/FileValidator`. Hiện `hasAllowedMagicBytes` / `startsWith` /
`MAGIC_JPEG|PNG|PDF` / `suffixOf` đều là `private` trong đó ⇒ module sau chắc chắn copy-paste, và chỉ
cần **một người quên chép hàm magic-bytes** là mất luôn lớp chặn SVG/XSS.

**5. Chưa chốt — quyết khi có consumer thứ hai:** một bảng `documents` dùng chung hay mỗi module một
bảng? `health_documents.student_id` đang là `nullable = false` nên **không chứa được** chứng nhận lô
của MF06 hay ảnh sự cố MF11. Đừng thiết kế bảng dùng chung khi mới có một consumer — quyết khi MF06
(hoặc module nào tới trước) thực sự cần, lúc đó mới đủ dữ kiện.

---

## Bảng tra nhanh: quyết định nào gỡ mismatch nào

| Quyết định | Mục review được gỡ |
|---|---|
| D1 | A1, B2, B4, C1, E3, F6, I2, I10 |
| D2 | A2, A6, A7 |
| D3 | A3 |
| D4 | A4, C3, E5, H8 |
| D5 | A5, C3, I2 |
| D6 | D1, D2, E1, E4, G2, H1, H5, H6 |
| D7 | A6 |
| D8 | — *(không gỡ mục nào; là cổng máy kiểm để D1/D2/D4/D6 không tái phát qua `/v3/api-docs` → T3.4)* |
| D9 | — *(phát hiện mới khi Nam dựng `BaseEntity` audit trail cho MF01, không nằm trong 76 mismatch gốc của `review.md`)* |
| D10 | — *(phát hiện mới cùng đợt — object storage abstraction chưa tồn tại lúc viết `review.md`)* |

---

## Checklist áp dụng cho chủ module

- [ ] §3 có dòng trỏ file này
- [ ] §5.2 có cột `code` + `UNIQUE(school_id, code)`; PK là UUID (D1)
- [ ] §9.2 path param dùng `{code}`; mọi field ngày là ISO (D4); không còn `@JsonFormat("dd/MM/yyyy")`
- [ ] §9.2 mọi enum dùng giá trị ở bảng D6 — không tự khai lại trong module
- [ ] §10 có ghi envelope D2 ở đầu bảng; mã lỗi đã có MSG chính thức (không còn "mã cục bộ")
- [ ] §17 không còn ràng buộc mâu thuẫn D1–D7
- [ ] §5.2 không tự khai lại cột `created_by`/`updated_by` (reserved cho `BaseEntity`, D9); field nghiệp vụ "ai làm" đặt tên khác kiểu `<hành_động>_by_<loại>_id`
- [ ] §9 mọi UC upload file/ảnh ghi rõ dùng `common.storage.StorageService`, không tự viết I/O (D10)
- [ ] §9 UC upload dùng `multipart/form-data` qua backend, **không** presigned URL; có kiểm dung lượng + content-type + magic bytes (D10-BIS.1)
- [ ] §5.2/§9 phân loại rõ file **công khai** (lưu URL) vs **riêng tư** (lưu `storageKey`, JSON chỉ trả **id tài liệu**, tải xuống qua endpoint có `@PreAuthorize` + audit) — D10-BIS.2
- [ ] §9 field tham chiếu file nhận **id tài liệu**, không nhận tên file (D10-BIS.3, nhất quán D1)

**D8 (chỉ áp dụng sau khi Nam approve):**

- [ ] §9.2 mỗi endpoint có `@Operation` với `description` chứa UC ID + BR ID (S2)
- [ ] §10 mỗi mã lỗi có cột HTTP status và có `@ApiResponse` trỏ `ErrorResponse` (S3)
- [ ] grep `@Schema(allowableValues` trong tài liệu → **0** kết quả (S5)
- [ ] grep `example` của field `id` → không dòng nào là UUID (S6)
- [ ] §13 có contract test đọc `/v3/api-docs` với 3 assertion (uuid / ErrorResponse / enum D6)

---

## Changelog

| Ngày | Thay đổi | Người đề xuất | Trạng thái |
|---|---|---|---|
| 2026-07-29 | Lập D1–D7 từ `review.md` mục A + `review_fix_plan.md` Phase 0 | Bùi Quang Huy | ✅ Approved 2026-07-30 — Nam |
| 2026-07-30 | **Thêm D8 — Tài liệu hoá OpenAPI (Swagger)**, 8 luật S1–S8. Lý do: Swagger đang là cổng nghiệm thu (`01_MVP…` §Nghiệm thu) và nguồn sinh type FE (T3.4) nhưng chưa được đặc tả — grep annotation springdoc trên `07_implement/` ra 0 kết quả. | Bùi Quang Huy | ⏳ **Chờ Nam approve** |
| 2026-07-31 | **Thêm D9 — cột `created_by`/`updated_by` reserved cho `BaseEntity`** và **D10 — upload file/ảnh qua `common/storage/StorageService`**. Phát sinh khi Nam dựng audit trail (`@CreatedBy`/`@LastModifiedBy` + `AuditorAware`) và storage abstraction (Local/R2) cho MF01 — phát hiện MF03/MF05/MF06/MF12 đã tự khai cột trùng tên khác kiểu. | Nam (tự phát hiện khi build MF01 backend) | ✅ Approved 2026-07-31 |

| 2026-08-02 | **Thêm §0-BIS — Trạng thái code thật.** Lý do: MF01+MF02 đã merge vào `dev` (31/07) nhưng **13 TDS + MVP plan vẫn viết "Flyway V1 = toàn bộ schema, Nam giữ"** — mô hình đó đã bị bỏ từ khi MF02 dùng `V3`. Khi đồng bộ TDS-MF03 với code thật (v1.1) phát hiện 4 lỗi hệ thống mà 10 module còn lại **chắc chắn cũng mắc** vì tài liệu của họ viết cùng thời điểm, trước khi có backend: (1) entity tự khai cột audit → duplicate column, build đỏ; (2) cột mã đặt tên riêng + unique toàn cục thay vì `code` + `UNIQUE(school_id, code)`; (3) khóa ngoại trong JSON là UUID thay vì `code`; (4) khai lại enum của D6. §0-BIS chốt vị trí project (`04_Development/Backend/`), khuôn package `com.sfms.mf<NN>`, luật đánh số Flyway, bảng 11 hạ tầng dùng chung, và chỉ MF02 làm module mẫu. **Đổi thứ tự ưu tiên:** code thật nay đứng trên tài liệu. | Bùi Quang Huy (phát hiện khi đồng bộ TDS/TestSpec-MF03 với backend) | ✅ Áp dụng ngay (mô tả hiện trạng, không phải quyết định mới) |
| 2026-08-02 | **Thêm D10-BIS — upload qua backend (không presign) + tách file công khai/riêng tư.** Phát hiện khi nối FE MF03: `proofFileId`/`documentId` đòi UUID tài liệu đã upload nhưng hệ thống chưa có endpoint upload nào ngoài `POST /profile/me/photo` (MF01). Đối chiếu code lộ ra mâu thuẫn thật: `R2StorageService.store()` trả **URL công khai** (`cdn.sfsm.io.vn/<key>`) trong khi javadoc `HealthDocument` yêu cầu **"KHÔNG public URL, tải xuống có RBAC + audit"** — tái dùng `store()` cho tài liệu y tế = lộ hồ sơ bệnh án cho bất kỳ ai có URL (PDPD). Chốt thêm: KHÔNG presign vì mất tầng kiểm magic-bytes (chặn SVG/XSS) mà `ProfileService` đang có; JSON tham chiếu file bằng **id tài liệu** chứ không phải tên file (nhất quán D1). Chưa chốt bảng `documents` dùng chung — `health_documents.student_id` là `NOT NULL` nên không chứa được chứng nhận lô MF06/ảnh sự cố MF11; quyết khi có consumer thứ hai. | Bùi Quang Huy (phát hiện khi nối FE MF03) | 🟡 **Chờ Nam ký** — riêng mục 2 (public URL) nên xử lý như lỗi bảo mật, không đợi ký |
| 2026-08-02 | **D8 chuyển từ ⏳ sang 🟡 "đang áp dụng thực tế".** Không phải Nam đã ký, mà là **bằng chứng trong code**: `OpenApiConfig.java` tồn tại và 11/11 controller MF01+MF02 mang đủ `@Tag`/`@Operation`/`@ApiResponse`. Để ⏳ nữa thì tài liệu mâu thuẫn code. **Vẫn cần Nam ký** để chuyển ✅. | Bùi Quang Huy | 🟡 Chờ Nam ký xác nhận |

**Khi Nam ký xác nhận D8, làm 2 việc:**

1. Đổi trạng thái D8 ở đầu file + 2 dòng bảng trên thành ✅ Approved.
2. Đổi đồng loạt `D1–D7` → `D1–D10` ở §3 của 12 TDS (24 dòng: 1 dòng tiêu đề + 1 dòng "Áp dụng…" mỗi file) và ở `00_Team_Work_Assignment_Plan.md` các mục §0/§2a/§2c-0/§2c-2/§2c-3/§2c-3b/§5/§6 — **không** đổi các dòng changelog lịch sử (§0 dòng 7/11/12, §2c-5) và **không** đổi `review_fix_plan.md` (là biên bản đợt sửa D1–D7, giữ nguyên).

**Việc tồn đọng do §0-BIS chỉ ra (không thuộc phạm vi lần sửa này):**

| # | Việc | Ai | Ghi chú |
|---|---|---|---|
| 1 | Rà 4 lỗi §0-BIS trong TDS/TestSpec của module mình **trước khi code** | mỗi chủ module | MF03 đã rà xong (v1.1). MF04–MF13 chưa. |
| 2 | ✅ **XONG 2026-08-03** — package trong TDS/TestSpec-MF05 đổi `com.sfms.menu` → `com.sfms.mf05` | Thảo | Làm trong Bước 1 của MF05 (`M05-D12`). Cùng đợt, MF05 phát hiện thêm: **§0-BIS nên có thêm lỗi #5 — "đặc tả lại resource module khác đã làm"**. `TDS-MF05` v1.1 khai lại nguyên `special_meal_requests` + `/api/v1/special-meals` mà MF03 **đã merge từ `V4`** ⇒ Flyway đỏ + 2 controller cùng path. Lệnh tự kiểm cho 9 module còn lại: `grep -rn "@RequestMapping" 04_Development/Backend/src/main/java/com/sfms/*/web/` và `grep -n "CREATE TABLE" 04_Development/Backend/src/main/resources/db/migration/*.sql` — đối chiếu với §5.2/§9.2 của TDS mình **trước khi code**. |
| 3 | Tạo `ParentAccountPort` (BR-010/012) | **Huy tự viết**, báo Nam vì file nằm trong `iam/` | **Không phải logic mới**: `iam/service/UserService.create()` đã làm đủ — kiểm email trùng (BR-012), `emailVerified=false` (BR-010), `activationService.issueAndSend()` token 48h + email (BR-011), sinh code, ghi audit. Port chỉ là adapter mỏng bọc method đó, truyền `RoleKey.PARENT`. Khai interface ở `iam/port/`, MF03 inject interface — **không** import `UserService` trực tiếp (ADR-M02-5) |
| 4 | Thêm `classroomCode/classroomIdByCode`, `userCode/userIdByCode` vào `ReferenceResolver` | **Huy tự viết**, Mạnh review | Bổ sung thuần, không đổi hành vi cái đã có. `iam/service/UserService` đã gọi `refs.schoolIdByCode(...)` — resolver vốn là tiện ích nhiều module dùng chung |
| 5 | Nối 4 field dẫn xuất MF02 đang trả `0` khi MF03 xong; `promoteStudents` hiện là stub rỗng nhưng endpoint đã public | Huy (khi làm MF03) + Mạnh review | `ClassroomService:214,224,237` · `AcademicYearService:188` |
| 6 | `.gitattributes` chốt LF/CRLF — mọi commit đang cảnh báo | Mạnh | Không chặn ai, nhưng làm diff bẩn |
