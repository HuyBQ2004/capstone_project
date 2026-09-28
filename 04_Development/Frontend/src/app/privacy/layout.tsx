import Link from "next/link";
import { ShieldCheck } from "lucide-react";

/**
 * Khung cho khối trang bảo vệ dữ liệu cá nhân — CÔNG KHAI, không qua `AuthGate`.
 *
 * <p>Đặt ngoài nhóm route `(app)` là có chủ đích: theo Điều 7 của chính sách, chủ thể dữ liệu có
 * quyền được biết hệ thống xử lý dữ liệu của mình như thế nào TRƯỚC khi tạo tài khoản — bắt đăng
 * nhập rồi mới cho đọc chính sách thì quyền đó thành vô nghĩa. Cha mẹ học sinh chưa có tài khoản
 * cũng phải mở được liên kết này.
 */
export default function PrivacyLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen bg-muted">
      <header className="border-b bg-background print:hidden">
        <div className="mx-auto flex max-w-4xl items-center gap-3 px-4 py-4">
          <Link href="/login" className="flex items-center gap-3">
            <div className="flex size-10 items-center justify-center rounded-xl bg-primary text-primary-foreground">
              <ShieldCheck className="size-6" />
            </div>
            <div className="leading-tight">
              <p className="font-bold">SFMS</p>
              <p className="text-xs text-muted-foreground">
                Hệ thống hỗ trợ quản lý an toàn thực phẩm học đường
              </p>
            </div>
          </Link>
          <nav className="ml-auto flex items-center gap-4 text-sm">
            <Link href="/privacy" className="text-muted-foreground hover:text-foreground">
              Chính sách
            </Link>
            <Link href="/privacy/consent" className="text-muted-foreground hover:text-foreground">
              Mẫu đồng ý
            </Link>
            <Link href="/login" className="font-medium text-primary hover:underline">
              Đăng nhập
            </Link>
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-4xl px-4 py-8 print:max-w-none print:px-0 print:py-0">
        {children}
      </main>
    </div>
  );
}
