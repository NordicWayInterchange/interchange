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

### On Windows?

You might experience some problems when using Windows. This is due to the ```docker-entrypoint.sh``` script having trailing 
Windows newlines. The user will se an error along the lines of 
```
standard_init_linux.go:190: exec user process caused "no such file or directory"
```
To fix the problem, run the ```clean_qpid_script.bat``` script, which converts the script to Unix line endings.


### Queues
Queues created: 
* onramp
* test-out


## Logstash container

The Dockerfile is in the "logstash" directory under docker-compose.
To create an image:
```
docker build -t logstash_image .
```

Run the container:
```
docker run --rm -it -p 9200:9200 -p 9300:9300 logstash_image
```



## Docker compose

The ```docker-compose.yml``` describes the setup of
five different docker images.
 * Postgis
    * Holds the PostGIS database. Performs lookups and logs to a .
    volume it shares with filebeat.
 * Filebeat
    * Harvests logs from a volume it shares with postgis
    and forwards them to logstash.
 * Logstash 
    * Receives logs from filebeat. Parses them and sends them to 
    elasticsearch
 * Elasticsearch
    * Stores the logfiles
 * Kibana
    * Visualizes the files in elasticsearch

To build all five containers run : 

```
docker-compose build
```

This builds the images for all the containers.

Run the images with:
````
docker-compose up -d
````

You now have five running containers. 

## Networks
The docker compose file defines two different networks. One network is defined
for the containers dealing with the logging. This network is called elknet. 
One network is defined for the application containers. This network is called interchange. 

## Logging service

We use the Elastic Stack to store and query logs from the
different services. 

The elastic containers
have their own network defined in the docker-compose.yml. This is
a separate network within the IXN that binds these containers
together, and prevents the application containers from accessing
them.

### Logfiles

At the moment we are pushing logs only from postgis and qpid into the 
elastic stack. When the Interchange App is up and running, 
we will also add logs from the Interchange App to the elastic stack. 

Both postgis and qpid have existing functions for logging. 
Postgis creates both .log and .csv files. Qpid logs only in .log 
for the moment.



The logs for postgis are located in the postgis container at 
```
/var/log/postgresql
```

and on the host at: 
```
./postgis_logs
```

The logs for qpid are located in the qpid container at
```
/var/qpid/log
```

and on the host at:
```
./qpid_logs
```

The logs for InterchangeApp are located in the container at
```
/var/interchange/log
```

and on the host at: 
```
./interchangeapp_logs
```


To view the logs, either inspect them individually from these 
folders, or use Kibana for a more user friendly interface.

### Filebeat
Is a fileshipper that ships the logfiles from the application to Logstash. 

Filebeat shares a volume with the application container where the
application logs are stored, and forwards the logs to Logstash.
This volume is read only from the Filebeat side.

Filebeat is only
forwarding .log files at the moment. It would perhaps be easier
to use .csv for the future to set up filters or pipelines in 
logstash. 

### Logstash
Logstash ingests data from mulitple sources (filebeat instances), 
with the possibility of structuring them, making them easier to 
query. At this point we have not done any structuring of logs in 
logstash, but this is a clear point of development for the next
iteration. 

### Elasticsearch
Elasticsearch stores the logfiles and makes them available to Kibana.

Elasticsearch needs quite a bit of memory to run. If the default memory limit 
of Docker is not set to at least 4 GB, Elasticsearch can be quite unstable. 

For debugging purposes, the Elasticsearch port 9200 is also bound
to the same port on localhost. This binding should be removed once 
we are confident everything is working, so that Kibana is the 
only entrypoint to the logs.


#### Seeing the number of files in Elasticsearch
```
http://localhost:9200/_count?pretty
```


### Kibana
In the docker-compose file we bind Kibana's port 5601 port to the 
same port on localhost to access the visualization function 
that Kibana offers.

Kibana can take some time at startup to connect with elasticsearch
to display the logfiles.


To open Kibana:
```
http://localhost:5601
```

#### Kibana index patterns
An index pattern tells Kibana which Elasticsearch indices you
want to explore. An index pattern is created to select files
from Elasticsearch, so the index pattern must match files 
that already exist in Elasticsearch.

*If you want to view logfiles using Kibana, you must first create
an index pattern*

#### Viewing index patterns
To see existing index patterns use:
```
http://localhost:9200/_cat/indices
```

#### Defining an index pattern
To create an index pattern, open Kibana in your
browser and navigate to ```Management```. If this is your first
index pattern, the ```Create Index Pattern``` page opens automatically.
Otherwise click `Index Pattern` under `Kibana`. 

Enter your index pattern and click ```Next Step```. 
Choose if you want to add a ``Time Filter``. 
Click ``Create index pattern`` to create the index pattern.




---------------

### TODO
 * pipeline configs.
 * Logs from Interchange app to elastic stack

# Using Windows?

If sharing doesn't work (for example postrges saying it can't write log files),
try unsharing and then sharing your local disk (usually c:) in the docker settings under "Shared Drives"

#Contact 
christian.berg.skjetne@vegvesen.no
