# Review: Medical Staff → Service Caterers — các điểm cần Service Caterers xử lý (Incident/CAPA)

> **Mục đích**: tách riêng khỏi `review_medicalstaff_incidentmanagement.md` những phát hiện/đề xuất phát sinh trong đợt rà soát module Incident Management (từ góc nhìn Medical Staff) nhưng có ảnh hưởng trực tiếp tới màn hình/luồng nghiệp vụ của **Service Caterers**, để người phụ trách Service Caterers dễ tra cứu mà không phải đọc toàn bộ file gốc.
>
> **Trạng thái chung (cập nhật 22/07/2026): 6/7 việc đã VÀO CODE.** Nội dung gốc bên dưới giữ nguyên làm lịch sử thảo luận; bảng "Tổng hợp việc cần làm" ở cuối file đã cập nhật trạng thái thật. Ngày tạo file: 20/07/2026.

---

> **Cập nhật 20/07/2026: Mục 1, 2, 3 dưới đây đã CHỐT xong** qua nhiều vòng thảo luận — xem `review_medicalstaff_incidentmanagement.md` Mục 16 (luồng đầy đủ + bảng phương án đã cân nhắc/bị loại) và `business_rules.md` BR-216 (mới). Giữ nguyên nội dung gốc bên dưới làm lịch sử, chỉ thêm ghi chú "ĐÃ CHỐT" ở đầu mỗi mục.

## 1. CAPA status — vòng đời bị đứt giữa 3 vai trò (P0)

> **ĐÃ CHỐT (20/07/2026)**: thay bằng 1 bộ enum thống nhất `DRAFT → PENDING_ADMIN_APPROVAL → APPROVED_FOR_IMPLEMENTATION → IN_PROGRESS → SUBMITTED_EVIDENCE → COMPLETED` (BR-216). Màn `corrective-actions` của Service Caterers cần hiển thị CAPA ngay từ khi ở `APPROVED_FOR_IMPLEMENTATION` (đã qua School Admin duyệt), có hành động "Xem + Accept" để tự chuyển sang `IN_PROGRESS` — không còn khái niệm `ASSIGNED`/`SUBMITTED`/`OVERDUE` như bàn cũ.

Chi tiết đầy đủ + số dòng code xem `review_medicalstaff_incidentmanagement.md` Mục 14.1. Tóm tắt phần liên quan Service Caterers:

- Medical Staff tạo CAPA với `status: "ASSIGNED"`.
- Màn `service-caterers/corrective-actions/page.tsx` (dòng 40, 168) **chỉ nhận diện được `status === "IN_PROGRESS"` hoặc `"OVERDUE"`** — không có nhánh xử lý `"ASSIGNED"`, nên CAPA vừa được giao **không hiện ra** cho Service Caterers thao tác.
- Không có đoạn code nào (ở cả 3 vai trò) chuyển `ASSIGNED` → `IN_PROGRESS`/`SUBMITTED`.

**Cần Service Caterers xác nhận khi thiết kế lại**: màn `corrective-actions` cần hiển thị CAPA ngay từ khi vừa được giao (`ASSIGNED`), có hành động rõ ràng để chuyển sang `IN_PROGRESS` khi bắt đầu thực hiện (khác với việc chỉ chờ hệ thống tự chuyển trạng thái).

**Trạng thái**: ghi nhận, chờ chốt Mục 3 (thứ tự duyệt) trước khi thiết kế lại enum đầy đủ.

---

## 2. Cơ chế phản hồi/tranh luận khi Service Caterers không đồng ý với CAPA — đề xuất mới

> **ĐÃ CHỐT (20/07/2026), theo hướng khác với đề xuất gốc bên dưới**: không thêm trạng thái `DISPUTED` hay bất kỳ kênh phản hồi/tranh luận nào trên hệ thống. Lý do: việc bàn bạc tính khả thi diễn ra **trực tiếp ngoài đời, trước khi** Medical Staff soạn CAPA (Medical Staff xuống bếp kiểm tra + trao đổi với Caterers trước). Khi CAPA lên hệ thống và được School Admin duyệt, Service Caterers **chỉ xem và accept**, không có nhánh từ chối. Đây là quyết định có chủ đích để tránh phình hệ thống thành kênh chat 2 bên — xem đầy đủ lý do và các phương án đã loại ở `review_medicalstaff_incidentmanagement.md` Mục 16.4.

