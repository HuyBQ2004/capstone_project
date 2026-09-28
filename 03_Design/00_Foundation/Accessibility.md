# Accessibility Baseline — SFMS

**Trạng thái**: Confirmed (2026-07-01) — thay thế TODO #8 [MEDIUM] "Accessibility baseline" trong `00_UI_Design_Documentation_Plan.md`.
**Chuẩn áp dụng**: WCAG 2.2 Level AA (không nhắm AAA — cân đối nguồn lực team sinh viên, nhưng vẫn phải đạt AA đầy đủ vì người dùng gồm cả phụ huynh/nhân viên lớn tuổi, không rành công nghệ).
**Nguồn nghiên cứu**: WCAG 2.2 Success Criteria 1.4.3 (Contrast Minimum), 2.5.8 (Target Size Minimum) — WebAIM, Apple HIG, Material Design 3 touch target guidance.

---

## 1. Contrast tối thiểu (WCAG 1.4.3)

| Loại nội dung | Ngưỡng bắt buộc | Áp dụng |
|---|---|---|
| Chữ thường | ≥ 4.5:1 | Toàn bộ body text, label form |
| Chữ lớn (≥18pt hoặc ≥14pt bold) | ≥ 3:1 | Tiêu đề, Headline |
| Icon/thành phần đồ hoạ mang thông tin | ≥ 3:1 | Icon cảnh báo, biểu tượng trạng thái |

**Đã kiểm tra sẵn trong `Design_System.md`**: toàn bộ token `success/warning/danger/critical/info` (shade 700 trên nền trắng) đều đạt ≥ 4.5:1 (dao động 5.2:1–7.1:1 theo tính toán trong bảng mục 3 của `Design_System.md`). Khi implement, **bắt buộc đo lại bằng công cụ thực tế** (VD WebAIM Contrast Checker) trước khi merge — bảng trong `Design_System.md` là ước lượng dựa trên giá trị hex chuẩn Tailwind, không phải kết quả đo trực tiếp.

**Quy tắc bổ sung**:
- Không dùng chữ màu trên nền cùng tông nhạt hơn 1 bậc (VD chữ `red-500` trên nền `red-100` — không đạt 4.5:1). Luôn dùng cặp "chữ đậm (600–700) trên nền cực nhạt (50–100)" như quy định ở `Design_System.md` mục 3.
- Không truyền đạt ý nghĩa **chỉ bằng màu sắc** (VD chỉ tô đỏ mà không có label/icon "Dị ứng") — bắt buộc kèm text hoặc icon để người dùng mù màu vẫn nhận biết được (ước tính ~8% nam giới có một dạng mù màu đỏ-xanh — rủi ro thật với cảnh báo an toàn).

## 2. Touch Target Size (WCAG 2.5.8 + platform guideline)

| Chuẩn | Kích thước tối thiểu | Áp dụng |
|---|---|---|
| WCAG 2.5.8 (mức sàn) | 24×24 CSS px | Web — mọi phần tử bấm được tối thiểu |
| Material Design 3 (khuyến nghị, dùng làm chuẩn chính thức cho Mobile) | 48×48 dp | Mobile — mọi nút, icon-button, item trong list |
| Apple HIG (tham khảo, không áp dụng trực tiếp vì dùng Flutter/Material) | 44×44 pt | Chỉ tham khảo khi review UI trên iOS |

→ **Quyết định**: SFMS dùng **48×48dp làm chuẩn tối thiểu cho Mobile** (cao hơn sàn WCAG, theo khuyến nghị Material 3) — quan trọng vì thao tác thực địa (bếp, cổng trường) thường vội, tay có thể ướt/đeo găng.
→ Web dùng tối thiểu **24×24px** theo WCAG nhưng khuyến khích **40×40px** cho nút hành động chính (đồng bộ cảm giác bấm với Mobile khi cùng 1 người dùng chuyển đổi giữa 2 nền tảng).

## 3. Semantic & Screen Reader

- Web: mọi component shadcn dựa trên Radix UI đã có sẵn ARIA roles — không được ghi đè `role`/`aria-*` tuỳ tiện khi tuỳ biến component.
- Mobile: dùng đúng widget Semantics mặc định của Flutter Material (VD `Semantics(label: ...)` cho icon-only button); ảnh bằng chứng cần `alt text`/`semanticLabel` mô tả ngắn (VD "Ảnh nhiệt độ tủ lưu mẫu").
- Form input luôn có `<label>` liên kết đúng field (Web: `htmlFor`; Mobile: `labelText` của `TextField`), không dùng `placeholder` thay cho label.

## 4. Font size & Zoom

- Không khoá `user-scalable=no` trên Web — cho phép pinch-zoom/browser zoom tới 200% mà không vỡ layout (kiểm tra khi review Prototype).
- Mobile tôn trọng `textScaleFactor` hệ điều hành (đã nêu ở `Responsive_Guidelines.md` mục 3) — không dùng đơn vị pixel cứng cho font, dùng `sp`/theme text style.
- Cỡ chữ tối thiểu cho Body text: **14px/14sp** (không dùng 12px cho nội dung quan trọng — chỉ dùng cho Label/caption phụ theo `Design_System.md` mục 6).

## 5. Đối tượng người dùng cần lưu ý đặc biệt

Theo `Report 1.md` Field Survey (School Board, Health Officers, Kitchen Managers, Kitchen Staff, Teachers, Parents) — nhóm phụ huynh và nhân viên bếp có thể không rành công nghệ hoặc lớn tuổi:
- Ưu tiên icon quen thuộc, tránh iconography trừu tượng cần học cách hiểu.
- Câu chữ MSG ngắn gọn, tránh thuật ngữ kỹ thuật (đã nêu ở `UI_Design_Guidelines.md` mục 1).
- Thời gian timeout session (BR-004, 30 phút) cần cảnh báo trước khi hết hạn (toast cảnh báo còn 2 phút) — tránh mất dữ liệu đang nhập do đăng xuất đột ngột.

## 6. Checklist review Accessibility (dùng trong `12_DesignReview`/`00_Review_Checklist.md` khi tài liệu đó được tạo)

- [ ] Mọi cặp màu chữ/nền đã đo contrast thực tế ≥ 4.5:1 (hoặc ≥3:1 với chữ lớn/icon).
- [ ] Mọi nút/icon-button đạt kích thước tối thiểu theo mục 2.
- [ ] Không có thông tin chỉ truyền đạt qua màu sắc.
- [ ] Mọi ảnh có alt text/semantic label.
- [ ] Layout không vỡ khi zoom 200% (Web) / tăng textScaleFactor (Mobile).

## Nguồn tham khảo
- WCAG 2.2 Success Criteria 1.4.3, 2.5.8 (w3.org/WAI, WebAIM.org/articles/contrast).
- Material Design 3 accessibility/touch target guidance (m3.material.io, docs.flutter.dev).
- `Report 1.md` mục Field Survey Methodology.
