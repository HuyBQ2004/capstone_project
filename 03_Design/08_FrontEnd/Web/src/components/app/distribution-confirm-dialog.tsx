"use client";

/**
 * 10_DistributionConfirmation.md — Dialog xác nhận phân phát (UC-016 bước 4-5).
 * BR-120: lưu timestamp xác nhận · BR-130: chặn xác nhận trùng (MSG-021) ·
 * MSG-045: xác nhận thành công. Dùng chung cho Distribution List (Confirm All)
 * và Distribution Detail (Confirm Served).
 */

import { Clock } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";

export function nowTimestamp() {
  const d = new Date();
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${pad(d.getHours())}:${pad(d.getMinutes())} ${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()}`;
}

interface DistributionConfirmDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  /** "Xác nhận đã phục vụ [Tên học sinh]?" hoặc "Xác nhận cả lớp?" */
  title: string;
  description?: string;
  pending?: boolean;
  onConfirm: () => void;
}

export function DistributionConfirmDialog({
  open,
  onOpenChange,
  title,
  description,
  pending,
  onConfirm,
}: DistributionConfirmDialogProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          <DialogDescription>
            {description ??
              "Hệ thống chặn xác nhận trùng trong cùng phiên phân phát (BR-130, MSG-021)."}
          </DialogDescription>
        </DialogHeader>

        {/* Timestamp Display (BR-120) — tự động, không chỉnh sửa */}
        <div className="flex items-center gap-2 rounded-lg border bg-muted/40 px-3 py-2 text-sm">
          <Clock className="size-4 text-muted-foreground" />
          <span className="text-muted-foreground">Thời điểm xác nhận (BR-120):</span>
          <span className="font-medium">{nowTimestamp()}</span>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} disabled={pending}>
            Hủy
          </Button>
          <Button onClick={onConfirm} disabled={pending}>
            {pending ? "Đang lưu…" : "Xác nhận đã phục vụ"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