**Vấn đề**: hiện không có cách nào trong hệ thống để Service Caterers phản hồi lại nếu thấy CAPA do Medical Staff đề xuất không hợp lý (thời hạn phi thực tế, thiếu nguyên liệu/nhân lực thay thế, nghi root cause chưa đúng...). Đã kiểm tra cơ chế tương tự gần nhất trong SRS (BR-095/096 — Corrective Action Request khi kiểm tra 3 bước thất bại) — cũng thiếu, không phải hạn chế riêng của CAPA. Chiều duy nhất đang có là ngược lại: Medical Staff/School Admin đẩy CAPA về cho Service Caterers làm lại (`reworkReason` bắt buộc, `medical-staff/incidents/[id]/page.tsx` dòng 566-576).

**Rủi ro nếu không có cơ chế**: bất đồng sẽ được giải quyết ngoài hệ thống (gọi điện, nhắn trực tiếp), phá vỡ audit trail — đúng loại vấn đề toàn hệ thống được xây để tránh.

**Đề xuất (chưa quyết, cần người phụ trách Service Caterers + Medical Staff cùng xác nhận)**:
1. Thêm hành động **"Yêu cầu điều chỉnh / Không thể thực hiện"** trên màn `corrective-actions` của Service Caterers, kèm lý do bắt buộc — theo đúng idiom đã có sẵn trong dự án (hành động phản đối/reject luôn kèm lý do bắt buộc + audit, ví dụ `reworkReason`, `rejectReason` ở CAPA/partnerships).
2. Thêm trạng thái CAPA mới, tạm gọi `DISPUTED` — CAPA chuyển về Medical Staff xem xét (vòng lặp tương tự E16 ở Workflow 11), School Admin nhìn thấy để can thiệp nếu cần.
3. **Câu hỏi mở**: thời gian ở trạng thái `DISPUTED` có tính vào SLA 12 giờ của BR-152 không? Đề xuất nghiêng về **tạm dừng đồng hồ SLA**, ghi riêng thời lượng tranh luận vào audit — tránh Service Caterers bị gắn nhãn "Late Submission" (BR-152 EX1) chỉ vì phản hồi hợp lý.

**Trạng thái**: đề xuất, chưa có BR, chưa thiết kế màn hình.

---

## 3. Thứ tự duyệt CAPA: trước hay sau khi Service Caterers thực hiện — vẫn đang mở

> **ĐÃ CHỐT (20/07/2026)**: School Administration duyệt **kế hoạch trước khi** Service Caterers thực hiện (khớp Workflow 11, không khớp UC-005 gốc trong `Report 3.md` — UC-005 cần viết lại). Màn `corrective-actions` của Service Caterers do đó **luôn có gate chờ duyệt trước khi được bắt đầu** — không có trường hợp "nhận CAPA rồi tự chuyển `IN_PROGRESS` mà chưa qua School Admin".

Phát hiện mâu thuẫn 3 chiều giữa Workflow 11 (duyệt kế hoạch trước khi thực hiện), BR-153 (chữ "được đề xuất" — nghiêng về duyệt kế hoạch), và UC-005/UC-059 trong `Report 3.md` (duyệt kết quả sau khi đã thực hiện — đa số use case text theo hướng này). Chi tiết đầy đủ xem `review_medicalstaff_incidentmanagement.md` Mục 14.2.

**Vì sao Service Caterers cần biết trước khi có quyết định cuối**: mô hình chọn sẽ quyết định màn `corrective-actions` của Service Caterers có gate "chờ duyệt kế hoạch mới được bắt đầu" hay được thực hiện ngay sau khi nhận CAPA (`ASSIGNED` → tự chuyển `IN_PROGRESS` không cần chờ ai duyệt trước).

