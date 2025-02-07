package app.cluttermap.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
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
import app.cluttermap.util.EventActionType;
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
    private final EventService eventService;
    private final OrgUnitService self;

    /* ------------- Constructor ------------- */
    public OrgUnitService(
            RoomRepository roomRepository,
            OrgUnitRepository orgUnitRepository,
            ItemRepository itemRepository,
            SecurityService securityService,
            ProjectService projectService,
            RoomService roomService,
            EventService eventService,
            @Lazy OrgUnitService self) {
        this.roomRepository = roomRepository;
        this.orgUnitRepository = orgUnitRepository;
        this.securityService = securityService;
        this.projectService = projectService;
        this.roomService = roomService;
        this.eventService = eventService;
        this.self = self;
    }

    /* ------------- CRUD Operations ------------- */
    /* --- Read Operations (GET) --- */
    public List<OrgUnit> getUserOrgUnits() {
        User user = securityService.getCurrentUser();

        return orgUnitRepository.findByOwnerId(user.getId());
    }

    @PreAuthorize("@securityService.isResourceOwner(#id, 'ORGANIZATIONAL_UNIT')")
    public OrgUnit getOrgUnitById(Long id) {
        return orgUnitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.ORGANIZATIONAL_UNIT, id));
    }

    /* --- Create Operation (POST) --- */
    @Transactional
    public OrgUnit createOrgUnit(NewOrgUnitDTO orgUnitDTO) {
        OrgUnit orgUnit;
        if (orgUnitDTO.getRoomId() == null) {
            if (orgUnitDTO.getProjectId() == null) {
                throw new IllegalArgumentException("Either RoomId or ProjectId must be provided.");
            }
            orgUnit = self.createUnassignedOrgUnit(orgUnitDTO, orgUnitDTO.getProjectIdAsLong());
        } else {
            orgUnit = self.createOrgUnitInRoom(orgUnitDTO, orgUnitDTO.getRoomIdAsLong());
        }

        eventService.logEvent(
                ResourceType.ORGANIZATIONAL_UNIT, orgUnit.getId(),
                EventActionType.CREATE, buildCreatePayload(orgUnit));
        return orgUnit;
    }

    @PreAuthorize("@securityService.isResourceOwner(#roomId, 'ROOM')")
    public OrgUnit createOrgUnitInRoom(NewOrgUnitDTO orgUnitDTO, Long roomId) {
        Room room = roomService.getRoomById(roomId);

        OrgUnit newOrgUnit = new OrgUnit(
                orgUnitDTO.getName(),
                orgUnitDTO.getDescription(),
                room);
        return orgUnitRepository.save(newOrgUnit);
    }

    @PreAuthorize("@securityService.isResourceOwner(#projectId, 'PROJECT')")
    public OrgUnit createUnassignedOrgUnit(NewOrgUnitDTO orgUnitDTO, Long projectId) {
        Project project = projectService.getProjectById(projectId);
        OrgUnit newOrgUnit = new OrgUnit(
                orgUnitDTO.getName(),
                orgUnitDTO.getDescription(),
                project);
        return orgUnitRepository.save(newOrgUnit);
    }

    @PreAuthorize("@securityService.isResourceOwner(#projectId, 'PROJECT')")
    public List<OrgUnit> getUnassignedOrgUnitsByProjectId(Long projectId) {
        return orgUnitRepository.findUnassignedOrgUnitsByProjectId(projectId);
    }

    /* --- Update Operation (PUT) --- */
    @Transactional
    public OrgUnit updateOrgUnit(Long id, UpdateOrgUnitDTO orgUnitDTO) {
        OrgUnit _orgUnit = self.getOrgUnitById(id);
        OrgUnit oldOrgUnit = _orgUnit.copy();

        _orgUnit.setName(orgUnitDTO.getName());
        if (orgUnitDTO.getDescription() != null) {
            _orgUnit.setDescription(orgUnitDTO.getDescription());
        }

        OrgUnit updatedOrgUnit = orgUnitRepository.save(_orgUnit);

        eventService.logEvent(
                ResourceType.ORGANIZATIONAL_UNIT, id,
                EventActionType.UPDATE, buildChangePayload(oldOrgUnit, updatedOrgUnit));

        return updatedOrgUnit;
    }

    /* --- Delete Operation (DELETE) --- */
    @Transactional
    public void deleteOrgUnitById(Long id) {
        OrgUnit orgUnit = self.getOrgUnitById(id);
        orgUnitRepository.delete(orgUnit); // Ensures Items are unassigned, not deleted

        eventService.logEvent(
                ResourceType.ORGANIZATIONAL_UNIT, id,
                EventActionType.DELETE, null);
    }

    /* ------------- Complex Operations ------------- */
    @Transactional
    public List<OrgUnit> assignOrgUnitsToRoom(List<Long> orgUnitIds, Long targetRoomId) {
        Room targetRoom = roomService.getRoomById(targetRoomId);

        List<OrgUnit> updatedOrgUnits = new ArrayList<>();

        for (Long orgUnitId : orgUnitIds) {
            OrgUnit orgUnit = self.getOrgUnitById(orgUnitId);
            Long previousRoomId = Optional.ofNullable(orgUnit.getRoom()).map(Room::getId).orElse(null);

            validateSameProject(orgUnit, targetRoom);

            // Check if org unit is already assigned to an room
            if (orgUnit.getRoom() != null) {
                unassignOrgUnitFromRoom(orgUnit, orgUnit.getRoom());
            }
            assignOrgUnitToRoom(orgUnit, targetRoom);
            eventService.logMoveEvent(
                    ResourceType.ORGANIZATIONAL_UNIT, orgUnit.getId(),
                    ResourceType.ROOM, previousRoomId, targetRoomId);
            updatedOrgUnits.add(orgUnit);
        }
        return updatedOrgUnits;
    }

    @Transactional
    public List<OrgUnit> unassignOrgUnits(List<Long> orgUnitIds) {
        List<OrgUnit> updatedOrgUnits = new ArrayList<>();
        for (Long orgUnitId : orgUnitIds) {
            OrgUnit orgUnit = self.getOrgUnitById(orgUnitId);
            Long previousRoomId = Optional.ofNullable(orgUnit.getRoom()).map(Room::getId).orElse(null);
            if (orgUnit.getRoom() != null) {
                unassignOrgUnitFromRoom(orgUnit, orgUnit.getRoom());
                eventService.logMoveEvent(
                        ResourceType.ORGANIZATIONAL_UNIT, orgUnit.getId(),
                        ResourceType.ROOM, previousRoomId, null);
            }
            updatedOrgUnits.add(orgUnit);
        }
        return updatedOrgUnits;
    }

    /* ------------- Ownership and Security Checks ------------- */
    public void checkOwnershipForOrgUnits(List<Long> orgUnitIds) {
        for (Long id : orgUnitIds) {
            if (!securityService.isResourceOwner(id, ResourceType.ORGANIZATIONAL_UNIT)) {
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

    private Map<String, Object> buildCreatePayload(OrgUnit orgUnit) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", orgUnit.getName());
        payload.put("description", orgUnit.getDescription());
        if (orgUnit.getRoom() != null) {
            payload.put("roomId", orgUnit.getRoom().getId());
        }
        return payload;
    }

    private Map<String, Object> buildChangePayload(OrgUnit oldOrgUnit, OrgUnit newOrgUnit) {
        Map<String, Object> changes = new HashMap<>();

        if (!Objects.equals(oldOrgUnit.getName(), newOrgUnit.getName())) {
            changes.put("name", newOrgUnit.getName());
        }
        if (!Objects.equals(oldOrgUnit.getDescription(), newOrgUnit.getDescription())) {
            changes.put("description", newOrgUnit.getDescription());
        }

        return changes;
    }
}
