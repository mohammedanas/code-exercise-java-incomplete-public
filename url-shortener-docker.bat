@echo off
setlocal
cd /d "%~dp0"
set "COMMAND=%~1"
if not defined COMMAND goto help

if /I "%COMMAND%"=="up" goto up
if /I "%COMMAND%"=="down" goto down
if /I "%COMMAND%"=="restart" goto restart
if /I "%COMMAND%"=="rebuild" goto rebuild
if /I "%COMMAND%"=="logs" goto logs
if /I "%COMMAND%"=="logs-follow" goto logs-follow
if /I "%COMMAND%"=="status" goto status
if /I "%COMMAND%"=="clean" goto clean
if /I "%COMMAND%"=="help" goto help

echo Unknown command: %COMMAND%
goto help

:up
docker compose up -d
exit /b %errorlevel%
:down
docker compose down
exit /b %errorlevel%
:restart
docker compose restart
exit /b %errorlevel%
:rebuild
docker compose up -d --build --force-recreate
exit /b %errorlevel%
:logs
docker compose logs --tail 100
exit /b %errorlevel%
:logs-follow
docker compose logs --tail 100 -f
exit /b %errorlevel%
:status
docker compose ps --all
exit /b %errorlevel%
:clean
docker compose down --rmi local --remove-orphans
exit /b %errorlevel%

:help
echo URL Shortener Docker commands:
echo   url-shortener-docker.bat up           Start the application
echo   url-shortener-docker.bat down         Stop and remove containers
echo   url-shortener-docker.bat restart      Restart running containers
echo   url-shortener-docker.bat rebuild      Rebuild and recreate services
echo   url-shortener-docker.bat logs         Show the last 100 log lines
echo   url-shortener-docker.bat logs-follow  Follow service logs
echo   url-shortener-docker.bat status       Show container status
echo   url-shortener-docker.bat clean        Remove containers and project images
echo.
echo All commands preserve the urlshortener-data database volume.
exit /b 0