# --- Giai đoạn 1: BUILD (Sử dụng Maven để đóng gói source code) ---
# Dùng image Maven có sẵn JDK 17 (khớp với dự án của bạn)
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Tạo thư mục làm việc trong container
WORKDIR /app

# Copy file pom.xml vào trước để tải thư viện (Tận dụng Docker Cache)
COPY pom.xml .

# Lệnh này để tải các thư viện về (Dependency) mà chưa cần build code
# Giúp lần build sau nhanh hơn nếu bạn không sửa file pom.xml
RUN mvn dependency:go-offline

# Bây giờ mới copy toàn bộ code nguồn vào
COPY src ./src

# Chạy lệnh build (tạo file .jar), bỏ qua test để tiết kiệm thời gian
RUN mvn clean package -DskipTests

# --- Giai đoạn 2: RUN (Chạy ứng dụng) ---
# Dùng image Java siêu nhẹ (Slim) chỉ để chạy app (không cần Maven nữa)
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy file .jar đã tạo ra ở Giai đoạn 1 (build) sang Giai đoạn 2
# 1. (Optional) Tạo user non-root để bảo mật
RUN groupadd -r spring && useradd -r -g spring spring
# Lưu ý: docker sẽ tự tìm file .jar trong thư mục target
COPY --from=build /app/target/*.jar app.jar

RUN chown spring:spring app.jar

# 4. Chuyển sang user thường
USER spring

HEALTHCHECK --interval=30s --timeout=5s \
  CMD java -version || exit 1

# Mở cổng 8080 (Mặc định của Spring Boot)
EXPOSE 8080

# Lệnh chạy ứng dụng khi container khởi động
ENTRYPOINT ["java", "-jar", "app.jar"]