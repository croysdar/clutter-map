package app.cluttermap.service;

import org.springframework.stereotype.Service;

import app.cluttermap.exception.item.ItemNotFoundException;
import app.cluttermap.model.Item;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.NewItemDTO;
import app.cluttermap.model.dto.UpdateItemDTO;
import app.cluttermap.repository.ItemsRepository;
import jakarta.transaction.Transactional;

@Service("itemService")
public class ItemsService {
    private final ItemsRepository itemsRepository;
    private final SecurityService securityService;
    private final OrgUnitService orgUnitService;

    public ItemsService(ItemsRepository itemsRepository, SecurityService securityService,
            OrgUnitService orgUnitService) {
        this.itemsRepository = itemsRepository;
        this.securityService = securityService;
        this.orgUnitService = orgUnitService;
    }

    public Item getItemById(Long id) {
        return itemsRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException());
    }

    @Transactional
    public Item createItem(NewItemDTO itemDTO) {
        OrgUnit orgUnit = orgUnitService.getOrgUnitById(itemDTO.getOrgUnitIdAsLong());

        Item newItem = new Item(itemDTO.getName(), itemDTO.getDescription(), itemDTO.getTags(), orgUnit);
        return itemsRepository.save(newItem);
    }

    public Iterable<Item> getUserItems() {
        User user = securityService.getCurrentUser();

        return itemsRepository.findItemsByUserId(user.getId());
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

        return itemsRepository.save(_item);
    }

    @Transactional
    public void deleteItem(Long id) {
        // Make sure item exists first
        getItemById(id);
        itemsRepository.deleteById(id);
    }

}
