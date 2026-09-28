/**
 * TYPE LẤY TỪ BACKEND THẬT — dùng cho MỌI module nối API từ MF04 trở đi.
 *
 * ─── Vì sao có file này ───
 *
 * Đến MF03, FE và BE cùng được sinh độc lập từ TDS rồi mới ghép: FE lấy
 * `mock-data.ts` làm sự thật, BE lấy DTO Java làm sự thật, và không ai so hai cái
 * đó cho tới lúc nối. Kết quả đo được ở đợt MF03 (02/08/2026) — mọi lỗi dưới đây
 * đều phải tìm bằng cách gọi thử từng endpoint, không cái nào bị trình biên dịch bắt:
 *
 *   · `StudentResponse` trả key `clazz`, FE đọc `Student.class`  → ~20 màn mất cột "Lớp"
 *   · `allergySeverity` trả `"SEVERE"`, FE so `=== "Severe"`      → mất cảnh báo dị ứng nặng
 *   · `AllergyDeclaration.proofFile` ở FE vs `proofFileId` ở BE
 *   · `date` trả ISO `2026-08-02`, FE seed `02/08/2026`
 *   · `SpecialMealRequestResponse` thiếu 7 field FE đang render
 *   · `PATCH {"class":"4A"}` trả 200 mà không lưu gì (Jackson bỏ field lạ)
 *
 * Với type sinh từ OpenAPI, tất cả những cái đó thành LỖI BIÊN DỊCH.
 *
 * ─── Quy trình cho MF04 → MF12 ───
 *
 *   1. Sinh backend trước (controller + DTO + `@Schema`).
 *   2. Chạy backend, rồi `npm run gen:api` → cập nhật `schema.d.ts`.
 *   3. Sinh/viết frontend, LẤY TYPE TỪ ĐÂY — không tự khai `interface` trong
 *      `mock-data.ts` nữa (chỗ đó chỉ còn giữ seed cho module chưa có backend).
 *
 * `schema.d.ts` được commit vào repo để người làm FE không cần backend chạy mới
 * build được; chạy lại `gen:api` mỗi khi backend đổi DTO.
 *
 * ⚠️ Field trong `schema.d.ts` là optional (`?`) vì OpenAPI không khai `required` —
 * đó là hạn chế của springdoc, không phải backend cho phép null. Cần chắc chắn thì
 * thêm `@Schema(requiredMode = REQUIRED)` ở DTO Java rồi sinh lại.
 */

import type { components } from "./schema";

/** Toàn bộ schema backend, tra theo tên DTO Java. */
export type ApiSchemas = components["schemas"];

/** Rút gọn: `Api<"SpecialMealRequestResponse">` thay cho chuỗi index dài. */
export type Api<K extends keyof ApiSchemas> = ApiSchemas[K];

/* ─── Alias cho các DTO đã nối thật (thêm dần khi migrate resource mới) ─── */

/**
 * MF01 — `GET|POST /users`, `PATCH /users/{code}/status` (IAM, nối thật trước MF03).
 * `role`/`status` sinh sẵn literal union từ enum OpenAPI — khớp `RoleKey` (`lib/roles.ts`).
 */
export type UserDto = Api<"UserResponse">;
export type RoleRecordDto = Api<"RoleResponse">;
export type PermissionDto = Api<"PermissionResponse">;
/** `GET /staff` — CÙNG DTO với `/users` (`UserController`/`StaffController` share `UserResponse`). */
export type StaffDto = Api<"UserResponse">;

/** MF02 — `GET|PATCH /schools`, `GET|POST /academic-years` (nối thật trước MF03). */
export type SchoolDto = Api<"SchoolResponse">;
export type AcademicYearDto = Api<"AcademicYearResponse">;

/**
 * MF02 — `GET /classrooms` (+ `POST`, `PATCH /{id}`, `PATCH /{id}/teacher`). Migrate từ
 * interface `Classroom` khai tay 07/08/2026.
 *
 * 2 điểm khác mock cũ, đọc trước khi dùng:
 * - **KHÔNG có `lunchTime`.** Backend chưa từng có field này (đo bằng curl 02/08/2026); mock
 *   khai nó với đúng 1 giá trị seed cho lớp 3A. Từ 06/08 khi `useTeacherClass()` chuyển sang
 *   API thật thì nó LUÔN `undefined` ⇒ 2 màn `teacher/{dashboard,distribution}` hiện "Bữa trưa
 *   dự kiến —" vĩnh viễn. Phiên 6 gỡ hẳn đoạn UI chết đó. Muốn khôi phục thì BE phải thêm field
 *   trước — đừng khai lại ở FE.
 * - `grade` là **NHÃN** hiển thị ("Khối 3", = `Grade.name` sau migration V44), KHÔNG phải số
 *   1–12 và cũng KHÔNG phải `gradeId` (UUID) — muốn PATCH thì tra UUID qua `GET /grades`.
 *   Đừng tự thêm tiền tố `"Khối "` lần nữa (lỗi thật của phiên 5).
 */
