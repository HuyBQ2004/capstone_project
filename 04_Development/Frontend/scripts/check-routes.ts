/**
 * Soi ĐƯỜNG ĐI GIỮA CÁC MÀN — hai chiều.
 *
 *   1. **Trang chết**: có `page.tsx` nhưng KHÔNG file nào dẫn tới ⇒ chỉ vào được bằng gõ tay URL.
 *   2. **Link gãy**: có link nhưng KHÔNG có route nào khớp ⇒ bấm vào ra 404.
 *
 * <p><b>Vì sao có script này.</b> 09/08/2026 Nam hỏi *"sao không có cách nào vào
 * `/service-caterers/menus/create`?"*. Đo ra: route đó tồn tại, được viết để mở từ lịch tuần kèm
 * `?grade=&date=&meal=`, nhưng link gọi nó đã mất trong một lần refactor — `grep` cho 0 kết quả.
 * Hệ quả: cả nhánh soạn thực đơn chi tiết (thành phần, định lượng, dinh dưỡng, AI Assist UC-075)
 * không ai vào được, dù code vẫn chạy tốt và `tsc`/`lint`/`build` đều sạch. Không công cụ nào
 * trong bộ 4 lệnh xác minh bắt được lớp lỗi này — nó không phải lỗi kiểu, không phải lỗi lint,
 * không phải lỗi build. Chỉ có đếm liên kết mới thấy.
 *
 * <p><b>Cách đọc kết quả.</b> Script CỐ Ý dè dặt: chỗ nào không chắc thì coi là ĐÃ NỐI, để
 * danh sách in ra không lẫn báo động giả. Vì vậy mỗi dòng in ra đáng để xem tận nơi.
 *
 * Chạy: `npm run check:routes`
 */
import { readFileSync, readdirSync, statSync } from "node:fs";
import { join, relative, sep } from "node:path";

const SRC = join(process.cwd(), "src");
const APP = join(SRC, "app");

/** Màn người dùng gõ thẳng/được hệ thống đưa tới, không cần ai link — không tính là chết. */
const ENTRY_POINTS = new Set([
  "/", // trang gốc
  "/login",
  "/register",
  "/forgot-password",
  "/reset-password",
  "/activate",
  // Đích của LINK TRONG EMAIL kích hoạt tài khoản — không màn nào trong app link tới, đúng thiết kế.
  "/activate-account",
  // Google trả về đây sau OAuth2 (Spring Security redirect), không phải người dùng bấm.
  "/auth/callback",
]);

function walk(dir: string, out: string[] = []): string[] {
  for (const name of readdirSync(dir)) {
    const p = join(dir, name);
    if (statSync(p).isDirectory()) walk(p, out);
    else out.push(p);
  }
  return out;
}

/** `src/app/(app)/service-caterers/menus/[id]/edit/page.tsx` → `/service-caterers/menus/[id]/edit` */
function routeOf(file: string): string | null {
  const rel = relative(APP, file).split(sep).join("/");
  if (!/(^|\/)page\.tsx$/.test(rel)) return null;
  const segs = rel
    .replace(/\/?page\.tsx$/, "")
    .split("/")
    .filter((s) => s && !/^\(.*\)$/.test(s)); // bỏ route group (app)/(auth)
  return "/" + segs.join("/");
}

/**
 * Thay mọi `${…}` bằng `*`, ĐẾM NGOẶC LỒNG NHAU.
 *
 * Regex `\$\{[^}]*\}` cắt sai ngay ở biểu thức có ngoặc/nháy bên trong, ví dụ
 * `` `/service-caterers/menus${qs ? `?${qs}` : ""}` `` — nó dừng ở dấu `}` đầu tiên và để lại
 * rác `${qs ? ` trong đường dẫn, rồi script báo "link gãy" cho một link hoàn toàn đúng.
 */
function stripInterpolations(p: string): string {
  let out = "";
  for (let i = 0; i < p.length; i++) {
    if (p[i] === "$" && p[i + 1] === "{") {
      let depth = 1;
      i += 2;
      while (i < p.length && depth > 0) {
        if (p[i] === "{") depth++;
        else if (p[i] === "}") depth--;
        i++;
      }
      i--;
      /*
       * `depth > 0` = chuỗi bị CẮT CỤT lúc bắt bằng regex (template lồng template có backtick
       * bên trong, vd. `` `/menus${qs ? `?${qs}` : ""}` ``). Phần còn lại không biết được ⇒ nối
       * "/*" để nó thành MỘT segment tự do, thay vì dính vào segment trước thành "menus*" rồi
       * bị báo là link gãy.
       */
      out += depth > 0 ? "/*" : "*";
    } else {
      out += p[i];
    }
  }
  return out;
}

