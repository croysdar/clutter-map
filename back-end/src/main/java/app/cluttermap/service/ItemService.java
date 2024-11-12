package app.cluttermap.service;

import java.util.List;

import org.springframework.stereotype.Service;

import app.cluttermap.exception.item.ItemNotFoundException;
import app.cluttermap.exception.org_unit.OrgUnitNotFoundException;
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

    @Transactional
    public Item moveItemBetweenOrgUnits(Long itemId, Long targetOrgUnitId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException());

        OrgUnit currentOrgUnit = item.getOrgUnit();
        OrgUnit targetOrgUnit = orgUnitRepository.findById(targetOrgUnitId)
                .orElseThrow(() -> new OrgUnitNotFoundException());

        // Ensure the current and target OrgUnit are in the same Project
        if (currentOrgUnit != null && !currentOrgUnit.getProject().equals(targetOrgUnit.getProject())) {
            throw new IllegalArgumentException("Cannot move item to a different project's OrgUnit");
        }

        // Remove the item from the current OrgUnit (if any) and add it to the target
        // OrgUnit
        if (currentOrgUnit != null) {
            currentOrgUnit.removeItem(item);
        }
        targetOrgUnit.addItem(item);

        orgUnitRepository.save(targetOrgUnit);

        return item;
    }

    @Transactional
    public void deleteItem(Long id) {
        // Make sure item exists first
        getItemById(id);
        itemRepository.deleteById(id);
    }
}
