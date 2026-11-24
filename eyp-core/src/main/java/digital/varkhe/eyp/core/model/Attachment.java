package digital.varkhe.eyp.core.model;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

/**
 * CT_Ek. For {@link AttachmentType#DED} the file content is supplied separately to the package builder;
 * {@code fileName} is then the part name segment inside /Ekler/ or /ImzasizEkler/.
 */
public record Attachment(UUID id, boolean eYazismaId, String documentNo, AttachmentType type, String fileName,
                         String mimeType, String name, int order, String description, URI reference,
                         Identifier ozId, Boolean signed) {
    public Attachment {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(type, "type");
        if (order < 1) throw new IllegalArgumentException("order must be >= 1");
    }
}
