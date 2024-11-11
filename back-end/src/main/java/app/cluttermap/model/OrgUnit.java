package app.cluttermap.model;

import java.util.ArrayList;
import java.util.List;

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
import jakarta.persistence.PreRemove;
import jakarta.persistence.Table;

@Entity
@Table(name = "org_units")
public class OrgUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonBackReference
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = true)
    @JsonBackReference
    private Room room;

    @OneToMany(mappedBy = "orgUnit", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = false)
    @JsonManagedReference
    private List<Item> items = new ArrayList<>();

    @PreRemove
    private void preRemove() {
        for (Item item : items) {
            item.setOrgUnit(null); // Unassign each item before OrgUnit deletion
        }
    }

    // no-arg constructor for Hibernate
    protected OrgUnit() {
    }

    public OrgUnit(String name, String description, Room room) {
        this.name = name;
        this.description = description;
        this.room = room;
        this.project = room.getProject();
    }

    // This org unit is unassigned
    public OrgUnit(String name, String description, Project project) {
        this.name = name;
        this.description = description;
        this.project = project;
    }

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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
        if (room != null && !room.getOrgUnits().contains(this)) {
            room.addOrgUnit(this);
        }
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    // Method to add an Item to this OrgUnit
    public void addItem(Item item) {
        items.add(item);
        item.setOrgUnit(this); // Set the orgUnit reference in Item
    }

    // Method to remove an Item from this OrgUnit
    public void removeItem(Item item) {
        items.remove(item);
        item.setOrgUnit(null); // Unassign the orgUnit reference in Item
    }
}
