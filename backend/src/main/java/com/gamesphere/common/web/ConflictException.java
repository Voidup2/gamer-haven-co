package com.gamesphere.common.web;

/**
 * @deprecated Use {@link com.gamesphere.common.exception.ConflictException}.
 */
@Deprecated(forRemoval = false)
public class ConflictException extends com.gamesphere.common.exception.ConflictException {
    public ConflictException(String message) {
        super(message);
    }
}
