# 06 — Thiết kế: Nối thật `sent-reports` (báo cáo tuân thủ/vi phạm gửi School Administration)

**Ngày lập:** 2026-08-05 · **Trạng thái:** ✅ XONG toàn bộ (backend + frontend), verify qua curl
thật, đã dọn dữ liệu test
**Người đề xuất:** Nam, qua trao đổi khi rà soát phần frontend còn dở dang

> **Tóm tắt 1 câu:** `sent-reports` là 1 trong 2 resource frontend còn lại chưa nối thật (cùng
> `school-config`) — gap này KHÔNG phải rename/lệch tên field, mà là 2 màn soạn báo cáo
> (`compliance/report`, `violations/reports`) tự soạn ở client một payload phong phú hơn nhiều
> những gì backend (`ComplianceDataPort`/`ViolationQueryPort`) thật sự tổng hợp được — cần thiết
> kế lại tính năng trước khi nối, không thể chỉ đổi tên field.

---

## 1. Hiện trạng đối chiếu Backend ↔ Frontend (trước khi sửa)

| | Backend chấp nhận/tính | Frontend đang gửi |
|---|---|---|
| Compliance | `rangeFrom,rangeTo,period,notes` → tự tính `totalInspections/averageScore/passCount/failCount/inspections[]` từ `kitchen_inspections` | `reportType,rangeLabel,avgScore,failedSessions[],threeStepSummary,staffHealthValid,assessment,sentBy,recipient,...` — không có `rangeFrom/rangeTo` |
| Violation | `rangeFrom,rangeTo,period,notes` → tự tính `totalViolations/violations[]` từ `violation_records` | `reportType,rangeLabel,summary,counts{processCount,staffCount,bySeverity},violationIds[],...` — không có `rangeFrom/rangeTo` |

`POST /sent-reports` (bare) không map tới handler nào — chỉ có `/compliance` và `/violations`.
Gọi `useCreate("sent-reports")` như FE cũ đang làm sẽ luôn 404/405.

## 2. Quyết định (Nam, 05/08/2026 — AskUserQuestion nhiều vòng)

1. **`assessment`** (nhận định chuyên môn Y tế, báo cáo tuân thủ) — free text hợp lệ, **thêm
   field mới** vào `SendComplianceReportRequest`, server lưu NGUYÊN VĂN vào `payload` (giống
   `notes`), không tính lại — không vi phạm ADR-M12-05 vì đây là input, không phải số liệu suy ra.
2. **`staffHealthValid`** (cả kỳ) — **TRUE chỉ khi MỌI lần kiểm tra bếp trong kỳ đều TRUE** (1
   lần vi phạm là cả kỳ không đạt). Dữ liệu gốc `KitchenInspection.staffHealthValid` đã có sẵn
   (cột thật), chỉ chưa lộ ra `ComplianceDataPort.InspectionBrief` — thêm field, không cần
   migration.
3. **`processCount`/`staffCount`** (phân loại vi phạm theo quy trình/nhân sự) — **KHÔNG có dữ
   liệu nguồn** (`ViolationBrief` không có field phân loại này). Nam chọn "thêm phân loại mới ở
   MF11" nhưng coi là **việc riêng, không gộp đợt này** — báo cáo vi phạm hiện tại chỉ còn
   `bySeverity` (suy được từ `violations[].severity` có sẵn).
4. **"Lý do" của 1 lần kiểm tra FAILED** — không có cột text riêng ở `KitchenInspection` (chỉ có
   `result`/`score`). Chọn **bỏ cột lý do**, chỉ hiện mã + ngày + điểm; muốn xem chi tiết vì sao
   FAILED thì mở đúng phiên kiểm tra đó (đã có màn riêng).