/** Một tham chiếu link đã chuẩn hoá: `${x}` → `*` (khớp với mọi segment). */
function normalizeRef(raw: string): string[] | null {
  let p = raw.trim();
  if (!p.startsWith("/")) return null; // link ngoài / mailto / anchor
  // Bỏ nội suy TRƯỚC khi cắt query: `${qs ? "?x" : ""}` có dấu "?" nằm bên trong.
  p = stripInterpolations(p);
  /*
   * Còn sót `$`/`{`/`}` nghĩa là chuỗi đã bị CẮT CỤT lúc bắt bằng regex: template lồng template
   * (`` `/menus${qs ? `?${qs}` : ""}` ``) có backtick bên trong nên dấu đóng bị nhận nhầm. Từ chỗ
   * đó trở đi coi như không biết ⇒ `*`. Không làm vậy thì script báo "link gãy" cho một link
   * hoàn toàn đúng — đúng loại báo động giả khiến người ta bỏ qua cả công cụ.
   */
  const cut = p.search(/[${}]/);
  if (cut >= 0) p = p.slice(0, cut) + "/*";
  p = p.split("?")[0].split("#")[0];
  const segs = p.split("/").filter(Boolean);
  return segs;
}

function isDynamic(seg: string) {
  return seg.startsWith("[");
}

/**
 * Một tham chiếu có "phủ" một route không.
 *
 * <p>`*` (phần nội suy `${…}`) chỉ khớp segment ĐỘNG của route, KHÔNG khớp segment tĩnh.
 *
 * <p>Bản đầu cho `*` khớp mọi thứ cho "an toàn" — kết quả là **không phát hiện được gì**: chỉ
 * cần một chỗ viết `` `/school-admin/${x}` `` là mọi màn hai-segment dưới `school-admin` đều
 * được coi là đã nối. Thử bằng một trang mồ côi dựng tạm: script im lặng. Một công cụ luôn báo
 * "sạch" thì tệ hơn không có, vì nó tạo niềm tin sai.
 *
 * <p>Thực tế `` `${…}` `` gần như luôn là một **mã bản ghi** đi vào route động (`[id]`), nên
 * luật chặt này đúng với đại đa số. Đổi lại có thể lọt vài báo động giả khi ai đó ghép đường
 * dẫn tĩnh bằng biến — chấp nhận được, vì mỗi dòng in ra đều phải xem tận nơi.
 */
function refCoversRoute(ref: string[], route: string[]): boolean {
  if (ref.length !== route.length) return false;
  return ref.every((r, i) => (r === "*" ? isDynamic(route[i]) : isDynamic(route[i]) || r === route[i]));
}

const files = walk(SRC).filter((f) => /\.(ts|tsx)$/.test(f) && !f.endsWith(".d.ts"));

const routes = files
  .map((f) => ({ file: f, route: routeOf(f) }))
  .filter((r): r is { file: string; route: string } => r.route !== null);

interface Ref {
  segs: string[];
  raw: string;
  file: string;
}

/*
 * MỌI chuỗi bắt đầu bằng "/" đều được coi là một đường dẫn có thể điều hướng tới.
 *
 * Bản đầu chỉ bắt `href=`/`router.push(` và cho ra 22 "trang chết", trong đó phần lớn là BÁO
 * ĐỘNG GIẢ: các màn chi tiết được dẫn qua `rowHref={(c) => `/school-admin/classrooms/${c.id}`}`
 * — một prop của `DataTable`, không khớp mẫu nào. Đuổi theo từng tên prop là trò không có hồi
 * kết (`rowHref`, `detailHref`, `to`, một hàm trả về đường dẫn…). Quét mọi chuỗi đường dẫn thì
 * dè dặt hơn: có thể BỎ SÓT trang chết, nhưng thứ in ra hầu như luôn thật.
 *
 * Đường dẫn API (`/students`, `/ai/chat`…) cũng lọt vào đây, nhưng chúng không mang tiền tố vai
 * nên khác số segment với route màn hình — phép khớp theo từng segment tự loại chúng.
 */
