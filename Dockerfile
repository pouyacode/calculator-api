FROM openjdk:11-jre-slim-bullseye
MAINTAINER Pouya Abbassi <me@pouyacode.net>

ADD target/*standalone.jar /calculator-api/app.jar

EXPOSE 8080

CMD ["java", "-Xmx32m", "-jar", "/calculator-api/app.jar"]
