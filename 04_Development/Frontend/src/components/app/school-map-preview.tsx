"use client";

import dynamic from "next/dynamic";
import { Loader2 } from "lucide-react";

export interface SchoolMapPreviewProps {
  lat: number | null;
  lng: number | null;
  name: string;
  address?: string;
}

export const SchoolMapPreview = dynamic<SchoolMapPreviewProps>(
  () => import("./school-map-preview-impl").then((m) => m.SchoolMapPreviewImpl),
  {
    ssr: false,
    loading: () => (
      <div className="flex h-52 items-center justify-center rounded-lg border bg-muted/40 text-sm text-muted-foreground">
        <Loader2 className="mr-2 size-4 animate-spin" /> Đang tải bản đồ...
      </div>
    ),
  }
);
