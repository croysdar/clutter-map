package app.cluttermap.model.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.Min;
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

    @Min(value = 1, message = "Quantity must be a least 1.")
    private Integer quantity;

    public NewItemDTO(String name, String description, List<String> tags, Integer quantity, String orgUnitId) {
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.quantity = (quantity != null) ? quantity : 1;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

}
