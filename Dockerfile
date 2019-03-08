FROM maven:3.6.0-jdk-8 as build

COPY pom.xml pom.xml
COPY debugclient/pom.xml debugclient/pom.xml
COPY debugclient/src debugclient/src

COPY interchangenode/src interchangenode/src
COPY interchangenode/pom.xml interchangenode/pom.xml

COPY neighbour-model/src neighbour-model/src
COPY neighbour-model/pom.xml neighbour-model/pom.xml

COPY neighbour-discoverer/src neighbour-discoverer/src
COPY neighbour-discoverer/pom.xml neighbour-discoverer/pom.xml

COPY neighbour-server/src neighbour-server/src
COPY neighbour-server/pom.xml neighbour-server/pom.xml

COPY onboard-server/src onboard-server/src
COPY onboard-server/pom.xml onboard-server/pom.xml

COPY routing-configurer/src routing-configurer/src
COPY routing-configurer/pom.xml routing-configurer/pom.xml

RUN mvn compile test
