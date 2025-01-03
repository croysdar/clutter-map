package app.cluttermap.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import app.cluttermap.EnableTestcontainers;
import app.cluttermap.TestDataFactory;
import app.cluttermap.model.Event;
import app.cluttermap.model.EventEntity;
import app.cluttermap.model.Project;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.EntityHistoryDTO;
import app.cluttermap.util.EventActionType;
import app.cluttermap.util.EventChangeType;
import app.cluttermap.util.ResourceType;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@EnableTestcontainers
public class EventEntityRepositoryIntegrationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventEntityRepository eventEntityRepository;

    private User mockUser;
    private Project mockProject;

    @BeforeEach
    void setUp() {
        eventEntityRepository.deleteAll();
        eventRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        mockUser = new User("mockProviderId");
        mockUser.setUsername("mockUser");
        userRepository.save(mockUser);

        mockProject = projectRepository
                .save(new TestDataFactory.ProjectBuilder().name("Test Project").user(mockUser).build());
    }

    @Test
    void findByEntityTypeAndEntityId_ShouldReturnPaginatedResults() {
        // Arrange: Create multiple events
        Event createEvent = new Event(EventActionType.CREATE, mockProject, mockUser);
        EventEntity createEventEntity = new EventEntity(
                createEvent,
                ResourceType.ROOM, 1L,
                EventChangeType.CREATE,
                "");
        createEvent.addEventEntity(createEventEntity);
        eventRepository.save(createEvent);

        for (int i = 0; i < 5; i++) {
            Event updateEvent = new Event(EventActionType.CREATE, mockProject, mockUser);
            EventEntity updateEventEntity = new EventEntity(
                    updateEvent,
                    ResourceType.ROOM, 1L,
                    EventChangeType.UPDATE,
                    "");
            updateEvent.addEventEntity(updateEventEntity);
            eventRepository.save(updateEvent);
        }

        // Act: Retrieve paginated events
        Page<EntityHistoryDTO> events = eventEntityRepository.findHistoryByEntity(
                ResourceType.ROOM, 1L,
                createPageable(0, 2));

        // Assert
        assertPageSizeAndTotal(events, 2, 6, 3);
        events.forEach(e -> {
            assertUserDetails(e, mockUser.getUsername());
            // assertThat(e.getEntityType()).isEqualTo(ResourceType.ROOM);
            // assertThat(e.getEntityId()).isEqualTo(1L);
        });

    }

    @Test
    void findByEntityTypeAndEntityId_ShouldOnlyReturnEventsForSpecifiedEntity() {
        // Arrange: Create events and associated EventEntities for different entities
        Event event1 = new Event(EventActionType.CREATE, mockProject, mockUser);
        EventEntity entityEvent1 = new EventEntity(event1, ResourceType.ROOM, 1L, EventChangeType.CREATE, "");
        event1.addEventEntity(entityEvent1);
        eventRepository.save(event1);

        Event event2 = new Event(EventActionType.CREATE, mockProject, mockUser);
        EventEntity entityEvent2 = new EventEntity(event2, ResourceType.ITEM, 2L, EventChangeType.CREATE, "");
        event2.addEventEntity(entityEvent2);
        eventRepository.save(event2);

        Event event3 = new Event(EventActionType.UPDATE, mockProject, mockUser);
        EventEntity entityEvent3 = new EventEntity(event3, ResourceType.ROOM, 3L, EventChangeType.UPDATE, "");
        event3.addEventEntity(entityEvent3);
        eventRepository.save(event3);

        // Act: Retrieve events for a specific entity type and ID
        Page<EntityHistoryDTO> results = eventEntityRepository.findHistoryByEntity(
                ResourceType.ROOM, 1L, createPageable(0, 10));

        // Assert: Verify only events for the specified entity type and ID are returned
        assertThat(results.getContent()).hasSize(1);

        EntityHistoryDTO dto = results.getContent().get(0);
        assertThat(dto.getAction()).isEqualTo(EventChangeType.CREATE);
        assertThat(dto.getUserName()).isEqualTo(mockUser.getUsername());
    }

    @Test
    void findByEntityTypeAndEntityId_ShouldReturnAllMatchesForSameEntity() {
        // Arrange: Create multiple Events with the same ResourceType and entityId
        for (int i = 0; i < 3; i++) {
            Event event = new Event(EventActionType.UPDATE, mockProject, mockUser);
            EventEntity entity = new EventEntity(event, ResourceType.ROOM, 1L, EventChangeType.UPDATE, "");
            event.addEventEntity(entity);
            eventRepository.save(event);
        }

        // Act: Query for matching ResourceType and entityId
        Page<EntityHistoryDTO> results = eventEntityRepository.findHistoryByEntity(
                ResourceType.ROOM, 1L, createPageable(0, 10));

        // Assert: Verify all matches are returned
        assertThat(results.getContent()).hasSize(3);
        results.getContent().forEach(dto -> {
            assertThat(dto.getAction()).isEqualTo(EventChangeType.UPDATE);
            assertThat(dto.getUserName()).isEqualTo(mockUser.getUsername());
        });
    }

    @Test
    void findByEntityTypeAndEntityId_ShouldReturnEmptyWhenNoMatches() {
        // Arrange: Create and save an Event with a non-matching EventEntity
        Event event = new Event(EventActionType.CREATE, mockProject, mockUser);
        EventEntity entity = new EventEntity(event, ResourceType.ITEM, 2L, EventChangeType.CREATE, "");
        event.addEventEntity(entity);
        eventRepository.save(event);

        // Act: Query for a non-matching ResourceType and entityId
        Page<EntityHistoryDTO> results = eventEntityRepository.findHistoryByEntity(
                ResourceType.ROOM, 1L, createPageable(0, 10));

        // Assert: Verify no results are returned
        assertThat(results.getContent()).isEmpty();
    }

    @Test
    void orphanRemoval_ShouldDeleteEventEntitiesWhenParentEventIsDeleted() {
        // Arrange: Create and save an Event with EventEntities
        Event event = new Event(EventActionType.CREATE, mockProject, mockUser);
        EventEntity entityEvent1 = new EventEntity(event, ResourceType.ROOM, 1L, EventChangeType.CREATE, "");
        EventEntity entityEvent2 = new EventEntity(event, ResourceType.ITEM, 2L, EventChangeType.CREATE, "");
        event.addEventEntity(entityEvent1);
        event.addEventEntity(entityEvent2);
        eventRepository.save(event);

        // Act: Delete the parent Event
        eventRepository.delete(event);

        // Assert: Verify EventEntities are removed
        assertThat(eventEntityRepository.findAll()).isEmpty();
    }

    @Test
    void findChangesSince_ShouldReturnAllChanges() {
        // Arrange: Create multiple events with associated EventEntities
        Event event1 = new Event(EventActionType.CREATE, mockProject, mockUser);
        EventEntity entityEvent1 = new EventEntity(event1, ResourceType.PROJECT, 1L, EventChangeType.CREATE, "");
        event1.addEventEntity(entityEvent1);
        eventRepository.save(event1);

        Event event2 = new Event(EventActionType.CREATE, mockProject, mockUser);
        EventEntity entityEvent2 = new EventEntity(event2, ResourceType.ROOM, 1L, EventChangeType.CREATE, "");
        event2.addEventEntity(entityEvent2);
        eventRepository.save(event2);

        Event event3 = new Event(EventActionType.CREATE, mockProject, mockUser);
        EventEntity entityEvent3 = new EventEntity(event3, ResourceType.ROOM, 2L, EventChangeType.UPDATE, "");
        event3.addEventEntity(entityEvent3);
        eventRepository.save(event3);

        // Act: Retrieve changes since a specific timestamp
        LocalDateTime since = LocalDateTime.now().minusDays(1);
        List<EntityHistoryDTO> changes = eventEntityRepository.findChangesSince(since, List.of(mockProject.getId()));

        // Assert: Verify all changes are returned
        assertThat(changes).hasSize(3);
    }

    @Test
    void findChangesSince_ShouldOnlyReturnEventsSinceSpecifiedTimestamp() {
        // Arrange: Create events with different timestamps
        LocalDateTime timestamp1 = LocalDateTime.now().minusDays(2);
        LocalDateTime timestamp2 = LocalDateTime.now().minusHours(1);

        // Event 1 (older than "since" timestamp)
        Event event1 = new Event(EventActionType.CREATE, mockProject, mockUser);
        EventEntity entityEvent1 = new EventEntity(event1, ResourceType.PROJECT, 1L, EventChangeType.CREATE, "");
        event1.addEventEntity(entityEvent1);
        event1.setTimestamp(timestamp1);
        eventRepository.save(event1);

        // Event 2 (newer than "since" timestamp)
        Event event2 = new Event(EventActionType.CREATE, mockProject, mockUser);
        EventEntity entityEvent2 = new EventEntity(event2, ResourceType.ROOM, 2L, EventChangeType.CREATE, "");
        event2.addEventEntity(entityEvent2);
        event2.setTimestamp(timestamp2);
        eventRepository.save(event2);

        // Act: Retrieve changes since the specified timestamp
        LocalDateTime since = LocalDateTime.now().minusDays(1);
        List<EntityHistoryDTO> changes = eventEntityRepository.findChangesSince(since, List.of(mockProject.getId()));

        // Assert: Verify only recent events are returned
        assertThat(changes).hasSize(1);
        assertThat(changes.get(0).getTimestamp()).isAfter(since);

        // Ensure no events older than the timestamp are included
        assertThat(changes.stream().anyMatch(change -> change.getTimestamp().isBefore(since))).isFalse();
    }

    @Test
    void findChangesSince_ShouldReturnEmptyWhenNoEventsSinceTimestamp() {
        // Arrange: Create events all before the specified timestamp
        LocalDateTime timestamp1 = LocalDateTime.now().minusDays(3);
        LocalDateTime timestamp2 = LocalDateTime.now().minusDays(2);

        Event event1 = new Event(EventActionType.CREATE, mockProject, mockUser);
        EventEntity entityEvent1 = new EventEntity(event1, ResourceType.PROJECT, 1L, EventChangeType.CREATE, "");
        event1.addEventEntity(entityEvent1);
        event1.setTimestamp(timestamp1);
        eventRepository.save(event1);

        Event event2 = new Event(EventActionType.CREATE, mockProject, mockUser);
        EventEntity entityEvent2 = new EventEntity(event2, ResourceType.ROOM, 2L, EventChangeType.CREATE, "");
        event2.addEventEntity(entityEvent2);
        event2.setTimestamp(timestamp2);
        eventRepository.save(event2);

        // Act: Retrieve changes since a timestamp after all events
        LocalDateTime since = LocalDateTime.now().minusDays(1);
        List<EntityHistoryDTO> changes = eventEntityRepository.findChangesSince(since, List.of(mockProject.getId()));

        // Assert: Verify no changes are returned
        assertThat(changes).isEmpty();
    }

    @Test
    void findChangesSince_ShouldReturnMultipleChangesForSameEntity() {
        // Arrange: Create multiple changes for the same entity
        LocalDateTime timestamp = LocalDateTime.now().minusHours(1);

        for (int i = 0; i < 3; i++) {
            Event event = new Event(EventActionType.UPDATE, mockProject, mockUser);
            EventEntity entity = new EventEntity(event, ResourceType.ROOM, 1L, EventChangeType.UPDATE,
                    "{\"change\":\"change" + i + "\"}");
            event.addEventEntity(entity);
            event.setTimestamp(timestamp.plusMinutes(i * 10)); // Stagger timestamps
            eventRepository.save(event);
        }

        // Act: Retrieve changes since a specific timestamp
        LocalDateTime since = LocalDateTime.now().minusHours(2);
        List<EntityHistoryDTO> changes = eventEntityRepository.findChangesSince(since, List.of(mockProject.getId()));

        // Assert: Verify all changes for the same entity are returned
        assertThat(changes).hasSize(3);
        changes.forEach(change -> {
            assertThat(change.getEntityType()).isEqualTo(ResourceType.ROOM);
            assertThat(change.getEntityId()).isEqualTo(1L);
            assertThat(change.getAction()).isEqualTo(EventChangeType.UPDATE);
        });
    }

    @Test
    void findChangesSince_ShouldReturnCorrectFormat() {
        // Arrange: Create and save an Event with an associated EventEntity
        Event event = new Event(EventActionType.CREATE, mockProject, mockUser);
        EventEntity eventEntity = new EventEntity(
                event,
                ResourceType.PROJECT,
                1L,
                EventChangeType.CREATE,
                "{\"field\":\"value\"}");
        event.addEventEntity(eventEntity);
        eventRepository.save(event);

        // Act: Retrieve changes since a specific timestamp
        List<EntityHistoryDTO> changes = eventEntityRepository.findChangesSince(
                LocalDateTime.now().minusDays(1),
                List.of(mockProject.getId()));

        // Assert: Verify that the returned changes are correctly formatted
        assertThat(changes).hasSize(1);
        changes.forEach(change -> {
            assertThat(change.getEntityType()).isEqualTo(ResourceType.PROJECT);
            assertThat(change.getEntityId()).isEqualTo(1L);
            assertThat(change.getAction()).isEqualTo(EventChangeType.CREATE);
            assertThat(change.getDetails()).isEqualTo("{\"field\":\"value\"}");
        });
    }

    protected Pageable createPageable(int page, int size) {
        return PageRequest.of(page, size);
    }

    protected void assertPageSizeAndTotal(Page<EntityHistoryDTO> page, int expectedSize, long expectedTotal,
            int expectedPages) {
        assertThat(page.getContent()).hasSize(expectedSize);
        assertThat(page.getTotalElements()).isEqualTo(expectedTotal);
        assertThat(page.getTotalPages()).isEqualTo(expectedPages);
    }

    protected void assertUserDetails(EntityHistoryDTO dto, String expectedUsername) {
        assertThat(dto.getUserName()).isEqualTo(expectedUsername);
    }

    protected void assertUserDetails(EventEntity eventEntity, String expectedUsername) {
        assertThat(eventEntity.getEvent().getUser()).isNotNull();
        assertThat(eventEntity.getEvent().getUser().getUsername()).isEqualTo(expectedUsername);
    }

}
