# 07_Prototype — Cấu trúc thư mục & Kế hoạch chia việc (Nam, Thảo, Minh, Mạnh, Huy — dùng Claude)

**Ngày lập**: 01/07/2026
**Cập nhật**: 01/07/2026 — Chuyển từ Figma sang **Google Stitch** (Stitch MCP + stitch-skills)
**Cập nhật 2**: 2026-07-02 — Sửa mục 1c (cho phép gen theo nhóm ≤5 màn liên kết/lần thay vì bắt buộc từng màn riêng, khớp cách nhóm màn đã dùng ở Wireframe), xoá câu mâu thuẫn "không dùng AI" còn sót ở Nguồn dữ liệu, sửa template manifest còn ghi "Figma", đánh lại số mục. **Lưu ý còn mở**: danh sách tool ở mục 1a chưa xác minh được với tài liệu chính thức (`stitch.withgoogle.com/docs/mcp/setup` là trang JS-render, không fetch được nội dung) — người thực thi cần tự kiểm tra tool list thật trong môi trường mình dùng (Antigravity/Claude Code/khác) trước khi chạy mục 1b, vì tên tool có thể khác nhau giữa các server Stitch MCP khác nhau.
**Trạng thái**: Đề xuất — bắt đầu sau khi phần Wireframe tương ứng đã qua QA (mục 5 của kế hoạch Wireframe)
**Nguồn**: `03_Design/06_Wireframes/00_Team_Work_Assignment_Plan.md` + `03_Design/05_ScreenSpecifications/00_Team_Work_Assignment_Plan.md` (đội hình, RACI, lịch SDS — **không lặp lại**), `00_UI_Design_Documentation_Plan.md` §4.7 + §6.6 (mục đích, template Prototype), `00_Foundation/AI_Design_Guidelines.md` mục 3

