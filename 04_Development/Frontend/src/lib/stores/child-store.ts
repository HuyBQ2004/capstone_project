"use client";

/**
 * Zustand store — con đang được phụ huynh xem (Child Switcher ở Topbar).
 * Một phụ huynh có thể có nhiều con học cùng trường; mọi màn hình Parent
 * hiển thị dữ liệu theo con đang chọn.
 *
 * Nối thật 06/08/2026: trước đây `activeChildId` mặc định + toàn bộ danh sách con
 * đọc từ mảng tĩnh `PARENT_CHILDREN` — MỌI tài khoản phụ huynh (bất kể đăng nhập
 * bằng ai) đều thấy đúng 3 con giả giống nhau. `ParentController.studentsOf()`
 * (`GET /parents/{code}/students`, UC-050) đã có sẵn ở backend nhưng chưa FE nào
 * gọi tới — nay `useParentChildren()` gọi đúng endpoint đó bằng mã tài khoản đang
 * đăng nhập (`SessionUser.id`), qua `parentChildOfStudent()` adapter.
 */

import { create } from "zustand";
import type { ParentChild } from "@/lib/domain-utils";
import { usePagedList } from "@/lib/api/hooks";
import type { StudentDto } from "@/lib/api/types";
import { parentChildOfStudent } from "@/lib/student-adapter";
import { useAuthStore } from "@/lib/stores/auth-store";

const EMPTY_CHILD: ParentChild = { studentId: "", name: "—", class: "—", dob: "—", age: 0, school: "—" };

interface ChildState {
  activeChildId: string | null;
  setActiveChild: (studentId: string) => void;
}

export const useChildStore = create<ChildState>((set) => ({
  activeChildId: null,
  setActiveChild: (studentId) => set({ activeChildId: studentId }),
}));

/** Toàn bộ con của phụ huynh đang đăng nhập — nguồn duy nhất cho Child Switcher. */
export function useParentChildren(): ParentChild[] {
  const user = useAuthStore((s) => s.user);
  // Chỉ gọi API khi role là PARENT — các role khác (SYSTEM_ADMIN, SUPPLIER…)
  // không có quan hệ phụ huynh–con, gọi sẽ bị 403 không cần thiết.
  const isParent = user?.role === "parent";
  const { data } = usePagedList<StudentDto>(
    `parents/${user?.id ?? "-"}/students`,
    { pageSize: 50 },
    { enabled: Boolean(user?.id) && isParent },
  );
  return (data?.items ?? []).map((s) => parentChildOfStudent(s, user?.school));
}

/** Hook tiện dụng: trả về object con đang chọn (con đầu tiên nếu chưa chọn/đang tải). */
export function useActiveChild(): ParentChild {
  const children = useParentChildren();
  const activeChildId = useChildStore((s) => s.activeChildId);
  return children.find((c) => c.studentId === activeChildId) ?? children[0] ?? EMPTY_CHILD;
}
