package app.cluttermap.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import app.cluttermap.model.dto.EntityHistoryDTO;
import app.cluttermap.service.EventService;
import app.cluttermap.util.EventChangeType;
import app.cluttermap.util.ResourceType;

@WebMvcTest(EventController.class)
@ExtendWith(SpringExtension.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
public class EventControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getEntityHistory_ShouldReturnPagedResults() throws Exception {
        // Arrange
        ResourceType entityType = ResourceType.ITEM;
        Long entityId = 5L;

        List<EntityHistoryDTO> mockHistory = List.of(
                new EntityHistoryDTO(entityType, entityId, EventChangeType.UPDATE, "{\"description\":\"Wire brush.\"}",
                        "Jane Smith", 1L, Instant.now()),
                new EntityHistoryDTO(entityType, entityId, EventChangeType.UPDATE, "{\"quantity\":1}",
                        "Jane Smith", 1L, Instant.now()));

        Page<EntityHistoryDTO> page = new PageImpl<>(mockHistory, PageRequest.of(0, 10), mockHistory.size());
        when(eventService.getEntityHistory(entityType, entityId, 0, 10)).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/events/{entityType}/{id}", entityType, entityId)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.entityHistoryDTOList").isArray())
                .andExpect(jsonPath("$._embedded.entityHistoryDTOList.length()").value(mockHistory.size()))
                .andExpect(jsonPath("$._embedded.entityHistoryDTOList[0].entityId").value(entityId))
                .andExpect(jsonPath("$._embedded.entityHistoryDTOList[0].userName").value("Jane Smith"));

        verify(eventService).getEntityHistory(entityType, entityId, 0, 10);
    }

    @Test
    void getEntityHistory_ShouldReturnEmptyList_WhenNoEventsExist() throws Exception {
        // Arrange
        ResourceType entityType = ResourceType.ITEM;
        Long entityId = 5L;

        Page<EntityHistoryDTO> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(eventService.getEntityHistory(entityType, entityId, 0, 10)).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/events/{entityType}/{id}", entityType, entityId)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist());

        verify(eventService).getEntityHistory(entityType, entityId, 0, 10);
    }

    @Test
    void getChangedEntitiesSince_ShouldReturnUpdates() throws Exception {
        // Arrange
        String since = "2025-01-01T12:00:00Z";
        Instant sinceTime = Instant.parse(since);

        List<EntityHistoryDTO> updates = List.of(
                new EntityHistoryDTO(ResourceType.ITEM, 5L, EventChangeType.UPDATE,
                        "{\"description\":\"New description\"}",
                        "Jane Smith", 1L, Instant.now()));

        when(eventService.fetchUpdatesSince(sinceTime)).thenReturn(updates);

        // Act & Assert
        mockMvc.perform(get("/fetch-updates")
                .param("since", since))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(updates.size()))
                .andExpect(jsonPath("$[0].entityType").value("ITEM"))
                .andExpect(jsonPath("$[0].entityId").value(5L))
                .andExpect(jsonPath("$[0].action").value("UPDATE"))
                .andExpect(jsonPath("$[0].userName").value("Jane Smith"));

        verify(eventService).fetchUpdatesSince(sinceTime);
    }

    @Test
    void getChangedEntitiesSince_ShouldReturnEmptyList_WhenNoUpdatesExist() throws Exception {
        // Arrange
        String since = "2025-01-01T12:00:00Z";
        Instant sinceTime = Instant.parse(since);

        when(eventService.fetchUpdatesSince(sinceTime)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/fetch-updates")
                .param("since", since))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(eventService).fetchUpdatesSince(sinceTime);
    }

}
