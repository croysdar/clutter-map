package app.cluttermap.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import app.cluttermap.exception.auth.InvalidAuthenticationException;
import app.cluttermap.exception.auth.UserNotFoundException;
import app.cluttermap.model.Project;
import app.cluttermap.model.User;
import app.cluttermap.repository.UserRepository;
import app.cluttermap.util.ResourceType;

@Service("securityService")
public class SecurityService {
    /* ------------- Injected Dependencies ------------- */
    private final UserRepository userRepository;
    private final EntityResolutionService entityResolutionService;

    /* ------------- Constructor ------------- */
    public SecurityService(
            UserRepository userRepository,
            EntityResolutionService entityResolutionService) {
        this.userRepository = userRepository;
        this.entityResolutionService = entityResolutionService;
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
    public boolean isResourceOwner(Long resourceId, ResourceType resourceType) {
        Long currentUserId = getCurrentUser().getId();

        Project project = entityResolutionService.resolveProject(resourceType, resourceId);

        return project.getOwner().getId().equals(currentUserId);
    }
}
