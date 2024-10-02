@echo off
setlocal EnableDelayedExpansion

:: Let's position into the folder containing this script:
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
set BONITA_BDM_DATABASE=%bdm.db.vendor%

call :checkVendorSupported %BONITA_DATABASE%
if ERRORLEVEL 1 (
    exit /b 1
)
call :checkVendorSupported %BONITA_BDM_DATABASE%
if ERRORLEVEL 1 (
    exit /b 1
)

"%JAVA_CMD%" -cp "%BASEDIR%;%CFG_FOLDER%;%INITIAL_CFG_FOLDER%;%LIB_FOLDER%\*" ^
    -Dsysprop.bonita.db.vendor=%BONITA_DATABASE% ^
    -Dsysprop.bonita.bdm.db.vendor=%BONITA_BDM_DATABASE% ^
    org.bonitasoft.platform.setup.PlatformSetupApplication %*

if ERRORLEVEL 1 (
    exit /b 1
)

:: restore previous folder:
cd %CURRENTDIR%

:: exit script with success
exit /b 0


REM Function that exits with an error message if the vendor is not supported
REM - first argument is the database vendor value to check
:checkVendorSupported
set DB_VENDOR=%1
set IS_SUPPORTED=false
for %%d in (h2 postgres) do (
    if "%%d" == "!DB_VENDOR!" (
        set IS_SUPPORTED=true
        goto :breakLoop
    )
)
:breakLoop
if "!IS_SUPPORTED!" == "false" (
    echo ERROR: Unsupported database vendor ^(valid values are h2, postgres^).
    echo For access to additional databases ^(oracle, mysql, sqlserver^), please consider upgrading to the Enterprise Edition.
    echo Please update file %BASEDIR%database.properties to set a valid value.
    exit /b 1
)
exit /b 0
