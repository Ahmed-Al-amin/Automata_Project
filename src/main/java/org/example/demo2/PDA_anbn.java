package org.example.demo2;

import java.util.Arrays;

public class PDA_anbn {

    public static PDA build() {
        PDA pda = new PDA();
        
        // States: q1 (reading a's), q2 (reading b's), q3 (accept)
        // Note: PDA.java starts in the state assigned to startState with [Z0] already on stack.
        pda.states.addAll(Arrays.asList("q1", "q2", "q3"));
        pda.startState = "q1";
        pda.acceptState = "q3";

        // Transition: (from, input, pop, to, push)
        
        // q1: Read 'a', push 'A'. 
        // Note: PDA.java pop logic requires matching stack top if pop != "ε"
        // Here we don't pop anything to push 'A', but we must handle the stack carefully.
        // In this PDA, 'A's are pushed on top of Z0 or other 'A's.
        pda.transitions.add(new PDATransition("q1", "a", "ε", "q1", "A"));
        
        // q1 -> q2: On first 'b', pop 'A'
        pda.transitions.add(new PDATransition("q1", "b", "A", "q2", "ε"));
        
        // q2: Read 'b', pop 'A'
        pda.transitions.add(new PDATransition("q2", "b", "A", "q2", "ε"));
        
        // q2 -> q3: On empty stack (only Z0 left), accept
        // We pop Z0 to match the "Accept State reached" condition in PDA.java (which doesn't strictly check empty stack but it's cleaner)
        pda.transitions.add(new PDATransition("q2", "ε", "$", "q3", "ε"));
        
        // Special case: n=0 (empty string)
        pda.transitions.add(new PDATransition("q1", "ε", "$", "q3", "ε"));

        return pda;
    }

    public static ValidationResult validate(String input) {
        PDA pda = build();
        return pda.validate(input);
    }
}
