package app.cluttermap.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
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
import app.cluttermap.model.dto.OrgUnitDTO;
import app.cluttermap.model.dto.RoomDTO;
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
    public ResponseEntity<List<RoomDTO>> getRooms() {
        List<RoomDTO> roomDTOs = new ArrayList<>();
        for (Room room : roomService.getUserRooms()) {
            roomDTOs.add(new RoomDTO(room));
        }
        return ResponseEntity.ok(roomDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomDTO> getOneRoom(@PathVariable("id") Long id) {
        return ResponseEntity.ok(new RoomDTO(roomService.getRoomById(id)));
    }

    // TODO should this be a query like /org-units?room={id}
    @GetMapping("/{id}/org-units")
    public ResponseEntity<List<OrgUnitDTO>> getRoomOrgUnits(@PathVariable("id") Long id) {
        List<OrgUnitDTO> orgUnitDTOs = new ArrayList<>();
        for (OrgUnit orgUnit : roomService.getRoomById(id).getOrgUnits()) {
            orgUnitDTOs.add(new OrgUnitDTO(orgUnit));
        }
        return ResponseEntity.ok(orgUnitDTOs);
    }

    /* ------------- POST Operations ------------- */
    @PostMapping()
    public ResponseEntity<RoomDTO> addOneRoom(@Valid @RequestBody NewRoomDTO roomDTO) {
        return ResponseEntity.ok(new RoomDTO(roomService.createRoom(roomDTO)));
    }

    /* ------------- PUT Operations ------------- */
    @PutMapping("/{id}")
    public ResponseEntity<RoomDTO> updateOneRoom(@PathVariable("id") Long id,
            @Valid @RequestBody UpdateRoomDTO roomDTO) {
        return ResponseEntity.ok(new RoomDTO(roomService.updateRoom(id, roomDTO)));
    }

    @PutMapping("/{roomId}/org-units")
    public ResponseEntity<List<OrgUnitDTO>> assignOrgUnitsToRoom(
            @PathVariable Long roomId,
            @RequestBody List<Long> orgUnitIds) {

        List<OrgUnitDTO> orgUnitDTOs = new ArrayList<>();
        for (OrgUnit orgUnit : orgUnitService.assignOrgUnitsToRoom(orgUnitIds, roomId)) {
            orgUnitDTOs.add(new OrgUnitDTO(orgUnit));
        }
        return ResponseEntity.ok(orgUnitDTOs);
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