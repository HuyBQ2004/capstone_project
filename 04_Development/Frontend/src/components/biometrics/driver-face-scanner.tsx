"use client";

import React, { useRef, useState } from "react";
import { Camera, CheckCircle2, AlertCircle, RefreshCw } from "lucide-react";

interface DriverFaceScannerProps {
  deliveryCode?: string;
  onVerificationComplete?: (success: boolean, message: string) => void;
}

export const DriverFaceScanner: React.FC<DriverFaceScannerProps> = ({
  deliveryCode,
  onVerificationComplete,
}) => {
  const videoRef = useRef<HTMLVideoElement>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [isCameraActive, setIsCameraActive] = useState(false);
  const [verifying, setVerifying] = useState(false);
  const [result, setResult] = useState<{ success: boolean; message: string } | null>(null);

  const startCamera = async () => {
    try {
      setResult(null);
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { width: 640, height: 480, facingMode: "user" },
      });
      if (videoRef.current) {
        videoRef.current.srcObject = stream;
        videoRef.current.play();
      }
      setIsCameraActive(true);
    } catch (err) {
      console.error("Camera access error:", err);
      setResult({
        success: false,
        message: "Không thể truy cập Camera thiết bị. Vui lòng kiểm tra cấp quyền browser.",
      });
    }
  };

  const stopCamera = () => {
    if (videoRef.current && videoRef.current.srcObject) {
      const stream = videoRef.current.srcObject as MediaStream;
      stream.getTracks().forEach((track) => track.stop());
      videoRef.current.srcObject = null;
    }
    setIsCameraActive(false);
  };

  const captureAndVerify = async () => {
    if (!videoRef.current || !canvasRef.current) return;
    setVerifying(true);

    const canvas = canvasRef.current;
    const video = videoRef.current;
    canvas.width = video.videoWidth || 640;
    canvas.height = video.videoHeight || 480;
    const ctx = canvas.getContext("2d");
    if (ctx) {
      ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
    }

    canvas.toBlob(async (blob) => {
      if (!blob) {
        setVerifying(false);
        return;
      }

      const formData = new FormData();
      formData.append("image", blob, "driver_capture.jpg");

      try {
        const endpoint = deliveryCode
          ? `/api/v1/deliveries/${deliveryCode}/verify-driver`
          : `/api/v1/frs/verify`;

        const res = await fetch(endpoint, {
          method: "POST",
          body: formData,
        });

        if (res.ok) {
          const data = await res.json();
          const resMsg = data.message || "Xác thực sinh trắc học khuôn mặt thành công!";
          setResult({ success: true, message: resMsg });
          stopCamera();
          if (onVerificationComplete) onVerificationComplete(true, resMsg);
        } else {
          const errData = await res.json().catch(() => ({}));
          const errMsg = errData.detail || "Xác thực khuôn mặt thất bại (BR-051). Vui lòng thử lại.";
          setResult({ success: false, message: errMsg });
          if (onVerificationComplete) onVerificationComplete(false, errMsg);
        }
      } catch (e) {
        console.error("Verification API error:", e);
        setResult({ success: false, message: "Lỗi kết nối máy chủ xác thực FRS." });
      } finally {
        setVerifying(false);
      }
    }, "image/jpeg");
  };

  return (
    <div className="flex flex-col items-center justify-center p-6 bg-slate-900 text-white rounded-xl border border-slate-800 shadow-2xl max-w-md mx-auto">
      <h3 className="text-lg font-semibold mb-2 flex items-center gap-2 text-sky-400">
        <Camera className="w-5 h-5" /> Xác Thực Sinh Trắc Học Tài Xế
      </h3>
      <p className="text-xs text-slate-400 mb-4 text-center">
        PDPD Compliant — Chỉ dành cho nhân viên giao hàng thực phẩm tại cổng trường (BR-051).
      </p>

      <div className="relative w-full aspect-video bg-black rounded-lg overflow-hidden border border-slate-700 mb-4 flex items-center justify-center">
        <video ref={videoRef} className="w-full h-full object-cover" muted playsInline />
        <canvas ref={canvasRef} className="hidden" />

        {isCameraActive && (
          <div className="absolute inset-0 border-2 border-dashed border-sky-500/50 rounded-full w-48 h-48 m-auto pointer-events-none flex items-center justify-center">
            <span className="text-[10px] text-sky-300 bg-slate-900/80 px-2 py-0.5 rounded">
              Căn khuôn mặt vào ô
            </span>
          </div>
        )}

        {!isCameraActive && !result && (
          <button
            onClick={startCamera}
            className="px-4 py-2 bg-sky-600 hover:bg-sky-500 text-white text-sm font-medium rounded-lg shadow transition flex items-center gap-2"
          >
            <Camera className="w-4 h-4" /> Bật Camera Quét Mặt
          </button>
        )}
      </div>

      {isCameraActive && (
        <div className="flex gap-3 w-full">
          <button
            onClick={captureAndVerify}
            disabled={verifying}
            className="flex-1 py-2.5 bg-emerald-600 hover:bg-emerald-500 disabled:bg-slate-700 text-white text-sm font-semibold rounded-lg shadow transition flex items-center justify-center gap-2"
          >
            {verifying ? (
              <>
                <RefreshCw className="w-4 h-4 animate-spin" /> Đang So Khớp FRS...
              </>
            ) : (
              "Xác Nhận & So Khớp"
            )}
          </button>
          <button
            onClick={stopCamera}
            className="px-4 py-2.5 bg-slate-700 hover:bg-slate-600 text-slate-300 text-sm font-medium rounded-lg shadow transition"
          >
            Tắt
          </button>
        </div>
      )}

      {result && (
        <div
          className={`mt-4 w-full p-3 rounded-lg border text-sm flex items-start gap-2.5 ${
            result.success
              ? "bg-emerald-950/60 border-emerald-500/40 text-emerald-300"
              : "bg-rose-950/60 border-rose-500/40 text-rose-300"
          }`}
        >
          {result.success ? (
            <CheckCircle2 className="w-5 h-5 text-emerald-400 shrink-0 mt-0.5" />
          ) : (
            <AlertCircle className="w-5 h-5 text-rose-400 shrink-0 mt-0.5" />
          )}
          <div className="flex-1">
            <p className="font-semibold">{result.success ? "Thành Công" : "Thất Bại"}</p>
            <p className="text-xs opacity-90">{result.message}</p>
            <button
              onClick={startCamera}
              className="mt-2 text-xs font-semibold underline text-sky-400 hover:text-sky-300"
            >
              Thử Quét Lại
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
