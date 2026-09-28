# Handoff — Rà soát gap toàn hệ thống (FE↔BE, phân quyền, UX)

**Ngày:** 07/08/2026 (đợt 1: `huybqhe180599`) · **cập nhật 07/08/2026 đợt 2:**
`namnvhe180863` — mục 3 và mục 5 ĐÃ XONG, xem bên dưới.
**Quan hệ với tài liệu khác:** đây là việc RIÊNG, tách khỏi `Handoff-Mock-Data-Removal.md`
(dọn mock data). Hai file đọc độc lập được.

## Cách rà (lặp lại được — đừng đoán từ code)

Toàn bộ kết luận dưới đây đo bằng **gọi API thật** với backend đang chạy, không suy từ code.
Script đã lưu vào repo tại **`07_implement/audit-scripts/`** — chạy lại được nguyên vẹn:

| Script | Việc |
|---|---|
| `scan_endpoints.py` | trích mọi resource FE gọi (regex theo hook), gom theo thư mục role |
| `scan2.py` | bản CHÍNH XÁC hơn — lần theo import để biết role nào THỰC SỰ với tới resource nào (ghi `by_role2.json`) |
| `check_exists.py` | đối chiếu resource FE gọi với `paths` trong OpenAPI đang chạy |
| `probe2.py` | đăng nhập 7 role, curl từng cặp (role × endpoint), lọc 401/403/5xx |
| `locate.py` | truy ngược FILE chứa lời gọi cho từng cặp nghi vấn — bước LỌC DƯƠNG TÍNH GIẢ |
| `ux_scan.py` | tìm màn thiếu trạng thái loading / mutation không xử lý lỗi |
| `scan_mut_handled.py` | (đợt 2) chấm TỪNG lời gọi factory-mutation: nơi gọi tự toast lỗi hay không |
| `apply_handled_meta.py` | (đợt 2) codemod gắn `{ handledError: true }` vào đúng những lời gọi "handled" |

Thứ tự chạy: `scan_endpoints.py > endpoints.json` → `check_exists.py` → `scan2.py` →
`probe2.py` → `locate.py` (dán danh sách cặp nghi vấn vào biến `PAIRS`).

```bash
# 1. Dựng backend (JDK 25, postgres cổng 5432 — container sfms-postgres)
docker start sfms-postgres
cd 04_Development/Backend && JAVA_HOME="/c/Program Files/Java/jdk-25.0.3" mvn -q spring-boot:run
# 2. Trích endpoint FE gọi, gắn role theo thư mục app/(app)/<role>/, lần theo import
# 3. Đăng nhập 7 role (pass chung P@ssw0rd123), curl từng cặp (role × endpoint), lọc 401/403/5xx
```

Tài khoản mẫu: `dangnh.sysadmin` · `tranth.admin` · `ngoth.medical` · `quynhdt.teacher` ·
`duchm.caterer` · `sonlt.supplier` · `binhnv.parent` (đuôi `.synthetic@example.test`).

**Bẫy đã mắc, đừng lặp:** gán chung resource của file dùng chung (`lib/`, `components/`) cho MỌI
role ⇒ 28 "gap" toàn dương tính giả (VD `lib/api/hooks.ts` tự chứa lời gọi `menus` trong
`useSpecialMeals()` nên mọi màn import hooks.ts đều "gọi menus"). Phải truy ngược file chứa lời
gọi rồi mới kết luận (`locate.py`).

## Kết quả tổng

| Hạng mục | Kết quả |
|---|---|
| Resource FE gọi có thật ở backend | **69/71** — 2 cái còn lại (`incident-events`, `menu-variants`) là dead code đã biết |
| Cặp (role × endpoint) đã dò | **162** |
| Gap phân quyền THẬT | **2** (số thô 28, lọc dương tính giả còn 2) |
| Màn có mutation KHÔNG xử lý lỗi | **37/86** → đợt 2 đo lại theo TỪNG lời gọi: **58/131** |
| Màn đọc API KHÔNG có trạng thái loading | **24** → đợt 2: **0** |

> Con số 37 (đợt 1) đếm theo FILE nên che mất các màn "nửa vời". Đợt 2 chấm theo
> TỪNG lời gọi (`scan_mut_handled.py`) ra 73 handled / 58 unhandled trên tổng 131:
> VD `service-caterers/dishes` có `uploadImage` tự toast nhưng `create`/`update`
> thì không — gắn cờ ở cả file sẽ làm 2 cái sau tiếp tục im lặng.

---

## ĐÃ SỬA — đợt 1 (nhánh `huybqhe180599`)

### ✅ 1. `apiErrorMessage()` bỏ mất `details[]` — 1 hàm, lan toả 36 màn

`lib/messages.ts` chỉ đọc `code` → `ERROR_LABELS` → `message`. `ApiError` **có** carry
`details` nhưng không ai đọc ⇒ cả 36 màn đã xử lý lỗi vẫn chỉ hiện `"Validation failed."`.

