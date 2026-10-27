package com.hospital.exception;

/**
 * Thrown when an entity requested by ID (Patient, Doctor, Appointment)
 * cannot be found in the data store.
 */
public class ResourceNotFoundException extends HospitalException {

    private final String resourceType;
    private final int resourceId;

    public ResourceNotFoundException(String resourceType, int resourceId) {
        super(String.format("%s with ID %d was not found.", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public int getResourceId() {
        return resourceId;
    }
}
