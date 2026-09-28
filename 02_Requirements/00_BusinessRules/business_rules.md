# Business Rules — School Food Safety Management Support System (SFMS)

> **Source**: Phân tích từ Report 1 (Project Introduction) và Report 3 (Software Requirement Specification)
> **Version**: 1.2  
> **Date**: 25/06/2026
> **Changelog v1.2 (2026-07-01)**: Chính thức hoá 2 đề xuất đã được duyệt — bổ sung Section 18 Quản lý Vi phạm & Xử lý Sai phạm (BR-183–190, mở rộng module MF07 Food Safety Inspection + MF11 Incident Management; `ViolationResolutionRecord` chạy **song song** với `CorrectiveActionRequest` hiện có, không thay thế) và Section 19 Quản lý Năm học (BR-191–195, mở rộng module MF02 School & Class Management). Xem `02_Requirements/Violation_Management_Proposal.md` và `02_Requirements/AcademicYear_Management_Proposal.md` cho bối cảnh đề xuất gốc.
> **Changelog v1.3 (2026-07-02)**: Đối chiếu Module 6 (Kho), 8 (Ba Bước), 9 (Lưu mẫu), 5 (Nhà cung cấp) với văn bản pháp luật gốc trong `00_Policies/` (QĐ 1246/QĐ-BYT, Luật ATTP 55/2010/QH12, NĐ 15/2018/NĐ-CP, TTLT 13/2016/TTLT-BYT-BGDĐT). Cụ thể hoá BR-111 (72 giờ, khớp Screen Spec hiện có, chú thích ngưỡng luật tối thiểu 24 giờ); bổ sung **BR-203** (khối lượng mẫu lưu tối thiểu — trước đây bị gán nhầm vào BR-107 ở một số Screen Spec); làm rõ điều kiện BR-105, nội dung nhãn BR-106, ngoại lệ chặn hủy BR-112; nâng ngưỡng cảnh báo BR-047 theo đúng thời hạn gia hạn giấy chứng nhận ATTP (Điều 37 Luật ATTP 2010); chú thích nguồn cho BR-090/BR-091 (không trích từ 00_Policies); làm rõ lại nội dung Bước 2 của BR-079; ghi chú thiếu nguồn cho BR-103.
> **Changelog v1.4 (2026-07-06)**: Sửa đổi **BR-207** — bỏ điều khoản "System Admin cấu hình chú thích căn cứ pháp lý, áp dụng chung mọi trường"; bổ sung **BR-210** mới (Medical Staff tự CRUD toàn bộ Kitchen Safety and Sanitation Checklist — nhóm, hạng mục, chú thích — theo từng trường, không qua phê duyệt), cấp **UC-186** tương ứng. Theo quyết định của người dùng chuyển checklist UC-014 sang mô hình dynamic — xem `MedicalStaff_KitchenInspectionChecklist_Proposal.md` Mục 0, `MedicalStaff_UserStory.md` US-22, `traceability_matrix.md`.
> **Changelog v1.5 (2026-07-06)**: Bổ sung **BR-211** (xuất Biên bản Vi phạm dạng PDF theo mẫu chuẩn + lưu ảnh biên bản đã ký 3 bên, áp dụng UC-178, UC-179, UC-180), dựa trên mẫu thực tế người dùng cung cấp `00_Policies/specialreport2173_1.md`. Bao gồm **gate bắt buộc (chặn cứng)**: không cho đóng biên bản (LOW)/gửi School Administration phê duyệt (MEDIUM/HIGH) nếu chưa có ảnh biên bản đã ký — quyết định người dùng 06/07/2026. Xem `21_RecordProcessViolation.md`, `22_RecordStaffViolation.md`, `24_RecordViolationResolution.md` (Web & Mobile).
> **Changelog v1.6 (2026-07-12)**: Sửa **BR-114** và bổ sung **BR-212** mới (UC-076 — Retrieve Food Sample Records): (1) field "Tên người ủy quyền" đổi thành tự động = tài khoản Medical Staff đăng nhập, readonly (nhất quán với `reportedBy`/BR-183, "Đại diện Y tế trường học"/BR-211); (2) bổ sung field "Đại diện phòng lab nhận mẫu" (tùy chọn) và "Chỉ tiêu yêu cầu kiểm nghiệm" (bắt buộc), căn cứ Phụ lục V (Biên bản bàn giao mẫu) của **Thông tư 14/2011/TT-BYT** (Bộ Y tế) — văn bản pháp lý chính thức về bàn giao mẫu thực phẩm cho đơn vị kiểm nghiệm, mới tìm và lưu tại `00_Policies/14_2011_TT-BYT_lấy mẫu thực phẩm phục vụ thanh tra kiểm tra.md`. **Không** áp dụng gate ký-2-bên/chụp ảnh như BR-211, vì "Phòng lab đích" là bên thứ ba ngoài hệ thống (không có tài khoản SFMS) và UC-076 diễn ra giữa lúc đang điều tra sự cố đang mở — thêm gate cứng có nguy cơ làm chậm tiến độ gửi mẫu đi xét nghiệm mà không có lợi ích tương xứng, khác bối cảnh BR-211 (2 bên đều là actor có tài khoản trong hệ thống). Xem `14_RetrieveFoodSampleRecordsDialog.md` (Web & Mobile).
> **Changelog v1.7 (2026-07-13)**: Cấp **BR-213** (UC-188 — Send Incident Summary Report) và **BR-214** (UC-189 — Send Violation Summary Report), mở rộng cơ chế "gửi báo cáo chủ động" của BR-209 (UC-185) sang 2 mảng dữ liệu Medical Staff quản lý còn thiếu kênh báo cáo lên School Administration: sự cố ATTP và vi phạm. Cả 3 UC (185/188/189) nay được gom vào 1 điểm vào chung — **UC-187 "ATTP Report Center"**. Đồng thời cấp **UC-190** (View Sent Reports History) — không cấp BR mới, chỉ là màn hình hiện thực hóa **BR-177** (Lưu trữ lịch sử báo cáo) đã có sẵn từ trước, áp riêng cho phạm vi Medical Staff. Nguyên tắc thiết kế cốt lõi: BR-213/BR-214 chỉ **tổng hợp số liệu (rollup)** từ dữ liệu đã ghi nhận — **không** lặp lại nội dung chi tiết đã có ở báo cáo từng sự cố (BR-154/UC-033) hay biên bản vi phạm đã ký (BR-211), tránh 2 nguồn dữ liệu cùng mô tả 1 sự việc. Bổ sung từ phản hồi người dùng (rà soát ảnh chụp màn hình thực tế, "Hình 7") — xem `03_Design/08_FrontEnd/Web/review/medical_staff/review_screen_medical_staff.md` Mục 17.
> **Changelog v1.8 (2026-07-15)**: Bổ sung chú thích căn cứ pháp lý vào **BR-149** — khung 48 giờ mặc định khi chọn "món ăn nghi vấn" để xét nghiệm/cách ly, dựa trên **Quyết định 39/2006/QĐ-BYT** ("Quy chế điều tra ngộ độc thực phẩm"), lưu tại `00_Policies/39_2006_QD-BYT_quy_che_dieu_tra_ngo_doc_thuc_pham.md`. Không cấp BR mới — chỉ làm rõ căn cứ cho cơ chế đã có. Thiết kế lại luồng "Sự cố & Điều tra" (biên bản sự cố thủ công mở rộng UC-114, gộp UC-009 vào cùng màn Incident Detail, gộp trình bày UC-168/UC-033) không đổi bất kỳ định nghĩa BR nào ở §11 — chỉ đổi cách trình bày màn hình. Xem `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 10.
> **Changelog v1.9 (2026-07-28)**: Sửa **BR-004** theo quyết định người dùng (Nam) — chuyển từ "hết phiên sau 30 phút, bắt đăng nhập lại" sang mô hình **access token + refresh token** để người dùng không phải đăng nhập lại thường xuyên (access token vẫn hết hạn sau 30 phút không hoạt động như cũ, nhưng refresh token tự gia hạn ngầm nếu app còn được mở trong vòng 14 ngày). Bổ sung **BR-218** (Đăng nhập bằng Google — OAuth2 Social Login), cấp **UC-191** tương ứng — xem `traceability_matrix.md` §1.1 và Report 3.md §3.2.1. Tham số "14 ngày" là đề xuất mặc định, có thể điều chỉnh.
> **Changelog v1.10 (2026-07-31)**: Bổ sung **BR-219** (Sức chứa lớp học không được nhỏ hơn sĩ số hiện tại), đóng khoảng trống **§2 L8b** của `TestSpec-MF02_School_Class_Management.md` — TDS §10 (`SCH-010`) và §17.1 C4 đã enforce luật này nhưng chưa có BR gốc. Quyết định người dùng (Nam) 2026-07-31: giữ luật, cấp BR mới thay vì bỏ.
> **Changelog v2.2 (2026-08-11)**: Bổ sung **BR-223** (Chặn đăng nhập nếu trường liên kết không ACTIVE) — chính thức hóa guard đã có sẵn trong `AuthService.completeLogin` (comment "Bổ sung 11/08/2026") nhưng chưa từng có mã BR: tài khoản active + email verified vẫn login được dù trường của họ đang PENDING chờ duyệt hoặc đã bị vô hiệu hóa, vì BR-010 chỉ đọc trạng thái tài khoản chứ không đọc trạng thái trường. Áp dụng cho cả UC-034 (email/mật khẩu) và UC-191 (Google) vì hai luồng dùng chung `completeLogin`. Không cấp mã lỗi/message riêng — dùng lại MSG-003. Nhân tiện bổ sung **BR-218** (Google login, đã có từ v1.9) vào bảng ma trận Phụ lục — bị bỏ sót khi thêm ở v1.9, không phải nội dung mới.
> **Changelog v2.1 (2026-08-03)**: Bổ sung **BR-221** (Xác nhận hai chiều biên bản kiểm tra bếp bởi đơn vị suất ăn) và **BR-222** (Đánh giá độc lập của Y tế trên phiên kiểm thực ba bước) — chính thức hóa **CF-10** của `TDS-MF07_Backend.md` §2.3: **6 field** đã tồn tại trong mock frontend từ 26/07/2026 (`KitchenInspection.confirmStatus`/`recordCaterersRep`/`recordPdfExportedAt`/`recordSignedFiles`; `InspectionSession.medicalReviewResult`/`Reason`/`By`/`At`) nhưng **nguồn duy nhất là file review**, không có trong tài liệu này lẫn `traceability_matrix.md`. Quyết định (Huy) 2026-08-03: **chính thức hóa**, không descope. **Không cấp UC mới** — BR-221 nằm trong luồng UC-014, BR-222 gắn UC-112 (hành vi của Medical Staff trên màn xem hồ sơ kiểm thực). Hai điểm cần nhấn để không hiểu sai: (a) BR-221 **không phải cổng chặn** — chưa xác nhận thì bếp vẫn vận hành, nhất quán với quyết định descope 05/07/2026 ở ghi chú sau BR-208; (b) BR-222 **không ghi đè** `overall` và không đổi kết quả BR-094/BR-098 — `overall` là kết luận của **đơn vị thực hiện** theo QĐ 1246, `medicalReviewResult` là ý kiến **giám sát độc lập** của nhà trường, hai chỉ số song song. Nguồn gốc phát hiện: Bước 1 của MF07 (`02_Module_Completion_Workflow.md` §7).
> **Changelog v1.11 (2026-08-02)**: **Sửa BR-060 và BR-041**, đóng 2 mục treo của MF04 (CF-M04-3, ADR-M04-6). **(1) BR-060** đổi từ *"Xác thực vị trí GPS khi giao hàng"* thành **"Mã hóa dữ liệu sinh trắc học (AES-256)"**. Bản GPS **không hiện thực được**: hệ thống chỉ có 7 vai trò (`RoleKey`) và **không có** nhân viên giao hàng — họ là *đối tượng* chứ không phải tác nhân (UC-068/UC-028 do Raw Food Supplier thực hiện, UC-079 do Service Caterers quét **tại cổng trường** theo BR-051), nên không có thiết bị nào của họ trong hệ thống để đọc GPS. Đồng thời việc xác minh GPS khi nhận hàng **đã được BR-028 + BR-073 phủ trọn**: BR-028 chốt toạ độ trung tâm trường + geofence 200m (cấu hình 50–500m) và ghi rõ MF06/MF07/MF08 dùng làm tham chiếu; BR-073 buộc tự động ghi GPS + thiết bị + timestamp trong quá trình nhận hàng — toạ độ đo được là của **thiết bị bên nhận**. Nội dung mới khớp `traceability_matrix.md` (vốn đã ghi AES-256) và cả hai UC trích BR-060 đều là UC **sinh trắc học**; trước khi sửa, tài liệu này **không có** quy tắc nào về mã hóa dữ liệu sinh trắc (AES-256 chỉ xuất hiện ở BR-163 backup, BR-164 credentials). *Hệ quả kỹ thuật:* MF04 bỏ `GateVerificationPort`/port GPS. **(2) BR-041** bổ sung trạng thái **`Suspended`** kèm ngữ nghĩa cascade (đối tác mất quyền giao hàng ngay; mọi tài khoản thuộc đối tác bị khóa theo qua `user_accounts.partner_id`, thu hồi refresh token). Trạng thái này **đã tồn tại** ở `data_model.puml` (C5), `ADR-M04-6` và code MF01 (`AccountCascadeHandler.onPartnerSuspended()`), nhưng BR chỉ liệt kê 3 trạng thái nên trạng thái thứ tư không có căn cứ nghiệp vụ. Quyết định (Huy) 2026-08-02: ghi nhận chính thức thay vì để dạng "mở rộng".

---

## Mục lục

1. [Quản lý Người dùng & Xác thực (User Management & Authentication)](#1-quản-lý-người-dùng--xác-thực)
2. [Quản lý Vai trò & Phân quyền (RBAC)](#2-quản-lý-vai-trò--phân-quyền-rbac)
3. [Quản lý Trường học & Lớp học](#3-quản-lý-trường-học--lớp-học)
4. [Quản lý Học sinh & Phụ huynh](#4-quản-lý-học-sinh--phụ-huynh)
5. [Quản lý Nhà cung cấp (Supplier & Caterer)](#5-quản-lý-nhà-cung-cấp-supplier--caterer)
6. [Quản lý Nguyên liệu & Kho](#6-quản-lý-nguyên-liệu--kho)
7. [Quản lý Thực đơn & Dinh dưỡng](#7-quản-lý-thực-đơn--dinh-dưỡng)
8. [Kiểm tra Thực phẩm Ba Bước (Three-Step Food Inspection)](#8-kiểm-tra-thực-phẩm-ba-bước)
9. [Lưu mẫu Thực phẩm (Food Sample Retention)](#9-lưu-mẫu-thực-phẩm)
10. [Phục vụ & Phân phát Bữa ăn](#10-phục-vụ--phân-phát-bữa-ăn)
11. [Quản lý Sự cố Thực phẩm (Incident Management)](#11-quản-lý-sự-cố-thực-phẩm)
12. [Giám sát An toàn Thực phẩm (Food Safety Monitoring)](#12-giám-sát-an-toàn-thực-phẩm)
13. [Phản hồi & Thông báo](#13-phản-hồi--thông-báo)
14. [AI & Dịch vụ Hỗ trợ](#14-ai--dịch-vụ-hỗ-trợ)
15. [Audit Log & Bảo mật Dữ liệu](#15-audit-log--bảo-mật-dữ-liệu)
16. [Báo cáo & Phân tích (Reporting & Analytics)](#16-báo-cáo--phân-tích-reporting--analytics)
17. [Offline & Đồng bộ Dữ liệu (Offline & Data Sync)](#17-offline--đồng-bộ-dữ-liệu-offline--data-sync)
18. [Quản lý Vi phạm & Xử lý Sai phạm (Violation Management)](#18-quản-lý-vi-phạm--xử-lý-sai-phạm)
19. [Quản lý Năm học (Academic Year Management)](#19-quản-lý-năm-học)

---

## 1. Quản lý Người dùng & Xác thực

### BR-001 — Mã hóa mật khẩu
- Tất cả mật khẩu người dùng **phải** được mã hóa bằng thuật toán BCrypt trước khi lưu vào cơ sở dữ liệu.
- Hệ thống không bao giờ lưu trữ mật khẩu dưới dạng văn bản thuần (plain text).

### BR-002 — Khóa tài khoản do đăng nhập sai
- Sau **5 lần** đăng nhập thất bại liên tiếp, hệ thống **tự động khóa tài khoản** trong **15 phút**.
- Sự kiện khóa tài khoản phải được ghi vào audit log.

### BR-003 — Thời hạn token đặt lại mật khẩu
- Token đặt lại mật khẩu (Forgot Password) chỉ có hiệu lực trong **1 giờ** kể từ khi tạo.
- Sau khi hết hạn, người dùng phải yêu cầu gửi lại link mới.
- Hệ thống giới hạn tối đa **1 yêu cầu** đặt lại mật khẩu mỗi **15 phút** cho mỗi email.

### BR-004 — Timeout phiên đăng nhập & Refresh Token *(sửa 2026-07-28 v1.9 — xem changelog)*
- **Access token**: tự động hết hạn sau **30 phút** không có hoạt động (không đổi so với bản gốc).
- **Refresh token**: cấp kèm access token khi đăng nhập, hiệu lực **14 ngày** kể từ lần cấp/gia hạn gần nhất. Hệ thống dùng refresh token để tự động gia hạn access token ngầm mỗi khi ứng dụng còn hoạt động — người dùng **không phải đăng nhập lại** miễn mở ứng dụng ít nhất 1 lần mỗi 14 ngày.
- Refresh token bị **vô hiệu hóa ngay** khi: người dùng logout thủ công; tài khoản bị khóa do đăng nhập sai (BR-002) hoặc bị vô hiệu hóa (UC-002/UC-003); hoặc đăng nhập trên thiết bị khác (BR-005 — single session, refresh token cũ bị thay thế).
- Sau **14 ngày** không mở ứng dụng, refresh token hết hạn → người dùng bị chuyển về màn hình đăng nhập.

### BR-005 — Đăng nhập một thiết bị (Single Session)
- Mỗi tài khoản chỉ được phép **đăng nhập trên một thiết bị** tại một thời điểm.
- Đăng nhập mới từ thiết bị khác sẽ vô hiệu hóa phiên hiện tại.

### BR-006 — Chính sách độ mạnh mật khẩu
- Mật khẩu phải đáp ứng chính sách bảo mật tối thiểu (độ dài, ký tự đặc biệt, v.v.).
- Mật khẩu mới **không được trùng** với **3 mật khẩu gần nhất** (BR-015).

### BR-009 — Chấp nhận điều khoản khi đăng nhập lần đầu
- Trong lần đăng nhập **đầu tiên**, người dùng bắt buộc phải chấp nhận **Chính sách Bảo mật** và **Điều khoản Sử dụng**.
- Hệ thống không cho phép truy cập chức năng trước khi người dùng xác nhận.

### BR-010 — Xác minh email trước khi đăng nhập
- Tài khoản chỉ được phép đăng nhập sau khi **xác minh địa chỉ email** thành công.
- Tài khoản chưa xác minh sẽ bị chặn đăng nhập và được nhắc thực hiện xác minh.

### BR-011 — Thời hạn token kích hoạt tài khoản
- Link kích hoạt tài khoản (Account Activation) chỉ có hiệu lực trong **48 giờ** kể từ khi tạo.
- Nếu hết hạn, hệ thống cho phép gửi lại link mới, gia hạn thêm 48 giờ.

### BR-012 — Định dạng thông tin người dùng
- Số điện thoại phải theo định dạng hợp lệ; hệ thống hiển thị cảnh báo nếu định dạng sai.
- Email phải là địa chỉ email hợp lệ và duy nhất trong hệ thống.

### BR-223 — Chặn đăng nhập nếu trường liên kết không ACTIVE *(mới 2026-08-11 — xem changelog v2.2)*
- Với tài khoản có `schoolId` (thuộc một trường cụ thể), hệ thống chỉ cho đăng nhập nếu trường liên kết đang ở trạng thái **ACTIVE**. Trường đang chờ duyệt (PENDING) hoặc đã bị vô hiệu hóa chặn đăng nhập của mọi tài khoản thuộc trường đó, kể cả khi tài khoản đã xác minh email và mật khẩu đúng (BR-010 chỉ đọc trạng thái tài khoản, không đọc trạng thái trường — lỗ hổng này được đóng bởi BR-223).
- **System Admin** (không gắn `schoolId`) không bị áp guard này.
- Nếu không xác định được trạng thái trường (không tìm thấy hàng tương ứng), hệ thống **cho qua** thay vì chặn — MF01 cố tình tách khỏi vòng đời entity trường của MF02 (đọc qua `ReferenceResolver`, không JOIN trực tiếp); orphan `schoolId` là lỗi toàn vẹn dữ liệu thuộc phạm vi khác, không phải điều guard đăng nhập này nhắm tới.
- Áp dụng cho cả đăng nhập email/mật khẩu (UC-034) lẫn đăng nhập Google (UC-191, BR-218) — dùng chung logic `completeLogin`.
- Vi phạm trả lỗi giống MSG-003 (tài khoản đã bị vô hiệu hóa), không tạo mã lỗi/message riêng.

### BR-218 — Đăng nhập bằng Google (OAuth2 Social Login) *(mới 2026-07-28 — xem changelog v1.9)*
- Người dùng có thể đăng nhập bằng tài khoản Google (OAuth2 Authorization Code flow) thay cho email/mật khẩu.
- **Chỉ liên kết, không tự tạo tài khoản mới**: nếu email Google trùng với email của một tài khoản SFMS đã tồn tại (được tạo qua UC-024/UC-026), hệ thống tự động liên kết tài khoản đó với Google cho lần đăng nhập đầu tiên. Nếu email Google **chưa tồn tại** trong hệ thống, đăng nhập bị chặn — hệ thống **không** tự tạo tài khoản mới qua Google (giữ nguyên BR-013: chỉ School Administration/System Admin được tạo tài khoản).
- Email Google được coi là đã xác minh (Google tự xác minh), thỏa điều kiện BR-010 mà không cần gửi lại email xác minh riêng.
- Không áp dụng BR-001/BR-006/BR-015 (hash/chính sách/lịch sử mật khẩu) cho phiên đăng nhập qua Google vì không có mật khẩu cục bộ. Các BR còn lại (BR-002 lockout, BR-004 session/refresh token, BR-005 single-session, BR-007 RBAC) áp dụng như bình thường.

---

## 2. Quản lý Vai trò & Phân quyền (RBAC)

### BR-007 — Phân quyền theo vai trò (RBAC)
- Hệ thống áp dụng mô hình **Role-Based Access Control (RBAC)**.
- Mỗi người dùng được gán một hoặc nhiều vai trò; quyền truy cập được xác định hoàn toàn bởi vai trò.
- Các vai trò trong hệ thống gồm: **System Admin, School Administration, Medical Staff, Teacher, Parent, Raw Food Supplier, Service Caterers**.

### BR-008 — Bảo vệ vai trò hệ thống mặc định
- Các vai trò **mặc định của hệ thống** không thể bị xóa.
- Chỉ các vai trò **tùy chỉnh (custom)** mới có thể bị xóa bởi System Admin.
- Một vai trò chỉ có thể bị xóa khi **không có người dùng nào** (kể cả đã vô hiệu hóa) đang được gán vai trò đó.

### BR-013 — Phân quyền tạo nhân viên
- Chỉ **School Administration** mới có quyền tạo tài khoản nhân viên (staff) thuộc trường của mình.
- **System Admin** có quyền tạo tài khoản người dùng trên toàn hệ thống.

---

## 3. Quản lý Trường học & Lớp học

### BR-020 — Một giáo viên chủ nhiệm mỗi lớp
- Mỗi lớp học chỉ được phép có **một giáo viên chủ nhiệm** tại một thời điểm.
- Khi gán giáo viên mới, hệ thống tự động hủy liên kết giáo viên cũ và ghi nhật ký thay đổi.

### BR-021 — Vô hiệu hóa trường học dạng thác nước (Cascade)
- Khi **vô hiệu hóa** một trường học, hệ thống tự động **tạm dừng** tất cả:
  - Lớp học thuộc trường đó
  - Tài khoản nhân viên thuộc trường
  - Tài khoản học sinh thuộc trường
- Thao tác này phải được ghi vào audit log.

### BR-022 — Quyền xem chính sách trường học theo vai trò
- Người dùng chỉ có thể xem chính sách của **trường mà họ thuộc về** (theo schoolId trong JWT).
- **Teacher, Medical Staff, Service Caterers** có quyền xem chính sách của trường mình để thực hiện công việc hàng ngày.
- Người dùng không thể xem chính sách của trường khác — mọi truy cập cross-school bị từ chối.

### BR-024 — Cấp độ lớp hợp lệ
- Cấp lớp học phải nằm trong khoảng **1 đến 12** (tương ứng với hệ thống giáo dục phổ thông Việt Nam).

### BR-025 — Sức chứa tối đa của lớp học
- Mỗi lớp học có sức chứa tối đa **50 học sinh**.

### BR-026 — Ghi nhật ký phân công giáo viên
- Mỗi lần phân công hoặc thay thế giáo viên chủ nhiệm đều phải được ghi vào **audit trail** kèm ngày giờ và thông tin thực hiện.

### BR-219 — Sức chứa lớp học không được nhỏ hơn sĩ số hiện tại *(mới 2026-07-31 — xem changelog v1.10)*
- Khi cập nhật sức chứa (`capacity`) của một lớp học đã có học sinh, giá trị mới **không được nhỏ hơn** số học sinh hiện đang học tại lớp đó (kể cả khi giá trị mới vẫn ≤ 50, giới hạn của BR-025).
- Áp dụng cho `UC-018` (Create Classroom), `UC-090` (Update Classroom), `UC-177` (Promote Students to Next Academic Year).

### BR-028 — Xác minh và khóa tọa độ GPS của trường học
- Tọa độ GPS của trường phải hợp lệ: **latitude** trong khoảng **-90.0 đến 90.0**, **longitude** trong khoảng **-180.0 đến 180.0**.
- Hệ thống từ chối lưu nếu tọa độ nằm ngoài phạm vi trên.
- Sau khi tọa độ GPS được xác nhận (trường chuyển sang trạng thái **ACTIVE**), trường `coordinateLocked` được đặt thành `true`.
- Khi `coordinateLocked = true`, mọi yêu cầu cập nhật tọa độ bị **từ chối** — các thông tin khác (địa chỉ, điện thoại, mô tả) vẫn được phép cập nhật bình thường; mọi xác minh vị trí đều dựa theo tọa độ GPS, không phụ thuộc địa chỉ văn bản.
- **Ngoại lệ mở khóa**: Chỉ **System Admin** mới có quyền đặt lại `coordinateLocked = false` để cho phép cập nhật tọa độ. Thao tác này bắt buộc phải kèm **lý do bằng văn bản** và được ghi vào **audit log** (người thực hiện, thời gian, lý do). Sau khi tọa độ mới được lưu, `coordinateLocked` tự động trả về `true`.
- Tọa độ GPS được sử dụng bởi các module khác (MF06 Ingredient Receiving, MF07 Three-Step Inspection, MF08 Food Sample Retention) làm điểm tham chiếu trung tâm để xác minh vị trí hoạt động trong khu vực hợp lệ của trường.
- **Bán kính geofence mặc định**: Khu vực hợp lệ được tính theo bán kính **200 mét** từ tọa độ trung tâm của trường. School Administration có thể cấu hình bán kính này trong khoảng **50 m – 500 m** tùy điều kiện thực tế. Mọi thay đổi bán kính geofence phải được ghi vào audit log.

---

## 4. Quản lý Học sinh & Phụ huynh

### BR-030 — Liên kết học sinh với phụ huynh
- Mỗi học sinh phải được liên kết với ít nhất một tài khoản phụ huynh.
- Thông tin dị ứng và chế độ ăn đặc biệt của học sinh phải được khai báo và lưu trong hồ sơ học sinh.

### BR-031 — Xét duyệt thông tin sức khỏe
- Thông tin sức khỏe và dị ứng do phụ huynh khai báo phải được **Medical Staff xem xét và xác nhận** trước khi có hiệu lực.

### BR-032 — Nhập hàng loạt học sinh và phụ huynh
- Hệ thống hỗ trợ nhập hàng loạt (bulk import) hồ sơ học sinh và phụ huynh qua template được định nghĩa sẵn với quy tắc kiểm tra dữ liệu (validation rules).

### BR-201 — Giới hạn liên kết học sinh trên tài khoản phụ huynh
- Một tài khoản phụ huynh được liên kết tối đa **5 hồ sơ học sinh**. Khi đã đạt giới hạn, hệ thống chặn thao tác liên kết thêm và yêu cầu School Administration xác minh trước khi xử lý ngoại lệ.
- *(Bổ sung 2026-07-01: mã số cũ cho quy tắc này trong Report 3 là "BR-040", nhưng khi `business_rules.md` canonical được tạo, số BR-040 đã được gán cho quy tắc khác — "Quy trình đăng ký nhà cung cấp" (§5). Đây là 1 dạng ghost-code khác với 37 mã đã liệt kê trong `BR_Citation_Audit_Report.md` (ở đó mã cũ không tồn tại trong canonical; ở đây mã cũ vẫn tồn tại nhưng bị tái sử dụng cho quy tắc khác) — phát hiện độc lập bởi Nguyễn Phương Thảo, Nguyễn Đức Mạnh, Nguyễn Văn Nam khi viết Screen Specification cho School & Class Management/Student & Parent Management. Toàn bộ trích dẫn "BR-040" sai ngữ cảnh trong Report 3.md (UC-025, UC-050, UC-053, UC-093, UC-135, UC-141) đã được sửa lại thành BR-201.)*

---

## 5. Quản lý Nhà cung cấp (Supplier & Caterer)

### BR-040 — Quy trình đăng ký nhà cung cấp
- Nhà cung cấp phải **nộp đầy đủ hồ sơ** bao gồm: hồ sơ công ty, giấy phép kinh doanh, chứng nhận an toàn thực phẩm, và thông tin sản phẩm.
- Hồ sơ phải được **School Administration xem xét và phê duyệt** trước khi nhà cung cấp được phép hoạt động.

### BR-041 — Trạng thái nhà cung cấp
- Nhà cung cấp có thể ở các trạng thái: **Pending (Chờ duyệt) → Approved (Đã duyệt) / Rejected (Từ chối)**, và **Suspended (Đình chỉ)** từ trạng thái Approved.
- Chỉ nhà cung cấp ở trạng thái **Approved** mới có thể giao nguyên liệu cho trường.
- **Suspended** áp dụng khi đối tác đang hợp tác bị đình chỉ (VD tài liệu tuân thủ hết hạn, vi phạm an toàn thực phẩm): đối tác **mất quyền giao hàng ngay**, và **mọi tài khoản người dùng thuộc đối tác đó bị khóa theo** (cascade — `user_accounts.partner_id`, thu hồi refresh token).
- *(Bổ sung 2026-08-02: `SUSPENDED` vốn đã tồn tại trong `data_model.puml` (ràng buộc C5) và trong `ADR-M04-6` (cascade đình chỉ tài khoản) — MF01 đã hiện thực `AccountCascadeHandler.onPartnerSuspended()`. Quy tắc này trước đây chỉ liệt kê 3 trạng thái nên trạng thái thứ tư **không có BR chống lưng**, dù code và mô hình dữ liệu đều đã có. Ghi nhận chính thức thay vì để dạng "mở rộng".)*

### BR-046 — Yêu cầu tài liệu tuân thủ
- Nhà cung cấp phải duy trì các tài liệu tuân thủ hợp lệ: chứng nhận an toàn thực phẩm, giấy phép kinh doanh, tài liệu xuất xứ sản phẩm.
- Tài liệu quá hạn sẽ được cảnh báo và có thể ảnh hưởng đến trạng thái hợp tác.

### BR-047 — Cảnh báo hết hạn tài liệu
- Hệ thống hiển thị cảnh báo **"Expiring Soon"** khi Giấy chứng nhận cơ sở đủ điều kiện an toàn thực phẩm (hoặc tài liệu tuân thủ khác) của nhà cung cấp còn **dưới 6 tháng (180 ngày)** trước khi hết hạn — khớp thời hạn nộp hồ sơ xin cấp lại theo Điều 37 Luật An toàn thực phẩm 2010 (Giấy chứng nhận có hiệu lực 3 năm, phải nộp hồ sơ gia hạn trước 6 tháng).
- Hệ thống nâng mức cảnh báo lên **"Urgent"** khi còn **dưới 30 ngày** và tài liệu chưa được gia hạn.
- *(Sửa 2026-07-02: ngưỡng cũ "dưới 30 ngày" duy nhất không đủ thời gian để nhà cung cấp hoàn tất thủ tục gia hạn theo luật — xem `00_Policies/55_2010_QH12...md` Điều 37.)*

### BR-048 — Từ chối nhà cung cấp
- Khi từ chối, **lý do từ chối** phải được ghi lại trong hệ thống và gửi thông báo cho nhà cung cấp.

### BR-050 — Đăng ký nhân viên giao hàng
- Mỗi nhân viên giao hàng của nhà cung cấp phải được đăng ký với: họ tên, số CMND/CCCD, số điện thoại, biển số xe.
- Mã CMND/CCCD phải là duy nhất trong hệ thống.

### BR-051 — Xác thực sinh trắc học (Face Biometric)
- Nhân viên giao hàng phải đăng ký dữ liệu khuôn mặt (biometric) để được xác thực tại cổng trường.
- Dữ liệu sinh trắc học của nhân viên giao hàng có thể bị xóa bởi nhà cung cấp, dẫn đến mất quyền truy cập cổng.
- **Lưu ý**: Không áp dụng xác thực sinh trắc học với học sinh (trẻ em) vì lý do bảo mật dữ liệu cá nhân (PDPD).

### BR-060 — Mã hóa dữ liệu sinh trắc học
- Dữ liệu sinh trắc học khuôn mặt của nhân viên giao hàng phải được **mã hóa AES-256** khi lưu trữ và khi truyền.
- Hệ thống **không lưu ảnh gốc**; chỉ giữ định danh do dịch vụ nhận diện trả về.
- *(Sửa 2026-08-02 — nội dung cũ **"Xác thực vị trí GPS khi giao hàng · Hệ thống xác minh vị trí GPS của nhân viên giao hàng"** đã bị thay, vì hai lý do:*
  1. *Nhân viên giao hàng **không phải người dùng hệ thống** — chỉ có 7 vai trò (`system-admin`, `school-admin`, `teacher`, `parent`, `medical-staff`, `service-caterers`, `supplier`), không vai trò nào là nhân viên giao hàng. Họ là **đối tượng** chứ không phải tác nhân: UC-068/UC-028 do Raw Food Supplier thực hiện, UC-079 do Service Caterers quét tại cổng trường (BR-051). Không có thiết bị nào của họ trong hệ thống để đọc GPS ⇒ quy tắc cũ **không thể hiện thực**.*
  2. *Việc xác minh GPS khi nhận hàng **đã được BR-028 + BR-073 phủ trọn**: BR-028 chốt toạ độ trung tâm của trường + geofence 200m (cấu hình 50–500m) và ghi rõ MF06/MF07/MF08 dùng làm điểm tham chiếu; BR-073 buộc **tự động ghi GPS + thiết bị + timestamp trong quá trình nhận hàng**. Toạ độ đo được là của **thiết bị bên nhận**, không phải của người giao.*
  *Nội dung mới khớp `traceability_matrix.md` và UC-068/UC-079 — cả hai UC trích BR-060 đều là UC **sinh trắc học**, không UC nào về logistics giao vận. Trước khi sửa, `business_rules.md` **không có** quy tắc nào về mã hóa dữ liệu sinh trắc (AES-256 chỉ xuất hiện ở BR-163 backup và BR-164 credentials tích hợp) — đây là lỗ hổng thật được lấp.)*

---

## 6. Quản lý Nguyên liệu & Kho

### BR-070 — Xác minh trạng thái nhà cung cấp khi nhận hàng
- Khi bắt đầu phiên nhận hàng, hệ thống **tự động xác minh** nhà cung cấp đang ở trạng thái **Approved**.
- Nếu nhà cung cấp không được phê duyệt, hệ thống từ chối phiên nhận hàng.

### BR-071 — Chứng nhận lô hàng bắt buộc
- Mỗi lô nguyên liệu nhập vào phải kèm theo đầy đủ chứng nhận: số lô, ngày sản xuất, hạn sử dụng, số lượng, và chứng chỉ liên quan.

### BR-072 — Chụp ảnh bằng chứng bắt buộc khi nhận hàng
- Nhân viên nhận hàng **bắt buộc phải chụp ảnh** nguyên liệu, nhãn, bao bì và tài liệu hỗ trợ làm bằng chứng.

### BR-073 — Ghi tọa độ GPS và timestamp tự động
- Hệ thống **tự động ghi nhận** tọa độ GPS, thông tin thiết bị, và timestamp trong quá trình nhận hàng — biện pháp chống gian lận.

### BR-074 — Xác minh vị trí nhận hàng
- Hệ thống xác minh hoạt động nhận hàng được thực hiện **trong khu vực nhận hàng được phép** của trường.

### BR-075 — Nguyên tắc FEFO (First Expired First Out)
- Hệ thống ưu tiên sử dụng nguyên liệu theo nguyên tắc **FEFO** — lô hàng hết hạn sớm nhất được xuất kho trước.

### BR-076 — Truy xuất lô nguyên liệu
- Hệ thống ghi nhận số lô nguyên liệu liên kết với từng thực đơn và phiên sản xuất để đảm bảo khả năng **truy xuất nguồn gốc đầy đủ**.

### BR-077 — Cảnh báo thiếu kho
- Hệ thống tự động phát cảnh báo **thiếu nguyên liệu** khi tồn kho không đủ đáp ứng nhu cầu kế hoạch bữa ăn.

### BR-078 — Từ chối lô nguyên liệu không đạt
- Lô nguyên liệu không đạt yêu cầu kiểm tra chất lượng có thể bị **từ chối (Rejected)**; lý do từ chối phải được ghi lại với ảnh bằng chứng.

### BR-202 — Khớp khu vực lưu trữ theo yêu cầu nhiệt độ
- Khi lưu kho, khu vực lưu trữ được chọn (Dry/Chilled/Frozen) phải khớp với nhóm nhiệt độ yêu cầu của nguyên liệu; hệ thống chặn lưu và báo lỗi nếu không khớp.
- *(Bổ sung 2026-07-01: mã số cũ cho quy tắc này trong Report 3 là "BR-081", nhưng số này đã bị chiếm bởi 1 rule hoàn toàn khác trong business_rules.md ["Món thay thế cho dị ứng phổ biến", nay đã xoá bỏ — xem ghi chú tại vị trí cũ]. Phát hiện khi rà lại BR-081 theo yêu cầu loại bỏ rule "món thay thế dị ứng" — nhiều file ScreenSpec/traceability_matrix (UC-061 Record Ingredient Storage) đang trích "BR-081" với nghĩa này, cần chuyển hết sang BR-202.)*

### BR-204 — Bắt buộc mã lý do và xác nhận quản lý khi điều chỉnh tồn kho thủ công
- Mọi thao tác điều chỉnh tồn kho thủ công (spoilage write-off, waste adjustment, audit correction) **bắt buộc** phải kèm theo: (1) **mã lý do** được chọn từ danh sách định sẵn (spoilage, waste, audit write-off, v.v.) và (2) **xác nhận/chữ ký quản lý** (manager sign-off).
- Hệ thống **chặn** ghi nhận giao dịch điều chỉnh nếu thiếu một trong hai yếu tố trên.
- Mọi giao dịch điều chỉnh phải được ghi vào audit trail kèm mã lý do và thông tin người phê duyệt (BR-170).
- *(Bổ sung 2026-07-02: mã số cũ cho quy tắc này trong Report 3 là "BR-082" — nhưng BR-082 trong business_rules.md canonical = "Chế biến riêng cho bữa ăn đặc biệt" (§7 Menu & Diet), hoàn toàn khác chủ đề. Phát hiện khi audit mục B2 — `00_TODO_GiaDinh_Master.md`. UC-064 Record Inventory Transactions và UC-045 AF1 đều yêu cầu cơ chế này; Screen Spec 31_RecordInventoryTransactionsLog.md đã ghi nhận khoảng trống BR. Cấp mã mới BR-204 thay vì tái sử dụng số đã có nghĩa khác.)*

### BR-205 — Khóa tự động nguyên liệu hết hạn trong kho
- Nguyên liệu có ngày hết hạn đã qua phải được **tự động khóa** trong hệ thống quản lý kho, ngăn không cho chọn để xuất kho hoặc sử dụng trong quá trình chế biến.
- Hệ thống hiển thị **cảnh báo trực quan** (badge "Expired") trên các lô đã hết hạn và loại chúng khỏi danh sách lựa chọn trong quy trình xuất kho.
- Lô nguyên liệu sắp hết hạn (trong ngưỡng cảnh báo) phải hiển thị **cảnh báo "Expiring Soon"** để nhắc nhở xuất kho ưu tiên theo nguyên tắc FEFO (BR-075).
- *(Bổ sung 2026-07-02: mã số cũ cho quy tắc này trong Report 3 là "BR-083" — nhưng BR-083 trong business_rules.md canonical = "Ghi nhận bằng chứng mỗi bước" (§8 Three-Step Inspection), hoàn toàn khác chủ đề. Report 3 dòng 6604 ghi nhận BR-083 với nghĩa "Expired ingredients must be locked" trong bảng tóm tắt Ingredient & Inventory — đây là ghost-citation cross-module. UC-045 Manage Inventory cần rule riêng này; MSG-052 cũng cần repoint từ BR-083 sang BR-205.)*

---

## 7. Quản lý Thực đơn & Dinh dưỡng

### BR-061 — Thời hạn nộp thực đơn để phê duyệt
- Thực đơn phải được nộp để phê duyệt **ít nhất 3 ngày** trước tuần sử dụng dự kiến.

### BR-062 — Mục tiêu dinh dưỡng theo nhóm tuổi
- Thực đơn phải đáp ứng **chỉ tiêu dinh dưỡng theo nhóm tuổi** học sinh (calo, đa dạng chất dinh dưỡng, cân bằng thành phần bữa ăn).

### BR-063 — Kiểm tra xung đột dị ứng
- Hệ thống **bắt buộc kiểm tra xung đột dị ứng** giữa thành phần thực đơn và hồ sơ dị ứng học sinh trước khi phê duyệt thực đơn.
- Nếu phát hiện xung đột nghiêm trọng, hệ thống **chặn** không cho nộp thực đơn.

### BR-064 — Tối ưu hóa thực đơn theo tồn kho
- Đề xuất thực đơn của AI phải ưu tiên các nguyên liệu **đang có sẵn trong kho** và tuân thủ **giới hạn chi phí** được thiết lập.

### BR-065 — Kế hoạch bữa ăn đặc biệt bắt buộc
- Đối với học sinh có dị ứng đã xác minh hoặc chế độ ăn đặc biệt, hệ thống **bắt buộc** Service Caterers phải tạo **kế hoạch bữa ăn riêng**.

### BR-066 — Danh mục chế độ ăn đặc biệt
- Hệ thống hỗ trợ các loại chế độ ăn đặc biệt: **Halal, Vegetarian (Chay), Gluten-Free, Nut-Free**, và các loại khác.

> ⚠️ **BR-066 là danh mục MỞ** — cụm *"và các loại khác"* là nguyên văn, có chủ đích. BR này **không** chống lưng cho việc **chặn** một `dietaryType` lạ. Hành vi chặn/thẩm định nằm ở **BR-220**, đừng cite BR-066 làm oracle cho việc từ chối. *(Ghi rõ 2026-08-02 sau khi `TestSpec-MF03` §2 L14 phát hiện TDS-MF03 đang cite BR-066 để reject `dietaryType="Keto"` — không có cơ sở.)*

### BR-220 — Chế độ ăn ngoài danh mục chuẩn phải qua thẩm định y tế
*(Mới 2026-08-02 — đóng L14/Q1 của `TestSpec-MF03`. Bổ sung cho BR-066, không thay thế.)*

- Danh mục **chuẩn** gồm 5 giá trị: **Halal, Vegetarian, Gluten-Free, Nut-Free, Lactose-Free**. Yêu cầu suất ăn đặc biệt có `dietaryType` thuộc danh mục này được Y tế **duyệt thẳng** (UC-078).
- `dietaryType` **ngoài** danh mục chuẩn **không bị từ chối** (BR-066 cho phép "các loại khác") nhưng **không được duyệt thẳng**: yêu cầu chuyển sang **`NEEDS_INFO`**, Y tế phải bổ sung căn cứ (chỉ định của bác sĩ, lý do tôn giáo/y khoa) rồi mới duyệt.
- Lý do: Service Caterers phải nấu được suất đó (**BR-065**). Nhận bừa một chế độ ăn không ai định nghĩa thì bếp không thực hiện được; từ chối thẳng thì trái BR-066 và loại oan các chế độ ăn hợp lệ hiếm gặp.
- Danh mục chuẩn là **dữ liệu cấu hình**, không hardcode — mở rộng được mà không sửa code.

### BR-067 — Khóa thực đơn đã phê duyệt
- Thực đơn đã ở trạng thái **Approved** hoặc **Published** không thể chỉnh sửa hoặc xóa.
- Chỉ thực đơn ở trạng thái **Draft** hoặc **Rejected** mới được phép chỉnh sửa.

### BR-068 — Quy trình phê duyệt thực đơn
- Thực đơn phải trải qua quy trình: **Draft → Pending Approval → Approved / Rejected → Published**.
- Chỉ **School Administration** mới có quyền phê duyệt hoặc từ chối thực đơn.

### BR-069 — Khai báo nguyên liệu đầy đủ
- Service Caterers phải khai báo **toàn bộ nguyên liệu và chất gây dị ứng tiềm ẩn** trong tất cả các món của thực đơn trước khi nộp phê duyệt.

### BR-080 — Trần chi phí thực đơn
- Thực đơn phải tuân thủ **giới hạn chi phí** đã được School Administration thiết lập.

### ~~BR-081 — Món thay thế cho dị ứng phổ biến~~ **ĐÃ XOÁ (2026-07-01)**
> *Nội dung cũ: "Thực đơn phải có món thay thế cho 5 loại dị ứng thực phẩm phổ biến nhất nếu được áp dụng."* Xoá theo quyết định của team sau khi rà soát: (1) danh sách "5 loại phổ biến nhất" không có định nghĩa rõ ràng (cố định hay tính động theo trường, không rule nào nói); (2) không có field/entity nào trong class diagram hỗ trợ khái niệm "món thay thế" (`Dish` chỉ có `allergenList`/`isSpecialDish`); (3) trùng lặp chức năng với cơ chế đã có sẵn và đặc tả đầy đủ hơn — Special Meal Plan (BR-065, UC-023) xử lý dị ứng đã xác minh bằng suất ăn riêng, chế biến tách biệt (BR-082); (4) mệnh đề "nếu được áp dụng" không thể validate/enforce được trong hệ thống. Xem thảo luận đầy đủ trong lịch sử trao đổi dự án. Vấn đề UX gốc (tránh Caterer phải chọn lại thực đơn nhiều lần khi có xung đột dị ứng) nên giải quyết qua cơ chế AI Assist (UC-075) đã có sẵn hard-filter dị ứng, kích hoạt sớm hơn thay vì thêm rule "món thay thế" mới.

### BR-082 — Chế biến riêng cho bữa ăn đặc biệt
- Bữa ăn đặc biệt phải được chế biến hoàn toàn **riêng biệt**, sử dụng dụng cụ nấu riêng để tránh nhiễm chéo.

---

## 8. Kiểm tra Thực phẩm Ba Bước

### BR-079 — Ba bước kiểm tra bắt buộc
- Mỗi lô thức ăn phải trải qua **đúng 3 bước kiểm tra** theo Điều 3-5, QĐ 1246/QĐ-BYT:
  - **Bước 1**: Kiểm tra nguyên liệu trước khi chế biến (chủng loại, giấy tờ nguồn gốc, cảm quan chất lượng/an toàn của từng loại thực phẩm nhập vào)
  - **Bước 2**: Kiểm tra trong quá trình chế biến — điều kiện vệ sinh từ lúc sơ chế đến khi chế biến xong (người tham gia, dụng cụ, khu vực chế biến), đánh giá cảm quan món ăn nếu có biểu hiện khác lạ, ghi giờ bắt đầu/kết thúc chế biến
  - **Bước 3**: Kiểm tra trước khi ăn (đối chiếu thực đơn, vệ sinh dụng cụ chia/ăn uống, dụng cụ che đậy/bảo quản, cảm quan món ăn trước khi phục vụ)
- *(Sửa 2026-07-02: làm rõ lại nội dung Bước 2 theo đúng Điều 4 QĐ 1246 — bản trước mô tả "kiểm tra thức ăn sau nấu" dễ hiểu nhầm thành kiểm tra chất lượng món ăn đã nấu xong, trong khi luật tập trung vào điều kiện vệ sinh trong SUỐT quá trình chế biến.)*

### BR-083 — Ghi nhận bằng chứng mỗi bước
- Mỗi bước kiểm tra phải kèm theo: **ảnh bằng chứng, vị trí GPS, và timestamp** được ghi tự động.

### BR-090 — Nhiệt độ tối thiểu thức ăn sau nấu
- Thức ăn sau nấu phải đạt **nhiệt độ lõi tối thiểu 75°C** (kiểm tra tại Bước 2).
- *(Chú thích 2026-07-02: QĐ 1246/QĐ-BYT không quy định ngưỡng nhiệt độ cụ thể ở Bước 2 — điều khoản chỉ yêu cầu ghi giờ chế biến + đánh giá cảm quan. Ngưỡng 75°C là thông lệ an toàn thực phẩm phổ biến (HACCP), không phải trích dẫn trực tiếp từ văn bản trong `00_Policies/`.)*

### BR-091 — Nhiệt độ giữ ấm tối thiểu
- Thức ăn đã nấu chờ phục vụ phải được duy trì ở **nhiệt độ tối thiểu 60°C** (kiểm tra tại Bước 3).
- *(Chú thích 2026-07-02: tương tự BR-090 — không có trong QĐ 1246/QĐ-BYT, là thông lệ an toàn thực phẩm phổ biến, không phải trích dẫn luật.)*

### BR-092 — Kiểm tra hạn sử dụng nguyên liệu
- Hệ thống tự động **kiểm tra hạn sử dụng** của các lô nguyên liệu được sử dụng trong bước kiểm tra.

### BR-093 — Hồ sơ kiểm tra đầy đủ
- Mỗi phiên kiểm tra ba bước phải tạo ra một **hồ sơ kiểm tra hoàn chỉnh** liên kết với lô nguyên liệu và hồ sơ sản xuất bữa ăn.

### BR-094 — Chặn phục vụ nếu kiểm tra thất bại
- Bữa ăn **không được phép phục vụ** nếu bất kỳ bước nào trong ba bước kiểm tra thất bại.

### BR-095 — Ghi nhận không tuân thủ
- Khi một bước kiểm tra thất bại, hệ thống phải ghi nhận **chi tiết không tuân thủ** và tự động **tạo yêu cầu hành động khắc phục (Corrective Action Request)**.

### BR-096 — Tái kiểm tra sau hành động khắc phục
- Sau khi hoàn thành hành động khắc phục, bước kiểm tra thất bại phải được **thực hiện lại (Re-inspection)**.

### BR-097 — Tích hợp với quản lý sự cố
- Kết quả kiểm tra thất bại nghiêm trọng phải được liên kết tự động với hệ thống **quản lý sự cố**.

### BR-098 — Phê duyệt bữa ăn để phục vụ
- Chỉ sau khi **cả 3 bước** kiểm tra đều **đạt (Passed)**, hệ thống mới **phê duyệt bữa ăn** để phục vụ.

### BR-099 — Quyền xem hồ sơ kiểm tra
- **Medical Staff** và **School Administration** có quyền xem toàn bộ hồ sơ kiểm tra.
- **Service Caterers** chỉ xem được hồ sơ của phiên kiểm tra do mình thực hiện.

---

## 9. Lưu mẫu Thực phẩm

### BR-105 — Lưu mẫu bắt buộc sau kiểm tra thành công
- Sau khi lô thức ăn **vượt qua 3 bước kiểm tra**, Service Caterers **bắt buộc** phải thực hiện lưu mẫu thực phẩm cho **tất cả món ăn** của bữa ăn.
- *(Sửa 2026-07-02: theo Điều 1.2 QĐ 1246/QĐ-BYT, quy định này về nguyên tắc chỉ bắt buộc với bữa ăn từ **30 suất trở lên**; do quy mô bữa ăn trường học trong hệ thống luôn ≥30 suất nên áp dụng không điều kiện là phù hợp thực tế, nhưng cần ghi rõ căn cứ để tránh hiểu lầm là áp dụng bất kể quy mô.)*

### BR-106 — Nhãn mẫu bắt buộc
- Mỗi mẫu lưu phải được dán nhãn theo đúng Mẫu số 4, Phụ lục 2, QĐ 1246/QĐ-BYT, gồm: **bữa ăn (sáng/trưa/tối), tên mẫu thức ăn, thời gian lấy mẫu (giờ, phút, ngày, tháng, năm), và người lấy mẫu (họ tên, chữ ký)**.
- Hệ thống có thể lưu thêm các trường bổ sung ngoài yêu cầu tối thiểu của luật (ví dụ số tham chiếu lô nguyên liệu) để phục vụ truy xuất nguồn gốc nội bộ.
- *(Sửa 2026-07-02: nội dung cũ liệt kê "tên thức ăn, ngày sản xuất, ngày lưu mẫu, số tham chiếu lô" không khớp đúng Mẫu số 4 — thiếu "bữa ăn" và "người lấy mẫu", thừa "ngày sản xuất"/"số tham chiếu lô" so với mẫu nhãn chính thức. Đã khớp lại theo đúng luật; Screen Spec `21_RecordSampleInformationForm.md` hiện đã triển khai đúng 4 trường theo Mẫu số 4, không cần sửa.)*

### BR-107 — Chụp ảnh mẫu bắt buộc
- Nhân viên **bắt buộc phải chụp ảnh** các mẫu thực phẩm và nhãn mẫu làm bằng chứng vận hành.

### BR-203 — Khối lượng và dụng cụ lưu mẫu tối thiểu
- Khối lượng mẫu lưu tối thiểu: **100 gam** đối với thức ăn đặc (xào, hấp, rán, luộc, rau/quả ăn ngay) hoặc **150 ml** đối với thức ăn lỏng (súp, canh...).
- Dụng cụ lưu mẫu phải có **nắp đậy kín**, đã được **rửa sạch và tiệt trùng** trước khi sử dụng; mỗi món ăn được lấy và lưu vào dụng cụ **riêng biệt, có niêm phong**.
- Hệ thống chặn xác nhận đăng ký mẫu nếu khối lượng nhập vào thấp hơn ngưỡng tối thiểu tương ứng loại món (đặc/lỏng).
- *(Bổ sung 2026-07-02, theo Điều 6-7 QĐ 1246/QĐ-BYT: quy tắc này trước đây chưa có mã BR chính thức — một số Screen Spec (`21_RecordSampleInformationForm.md` Web/Mobile) đang gán nhầm field "Trọng lượng mẫu ≥100g" vào **BR-107** (thực chất là quy tắc chụp ảnh, không liên quan khối lượng). Đã cấp mã mới BR-203 và cần repoint các trích dẫn liên quan — xem `traceability_matrix.md` và ScreenSpec.)*

### BR-108 — Ghi tọa độ GPS và timestamp tự động khi lưu mẫu
- Hệ thống **tự động ghi nhận** GPS, thông tin thiết bị, và timestamp trong quá trình lưu mẫu.

### BR-109 — Xác minh vị trí lưu mẫu
- Hệ thống xác minh việc lưu mẫu được thực hiện trong **khu vực bếp hoặc kho được chỉ định**.

### BR-110 — Nhiệt độ bảo quản mẫu
- Tủ lạnh lưu mẫu phải duy trì nhiệt độ trong khoảng **2°C – 8°C**.
- ~~Hệ thống **phát cảnh báo** nếu nhiệt độ lệch ra ngoài phạm vi này trong hơn **30 phút**.~~
- **Sửa 11/07/2026**: bỏ cơ chế cảnh báo tự động ở trên — ngụ ý giám sát liên tục qua cảm biến/IoT mà hệ thống không quản lý (cùng lý do deprecate BR-089). **Giữ nguyên giới hạn nhiệt độ 2°C–8°C** làm tiêu chí kiểm tra thủ công (nhân viên đo tại 1 thời điểm cụ thể, ví dụ trong lúc kiểm thực 3 bước hoặc kiểm tra bếp UC-014) — không còn gắn với bất kỳ cơ chế cảnh báo/giám sát tự động nào. Chi tiết: `review/medical_staff/review_medicalstaff_compliance_kitcheninspection.md` Mục 2.

### BR-111 — Thời gian lưu mẫu tối thiểu
- Hệ thống **tự động theo dõi** thời hạn lưu mẫu và khởi động đồng hồ đếm ngược **24 giờ** kể từ khi đăng ký lưu mẫu.
- Mẫu **không được hủy** trước khi hết 24 giờ này.
- *(Sửa 2026-07-02: cụ thể hoá số giờ — trước đây chỉ ghi "theo quy định". **Sửa lại 26/07/2026 (nguồn: phỏng vấn thực tế Cán bộ Y tế, `review/medical_staff/review_medicalstaff_foodsamples.md` Mục 9.1)**: đổi từ 72 giờ (buffer an toàn, ghi chú 2026-07-02) về đúng **24 giờ** — Điều 8.2 QĐ 1246/QĐ-BYT quy định ngưỡng pháp lý tối thiểu 24 giờ, và Điều 8.1 quy định mẫu bảo quản ở **2°C–8°C** (ngăn mát/tủ lạnh, không phải tủ đông) — ở dải nhiệt độ này mẫu đã nấu chín xuống cấp nhanh, giữ quá 24h không còn "an toàn hơn" mà có nguy cơ mất giá trị xét nghiệm; đồng thời năng lực tủ lưu mẫu thực tế hạn chế, cần giải phóng sau 24h. Cần đồng bộ lại `21_RecordSampleInformationForm.md`, `22_FoodSampleDetail.md` (Web/Mobile) đang dùng 72 giờ — CHƯA áp.)*

### BR-112 — Hủy mẫu sau thời hạn
- Sau khi hết thời gian lưu mẫu bắt buộc (BR-111), nhân viên thực hiện hủy mẫu theo quy trình; hệ thống ghi nhận thông tin hủy mẫu, nhân viên chịu trách nhiệm, và timestamp.
- **Ngoại lệ bắt buộc**: hệ thống **chặn hủy mẫu** (dù đã hết 24 giờ) nếu có sự cố an toàn thực phẩm/nghi ngờ ngộ độc đang liên kết với lô mẫu (xem BR-113) hoặc có yêu cầu từ cơ quan quản lý, cho đến khi sự cố được đóng hoặc có thông báo khác.
- *(Sửa 2026-07-02: bổ sung ngoại lệ theo đúng câu cuối Điều 8.2 QĐ 1246/QĐ-BYT — trước đây BR-112 không nối rõ với cơ chế thu hồi mẫu khi có sự cố ở BR-113, có thể dẫn tới hủy nhầm mẫu đang cần điều tra.)*
- *(Ghi chú 15/07/2026 — Mục 11.6 review Incident, số giờ nay đã đổi thành 24h theo BR-111 sửa 26/07/2026)*: mốc lưu mẫu tối thiểu (BR-111) là ngưỡng **TỐI THIỂU** ("không được hủy trước 24h"), **không phải giới hạn tối đa**. Sau 24h mẫu **có thể** đã bị hủy (không bắt buộc hủy ngay). Hệ quả cho UI thu hồi mẫu trong điều tra sự cố: nút "Mở rộng phạm vi ngày" (`26_IncidentDetail.md`) giới hạn ở mốc 24h là ngưỡng còn khả năng thu hồi thật; mẫu ở trạng thái `Disposed` chỉ còn liên kết metadata qua "Link Disposed Sample to Incident" (UC-076 AF1), không còn "Authorize Retrieval".

### BR-113 — Chỉ định và tự thực hiện thu hồi mẫu khi có sự cố
- Khi xảy ra sự cố an toàn thực phẩm, **Medical Staff** chỉ định mẫu thực phẩm đang lưu cần thu hồi để điều tra (kèm phòng kiểm nghiệm đích và chỉ tiêu yêu cầu xét nghiệm) **và tự mình thực hiện** việc lấy mẫu vật lý tại tủ lưu, vận chuyển đến phòng kiểm nghiệm đích; hệ thống ghi nhận yêu cầu, mục đích điều tra, người chỉ định/thực hiện, và cho phép tải lên ảnh/PDF "Biên bản giao nhận mẫu" (phiếu nhận của phòng lab) làm bằng chứng bàn giao.
- **Sửa 19/07/2026 → đảo ngược lại 26/07/2026 (nguồn: phỏng vấn thực tế Cán bộ Y tế, `review/medical_staff/review_medicalstaff_foodsamples.md` Mục 9.2)**: bản sửa 19/07/2026 từng tách vai trò thực hiện sang Service Caterers (lý do thuận tiện vận hành — xem BR-215, nay Deprecated). Phản hồi thực tế từ cán bộ y tế chỉ ra rủi ro **tráo mẫu**: Nhà thầu là bên có thể bị quy trách nhiệm qua kết quả xét nghiệm, nếu để chính họ tự tay mang mẫu đi gửi lab (có sẵn tem, nguyên liệu tương tự) thì hoàn toàn có thể tráo mẫu trước khi nộp, làm mất tính khách quan chuỗi giám sát (chain of custody) — đúng lúc mẫu cần gửi lab nhất (khi đang điều tra sự cố) cũng là lúc Nhà thầu có động cơ tráo mẫu cao nhất. Khôi phục lại: Medical Staff tự thực hiện toàn bộ, không giao Service Caterers. CHƯA áp code (đảo ngược phần đã áp 19/07/2026) — xem checklist `review/medical_staff/review_medicalstaff_foodsamples.md` Mục 10.

### BR-114 — Liên kết mẫu với thực đơn và kiểm tra
- Hồ sơ lưu mẫu phải được **liên kết** với thông tin thực đơn, hồ sơ kiểm tra, và thông tin lô sản xuất bữa ăn.
- *(Sửa 12/07/2026)*: field "Tên người ủy quyền" (`authorizedBy`) ở `14_RetrieveFoodSampleRecordsDialog.md` (UC-076) đổi từ nhập tay tự do sang **tự động = tài khoản Medical Staff đang đăng nhập, readonly** — nhất quán với cách `reportedBy` (BR-183) và "Đại diện Y tế trường học" (BR-211) đã làm, đảm bảo audit trail chính xác 100%, không cho phép ghi nhầm/ghi thay tên người khác.
- *(Ghi chú 19/07/2026, đảo ngược lại 26/07/2026)*: `authorizedBy` nay mang nghĩa cả "người chỉ định" lẫn "người thực hiện thu hồi" — cùng 1 người (Medical Staff), do BR-215 đã Deprecated (xem BR-113 sửa 26/07/2026).

### BR-212 — Bằng chứng bàn giao mẫu cho phòng kiểm nghiệm (UC-076)
- Khi Medical Staff thực hiện thu hồi & gửi xét nghiệm mẫu (BR-113), hệ thống yêu cầu: **Chỉ tiêu yêu cầu kiểm nghiệm** (nhập khi chỉ định — bắt buộc) và **Ảnh/scan phiếu nhận mẫu do phòng kiểm nghiệm cấp** (tải lên khi xác nhận đã gửi — bắt buộc), có thể kèm **Đại diện phòng lab nhận mẫu** (tên người nhận tại phòng kiểm nghiệm — tùy chọn nếu phiếu nhận không ghi rõ).
- Ảnh/scan phiếu nhận của chính phòng kiểm nghiệm đóng vai trò bằng chứng bàn giao đã diễn ra "ngay, trong thời gian sớm nhất" theo Điều 5 khoản 2, Thông tư 14/2011/TT-BYT — hệ thống **không tự sinh** một "Biên bản bàn giao mẫu" song phương nào khác.
- **Không** áp dụng cơ chế ký 2 bên + gate chặn cứng như BR-211: phòng kiểm nghiệm là đơn vị bên ngoài, không có tài khoản/quan hệ trực tiếp trong SFMS.
- *(Sửa 19/07/2026 → đảo ngược lại 26/07/2026)*: actor thực hiện quay lại là **Medical Staff** (không phải Service Caterers) — theo BR-113 sửa 26/07/2026. Trích dẫn pháp lý (Điều 5 khoản 2, Thông tư 14/2011/TT-BYT) và yêu cầu bằng chứng (phiếu nhận phòng lab) không đổi, chỉ đổi actor thực hiện. Chi tiết: `review/medical_staff/review_medicalstaff_foodsamples.md` Mục 9.2.

### BR-215 — Deprecated 26/07/2026 (vai trò Service Caterers thực hiện thu hồi mẫu bị bãi bỏ)
- ~~Service Caterers thực hiện lấy mẫu, bàn giao phòng kiểm nghiệm, xác nhận gửi xét nghiệm~~ — theo phản hồi thực tế Cán bộ Y tế (rủi ro tráo mẫu/chain of custody), vai trò thực hiện được chuyển lại hoàn toàn cho **Medical Staff** (xem BR-113 sửa 26/07/2026). Giữ số hiệu để tránh xáo trộn liên kết chéo trong tài liệu cũ; không còn hiệu lực độc lập. Không tự đảo lại lần 3 nếu chưa có căn cứ thực tế mới — xem `review/medical_staff/review_medicalstaff_foodsamples.md` Mục 9.2.

---

## 10. Phục vụ & Phân phát Bữa ăn

### BR-120 — Xác nhận phân phát bữa ăn theo lớp
- **Giáo viên (Teacher)** xác nhận việc phân phát bữa ăn cho học sinh trong lớp của mình.

### BR-121 — Theo dõi lịch sử bữa ăn học sinh
- Hệ thống ghi nhận và theo dõi **lịch sử tiêu thụ bữa ăn** của từng học sinh.

### BR-123 — Cảnh báo dị ứng khi quét ID học sinh
- Khi quét ID học sinh có **dị ứng thực phẩm đã đăng ký** trong phiên phục vụ, hệ thống **tự động kích hoạt cảnh báo trực quan và âm thanh** trên màn hình phục vụ để ngăn phân phát sai suất ăn.

### BR-125 — Xem yêu cầu bữa ăn đặc biệt khi phục vụ
- Giáo viên và nhân viên phục vụ phải có thể xem **yêu cầu bữa ăn đặc biệt** của từng học sinh trong lớp trước và trong giờ phục vụ.

### BR-130 — Ngăn phân phát trùng lặp
- Hệ thống **chặn phân phát bữa ăn trùng lặp** — một học sinh không thể được quét hai lần trong cùng một phiên phục vụ bữa ăn.

### BR-133 — Tóm tắt bữa ăn hàng ngày cho phụ huynh
- Hệ thống **tự động biên soạn** thực đơn đã phục vụ và cấu trúc dinh dưỡng, gửi **thông báo tóm tắt hàng ngày** đến phụ huynh.

### BR-135 — Thông báo trễ phục vụ bữa ăn
- Hệ thống **tự động gửi thông báo trễ** đến giáo viên chủ nhiệm và phụ huynh khi việc phục vụ bữa ăn bị trễ hơn **20 phút** so với thời gian đã lên kế hoạch.

---

## 11. Quản lý Sự cố Thực phẩm

### BR-146 — Ngưỡng phát hiện cụm triệu chứng (tự động gợi ý, thủ công quyết định)
- ~~Hệ thống tự động khởi tạo điều tra sự cố an toàn thực phẩm khi từ 3 học sinh trở lên báo cáo các triệu chứng tương đồng trong vòng 24 giờ.~~
- Hệ thống **tự động phát hiện và hiển thị thông báo gợi ý** cho Medical Staff khi có **từ 3 học sinh trở lên** báo triệu chứng tương đồng trong vòng **24 giờ**, để Medical Staff biết và tự rà soát các báo cáo thô (`RPT-xxx`) liên quan.
- Hệ thống **không tự động gộp báo cáo, không tự động khởi tạo** Incident dựa trên ngưỡng này — quyết định gộp nhiều `RPT-xxx` thành 1 `INC-xxx`, hoặc mở Incident mới, **hoàn toàn thủ công** do Medical Staff thực hiện, khớp đúng hành vi đã triển khai tại `medical-staff/incidents/page.tsx` (tab "Chờ tiếp nhận").
- *(Sửa 20/07/2026, đính chính lại lần sửa cùng ngày: giữ nguyên cơ chế **phát hiện + thông báo tự động** (vẫn cần tổng hợp dữ liệu real-time phía server, đúng lý do đã nêu ở BR-198 — do đó **BR-198 giữ nguyên, không cần sửa**), chỉ bỏ phần **tự động khởi tạo/tự động gộp** Incident. Đây là quyết định cuối của người dùng khi rà soát module Incident Management: ưu tiên Medical Staff toàn quyền quyết định mở/gộp sự cố, hệ thống chỉ đóng vai trò cảnh báo, không tự hành động thay. Không tìm thấy căn cứ pháp luật cụ thể cho số liệu "3 học sinh/24 giờ" trong `00_Policies/` — đây là quy ước nội bộ dự án, không phải yêu cầu luật định. Chi tiết thảo luận: xem `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 15.)*

