// Service worker riêng cho Firebase Cloud Messaging — PHẢI nằm ở public/ (root), Firebase SDK
// mặc định register đúng path "/firebase-messaging-sw.js". Chạy độc lập với tab web nên vẫn
// nhận được push khi tab đã đóng (xem giải thích trong chat, không lặp lại ở đây).
importScripts("https://www.gstatic.com/firebasejs/10.13.2/firebase-app-compat.js");
importScripts("https://www.gstatic.com/firebasejs/10.13.2/firebase-messaging-compat.js");

// Config không phải secret (luôn bị nhúng công khai vào bundle JS của mọi web app dùng Firebase).
firebase.initializeApp({
  apiKey: "AIzaSyBz_3uelvJLp08Dxl3EVPyer5Buo1kvSOw",
  authDomain: "sfms-production.firebaseapp.com",
  projectId: "sfms-production",
  storageBucket: "sfms-production.firebasestorage.app",
  messagingSenderId: "208623889846",
  appId: "1:208623889846:web:172a11341d986bcd572517",
});

const messaging = firebase.messaging();

// Hiện notification khi push đến lúc tab không active/đã đóng (background message).
messaging.onBackgroundMessage((payload) => {
  const { title, body, icon } = payload.notification ?? {};
  self.registration.showNotification(title ?? "SFMS", {
    body,
    icon: icon ?? "/icon-192.png",
    data: payload.data,
  });
});
