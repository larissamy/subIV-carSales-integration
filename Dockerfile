FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -DskipTests clean package

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN mkdir -p /data && chown -R 1000:1000 /app /data

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081
ENV SERVER_PORT=8081
USER 1000

ENTRYPOINT ["java","-jar","/app/app.jar"]
