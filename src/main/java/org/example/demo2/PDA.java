package org.example.demo2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

public class PDA {
    public List<String> states = new ArrayList<>();
    public List<PDATransition> transitions = new ArrayList<>();
    public String startState;
    public String acceptState;

    public ValidationResult validate(String input) {        // 🧠 Upgraded Config with an Epsilon Loop Killer
        class Config {
            String state;
            int position;
            Stack<String> stack;
            List<String> trace;
            int epsilons; // Tracks consecutive epsilon jumps

            Config(String state, int position, Stack<String> stack, List<String> trace, int epsilons) {
                this.state = state;
                this.position = position;
                this.stack = (Stack<String>) stack.clone();
                this.trace = new ArrayList<>(trace);
                this.epsilons = epsilons;
            }
        }

        // Back to the fair LinkedList!
        Queue<Config> queue = new LinkedList<>();
        Stack<String> startStack = new Stack<>();
        startStack.push("$"); 

        List<String> startTrace = new ArrayList<>();
        startTrace.add("Start: State " + startState + ", Stack [$]");
        queue.add(new Config(startState, 0, startStack, startTrace, 0));

        // 🧠 MEMORY: Prevents the machine from doing the exact same work twice
        Set<String> visited = new HashSet<>(); 

        Config lastProcessed = null;

        int totalConfigs = 0;
        int maxConfigs = 15000;
        
        while (!queue.isEmpty()) {
            Config current = queue.poll();
            lastProcessed = current;
            
            totalConfigs++;
            if (totalConfigs > maxConfigs) {
                current.trace.add("FATAL: State space explosion. Over " + maxConfigs + " paths checked.");
                return new ValidationResult(false, "Rejected: Execution limit exceeded.", current.trace);
            }
            
            if (current.epsilons > 50) continue;

            // MEMORY CHECK: Have we been in this exact state before? If yes, skip it!
            String stateKey = current.state + "|" + current.position + "|" + current.stack.toString();
            if (visited.contains(stateKey)) continue;
            visited.add(stateKey);

            // Win Condition
            if (current.stack.isEmpty() && current.position == input.length()) {
                current.trace.add("Stack is empty ($ popped) and all input consumed! Acceptance reached.");
                return new ValidationResult(true, "Accepted by PDA", current.trace);
            }

            for (PDATransition t : transitions) {
                if (!t.fromState.equals(current.state)) continue;

                String stackTop = current.stack.isEmpty() ? "ε" : current.stack.peek();
                if (!t.pop.equals("ε") && !t.pop.equals(stackTop)) continue;

                boolean isEpsilon = t.input.equals("ε");
                boolean inputMatches = isEpsilon || 
                    (current.position < input.length() && t.input.equals(String.valueOf(input.charAt(current.position))));

                if (!inputMatches) continue;

                Stack<String> newStack = (Stack<String>) current.stack.clone();
                if (!t.pop.equals("ε") && !newStack.isEmpty()) newStack.pop();

                // Bulletproof character array push
                if (!t.push.equals("ε")) {
                    for (int j = t.push.length() - 1; j >= 0; j--) {
                        newStack.push(String.valueOf(t.push.charAt(j)));
                    }
                }

                int newPos = current.position + (isEpsilon ? 0 : 1);
                // If we read a real letter, reset the epsilon counter. If not, add 1.
                int newEpsilons = isEpsilon ? current.epsilons + 1 : 0;
                
                List<String> nextTrace = new ArrayList<>(current.trace);
                nextTrace.add(String.format("Input: '%s', State: %s -> %s | POP: '%s' | PUSH: '%s' | Stack: %s", 
                    isEpsilon ? "ε" : t.input, t.fromState, t.toState, t.pop, t.push, newStack.toString()));

                queue.add(new Config(t.toState, newPos, newStack, nextTrace, newEpsilons));
            }
        }

        return new ValidationResult(false, "Rejected: No valid path to accept state.", lastProcessed != null ? lastProcessed.trace : new ArrayList<>());
    }
}
