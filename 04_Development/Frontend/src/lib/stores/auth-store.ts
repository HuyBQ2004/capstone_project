"use client";

/**
 * Zustand store — phiên đăng nhập.
 *
 * Từ 02/08/2026 đây là phiên THẬT với MF01: `POST /auth/login` cấp JWT, rồi
 * `GET /profile/me` nạp danh tính đầy đủ. Không còn tài khoản demo/giả nào —
 * mọi màn cần danh tính người dùng phải đọc từ phiên thật (`user`) hoặc chấp
 * nhận `undefined` khi chưa có phiên.
 *
 * Token nằm ở `lib/api/tokens.ts`: access token trong memory, refresh token trong
 * cookie httpOnly. Store này chỉ giữ phần *hiển thị được* của phiên.
 */

import { create } from "zustand";
import { api, ApiError } from "@/lib/api/client";
import {
  clearRefreshToken,
  persistRefreshToken,
  refreshAccessToken,
  registerSessionExpiredHandler,
  setAccessToken,
} from "@/lib/api/tokens";
import type { RoleKey } from "@/lib/roles";
import type { ClassroomDto } from "@/lib/api/types";
import { usePagedList } from "@/lib/api/hooks";

export interface SessionUser {
  /**
   * Mã nghiệp vụ của tài khoản (D1) — `UserResponse.id`, VD `U-9902`. Trước đây
   * khoá tới `UserAccount.id` của seed mock; nay là mã thật từ backend.
   */
  id?: string;
  name: string;
  email: string;
  role: RoleKey;
  /** Nhãn hiển thị tên trường (`ProfileResponse.school`). */
  school?: string;
  /** D1 — mã nghiệp vụ của trường, khoá để lọc dữ liệu (`SCH-HN-0042`). */
  schoolId?: string;
  /** D1 — mã đối tác, khoá cách ly đa-tenant cho tài khoản NCC/đơn vị nấu ăn. */
  partnerId?: string;
  /**
   * Trường mà CẤU HÌNH VẬN HÀNH áp cho tài khoản này — khác `schoolId` ("thuộc trường nào").
   * Tài khoản Đơn vị suất ăn không có `schoolId`; server suy từ hợp tác đang hiệu lực
   * (BR-EXCLUSIVE-CATERER, 1-1). NULL với NCC vì phục vụ nhiều trường. Xem `lib/school-config.ts`.
   */
  activeSchoolId?: string;
  /** Nhãn hiển thị của `activeSchoolId` — đi cặp với nó. Dùng qua `useSchoolLabel()`. */
  activeSchool?: string;
  avatarUrl?: string;
  phone?: string;
  status?: string;
  termsAcceptedAt?: string | null;
}

/** `POST /api/v1/auth/login` — `AuthDtos.LoginResponse`, đúng 6 khóa. */
interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  role: RoleKey;
  schoolId: string | null;
  /** Bổ sung 29/07/2026 (review B3) — khoá cách ly đa-tenant cho tài khoản đối tác. */
  partnerId: string | null;
  /** BR-009 — lần đăng nhập đầu ⇒ true, FE hiện màn chấp nhận điều khoản. */
  needTermsAccept: boolean;
}

/** `GET /api/v1/profile/me` — `UserDtos.ProfileResponse`. */
interface ProfileResponse {
  id: string;
  name: string;
  email: string;
  phone: string | null;
  role: RoleKey;
  schoolId: string | null;
  school: string | null;
  partnerId: string | null;
  activeSchoolId: string | null;
  activeSchool: string | null;
  status: string;
  avatarUrl: string | null;
  lastLogin: string | null;
  termsAcceptedAt: string | null;
}

/**
 * `idle` → chưa thử khôi phục phiên; `loading` → đang gọi `/auth/refresh` lúc mount.
 * Phân biệt hai cái này để AppShell không nháy màn "chưa đăng nhập" trong lúc
 * cookie httpOnly đang được đổi lấy access token.
 */
export type AuthStatus = "idle" | "loading" | "authenticated" | "anonymous";

interface AuthState {
  user: SessionUser | null;
  status: AuthStatus;
  /** BR-009 — cần hiện màn chấp nhận điều khoản trước khi vào workspace. */
  needTermsAccept: boolean;

  /**
   * Phiên vừa CHẾT GIỮA CHỪNG (refresh token hết hạn hoặc bị revoke vì đăng xuất
   * ở thiết bị khác — BR-004), khác hẳn "chưa từng đăng nhập". Phân biệt để màn
   * đăng nhập nói đúng lý do thay vì hiện form trắng như người lạ vừa ghé.
   */
  sessionExpired: boolean;

  /** Màn đăng nhập gọi sau khi đã hiện thông báo, để không lặp lại ở lần sau. */
  clearSessionExpired: () => void;

  /**
   * UC-034 — đăng nhập thật. Trả về phiên đầy đủ (không chỉ vai trò) để trang login
   * tính đúng trang đích qua {@link homeRouteFor} — cần cả `activeSchoolId`.
   */
  loginWithPassword: (email: string, password: string) => Promise<SessionUser>;

