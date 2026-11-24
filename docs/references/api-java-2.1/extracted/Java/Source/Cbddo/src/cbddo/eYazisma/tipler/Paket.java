package cbddo.eYazisma.tipler;


import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.*;
import org.apache.poi.util.IOUtils;
import urn.cbddo.eyazisma.schema.xsd.ObjectFactory;
import urn.cbddo.eyazisma.schema.xsd.codelist.IdentifierType;
import urn.cbddo.eyazisma.schema.xsd.kodekturu.STKodEkTuru;
import urn.cbddo.eyazisma.schema.xsd.nihaiOzet.CTNihaiOzet;
import urn.cbddo.eyazisma.schema.xsd.nihaiUstveri.CTNihaiUstveri;
import urn.cbddo.eyazisma.schema.xsd.paketOzeti.CTDigestItem;
import urn.cbddo.eyazisma.schema.xsd.paketOzeti.CTPaketOzeti;
import urn.cbddo.eyazisma.schema.xsd.paketOzeti.CTReference;
import urn.cbddo.eyazisma.schema.xsd.parafOzeti.CTParafOzeti;
import urn.cbddo.eyazisma.schema.xsd.tipler.CTDagitim;
import urn.cbddo.eyazisma.schema.xsd.tipler.CTEk;
import urn.cbddo.eyazisma.schema.xsd.tipler.CTIlgi;
import urn.cbddo.eyazisma.schema.xsd.tipler.CTKonulmamisEk;
import urn.cbddo.eyazisma.schema.xsd.ustveri.CTUstveri;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.URI;
import java.util.*;


public class Paket {
    private OPCPackage _package;
    private PaketModu _paketModu;
    private Ustveri _ustveri;
    private NihaiUstveri _nihaiUstveri;
    private PaketOzeti _paketOzeti;
    private ParafOzeti _parafOzeti;
    private NihaiOzet _nihaiOzet;

    private OzetModu _varsayilanOzetModu;
    private OutputStream internalOutputStream;
    private InputStream internalInputStream;

    public NihaiUstveri getNihaiUstveri() {
        return _nihaiUstveri;
    }

    public ParafOzeti getParafOzeti() {
        return _parafOzeti;
    }

    public Ustveri getUstveri() {
        return _ustveri;
    }

