FROM openjdk:17-jdk-slim
RUN apt-get update && apt-get install -y maven
WORKDIR /app
COPY converter-service .
COPY converter-core/target/converter-core-1.0.0.jar /app/converter-core.jar
RUN mvn install:install-file -Dfile=/app/converter-core.jar -DgroupId=com.flint -DartifactId=converter-core -Dversion=1.0.0 -Dpackaging=jar
EXPOSE 8081
CMD ["mvn", "-f", "pom.xml", "spring-boot:run", "-q"]