FROM eclipse-temurin:17-jre-focal
ENV TZ Asia/Seoul
WORKDIR /app
COPY app.jar /app/
EXPOSE 8080
ENTRYPOINT ["java", "-Duser.timezone=${TZ}", "-jar", "/app/app.jar"]