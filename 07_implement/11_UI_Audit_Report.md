# 11 — Báo cáo rà giao diện bằng trình duyệt thật (08/08/2026)

> **Cách làm:** điều khiển Chromium qua Playwright MCP, đăng nhập **6 vai thật**, đi các màn
> chính trên **dữ liệu thật** (3 học sinh, thực đơn hôm nay đã công bố + đã phát, 2 mẫu lưu,
> 4 lô kho, 4 sự cố trong đó 2 đã đóng). Mỗi phát hiện dưới đây đều **nhìn thấy trên màn hình**
> và, khi cần, **đối chiếu ngược với API** để biết lỗi nằm ở FE hay BE.
>
> Đọc kèm: `10_E2E_API_Run_Report.md` (đo tầng API cùng ngày) · `08_E2E_Findings_Backlog.md`.

## Tóm tắt

Xương sống chạy được: đăng nhập cả 6 vai, không màn nào trắng/vỡ, luồng bếp và luồng sự cố
đều **nhìn thấy dữ liệu thật**. Chất lượng trình bày **khá cao** ở phần lớn màn (checklist
điều tra, cảnh báo lô cách ly, phiên kiểm thực).

Nhưng có **3 lỗi P0 sai lệch nghiệp vụ** và một **mẫu lỗi lặp lại**: *card thống kê đếm một
kiểu, bảng ngay bên dưới liệt kê một kiểu* — gặp ở **3 vai khác nhau**.

| # | Mức | Vấn đề | Ở đâu |
|---|---|---|---|
| 1 | 🔴 P0 | Lô đã xử lý vẫn hiện **"Chờ Caterers hoàn trả NCC"** | `medical-staff/incidents/[id]/page.tsx:1538` |
| 2 | 🔴 P0 | Phiên hết hạn ⇒ **hiện "Không có bản ghi nào"** thay vì báo đăng nhập lại | toàn hệ thống |
| 3 | 🔴 P0 | Trạng thái mẫu **`HOLD`** lọt ra màn hình dạng chuỗi enum thô | `service-caterers/samples` |
| 4 | 🟠 P1 | `formHeader` của Mẫu 1/2/3 **bị backend bỏ qua**, trả về giá trị **in cứng** | `InspectionAssembler.java:45-47` |
| 5 | 🟠 P1 | Card KPI mâu thuẫn bảng ngay dưới — **3 vai** | BGH · NCC · (Giáo viên) |
| 6 | 🟠 P1 | Y tế **không có nút giữ mẫu**, cũng **không thấy mẫu đang giữ** | `medical-staff/incidents/[id]` |
| 7 | 🟠 P1 | "Sự cố mới nhất" lấy **sai bản ghi** (không sắp xếp) | `teacher/dashboard` |
| 8 | 🟡 P2 | **4 kiểu định dạng ngày/giờ** khác nhau giữa các màn | nhiều màn |
| 9 | 🟡 P2 | Mỗi lần tải trang bắn **7 request 401** rồi mới retry | toàn hệ thống |
| 10 | 🟡 P2 | Bảng cuộn ngang + cắt chữ dù màn hình còn trống mênh mông | BGH · NCC |

---

## 🔴 P0-1 · Lô đã trả NCC vẫn hiện "Chờ Caterers hoàn trả"

**Nhìn thấy:** mở sự cố `INC-2026-005` (đã đóng) → mục *Bản đồ truy xuất & lô đã cách ly* →
lô `SB-CA-BASA` gắn badge vàng **"Chờ Caterers hoàn trả NCC"**.

**Đối chiếu API:** `GET /incidents/INC-2026-005` trả
`"disposition": "RETURNED_TO_SUPPLIER", "dispositionAt": "2026-08-08T06:35:14Z"`.
Backend **đúng** — nếu sai thì sự cố đã không đóng được (`closure-readiness` đòi
`quarantineBatchesUnresolved = 0`).

