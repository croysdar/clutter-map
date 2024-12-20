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
    /* ------------- Injected Dependencies ------------- */
    private final ItemService itemService;

    /* ------------- Constructor ------------- */
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    /* ------------- GET Operations ------------- */
    @GetMapping()
    public ResponseEntity<Iterable<Item>> getItems() {
        return ResponseEntity.ok(itemService.getUserItems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getOneItem(@PathVariable("id") Long id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    /* ------------- POST Operations ------------- */
    @PostMapping()
    public ResponseEntity<Item> addOneItem(@Valid @RequestBody NewItemDTO itemDTO) {
        return ResponseEntity.ok(itemService.createItem(itemDTO));
    }

    /* ------------- PUT Operations ------------- */
    @PutMapping("/{id}")
    public ResponseEntity<Item> updateOneItem(@PathVariable("id") Long id,
            @Valid @RequestBody UpdateItemDTO itemDTO) {
        return ResponseEntity.ok(itemService.updateItem(id, itemDTO));
    }

    @PutMapping("/unassign")
    public ResponseEntity<Iterable<Item>> unassignItems(@RequestBody List<Long> itemIds) {
        return ResponseEntity.ok(itemService.unassignItems(itemIds));
    }

    /* ------------- DELETE Operations ------------- */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOneItem(@PathVariable("id") Long id) {
        itemService.deleteItemById(id);
        return ResponseEntity.noContent().build();
    }
}
