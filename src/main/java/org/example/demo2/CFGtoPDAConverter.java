package org.example.demo2;

import java.util.Arrays;

public class CFGtoPDAConverter {

    public static PDA convert(CFG cfg) {
        PDA pda = new PDA();

        // States
        pda.states.addAll(Arrays.asList("q0", "q1", "qf"));
        pda.startState = "q0";
        pda.acceptState = "qf";

        String startStackSymbol = "$";

        // 1️⃣ Push start symbol + stack bottom
        pda.transitions.add(
                new PDATransition("q0", "ε", "ε", "q1", cfg.startSymbol)
        );

        // 2️⃣ Handle productions
        for (String var : cfg.productions.keySet()) {
            for (String prod : cfg.productions.get(var)) {

                String pushString;

                if (prod.equals("ε")) {
                    pushString = "ε";
                } else {
                    // 🔥 Reverse production for correct stack push
                    pushString = prod;
                }

                pda.transitions.add(
                        new PDATransition("q1", "ε", var, "q1", pushString)
                );
            }
        }

        // 3️⃣ Match terminals
        for (String t : cfg.terminals) {
            pda.transitions.add(
                    new PDATransition("q1", t, t, "q1", "ε")
            );
        }

        // 4️⃣ Accept when stack reaches bottom symbol
        pda.transitions.add(
                new PDATransition("q1", "ε", startStackSymbol, "qf", "ε")
        );

        return pda;
    }
}
