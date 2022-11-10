#FROM openjdk:17-jdk-slim-buster
FROM eclipse-temurin:17-jre-alpine

## script that will wait on the availability of a host and TCP port
COPY docker/wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
#ARG JAR_FILE=build/libs/jworldcup-be-sb.jar
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar 

ENTRYPOINT sleep 30; java -jar app.jar
