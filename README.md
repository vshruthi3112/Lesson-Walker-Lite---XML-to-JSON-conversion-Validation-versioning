# Lesson Walker Lite — XML to JSON Converter

## What Is This Project?

This is a **command-line Java application** that reads lesson data from an XML file, validates it against an XSD schema, and converts it into a JSON file. Think of it as a data transformation tool with built-in quality checks:

```
lesson.xml
   │
   ▼
[LessonSchemaValidator]  ── Validates XML against lesson.xsd rules
   │                        (version format, required fields, allowed types)
   │                        Uses CollectingErrorHandler to gather ALL errors
   │
   ▼
[LessonXmlParser]        ── Reads validated XML using DOM parsing (XXE-safe)
   │                        Walks the DOM tree, extracts attributes & text
   │                        Builds Java objects: Lesson, Chapter, Exercise
   │
   ▼
[LessonJsonWriter]       ── Serializes Java objects to JSON using Jackson
   │                        ObjectMapper with pretty-printing enabled
   │                        @JsonInclude(NON_NULL) skips null optional fields
   │
   ▼
lesson.json
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

### Overview

The application is a 3-step pipeline. `Main.java` orchestrates the entire flow:

```
lesson.xml (text file on disk)
    │
    ▼
┌─ STEP 1: VALIDATE ─────────────────────────┐
│  lesson.xsd (rules) + lesson.xml (data)     │
│  → SchemaFactory compiles XSD               │
│  → Validator checks XML against rules       │
│  → CollectingErrorHandler gathers errors    │
│  → Pass: continue / Fail: exit(2)           │
└─────────────────────────────────────────────┘
    │ (XML is valid)
    ▼
┌─ STEP 2: PARSE ────────────────────────────┐
│  lesson.xml → DocumentBuilder (XXE-safe)    │
│  → DOM tree (Nodes in memory)               │
│  → Walk tree, extract attributes & text     │
│  → Build Lesson, Chapter, Exercise objects  │
└─────────────────────────────────────────────┘
    │ (Lesson object with all data)
    ▼
┌─ STEP 3: WRITE ────────────────────────────┐
│  Lesson object → ObjectMapper               │
│  → Reflection reads getters                 │
│  → @JsonInclude skips nulls                 │
│  → Pretty-print with indentation            │
│  → Write to output/lesson.json              │
└─────────────────────────────────────────────┘
    │
    ▼
lesson.json (formatted JSON file on disk)
```

The data changes form three times:
1. **XML text** → **DOM tree** (in-memory nodes) during parsing
2. **DOM tree** → **Java objects** (Lesson/Chapter/Exercise) during extraction
3. **Java objects** → **JSON text** during serialization

Each stage has its own error handling, its own exit code, and its own log messages. If anything breaks at any stage, the pipeline stops immediately with a clear explanation of what went wrong.

---

### Step 0: The User Runs the Command

```bash
java -jar lesson-walker-lite-1.0-SNAPSHOT.jar lesson.xml output/lesson.json
```

The JVM starts and calls `Main.main(args)` where `args = ["lesson.xml", "output/lesson.json"]`.

---

### Step 1: Main.java — Read and Validate Arguments

```java
String inputPath = args[0];                          // "lesson.xml"
String outputPath = args.length > 1 ? args[1] : "output/lesson.json";

Path xmlFile = Paths.get(inputPath);
Path jsonFile = Paths.get(outputPath);
```

Main does two safety checks before anything else:
- `Files.exists(xmlFile)` — does the file actually exist on disk?
- `Files.isReadable(xmlFile)` — can we read it (permissions)?

If either fails → log error, `System.exit(1)`. The app never touches the XML content. This is the cheapest possible check.

---

### Step 2: LessonSchemaValidator — Is the XML Structurally Correct?

```java
LessonSchemaValidator validator = new LessonSchemaValidator();
validator.validate(xmlFile);
```

This is a two-phase process:

**Phase A: Load the schema (happens in the constructor)**
```
lesson.xsd (from classpath via getResourceAsStream)
    ↓
SchemaFactory.newSchema()
    ↓
Schema object (compiled rules in memory)
```

The XSD file gets loaded from inside the JAR via `getClass().getResourceAsStream("/schema/lesson.xsd")`. The `SchemaFactory` compiles it into a `Schema` object — a "rule engine" that knows what valid XML looks like.

**Phase B: Validate the XML against those rules**
```
lesson.xml + Schema object
    ↓
Validator.validate()
    ↓
CollectingErrorHandler gathers errors into List<String>
    ↓
