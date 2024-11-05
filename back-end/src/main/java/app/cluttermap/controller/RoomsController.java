package app.cluttermap.controller;

import org.springframework.beans.factory.annotation.Autowired;
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

import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Room;
import app.cluttermap.model.dto.NewRoomDTO;
import app.cluttermap.model.dto.UpdateRoomDTO;
import app.cluttermap.service.RoomService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/rooms")
public class RoomsController {
    @Autowired
    private final RoomService roomService;

    public RoomsController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping()
    public ResponseEntity<Iterable<Room>> getRooms() {
        return ResponseEntity.ok(roomService.getUserRooms());
    }

    @PostMapping()
    @PreAuthorize("@securityService.isResourceOwner(#roomDTO.getProjectId(), 'project')")
    public ResponseEntity<Room> addOneRoom(@Valid @RequestBody NewRoomDTO roomDTO) {
        return ResponseEntity.ok(roomService.createRoom(roomDTO));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityService.isResourceOwner(#id, 'room')")
    public ResponseEntity<Room> getOneRoom(@PathVariable("id") Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @GetMapping("/{id}/org-units")
    @PreAuthorize("@securityService.isResourceOwner(#id, 'room')")
    public ResponseEntity<Iterable<OrgUnit>> getRoomOrgUnits(@PathVariable("id") Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id).getOrgUnits());
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isResourceOwner(#id, 'room')")
    public ResponseEntity<Room> updateOneRoom(@PathVariable("id") Long id,
            @Valid @RequestBody UpdateRoomDTO roomDTO) {
        return ResponseEntity.ok(roomService.updateRoom(id, roomDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isResourceOwner(#id, 'room')")
    public ResponseEntity<Void> deleteOneRoom(@PathVariable("id") Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

}

// https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-requestmapping.html

// https://hackernoon.com/using-postgres-effectively-in-spring-boot-applications

// https://www.bezkoder.com/spring-boot-postgresql-example/