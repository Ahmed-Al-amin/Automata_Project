package org.example.demo2;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class CFGPDAVisualizer {

    public static void show(PDA pda) {
        Stage stage = new Stage();
        stage.setTitle("CFG-to-PDA Diagram");

        HBox root = new HBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");

        // 1. Diagram Pane (Left Side)
        Pane diagramPane = new Pane();
        diagramPane.setPrefSize(500, 400);

        // Draw States q0, q1, qf
        drawState(diagramPane, 100, 200, "q0", false, true);
        drawState(diagramPane, 250, 200, "q1", false, false);
        drawState(diagramPane, 400, 200, "qf", true, false);

        // Draw Arcs (Text labels removed!)
        drawArrow(diagramPane, 130, 200, 220, 200);
        drawSelfLoop(diagramPane, 250, 170);
        drawArrow(diagramPane, 280, 200, 370, 200);

        // 2. Rules Pane (Right Side)
        VBox rulesBox = new VBox(10);
        Label rulesTitle = new Label("Generated PDA Rules:");
        rulesTitle.setFont(new Font("System Bold", 16));
        
        VBox transitionList = new VBox(5);
        for (PDATransition t : pda.transitions) {
            Label l = new Label(t.toString());
            l.setFont(new Font("Consolas", 12));
            transitionList.getChildren().add(l);
        }
        ScrollPane scroll = new ScrollPane(transitionList);
        scroll.setPrefSize(300, 350);
        rulesBox.getChildren().addAll(rulesTitle, scroll);

        root.getChildren().addAll(diagramPane, rulesBox);

        Scene scene = new Scene(root, 850, 450);
        stage.setScene(scene);
        stage.show();
    }

    private static void drawState(Pane pane, double x, double y, String name, boolean isAccept, boolean isStart) {
        Circle circle = new Circle(x, y, 30);
        circle.setFill(isAccept ? Color.LIGHTGREEN : Color.LIGHTBLUE);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2);

        if (isAccept) {
            Circle inner = new Circle(x, y, 25);
            inner.setFill(Color.TRANSPARENT);
            inner.setStroke(Color.BLACK);
            pane.getChildren().add(inner);
        }

        Text text = new Text(x - 10, y + 5, name);
        text.setFont(new Font(14));

        if (isStart) {
            Line arrow = new Line(x - 60, y, x - 30, y);
            arrow.setStrokeWidth(2);
            pane.getChildren().add(arrow);
        }
        pane.getChildren().addAll(circle, text);
    }

    private static void drawArrow(Pane pane, double startX, double startY, double endX, double endY) {
        Line line = new Line(startX, startY, endX, endY);
        Polygon arrowhead = new Polygon();
        arrowhead.getPoints().addAll(new Double[]{endX, endY, endX - 10, endY - 5, endX - 10, endY + 5});
        pane.getChildren().addAll(line, arrowhead);
    }

    private static void drawSelfLoop(Pane pane, double x, double y) {
        Arc arc = new Arc(x, y, 20, 20, -30, 240);
        arc.setFill(Color.TRANSPARENT);
        arc.setStroke(Color.BLACK);
        arc.setType(ArcType.OPEN);
        pane.getChildren().add(arc);
    }
}