package app.cluttermap.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
import app.cluttermap.model.Project;
import app.cluttermap.model.User;
import app.cluttermap.util.EventActionType;
import app.cluttermap.util.ResourceType;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@EnableTestcontainers
public class EventRepositoryIntegrationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EventRepository eventRepository;

    private User mockUser;
    private Project mockProject;

    @BeforeEach
    void setUp() {
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
        List<Event> eventsToSave = new ArrayList<>();
        eventsToSave.add(new Event(
                ResourceType.ROOM, 1L,
                EventActionType.CREATE, null,
                mockProject, mockUser));
        for (int i = 0; i < 5; i++) {
            eventsToSave.add(new Event(
                    ResourceType.ROOM, 1L,
                    EventActionType.UPDATE, null,
                    mockProject, mockUser));
        }
        eventRepository.saveAll(eventsToSave);

        // Act: Retrieve paginated events
        Page<Event> events = eventRepository.findByEntityTypeAndEntityId(
                ResourceType.ROOM, 1L,
                createPageable(0, 2));

        // Assert
        assertPageSizeAndTotal(events, 2, 6, 3);
        events.forEach(e -> assertUserDetails(e, mockUser.getUsername()));
    }

    @Test
    void findByEntityTypeAndEntityId_ShouldOnlyReturnEventsForSpecifiedEntity() {
        // Arrange: Create events for different entities
        Event event1 = new Event(ResourceType.ROOM, 1L, EventActionType.CREATE, null, mockProject, mockUser);
        Event event2 = new Event(ResourceType.ITEM, 2L, EventActionType.CREATE, null, mockProject, mockUser);
        Event event3 = new Event(ResourceType.ITEM, 2L, EventActionType.UPDATE, null, mockProject, mockUser);
        eventRepository.saveAll(List.of(event1, event2, event3));

        // Act: Retrieve events for a specific entity type and ID
        Page<Event> project1Events = eventRepository.findByEntityTypeAndEntityId(
                ResourceType.ROOM, 1L, createPageable(0, 10));

        // Assert: Verify only events for the specified entity type and ID are returned
        assertThat(project1Events.getContent()).hasSize(1);
        assertThat(project1Events.getContent().get(0)).isEqualTo(event1);

        // Assert: Ensure no unrelated events are included
        assertThat(project1Events.getContent()).doesNotContain(event2, event3);
    }

    @Test
    void findAllEventsInProject_ShouldReturnPaginatedResults() {
        // Arrange: Create multiple events within the same project
        List<Event> eventsToSave = new ArrayList<>();
        eventsToSave.add(
                new Event(
                        ResourceType.ROOM, 1L,
                        EventActionType.CREATE, null,
                        mockProject, mockUser));
        for (int i = 0; i < 5; i++) {
            eventsToSave.add(
                    new Event(
                            ResourceType.ROOM, 1L,
                            EventActionType.UPDATE, null,
                            mockProject, mockUser));
        }
        eventRepository.saveAll(eventsToSave);

        // Act: Retrieve paginated events within the project
        Page<Event> events = eventRepository.findAllEventsInProject(mockProject, createPageable(0, 2));

        // Assert: Check pagination results
        assertPageSizeAndTotal(events, 2, 6, 3);

        // Verify user details are included
        events.forEach(e -> assertUserDetails(e, mockUser.getUsername()));
    }

    @Test
    void findAllEventsInProject_ShouldOnlyReturnEventsForSpecifiedProject() {
        // Arrange: Create events for two projects
        Project otherProject = projectRepository
                .save(new TestDataFactory.ProjectBuilder().name("Other Project").user(mockUser).build());

        Event event1 = new Event(ResourceType.ROOM, 1L, EventActionType.CREATE, null, mockProject, mockUser);
        Event event2 = new Event(ResourceType.ROOM, 2L, EventActionType.CREATE, null, otherProject, mockUser);
        eventRepository.saveAll(List.of(event1, event2));

        // Act: Retrieve events for mockProject
        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> projectEvents = eventRepository.findAllEventsInProject(mockProject, pageable);

        // Assert: Verify only events for mockProject are returned
        assertThat(projectEvents.getContent()).hasSize(1);
        assertThat(projectEvents.getContent().get(0)).isEqualTo(event1);

        // Assert: Ensure no unrelated events are included
        assertThat(projectEvents.getContent()).doesNotContain(event2);
    }

    @Test
    void findAllEventsInProject_ShouldOnlyIncludeUserIdAndUsername() {
        // Arrange
        Event event = new Event(ResourceType.ROOM, 1L, EventActionType.CREATE, null, mockProject, mockUser);
        eventRepository.save(event);

        // Act
        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> events = eventRepository.findAllEventsInProject(mockProject, pageable);

        // Assert
        events.forEach(e -> {
            assertThat(e.getUser().getId()).isNotNull();
            assertThat(e.getUser().getUsername()).isNotNull();
            // Ensure no other fields are fetched
            assertThat(e.getUser().getFirstName()).isNull();
            assertThat(e.getUser().getLastName()).isNull();
            assertThat(e.getUser().getEmail()).isNull();
        });
    }

    @Test
    void findAllEventsInProject_ShouldSortByTimestampDescending() {
        // Arrange: Create multiple events with different timestamps
        Event event1 = new Event(ResourceType.PROJECT, 1L, EventActionType.CREATE, null, mockProject, mockUser);
        event1.setTimestamp(LocalDateTime.now().minusDays(1));
        Event event2 = new Event(ResourceType.PROJECT, 1L, EventActionType.UPDATE, null, mockProject, mockUser);
        event2.setTimestamp(LocalDateTime.now());
        eventRepository.saveAll(List.of(event1, event2)); // Act: Retrieve events
        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> events = eventRepository.findAllEventsInProject(mockProject, pageable);

        // Assert: Verify events are sorted by timestamp descending
        assertThat(events.getContent()).containsExactly(event2, event1);
    }

    @Test
    void findAllEventsInProject_ShouldReturnEmptyPage_WhenNoEventsExist() {
        // Act: Retrieve events for a project with no associated events
        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> events = eventRepository.findAllEventsInProject(mockProject, pageable);

        // Assert: Verify empty result
        assertThat(events.getContent()).isEmpty();
    }

    @Test
    void findChangesSince_ShouldReturnDistinctEntities() {
        // Arrange: Create multiple events for different entities
        eventRepository.save(new Event(ResourceType.PROJECT, 1L, EventActionType.CREATE, null, mockProject, mockUser));
        eventRepository.save(new Event(ResourceType.ROOM, 1L, EventActionType.CREATE, null, mockProject, mockUser));
        eventRepository.save(new Event(ResourceType.ROOM, 2L, EventActionType.CREATE, null, mockProject, mockUser));

        // Act: Retrieve changes since a specific timestamp
        LocalDateTime since = LocalDateTime.now().minusDays(1);
        List<Object[]> changes = eventRepository.findChangesSince(since, List.of(mockProject.getId()));

        // Assert: Verify distinct entity types and IDs are returned
        assertThat(changes).hasSize(3);
        assertThat(changes).contains(
                new Object[] { ResourceType.PROJECT, 1L },
                new Object[] { ResourceType.ROOM, 1L },
                new Object[] { ResourceType.ROOM, 2L });
    }

    @Test
    void findChangesSince_ShouldOnlyReturnEventsSinceSpecifiedTimestamp() {
        // Arrange: Create events with different timestamps
        LocalDateTime timestamp1 = LocalDateTime.now().minusDays(2);
        LocalDateTime timestamp2 = LocalDateTime.now().minusHours(1);

        Event event1 = new Event(ResourceType.PROJECT, 1L, EventActionType.CREATE, null, mockProject, mockUser);
        event1.setTimestamp(timestamp1);
        Event event2 = new Event(ResourceType.ROOM, 2L, EventActionType.CREATE, null, mockProject, mockUser);
        event2.setTimestamp(timestamp2);
        eventRepository.saveAll(List.of(event1, event2));

        // Act: Retrieve changes since a specific timestamp
        LocalDateTime since = LocalDateTime.now().minusDays(1);
        List<Object[]> changes = eventRepository.findChangesSince(since, List.of(mockProject.getId()));

        // Assert: Verify only recent events are returned
        assertThat(changes).hasSize(1);
        assertThat(changes).containsExactly(new Object[] { ResourceType.ROOM, 2L });

        // Assert: Ensure older events are excluded
        assertThat(changes).doesNotContain(new Object[] { ResourceType.PROJECT, 1L });
    }

    @Test
    void findChangesSince_ShouldReturnCorrectFormat() {
        // Arrange
        eventRepository.save(new Event(ResourceType.PROJECT, 1L, EventActionType.CREATE, null, mockProject, mockUser));

        // Act
        List<Object[]> changes = eventRepository.findChangesSince(
                LocalDateTime.now().minusDays(1),
                List.of(mockProject.getId()));

        // Assert
        assertThat(changes).allMatch(change -> change.length == 2);
        assertThat(changes).allMatch(change -> change[0] instanceof ResourceType);
        assertThat(changes).allMatch(change -> change[1] instanceof Long);
    }

    protected Pageable createPageable(int page, int size) {
        return PageRequest.of(page, size);
    }

    protected void assertPageSizeAndTotal(Page<Event> page, int expectedSize, long expectedTotal, int expectedPages) {
        assertThat(page.getContent()).hasSize(expectedSize);
        assertThat(page.getTotalElements()).isEqualTo(expectedTotal);
        assertThat(page.getTotalPages()).isEqualTo(expectedPages);
    }

    protected void assertUserDetails(Event event, String expectedUsername) {
        assertThat(event.getUser()).isNotNull();
        assertThat(event.getUser().getUsername()).isEqualTo(expectedUsername);
    }

    protected Event createAndSaveEvent(ResourceType resourceType, Long entityId, EventActionType actionType,
            Project project, User user, LocalDateTime timestamp) {
        Event event = new Event(resourceType, entityId, actionType, null, project, user);
        if (timestamp != null) {
            event.setTimestamp(timestamp);
        }
        return eventRepository.save(event);
    }
}
