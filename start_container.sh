docker container rm --force project-container
docker image     rm --force project-image

docker image     build                                               --tag project-image .
docker container create --name project-container --tty --interactive       project-image /bin/bash

pushd ~/OneDrive/data/major/development/AngeloSalvade.MavenCentral.SigningKey
docker container cp gradle.docker.properties project-container:/root/.gradle/gradle.properties
docker container cp maven.central.key.gpg    project-container:/root/.gradle/maven.central.key.gpg
popd

docker container start --attach --interactive project-container
