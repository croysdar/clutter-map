package app.cluttermap.model.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

public class UpdateProjectDTO {
    /* ------------- Fields ------------- */
    @NotBlank(message = "Project name must not be blank.")
    private String name;

    /* ------------- Constructors ------------- */
    // NOTE: Constructor parameters should follow the same order as the fields.
    @JsonCreator
    public UpdateProjectDTO(@JsonProperty("name") String name) {
        this.name = name;
    }

    /* ------------- Getters ------------- */
    // NOTE: Getters should follow the same order as the fields and constructor for
    // consistency.
    public String getName() {
        return name;
    }
}
