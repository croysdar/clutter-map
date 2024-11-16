package app.cluttermap.exception;

public class IllegalArgumentResponse {
    private String message;

    public IllegalArgumentResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}