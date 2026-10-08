#!/usr/bin/env bash
set -e
cd "$(dirname "$0")/.."
PG=lib/postgresql-42.7.13.jar
rm -rf out/main && mkdir -p out/main
javac -encoding UTF-8 -nowarn -d out/main -cp "$PG" $(find src/main/java -name '*.java')
java -Djava.security.egd=file:/dev/./urandom -cp "out/main:$PG" app.DemoFinal
