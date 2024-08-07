# Routes Directory

This directory contains the routing logic for the application. It defines the routes and their corresponding components.

## Structure

- **Routes.tsx**: Main routing file that sets up all the routes for the application.

## Guidelines

- Define routes using `react-router-dom`.
- Keep the routing logic clean and modular. Avoid including too much business logic in the route definitions.
- Use lazy loading for routes to improve performance by splitting the code and loading components only when needed.