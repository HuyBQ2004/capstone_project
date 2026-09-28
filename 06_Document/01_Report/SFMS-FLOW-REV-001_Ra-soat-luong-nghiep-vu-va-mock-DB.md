# SFMS — Rà soát luồng nghiệp vụ, máy trạng thái và mock database

| Trường | Giá trị |
|---|---|
| **Document ID** | SFMS-FLOW-REV-001 |
| **Phiên bản** | 3.0 |
| **Ngày** | 22/07/2026 |
| **Người thực hiện** | Nguyễn Văn Nam |
| **Phạm vi** | Toàn bộ bản mock FE (`03_Design/08_FrontEnd/Web`) — 50 resource, 384 bản ghi, 121 trang |
| **Tài liệu liên quan** | SFMS-DATA-IMP-001 (Mô hình dữ liệu cho Backend) |

**Thay đổi qua các bản:** v1.0 kết luận "mô hình đã chốt được nhưng còn 6 tồn đọng ở tầng
luồng". v2.0 ghi lại kết quả sau khi xử lý toàn bộ 6 tồn đọng đó cùng 4 lỗi mới phát hiện.
v3.0 bổ sung **§9 — rà soát UI hai khu School Admin và System Admin**, phát hiện một lớp
lỗi khác hẳn: màn hình hoàn chỉnh về giao diện nhưng thao tác trên đó không đi tới đâu.

---

## 1. Mục đích và phương pháp

Tài liệu trả lời ba câu hỏi trước khi chốt schema cho backend Spring Boot:

1. Các luồng nghiệp vụ đã chạy thông từ đầu đến cuối chưa?
2. Máy trạng thái của từng thực thể đã đầy đủ đường đi chưa?
3. Mock database đã đủ đúng để làm đầu vào cho class diagram chưa?

**Phương pháp — không đánh giá bằng cách đọc code.** Mọi khẳng định được kiểm chứng bằng
script nạp trực tiếp `mock-data.ts` qua `jiti` rồi gọi chính các hàm nghiệp vụ mà màn hình
đang gọi (`personalizeBlock`, `mealServingGate`, `allocateFEFO`, `unresolvedAllergyDishes`,
`derivedMealPlans`…). Cách này bắt được đúng loại lỗi mà đọc code không thấy: dữ liệu hợp
lệ về kiểu nhưng vô nghĩa về nghiệp vụ.

Bốn kỹ thuật rà chính:

| Kỹ thuật | Bắt được gì |
|---|---|
| Quét toàn vẹn tham chiếu (mọi mã `HS-`/`MENU-`/`INC-`/`BATCH-`… đối chiếu bản ghi thật) | Khoá ngoại treo |
| Đối chiếu resource được đăng ký ↔ resource được đọc/ghi | Bảng chết, luồng không có người ghi |
| Đối chiếu trạng thái có trong DB ↔ trạng thái màn hình sinh ra được | Trạng thái không có đường vào, mâu thuẫn chéo |
| Chiếu từng học sinh qua từng thực đơn bằng chính hàm cá nhân hoá | Sai lệch giữa cái hệ thống *báo* và cái học sinh *nhận* |

---

## 2. Kết luận

**Mô hình dữ liệu và các luồng nghiệp vụ đã sẵn sàng để chốt schema.**

- Nợ mô hình: **0/8** còn lại (SFMS-DATA-IMP-001 §17.1).
- Tồn đọng luồng của v1.0: **0/6** còn lại.
- Bộ kiểm chứng cuối: **19/19 mục đạt** (§7).

Đợt rà phát hiện và sửa **13 lỗi**, trong đó 1 lỗi an toàn thực phẩm, 4 lỗi làm đứt luồng
và 4 trạng thái nghiệp vụ không thao tác được.

Hai quyết định cho backend nêu ở §8.

---

## 3. Đánh giá từng luồng nghiệp vụ

