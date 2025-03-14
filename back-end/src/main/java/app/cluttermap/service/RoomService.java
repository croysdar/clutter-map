package app.cluttermap.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import app.cluttermap.exception.ResourceNotFoundException;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.NewRoomDTO;
import app.cluttermap.model.dto.UpdateRoomDTO;
import app.cluttermap.repository.RoomRepository;
import app.cluttermap.util.EventChangeType;
import app.cluttermap.util.ResourceType;
import jakarta.transaction.Transactional;

@Service("roomService")
public class RoomService {
    // TODO add limit of rooms per project

    /* ------------- Injected Dependencies ------------- */
    private final RoomRepository roomRepository;
    private final SecurityService securityService;
    private final ProjectService projectService;
    private final EventService eventService;
    private final RoomService self;

    /* ------------- Constructor ------------- */
    public RoomService(
            RoomRepository roomRepository,
            SecurityService securityService,
            ProjectService projectService,
            EventService eventService,
            @Lazy RoomService self) {
        this.roomRepository = roomRepository;
        this.securityService = securityService;
        this.projectService = projectService;
        this.eventService = eventService;
        this.self = self;
    }

    /* ------------- CRUD Operations ------------- */
    /* --- Read Operations (GET) --- */
    public List<Room> getUserRooms() {
        User user = securityService.getCurrentUser();

        return roomRepository.findByOwnerId(user.getId());
    }

    @PreAuthorize("@securityService.isResourceOwner(#id, 'ROOM')")
    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.ROOM, id));
    }

    /* --- Create Operation (POST) --- */
    @Transactional
    public Room createRoom(NewRoomDTO roomDTO) {
        Room room = self.createRoomInProject(roomDTO, roomDTO.getProjectIdAsLong());
        long id = room.getId();

        eventService.logEvent(
                ResourceType.ROOM, id,
                EventChangeType.CREATE, buildCreatePayload(room));
        
        Map<String, Object> addChildDetails = new HashMap<>();
        addChildDetails.put("childId", id);
        addChildDetails.put("childType", ResourceType.ROOM);
        eventService.logEvent(
            ResourceType.PROJECT, room.getProject().getId(),
            EventChangeType.ADD_CHILD, addChildDetails
        );

        return room;
    }

    @PreAuthorize("@securityService.isResourceOwner(#projectId, 'PROJECT')")
    public Room createRoomInProject(NewRoomDTO roomDTO, Long projectId) {
        Project project = projectService.getProjectById(roomDTO.getProjectIdAsLong());

        Room newRoom = new Room(roomDTO.getName(), roomDTO.getDescription(), project);
        return roomRepository.save(newRoom);
    }

    /* --- Update Operation (PUT) --- */
    @Transactional
    public Room updateRoom(Long id, UpdateRoomDTO roomDTO) {
        Room _room = self.getRoomById(id);
        Room oldRoom = _room.copy();

        _room.setName(roomDTO.getName());
        if (roomDTO.getDescription() != null) {
            _room.setDescription(roomDTO.getDescription());
        }

        Room updatedRoom = roomRepository.save(_room);

        eventService.logEvent(
                ResourceType.ROOM, id,
                EventChangeType.UPDATE, buildChangePayload(oldRoom, updatedRoom));

        return updatedRoom;
    }

    /* --- Delete Operation (DELETE) --- */
    @Transactional
    public void deleteRoomById(Long id) {
        Room room = self.getRoomById(id);

        eventService.logEvent(
                ResourceType.ROOM, id,
                EventChangeType.DELETE, null);
        
        Map<String, Object> removeChildDetails = new HashMap<>();
        removeChildDetails.put("childId", id);
        removeChildDetails.put("childType", ResourceType.ROOM);
        eventService.logEvent(
            ResourceType.PROJECT, room.getProject().getId(),
            EventChangeType.REMOVE_CHILD, removeChildDetails
        );

        roomRepository.delete(room); // Ensures OrgUnits are unassigned, not deleted
    }

    private Map<String, Object> buildCreatePayload(Room room) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("projectId", room.getProject().getId());

        payload.put("name", room.getName());
        payload.put("description", room.getDescription());
        return payload;
    }

    private Map<String, Object> buildChangePayload(Room oldRoom, Room newRoom) {
        Map<String, Object> changes = new HashMap<>();

        if (!Objects.equals(oldRoom.getName(), newRoom.getName())) {
            changes.put("name", newRoom.getName());
        }
        if (!Objects.equals(oldRoom.getDescription(), newRoom.getDescription())) {
            changes.put("description", newRoom.getDescription());
        }

        return changes;
    }
}
