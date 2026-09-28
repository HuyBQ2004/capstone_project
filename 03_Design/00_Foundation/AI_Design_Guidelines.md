# AI Design Guidelines — SFMS

**Trạng thái**: Confirmed (2026-07-01)
**Mục đích**: Chuẩn hoá cách dùng công cụ AI (Stitch, Figma AI, v0, ChatGPT...) để sinh Wireframe/Prototype nhất quán với Design System đã chốt — kế thừa tinh thần "CASE 2.0" đã áp dụng cho ADR (`06_Document/Templates/PHASE-2_ADR.md`) và mở rộng khối "AI Generation Prompt" đã có sẵn trong template Screen Specification (`00_UI_Design_Documentation_Plan.md` mục 6.4).

---

## 1. Nguyên tắc bắt buộc khi dùng AI sinh UI

1. **AI không được tự quyết định nghiệp vụ** — mọi field/trạng thái/luồng đưa vào prompt phải lấy nguyên từ Screen Specification đã duyệt (`05_ScreenSpecifications/`), không để AI "đoán" thêm field hoặc bớt field.
2. **AI phải tuân theo Design System đã chốt** (`Design_System.md`, `Component_Guidelines.md`) — không dùng màu/font mặc định của AI tool nếu khác với token đã định nghĩa.
3. **Luôn sinh riêng theo từng nền tảng** — 1 prompt cho Web (shadcn/Tailwind aesthetic), 1 prompt riêng cho Mobile (Material 3 aesthetic), không dùng chung 1 prompt rồi "responsive hoá" sau (2 ngôn ngữ thiết kế khác nhau, xem `Design_System.md` mục 1).
4. **Output của AI là bản nháp (draft), luôn cần review theo checklist mục 4** trước khi coi là Wireframe chính thức — không merge thẳng vào `06_Wireframes/` khi chưa review.

## 2. Prompt Template chuẩn (mở rộng từ mục 6.4 của `00_UI_Design_Documentation_Plan.md`)

```
Tạo wireframe cho màn hình [Tên màn hình], nền tảng [Web/Mobile], actor [vai trò].

Design System bắt buộc tuân theo:
- Font: Be Vietnam Pro (fallback Inter/Noto Sans) — xem Design_System.md mục 6.
- Màu thương hiệu: primary #0E7490, xem đầy đủ palette tại Design_System.md mục 2–3.
- [Web] Style: shadcn/ui "new-york" style + Tailwind CSS, bo góc 8px, shadow nhẹ.
- [Mobile] Style: Material Design 3, dynamic color từ seed #0E7490, bo góc 8-12px, tone-based elevation (không đổ bóng cứng).

Bố cục: [lấy từ Layout_Guidelines.md mục tương ứng nền tảng]
Component bắt buộc dùng: [liệt kê từ Component_Guidelines.md — chỉ dùng component đã duyệt]
Thành phần/field bắt buộc: [copy nguyên từ khối "Business Logic" của Screen Specification]
Trạng thái cần thể hiện: loading, empty, error (MSG-xxx), success (MSG-yyy)
Điều hướng vào/ra: [lấy từ Navigation_Guidelines.md + 04_ScreenFlow tương ứng]

KHÔNG được:
- Thêm field/nút/tính năng ngoài danh sách trên.
- Tự chọn màu ngoài bảng Design_System.md (kể cả màu AI tool đề xuất mặc định).
- Trộn phong cách Material 3 vào bản Web hoặc phong cách shadcn/Tailwind vào bản Mobile.
```

## 3. Công cụ đề xuất theo mục đích

| Mục đích | Công cụ | Ghi chú |
|---|---|---|
| Wireframe low-fidelity nhanh | Google Stitch, v0.dev (cho Web — sinh trực tiếp code React/Tailwind gần với shadcn) | Ưu tiên vì output gần code thật, giảm công chuyển hoá sang implement |
| Wireframe/Mockup Mobile | Figma AI, Galileo AI | Cần chỉnh sửa thủ công nhiều hơn để đúng Material 3 component thật (AI tool thường lệch nhẹ so với Flutter Material widget chuẩn) |
| Viết nhanh nội dung placeholder/copy UI | ChatGPT/Claude | Dùng để sinh câu MSG nháp trước khi đối chiếu lại với `error_message.md` — không dùng thẳng output AI làm MSG chính thức |
| Prototype click-through | Figma (thủ công nối các Wireframe đã duyệt) | Không cần AI ở bước này — chỉ nối theo đúng `04_ScreenFlow` |

## 4. Checklist Review sau khi AI sinh Wireframe

- [ ] Đủ toàn bộ field trong Screen Specification, không thừa không thiếu.
- [ ] Đúng token màu/font theo `Design_System.md` (không còn màu mặc định của AI tool).
- [ ] Đúng component đã duyệt trong `Component_Guidelines.md` (không có component lạ AI tự chèn).
- [ ] Thể hiện đủ 4 trạng thái màn hình (`UI_Design_Guidelines.md` mục 6).
- [ ] Đúng phong cách nền tảng (không lẫn Material vào Web hay shadcn vào Mobile).
- [ ] Đã đối chiếu nhanh Accessibility (contrast, kích thước nút) theo `Accessibility.md` mục 6.

Wireframe chỉ được chuyển vào `06_Wireframes/` sau khi đạt đủ checklist trên — quy trình đầy đủ nối tiếp `12_DesignReview/00_Review_Checklist.md` khi tài liệu đó được viết.

## Nguồn dữ liệu
- `06_Document/Templates/PHASE-2_ADR.md` (tinh thần CASE 2.0 — AI Prompt Constraint).
- `00_UI_Design_Documentation_Plan.md` mục 6.4 (AI Generation Prompt gốc).
- `Design_System.md`, `Component_Guidelines.md`, `Layout_Guidelines.md`, `Navigation_Guidelines.md` (input bắt buộc cho prompt).
