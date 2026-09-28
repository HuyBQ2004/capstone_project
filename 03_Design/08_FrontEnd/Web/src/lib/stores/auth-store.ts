"use client";

/**
 * Zustand store — phiên đăng nhập mock (BR-004/BR-005 mô phỏng phía UI).
 * Login page chọn vai trò → set session; Topbar/AppShell đọc từ store.
 */

import { create } from "zustand";
import type { RoleKey } from "@/lib/roles";
import { CLASSROOMS, type Classroom } from "@/lib/mock-data";

export interface SessionUser {
  /**
   * Khoá tới `UserAccount.id` (review/teacher — `review_teacher_data_linkage_plan.md`
   * Đợt 0). Optional vì `system-admin` demo không có bản ghi `UserAccount` seed
   * tương ứng — không bịa id cho tài khoản không tồn tại trong `mock-data.ts`.
   */
  id?: string;
  name: string;
  email: string;
  role: RoleKey;
  school?: string;
}

/** Tài khoản demo cho từng vai trò (khớp USER_ACCOUNTS trong mock-data). */
export const DEMO_USERS: Record<RoleKey, SessionUser> = {
  "system-admin": { name: "Quản trị SFMS", email: "admin@sfms.vn", role: "system-admin" },
  "school-admin": { id: "U-0001", name: "Phạm Quốc Toản", email: "toanpq@dtd.edu.vn", role: "school-admin", school: "TH Đoàn Thị Điểm" },
  "medical-staff": { id: "U-0002", name: "Ngô Thị Hà", email: "hant@dtd.edu.vn", role: "medical-staff", school: "TH Đoàn Thị Điểm" },
  "service-caterers": { id: "U-0003", name: "Trần Văn Hùng", email: "hungtv@anbinhcatering.vn", role: "service-caterers", school: "TH Đoàn Thị Điểm" },
  teacher: { id: "U-0004", name: "Nguyễn Thu Trang", email: "trangnt@dtd.edu.vn", role: "teacher", school: "TH Đoàn Thị Điểm" },
  parent: { id: "U-0005", name: "Nguyễn Văn Bình", email: "binhnv.parent@gmail.com", role: "parent", school: "TH Đoàn Thị Điểm" },
  supplier: { id: "U-0006", name: "Lý Thái Sơn", email: "sonlt@greenfarm.vn", role: "supplier" },
};

interface AuthState {
  user: SessionUser | null;
  login: (role: RoleKey) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  login: (role) => set({ user: DEMO_USERS[role] }),
  logout: () => set({ user: null }),
}));

/**
 * Lớp chủ nhiệm của giáo viên đang đăng nhập — nối theo `teacherId` (khoá ổn
 * định), KHÔNG so tên hiển thị (review/teacher — Đợt 0).
 *
 * Fix 27/07/2026: `useAuthStore` KHÔNG persist qua reload (khác `useChildStore`
 * — `child-store.ts` — vốn có default `activeChildId` sẵn nên Parent luôn có
 * dữ liệu để xem). Vào thẳng URL `/teacher/...` hoặc F5 giữa phiên demo (chưa
 * qua `/login` chọn vai trò) thì `user` là `null`, khiến mọi trang Teacher lọc
 * ra rỗng nếu hàm này trả `undefined`. Nay mặc định về đúng tài khoản giáo
 * viên demo (`DEMO_USERS.teacher`) khi chưa có phiên — có đăng nhập thật vẫn
 * ưu tiên đúng id đang đăng nhập.
 */
export function useTeacherClass(): Classroom | undefined {
  const user = useAuthStore((s) => s.user);
  const teacherId = user?.id ?? DEMO_USERS.teacher.id;
  return CLASSROOMS.find((c) => c.teacherId === teacherId);
}