0 errors → return (pass)
N errors → throw LessonValidationException (with all N error messages)
```

The `CollectingErrorHandler` is an inner class that implements the SAX `ErrorHandler` interface with three methods:
- `warning()` → logs it, doesn't count as an error
- `error()` → adds `"Line 8: cvc-complex-type..."` to the error list, **keeps going**
- `fatalError()` → adds to list AND throws (can't continue past fatal XML issues)

After validation completes, if the error list is non-empty, it throws `LessonValidationException` carrying all the errors. Main catches this, logs every error, and exits with code 2.

If validation passes, the XML is **guaranteed** to have:
- A `version` attribute matching `\d+\.\d+`
- A non-empty `<title>`
- At least one `<chapter>` with a positive integer `id` and non-empty `<title>`
- Exercises (if present) with valid `type` (quiz/coding/practice) and `<question>`

---

### Step 3: LessonXmlParser — Turn XML Text Into Java Objects

```java
LessonXmlParser parser = new LessonXmlParser();
Lesson lesson = parser.parse(xmlFile);
```

Three sub-steps happen here:

**3a: Load XML into a DOM tree**

```
lesson.xml (text on disk)
    ↓
DocumentBuilderFactory (with XXE protections enabled)
    ↓
DocumentBuilder.parse()
    ↓
Document object (in-memory tree)
```

The XML file gets parsed into a tree of `Node` objects in memory. After calling `normalize()`, the tree looks like:

```
Document
└── Element: <lesson version="1.0">
    ├── Element: <title> → "Introduction to Java"
    ├── Element: <chapters>
    │   ├── Element: <chapter id="1">
    │   │   ├── Element: <title> → "Getting Started"
    │   │   └── Element: <content> → "Java is a..."
    │   ├── Element: <chapter id="2"> ...
    │   └── Element: <chapter id="3"> ...
    └── Element: <exercises>
        ├── Element: <exercise id="1" type="quiz">
        │   ├── Element: <question> → "What is the JVM?"
        │   └── Element: <answer> → "Java Virtual Machine..."
        └── ... more exercises
```

**Why XXE protection matters in this step**

Without the security settings in `loadDocument()`, an attacker could craft a malicious XML file like this:

```xml
<?xml version="1.0"?>
<!DOCTYPE lesson [
  <!ENTITY steal SYSTEM "file:///etc/passwd">
]>
<lesson version="1.0">
    <title>&steal;</title>
    ...
</lesson>
```

Here's what would happen with an **unprotected** parser:
1. The parser sees `<!DOCTYPE>` and processes the entity definition
2. `<!ENTITY steal SYSTEM "file:///etc/passwd">` tells the parser: "whenever you see `&steal;`, replace it with the contents of `/etc/passwd`"
3. When the parser reaches `<title>&steal;</title>`, it reads the actual password file from the server
4. That sensitive data ends up in the `title` field — and eventually in the JSON output

Our parser blocks this with these settings in `loadDocument()`:

```java
// Disallow DOCTYPE entirely — kills XXE at the root
factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

// Even if DOCTYPE slipped through, block external entity resolution
factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

// Block any external DTD or schema loading
factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
```

With these protections, if someone feeds in the malicious XML above, the parser immediately throws a `SAXException` ("DOCTYPE is disallowed") which gets wrapped in `LessonParseException` → exit code 3. The file is never read, the entity is never resolved, and no sensitive data leaks.

---

**3b: Extract data from the tree**

The parser walks this tree and pulls data out:

```java
Element root = document.getDocumentElement();        // <lesson>
String version = root.getAttribute("version");       // "1.0"
String title = getTextContent(root, "title");        // "Introduction to Java"

// Walk <chapters> → <chapter> elements
NodeList chapterNodes = chaptersElement.getElementsByTagName("chapter");
// For each: read id attribute, title element, content element
```

**3c: Build model objects**

As the parser extracts data, it constructs Java objects:

```
Lesson
├── version = "1.0"
├── title = "Introduction to Java"
├── chapters = [
│   Chapter{id=1, title="Getting Started", content="Java is a..."},
│   Chapter{id=2, title="Variables and Types", content="Java has..."},
│   Chapter{id=3, title="Control Flow", content="Learn about..."}
│ ]
└── exercises = [
    Exercise{id=1, type="quiz", question="What is the JVM?",
             answer="Java Virtual Machine...", hint=null},
    Exercise{id=2, type="coding", question="Write a Hello World...",
             answer=null, hint="Use System.out..."},
    Exercise{id=3, type="quiz", question="What is the difference...",
             answer="== compares...", hint=null},
    Exercise{id=4, type="coding", question="Write a for loop...",
             answer=null, hint="Use a for loop..."}
  ]
```

Note: some exercises have `answer=null`, others have `hint=null`. These are the optional fields that `@JsonInclude(NON_NULL)` handles in the next step.

---

### Step 4: LessonJsonWriter — Java Objects to JSON File

```java
LessonJsonWriter writer = new LessonJsonWriter();
writer.write(lesson, jsonFile);
```

**4a: Ensure the output directory exists**
```java
Files.createDirectories(jsonFile.getParent());   // creates "output/" if missing
```

**4b: Serialize using Jackson's ObjectMapper**
```
Lesson object
    ↓
