package cbddo.eYazisma.tipler;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.*;
import urn.cbddo.eyazisma.schema.xsd.ObjectFactory;
import urn.cbddo.eyazisma.schema.xsd.belgeHedef.CTBelgeHedef;
import urn.cbddo.eyazisma.schema.xsd.nihaiOzet.CTNihaiOzet;
import urn.cbddo.eyazisma.schema.xsd.tipler.CTSifreliIcerikBilgisi;
import urn.cbddo.eyazisma.schema.xsd.tipler.CTOlusturan;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;


public class SifreliPaket {

    private OPCPackage _package;
    private PaketModu _paketModu;
    private BelgeHedef _belgeHedef;
    private NihaiOzet _nihaiOzet;
    private SifreliIcerikBilgisi _sifreliIcerikBilgisi;
    private boolean _sifreliIcerikEklenmisMi = false;

    public BelgeHedef getBelgeHedef() {
        return _belgeHedef;
    }

    public NihaiOzet getNihaiOzet() {
        return _nihaiOzet;
    }

    protected SifreliPaket() {
    }

    // =======================================AcKapat===========================START
    public static SifreliPaket ac(InputStream stream) throws Exception {
        SifreliPaket paket = new SifreliPaket();
        paket._package = OPCPackage.open(stream);
        int belgeHedefSayisi = 0;
        if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_BELGEHEDEF) != null) {
            belgeHedefSayisi = paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_BELGEHEDEF).size();
            if (belgeHedefSayisi > 1)
                throw new Exception("Geçersiz e-Yazışma paketi. \"BelgeHedef\" bileşeni birden fazla.");
        }

        if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_CORE) == null || paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_CORE).size() != 1) {
            throw new Exception("Geçersiz e-Yazışma paketi. \"Core\" bileşeni yok.");
        }
        if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_NIHAIOZET) == null || paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_NIHAIOZET).size() != 1) {
            throw new Exception("Geçersiz e-Yazışma paketi. \"NihaiÖzet\" bileşeni yok.");
        }
        if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_SIFRELIICERIKBILGISI) == null || paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_SIFRELIICERIKBILGISI).size() != 1) {
            throw new Exception("Geçersiz e-Yazışma paketi. \"SifreliIcerikBilgisi\" bileşeni yok.");
        }

        if (belgeHedefSayisi > 0) {
            ArrayList<PackagePart> packagePartsBelgeHedef = paket._package.getPartsByRelationshipType(Araclar.RELATION_TYPE_BELGEHEDEF);
            InputStream inputStreamBelgeHedef = packagePartsBelgeHedef.get(0).getInputStream();
            JAXBContext contextBelgeHedef = JAXBContext.newInstance(ObjectFactory.class);
            Unmarshaller unmarshallerBelgeHedef = contextBelgeHedef.createUnmarshaller();
            CTBelgeHedef okunanBelgeHedef = (CTBelgeHedef) JAXBIntrospector.getValue(unmarshallerBelgeHedef.unmarshal(inputStreamBelgeHedef));
            paket._belgeHedef = new BelgeHedefInternal(okunanBelgeHedef);
        } else {
            paket._belgeHedef = new BelgeHedefInternal();
        }

        ArrayList<PackagePart> packagePartsNihaiOzet = paket._package.getPartsByRelationshipType(Araclar.RELATION_TYPE_NIHAIOZET);
        InputStream inputStreamNihaiOzet = packagePartsNihaiOzet.get(0).getInputStream();
        JAXBContext contextNihaiOzet = JAXBContext.newInstance(ObjectFactory.class);
        Unmarshaller unmarshallerPaketOzeti = contextNihaiOzet.createUnmarshaller();
        CTNihaiOzet okunanNihaiOzet = (CTNihaiOzet) JAXBIntrospector.getValue(unmarshallerPaketOzeti.unmarshal(inputStreamNihaiOzet));


        ArrayList<PackagePart> packagePartsSifreliIcerikBilgisi = paket._package.getPartsByRelationshipType(Araclar.RELATION_TYPE_SIFRELIICERIKBILGISI);
        InputStream inputStreamSifreliIcerikBilgisi = packagePartsSifreliIcerikBilgisi.get(0).getInputStream();
        JAXBContext contextSifreliIcerikBilgisi = JAXBContext.newInstance(ObjectFactory.class);
        Unmarshaller unmarshallerSifreliIcerikBilgisi = contextSifreliIcerikBilgisi.createUnmarshaller();
        CTSifreliIcerikBilgisi okunanSifreliIcerikBilgisi = (CTSifreliIcerikBilgisi) JAXBIntrospector.getValue(unmarshallerSifreliIcerikBilgisi.unmarshal(inputStreamSifreliIcerikBilgisi));

        paket._nihaiOzet = new NihaiOzetInternal(okunanNihaiOzet);
        paket._sifreliIcerikBilgisi = new SifreliIcerikBilgisiInternal(okunanSifreliIcerikBilgisi);
        paket._paketModu = PaketModu.Ac;
        return paket;
    }

    public static SifreliPaket ac(OutputStream stream) {
        SifreliPaket paket = new SifreliPaket();
        paket._package = OPCPackage.create(stream);
        paket._nihaiOzet = new NihaiOzetInternal();
        paket._belgeHedef = new BelgeHedefInternal();
        paket._sifreliIcerikBilgisi = new SifreliIcerikBilgisiInternal();
        paket._paketModu = PaketModu.Olustur;
        return paket;
    }

    public static SifreliPaket ac(String dosyaYolu, PaketModu paketModu) throws Exception {
        switch (paketModu) {
            case Guncelle:
                SifreliPaket paket = new SifreliPaket();
                paket._package = OPCPackage.open(dosyaYolu, PackageAccess.READ_WRITE);
                if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_BELGEHEDEF) != null && paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_BELGEHEDEF).size() == 1) {
                    try {
                        ArrayList<PackagePart> packagePartsBelgeHedef = paket._package.getPartsByRelationshipType(Araclar.RELATION_TYPE_BELGEHEDEF);
                        InputStream inputStreamBelgeHedef = packagePartsBelgeHedef.get(0).getInputStream();
                        JAXBContext contextBelgeHedef = JAXBContext.newInstance(ObjectFactory.class);
                        Unmarshaller unmarshallerBelgeHedef = contextBelgeHedef.createUnmarshaller();
                        CTBelgeHedef okunanBelgeHedef = (CTBelgeHedef) JAXBIntrospector.getValue(unmarshallerBelgeHedef.unmarshal(inputStreamBelgeHedef));
                        paket._belgeHedef = new BelgeHedefInternal(okunanBelgeHedef);
                    } catch (Exception ex) {
                        throw new Exception("Geçersiz e-Yazışma paketi. \"BelgeHedef\" bileşeni hatalı.", ex);
                    }
                } else {
                    paket._belgeHedef = new BelgeHedefInternal();
                }
                if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_SIFRELIICERIKBILGISI) != null && paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_SIFRELIICERIKBILGISI).size() == 1) {
                    try {
                        ArrayList<PackagePart> packagePartsSifreliIcerikBilgisi = paket._package.getPartsByRelationshipType(Araclar.RELATION_TYPE_SIFRELIICERIKBILGISI);
                        InputStream inputStreamSifreliIcerikBilgisi = packagePartsSifreliIcerikBilgisi.get(0).getInputStream();
                        JAXBContext contextSifreliIcerikBilgisi = JAXBContext.newInstance(ObjectFactory.class);
                        Unmarshaller unmarshallerSifreliIcerikBilgisi = contextSifreliIcerikBilgisi.createUnmarshaller();
                        CTSifreliIcerikBilgisi okunanSifreliIcerikBilgisi = (CTSifreliIcerikBilgisi) JAXBIntrospector.getValue(unmarshallerSifreliIcerikBilgisi.unmarshal(inputStreamSifreliIcerikBilgisi));
                        paket._sifreliIcerikBilgisi = new SifreliIcerikBilgisiInternal(okunanSifreliIcerikBilgisi);
                    } catch (Exception ex) {
                        throw new Exception("Geçersiz e-Yazışma paketi. \"SifreliIcerikBilgisi\" bileşeni hatalı.", ex);
                    }
                } else {
                    paket._sifreliIcerikBilgisi = new SifreliIcerikBilgisiInternal();
                }

                if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_NIHAIOZET) != null && paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_NIHAIOZET).size() == 1) {
                    try {
                        ArrayList<PackagePart> packagePartsNihaiOzet = paket._package.getPartsByRelationshipType(Araclar.RELATION_TYPE_NIHAIOZET);
                        InputStream inputStreamNihaiOzet = packagePartsNihaiOzet.get(0).getInputStream();
                        JAXBContext contextNihaiOzet = JAXBContext.newInstance(ObjectFactory.class);
                        Unmarshaller unmarshallerNihaiOzet = contextNihaiOzet.createUnmarshaller();
                        CTNihaiOzet okunanNihaiOzet = (CTNihaiOzet) JAXBIntrospector.getValue(unmarshallerNihaiOzet.unmarshal(inputStreamNihaiOzet));
                        paket._nihaiOzet = new NihaiOzetInternal(okunanNihaiOzet);
                    } catch (Exception ex) {
                        throw new Exception("Geçersiz e-Yazışma paketi. \"NihaiOzet\" bileşeni hatalı.", ex);
                    }
                } else {
                    paket._nihaiOzet = new NihaiOzetInternal();
                }
                paket._paketModu = paketModu;
                return paket;
            case Ac:
                return SifreliPaket.ac(new FileInputStream(dosyaYolu));
            case Olustur:
                return SifreliPaket.ac(new FileOutputStream(dosyaYolu));
        }
        return null;
    }


    public void kapat(UUID belgeId, CTOlusturan olusturan, String konu) throws Exception {
        if (_paketModu == PaketModu.Olustur) {
            if (belgeId == null) {
                throw new Exception("belgeId");
            }
            paketBelirteciBelirle(belgeId.toString().toUpperCase());
            if (olusturan == null) {
                throw new IllegalArgumentException("olusturan");
            }
            paketOlusturanBelirle(Araclar.olusturanAdiOlustur(olusturan));
            if (konu != null) {
                paketKonusuBelirle(konu);
            }
            paketKategorisiBelirle(Araclar.RESMIYAZISMASIFRELI);
            paketIcerikTuruBelirle(Araclar.EYAZISMAMIMETURU);
            paketVersiyonuBelirle(Araclar.PAKET_VERSIYON);
            paketRevizyonuBelirle(String.format(Araclar.PAKET_REVIZYON, Paket.class.getPackage().getSpecificationVersion()));

            _package.flush();
            _package.close();

        } else if (_paketModu == PaketModu.Guncelle) {
            if (belgeId == null) {
                throw new Exception("belgeId");
            }
            paketBelirteciBelirle(belgeId.toString().toUpperCase());
            if (olusturan == null) {
                throw new IllegalArgumentException("olusturan");
            }
            paketOlusturanBelirle(Araclar.olusturanAdiOlustur(olusturan));
            if (konu != null) {
                paketKonusuBelirle(konu);
            }
            paketKategorisiBelirle(Araclar.RESMIYAZISMASIFRELI);
            paketIcerikTuruBelirle(Araclar.EYAZISMAMIMETURU);
            paketVersiyonuBelirle(Araclar.PAKET_VERSIYON);
            paketRevizyonuBelirle(String.format(Araclar.PAKET_REVIZYON, Paket.class.getPackage().getSpecificationVersion()));
            _package.flush();
            _package.close();
        } else if (_paketModu == PaketModu.Ac) {
            _package.close();
        }
    }

    // =======================================Ozellikler===========================START
    // =======================================OzelliklerOlusturanBelirler===========================START
    public void paketAciklamasiBelirle(String aciklama) throws Exception {
        if (_paketModu == PaketModu.Ac)
            throw new Exception("PaketModu 'Ac' olarak işlem yapılan paketler için paketAciklamasiBelirle fonksiyonu kullanılamaz.");
        _package.getPackageProperties().setDescriptionProperty(aciklama);
    }

    public void paketSonYazdirilmaTarihiBelirle(Optional<Date> sonYazdirilmaTarihi) throws Exception {
        if (_paketModu == PaketModu.Ac)
            throw new Exception("PaketModu 'Ac' olarak işlem yapılan paketler için paketSonYazdirilmaTarihiBelirle fonksiyonu kullanılamaz.");
        _package.getPackageProperties().setLastPrintedProperty(sonYazdirilmaTarihi);
    }

    public void paketGuncellemeTarihiBelirle(Optional<Date> guncellemeTarihi) throws Exception {
        if (_paketModu == PaketModu.Ac)
            throw new Exception("PaketModu 'Ac' olarak işlem yapılan paketler için paketGuncellemeTarihiBelirle fonksiyonu kullanılamaz.");
        _package.getPackageProperties().setModifiedProperty(guncellemeTarihi);
    }

    public void paketSonGuncelleyenBelirle(String sonGuncelleyen) throws Exception {
        if (_paketModu == PaketModu.Ac)
            throw new Exception("PaketModu 'Ac' olarak işlem yapılan paketler için paketSonGuncelleyenBelirle fonksiyonu kullanılamaz.");
        _package.getPackageProperties().setLastModifiedByProperty(sonGuncelleyen);
    }

    public void paketAnahtarKelimeleriBelirle(String anahtarKelimeler) throws Exception {
        if (_paketModu == PaketModu.Ac)
            throw new Exception("PaketModu 'Ac' olarak işlem yapılan paketler için paketAnahtarKelimeleriBelirle fonksiyonu kullanılamaz.");
        _package.getPackageProperties().setKeywordsProperty(anahtarKelimeler);
    }

    public void paketDurumuBelirle(String durum) throws Exception {
        if (_paketModu == PaketModu.Ac)
            throw new Exception("PaketModu 'Ac' olarak işlem yapılan paketler için paketDurumuBelirle fonksiyonu kullanılamaz.");
        _package.getPackageProperties().setContentStatusProperty(durum);
    }

    public void paketOlusturulmaTarihiBelirle(Date olusturulmaTarihi) throws Exception {
        if (_paketModu == PaketModu.Ac)
            throw new Exception("PaketModu 'Ac' olarak işlem yapılan paketler için paketOlusturulmaTarihiBelirle fonksiyonu kullanılamaz.");
        _package.getPackageProperties().setCreatedProperty(Optional.of(olusturulmaTarihi));
    }

    public void paketBasligiBelirle(String baslik) throws Exception {
        if (_paketModu == PaketModu.Ac)
            throw new Exception("PaketModu 'Ac' olarak işlem yapılan paketler için paketBasligiBelirle fonksiyonu kullanılamaz.");
        _package.getPackageProperties().setTitleProperty(baslik);
    }

    public void paketDiliBelirle(String dil) throws Exception {
        if (_paketModu == PaketModu.Ac)
            throw new Exception("PaketModu 'Ac' olarak işlem yapılan paketler için paketDiliBelirle fonksiyonu kullanılamaz.");
        _package.getPackageProperties().setLanguageProperty(dil);
    }

    // =======================================OzelliklerUstveriIleAyni===========================START
    private void paketBelirteciBelirle(String belirtec) throws InvalidFormatException {
        _package.getPackageProperties().setIdentifierProperty(belirtec);
    }

    private void paketOlusturanBelirle(String olusturan) throws InvalidFormatException {
        _package.getPackageProperties().setCreatorProperty(olusturan);
    }

    private void paketKonusuBelirle(String konu) throws InvalidFormatException {
        _package.getPackageProperties().setSubjectProperty(konu);
    }

    // =======================================OzelliklerStatic===========================START
    private void paketKategorisiBelirle(String kategori) throws InvalidFormatException // OPCStatic
    {
        _package.getPackageProperties().setCategoryProperty(kategori);
    }

    private void paketIcerikTuruBelirle(String icerikTuru) throws InvalidFormatException // OPCStatic
    {
        _package.getPackageProperties().setContentTypeProperty(icerikTuru);
    }

    private void paketVersiyonuBelirle(String versiyon) throws InvalidFormatException // DCStatic
    {
        _package.getPackageProperties().setVersionProperty(versiyon);
    }

    private void paketRevizyonuBelirle(String revisyon) throws InvalidFormatException // DCOlusturanBelirler
    {
        _package.getPackageProperties().setRevisionProperty(revisyon);
    }

    // =======================================NIHAIOZET===========================START
    public InputStream nihaiOzetAl() throws Exception {
        PackageRelationshipCollection colNihaiOzet = _package.getRelationshipsByType(Araclar.RELATION_TYPE_NIHAIOZET);
        if (colNihaiOzet == null || colNihaiOzet.size() == 0) {
            throw new Exception("'NihaiOzet' iliskisi bulunamadi.");
        }
        return _package.getPart(_package.getRelationshipsByType(Araclar.RELATION_TYPE_NIHAIOZET).getRelationship(0)).getInputStream();
    }

    public void nihaiOzetEkle(InputStream nihaiOzet) throws Exception {
        if (_paketModu == PaketModu.Ac)
            throw new Exception("PaketModu 'Ac' olarak işlem yapılan paketler için nihaiOZetEkle fonksiyonu kullanılamaz.");
        if (nihaiOzet == null) {
            throw new Exception("nihaiOzet");
        }
        PackagePartName partNameNihaiOzet = PackagingURIHelper.createPartName(Araclar.URI_NIHAIOZET);
        if (_paketModu == PaketModu.Guncelle) {
            if (_package.containPart(partNameNihaiOzet)) {
                _package.deletePartRecursive(partNameNihaiOzet);
            }
        }
        _package.addRelationship(partNameNihaiOzet, TargetMode.INTERNAL, Araclar.RELATION_TYPE_NIHAIOZET, Araclar.ID_NIHAIOZET);
        PackagePart partNihaiOzet = _package.createPart(partNameNihaiOzet, Araclar.MIME_XML);
        OutputStream streamNihaiOzet = partNihaiOzet.getOutputStream();
        Araclar.copyStream(nihaiOzet, streamNihaiOzet);
        streamNihaiOzet.close();

        JAXBContext contextNihaiOzet = JAXBContext.newInstance(ObjectFactory.class);
        Unmarshaller unmarshallerNihaiOzet = contextNihaiOzet.createUnmarshaller();
        CTNihaiOzet okunanNihaiOzet = (CTNihaiOzet) JAXBIntrospector.getValue(unmarshallerNihaiOzet.unmarshal(_package.getPart(partNameNihaiOzet).getInputStream()));
        _nihaiOzet = new NihaiOzetInternal(okunanNihaiOzet);
        _nihaiOzet.kontrolEt();
    }

    // =======================================Hedefler===========================START
    public InputStream belgeHedefiAl() throws Exception {
        PackageRelationshipCollection colBelgeHedef = _package.getRelationshipsByType(Araclar.RELATION_TYPE_BELGEHEDEF);
        if (colBelgeHedef == null || colBelgeHedef.size() == 0) {
            throw new Exception("'BelgeHedef' iliskisi bulunamadi.");
        }
        return _package.getPart(_package.getRelationshipsByType(Araclar.RELATION_TYPE_BELGEHEDEF).getRelationship(0)).getInputStream();
    }

    public void belgeHedefEkle(InputStream belgeHedef) throws Exception {
        if (_paketModu == PaketModu.Ac)
            throw new Exception("PaketModu 'Ac' olarak işlem yapılan paketler için belgeHedefEkle fonksiyonu kullanılamaz.");
        if (belgeHedef == null) {
            throw new Exception("belgeHedef");
        }
        PackagePartName partNameBelgeHedef = PackagingURIHelper.createPartName(Araclar.URI_BELGEHEDEF);
        if (_paketModu == PaketModu.Guncelle) {
            if (_package.containPart(partNameBelgeHedef)) {
                _package.deletePartRecursive(partNameBelgeHedef);
            }
        }
        _package.addRelationship(partNameBelgeHedef, TargetMode.INTERNAL, Araclar.RELATION_TYPE_BELGEHEDEF, Araclar.ID_BELGEHEDEF);
        PackagePart partBelgeHedef = _package.createPart(partNameBelgeHedef, Araclar.MIME_XML);
        OutputStream streamBelgeHedef = partBelgeHedef.getOutputStream();
        Araclar.copyStream(belgeHedef, streamBelgeHedef);
        streamBelgeHedef.close();
        JAXBContext contextBelgeHedef = JAXBContext.newInstance(ObjectFactory.class);
        Unmarshaller unmarshallerBelgeHedef = contextBelgeHedef.createUnmarshaller();
        CTBelgeHedef okunanBelgeHedef = (CTBelgeHedef) JAXBIntrospector.getValue(unmarshallerBelgeHedef.unmarshal(_package.getPart(partNameBelgeHedef).getInputStream()));
        _belgeHedef = new BelgeHedefInternal(okunanBelgeHedef);
        _belgeHedef.kontrolEt();
    }

    public void sifreliIcerikBilgisiOlustur() throws Exception {
        if (_paketModu == PaketModu.Ac)
            throw new Exception("PaketModu 'Ac' olarak işlem yapılan paketler için sifreliIcerikBilgisiOlustur fonksiyonu kullanılamaz.");
        _sifreliIcerikBilgisi.getSifreliIcerikBilgisi().setId("");
        _sifreliIcerikBilgisi.getSifreliIcerikBilgisi().getURI().add(Araclar.SIFRELEME_YONTEMI_URI_1);
        _sifreliIcerikBilgisi.getSifreliIcerikBilgisi().getURI().add(Araclar.SIFRELEME_YONTEMI_URI_2);
        _sifreliIcerikBilgisi.getSifreliIcerikBilgisi().setYontem(Araclar.SIFRELEME_YONTEMI);
        _sifreliIcerikBilgisi.getSifreliIcerikBilgisi().setVersion(Araclar.SIFRELEME_YONTEMI_VERSIYONU);
        _sifreliIcerikBilgisi.kontrolEt();
        PackagePartName partNameSifreliIcerikBilgisi = PackagingURIHelper.createPartName(Araclar.URI_SIFRELIICERIKBILGISI);
        if (_paketModu == PaketModu.Guncelle) {
            if (_package.containPart(partNameSifreliIcerikBilgisi)) {
                _package.deletePartRecursive(partNameSifreliIcerikBilgisi);
            }
        }
        _package.addRelationship(partNameSifreliIcerikBilgisi, TargetMode.INTERNAL, Araclar.RELATION_TYPE_SIFRELIICERIKBILGISI, Araclar.ID_SIFRELIICERIKBILGISI);
        PackagePart partSifreliIcerikBilgisi = _package.createPart(partNameSifreliIcerikBilgisi, Araclar.MIME_XML);
        OutputStream streamSifreliIcerikBilgisi = partSifreliIcerikBilgisi.getOutputStream();
        JAXBContext contextSifreliIcerikBilgisi = JAXBContext.newInstance(ObjectFactory.class);
        Marshaller marshallerSifreliIcerikBilgisi = contextSifreliIcerikBilgisi.createMarshaller();
        marshallerSifreliIcerikBilgisi.marshal(_sifreliIcerikBilgisi.getSifreliIcerikBilgisi(), streamSifreliIcerikBilgisi);
        streamSifreliIcerikBilgisi.close();
    }

    public void sifreliIcerikEkle(FileInputStream dosya, UUID paketId) throws Exception {
        if (_sifreliIcerikEklenmisMi)
            throw new Exception("Daha önce şifreli içerik eklenmiş.");
        if (dosya == null)
            throw new Exception("dosya");
        if (paketId == null)
            throw new Exception("paketId");
        if (_paketModu == PaketModu.Ac)
            throw new Exception("PaketModu 'Ac' olarak işlem yapılan paketler için sifreliIcerikBilgisiEkle fonksiyonu kullanılamaz.");

        PackagePartName partNameSifreliIcerik = PackagingURIHelper.createPartName(String.format(Araclar.URI_FORMAT_SIFRELIICERIK, paketId.toString().toUpperCase()));
        PackagePart partSifreliIcerik = _package.createPart(partNameSifreliIcerik, "application/pkcs7-mime");
        OutputStream streamSifreliIcerik = partSifreliIcerik.getOutputStream();
        byte fileData[] = Araclar.inputStreamToBytes(dosya);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fileData);
        Araclar.copyStream(byteArrayInputStream, streamSifreliIcerik);
        _package.addRelationship(partNameSifreliIcerik, TargetMode.INTERNAL, Araclar.RELATION_TYPE_SIFRELIICERIK, Araclar.ID_SIFRELIICERIK);
        streamSifreliIcerik.close();
        partSifreliIcerik.close();
        dosya.close();
        _sifreliIcerikEklenmisMi = true;
    }

    public InputStream sifreliIcerikal() throws Exception {
        PackageRelationshipCollection colSifreliIcerik = _package.getRelationshipsByType(Araclar.RELATION_TYPE_SIFRELIICERIK);
        if (colSifreliIcerik == null || colSifreliIcerik.size() == 0) {
            throw new Exception("'SifreliIcerik' iliskisi bulunamadi.");
        }
        return _package.getPart(_package.getRelationshipsByType(Araclar.RELATION_TYPE_SIFRELIICERIK).getRelationship(0)).getInputStream();
    }

    public URI sifreliIcerikDosyasiAdiAl() throws Exception {
        if (_package.getRelationship(Araclar.ID_SIFRELIICERIK) != null) {
            return _package.getRelationship(Araclar.ID_SIFRELIICERIK).getTargetURI();
        } else {
            throw new Exception("Şifreli içerik bulunamadı.");
        }
    }

    public void belgeHedefOlustur() throws Exception {
        if (_paketModu == PaketModu.Ac) {
            throw new Exception("PaketModu 'Ac' olarak işlem yapılan paketler için BelgeHedefOlustur fonksiyonu kullanılamaz.");
        }
        _belgeHedef.kontrolEt();
        PackagePartName partNameBelgeHedef = PackagingURIHelper.createPartName(Araclar.URI_BELGEHEDEF);
        if (_paketModu == PaketModu.Guncelle) {
            _package.removePart(partNameBelgeHedef);
            _package.removeRelationship(Araclar.ID_BELGEHEDEF);
            PackagePartName relationshipPartName = PackagingURIHelper.getRelationshipPartName(partNameBelgeHedef);
            if (_package.containPart(relationshipPartName)) {
                _package.removePart(relationshipPartName);
            }
        }
        _package.addRelationship(partNameBelgeHedef, TargetMode.INTERNAL, Araclar.RELATION_TYPE_BELGEHEDEF, Araclar.ID_BELGEHEDEF);
        PackagePart partBelgeHedef = _package.createPart(partNameBelgeHedef, Araclar.MIME_XML);
        OutputStream streamNihaiOzet = partBelgeHedef.getOutputStream();
        JAXBContext contextNihaiOzet = JAXBContext.newInstance(ObjectFactory.class);
        Marshaller marshallerNihaiOzet = contextNihaiOzet.createMarshaller();
        marshallerNihaiOzet.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshallerNihaiOzet.marshal(_belgeHedef.getCTBelgeHedef(), streamNihaiOzet);
        streamNihaiOzet.close();
    }
}
