#!/usr/bin/env python3
"""mdiff — 左右並べ HTML 差分ビューア（日本語 Markdown 文書向け）

lazygit の「赤と緑の海」対策。左＝元の文 / 右＝いまの文 を行ごとに突き合わせ、
変わった語句（日本語は文字単位）だけに色を付ける。Python 3.8+ / 標準ライブラリのみ。

使い方:
  python3 etc/tools/mdiff.py                    # HEAD ⇄ いまの作業ツリー
  python3 etc/tools/mdiff.py docs/              # パスを絞る
  python3 etc/tools/mdiff.py --rev HEAD~3       # 3コミット前と比較
  python3 etc/tools/mdiff.py --files old.md new.md   # git なしで2ファイル
  オプション: -o 出力先HTML / --no-open ブラウザを開かない

キー操作: n/p 次・前の変更へ / f 折りたたみ切替 / w 語句ハイライト切替
"""
import argparse
import difflib
import html
import os
import subprocess
import sys
import tempfile
import webbrowser

CONTEXT = 3          # 変更の前後に見せる行数
FOLD_MIN = 8         # これ以上連続した無変更行は畳む


def run(args):
    return subprocess.run(args, capture_output=True)


def git_root():
    r = run(["git", "rev-parse", "--show-toplevel"])
    return r.stdout.decode().strip() if r.returncode == 0 else None


def git_show(rev, path):
    r = run(["git", "show", f"{rev}:{path}"])
    return r.stdout.decode("utf-8", "replace") if r.returncode == 0 else ""


def changed_files(rev, paths):
    args = ["git", "diff", "--name-only", rev, "--"] + paths
    files = [l for l in run(args).stdout.decode().split("\n") if l.strip()]
    args = ["git", "ls-files", "--others", "--exclude-standard", "--"] + paths
    untracked = [l for l in run(args).stdout.decode().split("\n") if l.strip()]
    seen, out = set(), []
    for f in files + untracked:
        if f not in seen:
            seen.add(f)
            out.append(f)
    return out


def read_text(path):
    try:
        with open(path, "rb") as fh:
            data = fh.read()
        if b"\x00" in data[:8000]:
            return None  # バイナリは対象外
        return data.decode("utf-8", "replace")
    except OSError:
        return ""


def inline_marks(a, b):
    """行内の文字単位差分。変わった部分だけ <span> で包む。"""
    sm = difflib.SequenceMatcher(None, a, b, autojunk=False)
    la, lb = [], []
    for op, i1, i2, j1, j2 in sm.get_opcodes():
        ea, eb = html.escape(a[i1:i2]), html.escape(b[j1:j2])
        if op == "equal":
            la.append(ea)
            lb.append(eb)
        else:
            if ea:
                la.append('<span class="d">%s</span>' % ea)
            if eb:
                lb.append('<span class="a">%s</span>' % eb)
    return "".join(la), "".join(lb)


def build_rows(old_text, new_text):
    """rows: (kind, old_lineno, old_html, new_lineno, new_html)"""
    a = old_text.split("\n")
    b = new_text.split("\n")
    if a and a[-1] == "":
        a.pop()
    if b and b[-1] == "":
        b.pop()
    rows = []
    sm = difflib.SequenceMatcher(None, a, b, autojunk=False)
    for op, i1, i2, j1, j2 in sm.get_opcodes():
        if op == "equal":
            for k in range(i2 - i1):
                rows.append(("eq", i1 + k + 1, html.escape(a[i1 + k]),
                             j1 + k + 1, html.escape(b[j1 + k])))
        elif op == "replace":
            n = max(i2 - i1, j2 - j1)
            for k in range(n):
                ai = i1 + k if i1 + k < i2 else None
                bj = j1 + k if j1 + k < j2 else None
                if ai is not None and bj is not None:
                    lo, ln = inline_marks(a[ai], b[bj])
                    rows.append(("chg", ai + 1, lo, bj + 1, ln))
                elif ai is not None:
                    rows.append(("del", ai + 1, html.escape(a[ai]), None, ""))
                else:
                    rows.append(("ins", None, "", bj + 1, html.escape(b[bj])))
        elif op == "delete":
            for k in range(i1, i2):
                rows.append(("del", k + 1, html.escape(a[k]), None, ""))
        elif op == "insert":
            for k in range(j1, j2):
                rows.append(("ins", None, "", k + 1, html.escape(b[k])))
    return rows


