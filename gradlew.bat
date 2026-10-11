@rem
@rem Copyright 2015 the original author or authors.
@rem
@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  Gradle startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

if not exist "%CLASSPATH%" (
    where gradle >nul 2>nul
    if %ERRORLEVEL% equ 0 (
        gradle %*
        goto end
    )
    echo Gradle wrapper jar not found. Please install Gradle or run 'gradle wrapper'.
    exit /b 1
)

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVACMD=java.exe
%JAVACMD% -version >nul 2>&1
if "%ERRORLEVEL%" == "0" goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
goto fail

:findJavaFromJavaHome
set JAVACMD=%JAVA_HOME%\bin\java.exe
if exist "%JAVACMD%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
goto fail

:execute
"%JAVACMD%" "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

:fail
exit /b 1

:end
if "%OS%"=="Windows_NT" endlocal
