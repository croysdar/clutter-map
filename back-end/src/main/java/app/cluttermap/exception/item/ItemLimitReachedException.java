package app.cluttermap.exception.item;

public class ItemLimitReachedException extends RuntimeException {
    public ItemLimitReachedException() {
        super("Maximum item limit reached.");
    }
}