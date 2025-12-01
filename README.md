# Webhook Workflow Application

A Spring Boot 3 application that automatically executes a webhook workflow on startup. This application demonstrates SOLID principles, proper layering, error handling, and retry mechanisms.

## Project Overview

This application performs the following workflow automatically on startup:

1. **Generate Webhook**: Sends user credentials to generate a webhook URL and access token
2. **Determine Question**: Based on the registration number (odd/even), selects the appropriate question URL
3. **Generate SQL Query**: Processes the question and generates a SQL query (currently mocked)
4. **Send Test Webhook**: Sends the generated SQL query to the test webhook endpoint

## Technologies Used

- **Spring Boot 3.2.0**
- **Java 17**
- **WebClient** (Reactive HTTP client)
- **Spring Retry** (For retry logic with exponential backoff)
- **Lombok** (For reducing boilerplate code)
- **JUnit 5 & Mockito** (For testing)
- **Maven** (Build tool)

## Project Structure

```
src/
├── main/
│   ├── java/com/bajaj/webhook/
│   │   ├── WebhookWorkflowApplication.java    # Main application entry point
│   │   ├── config/
│   │   │   ├── WebClientConfig.java           # WebClient configuration
│   │   │   └── RetryConfig.java               # Retry configuration
│   │   ├── dto/
│   │   │   ├── WebhookGenerationRequest.java  # Request DTO
│   │   │   ├── WebhookGenerationResponse.java # Response DTO
│   │   │   └── TestWebhookRequest.java        # Test webhook DTO
│   │   ├── service/
│   │   │   └── WebhookService.java            # Business logic service
│   │   └── runner/
│   │       └── WorkflowRunner.java            # ApplicationRunner implementation
│   └── resources/
│       └── application.properties             # Application configuration
└── test/
    └── java/com/bajaj/webhook/
        ├── service/
        │   └── WebhookServiceTest.java        # Service tests
        └── runner/
            └── WorkflowRunnerTest.java        # Runner tests
```

## Prerequisites

- **Java 17** or higher
- **Maven 3.6+**

## Building the Application

### 1. Clean and Build

```bash
mvn clean package
```

This will:
- Compile the source code
- Run all tests
- Generate a runnable JAR file in `target/webhook-workflow-1.0.0.jar`

### 2. Skip Tests (Optional)

If you want to build without running tests:

```bash
mvn clean package -DskipTests
```

## Running the Application

### Option 1: Run with Maven

```bash
mvn spring-boot:run
```

### Option 2: Run the JAR file

```bash
java -jar target/webhook-workflow-1.0.0.jar
```

## Configuration

All configuration is in `src/main/resources/application.properties`:

```properties
# User Information (Update these values)
user.name=Gopal Yadav
user.regNo=22BCT0094
user.email=gopalyadav6560@gmail.com

# Webhook URLs (These are predefined)
webhook.generate.url=https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA
webhook.test.url=https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA
webhook.question1.url=https://drive.google.com/file/d/1LAPx2to9zmN5DY0tkMrJRNvJrNVx1gnR/view
webhook.question2.url=https://drive.google.com/file/d/1b0p5C-6fUrUQglJVaWWAAB3P12lfoBCH/view
```

## Features

### 1. **Automatic Execution**
- Uses `ApplicationRunner` to execute workflow automatically on startup
- No manual REST controller triggers needed

### 2. **Retry Logic**
- Implements exponential backoff retry strategy
- Retries up to 3 times on failure
- 2-second initial delay, multiplied by 2 for each retry

### 3. **Error Handling**
- Comprehensive exception handling
- Detailed error logging with context
- Graceful failure recovery

### 4. **Security**
- Access tokens are masked in logs (shows only first and last 4 characters)
- Sensitive information protected

### 5. **Proper Logging**
- Structured logging with SLF4J
- Clear workflow progress indicators
- Request/response logging for debugging

## Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=WebhookServiceTest
```

### Test Coverage

The application includes comprehensive unit tests:
- `WebhookServiceTest`: Tests all service methods including success and failure scenarios
- `WorkflowRunnerTest`: Tests the complete workflow execution

## Expected Output

When the application runs successfully, you'll see output similar to:

```
========================================
Starting Webhook Workflow
========================================
Step 1: Generating webhook for user Gopal Yadav (22BCT0094)
Successfully generated webhook. Access Token: test****5678
Webhook URL: https://example.com/webhook
Step 2: Determining question URL based on registration number
Registration number ends with even digits (94). Using Question 2.
Step 3: Processing question and generating SQL query
Generated SQL query: SELECT * FROM users WHERE registration_number = '22BCT0094' AND status = 'active'
Step 4: Sending test webhook with generated query
Test webhook sent successfully.
========================================
Webhook Workflow Completed Successfully!
========================================
```

## API Endpoints Used

### 1. Generate Webhook
- **URL**: `https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA`
- **Method**: POST
- **Body**: `{ "name": "...", "regNo": "...", "email": "..." }`

### 2. Test Webhook
- **URL**: `https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA`
- **Method**: POST
- **Headers**: `Authorization: <accessToken>`
- **Body**: `{ "finalQuery": "..." }`

## Architecture Highlights

### SOLID Principles Applied

1. **Single Responsibility**: Each class has one clear purpose
   - `WebhookService`: Handles all webhook-related operations
   - `WorkflowRunner`: Orchestrates the workflow execution
   - DTOs: Pure data transfer objects

2. **Open/Closed**: Service is open for extension (can add new methods) but closed for modification

3. **Dependency Inversion**: Dependencies injected via constructor injection

### Layered Architecture

- **Presentation Layer**: `WorkflowRunner` (orchestration)
- **Service Layer**: `WebhookService` (business logic)
- **Data Layer**: DTOs for data transfer
- **Configuration Layer**: Separate config classes

## Troubleshooting

### Issue: Connection Timeout
- Check your internet connection
- Verify the API endpoints are accessible
- Check firewall settings

### Issue: Authentication Failed
- Verify user credentials in `application.properties`
- Check if the API is accepting requests

### Issue: Build Fails
- Ensure Java 17+ is installed: `java -version`
- Ensure Maven is installed: `mvn -version`
- Clear Maven cache: `mvn clean`

## Future Enhancements

1. **Actual Question Processing**: Replace mock SQL generation with real document parsing
2. **Database Integration**: Store workflow results in a database
3. **REST API**: Add optional REST endpoints for manual triggering
4. **Monitoring**: Add Spring Actuator for health checks and metrics
5. **Docker**: Containerize the application

## License

This project is created for educational and recruitment purposes.

## Author

Gopal Yadav (22BCT0094)
