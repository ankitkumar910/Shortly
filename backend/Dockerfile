# ---------- Build stage ----------
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -DskipTests

COPY src src

RUN ./mvnw clean package -DskipTests


# ---------- Runtime stage ----------
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/target/shortly.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]