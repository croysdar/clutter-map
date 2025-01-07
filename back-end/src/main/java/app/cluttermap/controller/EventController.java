package app.cluttermap.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.cluttermap.model.dto.EntityHistoryDTO;
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
        LocalDateTime sinceTime = LocalDateTime.parse(since);

        List<EntityHistoryDTO> updates = eventService.fetchUpdatesSince(sinceTime);

        return ResponseEntity.ok(updates);
    }
}
