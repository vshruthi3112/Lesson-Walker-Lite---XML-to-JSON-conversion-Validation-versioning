package com.lessonwalker;

import com.lessonwalker.exception.LessonParseException;
import com.lessonwalker.exception.LessonValidationException;
import com.lessonwalker.exception.LessonWalkerException;
import com.lessonwalker.exception.LessonWriteException;
import com.lessonwalker.model.Lesson;
import com.lessonwalker.parser.LessonXmlParser;
import com.lessonwalker.validator.LessonSchemaValidator;
import com.lessonwalker.writer.LessonJsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Entry point for the Lesson Walker Lite application.
 * Reads a lesson.xml file, validates it against the XSD schema,
 * and converts it to lesson.json.
 *
 * Usage: java -jar lesson-walker-lite.jar &lt;input.xml&gt; [output.json]
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String APP_VERSION = "2.0";

    public static void main(String[] args) {
        logger.info("=== Lesson Walker Lite v{} started ===", APP_VERSION);

        if (args.length < 1) {
            logger.error("No input file specified");
            logger.info("Usage: java -jar lesson-walker-lite.jar <input.xml> [output.json]");
            logger.info("  <input.xml>   - Path to the lesson XML file (required)");
            logger.info("  [output.json] - Path for the output JSON file (optional, defaults to output/lesson.json)");
            System.exit(1);
        }

        String inputPath = args[0];
        String outputPath = args.length > 1 ? args[1] : "output/lesson.json";

        Path xmlFile = Paths.get(inputPath);
        Path jsonFile = Paths.get(outputPath);

        // Validate input file exists
        if (!Files.exists(xmlFile)) {
            logger.error("Input file not found: {}", xmlFile.toAbsolutePath());
            System.exit(1);
        }

        if (!Files.isReadable(xmlFile)) {
            logger.error("Input file is not readable: {}", xmlFile.toAbsolutePath());
            System.exit(1);
        }

        try {
            // Step 1: Schema validation
            logger.info("Step 1/3 - Validating XML schema...");
            LessonSchemaValidator validator = new LessonSchemaValidator();
            validator.validate(xmlFile);

            // Step 2: Parse XML
            logger.info("Step 2/3 - Parsing XML: {}", xmlFile.toAbsolutePath());
            LessonXmlParser parser = new LessonXmlParser();
            Lesson lesson = parser.parse(xmlFile);

            logger.info("Parsed lesson details:");
            logger.info("  Version:   {}", lesson.getVersion());
            logger.info("  Title:     {}", lesson.getTitle());
            logger.info("  Chapters:  {}", lesson.getChapters().size());
            logger.info("  Exercises: {}", lesson.getExercises().size());

            // Step 3: Write JSON
            logger.info("Step 3/3 - Writing JSON: {}", jsonFile.toAbsolutePath());
            LessonJsonWriter writer = new LessonJsonWriter();
            writer.write(lesson, jsonFile);

            logger.info("=== Conversion complete! {} -> {} ===", xmlFile, jsonFile);

        } catch (LessonValidationException e) {
            logger.error("Schema validation failed: {}", e.getMessage());
            for (String error : e.getValidationErrors()) {
                logger.error("  Validation error: {}", error);
            }
            System.exit(2);

        } catch (LessonParseException e) {
            logger.error("XML parsing failed: {}", e.getMessage());
            if (e.getCause() != null) {
                logger.debug("Parse error cause: ", e.getCause());
            }
            System.exit(3);

        } catch (LessonWriteException e) {
            logger.error("JSON writing failed: {}", e.getMessage());
            if (e.getCause() != null) {
                logger.debug("Write error cause: ", e.getCause());
            }
            System.exit(4);

        } catch (LessonWalkerException e) {
            logger.error("Unexpected application error: {}", e.getMessage(), e);
            System.exit(5);
        }
    }
}
