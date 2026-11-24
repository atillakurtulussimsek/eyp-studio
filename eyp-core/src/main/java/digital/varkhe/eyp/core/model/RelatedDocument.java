package digital.varkhe.eyp.core.model;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/** CT_Ilgi ("İlgi: a) ..."). {@code label} is the Etiket, e.g. "a". */
public record RelatedDocument(UUID id, boolean eYazismaId, String documentNo, OffsetDateTime date, String label,
                              UUID attachmentId, String name, String description, Identifier ozId) {
    public RelatedDocument {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(label, "label");
    }
}
