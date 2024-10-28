package app.cluttermap.dto;

import jakarta.validation.constraints.NotBlank;

public class NewOrgUnitDTO {
    private String name;
    private String description;
    private String roomId;

    public NewOrgUnitDTO(String name, String description, String roomId) {
        this.name = name;
        this.description = description;
        this.roomId = roomId;
    }

    @NotBlank(message = "Organization unit name must not be blank.")
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getRoomId() {
        return roomId;
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
}
