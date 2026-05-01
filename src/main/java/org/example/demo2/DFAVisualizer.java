package org.example.demo2;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class DFAVisualizer {

    private static class Point {
        double x, y;
        Point(double x, double y) { this.x = x; this.y = y; }
    }

    public static void showDFA() {
        Stage stage = new Stage();
        stage.setTitle("DFA Diagram - 1s Divisible by 3 and Ends with 0");

        Pane root = new Pane();
        root.setPrefSize(800, 600);
        root.setStyle("-fx-background-color: white;");

        Map<String, Point> positions = new HashMap<>();
        positions.put("q1", new Point(100, 300));
        positions.put("q2", new Point(250, 150));
        positions.put("q3", new Point(450, 150));
        positions.put("q4", new Point(250, 450));
        positions.put("q5", new Point(450, 450));
        positions.put("q6", new Point(650, 300));
        positions.put("q7", new Point(450, 300));

        DFA dfa = DFABuilder.build();

        // Draw transitions
        for (String from : dfa.transition.keySet()) {
            for (Map.Entry<Character, String> entry : dfa.transition.get(from).entrySet()) {
                char input = entry.getKey();
                String to = entry.getValue();
                
                Point p1 = positions.get(from);
                Point p2 = positions.get(to);
                
                if (from.equals(to)) {
                    drawSelfLoop(root, p1, input);
                } else {
                    drawArrow(root, p1, p2, String.valueOf(input));
                }
            }
        }

        // Draw states
        for (String state : dfa.states) {
            Point p = positions.get(state);
            boolean isAccept = dfa.acceptStates.contains(state);
            boolean isStart = state.equals(dfa.startState);
            drawState(root, p, state, isAccept, isStart);
        }

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private static void drawState(Pane pane, Point p, String name, boolean isAccept, boolean isStart) {
        Circle circle = new Circle(p.x, p.y, 30);
        circle.setFill(isAccept ? Color.LIGHTGREEN : Color.LIGHTBLUE);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2);

        if (isAccept) {
            Circle inner = new Circle(p.x, p.y, 25);
            inner.setFill(Color.TRANSPARENT);
            inner.setStroke(Color.BLACK);
            pane.getChildren().add(inner);
        }

        Text text = new Text(p.x - 10, p.y + 5, name);
        text.setFont(new Font(14));

        if (isStart) {
            Line arrow = new Line(p.x - 60, p.y, p.x - 30, p.y);
            arrow.setStrokeWidth(2);
            pane.getChildren().add(arrow);
        }

        pane.getChildren().addAll(circle, text);
    }

    private static void drawArrow(Pane pane, Point p1, Point p2, String label) {
        double r = 30;
        double angle = Math.atan2(p2.y - p1.y, p2.x - p1.x);
        
        double startX = p1.x + r * Math.cos(angle);
        double startY = p1.y + r * Math.sin(angle);
        double endX = p2.x - r * Math.cos(angle);
        double endY = p2.y - r * Math.sin(angle);

        Line line = new Line(startX, startY, endX, endY);
        line.setStrokeWidth(1.5);

        // Arrowhead
        double arrowSize = 10;
        Polygon arrowhead = new Polygon();
        arrowhead.getPoints().addAll(new Double[]{
            endX, endY,
            endX - arrowSize * Math.cos(angle - Math.PI/6), endY - arrowSize * Math.sin(angle - Math.PI/6),
            endX - arrowSize * Math.cos(angle + Math.PI/6), endY - arrowSize * Math.sin(angle + Math.PI/6)
        });
        arrowhead.setFill(Color.BLACK);

        Text text = new Text((startX + endX)/2 + 5 * Math.sin(angle), (startY + endY)/2 - 5 * Math.cos(angle), label);
        text.setFont(new Font(12));
        text.setFill(Color.RED);

        pane.getChildren().addAll(line, arrowhead, text);
    }

    private static void drawSelfLoop(Pane pane, Point p, char label) {
        Arc arc = new Arc(p.x, p.y - 30, 20, 20, -30, 240);
        arc.setFill(Color.TRANSPARENT);
        arc.setStroke(Color.BLACK);
        arc.setType(ArcType.OPEN);

        Text text = new Text(p.x - 5, p.y - 55, String.valueOf(label));
        text.setFill(Color.RED);
        
        pane.getChildren().addAll(arc, text);
    }
}
