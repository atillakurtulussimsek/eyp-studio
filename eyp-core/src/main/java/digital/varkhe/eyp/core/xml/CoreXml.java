package digital.varkhe.eyp.core.xml;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/** OPC core properties part (ISO/IEC 29500-2 §11). */
public final class CoreXml {
    static final String NS_CP = "http://schemas.openxmlformats.org/package/2006/metadata/core-properties";
    static final String NS_DC = "http://purl.org/dc/elements/1.1/";
    static final String NS_DCTERMS = "http://purl.org/dc/terms/";
    static final String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";

    public record CoreProperties(String identifier, String creator, String subject, String category, String contentType,
                                 String version, String revision, Instant created) {
    }

    public static byte[] write(CoreProperties c) {
        XmlOut x = new XmlOut();
        x.start("cp", NS_CP, "coreProperties").ns("cp", NS_CP).ns("dc", NS_DC).ns("dcterms", NS_DCTERMS).ns("xsi", NS_XSI);
        x.leaf("cp", NS_CP, "category", c.category());
        x.leaf("cp", NS_CP, "contentType", c.contentType());
        if (c.created() != null) {
            x.start("dcterms", NS_DCTERMS, "created").attr("xsi", NS_XSI, "type", "dcterms:W3CDTF")
                    .text(w3cdtf(c.created())).end();
        }
        x.leaf("dc", NS_DC, "creator", c.creator());
        x.leaf("dc", NS_DC, "identifier", c.identifier());
        x.leaf("cp", NS_CP, "revision", c.revision());
        x.leaf("dc", NS_DC, "subject", c.subject());
        x.leaf("cp", NS_CP, "version", c.version());
        x.end();
        return x.finish();
    }

    static String w3cdtf(Instant t) {
        return t.truncatedTo(ChronoUnit.SECONDS).toString();
    }

    private CoreXml() {
    }
}
