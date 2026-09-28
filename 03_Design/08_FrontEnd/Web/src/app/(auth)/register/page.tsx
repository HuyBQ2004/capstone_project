import Link from "next/link";
import { Building2, LogIn, ShieldCheck, Truck, UtensilsCrossed } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";

/**
 * Guest Portal — 13_Guest_Common_Screen_Flow.md mục 1 (Report 3 §3.3.2.1.1).
 * Logo + Welcome Text + 3 nút Register (School/Supplier/Caterer) + nút Login.
 * Hồ sơ đăng ký ở trạng thái Pending chờ System Admin/School Administration duyệt.
 */
export default function GuestPortalPage() {
  const options = [
    {
      href: "/register/school",
      icon: Building2,
      title: "Đăng ký Trường học",
      description: "Trường đăng ký sử dụng SFMS — chờ System Admin phê duyệt (UC-070)",
    },
    {
      href: "/register/supplier",
      icon: Truck,
      title: "Đăng ký Nhà cung cấp",
      description: "Nhà cung cấp nguyên liệu nộp hồ sơ + chứng nhận ATTP (UC-071)",
    },
    {
      href: "/register/caterer",
      icon: UtensilsCrossed,
      title: "Đăng ký Đơn vị Suất ăn",
      description: "Service Caterers nộp hồ sơ năng lực để hợp tác với trường",
    },
  ];

  return (
    <Card className="w-full max-w-lg">
      <CardHeader className="items-center text-center">
        <div className="mb-2 flex size-12 items-center justify-center rounded-xl bg-primary text-primary-foreground">
          <ShieldCheck className="size-6" />
        </div>
        <CardTitle>Chào mừng đến với SFMS</CardTitle>
        <CardDescription>
          Hệ thống quản lý an toàn thực phẩm học đường — chọn loại tài khoản để
          đăng ký, hoặc đăng nhập nếu đã có tài khoản.
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-3">
        {options.map((o) => (
          <Link
            key={o.href}
            href={o.href}
            className="flex items-center gap-3 rounded-xl border p-4 transition-colors hover:border-primary hover:bg-primary-soft"
          >
            <div className="flex size-10 shrink-0 items-center justify-center rounded-lg bg-primary-soft text-primary">
              <o.icon className="size-5" />
            </div>
            <div className="leading-tight">
              <p className="text-sm font-medium">{o.title}</p>
              <p className="text-xs text-muted-foreground">{o.description}</p>
            </div>
          </Link>
        ))}

        <Link href="/login" className="block pt-1">
          <Button variant="outline" className="w-full">
            <LogIn /> Đã có tài khoản? Đăng nhập
          </Button>
        </Link>
      </CardContent>
    </Card>
  );
}
