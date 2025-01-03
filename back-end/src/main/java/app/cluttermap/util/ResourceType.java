package app.cluttermap.util;

public enum ResourceType {
    PROJECT,
    ROOM,
    ORGANIZATIONAL_UNIT,
    ITEM;

    public String toFriendlyString() {
        switch (this) {
            case PROJECT:
                return "project";
            case ROOM:
                return "room";
            case ORGANIZATIONAL_UNIT:
                return "organizer";
            default:
                return "item";
        }
    }

}