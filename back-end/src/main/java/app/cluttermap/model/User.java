package app.cluttermap.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreRemove;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_users")
public class User {

    /* ------------- Fields ------------- */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String providerId;
    private String provider; // ex: 'google'
    private String username;
    private String firstName;
    private String lastName;
    private String email;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Project> projects = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = false)
    @JsonManagedReference
    private List<Event> events = new ArrayList<>();

    /* ------------- Constructors ------------- */
    // NOTE: Constructors should list parameters in the same order as the fields for
    // consistency.
    // Fields like 'projects' and 'createdAt' are not included because they are
    // initialized or managed automatically.

    // No-Arg constructor for Hibernate
    protected User() {
    }

    public User(String providerId) {
        this.providerId = providerId;
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

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public List<Event> getEvents() {
        return this.events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    /* ------------- Custom Builders (Fluent Methods) ------------- */

    public User id(Long id) {
        setId(id);
        return this;
    }

    public User providerId(String providerId) {
        setProviderId(providerId);
        return this;
    }

    public User provider(String provider) {
        setProvider(provider);
        return this;
    }

    public User username(String username) {
        setUsername(username);
        return this;
    }

    public User firstName(String firstName) {
        setFirstName(firstName);
        return this;
    }

    public User lastName(String lastName) {
        setLastName(lastName);
        return this;
    }

    public User email(String email) {
        setEmail(email);
        return this;
    }

    public User createdAt(Date createdAt) {
        setCreatedAt(createdAt);
        return this;
    }

    public User projects(List<Project> projects) {
        setProjects(projects);
        return this;
    }

    public User events(List<Event> events) {
        setEvents(events);
        return this;
    }

    /* ------------- Lifecycle Callback Methods ------------- */

    @PreRemove
    private void preRemove() {
        for (Event event : events) {
            event.setUser(null);
        }
    }

    /* ------------- Equals, HashCode, and ToString ------------- */

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof User)) {
            return false;
        }
        User user = (User) o;

        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", providerId=" + providerId +
                ", provider='" + provider + '\'' +
                ", username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                ", projects=" + (projects != null ? projects.size() : "null") +
                ", events=" + (events != null ? events.size() : "null") +
                '}';
    }
}
