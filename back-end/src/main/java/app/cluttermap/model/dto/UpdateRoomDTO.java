package app.cluttermap.model.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateRoomDTO {
    /* ------------- Fields ------------- */
    @NotBlank(message = "Room name must not be blank.")
    private String name;

    private String description;

    /* ------------- Constructors ------------- */
    // NOTE: Constructor parameters should follow the same order as the fields.
    public UpdateRoomDTO(String name, String description) {
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
