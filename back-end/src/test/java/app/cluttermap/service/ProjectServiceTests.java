package app.cluttermap.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import app.cluttermap.exception.project.ProjectLimitReachedException;
import app.cluttermap.exception.project.ProjectNotFoundException;
import app.cluttermap.model.Project;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.NewProjectDTO;
import app.cluttermap.model.dto.UpdateProjectDTO;
import app.cluttermap.repository.ProjectsRepository;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class ProjectServiceTests {
    @Mock
    private ProjectsRepository projectsRepository;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private ProjectService projectService;

    private User mockUser;

    private static int PROJECT_LIMIT = 3;

    @BeforeEach
    void setUp() {
        mockUser = new User("mockProviderId");
    }

    @Test
    void getProjectById_ShouldReturnProject_WhenProjectExists() {
        /*
         * Ensures that getProjectById returns the correct project when it exists in the
         * repository.
         */
        Project project = new Project("Sample Project", mockUser);
        when(projectsRepository.findById(1L)).thenReturn(Optional.of(project));

        Project foundProject = projectService.getProjectById(1L);

        assertThat(foundProject).isEqualTo(project);
    }

    @Test
    void getProjectById_ShouldThrowException_WhenProjectDoesNotExist() {
        /*
         * Verifies that getProjectById throws ProjectNotFoundException when attempting
         * to retrieve a non-existent project.
         */
        when(projectsRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class, () -> projectService.getProjectById(1L));
    }

    @Test
    void createProject_ShouldCreateProject_WhenLimitNotReached() {
        /*
         * Confirms that a new project can be created when the user has not reached the
         * project limit.
         */
        when(securityService.getCurrentUser()).thenReturn(mockUser);
        when(projectsRepository.findByOwner(mockUser)).thenReturn(Collections.emptyList());

        NewProjectDTO projectDTO = new NewProjectDTO("New Project");

        Project newProject = new Project("New Project", mockUser);
        when(projectsRepository.save(any(Project.class))).thenReturn(newProject);

        Project createdProject = projectService.createProject(projectDTO);

        assertThat(createdProject).isNotNull();
        assertThat(createdProject.getName()).isEqualTo("New Project");
        assertThat(createdProject.getOwner()).isEqualTo(mockUser);

        verify(projectsRepository).save(any(Project.class));
    }

    @Test
    void createProject_ShouldThrowException_WhenLimitReached() {
        /*
         * Tests that createProject throws ProjectLimitReachedException when the user
         * has reached the maximum project limit.
         */
        when(securityService.getCurrentUser()).thenReturn(mockUser);

        // Mock the number of projects to be one less than the limit
        List<Project> existingProjects = new ArrayList<>();
        for (int i = 0; i < PROJECT_LIMIT - 1; i++) {
            existingProjects.add(new Project("Project " + (i + 1), mockUser));
        }

        when(projectsRepository.findByOwner(mockUser)).thenReturn(existingProjects);

        // Attempt to create a new project within the limit
        NewProjectDTO projectDTO = new NewProjectDTO("Within Limit Project");
        Project newProject = new Project("Within Limit Project", mockUser);
        when(projectsRepository.save(any(Project.class))).thenReturn(newProject);

        Project createdProject = projectService.createProject(projectDTO);

        assertThat(createdProject).isNotNull();
        assertThat(createdProject.getName()).isEqualTo("Within Limit Project");

        verify(projectsRepository).save(any(Project.class));

        // Add one more project to reach the limit exactly
        existingProjects.add(createdProject);
        when(projectsRepository.findByOwner(mockUser)).thenReturn(existingProjects);

        NewProjectDTO exceedingProjectDTO = new NewProjectDTO("Exceeding Limit Project");

        assertThrows(ProjectLimitReachedException.class, () -> projectService.createProject(exceedingProjectDTO));
        verify(projectsRepository, times(1)).save(any(Project.class));
    }

    @Test
    void getUserProjects_ShouldReturnProjectsOwnedByUser() {
        /*
         * Checks that getUserProjects returns only the projects associated with the
         * current user.
         */
        when(securityService.getCurrentUser()).thenReturn(mockUser);

        Project project1 = new Project("Project 1", mockUser);
        Project project2 = new Project("Project 2", mockUser);
        when(projectsRepository.findByOwner(mockUser)).thenReturn(List.of(project1, project2));

        Iterable<Project> userProjects = projectService.getUserProjects();

        assertThat(userProjects).containsExactly(project1, project2);
    }

    @Test
    void getUserProjects_ShouldReturnEmptyList_WhenNoProjectsExist() {
        /*
         * Ensures that getUserProjects returns an empty list if the user has no
         * projects.
         */
        when(securityService.getCurrentUser()).thenReturn(mockUser);
        when(projectsRepository.findByOwner(mockUser)).thenReturn(Collections.emptyList());

        Iterable<Project> userProjects = projectService.getUserProjects();

        assertThat(userProjects).isEmpty();
    }

    @Test
    void updateProject_ShouldUpdateProject_WhenProjectExists() {
        /*
         * Verifies that updateProject updates the project’s name when the specified
         * project exists.
         */
        Project project = new Project("Old Name", mockUser);
        when(projectsRepository.findById(1L)).thenReturn(Optional.of(project));

        UpdateProjectDTO projectDTO = new UpdateProjectDTO("Updated Name");

        when(projectsRepository.save(project)).thenReturn(project);

        Project updatedProject = projectService.updateProject(1L, projectDTO);

        assertThat(updatedProject.getName()).isEqualTo("Updated Name");
        verify(projectsRepository).save(project);
    }

    @Test
    void updateProject_ShouldThrowException_WhenProjectDoesNotExist() {
        /*
         * Ensures that updateProject throws ProjectNotFoundException when trying to
         * update a non-existent project.
         */
        when(projectsRepository.findById(1L)).thenReturn(Optional.empty());

        UpdateProjectDTO projectDTO = new UpdateProjectDTO("Updated Name");

        assertThrows(ProjectNotFoundException.class, () -> projectService.updateProject(1L, projectDTO));
    }

    @Test
    void deleteProject_ShouldDeleteProject_WhenProjectExists() {
        /*
         * Tests that deleteProject successfully deletes the specified project when it
         * exists.
         */
        Project project = new Project("Sample Project", mockUser);
        when(projectsRepository.findById(1L)).thenReturn(Optional.of(project));

        projectService.deleteProject(1L);

        verify(projectsRepository).deleteById(1L);
    }

    @Test
    void deleteProject_ShouldThrowException_WhenProjectDoesNotExist() {
        /*
         * Verifies that deleteProject throws ProjectNotFoundException if attempting to
         * delete a non-existent project and doesn’t call the delete method.
         */
        when(projectsRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class, () -> projectService.deleteProject(1L));
        verify(projectsRepository, never()).deleteById(anyLong());
    }
}
