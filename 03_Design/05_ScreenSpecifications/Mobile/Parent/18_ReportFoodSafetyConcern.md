# Screen Spec — Report Food Safety Concern

**Actor**: Parent | **UC liên quan**: UC-073 - Report Food Safety Concern | **Status**: Draft
**BR liên quan**: BR-146 (tự động kích hoạt điều tra ≥3 học sinh/24h), BR-147, BR-148, BR-149 (tự động quarantine), BR-150, BR-151 (bản đồ truy xuất 4h), BR-154, BR-155
**MSG liên quan**: MSG-004, MSG-005, MSG-023 (thiếu thông tin bắt buộc), MSG-048 (ghi nhận thành công)

> **Cập nhật 27/07/2026** (đồng bộ theo code Web đã triển khai, `parent/safety-concern/page.tsx`,
> áp dụng chung cho Mobile để nhất quán 2 nền tảng): gộp 3 field rời (Symptoms Checkbox
> Group, Meal Date Input, Suspected Dish Input) vào chung ô "Incident Details" (free-text).
> Lý do: đây là kênh báo cáo khẩn cấp (BR-138: gửi NGAY, không qua duyệt) — bắt phụ
> huynh chọn đủ 3 ô riêng trước khi mô tả sự việc làm chậm luồng gửi trong tình huống
> gấp; Y tế trường vẫn nhận đủ thông tin qua đọc mô tả tự do khi tiếp nhận/phân loại
> (BR-146). Danh mục "Concern Category" vẫn giữ là Dropdown riêng vì cần cho phân loại
> tự động.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title Header | Text (đỏ) | "Report Safety Issue" | — | Nguồn: Report 3 §3.3.2.4 |
| 2 | Concern Category Dropdown | Dropdown | Bắt buộc (VD "Spoiled/Sour Food") | BR-146 | |
| 3 | Incident Details Section | Textarea | Bắt buộc | BR-146 | MSG-023 nếu thiếu. Ô mô tả tự do — phụ huynh ghi đủ: ngày/bữa ăn nghi vấn (thay Meal Date Input), món ăn nghi vấn nếu biết (thay Suspected Dish Input), triệu chứng của con nếu có (thay Symptoms Checkbox Group), thời điểm xuất hiện, tình trạng hiện tại. Đã gộp 27/07/2026 — xem ghi chú cập nhật ở đầu file. |
| 4 | Photo Attachment Component | File upload (đỏ, bắt buộc) | Bắt buộc — "Attach Food Error / Medical Report Photo *" | — | MSG-023 nếu thiếu |
| 5 | Send Urgent Report Button | Button (danger, full-width) | Validate, gửi ngay không qua duyệt (BR-138) | BR-146, BR-149, BR-151 | MSG-023 nếu thiếu; MSG-048 khi thành công |

### Trạng thái màn hình
- Loading: Spinner khi submit.
- Empty: không áp dụng (form tạo mới).
- Error: `SnackBar` `danger` + MSG-023 (thiếu trường bắt buộc, đặc biệt ảnh bằng chứng).
- Success: `SnackBar` `success` (MSG-048) — nếu đạt ngưỡng ≥3 học sinh/24h, tự động tạo sự cố chính thức và quarantine lô nghi vấn (BR-146, BR-149), thông báo cho School Administration.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Parent Dashboard ("Report Urgent Safety Incident") hoặc Feedback Center ("safety concern"). Ra: về Parent Dashboard sau khi gửi (không có bước duyệt trung gian — BR-138).
**Bố cục**: AppBar (tiêu đề đỏ) → form 1 cột: Concern Category → Incident Details (textarea tự do) → Photo Attachment (viền đỏ) → Send Urgent Report Button (nền đỏ, full-width, cố định đáy).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Report Food Safety Concern, nền tảng Mobile, actor Parent.
Bố cục: AppBar (tiêu đề đỏ) + form 1 cột (Concern Category, Incident Details, Photo Attachment viền đỏ) + nút Send Urgent Report (nền đỏ) cố định đáy.
Thành phần bắt buộc: Concern Category Dropdown, Incident Details Section (textarea tự do — gộp cả ngày/bữa ăn nghi vấn, món nghi vấn, triệu chứng), Photo Attachment Component, Send Urgent Report Button.
Áp dụng Design System: toàn bộ màn hình nhấn mạnh màu danger (khẩn cấp, bypass duyệt BR-138).
Trạng thái cần thể hiện: loading, error (MSG-023), success (MSG-048).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6142–6159 (Report Safety Issue, field-by-field gốc), dòng 4485–4505 (UC-073 Normal/Alternative/Exception Flow).
- `business_rules.md` BR-138, BR-146–151, BR-154, BR-155; `error_message.md` MSG-023, MSG-048.
