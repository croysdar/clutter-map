package app.cluttermap.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import app.cluttermap.model.OrgUnit;

@Repository
public interface OrgUnitsRepository extends CrudRepository<OrgUnit, Long> {
    @Query(value = "SELECT ou.* FROM org_units ou " +
            "JOIN projects p ON ou.project_id = p.id " +
            "WHERE p.owner_id = :ownerId", nativeQuery = true)
    List<OrgUnit> findOrgUnitsByUserId(@Param("ownerId") Long ownerId);

    @Query(value = "SELECT r.* FROM rooms r WHERE r.project_id =:projectId", nativeQuery = true)
    List<OrgUnit> findByProjectId(@Param("projectId") Long project_id);
}