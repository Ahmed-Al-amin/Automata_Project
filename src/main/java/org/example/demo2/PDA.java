package org.example.demo2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class PDA {
    public List<String> states = new ArrayList<>();
    public List<PDATransition> transitions = new ArrayList<>();
    public String startState;
    public String acceptState;

    public ValidationResult validate(String input) {
        class Config {
            String state;
            int position;
            Stack<String> stack;
            List<String> trace;

            Config(String state, int position, Stack<String> stack, List<String> trace) {
                this.state = state;
                this.position = position;
                this.stack = (Stack<String>) stack.clone();
                this.trace = new ArrayList<>(trace);
            }
        }

        Queue<Config> queue = new LinkedList<>();
        Stack<String> startStack = new Stack<>();
        startStack.push("Z0"); // Bottom of stack

        List<String> startTrace = new ArrayList<>();
        startTrace.add("Start: State " + startState + ", Stack [Z0]");
        queue.add(new Config(startState, 0, startStack, startTrace));

        Config lastProcessed = null;
        int totalConfigs = 0;
        int maxConfigs = 10000; // Total limit across all branches

        while (!queue.isEmpty()) {
            Config current = queue.poll();
            lastProcessed = current;
            totalConfigs++;

            if (totalConfigs > maxConfigs) {
                return new ValidationResult(false, "Rejected: Execution limit exceeded (potential infinite loop).", current.trace);
            }

            // Safety limit for a single path
            if (current.trace.size() > 200) continue;

            // NEW LOGIC: Acceptance by Empty Stack
            if (current.stack.isEmpty() && current.position == input.length()) {
                current.trace.add("Stack is empty (Z0 popped) and all input consumed! Acceptance reached.");
                return new ValidationResult(true, "Accepted by PDA (Empty Stack)", current.trace);
            }

            for (PDATransition t : transitions) {
                if (!t.fromState.equals(current.state)) continue;

                String stackTop = current.stack.isEmpty() ? "ε" : current.stack.peek();
                if (!t.pop.equals("ε") && !t.pop.equals(stackTop)) continue;

                boolean inputMatches = t.input.equals("ε") || 
                    (current.position < input.length() && t.input.equals(String.valueOf(input.charAt(current.position))));

                if (!inputMatches) continue;

                Stack<String> newStack = (Stack<String>) current.stack.clone();
                if (!t.pop.equals("ε") && !newStack.isEmpty()) newStack.pop();

                if (!t.push.equals("ε")) {
                    String[] toPush = t.push.split("");
                    for (int j = toPush.length - 1; j >= 0; j--) {
                        if (!toPush[j].equals("ε")) newStack.push(toPush[j]);
                    }
                }

                // Create descriptive step details
                StringBuilder stepDetail = new StringBuilder();
                stepDetail.append(String.format("Input: '%s', State: %s -> %s", t.input, t.fromState, t.toState));
                
                if (!t.pop.equals("ε")) {
                    stepDetail.append(String.format(" | POP: '%s'", t.pop));
                }
                if (!t.push.equals("ε")) {
                    stepDetail.append(String.format(" | PUSH: '%s'", t.push));
                }
                
                stepDetail.append(String.format(" | Stack Size: %d | Stack: %s", newStack.size(), newStack.toString()));

                int newPos = current.position + (t.input.equals("ε") ? 0 : 1);
                List<String> nextTrace = new ArrayList<>(current.trace);
                nextTrace.add(stepDetail.toString());

                queue.add(new Config(t.toState, newPos, newStack, nextTrace));
            }
        }

        String reason = "Rejected: No valid path to accept state.";
        return new ValidationResult(false, reason, lastProcessed != null ? lastProcessed.trace : new ArrayList<>());
    }
}
