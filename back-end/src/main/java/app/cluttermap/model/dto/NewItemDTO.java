package app.cluttermap.model.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class NewItemDTO {
    private String name;
    private String description;
    private String orgUnitId;
    private List<String> tags;

    public NewItemDTO(String name, String description, String orgUnitId, List<String> tags) {
        this.name = name;
        this.description = description;
        this.orgUnitId = orgUnitId;
        this.tags = tags;
    }

    @NotBlank(message = "Item name must not be blank.")
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @NotBlank(message = "OrgUnit ID must not be blank.")
    @Pattern(regexp = "\\d+", message = "OrgUnit ID must be a valid number.")
    public String getOrgUnitId() {
        return orgUnitId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOrgUnitId(String orgUnitId) {
        this.orgUnitId = orgUnitId;
    }

    public Long getOrgUnitIdAsLong() {
        return Long.parseLong(orgUnitId);
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

}
