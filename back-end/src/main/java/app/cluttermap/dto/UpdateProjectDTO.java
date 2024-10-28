package app.cluttermap.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

public class UpdateProjectDTO {
    @NotBlank(message = "Project name must not be blank.")
    private String name;

    @JsonCreator
    public UpdateProjectDTO(@JsonProperty("name") String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