  /**
   * UC-191 — hoàn tất đăng nhập Google sau khi `/app/auth/callback` đọc được
   * `accessToken`/`refreshToken` từ URL fragment (`GoogleOAuth2SuccessHandler`).
   */
  completeOAuthLogin: (accessToken: string, refreshToken: string) => Promise<SessionUser>;

  /** Khôi phục phiên từ cookie httpOnly sau khi F5. */
  restoreSession: () => Promise<void>;

  /** UC-035 — revoke refresh token phía server rồi dọn sạch phía client. */
  logout: () => Promise<void>;

  /**
   * BR-009 — ghi nhận người dùng vừa chấp nhận điều khoản & chính sách bảo vệ dữ liệu cá nhân.
   * Gọi sau khi `POST /account/accept-terms` trả về mốc thời gian, để hạ cổng đồng ý ngay mà
   * không phải nạp lại `/profile/me`.
   */
  markTermsAccepted: (acceptedAt: string) => void;
}

/**
 * 13/08/2026 (Nam) — trang đích sau đăng nhập.
 *
 * Bình thường là `/{role}/dashboard`. NGOẠI LỆ: tài khoản `service-caterers` vừa
 * đăng ký (UC-101) mà trường CHƯA duyệt hợp tác (BR-EXCLUSIVE-CATERER) không có
 * `activeSchoolId` — mọi API của "Tổng quan vận hành bếp" (`/service-caterers/dashboard`)
 * đều lọc theo trường nên trả lỗi/rỗng hết, người dùng thấy màn hỏng ngay lúc vừa vào.
 * Đá thẳng qua trang hồ sơ (`/service-caterers/profile`) — nơi họ còn việc thật để làm
 * (nộp hồ sơ/tài liệu tuân thủ) trong lúc chờ trường duyệt.
 */
export function homeRouteFor(user: Pick<SessionUser, "role" | "activeSchoolId">): string {
  if (user.role === "service-caterers" && !user.activeSchoolId) {
    return "/service-caterers/profile";
  }
  return `/${user.role}/dashboard`;
}

function toSessionUser(p: ProfileResponse): SessionUser {
  return {
    id: p.id,
    name: p.name,
    email: p.email,
    role: p.role,
    school: p.school ?? undefined,
    schoolId: p.schoolId ?? undefined,
    partnerId: p.partnerId ?? undefined,
    activeSchoolId: p.activeSchoolId ?? undefined,
    activeSchool: p.activeSchool ?? undefined,
    avatarUrl: p.avatarUrl ?? undefined,
    phone: p.phone ?? undefined,
    status: p.status,
    termsAcceptedAt: p.termsAcceptedAt,
  };
}

export const useAuthStore = create<AuthState>((set, get) => ({
  user: null,
  status: "idle",
  needTermsAccept: false,
  sessionExpired: false,

  clearSessionExpired: () => set({ sessionExpired: false }),

  loginWithPassword: async (email, password) => {
    const { data } = await api.post<LoginResponse>("/auth/login", { email, password });

    setAccessToken(data.accessToken);
    await persistRefreshToken(data.refreshToken);

    /*
     * `LoginResponse` chỉ có role/schoolId/partnerId — KHÔNG có tên và email hiển
     * thị, nên lấy phiên đầy đủ từ `/profile/me` ngay sau khi đăng nhập.
     */
    const { data: profile } = await api.get<ProfileResponse>("/profile/me");

    const sessionUser = toSessionUser(profile);
    set({
      user: sessionUser,
      status: "authenticated",
      needTermsAccept: data.needTermsAccept,
      sessionExpired: false,
    });

    return sessionUser;
  },

  completeOAuthLogin: async (accessToken, refreshToken) => {
    setAccessToken(accessToken);
    await persistRefreshToken(refreshToken);

    /*
     * Fragment redirect (`GoogleOAuth2SuccessHandler`) chỉ mang accessToken/refreshToken
     * — KHÔNG có needTermsAccept như `LoginResponse` của đăng nhập thường. Suy ra từ
     * `termsAcceptedAt` (đúng logic backend dùng: null ⇒ chưa chấp nhận, BR-009).
     */
    const { data: profile } = await api.get<ProfileResponse>("/profile/me");

    const sessionUser = toSessionUser(profile);
    set({
      user: sessionUser,
      status: "authenticated",
      needTermsAccept: profile.termsAcceptedAt == null,
      sessionExpired: false,
    });

    return sessionUser;
  },

  restoreSession: async () => {
    set({ status: "loading" });

    const token = await refreshAccessToken();
    if (!token) {
      set({ user: null, status: "anonymous" });
      return;
    }

    try {
      const { data: profile } = await api.get<ProfileResponse>("/profile/me");
      /*
       * Suy `needTermsAccept` từ `termsAcceptedAt` y như luồng OAuth: khôi phục phiên sau F5
       * KHÔNG đi qua `/auth/login` nên không có cờ từ `LoginResponse`. Thiếu dòng này thì người
       * chưa chấp nhận điều khoản chỉ cần tải lại trang là thoát được cổng đồng ý (BR-009).
       */
      set({
        user: toSessionUser(profile),
        status: "authenticated",
        needTermsAccept: profile.termsAcceptedAt == null,
      });
    } catch (e) {
      /*
       * Có access token nhưng `/profile/me` vẫn hỏng ⇒ tài khoản bị vô hiệu hóa
       * giữa phiên, hoặc backend đang lỗi. Không giữ phiên nửa vời.
       */
      if (e instanceof ApiError) {
        console.warn(`[auth] không nạp được /profile/me: ${e.code ?? e.status} ${e.message}`);
      }
      setAccessToken(null);
      set({ user: null, status: "anonymous" });
    }
  },

  logout: async () => {
    try {
      // Cần access token còn sống ⇒ gọi TRƯỚC khi dọn. Idempotent phía backend.
      await api.post("/auth/logout", {});
    } catch {
      // Token đã hết hạn thì server cũng đã revoke — vẫn phải dọn phía client.
    }
    await clearRefreshToken();
    // Tự bấm Đăng xuất thì KHÔNG phải "phiên hết hạn" — đừng doạ người dùng ở màn login.
    set({ user: null, status: "anonymous", needTermsAccept: false, sessionExpired: false });
  },

  markTermsAccepted: (acceptedAt) => {
    const user = get().user;
    set({
      needTermsAccept: false,
      user: user ? { ...user, termsAcceptedAt: acceptedAt } : user,
    });
  },
}));

