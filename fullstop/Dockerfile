FROM registry.opensource.zalan.do/stups/openjdk:8-42

MAINTAINER Zalando SE

CMD java $(java-dynamic-memory-opts) $(appdynamics-agent) -jar /fullstop.jar

COPY target/fullstop.jar /
COPY target/scm-source.json /
