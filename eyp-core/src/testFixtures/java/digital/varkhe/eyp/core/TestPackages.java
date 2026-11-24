package digital.varkhe.eyp.core;

import digital.varkhe.eyp.core.model.Attachment;
import digital.varkhe.eyp.core.model.AttachmentType;
import digital.varkhe.eyp.core.model.ContactInfo;
import digital.varkhe.eyp.core.model.DigestAlgorithm;
import digital.varkhe.eyp.core.model.Distribution;
import digital.varkhe.eyp.core.model.DistributionType;
import digital.varkhe.eyp.core.model.FilingCode;
import digital.varkhe.eyp.core.model.FinalMetadata;
import digital.varkhe.eyp.core.model.Identifier;
import digital.varkhe.eyp.core.model.Metadata;
import digital.varkhe.eyp.core.model.Party;
import digital.varkhe.eyp.core.model.Person;
import digital.varkhe.eyp.core.model.RelatedDocument;
import digital.varkhe.eyp.core.model.SecurityCode;
import digital.varkhe.eyp.core.model.Signer;
import digital.varkhe.eyp.core.model.Urgency;
import digital.varkhe.eyp.core.opc.OpcPackage;
import digital.varkhe.eyp.core.opc.OpcReader;
import digital.varkhe.eyp.core.pkg.PackageBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/** Test fixtures. All identifiers and parties are fictitious. */
public final class TestPackages {
    public static final UUID DOC_ID = UUID.fromString("5f0c8a2e-3b7d-4c1e-9a6f-0d2b4e8c1a73");
    public static final UUID EK_SIGNED = UUID.fromString("a1b2c3d4-0000-4000-8000-000000000001");
    public static final UUID EK_UNSIGNED = UUID.fromString("a1b2c3d4-0000-4000-8000-000000000002");
    public static final UUID ILGI = UUID.fromString("a1b2c3d4-0000-4000-8000-000000000003");
    public static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-24T09:00:00Z"), ZoneOffset.UTC);

    public static final byte[] COVER_LETTER = "%PDF-1.7\n% test cover letter\n%%EOF\n".getBytes(StandardCharsets.US_ASCII);
    public static final byte[] EK1 = "%PDF-1.7\n% ek 1\n%%EOF\n".getBytes(StandardCharsets.US_ASCII);
    public static final byte[] EK2 = "%PDF-1.7\n% ek 2 imzasiz\n%%EOF\n".getBytes(StandardCharsets.US_ASCII);

    public static Metadata metadata() {
        Party sender = new Party.LegalEntity(new Identifier("1234567890", "VKN"), "Örnek Yazılım A.Ş.",
                new ContactInfo(null, null, null, "ornek@hs01.kep.tr", null, null, "Ankara", "Ankara", null, "Türkiye"));
        Party recipient = new Party.PublicInstitution("ABCDEF01234567", "Örnek Bakanlığı", null, null);
        Attachment ek1 = new Attachment(EK_SIGNED, false, null, AttachmentType.DED, "Ek1.pdf", "application/pdf",
                "Fatura listesi", 1, null, null, null, true);
        Attachment ek2 = new Attachment(EK_UNSIGNED, false, null, AttachmentType.DED, "Ek2.pdf", "application/pdf",
                "Bilgi notu", 2, null, null, null, false);
        RelatedDocument ilgi = new RelatedDocument(ILGI, false, "E-12345", OffsetDateTime.parse("2026-10-01T00:00:00+03:00"),
                "a", EK_SIGNED, "Önceki yazı", null, null);
        return new Metadata(DOC_ID, "Sözleşme hk.", SecurityCode.YOK, null, "application/pdf", null,
                List.of(new Distribution(recipient, Urgency.NRM, DistributionType.GRG)),
                List.of(ek1, ek2), List.of(ilgi), null, sender, null, "UstYazi.pdf",
                List.of(new FilingCode("045.02", "Sözleşmeler", null)), "https://dogrulama.example.com/");
    }

    public static PackageBuilder builder() {
        return new PackageBuilder(metadata(), DigestAlgorithm.SHA256, CLOCK)
                .coverLetter(COVER_LETTER).attachment(EK_SIGNED, EK1).attachment(EK_UNSIGNED, EK2);
    }

    public static FinalMetadata finalMetadata() {
        return new FinalMetadata(OffsetDateTime.parse("2026-11-24T12:00:00+03:00"), "2026/15",
                List.of(new Signer(new Party.NaturalPerson(Person.of("Ayşe", "Yılmaz"), null, "Genel Müdür", null),
                        null, null, null, null)));
    }

    /**
     * Stand-in for a CAdES blob in tests that only exercise package structure: an attached signature contains
     * the signed bytes verbatim, which is what K.81/K.100 checks look for. Not a signature.
     */
    public static byte[] fakeAttachedCms(byte[] content) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        b.writeBytes(new byte[]{0x30, (byte) 0x80});
        b.writeBytes(content);
        b.writeBytes(new byte[]{0, 0});
        return b.toByteArray();
    }

    public static byte[] unsigned() throws IOException {
        PackageBuilder b = builder();
        b.buildPaketOzeti();
        return write(b);
    }

    public static byte[] complete() throws IOException {
        PackageBuilder b = builder();
        byte[] po = b.buildPaketOzeti();
        b.signature(fakeAttachedCms(po));
        byte[] no = b.buildNihaiOzet(finalMetadata());
        b.seal(fakeAttachedCms(no));
        return write(b);
    }

    public static byte[] write(PackageBuilder b) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        b.write(out);
        return out.toByteArray();
    }

    public static OpcPackage read(byte[] zip) throws Exception {
        return OpcReader.read(new ByteArrayInputStream(zip));
    }

    /** Rewrites selected ZIP entries (entry name without leading slash -> transformer). */
    public static byte[] rewrite(byte[] zip, Map<String, UnaryOperator<byte[]>> changes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip));
             ZipOutputStream z = new ZipOutputStream(out)) {
            ZipEntry e;
            while ((e = in.getNextEntry()) != null) {
                byte[] data = in.readAllBytes();
                UnaryOperator<byte[]> f = changes.get(e.getName());
                z.putNextEntry(new ZipEntry(e.getName()));
                z.write(f == null ? data : f.apply(data));
                z.closeEntry();
            }
        }
        return out.toByteArray();
    }

    private TestPackages() {
    }
}
