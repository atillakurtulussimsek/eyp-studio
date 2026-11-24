package cbddo.eYazisma.tipler;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import urn.cbddo.eyazisma.schema.xsd.ObjectFactory;
import urn.cbddo.eyazisma.schema.xsd.nihaiOzet.CTNihaiOzet;
import urn.cbddo.eyazisma.schema.xsd.paketOzeti.CTPaketOzeti;
import urn.cbddo.eyazisma.schema.xsd.parafOzeti.CTParafOzeti;
import urn.cbddo.eyazisma.schema.xsd.tipler.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;


/**
 * Sabit tanımlar ve yardımcı metodları içerir.
 */
public class Araclar {
    /**
     * Ustveri bileşeni ilişki türü.
     */
    public final static String RELATION_TYPE_USTVERI = "http://eyazisma.dpt/iliskiler/ustveri";
    /**
     * Ustveri bileşeni ilişki türü.
     */
    public final static String RELATION_TYPE_ORIJINAL_PAKET = "http://eyazisma.dpt/iliskiler/orijinalpaket";
    /**
     * Ustveri bileşeni ilişki türü.
     */
    public final static String RELATION_TYPE_NIHAIUSTVERI = "http://eyazisma.dpt/iliskiler/nihaiustveri";
    /**
     * OrijinalPaket bileşenine ait URI.
     */
    public final static String URI_FORMAT_ORIJINAL_PAKET = "/OrijinalPaket/OrijinalPaket.%s";
    /**
     * BelgeHedef bileşeni ilişki türü.
     */
    public final static String RELATION_TYPE_BELGEHEDEF = "http://eyazisma.dpt/iliskiler/belgehedef";
    /**
     * PaketOzeti bileşeni ilişki türü.
     */
    public final static String RELATION_TYPE_PAKETOZETI = "http://eyazisma.dpt/iliskiler/paketozeti";

    /**
     * ParafOzeti bileşeni ilişki türü.
     */
    public final static String RELATION_TYPE_PARAFOZETI = "http://eyazisma.dpt/iliskiler/parafozeti";
    /**
     * NihaiOzet bileşeni ilişki türü.
     */
    public final static String RELATION_TYPE_NIHAIOZET = "http://eyazisma.dpt/iliskiler/nihaiozet";
    /**
     * UstYazi bileşeni ilişki türü.
     */
    public final static String RELATION_TYPE_USTYAZI = "http://eyazisma.dpt/iliskiler/ustyazi";
    /**
     * Imzali PaketOzeti bileşeni ilişki türü.
     */
    public final static String RELATION_TYPE_IMZA = "http://eyazisma.dpt/iliskiler/imzacades";

    /**
     * Imzali ParafOzeti bileşeni ilişki türü.
     */
    public final static String RELATION_TYPE_PARAFIMZA = "http://eyazisma.dpt/iliskiler/parafimzacades";

