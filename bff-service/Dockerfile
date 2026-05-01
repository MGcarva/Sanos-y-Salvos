FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml ./pom.xml
COPY bff-service/pom.xml ./bff-service/pom.xml
COPY bff-service/src ./bff-service/src
RUN apk add --no-cache maven && mvn -f bff-service/pom.xml clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Lambda Web Adapter: permite ejecutar Spring Boot en AWS Lambda sin cambios de código
COPY --from=public.ecr.aws/awsguru/aws-lambda-adapter:0.8.4 /lambda-adapter /opt/extensions/lambda-adapter
ENV PORT=8080
COPY --from=builder /app/bff-service/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
