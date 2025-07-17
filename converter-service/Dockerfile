FROM eclipse-temurin:17-jdk
RUN apt-get update && apt-get install -y maven && apt-get clean
WORKDIR /app
COPY converter-core /app/converter-core
RUN mvn -f /app/converter-core clean install
COPY converter-json /app/converter-json
WORKDIR /app/converter-json
EXPOSE 8081
CMD ["mvn", "spring-boot:run"]