**Trạng thái**: chưa chốt — người dùng đang nghiêng về mô hình "duyệt sau khi làm" (khớp đa số UC text + tính khẩn cấp ATTP, không ăn vào SLA 12 giờ BR-152), nhưng chưa chốt chính thức.

---

## 4. Thu hồi mẫu xét nghiệm — Service Caterers là bên thực hiện vật lý (đã chốt trước đó, nhắc lại để tổng hợp)

BR-113/BR-215 đã sửa từ 19/07/2026 (trước đợt rà soát này): Medical Staff chỉ **chỉ định** mẫu cần thu hồi + phòng lab đích + chỉ tiêu; **Service Caterers** là bên thực hiện lấy mẫu tại tủ lưu, bàn giao phòng kiểm nghiệm, và xác nhận đã gửi xét nghiệm kèm bằng chứng (ảnh/scan phiếu nhận, BR-212).

**Chưa đồng bộ**: `Workflow 8` (`00_Business_Workflows.md`) và UC-076 trong `Report 3.md` **vẫn còn ghi Medical Staff trực tiếp thực hiện thu hồi** — chỉ riêng `business_rules.md` đã sửa, 2 tài liệu này chưa cập nhật theo. Cần Service Caterers xác nhận màn hình xác nhận-đã-gửi-lab (ảnh/scan + tên đại diện phòng lab nhận mẫu) khi Screen Spec `14_RetrieveFoodSampleRecordsDialog.md` được tách thành 2 phần (form chỉ định của Medical Staff | phần xác nhận của Service Caterers) — việc này đã ghi trong `review_medicalstaff_foodsamples.md`, chưa áp code.

**Trạng thái**: BR đã đúng, tài liệu Workflow/UC còn sai, code/Screen Spec chưa tách 2 phần.

---

## 5. Kết quả xét nghiệm lab — Medical Staff tải file kết quả; Service Caterers chỉ tải bằng chứng bàn giao (đã đính chính 20/07/2026)

> **ĐÍNH CHÍNH 20/07/2026**: nội dung gốc bên dưới ghi sai actor — đã sửa lại. Service Caterers **không** tải file kết quả xét nghiệm. Xem lý do trong `review_medicalstaff_incidentmanagement.md` Mục 17.2 điểm 6.

Đã chốt: cấu trúc dữ liệu "Kết quả xét nghiệm lab" chuyển từ textarea tự do (hiện tại, Medical Staff tự gõ trên tab Root Cause) sang **structured field: nhiều ảnh/file PDF + 1 field kết luận kết quả, do Medical Staff tải lên** — vì phòng xét nghiệm phản hồi trực tiếp cho bên đứng tên yêu cầu xét nghiệm (Medical Staff, đã ghi tên khi chỉ định thu hồi mẫu — BR-113), không đi qua Service Caterers. Service Caterers **chỉ** tải ảnh/scan hoặc biên bản **bàn giao** mẫu cho cơ quan y tế (bằng chứng đã gửi đi, BR-212 — không đổi, xem Mục 4) — đây là 2 sự kiện khác nhau (bàn giao mẫu vs. nhận kết quả), trước đó bị gộp nhầm thành 1 actor.

**Lưu ý khi thiết kế**: màn nhập kết luận kết quả (Medical Staff) nên hiển thị luôn danh sách món/lô nguyên liệu đang bị khoá liên quan, kèm hành động mở khoá tại chỗ nếu kết luận không phải do lô đó — lý do mở khoá tự động lấy từ field kết luận, không bắt nhập lại (BR-217).

**Trạng thái**: đã chốt hướng, chưa thiết kế field/màn hình cụ thể.

---

## 6. Hủy yêu cầu thu hồi mẫu trước khi Service Caterers xác nhận đã gửi lab — cần chốt granularity

Đã đồng ý cho Medical Staff hủy **yêu cầu** thu hồi (khác hủy mẫu lưu vật lý — mẫu lưu đã khoá hủy ngay theo BR-215) kèm lý do bắt buộc + audit, miễn Service Caterers chưa xác nhận gửi lab. Việc thêm không phức tạp (1 trạng thái `CANCELLED` trên request + quy tắc mở khoá lại mẫu lưu tương ứng).

