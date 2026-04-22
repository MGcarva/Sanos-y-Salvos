FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml ./pom.xml
COPY bff-service/pom.xml ./bff-service/pom.xml
COPY bff-service/src ./bff-service/src
RUN apk add --no-cache maven && mvn -f bff-service/pom.xml clean package -Dmaven.test.skip=true \
    -Dmaven.wagon.http.retryHandler.count=5 \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 \
    -Dmaven.wagon.http.connectionTimeout=60000 \
    -Dmaven.wagon.http.readTimeout=120000

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/bff-service/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
