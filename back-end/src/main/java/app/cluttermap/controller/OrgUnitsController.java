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

import app.cluttermap.dto.NewOrgUnitDTO;
import app.cluttermap.model.Room;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.repository.RoomsRepository;
import app.cluttermap.repository.OrgUnitsRepository;

@RestController
@RequestMapping("/orgUnits")
public class OrgUnitsController {
    @Autowired
    private final OrgUnitsRepository orgUnitsRepository;

    @Autowired
    private final RoomsRepository roomsRepository;

    public OrgUnitsController(OrgUnitsRepository orgUnitsRepository, RoomsRepository roomsRepository) {
        this.orgUnitsRepository = orgUnitsRepository;
        this.roomsRepository = roomsRepository;
    }

    @GetMapping()
    public Iterable<OrgUnit> getOrgUnits() {
        return this.orgUnitsRepository.findAll();
    }

    @PostMapping()
    public ResponseEntity<OrgUnit> addOneOrgUnit(@RequestBody NewOrgUnitDTO orgUnitDTO) {
        if (orgUnitDTO.getName() == null || orgUnitDTO.getName().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (orgUnitDTO.getRoomId() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Get room from room ID
        Optional<Room> roomData = this.roomsRepository.findById(Long.parseLong(orgUnitDTO.getRoomId()));
        if (!roomData.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Create orgUnit using name, description, and room
        OrgUnit newOrgUnit = new OrgUnit(orgUnitDTO.getName(), orgUnitDTO.getDescription(), roomData.get());

        return new ResponseEntity<>(this.orgUnitsRepository.save(newOrgUnit), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrgUnit> getOneOrgUnit(@PathVariable("id") Long id) {
        Optional<OrgUnit> orgUnitData = orgUnitsRepository.findById(id);

        if (orgUnitData.isPresent()) {
            return new ResponseEntity<>(orgUnitData.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrgUnit> updateOneOrgUnit(@PathVariable("id") Long id, @RequestBody OrgUnit orgUnit) {
        Optional<OrgUnit> orgUnitData = orgUnitsRepository.findById(id);

        if (orgUnitData.isPresent()) {
            OrgUnit _orgUnit = orgUnitData.get();
            _orgUnit.setName(orgUnit.getName());
            _orgUnit.setDescription(orgUnit.getDescription());

            return new ResponseEntity<>(orgUnitsRepository.save(_orgUnit), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<OrgUnit> deleteOneOrgUnit(@PathVariable("id") Long id) {
        Optional<OrgUnit> orgUnitData = orgUnitsRepository.findById(id);

        if (orgUnitData.isPresent()) {
            try {
                orgUnitsRepository.deleteById(id);
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