# Lesson Walker Lite — XML to JSON Converter

## What Is This Project?

This is a **command-line Java application** that reads lesson data from an XML file and converts it into a JSON file. Think of it as a data transformation tool:

```
lesson.xml  →  [Lesson Walker Lite]  →  lesson.json
```

**Why would you do this?**
- XML is a common format for structured documents (used in publishing, education, enterprise systems)
- JSON is the standard for web APIs and modern applications
- Converting between formats is a very common real-world task

---

## Project Structure

```
XML to JSON- Validation & Versioning/
├── pom.xml                                    # Maven build configuration
├── .gitignore                                 # Files Git should ignore
├── README.md                                  # This file!
└── src/main/
    ├── java/com/lessonwalker/
    │   ├── Main.java                          # Entry point (runs the app)
    │   ├── model/
    │   │   ├── Lesson.java                    # Data class for a lesson
    │   │   ├── Chapter.java                   # Data class for a chapter
    │   │   └── Exercise.java                  # Data class for an exercise
    │   ├── parser/
    │   │   └── LessonXmlParser.java           # Reads XML → Java objects
    │   └── writer/
    │       └── LessonJsonWriter.java          # Writes Java objects → JSON
    └── resources/
        ├── logback.xml                        # Logging configuration
        └── sample/
            └── lesson.xml                     # Sample input file for testing
```

---

## How to Run

### Prerequisites
- Java 17 or higher installed
- Maven installed (for building)

### Steps

```bash
# 1. Navigate to the project folder
cd "XML to JSON- Validation & Versioning"

# 2. Build the project (compiles code + packages into a JAR)
mvn clean package -q

# 3. Run the conversion
java -jar target/lesson-walker-lite-1.0-SNAPSHOT.jar src/main/resources/sample/lesson.xml output/lesson.json
```

### What Each Argument Means
- `java -jar target/lesson-walker-lite-1.0-SNAPSHOT.jar` — Run the compiled application
- `src/main/resources/sample/lesson.xml` — The input XML file to read (required)
- `output/lesson.json` — Where to write the JSON output (optional, defaults to `lesson.json`)

### Expected Output
```
Done! Converted src\main\resources\sample\lesson.xml -> output\lesson.json
  Title: Introduction to Java
  Chapters: 3
  Exercises: 4
```

---

## Concepts & Technologies Used

### 1. XML (eXtensible Markup Language)

XML is a way to store structured data using tags (like HTML, but you define your own tag names).

```xml
<lesson version="1.0">
    <title>Introduction to Java</title>
    <chapters>
        <chapter id="1">
            <title>Getting Started</title>
            <content>Java basics...</content>
        </chapter>
    </chapters>
</lesson>
```

**Key XML terminology:**
- **Element**: A piece of data wrapped in tags — `<title>Introduction to Java</title>`
- **Attribute**: Extra info on an element — `id="1"` in `<chapter id="1">`
- **Nesting**: Elements inside elements — `<chapter>` is inside `<chapters>`
- **Root element**: The outermost element — `<lesson>` in our case

### 2. JSON (JavaScript Object Notation)

JSON is a lightweight format for data exchange, widely used in web APIs.

```json
{
  "title": "Introduction to Java",
  "chapters": [
    { "id": 1, "title": "Getting Started", "content": "Java basics..." }
  ]
}
```

**Key JSON terminology:**
- **Object**: Data in curly braces `{}` with key-value pairs
- **Array**: A list in square brackets `[]`
- **Key**: The name (always a string in quotes)
- **Value**: Can be string, number, boolean, object, array, or null

### 3. DOM Parsing (Document Object Model)

DOM parsing loads the **entire XML file into memory** as a tree structure. You can then navigate the tree to extract data.

```
Document (tree root)
└── <lesson>              ← root element
    ├── <title>
    ├── <chapters>
    │   ├── <chapter id="1">
    │   ├── <chapter id="2">
    │   └── <chapter id="3">
    └── <exercises>
        ├── <exercise id="1">
        └── ...
```

**When to use DOM:**
- Small to medium files (fits in memory)
- You need to navigate back and forth in the document
- Random access to elements

**Alternatives (not used here):**
- **SAX** — reads XML sequentially (event-driven), better for huge files
- **JAXB** — maps XML directly to Java objects using annotations (more magic, less control)

### 4. Maven (Build Tool)

Maven compiles your code, downloads libraries, and packages everything into a JAR.

