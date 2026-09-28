# Review — Tái cấu trúc điều hướng (Sidebar IA) theo 4 Module — Medical Staff

**Mục đích**: ghi nhận quyết định tái tổ chức sidebar/nav của Medical Staff theo đề xuất "4 module chính" từ phỏng vấn thực tế Cán bộ Y tế, đối chiếu với cấu trúc nav thật hiện có (`src/lib/roles.ts`). Đây là thay đổi **thuần điều hướng/nhóm menu** — không đổi route, không đổi field, không đổi business rule nào. **CHƯA áp code trong session này** (theo yêu cầu người dùng — chỉ sửa file review).

**Ngày biên soạn**: 26/07/2026.

**Nguồn**:
- `Phỏng vấn trải nghiệm UI ngày 25_7.md` Mục I (đề xuất 4 module) và Mục II.6 (chú thích "Danh mục NCC" là tính năng Caterers).
- `03_Design/08_FrontEnd/Web/src/lib/roles.ts` dòng 175-238 (cấu trúc nav thật hiện tại của Medical Staff, 7 nhóm).

---

## 1. Đối chiếu cấu trúc hiện tại vs đề xuất

### 1.1. Cấu trúc hiện tại (7 nhóm, thứ tự cố định trong `roles.ts`)

| # | Nhóm hiện tại | Mục con |
| --- | --- | --- |
| 1 | Tổng quan | Dashboard |
| 2 | Hồ sơ Sức khỏe & Dị ứng | Duyệt khai báo sức khỏe, Hồ sơ sức khỏe, Bữa ăn đặc biệt |
| 3 | Kiểm tra & Giám sát ATTP | Bảng giám sát tuân thủ, Kiểm tra an toàn bếp, Hồ sơ kiểm thực, Xem báo cáo ATTP, Trung tâm Báo cáo ATTP, Chính sách trường học |
| 4 | Lưu mẫu Thực phẩm | Sổ lưu mẫu (Mẫu số 5) |
| 5 | Sự cố An toàn Thực phẩm | Sự cố & Điều tra, Hành động khắc phục |
| 6 | Quản lý Vi phạm | Sổ vi phạm & Xử lý |
| 7 | Nhà trường | Thông báo trường |

**Vấn đề đã xác định**: việc hàng ngày (kiểm tra bếp, kiểm thực, lưu mẫu) bị rải ra 2 nhóm khác nhau (3 và 4); luồng sự cố→vi phạm bị tách thành 2 nhóm dù về nghiệp vụ là 1 chuỗi liên tục (Section C "Nguyên nhân gốc rễ" của Incident Detail đã có sẵn nút "Lập biên bản liên quan" dẫn sang Violations — xem `review_medicalstaff_violations.md` Mục 2.6); báo cáo/thống kê (đọc, nhìn lại) bị trộn chung với thao tác kiểm tra hàng ngày (ghi, làm việc).

### 1.2. Đề xuất phỏng vấn (Mục I) — đọc kỹ thứ tự ưu tiên nêu trong văn bản gốc

> - Module 1: Dữ liệu đầu kỳ **đẩy xuống thứ 3** ("t3") — Hồ sơ sức khỏe học sinh; Danh mục & Hồ sơ pháp lý NCC (đã duyệt).
> - Module 2: Công việc kiểm tra hàng ngày **đẩy lên đầu giao diện** — Kiểm tra an toàn bếp & nguyên liệu đầu vào; Sổ kiểm thực 3 bước; Sổ lưu mẫu & Hủy mẫu.
> - Module 3: Quy trình xử lý sự cố phát sinh **đẩy lên thứ 2** — Tiếp nhận báo cáo sự cố; Mở phiên điều tra, cách ly lô hàng, lập biên bản vi phạm, theo dõi khắc phục.
> - Module 4: Báo cáo & Thống kê cuối cùng — Bảng giám sát tuân thủ, sổ vi phạm xử lý; thống kê tần suất sự cố, sai phạm định kỳ.

Đọc đúng ký hiệu vị trí trong văn bản gốc ("đẩy xuống t3" = đẩy xuống **thứ 3**, không phải viết tắt mơ hồ): thứ tự ưu tiên hiển thị là **Module 2 (vị trí 1) → Module 3 (vị trí 2) → Module 1 (vị trí 3) → Module 4 (cuối cùng)** — xếp theo tần suất thao tác thực tế (việc hàng ngày trước, việc phát sinh không thường xuyên sau, việc thiết lập đầu kỳ/hiếm khi đổi xếp gần cuối, báo cáo nhìn lại xếp cuối cùng).

---

## 2. Quyết định (26/07/2026)

### 2.1. Danh mục NCC — KHÔNG thêm vào Medical Staff

