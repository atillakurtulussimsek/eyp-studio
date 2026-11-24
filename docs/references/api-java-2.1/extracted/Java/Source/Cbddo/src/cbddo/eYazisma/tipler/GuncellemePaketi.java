package cbddo.eYazisma.tipler;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.*;
import org.apache.poi.util.IOUtils;
import urn.cbddo.eyazisma.schema.xsd.ObjectFactory;
import urn.cbddo.eyazisma.schema.xsd.kodekturu.STKodEkTuru;
import urn.cbddo.eyazisma.schema.xsd.nihaiOzet.CTNihaiOzet;
import urn.cbddo.eyazisma.schema.xsd.paketOzeti.CTDigestItem;
import urn.cbddo.eyazisma.schema.xsd.paketOzeti.CTReference;
import urn.cbddo.eyazisma.schema.xsd.tipler.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.URI;
import java.util.*;

public class GuncellemePaketi {
    private OPCPackage _package;
    private PaketModu _paketModu;
    private boolean _orijinalPaketEklenmisMi;

    private GuncellemeBilgisi _guncellemeBilgisi;
    private NihaiOzet _nihaiOzet;
    private OzetModu _varsayilanOzetModu;

    public GuncellemeBilgisi getGuncellemeBilgisi() {
        return _guncellemeBilgisi;
    }

    public NihaiOzet getNihaiOzet() {
        return _nihaiOzet;
    }

    public OzetModu getVarsayilanOzetModu() {
        return _varsayilanOzetModu;
    }

    public void setVarsayilanOzetModu(OzetModu varsayilanOzetModu) {
        _varsayilanOzetModu = varsayilanOzetModu;
    }

    protected GuncellemePaketi() {
        _varsayilanOzetModu = OzetModu.SHA384;
    }

