package org.example.demo2;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.*;

public class AutomataUI extends Application {

    private TextArea outputArea;
    private TextField inputField;
    private TextField cfgInputField;
    private TextArea grammarArea;

    // Action Buttons made class fields for easy visibility management
    private Button traceBtn;
    private Button exportBtn;
    private Button viewDfaBtn;
    private Button debugDfaBtn;
    private Button viewPdaBtn;
    private Button debugPdaBtn;
    private Button runBtn;

    private static Button selectedButton = null;
    private String selectedMode = "";
    private ValidationResult lastResult = null;
    private PDA lastPDA = null;

    @Override
    public void start(Stage stage) {

        stage.setTitle("Automata Simulator Pro");

        // ===== SIDEBAR =====
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: #e0e0e0;");

        Button cfgBtn = createSidebarButton("CFG → PDA");
        Button dfaBtn = createSidebarButton("DFA");
        Button pdaBtn = createSidebarButton("PDA (aⁿbⁿ)");

        sidebar.getChildren().addAll(cfgBtn, dfaBtn, pdaBtn);

        // ===== MAIN CONTENT =====
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label title = new Label("Automata Simulator");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        // ===== INPUTS =====
        inputField = new TextField();
        inputField.setPromptText("Enter input string...");
        VBox stringInputBox = new VBox(5, new Label("Input String:"), inputField);

        grammarArea = new TextArea();
        grammarArea.setPromptText("S -> aSb | ε\nA -> aS | b");
        grammarArea.setPrefHeight(120);
        grammarArea.setStyle("-fx-font-family: Consolas;");

        cfgInputField = new TextField();
        cfgInputField.setPromptText("Test a string against this grammar...");
        VBox cfgTestBox = new VBox(5, new Label("Test String (Optional):"), cfgInputField);

        Label grammarNote = new Label("Note: UPPERCASE = Non-Terminal, lowercase = Terminal, ε = Epsilon");
        grammarNote.setStyle("-fx-font-size: 11px; -fx-text-fill: #555555; -fx-font-style: italic;");

        Button epsilonBtn = new Button("Insert ε");
        epsilonBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #e1e1e1;");
        epsilonBtn.setOnAction(e -> {
            grammarArea.insertText(grammarArea.getCaretPosition(), "ε");
            grammarArea.requestFocus();
        });

        HBox grammarLabelRow = new HBox(10, new Label("CFG Grammar:"), epsilonBtn);
        grammarLabelRow.setAlignment(Pos.CENTER_LEFT);

        VBox grammarInputBox = new VBox(10, grammarLabelRow, grammarNote, grammarArea, cfgTestBox);

        grammarInputBox.setVisible(false);
        grammarInputBox.setManaged(false);
        stringInputBox.setVisible(false);
        stringInputBox.setManaged(false);

        // ===== BUTTONS =====
        runBtn = new Button("Run");
        Button clearBtn = new Button("Clear");
        traceBtn = new Button("Show Trace");
        exportBtn = new Button("💾 Export Transitions");
        viewDfaBtn = new Button("👁 View DFA Diagram");
        debugDfaBtn = new Button("🐛 Debug DFA Step-by-Step");
        viewPdaBtn = new Button("👁 View PDA Diagram");
        debugPdaBtn = new Button("🐛 Debug PDA Step-by-Step");

        stylePrimaryButton(runBtn);
        styleSecondaryButton(clearBtn);
        styleSecondaryButton(traceBtn);
        styleSecondaryButton(exportBtn);
        styleSecondaryButton(viewDfaBtn);
        styleSecondaryButton(debugDfaBtn);
        styleSecondaryButton(viewPdaBtn);
        styleSecondaryButton(debugPdaBtn);

        HBox controls = new HBox(10, runBtn, clearBtn, traceBtn, exportBtn, viewDfaBtn, debugDfaBtn, viewPdaBtn, debugPdaBtn);
        refreshButtonVisibility(); // Initial hide

        // ===== OUTPUT =====
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefHeight(300);
        outputArea.setStyle("-fx-font-family: Consolas;");

        VBox outputCard = new VBox(10, new Label("Output"), outputArea);
        outputCard.setPadding(new Insets(10));
        outputCard.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 10;");

        content.getChildren().addAll(title, grammarInputBox, stringInputBox, controls, outputCard);

        BorderPane root = new BorderPane();
        root.setLeft(sidebar);
        root.setCenter(content);
        root.setStyle("-fx-base: #f4f4f4; -fx-control-inner-background: #ffffff;");

        // ===== LOGIC =====
        cfgBtn.setOnAction(e -> {
            highlight(cfgBtn);
            selectedMode = "CFG";
            grammarInputBox.setVisible(true); grammarInputBox.setManaged(true);
            stringInputBox.setVisible(false); stringInputBox.setManaged(false);
            resetExecutionState();
        });

        dfaBtn.setOnAction(e -> {
            highlight(dfaBtn);
            selectedMode = "DFA";
            grammarInputBox.setVisible(false); grammarInputBox.setManaged(false);
            stringInputBox.setVisible(true); stringInputBox.setManaged(true);
            resetExecutionState();
        });

        pdaBtn.setOnAction(e -> {
            highlight(pdaBtn);
            selectedMode = "PDA";
            grammarInputBox.setVisible(false); grammarInputBox.setManaged(false);
            stringInputBox.setVisible(true); stringInputBox.setManaged(true);
            resetExecutionState();
        });

        viewDfaBtn.setOnAction(e -> DFAVisualizer.showDFA());
        
        debugDfaBtn.setOnAction(e -> {
            String input = inputField.getText().trim();
            if (input.isEmpty() || !input.matches("[01]+")) {
                outputArea.setText("Please enter a valid binary string first.");
                return;
            }
            new DFAVisualDebugger(input, DFABuilder.build()).show();
        });

        viewPdaBtn.setOnAction(e -> PDAVisualizer.showPDA());
        debugPdaBtn.setOnAction(e -> {
            String input = inputField.getText().trim();
            if (input.isEmpty() || !input.matches("[ab]+")) {
                outputArea.setText("Please enter a valid 'ab' string first.");
                return;
            }
            new PDAVisualDebugger(input, PDA_anbn.build()).show();
        });

        runBtn.setOnAction(e -> {
            handleRun(selectedMode);
            refreshButtonVisibility();
        });

        traceBtn.setOnAction(e -> {
            if (lastResult != null && lastResult.trace != null) {
                StringBuilder sb = new StringBuilder("--- Execution Trace ---\n");
                for (String step : lastResult.trace) sb.append(step).append("\n");
                outputArea.setText(sb.toString());
            }
        });

        exportBtn.setOnAction(e -> {
            if (lastPDA != null) {
                StringBuilder sb = new StringBuilder("PDA Transitions Export:\n\n");
                for (PDATransition t : lastPDA.transitions) sb.append(t).append("\n");
                saveToFile(sb.toString());
            }
        });

        clearBtn.setOnAction(e -> {
            grammarArea.clear();
            inputField.clear();
            cfgInputField.clear();
            outputArea.clear();
            resetExecutionState();
        });

        Scene scene = new Scene(root, 900, 600);
        stage.setScene(scene);
        stage.show();
    }

