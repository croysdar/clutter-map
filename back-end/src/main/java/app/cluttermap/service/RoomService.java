package app.cluttermap.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.cluttermap.exception.room.RoomNotFoundException;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.NewRoomDTO;
import app.cluttermap.model.dto.UpdateRoomDTO;
import app.cluttermap.repository.RoomsRepository;
import jakarta.transaction.Transactional;

@Service("roomService")
public class RoomService {
    @Autowired
    private final RoomsRepository roomsRepository;

    @Autowired
    private final SecurityService securityService;

    @Autowired ProjectService projectService;

    // TODO add limit of rooms per project

    public RoomService(RoomsRepository roomsRepository, SecurityService securityService,
            ProjectService projectService) {
        this.roomsRepository = roomsRepository;
        this.securityService = securityService;
        this.projectService = projectService;
    }

    public Room getRoomById(Long id) {
        return roomsRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException());
    }

    @Transactional
    public Room createRoom(NewRoomDTO roomDTO) {
        Project project = projectService.getProjectById(roomDTO.getProjectIdAsLong());

        Room newRoom = new Room(roomDTO.getName(), roomDTO.getDescription(), project);
        return roomsRepository.save(newRoom);
    }

    public Iterable<Room> getUserRooms() {
        User user = securityService.getCurrentUser();

        return roomsRepository.findRoomsByProjectOwnerId(user.getId());
    }

    @Transactional
    public Room updateRoom(Long id, UpdateRoomDTO roomDTO) {
        Room _room = getRoomById(id);
        _room.setName(roomDTO.getName());
        _room.setDescription(roomDTO.getDescription());

        return roomsRepository.save(_room);
    }

    @Transactional
    public void deleteRoom(Long id) {
        // Make sure room exists first
        getRoomById(id);
        roomsRepository.deleteById(id);
    }
}