Ca thật đo được (NCC khai mặt hàng sai đơn vị):
```
422 ING-422  message = "Validation failed."
             details = [{field:"conversionToBase",
                         value:"bắt buộc khai rõ khi đơn vị báo giá (thung) khác đơn vị chuẩn (kg)"}]
```
Đã thêm `detailText()` nối `details` vào sau thông điệp chính (có chống lặp khi `message` đã
chứa sẵn nội dung đó).

### ✅ 2. Cổng kiểm thực BR-094 màn Giáo viên — nối endpoint đúng

`teacher/distribution/[id]` gọi `GET /inspections` (list) rồi tự tính bằng `mealServingGate()`.
**Hai cái sai cùng lúc:**
1. `InspectionController.list()` KHÔNG cấp quyền TEACHER ⇒ **403**, `items` luôn rỗng.
2. `mealServingGate([])` **FAIL-OPEN** — không thấy phiên nào thì `blocked:false` + `noSession:true`.

Hệ quả đo được (dữ liệu thật 07/08): phiên `INSP-20260807-L` đang `IN_PROGRESS` nhưng màn hiện
*"Chưa có phiên kiểm thực"* (sai) và nút Phát suất **vẫn bật**.

Đã đổi sang `GET /inspections/serving-gate?date=&mealType=` — endpoint server dựng đúng cho việc
này, `@PreAuthorize` **có TEACHER**, trả `{allowed, reasonCode, message, sessionId}`.
(Comment cũ trong file nói "chưa có endpoint GET trần cho gate" đã LỖI THỜI.)

> ⚠️ **HAI ĐƯỜNG SERVER KHÔNG CÙNG NGHĨA — đọc kỹ trước khi "đồng bộ":**
> · `GET /inspections/serving-gate` (`InspectionServiceImpl.checkGate`) trả `allowed:false` cho
>   **cả ba**: `NO_SESSION` · `IN_PROGRESS` · `INSPECTION_FAILED`.
> · `POST /student-distributions/{id}/serve` (`StudentDistributionServiceImpl` — **chốt chặn
>   thật**) chỉ ném lỗi khi có bước **FAILED**; không có phiên (`gate.isEmpty()`) hoặc phiên
>   đang dở thì **VẪN CHO PHÁT**.
>
> ⇒ FE chỉ DISABLE nút theo `INSPECTION_FAILED` (đúng cái server thi hành); `IN_PROGRESS` /
> `NO_SESSION` chỉ hiện cảnh báo. Disable theo `allowed:false` sẽ **chặn oan** GV trong ca mà
> server cho qua. **Đây là điểm cần BE chốt lại**: hai chỗ cùng mang danh "BR-094 gate" mà
> ngữ nghĩa lệch nhau.

Xác minh: `tsc --noEmit` sạch · `npm run build` compile hết 136 route · lint 8 warning (bằng
trước khi sửa, không thêm cái nào).

---

## ĐÃ SỬA — đợt 2 (07/08/2026, nhánh `namnvhe180863`)

### ✅ 3. Lỗi im lặng toàn app — XONG (đợt 2, Nam chốt hướng (a))

`components/app/providers.tsx` trước đây chỉ set `defaultOptions.queries`, **không có
`MutationCache` onError** và axios interceptor cũng không toast ⇒ 58/131 lời gọi `.mutate()`
không kèm `onError` thất bại **im lặng hoàn toàn**: bấm Lưu, không toast, dialog đứng im.
Ca chứng minh: `supplier/products` gọi `update.mutate({...}, { onSuccess: done })` — server
trả 422 kèm `details` rất rõ (mục 1) mà NCC không thấy gì.

**Đã làm — hướng (a), 3 phần:**

1. `lib/api/hooks.ts` — thêm `interface MutationOpts { handledError?: boolean }` + `metaOf()`,
   truyền `meta` vào **cả 8 factory** (`useCreate`, `useCreateAt`, `useUpdate`, `usePatch`,
   `usePatchAction`, `useDelete`, `useAction`, `useUploadMutation`). Tham số `opts` đứng
   CUỐI; riêng `useUploadMutation` nằm sau `method` nên nơi dùng mặc định phải viết đủ
   `("res", "post", { handledError: true })`.
2. `components/app/providers.tsx` — `mutationCache: new MutationCache({ onError })`, bỏ qua
   khi `mutation.meta?.handledError`, còn lại toast `apiErrorMessage(error, …)`.
3. Gắn cờ ở **73 lời gọi** đã tự xử lý lỗi + 1 `useMutation` thô
   (`service-caterers/notification-settings`). Làm bằng codemod `apply_handled_meta.py`
   (khớp 73/73, không sót cái nào phải sửa tay).

