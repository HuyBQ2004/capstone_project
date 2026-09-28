import { Badge, type BadgeVariant } from "@/components/ui/badge";
import { STATUS_BADGE } from "@/lib/mock-data";

/**
 * `label`/`variant` override nhãn & màu chuẩn của STATUS_BADGE cho ngữ cảnh
 * từng màn — VD PRICE_ACCEPTED phía SC là "Đã chốt giá — chờ giao" còn phía
 * Supplier là "— cần giao": cùng một trạng thái nhưng hai vai nhìn hai việc.
 */
export function StatusBadge({
  status,
  label,
  variant,
}: {
  status: string;
  label?: string;
  variant?: BadgeVariant;
}) {
  const config = STATUS_BADGE[status];
  if (!config) return <Badge variant={variant ?? "neutral"}>{label ?? status}</Badge>;
  return <Badge variant={variant ?? config.variant}>{label ?? config.label}</Badge>;
}