**Điểm thay đổi quan trọng so với kế hoạch gốc**: Bước này **KHÔNG dùng Figma** — thay bằng **[Google Stitch](https://stitch.withgoogle.com)** thông qua **Stitch MCP server** ([stitch-skills](https://github.com/google-labs-code/stitch-skills) đã cài sẵn trong môi trường Antigravity). Claude **có thể tự gen màn hình thật** bằng tool `generate_screen_from_text` + `create_design_system` — không cần thao tác kéo-thả thủ công. Output là **link Stitch project** có thể xem, share với stakeholder, và export code nếu cần.

---

## 1. Công cụ & Quy trình — Google Stitch thay Figma

### 1a. Stitch MCP tools sẵn có trong môi trường

| Tool | Mục đích |
| --- | --- |
| `create_project` | Tạo Stitch project mới cho mỗi Actor × Nền tảng |
| `create_design_system` | Thiết lập Design System (màu, font, border-radius) từ `00_Foundation/Design_System.md` |
| `update_design_system` | Cập nhật Design System — **luôn gọi ngay sau `create_design_system`** |
| `apply_design_system` | Gắn Design System vào project |
| `generate_screen_from_text` | Gen 1 hoặc nhiều màn hình liên kết (tối đa ~5 màn/lần) từ prompt text (bám đúng field/BR từ Screen Spec) |
| `edit_screens` | Chỉnh sửa màn hình đã gen (sửa field, bố cục, trạng thái) |
| `extract_design_context` | Lấy "Design DNA" (font, màu, layout) từ 1 màn đã gen để giữ nhất quán khi gen tiếp màn/nhóm khác |
| `generate_variants` | Tạo thêm biến thể (Empty state, Error state, Loading state) |
| `list_projects` / `get_project` | Kiểm tra project + lấy link |
| `list_screens` / `get_screen` | Xem danh sách / chi tiết màn hình trong project |
| `upload_design_md` | Upload Design Markdown phức tạp cho Design System |

### 1b. Thứ tự bắt buộc khi gen 1 actor

```
1. list_projects              → kiểm tra project đã tồn tại chưa
2. create_project             → tạo project "<Nền tảng>_<Actor>_SFMS"
3. create_design_system       → tạo Design System từ Design_System.md
4. update_design_system       → apply ngay (BẮT BUỘC gọi ngay sau bước 3)
5. apply_design_system        → gắn vào project
6. generate_screen_from_text  → gen theo NHÓM màn liên kết (loop qua từng nhóm luồng,
   khớp đúng cách nhóm đã dùng ở 06_Wireframes/ — ví dụ nhóm "01-06_MenuManagement"
   gen chung 1 lần thay vì 6 lần riêng lẻ; nhóm nào >5 màn thì tách thành 2 lượt gen
   liên tiếp, dùng extract_design_context sau lượt 1 để giữ nhất quán phong cách)
   ├── Nếu timeout: get_screen sau 30s × tối đa 10 lần — KHÔNG retry generate
   └── Nếu có nhiều state: generate_variants cho Error/Empty/Loading
7. edit_screens (nếu cần)     → chỉnh sau khi xem kết quả
8. get_project                → lấy link project điền vào manifest
```

### 1c. Lưu ý quan trọng

- **`generate_screen_from_text` mất vài phút** — kiên nhẫn, không retry nếu timeout.
- **Gen theo nhóm màn liên kết (≤5 màn/lần)** — khớp đúng cách nhóm luồng đã dùng ở Wireframe, không gen rời từng màn một (Stitch không nhớ ngữ cảnh giữa các lần gen riêng biệt nên dễ lệch phong cách giữa các màn cùng luồng nếu gen tách rời). *(Sửa 2026-07-02: trước đây ghi ngược lại — "không gom nhiều màn trong 1 prompt" — đã đổi lại theo đúng khả năng thật của Stitch và cho nhất quán với Wireframe.)*
- **Prompt bám đúng Screen Spec** — lấy nguyên field từ "Business Logic", không để Stitch tự sáng tác.
- **Device type**: `DESKTOP` cho Web actor, `MOBILE` cho Mobile actor.
- **Model đề xuất**: `GEMINI_3_1_PRO` cho màn phức tạp, `GEMINI_3_FLASH` cho màn đơn giản.
- *(Chưa xác minh 2026-07-02: tên tool ở mục 1a lấy từ bản gốc của plan, chưa đối chiếu được với tài liệu chính thức Stitch MCP — kiểm tra lại tool list thật trước khi chạy nếu môi trường không phải Antigravity.)*

---

## 2. Cấu trúc thư mục

```
07_Prototype/
├── 00_Team_Work_Assignment_Plan.md        ← file này
├── 00_Review_Checklist.md                 (viết ở bước cuối, mục 5)
├── Web_SystemAdmin_Prototype.md
├── Web_SchoolAdministration_Prototype.md
├── Web_ServiceCaterers_Prototype.md
├── Web_RawFoodSupplier_Prototype.md
├── Web_Teacher_Prototype.md
├── Web_Parent_Prototype.md
├── Mobile_MedicalStaff_Prototype.md
├── Mobile_ServiceCaterers_Prototype.md
├── Mobile_RawFoodSupplier_Prototype.md
├── Mobile_Teacher_Prototype.md
├── Mobile_Parent_Prototype.md
└── (1 file `.md` mỗi cặp Actor × Nền tảng — giống cấu trúc gốc)
```

Đặt tên: `<Nền tảng>_<Actor>_Prototype.md`, PascalCase — giữ nguyên quy ước từ kế hoạch gốc.

---

## 3. Chia việc — kế thừa nguyên vẹn phân công ở 2 bước trước

| Actor | Người phụ trách | Vai trò ở bước Prototype |
| --- | --- | --- |
| Service Caterers | Trần Tuấn Minh | Gen Stitch project + viết manifest |
| Medical Staff + Guest & Common | Bùi Quang Huy | Gen Stitch project + viết manifest |
| Raw Food Supplier | Nguyễn Đức Mạnh | Gen Stitch project + viết manifest |
| School Administration + System Admin | Nguyễn Văn Nam | Gen Stitch project + viết manifest |
| Teacher + Parent | Nguyễn Phương Thảo | Gen Stitch project + viết manifest + **Review Checklist cuối** (mục 5) cho toàn bộ |

**Thay đổi so với kế hoạch gốc**: Với Stitch MCP, **mọi thành viên tự gen được** thông qua Claude — không phụ thuộc Minh như bản gốc (vốn chỉ Minh có quyền A/R Figma theo RACI). Mỗi người tự chạy workflow MCP cho actor của mình.

**Thứ tự ưu tiên**: giữ nguyên theo lịch SDS (`05_ScreenSpecifications/00_Team_Work_Assignment_Plan.md` mục 8.3).

---

## 4. Template manifest + Prompt cho Claude

### 4a. Template file manifest

```markdown
# Prototype — [Web|Mobile] App — [Actor]

**Link công cụ**: [Stitch project link — điền sau khi dựng xong, để trống nếu chưa có]
**Phạm vi màn hình**: [danh sách toàn bộ <STT>_<TenManHinh> của actor này, lấy đúng từ
  06_Wireframes/<Web|Mobile>/<Actor>/ — copy nguyên, không tự bớt]
**Ngày dựng**: YYYY-MM-DD | **Trạng thái**: Draft cho Review | Đã Review

## Luồng điều hướng đã hiện thực trong Prototype
[copy nguyên mermaid flowchart từ 04_ScreenFlow/<file actor tương ứng>.md, đánh dấu
 ✅/⬜ từng bước đã click-through được trong Stitch project hay chưa]

## Ghi chú review
[điền khi chạy qua 00_Review_Checklist.md — xem mục 4]
```

**Prompt cho Claude viết manifest (batch theo actor, 1 lượt/actor vì mỗi actor chỉ có 1 file)**:

```
Viết file Prototype manifest cho actor <Actor>, nền tảng <Web|Mobile>, theo đúng template
ở 03_Design/00_UI_Design_Documentation_Plan.md mục 6.6.

1. Liệt kê "Phạm vi màn hình" = toàn bộ file trong 06_Wireframes/<Web|Mobile>/<Actor>/ đã
   qua QA (không tự thêm/bớt màn nào ngoài danh sách đó).
2. Copy nguyên mermaid flowchart điều hướng từ 04_ScreenFlow/<file actor tương ứng>.md,
   đánh dấu tất cả bước là ⬜ (chưa click-through) — sẽ cập nhật ✅ thủ công sau khi
   dựng xong trong Stitch project.
3. Để trống "Link công cụ" — điền sau khi có Stitch project thật.
4. Lưu tại 07_Prototype/<Web|Mobile>_<Actor>_Prototype.md
```

---

## 5. `00_Review_Checklist.md` — Nguyễn Phương Thảo phụ trách (đúng RACI Testing/Defect Tracking)

Viết khi tất cả actor đã có Prototype manifest + Figma link thật, theo `00_UI_Design_Documentation_Plan.md` §4.7: *"mỗi màn hình trong Prototype phải thể hiện đủ BR/MSG được gán trong Screen Specification"*. Cách làm:

1. Mở rộng `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` thêm 1 cột "Screen" (theo đúng gợi ý ở §4.7 nguồn dữ liệu) — map mỗi UC/BR/MSG sang đúng file Screen Specification + Prototype tương ứng.
2. Với mỗi actor, đối chiếu Figma thật (click qua từng luồng) có thể hiện đúng validation/error state (MSG) đã khai trong Screen Specification hay không.
3. Gom danh sách lệch (nếu có) thành bảng, trả lại đúng người phụ trách actor đó sửa trong Figma trước khi coi Prototype là "Đã Review".
4. Đây là bước rà soát cuối cùng trước khi bàn giao demo/UAT (theo `Report 1.md` giai đoạn UAT) — không có bước tài liệu nào sau `07_Prototype/` trong `00_UI_Design_Documentation_Plan.md`.

## Nguồn dữ liệu
- `03_Design/06_Wireframes/00_Team_Work_Assignment_Plan.md`, `03_Design/05_ScreenSpecifications/00_Team_Work_Assignment_Plan.md` (đội hình, RACI, lịch SDS dùng chung).
- `03_Design/00_UI_Design_Documentation_Plan.md` §4.7, §6.6 (mục đích, template Prototype).
- `03_Design/00_Foundation/AI_Design_Guidelines.md` mục 3 (áp dụng cho việc dùng Stitch MCP sinh màn hình ở bước này — *sửa 2026-07-02: câu cũ ghi ngược "bước này không dùng AI, chỉ Figma thủ công", là tàn dư từ bản trước khi pivot sang Stitch ngày 01/07/2026, mâu thuẫn với toàn bộ nội dung phần 1-3 ở trên; đã xoá*).
- `02_Requirements/03_TraceabilityMatrix/traceability_matrix.md` (nguồn mở rộng cột "Screen" cho checklist rà soát cuối).
