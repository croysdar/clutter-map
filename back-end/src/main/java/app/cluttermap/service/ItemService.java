package app.cluttermap.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import app.cluttermap.exception.ResourceNotFoundException;
import app.cluttermap.model.Item;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Project;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.NewItemDTO;
import app.cluttermap.model.dto.UpdateItemDTO;
import app.cluttermap.repository.ItemRepository;
import app.cluttermap.repository.OrgUnitRepository;
import app.cluttermap.util.EventActionType;
import app.cluttermap.util.ResourceType;
import jakarta.transaction.Transactional;

@Service("itemService")
public class ItemService {
    /* ------------- Constants ------------- */
    public static final String PROJECT_MISMATCH_ERROR = "Cannot move item to a different project's organization unit.";
    public static final String ACCESS_DENIED_STRING = "You do not have permission to access item with ID: %d";

    /* ------------- Injected Dependencies ------------- */
    private final OrgUnitRepository orgUnitRepository;
    private final ItemRepository itemRepository;
    private final SecurityService securityService;
    private final ProjectService projectService;
    private final OrgUnitService orgUnitService;
    private final EventService eventService;
    private final ItemService self;

    /* ------------- Constructor ------------- */
    public ItemService(
            OrgUnitRepository orgUnitRepository,
            ItemRepository itemRepository,
            SecurityService securityService,
            ProjectService projectService,
            OrgUnitService orgUnitService,
            EventService eventService,
            @Lazy ItemService self) {
        this.orgUnitRepository = orgUnitRepository;
        this.itemRepository = itemRepository;
        this.securityService = securityService;
        this.projectService = projectService;
        this.orgUnitService = orgUnitService;
        this.eventService = eventService;
        this.self = self;
    }

    /* ------------- CRUD Operations ------------- */
    /* --- Read Operations (GET) --- */
    public Iterable<Item> getUserItems() {
        User user = securityService.getCurrentUser();

        return itemRepository.findByOwnerId(user.getId());
    }

