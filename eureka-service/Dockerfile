# Используем официальный образ Gradle в качестве базового
FROM openjdk:17-jdk-alpine

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем собранный JAR-файл из образа сборки
COPY build/libs/eureka-service-0.0.1-SNAPSHOT.jar ./eureka-service.jar

# Указываем команду для запуска приложения
ENTRYPOINT ["java", "-jar", "eureka-service.jar"]
