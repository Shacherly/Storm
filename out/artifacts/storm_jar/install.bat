@echo off
cd /d "D:\Project\DUYI_EDU\2LevelOne\storm\out\artifacts\storm_jar"
mvn install:install-file -Dfile=storm.jar -DgroupId=org.mufasa -DartifactId=storm -Dversion=1.0.0 -Dpackaging=jar
pause