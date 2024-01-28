FROM openjdk:8-jre

ARG JAR_FILE
ADD ${JAR_FILE} /usr/share/sensor-api/${JAR_FILE}

ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/share/sensor-api/${JAR_FILE}"]

EXPOSE 8080