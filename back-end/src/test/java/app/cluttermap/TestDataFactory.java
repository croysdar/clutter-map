package app.cluttermap;

import static app.cluttermap.TestDataConstants.*;

import java.util.List;

import app.cluttermap.model.Item;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Project;
import app.cluttermap.model.dto.NewItemDTO;
import app.cluttermap.model.dto.NewOrgUnitDTO;
import app.cluttermap.model.dto.NewProjectDTO;
import app.cluttermap.model.dto.NewRoomDTO;
import app.cluttermap.model.dto.UpdateItemDTO;
import app.cluttermap.model.dto.UpdateOrgUnitDTO;
import app.cluttermap.model.dto.UpdateProjectDTO;
import app.cluttermap.model.dto.UpdateRoomDTO;

public class TestDataFactory {

    public static class ItemBuilder {
        private String name = DEFAULT_ITEM_NAME;
        private String description = DEFAULT_ITEM_DESCRIPTION;
        private List<String> tags = DEFAULT_ITEM_TAGS;
        private Integer quantity = DEFAULT_ITEM_QUANTITY;
        private OrgUnit orgUnit;
        private Project project;

        public ItemBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ItemBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ItemBuilder tags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public ItemBuilder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public ItemBuilder orgUnit(OrgUnit orgUnit) {
            this.orgUnit = orgUnit;
            this.project = null;
            return this;
        }

        public ItemBuilder project(Project project) {
            this.project = project;
            this.orgUnit = null;
            return this;
        }

        public ItemBuilder fromDTO(NewItemDTO itemDTO) {
            this.name = itemDTO.getName();
            this.description = itemDTO.getDescription();
            this.tags = itemDTO.getTags();
            this.quantity = itemDTO.getQuantity();
            return this;
        }

        public ItemBuilder fromDTO(UpdateItemDTO itemDTO) {
            this.name = itemDTO.getName();
            this.description = itemDTO.getDescription();
            this.tags = itemDTO.getTags();
            this.quantity = itemDTO.getQuantity();
            return this;
        }

        public Item build() {
            if (orgUnit != null) {
                return new Item(name, description, tags, quantity, orgUnit);
            } else if (project != null) {
                return new Item(name, description, tags, quantity, project);
            } else {
                throw new IllegalStateException("Either OrgUnit or Project must be set to build an Item.");
            }
        }
    }

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

    public static class NewOrgUnitDTOBuilder {
        private String name = DEFAULT_ORG_UNIT_NAME;
        private String description = DEFAULT_ORG_UNIT_DESCRIPTION;
        private String roomId = DEFAULT_ROOM_ID;
        private String projectId = DEFAULT_PROJECT_ID;

        public NewOrgUnitDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public NewOrgUnitDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public NewOrgUnitDTOBuilder roomId(Object roomId) {
            if (roomId instanceof Long) {
                this.roomId = String.valueOf(roomId);
            } else if (roomId instanceof String) {
                this.roomId = (String) roomId;
            } else {
                this.roomId = null;
            }
            return this;
        }

        public NewOrgUnitDTOBuilder projectId(Object projectId) {
            if (projectId instanceof Long) {
                this.projectId = String.valueOf(projectId);
            } else if (projectId instanceof String) {
                this.projectId = (String) projectId;
            } else {
                this.projectId = null;
            }
            return this;
        }

        public NewOrgUnitDTO build() {
            return new NewOrgUnitDTO(name, description, roomId, projectId);
        }
    }

    public static class UpdateOrgUnitDTOBuilder {
        private String name = DEFAULT_ORG_UNIT_NAME;
        private String description = DEFAULT_ORG_UNIT_DESCRIPTION;

        public UpdateOrgUnitDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UpdateOrgUnitDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public UpdateOrgUnitDTO build() {
            return new UpdateOrgUnitDTO(name, description);
        }
    }

    public static class NewRoomDTOBuilder {
        private String name = DEFAULT_ROOM_NAME;
        private String description = DEFAULT_ROOM_DESCRIPTION;
        private String projectId = DEFAULT_PROJECT_ID;

        public NewRoomDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public NewRoomDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public NewRoomDTOBuilder projectId(Object projectId) {
            if (projectId instanceof Long) {
                this.projectId = String.valueOf(projectId);
            } else if (projectId instanceof String) {
                this.projectId = (String) projectId;
            } else {
                this.projectId = null;
            }
            return this;
        }

        public NewRoomDTO build() {
            return new NewRoomDTO(name, description, projectId);
        }
    }

    public static class UpdateRoomDTOBuilder {
        private String name = DEFAULT_ROOM_NAME;
        private String description = DEFAULT_ROOM_DESCRIPTION;

        public UpdateRoomDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UpdateRoomDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public UpdateRoomDTO build() {
            return new UpdateRoomDTO(name, description);
        }
    }

    public static class NewProjectDTOBuilder {
        private String name = DEFAULT_PROJECT_NAME;

        public NewProjectDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public NewProjectDTO build() {
            return new NewProjectDTO(name);
        }
    }

    public static class UpdateProjectDTOBuilder {
        private String name = DEFAULT_PROJECT_NAME;

        public UpdateProjectDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UpdateProjectDTO build() {
            return new UpdateProjectDTO(name);
        }
    }
}
