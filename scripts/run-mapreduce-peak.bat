@echo off
setlocal
if "%~2"=="" (
  echo Usage: scripts\run-mapreduce-peak.bat inputPath outputPath
  exit /b 2
)
mvn -q -DskipTests package
hadoop jar target\website-visit-mapreduce-1.0.0.jar com.course.mapreduce.job.HourPeakJob %1 %2
