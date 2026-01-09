FROM amazoncorretto:25-alpine-jdk
LABEL version="1.0"
RUN apk add --no-cache tzdata fontconfig freetype font-noto ttf-liberation busybox-extras curl
ENV TZ=Europe/Vienna
WORKDIR /app
COPY build/libs/backend.jar /jar/backend.jar
EXPOSE 8080
ENTRYPOINT ["java","-Xms512M","-Xmx1024M","-jar","/jar/backend.jar"]
