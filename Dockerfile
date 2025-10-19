FROM openjdk:22-slim
WORKDIR /app
COPY modules/bootstrap/api-payment-gateway/build/libs/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]