FROM openjdk:17-jdk-slim
RUN apt-get update && apt-get install -y maven
COPY discovery-server .
EXPOSE 8761
CMD ["mvn", "-f", "pom.xml", "spring-boot:run", "-q"]
