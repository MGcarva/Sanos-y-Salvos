FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml ./pom.xml
COPY ms-geolocalizacion/pom.xml ./ms-geolocalizacion/pom.xml
COPY ms-geolocalizacion/src ./ms-geolocalizacion/src
RUN apk add --no-cache maven && mvn -f ms-geolocalizacion/pom.xml clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Lambda Web Adapter: permite ejecutar Spring Boot en AWS Lambda sin cambios de código
COPY --from=public.ecr.aws/awsguru/aws-lambda-adapter:0.8.4 /lambda-adapter /opt/extensions/lambda-adapter
ENV PORT=8083
COPY --from=builder /app/ms-geolocalizacion/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
