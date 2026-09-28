# Screen Spec — Food Sample Detail

**Actor**: Service Caterers | **UC liên quan**: Không có UC riêng — màn hình chi tiết mở từ Record Sample Information Form (`04_Service_Caterers_Screen_Flow.md`: `SampleForm --> SampleDetail`), dữ liệu nguồn từ UC-069 và UC-118 | **Status**: Draft
**BR liên quan**: BR-106, BR-108, BR-109, BR-111, BR-114
**MSG liên quan**: MSG-059 (Warning), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thông tin mẫu (tên món, ngày sản xuất, ngày lưu mẫu, số tham chiếu lô) | Read-only card | — | BR-106 | UC-069 Postconditions |
| 2 | Trọng lượng mẫu, nơi lưu (tủ số mấy) | Read-only | — | BR-107, BR-109 | — |
| 3 | Đồng hồ đếm ngược thời gian lưu mẫu (72 giờ) | Countdown/Progress indicator | Tự động tính | BR-111 | UC-069 Postconditions |
| 4 | Trạng thái mẫu | Chip | Retained / Retrieved / Disposed | BR-114 | UC-118 |
| 5 | Ảnh nhãn + hộp mẫu đã lưu | Ảnh xem lại (read-only) | — | BR-107 | — |
| 6 | Cảnh báo nếu tủ lưu mẫu mở/nhiệt độ dao động | MaterialBanner | Hiện nếu có sự kiện bất thường | BR-110 | MSG-059 |

### Trạng thái màn hình
- Loading: `Shimmer` khi tải chi tiết mẫu.
- Empty: Không áp dụng.
- Error: `SnackBar` MSG-004/MSG-005 nếu tải thất bại.
- Success: Không áp dụng.

---

## Platform Layout — Mobile

**Entry/Exit points**: Mở từ Record Sample Information Form sau khi lưu mẫu, hoặc từ danh sách trong Food Sample Retention Dashboard. Thoát: back về Dashboard.
**Bố cục**: Trang chi tiết 1 cột — ảnh nhãn/hộp mẫu trên cùng, đồng hồ đếm ngược nổi bật, thông tin mẫu dạng list bên dưới, banner cảnh báo trên cùng nếu có.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Food Sample Detail, nền tảng Mobile, actor Service Caterers.
Bố cục: 1 cột — ảnh nhãn/hộp mẫu, đồng hồ đếm ngược 72 giờ nổi bật, thông tin mẫu dạng list, chip trạng thái, banner cảnh báo nếu có.
Thành phần bắt buộc: chip trạng thái 3 giá trị, cảnh báo MSG-059.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/04_Service_Caterers_Screen_Flow.md` (nhánh SampleForm --> SampleDetail)
- Report 3.md dòng 3735–3749 (UC-069), dòng 3775–3793 (UC-118 - View Food Sample Records)
- business_rules.md BR-106, BR-108, BR-109, BR-110, BR-111, BR-114
- error_message.md MSG-059