const PATH_LITERAL = /["'`](\/[^"'`\n]*)["'`]/g;

const refs: Ref[] = [];
for (const file of files) {
  const text = readFileSync(file, "utf8");
  PATH_LITERAL.lastIndex = 0;
  let m: RegExpExecArray | null;
  while ((m = PATH_LITERAL.exec(text))) {
    const segs = normalizeRef(m[1]);
    if (segs) refs.push({ segs, raw: m[1], file: relative(process.cwd(), file) });
  }
}

/*
 * ── Nối lại đường dẫn ghép từ prop, ví dụ `AnnouncementList` ──
 *
 * `` <Link href={`${basePath}/${a.id}`}> `` không phân giải tĩnh được, nên 5 màn
 * `announcements/[id]` từng bị báo nhầm là chết. Nhưng GIÁ TRỊ của prop thì có trong code:
 * `<AnnouncementList basePath="/teacher/announcements" />`. Ghép hai mảnh lại là ra đường dẫn
 * thật — không cần đoán, cũng không cần nới lỏng phép khớp (thứ từng làm công cụ mù hoàn toàn).
 *
 * Chỉ nối khi CÓ THẬT một chỗ viết `` `${<tên prop>}/…` `` — không thì mọi chuỗi truyền qua prop
 * đều bị coi là đường dẫn.
 */
const PROP_JOIN = /`\$\{([A-Za-z_$][\w$]*)\}(\/[^`]*)`/g;
const joinedPropNames = new Set<string>();
for (const file of files) {
  const text = readFileSync(file, "utf8");
  PROP_JOIN.lastIndex = 0;
  let m: RegExpExecArray | null;
  while ((m = PROP_JOIN.exec(text))) joinedPropNames.add(m[1]);
}
for (const file of files) {
  const text = readFileSync(file, "utf8");
  for (const prop of joinedPropNames) {
    const re = new RegExp(`${prop}\\s*=\\s*["'\`](\\/[^"'\`\\n]+)["'\`]`, "g");
    let m: RegExpExecArray | null;
    while ((m = re.exec(text))) {
      const base = normalizeRef(m[1]);
      if (base) refs.push({ segs: [...base, "*"], raw: `${m[1]}/\${…}`, file: relative(process.cwd(), file) });
    }
  }
}

/** Chiều "link gãy" thì CHỈ xét link điều hướng thật — chuỗi API không phải route Next. */
const NAV_PATTERNS: RegExp[] = [
  /href\s*=\s*\{?\s*["'`]([^"'`\n]+)["'`]/g,
  /href:\s*["'`]([^"'`\n]+)["'`]/g, // cấu hình nav trong roles.ts
  /router\.(?:push|replace|prefetch)\(\s*["'`]([^"'`\n]+)["'`]/g,
  /redirect\(\s*["'`]([^"'`\n]+)["'`]/g,
];

const navRefs: Ref[] = [];
for (const file of files) {
  const text = readFileSync(file, "utf8");
  for (const re of NAV_PATTERNS) {
    re.lastIndex = 0;
    let m: RegExpExecArray | null;
    while ((m = re.exec(text))) {
      const segs = normalizeRef(m[1]);
      if (segs) navRefs.push({ segs, raw: m[1], file: relative(process.cwd(), file) });
    }
  }
}

/* ─────────────────────────── 1. Trang chết ─────────────────────────── */

/**
 * Màn `@deprecated` chỉ còn `router.replace()` sang chỗ mới — KHÔNG ai link tới là ĐÚNG thiết
 * kế: nó tồn tại để link cũ/bookmark cũ không rơi vào 404. Xếp riêng, đừng bắt người đọc nhận
 * ra điều đó lần nữa ở mỗi lần chạy.
 */
function isDeprecatedBridge(text: string): boolean {
  if (!/router\.(replace|push)\(/.test(text)) return false;
  if (/@deprecated/.test(text)) return true;
  /*
   * Không phải cầu nào cũng gắn `@deprecated`: `service-caterers/special-meals` chỉ ghi trong
   * Javadoc là "giữ route này làm cầu chuyển hướng". Dấu hiệu chắc hơn là màn KHÔNG render gì —
   * không tiêu đề, không bảng, không form. Một màn thật luôn có ít nhất một trong số đó.
   */
  return !/PageHeader|DataTable|SimpleTable|<Table|<Card|<form/.test(text);
}

const dead: { route: string; file: string }[] = [];
const bridges: { route: string; file: string }[] = [];
for (const { route, file } of routes) {
  if (ENTRY_POINTS.has(route)) continue;
  const segs = route.split("/").filter(Boolean);
  const selfFile = relative(process.cwd(), file);
  // Link TỪ CHÍNH màn đó tới nó không tính là đường vào.
  const covered = refs.some((r) => r.file !== selfFile && refCoversRoute(r.segs, segs));
  if (covered) continue;
  if (isDeprecatedBridge(readFileSync(file, "utf8"))) bridges.push({ route, file: selfFile });
  else dead.push({ route, file: selfFile });
}

/*
 * Điểm mù ĐÃ BIẾT: đường dẫn ghép từ một biến base, ví dụ `AnnouncementList` dựng
 * `` `${basePath}/${id}` ``. Chuỗi đó không bắt đầu bằng "/" nên không phân giải tĩnh được, và
 * mọi màn `announcements/[id]` của 5 vai bị báo nhầm là chết (đo 09/08/2026). In kèm
 * danh sách nguồn để người đọc biết ngay chỗ nào cần kiểm tay thay vì tin nhầm.
 */
const BASE_PATH_JOIN = /`\$\{[A-Za-z_$][\w$]*\}\//g;
const basePathJoins = files.filter((f) => BASE_PATH_JOIN.test(readFileSync(f, "utf8")));

/* ─────────────────────────── 2. Link gãy ─────────────────────────── */

const routeSegs = routes.map((r) => r.route.split("/").filter(Boolean));

/** Đường dẫn do BACKEND phục vụ, nginx định tuyến thẳng — không phải route của Next. */
const NON_NEXT_PREFIXES = ["/api/", "/media/", "/oauth2/", "/login/oauth2/", "/swagger-ui", "/v3/"];

const broken: Ref[] = [];
for (const r of navRefs) {
  if (r.segs.some((s) => s === "*")) continue; // nội suy — không kết luận được, bỏ qua
  const path = "/" + r.segs.join("/");
  if (ENTRY_POINTS.has(path)) continue;
  if (NON_NEXT_PREFIXES.some((p) => path.startsWith(p))) continue;
  if (!routeSegs.some((route) => refCoversRoute(r.segs, route))) broken.push(r);
}

/* ─────────────────────────── Báo cáo ─────────────────────────── */

console.log(
  `Đã quét ${routes.length} route · ${refs.length} chuỗi đường dẫn · ${navRefs.length} link điều hướng\n`
);

if (dead.length === 0) {
  console.log("✓ Không có trang chết — mọi màn đều có ít nhất một đường vào.");
} else {
  console.log(`✗ ${dead.length} TRANG CHẾT (chỉ vào được bằng gõ tay URL):`);
  for (const d of dead) console.log(`   ${d.route}\n      ${d.file}`);
  if (basePathJoins.length) {
    console.log(
      `\n   ⚠ Điểm mù: ${basePathJoins.length} file ghép đường dẫn từ biến (\`\${base}/…\`) nên` +
        ` không phân giải tĩnh được. Màn chi tiết mở từ các file dưới đây CÓ THỂ vẫn nối được —` +
        ` kiểm tay trước khi kết luận:`
    );
    for (const f of basePathJoins) console.log(`      ${relative(process.cwd(), f)}`);
  }
}

if (bridges.length) {
  console.log(`\n· ${bridges.length} cầu chuyển hướng @deprecated (không ai link tới là ĐÚNG):`);
  for (const b of bridges) console.log(`   ${b.route}`);
}

console.log("");

if (broken.length === 0) {
  console.log("✓ Không có link gãy.");
} else {
  const seen = new Set<string>();
  const uniq = broken.filter((b) => {
    const k = `${b.raw}|${b.file}`;
    if (seen.has(k)) return false;
    seen.add(k);
    return true;
  });
  console.log(`✗ ${uniq.length} LINK GÃY (bấm vào ra 404):`);
  for (const b of uniq) console.log(`   ${b.raw}\n      ${b.file}`);
}

process.exit(dead.length + broken.length > 0 ? 1 : 0);
