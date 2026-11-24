package digital.varkhe.eyp.core.model;

import java.time.OffsetDateTime;
import java.util.Objects;

/** CT_Imza inside Nihai Üstveri. {@code signer} must match the signing certificate holder. */
public record Signer(Party.NaturalPerson signer, String office, String purpose, String description, OffsetDateTime date) {
    public Signer {
        Objects.requireNonNull(signer, "signer");
    }
}