**Nguyên nhân — bản sao thứ hai bị bỏ sót.** Trong **cùng một file** có hai chỗ vẽ trạng thái lô:

- dòng ~1063: **đã sửa đúng**, đọc `b.disposition` → "Đã trả nhà cung cấp" / "Đã huỷ bỏ";
- dòng **1538**: `<Badge variant="warning">Chờ Caterers hoàn trả NCC</Badge>` — **in cứng**.

**Vì sao nghiêm trọng hơn vẻ ngoài:** khối đã sửa nằm trong form soạn thảo, **ẩn khi hồ sơ
read-only**; khối in cứng lại là khối **duy nhất còn hiển thị sau khi sự cố đóng**. Nghĩa là
**mọi sự cố đã đóng đều hiện sai trạng thái lô** — đúng thứ mà thanh tra sẽ đọc.

> Đây chính là lỗi mà Handoff phiên trước ghi *"✅ B1 đã nối FE — nay thay bằng disposition
> thật"*. Sửa một bản sao, còn bản sao kia sống nguyên. Cùng mô-típ với `NUTRITION_TARGET`,
> `menuLeadTimeDays`, `UNIT_FACTOR`.

## 🔴 P0-2 · Phiên hết hạn ⇒ màn hình nói "Không có bản ghi nào"

**Nhìn thấy:** đang ở màn **Kho nguyên liệu** của Đơn vị suất ăn, refresh token bị thu hồi
(vì cùng tài khoản **đăng xuất ở trình duyệt khác** — hành vi đúng của BR-004). Kết quả:
`POST /api/auth/refresh` → **401**, mọi request → 401, và màn hình **vẫn vẽ đầy đủ** khung,
tiêu đề, nút "Xuất kho FEFO", chỉ thay phần dữ liệu bằng **"Không có bản ghi nào."**

Tải lại trang vẫn y hệt. **Không** chuyển về trang đăng nhập, **không** toast, **không** báo
phiên hết hạn. Đăng nhập lại thì 4 lô hiện ra bình thường.

**Vì sao P0:** với màn Kho / Mẫu lưu / Sự cố, "rỗng" và "mất phiên" là hai chuyện hoàn toàn
khác nhau. Người dùng sẽ tin là **mất sạch dữ liệu** và có thể thao tác sai theo (nhập lại,
báo mất hàng). Cần: 401 ở `refresh` ⇒ xoá phiên + điều hướng `/login` kèm thông báo.

> Ghi rõ để không quy oan: nguyên nhân token chết là **đăng xuất nơi khác**, không phải lỗi
> backend. Lỗi nằm ở **cách FE phản ứng** với 401.

## 🔴 P0-3 · Trạng thái `HOLD` lọt ra màn hình

**Nhìn thấy:** `service-caterers/samples` → cột TRẠNG THÁI:

- `SMP-CANH-CAI` → badge xanh **"Đang lưu"**
- `SMP-THIT-KHO` → chữ trần **`HOLD`**, không badge, không dịch

`HOLD` là giá trị enum **mới thêm ở phiên trước** (chốt chặn B3 — mẫu đang bị điều tra thì
bếp không huỷ được). Backend thêm giá trị, **bảng nhãn phía FE không được cập nhật** ⇒ rơi ra
chuỗi thô. Và đây không phải trạng thái phụ: nó là trạng thái **quan trọng nhất** của màn này —
người trực bếp cần thấy ngay "mẫu này đang bị Y tế giữ, đừng đụng vào".

Đề xuất nhãn: **"Đang giữ để điều tra"** + badge cảnh báo, kèm link tới sự cố liên quan.

## 🟠 P1-4 · `formHeader` của biểu mẫu QĐ 1246 bị bỏ qua và thay bằng chuỗi in cứng

**Nhìn thấy:** chi tiết phiên `INSP-20260808-L` → *Thông tin mẫu biểu (in trên đầu mỗi Mẫu
1/2/3)*: **Tên cơ sở** = "Trường Trung Học Nam Nguyễn", **Địa điểm kiểm tra** = "Bếp trung tâm".

