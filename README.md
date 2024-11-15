# Clutter Map

Clutter Map is a Spring Boot and React application designed to help users organize and track their household items. The backend is built using Java Spring Boot, with PostgreSQL for data persistence, and the frontend is built with React and TypeScript.

## Project Structure

The project is organized into two main directories: `back-end` for the Spring Boot API and `react-front-end` for the React TypeScript frontend.

---

### `back-end/` (Spring Boot)

- **`gradle/`**: Contains Gradle-specific files that handle dependency management and the build process for the backend.
- **`src/`**: Contains the main Java source code and resources for the Spring Boot backend.
  - **`main/java/app/cluttermap/`**: The root package for the backend's Java code.
    - **`config/`**: Contains configuration files, including security and JWT configurations for authentication and authorization.
    - **`controller/`**: Contains REST controllers, which handle incoming HTTP requests and map them to service calls.
    - **`exception/`**: Contains custom exception classes to handle different error scenarios.
    - **`model/`**: Contains Java entity classes that define the data models and how they map to database tables.
      - **`dto/`**: Data Transfer Objects for models to simplify data handling between layers.
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
  - **`api/`**: Contains client.js, a custom fetch wrapper.
  - **`app/`**: Contains the main application logic for the frontend.
  - **`assets/`**: Contains images, fonts, and other static assets used in the application.
  - **`components/`**: Contains reusable UI components used across the app, such as buttons, forms, etc.
  - **`contexts/`**: Contains React context files that provide global state management across components.
  - **`hooks/`**: Custom React hooks to encapsulate reusable logic for the application.
  - **`pages/`**: Contains components that represent full pages in the application (e.g., Home, Dashboard).
  - **`routes/`**: Contains routing-related logic, defining which components to render for each URL.
  - **`tests/`**: Contains unit and integration tests for the React frontend.
  - **`types/`**: Contains TypeScript type definitions for stronger typing throughout the application.
  - **`utils/`**: Contains utility functions that are used throughout the frontend.
  - **`index.tsx`**: The entry point for the React app, responsible for rendering the app to the DOM.
  - **`react-app-env.d.ts`**: Auto-generated TypeScript declaration file for React.
  - **`tsconfig.json`**: TypeScript configuration file defining the compilation options for the TypeScript codebase.

---

## How to Run the Project

1. **Backend (Spring Boot)**:

   - Navigate to the `back-end/` directory.
   - Create a `.env` file with the following environment variables. Make sure to replace `{DATABASE_NAME}` and other placeholders with your own values:

     ```plaintext
     JWT_SECRET={YOUR_JWT_SECRET}

     GOOGLE_OAUTH_CLIENT_ID={YOUR_GOOGLE_OAUTH_CLIENT_ID}
     GOOGLE_OAUTH_CLIENT_SECRET={YOUR_GOOGLE_OAUTH_CLIENT_SECRET}

     DB_SOURCE_URL=jdbc:postgresql://localhost:5432/{YOUR_DATABASE_NAME}

     DB_USERNAME={YOUR_DB_USERNAME}
     DB_PASSWORD={YOUR_DB_PASSWORD}
     ```

   - Run the following command to start the Spring Boot server:
     ```bash
     ./gradlew bootRun
     ```
   - Note: The `.env` file is stored locally and is not included in the repository for security reasons. You must create it yourself with your own values.

1. **Frontend (React)**:
   - Navigate to the `react-front-end/` directory.
   - Run the following commands to install dependencies and start the React app:
     ```
     npm install
     npm start
     ```

## API Endpoints

### `/auth`

- **POST `/verify-token/google`**  
  Verifies a Google ID token and returns a JWT token and user information.  
  **Request Body**: `{ "idTokenString": "string" }`  
  **Response**: `{ "token": "JWT token" }`

- **GET `/user-info`**  
  Retrieves the current user's email and username.  
  **Response**: `{ "userEmail": "string", "userName": "string", "userFirstName": "string", "userLastName": "string }`

---

### `/org-units`

- **GET `/`**  
  Retrieves a list of all organization units for the current user.  
  **Response**: `Iterable<OrgUnit>`

- **POST `/`**  
  Adds a new organization unit.  
  **Request Body**: `NewOrgUnitDTO`  
  **Response**: `OrgUnit`

- **GET `/{id}`**  
  Retrieves a specific organization unit by ID.  
  **Path Variable**: `id` (Long)  
  **Response**: `OrgUnit`

- **GET `/{id}/items`**  
  Retrieves all items within a specific organization unit by organization unit ID.  
  **Path Variable**: `id` (Long)  
  **Response**: `Iterable<Item>`

