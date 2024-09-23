package app.cluttermap.controller;

import org.springframework.web.bind.annotation.CrossOrigin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import app.cluttermap.model.Room;

import java.util.Arrays;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class RoomsController {
    @GetMapping("/rooms")
    public List<Room> getRooms() {
        System.out.println("GET /rooms called");

        return Arrays.asList(
                new Room("room1", "Kitchen", "The kitchen"),
                new Room("room2", "Living Room", "The room with the tv"),
                new Room("room3", "Master Bedroom", "The main bedroom"));
    }
}
