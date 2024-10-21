package app.cluttermap.model;

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
// table annotation overrides the default table name
@Table(name = "orgunits")
public class Item {
    
    @Id
    // Postgres generates an ID
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orgUnit_id")
    @JsonBackReference
    private OrgUnit orgUnit;

    // no-arg constructor for Hibernate
    protected Item() { }

    // public constructor
    // ID is not required because Postgres generates the ID
    public Item(String name, String description, OrgUnit orgUnit){
        this.name = name;
        this.description = description;
        this.orgUnit = orgUnit;
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

    public OrgUnit getOrgUnit() {
        return orgUnit;
    }

    public void setOrgUnit(OrgUnit orgUnit) {
        this.orgUnit = orgUnit;
    }
}
