Interchange node under the Nordic Way project
====

### License
See full MIT license text [here](license.md).

See instructions to retrieve all the third party licences [here](#third-party-licenses)

### Introduction
The Nordic Way Interchange Node (NWIXN) is a clustered message broker that 
enables location-based exchange of traffic data. Clients, known as Service Providers send traffic information
to the broker in the form of AMQP messages. These messages are distributed to subscribing partners, either on the same interchange,
or on other interchanges in the cluster.

Messages are produced and exchanged over AMQP 1.0. Message content is not examined, but all messages must provide 
header attributes as specified in the client specification. 

Traffic message formats supported are DATEX2, DENM, IVI, SPATEM, MAPEM, SREM, SSEM, and CAM.  

![Interchange architecture](/diagrams/updated_federated_node.png)

An Interchange network consists of one or more interchanges, registered in the DNS (domain name server).
Each Interchange has a matching SRV record, which points to the Interchanges' control channel host name and port.

All interchanges must present the message types they produce - capabilities - in this discovery process.

Interchanges also has a set of message types they are interested in - subscriptions. 
If a neighbour interchange produces a message type we are interested in, we issue a subscription request to the neighbour.

Accepted subscriptions will be set up by the Routing Configurer so finally the messages can be collected by the Message collector.

Additional information about the Nordic Way Interchange Node can be found [here](https://www.nordicway.net/).

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
is automatically built by GitHub and published to the container registry europe-west4-docker.pkg.dev/nw-shared-w3ml/nordic-way-interchange/ on
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

