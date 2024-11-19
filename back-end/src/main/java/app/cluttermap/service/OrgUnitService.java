package app.cluttermap.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import app.cluttermap.exception.ResourceNotFoundException;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.NewOrgUnitDTO;
import app.cluttermap.model.dto.UpdateOrgUnitDTO;
import app.cluttermap.repository.ItemRepository;
import app.cluttermap.repository.OrgUnitRepository;
import app.cluttermap.repository.RoomRepository;
import app.cluttermap.util.ResourceType;
import jakarta.transaction.Transactional;

@Service("orgUnitService")
public class OrgUnitService {
    /* ------------- Constants ------------- */
    public static final String PROJECT_MISMATCH_ERROR = "Cannot move organization unit to a different project's room.";
    public static final String ACCESS_DENIED_STRING = "You do not have permission to access org unit with ID: %d";

    /* ------------- Injected Dependencies ------------- */
    private final RoomRepository roomRepository;
    private final OrgUnitRepository orgUnitRepository;
    private final SecurityService securityService;
    private final ProjectService projectService;
    private final RoomService roomService;

    /* ------------- Constructor ------------- */
    public OrgUnitService(
            RoomRepository roomRepository,
            OrgUnitRepository orgUnitRepository,
            ItemRepository itemRepository,
            SecurityService securityService,
            ProjectService projectService,
            RoomService roomService) {
        this.roomRepository = roomRepository;
        this.orgUnitRepository = orgUnitRepository;
        this.securityService = securityService;
        this.projectService = projectService;
        this.roomService = roomService;
    }

    /* ------------- CRUD Operations ------------- */
    /* --- Read Operations (GET) --- */
    public Iterable<OrgUnit> getUserOrgUnits() {
        User user = securityService.getCurrentUser();

        return orgUnitRepository.findByOwnerId(user.getId());
    }

    public OrgUnit getOrgUnitById(Long id) {
        return orgUnitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.ORGANIZATIONAL_UNIT, id));
    }

    /* --- Create Operation (POST) --- */
    @Transactional
    public OrgUnit createOrgUnit(NewOrgUnitDTO orgUnitDTO) {
        if (orgUnitDTO.getRoomId() == null) {
            // Check if the item is being created as unassigned to a room
            Project project = projectService.getProjectById(orgUnitDTO.getProjectIdAsLong());
            OrgUnit newOrgUnit = new OrgUnit(
                    orgUnitDTO.getName(),
                    orgUnitDTO.getDescription(),
                    project);
            return orgUnitRepository.save(newOrgUnit);

        }
        Room room = roomService.getRoomById(orgUnitDTO.getRoomIdAsLong());

        OrgUnit newOrgUnit = new OrgUnit(
                orgUnitDTO.getName(),
                orgUnitDTO.getDescription(),
                room);
        return orgUnitRepository.save(newOrgUnit);
    }

    public Iterable<OrgUnit> getUnassignedOrgUnitsByProjectId(Long projectId) {
        return orgUnitRepository.findUnassignedOrgUnitsByProjectId(projectId);
    }

    /* --- Update Operation (PUT) --- */
    @Transactional
    public OrgUnit updateOrgUnit(Long id, UpdateOrgUnitDTO orgUnitDTO) {
        OrgUnit _orgUnit = getOrgUnitById(id);
        _orgUnit.setName(orgUnitDTO.getName());
        if (orgUnitDTO.getDescription() != null) {
            _orgUnit.setDescription(orgUnitDTO.getDescription());
        }

        return orgUnitRepository.save(_orgUnit);
    }

    /* --- Delete Operation (DELETE) --- */
    @Transactional
    public void deleteOrgUnit(Long orgUnitId) {
        OrgUnit orgUnit = orgUnitRepository.findById(orgUnitId)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.ORGANIZATIONAL_UNIT, orgUnitId));
        orgUnitRepository.delete(orgUnit); // Ensures Items are unassigned, not deleted
    }

    /* ------------- Complex Operations ------------- */
    @Transactional
    public Iterable<OrgUnit> assignOrgUnitsToRoom(List<Long> orgUnitIds, Long targetRoomId) {
        Room targetRoom = roomService.getRoomById(targetRoomId);

        List<OrgUnit> updatedOrgUnits = new ArrayList<>();

        // TODO allow partial success

        for (Long orgUnitId : orgUnitIds) {
            OrgUnit orgUnit = getOrgUnitById(orgUnitId);

            validateSameProject(orgUnit, targetRoom);

            // Check if org unit is already assigned to an room
            if (orgUnit.getRoom() != null) {
                unassignOrgUnitFromRoom(orgUnit, orgUnit.getRoom());
            }
            assignOrgUnitToRoom(orgUnit, targetRoom);
            updatedOrgUnits.add(orgUnit);
        }
        return updatedOrgUnits;
    }

    @Transactional
    public Iterable<OrgUnit> unassignOrgUnits(List<Long> orgUnitIds) {
        List<OrgUnit> updatedOrgUnits = new ArrayList<>();
        // TODO Allow partial success
        for (Long orgUnitId : orgUnitIds) {
            OrgUnit orgUnit = getOrgUnitById(orgUnitId);
            if (orgUnit.getRoom() != null) {
                unassignOrgUnitFromRoom(orgUnit, orgUnit.getRoom());
            }
            updatedOrgUnits.add(orgUnit);
        }
        return updatedOrgUnits;
    }

    /* ------------- Ownership and Security Checks ------------- */
    public void checkOwnershipForOrgUnits(List<Long> orgUnitIds) {
        for (Long id : orgUnitIds) {
            if (!securityService.isResourceOwner(id, "org-unit")) {
                throw new AccessDeniedException(String.format(ACCESS_DENIED_STRING, id));
            }
        }
    }

    /* ------------- Private Helper Methods ------------- */
    private void validateSameProject(OrgUnit orgUnit, Room targetRoom) {
        if (targetRoom == null || targetRoom.getProject() == null) {
            throw new IllegalArgumentException("Target Room or its Project is null");
        }
        if (orgUnit == null || orgUnit.getProject() == null) {
            throw new IllegalArgumentException("Org Unit or its Project is null");
        }
        if (!orgUnit.getProject().equals(targetRoom.getProject())) {
            throw new IllegalArgumentException(PROJECT_MISMATCH_ERROR);
        }
    }

    private Room assignOrgUnitToRoom(OrgUnit orgUnit, Room room) {
        room.addOrgUnit(orgUnit); // Manages both sides of the relationship
        return roomRepository.save(room);
    }

    private Room unassignOrgUnitFromRoom(OrgUnit orgUnit, Room room) {
        room.removeOrgUnit(orgUnit); // Manages both sides of the relationship
        return roomRepository.save(room);
    }
}
