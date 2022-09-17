FROM openjdk:17-alpine
MAINTAINER kjoshi007
COPY target/git-consumer-service-1.0.0.jar git-consumer-service-1.0.0.jar
ENTRYPOINT ["java","-jar","/git-consumer-service-1.0.0.jar"]