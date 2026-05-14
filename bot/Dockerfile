FROM maven:3-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
COPY dto/pom.xml dto/
COPY server/pom.xml server/
COPY bot/pom.xml bot/
RUN mvn dependency:go-offline -pl dto,bot -B -q
COPY dto/ dto/
COPY bot/ bot/
RUN mvn install -pl dto,bot -DskipTests -B -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/bot/target/castlekeeper-bot-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
