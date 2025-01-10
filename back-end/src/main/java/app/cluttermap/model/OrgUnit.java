package app.cluttermap.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

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
import jakarta.persistence.PreRemove;
import jakarta.persistence.Table;

@Entity
@Table(name = "org_units")
public class OrgUnit {

    /* ------------- Fields ------------- */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = true)
    @JsonBackReference
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = true)
    @JsonBackReference
    private Project project;

    @OneToMany(mappedBy = "orgUnit", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = false)
    @JsonManagedReference
    private List<Item> items = new ArrayList<>();

    /* ------------- Constructors ------------- */
    // NOTE: Constructors should list parameters in the same order as the fields for
    // consistency.
    // Fields like 'items' are not included because they are initialized with
    // default values.

    // No-Arg constructor for Hibernate
    protected OrgUnit() {
    }

    public OrgUnit(
            String name,
            String description,
            Room room) {
        this.name = name;
        this.description = description;
        if (room == null) {
            throw new IllegalArgumentException("Room cannot be null for this constructor.");
        }
        this.room = room;
        this.project = room.getProject();
    }

    public OrgUnit(
            String name,
            String description,
            Project project) {
        this.name = name;
        this.description = description;
        this.project = project;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Room getRoom() {
        return room;
    }

    @JsonProperty("roomId")
    public Long getRoomId() {
        return room != null ? room.getId() : null;
    }

    @JsonProperty("roomName")
    public String getRoomName() {
        return room != null ? room.getName() : null;
    }

    public void setRoom(Room room) {
        this.room = room;
        if (room != null && !room.getOrgUnits().contains(this)) {
            room.addOrgUnit(this);
        }
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    /* ------------- Custom Builders (Fluent Methods) ------------- */

    public OrgUnit id(Long id) {
        setId(id);
        return this;
    }

    public OrgUnit name(String name) {
        setName(name);
        return this;
    }

    public OrgUnit description(String description) {
        setDescription(description);
        return this;
    }

    public OrgUnit room(Room room) {
        setRoom(room);
        return this;
    }

    public OrgUnit project(Project project) {
        setProject(project);
        return this;
    }

    public OrgUnit items(List<Item> items) {
        setItems(items);
        return this;
    }

    /* ------------- Utility Methods ------------- */

    public void addItem(Item item) {
        if (!items.contains(item)) {
            items.add(item);
        }
        item.setOrgUnit(this);
    }

    public void removeItem(Item item) {
        if (items.contains(item)) {
            items.remove(item);
        }
        item.setOrgUnit(null);
    }

    /* ------------- Lifecycle Callback Methods ------------- */

    @PreRemove
    private void preRemove() {
        for (Item item : items) {
            item.setOrgUnit(null); // Unassign each item before OrgUnit deletion
        }
    }

    /* ------------- Equals, HashCode, and ToString ------------- */

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof OrgUnit)) {
            return false;
        }
        OrgUnit orgUnit = (OrgUnit) o;
        return Objects.equals(id, orgUnit.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OrgUnit{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", roomId=" + (room != null ? room.getId() : "null") +
                ", projectId=" + (project != null ? project.getId() : "null") +
                '}';
    }

    public OrgUnit copy() {
        OrgUnit copy = new OrgUnit();
        copy.setId(this.getId());
        copy.setName(this.getName());
        copy.setDescription(this.getDescription());
        copy.setRoom(this.getRoom());
        copy.setProject(this.getProject());
        return copy;
    }
}