    /**
     * Mühürlü NihaiOzet bileşeni ilişki türü.
     */
    public final static String RELATION_TYPE_MUHUR = "http://eyazisma.dpt/iliskiler/muhurcades";
    /**
     * Güncelleme bilgisi bileşeni ilişki türü.
     */
    public final static String RELATION_TYPE_GUNCELLEME_BILGISI = "http://eyazisma.dpt/iliskiler/guncellemebilgisi";
    /**
     * Ek bileşeni ilişki türü.
     */
    public final static String RELATION_TYPE_EK = "http://eyazisma.dpt/iliskiler/ek";
    /**
     * İmzasız ek bileşeni ilişki türü.
     */
    public final static String RELATION_TYPE_IMZASIZEK = "http://eyazisma.dpt/iliskiler/imzasizEk";
    /**
     * Core bileşeni ilişki türü.
     */
    public final static String RELATION_TYPE_CORE = "http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties";
    /**
     * ArşivOzellikleri bileşeni ilişki türü. Bu bileşenin tanımı ileride yapılacaktır.
     */
    public final static String RELATION_TYPE_ARSIVOZELLIKLERI = "http://eyazisma.dpt/iliskiler/arsivozellikleri";
    /**
     * SifreliIcerik bileşeni ilişki türü.
     */
    public final static String RELATION_TYPE_SIFRELIICERIK = "http://eyazisma.dpt/iliskiler/sifreliicerik";
    /**
     * SifreliIcerikBilgisi bileşeni ilişki türü.
     */
    public final static String RELATION_TYPE_SIFRELIICERIKBILGISI = "http://eyazisma.dpt/iliskiler/sifreliicerikbilgisi";
    /**
     * W3C XML Signature Syntax and Processing, SHA256 namespace
     */
    public final static String ALGORITHM_SHA256 = "http://www.w3.org/2001/04/xmlenc#sha256";
    /**
     * W3C XML Signature Syntax and Processing, SHA384 namespace
     */
    public final static String ALGORITHM_SHA384 = "http://www.w3.org/2001/04/xmldsig-more#sha384";
    /**
     * W3C XML Signature Syntax and Processing, SHA512 namespace
     */
    public final static String ALGORITHM_SHA512 = "http://www.w3.org/2001/04/xmlenc#sha512";
    /**
     * Paket Özeti ve Nihai Özete eklenen paket dışı bileşenlere ilişkin tip değeri
     */
    public final static String HARICI_PAKET_BILESENI_REFERANS_TIPI = "http://eyazisma.dpt/bilesen#harici";
    /**
     * Paket Özeti ve Nihai Özete eklenen paket içi bileşenlere ilişkin tip değeri
     */
    public final static String DAHILI_PAKET_BILESENI_REFERANS_TIPI = "http://eyazisma.dpt/bilesen#dahili";

    /**
     * Şifreleme Yöntemi
     */
    public final static String SIFRELEME_YONTEMI = "Elektronik Belgeleri Açık Anahtar Altyapısı Kullanarak Güvenli İşleme Rehberi";
    /**
     * Şifreleme Yöntemi Rehberi URI
     */
    public final static String SIFRELEME_YONTEMI_URI_1 = "http://www.kamusm.gov.tr/dokumanlar/belgeler";
    /**
     * Şifreleme Yöntemi Rehberi URI
     */
    public final static String SIFRELEME_YONTEMI_URI_2 = "http://www.e-yazisma.gov.tr/SitePages/dokumanlar.aspx";
    /**
     * Şifreleme Yöntemi Versiyonu
     */
    public final static String SIFRELEME_YONTEMI_VERSIYONU = "1.4";

    /**
     * e-Yazışma paketi kategorisi
     */
    public final static String RESMIYAZISMA = "RESMIYAZISMA";
    /**
     * Şifreli e-Yazışma paketi kategorisi
     */
    public final static String RESMIYAZISMASIFRELI = "RESMIYAZISMA/SIFRELI";
    /**
     * Güncelleme e-Yazışma paketi kategorisi
     */
    public final static String RESMIYAZISMAGUNCELLEME = "RESMIYAZISMA/GUNCELLEME";
    /**
     * e-Yazışma paketi MIME türü
     */
    public final static String EYAZISMAMIMETURU = "application/eyazisma";

    /***
     * Paket Versiyonu
     */
    public final static String PAKET_VERSIYON = "2.0";
    public final static String PAKET_VERSIYON_2_1 = "2.1";

    /***
     * Paket revizyonu
     */
    public final static String PAKET_REVIZYON = "Java/%s";

