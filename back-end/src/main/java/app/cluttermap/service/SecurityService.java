package app.cluttermap.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import app.cluttermap.exception.ResourceNotFoundException;
import app.cluttermap.exception.auth.InvalidAuthenticationException;
import app.cluttermap.exception.auth.UserNotFoundException;
import app.cluttermap.model.Item;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import app.cluttermap.repository.ItemRepository;
import app.cluttermap.repository.OrgUnitRepository;
import app.cluttermap.repository.ProjectRepository;
import app.cluttermap.repository.RoomRepository;
import app.cluttermap.repository.UserRepository;
import app.cluttermap.util.ResourceType;

@Service("securityService")
public class SecurityService {
    /* ------------- Injected Dependencies ------------- */
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final RoomRepository roomRepository;
    private final OrgUnitRepository orgUnitRepository;
    private final ItemRepository itemRepository;

    /* ------------- Constructor ------------- */
    public SecurityService(
            UserRepository userRepository,
            ProjectRepository projectRepository,
            RoomRepository roomRepository,
            OrgUnitRepository orgUnitRepository,
            ItemRepository itemRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.roomRepository = roomRepository;
        this.orgUnitRepository = orgUnitRepository;
        this.itemRepository = itemRepository;
    }

    /* ------------- Current User Operations ------------- */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            throw new InvalidAuthenticationException("Authentication does not contain a JWT token.");
        }

        Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
        Long user_id = Long.valueOf(jwt.getSubject());

        return userRepository.findById(user_id).orElseThrow(() -> new UserNotFoundException());
    }

    /* ------------- Resource Ownership Checks ------------- */
    public boolean isResourceOwner(Long resourceId, String resourceType) {
        Long currentUserId = getCurrentUser().getId();

        switch (resourceType) {
            case "project":
                Project project = projectRepository.findById(resourceId)
                        .orElseThrow(() -> new ResourceNotFoundException(ResourceType.PROJECT, resourceId));

                return project.getOwner().getId().equals(currentUserId);

            case "room":
                Room room = roomRepository.findById(resourceId)
                        .orElseThrow(() -> new ResourceNotFoundException(ResourceType.ROOM, resourceId));

                return room.getProject().getOwner().getId().equals(currentUserId);

            case "org-unit":
                OrgUnit orgUnit = orgUnitRepository.findById(resourceId)
                        .orElseThrow(() -> new ResourceNotFoundException(ResourceType.ORGANIZATIONAL_UNIT, resourceId));

                return orgUnit.getRoom().getProject().getOwner().getId().equals(currentUserId);

            case "item":
                Item item = itemRepository.findById(resourceId)
                        .orElseThrow(() -> new ResourceNotFoundException(ResourceType.ITEM, resourceId));

                return item.getOrgUnit().getRoom().getProject().getOwner().getId().equals(currentUserId);
        }

        return false;
    }
}
