# 06_Wireframes — Cấu trúc thư mục & Kế hoạch chia việc (Nam, Thảo, Minh, Mạnh, Huy — dùng Claude)

**Ngày lập**: 01/07/2026
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
| Medical Staff + Guest & Common | Bùi Quang Huy |
| Raw Food Supplier | Nguyễn Đức Mạnh |
| School Administration + System Admin | Nguyễn Văn Nam |
| Teacher + Parent | Nguyễn Phương Thảo (+ vai trò QA cuối, xem mục 5) |

**Thứ tự ưu tiên thực thi**: bám đúng lịch SDS thật đã đối chiếu ở `05_ScreenSpecifications/00_Team_Work_Assignment_Plan.md` mục 8.3 — **không làm Wireframe theo thứ tự alphabet actor**, mà theo mốc SDS gần nhất trước:

1. Mạnh (Raw Food Supplier) + Minh (Service Caterers, phần receiving/evidence trước) → kịp SDS v1.3 (05/07/2026).
2. Minh (phần còn lại: 3-step inspection, food sample retention) → SDS v1.4 (12/07/2026).
3. Huy (Medical Staff) + Thảo (Teacher/Parent) → SDS v1.5 (19/07/2026).
4. Huy + Nam (phần Incident) → SDS v1.6 (26/07/2026).
5. Nam (School Administration/System Admin, phần Reporting/Dashboard) → SDS v1.7 (02/08/2026).

---

## 3. Cách làm Wireframe với Claude — 3 hướng, chọn theo công cụ sẵn có

### 3a. Hướng khuyến nghị (mặc định mới) — dùng skill `wireframer` (agilek/wireframer-skill)

Đã kiểm chứng phù hợp cho dự án này (xem file demo `_DEMO_wireframer_skill_Login.html` trong thư mục này). Đây là 1 Agent Skill công khai, sinh **wireframe HTML/React/Vue/Svelte thật, có thể click chạy**, phong cách hand-drawn kiểu Balsamiq (grayscale, viền vẽ tay, font Patrick Hand, nền giấy kẻ ô, dùng `wired-elements` web components + icon doodle) — đúng tinh thần "low-fidelity" của `00_UI_Design_Documentation_Plan.md` §4.6 nhưng **thật hơn hẳn** so với khối ASCII (mục 3c), và tự có điều hướng multi-screen (dùng `useState`/vanilla JS ẩn-hiện, không cần router) — rất hợp để dựng cả 1 nhóm màn hình theo đúng luồng trong `04_ScreenFlow` chỉ trong 1 lượt.

**Cài đặt** (mỗi thành viên làm 1 lần trong Claude Code của mình):
```
npx skills add agilek/wireframer-skill
```
hoặc nếu dùng Claude Code dạng plugin:
```
/plugin marketplace add agilek/wireframer-skill
/plugin install wireframes-designer@wireframer-skill
```
Không dùng được với bản Cowork/chat thường không cài được skill ngoài — khi đó dùng mục 3b hoặc 3c.

**Cách dùng cho dự án SFMS** (khác cách dùng mặc định của skill — skill mặc định nhận mô tả tự do, ở đây phải ép nó bám đúng Screen Specification):
```
Dùng skill wireframer để dựng wireframe HTML (vanilla HTML, không cần React) cho nhóm màn hình
sau của actor <Actor>, nền tảng <Web|Mobile>, theo đúng luồng điều hướng multi-screen
(dùng JS ẩn/hiện div, không cần router):

<Danh sách màn hình + STT, lấy từ 04_ScreenFlow/<file actor>.md — 1 nhóm màn liên quan nhau,
VD toàn bộ luồng "Three-Step Inspection" hoặc toàn bộ luồng "Academic Year Management">

Với MỖI màn hình, field/thành phần PHẢI lấy đúng nguyên văn từ khối "Business Logic" trong
03_Design/05_ScreenSpecifications/<Web|Mobile>/<Actor>/<STT>_<TenManHinh>.md — không tự thêm
field ngoài Screen Specification (khác với cách dùng mặc định của skill là tự bịa nội dung
"realistic copy" — ở đây copy phải khớp Screen Spec, không tự sáng tác).
Nút điều hướng giữa các màn phải khớp đúng mũi tên trong mermaid flowchart của 04_ScreenFlow.

Lưu file tại: 06_Wireframes/<Web|Mobile>/<Actor>/<STT-đầu>-<STT-cuối>_<TenNhomManHinh>.html
```

