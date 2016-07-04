FROM registry.opensource.zalan.do/stups/openjdk:8-28

MAINTAINER Zalando SE

CMD java $(java-dynamic-memory-opts) $(appdynamics-agent) -jar /fullstop-job-launcher.jar

COPY target/fullstop-job-launcher.jar /
COPY target/scm-source.json /
