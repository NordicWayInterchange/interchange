# Docker Compose systemtest scripts

This directory contains a Docker Compose project for developers that wants to test code locally 
without depending on an external test environment.
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

There are scripts to run the onboard client for a and b (`a_onboard_client.sh` and `b_onboard_client`,
respectively), as well as demo jms clients for a and b (`jms_client_a.sh` and `jms_client_b`, 
respectively).
Example json input files are supplied in the `json` directory.

## Napcore testing

We also have a test instance of Napcore frontend. To use it, make sure you have an Aut0 test
instance registered and configured, and the details filled in a file called `napcoresettings`.
The exact variables are listed in `napcoresettings.example`.
Please refer to the [Aut0 documentation](https://auth0.com/docs/quickstart/webapp/nextjs/interactive).
To actually run the project, use the `systemtest-napcore.sh` script.