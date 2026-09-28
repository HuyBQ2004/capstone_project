# Screen Spec — Menu Detail

**Actor**: School Administration | **UC liên quan**: UC-134 (View Menu Detail), UC-006 (Approval / Reject Menu) — Report 3 §3.2 | **Status**: Draft
**BR liên quan**: BR-062 (mục tiêu dinh dưỡng), BR-063 (xung đột dị ứng — chặn duyệt nếu nghiêm trọng), BR-066 (electronic signature khi duyệt), BR-067 (khóa menu Approved/Published), BR-069 (khai báo nguyên liệu đầy đủ), BR-070 (khóa/publish sau approve — theo UC-006 gốc), BR-074 (ghi log lý do từ chối), BR-075 (thông báo sau approve)
**MSG liên quan**: MSG-004, MSG-005, MSG-040 (phê duyệt & công bố thành công), MSG-051 (cảnh báo xung đột dị ứng)

> Ghi chú đối chiếu BR: UC-006 trong Report 3 (dòng 2817) liệt kê "BR-063, BR-066, BR-067, BR-070, BR-074, BR-075" — trong đó BR-070/BR-074/BR-075 theo `business_rules.md` hiện hành lại mang nội dung khác (BR-070 = xác minh trạng thái Approved nhà cung cấp khi nhận hàng; BR-074 = xác minh vị trí nhận hàng; BR-075 = nguyên tắc FEFO) — không khớp ngữ nghĩa "khóa menu/ghi log rejection/thông báo" như mô tả trong Normal Flow. Khả năng cao đây là lệch số BR giữa 2 lần cập nhật `business_rules.md` (UC-006 có thể đã được viết khi BR còn đánh số khác). **[Giả định — cần team xác nhận]**: giữ nguyên trích dẫn theo đúng Report 3 gốc nhưng ghi chú rõ nghi vấn lệch số này để team đối chiếu lại, đồng thời bổ sung BR-067/BR-068 (khóa & quy trình phê duyệt) làm căn cứ chính xác hơn theo `business_rules.md` hiện hành.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Ngày phục vụ + Meal slot | Text hiển thị | — | — | |
| 2 | Danh sách món ăn + nguyên liệu đầy đủ + trace allergen | Table/Card list | — | BR-069 | |
| 3 | Tóm tắt dinh dưỡng | Card số liệu | Theo nhóm tuổi | BR-062 | |
| 4 | Cảnh báo xung đột dị ứng (nếu có) | Alert (danger) | Chặn Approve nếu xung đột nghiêm trọng | BR-063 | AF2 UC-006, MSG-051 |
| 5 | Lịch sử duyệt/từ chối (timestamp, chữ ký điện tử, ghi chú) | Table lịch sử | — | BR-066 | |
| 6 | Nút "Approve" | Button (primary) | Disable nếu đang có cảnh báo dị ứng nghiêm trọng chưa xử lý | BR-063, BR-067 | |
| 7 | Nút "Reject" + ghi chú lý do | Button (destructive) + Textarea bắt buộc | Bắt buộc ghi chú | — | AF1 UC-006 |

### Trạng thái màn hình
- Loading: Skeleton card khi tải chi tiết menu.
- Empty: Không áp dụng.
- Error: MSG-051 nếu phát hiện xung đột dị ứng — chặn Approve (AF2); MSG-004/MSG-005.
- Success: MSG-040 khi Approve (khóa + publish + thông báo phụ huynh/giáo viên); toast xác nhận khi Reject (trả về trạng thái Rejected, thông báo Service Caterers).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Menu Management` (UC-128) hoặc `Menu Approval Dashboard` — click 1 dòng. Không có màn hình con.
**Bố cục**: Dashboard Shell — Page Header (ngày phục vụ + nút Approve/Reject), Content 2 cột: danh sách món + dinh dưỡng (`col-span-8`), lịch sử duyệt (`col-span-4`).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Menu Detail, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (nút Approve/Reject), 2 cột: danh sách món + nguyên liệu + dinh dưỡng, lịch sử duyệt.
Thành phần bắt buộc: ngày phục vụ, danh sách món (nguyên liệu + allergen), card dinh dưỡng, alert cảnh báo xung đột dị ứng, bảng lịch sử duyệt, nút Approve, nút Reject + textarea lý do.
Áp dụng Design System: alert dị ứng dùng token danger.
Trạng thái cần thể hiện: error (xung đột dị ứng chặn duyệt), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2805–2821 (Table 3-62 Approval/Reject Menu), 2977–2997 (Table 3-70 View Menu Detail)
- traceability_matrix.md §1.6 (UC-006, UC-134)
- business_rules.md BR-062, BR-063, BR-067, BR-068, BR-069
