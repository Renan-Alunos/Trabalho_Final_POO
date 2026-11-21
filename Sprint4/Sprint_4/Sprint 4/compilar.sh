#!/bin/sh
set -eu
cd "$(dirname "$0")"
if ! command -v javac >/dev/null 2>&1; then
  echo 'Erro: instale um JDK (17+) e deixe javac no PATH.' >&2
  exit 1
fi
mkdir -p build
javac -encoding UTF-8 -d build src/*.java Gui/*.java verificacao/*.java
echo 'Sprint 4 compilada. Execute: java -cp build TelaFarmacia'
