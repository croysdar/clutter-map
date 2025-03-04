package app.cluttermap.service;

import org.springframework.stereotype.Service;

import app.cluttermap.exception.ResourceNotFoundException;
import app.cluttermap.model.Item;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.repository.ItemRepository;
import app.cluttermap.repository.OrgUnitRepository;
import app.cluttermap.repository.ProjectRepository;
import app.cluttermap.repository.RoomRepository;
import app.cluttermap.util.ResourceType;

@Service
public class EntityResolutionService {
    private final ProjectRepository projectRepository;
    private final RoomRepository roomRepository;
    private final OrgUnitRepository orgUnitRepository;
    private final ItemRepository itemRepository;

    public EntityResolutionService(
            ProjectRepository projectRepository,
            RoomRepository roomRepository,
            OrgUnitRepository orgUnitRepository,
            ItemRepository itemRepository) {
        this.projectRepository = projectRepository;
        this.roomRepository = roomRepository;
        this.orgUnitRepository = orgUnitRepository;
        this.itemRepository = itemRepository;
    }

    public Project resolveProject(ResourceType resourceType, Long resourceId) {

        switch (resourceType) {
            case PROJECT:
                Project project = projectRepository.findById(resourceId)
                        .orElseThrow(() -> new ResourceNotFoundException(ResourceType.PROJECT, resourceId));
                return project;

            case ROOM:
                Room room = roomRepository.findById(resourceId)
                        .orElseThrow(() -> new ResourceNotFoundException(ResourceType.ROOM, resourceId));
                return room.getProject();

            case ORGANIZATIONAL_UNIT:
                OrgUnit orgUnit = orgUnitRepository.findById(resourceId)
                        .orElseThrow(() -> new ResourceNotFoundException(ResourceType.ORGANIZATIONAL_UNIT, resourceId));

                return orgUnit.getProject();

            case ITEM:
                Item item = itemRepository.findById(resourceId)
                        .orElseThrow(() -> new ResourceNotFoundException(ResourceType.ITEM, resourceId));

                return item.getProject();
            default:
                throw new IllegalArgumentException("Unknown entity type: " + resourceType);
        }
    }
}
