package com.lessonwalker.validator;

import com.lessonwalker.exception.LessonValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates lesson XML files against the lesson.xsd schema.
 * Collects all validation errors before reporting them.
 */
public class LessonSchemaValidator {

    private static final Logger logger = LoggerFactory.getLogger(LessonSchemaValidator.class);
    private static final String SCHEMA_RESOURCE = "/schema/lesson.xsd";

    private final Schema schema;

    public LessonSchemaValidator() throws LessonValidationException {
        try (InputStream schemaStream = getClass().getResourceAsStream(SCHEMA_RESOURCE)) {
            if (schemaStream == null) {
                throw new LessonValidationException(
                        "Schema file not found on classpath: " + SCHEMA_RESOURCE,
                        List.of("Missing schema resource: " + SCHEMA_RESOURCE));
            }

            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            // Disable external access for security
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            this.schema = factory.newSchema(new StreamSource(schemaStream));
            logger.debug("Schema loaded successfully from {}", SCHEMA_RESOURCE);
        } catch (SAXException e) {
            throw new LessonValidationException(
                    "Failed to load XML schema: " + e.getMessage(),
                    List.of(e.getMessage()), e);
        } catch (IOException e) {
            throw new LessonValidationException(
                    "I/O error loading schema: " + e.getMessage(),
                    List.of(e.getMessage()), e);
        }
    }

    /**
     * Validates the given XML file against the lesson schema.
     * Collects all errors and throws a single exception with all of them.
     *
     * @param xmlFile path to the XML file to validate
     * @throws LessonValidationException if the file fails validation, containing all error details
     */
    public void validate(Path xmlFile) throws LessonValidationException {
        logger.info("Validating XML against schema: {}", xmlFile);
        List<String> errors = new ArrayList<>();

        try {
            Validator validator = schema.newValidator();
            // Disable external access for security
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            // Collect errors instead of failing on first one
            validator.setErrorHandler(new CollectingErrorHandler(errors));
            validator.validate(new StreamSource(xmlFile.toFile()));
        } catch (SAXException e) {
            errors.add("Fatal XML error: " + e.getMessage());
        } catch (IOException e) {
            throw new LessonValidationException(
                    "Cannot read XML file: " + xmlFile, List.of(e.getMessage()), e);
        }

        if (!errors.isEmpty()) {
            logger.error("Schema validation failed with {} error(s):", errors.size());
            for (String error : errors) {
                logger.error("  - {}", error);
            }
            throw new LessonValidationException(
                    "XML validation failed with " + errors.size() + " error(s)", errors);
        }

        logger.info("Schema validation passed for: {}", xmlFile);
    }

    /**
     * SAX error handler that collects all errors into a list
     * instead of stopping at the first one.
     */
    private static class CollectingErrorHandler implements org.xml.sax.ErrorHandler {

        private final List<String> errors;

        CollectingErrorHandler(List<String> errors) {
            this.errors = errors;
        }

        @Override
        public void warning(SAXParseException e) {
            // Warnings are logged but not treated as errors
            LoggerFactory.getLogger(LessonSchemaValidator.class)
                    .warn("Validation warning at line {}: {}", e.getLineNumber(), e.getMessage());
        }

        @Override
        public void error(SAXParseException e) {
            errors.add("Line " + e.getLineNumber() + ": " + e.getMessage());
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            errors.add("Fatal at line " + e.getLineNumber() + ": " + e.getMessage());
            throw e; // Fatal errors stop parsing
        }
    }
}
