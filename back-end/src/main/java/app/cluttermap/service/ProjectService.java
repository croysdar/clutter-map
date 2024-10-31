package app.cluttermap.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.api.client.util.Value;

import app.cluttermap.exception.project.ProjectLimitReachedException;
import app.cluttermap.exception.project.ProjectNotFoundException;
import app.cluttermap.model.Project;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.NewProjectDTO;
import app.cluttermap.model.dto.UpdateProjectDTO;
import app.cluttermap.repository.ProjectsRepository;
import jakarta.transaction.Transactional;

@Service("projectService")
public class ProjectService {
    @Autowired
    private final ProjectsRepository projectsRepository;

    @Autowired
    private final SecurityService securityService;

    @Value("${project.limit}")
    private int projectLimit;

    public ProjectService(ProjectsRepository projectsRepository, SecurityService securityService) {
        this.projectsRepository = projectsRepository;
        this.securityService = securityService;
    }

    public Project getProjectById(Long id) {
        return projectsRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException());
    }

    @Transactional
    public Project createProject(NewProjectDTO projectDTO) {
        User user = securityService.getCurrentUser();

        int num = projectsRepository.findByOwner(user).size();
        if (num >= projectLimit) {
            throw new ProjectLimitReachedException();
        }

        Project newProject = new Project(projectDTO.getName(), user);
        return this.projectsRepository.save(newProject);
    }

    public Iterable<Project> getUserProjects() {
        User user = securityService.getCurrentUser();

        return projectsRepository.findByOwner(user);
    }

    @Transactional
    public Project updateProject(Long id, UpdateProjectDTO projectDTO) {
        Project _project = getProjectById(id);

        _project.setName(projectDTO.getName());

        return projectsRepository.save(_project);
    }

    @Transactional
    public void deleteProject(Long id) {
        // Make sure project exists first
        getProjectById(id);
        projectsRepository.deleteById(id);
    }
}