export type ClassroomDto = Api<"ClassroomResponse">;

/** MF03 — `GET|POST /special-meals`, `POST /special-meals/{id}/{approve,reject}`. */
export type SpecialMealRequestDto = Api<"SpecialMealRequestResponse">;
export type SubmitSpecialMealDto = Api<"SubmitSpecialMealRequest">;

/**
 * MF03 — `GET /students` trả HAI shape tuỳ vai trò (chốt 2026-08-02):
 * Service Caterers nhận bản rút gọn KHÔNG có `name`/`dob`/phụ huynh.
 * Spec khai bằng `oneOf`, nên FE phải xử lý cả hai — đây là thứ trước kia
 * chỉ phát hiện được bằng cách đăng nhập đúng vai trò rồi gọi thử.
 */
export type StudentDto = Api<"StudentResponse">;
export type StudentDietSummaryDto = Api<"StudentDietSummaryResponse">;
/** `POST /students`. `classroomId` — KHÔNG @JsonProperty("class") ở CREATE, khác PATCH/GET. */
export type CreateStudentDto = Api<"CreateStudentRequest">;
export type ParentInfoDto = Api<"ParentInfoRequest">;
/** `POST /students/import/validate` (pha 1/2, ADR-M03-6) → xem `POST .../commit` cho pha 2/2. */
export type ImportPreviewDto = Api<"ImportPreviewResponse">;
export type ImportRowResultDto = Api<"ImportRowResult">;
export type ImportResultDto = Api<"ImportResultResponse">;
/**
 * `GET|PATCH /students/{code}/health-file` — hồ sơ LÂM SÀNG (UC-051/094), tách khỏi
 * `StudentResponse` (C8/COND-009): CHỈ 4 field `medicalConditions`/`medications`/
 * `emergencyContact`/`emergencyInstructions` ghi được — field lạ (allergies/specialDiet/
 * healthStatus/allergySeverity) → 400 STU-094 (backend đọc raw JSON map để chặn).
 */
export type HealthFileDto = Api<"HealthFileResponse">;
/**
 * Backend nhận `PATCH .../health-file` như `Map<String,Object>` thô (không phải DTO
 * đánh dấu `@RequestBody`), để tự chặn field lạ trước khi bind (C8/COND-009 — Jackson
 * mặc định BỎ QUA field lạ khi bind thẳng record, nên springdoc không sinh schema
 * component nào cho request body này) — khai tay, không derive từ `Api<...>`.
 */
export type UpdateHealthFileDto = {
  medicalConditions?: string;
  medications?: string;
  emergencyContact?: string;
  emergencyInstructions?: string;
};

/**
 * MF03 — `GET|POST /allergy-declarations`, `POST /{id}/{verify,request-info}` (nối thật
 * 04/08/2026). `id` là UUID thô (KHÔNG remap D1 — declaration không có mã nghiệp vụ riêng,
 * chỉ là bản ghi phụ thuộc `studentId`). `severity` là enum thật — xem `lib/health-severity.ts`
 * cho bảng map hiển thị tiếng Việt.
 */
export type AllergyDeclarationDto = Api<"AllergyDeclarationResponse">;
export type SubmitAllergyDto = Api<"SubmitAllergyRequest">;

/** MF03 — `GET|POST /chronic-conditions`, `POST /{id}/{verify,request-info}` (nối thật 04/08/2026). */
export type ChronicConditionDto = Api<"ChronicConditionResponse">;
export type SubmitChronicConditionDto = Api<"SubmitChronicConditionRequest">;

/**
 * MF03 — `GET /students/{code}/health-record` (UC-050). Chưa có hồ sơ ⇒ **`204 No Content`**
 * (chốt 07/08/2026, đo bằng curl). Trước đó đã đi qua 2 hợp đồng SAI: chuỗi JSON `"null"`, rồi
 * `200` + body rỗng (`ResponseEntity.ok(null)` — Spring bỏ hẳn body chứ không serialize `null`).
 * Cả 3 dạng đều làm axios trả `""`, nên `?? undefined` KHÔNG bắt được — phải kiểm
 * `typeof === "object"`, xem `parent/health/page.tsx`.
 */
export type HealthRecordDto = Api<"HealthRecordResponse">;

/**
 * MF03 — `POST /students/{code}/health-documents` (thêm 04/08/2026). `id` trả về dùng
 * THẲNG làm `proofFileId` khi khai `allergy-declarations`/`chronic-conditions` mức SEVERE.
 */
