# JAR, Jenkins pipeline'inda Stage 2'de host'ta (gradlew) uretilir.
# Stage 3'te bu Dockerfile sadece hazir jar'i paketler (hizli, ag bagimsiz).
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
