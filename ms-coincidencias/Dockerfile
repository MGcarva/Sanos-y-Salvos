FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml ./pom.xml
COPY ms-coincidencias/pom.xml ./ms-coincidencias/pom.xml
COPY ms-coincidencias/src ./ms-coincidencias/src
RUN apk add --no-cache maven && mvn -f ms-coincidencias/pom.xml clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Lambda Web Adapter: permite ejecutar Spring Boot en AWS Lambda sin cambios de código
COPY --from=public.ecr.aws/awsguru/aws-lambda-adapter:0.8.4 /lambda-adapter /opt/extensions/lambda-adapter
ENV PORT=8084
COPY --from=builder /app/ms-coincidencias/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