**Đối chiếu:** client gửi lên `facility: "Bếp trường"`, `location: "Khu bếp"`,
`checkedAt: "…T05:40:00Z"`. API trả về **giá trị khác hẳn**.

**Truy ngược code:**
- `SubmitStepRequest.formHeader` (`InspectionDtos.java:57`) **không được đọc ở service nào**
  (grep `formHeader()` → 0 nơi dùng ở tầng ghi);
- **không có cột** `facility` / `location` / `checked_at` trong `V12__mf07_inspection.sql`;
- `InspectionAssembler.java:45-47` **dựng lại** header khi đọc, với chuỗi
  **`"Bếp trung tâm"` in cứng trong Java**.

⇒ Đây là **field ma**: FE gửi, DTO nhận, không lưu, không dùng — và giá trị hiển thị là bịa.
Trên **biểu mẫu pháp lý** (QĐ 1246/QĐ-BYT) thì "Địa điểm kiểm tra" sai là vấn đề thật với
trường có nhiều điểm chế biến.

**Kèm theo:** `checkedAt` được format bằng `ZoneOffset.UTC` (cùng dòng code) ⇒ in **"08/08/2026
06:34"** trong khi giờ Việt Nam là **13:34**. Mọi mốc giờ trên 3 biểu mẫu đều **lệch 7 tiếng**.

## 🟠 P1-5 · Card KPI mâu thuẫn với bảng ngay bên dưới — lặp ở 3 vai

| Vai | Card nói | Bảng ngay dưới nói |
|---|---|---|
| **BGH** (`school-admin/dashboard`) | "Thực đơn chờ duyệt: **0**" | *Thực đơn chờ phê duyệt* — có **1 dòng** (08/08/2026, Cty An Bình) |
| **NCC** (`supplier/dashboard`) | "Giao hàng sắp tới: **0** · Chưa có chuyến nào" | *Giao hàng* — có **DLV-E2E-001, 08/08/2026** |
| **Đơn vị suất ăn** (`service-caterers/dashboard`) | "Mẫu đang lưu: **1**" | màn Lưu mẫu có **2 mẫu** (1 RETAINED + 1 HOLD) |

Ba màn khác nhau, cùng một bệnh: **card đếm theo một bộ lọc, bảng liệt kê theo bộ lọc khác**,
không ai đối chiếu. Riêng ca thứ ba còn nguy hiểm: mẫu bị **giữ để điều tra** biến mất khỏi ô
"Mẫu đang lưu" — trong khi nó là mẫu **bắt buộc phải còn**.

Đề xuất: card và bảng phải dùng **chung một hàm lọc**; nếu cố ý khác thì nhãn phải nói rõ
("Chờ duyệt hôm nay" vs "Tất cả thực đơn tuần này").

## 🟠 P1-6 · Y tế không giữ được mẫu, và không thấy mẫu đang giữ

Màn điều tra sự cố có checklist rất tốt, nhưng:

- Mục mẫu duy nhất là **"Thu hồi & gửi mẫu xét nghiệm"** (`sample-retrieval` — gửi đi lab).
  **Không có** mục "Giữ mẫu tại trường" (`sample-hold`). Muốn giữ mẫu, y tế buộc phải **khai
  khống là đã bàn giao đi xét nghiệm** — đúng cái mà B3 sinh ra để bỏ.
- Chiều đọc cũng hở: sự cố `INC-2026-005` **có 1 mẫu đang HOLD**, nhưng mục *Mẫu phục vụ điều
  tra* vẫn ghi **"Chưa gửi mẫu nào (không bắt buộc)"** ⇒ nhìn hồ sơ tưởng **không giữ mẫu nào**.
- Checklist điều tra **không có bước tra suất ăn của trẻ** (`meal-history`) — bước cốt lõi để
  biết em nào ăn món gì. Backend đã sẵn sàng (đo được 200), FE chưa có đường vào.

