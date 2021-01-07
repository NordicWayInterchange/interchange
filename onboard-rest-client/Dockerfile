FROM java:8-alpine
RUN apk add --no-cache bash

COPY target/onboard-rest-client-1.0.3-SNAPSHOT.jar onboard-rest-client.jar
ADD entrypoint.sh /
RUN chmod u+x /entrypoint.sh
ADD datatype.json /
ADD dat_no_datatype.json /
ADD dat_se_datatype.json /
ADD ivi_no_datatype.json /
ADD ivi_se_datatype.json /
ADD denm_no_datatype.json /
ADD denm_se_datatype.json /
ENTRYPOINT ["/entrypoint.sh"]
