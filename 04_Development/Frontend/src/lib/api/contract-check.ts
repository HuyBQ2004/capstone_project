/**
 * GUARD BIÊN DỊCH — type FE của resource ĐÃ NỐI THẬT phải khớp DTO backend.
 *
 * File này không sinh ra JS nào lúc chạy; nó tồn tại để `tsc` gãy khi backend đổi
 * DTO mà FE chưa theo. Đây là thứ đã THIẾU ở đợt MF03: mọi lệch tên field đều phải
 * dò bằng curl từng endpoint, và cái nguy nhất (`clazz` vs `class`) chỉ lộ ra khi
 * ~20 màn cùng mất cột "Lớp".
 *
 * Thêm một dòng vào đây MỖI KHI thêm resource vào `REAL_RESOURCES`.
 *
 * Cách đọc lỗi: nếu `tsc` báo
 *     Type '"requestedBy"' is not assignable to type 'true'
 * nghĩa là FE đang đọc field `requestedBy` mà DTO backend KHÔNG có — hoặc backend
 * bỏ field đó, hoặc FE đọc nhầm tên. Tên field lệch nằm ngay trong thông báo lỗi.
 */

import type {
  Dish,
  Incident,
  IncidentRecordDraft,
  IncidentSourceReport,
  InspectionSession,
  KitchenInspection,
  SeedMenu,
  ViolationRecord,
} from "@/lib/domain-utils";
// `Api`, `PartnerDetailDto`, `SpecialMealRequestDto` GỠ khỏi import 07/08/2026 — 3 dòng check
// dùng chúng (`_classroomsContract`/`_partnersContract`/`_specialMealsContract`) đã xoá theo
// các interface mock tương ứng; tên vẫn còn trong COMMENT bên dưới nên đừng tưởng còn dùng.
import type {
  ComplianceReportDto,
  DishDto,
  IncidentDto,
  IncidentRecordDto,
  InspectionSessionDto,
  KitchenInspectionDto,
  MenuDto,
  SourceReportDto,
  ViolationDto,
  ReportDefinitionDto,
} from "./types";

/**
 * `true` nếu mọi key của `FE` đều có trong `Dto`; ngược lại trả về CHÍNH tên key
 * bị thiếu (union các string literal) để hiện thẳng trong thông báo lỗi của tsc.
 *
 * Chỉ so TÊN field, không so kiểu — vì `schema.d.ts` để mọi field optional
 * (springdoc không khai `required`), so kiểu chặt sẽ báo nhiễu. Lệch tên mới là
 * lớp lỗi đã thực sự gây hỏng ở MF03.
 */
type MissingOnDto<FE, Dto> = Exclude<keyof FE, keyof Dto> extends never
  ? true
  : Exclude<keyof FE, keyof Dto>;

/* ─── MF01 — IAM (nối thật từ đợt trước) ─── */
// `_usersContract` (UserAccount) xoá 07/08/2026 — `system-admin/users/page.tsx` đã đọc thẳng
// `UserDto` (= `Api<"UserResponse">`), không còn interface FE cũ nào để so khớp field.
// `_staffContract` (StaffMember) xoá 07/08/2026 — staff pages đọc thẳng `StaffDto`.
// `_rolesContract`/`_permissionsContract` (RoleRecord/Permission) xoá 07/08/2026 —
// `system-admin/roles/permissions` đã đọc thẳng `RoleRecordDto`/`PermissionDto`.

/* ─── MF02 — School / Class / Academic Year ─── */
// `_schoolsContract`/`_academicYearsContract` (SchoolRecord/AcademicYear) xoá 07/08/2026 —
// mọi trang đọc thẳng `SchoolDto`/`AcademicYearDto`.

/*
 * `_classroomsContract` xoá 07/08/2026 — interface `Classroom` (mock) đã xoá, 16 nơi dùng đọc
 * thẳng `ClassroomDto`, không còn 2 shape để so khớp.
 *
 * Quyết định mà dòng check này "giữ chỗ" (`Omit<Classroom, "lunchTime">`) NAY ĐÃ RA: `lunchTime`
 * bỏ hẳn ở FE. Backend chưa từng có field đó, và từ 06/08 khi `useTeacherClass()` chuyển sang
 * API thật thì nó luôn `undefined` — 2 màn `teacher/{dashboard,distribution}` hiện "Bữa trưa dự
 * kiến —" vĩnh viễn, tức UI CHẾT chứ không phải "chờ backend". Đã gỡ đoạn hiển thị đó. Muốn
 * khôi phục tính năng thì BE thêm field vào `ClassroomResponse` TRƯỚC, đừng khai lại ở FE.
 */

