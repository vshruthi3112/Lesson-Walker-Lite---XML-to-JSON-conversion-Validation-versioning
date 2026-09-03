package com.lessonwalker.exception;

/**
 * Thrown when an error occurs while parsing a lesson XML file.
 * Wraps lower-level parsing errors with a descriptive message.
 */
public class LessonParseException extends LessonWalkerException {

    public LessonParseException(String message) {
        super(message);
    }

    public LessonParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