Phỏng vấn tự mâu thuẫn: liệt kê "Danh mục & Hồ sơ pháp lý NCC" trong Module 1 của Medical Staff nhưng chính Mục II.6 lại chú thích đây là "tính năng của cateerers". Người dùng xác nhận: **không thêm** — đây là điểm phỏng vấn ghi nhầm vị trí, giữ đúng theo chú thích gốc (thuộc Service Caterers). Module 1 của Medical Staff chỉ còn đúng "Hồ sơ sức khỏe học sinh" — khớp 100% nhóm "Hồ sơ Sức khỏe & Dị ứng" hiện có, không đổi nội dung.

### 2.2. Gộp "Sự cố An toàn Thực phẩm" + "Quản lý Vi phạm" thành 1 nhóm — GIỮ NGUYÊN "Sổ vi phạm" trong nhóm này (đã sửa sau phản biện của người dùng)

**Đề xuất ban đầu của tôi (SAI, đã bị người dùng bác bỏ)**: gộp 2 nhóm thành "Xử lý Sự cố & Vi phạm", nhưng tách "Sổ vi phạm & Xử lý" ra khỏi nhóm này, chuyển sang Module 4 (Báo cáo & Thống kê) — với lý do hành động "Lập biên bản" luôn đi qua ngữ cảnh 1 Incident cụ thể nên không cần link riêng ở Module 3.

**Lý do bị bác bỏ**: người dùng chỉ ra tình huống thực tế bị bỏ sót — Medical Staff đi kiểm tra bếp **trực tiếp** (không thông qua bất kỳ Incident nào), phát hiện vi phạm và cần lập biên bản **độc lập**, không gắn `incidentId`. Route `/violations/new` vốn đã hỗ trợ tạo mới không cần `incidentId` (xem `review_medicalstaff_violations.md` Mục 2.1 — `incidentId` chỉ là query param tùy chọn). Nếu chuyển link "Sổ vi phạm & Xử lý" sang Module 4 (nhóm báo cáo/nhìn lại), lối vào để TẠO MỚI độc lập này sẽ bị che sau nhãn "Báo cáo" — sai bản chất, vì đây vẫn là 1 hành động/case-workspace, không phải báo cáo thuần.

**Quyết định sửa lại**: "Sổ vi phạm & Xử lý" ở lại nhóm Module 3 — không chuyển sang Module 4. Nhóm Module 3 "Xử lý Sự cố & Vi phạm" gồm cả 3 mục cũ: **Sự cố & Điều tra**, **Hành động khắc phục**, **Sổ vi phạm & Xử lý** — chỉ gộp 2 nhóm cũ (5+6) thành 1, không tách/di chuyển mục con nào ra ngoài.

### 2.3. Tách nhóm "Báo cáo & Thống kê" (Module 4) khỏi nhóm ATTP hiện tại

Người dùng chốt hướng đơn giản hơn: ban đầu chỉ định đổi **thứ tự nhóm**, giữ nguyên nội dung từng nhóm. Riêng phần báo cáo/dashboard trong nhóm "Kiểm tra & Giám sát ATTP" hiện tại (đang gộp chung với việc hàng ngày), người dùng đồng ý tách ra thành nhóm Module 4 riêng vì hợp lý (không kéo phần "nhìn lại" lên đầu sidebar cùng việc hàng ngày):

- Phần **thao tác hàng ngày** (Kiểm tra an toàn bếp, Hồ sơ kiểm thực) → Module 2, cùng Sổ lưu mẫu (vốn đứng nhóm riêng, gộp lại).
- Phần **xem lại/báo cáo** (Bảng giám sát tuân thủ, Xem báo cáo ATTP, Trung tâm Báo cáo ATTP) → nhóm Module 4 riêng, cuối sidebar. **Không** có "Sổ vi phạm & Xử lý" trong nhóm này (đã sửa theo Mục 2.2).

Lý do: nguyên tắc IA — công cụ thao tác/vận hành (ghi dữ liệu mỗi ngày) đứng trước công cụ nhìn lại/phân tích (đọc số liệu tổng hợp) — khớp đúng ý "Báo cáo & Thống kê cuối cùng" của phỏng vấn.

"Chính sách trường học" (tài liệu tham khảo, không phải việc hàng ngày/sự cố/báo cáo) chuyển sang nhóm cuối "Nhà trường" cùng "Thông báo trường".

---

## 3. Cấu trúc nav đề xuất (đích cuối cùng — CHƯA áp code)

| Thứ tự | Nhóm | Mục con | Route (không đổi) |
| --- | --- | --- | --- |
| 1 | Tổng quan | Dashboard | `/medical-staff/dashboard` |
| 2 | **Module 2 — Công việc Kiểm tra Hàng ngày** | Kiểm tra an toàn bếp | `/medical-staff/conduct-inspection` |
| | | Hồ sơ kiểm thực | `/medical-staff/inspections` |
| | | Sổ lưu mẫu (Mẫu số 5) | `/medical-staff/samples` |
| 3 | **Module 3 — Xử lý Sự cố & Vi phạm** | Sự cố & Điều tra | `/medical-staff/incidents` |
| | | Hành động khắc phục | `/medical-staff/corrective-actions` |
| | | Sổ vi phạm & Xử lý | `/medical-staff/violations` |
| 4 | **Module 1 — Hồ sơ Sức khỏe & Dị ứng** | Duyệt khai báo sức khỏe | `/medical-staff/health-reviews` |
| | | Hồ sơ sức khỏe | `/medical-staff/health-records` |
| | | Bữa ăn đặc biệt | `/medical-staff/special-meals` |
| 5 | **Module 4 — Báo cáo & Thống kê** | Bảng giám sát tuân thủ | `/medical-staff/compliance` |
| | | Xem báo cáo ATTP | `/medical-staff/reports` |
| | | Trung tâm Báo cáo ATTP | `/medical-staff/report-center` |
| 6 | Nhà trường | Chính sách trường học | `/medical-staff/policies` |
| | | Thông báo trường | `/medical-staff/announcements` |

