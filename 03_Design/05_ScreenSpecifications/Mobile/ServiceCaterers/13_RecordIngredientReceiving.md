# Screen Spec — Record Ingredient Receiving

**Actor**: Service Caterers | **UC liên quan**: UC-060 - Record Ingredient Receiving (Report 3 §3.2, bước cuối "Send the Receipt of Goods") | **Status**: Draft
**BR liên quan**: BR-050, BR-071, BR-073, BR-074, BR-076, BR-079, BR-080
**MSG liên quan**: MSG-041 (Success), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tóm tắt toàn bộ phiên nhận hàng (nhà cung cấp, chứng nhận, kết quả kiểm tra, ảnh bằng chứng, GPS/timestamp) | Read-only summary card | Tổng hợp từ các bước trước | BR-071, BR-073, BR-074, BR-079 | UC-060 Normal Flow bước 6 |
| 2 | Kiểm tra nhiệt độ chuỗi lạnh (cold chain) | Read-only kết quả | Cảnh báo nếu > 5°C | BR-080 | UC-060 Alternative Flow AF1 |
| 3 | Số lô liên kết truy xuất nguồn gốc | Read-only text | Ghi nhận số lô gắn với thực đơn/phiên sản xuất | BR-076 | — |
| 4 | Nút "Xác nhận Hoàn tất Phiên Nhận hàng" | FilledButton (cố định cuối màn) | Tạo bản ghi batch traceability, cập nhật kho | — | UC-060 Normal Flow bước 6 |

### Trạng thái màn hình
- Loading: `Shimmer` khi tổng hợp dữ liệu phiên.
- Empty: Không áp dụng.
- Error: `AlertDialog` MSG-004/MSG-005 nếu lưu thất bại.
- Success: `SnackBar` MSG-041.

---

## Platform Layout — Mobile

**Entry/Exit points**: Bước cuối trong Receive Ingredients Workspace. Thoát: sau khi xác nhận, điều hướng tự động sang Store Ingredients Form.
**Bố cục**: Trang tóm tắt 1 cột, mỗi nhóm dữ liệu là 1 `Card` (Nhà cung cấp, Chứng nhận, Kết quả kiểm tra, Bằng chứng), nút xác nhận cố định cuối màn.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Record Ingredient Receiving, nền tảng Mobile, actor Service Caterers.
Bố cục: trang tóm tắt 1 cột nhiều Card (Nhà cung cấp, Chứng nhận, Kết quả kiểm tra, Bằng chứng, GPS/timestamp), nút Xác nhận Hoàn tất Phiên Nhận hàng cố định dưới cùng.
Thành phần bắt buộc: cảnh báo cold chain nếu vượt ngưỡng nhiệt độ.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error, success (MSG-041).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3155–3174 (UC-060 - Record Ingredient Receiving)
- business_rules.md BR-050, BR-071, BR-073, BR-074, BR-076, BR-079, BR-080
- error_message.md MSG-041
