package app.cluttermap.model.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateOrgUnitDTO {
    private String name;
    private String description;

    public UpdateOrgUnitDTO(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @NotBlank(message = "Organization unit name must not be blank.")
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
