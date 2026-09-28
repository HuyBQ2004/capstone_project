"use client";

/**
 * 16_ViewSchoolPolicyScreen.md — UC-137 View School Policy (Medical Staff).
 * BR-022: chỉ xem chính sách của trường mình.
 * Web: danh sách bên trái + panel nội dung bên phải.
 */

import { useState } from "react";
import { FileText } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { SCHOOL_POLICIES, CURRENT_SCHOOL } from "@/lib/mock-data";
import { cn } from "@/lib/utils";

export default function SchoolPolicyPage() {
  const [selectedId, setSelectedId] = useState(SCHOOL_POLICIES[0]?.id);
  const selected = SCHOOL_POLICIES.find((p) => p.id === selectedId);

  return (
    <>
      <PageHeader
        title="Chính sách trường học (UC-137)"
        description={`${CURRENT_SCHOOL.name} — chỉ hiển thị chính sách của trường bạn (BR-022)`}
      />

      {SCHOOL_POLICIES.length === 0 ? (
        <Card className="p-10 text-center text-muted-foreground">
          Trường chưa cập nhật chính sách nào.
        </Card>
      ) : (
        <div className="grid gap-4 lg:grid-cols-3">
          <div className="space-y-2">
            {SCHOOL_POLICIES.map((p) => (
              <button
                key={p.id}
                onClick={() => setSelectedId(p.id)}
                className={cn(
                  "w-full rounded-lg border p-3 text-left text-sm transition-colors",
                  selectedId === p.id
                    ? "border-primary bg-primary-soft"
                    : "bg-card hover:bg-muted"
                )}
              >
                <div className="mb-1 flex items-center gap-2">
                  <FileText className="size-4 shrink-0 text-muted-foreground" />
                  <span className="font-medium">{p.title}</span>
                </div>
                <div className="flex items-center gap-2 text-xs text-muted-foreground">
                  <Badge variant="neutral">{p.category}</Badge>
                  Ban hành {p.issuedAt}
                </div>
              </button>
            ))}
          </div>

          <Card className="lg:col-span-2">
            {selected && (
              <>
                <CardHeader>
                  <CardTitle>{selected.title}</CardTitle>
                  <p className="text-xs text-muted-foreground">
                    {selected.category} · Ban hành {selected.issuedAt} · {selected.issuedBy} · Chỉ đọc
                  </p>
                </CardHeader>
                <CardContent>
                  <div className="whitespace-pre-line text-sm leading-6">{selected.content}</div>
                </CardContent>
              </>
            )}
          </Card>
        </div>
      )}
    </>
  );
}