    public static GuncellemePaketi ac(InputStream stream) throws Exception {
        GuncellemePaketi paket = new GuncellemePaketi();
        paket._package = OPCPackage.open(stream);

        if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_CORE) == null || paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_CORE).size() != 1) {
            throw new Exception("Geçersiz e-Yazışma paketi. \"Core\" bileşeni yok.");
        }
        if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_NIHAIOZET) == null || paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_NIHAIOZET).size() != 1) {
            throw new Exception("Geçersiz e-Yazışma paketi. \"NihaiÖzet\" bileşeni yok.");
        }
        if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_ORIJINAL_PAKET) == null || paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_ORIJINAL_PAKET).size() != 1) {
            throw new Exception("Geçersiz e-Yazışma paketi. \"SifreliIcerikBilgisi\" bileşeni yok.");
        }
        if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_GUNCELLEME_BILGISI) == null || paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_GUNCELLEME_BILGISI).size() != 1) {
            throw new Exception("Geçersiz e-Yazışma paketi. \"GuncellemeBilgisi\" bileşeni yok.");
        }


        ArrayList<PackagePart> packagePartsNihaiOzet = paket._package.getPartsByRelationshipType(Araclar.RELATION_TYPE_NIHAIOZET);
        InputStream inputStreamNihaiOzet = packagePartsNihaiOzet.get(0).getInputStream();
        JAXBContext contextNihaiOzet = JAXBContext.newInstance(ObjectFactory.class);
        Unmarshaller unmarshallerPaketOzeti = contextNihaiOzet.createUnmarshaller();
        CTNihaiOzet okunanNihaiOzet = (CTNihaiOzet) JAXBIntrospector.getValue(unmarshallerPaketOzeti.unmarshal(inputStreamNihaiOzet));

        ArrayList<PackagePart> packagePartGuncellemeBilgisi = paket._package.getPartsByRelationshipType(Araclar.RELATION_TYPE_GUNCELLEME_BILGISI);
        InputStream inputStreamGuncellemeBilgisi = packagePartGuncellemeBilgisi.get(0).getInputStream();
        JAXBContext contextGuncellemeBilgisi = JAXBContext.newInstance(ObjectFactory.class);
        Unmarshaller unmarshallerSifreliIcerikBilgisi = contextGuncellemeBilgisi.createUnmarshaller();
        CTGuncellemeBilgisi okunanGuncellemeBilgisi = (CTGuncellemeBilgisi) JAXBIntrospector.getValue(unmarshallerSifreliIcerikBilgisi.unmarshal(inputStreamGuncellemeBilgisi));

        paket._nihaiOzet = new NihaiOzetInternal(okunanNihaiOzet);
        paket._guncellemeBilgisi = new GuncellemeBilgisiInternal(okunanGuncellemeBilgisi);
        paket._guncellemeBilgisi.kontrolEt();
        paket._paketModu = PaketModu.Ac;
        return paket;
    }

    public static GuncellemePaketi ac(OutputStream stream) {
        GuncellemePaketi paket = new GuncellemePaketi();
        paket._package = OPCPackage.create(stream);
        paket._nihaiOzet = new NihaiOzetInternal();
        paket._guncellemeBilgisi = new GuncellemeBilgisiInternal();
        paket._paketModu = PaketModu.Olustur;
        return paket;
    }

    public static GuncellemePaketi ac(String dosyaYolu, PaketModu paketModu) throws Exception {
        switch (paketModu) {
            case Guncelle:
                GuncellemePaketi paket = new GuncellemePaketi();
                paket._package = OPCPackage.open(dosyaYolu, PackageAccess.READ_WRITE);

                if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_GUNCELLEME_BILGISI) != null
                        && paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_GUNCELLEME_BILGISI).size() == 1) {
                    try {
                        ArrayList<PackagePart> packagePartGuncellemeBilgisi = paket._package.getPartsByRelationshipType(Araclar.RELATION_TYPE_GUNCELLEME_BILGISI);
                        InputStream inputStreamGuncellemeBilgisi = packagePartGuncellemeBilgisi.get(0).getInputStream();
                        JAXBContext contextGuncellemeBilgisi = JAXBContext.newInstance(ObjectFactory.class);
                        Unmarshaller unmarshallerGuncellemeBilgisi = contextGuncellemeBilgisi.createUnmarshaller();
                        CTGuncellemeBilgisi okunanGuncellemeBilgisi = (CTGuncellemeBilgisi) JAXBIntrospector.getValue(unmarshallerGuncellemeBilgisi.unmarshal(inputStreamGuncellemeBilgisi));
                        paket._guncellemeBilgisi = new GuncellemeBilgisiInternal(okunanGuncellemeBilgisi);
                    } catch (Exception ex) {
                        throw new Exception("Geçersiz e-Yazışma paketi. \"GuncellemeBilgisi\" bileşeni hatalı.", ex);
                    }
                } else {
                    paket._guncellemeBilgisi = new GuncellemeBilgisiInternal();
                }

                if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_NIHAIOZET) != null
                        && paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_NIHAIOZET).size() == 1) {
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
                return GuncellemePaketi.ac(new FileInputStream(dosyaYolu));
            case Olustur:
                return GuncellemePaketi.ac(new FileOutputStream(dosyaYolu));
        }
        return null;
    }

    public void belirtecleriBelirle(UUID belgeId, CTOlusturan olusturan, String konu) throws Exception {
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
        paketOlusturulmaTarihiBelirle(new Date());
        paketKategorisiBelirle(Araclar.RESMIYAZISMAGUNCELLEME);
        paketIcerikTuruBelirle(Araclar.EYAZISMAMIMETURU);
        paketVersiyonuBelirle(Araclar.PAKET_VERSIYON_2_1);
        paketRevizyonuBelirle(String.format(Araclar.PAKET_REVIZYON, Paket.class.getPackage().getSpecificationVersion()));

    }

    public void kapat() throws Exception {
        if (_paketModu == PaketModu.Olustur) {
            _package.flush();
            _package.close();
        } else if (_paketModu == PaketModu.Guncelle) {
            _package.flush();
            _package.close();
        } else if (_paketModu == PaketModu.Ac) {
            _package.close();
        }
    }

    public void paketGuncellemeTarihiBelirle(Optional<Date> guncellemeTarihi) throws Exception {
        if (_paketModu == PaketModu.Ac)
            throw new Exception("PaketModu 'Ac' olarak işlem yapılan paketler için paketGuncellemeTarihiBelirle fonksiyonu kullanılamaz.");
        _package.getPackageProperties().setModifiedProperty(guncellemeTarihi);
    }

    private void paketBelirteciBelirle(String belirtec) throws Exception {
        if (belirtec == null) {
            throw new Exception("belirtec");
        }
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

    public void paketOlusturulmaTarihiBelirle(Date olusturulmaTarihi) throws Exception {
        if (_paketModu == PaketModu.Ac)
            throw new Exception("PaketModu 'Ac' olarak işlem yapılan paketler için paketOlusturulmaTarihiBelirle fonksiyonu kullanılamaz.");

        _package.getPackageProperties().setCreatedProperty(Optional.of(olusturulmaTarihi));
    }

    public InputStream guncellemeBilgisiAl() throws InvalidFormatException, IOException {
        if (_package.containPart(PackagingURIHelper.createPartName(Araclar.URI_GUNCELLEME_BILGISI))) {
            return _package.getPart(PackagingURIHelper.createPartName(Araclar.URI_GUNCELLEME_BILGISI)).getInputStream();
        }
        return null;
    }

    public void guncellemeBilgisiOlustur() throws Exception {

        if (_paketModu == PaketModu.Ac) {
            throw new Exception("PaketModu 'Ac' olarak işlem yapılan paketler için guncellemeBilgisiOlustur fonksiyonu kullanılamaz.");
        }

        XsdAraclar.kontrolEt(_guncellemeBilgisi.getGuncellemeBilgisi());
        PackagePartName guncellemeBilgisiPartName = PackagingURIHelper.createPartName(Araclar.URI_GUNCELLEME_BILGISI);
        if (_paketModu == PaketModu.Guncelle) {
            if (_package.containPart(guncellemeBilgisiPartName)) {
                _package.deletePartRecursive(guncellemeBilgisiPartName);
            }
        }

        _package.addRelationship(guncellemeBilgisiPartName, TargetMode.INTERNAL, Araclar.RELATION_TYPE_GUNCELLEME_BILGISI,
                Araclar.ID_GUNCELLEME_BILGISI);
        PackagePart part = _package.createPart(guncellemeBilgisiPartName, Araclar.MIME_XML);

        OutputStream stream = part.getOutputStream();
        JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(_guncellemeBilgisi.getGuncellemeBilgisi(), stream);
        stream.close();
    }

    public boolean guncellemeBilgisiVarMi() throws InvalidFormatException {
        return _package.containPart(PackagingURIHelper.createPartName(Araclar.URI_GUNCELLEME_BILGISI));
    }

    public void orijinalPaketEkle(FileInputStream dosya, PaketTipi paketTipi) throws Exception {
        if (paketTipi == PaketTipi.SifreliPaket) {
            throw new Exception("Paket tipi SifreliPaket kullanılamaz");
        }
        if (_orijinalPaketEklenmisMi) {
            throw new Exception("Daha önce orijinal paket eklenmiş.");
        }
        if (dosya == null) {
            throw new Exception("dosya");
        }
        if (_paketModu == PaketModu.Ac) {
            throw new Exception("PaketModu 'Ac' olarak işlem yapılan paketler için orijinalPaketEkle fonksiyonu kullanılamaz.");
        }
        String dosyaUzantisi = "";
        if (paketTipi == PaketTipi.NormalPaket) {
            dosyaUzantisi = "eyp";
        } else if (paketTipi == PaketTipi.GuncellemePaketi) {
            dosyaUzantisi = "eypg";
        }

        PackagePartName partNameOrijinalPaket = PackagingURIHelper.createPartName(String.format(Araclar.URI_FORMAT_ORIJINAL_PAKET, dosyaUzantisi));
        PackagePart paketOrijinalPaket = _package.createPart(partNameOrijinalPaket, "application/pkcs7-mime");
        OutputStream streamOrijinalPaket = paketOrijinalPaket.getOutputStream();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Araclar.inputStreamToBytes(dosya));
        Araclar.copyStream(byteArrayInputStream, streamOrijinalPaket);
        _package.addRelationship(partNameOrijinalPaket, TargetMode.INTERNAL, Araclar.RELATION_TYPE_ORIJINAL_PAKET, Araclar.ID_ORIJINAL_PAKET);
        streamOrijinalPaket.close();
        paketOrijinalPaket.close();
        dosya.close();
        _orijinalPaketEklenmisMi = true;
    }

    public InputStream orijinalPaketAl() throws Exception {
        PackageRelationshipCollection colOrijinalPaket = _package.getRelationshipsByType(Araclar.RELATION_TYPE_ORIJINAL_PAKET);
        if (colOrijinalPaket == null || colOrijinalPaket.size() == 0) {
            throw new Exception("'OrijinalPaket' iliskisi bulunamadi.");
        }
        return _package.getPart(_package.getRelationshipsByType(Araclar.RELATION_TYPE_ORIJINAL_PAKET).getRelationship(0)).getInputStream();
    }

    public URI orijinalPaketDosyasiAdiAl() throws Exception {
        if (_package.getRelationship(Araclar.ID_ORIJINAL_PAKET) != null) {
            return _package.getRelationship(Araclar.ID_ORIJINAL_PAKET).getTargetURI();
        } else {
            throw new Exception("Orijinal paket  bulunamadı.");
        }
    }

    public boolean orijinalPaketVarMi() throws Exception {
        PackageRelationshipCollection colSifreliIcerik = _package.getRelationshipsByType(Araclar.RELATION_TYPE_ORIJINAL_PAKET);
        if (colSifreliIcerik == null || colSifreliIcerik.size() == 0) {
            throw new Exception("'OrijinalPaket' iliskisi bulunamadi.");
        }
        return _package.containPart(PackagingURIHelper.createPartName(_package.getRelationshipsByType(Araclar.RELATION_TYPE_ORIJINAL_PAKET).getRelationship(0).getTargetURI()));
    }


    // =======================================Nihai Ozet START===========================

    public InputStream nihaiOzetAl() throws Exception {
        PackageRelationshipCollection colNihaiOzet = _package.getRelationshipsByType(Araclar.RELATION_TYPE_NIHAIOZET);
        if (colNihaiOzet == null || colNihaiOzet.size() == 0) {
            return null;
        }
        return _package.getPart(_package.getRelationshipsByType(Araclar.RELATION_TYPE_NIHAIOZET).getRelationship(0)).getInputStream();
    }

    public boolean nihaiOzetVarMi() throws InvalidFormatException {
        return _package.containPart(PackagingURIHelper.createPartName(Araclar.URI_NIHAIOZET));
    }

    public boolean nihaiOzetDogrula(CTNihaiOzet ozet, List<OzetDogrulamaHatasi> sonuclar) throws Exception {
        if (sonuclar == null)
            return false;

        if (ozet == null) {
            sonuclar.add(new OzetDogrulamaHatasi("Ozet değeri verilmemiş.", OzetDogrulamaHataKodu.OZET_DEGERI_VERILMEMIS));
            return false;
        }
        if (_package.getRelationshipsByType(Araclar.RELATION_TYPE_GUNCELLEME_BILGISI) == null
                || _package.getRelationshipsByType(Araclar.RELATION_TYPE_GUNCELLEME_BILGISI).size() == 0) {
            sonuclar.add(new OzetDogrulamaHatasi("Güncelleme bilgisi bileşeni yok.", OzetDogrulamaHataKodu.GUNCELLEME_BILGISI_BILESENI_YOK));
        } else {
            URI readedGuncellemeBilgisiUri = _package.getRelationshipsByType(Araclar.RELATION_TYPE_GUNCELLEME_BILGISI).getRelationship(0).getTargetURI();
            boolean bilesenVarMi = false;
            for (int i = 0; i <= ozet.getReference().size() - 1; i++) {
                if (PackagingURIHelper.decodeURI(URI.create(ozet.getReference().get(i).getURI())).compareTo(PackagingURIHelper.decodeURI(readedGuncellemeBilgisiUri)) == 0) {
                    bilesenVarMi = true;
                    break;
                }
                if (ozet.getReference().get(i).getURI().compareTo(readedGuncellemeBilgisiUri.toString()) == 0) {
                    bilesenVarMi = true;
                    break;
                }
            }
            if (!bilesenVarMi) {
                sonuclar.add(new OzetDogrulamaHatasi("NihaiOzet'te güncelleme bilgisi ozet değeri yok.", OzetDogrulamaHataKodu.NIHAIOZET_TE_GUNCELLEME_BILGISI_OZET_DEGERI_YOK));
                return false;
            }
        }


        if (_package.getRelationshipsByType(Araclar.RELATION_TYPE_ORIJINAL_PAKET) == null
                || _package.getRelationshipsByType(Araclar.RELATION_TYPE_ORIJINAL_PAKET).size() == 0) {
            sonuclar.add(new OzetDogrulamaHatasi("Orijinal paket bileşeni yok.", OzetDogrulamaHataKodu.ORIJINAL_PANEL_BILESENI_YOK));
        } else {
            URI readedOrijinalPaketUri = _package.getRelationshipsByType(Araclar.RELATION_TYPE_ORIJINAL_PAKET).getRelationship(0).getTargetURI();
            boolean bilesenVarMi = false;
            for (int i = 0; i <= ozet.getReference().size() - 1; i++) {
                if (PackagingURIHelper.decodeURI(URI.create(ozet.getReference().get(i).getURI())).compareTo(PackagingURIHelper.decodeURI(readedOrijinalPaketUri)) == 0) {
                    bilesenVarMi = true;
                    break;
                }
                if (ozet.getReference().get(i).getURI().compareTo(readedOrijinalPaketUri.toString()) == 0) {
                    bilesenVarMi = true;
                    break;
                }
            }
            if (!bilesenVarMi) {
                sonuclar.add(new OzetDogrulamaHatasi("NihaiOzet'te Orijinal paket değeri yok.", OzetDogrulamaHataKodu.NIHAIOZET_TE_ORIJINAL_PAKET_OZET_DEGERI_YOK));
                return false;
            }
        }

        if (_package.getRelationshipsByType(Araclar.RELATION_TYPE_CORE) == null || _package.getRelationshipsByType(Araclar.RELATION_TYPE_CORE).size() == 0) {
            sonuclar.add(new OzetDogrulamaHatasi("Core bileşeni yok.", OzetDogrulamaHataKodu.CORE_BILESENI_YOK));
        } else {
            URI readedCoreUri = _package.getRelationshipsByType(Araclar.RELATION_TYPE_CORE).getRelationship(0).getTargetURI();
            boolean bilesenVarMi = false;
            for (int i = 0; i <= ozet.getReference().size() - 1; i++) {
                if (PackagingURIHelper.decodeURI(URI.create(ozet.getReference().get(i).getURI())).compareTo(PackagingURIHelper.decodeURI(readedCoreUri)) == 0) {
                    bilesenVarMi = true;
                    break;
                }
                if (ozet.getReference().get(i).getURI().compareTo(readedCoreUri.toString()) == 0) {
                    bilesenVarMi = true;
                    break;
                }
            }
            if (!bilesenVarMi) {
                sonuclar.add(new OzetDogrulamaHatasi("NihaiOzet'te Core ozet değeri yok.", OzetDogrulamaHataKodu.NIHAIOZET_TE_CORE_OZET_DEGERI_YOK));
                return false;
            }
        }

        for (CTReference ctReference : ozet.getReference()) {
            referansDogrula(ctReference, sonuclar);
        }
        return sonuclar.size() == 0;
    }

    public boolean referansDogrula(CTReference item, List<OzetDogrulamaHatasi> sonuclar) {

        if (sonuclar == null)
            return false;

        if (item == null) {
            sonuclar.add(new OzetDogrulamaHatasi("Reference değeri verilmemiş.", OzetDogrulamaHataKodu.REFERENCE_DEGERI_VERILMEMIS));
            return false;
        }

        if (item.getURI() == null) {
            sonuclar.add(new OzetDogrulamaHatasi("\"URI\" değeri boş.", OzetDogrulamaHataKodu.URI_DEGERI_BOS));
            return false;
        }

        if (item.getContent() == null || item.getContent().size() == 0) {
            sonuclar.add(new OzetDogrulamaHatasi(String.format("\"DigestItem\" değeri boş. URI:%s.", item.getURI()), OzetDogrulamaHataKodu.DIGESTITEM_DEGERI_BOS));
            return false;
        }

        if (item.getContent() == null || item.getContent().size() == 1) {
            sonuclar.add(new OzetDogrulamaHatasi(String.format("\"DigestItem1\" değeri boş. URI:%s", item.getURI()), OzetDogrulamaHataKodu.DIGESTITEM1_DEGERI_BOS));
            return false;
        }

        CTDigestItem digestItem = item.getContent().get(0).getValue();
        CTDigestItem digestItem1 = item.getContent().get(1).getValue();

        if (digestItem.getDigestMethod() == null) {
            sonuclar.add(new OzetDogrulamaHatasi(String.format("\"DigestMethod\" değeri boş. URI:%s.", item.getURI()), OzetDogrulamaHataKodu.DIGESTMETHOD_DEGERI_BOS));
            return false;
        }
        if (digestItem1.getDigestMethod() == null) {
            sonuclar.add(new OzetDogrulamaHatasi(String.format("\"DigestMethod\" değeri boş. URI:%s.", item.getURI()), OzetDogrulamaHataKodu.DIGESTMETHOD_DEGERI_BOS));
            return false;
        }
        if (digestItem.getDigestMethod().getAlgorithm() == null || digestItem.getDigestMethod().getAlgorithm().isEmpty()) {
            sonuclar.add(new OzetDogrulamaHatasi(String.format("\"Algorithm\" değeri boş. URI:%s.", item.getURI()), OzetDogrulamaHataKodu.ALGORITHM_DEGERI_BOS));
            return false;
        }
        if (digestItem1.getDigestMethod().getAlgorithm() == null || digestItem1.getDigestMethod().getAlgorithm().isEmpty()) {
            sonuclar.add(new OzetDogrulamaHatasi(String.format("\"Algorithm\" değeri boş. URI:%s.", item.getURI()), OzetDogrulamaHataKodu.ALGORITHM_DEGERI_BOS));
            return false;
        }
        if (digestItem.getDigestMethod().getAlgorithm().equals(digestItem1.getDigestMethod().getAlgorithm())) {
            sonuclar.add(new OzetDogrulamaHatasi(String.format("İki DigestItem için \"Algorithm\" değerleri aynı. URI:%s.\"", item.getURI()), OzetDogrulamaHataKodu.ALGORITHM_DEGERLERI_AYNI));
            return false;
        }
        if (!digestItem.getDigestMethod().getAlgorithm().equals(Araclar.ozetModuToString(OzetModu.SHA512))
                && !digestItem1.getDigestMethod().getAlgorithm().equals(Araclar.ozetModuToString(OzetModu.SHA512))) {
            sonuclar.add(new OzetDogrulamaHatasi(String.format("En az bir DigestItem için \"Algorithm\" değeri SHA512 olmalıdır. URI:%s.\"", item.getURI()), OzetDogrulamaHataKodu.ALGORITHM_SHA512_KULLANILMAMIS));
            return false;
        }

        OzetModu mod;
        if (digestItem.getDigestMethod().getAlgorithm().equals(Araclar.ozetModuToString(OzetModu.SHA512))) {
            mod = OzetModu.SHA512;
        } else if (digestItem.getDigestMethod().getAlgorithm().equals(Araclar.ozetModuToString(OzetModu.SHA256))) {
            mod = OzetModu.SHA256;
        } else if (digestItem.getDigestMethod().getAlgorithm().equals(Araclar.ozetModuToString(OzetModu.SHA384))) {
            mod = OzetModu.SHA384;
        } else {
            sonuclar.add(new OzetDogrulamaHatasi(String.format("Desteklenmeyen OzetModu. URI:%s.", item.getURI()), OzetDogrulamaHataKodu.DESTEKLENMEYEN_OZETMODU));
            return false;
        }


        OzetModu mod1;
        if (digestItem1.getDigestMethod().getAlgorithm().equals(Araclar.ozetModuToString(OzetModu.SHA512))) {
            mod1 = OzetModu.SHA512;
        } else if (digestItem1.getDigestMethod().getAlgorithm().equals(Araclar.ozetModuToString(OzetModu.SHA256))) {
            mod1 = OzetModu.SHA256;
        } else if (digestItem1.getDigestMethod().getAlgorithm().equals(Araclar.ozetModuToString(OzetModu.SHA384))) {
            mod1 = OzetModu.SHA384;
        } else {
            sonuclar.add(new OzetDogrulamaHatasi(String.format("Desteklenmeyen OzetModu. URI:%s.", item.getURI()), OzetDogrulamaHataKodu.DESTEKLENMEYEN_OZETMODU));
            return false;
        }

        if (item.getType() != null && item.getType().equals(Araclar.HARICI_PAKET_BILESENI_REFERANS_TIPI)) {
            return true;
        }

        InputStream stream;
        InputStream stream2;

        try {
            if (getCorePartUri().equalsIgnoreCase(item.getURI()))
                return true;

            PackagePart part = _package.getPart(PackagingURIHelper.createPartName(item.getURI()));
            stream = new ByteArrayInputStream(IOUtils.toByteArray(part.getInputStream()));
            stream2 = new ByteArrayInputStream(IOUtils.toByteArray(part.getInputStream()));

        } catch (Exception ex) {
            if (getCorePartUri().equals(item.getURI())) {
                return true;
            }
            sonuclar.add(new OzetDogrulamaHatasi(String.format("Paket bileşeni alınamadı. URI:%s.", item.getURI()),
                    OzetDogrulamaHataKodu.PAKET_BILESENI_ALINAMADI, item.getURI(), ex));
            return false;
        }

        byte[] computedDigestValue;
        byte[] computedDigestValue1;

        try {
            computedDigestValue = Araclar.ozetHesapla(stream, mod);
            computedDigestValue1 = Araclar.ozetHesapla(stream2, mod1);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            sonuclar.add(new OzetDogrulamaHatasi(String.format("Paket bileşenine ait hash hesaplanamadı. URI:%s.", item.getURI()),
                    OzetDogrulamaHataKodu.PAKET_BILESENINE_AIT_HASH_HESAPLANAMADI));
            return false;
        }

        if (!Arrays.equals(computedDigestValue, digestItem.getDigestValue())) {
            sonuclar.add(new OzetDogrulamaHatasi(String.format("Hashler eşit değil. URI:%s.", item.getURI()), OzetDogrulamaHataKodu.HASHLER_ESIT_DEGIL));
            return false;
        }
        if (!Arrays.equals(computedDigestValue1, digestItem1.getDigestValue())) {
            sonuclar.add(new OzetDogrulamaHatasi(String.format("Hashler eşit değil. URI:%s.", item.getURI()), OzetDogrulamaHataKodu.HASHLER_ESIT_DEGIL));
            return false;
        }

        return sonuclar.size() == 0;
    }

    public String getCorePartUri() {
        PackageRelationshipCollection cols = _package.getRelationshipsByType(Araclar.RELATION_TYPE_CORE);
        if (cols != null && cols.size() > 0) {
            return cols.getRelationship(0).getTargetURI().toString();
        }
        return null;
    }

    public void nihaiOzetOlustur() throws Exception {

        if (_paketModu == PaketModu.Ac)
            throw new Exception("PaketModu 'Ac' olarak işlem yapılan paketler için NihaiOzetOlustur fonksiyonu kullanılamaz.");

        if (!orijinalPaketVarMi()) {
            throw new RuntimeException("Orijinal paket bulunmadığı durumlarda NihaiOzetOlustur fonksiyonu kullanılamaz.");
        }
        if (!guncellemeBilgisiVarMi()) {
            throw new RuntimeException("Güncelleme bilgisi bulunmadığı durumlarda NihaiOzetOlustur fonksiyonu kullanılamaz.");
        }

        byte[] ozetUstVeri = Araclar.ozetHesapla(guncellemeBilgisiAl(), _varsayilanOzetModu);
        byte[] ozetUstVeriSha512 = Araclar.ozetHesapla(guncellemeBilgisiAl(), OzetModu.SHA512);
        _nihaiOzet.ekle(_varsayilanOzetModu, ozetUstVeri, ozetUstVeriSha512, PackagingURIHelper
                .createPartName(Araclar.URI_GUNCELLEME_BILGISI).getURI());


        PackageRelationshipCollection orijinalPaketRelationType = _package.getRelationshipsByType(Araclar.RELATION_TYPE_ORIJINAL_PAKET);
        PackagePartName ustYaziPart = PackagingURIHelper.createPartName(orijinalPaketRelationType.getRelationship(0).getTargetURI());
        byte[] ozetUstYazi = Araclar.ozetHesapla(orijinalPaketAl(), _varsayilanOzetModu);
        byte[] ozetUstYaziSha512 = Araclar.ozetHesapla(orijinalPaketAl(), OzetModu.SHA512);
        _nihaiOzet.ekle(_varsayilanOzetModu, ozetUstYazi, ozetUstYaziSha512, ustYaziPart.getURI());

        //Core NİHAİ ÖZETE Daha sonra NihaiOzeteCore ekle metodu ile eklenmeli.
        _nihaiOzet.getNihaiOzet().setId(_package.getPackageProperties().getIdentifierProperty().get().toUpperCase());
        _nihaiOzet.kontrolEt();
        PackagePartName partNameNihaiOzet = PackagingURIHelper.createPartName(Araclar.URI_NIHAIOZET);
        if (_paketModu == PaketModu.Guncelle) {
            if (_package.containPart(partNameNihaiOzet)) {
                _package.deletePartRecursive(partNameNihaiOzet);
            }
        }
        _package.addRelationship(partNameNihaiOzet, TargetMode.INTERNAL, Araclar.RELATION_TYPE_NIHAIOZET, Araclar.ID_NIHAIOZET);
        PackagePart partNihaiOzet = _package.createPart(partNameNihaiOzet, Araclar.MIME_XML);
        OutputStream streamNihaiOzet = partNihaiOzet.getOutputStream();
        JAXBContext contextNihaiOzet = JAXBContext.newInstance(ObjectFactory.class);
        Marshaller marshallerNihaiOzet = contextNihaiOzet.createMarshaller();
        marshallerNihaiOzet.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshallerNihaiOzet.marshal(_nihaiOzet.getNihaiOzet(), streamNihaiOzet);
        streamNihaiOzet.close();
    }

    public void muhurEkle(byte[] muhur) throws Exception {
        if (muhur == null) {
            throw new IllegalArgumentException("muhur");
        }
        if (_paketModu == PaketModu.Ac) {
            throw new Exception("PaketModu 'Ac' olarak işlem yapılan paketler için muhurEkle fonksiyonu kullanılamaz.");
        }

        PackagePartName partNameMuhur = PackagingURIHelper.createPartName(Araclar.URI_MUHUR);
        if (_package.containPart(partNameMuhur)) {
            _package.deletePartRecursive(partNameMuhur);
        }
        PackagePart partMuhur = _package.createPart(partNameMuhur, Araclar.MIME_OCTETSTREAM);
        ByteArrayInputStream fileInputStreamMuhur = new ByteArrayInputStream(muhur);
        OutputStream streamMuhur = partMuhur.getOutputStream();
        Araclar.copyStream(fileInputStreamMuhur, streamMuhur);
        _package.getPart(_package.getRelationshipsByType(Araclar.RELATION_TYPE_NIHAIOZET)
                        .getRelationship(0))
                .addRelationship(partNameMuhur, TargetMode.INTERNAL, Araclar.RELATION_TYPE_MUHUR, Araclar.ID_MUHUR);
        streamMuhur.close();
    }

    public InputStream muhurAl() throws Exception {
        PackageRelationshipCollection colNihaiOzet = _package.getRelationshipsByType(Araclar.RELATION_TYPE_NIHAIOZET);
        if (colNihaiOzet == null || colNihaiOzet.size() == 0) {
            return null;
        }
        PackagePart ozet = _package.getPart(PackagingURIHelper.createPartName(colNihaiOzet.getRelationship(0).getTargetURI()));
        PackageRelationshipCollection colMuhur = ozet.getRelationshipsByType(Araclar.RELATION_TYPE_MUHUR);
        if (colMuhur == null || colMuhur.size() == 0) {
            return null;
        }
        PackagePart muhur = _package.getPart(PackagingURIHelper.createPartName(colMuhur.getRelationship(0).getTargetURI()));
        return muhur.getInputStream();
    }

    public boolean muhurVarMi() throws InvalidFormatException {
        return _package.containPart(PackagingURIHelper.createPartName(Araclar.URI_MUHUR));
    }

}
