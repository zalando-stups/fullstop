FROM zalando/openjdk:8u45-b14-5

MAINTAINER Zalando SE

COPY target/${project.artifactId}-${project.version}.jar /fullstop.jar
COPY target/docker/scm-source.json /

EXPOSE 8080

CMD java $(java-dynamic-memory-opts) $(appdynamics-agent) $(newrelic-agent) -jar /fullstop.jar