export type HealthDocumentDto = Api<"HealthDocumentResponse">;

/**
 * MF03 — `GET /health-history?studentId=<mã>` (nối thật 05/08/2026). `type` là enum thật
 * KHÔNG dấu (`KHAI_BAO`/`XAC_MINH`/...) — xem `lib/health-history-type.ts` cho bảng map
 * hiển thị tiếng Việt. Sổ append-only (BR-172): không có `POST`/`PUT`/`PATCH`/`DELETE`.
 */
export type HealthHistoryEventDto = Api<"HealthHistoryEventResponse">;

/**
 * MF04 — `GET /partners` trả `PartnerResponse` (rút gọn); `GET /partners/{code}`
 * và `GET /partners/me` trả `PartnerDetailResponse` (đủ taxId/address/contact/
 * documents). Hai shape khác nhau — đọc field chỉ có ở Detail (taxId, address,
 * documents…) từ danh sách phân trang sẽ ra `undefined` dù compile qua.
 */
export type PartnerDto = Api<"PartnerResponse">;
export type PartnerDetailDto = Api<"PartnerDetailResponse">;
export type PartnerActionDto = Api<"PartnerActionResponse">;
export type UploadDocumentDto = Api<"UploadDocumentRequest">;
export type DocumentDto = Api<"DocumentResponse">;
export type DeliveryPersonDto = Api<"DeliveryPersonResponse">;
export type ApplicationDto = Api<"ApplicationResponse">;
export type SubmitApplicationDto = Api<"SubmitApplicationRequest">;
export type PartnershipDto = Api<"PartnershipResponse">;

/**
 * MF05 — `GET /menus` trả HAI shape tuỳ vai trò (D11/M05-D08): Caterer/SchoolAdmin/
 * Medical/SysAdmin nhận `MenuResponse` (đủ giá + chữ ký duyệt), Parent/Teacher nhận
 * `MenuPublicResponse` (rút gọn, chỉ bản ghi PUBLISHED). `MenuDto` dùng bản đủ field
 * cho contract-check vì nó là tập cha.
 */
export type MenuDto = Api<"MenuResponse">;
export type MenuPublicDto = Api<"MenuPublicResponse">;
export type DishDto = Api<"DishResponse">;
export type MenuTemplateDto = Api<"MenuTemplateResponse">;

/**
 * MF05 — MỚI 05/08/2026 (`05_Menu_Grade_Diet_Coverage_Dish_Media_Design.md`): cảnh báo dị
 * ứng/chế độ ăn theo khối (`grade-summary`, dùng khi lập Thực đơn mẫu) và checklist "cần menu
 * cho nhóm nào" của đúng (khối, ngày, buổi) (`diet-coverage`, BR-065 — dùng TRƯỚC khi submit()
 * chặn cứng nếu thiếu).
 */
export type AllergenCountDto = Api<"AllergenCount">;
export type DietGroupCountDto = Api<"DietGroupCount">;
export type GradeSummaryDto = Api<"GradeSummaryResponse">;
export type DietCoverageItemDto = Api<"DietCoverageItem">;
export type DietCoverageDto = Api<"DietCoverageResponse">;
/**
 * `GET /diet-groups` (nối thật 06/08/2026) — danh mục nhóm chế độ ăn đặc biệt TOÀN HỆ THỐNG,
 * nguồn sự thật DUY NHẤT cho mọi màn chọn nhóm (Thực đơn mẫu, Lịch thực đơn tuần). KHÔNG dùng
 * hằng số `DIET_GROUPS` khai tay trong `mock-data.ts` cho việc CHỌN/LỌC — đó là dữ liệu KHÔNG
 * do Y tế duyệt, có thể thiếu nhóm thật (VD thiếu "Không gluten" — phát hiện 06/08/2026).
 */
export type DietGroupDto = Api<"DietGroupResponse">;

/**
 * `GET /measure-units` (nối thật 08/08/2026, backlog D5) — danh mục đơn vị đo TOÀN HỆ THỐNG.
 * Nguồn sự thật DUY NHẤT cho mọi dropdown chọn đơn vị; KHÔNG khai lại danh sách đơn vị hay
 * bảng hệ số quy đổi trong code client (`OFF_CATALOG_UNIT_OPTIONS`/`UNIT_FACTOR` cũ chỉ có
 * 10/4 dòng trong khi DB có 13 — thêm một đơn vị mới là FE tính sai IM LẶNG).
 */
export type MeasureUnitDto = Api<"MeasureUnitResponse">;

/**
 * MF06 — 6 resource còn lại nối thật đợt 05/08/2026 (xem
 * `07_implement/03_FE_Full_Wiring_Master_Plan.md`).
 */
