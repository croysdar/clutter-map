package app.cluttermap.service;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import app.cluttermap.exception.auth.InvalidAuthenticationException;
import app.cluttermap.exception.auth.UserNotFoundException;
import app.cluttermap.exception.item.ItemNotFoundException;
import app.cluttermap.exception.org_unit.OrgUnitNotFoundException;
import app.cluttermap.exception.project.ProjectNotFoundException;
import app.cluttermap.exception.room.RoomNotFoundException;
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

@Service("securityService")
public class SecurityService {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final RoomRepository roomRepository;
    private final OrgUnitRepository orgUnitRepository;
    private final ItemRepository itemRepository;

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

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            throw new InvalidAuthenticationException("Authentication does not contain a JWT token.");
        }

        Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
        Long user_id = Long.valueOf(jwt.getSubject());

        return userRepository.findById(user_id).orElseThrow(() -> new UserNotFoundException());
    }

    public boolean isResourceOwner(Long resourceId, String resourceType) {
        Long currentUserId = getCurrentUser().getId();

        switch (resourceType) {
            case "project":
                Optional<Project> projectData = projectRepository.findById(resourceId);
                if (!projectData.isPresent()) {
                    throw new ProjectNotFoundException();
                }

                return projectData.get().getOwner().getId().equals(currentUserId);

            case "room":
                Optional<Room> roomData = roomRepository.findById(resourceId);
                if (!roomData.isPresent()) {
                    throw new RoomNotFoundException();
                }

                return roomData.get().getProject().getOwner().getId().equals(currentUserId);

            case "org-unit":
                Optional<OrgUnit> orgUnitData = orgUnitRepository.findById(resourceId);
                if (!orgUnitData.isPresent()) {
                    throw new OrgUnitNotFoundException();
                }

                return orgUnitData.get().getRoom().getProject().getOwner().getId().equals(currentUserId);

            case "item":
                Optional<Item> itemData = itemRepository.findById(resourceId);
                if (!itemData.isPresent()) {
                    throw new ItemNotFoundException();
                }

                return itemData.get().getOrgUnit().getRoom().getProject().getOwner().getId().equals(currentUserId);
        }

        return false;
    }
}
