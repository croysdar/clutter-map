package app.cluttermap.model.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class NewItemDTO {
    /* ------------- Fields ------------- */
    @NotBlank(message = "Item name must not be blank.")
    private String name;

    private String description;

    private List<String> tags;

    @Min(value = 1, message = "Quantity must be a least 1.")
    private Integer quantity;

    @Pattern(regexp = "\\d+", message = "OrgUnit ID must be a valid number.")
    private String orgUnitId;

    @Pattern(regexp = "\\d+", message = "Project ID must be a valid number.")
    private String projectId;

    /* ------------- Validation Methods ------------- */
    // Custom validation method to ensure that either OrgUnitId or ProjectId is
    // provided.
    @AssertTrue(message = "Either OrgUnitId or ProjectId must be provided.")
    public boolean isOrgUnitOrProjectValid() {
        return orgUnitId != null || projectId != null;
    }

    /* ------------- Constructors ------------- */
    // NOTE: Constructor parameters should follow the same order as the fields.
    public NewItemDTO(String name, String description, List<String> tags, Integer quantity, String orgUnitId,
            String projectId) {
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.quantity = (quantity != null) ? quantity : 1;
        this.orgUnitId = orgUnitId;
        this.projectId = projectId;
    }

    /* ------------- Getters ------------- */
    // NOTE: Getters should follow the same order as the fields and constructor for
    // consistency.
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

    public String getOrgUnitId() {
        return orgUnitId;
    }

    public String getProjectId() {
        return projectId;
    }

    /* ------------- JsonIgnore Getters ------------- */
    // NOTE: These getters are used internally for processing and are excluded from
    // JSON serialization.

    @JsonIgnore
    public Long getOrgUnitIdAsLong() {
        return Long.parseLong(orgUnitId);
    }

    @JsonIgnore
    public Long getProjectIdAsLong() {
        return Long.parseLong(projectId);
    }
}
