# Spring Bank which reacts to your personal finance needs.

This is a simple spring boot application demonstrating reactive programming with spring boot and r2dbc along with 
some of the common patterns used in building microservices.

## Prerequisites
- java 21
- maven 3.9+
- docker & docker-compose

## How to run
1. Compile the application
    ```bash
    mvn clean package -DskipTests
    ```
2. Start the application stack (using docker-compose)
    ```bash
    docker-compose up -d
    ```
3. Access the application at: `http://localhost:8080`
4. You can check the health of the application by accessing:
    ```bash
    curl http://localhost:8080/actuator/health
    ```
5. Pgadmin is available at: `http://localhost:5050` (user: admin@example.com / password: admin123)` to look at the database
6. Sample curls and bruno collection is available in the bruno folder to test the application endpoints as well as swgger ui in [Swagger - Ui](http://localhost:8081/)
7. If you want to old school way of testing you can use the below curl to create an account.

## Requests
### Create Account
```bash
curl --request POST \
  --url http://localhost:8080/accounts \
  --header 'content-type: application/json' \
  --data '{
  "document_number":"123456777"
}'
```
### Get Account
```bash
curl http://localhost:8080/accounts/1
```
### Create Transaction
```bash
curl --request POST \
  --url http://localhost:8080/transactions \
  --header 'content-type: application/json' \
  --data '{
  "account_id": 1,
  "operation_type_id": 1,
  "amount": 100.00
}'
```

## Ports Used
- Application: `8080`
- PgAdmin: `5050`
- PostgreSQL: `5432`
- Swagger UI: `8081`
 
- ## API Specification
Open API can be found at: [swagger-spec.yml](http://localhost:8081/swagger-spec.yml)
The same can be opened in swagger ui at: [Swagger - Ui](http://localhost:8081/)


## Running this with manual effort 
Open the application.yaml in the path `application/src/main/resources/application.yaml`
- Update the database connection details to point to your local database
- POSTGRESS_URL: <DBURL> r2dbc:postgresql://localhost:5432/appdb?timeZone=UTC
  POSTGRESS_USER: <USER>
  POSTGRESS_PASSWORD: <app123> 
- and save the file 
- Execute mvn clean install -DSkipTests
- build and push the docker image and run the app
The table scripts are in the path `persistence/src/main/resources/DDL/` you can run them manually to create the tables in your local db.



# TLDR Part

## Architectural Overview
1. Technologies used 
2. Application architecture and considerations
3. Design patterns implemented 
3. Database considerations 
4. Testing coverage in the application

### 1. Technologies used
- Java 21 as it is the latest LTS version
- Spring Boot 3.x with WebFlux for reactive programming model where applicatoin can handle more concurrent users with less resource utilization
- R2DBC for reactive database connectivity provides non blocking io with database 
- PostgreSQL as it is open source and has good r2dbc support
- Docker & Docker-compose for containerization and easy setup of the application stack
- Maven for build and dependency management
- Junit5 and Mockito for unit testing

### 2. Application architecture and considerations
- The application follows ports and adapters architecture (hexagonal architecture) to separate core business logic from external systems
- The business logic is in the Domain layer - this seperates the technology from the business making it easy to maintain and evolve , in there is a need arrises we can simply lift and shift this module to any other java framework 
- The API module will take care of all HTTP related concerns and will interact with the domain layer via services
- The persistence module will take care of all database related concerns and will interact with the domain layer

## Architecture Diagram

### Hexagonal Architecture (Ports and Adapters)
![Hexagonal Architecture](./Hexagonal_architecture.png)




### 3. Design patterns implemented
- **Repository Pattern**: Abstracts data access logic through R2DBC repositories, providing a clean separation between domain and data layers
- **DTO (Data Transfer Object) Pattern**: Separates internal domain models from external API representations for better encapsulation
- **Strategy Pattern**: Different operation types (purchase, payment, withdrawal) are handled through polymorphic behavior and new types can be added easily
- **Adapter Pattern**: Hexagonal architecture implements adapters to connect core domain logic with external systems (API, database)
- **Exception Handling Pattern**: Centralized error handling mechanism for consistent API responses

### 4. Database considerations
- PostgreSQL is used for its robustness and open-source nature






