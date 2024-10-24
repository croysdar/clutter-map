# DTO Directory

This directory contains the Data Transfer Objects (DTOs). DTOs are used to transfer data between different layers of the application, typically between the controller and service layers, or between the backend and frontend. They help to encapsulate data and are usually not directly mapped to database entities.

## Structure

- Each DTO corresponds to a particular resource (e.g., `NewRoomDTO`).
- DTOs typically contain fields relevant to the data being transferred, excluding unnecessary entity details.
- The fields in a DTO may correspond to a subset of the entity's fields or be custom fields required for API communication.

## Example

- `NewRoomDTO.java`: Represents the data that will be transferred when creating a new room. This DTO encapsulates the following fields:
  - `name`: The name of the room.
  - `description`: A brief description of the room.
  - `projectId`: The identifier of the project to which this room belongs.