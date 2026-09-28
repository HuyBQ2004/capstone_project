"use client";

/**
 * Ảnh bằng chứng THẬT của một phiên kiểm thực ba bước (BR-083) — 10/08/2026.
 *
 * Trước đây `POST /inspections/{code}/evidences/{ownerType}` đã lưu ảnh thật (SHA-256,
 * storage riêng tư) nhưng KHÔNG MÀN NÀO đọc lại được — người xem chi tiết chỉ thấy số đếm
 * (`photoCount`), không có cách nào kiểm ảnh có thật hay đúng nội dung không. Component này
 * gọi `GET /inspections/{code}/evidences` (danh sách) rồi tải từng ảnh qua endpoint tải
 * xuống có `@PreAuthorize` (không phải URL công khai — D10-BIS), hiện dạng lưới.
 *
 * Ảnh gom theo (phiên, LOẠI bước) — `ownerType` SESSION/STEP1_FRESH/STEP1_DRY/STEP2/STEP3 —
 * KHÔNG phân biệt được từng dòng món/lô trong cùng bước (giới hạn kiến trúc hiện tại, xem
 * `EvidenceDtos.EvidenceSummary`). Quyết định 10/08/2026 (Nam): xem ảnh CHUNG theo phiên là
 * đủ cho mục đích xác minh "có làm thật", không cần tách theo dòng ở giai đoạn này.
 *
 * 13/08/2026 (theo Nam) — RIÊNG "kitchen-inspections" (kiểm tra bếp) KHÔNG còn giới hạn ở
 * trên: `GET /kitchen-inspections/{code}/evidences` trả `itemCode` thật cho từng ảnh (backend
 * đã lưu đúng `ChecklistItemResult.resultId` làm owner từ V68, chỉ là API cũ bỏ phí liên kết
 * này). Gom theo `itemCode` khi có, để hiện ảnh đúng theo TỪNG hạng mục thay vì gộp một cục
 * "Kiểm tra bếp".
 */

import { useEffect, useState } from "react";
import { ImageOff } from "lucide-react";
import { api } from "@/lib/api/client";
import { useSubDetail } from "@/lib/api/hooks";
import { formatVnDateTime } from "@/lib/format";

type EvidenceSummary = {
  id: string;
  fileName: string;
  uploadedAt: string;
  /** "inspections" (kiểm thực 3 bước). */
  ownerType?: string;
  /** "kitchen-inspections" (kiểm tra bếp, V68) — mã hạng mục checklist thật. */
  itemCode?: string;
};

const OWNER_LABEL: Record<string, string> = {
  SESSION: "Ảnh chung phiên",
  STEP1_FRESH: "Mẫu 1 — Thực phẩm tươi sống",
  STEP1_DRY: "Mẫu 1 — Thực phẩm bao gói/khô",
  STEP2: "Mẫu 2 — Trong chế biến",
  STEP3: "Mẫu 3 — Trước khi ăn",
  KITCHEN_ITEM: "Kiểm tra bếp",
};

function Thumb({
  resource,
  inspectionCode,
  evidence,
}: {
  resource: string;
  inspectionCode: string;
  evidence: EvidenceSummary;
}) {
  const [url, setUrl] = useState<string | null>(null);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    let objectUrl: string | null = null;
    let cancelled = false;
    api
      .get(`/${resource}/${inspectionCode}/evidences/${evidence.id}`, { responseType: "blob" })
      .then((res) => {
        if (cancelled) return;
        objectUrl = URL.createObjectURL(res.data as Blob);
        setUrl(objectUrl);
      })
      .catch(() => {
        if (!cancelled) setFailed(true);
      });
    return () => {
      cancelled = true;
      if (objectUrl) URL.revokeObjectURL(objectUrl);
    };
  }, [resource, inspectionCode, evidence.id]);

  return (
    <a
      href={url ?? undefined}
      target="_blank"
      rel="noreferrer"
      className="block overflow-hidden rounded-md border bg-muted/30"
      title={`${evidence.fileName} — ${formatVnDateTime(evidence.uploadedAt)}`}
    >
      {failed ? (
        <div className="flex h-24 w-32 items-center justify-center text-muted-foreground">
          <ImageOff className="size-5" />
        </div>
      ) : url ? (
        // eslint-disable-next-line @next/next/no-img-element
        <img src={url} alt={evidence.fileName} className="h-24 w-32 object-cover" />
      ) : (
        <div className="h-24 w-32 animate-pulse bg-muted" />
      )}
    </a>
  );
}

/**
 * `inspectionCode` = mã D1 của phiên (`InspectionSessionResponse.id`/`KitchenInspectionResponse.id`),
 * KHÔNG phải UUID nội bộ. `resource` mặc định "inspections" (kiểm thực 3 bước); truyền
 * "kitchen-inspections" khi dùng cho màn xem lại "Kiểm tra an toàn bếp" (V68, 11/08/2026) —
 * cùng shape API (`GET /{resource}/{code}/evidences[/​{id}]`), chỉ khác tiền tố route.
 */
export function EvidenceGallery({
  inspectionCode,
  resource = "inspections",
  itemLabelOf,
}: {
  inspectionCode: string;
  resource?: string;
  /** "kitchen-inspections" — tra tên hiển thị từ mã hạng mục (VD "CLG-1-1" → "Rửa tay đúng cách"). */
  itemLabelOf?: (code: string) => string;
}) {
  const { data: items = [], isLoading } = useSubDetail<EvidenceSummary[]>(
    resource,
    inspectionCode,
    "evidences"
  );

  if (isLoading) {
    return <div className="flex gap-2">{[0, 1, 2].map((i) => <div key={i} className="h-24 w-32 animate-pulse rounded-md bg-muted" />)}</div>;
  }
  if (items.length === 0) {
    return <p className="text-sm text-muted-foreground">Chưa có ảnh bằng chứng nào được tải lên cho phiên này.</p>;
  }

  const groups = new Map<string, EvidenceSummary[]>();
  for (const e of items) {
    const key = e.itemCode ?? e.ownerType ?? "SESSION";
    const list = groups.get(key) ?? [];
    list.push(e);
    groups.set(key, list);
  }

  return (
    <div className="space-y-3">
      {[...groups.entries()].map(([key, list]) => (
        <div key={key} className="space-y-1.5">
          <p className="text-xs font-medium text-muted-foreground">
            {list[0]?.itemCode ? (itemLabelOf?.(key) ?? key) : (OWNER_LABEL[key] ?? key)} ({list.length})
          </p>
          <div className="flex flex-wrap gap-2">
            {list.map((e) => (
              <Thumb key={e.id} resource={resource} inspectionCode={inspectionCode} evidence={e} />
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}
