package digital.varkhe.eyp.core.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Üstveri (CT_Ustveri, EY-Ustveri-2). Element order in XML follows the XSD sequence.
 * {@code mimeType} and {@code fileName} describe the cover letter (Üst Yazı).
 */
public record Metadata(UUID documentId, String subject, SecurityCode securityCode, OffsetDateTime securityCodeValidUntil,
                       String mimeType, Identifier ozId, List<Distribution> distributions, List<Attachment> attachments,
                       List<RelatedDocument> relatedDocuments, String language, Party creator, List<Party> concerned,
                       String fileName, List<FilingCode> filingCodes, String verificationAddress) {
    public Metadata {
        Objects.requireNonNull(documentId, "documentId");
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(securityCode, "securityCode");
        Objects.requireNonNull(mimeType, "mimeType");
        Objects.requireNonNull(creator, "creator");
        Objects.requireNonNull(fileName, "fileName");
        Objects.requireNonNull(verificationAddress, "verificationAddress");
        distributions = List.copyOf(Objects.requireNonNull(distributions, "distributions"));
        if (distributions.isEmpty()) throw new IllegalArgumentException("at least one distribution is required");
        attachments = attachments == null ? List.of() : List.copyOf(attachments);
        relatedDocuments = relatedDocuments == null ? List.of() : List.copyOf(relatedDocuments);
        concerned = concerned == null ? List.of() : List.copyOf(concerned);
        filingCodes = filingCodes == null ? List.of() : List.copyOf(filingCodes);
    }
}
