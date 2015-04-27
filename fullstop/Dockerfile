FROM zalando/openjdk:8u40-b09-4

MAINTAINER Zalando SE

COPY target/fullstop.jar /

RUN mkdir $FULLSTOP_LOGS

EXPOSE 8080

CMD java $(java-dynamic-memory-opts) -jar /fullstop.jar