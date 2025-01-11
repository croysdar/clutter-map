package app.cluttermap.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "projects")
public class Project {

    /* ------------- Fields ------------- */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @JsonBackReference
    private User owner;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Room> rooms = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<OrgUnit> orgUnits = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Item> items = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Event> events = new ArrayList<>();

    /* ------------- Constructors ------------- */
    // NOTE: Constructors should list parameters in the same order as the fields for
    // consistency.
    // Fields like 'rooms', 'orgUnits', and 'items' are not included because they
    // are initialized with default values.

    // No-Arg constructor for Hibernate
    protected Project() {
    }

    public Project(String name, User owner) {
        this.owner = owner;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    public List<OrgUnit> getOrgUnits() {
        return orgUnits;
    }

    public void setOrgUnits(List<OrgUnit> orgUnits) {
        this.orgUnits = orgUnits;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    /* ------------- Custom Builders (Fluent Methods) ------------- */

    public Project id(Long id) {
        setId(id);
        return this;
    }

    public Project name(String name) {
        setName(name);
        return this;
    }

    public Project owner(User owner) {
        setOwner(owner);
        return this;
    }

    public Project rooms(List<Room> rooms) {
        setRooms(rooms);
        return this;
    }

    public Project orgUnits(List<OrgUnit> orgUnits) {
        setOrgUnits(orgUnits);
        return this;
    }

    public Project items(List<Item> items) {
        setItems(items);
        return this;
    }

    public Project events(List<Event> events) {
        setEvents(events);
        return this;
    }

    /* ------------- Utility Methods ------------- */

    public void addRoom(Room room) {
        if (!rooms.contains(room)) {
            rooms.add(room);
        }
        room.setProject(this);
    }

    public void removeRoom(Room room) {
        rooms.remove(room);
    }

    public void addOrgUnit(OrgUnit orgUnit) {
        if (!orgUnits.contains(orgUnit)) {
            orgUnits.add(orgUnit);
        }
        orgUnit.setProject(this);
    }

    public void removeOrgUnit(OrgUnit orgUnit) {
        orgUnits.remove(orgUnit);
    }

    public void addItem(Item item) {
        if (!items.contains(item)) {
            items.add(item);
        }
        item.setProject(this);
    }

    public void removeItem(Item item) {
        items.remove(item);
    }

    /* ------------- Equals, HashCode, and ToString ------------- */

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Project)) {
            return false;
        }
        Project project = (Project) o;
        return Objects.equals(id, project.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", owner=" + (owner != null ? owner.getUsername() : "null") +
                ", rooms=" + (rooms != null ? rooms.size() : "null") +
                ", orgUnits=" + (orgUnits != null ? orgUnits.size() : "null") +
                ", items=" + (items != null ? items.size() : "null") +
                ", events=" + (events != null ? events.size() : "null") +
                '}';
    }

    public Project copy() {
        Project copy = new Project();
        copy.setId(this.getId());
        copy.setName(this.getName());
        return copy;
    }
}
