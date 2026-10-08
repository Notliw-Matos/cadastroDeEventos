@echo off
REM Compila e roda TODOS os testes pela linha de comando (Windows).
REM Sem DB_PASSWORD: roda so os testes unitarios (os de banco sao ignorados).
REM Com DB_PASSWORD: roda tambem os testes de integracao contra o banco.
cd /d "%~dp0.."
set JUNIT=tools\junit-platform-console-standalone.jar
set PG=lib\postgresql-42.7.13.jar
if exist out rmdir /s /q out
mkdir out\main
mkdir out\test
dir /s /b src\main\java\*.java > out\main.txt
dir /s /b src\test\java\*.java > out\test.txt
javac -encoding UTF-8 -nowarn -d out\main -cp "%PG%" @out\main.txt || exit /b 1
javac -encoding UTF-8 -nowarn -d out\test -cp "out\main;%PG%;%JUNIT%" @out\test.txt || exit /b 1
java -jar "%JUNIT%" --class-path "out\main;out\test;%PG%" --scan-class-path out\test --disable-banner %*
