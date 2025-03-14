package app.cluttermap.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import app.cluttermap.TestDataFactory;
import app.cluttermap.util.EventChangeType;

@ActiveProfiles("test")
public class EventModelTests {
    @Test
    void event_ShouldSetFieldsCorrectly_WhenConstructed() {
        // Arrange
        EventChangeType action = EventChangeType.CREATE;
        User user = new User("mockProviderId");
        Project project = new TestDataFactory.ProjectBuilder().user(user).build();

        // Act
        Event event = new Event(action, project, user);

        // Assert
        assertEquals(action, event.getAction(), "Action should match the constructor parameter");
        assertEquals(user, event.getUser(), "User should match the constructor parameter");
        assertEquals(project, event.getProject(), "Project should match the constructor parameter");
        assertNotNull(event.getTimestamp(), "Timestamp should be automatically generated");
        assertTrue(event.getEventEntities().isEmpty(), "EventEntities should be initialized as an empty list");
    }

    @Test
    void toString_ShouldHandleNullFields() {
        // Arrange
        Event event = new Event();
        event.setId(1L);
        event.setAction(EventChangeType.CREATE);
        event.setTimestamp(Instant.now());

        // Act
        String result = event.toString();

        // Assert
        assertThat(result).contains("user=null");
        assertThat(result).contains("project=null");
    }

    @Test
    void toString_ShouldHandleNonNullFields() {
        // Arrange
        User user = new User("provider_id");
        user.setUsername("john_doe");
        Project project = new Project();
        project.setId(101L);

        Event event = new Event(EventChangeType.CREATE, project, user);

        // Act
        String result = event.toString();

        // Assert
        assertThat(result).contains("user=john_doe");
        assertThat(result).contains("project=101");
    }
}
