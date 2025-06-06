package app.cluttermap.model.dto;

import java.time.Instant;

import app.cluttermap.util.EventChangeType;
import app.cluttermap.util.ResourceType;

public class EntityHistoryDTO {
    ResourceType entityType;
    Long entityId;
    EventChangeType action;
    String details;
    String userName;
    Long userId;
    Instant timestamp;

    public EntityHistoryDTO() {
    }

    public EntityHistoryDTO(
            ResourceType entityType,
            Long entityId,
            EventChangeType action,
            String details,
            String userName,
            Long userId,
            Instant timestamp) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.details = details;
        this.userName = userName;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    public ResourceType getEntityType() {
        return this.entityType;
    }

    public Long getEntityId() {
        return this.entityId;
    }

    public EventChangeType getAction() {
        return this.action;
    }

    public String getDetails() {
        return this.details;
    }

    public String getUserName() {
        return this.userName;
    }

    public Long getUserId() {
        return this.userId;
    }

    public Instant getTimestamp() {
        return this.timestamp;
    }
}
