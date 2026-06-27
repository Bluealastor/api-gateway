# ── Stage 1: BUILD ────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn package -DskipTests -q

# ── Stage 2: RUNTIME ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# 8080 è l'unica porta esposta all'esterno — tutto il traffico passa da qui.
# Le porte interne (8081, 8082, 8761) rimangono nella rete Docker privata.
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
