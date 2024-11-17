package app.cluttermap.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class NewOrgUnitDTO {
    /* ------------- Fields ------------- */
    @NotBlank(message = "Organization unit name must not be blank.")
    private String name;

    private String description;

    @Pattern(regexp = "\\d+", message = "Room ID must be a valid number.")
    private String roomId;

    @Pattern(regexp = "\\d+", message = "Project ID must be a valid number.")
    private String projectId;

    /* ------------- Validation Methods ------------- */
    // Custom validation method to ensure that either RoomId or ProjectId is
    // provided.
    @AssertTrue(message = "Either RoomId or ProjectId must be provided.")
    public boolean isRoomOrProjectValid() {
        return roomId != null || projectId != null;
    }

    /* ------------- Constructors ------------- */
    // NOTE: Constructor parameters should follow the same order as the fields.
    public NewOrgUnitDTO(String name, String description, String roomId, String projectId) {
        this.name = name;
        this.description = description;
        this.roomId = roomId;
        this.projectId = projectId;
    }

    /* ------------- Getters ------------- */
    // NOTE: Getters should follow the same order as the fields and constructor for
    // consistency.
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getProjectId() {
        return projectId;
    }

    /* ------------- JsonIgnore Getters ------------- */
    // NOTE: These getters are used internally for processing and are excluded from
    // JSON serialization.

    @JsonIgnore
    public Long getRoomIdAsLong() {
        return Long.parseLong(roomId);
    }

    @JsonIgnore
    public Long getProjectIdAsLong() {
        return Long.parseLong(projectId);
    }
}
