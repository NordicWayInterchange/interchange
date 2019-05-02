FROM postgres:9.6

ADD schema.sql /docker-entrypoint-initdb.d/01_schema.sql
ADD data.sql /docker-entrypoint-initdb.d/02_data.sql


