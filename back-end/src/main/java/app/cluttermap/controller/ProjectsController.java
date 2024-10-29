package app.cluttermap.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.cluttermap.dto.NewProjectDTO;
import app.cluttermap.dto.ProjectResponseDTO;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import app.cluttermap.repository.ProjectsRepository;
import app.cluttermap.service.SecurityService;

@RestController
@RequestMapping("/projects")
public class ProjectsController {
    @Autowired
    private final ProjectsRepository projectsRepository;

    private final SecurityService securityService;

    @Value("${project.limit}")
    private int projectLimit;

    public ProjectsController(ProjectsRepository projectsRepository, SecurityService securityService) {
        this.projectsRepository = projectsRepository;
        this.securityService = securityService;
    }

    @GetMapping()
    public Iterable<Project> getProjects(Authentication authentication) {
        User owner = securityService.getCurrentUser();
        return projectsRepository.findByOwner(owner);
    }

    @GetMapping("/{id}/rooms")
    @PreAuthorize("@securityService.isResourceOwner(authentication, #id, 'project')")
    public ResponseEntity<List<Room>> getProjectRooms(@PathVariable("id") Long id) {
        Optional<Project> projectData = projectsRepository.findById(id);

        if (projectData.isPresent()) {
            Project project = projectData.get();
            return new ResponseEntity<>(project.getRooms(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping()
    public ResponseEntity<ProjectResponseDTO> addOneProject(@RequestBody NewProjectDTO projectDTO,
            Authentication authentication) {
        if (projectDTO.getName() == null || projectDTO.getName().isEmpty()) {
            return new ResponseEntity<>(new ProjectResponseDTO("Project name is required"), HttpStatus.BAD_REQUEST);
        }

        try {
            User user = securityService.getCurrentUser();

            int num = projectsRepository.findByOwner(user).size();
            if (num >= projectLimit) {
                return new ResponseEntity<>(new ProjectResponseDTO("Maximum project limit reached."),
                        HttpStatus.BAD_REQUEST);
            }

            Project newProject = new Project(projectDTO.getName(), user);
            return new ResponseEntity<>(new ProjectResponseDTO(this.projectsRepository.save(newProject)),
                    HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(new ProjectResponseDTO(e.getMessage()), HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityService.isResourceOwner(authentication, #id, 'project')")
    public ResponseEntity<Project> getOneProject(@PathVariable("id") Long id) {
        Optional<Project> projectData = projectsRepository.findById(id);

        if (projectData.isPresent()) {
            return new ResponseEntity<>(projectData.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isResourceOwner(authentication, #id, 'project')")
    public ResponseEntity<Project> updateOneProject(@PathVariable("id") Long id, @RequestBody Project project) {
        /*
         * Takes project, returns a project.
         * Use to change project name.
         * Cannot use to change rooms within project.
         */
        Optional<Project> projectData = projectsRepository.findById(id);

        if (projectData.isPresent()) {
            Project _project = projectData.get();
            _project.setName(project.getName());

            return new ResponseEntity<>(projectsRepository.save(_project), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isResourceOwner(authentication, #id, 'project')")
    public ResponseEntity<Project> deleteOneProject(@PathVariable("id") Long id) {
        Optional<Project> projectData = projectsRepository.findById(id);

        if (projectData.isPresent()) {
            try {
                projectsRepository.deleteById(id);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
