Interchange node under the Nordic Way project
====
## Under construction 


## PostGIS database container
### Building the PostGIS docker image

From the directory containing the Dockerfile and the init.sql file, run the following command to build the PostGIS docker image
```
docker build -t postgis .
```
- ```-t``` gives the image the name  'postgis'. An optional tag can be added at this stage. A tag is added by giving the image a name in the 'name:tag' format.

The output of the command ```docker images``` is now:

```
REPOSITORY          TAG                 IMAGE ID            CREATED             SIZE
postgis             latest              c4a97b185132        3 minutes ago       666MB
```

This will build an image called ```postgis```
### Running the PostGIS docker image 
The following command runs the postgis-image, and creates a container called ```postgis_container``` :
```
docker run --name postgis_container -e POSTGRES_USER=geolookup -e POSTGRES_PASSWORD=geolookup -e POSTGRES_DB=geolookup -d postgis
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
- ```-it``` To flags: ````-i```, meaning interactive, keeps STDIN open even if the container is running in the background. ````-t```, meaning tty, allocates a pseudo-TTY. 


Contact 
christian.berg.skjetne@vegvesen.no
