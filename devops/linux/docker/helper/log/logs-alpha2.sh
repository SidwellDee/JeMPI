#!/bin/bash

set -e
set -u

source ../../conf.env
docker service logs --follow --raw ${STACK_NAME}_alpha-02
echo
