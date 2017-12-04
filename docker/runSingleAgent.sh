#!/bin/bash
set -x
GROUP=`hostname`
#$1 is the docker container name
#$2 is the requested build full path
#$3 is the requested zone
echo "creating docker container with name = $1"
echo "on top of build = $2"

docker run --privileged --env XAP_LOOKUP_GROUPS=$GROUP --env LOOKUPGROUPS=$GROUP --name $1 --volume $2:/xap --volume `pwd`/shared-folder:/shared --volume `pwd`/output:/output --volume `pwd`/jars:/jars --hostname=$1 --volume ${SGTEST_DIR}/checkouts:/checkouts --detach agent /bin/bash -c "cp -rf /xap/deploy / && /usr/bin/supervisord -c /etc/supervisord.conf";

echo "deploying agent with 3 gsc"
docker exec -d $1 /bin/bash -c "cd /xap/bin && ./gs-agent.sh --lus=1 --gsm=1 --gsc=3 &"