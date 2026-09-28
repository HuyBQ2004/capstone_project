# Đề xuất Hạng mục Checklist — Kitchen Safety and Sanitation Checklist (UC-014)

> **Mục đích**: hỗ trợ quyết định các hạng mục cụ thể cho checklist kiểm tra bếp của Medical Staff (US-06/US-07, `MedicalStaff_UserStory.md`), dựa trên đối chiếu với văn bản pháp luật hiện có trong `00_Policies/`. Đây là **tài liệu tham khảo/đề xuất**, chưa phải checklist chính thức — cần đội dự án rà soát trước khi đưa vào Screen Spec `12_ConductFoodSafetyInspectionScreen.md`.
> **Nguồn đối chiếu**: `00_Policies/1246_QD-BYT...md` (Điều 3–8), `00_Policies/55_2010_QH12...md` (Điều 19–30), `00_Policies/13_2016_TTLT-BYT-BGDDT...md` (Điều 5, 6, 8, Phụ lục 03/04 Mục IV), `00_Policies/15_2018_ND-CP...md` (Điều 24, 26, 33), `02_Requirements/00_BusinessRules/business_rules.md` (BR-101–104, BR-110, BR-207).
> **Ngày biên soạn**: 04/07/2026. **Cập nhật 05/07/2026**: bổ sung mục D4 cũ (nhiệt độ tủ lưu mẫu, BR-110). **Cập nhật 06/07/2026 (đợt này)**: thay thế toàn bộ nội dung checklist theo bản "Safety Canteen Audit Checklist" do người dùng cung cấp (06/07/2026), đồng thời chuyển mô hình checklist sang **dynamic** — xem Mục 0.

> ⚠️ **File này trước đó bị cắt ngang giữa dòng ở mục Nhóm E (Nguồn nước, dòng 61/61)** — cùng dạng lỗi ngắt/truncate đã ghi nhận ở `MedicalStaff_UserStory.md` (xem `review_screen_medical_staff.md` §6.2, mục 7). Không xác định được nội dung bị mất phía sau E1 trong bản cũ vì file kết thúc đúng tại đó (`wc -l` = 61 dòng). Bản thay thế dưới đây được viết lại từ đầu theo checklist mới do người dùng cung cấp; các mục tương ứng của bản cũ (Nhóm A–E) được đối chiếu lại ở Mục 4.

---

## 0. Bối cảnh cập nhật 06/07/2026 — Checklist chuyển sang mô hình "dynamic"

Theo quyết định của người dùng (06/07/2026), checklist kiểm tra bếp (UC-014) không còn là danh sách hạng mục cố định do đội dự án/System Admin quyết định một lần, mà là **dữ liệu có thể chỉnh sửa trực tiếp bởi Medical Staff**, bao gồm cả tên nhóm, tên hạng mục, và nội dung chú thích căn cứ pháp lý (icon "ℹ"). Cụ thể, 4 quyết định đã chốt:

1. **Quyền chỉnh sửa**: Medical Staff tự sửa trực tiếp (tên nhóm, tên hạng mục, chú thích), **không cần** School Administration hay System Admin phê duyệt.
2. **Phạm vi thao tác**: đầy đủ CRUD — thêm/xóa/sắp xếp lại cả nhóm (group) và từng hạng mục (item) trong checklist, không giới hạn ở việc sửa nội dung chú thích như đề xuất US-07b ban đầu.
3. **Nguồn chú thích pháp lý**: không chờ người dùng cung cấp trích dẫn — đội triển khai (tôi) tự đối chiếu với `00_Policies/` hiện có, ghi rõ nguồn khi khớp được, và đánh dấu **`[cần xác nhận thêm]`** ở các hạng mục chưa tìm thấy văn bản gốc tương ứng trong `00_Policies/`, tuyệt đối không suy diễn/tự đặt ra căn cứ pháp lý không có thật.
4. **Hạng mục thuộc phạm vi module khác** (ví dụ: Sổ kiểm thực 3 bước và lưu mẫu — thuộc Food Sample Retention, UC-079/UC-076/UC-118): **giữ nguyên** trong checklist UC-014, có ghi chú rõ đây là mục **kiểm tra chéo (cross-check)** trong lúc audit bếp tổng quát, không phải quy tắc nghiệp vụ mới của UC-014 — tiếp nối đúng mẫu đã áp dụng cho mục D4/BR-110 ở bản trước.

> **Hệ quả kiến trúc (chưa thực hiện — xem Mục 5)**: quyết định #1 và #2 ở trên **mâu thuẫn trực tiếp** với BR-207 hiện hành (`business_rules.md` dòng 462: *"...do **System Admin** cấu hình và chỉnh sửa; áp dụng chung cho mọi trường sử dụng hệ thống"*). BR-207 cần được sửa đổi chính thức, và có thể cần một Business Rule/Use Case mới để mô tả quyền CRUD của Medical Staff. Việc này **chưa được thực hiện trong lần cập nhật này** — xem danh sách việc cần làm ở Mục 5 và câu hỏi xác nhận cuối tài liệu.

