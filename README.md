# Automata Project - CSE432

A comprehensive JavaFX-based suite for simulating and visualizing formal languages and automata, developed for the **CSE432: Automata and Computability** course.

## 🚀 Features

### 1. Context-Free Grammar (CFG) → PDA
- **Conversion**: Automatically convert any valid CFG into its equivalent Pushdown Automaton.
- **Cross-Validation**: Test an input string against both the original Grammar and the generated PDA to verify conversion accuracy.
- **Trace Support**: View a detailed step-by-step execution history of the PDA.
- **Export**: Export generated PDA transitions to a text file.

### 2. Deterministic Finite Automata (DFA)
- **Specific Logic**: Strictly implements the DFA for strings where the number of `1`s is divisible by 3 and the string ends with `0`.
- **Static Visualization**: View the formal 7-state diagram of the constructed DFA.
- **Interactive Debugger**: Animate the processing of an input string character-by-character, with real-time state highlighting on the diagram.

### 3. Pushdown Automata (PDA) for $L = \{a^n b^n \mid n \geq 0\}$
- **Formal Implementation**: Uses a transition-based PDA engine with stack-based acceptance.
- **Static Visualization**: View the formal diagram with states $q_1, q_2$, and $q_3$.
- **Interactive Debugger**: Step through `a`s and `b`s with a **Live Stack Visualization** to see pushes and pops in real-time.

## 🛠 Project Structure

The project has been refactored into a modular architecture for better maintainability:
- `AutomataUI.java`: Main JavaFX interface.
- `DFAVisualDebugger.java` / `PDAVisualDebugger.java`: Interactive animation engines.
- `CFGValidator.java` / `PDA.java` / `DFA.java`: Core mathematical simulation engines.
- `DFABuilder.java` / `PDA_anbn.java`: Construction of specific automata models.

## 🏃 How to Run

### Using Maven (Recommended)
```bash
mvn javafx:run
```

### Using an IDE
1. Open the project folder (IntelliJ, Eclipse, or VS Code).
2. Wait for Maven to sync dependencies.
3. Run the `AutomataUI` class.

## 📋 Requirements
- Java 17 or higher
- Maven 3.6+
- JavaFX 17 (managed via Maven)

---
*Developed for the Faculty of Engineering, Ain Shams University - Spring 2026.*
