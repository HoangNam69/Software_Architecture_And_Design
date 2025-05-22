FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /workspace/app

# Sao chép file Maven và cài đặt dependencies
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Đảm bảo script mvnw có quyền thực thi
RUN chmod +x ./mvnw
RUN ./mvnw install -DskipTests

# Giai đoạn tiếp theo, tạo ảnh container cuối cùng
FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp

# Tạo thư mục health-check
RUN mkdir -p /health-check
COPY --from=build /workspace/app/target/*.jar app.jar

# Tạo file script để kiểm tra health
RUN echo '#!/bin/sh\n\
curl -f http://localhost:8080/actuator/health || exit 1' > /health-check/health_check.sh

# Cấp quyền thực thi cho script health check
RUN chmod +x /health-check/health_check.sh

# Thiết lập HEALTHCHECK để Docker theo dõi trạng thái của container
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 CMD /health-check/health_check.sh

ENTRYPOINT ["java","-jar","/app.jar"]