Vì checklist giờ là dữ liệu **có thể sửa được tại runtime**, nội dung 6 nhóm dưới đây đóng vai trò **dữ liệu khởi tạo mặc định (default seed data)** khi hệ thống triển khai lần đầu, không còn là "đặc tả cứng" — Medical Staff có thể điều chỉnh sau khi hệ thống vận hành.

---

## 1. Nguyên tắc áp dụng cho mọi hạng mục (kế thừa từ BR-207, cập nhật quyền chỉnh sửa theo Mục 0)

Mỗi hạng mục dưới đây được đánh giá **Đạt / Không đạt** riêng biệt:
- **Đạt** → ảnh minh chứng là **tùy chọn**.
- **Không đạt** → **bắt buộc** ít nhất 1 ảnh gắn đúng hạng mục + **bắt buộc** nhập lý do (comment).

Mỗi hạng mục có thêm 1 dòng "Chú thích rút gọn" (icon "ℹ", mở rộng khi cần) để Medical Staff đối chiếu trước khi tick, không cần chuyên môn sâu về ATTP. **Theo quyết định 06/07/2026, trường này nay do chính Medical Staff sửa trực tiếp, không qua phê duyệt** (khác với ghi chú "chỉ System Admin được sửa" ở bản trước — xem Mục 0). Nội dung chú thích dưới đây là **bản nháp tham khảo dựa trên `00_Policies/` hiện có**, cần đội dự án/Medical Staff rà lại trước khi đưa vào hệ thống làm dữ liệu khởi tạo.

Cột "Nguồn pháp lý" trích dẫn đúng điều khoản đã đọc được trong `00_Policies/`; mục nào **không có căn cứ trong `00_Policies/`** được ghi chú rõ bằng **`[cần xác nhận thêm]`**, không suy diễn.

---

## 2. Checklist đề xuất — 6 nhóm theo bản "Safety Canteen Audit Checklist" (người dùng cung cấp, 06/07/2026)

### Nhóm 1 — Hồ sơ

| # | Hạng mục kiểm tra | Nguồn pháp lý | BR liên quan | Chú thích rút gọn (nháp) |
| --- | --- | --- | --- | --- |
| 1.1 | Hợp đồng cung cấp hàng hóa, hồ sơ nguồn gốc nguyên liệu | Luật ATTP 55/2010/QH12, Điều 19.1.đ ("lưu giữ hồ sơ về nguồn gốc, xuất xứ nguyên liệu thực phẩm..."); QĐ 1246/QĐ-BYT, Điều 3.1.a (kiểm tra "hợp đồng mua bán" khi nhập nguyên liệu); TTLT 13/2016, Phụ lục 04, Mục IV.4.2 ("Đối với các trường không tự cung cấp thức ăn: có ký hợp đồng với các cơ sở có giấy chứng nhận đủ điều kiện an toàn thực phẩm để cung cấp suất ăn cho học sinh" — 2.0 điểm) | — | Cơ sở phải lưu giữ hợp đồng cung cấp hàng hóa và hồ sơ chứng minh nguồn gốc nguyên liệu; nếu trường không tự nấu ăn, đơn vị cung cấp suất ăn phải có Giấy chứng nhận đủ điều kiện an toàn thực phẩm (Luật ATTP Điều 19.1.đ; TTLT 13/2016 Phụ lục 04 Mục IV.4.2). |
| 1.2 | Hóa đơn chứng từ chứng minh nguồn gốc nguyên liệu thực phẩm | QĐ 1246/QĐ-BYT, Điều 3.1.a ("chứng nhận về nguồn gốc, xuất xứ,... hóa đơn và các giấy tờ khác có liên quan") | — | Mỗi lô nguyên liệu nhập vào phải có hóa đơn/chứng từ chứng minh nguồn gốc, xuất xứ, kiểm tra ngay ở Bước 1 kiểm thực ba bước (QĐ 1246/QĐ-BYT, Điều 3.1.a). |

### Nhóm 2 — Yêu cầu đối với người tiếp xúc trực tiếp sản phẩm

