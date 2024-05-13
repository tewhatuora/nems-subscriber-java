# FROM maven:3.8.6-eclipse-temurin-17

# WORKDIR /app
 
# COPY pom.xml ./
 
# COPY src ./src
 
# CMD ["mvn", "clean","install"]


FROM openjdk:17-jdk-slim
WORKDIR /opt
COPY /target /opt
ENTRYPOINT ["java", "-jar", "./GuaranteedSubscriber-1.0.0.jar"]