**Câu hỏi còn mở cần Service Caterers/Medical Staff cùng chốt**: 1 yêu cầu có thể gồm nhiều món/mẫu cùng gửi 1 lần (theo quyết định đã có ở `review_medicalstaff_foodsamples.md` Mục 11.6/11.11) — khi Medical Staff muốn hủy chỉ 1 món trong yêu cầu đó, hủy toàn bộ yêu cầu hay chỉ hủy dòng đó (giữ lại các món còn lại)?

**Trạng thái**: nguyên tắc đã đồng ý, chi tiết granularity chưa chốt.

---

## Tổng hợp việc cần làm (CHƯA thực hiện — chỉ tổng hợp lại các mục trên)

| # | Việc | Trạng thái (22/07/2026) |
| --- | --- | --- |
| 1 | Cài enum CAPA mới theo BR-216 dùng chung 3 vai trò | ✅ **Đã làm.** Vòng đời `DRAFT → PENDING_ADMIN_APPROVAL → APPROVED_FOR_IMPLEMENTATION → IN_PROGRESS → SUBMITTED_EVIDENCE → COMPLETED` chạy đủ trên cả 3 màn. 22/07 gỡ nốt `ASSIGNED`/`SUBMITTED`/`OVERDUE` — chú thích cũ nói "giữ để đọc dữ liệu cũ" nhưng đây là mock DB, không có dữ liệu cũ nào |
| 2 | ~~Hành động "Yêu cầu điều chỉnh / Không thể thực hiện"~~ | ✅ **Đã chốt không làm** (Mục 2) |
| 3 | Gate "chờ School Admin duyệt trước khi Caterers thực hiện" | ✅ **Đã làm.** Màn Caterers hiện CAPA từ `APPROVED_FOR_IMPLEMENTATION` với nút nhận việc |
| 4 | Đồng bộ Workflow 8 + UC-076 theo BR-113/BR-215 | ✅ **Đã làm 22/07.** Workflow 8 tách bước 15 thành 15a (Medical Staff chỉ định) / 15b (Service Caterers lấy & bàn giao); UC-076 bổ sung Secondary Actor, sửa bước 8 (không tự sinh biên bản — BR-212), thêm AF3 huỷ yêu cầu |
| 5 | Field kết quả lab + panel món/lô đang khoá kèm nút mở khoá tại chỗ | ✅ **Đã làm 22/07.** Panel lô đang khoá nằm ngay cạnh field kết luận, kèm tồn còn lại và số bữa đang chờ; lý do mở khoá tự lấy từ kết luận. Kết quả lab vẫn là textarea — nâng thành nhiều ảnh/PDF là việc còn lại |
| 6 | Trạng thái `CANCELLED` cho yêu cầu thu hồi mẫu + chốt granularity | ✅ **Đã làm 22/07.** Chốt granularity: **huỷ cả yêu cầu**, không huỷ từng dòng — một yêu cầu là một lần bàn giao cho lab, bỏ bớt một món giữa chừng thì bên nhận không đối chiếu được với phiếu đã in |
| 7 | Bỏ `capaDeadline()`/hạn SLA 12h; hiển thị 2 timestamp | ✅ **Đã làm.** Hàm `capaDeadline` không còn tồn tại trong code |

### Còn lại sau đợt 22/07/2026

| # | Việc | Ghi chú |
| --- | --- | --- |
| 5b | Kết quả lab: nâng từ textarea thành nhiều ảnh/PDF | **Đã quyết 22/07: GIỮ ô nhập tạm.** Bản mock chưa có cơ chế lưu file thật; dựng khối upload chỉ để hiển thị sẽ là đúng loại "màn báo thành công mà không lưu gì" vừa dọn ở SFMS-FLOW-REV-001 §9.2. Nâng khi làm backend, lúc đó có chỗ lưu file thật |
