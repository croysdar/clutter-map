package app.cluttermap.model.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public class UpdateItemDTO {
    private String name;
    private String description;
    private List<String> tags;

    public UpdateItemDTO(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @NotBlank(message = "Item name must not be blank.")
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
}