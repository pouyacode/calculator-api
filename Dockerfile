FROM openjdk:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/calculator-api-0.0.1-SNAPSHOT-standalone.jar /calculator-api/app.jar

EXPOSE 8080

CMD ["java", "-jar", "/calculator-api/app.jar"]
