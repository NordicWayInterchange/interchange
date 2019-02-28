CREATE USER hibernate WITH PASSWORD 'hibernate';
CREATE USER spring WITH PASSWORD 'spring';
CREATE DATABASE spring_hibernate_test;
GRANT ALL PRIVILEGES ON DATABASE spring_hibernate_test TO hibernate;
GRANT ALL PRIVILEGES ON DATABASE spring_hibernate_test TO spring;