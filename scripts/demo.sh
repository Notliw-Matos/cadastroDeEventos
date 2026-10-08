#!/usr/bin/env bash
# Roda o roteiro de demonstração S2/S3 (precisa de DB_PASSWORD).
set -e
cd "$(dirname "$0")/.."
PG=lib/postgresql-42.7.13.jar
rm -rf out/main && mkdir -p out/main
javac -encoding UTF-8 -nowarn -d out/main -cp "$PG" $(find src/main/java -name '*.java')
java -cp "out/main:$PG" app.DemoS2S3 "$@"
