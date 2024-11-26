package app.cluttermap.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Room;
import app.cluttermap.model.dto.NewRoomDTO;
import app.cluttermap.model.dto.UpdateRoomDTO;
import app.cluttermap.service.OrgUnitService;
import app.cluttermap.service.RoomService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/rooms")
@Validated
public class RoomController {
    /* ------------- Injected Dependencies ------------- */
    private final RoomService roomService;
    private final OrgUnitService orgUnitService;

    /* ------------- Constructor ------------- */
    public RoomController(
            RoomService roomService,
            OrgUnitService orgUnitService) {
        this.roomService = roomService;
        this.orgUnitService = orgUnitService;
    }

    /* ------------- GET Operations ------------- */
    @GetMapping()
    public ResponseEntity<Iterable<Room>> getRooms() {
        return ResponseEntity.ok(roomService.getUserRooms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getOneRoom(@PathVariable("id") Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @GetMapping("/{id}/org-units")
    public ResponseEntity<Iterable<OrgUnit>> getRoomOrgUnits(@PathVariable("id") Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id).getOrgUnits());
    }

    /* ------------- POST Operations ------------- */
    @PostMapping()
    public ResponseEntity<Room> addOneRoom(@Valid @RequestBody NewRoomDTO roomDTO) {
        return ResponseEntity.ok(roomService.createRoom(roomDTO));
    }

    /* ------------- PUT Operations ------------- */
    @PutMapping("/{id}")
    public ResponseEntity<Room> updateOneRoom(@PathVariable("id") Long id,
            @Valid @RequestBody UpdateRoomDTO roomDTO) {
        return ResponseEntity.ok(roomService.updateRoom(id, roomDTO));
    }

    @PutMapping("/{roomId}/org-units")
    public ResponseEntity<Iterable<OrgUnit>> assignOrgUnitsToRoom(
            @PathVariable Long roomId,
            @RequestBody List<Long> orgUnitIds) {

        Iterable<OrgUnit> updatedOrgUnits = orgUnitService.assignOrgUnitsToRoom(orgUnitIds, roomId);
        return ResponseEntity.ok(updatedOrgUnits);
    }

    /* ------------- DELETE Operations ------------- */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOneRoom(@PathVariable("id") Long id) {
        roomService.deleteRoomById(id);
        return ResponseEntity.noContent().build();
    }

}

// https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-requestmapping.html

// https://hackernoon.com/using-postgres-effectively-in-spring-boot-applications

// https://www.bezkoder.com/spring-boot-postgresql-example/