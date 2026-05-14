FROM maven:3-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
COPY dto/pom.xml dto/
COPY server/pom.xml server/
COPY bot/pom.xml bot/
RUN mvn dependency:go-offline -pl dto,server -B -q
COPY dto/ dto/
COPY server/ server/
RUN mvn install -pl dto,server -DskipTests -B -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/server/target/castlekeeper-server-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
