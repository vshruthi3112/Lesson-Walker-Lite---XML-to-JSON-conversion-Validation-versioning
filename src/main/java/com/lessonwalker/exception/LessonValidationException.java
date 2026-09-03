package com.lessonwalker.exception;

import java.util.Collections;
import java.util.List;

/**
 * Thrown when an XML file fails schema validation.
 * Contains a list of all validation errors encountered.
 */
public class LessonValidationException extends LessonWalkerException {

    private final List<String> validationErrors;

    public LessonValidationException(String message, List<String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors != null
                ? Collections.unmodifiableList(validationErrors)
                : Collections.emptyList();
    }

    public LessonValidationException(String message, List<String> validationErrors, Throwable cause) {
        super(message, cause);
        this.validationErrors = validationErrors != null
                ? Collections.unmodifiableList(validationErrors)
                : Collections.emptyList();
    }

    /**
     * Returns the list of individual validation error messages.
     */
    public List<String> getValidationErrors() {
        return validationErrors;
    }
}
