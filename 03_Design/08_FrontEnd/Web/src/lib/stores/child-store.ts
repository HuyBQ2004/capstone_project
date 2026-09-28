"use client";

/**
 * Zustand store — con đang được phụ huynh xem (Child Switcher ở Topbar).
 * Một phụ huynh có thể có nhiều con học cùng trường; mọi màn hình Parent
 * hiển thị dữ liệu theo con đang chọn.
 */

import { create } from "zustand";
import { PARENT_CHILDREN, type ParentChild } from "@/lib/mock-data";

interface ChildState {
  activeChildId: string;
  setActiveChild: (studentId: string) => void;
}

export const useChildStore = create<ChildState>((set) => ({
  activeChildId: PARENT_CHILDREN[0].studentId,
  setActiveChild: (studentId) => set({ activeChildId: studentId }),
}));

/** Hook tiện dụng: trả về object con đang chọn. */
export function useActiveChild(): ParentChild {
  const activeChildId = useChildStore((s) => s.activeChildId);
  return (
    PARENT_CHILDREN.find((c) => c.studentId === activeChildId) ??
    PARENT_CHILDREN[0]
  );
}
