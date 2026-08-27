#!/usr/bin/env python3
"""Check every markdown link in the repo — file targets and #anchors.

Usage:  python3 tools/check-links.py [root]

Exits 1 if any link is broken, so it can gate a commit.
Anchor slugs follow GitHub's rule: lowercase, drop punctuation and
symbols (§ ⭐ em dash), collapse whitespace to single hyphens. That
means "## §A Functional interface" is reachable as #a-functional-interface.
"""
import os
import re
import sys

HEADING = re.compile(r"^(#{1,6})\s+(.*)$")
# [text](target.md) or [text](target.md#anchor) or [text](#anchor)
LINK = re.compile(r"\]\(\s*([^)\s#]*?)(#[^)\s]*)?\s*\)")
SKIP_DIRS = {".git", "node_modules", "build", ".gradle"}
IN_FENCE = re.compile(r"^\s*(```|~~~)")
INLINE_CODE = re.compile(r"`[^`]*`")


def slug(heading: str) -> str:
    """GitHub's anchor slug for a heading's text."""
    s = heading.strip().lower().replace("`", "")
    s = re.sub(r"\[([^\]]*)\]\([^)]*\)", r"\1", s)   # links render as their text
    s = s.strip()
    s = re.sub(r"[^\w\s-]", "", s)                    # drop § ⭐ — " etc
    # each space becomes one hyphen, and symbols leave their spaces behind:
    # "interface — the" slugs to "interface--the", "Kafka ⚓" to "kafka-"
    return re.sub(r"\s", "-", s)


def scan(path):
    """Return (set of anchor slugs, list of (lineno, target, anchor))."""
    anchors, links, fenced = set(), [], False
    with open(path, encoding="utf-8") as fh:
        for n, line in enumerate(fh, 1):
            if IN_FENCE.match(line):
                fenced = not fenced
                continue
            if fenced:
                continue
            m = HEADING.match(line)
            if m:
                base = slug(m.group(2))
                # duplicate headings get -1, -2, … appended, as GitHub does
                cand, i = base, 1
                while cand in anchors:
                    cand, i = f"{base}-{i}", i + 1
                anchors.add(cand)
            bare = INLINE_CODE.sub(" ", line)   # `[§4](#4-…)` is an example
            for lm in LINK.finditer(bare):
                target, anchor = lm.group(1), lm.group(2)
                if target and re.match(r"^[a-z][a-z0-9+.-]*:", target):
                    continue            # external URL — not ours to verify
                if not target and not anchor:
                    continue
                links.append((n, target, anchor[1:] if anchor else None))
    return anchors, links


def main() -> int:
    root = sys.argv[1] if len(sys.argv) > 1 else "."
    files = {}
    for dirpath, dirnames, filenames in os.walk(root):
        dirnames[:] = [d for d in dirnames if d not in SKIP_DIRS]
        for name in filenames:
            if name.endswith(".md"):
                p = os.path.normpath(os.path.join(dirpath, name))
                files[p] = scan(p)

    broken = 0
    for path, (_, links) in sorted(files.items()):
        for lineno, target, anchor in links:
            dest = path if not target else os.path.normpath(
                os.path.join(os.path.dirname(path), target))
            if dest not in files:
                if target.endswith(".md") or not os.path.exists(dest):
                    kind = "no such file" if target.endswith(".md") else "not found"
                    print(f"{path}:{lineno}: {kind}: {target}")
                    broken += 1
                continue
            if anchor and anchor not in files[dest][0]:
                where = "in this file" if dest == path else f"in {target}"
                print(f"{path}:{lineno}: no heading '#{anchor}' {where}")
                broken += 1

    total = sum(len(v[1]) for v in files.values())
    if broken:
        print(f"\n{broken} broken of {total} links across {len(files)} files.")
        return 1
    print(f"OK — {total} links across {len(files)} files, none broken.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