/* ─── MF03 — special-meals (nối thật 02/08/2026) ─── */
// `_specialMealsContract` xoá 07/08/2026 — interface `SpecialMealRequest` (mock) đã xoá,
// cả 11 nơi dùng đọc thẳng `SpecialMealRequestDto`, không còn 2 shape để so khớp.

/*
 * `students`/`health-history` (nối thật 05/08/2026) KHÔNG có dòng check `MissingOnDto`
 * ở đây — cùng lý do `allergy-declarations`/`chronic-conditions`/`announcements`/
 * `notification-preferences`: shape lệch đủ sâu (`StudentResponse` thiếu 4 field lâm
 * sàng — tách riêng `/students/{code}/health-file`, HealthFileDto; `StudentDietSummaryResponse`
 * rút gọn cho Service Caterers không có `name`/`dob`/PH) nên TOÀN BỘ ~26 màn tiêu thụ đã
 * đổi hẳn sang đọc `StudentDto`/`StudentDietSummaryDto`/`HealthFileDto`/`HealthHistoryEventDto`
 * (`lib/api/types.ts`) trực tiếp — không còn nơi nào dùng interface `Student` (mock-data.ts)
 * làm kiểu resource nữa để so khớp field. `Student` (mock) vẫn còn tồn tại làm shape trung
 * gian cho `lib/special-meal-plans.ts`/`dietCoverageGaps`/`dietGroupsNeededByGrade` — xem
 * `lib/student-adapter.ts` (`studentDtoToMock`) cho điểm chuyển đổi DUY NHẤT.
 */

/* ─── MF04 — Supplier & Partner Management (nối thật 03/08/2026) ─── */

/*
 * `_partnersContract` xoá 07/08/2026 — interface `Partner` (mock) đã xoá.
 *
 * Lỗ hổng mà chính dòng check này tự khai báo ("so với bản ĐẦY ĐỦ hơn nên KHÔNG bắt được việc
 * đọc nhầm field chỉ-có-ở-Detail từ kết quả `usePagedList` — tự rà bằng tay") NAY ĐÃ ĐÓNG bằng
 * cách tách hẳn 2 type theo ngữ cảnh: 9 điểm `usePagedList` dùng `PartnerDto`, 2 điểm
 * `useDetail` dùng `PartnerDetailDto`. `tsc` giờ chặn thẳng việc đọc `taxId`/`documents` từ một
 * hàng danh sách — không còn phải "tự rà bằng tay" nữa.
 */
// `_supplierDocumentsContract` (SupplierDocument) xoá 07/08/2026 — mọi trang đọc thẳng
// `DocumentDto`.
// `_deliveryPersonsContract` (SupplierDeliveryPerson) xoá 07/08/2026 —
// `supplier/delivery-staff/page.tsx` đọc thẳng `DeliveryPersonDto`.
// `_partnershipApplicationsContract` (PartnershipApplication) xoá 07/08/2026 —
// `supplier/partnerships/page.tsx` đọc thẳng `ApplicationDto`.
/*
 * `supplier-partnerships` KHÔNG có dòng check: `SupplierPartnership` (mock-data.ts)
 * dùng `since`, nhưng `PartnershipResponse` thật trả `startDate` — khác tên, không
 * chỉ thiếu field. `supplier/partnerships/page.tsx` đã đổi sang đọc thẳng
 * `Api<"PartnershipResponse">` (`PartnershipDto`, xem `types.ts`) thay vì ép qua
 * interface FE cũ — không còn gì để check ở đây.
 */

/* ─── MF05 — Menu & Diet Management ─── */

/**
 * `SeedMenu` (= `MenuItem` + `blocksData`/`servings`) có `createdAt`, nhưng
 * `MenuResponse` KHÔNG trả field này (Menu.java không có cột tương ứng — chỉ có
 * `BaseEntity.createdAt` ẩn, không map ra DTO). Không trang menu nào đọc
 * `.createdAt` của một Menu (đã grep — chỉ `medical-staff/incidents/*` dùng field
 * cùng tên trên object khác) nên Omit tạm, không phải nợ đang chờ quyết định.
 */
export const _menusContract: MissingOnDto<Omit<SeedMenu, "createdAt">, MenuDto> = true;
export const _dishesContract: MissingOnDto<Dish, DishDto> = true;
// MenuTemplate migrate hẳn sang Pick<MenuTemplateDto,...> ở `lib/menu-template-adapter.ts`
// (phiên 5) — tự đảm bảo khớp DTO bằng cấu trúc, không cần dòng contract riêng nữa.

