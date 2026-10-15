# Simple ToDo

A private, single-user task management application with a React/Vite dashboard backed by a Spring Boot REST API and PostgreSQL.

---

## Architecture & Tech Stack

- **Frontend**: React 19, Vite 8, Vitest (`frontend/`)
- **Backend**: Spring Boot 4.1.1, Java 25, Spring Data JPA, Flyway, Gradle 9.8.1 wrapper (`backend/`)
- **Database**: PostgreSQL with Flyway automated migrations

---

## Prerequisites

Ensure the following tools are installed on your machine:
- **Java**: JDK 25
- **Node.js**: v20+ (with `npm`)
- **PostgreSQL**: A running local instance (default port `5432` or custom port like `5433`)

---

## Database Setup

1. Connect to your PostgreSQL instance (e.g., via `psql`, pgAdmin, or DBeaver).
2. Create the application database:
   ```sql
   CREATE DATABASE simple_todo;
   ```
> [!NOTE]
> You do **not** need to create tables manually. Flyway automatically runs migration scripts (such as `V1__create_tasks.sql`) when the backend service starts.

---

## Running the Application Locally

The project consists of two components running concurrently in separate terminals.

### Terminal 1: Backend Service (Spring Boot)

1. Open a terminal and navigate to the `backend/` directory:
   ```powershell
   cd backend
   ```

2. Configure environment variables for your database connection and port:
   
   **PowerShell (Windows):**
   ```powershell
   # Point to your local PostgreSQL port (e.g., 5433 or 5432) and database name:
   $env:SPRING_DATASOURCE_URL="jdbc:postgresql://127.0.0.1:5433/simple_todo"
   $env:SPRING_DATASOURCE_USERNAME="postgres"
   $env:SPRING_DATASOURCE_PASSWORD="your_postgres_password"  # Leave empty if no password

   # Optional: set custom server port (defaults to 8080 if unset):
   $env:SERVER_PORT="8081"
   ```

   **Bash / macOS / Linux:**
   ```bash
   export SPRING_DATASOURCE_URL="jdbc:postgresql://127.0.0.1:5433/simple_todo"
   export SPRING_DATASOURCE_USERNAME="postgres"
   export SPRING_DATASOURCE_PASSWORD="your_postgres_password"
   export SERVER_PORT="8081"
   ```

3. Launch the application using the Gradle wrapper:
   
   **Windows:**
   ```powershell
   .\gradlew.bat bootRun
   ```

   **macOS / Linux:**
   ```bash
   ./gradlew bootRun
   ```

4. Verify backend health by visiting:
   [http://127.0.0.1:8081/api/tasks/dashboard](http://127.0.0.1:8081/api/tasks/dashboard) (or port `8080` if default).

---

### Terminal 2: Frontend Client (React + Vite)

1. Open a second terminal and navigate to the `frontend/` directory:
   ```powershell
   cd frontend
   ```

2. Configure the API endpoint in a `.env` file:
   Create a file named `.env` in the `frontend/` directory with:
   ```env
   VITE_API_BASE_URL=http://127.0.0.1:8081
   ```
   *(Ensure the port matches the `SERVER_PORT` used in Terminal 1).*

3. Install frontend dependencies:
   ```powershell
   # Use npm ci for clean, reproducible installs from package-lock.json:
   npm ci
   ```

4. Start the Vite development server:
   ```powershell
   npm run dev
   ```

5. Open your browser and navigate to:
   👉 **[http://127.0.0.1:5173](http://127.0.0.1:5173)**

### Editing dashboard tasks

Use **Edit task** on a dashboard card to replace its title and optional notes, then choose **Save changes**. The title must not be blank. Use the card's **Change status** menu to move a task among TODO, In progress, and Completed; status changes save immediately. Successful changes refresh the dashboard from the API, including its seven-task recency limit. If a request fails, the task is not shown as saved, edit values remain available, and status changes can be retried.

---

## Verification & Testing Commands

You can run verification checks either from the project root or from the respective subdirectories:

### Frontend Verification (Vitest, Build, Oxlint)

**From project root:**
```powershell
# Run unit & component tests once (non-watch):
npm --prefix frontend test -- --run

# Run production build check:
npm --prefix frontend run build

# Run linter:
npm --prefix frontend run lint
```

**From `frontend/` directory:**
```powershell
cd frontend
npm test -- --run
npm run build
npm run lint
```

---

### Backend Verification (Unit & MockMvc Tests)

Standard unit and MockMvc contract/CORS tests (runs without requiring a live database):

**From `backend/` directory:**
```powershell
cd backend
.\gradlew.bat test
```

---

### Backend Live PostgreSQL Integration Tests

To run the integration suite tagged `@Tag("postgres-integration")` (which tests real PostgreSQL Flyway migrations, ordering, and 7-item cutoff against a live database):

**PowerShell (Windows):**
```powershell
cd backend
# Set TASKS_TEST_DATABASE_URL to enable the integration test suite:
$env:TASKS_TEST_DATABASE_URL="jdbc:postgresql://127.0.0.1:5433/simple_todo_test"
$env:SPRING_DATASOURCE_URL=$env:TASKS_TEST_DATABASE_URL
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD=""  # Set your password if applicable

.\gradlew.bat test
```

*(The test suite automatically excludes `postgres-integration` tests if `TASKS_TEST_DATABASE_URL` is unset).*
