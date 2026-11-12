@echo off
setlocal
cd /d "%~dp0"

echo Compiling src and test...
javac -d out src\*.java test\*.java
if errorlevel 1 (
    echo COMPILE FAILED
    exit /b 1
)

echo.
echo === LabTestRunner (acceptance) ===
java -ea -cp out LabTestRunner
set LAB_RC=%errorlevel%

echo.
echo === StudentBSTTest (student-authored) ===
java -ea -cp out StudentBSTTest
set STUDENT_RC=%errorlevel%

echo.
if not "%LAB_RC%"=="0" exit /b %LAB_RC%
if not "%STUDENT_RC%"=="0" exit /b %STUDENT_RC%
echo All test suites passed.
exit /b 0
