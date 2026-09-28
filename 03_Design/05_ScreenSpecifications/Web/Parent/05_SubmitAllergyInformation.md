# Screen Spec — Submit Allergy Information

**Actor**: Parent | **UC liên quan**: UC-084 - Submit Health & Allergy Information | **Status**: Draft
**BR liên quan**: BR-031, BR-066, BR-172
**MSG liên quan**: MSG-004, MSG-005, MSG-036

> Chế độ tạo mới — xem ghi chú phân biệt Submit/Update ở bản Mobile cùng tên.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Allergen Category Select | `Select` | Bắt buộc | BR-066 | |
| 2 | Specific Allergen Name Input | `Input` (điều kiện "Other") | Bắt buộc nếu Other | — | |
| 3 | Severity Level Radio Group | `RadioGroup` | Mild/Moderate/Severe | BR-031 | |
| 4 | Upload Medical Proof Component | `Input type="file"` | Bắt buộc nếu Severe | BR-031 | MSG-036 nếu thiếu |
| 5 | Save Allergy Button | Button (primary) | Validate trước khi lưu | BR-031 | |

### Trạng thái màn hình
- Loading: Spinner.
- Error: `Alert variant="destructive"` + MSG-005; [Giả định — chưa có MSG riêng cho thiếu tài liệu Severe].
- Success: `Toast` `success` (MSG-036).
- Empty: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Sức khỏe & Dị ứng" (`10_Parent_Screen_Flow_Web.md`), từ Student Health Information.
**Bố cục**: Form ngắn (<8 field) hiện trong `Dialog`/`Sheet` theo `Layout_Guidelines.md` mục 1.2.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Submit Allergy Information, nền tảng Web, actor Parent.
Bố cục: Dialog/Sheet ngắn với form (Allergen Category, Specific Name, Severity Radio, Upload Medical Proof) + nút Save Allergy.
Thành phần bắt buộc: Allergen Category Select, Specific Allergen Name Input, Severity Level Radio Group, Upload Medical Proof Component, Save Allergy Button.
Trạng thái cần thể hiện: loading, error (MSG-005/036), success (MSG-036).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6032–6049, dòng 2087–2107 (UC-084).
- `business_rules.md` BR-031, BR-066, BR-172; `error_message.md` MSG-036.