| # | Hạng mục kiểm tra | Nguồn pháp lý | BR liên quan | Chú thích rút gọn (nháp) |
| --- | --- | --- | --- | --- |
| 2.1 | Phải mặc đồ bảo hộ, khẩu trang để đảm bảo vệ sinh trong quá trình chế biến, cung cấp đồ ăn | TTLT 13/2016, Phụ lục 04, Mục IV.4.4 ("Người trực tiếp làm việc tại nhà ăn, nhà bếp mặc trang phục bảo hộ riêng, đội mũ, đi găng tay chuyên dụng, đeo khẩu trang" — 1.0 điểm); QĐ 1246/QĐ-BYT, Điều 4.1.a | — | Người trực tiếp chế biến, phục vụ phải mặc trang phục bảo hộ riêng, đội mũ, đeo khẩu trang, đi găng tay chuyên dụng trong suốt quá trình làm việc (TTLT 13/2016, Phụ lục 04, Mục IV.4.4). |
| 2.2 | Đảm bảo vệ sinh cá nhân sạch sẽ, gọn gàng tuân thủ quy định VSATTP | Luật ATTP 55/2010/QH12, Điều 29.4 ("Tuân thủ quy định về sức khoẻ, kiến thức và thực hành của người trực tiếp sản xuất, kinh doanh thực phẩm"); QĐ 1246/QĐ-BYT, Điều 4.1.a | BR-103 (gián tiếp) | Người trực tiếp chế biến, phục vụ phải giữ vệ sinh cá nhân sạch sẽ, gọn gàng, tuân thủ các quy định về sức khỏe, kiến thức và thực hành an toàn thực phẩm (Luật ATTP, Điều 29.4). |
| 2.3 | Không được ăn uống, nhai kẹo cao su, hút thuốc lá trong khu vực nhà bếp | **`[cần xác nhận thêm]`** — chưa tìm thấy văn bản gốc quy định trực tiếp nội dung này trong `00_Policies/` hiện có (nhiều khả năng thuộc QCVN 01-1:2018/BYT về thực hành vệ sinh, văn bản này chưa có trong hồ sơ dự án) | — | *Chưa xác nhận được nguồn pháp lý cụ thể — cần bổ sung văn bản (ví dụ QCVN 01-1:2018/BYT) nếu có, trước khi đưa vào hệ thống như một quy tắc bắt buộc có căn cứ pháp lý.* |
| 2.4 | Sử dụng găng tay nilon khi tiếp xúc với thực phẩm chín, lau đũa thìa | QĐ 1246/QĐ-BYT, Điều 4.1.a (kiểm tra "găng tay" của người chế biến); Luật ATTP, Điều 29.1 (dụng cụ riêng cho thực phẩm sống/chín) — **`[cần xác nhận thêm]`** phần chi tiết "chất liệu nilon" cụ thể, luật chỉ yêu cầu chung "găng tay", không quy định chất liệu | — | Người chế biến phải đeo găng tay khi tiếp xúc trực tiếp với thực phẩm đã chế biến (chín) và khi lau dụng cụ ăn uống (QĐ 1246/QĐ-BYT, Điều 4.1.a). *Chất liệu "nilon" cụ thể: chưa xác nhận được nguồn pháp lý riêng.* |

### Nhóm 3 — Điều kiện bảo quản thực phẩm

| # | Hạng mục kiểm tra | Nguồn pháp lý | BR liên quan | Chú thích rút gọn (nháp) |
| --- | --- | --- | --- | --- |
| 3.1 | Khu vực nhập hàng, kho bảo quản thực phẩm đảm bảo vệ sinh, thực phẩm đảm bảo khoảng cách với nền, tường, trần theo quy định | Luật ATTP, Điều 20.1.a (diện tích đủ rộng, bảo quản riêng biệt, xếp dỡ an toàn); TTLT 13/2016, Phụ lục 04, Mục IV.4.3 (kho chứa thực phẩm: tường/trần/sàn nhẵn, phẳng) — **`[cần xác nhận thêm]`** con số khoảng cách cụ thể (cm) với nền/tường/trần, luật chỉ yêu cầu chung chung | — | Khu nhập hàng và kho bảo quản phải sạch sẽ, có phương tiện phân loại và lưu giữ thực phẩm phù hợp (Luật ATTP, Điều 20.1.a; TTLT 13/2016, Phụ lục 04, Mục IV.4.3). *Khoảng cách tối thiểu cụ thể với nền/tường/trần: chưa xác nhận được con số trong `00_Policies/`.* |
| 3.2 | Thực phẩm phải có đầy đủ thông tin ghi nhãn theo quy định | NĐ 15/2018/NĐ-CP, Điều 24 ("Nội dung ghi nhãn bắt buộc") | — | Thực phẩm bao gói sẵn phải ghi nhãn đầy đủ theo nội dung bắt buộc quy định tại NĐ 15/2018/NĐ-CP, Điều 24. |
| 3.3 | Thực phẩm được bảo quản đúng cách, đúng nhiệt độ, ngăn ngừa lây nhiễm chéo (sống chín tách biệt...) | Luật ATTP, Điều 20.1.b (nhiệt độ, độ ẩm...); Điều 28.1 (không nhiễm chéo sống/chín); Điều 29.1 | — | Thực phẩm sống và thực phẩm đã chế biến (chín) phải được bảo quản tách biệt, đúng nhiệt độ quy định, tránh lây nhiễm chéo (Luật ATTP, Điều 28.1, Điều 29.1). |
| 3.4 | Dụng cụ bảo quản tách biệt riêng giữa thực phẩm tươi sống, thực phẩm đã qua chế biến | Luật ATTP, Điều 29.1 | — | Phải có dụng cụ, đồ chứa đựng riêng cho thực phẩm sống và thực phẩm chín, không dùng lẫn (Luật ATTP, Điều 29.1). |
| 3.5 | Chế độ theo dõi nhiệt độ bảo quản phù hợp | Luật ATTP, Điều 20.1.b | BR-110 *(kiểm tra chéo — xem ghi chú Mục 4)* | Khu vực bảo quản phải có thiết bị chuyên dụng điều chỉnh và theo dõi nhiệt độ phù hợp với từng loại thực phẩm (Luật ATTP, Điều 20.1.b). Riêng tủ lưu mẫu thực phẩm áp dụng thêm quy tắc nhiệt độ 2–8°C của BR-110 (kiểm tra chéo với module Lưu mẫu Thực phẩm). |
| 3.6 | Chế độ vệ sinh khu vực bảo quản định kỳ đảm bảo sạch | Luật ATTP, Điều 20.1.c | — | Khu vực bảo quản phải được vệ sinh định kỳ theo đúng quy định của tổ chức, cá nhân sản xuất, kinh doanh thực phẩm (Luật ATTP, Điều 20.1.c). |
| 3.7 | Khu vực bảo quản thực phẩm sắp xếp gọn gàng, ngăn nắp | Luật ATTP, Điều 20.1.a; TTLT 13/2016, Phụ lục 04, Mục IV.4.3 | — | Khu vực bảo quản phải đủ diện tích, sắp xếp gọn gàng để bảo quản riêng biệt từng loại thực phẩm, thực hiện xếp dỡ an toàn, chính xác (Luật ATTP, Điều 20.1.a). |
| 3.8 | Không có dấu vết của côn trùng, động vật gây hại | Luật ATTP, Điều 19.1.c (thiết bị phòng, chống côn trùng và động vật gây hại); Điều 20.1.b | — | Khu vực bảo quản phải có biện pháp ngăn ngừa côn trùng, động vật gây hại xâm nhập (Luật ATTP, Điều 19.1.c, Điều 20.1.b). |

