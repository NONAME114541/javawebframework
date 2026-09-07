# ===== 第19课：多阶段构建（南坡小炒镜像） =====
# 阶段一：构建——用带 Maven 的 JDK 镜像编译打包
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
# 先只拷 pom 下载依赖：这一层只要 pom 不变就命中缓存，改代码重建时快得多
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package -DskipTests

# 阶段二：运行——只要 JRE，镜像更小、攻击面更小
FROM eclipse-temurin:17-jre
# curl 供 docker-compose 的 healthcheck 探活 /actuator/health 用
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
# 日志/LocalDateTime 时区更友好
ENV TZ=Asia/Shanghai
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
