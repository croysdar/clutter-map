package app.cluttermap.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class NewOrgUnitDTO {
    @NotBlank(message = "Organization unit name must not be blank.")
    private String name;

    private String description;

    @Pattern(regexp = "\\d+", message = "Room ID must be a valid number.")
    private String roomId;

    @Pattern(regexp = "\\d+", message = "Project ID must be a valid number.")
    private String projectId;

    @AssertTrue(message = "Either RoomId or ProjectId must be provided.")
    public boolean isRoomOrProjectValid() {
        return roomId != null || projectId != null;
    }

    public NewOrgUnitDTO(String name, String description, String roomId, String projectId) {
        this.name = name;
        this.description = description;
        this.roomId = roomId;
        this.projectId = projectId;
    }

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

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    @JsonIgnore
    public Long getRoomIdAsLong() {
        return Long.parseLong(roomId);
    }

    @JsonIgnore
    public Long getProjectIdAsLong() {
        return Long.parseLong(projectId);
    }
}
