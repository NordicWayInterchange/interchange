# Docker Compose systemtest scripts

This directory contains a couple of Docker Compose project for developers that wants to test code locally 
without depending on an external test environment.

## Systemtest clustered script

It uses a dummy DNS server (fake-dns-server) for lookup in the cluster, a test PKI, and two 
complete interchanges, a and b. These are run on an internal network (testing_net), but at the 
moment they exist on the same network, so linking between services and dns names are necessary.

To run the project make sure you have built the Java project (`mvn clean package` in the parent 
directory), then use the `systemtest.sh`.

If you encounter any problems starting the project, try doing either a hard prune of Docker,
(`docker container prune -f; docker network prune -f; docker volume prune -f` WARNING: the 
command will remove all containers, networks and volumes in Docker) if you don't have
any containers, network or volumes you want to keep on the Docker host, or remove any 
conflicting items using `docker container/volume/network rm`.

There are scripts to run the service provider client for a and b (`a_service_provider_client.sh` and `b_service_provider_client.sh`, respectively)
Example json input files are supplied in the `json` directory.

## Napcore testing - systemtest

We also have a test instance of Napcore frontend. 
To actually run the project, use the `systemtest-napcore.sh` script.

## Single node test

The `single-node-systemtest.sh` runs a single node of the cluster, and thus demands a bit less resources to run. Like the `systemtest.sh`
script, this uses the jar files built locally by Maven. It also runs both napcore and admin as a part of the cluster.


## Admin-frontend

The Admin portal provides system administrators with a view of the system. It is currently read-only and is designed to help administrators monitor the system,
investigate issues, and gather the information needed to diagnose and resolve problems.

