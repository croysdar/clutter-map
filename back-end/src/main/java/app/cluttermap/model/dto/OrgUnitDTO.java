package app.cluttermap.model.dto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import app.cluttermap.model.Item;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Room;

public class OrgUnitDTO {
    /* ------------- Fields ------------- */
    private Long id;
    private String name;
    private String description;
    private Optional<Long> roomId;
    private Optional<String> roomName;
    private Long projectId;
    private List<Long> itemIds;

    /* ------------- Constructors ------------- */
    // NOTE: Constructor parameters should follow the same order as the fields.
    public OrgUnitDTO(OrgUnit orgUnit) {
        this.id = orgUnit.getId();
        this.name = orgUnit.getName();
        this.description = orgUnit.getDescription();
        this.roomId = Optional.ofNullable(orgUnit.getRoom()).map(Room::getId);
        this.roomName = Optional.ofNullable(orgUnit.getRoom()).map(Room::getName);
        this.projectId = orgUnit.getProject().getId();
        this.itemIds = orgUnit.getItems().stream()
                .map(Item::getId)
                .collect(Collectors.toList());
    }

    /* ------------- Getters ------------- */
    // NOTE: Getters should follow the same order as the fields and constructor for
    // consistency.

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Optional<Long> getRoomId() {
        return roomId;
    }

    public Optional<String> getRoomName() {
        return roomName;
    }

    public Long getProjectId() {
        return projectId;
    }

    public List<Long> getItemIds() {
        return itemIds;
    }
}