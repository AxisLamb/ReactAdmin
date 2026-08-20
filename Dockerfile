# ========== 构建阶段 ==========
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

# 使用阿里云镜像加速依赖下载
COPY docker/maven/settings.xml /usr/share/maven/conf/settings.xml

# 先拷贝 pom.xml，利用 Docker 层缓存复用依赖下载
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

# 拷贝源码并打包（跳过测试）
COPY src ./src
RUN mvn -B -DskipTests clean package

# ========== 运行阶段 ==========
FROM eclipse-temurin:21-jre
LABEL maintainer="lain"

ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-Xms256m -Xmx512m -Djava.security.egd=file:/dev/./urandom -Duser.timezone=Asia/Shanghai" \
    SPRING_PROFILES_ACTIVE=prod

WORKDIR /app

# 创建非 root 运行用户
RUN groupadd -r app && useradd -r -g app -d /app app

# 拷贝构建产物
COPY --from=builder --chown=app:app /build/target/*.jar /app/app.jar

# 本地文件存储与日志目录
RUN mkdir -p /app/uploads /app/logs && chown -R app:app /app

USER app

EXPOSE 8888

# 本地文件存储目录挂载点（使用 OSS/MinIO 时可去掉）
VOLUME ["/app/uploads", "/app/logs"]

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
