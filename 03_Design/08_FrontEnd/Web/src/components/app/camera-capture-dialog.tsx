"use client";

/**
 * CameraCaptureDialog — chụp ảnh bằng chứng TRỰC TIẾP từ camera (không cho
 * upload ảnh có sẵn) — nhận xét Minh 17/07/2026: lô hàng bị từ chối phải có
 * ảnh chụp làm chứng theo TỪNG LÔ, chụp tại chỗ để đảm bảo tính xác thực
 * (BR-072 — timestamp/GPS gắn lúc chụp).
 * Web mở webcam qua getUserMedia rồi chụp khung hình vào canvas; nếu trình
 * duyệt chặn camera → cho chụp mock (ảnh placeholder, ghi chú rõ) để demo.
 */

import { useCallback, useEffect, useRef, useState } from "react";
import { Camera, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Alert } from "@/components/ui/alert";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";

interface CameraCaptureDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  /** Đối tượng đang chụp bằng chứng (VD "Lô BATCH-GF-4411 — Thịt lợn nạc vai"). */
  subject: string;
  /** Ảnh đã chụp trước đó (mở lại dialog vẫn giữ). */
  photos: string[];
  /** Gọi khi bấm Lưu — trả toàn bộ ảnh (dataURL) đã chụp. */
  onSave: (photos: string[]) => void;
}

/** Ảnh placeholder khi không có camera (demo mock, vẫn là "chụp" — không upload). */
function mockShot(subject: string): string {
  const canvas = document.createElement("canvas");
  canvas.width = 480;
  canvas.height = 320;
  const ctx = canvas.getContext("2d")!;
  ctx.fillStyle = "#334155";
  ctx.fillRect(0, 0, canvas.width, canvas.height);
  ctx.fillStyle = "#f1f5f9";
  ctx.font = "16px sans-serif";
  ctx.fillText("ẢNH MOCK (không có camera)", 24, 140);
  ctx.fillText(subject.slice(0, 40), 24, 170);
  ctx.fillText(new Date().toLocaleString("vi-VN"), 24, 200);
  return canvas.toDataURL("image/jpeg", 0.8);
}

export function CameraCaptureDialog({
  open,
  onOpenChange,
  subject,
  photos,
  onSave,
}: CameraCaptureDialogProps) {
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const streamRef = useRef<MediaStream | null>(null);
  const [cameraState, setCameraState] = useState<"starting" | "ready" | "denied">("starting");
  const [shots, setShots] = useState<string[]>(photos);

  const stopCamera = useCallback(() => {
    streamRef.current?.getTracks().forEach((t) => t.stop());
    streamRef.current = null;
  }, []);

  useEffect(() => {
    if (!open) {
      stopCamera();
      setCameraState("starting");
      return;
    }
    setShots(photos);
    let cancelled = false;
    async function start() {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" },
          audio: false,
        });
        if (cancelled) {
          stream.getTracks().forEach((t) => t.stop());
          return;
        }
        streamRef.current = stream;
        if (videoRef.current) {
          videoRef.current.srcObject = stream;
        }
        setCameraState("ready");
      } catch {
        if (!cancelled) setCameraState("denied");
      }
    }
    start();
    return () => {
      cancelled = true;
      stopCamera();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, stopCamera]);

  function capture() {
    const video = videoRef.current;
    if (cameraState !== "ready" || !video || video.videoWidth === 0) {
      setShots((p) => [...p, mockShot(subject)]);
      return;
    }
    const canvas = document.createElement("canvas");
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    canvas.getContext("2d")!.drawImage(video, 0, 0);
    setShots((p) => [...p, canvas.toDataURL("image/jpeg", 0.8)]);
  }

  function save() {
    stopCamera();
    onSave(shots);
    onOpenChange(false);
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Camera className="size-5" /> Chụp ảnh bằng chứng
          </DialogTitle>
          <DialogDescription>
            {subject} — ảnh phải <b>chụp trực tiếp bằng camera</b> tại thời điểm
            nhận hàng, không chấp nhận ảnh tải lên từ máy (BR-072).
          </DialogDescription>
        </DialogHeader>

        <div className="relative overflow-hidden rounded-lg border bg-muted">
          {cameraState === "denied" ? (
            <div className="flex h-52 items-center justify-center p-4">
              <Alert variant="warning" title="Không truy cập được camera">
                Trình duyệt chặn quyền camera hoặc máy không có webcam — nút
                chụp bên dưới sẽ tạo ảnh mock (chỉ để demo, bản thật bắt buộc
                camera trên Mobile).
              </Alert>
            </div>
          ) : (
            <>
              <video ref={videoRef} autoPlay playsInline muted className="h-52 w-full object-cover" />
              {cameraState === "starting" && (
                <div className="absolute inset-0 flex items-center justify-center gap-2 bg-muted text-sm text-muted-foreground">
                  <Camera className="size-4" /> Đang mở camera…
                </div>
              )}
            </>
          )}
        </div>

        {shots.length > 0 && (
          <div className="flex flex-wrap gap-2">
            {shots.map((src, i) => (
              <div key={i} className="group relative">
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img src={src} alt={`Ảnh bằng chứng ${i + 1}`} className="h-16 w-24 rounded-md border object-cover" />
                <button
                  type="button"
                  className="absolute -right-1.5 -top-1.5 hidden rounded-full bg-destructive p-0.5 text-white group-hover:block"
                  onClick={() => setShots((p) => p.filter((_, idx) => idx !== i))}
                  aria-label={`Xóa ảnh ${i + 1}`}
                >
                  <Trash2 className="size-3" />
                </button>
              </div>
            ))}
          </div>
        )}

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Hủy
          </Button>
          <Button
            type="button"
            variant="outline"
            onClick={capture}
            disabled={cameraState === "starting"}
          >
            <Camera /> {cameraState === "denied" ? "Chụp mock (không camera)" : "Chụp ảnh"}
          </Button>
          <Button onClick={save} disabled={shots.length === 0}>
            Lưu {shots.length > 0 ? `${shots.length} ảnh` : "ảnh"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
