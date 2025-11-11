# Bajaj Finserv Health — JAVA Qualifier Autostart Solution

This Spring Boot app fulfills the assessment requirements:

- On startup, it sends a POST request to **/hiring/generateWebhook/JAVA** with your name, regNo, and email.
- It determines which SQL question applies based on the **last two digits** of your `regNo` (odd → Question 1, even → Question 2).
- It computes the corresponding **final SQL query** string.
- It **submits** the final query to the **returned `webhook` URL** using the JWT in the `Authorization` header (falls back to the documented test endpoint if webhook is missing).

No controllers are exposed — the whole flow runs automatically at startup.

## Configure your details

Edit `src/main/resources/application.properties`:
```
app.participant.name=Your Name
app.participant.regno=REG12347
app.participant.email=your@email.com
```

Run with:
```
./mvnw spring-boot:run
```
or build a jar:
```
./mvnw -q -DskipTests package
java -jar target/bfh-java-webhook-solution-0.0.1-SNAPSHOT.jar
```

## Notes

- Uses **Spring WebFlux WebClient**.
- Implements startup logic via **ApplicationRunner**.
- Includes robust logging and error handling.
- SQL answers are implemented inside `SqlSolver.java` with references to the problem statements.
