Interchange node under the Nordic Way project
====

### License
See full MIT license text [here](license.md).

See instructions to retrieve all the third party licences [here](#third-party-licenses)

### Introduction
The Nordic Way Interchange Node (NWIXN) is a message broker that
enables location-based exchange of traffic data. Nordic Way partners send traffic information
to the broker in the form of AMQP messages. These messages are distributed to subscribing partners.

Messages are produced and exchanged over AMQP 1.0. Message content is not examined, but all messages must provide 
header attributes as specified in the client specification. 

Traffic message formats supported are DATEX, DENM and IVI.  

![Interchange architecture](/diagrams/interchange_architecture.png)

Neighbour nodes registered in the DNS (domain name server) will be discovered via a control channel.

All neighbours must present the message types they produce - capabilities - in this discovery process.

Neighbours also has a set of message types they are interested in - subscriptions. 
If a neighbour produces a message type we are interested in, we issue a subscription request to the neighbour.

Accepted subscriptions will be set up by the Routing Configurer so finally the messages can be collected by the Message collector.

More information on the concepts and the open source implementation of the interchange node is found on the [Github Wiki](https://github.com/NordicWayInterchange/interchange/wiki) 

Additional information about the Nordic Way Interchange Node can be found [here](https://www.nordicway.net/).

### Contact
For any questions please contact
* Christian Berg Skjetne: christian.berg.skjetne@vegvesen.no
* Roger Martinsen : roger.martinsen@bouvet.no

## Getting Started

### Prerequisites
* Maven 3.5.x or later
* Java 8
* Docker Engine 18.06.1-ce and Docker Compose 1.22.0 or later

### Building the project with Maven
The maven build uses [testcontainers](https://www.testcontainers.org/) in the integration test stage, so you will need Docker installed locally
in order to run the integration tests.
To build the project without integration test, use `mvn build`, and to run the integration tests, run `mvn verify`.

 
### Federation docker images
All the docker images specified in the github https://github.com/NordicWayInterchange/interchange/ 
is automatically built by CircleCI and published to the container registry eu.gcr.io/nordic-way-aad182cc/ on 
each commit. Each component in the system has its own registry.

All the images are tagged with git commit hash, and branch name. Branch "federation-master" is considered to be the stable branch.

### Running the Tests
Unit tests are run in the maven 'test' stage, and integration tests are run in the 'verify' stage.

The profile "IT" separates the unit tests from the integration tests by ensuring the integration tests are run in the 'verify' stage. 
The profile "IT" is defined in the top pom and is always activated.

#### Test framework
We use junit5 in our tests.
https://junit.org/junit5/

#### Assertions
We aim at using fluent assertions using [assertj](https://assertj.github.io/doc/). We use the version of assertj-core defined by spring-boot-starter-test.

### Deploy to kubernetes 
#### Helm
Helm uses Charts to pack all the Kubernetes components for an application to deploy, run and scale. This is also where 
the configuration of the application can be updated and maintained.

The helm templates is provided under helm/interchange/templates. 
See the example values file helm/interchange/example_values.yml

### Connect to the server
We provide two clients to demonstrate how to publish messages to the interchange node.

1) no.vegvesen.interchange.DebugClient
2) no.vegvesen.ixn.Sink and Source

DebugClient must be run with -D parameters to point to keystore and truststore.
Source and Sink reads keystore and truststore instructions from property files.  

### Resources
[Trello](https://trello.com/b/MXlcCmye/interchange)

[Github Wiki](https://github.com/NordicWayInterchange/interchange/wiki)

[Nordic Way](http://vejdirektoratet.dk/EN/roadsector/Nordicway/Pages/Default.aspx)

# Third-party licenses 
To get a list of the third-party licenses used in the project, run (in bash):
```
mvn license:add-third-party
```

followed by:
```
find . -name THIRD-PARTY.txt -exec cat {} \; | sort | uniq 
``` 

