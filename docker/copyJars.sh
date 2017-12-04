#!/bin/bash

echo "packaging jars"

cd ..
mvn clean package
cp source-delegator/target/source-delegator.jar docker/jars
cp source-sink/target/source-sink.jar docker/jars
cp source-space/target/source-space.jar docker/jars
cp target-delegator/target/target-delegator.jar docker/jars
cp target-sink/target/target-sink.jar docker/jars
cp target-space/target/target-space.jar docker/jars