## 🟠 P1-7 · "Sự cố mới nhất" lấy sai bản ghi

`teacher/dashboard` → *Bảng tin hôm nay* → "Sự cố mới nhất: **Nghi ngo ngo doc thuc pham lop
1A** · 08/08/2026 **10:29**". Trong khi sự cố mới nhất thật là `INC-2026-005` lúc **13:35**.
Lấy phần tử đầu mảng mà **không sắp xếp**.

Cùng họ: `school-admin/incidents` liệt kê theo thứ tự **002 → 004 → 005 → 003**, không theo
thời gian cũng không theo mã.

## 🟡 P2-8 · Bốn kiểu ngày/giờ khác nhau trong cùng một sản phẩm

| Màn | Hiển thị |
|---|---|
| `school-admin/corrective-actions` | `2026-08-08T03:31:10.338454Z` — **ISO thô, micro-giây, giờ UTC** |
| `teacher/dashboard` (bảng phân phát) | `2026-08-08` |
| `school-admin/incidents` | `08/08/2026 10:29` ✅ |
| Ô lọc ngày ở `service-caterers/samples` | placeholder **`mm/dd/yyyy`** (định dạng Mỹ) |

Ca đầu tệ nhất: người dùng đọc "03:31" trong khi đồng hồ chỉ 10:31 — **lệch 7 tiếng** và lộ
định dạng máy. Cần một hàm format dùng chung, luôn theo giờ Việt Nam.

## 🟡 P2-9 · Mỗi lần tải trang bắn 7 request 401 rồi mới retry

Vào thẳng `school-admin/menu-approval` (F5): 7 request (`notifications`, `classrooms`, `menus`,
`dishes`, `menu-templates`, `students`, `allergy-declarations`) chạy **trước khi** token sẵn
sàng ⇒ **401 cả 7**, sau đó `refresh` 200 rồi **gọi lại đủ 7 lần nữa**.

Hệ quả: gấp đôi số request mỗi lần tải trang, và console luôn đỏ 7 dòng — làm **lỗi thật bị
chìm** trong lỗi giả.

Cùng chỗ này còn một vấn đề quy mô: FE tải **toàn bộ** danh sách về máy khách
(`students?pageSize=300`, `allergy-declarations?pageSize=500`, `dishes?pageSize=200`). Trường
thật vài nghìn học sinh sẽ gãy.

## 🟡 P2-10 · Bảng cuộn ngang và cắt chữ dù màn hình còn trống

- `supplier/dashboard` → bảng *Yêu cầu mua hàng*: cột GIAO hiện **"08/08/2..."** bị cắt, phải
  cuộn ngang — trong khi **nửa phải màn hình bỏ trống**.
- `school-admin/dashboard` → cả hai bảng *Thực đơn chờ phê duyệt* và *Đối tác* đều có thanh
  cuộn ngang dù chỉ 2 cột.
- `school-admin/dashboard` → card thứ tư tiêu đề bị cắt: **"Tỷ lệ kiểm tra bếp đạt (30 …"**.

## Ghi chú nhỏ khác (không xếp hạng)

- **Thanh tiến trình "Kho theo trạng thái lô"** vẽ *Đang sử dụng 2 lô* và *Cách ly 2 lô* đều
  **full-width 100%** — không theo tỉ lệ trên tổng 4 lô, nhìn như mỗi loại chiếm toàn bộ.
- **Lọt tiếng Anh giữa câu tiếng Việt:** *"chỉ **School Administration** được đóng sự cố"*
  (màn điều tra) — chỗ khác gọi vai này là "Ban Giám hiệu".
- **"đang biên soạn"** là nhãn của trạng thái `COMPILING` cho bản đồ truy xuất — nghe không
  giống ngôn ngữ nghiệp vụ ("đang tổng hợp"?).
- **Học sinh vừa tạo đã mang nhãn "Đã xác minh"** ở cột *Hồ sơ sức khỏe* dù chưa khai gì —
  cùng lớp lỗi "chưa khai ≠ đã xác minh" mà dự án đã chốt ở `NUTRITION_TARGET`.
