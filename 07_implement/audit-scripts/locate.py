import os, re, sys, collections

SRC = r"C:\Users\phuoc\Downloads\SEP490_G8\04_Development\Frontend\src"
hook_pat = re.compile(
    r'use(PagedList|PlainList|Single|Detail|SubDetail|Create|Update|Patch|PatchAction'
    r'|Action|Remove|UploadMutation|SubList)\s*<[^;]{0,400}?>\s*\(\s*"([^"]+)"'
)

PAIRS = [l.split() for l in """
system-admin allergy-declarations
system-admin menus
system-admin students
school-admin schools
medical-staff supplier-products
teacher diet-groups
teacher inspections
teacher inventory-transactions
teacher partners
teacher supplier-products
service-caterers allergy-declarations
service-caterers delivery-persons
supplier classrooms
supplier dishes
supplier menus
supplier partners
supplier students
parent classrooms
parent ingredients
parent inventory
parent partners
parent source-reports
parent supplier-products
""".strip().splitlines()]

where = collections.defaultdict(list)
for root, _d, fns in os.walk(SRC):
    for fn in fns:
        if not fn.endswith((".ts", ".tsx")):
            continue
        p = os.path.join(root, fn)
        try:
            s = open(p, encoding="utf-8").read()
        except Exception:
            continue
        rel = os.path.relpath(p, SRC).replace("\\", "/")
        for _h, res in hook_pat.findall(s):
            where[res].append(rel)

for role, res in PAIRS:
    locs = sorted(set(where.get(res, [])))
    own = [l for l in locs if f"app/(app)/{role}/" in l]
    tag = "TRONG MAN " + role if own else "chi o file dung chung"
    print(f"[{role}] {res}: {tag}")
    for l in (own or locs):
        print("      ", l)
