# Services Directory

This directory contains service modules that handle API calls and other business logic related to external data.

## Structure

- **API services**: Functions that interact with backend APIs or external services.
- **Utility services**: Functions that perform common tasks and calculations.

## Guidelines

- Separate API calls into distinct service modules based on the feature or resource they interact with.
- Keep the service functions pure and free of side effects when possible.
- Handle errors gracefully within service functions and return meaningful error messages or status codes.
