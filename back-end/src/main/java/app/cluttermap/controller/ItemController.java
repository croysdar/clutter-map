package app.cluttermap.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.cluttermap.model.Item;
import app.cluttermap.model.dto.ItemDTO;
import app.cluttermap.model.dto.NewItemDTO;
import app.cluttermap.model.dto.UpdateItemDTO;
import app.cluttermap.service.ItemService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/items")
public class ItemController {
    /* ------------- Injected Dependencies ------------- */
    private final ItemService itemService;

    /* ------------- Constructor ------------- */
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    /* ------------- GET Operations ------------- */
    @GetMapping()
    public ResponseEntity<List<ItemDTO>> getItems() {
        List<ItemDTO> itemDTOs = new ArrayList<>();
        for (Item item : itemService.getUserItems()) {
            itemDTOs.add(new ItemDTO(item));
        }
        return ResponseEntity.ok(itemDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDTO> getOneItem(@PathVariable("id") Long id) {
        return ResponseEntity.ok(new ItemDTO(itemService.getItemById(id)));
    }

    /* ------------- POST Operations ------------- */
    @PostMapping()
    public ResponseEntity<ItemDTO> addOneItem(@Valid @RequestBody NewItemDTO itemDTO) {
        return ResponseEntity.ok(new ItemDTO(itemService.createItem(itemDTO)));
    }

    /* ------------- PUT Operations ------------- */
    @PutMapping("/{id}")
    public ResponseEntity<ItemDTO> updateOneItem(@PathVariable("id") Long id,
            @Valid @RequestBody UpdateItemDTO itemDTO) {
        return ResponseEntity.ok(new ItemDTO(itemService.updateItem(id, itemDTO)));
    }

    @PutMapping("/unassign")
    public ResponseEntity<List<ItemDTO>> unassignItems(@RequestBody List<Long> itemIds) {
        itemService.checkOwnershipForItems(itemIds);
        List<ItemDTO> unassignedItemDTOs = itemService.unassignItems(itemIds).stream()
                .map(ItemDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(unassignedItemDTOs);
    }

    /* ------------- DELETE Operations ------------- */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOneItem(@PathVariable("id") Long id) {
        itemService.deleteItemById(id);
        return ResponseEntity.noContent().build();
    }
}
