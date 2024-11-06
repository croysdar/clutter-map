package app.cluttermap.exception.org_unit;

public class OrgUnitLimitReachedException extends RuntimeException {
    public OrgUnitLimitReachedException() {
        super("Maximum org unit limit reached.");
    }
}