FROM eclipse-temurin:17-jdk
RUN apt-get update && apt-get install -y maven && apt-get clean
WORKDIR /app
COPY converter-core /app/converter-core
RUN mvn -f /app/converter-core clean install
COPY converter-csv /app/converter-csv
WORKDIR /app/converter-csv
EXPOSE 8083
CMD ["mvn", "spring-boot:run"]