# Screen Spec — Food Sample Detail

**Actor**: Service Caterers | **UC liên quan**: Không có UC riêng — màn hình chi tiết mở từ Record Sample Information Form (`04_Service_Caterers_Screen_Flow.md`: `SampleForm --> SampleDetail`), dữ liệu nguồn từ UC-069 (Register Food Sample) và UC-118 (View Food Sample Records) | **Status**: Draft
**BR liên quan**: BR-106, BR-108, BR-109, BR-111, BR-114
**MSG liên quan**: MSG-059 (Warning — tủ mở/nhiệt độ dao động), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thông tin mẫu (tên món, ngày sản xuất, ngày lưu mẫu, số tham chiếu lô) | Read-only card | — | BR-106 | UC-069 Postconditions |
| 2 | Trọng lượng mẫu, nơi lưu (tủ số mấy) | Read-only | — | BR-107, BR-109 | — |
| 3 | Đồng hồ đếm ngược thời gian lưu mẫu (72 giờ) | Countdown/Progress | Tự động tính từ BR-111 | BR-111 | UC-069 Postconditions |
| 4 | Trạng thái mẫu | Badge | Retained / Retrieved / Disposed | BR-114 | UC-118 |
| 5 | Ảnh nhãn + hộp mẫu đã lưu | Ảnh xem lại (read-only) | — | BR-107 | — |
| 6 | Cảnh báo nếu tủ lưu mẫu mở/nhiệt độ dao động trong thời gian lưu | Alert banner | Hiện nếu có sự kiện bất thường ghi nhận | BR-110 | MSG-059 |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải chi tiết mẫu.
- Empty: Không áp dụng.
- Error: `Alert variant="destructive"` MSG-004/MSG-005 nếu tải thất bại.
- Success: Không áp dụng (màn hình chỉ xem).

---

## Platform Layout — Web

**Entry/Exit points**: Mở từ Record Sample Information Form sau khi lưu mẫu thành công, hoặc từ danh sách trong Food Sample Retention Dashboard. Thoát: back về Dashboard.
**Bố cục**: Trang chi tiết dạng 2 cột — thông tin mẫu bên trái, ảnh + đồng hồ đếm ngược bên phải, banner cảnh báo (nếu có) trên cùng.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Food Sample Detail, nền tảng Web, actor Service Caterers.
Bố cục: 2 cột — thông tin mẫu + badge trạng thái bên trái, ảnh nhãn/hộp mẫu + đồng hồ đếm ngược 72 giờ bên phải, banner cảnh báo nhiệt độ nếu có.
Thành phần bắt buộc: badge trạng thái 3 giá trị (Retained/Retrieved/Disposed), cảnh báo MSG-059.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/04_Service_Caterers_Screen_Flow.md` (nhánh SampleForm --> SampleDetail)
- Report 3.md dòng 3735–3749 (UC-069), dòng 3775–3793 (UC-118 - View Food Sample Records)
- business_rules.md BR-106, BR-108, BR-109, BR-110, BR-111, BR-114
- error_message.md MSG-059
