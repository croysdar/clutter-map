package app.cluttermap.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.cluttermap.dto.NewItemDTO;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Item;
import app.cluttermap.repository.OrgUnitsRepository;
import app.cluttermap.repository.ItemsRepository;

@RestController
@RequestMapping("/Items")
public class ItemsController {
    @Autowired
    private final ItemsRepository ItemsRepository;

    @Autowired
    private final OrgUnitsRepository orgUnitsRepository;

    public ItemsController(ItemsRepository ItemsRepository, OrgUnitsRepository orgUnitsRepository) {
        this.ItemsRepository = ItemsRepository;
        this.orgUnitsRepository = orgUnitsRepository;
    }

    @GetMapping()
    public Iterable<Item> getItems() {
        return this.ItemsRepository.findAll();
    }

    @PostMapping()
    public ResponseEntity<Item> addOneItem(@RequestBody NewItemDTO ItemDTO) {
        if (ItemDTO.getName() == null || ItemDTO.getName().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (ItemDTO.getOrgUnitId() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Get orgUnit from orgUnit ID
        Optional<OrgUnit> orgUnitData = this.orgUnitsRepository.findById(Long.parseLong(ItemDTO.getOrgUnitId()));
        if (!orgUnitData.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Create Item using name, description, and orgUnit
        Item newItem = new Item(ItemDTO.getName(), ItemDTO.getDescription(), orgUnitData.get());

        return new ResponseEntity<>(this.ItemsRepository.save(newItem), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getOneItem(@PathVariable("id") Long id) {
        Optional<Item> ItemData = ItemsRepository.findById(id);

        if (ItemData.isPresent()) {
            return new ResponseEntity<>(ItemData.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> updateOneItem(@PathVariable("id") Long id, @RequestBody Item Item) {
        Optional<Item> ItemData = ItemsRepository.findById(id);

        if (ItemData.isPresent()) {
            Item _Item = ItemData.get();
            _Item.setName(Item.getName());
            _Item.setDescription(Item.getDescription());

            return new ResponseEntity<>(ItemsRepository.save(_Item), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Item> deleteOneItem(@PathVariable("id") Long id) {
        Optional<Item> ItemData = ItemsRepository.findById(id);

        if (ItemData.isPresent()) {
            try {
                ItemsRepository.deleteById(id);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}

// https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-requestmapping.html

// https://hackernoon.com/using-postgres-effectively-in-spring-boot-applications

// https://www.bezkoder.com/spring-boot-postgresql-example/