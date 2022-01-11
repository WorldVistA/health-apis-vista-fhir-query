@echo off
echo "Welcome to the health-apis interactive test suite!"
echo "Pulling docker image..."
set dockerImage=vasdvp/health-apis-vista-fhir-query-interactive-tests:latest
docker pull %dockerImage%
set scriptDir=%~dp0
set scriptDir=%scriptDir:~0,-1%
docker run --rm -it -v "%scriptDir%\test-properties:/sentinel/test-properties" %dockerImage%
cmd
