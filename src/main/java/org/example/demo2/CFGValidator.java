package org.example.demo2;

import java.util.*;

public class CFGValidator {

    public static ValidationResult validate(CFG cfg, String input) {
        List<String> trace = new ArrayList<>();
        trace.add("Validating CFG for input: " + input);
        
        // Initiate the recursive string derivation
        Set<String> visited = new HashSet<>();

        if (canDerive(cfg, cfg.startSymbol, input, 0, trace, 0, visited)) {
            trace.add("CFG Success: String can be derived.");
            return new ValidationResult(true, "Accepted by CFG", trace);
        } else {
            trace.add("CFG Failure: String cannot be derived from grammar.");
            return new ValidationResult(false, "Rejected by CFG", trace);
        }
    }

    private static boolean canDerive(CFG cfg, String current, String target, int pos, List<String> trace, int depth, Set<String> visited) {
        // Safety depth and breadth limits
        if (depth > 50) return false;
        
        String memoKey = current + "@" + pos;
        if (visited.contains(memoKey)) return false;
        visited.add(memoKey);

        // Base case: All input consumed
        if (pos == target.length() && current.isEmpty()) return true;
        if (current.isEmpty()) return false;

        // Optimization: if current length (terminals) already exceeds target, stop
        int terminalCount = 0;
        for(char c : current.toCharArray()) if(cfg.terminals.contains(String.valueOf(c))) terminalCount++;
        if (pos + terminalCount > target.length()) return false;

        String first = current.substring(0, 1);
        String rest = current.substring(1);

        if (cfg.terminals.contains(first)) {
            if (pos < target.length() && target.substring(pos, pos + 1).equals(first)) {
                return canDerive(cfg, rest, target, pos + 1, trace, depth, visited);
            }
            return false;
        }

        if (cfg.variables.contains(first)) {
            List<String> rules = cfg.productions.get(first);
            if (rules == null) return false;

            for (String prod : rules) {
                String nextDerivation = prod.equals("ε") ? rest : prod + rest;
                
                if (canDerive(cfg, nextDerivation, target, pos, trace, depth + 1, visited)) {
                    trace.add(String.format("Step: %s -> %s", first, prod));
                    return true;
                }
            }
        }

        return false;
    }
}