    @PreAuthorize("@securityService.isResourceOwner(#id, 'ITEM')")
    public Item getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.ITEM, id));
    }

    @PreAuthorize("@securityService.isResourceOwner(#projectId, 'PROJECT')")
    public List<Item> getUnassignedItemsByProjectId(Long projectId) {
        return itemRepository.findUnassignedItemsByProjectId(projectId);
    }

    /* --- Create Operation (POST) --- */
    @Transactional
    public Item createItem(NewItemDTO itemDTO) {
        Item item;
        if (itemDTO.getOrgUnitId() == null) {
            if (itemDTO.getProjectId() == null) {
                throw new IllegalArgumentException("Either OrgUnitId or ProjectId must be provided.");
            }
            item = self.createUnassignedItem(itemDTO, itemDTO.getProjectIdAsLong());
        } else {
            item = self.createItemInOrgUnit(itemDTO, itemDTO.getOrgUnitIdAsLong());
        }

        eventService.logEvent(
                ResourceType.ITEM, item.getId(),
                EventActionType.CREATE, buildCreatePayload(item));
        return item;
    }

    @PreAuthorize("@securityService.isResourceOwner(#orgUnitId, 'ORGANIZATIONAL_UNIT')")
    public Item createItemInOrgUnit(NewItemDTO itemDTO, Long orgUnitId) {
        OrgUnit orgUnit = orgUnitService.getOrgUnitById(orgUnitId);

        Item newItem = new Item(
                itemDTO.getName(),
                itemDTO.getDescription(),
                itemDTO.getTags(),
                itemDTO.getQuantity(),
                orgUnit);
        return itemRepository.save(newItem);
    }

    @PreAuthorize("@securityService.isResourceOwner(#projectId, 'PROJECT')")
    public Item createUnassignedItem(NewItemDTO itemDTO, Long projectId) {
        Project project = projectService.getProjectById(projectId);

        Item newItem = new Item(
                itemDTO.getName(),
                itemDTO.getDescription(),
                itemDTO.getTags(),
                itemDTO.getQuantity(),
                project);
        return itemRepository.save(newItem);
    }

    /* --- Update Operation (PUT) --- */
    @Transactional
    public Item updateItem(Long id, UpdateItemDTO itemDTO) {
        Item _item = self.getItemById(id);
        Item oldItem = _item.copy();

        _item.setName(itemDTO.getName());
        if (itemDTO.getDescription() != null) {
            _item.setDescription(itemDTO.getDescription());
        }
        if (itemDTO.getQuantity() != null && itemDTO.getQuantity() >= 1) {
            _item.setQuantity(itemDTO.getQuantity());
        }
        if (itemDTO.getTags() != null) {
            _item.setTags(itemDTO.getTags());
        }

        Item updatedItem = itemRepository.save(_item);

        eventService.logEvent(
                ResourceType.ITEM, id, EventActionType.UPDATE,
                buildChangePayload(oldItem, updatedItem));

        return updatedItem;
    }

    /* --- Delete Operation (DELETE) --- */
    @Transactional
    public void deleteItemById(Long id) {
        // Make sure item exists first
        self.getItemById(id);
        itemRepository.deleteById(id);

        eventService.logEvent(
                ResourceType.ITEM, id,
                EventActionType.DELETE, null);
    }

    /* ------------- Complex Operations ------------- */
    @Transactional
    public Iterable<Item> assignItemsToOrgUnit(List<Long> itemIds, Long targetOrgUnitId) {
        OrgUnit targetOrgUnit = orgUnitService.getOrgUnitById(targetOrgUnitId);

        List<Item> updatedItems = new ArrayList<>();

        for (Long itemId : itemIds) {
            Item item = self.getItemById(itemId);
            Long previousOrgUnitId = item.getOrgUnitId();

            validateSameProject(item, targetOrgUnit);

            // Check if item is already assigned to an org unit
            if (item.getOrgUnit() != null) {
                unassignItemFromOrgUnit(item, item.getOrgUnit());
            }
            assignItemToOrgUnit(item, targetOrgUnit);
            eventService.logMoveEvent(
                    ResourceType.ITEM, item.getId(),
                    ResourceType.ORGANIZATIONAL_UNIT, previousOrgUnitId, targetOrgUnitId);
            updatedItems.add(item);
        }
        return updatedItems;
    }

    @Transactional
    public Iterable<Item> unassignItems(List<Long> itemIds) {
        List<Item> updatedItems = new ArrayList<>();
        for (Long itemId : itemIds) {
            Item item = self.getItemById(itemId);
            Long previousOrgUnitId = item.getOrgUnitId();
            if (previousOrgUnitId != null) {
                unassignItemFromOrgUnit(item, item.getOrgUnit());
                eventService.logMoveEvent(
                        ResourceType.ITEM, item.getId(),
                        ResourceType.ORGANIZATIONAL_UNIT, previousOrgUnitId, null);
            }
            updatedItems.add(item);
        }
        return updatedItems;
    }

    /* ------------- Ownership and Security Checks ------------- */
    public void checkOwnershipForItems(List<Long> itemIds) {
        for (Long id : itemIds) {
            if (!securityService.isResourceOwner(id, ResourceType.ITEM)) {
                throw new AccessDeniedException(String.format(ACCESS_DENIED_STRING, id));
            }
        }
    }

    /* ------------- Private Helper Methods ------------- */
    private void validateSameProject(Item item, OrgUnit targetOrgUnit) {
        if (targetOrgUnit == null || targetOrgUnit.getProject() == null) {
            throw new IllegalArgumentException("Target OrgUnit or its Project is null");
        }
        if (item == null || item.getProject() == null) {
            throw new IllegalArgumentException("Item or its Project is null");
        }
        if (!item.getProject().equals(targetOrgUnit.getProject())) {
            throw new IllegalArgumentException(PROJECT_MISMATCH_ERROR);
        }
    }

    private OrgUnit assignItemToOrgUnit(Item item, OrgUnit orgUnit) {
        orgUnit.addItem(item); // Manages both sides of the relationship
        return orgUnitRepository.save(orgUnit);
    }

    private OrgUnit unassignItemFromOrgUnit(Item item, OrgUnit orgUnit) {
        orgUnit.removeItem(item); // Manages both sides of the relationship
        return orgUnitRepository.save(orgUnit);
    }

    private Map<String, Object> buildCreatePayload(Item item) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", item.getName());
        payload.put("description", item.getDescription());
        payload.put("quantity", item.getQuantity());
        payload.put("tags", item.getTags());
        if (item.getOrgUnit() != null) {
            payload.put("orgUnitId", item.getOrgUnit().getId());
        }
        return payload;
    }

    private Map<String, Object> buildChangePayload(Item oldItem, Item newItem) {
        Map<String, Object> changes = new HashMap<>();

        if (!Objects.equals(oldItem.getName(), newItem.getName())) {
            changes.put("name", newItem.getName());
        }
        if (!Objects.equals(oldItem.getDescription(), newItem.getDescription())) {
            changes.put("description", newItem.getDescription());
        }
        if (!Objects.equals(oldItem.getQuantity(), newItem.getQuantity())) {
            changes.put("quantity", newItem.getQuantity());
        }
        if (!Objects.equals(oldItem.getTags(), newItem.getTags())) {
            changes.put("tags", newItem.getTags());
        }

        return changes;
    }
}
