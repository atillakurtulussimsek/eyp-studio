package digital.varkhe.eyp.core.opc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Read-only view of an OPC package. Part names are absolute ("/A/b.xml"); lookups are case-insensitive. */
public final class OpcPackage {

    public record Part(String name, String contentType, byte[] data) {
        @Override
        public byte[] data() {
            return data.clone();
        }
    }

    private final Map<String, Part> parts;
    private final List<Relationship> relationships;

    OpcPackage(Map<String, Part> parts, List<Relationship> relationships) {
        this.parts = Collections.unmodifiableMap(new LinkedHashMap<>(parts));
        this.relationships = List.copyOf(relationships);
    }

    public Optional<Part> part(String name) {
        return Optional.ofNullable(parts.get(PartNames.key(PartNames.normalize(name))));
    }

    public List<Part> parts() {
        return List.copyOf(parts.values());
    }

    public List<Relationship> relationships() {
        return relationships;
    }

    public List<Relationship> relationships(String source, String type) {
        List<Relationship> out = new ArrayList<>();
        for (Relationship r : relationships) {
            if (r.source().equalsIgnoreCase(source) && r.type().equals(type)) out.add(r);
        }
        return out;
    }
}