    /**
     * PaketOzeti bileşenine ait URI.
     */
    public final static String URI_PAKETOZETI = "/PaketOzeti/PaketOzeti.xml";
    /**
     * ParafOzeti bileşenine ait URI.
     */
    public final static String URI_PARAFOZETI = "/ParafOzeti/ParafOzeti.xml";
    /**
     * NihaiOzet bileşenine ait URI.
     */
    public final static String URI_NIHAIOZET = "/NihaiOzet/NihaiOzet.xml";
    /**
     * Ustveri bileşenine ait URI.
     */
    public final static String URI_USTVERI = "/Ustveri/Ustveri.xml";
    /**
     * NihaiUstveri bileşenine ait URI.
     */
    public final static String URI_NIHAIUSTVERI = "/NihaiUstveri/NihaiUstveri.xml";
    /**
     * BelgeHedef bileşenine ait URI.
     */
    public final static String URI_BELGEHEDEF = "/BelgeHedef/BelgeHedef.xml";
    /**
     * UstYazi bileşenine ait URI formatı.
     */
    public final static String URI_FORMAT_USTYAZI = "/UstYazi/%s";
    /**
     * Ek bileşenine ait URI.
     */
    public final static String URI_ROOT_EK = "Ekler";
    /**
     * İmzasızEk bileşenine ait URI.
     */
    public final static String URI_ROOT_IMZASIZEK = "ImzasizEkler";
    /**
     * Imzali PaketOzeti bileşenine ait URI.
     */
    public final static String URI_IMZA = "/Imzalar/ImzaCades.imz";
    /**
     * Imzali ParafOzeti bileşenine ait URI.
     */
    public final static String URI_PARAFIMZA = "/Paraflar/ParafImzaCades.imz";
    /**
     * Mühürlü NihaiOzet bileşenine ait URI.
     */
    public final static String URI_MUHUR = "/Muhur/MuhurCades.imz";
    public final static String URI_ARSIVOZELLIKLERI = "/Arsiv/ArsivOzellikleri.xml";
    /**
     * SifreliIcerik bileşenine ait URI.
     */
    public final static String URI_FORMAT_SIFRELIICERIK = "/SifreliIcerik/%s";
    /**
     * SifreliIcerikBilgisi bileşenine ait URI.
     */
    public final static String URI_SIFRELIICERIKBILGISI = "/SifreliIcerikBilgisi/SifreliIcerikBilgisi.xml";
    /**
     * GuncellemeBilgisi bileşenine ait URI.
     */
    public final static String URI_GUNCELLEME_BILGISI = "/GuncellemeBilgisi/GuncellemeBilgisi.xml";

    /**
     * Ek bileşeni ilişkisi Id formatı.
     */
    public final static String ID_ROOT_EK = "IdEk_";
    /**
     * İmzasız Ek bileşeni ilişkisi Id formatı.
     */
    public final static String ID_ROOT_IMZASIZEK = "IdImzasizEk_";
    /**
     * İmzalı PaketOzeti bileşeni ilişkisi Id'si.
     */
    public final static String ID_IMZA = "IdImzaCades";
    /**
     * İmzalı ParafOzetu bileşeni ilişkisi Id'si.
     */
    public final static String ID_PARAFIMZA = "IdParafImzaCades";
    /**
     * İmzalı NihaiOzet bileşeni ilişkisi Id'si.
     */
    public final static String ID_MUHUR = "IdMuhurCades";
    //public final static String ID_BELGEIMZA = "IdBelgeImza";
    /**
     * UstYazi bileşeni ilişkisi Id'si.
     */
    public final static String ID_USTYAZI = "IdUstYazi";
    /**
     * Ustveri bileşeni ilişkisi Id'si.
     */
    public final static String ID_USTVERI = "IdUstveri";
    /**
     * NihaiUstveri bileşeni ilişkisi Id'si.
     */
    public final static String ID_NIHAIUSTVERI = "IdNihaiUstveri";
    /**
     * BelgeHedef bileşeni ilişkisi Id'si.
     */
    public final static String ID_BELGEHEDEF = "IdBelgeHedef";
    /**
     * PaketOzeti bileşeni ilişkisi Id'si.
     */
    public final static String ID_PAKETOZETI = "IdPaketOzeti";
    /**
     * ParafOzeti bileşeni ilişkisi Id'si.
     */
    public final static String ID_PARAFOZETI = "IdParafOzeti";
    /**
     * NihaiOzet bileşeni ilişkisi Id'si.
     */
    public final static String ID_NIHAIOZET = "IdNihaiOzet";
    /**
     * SifreliIcerik bileşeni ilişkisi Id'si.
     */
    public final static String ID_SIFRELIICERIK = "IdSifreliIcerik";

