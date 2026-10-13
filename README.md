# Notification Service

Servicio HTTP base construido con Java 25, Spring Boot y Maven.

## Ejecución local con Maven

Requiere JDK 25 configurado en `JAVA_HOME`.

```bash
./mvnw spring-boot:run
```

En Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

La comprobación de estado queda disponible en `http://localhost:8080/health`.

## Ejecución local con Docker CLI

```bash
docker build -t notification-service .
docker run --rm -p 8080:8080 notification-service
```

## Despliegue en Render

El archivo `render.yaml` configura un Web Service con runtime Docker y la ruta
de salud `/health`. Render asigna el puerto mediante la variable `PORT`, que la
aplicación consume automáticamente.

No se requiere Docker Compose dentro de la aplicación desplegada.
