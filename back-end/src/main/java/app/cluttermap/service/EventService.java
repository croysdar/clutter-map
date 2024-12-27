package app.cluttermap.service;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import app.cluttermap.model.Event;
import app.cluttermap.model.Project;
import app.cluttermap.model.User;
import app.cluttermap.repository.EventRepository;
import app.cluttermap.util.EventActionType;
import app.cluttermap.util.ResourceType;

@Service
public class EventService {
    /* ------------- Injected Dependencies ------------- */
    private final EventRepository eventRepository;
    private final SecurityService securityService;
    private final ProjectAccessService projectAccessService;
    private final EntityResolutionService entityResolutionService;

    private final EventService self;

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    /* ------------- Constructor ------------- */
    public EventService(
            SecurityService securityService,
            ProjectAccessService projectAccessService,
            EntityResolutionService entityResolutionService,
            EventRepository eventRepository,
            @Lazy EventService self) {
        this.entityResolutionService = entityResolutionService;
        this.projectAccessService = projectAccessService;
        this.securityService = securityService;
        this.eventRepository = eventRepository;
        this.self = self;
    }

    /* ------------- CRUD Operations ------------- */
    /* --- Read Operations (GET) --- */

    public Page<Event> getAllEventsInProject(Project project, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return eventRepository.findAllEventsInProject(project, pageable);
    }

    public Page<Event> getEventsForEntity(ResourceType entityType, Long entityId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return eventRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable);
    }

    public Map<ResourceType, Set<Long>> getChangedEntitiesSince(LocalDateTime since) {
        List<Long> projectIds = projectAccessService.getAccessibleProjectIds();

        if (projectIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Object[]> rawResults = eventRepository.findChangesSince(since, projectIds);

        Map<ResourceType, Set<Long>> changes = new HashMap<>();
        for (Object[] result : rawResults) {
            ResourceType type = (ResourceType) result[0];
            Long id = (Long) result[1];
            changes.computeIfAbsent(type, k -> new HashSet<>()).add(id);
        }

        return changes;
    }

    /* --- Create Operation (POST) --- */
    public Event logEvent(
            ResourceType entityType,
            Long entityId,
            EventActionType actionType,
            Map<String, Object> payload) {
        Event event = initializeEvent(entityType, entityId, actionType);

        if (payload != null) {
            event.setPayload(convertToJson(payload));
        }

        return self.save(event);
    }

    /* --- Update Operation (PUT) --- */
    // EVENTS ARE IMMUTABLE, DO NOT CREATE ANY UPDATE OPERATIONS

    /* --- Delete Operation (DELETE) --- */

    /* ------------- Complex Operations ------------- */

    public Event save(Event event) {
        return eventRepository.save(event);
    }

    public String convertToJson(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY); // Exclude empty collections

        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("Failed to convert object to JSON: " + object, e);
            throw new RuntimeException("Error converting to JSON", e);
        }
    }

    public Map<String, Object> detectChanges(Object oldEntity, Object newEntity) {
        Map<String, Object> changes = new HashMap<>();

        if (oldEntity == null || newEntity == null || !oldEntity.getClass().equals(newEntity.getClass())) {
            throw new IllegalArgumentException("Entities must be non-null and of the same class.");
        }

        Class<?> clazz = oldEntity.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true); // Make private fields accessible

            try {
                Object oldValue = field.get(oldEntity);
                Object newValue = field.get(newEntity);

                // Include only changed fields
                if ((oldValue != null && !oldValue.equals(newValue)) || (oldValue == null && newValue != null)) {
                    // Check if the field is a nested object with an 'id' field
                    Field idField = getIdField(field.getType());
                    if (idField != null) {
                        idField.setAccessible(true);
                        Long oldId = oldValue != null ? (Long) idField.get(oldValue) : null;
                        Long newId = newValue != null ? (Long) idField.get(newValue) : null;

                        // Add only the IDs if they differ
                        if (!Objects.equals(oldId, newId)) {
                            changes.put(field.getName() + "Id", newId);
                        }
                    } else {
                        // For other fields, store the new value
                        changes.put(field.getName(), newValue);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error accessing field: " + field.getName(), e);
            }
        }

        return changes;
    }

    /* --- Private Helper Methods --- */
    private Event initializeEvent(ResourceType entityType, Long entityId, EventActionType actionType) {
        if (entityType == null || entityId == null || actionType == null) {
            throw new IllegalArgumentException("Entity type, ID, and action type must not be null");
        }
        Event event = new Event();
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setAction(actionType);
        User user = securityService.getCurrentUser();
        event.setUser(user);
        event.setProject(entityResolutionService.resolveProject(entityType, entityId));
        return event;
    }

    /**
     * Checks if a class has an 'id' field and returns it if found.
     */
    private Field getIdField(Class<?> clazz) {
        try {
            return clazz.getDeclaredField("id");
        } catch (NoSuchFieldException e) {
            return null; // Not all classes will have an 'id' field
        }
    }
}
