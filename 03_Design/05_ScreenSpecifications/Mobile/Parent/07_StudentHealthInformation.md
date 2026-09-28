# Screen Spec — Student Health Information

**Actor**: Parent | **UC liên quan**: UC-050 - View List Student Health Information | **Status**: Draft
**BR liên quan**: BR-031 (liên kết học sinh–phụ huynh, xét duyệt thông tin sức khỏe)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Student Summary Card | Card | Họ tên, tuổi, lớp | BR-031 | Nguồn: Report 3 §3.3.2.4 Student Health Hub |
| 2 | Height Card | Metric tile | Chiều cao (cm) | — | |
| 3 | Weight Card | Metric tile | Cân nặng (kg) | — | |
| 4 | Blood Type Card | Metric tile | Nhóm máu | — | |
| 5 | BMI Status Card | Metric tile | Chỉ số BMI + phân loại (Normal/...) | — | |
| 6 | Allergy Profile Summary Card | Card (viền đỏ) | Danh sách allergen + mức độ nghiêm trọng | BR-031 | |
| 7 | Verification Status Badge | Badge | "Pending"/"Verified" | BR-031 | |
| 8 | Manage Health Details Button | Button (primary, full-width) | → Submit/Update Health Information | — | |
| 9 | Manage Allergy List Button | Button (primary, full-width) | → Submit/Update Allergy Information | — | |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: "Chưa khai báo thông tin sức khỏe" + nút "Khai báo ngay" → Submit Health Information.
- Error: `SnackBar` `danger` + MSG-004/005.
- Success: không áp dụng (màn hình tổng quan, chỉ đọc + điều hướng).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Parent Dashboard ("Health Hub" tile) hoặc bottom-nav "Health". Ra: Submit/Update Health Information, Submit/Update Allergy Information.
**Bố cục**: AppBar (back) → Student Summary Card → lưới 4 Metric Card (2x2: Height/Weight/Blood Type/BMI) → Allergy Profile Summary Card → 2 nút hành động full-width → `BottomNavigationBar`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Student Health Information, nền tảng Mobile, actor Parent.
Bố cục: AppBar + Student Summary Card + lưới 4 Metric Card (Height/Weight/Blood Type/BMI) + Allergy Profile Summary Card (viền đỏ) + 2 nút hành động (Manage Health Details/Manage Allergy List) + BottomNavigationBar.
Thành phần bắt buộc: Student Summary Card, Height/Weight/Blood Type/BMI Status Card, Allergy Profile Summary Card, Verification Status Badge, Manage Health Details Button, Manage Allergy List Button.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 5989–6011 (Student Health Hub, field-by-field gốc), dòng 1999–2019 (UC-050 Normal Flow).
- `business_rules.md` BR-031.
