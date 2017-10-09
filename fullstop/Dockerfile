FROM registry.opensource.zalan.do/stups/openjdk:latest

MAINTAINER Zalando SE

CMD java $(java-dynamic-memory-opts) $(appdynamics-agent) -jar /fullstop.jar

COPY target/fullstop-executable.jar /fullstop.jar
COPY target/scm-source.json /
