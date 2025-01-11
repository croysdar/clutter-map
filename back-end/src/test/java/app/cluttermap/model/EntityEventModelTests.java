package app.cluttermap.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import app.cluttermap.util.EventActionType;
import app.cluttermap.util.EventChangeType;
import app.cluttermap.util.ResourceType;

@ActiveProfiles("test")
public class EntityEventModelTests {
    @Test
    void eventEntity_ShouldSetFieldsCorrectly_WhenConstructed() {
        // Arrange
        EventChangeType changeType = EventChangeType.CREATE;
        ResourceType entityType = ResourceType.ITEM;
        Long entityId = 10L;
        String details = "{\"key\":\"value\"}";
        Event event = new Event(EventActionType.CREATE, new Project(), new User());

        // Act
        EventEntity eventEntity = new EventEntity(event, entityType, entityId, changeType, details);

        // Assert
        assertEquals(changeType, eventEntity.getChange(), "Change should match the constructor parameter");
        assertEquals(entityType, eventEntity.getEntityType(), "EntityType should match the constructor parameter");
        assertEquals(entityId, eventEntity.getEntityId(), "EntityId should match the constructor parameter");
        assertEquals(details, eventEntity.getDetails(), "Details should match the constructor parameter");
        assertEquals(event, eventEntity.getEvent(), "Event should match the constructor parameter");
    }

    @Test
    void toString_ShouldHandleNullFields() {
        // Arrange
        EventEntity eventEntity = new EventEntity();
        eventEntity.setId(1L);

        // Act
        String result = eventEntity.toString();

        // Assert
        assertThat(result).contains("id='1'");
        assertThat(result).contains("entityType='null'");
        assertThat(result).contains("entityId='null'");
        assertThat(result).contains("change='null'");
        assertThat(result).contains("details='null'");
    }

    @Test
    void toString_ShouldHandleNonNullFields() {
        // Arrange
        Event event = new Event(EventActionType.CREATE, new Project(), new User());
        ResourceType entityType = ResourceType.ROOM;
        Long entityId = 101L;
        EventChangeType changeType = EventChangeType.UPDATE;
        String details = "{\"field\":\"value\"}";

        EventEntity eventEntity = new EventEntity(event, entityType, entityId, changeType, details);
        eventEntity.setId(1L);

        // Act
        String result = eventEntity.toString();

        // Assert
        assertThat(result).contains("id='1'");
        assertThat(result).contains("entityType='ROOM'");
        assertThat(result).contains("entityId='101'");
        assertThat(result).contains("change='UPDATE'");
        assertThat(result).contains("details='{\"field\":\"value\"}'");
    }
}
