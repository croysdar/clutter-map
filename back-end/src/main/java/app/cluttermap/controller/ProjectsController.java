package app.cluttermap.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.repository.ProjectsRepository;
import app.cluttermap.repository.RoomsRepository;

@RestController
@RequestMapping("/projects")
@CrossOrigin(origins = "http://localhost:3000")
public class ProjectsController {
    @Autowired
    private final ProjectsRepository projectsRepository;

    public ProjectsController(ProjectsRepository projectsRepository) {
        this.projectsRepository = projectsRepository;
    }

    @GetMapping()
    public Iterable<Project> getProjects() {
        return this.projectsRepository.findAll();
    }

    @PostMapping()
    public ResponseEntity<Project> addOneProject(@RequestBody Project project) {
        if (project.getName() == null || project.getName().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Project savedProject = this.projectsRepository.save(project);
        return new ResponseEntity<>(savedProject, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getOneProject(@PathVariable("id") Long id) {
        Optional<Project> projectData = projectsRepository.findById(id);

        if (projectData.isPresent()) {
            return new ResponseEntity<>(projectData.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Project> updateOneProject(@PathVariable("id") Long id, @RequestBody Project project) {
        Optional<Project> projectData = projectsRepository.findById(id);

        if (projectData.isPresent()) {
            Project _project = projectData.get();
            _project.setName(project.getName());
            _project.setRooms(project.getRooms());

            return new ResponseEntity<>(projectsRepository.save(_project), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{id}/rooms")
    public ResponseEntity<Project> addOneRoom(@PathVariable("id") Long id, @RequestBody Room room) {
        Optional<Project> projectData = projectsRepository.findById(id);
        if (projectData.isPresent()) {
            Project _project = projectData.get();
            Room newRoom = new Room("name", "description", _project);
            newRoom.setName(room.getName());
            newRoom.setDescription(room.getDescription());
            _project.addRoom(newRoom);

            return new ResponseEntity<>(projectsRepository.save(_project), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Project> deleteOneProject(@PathVariable("id") Long id) {
        Optional<Project> projectData = projectsRepository.findById(id);

        if (projectData.isPresent()) {
            try {
                projectsRepository.deleteById(id);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
