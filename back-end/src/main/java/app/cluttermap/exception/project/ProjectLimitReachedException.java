package app.cluttermap.exception.project;

public class ProjectLimitReachedException extends RuntimeException {
    public ProjectLimitReachedException() {
        super("Maximum project limit reached.");
    }
}
