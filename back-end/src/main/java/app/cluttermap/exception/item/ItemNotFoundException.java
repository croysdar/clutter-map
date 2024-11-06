package app.cluttermap.exception.item;

public class ItemNotFoundException extends RuntimeException{
    public ItemNotFoundException() {
        super("Item not found.");
    }
}
