FROM maven:3.9-eclipse-temurin-24 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q compile dependency:copy-dependencies -DoutputDirectory=target/dependency

FROM eclipse-temurin:24-jre
WORKDIR /app
COPY --from=build /app/target/classes ./classes
COPY --from=build /app/target/dependency ./lib
ENV PORT=8080
EXPOSE 8080
CMD ["java", "-cp", "classes:lib/*", "com.wattwise.BrowserServer"]
