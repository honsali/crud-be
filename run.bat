@echo off
setlocal

rem Use JDK 25 for the Spring Boot 4 showcase
set "JAVA_HOME=C:\Logiciels\jdk-25.0.3+9"
set "PATH=%JAVA_HOME%\bin;%PATH%"

if not defined APP_SECURITY_JWT_BASE64_SECRET (
    echo APP_SECURITY_JWT_BASE64_SECRET must be configured.
    exit /b 1
)

call mvnw.cmd spring-boot:run

endlocal
pause
