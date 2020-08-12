Interchange node under the Nordic Way project
====

### License
See full MIT license text [here](license.md).

####Third-party licenses
To get a list of the third-party licenses used in the project, run (in bash):
```
mvn license:add-third-party
```

followed by:
```
find . -name THIRD-PARTY.txt -exec cat {} \; | sort | uniq 
``` 

### Introduction
The Nordic Way Interchange Node (NWIXN) is a message broker that
enables crowdsourcing of traffic data. Nordic Way partners send traffic information
to the broker in the form of AMQP messages. These messages are distributed to subscribing partners.

### Contact
For any questions please contact
* Christian Berg Skjetne: christian.berg.skjetne@vegvesen.no
* Roger Martinsen : roger.martinsen@bouvet.no

## Getting Started

### Prerequisites
* Maven 3.5.4
* Java 8
* Docker Engine 18.06.1-ce and Docker Compose 1.22.0

### Building the project with Maven
Make sure to build the project with Maven before running docker compose.
Maven starts temporary instances of the containers for integration
testing, and so the container ports will already be in use.

### Development environment
- Run `qpid/generate-keys.sh` to get keys and certs necessary for simulating a
  client/server relationship. These will be generated in `tmp/keys/`.
- run `docker-compose up --build` to start the environment.
- make sure you've run `mvn clean install` in `debugclient` so the jar is available for your test-client below.
- use `test-client.sh` to run the debugclient with all your generated keys.
  Adding a command line argument will modify the server name being used (default
  is localhost).

### Federation docker images
All the docker images specified in the github https://github.com/NordicWayInterchange/interchange/ 
is automatically built by CircleCI and published to the container registry eu.gcr.io/nordic-way-aad182cc/ on 
each commit. Each component in the system has its own registry.

All the images are tagged with git commit hash, and branch name. Branch "federation-master" is considered to be the stable branch.

The module "federation-st" is created to demonstrate how to start a set of containers for test using docker-compose.  

### Running the Tests
Unit tests are run in the maven 'test' stage, and integration tests are run in the 'verify' stage.

The profile "IT" separates the unit tests from the integration tests by ensuring the integration tests are run in the 'verify' stage. 
The profile "IT" is defined in the top pom and is always activated.

#### Docker containers in tests
Docker containers needed in the integration tests are started automatically by using the org.testcontainers framework.
Tests can be run from maven and the IDE with no extra configuration.

https://www.testcontainers.org/

#### Test framework
We use junit5 in our tests.
Version 1.x of the org.testcontainers has a dependency on junit4, so when writing a new test be sure to import test annontations from 'org.junit.jupiter.api' to avoid mixing junit 4 and 5 tests.
Version 2.x of org.testcontainers will probably support junit5.
 
https://junit.org/junit5/

#### Assertions
Fluent assertions from assertj-core shall be used. We use the version of assertj-core defined by spring-boot-starter-test.

https://assertj.github.io/doc/


### Deploy to kubernetes 
#### Helm
Helm uses Charts to pack all the Kubernetes components for an application to deploy, run and scale. This is also where the configuration of the application can be updated and maintained.

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


# Using Windows?

Make sure you have Git, Java and Maven installed.
Download and install docker as documented here: https://docs.docker.com/v17.09/docker-for-windows/install/#download-docker-for-windows

Note that Windows Subsystem for Linux 1 (WSL 1) needs to have virtualisation and Hyper-V enabled (see the section [What to know before you install](https://docs.docker.com/v17.09/docker-for-windows/install/#what-to-know-before-you-install) )

With WSL 2 there is no need to enable Hyper-V. When running a mix of WSL 1 and 2 Hyper-V can be enabled.

The actual start-up instructions are the same as for *nix systems:
- make sure docker is up and running
- run `mvn install` in the top code directory to compile all the needed code.
  - this might fail with an error message saying `unable to start container`. If it does, restart docker using the whale icon in the lower-left systems menu. Repeat the build when docker is strted (might take some time, so be patient)   
- run `docker-compose up --build` to start the environment. This will use the keys generated during the build procedure.
- to run the debug client, run the `test-client.bat`. This will start the debug client keys matching the ones mentioned in the previous step. 

In general, the docker daemon sometimes have problems mounting directories or starting the docker containers. Try restarting the container and try again after it is restarted, 
or remount the directories in the docker settings.

