# Controller Directory

This directory contains the REST controllers for the application. Each controller is responsible for handling incoming HTTP requests and mapping them to the appropriate service layer methods.

## Structure

- Each resource (e.g., `Item`) has its own controller.
- Controllers define endpoints using `@GetMapping`, `@PostMapping`, `@PutMapping`, and `@DeleteMapping` annotations.

## Example

- `ItemController.java`: Manages the API endpoints for CRUD operations on `Item` resources.
