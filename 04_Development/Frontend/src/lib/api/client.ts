import axios, { AxiosError, type InternalAxiosRequestConfig } from "axios";
import {
  getAccessToken,
  notifySessionExpired,
  refreshAccessToken,
} from "./tokens";

/**
 * Axios client dùng chung cho toàn bộ frontend.
 *
 * Mọi request đi THẬT tới Spring Boot, qua rewrite `/api/v1/:path*` ở
 * `next.config.ts` (same-origin ⇒ không cần CORS). `mock-server.ts`/`db.ts`/
 * `real-resources.ts` (adapter mock in-memory dùng trong giai đoạn nối thật dần
 * từng module) đã xoá 06/08/2026 — toàn bộ resource đã có backend thật.
 *
 * `/api/v1` theo `07_implement/00_Common_Conventions.md` D3 — mọi TDS đều ghi
 * "Path thật thêm tiền tố /api/v1".
 */
export const api = axios.create({
  baseURL: "/api/v1",
  timeout: 10000,
});

/**
 * Lỗi API đã bóc khỏi envelope, dùng chung cho mọi `onError` của TanStack Query.
 *
 * `code` là mã module (`SCH-010`, `SMP-105`…) — tra `lib/messages.ts` để lấy
 * nhãn tiếng Việt. `message` là nguyên văn tiếng Anh từ `error_message.md`,
 * dùng làm fallback khi chưa có bản dịch (00_Common_Conventions.md D7).
 */
export class ApiError extends Error {
  constructor(
    readonly code: string | undefined,
    message: string,
    readonly details: unknown[] = [],
    readonly status?: number
  ) {
    super(message);
    this.name = "ApiError";
  }
}

/**
 * Bóc envelope lỗi thành `ApiError`.
 *
 * KHOAN DUNG HAI SHAPE — có chủ đích:
 *   - backend thật (D2):  { error: { code, message, details } }
 *   - mock hiện tại:      { message }
 *
 * Mock đang trả shape phẳng ở 10 chỗ và sẽ bị xóa khi backend chạy, nên sửa nó
 * là công vô ích (xem `07_implement/review_fix_plan.md` mục "KHÔNG làm"). Đọc
 * được cả hai nghĩa là interceptor này không phải viết lại lúc cắt sang backend.
 */
function toApiError(error: AxiosError): ApiError {
  /*
   * Request được replay sau khi refresh đi qua CẢ HAI interceptor response một
   * lần nữa, nên lỗi của lần replay tới đây đã là `ApiError`. Bọc lại lần hai sẽ
   * nuốt mất `code`/`status` (vì `ApiError` không có `.response`) và để lại đúng
   * một chuỗi message trần — trả nguyên vẹn thay vì dựng lại.
   */
  if (error instanceof ApiError) return error;

  const data = error.response?.data as
    | { error?: { code?: string; message?: string; details?: unknown[] }; message?: string }
    | undefined;

  const enveloped = data?.error;
  return new ApiError(
    enveloped?.code,
    enveloped?.message ?? data?.message ?? error.message ?? "Đã có lỗi xảy ra",
    enveloped?.details ?? [],
    error.response?.status
  );
}

/*
 * ─────────────────────── Request: gắn Bearer (§16 Auth Matrix) ───────────────────────
 *
 * Đọc token từ `tokens.ts` ở TỪNG request chứ không set `api.defaults.headers` một
 * lần: token đổi sau mỗi lần refresh, và `defaults` đã đọc lúc module load thì
 * request sau khi rotate vẫn mang token cũ.
 */
/**
 * Đường dẫn gọi mô hình ngôn ngữ — CHẬM theo bản chất, không phải do mạng lỗi.
 *
 * <p>Đo 09/08/2026: một lượt sinh của rag-service mất 11.5s cho câu hỏi ngắn và vượt 60s cho
 * prompt gợi ý thực đơn kèm danh mục nguyên liệu. Với `timeout: 10000` dùng chung, MỌI câu hỏi
 * gửi từ trợ lý AI đều bị axios huỷ trước khi có câu trả lời và hiện "Không kết nối được trợ lý
 * AI" — trong khi backend vẫn sinh xong và ghi log THÀNH CÔNG, nên nhìn từ log tưởng không lỗi.
 *
 * <p>Giữ 10s cho phần còn lại: request CRUD mà quá 10s thì đúng là hỏng, báo sớm vẫn tốt hơn.
 * Nginx cũng phải nới tương ứng cho `/api/v1/ai/` (xem `nginx/nginx.conf`) — nới một đầu là
 * chưa đủ, đầu kia vẫn trả 504.
 */