export type DishBatchesDto = Api<"DishBatchesResponse">;
export type InventoryBatchDto = Api<"InventoryBatchResponse">;
export type IssueSlipDto = Api<"IssueSlipResponse">;
export type IssuePlanDto = Api<"IssuePlanResponse">;
export type IssuePlanLineDto = Api<"IssuePlanLine">;
export type InventoryTransactionDto = Api<"InventoryTransactionResponse">;
export type DeliveryDto = Api<"DeliveryResponse">;
export type PurchaseRequestDto = Api<"PurchaseRequestResponse">;
export type PurchaseRequestItemDto = Api<"ItemResponse">;
export type SupplierBatchDto = Api<"SupplierBatchResponse">;
export type ReceivingSessionDto = Api<"ReceivingSessionResponse">;

/**
 * MF07 — Kiểm thực ba bước / Kiểm tra bếp (nối thật 03/08/2026). Bốn resource
 * `inspections`/`kitchen-inspections`/`checklist-groups`/`safety-signoffs`, không `oneOf`
 * (D11 "không áp dụng" — BR-099 lọc row-level, xem `TDS-MF07` §2.4 M07-D13 +
 * `InspectionRoleFieldParityIT`).
 *
 * ⚠️ `checklist-groups` GET trả THẲNG mảng `ChecklistGroupResponse[]`, KHÔNG bọc
 * `PagedResponseChecklistGroupResponse` (danh mục nhỏ theo trường, không phân trang —
 * cùng khuôn `permissions` của MF01). Dùng `usePlainList`, không phải `usePagedList`.
 */
export type InspectionSessionDto = Api<"InspectionSessionResponse">;
export type KitchenInspectionDto = Api<"KitchenInspectionResponse">;
export type ChecklistGroupDto = Api<"ChecklistGroupResponse">;
export type ChecklistItemDto = Api<"ChecklistItemResponse">;
export type SafetySignoffDto = Api<"SafetySignoffResponse">;

/**
 * `inspections`/`kitchen-inspections` (nối thật 05/08/2026) — resource CHỈ có action
 * endpoint (`POST`), KHÔNG có PUT/PATCH `/{code}` nào (`InspectionController`/
 * `KitchenInspectionController`). `SubmitStepRequest.formHeader` KHÔNG được server dùng
 * để lưu gì — `InspectionAssembler.toResponse()` LUÔN tự tính lại `formHeader` từ
 * `schoolName`/`inspector`/`decidedAt` (server truth, không nhận override từ client) —
 * form "Thông tin mẫu biểu" ở FE vì vậy đổi sang CHỈ ĐỌC.
 */
export type OpenSessionDto = Api<"OpenSessionRequest">;
export type SubmitStepDto = Api<"SubmitStepRequest">;
export type CoSignDto = Api<"CoSignRequest">;
export type MedicalReviewDto = Api<"MedicalReviewRequest">;
export type ServingGateDto = Api<"ServingGateResponse">;
/** `POST /inspections/{code}/evidences[/{ownerType}]` — ảnh bằng chứng BR-083. */
export type InspectionEvidenceDto = Api<"EvidenceResponse">;
/** `SubmitKitchenInspectionRequest.score` BỊ SERVER BỎ QUA — luôn tự tính lại (CF-26). */
export type SubmitKitchenInspectionDto = Api<"SubmitKitchenInspectionRequest">;

/**
 * MF08 — Food Sample Retention (nối thật 03/08/2026, 1/3 resource — xem
 * `real-resources.ts` cho lý do `samples`/`sample-lab-requests` còn mock).
 * `receiving-authorities` là danh mục toàn cục (không school-scoped, M08-D03),
 * field khớp 1:1 với `ReceivingAuthority` của `mock-data.ts` (đối chiếu curl
 * thật 03/08/2026: id/name/type/contact/address/isActive/note, enum
 * `type` cùng giá trị `LAB`/`AUTHORITY`).
 */
export type ReceivingAuthorityDto = Api<"ReceivingAuthorityResponse">;

/**
 * MF08 — `samples`/`sample-lab-requests` (nối thật 05/08/2026, MF08 giờ 3/3). `register()`/
 * `handOver()` là multipart (`payload` JSON part + file part(s)) — dùng `useUploadMutation`
 * kiểu cũ không hợp vì cần validate + đọc lại response điển hình `useCreate`; gọi `api.post`
 * trực tiếp với `FormData` (cùng mẫu `jsonPart()` trong `hooks.ts`).
 */
export type FoodSampleDto = Api<"FoodSampleResponse">;
export type RegisterSampleDto = Api<"RegisterSampleRequest">;
export type UpdateStorageDto = Api<"UpdateStorageRequest">;
export type DisposeSampleDto = Api<"DisposeSampleRequest">;
export type IncidentLinkDto = Api<"IncidentLinkRequest">;
export type Mau4LabelDto = Api<"Mau4LabelResponse">;
export type SampleLabRequestDto = Api<"SampleLabRequestResponse">;
export type HandOverSamplesDto = Api<"HandOverSamplesRequest">;

