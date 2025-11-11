# QueueCTL

A CLI-based background job queue system built using Spring Boot that manages job scheduling, retries with exponential backoff, and a Dead Letter Queue (DLQ).

## Features

- **Job Queue Management**: Submit, monitor, and manage background jobs
- **CLI Interface**: Interactive command-line interface using Spring Shell
- **Retry Mechanism**: Automatic job retries with exponential backoff
- **Dead Letter Queue (DLQ)**: Failed jobs are moved to DLQ for manual inspection
- **Database Persistence**: Job state and metadata stored in MySQL
- **Worker Management**: Control job processing workers
- **Queue Monitoring**: Real-time queue status and job statistics

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- MySQL 8.0+

## Getting Started

### 1. Database Setup

Create a MySQL database for the application:

```sql
CREATE DATABASE queuectl;
CREATE USER 'queuectl_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON queuectl.* TO 'queuectl_user'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Configuration

Create a `.env` file in the project root with your database configuration:

```env
DB_URL=jdbc:mysql://localhost:3306/queuectl
DB_USERNAME=queuectl_user
DB_PASSWORD=your_password
```

### 3. Build and Run

```bash
# Build the project
mvn clean compile

# Run the application
mvn spring-boot:run
```

## CLI Commands

Once the application is running, you can use the following commands in the interactive shell:

### Job Management

```bash
# Submit a new job
job submit --type "EMAIL_SEND" --payload "user@example.com"

# List all jobs
job list

# Get job details
job status --id <job-id>

# Cancel a job
job cancel --id <job-id>
```

### Worker Management

```bash
# Start workers
worker start --count 3

# Stop workers
worker stop

# Check worker status
worker status
```

### Dead Letter Queue

```bash
# List failed jobs in DLQ
dlq list

# Retry a job from DLQ
dlq retry --id <job-id>

# Clear DLQ
dlq clear
```

### Configuration

```bash
# View current configuration
config show

# Update configuration
config set --key "retry.max-attempts" --value "5"
```

## Architecture

The application follows a layered architecture:

- **CLI Layer**: Spring Shell commands for user interaction
- **Service Layer**: Business logic for job processing and queue management
- **Repository Layer**: Data access using Spring Data JPA
- **Entity Layer**: JPA entities representing jobs and queue state

### Key Components

- `JobService`: Core job management logic
- `JobWorker`: Background job processing
- `JobRepository`: Database operations
- `*ShellCommand`: CLI command handlers

## Job Lifecycle

1. **Submitted**: Job is created and queued
2. **Processing**: Worker picks up and executes the job
3. **Completed**: Job finished successfully
4. **Failed**: Job failed and moved to retry queue
5. **Dead**: Job exceeded retry limit and moved to DLQ

## Configuration

The application supports various configuration options:

- Retry attempts and backoff strategy
- Worker pool size
- Queue polling intervals
- Database connection settings

## Development

### Running Tests

```bash
mvn test
```

### Building JAR

```bash
mvn clean package
```

The executable JAR will be created in the `target/` directory.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.