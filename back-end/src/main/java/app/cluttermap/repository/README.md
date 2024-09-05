# Repository Directory

This directory contains the repository interfaces, which are responsible for interacting with the database. Each repository extends the `JpaRepository` interface provided by Spring Data JPA, giving access to CRUD operations.

## Structure

- Each resource (e.g., `Item`) has its own repository.
- Repositories provide the data access layer and are typically used by services to fetch or modify data.

## Example

- `ItemRepository.java`: The repository for accessing and manipulating `Item` entities in the database.
