import axios, { AxiosError } from "axios";

/**
 * Axios client dùng chung cho toàn bộ frontend.
 * Khi chưa có backend thật, mock adapter (mock-server.ts) sẽ chặn
 * mọi request tới baseURL này và trả dữ liệu in-memory.
 *
 * `/api/v1` theo `07_implement/00_Common_Conventions.md` D3 — mọi TDS đều ghi
 * "Path thật thêm tiền tố /api/v1". Mock adapter khớp trên `config.url`
 * (không gồm baseURL) nên không bị ảnh hưởng bởi tiền tố này.
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