### BR-147 — Phân loại mức độ nghiêm trọng sự cố
- **Medical Staff** chịu trách nhiệm **phân loại mức độ nghiêm trọng** (Severity) của sự cố.

### BR-148 — Cảnh báo ngay lập tức cho sự cố nghiêm trọng
- Sự cố được phân loại **mức độ cao (High Severity)** phải kích hoạt **ngay lập tức** cảnh báo đến:
  - Ban giám hiệu nhà trường
  - Công ty catering
  - Cơ quan y tế địa phương

### BR-149 — Cách ly lô nguyên liệu theo lựa chọn cụ thể của Medical Staff
- ~~Khi sự cố an toàn thực phẩm được báo cáo, hệ thống tự động đặt trạng thái Quarantine (Cách ly) cho tất cả lô thức ăn nghi vấn và nguyên liệu liên quan trong kho.~~
- Medical Staff khảo sát thực địa, sau đó có thể **(tùy chọn, không bắt buộc)** chọn món ăn nghi vấn. Khi chọn 1 món, hệ thống hiển thị danh sách lô nguyên liệu liên quan tới món đó (mã lô, tên nguyên liệu, khối lượng còn lại, ngày nhập lô), lấy từ dữ liệu lô hàng của Service Caterers.
- Medical Staff **tự chọn từng lô nguyên liệu cụ thể** cần cách ly trong danh sách đó — hệ thống đặt trạng thái Quarantine **ngay khi chọn**, kèm **lý do cách ly bắt buộc** nhập ngay tại dòng lô đó (sửa 27/07/2026, xem ghi chú dưới), và thông báo ngay cho Service Caterers. **Không** tự động cách ly toàn bộ lô liên quan tới món chỉ vì món được chọn.
- *(Sửa 20/07/2026: đổi từ "tự động cách ly TẤT CẢ lô liên quan" sang cách ly theo lựa chọn cụ thể của Medical Staff ở cấp độ từng lô. Lý do: cách ly tự động toàn bộ lô gắn với 1 món (VD toàn bộ 50kg thịt + 30kg trứng chỉ vì nghi 1 món dùng 2 nguyên liệu này) gây thiệt hại lớn không tương xứng, trong khi thực tế Medical Staff thường chỉ cần cách ly 1 phần nguyên liệu (thịt, rau củ) sau khảo sát trực tiếp, không cách ly toàn bộ (mắm, mì chính hiếm khi là nguyên nhân). Việc chọn món nghi vấn cũng đổi thành **tùy chọn** — không có căn cứ bắt buộc phải xác định được món mới được tiếp tục điều tra. Chi tiết đầy đủ luồng xem BR-217 (mới) và `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 17.)*
- *(Sửa 27/07/2026: bỏ "không cần xác nhận thêm".* Quyết định 20/07 để khoá lô **không cần xác nhận** dựa trên tiền đề còn đường lùi: nhả lô (BR-217 bước 5-6 gốc) có dialog + lý do bắt buộc riêng, coi là bước "thật sự cần cẩn trọng" — bất đối xứng có chủ đích. Nay BR-217 đã bỏ hẳn cơ chế nhả lô (Caterers trả lô về NCC ngay khi cách ly, không chờ kết quả lab — xem BR-217 sửa 27/07/2026), khoá lô trở thành **hành động không thể hoàn tác**, tức đã thế chỗ đúng vị trí rủi ro mà nhả lô từng giữ. Chuyển yêu cầu "lý do bắt buộc" sang thẳng hành động khoá — không thêm dialog xác nhận riêng, ô lý do bắt buộc tại chỗ tick đã đủ chặn tick nhầm. Chi tiết xem `review_medicalstaff_incidentmanagement.md` Mục 17.4.)*
- ~~*(Sửa 20/07/2026, gộp và đơn giản hoá ghi chú 15/07/2026)*: danh sách "món ăn nghi vấn" dùng **1 mốc cố định duy nhất 72 giờ** trước thời điểm báo cáo sự cố — bỏ cơ chế 2 tầng "mặc định 48h + nút Mở rộng phạm vi ngày" trước đây. Mốc 72h vẫn nằm trong tinh thần tham chiếu của **Quyết định 39/2006/QĐ-BYT**...~~
- *(Sửa 27/07/2026 — đổi cửa sổ 72 giờ → **24 giờ**, áp thống nhất cho CẢ danh sách "món ăn nghi vấn" lẫn danh sách mẫu gửi xét nghiệm)*: cơ chế 1 mốc cố định duy nhất **giữ nguyên** (vẫn không có 2 tầng "48h + nút mở rộng"), chỉ đổi độ dài cửa sổ. Căn cứ: **Quyết định 39/2006/QĐ-BYT** quy định điều tra ăn uống **24-48 giờ** trước phát bệnh (`00_Policies/39_2006_QD-BYT_quy_che_dieu_tra_ngo_doc_thuc_pham.md`); mốc 72h trước đây là phần **nới thêm** cho tác nhân ủ bệnh lâu (Campylobacter, E. coli STEC, trung vị 62-87h), không phải con số của văn bản pháp lý. Quyết định 27/07/2026 chọn bám sát cận dưới của văn bản (24h) và **đồng bộ với BR-111** (hạn lưu mẫu tối thiểu cũng 24h, sửa 26/07/2026) — tránh tình trạng danh sách món trải rộng hơn hẳn khoảng thời gian còn tồn tại mẫu để đối chứng.
- *(Ghi chú thực thi, 27/07/2026)*: **mốc gốc để tính cửa sổ** đổi theo thứ tự ưu tiên `occurredAt` (thời điểm xảy ra sự cố) → suy từ `mealDate` (bữa ăn đã xác nhận) → `reportedAt` (thời điểm lập biên bản, chỉ dùng khi thiếu cả hai). Trước đây chỉ dùng `reportedAt` — sai nguồn, vì đó là lúc lập biên bản chứ không phải lúc xảy ra; với cửa sổ hẹp 24h thì sai mốc gây rỗng danh sách nhanh hơn hẳn. Chi tiết xem `review/medical_staff/review_medicalstaff_foodsamples.md` Mục 13.4.

### BR-217 — Quy trình cách ly, thu hồi mẫu xét nghiệm, và trả lô nguyên liệu về Nhà cung cấp (sửa 27/07/2026 — bỏ cơ chế "mở khoá")
- Quy trình đầy đủ, tiếp nối BR-149:
  1. Medical Staff khảo sát thực địa, đánh giá mức độ nghiêm trọng (BR-147), tùy chọn chọn món ăn nghi vấn và tự chọn lô nguyên liệu cần cách ly (BR-149) — hệ thống cách ly ngay, thông báo Service Caterers.
  1b. **(Mới 27/07/2026)** Ngay sau khi cách ly, Service Caterers **trả lô nguyên liệu về Nhà cung cấp** — không giữ lại kho chờ kết quả xét nghiệm. Lý do: nguyên liệu tươi sống hỏng nhanh, chờ kết quả lab (thường ~7 ngày) khiến lô không còn giá trị sử dụng dù kết quả là gì — giữ lại chờ "mở khoá" là vô nghĩa về mặt vận hành.
  2. Medical Staff chỉ định thu hồi mẫu thực phẩm đang lưu để xét nghiệm (BR-113): mẫu xét nghiệm tự động lấy theo món ăn nghi vấn đã chọn; các field: tên cơ quan y tế tiếp nhận mẫu (bắt buộc), chỉ tiêu xét nghiệm (bắt buộc), ghi chú cho Service Caterers (tùy chọn).
  3. Service Caterers thực hiện lấy mẫu, bàn giao cho cơ quan y tế, tải ảnh/scan hoặc biên bản bàn giao làm bằng chứng (BR-212, BR-215) — **không** bao gồm việc tải kết quả xét nghiệm.
  4. Khi có kết quả, **Medical Staff** (không phải Service Caterers) tải nhiều ảnh/file PDF kết quả lên hệ thống, kèm 1 field kết luận kết quả. Lý do: phòng xét nghiệm phản hồi trực tiếp cho bên đứng tên yêu cầu (Medical Staff, đã ghi ở bước 2); Service Caterers chỉ có vai trò hậu cần vận chuyển mẫu (bước 3).
  5. ~~Tại đúng màn hình nhập kết luận kết quả, hệ thống hiển thị danh sách món ăn đang bị khoá kèm danh sách lô nguyên liệu đã khoá của từng món, để thao tác mở khoá ngay tại chỗ.~~ **Bỏ (27/07/2026)** — không còn "lô đang khoá vật lý" nào để mở, vì đã trả về NCC ở bước 1b từ trước khi có kết quả.
  6. ~~Nếu kết luận xác định (các) lô đang khoá không phải nguyên nhân, Medical Staff tick mở khoá...~~ **Bỏ (27/07/2026)**, thay bằng: kết quả xét nghiệm (bước 4) đóng vai trò thuần túy **căn cứ pháp lý** — lưu kèm hồ sơ sự cố, dùng làm cơ sở khiếu nại/thanh lý hợp đồng/phạt vi phạm với Nhà cung cấp (nếu kết quả xác định lô là nguyên nhân), không kích hoạt hành động hệ thống nào (không mở khoá, không cập nhật trạng thái tồn kho).
- *(Sửa 27/07/2026: giải quyết câu hỏi để mở tại `review_medicalstaff_foodsamples.md` Mục 9.3 (26/07/2026) — phỏng vấn cán bộ y tế thực tế chỉ ra Service Caterers trả lô về NCC ngay, không chờ kết quả lab ~7 ngày; xác nhận trực tiếp qua trao đổi 27/07/2026. Chi tiết đầy đủ, các phương án đã cân nhắc trước đó: xem `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 17 và `review_medicalstaff_foodsamples.md` Mục 13.)*
- *(Mới 20/07/2026, hợp thức hoá luồng đã thống nhất qua thảo luận thiết kế — xuất phát từ vấn đề thật: cách ly tự động toàn bộ lô theo món (BR-149 bản cũ) gây thiệt hại lớn không tương xứng. Bối cảnh gốc giữ nguyên giá trị, chỉ bước 5-6 bị thay như trên.)*

