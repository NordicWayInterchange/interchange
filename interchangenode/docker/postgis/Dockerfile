FROM mdillon/postgis
COPY init.sql /docker-entrypoint-initdb.d/

# Copying custom config file from host to container and giving
# access rights to postgres user.
COPY postgresql.conf /etc/postgresql/postgresql.conf
RUN chmod go+r /etc/postgresql/postgresql.conf

# Installing VIM for debug purposes.
#RUN apt-get update -q\
#&& apt-get install -y -q vim

# setting Postgresql config file to our custom conf file.
CMD ["-c", "config_file=/etc/postgresql/postgresql.conf"]
