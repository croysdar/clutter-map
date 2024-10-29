package app.cluttermap.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

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

    @NotBlank(message = "Room ID must not be blank.")
    @Pattern(regexp = "\\d+", message = "Room ID must be a valid number.")
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

    public Long getRoomIdAsLong() {
        return Long.parseLong(roomId);
    }
}
