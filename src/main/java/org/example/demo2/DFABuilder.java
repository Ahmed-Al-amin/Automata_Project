package org.example.demo2;

import java.util.Arrays;
import java.util.HashMap;

public class DFABuilder {

    public static DFA build() {
        DFA dfa = new DFA();

        // Renaming to q1-q7
        // q1 = q0 (Start)
        // q2 = q1_1, q3 = q1_0
        // q4 = q2_1, q5 = q2_0
        // q6 = q3_1, q7 = q3_0 (Accept)
        dfa.states.addAll(Arrays.asList("q1", "q2", "q3", "q4", "q5", "q6", "q7"));
        dfa.startState = "q1";

        // Accept only if divisible by 3 AND > 0 ones AND ends with 0 (q7)
        dfa.acceptStates.add("q7");

        for (String s : dfa.states) {
            dfa.transition.put(s, new HashMap<>());
        }

        // q1: 0 ones seen
        dfa.transition.get("q1").put('0', "q7");
        dfa.transition.get("q1").put('1', "q2");

        // q2, q3: 1 one seen (mod 3)
        dfa.transition.get("q2").put('0', "q3");
        dfa.transition.get("q2").put('1', "q4");
        dfa.transition.get("q3").put('0', "q3");
        dfa.transition.get("q3").put('1', "q4");

        // q4, q5: 2 ones seen (mod 3)
        dfa.transition.get("q4").put('0', "q5");
        dfa.transition.get("q4").put('1', "q6");
        dfa.transition.get("q5").put('0', "q5");
        dfa.transition.get("q5").put('1', "q6");

        // q6, q7: 0 ones seen (mod 3) AND at least one 1 seen
        dfa.transition.get("q6").put('0', "q7");
        dfa.transition.get("q6").put('1', "q2");
        dfa.transition.get("q7").put('0', "q7");
        dfa.transition.get("q7").put('1', "q2");

        return dfa;
    }
}