**Key concepts in `pom.xml`:**
- `<dependencies>` — libraries your project uses
- `<plugins>` — tools that run during the build process
- `mvn clean package` — delete old build → compile → package into JAR

### 5. Jackson (JSON Library)

Jackson is the most popular Java library for working with JSON. It converts Java objects to JSON and back.

**Key class:** `ObjectMapper` — the main tool that does the conversion.

### 6. SLF4J + Logback (Logging)

Instead of `System.out.println()`, professional applications use a logging framework:
- **SLF4J** — the API (interface) you write code against
- **Logback** — the implementation that actually writes logs

**Why logging instead of println?**
- Log levels (DEBUG, INFO, WARN, ERROR) let you control verbosity
- Logs can go to files, not just the console
- You can turn logging on/off without changing code
- Timestamps and class names are added automatically

---

## File-by-File Explanation

---

### `pom.xml` — Maven Build Configuration

```xml
<?xml version="1.0" encoding="UTF-8"?>
```
Every XML file starts with this declaration. It says "this is XML, encoded in UTF-8."

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" ...>
    <modelVersion>4.0.0</modelVersion>
```
This is boilerplate — tells Maven the format version of this file.

```xml
    <groupId>com.lessonwalker</groupId>
    <artifactId>lesson-walker-lite</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>
```
- **groupId** — your organization identifier (like a namespace)
- **artifactId** — the project's unique name
- **version** — `SNAPSHOT` means "still in development"
- **packaging** — output format (JAR = Java ARchive, a zip of compiled classes)

```xml
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
```
- Compile using Java 17 features
- Source files are UTF-8 encoded

```xml
    <dependencies>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.15.2</version>
        </dependency>
```
**Jackson** — converts Java objects to/from JSON. We use a pinned version (2.15.2) so it doesn't change unexpectedly.

```xml
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>1.4.11</version>
        </dependency>
    </dependencies>
```
**Logback** — logging implementation. It automatically includes SLF4J (the logging API).

```xml
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.5.1</version>
                ...
                <mainClass>com.lessonwalker.Main</mainClass>
                ...
            </plugin>
        </plugins>
    </build>
```
**Shade plugin** creates a "fat JAR" — a single JAR file that includes all dependencies (Jackson, Logback, SLF4J). Without this, you'd get `ClassNotFoundException` at runtime because the dependencies wouldn't be in the JAR.

`<mainClass>` tells Java which class to run when you do `java -jar`.

---

### `Main.java` — Application Entry Point

```java
package com.lessonwalker;
```
**Package declaration.** This tells Java where this class lives in the project hierarchy. It must match the folder structure (`com/lessonwalker/`).

```java
import com.lessonwalker.parser.LessonXmlParser;
import com.lessonwalker.writer.LessonJsonWriter;
import com.lessonwalker.model.Lesson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
```
**Imports** — bring in classes we'll use. Like `#include` in C or `import` in Python.
- Our own classes (parser, writer, model)
- SLF4J for logging
- `java.nio.file` for modern file operations

```java
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
```
**Logger setup.** Every class gets its own logger. `LoggerFactory.getLogger(Main.class)` creates a logger named after this class, so log messages show where they came from.

- `private` — only this class can access it
- `static` — belongs to the class, not an instance
- `final` — can't be reassigned after creation

```java
    public static void main(String[] args) {
        logger.info("Lesson Walker Lite started");
```
**Entry point.** Java always starts at `public static void main(String[] args)`. The `args` array contains command-line arguments.

`logger.info(...)` logs at INFO level — important state changes worth recording.

```java
        if (args.length < 1) {
            System.out.println("Usage: java -jar lesson-walker-lite.jar <input.xml> [output.json]");
            System.exit(1);
        }
```
**Argument validation.** If no input file is provided, print usage instructions and exit with code 1 (error). Code 0 = success, anything else = failure.

```java
        String inputPath = args[0];
        String outputPath = args.length > 1 ? args[1] : "lesson.json";
```
- `args[0]` — first argument (the XML file path)
- Ternary operator `? :` — if a second argument exists, use it; otherwise default to "lesson.json"

```java
        Path xmlFile = Paths.get(inputPath);
        Path jsonFile = Paths.get(outputPath);
```
**Path objects.** `Paths.get()` converts a string into a `Path` object, which is Java's modern way to represent file locations. Better than raw strings because it handles OS differences (/ vs \).

