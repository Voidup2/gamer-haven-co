package com.gamesphere.common.web;

/**
 * @deprecated Use {@link com.gamesphere.common.exception.ResourceNotFoundException}.
 */
@Deprecated(forRemoval = false)
public class ResourceNotFoundException extends com.gamesphere.common.exception.ResourceNotFoundException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