5. **`threeStepSummary`** (kiểm thực 3 bước) — **KHÔNG đưa vào payload đã gửi**. Nguồn dữ liệu
   hoàn toàn khác (`inspection_sessions`, MF07) so với `ComplianceDataPort` (chỉ đọc
   `kitchen_inspections`) — giữ làm card hiển thị RIÊNG ở màn soạn (context, không phải nội dung
   báo cáo), không mở rộng `ComplianceDataPort` sang nguồn thứ 2 trong đợt này.

### 2.1. Tự phát hiện & tự sửa: `staffInvolved` mâu thuẫn QĐ 7

Ban đầu đã thêm `staffInvolved` (nhân sự liên quan, tự do) vào `SendViolationReportRequest`
theo cùng logic với `assessment` — nhưng khi đọc lại `violations/reports/page.tsx` (màn ĐANG
SỐNG, không phải dialog cũ) phát hiện field này **trực tiếp mâu thuẫn QĐ 7** đã ghi rõ trong
chính code: *"báo cáo không nêu tên cá nhân dưới bất kỳ hình thức nào"* (Mục 10.3,
27/07/2026 — cảnh báo tái diễn BR-187 đổi hẳn sang đối chiếu theo đơn vị Nhà thầu, không theo
tên nhân viên). Field `staffInvolved` chỉ tồn tại ở dialog **ĐÃ CHẾT** (`send-violation-summary-
dialog.tsx`, không còn điểm gọi nào từ 26/07/2026) — nhầm lẫn lấy yêu cầu từ code chết thay vì
code đang sống. Đã **revert** field này khỏi backend (không hỏi lại người dùng — QĐ 7 là quyết
định đã có sẵn, ghi rõ trong code, không mơ hồ).

## 3. Triển khai — Backend

- `ComplianceDataPort.InspectionBrief` thêm field `staffHealthValid` (mục 2.2); `Compliance
  DataPortImpl.kitchenInspectionsIn()` populate từ `KitchenInspection.isStaffHealthValid()`.
- `SendComplianceReportRequest` thêm `assessment` (nullable); `ComplianceReportSendServiceImpl`
  lưu `payload.put("assessment", request.assessment())` +
  `payload.put("staffHealthValid", inspections.stream().allMatch(InspectionBrief::staffHealthValid))`.
- `SendViolationReportRequest` **KHÔNG đổi** (vẫn đúng 4 field cũ) sau khi revert `staffInvolved`.
- Không có migration DB nào — mọi field mới chỉ thêm KEY vào `payload` (JSONB), không đổi
  schema bảng `sent_reports`.

### 3.1. Bug phát hiện khi verify: JSONB `LocalDate` serialize không nhất quán

Verify `GET /sent-reports/{code}` sau khi tạo mới phát hiện `payload.inspections[].date` trả
`"2026-07-05"` (ISO string) ở response CREATE, nhưng trả `[2026,7,5]` (mảng) khi GET LẠI cùng
bản ghi từ DB — **cùng 1 API, 2 hình dạng khác nhau tuỳ mới tạo hay đọc lại**, sẽ làm hỏng bất kỳ
FE code nào parse `payload.X.date` là string cố định.

**Nguyên nhân gốc**: Hibernate `@JdbcTypeCode(SqlTypes.JSON)` (13 entity dùng, toàn hệ thống —
`RoleRecord`, `SchoolConfig`, `DietGroup`, `Dish`, `ReceivingLine`, `Announcement`, `Feedback`,
`NotificationPreference`, `UserNotification`, `Incident`, `IncidentSourceReport`, `SentReport`,
`ReportExport`) mặc định dùng MỘT `ObjectMapper` NỘI BỘ riêng của Hibernate — KHÔNG phải bean
`ObjectMapper` app đang cấu hình (`JacksonConfig`, có `JavaTimeModule` + ISO dates). REST layer
(Spring MVC) dùng bean app → ISO string đúng; Hibernate JSON column dùng mapper nội bộ không có
`JavaTimeModule` → LocalDate serialize kiểu mảng mặc định của Jackson.

