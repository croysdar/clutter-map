package app.cluttermap.controller;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.cluttermap.model.dto.EntityHistoryDTO;
import app.cluttermap.service.EventService;
import app.cluttermap.util.ResourceType;

@RestController
public class EventController {
    /* ------------- Injected Dependencies ------------- */
    private final EventService eventService;

    /* ------------- Constructor ------------- */
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /* ------------- GET Operations ------------- */
    @GetMapping("/events/{entityType}/{id}")
    public ResponseEntity<PagedModel<EntityModel<EntityHistoryDTO>>> getEntityHistory(
            @PathVariable("entityType") ResourceType entityType,
            @PathVariable("id") Long id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            PagedResourcesAssembler<EntityHistoryDTO> assembler) {
        Page<EntityHistoryDTO> historyPage = eventService.getEntityHistory(entityType, id, page, size);

        return ResponseEntity.ok(assembler.toModel(historyPage));
    }

    @GetMapping("/fetch-updates")
    public ResponseEntity<List<EntityHistoryDTO>> getChangedEntitiesSince(@RequestParam("since") String since) {
        Instant sinceTime;
        try {
            // Try parsing as an ISO-8601 string (e.g., "2025-01-02T14:30:50Z")
            sinceTime = Instant.parse(since);
        } catch (DateTimeParseException e) {
            // If parsing fails, assume it's a Unix timestamp in milliseconds
            long epochMillis = Long.parseLong(since);
            sinceTime = Instant.ofEpochMilli(epochMillis);
        }

        List<EntityHistoryDTO> updates = eventService.fetchUpdatesSince(sinceTime);

        return ResponseEntity.ok(updates);
    }
}
