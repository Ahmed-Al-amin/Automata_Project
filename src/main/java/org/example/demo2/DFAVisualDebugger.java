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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DFAVisualDebugger {

    private String input;
    private DFA dfa;
    private int currentIndex = 0;
    private String currentState;
    
    private Pane diagramPane;
    private HBox stringBox;
    private Label statusLabel;
    
    private Map<String, Point> positions = new HashMap<>();
    private List<StackPane> charNodes = new ArrayList<>();
    private Map<String, Group> stateNodes = new HashMap<>();

    private static class Point {
        double x, y;
        Point(double x, double y) { this.x = x; this.y = y; }
    }

    public DFAVisualDebugger(String input, DFA dfa) {
        this.input = input;
        this.dfa = dfa;
        this.currentState = dfa.startState;
    }

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("DFA Interactive Visual Debugger");

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");

        // 1. String Visualization
        stringBox = new HBox(5);
        stringBox.setAlignment(Pos.CENTER);
        renderString();

        // 2. Diagram Area
        diagramPane = new Pane();
        diagramPane.setPrefSize(800, 450);
        diagramPane.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        setupPositions();
        renderDiagram();

        // 3. Controls
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER);
        
        Button nextBtn = new Button("Step Forward ➡");
        Button resetBtn = new Button("🔄 Reset");
        statusLabel = new Label("Ready to start. Current State: " + currentState);
        statusLabel.setFont(new Font("System Bold", 14));

        nextBtn.setOnAction(e -> step());
        resetBtn.setOnAction(e -> reset());

        controls.getChildren().addAll(resetBtn, nextBtn, statusLabel);

        root.getChildren().addAll(new Label("Input Processing:"), stringBox, diagramPane, controls);

        updateHighlight();

        Scene scene = new Scene(root, 850, 650);
        stage.setScene(scene);
        stage.show();
    }

    private void renderString() {
        stringBox.getChildren().clear();
        charNodes.clear();
        for (int i = 0; i < input.length(); i++) {
            StackPane sp = new StackPane();
            Rectangle rect = new Rectangle(30, 40);
            rect.setFill(Color.WHITE);
            rect.setStroke(Color.BLACK);
            Text t = new Text(String.valueOf(input.charAt(i)));
            t.setFont(new Font(18));
            sp.getChildren().addAll(rect, t);
            charNodes.add(sp);
            stringBox.getChildren().add(sp);
        }
    }

    private void setupPositions() {
        positions.put("q1", new Point(100, 225));
        positions.put("q2", new Point(250, 100));
        positions.put("q3", new Point(450, 100));
        positions.put("q4", new Point(250, 350));
        positions.put("q5", new Point(450, 350));
        positions.put("q6", new Point(650, 225));
        positions.put("q7", new Point(450, 225));
    }

    private void renderDiagram() {
        // Draw transitions first
        for (String from : dfa.transition.keySet()) {
            for (Map.Entry<Character, String> entry : dfa.transition.get(from).entrySet()) {
                Point p1 = positions.get(from);
                Point p2 = positions.get(entry.getValue());
                if (from.equals(entry.getValue())) {
                    drawSelfLoop(diagramPane, p1, entry.getKey());
                } else {
                    drawArrow(diagramPane, p1, p2, String.valueOf(entry.getKey()));
                }
            }
        }

        // Draw states
        for (String state : dfa.states) {
            javafx.scene.Group group = new javafx.scene.Group();
            Point p = positions.get(state);
            
            Circle circle = new Circle(p.x, p.y, 30);
            circle.setFill(Color.LIGHTBLUE);
            circle.setStroke(Color.BLACK);
            circle.setStrokeWidth(2);
            circle.setId("circle_" + state);

            if (dfa.acceptStates.contains(state)) {
                Circle inner = new Circle(p.x, p.y, 25);
                inner.setFill(Color.TRANSPARENT);
                inner.setStroke(Color.BLACK);
                group.getChildren().add(inner);
            }

            Text text = new Text(p.x - 10, p.y + 5, state);
            text.setFont(new Font(14));
            
            group.getChildren().addAll(circle, text);
            stateNodes.put(state, group);
            diagramPane.getChildren().add(group);

            if (state.equals(dfa.startState)) {
                Line arrow = new Line(p.x - 60, p.y, p.x - 30, p.y);
                arrow.setStrokeWidth(2);
                diagramPane.getChildren().add(arrow);
            }
        }
    }

    private void step() {
        if (currentIndex >= input.length()) {
            boolean accepted = dfa.acceptStates.contains(currentState);
            statusLabel.setText("Processing finished. Result: " + (accepted ? "ACCEPTED ✅" : "REJECTED ❌"));
            return;
        }

        char c = input.charAt(currentIndex);
        if (dfa.transition.containsKey(currentState) && dfa.transition.get(currentState).containsKey(c)) {
            String nextState = dfa.transition.get(currentState).get(c);
            statusLabel.setText(String.format("Input '%c': %s -> %s", c, currentState, nextState));
            currentState = nextState;
            currentIndex++;
            updateHighlight();
        } else {
            statusLabel.setText("Error: No transition for input '" + c + "'");
        }
    }

    private void reset() {
        currentIndex = 0;
        currentState = dfa.startState;
        statusLabel.setText("Reset. Current State: " + currentState);
        updateHighlight();
    }

    private void updateHighlight() {
        // Reset string highlights
        for (int i = 0; i < charNodes.size(); i++) {
            Rectangle r = (Rectangle) charNodes.get(i).getChildren().get(0);
            if (i == currentIndex) {
                r.setFill(Color.YELLOW);
                r.setStrokeWidth(3);
            } else if (i < currentIndex) {
                r.setFill(Color.LIGHTGRAY);
                r.setStrokeWidth(1);
            } else {
                r.setFill(Color.WHITE);
                r.setStrokeWidth(1);
            }
        }

        // Reset state highlights
        for (String state : stateNodes.keySet()) {
            javafx.scene.Group g = stateNodes.get(state);
            Circle c = null;
            for(javafx.scene.Node node : g.getChildren()) {
                if (node instanceof Circle && (node.getId() != null && node.getId().startsWith("circle_"))) {
                    c = (Circle) node;
                }
            }
            if (c != null) {
                if (state.equals(currentState)) {
                    c.setFill(Color.ORANGE);
                    c.setStrokeWidth(4);
                } else {
                    c.setFill(dfa.acceptStates.contains(state) ? Color.LIGHTGREEN : Color.LIGHTBLUE);
                    c.setStrokeWidth(2);
                }
            }
        }
    }

    // Reuse arrow drawing logic
    private void drawArrow(Pane pane, Point p1, Point p2, String label) {
        double r = 30;
        double angle = Math.atan2(p2.y - p1.y, p2.x - p1.x);
        double startX = p1.x + r * Math.cos(angle);
        double startY = p1.y + r * Math.sin(angle);
        double endX = p2.x - r * Math.cos(angle);
        double endY = p2.y - r * Math.sin(angle);
        Line line = new Line(startX, startY, endX, endY);
        double arrowSize = 10;
        Polygon arrowhead = new Polygon();
        arrowhead.getPoints().addAll(new Double[]{
            endX, endY,
            endX - arrowSize * Math.cos(angle - Math.PI/6), endY - arrowSize * Math.sin(angle - Math.PI/6),
            endX - arrowSize * Math.cos(angle + Math.PI/6), endY - arrowSize * Math.sin(angle + Math.PI/6)
        });
        Text text = new Text((startX + endX)/2 + 5 * Math.sin(angle), (startY + endY)/2 - 5 * Math.cos(angle), label);
        text.setFill(Color.RED);
        pane.getChildren().addAll(line, arrowhead, text);
    }

    private void drawSelfLoop(Pane pane, Point p, char label) {
        Arc arc = new Arc(p.x, p.y - 30, 20, 20, -30, 240);
        arc.setFill(Color.TRANSPARENT);
        arc.setStroke(Color.BLACK);
        arc.setType(ArcType.OPEN);
        Text text = new Text(p.x - 5, p.y - 55, String.valueOf(label));
        text.setFill(Color.RED);
        pane.getChildren().addAll(arc, text);
    }
}
