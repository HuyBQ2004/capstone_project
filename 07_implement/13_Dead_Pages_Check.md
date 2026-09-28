# Soi trang chết / link gãy — `npm run check:routes` (09/08/2026)

**Vì sao có công cụ này.** Nam hỏi *"sao không có cách nào để vào trang này:
`/service-caterers/menus/create`"*. Đo: `grep -rn "menus/create"` → **0 kết quả**. Route tồn tại,
được viết để mở từ lịch tuần kèm `?grade=&date=&meal=`, nhưng **link gọi nó đã mất trong một lần
refactor**. Cả nhánh soạn thực đơn chi tiết (thành phần, định lượng, dinh dưỡng, AI Assist
UC-075) không ai vào được — mà `tsc`, `lint`, `build`, `mvn test` đều **sạch**. Không lệnh nào
trong bộ 4 lệnh xác minh bắt được lớp lỗi này: nó không phải lỗi kiểu, không phải lỗi lint.
Chỉ có **đếm liên kết** mới thấy.

Nam yêu cầu: *"lát viết code check xem có trang nào chết không thể nào dẫn vào tương tự không"*.

## 1. Công cụ

`04_Development/Frontend/scripts/check-routes.ts` — chạy `npm run check:routes`. Soi **hai
chiều**:

* **Trang chết** — có `page.tsx` nhưng không file nào dẫn tới ⇒ chỉ vào được bằng gõ tay URL.
* **Link gãy** — có link nhưng không route nào khớp ⇒ bấm vào ra 404.

Thoát mã 1 khi có phát hiện, nên cắm vào CI được.

### Hai lần sửa vì công cụ tự nó sai

1. **Bản đầu chỉ bắt `href=` / `router.push(`** ⇒ **22 "trang chết"**, phần lớn là báo động giả:
   màn chi tiết được dẫn qua `rowHref={(c) => ...}` — một prop của `DataTable`, không khớp mẫu
   nào. Đuổi theo từng tên prop là trò không hồi kết. Đổi sang quét **mọi chuỗi bắt đầu bằng
   `/`**.
2. **Rồi lại cho `*` (phần `${…}`) khớp mọi segment cho "an toàn"** ⇒ **0 phát hiện**: chỉ cần
   một chỗ viết `` `/school-admin/${x}` `` là mọi màn hai-segment dưới `school-admin` đều được
   coi là đã nối. Thử bằng một trang mồ côi dựng tạm — script **im lặng**. Siết lại: `*` chỉ
   khớp segment **động** (`[id]`).

> Một công cụ luôn báo "sạch" tệ hơn không có công cụ, vì nó tạo niềm tin sai. **Đã chứng minh
> nó biết kêu** trước khi tin nó: dựng tạm một `page.tsx` mồ côi → báo đúng 1 trang chết; dựng
> tạm một link tới route không tồn tại → báo đúng 1 link gãy.

## 2. Kết quả đo trên toàn bộ 180 route

Lần chạy đầu:

```
✗ 6 TRANG CHẾT · 3 cầu chuyển hướng @deprecated (đúng thiết kế) · ✓ 0 link gãy
```

Sau khi xử lý điểm mù `basePath` (2.1) và nối lại `menu-approval/[id]` (mục 3) — **baseline hiện
tại, exit 0 nên cắm CI được**:

```
Đã quét 180 route · 1090 chuỗi đường dẫn · 436 link điều hướng
✓ Không có trang chết  · 3 cầu chuyển hướng @deprecated  · ✓ Không có link gãy
```

### 2.1 Xử lý điểm mù `basePath` — không nới lỏng phép khớp

`` <Link href={`${basePath}/${a.id}`}> `` không phân giải tĩnh được ⇒ 5 màn `announcements/[id]`
bị báo nhầm. Nhưng **giá trị của prop thì có trong code**:
`<AnnouncementList basePath="/teacher/announcements" />`. Script nay ghép hai mảnh: tìm mọi prop
được dùng theo kiểu `` `${<prop>}/…` ``, rồi lấy các chuỗi truyền vào đúng prop đó. Không đoán,
và **không** phải nới lỏng phép khớp — thứ từng làm công cụ mù hoàn toàn.

| Route | Kết luận sau khi kiểm TAY |
|---|---|
| `/{5 vai}/announcements/[id]` | **BÁO ĐỘNG GIẢ** — `AnnouncementList` ghép `` `${basePath}/${id}` ``. **Đã xử lý ở script** (xem 2.1), không còn báo |
| `/school-admin/menu-approval/[id]` | **LỖI THẬT** — xem mục 3 |
| `/medical-staff/report-center`, `/medical-staff/incidents/[id]/investigation`, `/service-caterers/special-meals` | **Đúng thiết kế** — cầu chuyển hướng giữ link cũ khỏi 404. Script tự xếp riêng (có `@deprecated`, hoặc không render gì ngoài `router.replace`) |

