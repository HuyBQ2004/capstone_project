import json, re, urllib.request

api = json.load(urllib.request.urlopen("http://localhost:8080/v3/api-docs"))
paths = list(api["paths"].keys())

fe = json.load(open("endpoints.json", encoding="utf-8"))

# normalise: an OpenAPI path like /api/v1/menus/{code}/approve -> segments
def matches(resource: str) -> bool:
    """resource is what FE puts after /api/v1/ , may contain sub-segments."""
    want = "/api/v1/" + resource
    for p in paths:
        if p == want:
            return True
        # FE resource may be the collection root of a templated path
        if p.startswith(want + "/") or p.startswith(want + "?"):
            return True
        # tolerate {var} segments: compare segment counts + literal segments
        pw, pp = want.strip("/").split("/"), p.strip("/").split("/")
        if len(pw) == len(pp) and all(b.startswith("{") or a == b for a, b in zip(pw, pp)):
            return True
    return False

missing = [r for r in fe["resources"] if not matches(r)]
print("=== FE goi nhung KHONG co trong OpenAPI (%d/%d):" % (len(missing), len(fe["resources"])))
for r in missing:
    roles = [k for k, v in fe["by_role"].items() if r in v]
    print("  -", r, " <- ", ",".join(roles))
