@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.2.13-hotspot
echo JAVA_HOME is set to: %JAVA_HOME%
call mvnw.cmd clean compile
