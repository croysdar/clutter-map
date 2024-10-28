package app.cluttermap.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import app.cluttermap.exception.auth.InvalidAuthenticationException;
import app.cluttermap.exception.auth.UserNotFoundException;
import app.cluttermap.exception.org_unit.OrgUnitNotFoundException;
import app.cluttermap.exception.project.ProjectNotFoundException;
import app.cluttermap.exception.room.RoomNotFoundException;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import app.cluttermap.repository.OrgUnitsRepository;
import app.cluttermap.repository.ProjectsRepository;
import app.cluttermap.repository.RoomsRepository;
import app.cluttermap.repository.UsersRepository;

@Service("securityService")
public class SecurityService {
    @Autowired
    private final ProjectsRepository projectsRepository;

    @Autowired
    private final RoomsRepository roomsRepository;

    @Autowired
    private final OrgUnitsRepository orgUnitsRepository;

    @Autowired
    private final UsersRepository usersRepository;

    public SecurityService(UsersRepository usersRepository, ProjectsRepository projectsRepository, RoomsRepository roomsRepository, OrgUnitsRepository orgUnitsRepository) {
        this.usersRepository = usersRepository;
        this.projectsRepository = projectsRepository;
        this.roomsRepository = roomsRepository;
        this.orgUnitsRepository = orgUnitsRepository;
    }

    public Long getUserIdFromAuthentication(Authentication authentication) {
        return getUserFromAuthentication(authentication).getId();
    }

    public User getUserFromAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            throw new InvalidAuthenticationException("Authentication does not contain a JWT token.");
        }

        Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
        Long user_id = Long.valueOf(jwt.getSubject());

        return usersRepository.findById(user_id).orElseThrow(() -> new UserNotFoundException("User does not exist"));
    }

    public boolean isResourceOwner(Authentication authentication, Long resourceId, String resourceType) {
        Long currentUserId = getUserIdFromAuthentication(authentication);

        switch (resourceType) {
            case "project" :
                Optional <Project> projectData = projectsRepository.findById(resourceId);
                if (!projectData.isPresent()) {
                    throw new ProjectNotFoundException();
                }

                return projectData.get().getOwner().getId().equals(currentUserId);

            case "room" :
                Optional <Room> roomData = roomsRepository.findById(resourceId);
                if (!roomData.isPresent()) {
                    throw new RoomNotFoundException();
                }

                return roomData.get().getProject().getOwner().getId().equals(currentUserId);

            case "org-unit" :
                Optional <OrgUnit> orgUnitData = orgUnitsRepository.findById(resourceId);
                if (!orgUnitData.isPresent()) {
                    throw new OrgUnitNotFoundException();
                }

                return orgUnitData.get().getRoom().getProject().getOwner().getId().equals(currentUserId);
        }

        return false;
    }
}
