package com.lessonwalker.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.lessonwalker.model.Lesson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * @throws Exception if writing fails
     */
    public void write(Lesson lesson, Path jsonFile) throws Exception {
        logger.debug("Serializing lesson to JSON: {}", jsonFile);

        // Ensure parent directories exist
        if (jsonFile.getParent() != null) {
            java.nio.file.Files.createDirectories(jsonFile.getParent());
        }

        objectMapper.writeValue(jsonFile.toFile(), lesson);

        logger.info("JSON written successfully: {} bytes", jsonFile.toFile().length());
    }

    /**
     * Serializes the Lesson object to a JSON string (useful for testing/logging).
     *
     * @param lesson the lesson to serialize
     * @return JSON string representation
     * @throws Exception if serialization fails
     */
    public String writeToString(Lesson lesson) throws Exception {
        return objectMapper.writeValueAsString(lesson);
    }
}
