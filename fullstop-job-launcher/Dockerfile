FROM zalando/openjdk:8u45-b14-6

MAINTAINER Zalando SE

COPY target/fullstop-job-launcher.jar /
COPY target/scm-source.json /

EXPOSE 7979
EXPOSE 8080

CMD java $(java-dynamic-memory-opts) $(appdynamics-agent) $(newrelic-agent) -jar /fullstop-job-launcher.jar
