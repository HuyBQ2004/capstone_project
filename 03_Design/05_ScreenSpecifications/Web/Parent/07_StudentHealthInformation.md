# Screen Spec — Student Health Information

**Actor**: Parent | **UC liên quan**: UC-050 - View List Student Health Information | **Status**: Draft
**BR liên quan**: BR-031
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Student Summary Card | Card | Họ tên, tuổi, lớp | BR-031 | |
| 2 | Height/Weight/Blood Type/BMI Card | Metric card grid (4 ô) | Chỉ số sức khỏe | — | |
| 3 | Allergy Profile Summary Table | `Table` hoặc Card | Allergen + mức độ | BR-031 | |
| 4 | Verification Status Badge | Badge | Pending/Verified | BR-031 | |
| 5 | Manage Health Details Button | Button (primary) | → Submit/Update Health Information | — | |
| 6 | Manage Allergy List Button | Button (primary) | → Submit/Update Allergy Information | — | |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Empty: "Chưa khai báo thông tin sức khỏe" + nút "Khai báo ngay".
- Error: `Alert variant="destructive"` + MSG-004/005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Sức khỏe & Dị ứng" (`10_Parent_Screen_Flow_Web.md`).
**Bố cục**: Topbar + Sidebar + Content: Student Summary Card + 4-card metric grid + Allergy Profile Table + 2 nút hành động góc trên phải.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Student Health Information, nền tảng Web, actor Parent.
Bố cục: Topbar + Sidebar + Student Summary Card + 4-card metric grid + Allergy Profile Table + 2 nút hành động.
Thành phần bắt buộc: Student Summary Card, 4 Metric Card, Allergy Profile Summary Table, Verification Status Badge, Manage Health Details Button, Manage Allergy List Button.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 5989–6011, dòng 1999–2019 (UC-050).
- `business_rules.md` BR-031.
