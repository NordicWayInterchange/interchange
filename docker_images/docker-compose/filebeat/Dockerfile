FROM prima/filebeat
COPY filebeat.yml /usr/share/filebeat/filebeat.yml

USER root
RUN chown root:filebeat /user/share/filebeat/filebeat.yml
USER filebeat
