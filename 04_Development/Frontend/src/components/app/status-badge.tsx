import { Badge, type BadgeVariant } from "@/components/ui/badge";
import { STATUS_BADGE } from "@/lib/domain-utils";

/** `label`/`variant` override nhãn & màu chuẩn của STATUS_BADGE khi ngữ cảnh màn hình cần khác đi. */
export function StatusBadge({
  status,
  label,
  variant,
}: {
  /**
   * Nhận cả `undefined`: mọi field của DTO sinh từ swagger đều optional (springdoc không
   * khai `required`), nên trang đọc thẳng DTO không có gì để `?? ""` cho tử tế — rơi về
   * badge trung tính là hành vi đúng, không phải lỗi cần ép nơi gọi tự xử.
   */
  status: string | undefined;
  label?: string;
  variant?: BadgeVariant;
}) {
  const config = status ? STATUS_BADGE[status] : undefined;
  if (!config) return <Badge variant={variant ?? "neutral"}>{label ?? status ?? "—"}</Badge>;
  return <Badge variant={variant ?? config.variant}>{label ?? config.label}</Badge>;
}