> ⚠️ **Quy ước từ nay:** màn nào tự `onError`/`try…catch` **bắt buộc** khai
> `{ handledError: true }`, nếu không sẽ ra **2 toast** — v5 gọi cả handler toàn cục lẫn
> callback của từng `mutate`. Ngược lại, màn KHÔNG tự xử lý thì **đừng** thêm gì: để handler
> chung lo. Kiểm lại bằng `scan_mut_handled.py` (cột `has_meta`).
>
> `ux_scan.py` vẫn in ra 37 màn "không xử lý lỗi" — **đó không còn là gap**, chỉ là những màn
> cố ý để handler chung lo. Đã ghi chú ngay trong script.

### ✅ 5. 24 màn đọc API không có trạng thái loading — XONG (đợt 2, làm hết cả 24)

Thêm `components/app/loading-skeleton.tsx` (`<LoadingSkeleton tiles blocks />`) và áp cho cả
24 màn; `ux_scan.py` chạy lại còn **0**. Ba kiểu xử lý tuỳ hình dạng màn:

| Kiểu | Dùng ở đâu | Cách làm |
|---|---|---|
| Chặn cả trang | 5 dashboard + `parent/health`, `medical-staff/compliance`, `service-caterers/meal-service` | giữ `PageHeader`, thân trang thay bằng `<LoadingSkeleton />` |
| Chặn từng khối | `school-admin/incidents` (3 ô KPI), `service-caterers/receiving` (2 ô), `system-admin/system-status` | chỉ khối đọc API mới xám, phần tĩnh giữ nguyên |
| Chặn từng ô/hàng | 4 màn dùng `DataTable` (`teacher/students`, `medical-staff/{health-records,inspections}`, `school-admin/allergy-review`) | `DataTable` đã tự có skeleton — chỉ cột/bộ lọc đọc **nguồn thứ hai** mới cần đánh dấu |

Điều cần giữ khi viết màn mới: cái nguy hiểm không phải "số 0" mà là những câu **khẳng định**
render ra từ mảng rỗng — "Lớp không có học sinh cần chế độ ăn đặc biệt" (BR-125),
"Chưa có phiên kiểm thực đạt" (BR-105), "Không truy vấn được" (DB), hay `tone="success"` xanh
lè trên ô CAPA chờ duyệt. Chặn **nhánh empty-state**, không chỉ chặn con số.

**Xác minh đợt 2:** `tsc --noEmit` sạch · `npm run build` compile hết 136 route ·
`next lint` 8 warning (đúng bằng trước khi sửa, không thêm cái nào) · `ux_scan.py` chạy lại:
màn thiếu loading **24 → 0**.

⚠️ Chưa chạy tay trên browser với backend thật — hai thứ nên kiểm mắt khi có dịp:
(1) một mutation hỏng ở màn KHÔNG tự xử lý (VD `supplier/products` khai sai đơn vị) phải hiện
đúng **1** toast; (2) một màn ĐÃ gắn cờ (VD `school-admin/grades`) cũng chỉ **1** toast.

---

## CHƯA SỬA — việc cho phiên sau (đều cần BE chốt)

### ⬜ 4. `GET /delivery-persons` không cấp SERVICE_CATERERS (cần BE quyết)

`DeliveryPersonController` = `hasAnyRole('SYSTEM_ADMIN','SUPPLIER')`. 4 màn SC dùng nó để tra
TÊN tài xế từ `driverId`: `inspections/[id]`, `orders/[id]`, `receiving/new`, `receiving/page`.
⇒ cột "Người giao hàng" rơi về id thô — **gồm cả cột (7) Mẫu số 1 kiểm thực ba bước**.
Hai hướng: mở quyền đọc cho SC, hoặc trả kèm `driverName` trong `DeliveryDto` (gọn hơn, không
mở thêm bề mặt).

### ⬜ 6. `service-caterers/meal-service` vẫn tự tính gate bằng `mealServingGate()`

SC **có** quyền `/inspections` nên KHÔNG lỗi như màn Teacher. Nhưng vẫn là logic trùng lặp với
`GET /inspections/serving-gate`. Nên hợp nhất sau khi BE chốt ngữ nghĩa gate (xem mục 2).
`mealServingGate()` trong `mock-data.ts` hiện CHỈ còn màn này dùng.

---

## Đã kiểm và XÁC NHẬN KHÔNG phải gap (đừng điều tra lại)

- `school-admin/config` → `GET /schools/{id}/config` **200**. (`GET /schools` collection 403 là
  đúng thiết kế — SCHOOL_ADMIN không được liệt kê mọi trường; màn này không gọi collection.)
- `supplier/dashboard` → `GET /partners/me` **200** (probe ban đầu nhầm sang collection).
- `parent/safety-concern` → `POST /source-reports` **422** (validation body rỗng) = CÓ quyền.
- Mọi 403 còn lại trong danh sách thô đều do file dùng chung, không màn nào thực sự gọi.