def render_file(path, rows, idx):
    out = []
    out.append('<section class="file" id="f%d">' % idx)
    out.append('<h2>%s</h2>' % html.escape(path))
    out.append('<table>')
    i = 0
    fold_id = 0
    first_of_block = True
    while i < len(rows):
        kind = rows[i][0]
        if kind == "eq":
            j = i
            while j < len(rows) and rows[j][0] == "eq":
                j += 1
            run_len = j - i
            head = CONTEXT if i > 0 else 0
            tail = CONTEXT if j < len(rows) else 0
            if run_len > head + tail + FOLD_MIN:
                for r in rows[i:i + head]:
                    out.append(row_html(r, False))
                fold_id += 1
                hidden = rows[i + head: j - tail]
                out.append(
                    '<tr class="foldbar" data-fold="%d"><td colspan="4">'
                    '⋯ 変更なし %d 行（クリックで表示）⋯</td></tr>'
                    % (fold_id, len(hidden)))
                for r in hidden:
                    out.append(row_html(r, False, fold=fold_id))
                for r in rows[j - tail: j]:
                    out.append(row_html(r, False))
            else:
                for r in rows[i:j]:
                    out.append(row_html(r, False))
            i = j
            first_of_block = True
        else:
            out.append(row_html(rows[i], first_of_block))
            first_of_block = False
            i += 1
    out.append('</table></section>')
    return "\n".join(out)


def row_html(r, hunk_start, fold=None):
    kind, ln_o, h_o, ln_n, h_n = r
    cls = kind + (" hunk" if hunk_start else "")
    attrs = ' class="%s"' % cls
    if fold is not None:
        attrs += ' data-infold="%d" hidden' % fold
    c_old = '<td class="ln">%s</td><td class="code old%s">%s</td>' % (
        ln_o if ln_o else "", " void" if ln_o is None else "", h_o)
    c_new = '<td class="ln">%s</td><td class="code new%s">%s</td>' % (
        ln_n if ln_n else "", " void" if ln_n is None else "", h_n)
    return "<tr%s>%s%s</tr>" % (attrs, c_old, c_new)


CSS = """
:root { --bg:#fff; --fg:#24292f; --muted:#6e7781; --line:#d8dee4;
  --del-bg:#ffebe9; --del-hl:#ffb3ad; --ins-bg:#e6ffec; --ins-hl:#9ff0b5;
  --fold:#f6f8fa; --head:#f6f8fa; --cur:#f2cc60; }
@media (prefers-color-scheme: dark) { :root:not([data-theme="light"]) { --bg:#0d1117; --fg:#e6edf3;
  --muted:#8b949e; --line:#30363d; --del-bg:#3c1618; --del-hl:#8b2c2c;
  --ins-bg:#12261e; --ins-hl:#1f6f3f; --fold:#161b22; --head:#161b22; --cur:#6b5618; } }
:root[data-theme="dark"] { --bg:#0d1117; --fg:#e6edf3;
  --muted:#8b949e; --line:#30363d; --del-bg:#3c1618; --del-hl:#8b2c2c;
  --ins-bg:#12261e; --ins-hl:#1f6f3f; --fold:#161b22; --head:#161b22; --cur:#6b5618; }
body { background: var(--bg); }
* { box-sizing:border-box; }
body { margin:0; background:var(--bg); color:var(--fg);
  font:13px/1.7 "SFMono-Regular",Consolas,"BIZ UDGothic","Hiragino Kaku Gothic ProN",Meiryo,monospace; }
header { position:sticky; top:0; z-index:9; background:var(--head);
  border-bottom:1px solid var(--line); padding:.5rem .9rem;
  display:flex; gap:1rem; align-items:center; flex-wrap:wrap; }
header .keys { color:var(--muted); font-size:12px; }
header select { font:inherit; max-width:40ch; }
#counter { font-weight:bold; min-width:5.5em; }
section.file { margin:1rem auto 2.5rem; max-width:1400px; padding:0 .8rem; }
h2 { font-size:14px; border-bottom:2px solid var(--fg); padding-bottom:.3rem; }
table { border-collapse:collapse; width:100%; table-layout:fixed; }
td.ln { width:3.4em; text-align:right; padding:0 .5em; color:var(--muted);
  user-select:none; vertical-align:top; border-right:1px solid var(--line); }
td.code { width:calc(50% - 3.4em); padding:0 .6em; white-space:pre-wrap;
  overflow-wrap:anywhere; vertical-align:top; }
tr.del td.old, tr.chg td.old { background:var(--del-bg); }
tr.ins td.new, tr.chg td.new { background:var(--ins-bg); }
td.void { background:repeating-linear-gradient(45deg, transparent 0 6px, var(--line) 6px 7px); }
.code .d { background:var(--del-hl); border-radius:2px; }
.code .a { background:var(--ins-hl); border-radius:2px; }
body.nohl .code .d, body.nohl .code .a { background:transparent; }
tr.foldbar td { background:var(--fold); color:var(--muted); text-align:center;
  cursor:pointer; padding:.15rem; border-top:1px solid var(--line);
  border-bottom:1px solid var(--line); }
tr.current td.code { outline:2px solid var(--cur); outline-offset:-2px; }
"""