- **"Tài liệu tuân thủ 0/0 — Tất cả còn hiệu lực"** (dashboard NCC): thông điệp sai khi rỗng.
- **Cột "TUẦN" chứa giá trị NGÀY** (`08/08/2026`) ở bảng thực đơn chờ duyệt của BGH.
- **Cột "CÒN LẠI / 24H" hiện `28h`** ở màn lưu mẫu — nhãn và giá trị mâu thuẫn.
- **Badge khu lưu trữ không đồng bộ:** "Lạnh"/"Đông lạnh" có badge, **"Khô" thì không**
  (`system-admin/ingredients`).
- **Vào `/login` khi đang đăng nhập** không chuyển hướng về dashboard, vẫn hiện form.
- **Console luôn có `404 /favicon.ico`** trên mọi trang.

## Kiểm soát dữ liệu — phát hiện ngoài lề nhưng đáng ghi

Nhận hàng **hai lần cùng một `supplierBatchId`** thì hệ thống **cộng dồn, không chặn**: lô NCC
khai 50 kg nhưng kho hiện **100 kg** (lộ ra do script seed chạy 2 lần). Cần chặn hoặc cảnh báo
khi số nhận vượt số lô NCC đã đăng ký.

## ✅ ĐÃ SỬA — 08/08/2026 (cùng ngày, sau khi rà)

Xác minh: `mvn test` **352 pass / BUILD SUCCESS** · `next build` sạch · `tsc --noEmit` sạch ·
compose rebuild rồi **đo lại bằng API thật** (kết quả ghi ở cột phải).

| # | Sửa ở đâu | Đo lại được gì |
|---|---|---|
| P0-1 | `medical-staff/incidents/[id]/page.tsx` — gom về `batchDispositionBadge()`, cả **hai** bản sao dùng chung | Lô `RETURNED_TO_SUPPLIER` hiện "Đã trả nhà cung cấp" trên hồ sơ đã đóng |
| P0-2 | **`components/app/auth-gate.tsx` (mới)** bọc toàn bộ `(app)` | Trước đây **không có cổng xác thực nào** — `layout.tsx` render thẳng `AppShell`. Nay `anonymous` ⇒ `/login?next=…` + Alert "Phiên làm việc đã kết thúc… Dữ liệu của bạn KHÔNG mất" |
| P0-3 | `lib/mock-data.ts` — thêm `HOLD: "Đang giữ để điều tra"` (badge `critical`) | Hết chuỗi enum trần ở màn Lưu mẫu |
| P1-4 | `InspectionAssembler` — bỏ `"Bếp trung tâm"` in cứng, `location` = `schools.address`; `ZoneOffset.UTC` → `Asia/Ho_Chi_Minh`; **bỏ `formHeader` khỏi `SubmitStepRequest`** | `{"location": "Thanh Đa, Phúc Thọ, Hà Nội", "checkedAt": "08/08/2026 13:34"}` — trước là `"Bếp trung tâm"` / `06:34` |
| P1-5 | BGH: bảng dùng `pendingMenus` (đúng bộ lọc của card) · NCC: "sắp tới" gồm cả `DISPATCHED/IN_TRANSIT/DELAYED` · Bếp: ô "Mẫu đang lưu" tính cả `HOLD` | Ba màn hết tự mâu thuẫn |
| P1-6 | 3 việc — xem mục riêng bên dưới | `GET /samples?incidentId=INC-2026-005` → **1 mẫu** (trước: 0) |
| P1-7 | `IncidentServiceImpl.list()` truyền `Sort.by(DESC, reportedAt)` tường minh | `INC-2026-005 → 004 → 003 → 002` (trước: 002 → 004 → 005 → 003) |
| P2-8 | 13 chỗ in timestamp thô nay qua `formatVnDateTime()` | Hết `2026-08-08T03:31:10.338454Z` ở màn CAPA |
| P2-9 | `AuthGate` chỉ mount `children` khi `authenticated` | Query không còn chạy trước khi có token ⇒ hết 7×401 mỗi lần tải trang |

