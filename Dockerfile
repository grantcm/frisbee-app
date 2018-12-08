FROM openjdk:8-alpine

COPY target/uberjar/frisbee-spa.jar /frisbee-spa/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/frisbee-spa/app.jar"]