**Lưu ý ranh giới quan trọng**: output của skill này là **hand-drawn, grayscale, chưa dùng Design System thật** — dù có thể click-qua-lại giữa nhiều màn, nó **không thay thế** `07_Prototype/` (vẫn cần Figma + Design System thật theo `00_UI_Design_Documentation_Plan.md` §4.7). Coi đây là bản nháp trực quan tốt hơn ASCII, không phải Prototype chính thức.

### 3b. Hướng thay thế — dùng công cụ AI khác nếu có tài khoản (Stitch/Figma AI/v0.dev/Galileo AI)

Nếu không cài được skill ở mục 3a nhưng có quyền dùng Stitch/Figma AI/v0.dev/Galileo AI (`AI_Design_Guidelines.md` mục 3), lấy nguyên khối **"AI Generation Prompt"** đã có sẵn trong file Screen Specification tương ứng (mục 6.4 của `00_UI_Design_Documentation_Plan.md`), mở rộng theo prompt chuẩn ở `AI_Design_Guidelines.md` mục 2, dán vào công cụ, rồi lưu link/ảnh output vào đúng field "File/Link" trong template Wireframe (mục 4). Output AI **luôn là bản nháp**, phải qua checklist mục 4 của `AI_Design_Guidelines.md` trước khi coi là chính thức.

### 3c. Hướng dự phòng (không phụ thuộc gì) — khối ASCII trong `.md`

Khi không cài được skill lẫn không có tài khoản công cụ ngoài, vẫn có thể mô tả bố cục bằng khối ASCII ngay trong file `.md`:

```
┌─────────────────────────────────────┐
│ Header: [Tên màn hình]      [≡ Menu]│
├─────────────────────────────────────┤
│ [Field 1: Label + Input]             │
│ [Field 2: Label + Dropdown]          │
│ [Button: Submit]  [Button: Cancel]   │
├─────────────────────────────────────┤
│ Bottom Nav (Mobile) / Sidebar (Web)  │
└─────────────────────────────────────┘
```

Dùng đúng thứ tự field/thành phần đã liệt kê trong khối "Business Logic" của Screen Specification tương ứng, đúng vị trí Header/Content/Action theo khối "Platform Layout" (Web hoặc Mobile) — không tự sắp xếp lại.

**Cả 3 hướng đều cần thêm 1 file `.md` manifest** theo template `00_UI_Design_Documentation_Plan.md` §6.5 (mục 4 dưới đây), trỏ "File/Link" về đúng file `.html`/ảnh/link vừa tạo.

---

## 4. Mẫu prompt batch cho Claude (5-10 màn/lượt, giống tinh thần mục 4b bên ScreenSpec) — dùng cho hướng 3b/3c

*(Hướng 3a — skill `wireframer` — đã có mẫu prompt riêng ngay trong mục 3a, vì skill này hợp với batch theo **nhóm màn hình cùng luồng** hơn là 5-10 màn rời rạc.)*

