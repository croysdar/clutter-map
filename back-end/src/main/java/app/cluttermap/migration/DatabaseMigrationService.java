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
        addTimestampColumnIfNotExists("projects", "last_updated");

        updateEventsActionConstraint();

        populateProjectIdForOrgUnits();
        populateProjectIdForItems();
    }

    private void addTimestampColumnIfNotExists(String tableName, String columnName) {
        String sql = String.format("""
                DO $$
                BEGIN
                    IF NOT EXISTS (
                        SELECT 1 FROM information_schema.columns
                        WHERE table_name='%s' AND column_name='%s'
                    ) THEN
                        ALTER TABLE %s ADD COLUMN %s TIMESTAMP WITH TIME ZONE DEFAULT NOW();
                    END IF;
                END $$;
                """, tableName, columnName, tableName, columnName);

        jdbcTemplate.execute(sql);
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

    private void updateEventsActionConstraint() {
        // Drop the old constraint if it exists
        String dropConstraintSql = """
                DO $$ 
                BEGIN
                    IF EXISTS (SELECT 1 FROM information_schema.table_constraints 
                            WHERE table_name='events' AND constraint_name='events_action_check') 
                    THEN
                        ALTER TABLE events DROP CONSTRAINT events_action_check;
                    END IF;
                END $$;
                """;
        jdbcTemplate.execute(dropConstraintSql);

        // Add the new constraint with updated values
        String addConstraintSql = """
                ALTER TABLE events 
                ADD CONSTRAINT events_action_check 
                CHECK (action IN ('CREATE', 'UPDATE', 'DELETE', 'MOVE', 'ADD_CHILD', 'REMOVE_CHILD'));
                """;
        jdbcTemplate.execute(addConstraintSql);
    }
}
