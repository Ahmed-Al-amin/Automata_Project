package org.example.demo2;

import java.util.Arrays;
import java.util.HashMap;

public class DFABuilder {

    public static DFA build() {
        DFA dfa = new DFA();

        dfa.states.addAll(Arrays.asList("q0", "q1", "q2", "q3"));
        dfa.startState = "q0";
        dfa.acceptStates.add("q3");

        for (String s : dfa.states) {
            dfa.transition.put(s, new HashMap<>());
        }

        // q0: 0 ones seen (mod 3), last char was not 0 (or start)
        dfa.transition.get("q0").put('0', "q3");
        dfa.transition.get("q0").put('1', "q1");

        // q1: 1 one seen (mod 3)
        dfa.transition.get("q1").put('0', "q1");
        dfa.transition.get("q1").put('1', "q2");

        // q2: 2 ones seen (mod 3)
        dfa.transition.get("q2").put('0', "q2");
        dfa.transition.get("q2").put('1', "q0");

        // q3: 0 ones seen (mod 3), last char was 0 (Accept)
        dfa.transition.get("q3").put('0', "q3");
        dfa.transition.get("q3").put('1', "q1");

        return dfa;
    }
}