```
Viết Wireframe cho <N> màn hình sau của actor <Actor>, nền tảng <Web|Mobile>
(danh sách + STT lấy đúng theo 03_Design/05_ScreenSpecifications/<Web|Mobile>/<Actor>/,
đã qua QA — chỉ làm Wireframe cho file Screen Spec đã tồn tại):

1. <STT>_<TenManHinh> — <Web|Mobile>
2. ...
<N>. ...

Với MỖI màn hình, làm tuần tự và độc lập:
1. Đọc đúng file 05_ScreenSpecifications/<Web|Mobile>/<Actor>/<STT>_<TenManHinh>.md —
   lấy nguyên field từ khối "Business Logic" và bố cục từ khối "Platform Layout — <Web|Mobile>".
2. Viết theo template 00_UI_Design_Documentation_Plan.md mục 6.5: Nguồn, Công cụ sinh
   ("Thủ công - text-block layout" nếu theo hướng 3a, hoặc tên tool AI nếu theo hướng 3b),
   File/Link, checklist đối chiếu (Đủ field / Đúng Design System / Đủ trạng thái).
3. Nếu theo hướng 3a: vẽ khối ASCII layout đúng thứ tự field, đúng vùng
   Header/Content/Action theo Platform Layout — không tự thêm/bớt field.
4. KHÔNG suy diễn bố cục màn sau từ pattern màn trước — mỗi màn tự đọc lại đúng
   Screen Spec riêng của nó (2 màn cùng dạng List/Detail vẫn có thể khác field).

Ràng buộc:
- Không thêm field/nghiệp vụ ngoài Screen Specification nguồn — phát hiện thiếu thì báo
  lại, không tự bịa thêm để "cho đủ".
- Lưu đúng đường dẫn: 06_Wireframes/<Web|Mobile>/<Actor>/<STT>_<TenManHinh>.md
- Sau khi xong, liệt kê lại các trường hợp phát hiện Screen Spec nguồn thiếu thông tin
  (nếu có) để báo lại người phụ trách Screen Spec actor đó sửa trước.
```

---

## 5. Review & tổng hợp — Nguyễn Phương Thảo phụ trách (giữ nguyên vai trò QA từ bước ScreenSpec)

1. Đối chiếu mỗi file Wireframe với đúng checklist ở `AI_Design_Guidelines.md` mục 4 (đủ field, đúng token, đúng component đã duyệt, đủ 4 trạng thái, đúng phong cách nền tảng, đối chiếu nhanh Accessibility).
2. Đối chiếu số lượng file `06_Wireframes/<Web|Mobile>/<Actor>/` phải khớp chính xác số lượng file tương ứng trong `05_ScreenSpecifications/<Web|Mobile>/<Actor>/` đã Confirmed — không thiếu, không thừa.
3. Gom danh sách các trường hợp "Screen Spec nguồn thiếu thông tin" (phát hiện ở bước 4 mục 4) thành 1 bảng, trả lại đúng người phụ trách Screen Spec actor đó để bổ sung trước khi sang `07_Prototype/`.

---

## 6. Prompt khởi động — mỗi thành viên chỉ gửi 1 lần duy nhất

Giống cách làm ở `05_ScreenSpecifications/00_Team_Work_Assignment_Plan.md` mục 7 — gửi thẳng file này kèm 1 tin nhắn ngắn nêu mình là ai, để Claude tự đọc quy tắc, tự kiểm tra Screen Spec nguồn đã sẵn sàng chưa, tự lập todo list theo nhóm màn hình cùng luồng, và tự chạy tới khi xong.

**Mỗi thành viên đổi `<Tên>` và `<Actor(s)>` đúng theo bảng mục 2 rồi gửi đúng 1 lần:**

