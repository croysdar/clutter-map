package app.cluttermap.service;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import app.cluttermap.model.Event;
import app.cluttermap.model.EventEntity;
import app.cluttermap.model.Project;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.EntityHistoryDTO;
import app.cluttermap.repository.EventEntityRepository;
import app.cluttermap.repository.EventRepository;
import app.cluttermap.util.EventActionType;
import app.cluttermap.util.EventChangeType;
import app.cluttermap.util.ResourceType;
import jakarta.transaction.Transactional;

@Service
public class EventService {
    /* ------------- Injected Dependencies ------------- */
    private final EventRepository eventRepository;
    private final EventEntityRepository eventEntityRepository;
    private final SecurityService securityService;
    private final ProjectAccessService projectAccessService;
    private final EntityResolutionService entityResolutionService;

    private final EventService self;

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    /* ------------- Constructor ------------- */
    public EventService(
            EventRepository eventRepository,
            EventEntityRepository eventEntityRepository,
            SecurityService securityService,
            ProjectAccessService projectAccessService,
            EntityResolutionService entityResolutionService,
            @Lazy EventService self) {
        this.eventRepository = eventRepository;
        this.eventEntityRepository = eventEntityRepository;
        this.securityService = securityService;
        this.projectAccessService = projectAccessService;
        this.entityResolutionService = entityResolutionService;
        this.self = self;
    }

    /* ------------- CRUD Operations ------------- */
    /* --- Read Operations (GET) --- */
    @PreAuthorize("@securityService.isResourceOwner(#projectId, PROJECT)")
    public Page<Event> getAllEventsInProject(Long projectId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        // TODO should this be project.getEvents instead?
        return eventRepository.findAllEventsInProject(projectId, pageable);
    }

    @PreAuthorize("@securityService.isResourceOwner(#entityId, #entityType)")
    public Page<EntityHistoryDTO> getEntityHistory(ResourceType entityType, Long entityId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return eventEntityRepository.findHistoryByEntity(entityType, entityId, pageable);
    }

    public List<EntityHistoryDTO> fetchUpdatesSince(Instant since) {
        List<Long> projectIds = projectAccessService.getUpdatedProjectIds(since);

        if (projectIds.isEmpty()) {
            return Collections.emptyList();
        }

        return eventEntityRepository.findChangesSince(since, projectIds);
    }

    /* --- Create Operation (POST) --- */
    @Transactional
    public Event logEvent(
            ResourceType entityType,
            Long entityId,
            EventActionType actionType,
            Map<String, Object> payload) {

        Project project = entityResolutionService.resolveProject(entityType, entityId);
        Event event = initializeEvent(actionType, project);

        EventEntity eventEntity = new EventEntity(
                event, entityType, entityId,
                resolveSimpleEventAction(actionType), convertToJson(payload));

        event.addEventEntity(eventEntity);

        return self.save(event);
    }

    @Transactional
    public Event logMoveEvent(
            ResourceType entityType,
            Long entityId,
            ResourceType parentEntityType,
            Long previousParentId,
            Long newParentId) {

        Project project = entityResolutionService.resolveProject(entityType, entityId);

        Event event = initializeEvent(EventActionType.UPDATE, project);

        Map<String, Object> moveDetails = new HashMap<>();
        moveDetails.put("previousParentId", previousParentId);
        moveDetails.put("newParentId", newParentId);

        EventEntity moveEntity = new EventEntity(
                event, entityType, entityId,
                EventChangeType.MOVE, convertToJson(moveDetails));
        event.addEventEntity(moveEntity);

        if (previousParentId != null) {
            Map<String, Object> removeChildDetails = new HashMap<>();
            removeChildDetails.put("childId", entityId);
            EventEntity previousParentEntity = new EventEntity(
                    event, parentEntityType, previousParentId,
                    EventChangeType.REMOVE_CHILD, convertToJson(removeChildDetails));
            event.addEventEntity(previousParentEntity);
        }

        if (newParentId != null) {
            Map<String, Object> addChildDetails = new HashMap<>();
            addChildDetails.put("childId", entityId);
            EventEntity newParentEntity = new EventEntity(
                    event, parentEntityType, newParentId,
                    EventChangeType.ADD_CHILD, convertToJson(addChildDetails));
            event.addEventEntity(newParentEntity);
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
        if (object == null) {
            return "{}";
        }
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
    private Event initializeEvent(EventActionType actionType, Project project) {
        if (actionType == null || project == null) {
            throw new IllegalArgumentException("Action type and project must not be null");
        }
        Event event = new Event();
        event.setAction(actionType);
        event.setProject(project);
        User user = securityService.getCurrentUser();
        event.setUser(user);

        // Set last updated value in the project
        project.touch();
        return event;
    }

    private EventChangeType resolveSimpleEventAction(EventActionType type) {
        switch (type) {
            case CREATE:
                return EventChangeType.CREATE;
            case UPDATE:
                return EventChangeType.UPDATE;
            case DELETE:
                return EventChangeType.DELETE;
            default:
                return EventChangeType.UPDATE;
        }
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
