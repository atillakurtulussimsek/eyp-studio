package digital.varkhe.eyp.core.model;

import java.util.Objects;

/** CT_SDP – Standart Dosya Planı code, e.g. 045.02. */
public record FilingCode(String code, String name, String description) {
    public FilingCode {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(name, "name");
    }
}
