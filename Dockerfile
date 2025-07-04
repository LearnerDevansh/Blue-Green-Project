FROM eclipse-temurin:17-jre-alpine

WORKDIR /usr/src/app

EXPOSE 8081

COPY target/*.jar app.jar

CMD ["java", "-jar", "app.jar"]


