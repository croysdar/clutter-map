package app.cluttermap.exception.room;

public class RoomLimitReachedException extends RuntimeException {
    public RoomLimitReachedException() {
        super("Maximum room limit reached.");
    }

}
