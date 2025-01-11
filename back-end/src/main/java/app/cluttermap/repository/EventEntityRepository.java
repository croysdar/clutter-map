package app.cluttermap.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import app.cluttermap.model.EventEntity;
import app.cluttermap.model.dto.EntityHistoryDTO;
import app.cluttermap.util.ResourceType;

@Repository
public interface EventEntityRepository extends CrudRepository<EventEntity, Long> {

    // Get a list of events/changes for a single entity
    // This gives you a timeline for an entity
    @Query("""
                SELECT new app.cluttermap.model.dto.EntityHistoryDTO(
                    ee.entityType,
                    ee.entityId,
                    ee.change,
                    ee.details,
                    u.username,
                    u.id,
                    e.timestamp
                )
                FROM EventEntity ee
                JOIN ee.event e
                JOIN e.user u
                WHERE ee.entityType = :entityType
                AND ee.entityId = :entityId
                ORDER BY e.timestamp DESC
            """)
    Page<EntityHistoryDTO> findHistoryByEntity(
            @Param("entityType") ResourceType entityType,
            @Param("entityId") Long entityId,
            Pageable pageable);

    // Get all changes in a group of projects since a certain date
    // this is for fetchUpdates
    @Query("""
                SELECT new app.cluttermap.model.dto.EntityHistoryDTO(
                    ee.entityType,
                    ee.entityId,
                    ee.change,
                    ee.details,
                    u.username,
                    u.id,
                    e.timestamp
                )
                FROM EventEntity ee
                JOIN ee.event e
                JOIN e.user u
                WHERE e.timestamp > :since
                AND e.project.id IN :projectIds
                ORDER BY e.timestamp ASC
            """)
    List<EntityHistoryDTO> findChangesSince(
            @Param("since") LocalDateTime since,
            @Param("projectIds") List<Long> projectIds);

}
