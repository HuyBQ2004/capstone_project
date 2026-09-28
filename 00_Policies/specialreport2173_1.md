# Biên bản Sự việc — Mẫu tham khảo (do người dùng cung cấp, 06/07/2026)

> **Loại tài liệu**: đây **không phải** văn bản quy phạm pháp luật (khác với các file còn lại trong `00_Policies/`). Đây là một **biên bản sự việc thực tế đã điền** (mẫu giấy, có nội dung ví dụ cụ thể) do người dùng thu thập làm căn cứ thiết kế field cho màn hình "Sổ vi phạm và xử lý" (UC-178 Record Process Violation, UC-179 Record Staff Violation). Nội dung số liệu bên dưới (tên người, chỉ tiêu kiểm tra, kết quả) chỉ là **dữ liệu ví dụ** của đơn vị phát hành gốc (Trường Đại học FPT, P.VSATTP) — không thuộc phạm vi dự án SFMS, chỉ dùng để rút ra **cấu trúc field** của mẫu biên bản.
> **Nguồn**: file đính kèm `specialreport2173_1.pdf` (ảnh chụp/scan qua CamScanner), người dùng tải lên 06/07/2026.
> **Mục đích sử dụng trong dự án**: căn cứ thiết kế lại field cho BR-211 (xuất Biên bản Vi phạm dạng PDF + lưu ảnh biên bản đã ký 3 bên) — xem `business_rules.md` §18, Screen Spec `21_RecordProcessViolation.md`, `22_RecordStaffViolation.md`.

---

## Nội dung nguyên văn (transcribe từ ảnh scan)

```
TRƯỜNG ĐẠI HỌC FPT                     CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM
P.VSATTP                               Độc lập - Tự do - Hạnh phúc

                        BIÊN BẢN SỰ VIỆC

Hôm nay, tại: SIMDO                    Vào: 22 giờ 47 phút, ngày 5 tháng 6 năm 2026

Chúng tôi gồm

Đại diện P.VSATTP
Ông(bà): Hà Thị Vân Anh                Chức vụ: P.VSANTP

Đại diện nhà thầu
Ông(bà): Đỗ Mạnh Hùng                  Chức vụ: Quản lý

Người chứng kiến
Ông(bà): ________________              Chức vụ: ________________
(để trống trong bản ví dụ)

Cùng nhau ghi nhận sự việc như sau:

Vào hồi 8h30 ngày 06/5/2026 phòng VSATTP thực hiện lấy mẫu tại Canteen Simdo test nhanh
các chỉ tiêu ATTP, kết quả như sau:

1. Chỉ tiêu Hàn the
- Mẫu Mọc: Đạt (Âm tính)

3. Chỉ tiêu Độ sạch bát đĩa
- Đĩa nhựa to: 5/5 Đạt
- Bát nhỏ (ăn xôi): 4/5 Đạt; 1/5 Không đạt độ tinh sạch dầu mỡ
- Khay ăn inox: 3/5 Đạt; 1/5 Không đạt độ tinh sạch dầu mỡ; 1/5 Không đạt độ tinh sạch dầu mỡ
- Bát sứ to: 5/5 Đạt độ tinh sạch dầu mỡ

3. Chỉ tiêu độ ôi khét dầu mỡ
- Mẫu dầu đang chiên tại bếp cơm: Đạt

Biên bản sự việc được hoàn thành vào 22 giờ 47 phút, ngày 5 tháng 6 năm 2026. Biên bản được
lập thành 01 bản, trong đó P.VSATTP giữ 1 bản, nhà thầu giữ 0 bản, có giá trị pháp lý như nhau.

        P.VSATTP                NHÀ THẦU                NGƯỜI LÀM CHỨNG
   (chữ ký, để trống)        (chữ ký, để trống)         (chữ ký, để trống)
```

*(Ghi chú transcribe: đánh số mục "1... 3... 3..." giữ nguyên như bản gốc — có khả năng lỗi đánh số của người lập biên bản gốc (thiếu mục "2"), không phải lỗi transcribe. Không tự ý sửa số thứ tự vì đây là trích dẫn nguyên văn.)*

---

## Cấu trúc field rút ra từ mẫu (dùng cho thiết kế BR-211)

| Nhóm | Field trong mẫu giấy | Ghi chú đối chiếu hệ thống |
| --- | --- | --- |
| Tiêu đề | Tên đơn vị lập biên bản + phòng ban (header trái), Quốc hiệu tiêu ngữ (header phải), tên loại biên bản ("BIÊN BẢN SỰ VIỆC") | Cố định trong template PDF do hệ thống sinh ra — không phải field nhập tay, lấy tên trường từ hồ sơ trường (`CURRENT_SCHOOL`) |
| Địa điểm & thời gian mở đầu | "Hôm nay, tại: ___", "Vào: giờ phút, ngày tháng năm" | Field mới `locationOfIncident` (địa điểm); thời gian ở đây là **thời điểm lập biên bản**, có thể lấy tự động = server timestamp khi bắt đầu điền, không cần nhập tay |
| Bên 1 — Đại diện P.VSATTP | Ông(bà) [tên], Chức vụ [chức vụ] | = **Y tế trường học (Medical Staff)** — người lập biên bản, tự động điền từ tài khoản đăng nhập (đã có sẵn — field `reportedBy`) |
| Bên 2 — Đại diện nhà thầu | Ông(bà) [tên], Chức vụ [chức vụ] | Field mới `caterersRepresentativeId` — theo quyết định người dùng 06/07/2026: liên kết tài khoản **Service Caterers**, không phải nhập tay tự do |
| Bên 3 — Người chứng kiến | Ông(bà) [tên], Chức vụ [chức vụ] — có thể để trống | Đã có sẵn field `witnessId` ở `21_RecordProcessViolation.md` (bắt buộc nếu ≥ MEDIUM, theo BR-183); cần bổ sung tương tự vào `22_RecordStaffViolation.md` để đồng nhất 2 màn theo cùng 1 mẫu |
| Nội dung sự việc | Đoạn văn tự do, có thể chia mục con với kết quả Đạt/Không đạt | Khớp field "Mô tả vi phạm" (Textarea) đã có sẵn ở cả 2 màn — không cần đổi loại field, Textarea đã hỗ trợ nội dung nhiều dòng/mục |
| Thời điểm xảy ra sự việc (trong nội dung) | "Vào hồi 8h30 ngày 06/5/2026..." | Field `occurredAt` — đã có sẵn ở `StaffViolationRecord` (UC-179); cần bổ sung tương tự cho vi phạm quy trình (UC-178) |
| Kết thúc | "Biên bản hoàn thành vào [giờ], lập thành 01 bản, [bên] giữ mấy bản, có giá trị pháp lý như nhau" | Câu văn cố định trong template PDF hệ thống sinh ra — không phải field nhập tay |
| Chữ ký 3 bên | 3 cột: P.VSATTP / NHÀ THẦU / NGƯỜI LÀM CHỨNG, để trống chờ ký tay | Hệ thống xuất PDF với 3 khoảng trống chữ ký tương ứng; sau khi in — ký tay — chụp ảnh, tải lên field mới `signedRecordPhotoUrl` (bắt buộc để có bằng chứng pháp lý hoàn chỉnh) |

Xem chi tiết Business Rule mới (BR-211) và cập nhật Screen Spec tại `business_rules.md` §18 và `21_RecordProcessViolation.md`/`22_RecordStaffViolation.md` (Web & Mobile).
