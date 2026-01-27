#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
Install the local tabletest skill to agent skill directories.

Usage:
  install-skill.sh [--dry-run] [--dest PATH]... [--add-dest PATH]... [--list-defaults]

Options:
  -d, --dest PATH   Install only to PATH (repeatable).
  -a, --add-dest PATH  Add PATH to the default destinations (repeatable).
  -n, --dry-run     Print actions without modifying files.
  -l, --list-defaults  Print resolved default destinations and exit.
  -h, --help        Show this help.
EOF
}

dry_run=false
list_defaults=false
custom_targets=()
add_targets=()

while [ "$#" -gt 0 ]; do
  case "$1" in
    -h|--help)
      usage
      exit 0
      ;;
    -n|--dry-run)
      dry_run=true
      shift
      ;;
    -l|--list-defaults)
      list_defaults=true
      shift
      ;;
    -d|--dest)
      if [ "$#" -lt 2 ]; then
        echo "Missing argument for $1" >&2
        exit 1
      fi
      custom_targets+=("$2")
      shift 2
      ;;
    -a|--add-dest)
      if [ "$#" -lt 2 ]; then
        echo "Missing argument for $1" >&2
        exit 1
      fi
      add_targets+=("$2")
      shift 2
      ;;
    --dest=*)
      custom_targets+=("${1#*=}")
      shift
      ;;
    --add-dest=*)
      add_targets+=("${1#*=}")
      shift
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage >&2
      exit 1
      ;;
  esac
done

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
src_dir="${script_dir}/skills/tabletest"

if [ ! -d "$src_dir" ]; then
  echo "Source skill not found: $src_dir" >&2
  exit 1
fi

resolve_path() {
  local input="$1"
  local expanded="${input/#\~/$HOME}"
  if [[ "$expanded" != /* ]]; then
    expanded="${PWD}/${expanded}"
  fi
  printf '%s\n' "$expanded"
}

default_targets() {
  local targets=("$HOME/.claude/skills")
  local jb_root
  local -a jb_candidates=()

  if [ -n "${CODEX_HOME:-}" ] && [ -d "${CODEX_HOME}/skills" ]; then
    targets+=("${CODEX_HOME}/skills")
  else
    jb_root="${HOME}/Library/Caches/JetBrains"
    if [ -d "$jb_root" ]; then
      shopt -s nullglob
      jb_candidates=("$jb_root"/IntelliJIdea*/aia/codex/skills)
      shopt -u nullglob
      if [ "${#jb_candidates[@]}" -gt 0 ]; then
        targets+=("${jb_candidates[@]}")
      else
        echo "Warning: No JetBrains Codex skills directory found under $jb_root" >&2
      fi
    fi
  fi

  printf '%s\n' "${targets[@]}"
}

if $list_defaults; then
  defaults=()
  while IFS= read -r line; do
    defaults+=("$line")
  done < <(default_targets)
  if [ "${#defaults[@]}" -eq 0 ]; then
    echo "No default destinations found." >&2
    exit 1
  fi
  for t in "${defaults[@]}"; do
    echo "$(resolve_path "$t")"
  done
  exit 0
fi

targets=()
while IFS= read -r line; do
  targets+=("$line")
done < <(default_targets)

if [ "${#add_targets[@]}" -gt 0 ]; then
  targets+=("${add_targets[@]}")
fi

if [ "${#custom_targets[@]}" -gt 0 ]; then
  targets=("${custom_targets[@]}")
fi

unique_targets=()
if [ "${#targets[@]}" -gt 0 ]; then
  for t in "${targets[@]}"; do
    resolved="$(resolve_path "$t")"
    already=false
    if [ "${#unique_targets[@]}" -gt 0 ]; then
      for u in "${unique_targets[@]}"; do
        if [ "$u" = "$resolved" ]; then
          already=true
          break
        fi
      done
    fi
    if ! $already; then
      unique_targets+=("$resolved")
    fi
  done
fi

if [ "${#unique_targets[@]}" -eq 0 ]; then
  echo "No destination directories resolved." >&2
  exit 1
fi

for dest_root in "${unique_targets[@]}"; do
  if $dry_run; then
    echo "Would create: $dest_root"
    echo "Would remove: ${dest_root}/tabletest"
    echo "Would copy: $src_dir -> ${dest_root}/tabletest"
  else
    mkdir -p "$dest_root"
    rm -rf "${dest_root}/tabletest"
    cp -R "$src_dir" "${dest_root}/tabletest"
    echo "Installed tabletest skill to ${dest_root}/tabletest"
  fi
done
