#!/bin/bash

set -e
set -u

CONTAINER=$(docker ps -aqf "name=mpi_mpi-mediator.1")
echo $CONTAINER
docker logs --follow ${CONTAINER}