### Ba lỗi GỐC lộ ra khi sửa (không nhìn thấy từ màn hình)

1. **Không hề có route guard.** P0-2 không phải "FE xử lý 401 chưa khéo" — `(app)/layout.tsx`
   render thẳng `AppShell`, `AppShell` render thẳng `children`. Store CÓ dọn phiên khi refresh
   token chết, nhưng **không ai đọc `status`**. Mọi màn trong `(app)` xưa nay vào được không
   cần phiên; sở dĩ chưa ai gặp là vì không có phiên thì API trả rỗng — đúng cái làm màn Kho
   nói "Không có bản ghi nào".
2. **`FoodSampleResponse.incidentId` trả UUID nội bộ** thay vì mã nghiệp vụ (vi phạm D1), và
   `FoodSampleSpec` so **chuỗi mã** với **cột UUID**. Nên `GET /samples?incidentId=INC-…`
   **luôn trả rỗng, im lặng** — đó mới là lý do hồ sơ đang giữ mẫu vẫn ghi "Chưa gửi mẫu nào",
   không phải lỗi hiển thị. Sửa cả hai chiều (assembler + resolve mã→UUID ở service), áp cho
   cả `sample-lab-requests`.
3. **`ListQuery.toPageable()` trả `Sort.unsorted()`** — Javadoc của nó đã ghi sẵn "chưa có
   sign-off của Nam". Đây chính là gốc của P1-7. **Đã chốt (a) + kèm luật**: giữ `unsorted()`
   làm mặc định (khôi phục mặc định `code` sẽ vỡ MF03 ngay), nhưng **mọi danh sách người dùng
   đọc theo thời gian phải tự truyền `Sort` ở call-site**. Ghi vào Javadoc `ListQuery`.

### P1-6 — ba mối nối, làm đủ cả ba

- **Giữ mẫu tại trường**: thêm dòng việc "Giữ mẫu tại trường để điều tra" gọi
  `POST /incidents/{code}/sample-hold`, danh sách mẫu đọc từ
  `GET /sample-lab-requests/retrievable` (server tự tính cửa sổ 24h BR-149). Trước đó muốn giữ
  mẫu thì y tế **buộc phải khai khống là đã bàn giao đi lab** — đúng cái B3 sinh ra để bỏ.
- **Hiện mẫu đang giữ**: câu tóm tắt tách "đang giữ tại trường" khỏi "đã gửi xét nghiệm";
  panel mẫu có nhánh riêng cho `HOLD`.
- **Tra suất ăn**: dòng việc "Tra suất ăn của học sinh bị ảnh hưởng" — mở từng em thì mới gọi
  `GET /students/{code}/meal-history` (`enabled`), tránh sự cố 30 em bắn 30 request.

### Ô nhập tay có ràng buộc → dropdown (yêu cầu thêm của Nam)

Rà toàn bộ FE đối chiếu với `CHECK` constraint trong migration. Hầu hết field enum **đã là**
`Select`; đúng **một** nhóm còn nhập tay, và nó là nhóm tệ nhất vì server validate ngầm:

| Màn | Trước | Sau |
|---|---|---|
| `system-admin/ingredients` — "Đơn vị tính *" | `<Input placeholder="kg, lít, quả, hộp…">` | `Select` từ `GET /measure-units` |
| `supplier/…/catalog` — "Đơn vị báo giá *" | `<Input placeholder="VD: kg, hoặc bó/thùng…">` | `Select` từ `GET /measure-units` |
| `menu-form` · `inspections/[id]` | `Select` nhưng đọc hằng `OFF_CATALOG_UNIT_OPTIONS` (**10** chuỗi khai tay) | `Select` từ `GET /measure-units` (**13** dòng thật) |