### BR-150 — Điều tra và phân tích nguyên nhân gốc rễ
- Hệ thống phải hỗ trợ **điều tra nguyên nhân gốc rễ (Root Cause Analysis)** với khả năng đính kèm bằng chứng.

### BR-151 — Biên soạn bản đồ truy xuất nguồn gốc
- Trong vòng **4 giờ** kể từ khi báo cáo sự cố, hệ thống **tự động biên soạn bản đồ truy xuất** cho tất cả lô nguyên liệu và nhà cung cấp liên quan.
- *(Footnote 15/07/2026 — cần đội dự án xác nhận)*: phạm vi "liên quan" chưa được định nghĩa rõ (toàn sự cố hay chỉ lô của món nghi vấn). Theo logic kiểm soát chi phí đã chốt ở BR-149 (khoá đúng lô của `suspectedDishIds[]`, không tràn lan), **đề xuất scope bản đồ truy xuất theo đúng lô đã khoá (`quarantineBatchIds[]`)** và hiển thị lồng nhau (mỗi lô bị khoá mở ra chuỗi truy xuất của nó) — xem Mục 11.7 `08_FrontEnd/Web/review/medical_staff/review_medicalstaff_incidentmanagement.md`. **Chưa khoá** vì có thể ảnh hưởng cách tự động hoá truy xuất hiện tại — chờ đội dự án xác nhận.

