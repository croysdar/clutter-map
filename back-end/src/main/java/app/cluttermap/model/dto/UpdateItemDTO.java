package app.cluttermap.model.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class UpdateItemDTO {
    @NotBlank(message = "Item name must not be blank.")
    private String name;

    private String description;
    private List<String> tags;

    @Min(value = 1, message = "Quantity must be a least 1.")
    private Integer quantity = 1;

    public UpdateItemDTO(String name, String description, List<String> tags, Integer quantity) {
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.quantity = (quantity != null) ? quantity : 1;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
