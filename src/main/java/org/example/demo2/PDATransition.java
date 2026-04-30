package org.example.demo2;

public class PDATransition {
    public String fromState;
    public String input;
    public String stackTop;
    public String toState;
    public String push;
    public String pop;

    public PDATransition(String from, String input, String stackTop,
                         String to, String push) {
        this.fromState = from;
        this.input = input;
        this.stackTop = stackTop;
        this.toState = to;
        this.push = push;
        this.pop = stackTop;
    }

    @Override
    public String toString() {
        return "(" + fromState + ", " + input + ", " + stackTop + ") → ("
                + toState + ", " + push + ")";
    }
}
