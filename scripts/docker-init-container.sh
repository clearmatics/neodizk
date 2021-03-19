#!/usr/bin/env bash

docker create \
       --name dizk \
       --tty \
       --interactive \
       --volume `git rev-parse --show-toplevel`:/home/dizk \
       --env "TERM=xterm-256color" \
       --network local_cluster_cluster-network \
       --ip 10.5.1.2 \
       dizk-base
