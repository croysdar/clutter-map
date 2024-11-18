package app.cluttermap.service;

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

    /* ------------- Constructor ------------- */
    public RoomService(
            RoomRepository roomRepository,
            SecurityService securityService,
            ProjectService projectService) {
        this.roomRepository = roomRepository;
        this.securityService = securityService;
        this.projectService = projectService;
    }

    /* ------------- CRUD Operations ------------- */
    /* --- Read Operations (GET) --- */
    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.ROOM, id));
    }

    public Iterable<Room> getUserRooms() {
        User user = securityService.getCurrentUser();

        return roomRepository.findByOwnerId(user.getId());
    }

    @Transactional
    public Room createRoom(NewRoomDTO roomDTO) {
        Project project = projectService.getProjectById(roomDTO.getProjectIdAsLong());

        Room newRoom = new Room(roomDTO.getName(), roomDTO.getDescription(), project);
        return roomRepository.save(newRoom);
    }

    @Transactional
    public Room updateRoom(Long id, UpdateRoomDTO roomDTO) {
        Room _room = getRoomById(id);
        _room.setName(roomDTO.getName());
        if (roomDTO.getDescription() != null) {
            _room.setDescription(roomDTO.getDescription());
        }

        return roomRepository.save(_room);
    }

    @Transactional
    public void deleteRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.ROOM, roomId));
        roomRepository.delete(room); // Ensures OrgUnits are unassigned, not deleted
    }
}
