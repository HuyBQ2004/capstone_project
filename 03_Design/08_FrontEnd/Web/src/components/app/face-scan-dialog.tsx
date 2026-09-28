"use client";

/**
 * FaceScanDialog — mock xác thực khuôn mặt trên Web (BR-051, ADR-002).
 * Web là secondary path: chỉ mở webcam qua getUserMedia để mô phỏng thao tác
 * quét mặt; KHÔNG có model sinh trắc học thật — kết quả luôn Passed sau delay.
 * Bản Mobile (triển khai sau) mới tích hợp SDK face biometric thật.
 * Nếu trình duyệt chặn camera → cho phép xác thực mock không cần camera
 * (tương đương luồng Manual Override, ghi chú rõ trên UI).
 */

import { useCallback, useEffect, useRef, useState } from "react";
import { Camera, CheckCircle2, Loader2, ScanFace } from "lucide-react";
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

interface FaceScanDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  /** Tên người cần xác thực (tài xế giao hàng…). */
  personName: string;
  /** Gọi khi xác thực (mock) thành công. */
  onVerified: () => void;
}

export function FaceScanDialog({
  open,
  onOpenChange,
  personName,
  onVerified,
}: FaceScanDialogProps) {
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const streamRef = useRef<MediaStream | null>(null);
  const [cameraState, setCameraState] = useState<"starting" | "ready" | "denied">("starting");
  const [scanning, setScanning] = useState(false);
  const [done, setDone] = useState(false);

  const stopCamera = useCallback(() => {
    streamRef.current?.getTracks().forEach((t) => t.stop());
    streamRef.current = null;
  }, []);

  useEffect(() => {
    if (!open) {
      stopCamera();
      setScanning(false);
      setDone(false);
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

  function handleScan() {
    setScanning(true);
    // Mock: giả lập thời gian đối chiếu khuôn mặt với hồ sơ sinh trắc học đã đăng ký (BR-050/BR-051)
    setTimeout(() => {
      setScanning(false);
      setDone(true);
      setTimeout(() => {
        stopCamera();
        onVerified();
        onOpenChange(false);
      }, 800);
    }, 1500);
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
            với hồ sơ sinh trắc học đã đăng ký (BR-050). Web là kênh phụ — bản mock
            chỉ mở camera minh họa, xác thực thật triển khai trên Mobile (ADR-002).
          </DialogDescription>
        </DialogHeader>

        <div className="relative overflow-hidden rounded-lg border bg-muted">
          {cameraState === "denied" ? (
            <div className="flex h-56 items-center justify-center p-4">
              <Alert variant="warning" title="Không truy cập được camera">
                Trình duyệt đã chặn quyền camera hoặc thiết bị không có webcam —
                dùng nút xác thực mock bên dưới (tương đương Manual Override,
                được ghi audit log).
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
              {/* Khung ngắm mô phỏng */}
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
            </>
          )}
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} disabled={scanning}>
            Hủy
          </Button>
          {cameraState === "denied" ? (
            <Button onClick={handleScan} disabled={scanning || done}>
              {scanning ? (
                <>
                  <Loader2 className="animate-spin" /> Đang đối chiếu…
                </>
              ) : (
                "Xác thực mock (không camera)"
              )}
            </Button>
          ) : (
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
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
