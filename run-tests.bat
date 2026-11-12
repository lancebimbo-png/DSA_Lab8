@echo off
setlocal
cd /d "%~dp0"
if exist out rmdir /s /q out
javac -d out src\*.java test\*.java
if errorlevel 1 exit /b 1
java -ea -cp out LabTestRunner
if errorlevel 1 exit /b 1
java -ea -cp out StudentBSTTest
if errorlevel 1 exit /b 1
java -ea -cp out ComplexityAudit
if errorlevel 1 exit /b 1
echo All test suites passed.
exit /b 0
