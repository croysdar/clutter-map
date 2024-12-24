package app.cluttermap.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.cluttermap.model.Event;
import app.cluttermap.service.EventService;
import app.cluttermap.util.ResourceType;

@RestController
@RequestMapping("/events")
public class EventController {
    /* ------------- Injected Dependencies ------------- */
    private final EventService eventService;

    /* ------------- Constructor ------------- */
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /* ------------- GET Operations ------------- */
    @GetMapping("/{entityType}/{id}")
    public ResponseEntity<Page<Event>> getEventsForEntity(
            @PathVariable("entityType") ResourceType entityType,
            @PathVariable("id") Long id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Page<Event> events = eventService.getEventsForEntity(entityType, id, page, size);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/changed-entities")
    public ResponseEntity<Map<ResourceType, Set<Long>>> getChangedEntitiesSince(@RequestParam("since") String since) {
        LocalDateTime sinceTime = LocalDateTime.parse(since);

        Map<ResourceType, Set<Long>> changes = eventService.getChangedEntitiesSince(sinceTime);

        return ResponseEntity.ok(changes);
    }
}
