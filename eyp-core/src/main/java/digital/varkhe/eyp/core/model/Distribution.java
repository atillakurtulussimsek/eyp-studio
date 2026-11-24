package digital.varkhe.eyp.core.model;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** CT_Dagitim – one recipient. */
public record Distribution(Party party, Urgency urgency, DistributionType type, Duration deadline,
                           List<UUID> notIncludedAttachmentIds) {
    public Distribution {
        Objects.requireNonNull(party, "party");
        Objects.requireNonNull(urgency, "urgency");
        Objects.requireNonNull(type, "type");
        notIncludedAttachmentIds = notIncludedAttachmentIds == null ? List.of() : List.copyOf(notIncludedAttachmentIds);
    }

    public Distribution(Party party, Urgency urgency, DistributionType type) {
        this(party, urgency, type, null, List.of());
    }
}
