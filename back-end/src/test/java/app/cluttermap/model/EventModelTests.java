package app.cluttermap.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import app.cluttermap.TestDataFactory;
import app.cluttermap.util.EventActionType;
import app.cluttermap.util.ResourceType;

@ActiveProfiles("test")
public class EventModelTests {
    @Test
    void event_ShouldSetFieldsCorrectly_WhenConstructed() {
        // Arrange
        ResourceType entityType = ResourceType.PROJECT;
        Long entityId = 123L;
        EventActionType action = EventActionType.CREATE;
        User user = new User("mockProviderId");
        Project project = new TestDataFactory.ProjectBuilder().user(user).build();
        String payload = "{\"key\":\"value\"}";

        // Act
        Event event = new Event(entityType, entityId, action, payload, project, user);

        event.setPayload(payload);

        // Assert
        assertEquals(entityType, event.getEntityType());
        assertEquals(entityId, event.getEntityId());
        assertEquals(action, event.getAction());
        assertEquals(user, event.getUser());
        assertEquals(project, event.getProject());
        assertEquals(payload, event.getPayload());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void event_ShouldHaveNullPayload_WhenNotSet() {
        // Arrange
        ResourceType entityType = ResourceType.PROJECT;
        Long entityId = 123L;
        EventActionType action = EventActionType.CREATE;
        User user = new User();
        Project project = new TestDataFactory.ProjectBuilder().user(user).build();

        // Act
        Event event = new Event(entityType, entityId, action, null, project, user);

        // Assert
        assertNull(event.getPayload());
    }

    @Test
    void toString_ShouldHandleNullFields() {
        // Arrange
        Event event = new Event();
        event.setId(1L);
        event.setEntityType(ResourceType.PROJECT);
        event.setEntityId(123L);
        event.setAction(EventActionType.CREATE);
        event.setTimestamp(LocalDateTime.now());

        // Act
        String result = event.toString();

        // Assert
        assertThat(result).contains("user=null");
        assertThat(result).contains("project=null");
    }
}