/* ─── MF06 — Ingredient & Inventory Management (nối thật 03/08/2026, 2/8 resource) ─── */

/**
 * `traceability` KHÔNG có dòng check: chưa trang nào tiêu thụ resource này (chuỗi
 * `"traceability"` trong FE hiện chỉ là report-slug/column-id).
 *
 * `preparation-records` — bỏ hẳn 06/08/2026 (trùng Bước 2 Kiểm thực ba bước, không có căn
 * cứ pháp lý riêng) — xem `PREPARATION_RECORDS` trong `mock-data.ts` (vẫn giữ, chỉ là dữ
 * liệu dẫn xuất nội bộ cho bản đồ truy xuất, KHÔNG còn endpoint backend nào tương ứng).
 */

/* ─── MF07 — Food Safety Inspection (nối thật 03/08/2026, 2/4 resource) ─── */

/**
 * `inspections`/`kitchen-inspections` KHÔNG có trong `REAL_RESOURCES` — 2 check dưới đây
 * KHÔNG phải "sắp migrate", mà chốt SỚM: shape ĐỌC (field name) đã khớp DTO thật (giữ xanh
 * làm cột mốc), nhưng đường GHI của 2 resource này lệch KIẾN TRÚC (không phải lệch field) —
 * chi tiết đầy đủ ở `real-resources.ts` (`inspections`: không có PUT/PATCH, chỉ 5 action
 * POST, màn nhập liệu chính đang gọi `useUpdate` tuỳ ý field; `kitchen-inspections`: tính
 * năng "biên bản ký" của `.../record/page.tsx` chưa có backend). Bật switch cần giải quyết
 * phần đó TRƯỚC, contract-check không bắt được lớp lỗi này (chỉ so tên field GET).
 */
export const _inspectionsContract: MissingOnDto<InspectionSession, InspectionSessionDto> = true;

/**
 * `KitchenInspection` trừ 7 field của tính năng "Biên bản kiểm tra bếp" (BR-211, in ra
 * ký tay rồi tải ảnh bản ký — `medical-staff/conduct-inspection/[id]/record/page.tsx`):
 * `type` (đã `@deprecated` từ phía FE, không còn nhập/hiển thị ở đâu) và 6 field
 * `record*` (`recordCaterersRep`/`recordConfirmedByMedicalStaffAt`/`recordConfirmedByMedicalStaffBy`/
 * `recordConfirmedByCaterersAt`/`recordPdfExportedAt`/`recordPdfVersion`/`recordSignedFiles`).
 *
 * Backend `KitchenInspectionResponse` KHÔNG có field nào trong nhóm `record*` — đây không
 * phải lệch tên, mà là CẢ TÍNH NĂNG "in biên bản, ký tay, tải ảnh bản ký" chưa có cột DB
 * lẫn endpoint (không giống `confirmedBy`/`confirmedAt`, đã bổ sung vào DTO 03/08/2026 vì
 * dữ liệu đã sẵn có ở entity, chỉ thiếu map ra response).
 *
 * ⇒ resource `kitchen-inspections` migrate được cho MỌI trang ĐỌC (dashboard/compliance/
 * history/list — chỉ mất `type` vốn đã bỏ hiển thị), nhưng `.../record/page.tsx` vẫn phải
 * giữ MOCK: nếu cắt mock, nút "Tải ảnh/scan bản đã ký" sẽ gọi `PUT /kitchen-inspections/{id}`
 * — Jackson bỏ qua 6 field lạ (D12, hành vi thật đã xác nhận ở `InspectionDerivedFieldRejectionIT`)
 * ⇒ trả 200 nhưng KHÔNG lưu gì, nút bấm "thành công" nhưng biên bản không bao giờ khoá được.
 * Bỏ `Omit<>` khi có ai xây tính năng ký-biên-bản thật cho MF07 (giống
 * `receiving_evidence_documents` + `POST /receiving-sessions/reject-documents` Huy đã làm cho MF06).
 */
export const _kitchenInspectionsContract: MissingOnDto<
  Omit<
    KitchenInspection,
    | "type"
    | "recordCaterersRep"
    | "recordConfirmedByMedicalStaffAt"
    | "recordConfirmedByMedicalStaffBy"
    | "recordConfirmedByCaterersAt"
    | "recordPdfExportedAt"
    | "recordPdfVersion"
    | "recordSignedFiles"
  >,
  KitchenInspectionDto
> = true;
// `_checklistGroupsContract` (ChecklistGroup) xoá 07/08/2026 — checklist/page.tsx đọc thẳng
// `ChecklistGroupDto`/`ChecklistItemDto`.
// `_safetySignoffsContract` (SafetySignoff) xoá 07/08/2026 — compliance/page.tsx đọc thẳng
// `SafetySignoffDto`.

