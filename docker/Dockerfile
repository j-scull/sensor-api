FROM openjdk:17-oracle

ARG JAR_FILE
ADD $JAR_FILE /usr/share/sensor-api/service.jar

ENTRYPOINT /usr/bin/java -jar /usr/share/sensor-api/service.jar

EXPOSE 8080