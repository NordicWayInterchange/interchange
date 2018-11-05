Interchange node under the Nordic Way project
====
## Under construction 

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
