FROM eclipse-temurin:17-jre-alpine

## script that will wait on the availability of a host and TCP port
COPY docker/wait-for-it.sh /wait-for-it.sh
RUN set -x; \
  chmod +x /wait-for-it.sh

## upload application jar file
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar 

ENTRYPOINT sleep 20; java -Djava.security.egd=file:/dev/./urandom -jar app.jar
