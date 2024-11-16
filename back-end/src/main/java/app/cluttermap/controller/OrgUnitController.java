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
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.dto.NewOrgUnitDTO;
import app.cluttermap.model.dto.UpdateOrgUnitDTO;
import app.cluttermap.service.ItemService;
import app.cluttermap.service.OrgUnitService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/org-units")
public class OrgUnitController {
    private final OrgUnitService orgUnitService;
    private final ItemService itemService;

    public OrgUnitController(
            OrgUnitService orgUnitService,
            ItemService itemService) {
        this.orgUnitService = orgUnitService;
        this.itemService = itemService;
    }

    @GetMapping()
    public ResponseEntity<Iterable<OrgUnit>> getOrgUnits() {
        return ResponseEntity.ok(orgUnitService.getUserOrgUnits());
    }

    @PostMapping()
    @PreAuthorize("@securityService.isResourceOwner(#orgUnitDTO.getRoomId(), 'room')")
    public ResponseEntity<OrgUnit> addOneOrgUnit(@Valid @RequestBody NewOrgUnitDTO orgUnitDTO) {
        return ResponseEntity.ok(orgUnitService.createOrgUnit(orgUnitDTO));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityService.isResourceOwner(#id, 'org-unit')")
    public ResponseEntity<OrgUnit> getOneOrgUnit(@PathVariable("id") Long id) {
        return ResponseEntity.ok(orgUnitService.getOrgUnitById(id));
    }

    @GetMapping("/{id}/items")
    @PreAuthorize("@securityService.isResourceOwner(#id, 'org-unit')")
    public ResponseEntity<Iterable<Item>> getOrgUnitItems(@PathVariable("id") Long id) {
        return ResponseEntity.ok(orgUnitService.getOrgUnitById(id).getItems());
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isResourceOwner(#id, 'org-unit')")
    public ResponseEntity<OrgUnit> updateOneOrgUnit(@PathVariable("id") Long id,
            @Valid @RequestBody UpdateOrgUnitDTO orgUnitDTO) {
        return ResponseEntity.ok(orgUnitService.updateOrgUnit(id, orgUnitDTO));
    }

    @PutMapping("/{orgUnitId}/items")
    @PreAuthorize("@securityService.isResourceOwner(#orgUnitId, 'org-unit')")
    public ResponseEntity<Iterable<Item>> assignItemsToOrgUnit(
            @PathVariable Long orgUnitId,
            @RequestBody List<Long> itemIds) {

        Iterable<Item> updatedItems = itemService.assignItemsToOrgUnit(itemIds, orgUnitId);
        return ResponseEntity.ok(updatedItems);
    }

    @PutMapping("/unassign")
    public ResponseEntity<Iterable<OrgUnit>> unassignOrgUnits(@RequestBody List<Long> orgUnitIds) {
        orgUnitService.checkOwnershipForOrgUnits(orgUnitIds);
        return ResponseEntity.ok(orgUnitService.unassignOrgUnits(orgUnitIds));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isResourceOwner(#id, 'org-unit')")
    public ResponseEntity<Void> deleteOneOrgUnit(@PathVariable("id") Long id) {
        orgUnitService.deleteOrgUnit(id);
        return ResponseEntity.noContent().build();
    }
}

// https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-requestmapping.html

// https://hackernoon.com/using-postgres-effectively-in-spring-boot-applications

// https://www.bezkoder.com/spring-boot-postgresql-example/