package org.example.demo2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AutomataLogic {
    public static void main(String[] args) {

        // ===== CFG → PDA =====
        Map<String, List<String>> productions = new HashMap<>();
        productions.put("S", Arrays.asList("aSb", "ε"));

        CFG cfg = new CFG(
                Set.of("S"),
                Set.of("a", "b"),
                "S",
                productions
        );

        PDA pda = CFGtoPDAConverter.convert(cfg);

        System.out.println("CFG → PDA:");
        for (PDATransition t : pda.transitions) {
            System.out.println(t);
        }

        // ===== DFA =====
        DFA dfa = DFABuilder.build();

        System.out.println("\nDFA Tests:");
        System.out.println("0 → " + dfa.validate("0").accepted);         // false
        System.out.println("1110 → " + dfa.validate("1110").accepted);   // true
        System.out.println("110 → " + dfa.validate("110").accepted);     // false
        System.out.println("111 → " + dfa.validate("111").accepted);     // false
        System.out.println("011100 → " + dfa.validate("011100").accepted); // true

        // ===== PDA a^n b^n =====
        System.out.println("\nPDA a^n b^n:");
        System.out.println("aaabbb → " + PDA_anbn.validate("aaabbb").accepted); // true
        System.out.println("aabbb → " + PDA_anbn.validate("aabbb").accepted);   // false
        System.out.println("aaabbbbbaa → " + PDA_anbn.validate("aaabbbbbaa").accepted); // false
    }
}
