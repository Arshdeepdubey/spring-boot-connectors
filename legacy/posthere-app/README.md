# Posthere - Spring Boot Microservice

A Spring Boot microservice application demonstrating loose coupling, database seeding, and CI/CD pipelines.

## Features

- **Loose Coupling**: Implements constructor injection for Controller and Service layers, ensuring testability and modularity.
- **Database Seeding**: Automatically pre-loads sample data into an H2 in-memory database using a dedicated `LoadDatabase` configuration.
- **REST API**: Exposes endpoints to manage "Details" entities.
- **CI/CD**: Integrated GitHub Actions workflows for Continuous Integration and Continuous Deployment.

## Getting Started

### Prerequisites

- Java 17
- Maven

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/Arshdeepdubey/Posthere.git
   ```
2. Navigate to the project directory:
   ```bash
   cd Posthere
   ```

## Running the Application

To run the application locally:

```bash
mvn spring-boot:run
```

The application will start, and the H2 console (if enabled) and API endpoints will be accessible.

### API Endpoints

- `GET /api/details`: Retrieve all details.
- `GET /api/details/{id}`: Retrieve details by ID.
- `POST /api/details`: Create new details.

## Build and Test

To build the project and run tests:

```bash
mvn clean install
```

## CI/CD Pipelines

The project uses GitHub Actions for CI/CD:

- **CI (`ci.yml`)**: Triggered on Push and PR to `main`. Runs `mvn clean package` to compile code and run tests.
- **CD (`cd.yml`)**: Triggered on Push to `main`. Runs `mvn clean package -DskipTests` to build the deployable JAR artifact.

## Recent Updates

- **Build Fix**: Resolved `ApplicationContext` loading issues in tests by isolating `CommandLineRunner` into a separate configuration class.
- **Refactoring**: Converted field injection to constructor injection in `DetailsController` and `DetailsServiceImpl` to enforce loose coupling.
- **DevOps**: Added separate `ci.yml` and `cd.yml` workflows for streamlined development operations.
