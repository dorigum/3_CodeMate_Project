FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache curl

WORKDIR /app

RUN addgroup -S codemate && adduser -S codemate -G codemate

COPY --from=builder /workspace/target/*.jar app.jar

USER codemate

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
