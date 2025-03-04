package app.cluttermap.model.dto;

import java.util.List;
import java.util.stream.Collectors;

import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Room;

public class RoomDTO {
    /* ------------- Fields ------------- */
    private Long id;
    private String name;
    private String description;
    private Long roomId;
    private String roomName;
    private Long projectId;
    private List<Long> orgUnitIds;

    /* ------------- Constructors ------------- */
    // NOTE: Constructor parameters should follow the same order as the fields.
    public RoomDTO(Room room) {
        this.id = room.getId();
        this.name = room.getName();
        this.description = room.getDescription();
        this.projectId = room.getProject().getId();
        this.orgUnitIds = room.getOrgUnits().stream()
                .map(OrgUnit::getId)
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

    public Long getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public Long getProjectId() {
        return projectId;
    }

    public List<Long> getOrgUnitIds() {
        return orgUnitIds;
    }
}
