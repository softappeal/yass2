echo off

docker       rm --force project
docker image rm --force project

set version=
set /p version=Version [ MAJOR.MINOR.PATCH or 'enter' for no-release ]?:
if "%version%" == "" goto norelease

docker build  --tag  project https://github.com/softappeal/yass2.git#v%version%
docker create --name project project ./gradlew -Pversion=%version% build publishYass2
goto continue

:norelease
docker build  --tag  project .
docker create --name project --tty --interactive project /bin/bash

:continue
pushd %HOMEPATH%\OneDrive\data\major\development\AngeloSalvade.MavenCentral.SigningKey
docker cp gradle.docker.properties project:/root/.gradle/gradle.properties
docker cp maven.central.key.gpg    project:/
popd

docker start --attach --interactive project
