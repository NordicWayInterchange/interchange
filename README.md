Interchange node under the Nordic Way project
====
## Under construction 




## PostGIS database container
To build and run the PostGIS image separately, 
use the instructions below. To build all the containers, 
use docker-compose. 

### Building the PostGIS docker image

From the directory containing the ```Dockerfile``` and the ```init.sql``` file, run the following command to build the PostGIS docker image
```
docker build -t postgis .
```

- ```-t``` gives the image the name  'postgis'. An optional tag can be added at this stage. A tag is added by giving the image a name in the 'name:tag' format.


This will build an image called ```postgis```. To see all the created images, use the command ```docker images```. By running this command we see our newly created image.

```
REPOSITORY          TAG                 IMAGE ID            CREATED             SIZE
postgis             latest              c4a97b185132        3 minutes ago       666MB
```



### Running the PostGIS docker image 
The following command runs the postgis-image, and creates a container called ```postgis_container``` :
```
docker run --name postgis_container -e POSTGRES_USER=geolookup -e POSTGRES_PASSWORD=geolookup -e POSTGRES_DB=geolookup -p 5432:5432 -d postgis
```

- ```-e``` Sets an environment variable in the container.
-  ```POSTGRES_USER=geolookup``` Creates a database user 'geolookup'
- ```POSTGRES_PASSWORD=geolookup``` Sets the password of the user to 'geolookup'
- ```POSTGRES_DB=geolookup``` Creates a database called 'geolookup'
- ```-d``` Sets the container to run in the background, in detached mode.


The output of ```docker container ls```  is now:
```
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS               NAMES
d2cabd523805        postgis             "docker-entrypoint.s…"   15 seconds ago      Up 14 seconds       5432/tcp            postgis_container
```



### Accessing the container shell
To access the container shell use the following command
```
docker exec -it postgis_container /bin/bash
```
- ```exec``` Runs a command in a running container
- ```-it``` Two flags: ```-i```, meaning interactive, keeps STDIN open even if the container is running in the background. ```-t``` allocates a pseudo-TTY. 


### Testing a lookup

To test a lookup, first login to psql with the username (```-U```) we created: 

```
root@1bbe9342922d:/# psql -U geolookup
```

This gives the psql prompt for the database 'geolookup'. 
```
geolookup=# 
```

We can now query the database with a given lat/lon
```
SELECT iso2 FROM worldshape_10kmbuffer WHERE ST_Within(ST_GeomFromText('POINT(10.0 63.0)'), worldshape_10kmbuffer.geom);
```

This query should give output 
```
iso2 
------
 NO
(1 row)
```

## QPID Docker Container

Create an image of the docker file
```
docker build -t qpid_image .
```

Run the container and expose the ports 
```
docker run --name qpid_container -p 8080:8080/tcp -p 5672:5672/tcp qpid_image
````

Access the managment web console at http://localhost:8080/ with the username/password admin/admin

### Queues
Queues created: 
* onramp
* test-out


## Logstash container

The Dockerfile is in the "logstash" directory.
To create an image:
```
docker build -t logstash_image .
```

Run the container:
```
docker run --rm -it -p 9200:9200 -p 9300:9300 logstash_image
```



## Docker compose
To set up all the containers simeltaneously, use docker-compose.

The ```docker-compose.yml``` describes the setup of
four different docker images, and the local network
bridge 'elknet'. The images are:
 * Postgis
    * Holds the PostGIS database. Performs lookups and logging.
 * Filebeat
 * Elasticsearch
 * Kibana

To build all four containers, 
navigate to the directory containing the Dockerfiles
and the ```docker-compose.yml``` file: 

````
docker-compose $tree
.
├── docker-compose.yml
├── filebeat
│   ├── Dockerfile
│   └── filebeat.yml
├── logs
└── postgis
    ├── Dockerfile
    ├── init.sql
    └── postgresql.conf

``````

Build the images listed in the ```docker-compose.yml``` by running the following command:


```
docker-compose build
```

This builds the images for all the containers.

To run the images:
````
docker-compose up -d
````

You now have four running containers and a network
called 'elknet':

````
CONTAINER ID        IMAGE                                    COMMAND                  CREATED             STATUS              PORTS                              NAMES
2318f16c0ea4        docker.elastic.co/beats/filebeat:6.4.1   "/usr/local/bin/dock…"   25 seconds ago      Up 24 seconds                                          filebeat
5999c0fd2305        kibana:6.4.1                             "/usr/local/bin/kiba…"   26 seconds ago      Up 25 seconds       0.0.0.0:5601->5601/tcp             kibana
cf9773e8c583        elasticsearch:6.4.1                      "/usr/local/bin/dock…"   28 seconds ago      Up 26 seconds       0.0.0.0:9200->9200/tcp, 9300/tcp   elasticsearch
9074535467ce        postgis-docker_postgis                   "docker-entrypoint.s…"   28 seconds ago      Up 26 seconds       0.0.0.0:5432->5432/tcp             postgis
````

````
NETWORK ID          NAME                     DRIVER              SCOPE
2b913cf8aa32        postgis-docker_elknet    bridge              local
````

### docker-compose TODO:
* Setting up logstash container
* Forwarding of logs from filebeat to logstash 


###
TODO

 * network setup
 * pointing to the elasticsearch container
 * pipeline configs.

#Contact 
christian.berg.skjetne@vegvesen.no
