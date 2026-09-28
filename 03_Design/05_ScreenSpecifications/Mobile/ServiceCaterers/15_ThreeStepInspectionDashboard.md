# Screen Spec — Three-Step Inspection Dashboard

**Actor**: Service Caterers | **UC liên quan**: UC-052 - Manage Three-Step Food Inspection (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-091, BR-092, BR-093, BR-094, BR-095, BR-096, BR-097, BR-098, BR-099
**MSG liên quan**: MSG-016 (Error), MSG-017 (Error), MSG-043 (Success), MSG-056 (Warning), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách phiên sản xuất bữa ăn đang hoạt động | Card list | Mỗi card: Mẻ sản xuất, Bước hiện tại (1/2/3), Trạng thái | — | UC-052 Trigger + Normal Flow |
| 2 | Trạng thái từng bước kiểm tra | Chip (Chưa bắt đầu/Đang thực hiện/Passed/Failed) | Bước 3 bị khoá cho tới khi Bước 2 = Passed | BR-093 | UC-052 Normal Flow bước 3 |
| 3 | Nút "Bắt đầu/Tiếp tục Kiểm tra" | FilledButton | Điều hướng tới đúng Step 1/2/3 | — | UC-052 Normal Flow |
| 4 | Trạng thái đồng ký số (co-sign) Catering Manager + School Representative | Chip kép | Bắt buộc cả 2 bên ký trước khi phê duyệt phục vụ | BR-099 | UC-052 Normal Flow bước 5 |
| 5 | Cảnh báo thất bại kiểm tra + Corrective Action liên kết | MaterialBanner | Hiện khi có bước Failed | BR-097, BR-098 | UC-052 Alternative Flow AF1, MSG-056 |

### Trạng thái màn hình
- Loading: `Shimmer` cho danh sách phiên sản xuất.
- Empty: Chưa có phiên sản xuất nào trong ngày.
- Error: MSG-016, MSG-017, `AlertDialog`/`SnackBar` màu `danger`.
- Success: `SnackBar` MSG-043 khi hoàn tất nộp checklist.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Service Caterers Dashboard → thẻ "Kiểm tra 3 Bước" trong Grid Menu Items (kênh chính tại bếp — `04_Service_Caterers_Screen_Flow.md`). Điều hướng tiếp: Step 1/2/3, View Preparation Records Ledger, View Inspection Records Log.
**Bố cục**: AppBar + Card list cuộn dọc (pull-to-refresh), mỗi card hiện Mẻ/Bước hiện tại/Trạng thái/nút hành động; FAB "Bắt đầu Kiểm tra Mới" nếu có phiên chưa khởi tạo.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Three-Step Inspection Dashboard, nền tảng Mobile, actor Service Caterers.
Bố cục: AppBar + Card list (Mẻ, Bước hiện tại, Trạng thái chip, Co-sign, nút Bắt đầu/Tiếp tục Kiểm tra), FAB nếu cần.
Thành phần bắt buộc: chip trạng thái 4 giá trị, banner cảnh báo khi có bước Failed (MSG-056).
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading (shimmer), empty, error (MSG-016/017), success (MSG-043).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3487–3505 (UC-052 - Manage Three-Step Food Inspection)
- business_rules.md BR-091–BR-099
- error_message.md MSG-016, MSG-017, MSG-043, MSG-056