    /**
     * Orijinal paket bileşeni ilişkisi Id'si.
     */
    public final static String ID_ORIJINAL_PAKET = "IdOrijinalPaket";
    /**
     * Ek bileşeni ilişkisi Id'si.
     */
    public final static String ID_SIFRELIICERIKBILGISI = "IdSifreliIcerikBilgisi";
    /**
     * Güncelleme bilgisi bileşeni ilişkisi Id'si.
     */
    public final static String ID_GUNCELLEME_BILGISI = "IdGuncellemeBilgisi";
    /**
     * XML Mime türü.
     */
    public final static String MIME_XML = "application/xml";
    /**
     * XML Octet-stream türü.
     */
    public final static String MIME_OCTETSTREAM = "application/octet-stream";

    /**
     * Verilen lokasyondaki dosyanın SHA256 özetini hesaplar.
     *
     * @param dosyaYolu Özeti hesaplanacak dosyanın yolu.
     * @return Özet değeri.
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static byte[] sha256OzetHesapla(String dosyaYolu) throws NoSuchAlgorithmException, IOException {
        return sha256OzetHesapla(new FileInputStream(dosyaYolu));
    }

    /**
     * @param dosya Verilen STREAM'in SHA256 özetini hesaplar.
     * @return Özeti hesaplanacak STREAM.
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static byte[] sha256OzetHesapla(InputStream dosya) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] dataBytes = new byte[1024];
        int nread;
        while ((nread = dosya.read(dataBytes)) != -1) {
            md.update(dataBytes, 0, nread);
        }
        byte[] mdbytes = md.digest();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mdbytes.length; i++) {
            sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return mdbytes;
    }

    /**
     * Verilen lokasyondaki dosyanın SHA512 özetini hesaplar.
     *
     * @param dosyaYolu Özeti hesaplanacak dosyanın yolu.
     * @return Özet değeri.
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static byte[] sha512OzetHesapla(String dosyaYolu) throws NoSuchAlgorithmException, IOException {
        return sha512OzetHesapla(new FileInputStream(dosyaYolu));
    }

    /**
     * Verilen STREAM'in SHA512 özetini hesaplar.
     *
     * @param dosya Özeti hesaplanacak STREAM.
     * @return Özet değeri.
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static byte[] sha512OzetHesapla(InputStream dosya) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] dataBytes = new byte[1024];
        int nread;
        while ((nread = dosya.read(dataBytes)) != -1) {
            md.update(dataBytes, 0, nread);
        }
        byte[] mdbytes = md.digest();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mdbytes.length; i++) {
            sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return mdbytes;
    }


    /**
     * Verilen lokasyondaki dosyanın SHA512 özetini hesaplar.
     *
     * @param dosyaYolu Özeti hesaplanacak dosyanın yolu.
     * @return Özet değeri.
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static byte[] sha384OzetHesapla(String dosyaYolu) throws NoSuchAlgorithmException, IOException {
        return sha384OzetHesapla(new FileInputStream(dosyaYolu));
    }

    /**
     * Verilen STREAM'in SHA384 özetini hesaplar.
     *
     * @param dosya Özeti hesaplanacak STREAM.
     * @return Özet değeri.
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static byte[] sha384OzetHesapla(InputStream dosya) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("SHA-384");
        byte[] dataBytes = new byte[1024];
        int nread;
        while ((nread = dosya.read(dataBytes)) != -1) {
            md.update(dataBytes, 0, nread);
        }
        byte[] mdbytes = md.digest();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mdbytes.length; i++) {
            sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return mdbytes;
    }

    /**
     * Verilen STREAM'in belirtilen algoritma ile özetini hesaplar.
     *
     * @param dosya Özeti hesaplanacak STREAM.
     * @param mod   Özetleme algoritması.
     * @return
     * @throws Exception
     */
    public static byte[] ozetHesapla(InputStream dosya, OzetModu mod) throws Exception {
        switch (mod) {
            case Yok:
                throw new Exception("Özetleme algoritması YOK.");
            case SHA256:
                return sha256OzetHesapla(dosya);
            case SHA512:
                return sha512OzetHesapla(dosya);
            case SHA384:
                return sha384OzetHesapla(dosya);
            default:
                return null;
        }
    }

