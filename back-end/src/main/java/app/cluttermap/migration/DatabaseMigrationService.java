package app.cluttermap.migration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class DatabaseMigrationService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void migrateDatabase() {
        addColumnIfNotExists("org_units", "project_id", "BIGINT");
        addColumnIfNotExists("items", "project_id", "BIGINT");

        populateProjectIdForOrgUnits();
        populateProjectIdForItems();
    }

    private void addColumnIfNotExists(String tableName, String columnName, String dataType) {
        String sql = String.format(
                "DO $$ " +
                        "BEGIN " +
                        "IF NOT EXISTS (SELECT 1 FROM information_schema.columns " +
                        "WHERE table_name='%s' AND column_name='%s') THEN " +
                        "ALTER TABLE %s ADD COLUMN %s %s; " +
                        "END IF; " +
                        "END $$;",
                tableName, columnName, tableName, columnName, dataType);

        jdbcTemplate.execute(sql);
    }

    private void populateProjectIdForOrgUnits() {
        String updateOrgUnitsSql = "UPDATE org_units " +
                "SET project_id = (SELECT project_id FROM rooms WHERE rooms.id = org_units.room_id) " +
                "WHERE project_id IS NULL";
        jdbcTemplate.update(updateOrgUnitsSql);
    }

    private void populateProjectIdForItems() {
        String updateItemsSql = "UPDATE items " +
                "SET project_id = (SELECT project_id FROM org_units WHERE org_units.id = items.org_unit_id) " +
                "WHERE project_id IS NULL";
        jdbcTemplate.update(updateItemsSql);
    }
}
