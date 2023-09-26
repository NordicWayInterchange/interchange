FROM eclipse-temurin:11-alpine
RUN apk add --no-cache bash alpine-conf
RUN setup-timezone -z Europe/Oslo

COPY target/onboard-rest-client-1.0.3-SNAPSHOT.jar onboard-rest-client.jar
ADD entrypoint.sh /
RUN chmod u+x /entrypoint.sh
ADD cap_king_gustaf_denm_no.json /
ADD dat_se_datatype.json /
ADD ivi_no_datatype.json /
ADD ivi_se_datatype.json /
ADD denm_no_datatype.json /
ADD denm_se_datatype.json /
ADD capability_dat_no.json /
ADD cap_king_olav_denm_no.json /
ADD sub_king_gustaf_denm_no.json /
ADD false_localSubscription_dat_no.json /
ADD local_private_channel.json /
ADD remote_private_channel.json /
ADD ivi_no_localSubscription.json /
ADD del_king_gustaf_denm_no.json /
ADD ivi_no_localSubscription.json /
ADD del_king_gustaf_denm_no.json /
ADD del_king_olav_denm_no.json /
ADD sub_king_olav_denm_no.json /
ADD sub_king_charles_denm_no.json /
ADD systemtest_remote_cap.json /
ADD redirect_sub_king_gustaf_denm_no.json /
ADD cap_king_charles_denm_no.json /
ADD sub_king_charles_denm_no.json /
ADD caps_king_olav.json /
ADD subs_king_olav.json /
ADD dels_king_olav.json /
ENTRYPOINT ["/entrypoint.sh"]
