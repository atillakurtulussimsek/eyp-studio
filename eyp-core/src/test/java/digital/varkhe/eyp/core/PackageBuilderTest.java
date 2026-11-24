package digital.varkhe.eyp.core;

import digital.varkhe.eyp.core.opc.OpcPackage;
import digital.varkhe.eyp.core.opc.Relationship;
import digital.varkhe.eyp.core.pkg.EypConstants;
import digital.varkhe.eyp.core.validation.Finding;
import digital.varkhe.eyp.core.validation.PackageValidator;
import digital.varkhe.eyp.core.validation.Severity;
import digital.varkhe.eyp.core.xml.SchemaValidator;
import digital.varkhe.eyp.core.xml.UstveriXml;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackageBuilderTest {

    @Test
    void ustveriAndNihaiUstveriValidateAgainstSchema() {
        assertEquals(List.of(), SchemaValidator.validate(SchemaValidator.Kind.USTVERI, UstveriXml.write(TestPackages.metadata())));
        assertEquals(List.of(), SchemaValidator.validate(SchemaValidator.Kind.NIHAI_USTVERI, UstveriXml.write(TestPackages.finalMetadata())));
    }

    @Test
    void ustveriUsesUpperCaseIds() {
        String xml = new String(UstveriXml.write(TestPackages.metadata()), StandardCharsets.UTF_8);
        assertTrue(xml.contains("5F0C8A2E-3B7D-4C1E-9A6F-0D2B4E8C1A73"));
        assertFalse(xml.contains("5f0c8a2e"));
    }

    @Test
    void unsignedPackageOnlyWarnsAboutMissingSignature() throws Exception {
        List<Finding> f = PackageValidator.validate(TestPackages.read(TestPackages.unsigned()));
        assertEquals(List.of("EYP-IMZA-YOK"), f.stream().map(Finding::rule).toList(), f.toString());
        assertEquals(Severity.WARNING, f.getFirst().severity());
    }

    @Test
    void completePackageHasNoFindings() throws Exception {
        List<Finding> f = PackageValidator.validate(TestPackages.read(TestPackages.complete()));
        assertEquals(List.of(), f);
    }

    @Test
    void layoutFollowsGuide() throws Exception {
        OpcPackage p = TestPackages.read(TestPackages.complete());
        assertTrue(p.part("/UstYazi/UstYazi.pdf").isPresent());
        assertTrue(p.part("/Ekler/Ek1.pdf").isPresent());
        assertTrue(p.part("/ImzasizEkler/Ek2.pdf").isPresent());
        assertTrue(p.part(EypConstants.PART_IMZA).isPresent());
        assertTrue(p.part(EypConstants.PART_MUHUR).isPresent());

        Relationship sig = p.relationships(EypConstants.PART_PAKET_OZETI, EypConstants.REL_IMZA).getFirst();
        assertEquals("IdImzaCades", sig.id());
        assertEquals("../Imzalar/ImzaCades.imz", sig.target());
        Relationship seal = p.relationships(EypConstants.PART_NIHAI_OZET, EypConstants.REL_MUHUR).getFirst();
        assertEquals("IdMuhurCades", seal.id());
        assertEquals(EypConstants.PART_MUHUR, seal.resolvedTarget());
        assertEquals("IdEk_A1B2C3D4-0000-4000-8000-000000000001",
                p.relationships("/", EypConstants.REL_EK).getFirst().id());
        assertEquals("IdImzasizEk_A1B2C3D4-0000-4000-8000-000000000002",
                p.relationships("/", EypConstants.REL_IMZASIZ_EK).getFirst().id());

        assertEquals("application/pdf", p.part("/UstYazi/UstYazi.pdf").get().contentType());
        assertEquals(EypConstants.MIME_OCTET, p.part(EypConstants.PART_IMZA).get().contentType());
        assertEquals(EypConstants.MIME_XML, p.part(EypConstants.PART_USTVERI).get().contentType());
    }

    @Test
    void paketOzetiExcludesUnsignedAttachmentAndPutsSha512First() throws Exception {
        String po = new String(TestPackages.builder().buildPaketOzeti(), StandardCharsets.UTF_8);
        assertTrue(po.contains("URI=\"/Ekler/Ek1.pdf\""));
        assertFalse(po.contains("Ek2.pdf"));
        assertTrue(po.indexOf("xmlenc#sha512") < po.indexOf("xmlenc#sha256"));
        assertTrue(po.contains("Id=\"5F0C8A2E-3B7D-4C1E-9A6F-0D2B4E8C1A73\""));
    }

    @Test
    void tamperedCoverLetterIsDetected() throws Exception {
        byte[] tampered = TestPackages.rewrite(TestPackages.complete(),
                Map.of("UstYazi/UstYazi.pdf", b -> "%PDF-1.7\n% changed\n".getBytes(StandardCharsets.US_ASCII)));
        List<String> rules = PackageValidator.validate(TestPackages.read(tampered)).stream().map(Finding::rule).toList();
        assertTrue(rules.contains("K.33"), rules.toString());
        assertTrue(rules.contains("K.43"), rules.toString());
    }

    @Test
    void tamperedUstveriBreaksSchemaOrDigest() throws Exception {
        byte[] tampered = TestPackages.rewrite(TestPackages.complete(),
                Map.of("Ustveri/Ustveri.xml", b -> new String(b, StandardCharsets.UTF_8)
                        .replace("Sözleşme hk.", "Başka konu").getBytes(StandardCharsets.UTF_8)));
        List<String> rules = PackageValidator.validate(TestPackages.read(tampered)).stream().map(Finding::rule).toList();
        assertTrue(rules.containsAll(List.of("K.33", "K.43", "K.69")), rules.toString());
    }

    @Test
    void signedPackageWithoutSealIsAnError() throws Exception {
        var b = TestPackages.builder();
        b.signature(TestPackages.fakeAttachedCms(b.buildPaketOzeti()));
        b.buildNihaiOzet(TestPackages.finalMetadata());
        List<String> rules = PackageValidator.validate(TestPackages.read(TestPackages.write(b))).stream().map(Finding::rule).toList();
        assertEquals(List.of("EYP-MUHUR-ZORUNLU"), rules);
    }

    @Test
    void detachedSignatureIsReported() throws Exception {
        var b = TestPackages.builder();
        b.buildPaketOzeti();
        b.signature(new byte[]{0x30, 0x03, 1, 2, 3});
        b.seal(TestPackages.fakeAttachedCms(b.buildNihaiOzet(TestPackages.finalMetadata())));
        List<String> rules = PackageValidator.validate(TestPackages.read(TestPackages.write(b))).stream().map(Finding::rule).toList();
        assertEquals(List.of("K.81"), rules);
    }
}
