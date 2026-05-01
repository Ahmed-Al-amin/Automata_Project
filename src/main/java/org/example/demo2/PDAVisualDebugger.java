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

public class PDAVisualDebugger {

    private String input;
    private PDA pda;
    private int currentIndex = 0;
    private String currentState;
    private Stack<String> stack = new Stack<>();
    
    private Pane diagramPane;
    private HBox stringBox;
    private VBox stackView;
    private Label statusLabel;
    
    private Map<String, Point> positions = new HashMap<>();
    private List<StackPane> charNodes = new ArrayList<>();
    private Map<String, Group> stateNodes = new HashMap<>();

    private static class Point {
        double x, y;
        Point(double x, double y) { this.x = x; this.y = y; }
    }

    public PDAVisualDebugger(String input, PDA pda) {
        this.input = input;
        this.pda = pda;
        this.currentState = pda.startState;
        this.stack.push("$");
    }

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("PDA Interactive Visual Debugger");

        HBox mainLayout = new HBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: white;");

        VBox leftArea = new VBox(20);
        leftArea.setPrefWidth(700);

        // 1. String Visualization
        stringBox = new HBox(5);
        stringBox.setAlignment(Pos.CENTER);
        renderString();

        // 2. Diagram Area
        diagramPane = new Pane();
        diagramPane.setPrefSize(700, 400);
        diagramPane.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        setupPositions();
        renderDiagram();

