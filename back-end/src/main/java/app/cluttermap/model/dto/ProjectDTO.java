package app.cluttermap.model.dto;

import java.util.List;
import java.util.stream.Collectors;

import app.cluttermap.model.Item;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;

public class ProjectDTO {
    /* ------------- Fields ------------- */
    private Long id;
    private String name;
    private List<Long> roomIds;
    private List<Long> orgUnitIds;
    private List<Long> itemIds;

    /* ------------- Constructors ------------- */
    // NOTE: Constructor parameters should follow the same order as the fields.
    public ProjectDTO(Project project) {
        this.id = project.getId();
        this.name = project.getName();
        this.roomIds = project.getRooms().stream()
                .map(Room::getId)
                .collect(Collectors.toList());
        this.orgUnitIds = project.getOrgUnits().stream()
                .map(OrgUnit::getId)
                .collect(Collectors.toList());
        this.itemIds = project.getItems().stream()
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

    public List<Long> getRoomIds() {
        return roomIds;
    }

    public List<Long> getOrgUnitIds() {
        return orgUnitIds;
    }

    public List<Long> getItemIds() {
        return itemIds;
    }
}
