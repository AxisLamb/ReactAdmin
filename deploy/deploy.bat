@echo off

echo start build and deploy ...

:: 设置变量
set JAVA_HOME=C:\Program Files\Java\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%
set PROJECT_DIR=D:\project\ReactAdmin\backend
set SERVICE_NAME=lain-experiment

:: 构建项目
cd /d "%PROJECT_DIR%"
call mvn clean package -DskipTests -Pprod

:: 复制 JAR 文件
mkdir deploy\%SERVICE_NAME%
copy "%PROJECT_DIR%\target\%SERVICE_NAME%-1.0.0.jar" "deploy\%SERVICE_NAME%\%SERVICE_NAME%.jar"

:: 创建 Dockerfile
copy "Dockerfile" "deploy\%SERVICE_NAME%\Dockerfile"

:: 构建和推送镜像
cd deploy\%SERVICE_NAME%
docker build --build-arg JAR_FILE=%SERVICE_NAME%.jar -t %SERVICE_NAME%:1.0 .
docker login -u=username -p password xxx
docker tag %SERVICE_NAME%:1.0 registry.cn-shenzhen.aliyuncs.com/namespace/react-admin:1.0
docker tag %SERVICE_NAME%:1.0 registry.cn-shenzhen.aliyuncs.com/namespace/react-admin:latest
docker push registry.cn-shenzhen.aliyuncs.com/namespace/react-admin:1.0
docker push registry.cn-shenzhen.aliyuncs.com/namespace/react-admin:latest

echo deploy successfully！
pause
