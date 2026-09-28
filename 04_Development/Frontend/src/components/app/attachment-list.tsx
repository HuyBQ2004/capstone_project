"use client";

/**
 * Danh sách tệp đính kèm THẬT (ảnh/PDF) của một bản ghi — dùng chung cho mọi endpoint
 * dạng `GET /{resource}/{code}/{subPath}` (liệt kê) + `GET /{resource}/{code}/{subPath}/{id}`
 * (tải, có @PreAuthorize — D10-BIS, không phải URL công khai). Đối chiếu
 * `evidence-gallery.tsx` (bản chuyên cho ảnh kiểm thực) — component này KHÔNG giả định
 * tệp luôn là ảnh (PDF kết quả xét nghiệm cũng đi qua đường này) nên chỉ hiện chip tên
 * tệp + link tải, không hiện thumbnail.
 */

import { useEffect, useState } from "react";
import { FileText, Loader2 } from "lucide-react";
import { api } from "@/lib/api/client";
import { useSubDetail } from "@/lib/api/hooks";

type AttachmentSummary = { id: string; fileName: string; sizeBytes: number };

/** Chip tải 1 tệp — dùng khi trang ĐÃ có sẵn metadata (VD `FoodSampleResponse.evidencePhotos`)
 * và chỉ cần link tải, không cần `AttachmentList` tự gọi API liệt kê. */
export function FileDownloadChip({ downloadUrl, name }: { downloadUrl: string; name: string }) {
  const [url, setUrl] = useState<string | null>(null);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    let objectUrl: string | null = null;
    let cancelled = false;
    api
      .get(downloadUrl, { responseType: "blob" })
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
  }, [downloadUrl]);

  return (
    <a
      href={url ?? undefined}
      target="_blank"
      rel="noreferrer"
      title={name}
      className="inline-flex items-center gap-1.5 rounded-md border bg-card px-2.5 py-1.5 text-xs hover:bg-muted"
    >
      {url ? <FileText className="size-3.5" /> : <Loader2 className="size-3.5 animate-spin" />}
      <span className="max-w-40 truncate">{name}</span>
      {failed && <span className="text-destructive">(lỗi)</span>}
    </a>
  );
}

/** `resource`/`code`/`subPath` ghép thành `GET /{resource}/{code}/{subPath}` (liệt kê) và
 * `.../{subPath}/{id}` (tải từng tệp). */
export function AttachmentList({
  resource,
  code,
  subPath,
  emptyLabel = "Chưa có tệp nào được tải lên.",
}: {
  resource: string;
  code: string | undefined;
  subPath: string;
  emptyLabel?: string;
}) {
  const { data: items = [], isLoading } = useSubDetail<AttachmentSummary[]>(resource, code, subPath);

  if (isLoading) {
    return <div className="flex gap-2">{[0, 1].map((i) => <div key={i} className="h-7 w-24 animate-pulse rounded-md bg-muted" />)}</div>;
  }
  if (items.length === 0) {
    return <p className="text-xs text-muted-foreground">{emptyLabel}</p>;
  }
  return (
    <div className="flex flex-wrap gap-1.5">
      {items.map((a) => (
        <FileDownloadChip key={a.id} downloadUrl={`/${resource}/${code}/${subPath}/${a.id}`} name={a.fileName} />
      ))}
    </div>
  );
}
