# Screen Spec — Violation Registry

**Actor**: School Administration | **UC liên quan**: UC-181 (Report 3 §3.2 "View Violation Registry") | **Status**: Draft
**BR liên quan**: BR-189 (danh sách tổng hợp vi phạm)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter theo Loại (Process/Staff/Partner) | Select | — | BR-183, BR-184 | |
| 2 | Filter theo Severity | Select | — | — | |
| 3 | Filter theo Status | Select | Pending/Approved/Closed | — | |
| 4 | Bảng danh sách vi phạm hợp nhất | Table | Cột: Type, Actor/Staff Name, Severity, Status, Reported Date | BR-189 | Nguồn: `NonConformanceRecord` (BR-183) + `StaffViolationRecord` (BR-184) |
| 5 | Badge Severity | Badge (danger=High, warning=Medium, info=Low) | — | — | |
| 6 | Cảnh báo nghi ngờ tái diễn (nếu có) | Badge/Icon cảnh báo | Best-effort, không tự động escalate | BR-187 | MSG-088 |
| 7 | Action: xem lịch sử xử lý | Row click | — | — | Mở `Record Violation Resolution` (nếu đã có) |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: "Không có vi phạm nào khớp bộ lọc" (trạng thái tích cực).
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ, cảnh báo tái diễn (MSG-088) hiện dưới dạng badge/icon cạnh dòng liên quan.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Kiểm tra & Sự cố" (cạnh Incident Dashboard). Điều hướng ra: `Record Violation Resolution`, `Weekly Override Abuse Report`.
**Bố cục**: Dashboard Shell — filter bar (Type/Severity/Status), bảng `Table`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Violation Registry, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — filter bar (Type/Severity/Status), bảng dữ liệu (Type/Actor-Staff Name/Severity/Status/Reported Date).
Thành phần bắt buộc: filter, bảng, badge severity, badge/icon cảnh báo tái diễn (best-effort), action xem chi tiết.
Áp dụng Design System: badge danger/warning/info theo severity.
Trạng thái cần thể hiện: empty, success (kèm cảnh báo tái diễn).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4683–4703 (Table 3-162 View Violation Registry Use Cases Specification)
- traceability_matrix.md §1.11 (UC-181 ↔ BR-189)
- business_rules.md BR-187 (cảnh báo tái diễn, best-effort)

**Đã hợp nhất (mục 6, `00_Team_Work_Assignment_Plan.md`)**: đối chiếu với `Web/MedicalStaff/27_ViolationRegistry.md` (và bản Mobile tương ứng) — field hiển thị dữ liệu khớp nhau hợp lý, không cần sửa thêm.
