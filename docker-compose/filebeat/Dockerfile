FROM docker.elastic.co/beats/filebeat:6.4.2

# Copying custom filebeat.yml from host to container.
# Making logstash.yml visible to filebeat.yml
USER root
COPY filebeat.yml /usr/share/filebeat/filebeat.yml
COPY default.yml /usr/share/filebeat/prospectors.d/default.yml
RUN mv /usr/share/filebeat/modules.d/logstash.yml.disabled /usr/share/filebeat/modules.d/logstash.yml
RUN chown root:filebeat /usr/share/filebeat/filebeat.yml
RUN chown root:filebeat /usr/share/filebeat/prospectors.d/default.yml