JS = """
const hunks=[...document.querySelectorAll('tr.hunk')];let cur=-1;
const counter=document.getElementById('counter');
function show(i){if(!hunks.length)return;cur=(i+hunks.length)%hunks.length;
 hunks.forEach(h=>h.classList.remove('current'));const h=hunks[cur];
 h.classList.add('current');
 if(h.hidden){const f=h.dataset.infold;toggleFold(f,true);}
 h.scrollIntoView({block:'center'});counter.textContent=(cur+1)+' / '+hunks.length;}
function toggleFold(id,open){
 document.querySelectorAll('[data-infold="'+id+'"]').forEach(r=>r.hidden=open?false:!r.hidden);
 const bar=document.querySelector('.foldbar[data-fold="'+id+'"]');
 if(bar&&open)bar.hidden=true;}
document.querySelectorAll('.foldbar').forEach(b=>b.addEventListener('click',
 ()=>{toggleFold(b.dataset.fold,true)}));
let allOpen=false;
document.addEventListener('keydown',e=>{
 if(e.target.tagName==='SELECT')return;
 if(e.key==='n')show(cur+1);
 if(e.key==='p')show(cur-1);
 if(e.key==='w')document.body.classList.toggle('nohl');
 if(e.key==='f'){allOpen=!allOpen;
  document.querySelectorAll('[data-infold]').forEach(r=>r.hidden=!allOpen);
  document.querySelectorAll('.foldbar').forEach(b=>b.hidden=allOpen);}});
const sel=document.getElementById('filesel');
if(sel)sel.addEventListener('change',()=>{
 document.getElementById(sel.value).scrollIntoView();});
counter.textContent=hunks.length? '0 / '+hunks.length : '差分なし';
"""


def render_page(title, sections, file_names):
    opts = "".join('<option value="f%d">%s</option>' % (i, html.escape(n))
                   for i, n in enumerate(file_names))
    filesel = ('<select id="filesel">%s</select>' % opts) if len(file_names) > 1 else ""
    return ("<!doctype html><meta charset='utf-8'>"
            "<title>%s</title><style>%s</style>"
            "<header><span id='counter'></span>%s"
            "<span class='keys'>n/p: 次・前の変更　f: 折りたたみ切替　w: ハイライト切替　"
            "左=元 / 右=いま</span></header>%s<script>%s</script>"
            % (html.escape(title), CSS, filesel, "\n".join(sections), JS))


def main():
    ap = argparse.ArgumentParser(description="左右並べ HTML 差分ビューア")
    ap.add_argument("paths", nargs="*", help="対象パス（省略時は変更のあった全ファイル）")
    ap.add_argument("--rev", default="HEAD", help="比較元リビジョン（既定: HEAD）")
    ap.add_argument("--files", nargs=2, metavar=("OLD", "NEW"), help="git を使わず 2 ファイル比較")
    ap.add_argument("-o", "--out", help="出力 HTML のパス（既定: 一時ファイル）")
    ap.add_argument("--no-open", action="store_true", help="ブラウザを開かない")
    args = ap.parse_args()

    pairs = []  # (表示名, old_text, new_text)
    if args.files:
        o, n = args.files
        pairs.append(("%s ⇄ %s" % (o, n), read_text(o) or "", read_text(n) or ""))
        title = "mdiff: %s ⇄ %s" % (o, n)
    else:
        root = git_root()
        if not root:
            sys.exit("git リポジトリではありません（--files で2ファイル比較は可能）")
        os.chdir(root)
        files = changed_files(args.rev, args.paths)
        if not files:
            sys.exit("差分はありません（%s ⇄ 作業ツリー）" % args.rev)
        for f in files:
            new = read_text(f)
            if new is None:
                continue
            pairs.append((f, git_show(args.rev, f), new))
        title = "mdiff: %s ⇄ working tree" % args.rev

    sections = []
    names = []
    for i, (name, old, new) in enumerate(pairs):
        rows = build_rows(old, new)
        if all(r[0] == "eq" for r in rows):
            continue
        sections.append(render_file(name, rows, len(names)))
        names.append(name)
    if not sections:
        sys.exit("差分はありません")

    out = args.out or os.path.join(tempfile.mkdtemp(prefix="mdiff-"), "diff.html")
    with open(out, "w", encoding="utf-8") as fh:
        fh.write(render_page(title, sections, names))
    print("wrote %s (%d file%s)" % (out, len(names), "s" if len(names) > 1 else ""))
    if not args.no_open:
        webbrowser.open("file://" + os.path.abspath(out))


if __name__ == "__main__":
    main()