### Nhóm 4 — Điều kiện khu vực sơ chế, chế biến thực phẩm

| # | Hạng mục kiểm tra | Nguồn pháp lý | BR liên quan | Chú thích rút gọn (nháp) |
| --- | --- | --- | --- | --- |
| 4.1 | Bố trí sắp xếp khu vực bếp theo nguyên tắc 1 chiều | TTLT 13/2016, Phụ lục 04, Mục IV.4.2 ("Nơi chế biến thức ăn được thiết kế theo nguyên tắc 1 chiều, có đủ dụng cụ chế biến, bảo quản, sử dụng riêng với thực phẩm sống và thức ăn chín" — 1.0 điểm); tương tự NĐ 15/2018/NĐ-CP, dòng 534 (nguyên tắc một chiều, áp dụng cho cơ sở sản xuất thực phẩm nói chung) | — | **Nguyên tắc một chiều** nghĩa là bố trí bếp theo trình tự vật lý một hướng: khu tiếp nhận nguyên liệu → khu sơ chế → khu chế biến/nấu nướng → khu chia soạn → khu ăn uống, sao cho thực phẩm/nguyên liệu di chuyển theo một chiều duy nhất, không đi ngược lại hoặc cắt ngang qua khu vực đã sạch, nhằm tránh nhiễm chéo giữa thực phẩm sống và thực phẩm chín (TTLT 13/2016, Phụ lục 04, Mục IV.4.2). |
| 4.2 | Khu vực sơ chế có đầy đủ trang thiết bị đảm bảo an toàn sơ chế | Luật ATTP, Điều 19.1.c | — | Khu sơ chế phải có đủ trang thiết bị phù hợp để xử lý nguyên liệu an toàn (Luật ATTP, Điều 19.1.c). |
| 4.3 | Cống rãnh thông thoát nước, không bị ứ đọng và được che đậy | Luật ATTP, Điều 28.4 ("Cống rãnh ở khu vực cửa hàng, nhà bếp phải thông thoát, không ứ đọng"); TTLT 13/2016, Điều 5.3.a — **`[cần xác nhận thêm]`** phần "được che đậy", luật chỉ yêu cầu "thông thoát, không ứ đọng", chưa quy định bắt buộc có nắp che | — | Cống rãnh khu vực bếp phải thông thoát, không ứ đọng nước thải (Luật ATTP, Điều 28.4). *Yêu cầu "có nắp che đậy": chưa xác nhận được nguồn pháp lý cụ thể trong `00_Policies/`.* |
| 4.4 | Dụng cụ thu gom rác thải có nắp đậy | Luật ATTP, Điều 28.3 ("Có dụng cụ thu gom, chứa đựng rác thải, chất thải bảo đảm vệ sinh") — **`[cần xác nhận thêm]`** chi tiết "có nắp đậy", luật chỉ yêu cầu "bảo đảm vệ sinh" nói chung | — | Cơ sở phải có dụng cụ thu gom, chứa đựng rác thải, chất thải bảo đảm vệ sinh (Luật ATTP, Điều 28.3). *Yêu cầu cụ thể "có nắp đậy": chưa xác nhận được nguồn pháp lý riêng.* |
| 4.5 | Dụng cụ sơ chế riêng biệt đối với thực phẩm tươi sống | Luật ATTP, Điều 29.1; TTLT 13/2016, Phụ lục 04, Mục IV.4.2 | — | Dụng cụ sơ chế thực phẩm tươi sống phải tách biệt, không dùng chung với thực phẩm đã qua chế biến (Luật ATTP, Điều 29.1). |
| 4.6 | Khu vực chế biến thực phẩm có trang bị phương tiện phòng chống côn trùng và động vật gây hại | Luật ATTP, Điều 19.1.c; Điều 28.5; TTLT 13/2016, Phụ lục 04, Mục IV.4.1 ("cửa sổ có lưới chống chuột, ruồi nhặng, côn trùng") | BR-102 *(gián tiếp — xem ghi chú Mục 4)* | Khu chế biến phải có phương tiện phòng, chống côn trùng và động vật gây hại (ví dụ lưới chắn côn trùng ở cửa sổ) (Luật ATTP, Điều 28.5; TTLT 13/2016, Phụ lục 04, Mục IV.4.1). |
| 4.7 | Dụng cụ, trang thiết bị chế biến đảm bảo vệ sinh và sử dụng riêng đối với thực phẩm đã qua chế biến | Luật ATTP, Điều 29.1, Điều 29.2 | — | Dụng cụ, thiết bị dùng cho thực phẩm đã qua chế biến (chín) phải bảo đảm vệ sinh, không dùng chung với thực phẩm sống (Luật ATTP, Điều 29.1, 29.2). |
| 4.8 | Chế biến trên bàn cao mặt đất tối thiểu 60 cm | Luật ATTP, Điều 30.3 ("được bày bán trên bàn hoặc giá cao hơn mặt đất") — **`[cần xác nhận thêm]`** con số "tối thiểu 60cm" cụ thể; luật hiện có chỉ yêu cầu định tính "cao hơn mặt đất", không nêu số đo | — | Bàn/giá chế biến, bày thực phẩm phải đặt cao hơn mặt đất để tránh nhiễm bẩn (Luật ATTP, Điều 30.3). *Con số "tối thiểu 60cm": chưa xác nhận được nguồn pháp lý cụ thể trong `00_Policies/` — có thể thuộc quy chuẩn kỹ thuật riêng (ví dụ QCVN chế biến suất ăn), cần bổ sung văn bản nếu có.* |
| 4.9 | Có đầy đủ phương tiện rửa tay, hóa chất diệt khuẩn cho nhân viên chế biến, phục vụ | Luật ATTP, Điều 28.6 (thiết bị rửa tay); TTLT 13/2016, Phụ lục 04, Mục IV.4.1 ("có nguồn nước sạch và chỗ rửa tay với xà phòng hoặc dung dịch sát khuẩn") | — | Khu chế biến phải có đủ phương tiện rửa tay và hóa chất diệt khuẩn/dung dịch sát khuẩn cho nhân viên (Luật ATTP, Điều 28.6; TTLT 13/2016, Phụ lục 04, Mục IV.4.1). |
| 4.10 | Sử dụng chất tẩy rửa có nguồn gốc, không sử dụng loại sản phẩm bán trôi nổi trên thị trường | Luật ATTP, Điều 23.1.đ ("Chất tẩy rửa, chất diệt khuẩn, chất khử độc khi sử dụng phải bảo đảm an toàn cho con người và môi trường") — **`[cần xác nhận thêm]`**: điều khoản này thuộc Mục 2 "sản xuất thực phẩm tươi sống", không phải Mục 4 "kinh doanh dịch vụ ăn uống"; dùng làm nguyên tắc tương tự (analogy), chưa tìm được điều khoản áp riêng cho dịch vụ ăn uống | — | Chất tẩy rửa, khử trùng sử dụng trong bếp phải có nguồn gốc rõ ràng, bảo đảm an toàn cho người sử dụng (áp dụng nguyên tắc chung tại Luật ATTP, Điều 23.1.đ). *Yêu cầu riêng cho dịch vụ ăn uống: chưa xác nhận được điều khoản áp trực tiếp.* |
| 4.11 | Đảm bảo các biện pháp phòng cháy, chữa cháy theo quy định | **`[cần xác nhận thêm]`** — thuộc phạm vi Luật Phòng cháy và chữa cháy (không nằm trong Luật An toàn thực phẩm), văn bản này hiện **không có** trong `00_Policies/` | — | *Yêu cầu về phòng cháy, chữa cháy thuộc phạm vi pháp luật PCCC, không phải Luật An toàn thực phẩm — cần bổ sung văn bản gốc (ví dụ Luật PCCC 2001, sửa đổi 2013) nếu muốn có căn cứ trích dẫn cụ thể.* **Quyết định 06/07/2026**: giữ mục này trong checklist Medical Staff, đánh dấu rõ đây là **kiểm tra chéo ngoài phạm vi Luật An toàn thực phẩm** (tương tự mẫu cross-check ở Nhóm 6) — Medical Staff kiểm tra chung trong lúc audit bếp tổng quát, nhưng căn cứ pháp lý chính (nếu có) thuộc trách nhiệm chuyên môn của bộ phận PCCC/Facilities, không phải Luật ATTP. |

