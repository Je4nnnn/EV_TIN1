FROM openjdk:17
ARG JAR_FILE=./target/*.jar
COPY ${JAR_FILE} kartingRM.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "kartingRM.jar"]