## 3. Lỗi thật tìm được: màn duyệt thực đơn chi tiết không có đường vào

`app/(app)/school-admin/menu-approval/[id]/page.tsx` — **550 dòng**, hiện đủ món, khai báo dị
ứng, danh sách học sinh, kèm nút Duyệt/Từ chối. **Không nút nào dẫn tới.**

Màn danh sách (`menu-approval/page.tsx`) duyệt **ngay trên lịch** (`approveDay`), còn bấm tên
thực đơn thì mở **dialog** xem món. Tức Ban giám hiệu ký duyệt thực đơn mà **không mở được hồ sơ
đầy đủ** — đúng lớp lỗi B6 của CAPA đã sửa hôm 08/08 ("duyệt trên dialog vài dòng mô tả").

**Nam chốt hướng (a)** — giữ cả hai: dialog cho thao tác duyệt nhanh cả tuần, thêm nút **"Xem hồ
sơ đầy đủ"** cho lúc cần soi kỹ. (Hướng (b) — bỏ dialog, bấm tên đi thẳng màn chi tiết — bị loại
vì duyệt cả tuần mà mỗi ngày phải mở một trang thì rất nặng tay.)

Cài đặt: thêm prop `fullRecordHref` vào `TemplateDetailDialog` (dùng chung với màn thực đơn của
đơn vị suất ăn) — **cùng khuôn với `issueHref` đã có sẵn**, nên không đẻ ra kiểu mới; chỉ màn
`menu-approval` truyền vào.

### 3.1 Nối được đường vào thì lộ ngay lỗi thứ hai: màn đó CRASH

Bấm thử trên trình duyệt: **"Application error: a client-side exception has occurred"** — màn
trắng. Console:

```
TypeError: Cannot read properties of null (reading 'toLocaleString')
```

`menu.estimatedCost` / `menu.costCeiling` là **null** với thực đơn không đi qua form soạn thảo
(seed, hoặc ghép từ thực đơn mẫu ở lịch tuần — đo trên `MENU-E2E-001`). Kiểu sinh từ OpenAPI khai
`number` nên `tsc` không cản, còn trang gọi thẳng `.toLocaleString()`.

Trang chết nên **chưa ai từng gặp lỗi này** — nối đường vào là nó hiện ra ngay. Đã sửa: null thì
hiện nhãn *"Chưa có chi phí ước tính"* thay vì sập cả màn; `allergyConflicts` cũng bọc `?? 0`.

Đo lại: màn mở bình thường, hiện *Đã công bố · Chưa có chi phí ước tính · Bữa trưa 380 kcal*.

## 4. Phát hiện kèm: `npm run audit` đã hỏng sẵn — nay dọn xong

Đo được **cả hai** nhánh của `npm run audit` đều chết, và chết từ lâu:

| Script | Lỗi | Nguyên nhân |
|---|---|---|
| `audit:data` (`semantic-audit.ts`) | `TypeError: Cannot read properties of undefined (reading 'map')` | đọc `mock-data.INVENTORY` |
| `audit:seed` (`seed-read-guard.ts`) | `ENOENT: src/lib/api/db.ts` | canh một file **đã bị xoá** |

Cả hai đều canh **mock DB**, thứ đã gỡ ngày 06/08/2026 — tiền đề của chúng không còn.
`git log` xác nhận không ai đụng vào từ `bbfedb7`.

**Đã xoá hẳn hai script**, `npm run audit` nay gọi `check:routes`. Lý do không "sửa cho chạy
lại": một lệnh kiểm tra luôn đỏ vì lý do không liên quan sẽ dạy cả đội bỏ qua nó — nguy hiểm hơn
là không có lệnh nào.

> **Còn nợ:** một bản kiểm ràng buộc ngữ nghĩa trên **DB thật** (kiểu "phiếu xuất trỏ đúng lô
> chưa", "mẫu lưu gắn đúng sự cố chưa") vẫn đáng có — `semantic-audit.ts` cũ là ý tưởng đúng đặt
> sai nguồn dữ liệu. Chưa làm.

Tiện tay vá luôn `gen:api`: nó trỏ cứng `localhost:8080`, cổng **không publish** khi chạy Docker
Compose ⇒ luôn `fetch failed`. Nay `gen:api` đi qua nginx, thêm `gen:api:local` cho ai chạy
backend bằng `mvn spring-boot:run`.

## 5. Xác minh

`tsc --noEmit` sạch · `next lint` 7 warning / 0 error (baseline) · `npm run build` thành công ·
`check:routes` chạy được và **đã chứng minh biết kêu ở cả hai chiều**.
