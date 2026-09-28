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
      <p className="mt-8 text-xs text-muted-foreground">
        SEP490 – G8 · School Food Safety Management Support System · 2026
      </p>
    </div>
  );
}
