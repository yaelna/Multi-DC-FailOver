# ITG-multiDC-failover
-----------------------------------------

[_Introduction_](#introduction)

[1. Building project](#building-project)

[2. Start XAP grid](#start-xap-grid)

[3. Demo DC1 failover](#demo-dc1-failover)

## Introduction

The goal of this project is to demonstrate a failover of Data Center 1 in 'NY' zone by redirecting all write requests to XAP in Data Center 2 in 'LN' zone and bootstrapping DC1 from DC2 when it is up again.
DC1 hosts 'source-space' statefull space with 2:0 partitioned cluster topology and replicates data to 'target-space' space in DC2 over WAN. 
In other words 'master-slave' WAN gateway replication topology is used. DC1 is a master cluster and DC2 is a slave cluster. 
After DC1 is down, DC2's space continues handling data requests. When DC1 is restarted,  'wan-bootstrap' application bootstraps it from DC2.  

The projects includes:
- source and target space PUs
- WAN gateway components
- feeder, that writes data to the source space and if it is unreachable it starts writing data to the target space.
- wan-bootstrap application, that changes the replication topology to master-master and performs DC1's space bootstrapping

## Building project

This project is based on Maven, so to build it, you would run next command:

```bash
cd "path to the project dir"
mvn clean package
```

## Start XAP grid
Login to host1 and start the first XAP cluster in zone NY.
Login to host2 and start the second XAP cluster in zone LN.
You may want to start XAP webui tool.

## Demo DC1 failover

1. Deploy source-space.jar and source-delegator.jar to DC1 on the host1 
- copy the artifacts to current directory 
- edit and run the following commands:

```bash
export XAP_LOOKUP_LOCATORS="host1-ip:4174"
"path to XAP_HOME"/bin/gs.sh deploy -zones NY source-space.jar
"path to XAP_HOME"/bin/gs.sh deploy -zones NY -properties "embed://source.host=host1-ip;target.host=host2-ip" source-delegator.jar
```

2. On the host2 deploy target-space.jar and target-sink.jar to DC2:

```bash
export XAP_LOOKUP_LOCATORS="host2-ip:4174"
"path to XAP_HOME"/bin/gs.sh deploy -zones LN target-space.jar
"path to XAP_HOME"/bin/gs.sh deploy -zones LN -properties "embed://source.host=host1-ip;target.host=host2-ip" target-sink.jar
```

3. Observe that WAN Gateway replication works by starting Feeder on the host1:

```bash
java -cp "location path"/feeder-${version}-with-dependencies.jar" com.gigaspaces.app.Feeder "host1-ip:4174,host2-ip:4174"
```
4. Shutdown DC1 by killing GS Agent process on the host1.
Observe that feeder continues writing data to the target space.

5. Start XAP grid on the host 1 in zone NY. Observe that 'source-space' and 'source-delegator' PUs are up and running.

6. Perform DC1 bootstrapping from DC2.
Deploy source-sink.jar to DC1 and target-delegator.jar to DC2. They are required for bootstrapping.
On the host 1 run the following command from the dir where source-sink.jar is located:

```bash
"path to XAP_HOME"/bin/gs.sh deploy -zones NY -properties "embed://source.host=host1-ip;target.host=host2-ip" source-sink.jar
```

On the host 2 run the following command from the dir where target-delegator.jar is located:

```bash
"path to XAP_HOME"/bin/gs.sh deploy -zones LN -properties "embed://source.host=host1-ip;target.host=host2-ip" target-delegator.jar
```

On the host1 run 'wan-bootstrap' application: 

```bash
java -cp "location path"/wan-bootstrap-${version}-with-dependencies.jar" com.gigaspaces.app.Main "host1-ip:4174,host2-ip:4174"
```
