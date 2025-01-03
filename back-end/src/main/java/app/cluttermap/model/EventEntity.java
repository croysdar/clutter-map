package app.cluttermap.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonBackReference;

import app.cluttermap.util.EventChangeType;
import app.cluttermap.util.ResourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "event_entities")
public class EventEntity {

    /* ------------- Fields ------------- */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @JsonBackReference
    private Event event;

    @Enumerated(EnumType.STRING)
    @NotNull
    private ResourceType entityType;

    @NotNull
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @NotNull
    private EventChangeType change;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String details;

    /* ------------- Constructors ------------- */
    // NOTE: Constructors should list parameters in the same order as the fields for
    // consistency.
    public EventEntity() {
    }

    public EventEntity(
            Event event,
            ResourceType entityType,
            Long entityId,
            EventChangeType change,
            String details) {
        this.event = event;
        this.entityType = entityType;
        this.entityId = entityId;
        this.change = change;
        this.details = details;
    }

    /* ------------- Getters and Setters ------------- */
    // NOTE: Getters and setters should follow the same order as the fields and
    // constructors for consistency.
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public ResourceType getEntityType() {
        return entityType;
    }

    public void setEntityType(ResourceType entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public EventChangeType getChange() {
        return change;
    }

    public void setChange(EventChangeType change) {
        this.change = change;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public EventEntity id(Long id) {
        setId(id);
        return this;
    }

    public EventEntity event(Event event) {
        setEvent(event);
        return this;
    }

    public EventEntity entityType(ResourceType entityType) {
        setEntityType(entityType);
        return this;
    }

    public EventEntity entityId(Long entityId) {
        setEntityId(entityId);
        return this;
    }

    public EventEntity change(EventChangeType change) {
        setChange(change);
        return this;
    }

    public EventEntity details(String details) {
        setDetails(details);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof EventEntity)) {
            return false;
        }
        EventEntity eventEntity = (EventEntity) o;
        return Objects.equals(id, eventEntity.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "EventEntity{" +
                " id='" + getId() + "'" +
                ", entityType='" + getEntityType() + "'" +
                ", entityId='" + getEntityId() + "'" +
                ", change='" + getChange() + "'" +
                ", details='" + getDetails() + "'" +
                "}";
    }
}
