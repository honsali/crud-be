@echo off
setlocal

rem Use JDK 25 for the Spring Boot 4 showcase
set "JAVA_HOME=C:\Logiciels\jdk-25.0.3+9"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "APP_SECURITY_ALLOW_UNSAFE_DEV_SECRET=true"
set "SPRING_PROFILES_ACTIVE=dev"
set "SPRING_LIQUIBASE_CONTEXTS=dev"

call mvnw.cmd spring-boot:run

endlocal
pause
