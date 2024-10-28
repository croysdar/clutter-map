package app.cluttermap.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.cluttermap.dto.NewRoomDTO;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.repository.ProjectsRepository;
import app.cluttermap.repository.RoomsRepository;
import app.cluttermap.service.SecurityService;

@RestController
@RequestMapping("/rooms")
public class RoomsController {
    @Autowired
    private final RoomsRepository roomsRepository;

    @Autowired
    private final ProjectsRepository projectsRepository;

    private final SecurityService securityService;

    public RoomsController(RoomsRepository roomsRepository, ProjectsRepository projectsRepository,
            SecurityService securityService) {
        this.roomsRepository = roomsRepository;
        this.projectsRepository = projectsRepository;
        this.securityService = securityService;
    }

    @GetMapping()
    public Iterable<Room> getRooms(Authentication authentication) {
        Long owner_id = securityService.getUserIdFromAuthentication(authentication);
        return roomsRepository.findRoomsByProjectOwnerId(owner_id);
    }

    @PostMapping()
    @PreAuthorize("@securityService.isResourceOwner(authentication, #roomDTO.projectId, 'project')")
    public ResponseEntity<Room> addOneRoom(@RequestBody NewRoomDTO roomDTO) {
        if (roomDTO.getName() == null || roomDTO.getName().isEmpty() || roomDTO.getProjectId() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Get project from project ID
        Optional<Project> projectData = this.projectsRepository.findById(Long.parseLong(roomDTO.getProjectId()));
        if (!projectData.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Create room using name, description, and project
        Room newRoom = new Room(roomDTO.getName(), roomDTO.getDescription(), projectData.get());

        return new ResponseEntity<>(this.roomsRepository.save(newRoom), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityService.isResourceOwner(authentication, #id, 'room')")
    public ResponseEntity<Room> getOneRoom(@PathVariable("id") Long id) {
        Optional<Room> roomData = roomsRepository.findById(id);

        if (roomData.isPresent()) {
            return new ResponseEntity<>(roomData.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isResourceOwner(authentication, #id, 'room')")
    public ResponseEntity<Room> updateOneRoom(@PathVariable("id") Long id, @RequestBody Room room) {
        Optional<Room> roomData = roomsRepository.findById(id);

        if (roomData.isPresent()) {
            Room _room = roomData.get();
            _room.setName(room.getName());
            _room.setDescription(room.getDescription());

            return new ResponseEntity<>(roomsRepository.save(_room), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isResourceOwner(authentication, #id, 'room')")
    public ResponseEntity<Room> deleteOneRoom(@PathVariable("id") Long id) {
        Optional<Room> roomData = roomsRepository.findById(id);

        if (roomData.isPresent()) {
            try {
                roomsRepository.deleteById(id);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}/org-units")
    @PreAuthorize("@securityService.isResourceOwner(authentication, #id, 'room')")
    public ResponseEntity<List<OrgUnit>> getRoomOrgUnits(@PathVariable("id") Long id) {
        Optional<Room> roomData = roomsRepository.findById(id);
        if (roomData.isPresent()) {
            Room room = roomData.get();
            return new ResponseEntity<>(room.getOrgUnits(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}

// https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-requestmapping.html

// https://hackernoon.com/using-postgres-effectively-in-spring-boot-applications

// https://www.bezkoder.com/spring-boot-postgresql-example/