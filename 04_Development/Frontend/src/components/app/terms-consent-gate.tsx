"use client";

import { useState } from "react";
import Link from "next/link";
import { FileText, Loader2, ShieldCheck } from "lucide-react";
import { useMutation } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { api } from "@/lib/api/client";
import { apiErrorMessage } from "@/lib/messages";
import {
  CONSENT_SUMMARY,
  PDPL_DECREE,
  PDPL_LAW,
  PRIVACY_POLICY_EFFECTIVE_DATE,
  PRIVACY_POLICY_VERSION,
} from "@/lib/privacy";
import { useAuthStore } from "@/lib/stores/auth-store";

/**
 * Cổng chấp nhận điều khoản & chính sách bảo vệ dữ liệu cá nhân — BR-009 / UC-034 (AF1).
 *
 * <p><b>Vì sao thêm 14/08/2026 (Nam).</b> Backend đã có đủ: `POST /account/accept-terms` ghi mốc
 * `terms_accepted_at`, `LoginResponse.needTermsAccept` báo cho FE biết ai chưa chấp nhận, và cả
 * hai đều có test. Store cũng đã giữ cờ `needTermsAccept` từ lúc đăng nhập. Nhưng KHÔNG màn nào
 * đọc cờ đó — `grep needTermsAccept` chỉ ra `auth-store.ts` và file schema sinh tự động. Hệ quả
 * đo được: `terms_accepted_at` của mọi tài khoản tạo qua giao diện đều là NULL, tức hệ thống
 * chưa từng lấy sự đồng ý của một người dùng nào, dù đường ống kỹ thuật đã dựng xong.
 *
 * <p>Theo {@link PDPL_LAW}, sự đồng ý phải rõ ràng và <i>sự im lặng không được coi là đồng ý</i>.
 * Vì vậy cổng này CỐ Ý không đóng được bằng phím Esc, bấm ra ngoài hay nút X: chỉ có hai lối ra
 * là chấp nhận, hoặc đăng xuất. Ô tích cũng cố ý mặc định TẮT — tích sẵn rồi cho bấm tiếp thì
 * mốc thời gian lưu vào DB không chứng minh được điều gì.
 *
 * <p>Không dùng `ui/dialog` vì `DialogContent` luôn kèm nút X đóng và cho phép đóng bằng Esc —
 * đúng thứ cổng này phải chặn.
 */
export function TermsConsentGate() {
  const needTermsAccept = useAuthStore((s) => s.needTermsAccept);
  const markTermsAccepted = useAuthStore((s) => s.markTermsAccepted);
  const logout = useAuthStore((s) => s.logout);
  const [checked, setChecked] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const accept = useMutation({
    mutationFn: async () => {
      const { data } = await api.post<{ termsAcceptedAt: string }>("/account/accept-terms", {});
      return data;
    },
    onSuccess: (data) => markTermsAccepted(data.termsAcceptedAt),
    onError: (e) => setError(apiErrorMessage(e, "Không ghi nhận được sự đồng ý — thử lại sau.")),
  });

  if (!needTermsAccept) return null;

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="terms-consent-title"
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4"
    >
      <div className="flex max-h-[90vh] w-full max-w-xl flex-col overflow-hidden rounded-xl border bg-card shadow-lg">
        <div className="flex items-start gap-3 border-b p-5">
          <div className="flex size-10 shrink-0 items-center justify-center rounded-lg bg-primary-soft text-primary">
            <ShieldCheck className="size-5" />
          </div>
          <div>
            <h2 id="terms-consent-title" className="text-lg font-semibold">
              Đồng ý xử lý dữ liệu cá nhân
            </h2>
            <p className="mt-0.5 text-sm text-muted-foreground">
              Trước khi bắt đầu sử dụng hệ thống, bạn cần đọc và chấp nhận Chính sách bảo vệ dữ
              liệu cá nhân — ban hành theo {PDPL_LAW} và {PDPL_DECREE}.
            </p>
          </div>
        </div>

        <div className="flex-1 space-y-4 overflow-y-auto p-5 text-sm">
          <dl className="space-y-3">
            {CONSENT_SUMMARY.map((item) => (
              <div key={item.label}>
                <dt className="font-medium">{item.label}</dt>
                <dd className="text-muted-foreground">{item.detail}</dd>
              </div>
            ))}
          </dl>

          <Link
            href="/privacy"
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-2 rounded-lg border p-3 text-sm hover:bg-muted"
          >
            <FileText className="size-4 shrink-0 text-primary" />
            <span>
              Đọc toàn văn Chính sách bảo vệ dữ liệu cá nhân
              <span className="block text-xs text-muted-foreground">
                Phiên bản {PRIVACY_POLICY_VERSION} · hiệu lực từ {PRIVACY_POLICY_EFFECTIVE_DATE} ·
                mở ở tab mới
              </span>
            </span>
          </Link>

          <label className="flex cursor-pointer items-start gap-2.5 rounded-lg border p-3">
            <input
              type="checkbox"
              checked={checked}
              onChange={(e) => setChecked(e.target.checked)}
              className="mt-0.5 size-4 shrink-0"
            />
            <span>
              Tôi đã đọc và đồng ý với Chính sách bảo vệ dữ liệu cá nhân phiên bản{" "}
              {PRIVACY_POLICY_VERSION}. Tôi hiểu mình có quyền rút lại sự đồng ý bất cứ lúc nào.
            </span>
          </label>

          {error && <p className="text-sm text-destructive">{error}</p>}
        </div>

        <div className="flex flex-wrap items-center justify-end gap-2 border-t bg-muted/40 p-4">
          <Button variant="outline" onClick={() => logout()} disabled={accept.isPending}>
            Đăng xuất
          </Button>
          <Button onClick={() => accept.mutate()} disabled={!checked || accept.isPending}>
            {accept.isPending && <Loader2 className="animate-spin" />}
            Tôi đồng ý
          </Button>
        </div>
      </div>
    </div>
  );
}