### BR-152 — Quản lý CAPA (Corrective and Preventive Action)
- Sau mỗi sự cố, phải có **Kế hoạch Hành động Khắc phục và Phòng ngừa (CAPA)** được tạo ra, phân công, và theo dõi đến khi hoàn thành.
- Biện pháp khẩn cấp (khóa lô, ngừng sử dụng món/lô, sơ cứu, cảnh báo HIGH — BR-148/149) **tách biệt hoàn toàn** khỏi CAPA: Medical Staff kích hoạt ngay theo quyền chuyên môn, không qua CAPA, không qua School Administration duyệt. CAPA chỉ bao gồm các hành động khắc phục/phòng ngừa dài hạn (điều tra nguyên nhân, thay quy trình, thay nhà cung cấp, đào tạo lại, vệ sinh định kỳ, đánh giá nhân sự...).
- *(Sửa 20/07/2026: bỏ mọi tham chiếu tới mốc thời gian "12 giờ"/"6 giờ" từng gắn với BR-152 trong văn bản Use Case ở `Report 3.md` — đã kiểm tra, không có căn cứ pháp lý nào trong `00_Policies/` cho các con số này, và bản thân BR-152 gốc chưa từng ghi số giờ nào. Quyết định: **không đặt hạn SLA cứng** cho việc thực hiện CAPA — hệ thống chỉ lưu timestamp thực tế (thời điểm CAPA được gửi cho Service Caterers, thời điểm Service Caterers nộp bằng chứng), hiển thị cho các bên tự đọc và tự quyết định khi nào cần thúc giục, không có ngưỡng/cờ cảnh báo "trễ hạn" nào. Xem chi tiết mô hình đầy đủ ở BR-216. `Report 3.md` UC-082 (đoạn "Late Submission"/`MSG-023` dựa trên hạn 12h) cần viết lại theo quyết định này — chưa làm, để dành đợt áp dụng kế tiếp.)*

