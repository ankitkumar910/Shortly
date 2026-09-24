# Shortly

Shortly is a learning project for building a URL shortener from the ground up. It consists of a **Spring Boot backend** and a **React/Vite frontend**.

The application converts long HTTP(S) URLs into shorter links, supports optional custom short codes and expiration times, redirects visitors to the original URL, caches resolved URLs with Redis, and tracks click counts using Redis-backed counters.

The project is intentionally still in development. This README documents the functionality currently implemented rather than presenting Shortly as a finished production service.

## Current Features

* Create short URLs from `http://` and `https://` URLs.
* Generate short codes using **Base62 encoding** of the database record ID.
* Optionally specify a custom alphanumeric short code.
* Reject duplicate custom short codes.
* Set an optional expiration date and time.
* Redirect valid short URLs using an HTTP `302 Found` response.
* Cache resolved URLs in Redis.
* Preserve the remaining expiration time when caching expiring URLs in Redis.
* Increment click counts using Redis.
* Periodically synchronize Redis click counts with MySQL.
* Return useful error responses for invalid URLs, invalid expiration dates, expired links, missing links, and duplicate short codes.
* Serve a simple HTML 404 page for missing or expired short links.
* Provide a responsive React interface with:

  * Long URL input
  * Optional custom short-code input
  * Optional expiration presets
  * Generated-link result view
  * Copy-to-clipboard support
  * Responsive styling
  * Rotating background artwork

### Expiration Presets

The frontend currently provides:

* 5 seconds
* 1 minute
* 30 minutes
* 1 hour
* 1 day
* 1 week
* 1 month
* 1 year

## How It Works

### Creating a Short URL

1. The frontend sends the long URL and optional parameters to the backend.
2. The backend validates the URL.
3. If an expiration time is supplied, the backend verifies that it is in the future.
4. A URL record is created in the MySQL `urls` table.
5. If no custom code is supplied, a short code is generated from the database ID using Base62 encoding.
6. The backend returns the generated short URL.

### Resolving a Short URL

When a visitor opens a short URL:

```text
GET /{shortCode}
```

the backend:

1. Checks Redis for the short code.
2. If found, retrieves the cached destination URL.
3. If not found, loads the URL record from MySQL.
4. Checks whether the link has expired.
5. Caches the destination URL in Redis.
6. Increments the Redis click counter.
7. Returns an HTTP `302 Found` redirect.

The simplified flow is:

```text
              GET /{shortCode}
                     │
                     ▼
                  Redis
                ┌────┴────┐
              HIT         MISS
               │            │
               │            ▼
               │          MySQL
               │            │
               │            ▼
               │          Redis
               │            │
               └─────┬──────┘
                     ▼
              Increment Redis
                click count
                     │
                     ▼
                302 Redirect
```

## Click Count Synchronization

Click counts are maintained in Redis so that every redirect does not require a MySQL write.

For example:

```text
clicks:abc → 42
clicks:vv  → 17
```

Redis increments these counters whenever a short URL is successfully resolved.

A scheduled Spring task runs every **60 seconds** and synchronizes the accumulated Redis counts with the `clickCount` field in MySQL.

This gives Redis responsibility for **high-frequency counter updates**, while MySQL provides **persistent storage**.

The synchronization is intentionally simple because Shortly is a learning project rather than a distributed production system.

## Database Model

The main persisted fields are:

| Field        | Purpose                                                 |
| ------------ | ------------------------------------------------------- |
| `id`         | Numeric database ID used to generate Base62 short codes |
| `longUrl`    | Original destination URL                                |
| `shortUrl`   | Generated or custom short code                          |
| `createdAt`  | Record creation timestamp                               |
| `clickCount` | Persisted click count                                   |
| `expireAt`   | Optional expiration timestamp                           |

## API

### Create a Short URL

```http
POST http://localhost:8080/api/v1/shorten?u=https%3A%2F%2Fexample.com%2Fsome%2Flong%2Fpath
```

### Parameters

| Parameter   | Required | Description                                            |
| ----------- | -------- | ------------------------------------------------------ |
| `u`         | Yes      | Original URL. Must begin with `http://` or `https://`. |
| `shortCode` | No       | Custom alphanumeric short code.                        |
| `expire`    | No       | Future ISO-8601 expiration timestamp.                  |

Example:

