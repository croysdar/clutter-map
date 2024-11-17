package app.cluttermap.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class NewRoomDTO {
    /* ------------- Fields ------------- */
    @NotBlank(message = "Room name must not be blank.")
    private String name;

    private String description;

    @NotNull(message = "Project ID must not be blank.")
    @Pattern(regexp = "\\d+", message = "Project ID must be a valid number.")
    private String projectId;

    /* ------------- Constructors ------------- */
    // NOTE: Constructor parameters should follow the same order as the fields.
    public NewRoomDTO(String name, String description, String projectId) {
        this.name = name;
        this.description = description;
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

    public String getProjectId() {
        return projectId;
    }

    /* ------------- JsonIgnore Getters ------------- */
    // NOTE: These getters are used internally for processing and are excluded from
    // JSON serialization.

    @JsonIgnore
    public Long getProjectIdAsLong() {
        return Long.parseLong(projectId);
    }
}
