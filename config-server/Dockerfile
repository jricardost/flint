FROM openjdk:17-jdk-slim
RUN apt-get update && apt-get install -y maven
WORKDIR /app
COPY config-server .
COPY config-files /app/config-files
EXPOSE 8888
CMD ["mvn", "-f", "pom.xml", "spring-boot:run", "-q"]