### BR-153 — Phê duyệt hành động khắc phục
- **School Administration** có quyền **phê duyệt hoặc từ chối** các hành động khắc phục **được đề xuất**, **trước khi** Service Caterers thực hiện.
- *(Sửa 20/07/2026: làm rõ tường minh thứ tự duyệt — trước đây `Report 3.md` UC-005 mô tả School Administration duyệt **kết quả sau khi Service Caterers đã thực hiện xong**, mâu thuẫn với Workflow 11 (School Administration duyệt **kế hoạch trước** khi thực hiện, E17 trước E18). Đã chốt theo hướng duyệt kế hoạch trước, vì biện pháp khẩn cấp (phần thực sự gấp) đã tách riêng khỏi CAPA (xem BR-152) — CAPA không còn cần bỏ qua bước duyệt trước để đảm bảo tốc độ. `Report 3.md` UC-005 cần viết lại cho khớp — chưa làm, để dành đợt áp dụng kế tiếp. Chi tiết đầy đủ xem BR-216 và `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 16.)*

### BR-154 — Báo cáo sự cố chính thức
- **Medical Staff** có thể tạo **báo cáo sự cố chính thức** để lưu hồ sơ và xuất báo cáo.

### BR-155 — Đóng sự cố
- Chỉ **School Administration** mới có quyền **đóng (Close)** một sự cố sau khi xác nhận hoàn thành các hành động khắc phục.

### BR-216 — Vòng đời trạng thái CAPA và quy trình xử lý (Mới 20/07/2026)
- Trạng thái CAPA đi theo đúng 1 bộ enum thống nhất dùng chung cho cả 3 vai trò: `DRAFT → PENDING_ADMIN_APPROVAL → APPROVED_FOR_IMPLEMENTATION → IN_PROGRESS → SUBMITTED_EVIDENCE → COMPLETED`.
- Quy trình:
  1. Khi có sự cố, Medical Staff trao đổi trực tiếp với Service Caterers về phương án khắc phục (ngoài hệ thống, không bắt buộc ghi vết), sau đó soạn CAPA trên hệ thống (`DRAFT`).
  2. Gửi School Administration duyệt (`PENDING_ADMIN_APPROVAL`). Không duyệt → về `DRAFT`, **bắt buộc lý do**, Medical Staff sửa và gửi lại. Duyệt → `APPROVED_FOR_IMPLEMENTATION`, gửi cho Service Caterers xem.
  3. Service Caterers **chỉ xem và xác nhận (accept)** — không có nhánh từ chối trên hệ thống, vì phương án đã được bàn bạc trực tiếp từ bước 1. Accept → `IN_PROGRESS`, hệ thống ghi nhận timestamp thời điểm này (thời điểm CAPA được gửi/accept).
  4. Service Caterers thực hiện, nộp ảnh chụp trực tiếp (live) + ghi chú các việc đã làm → `SUBMITTED_EVIDENCE`, hệ thống ghi nhận timestamp nộp. **Không có hạn SLA, không có cờ cảnh báo trễ** — chỉ hiển thị 2 timestamp (gửi CAPA / nộp bằng chứng) để các bên tự đọc.
  5. Medical Staff đánh giá hiệu quả chuyên môn. Chưa đạt → gửi lại **đúng phương án đã được duyệt** (không cần trình School Administration duyệt lại vì phương án không đổi), yêu cầu làm lại kèm **lý do bắt buộc** → về `IN_PROGRESS`. Đạt → `COMPLETED` — đây là trạng thái cuối của CAPA.
  6. Medical Staff tổng hợp toàn bộ hồ sơ sự cố (bao gồm các CAPA đã `COMPLETED`) thành báo cáo (BR-154), gửi School Administration. School Administration rà soát toàn bộ hồ sơ và quyết định đóng Incident (BR-155) — đây là quyết định thuộc state machine của **Incident**, không phải của CAPA.
- 1 Incident = 1 CAPA (có thể gộp nhiều hành động khắc phục khác nhau trong cùng 1 bản ghi/1 log bằng chứng) — không tách nhiều CAPA song song trong cùng 1 Incident.
- Mỗi lần sửa CAPA sau khi đã gửi duyệt lưu 1 bản chụp revision nhẹ (nội dung, người sửa, lý do, thời điểm) — theo cơ chế audit log chung (BR-170), không phải hệ thống versioning riêng.
- *(Chi tiết thảo luận, các phương án đã cân nhắc và bị loại (CAPA đa cấp song song, kênh phản hồi giữa lúc thực hiện, field "đã trao đổi" bắt buộc, hạn SLA theo giờ...) xem `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 16 và `review/medical_staff/review_medicalstaff_to_servicecaterers.md`.)*

---

## 12. Giám sát An toàn Thực phẩm

### BR-089 — ~~Giám sát nhiệt độ tủ lạnh~~ `(Deprecated 11/07/2026)`
- ~~Hệ thống giám sát nhiệt độ tủ lạnh liên tục và phát cảnh báo khi nhiệt độ **lệch ra ngoài phạm vi cho phép** trong hơn 30 phút.~~
- **Deprecated 11/07/2026**: rule ngụ ý giám sát liên tục qua cảm biến/IoT — hệ thống không quản lý IoT (quyết định đội dự án). Ngoài ra rule chưa từng có con số "phạm vi cho phép" cụ thể trong toàn dự án (kiểm tra `MedicalStaff_KitchenInspectionChecklist_Proposal.md`, `ServiceCaterers/28_InventoryManagementDashboard.md` — cả 2 chỉ trích dẫn mã BR, không có field/số liệu nhiệt độ thật), nên không có gì để giữ lại làm giới hạn tĩnh (khác BR-110, xem ngay dưới, có số liệu rõ ràng nên được giữ). Giữ nguyên số hiệu BR-089 (không tái sử dụng cho quy tắc khác). Chi tiết: `review/medical_staff/review_medicalstaff_compliance_kitcheninspection.md` Mục 2.

### BR-100 — Điểm số vệ sinh hàng ngày
- Hệ thống phải ghi nhận và theo dõi **điểm số vệ sinh hàng ngày** của bếp ăn.

### BR-101 — Kiểm tra an toàn và vệ sinh hàng tháng
- Hệ thống phải lưu trữ kết quả **kiểm tra an toàn và vệ sinh định kỳ hàng tháng**.

### BR-102 — Kiểm soát côn trùng (sửa 27/07/2026 — bỏ mô hình nhật ký độc lập)
- ~~Hệ thống phải lưu trữ nhật ký kiểm soát côn trùng (Pest Control Log) cho bếp ăn~~ (mô hình nhật ký sự kiện độc lập, chốt 09/07/2026 — xem `review_medicalstaff_compliance_kitcheninspection.md` Mục 1).
- **Nội dung mới**: kiểm soát côn trùng **không còn là một hồ sơ/màn hình độc lập**. Nếu phát sinh ghi nhận (phát hiện côn trùng, biện pháp xử lý…), Medical Staff **có thể** thêm nó như **một mục tùy chọn** trong phiên **Kiểm tra an toàn bếp hàng ngày** (UC-014) đang thực hiện — không bắt buộc mọi phiên phải có, không có màn nhập liệu/nhật ký riêng.
- Nếu phiên kiểm tra không ghi nhận gì về côn trùng, hệ thống không hiển thị gì thêm cho mục này (không suy diễn, không để trống chờ dữ liệu).
- *(Lý do đổi 27/07/2026: BR-102 không có căn cứ pháp lý cụ thể trong QĐ 1246/QĐ-BYT (đã tra cứu — Mục 1); không có nguồn nhập liệu thật nào cho mô hình nhật ký độc lập trong toàn dự án (Mục 11.1 — grep `useCreate` vào `pest-control-log` cho 0 kết quả ở mọi actor). Duy trì 1 màn/tab/card riêng cho dữ liệu không ai nhập là gánh nặng UI không cần thiết — gộp thẳng vào checklist kiểm tra bếp hàng ngày đã có sẵn cơ chế nhập liệu và lưu vết. Xem `review_medicalstaff_compliance_kitcheninspection.md` Mục 15.)*

### BR-103 — Xác nhận sức khỏe nhân viên bếp
- Hệ thống phải theo dõi tính hợp lệ của **giấy xác nhận sức khỏe nhân viên bếp** theo quy định.
- *(Chú thích 2026-07-02: yêu cầu này bắt nguồn từ Điều 6.1.c TTLT 13/2016/TTLT-BYT-BGDĐT, dẫn chiếu tới Thông tư 15/2012/TT-BYT để quy định chi tiết. Thông tư 15/2012/TT-BYT hiện KHÔNG có trong `00_Policies/` — chưa thể xác nhận chu kỳ khám sức khỏe định kỳ cụ thể (ví dụ hàng năm) cho tới khi bổ sung văn bản này.)*

### BR-104 — Điểm số vệ sinh đầu bếp
- Điểm số vệ sinh của nhân viên bếp được tổng hợp và sử dụng trong **Báo cáo Tuân thủ An toàn Thực phẩm**.

