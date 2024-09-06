# Clutter Map

Clutter Map is a Spring Boot and React application designed to help users organize and track their household items. The backend is built using Java Spring Boot, with PostgreSQL for data persistence, and the frontend is built with React and TypeScript.

## Project Structure

The project is organized into two main directories: `back-end` for the Spring Boot API and `react-front-end` for the React TypeScript frontend.

---

### `back-end/` (Spring Boot)

- **`gradle/`**: Contains Gradle-specific files that handle dependency management and the build process for the backend.
- **`src/`**: Contains the main Java source code and resources for the Spring Boot backend.
  - **`main/java/app/cluttermap/`**: The root package for the backend's Java code.
    - **`controller/`**: Contains REST controllers, which handle incoming HTTP requests and map them to service calls.
    - **`model/`**: Contains Java entity classes that define the data models and how they map to database tables.
    - **`repository/`**: Contains interfaces extending `JpaRepository` for interacting with the database.
    - **`service/`**: Contains service classes that encapsulate the business logic and interact with repositories.
    - **`ClutterMapApplication.java`**: The entry point for the Spring Boot application.
  - **`main/resources/`**: Contains resource files such as `application.properties`, where configuration settings for the backend (like database connection details) are specified.
  - **`test/java/app/cluttermap/`**: Contains unit and integration tests for the backend.

---

### `react-front-end/` (React with TypeScript)

- **`node_modules/`**: Contains all the Node.js packages and dependencies for the frontend.
- **`public/`**: Contains static files such as the `index.html` file, which is the entry point for the React application.
- **`src/`**: Contains the main source code for the React frontend.
  - **`assets/`**: Contains images, fonts, and other static assets used in the application.
  - **`components/`**: Contains reusable UI components used across the app, such as buttons, forms, etc.
  - **`contexts/`**: Contains React context files that provide global state management across components.
  - **`hooks/`**: Custom React hooks to encapsulate reusable logic for the application.
  - **`pages/`**: Contains components that represent full pages in the application (e.g., Home, Dashboard).
  - **`routes/`**: Contains routing-related logic, defining which components to render for each URL.
  - **`services/`**: Contains code for making API requests to the backend.
  - **`store/`**: Contains state management logic (e.g., Redux).
  - **`tests/`**: Contains unit and integration tests for the React frontend.
  - **`types/`**: Contains TypeScript type definitions for stronger typing throughout the application.
  - **`utils/`**: Contains utility functions that are used throughout the frontend.
  - **`App.tsx`**: The main React component that defines the app structure.
  - **`index.tsx`**: The entry point for the React app, responsible for rendering the app to the DOM.
  - **`react-app-env.d.ts`**: Auto-generated TypeScript declaration file for React.
  - **`tsconfig.json`**: TypeScript configuration file defining the compilation options for the TypeScript codebase.

---

## How to Run the Project

1. **Backend (Spring Boot)**:
   - Navigate to the `back-end/` directory.
   - Run the following command to start the Spring Boot server:
     ```
     ./gradlew bootRun
     ```

2. **Frontend (React)**:
   - Navigate to the `react-front-end/` directory.
   - Run the following commands to install dependencies and start the React app:
     ```
     npm install
     npm start
     ```

## API Endpoints

The following REST endpoints are available:

- `GET /api/items`: Retrieve a list of all items.
- `POST /api/items`: Create a new item.
- `PUT /api/items/{id}`: Update an existing item.
- `DELETE /api/items/{id}`: Delete an item.

## Database Configuration

The application uses PostgreSQL as its database. Configuration is stored in `src/main/resources/application.properties`.