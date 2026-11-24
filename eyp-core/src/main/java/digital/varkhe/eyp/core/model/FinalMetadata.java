package digital.varkhe.eyp.core.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

/** Nihai Üstveri (CT_NihaiUstveri): document date, number and signers. */
public record FinalMetadata(OffsetDateTime date, String documentNo, List<Signer> signers) {
    public FinalMetadata {
        Objects.requireNonNull(date, "date");
        Objects.requireNonNull(documentNo, "documentNo");
        signers = List.copyOf(Objects.requireNonNull(signers, "signers"));
        if (signers.isEmpty()) throw new IllegalArgumentException("at least one signer is required");
    }
}
