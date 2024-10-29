package app.cluttermap.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

import app.cluttermap.dto.NewOrgUnitDTO;
import app.cluttermap.dto.UpdateOrgUnitDTO;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.service.OrgUnitService;

@RestController
@RequestMapping("/org-units")
public class OrgUnitsController {

    @Autowired
    private final OrgUnitService orgUnitService;

    public OrgUnitsController(OrgUnitService orgUnitService) {
        this.orgUnitService = orgUnitService;
    }

    @GetMapping()
    public ResponseEntity<Iterable<OrgUnit>> getOrgUnits() {
        return new ResponseEntity<>(orgUnitService.getUserOrgUnits(), HttpStatus.OK);
    }

    @PostMapping()
    @PreAuthorize("@securityService.isResourceOwner(orgUnitDTO.roomId, 'room')")
    public ResponseEntity<OrgUnit> addOneOrgUnit(@RequestBody NewOrgUnitDTO orgUnitDTO) {
        return new ResponseEntity<>(orgUnitService.createOrgUnit(orgUnitDTO), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityService.isResourceOwner(#id, 'org-unit')")
    public ResponseEntity<OrgUnit> getOneOrgUnit(@PathVariable("id") Long id) {
        return new ResponseEntity<>(orgUnitService.getOrgUnitById(id), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isResourceOwner(#id, 'org-unit')")
    public ResponseEntity<OrgUnit> updateOneOrgUnit(@PathVariable("id") Long id,
            @RequestBody UpdateOrgUnitDTO orgUnitDTO) {
        return new ResponseEntity<>(orgUnitService.updateOrgUnit(id, orgUnitDTO), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isResourceOwner(#id, 'org-unit')")
    public ResponseEntity<OrgUnit> deleteOneOrgUnit(@PathVariable("id") Long id) {
        orgUnitService.deleteOrgUnit(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

// https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-requestmapping.html

// https://hackernoon.com/using-postgres-effectively-in-spring-boot-applications

// https://www.bezkoder.com/spring-boot-postgresql-example/