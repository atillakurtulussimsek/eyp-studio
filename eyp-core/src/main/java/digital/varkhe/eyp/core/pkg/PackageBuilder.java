package digital.varkhe.eyp.core.pkg;

import digital.varkhe.eyp.core.model.Attachment;
import digital.varkhe.eyp.core.model.AttachmentType;
import digital.varkhe.eyp.core.model.DigestAlgorithm;
import digital.varkhe.eyp.core.model.FinalMetadata;
import digital.varkhe.eyp.core.model.Metadata;
import digital.varkhe.eyp.core.model.Party;
import digital.varkhe.eyp.core.opc.OpcWriter;
import digital.varkhe.eyp.core.opc.PartNames;
import digital.varkhe.eyp.core.xml.CoreXml;
import digital.varkhe.eyp.core.xml.DigestXml;
import digital.varkhe.eyp.core.xml.UstveriXml;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static digital.varkhe.eyp.core.pkg.EypConstants.*;

/**
 * Builds an EYP 2.x package in the order the guide requires:
 * <ol>
 *   <li>{@link #buildPaketOzeti()} – freezes Üstveri, Üst Yazı and attachments; returns the bytes to be signed.</li>
 *   <li>{@link #signature(byte[])} – attached CAdES over Paket Özeti (produced elsewhere).</li>
 *   <li>{@link #buildNihaiOzet(FinalMetadata)} – writes Nihai Üstveri and Core, returns the bytes to be sealed.</li>
 *   <li>{@link #seal(byte[])} – CAdES over Nihai Özet.</li>
 *   <li>{@link #write(OutputStream)}.</li>
 * </ol>
 * Steps 2–4 are optional so that unsigned packages can be produced for testing and review.
 */
public final class PackageBuilder {

    private final Metadata metadata;
    private final DigestAlgorithm secondDigest;
    private final Clock clock;
    private byte[] coverLetter;
    private final Map<UUID, byte[]> attachmentData = new HashMap<>();

    private byte[] ustveriXml;
    private byte[] paketOzetiXml;
    private byte[] signature;
    private byte[] nihaiUstveriXml;
    private byte[] coreXml;
    private String corePartName;
    private byte[] nihaiOzetXml;
    private byte[] seal;

    /**
     * @param secondDigest the digest written next to SHA-512 for every component (guide: two different algorithms,
     *                     one of them SHA-512; the official API defaults to SHA-384).
     */
    public PackageBuilder(Metadata metadata, DigestAlgorithm secondDigest, Clock clock) {
        this.metadata = Objects.requireNonNull(metadata, "metadata");
        this.secondDigest = Objects.requireNonNull(secondDigest, "secondDigest");
        if (secondDigest == DigestAlgorithm.SHA512) throw new IllegalArgumentException("second digest must differ from SHA-512");
        this.clock = Objects.requireNonNull(clock, "clock");
        if (!PartNames.sanitizeFileName(metadata.fileName()).equals(metadata.fileName())) {
            throw new IllegalArgumentException("Metadata.fileName must already be a sanitised part name: " + metadata.fileName());
        }
    }

    public PackageBuilder coverLetter(byte[] data) {
        requireStage(ustveriXml == null, "cover letter must be added before Paket Özeti");
        this.coverLetter = data.clone();
        return this;
    }

    /** Adds the content of a DED attachment declared in {@link Metadata#attachments()}. */
    public PackageBuilder attachment(UUID attachmentId, byte[] data) {
        requireStage(ustveriXml == null, "attachments must be added before Paket Özeti");
        Attachment a = attachmentOf(attachmentId);
        if (a.type() != AttachmentType.DED) throw new IllegalArgumentException("Only DED attachments carry data: " + attachmentId);
        attachmentData.put(attachmentId, data.clone());
        return this;
    }

