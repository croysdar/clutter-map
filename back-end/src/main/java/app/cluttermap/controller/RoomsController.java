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
                new Room(1001L, "Kitchen", "The kitchen"),
                new Room(1002L, "Living Room", "The room with the tv"),
                new Room(1003L, "Master Bedroom", "The main bedroom"));
    }
}


// https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-requestmapping.html

// https://hackernoon.com/using-postgres-effectively-in-spring-boot-applications