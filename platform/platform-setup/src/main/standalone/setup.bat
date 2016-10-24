@echo off
setlocal

rem Let's position into folder containing this script:
set CURRENTDIR="%cd%"
set BASEDIR=%~dp0
cd %BASEDIR%

set CFG_FOLDER=%BASEDIR%\platform_conf
set INITIAL_CFG_FOLDER=%CFG_FOLDER%\initial
set LIB_FOLDER=%BASEDIR%\lib

FOR /F "tokens=1,* delims== eol=#" %%A IN (database.properties) DO (set %%A=%%B)
set BONITA_DATABASE=%db.vendor%

IF NOT "%BONITA_DATABASE%" == "h2" IF NOT "%BONITA_DATABASE%" == "postgres" IF NOT "%BONITA_DATABASE%" == "sqlserver" IF NOT "%BONITA_DATABASE%" == "oracle" IF NOT "%BONITA_DATABASE%" == "mysql"  (
    echo Cannot determine database vendor valid values are [h2, postgres, sqlserver, oracle, mysql].
    echo Please configure file %BASEDIR%database.properties properly.
    exit /b 1
)


REM get rid of first parameter (action parameter) and pass the rest of the command line to the java program through $@:
SHIFT
java -cp "%BASEDIR%;%CFG_FOLDER%;%INITIAL_CFG_FOLDER%;%LIB_FOLDER%\*"  -Dspring.profiles.active=default -Dsysprop.bonita.db.vendor=%BONITA_DATABASE% org.bonitasoft.platform.setup.PlatformSetupApplication %0 %1 %2 %3 %4 %5 %6 %7 %8 %9

if errorlevel 1 (
    exit /b 1
)

rem restore previous folder:
cd %CURRENTDIR%

exit /b 0