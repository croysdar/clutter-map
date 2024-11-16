package app.cluttermap.model;

import java.util.List;

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

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    private List<String> tags;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonBackReference
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_unit_id", nullable = true)
    @JsonBackReference
    private OrgUnit orgUnit;

    private Integer quantity;

    // no-arg constructor for Hibernate
    protected Item() {
    }

    // public constructor
    // ID is not required because Postgres generates the ID
    public Item(String name, String description, List<String> tags, Integer quantity, OrgUnit orgUnit) {
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.quantity = quantity;
        this.orgUnit = orgUnit;
        this.project = orgUnit.getProject();
    }

    // This item is unassigned
    public Item(String name, String description, List<String> tags, Integer quantity, Project project) {
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.quantity = quantity;
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}