# Service Directory

This directory contains the service layer classes. The service layer is responsible for implementing business logic and interacting with the repository layer.

## Structure

- Each resource (e.g., `Item`) has its own service.
- Services are annotated with `@Service` and are used by controllers to perform operations.

## Example

- `ItemService.java`: Implements the logic for managing `Item` entities, such as creating, updating, and deleting items.
