"use client";

/**
 * Nguồn THẬT DUY NHẤT cho khối lớp — `GET /grades` (bảng `grades` school-scoped, thêm ở
 * migration V44 thay cho `classrooms.grade` INT và `menus.grade`/`menu_templates.grade`
 * text tự do).
 *
 * Vì sao cần: sau V44 mọi endpoint GHI của MF05 khoá theo `gradeId` (UUID) —
 * `CreateMenuRequest.gradeId` là `@NotBlank`, `GET /menus/diet-coverage` và
 * `/menus/grade-summary` khai `@RequestParam UUID gradeId`. Nhưng FE vẫn gửi NHÃN
 * ("Khối 3") ⇒ tạo thực đơn **luôn 422**, còn hai endpoint đọc kia **luôn 400 và chưa
 * từng hiển thị được gì**. Không màn nào của MF05 gọi `GET /grades` để lấy UUID.
 *
 * ⚠️ `ClassroomResponse.gradeId` là `null` (đo 07/08/2026) — KHÔNG lấy được UUID khối từ
 * `classrooms`, bắt buộc qua đây.
 *
 * ## Quy ước: nhãn là khoá HIỂN THỊ, UUID chỉ dùng ở ranh giới gửi request
 *
 * Nhãn ("Khối 3") đang là khoá của `?grade=` trên URL, của `m.grade === grade`,
 * `gradeEnrollmentOf()`, `dishState()`, `gradeAllergySummary()`… Đổi hết sang UUID là
 * refactor xuyên 6 màn + `mock-data.ts`. Nên: **giữ nhãn ở state/URL, gọi `useGradeId()`
 * ngay trước khi gửi**.
 *
 * Đã đo và xác nhận điều kiện tiên quyết: `grades.name` trả về đúng chuỗi mà
 * `ClassroomResponse.grade` trả về ("Khối 1"/"Khối 2"/"Khối 3"…), nên tra ngược bằng nhãn
 * là an toàn. Dù vậy, ở màn GHI hãy dựng dropdown từ `useGrades()` chứ không từ
 * `gradeLevelsOf(classrooms)` — để mọi nhãn chọn được đều chắc chắn resolve ra UUID.
 */

import { usePlainList } from "@/lib/api/hooks";
import type { GradeDto } from "@/lib/api/types";

/** `id`/`name` narrow required ngay tại ranh giới nạp (springdoc khai mọi field optional). */
export interface Grade {
  id: string;
  name: string;
  sortOrder: number;
  active: boolean;
}

/** `GradeDto` (API thật) → `Grade`. */
export function toGrade(g: GradeDto): Grade {
  return {
    id: g.id ?? "",
    name: g.name ?? "",
    sortOrder: g.sortOrder ?? 0,
    active: g.active ?? true,
  };
}

/**
 * Khối ĐANG DÙNG của trường, đã sắp theo `sortOrder` — dùng dựng dropdown ở màn GHI.
 *
 * `studentCode` (11/08/2026) — BẮT BUỘC truyền khi gọi từ màn Phụ huynh: Parent không có
 * `schoolId` trong JWT, server suy trường từ CON đang chọn (`studentCode`); thiếu tham số
 * này, phụ huynh có con ở ≥2 trường sẽ bị suy nhầm về trường ĐẦU TIÊN theo `student_parents`
 * (không phải trường của con đang xem) — xem `GradeServiceImpl.resolveSchoolId()`. Màn GHI
 * (School Admin/Caterer) không cần truyền — JWT của các vai này đã có `schoolId`.
 */
export function useGrades(studentCode?: string): Grade[] {
  const { data: dtos = [] } = usePlainList<GradeDto>("grades", studentCode ? { studentCode } : {});
  return dtos
    .map(toGrade)
    .filter((g) => g.active && g.id)
    .sort((a, b) => a.sortOrder - b.sortOrder);
}

/**
 * Nhãn khối → UUID, dùng NGAY TRƯỚC khi gửi request.
 *
 * Trả `undefined` khi chưa tải xong hoặc nhãn không khớp khối nào — nơi gọi phải chặn
 * (`enabled: Boolean(gradeId)` cho query, hoặc không cho bấm Lưu). **Đừng gửi request với
 * `gradeId` undefined**: server trả 422/400 và người dùng chỉ thấy "Validation failed".
 *
 * `studentCode` — xem Javadoc `useGrades()`; truyền khi gọi từ màn Phụ huynh.
 */
export function useGradeId(gradeLabel: string | undefined, studentCode?: string): string | undefined {
  const grades = useGrades(studentCode);
  if (!gradeLabel) return undefined;
  return grades.find((g) => g.name === gradeLabel)?.id;
}