/* ─── MF08 — Food Sample Retention (nối thật TRỌN VẸN 05/08/2026, 3/3 resource) ───
 * `samples`/`sample-lab-requests` KHÔNG có dòng `MissingOnDto` — cùng lý do
 * `students`/`inspections` (MF03/MF07): shape lệch đủ sâu (mock `FoodSample`/
 * `SampleLabRequest` có nhiều field tự do — `labDestination` tên gõ tay,
 * `sealPhotoFiles`/`handoverRecordFiles` mảng file, `cancelledAt/By/Reason` của luồng
 * BR-215 Deprecated…) nên mọi màn tiêu thụ đã đổi hẳn sang đọc `FoodSampleDto`/
 * `SampleLabRequestDto` trực tiếp, không còn dùng 2 interface mock làm kiểu resource.
 */
// `_receivingAuthoritiesContract` (ReceivingAuthority) xoá 07/08/2026 — đã dead code,
// `medical-staff/samples/page.tsx` đọc thẳng `ReceivingAuthorityDto`.

/* ─── MF09 — Meal Serving & Distribution (nối thật TRỌN VẸN 05/08/2026, 5/5 resource) ───
 * `student-distributions` (teacher/distribution[/{id}], teacher/dashboard,
 * service-caterers/meal-service) — không có dòng check: mock `StudentDistribution` interface
 * phong phú hơn `DistributionDto` rất nhiều (field tự do `assignedMeal`/`specialMeal`/
 * `dietTag`/`allergens` không tồn tại thật) — mọi màn tiêu thụ đã đổi hẳn sang đọc
 * `DistributionDto` trực tiếp + endpoint MỚI `GET /planned-dishes` (xem
 * `04_Meal_Substitution_Portion_Pipeline_Design.md` Phase 1) cho preview suất dự kiến, cùng lý
 * do `students`/`inspections`/`samples` ở các khối trên.
 * `allergy-alerts` (class-wide) — không có dòng check vì chưa trang nào gọi.
 * `meal-history`/`special-meal-records` — không có dòng check vì `MealHistoryEntryDto` là
 * projection dẫn xuất, KHÔNG có mock type tương đương để đối chiếu (chưa từng có
 * `MealHistory`/`SpecialMealRecord` interface trong `mock-data.ts` — 2 màn tiêu thụ
 * trước đó đọc mảng tĩnh `MEAL_HISTORY`/lọc `student-distributions`, không qua type
 * resource riêng).
 */
// `_mealRecordsContract` (TeacherMealRecord) xoá 07/08/2026 — teacher/meal-records,
// teacher/students/[id] đọc thẳng `MealRecordDto`.

/* ─── MF10 — Feedback & Announcement Management (nối thật TRỌN VẸN 05/08/2026, 4/4 resource) ───
 * `announcements` KHÔNG có dòng check ở đây: shape lệch mock (`Announcement` ở
 * `mock-data.ts`) đủ sâu (list item KHÔNG có `content`, `audienceCodes` là field mới,
 * `revisionCount` thay `revisions[]`) nên 6 màn tiêu thụ đã đổi hẳn sang đọc
 * `AnnouncementDto`/`AnnouncementListItemDto` (`lib/api/types.ts`) trực tiếp — cùng cách
 * `supplier/partnerships` đã làm cho `PartnershipResponse` (xem chú thích MF04 trên) — không
 * còn interface FE cũ nào để so khớp field.
 *
 * `notification-preferences` cũng vậy: `components/app/user-menu.tsx` (dialog "Cài đặt thông
 * báo" ở Topbar, dùng chung mọi role — trang riêng `service-caterers/notification-settings`
 * đã gộp vào đây 14/08/2026) đọc thẳng `NotificationPreferenceDto`, không qua interface
 * trung gian nào.
 *
 * `feedbacks` (nối thật cùng đợt) — KHÔNG có dòng check, cùng lý do: mock `Feedback` phẳng
 * (một shape cho mọi vai) trong khi thật `oneOf` theo vai (`FeedbackOwnerListItemDto`/
 * `FeedbackAnonymizedListItemDto`, D11/BR-145) + field mới hẳn (`mealSessionId`, `scope`) —
 * 4 màn tiêu thụ đã đổi sang đọc DTO thật trực tiếp, không còn dùng interface `Feedback` cũ.
 *
 * `notifications` (nối thật cùng đợt) — KHÔNG có dòng check: mock `UserNotification` (taxonomy
 * 4 giá trị `ALLERGY|FOOD_SAFETY|NEWS|SYSTEM`, trước D6) lệch hẳn `NotificationResponse` thật
 * (7 giá trị D6) — `notification-bell.tsx` đã đổi hẳn sang đọc `NotificationDto`, không còn
 * dùng interface `UserNotification`/`TYPE_META` cũ. `lib/notify.ts` (`useNotify()`, `POST`
 * giả lập — không có backend nào để gọi, ADR-M10-4) đã XOÁ HẲN, bỏ khỏi cả 8 màn từng gọi.
 */

