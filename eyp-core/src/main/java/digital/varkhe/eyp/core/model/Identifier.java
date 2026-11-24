package digital.varkhe.eyp.core.model;

import java.util.Objects;

/** udt:IdentifierType – a value with its scheme (e.g. schemeID="VKN"). */
public record Identifier(String value, String schemeId) {
    public Identifier {
        Objects.requireNonNull(value, "value");
    }
}
