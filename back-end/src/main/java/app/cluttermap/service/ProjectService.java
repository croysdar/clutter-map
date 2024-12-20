package app.cluttermap.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import app.cluttermap.exception.ResourceNotFoundException;
import app.cluttermap.exception.project.ProjectLimitReachedException;
import app.cluttermap.model.Project;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.NewProjectDTO;
import app.cluttermap.model.dto.UpdateProjectDTO;
import app.cluttermap.repository.ProjectRepository;
import app.cluttermap.util.ResourceType;
import jakarta.transaction.Transactional;

@Service("projectService")
public class ProjectService {
    /* ------------- Constants ------------- */
    private final int PROJECT_LIMIT = 3;

    /* ------------- Injected Dependencies ------------- */
    private final ProjectRepository projectRepository;
    private final SecurityService securityService;
    private final ProjectService self;

    /* ------------- Constructor ------------- */
    public ProjectService(
            ProjectRepository projectRepository,
            SecurityService securityService,
            @Lazy ProjectService self) {
        this.projectRepository = projectRepository;
        this.securityService = securityService;
        this.self = self;
    }

    /* ------------- CRUD Operations ------------- */
    /* --- Read Operations (GET) --- */
    public Iterable<Project> getUserProjects() {
        User user = securityService.getCurrentUser();

        return projectRepository.findByOwnerId(user.getId());
    }

    @PreAuthorize("@securityService.isResourceOwner(#id, 'project')")
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.PROJECT, id));
    }

    /* --- Create Operation (POST) --- */
    @Transactional
    public Project createProject(NewProjectDTO projectDTO) {
        User user = securityService.getCurrentUser();

        int num = projectRepository.findByOwnerId(user.getId()).size();
        if (num >= PROJECT_LIMIT) {
            throw new ProjectLimitReachedException();
        }

        Project newProject = new Project(projectDTO.getName(), user);
        return this.projectRepository.save(newProject);
    }

    /* --- Update Operation (PUT) --- */
    @Transactional
    public Project updateProject(Long id, UpdateProjectDTO projectDTO) {
        Project _project = self.getProjectById(id);

        _project.setName(projectDTO.getName());

        return projectRepository.save(_project);
    }

    /* --- Delete Operation (DELETE) --- */
    @Transactional
    public void deleteProjectById(Long id) {
        // Make sure project exists first
        self.getProjectById(id);
        projectRepository.deleteById(id);
    }
}
