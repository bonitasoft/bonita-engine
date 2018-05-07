@echo off
setlocal EnableDelayedExpansion

:: Let's position into folder containing this script:
set CURRENTDIR="%cd%"
set BASEDIR=%~dp0
cd %BASEDIR%

:: Check if JAVA_CMD has been passed by start-bonita.bat:
if not "%JAVA_CMD%" == "" goto gotJavaCmd
set JAVA_CMD="java"
:gotJavaCmd

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

:: extract specific parameters
set otherArgs=
set JVM_OPTS=
FOR %%a IN (%*) DO (
    if "%%a" == "--debug" (
        set JVM_OPTS="-Dbonita.platform.setup.log=DEBUG"
    ) else (
        set otherArgs=!otherArgs! %%~a
    )
)

"%JAVA_CMD%" -cp "%BASEDIR%;%CFG_FOLDER%;%INITIAL_CFG_FOLDER%;%LIB_FOLDER%\*" %JVM_OPTS% -Dspring.profiles.active=default -Dsysprop.bonita.db.vendor=%BONITA_DATABASE% org.bonitasoft.platform.setup.PlatformSetupApplication %otherArgs%

if errorlevel 1 (
    exit /b 1
)

:: restore previous folder:
cd %CURRENTDIR%

exit /b 0