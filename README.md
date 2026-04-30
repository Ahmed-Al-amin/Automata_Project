# Automata Project

This project implements various automata simulators as required for CSE432: Automata and Computability.

## Project Structure

- `src/main/java/org/example/demo2/AutomataUI.java`: The JavaFX GUI application.
- `src/main/java/org/example/demo2/AutomataLogic.java`: The core logic for CFG to PDA conversion, DFA, and PDA (aⁿbⁿ).
- `pom.xml`: Maven configuration file with JavaFX dependencies.

## How to Run

### Using Maven (Recommended)

If you have Maven installed, you can run the GUI application directly using:

```bash
mvn javafx:run
```

### Using an IDE (IntelliJ IDEA / Eclipse / VS Code)

1. Open the project folder in your IDE.
2. The IDE should automatically detect the `pom.xml` and download the necessary JavaFX dependencies.
3. Run the `AutomataUI` class.

### Manual Compilation (Legacy)

If you don't use Maven, you will need to download the JavaFX SDK and use the following commands:

**Compile:**
```bash
javac --module-path /path/to/javafx/lib --add-modules javafx.controls src/main/java/org/example/demo2/*.java
```

**Run:**
```bash
java --module-path /path/to/javafx/lib --add-modules javafx.controls -cp src/main/java org.example.demo2.AutomataUI
```

## Features

1. **CFG → PDA**: Converts a Context-Free Grammar into a Pushdown Automaton.
2. **DFA**: Simulates a DFA that accepts strings over {0,1} where the number of 1s is divisible by 3 and ends with 0.
3. **PDA (aⁿbⁿ)**: Simulates a PDA for the language L = { aⁿbⁿ | n ≥ 0 }.
