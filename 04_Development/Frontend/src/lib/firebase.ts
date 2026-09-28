import { initializeApp, getApps } from "firebase/app";
import { getMessaging, getToken, isSupported, type Messaging } from "firebase/messaging";

const firebaseConfig = {
  apiKey: process.env.NEXT_PUBLIC_FIREBASE_API_KEY,
  authDomain: process.env.NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN,
  projectId: process.env.NEXT_PUBLIC_FIREBASE_PROJECT_ID,
  storageBucket: process.env.NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: process.env.NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID,
  appId: process.env.NEXT_PUBLIC_FIREBASE_APP_ID,
};

function getFirebaseApp() {
  return getApps()[0] ?? initializeApp(firebaseConfig);
}

/**
 * Xin quyền notification + lấy FCM registration token cho browser/thiết bị hiện tại.
 * Trả về null khi: browser không hỗ trợ (Safari <16.4 chưa PWA), hoặc user từ chối quyền.
 *
 * TODO: chưa có endpoint backend lưu token (MF10 `notification_deliveries` mới có channel
 * PUSH ở schema, chưa nối gửi thật — xem NotificationPortImpl.java). Khi có endpoint, POST
 * token này lên đây, gắn với user đang đăng nhập.
 */
export async function requestPushToken(): Promise<string | null> {
  if (typeof window === "undefined" || !(await isSupported())) return null;

  const permission = await Notification.requestPermission();
  if (permission !== "granted") return null;

  const registration = await navigator.serviceWorker.register("/firebase-messaging-sw.js");
  const messaging: Messaging = getMessaging(getFirebaseApp());

  return getToken(messaging, {
    vapidKey: process.env.NEXT_PUBLIC_FIREBASE_VAPID_KEY,
    serviceWorkerRegistration: registration,
  });
}
