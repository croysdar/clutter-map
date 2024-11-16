package app.cluttermap.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.cluttermap.model.Item;
import app.cluttermap.model.dto.NewItemDTO;
import app.cluttermap.model.dto.UpdateItemDTO;
import app.cluttermap.service.ItemService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping()
    public ResponseEntity<Iterable<Item>> getItems() {
        return ResponseEntity.ok(itemService.getUserItems());
    }

    @PostMapping()
    @PreAuthorize("@securityService.isResourceOwner(#itemDTO.getOrgUnitId(), 'org-unit')")
    public ResponseEntity<Item> addOneItem(@Valid @RequestBody NewItemDTO itemDTO) {
        return ResponseEntity.ok(itemService.createItem(itemDTO));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityService.isResourceOwner(#id, 'item')")
    public ResponseEntity<Item> getOneItem(@PathVariable("id") Long id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isResourceOwner(#id, 'item')")
    public ResponseEntity<Item> updateOneItem(@PathVariable("id") Long id,
            @Valid @RequestBody UpdateItemDTO itemDTO) {
        return ResponseEntity.ok(itemService.updateItem(id, itemDTO));
    }

    @PutMapping("/unassign")
    public ResponseEntity<Iterable<Item>> unassignItems(@RequestBody List<Long> itemIds) {
        itemService.checkOwnershipForItems(itemIds);
        return ResponseEntity.ok(itemService.unassignItems(itemIds));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isResourceOwner(#id, 'item')")
    public ResponseEntity<Void> deleteOneItem(@PathVariable("id") Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}
