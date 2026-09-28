"""Map: role folder -> resources it can actually reach, following local imports."""
import os, re, json, collections

SRC = r"C:\Users\phuoc\Downloads\SEP490_G8\04_Development\Frontend\src"

hook_pat = re.compile(
    r'use(PagedList|PlainList|Single|Detail|SubDetail|Create|Update|Patch|PatchAction'
    r'|Action|Remove|UploadMutation|SubList)\s*<[^;]{0,400}?>\s*\(\s*"([^"]+)"'
)
imp_pat = re.compile(r'from\s+"@/([^"]+)"')
role_pat = re.compile(r'app[\\/]\(app\)[\\/]([^\\/]+)[\\/]')

files = {}
for root, _d, fns in os.walk(SRC):
    for fn in fns:
        if fn.endswith((".ts", ".tsx")):
            p = os.path.join(root, fn)
            try:
                files[p] = open(p, encoding="utf-8").read()
            except Exception:
                pass


def resolve(spec):
    base = os.path.join(SRC, spec.replace("/", os.sep))
    for cand in (base + ".tsx", base + ".ts",
                 os.path.join(base, "index.tsx"), os.path.join(base, "index.ts")):
        if cand in files:
            return cand
    return None


own = {p: set(r for _h, r in hook_pat.findall(s)) for p, s in files.items()}
imports = {p: [q for q in (resolve(m) for m in imp_pat.findall(s)) if q] for p, s in files.items()}


def reach(p, seen):
    if p in seen:
        return set()
    seen.add(p)
    out = set(own.get(p, ()))
    for q in imports.get(p, ()):
        out |= reach(q, seen)
    return out


by_role = collections.defaultdict(set)
for p in files:
    m = role_pat.search(p)
    if not m:
        continue
    by_role[m.group(1)] |= reach(p, set())

json.dump({k: sorted(v) for k, v in sorted(by_role.items())},
          open("by_role2.json", "w", encoding="utf-8"), ensure_ascii=False, indent=1)
for k, v in sorted(by_role.items()):
    print("%-18s %3d" % (k, len(v)))
