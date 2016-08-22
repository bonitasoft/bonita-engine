@echo off
setlocal

rem Let's position into folder containing this script:
set CURRENTDIR="%cd%"
set BASEDIR=%~dp0
cd %BASEDIR%

set CFG_FOLDER=%BASEDIR%\platform_conf
set INITIAL_CFG_FOLDER=%CFG_FOLDER%\initial
set LIB_FOLDER=%BASEDIR%\lib

rem JAVA_OPTS="-Xss128m"

FOR /F "tokens=1,* delims== eol=#" %%A IN (database.properties) DO (set %%A=%%B)
set BONITA_DATABASE=%db.vendor%

IF NOT "%BONITA_DATABASE%" == "h2" IF NOT "%BONITA_DATABASE%" == "postgres" IF NOT "%BONITA_DATABASE%" == "sqlserver" IF NOT "%BONITA_DATABASE%" == "oracle" IF NOT "%BONITA_DATABASE%" == "mysql"  (
    echo Cannot determine database vendor valid values are [h2, postgres, sqlserver, oracle, mysql].
    echo Please configure file %BASEDIR%database.properties properly.
    exit /b 1
)

IF NOT (%1) == () set ACTION=%1
IF NOT "%ACTION%" == "init" IF NOT "%ACTION%" == "pull" IF NOT "%ACTION%" == "push" (
    echo Missing action argument. Available values are: init, pull, push
    exit /b 1
)

echo using database %BONITA_DATABASE%
echo action is %ACTION%

java -cp "%BASEDIR%;%CFG_FOLDER%;%INITIAL_CFG_FOLDER%;%LIB_FOLDER%\*" -Dorg.bonitasoft.platform.setup.action=%ACTION% -Dspring.profiles.active=default -Dsysprop.bonita.db.vendor=%BONITA_DATABASE% org.bonitasoft.platform.setup.PlatformSetupApplication

if errorlevel 1 (
    echo ERROR 1 Executing platform setup
    exit /b 1
)

IF "%ACTION%" == "pull" (
        echo Pulled configuration:
        tree /F %CFG_FOLDER%\current 
)

rem restore previous folder:
cd %CURRENTDIR%

exit /b 0