# Multi-stage — build Next.js, runtime chỉ copy output "standalone" (server.js + node_modules
# tối thiểu tự trace được), không copy toàn bộ node_modules/src vào image chạy.
# 11/08/2026 — chạm file để kích hoạt lần build CI/CD đầu tiên thật sự.
FROM node:20-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
# SFMS_BACKEND_URL không cần ở build time (next.config.ts đọc process.env lúc SERVER START,
# không phải lúc build) — build không cần biết backend ở đâu.
RUN npm run build

FROM node:20-alpine AS runtime
WORKDIR /app
ENV NODE_ENV=production

COPY --from=build /app/public ./public
COPY --from=build /app/.next/standalone ./
COPY --from=build /app/.next/static ./.next/static

EXPOSE 3000
CMD ["node", "server.js"]
