# Screen Spec — Inspect Ingredients Before Processing Screen (Step 1)

**Actor**: Service Caterers | **UC liên quan**: UC-062 - Record Ingredients Before Processing (Report 3 §3.2, "Conduct Step 1 — Sample No. 1") | **Status**: Draft
**BR liên quan**: BR-076, BR-079, BR-083, BR-090, BR-091, BR-092
**MSG liên quan**: MSG-015 (Error — GPS), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Quét mã vạch nguyên liệu/dry ingredient để xuất kho | Input/Barcode scan | Bắt buộc | BR-090 | UC-062 Normal Flow bước 2 |
| 2 | Với nguyên liệu tươi/đông lạnh: giờ giao, khối lượng, nhà cung cấp, chứng nhận thú y/kiểm dịch | Form nhập | Bắt buộc | BR-092 | UC-062 Normal Flow bước 2 |
| 3 | Với nguyên liệu khô/đóng gói: nhà sản xuất, hạn dùng, điều kiện lưu trữ | Form nhập | Bắt buộc | BR-092 | UC-062 Normal Flow bước 2 |
| 4 | Kiểm tra cảm quan (màu sắc, mùi, hình thức) | Checklist | Bắt buộc đủ mục | BR-095 | UC-062 Normal Flow bước 2 |
| 5 | Kiểm tra hạn sử dụng + FEFO | Validation tự động | Chặn nếu nguyên liệu hết hạn | BR-076, BR-083 | UC-062 Normal Flow bước 2 |
| 6 | Ảnh bằng chứng nguyên liệu + chứng nhận | Upload/Chụp ảnh | Bắt buộc | BR-083 | UC-062 Normal Flow bước 2 |
| 7 | GPS + timestamp | Tự động ghi nhận | Trong khu vực hợp lệ của trường | BR-079 | UC-062 Preconditions, Normal Flow |
| 8 | Nút "Nộp Bước 1" | Button | Nếu đạt → mở khoá Step 2; nếu thất bại → tạo Corrective Action (UC-038), chặn tiến trình | BR-097 | UC-062 Alternative Flow AF1 |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải thông tin phiên sản xuất.
- Empty: Không áp dụng.
- Error: MSG-015 (GPS ngoài khu vực); chặn tiến trình + tạo Corrective Action Request nếu bất kỳ mục nào Failed (BR-097).
- Success: Chuyển trạng thái sang Step 2 (Upload Image Cooked Food Screen).

---

## Platform Layout — Web

**Entry/Exit points**: Từ Three-Step Inspection Dashboard (nút "Bắt đầu/Tiếp tục Kiểm tra" khi ở Bước 1). Thoát: nộp thành công → Step 2.
**Bố cục**: Form 1 trang dài trong `Tabs`/Stepper của Inspection Dashboard, ảnh bằng chứng dùng upload file thay chụp trực tiếp (secondary path, khuyến nghị dùng Mobile tại bếp theo `09_Service_Caterers_Screen_Flow_Web.md`).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Inspect Ingredients Before Processing Screen (Step 1), nền tảng Web, actor Service Caterers.
Bố cục: form 1 trang trong Stepper, gồm quét mã vạch, form nguyên liệu tươi/khô, checklist cảm quan, upload ảnh, khối GPS/timestamp, nút Nộp Bước 1.
Thành phần bắt buộc: cảnh báo hết hạn/FEFO (BR-076/BR-083), cảnh báo GPS (MSG-015).
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error (chặn + tạo Corrective Action), success (mở khoá Step 2).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3535–3549 (UC-062 - Record Ingredients Before Processing, mục "Conduct Step 1")
- business_rules.md BR-076, BR-079, BR-083, BR-090, BR-091, BR-092
- error_message.md MSG-015