```bash
curl -X POST "http://localhost:8080/api/v1/shorten?u=https%3A%2F%2Fexample.com%2Fdocs&shortCode=docs&expire=2026-10-01T12%3A00%3A00Z"
```

Successful response:

```json
{
  "shortUrl": "http://localhost:8080/1C",
  "longUrl": "https://example.com/docs"
}
```

### Resolve a Short URL

```http
GET http://localhost:8080/{shortCode}
```

For a valid short code, the backend responds with:

```http
302 Found
Location: https://example.com/...
```

Missing or expired links return a not-found response.

## Running Locally

### Prerequisites

* Java 25
* Maven or the included Maven Wrapper
* Node.js and npm
* MySQL
* Redis

### 1. Prepare MySQL

Create the database:

```sql
CREATE DATABASE shortlydb;
```

The backend expects MySQL to be available at:

```text
localhost:3306
```

Configure the database password in:

```text
backend/src/main/resources/application.properties
```

Hibernate currently uses:

```properties
spring.jpa.hibernate.ddl-auto=update
```

so the `urls` table is created or updated from the entity mapping.

### 2. Start Redis

Run Redis locally on its default port:

```text
6379
```

The backend is currently configured with:

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

### 3. Start the Backend

From the `backend` directory:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The API is available at:

```text
http://localhost:8080
```

### 4. Start the Frontend

From the `frontend` directory:

```bash
npm install
npm run dev
```

The Vite development server normally runs at:

```text
http://localhost:5173
```

The frontend currently communicates with:

```text
http://localhost:8080/api/v1/shorten
```

The backend allows cross-origin requests from the Vite development origin.

For a production-style frontend build:

```bash
npm run build
npm run preview
```

## Project Structure

```text
Shortly/
├── backend/
│   ├── src/main/java/dev/ankitkumar/shortly/
│   │   ├── controller/       HTTP endpoints and redirects
│   │   ├── dto/              API response DTOs
│   │   ├── entity/           JPA URL entity
│   │   ├── exception/        Custom exceptions and handlers
│   │   ├── repository/       Spring Data repository
│   │   ├── service/          URL and Redis business logic
│   │   └── utils/            Base62 short-code conversion
│   ├── src/main/resources/   Application configuration and 404 page
│   └── src/test/              Backend tests
│
└── frontend/
    ├── src/App.jsx           URL-shortening interface and API calls
    ├── src/App.css           Interface styling and responsive layout
    ├── src/backgrounds.js    Background image selection
    └── package.json           Vite scripts and React dependencies
```

## Tests and Useful Commands

### Backend Tests

```bash
cd backend
./mvnw test
```

Windows:

```powershell
cd backend
.\mvnw.cmd test
```

The current backend tests cover:

* Application context loading
* Base62 conversion behavior

### Frontend

Run linting:

```bash
cd frontend
npm run lint
```

Build the frontend:

```bash
npm run build
```

The frontend currently does not have a dedicated test suite.

<!-- ## Current Limitations

Shortly is still a learning project and currently has the following limitations:

* No user accounts, authentication, authorization, or per-user link management.
* No user dashboard or user profile.
* No admin dashboard yet.
* No interface for listing, editing, or deleting links.
* Click counts are buffered in Redis and synchronized with MySQL every 60 seconds rather than being persisted on every redirect.
* Click counts may be lost if Redis fails before the next synchronization.
* Configuration is currently oriented toward local development.
* Database connection settings, frontend API URL, and CORS configuration are currently stored in application configuration.
* Custom short codes currently accept only alphanumeric characters on the backend.
* The frontend currently allows hyphens and underscores in its input pattern, although the backend rejects them.
* No rate limiting or abuse prevention.
* No URL safety or malicious-URL screening.
* No production HTTPS configuration.
* No production secret-management strategy.
* URL creation currently uses query parameters rather than a JSON request body.
* The project has not yet been optimized or tested for high-scale traffic.

## Project Status

**Status: In Development**

The current focus of Shortly is learning and implementing:

* Spring Boot REST APIs
* MySQL persistence
* Redis caching
* Redis counters
* Cache expiration and TTL
* Periodic Redis-to-MySQL synchronization
* HTTP redirects
* Base62 encoding
* React/Vite frontend development

The project will continue to evolve as additional backend and system-design concepts are explored. -->