    /**
     * Verilen CT_Dagitim nesnesini  CT_Hedef nesnesine dönüştürür.
     *
     * @param dagitim
     * @return
     */
    public static CTHedef dagitim2Hedef(CTDagitim dagitim) {
        CTHedef H = new CTHedef();
        if (dagitim.getGercekSahis() != null) {
            H.setGercekSahis(dagitim.getGercekSahis());
            return H;
        }
        if (dagitim.getTuzelSahis() != null) {
            H.setTuzelSahis(dagitim.getTuzelSahis());
            return H;
        }
        if (dagitim.getKurumKurulus() != null) {
            H.setKurumKurulus(dagitim.getKurumKurulus());
            return H;
        }
        return H;
    }

    public static boolean isNullOrWhitespace(String s) {
        return s == null || isWhitespace(s);
    }

    private static boolean isWhitespace(String s) {
        int length = s.length();
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                if (!Character.isWhitespace(s.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Verilen {@link CTGercekSahis} nesnesindeki bilgileri kullanarak gerçek şahıs adı oluşturur.
     *
     * @param olusturan
     * @return
     */
    private static String gercekSahisAdiOlustur(CTGercekSahis olusturan) {
        String sonuc = "";
        String tcKimlikNo = olusturan.getTCKN();
        String gorev = "";
        String ad = "";
        if (olusturan.getGorev() != null && olusturan.getGorev().getValue().length() != 0) {
            gorev = olusturan.getGorev().getValue();
        }
        if (olusturan.getKisi() != null) {
            if (olusturan.getKisi().getOnEk() != null && olusturan.getKisi().getOnEk().getValue().length() != 0) {
                ad += olusturan.getKisi().getOnEk().getValue();
            }
            if (olusturan.getKisi().getUnvan() != null && olusturan.getKisi().getUnvan().getValue().length() != 0) {
                if (ad.length() != 0) {
                    ad += " ";
                }
                ad += olusturan.getKisi().getUnvan().getValue();
            }
            if (olusturan.getKisi().getIlkAdi() != null && olusturan.getKisi().getIlkAdi().getValue().length() != 0) {
                if (ad.length() != 0) {
                    ad += " ";
                }
                ad += olusturan.getKisi().getIlkAdi().getValue();
            }
            if (olusturan.getKisi().getIkinciAdi() != null && olusturan.getKisi().getIkinciAdi().getValue().length() != 0) {
                if (ad.length() != 0) {
                    ad += " ";
                }
                ad += olusturan.getKisi().getIkinciAdi().getValue();
            }
            if (olusturan.getKisi().getSoyadi() != null && olusturan.getKisi().getSoyadi().getValue().length() != 0) {
                if (ad.length() != 0) {
                    ad += " ";
                }
                ad += olusturan.getKisi().getSoyadi().getValue();
            }
        }
        if (tcKimlikNo.length() != 0) {
            sonuc += tcKimlikNo + ",";
        }
        if (gorev.length() != 0) {
            sonuc += gorev + ",";
        }
        if (ad.length() != 0) {
            sonuc += ad + ",";
        }
        if (sonuc.length() != 0) {
            sonuc = sonuc.substring(0, sonuc.length() - 2);
        }
        return sonuc;
    }

    /**
     * Verilen {@link CTTuzelSahis} nesnesindeki bilgileri kullanarak tüzel şahıs adı oluşturur.
     *
     * @param olusturan
     * @return
     */
    private static String tuzelSahisAdiOlustur(CTTuzelSahis olusturan) {
        if (olusturan.getAdi() != null && olusturan.getAdi().getValue().length() != 0)
            return olusturan.getAdi().getValue();

        if (olusturan.getId() != null && olusturan.getId().getValue().length() != 0) {
            String id = "";
            if (olusturan.getId() != null && olusturan.getId().getSchemeID().length() != 0) {
                id += olusturan.getId().getSchemeID();
            }
            if (olusturan.getId() != null && olusturan.getId().getValue().length() != 0) {
                if (id.length() != 0) {
                    id += ":";
                }
                id += olusturan.getId().getValue();
            }
            return id;
        }
        return "";
    }

    /**
     * Verilen {@link CTKurumKurulus} nesnesindeki bilgileri kullanarak kurum/kuruluş adı oluşturur.
     *
     * @param olusturan
     * @return
     */
    private static String kurumKurulusAdiOlustur(CTKurumKurulus olusturan) {
        if (olusturan.getAdi() != null && olusturan.getAdi().getValue().length() != 0) {
            if (olusturan.getKKK() != null && !olusturan.getKKK().trim().isEmpty()) {
                return String.format("%s/%s", olusturan.getAdi().getValue(), olusturan.getKKK());
            }
            return olusturan.getAdi().getValue();
        }
        return olusturan.getKKK();
    }

    /**
     * Verilen {@link CTOlusturan} nesnesindeki bilgileri kullanarak oluşturan adı oluşturur.
     *
     * @param olusturan
     * @return
     */
    public static String olusturanAdiOlustur(CTOlusturan olusturan) {
        if (olusturan.getKurumKurulus() != null) {
            return Araclar.kurumKurulusAdiOlustur(olusturan.getKurumKurulus());
        }
        if (olusturan.getTuzelSahis() != null) {
            return Araclar.tuzelSahisAdiOlustur(olusturan.getTuzelSahis());
        }
        if (olusturan.getGercekSahis() != null) {
            return Araclar.gercekSahisAdiOlustur(olusturan.getGercekSahis());
        }
        return "";
    }

    /**
     * Verilen sıralı byte nesnesini deserialize ederek {@link CTPaketOzeti}  nesnesi oluşturur.
     *
     * @param paketOzetiVerisi Deserialize edilecek sıralı byte.
     * @return Oluşturulan {@link CTPaketOzeti} nesnesi
     * @throws JAXBException
     */

    public static CTPaketOzeti paketOzetiAl(byte[] paketOzetiVerisi) throws JAXBException {
        ByteArrayInputStream inputStreamPaketOzeti = new ByteArrayInputStream(paketOzetiVerisi);
        JAXBContext contextPaketOzeti = JAXBContext.newInstance(ObjectFactory.class);
        Unmarshaller unmarshallerPaketOzeti = contextPaketOzeti.createUnmarshaller();
        CTPaketOzeti okunanPaketOzeti = (CTPaketOzeti) JAXBIntrospector.getValue(unmarshallerPaketOzeti.unmarshal(inputStreamPaketOzeti));
        return okunanPaketOzeti;
    }

    /**
     * Verilen sıralı STREAM'i deserialize ederek {@link CTPaketOzeti} nesnesi oluşturur.
     *
     * @param paketOzetiStream Deserialize edilecek STREAM.
     * @return Oluşturulan {@link CTPaketOzeti} nesnesi.
     * @throws JAXBException
     */
    public static CTPaketOzeti paketOzetiAl(ByteArrayInputStream paketOzetiStream) throws JAXBException {
        JAXBContext contextPaketOzeti = JAXBContext.newInstance(ObjectFactory.class);
        Unmarshaller unmarshallerPaketOzeti = contextPaketOzeti.createUnmarshaller();
        CTPaketOzeti okunanPaketOzeti = (CTPaketOzeti) JAXBIntrospector.getValue(unmarshallerPaketOzeti.unmarshal(paketOzetiStream));
        return okunanPaketOzeti;
    }

    /**
     * Verilen sıralı STREAM'i deserialize ederek {@link CTNihaiOzet} nesnesi oluşturur.
     *
     * @param nihaiOzetVerisi Deserialize edilecek byte array.
     * @return Oluşturulan {@link CTNihaiOzet} nesnesi
     * @throws JAXBException
     */
    public static CTNihaiOzet nihaiOzetAl(byte[] nihaiOzetVerisi) throws JAXBException {
        ByteArrayInputStream inputStreamNihaiOzet = new ByteArrayInputStream(nihaiOzetVerisi);
        JAXBContext contextNihaiOzet = JAXBContext.newInstance(ObjectFactory.class);
        Unmarshaller unmarshallerNihaiOzet = contextNihaiOzet.createUnmarshaller();
        CTNihaiOzet okunanNihaiOzet = (CTNihaiOzet) JAXBIntrospector.getValue(unmarshallerNihaiOzet.unmarshal(inputStreamNihaiOzet));
        return okunanNihaiOzet;
    }

    /**
     * Verilen sıralı STREAM'i deserialize ederek {@link CTNihaiOzet} nesnesi oluşturur.
     *
     * @param nihaiOzetStream Deserialize edilecek STREAM.
     * @return Oluşturulan {@link CTNihaiOzet} nesnesi
     * @throws JAXBException
     */
    public static CTNihaiOzet nihaiOzetAl(ByteArrayInputStream nihaiOzetStream) throws JAXBException {
        JAXBContext contextNihaiOzet = JAXBContext.newInstance(ObjectFactory.class);
        Unmarshaller unmarshallerNihaiOzet = contextNihaiOzet.createUnmarshaller();
        CTNihaiOzet okunanNihaiOzet = (CTNihaiOzet) JAXBIntrospector.getValue(unmarshallerNihaiOzet.unmarshal(nihaiOzetStream));
        return okunanNihaiOzet;
    }

    /**
     * Verilen sıralı STREAM'i deserialize ederek {@link CTParafOzeti} nesnesi oluşturur.
     *
     * @param parafOzetiVerisi Deserialize edilecek byte array.
     * @return Oluşturulan {@link CTParafOzeti} nesnesi
     * @throws JAXBException
     */
    public static CTParafOzeti parafOzetiAl(byte[] parafOzetiVerisi) throws JAXBException {
        ByteArrayInputStream inputStreamNihaiOzet = new ByteArrayInputStream(parafOzetiVerisi);
        JAXBContext contextNihaiOzet = JAXBContext.newInstance(ObjectFactory.class);
        Unmarshaller unmarshallerNihaiOzet = contextNihaiOzet.createUnmarshaller();
        return (CTParafOzeti) JAXBIntrospector.getValue(unmarshallerNihaiOzet.unmarshal(inputStreamNihaiOzet));
    }

    /**
     * Verilen sıralı STREAM'i deserialize ederek {@link CTParafOzeti} nesnesi oluşturur.
     *
     * @param parafOzetiStream Deserialize edilecek STREAM.
     * @return Oluşturulan {@link CTParafOzeti} nesnesi
     * @throws JAXBException
     */
    public static CTParafOzeti parafOzetiAl(ByteArrayInputStream parafOzetiStream) throws JAXBException {
        JAXBContext contextNihaiOzet = JAXBContext.newInstance(ObjectFactory.class);
        Unmarshaller unmarshallerNihaiOzet = contextNihaiOzet.createUnmarshaller();
        return (CTParafOzeti) JAXBIntrospector.getValue(unmarshallerNihaiOzet.unmarshal(parafOzetiStream));
    }

    /**
     * Verilen STREAM'de bulunan paketin şifreli bir paket olup olmadığı bilgisini döner.
     *
     * @param stream Paketin bulunduğu STREAM.
     * @return Paket şifreli ise true, aksi halde false döner.
     * @throws Exception
     * @throws IOException
     */
    public static boolean paketSifreliMi(InputStream stream) throws Exception {
        OPCPackage _package = OPCPackage.open(stream);
        if (!_package.getPackageProperties().getCategoryProperty().isPresent()) {
            throw new Exception("Geçersiz e-yazışma paketi.");
        }
        if (_package.getPackageProperties().getCategoryProperty().get().equals(Araclar.RESMIYAZISMASIFRELI)) {
            return true;
        }
        if (_package.getPackageProperties().getCategoryProperty().get().equals(Araclar.RESMIYAZISMA)) {
            return false;
        }
        throw new Exception("Geçersiz e-yazışma paketi.");
    }

    /**
     * Bir STREAM'deki veriyi diğerine kopyalar.
     *
     * @param source Kaynak STREAM.
     * @param target Hedef STREAM.
     * @throws Exception
     */
    public static void copyStream(InputStream source, OutputStream target) throws Exception {
        if (source == null)
            throw new Exception("source");
        int bufSize = 0x1000;
        byte[] buf = new byte[bufSize];
        int bytesRead;
        while ((bytesRead = source.read(buf, 0, bufSize)) > 0)
            target.write(buf, 0, bytesRead);
    }

    /**
     * Verilen STREAM'i Byte Array'e dönüştürür.
     *
     * @param is
     * @return byte[]
     * @throws IOException
     */
    public static byte[] inputStreamToBytes(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(b)) != -1) {
            bos.write(b, 0, bytesRead);
        }
        return bos.toByteArray();
    }

    /**
     * Türkçe karakterleri ve geçersiz karakterleri temizler
     *
     * @param s
     * @return
     */
    static String encodePath(String s) {
        return s.replace('ş', 's')
                .replace('Ş', 'S')
                .replace('ö', 'o')
                .replace('Ö', 'O')
                .replace('ç', 'c')
                .replace('Ç', 'C')
                .replace('i', 'i')
                .replace('İ', 'I')
                .replace('ı', 'i')
                .replace('I', 'I')
                .replace('ğ', 'g')
                .replace('Ğ', 'G')
                .replace('ü', 'u')
                .replace('Ü', 'U')
                .replaceAll("[^\\x20-\\x7e]", "")
                .replaceAll("\\s+", "");
    }

    public static String ozetModuToString(OzetModu ozetModu) {
        switch (ozetModu) {
            case SHA256:
                return ALGORITHM_SHA256;
            case SHA512:
                return ALGORITHM_SHA512;
            case SHA384:
                return ALGORITHM_SHA384;
            default:
                return "";
        }
    }

    /**
     * Paketi açar ve paket özelliklerinde bulunan versiyon numarasının ilk bölümünü getirir
     * 1.3 olan paket numarasını 1 olarak getirir.
     * 2.0 olan paket numarasını 2 olarak getirir.
     *
     * @param stream
     * @return Optional<Integer>
     * @throws Exception
     */
    public static Optional<Integer> PaketVersiyonuGetir(InputStream stream) throws Exception {
        OPCPackage paket = OPCPackage.open(stream);
        Optional<String> paketVersionu = paket.getPackageProperties().getVersionProperty();
        if (!paketVersionu.isPresent())
            return Optional.empty();

        if (paketVersionu.get().isEmpty())
            return Optional.empty();

        String[] versiyonBilgisi = paketVersionu.get().split("\\.");
        if (versiyonBilgisi.length != 2) {
            //Versiyon bilgisi alanı 1.3, 2.0 ,2.1 şeklinde olmaktadır.
            return Optional.empty();
        }
        return Optional.of(Integer.parseInt(versiyonBilgisi[0]));
    }

    public static Optional<PaketTipi> paketTipiGetir(InputStream stream) throws IOException, InvalidFormatException {
        OPCPackage paket = OPCPackage.open(stream);
        Optional<String> paketKategorisi = paket.getPackageProperties().getCategoryProperty();
        if (!paketKategorisi.isPresent())
            return Optional.empty();

        if (paketKategorisi.get().isEmpty())
            return Optional.empty();

        switch (paketKategorisi.get()) {
            case RESMIYAZISMA:
                return Optional.of(PaketTipi.NormalPaket);
            case RESMIYAZISMASIFRELI:
                return Optional.of(PaketTipi.SifreliPaket);
            case RESMIYAZISMAGUNCELLEME:
                return Optional.of(PaketTipi.GuncellemePaketi);
            default:
                return Optional.empty();
        }
    }

    public static Optional<PaketTipi> paketTipiGetir(String dosyaYolu) throws IOException, InvalidFormatException {
        if (!(new File(dosyaYolu)).exists()) {
            return Optional.empty();
        }
        return paketTipiGetir(new FileInputStream(dosyaYolu));
    }
}
