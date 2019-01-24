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

If sharing doesn't work (for example postrges saying it can't write log files),
try unsharing and then sharing your local disk (usually c:) in the docker settings under "Shared Drives"

Also, the docker command may sometimes fail to set up the containers properly. A restart of the docker daemon should fix it.
