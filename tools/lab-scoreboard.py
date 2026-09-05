#!/usr/bin/env python3
"""Regenerate the lab README scoreboard, Done column and Scorecard from source.

Truth is the starter files themselves: a lab still carrying its
UnsupportedOperationException or TODO seam is outstanding. The eight
concurrency stations are demonstrations with no such seam, so their state is
read back from the existing Done marks rather than detected.

Run from the repo root after completing a lab:  python3 tools/lab-scoreboard.py
Exit 1 means the README changed and should be committed.
"""
import pathlib
import re
import sys

LAB = pathlib.Path(__file__).resolve().parent.parent / "core-java" / "lab"
README = LAB / "README.md"

SECTIONS = [
    ("Introduction", "Introduction", 13),
    ("Strings", "Strings", 11),
    ("BigNumber", "BigNumber", 3),
    ("Data Structures", "Data Structures", 15),
    ("Object-Oriented Programming", "OOP", 8),
    ("Exception Handling", "Exceptions", 2),
    ("Threads and Concurrency", "Threads", 8),
    ("Advanced", "Advanced", 12),
    ("Stream API", "Stream API", 18),
]
# Track heading -> Scorecard subdomain, where the two tables disagree.
HEADING_TO_SUBDOMAIN = {"Advanced": "Advanced", "Introduction": "Introduction"}

BAR_WIDTH = 30
SECTION_BAR_WIDTH = 22


def solved(path):
    """A starter is solved once its marked seam is gone."""
    text = path.read_text(encoding="utf-8")
    return "UnsupportedOperationException" not in text and "TODO" not in text


def main():
    text = README.read_text(encoding="utf-8")
    lines = text.split("\n")

    out, section, counts = [], None, {}
    for line in lines:
        if line.startswith("### "):
            section = line[4:].strip()
        m = re.match(r"^(\|.*\]\((?:([^)/]*)/)?([A-Za-z0-9]+\.java)\).*?\|)\s*(✅|⬜)\s*\|\s*$", line)
        if m and section:
            row, folder, filename, existing = m.groups()
            if folder and folder.startswith("07-"):
                mark = existing          # stations are self-reported
            else:
                candidates = list(LAB.rglob(filename))
                mark = "✅" if candidates and solved(candidates[0]) else "⬜"
            done, total = counts.get(section, (0, 0))
            counts[section] = (done + (mark == "✅"), total + 1)
            out.append(f"{row} {mark} |")
            continue
        out.append(line)

    text = "\n".join(out)

    total_done = sum(d for d, _ in counts.values())
    total_all = sum(t for _, t in counts.values())
    pct = round(total_done / total_all * 100)
    filled = round(total_done / total_all * BAR_WIDTH)
    bar = "█" * filled + "░" * (BAR_WIDTH - filled)

    for heading, _, expected in SECTIONS:
        got = counts.get(heading, (0, 0))[1]
        if got != expected:
            print(f"warning: {heading} has {got} rows, expected {expected}", file=sys.stderr)

    pairs = [(short, counts.get(head, (0, 0))) for head, short, _ in SECTIONS]
    name_w = max(len(s) for s, _ in pairs)
    frac_w = max(len(f"{d}/{t}") for _, (d, t) in pairs)
    rows = []
    for short, (d, t) in pairs:
        fill = round(d / t * SECTION_BAR_WIDTH) if t else 0
        rows.append(
            f"{short.ljust(name_w)}  {'█' * fill}{'░' * (SECTION_BAR_WIDTH - fill)}  "
            f"{f'{d}/{t}'.rjust(frac_w)}"
        )
    chart = "\n".join(rows)

    board = (
        f'<div align="center">\n\n'
        f"## `{total_done} / {total_all}` labs complete\n\n"
        f"`{bar}` **{pct}%**\n\n"
        f"</div>\n\n"
        f"```text\n{chart}\n```"
    )
    text = re.sub(
        r'<div align="center">\n\n## `.*?```text\n.*?\n```|<div align="center">\n\n## `.*?</div>',
        lambda _: board, text, count=1, flags=re.S,
    )

    # Scorecard rows and total
    for heading, _, _ in SECTIONS:
        sub = HEADING_TO_SUBDOMAIN.get(heading, heading)
        d, t = counts.get(heading, (0, 0))
        text = re.sub(
            rf"^\| {re.escape(sub)} \| (\d+) \| \d+/\d+ \|$",
            f"| {sub} | {t} | {d}/{t} |",
            text, count=1, flags=re.M,
        )
    text = re.sub(
        r"^\| \*\*Total\*\* \| \*\*\d+\*\* \| \*\*\d+/\d+\*\* \|$",
        f"| **Total** | **{total_all}** | **{total_done}/{total_all}** |",
        text, count=1, flags=re.M,
    )

    changed = text != README.read_text(encoding="utf-8")
    README.write_text(text, encoding="utf-8")
    print(f"{total_done}/{total_all} complete ({pct}%)" + ("  — README updated" if changed else "  — no change"))
    return 1 if changed else 0


if __name__ == "__main__":
    sys.exit(main())
