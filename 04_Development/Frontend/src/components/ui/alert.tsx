import * as React from "react";
import {
  AlertTriangle,
  CheckCircle2,
  Info,
  OctagonAlert,
  ShieldAlert,
} from "lucide-react";
import { cn } from "@/lib/utils";

type AlertVariant = "success" | "warning" | "danger" | "critical" | "info";

const variantConfig: Record<
  AlertVariant,
  { classes: string; icon: React.ReactNode }
> = {
  success: {
    classes: "bg-success-soft text-success border-success/30",
    icon: <CheckCircle2 className="size-4" />,
  },
  warning: {
    classes: "bg-warning-soft text-warning border-warning/30",
    icon: <AlertTriangle className="size-4" />,
  },
  danger: {
    classes: "bg-destructive-soft text-destructive border-destructive/30",
    icon: <OctagonAlert className="size-4" />,
  },
  critical: {
    classes: "bg-critical-soft text-critical border-critical/30",
    icon: <ShieldAlert className="size-4" />,
  },
  info: {
    classes: "bg-info-soft text-info border-info/30",
    icon: <Info className="size-4" />,
  },
};

export function Alert({
  className,
  variant = "info",
  title,
  children,
}: {
  className?: string;
  variant?: AlertVariant;
  title?: string;
  children?: React.ReactNode;
}) {
  const config = variantConfig[variant];
  return (
    <div
      role="alert"
      className={cn(
        "flex gap-3 rounded-lg border p-3 text-sm",
        config.classes,
        className
      )}
    >
      <span className="mt-0.5 shrink-0">{config.icon}</span>
      <div>
        {title && <p className="font-semibold">{title}</p>}
        {children && <div className="mt-0.5">{children}</div>}
      </div>
    </div>
  );
}