/**
 * MF08 — `sample-label-reservations` (in tem TRƯỚC cho nhiều ngày, thêm 10/08/2026, backend
 * mới — controller CHƯA chạy trên server đang deploy nên `npm run gen:api` chưa sinh được type
 * cho nó). Khai TAY tạm thời, khớp 1:1 field/kiểu với
 * `SampleLabelReservationDtos.{BulkCreateRequest,ReservationItem,ReservationResponse,
 * ReservationLabelResponse}` phía Java — XOÁ khối này và đổi lại `Api<"...">` ngay khi backend
 * lên server thật và `gen:api` chạy lại được (không được để hai định nghĩa cùng tồn tại lâu,
 * dễ lệch — xem lời dặn đầu file này).
 */
export interface SampleLabelReservationItemDto {
  collectDate: string;
  mealType: string;
  dishId: string;
}
export interface BulkCreateReservationDto {
  items: SampleLabelReservationItemDto[];
}
export interface SampleLabelReservationDto {
  id?: string;
  dishId?: string;
  dish?: string;
  mealType?: string;
  collectDate?: string;
  status?: string;
}
export interface ReservationLabelDto {
  id?: string;
  facility?: string;
  dishName?: string;
  mealType?: string;
  collectDate?: string;
}

/**
 * MF09 — Meal Serving & Distribution (nối thật 03/08/2026, 1/4 resource — xem
 * `real-resources.ts`). `meal-records` là resource duy nhất migrate đợt này:
 * verb khớp sẵn (`PATCH /{code}`, `POST /batch`), 1 role (TEACHER, không `oneOf`),
 * chỉ thiếu `studentId` — đã bổ sung vào `MealRecordResponse` (D1, dùng
 * `ReferenceResolver.studentCode()`, cùng khuôn các resource khác).
 */
export type MealRecordDto = Api<"MealRecordResponse">;

/**
 * MF09 — `meal-history`/`special-meal-records` (nối thật 05/08/2026). Cả hai là
 * projection GET-only dẫn xuất từ `student_distributions ⋈ serving_dishes ⋈
 * meal_consumption_records` (ADR-M09-2) — KHÔNG bảng riêng, KHÔNG PUT/PATCH/DELETE.
 * Lồng dưới `/students/{code}/...` (chủ MF03) nên gọi qua `usePagedList`/`useDetail`
 * với resource string chứa cả path phụ, VD `usePagedList<MealHistoryEntryDto>(
 * \`students/${code}/meal-history\`, …)`. `consumption` là `null` khi chưa có
 * `MealConsumptionRecord` (server KHÔNG trả "PENDING" — field vắng hẳn).
 */
export type MealHistoryEntryDto = Api<"MealHistoryEntryResponse">;
export type ServingDishViewDto = Api<"ServingDishView">;

/**
 * MF09 — `student-distributions` (nối thật 05/08/2026). `StudentDistributionController`
 * KHÔNG có PUT/PATCH — chỉ 4 action POST (`/serve`, `/confirm`, `/revoke`, `/error-report`) +
 * GET (`oneOf` theo vai, D11: Service Caterers nhận `DistributionCatererDto` rút gọn, không PII).
 * `POST /confirm` chỉ cần `distributionIds` — server tự tính `servedDishes` (ADR-M09-1, dùng
 * lại đúng luật ở `PlannedDishesDto`), KHÔNG cần FE gửi món.
 * `GET /planned-dishes` (04_Meal_Substitution_Portion_Pipeline_Design.md Phase 1) — xem trước
 * suất dự kiến của học sinh còn PENDING, GET-only, KHÔNG ghi gì, dùng lại chính xác luật thay
 * món của `serve()` nên không lệch kết quả thật khi phát.
 */
export type DistributionDto = Api<"DistributionResponse">;
export type DistributionCatererDto = Api<"DistributionCatererResponse">;
export type DistributionSummaryDto = Api<"DistributionSummaryResponse">;
export type ConfirmBatchDto = Api<"ConfirmBatchRequest">;
export type ErrorReportDto = Api<"ErrorReportRequest">;
export type PlannedDishRowDto = Api<"PlannedDishRow">;
export type PlannedDishSummaryDto = Api<"PlannedDishSummary">;
export type PlannedDishesDto = Api<"PlannedDishesResponse">;