Toàn bộ 14 luồng chạy thông. Cột cuối ghi việc đã làm trong đợt này.

| # | Luồng | Chuỗi vai | Đã sửa gì |
|---|---|---|---|
| 1 | **Khai báo dị ứng** | Phụ huynh → Y tế | Khai báo trước đây không được lưu; xác minh không chạm tới khai báo. Nay lưu thật, xác minh theo từng tác nhân |
| 2 | **Khai báo bệnh lý nền** | Phụ huynh → Y tế | Thiếu `studentId`, không được lưu, và **không màn nào xác minh**. Nay đủ cả ba |
| 3 | **Bữa ăn đặc biệt** | Phụ huynh → Y tế → Đơn vị suất ăn → Giáo viên | Em ăn chay và em Halal đang được giao thẳng món thịt. Nay mọi thực đơn đã công bố đều có suất hợp chế độ ăn |
| 4 | **Lập & duyệt thực đơn** | Đơn vị suất ăn → Ban giám hiệu | Gộp `APPROVED`/`PUBLISHED` về một trạng thái; `allergyConflicts` chuyển thành dẫn xuất |
| 5 | **Mua nguyên liệu** | Đơn vị suất ăn ↔ Nhà cung cấp | Không sửa — `PENDING → QUOTED → PRICE_ACCEPTED/QUOTE_REVISION → APPROVED → COMPLETED` vốn đã đủ |
| 6 | **Nhập kho** | Nhà cung cấp → Đơn vị suất ăn | Đọc đối tác từ db thay vì mảng tĩnh; phiếu nhận ghi trạng thái thật của đối tác |
| 7 | **Xuất kho nấu ăn** | Đơn vị suất ăn | Không sửa — gộp thực đơn mọi khối, nổ BOM, FEFO một lần rồi chia ngược |
| 8 | **Kiểm thực ba bước** | Đơn vị suất ăn → Y tế | Mỗi bữa đã công bố nay đều có phiên; nhánh CHẶN của BR-094 chạy được trên dữ liệu thật |
| 9 | **Lưu mẫu** | Đơn vị suất ăn → Y tế → CDC | Bữa dùng để truy xuất vốn không có mẫu nào |
| 10 | **Phiên điều tra sự cố** | Giáo viên/Phụ huynh → Y tế → Ban giám hiệu | Cách ly lô nay chạm tới kho thật, không chỉ ghi vào hồ sơ sự cố |
| 11 | **CAPA** | Y tế/Kiểm thực → Ban giám hiệu → Đơn vị suất ăn → Y tế | Gỡ hai mâu thuẫn trạng thái sự cố ↔ CAPA |
| 12 | **Quản lý nhân viên** | Ban giám hiệu | Nhân viên `PENDING` nay kích hoạt được |
| 13 | **Quản lý lớp & năm học** | Ban giám hiệu | Không sửa — `DRAFT → ACTIVE → CLOSED` đủ |
| 14 | **Phân phát suất ăn** | Giáo viên → Phụ huynh | Chốt suất thực tế lúc trao tay; chặn khi không tra ra thực đơn |
| 15 | **Quản lý đối tác** | Ban giám hiệu → NCC | Đình chỉ/khôi phục kèm lý do, kéo theo khoá tài khoản của đối tác |

### 3.1. Chuỗi truy xuất mẫu

Bữa trưa **06/07/2026** là chuỗi hoàn chỉnh dùng để nghiệm thu:

| Mắt xích | Dữ liệu |
|---|---|
| Thực đơn | 3 thực đơn đã công bố — `MENU-2026-27` (Khối 3) · `27C` (Khối 1) · `27D` (Khối 5) |
| Kiểm thực | Phiên ba bước đạt ⇒ cổng BR-094 cho phục vụ |
| Xuất kho | Phân bổ FEFO gộp cả ba khối trong một lần |
| Phát suất | 13 suất (3A + 3B), 4 em đã nhận với suất đóng băng |
| Mẫu lưu | 4 mẫu — phủ đủ 4/4 món của bữa |

