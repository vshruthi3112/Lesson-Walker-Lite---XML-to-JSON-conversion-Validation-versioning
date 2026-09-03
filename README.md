# Lesson Walker Lite — XML to JSON Converter

## What Is This Project?

This is a **command-line Java application** that reads lesson data from an XML file, validates it against an XSD schema, and converts it into a JSON file. Think of it as a data transformation tool with built-in quality checks:

```
lesson.xml  →  [Schema Validation]  →  [Lesson Walker Lite]  →  lesson.json
```

**Why would you do this?**
- XML is a common format for structured documents (used in publishing, education, enterprise systems)
- JSON is the standard for web APIs and modern applications
- Converting between formats is a very common real-world task
- Schema validation catches bad data before it causes downstream problems

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
    │   │   ├── Lesson.java                    # Data class for a lesson (with version)
    │   │   ├── Chapter.java                   # Data class for a chapter
    │   │   └── Exercise.java                  # Data class for an exercise
    │   ├── parser/
    │   │   └── LessonXmlParser.java           # Reads XML → Java objects
    │   ├── writer/
    │   │   └── LessonJsonWriter.java          # Writes Java objects → JSON
    │   ├── validator/
    │   │   └── LessonSchemaValidator.java     # Validates XML against XSD schema
    │   └── exception/
    │       ├── LessonWalkerException.java     # Base exception class
    │       ├── LessonValidationException.java # Schema validation errors
    │       ├── LessonParseException.java      # XML parsing errors
    │       └── LessonWriteException.java      # JSON writing errors
    └── resources/
        ├── logback.xml                        # Logging configuration
        ├── schema/
        │   └── lesson.xsd                     # XSD schema for lesson XML
        └── sample/
            └── lesson.xml                     # Sample input file for testing
```

---

## What's New in Week 10

### 1. Schema Validation (XSD)
Before parsing, the XML file is validated against `lesson.xsd`. This catches structural errors early — missing required elements, invalid attributes, wrong data types — with clear error messages listing every violation.

### 2. Version Support
The `version` attribute on `<lesson version="1.0">` is now parsed, logged, and included in the JSON output. This enables future format evolution while maintaining backward compatibility.

### 3. Custom Error Handling
Generic `Exception` replaced with a hierarchy of specific exceptions:
- `LessonValidationException` — schema validation failures (with all error details)
- `LessonParseException` — XML parsing issues
- `LessonWriteException` — JSON output failures
- `LessonWalkerException` — base class for all app errors

Each failure type produces a distinct exit code for scripting:
| Exit Code | Meaning |
|-----------|---------|
| 0 | Success |
| 1 | Bad arguments / missing input file |
| 2 | Schema validation failed |
| 3 | XML parsing failed |
| 4 | JSON writing failed |
| 5 | Unexpected application error |

### 4. Enhanced Logging
- Separate error log file (`logs/lesson-walker-errors.log`) for WARN+ messages
- Secure XML parsing (XXE attack prevention)
- Step-by-step progress reporting (Step 1/3, 2/3, 3/3)
- Thread name and full stack traces in log files
- All `System.out`/`System.err` replaced with proper logger calls

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
- `output/lesson.json` — Where to write the JSON output (optional, defaults to `output/lesson.json`)

### Expected Output
```
19:13:30 [INFO] === Lesson Walker Lite v2.0 started ===
19:13:30 [INFO] Step 1/3 - Validating XML schema...
19:13:30 [INFO] Schema validation passed for: src\main\resources\sample\lesson.xml
19:13:30 [INFO] Step 2/3 - Parsing XML: ...\lesson.xml
19:13:30 [INFO] Parsed lesson details:
19:13:30 [INFO]   Version:   1.0
19:13:30 [INFO]   Title:     Introduction to Java
19:13:30 [INFO]   Chapters:  3
19:13:30 [INFO]   Exercises: 4
19:13:30 [INFO] Step 3/3 - Writing JSON: ...\lesson.json
19:13:30 [INFO] === Conversion complete! ===
```

---

## Schema Validation (Week 10)

### What Is XSD?
XSD (XML Schema Definition) is a way to define **rules** for what valid XML looks like. It's like a blueprint that says "a lesson MUST have a title, chapters MUST have ids, exercise types can only be quiz/coding/practice."

### Our Schema Rules (`lesson.xsd`)

| Rule | What It Enforces |
|------|-----------------|
| `version` attribute required | Every `<lesson>` must declare its version (e.g., `1.0`) |
| `version` format | Must match pattern `X.Y` (digits.digits) |
| `<title>` required | Lesson and chapters must have non-empty titles |
| `<chapter id>` required | Every chapter needs a positive integer id |
| `<exercise id>` required | Every exercise needs a positive integer id |
| `<exercise type>` restricted | Must be one of: `quiz`, `coding`, `practice` |
| `<question>` required | Every exercise must have a question |
| `<answer>` and `<hint>` optional | These are allowed but not required |

### What Happens With Invalid XML

If you feed in XML that violates the schema, you get detailed error messages:
```
19:13:30 [ERROR] Schema validation failed: XML validation failed with 2 error(s)
19:13:30 [ERROR]   Validation error: Line 3: cvc-pattern-valid: Value 'abc' is not facet-valid...
19:13:30 [ERROR]   Validation error: Line 8: cvc-complex-type.4: Attribute 'id' must appear...
```

### Collecting Errors vs Failing Fast
Our validator collects **all** errors before reporting them, so you can fix everything in one pass instead of playing whack-a-mole one error at a time.

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
  "version": "1.0",
  "title": "Introduction to Java",
  "chapters": [
    { "id": 1, "title": "Getting Started", "content": "Java basics..." }
  ]
}
```

