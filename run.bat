@echo off
echo ================================================================================
echo  Food Delivery App - Build and Run
echo ================================================================================

:: Create output directory
if not exist "out" mkdir out

:: Collect all Java source files into a temp list
echo Compiling all source files...
dir /s /b src\*.java > sources.txt 2>nul

:: Compile everything from project root
javac -d out -sourcepath src @sources.txt 2>&1
del sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [BUILD FAILED] Fix the errors above and try again.
    pause
    exit /b 1
)

echo [BUILD SUCCESS] Starting application...
echo.
java -cp out com.food_delivery.Main
