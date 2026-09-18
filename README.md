# String Combination Generator

A Java application for systematically generating string combinations around a user-defined base string.

The generator preserves the base string as a contiguous block and enumerates all possible values for the remaining character positions using a configurable character set.

The project demonstrates combinatorial generation, exhaustive search-space traversal, efficient file streaming, and the exponential growth of possible combinations as the target length increases.

The application is implemented in plain Java and requires no external libraries or build tools.

---

## Overview

The program accepts two primary inputs:

- **Base string** — a string that must remain intact in every generated result.
- **Target length** — the total length of each generated string.

For example:

```text
Base string: test
Target length: 7
```

The base string contains 4 characters, leaving 3 free positions:

```text
7 - 4 = 3
```

Because the base string remains contiguous, it can occupy multiple positions within the final string:

```text
test???
?test??
??test?
???test
```

Each `?` represents a position that is systematically filled using characters from the configured character set.

The generator continues until the complete configured combination space has been traversed.

---

## How It Works

The generator does not use random character selection.

Instead, it systematically enumerates the available character set for every free position.

Consider:

```text
Base string: test
Target length: 6
```

There are two free positions:

```text
6 - 4 = 2
```

The possible contiguous placements of the base string are:

```text
test??
?test?
??test
```

The remaining positions are then populated with combinations from the configured alphabet.

Example results could include:

```text
testaa
testaB
test19
test!@
atestb
1test9
!testA
69test
ABtest
@@test
```

The process is deterministic: every position is traversed systematically rather than populated randomly.

---

## Character Set

By default, the generator uses the following character set:

```text
abcdefghijklmnopqrstuvwxyz
ABCDEFGHIJKLMNOPQRSTUVWXYZ
0123456789
!#$@%
```

This provides:

```text
67 possible characters
```

for every generated position.

The character set can be modified in the Java source code for different generation requirements or experiments.

---

## Search-Space Calculation

The number of generated strings grows exponentially as the number of free positions increases.

For a character set containing `A` characters and `N` free positions, each fixed placement of the base string produces:

```text
A^N
```

possible combinations.

If the base string can occupy `P` contiguous positions within the target string, the generation space before accounting for possible duplicate outputs is:

```text
P × A^N
```

For example:

```text
Base string: test
Target length: 9

Base length: 4
Free positions: 5
Alphabet size: 67
Contiguous placements: 6
```

The theoretical generation count is:

```text
6 × 67^5
```

which results in billions of generated entries.

This demonstrates how quickly a combinatorial search space grows when only a few additional positions are introduced.

---

## Output

Generated strings are written to a text file inside the `output` directory.

The application writes each generated string directly to disk rather than storing the complete result set in memory.

This design makes it possible to process much larger generation spaces without requiring enough RAM to hold every generated string simultaneously.

---

## Streaming Instead of In-Memory Storage

A simple implementation could generate every string and store it in a collection such as an `ArrayList`.

For large combination spaces, this quickly becomes impractical.

Instead, this project follows a streaming approach:

```text
Generate combination
        |
        v
Write combination to file
        |
        v
Generate next combination
```

Only the data required for the current generation step needs to remain in memory.

As a result, the primary constraints for very large generation spaces become:

- Disk capacity
- Disk write performance
- CPU time
- Total number of combinations

---

## Working with Large Output Files

Generated files may contain millions or even billions of lines.

At that scale, attempting to open the entire file with a conventional text editor may consume excessive memory or cause the editor to become unresponsive.

On Linux and macOS, large files can instead be inspected incrementally.

### View the file without loading everything at once

```bash
less output/test_combinations.txt
```

### Display the first 20 entries

```bash
head -n 20 output/test_combinations.txt
```

### Display the last 20 entries

```bash
tail -n 20 output/test_combinations.txt
```

### Search for an exact generated string

```bash
grep -Fx 'testA69' output/test_combinations.txt
```

### Search for an exact string and display its line number

```bash
grep -nFx 'testA69' output/test_combinations.txt
```

