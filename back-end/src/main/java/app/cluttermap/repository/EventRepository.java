package app.cluttermap.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import app.cluttermap.model.Event;
import app.cluttermap.model.Project;
import app.cluttermap.util.ResourceType;

@Repository
public interface EventRepository extends CrudRepository<Event, Long> {
    @EntityGraph(attributePaths = { "user.id", "user.username" })
    @Query("SELECT e FROM Event e WHERE e.entityType = :entityType AND e.entityId = :entityId ORDER BY e.timestamp DESC")
    Page<Event> findByEntityTypeAndEntityId(ResourceType entityType, Long entityId, Pageable pageable);

    @EntityGraph(attributePaths = { "user.id", "user.username" })
    @Query("SELECT e FROM Event e WHERE e.project = :project ORDER BY e.timestamp DESC")
    Page<Event> findAllEventsInProject(Project project, Pageable pageable);

    @Query("SELECT DISTINCT e.entityType, e.entityId FROM Event e WHERE e.timestamp > :since")
    List<Object[]> findChangesSince(@Param("since") LocalDateTime since);
}