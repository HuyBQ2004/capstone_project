# Screen Spec — Submit Allergy Information

**Actor**: Parent | **UC liên quan**: UC-084 - Submit Health & Allergy Information | **Status**: Draft
**BR liên quan**: BR-031 (liên kết học sinh–phụ huynh), BR-172 (bảo vệ toàn vẹn nhật ký), BR-066 (danh mục chế độ ăn đặc biệt)
**MSG liên quan**: MSG-004, MSG-005, MSG-036 (cập nhật hồ sơ dị ứng thành công)

> Đây là màn "Add/Edit Allergy" (Report 3 §3.3.2.4) ở **chế độ tạo mới (Submit)** — khác với `06_UpdateAllergyInformation.md` (chế độ chỉnh sửa hồ sơ đã có). Cùng 1 form UI, khác action/endpoint theo đúng UC-084 (tạo mới, trạng thái "Pending Verification") vs UC-093 (cập nhật, giữ nguyên trạng thái "Pending Verification" sau khi sửa).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Allergen Category Dropdown | Dropdown | Bắt buộc chọn danh mục (Peanuts, Seafood...) | BR-066 | |
| 2 | Specific Allergen Name Input | Input (chỉ hiện khi chọn "Other") | Bắt buộc nếu Other | — | |
| 3 | Severity Level Radio Group | Radio group | Mild/Moderate/Severe | BR-031 | |
| 4 | Upload Medical Proof Component | File upload (camera/gallery) | **Bắt buộc nếu Severity = Severe** | BR-031 | MSG-036 nếu thiếu khi Severe (UC-084 AF1 — chặn submit) |
| 5 | Save Allergy Button | Button (primary, full-width) | Validate đầy đủ trước khi lưu | BR-031 | Lưu với trạng thái "Pending Verification", thông báo Medical Staff |

### Trạng thái màn hình
- Loading: Spinner khi submit.
- Empty: không áp dụng (form tạo mới).
- Error: `SnackBar` `danger` + MSG-004/005; chặn submit nếu Severe thiếu tài liệu (không có MSG code riêng cho AF1 trong `error_message.md` — dùng MSG-005 dạng validation hoặc [Giả định — cần bổ sung MSG riêng]).
- Success: `SnackBar` `success` (MSG-036).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Student Health Information ("Manage Allergy List" khi chưa có allergen nào). Ra: về Student Health Information sau khi lưu.
**Bố cục**: AppBar (back) → form 1 cột: Allergen Category → Specific Name (nếu Other) → Severity Radio → Upload Medical Proof → Save Allergy Button cố định đáy.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Submit Allergy Information, nền tảng Mobile, actor Parent.
Bố cục: AppBar + form 1 cột (Allergen Category, Specific Name, Severity Radio, Upload Medical Proof) + nút Save Allergy cố định đáy.
Thành phần bắt buộc: Allergen Category Dropdown, Specific Allergen Name Input, Severity Level Radio Group, Upload Medical Proof Component, Save Allergy Button.
Áp dụng Design System: Severe = danger (đỏ, mặc định được chọn theo wireframe gốc).
Trạng thái cần thể hiện: loading, error (MSG-005/036 thiếu tài liệu), success (MSG-036).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6032–6049 (Add/Edit Allergy, field-by-field gốc), dòng 2087–2107 (UC-084 Normal/Alternative Flow).
- `business_rules.md` BR-031, BR-066, BR-172; `error_message.md` MSG-036.
