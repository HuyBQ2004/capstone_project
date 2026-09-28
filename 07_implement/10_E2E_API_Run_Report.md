# 10 — Báo cáo chạy E2E bằng API thật (08/08/2026)

> **Mục đích phiên này:** trả nợ lớn nhất còn lại — *"ĐO THẬT Giai đoạn 2"*. Phiên trước chỉ
> đo được tới **tầng phân quyền** vì DB trắng không có tài khoản MEDICAL_STAFF và không có
> sự cố/lô/mẫu nào. Phiên này dựng đủ dữ liệu **chỉ bằng API thật** rồi chạy hết đường.
>
> Đọc kèm: `Handoff-E2E-Flow-Readiness.md` (nhật ký) · `08_E2E_Findings_Backlog.md` (còn lại)
> · `seed/README.md` (cách chạy lại script seed).

## 0. Môi trường đã dùng

Stack **compose** (`http://localhost` qua nginx), DB `sep490_g8-postgres-1`, không phải
môi trường chạy tay. Ba điều phải làm trước khi đo được gì:

1. **`docker compose build backend`** — jar trong image thiếu **cả 4 endpoint** của phiên
   trước (`measure-units`, `sample-hold`, `.../disposition`, `dishes/{code}/students`);
   `/v3/api-docs` đếm 291 path, sau khi build lại là 295. Đúng cái bẫy runbook mục 9 đã cảnh
   báo, lần này dính thật.
2. **Trỏ SMTP về Mailpit** — backend đang nối **Gmail thật**, nên email kích hoạt tài khoản
   y tế bay ra ngoài và không lấy được token. Thêm `docker-compose.mailpit.yml` (override
   **không tự nạp**, phải truyền `-f` — không đụng gì tới cấu hình Gmail trong `.env`).
3. `MENU_LEAD_TIME_DAYS=0` — compose đã đặt sẵn.

## 1. Kết quả ĐO — ba câu hỏi của phiên này

| Câu hỏi | Kết quả đo |
|---|---|
| `GET /students/{code}/meal-history` bằng **token MEDICAL_STAFF** | **200** — và sau khi phát suất, trả **đủ danh sách món** (`DISH-THIT-KHO`, `DISH-CANH-CAI`). B4 thông thật, không chỉ đúng annotation |
| Chạy hết đường **cách ly lô → giữ mẫu → CAPA → xử lý lô → đóng** | **Sự cố `INC-2026-002` đạt `status = CLOSED`**. Trước đó `POST /close` khi lô chưa xử lý trả đúng **409 IEM-217**; `closure-readiness` đi từ `{capaOpen:1, studentsNotRecovered:2, quarantineBatchesUnresolved:1}` về `ready:true` |
| `suspected-dish-candidates` có khác nhau giữa 2 sự cố cùng ngày | **Có.** `INC-2026-002` (2 em) → mỗi món `affectedStudentsWhoAte = 2`; `INC-2026-003` (1 em, cùng ngày, cùng bữa) → **= 1**. Không còn trả toàn bộ món của trường |

Đo thêm, cùng đợt:

- **B3 chốt chặn hủy mẫu**: sau `sample-hold`, mẫu về `status = HOLD`, và bếp hủy nhận
  **409 SMP-112** (*"linked to an open food safety incident"*) — chặn vì **sự cố**, không
  phải vì mốc 24h. Mẫu KHÔNG bị giữ thì hủy nhận 409 **SMP-111** (còn 32h). Hai nhánh phân
  biệt được, đúng thiết kế.
- **Giai đoạn 1.4 nay thông thật**: đọc lại phiên kiểm thực vừa nộp thấy `step2Rows`/
  `step3Rows` mang **mã món** (`DISH-THIT-KHO`), nhờ đó `POST /samples` chọn được món và
  đăng ký lưu mẫu **201**.
- **Cổng BR-094 chạy thật lần đầu**: kiểm thực 3 bước PASSED ⇒ `serving-gate` trả
  `{allowed:true}` ⇒ **phát được cả 3 suất** (trước đây DB không có suất nào để bấm thử).
- **B1 chiều ngược**: `GET /incidents/{code}/dishes/{dishCode}/students` trả đủ 3 em kèm cờ
  `affectedByThisIncident`.
- **B5 bản đồ truy xuất — cập nhật lại mô tả trong backlog:** không còn "rỗng nghĩa" hoàn
  toàn. Đo được `supplierName`, `receivingSessionCode`, `deliveryDate` **có giá trị thật**;
  còn null là `batchId`, `ingredientName`, `supplierId`, và vẫn **không có món**.

