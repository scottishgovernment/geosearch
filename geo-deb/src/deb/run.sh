#!/bin/sh -l
export codepoint=/opt/os/codepoint/codepoint.jar
exec /usr/bin/java \
  -Xmx64m \
  -XX:GCTimeRatio=10 \
  -Dlogback.configurationFile=/opt/geosearch/logback.xml \
  -jar /opt/geosearch/*.jar \
  >> /var/log/geosearch/geosearch.log 2>&1
