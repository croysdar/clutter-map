package app.cluttermap.exception;

import app.cluttermap.util.ResourceType;

public class ResourceNotFoundException extends RuntimeException {
    private final ResourceType resourceType;
    private final Long resourceId;

    public ResourceNotFoundException(ResourceType resourceType, Long resourceId) {
        super(generateMessage(resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    private static String generateMessage(ResourceType resourceType, Long resourceId) {
        return resourceType + " with ID " + resourceId + " not found.";
    }

    public String getErrorMessage() {
        return super.getMessage();
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public Long getResourceId() {
        return resourceId;
    }
}