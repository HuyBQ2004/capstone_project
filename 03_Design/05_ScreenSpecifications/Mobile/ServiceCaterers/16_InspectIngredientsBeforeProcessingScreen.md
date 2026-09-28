# Screen Spec — Inspect Ingredients Before Processing Screen (Step 1)

**Actor**: Service Caterers | **UC liên quan**: UC-062 - Record Ingredients Before Processing (Report 3 §3.2, "Conduct Step 1 — Sample No. 1") | **Status**: Draft
**BR liên quan**: BR-076, BR-079, BR-083, BR-090, BR-091, BR-092, BR-196
**MSG liên quan**: MSG-015 (Error — GPS), MSG-078 (Success — lưu offline), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Quét mã vạch nguyên liệu/dry ingredient để xuất kho | Camera scanner | Bắt buộc | BR-090 | UC-062 Normal Flow bước 2 |
| 2 | Với nguyên liệu tươi/đông lạnh: giờ giao, khối lượng, nhà cung cấp, chứng nhận thú y/kiểm dịch | Form nhập | Bắt buộc | BR-092 | UC-062 Normal Flow bước 2 |
| 3 | Với nguyên liệu khô/đóng gói: nhà sản xuất, hạn dùng, điều kiện lưu trữ | Form nhập | Bắt buộc | BR-092 | UC-062 Normal Flow bước 2 |
| 4 | Kiểm tra cảm quan (màu sắc, mùi, hình thức) | Checklist (CheckboxListTile) | Bắt buộc đủ mục — có thể điền offline (BR-196) | BR-095, BR-196 | UC-062 Normal Flow bước 2 |
| 5 | Kiểm tra hạn sử dụng + FEFO | Validation tự động | Chặn nếu nguyên liệu hết hạn | BR-076, BR-083 | UC-062 Normal Flow bước 2 |
| 6 | Ảnh bằng chứng nguyên liệu + chứng nhận | Camera capture | Bắt buộc — cho phép offline (BR-196) | BR-083, BR-196 | UC-062 Normal Flow bước 2 |
| 7 | GPS + timestamp | Tự động ghi nhận | Trong khu vực hợp lệ của trường | BR-079 | UC-062 Preconditions |
| 8 | Nút "Nộp Bước 1" | FilledButton (cố định cuối màn) | Nếu đạt → mở khoá Step 2; nếu Failed → tạo Corrective Action, chặn tiến trình | BR-097 | UC-062 Alternative Flow AF1 |

### Trạng thái màn hình
- Loading: `Shimmer` khi tải thông tin phiên sản xuất.
- Empty: Không áp dụng.
- Error: MSG-015 (GPS ngoài khu vực); chặn tiến trình + tạo Corrective Action Request nếu Failed (BR-097).
- Success: `SnackBar` MSG-043 (online) hoặc MSG-078 + badge "Chờ đồng bộ" (offline) — chuyển sang Step 2.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Three-Step Inspection Dashboard (nút "Bắt đầu/Tiếp tục Kiểm tra" khi ở Bước 1) — kênh chính tại bếp. Thoát: nộp thành công → Step 2 (Upload Image Cooked Food Screen).
**Bố cục**: Trường cần camera/GPS đặt ưu tiên đầu form (quét mã vạch, chụp ảnh) theo `Layout_Guidelines.md` mục 2.2, checklist cảm quan và form nguyên liệu bên dưới, nút "Nộp Bước 1" cố định cuối màn.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Inspect Ingredients Before Processing Screen (Step 1), nền tảng Mobile, actor Service Caterers.
Bố cục: quét mã vạch + chụp ảnh ưu tiên đầu form, form nguyên liệu tươi/khô, checklist cảm quan, khối GPS/timestamp, nút Nộp Bước 1 cố định dưới cùng.
Thành phần bắt buộc: cảnh báo hết hạn/FEFO, cảnh báo GPS (MSG-015), badge "Chờ đồng bộ" nếu offline.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error (chặn + tạo Corrective Action), success (online/offline).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3535–3549 (UC-062 - Record Ingredients Before Processing, mục "Conduct Step 1")
- business_rules.md BR-076, BR-079, BR-083, BR-090, BR-091, BR-092, BR-196
- error_message.md MSG-015, MSG-078
