package org.example.demo2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DFA {
    public Set<String> states;
    public String startState;
    public Set<String> acceptStates;
    public Map<String, Map<Character, String>> transition;

    public DFA() {
        states = new HashSet<>();
        acceptStates = new HashSet<>();
        transition = new HashMap<>();
    }

    public ValidationResult validate(String input) {
        String current = startState;
        List<String> trace = new ArrayList<>();
        trace.add("Start state: " + current);
        trace.add("Final states: " + acceptStates.toString());

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (transition.containsKey(current) && transition.get(current).containsKey(c)) {
                String next = transition.get(current).get(c);
                trace.add(String.format("Step %d: Input '%c' | %s -> %s", (i + 1), c, current, next));
                current = next;
            } else {
                return new ValidationResult(false, "Invalid character or missing transition: " + c, trace);
            }
        }

        boolean isAccepted = acceptStates.contains(current);
        trace.add("Final reached state: " + current + (isAccepted ? " (Final State ✅)" : " (Not a Final State ❌)"));
        
        if (isAccepted) {
            return new ValidationResult(true, "Accepted: Divisible by 3 and ends with 0.", trace);
        } else {
            return new ValidationResult(false, "Rejected: Does not meet requirements.", trace);
        }
    }
}