Backend đã validate đơn vị theo bảng `measure_units` (`MeasureUnitService.isValidUnit`) ⇒ gõ
"Kg"/"gram"/"bo" đều **422 lúc bấm Lưu** mà người dùng không có cách nào biết trước tập hợp
lệ. Thêm `lib/measure-units.ts` (`useMeasureUnits()`, cùng khuôn `useDietGroups()`) và **xoá**
`OFF_CATALOG_UNIT_OPTIONS`. Đây là lần thứ 4 của lớp lỗi "hằng số nhân đôi" sau
`NUTRITION_TARGET`, `menuLeadTimeDays`, `STANDARD_DIETS` — backlog D5-FE.

> Còn nợ: `toBaseQuantity()`/`UNIT_FACTOR` (4 hệ số khai tay) vẫn đồng bộ nên chưa chuyển được
> sang hook. Đã ghi cảnh báo tại chỗ + `useMeasureUnits().toBase()` sẵn sàng thay.

### Sửa kèm từ mục "Ghi chú nhỏ"

- Vào `/login` khi đang đăng nhập → đá về workspace (và về `?next=` nếu có).
- NCC: "Tài liệu tuân thủ 0/0 — Tất cả còn hiệu lực" → "Chưa nộp hồ sơ tuân thủ nào".
- BGH: cột **"TUẦN"** → **"Ngày áp dụng"** (thực đơn theo NGÀY từ 11/07/2026).
- `system-admin/ingredients`: `<select>` thô + class chép tay → `Select` dùng chung.

### CHƯA sửa (cố ý, cần quyết định)

- **P2-9 phần hai** — FE tải toàn bộ danh sách về client (`students?pageSize=300`,
  `allergy-declarations?pageSize=500`). Cơn bão 401 đã hết nhưng **kiến trúc phân trang** thì
  chưa; trường vài nghìn học sinh vẫn gãy. Việc riêng, không nhét vào đợt sửa lỗi.
- **P2-10 bảng cuộn ngang** — nguyên nhân đo được là **không có tầng trình bày chung**: 46 màn
  dùng `DataTable` (tự lo phân trang/skeleton/empty/cuộn), **47 màn** dùng thẳng primitives
  `ui/table` nên mỗi màn tự đặt độ rộng cột và câu "không có bản ghi", 2 màn còn dùng `<table>`
  thô. Cần bóc phần trình bày của `DataTable` thành `SimpleTable` (nhận sẵn `rows`) rồi chuyển
  dần — **đợi Nam duyệt**, không tự làm trong đợt này.
- **Nhận hàng hai lần cùng `supplierBatchId` thì cộng dồn** (50 kg khai → 100 kg trong kho).
  Là lỗi kiểm soát dữ liệu, thuộc MF06, cần chốt chặn hay chỉ cảnh báo.
- Thanh tiến trình "Kho theo trạng thái lô" vẽ sai tỉ lệ · "đang biên soạn" · học sinh mới tạo
  đã "Đã xác minh" · cột "CÒN LẠI / 24H" hiện `28h` · `404 /favicon.ico`.

## Việc nên làm theo thứ tự

~~Danh sách gốc (P0-1 → P1-5/P1-7/P2-8) đã làm hết trong cùng ngày~~ — xem mục
**"✅ ĐÃ SỬA"** phía trên. Còn lại theo thứ tự:

1. **Tầng bảng dùng chung** — bóc `SimpleTable` ra khỏi `DataTable`, chuyển dần 47 màn đang
   dùng thẳng primitives. Gỡ được P2-10 và cả lớp "mỗi màn một câu empty state".
2. **Phân trang thật ở FE** — bỏ `pageSize=300/500`, dùng phân trang server (P2-9 phần hai).
3. **MF06 — chặn nhận vượt số lô NCC đã đăng ký** (nhận 2 lần thì cộng dồn).
4. Nhóm ghi chú nhỏ còn lại (tỉ lệ thanh tiến trình, "đang biên soạn", `28h`, favicon).
