FROM azul/zulu-openjdk:17
WORKDIR /app
ARG PORT
ENV PORT $PORT

EXPOSE ${PORT}

# Copy the jar to the production image from the builder stage.
COPY partition-core-plus/target/partition-core-plus-*-spring-boot.jar /partition-core-plus.jar

ENTRYPOINT ["java","-jar","/partition-core-plus.jar"]
