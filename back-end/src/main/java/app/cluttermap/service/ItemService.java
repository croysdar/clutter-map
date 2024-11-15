package app.cluttermap.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import app.cluttermap.exception.item.ItemNotFoundException;
import app.cluttermap.model.Item;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.NewItemDTO;
import app.cluttermap.model.dto.UpdateItemDTO;
import app.cluttermap.repository.ItemRepository;
import app.cluttermap.repository.OrgUnitRepository;
import jakarta.transaction.Transactional;

@Service("itemService")
public class ItemService {
    private final OrgUnitRepository orgUnitRepository;
    private final ItemRepository itemRepository;
    private final SecurityService securityService;
    private final OrgUnitService orgUnitService;

    public ItemService(
            OrgUnitRepository orgUnitRepository,
            ItemRepository itemRepository,
            SecurityService securityService,
            OrgUnitService orgUnitService) {
        this.orgUnitRepository = orgUnitRepository;
        this.itemRepository = itemRepository;
        this.securityService = securityService;
        this.orgUnitService = orgUnitService;
    }

    public static final String PROJECT_MISMATCH_ERROR = "Cannot move item to a different project's organization unit.";

    public Item getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException());
    }

    public List<Item> getUnassignedItemsByProjectId(Long projectId) {
        return itemRepository.findUnassignedItemsByProjectId(projectId);
    }

    @Transactional
    public Item createItem(NewItemDTO itemDTO) {
        OrgUnit orgUnit = orgUnitService.getOrgUnitById(itemDTO.getOrgUnitIdAsLong());

        Item newItem = new Item(
                itemDTO.getName(),
                itemDTO.getDescription(),
                itemDTO.getTags(),
                orgUnit);
        return itemRepository.save(newItem);
    }

    public Iterable<Item> getUserItems() {
        User user = securityService.getCurrentUser();

        return itemRepository.findByOwnerId(user.getId());
    }

    @Transactional
    public Item updateItem(Long id, UpdateItemDTO itemDTO) {
        Item _item = getItemById(id);
        _item.setName(itemDTO.getName());
        if (itemDTO.getDescription() != null) {
            _item.setDescription(itemDTO.getDescription());
        }
        if (itemDTO.getTags() != null) {
            _item.setTags(itemDTO.getTags());
        }

        return itemRepository.save(_item);
    }

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
        // TODO allow for partial success

        List<Item> updatedItems = new ArrayList<>();
        for (Long itemId : itemIds) {
            Item item = getItemById(itemId);
            if (item.getOrgUnit() != null) {
                unassignItemFromOrgUnit(item, item.getOrgUnit());
            }
            updatedItems.add(item);
        }
        return updatedItems;

        // Fetch all items at once for the provided IDs
        // Iterable<Item> items = itemRepository.findAllById(itemIds);

        // If no items are found, throw an exception
        // if (items.isEmpty()) {
        // throw new ItemsNotFoundException("None of the specified items were found: " +
        // itemIds);
        // }
    }

    @Transactional
    public void deleteItem(Long id) {
        // Make sure item exists first
        getItemById(id);
        itemRepository.deleteById(id);
    }
}
