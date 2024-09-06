# Model Directory

This directory contains the entity classes. Each entity represents a table in the database, and its fields correspond to the columns of that table.

## Structure

- Each resource (e.g., `Item`) has its own entity class.
- Entities are annotated with `@Entity` and often include fields like `@Id`, `@GeneratedValue`, and `@Column`.

## Example

- `Item.java`: Represents the `Item` entity, mapping to the `items` table in the database.
