# TableTest Parser

TableTest Parser is a Java implementation for parsing the TableTest format. This format enables defining multiple test scenarios in tabular form, enhancing the readability, extensibility, and maintainability of tests. Test framework extensions use TableTest Parser to facilitate data-driven testing with the TableTest format.

## Table Format

Tables use pipe characters (`|`) to separate columns. The first line contains header descriptions, and subsequent lines represent individual test cases whose values are passed as arguments to the test method.

```tabletest
Augend | Addend | Sum?
2      | 3      | 5
0      | 0      | 0
1      | 1      | 2

```

Column values can be **single values**, **lists**, **sets**, or **maps**.

### Single-Value Format

Single values can appear with or without quotes. Unquoted values must not contain `[`, `|`, `,`, or `:` characters. These special characters require single or double quotes.

Whitespace around unquoted values is trimmed. To preserve leading or trailing whitespace, use quotes. Empty values are represented by adjacent quote pairs (`""` or `''`).

```tabletest
Value          | Length?
Hello world    | 11
"World, hello" | 12
'|'            | 1
""             | 0

```

### List Value Format

Lists are enclosed in square brackets with comma-separated elements. Lists can contain single values or compound values (nested lists/maps). Empty lists are represented by `[]`.

```tabletest
List             | Size?
[Hello, World]   | 2
["World, Hello"] | 1
['|', ",", abc]  | 3
[[1, 2], [3, 4]] | 2
[[a: 4], [b: 5]] | 2
[]               | 0

```

### Set Value Format

Sets are enclosed in curly braces with comma-separated elements. Sets can contain single values or compound values (nested lists/sets/maps). Empty sets are represented by `{}`.

```tabletest
Set              | Size?
{Hello, World}   | 2
{Hello, Hello}   | 1
{'|', "|", abc}  | 2
{[1, 2], [1, 2]} | 1
{[a: 4], [a: 4]} | 1
{}               | 0

```

### Map Value Format

Maps use square brackets with comma-separated key-value pairs. Keys and values are separated by colons. Keys must be unquoted single values, while values can be single or compound. Empty maps are represented by `[:]`.

```tabletest
Map                                      | Size?
[1: Hello, 2: World]                     | 2
[string: abc, list: [1, 2], map: [a: 4]] | 3
[:]                                      | 0

```

### Comments and Blank Lines

Lines starting with `//` (ignoring leading whitespace) are treated as comments and ignored during parsing. Comments allow adding explanations or temporarily disabling data rows.

Blank lines are also ignored and can be used to visually group related rows.

```tabletest
String      | Length?
Hello world | 11

// The next row is currently disabled
// "World, hello" | 12
    
// Special characters must be quoted
'|'         | 1
'[:]'       | 3

```

## Usage

TableParser converts strings in TableTest format into structured Table objects. TableTest format uses pipes (`|`) to separate columns and newlines to separate rows, with the first row treated as the header.

### Basic Usage

Here is an example of basic usage in Java:

```java
import io.github.nchaugen.tabletest.parser.Table; 
import io.github.nchaugen.tabletest.parser.TableParser;

// Parse a multi-line string in TableTest format
String tableText = """
    Name       | Age | Skills       | Attributes
    John Smith | 30  | [Java, SQL]  | [strength: 8, dexterity: 6]
    Jane Doe   | 28  | [Python, JS] | [wisdom: 9, charisma: 10]
    """;

Table table = TableParser.parse(tableText);

// Access table properties 
int rowCount = table.rowCount(); // 2 
int columnCount = table.columnCount(); // 4

// Access headers 
String nameHeader = table.header(0); // "Name"
List headers = table.headers(); // ["Name", "Age", "Skills", "Attributes"] 

// Access row and cell data - properly typed as String, List, or Map 
String name = (String) table.row(0).cell(0); // "John Smith"
int age = Integer.parseInt((String) table.row(0).cell(1)); // 30
List<String> skills = (List<String>) table.row(0).cell(2); // ["Java", "SQL"]
Map<String, String> attrs = (Map<String, String>) table.row(0).cell(3); // {"strength": "8", "dexterity": "6"}  

// Get all cells in a row 
List rowCells = table.row(1).cells(); // ["Jane Doe", "28", ["Python", "JS"], {"wisdom": "9", "charisma": "10"}]

// Process all rows in a functional style
table.map(row -> {
    String rowName = (String) row.cell(0);
    List<String> rowSkills = (List<String>) row.cell(2);
    return rowName + " has " + rowSkills.size() + " skills: " + String.join(", ", rowSkills);
}).forEach(System.out::println);
```

Similarly used from Kotlin:

```kotlin
import io.github.nchaugen.tabletest.parser.Table
import io.github.nchaugen.tabletest.parser.TableParser

// Parse a multi-line string in TableTest format
val tableText = """
    Name       | Age | Skills       | Attributes
    John Smith | 30  | [Java, SQL]  | [strength: 8, dexterity: 6]
    Jane Doe   | 28  | [Python, JS] | [wisdom: 9, charisma: 10]
""".trimIndent()

val table = TableParser.parse(tableText)

// Access table properties
val rowCount = table.rowCount()     // 2
val columnCount = table.columnCount() // 4

// Access headers
val nameHeader = table.header(0)  // "Name"
val headers = table.headers()  // ["Name", "Age", "Skills", "Attributes"]

// Access row and cell data - properly typed as String, List, or Map
val name = table.row(0).cell(0) as String  // "John Smith"
val age = (table.row(0).cell(1) as String).toInt()  // 30
val skills = table.row(0).cell(2) as List<String>  // ["Java", "SQL"]
val attrs = table.row(0).cell(3) as Map<String, String>  // {"strength": "8", "dexterity": "6"}

// Get all cells in a row
val rowCells = table.row(1).cells()  // ["Jane Doe", "28", ["Python", "JS"], {"wisdom": "9", "charisma": "10"}]

// Process all rows in a functional style
table.map { row ->
    val rowName = row.cell(0) as String
    val rowSkills = row.cell(2) as List<String>
    "$rowName has ${rowSkills.size} skills: ${rowSkills.joinToString()}"
}.forEach(::println)
```

### Data Types

TableTest supports three types of cell values:

1. **Simple Values**: Plain text or quoted text with `"` or `'`. Represented as `String`.
2. **Lists**: Values enclosed in square brackets like `[item1, item2]`. Represented as `List<Object>`.
3. **Sets**: Values enclosed in curly braces like `{item1, item2}`. Represented as `Set<Object>`.
4. **Maps**: Key-value pairs like `[key1: value1, key2: value2]`. Represented as `Map<String, Object>`.

Lists, sets, and maps can contain nested structures of any type, allowing for complex data representation.

### Special Features

- **Comments**: Lines beginning with `//` are ignored
- **Empty Lines**: Blank lines are ignored
- **Whitespace**: Leading/trailing whitespace in cells is trimmed
