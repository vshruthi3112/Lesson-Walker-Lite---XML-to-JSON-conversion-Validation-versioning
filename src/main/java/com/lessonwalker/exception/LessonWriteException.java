package com.lessonwalker.exception;

/**
 * Thrown when an error occurs while writing a lesson to JSON.
 */
public class LessonWriteException extends LessonWalkerException {

    public LessonWriteException(String message) {
        super(message);
    }

    public LessonWriteException(String message, Throwable cause) {
        super(message, cause);
    }
}
