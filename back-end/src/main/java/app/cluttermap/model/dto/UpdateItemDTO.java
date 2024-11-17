package app.cluttermap.model.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class UpdateItemDTO {
    /* ------------- Fields ------------- */
    @NotBlank(message = "Item name must not be blank.")
    private String name;

    private String description;

    private List<String> tags;

    @Min(value = 1, message = "Quantity must be a least 1.")
    private Integer quantity = 1;

    /* ------------- Constructors ------------- */
    // NOTE: Constructor parameters should follow the same order as the fields.
    public UpdateItemDTO(String name, String description, List<String> tags, Integer quantity) {
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.quantity = (quantity != null) ? quantity : 1;
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
}
