package app.cluttermap.service;

import org.springframework.stereotype.Service;

import app.cluttermap.exception.org_unit.OrgUnitNotFoundException;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.NewOrgUnitDTO;
import app.cluttermap.model.dto.UpdateOrgUnitDTO;
import app.cluttermap.repository.OrgUnitsRepository;
import jakarta.transaction.Transactional;

@Service("orgUnitService")
public class OrgUnitService {
    private final OrgUnitsRepository orgUnitsRepository;
    private final SecurityService securityService;
    private final RoomService roomService;

    public OrgUnitService(OrgUnitsRepository orgUnitsRepository, SecurityService securityService,
            RoomService roomService) {
        this.orgUnitsRepository = orgUnitsRepository;
        this.securityService = securityService;
        this.roomService = roomService;
    }

    public OrgUnit getOrgUnitById(Long id) {
        return orgUnitsRepository.findById(id)
                .orElseThrow(() -> new OrgUnitNotFoundException());
    }

    @Transactional
    public OrgUnit createOrgUnit(NewOrgUnitDTO orgUnitDTO) {
        Room room = roomService.getRoomById(orgUnitDTO.getRoomIdAsLong());

        OrgUnit newOrgUnit = new OrgUnit(
                orgUnitDTO.getName(),
                orgUnitDTO.getDescription(),
                room);
        return orgUnitsRepository.save(newOrgUnit);
    }

    public Iterable<OrgUnit> getUserOrgUnits() {
        User user = securityService.getCurrentUser();

        return orgUnitsRepository.findOrgUnitsByUserId(user.getId());
    }

    @Transactional
    public OrgUnit updateOrgUnit(Long id, UpdateOrgUnitDTO orgUnitDTO) {
        OrgUnit _orgUnit = getOrgUnitById(id);
        _orgUnit.setName(orgUnitDTO.getName());
        if (orgUnitDTO.getDescription() != null) {
            _orgUnit.setDescription(orgUnitDTO.getDescription());
        }

        return orgUnitsRepository.save(_orgUnit);
    }

    @Transactional
    public void deleteOrgUnit(Long id) {
        // Make sure org unit exists first
        getOrgUnitById(id);
        orgUnitsRepository.deleteById(id);
    }
}
