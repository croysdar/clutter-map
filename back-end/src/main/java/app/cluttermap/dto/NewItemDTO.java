package app.cluttermap.dto;

public class NewItemDTO {
    private String name;
    private String description;
    private String orgUnitId;


    public NewItemDTO(String name, String description, String orgUnitId) {
        this.name = name;
        this.description = description;
        this.orgUnitId = orgUnitId;
    }

    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getOrgUnitId() {
        return orgUnitId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOrgUnitId(String orgUnitId) {
        this.orgUnitId = orgUnitId;
    }
    
    
}
