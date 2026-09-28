"use client";

import dynamic from "next/dynamic";
import { Loader2 } from "lucide-react";

/**
 * Leaflet đụng `window` ngay lúc module load ⇒ SSR/build sẽ crash nếu import thẳng
 * `location-picker-map.tsx`. `ssr: false` bắt Next chỉ render nó ở client.
 */
export const LocationPicker = dynamic(
  () => import("./location-picker-map").then((m) => m.LocationPickerMap),
  {
    ssr: false,
    loading: () => (
      <div className="flex h-64 items-center justify-center rounded-md border text-muted-foreground">
        <Loader2 className="size-5 animate-spin" />
      </div>
    ),
  }
);
