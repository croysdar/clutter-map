package app.cluttermap.service;

import org.springframework.stereotype.Service;

import app.cluttermap.exception.item.ItemNotFoundException;
import app.cluttermap.exception.org_unit.OrgUnitNotFoundException;
import app.cluttermap.exception.room.RoomNotFoundException;
import app.cluttermap.model.Item;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.NewOrgUnitDTO;
import app.cluttermap.model.dto.UpdateOrgUnitDTO;
import app.cluttermap.repository.ItemRepository;
import app.cluttermap.repository.OrgUnitRepository;
import app.cluttermap.repository.RoomRepository;
import jakarta.transaction.Transactional;

@Service("orgUnitService")
public class OrgUnitService {
    private final RoomRepository roomRepository;
    private final OrgUnitRepository orgUnitRepository;
    private final ItemRepository itemRepository;
    private final SecurityService securityService;
    private final RoomService roomService;

    public OrgUnitService(
            RoomRepository roomRepository,
            OrgUnitRepository orgUnitRepository,
            ItemRepository itemRepository,
            SecurityService securityService,
            RoomService roomService) {
        this.roomRepository = roomRepository;
        this.orgUnitRepository = orgUnitRepository;
        this.itemRepository = itemRepository;
        this.securityService = securityService;
        this.roomService = roomService;
    }

    public OrgUnit getOrgUnitById(Long id) {
        return orgUnitRepository.findById(id)
                .orElseThrow(() -> new OrgUnitNotFoundException());
    }

    // TODO add get unassigned org units

    @Transactional
    public OrgUnit createOrgUnit(NewOrgUnitDTO orgUnitDTO) {
        Room room = roomService.getRoomById(orgUnitDTO.getRoomIdAsLong());

        OrgUnit newOrgUnit = new OrgUnit(
                orgUnitDTO.getName(),
                orgUnitDTO.getDescription(),
                room);
        return orgUnitRepository.save(newOrgUnit);
    }

    public Iterable<OrgUnit> getUserOrgUnits() {
        User user = securityService.getCurrentUser();

        return orgUnitRepository.findByOwnerId(user.getId());
    }

    @Transactional
    public OrgUnit updateOrgUnit(Long id, UpdateOrgUnitDTO orgUnitDTO) {
        OrgUnit _orgUnit = getOrgUnitById(id);
        _orgUnit.setName(orgUnitDTO.getName());
        if (orgUnitDTO.getDescription() != null) {
            _orgUnit.setDescription(orgUnitDTO.getDescription());
        }

        return orgUnitRepository.save(_orgUnit);
    }

    public OrgUnit addItemToOrgUnit(Long orgUnitId, Long itemId) {
        OrgUnit orgUnit = orgUnitRepository.findById(orgUnitId)
                .orElseThrow(() -> new OrgUnitNotFoundException());

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException());

        // Ensure that item and orgUnit are in the same Project
        if (!item.getProject().equals(orgUnit.getProject())) {
            throw new IllegalArgumentException("Cannot add item to a different project's org unit");
        }

        orgUnit.addItem(item); // Manages both sides of the relationship
        return orgUnitRepository.save(orgUnit);
    }

    public OrgUnit removeItemFromOrgUnit(Long orgUnitId, Long itemId) {
        OrgUnit orgUnit = orgUnitRepository.findById(orgUnitId)
                .orElseThrow(() -> new OrgUnitNotFoundException());

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException());

        orgUnit.removeItem(item); // Manages both sides of the relationship
        return orgUnitRepository.save(orgUnit);
    }

    @Transactional
    public OrgUnit moveOrgUnitBetweenRooms(Long orgUnitId, Long targetRoomId) {
        OrgUnit orgUnit = orgUnitRepository.findById(orgUnitId)
                .orElseThrow(() -> new OrgUnitNotFoundException());

        Room currentRoom = orgUnit.getRoom();
        Room targetRoom = roomRepository.findById(targetRoomId)
                .orElseThrow(() -> new RoomNotFoundException());

        // Ensure that current and target Rooms are in the same Project
        if (currentRoom != null && !currentRoom.getProject().equals(targetRoom.getProject())) {
            throw new IllegalArgumentException("Cannot move org unit to a different project's Room");
        }

        // Remove from the current room (if any) and add to the new room
        if (orgUnit.getRoom() != null) {
            orgUnit.getRoom().removeOrgUnit(orgUnit);
        }
        targetRoom.addOrgUnit(orgUnit);

        roomRepository.save(targetRoom);

        return orgUnit;
    }

    @Transactional
    public void deleteOrgUnit(Long orgUnitId) {
        OrgUnit orgUnit = orgUnitRepository.findById(orgUnitId)
                .orElseThrow(() -> new OrgUnitNotFoundException());
        orgUnitRepository.delete(orgUnit); // Ensures Items are unassigned, not deleted
    }
}
