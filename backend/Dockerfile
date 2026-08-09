//从官网引入包
FROM docker.m.daocloud.io/library/eclipse-temurin:21-jdk-alpine
//指定运行目录
WORKDIR /app
//把本地的build产物复制到容器当中
COPY target/*.jar app.jar
//声明暴露8080端口
EXPOSE 8080
//运行该程序
//entrypoint无法被外部的docker传入的指令覆盖，相反docker指令传入的参数会被他作为追加参数
ENTRYPOINT ["java","-jar","app.jar"]