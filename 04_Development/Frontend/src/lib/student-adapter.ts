import type { ParentChild, Student } from "@/lib/domain-utils";
import type { StudentDto } from "@/lib/api/types";
import { formatVnDate } from "@/lib/format";

/**
 * Adapter `StudentDto` (thật) → shape `Student` (mock) — dùng khi cần truyền dữ liệu học
 * sinh THẬT vào các hàm dẫn xuất suất ăn đặc biệt (`lib/special-meal-plans.ts`), vốn viết
 * cho mock `Student` với field KHÔNG optional. KHÔNG đổi các hàm dùng chung đó — chỉ thích
 * ứng tại chỗ gọi, cùng cách đã làm với `service-caterers/inspections/[id]` (MF06,
 * 05/08/2026: "viết adapter tại chỗ gọi, không đổi hàm mock dùng chung").
 *
 * `allergySeverity` CỐ Ý bỏ qua — enum thật (`MILD`/`MODERATE`/`SEVERE`) khác casing với
 * union mock (`"Mild"|"Moderate"|"Severe"`), và không hàm nào trong `special-meal-plans.ts`
 * đọc field này (chỉ `id`/`name`/`class`/`allergies`/`specialDiet` được dùng).
 */
export function studentDtoToMock(s: StudentDto): Student {
  return {
    id: s.id ?? "",
    name: s.name ?? "",
    class: s.class ?? "",
    allergies: s.allergies ?? [],
    specialDiet: s.specialDiet,
    healthStatus: (s.healthStatus as Student["healthStatus"]) ?? "PENDING",
    parent: s.parent ?? "",
  };
}

function ageOf(dobIso: string | undefined): number {
  if (!dobIso) return 0;
  const dob = new Date(dobIso);
  if (Number.isNaN(dob.getTime())) return 0;
  const now = new Date();
  let age = now.getFullYear() - dob.getFullYear();
  if (now.getMonth() < dob.getMonth() || (now.getMonth() === dob.getMonth() && now.getDate() < dob.getDate())) {
    age--;
  }
  return age;
}

/**
 * Adapter `StudentDto` (thật, từ `GET /parents/{code}/students`) → `ParentChild` (shape Child
 * Switcher đang dùng). `age`/`school` không có trên `StudentResponse` — `age` tính từ `dob`
 * (ISO), `school` lấy từ tên trường của chính phiên phụ huynh (một PH chỉ thuộc một trường).
 */
export function parentChildOfStudent(s: StudentDto, schoolName: string | undefined): ParentChild {
  return {
    studentId: s.id ?? "",
    name: s.name ?? "",
    class: s.class ?? "",
    dob: formatVnDate(s.dob),
    age: ageOf(s.dob),
    school: schoolName ?? "—",
  };
}
