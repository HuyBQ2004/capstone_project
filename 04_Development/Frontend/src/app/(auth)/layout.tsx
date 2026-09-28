import Link from "next/link";
import { ShieldCheck } from "lucide-react";

export default function AuthLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-muted p-4">
      <div className="mb-6 flex items-center gap-3">
        <div className="flex size-12 items-center justify-center rounded-xl bg-primary text-primary-foreground">
          <ShieldCheck className="size-7" />
        </div>
        <div className="leading-tight">
          <p className="text-xl font-bold">SFMS</p>
          <p className="text-sm text-muted-foreground">
            Hệ thống hỗ trợ quản lý an toàn thực phẩm học đường
          </p>
        </div>
      </div>
      {children}
      {/*
        Liên kết chính sách phải có mặt TRƯỚC khi đăng nhập/đăng ký: người chưa có tài khoản vẫn
        có quyền biết hệ thống xử lý dữ liệu của mình thế nào (Điều 7 Chính sách, Luật 91/2025).
      */}
      <p className="mt-8 flex flex-wrap items-center justify-center gap-x-2 gap-y-1 text-xs text-muted-foreground">
        <Link href="/privacy" className="hover:text-foreground hover:underline">
          Chính sách bảo vệ dữ liệu cá nhân
        </Link>
        <span aria-hidden>·</span>
        <Link href="/privacy/consent" className="hover:text-foreground hover:underline">
          Mẫu văn bản đồng ý
        </Link>
      </p>
      <p className="mt-2 text-xs text-muted-foreground">
        SEP490 – G8 · School Food Safety Management Support System · 2026
      </p>
    </div>
  );
}
