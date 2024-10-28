package app.cluttermap.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.google.api.client.util.Value;

import app.cluttermap.dto.NewProjectDTO;
import app.cluttermap.dto.UpdateProjectDTO;
import app.cluttermap.exception.project.ProjectLimitReachedException;
import app.cluttermap.exception.project.ProjectNotFoundException;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import app.cluttermap.repository.ProjectsRepository;
import jakarta.transaction.Transactional;

@Service
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = securityService.getUserFromAuthentication(authentication);

        int num = projectsRepository.findByOwner(user).size();
        if (num >= projectLimit) {
            throw new ProjectLimitReachedException();
        }

        Project newProject = new Project(projectDTO.getName(), user);
        return this.projectsRepository.save(newProject);
    }

    public Iterable<Project> getUserProjects() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = securityService.getUserFromAuthentication(authentication);

        return projectsRepository.findByOwner(user);
    }

    public Iterable<Room> getProjectRooms(Long id) {
        return getProjectById(id).getRooms();
    }

    public Project updateProject(Long id, UpdateProjectDTO projectDTO) {
        Project project = getProjectById(id);

        return projectsRepository.save(project);
    }

    public void deleteProject(Long id) {
        // Make sure project exists first
        getProjectById(id);
        projectsRepository.deleteById(id);
    }
}
