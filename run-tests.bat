@echo off
echo ================================================================================
echo  Food Delivery App - Run Tests Only (No interactive menu)
echo ================================================================================

if not exist "out" mkdir out

dir /s /b src\*.java > sources.txt 2>nul
javac -d out -sourcepath src @sources.txt 2>&1
del sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo [BUILD FAILED]
    pause
    exit /b 1
)

echo [BUILD SUCCESS] Running all 88 tests...
echo.
java -cp out com.food_delivery.test.AllModulesTestSuite
pause
