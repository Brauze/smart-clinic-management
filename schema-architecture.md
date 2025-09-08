# Smart Clinic Management System - Architecture Design

## Section 1: Architecture Summary

The Smart Clinic Management System employs a three-tier architecture pattern leveraging Spring Boot for the backend, with a clear separation between presentation (HTML/JavaScript frontend with Thymeleaf templates), application (Spring Boot controllers and services), and data layers (dual database approach using MySQL for relational data and MongoDB for document storage). The system implements both MVC controllers for server-side rendered views and RESTful APIs for client-side interactions, secured through JWT token-based authentication. This hybrid approach ensures scalability, maintainability, and flexibility in handling both structured clinical data and unstructured documents like prescriptions and logs.

## Section 2: Numbered Flow

1. **Client Request Initiation**: User interacts with the frontend (HTML/JavaScript) through browser, triggering either a page request or API call
2. **Request Routing**: Spring Boot's DispatcherServlet receives the HTTP request and routes it based on URL pattern
3. **Authentication Check**: JWT token validation occurs via TokenService for API endpoints or session check for MVC routes
4. **Controller Processing**: Request reaches either @RestController (for APIs) or @Controller (for MVC) based on endpoint type
5. **Service Layer Invocation**: Controller delegates business logic to appropriate Service class (DoctorService, PatientService, etc.)
6. **Repository Interaction**: Service layer calls Repository interfaces for database operations
7. **Database Query Execution**: Spring Data JPA executes queries on MySQL for entities, or MongoDB operations for documents
8. **Data Retrieval**: Database returns result set to Repository layer
9. **Entity/DTO Mapping**: Service layer converts entities to DTOs for data transfer
10. **Response Generation**: Controller creates ResponseEntity for REST APIs or returns Thymeleaf view name for MVC
11. **View Rendering**: For MVC, Thymeleaf engine processes template with model data; for REST, Jackson serializes response to JSON
12. **HTTP Response**: Final HTML page or JSON response sent back to client browser