These tools make it possible to inspect very large generated datasets without loading the entire file into memory.

---

## Project Structure

```text
string-combination-generator/
|
├── src/
│   └── Main.java
|
├── output/
│   └── .gitkeep
|
├── .gitignore
├── LICENSE
└── README.md
```

Generated `.txt` files should remain outside version control because they can become extremely large.

The repository's `.gitignore` should therefore exclude generated output while preserving the directory structure.

---

## Requirements

The project requires:

- Java Development Kit (JDK)
- A terminal or Java-compatible IDE

No Maven, Gradle, or third-party dependencies are required.

---

## Compilation

Clone the repository and enter the project directory:

```bash
git clone <repository-url>
cd string-combination-generator
```

Compile the Java source:

```bash
javac -d out src/Main.java
```

This creates the compiled classes inside the `out` directory.

---

## Running the Application

After compilation:

```bash
java -cp out Main
```

The application will request the base string:

```text
Enter base string: test
```

and the desired total length:

```text
Enter total length: 7
```

It then calculates the required generation space and begins writing combinations to the output file.

---

## Example

Example input:

```text
Base string: test
Target length: 6
```

Possible generated strings include:

```text
testaa
testaA
test01
test!@
atest9
Atest#
1testB
!test@
69test
ABtest
```

The base string remains intact while its position and the surrounding generated characters change systematically.

---

## Use Cases

The project can be used as a practical example of several programming and computer-science concepts:

- Combinatorial generation
- Exhaustive enumeration
- Search-space analysis
- Custom string generation
- Identifier and code generation
- Test-data generation
- Character-set traversal
- File streaming
- Large-file processing
- Iterative generation algorithms
- Memory-efficient data generation
- Computational complexity
- Disk I/O considerations

---

## Cybersecurity and Search-Space Concepts

Combinatorial generation is also relevant to cybersecurity education.

Password-search techniques can be understood as search-space problems. When every character of a string is unknown, the number of possibilities depends on both the character-set size and the total number of unknown positions.

If part of a string is already known or predictable, fewer positions need to be explored.

This project can therefore be used in controlled laboratory environments to demonstrate concepts such as:

- Exhaustive search
- Brute-force search spaces
- Password complexity
- Character-set size
- Password length
- Predictability and entropy
- The effect of known patterns on a search space
- The computational cost of exhaustive enumeration

It also demonstrates an important security principle: passwords constructed from predictable words, names, dates, personal information, or common patterns can significantly reduce the uncertainty an attacker would otherwise have to overcome.

Security experiments should only be performed against systems, accounts, datasets, and environments that you own or have explicit authorization to test.

---

## Performance Considerations

Combination counts grow exponentially.

Adding a single free position multiplies the search space by the size of the configured alphabet.

With the default 67-character alphabet:

```text
1 free position  -> 67 possibilities per placement
2 free positions -> 4,489 possibilities per placement
3 free positions -> 300,763 possibilities per placement
4 free positions -> 20,151,121 possibilities per placement
5 free positions -> 1,350,125,107 possibilities per placement
```

The number of possible placements of the base string must then also be considered.

For sufficiently large target lengths, generating every possible result can require substantial processing time and storage capacity.

This limitation is an inherent property of exhaustive combinatorial generation rather than a limitation specific to Java.

---

## Important Note About Generated Files

Large generated output files should not be committed to the repository.

A generation involving billions of strings can produce files tens of gigabytes in size.

The GitHub repository should primarily contain:

```text
Source code
Documentation
Configuration
License
```

rather than complete generated datasets.

Small example output files may be included when needed for demonstration or testing.

---

## License

This project is distributed under the MIT License.

See the [LICENSE](LICENSE) file for the complete license text.

---

## Disclaimer

This project is intended for software-development experimentation, computer-science education, combinatorial analysis, test-data generation, and authorized cybersecurity research.

The software should not be used to attempt unauthorized access to accounts, systems, networks, services, or data.
