package app.cluttermap.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class NewRoomDTO {
    @NotBlank(message = "Room name must not be blank.")
    private String name;

    private String description;

    @NotNull(message = "Project ID must not be blank.")
    @Pattern(regexp = "\\d+", message = "Project ID must be a valid number.")
    private String projectId;

    public NewRoomDTO(String name, String description, String projectId) {
        this.name = name;
        this.description = description;
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
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

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @JsonIgnore
    public Long getProjectIdAsLong() {
        return Long.parseLong(projectId);
    }
}
