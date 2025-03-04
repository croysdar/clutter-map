package app.cluttermap.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import app.cluttermap.model.Room;

@Repository
public interface RoomRepository extends CrudRepository<Room, Long> {
    @Query(value = "SELECT r.* FROM rooms r JOIN projects p ON r.project_id = p.id WHERE p.owner_id = :ownerId", nativeQuery = true)
    List<Room> findByOwnerId(@Param("ownerId") Long owner_id);

    @Query(value = "SELECT r.* FROM rooms r WHERE r.project_id =:projectId", nativeQuery = true)
    List<Room> findByProjectId(@Param("projectId") Long project_id);
}
