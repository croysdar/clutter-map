package app.cluttermap.model.dto;

import java.time.LocalDateTime;

import app.cluttermap.util.EventChangeType;
import app.cluttermap.util.ResourceType;

public class EntityHistoryDTO {
    ResourceType entityType;
    Long entityId;
    EventChangeType action;
    String details;
    String userName;
    Long userId;
    LocalDateTime timestamp;

    public EntityHistoryDTO() {
    }

    public EntityHistoryDTO(ResourceType entityType, Long entityId, EventChangeType action, String details,
            String userName, Long userId, LocalDateTime timestamp) {
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

    public void setEntityType(ResourceType entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return this.entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public EventChangeType getAction() {
        return this.action;
    }

    public void setAction(EventChangeType action) {
        this.action = action;
    }

    public String getDetails() {
        return this.details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public EntityHistoryDTO entityType(ResourceType entityType) {
        setEntityType(entityType);
        return this;
    }

    public EntityHistoryDTO entityId(Long entityId) {
        setEntityId(entityId);
        return this;
    }

    public EntityHistoryDTO action(EventChangeType action) {
        setAction(action);
        return this;
    }

    public EntityHistoryDTO details(String details) {
        setDetails(details);
        return this;
    }

    public EntityHistoryDTO userName(String userName) {
        setUserName(userName);
        return this;
    }

    public EntityHistoryDTO userId(Long userId) {
        setUserId(userId);
        return this;
    }

    public EntityHistoryDTO timestamp(LocalDateTime timestamp) {
        setTimestamp(timestamp);
        return this;
    }
}
