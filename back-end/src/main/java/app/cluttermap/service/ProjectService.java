package app.cluttermap.service;

import org.springframework.stereotype.Service;

import app.cluttermap.exception.project.ProjectLimitReachedException;
import app.cluttermap.exception.project.ProjectNotFoundException;
import app.cluttermap.model.Project;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.NewProjectDTO;
import app.cluttermap.model.dto.UpdateProjectDTO;
import app.cluttermap.repository.ProjectRepository;
import jakarta.transaction.Transactional;

@Service("projectService")
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final SecurityService securityService;

    private final int PROJECT_LIMIT = 3;

    public ProjectService(ProjectRepository projectRepository, SecurityService securityService) {
        this.projectRepository = projectRepository;
        this.securityService = securityService;
    }

    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException());
    }

    @Transactional
    public Project createProject(NewProjectDTO projectDTO) {
        User user = securityService.getCurrentUser();

        int num = projectRepository.findByOwner(user).size();
        if (num >= PROJECT_LIMIT) {
            throw new ProjectLimitReachedException();
        }

        Project newProject = new Project(projectDTO.getName(), user);
        return this.projectRepository.save(newProject);
    }

    public Iterable<Project> getUserProjects() {
        User user = securityService.getCurrentUser();

        return projectRepository.findByOwner(user);
    }

    @Transactional
    public Project updateProject(Long id, UpdateProjectDTO projectDTO) {
        Project _project = getProjectById(id);

        _project.setName(projectDTO.getName());

        return projectRepository.save(_project);
    }

    @Transactional
    public void deleteProject(Long id) {
        // Make sure project exists first
        getProjectById(id);
        projectRepository.deleteById(id);
    }
}
