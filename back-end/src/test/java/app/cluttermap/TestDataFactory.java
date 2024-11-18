package app.cluttermap;

import java.util.List;

import app.cluttermap.model.dto.NewItemDTO;
import app.cluttermap.model.dto.UpdateItemDTO;

public class TestDataFactory {
    // Constants for default test values
    public static final String DEFAULT_ITEM_NAME = "Item Name";
    public static final String DEFAULT_ITEM_DESCRIPTION = "Item Description";
    public static final List<String> DEFAULT_ITEM_TAGS = List.of("Tag 1", "Tag 2");
    public static final Integer DEFAULT_ITEM_QUANTITY = 1;
    public static final String DEFAULT_ORG_UNIT_ID = "1";
    public static final String DEFAULT_PROJECT_ID = "1";

    public static class NewItemDTOBuilder {
        private String name = DEFAULT_ITEM_NAME;
        private String description = DEFAULT_ITEM_DESCRIPTION;
        private List<String> tags = DEFAULT_ITEM_TAGS;
        private Integer quantity = DEFAULT_ITEM_QUANTITY;
        private String orgUnitId = DEFAULT_ORG_UNIT_ID;
        private String projectId = DEFAULT_PROJECT_ID;

        public NewItemDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public NewItemDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public NewItemDTOBuilder tags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public NewItemDTOBuilder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public NewItemDTOBuilder orgUnitId(Object orgUnitId) {
            if (orgUnitId instanceof Long) {
                this.orgUnitId = String.valueOf(orgUnitId);
            } else if (orgUnitId instanceof String) {
                this.orgUnitId = (String) orgUnitId;
            } else {
                this.orgUnitId = null;
            }
            return this;
        }

        public NewItemDTOBuilder projectId(Object projectId) {
            if (projectId instanceof Long) {
                this.projectId = String.valueOf(projectId);
            } else if (projectId instanceof String) {
                this.projectId = (String) projectId;
            } else {
                this.projectId = null;
            }
            return this;
        }

        public NewItemDTO build() {
            return new NewItemDTO(name, description, tags, quantity, orgUnitId, projectId);
        }
    }

    // Factory method to initialize a builder with defaults
    public static NewItemDTOBuilder newItemDTOBuilder() {
        return new NewItemDTOBuilder();
    }

    public static class UpdateItemDTOBuilder {
        private String name = DEFAULT_ITEM_NAME;
        private String description = DEFAULT_ITEM_DESCRIPTION;
        private List<String> tags = DEFAULT_ITEM_TAGS;
        private Integer quantity = DEFAULT_ITEM_QUANTITY;

        public UpdateItemDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UpdateItemDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public UpdateItemDTOBuilder tags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public UpdateItemDTOBuilder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public UpdateItemDTO build() {
            return new UpdateItemDTO(name, description, tags, quantity);
        }
    }

    // Factory method to initialize a builder with defaults
    public static UpdateItemDTOBuilder updateItemDTOBuilder() {
        return new UpdateItemDTOBuilder();
    }

}