## 2. Sáu lỗi backend tìm ra nhờ ĐO — đã sửa

### 2.1 MF06 trả 403 cho **đúng vai được giao quản lý bếp**

Tài khoản Đơn vị suất ăn tạo qua đăng ký đối tác mang claim `partnerId` nhưng **không có
`schoolId`**. `Mf06Scope.requireSchoolId()` đòi `schoolId` từ JWT ⇒ ném `accessDenied`.

Đo bằng chính tài khoản đó — **6 endpoint 403**: `/purchase-requests`, `/receiving-sessions`,
`/deliveries`, `/inventory`, `/inventory/issue-slips`, `/inventory-transactions`.
Mọi module khác 200. Hệ quả dây chuyền: **không mua được hàng ⇒ không có lô kho ⇒ luồng sự
cố không có gì để cách ly.**

MF05/07/08/09/10 **đã** xử đúng ca này từ lâu bằng `ReferenceResolver.activeSchoolIdForPartner()`
(12 nơi gọi) — **MF06 là module duy nhất còn sót**. Sửa: thêm vế đó vào `Mf06Scope`, bỏ hẳn
bản một tham số (mọi service MF06 đều đã inject sẵn `ReferenceResolver`), cập nhật 20 nơi gọi.
Đo lại: **6/6 endpoint → 200**.

### 2.2 MF11 làm CAPA không ai nhận được ⇒ **sự cố vĩnh viễn không đóng được**

Cùng gốc, module khác. `CapaServiceImpl.load()` / `IncidentServiceImpl` đọc thẳng
`actor.schoolId()`. Đo với tài khoản đối tác Đơn vị suất ăn:

| Gọi | Trước | Sau |
|---|---|---|
| `GET /incidents` | 200 nhưng **total = 0** (im lặng, không phải 403) | 200, total = 1 |
| `GET /incidents/{code}` | **404** | 200 |
| `GET /corrective-actions` | 200, **total = 0** | 200, total = 1 |
| `POST /corrective-actions/{code}/accept` | **404** | 200 |

Vì bên thực hiện không "nhận" được CAPA nên `closure-readiness.capaOpen` không bao giờ về 0.
Thêm `Mf11Scope` (cùng khuôn `Mf06Scope`), áp cho CAPA/Incident/Violation.

> Ghi chú cho người sau: **Javadoc trong `ReferenceResolver` nói SERVICE_CATERERS "KHÔNG có
> claim `partnerId` trong JWT"** — đo thật thì **có**. Câu đó chỉ đúng với tài khoản tạo qua
> `POST /staff`. Tồn tại **hai hình dạng tài khoản** cùng vai này, và code phải chịu được cả
> hai (nay `Mf06Scope`/`Mf11Scope` đều xử cả hai chiều).

### 2.3 `ClassCastException` làm **Bước 1 kiểm thực luôn 500** khi khai lô có thật

`ReferenceResolver.batchExpiryDate()` ép `(java.sql.Date)` trong khi driver trả
`java.time.LocalDate` ⇒ `POST /inspections/{code}/steps/1` **500** ngay khi Mẫu số 1 khai một
`batchId` **có thật** — tức đúng con đường BR-092/093 vừa mở ở A1. Lô không tồn tại thì
`batchId = null` nên không ai chạm vào, lỗi ẩn suốt.

Sửa bằng helper `toLocalDate(Object)` chịu cả ba kiểu (đúng cách `mf11/port/
InventoryBatchPortImpl` đã làm từ trước), áp cho cả 2 chỗ ép kiểu trong file.

### 2.4 Sai content-type / method / thiếu part ⇒ **500** thay vì 415 / 405 / 422

Gặp **3 lần** trong một buổi (`/partnership-applications`, `/samples`,
`/corrective-actions/{code}/evidence-photos`), cộng với ca `/incidents/{code}/assess` đã ghi
ở Handoff trước. Đều là **lỗi client** nhưng báo 5xx: người tích hợp đi tìm sự cố ở server,
và cảnh báo giám sát bị nhiễu. Thêm handler vào `GlobalExceptionHandler`:
`HttpMediaTypeNotSupportedException → 415` (kèm danh sách media type nhận được),
`HttpRequestMethodNotSupportedException → 405`, `MissingServletRequestPartException → 422`
(kèm **tên part** còn thiếu).

