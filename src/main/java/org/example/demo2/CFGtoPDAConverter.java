package org.example.demo2;

import java.util.Arrays;

public class CFGtoPDAConverter {

    public static PDA convert(CFG cfg) {
        PDA pda = new PDA();

        // Initialize Standard States
        pda.states.addAll(Arrays.asList("q0", "q1", "qf"));
        pda.startState = "q0";
        pda.acceptState = "qf";

        String startStackSymbol = "$";

        // 1- Push start symbol & stack marker
        pda.transitions.add(
                new PDATransition("q0", "ε", "ε", "q1", cfg.startSymbol)
        );

        // 2- Handle Production Rules (Push variables to the stack)
        for (String var : cfg.productions.keySet()) {
            for (String prod : cfg.productions.get(var)) {

                String pushString;

                if (prod.equals("ε")) {
                    pushString = "ε";
                } 
                else {                    
                    pushString = prod;
                }

                pda.transitions.add(
                        new PDATransition("q1", "ε", var, "q1", pushString)
                );
            }
        }

        // 3- Match Terminals (Read input and pop stack)
        for (String t : cfg.terminals) {
            pda.transitions.add(
                    new PDATransition("q1", t, t, "q1", "ε")
            );
        }

        // 4- Accept when stack reaches bottom symbol
        pda.transitions.add(
                new PDATransition("q1", "ε", startStackSymbol, "qf", "ε")
        );

        return pda;
    }
}
