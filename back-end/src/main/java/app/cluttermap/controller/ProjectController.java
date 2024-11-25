package app.cluttermap.controller;

import java.util.List;

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

import app.cluttermap.model.Item;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.model.dto.NewProjectDTO;
import app.cluttermap.model.dto.UpdateProjectDTO;
import app.cluttermap.service.ItemService;
import app.cluttermap.service.OrgUnitService;
import app.cluttermap.service.ProjectService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/projects")
public class ProjectController {
    /* ------------- Injected Dependencies ------------- */
    private final ProjectService projectService;
    private final OrgUnitService orgUnitService;
    private final ItemService itemService;

    /* ------------- Constructor ------------- */
    public ProjectController(
            ProjectService projectService,
            OrgUnitService orgUnitService,
            ItemService itemService) {
        this.projectService = projectService;
        this.orgUnitService = orgUnitService;
        this.itemService = itemService;
    }

    /* ------------- GET Operations ------------- */
    @GetMapping()
    public ResponseEntity<Iterable<Project>> getProjects() {
        return ResponseEntity.ok(projectService.getUserProjects());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityService.isResourceOwner(#id, 'project')")
    public ResponseEntity<Project> getOneProject(@PathVariable("id") Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @GetMapping("/{id}/rooms")
    @PreAuthorize("@securityService.isResourceOwner(#id, 'project')")
    public ResponseEntity<Iterable<Room>> getProjectRooms(@PathVariable("id") Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id).getRooms());
    }

    @GetMapping("/{id}/org-units")
    @PreAuthorize("@securityService.isResourceOwner(#id, 'project')")
    public ResponseEntity<Iterable<OrgUnit>> getProjectOrgUnits(@PathVariable("id") Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id).getOrgUnits());
    }

    @GetMapping("/{id}/items")
    @PreAuthorize("@securityService.isResourceOwner(#id, 'project')")
    public ResponseEntity<Iterable<Item>> getProjectItems(@PathVariable("id") Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id).getItems());
    }

    @GetMapping("/{projectId}/org-units/unassigned")
    @PreAuthorize("@securityService.isResourceOwner(#projectId, 'project')")
    public ResponseEntity<Iterable<OrgUnit>> getUnassignedOrgUnitsByProjectId(@PathVariable Long projectId) {
        Iterable<OrgUnit> unassignedOrgUnits = orgUnitService.getUnassignedOrgUnitsByProjectId(projectId);
        return ResponseEntity.ok(unassignedOrgUnits);
    }

    @GetMapping("/{projectId}/items/unassigned")
    @PreAuthorize("@securityService.isResourceOwner(#projectId, 'project')")
    public ResponseEntity<List<Item>> getUnassignedItemsByProjectId(@PathVariable Long projectId) {
        List<Item> unassignedItems = itemService.getUnassignedItemsByProjectId(projectId);
        return ResponseEntity.ok(unassignedItems);
    }

    /* ------------- POST Operations ------------- */
    @PostMapping()
    public ResponseEntity<Project> addOneProject(@Valid @RequestBody NewProjectDTO projectDTO) {
        return ResponseEntity.ok(projectService.createProject(projectDTO));
    }

    /* ------------- PUT Operations ------------- */
    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isResourceOwner(#id, 'project')")
    public ResponseEntity<Project> updateOneProject(@PathVariable("id") Long id,
            @Valid @RequestBody UpdateProjectDTO projectDTO) {
        return ResponseEntity.ok(projectService.updateProject(id, projectDTO));
    }

    /* ------------- DELETE Operations ------------- */
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isResourceOwner(#id, 'project')")
    public ResponseEntity<Void> deleteOneProject(@PathVariable("id") Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
