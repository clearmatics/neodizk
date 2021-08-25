#!/usr/bin/env bash

set -e
set -x

echo "============================================================"
echo "==        BUILDING DOCKER IMAGES FOR LOCAL CLUSTER        =="
echo "============================================================"

# Build base image if required
docker build -t dizk-base -f Dockerfile-base .

# Build master image.
# Note, a standalone master would be started as follows:
#   docker run --rm -p 7077:7077 -p 8080:8080 dizk-cluster-master
docker build -t dizk-cluster-master -f Dockerfile-local-master .

# Build slave image
docker build -t dizk-cluster-slave -f Dockerfile-local-slave .
