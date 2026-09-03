package com.lessonwalker.exception;

/**
 * Base exception for all Lesson Walker application errors.
 * Provides a common parent for specific exception types.
 */
public class LessonWalkerException extends Exception {

    public LessonWalkerException(String message) {
        super(message);
    }

    public LessonWalkerException(String message, Throwable cause) {
        super(message, cause);
    }
}
