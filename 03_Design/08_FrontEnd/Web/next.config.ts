import type { NextConfig } from "next";
import path from "path";

const nextConfig: NextConfig = {
  // Có 2 lockfile (thư mục cha 08_FrontEnd và Web/) khiến Next suy nhầm workspace
  // root → cảnh báo + rủi ro tracing sai khi build. Ghim root về chính thư mục Web
  // này (review L2).
  outputFileTracingRoot: path.join(__dirname),
};

export default nextConfig;
