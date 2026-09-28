# Screen Spec — Report Food Safety Concern

**Actor**: Parent | **UC liên quan**: UC-073 - Report Food Safety Concern | **Status**: Draft
**BR liên quan**: BR-138, BR-146, BR-147, BR-148, BR-149, BR-150, BR-151, BR-154, BR-155
**MSG liên quan**: MSG-004, MSG-005, MSG-023, MSG-048

> **Cập nhật 27/07/2026** (đồng bộ theo code đã triển khai, `parent/safety-concern/page.tsx`):
> gộp 3 field rời (Symptoms Checkbox Group, Meal Date Input, Suspected Dish Input) vào
> chung ô "Mô tả chi tiết sự việc" (Incident Details, free-text). Lý do: phụ huynh báo
> sự cố trong tình huống khẩn cấp — bắt gõ đủ 3 ô riêng trước khi mô tả sự việc làm
> chậm luồng gửi báo cáo khẩn (BR-138: gửi NGAY, không qua duyệt); Y tế trường vẫn
> nhận đủ thông tin qua đọc mô tả tự do khi tiếp nhận/phân loại (BR-146). Danh mục
> "Loại sự cố" (Concern Category) vẫn giữ là Select riêng vì cần cho phân loại tự động.
> (Lưu ý: BR-154 — "Medical Staff tạo báo cáo sự cố chính thức" — không liên quan
> trực tiếp tới field này; field #3 chỉ căn cứ BR-146, không trích BR-154 như ghi
> chú cũ trong code trước đây.)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text (destructive) | "Report Safety Issue" | — | |
| 2 | Concern Category Select | `Select` | Bắt buộc | BR-146 | |
| 3 | Incident Details Section | `Textarea` | Bắt buộc | BR-146 | MSG-023. Ô mô tả tự do — phụ huynh ghi đủ: ngày/bữa ăn nghi vấn (thay Meal Date Input), món ăn nghi vấn nếu biết (thay Suspected Dish Input), triệu chứng của con nếu có (thay Symptoms Checkbox Group), thời điểm xuất hiện, tình trạng hiện tại. Đã gộp 27/07/2026 — xem ghi chú cập nhật ở đầu file. |
| 4 | Photo Attachment Component | `Input type="file"` (bắt buộc) | Bắt buộc | — | MSG-023 |
| 5 | Send Urgent Report Button | Button (destructive) | Validate, gửi ngay (BR-138) | BR-146, BR-149, BR-151 | MSG-023/048 |

### Trạng thái màn hình
- Loading: Spinner khi submit.
- Error: `Alert variant="destructive"` + MSG-023.
- Success: `Toast` `success` (MSG-048).
- Empty: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Phản hồi & Sự cố" (đặt cố định, dễ thấy — không chỉ ẩn trong menu, theo `10_Parent_Screen_Flow_Web.md` "Khác biệt so với bản Mobile").
**Bố cục**: Topbar + Sidebar + Content: form đơn (không Stepper — <8 field) với toàn bộ nhấn mạnh màu destructive, nút Send Urgent Report nổi bật.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Report Food Safety Concern, nền tảng Web, actor Parent.
Bố cục: Topbar + Sidebar + form đơn (Concern Category, Incident Details, Photo Attachment) + nút Send Urgent Report màu destructive.
Thành phần bắt buộc: Concern Category Select, Incident Details Section (textarea tự do — gộp cả ngày/bữa ăn nghi vấn, món nghi vấn, triệu chứng), Photo Attachment Component, Send Urgent Report Button.
Áp dụng Design System: nhấn mạnh màu danger toàn màn hình.
Trạng thái cần thể hiện: loading, error (MSG-023), success (MSG-048).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6142–6159, dòng 4485–4505 (UC-073).
- `business_rules.md` BR-138, BR-146–151, BR-154, BR-155; `error_message.md` MSG-023, MSG-048.
