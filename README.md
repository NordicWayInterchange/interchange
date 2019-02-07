Interchange node under the Nordic Way project
====

The Nordic Way Interchange Node (NWIXN) is a message broker that
enables crowdsourcing of traffic data. Nordic Way partners send traffic information
to the broker in the form of AMQP messages. These messages are distributed to subscribing partners.

### Contact
For any questions please contact
* Christian Berg Skjente: christian.berg.skjetne@vegvesen.no
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

### Running the Tests
Unit tests are run by default, for instance when running `mvn clean install`.

To run the integration tests, activate the integration test profile (IT): 
`mvn clean install -PIT`

To start the docker containers for integration tests from maven so you can run the integration tests from your IDE:
`mvn docker:start -PIT`    

To stop the docker containers for integration tests from maven: 
`mvn docker:stop -PIT`

### Resources
[Trello](https://trello.com/b/MXlcCmye/interchange)

[Github Wiki](https://github.com/NordicWayInterchange/interchange/wiki)

[Nordic Way](http://vejdirektoratet.dk/EN/roadsector/Nordicway/Pages/Default.aspx)


# Using Windows?

Make sure you have Git, Java and Maven installed.
Download and install docker as documented here: https://docs.docker.com/v17.09/docker-for-windows/install/#download-docker-for-windows
Note that the physical box you are running needs to have virtualisation and Hyper-V enabled (see the section [What to know before you install](https://docs.docker.com/v17.09/docker-for-windows/install/#what-to-know-before-you-install) )

The actual start-up instructions are the same as for *nix systems:
- make sure docker is up and running
- run `mvn install` in the top code directory to compile all the needed code.
  - this might fail with an error message saying `unable to start container`. If it does, restart docker using the whale icon in the lower-left systems menu. Repeat the build when docker is strted (might take some time, so be patient)   
- run `docker-compose up --build` to start the environment. This will use the keys generated during the build procedure.
- to run the debug client, run the `test-client.bat`. This will start the debug client keys matching the ones mentioned in the previous step. 

In general, the docker daemon sometimes have problems mounting directories or starting the docker containers. Try restarting the container and try again after it is restarted, 
or remount the directories in the docker settings.