/**
 * MF06 — `class-meal-deliveries` (bếp→lớp, MỚI 05/08/2026 — Phase 2 mục (1)+(4) của
 * `04_Meal_Substitution_Portion_Pipeline_Design.md`). KHÁC `deliveries` (NCC→bếp, đã có từ
 * trước). Chỉ theo dõi TRẠNG THÁI giao/nhận theo (ngày, bữa, lớp) — KHÔNG có breakdown món/suất
 * trong response, gọi riêng `student-distributions/planned-dishes?date=&class=&mealType=`
 * (MF09) để lấy đúng số suất từng món (kể cả món thay thế) cho lớp đó.
 */
export type ClassMealDeliveryDto = Api<"ClassMealDeliveryResponse">;
export type CreateDeliveryDto = Api<"CreateDeliveryRequest">;

/**
 * MF10 — Feedback & Announcement Management (nối thật 04/08/2026, 2/4 resource —
 * xem `real-resources.ts`). `announcements`/`notification-preferences` migrate đợt
 * này; `feedbacks`/`notifications` còn mock (lý do chi tiết ở `real-resources.ts`).
 *
 * `AnnouncementAdminListItem`/`AnnouncementAudienceListItem` KHÔNG có `content` —
 * springdoc chỉ trả field liệt kê, danh sách/preview không tóm tắt nội dung được
 * (khác `Announcement` cũ ở `mock-data.ts`, vốn giữ `content` đầy đủ trên cả list
 * item). `AnnouncementListItemDto` dùng cho `usePagedList` (oneOf); `AnnouncementDto`
 * (đủ field, có `content`/`revisions`) dùng cho `useDetail`.
 */
export type AnnouncementDto = Api<"AnnouncementDetailResponse">;
export type AnnouncementListItemDto = Api<"AnnouncementAdminListItem"> | Api<"AnnouncementAudienceListItem">;
export type CreateAnnouncementDto = Api<"CreateAnnouncementRequest">;
export type UpdateAnnouncementDto = Api<"UpdateAnnouncementRequest">;

/** MF10 — `GET|PUT /notification-preferences/me` (UC-088, BR-158b). */
export type NotificationPreferenceDto = Api<"NotificationPreferenceResponse">;
export type UpdatePreferenceDto = Api<"UpdatePreferenceRequest">;

/**
 * MF10 — `notifications` (nối thật 05/08/2026, MF10 giờ 4/4 TRỌN VẸN). `GET /notifications`
 * đã tự lọc theo VAI + tính `read` theo user đang đăng nhập ở SERVER
 * (`NotificationServiceImpl.listForCurrentUser()`) — FE không cần lọc `audience`/
 * `recipientRoles` nữa. `NotificationController` KHÔNG có `POST` (ADR-M10-4 — thông báo là hệ
 * quả SERVER tự phát qua `NotificationPort` từ luồng nghiệp vụ khác, không phải hành động
 * người dùng bấm nút) — `lib/notify.ts` (`useNotify()`, `POST` giả lập) đã BỊ XOÁ HẲN.
 */
export type NotificationDto = Api<"NotificationResponse">;

/**
 * MF10 — `feedbacks` (nối thật 05/08/2026). `GET /feedbacks` trả `oneOf` theo vai (D11):
 * Parent/Teacher (chủ sở hữu) → `FeedbackOwnerListItemDto` (có `studentId`/`student`);
 * School Admin/Service Caterers → `FeedbackAnonymizedListItemDto` (KHÔNG PII, BR-145).
 * `GET /feedbacks/eligible-meals?studentId=` (BR-139) LUÔN trả `dishes: []` (giới hạn MVP ghi
 * trong Javadoc `FeedbackServiceImpl.eligibleMeals()` — MF09 chưa expose tra món theo phiên qua
 * `ReferenceResolver`) — muốn danh sách món thật của 1 phiên phải cross-reference sang
 * `GET /students/{code}/meal-history` (đã nối thật, MF09) theo `id` (=`mealSessionId`), lấy
 * `dishes[]` ở đó. BR-140 (rating<3 bắt buộc tag) VÀ BR-142 (từ khoá bệnh lý → thông báo Y tế)
 * đều tính ở SERVER (`illnessKeywordDetector`/`notificationPort.publish()` best-effort sau khi
 * lưu) — FE KHÔNG cần tự gọi `useNotify()` nữa như bản mock cũ (2 màn submit đã bỏ).
 */
export type FeedbackOwnerListItemDto = Api<"FeedbackOwnerListItem">;
export type FeedbackAnonymizedListItemDto = Api<"FeedbackAnonymizedListItem">;
export type FeedbackOwnerDto = Api<"FeedbackOwnerResponse">;
export type SubmitFeedbackDto = Api<"SubmitFeedbackRequest">;
export type UpdateFeedbackDto = Api<"UpdateFeedbackRequest">;
export type EligibleMealDto = Api<"EligibleMealResponse">;
export type FeedbackWeeklyAggregateDto = Api<"FeedbackWeeklyAggregate">;

