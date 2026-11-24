package digital.varkhe.eyp.crosscheck;

import cbddo.eYazisma.tipler.Araclar;
import cbddo.eYazisma.tipler.OzetDogrulamaHatasi;
import cbddo.eYazisma.tipler.OzetModu;
import cbddo.eYazisma.tipler.Paket;
import cbddo.eYazisma.tipler.PaketModu;
import cbddo.eYazisma.tipler.PaketTipi;
import digital.varkhe.eyp.core.TestPackages;
import digital.varkhe.eyp.core.opc.OpcReader;
import digital.varkhe.eyp.core.validation.Finding;
import digital.varkhe.eyp.core.validation.PackageValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import urn.cbddo.eyazisma.schema.xsd.kodguvenlik.STKodGuvenlikKodu;
import urn.cbddo.eyazisma.schema.xsd.koddagitimturu.STKodDagitimTuru;
import urn.cbddo.eyazisma.schema.xsd.kodivedilik.STKodIvedilik;
import urn.cbddo.eyazisma.schema.xsd.tipler.CTDagitim;
import urn.cbddo.eyazisma.schema.xsd.tipler.CTDogrulamaBilgisi;
import urn.cbddo.eyazisma.schema.xsd.tipler.CTKurumKurulus;
import urn.cbddo.eyazisma.schema.xsd.tipler.CTTuzelSahis;
import urn.cbddo.eyazisma.schema.xsd.codelist.IdentifierType;
import urn.cbddo.eyazisma.schema.xsd.codelist.NameType;
import urn.cbddo.eyazisma.schema.xsd.codelist.TextType;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Opens packages produced by eyp-core with the official e-Yazışma Java API 2.1, and vice versa. */
class OfficialApiCrossCheckTest {

    @Test
    void officialApiReadsCompletePackageAndAcceptsBothDigests() throws Exception {
        byte[] eyp = TestPackages.complete();
        assertEquals(2, Araclar.PaketVersiyonuGetir(new ByteArrayInputStream(eyp)).orElseThrow());
        assertEquals(PaketTipi.NormalPaket, Araclar.paketTipiGetir(new ByteArrayInputStream(eyp)).orElseThrow());

        Paket paket = Paket.ac(new ByteArrayInputStream(eyp)); // read mode requires all 2.x components
        assertEquals(TestPackages.DOC_ID.toString().toUpperCase(java.util.Locale.ROOT), paket.getUstveri().belgeIdAl());
        assertEquals("Sözleşme hk.", paket.getUstveri().konuAl().getValue());
        assertEquals(2, paket.getUstveri().ekleriAl().size());
        assertTrue(paket.imzaVarMi());
        assertTrue(paket.muhurVarMi());

        List<OzetDogrulamaHatasi> po = new ArrayList<>();
        paket.paketOzetiDogrula(Araclar.paketOzetiAl(paket.paketOzetiAl().readAllBytes()), po);
        assertEquals(List.of(), messages(po));

        List<OzetDogrulamaHatasi> no = new ArrayList<>();
        paket.nihaiOzetDogrula(Araclar.nihaiOzetAl(paket.nihaiOzetAl().readAllBytes()), no);
        assertEquals(List.of(), messages(no));

        paket.ekleriKontrolEt();
        paket.ilgileriKontrolEt();
        paket.kapat();
    }

    @Test
    void officialApiValidatesUnsignedPackageInUpdateMode(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("p.eyp");
        Files.write(file, TestPackages.unsigned());
        Paket paket = Paket.ac(file.toString(), PaketModu.Guncelle); // read mode refuses packages without signature
        List<OzetDogrulamaHatasi> po = new ArrayList<>();
        paket.paketOzetiDogrula(Araclar.paketOzetiAl(paket.paketOzetiAl().readAllBytes()), po);
        assertEquals(List.of(), messages(po));
        paket.ekleriKontrolEt();
    }

