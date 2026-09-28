"use client";

import { useState } from "react";
import { PlugZap, Save } from "lucide-react";
import { toast } from "sonner";
import { useAction, usePlainList, useUpdate } from "@/lib/api/hooks";
import type { ConnectionTestResultDto, IntegrationDto } from "@/lib/api/types";
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
 *
 * Nối thật 04/08/2026 — `IntegrationController` (BR-163/164, MF12-TC-CON-008):
 * `connected` là field DẪN XUẤT, server chỉ bật khi PUT kèm `apiKey` không rỗng VÀ lần
 * test gần nhất (≤10 phút) `passed=true` — client KHÔNG còn tự gán `connected:true` như
 * bản mock. 2 bước Test/Save tách hẳn thành 2 lệnh gọi thật: `POST /{code}/test-connection`
 * rồi `PUT /{code}`.
 */
function ServicePanel({ svc }: { svc: IntegrationDto }) {
  const testConnection = useAction<ConnectionTestResultDto>("integrations", { handledError: true });
  const save = useUpdate<{ id: string; endpoint: string; apiKey: string }, IntegrationDto>("integrations");
  const [endpoint, setEndpoint] = useState(svc.endpoint ?? "");
  const [apiKey, setApiKey] = useState("");
  const [testState, setTestState] = useState<"idle" | "testing" | "ok" | "fail">(
    svc.connected ? "ok" : "idle"
  );

  function runTest() {
    if (!endpoint.trim() || !apiKey.trim()) {
      setTestState("fail");
      return;
    }
    setTestState("testing");
    testConnection.mutate(
      { id: svc.id ?? "", action: "test-connection", body: { endpoint: endpoint.trim(), apiKey } },
      {
        onSuccess: (result) => {
          setTestState(result.passed ? "ok" : "fail");
          if (result.passed) toast.success(`${svc.label}: Test connection thành công (BR-163)`);
        },
        onError: () => setTestState("fail"),
      }
    );
  }

  function runSave() {
    save.mutate(
      { id: svc.id ?? "", endpoint: endpoint.trim(), apiKey },
      {
        onSuccess: () => {
          toast.success(`${svc.label}: đã lưu cấu hình — credentials được mã hóa (BR-164), ghi audit log (BR-162)`);
          setApiKey("");
        },
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
          <Button variant="outline" disabled={testState === "testing"} onClick={runTest}>
            <PlugZap /> Kiểm tra kết nối (BR-163)
          </Button>
          <Button disabled={testState !== "ok" || save.isPending} onClick={runSave}>
            <Save /> Lưu
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}

export default function IntegrationsPage() {
  const { data: services = [], isLoading } = usePlainList<IntegrationDto>("integrations");
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
            <TabsTrigger key={s.id} value={s.id ?? ""}>
              {s.label}
            </TabsTrigger>
          ))}
        </TabsList>
        {services.map((s) => (
          <TabsContent key={s.id} value={s.id ?? ""} className="mt-4">
            <ServicePanel svc={s} />
          </TabsContent>
        ))}
      </Tabs>
    </>
  );
}
