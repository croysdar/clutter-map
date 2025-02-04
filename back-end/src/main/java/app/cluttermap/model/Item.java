package app.cluttermap.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "items")
public class Item {

    /* ------------- Fields ------------- */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    private List<String> tags;

    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_unit_id", nullable = true)
    @JsonBackReference
    private OrgUnit orgUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = true)
    @JsonBackReference
    private Project project;

    /* ------------- Constructors ------------- */
    // NOTE: Constructors should list parameters in the same order as the fields for
    // consistency.

    // No-Arg constructor for Hibernate
    protected Item() {
    }

    public Item(
            String name,
            String description,
            List<String> tags,
            Integer quantity,
            OrgUnit orgUnit) {
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.quantity = quantity;
        if (orgUnit == null) {
            throw new IllegalArgumentException("OrgUnit cannot be null for this constructor.");
        }
        this.orgUnit = orgUnit;
        this.project = orgUnit.getProject();
    }

    public Item(
            String name,
            String description,
            List<String> tags,
            Integer quantity,
            Project project) {
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.quantity = quantity;
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public OrgUnit getOrgUnit() {
        return orgUnit;
    }

    public void setOrgUnit(OrgUnit orgUnit) {
        this.orgUnit = orgUnit;
        if (orgUnit != null && !orgUnit.getItems().contains(this)) {
            orgUnit.addItem(this);
        }
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    /* ------------- Custom Builders (Fluent Methods) ------------- */

    public Item id(Long id) {
        setId(id);
        return this;
    }

    public Item name(String name) {
        setName(name);
        return this;
    }

    public Item description(String description) {
        setDescription(description);
        return this;
    }

    public Item tags(List<String> tags) {
        setTags(tags);
        return this;
    }

    public Item quantity(Integer quantity) {
        setQuantity(quantity);
        return this;
    }

    public Item orgUnit(OrgUnit orgUnit) {
        setOrgUnit(orgUnit);
        return this;
    }

    public Item project(Project project) {
        setProject(project);
        return this;
    }

    /* ------------- Equals, HashCode, and ToString ------------- */

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Item)) {
            return false;
        }
        Item item = (Item) o;
        return Objects.equals(id, item.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String toString() {
        return "Item{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", tags=" + tags +
                ", quantity=" + quantity +
                ", orgUnitId=" + (orgUnit != null ? orgUnit.getId() : "null") +
                ", projectId=" + (project != null ? project.getId() : "null") +
                '}';
    }

    public Item copy() {
        Item copy = new Item();
        copy.setId(this.getId());
        copy.setName(this.getName());
        copy.setDescription(this.getDescription());
        copy.setQuantity(this.getQuantity());
        copy.setTags(new ArrayList<>(this.getTags()));
        copy.setOrgUnit(this.getOrgUnit());
        copy.setProject(this.getProject());
        return copy;
    }
}