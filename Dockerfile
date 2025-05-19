
FROM maven:3.9.9-eclipse-temurin-21 as builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn package

FROM eclipse-temurin:21-jdk-ubi9-minimal

WORKDIR /app

COPY --from=builder /app/target/expense-tracker-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
