# BlueprintsManager

This is the BlueprintsManager application. This application developed for education purposes. This software designed for automatic preparation design documents according to GOST R 21.101–2020  and download in multipage PDF document.

## Prerequisites

Developed with Java 17 and Spring Boot. Database migrations implemented by Liquibase.
Install docker for deploy.

## Built With

Maven

SpringBoot

Thymeleaf for Email Templates

Spring Data Jpa

Spring Security - Authentication by JWT

Liquibase

Lombok

Logback

iText7 for pdf generation

## Getting Started

1. Check variables in docker-compose.yml and bpm-credentials.env
2. Build jar file and deploy it by: "mvn deploy" command
3. Application will be deployed to your local docker.

## Using the App

1. Open your browser and put application Swagger URL (http://localhost:8090/swagger-ui/) into address line to see application REST API.
2. Send Http requests via any tool.
3. Send POST with credentials to /api/user/auth - for authentication. You'll get set of JWT for requests authentication.
4. Use JWT for every request.
5. POST to /api/user/logout - for logout.
6. POST with REFRESH Token to /api/user/refresh_token_auth - for token refresh.
7. POST with user data to /api/user - for new user registration. Use with admin role only.
8. Default admin user: login - "admin", password - "admin"
9. There is 3 type of roles: ADMIN, EDITOR, VIEWER.
10. Any GET request available with any role.
11. POST and PUT requests to /api/project/* available with EDITOR role only.
12. POST and PUT requests to /api/company/* and /api/user/* available with ADMIN role only.
13. Automatically generated documents when creating a project:
    - Cover page;
    - Title page;
    - Table of contents;
14. Document types supported for manual adding to a project:
    - General Information document;
    - Drawing document;
23. Provide a text file to add General Information document. If no text document is provided, an empty document will be created.
24. Provide a Drawing in PDF to add General Information document. The Provided PDF document main block must comply with GOST R 21.101–2020. If no PDF document is provided, an empty document will be created.
