FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY ./application/target/spring-reactive-bank.jar /app/spring-reactive-bank.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/spring-reactive-bank.jar"]