### 3. XSD Schema Validation

An XSD file defines the structure, data types, and constraints that valid XML must follow. Java's `javax.xml.validation` API validates XML against XSD at runtime.

```
XML File + XSD Schema → Validator → Pass/Fail (with error details)
```

### 4. DOM Parsing (Document Object Model)

DOM parsing loads the **entire XML file into memory** as a tree structure, with secure settings to prevent XXE (XML External Entity) attacks.

### 5. Custom Exception Hierarchy

```
LessonWalkerException (base)
├── LessonValidationException  (schema errors, carries error list)
├── LessonParseException       (XML structure errors)
└── LessonWriteException       (JSON output errors)
```

### 6. Maven, Jackson, SLF4J + Logback

Same libraries as Week 9, now with an additional error-only log file for production monitoring.

---

## Data Flow (How It All Connects)

```
┌──────────────────────────────────────────────────────────────────┐
│                         Main.java                                 │
│                                                                   │
│  1. Read command-line args (input.xml, output.json)              │
│  2. Validate input file exists                                    │
│  3. Call validator.validate(xmlFile)     ← NEW: Schema check     │
│  4. Call parser.parse(xmlFile)                                    │
│  5. Call writer.write(lesson, jsonFile)                           │
│  6. Log summary with version info        ← NEW: Version support  │
└──────┬──────────────┬──────────────────────┬─────────────────────┘
       │              │                      │
       ▼              ▼                      ▼
┌──────────────┐ ┌─────────────────┐  ┌──────────────────────────┐
│ Schema       │ │ LessonXmlParser │  │   LessonJsonWriter       │
│ Validator    │ │                 │  │                          │
│              │ │ XML File        │  │  Lesson object           │
│ XML + XSD   │ │  → DOM Document │  │   → ObjectMapper         │
│  → Pass/Fail│ │  → Extract data │  │   → JSON File            │
│  → Error    │ │  → Build Lesson │  │                          │
│    details  │ │    (w/ version) │  │                          │
└──────────────┘ └────────┬────────┘  └──────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────┐
│           Model Classes              │
│                                      │
│  Lesson                              │
│  ├── version: String    ← NEW       │
│  ├── title: String                   │
│  ├── chapters: List<Chapter>         │
│  │   └── Chapter {id, title, content}│
│  └── exercises: List<Exercise>       │
│      └── Exercise {id, type, ...}    │
└──────────────────────────────────────┘
```

---

## Key Java Concepts Demonstrated

| Concept | Where Used | What It Means |
|---------|-----------|---------------|
| Exception Hierarchy | `exception/` package | Custom exceptions for different failure types |
| XSD Validation | `LessonSchemaValidator` | Validate XML against a schema before parsing |
| XXE Prevention | `LessonXmlParser` | Disable external entities for security |
| Error Collecting | `CollectingErrorHandler` | Gather all errors instead of failing on the first |
| Exit Codes | `Main.java` | Different codes for different failure types |
| Inner Classes | `CollectingErrorHandler` | Helper class scoped inside its parent |
| Resource Loading | `getResourceAsStream` | Load files from the classpath (inside the JAR) |
| Packages | Every file | Organize classes into namespaces |
| Encapsulation | Model classes | Private fields + public getters/setters |
| Factory Pattern | `DocumentBuilderFactory` | Create configured objects |
| Annotations | `@JsonInclude`, `@Override` | Metadata that changes behavior |
| SLF4J Logging | Every class | Professional logging with levels |

---

## Quick Reference — Common Commands

```bash
# Build the project
mvn clean package -q

# Run with sample file
java -jar target/lesson-walker-lite-1.0-SNAPSHOT.jar src/main/resources/sample/lesson.xml output/lesson.json

# Run with default output (creates output/lesson.json)
java -jar target/lesson-walker-lite-1.0-SNAPSHOT.jar src/main/resources/sample/lesson.xml

# See usage help
java -jar target/lesson-walker-lite-1.0-SNAPSHOT.jar
```
