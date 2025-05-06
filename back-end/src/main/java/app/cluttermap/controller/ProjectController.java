package app.cluttermap.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
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
import app.cluttermap.model.dto.ItemDTO;
import app.cluttermap.model.dto.NewProjectDTO;
import app.cluttermap.model.dto.OrgUnitDTO;
import app.cluttermap.model.dto.ProjectDTO;
import app.cluttermap.model.dto.RoomDTO;
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
    public ResponseEntity<List<ProjectDTO>> getProjects() {
        List<ProjectDTO> projectDTOs = new ArrayList<>();
        for (Project project : projectService.getUserProjects()) {
            projectDTOs.add(new ProjectDTO(project));
        }
        return ResponseEntity.ok(projectDTOs);
    }

    @GetMapping("/ids")
    public ResponseEntity<List<Long>> getProjectIDs() {
        List<Long> projectIds = projectService.getUserProjects()
                .stream()
                .map(Project::getId)
                .collect(Collectors.toList());
        return ResponseEntity.ok(projectIds);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO> getOneProject(@PathVariable("id") Long id) {
        return ResponseEntity.ok(new ProjectDTO(projectService.getProjectById(id)));
    }

    @GetMapping("/{id}/rooms")
    public ResponseEntity<List<RoomDTO>> getProjectRooms(@PathVariable("id") Long id) {
        List<RoomDTO> roomDTOs = new ArrayList<>();
        for (Room room : projectService.getProjectById(id).getRooms()) {
            roomDTOs.add(new RoomDTO(room));
        }
        return ResponseEntity.ok(roomDTOs);
    }

    @GetMapping("/{id}/org-units")
    public ResponseEntity<List<OrgUnitDTO>> getProjectOrgUnits(@PathVariable("id") Long id) {
        List<OrgUnitDTO> orgUnitDTOs = new ArrayList<>();
        for (OrgUnit orgUnit : projectService.getProjectById(id).getOrgUnits()) {
            orgUnitDTOs.add(new OrgUnitDTO(orgUnit));
        }
        return ResponseEntity.ok(orgUnitDTOs);
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<List<ItemDTO>> getProjectItems(@PathVariable("id") Long id) {
        List<ItemDTO> itemDTOs = new ArrayList<>();
        for (Item item : projectService.getProjectById(id).getItems()) {
            itemDTOs.add(new ItemDTO(item));
        }
        return ResponseEntity.ok(itemDTOs);
    }

    @GetMapping("/{projectId}/org-units/unassigned")
    public ResponseEntity<List<OrgUnitDTO>> getUnassignedOrgUnitsByProjectId(@PathVariable Long projectId) {
        List<OrgUnitDTO> unassignedOrgUnitDTOs = new ArrayList<>();
        for (OrgUnit orgUnit : orgUnitService.getUnassignedOrgUnitsByProjectId(projectId)) {
            unassignedOrgUnitDTOs.add(new OrgUnitDTO(orgUnit));
        }
        return ResponseEntity.ok(unassignedOrgUnitDTOs);
    }

    @GetMapping("/{projectId}/items/unassigned")
    public ResponseEntity<List<ItemDTO>> getUnassignedItemsByProjectId(@PathVariable Long projectId) {
        List<ItemDTO> unassignedItemDTOs = new ArrayList<>();
        for (Item item : itemService.getUnassignedItemsByProjectId(projectId)) {
            unassignedItemDTOs.add(new ItemDTO(item));
        }
        return ResponseEntity.ok(unassignedItemDTOs);
    }

    /* ------------- POST Operations ------------- */
    @PostMapping()
    public ResponseEntity<ProjectDTO> addOneProject(@Valid @RequestBody NewProjectDTO projectDTO) {
        return ResponseEntity.ok(new ProjectDTO(projectService.createProject(projectDTO)));
    }

    /* ------------- PUT Operations ------------- */
    @PutMapping("/{id}")
    public ResponseEntity<ProjectDTO> updateOneProject(@PathVariable("id") Long id,
            @Valid @RequestBody UpdateProjectDTO projectDTO) {
        return ResponseEntity.ok(new ProjectDTO(projectService.updateProject(id, projectDTO)));
    }

    /* ------------- DELETE Operations ------------- */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOneProject(@PathVariable("id") Long id) {
        projectService.deleteProjectById(id);
        return ResponseEntity.noContent().build();
    }
}
