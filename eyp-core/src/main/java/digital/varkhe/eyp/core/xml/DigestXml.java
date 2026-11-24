package digital.varkhe.eyp.core.xml;

import digital.varkhe.eyp.core.model.DigestAlgorithm;

import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static digital.varkhe.eyp.core.pkg.EypConstants.NS_NIHAI_OZET;
import static digital.varkhe.eyp.core.pkg.EypConstants.NS_PAKET_OZETI;

/**
 * Paket Özeti / Nihai Özet serialisation (EY-PaketOzeti-2, EY-NihaiOzet-2). Each Reference carries two
 * DigestItems; like the official API the SHA-512 item is written first.
 */
public final class DigestXml {

    /** One digested component. {@code values} must have exactly two entries with different algorithms. */
    public record Reference(String uri, String type, List<DigestValue> values) {
        public Reference {
            Objects.requireNonNull(uri, "uri");
            Objects.requireNonNull(type, "type");
            values = List.copyOf(values);
        }
    }

    public record DigestValue(DigestAlgorithm algorithm, byte[] value) {
        public DigestValue {
            Objects.requireNonNull(algorithm, "algorithm");
            Objects.requireNonNull(value, "value");
            value = value.clone();
        }

        @Override
        public byte[] value() {
            return value.clone();
        }
    }

    public static byte[] writePaketOzeti(UUID documentId, List<Reference> refs) {
        return write("PaketOzeti", NS_PAKET_OZETI, documentId, refs);
    }

    public static byte[] writeNihaiOzet(UUID documentId, List<Reference> refs) {
        return write("NihaiOzet", NS_NIHAI_OZET, documentId, refs);
    }

    private static byte[] write(String root, String rootNs, UUID documentId, List<Reference> refs) {
        XmlOut x = new XmlOut();
        boolean nihai = !rootNs.equals(NS_PAKET_OZETI);
        x.start("", rootNs, root).ns("", rootNs);
        if (nihai) x.ns("po", NS_PAKET_OZETI);
        x.attr("Id", UstveriXml.id(documentId));
        String p = nihai ? "po" : "";
        Base64.Encoder b64 = Base64.getEncoder();
        for (Reference r : refs) {
            x.start(p, NS_PAKET_OZETI, "Reference").attr("URI", r.uri()).attr("Type", r.type());
            for (DigestValue v : r.values()) {
                x.start(p, NS_PAKET_OZETI, "DigestItem");
                x.start(p, NS_PAKET_OZETI, "DigestMethod").attr("Algorithm", v.algorithm().uri()).end();
                x.leaf(p, NS_PAKET_OZETI, "DigestValue", b64.encodeToString(v.value()));
                x.end();
            }
            x.end();
        }
        x.end();
        return x.finish();
    }

    private DigestXml() {
    }
}