### BR-207 — Bằng chứng theo từng hạng mục checklist + chú thích căn cứ pháp lý (UC-014)
- Mỗi hạng mục của Kitchen Safety and Sanitation Checklist (UC-014) được đánh giá **Đạt / Không đạt** độc lập theo từng mục, không dùng cơ chế "1 ảnh bằng chứng chung cho cả phiên" như trước.
- **Đạt** → ảnh minh chứng là **tùy chọn**. **Không đạt** → **bắt buộc** ít nhất 1 ảnh gắn đúng hạng mục + **bắt buộc** nhập lý do (comment).
- Mỗi hạng mục có thể có 1 chú thích căn cứ pháp lý dạng **văn bản đơn giản** (không gắn versioning/effective-date), hiển thị dạng thu gọn (icon "ℹ" trên Web, BottomSheet trên Mobile).
- ~~do **System Admin** cấu hình và chỉnh sửa; áp dụng chung cho mọi trường sử dụng hệ thống~~ — **[SỬA 06/07/2026]** quyền chỉnh sửa nội dung chú thích **không còn thuộc System Admin**; xem BR-210 (Medical Staff tự quản lý checklist, bao gồm cả chú thích).
- Thay đổi nội dung chú thích được ghi nhận qua cơ chế audit log chung (BR-170), không cần hệ thống versioning riêng.
- *(Bổ sung 05/07/2026: chính thức hóa từ đề xuất US-06/US-07/US-07b trong `02_Requirements/MedicalStaff_UserStory.md` — đội dự án đã tạm chốt hướng thiết kế này ngày 05/07/2026. Áp dụng cho UC-014 Normal Flow bước 3, 5, 7 và Screen Spec `12_ConductFoodSafetyInspectionScreen.md` (Web & Mobile). Nội dung hạng mục checklist cụ thể xem `MedicalStaff_KitchenInspectionChecklist_Proposal.md`.)*
- *(Sửa đổi 06/07/2026: người dùng quyết định chuyển checklist sang mô hình "dynamic" — Medical Staff tự chỉnh sửa toàn bộ checklist (nhóm, hạng mục, chú thích) trực tiếp, không qua System Admin, không cần phê duyệt. Điều khoản "System Admin cấu hình" ở trên **hết hiệu lực**, thay bằng BR-210 mới. Cơ chế Đạt/Không đạt + bằng chứng theo hạng mục (2 gạch đầu dòng đầu của BR-207) **không đổi**. Xem `MedicalStaff_KitchenInspectionChecklist_Proposal.md` Mục 0 và UC-186.)*

### BR-210 — Quản lý động (CRUD) Kitchen Safety and Sanitation Checklist (UC-186)
- Medical Staff có toàn quyền **thêm, sửa, xóa, sắp xếp lại** cả nhóm (group) và từng hạng mục (item) của Kitchen Safety and Sanitation Checklist (UC-014), bao gồm: tên nhóm, tên hạng mục, và nội dung chú thích căn cứ pháp lý (icon "ℹ").
- **Không cần phê duyệt** từ School Administration hay System Admin cho bất kỳ thao tác nào ở trên (khác với mô hình BR-207 bản gốc, vốn giao quyền cấu hình chú thích cho System Admin).
- Phạm vi áp dụng: **theo từng trường** (không còn là cấu hình dùng chung toàn hệ thống như BR-207 bản gốc mô tả) — mỗi trường có checklist riêng, Medical Staff của trường đó tự quản lý, không ảnh hưởng tới checklist của trường khác.
- Xóa một nhóm/hạng mục **đang được tham chiếu bởi ít nhất 1 phiên kiểm tra đã lưu** (lịch sử): hệ thống **không xóa dữ liệu lịch sử đã ghi nhận** — chỉ ẩn hạng mục khỏi các phiên kiểm tra mới, dữ liệu Đạt/Không đạt/ảnh/lý do của các phiên cũ vẫn giữ nguyên để tra cứu.
- Mọi thao tác thêm/sửa/xóa/sắp xếp lại được ghi nhận qua cơ chế audit log chung (BR-170), thay cho cơ chế versioning riêng.
- Dữ liệu khởi tạo mặc định (default seed data) khi triển khai lần đầu: xem `MedicalStaff_KitchenInspectionChecklist_Proposal.md` (6 nhóm, dựa trên checklist do người dùng cung cấp 06/07/2026, có chú thích căn cứ pháp lý đối chiếu `00_Policies/`).
- *(Bổ sung 06/07/2026: chính thức hóa quyết định của người dùng — chuyển mô hình quản lý checklist từ "System Admin cấu hình, áp dụng chung" (BR-207 bản gốc) sang "Medical Staff tự quản lý CRUD theo từng trường". Đây là thay đổi kiến trúc so với BR-207 gốc, không phải sửa lỗi trích dẫn — lý do: checklist cần phản ánh đúng luật/quy định mới nhất của Bộ Y tế theo thời gian thực, và có thể khác nhau giữa các trường tùy đặc thù cơ sở vật chất, nên giao quyền trực tiếp cho Medical Staff (người trực tiếp vận hành, hiểu rõ nhất tình hình thực tế của bếp ăn tại trường mình) thay vì qua một cấu hình tập trung. Cấp UC-186 mới cho hành vi này — xem `traceability_matrix.md`.)*

### BR-208 — Phân loại kiểu kiểm tra bếp: Định kỳ / Đột xuất `(Deprecated 09/07/2026)`
- ~~Mỗi phiên Kitchen Safety and Sanitation Checklist (UC-014) phải được gắn đúng 1 trong 2 loại: **Định kỳ** (theo lịch cố định, ví dụ hàng tháng) hoặc **Đột xuất** (kiểm tra ngẫu nhiên, không báo trước).~~
- ~~Field này phục vụ mục đích thống kê/báo cáo tần suất kiểm tra; **không** làm thay đổi cơ chế tính điểm hay ngưỡng cảnh báo (BR-104 và ghi chú về ngưỡng 60% bên dưới — đã descope khỏi cơ chế cách ly bếp 05/07/2026, xem ghi chú sau BR-208).~~
- *(Bổ sung 05/07/2026: hợp thức hóa field `type` đã tồn tại sẵn trong triển khai frontend (`conduct-inspection/page.tsx`, `KitchenInspection.type`) nhưng trước đó chưa có căn cứ BR/UC — phát hiện khi audit frontend, xem `03_Design/08_FrontEnd/Web/review/medical_staff/review_screen_medical_staff.md` mục 3.5. Đội dự án xác nhận và tạm chốt 05/07/2026. Chưa xác nhận được căn cứ pháp luật cụ thể cho việc phân loại này trong `00_Policies/` — đây là quyết định vận hành nội bộ của dự án/nhà trường, không phải yêu cầu pháp lý bắt buộc.)*
- **Deprecated 09/07/2026**: đội dự án quyết định bỏ hẳn field/bước chọn "Loại kiểm tra" — không còn phân biệt Định kỳ/Đột xuất ở bất kỳ tầng nào của hệ thống (kể cả thống kê ngầm), vì chỉ phục vụ thống kê và không có căn cứ pháp luật bắt buộc. Quyết định + phạm vi áp dụng chi tiết xem `03_Design/08_FrontEnd/Web/review/medical_staff/review_medicalstaff_compliance_kitcheninspection.md` Mục 3. **Giữ nguyên số hiệu BR-208** (không tái sử dụng cho quy tắc khác) để bảo toàn các tham chiếu chéo cũ (VD `MedicalStaff_UserStory.md` US-08 "ghi chú sau BR-208").

> **Ghi chú 05/07/2026 — về ngưỡng "điểm <60%" (đề xuất BR-206 trước đây)**: đội dự án đã xem xét và **quyết định KHÔNG chính thức hóa** đề xuất BR-206 thành một Business Rule mới, vì rà soát toàn bộ `00_Policies/` (Luật ATTP 55/2010/QH12, QĐ 1246/QĐ-BYT, TTLT 13/2016/TTLT-BYT-BGDĐT, NĐ 15/2018/NĐ-CP) không tìm thấy căn cứ pháp luật/quy định nội bộ nào cho ngưỡng 60% hay bất kỳ cơ chế tự động nào gắn với nó.
>
> **Quyết định bổ sung 05/07/2026 — bỏ cơ chế khóa vận hành bếp**: đội dự án quyết định **loại bỏ hoàn toàn** hậu quả "khóa vận hành bếp, chặn phân phát bữa ăn" mà UC-014 AF1 gốc (`Report 3.md`, dòng 3455) mô tả khi điểm <60%. Đây là **descope có chủ đích** so với SRS gốc (không phải sửa lỗi trích dẫn), lý do: (1) không có căn cứ pháp lý cho việc khóa hoàn toàn một bếp ăn chỉ dựa trên 1 điểm số nội bộ, có rủi ro vận hành thực tế (học sinh không có bữa ăn); (2) mã `MSG-024`/`BR-105` mà UC-014 gốc mượn cho cơ chế này thực chất thuộc phạm vi **Incident Management** (xem `error_message.md`, MSG-024 được xếp vào module "Incident Management", không phải "Food Safety Monitoring") — nghĩa là ngay từ đầu SRS đã mượn nhầm cơ chế cách ly cấp-sự-cố (BR-149, cách ly **lô nguyên liệu** cụ thể khi có sự cố ATTP) để áp cho một ngưỡng điểm kiểm tra định kỳ, hai bối cảnh khác hẳn nhau.
>
> **Hành vi mới (thay thế)**: khi điểm checklist <60%, hệ thống **vẫn tự động gửi cảnh báo** tới School Administration (không còn gửi Service Caterers/School Board như AF1 gốc — chỉ School Administration, vì đây là kênh giám sát nội bộ, không phải tình huống khẩn cấp cấp-sự-cố), nhưng **không khóa vận hành bếp, không chặn phân phát bữa ăn**. Bếp tiếp tục vận hành bình thường; School Administration nhận cảnh báo và tự quyết định hành động tiếp theo (nếu cần, có thể tạo Corrective Action Request qua BR-095 như các bước kiểm tra thất bại khác). Kết quả chấm điểm (Pass/Failed) vẫn được ghi nhận đầy đủ theo BR-104.
> **Mã MSG (đã cấp 05/07/2026)**: `MSG-093` (Warning) — "Điểm kiểm tra bếp dưới ngưỡng 60%, đã gửi cảnh báo School Administration, bếp vẫn vận hành bình thường". Không tái dùng `MSG-024` (thuộc Incident Management, ngữ nghĩa "operation blocked" không còn đúng). Xem `error_message.md` §3.
> BR-149 (tự động cách ly **lô nguyên liệu** khi có sự cố ATTP, thuộc Incident Management) **giữ nguyên, không bị ảnh hưởng** bởi quyết định này — đây là cơ chế khác, đúng phạm vi, không liên quan tới checklist kiểm tra bếp định kỳ của UC-014.

### BR-209 — Gửi báo cáo tuân thủ ATTP định kỳ chủ động (UC-185)
- Medical Staff có thể chủ động soạn và gửi một báo cáo tuân thủ ATTP (chọn kỳ **Daily** hoặc **Weekly**) cho School Administration, tổng hợp từ dữ liệu `KitchenInspection`/`InspectionSession` đã ghi nhận trong kỳ được chọn — không cần School Administration phải tự vào Compliance Dashboard (UC-027) mới biết tình hình.
- Nội dung báo cáo tối thiểu: điểm vệ sinh trung bình trong kỳ (BR-104), danh sách phiên kiểm tra Failed (nếu có, kèm lý do), khám sức khỏe nhân viên bếp (BR-103), ghi chú bổ sung tùy chọn của Medical Staff. *(Sửa 27/07/2026: bỏ dòng "tình trạng hiệu lực chứng nhận kiểm soát côn trùng" — BR-102 không còn mô hình hiệu lực/nhật ký độc lập, xem BR-102 mới. Nếu phiên kiểm tra trong kỳ có ghi nhận côn trùng như 1 mục tùy chọn của UC-014, báo cáo nêu kèm trong phần lý do/ghi chú của phiên đó, không tách thành field báo cáo riêng.)*
- Nếu không có bất kỳ phiên kiểm tra nào trong kỳ đã chọn, hệ thống **chặn gửi** và báo lỗi (MSG-092).
- Báo cáo được xuất PDF/Excel theo đúng định dạng chung (BR-178) và gửi thông báo trong hệ thống (BR-158/159) tới School Administration; xác nhận gửi thành công báo MSG-091.
- Mọi lần gửi báo cáo được ghi vào audit log (BR-170).
- *(Bổ sung 05/07/2026: chính thức hóa **US-09** trong `MedicalStaff_UserStory.md` — người dùng đã duyệt 05/07/2026. Đây là bổ sung **ngoài phạm vi `Report 3.md` gốc** (không có UC này trong SRS ban đầu), phát sinh sau khi phát hiện mô hình pull hiện có (UC-027 Compliance Dashboard) chưa có route triển khai thật ở phía School Administration — xem `03_Design/08_FrontEnd/Web/review/review_screen_medical_staff.md` mục 3.9. Cấp UC-185 mới. Không thay thế UC-027 — School Administration vẫn có thể tự xem Compliance Dashboard khi màn đó được xây xong; đây là kênh bổ sung, chủ động hơn.)*
- *(Cập nhật 13/07/2026: điểm vào (entry point) mở rộng thêm — ngoài nút "Gửi báo cáo tuân thủ" trên `23_ComplianceDashboard.md` (giữ nguyên), UC-185 nay cũng truy cập được từ **UC-187 "ATTP Report Center"** — màn hub tổng hợp cả 3 loại báo cáo Medical Staff có thể chủ động gửi (BR-209/213/214). Nội dung/field của UC-185 không đổi. Xem `review_screen_medical_staff.md` Mục 17.)*

### BR-221 — Xác nhận hai chiều biên bản kiểm tra bếp bởi đơn vị suất ăn (UC-014) *(mới 2026-08-03 — xem changelog v2.1)*
- Sau khi Medical Staff nộp một phiên `KitchenInspection` (UC-014), phiên chuyển sang trạng thái **`AWAITING_CATERERS_CONFIRM`**. Đại diện đơn vị suất ăn (Service Caterers) phải **xác nhận đã đọc biên bản** thì phiên mới chuyển sang **`CONFIRMED`**.
- Hệ thống ghi lại: **ai** xác nhận (`recordCaterersRep` — tài khoản đại diện Nhà thầu), **lúc nào** (`confirmedAt`), và thời điểm biên bản được **xuất PDF để in** (`recordPdfExportedAt`, `recordPdfVersion`) cùng ảnh/scan bản đã ký nếu có (`recordSignedFiles`).
- Việc **chưa xác nhận KHÔNG chặn** bếp vận hành, KHÔNG chặn phân phát bữa ăn, và KHÔNG làm thay đổi `score`/`result` của phiên (nhất quán với quyết định descope 05/07/2026 ở ghi chú sau BR-208). Đây là bằng chứng **đã thông báo hai chiều**, không phải một cổng chặn.
- Mọi thao tác xác nhận ghi vào audit log (BR-170).
- *(Bổ sung 2026-08-03: chính thức hóa 4 field `confirmStatus`, `recordCaterersRep`, `recordPdfExportedAt`, `recordSignedFiles` đã tồn tại trong mock frontend nhưng trước đó **không có căn cứ BR/UC nào** — ghi nhận là **CF-10** trong `07_implement/MF07_Food_Safety_Inspection/TDS-MF07_Backend.md` §2.3. Nguồn quyết định gốc: `review_medicalstaff_compliance_kitcheninspection.md` Mục 8.4. Huy xác nhận chính thức hóa 2026-08-03. Không cấp UC mới — hành vi này nằm trong luồng UC-014.)*

### BR-222 — Đánh giá độc lập của Y tế trên phiên kiểm thực ba bước (UC-112) *(mới 2026-08-03 — xem changelog v2.1)*
- Medical Staff có thể ghi một **đánh giá riêng** trên một phiên `InspectionSession` do Service Caterers thực hiện: kết quả (`medicalReviewResult` ∈ {`PASSED`, `FAILED`}), lý do (`medicalReviewReason`), người đánh giá (`medicalReviewBy`), thời điểm (`medicalReviewAt`).
- Đánh giá này **KHÔNG ghi đè** `overall` của phiên và **KHÔNG** làm thay đổi kết quả cổng chặn phục vụ (BR-094/BR-098) — hai chỉ số tồn tại song song: `overall` là kết luận của **đơn vị thực hiện** theo QĐ 1246, `medicalReviewResult` là ý kiến **giám sát độc lập** của nhà trường.
- `medicalReviewResult = FAILED` **bắt buộc** kèm `medicalReviewReason` (không được để trống).
- Chỉ Medical Staff ghi được 4 field này; Service Caterers **chỉ đọc** (phân quyền theo field — xem `TDS-MF07` §16).
- Đánh giá FAILED **không** tự sinh Corrective Action Request (khác BR-095, vốn gắn với bước kiểm thực trượt); School Administration tự quyết định hành động dựa trên báo cáo tuân thủ (BR-104/BR-209).
- *(Bổ sung 2026-08-03: chính thức hóa nhóm field `medicalReview*` đã tồn tại trong mock frontend nhưng trước đó **không có căn cứ BR/UC nào** — **CF-10** trong `TDS-MF07` §2.3. Nguồn quyết định gốc: `review_medicalstaff_compliance_kitcheninspection.md` Mục 8.5 (26/07/2026). Huy xác nhận chính thức hóa 2026-08-03. Không cấp UC mới — gắn vào UC-112 "View Food Inspection Records" vì đây là hành vi của Medical Staff trên màn xem hồ sơ kiểm thực.)*

---

## 13. Phản hồi & Thông báo

### BR-138 — Cảnh báo quan trọng bỏ qua quy trình phê duyệt
- Các cảnh báo **quan trọng về an toàn thực phẩm** (ví dụ: thu hồi thực phẩm) được **phát ngay lập tức** đến tất cả người dùng liên quan mà không cần qua quy trình phê duyệt của quản trị viên.

### BR-139 — Cửa sổ thời gian nộp phản hồi
- Phụ huynh và giáo viên chỉ có thể nộp phản hồi về bữa ăn trong vòng **24 giờ** kể từ khi bữa ăn được phục vụ.

### BR-140 — Tag quan tâm chất lượng bắt buộc khi đánh giá thấp
- Khi phụ huynh đánh giá **dưới 3 sao**, hệ thống **bắt buộc** nhắc chọn **tag quan tâm chất lượng** từ danh sách có sẵn.

