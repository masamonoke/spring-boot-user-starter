# About
A Spring Boot starter with implemented user authorization, authentication and more. Spring Security with JWT, email confirmation.
You can use this project as a start point for your application.

# Build

```console
docker-compose up
mvn spring-boot:run
```

# API
You can check API on ```http://localhost:3003/swagger-ui/index.html``` when application is running.
# SSL
Optionally you can use already prepeared SSL configuration for secured connections.
To get it uncomment SSL section from ```application.properties.``` Then all your requests need to be ```https://``` prefixed and ```mail.confirm-link-pattern``` should also be switched to https.
