package app.cluttermap.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import app.cluttermap.util.EventChangeType;
import app.cluttermap.util.ResourceType;
import jakarta.transaction.Transactional;

@Service("projectService")
public class ProjectService {
    /* ------------- Constants ------------- */
    private final int PROJECT_LIMIT = 3;

    /* ------------- Injected Dependencies ------------- */
    private final ProjectRepository projectRepository;
    private final SecurityService securityService;
    private final EventService eventService;
    private final ProjectService self;

    /* ------------- Constructor ------------- */
    public ProjectService(
            ProjectRepository projectRepository,
            SecurityService securityService,
            EventService eventService,
            @Lazy ProjectService self) {
        this.projectRepository = projectRepository;
        this.securityService = securityService;
        this.eventService = eventService;
        this.self = self;
    }

    /* ------------- CRUD Operations ------------- */
    /* --- Read Operations (GET) --- */
    public List<Project> getUserProjects() {
        User user = securityService.getCurrentUser();

        return projectRepository.findByOwnerId(user.getId());
    }

    @PreAuthorize("@securityService.isResourceOwner(#id, 'PROJECT')")
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
        Project project = projectRepository.save(
                newProject);

        eventService.logEvent(
                ResourceType.PROJECT, project.getId(),
                EventChangeType.CREATE, buildCreatePayload(project));

        return project;
    }

    /* --- Update Operation (PUT) --- */
    @Transactional
    public Project updateProject(Long id, UpdateProjectDTO projectDTO) {
        Project _project = self.getProjectById(id);
        Project oldProject = _project.copy();

        _project.setName(projectDTO.getName());

        Project updatedProject = projectRepository.save(_project);

        eventService.logEvent(
                ResourceType.PROJECT, id,
                EventChangeType.UPDATE, buildChangePayload(oldProject, updatedProject));

        return updatedProject;
    }

    /* --- Delete Operation (DELETE) --- */
    @Transactional
    public void deleteProjectById(Long id) {
        // Make sure project exists first
        self.getProjectById(id);

        projectRepository.deleteById(id);
    }

    private Map<String, Object> buildCreatePayload(Project project) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", project.getName());
        return payload;
    }

    private Map<String, Object> buildChangePayload(Project oldProject, Project newProject) {
        Map<String, Object> changes = new HashMap<>();

        if (!Objects.equals(oldProject.getName(), newProject.getName())) {
            changes.put("name", newProject.getName());
        }

        return changes;
    }
}