    private void resetExecutionState() {
        lastResult = null;
        lastPDA = null;
        outputArea.clear();
        refreshButtonVisibility();
    }

    private void refreshButtonVisibility() {
        // Trace button: visible if we have a result that contains a trace
        boolean hasTrace = (lastResult != null && lastResult.trace != null && !lastResult.trace.isEmpty());
        traceBtn.setVisible(hasTrace);
        traceBtn.setManaged(hasTrace);

        // Export button: only in CFG mode if a PDA was successfully generated
        boolean canExport = (selectedMode.equals("CFG") && lastPDA != null);
        exportBtn.setVisible(canExport);
        exportBtn.setManaged(canExport);

        // DFA Visualizers: only in DFA mode
        boolean isDfaMode = selectedMode.equals("DFA");
        viewDfaBtn.setVisible(isDfaMode);
        viewDfaBtn.setManaged(isDfaMode);
        debugDfaBtn.setVisible(isDfaMode);
        debugDfaBtn.setManaged(isDfaMode);

        // PDA Visualizers: only in PDA mode
        boolean isPdaMode = selectedMode.equals("PDA");
        viewPdaBtn.setVisible(isPdaMode);
        viewPdaBtn.setManaged(isPdaMode);
        debugPdaBtn.setVisible(isPdaMode);
        debugPdaBtn.setManaged(isPdaMode);
        
        System.out.println("DEBUG: Mode=" + selectedMode + ", hasTrace=" + hasTrace + ", lastResult=" + (lastResult != null));
    }