```java
        if (!Files.exists(xmlFile)) {
            logger.error("Input file not found: {}", xmlFile.toAbsolutePath());
            System.err.println("Error: Input file not found: " + xmlFile.toAbsolutePath());
            System.exit(1);
        }
```
**File existence check.** Before trying to parse, verify the file actually exists. `toAbsolutePath()` shows the full path for easier debugging.

Note: `{}` in logger messages is a **placeholder** — SLF4J fills it in. This is faster than string concatenation because the string is only built if that log level is enabled.

```java
        try {
            LessonXmlParser parser = new LessonXmlParser();
            Lesson lesson = parser.parse(xmlFile);
```
**Parse the XML.** Create a parser object and call `parse()` which returns a `Lesson` object containing all the data from the XML file.

```java
            LessonJsonWriter writer = new LessonJsonWriter();
            writer.write(lesson, jsonFile);
```
**Write the JSON.** Create a writer and serialize the `Lesson` object to a JSON file.

```java
        } catch (Exception e) {
            logger.error("Conversion failed: {}", e.getMessage(), e);
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
```
**Error handling.** If anything goes wrong (bad XML, file permissions, etc.), catch the exception, log it with full stack trace (the third parameter `e`), show a clean error to the user, and exit.

---

### `model/Lesson.java` — Root Data Class

```java
public class Lesson {
    private String title;
    private List<Chapter> chapters;
    private List<Exercise> exercises;
```
**Fields.** These represent the data inside a lesson. They're `private` — only accessible through getter/setter methods (encapsulation).

- `List<Chapter>` — a list (dynamic array) that holds Chapter objects
- `List<Exercise>` — same but for exercises

```java
    public Lesson() {
        this.chapters = new ArrayList<>();
        this.exercises = new ArrayList<>();
    }
```
**No-argument constructor.** Initializes empty lists so we don't get `NullPointerException` when adding items later. Jackson also needs this to create objects during deserialization.

```java
    public Lesson(String title, List<Chapter> chapters, List<Exercise> exercises) {
        this.title = title;
        this.chapters = chapters != null ? chapters : new ArrayList<>();
        this.exercises = exercises != null ? exercises : new ArrayList<>();
    }
```
**Parameterized constructor.** For creating a Lesson with all data at once. The null check (`!= null ? x : new ArrayList<>()`) prevents null lists — defensive programming.

```java
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    // ... same pattern for chapters and exercises
```
**Getters and Setters.** Standard Java pattern for accessing private fields. Jackson uses these to read field values when writing JSON (it calls `getTitle()`, `getChapters()`, etc.).

```java
    @Override
    public String toString() {
        return "Lesson{title='" + title + "', chapters=" + chapters.size() + ", exercises=" + exercises.size() + "}";
    }
```
**toString()** — defines how this object looks when printed or logged. `@Override` tells the compiler we're intentionally replacing the default `Object.toString()`.

---

### `model/Chapter.java` — Chapter Data Class

```java
public class Chapter {
    private int id;
    private String title;
    private String content;
```
Simple data holder with three fields. Same getter/setter pattern as Lesson.

The `int id` field corresponds to the `id="1"` attribute in the XML. We parse the string "1" into an integer.

---