### Nhóm 5 — An toàn thực phẩm

| # | Hạng mục kiểm tra | Nguồn pháp lý | BR liên quan | Chú thích rút gọn (nháp) |
| --- | --- | --- | --- | --- |
| 5.1 | Nguyên liệu chế biến đảm bảo có nguồn gốc, có kiểm định theo đúng quy định của Nhà nước, được rửa sạch sẽ trước khi đưa vào chế biến | Luật ATTP, Điều 26.1 (nguyên liệu có nguồn gốc, xuất xứ rõ ràng); Điều 25.2 (không ô nhiễm chéo); QĐ 1246/QĐ-BYT, Điều 3 (Bước 1 — kiểm tra trước khi chế biến) | — | Nguyên liệu phải rõ nguồn gốc, còn hạn sử dụng, được kiểm tra cảm quan và rửa sạch trước khi chế biến — thực hiện đúng Bước 1 kiểm thực ba bước (Luật ATTP, Điều 26.1; QĐ 1246/QĐ-BYT, Điều 3). |
| 5.2 | Không sử dụng các thực phẩm quá hạn sử dụng, ôi, thiu, hỏng không đảm bảo chất lượng để chế biến đồ ăn | Luật ATTP, Điều 26.1 ("còn thời hạn sử dụng"); QĐ 1246/QĐ-BYT, Điều 3.1.c (loại bỏ nguyên liệu không đạt yêu cầu) | — | Không được sử dụng nguyên liệu quá hạn, ôi thiu, hư hỏng; nếu phát hiện khi kiểm tra Bước 1 phải loại bỏ/trả lại (Luật ATTP, Điều 26.1; QĐ 1246/QĐ-BYT, Điều 3.1.c). |
| 5.3 | Thức ăn, đồ uống phải đảm bảo được nấu chín/đủ điều kiện chín trước khi cung cấp cho khách hàng | Luật ATTP, Điều 30.2 ("Thực phẩm phải được chế biến bảo đảm an toàn, hợp vệ sinh"); QĐ 1246/QĐ-BYT, Điều 5 (Bước 3 — kiểm tra trước khi ăn) | — | Món ăn phải được chế biến chín/đủ điều kiện an toàn trước khi phục vụ, đối chiếu đánh giá cảm quan ở Bước 3 kiểm thực ba bước (Luật ATTP, Điều 30.2; QĐ 1246/QĐ-BYT, Điều 5). |
| 5.4 | Không sử dụng các loại phụ gia thực phẩm, phẩm màu, chất ngọt tổng hợp gây hại trong chế biến đồ uống ăn | Luật ATTP, Điều 26.2 (phụ gia phải tuân thủ Điều 13, 17); NĐ 15/2018/NĐ-CP, Điều 33.2 ("Sử dụng phụ gia thực phẩm không vượt quá mức sử dụng tối đa cho phép;... có nguồn gốc, xuất xứ rõ ràng") — **`[cần xác nhận thêm]`** danh mục cụ thể các loại phẩm màu/chất ngọt tổng hợp bị cấm; danh mục chi tiết do Bộ Y tế ban hành, chưa có trong `00_Policies/` | — | Chỉ được dùng phụ gia thực phẩm trong danh mục Bộ Y tế cho phép, đúng liều lượng, có nguồn gốc rõ ràng (Luật ATTP, Điều 26.2; NĐ 15/2018/NĐ-CP, Điều 33.2). *Danh mục cụ thể các chất bị cấm (phẩm màu, chất ngọt tổng hợp...): chưa xác nhận được — cần bổ sung Thông tư/Phụ lục danh mục phụ gia của Bộ Y tế nếu có.* |

