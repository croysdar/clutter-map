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
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.dto.ItemDTO;
import app.cluttermap.model.dto.NewOrgUnitDTO;
import app.cluttermap.model.dto.OrgUnitDTO;
import app.cluttermap.model.dto.UpdateOrgUnitDTO;
import app.cluttermap.service.ItemService;
import app.cluttermap.service.OrgUnitService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/org-units")
public class OrgUnitController {
    /* ------------- Injected Dependencies ------------- */
    private final OrgUnitService orgUnitService;
    private final ItemService itemService;

    /* ------------- Constructor ------------- */
    public OrgUnitController(
            OrgUnitService orgUnitService,
            ItemService itemService) {
        this.orgUnitService = orgUnitService;
        this.itemService = itemService;
    }

    /* ------------- GET Operations ------------- */
    @GetMapping()
    public ResponseEntity<List<OrgUnitDTO>> getOrgUnits() {
        List<OrgUnitDTO> orgUnitDTOS = new ArrayList<>();
        for (OrgUnit orgUnit : orgUnitService.getUserOrgUnits()) {
            orgUnitDTOS.add(new OrgUnitDTO(orgUnit));
        }
        return ResponseEntity.ok(orgUnitDTOS);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrgUnitDTO> getOneOrgUnit(@PathVariable("id") Long id) {
        return ResponseEntity.ok(new OrgUnitDTO(orgUnitService.getOrgUnitById(id)));
    }

    // TODO should this be a query like /items?org-unit={id}
    @GetMapping("/{id}/items")
    public ResponseEntity<List<ItemDTO>> getOrgUnitItems(@PathVariable("id") Long id) {
        List<ItemDTO> itemDTOs = new ArrayList<>();
        for (Item item : orgUnitService.getOrgUnitById(id).getItems()) {
            itemDTOs.add(new ItemDTO(item));
        }
        return ResponseEntity.ok(itemDTOs);
    }

    /* ------------- POST Operations ------------- */
    @PostMapping()
    public ResponseEntity<OrgUnitDTO> addOneOrgUnit(@Valid @RequestBody NewOrgUnitDTO orgUnitDTO) {
        return ResponseEntity.ok(new OrgUnitDTO(orgUnitService.createOrgUnit(orgUnitDTO)));
    }

    /* ------------- PUT Operations ------------- */
    @PutMapping("/{id}")
    public ResponseEntity<OrgUnitDTO> updateOneOrgUnit(@PathVariable("id") Long id,
            @Valid @RequestBody UpdateOrgUnitDTO orgUnitDTO) {
        return ResponseEntity.ok(new OrgUnitDTO(orgUnitService.updateOrgUnit(id, orgUnitDTO)));
    }

    @PutMapping("/{orgUnitId}/items")
    public ResponseEntity<List<ItemDTO>> assignItemsToOrgUnit(
            @PathVariable Long orgUnitId,
            @RequestBody List<Long> itemIds) {

        List<ItemDTO> itemDTOs = new ArrayList<>();
        for (Item item : itemService.assignItemsToOrgUnit(itemIds, orgUnitId)) {
            itemDTOs.add(new ItemDTO(item));
        }
        return ResponseEntity.ok(itemDTOs);
    }

    @PutMapping("/unassign")
    public ResponseEntity<Iterable<OrgUnitDTO>> unassignOrgUnits(@RequestBody List<Long> orgUnitIds) {
        orgUnitService.checkOwnershipForOrgUnits(orgUnitIds);
        List<OrgUnitDTO> unassignedOrgUnitDTOs = orgUnitService.unassignOrgUnits(orgUnitIds).stream()
                .map(OrgUnitDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(unassignedOrgUnitDTOs);
    }

    /* ------------- DELETE Operations ------------- */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOneOrgUnit(@PathVariable("id") Long id) {
        orgUnitService.deleteOrgUnitById(id);
        return ResponseEntity.noContent().build();
    }
}

// https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-requestmapping.html

// https://hackernoon.com/using-postgres-effectively-in-spring-boot-applications

// https://www.bezkoder.com/spring-boot-postgresql-example/