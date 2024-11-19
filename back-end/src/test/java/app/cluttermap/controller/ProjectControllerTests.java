package app.cluttermap.controller;

import static org.hamcrest.Matchers.greaterThan;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.cluttermap.TestDataFactory;
import app.cluttermap.exception.ResourceNotFoundException;
import app.cluttermap.model.Item;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.NewProjectDTO;
import app.cluttermap.model.dto.UpdateProjectDTO;
import app.cluttermap.service.ItemService;
import app.cluttermap.service.OrgUnitService;
import app.cluttermap.service.ProjectService;
import app.cluttermap.service.SecurityService;
import app.cluttermap.util.ResourceType;

@WebMvcTest(ProjectController.class)
@ExtendWith(SpringExtension.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class ProjectControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private OrgUnitService orgUnitService;

    @MockBean
    private ItemService itemService;

    @MockBean
    private SecurityService securityService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User("mockProviderId");
        when(securityService.getCurrentUser()).thenReturn(mockUser);
        // Stub isResourceOwner to allow access to protected resources
        when(securityService.isResourceOwner(anyLong(), eq("project"))).thenReturn(true);
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getUserProjects_ShouldReturnAllUserProjects() throws Exception {
        // Arrange: Set up mock user projects and mock the service to return them
        Project project1 = new TestDataFactory.ProjectBuilder().user(mockUser).build();
        Project project2 = new TestDataFactory.ProjectBuilder().user(mockUser).build();
        when(projectService.getUserProjects()).thenReturn(List.of(project1, project2));

        // Act: Perform a GET request to the /projects endpoint
        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())

                // Assert: Verify the response contains 2 projects
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        // Assert: Ensure that the service method was called
        verify(projectService).getUserProjects();
    }

    @Test
    void getUserProjects_ShouldReturnEmptyList_WhenNoProjectsExist() throws Exception {
        // Arrange: Set up the service to return an empty list
        when(projectService.getUserProjects()).thenReturn(Collections.emptyList());

        // Act: Perform a GET request to the /projects endpoint
        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())

                // Assert: Verify the response contains an empty array
                .andExpect(jsonPath("$").isEmpty());

        // Assert: Ensure that the service method was called
        verify(projectService).getUserProjects();
    }

    @Test
    void getOneProject_ShouldReturnProject_WhenProjectExists() throws Exception {
        // Arrange: Set up a mock project and stub the service to return it when
        // searched by ID
        Project project = new TestDataFactory.ProjectBuilder().name("Test Project").user(mockUser).build();
        when(projectService.getProjectById(1L)).thenReturn(project);

        // Act: Perform a GET request to the /projects/1 endpoint
        mockMvc.perform(get("/projects/1"))
                .andExpect(status().isOk())
                // Assert: Verify the response contains the expected project name
                .andExpect(jsonPath("$.name").value("Test Project"));

        // Assert: Ensure that the service method was called
        verify(projectService).getProjectById(1L);
    }

    @Test
    void getOneProject_ShouldReturnNotFound_WhenProjectDoesNotExist() throws Exception {
        // Arrange: Mock the service to throw ProjectNotFoundException when a
        // non-existent project ID is requested
        when(projectService.getProjectById(1L)).thenThrow(new ResourceNotFoundException(ResourceType.PROJECT, 1L));

        // Act: Perform a GET request to the /projects/1 endpoint
        mockMvc.perform(get("/projects/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("PROJECT with ID 1 not found."));

        // Assert: Ensure that the service method was called
        verify(projectService).getProjectById(1L);
    }

    @Test
    void getUnassignedItemsByProjectId_Success() throws Exception {
        // Arrange: Set up projectId and simulate unassigned items
        Project project = new TestDataFactory.ProjectBuilder().user(mockUser).build();
        Long projectId = 1L;
        Item unassignedItem = new TestDataFactory.ItemBuilder().project(project).build();
        when(itemService.getUnassignedItemsByProjectId(projectId)).thenReturn(List.of(unassignedItem));

        // Act & Assert: Perform the GET request on the new path and verify status 200
        // OK and correct data
        mockMvc.perform(get("/projects/{projectId}/items/unassigned", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(unassignedItem.getName()));
    }

    @Test
    void getUnassignedItemsByProjectId_NoUnassignedItems_ShouldReturnEmptyList() throws Exception {
        // Arrange: Set up projectId and simulate no unassigned items
        Long projectId = 1L;
        when(itemService.getUnassignedItemsByProjectId(projectId)).thenReturn(List.of());

        // Act & Assert: Perform the GET request and verify status 200 OK with an empty
        // list
        mockMvc.perform(get("/projects/{projectId}/items/unassigned", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getUnassignedItemsByNonExistentProjectId_ShouldReturnNotFound() throws Exception {
        // Arrange: Use a project ID that does not exist
        Long nonExistentProjectId = 999L;
        when(itemService.getUnassignedItemsByProjectId(nonExistentProjectId))
                .thenThrow(new ResourceNotFoundException(ResourceType.PROJECT, 999L));

        // Act & Assert: Perform the GET request and verify status 404 Not Found
        mockMvc.perform(get("/projects/{projectId}/items/unassigned", nonExistentProjectId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("PROJECT with ID 999 not found."));
    }

    @Test
    void getUnassignedOrgUnitsByProjectId_Success() throws Exception {
        // Arrange: Set up projectId and simulate unassigned org units
        Project project = new TestDataFactory.ProjectBuilder().user(mockUser).build();
        Long projectId = 1L;
        OrgUnit unassignedOrgUnit = new TestDataFactory.OrgUnitBuilder().project(project).build();
        when(orgUnitService.getUnassignedOrgUnitsByProjectId(projectId)).thenReturn(List.of(unassignedOrgUnit));

        // Act & Assert: Perform the GET request and verify status 200 OK and correct
        // data
        mockMvc.perform(get("/projects/{projectId}/org-units/unassigned", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(unassignedOrgUnit.getName()));
    }

    @Test
    void getUnassignedOrgUnitsByProjectId_NoUnassignedOrgUnits_ShouldReturnEmptyList() throws Exception {
        // Arrange: Set up projectId and simulate no unassigned org units
        Long projectId = 1L;
        when(orgUnitService.getUnassignedOrgUnitsByProjectId(projectId)).thenReturn(List.of());

        // Act & Assert: Perform the GET request and verify status 200 OK with an empty
        // list
        mockMvc.perform(get("/projects/{projectId}/org-units/unassigned", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getUnassignedOrgUnitsByNonExistentProjectId_ShouldReturnNotFound() throws Exception {
        // Arrange: Use a project ID that does not exist
        Long nonExistentProjectId = 999L;
        when(orgUnitService.getUnassignedOrgUnitsByProjectId(nonExistentProjectId))
                .thenThrow(new ResourceNotFoundException(ResourceType.PROJECT, 999L));

        // Act & Assert: Perform the GET request and verify status 404 Not Found
        mockMvc.perform(get("/projects/{projectId}/org-units/unassigned", nonExistentProjectId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("PROJECT with ID 999 not found."));
    }

    @Test
    void addOneProject_ShouldCreateProject_WhenValidRequest() throws Exception {
        // Arrange: Set up a NewProjectDTO with valid data and mock the service to
        // return a new project
        NewProjectDTO projectDTO = new TestDataFactory.NewProjectDTOBuilder().build();
        Project newProject = new TestDataFactory.ProjectBuilder().fromDTO(projectDTO).user(mockUser).build();
        when(projectService.createProject(any(NewProjectDTO.class))).thenReturn(newProject);

        // Act: Perform a POST request to the /projects endpoint with the project data
        mockMvc.perform(post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isOk())

                // Assert: Verify the response contains the expected project name
                .andExpect(jsonPath("$.name").value(projectDTO.getName()));

        // Assert: Ensure the service method was called to create the project
        verify(projectService).createProject(any(NewProjectDTO.class));
    }

    @Test
    void addOneProject_ShouldReturnBadRequest_WhenProjectNameIsBlank() throws Exception {
        // Arrange: Set up a NewProjectDTO with a blank name to trigger validation
        NewProjectDTO projectDTO = new TestDataFactory.NewProjectDTOBuilder().name("").build();

        // Act: Perform a POST request to the /projects endpoint with the blank project
        // name
        mockMvc.perform(post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isBadRequest())

                // Assert: Verify the validation error response for the name field
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Project name must not be blank."));
    }

    @Test
    void addOneProject_ShouldReturnBadRequest_WhenProjectNameIsNull() throws Exception {
        // Arrange: Set up a NewProjectDTO with a null name to trigger validation
        NewProjectDTO projectDTO = new TestDataFactory.NewProjectDTOBuilder().name(null).build();

        // Act: Perform a POST request to the /projects endpoint with the null project
        // name
        mockMvc.perform(post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isBadRequest())

                // Assert: Verify the validation error response for the name field
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Project name must not be blank."));
    }

    @Test
    void updateOneProject_ShouldUpdateProject_WhenValidRequest() throws Exception {
        // Arrange: Set up an UpdateProjectDTO with a new name and mock the service to
        // return the updated project
        NewProjectDTO projectDTO = new TestDataFactory.NewProjectDTOBuilder().build();

        Project updatedProject = new TestDataFactory.ProjectBuilder().fromDTO(projectDTO).user(mockUser).build();
        when(projectService.updateProject(eq(1L), any(UpdateProjectDTO.class))).thenReturn(updatedProject);

        // Act: Perform a PUT request to the /projects/1 endpoint with the update data
        mockMvc.perform(put("/projects/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isOk())

                // Assert: Verify the response contains the updated project name
                .andExpect(jsonPath("$.name").value(projectDTO.getName()));

        // Assert: Ensure the service method was called
        verify(projectService).updateProject(eq(1L), any(UpdateProjectDTO.class));
    }

    @Test
    void updateOneProject_ShouldReturnBadRequest_WhenProjectNameIsBlank() throws Exception {
        // Arrange: Set up an UpdateProjectDTO with a blank project name to trigger
        // validation
        UpdateProjectDTO projectDTO = new TestDataFactory.UpdateProjectDTOBuilder().name("").build();

        // Act: Perform a PUT request to the /projects/1 endpoint with the invalid
        // project name
        mockMvc.perform(put("/projects/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isBadRequest())

                // Assert: Verify the validation error response for the name field
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Project name must not be blank."));
    }

    @Test
    void updateOneProject_ShouldReturnBadRequest_WhenProjectNameIsNull() throws Exception {
        // Arrange: Set up an UpdateProjectDTO with a null project name to trigger
        // validation
        UpdateProjectDTO projectDTO = new TestDataFactory.UpdateProjectDTOBuilder().name(null).build();

        // Act: Perform a PUT request to the /projects/1 endpoint with the null project
        // name
        mockMvc.perform(put("/projects/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isBadRequest())

                // Assert: Verify the validation error response for the name field
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Project name must not be blank."));
    }

    @Test
    void deleteOneProject_ShouldDeleteProject_WhenProjectExists() throws Exception {
        // Act: Perform a DELETE request to the /projects/1 endpoint
        mockMvc.perform(delete("/projects/1"))
                .andExpect(status().isNoContent());

        // Assert: Ensure the service method was called to delete the project by ID
        verify(projectService).deleteProject(1L);
    }

    @Test
    void deleteOneProject_ShouldReturnNotFound_WhenProjectDoesNotExist() throws Exception {
        // Arrange: Mock the service to throw ProjectNotFoundException when deleting a
        // non-existent project
        doThrow(new ResourceNotFoundException(ResourceType.PROJECT, 1L)).when(projectService).deleteProject(1L);

        // Act: Perform a DELETE request to the /projects/1 endpoint
        mockMvc.perform(delete("/projects/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("PROJECT with ID 1 not found."));

        // Assert: Ensure the service method was called to attempt to delete the project
        verify(projectService).deleteProject(1L);
    }

    @Test
    void getProjectRooms_ShouldReturnRooms_WhenProjectExists() throws Exception {
        // Arrange: Set up a project with a room and mock the service to return the
        // project
        Project project = new TestDataFactory.ProjectBuilder().user(mockUser).build();
        Room room = new TestDataFactory.RoomBuilder().project(project).build();
        project.setRooms(Collections.singletonList(room));
        when(projectService.getProjectById(1L)).thenReturn(project);

        // Act: Perform a GET request to the /projects/1/rooms endpoint
        mockMvc.perform(get("/projects/1/rooms"))
                .andExpect(status().isOk())

                // Assert: Verify the response contains the expected room name
                .andExpect(jsonPath("$[0].name").value(room.getName()));

        // Assert: Ensure the service method was called to retrieve the project by ID
        verify(projectService).getProjectById(1L);
    }

    @Test
    void getProjectRooms_ShouldReturnNotFound_WhenProjectDoesNotExist() throws Exception {
        // Arrange: Mock the service to throw ProjectNotFoundException when retrieving
        // rooms for a non-existent project
        when(projectService.getProjectById(1L)).thenThrow(new ResourceNotFoundException(ResourceType.PROJECT, 1L));

        // Act: Perform a GET request to the /projects/1/rooms endpoint
        mockMvc.perform(get("/projects/1/rooms"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("PROJECT with ID 1 not found."));

        // Assert: Ensure the service method was called to attempt to retrieve the
        // project
        verify(projectService).getProjectById(1L);
    }
}
