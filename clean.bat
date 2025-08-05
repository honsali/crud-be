@echo off
setlocal

rem Définir le chemin vers la JDK 17
set "JAVA_HOME=C:\Logiciels\jdk21"
set "PATH=%JAVA_HOME%\bin;%PATH%"

rem Lancer Maven avec les bons arguments
mvn clean install  -DskipTests  -Dmaven.test.skip=true

endlocal
pause