- **PUT `/{id}`**  
  Updates an existing organization unit.  
  **Path Variable**: `id` (Long)  
  **Request Body**: `UpdateOrgUnitDTO`  
  **Response**: `OrgUnit`

- **PUT `/{id}/items`**  
  Updates the items assigned to an existing organization unit.  
  Items must exist
  **Path Variable**: `id` (Long)  
  **Request Body**: `List<Long> itemIds`  
  **Response**: `Iterable<Item>`

- **PUT `/unassign`**  
  Unassigns the specified org units from their rooms.
  Org Units must exist
  **Path Variable**: `id` (Long)  
  **Request Body**: `List<Long> orgUnitIds`  
  **Response**: `Iterable<OrgUnit>`

- **DELETE `/{id}`**  
  Deletes an organization unit by ID.  
  **Path Variable**: `id` (Long)  
  **Response**: `Void`

---

### `/rooms`

- **GET `/`**  
  Retrieves a list of all rooms for the current user.  
  **Response**: `Iterable<Room>`

- **POST `/`**  
  Adds a new room.  
  **Request Body**: `NewRoomDTO`  
  **Response**: `Room`

- **GET `/{id}`**  
  Retrieves a specific room by ID.  
  **Path Variable**: `id` (Long)  
  **Response**: `Room`

- **GET `/{id}/org-units`**  
  Retrieves all organization units within a specific room by room ID.  
  **Path Variable**: `id` (Long)  
  **Response**: `Iterable<OrgUnit>`

- **PUT `/{id}`**  
  Updates an existing room.  
  **Path Variable**: `id` (Long)  
  **Request Body**: `UpdateRoomDTO`  
  **Response**: `Room`

- **PUT `/{roomId}/org-units`**  
  Assigns organization units to a specific room.  
  **Path Variable**: `roomId` (Long): ID of the room.
  **Request Body**: `List<Long> orgUnitIds`  
  **Response**: `Room`

- **DELETE `/{id}`**  
  Deletes a room by ID.  
  **Path Variable**: `id` (Long)  
  **Response**: `Void`

---

### `/projects`

- **GET `/`**  
  Retrieves a list of all projects for the current user.  
  **Response**: `Iterable<Project>`

- **POST `/`**  
  Adds a new project.  
  **Request Body**: `NewProjectDTO`  
  **Response**: `Project`

- **GET `/{id}`**  
  Retrieves a specific project by ID.  
  **Path Variable**: `id` (Long)  
  **Response**: `Project`

- **GET `/{id}/rooms`**  
  Retrieves all rooms within a specific project by project ID.  
  **Path Variable**: `id` (Long)  
  **Response**: `Iterable<Room>`

- **GET `/{projectId}/org-units/unassigned`**  
  Retrieves a list of unassigned organization units within a project (OrgUnits not linked to any room).  
  **Path Variable**:

  - `projectId` (Long): ID of the project.  
    **Response**: `List<OrgUnit>`

- **GET `/{projectId}/items/unassigned`**  
  Retrieves a list of unassigned items within a project (items not linked to any organization unit).  
  **Path Variable**:

  - `projectId` (Long): ID of the project.  
    **Response**: `List<Item>`

- **PUT `/{id}`**  
  Updates an existing project.  
  **Path Variable**: `id` (Long)  
  **Request Body**: `UpdateProjectDTO`  
  **Response**: `Project`

- **DELETE `/{id}`**  
  Deletes a project by ID.  
  **Path Variable**: `id` (Long)  
  **Response**: `Void`

---

### `/items`

- **GET `/`**  
  Retrieves a list of all items.  
  **Response**: `Iterable<Item>`

- **POST `/`**  
  Adds a new item to an organization unit.  
  **Request Body**: `NewItemDTO`  
  **Response**: `Item`

- **GET `/{id}`**  
  Retrieves a specific item by ID.  
  **Path Variable**: `id` (Long): ID of the item to retrieve.  
   **Response**: `Item`

- **PUT `/{id}`**  
  Updates an existing item.  
  **Path Variable**: `id` (Long): ID of the item to update.  
  **Request Body**: `UpdateItemDTO`  
  **Response**: `Item`

- **PUT `/unassign`**  
  Unassigns the specified items from their org units.
  Items must exist
  **Path Variable**: `id` (Long)  
  **Request Body**: `List<Long> itemIds`  
  **Response**: `Iterable<Item>`

- **DELETE `/{id}`**  
  Deletes an item by ID.  
  **Path Variable**:
  - `id` (Long): ID of the item to delete.  
    **Response**: `Void`

---

Each endpoint is protected by security constraints, where applicable, to ensure that only authorized users can perform certain operations.

## Database Configuration

The application uses PostgreSQL as its database. Configuration is stored in
`back-end/src/main/resources/application.properties`.
