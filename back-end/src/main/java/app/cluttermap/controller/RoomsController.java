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

import app.cluttermap.dto.NewRoomDTO;
import app.cluttermap.dto.UpdateRoomDTO;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Room;
import app.cluttermap.service.RoomService;

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
        return new ResponseEntity<>(roomService.getUserRooms(), HttpStatus.OK);
    }

    @PostMapping()
    @PreAuthorize("@securityService.isResourceOwner(authentication, #roomDTO.projectId, 'project')")
    public ResponseEntity<Room> addOneRoom(@RequestBody NewRoomDTO roomDTO) {
        return new ResponseEntity<>(roomService.createRoom(roomDTO), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityService.isResourceOwner(authentication, #id, 'room')")
    public ResponseEntity<Room> getOneRoom(@PathVariable("id") Long id) {
        return new ResponseEntity<>(roomService.getRoomById(id), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isResourceOwner(authentication, #id, 'room')")
    public ResponseEntity<Room> updateOneRoom(@PathVariable("id") Long id, @RequestBody UpdateRoomDTO roomDTO) {
        return new ResponseEntity<>(roomService.updateRoom(id, roomDTO), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isResourceOwner(authentication, #id, 'room')")
    public ResponseEntity<Void> deleteOneRoom(@PathVariable("id") Long id) {
        roomService.deleteRoom(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}/org-units")
    @PreAuthorize("@securityService.isResourceOwner(authentication, #id, 'room')")
    public ResponseEntity<Iterable<OrgUnit>> getRoomOrgUnits(@PathVariable("id") Long id) {
        return new ResponseEntity<>(roomService.getRoomById(id).getOrgUnits(), HttpStatus.OK);
    }
}

// https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-requestmapping.html

// https://hackernoon.com/using-postgres-effectively-in-spring-boot-applications

// https://www.bezkoder.com/spring-boot-postgresql-example/