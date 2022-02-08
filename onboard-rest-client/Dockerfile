FROM adoptopenjdk/openjdk8:alpine
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
ADD capability_dat_no.json /
ADD localSubscription_dat_no.json /
ADD false_localSubscription_dat_no.json /
ADD local_private_channel.json /
ADD remote_private_channel.json /
ADD ivi_no_localSubscription.json /
ADD denm_no_delivery.json /
ENTRYPOINT ["/entrypoint.sh"]
