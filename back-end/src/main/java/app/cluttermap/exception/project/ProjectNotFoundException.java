package app.cluttermap.exception.project;

public class ProjectNotFoundException extends RuntimeException{
    public ProjectNotFoundException() {
        super("Project not found.");
    }
}