```
Tôi là <Tên>, phụ trách Wireframe cho <Actor(s)> theo đúng phân công ở mục 2 của file
03_Design/06_Wireframes/00_Team_Work_Assignment_Plan.md (đọc kỹ toàn bộ file này trước).

Hãy tự làm toàn bộ phần việc của tôi, không cần hỏi lại giữa chừng trừ khi phát hiện
mâu thuẫn dữ liệu nghiêm trọng:

1. Đọc kỹ 00_Team_Work_Assignment_Plan.md — nhớ đúng: cấu trúc thư mục (mục 1), 3 hướng
   làm Wireframe (mục 3, ưu tiên 3a dùng skill wireframer nếu môi trường cài được, không
   thì 3b/3c), mẫu prompt (mục 3a hoặc mục 4 tuỳ hướng).
2. Kiểm tra skill `wireframer` (agilek/wireframer-skill) đã cài trong môi trường này chưa.
   Nếu chưa và có thể tự cài (`npx skills add agilek/wireframer-skill`), tự cài. Nếu môi
   trường không cho cài skill ngoài (VD Cowork), tự chuyển sang hướng 3b/3c mà không cần hỏi lại.
3. Đối chiếu 03_Design/05_ScreenSpecifications/<Web|Mobile>/<Actor>/ — xác nhận các file
   Screen Specification của tôi đã tồn tại và ở trạng thái đã qua QA (mục 6 của kế hoạch
   ScreenSpec) chưa. Nếu còn thiếu/chưa qua QA, báo lại thay vì tự bịa nội dung để làm Wireframe.
4. Tra 03_Design/04_ScreenFlow/<file actor tương ứng>.md để nhóm các màn hình theo đúng
   luồng điều hướng (mermaid flowchart) — mỗi nhóm là 1 luồng nghiệp vụ liền mạch (VD
   "Three-Step Inspection", "Academic Year Management"), không nhóm tuỳ tiện.
5. Tự lập 1 todo list theo từng nhóm luồng. Với MỖI nhóm: nếu dùng hướng 3a, chạy đúng
   mẫu prompt ở mục 3a (bám Screen Spec, không để skill tự bịa nội dung); nếu dùng hướng
   3b/3c, chạy đúng mẫu prompt ở mục 4.
6. Với MỖI màn hình trong nhóm, field/bố cục phải lấy đúng từ Screen Specification tương
   ứng — không suy diễn từ màn khác trong cùng nhóm dù giống dạng List/Detail.
7. Lưu đúng đường dẫn theo quy tắc ở mục 1/3a, kèm file `.md` manifest theo mục 4.
8. Sau khi xong toàn bộ nhóm luồng được giao, đối chiếu số lượng file với số lượng Screen
   Spec tương ứng (mục 5 bước 2) — không thiếu, không thừa.
9. Báo cáo tóm tắt cho Nguyễn Phương Thảo (người phụ trách QA mục 5): tổng số file đã tạo,
   hướng đã dùng (3a/3b/3c), danh sách "Screen Spec nguồn thiếu thông tin" nếu có.

Dùng todo list/task tracking riêng để tự theo dõi tiến độ qua nhiều lượt. Bắt đầu ngay.
```

**Lưu ý**: prompt này giả định Claude đang chạy trong môi trường có quyền đọc/ghi trực tiếp file dự án (Claude Code, hoặc Cowork có kết nối thư mục `SEP490_G8`) — riêng bước cài skill `wireframer` (mục 3a) chỉ khả thi ở Claude Code hoặc môi trường hỗ trợ cài skill ngoài, không khả thi ở bản Cowork/chat thường (Claude sẽ tự chuyển sang 3b/3c theo bước 2 ở trên).

## Nguồn dữ liệu
- `03_Design/05_ScreenSpecifications/00_Team_Work_Assignment_Plan.md` (đội hình, RACI, lịch SDS — nguồn dùng chung).
- `03_Design/00_UI_Design_Documentation_Plan.md` §4.6, §6.5 (mục đích, template Wireframe).
- `03_Design/00_Foundation/AI_Design_Guidelines.md` (prompt chuẩn mục 2, công cụ đề xuất mục 3, checklist review mục 4).
- `03_Design/00_Foundation/Naming_Convention.md` §2 (quy ước đặt tên dùng chung Screen Spec/Wireframe).
- [agilek/wireframer-skill](https://github.com/agilek/wireframer-skill) (SKILL.md, README) — skill dùng ở mục 3a, đã kiểm chứng bằng file demo `_DEMO_wireframer_skill_Login.html` trong thư mục này (2026-07-01).
