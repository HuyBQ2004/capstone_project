"use client";

import { useCallback, useEffect, useRef, useState } from "react";

export interface GpsCoords {
  lat: number;
  lng: number;
}

/**
 * GPS dùng cho geofence (BR-073/074/083) — PHẢI là vị trí THẬT tại đúng thời điểm hành động,
 * không phải vị trí lấy một lần rồi tái dùng suốt phiên: người dùng có thể đã di chuyển ra
 * ngoài geofence giữa lúc ghi vị trí và lúc nộp, tái dùng giá trị cũ sẽ vô hiệu hoá đúng thứ
 * geofence muốn kiểm (cùng lý do `CameraCaptureDialog` bắt buộc chụp trực tiếp, không cho
 * chọn ảnh có sẵn — BR-072).
 *
 * 11/08/2026 (theo Nam) — trước đây mỗi trang tự viết lại y hệt logic
 * `navigator.geolocation.getCurrentPosition` + state loading/coords (receiving/new,
 * inspections/[id]) và CHỈ lấy vị trí khi người dùng bấm nút. Hook này:
 *   1. Tự thử lấy vị trí NGAY khi mount (im lặng — không toast lỗi nếu bị từ chối/chưa cấp
 *      quyền, quyền trình duyệt vốn đã nhớ theo origin nên lần sau vào lại không hỏi lại).
 *   2. `capture()` LUÔN gọi lại `getCurrentPosition` thật (không trả state cũ) — gọi hàm này
 *      ngay trước lúc nộp để đảm bảo toạ độ mới nhất, không phải giá trị lấy từ trước đó.
 */
export function useGpsCapture() {
  const [coords, setCoords] = useState<GpsCoords | null>(null);
  const [loading, setLoading] = useState(false);
  const mountedRef = useRef(true);

  useEffect(() => {
    mountedRef.current = true;
    return () => {
      mountedRef.current = false;
    };
  }, []);

  const capture = useCallback((): Promise<GpsCoords | null> => {
    if (typeof navigator === "undefined" || !navigator.geolocation) {
      return Promise.resolve(null);
    }
    setLoading(true);
    return new Promise((resolve) => {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          const next = { lat: pos.coords.latitude, lng: pos.coords.longitude };
          if (mountedRef.current) {
            setCoords(next);
            setLoading(false);
          }
          resolve(next);
        },
        () => {
          if (mountedRef.current) setLoading(false);
          resolve(null);
        },
        { enableHighAccuracy: true, timeout: 10_000 }
      );
    });
  }, []);

  // Thử ngầm ngay khi vào trang — đỡ phải bấm nút nếu quyền đã cấp từ trước.
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => { capture(); }, []);

  return { coords, loading, capture };
}
