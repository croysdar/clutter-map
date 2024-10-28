package app.cluttermap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class NewRoomDTO {
    private String name;
    private String description;
    private String projectId;

    public NewRoomDTO(String name, String description, String projectId) {
        this.name = name;
        this.description = description;
        this.projectId = projectId;
    }

    @NotBlank(message = "Room name must not be blank.")
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @NotBlank(message = "Project ID must not be blank.")
    @Pattern(regexp = "\\d+", message = "Project ID must be a valid number.")
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

    public Long getProjectIdAsLong() {
        return Long.parseLong(projectId);
    }
}
