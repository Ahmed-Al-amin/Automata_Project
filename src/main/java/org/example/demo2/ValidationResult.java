package org.example.demo2;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {
    public final boolean accepted;
    public final String reason;
    public final List<String> trace;

    public ValidationResult(boolean accepted, String reason) {
        this(accepted, reason, new ArrayList<>());
    }

    public ValidationResult(boolean accepted, String reason, List<String> trace) {
        this.accepted = accepted;
        this.reason = reason;
        this.trace = trace;
    }
}
