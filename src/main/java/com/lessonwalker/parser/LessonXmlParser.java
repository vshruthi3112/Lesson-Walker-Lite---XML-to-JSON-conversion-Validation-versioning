package com.lessonwalker.parser;

import com.lessonwalker.model.Chapter;
import com.lessonwalker.model.Exercise;
import com.lessonwalker.model.Lesson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses a lesson XML file into a Lesson model object using DOM parsing.
 * 
 * Expected XML structure:
 * <pre>
 * &lt;lesson&gt;
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
     * @throws Exception if the file cannot be parsed
     */
    public Lesson parse(Path xmlFile) throws Exception {
        logger.debug("Starting XML parse of: {}", xmlFile);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlFile.toFile());

        // Normalize the document structure
        document.getDocumentElement().normalize();

        Element root = document.getDocumentElement();
        if (!"lesson".equals(root.getTagName())) {
            throw new IllegalArgumentException("Root element must be <lesson>, found: <" + root.getTagName() + ">");
        }

        Lesson lesson = new Lesson();

        // Parse title
        String title = getTextContent(root, "title");
        lesson.setTitle(title);
        logger.debug("Parsed lesson title: \"{}\"", title);

        // Parse chapters
        List<Chapter> chapters = parseChapters(root);
        lesson.setChapters(chapters);
        logger.debug("Parsed {} chapters", chapters.size());

        // Parse exercises
        List<Exercise> exercises = parseExercises(root);
        lesson.setExercises(exercises);
        logger.debug("Parsed {} exercises", exercises.size());

        logger.info("XML parsing complete: {}", lesson);
        return lesson;
    }

    /**
     * Parses all chapter elements from the <chapters> section.
     */
    private List<Chapter> parseChapters(Element root) {
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
            if (!idAttr.isEmpty()) {
                chapter.setId(Integer.parseInt(idAttr));
            }

            // Parse child elements
            chapter.setTitle(getTextContent(chapterEl, "title"));
            chapter.setContent(getTextContent(chapterEl, "content"));

            chapters.add(chapter);
            logger.debug("  Parsed chapter: {}", chapter);
        }

        return chapters;
    }

    /**
     * Parses all exercise elements from the <exercises> section.
     */
    private List<Exercise> parseExercises(Element root) {
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

            // Parse attributes
            String idAttr = exerciseEl.getAttribute("id");
            if (!idAttr.isEmpty()) {
                exercise.setId(Integer.parseInt(idAttr));
            }

            String typeAttr = exerciseEl.getAttribute("type");
            if (!typeAttr.isEmpty()) {
                exercise.setType(typeAttr);
            }

            // Parse child elements
            exercise.setQuestion(getTextContent(exerciseEl, "question"));
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
        logger.debug("Getting text content for tag: {}", tagName);
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return null;
        }
        // Get the first direct or nested match
        String text = nodes.item(0).getTextContent();
        return text != null ? text.trim() : null;
    }
}
