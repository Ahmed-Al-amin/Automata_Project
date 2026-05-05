package org.example.demo2;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.*;

public class CFGPDAVisualDebugger {

    private String input;
    private PDA pda;
    private List<String> trace; 
    private int traceIndex = 1; 
    
    private int currentIndex = 0;
    private String currentState;
    private Stack<String> stack = new Stack<>();
    
    private HBox stringBox;
    private VBox stackView;
    private Label statusLabel;
    private Pane diagramPane;
    
    private List<StackPane> charNodes = new ArrayList<>();
    private Map<String, Point> positions = new HashMap<>();
    private Map<String, Group> stateNodes = new HashMap<>();

    private static class Point {
        double x, y;
        Point(double x, double y) { this.x = x; this.y = y; }
    }

    public CFGPDAVisualDebugger(String input, PDA pda, List<String> trace) {
        this.input = input;
        this.pda = pda;
        this.trace = trace;
        this.currentState = "q0";
        this.stack.push("$");
    }

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("CFG PDA Trace Playback Debugger");

        HBox mainLayout = new HBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: white;");

        VBox leftArea = new VBox(20);
        leftArea.setPrefWidth(600);
        
        stringBox = new HBox(5);
        renderString();

        // Diagram Area
        diagramPane = new Pane();
        diagramPane.setPrefSize(600, 250);
        diagramPane.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        setupPositions();
        renderDiagram();