### 2.5 `ResponseStatusException` bị nuốt thành 500 — cả màn danh mục nguyên liệu

Chạy lại script seed trên DB đã có dữ liệu: `POST /ingredients` với mã trùng trả **500
IAM-500**, trong khi log ghi rõ service ném `409 CONFLICT "Mã nguyên liệu đã tồn tại:
ING-THIT-HEO"`. Nguyên nhân: `@ExceptionHandler(Exception.class)` của `GlobalExceptionHandler`
**thắng** cơ chế mặc định của Spring cho `ResponseStatusException`.

`IngredientServiceImpl` là chỗ duy nhất trong toàn backend dùng kiểu ngoại lệ này, nhưng dùng
**7 lần**: 409 trùng mã · 404 không tìm thấy (PUT/DELETE) · 422 validate. Nghĩa là **mọi lỗi
của màn `system-admin/ingredients` đều hiện "lỗi hệ thống"** — mà đây là màn **đầu tiên**
trong thứ tự dựng dữ liệu (runbook mục 11). Sửa: thêm handler giữ nguyên status + `reason`.

### 2.6 (phụ) `batchId` của Mẫu số 1 trả UUID thô — vi phạm D1

Đọc lại phiên vừa nộp: `step1Fresh[].batchId = "680bc2a7-…"` thay vì `"SB-THIT-HEO"` client
vừa gửi. Đúng lớp lỗi mà bản vá 05/08 đã sửa cho `dishId` Mẫu 2/3 nhưng **bỏ sót Mẫu 1**.
Thêm `ReferenceResolver.batchCode()` và dịch ở `InspectionAssembler`.

## 3. Script seed — `07_implement/seed/`

Toàn bộ dữ liệu trên được dựng **chỉ bằng API** và đã gói thành script chạy lại được:
`seed_demo.py` (8 pha, chạy cả gói hoặc từng pha) + `seed_lib.py` + `README.md` +
`.env.seed.example`. Mật khẩu để trong `.env.seed` — **đã bị `.gitignore` bắt** qua mẫu
`.env.*`, không lên repo.

Vì đi qua đúng endpoint người dùng bấm, dữ liệu seed có đủ audit log, mã sinh theo
`code_sequences`, và **đụng mọi gate BR** — chính nhờ vậy phiên này lộ ra 5 lỗi trên. Các bẫy
gặp khi viết script (field nguyên thuỷ vắng mặt ⇒ 422 vô danh, 3 endpoint multipart, các giá
trị bị DB CHECK ràng buộc, giáo viên phải là GVCN mới thấy suất) ghi ở `seed/README.md`.

**Đã chạy lại để chốt (không phải "viết xong rồi tin"):** chạy `seed_demo.py` toàn bộ trên DB
đã có dữ liệu — mọi bước trùng trả **409 và được bỏ qua đúng**, các bước mới chạy tiếp bình
thường. Riêng pha `incident` chạy lại sinh **sự cố mới `INC-2026-005` và đóng được (CLOSED)**,
tức đường nghiệp vụ lặp lại được chứ không phải may mắn một lần. Bẫy đã xử trong lúc chốt:
cách ly lô là **một chiều** (IEM-149B), nên script **chọn lô còn tự do từ `GET /inventory`**
thay vì cắm cứng mã lô.

## 4. Bốn lệnh xác minh (sau khi sửa)

| Lệnh | Kết quả | Baseline |
|---|---|---|
| `mvn test` | **352 pass, 0 failure, 0 error, BUILD SUCCESS** | 352 ✅ |
| `npx tsc --noEmit` | sạch | sạch ✅ |
| `npx next lint` | **7 warning, 0 Error** | ≤8 warning, 0 Error ✅ |
| `npm run build` | **181 route** | 181 ✅ |

> ⚠️ **Bẫy môi trường mới, ghi để khỏi mất 40 phút như phiên này:** `mvn test` **chết vì hết
> RAM** (`Native memory allocation (mmap) failed`) khi stack compose đang chạy — Testcontainers
> cần dựng thêm container riêng. Triệu chứng dễ đọc nhầm thành lỗi test: Mockito báo
> *"Could not initialize plugin: MockMaker"*, hoặc surefire báo fork chết ở một class đã ghi
> `Tests run: 120, Failures: 0`. **Cách xử: `docker compose stop` trước khi chạy `mvn test`**,
> và kill các JVM mồ côi còn sót từ lần chạy bị huỷ.

