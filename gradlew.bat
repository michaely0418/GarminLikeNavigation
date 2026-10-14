@echo off
setlocal
set "PROJECT_DIR=%~dp0"
if not exist "%PROJECT_DIR%gradle\wrapper\gradle-wrapper.jar" (
  echo Missing verified Gradle wrapper JAR.
  echo First run: powershell -ExecutionPolicy Bypass -File scripts\bootstrap-gradle-wrapper.ps1
  exit /b 2
)
java -classpath "%PROJECT_DIR%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
exit /b %ERRORLEVEL%
