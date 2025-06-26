FROM eclipse-temurin:17-jdk-alpine

#APP port
EXPOSE 8081

#Define App Directory
ENV APP_HOME=/usr/src/app 

#Create App Directory
RUN mkdir -p $APP_HOME

#Copy Compiled jar
COPY target/*.jar $APP_HOME/app.jar

#Set Working Directory
WORKDIR $APP_HOME

#Run the application
CMD ["java", "-jar", "app.jar"]