/**
 * MF11 — Incident & Emergency Management (Bước 3, 04/08/2026). `GET /incidents`/
 * `/{code}` trả BA shape tuỳ vai (D11/M11-D15): `IncidentResponse` (Medical Staff/
 * School Admin/System Admin), `IncidentCaterersResponse` (Service Caterers, PDPD/C14
 * — cắt dữ liệu sức khoẻ), `IncidentTeacherResponse` (Teacher, own-class). `IncidentDto`
 * dùng bản đủ field cho contract-check (tập cha).
 */
export type SourceReportDto = Api<"SourceReportResponse">;
export type CreateSourceReportDto = Api<"CreateSourceReportRequest">;
export type IncidentRecordDto = Api<"IncidentRecordResponse">;
export type CreateRecordDto = Api<"CreateRecordRequest">;
export type OpenInvestigationDto = Api<"OpenInvestigationRequest">;
export type IncidentDto = Api<"IncidentResponse">;
export type IncidentCaterersDto = Api<"IncidentCaterersResponse">;
export type IncidentTeacherDto = Api<"IncidentTeacherResponse">;
export type IncidentEventDto = Api<"IncidentEventResponse">;
export type ClosureReadinessDto = Api<"ClosureReadinessResponse">;
export type EvidenceDto = Api<"EvidenceDto">;
export type KnowledgeDocumentDto = Api<"KnowledgeDocumentResponse">;
export type CreateKnowledgeDocumentDto = Api<"CreateKnowledgeDocumentRequest">;
export type SuspectedDishCandidateDto = Api<"SuspectedDishCandidateResponse">;
export type TraceabilityMapDto = Api<"TraceabilityMapResponse">;
export type BatchTraceDto = Api<"BatchTrace">;
export type CorrectiveActionDto = Api<"CorrectiveActionResponse">;
export type ViolationDto = Api<"ViolationResponse">;
export type ViolationFileDto = Api<"ViolationFileDto">;
/** M11-D19 — `GET /violations/caterer-representatives` (đại diện Nhà thầu, không phải đối tác). */
export type CatererRepDto = Api<"UserBrief">;

/**
 * MF12 — AI & Supporting Services (Bước 3, 04/08/2026, 4/5 resource — `sent-reports`
 * chưa migrate, xem `real-resources.ts`).
 */
export type AuditLogDto = Api<"AuditLogResponse">;
export type ErrorLogDto = Api<"ErrorLogSummary">;
/** `GET /security-policies` — 1 object trần (`useSingle`), KHÔNG `updatedAt`/`updatedBy`. */
export type SecurityPolicyDto = Api<"SecurityPolicyResponse">;
export type UpdateSecurityPolicyDto = Api<"UpdateSecurityPolicyRequest">;
/** `GET /integrations` — `List<>` trần (`usePlainList`), KHÔNG `PagedResponse`. */
export type IntegrationDto = Api<"IntegrationResponse">;
export type SaveIntegrationDto = Api<"SaveIntegrationRequest">;
export type ConnectionTestResultDto = Api<"ConnectionTestResult">;

/**
 * MF13 — Reporting & Analytics (Bước 3, 05/08/2026, 2/3 resource — `report-schedules`
 * chưa migrate, không có màn FE nào tiêu thụ UC-173, xem `real-resources.ts`).
 *
 * `GET /reports/{slug}/rows` KHÔNG có 1 DTO cố định — `PagedResponse<Map<String,Object>>`,
 * 7 shape khác nhau theo whitelist BR-181 (`ReportRowProjection` phía backend), nên KHÔNG
 * check được ở đây (không có `Api<"...">` tương ứng); dò field bằng cách đọc thẳng
 * `ReportRowProjection.WHITELIST`.
 */
export type ComplianceReportDto = Api<"ComplianceSummaryResponse">;
export type ReportDefinitionDto = Api<"ReportDefinitionResponse">;
export type ReportExportDto = Api<"ReportExportResponse">;

/**
 * MF12 — `sent-reports` (nối thật 05/08/2026, xem
 * `07_implement/06_Sent_Reports_Redesign_Design.md`). `payload` là JSONB snapshot NGUYÊN VĂN
 * (ADR-M12-05) — shape THEO reportType, generated schema chỉ khai `{[key:string]:unknown}` nên
 * không tự check được; đọc qua `toComplianceDoc()`/`toViolationDoc()`
 * (`components/app/report-document.tsx`) thay vì truy cập field trực tiếp.
 */
export type SentReportDto = Api<"SentReportResponse">;
export type SendComplianceReportDto = Api<"SendComplianceReportRequest">;
export type SendViolationReportDto = Api<"SendViolationReportRequest">;

