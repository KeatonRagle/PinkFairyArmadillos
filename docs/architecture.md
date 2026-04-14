# 🐾 Project Architecture & Style Guide

## Architecture Overview
This project follows a **layered Spring Boot architecture**, separating responsibilities to improve maintainability, scalability, and clarity.

**High-Level Flow:**
Client → Controller → Service → Repository → Database
             ↓
            DTOs

---

## Package Structure

- `config` → Security, password encoding, application configs  
- `controllers` → REST API endpoints  
- `services` → Business logic  
- `repositories` → Database access (Spring Data JPA)  
- `models` → Database entities  
- `dto`
  - `request` → Incoming data  
  - `response` → Outgoing data  
- `security` → Authentication & authorization  
- `schedulers` → Background tasks  

---

## Application Layer

**Purpose:** Entry point of the backend application.

**Responsibilities:**
- Bootstraps Spring Boot application  
- Initializes all components  
- Enables scheduling and async processing  
- Starts embedded server (Tomcat)  

---

## Controllers (API Layer)

**Purpose:** Handle HTTP requests and responses.

**Responsibilities:**
- Define API endpoints  
- Accept request data  
- Call services  
- Return responses with status codes  

### Key Controllers

**AdminController**
- Retrieve all users  
- Promote users to ADMIN  

**PetController**
- Fetch pets  
- Retrieve pet by ID  
- Trigger web scraping  

**PingController**
- Health check endpoint (`/api/public/ping`)  

**UserController**
- Register users  
- Authenticate (login)  
- Retrieve user data  
- Resolve authenticated user via JWT  

---

## Services (Business Logic Layer)

**Purpose:** Core logic of the application.

### Key Services

**UserService**
- User authentication  
- JWT generation  
- Password hashing  
- Role management  

**PetService**
- Retrieve pet data  
- Convert entities → DTOs  
- Sync scraped data with database  
- Add / update / deactivate pets  

**WebScraperService**
- Scrapes adoption websites  
- Uses Selenium + Jsoup  
- Extracts and transforms pet data  

**JWTService**
- Generate tokens  
- Validate tokens  
- Extract user data  

**DatabaseBackupService**
- Creates database backups before major operations  

**CustomUserDetailsService**
- Bridges user data with Spring Security  

---

## Data Flow (Scraping)

Scheduler → WebScraperService → PetService.sync()
↓
Compare DB vs Scraped
→ Add
→ Update
→ Deactivate


---

## Repositories (Data Access Layer)

**Purpose:** Communicate with the database.

- Uses Spring Data JPA  
- Handles CRUD operations  
- No business logic  

### Key Repositories
- `UserRepository`  
- `PetRepository`  
- `AdoptionSiteRepository`  

---

## Models (Entities)

**Purpose:** Represent database tables.

### Key Entities
- `User` → authentication & roles  
- `Pet` → core pet data  
- `AdoptionSite` → adoption locations  
- `Reviews` → user feedback  
- `Comments` → user comments  
- `PetRating` → ratings system  
- `FeaturedPets` → highlighted pets  
- `Submissions` → user-submitted pets  

---

## Security

**Key Concepts:**
- JWT-based authentication  
- Stateless sessions (no server-side sessions)  
- Role-based access control (USER / ADMIN)  

### Components
- `JwtFilter` → validates tokens  
- `UserPrincipal` → authenticated user representation  
- Custom handlers for:
  - 401 Unauthorized  
  - 403 Forbidden  

---

## Authentication Flow

Login Request
↓
UserController
↓
UserService.verify()
↓
AuthenticationManager
↓
CustomUserDetailsService
↓
UserRepository
↓
JWTService.generateToken()
↓
Return JWT
---

## DTOs & Request Objects

**Purpose:** Control data flow between layers.

### Request Objects (Incoming)
- Represent client input  
- Include validation  

### DTOs (Outgoing)
- Safe API responses  
- Hide sensitive data (passwords)  

---

## Configuration

**Purpose:** Customize application behavior.

### Includes:
- Security rules  
- Password encryption  
- CORS configuration  

### Environments:
- `dev`  
- `test`  
- `prod`  

Each environment has its own configuration properties.

---

## Schedulers

**Purpose:** Run background tasks.

### Example:
- Web scraping jobs  
- Data updates  

Runs asynchronously using:
- `@Scheduled`  
- `@Async`  

---

## Testing

**Purpose:** Validate application behavior.

### Features:
- Uses Testcontainers (MySQL)  
- Isolated test environment  
- Integration testing  

### Test Coverage Includes:
- Security (JWT, authentication)  
- Services (User, Pet)  
- Web scraping logic  
- API endpoints  
---

## Resources

**Purpose:** Store configuration files.

### Includes:
- Environment configs  
- Flyway migrations  
- Database settings  

---

## Key Design Principles

- Separation of concerns (layered architecture)  
- DTO pattern for security  
- Stateless authentication (JWT)  
- Scalable scraping system  
- Environment-based configuration  
- Automated testing with isolated environments  

