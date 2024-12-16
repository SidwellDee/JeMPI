#!/bin/bash

set -e
set -u

CONTAINER=$(docker ps -aqf "name=openhim-mapping-mediator_openhim-mapping-mediator")
echo $CONTAINER
docker logs --follow ${CONTAINER}
