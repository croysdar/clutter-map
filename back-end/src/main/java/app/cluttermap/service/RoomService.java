package app.cluttermap.service;

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
import app.cluttermap.util.ResourceType;
import jakarta.transaction.Transactional;

@Service("roomService")
public class RoomService {
    // TODO add limit of rooms per project

    /* ------------- Injected Dependencies ------------- */
    private final RoomRepository roomRepository;
    private final SecurityService securityService;
    private final ProjectService projectService;
    private final RoomService self;

    /* ------------- Constructor ------------- */
    public RoomService(
            RoomRepository roomRepository,
            SecurityService securityService,
            ProjectService projectService,
            @Lazy RoomService self) {
        this.roomRepository = roomRepository;
        this.securityService = securityService;
        this.projectService = projectService;
        this.self = self;
    }

    /* ------------- CRUD Operations ------------- */
    /* --- Read Operations (GET) --- */
    public Iterable<Room> getUserRooms() {
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
        return self.createRoomInProject(roomDTO, roomDTO.getProjectIdAsLong());
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
        _room.setName(roomDTO.getName());
        if (roomDTO.getDescription() != null) {
            _room.setDescription(roomDTO.getDescription());
        }

        return roomRepository.save(_room);
    }

    /* --- Delete Operation (DELETE) --- */
    @Transactional
    public void deleteRoomById(Long roomId) {
        Room room = self.getRoomById(roomId);
        roomRepository.delete(room); // Ensures OrgUnits are unassigned, not deleted
    }
}
