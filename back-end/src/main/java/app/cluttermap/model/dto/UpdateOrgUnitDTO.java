package app.cluttermap.model.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateOrgUnitDTO {
    /* ------------- Fields ------------- */
    @NotBlank(message = "Organization unit name must not be blank.")

    private String name;

    private String description;

    /* ------------- Constructors ------------- */
    // NOTE: Constructor parameters should follow the same order as the fields.
    public UpdateOrgUnitDTO(String name, String description) {
        this.name = name;
        this.description = description;
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
}