### `model/Exercise.java` — Exercise Data Class

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Exercise {
    private int id;
    private String type;
    private String question;
    private String answer;    // only for "quiz" type
    private String hint;      // only for "coding" type
```

**`@JsonInclude(JsonInclude.Include.NON_NULL)`** — This Jackson annotation says "when writing JSON, skip any field that is null." Without this, quiz exercises would show `"hint": null` and coding exercises would show `"answer": null` in the output. Cleaner JSON!

The `answer` and `hint` fields are **optional** — not every exercise has both. A quiz has an answer but no hint. A coding exercise has a hint but no answer.

---

### `parser/LessonXmlParser.java` — XML Parsing Logic

This is the core of the application. It reads an XML file and builds Java objects.

```java
public class LessonXmlParser {
    private static final Logger logger = LoggerFactory.getLogger(LessonXmlParser.class);
```
Logger for this class — messages will show `c.l.parser.LessonXmlParser` in logs.

```java
    public Lesson parse(Path xmlFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlFile.toFile());
```
**DOM parsing in 3 steps:**
1. `DocumentBuilderFactory.newInstance()` — creates a factory (configuration object)
2. `factory.newDocumentBuilder()` — creates a parser from the factory
3. `builder.parse(xmlFile.toFile())` — reads the XML file and builds a tree in memory

This is the **Factory Pattern** — a common Java design pattern where you use a factory to create objects instead of calling `new` directly. It allows configuration before creation.

```java
        document.getDocumentElement().normalize();
```
**Normalize** — merges adjacent text nodes and removes empty ones. XML can split text across multiple nodes; this cleans that up so `getTextContent()` works reliably.

```java
        Element root = document.getDocumentElement();
        if (!"lesson".equals(root.getTagName())) {
            throw new IllegalArgumentException("Root element must be <lesson>, found: <" + root.getTagName() + ">");
        }
```
**Validation.** Get the root element and verify it's `<lesson>`. If someone passes a completely different XML file, fail early with a clear message.

Note: `"lesson".equals(root.getTagName())` instead of `root.getTagName().equals("lesson")` — this prevents `NullPointerException` if `getTagName()` returns null (defensive programming).

```java
    private List<Chapter> parseChapters(Element root) {
        List<Chapter> chapters = new ArrayList<>();

        NodeList chaptersNodes = root.getElementsByTagName("chapters");
        if (chaptersNodes.getLength() == 0) {
            logger.warn("No <chapters> section found in XML");
            return chapters;
        }
```
**Find the `<chapters>` container.** `getElementsByTagName` returns all matching elements. If none found, log a warning and return an empty list (don't crash).

```java
        Element chaptersElement = (Element) chaptersNodes.item(0);
        NodeList chapterNodes = chaptersElement.getElementsByTagName("chapter");
```
Get the first `<chapters>` element, then find all `<chapter>` children inside it.

`(Element)` is a **cast** — `item(0)` returns a `Node`, but we need an `Element` (which is a subtype of Node that has extra methods like `getAttribute()`).

```java
        for (int i = 0; i < chapterNodes.getLength(); i++) {
            Element chapterEl = (Element) chapterNodes.item(i);
            Chapter chapter = new Chapter();

            String idAttr = chapterEl.getAttribute("id");
            if (!idAttr.isEmpty()) {
                chapter.setId(Integer.parseInt(idAttr));
            }

            chapter.setTitle(getTextContent(chapterEl, "title"));
            chapter.setContent(getTextContent(chapterEl, "content"));

            chapters.add(chapter);
        }
```
**Loop through each `<chapter>` element:**
1. Get the `id` attribute (it's a string in XML, so we parse it to int)
2. Get the `<title>` text content
3. Get the `<content>` text content
4. Add the chapter to our list

```java
    private String getTextContent(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return null;
        }
        String text = nodes.item(0).getTextContent();
        return text != null ? text.trim() : null;
    }
```
**Helper method** — finds a child element by tag name and returns its text content, trimmed of whitespace. Returns null if the element doesn't exist. This avoids repeating the same 4 lines of code for every field we extract.

---

### `writer/LessonJsonWriter.java` — JSON Output

```java
public class LessonJsonWriter {
    private final ObjectMapper objectMapper;

    public LessonJsonWriter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
```
**ObjectMapper** is Jackson's main class. It knows how to convert Java objects to JSON.

`INDENT_OUTPUT` — makes the JSON pretty-printed (with indentation and newlines). Without this, you'd get one giant line of JSON.

```java
    public void write(Lesson lesson, Path jsonFile) throws Exception {
        if (jsonFile.getParent() != null) {
            java.nio.file.Files.createDirectories(jsonFile.getParent());
        }

        objectMapper.writeValue(jsonFile.toFile(), lesson);
    }
```
**Writing process:**
1. Create parent directories if they don't exist (e.g., `output/` folder)
2. `writeValue()` — Jackson inspects the `Lesson` object, calls all its getters, and writes the JSON

**How Jackson converts objects to JSON:**
- Calls `lesson.getTitle()` → writes `"title": "Introduction to Java"`
- Calls `lesson.getChapters()` → writes `"chapters": [...]`
- For each Chapter, calls `getChapter.getId()`, `getTitle()`, `getContent()`
- Skips null fields on Exercise (because of `@JsonInclude(NON_NULL)`)

---

### `resources/logback.xml` — Logging Configuration

```xml
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%level] %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
```
**Console appender** — writes logs to the terminal (stdout).

**Pattern breakdown:**
- `%d{HH:mm:ss.SSS}` — timestamp (hour:minute:second.millisecond)
- `[%level]` — log level (DEBUG, INFO, WARN, ERROR)
- `%logger{36}` — class name (shortened to 36 chars)
- `%msg` — the actual message
- `%n` — newline

Example output: `11:18:53.039 [INFO] com.lessonwalker.Main - Lesson Walker Lite started`

```xml
    <appender name="FILE" class="ch.qos.logback.core.FileAppender">
        <file>logs/lesson-walker.log</file>
```
**File appender** — also writes logs to a file for later review.

```xml
    <logger name="com.lessonwalker" level="DEBUG" />
```
Our application code logs at DEBUG level (shows everything).

```xml
    <root level="INFO">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="FILE" />
    </root>
```
**Root logger** — everything else (libraries, framework) only logs INFO and above. Both appenders (console + file) are active.

---

### `resources/sample/lesson.xml` — Test Input File

This is the sample XML you feed to the application. It contains:
- A lesson titled "Introduction to Java"
- 3 chapters (Getting Started, Variables and Types, Control Flow)
- 4 exercises (2 quizzes with answers, 2 coding exercises with hints)

The `version="1.0"` attribute on `<lesson>` will be used in Week 10 for version support.

---

## Data Flow (How It All Connects)

```
┌─────────────────────────────────────────────────────────────────┐
│                        Main.java                                 │
│                                                                  │
│  1. Read command-line args (input.xml, output.json)             │
│  2. Validate input file exists                                   │
│  3. Call parser.parse(xmlFile)                                   │
│  4. Call writer.write(lesson, jsonFile)                          │
│  5. Print summary                                                │
└───────────────┬──────────────────────────┬──────────────────────┘
                │                          │
                ▼                          ▼
┌───────────────────────────┐  ┌──────────────────────────────┐
│    LessonXmlParser        │  │     LessonJsonWriter          │
│                           │  │                               │
│  XML File                 │  │  Lesson object                │
│    → DOM Document         │  │    → ObjectMapper             │
│    → Navigate elements    │  │    → JSON File                │
│    → Extract data         │  │                               │
│    → Build Lesson object  │  │                               │
└───────────────┬───────────┘  └───────────────────────────────┘
                │
                ▼
┌───────────────────────────┐
│      Model Classes         │
│                            │
│  Lesson                    │
│  ├── title: String         │
│  ├── chapters: List        │
│  │   └── Chapter           │
│  │       ├── id: int       │
│  │       ├── title: String │
│  │       └── content: Str  │
│  └── exercises: List       │
│      └── Exercise          │
│          ├── id: int       │
│          ├── type: String  │
│          ├── question: Str │
│          ├── answer: Str?  │
│          └── hint: Str?    │
└────────────────────────────┘
```

---

## Key Java Concepts Demonstrated

| Concept | Where Used | What It Means |
|---------|-----------|---------------|
| Packages | Every file | Organize classes into namespaces |
| Encapsulation | Model classes | Private fields + public getters/setters |
| Constructors | Model classes | Initialize objects when created |
| Lists & Generics | `List<Chapter>` | Type-safe collections |
| Null safety | `!= null ? x : default` | Prevent NullPointerException |
| Try-catch | Main.java | Handle errors gracefully |
| Factory Pattern | `DocumentBuilderFactory` | Create configured objects |
| Casting | `(Element) node` | Convert parent type to specific child type |
| Annotations | `@JsonInclude`, `@Override` | Metadata that changes behavior |
| Static final | Logger | Shared constant per class |
| Path API | `Paths.get()`, `Files` | Modern file operations (Java NIO) |
| SLF4J Logging | Every class | Professional logging with levels |

---

## What's Coming in Week 10

- **XSD Schema Validation** — define rules for valid XML structure and reject bad files before parsing
- **Version Support** — read the `version="1.0"` attribute and support multiple XML formats
- **Better Error Handling** — custom exceptions, specific error messages for each failure type
- **Enhanced Logging** — structured logging with context, performance timing

---

## Quick Reference — Common Commands

```bash
# Build the project
mvn clean package -q

# Run with sample file
java -jar target/lesson-walker-lite-1.0-SNAPSHOT.jar src/main/resources/sample/lesson.xml output/lesson.json

# Run with default output (creates lesson.json in current directory)
java -jar target/lesson-walker-lite-1.0-SNAPSHOT.jar src/main/resources/sample/lesson.xml

# See usage help
java -jar target/lesson-walker-lite-1.0-SNAPSHOT.jar
```
