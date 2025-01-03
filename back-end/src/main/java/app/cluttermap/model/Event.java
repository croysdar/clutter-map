package app.cluttermap.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import app.cluttermap.util.EventActionType;
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

    @Enumerated(EnumType.STRING)
    @NotNull
    private EventActionType action;

    @NotNull
    private LocalDateTime timestamp = LocalDateTime.now();

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
            EventActionType action,
            Project project,
            User user) {

        if (action == null) {
            throw new IllegalArgumentException("EventActionType cannot be null");
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

    public EventActionType getAction() {
        return this.action;
    }

    public void setAction(EventActionType action) {
        this.action = action;
    }

    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
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

    public Event action(EventActionType action) {
        setAction(action);
        return this;
    }

    public Event timestamp(LocalDateTime timestamp) {
        setTimestamp(timestamp);
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
                ", action=" + action +
                ", timestamp=" + timestamp +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", project=" + (project != null ? project.getId() : "null") +
                '}';
    }

}
