FROM openjdk:24-jdk

# Set environment
VOLUME /tmp
ARG JAR_FILE=backend/target/*.jar

# Copy built JAR file
COPY ${JAR_FILE} app.jar

# Run the JAR
ENTRYPOINT ["java","-jar","/app.jar"]