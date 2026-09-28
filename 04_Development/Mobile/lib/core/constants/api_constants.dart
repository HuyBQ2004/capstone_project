class ApiConstants {
  // Domain thật, dùng cho build release và khi test trên máy thật/emulator
  // không kết nối được về máy dev (10.0.2.2 chỉ dùng nội bộ Android Emulator).
  // Đổi lại URL local (http://10.0.2.2:8080/api/v1, ...) khi cần debug backend
  // chạy trên máy mình.
  static const String origin = 'https://sfsm.io.vn';
  static const String baseUrl = '$origin/api/v1';
  static const String frsUrl = '$origin/api/v1/frs';
  static const String ragUrl = '$origin/api/v1/rag';

  /// `avatarUrl`/media path backend trả về path tương đối (`/media/...`,
  /// `LocalDiskStorageService`) — ghép với [origin] để dùng trực tiếp trong
  /// `Image.network`. Đã là URL tuyệt đối thì giữ nguyên.
  static String resolveMediaUrl(String path) => path.startsWith('http') ? path : '$origin$path';

  // Endpoints
  static const String login = '$baseUrl/auth/login';
  static const String verifyDriverFace = '$baseUrl/deliveries';
  static const String aiChat = '$baseUrl/ai/chat';
  static const String mealRecords = '$baseUrl/meal-records';
  static const String allergyDeclarations = '$baseUrl/allergy-declarations';
}