const SLOW_AI_PATHS = ["/ai/", "/ai-suggest"];
const AI_TIMEOUT_MS = 180_000;

/**
 * NHẬP HÀNG LOẠT — chậm theo KHỐI LƯỢNG, không phải do hỏng (13/08/2026, Nam báo: nhập
 * danh sách 100 học sinh ⇒ "timeout of 10000ms exceeded").
 *
 * <p>`POST /students/import/{id}/commit` cố ý chạy TẤT CẢ trong MỘT transaction
 * (ADR-M03-6 — all-or-nothing, không nhập một phần), và mỗi hàng còn phải sinh mã học
 * sinh + mã đăng ký, tạo tài khoản phụ huynh (băm mật khẩu BCrypt — mỗi lần cỡ 100ms) và
 * ghi lịch sử sức khoẻ. 100 hàng vượt 10s là chuyện bình thường.
 *
 * <p>Hậu quả của timeout 10s ở đây KHÔNG chỉ là báo lỗi sai: axios huỷ ở phía client
 * nhưng backend VẪN chạy tiếp tới khi commit xong, nên người dùng thấy "lỗi" rồi bấm
 * Nhập lại — lần này backend trả `importAlreadyCommitted`. Nhìn từ UI là "nhập mãi không
 * được", trong khi dữ liệu đã vào từ lần đầu.
 *
 * <p>Khớp theo `/import` (đủ hẹp: chỉ luồng nhập file của students và supplier-products).
 */
const BULK_IMPORT_PATHS = ["/import"];
const BULK_IMPORT_TIMEOUT_MS = 120_000;

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getAccessToken();
  if (token) {
    config.headers.set("Authorization", `Bearer ${token}`);
  }
  const url = config.url ?? "";
  if (SLOW_AI_PATHS.some((p) => url.includes(p))) {
    config.timeout = AI_TIMEOUT_MS;
  } else if (BULK_IMPORT_PATHS.some((p) => url.includes(p))) {
    config.timeout = BULK_IMPORT_TIMEOUT_MS;
  }
  return config;
});

/** Các endpoint tự nó là luồng xác thực — 401 ở đây KHÔNG được kích hoạt refresh. */
const NO_REFRESH_PATHS = [
  "/auth/login",
  "/auth/refresh",
  "/auth/forgot-password",
  "/auth/reset-password",
  "/auth/activate",
];

/** Đánh dấu request đã replay một lần, chặn vòng lặp 401 → refresh → 401 → … */
type RetriableConfig = InternalAxiosRequestConfig & { _sfmsRetried?: boolean };

/*
 * ─────────────────────── Response: 401 → refresh → replay ───────────────────────
 *
 * Interceptor đăng ký TRƯỚC cái chuyển `ApiError` bên dưới, vì axios chạy các
 * handler response theo đúng thứ tự đăng ký: phải còn `AxiosError` (còn `config`
 * để replay) thì mới thử lại được. Nếu đảo thứ tự, tới đây chỉ còn `ApiError` —
 * mất `config`, không replay được nữa.
 */
api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const config = error.config as RetriableConfig | undefined;
    const url = config?.url ?? "";

    const refreshable =
      error.response?.status === 401 &&
      config != null &&
      !config._sfmsRetried &&
      !NO_REFRESH_PATHS.some((p) => url.startsWith(p));

    if (!refreshable) return Promise.reject(error);

    const fresh = await refreshAccessToken();

    if (!fresh) {
      // Refresh token hết hạn/bị revoke → dọn phiên + để `auth-store` đá về /login.
      notifySessionExpired();
      return Promise.reject(error);
    }

    config._sfmsRetried = true;
    config.headers.set("Authorization", `Bearer ${fresh}`);
    return api.request(config);
  }
);

api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => Promise.reject(toApiError(error))
);

/** Chuẩn response phân trang của backend (đồng bộ với mock-server). */
export interface Paged<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
  pageCount: number;
}

export interface ListParams {
  page?: number;
  pageSize?: number;
  /** Tìm kiếm không phân biệt hoa thường trên các trường chuỗi. */
  search?: string;
  /** Lọc chính xác theo field, ví dụ { status: "PENDING" }. */
  [key: string]: string | number | undefined;
}
