# BlueprintsManager

This is the SpringNews application. This application developed for education purposes. This app allows you to upload and
view articles.

## Prerequisites

Developed with Java 17 and Spring Boot. Database migrations implemented by Liquibase.
Check DB connection URL in Spring Boot property file.

## Built With

Maven

SpringBoot

Thymeleaf

JDBC

Liquibase

Lombok

Logback

iText7 for pdf generation

## Getting Started

1. Build jar file by: mvn clean install command
2. Run jar file by: java -jar blueprintsManager-0.0.1-SNAPSHOT.jar.

## Using the App

1. Open your browser and put application URL (http://server_ip:8085/) into address line to access the application.
2. The main page contains the 5 newest projects sorted by date.
3. Click on COMMENTS button of the project to open the list of project comments.
4. Click on VIEW button of the project to open the fields of project and list of project documents.
5. Click on REASSEMBLE button of the project to reassemble project in PDF. Button disabled when not needed.
6. Click on DOWNLOAD button of the project to download project in PDF.
7. Click on DELETE button of the project to mark project as deleted. Deleted projects not showing up on start page.
8. Click on CREATE NEW PROJECT button to create new project.
   - fill the fields and press SAVE button. (Validation will warn you if something goes wrong.)
9. Automatically generated documents when creating a project:
   - Cover page;
   - Title page;
   - Table of contents;
10. Click on ADD DOCUMENT button in Project form to add new document.
11. Document types supported for adding to a project:
    - General Information document;
    - Drawing document;
12. Provide a text file to add General Information document. If no text document is provided, an empty document will be created.
13. Provide a Drawing in PDF to add General Information document. The Provided PDF document main block must comply with GOST R 21.101–2020. If no PDF document is provided, an empty document will be created.
