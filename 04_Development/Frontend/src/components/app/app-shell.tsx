import { AiChatWidget } from "./ai-chat-widget";
import { AuthGate } from "./auth-gate";
import { Sidebar } from "./sidebar";
import { TermsConsentGate } from "./terms-consent-gate";
import { Topbar } from "./topbar";

/**
 * `AuthGate` bọc CẢ Sidebar/Topbar chứ không chỉ `children`: chuông thông báo ở
 * Topbar cũng là một query, và nó nằm trong nhóm 7 request từng bắn trước khi có
 * token (P2-9). Bọc cả khung thì màn chờ cũng không loé ra bộ khung rỗng khi phiên
 * đã chết — đúng thứ khiến người dùng tưởng dữ liệu biến mất (P0-2).
 */
export function AppShell({ children }: { children: React.ReactNode }) {
  return (
    <AuthGate>
      <div className="min-h-screen">
        <Sidebar />
        <div className="lg:pl-64">
          <Topbar />
          <main className="mx-auto max-w-7xl p-4 sm:p-6">{children}</main>
        </div>
        {/* Trợ lý AI nổi — dùng được ở MỌI màn, không phải rời việc đang làm để đi hỏi. */}
        <AiChatWidget />
        {/*
          BR-009 — phủ lên toàn bộ khung làm việc cho tới khi người dùng chấp nhận chính sách bảo
          vệ dữ liệu cá nhân. Đặt trong `AuthGate` vì chỉ hỏi được người đã có phiên.
        */}
        <TermsConsentGate />
      </div>
    </AuthGate>
  );
}
