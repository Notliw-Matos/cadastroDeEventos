#!/usr/bin/env bash
# Compila e roda a API HTTP (S4). Depois é só abrir site/index.html no navegador.
set -e
cd "$(dirname "$0")/.."
PG=lib/postgresql-42.7.13.jar
rm -rf out/main && mkdir -p out/main
javac -encoding UTF-8 -nowarn -d out/main -cp "$PG" $(find src/main/java -name '*.java')
java -cp "out/main:$PG" app.ServidorApi
