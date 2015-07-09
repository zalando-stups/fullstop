FROM zalando/openjdk:8u45-b14-2

MAINTAINER Zalando SE

COPY target/fullstop.jar /
COPY target/scm-source.json /

EXPOSE 8080

CMD java $(java-dynamic-memory-opts) $(newrelic-agent) -jar /fullstop.jar
