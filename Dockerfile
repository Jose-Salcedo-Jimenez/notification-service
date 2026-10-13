FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

COPY src src
RUN ./mvnw clean package

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=build --chown=spring:spring /workspace/target/*.jar app.jar

USER spring:spring
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