    /** Reverse direction: what our validator says about a package built by the official API (fake signatures). */
    @Test
    void ourValidatorOnOfficialApiOutput(@TempDir Path dir) throws Exception {
        Path file = dir.resolve(UUID.randomUUID().toString().toUpperCase(java.util.Locale.ROOT) + ".eyp");
        Path cover = dir.resolve("ustyazi.pdf");
        Files.write(cover, TestPackages.COVER_LETTER);

        Paket p = Paket.ac(new FileOutputStream(file.toFile()));
        p.setVarsayilanOzetModu(OzetModu.SHA384);
        p.getUstveri().belgeIdBelirle(UUID.randomUUID());
        TextType konu = new TextType();
        konu.setValue("Resmî API çıktısı");
        p.getUstveri().konuBelirle(konu);
        p.getUstveri().guvenlikKoduBelirle(STKodGuvenlikKodu.YOK);
        CTTuzelSahis sender = new CTTuzelSahis();
        IdentifierType vkn = new IdentifierType();
        vkn.setValue("1234567890");
        vkn.setSchemeID("VKN");
        sender.setId(vkn);
        p.getUstveri().olusturanBelirle(sender);
        CTDagitim d = new CTDagitim();
        CTKurumKurulus k = new CTKurumKurulus();
        k.setKKK("ABCDEF01234567");
        NameType ad = new NameType();
        ad.setValue("Örnek Bakanlığı");
        k.setAdi(ad);
        d.setKurumKurulus(k);
        d.setIvedilik(STKodIvedilik.NRM);
        d.setDagitimTuru(STKodDagitimTuru.GRG);
        p.getUstveri().dagitimEkle(d);
        p.getUstveri().dosyaAdiBelirle("ustyazi.pdf");
        CTDogrulamaBilgisi db = new CTDogrulamaBilgisi();
        db.setDogrulamaAdresi("https://dogrulama.example.com/");
        p.getUstveri().getCTUstveri().setDogrulamaBilgisi(db);
        p.ustYaziEkle(cover.toString(), "application/pdf");
        p.ustveriOlustur();
        p.paketOzetiOlustur();
        p.imzaEkle(TestPackages.fakeAttachedCms(p.paketOzetiAl().readAllBytes()));
        p.getNihaiUstveri().belgeNoBelirle("1");
        p.getNihaiUstveri().tarihBelirle(Calendar.getInstance());
        var imza = new urn.cbddo.eyazisma.schema.xsd.tipler.CTImza();
        var gs = new urn.cbddo.eyazisma.schema.xsd.tipler.CTGercekSahis();
        var kisi = new urn.cbddo.eyazisma.schema.xsd.tipler.CTKisi();
        NameType ilk = new NameType();
        ilk.setValue("Ayşe");
        NameType soy = new NameType();
        soy.setValue("Yılmaz");
        kisi.setIlkAdi(ilk);
        kisi.setSoyadi(soy);
        gs.setKisi(kisi);
        imza.setImzalayan(gs);
        p.getNihaiUstveri().imzaEkle(imza);
        p.nihaiUstVeriOlustur();
        p.nihaiOzetOlustur();
        p.muhurEkle(TestPackages.fakeAttachedCms(p.nihaiOzetAl().readAllBytes()));
        p.kapat();

        byte[] bytes = Files.readAllBytes(file);
        List<Finding> findings = PackageValidator.validate(OpcReader.read(new ByteArrayInputStream(bytes)));
        // Recorded for docs/xsd-differences.md. Expected: NihaiOzet lacks the Core digest unless the caller adds it
        // separately (the API's own test app does this in nihaiOzeteCoreEkle).
        findings.forEach(f -> System.out.println("OFFICIAL-API-OUTPUT " + f));
        List<String> rules = findings.stream().map(Finding::rule).toList();
        assertEquals(List.of("K.43"), rules, findings.toString());
    }

    private static List<String> messages(List<OzetDogrulamaHatasi> l) {
        return l.stream().map(e -> e.getHataKodu() + " " + e.getHata()).toList();
    }
}