        // 3. Controls
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER);
        Button nextBtn = new Button("Step Forward ➡");
        Button resetBtn = new Button("🔄 Reset");
        statusLabel = new Label("Ready. Current State: " + currentState);
        statusLabel.setFont(new Font("System Bold", 14));
        nextBtn.setOnAction(e -> step());
        resetBtn.setOnAction(e -> reset());
        controls.getChildren().addAll(resetBtn, nextBtn, statusLabel);

        leftArea.getChildren().addAll(new Label("Input Processing:"), stringBox, diagramPane, controls);

        // 4. Stack Visualization
        VBox rightArea = new VBox(10);
        rightArea.setAlignment(Pos.TOP_CENTER);
        rightArea.setPadding(new Insets(0, 20, 0, 20));
        Label stackLabel = new Label("STACK");
        stackLabel.setFont(new Font("System Bold", 16));
        stackView = new VBox(2);
        stackView.setAlignment(Pos.BOTTOM_CENTER);
        stackView.setStyle("-fx-border-color: black; -fx-border-width: 0 2 2 2;");
        stackView.setPrefHeight(400);
        stackView.setPrefWidth(100);
        updateStackView();

        rightArea.getChildren().addAll(stackLabel, stackView);

        mainLayout.getChildren().addAll(leftArea, rightArea);

        updateHighlight();

        Scene scene = new Scene(mainLayout, 950, 650);
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
        positions.put("q1", new Point(100, 200));
        positions.put("q2", new Point(350, 200));
        positions.put("q3", new Point(600, 200));
    }

    private void renderDiagram() {
        for (PDATransition t : pda.transitions) {
            Point p1 = positions.get(t.fromState);
            Point p2 = positions.get(t.toState);
            if (t.fromState.equals(t.toState)) {
                drawSelfLoop(diagramPane, p1, t.input, t.input.equals("a") ? -1 : 1);
            } else {
                drawArrow(diagramPane, p1, p2, t.input);
            }
        }

        for (String state : pda.states) {
            Group group = new Group();
            Point p = positions.get(state);
            Circle circle = new Circle(p.x, p.y, 35);
            circle.setFill(Color.LIGHTBLUE);
            circle.setStroke(Color.BLACK);
            circle.setStrokeWidth(2);
            circle.setId("pda_circle_" + state);

            if (state.equals(pda.acceptState)) {
                Circle inner = new Circle(p.x, p.y, 30);
                inner.setFill(Color.TRANSPARENT);
                inner.setStroke(Color.BLACK);
                group.getChildren().add(inner);
            }

            Text text = new Text(p.x - 10, p.y + 5, state);
            text.setFont(new Font(16));
            group.getChildren().addAll(circle, text);
            stateNodes.put(state, group);
            diagramPane.getChildren().add(group);

            if (state.equals(pda.startState)) {
                Line arrow = new Line(p.x - 75, p.y, p.x - 35, p.y);
                arrow.setStrokeWidth(2);
                diagramPane.getChildren().add(arrow);
            }
        }
    }

    private void step() {
        // Try to find a valid transition
        String inputChar = (currentIndex < input.length()) ? String.valueOf(input.charAt(currentIndex)) : "ε";
        String top = stack.isEmpty() ? "ε" : stack.peek();

        PDATransition match = null;
        for (PDATransition t : pda.transitions) {
            if (t.fromState.equals(currentState) && t.input.equals(inputChar) && (t.stackTop.equals("ε") || t.stackTop.equals(top))) {
                match = t; break;
            }
        }
        
        // If no direct char match, check epsilon transitions
        if (match == null) {
            for (PDATransition t : pda.transitions) {
                if (t.fromState.equals(currentState) && t.input.equals("ε") && (t.stackTop.equals("ε") || t.stackTop.equals(top))) {
                    match = t; break;
                }
            }
        }

        if (match != null) {
            if (!match.input.equals("ε")) currentIndex++;
            if (!match.stackTop.equals("ε")) stack.pop();
            if (!match.push.equals("ε")) {
                for (int i = match.push.length() - 1; i >= 0; i--) {
                    stack.push(String.valueOf(match.push.charAt(i)));
                }
            }
            currentState = match.toState;
            statusLabel.setText(String.format("Step: (%s, %s, %s) -> %s", match.input, match.stackTop, match.push, currentState));
            updateHighlight();
            updateStackView();
        } else {
            if (currentIndex == input.length() && stack.isEmpty()) {
                statusLabel.setText("Accepted by Empty Stack! ✅");
            } else {
                statusLabel.setText("No transition found! Rejected ❌");
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

    private void reset() {
        currentIndex = 0;
        currentState = pda.startState;
        stack.clear();
        stack.push("$");
        statusLabel.setText("Reset.");
        updateHighlight();
        updateStackView();
    }

    private void updateHighlight() {
        for (int i = 0; i < charNodes.size(); i++) {
            Rectangle r = (Rectangle) charNodes.get(i).getChildren().get(0);
            r.setFill(i == currentIndex ? Color.YELLOW : (i < currentIndex ? Color.LIGHTGRAY : Color.WHITE));
        }

        for (String s : stateNodes.keySet()) {
            Group g = stateNodes.get(s);
            Circle c = null;
            for(javafx.scene.Node n : g.getChildren()) if(n instanceof Circle && n.getId() != null) c = (Circle) n;
            if (c != null) {
                c.setFill(s.equals(currentState) ? Color.ORANGE : (s.equals(pda.acceptState) ? Color.LIGHTGREEN : Color.LIGHTBLUE));
                c.setStrokeWidth(s.equals(currentState) ? 4 : 2);
            }
        }
    }

    private void drawArrow(Pane pane, Point p1, Point p2, String label) {
        double r = 35;
        double angle = Math.atan2(p2.y - p1.y, p2.x - p1.x);
        double startX = p1.x + r * Math.cos(angle);
        double startY = p1.y + r * Math.sin(angle);
        double endX = p2.x - r * Math.cos(angle);
        double endY = p2.y - r * Math.sin(angle);
        Line line = new Line(startX, startY, endX, endY);
        Polygon arrowhead = new Polygon();
        double arrowSize = 10;
        arrowhead.getPoints().addAll(new Double[]{endX, endY,
            endX - arrowSize * Math.cos(angle - Math.PI/6), endY - arrowSize * Math.sin(angle - Math.PI/6),
            endX - arrowSize * Math.cos(angle + Math.PI/6), endY - arrowSize * Math.sin(angle + Math.PI/6)});
        pane.getChildren().addAll(line, arrowhead);
    }

    private void drawSelfLoop(Pane pane, Point p, String label, int direction) {
        double yOffset = 35 * direction;
        Arc arc = new Arc(p.x, p.y + yOffset, 25, 25, direction == -1 ? 0 : 180, 240);
        arc.setFill(Color.TRANSPARENT); arc.setStroke(Color.BLACK); arc.setType(ArcType.OPEN);
        pane.getChildren().add(arc);
    }
}
