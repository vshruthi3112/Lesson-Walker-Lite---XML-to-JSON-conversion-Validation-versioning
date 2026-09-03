package com.lessonwalker.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.lessonwalker.exception.LessonWriteException;
import com.lessonwalker.model.Lesson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes a Lesson object to a JSON file using Jackson.
 * Produces pretty-printed output for readability.
 */
public class LessonJsonWriter {

    private static final Logger logger = LoggerFactory.getLogger(LessonJsonWriter.class);

    private final ObjectMapper objectMapper;

    public LessonJsonWriter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Serializes the Lesson object to a JSON file.
     *
     * @param lesson   the lesson to write
     * @param jsonFile the output file path
     * @throws LessonWriteException if writing fails
     */
    public void write(Lesson lesson, Path jsonFile) throws LessonWriteException {
        logger.debug("Serializing lesson to JSON: {}", jsonFile);

        try {
            // Ensure parent directories exist
            if (jsonFile.getParent() != null) {
                Files.createDirectories(jsonFile.getParent());
            }

            objectMapper.writeValue(jsonFile.toFile(), lesson);

            long fileSize = jsonFile.toFile().length();
            logger.info("JSON written successfully: {} bytes -> {}", fileSize, jsonFile);
        } catch (IOException e) {
            throw new LessonWriteException(
                    "Failed to write JSON to " + jsonFile + ": " + e.getMessage(), e);
        }
    }

    /**
     * Serializes the Lesson object to a JSON string (useful for testing/logging).
     *
     * @param lesson the lesson to serialize
     * @return JSON string representation
     * @throws LessonWriteException if serialization fails
     */
    public String writeToString(Lesson lesson) throws LessonWriteException {
        try {
            return objectMapper.writeValueAsString(lesson);
        } catch (IOException e) {
            throw new LessonWriteException(
                    "Failed to serialize lesson to JSON string: " + e.getMessage(), e);
        }
    }
}