Bữa trưa **07/07/2026** là chuỗi đối chứng: Bước 2 kiểm thực không đạt (nhiệt lõi 68°C <
75°C) ⇒ 10 suất đều ở trạng thái chưa phát. Nhánh chặn của BR-094 nay chạy trên dữ liệu
thật chứ không chỉ tồn tại trong code.

---

## 4. Máy trạng thái

### 4.1. Bốn trạng thái nay đã có đường vào

Trước đợt này, bốn trạng thái tồn tại trong dữ liệu nhưng **không màn nào tạo ra được** —
nghiệp vụ có quy tắc mà không thi hành được.

| Thực thể | Trạng thái | Đã bổ sung |
|---|---|---|
| `partners` | `SUSPENDED` | Ban giám hiệu đình chỉ/khôi phục kèm lý do bắt buộc (`suspendReason`) |
| `inventory` | cách ly | Màn điều tra khoá/nhả lô thật; FEFO loại đúng lô bị cách ly |
| `users` | `SUSPENDED` | Đi theo quyết định đình chỉ đối tác qua `partnerId` |
| `staff` | `PENDING → ACTIVE` | Kích hoạt khi đã xác minh email (BR-010) |

**Đình chỉ mới có ý nghĩa nếu nó chặn được.** Màn nhận hàng vốn có kiểm tra BR-070 nhưng
đọc mảng tĩnh nên đình chỉ trong phiên làm việc không tới nơi — đã chuyển sang đọc db. Phiếu
nhận cũng ghi trạng thái **thật** của đối tác lúc nhận thay vì mặc định `"APPROVED"`; phiếu
nhận là chứng từ truy xuất, khai sai thì sau này không biết lô hàng vào kho trong tình trạng nào.

### 4.2. Hai chỗ một cột gánh hai khái niệm

| Chỗ | Vấn đề | Cách sửa |
|---|---|---|
| `InventoryBatch.status` | Chứa cả hạn dùng (`ACTIVE`/`EXPIRING_SOON`/`EXPIRED` — **dữ kiện**) lẫn `QUARANTINED` (**quyết định** của người điều tra). Cách ly một lô đang `EXPIRING_SOON` là ghi đè mất hạn; nhả cách ly xong không biết trả về đâu | Tách `quarantine` thành trường riêng |
| `Menu.status` | Có cả `APPROVED` lẫn `PUBLISHED`, mọi nơi tiêu thụ lọc cả hai như nhau — hai tên cho cùng một trạng thái. Màn duyệt chỉ sinh `PUBLISHED`, không có bước công bố riêng | Gộp về `PUBLISHED`, thay 6 chỗ lọc thủ công bằng `isMenuLive()` |

Trường hợp `users` thì ngược lại: `SUSPENDED` và `INACTIVE` **đúng là hai khái niệm khác
nhau** nhưng không gì phân biệt. Nay `SUSPENDED` là khoá tạm đi theo đình chỉ đối tác,
`INACTIVE` là ngừng hẳn do quản trị.

### 4.3. Mâu thuẫn trạng thái chéo thực thể

| Chỗ mâu thuẫn | Trước | Sau |
|---|---|---|
| `INC-2026-014` | Ghi "CAPA đã duyệt" trong khi CAPA vẫn `PENDING_ADMIN_APPROVAL` | CAPA → `APPROVED_FOR_IMPLEMENTATION` |
| `INC-2026-013` | Ở trạng thái ĐÃ ĐÓNG trong khi CAPA mới `SUBMITTED_EVIDENCE` — màn đóng sự cố đòi mọi CAPA `COMPLETED`, tức **trạng thái này không thể đạt được qua giao diện** | Sự cố → `CAPA_APPROVED` |

---

## 5. Lỗi phát hiện và đã sửa

