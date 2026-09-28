import { formatVnDate } from "@/lib/format";
import { MEAL_TYPE_LABEL, type InspectionSession } from "@/lib/domain-utils";
import type { InspectionSessionDto } from "@/lib/api/types";

/**
 * Adapter `InspectionSessionDto` (thật) → shape `InspectionSession` (mock) — dùng khi
 * cần truyền phiên kiểm thực THẬT vào các hàm dẫn xuất viết cho mock (`menusOfInspection`,
 * `inspectionForMeal` — mọi hàm này tự convert `date`/`meal` NGƯỢC lại từ dạng mock bằng
 * `isoOfVnDate()`/`mealTypeOfLabel()`). `sampleDishOptionsOfInspection`/`kitchenStaffNames`
 * (mock cũ) đã xoá — `samples/page.tsx` nay đọc thẳng `step2Rows`/`step3Rows` thật.
 *
 * `date` thật là ISO (`s.getDate().toString()`), `meal` thật là enum thô
 * (`s.getMealType().name()` — "LUNCH"/"BREAKFAST"/"SNACK") — khác hẳn mock (`dd/MM/yyyy`,
 * nhãn tiếng Việt "Bữa trưa"). KHÔNG đổi các hàm dùng chung đó — chỉ thích ứng tại chỗ gọi
 * (cùng nguyên tắc `lib/student-adapter.ts`).
 */
export function inspectionDtoToMock(s: InspectionSessionDto): InspectionSession {
  return {
    id: s.id ?? "",
    date: formatVnDate(s.date),
    meal: (MEAL_TYPE_LABEL[(s.meal ?? "LUNCH") as keyof typeof MEAL_TYPE_LABEL] ??
      "Bữa trưa") as InspectionSession["meal"],
    step1: (s.step1 ?? "PENDING") as InspectionSession["step1"],
    step2: (s.step2 ?? "PENDING") as InspectionSession["step2"],
    step3: (s.step3 ?? "PENDING") as InspectionSession["step3"],
    overall: (s.overall ?? "PENDING") as InspectionSession["overall"],
    coreTemp: s.coreTemp,
    inspector: s.inspector ?? "",
    coSigner: s.coSigner,
    /*
     * 13/08/2026 (theo Nam) — bỏ sót field này là lý do bản in (`inspection-sheets.ts`
     * → `sheetHeader()`) luôn hiện chấm chấm "………" ở Tên cơ sở/Địa điểm kiểm tra thay vì
     * tên/địa chỉ trường thật, dù backend đã tự tính đúng `formHeader.facility/location`
     * từ trường của phiên (`InspectionAssembler.toResponse()` — xem ghi chú ở
     * `lib/api/types.ts`) và màn xem phiên (`inspections/[id]/page.tsx`) đọc đúng field
     * này vì nó đọc thẳng DTO gốc, không qua adapter này.
     */
    formHeader: s.formHeader as InspectionSession["formHeader"],
    dish: s.dish,
    dishId: s.dishId,
    evidencePhotos: s.evidencePhotos,
    gps: s.gps,
    staffNotes: s.staffNotes,
    medicalReviewResult: s.medicalReviewResult as InspectionSession["medicalReviewResult"],
    medicalReviewReason: s.medicalReviewReason,
    medicalReviewBy: s.medicalReviewBy,
    medicalReviewAt: s.medicalReviewAt,
    step1Fresh: s.step1Fresh as InspectionSession["step1Fresh"],
    step1Dry: s.step1Dry as InspectionSession["step1Dry"],
    step2Rows: s.step2Rows as InspectionSession["step2Rows"],
    step3Rows: s.step3Rows as InspectionSession["step3Rows"],
  };
}
