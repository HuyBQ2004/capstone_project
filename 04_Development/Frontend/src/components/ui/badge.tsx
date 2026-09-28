import * as React from "react";
import { cn } from "@/lib/utils";

/**
 * Soft badge pattern theo Design_System.md mục 3:
 * nền shade 100 + chữ shade 700 cùng họ màu — không dùng chữ trắng trên nền đậm.
 */
export type BadgeVariant =
  | "success"
  | "warning"
  | "danger"
  | "critical"
  | "info"
  | "neutral"
  | "primary";

const variantClasses: Record<BadgeVariant, string> = {
  success: "bg-success-soft text-success",
  warning: "bg-warning-soft text-warning",
  danger: "bg-destructive-soft text-destructive",
  critical: "bg-critical-soft text-critical",
  info: "bg-info-soft text-info",
  neutral: "bg-muted text-muted-foreground",
  primary: "bg-primary-soft text-primary",
};

export interface BadgeProps extends React.HTMLAttributes<HTMLSpanElement> {
  variant?: BadgeVariant;
}

export function Badge({
  className,
  variant = "neutral",
  ...props
}: BadgeProps) {
  return (
    <span
      className={cn(
        "inline-flex items-center gap-1 rounded-md px-2 py-0.5 text-xs font-medium whitespace-nowrap",
        variantClasses[variant],
        className
      )}
      {...props}
    />
  );
}
