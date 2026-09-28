# Screen Spec — Create Classroom

**Actor**: School Administration | **UC liên quan**: UC-018 (Report 3 §3.2 "Create Classroom") | **Status**: Draft
**BR liên quan**: BR-024 (cấp lớp 1-12), BR-025 (sức chứa tối đa 50), BR-192 (lớp phải thuộc năm học Active)
**MSG liên quan**: MSG-004, MSG-005, MSG-010 (vượt sức chứa), MSG-033 (tạo lớp thành công), MSG-081 (chưa có năm học Active)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tên lớp | Input (text) | Bắt buộc, duy nhất trong trường | — | |
| 2 | Cấp lớp (Grade Level) | Select | 1-12 | BR-024 | |
| 3 | Sức chứa tối đa | Input (number) | ≤ 50 (MSG-010 nếu vượt) | BR-025 | |
| 4 | Năm học áp dụng | Select (read-only nếu chỉ có 1 năm Active) | **Bắt buộc phải có năm học Active** — nếu chưa có, chặn tạo lớp (MSG-081) | BR-192 | Ghi chú tích hợp: BR-192 chính thức hoá 2026-07-01, mở rộng Normal Flow gốc của UC-018 |
| 5 | Nút "Tạo lớp" | Button (primary) | Disable tới khi field hợp lệ | — | |

### Trạng thái màn hình
- Loading: Button "Tạo lớp" loading.
- Empty: Không áp dụng (form tạo mới).
- Error: MSG-010 nếu vượt sức chứa/cấp lớp sai; MSG-081 nếu trường chưa có năm học Active (chặn hoàn toàn, gợi ý điều hướng sang `Academic Year List` để kích hoạt năm học trước); MSG-004/MSG-005.
- Success: MSG-033, quay về `Classroom Management`.

---

## Platform Layout — Web
**Entry/Exit points**: Mở từ nút "Create Classroom" trên `Classroom Management`. Sau khi tạo quay về `Classroom Management`.
**Bố cục**: Form ngắn (<8 field) hiện trong `Dialog` giữa màn hình theo `Layout_Guidelines.md` mục 1.2.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Create Classroom, nền tảng Web, actor School Administration.
Bố cục: Dialog giữa màn hình, form 1 cột.
Thành phần bắt buộc: Tên lớp, Cấp lớp (select 1-12), Sức chứa tối đa (≤50), Năm học áp dụng (select/read-only), nút Tạo lớp.
Áp dụng Design System: lỗi field dùng token danger.
Trạng thái cần thể hiện: error (vượt sức chứa, chưa có năm học Active), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1645–1665 (Table 3-22 Create Classroom Use Cases Specification)
- traceability_matrix.md §1.2 (UC-018 ↔ BR-024/BR-025/BR-192)
- business_rules.md BR-192 (bổ sung 2026-07-01, mở rộng Normal Flow gốc)