### 5.1. Hai mô hình dị ứng cấp cho hai luồng khác nhau — **lỗi an toàn thực phẩm**

Màn duyệt thực đơn đọc `Student.allergies` + `healthStatus`; màn phát suất đọc
`PARENT_ALLERGIES` — bảng chỉ có 2 dòng cho 1 em trong khi 8 em có dị ứng. Chiếu bữa trưa
06/07 Khối 3 qua từng em:

| Học sinh | Bên duyệt thực đơn thấy | Bên phát suất thấy | Em thực nhận |
|---|---|---|---|
| Ngô Khánh Vy | `["Trứng"]` | `[]` | **Thịt kho trứng cút** |
| Trịnh Bảo Ngọc | `["Hải sản"]` | `[]` | **Canh bí đỏ nấu tôm** |

Ban giám hiệu được báo "đã xử lý xung đột dị ứng", còn em thì nhận đúng món chứa tác nhân
của mình.

**Đã sửa:** `ALLERGY_DECLARATIONS` là nguồn duy nhất; `Student.allergies` thành trường dẫn
xuất; xác minh chuyển về mức **từng tác nhân** theo BR-031 — `healthStatus` ở mức học sinh
khiến em có 3 tác nhân mới xác minh 1 thì cả 3 bị coi cùng trạng thái.

### 5.2. Gộp xong thì lộ tiếp hai lỗi nghiệp vụ nằm sẵn trong seed

| Lỗi | Chi tiết |
|---|---|
| Thực đơn đã công bố mà còn vướng dị ứng | `MENU-2026-27` và `MENU-2026-26` đã công bố/đã duyệt nhưng vẫn còn món chạm dị ứng đã xác minh, không có suất riêng — vi phạm BR-063 |
| `Menu.allergyConflicts` khai tay và sai | Sai ở **cả 4** thực đơn có xung đột (27 ghi 0 trong khi còn 1; 28 ghi 1 trong khi có 2; 25 ghi 3 trong khi còn 1). Ban giám hiệu duyệt dựa trên chính con số này ⇒ chuyển thành dẫn xuất |

### 5.3. Suất theo chế độ ăn có chỗ khai nhưng bỏ trống

Em ăn chay và em Halal được giao thẳng món thịt trên thực đơn **đã công bố**. Mô hình
không sai — chỗ khai suất riêng vẫn có — nhưng dữ liệu để trống nên không ai thấy.

Sửa xong lộ ra một giới hạn của công cụ: helper seed `dish()` chỉ nhận **một** suất riêng
cho mỗi món, trong khi món thịt cần đồng thời suất dị ứng, suất chay và suất Halal. Đã mở
rộng nhận mảng, rồi bổ sung suất cho 3 thực đơn đã công bố.

### 5.4. Sổ phát suất không neo vào thực đơn nào

Seed ghi ngày 03/07 — **ngày không có thực đơn nào**. Nhãn suất là chuỗi gõ tay không khớp
món nào: **10/10 dòng tra ngược không ra thực đơn**. Nặng nhất là màn phát suất tra thực
đơn theo (ngày, khối) không ra gì, nên bấm "Xác nhận đã phục vụ" sẽ đóng băng danh sách
**rỗng** — hỏng đúng cơ chế sinh ra để chống lịch sử bị viết lại.

### 5.5. Khai báo dị ứng và bệnh lý nền không được lưu

Cả hai màn khai của Phụ huynh giữ danh sách trong `useState`: khai xong tải lại trang là
mất, Y tế không bao giờ nhìn thấy. Riêng bệnh lý nền còn **thiếu `studentId`** nên phụ
huynh nhiều con thấy chung một danh sách cho mọi đứa, và **không màn nào xác minh** nên
khai báo đứng mãi ở `PENDING`.

### 5.6. Cổng BR-094 chỉ chạy được một trong ba nhánh

