# Screen Spec — Update Allergy Information

**Actor**: Parent | **UC liên quan**: UC-093 - Update Health & Allergy Information | **Status**: Draft
**BR liên quan**: BR-031 (liên kết học sinh–phụ huynh)
**MSG liên quan**: MSG-004, MSG-005, MSG-036

> Cùng UI với `05_SubmitAllergyInformation.md` (Add/Edit Allergy, Report 3 dòng 6032–6049) nhưng ở **chế độ chỉnh sửa** — form hiện sẵn dữ liệu allergen đã khai báo trước đó, action lưu là "Update" thay vì "Create". Theo UC-093: sau khi sửa, hồ sơ chuyển lại trạng thái "Pending Verification" và Medical Staff được thông báo lại.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Allergen Category Dropdown | Dropdown (pre-filled) | Bắt buộc | — | |
| 2 | Specific Allergen Name Input | Input (pre-filled, điều kiện Other) | Bắt buộc nếu Other | — | |
| 3 | Severity Level Radio Group | Radio group (pre-filled) | Mild/Moderate/Severe | BR-031 | Nếu đổi sang Severe → bắt buộc upload lại tài liệu y tế mới (UC-093 bước 3) |
| 4 | Upload Medical Proof Component | File upload | Bắt buộc nếu chuyển sang Severe | BR-031 | |
| 5 | Save Allergy Button | Button (primary, full-width) | Validate, lưu về "Pending Verification" | BR-031 | MSG-036 |

### Trạng thái màn hình
- Loading: Spinner khi submit; `Shimmer` khi tải dữ liệu cũ.
- Empty: không áp dụng.
- Error: `SnackBar` `danger` + MSG-004/005; chặn lưu nếu thiếu tài liệu khi chuyển Severe (UC-093 Exceptions).
- Success: `SnackBar` `success` (MSG-036).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Student Health Information ("Manage Allergy List", khi đã có allergen). Ra: về Student Health Information.
**Bố cục**: Giống `05_SubmitAllergyInformation.md` nhưng field đã điền sẵn dữ liệu hiện có.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Update Allergy Information, nền tảng Mobile, actor Parent.
Bố cục: AppBar + form 1 cột đã điền sẵn dữ liệu (Allergen Category, Specific Name, Severity Radio, Upload Medical Proof) + nút Save Allergy cố định đáy.
Thành phần bắt buộc: Allergen Category Dropdown, Specific Allergen Name Input, Severity Level Radio Group, Upload Medical Proof Component, Save Allergy Button.
Trạng thái cần thể hiện: loading, error (MSG-004/005), success (MSG-036).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6032–6049 (wireframe gốc dùng chung), dòng 2109–2129 (UC-093 Normal Flow).
- `business_rules.md` BR-031; `error_message.md` MSG-036.
