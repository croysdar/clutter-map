package app.cluttermap.service;

import java.util.List;

import org.springframework.stereotype.Service;

import app.cluttermap.model.User;
import app.cluttermap.repository.ProjectRepository;

@Service
public class ProjectAccessService {
    private final ProjectRepository projectRepository;
    private final SecurityService securityService;

    public ProjectAccessService(ProjectRepository projectRepository, SecurityService securityService) {
        this.projectRepository = projectRepository;
        this.securityService = securityService;
    }

    public List<Long> getAccessibleProjectIds() {
        User user = securityService.getCurrentUser();
        return projectRepository.findProjectIdsByOwnerId(user.getId());
    }
}
