#!/bin/sh -l
export codepoint=/opt/os/codepoint/codepoint.jar
exec /usr/bin/java \
  -Xmx64m \
  -XX:+UseParallelGC \
  -XX:MinHeapFreeRatio=20 \
  -XX:MaxHeapFreeRatio=40 \
  -XX:GCTimeRatio=4 \
  -XX:AdaptiveSizePolicyWeight=90 \
  -Dlogback.configurationFile=/opt/geosearch/logback.xml \
  -jar /opt/geosearch/*.jar \
  >> /var/log/geosearch/geosearch.log 2>&1