    /** Freezes Üstveri and returns Paket Özeti (the content to sign). */
    public byte[] buildPaketOzeti() {
        requireStage(ustveriXml == null, "Paket Özeti already built");
        Objects.requireNonNull(coverLetter, "cover letter is required");
        for (Attachment a : metadata.attachments()) {
            if (a.type() == AttachmentType.DED && !attachmentData.containsKey(a.id())) {
                throw new IllegalStateException("Missing data for DED attachment " + a.id());
            }
            if (a.type() == AttachmentType.DED) {
                Objects.requireNonNull(a.fileName(), "DED attachment needs fileName");
                if (!PartNames.sanitizeFileName(a.fileName()).equals(a.fileName())) {
                    throw new IllegalArgumentException("Attachment fileName must be sanitised: " + a.fileName());
                }
            }
        }
        ustveriXml = UstveriXml.write(metadata);
        List<DigestXml.Reference> refs = new ArrayList<>();
        refs.add(ref(PART_USTVERI, ustveriXml));
        refs.add(ref(coverLetterPart(), coverLetter));
        for (Attachment a : signedAttachments()) refs.add(ref(attachmentPart(a), attachmentData.get(a.id())));
        paketOzetiXml = DigestXml.writePaketOzeti(metadata.documentId(), refs);
        return paketOzetiXml.clone();
    }

    /** Attached CAdES signature over Paket Özeti. */
    public PackageBuilder signature(byte[] cades) {
        requireStage(paketOzetiXml != null && nihaiOzetXml == null, "signature belongs between Paket Özeti and Nihai Özet");
        this.signature = cades.clone();
        return this;
    }

    /** Writes Nihai Üstveri and Core and returns Nihai Özet (the content to seal). Requires a signature. */
    public byte[] buildNihaiOzet(FinalMetadata finalMetadata) {
        requireStage(signature != null, "Nihai Özet requires the signature");
        requireStage(nihaiOzetXml == null, "Nihai Özet already built");
        nihaiUstveriXml = UstveriXml.write(finalMetadata);
        buildCore();
        List<DigestXml.Reference> refs = new ArrayList<>();
        refs.add(ref(PART_USTVERI, ustveriXml));
        refs.add(ref(coverLetterPart(), coverLetter));
        refs.add(ref(PART_PAKET_OZETI, paketOzetiXml));
        refs.add(ref(PART_IMZA, signature));
        refs.add(ref(PART_NIHAI_USTVERI, nihaiUstveriXml));
        for (Attachment a : signedAttachments()) refs.add(ref(attachmentPart(a), attachmentData.get(a.id())));
        refs.add(ref(corePartName, coreXml));
        nihaiOzetXml = DigestXml.writeNihaiOzet(metadata.documentId(), refs);
        return nihaiOzetXml.clone();
    }

    /** CAdES seal over Nihai Özet. */
    public PackageBuilder seal(byte[] cades) {
        requireStage(nihaiOzetXml != null, "seal requires Nihai Özet");
        this.seal = cades.clone();
        return this;
    }

    /** Writes the package. Works at any stage after {@link #buildPaketOzeti()}. */
    public void write(OutputStream out) throws IOException {
        requireStage(paketOzetiXml != null, "build Paket Özeti first");
        if (coreXml == null) buildCore();
        OpcWriter w = new OpcWriter();
        String coverPart = coverLetterPart();
        w.part(coverPart, metadata.mimeType(), coverLetter).relationship("/", ID_USTYAZI, REL_USTYAZI, coverPart);
        w.part(PART_USTVERI, MIME_XML, ustveriXml).relationship("/", ID_USTVERI, REL_USTVERI, PART_USTVERI);
        for (Attachment a : metadata.attachments()) {
            if (a.type() != AttachmentType.DED) continue;
            String part = attachmentPart(a);
            boolean signed = isSigned(a);
            w.part(part, a.mimeType() == null ? MIME_OCTET : a.mimeType(), attachmentData.get(a.id()));
            w.relationship("/", (signed ? ID_PREFIX_EK : ID_PREFIX_IMZASIZ_EK) + a.id().toString().toUpperCase(Locale.ROOT),
                    signed ? REL_EK : REL_IMZASIZ_EK, part);
        }
        w.part(PART_PAKET_OZETI, MIME_XML, paketOzetiXml).relationship("/", ID_PAKET_OZETI, REL_PAKET_OZETI, PART_PAKET_OZETI);
        if (signature != null) {
            w.part(PART_IMZA, MIME_OCTET, signature).relationship(PART_PAKET_OZETI, ID_IMZA, REL_IMZA, PART_IMZA);
        }
        if (nihaiUstveriXml != null) {
            w.part(PART_NIHAI_USTVERI, MIME_XML, nihaiUstveriXml)
                    .relationship("/", ID_NIHAI_USTVERI, REL_NIHAI_USTVERI, PART_NIHAI_USTVERI);
        }
        if (nihaiOzetXml != null) {
            w.part(PART_NIHAI_OZET, MIME_XML, nihaiOzetXml).relationship("/", ID_NIHAI_OZET, REL_NIHAI_OZET, PART_NIHAI_OZET);
        }
        if (seal != null) {
            w.part(PART_MUHUR, MIME_OCTET, seal).relationship(PART_NIHAI_OZET, ID_MUHUR, REL_MUHUR, PART_MUHUR);
        }
        w.part(corePartName, MIME_CORE, coreXml).relationship("/", "R" + hex16(), REL_CORE, corePartName);
        w.write(out);
    }

