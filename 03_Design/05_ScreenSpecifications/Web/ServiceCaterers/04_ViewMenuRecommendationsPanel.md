# Screen Spec — View Menu Recommendations Panel (AI Integration)

**Actor**: Service Caterers | **UC liên quan**: UC-075 - Request AI Menu Recommendations (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-064, BR-080, BR-156, BR-157 — **Lưu ý mâu thuẫn nguồn**: bảng Business Rules của UC-075 trong `Report 3.md` (dòng 2905) trích "BR-072", nhưng theo `business_rules.md` (nguồn BR chính thức đã chốt, xem `00_UI_Design_Documentation_Plan.md` mục 2.4) BR-072 là "Chụp ảnh bằng chứng bắt buộc khi nhận hàng" (Ingredient Receiving), không liên quan trần chi phí thực đơn. Quy tắc "trần chi phí" đúng trong `business_rules.md` là **BR-080**. Screen Spec này dùng BR-080 (nguồn đúng), không dùng BR-072 (trích dẫn lỗi thời của Report 3).
**MSG liên quan**: MSG-073 (Warning — AI không khả dụng), MSG-051 (Warning — cờ dị ứng trong đề xuất)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Ngữ cảnh gửi AI (tồn kho, mục tiêu dinh dưỡng, hồ sơ dị ứng, trần chi phí) | Hiển thị tóm tắt read-only | Tự động lấy từ hệ thống, không cho sửa tay ở panel này | BR-062, BR-064 | UC-075 Normal Flow bước 2 |
| 2 | Danh sách Option Card đề xuất món ăn | Card list | Mỗi card: tên món, phân tích dinh dưỡng, danh sách nguyên liệu, cờ cảnh báo dị ứng | BR-156 | UC-075 Normal Flow bước 4; qua hard-filter dị ứng trước khi hiển thị (ghi chú ScreenFlow) |
| 3 | Cờ cảnh báo dị ứng trên từng Option Card | Badge màu `danger` | Hiện khi món chứa allergen trùng hồ sơ học sinh | BR-063, BR-156 | — |
| 4 | Nút "Áp dụng" trên từng Option Card | Button | Áp dụng gợi ý vào Create Menu Workspace đang mở | — | ScreenFlow: `AIPanel -->|"Apply Option"| CreateMenuWS` |
| 5 | Thông báo dịch vụ AI không khả dụng | Alert banner | Hiện khi Gemini AI Service không phản hồi | — | UC-075 Alternative Flow AF1, MSG-073 |
| 6 | Ghi chú lưu trữ hội thoại AI | Text nhỏ cuối panel | Thông báo "Cuộc trao đổi này được lưu trữ để phục vụ tuân thủ" | BR-157 | UC-075 Postconditions |

### Trạng thái màn hình
- Loading: `Skeleton` cho Option Card trong lúc chờ Gemini AI phản hồi.
- Empty: Không có gợi ý phù hợp → thông báo + gợi ý điều chỉnh mục tiêu dinh dưỡng/chi phí.
- Error: `Alert variant="destructive"` MSG-073 khi AI Service không khả dụng, kèm nút "Thử lại" và lối "Tạo thủ công" quay về Create Menu Workspace.
- Success: Không cần toast riêng — áp dụng option cập nhật trực tiếp form Create Menu Workspace.

---

## Platform Layout — Web

**Entry/Exit points**: Mở từ Create Menu Workspace (nút "AI Assist") dạng `Sheet` trượt từ phải, không rời trang chính. Thoát: "Áp dụng" (đóng panel, cập nhật form) hoặc đóng panel không áp dụng gì.
**Bố cục**: `Sheet` chiều rộng ~480px, header tóm tắt ngữ cảnh gửi AI, danh sách Option Card cuộn dọc bên dưới, mỗi card có nút "Áp dụng" góc phải dưới.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình View Menu Recommendations Panel (AI Integration), nền tảng Web, actor Service Caterers.
Bố cục: Sheet trượt từ phải, header tóm tắt ngữ cảnh AI, danh sách Option Card (tên món, dinh dưỡng, nguyên liệu, cờ dị ứng, nút Áp dụng).
Thành phần bắt buộc: badge cảnh báo dị ứng màu đỏ (token danger), ghi chú lưu trữ hội thoại AI.
Áp dụng Design System: xem 00_Foundation/Design_System.md (Alert, Badge), ADR-003 (pipeline AI + hard-filter dị ứng).
Trạng thái cần thể hiện: loading, empty, error (MSG-073), thành công khi áp dụng option.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 2889–2907 (UC-075 - Request AI Menu Recommendations)
- business_rules.md BR-064, BR-156, BR-157, BR-063
- error_message.md MSG-073, MSG-051
- `03_Design/04_ScreenFlow/04_Service_Caterers_Screen_Flow.md` (ghi chú "kết quả AI luôn đi qua hard-filter dị ứng", ADR-003)
