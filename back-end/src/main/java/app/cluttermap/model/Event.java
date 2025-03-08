package app.cluttermap.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import app.cluttermap.util.EventChangeType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "events")
public class Event {

    /* ------------- Fields ------------- */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<EventEntity> eventEntities = new ArrayList<>();

    @NotNull
    private Instant timestamp = Instant.now();

    @Enumerated(EnumType.STRING)
    @NotNull
    private EventChangeType action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = true)
    @JsonBackReference
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    @JsonBackReference
    private User user;

    /* ------------- Constructors ------------- */
    // NOTE: Constructors should list parameters in the same order as the fields for
    // consistency.
    public Event() {
    }

    public Event(
            EventChangeType action,
            Project project,
            User user) {
        if (action == null) {
            throw new IllegalArgumentException("EventChangeType cannot be null");
        }
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        this.action = action;
        this.project = project;
        this.user = user;
    }

    /* ------------- Getters and Setters ------------- */
    // NOTE: Getters and setters should follow the same order as the fields and
    // constructors for consistency.

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<EventEntity> getEventEntities() {
        return this.eventEntities;
    }

    public void setEventEntities(List<EventEntity> eventEntities) {
        this.eventEntities = eventEntities;
    }

    public Instant getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public EventChangeType getAction() {
        return this.action;
    }

    public void setAction(EventChangeType action) {
        this.action = action;
    }

    public Project getProject() {
        return this.project;
    }

    @JsonProperty("projectId")
    public Long getProjectId() {
        return project.getId();
    }

    @JsonProperty("projectName")
    public String getProjectName() {
        return project.getName();
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getUser() {
        return this.user;
    }

    @JsonProperty("userId")
    public Long getUserId() {
        return user.getId();
    }

    @JsonProperty("userName")
    public String getUserName() {
        return user.getUsername();
    }

    public void setUser(User user) {
        this.user = user;
    }

    /* ------------- Custom Builders (Fluent Methods) ------------- */

    public Event id(Long id) {
        setId(id);
        return this;
    }

    public Event eventEntities(List<EventEntity> eventEntities) {
        setEventEntities(eventEntities);
        return this;
    }

    public Event timestamp(Instant timestamp) {
        setTimestamp(timestamp);
        return this;
    }

    public Event action(EventChangeType action) {
        setAction(action);
        return this;
    }

    public Event project(Project project) {
        setProject(project);
        return this;
    }

    public Event user(User user) {
        setUser(user);
        return this;
    }

    /* ------------- Utility Methods ------------- */

    public void addEventEntity(EventEntity entity) {
        eventEntities.add(entity);
        entity.setEvent(this);
    }

    /* ------------- Equals, HashCode, and ToString ------------- */

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Event)) {
            return false;
        }

        Event event = (Event) o;

        return Objects.equals(id, event.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", timestamp=" + timestamp +
                ", action=" + action +
                ", project=" + (project != null ? project.getId() : "null") +
                ", user=" + (user != null ? user.getUsername() : "null") +
                '}';
    }

}