**Vá tận gốc** (`JacksonConfig.java`, `hibernateJsonFormatMapperCustomizer` — `Hibernate
PropertiesCustomizer` bean trỏ `AvailableSettings.JSON_FORMAT_MAPPER` về ĐÚNG bean `ObjectMapper`
app đang dùng) — ảnh hưởng **CẢ 13 entity**, không riêng `sent-reports`. Verify: xoá bản ghi cũ
(serialize SAI, lưu trước khi vá), tạo bản ghi MỚI sau khi vá → cả response CREATE lẫn GET LẠI
đều trả `"2026-07-05"` nhất quán.

## 4. Triển khai — Frontend

- `lib/api/types.ts` — thêm `SentReportDto`/`SendComplianceReportDto`/`SendViolationReportDto`
  (`Api<"...">` alias thật, thay hoàn toàn `SentReport` mock).
- `lib/report-period.ts` — `MOCK_TODAY` (hằng số neo cứng "hôm nay" = 02/07/2026, dùng cho MỌI
  tính toán kỳ báo cáo) đã **deprecated**, `periodRange()` đổi sang `new Date()` thật. Đây là
  bug tiềm ẩn phát hiện khi rà: nối `sent-reports` thật nghĩa là lọc `kitchen_inspections`/
  `violation_records` theo NGÀY THẬT — neo "hôm nay" ở một ngày cố định trong quá khứ sẽ khiến
  mọi báo cáo tính sai kỳ vĩnh viễn. Thêm `toIsoDate()` (local-time, không qua `toISOString()`
  UTC — tránh lệch ngày gần nửa đêm) để gửi đúng `rangeFrom`/`rangeTo` backend cần.
- `components/app/report-document.tsx` — viết lại HOÀN TOÀN `ReportDocumentData`/
  `buildReportDocumentHtml`/`ReportDocument` theo ĐÚNG shape backend (`averageScore`/
  `passCount`/`failCount`/`inspections[]`/`staffHealthValid`/`assessment` cho compliance;
  `totalViolations`/`violations[]` cho violation, `bySeverity` suy client-side từ
  `violations[].severity`). Thêm `toComplianceDoc()`/`toViolationDoc()`/`toIncidentDoc()` map
  `SentReportDto` (đã gửi) → `ReportDocumentData`; màn soạn tự dựng object CÙNG shape từ số liệu
  đang tính làm bản xem trước — giữ đúng nguyên tắc gốc "xem trước và bản gửi dùng chung 1
  component render, không lệch nhau".
- `medical-staff/compliance/report/page.tsx` — `submit()` chỉ gửi
  `{rangeFrom,rangeTo,period,notes,assessment}`; giữ nguyên các card hiển thị nguồn dữ liệu
  (đọc trực tiếp `kitchen-inspections`/`inspections`, đã là API thật từ trước) — riêng card
  "Kiểm thực ba bước" giữ lại làm NGỮ CẢNH khi soạn, không gửi vào payload (quyết định mục 2.5).
- `medical-staff/violations/reports/page.tsx` — `submit()` chỉ gửi
  `{rangeFrom,rangeTo,period,notes}`; giữ nguyên số liệu tự tính hiển thị (QĐ 7), KHÔNG thêm ô
  nhập `staffInvolved` (mục 2.1).
- `medical-staff/reports/page.tsx` + `[id]/page.tsx` — đổi type sang `SentReportDto`, dùng
  `toComplianceDoc()`/`toViolationDoc()`/`toIncidentDoc()`. Bỏ bộ lọc "Gửi từ ngày/Đến ngày" ở
  màn danh sách — backend thật (`SentReportQueryServiceImpl.JSON_TO_COLUMN`) chỉ lọc được
  `reportType`/`period`, không có `dateFrom`/`dateTo` như `DataTable` mock cũ tự lọc mọi field.
- `medical-staff/compliance/page.tsx` — đổi `usePagedList<SentReport>` sang
  `usePagedList<SentReportDto>`, sửa card "Báo cáo đã gửi gần nhất" đọc `sentAt` thay vì `date`
  (field cũ không còn tồn tại ở DTO thật).
