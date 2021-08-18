#!/usr/bin/env bash

set -e
set -x

echo "============================================================"
echo "==      BUILDING DOCKER IMAGES FOR LOCAL DEVELOPMENT      =="
echo "============================================================"

# Build base image if required
docker build -t dizk-base -f Dockerfile-base .

# Build dizk development image if required
docker build -t dizk-dev -f Dockerfile-dev .

# Create the virtual network (if is wasn't already created by docker-compose)
if ! (docker network ls | grep local_cluster_cluster-network) ; then
    # Match the details in scripts.local_cluster/docker-compose.yml,
    # including the name, which is based on the file location.
    docker network create \
           --driver bridge \
           --attachable \
           --subnet 10.5.0.0/16 \
           local_cluster_cluster-network
fi

# Create the development image based on the dizk-dev
docker create \
       --name dizk \
       --tty \
       --interactive \
       --volume `git rev-parse --show-toplevel`:/home/dizk \
       --env "TERM=xterm-256color" \
       --network local_cluster_cluster-network \
       --ip 10.5.1.2 \
       --env SPARK_MASTER_HOST=cluster-master \
       dizk-dev

# Network can be (re)connected after creation:
#   docker network connect --ip 10.5.1.2 local_cluster_cluster-network dizk
