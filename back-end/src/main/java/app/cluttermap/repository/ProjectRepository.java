package app.cluttermap.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import app.cluttermap.model.Project;

@Repository
public interface ProjectRepository extends CrudRepository<Project, Long> {
    @Query(value = "SELECT p.* FROM projects p WHERE p.owner_id =:ownerId", nativeQuery = true)
    List<Project> findByOwnerId(@Param("ownerId") Long ownerId);
}
