#!/bin/bash
set -x
GROUP=`hostname`
#$1 will be concatenated to the docker container name (i.e xap1 xap2 ...)
#$2 is the requested build full path

echo "1 = $1"
echo "2 = $2"

docker run --privileged --env XAP_LOOKUP_GROUPS=$GROUP --env LOOKUPGROUPS=$GROUP --name xap$1 --volume $2:/xap --volume `pwd`/shared-folder:/shared --volume `pwd`/output:/output --volume `pwd`/jars:/jars --hostname=xap$1 --volume ${SGTEST_DIR}/checkouts:/checkouts --detach agent /bin/bash -c "cp -rf /xap/deploy / && /usr/bin/supervisord -c /etc/supervisord.conf";
docker exec -d xap$1 /bin/bash -c "cd /xap/bin && ./gs-agent.sh &"