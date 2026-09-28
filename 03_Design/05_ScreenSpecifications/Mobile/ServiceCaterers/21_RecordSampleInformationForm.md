# Screen Spec — Record Sample Information Form

**Actor**: Service Caterers | **UC liên quan**: UC-069 - Register Food Sample (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-106, BR-107, BR-108, BR-109, BR-111, BR-114, BR-196, BR-203
**MSG liên quan**: MSG-019 (Error), MSG-020 (Error, xem lưu ý), MSG-044 (Success), MSG-078 (Success — offline), MSG-004/MSG-005 (Error)

> **Lưu ý mâu thuẫn nguồn**: giống bản Web — `Report 3.md` UC-069 AF2 ghi MSG-072 cho lỗi GPS, nhưng MSG-072 thực tế trong `error_message.md` là thông báo Success khác (lưu kế hoạch bữa ăn đặc biệt). Dùng **MSG-020** thay thế, **[Giả định — cần team xác nhận]**.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Xác minh GPS trong khu vực bếp trường | Kiểm tra tự động (GPS thiết bị) | Chặn đăng ký nếu ngoài khu vực | BR-109 | UC-069 Normal Flow bước 2, AF2 |
| 2 | Bữa ăn / Món / Tổng số suất | Read-only | — | — | UC-069 Normal Flow bước 3 |
| 3 | Trọng lượng mẫu (gram) | TextField số | Bắt buộc ≥ 100g (đặc) / ≥150ml (lỏng) | BR-203 | MSG-019 nếu dưới ngưỡng *(sửa 2026-07-02: trước đây gán nhầm BR-107, đã repoint sang BR-203)* |
| 4 | Mô tả hộp/túi đựng mẫu | TextField multiline | Bắt buộc | — | UC-069 Normal Flow bước 3 |
| 5 | Nhiệt độ tủ lưu mẫu tại thời điểm lưu | TextField số (°C) | Trong khoảng 2–8°C | BR-109, BR-110 | UC-069 Normal Flow bước 3 |
| 6 | Thời điểm thu mẫu | Time picker (M3) | Bắt buộc | — | UC-069 Normal Flow bước 3 |
| 7 | In nhãn mẫu (Sample No 4) | Button "In nhãn" (kết nối máy in nhiệt Bluetooth nếu có) | Tự động sinh nội dung | BR-106 | UC-069 Normal Flow bước 4 |
| 8 | Ảnh hộp mẫu đã dán nhãn | Camera capture | Bắt buộc — cho phép offline (BR-196) | BR-107, BR-196 | UC-069 Normal Flow bước 5 |
| 9 | Chữ ký số người đăng ký | Signature pad | Bắt buộc | — | UC-069 Normal Flow bước 5 |
| 10 | Nút "Xác nhận Lưu mẫu" | FilledButton (cố định cuối màn) | Khởi động đồng hồ lưu mẫu 72 giờ | BR-111 | UC-069 Normal Flow bước 6 |

### Trạng thái màn hình
- Loading: `Shimmer` khi tải thông tin phiên.
- Empty: Không áp dụng.
- Error: MSG-019 (trọng lượng < 100g); MSG-020 (GPS/nhiệt độ ngoài phạm vi).
- Success: `SnackBar` MSG-044 (online) hoặc MSG-078 + badge "Chờ đồng bộ" (offline cho phần chụp ảnh/nhãn, xác minh GPS vẫn cần online theo BR-109/BR-197).

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Food Sample Retention Dashboard (FAB "Đăng ký Mẫu mới"). Thoát: lưu xong → Food Sample Detail.
**Bố cục**: Camera + GPS ưu tiên đầu form, input trọng lượng/nhiệt độ nổi bật, khối in nhãn (kết nối máy in Bluetooth nếu có) giữa form, chữ ký số cuối form, nút "Xác nhận Lưu mẫu" cố định cuối màn.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Record Sample Information Form, nền tảng Mobile, actor Service Caterers.
Bố cục: camera + GPS ưu tiên đầu form, input trọng lượng/nhiệt độ, khối in nhãn mẫu, camera chụp ảnh hộp đã dán nhãn, chữ ký số, nút Xác nhận Lưu mẫu cố định dưới cùng.
Thành phần bắt buộc: cảnh báo MSG-019 nếu trọng lượng < 100g, cảnh báo GPS/nhiệt độ ngoài phạm vi, badge "Chờ đồng bộ" nếu offline.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error, success (online/offline).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3735–3749 (UC-069 - Register Food Sample)
- business_rules.md BR-106, BR-107, BR-108, BR-109, BR-111, BR-114, BR-196, BR-203
- error_message.md MSG-019, MSG-020, MSG-044, MSG-078