        // Controls
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_LEFT);
        Button resetBtn = new Button("🔄 Reset");
        Button nextBtn = new Button("Step Forward ➡");
        statusLabel = new Label("Ready. State: q0");
        statusLabel.setFont(new Font("System Bold", 14));
        
        resetBtn.setOnAction(e -> reset());
        nextBtn.setOnAction(e -> step());
        controls.getChildren().addAll(resetBtn, nextBtn, statusLabel);

        leftArea.getChildren().addAll(new Label("Input Processing:"), stringBox, diagramPane, controls);

        // Stack Area
        VBox rightArea = new VBox(10);
        rightArea.setAlignment(Pos.TOP_CENTER);
        stackView = new VBox(2);
        stackView.setAlignment(Pos.BOTTOM_CENTER);
        stackView.setStyle("-fx-border-color: black; -fx-border-width: 0 2 2 2;");
        stackView.setPrefSize(120, 350);
        updateStackView();
        rightArea.getChildren().addAll(new Label("STACK"), stackView);

        mainLayout.getChildren().addAll(leftArea, rightArea);
        
        updateHighlight();

        Scene scene = new Scene(mainLayout, 800, 500);
        stage.setScene(scene);
        stage.show();
    }

    private void setupPositions() {
        positions.put("q0", new Point(100, 125));
        positions.put("q1", new Point(300, 125));
        positions.put("qf", new Point(500, 125));
    }

    private void renderString() {
        if (input.isEmpty()) input = "ε"; 
        for (int i = 0; i < input.length(); i++) {
            StackPane sp = new StackPane();
            Rectangle rect = new Rectangle(30, 40);
            rect.setFill(Color.WHITE);
            rect.setStroke(Color.BLACK);
            Text t = new Text(String.valueOf(input.charAt(i)));
            sp.getChildren().addAll(rect, t);
            charNodes.add(sp);
            stringBox.getChildren().add(sp);
        }
    }

    private void renderDiagram() {
        drawArrow(diagramPane, 130, 125, 270, 125);
        drawSelfLoop(diagramPane, 300, 95);
        drawArrow(diagramPane, 330, 125, 470, 125);

        for (String state : new String[]{"q0", "q1", "qf"}) {
            Group group = new Group();
            Point p = positions.get(state);
            
            // 1. ADD MAIN CIRCLE FIRST (Index 0) so it acts as the base layer
            Circle circle = new Circle(p.x, p.y, 30);
            circle.setFill(Color.LIGHTBLUE);
            circle.setStroke(Color.BLACK);
            circle.setStrokeWidth(2);
            group.getChildren().add(circle);
            
            // 2. ADD INNER CIRCLE SECOND (Index 1) so it draws ON TOP of the blue
            if (state.equals("qf")) {
                Circle inner = new Circle(p.x, p.y, 25);
                inner.setFill(Color.TRANSPARENT);
                inner.setStroke(Color.BLACK);
                group.getChildren().add(inner);
            }

            // 3. ADD TEXT THIRD (Index 2)
            Text text = new Text(p.x - 10, p.y + 5, state);
            text.setFont(new Font(14));

            if (state.equals("q0")) {
                Line arrow = new Line(p.x - 60, p.y, p.x - 30, p.y);
                arrow.setStrokeWidth(2);
                diagramPane.getChildren().add(arrow);
            }
            
            group.getChildren().add(text);
            stateNodes.put(state, group);
            diagramPane.getChildren().add(group);
        }
    }

    private void reset() {
        traceIndex = 1;
        currentIndex = 0;
        currentState = "q0";
        stack.clear();
        stack.push("$");
        statusLabel.setText("Reset. State: q0");
        updateHighlight();
        updateStackView();
    }

    private void step() {
        if (trace == null || traceIndex >= trace.size()) {
            statusLabel.setText("Trace finished!");
            return;
        }

        String line = trace.get(traceIndex);

        // 🎉 THE FIX: Explicitly check for acceptance and trigger the green state!
        if (line.contains("Acceptance reached") || line.contains("Accepted")) {
            currentState = "DONE"; // Triggers the green highlight in updateHighlight()
            statusLabel.setText("String Accepted! ✅ Reached Final State (qf)");
            updateHighlight();
            traceIndex++;
            return;
        }

        // Show a red X if the machine rejected the string
        if (line.contains("Rejected") || line.contains("FATAL")) {
            currentState = "ERROR";
            statusLabel.setText("String Rejected ❌");
            updateHighlight();
            traceIndex++;
            return;
        }

        if (line.startsWith("Input: '")) {
            char inputChar = line.charAt(8); // Grabs the exact character inside the quotes
            if (inputChar != 'ε') {
                currentIndex++;
            }
        }

        // Parse State
        if (line.contains("State: ")) {
            try {
                String statePart = line.substring(line.indexOf("State: ") + 7, line.indexOf(" | POP"));
                currentState = statePart.split("->")[1].trim();
            } catch (Exception e) {}
        }

        // Parse Stack
        if (line.contains("Stack: [")) {
            String stackStr = line.substring(line.indexOf("Stack: [") + 8, line.indexOf("]"));
            stack.clear();
            if (!stackStr.trim().isEmpty()) {
                String[] items = stackStr.split(",");
                for (int i = 0; i < items.length; i++) {
                    stack.push(items[i].trim());
                }
            }
        }

        try {
            String action = line.split(" \\| Stack")[0]; 
            statusLabel.setText(action);
        } catch (Exception e) {
            statusLabel.setText("Step " + traceIndex);
        }

        updateHighlight();
        updateStackView();
        traceIndex++;
    }

    private void updateHighlight() {
        if (!input.equals("ε")) {
            for (int i = 0; i < charNodes.size(); i++) {
                Rectangle r = (Rectangle) charNodes.get(i).getChildren().get(0);
                r.setFill(i == currentIndex ? Color.YELLOW : (i < currentIndex ? Color.LIGHTGRAY : Color.WHITE));
            }
        }

        for (String s : stateNodes.keySet()) {
            Group g = stateNodes.get(s);
            // 🎉 THE FIX: Safely grab the main base circle which is guaranteed to be at Index 0
            Circle c = (Circle) g.getChildren().get(0); 
            
            if (s.equals(currentState)) {
                c.setFill(Color.ORANGE);
                c.setStrokeWidth(4);
            } else if (s.equals("qf") && currentState.equals("DONE")) {
                c.setFill(Color.LIGHTGREEN);
                c.setStrokeWidth(4);
            } else if (s.equals("qf") && currentState.equals("ERROR")) {
                c.setFill(Color.LIGHTCORAL); 
                c.setStrokeWidth(4);
            } else {
                c.setFill(Color.LIGHTBLUE);
                c.setStrokeWidth(2);
            }
        }
    }

    private void updateStackView() {
        stackView.getChildren().clear();
        List<String> items = new ArrayList<>(stack);
        Collections.reverse(items);
        for (String s : items) {
            StackPane sp = new StackPane();
            Rectangle r = new Rectangle(80, 25);
            r.setFill(Color.LIGHTCORAL);
            r.setStroke(Color.BLACK);
            Text t = new Text(s);
            sp.getChildren().addAll(r, t);
            stackView.getChildren().add(sp);
        }
    }

    private void drawArrow(Pane pane, double startX, double startY, double endX, double endY) {
        Line line = new Line(startX, startY, endX, endY);
        Polygon arrowhead = new Polygon();
        arrowhead.getPoints().addAll(new Double[]{endX, endY, endX - 10, endY - 5, endX - 10, endY + 5});
        pane.getChildren().addAll(line, arrowhead);
    }

    private void drawSelfLoop(Pane pane, double x, double y) {
        Arc arc = new Arc(x, y, 20, 20, -30, 240);
        arc.setFill(Color.TRANSPARENT);
        arc.setStroke(Color.BLACK);
        arc.setType(ArcType.OPEN);
        pane.getChildren().add(arc);
    }
}