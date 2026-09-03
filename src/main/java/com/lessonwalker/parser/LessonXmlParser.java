package com.lessonwalker.parser;

import com.lessonwalker.exception.LessonParseException;
import com.lessonwalker.model.Chapter;
import com.lessonwalker.model.Exercise;
import com.lessonwalker.model.Lesson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses a lesson XML file into a Lesson model object using DOM parsing.
 * Supports version attributes and provides detailed error messages.
 *
 * Expected XML structure:
 * <pre>
 * &lt;lesson version="1.0"&gt;
 *   &lt;title&gt;...&lt;/title&gt;
 *   &lt;chapters&gt;
 *     &lt;chapter id="1"&gt;
 *       &lt;title&gt;...&lt;/title&gt;
 *       &lt;content&gt;...&lt;/content&gt;
 *     &lt;/chapter&gt;
 *   &lt;/chapters&gt;
 *   &lt;exercises&gt;
 *     &lt;exercise id="1" type="quiz"&gt;
 *       &lt;question&gt;...&lt;/question&gt;
 *       &lt;answer&gt;...&lt;/answer&gt;
 *       &lt;hint&gt;...&lt;/hint&gt;
 *     &lt;/exercise&gt;
 *   &lt;/exercises&gt;
 * &lt;/lesson&gt;
 * </pre>
 */
public class LessonXmlParser {

    private static final Logger logger = LoggerFactory.getLogger(LessonXmlParser.class);

    /**
     * Parses the given XML file into a Lesson object.
     *
     * @param xmlFile path to the XML file
     * @return parsed Lesson object
     * @throws LessonParseException if the file cannot be parsed
     */
    public Lesson parse(Path xmlFile) throws LessonParseException {
        logger.debug("Starting XML parse of: {}", xmlFile);

        Document document = loadDocument(xmlFile);
        document.getDocumentElement().normalize();

        Element root = document.getDocumentElement();
        if (!"lesson".equals(root.getTagName())) {
            throw new LessonParseException(
                    "Root element must be <lesson>, found: <" + root.getTagName() + ">");
        }

        Lesson lesson = new Lesson();

        // Parse version attribute
        String version = root.getAttribute("version");
        if (version != null && !version.isEmpty()) {
            lesson.setVersion(version);
            logger.info("Lesson version: {}", version);
        } else {
            logger.warn("No version attribute found on <lesson> element");
        }

        // Parse title
        String title = getTextContent(root, "title");
        if (title == null || title.isEmpty()) {
            throw new LessonParseException("Lesson <title> is required but was empty or missing");
        }
        lesson.setTitle(title);
        logger.debug("Parsed lesson title: \"{}\"", title);

        // Parse chapters
        List<Chapter> chapters = parseChapters(root);
        lesson.setChapters(chapters);
        logger.info("Parsed {} chapter(s)", chapters.size());

        // Parse exercises
        List<Exercise> exercises = parseExercises(root);
        lesson.setExercises(exercises);
        logger.info("Parsed {} exercise(s)", exercises.size());

        logger.info("XML parsing complete: {}", lesson);
        return lesson;
    }

