#!/usr/bin/env bash
# Шифрует URL сервера для config/server.url.enc.
# Ключ пишется только в local.properties (в git не коммитить).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
URL="${1:-}"
if [ -z "$URL" ]; then
  echo "Usage: $0 'http://HOST:PORT'"
  exit 1
fi

KEY_B64="$(openssl rand -base64 16 | tr -d '\n')"
KEY_HEX="$(printf '%s' "$KEY_B64" | openssl base64 -d -A | xxd -p -c 256)"
IV_HEX="$(openssl rand -hex 16)"
IV_BIN="$(printf '%s' "$IV_HEX" | xxd -r -p)"

CT_BIN="$(printf '%s' "$URL" | openssl enc -aes-128-cbc -K "$KEY_HEX" -iv "$IV_HEX" -nosalt)"
BLOB="$( { printf '%s' "$IV_BIN"; printf '%s' "$CT_BIN"; } | openssl base64 -A )"

mkdir -p "$ROOT/config"
printf '%s\n' "$BLOB" > "$ROOT/config/server.url.enc"

LP="$ROOT/local.properties"
touch "$LP"
grep -v '^server.url.key=' "$LP" > "$LP.tmp" || true
mv "$LP.tmp" "$LP"
echo "server.url.key=$KEY_B64" >> "$LP"

echo "✓ Записано: config/server.url.enc"
echo "✓ Ключ добавлен в local.properties (не коммитьте этот файл)"
echo "  Проверка сборки: ./gradlew :app:generateDebugBuildConfig"