3/4 bữa đã công bố không có phiên kiểm thực nào ⇒ cổng rơi vào nhánh "không tìm thấy
phiếu", cảnh báo suông thay vì kết luận được. Nhánh CHẶN chưa từng chạy trên bữa nào đã
công bố. Đã thêm phiên cho cả ba ngày còn thiếu, trong đó 07/07 để Bước 2 không đạt.

### 5.7. Các lỗi toàn vẹn tham chiếu

| Lỗi | Chi tiết |
|---|---|
| Học sinh không tồn tại | `RPT-2026-033` ghi "Vũ Hải Đăng" — không có trong `STUDENTS` |
| Con thứ hai không có hồ sơ | `PARENT_CHILDREN` có `HS-1B-007` nhưng `STUDENTS` không có ⇒ phụ huynh đổi con là mọi màn rỗng trắng |
| Số học sinh ảnh hưởng không khớp | `INC-2026-014` khai 4 HS nhưng chỉ gộp 1 báo cáo có 2 HS |
| Khối không có thực đơn | Khối 1 chỉ có `DRAFT`, Khối 5 chỉ có `REJECTED` ⇒ 4 học sinh không có bữa nào đã công bố |
| Bữa truy xuất không có mẫu lưu | Mẫu dừng ở 02/07 trong khi bữa có đủ chuỗi truy xuất là 06/07 |

---

## 6. Đánh giá mock database và màn hình

### 6.1. Mock database

| Tiêu chí | Kết quả |
|---|---|
| Toàn vẹn tham chiếu | 0 khoá ngoại treo trên toàn bộ seed |
| Một nguồn sự thật | 0 cặp bảng trùng khái niệm |
| Bảng chết | 0 resource được đăng ký mà không màn nào đọc |
| Luồng không có người ghi | 0 |
| Trạng thái mâu thuẫn chéo | 0 |
| Trạng thái không có đường vào | 0 |
| Trường khai tay đáng lẽ dẫn xuất | 0 |

Quy mô: 46 resource · 377 bản ghi · 19 học sinh · 11 thực đơn · 29 món · 10 khai báo dị
ứng · 23 suất phát · 20 lô tồn kho · 9 mẫu lưu · 10 phiên kiểm thực · 3 sự cố · 5 CAPA.

**Ba nguyên tắc mà seed hiện tuân thủ** — đề nghị giữ khi làm backend:

1. **Dẫn xuất thay vì lưu.** Nếu tính lại sau 6 tháng vẫn ra cùng kết quả từ dữ liệu gốc
   thì dẫn xuất; nếu ra kết quả khác thì bắt buộc phải lưu. Suất ăn thực tế lúc phát thuộc
   loại thứ hai nên được đóng băng; số xung đột dị ứng thuộc loại thứ nhất nên được tính.
2. **Khoá thay vì tên chuỗi.** Mọi liên kết đi qua mã, không qua so tên.
3. **Trạng thái phải có đường vào.** Trạng thái nào giao diện không tạo ra được thì hoặc
   thiếu chức năng, hoặc thừa trạng thái — không có khả năng thứ ba.

### 6.2. Màn hình

Xây dựng: `tsc` sạch · 121 trang build thành công · 14 cảnh báo lint (đều là cảnh báo phụ
thuộc hook, không phải lỗi).

| Tiêu chí | Kết quả |
|---|---|
| Màn báo thành công mà không ghi gì vào DB | Còn 23 màn, **đều là thao tác xuất báo cáo/tải file**. 2 màn lưu dữ liệu thật đã được nối |
| Màn hiển thị dữ liệu bịa | 0 |
| Trạng thái nghiệp vụ không thao tác được | 0 |
| Liên kết chéo giữa các vai | Đủ — thông báo có người nhận, hộp thư lọc theo vai |

---

## 7. Bộ kiểm chứng

Chạy trên seed sau khi sửa — **19/19 đạt**:

