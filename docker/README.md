## Building project

This project is based on Maven, so to build it, you would run next command:

```bash
cd "path to the project dir"
mvn clean package
```

## Building the docker image
In this project we included a Dockerfile to build a _agent_ docker image on your machine,
this may take a few minuts to complete.

```bash
./buildDockerImage.sh
```
to check docker image was added confirm 'agent' image in images list
```bash
docker image ls

REPOSITORY             TAG                 IMAGE ID            CREATED             SIZE
agent                  latest              dd0e937864cb        1 minutes ago      2.91 GB
```

## Demo DC1 failover

1. create two xap build folders, i.e gigaspaces-xap-12.2.1-source and gigaspaces-xap-12.2.1-target.

    in source build add the following to setenv-overrides.sh:
    
        export XAP_LOOKUP_LOCATORS="172.17.0.2:4174"
        export XAP_GSC_OPTIONS=-Dcom.gs.zones=NY ${XAP_GSC_OPTIONS}
    
    in target build add the following to setenv-overrides.sh:
   
        export XAP_LOOKUP_LOCATORS="172.17.0.3:4174"
        export XAP_GSC_OPTIONS=-Dcom.gs.zones=LN ${XAP_GSC_OPTIONS}

2. Spawn two docker containers, xap-source and xap-target, with a gs-agent with 3 gsc on it:

```bash
./runSingleAgent xap-source "path to source build"
./runSingleAgent xap-target "path to target build"
```

3. Deploy source-space.jar and source-delegator.jar to DC1 on xap-source container

first open docker bash interface:
```bash
docker exec -it xap-source bin/bash
```
within it run the following:

```bash
./xap/bin/gs.sh deploy -zones NY jars/source-space.jar
./xap/bin/gs.sh deploy -zones NY jars/source-delegator.jar
```

4. On xap-target container deploy target-space.jar and target-sink.jar:

first open docker bash interface:
```bash
docker exec -it xap-target bin/bash
```
within it run the following:

```bash
export XAP_LOOKUP_LOCATORS="host2-ip:4174"
./xap/bin/gs.sh deploy -zones LN jars/target-space.jar
./xap/bin/gs.sh deploy -zones LN jars/target-sink.jar
```

5. Observe that WAN Gateway replication works by starting Feeder:

```bash
java -cp ../feeder/target/feeder-1.0-SNAPSHOT-with-dependencies.jar com.gigaspaces.app.Feeder "172.17.0.2:4174,172.17.0.3:4174"
```
6. Shutdown DC1 by killing GS Agent process on xap-source container.
Observe that feeder continues writing data to the target space.

7. Start XAP grid on xap-source container:
first open docker bash interface:
```bash
docker exec -it xap-source bin/bash
```
within it run the following:

```bash
./xap/bin/gs-agent.sh --lus=1 --gsm=1 --gsc=3 &
```
Observe that 'source-space' and 'source-delegator' PUs are up and running.

8. Perform DC1 bootstrapping from DC2.
Deploy source-sink.jar to DC1 and target-delegator.jar to DC2. They are required for bootstrapping.

On  xap-source container run the following command:

```bash
./xap/bin/gs.sh deploy -zones NY jars/source-sink.jar
```

On xap-target run the following command from the dir where target-delegator.jar is located:

```bash
./xap/bin/gs.sh deploy -zones LN jars/target-delegator.jar
```

Run 'wan-bootstrap' application: 

```bash
java -cp ../wan-bootstrap/target/wan-bootstrap-1.0-SNAPSHOT-with-dependencies.jar com.gigaspaces.app.Main "172.17.0.2:4174,172.17.0.3:4174"
```
