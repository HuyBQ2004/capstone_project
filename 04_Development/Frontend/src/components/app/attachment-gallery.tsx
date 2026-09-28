"use client";

/**
 * Ảnh/tệp bằng chứng dạng `IncidentDtos.AttachmentResponse` ({id, fileName, sizeBytes}) —
 * dùng cho sự cố (mf11: incident evidence-photos/lab-result-photos, CAPA evidence-photos,
 * source-report evidence-photos). Khác `EvidenceGallery` (mf07, gom theo ownerType) — ở đây
 * KHÔNG có ownerType/uploadedAt, chỉ là danh sách phẳng tải qua endpoint có `@PreAuthorize`
 * (D10-BIS, không phải URL công khai).
 */

import { useEffect, useState } from "react";
import { FileText, ImageOff } from "lucide-react";
import { api } from "@/lib/api/client";
import { useSubDetail } from "@/lib/api/hooks";

type AttachmentSummary = { id: string; fileName: string; sizeBytes: number };

function isImageName(name: string) {
  return /\.(png|jpe?g|gif|webp|bmp)$/i.test(name);
}

function Thumb({
  resource,
  code,
  subpath,
  file,
}: {
  resource: string;
  code: string;
  subpath: string;
  file: AttachmentSummary;
}) {
  const [url, setUrl] = useState<string | null>(null);
  const [failed, setFailed] = useState(false);
  const isImage = isImageName(file.fileName);

  useEffect(() => {
    let objectUrl: string | null = null;
    let cancelled = false;
    api
      .get(`/${resource}/${code}/${subpath}/${file.id}`, { responseType: "blob" })
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
  }, [resource, code, subpath, file.id]);

  return (
    <a
      href={url ?? undefined}
      target="_blank"
      rel="noreferrer"
      className="block overflow-hidden rounded-md border bg-muted/30"
      title={file.fileName}
    >
      {failed ? (
        <div className="flex h-24 w-32 items-center justify-center text-muted-foreground">
          <ImageOff className="size-5" />
        </div>
      ) : !isImage ? (
        <div className="flex h-24 w-32 flex-col items-center justify-center gap-1 text-muted-foreground">
          <FileText className="size-5" />
          <span className="max-w-28 truncate px-1 text-[10px]">{file.fileName}</span>
        </div>
      ) : url ? (
        // eslint-disable-next-line @next/next/no-img-element
        <img src={url} alt={file.fileName} className="h-24 w-32 object-cover" />
      ) : (
        <div className="h-24 w-32 animate-pulse bg-muted" />
      )}
    </a>
  );
}

/** `code` = mã D1 (không phải UUID nội bộ). `subpath` vd: "evidence-photos", "lab-result-photos". */
export function AttachmentGallery({
  resource,
  code,
  subpath,
  emptyLabel = "Chưa có tệp bằng chứng nào được tải lên.",
}: {
  resource: string;
  code: string;
  subpath: string;
  emptyLabel?: string;
}) {
  const { data: items = [], isLoading } = useSubDetail<AttachmentSummary[]>(resource, code, subpath);

  if (isLoading) {
    return <div className="flex gap-2">{[0, 1, 2].map((i) => <div key={i} className="h-24 w-32 animate-pulse rounded-md bg-muted" />)}</div>;
  }
  if (items.length === 0) {
    return <p className="text-sm text-muted-foreground">{emptyLabel}</p>;
  }

  return (
    <div className="flex flex-wrap gap-2">
      {items.map((f) => (
        <Thumb key={f.id} resource={resource} code={code} subpath={subpath} file={f} />
      ))}
    </div>
  );
}
