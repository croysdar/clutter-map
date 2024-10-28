package app.cluttermap.exception.room;

public class RoomNotFoundException extends RuntimeException{
    public RoomNotFoundException() {
        super("Room not found");
    }
}