**Không đổi**: mọi route, mọi field, mọi component bên trong từng màn — đây thuần là thay đổi **thứ tự và cách nhóm** các entry trong mảng `nav` của `roles.ts["medical-staff"]`. "Chính sách trường học" chuyển từ nhóm 3 (ATTP) hiện tại sang nhóm 6 (Nhà trường). "Sổ vi phạm & Xử lý" ở lại nhóm Module 3 (Mục 2.2, sửa sau phản biện) — không sang Module 4.

---

## 4. Checklist thay đổi cần áp dụng theo file

| File / Component | Thay đổi | Trạng thái |
| --- | --- | --- |
| `src/lib/roles.ts` (dòng 175-238, khối `"medical-staff"`) | Viết lại mảng `nav`: 6 nhóm theo Mục 3 (đổi thứ tự nhóm, gộp/tách mục con, đổi tên nhóm — không đổi `href`/`icon` từng mục) | **ĐÃ ÁP CODE 27/07/2026** |
| Screen Spec tổng hợp `00_TongHop_MedicalStaff.md` | Cập nhật mô tả "Platform Layout — Sidebar" nếu có liệt kê tên nhóm nav cũ | CHƯA áp (lượt docs riêng) |
| `review_medicalstaff_plan.md` | Có thể gộp việc này vào 1 đợt "Đợt 0 — đổi IA nav" nếu triển khai cùng lúc với các đợt code khác đã lên kế hoạch ở file đó | Không cần — đã áp riêng, độc lập với 5 đợt của plan |

**Trạng thái chung**: quyết định đã chốt đầy đủ qua trao đổi trực tiếp 26/07/2026 — ~~chưa áp code~~ → **ĐÃ ÁP CODE `roles.ts` ngày 27/07/2026**, xem Mục 5.

---

## 5. Trạng thái áp dụng CODE — 27/07/2026

Đã viết lại mảng `nav` của `roles.ts["medical-staff"]` đúng bảng Mục 3:

| Thứ tự | Nhóm sau khi áp | Ghi chú so với bản cũ |
| --- | --- | --- |
| 1 | Tổng quan | Không đổi |
| 2 | **Công việc Kiểm tra Hàng ngày** | Gộp "Kiểm tra an toàn bếp" + "Hồ sơ kiểm thực" (tách khỏi nhóm ATTP cũ) + "Sổ lưu mẫu (Mẫu số 5)" (nhóm riêng cũ bị xoá, gộp vào đây) |
| 3 | **Xử lý Sự cố & Vi phạm** | Gộp 2 nhóm cũ 5+6; **"Sổ vi phạm & Xử lý" ở lại đây** đúng Mục 2.2 (không chuyển sang nhóm Báo cáo) |
| 4 | Hồ sơ Sức khỏe & Dị ứng | Giữ nguyên 3 mục con, chỉ đổi vị trí (từ nhóm 2 → nhóm 4) |
| 5 | **Báo cáo & Thống kê** | Tách từ nhóm ATTP cũ: Bảng giám sát tuân thủ · Xem báo cáo ATTP · Trung tâm Báo cáo ATTP |
| 6 | Nhà trường | Thêm "Chính sách trường học" (chuyển từ nhóm ATTP cũ) đứng trước "Thông báo trường" |

Xác nhận phạm vi: **không đổi `href`/`icon` của bất kỳ mục nào**, không thêm/bớt mục con nào (7 nhóm → 6 nhóm chỉ do gộp), không đụng route/field/BR. "Danh mục & Hồ sơ pháp lý NCC" không được thêm (Mục 2.1). Lý do sắp xếp được ghi thành comment ngay trong `roles.ts` để người đọc code sau không tưởng là ngẫu nhiên.

**Lưu ý kiểm thử**: chưa chạy được `tsc --noEmit` (sandbox Linux vẫn không khởi động được — cùng tình trạng ghi ở `review_medicalstaff_healthrecord.md` Mục 4.8 và `review_medicalstaff_compliance_kitcheninspection.md` Mục 8.8). Thay đổi này chỉ sắp xếp lại các phần tử mảng, mọi icon dùng đều đã có sẵn trong khối `import` của file — rủi ro biên dịch gần như bằng 0, nhưng vẫn nên chạy type-check cùng 2 đợt trên khi môi trường khả dụng.
