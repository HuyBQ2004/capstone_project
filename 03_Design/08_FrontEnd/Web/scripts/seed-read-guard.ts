/**
 * CHẶN MÀN HÌNH ĐỌC THẲNG MẢNG SEED
 *
 * Chạy: npm run audit:seed
 *
 * `db.ts` dựng mỗi bảng bằng `clone(SEED)` — một BẢN SAO. Màn hình nào đọc thẳng
 * mảng seed thì thấy dữ liệu của lúc nạp module, vĩnh viễn không đổi: sửa gì qua
 * API cũng không hiện ra. Không lỗi nào báo, không cảnh báo nào bật, màn hình vẫn
 * render — chỉ là render dữ liệu chết.
 *
 * Đây là loại lỗi đã ăn ba lần:
 *   · tick cách ly lô xong ô tick tự bật lại (đọc `INVENTORY` seed)
 *   · FEFO vẫn phân bổ lô đang bị cách ly, phá thẳng BR-149
 *   · bếp không thấy lô Y tế vừa khoá cho tới khi tải lại trang
 *
 * Cách đúng: `usePagedList<T>("resource")` trong component, hoặc nhận dữ liệu qua
 * tham số nếu là hàm thuần. Import KIỂU (`type X`) thì vẫn tự do.
 */
import { readFileSync, readdirSync, statSync } from "fs";
import { join } from "path";

const dbSrc = readFileSync("src/lib/api/db.ts", "utf8");
const seedToResource = new Map<string, string>();
for (const m of dbSrc.matchAll(/"?([\w-]+)"?:\s*clone\(\s*([A-Z_][A-Z0-9_]*)/g)) {
  seedToResource.set(m[2], m[1]);
}

function walk(dir: string): string[] {
  return readdirSync(dir).flatMap((f) => {
    const p = join(dir, f);
    if (statSync(p).isDirectory()) return walk(p);
    return /\.tsx?$/.test(f) ? [p.replace(/\\/g, "/")] : [];
  });
}

const offenders: string[] = [];
for (const file of [...walk("src/app"), ...walk("src/components")]) {
  const src = readFileSync(file, "utf8");
  // bỏ chú thích để không bắt nhầm tên seed nhắc trong lời giải thích
  const code = src
    .split("\n")
    .filter((l) => {
      const t = l.trim();
      return !t.startsWith("*") && !t.startsWith("//") && !t.startsWith("/*");
    })
    .join("\n");
  for (const [seed, resource] of seedToResource) {
    // `type X` và `typeof X` không phải đọc dữ liệu
    const re = new RegExp(String.raw`(?<!typeof\s)(?<![\w.])${seed}(?![\w])`, "g");
    const hits = [...code.matchAll(re)].length;
    if (hits > 0) offenders.push(`${file}: ${seed} (${hits} chỗ) — dùng usePagedList("${resource}")`);
  }
}

if (offenders.length === 0) {
  console.log(`✔ ${seedToResource.size} bảng seed, không màn hình nào đọc thẳng.`);
  process.exit(0);
}
console.log("✘ Màn hình đang đọc mảng seed thay vì đọc qua API:\n");
for (const o of offenders) console.log("   " + o);
console.log(`\n${offenders.length} chỗ. Xem đầu file này để biết vì sao đây là lỗi.`);
process.exit(1);
