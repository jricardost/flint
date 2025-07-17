FROM eclipse-temurin:17-jdk
RUN apt-get update && apt-get install -y maven && apt-get clean
WORKDIR /app
COPY . .
EXPOSE 8761
CMD ["mvn", "spring-boot:run"]
