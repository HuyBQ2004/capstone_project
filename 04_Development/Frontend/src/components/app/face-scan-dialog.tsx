"use client";

/**
 * FaceScanDialog — xác thực khuôn mặt tài xế THẬT (BR-050/BR-051, UC-060).
 *
 * 06/08/2026 — bỏ hẳn mock "luôn Passed sau delay": giờ chụp khung hình camera thật, gửi
 * `POST /deliveries/{code}/verify-driver` (multipart) tới backend → `FaceRecognitionPort` →
 * FRS service thật (DeepFace, xem `04_Development/AiServices/frs_service`) so khớp với
 * `delivery_persons.face_id` đã đăng ký (BR-050). Không có camera ⇒ chặn hẳn, không cho
 * "xác thực mock" nữa — cùng quyết định với `CameraCaptureDialog`.
 */

import { useCallback, useEffect, useRef, useState } from "react";
import { Camera, CheckCircle2, Loader2, ScanFace, XCircle } from "lucide-react";
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
import { api, ApiError } from "@/lib/api/client";
import { apiErrorMessage } from "@/lib/messages";

interface FaceScanDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  /** Tên người cần xác thực (tài xế giao hàng…). */
  personName: string;
  /** Mã chuyến giao (D1) — `POST /deliveries/{code}/verify-driver`. */
  deliveryCode: string;
  /** Gọi khi xác thực THẬT thành công (backend trả `verified: true`). */
  onVerified: () => void;
}

export function FaceScanDialog({
  open,
  onOpenChange,
  personName,
  deliveryCode,
  onVerified,
}: FaceScanDialogProps) {
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const streamRef = useRef<MediaStream | null>(null);
  const [cameraState, setCameraState] = useState<"starting" | "ready" | "denied">("starting");
  const [scanning, setScanning] = useState(false);
  const [done, setDone] = useState(false);
  const [error, setError] = useState("");

  const stopCamera = useCallback(() => {
    streamRef.current?.getTracks().forEach((t) => t.stop());
    streamRef.current = null;
  }, []);

  useEffect(() => {
    if (!open) {
      stopCamera();
      setScanning(false);
      setDone(false);
      setError("");
      setCameraState("starting");
      return;
    }
    let cancelled = false;
    async function start() {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "user" },
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
  }, [open, stopCamera]);

  async function handleScan() {
    const video = videoRef.current;
    if (cameraState !== "ready" || !video || video.videoWidth === 0) return;

    setError("");
    setScanning(true);

    const canvas = document.createElement("canvas");
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    canvas.getContext("2d")!.drawImage(video, 0, 0);

    canvas.toBlob(
      async (blob) => {
        if (!blob) {
          setScanning(false);
          setError("Không chụp được khung hình — thử lại.");
          return;
        }
        try {
          // KHÔNG tự set Content-Type — trình duyệt/axios tự thêm boundary đúng khi body
          // là FormData; set tay "multipart/form-data" thiếu boundary sẽ hỏng parse ở backend.
          const form = new FormData();
          form.append("image", blob, "capture.jpg");
          await api.post(`/deliveries/${deliveryCode}/verify-driver`, form);
          setScanning(false);
          setDone(true);
          setTimeout(() => {
            stopCamera();
            onVerified();
            onOpenChange(false);
          }, 800);
        } catch (err) {
          setScanning(false);
          setError(err instanceof ApiError ? apiErrorMessage(err, "Xác thực khuôn mặt thất bại.") : "Không kết nối được máy chủ.");
        }
      },
      "image/jpeg",
      0.85
    );
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <ScanFace className="size-5" /> Xác thực khuôn mặt (BR-051)
          </DialogTitle>
          <DialogDescription>
            Đối chiếu khuôn mặt của <span className="font-medium">{personName}</span>{" "}
            với hồ sơ sinh trắc học đã đăng ký (BR-050) — gọi FRS service thật, không mô phỏng.
          </DialogDescription>
        </DialogHeader>

        {error && (
          <Alert variant="danger" title="Xác thực thất bại">
            {error}
          </Alert>
        )}

        <div className="relative overflow-hidden rounded-lg border bg-muted">
          {cameraState === "denied" ? (
            <div className="flex h-56 items-center justify-center p-4">
              <Alert variant="danger" title="Không truy cập được camera">
                Trình duyệt chặn quyền camera hoặc thiết bị không có webcam — BR-051 bắt buộc
                quét mặt trực tiếp, không thể xác thực cho tới khi cấp quyền camera.
              </Alert>
            </div>
          ) : (
            <>
              <video
                ref={videoRef}
                autoPlay
                playsInline
                muted
                className="h-56 w-full object-cover"
              />
              {cameraState === "starting" && (
                <div className="absolute inset-0 flex items-center justify-center gap-2 bg-muted text-sm text-muted-foreground">
                  <Camera className="size-4" /> Đang mở camera…
                </div>
              )}
              {/* Khung ngắm */}
              {cameraState === "ready" && !done && (
                <div className="pointer-events-none absolute inset-0 flex items-center justify-center">
                  <div
                    className={`size-40 rounded-full border-2 ${
                      scanning ? "animate-pulse border-primary" : "border-white/70"
                    }`}
                  />
                </div>
              )}
              {done && (
                <div className="absolute inset-0 flex flex-col items-center justify-center gap-2 bg-success/80 text-white">
                  <CheckCircle2 className="size-10" />
                  <p className="text-sm font-semibold">FACE MATCHED — {personName}</p>
                </div>
              )}
              {error && !done && (
                <div className="absolute inset-0 flex flex-col items-center justify-center gap-2 bg-destructive/80 text-white">
                  <XCircle className="size-10" />
                  <p className="text-sm font-semibold">
                    {error.toLowerCase().includes("không khớp") || error.toLowerCase().includes("thất bại") ? "KHÔNG KHỚP" : "LỖI XÁC THỰC"}
                  </p>
                </div>
              )}
            </>
          )}
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} disabled={scanning}>
            Hủy
          </Button>
          <Button onClick={handleScan} disabled={scanning || done || cameraState !== "ready"}>
            {scanning ? (
              <>
                <Loader2 className="animate-spin" /> Đang đối chiếu…
              </>
            ) : (
              <>
                <ScanFace /> Quét & xác thực
              </>
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
