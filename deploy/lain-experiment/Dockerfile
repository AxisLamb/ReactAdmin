FROM registry.cn-shenzhen.aliyuncs.com/lain_wms/mini-java:21
MAINTAINER LAIN

RUN cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

ARG JAR_FILE
COPY ${JAR_FILE} /app.jar

ENTRYPOINT ["java", "-Xmx512m", "-Djava.security.egd=file:/dev/./urandom","-jar", "/app.jar"]
CMD ["--spring.profiles.active=prod"]