    /** Recommended file name (K.66): [BelgeId].eyp. */
    public String fileName() {
        return metadata.documentId().toString().toUpperCase(Locale.ROOT) + ".eyp";
    }

    // ------------------------------------------------------------------ helpers

    private void buildCore() {
        corePartName = DIR_CORE + UUID.randomUUID().toString().replace("-", "") + ".psmdcp";
        coreXml = CoreXml.write(new CoreXml.CoreProperties(
                metadata.documentId().toString().toUpperCase(Locale.ROOT),
                creatorName(metadata.creator()),
                metadata.subject(),
                CORE_CATEGORY,
                CORE_CONTENT_TYPE,
                CORE_VERSION,
                "EYP Oluşturucu",
                clock.instant()));
    }

    private DigestXml.Reference ref(String part, byte[] data) {
        return new DigestXml.Reference(part, REF_TYPE_INTERNAL, List.of(
                new DigestXml.DigestValue(DigestAlgorithm.SHA512, DigestAlgorithm.SHA512.digest(data)),
                new DigestXml.DigestValue(secondDigest, secondDigest.digest(data))));
    }

    private String coverLetterPart() {
        return DIR_USTYAZI + metadata.fileName();
    }

    private String attachmentPart(Attachment a) {
        return (isSigned(a) ? DIR_EKLER : DIR_IMZASIZ_EKLER) + a.fileName();
    }

    /** ImzaliMi absent is treated as signed, mirroring how attachments are placed under /Ekler/. */
    private static boolean isSigned(Attachment a) {
        return a.signed() == null || a.signed();
    }

    private List<Attachment> signedAttachments() {
        Map<UUID, Attachment> out = new LinkedHashMap<>();
        for (Attachment a : metadata.attachments()) if (a.type() == AttachmentType.DED && isSigned(a)) out.put(a.id(), a);
        return List.copyOf(out.values());
    }

    private Attachment attachmentOf(UUID id) {
        for (Attachment a : metadata.attachments()) if (a.id().equals(id)) return a;
        throw new IllegalArgumentException("Attachment not declared in Üstveri: " + id);
    }

    /** Same rules as the official API's {@code olusturanAdiOlustur}. */
    static String creatorName(Party p) {
        return switch (p) {
            case Party.PublicInstitution k -> k.name() == null ? k.kkk() : k.name() + "/" + k.kkk();
            case Party.LegalEntity l -> l.name() != null ? l.name()
                    : (l.id().schemeId() == null ? "" : l.id().schemeId() + ":") + l.id().value();
            case Party.NaturalPerson n -> {
                List<String> parts = new ArrayList<>();
                if (n.tckn() != null) parts.add(n.tckn());
                if (n.duty() != null) parts.add(n.duty());
                parts.add(String.join(" ", nonNull(n.person().prefix(), n.person().title(), n.person().firstName(),
                        n.person().middleName(), n.person().lastName())));
                yield String.join(",", parts);
            }
        };
    }

    private static List<String> nonNull(String... values) {
        List<String> out = new ArrayList<>();
        for (String v : values) if (v != null && !v.isBlank()) out.add(v);
        return out;
    }

    private static String hex16() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase(Locale.ROOT);
    }

    private static void requireStage(boolean ok, String message) {
        if (!ok) throw new IllegalStateException(message);
    }
}
