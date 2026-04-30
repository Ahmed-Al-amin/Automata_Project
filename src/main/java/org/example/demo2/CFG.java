package org.example.demo2;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class CFG {
    public Set<String> variables;
    public Set<String> terminals;
    public String startSymbol;
    public Map<String, List<String>> productions;

    public CFG(Set<String> variables, Set<String> terminals,
               String startSymbol, Map<String, List<String>> productions) {
        this.variables = variables;
        this.terminals = terminals;
        this.startSymbol = startSymbol;
        this.productions = productions;
    }
}
