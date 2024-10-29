package app.cluttermap.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.cluttermap.dto.NewProjectDTO;
import app.cluttermap.dto.UpdateProjectDTO;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.service.ProjectService;

@RestController
@RequestMapping("/projects")
public class ProjectsController {
    @Autowired
    private final ProjectService projectService;

    @Value("${project.limit}")
    private int projectLimit;

    public ProjectsController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping()
    public ResponseEntity<Iterable<Project>> getProjects() {
        return new ResponseEntity<>(projectService.getUserProjects(), HttpStatus.OK);
    }

    @GetMapping("/{id}/rooms")
    @PreAuthorize("@securityService.isResourceOwner(#id, 'project')")
    public ResponseEntity<Iterable<Room>> getProjectRooms(@PathVariable("id") Long id) {
        return new ResponseEntity<>(projectService.getProjectById(id).getRooms(), HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<Project> addOneProject(@RequestBody NewProjectDTO projectDTO) {
        return new ResponseEntity<>(projectService.createProject(projectDTO), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityService.isResourceOwner(#id, 'project')")
    public ResponseEntity<Project> getOneProject(@PathVariable("id") Long id) {
        return new ResponseEntity<>(projectService.getProjectById(id), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isResourceOwner(#id, 'project')")
    public ResponseEntity<Project> updateOneProject(@PathVariable("id") Long id,
            @RequestBody UpdateProjectDTO projectDTO) {
        /*
         * Use to change project name.
         * Cannot use to change rooms within project.
         */
        return new ResponseEntity<>(projectService.updateProject(id, projectDTO), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isResourceOwner(#id, 'project')")
    public ResponseEntity<Void> deleteOneProject(@PathVariable("id") Long id) {
        projectService.deleteProject(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
