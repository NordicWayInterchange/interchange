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