    public PaketOzeti getPaketOzeti() {
        return _paketOzeti;
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

    protected Paket() {
        _varsayilanOzetModu = OzetModu.SHA384;
    }


    private static void versiyonNumarasiKontrolEt(OPCPackage opcPackage) throws Exception {
        if (!opcPackage.getPackageProperties().getVersionProperty().isPresent()) {
            throw new Exception("e-Yazışma paketi versiyon numarası alınamadı.");
        }

        String[] versiyonNumarasiParcalari = opcPackage.getPackageProperties().getVersionProperty().get().split("\\.");
        if (versiyonNumarasiParcalari.length != 2) {
            throw new Exception("e-Yazışma paketi versiyon numarası alınamadı.");
        }
        int ilkBolum = Integer.parseInt(versiyonNumarasiParcalari[0]);

        if (ilkBolum < 2) {
            throw new Exception(String.format("e-Yazışma paketi version numarası %s. Versiyon 2 API önceki versiyon API'ler ile oluşturulmuş e-Yazışma paketlerini desteklememektedir. Lütfen önceki sürümleri açabilmek için version 1 API'leri kullanınız.",
                    opcPackage.getPackageProperties().getVersionProperty().get()));
        }

    }

    // =======================================Ac Kapat START ===========================
    public static Paket ac(InputStream stream) throws Exception {
        Paket paket = new Paket();
        paket._package = OPCPackage.open(stream);
        paket.internalInputStream = stream;

        if (paket._package.getRelationships().size() == 0) {
            throw new Exception("Geçersiz e-Yazışma paketi. \"İlişki\" bileşeni bulunamadı");
        }
        if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_CORE).size() == 0) {
            throw new Exception("Geçersiz e-Yazışma paketi. \"Core\" bileşeni yok.");
        }
        if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_CORE).size() > 1) {
            throw new Exception("Geçersiz e-Yazışma paketi. \"Core\" bileşeni birden fazla olamaz.");
        }
        versiyonNumarasiKontrolEt(paket._package);

        if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_USTVERI).size() == 0) {
            throw new Exception("Geçersiz e-Yazışma paketi. \"Üstveri\" bileşeni yok.");
        }
        if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_USTVERI).size() > 1) {
            throw new Exception("Geçersiz e-Yazışma paketi. \"Üstveri\" bileşeni birden fazla olamaz");
        }
        if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_USTYAZI).size() == 0) {
            throw new Exception("Geçersiz e-Yazışma paketi. \"ÜstYazı\" bileşeni yok.");
        }
        if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_USTYAZI).size() > 1) {
            throw new Exception("Geçersiz e-Yazışma paketi. \"ÜstYazı\" bileşeni birden fazla olamaz.");
        }
        if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_NIHAIUSTVERI).size() == 0) {
            throw new Exception("Geçersiz e-Yazışma paketi. \"NihaiÜstveri\" bileşeni yok.");
        }
        if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_NIHAIUSTVERI).size() > 1) {
            throw new Exception("Geçersiz e-Yazışma paketi. \"NihaiÜstveri\" bileşeni birden fazla olamaz.");
        }
        if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_PAKETOZETI).size() == 0) {
            throw new Exception("Geçersiz e-Yazışma paketi. \"PaketÖzeti\" bileşeni yok.");
        }
        if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_PAKETOZETI).size() > 1) {
            throw new Exception("Geçersiz e-Yazışma paketi. \"PaketÖzeti\" bileşeni birden fazla olamaz.");
        }
        if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_NIHAIOZET).size() == 0) {
            throw new Exception("Geçersiz e-Yazışma paketi. \"NihaiOzet\" bileşeni yok.");
        }
        if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_NIHAIOZET).size() > 1) {
            throw new Exception("Geçersiz e-Yazışma paketi. \"NihaiOzet\" bileşeni birden fazla olamaz.");
        }
        if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_PARAFOZETI).size() > 1) {
            throw new Exception("Geçersiz e-Yazışma paketi. \"ParafÖzeti\" bileşeni birden fazla olamaz.");
        }

        try {
            ArrayList<PackagePart> packagePartsUstveri = paket._package.getPartsByRelationshipType(Araclar.RELATION_TYPE_USTVERI);
            InputStream inputStreamUstveri = packagePartsUstveri.get(0).getInputStream();
            JAXBContext contextUstveri = JAXBContext.newInstance(ObjectFactory.class);
            Unmarshaller unmarshallerUstveri = contextUstveri.createUnmarshaller();
            CTUstveri okunanUstveri = (CTUstveri) JAXBIntrospector.getValue(unmarshallerUstveri.unmarshal(inputStreamUstveri));
            paket._ustveri = new UstveriInternal(okunanUstveri);
        } catch (Exception ex) {
            throw new Exception("Geçersiz e-Yazışma paketi. \"Üstveri\" bileşeni hatalı.", ex);
        }

        try {
            ArrayList<PackagePart> packagePartNihaiUstveri = paket._package.getPartsByRelationshipType(Araclar.RELATION_TYPE_NIHAIUSTVERI);
            InputStream inputStreamNihaiUstveri = packagePartNihaiUstveri.get(0).getInputStream();
            JAXBContext contextNihaiUstveri = JAXBContext.newInstance(ObjectFactory.class);
            Unmarshaller unmarshallerUstveri = contextNihaiUstveri.createUnmarshaller();
            CTNihaiUstveri okunanUstveri = (CTNihaiUstveri) JAXBIntrospector.getValue(unmarshallerUstveri.unmarshal(inputStreamNihaiUstveri));
            paket._nihaiUstveri = new NihaiUstveriInternal(okunanUstveri);
        } catch (Exception ex) {
            throw new Exception("Geçersiz e-Yazışma paketi. \"NihaiUstveri\" bileşeni hatalı.", ex);
        }

        try {
            ArrayList<PackagePart> packagePartsPaketOzeti = paket._package.getPartsByRelationshipType(Araclar.RELATION_TYPE_PAKETOZETI);
            InputStream inputStreamPaketOzeti = packagePartsPaketOzeti.get(0).getInputStream();
            JAXBContext contextPaketOzeti = JAXBContext.newInstance(ObjectFactory.class);
            Unmarshaller unmarshallerPaketOzeti = contextPaketOzeti.createUnmarshaller();
            CTPaketOzeti okunanPaketOzeti = (CTPaketOzeti) JAXBIntrospector.getValue(unmarshallerPaketOzeti.unmarshal(inputStreamPaketOzeti));
            paket._paketOzeti = new PaketOzetiInternal(okunanPaketOzeti);
        } catch (Exception ex) {
            throw new Exception("Geçersiz e-Yazışma paketi. \"PaketOzeti\" bileşeni hatalı.", ex);
        }


        if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_PARAFOZETI).size() == 1) {
            try {
                ArrayList<PackagePart> packagePartsParafOzeti = paket._package.getPartsByRelationshipType(Araclar.RELATION_TYPE_PARAFOZETI);
                InputStream inputStreamParafOzeti = packagePartsParafOzeti.get(0).getInputStream();
                JAXBContext contextParafOzeti = JAXBContext.newInstance(ObjectFactory.class);
                Unmarshaller unmarshallerParafOzeti = contextParafOzeti.createUnmarshaller();
                CTParafOzeti okunanParafOzeti = (CTParafOzeti) JAXBIntrospector.getValue(unmarshallerParafOzeti.unmarshal(inputStreamParafOzeti));
                paket._parafOzeti = new ParafOzetiInternal(okunanParafOzeti);
            } catch (Exception ex) {
                throw new Exception("Geçersiz e-Yazışma paketi. \"ParafOzeti\" bileşeni hatalı.", ex);
            }
        }

        try {
            ArrayList<PackagePart> packagePartsNihaiOzet = paket._package.getPartsByRelationshipType(Araclar.RELATION_TYPE_NIHAIOZET);
            InputStream inputStreamNihaiOzet = packagePartsNihaiOzet.get(0).getInputStream();
            JAXBContext contextNihaiOzet = JAXBContext.newInstance(ObjectFactory.class);
            Unmarshaller unmarshallerNihaiOzet = contextNihaiOzet.createUnmarshaller();
            CTNihaiOzet okunanNihaiOzet = (CTNihaiOzet) JAXBIntrospector.getValue(unmarshallerNihaiOzet.unmarshal(inputStreamNihaiOzet));
            paket._nihaiOzet = new NihaiOzetInternal(okunanNihaiOzet);
        } catch (Exception ex) {
            throw new Exception("Geçersiz e-Yazışma paketi. \"ParafOzeti\" bileşeni hatalı.", ex);
        }

        if (!paket.imzaVarMi()) {
            throw new Exception("Geçersiz e-Yazışma paketi. \"Imza\" bileşeni yok.");
        }


        paket._paketModu = PaketModu.Ac;
        return paket;
    }

    public static Paket ac(OutputStream stream) {
        Paket paket = new Paket();
        paket._package = OPCPackage.create(stream);
        paket._ustveri = new UstveriInternal();
        paket._paketOzeti = new PaketOzetiInternal();
        paket._nihaiOzet = new NihaiOzetInternal();
        paket._nihaiUstveri = new NihaiUstveriInternal();
        paket._parafOzeti = new ParafOzetiInternal();
        paket._paketModu = PaketModu.Olustur;
        paket.internalOutputStream = stream;
        return paket;
    }

    public static Paket ac(String dosyaYolu, PaketModu paketModu) throws Exception {
        switch (paketModu) {
            case Guncelle:
                Paket paket = new Paket();
                paket.internalInputStream = new FileInputStream(dosyaYolu);
                paket._package = OPCPackage.open(dosyaYolu, PackageAccess.READ_WRITE);

                if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_USTYAZI).size() > 1) {
                    throw new Exception("Geçersiz e-Yazışma paketi. \"ÜstYazı\" bileşeni birden fazla olamaz.");
                }
                if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_USTVERI).size() > 1) {
                    throw new Exception("Geçersiz e-Yazışma paketi. \"Üstveri\" bileşeni birden fazla olamaz");
                }
                if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_NIHAIUSTVERI).size() > 1) {
                    throw new Exception("Geçersiz e-Yazışma paketi. \"NihaiÜstveri\" bileşeni birden fazla olamaz.");
                }
                if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_PAKETOZETI).size() > 1) {
                    throw new Exception("Geçersiz e-Yazışma paketi. \"PaketÖzeti\" bileşeni birden fazla olamaz.");
                }
                if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_PARAFOZETI).size() > 1) {
                    throw new Exception("Geçersiz e-Yazışma paketi. \"ParafÖzeti\" bileşeni birden fazla olamaz.");
                }
                if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_NIHAIOZET).size() > 1) {
                    throw new Exception("Geçersiz e-Yazışma paketi. \"NihaiOzet\" bileşeni birden fazla olamaz.");
                }

                if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_USTVERI).size() == 1) {
                    try {
                        ArrayList<PackagePart> packagePartsUstveri = paket._package.getPartsByRelationshipType(Araclar.RELATION_TYPE_USTVERI);
                        InputStream inputStreamUstveri = packagePartsUstveri.get(0).getInputStream();
                        JAXBContext contextUstveri = JAXBContext.newInstance(ObjectFactory.class);
                        Unmarshaller unmarshallerUstveri = contextUstveri.createUnmarshaller();
                        CTUstveri okunanUstveri = (CTUstveri) JAXBIntrospector.getValue(unmarshallerUstveri.unmarshal(inputStreamUstveri));
                        paket._ustveri = new UstveriInternal(okunanUstveri);
                    } catch (Exception ex) {
                        throw new Exception("Geçersiz e-Yazışma paketi. \"Üstveri\" bileşeni hatalı.", ex);
                    }
                }

                if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_NIHAIUSTVERI).size() == 1) {
                    try {
                        ArrayList<PackagePart> packagePartNihaiUstveri = paket._package.getPartsByRelationshipType(Araclar.RELATION_TYPE_NIHAIUSTVERI);
                        InputStream inputStreamNihaiUstveri = packagePartNihaiUstveri.get(0).getInputStream();
                        JAXBContext contextNihaiUstveri = JAXBContext.newInstance(ObjectFactory.class);
                        Unmarshaller unmarshallerUstveri = contextNihaiUstveri.createUnmarshaller();
                        CTNihaiUstveri okunanUstveri = (CTNihaiUstveri) JAXBIntrospector.getValue(unmarshallerUstveri.unmarshal(inputStreamNihaiUstveri));
                        paket._nihaiUstveri = new NihaiUstveriInternal(okunanUstveri);
                    } catch (Exception ex) {
                        throw new Exception("Geçersiz e-Yazışma paketi. \"NihaiUstveri\" bileşeni hatalı.", ex);
                    }
                } else {
                    paket._nihaiUstveri = new NihaiUstveriInternal();
                }

                if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_PAKETOZETI).size() == 1) {
                    try {
                        ArrayList<PackagePart> packagePartsPaketOzeti = paket._package.getPartsByRelationshipType(Araclar.RELATION_TYPE_PAKETOZETI);
                        InputStream inputStreamPaketOzeti = packagePartsPaketOzeti.get(0).getInputStream();
                        JAXBContext contextPaketOzeti = JAXBContext.newInstance(ObjectFactory.class);
                        Unmarshaller unmarshallerPaketOzeti = contextPaketOzeti.createUnmarshaller();
                        CTPaketOzeti okunanPaketOzeti = (CTPaketOzeti) JAXBIntrospector.getValue(JAXBIntrospector.getValue(unmarshallerPaketOzeti.unmarshal(inputStreamPaketOzeti)));
                        paket._paketOzeti = new PaketOzetiInternal(okunanPaketOzeti);
                    } catch (Exception ex) {
                        throw new Exception("Geçersiz e-Yazışma paketi. \"PaketOzeti\" bileşeni hatalı.", ex);
                    }
                } else {
                    paket._paketOzeti = new PaketOzetiInternal();
                }

                if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_PARAFOZETI).size() == 1) {
                    try {
                        ArrayList<PackagePart> packagePartsParafOzeti = paket._package.getPartsByRelationshipType(Araclar.RELATION_TYPE_PARAFOZETI);
                        InputStream inputStreamParafOzeti = packagePartsParafOzeti.get(0).getInputStream();
                        JAXBContext contextParafOzeti = JAXBContext.newInstance(ObjectFactory.class);
                        Unmarshaller unmarshallerParafOzeti = contextParafOzeti.createUnmarshaller();
                        CTParafOzeti okunanParafOzeti = (CTParafOzeti) JAXBIntrospector.getValue(unmarshallerParafOzeti.unmarshal(inputStreamParafOzeti));
                        paket._parafOzeti = new ParafOzetiInternal(okunanParafOzeti);
                    } catch (Exception ex) {
                        throw new Exception("Geçersiz e-Yazışma paketi. \"ParafOzeti\" bileşeni hatalı.", ex);
                    }
                } else {
                    paket._parafOzeti = new ParafOzetiInternal();
                }

                if (paket._package.getRelationshipsByType(Araclar.RELATION_TYPE_NIHAIOZET).size() == 1) {
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
                return Paket.ac(new FileInputStream(dosyaYolu));
            case Olustur:
                return Paket.ac(new FileOutputStream(dosyaYolu));
        }
        return null;
    }

    public void ilgileriKontrolEt() throws Exception {
        if (_ustveri.getCTUstveri().getIlgiler() == null || _ustveri.getCTUstveri().getIlgiler().getIlgi() == null) {
            return;
        }

        for (CTIlgi ilgi : _ustveri.getCTUstveri().getIlgiler().getIlgi()) {
            if (ilgi.getEkId() != null && !(ilgi.getEkId().isEmpty())) {
                if (_ustveri.getCTUstveri().getEkler() == null || _ustveri.getCTUstveri().getEkler().getEk() == null || _ustveri.getCTUstveri().getEkler().getEk().size() == 0) {
                    throw new Exception("Üstveri bileşeninde verilen ilgilerden, ek olarak belirtilmiş ilgi için, paket içerisine eklenmiş Ek bileşeni bulunamadı.");
                }

                boolean kontrol = false;
                for (CTEk ek : _ustveri.getCTUstveri().getEkler().getEk()) {
                    if (ilgi.getEkId().compareToIgnoreCase(ekIdAl(ek)) == 0) {
                        kontrol = true;
                        break;
                    }
                }
                if (!kontrol) {
                    throw new Exception("Üstveri bileşeninde verilen ilgilerden, ek olarak belirtilmiş ilgi için, paket içerisine eklenmiş Ek bileşeni bulunamadı.");
                }
            }
        }
        for (CTIlgi ustveriIlgi1 : _ustveri.getCTUstveri().getIlgiler().getIlgi()) {
            for (CTIlgi ustveriIlgi2 : _ustveri.getCTUstveri().getIlgiler().getIlgi()) {
                if (ustveriIlgi1.getEtiket().equals(ustveriIlgi2.getEtiket()) && ustveriIlgi1.getId().getValue().compareToIgnoreCase(ustveriIlgi2.getId().getValue()) != 0) {
                    throw new Exception("Paket içerisine aynı Etiket değerine sahip birden fazla ilgi eklenemez.");
                }
            }
        }
    }

    public void ekleriKontrolEt() throws Exception {
        ekleriKontrolEt(null);
    }

    public void ekleriKontrolEt(IdentifierType dagitimIdentifier) throws Exception {
        if (_ustveri.getCTUstveri().getEkler() != null && _ustveri.getCTUstveri().getEkler().getEk() != null) {
            for (CTEk ustveriEki : _ustveri.getCTUstveri().getEkler().getEk()) {
                if (ustveriEki.getTur() == STKodEkTuru.DED) {
                    PackageRelationshipCollection rels = _package.getRelationshipsByType(Araclar.RELATION_TYPE_EK);
                    boolean kontrol = false;
                    for (PackageRelationship rel : rels) {
                        if (rel.getId().compareToIgnoreCase(Araclar.ID_ROOT_EK + ekIdAl(ustveriEki)) == 0) {
                            kontrol = true;
                            break;
                        }
                    }
                    if (!kontrol) {
                        PackageRelationshipCollection relsImzasiz = _package.getRelationshipsByType(Araclar.RELATION_TYPE_IMZASIZEK);
                        for (PackageRelationship rel : relsImzasiz) {
                            if (rel.getId().compareToIgnoreCase(Araclar.ID_ROOT_IMZASIZEK + ekIdAl(ustveriEki)) == 0) {
                                kontrol = true;
                                break;
                            }
                        }
                    }
                    if (!kontrol) {
                        // bu ek icin pakette relation yok
                        if (dagitimIdentifier == null) {
                            throw new Exception(String.format("Üstveri bileşeni için eklenen ek, paket içerisine eklenmemiş. EkId:%s", ustveriEki.getId().getValue()));
                        }
                        List<CTDagitim> dagitimlar = _ustveri.dagitimlariAl();
                        if (dagitimlar == null || dagitimlar.size() == 0) {
                            throw new Exception("Paket içerisine eklenmiş dağıtımlar bulunamadı.");
                        }

                        CTDagitim ctDagitim = null;
                        for (CTDagitim dagitim : dagitimlar) {
                            if (dagitim.getTuzelSahis() != null) {
                                if (dagitim.getTuzelSahis().getId() != null && !Araclar.isNullOrWhitespace(dagitim.getTuzelSahis().getId().getValue())
                                        && dagitim.getTuzelSahis().getId().getValue().equals(dagitimIdentifier.getValue())) {
                                    ctDagitim = dagitim;
                                    break;
                                }

                            } else if (dagitim.getGercekSahis() != null) {
                                if (!Araclar.isNullOrWhitespace(dagitim.getGercekSahis().getTCKN()) && dagitim.getGercekSahis().getTCKN().equals(dagitimIdentifier.getValue())) {
                                    ctDagitim = dagitim;
                                    break;
                                }
                            } else if (dagitim.getKurumKurulus() != null) {
                                if (!Araclar.isNullOrWhitespace(dagitim.getKurumKurulus().getKKK())
                                        && dagitim.getKurumKurulus().getKKK().equals(dagitimIdentifier.getValue())) {
                                    ctDagitim = dagitim;
                                    break;
                                }
                            }
                        }

                        if (ctDagitim == null || ctDagitim.getKonulmamisEkListesi() == null || ctDagitim.getKonulmamisEkListesi().getKonulmamisEk() == null
                                || ctDagitim.getKonulmamisEkListesi().getKonulmamisEk().size() == 0) {
                            throw new Exception(String.format("Üstveri bileşeni için eklenen ek, paket içerisine eklenmemiş. EkId:%s", ustveriEki.getId().getValue()));
                        } else {
                            boolean ekVarmi = false;
                            for (CTKonulmamisEk ctKonulmamisEk : ctDagitim.getKonulmamisEkListesi().getKonulmamisEk()) {
                                if (ctKonulmamisEk.getEkId().equals(ustveriEki.getId().getValue())) {
                                    ekVarmi = true;
                                }
                            }
                            if (!ekVarmi) {
                                throw new Exception(String.format("Üstveri bileşeni için eklenen ek, paket içerisine eklenmemiş. EkId:%s", ustveriEki.getId().getValue()));
                            }
                        }
                    }
                }
            }
        }
        for (PackageRelationship relationship : _package.getRelationshipsByType(Araclar.RELATION_TYPE_EK)) {
            if (_ustveri.getCTUstveri().getEkler() == null || _ustveri.getCTUstveri().getEkler().getEk() == null) {
                throw new Exception("Paket içerisine eklenmiş eklerin hiç biri, üstveri bileşeninde belirtilmemiş.");
            }
            boolean kontrol = false;
            CTEk ustveriEki = null;
            for (CTEk ek : _ustveri.getCTUstveri().getEkler().getEk()) {
                if (relationship.getId().compareToIgnoreCase(Araclar.ID_ROOT_EK + ekIdAl(ek)) == 0) {
                    kontrol = true;
                    ustveriEki = ek;
                    break;
                }
            }
            if (!kontrol) {
                throw new Exception("Paket içerisine eklenmiş ek, üstveri bileşeninde belirtilmemiş.");
            } else {
                if (ustveriEki.getTur() != STKodEkTuru.DED) {
                    throw new Exception("Paket içerisine eklenmiş ek, üstveri bileşeninde DED (Dahili Elektronik Dosya) olarak belirtilmelidir.");
                }
            }
        }
        for (PackageRelationship relationship : _package.getRelationshipsByType(Araclar.RELATION_TYPE_IMZASIZEK)) {
            if (_ustveri.getCTUstveri().getEkler() == null || _ustveri.getCTUstveri().getEkler().getEk() == null) {
                throw new Exception("Paket içerisine eklenmiş eklerin hiç biri, üstveri bileşeninde belirtilmemiş.");
            }
            boolean kontrol = false;
            CTEk ustveriEki = null;
            for (CTEk ek : _ustveri.getCTUstveri().getEkler().getEk()) {
                if (relationship.getId().compareToIgnoreCase(Araclar.ID_ROOT_IMZASIZEK + ekIdAl(ek)) == 0) {
                    kontrol = true;
                    ustveriEki = ek;
                    break;
                }
            }
            if (!kontrol) {
                throw new Exception("Paket içerisine eklenmiş ek, üstveri bileşeninde belirtilmemiş.");
            } else {
                if (ustveriEki.getTur() != STKodEkTuru.DED) {
                    throw new Exception("Paket içerisine eklenmiş ek, üstveri bileşeninde DED (Dahili Elektronik Dosya) olarak belirtilmelidir.");
                }
            }
        }
        if (_ustveri.getCTUstveri().getEkler() != null && _ustveri.getCTUstveri().getEkler().getEk() != null) {
            for (CTEk ustveriEki : _ustveri.getCTUstveri().getEkler().getEk()) {
                if (ustveriEki.getSiraNo() <= 0) {
                    throw new Exception("Paket içerisine SiraNo değeri '1'den küçük olan ek eklenemez.");
                }
            }
            for (CTEk ustveriEki : _ustveri.getCTUstveri().getEkler().getEk()) {
                for (CTEk ustveriEki2 : _ustveri.getCTUstveri().getEkler().getEk()) {
                    if (ustveriEki.getSiraNo() == ustveriEki2.getSiraNo() && ustveriEki.getId().getValue().compareToIgnoreCase(ustveriEki2.getId().getValue()) != 0) {
                        throw new Exception("Paket içerisine aynı SiraNo değerine sahip birden fazla ek eklenemez.");
                    }
                }
            }
            for (CTDagitim dagitim : _ustveri.getCTUstveri().getDagitimListesi().getDagitim()) {
                if (dagitim.getKonulmamisEkListesi() != null && dagitim.getKonulmamisEkListesi().getKonulmamisEk() != null && dagitim.getKonulmamisEkListesi().getKonulmamisEk().size() > 0) {
                    for (CTKonulmamisEk konulmamisEk : dagitim.getKonulmamisEkListesi().getKonulmamisEk()) {
                        String id = konulmamisEk.getEkId().toUpperCase();
                        Boolean ustVerideVarMi = false;
                        for (CTEk ustveriEki2 : _ustveri.getCTUstveri().getEkler().getEk()) {
                            if (ustveriEki2.getId().getValue().compareToIgnoreCase(id) == 0) {
                                ustVerideVarMi = true;
                                break;
                            }
                        }
                        if (!ustVerideVarMi) {
                            throw new Exception("UstVeri'de belirtilmemiş bir ek, DagitimListesinde KonulmamisEk olarak belirtilemez.");
                        }
                    }
                }
            }
        }
    }

    public void paketOzetiOlustur() throws Exception {
        if (_paketModu == PaketModu.Ac)
            throw new Exception("PaketModu 'Ac' olarak işlem yapılan paketler için paketOzetiOlustur fonksiyonu kullanılamaz.");

        if (!ustveriVarMi()) {
            throw new Exception("Üstveri bileşeni bulunmadığı durumlarda PaketOzetiOlustur fonksiyonu kullanılamaz.");
        }

        if (!ustYaziVarMi()) {
            throw new Exception("ÜstYazı bileşeni bulunmadığı durumlarda PaketOzetiOlustur fonksiyonu kullanılamaz.");
        }

        byte[] ozetUstVeri = Araclar.ozetHesapla(ustveriAl(), _varsayilanOzetModu);
        byte[] ozetUstVeriSha512 = Araclar.ozetHesapla(ustveriAl(), OzetModu.SHA512);
        _paketOzeti.ekle(_varsayilanOzetModu, ozetUstVeri, ozetUstVeriSha512, PackagingURIHelper
                .createPartName(Araclar.URI_USTVERI).getURI());


        PackageRelationshipCollection ustYaziRelationshipsByType = _package.getRelationshipsByType(Araclar.RELATION_TYPE_USTYAZI);
        PackagePartName ustYaziPart = PackagingURIHelper.createPartName(ustYaziRelationshipsByType.getRelationship(0).getTargetURI());
        byte[] ozetUstYazi = Araclar.ozetHesapla(ustYaziAl(), _varsayilanOzetModu);
        byte[] ozetUstYaziSha512 = Araclar.ozetHesapla(ustYaziAl(), OzetModu.SHA512);
        _paketOzeti.ekle(_varsayilanOzetModu, ozetUstYazi, ozetUstYaziSha512, ustYaziPart.getURI());

        if (parafImzaVarMi()) {
            PackagePartName parafImzaPartName = PackagingURIHelper.createPartName(Araclar.URI_PARAFIMZA);
            byte[] ozetParafImza = Araclar.ozetHesapla(parafImzaAl(), _varsayilanOzetModu);
            byte[] ozetParafImzaSha512 = Araclar.ozetHesapla(parafImzaAl(), OzetModu.SHA512);
            _paketOzeti.ekle(_varsayilanOzetModu, ozetParafImza, ozetParafImzaSha512, parafImzaPartName.getURI());
        }

        if (parafOzetiVarMi()) {
            PackagePartName parafOzetiPartNAme = PackagingURIHelper.createPartName(Araclar.URI_PARAFOZETI);
            byte[] ozetParafOzeti = Araclar.ozetHesapla(parafOzetiAl(), _varsayilanOzetModu);
            byte[] ozetParafOzetiSha512 = Araclar.ozetHesapla(parafOzetiAl(), OzetModu.SHA512);
            _paketOzeti.ekle(_varsayilanOzetModu, ozetParafOzeti, ozetParafOzetiSha512, parafOzetiPartNAme.getURI());
        }


        if (_ustveri.getCTUstveri().getEkler() != null && _ustveri.getCTUstveri().getEkler().getEk() != null && _ustveri.getCTUstveri().getEkler().getEk().size() > 0) {
            for (CTEk ctEk : _ustveri.getCTUstveri().getEkler().getEk()) {
                if (ctEk.getTur() != STKodEkTuru.DED) {
                    continue;
                }
                if (ctEk.isImzaliMi() == null || !ctEk.isImzaliMi()) {
                    continue;
                }

                URI relationShipTargetURI = _package.getRelationship(Araclar.ID_ROOT_EK + ctEk.getId().getValue()).getTargetURI();
                PackagePart part = _package.getPart(PackagingURIHelper.createPartName(relationShipTargetURI));
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Araclar.inputStreamToBytes(part.getInputStream()));
                ByteArrayInputStream byteArrayInputStreamSha512 = new ByteArrayInputStream(Araclar.inputStreamToBytes(part.getInputStream()));
                byte[] ekOzeti = Araclar.ozetHesapla(byteArrayInputStream, _varsayilanOzetModu);
                byte[] ekOzetiSha512 = Araclar.ozetHesapla(byteArrayInputStreamSha512, OzetModu.SHA512);

                _paketOzeti.ekle(_varsayilanOzetModu, ekOzeti, ekOzetiSha512, relationShipTargetURI);
            }
        }

        _paketOzeti.getPaketOzeti().setId(_ustveri.belgeIdAl().toUpperCase());
        _paketOzeti.kontrolEt();
        PackagePartName partNamePaketOzeti = PackagingURIHelper.createPartName(Araclar.URI_PAKETOZETI);
        if (_paketModu == PaketModu.Guncelle) {
            if (_package.containPart(partNamePaketOzeti)) {
                _package.deletePartRecursive(partNamePaketOzeti);
            }
        }

        _package.addRelationship(partNamePaketOzeti, TargetMode.INTERNAL, Araclar.RELATION_TYPE_PAKETOZETI, Araclar.ID_PAKETOZETI);
        PackagePart partPaketOzeti = _package.createPart(partNamePaketOzeti, Araclar.MIME_XML);

        OutputStream streamPaketOzeti = partPaketOzeti.getOutputStream();
        JAXBContext contextPaketOzeti = JAXBContext.newInstance(ObjectFactory.class);
        Marshaller marshallerPaketOzeti = contextPaketOzeti.createMarshaller();
        marshallerPaketOzeti.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshallerPaketOzeti.marshal(_paketOzeti.getPaketOzeti(), streamPaketOzeti);
        streamPaketOzeti.close();
    }

    public void parafOzetiOlustur() throws Exception {

        if (_paketModu == PaketModu.Ac) {
            throw new RuntimeException("PaketModu 'Ac' olarak işlem yapılan paketler için ParafOzetiOlustur fonksiyonu kullanılamaz.");
        }
        if (!ustveriVarMi()) {
            throw new RuntimeException("Üstveri bileşeni bulunmadığı durumlarda ParafOzetiOlustur fonksiyonu kullanılamaz.");
        }
        if (!ustYaziVarMi()) {
            throw new RuntimeException("ÜstYazı bileşeni bulunmadığı durumlarda ParafOzetiOlustur fonksiyonu kullanılamaz.");
        }

        byte[] ozetUstVeri = Araclar.ozetHesapla(ustveriAl(), _varsayilanOzetModu);
        byte[] ozetUstVeriSha512 = Araclar.ozetHesapla(ustveriAl(), OzetModu.SHA512);
        _parafOzeti.ekle(_varsayilanOzetModu, ozetUstVeri, ozetUstVeriSha512, PackagingURIHelper.createPartName(Araclar.URI_USTVERI).getURI());


        PackageRelationshipCollection ustYaziRelationshipsByType = _package.getRelationshipsByType(Araclar.RELATION_TYPE_USTYAZI);
        PackagePartName ustYaziPart = PackagingURIHelper.createPartName(ustYaziRelationshipsByType.getRelationship(0).getTargetURI());
        byte[] ozetUstYazi = Araclar.ozetHesapla(ustYaziAl(), _varsayilanOzetModu);
        byte[] ozetUstYaziSha512 = Araclar.ozetHesapla(ustYaziAl(), OzetModu.SHA512);
        _parafOzeti.ekle(_varsayilanOzetModu, ozetUstYazi, ozetUstYaziSha512, ustYaziPart.getURI());

        if (_ustveri.getCTUstveri().getEkler() != null && _ustveri.getCTUstveri().getEkler().getEk() != null && _ustveri.getCTUstveri().getEkler().getEk().size() > 0) {
            for (CTEk ctEk : _ustveri.getCTUstveri().getEkler().getEk()) {
                if (ctEk.getTur() != STKodEkTuru.DED) {
                    continue;
                }
                if (ctEk.isImzaliMi() == null || !ctEk.isImzaliMi()) {
                    continue;
                }

                URI relationShipTargetURI = _package.getRelationship(Araclar.ID_ROOT_EK + ctEk.getId().getValue()).getTargetURI();
                PackagePart part = _package.getPart(PackagingURIHelper.createPartName(relationShipTargetURI));
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Araclar.inputStreamToBytes(part.getInputStream()));
                ByteArrayInputStream byteArrayInputStreamSha512 = new ByteArrayInputStream(Araclar.inputStreamToBytes(part.getInputStream()));
                byte[] ekOzeti = Araclar.ozetHesapla(byteArrayInputStream, _varsayilanOzetModu);
                byte[] ekOzetiSha512 = Araclar.ozetHesapla(byteArrayInputStreamSha512, OzetModu.SHA512);

                _parafOzeti.ekle(_varsayilanOzetModu, ekOzeti, ekOzetiSha512, relationShipTargetURI);
            }
        }


        _parafOzeti.getParafOzeti().setId(_ustveri.belgeIdAl().toUpperCase());
        _parafOzeti.kontrolEt();
        PackagePartName partNameParafOzeti = PackagingURIHelper.createPartName(Araclar.URI_PARAFOZETI);
        if (_paketModu == PaketModu.Guncelle) {
            if (_package.containPart(partNameParafOzeti)) {
                _package.deletePartRecursive(partNameParafOzeti);
            }
        }

        _package.addRelationship(partNameParafOzeti, TargetMode.INTERNAL, Araclar.RELATION_TYPE_PARAFOZETI, Araclar.ID_PARAFOZETI);
        PackagePart partPaketOzeti = _package.createPart(partNameParafOzeti, Araclar.MIME_XML);

        OutputStream streamPaketOzeti = partPaketOzeti.getOutputStream();
        JAXBContext contextPaketOzeti = JAXBContext.newInstance(ObjectFactory.class);
        Marshaller marshallerPaketOzeti = contextPaketOzeti.createMarshaller();
        marshallerPaketOzeti.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshallerPaketOzeti.marshal(_parafOzeti.getParafOzeti(), streamPaketOzeti);
        streamPaketOzeti.close();
    }

    public void nihaiOzetOlustur() throws Exception {

        if (_paketModu == PaketModu.Ac)
            throw new Exception("PaketModu 'Ac' olarak işlem yapılan paketler için NihaiOzetOlustur fonksiyonu kullanılamaz.");
        if (!ustveriVarMi()) {
            throw new RuntimeException("Üstveri bileşeni bulunmadığı durumlarda NihaiOzetOlustur fonksiyonu kullanılamaz.");
        }
        if (!ustYaziVarMi()) {
            throw new RuntimeException("ÜstYazı bileşeni bulunmadığı durumlarda NihaiOzetOlustur fonksiyonu kullanılamaz.");
        }
        if (!nihaiUstveriVarMi()) {
            throw new RuntimeException("NihaiÜstveri bileşeni bulunmadığı durumlarda NihaiOzetOlustur fonksiyonu kullanılamaz.");
        }

        if (!imzaVarMi()) {
            throw new RuntimeException("Elektronik İmza bileşeni bulunmadığı durumlarda NihaiOzetOlustur fonksiyonu kullanılamaz.");
        }

        byte[] ozetUstVeri = Araclar.ozetHesapla(ustveriAl(), _varsayilanOzetModu);
        byte[] ozetUstVeriSha512 = Araclar.ozetHesapla(ustveriAl(), OzetModu.SHA512);
        _nihaiOzet.ekle(_varsayilanOzetModu, ozetUstVeri, ozetUstVeriSha512, PackagingURIHelper
                .createPartName(Araclar.URI_USTVERI).getURI());


        PackageRelationshipCollection ustYaziRelationshipsByType = _package.getRelationshipsByType(Araclar.RELATION_TYPE_USTYAZI);
        PackagePartName ustYaziPart = PackagingURIHelper.createPartName(ustYaziRelationshipsByType.getRelationship(0).getTargetURI());
        byte[] ozetUstYazi = Araclar.ozetHesapla(ustYaziAl(), _varsayilanOzetModu);
        byte[] ozetUstYaziSha512 = Araclar.ozetHesapla(ustYaziAl(), OzetModu.SHA512);
        _nihaiOzet.ekle(_varsayilanOzetModu, ozetUstYazi, ozetUstYaziSha512, ustYaziPart.getURI());

        if (parafImzaVarMi()) {
            PackagePartName parafImzaPartName = PackagingURIHelper.createPartName(Araclar.URI_PARAFIMZA);
            byte[] ozetParafImza = Araclar.ozetHesapla(parafImzaAl(), _varsayilanOzetModu);
            byte[] ozetParafImzaSha512 = Araclar.ozetHesapla(parafImzaAl(), OzetModu.SHA512);
            _nihaiOzet.ekle(_varsayilanOzetModu, ozetParafImza, ozetParafImzaSha512, parafImzaPartName.getURI());
        }

        if (parafOzetiVarMi()) {
            PackagePartName parafOzetiPartNAme = PackagingURIHelper.createPartName(Araclar.URI_PARAFOZETI);
            byte[] ozetParafOzeti = Araclar.ozetHesapla(parafOzetiAl(), _varsayilanOzetModu);
            byte[] ozetParafOzetiSha512 = Araclar.ozetHesapla(parafOzetiAl(), OzetModu.SHA512);
            _nihaiOzet.ekle(_varsayilanOzetModu, ozetParafOzeti, ozetParafOzetiSha512, parafOzetiPartNAme.getURI());
        }

        PackageRelationshipCollection paketOzetiRelationshipsByType = _package.getRelationshipsByType(Araclar.RELATION_TYPE_PAKETOZETI);
        PackagePartName paketOzetiPart = PackagingURIHelper.createPartName(paketOzetiRelationshipsByType.getRelationship(0).getTargetURI());
        byte[] ozetPaketOzeti = Araclar.ozetHesapla(paketOzetiAl(), _varsayilanOzetModu);
        byte[] ozetPaketOzetiSha512 = Araclar.ozetHesapla(paketOzetiAl(), OzetModu.SHA512);
        _nihaiOzet.ekle(_varsayilanOzetModu, ozetPaketOzeti, ozetPaketOzetiSha512, paketOzetiPart.getURI());

        byte[] ozetImza = Araclar.ozetHesapla(imzaAl(), _varsayilanOzetModu);
        byte[] ozetImzaSha512 = Araclar.ozetHesapla(imzaAl(), OzetModu.SHA512);
        _nihaiOzet.ekle(_varsayilanOzetModu, ozetImza, ozetImzaSha512, PackagingURIHelper.createPartName(Araclar.URI_IMZA).getURI());

        byte[] ozetNihaiUstVeri = Araclar.ozetHesapla(nihaiUstveriAl(), _varsayilanOzetModu);
        byte[] ozetNihaiUstveriSha512 = Araclar.ozetHesapla(nihaiUstveriAl(), OzetModu.SHA512);
        _nihaiOzet.ekle(_varsayilanOzetModu, ozetNihaiUstVeri, ozetNihaiUstveriSha512, PackagingURIHelper.createPartName(Araclar.URI_NIHAIUSTVERI).getURI());

        if (_ustveri.getCTUstveri().getEkler() != null && _ustveri.getCTUstveri().getEkler().getEk() != null && _ustveri.getCTUstveri().getEkler().getEk().size() > 0) {
            for (CTEk ctEk : _ustveri.getCTUstveri().getEkler().getEk()) {
                if (ctEk.getTur() != STKodEkTuru.DED) {
                    continue;
                }
                if (ctEk.isImzaliMi() == null || !ctEk.isImzaliMi()) {
                    continue;
                }
                URI relationShipTargetURI = _package.getRelationship(Araclar.ID_ROOT_EK + ctEk.getId().getValue()).getTargetURI();
                PackagePart part = _package.getPart(PackagingURIHelper.createPartName(relationShipTargetURI));
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Araclar.inputStreamToBytes(part.getInputStream()));
                ByteArrayInputStream byteArrayInputStreamSha512 = new ByteArrayInputStream(Araclar.inputStreamToBytes(part.getInputStream()));
                byte[] ekOzeti = Araclar.ozetHesapla(byteArrayInputStream, _varsayilanOzetModu);
                byte[] ekOzetiSha512 = Araclar.ozetHesapla(byteArrayInputStreamSha512, OzetModu.SHA512);
                _nihaiOzet.ekle(_varsayilanOzetModu, ekOzeti, ekOzetiSha512, relationShipTargetURI);
            }
        }

        //Core NİHAİ ÖZETE Daha sonra NihaiOzeteCore ekle metodu ile eklenmeli.
        _nihaiOzet.getNihaiOzet().setId(_ustveri.belgeIdAl().toUpperCase());
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

    public void kapat() throws Exception {
        if (_paketModu == PaketModu.Olustur) {
            PackageRelationshipCollection ustYaziRelation = _package.getRelationshipsByType(Araclar.RELATION_TYPE_USTYAZI);
            if (ustYaziRelation == null || ustYaziRelation.size() == 0) {
                throw new Exception("Üst yazı eklenmemiş.");
            }
            paketBelirteciBelirle(_ustveri.getCTUstveri().getBelgeId());
            paketOlusturanBelirle(Araclar.olusturanAdiOlustur(_ustveri.getCTUstveri().getOlusturan()));
            paketKonusuBelirle(_ustveri.getCTUstveri().getKonu().getValue());
            paketKategorisiBelirle(Araclar.RESMIYAZISMA);
            paketOlusturulmaTarihiBelirle(new Date());
            paketIcerikTuruBelirle(Araclar.EYAZISMAMIMETURU);
            paketVersiyonuBelirle(Araclar.PAKET_VERSIYON);
            paketRevizyonuBelirle(String.format(Araclar.PAKET_REVIZYON, Paket.class.getPackage().getSpecificationVersion()));
            _package.flush();
            _package.close();

        } else if (_paketModu == PaketModu.Guncelle) {
            paketBelirteciBelirle(_ustveri.getCTUstveri().getBelgeId());
            paketOlusturanBelirle(Araclar.olusturanAdiOlustur(_ustveri.getCTUstveri().getOlusturan()));
            paketKonusuBelirle(_ustveri.getCTUstveri().getKonu().getValue());
            paketKategorisiBelirle(Araclar.RESMIYAZISMA);
            paketIcerikTuruBelirle(Araclar.EYAZISMAMIMETURU);
            paketVersiyonuBelirle(Araclar.PAKET_VERSIYON);
            paketRevizyonuBelirle(String.format(Araclar.PAKET_REVIZYON, Paket.class.getPackage().getSpecificationVersion()));
            _package.flush();
            _package.close();
        } else if (_paketModu == PaketModu.Ac) {
            _package.close();
        }
    }


    // =======================================Ustveri START===========================
    public InputStream ustveriAl() throws InvalidFormatException, IOException {
        PackagePart part = _package.getPart(PackagingURIHelper.createPartName(Araclar.URI_USTVERI));
        if (part == null)
            return null;
        return _package.getPart(PackagingURIHelper.createPartName(Araclar.URI_USTVERI)).getInputStream();
    }

    public void ustveriOlustur() throws Exception {
        if (_paketModu == PaketModu.Ac)
            throw new Exception("PaketModu 'Ac' olarak işlem yapılan paketler için UstveriOlustur fonksiyonu kullanılamaz.");
        _ustveri.kontrolEt();
        PackagePartName partNameUstveri = PackagingURIHelper.createPartName(Araclar.URI_USTVERI);
        if (_paketModu == PaketModu.Guncelle) {
            if (_package.containPart(partNameUstveri)) {
                _package.deletePartRecursive(partNameUstveri);
            }
        }
        _package.addRelationship(partNameUstveri, TargetMode.INTERNAL, Araclar.RELATION_TYPE_USTVERI, Araclar.ID_USTVERI);
        PackagePart partUstveri = _package.createPart(partNameUstveri, Araclar.MIME_XML);
        OutputStream streamUstveri = partUstveri.getOutputStream();
        JAXBContext contextUstveri = JAXBContext.newInstance(ObjectFactory.class);
        Marshaller marshallerUstveri = contextUstveri.createMarshaller();
        marshallerUstveri.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        marshallerUstveri.marshal(_ustveri.getCTUstveri(), streamUstveri);
        streamUstveri.close();
    }

    public boolean ustveriVarMi() throws InvalidFormatException {

        return _package.containPart(PackagingURIHelper.createPartName(Araclar.URI_USTVERI));
    }

    // ============================ NİHAİ ÜST VERİ START============================

    public boolean nihaiUstveriVarMi() throws InvalidFormatException {
        return _package.containPart(PackagingURIHelper.createPartName(Araclar.URI_NIHAIUSTVERI));
    }

    public InputStream nihaiUstveriAl() throws InvalidFormatException, IOException {
        if (_package.containPart(PackagingURIHelper.createPartName(Araclar.URI_NIHAIUSTVERI))) {
            return _package.getPart(PackagingURIHelper.createPartName(Araclar.URI_NIHAIUSTVERI)).getInputStream();
        }
        return null;
    }

    public void nihaiUstVeriOlustur() throws Exception {

        if (_paketModu == PaketModu.Ac) {
            throw new Exception("PaketModu 'Ac' olarak işlem yapılan paketler için NihaiUstveriOlustur fonksiyonu kullanılamaz.");
        }

        XsdAraclar.kontrolEt(_nihaiUstveri.getNihaiUstveri());
        PackagePartName nihaiUstVeriPartName = PackagingURIHelper.createPartName(Araclar.URI_NIHAIUSTVERI);
        if (_paketModu == PaketModu.Guncelle) {
            if (_package.containPart(nihaiUstVeriPartName)) {
                _package.deletePartRecursive(nihaiUstVeriPartName);
            }
        }

        _package.addRelationship(nihaiUstVeriPartName, TargetMode.INTERNAL, Araclar.RELATION_TYPE_NIHAIUSTVERI, Araclar.ID_NIHAIUSTVERI);
        PackagePart part = _package.createPart(nihaiUstVeriPartName, Araclar.MIME_XML);

        OutputStream stream = part.getOutputStream();
        JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(_nihaiUstveri.getNihaiUstveri(), stream);
        stream.close();

    }

    // =======================================Ust Yazi START===========================

    public void ustYaziEkle(String dosyaYolu, String mimeTuru) throws Exception {
        ustYaziEkle(dosyaYolu, mimeTuru, _varsayilanOzetModu);
    }

    public void ustYaziEkle(InputStream dosya, String dosyaAdi, String mimeTuru) throws Exception {
        ustYaziEkle(dosya, dosyaAdi, mimeTuru, _varsayilanOzetModu);
    }

    public void ustYaziEkle(String dosyaYolu, String mimeTuru, OzetModu ozetModu) throws Exception {
        if (_package.getRelationshipsByType(Araclar.RELATION_TYPE_USTYAZI) != null && _package.getRelationshipsByType(Araclar.RELATION_TYPE_USTYAZI).size() == 1)
            throw new Exception("Daha önce üstyazı eklenmiş.");
        if (dosyaYolu == null)
            throw new Exception("dosyaYolu");
        if (mimeTuru == null)
            throw new Exception("mimeTuru");
        if (!(new File(dosyaYolu)).exists())
            throw new Exception("Dosya bulunamadı.");
        if (ozetModu == OzetModu.Yok)
            throw new Exception("OzetModu YOK olamaz.");
        if (_paketModu == PaketModu.Ac)
            throw new Exception("PaketModu 'Ac' olarak işlem yapılan paketler için UstYaziEkle fonksiyonu kullanılamaz.");
        FileInputStream fis = new FileInputStream(dosyaYolu);
        ustYaziEkle(fis, new File(dosyaYolu).getName(), mimeTuru, ozetModu);
        fis.close();
    }

    public void ustYaziEkle(InputStream dosya, String dosyaAdi, String mimeTuru, OzetModu ozetModu) throws Exception {
        if (_package.getRelationshipsByType(Araclar.RELATION_TYPE_USTYAZI) != null && _package.getRelationshipsByType(Araclar.RELATION_TYPE_USTYAZI).size() == 1)
            throw new Exception("Daha önce üstyazı eklenmiş.");
        if (dosya == null)
            throw new Exception("dosya");
        if (mimeTuru == null)
            throw new Exception("mimeTuru");
        if (dosyaAdi == null)
            throw new Exception("dosyaAdi");
        if (ozetModu == OzetModu.Yok)
            throw new Exception("OzetModu YOK olamaz.");
        if (_paketModu == PaketModu.Ac)
            throw new Exception("PaketModu 'Ac' olarak işlem yapılan paketler için UstYaziEkle fonksiyonu kullanılamaz.");

        PackagePartName samplePartName = PackagingURIHelper.createPartName(String.format(Araclar.URI_FORMAT_USTYAZI, Araclar.encodePath(dosyaAdi)));
        PackagePart samplePart = _package.createPart(samplePartName, mimeTuru);
        OutputStream partStream = samplePart.getOutputStream();
        byte fileData[] = Araclar.inputStreamToBytes(dosya);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fileData);
        Araclar.copyStream(byteArrayInputStream, partStream);
        samplePartName = PackagingURIHelper.createPartName(String.format(Araclar.URI_FORMAT_USTYAZI, Araclar.encodePath(dosyaAdi)));
        _package.addRelationship(samplePartName, TargetMode.INTERNAL, Araclar.RELATION_TYPE_USTYAZI, Araclar.ID_USTYAZI);
        _ustveri.mimeTuruBelirle(mimeTuru);
        partStream.close();
        samplePart.close();
    }

    public boolean ustYaziVarMi() {
        return _package.getRelationshipsByType(Araclar.RELATION_TYPE_USTYAZI).size() == 1;
    }

    public InputStream ustYaziAl() throws Exception {
        if (_package.getRelationshipsByType(Araclar.RELATION_TYPE_USTYAZI) == null || _package.getRelationshipsByType(Araclar.RELATION_TYPE_USTYAZI).size() < 1) {
            return null;
        }
        return _package.getPart(_package.getRelationshipsByType(Araclar.RELATION_TYPE_USTYAZI).getRelationship(0)).getInputStream();
    }


    // =======================================Ek START===========================
    public void ekEkle(CTEk ek, String dosyaYolu) throws Exception {
        ekEkle(ek, dosyaYolu, _varsayilanOzetModu);
    }

    public void ekEkle(CTEk ek, String dosyaYolu, OzetModu ozetModu) throws Exception {
        if (dosyaYolu == null)
            throw new IllegalArgumentException("dosyaYolu");
        if (!(new File(dosyaYolu)).exists())
            throw new IllegalArgumentException("Dosya bulunamadı.");
        if (ek == null)
            throw new IllegalArgumentException("ek");
        if (ek.getMimeTuru() == null || ek.getMimeTuru().trim().isEmpty())
            throw new IllegalArgumentException("ek.MimeTuru");
        if ((ek.getId() == null || ek.getId().getValue().isEmpty()))
            throw new IllegalArgumentException("ek.Id");
        if (_paketModu == PaketModu.Ac)
            throw new Exception("PaketModu 'Ac' olarak işlem yapılan paketler için EkEkle fonksiyonu kullanılamaz.");

        ekEkle(ek, new FileInputStream(dosyaYolu), new File(dosyaYolu).getName(), ozetModu);
    }

    public void ekEkle(CTEk ek, InputStream dosya, String dosyaAdi, OzetModu ozetModu) throws Exception {
        if (dosya == null)
            throw new Exception("dosya");
        if (dosyaAdi == null)
            throw new Exception("dosyaAdi");
        if (ek == null)
            throw new IllegalArgumentException("ek");
        if (ek.getMimeTuru() == null || ek.getMimeTuru().trim().isEmpty())
            throw new IllegalArgumentException("ek.MimeTuru");
        if ((ek.getId() == null || ek.getId().getValue().isEmpty()))
            throw new IllegalArgumentException("ek.Id");
        if (_paketModu == PaketModu.Ac)
            throw new Exception("PaketModu 'Ac' olarak işlem yapılan paketler için EkEkle fonksiyonu kullanılamaz.");
        if (ek.isImzaliMi() != null && ek.isImzaliMi() && ozetModu == OzetModu.Yok) {
            throw new Exception("İmzalı bir ek, ÖzetModu değeri verilmeksizin pakete eklenemez.");
        }
        if (ek.isImzaliMi() != null && !ek.isImzaliMi() && ozetModu != OzetModu.Yok) {
            throw new Exception("İmzasız bir ek, ÖzetModu değeri verilerek pakete eklenemez.");
        }
        String klasorAdi;
        String iliskiAdi;
        String iliskiId;
        if (ozetModu == OzetModu.Yok) {
            klasorAdi = Araclar.URI_ROOT_IMZASIZEK;
            iliskiAdi = Araclar.RELATION_TYPE_IMZASIZEK;
            iliskiId = Araclar.ID_ROOT_IMZASIZEK;
        } else {
            klasorAdi = Araclar.URI_ROOT_EK;
            iliskiAdi = Araclar.RELATION_TYPE_EK;
            iliskiId = Araclar.ID_ROOT_EK;
        }
        PackagePartName partNameEk = PackagingURIHelper.createPartName("/" + klasorAdi + "/" + Araclar.encodePath(dosyaAdi));
        PackagePart partEk = _package.createPart(partNameEk, ek.getMimeTuru());
        OutputStream streamEk = partEk.getOutputStream();
        byte fileData[] = Araclar.inputStreamToBytes(dosya);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fileData);
        Araclar.copyStream(byteArrayInputStream, streamEk);
        String EkId = ek.getId().getValue();
        _package.addRelationship(partNameEk, TargetMode.INTERNAL, iliskiAdi, iliskiId + EkId.toUpperCase());
        streamEk.close();
        partEk.close();
        dosya.close();
    }

    public InputStream ekAl(UUID id) throws Exception {
        if (_package.getRelationship(Araclar.ID_ROOT_EK + id.toString().toUpperCase()) != null) {
            return _package.getPart(PackagingURIHelper.createPartName(_package.getRelationship(Araclar.ID_ROOT_EK + id.toString().toUpperCase()).getTargetURI())).getInputStream();
        }
        if (_package.getRelationship(Araclar.ID_ROOT_IMZASIZEK + id.toString().toUpperCase()) != null) {
            return _package.getPart(PackagingURIHelper.createPartName(_package.getRelationship(Araclar.ID_ROOT_IMZASIZEK + id.toString().toUpperCase()).getTargetURI())).getInputStream();
        }
        return null;
    }

    public boolean ekCikar(UUID id) throws Exception {
        if (_paketModu == PaketModu.Ac)
            throw new Exception("PaketModu 'Ac' olarak işlem yapılan paketler için EkCikar fonksiyonu kullanılamaz.");
        PackageRelationship pr = _package.getRelationship(Araclar.ID_ROOT_EK + id.toString().toUpperCase());
        if (pr != null) {
            PackagePartName samplePartName = PackagingURIHelper.createPartName(pr.getTargetURI());
            _package.deletePartRecursive(samplePartName);
            _package.removeRelationship(Araclar.ID_ROOT_EK + id.toString().toUpperCase());
            return true;
        }
        pr = _package.getRelationship(Araclar.ID_ROOT_IMZASIZEK + id.toString().toUpperCase());
        if (pr != null) {
            PackagePartName samplePartName = PackagingURIHelper.createPartName(pr.getTargetURI());
            _package.deletePartRecursive(samplePartName);
            _package.removeRelationship(Araclar.ID_ROOT_IMZASIZEK + id.toString().toUpperCase());
            return true;
        }
        return false;
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

    // =======================================Imza START===========================


    public void imzaEkle(byte[] imza) throws Exception {
        if (imza == null) {
            throw new IllegalArgumentException("imza");
        }

        if (_paketModu == PaketModu.Ac)
            throw new RuntimeException("PaketModu 'Ac' olarak işlem yapılan paketler için ImzaEkle fonksiyonu kullanılamaz.");

        PackagePartName imzaPartName = PackagingURIHelper.createPartName(Araclar.URI_IMZA);
        if (_package.containPart(imzaPartName)) {
            _package.removePart(imzaPartName);
            PackagePartName relationshipPartName = PackagingURIHelper.getRelationshipPartName(imzaPartName);
            if (_package.containPart(relationshipPartName)) {
                _package.removePart(relationshipPartName);
            }
            _package.getPart(PackagingURIHelper.createPartName(Araclar.URI_PAKETOZETI)).removeRelationship(Araclar.ID_IMZA);
        }

        PackagePart partParafImza = _package.createPart(imzaPartName, Araclar.MIME_OCTETSTREAM);
        ByteArrayInputStream fileInput = new ByteArrayInputStream(imza);
        OutputStream stream = partParafImza.getOutputStream();
        Araclar.copyStream(fileInput, stream);
        fileInput.reset();
        _package.getPart(_package.getRelationshipsByType(Araclar.RELATION_TYPE_PAKETOZETI).getRelationship(0))
                .addRelationship(imzaPartName, TargetMode.INTERNAL, Araclar.RELATION_TYPE_IMZA, Araclar.ID_IMZA);
        stream.close();
    }

    public boolean imzaVarMi() throws InvalidFormatException {
        return _package.containPart(PackagingURIHelper.createPartName(Araclar.URI_IMZA));
    }

    public InputStream imzaAl() throws Exception {
        if (_package.containPart(PackagingURIHelper.createPartName(Araclar.URI_IMZA))) {
            return _package.getPart(PackagingURIHelper.createPartName(Araclar.URI_IMZA)).getInputStream();
        }
        return null;
    }

    // =======================================PARAF İMZA START===========================
    public boolean parafImzaVarMi() throws InvalidFormatException {
        return _package.containPart(PackagingURIHelper.createPartName(Araclar.URI_PARAFIMZA));
    }

    public InputStream parafImzaAl() throws InvalidFormatException, IOException {
        if (_package.containPart(PackagingURIHelper.createPartName(Araclar.URI_PARAFIMZA))) {
            return _package.getPart(PackagingURIHelper.createPartName(Araclar.URI_PARAFIMZA)).getInputStream();
        }
        return null;
    }

    public void parafImzaEkle(byte[] imza) throws Exception {
        if (imza == null) {
            throw new IllegalArgumentException("imza");
        }

        if (_paketModu == PaketModu.Ac)
            throw new RuntimeException("PaketModu 'Ac' olarak işlem yapılan paketler için ParafImzaEkle fonksiyonu kullanılamaz.");

        PackagePartName parafImzaPartName = PackagingURIHelper.createPartName(Araclar.URI_PARAFIMZA);
        if (_package.containPart(parafImzaPartName)) {
            _package.removePart(parafImzaPartName);

            PackagePartName relationshipPartName = PackagingURIHelper.getRelationshipPartName(parafImzaPartName);
            if (_package.containPart(relationshipPartName)) {
                _package.removePart(relationshipPartName);
            }
            _package.getPart(PackagingURIHelper.createPartName(Araclar.URI_PARAFOZETI)).removeRelationship(Araclar.ID_PARAFIMZA);
        }

        PackagePart partParafImza = _package.createPart(parafImzaPartName, Araclar.MIME_OCTETSTREAM);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(imza);
        OutputStream outputStream = partParafImza.getOutputStream();
        Araclar.copyStream(inputStream, outputStream);
        inputStream.reset();
        _package.getPart(_package.getRelationshipsByType(Araclar.RELATION_TYPE_PARAFOZETI).getRelationship(0))
                .addRelationship(parafImzaPartName, TargetMode.INTERNAL, Araclar.RELATION_TYPE_PARAFIMZA, Araclar.ID_PARAFIMZA);
        outputStream.close();
    }


    public boolean parafImzaCikar() throws InvalidFormatException {
        if (_paketModu == PaketModu.Ac)
            throw new RuntimeException("PaketModu 'Ac' olarak işlem yapılan paketler için EkCikar fonksiyonu kullanılamaz.");

        PackagePartName partNameParafImza = PackagingURIHelper.createPartName(Araclar.URI_PARAFIMZA);
        if (_package.containPart(partNameParafImza)) {
            _package.removePart(partNameParafImza);
            _package.getPart(PackagingURIHelper.createPartName(Araclar.URI_PARAFOZETI)).removeRelationship(Araclar.ID_PARAFIMZA);
            return true;
        }
        return false;
    }


    // =======================================Muhur START===========================
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

    // =======================================PaketOzeti START===========================
    public InputStream paketOzetiAl() throws Exception {
        PackageRelationshipCollection colPaketOzeti = _package.getRelationshipsByType(Araclar.RELATION_TYPE_PAKETOZETI);
        if (colPaketOzeti == null || colPaketOzeti.size() == 0) {
            return null;
        }
        return _package.getPart(_package.getRelationshipsByType(Araclar.RELATION_TYPE_PAKETOZETI).getRelationship(0)).getInputStream();
    }

    public boolean referansDogrula(CTReference item, List<OzetDogrulamaHatasi> sonuclar) {
        return referansDogrula(item, sonuclar, null);
    }

    public boolean referansDogrula(CTReference item, List<OzetDogrulamaHatasi> sonuclar, IdentifierType dagitimIdentifier) {

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
            if (item.getURI().equals(Araclar.URI_PARAFIMZA)) {
                /*
                 * /Paraflar/ParafImzaCades.imz zorunlu olmadığından ve daha sonra paket içerisinden çıkartılabildiğinden dolayı
                 * eğer paket içerisinde yok ise paketin içerisinden çıkartıldığı düşünülmektedir ve doğrulama sonucunda özel hata kodu döndürülmektedir.
                 * Kendisine gönderilen paketi açan kurum ParafImza bileşeninin çıkartıldığını varsaymalıdır ve paketin doğruluğunu kabul etmelidir.
                 * */
                sonuclar.add(new OzetDogrulamaHatasi(String.format("Paket bileşeni alınamadı. URI:%s.", item.getURI()),
                        OzetDogrulamaHataKodu.PARAF_IMZA_BILESENI_YOK, item.getURI(), ex));
                return false;
            }
            if (!item.getURI().startsWith("/Ekler/")) {
                sonuclar.add(new OzetDogrulamaHatasi(String.format("Paket bileşeni alınamadı. URI:%s.", item.getURI()),
                        OzetDogrulamaHataKodu.PAKET_BILESENI_ALINAMADI, item.getURI(), ex));
                return false;
            }
            if (dagitimIdentifier == null) {
                sonuclar.add(new OzetDogrulamaHatasi(String.format("Paket bileşeni alınamadı. URI:%s.", item.getURI()),
                        OzetDogrulamaHataKodu.PAKET_BILESENI_ALINAMADI, item.getURI(), ex));
                return false;
            }
            List<CTDagitim> dagitimlar = _ustveri.dagitimlariAl();
            if (dagitimlar == null || dagitimlar.size() == 0) {
                sonuclar.add(new OzetDogrulamaHatasi(String.format("Paket bileşeni alınamadı. URI:%s.", item.getURI()),
                        OzetDogrulamaHataKodu.PAKET_BILESENI_ALINAMADI, item.getURI(), ex));
                return false;
            }

            List<CTEk> ekler = _ustveri.ekleriAl();
            if (ekler == null || ekler.size() == 0) {
                sonuclar.add(new OzetDogrulamaHatasi(String.format("Paket bileşeni alınamadı. URI:%s.", item.getURI()),
                        OzetDogrulamaHataKodu.PAKET_BILESENI_ALINAMADI, item.getURI(), ex));
                return false;
            }
            String ekAdi = item.getURI().replace("/Ekler/", "");
            if (Araclar.isNullOrWhitespace(ekAdi)) {
                sonuclar.add(new OzetDogrulamaHatasi(String.format("Paket bileşeni alınamadı. URI:%s.", item.getURI()),
                        OzetDogrulamaHataKodu.PAKET_BILESENI_ALINAMADI, item.getURI(), ex));
                return false;
            }

            CTEk ek = null;
            for (CTEk ctEk : ekler) {
                if (ctEk.getDosyaAdi().equals(ekAdi)) {
                    ek = ctEk;
                    break;
                }
            }
            if (ek == null) {
                sonuclar.add(new OzetDogrulamaHatasi(String.format("Paket bileşeni alınamadı. URI:%s.", item.getURI()),
                        OzetDogrulamaHataKodu.PAKET_BILESENI_ALINAMADI, item.getURI(), ex));
                return false;
            }

            CTDagitim ctDagitim = null;
            for (CTDagitim dagitim : dagitimlar) {
                if (dagitim.getTuzelSahis() != null) {
                    if (dagitim.getTuzelSahis().getId() != null && !Araclar.isNullOrWhitespace(dagitim.getTuzelSahis().getId().getValue())
                            && dagitim.getTuzelSahis().getId().getValue().equals(dagitimIdentifier.getValue())) {
                        ctDagitim = dagitim;
                        break;
                    }

                } else if (dagitim.getGercekSahis() != null) {
                    if (!Araclar.isNullOrWhitespace(dagitim.getGercekSahis().getTCKN()) && dagitim.getGercekSahis().getTCKN().equals(dagitimIdentifier.getValue())) {
                        ctDagitim = dagitim;
                        break;
                    }
                } else if (dagitim.getKurumKurulus() != null) {
                    if (!Araclar.isNullOrWhitespace(dagitim.getKurumKurulus().getKKK())
                            && dagitim.getKurumKurulus().getKKK().equals(dagitimIdentifier.getValue())) {
                        ctDagitim = dagitim;
                        break;
                    }
                }
            }

            if (ctDagitim == null || ctDagitim.getKonulmamisEkListesi() == null || ctDagitim.getKonulmamisEkListesi().getKonulmamisEk() == null
                    || ctDagitim.getKonulmamisEkListesi().getKonulmamisEk().size() == 0) {
                sonuclar.add(new OzetDogrulamaHatasi(String.format("Paket bileşeni alınamadı. URI:%s.", item.getURI()),
                        OzetDogrulamaHataKodu.PAKET_BILESENI_ALINAMADI, item.getURI(), ex));
                return false;
            } else {
                boolean ekVarmi = false;
                for (CTKonulmamisEk ctKonulmamisEk : ctDagitim.getKonulmamisEkListesi().getKonulmamisEk()) {
                    if (ctKonulmamisEk.getEkId().equals(ek.getId().getValue())) {
                        ekVarmi = true;
                    }
                }
                if (!ekVarmi) {
                    sonuclar.add(new OzetDogrulamaHatasi(String.format("Paket bileşeni alınamadı. URI:%s.", item.getURI()),
                            OzetDogrulamaHataKodu.PAKET_BILESENI_ALINAMADI, item.getURI(), ex));
                    return false;
                }
            }
            return true;
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

    public boolean paketOzetiVarMi() throws InvalidFormatException {
        return _package.containPart(PackagingURIHelper.createPartName(Araclar.URI_PAKETOZETI));
    }

    public boolean paketOzetiDogrula(CTPaketOzeti ozet, List<OzetDogrulamaHatasi> sonuclar) throws InvalidFormatException {
        return paketOzetiDogrula(ozet, sonuclar, null);
    }

    public boolean paketOzetiDogrula(CTPaketOzeti ozet, List<OzetDogrulamaHatasi> sonuclar, IdentifierType dagitimIdentifier) throws InvalidFormatException {
        if (sonuclar == null)
            return false;

        if (ozet == null) {
            sonuclar.add(new OzetDogrulamaHatasi("Ozet değeri verilmemiş.", OzetDogrulamaHataKodu.OZET_DEGERI_VERILMEMIS));
            return false;
        }

        if (_package.getRelationshipsByType(Araclar.RELATION_TYPE_USTVERI) == null || _package.getRelationshipsByType(Araclar.RELATION_TYPE_USTVERI).size() == 0) {
            sonuclar.add(new OzetDogrulamaHatasi("Ustveri bileşeni yok.", OzetDogrulamaHataKodu.USTVERI_BILESENI_YOK));
            return false;
        }

        URI targetURI = _package.getRelationshipsByType(Araclar.RELATION_TYPE_USTVERI).getRelationship(0).getTargetURI();
        URI partURI = PackagingURIHelper.createPartName(targetURI).getURI();
        for (CTReference ctReference : ozet.getReference()) {
            if (ctReference.getURI().equals(partURI)) {
                sonuclar.add(new OzetDogrulamaHatasi("PaketOzet'inde Ustveri ozet değeri yok.", OzetDogrulamaHataKodu.PAKETOZET_INDE_USTVERI_OZET_DEGERI_YOK));
                return false;
            }
        }

        if (_package.getRelationshipsByType(Araclar.RELATION_TYPE_USTYAZI) == null || _package.getRelationshipsByType(Araclar.RELATION_TYPE_USTYAZI).size() == 0) {
            sonuclar.add(new OzetDogrulamaHatasi("UstYazi bileşeni yok.", OzetDogrulamaHataKodu.USTYAZI_BILESENI_YOK));
            return false;
        }

        URI targetURIUstYazi = _package.getRelationshipsByType(Araclar.RELATION_TYPE_USTYAZI).getRelationship(0).getTargetURI();
        URI partURIUstYazi = PackagingURIHelper.createPartName(targetURIUstYazi).getURI();

        for (CTReference ctReference : ozet.getReference()) {
            if (ctReference.getURI().equals(partURIUstYazi)) {
                sonuclar.add(new OzetDogrulamaHatasi("PaketOzet'inde UstYazi ozet değeri yok.", OzetDogrulamaHataKodu.PAKETOZET_INDE_USTYAZI_OZET_DEGERI_YOK));
                return false;
            }
        }

        for (CTReference ctReference : ozet.getReference()) {
            referansDogrula(ctReference, sonuclar, dagitimIdentifier);
        }
        return sonuclar.size() == 0;
    }

    // =======================================Nihai Ozet END===========================


    // =======================================PARAF ÖZETİ START===========================

    public InputStream parafOzetiAl() throws InvalidFormatException, IOException {
        if (_package.containPart(PackagingURIHelper.createPartName(Araclar.URI_PARAFOZETI))) {
            return _package.getPart(PackagingURIHelper.createPartName(Araclar.URI_PARAFOZETI)).getInputStream();
        }
        return null;
    }

    public boolean parafOzetiVarMi() throws InvalidFormatException {
        return _package.containPart(PackagingURIHelper.createPartName(Araclar.URI_PARAFOZETI));
    }

    public boolean parafOzetiDogrula(CTParafOzeti ozet, List<OzetDogrulamaHatasi> sonuclar) throws InvalidFormatException {
        return parafOzetiDogrula(ozet, sonuclar, null);
    }

    public boolean parafOzetiDogrula(CTParafOzeti ozet, List<OzetDogrulamaHatasi> sonuclar, IdentifierType dagitimIdentifier) throws InvalidFormatException {
        if (sonuclar == null)
            return false;

        if (ozet == null) {
            sonuclar.add(new OzetDogrulamaHatasi("Ozet değeri verilmemiş.", OzetDogrulamaHataKodu.OZET_DEGERI_VERILMEMIS));
            return false;
        }

        if (_package.getRelationshipsByType(Araclar.RELATION_TYPE_USTVERI) == null || _package.getRelationshipsByType(Araclar.RELATION_TYPE_USTVERI).size() == 0) {
            sonuclar.add(new OzetDogrulamaHatasi("Ustveri bileşeni yok.", OzetDogrulamaHataKodu.USTVERI_BILESENI_YOK));
            return false;
        }

        URI targetURI = _package.getRelationshipsByType(Araclar.RELATION_TYPE_USTVERI).getRelationship(0).getTargetURI();
        boolean ustVeriBileseniVarMi = false;
        for (int i = 0; i <= ozet.getReference().size() - 1; i++) {
            if (PackagingURIHelper.decodeURI(URI.create(ozet.getReference().get(i).getURI())).compareTo(PackagingURIHelper.decodeURI(targetURI)) == 0) {
                ustVeriBileseniVarMi = true;
                break;
            }
            if (ozet.getReference().get(i).getURI().compareTo(targetURI.toString()) == 0) {
                ustVeriBileseniVarMi = true;
                break;
            }
        }
        if (!ustVeriBileseniVarMi) {
            sonuclar.add(new OzetDogrulamaHatasi("ParafOzet'inde UstVeri ozet değeri yok.", OzetDogrulamaHataKodu.PARAFOZET_INDE_USTVERI_OZET_DEGERI_YOK));
            return false;
        }


        if (_package.getRelationshipsByType(Araclar.RELATION_TYPE_USTYAZI) == null || _package.getRelationshipsByType(Araclar.RELATION_TYPE_USTYAZI).size() == 0) {
            sonuclar.add(new OzetDogrulamaHatasi("UstYazi bileşeni yok.", OzetDogrulamaHataKodu.USTYAZI_BILESENI_YOK));
            return false;
        }
        URI targetURIUstYazi = _package.getRelationshipsByType(Araclar.RELATION_TYPE_USTYAZI).getRelationship(0).getTargetURI();
        boolean ustYaziBileseniVarMi = false;
        for (int i = 0; i <= ozet.getReference().size() - 1; i++) {
            if (PackagingURIHelper.decodeURI(URI.create(ozet.getReference().get(i).getURI())).compareTo(PackagingURIHelper.decodeURI(targetURIUstYazi)) == 0) {
                ustYaziBileseniVarMi = true;
                break;
            }
            if (ozet.getReference().get(i).getURI().compareTo(targetURIUstYazi.toString()) == 0) {
                ustYaziBileseniVarMi = true;
                break;
            }
        }
        if (!ustYaziBileseniVarMi) {
            sonuclar.add(new OzetDogrulamaHatasi("ParafOzet'inde UstYazi ozet değeri yok.", OzetDogrulamaHataKodu.PARAFOZET_INDE_USTYAZI_OZET_DEGERI_YOK));
            return false;
        }

        for (CTReference ctReference : ozet.getReference()) {
            referansDogrula(ctReference, sonuclar, dagitimIdentifier);
        }
        return sonuclar.size() == 0;
    }

    // =======================================PARAF ÖZETİ END===========================


    private String ekIdAl(CTEk ek) throws Exception {
        if (ek.getId() != null && ek.getId().getValue().length() != 0) {
            return ek.getId().getValue();
        } else {
            throw new Exception("EkId");
        }
    }

    public String getCorePartUri() {
        PackageRelationshipCollection cols = _package.getRelationshipsByType(Araclar.RELATION_TYPE_CORE);
        if (cols != null && cols.size() > 0) {
            return cols.getRelationship(0).getTargetURI().toString();
        }
        return null;
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
        return nihaiOzetDogrula(ozet, sonuclar, null);
    }

    public boolean nihaiOzetDogrula(CTNihaiOzet ozet, List<OzetDogrulamaHatasi> sonuclar, IdentifierType dagitimIdentifier) throws Exception {
        if (sonuclar == null)
            return false;

        if (ozet == null) {
            sonuclar.add(new OzetDogrulamaHatasi("Ozet değeri verilmemiş.", OzetDogrulamaHataKodu.OZET_DEGERI_VERILMEMIS));
            return false;
        }
        if (_package.getRelationshipsByType(Araclar.RELATION_TYPE_USTVERI) == null || _package.getRelationshipsByType(Araclar.RELATION_TYPE_USTVERI).size() == 0) {
            sonuclar.add(new OzetDogrulamaHatasi("Ustveri bileşeni yok.", OzetDogrulamaHataKodu.USTVERI_BILESENI_YOK));
        } else {
            URI readedUstveriUri = _package.getRelationshipsByType(Araclar.RELATION_TYPE_USTVERI).getRelationship(0).getTargetURI();
            boolean bilesenVarMi = false;
            for (int i = 0; i <= ozet.getReference().size() - 1; i++) {
                if (PackagingURIHelper.decodeURI(URI.create(ozet.getReference().get(i).getURI())).compareTo(PackagingURIHelper.decodeURI(readedUstveriUri)) == 0) {
                    bilesenVarMi = true;
                    break;
                }
                if (ozet.getReference().get(i).getURI().compareTo(readedUstveriUri.toString()) == 0) {
                    bilesenVarMi = true;
                    break;
                }
            }
            if (!bilesenVarMi) {
                sonuclar.add(new OzetDogrulamaHatasi("NihaiOzet'te Ustveri ozet değeri yok.", OzetDogrulamaHataKodu.NIHAIOZET_TE_USTVERI_OZET_DEGERI_YOK));
                return false;
            }
        }


        if (_package.getRelationshipsByType(Araclar.RELATION_TYPE_USTYAZI) == null || _package.getRelationshipsByType(Araclar.RELATION_TYPE_USTYAZI).size() == 0) {
            sonuclar.add(new OzetDogrulamaHatasi("UstYazi bileşeni yok.", OzetDogrulamaHataKodu.USTYAZI_BILESENI_YOK));
        } else {
            URI readedUstYaziUri = _package.getRelationshipsByType(Araclar.RELATION_TYPE_USTYAZI).getRelationship(0).getTargetURI();
            boolean bilesenVarMi = false;
            for (int i = 0; i <= ozet.getReference().size() - 1; i++) {
                if (PackagingURIHelper.decodeURI(URI.create(ozet.getReference().get(i).getURI())).compareTo(PackagingURIHelper.decodeURI(readedUstYaziUri)) == 0) {
                    bilesenVarMi = true;
                    break;
                }
                if (ozet.getReference().get(i).getURI().compareTo(readedUstYaziUri.toString()) == 0) {
                    bilesenVarMi = true;
                    break;
                }
            }
            if (!bilesenVarMi) {
                sonuclar.add(new OzetDogrulamaHatasi("NihaiOzet'te UstYazi ozet değeri yok.", OzetDogrulamaHataKodu.NIHAIOZET_TE_USTYAZI_OZET_DEGERI_YOK));
                return false;
            }
        }

        if (_package.getRelationshipsByType(Araclar.RELATION_TYPE_PAKETOZETI) == null || _package.getRelationshipsByType(Araclar.RELATION_TYPE_PAKETOZETI).size() == 0) {
            sonuclar.add(new OzetDogrulamaHatasi("PaketOzeti bileşeni yok.", OzetDogrulamaHataKodu.PAKETOZETI_BILESENI_YOK));
        } else {
            PackageRelationshipCollection relImza = _package.getPart(PackagingURIHelper.createPartName(Araclar.URI_PAKETOZETI)).getRelationshipsByType(Araclar.RELATION_TYPE_IMZA);
            if (relImza == null || relImza.size() == 0) {
                sonuclar.add(new OzetDogrulamaHatasi("Imza bileşeni yok.", OzetDogrulamaHataKodu.IMZA_BILESENI_YOK));
                return false;
            }
            URI readedImzaUri = relImza.getRelationship(0).getTargetURI();
            boolean bilesenVarMi = false;
            for (int i = 0; i <= ozet.getReference().size() - 1; i++) {
                if (PackagingURIHelper.decodeURI(URI.create(ozet.getReference().get(i).getURI())).compareTo(PackagingURIHelper.decodeURI(readedImzaUri)) == 0) {
                    bilesenVarMi = true;
                    break;
                }
                if (ozet.getReference().get(i).getURI().compareTo(readedImzaUri.toString()) == 0) {
                    bilesenVarMi = true;
                    break;
                }
            }
            if (!bilesenVarMi) {
                sonuclar.add(new OzetDogrulamaHatasi("NihaiOzet'te Imza ozet değeri yok.", OzetDogrulamaHataKodu.NIHAIOZET_TE_IMZA_OZET_DEGERI_YOK));
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
            referansDogrula(ctReference, sonuclar, dagitimIdentifier);
        }
        return sonuclar.size() == 0;
    }
}
