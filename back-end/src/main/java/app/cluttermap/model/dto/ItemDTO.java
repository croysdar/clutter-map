package app.cluttermap.model.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import app.cluttermap.model.Item;
import app.cluttermap.model.OrgUnit;

public class ItemDTO {
    /* ------------- Fields ------------- */
    private Long id;
    private String name;
    private String description;
    private List<String> tags;
    private Integer quantity;
    private Optional<Long> orgUnitId;
    private Optional<String> orgUnitName;
    private Long projectId;

    /* ------------- Constructors ------------- */
    // NOTE: Constructor parameters should follow the same order as the fields.
    public ItemDTO(Item item) {
        this.id = item.getId();
        this.name = item.getName();
        this.description = item.getDescription();
        this.tags = item.getTags() != null ? item.getTags() : new ArrayList<>();
        this.quantity = item.getQuantity();
        this.orgUnitId = Optional.ofNullable(item.getOrgUnit()).map(OrgUnit::getId);
        this.orgUnitName = Optional.ofNullable(item.getOrgUnit()).map(OrgUnit::getName);
        this.projectId = item.getProject().getId();
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

    public List<String> getTags() {
        return tags;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Optional<Long> getOrgUnitId() {
        return orgUnitId;
    }

    public Optional<String> getOrgUnitName() {
        return orgUnitName;
    }

    public Long getProjectId() {
        return projectId;
    }
}
