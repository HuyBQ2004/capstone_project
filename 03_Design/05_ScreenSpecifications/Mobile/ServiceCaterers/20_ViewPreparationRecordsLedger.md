# Screen Spec — View Preparation Records Ledger

**Actor**: Service Caterers | **UC liên quan**: Không có UC riêng — màn hình tổng hợp xuất ra sau khi hoàn tất Step 3 (`04_Service_Caterers_Screen_Flow.md`: `Step3 --> PrepLedger`), liên quan chặt tới UC-066 và UC-052 | **Status**: Draft
**BR liên quan**: BR-076, BR-093, BR-099
**MSG liên quan**: MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách phiên sơ chế/sản xuất đã hoàn tất | Card list | Mỗi card: Mẻ, Món, Nhân viên chịu trách nhiệm, Ngày | — | Tổng hợp từ UC-066 + Step 1-3 |
| 2 | Liên kết số lô nguyên liệu đã dùng | Read-only link | Truy xuất nguồn gốc | BR-076 | — |
| 3 | Trạng thái đồng ký số của phiên | Chip | Đã ký / Chưa ký đủ | BR-099 | Kế thừa từ Step 3 |
| 4 | Bộ lọc theo ngày/món | Filter chip row | Tuỳ chọn | — | — |
| 5 | Xem chi tiết 1 bản ghi | Điều hướng chi tiết (read-only) | — | BR-093 | — |

### Trạng thái màn hình
- Loading: `Shimmer` cho danh sách.
- Empty: Chưa có phiên nào hoàn tất trong khoảng lọc.
- Error: `SnackBar` MSG-004/MSG-005 nếu tải thất bại.
- Success: Không áp dụng.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ cuối Step 3 hoặc từ Three-Step Inspection Dashboard. Thoát: back về Dashboard.
**Bố cục**: AppBar + filter chip row + Card list cuộn dọc (pull-to-refresh).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình View Preparation Records Ledger, nền tảng Mobile, actor Service Caterers.
Bố cục: AppBar + filter chip row + Card list (Mẻ, Món, Nhân viên, Ngày, Trạng thái ký).
Thành phần bắt buộc: chip trạng thái đồng ký số.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading (shimmer), empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/04_Service_Caterers_Screen_Flow.md` (nhánh Step3 --> PrepLedger)
- Report 3.md dòng 3221–3239 (UC-066), dòng 3487–3549 (UC-052/UC-062)
- business_rules.md BR-076, BR-093, BR-099
