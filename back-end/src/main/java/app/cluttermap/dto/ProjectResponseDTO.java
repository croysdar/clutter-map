package app.cluttermap.dto;

import app.cluttermap.model.Project;

public class ProjectResponseDTO {
    private Project project;
    private String error;

    public ProjectResponseDTO(Project project) {
        this.project = project;
    }

    public ProjectResponseDTO(String error) {
        this.error = error;
    }

    public Project getProject() {
        return project;
    }

    public String getError() {
        return error;
    }
    
}
