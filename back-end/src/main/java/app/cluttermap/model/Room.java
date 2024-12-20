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
@Table(name = "rooms")
public class Room {

    /* ------------- Fields ------------- */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @JsonBackReference
    private Project project;

    @OneToMany(mappedBy = "room", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = false)
    @JsonManagedReference
    private List<OrgUnit> orgUnits = new ArrayList<>();

    /* ------------- Constructors ------------- */
    // NOTE: Constructors should list parameters in the same order as the fields for
    // consistency.
    // Fields like 'orgUnits' are not included because they are initialized with
    // default values.

    // no-arg constructor for Hibernate
    protected Room() {
    }

    public Room(String name, String description, Project project) {
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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<OrgUnit> getOrgUnits() {
        return orgUnits;
    }

    public void setOrgUnits(List<OrgUnit> orgUnits) {
        this.orgUnits = orgUnits;
    }

    /* ------------- Utility Methods ------------- */

    public void addOrgUnit(OrgUnit orgUnit) {
        if (!orgUnits.contains(orgUnit)) {
            orgUnits.add(orgUnit);
        }
        orgUnit.setRoom(this);
    }

    public void removeOrgUnit(OrgUnit orgUnit) {
        if (orgUnits.contains(orgUnit)) {
            orgUnits.remove(orgUnit);
        }
        orgUnit.setRoom(null);
    }

    /* ------------- Lifecycle Callback Methods ------------- */

    @PreRemove
    private void preRemove() {
        for (OrgUnit orgUnit : orgUnits) {
            orgUnit.setRoom(null); // Unassign each orgUnit before Room deletion
        }
    }
}
