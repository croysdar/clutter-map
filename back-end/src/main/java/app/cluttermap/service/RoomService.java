package app.cluttermap.service;

import org.springframework.stereotype.Service;

import app.cluttermap.exception.room.RoomNotFoundException;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.NewRoomDTO;
import app.cluttermap.model.dto.UpdateRoomDTO;
import app.cluttermap.repository.RoomRepository;
import jakarta.transaction.Transactional;

@Service("roomService")
public class RoomService {
    private final RoomRepository roomRepository;
    private final SecurityService securityService;
    private final ProjectService projectService;

    // TODO add limit of rooms per project

    public RoomService(RoomRepository roomRepository, SecurityService securityService,
            ProjectService projectService) {
        this.roomRepository = roomRepository;
        this.securityService = securityService;
        this.projectService = projectService;
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException());
    }

    @Transactional
    public Room createRoom(NewRoomDTO roomDTO) {
        Project project = projectService.getProjectById(roomDTO.getProjectIdAsLong());

        Room newRoom = new Room(roomDTO.getName(), roomDTO.getDescription(), project);
        return roomRepository.save(newRoom);
    }

    public Iterable<Room> getUserRooms() {
        User user = securityService.getCurrentUser();

        return roomRepository.findByOwnerId(user.getId());
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
    public void deleteRoom(Long id) {
        // Make sure room exists first
        getRoomById(id);
        roomRepository.deleteById(id);
    }
}
