FROM mdillon/postgis

ADD init.sql /docker-entrypoint-initdb.d/
ADD postgresql.conf /etc/postgresql/postgresql.conf

CMD ["-c", "config_file=/etc/postgresql/postgresql.conf"]
