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

Column values can be **strings**, **lists**, **sets**, or **maps**.

### String Format

String values can appear with or without quotes. Unquoted values must not contain `[`, `|`, `,`, or `:` characters. These special characters require single or double quotes.

Whitespace around unquoted values is trimmed. To preserve leading or trailing whitespace, use quotes. Empty values are represented by adjacent quote pairs (`""` or `''`).

```tabletest
Value          | Length?
Hello world    | 11
"World, hello" | 12
'|'            | 1
""             | 0

```

### List Value Format

Lists are enclosed in square brackets with comma-separated elements. Lists can contain string values or compound values (nested lists/sets/maps). Empty lists are represented by `[]`.

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

Sets are enclosed in curly braces with comma-separated elements. Sets can contain string values or compound values (nested lists/sets/maps). Empty sets are represented by `{}`.

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

Maps use square brackets with comma-separated key-value pairs. Colons separate keys and values. Keys must be unquoted strings, while values can be any value type. Empty maps are represented by `[:]`.

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

TableParser converts strings in TableTest format into structured Table objects, providing access to headers, rows, and typed cell values (String, List, Set, or Map). Comments (lines starting with `//`) and blank lines are ignored during parsing. Whitespace around cell values is trimmed.

### Java

```java
import io.github.nchaugen.tabletest.parser.Table;
import io.github.nchaugen.tabletest.parser.TableParser;

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
String name = (String) table.row(0).value(0); // "John Smith"
int age = Integer.parseInt((String) table.row(0).value(1)); // 30
List<String> skills = (List<String>) table.row(0).value(2); // ["Java", "SQL"]
Map<String, String> attrs = (Map<String, String>) table.row(0).value(3); // {"strength": "8", "dexterity": "6"}

// Get all values in a row
List rowCells = table.row(1).values(); // ["Jane Doe", "28", ["Python", "JS"], {"wisdom": "9", "charisma": "10"}]

// Process all rows in a functional style
table.map(row -> {
    String rowName = (String) row.value(0);
    List<String> rowSkills = (List<String>) row.value(2);
    return rowName + " has " + rowSkills.size() + " skills: " + String.join(", ", rowSkills);
}).forEach(System.out::println);
```

### Kotlin

```kotlin
import io.github.nchaugen.tabletest.parser.Table
import io.github.nchaugen.tabletest.parser.TableParser

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
val name = table.row(0).value(0) as String  // "John Smith"
val age = (table.row(0).value(1) as String).toInt()  // 30
val skills = table.row(0).value(2) as List<String>  // ["Java", "SQL"]
val attrs = table.row(0).value(3) as Map<String, String>  // {"strength": "8", "dexterity": "6"}

// Get all values in a row
val rowCells = table.row(1).values()  // ["Jane Doe", "28", ["Python", "JS"], {"wisdom": "9", "charisma": "10"}]

// Process all rows in a functional style
table.map { row ->
    val rowName = row.value(0) as String
    val rowSkills = row.value(2) as List<String>
    "$rowName has ${rowSkills.size} skills: ${rowSkills.joinToString()}"
}.forEach(::println)
```

### Preserving Quotes

By default, quotes are removed from string values during parsing. To preserve the original quotes, pass `true` as the second parameter:

```java
String tableText = """
    Type       | Value
    unquoted   | hello
    single     | 'world'
    double     | "test"
    """;

// Default behaviour: quotes removed
Table table = TableParser.parse(tableText);
table.row(1).value(1); // "world" (quotes removed)

// Preserve quotes
Table tableWithQuotes = TableParser.parse(tableText, true);
tableWithQuotes.row(1).value(1); // "'world'" (quotes preserved)
```

This is useful when testing quote-handling logic or when the presence of quotes is semantically significant.