ObjectMapper (with INDENT_OUTPUT enabled for pretty-printing)
    ↓
Jackson introspects the object using reflection:
    - Calls getVersion()   → writes "version": "1.0"
    - Calls getTitle()     → writes "title": "Introduction to Java"
    - Calls getChapters()  → iterates the list
        - For each Chapter: getId(), getTitle(), getContent()
    - Calls getExercises() → iterates the list
        - For each Exercise: getId(), getType(), getQuestion(), getAnswer(), getHint()
        - @JsonInclude(NON_NULL) → skips getAnswer()/getHint() when they return null
    ↓
Pretty-printed JSON written to output/lesson.json
```

---

### Step 5: Main.java — Log Summary and Exit

```java
logger.info("=== Conversion complete! {} -> {} ===", xmlFile, jsonFile);
```

Main logs the final summary and the JVM exits with code 0 (success).

---

### Error Handling at Each Stage

If anything breaks at any step, a specific exception is thrown and Main catches it:

| Stage | Exception | Exit Code | What Went Wrong |
|-------|-----------|-----------|-----------------|
| Arguments / file check | — (direct exit) | 1 | Missing args or file not found |
| Schema validation | `LessonValidationException` | 2 | XML violates XSD rules (with full error list) |
| XML parsing | `LessonParseException` | 3 | Malformed XML or missing required elements |
| JSON writing | `LessonWriteException` | 4 | Cannot create output file or serialization error |
| Anything else | `LessonWalkerException` | 5 | Unexpected application error |

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

## Testing Error Scenarios

These commands let you manually trigger each error path in `Main.java` to verify the logging and exit codes.

### Missing Arguments (Exit Code 1)
```bash
java -jar target/lesson-walker-lite-1.0-SNAPSHOT.jar
```

### File Not Found (Exit Code 1)
```bash
java -jar target/lesson-walker-lite-1.0-SNAPSHOT.jar nonexistent.xml
```

### File Not Readable (Exit Code 1) — Windows PowerShell
```powershell
# Create a dummy file
echo "<lesson></lesson>" > test-unreadable.xml

# Deny read permission for your user
icacls test-unreadable.xml /deny "${env:USERNAME}:(R)"

# Run the app — triggers the !Files.isReadable() branch
java -jar target/lesson-walker-lite-1.0-SNAPSHOT.jar test-unreadable.xml

# Clean up — restore permissions and delete
icacls test-unreadable.xml /grant "${env:USERNAME}:(R)"
del test-unreadable.xml
```

> **Note:** Run from a non-elevated (non-Admin) terminal. Administrator sessions may override deny rules.

### Schema Validation Failure (Exit Code 2)

Create a file with an invalid version format:
```xml
<!-- test-invalid.xml -->
<lesson version="abc">
    <title>Bad Lesson</title>
    <chapters>
        <chapter id="1"><title>Ch1</title><content>text</content></chapter>
    </chapters>
</lesson>
```
```bash
java -jar target/lesson-walker-lite-1.0-SNAPSHOT.jar test-invalid.xml
```

### XML Parse Failure (Exit Code 3)

Exit code 3 is normally unreachable because the XSD validator (Step 1) catches structural issues before the parser (Step 2) runs. This is defense-in-depth by design. To demo it, temporarily bypass validation.

**Step 1:** Comment out validation in `Main.java`:
```java
// logger.info("Step 1/3 - Validating XML schema...");
// LessonSchemaValidator validator = new LessonSchemaValidator();
// validator.validate(xmlFile);
```

**Step 2:** Create `src/main/resources/sample/test-parse-fail.xml` with a wrong root element:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<book version="1.0">
    <title>Not a lesson</title>
</book>
```

**Step 3:** Rebuild and run:
```powershell
mvn clean package -q
java -jar target/lesson-walker-lite-1.0-SNAPSHOT.jar src/main/resources/sample/test-parse-fail.xml
```

Expected output:
```
[ERROR] XML parsing failed: Root element must be <lesson>, found: <book>
```

**Step 4:** Uncomment the validation lines in `Main.java` and rebuild when done.

### JSON Write Failure (Exit Code 4)

Point the output to a path you don't have write access to. No code changes needed.

```powershell
java -jar target/lesson-walker-lite-1.0-SNAPSHOT.jar src/main/resources/sample/lesson.xml C:/Windows/System32/lesson.json
```

Expected output:
```
[ERROR] JSON writing failed: Failed to write JSON to C:\Windows\System32\lesson.json: Access is denied
```

Parsing succeeds, but Windows blocks writing to `System32`, triggering `LessonWriteException` → exit code 4.

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

# Missing or unreadable file
java -jar target/lesson-walker-lite-1.0-SNAPSHOT.jar src/main/resources/sample/nonexistent.xml output/lesson.json
```