/* ─── MF11 — Incident & Emergency Management (Bước 3 chạy 04/08/2026, KHÔNG resource
 * nào migrate được đợt này — xem lý do đầy đủ ở `real-resources.ts`). 5 dòng dưới đây
 * CHỈ để dò field GET lúc phân tích (đã xác nhận: 3/5 resource chỉ lệch RENAME, không
 * lệch field thật), giữ lại làm cột mốc "shape đọc đã khớp" — cùng cách MF07 giữ
 * `_inspectionsContract` dù chưa bật switch. KHÔNG suy ra từ dòng xanh rằng resource
 * đã sẵn sàng bật — chặn thật nằm ở LUỒNG GHI, contract-check không bắt được lớp đó.
 */
export const _sourceReportsContract: MissingOnDto<
  Omit<IncidentSourceReport, "reportedBy" | "affectedStudents">,
  SourceReportDto
> = true;
export const _incidentRecordsContract: MissingOnDto<
  Omit<IncidentRecordDraft, "createdBy" | "sourceReportIds" | "updatedAt">,
  IncidentRecordDto
> = true;
export const _incidentsContract: MissingOnDto<
  Omit<
    Incident,
    | "reportedBy" | "sourceReportIds" | "suspectedDish" | "mealTime" | "firstAid"
    | "investigationEvidence" | "investigationRootCause" | "investigationRootCauseStatus"
    | "suspectedMeal" | "suspectedDishIds" | "assessmentDraftSaved" | "investigationEvidenceFinal"
    | "investigationMeasuresDraft" | "quarantineReleaseLog"
  >,
  IncidentDto
> = true;
// `_correctiveActionsContract` (CorrectiveAction) xoá 07/08/2026 — đã dead code,
// mọi trang `corrective-actions` đọc thẳng `CorrectiveActionDto`.
export const _violationsContract: MissingOnDto<
  Omit<
    ViolationRecord,
    | "type" | "evidencePhotos" | "witness" | "evidenceFiles" | "recordedBy" | "staffName"
    | "staffPosition" | "disciplinaryLevel" | "handledBy" | "caterersRepresentative"
    | "confirmedByMedicalStaffAt" | "confirmedByMedicalStaffBy" | "confirmedByCaterersAt"
    | "signedRecordPhoto" | "signedRecordFiles"
  >,
  ViolationDto
> = true;

/* ─── MF12 — AI & Supporting Services (Bước 3, 04/08/2026, 4/5 resource) ───
 * `sent-reports` KHÔNG có dòng check — chưa migrate, shape lệch SÂU (không phải rename),
 * xem `real-resources.ts`.
 */
// `_auditLogsContract` (AuditLog) xoá 07/08/2026 — audit-logs/page.tsx đọc thẳng `AuditLogDto`.
// `_errorLogsContract` (ErrorLog) xoá 07/08/2026 — error-logs/dashboard đọc thẳng `ErrorLogDto`.
// `_integrationsContract` (ExternalIntegration) xoá 07/08/2026 — interface đã là dead code,
// `system-admin/{integrations,system-status}` đọc thẳng `IntegrationDto`.
// `_securityPoliciesContract` (SecurityPolicy) xoá 07/08/2026 —
// system-admin/security-policies + (auth)/reset-password đọc thẳng `SecurityPolicyDto`.

/* ─── MF13 — Reporting & Analytics (Bước 3, 05/08/2026, 2/3 resource) ───
 * `reports/{slug}/rows` KHÔNG có dòng check — 7 shape khác nhau (D11), không có 1
 * `Api<"...">` cố định để so; `report-schedules` chưa migrate (không có màn FE nào
 * tiêu thụ UC-173) — xem `real-resources.ts`.
 */
export const _complianceReportContract: MissingOnDto<
  { generatedAt: unknown; periodLabel: unknown; modules: unknown },
  ComplianceReportDto
> = true;
export const _reportDefinitionContract: MissingOnDto<
  { slug: unknown; title: unknown; group: unknown; exportable: unknown },
  ReportDefinitionDto
> = true;
