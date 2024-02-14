FROM alpine:3.19.1

RUN  apk update \
  && apk upgrade \
  && apk add ca-certificates \
  && update-ca-certificates \
  && apk add --update coreutils && rm -rf /var/cache/apk/* \
  && apk add --update openjdk17 curl bash \
  && apk add --no-cache nss \
  && rm -rf /var/cache/apk/*

ARG JAR_FILE
ADD $JAR_FILE /usr/share/sensor-api/service.jar

ENTRYPOINT /usr/bin/java -jar /usr/share/sensor-api/service.jar
