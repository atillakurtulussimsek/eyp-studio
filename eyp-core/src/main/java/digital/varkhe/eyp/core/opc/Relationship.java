package digital.varkhe.eyp.core.opc;

import java.util.Objects;

/** An OPC relationship. {@code source} is "/" for package relationships, otherwise a part name. */
public record Relationship(String source, String id, String type, String target) {
    public Relationship {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(target, "target");
    }

    /** Resolves {@link #target()} against the source part to an absolute part name. */
    public String resolvedTarget() {
        if (target.startsWith("/")) return PartNames.normalize(target);
        String base = source.equals("/") ? "/" : source.substring(0, source.lastIndexOf('/') + 1);
        return PartNames.normalize(base + target);
    }
}
