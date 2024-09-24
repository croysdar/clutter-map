package app.cluttermap.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.cluttermap.model.Room;
import app.cluttermap.repository.RoomsRepository;

@RestController
@RequestMapping("/rooms")
@CrossOrigin(origins = "http://localhost:3000")
public class RoomsController {
    @Autowired
    private final RoomsRepository roomsRepository;

    public RoomsController(RoomsRepository roomsRepository) {
        this.roomsRepository = roomsRepository;
    }

    @GetMapping()
    public Iterable<Room> getRooms() {
        return this.roomsRepository.findAll();
    }

    @PostMapping()
    public ResponseEntity<Room> addOneRoom(@RequestBody Room room) {
        if (room.getName() == null || room.getName().isEmpty()){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Room savedRoom = this.roomsRepository.save(room);
        return new ResponseEntity<>(savedRoom, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getOneRoom(@PathVariable("id") Long id) {
        Optional<Room> roomData = roomsRepository.findById(id);

        if (roomData.isPresent()) {
            return new ResponseEntity<>(roomData.get(), HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}


// https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-requestmapping.html

// https://hackernoon.com/using-postgres-effectively-in-spring-boot-applications