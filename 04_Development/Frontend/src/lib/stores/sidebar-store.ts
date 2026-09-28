"use client";

import { create } from "zustand";

/** Trạng thái mở/đóng sidebar trên mobile (drawer). Desktop (lg+) luôn hiển thị. */
interface SidebarState {
  open: boolean;
  toggle: () => void;
  close: () => void;
}

export const useSidebarStore = create<SidebarState>((set) => ({
  open: false,
  toggle: () => set((s) => ({ open: !s.open })),
  close: () => set({ open: false }),
}));
