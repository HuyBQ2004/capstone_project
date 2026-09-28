"use client";

import { useState } from "react";
import { PlugZap, Save } from "lucide-react";
import { toast } from "sonner";
import { usePagedList, useUpdate } from "@/lib/api/hooks";
import type { ExternalIntegration } from "@/lib/mock-data";
import { PageHeader } from "@/components/app/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import { Input, Label } from "@/components/ui/input";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";

/**
 * 16_AISettings.md — UC-040 Manage External Integrations.
 * 1 màn hình dạng Tabs cho 4 dịch vụ: GPS / Gmail SMTP / Firebase FCM /
 * Gemini AI ("AI Settings" trong Screen Authorization là tab Gemini AI).
 * BR-163: bắt buộc Kiểm tra kết nối thành công trước khi Save (AF1: thất bại →
 * hiển thị lỗi chi tiết, chặn lưu) · BR-164: credentials mã hóa khi lưu.
 */

/*
 * Danh sách dịch vụ ngoài trước 22/07/2026 tồn tại HAI BẢN: một ở đây, một ở màn
 * "Trạng thái hệ thống" — và chúng nói ngược nhau về Gemini (một bên chưa kết nối,
 * một bên đang chạy). Nay cả hai đọc chung resource `integrations`.
 */
function ServicePanel({ svc }: { svc: ExternalIntegration }) {
  const update = useUpdate<{ id: string } & Partial<ExternalIntegration>>("integrations");
  const [endpoint, setEndpoint] = useState(svc.endpoint);
  const [apiKey, setApiKey] = useState("");
  const [testState, setTestState] = useState<"idle" | "testing" | "ok" | "fail">(
    svc.connected ? "ok" : "idle"
  );

  function testConnection() {
    if (!endpoint.trim() || !apiKey.trim()) {
      setTestState("fail");
      return;
    }
    setTestState("testing");
    setTimeout(() => {
      // Mock: key chứa "invalid" → thất bại (AF1 UC-040)
      const ok = !apiKey.toLowerCase().includes("invalid");
      setTestState(ok ? "ok" : "fail");
      if (ok) toast.success(`${svc.label}: Test connection thành công (BR-163)`);
    }, 600);
  }

  function save() {
    // Lưu THẬT: trạng thái kết nối ở đây là thứ màn Trạng thái hệ thống hiển thị và
    // là điều kiện để bếp dùng được gợi ý thực đơn AI.
    update.mutate(
      {
        id: svc.id,
        endpoint: endpoint.trim(),
        connected: true,
        statusNote: undefined,
        lastTestedAt: new Date().toLocaleString("vi-VN"),
      },
      {
        onSuccess: () =>
          toast.success(`${svc.label}: đã lưu cấu hình — credentials được mã hóa (BR-164), ghi audit log (BR-162)`),
      }
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center justify-between">
          <span>{svc.label}</span>
          <Badge variant={testState === "ok" ? "success" : "danger"}>
            {testState === "ok" ? "Đã kết nối" : "Chưa kết nối"}
          </Badge>
        </CardTitle>
      </CardHeader>
      <CardContent className="max-w-xl space-y-4">
        <p className="text-sm text-muted-foreground">{svc.purpose}</p>
        {testState === "fail" && (
          <Alert variant="danger" title="Kiểm tra kết nối thất bại (AF1 UC-040)">
            Invalid API key hoặc endpoint không phản hồi — kiểm tra lại thông tin;
            nút Save bị chặn cho tới khi test thành công (BR-163).
          </Alert>
        )}
        <div className="space-y-1.5">
          <Label htmlFor={`${svc.id}-endpoint`}>API Endpoint *</Label>
          <Input
            id={`${svc.id}-endpoint`}
            value={endpoint}
            placeholder={svc.endpointPlaceholder}
            onChange={(e) => {
              setEndpoint(e.target.value);
              setTestState("idle");
            }}
          />
        </div>
        <div className="space-y-1.5">
          <Label htmlFor={`${svc.id}-key`}>OAuth2 Client Credentials / API Key * (mã hóa khi lưu — BR-164)</Label>
          <Input
            id={`${svc.id}-key`}
            type="password"
            value={apiKey}
            placeholder="••••••••••••"
            onChange={(e) => {
              setApiKey(e.target.value);
              setTestState("idle");
            }}
          />
        </div>
        <div className="flex gap-2 border-t pt-4">
          <Button variant="outline" disabled={testState === "testing"} onClick={testConnection}>
            <PlugZap /> Kiểm tra kết nối (BR-163)
          </Button>
          <Button disabled={testState !== "ok" || update.isPending} onClick={save}>
            <Save /> Lưu
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}

export default function IntegrationsPage() {
  const { data, isLoading } = usePagedList<ExternalIntegration>("integrations", { pageSize: 20 });
  const services = data?.items ?? [];
  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  return (
    <>
      <PageHeader
        title="Tích hợp dịch vụ ngoài & AI"
        description="UC-040 — cấu hình GPS / Gmail SMTP / Firebase FCM / Gemini AI; Save chỉ mở khi Kiểm tra kết nối thành công (BR-163); credentials mã hóa (BR-164)"
      />
      <Tabs defaultValue="gemini">
        <TabsList>
          {services.map((s) => (
            <TabsTrigger key={s.id} value={s.id}>
              {s.label}
            </TabsTrigger>
          ))}
        </TabsList>
        {services.map((s) => (
          <TabsContent key={s.id} value={s.id} className="mt-4">
            <ServicePanel svc={s} />
          </TabsContent>
        ))}
      </Tabs>
    </>
  );
}
