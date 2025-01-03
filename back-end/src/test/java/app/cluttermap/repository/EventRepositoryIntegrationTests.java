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
    void findAllEventsInProject_ShouldReturnPaginatedResults() {
        // Arrange: Create multiple events within the same project
        List<Event> eventsToSave = new ArrayList<>();
        eventsToSave.add(
                new Event(
                        EventActionType.CREATE,
                        mockProject, mockUser));
        for (int i = 0; i < 5; i++) {
            eventsToSave.add(
                    new Event(
                            EventActionType.UPDATE,
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

        Event event1 = new Event(EventActionType.CREATE, mockProject, mockUser);
        Event event2 = new Event(EventActionType.CREATE, otherProject, mockUser);
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
        Event event = new Event(EventActionType.CREATE, mockProject, mockUser);
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
        Event event1 = new Event(EventActionType.CREATE, mockProject, mockUser);
        event1.setTimestamp(LocalDateTime.now().minusDays(1));
        Event event2 = new Event(EventActionType.UPDATE, mockProject, mockUser);
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
        Event event = new Event(actionType, project, user);
        if (timestamp != null) {
            event.setTimestamp(timestamp);
        }
        return eventRepository.save(event);
    }
}
