Norwegian National Interchange Node
====

### License
See full MIT license text [here](license.md).

See instructions to retrieve all the third party licences [here](#third-party-licenses)

### Introduction

The Norwegian National Interchange Node is a node implementing the C-ROADS C-ITS IP based Profile, Improved Interface 
2.1.0 spec by [C-ROADS](https://www.c-roads.eu/)

The Node contains a broker (Qpid Broker-J) and several other components to negotiate data exchange with other nodes 
(interchanges) in the same network over the AMQP(S) 1.0 protocol.
The node support the DENM, IVI, SPATEM, MAPEM, SREM, SSEM, and CAM message formats, as specified in the C-ROADS spec, 
as well as DATEX2.

The interchange is designed to run as a member of an interchange cluster, which runs across organization boundaries, 
typically traffic OEMs and national road authorities.  A top-level administrating organization will control the members
of the cluster using DNS.


## Overall Architecture
![Interchange architecture](/diagrams/updated_federated_node.png)

### Discovery 

Membership in the network is determined by an Owning Domain, which is a domain listed in DNS. A node looks up the 
well-known Owning Domain in DNS, and finds all the participating nodes are listed in that domain as SRV records. It is 
up to each Node to communicate with all the other nodes in the network, and to exclude any other node that is not listed
in the DNS.

### Control Plane

The nodes communicate over HTTPS, using client certificates stemming from a well-known root CA to ensure two-way trust.
The Improved Interface protocol, as it is called, is described in the [specification](https://www.c-roads.eu/).
We also maintain a swagger of our implementation of the protocol (which we call Neighbour API) 
[here](https://nordicwayinterchange.github.io/interchange-swagger-docs/swagger-neighbour/)


### Local Actor Plane

Local Actors (ie the users of the system), can request to add Capabilities and Subscriptions to the system, and to 
deliver data over a Delivery.
A Local Actor API is provided for the actors to be able to negotiate subscriptions, capabilities and deliveries, and 
to find the AMQP endpoints to read from or write to.
The local actor plane is not a part of the C-ROADS spec, and is continuously evolving depending on the needs of local 
actors.
We maintain a swagger of the implementation (which we call Onboard API) 
[here](https://nordicwayinterchange.github.io/interchange-swagger-docs/swagger-onboard/)

### Message Plane

Messages are typically sent by a Local Actor over AMQPS 1.0 to a broker endpoint provided by the system, and are 
received by interested parties on either the same or a different broker. Messages are exchanged between brokers by the 
system.

Norwegian National Interchange Node was initially developed as a part of the 
[Nordic Way project](https://www.nordicway.net/).

### Contact
For any questions please contact
* Christian Berg Skjetne: christian.berg.skjetne@vegvesen.no

## Getting Started

### Prerequisites
* Maven 3.9.x or later
* Java 21
* Docker 

### Building the project with Maven
The maven build uses [testcontainers](https://www.testcontainers.org/) in the integration test stage, so you will need Docker installed locally
in order to run the integration tests.
To build the project without integration test, use `mvn package`, and to run the integration tests, run `mvn verify`.

### Running the Tests
Unit tests are run in the maven 'test' stage, and integration tests are run in the 'verify' stage.

The profile "IT" separates the unit tests from the integration tests by ensuring the integration tests are run in the 'verify' stage. 
The profile "IT" is defined in the top pom and is always activated.

### Run using Docker Compose

We have created a few Docker Compose configs in the `demo` folder, with associated startup scripts.
The folder `single-node` starts up a single-node interchange in Docker Compose, called `a.interchangedomain.com`.
There's a short tutorial [here](demo/single-node/README.md)



## Federation docker images
All the docker images specified in the github https://github.com/NordicWayInterchange/interchange/
is automatically built by GitHub and published to the container registry ghcr.io/nordicwayinterchange/ on
each commit. Each component in the system has its own registry.

All the images are tagged with the 7-character start of the git commit hash. Branch "federation-master" is considered to be the stable branch.

### Deploy to kubernetes 
#### Helm
Helm uses Charts to pack all the Kubernetes components for an application to deploy, run and scale. This is also where 
the configuration of the application can be updated and maintained.

The helm templates is provided under helm/interchange/templates. 
See the example values file helm/interchange/example_values.yml 

### Resources
[Github Wiki](https://github.com/NordicWayInterchange/interchange/wiki)

[Nordic Way](https://www.nordicway.net/)

# Third-party licenses 
To get a list of the third-party licenses used in the project, run (in bash):
```
mvn license:add-third-party
```

followed by:
```
find . -name THIRD-PARTY.txt -exec cat {} \; | sort | uniq 
``` 

