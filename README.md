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
 

### Docker compose
The Interchange Node is started using Docker compose. The compose file 
`docker-compose.yml` will start all the containers, expose container ports
and bind directories on the host to directories in the containers. 

To use docker compose, navigate to `src/main/docker`.

* Run ``docker-compose -up -d `` to start all containers in the background.
* To stop all containers run ``docker-compose down``

### Running the Tests


### Resources
[Trello](https://trello.com/b/MXlcCmye/interchange)   
[Github Wiki](https://github.com/NordicWayInterchange/interchange/wiki)   
[Nordic Way](http://vejdirektoratet.dk/EN/roadsector/Nordicway/Pages/Default.aspx) 


# Using Windows?

If sharing doesn't work (for example postrges saying it can't write log files),
try unsharing and then sharing your local disk (usually c:) in the docker settings under "Shared Drives"

