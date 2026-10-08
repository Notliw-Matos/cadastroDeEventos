@echo off
cd /d "%~dp0.."
set PG=lib\postgresql-42.7.13.jar
if exist out\main rmdir /s /q out\main
mkdir out\main
dir /s /b src\main\java\*.java > out\main.txt
javac -encoding UTF-8 -nowarn -d out\main -cp "%PG%" @out\main.txt || exit /b 1
java -cp "out\main;%PG%" app.DemoFinal