/**
 * MF06 — `ingredients` (nối thật 06/08/2026). KHÔNG có field `allergens` (khác
 * `CatalogIngredient` mock cũ trong `mock-data.ts`) — dò dị ứng theo nguyên liệu vẫn qua
 * `resolveIngredientAllergens()` (heuristic theo tên), CHƯA migrate, xem chú thích tại đó.
 */
export type IngredientDto = Api<"IngredientResponse">;

/**
 * MF06 — `supplier-products` (nối thật 06/08/2026, `SupplierProductController`). Bảng đã có
 * từ trước nhưng chưa controller nào expose — KHÔNG có field `brand`/`minimumOrderQty` (khác
 * `SupplierProduct` mock cũ, hai field đó chưa màn nào tiêu thụ nên không thêm vào DTO thật).
 */
export type SupplierProductDto = Api<"SupplierProductResponse">;

/**
 * MF06 — `GET /supplier-products/sourcing?ingredientIds=<CSV mã>` (thêm 07/08/2026, BR-076).
 * Endpoint RIÊNG cho panel "Nhà cung cấp nguyên liệu" màn Phụ huynh: trả tên NCC rẻ nhất còn
 * hợp tác (APPROVED) cho từng nguyên liệu, **KHÔNG có `unitPrice`** — khác `SupplierProductDto`
 * ở trên (list gốc có giá, KHÔNG cấp quyền PARENT và không nên mở). Trả THẲNG mảng, không bọc
 * `Paged<T>` ⇒ đọc bằng `usePlainList`, không phải `usePagedList`. Nguyên liệu không tra được
 * mã trong danh mục chuẩn bị BỎ QUA (không có phần tử trả về) — nơi gọi phải tra theo
 * `ingredientId`, đừng khớp theo thứ tự/chỉ số.
 */
export type IngredientSourcingDto = Api<"IngredientSourcingResponse">;

/**
 * MF04 — `partnerships/{code}/catalog-items` + `catalog-change-requests`. PROPOSED — chưa có
 * TDS §8 chính thức, xem `Proposal-Partnership-Catalog-Contract.md`.
 */
/**
 * MF05 — `GET /menus/{code}/allergy-conflicts` (BR-063). Trả `{conflicts: [...]}` với MỖI DÒNG là
 * một cặp (món × tác nhân): `dishName`, `allergen`, `affectedStudentCount`, `resolvedBySubstitute`.
 * Endpoint có từ lâu nhưng tới 13/08/2026 chưa màn nào gọi — các màn duyệt tự tính lại ở client
 * bằng cách tải toàn bộ sổ học sinh + khai báo dị ứng về trình duyệt.
 */
export type AllergyConflictListDto = Api<"AllergyConflictListResponse">;

export type CatalogItemDto = Api<"CatalogItemResponse">;
export type ChangeRequestDto = Api<"ChangeRequestResponse">;

/**
 * MF11 — `GET /violations/override-abuse-report` (UC-183, BR-190, ADR-M11-6 — dẫn xuất
 * runtime, không bảng riêng). `week` là tuần ISO ("2026-W27"), KHÁC nhãn tiếng Việt
 * ("Tuần 27 (29/06–05/07)") của `OverrideAbuseRow` mock cũ. Gộp theo ĐƠN VỊ nhà thầu
 * (`caterersRepresentativeLabel`) — model vi phạm đã chuyển từ cấp nhân viên sang cấp đơn vị
 * (`ViolationRecord.staffName` cũ nay @Deprecated), không còn field `context`/`severity`/
 * `violationId` theo từng vụ như mock — chỉ đếm số lần vượt ngưỡng.
 */
export type OverrideAbuseRowDto = Api<"OverrideAbuseRow">;

/**
 * `GET|POST|PATCH|DELETE /grades` — Danh mục khối, school-scoped qua JWT (không cần
 * `schoolId` truyền tay):
 *   GET  → `GradeResponse[]` (mảng trần, dùng `usePlainList`, KHÔNG `Paged<T>`)
 *   POST body `{name, sortOrder}` → 201 `GradeResponse`
 *   PATCH `/{id}` body `{name?, sortOrder?, active?}` → 200 `GradeResponse`
 *   DELETE `/{id}` → 204 (soft-delete, set `active=false`)
 *
 * 07/08/2026: đổi từ khai TAY sang `Api<"GradeResponse">` — backend đã sinh xong schema
 * (`npm run gen:api`), đối chiếu 4 field khớp tên 1:1 (`id`/`name`/`sortOrder`/`active`).
 * Khác biệt duy nhất: DTO thật khai MỌI field optional (springdoc), còn bản khai tay cũ để
 * required — nơi dùng phải `?? ""`/`?? 0` cho đúng thực tế wire.
 */
export type GradeDto = Api<"GradeResponse">;
