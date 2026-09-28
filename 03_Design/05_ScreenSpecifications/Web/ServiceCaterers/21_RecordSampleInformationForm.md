# Screen Spec — Record Sample Information Form

**Actor**: Service Caterers | **UC liên quan**: UC-069 - Register Food Sample (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-106, BR-107, BR-108, BR-109, BR-111, BR-114, BR-203
**MSG liên quan**: MSG-019 (Error — trọng lượng mẫu dưới 100g), MSG-020 (Error — GPS/nhiệt độ ngoài phạm vi theo trích dẫn UC-069 AF2), MSG-044 (Success), MSG-004/MSG-005 (Error)

> **Lưu ý mâu thuẫn nguồn**: `Report 3.md` UC-069 Alternative Flow AF2 ("GPS Location Verification Fails") ghi hiển thị **MSG-072**, nhưng theo `error_message.md`, MSG-072 là thông báo Success "Special meal plan saved successfully" — không liên quan lỗi GPS. Mã lỗi đúng ngữ cảnh "GPS ngoài khu vực cho phép" theo `error_message.md` là **MSG-015** hoặc **MSG-020** (nhiệt độ/GPS ngoài phạm vi tại retention). Screen Spec này dùng **MSG-020** cho lỗi GPS ngoài khu vực bếp trường (gần nghĩa nhất với ngữ cảnh retention), và ghi rõ **[Giả định — cần team xác nhận]** số MSG chính xác cho lỗi GPS tại bước đăng ký mẫu.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Xác minh GPS trong khu vực bếp trường | Kiểm tra tự động | Chặn đăng ký nếu ngoài khu vực | BR-109 | UC-069 Normal Flow bước 2, AF2 |
| 2 | Bữa ăn / Món / Tổng số suất | Read-only (kế thừa từ phiên đã qua 3 bước) | — | — | UC-069 Normal Flow bước 3 |
| 3 | Trọng lượng mẫu (gram) | Input số | Bắt buộc ≥ 100g (đặc) / ≥150ml (lỏng) | BR-203 | UC-069 Normal Flow bước 3, MSG-019 nếu dưới ngưỡng *(sửa 2026-07-02: trước đây gán nhầm BR-107 — BR-107 thực chất là quy tắc chụp ảnh mẫu, không liên quan khối lượng; đã repoint sang BR-203)* |
| 4 | Mô tả hộp/túi đựng mẫu | Textarea | Bắt buộc | — | UC-069 Normal Flow bước 3 |
| 5 | Nhiệt độ tủ lưu mẫu tại thời điểm lưu | Input số (°C) | Trong khoảng 2–8°C | BR-109, BR-110 | UC-069 Normal Flow bước 3 |
| 6 | Thời điểm thu mẫu | Time picker | Bắt buộc | — | UC-069 Normal Flow bước 3 |
| 7 | In nhãn mẫu (Sample No 4) | Button "In nhãn" | Tự động sinh nội dung: Bữa ăn, Tên mẫu, Thời điểm lấy mẫu, Người lấy mẫu | BR-106 | UC-069 Normal Flow bước 4 |
| 8 | Ảnh hộp mẫu đã dán nhãn | Upload ảnh | Bắt buộc | BR-107 | UC-069 Normal Flow bước 5 |
| 9 | Chữ ký số người đăng ký (cột 11 Mẫu số 5) | Chữ ký số | Bắt buộc | — | UC-069 Normal Flow bước 5 |
| 10 | Nút "Xác nhận Lưu mẫu" | Button | Khởi động đồng hồ lưu mẫu 72 giờ (BR-111) | BR-111 | UC-069 Normal Flow bước 6 |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải thông tin phiên đã qua kiểm tra.
- Empty: Không áp dụng.
- Error: MSG-019 (trọng lượng < 100g); MSG-020 (GPS/nhiệt độ ngoài phạm vi, xem lưu ý mâu thuẫn nguồn ở trên).
- Success: `Toast` MSG-044 — "Đăng ký lưu mẫu thực phẩm thành công, đồng hồ lưu trữ lạnh đã khởi động".

---

## Platform Layout — Web

**Entry/Exit points**: Từ Food Sample Retention Dashboard (nút "Đăng ký Mẫu mới") — sau khi hoàn tất Three-Step Inspection. Thoát: lưu xong → Food Sample Detail.
**Bố cục**: Form dài trong `Dialog`/trang riêng, khối in nhãn ở giữa form (xem trước nhãn trước khi in), upload ảnh + chữ ký số cuối form. GPS xác nhận qua Geolocation API trình duyệt (secondary path).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Record Sample Information Form, nền tảng Web, actor Service Caterers.
Bố cục: form dài gồm thông tin bữa ăn/món, input trọng lượng mẫu, mô tả hộp đựng, nhiệt độ tủ lưu, khối xem trước + in nhãn mẫu, upload ảnh, chữ ký số, nút Xác nhận Lưu mẫu.
Thành phần bắt buộc: cảnh báo MSG-019 nếu trọng lượng < 100g, cảnh báo GPS/nhiệt độ ngoài phạm vi.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error (MSG-019/MSG-020), success (MSG-044).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3735–3749 (UC-069 - Register Food Sample)
- business_rules.md BR-106, BR-107, BR-108, BR-109, BR-111, BR-114, BR-203
- error_message.md MSG-019, MSG-020, MSG-044
