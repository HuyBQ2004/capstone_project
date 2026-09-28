import os, re, json, collections

SRC = r"C:\Users\phuoc\Downloads\SEP490_G8\04_Development\Frontend\src"

pat = re.compile(
    r'use(PagedList|PlainList|Single|Detail|SubDetail|Create|Update|Patch|PatchAction'
    r'|Action|Remove|UploadMutation|SubList)\s*<[^;]{0,400}?>\s*\(\s*"([^"]+)"'
)
role_pat = re.compile(r'app[\\/]\(app\)[\\/]([^\\/]+)[\\/]')

by_role = collections.defaultdict(set)
by_res = collections.defaultdict(set)

for root, _dirs, files in os.walk(SRC):
    for fn in files:
        if not fn.endswith((".ts", ".tsx")):
            continue
        p = os.path.join(root, fn)
        try:
            s = open(p, encoding="utf-8").read()
        except Exception:
            continue
        m = role_pat.search(p)
        if m:
            role = m.group(1)
        elif "components" in p or "lib" in p:
            role = "_shared"
        else:
            role = "_other"
        for _hook, res in pat.findall(s):
            by_role[role].add(res)
            by_res[res].add(role)

out = {
    "by_role": {k: sorted(v) for k, v in sorted(by_role.items())},
    "resources": sorted(by_res),
}
print(json.dumps(out, ensure_ascii=False, indent=1))
