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

Maven will also build the images and push them to the Google Container Registry.
Since pulling images from Docker Hub and pushing images to GCR requires
authentication, `mvn install` must be run with environmental variables for
authentication. From a terminal with gcloud installed, where you have already
authenticated against google cloud, run the following to build the project and push
the images to GCR:

```
mvn clean install -Denv.DOCKERUSER=username -Denv.DOCKERPASSWD=password -Denv.GAUTHUSER=oauth2accesstoken -Denv.GAUTHTOKEN=$(gcloud auth print-access-token)
```

Replace `username` and `password` with your docker username and password.

### Development environment
- Run `generate-keys.sh` to get keys and certs necessary for simulating a
  client/server relationship. These will be generated in `tmp/keys/`.
- make sure you've run `mvn clean install` in both `interchangenode` and
  `debugclient` so the jars are available for your builds/tests
- run `docker-compose up --build` to start the environment.
- use `test-client.sh` to run the debugclient with all your generated keys.
  Adding a command line argument will modify the server name being used (default
  is localhost).

### Running the Tests


### Resources
[Trello](https://trello.com/b/MXlcCmye/interchange)
[Github Wiki](https://github.com/NordicWayInterchange/interchange/wiki)
[Nordic Way](http://vejdirektoratet.dk/EN/roadsector/Nordicway/Pages/Default.aspx)


# Using Windows?

If sharing doesn't work (for example postrges saying it can't write log files),
try unsharing and then sharing your local disk (usually c:) in the docker settings under "Shared Drives"