| # | Khẳng định | Kết quả |
|---|---|---|
| 1 | Không tham chiếu treo | 0 |
| 2 | Không học sinh nào nhận món chứa tác nhân của chính mình | 0 |
| 3 | Nhãn suất khớp kết quả cá nhân hoá | 0 lệch |
| 4 | Không suất nào được phát trong bữa bị chặn (BR-094) | 0 |
| 5 | Không suất nào trái chế độ ăn trên thực đơn đã công bố | 0 |
| 6 | Không thực đơn đã công bố nào còn vướng dị ứng (BR-063) | 0 |
| 7 | Số xung đột dị ứng khớp thực tế | khớp |
| 8 | `Menu` chỉ còn một trạng thái hiệu lực | đạt |
| 9 | Mọi bữa đã công bố đều có phiên kiểm thực | đạt |
| 10 | Nhánh CHẶN của BR-094 chạy được trên dữ liệu thật | 1 bữa |
| 11 | Cách ly tách khỏi hạn dùng | đạt |
| 12–13 | FEFO loại đúng 2 lô bị cách ly | đạt |
| 14 | Đối tác bị đình chỉ đều có lý do | đạt |
| 15 | Tài khoản `SUSPENDED` đều gắn đúng đối tác | đạt |
| 16–18 | Cả 3 sự cố đóng đúng luật (CAPA hoàn tất mới đóng được) | đạt |
| 19 | Mẫu lưu phủ đủ 4/4 món của bữa truy xuất | đạt |

### 7.1. Tầng kiểm tra thứ ba — ràng buộc ngữ nghĩa (22/07/2026)

Ba tầng kiểm tra, mỗi tầng bắt một loại lỗi mà tầng trên không thấy:

| Tầng | Công cụ | Bắt được gì |
|---|---|---|
| 1 | `tsc --noEmit` | Sai kiểu dữ liệu |
| 2 | `findIsolatedNameFields()` | Bản ghi nối bằng **tên** thay vì bằng khoá |
| 3 | `npm run audit:data` | Khoá nối **đúng bảng nhưng sai dòng** |

Tầng 3 là loại khó thấy nhất: khoá tồn tại, trỏ đúng bảng, màn hình vẫn render bình
thường, không lỗi nào báo ra — chỉ có nghiệp vụ là sai. **46 luật, đã chạy sạch.**

Đợt chạy đầu tiên phát hiện 22 vi phạm, trong đó **17 là lỗi thật**:

| Lỗi | Số | Bản chất |
|---|---|---|
| Phiếu xuất trỏ lô sai nguyên liệu | 4 | Phiếu ghi "Bí đỏ" trỏ lô **muối i-ốt**; phiếu "Cá basa" trỏ lô **nước mắm** |
| Nấu bằng lô đã hết hạn | 3+3 | Bữa 06–07/07 dùng lô thịt hết hạn 03/07 và lô su su hết hạn 04/07 |
| Thực đơn đã công bố không có người duyệt | 3 | `PUBLISHED` mà `approvedBy` trống |
| Mẫu lưu không nối ngược về phiếu bàn giao | 2 | Phiếu liệt kê mẫu, mẫu không biết mình đã bị mang đi |
| Phiếu bàn giao trỏ mẫu của bữa khác | 2 | Sự cố bữa 30/06 nhưng gửi xét nghiệm mẫu lấy ngày 02/07 — **sau** sự cố hai ngày |
| Lô không có phiên nhận hàng | 14 | Lô "tự xuất hiện trong kho"; truy xuất BR-076 dừng lại, không biết ai giao |
| Nhận hàng sau khi NCC mất chứng nhận | 2 | Lô cá/tôm của Đại Dương ghi ngày nhận **sau** ngày chứng nhận ATTP hết hiệu lực (BR-070) |

Và **5 là dương tính giả của luật, không phải lỗi dữ liệu** — đã sửa lại luật:

