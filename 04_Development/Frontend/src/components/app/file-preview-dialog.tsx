"use client";

import { FileText } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";

interface FilePreviewInfo {
  url: string;
  type: string;
  name: string;
}

export function FilePreviewDialog({
  file,
  onClose,
}: {
  file: FilePreviewInfo | null;
  onClose: () => void;
}) {
  if (!file) return null;
  const isPdf = file.type.includes("pdf") || file.name.toLowerCase().endsWith(".pdf");
  const isImage = file.type.includes("image") || /\.(jpg|jpeg|png)$/i.test(file.name);

  return (
    <Dialog open onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="max-w-4xl h-[80vh] flex flex-col">
        <DialogHeader>
          <DialogTitle>{file.name}</DialogTitle>
          <DialogDescription>Xem trực tiếp tài liệu hồ sơ</DialogDescription>
        </DialogHeader>
        <div className="flex-1 min-h-0 bg-muted rounded-lg overflow-hidden flex items-center justify-center relative p-2 border">
          {isPdf ? (
            <iframe src={file.url} className="size-full border-none rounded-lg bg-white" title="PDF Preview" />
          ) : isImage ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img src={file.url} alt="Preview" className="max-w-full max-h-full object-contain rounded-lg shadow-sm" />
          ) : (
            <div className="text-center p-6 bg-background rounded-lg border shadow-sm">
              <FileText className="mx-auto size-12 text-muted-foreground opacity-60 mb-2" />
              <p className="text-sm font-semibold mb-2">Định dạng file không hỗ trợ xem trực tiếp.</p>
              <a href={file.url} download={file.name} className="inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-primary text-primary-foreground hover:bg-primary/90 h-9 px-4 py-2">
                Tải về máy
              </a>
            </div>
          )}
        </div>
        <DialogFooter className="mt-2 shrink-0">
          <Button variant="outline" onClick={onClose}>Đóng</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