### Nhóm 6 — Sổ kiểm thực 3 bước và lưu mẫu

> **Ghi chú phạm vi (áp dụng quyết định #4, Mục 0)**: hai hạng mục dưới đây thuộc phạm vi nghiệp vụ chính của module **Food Sample Retention / Kiểm thực ba bước** (UC-079 "Hồ sơ kiểm thực", UC-076/UC-118 — lưu mẫu), không phải quy tắc mới của UC-014. Được giữ trong checklist bếp tổng quát như một **mục kiểm tra chéo (cross-check)**, theo đúng mẫu đã áp dụng cho mục nhiệt độ tủ lưu mẫu (BR-110) ở bản đề xuất trước — xem `review_screen_medical_staff.md` §7.3 (finding h) về việc dữ liệu kiểm thực ba bước hiện bị trùng lặp giữa nhiều màn hình.

| # | Hạng mục kiểm tra | Nguồn pháp lý | BR liên quan | Chú thích rút gọn (nháp) |
| --- | --- | --- | --- | --- |
| 6.1 | Có thực hiện việc lưu mẫu đúng theo quy định không (số lượng, thời gian, dụng cụ, tình trạng bảo quản) và ghi chép sổ 3 bước đúng quy định | QĐ 1246/QĐ-BYT, Điều 3–5 (kiểm thực 3 bước: Bước 1/2/3); Điều 6 (dụng cụ lưu mẫu: nắp đậy kín, tối thiểu 100g thức ăn đặc/150ml thức ăn lỏng); Điều 7 (thời điểm lấy mẫu); Điều 8 (bảo quản: 2–8°C, tối thiểu 24 giờ) | BR-105–BR-114 *(cross-check, module Food Sample Retention)*; BR-110 | Cơ sở phải ghi chép đầy đủ Sổ kiểm thực ba bước (Mẫu số 1–3, Phụ lục 1, QĐ 1246/QĐ-BYT) và lưu mẫu thức ăn đúng quy định: dụng cụ có nắp đậy kín, bảo quản 2–8°C, lưu tối thiểu 24 giờ (QĐ 1246/QĐ-BYT, Điều 6–8). *Đây là mục kiểm tra chéo với module Lưu mẫu Thực phẩm — dữ liệu chi tiết được quản lý chính thức tại UC-079/UC-076/UC-118, không nhập lại tại đây.* |
| 6.2 | Lưu đủ mẫu, đúng tên thực phẩm, đủ định lượng mẫu lưu | QĐ 1246/QĐ-BYT, Điều 7.2 ("Thức ăn đặc... tối thiểu 100 gam"; "Thức ăn lỏng... tối thiểu 150 ml"); Điều 1.2 (áp dụng cho bữa ăn từ 30 suất trở lên) | BR-105–BR-114 *(cross-check)* | Mỗi món ăn của bữa ăn từ 30 suất trở lên phải được lưu mẫu riêng, đúng tên, đủ định lượng tối thiểu (100g thức ăn đặc / 150ml thức ăn lỏng) (QĐ 1246/QĐ-BYT, Điều 1.2, Điều 7.2). |

---

## 3. Đối chiếu với bản đề xuất trước (Nhóm A–E, 04–05/07/2026)

Bản checklist mới do người dùng cung cấp (06/07/2026) được xác nhận là **bản chuẩn thay thế** cho bản đề xuất Nhóm A–E trước đó. Tuy nhiên, khi đối chiếu, một số hạng mục ở bản cũ — vốn đã có căn cứ pháp lý xác nhận rõ ràng — **không xuất hiện tường minh** trong bản checklist mới. Đây không phải lỗi của bản mới (được cung cấp như một checklist thực hành, không nhất thiết bao quát 100% mọi điều khoản luật), nhưng cần được nêu rõ để tránh **vô tình bỏ sót yêu cầu đã có căn cứ** khi khởi tạo dữ liệu mặc định:

| Hạng mục ở bản cũ | Căn cứ pháp lý đã xác nhận | Có trong bản mới? | Đề xuất |
| --- | --- | --- | --- |
| A3 — Giấy xác nhận đủ sức khỏe của người trực tiếp chế biến | Luật ATTP, Điều 36.1.d; TTLT 13/2016, Phụ lục 04, Mục IV.4.4 ("Có giấy chứng nhận sức khỏe theo quy định của Bộ Y tế" — 1.0 điểm) | Không có mục riêng (Nhóm 2 chỉ nói "vệ sinh cá nhân", không nhắc giấy khám sức khỏe) | Đề xuất giữ lại như một hạng mục riêng trong Nhóm 2 (liên kết BR-103) — có căn cứ pháp lý rõ và đã có BR chính thức, tránh mất dấu vết truy nguyên (traceability) tới BR-103 |
| A4 — Giấy xác nhận đã tập huấn kiến thức ATTP | Luật ATTP, Điều 36.1.đ; TTLT 13/2016, Phụ lục 04, Mục IV.4.4 ("Có giấy chứng nhận tập huấn về an toàn thực phẩm" — 1.0 điểm) | Không có | Đề xuất giữ lại tương tự A3 — hiện **chưa có BR** cho mục này (đã ghi chú ở bản cũ), cần đội dự án xem xét cấp BR mới nếu giữ |
| D1/D2 — Phòng chống côn trùng + chứng nhận kiểm soát côn trùng định kỳ | Luật ATTP, Điều 19.1.c, Điều 27.1.c; BR-102 (Nhật ký kiểm soát côn trùng) | Có một phần (mục 4.6 — chỉ nói "trang bị phương tiện", không nhắc "nhật ký/chứng nhận kiểm soát côn trùng định kỳ") | Đề xuất bổ sung lại **nhật ký/chứng nhận kiểm soát côn trùng định kỳ** như một hạng mục riêng, để giữ liên kết với BR-102 (hiện BR-102 đang mô tả một "nhật ký" mà nếu bỏ hạng mục checklist tương ứng thì không rõ hạng mục nào trong UC-014 tạo ra dữ liệu cho nhật ký đó) |
| E1 — Nguồn nước chế biến đạt quy chuẩn kỹ thuật | TTLT 13/2016, Điều 5; TTLT 13/2016, Phụ lục 04, Mục IV.4.1 ("có nguồn nước sạch"); Luật ATTP, Điều 28.2 ("Có đủ nước đạt quy chuẩn kỹ thuật phục vụ việc chế biến, kinh doanh") | Không có nhóm/mục riêng về nguồn nước | Đề xuất bổ sung lại như một hạng mục (có thể gộp vào Nhóm 3 hoặc Nhóm 4) — nguồn nước là điều kiện có căn cứ pháp lý trực tiếp và rõ ràng nhất trong toàn bộ các văn bản đã đọc |

> **Quyết định 06/07/2026 (người dùng)**: **giữ đúng 6 nhóm** như bản checklist đã cung cấp ở Mục 2, **không** bổ sung lại 4 hạng mục trên vào dữ liệu khởi tạo mặc định. Lý do được chấp nhận: vì checklist nay đã cho phép Medical Staff tự CRUD (Mục 0), Medical Staff có thể tự thêm các hạng mục này sau khi hệ thống vận hành nếu thấy cần thiết, không cần đội dự án quyết định trước.
> **Lưu ý cần theo dõi**: vì 4 hạng mục này (đặc biệt A3/A4 — giấy khám sức khỏe/tập huấn ATTP) vẫn có liên kết với BR-102/BR-103 đã tồn tại và được các Screen Spec khác tham chiếu (ví dụ `12_ConductFoodSafetyInspectionScreen.md` mục 3–4 "Xác minh chứng nhận kiểm soát côn trùng"/"Xác minh khám sức khỏe nhân viên bếp"), các mục này **không biến mất khỏi hệ thống** — chúng vẫn tồn tại như field xác minh riêng (mục 3, 4 trong Screen Spec 12), độc lập với checklist 6 nhóm động ở đây. Chỉ có "nguồn nước" (E1 cũ) là thực sự không còn xuất hiện ở bất kỳ đâu sau quyết định này.

---

## 4. Tổng hợp các mục còn `[cần xác nhận thêm]`

| # | Hạng mục | Vấn đề |
| --- | --- | --- |
| 2.3 | Không ăn uống/nhai kẹo cao su/hút thuốc trong bếp | Chưa tìm thấy văn bản gốc trong `00_Policies/` |
| 2.4 | Găng tay **nilon** cụ thể | Luật chỉ yêu cầu chung "găng tay", không quy định chất liệu |
| 3.1 | Khoảng cách cụ thể với nền/tường/trần | Luật chỉ yêu cầu định tính, không có con số |
| 4.3 | Cống rãnh "có nắp che đậy" | Luật chỉ yêu cầu "thông thoát, không ứ đọng" |
| 4.4 | Thùng rác "có nắp đậy" | Luật chỉ yêu cầu "bảo đảm vệ sinh" chung |
| 4.8 | Bàn chế biến cao tối thiểu **60cm** | Luật chỉ yêu cầu định tính "cao hơn mặt đất" |
| 4.10 | Chất tẩy rửa "có nguồn gốc, không trôi nổi" | Điều khoản khớp gần nhất thuộc nhóm thực phẩm tươi sống, không phải dịch vụ ăn uống |
| 4.11 | Phòng cháy, chữa cháy | Thuộc Luật PCCC, không có trong `00_Policies/`; cần xác nhận có thuộc phạm vi Medical Staff hay không |
| 5.4 | Danh mục cụ thể phẩm màu/chất ngọt tổng hợp bị cấm | Danh mục chi tiết do Bộ Y tế ban hành riêng, chưa có trong `00_Policies/` |

---

## 5. Tác động tới tài liệu liên quan

Việc chuyển checklist sang mô hình dynamic (Mục 0) kéo theo các thay đổi sau. **Quyết định 06/07/2026 (người dùng): thực hiện toàn bộ ngay trong đợt cập nhật này** (không hoãn lại):

1. **`business_rules.md`, BR-207**: sửa lại điều khoản "do System Admin cấu hình và chỉnh sửa" để phản ánh đúng quyền CRUD trực tiếp của Medical Staff (quyết định #1, #2, Mục 0). — **Đã thực hiện**, xem `business_rules.md` §12.
2. **Business Rule mới BR-210**: mô tả quyền quản lý/chỉnh sửa checklist động của Medical Staff (thêm/xóa/sắp xếp nhóm và hạng mục, sửa chú thích) — tương tự cách BR-209 được cấp cho US-09. — **Đã cấp**, xem `business_rules.md` §12.
3. **Use Case mới UC-186** — "Manage Kitchen Inspection Checklist" (Medical Staff), tiếp nối UC-185. — **Đã cấp**, xem `traceability_matrix.md`.
4. **`MedicalStaff_UserStory.md`**: bổ sung US-22 mô tả hành vi CRUD checklist. — **Đã thực hiện**.
5. **Screen Spec `12_ConductFoodSafetyInspectionScreen.md`** (Web & Mobile): bổ sung mô tả màn hình/luồng quản lý checklist (thêm/sửa/xóa nhóm, hạng mục, chú thích), cập nhật lại ghi chú quyền chỉnh sửa chú thích (không còn "chỉ System Admin"). — **Đã thực hiện**.
6. **`traceability_matrix.md`**: thêm dòng UC-186/BR-210. — **Đã thực hiện**.
7. **`error_message.md`**: xem xét message code mới cho lỗi xóa nhóm/hạng mục đang được tham chiếu bởi lịch sử kiểm tra đã lưu. — **Đã rà soát**, xem `error_message.md` §3 (mã mới nếu có, hoặc ghi chú lý do chưa cần mã riêng).

**Quyết định đã chốt 06/07/2026** (không còn là câu hỏi mở):
- 4 hạng mục ở Mục 3 (giấy khám sức khỏe, giấy tập huấn ATTP, nhật ký côn trùng, nguồn nước): **giữ nguyên hiện trạng** — không thêm vào 6 nhóm checklist động; giấy khám sức khỏe/tập huấn ATTP/côn trùng tiếp tục là field xác minh riêng ở Screen Spec 12 (mục 3–4, không đổi); "nguồn nước" tạm thời không xuất hiện ở đâu cả — Medical Staff có thể tự thêm sau nếu cần (đã có CRUD).
- Mục 4.11 (phòng cháy chữa cháy): **giữ trong checklist Medical Staff**, đánh dấu là mục kiểm tra chéo ngoài phạm vi Luật An toàn thực phẩm (xem chú thích tại Mục 2, Nhóm 4).
