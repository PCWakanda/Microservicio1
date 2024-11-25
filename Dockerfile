# Usa una imagen base de Java 17
FROM openjdk:21-jdk-slim

# Establece el directorio de trabajo
WORKDIR /app

# Copia el archivo JAR al contenedor
COPY target/Microservicio1-0.0.1-SNAPSHOT.jar /app/microservicio1.jar

# Define el puerto en el que se ejecutará la aplicación
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "/app/microservicio1.jar"]