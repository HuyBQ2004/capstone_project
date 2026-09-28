"use client";

/**
 * CameraCaptureDialog — chụp ảnh bằng chứng TRỰC TIẾP từ camera (không cho
 * upload ảnh có sẵn) — nhận xét Minh 17/07/2026: lô hàng bị từ chối phải có
 * ảnh chụp làm chứng theo TỪNG LÔ, chụp tại chỗ để đảm bảo tính xác thực
 * (BR-072 — timestamp/GPS gắn lúc chụp).
 * Web mở webcam qua getUserMedia rồi chụp khung hình vào canvas.
 *
 * 06/08/2026 — bỏ hẳn nhánh "chụp mock khi không có camera": trước đây trình duyệt
 * chặn/không có camera vẫn cho tạo ảnh placeholder và submit như ảnh thật, vô hiệu hoá
 * hoàn toàn yêu cầu BR-072 "ảnh phải chụp trực tiếp". Giờ không có camera ⇒ chặn hẳn,
 * không cho chụp/lưu.
 */

import { useCallback, useEffect, useRef, useState } from "react";
import { Camera, RefreshCw, Trash2 } from "lucide-react";
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
  // Lật camera trước/sau — chỉ có ý nghĩa khi thiết bị có nhiều hơn 1 camera (điện thoại/
  // tablet); desktop webcam thường chỉ có 1 nên nút lật bị ẩn thay vì hiện ra không làm gì.
  const [facingMode, setFacingMode] = useState<"environment" | "user">("environment");
  const [multiCamera, setMultiCamera] = useState(false);

  const stopCamera = useCallback(() => {
    streamRef.current?.getTracks().forEach((t) => t.stop());
    streamRef.current = null;
  }, []);

  useEffect(() => {
    if (!open) {
      stopCamera();
      setCameraState("starting");
      setFacingMode("environment");
      return;
    }
    setShots(photos);
    let cancelled = false;
    async function start() {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({
          video: { facingMode },
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
        // Nhãn thiết bị chỉ đọc được SAU khi đã cấp quyền camera — enumerate lại ở đây.
        const devices = await navigator.mediaDevices.enumerateDevices();
        if (!cancelled) {
          setMultiCamera(devices.filter((d) => d.kind === "videoinput").length > 1);
        }
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
  }, [open, facingMode, stopCamera]);

  function capture() {
    const video = videoRef.current;
    if (cameraState !== "ready" || !video || video.videoWidth === 0) {
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
    // `modal={false}` (10/08/2026) — component này thường mở LỒNG bên trong một Dialog form
    // khác (VD `service-caterers/samples/page.tsx`). Radix nested-Dialog: khi dialog trong
    // đóng, FocusScope trả focus về `document.body` (dialog trong đã unmount), DismissableLayer
    // của dialog NGOÀI đọc đó là "focus ra ngoài content" ⇒ tự đóng luôn dialog ngoài — bấm
    // "Lưu ảnh" đóng mất cả form đang điền dở, không chỉ đóng camera. `modal=false` bỏ
    // RemoveScroll/focus-trap ngặt của Radix cho dialog trong, đủ để hết xung đột tầng.
    <Dialog open={open} onOpenChange={onOpenChange} modal={false}>
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
              <Alert variant="danger" title="Không truy cập được camera">
                Trình duyệt chặn quyền camera hoặc máy không có webcam — BR-072 bắt buộc ảnh
                chụp trực tiếp, không thể tiếp tục cho tới khi cấp quyền camera.
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
              {cameraState === "ready" && multiCamera && (
                <button
                  type="button"
                  onClick={() => setFacingMode((m) => (m === "environment" ? "user" : "environment"))}
                  className="absolute right-2 top-2 flex items-center gap-1.5 rounded-full bg-black/50 px-3 py-1.5 text-xs font-medium text-white backdrop-blur hover:bg-black/70"
                  title="Đổi camera trước/sau"
                >
                  <RefreshCw className="size-3.5" />
                  {facingMode === "environment" ? "Cam sau" : "Cam trước"}
                </button>
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
            disabled={cameraState !== "ready"}
          >
            <Camera /> Chụp ảnh
          </Button>
          <Button onClick={save} disabled={shots.length === 0}>
            Lưu {shots.length > 0 ? `${shots.length} ảnh` : "ảnh"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
