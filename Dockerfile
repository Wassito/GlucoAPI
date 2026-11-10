FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src/java src/java
COPY web web
RUN mvn -B -DskipTests package

FROM tomcat:11.0-jre21-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/api-healthblood-1.0.0.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 10000
CMD ["sh", "-c", "sed -i \"s/port=\\\"8080\\\"/port=\\\"${PORT:-10000}\\\"/\" /usr/local/tomcat/conf/server.xml && exec catalina.sh run"]
