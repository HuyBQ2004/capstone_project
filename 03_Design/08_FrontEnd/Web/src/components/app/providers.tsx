"use client";

import { useState } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { Toaster } from "sonner";
import { setupMockServer } from "@/lib/api/mock-server";

// Đăng ký mock adapter một lần duy nhất phía client (module scope).
let mockReady = false;
function ensureMockServer() {
  if (!mockReady) {
    setupMockServer();
    mockReady = true;
  }
}

export function Providers({ children }: { children: React.ReactNode }) {
  ensureMockServer();
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: { staleTime: 30_000, retry: 1, refetchOnWindowFocus: false },
        },
      })
  );

  return (
    <QueryClientProvider client={queryClient}>
      {children}
      <Toaster richColors position="top-right" />
    </QueryClientProvider>
  );
}
