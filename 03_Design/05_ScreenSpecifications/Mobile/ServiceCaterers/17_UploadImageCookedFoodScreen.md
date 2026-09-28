# Screen Spec — Upload Image Cooked Food Screen (Step 2)

**Actor**: Service Caterers | **UC liên quan**: UC-063 - Record Ingredients In Processing (theo `14_Screen_Authorization_Matrix.md` + danh sách UC tổng hợp Report 3 dòng 1070) | **Status**: Draft
**BR liên quan**: BR-090, BR-091, BR-093, BR-095, BR-097, BR-196
**MSG liên quan**: MSG-017 (Error — nhiệt độ nấu chưa đạt), MSG-078 (Success — lưu offline), MSG-004/MSG-005 (Error)

> **Lưu ý khoảng trống nguồn**: giống bản Web — UC-063 không có bảng đặc tả UC đầy đủ riêng trong `Report 3.md`, chỉ có 1 dòng khai báo trong bảng tổng hợp UC (dòng 1070). Nội dung dưới đây suy ra từ **Step 2 trong Normal Flow của UC-062** (dòng 3542). **[Giả định — cần team xác nhận]**.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tên món / Meal Slot | Read-only | — | — | UC-062 Step 2 |
| 2 | Thời điểm bắt đầu/kết thúc sơ chế và nấu | Time picker (M3) | Bắt buộc | — | UC-062 Step 2 |
| 3 | Checklist điều kiện vệ sinh (nhân viên, thiết bị, khu vực sơ chế) | CheckboxListTile | Bắt buộc đủ mục — có thể điền offline | BR-095, BR-196 | UC-062 Step 2 |
| 4 | Nhiệt độ lõi thức ăn sau nấu | TextField số (°C) | Phải ≥ 75°C trong ≥ 15 giây | BR-093 | UC-062 Step 2, MSG-017 nếu không đạt |
| 5 | Ảnh món ăn sau khi nấu | Camera capture | Bắt buộc — cho phép offline (BR-196) | BR-090, BR-196 | Tên màn hình gốc "Upload Image Cooked Food" |
| 6 | GPS + timestamp | Tự động ghi nhận | — | BR-079 | UC-062 Step 2 |
| 7 | Nút "Nộp Bước 2" | FilledButton (cố định cuối màn) | Nếu nhiệt độ < 75°C → chặn, MSG-017, tạo Corrective Action; nếu đạt → mở khoá Step 3 | BR-097 | UC-052 Normal Flow bước 3 |

### Trạng thái màn hình
- Loading: `Shimmer` khi tải thông tin phiên.
- Empty: Không áp dụng.
- Error: `AlertDialog` MSG-017 khi nhiệt độ chưa đạt — chặn, tạo Corrective Action Request.
- Success: `SnackBar` MSG-043 (online) hoặc MSG-078 + badge "Chờ đồng bộ" (offline) — chuyển sang Step 3.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Three-Step Inspection Dashboard, chỉ khả dụng sau khi Step 1 = Passed. Thoát: nộp thành công → Step 3 (Inspect Meals Before Serving Screen).
**Bố cục**: Camera chụp ảnh món ăn ưu tiên đầu form, input nhiệt độ nổi bật (số lớn, cảnh báo màu ngay khi < 75°C), checklist vệ sinh bên dưới, nút "Nộp Bước 2" cố định cuối màn.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Upload Image Cooked Food Screen (Step 2), nền tảng Mobile, actor Service Caterers.
Bố cục: camera chụp ảnh món ăn ưu tiên đầu form, input nhiệt độ lõi nổi bật, checklist vệ sinh, khối GPS/timestamp, nút Nộp Bước 2 cố định dưới cùng.
Thành phần bắt buộc: cảnh báo nhiệt độ chưa đạt 75°C (MSG-017) hiển thị màu danger ngay khi nhập, badge "Chờ đồng bộ" nếu offline.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error (chặn + Corrective Action), success (online/offline).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3542 (UC-062, đoạn mô tả "Conduct Step 2") — xem ghi chú khoảng trống nguồn ở trên
- business_rules.md BR-090, BR-091, BR-093, BR-095, BR-097, BR-196
- error_message.md MSG-017, MSG-078