### BR-141 — Tổng hợp phản hồi hàng tuần
- Hệ thống **tự động biên soạn** biểu đồ phản hồi tổng hợp **hàng tuần** và gửi báo cáo đến quản lý catering và ban giám hiệu.

### BR-142 — Phát hiện từ khóa bệnh lý trong phản hồi
- Hệ thống **tự động phân tích** nội dung phản hồi của phụ huynh, phát hiện **từ khóa liên quan đến bệnh tật** (ví dụ: "đau bụng", "nôn mửa").
- Khi phát hiện, hệ thống **kích hoạt cảnh báo khẩn cấp** và thông báo ngay cho **Medical Staff** để xem xét.

### BR-143 — Phản hồi từ nhà cung cấp dịch vụ catering
- **Service Caterers** có thể trả lời phản hồi của phụ huynh.

### BR-144 — Lịch sử sửa đổi thông báo
- Mỗi lần cập nhật thông báo (Announcement) phải tạo một **mục lịch sử sửa đổi** kèm timestamp và ID người thực hiện.

### BR-145 — Ẩn danh dữ liệu phản hồi tổng hợp
- Dữ liệu phản hồi được **tổng hợp ẩn danh** vào dashboard của trường.

### BR-158 — Giờ yên tĩnh (Quiet Hours) mặc định
- Mặc định, hệ thống **không gửi thông báo push không khẩn cấp** trong khoảng thời gian **22:00 – 06:00**.
- Cảnh báo khẩn cấp **không bị ảnh hưởng** bởi giờ yên tĩnh.

### BR-159 — Thử lại và dự phòng khi gửi thông báo thất bại
- Khi gửi push notification thất bại, hệ thống **tự động thử lại**; sau khi hết số lần thử lại, hệ thống chuyển sang gửi **email dự phòng**.

---

## 14. AI & Dịch vụ Hỗ trợ

### BR-156 — Đề xuất thực đơn bằng AI
- **Gemini AI Service** phân tích các yếu tố: yêu cầu dinh dưỡng, hồ sơ dị ứng học sinh, nguyên liệu hiện có trong kho, và trần chi phí để **đề xuất thực đơn tối ưu**.
- AI hiển thị kết quả kèm **phân tích dinh dưỡng, danh sách nguyên liệu, và cờ cảnh báo dị ứng**.

### BR-157 — Lưu trữ trao đổi AI bắt buộc
- Toàn bộ cuộc hội thoại giữa người dùng và AI (bao gồm cả đề xuất thực đơn AI) phải được **lưu trữ (archive)** để phục vụ mục đích tuân thủ và kiểm tra.

### BR-158b — Cấu hình giờ yên tĩnh
- Người dùng có thể **cấu hình giờ yên tĩnh** theo cài đặt cá nhân.

### BR-160 — Sao lưu cơ sở dữ liệu tự động
- Hệ thống thực hiện **sao lưu đầy đủ cơ sở dữ liệu** mỗi **Chủ nhật lúc 02:00 sáng**.
- File sao lưu được **mã hóa AES-256** và nhân bản vào **cloud storage ngoại vi (off-site)**.

### BR-161 — Bảo vệ file sao lưu
- File sao lưu phải được mã hóa và lưu trữ tách biệt khỏi hạ tầng chính.

### BR-162 — Nhật ký audit không thể thay đổi
- Nhật ký audit (audit trail) phải là **bất biến (immutable)** — không ai có thể chỉnh sửa hoặc xóa các bản ghi audit.

### BR-163 — Kiểm tra kết nối tích hợp bên ngoài
- Khi cập nhật cấu hình tích hợp dịch vụ bên ngoài (GPS, Gmail, Firebase, Gemini AI), hệ thống phải **kiểm tra kết nối thành công** trước khi lưu cấu hình mới.

### BR-164 — Mã hóa thông tin xác thực tích hợp
- Tất cả thông tin xác thực của dịch vụ bên ngoài (API keys, OAuth credentials) phải được **mã hóa** khi lưu trữ.

### BR-165 — Báo cáo sức khỏe hệ thống hàng tuần
- Hệ thống **tự động biên soạn** báo cáo sức khỏe hệ thống và số liệu người dùng hoạt động **hàng tuần** cho System Admin.

---

## 15. Audit Log & Bảo mật Dữ liệu

### BR-170 — Ghi nhật ký mọi hành động quan trọng
- Tất cả hành động quan trọng trong hệ thống (tạo, cập nhật, xóa, phê duyệt, từ chối) phải được ghi vào **audit trail** kèm: người thực hiện, thời gian, đối tượng bị tác động.

### BR-171 — Chính sách lưu trữ nhật ký
- Hệ thống phải có **chính sách lưu trữ nhật ký (Log Retention Policy)** xác định thời gian tối thiểu lưu giữ dữ liệu nhật ký.

### BR-172 — Bảo vệ toàn vẹn nhật ký
- Hệ thống phải có cơ chế **bảo vệ toàn vẹn** của nhật ký, ngăn chặn mọi hình thức chỉnh sửa trái phép.

### BR-173 — Bảo vệ dữ liệu cá nhân (PDPD)
- Hệ thống phải tuân thủ **Nghị định Bảo vệ Dữ liệu Cá nhân (PDPD)** của Việt Nam.
- Dữ liệu sinh trắc học của trẻ em (học sinh) **không được thu thập** vì lý do bảo mật dữ liệu cá nhân.

### BR-174 — Truy vết lịch sử vận hành
- Hệ thống phải hỗ trợ **truy vấn lịch sử vận hành** và theo dõi audit trail theo thời gian.

---

## Phụ lục: Ma trận Nghiệp vụ — Business Rules theo Module

| Module | Business Rules Chính |
|---|---|
| User Management & Auth | BR-001, BR-002, BR-003, BR-004, BR-005, BR-006, BR-009, BR-010, BR-011, BR-012, BR-015, BR-218, **BR-223** |
| RBAC | BR-007, BR-008, BR-013 |
| School & Class Mgmt | BR-020, BR-021, BR-022, BR-024, BR-025, BR-026, BR-028, **BR-219** |
| Student & Parent Mgmt | BR-030, BR-031, BR-032, BR-201 |
| Supplier & Caterer Mgmt | BR-040, BR-041, BR-046, BR-047, BR-048, BR-050, BR-051, BR-060 |
| Ingredient & Inventory | BR-070 – BR-078, BR-202, BR-204, BR-205 |
| Menu & Diet Mgmt | BR-061 – BR-069, BR-080, BR-082 *(BR-081 đã xoá 2026-07-01 — xem ghi chú tại vị trí cũ)* |
| Three-Step Inspection | BR-079, BR-083, BR-090 – BR-099 |
| Food Sample Retention | BR-105 – BR-114, BR-203, BR-212 *(mới 12/07/2026)* |
| Meal Serving | BR-120, BR-121, BR-123, BR-125, BR-130, BR-133, BR-135 |
| Incident Management | BR-146 – BR-155 |
| Food Safety Monitoring | ~~BR-089~~, BR-100 – BR-104, BR-207, ~~BR-208~~, BR-209, BR-210 *(ngưỡng "điểm <60%" đã descope khỏi cơ chế cách ly bếp, chỉ còn cảnh báo — MSG-093, không có mã BR riêng; BR-089 = Deprecated 11/07/2026 (giám sát nhiệt độ liên tục qua IoT, không có số liệu cụ thể — xem ghi chú tại BR-089); BR-208 = Deprecated 09/07/2026, xem ghi chú tại BR-208; BR-209 = gửi báo cáo tuân thủ chủ động, UC-185, US-09; BR-210 = Medical Staff tự quản lý CRUD checklist, UC-186 — bổ sung 06/07/2026, xem ghi chú cuối §12)* |
| Feedback & Notification | BR-138 – BR-145, BR-158, BR-159 |
| AI & Support Services | BR-156, BR-157, BR-160 – BR-165 |
| Audit Log & Data Security | BR-170 – BR-174 |
| Reporting & Analytics | BR-175 – BR-182, BR-213, BR-214 *(BR-213/214 mới 13/07/2026 — báo cáo tổng hợp sự cố/vi phạm theo kỳ, UC-188/189, gộp điểm vào UC-187 "ATTP Report Center" cùng BR-209/UC-185)* |
| Offline & Data Sync | BR-196 – BR-200 |
| Violation Management (mở rộng MF07 + MF11) | BR-183 – BR-190, BR-211 *(BR-211 bổ sung 06/07/2026 — xuất PDF biên bản + lưu ảnh ký 3 bên, UC-178/179)* |
| Academic Year Management (mở rộng MF02) | BR-191 – BR-195 |

---

## 16. Báo cáo & Phân tích (Reporting & Analytics)

### BR-175 — Phân quyền xem báo cáo theo vai trò
- Quyền truy cập từng loại báo cáo được kiểm soát bởi vai trò (RBAC):
  - **School Administration**: xem toàn bộ báo cáo vận hành của trường.
  - **Medical Staff**: xem báo cáo kiểm tra thực phẩm, sự cố y tế, và tuân thủ an toàn.
  - **Service Caterers**: chỉ xem báo cáo liên quan đến hoạt động của mình (nhận hàng, kiểm tra, sự cố).
  - **System Admin**: xem tất cả báo cáo trên toàn hệ thống.

### BR-176 — Phạm vi dữ liệu báo cáo giới hạn theo trường
- Dữ liệu hiển thị trong báo cáo phải được **giới hạn trong phạm vi trường** mà người dùng đang thuộc về.
- Chỉ **System Admin** được phép xem báo cáo tổng hợp đa trường.

### BR-177 — Lưu trữ lịch sử báo cáo
- Báo cáo đã tạo phải được lưu trữ trong hệ thống tối thiểu **12 tháng**.
- Mỗi lần xuất báo cáo (ai xuất, khi nào, loại báo cáo) phải được ghi vào **audit trail**.
- *(Chú thích 13/07/2026: rule này là căn cứ trực tiếp cho **UC-190 — View Sent Reports History**, màn hình cho phép Medical Staff xem lại các báo cáo ATTP mình đã gửi (BR-209/213/214) — không cấp BR mới, chỉ hiện thực hóa rule đã có sẵn ở phạm vi Medical Staff.)*

### BR-178 — Định dạng xuất báo cáo
- Hệ thống hỗ trợ xuất báo cáo theo hai định dạng: **PDF** và **Excel (XLSX)**.
- Mỗi file xuất phải bao gồm: tiêu đề báo cáo, khoảng thời gian dữ liệu, tên trường, và timestamp xuất.

### BR-179 — Lên lịch xuất báo cáo tự động
- Hệ thống hỗ trợ **lên lịch tạo và gửi báo cáo định kỳ** (hàng ngày, hàng tuần, hàng tháng).
- Báo cáo định kỳ được gửi đến danh sách email được cấu hình bởi School Administration.
- Khi gửi thất bại, hệ thống tự động thử lại tối đa **3 lần**; nếu vẫn thất bại sẽ ghi nhận lỗi và thông báo cho System Admin.

### BR-180 — Dữ liệu dashboard cập nhật trong thời gian thực
- Dashboard Phân tích phải hiển thị dữ liệu được làm mới trong vòng **5 phút** kể từ khi có thay đổi vận hành.
- Nội dung dashboard bao gồm: chỉ số rủi ro an toàn thực phẩm, tỷ lệ tuân thủ kiểm tra, tóm tắt bữa ăn hàng ngày.

### BR-181 — Bảo mật và ẩn danh dữ liệu báo cáo
- Báo cáo **không được tiết lộ thông tin nhận diện cá nhân** của học sinh trong dữ liệu tổng hợp.
- Báo cáo xuất ra ngoài hệ thống phải được đánh dấu **"Confidential"** nếu chứa thông tin nhạy cảm về vận hành hoặc sự cố.

### BR-182 — Toàn vẹn và độ chính xác dữ liệu báo cáo
- Dữ liệu báo cáo phải được tính toán trực tiếp từ **nguồn dữ liệu gốc** trong hệ thống; không cho phép chỉnh sửa thủ công sau khi tạo báo cáo.
- Báo cáo phải hiển thị rõ **ngày giờ tạo** và **khoảng thời gian** dữ liệu được tổng hợp.

### BR-213 — Deprecated 26/07/2026 (Gửi báo cáo tổng hợp sự cố ATTP theo kỳ, UC-188)
- Medical Staff có thể chủ động soạn và gửi một **báo cáo tổng hợp sự cố ATTP** (chọn kỳ **Daily** hoặc **Weekly**, cùng cơ chế chọn kỳ với BR-209) cho School Administration, tổng hợp từ dữ liệu sự cố (`25_IncidentList.md`) phát sinh trong kỳ được chọn.
- Nội dung tối thiểu: số lượng sự cố theo trạng thái (Chờ tiếp nhận/Đang xử lý/Đã đóng), phân bố theo mức độ nghiêm trọng (BR-147), danh sách mã sự cố kèm trạng thái/mức độ (mỗi mã có liên kết điều hướng tới `26_IncidentDetail.md` tương ứng), ghi chú bổ sung tùy chọn của Medical Staff.
- **Không** lặp lại nội dung chi tiết đã có trong báo cáo chính thức của từng sự cố (clinical notes, kết quả xét nghiệm mẫu, root cause, corrective action — đã có sẵn theo BR-154/UC-033, tự động gửi School Administration ngay khi hoàn tất điều tra). Báo cáo tổng hợp chỉ đóng vai trò rollup số liệu + điều hướng, tránh 2 nguồn dữ liệu cùng mô tả 1 sự việc.
- Tuân thủ **BR-181**: không tiết lộ thông tin nhận diện cá nhân học sinh (tên, mã số cụ thể) trong nội dung tổng hợp — chỉ dùng số lượng thống kê; báo cáo xuất ra được đánh dấu **"Confidential"**.
- Nếu không có sự cố nào phát sinh trong kỳ đã chọn, hệ thống **chặn gửi** và báo lỗi (MSG-096).
- Báo cáo được xuất PDF/Excel theo đúng định dạng chung (BR-178) và gửi thông báo trong hệ thống (BR-158/159) tới School Administration; xác nhận gửi thành công báo MSG-098.
- Mọi lần gửi báo cáo được ghi vào audit log (BR-170) và lưu trữ theo BR-177 (xem lại tại UC-190 — View Sent Reports History).
- *(Bổ sung 13/07/2026: phát sinh từ phản hồi người dùng khi rà soát UC-185 — nhận thấy sự cố ATTP là mảng dữ liệu Medical Staff quản lý nhưng chưa có kênh báo cáo tổng hợp định kỳ lên School Administration, ngoài báo cáo từng-sự-cố-một theo BR-154. Cấp UC-188 mới, gộp chung điểm vào với UC-185 tại UC-187 "ATTP Report Center". Xem `review_screen_medical_staff.md` Mục 17.)*
- **Deprecated 26/07/2026**: đối chiếu code thật xác nhận School Administration đã có đủ dữ liệu để xử lý/đóng từng Incident (`incident-detail-view.tsx`, `school-admin/incidents/[id]/close/page.tsx`) và có sẵn export danh sách sự cố lọc theo severity/status/caterer (UC-031) — nhưng **thiếu báo cáo quản trị tổng hợp theo kỳ** (xu hướng, phân bố mức độ/trạng thái, CAPA tồn đọng). Quyết định: bổ sung màn báo cáo tổng hợp Incident **tại chính module Incident của School Administration** (tự mở, tự lọc kỳ, tự export — không cần Medical Staff soạn/gửi), vì toàn bộ dữ liệu nguồn (`Incident`) đã có sẵn ở phía School Admin, không cần thêm 1 kênh gửi/nhận trùng lặp dữ liệu và trạng thái "đã gửi/chưa gửi". BR-213 do đó **không còn cần thiết** — UC-187 "ATTP Report Center" (nơi UC-188 từng có điểm vào) cũng Deprecated cùng đợt (xem `review_medicalstaff_compliance_kitcheninspection.md` Mục 10). BR-214 (Báo cáo Tổng hợp Vi phạm) **không đổi** — không có căn cứ tương tự để bỏ.

### BR-214 — Gửi báo cáo tổng hợp vi phạm theo kỳ (UC-189)
- Medical Staff có thể chủ động soạn và gửi một **báo cáo tổng hợp vi phạm** (chọn kỳ Daily/Weekly) cho School Administration, tổng hợp từ dữ liệu vi phạm (`27_ViolationRegistry.md`, UC-181) ghi nhận trong kỳ được chọn.
- Nội dung tối thiểu: số vi phạm trong kỳ theo loại (quy trình/nhân viên/đối tác — BR-189), theo mức độ (BR-186), danh sách vi phạm kèm đối tượng liên quan/mức độ/trạng thái (mỗi vi phạm có liên kết tới biên bản PDF đã ký nếu có, theo BR-211), ghi chú bổ sung tùy chọn.
- **Không** lặp lại nội dung chi tiết biên bản vi phạm đã ký 3 bên (đã có sẵn theo BR-211, xuất PDF ngay khi lập từng vi phạm) — báo cáo tổng hợp chỉ rollup số liệu + điều hướng.
- Nếu không có vi phạm nào ghi nhận trong kỳ đã chọn, hệ thống **chặn gửi** và báo lỗi (MSG-097).
- Báo cáo được xuất PDF/Excel theo đúng định dạng chung (BR-178) và gửi thông báo trong hệ thống (BR-158/159) tới School Administration; xác nhận gửi thành công báo MSG-099.
- Mọi lần gửi báo cáo được ghi vào audit log (BR-170) và lưu trữ theo BR-177 (xem lại tại UC-190).
- Đồng thời bổ sung **nút Export (theo kỳ)** trực tiếp trên `27_ViolationRegistry.md` (UC-181) — khoảng trống phát hiện khi rà soát: màn này trước đây chưa có Export, khác với các màn danh sách khác đã có (Incident List, View Food Safety Reports).
- *(Bổ sung 13/07/2026: tương tự BR-213, cấp UC-189 mới, gộp điểm vào UC-187. Xem `review_screen_medical_staff.md` Mục 17.)*

---

## 17. Offline & Đồng bộ Dữ liệu (Offline & Data Sync)

> **Quyết định** (2026-07-01, giải quyết mâu thuẫn giữa Report 1 "offline-capable" và Report 3 NFR "requires stable Internet"): hệ thống hỗ trợ offline **có giới hạn** — cho phép ghi nhận bằng chứng khi mất kết nối, nhưng **bắt buộc** kết nối mạng cho mọi thao tác xác minh an toàn thời gian thực. Áp dụng cho ứng dụng Mobile (Flutter); Web luôn yêu cầu kết nối như hiện tại.

