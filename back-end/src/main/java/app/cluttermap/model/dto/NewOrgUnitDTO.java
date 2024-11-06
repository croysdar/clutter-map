package app.cluttermap.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class NewOrgUnitDTO {
    @NotBlank(message = "Organization unit name must not be blank.")
    private String name;

    private String description;

    @NotNull(message = "Room ID must not be blank.")
    @Pattern(regexp = "\\d+", message = "Room ID must be a valid number.")
    private String roomId;

    public NewOrgUnitDTO(String name, String description, String roomId) {
        this.name = name;
        this.description = description;
        this.roomId = roomId;
    }

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

    @JsonIgnore
    public Long getRoomIdAsLong() {
        return Long.parseLong(roomId);
    }
}