    private void handleRun(String choice) {
        // Clear previous results at the start of a new run
        lastResult = null; 
        
        if (choice.isEmpty()) {
            outputArea.setText("Please select an operation.");
            return;
        }

        String input = inputField.getText().trim();

        if (choice.equals("CFG")) {
            String grammarText = grammarArea.getText().trim();
            if (grammarText.isEmpty()) {
                outputArea.setText("Enter a grammar.");
                return;
            }
            CFG cfg = parseGrammar(grammarText);
            lastPDA = CFGtoPDAConverter.convert(cfg);
            
            StringBuilder result = new StringBuilder();
            String testString = cfgInputField.getText().trim();
            if (!testString.isEmpty()) {
                ValidationResult cfgRes = CFGValidator.validate(cfg, testString);
                ValidationResult pdaRes = lastPDA.validate(testString);
                result.append("Grammar Result: ").append(cfgRes.accepted ? "✅ Accepted" : "❌ Rejected").append("\n");
                result.append("PDA Result:     ").append(pdaRes.accepted ? "✅ Accepted" : "❌ Rejected").append("\n\n");
                lastResult = pdaRes; // This enables the Trace button
            }

            result.append("Generated PDA Transitions:\n");
            for (PDATransition t : lastPDA.transitions) result.append(t).append("\n");
            outputArea.setText(result.toString());
        } else if (choice.equals("DFA")) {
            if (input.isEmpty()) {
                outputArea.setText("Enter a binary string.");
                return;
            }
            if (!input.matches("[01]+")) {
                outputArea.setText("❌ ERROR: DFA only accepts binary characters (0 and 1).");
                return;
            }
            lastResult = DFABuilder.build().validate(input);
            outputArea.setText("Input: " + input + "\nResult: " + (lastResult.accepted ? "✅ " : "❌ ") + lastResult.reason);
        } else if (choice.equals("PDA")) {
            if (input.isEmpty()) {
                outputArea.setText("Enter an 'a' and 'b' string.");
                return;
            }
            if (!input.matches("[ab]+")) {
                outputArea.setText("❌ ERROR: PDA only accepts 'a' and 'b'.");
                return;
            }
            lastResult = PDA_anbn.validate(input);
            outputArea.setText("Input: " + input + "\nResult: " + (lastResult.accepted ? "✅ " : "❌ ") + lastResult.reason);
        }
    }

    private void saveToFile(String content) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Save PDA Transitions");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Text Files", "*.txt"));
        java.io.File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                writer.println(content);
                outputArea.setText("Successfully exported to: " + file.getAbsolutePath());
            } catch (java.io.IOException ex) {
                outputArea.setText("Error saving file: " + ex.getMessage());
            }
        }
    }

    private CFG parseGrammar(String text) {
        Map<String, List<String>> prods = new HashMap<>();
        Set<String> vars = new HashSet<>(), terms = new HashSet<>();
        String[] lines = text.split("\n");
        for (String line : lines) {
            String[] parts = line.split("->");
            if (parts.length < 2) continue;
            String left = parts[0].trim();
            vars.add(left);
            List<String> rules = new ArrayList<>();
            for (String r : parts[1].split("\\|")) {
                r = r.trim(); rules.add(r);
                for (char c : r.toCharArray()) {
                    if (Character.isLowerCase(c)) terms.add(String.valueOf(c));
                    else if (Character.isUpperCase(c)) vars.add(String.valueOf(c));
                }
            }
            prods.put(left, rules);
        }
        return new CFG(vars, terms, lines[0].split("->")[0].trim(), prods);
    }

    private Button createSidebarButton(String text) {
        Button b = new Button(text); b.setMaxWidth(Double.MAX_VALUE); b.setPrefHeight(40);
        b.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: black;");
        return b;
    }

    private void highlight(Button btn) {
        if (selectedButton != null) selectedButton.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: black;");
        btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        selectedButton = btn;
    }

    private void stylePrimaryButton(Button b) { b.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;"); }
    private void styleSecondaryButton(Button b) { b.setStyle("-fx-background-color: #cccccc;"); }

    public static void main(String[] args) { launch(); }
}
