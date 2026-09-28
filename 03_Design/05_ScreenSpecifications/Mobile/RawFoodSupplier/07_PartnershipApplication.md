# Screen Spec — Partnership Application

**Actor**: Raw Food Supplier | **UC liên quan**: UC-152 - Submit Supplier Application (Report 3 §3.2, dòng 2639–2658); liên quan UC-155, UC-170 | **Status**: Draft
**BR liên quan**: BR-040, BR-046, BR-047, BR-048, BR-010
**MSG liên quan**: MSG-004, MSG-005, MSG-007, MSG-008, MSG-009, MSG-013, MSG-025, MSG-028

> **Ghi chú đối chiếu nguồn**: xem giải thích lệch BR-050 ở bản Web cùng tên (`Web/RawFoodSupplier/07_PartnershipApplication.md`).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Apply for Onboarding" | — | |
| 2 | Company Name | Input text | Bắt buộc | BR-040 | |
| 3 | Tax ID | Input text | Bắt buộc, đúng định dạng (MSG-007 nếu sai) | BR-040 | |
| 4 | Business Address | Input text | Bắt buộc | BR-040 | |
| 5 | Business License (file) | File upload (camera/thư viện) | Bắt buộc, PDF/JPG/PNG, ≤10MB (MSG-008/009) | BR-040 | |
| 6 | Food Safety Certificate(s) (file, có thể nhiều) | File upload (multi) | Bắt buộc ≥1, không hết hạn (BR-048, MSG-013) | BR-046, BR-047, BR-048 | |
| 7 | Ngày hết hạn từng chứng nhận | Date picker | Bắt buộc theo mỗi file | BR-047 | |
| 8 | "Submit" Button | Button | Validate toàn bộ trước khi gửi | BR-040, BR-046, BR-047, BR-048 | |
| 9 | "Cancel" Button | Button | Huỷ, quay về School Partnerships (03) | — | |

### Trạng thái màn hình
- Loading: Shimmer form khi tải lại dữ liệu đơn cũ (Update/Resubmit UC-170).
- Empty: N/A.
- Error: MSG-007, MSG-008, MSG-009, MSG-013, MSG-025.
- Success: MSG-028 → điều hướng Application Status (08).

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ School Partnerships (03) qua nút "submit application". Tương ứng node `PartnerApp["Supplier Partnership Application Form"]` trong `06_Raw_Food_Supplier_Screen_Flow.md`, dẫn tiếp tới `VD["Verification Documents Upload"]`. Điều hướng đi: Application Status (08).
**Bố cục**: Form nhiều trang riêng (không Stepper ngang, `Layout_Guidelines.md` §2.2) — Trang 1: Company Name/Tax ID/Business Address; Trang 2: Upload Business License + Food Safety Certificate + ngày hết hạn (dùng `image_picker`). Nút "Tiếp theo/Quay lại" cố định dưới cùng, nút Submit ở trang cuối.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Partnership Application, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: form 2 trang riêng, nút Tiếp theo/Quay lại cố định dưới cùng.
Component bắt buộc dùng: TextField, DatePicker (M3), image_picker upload + preview, FilledButton.
Thành phần bắt buộc: Company Name, Tax ID, Business Address, Business License upload, Food Safety Certificate upload (multi) + ngày hết hạn, nút Submit/Cancel.
Áp dụng Design System: seed color #0E7490, Material Design 3.
Trạng thái cần thể hiện: error (MSG-007/008/009/013), success (MSG-028).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 2639–2658 (UC-152).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-010, BR-040, BR-046, BR-047, BR-048.
- `02_Requirements/00_BusinessRules/error_message.md` MSG-007, MSG-008, MSG-009, MSG-013, MSG-025, MSG-028.
- `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md`.
