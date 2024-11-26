package app.cluttermap.service;

import java.util.ArrayList;
import java.util.List;

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

    /* ------------- Constructor ------------- */
    public ItemService(
            OrgUnitRepository orgUnitRepository,
            ItemRepository itemRepository,
            SecurityService securityService,
            ProjectService projectService,
            OrgUnitService orgUnitService) {
        this.orgUnitRepository = orgUnitRepository;
        this.itemRepository = itemRepository;
        this.securityService = securityService;
        this.projectService = projectService;
        this.orgUnitService = orgUnitService;
    }

    /* ------------- CRUD Operations ------------- */
    /* --- Read Operations (GET) --- */
    public Iterable<Item> getUserItems() {
        User user = securityService.getCurrentUser();

        return itemRepository.findByOwnerId(user.getId());
    }

    @PreAuthorize("@securityService.isResourceOwner(#id, 'item')")
    public Item getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.ITEM, id));
    }

    @PreAuthorize("@securityService.isResourceOwner(#projectId, 'project')")
    public List<Item> getUnassignedItemsByProjectId(Long projectId) {
        return itemRepository.findUnassignedItemsByProjectId(projectId);
    }

    /* --- Create Operation (POST) --- */
    @Transactional
    public Item createItem(NewItemDTO itemDTO) {
        if (itemDTO.getOrgUnitId() == null) {
            return createUnassignedItem(itemDTO, itemDTO.getProjectIdAsLong());
        }
        return createItemInOrgUnit(itemDTO, itemDTO.getOrgUnitIdAsLong());
    }

    @PreAuthorize("@securityService.isResourceOwner(#orgUnitId, 'org-unit')")
    private Item createItemInOrgUnit(NewItemDTO itemDTO, Long orgUnitId) {
        OrgUnit orgUnit = orgUnitService.getOrgUnitById(orgUnitId);

        Item newItem = new Item(
                itemDTO.getName(),
                itemDTO.getDescription(),
                itemDTO.getTags(),
                itemDTO.getQuantity(),
                orgUnit);
        return itemRepository.save(newItem);
    }

    @PreAuthorize("@securityService.isResourceOwner(#projectId, 'project')")
    private Item createUnassignedItem(NewItemDTO itemDTO, Long projectId) {
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
        Item _item = getItemById(id);
        _item.setName(itemDTO.getName());
        if (itemDTO.getDescription() != null) {
            _item.setDescription(itemDTO.getDescription());
        }
        if (itemDTO.getQuantity() >= 1) {
            _item.setQuantity(itemDTO.getQuantity());
        }
        if (itemDTO.getTags() != null) {
            _item.setTags(itemDTO.getTags());
        }

        return itemRepository.save(_item);
    }

    /* --- Delete Operation (DELETE) --- */
    @Transactional
    public void deleteItem(Long id) {
        // Make sure item exists first
        getItemById(id);
        itemRepository.deleteById(id);
    }

    /* ------------- Complex Operations ------------- */
    @Transactional
    public Iterable<Item> assignItemsToOrgUnit(List<Long> itemIds, Long targetOrgUnitId) {
        OrgUnit targetOrgUnit = orgUnitService.getOrgUnitById(targetOrgUnitId);

        List<Item> updatedItems = new ArrayList<>();

        for (Long itemId : itemIds) {
            Item item = getItemById(itemId);

            validateSameProject(item, targetOrgUnit);

            // Check if item is already assigned to an org unit
            if (item.getOrgUnit() != null) {
                unassignItemFromOrgUnit(item, item.getOrgUnit());
            }
            assignItemToOrgUnit(item, targetOrgUnit);
            updatedItems.add(item);
        }
        return updatedItems;
    }

    @Transactional
    public Iterable<Item> unassignItems(List<Long> itemIds) {
        List<Item> updatedItems = new ArrayList<>();
        for (Long itemId : itemIds) {
            Item item = getItemById(itemId);
            if (item.getOrgUnit() != null) {
                unassignItemFromOrgUnit(item, item.getOrgUnit());
            }
            updatedItems.add(item);
        }
        return updatedItems;
    }

    /* ------------- Ownership and Security Checks ------------- */
    public void checkOwnershipForItems(List<Long> itemIds) {
        for (Long id : itemIds) {
            if (!securityService.isResourceOwner(id, "item")) {
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
}
