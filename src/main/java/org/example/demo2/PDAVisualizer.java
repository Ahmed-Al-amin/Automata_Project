package org.example.demo2;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class PDAVisualizer {

    private static class Point {
        double x, y;
        Point(double x, double y) { this.x = x; this.y = y; }
    }

    public static void showPDA() {
        Stage stage = new Stage();
        stage.setTitle("PDA Diagram - L = {aⁿbⁿ | n ≥ 0}");

        Pane root = new Pane();
        root.setPrefSize(800, 400);
        root.setStyle("-fx-background-color: white;");

        Map<String, Point> positions = new HashMap<>();
        positions.put("q1", new Point(150, 200));
        positions.put("q2", new Point(400, 200));
        positions.put("q3", new Point(650, 200));

        PDA pda = PDA_anbn.build();

        // Draw transitions
        for (PDATransition t : pda.transitions) {
            Point p1 = positions.get(t.fromState);
            Point p2 = positions.get(t.toState);
            String label = String.format("%s, %s → %s", t.input, t.stackTop, t.push);
            
            if (t.fromState.equals(t.toState)) {
                drawSelfLoop(root, p1, label, t.input.equals("a") ? -1 : 1);
            } else {
                drawArrow(root, p1, p2, label);
            }
        }

        // Draw states
        for (String state : pda.states) {
            Point p = positions.get(state);
            boolean isAccept = state.equals(pda.acceptState);
            boolean isStart = state.equals(pda.startState);
            drawState(root, p, state, isAccept, isStart);
        }

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private static void drawState(Pane pane, Point p, String name, boolean isAccept, boolean isStart) {
        Circle circle = new Circle(p.x, p.y, 35);
        circle.setFill(isAccept ? Color.LIGHTGREEN : Color.LIGHTBLUE);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2);

        if (isAccept) {
            Circle inner = new Circle(p.x, p.y, 30);
            inner.setFill(Color.TRANSPARENT);
            inner.setStroke(Color.BLACK);
            pane.getChildren().add(inner);
        }

        Text text = new Text(p.x - 10, p.y + 5, name);
        text.setFont(new Font(16));

        if (isStart) {
            Line arrow = new Line(p.x - 75, p.y, p.x - 35, p.y);
            arrow.setStrokeWidth(2);
            pane.getChildren().add(arrow);
        }

        pane.getChildren().addAll(circle, text);
    }

    private static void drawArrow(Pane pane, Point p1, Point p2, String label) {
        double r = 35;
        double angle = Math.atan2(p2.y - p1.y, p2.x - p1.x);
        double startX = p1.x + r * Math.cos(angle);
        double startY = p1.y + r * Math.sin(angle);
        double endX = p2.x - r * Math.cos(angle);
        double endY = p2.y - r * Math.sin(angle);

        Line line = new Line(startX, startY, endX, endY);
        Polygon arrowhead = new Polygon();
        double arrowSize = 10;
        arrowhead.getPoints().addAll(new Double[]{
            endX, endY,
            endX - arrowSize * Math.cos(angle - Math.PI/6), endY - arrowSize * Math.sin(angle - Math.PI/6),
            endX - arrowSize * Math.cos(angle + Math.PI/6), endY - arrowSize * Math.sin(angle + Math.PI/6)
        });

        Text text = new Text((startX + endX)/2, (startY + endY)/2 - 10, label);
        text.setFont(new Font(10));
        text.setFill(Color.DARKRED);

        pane.getChildren().addAll(line, arrowhead, text);
    }

    private static void drawSelfLoop(Pane pane, Point p, String label, int direction) {
        // direction -1 for top, 1 for bottom
        double yOffset = 35 * direction;
        Arc arc = new Arc(p.x, p.y + yOffset, 25, 25, direction == -1 ? 0 : 180, 240);
        arc.setFill(Color.TRANSPARENT);
        arc.setStroke(Color.BLACK);
        arc.setType(ArcType.OPEN);

        Text text = new Text(p.x - 30, p.y + yOffset + (15 * direction), label);
        text.setFont(new Font(10));
        text.setFill(Color.DARKRED);
        
        pane.getChildren().addAll(arc, text);
    }
}
