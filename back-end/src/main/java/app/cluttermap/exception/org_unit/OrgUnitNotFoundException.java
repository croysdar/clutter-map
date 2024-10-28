package app.cluttermap.exception.org_unit;

public class OrgUnitNotFoundException extends RuntimeException{
    public OrgUnitNotFoundException() {
        super("Organization unit not found.");
    }
}
