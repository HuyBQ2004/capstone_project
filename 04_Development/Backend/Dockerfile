# Multi-stage — build bằng Maven (Java 25, khớp pom.xml maven.compiler.release), runtime chỉ
# cần JRE cho gọn image. Không cần mvnw (repo không có Maven Wrapper), dùng mvn có sẵn trong image build.
# 11/08/2026 — chạm file để kích hoạt lần build CI/CD đầu tiên thật sự (mọi lần trước pipeline
# fail sớm ở stage test, build/push image chưa từng chạy tới).
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# Cache layer dependency riêng — chỉ re-download khi pom.xml đổi, không phải mỗi lần sửa code.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# BR-160/161 — BackupServiceImpl gọi `pg_dump` (ProcessBuilder), không có trong image JRE trần.
RUN apk add --no-cache postgresql-client

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
