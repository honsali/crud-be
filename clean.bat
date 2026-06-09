@echo off
setlocal

rem Use JDK 25 for the Spring Boot 4 showcase
set "JAVA_HOME=C:\Logiciels\jdk-25.0.3+9"
set "PATH=%JAVA_HOME%\bin;%PATH%"

call mvnw.cmd -DskipTests clean package

endlocal
pause
