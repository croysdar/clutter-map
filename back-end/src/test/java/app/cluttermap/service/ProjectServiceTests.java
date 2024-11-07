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

import app.cluttermap.exception.project.ProjectLimitReachedException;
import app.cluttermap.exception.project.ProjectNotFoundException;
import app.cluttermap.model.Project;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.NewProjectDTO;
import app.cluttermap.model.dto.UpdateProjectDTO;
import app.cluttermap.repository.ProjectsRepository;

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
        // Arrange: Prepare a sample project and stub the repository to return it when
        // searched by ID
        Project project = new Project("Sample Project", mockUser);
        when(projectsRepository.findById(1L)).thenReturn(Optional.of(project));

        // Act: Call the service method to retrieve the project by ID
        Project foundProject = projectService.getProjectById(1L);

        // Assert: Verify that the retrieved project matches the expected project
        assertThat(foundProject).isEqualTo(project);
    }

    @Test
    void getProjectById_ShouldThrowException_WhenProjectDoesNotExist() {
        // Arrange: Stub the repository to return an empty result when searching for a
        // non-existent project ID
        when(projectsRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert: Verify that calling getProjectById throws
        // ProjectNotFoundException for a missing project
        assertThrows(ProjectNotFoundException.class, () -> projectService.getProjectById(1L));
    }

    @Test
    void createProject_ShouldCreateProject_WhenLimitNotReached() {
        // Arrange: Set up mocks for the current user and their existing projects
        when(securityService.getCurrentUser()).thenReturn(mockUser);
        when(projectsRepository.findByOwner(mockUser)).thenReturn(Collections.emptyList());

        // Arrange: Create a DTO for the new project and set up a mock project to return
        // on save
        NewProjectDTO projectDTO = new NewProjectDTO("New Project");
        Project newProject = new Project("New Project", mockUser);
        when(projectsRepository.save(any(Project.class))).thenReturn(newProject);

        // Act: Call the service to create the project
        Project createdProject = projectService.createProject(projectDTO);

        // Assert: Verify the project was created with the expected properties
        assertThat(createdProject).isNotNull();
        assertThat(createdProject.getName()).isEqualTo("New Project");
        assertThat(createdProject.getOwner()).isEqualTo(mockUser);

        // Assert: Ensure the project was saved in the repository
        verify(projectsRepository).save(any(Project.class));
    }

    @Test
    void createProject_ShouldThrowException_WhenLimitReached() {
        // Arrange: Set up the current user and mock their existing projects to be one
        // less than the project limit
        when(securityService.getCurrentUser()).thenReturn(mockUser);

        List<Project> existingProjects = new ArrayList<>();
        for (int i = 0; i < PROJECT_LIMIT - 1; i++) {
            existingProjects.add(new Project("Project " + (i + 1), mockUser));
        }
        when(projectsRepository.findByOwner(mockUser)).thenReturn(existingProjects);

        // Arrange: Prepare a DTO for creating a new project within the project limit
        NewProjectDTO projectDTO = new NewProjectDTO("Within Limit Project");
        Project newProject = new Project("Within Limit Project", mockUser);
        when(projectsRepository.save(any(Project.class))).thenReturn(newProject);

        // Act: Call the service to create a project within the limit
        Project createdProject = projectService.createProject(projectDTO);

        // Assert: Verify the project was created and saved within the limit
        assertThat(createdProject).isNotNull();
        assertThat(createdProject.getName()).isEqualTo("Within Limit Project");
        verify(projectsRepository).save(any(Project.class));

        // Arrange: Add one more project to reach the project limit exactly
        existingProjects.add(createdProject);
        when(projectsRepository.findByOwner(mockUser)).thenReturn(existingProjects);

        // Act & Assert: Attempting to create another project should throw
        // ProjectLimitReachedException
        NewProjectDTO exceedingProjectDTO = new NewProjectDTO("Exceeding Limit Project");
        assertThrows(ProjectLimitReachedException.class, () -> projectService.createProject(exceedingProjectDTO));
        verify(projectsRepository, times(1)).save(any(Project.class));
    }

    @Test
    void getUserProjects_ShouldReturnProjectsOwnedByUser() {
        // Arrange: Set up the current user and mock the projects they own
        when(securityService.getCurrentUser()).thenReturn(mockUser);

        Project project1 = new Project("Project 1", mockUser);
        Project project2 = new Project("Project 2", mockUser);
        when(projectsRepository.findByOwner(mockUser)).thenReturn(List.of(project1, project2));

        // Act: Call the service to retrieve all projects owned by the user
        Iterable<Project> userProjects = projectService.getUserProjects();

        // Assert: Verify that the returned projects match the expected projects
        assertThat(userProjects).containsExactly(project1, project2);
    }

    @Test
    void getUserProjects_ShouldReturnEmptyList_WhenNoProjectsExist() {
        // Arrange: Set up the current user and mock their projects to return an empty
        // list
        when(securityService.getCurrentUser()).thenReturn(mockUser);
        when(projectsRepository.findByOwner(mockUser)).thenReturn(Collections.emptyList());

        // Act: Call the service to retrieve projects owned by the user
        Iterable<Project> userProjects = projectService.getUserProjects();

        // Assert: Verify that the returned projects list is empty
        assertThat(userProjects).isEmpty();
    }

    @Test
    void updateProject_ShouldUpdateProject_WhenProjectExists() {
        // Arrange: Set up an existing project and mock the repository to return it when
        // searched by ID
        Project project = new Project("Old Name", mockUser);
        when(projectsRepository.findById(1L)).thenReturn(Optional.of(project));

        // Arrange: Prepare the DTO with the updated project name
        UpdateProjectDTO projectDTO = new UpdateProjectDTO("Updated Name");

        // Arrange: Mock the repository save to return the updated project
        when(projectsRepository.save(project)).thenReturn(project);

        // Act: Call the service to update the project's name
        Project updatedProject = projectService.updateProject(1L, projectDTO);

        // Assert: Verify that the project's name was updated as expected
        assertThat(updatedProject.getName()).isEqualTo("Updated Name");

        // Assert: Ensure the repository save method was called to persist the changes
        verify(projectsRepository).save(project);
    }

    @Test
    void updateProject_ShouldThrowException_WhenProjectDoesNotExist() {
        // Arrange: Mock the repository to return an empty result when searching for a
        // non-existent project ID
        when(projectsRepository.findById(1L)).thenReturn(Optional.empty());

        // Arrange: Prepare the DTO with the updated project name
        UpdateProjectDTO projectDTO = new UpdateProjectDTO("Updated Name");

        // Act & Assert: Verify that attempting to update a non-existent project throws
        // ProjectNotFoundException
        assertThrows(ProjectNotFoundException.class, () -> projectService.updateProject(1L, projectDTO));
    }

    @Test
    void deleteProject_ShouldDeleteProject_WhenProjectExists() {
        // Arrange: Set up an existing project and mock the repository to return it when
        // searched by ID
        Project project = new Project("Sample Project", mockUser);
        when(projectsRepository.findById(1L)).thenReturn(Optional.of(project));

        // Act: Call the service to delete the project by ID
        projectService.deleteProject(1L);

        // Assert: Verify that the repository's deleteById method was called with the
        // correct ID
        verify(projectsRepository).deleteById(1L);
    }

    @Test
    void deleteProject_ShouldThrowException_WhenProjectDoesNotExist() {
        // Arrange: Mock the repository to return an empty result when searching for a
        // non-existent project ID
        when(projectsRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert: Verify that attempting to delete a non-existent project throws
        // ProjectNotFoundException
        assertThrows(ProjectNotFoundException.class, () -> projectService.deleteProject(1L));

        // Assert: Verify that deleteById was never called on the repository, as the
        // project does not exist
        verify(projectsRepository, never()).deleteById(anyLong());
    }
}
