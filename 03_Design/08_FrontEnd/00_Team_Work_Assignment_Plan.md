# 08_FrontEnd — Cấu trúc thư mục & Kế hoạch chia việc (Nam, Thảo, Minh, Mạnh, Huy — dùng Claude)

**Ngày lập**: 04/07/2026
**Trạng thái**: Đề xuất — bắt đầu ngay khi phần Screen Specification tương ứng của mỗi người đã qua QA (mục 6 của kế hoạch ScreenSpec)
**Nguồn**: `03_Design/05_ScreenSpecifications/00_Team_Work_Assignment_Plan.md` (đội hình, RACI, lịch SDS — **tài liệu này KHÔNG lặp lại**, chỉ khác phần đặc thù Wireframe), `00_UI_Design_Documentation_Plan.md` §4.6 + §6.5 (mục đích, template Wireframe), `00_Foundation/AI_Design_Guidelines.md` (prompt chuẩn + checklist review)

**Quan hệ với `05_ScreenSpecifications`**: Wireframe sinh **1-1** từ Screen Specification đã Confirmed — không tự thêm field/nghiệp vụ mới ngoài Screen Spec. Nếu thiếu field khi làm Wireframe, quay lại sửa Screen Specification trước, không vá trực tiếp ở đây.

---

## 1. Cấu trúc thư mục — giữ nguyên cấu trúc actor như `05_ScreenSpecifications`

```
06_Wireframes/
├── 00_Team_Work_Assignment_Plan.md   ← file này
├── Web/<Actor>/<STT>_<TenManHinh>.md      (8 actor, giống hệt danh sách bên 05_ScreenSpecifications/Web/)
└── Mobile/<Actor>/<STT>_<TenManHinh>.md   (6 actor, giống hệt danh sách bên 05_ScreenSpecifications/Mobile/)
```

**Quy tắc đặt tên**: theo `Naming_Convention.md` §2 — Screen Specification và Wireframe dùng **chung 1 quy tắc đặt tên** (`<STT>_<TenManHinh>.md`, cùng `<Nền tảng>/<Actor>/`). **Không thêm hậu tố `_wireframe`** dù template minh hoạ ở `00_UI_Design_Documentation_Plan.md` §6.5 có ví dụ hậu tố này — giữ tên giống hệt file Screen Spec tương ứng (khác thư mục gốc `05_.../` vs `06_.../` nên không trùng đường dẫn) để dò cặp 1-1 nhanh nhất, đúng tinh thần "Naming_Convention.md đã Confirmed".

---

## 2. Chia việc — kế thừa nguyên vẹn phân công ở `05_ScreenSpecifications` mục 2

Người nào viết Screen Spec của actor nào thì làm Wireframe actor đó — **không đổi người giữa 2 bước**, vì người viết Screen Spec đã nắm rõ field/BR/MSG, làm Wireframe tiếp nối sẽ nhanh và chính xác hơn giao cho người khác đọc lại từ đầu.

| Actor | Người phụ trách (giữ nguyên) |
| --- | --- |
| Service Caterers | Trần Tuấn Minh |
| Raw Food Supplier + Guest & Common | Bùi Quang Huy |
| Medical Staff | Nguyễn Đức Mạnh |
| School Administration + System Admin | Nguyễn Văn Nam |
| Teacher + Parent | Nguyễn Phương Thảo (+ vai trò QA cuối, xem mục 5) |

**Thứ tự ưu tiên thực thi**: bám đúng lịch SDS thật đã đối chiếu ở `05_ScreenSpecifications/00_Team_Work_Assignment_Plan.md` mục 8.3 — **không làm Wireframe theo thứ tự alphabet actor**, mà theo mốc SDS gần nhất trước:

1. Minh (Service Caterers, phần receiving/evidence trước) + Mạnh (Raw Food Supplier) → kịp SDS v1.3 (05/07/2026).
2. Minh (phần còn lại: 3-step inspection, food sample retention) → SDS v1.4 (12/07/2026).
3. Thảo (Teacher/Parent) + Huy (Medical Staff) → SDS v1.5 (19/07/2026).
4. Nam (phần Incident) + Huy (Medical Staff) → SDS v1.6 (26/07/2026).
5. Nam (School Administration/System Admin, phần Reporting/Dashboard) → SDS v1.7 (02/08/2026).

---