    /**
     * Loads and parses the XML file into a DOM Document with secure settings.
     * Disables external entities and DTDs to prevent XXE attacks.
     */
    private Document loadDocument(Path xmlFile) throws LessonParseException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            // Secure the parser against XXE attacks
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(xmlFile.toFile());
        } catch (ParserConfigurationException e) {
            throw new LessonParseException("XML parser configuration error: " + e.getMessage(), e);
        } catch (SAXException e) {
            throw new LessonParseException("XML syntax error in " + xmlFile + ": " + e.getMessage(), e);
        } catch (IOException e) {
            throw new LessonParseException("Cannot read XML file " + xmlFile + ": " + e.getMessage(), e);
        }
    }

    /**
     * Parses all chapter elements from the &lt;chapters&gt; section.
     */
    private List<Chapter> parseChapters(Element root) throws LessonParseException {
        List<Chapter> chapters = new ArrayList<>();

        NodeList chaptersNodes = root.getElementsByTagName("chapters");
        if (chaptersNodes.getLength() == 0) {
            logger.warn("No <chapters> section found in XML");
            return chapters;
        }

        Element chaptersElement = (Element) chaptersNodes.item(0);
        NodeList chapterNodes = chaptersElement.getElementsByTagName("chapter");

        for (int i = 0; i < chapterNodes.getLength(); i++) {
            Element chapterEl = (Element) chapterNodes.item(i);
            Chapter chapter = new Chapter();

            // Parse id attribute
            String idAttr = chapterEl.getAttribute("id");
            if (idAttr.isEmpty()) {
                throw new LessonParseException(
                        "Chapter at position " + (i + 1) + " is missing required 'id' attribute");
            }
            try {
                chapter.setId(Integer.parseInt(idAttr));
            } catch (NumberFormatException e) {
                throw new LessonParseException(
                        "Chapter id must be a number, got: '" + idAttr + "'", e);
            }

            // Parse child elements
            String chapterTitle = getTextContent(chapterEl, "title");
            if (chapterTitle == null || chapterTitle.isEmpty()) {
                throw new LessonParseException(
                        "Chapter " + idAttr + " is missing required <title>");
            }
            chapter.setTitle(chapterTitle);
            chapter.setContent(getTextContent(chapterEl, "content"));

            chapters.add(chapter);
            logger.debug("  Parsed chapter: {}", chapter);
        }

        return chapters;
    }

    /**
     * Parses all exercise elements from the &lt;exercises&gt; section.
     */
    private List<Exercise> parseExercises(Element root) throws LessonParseException {
        List<Exercise> exercises = new ArrayList<>();

        NodeList exercisesNodes = root.getElementsByTagName("exercises");
        if (exercisesNodes.getLength() == 0) {
            logger.warn("No <exercises> section found in XML");
            return exercises;
        }

        Element exercisesElement = (Element) exercisesNodes.item(0);
        NodeList exerciseNodes = exercisesElement.getElementsByTagName("exercise");

        for (int i = 0; i < exerciseNodes.getLength(); i++) {
            Element exerciseEl = (Element) exerciseNodes.item(i);
            Exercise exercise = new Exercise();

            // Parse id attribute
            String idAttr = exerciseEl.getAttribute("id");
            if (idAttr.isEmpty()) {
                throw new LessonParseException(
                        "Exercise at position " + (i + 1) + " is missing required 'id' attribute");
            }
            try {
                exercise.setId(Integer.parseInt(idAttr));
            } catch (NumberFormatException e) {
                throw new LessonParseException(
                        "Exercise id must be a number, got: '" + idAttr + "'", e);
            }

            // Parse type attribute
            String typeAttr = exerciseEl.getAttribute("type");
            if (typeAttr.isEmpty()) {
                throw new LessonParseException(
                        "Exercise " + idAttr + " is missing required 'type' attribute");
            }
            exercise.setType(typeAttr);

            // Parse child elements
            String question = getTextContent(exerciseEl, "question");
            if (question == null || question.isEmpty()) {
                throw new LessonParseException(
                        "Exercise " + idAttr + " is missing required <question>");
            }
            exercise.setQuestion(question);
            exercise.setAnswer(getTextContent(exerciseEl, "answer"));
            exercise.setHint(getTextContent(exerciseEl, "hint"));

            exercises.add(exercise);
            logger.debug("  Parsed exercise: {}", exercise);
        }

        return exercises;
    }

    /**
     * Helper: gets text content of the first child element with the given tag name.
     * Returns null if the element doesn't exist.
     */
    private String getTextContent(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return null;
        }
        String text = nodes.item(0).getTextContent();
        return text != null ? text.trim() : null;
    }
}
