docker container rm --force                   project-container
docker image     rm --force                                                         project-image
docker image     build                                                        --tag project-image .
docker container create                --name project-container --tty --interactive project-image /bin/bash
docker container start --attach --interactive project-container
