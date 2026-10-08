#!/usr/bin/env bash
# Compila e roda TODOS os testes pela linha de comando (Linux/macOS/Git Bash).
# Sem DB_PASSWORD: roda só os testes unitários (os de banco são ignorados).
# Com DB_PASSWORD: roda também os testes de integração contra o banco.
set -e
cd "$(dirname "$0")/.."
JUNIT=tools/junit-platform-console-standalone.jar
PG=lib/postgresql-42.7.13.jar
rm -rf out && mkdir -p out/main out/test
javac -encoding UTF-8 -nowarn -d out/main -cp "$PG" $(find src/main/java -name '*.java')
javac -encoding UTF-8 -nowarn -d out/test -cp "out/main:$PG:$JUNIT" $(find src/test/java -name '*.java')
java -jar "$JUNIT" --class-path "out/main:out/test:$PG" --scan-class-path out/test --disable-banner "$@"
