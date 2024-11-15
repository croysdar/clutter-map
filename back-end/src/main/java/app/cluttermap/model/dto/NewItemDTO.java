package app.cluttermap.model.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class NewItemDTO {
    @NotBlank(message = "Item name must not be blank.")
    private String name;

    private String description;
    private List<String> tags;

    @NotNull(message = "OrgUnit ID must not be blank.")
    @Pattern(regexp = "\\d+", message = "OrgUnit ID must be a valid number.")
    private String orgUnitId;

    // TODO see if we can allow *either* org unit or project id as parent

    public NewItemDTO(String name, String description, List<String> tags, String orgUnitId) {
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.orgUnitId = orgUnitId;
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

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

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

    @JsonIgnore
    public Long getOrgUnitIdAsLong() {
        return Long.parseLong(orgUnitId);
    }

}