### BR-196 — Cho phép ghi nhận bằng chứng khi offline
- Các thao tác **chỉ ghi nhận bằng chứng** (không cần xác minh dữ liệu server để quyết định đúng/sai) được phép thực hiện offline: chụp ảnh + GPS + timestamp trong kiểm tra ba bước (BR-083), nhận hàng nguyên liệu (BR-072), lưu mẫu thực phẩm (BR-107), và điền checklist kiểm tra.
- Dữ liệu được lưu vào **hàng đợi đồng bộ cục bộ (local sync queue)** trên thiết bị và tự động gửi lên server ngay khi có kết nối trở lại.

### BR-197 — Bắt buộc kết nối mạng cho xác minh an toàn thời gian thực
- Các thao tác sau **không được phép** thực hiện offline, kể cả khi có dữ liệu cache cũ trên thiết bị:
  - Cảnh báo dị ứng khi quét ID học sinh (BR-123).
  - Xác minh trạng thái Approved của nhà cung cấp trước khi nhận hàng (BR-070).
  - Kiểm tra trạng thái Quarantine của lô hàng (BR-149).
  - Xác thực sinh trắc học khuôn mặt (BR-051).
- Khi không có kết nối, hệ thống **chặn thao tác** và hiển thị thông báo yêu cầu kết nối mạng — không được phép dùng dữ liệu cache để tự ý cho qua.

### BR-198 — Tổng hợp sự cố tự động yêu cầu kết nối server
- Việc tự động kích hoạt điều tra sự cố khi có từ 3 học sinh trở lên báo triệu chứng tương đồng trong 24 giờ (BR-146) yêu cầu tổng hợp dữ liệu thời gian thực phía server — không thể thực hiện hoặc mô phỏng offline trên thiết bị.

### BR-199 — Giải quyết xung đột khi đồng bộ
- Khi hai thiết bị cùng chỉnh sửa một bản ghi trong thời gian offline, hệ thống áp dụng nguyên tắc **bản ghi mới nhất theo timestamp server thắng** (last-write-wins theo thời gian server, không dùng thời gian thiết bị).
- Cả hai lần ghi đều phải được lưu vào **audit log bất biến** (BR-162) để không mất dấu vết, kể cả bản ghi bị ghi đè.

### BR-200 — Giới hạn thời gian hiệu lực của dữ liệu cache offline
- Dữ liệu cache cho phép dùng khi offline (thực đơn, danh sách lớp, danh sách học sinh...) chỉ hợp lệ trong tối đa **[TODO — team xác định số giờ]** kể từ lần đồng bộ gần nhất.
- Khi cache quá hạn, màn hình liên quan hiển thị trạng thái **"Cần đồng bộ lại"** và hạn chế các thao tác ghi mới cho tới khi đồng bộ thành công.

---

## 18. Quản lý Vi phạm & Xử lý Sai phạm

> **Bối cảnh**: Phản hồi của Hội đồng đồ án — *"Nên khảo sát quy trình xử lý sai phạm và thực hiện quản lý/ghi nhận trên hệ thống."* Trước đây hệ thống chỉ có cơ chế rời rạc cho vi phạm quy trình/sản phẩm (`NonConformanceRecord` → `CorrectiveActionRequest`) và sự cố nghiêm trọng (`FoodSafetyIncident` → `CAPA`), nhưng **chưa có cơ chế nào cho vi phạm hành vi/quy trình gây ra bởi Nhà thầu**. Chính thức hoá từ `02_Requirements/Violation_Management_Proposal.md` (2026-07-01).
> **Quyết định phạm vi**: `ViolationResolutionRecord` (BR-185) chạy **song song** với `CorrectiveActionRequest`/`CAPA` hiện có — không thay thế; mở rộng module MF07 (Food Safety Inspection & Monitoring) và MF11 (Incident & Emergency Management) + registry tổng hợp, không tạo module MF13 riêng. Biên bản vi phạm do **Medical Staff** và **Service Caterers** lập (cả hai vai trò đều có quyền phát hiện và lập biên bản).
> **Sửa 26/07/2026 (nguồn: phỏng vấn thực tế Cán bộ Y tế, `review/medical_staff/review_medicalstaff_violations.md` Mục 9.1)**: bỏ hẳn khái niệm biên bản/kỷ luật nhắm **cá nhân nhân viên** của Nhà thầu ra khỏi phạm vi vai trò Medical Staff — quyết định người dùng: *"Bỏ hết những gì xử phạt liên quan đến nhân viên của bên cateerers tại role medical staff... Medical staff chỉ lập biên bản đối với đại diện của cateerers."* BR-184 (biên bản vi phạm nhân viên) **gộp vào BR-183**, không còn là loại biên bản riêng; BR-185/187/190 sửa lại theo cấp đơn vị (đơn vị Nhà thầu), không còn theo cá nhân. Việc kỷ luật nhân viên cụ thể (nếu có) là trách nhiệm nội bộ của Nhà thầu, ngoài phạm vi SFMS.

### BR-183 — Hình thức hóa Biên bản Vi phạm (cấp Đơn vị Nhà thầu)
- Mọi vi phạm về an toàn thực phẩm/quy trình/hành vi do **Service Caterers** gây ra — dù phát hiện qua `NonConformanceRecord` (thất bại 1 bước trong kiểm tra ba bước) hay phát hiện trực tiếp qua audit/quan sát (bỏ qua quy trình bắt buộc, vi phạm vệ sinh, lạm dụng manual override, hành động không được ủy quyền...) — đều phải được bổ sung đầy đủ thành **1 loại Biên bản Vi phạm duy nhất**, nhắm vào **đơn vị Nhà thầu** (không nhắm cá nhân nhân viên): người phát hiện (`reportedBy`), mô tả, ảnh bằng chứng, mức độ nghiêm trọng (`severityLevel`), đại diện Nhà thầu (`caterersRepresentativeId`).
- **Sửa 11/07/2026**: nhập liệu qua **1 dialog hợp nhất** (`22_RecordStaffViolation.md`), không còn màn hình riêng (UC-178 cũ, nay Deprecated).
- **Sửa 26/07/2026**: gộp luôn nội dung BR-184 (trước đây là "Biên bản Vi phạm Nhân viên" tách riêng) — không còn phân biệt 2 loại biên bản theo nguồn phát hiện; không còn field định danh cá nhân (`staffName`/chức vụ) ở bất kỳ đâu trong form.

### BR-184 — Deprecated 26/07/2026 (gộp vào BR-183)
- ~~Biên bản Vi phạm Nhân viên (`StaffViolationRecord`), field `staffName` tùy chọn~~ — nội dung đã gộp hoàn toàn vào BR-183 (cấp đơn vị). Giữ số hiệu để tránh xáo trộn liên kết chéo trong tài liệu cũ; không còn hiệu lực độc lập.

### BR-185 — Biên bản Xử lý Sai phạm
- **Biên bản Xử lý Sai phạm** (`ViolationResolutionRecord`) phải ghi nhận: hành động xử lý (`actionTaken`), người xử lý (`handledBy`), và mức độ xử lý áp dụng cho **đơn vị Nhà thầu** (`unitSanctionLevel`: Nhắc nhở bằng văn bản / Cảnh cáo hợp đồng / Phạt vi phạm hợp đồng / Đề xuất chấm dứt hợp đồng).
- Cơ chế này **chạy song song** với `CorrectiveActionRequest`/`CAPA` hiện có (không thay thế).
- **Sửa 26/07/2026**: bỏ hẳn field `disciplinaryLevel` (kỷ luật cá nhân nhân viên: Nhắc nhở/Cảnh cáo/Đình chỉ/Đề xuất chấm dứt hợp đồng nhân viên) — thay bằng `unitSanctionLevel` áp dụng cho đơn vị Nhà thầu như trên. Medical Staff không còn đề xuất/theo dõi hình thức kỷ luật nhắm cá nhân nhân viên của Nhà thầu.

### BR-186 — Phân cấp phê duyệt Xử lý Sai phạm
- Vi phạm mức **LOW**: **Medical Staff** được phép trực tiếp phê duyệt/đóng Biên bản Xử lý Sai phạm mà không cần chuyển School Administration.
- Vi phạm mức **MEDIUM** hoặc **HIGH**: bắt buộc **School Administration** phê duyệt hoặc từ chối trước khi đóng biên bản — theo đúng pattern đã có ở `CorrectiveActionRequest.approve()` và `CAPA.approve()`.

### BR-187 — Cảnh báo nghi ngờ tái diễn vi phạm (best-effort)
- Khi phát hiện vi phạm mới với **đại diện/đơn vị Nhà thầu** (`caterersRepresentativeId`) trùng khớp với một Biên bản Vi phạm khác cùng mức **≥ MEDIUM** trong vòng **90 ngày gần nhất** (mặc định, School Administration có thể điều chỉnh khoảng thời gian này theo nhu cầu thực tế), hệ thống hiển thị cảnh báo nghi ngờ tái diễn để School Administration xác minh thủ công.
- Đây là cơ chế **best-effort, không đảm bảo tuyệt đối** và **không tự động escalate**.
- **Sửa 26/07/2026**: đổi căn cứ đối chiếu từ `staffName` (text tự do, theo cá nhân) sang `caterersRepresentativeId`/đơn vị Nhà thầu (khóa ngoại đã có, chính xác hơn) — khớp việc bỏ hẳn khái niệm cá nhân ở BR-183/184.

### BR-188 — Audit log bất biến cho hồ sơ vi phạm
- Mọi Biên bản Vi phạm (BR-183, gồm cả `NonConformanceRecord` đã nâng cấp) và Biên bản Xử lý Sai phạm (`ViolationResolutionRecord`) phải được ghi vào audit log bất biến — áp dụng cùng nguyên tắc với BR-162, BR-172.

### BR-189 — Danh sách tổng hợp vi phạm (Violation Registry)
- **School Administration** phải có màn hình xem danh sách tổng hợp **mọi loại vi phạm** (quy trình, đối tác) theo mức độ và trạng thái, phục vụ giám sát tổng thể.
- **Sửa 26/07/2026**: bỏ "nhân viên" khỏi phân loại liệt kê — chỉ còn phân loại theo nguồn phát hiện (quy trình/audit trực tiếp), không còn phân loại theo cá nhân/đơn vị vì toàn bộ biên bản nay đều ở cấp đơn vị (BR-183).

### BR-190 — Báo cáo lạm dụng Manual Override hàng tuần
- Hệ thống phải tự động tổng hợp **hàng tuần** các trường hợp lạm dụng chức năng ghi đè thủ công (manual override), thể hiện dưới dạng Biên bản Vi phạm (BR-183, cấp đơn vị Nhà thầu) với `violationType = OVERRIDE_ABUSE`.
- **Sửa 26/07/2026**: đổi "Biên bản Vi phạm Nhân viên" thành "Biên bản Vi phạm" (đơn vị) — khớp việc gộp BR-184 vào BR-183.

### BR-211 — Xuất Biên bản Vi phạm, ký tay 2 bên, chụp ảnh/scan tải lên (UC-179 — mã sống sót; UC-178 Deprecated)
- Sau khi người lập biên bản (Medical Staff hoặc Service Caterers) điền đầy đủ các trường bắt buộc của Biên bản Vi phạm (BR-183, cấp đơn vị) — **qua 1 dialog/route hợp nhất duy nhất, `22_RecordStaffViolation.md`** — hệ thống xuất PDF (theo mẫu `00_Policies/specialreport2173_1.md`, tiêu đề "BIÊN BẢN VI PHẠM"). Người lập **in bản PDF, ký tay** cùng Đại diện Nhà thầu (`caterersRepresentativeId`) ngoài đời, sau đó **chụp ảnh/scan và tải lên** (`signedRecordPhotoUrl`, bắt buộc). Có ảnh ký hợp lệ → biên bản chuyển trạng thái **Đã khóa**, không sửa được nội dung, có giá trị lưu trữ trên hệ thống.
- Trường dữ liệu: `locationOfIncident`, `occurredAt`, `caterersRepresentativeId`, `signedRecordPhotoUrl` (bắt buộc). Cột chữ ký "Người chứng kiến" trên bản PDF tùy chọn tiếp tục để trống tĩnh — hệ thống không track `witnessId` dưới bất kỳ hình thức nào (giữ nguyên quyết định 11/07/2026).
- Việc lưu Biên bản Vi phạm **không bị chặn** bởi việc chưa có ảnh ký — dữ liệu số vẫn được lưu ngay để phục vụ cảnh báo severity/tái diễn (BR-186, BR-187) và audit log (BR-188).
- **Gate bắt buộc**: tại `24_RecordViolationResolution.md`, nút "Đóng biên bản" (LOW) và "Gửi School Administration phê duyệt" (MEDIUM/HIGH) **bị vô hiệu hóa (disabled)** cho đến khi biên bản đã có `signedRecordPhotoUrl`.
- Hệ quả: vì gate được áp dụng ngay tại bước "Gửi School Administration phê duyệt", nên khi hồ sơ MEDIUM/HIGH tới màn hình `48_ApproveRejectViolationResolution.md` (UC-182), ảnh ký **luôn đã tồn tại** — không cần thêm gate riêng ở bước Approve/Reject của School Administration.
- *(Lịch sử: 06/07/2026 chốt cơ chế in/ký tay/chụp ảnh + gate chặn cứng; 11/07/2026 gộp UC-178+UC-179 thành 1 dialog, bỏ hẳn `witnessId`, đổi tiêu đề PDF; **26/07/2026 đổi tạm sang cơ chế 2 nút xác nhận điện tử** — sau đó phát hiện lỗi kiến trúc (2 nút không thể cùng nằm trên 1 màn hình vì Y tế và Service Caterers thuộc 2 role/layout khác nhau; hơn nữa `caterersRepresentativeId` chỉ là "chọn tài khoản" do Medical Staff thao tác hộ, không phải Nhà thầu tự đăng nhập xác nhận — nên "2 xác nhận điện tử" trên thực chất không phải hành động độc lập của 2 bên) → **27/07/2026 đảo ngược lại nguyên bản (06/07, 11/07/2026)**: quay về in/ký tay/chụp ảnh tải lên. Ảnh chữ ký tay là bằng chứng có giá trị pháp lý/đối chiếu trách nhiệm cao hơn, phù hợp với hệ quả biên bản có thể dẫn tới chế tài hợp đồng đối với Nhà thầu (`unitSanctionLevel`, BR-185). Xem `review/medical_staff/review_medicalstaff_violations.md` Mục 11. Chưa áp code cho lần đảo ngược 27/07/2026 — xem checklist file review tương ứng.)*

---

## 19. Quản lý Năm học

> **Bối cảnh**: Rà soát cho thấy hệ thống hoàn toàn chưa có khái niệm vòng đời năm học — `Classroom` là bản ghi tĩnh, không phân biệt được "Lớp 5A năm học 2026–2027" với "Lớp 5A năm học 2027–2028". Chính thức hoá từ `02_Requirements/AcademicYear_Management_Proposal.md` (2026-07-01).
> **Quyết định thiết kế đã chốt**: Hồ sơ sức khỏe/dị ứng học sinh (`StudentHealthRecord`, `AllergyProfile`) **không tách theo năm học** — vẫn là 1 hồ sơ xuyên suốt gắn với `Student`, giữ nguyên qua các năm học. Chỉ `Classroom` là entity có vòng đời gắn với năm học cụ thể (nguyên tắc **append-only theo năm học**: không sửa/xóa `Classroom` cũ, tạo bản ghi `Classroom` mới cho năm học mới).

### BR-191 — Một năm học Active duy nhất mỗi trường
- Mỗi trường chỉ được có **đúng 1** `AcademicYear` ở trạng thái **Active** tại một thời điểm.
- Tạo/chuyển một năm học sang trạng thái Active mới bắt buộc phải đóng (Close) năm học Active hiện tại trước.
- Được phép tồn tại song song một năm học ở trạng thái **Draft** trong khi năm học hiện tại vẫn đang Active (chuẩn bị trước cho năm học kế tiếp); ràng buộc "duy nhất" chỉ áp dụng cho trạng thái Active.

### BR-192 — Classroom phải thuộc về một năm học Active
- Mỗi `Classroom` phải thuộc về **đúng 1** `AcademicYear`.
- Không thể tạo `Classroom` mới khi trường chưa có năm học ở trạng thái Active.

### BR-193 — Đóng năm học chuyển Classroom sang chỉ đọc
- Khi đóng (Close) một năm học, toàn bộ `Classroom` thuộc năm học đó chuyển sang trạng thái **chỉ đọc** (không tạo học sinh mới, không đổi giáo viên chủ nhiệm), nhưng toàn bộ dữ liệu lịch sử vẫn được giữ nguyên để tra cứu/báo cáo — không xóa dữ liệu, áp dụng cùng nguyên tắc cascade với BR-021.

### BR-194 — Sao chép cấu trúc lớp khi tạo năm học mới; "lên lớp" là hành động tường minh
- Khi tạo năm học mới, hệ thống cho phép **sao chép cấu trúc lớp** (tên lớp, cấp lớp, sức chứa) từ năm học liền trước làm mẫu khởi tạo.
- Hệ thống **không tự động** chuyển học sinh sang lớp mới. "Lên lớp" (promote) là một hành động riêng, tường minh, do School Administration thực hiện — hỗ trợ chuyển hàng loạt theo khối (VD "cả khối 5 lên khối 6") kèm khả năng loại trừ/giữ lại từng học sinh cụ thể để xử lý trường hợp lưu ban.
- Hồ sơ sức khỏe/dị ứng học sinh (`StudentHealthRecord`, `AllergyProfile`) không bị ảnh hưởng bởi thao tác lên lớp — không cần Medical Staff xác nhận lại.

### BR-195 — Dữ liệu năm học đã Archived luôn truy vấn được
- Dữ liệu của năm học đã **Archived** (bữa ăn, kiểm tra, sự cố... gắn với `Classroom` của năm học đó) phải luôn truy vấn được ở chế độ **read-only** phục vụ audit/báo cáo dài hạn — áp dụng cùng nguyên tắc với BR-171 (Log Retention Policy) và BR-174 (Truy vết lịch sử vận hành).

---

*Tài liệu này được tổng hợp từ Report 1 (Project Introduction) và Report 3 (SRS) của dự án SFMS — SEP490 G8.*
