package app.cluttermap.model.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class NewItemDTO {
    @NotBlank(message = "Item name must not be blank.")
    private String name;

    private String description;
    private List<String> tags;

    @Pattern(regexp = "\\d+", message = "OrgUnit ID must be a valid number.")
    private String orgUnitId;

    @Pattern(regexp = "\\d+", message = "Project ID must be a valid number.")
    private String projectId;

    @AssertTrue(message = "Either OrgUnitId or ProjectId must be provided.")
    public boolean isOrgUnitOrProjectValid() {
        return orgUnitId != null || projectId != null;
    }

    public NewItemDTO(String name, String description, List<String> tags, String orgUnitId, String projectId) {
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.orgUnitId = orgUnitId;
        this.projectId = projectId;
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

    public String getOrgUnitId() {
        return orgUnitId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
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

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @JsonIgnore
    public Long getOrgUnitIdAsLong() {
        return Long.parseLong(orgUnitId);
    }

    @JsonIgnore
    public Long getProjectIdAsLong() {
        return Long.parseLong(projectId);
    }
}