- **Xoá hẳn 3 dialog chết** (`send-compliance-report-dialog.tsx`, `send-violation-summary-
  dialog.tsx`, `send-incident-summary-dialog.tsx`) — không còn điểm gọi nào từ 26–27/07/2026,
  gửi payload theo shape KHÔNG còn khớp `sent-reports` thật. File `report-center/page.tsx`
  (route redirect deprecated) trước đó ghi chú "để nguyên theo quy ước không xoá cứng" — đã xoá
  theo yêu cầu rõ ràng của người dùng hôm nay (giảm khối lượng code không dùng), cập nhật lại
  comment cho khớp.
- **Dọn `mock-data.ts`** — xoá `SentReport`/`ComplianceSentReport`/`ViolationSentReport`/
  `IncidentSummarySentReport`/`SentReportThreeStepRow`/`SentReportFailedSession`/
  `SENT_REPORTS` (toàn bộ ~110 dòng, thay hoàn toàn bởi `SentReportDto`/`ReportDocumentData`).
  `db.ts` bỏ seed `"sent-reports"` (resource nối thật, không còn phục vụ qua mock adapter).
- Thêm `"sent-reports"` vào `REAL_RESOURCES` (MF12, nay **5/5 resource**).
- Sửa tiện thể 1 comment lỗi thời ở `scripts/seed-read-guard.ts` (`KNOWN_SEED_READS` ghi
  `DISH_LIBRARY` là do "MF04 chưa có backend" — sai, `dishes` đã nối thật từ lâu; nợ thật là 2
  helper thuần đồng bộ (`menu-personalization.ts`/`special-meal-plans.ts`) chưa refactor sang
  gọi API async).

## 5. Verify

- Backend: `mvn package` sạch × 2 lần (lần đầu thêm field, lần 2 sau khi revert `staffInvolved`
  + vá JSON format mapper), Flyway không cần chạy gì mới (không migration).
- Curl thật (`ngoth.medical.synthetic`, dựng tạm 2 dòng `kitchen_inspections` test — 1 PASSED
  staffHealthValid=true, 1 FAILED staffHealthValid=false — vì seed gốc rỗng bảng này):
  - `POST /sent-reports/compliance` → `payload.staffHealthValid=false` (đúng — 1/2 phiên
    FALSE), `payload.assessment` lưu đúng nguyên văn tiếng Việt có dấu.
  - `POST /sent-reports/violations` (dùng 1 dòng `violation_records` seed thật có sẵn,
    `VIO-2026-001`) → đúng `totalViolations=1`, không có field `staffInvolved`/`summary`/
    `counts` nào (đã bỏ).
  - `GET /sent-reports?reportType=VIOLATION_SUMMARY` → filter đúng, chỉ trả bản ghi vi phạm.
  - Xác nhận bug JSON date TRƯỚC vá (đọc lại ra `[2026,7,5]`) và SAU vá (đọc lại ra
    `"2026-07-05"`, nhất quán với response CREATE) bằng cách tạo lại bản ghi mới sau khi build
    lại jar.
  - Đã xoá toàn bộ dữ liệu test (`sent_reports`, `kitchen_inspections` mã `KIN-TEST-*`) sau khi
    verify xong.
- Frontend: `npx tsc --noEmit`, `npx eslint` (12 file đã sửa), `npm run audit` sạch toàn dự án
  (seed table 53→52 sau khi xoá `SENT_REPORTS`).

## 6. Còn lại

- `school-config` (resource cuối chưa nối, lệch shape đơn giản hơn — 1 object vs phân trang) —
  chưa làm trong đợt này, để bàn riêng khi cần.
- `processCount`/`staffCount` (phân loại vi phạm theo quy trình/nhân sự, MF11) — Nam đã chọn
  "làm sau, việc riêng" (mục 2 điểm 3) — chưa lên kế hoạch cụ thể.
