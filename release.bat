echo off

docker container rm --force project-container
docker image     rm --force project-image

set version=
set /p version=Version [ MAJOR.MINOR.PATCH or 'enter' for no-release ]?:
if "%version%" == "" goto norelease

docker image     build                           --tag project-image https://github.com/softappeal/yass2.git#%version%
docker container create --name project-container       project-image ./gradlew -Pversion=%version% build publishYass2
goto continue

:norelease
docker image     build                                               --tag project-image .
docker container create --name project-container --tty --interactive       project-image /bin/bash

:continue
pushd %HOMEPATH%\OneDrive\data\major\development\AngeloSalvade.MavenCentral.SigningKey
docker container cp gradle.docker.properties project-container:/root/.gradle/gradle.properties
docker container cp maven.central.key.gpg    project-container:/root/.gradle/maven.central.key.gpg
popd

docker container start --attach --interactive project-container
