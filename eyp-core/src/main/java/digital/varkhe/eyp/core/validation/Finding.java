package digital.varkhe.eyp.core.validation;

/**
 * One validation result. {@code rule} is the guide rule id (e.g. "K.33" from Teknik Rehber 2.1 Ek-1) or an
 * "EYP-" prefixed id for checks derived from other guide sections.
 */
public record Finding(String rule, Severity severity, String message) {
    @Override
    public String toString() {
        return "[" + severity + "] " + rule + " " + message;
    }
}
