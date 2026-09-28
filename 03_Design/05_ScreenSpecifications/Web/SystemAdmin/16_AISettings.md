# Screen Spec — AI Settings

**Actor**: System Admin | **UC liên quan**: UC-040 (Report 3 §3.2 "Manage External Integrations" — 1 phần, tab Gemini AI) | **Status**: Draft
**BR liên quan**: BR-163 (kiểm tra kết nối tích hợp bên ngoài), BR-164 (mã hóa thông tin xác thực tích hợp)
**MSG liên quan**: MSG-004, MSG-005 (Exceptions gốc); MSG-069 (độ trễ API cao — tham chiếu warning liên quan)

> **Nguồn**: "AI Settings" **không phải** 1 UC độc lập. Normal Flow thật của UC-040 (Manage External Integrations) gộp chung cấu hình 4 dịch vụ: GPS Service, Gmail SMTP, Firebase Cloud Messaging, và Gemini AI trong cùng 1 màn hình "External Integrations" (dạng Tabs). "AI Settings" trong Screen Authorization Matrix nhiều khả năng là 1 tab/filter của màn hình đó — xem `01_System_Admin_Screen_Flow.md`. File này đặc tả riêng tab Gemini AI theo đúng tên gọi trong ma trận, nhưng khuyến nghị implement như 1 tab trong 1 màn "External Integrations" chung, không phải 4 trang riêng biệt.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tabs chọn dịch vụ (GPS/Gmail/Firebase/Gemini AI) | Tabs | — | — | Tab đang mở: "Gemini AI" |
| 2 | API Endpoint | Input (url) | Bắt buộc | — | |
| 3 | OAuth2 Client Credentials / API Key | Input (password, ẩn giá trị) | Bắt buộc, mã hóa khi lưu | BR-164 | |
| 4 | Nút "Test Connection" | Button (secondary) | Phải test thành công trước khi cho phép "Save" | BR-163 | AF1: nếu thất bại, hiển thị lỗi chi tiết và chặn lưu |
| 5 | Nút "Save" | Button (primary) | Disable tới khi Test Connection thành công | BR-163, BR-164 | |
| 6 | Trạng thái kết nối hiện tại | Badge (success/danger) | — | — | |

### Trạng thái màn hình
- Loading: Button "Test Connection"/"Save" chuyển loading.
- Empty: Chưa cấu hình → hiển thị placeholder "Chưa kết nối Gemini AI".
- Error: EX1 thiếu field bắt buộc (MSG-004 dạng validation chung); AF1 Test Connection thất bại → hiển thị lỗi chi tiết (VD "Invalid API key"), chặn Save.
- Success: Lưu cấu hình thành công (mã hóa credentials), ghi audit log bất biến (BR-162 tham chiếu chéo), hiển thị thông báo thành công.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Cấu hình Hệ thống" → "External Integrations" → tab "Gemini AI". Không có màn hình con.
**Bố cục**: Dashboard Shell — Content dùng `Tabs` ngang trên cùng (4 tab GPS/Gmail/Firebase/Gemini AI), nội dung tab là form ngắn (`col-span-6`, căn giữa) gồm endpoint + credentials + nút Test/Save.

## AI Generation Prompt ⭐

```
Tạo wireframe cho tab AI Settings (Gemini AI) trong màn hình External Integrations, nền tảng Web, actor System Admin.
Bố cục: Dashboard Shell — Tabs ngang (GPS/Gmail/Firebase/Gemini AI đang mở), form ngắn giữa trang.
Thành phần bắt buộc: API Endpoint, API Key/OAuth2 credentials (ẩn giá trị), nút Test Connection, nút Save (disable tới khi test thành công), badge trạng thái kết nối.
Áp dụng Design System: badge success/danger cho trạng thái kết nối.
Trạng thái cần thể hiện: error (test connection thất bại), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4777–4793 (Table 3-144 Manage External Integrations Use Cases Specification)
- traceability_matrix.md §1.12 (UC-040 ↔ BR-163/BR-164)
- 03_Design/02_ADR/ADR-003.md (RAG/Gemini pipeline liên quan)
