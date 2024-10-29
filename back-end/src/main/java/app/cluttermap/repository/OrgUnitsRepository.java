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
            "JOIN rooms r ON ou.room_id = r.id " +
            "JOIN projects p ON r.project_id = p.id " +
            "WHERE p.owner_id = :ownerId", nativeQuery = true)
    List<OrgUnit> findOrgUnitsByUserId(@Param("ownerId") Long ownerId);
}