/*
 * Nối interceptor 401 của `client.ts` với store: khi refresh token chết, mọi màn
 * đọc `useAuthStore` phải thấy phiên biến mất ngay, không đợi request kế tiếp.
 */
registerSessionExpiredHandler(() => {
  /*
   * 08/08/2026 — thêm `sessionExpired`. Trước đó store có dọn phiên nhưng KHÔNG ai
   * đọc `status` để điều hướng: `(app)/layout.tsx` render thẳng `AppShell` không qua
   * cổng nào. Hệ quả đo được: refresh token bị revoke (đăng xuất ở máy khác, BR-004)
   * ⇒ mọi request 401 ⇒ màn Kho vẫn vẽ đủ khung và báo "Không có bản ghi nào", người
   * trực bếp tưởng mất sạch dữ liệu. Nay `AuthGate` chặn ở `AppShell`.
   */
  useAuthStore.setState({
    user: null,
    status: "anonymous",
    needTermsAccept: false,
    sessionExpired: true,
  });
});

/**
 * Lớp chủ nhiệm của giáo viên đang đăng nhập — nối theo `teacherId` (khoá ổn
 * định), KHÔNG so tên hiển thị (review/teacher — Đợt 0).
 *
 * Nối thật 06/08/2026: `GET /api/v1/classrooms` cho phép TEACHER gọi
 * (`@PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS',
 * 'MEDICAL_STAFF','TEACHER')")`, xem `ClassroomController`), và `ClassroomResponse`
 * có sẵn `teacherId` — không cần filter riêng phía server, đọc cả danh sách rồi
 * `.find()` phía client. Trước đây đọc hằng `CLASSROOMS` (seed mock, luôn rỗng)
 * nên mọi màn Teacher phụ thuộc "lớp của tôi" đều trống.
 *
 * Fix 27/07/2026: `useAuthStore` KHÔNG persist qua reload, nên vào thẳng URL
 * `/teacher/...` lúc chưa có phiên thì `user` là `null` — trả `undefined`, màn
 * gọi hàm này tự xử lý trạng thái "chưa có phiên" (AppShell đã chặn route trước
 * khi tới đây trong luồng bình thường).
 */
export function useTeacherClass(): ClassroomDto | undefined {
  const user = useAuthStore((s) => s.user);
  const { data } = usePagedList<ClassroomDto>("classrooms", { pageSize: 200 });
  if (!user?.id) return undefined;
  return (data?.items ?? []).find((c) => c.teacherId === user.id);
}

/**
 * Tên trường để HIỂN THỊ — dùng thay cho `user?.school` ở mọi tiêu đề/mô tả màn.
 *
 * Tài khoản đối tác (Đơn vị suất ăn) không có `schoolId` nên `school` luôn rỗng: màn Thông báo
 * của bếp hiện mô tả bắt đầu bằng "— ·" trong khi cùng màn đó ở vai Y tế/Giáo viên hiện đúng
 * tên trường (đo 08/08/2026). `activeSchool` do server suy từ hợp tác đang hiệu lực — xem
 * `common/scope/ActorScope` phía backend.
 */
export function useSchoolLabel(fallback = "—"): string {
  return useAuthStore((s) => s.user?.school ?? s.user?.activeSchool) ?? fallback;
}
