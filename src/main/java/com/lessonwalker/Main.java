package com.lessonwalker;

import com.lessonwalker.parser.LessonXmlParser;
import com.lessonwalker.writer.LessonJsonWriter;
import com.lessonwalker.model.Lesson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Entry point for the Lesson Walker Lite application.
 * Reads a lesson.xml file and converts it to lesson.json.
 *
 * Usage: java -jar lesson-walker-lite.jar <input.xml> [output.json]
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Lesson Walker Lite started");

        if (args.length < 1) {
            System.out.println("Usage: java -jar lesson-walker-lite.jar <input.xml> [output.json]");
            System.out.println("  <input.xml>   - Path to the lesson XML file (required)");
            System.out.println("  [output.json] - Path for the output JSON file (optional, defaults to lesson.json)");
            System.exit(1);
        }

        String inputPath = args[0];
        String outputPath = args.length > 1 ? args[1] : "lesson.json";

        Path xmlFile = Paths.get(inputPath);
        Path jsonFile = Paths.get(outputPath);

        // Validate input file exists
        if (!Files.exists(xmlFile)) {
            logger.error("Input file not found: {}", xmlFile.toAbsolutePath());
            System.err.println("Error: Input file not found: " + xmlFile.toAbsolutePath());
            System.exit(1);
        }

        try {
            // Parse XML
            logger.info("Parsing XML file: {}", xmlFile.toAbsolutePath());
            LessonXmlParser parser = new LessonXmlParser();
            Lesson lesson = parser.parse(xmlFile);
            logger.info("Successfully parsed lesson: \"{}\"", lesson.getTitle());

            // Write JSON
            logger.info("Writing JSON to: {}", jsonFile.toAbsolutePath());
            LessonJsonWriter writer = new LessonJsonWriter();
            writer.write(lesson, jsonFile);
            logger.info("Conversion complete! Output: {}", jsonFile.toAbsolutePath());

            System.out.println("Done! Converted " + xmlFile + " -> " + jsonFile);
            System.out.println("  Title: " + lesson.getTitle());
            System.out.println("  Chapters: " + lesson.getChapters().size());
            System.out.println("  Exercises: " + lesson.getExercises().size());

        } catch (Exception e) {
            logger.error("Conversion failed: {}", e.getMessage(), e);
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