## 5. Rà UI — CHƯA LÀM, và đây là 4 mối nối FE còn hở (đã grep xác nhận 08/08)

Phiên này **chỉ đo API, chưa bấm giao diện lần nào**. Grep phía FE cho thấy backend xong
nhưng UI chưa có đường vào:

| Hở | Bằng chứng | Hệ quả khi demo bằng UI |
|---|---|---|
| `sample-hold` | chỉ xuất hiện trong `lib/api/schema.d.ts` (type sinh tự động), **0 màn gọi** | Y tế **không giữ được mẫu** từ giao diện |
| Màn y tế tra suất ăn của trẻ | `app/(app)/medical-staff/` **không có** thư mục `students*` | B4 xong ở backend nhưng **không có đường vào** |
| `dishes/{dishCode}/students` | 0 nơi gọi | Thiếu chiều ngược khoanh vùng |
| `batchId` Bước 1 | `step1RowsOfIssue()` (`lib/mock-data.ts`) **không set `batchId`** | BR-092/093 **vẫn đứt ở đầu FE** — backend đã nhận đúng mã lô (đo được ở mục 1) |

**Ngược lại, chuỗi CAPA thì đã nối đủ nút** (grep `action: "…"`): `approve`/`reject`
(school-admin) · `accept` + `submit-evidence` + `evidence-photos` (service-caterers) ·
`submit-for-approval` + `validate` + `rework` (medical-staff). B6 chỉ thiếu **màn chi tiết**,
không thiếu thao tác — sửa lại mô tả B6 trong backlog theo ý này.

**Việc tiếp theo:** rà UI bằng **Playwright MCP** (Nam đã chạy
`claude mcp add playwright -- npx @playwright/mcp@latest`; MCP chỉ nạp khi khởi động lại
phiên). Đi theo thứ tự demo: đăng nhập 6 vai → luồng bếp → luồng sự cố. Dữ liệu đã sẵn trên
DB compose, không cần dựng lại — hoặc chạy `07_implement/seed/seed_demo.py`.

## 6. Còn lại / cần Nam chốt

1. ~~**Bỏ bước báo giá?**~~ — **Nam chốt 08/08: GIỮ NGUYÊN.** (Ghi lại phân tích để khỏi hỏi lại:) Kỹ thuật thì bỏ được: `supplier_products`
   đã có `unitPrice` + `effectiveFrom` — tức **đã là bảng giá hợp đồng**, `quote` chỉ chép
   lại. Nhưng `quote → accept-price → confirm-schedule` là UC-139/CF-M06-5 đang có trong
   đặc tả và trong màn hình NCC. **Đề xuất:** giữ endpoint, nhưng cho trường hợp đã có giá
   hiệu lực thì tự điền và cho phép "chấp nhận nhanh" — đổi 3 bước bấm thành 1, không phải
   xoá luật. Chưa làm, chờ Nam quyết.
2. ~~**Tài khoản `STF-003` (`kitchen.e2e@example.test`)**~~ — tạo trong lúc dò lỗi theo mô hình
   "nhân viên bếp" mà Nam xác nhận **không tồn tại**; **đã vô hiệu hoá** (`PATCH
   /staff/STF-003/status {active:false}` → `INACTIVE`).
3. **Chưa đo lại**: `retrievableForIncident` vẫn bỏ qua `suspectedDishIds`;
   `GET /sample-lab-requests/retrievable?incidentCode=…` trả **422 `incidentId invalid`**
   (tham số tên gì chưa rõ, cần xem lại hợp đồng API).
4. `SMP-112` in **UUID sự cố** trong thông báo lỗi thay vì mã `INC-…` (D1, và là chuỗi hiện
   ra cho người dùng cuối).
5. **Tiền tố mã lỗi của `/ingredients` là `IAM-…`** (`IAM-409`, `IAM-404`) chứ không phải
   `ING-…` — `ModuleCodeResolver` chưa map đường dẫn này. Không sai hành vi, nhưng lệch quy
   ước mã lỗi theo module.
6. `TraceabilityService:54` dùng `rs.getDate(4)` rồi `expiry.isBefore(today)` — lô không có
   hạn dùng sẽ **NPE**. Chưa sửa (ngoài phạm vi phiên này), đã ghi để không quên.
7. **B5/B6 vẫn nợ**: bản đồ truy xuất còn 3 field null + không có món; 2 màn chi tiết CAPA
   phía FE.