- Khai dị ứng đã xác minh mà không có tệp minh chứng: BR-031 chỉ bắt buộc tài liệu với
  mức **Nặng**; cả 5 dòng đều mức Trung bình.
- Thực đơn vượt trần chi phí ở trạng thái `PENDING_APPROVAL`: đó **chính là** ca School
  Admin phải thấy cảnh báo BR-080. Luật sửa thành "đã duyệt mà vẫn vượt trần".

**Nguyên tắc đọc kết quả:** mỗi luật đếm số dòng thực sự kiểm được. Luật kiểm 0 dòng là
luật **hỏng** (thường do đổi tên trường), không phải luật sạch — script thoát lỗi trong
cả hai trường hợp. Lần chạy đầu, 8 luật im lặng vì đoán sai tên trường; nếu không có bộ
đếm này thì đã kết luận nhầm là dữ liệu sạch.

Kèm theo, một bảng tra cứng ở màn điều tra sự cố đã bị bỏ: nó ghi `BATCH-GF-4411` là
"Rau củ tổng hợp" (thực tế là thịt lợn nạc vai) và gán `BATCH-DD-1080` cho phiếu nhận
`RCV-20260630-02` — phiếu đó nhận lô khác và **đã bị từ chối**. Nay thay bằng
`batchOriginOf()` đọc thẳng từ kho và phiên nhận.

---

## 8. Hai quyết định cho backend

1. **`Menu` chỉ nên có một trạng thái hiệu lực.** Bản mock đã gộp về `PUBLISHED`. Nếu sau
   này thật sự cần bước công bố riêng cho phụ huynh (duyệt xong nhưng chưa cho xem), thì
   phải có màn thực hiện bước `APPROVED → PUBLISHED` — đừng thêm trạng thái không ai chuyển được.

2. **`InventoryBatch` không nên lưu trạng thái hạn dùng.** Hạn dùng suy được từ `expiry_date`
   (`batchShelfStatus`); cách ly là bảng/cột riêng. Seed giữ giá trị cố định vì mốc thời
   gian của seed neo quanh 06–07/2026, backend nên tính tại chỗ.

### 8.1. Ràng buộc phải đặt ở tầng database hoặc service

Không thể trông vào kỷ luật nhập liệu — đây đều là những chỗ seed đã từng sai:

- Sự cố chỉ đóng được khi mọi CAPA của nó đã hoàn tất.
- Thực đơn chỉ công bố được khi không còn món chạm dị ứng đã xác minh mà chưa có suất riêng.
- Bản ghi phát suất phải trỏ tới một thực đơn có thật của đúng (ngày, khối, bữa).
- Không ghi nhận phát suất cho bữa chưa có phiên kiểm thực đạt.
- Mọi khai báo dị ứng/bệnh lý nền phải có `student_id`.
- Đối tác bị đình chỉ thì tài khoản của họ không đăng nhập được, và không nhận được đơn mới.

---

## 9. Rà soát UI — School Admin & System Admin

### 9.1. Điều hướng và bố cục: không có vấn đề

33 màn School Admin + 14 màn System Admin. Kiểm tra khả năng tiếp cận (nav hoặc link vào)
và bộ lọc:

| Tiêu chí | Kết quả |
|---|---|
| Màn không có đường tới | 0 |
| Nav phủ thiếu | 0 |
| Bộ lọc không lọc gì | 1 (đã sửa) |
| Màn hiển thị dữ liệu bịa | 0 |
| Màn thiếu trạng thái chờ đáng kể | 0 |

### 9.2. Lớp lỗi thật: màn hoàn chỉnh nhưng thao tác không đi tới đâu

Năm màn giữ dữ liệu trong `useState` rồi `setTimeout` báo lưu thành công. Nhưng vấn đề
nặng hơn "không lưu": **phần lớn tham số mà các màn này khai là cấu hình được thì nơi
dùng đang tự khai lại một bản riêng**. Nghĩa là màn cấu hình về mặt chức năng là trang trí.

