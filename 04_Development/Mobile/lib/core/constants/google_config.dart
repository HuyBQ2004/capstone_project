/// Web Client ID của cùng project Google OAuth mà backend đã cấu hình ở
/// `application.yml: spring.security.oauth2.client.registration.google.client-id`
/// (ENV `GOOGLE_CLIENT_ID`). Bắt buộc TRÙNG giá trị đó — `GoogleIdTokenVerifier`
/// (backend, `com.sfms.iam.security`) so khớp claim `aud` của id_token với đúng
/// client-id này, khác thì bị chặn IAM-103 dù chữ ký hợp lệ.
///
/// Truyền vào `GoogleSignIn(serverClientId: ...)` — KHÔNG phải Android client ID.
///
/// Việc còn thiếu để nút "Đăng nhập Google" chạy được (làm ở Google Cloud/Firebase
/// Console, không phải code):
///   1. Lấy SHA-1 (debug + release) của app `com.sfms.sfms_mobile`.
///   2. Firebase Console → Project settings → app Android → Add fingerprint.
///      (Project đã link cùng GCP project với backend, tự sinh Android OAuth
///      client — không cần tạo tay trên GCP Console.)
///   3. Điền đúng giá trị `GOOGLE_CLIENT_ID` (backend) vào hằng số dưới đây.
const String kGoogleServerClientId = '208623889846-vnpmvrh4q2v7gbkbqobdg6gqu96ssvdd.apps.googleusercontent.com';
