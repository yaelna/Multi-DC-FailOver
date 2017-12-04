#!/bin/bash

echo "tearing down sgtest test"

#stop and remove all dockers
docker rm -f $(docker ps -a -q)