| Màn | Tham số | Nơi dùng đang làm gì | Đã sửa |
|---|---|---|---|
| Cấu hình trường (UC-095) | Trần chi phí (BR-080) | Chép cứng `30000` vào từng thực đơn lúc tạo ⇒ đổi trần không thực đơn nào đổi theo | Thực đơn lấy từ `school-config`, đóng băng vào bản ghi lúc lưu |
| Cấu hình trường | Bán kính geofence (BR-028) | Màn nhận hàng đọc hằng số `CURRENT_SCHOOL` | Đọc từ `school-config` |
| Chính sách bảo mật (UC-184) | Độ mạnh mật khẩu (BR-006) | Màn đặt lại mật khẩu hardcode "Tối thiểu 8 ký tự" ⇒ đặt 12 vẫn cho qua ở 8 | Dựng danh sách quy tắc từ chính sách |
| Chính sách bảo mật | Khoá sau N lần sai (BR-002) | Chỉ tồn tại trong một dòng chú thích ở màn đăng nhập | Đếm lần sai, báo còn mấy lần, khoá đúng số phút cấu hình |
| Tích hợp ngoài (UC-040) | Trạng thái kết nối (BR-163) | Màn Trạng thái hệ thống tự khai một danh sách khác, nói ngược về Gemini | Một nguồn `integrations`; gợi ý AI của bếp bị chặn khi chưa kết nối |
| Bảng tuân thủ (UC-027) | Chữ ký xác nhận an toàn ngày | `useState(true)` — tải lại là mất, thanh tra hỏi không có gì đưa ra | `SafetySignoff` kèm người ký, mốc ký, và chụp lại căn cứ tại thời điểm ký |
| Thống kê sử dụng (UC-150) | Bộ lọc ngày/vai trò | Chỉ đổi nhãn: chọn "30 ngày" vẫn 7 cột, chọn vai trò thì số liệu đứng yên | Số liệu tách theo vai, đủ 30 ngày, neo vào ngày chạy |

Hai mâu thuẫn đáng chú ý mà lớp lỗi này che giấu:

- **Gemini**: màn Tích hợp ghi "Chưa kết nối", màn Trạng thái hệ thống ghi "Chậm — 2.1s"
  (tức đang chạy). Quản trị viên mở hai màn cạnh nhau thấy hai sự thật. Trong khi đó bếp
  vẫn bấm ra được đề xuất thực đơn AI.
- **Trần chi phí**: Ban giám hiệu duyệt thực đơn dựa trên cột `costCeiling` của chính thực
  đơn đó, còn màn cấu hình thì khai đây là tham số cấp trường. Hai chỗ không nối với nhau.

### 9.3. Cố ý giữ nguyên

Bốn màn giám sát hạ tầng (`monitoring`, `system-status` phần nội bộ, `backup`, và phần
dung lượng/API của `usage-statistics`) dùng số cố định. Đây là dữ liệu health-check của
chính hệ thống, không suy được từ dữ liệu nghiệp vụ — bản thật lấy từ API hạ tầng.

### 9.4. Bài học cho backend

Ba tham số cấu hình bị sao chép xuống nơi dùng (`Menu.costCeiling`, quy tắc mật khẩu,
danh sách dịch vụ) là cùng một lỗi với `Student.allergies` ở §5.1: **một khái niệm, hai
bản ghi, không cơ chế đồng bộ**. Quy tắc phân biệt:

- Tham số **hiện hành** (chính sách mật khẩu, trạng thái kết nối) ⇒ nơi dùng đọc thẳng
  bảng cấu hình, không sao chép.
- Tham số **tại thời điểm ra quyết định** (trần chi phí của thực đơn đã duyệt, điểm vệ
  sinh lúc ký xác nhận) ⇒ đóng băng vào bản ghi, vì tính lại sau 6 tháng sẽ ra kết quả khác.
