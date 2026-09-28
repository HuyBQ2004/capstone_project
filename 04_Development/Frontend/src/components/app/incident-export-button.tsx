"use client";

import { Download } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";

/** Nút xuất báo cáo sự cố (AF1 UC-116) — tách client để dùng trong view server. */
export function IncidentExportButton({ id }: { id: string }) {
  return (
    <Button
      variant="outline"
      onClick={() => toast.success(`Đã xuất báo cáo sự cố ${id} ra PDF (AF1 UC-116)`)}
    >
      <Download /> Xuất báo cáo
    </Button>
  );
}
