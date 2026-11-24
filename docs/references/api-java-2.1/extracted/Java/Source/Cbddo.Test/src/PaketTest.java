import cbddo.eYazisma.tipler.*;
import urn.cbddo.eyazisma.schema.xsd.codelist.IdentifierType;
import urn.cbddo.eyazisma.schema.xsd.codelist.NameType;
import urn.cbddo.eyazisma.schema.xsd.codelist.TextType;
import urn.cbddo.eyazisma.schema.xsd.guncellemeturu.STGuncellemeTuru;
import urn.cbddo.eyazisma.schema.xsd.koddagitimturu.STKodDagitimTuru;
import urn.cbddo.eyazisma.schema.xsd.kodekturu.STKodEkTuru;
import urn.cbddo.eyazisma.schema.xsd.kodguvenlik.STKodGuvenlikKodu;
import urn.cbddo.eyazisma.schema.xsd.kodivedilik.STKodIvedilik;
import urn.cbddo.eyazisma.schema.xsd.nihaiOzet.CTNihaiOzet;
import urn.cbddo.eyazisma.schema.xsd.nihaiUstveri.CTNihaiUstveri;
import urn.cbddo.eyazisma.schema.xsd.paketOzeti.CTDigestItem;
import urn.cbddo.eyazisma.schema.xsd.paketOzeti.CTPaketOzeti;
import urn.cbddo.eyazisma.schema.xsd.paketOzeti.CTReference;
import urn.cbddo.eyazisma.schema.xsd.parafOzeti.CTParafOzeti;
import urn.cbddo.eyazisma.schema.xsd.tipler.*;
import urn.cbddo.eyazisma.schema.xsd.ustveri.CTUstveri;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PaketTest {
    private static Scanner scanner = new Scanner(System.in);
    static String userDir = System.getProperty("user.dir");
    static String calismaKlasoru = userDir + "/dosyalar/";
    static String ustYaziDosyasi = calismaKlasoru + "ustyazi.pdf";
    static String ekDosyasi1 = calismaKlasoru + "dosya1.pdf";
    static String ekDosyasi2 = calismaKlasoru + "dosya_imzasiz.docm";
    static String ekDosyasi3 = calismaKlasoru + "dosya3.docx";
    static String guncellemePaketiDosyasiYolu;
    static String paketYolu;
    static String sifreliPaketYolu;
    private static Paket _paket;
    private static String dosyaAdi;
    private List<PaketDurumu> _paketDurumlari;
    private PaketDurumu _paketDurumu;


    public static void main(String[] args) throws Exception {
        PaketTest test = new PaketTest();
        test.paketDurumlariOlustur();

        for (PaketDurumu paketDurumu : test._paketDurumlari) {
            if (paketDurumu.getPaketDurumuEnum() == PaketDurumuEnum.Yok) {
                test._paketDurumu = paketDurumu;
                break;
            }
        }
        test.menuOlustur();
    }

    private void paketDurumlariOlustur() {
        _paketDurumlari = new ArrayList<>();

        PaketDurumu durum = new PaketDurumu();
        durum.setPaketDurumuEnum(PaketDurumuEnum.Yok);
        List<AksiyonEnum> aksiyonlar1 = new ArrayList<>();
        aksiyonlar1.add(AksiyonEnum.PaketOlustur);
        aksiyonlar1.add(AksiyonEnum.Ac);
        durum.setAksiyonlar(aksiyonlar1);
        _paketDurumlari.add(durum);

        durum = new PaketDurumu();
        durum.setPaketDurumuEnum(PaketDurumuEnum.YeniOlusturuldu);
        List<AksiyonEnum> aksiyonlar2 = new ArrayList<>();
        aksiyonlar2.add(AksiyonEnum.Kapat);
        aksiyonlar2.add(AksiyonEnum.ParafEkle);
        aksiyonlar2.add(AksiyonEnum.ImzaEkle);
        durum.setAksiyonlar(aksiyonlar2);
        _paketDurumlari.add(durum);

        durum = new PaketDurumu();
        durum.setPaketDurumuEnum(PaketDurumuEnum.Kapali);
        List<AksiyonEnum> aksiyonlar3 = new ArrayList<>();
        aksiyonlar3.add(AksiyonEnum.AcGuncellemeModunda);
        aksiyonlar3.add(AksiyonEnum.AcOkumaModunda);
        aksiyonlar3.add(AksiyonEnum.Sifrele);
        aksiyonlar3.add(AksiyonEnum.UstveriGuncelleme);
        durum.setAksiyonlar(aksiyonlar3);
        _paketDurumlari.add(durum);

        durum = new PaketDurumu();
        durum.setPaketDurumuEnum(PaketDurumuEnum.ParafEklendi);
        List<AksiyonEnum> aksiyonlar4 = new ArrayList<>();
        aksiyonlar4.add(AksiyonEnum.ParafEkle);
        aksiyonlar4.add(AksiyonEnum.ImzaEkle);
        aksiyonlar4.add(AksiyonEnum.Kapat);
        durum.setAksiyonlar(aksiyonlar4);
        _paketDurumlari.add(durum);

        durum = new PaketDurumu();
        durum.setPaketDurumuEnum(PaketDurumuEnum.ImzaEklendi);
        List<AksiyonEnum> aksiyonlar5 = new ArrayList<>();
        aksiyonlar5.add(AksiyonEnum.ImzaEkle);
        aksiyonlar5.add(AksiyonEnum.MuhurEkle);
        aksiyonlar5.add(AksiyonEnum.Kapat);
        durum.setAksiyonlar(aksiyonlar5);
        _paketDurumlari.add(durum);

        durum = new PaketDurumu();
        durum.setPaketDurumuEnum(PaketDurumuEnum.MuhurEklendi);
        List<AksiyonEnum> aksiyonlar6 = new ArrayList<>();
        aksiyonlar6.add(AksiyonEnum.Kapat);
        aksiyonlar6.add(AksiyonEnum.ParafImzaCikar);
        durum.setAksiyonlar(aksiyonlar6);
        _paketDurumlari.add(durum);

        durum = new PaketDurumu();
        durum.setPaketDurumuEnum(PaketDurumuEnum.OkumaModundaAcildi);
        List<AksiyonEnum> aksiyonlar7 = new ArrayList<>();
        aksiyonlar7.add(AksiyonEnum.Kapat);
        durum.setAksiyonlar(aksiyonlar7);
        _paketDurumlari.add(durum);

    }

    private void paketDurumuBelirle(PaketDurumuEnum paketDurumuEnum) {
        for (PaketDurumu paketDurumu : _paketDurumlari) {
            if (paketDurumu.getPaketDurumuEnum() == paketDurumuEnum) {
                _paketDurumu = paketDurumu;
                break;
            }
        }
    }

    private void menuOlustur() throws Exception {

        int secim = 0;
        do {
            ConsoleHelper.println("");

            List<Integer> aksiyonNumaralari = new ArrayList<>();
            if (_paketDurumu != null && _paketDurumu.getAksiyonlar() != null
                    && _paketDurumu.getAksiyonlar().size() > 0
                    && _paketDurumu.getAksiyonlar().contains(AksiyonEnum.PaketOlustur)) {
                ConsoleHelper.println("[1] Paket Oluştur");
                aksiyonNumaralari.add(1);
            }
            if (_paketDurumu != null && _paketDurumu.getAksiyonlar() != null
                    && _paketDurumu.getAksiyonlar().size() > 0
                    && _paketDurumu.getAksiyonlar().contains(AksiyonEnum.ParafEkle)) {
                ConsoleHelper.println("[2] Pakete Elektronik İmzalı Paraf Ekle");
                aksiyonNumaralari.add(2);
            }
            if (_paketDurumu != null && _paketDurumu.getAksiyonlar() != null
                    && _paketDurumu.getAksiyonlar().size() > 0
                    && _paketDurumu.getAksiyonlar().contains(AksiyonEnum.ImzaEkle)) {
                ConsoleHelper.println("[3] Pakete Elektronik İmza Ekle");
                aksiyonNumaralari.add(3);
            }
            if (_paketDurumu != null && _paketDurumu.getAksiyonlar() != null
                    && _paketDurumu.getAksiyonlar().size() > 0
                    && _paketDurumu.getAksiyonlar().contains(AksiyonEnum.MuhurEkle)) {
                ConsoleHelper.println("[4] Pakete Mühür Ekle");
                aksiyonNumaralari.add(4);
            }
            if (_paketDurumu != null && _paketDurumu.getAksiyonlar() != null
                    && _paketDurumu.getAksiyonlar().size() > 0
                    && _paketDurumu.getAksiyonlar().contains(AksiyonEnum.Kapat)) {
                ConsoleHelper.println("[5] Paketi Kapat");
                aksiyonNumaralari.add(5);
            }
            if (_paketDurumu != null && _paketDurumu.getAksiyonlar() != null
                    && _paketDurumu.getAksiyonlar().size() > 0
                    && _paketDurumu.getAksiyonlar().contains(AksiyonEnum.AcOkumaModunda)) {
                ConsoleHelper.println("[6] Paket Görüntüle");
                aksiyonNumaralari.add(6);
            }
            if (_paketDurumu != null && _paketDurumu.getAksiyonlar() != null
                    && _paketDurumu.getAksiyonlar().size() > 0
                    && _paketDurumu.getAksiyonlar().contains(AksiyonEnum.AcOkumaModunda)) {
                ConsoleHelper.println("[7] Paketi Okuma Modunda Aç");
                aksiyonNumaralari.add(7);
            }
            if (_paketDurumu != null && _paketDurumu.getAksiyonlar() != null
                    && _paketDurumu.getAksiyonlar().size() > 0
                    && _paketDurumu.getAksiyonlar().contains(AksiyonEnum.AcGuncellemeModunda)) {
                ConsoleHelper.println("[8] Paketi Güncelleme Modunda Aç");
                aksiyonNumaralari.add(8);
            }

            if (_paketDurumu != null && _paketDurumu.getAksiyonlar() != null
                    && _paketDurumu.getAksiyonlar().size() > 0
                    && _paketDurumu.getAksiyonlar().contains(AksiyonEnum.Sifrele)) {
                ConsoleHelper.println("[9] Paketi Şifrele");
                aksiyonNumaralari.add(9);
            }
            if (_paketDurumu != null && _paketDurumu.getAksiyonlar() != null
                    && _paketDurumu.getAksiyonlar().size() > 0
                    && _paketDurumu.getAksiyonlar().contains(AksiyonEnum.AcOkumaModunda)) {
                ConsoleHelper.println("[10] Paket İçeriğini Çıkart");
                aksiyonNumaralari.add(10);
            }
            if (_paketDurumu != null && _paketDurumu.getAksiyonlar() != null
                    && _paketDurumu.getAksiyonlar().size() > 0
                    && _paketDurumu.getAksiyonlar().contains(AksiyonEnum.UstveriGuncelleme)) {
                ConsoleHelper.println("[11] Güncelleme Paketi Oluştur");
                aksiyonNumaralari.add(11);
            }

            ConsoleHelper.println("[12] Paket Aç");
            ConsoleHelper.println("[13] Çıkış");
            aksiyonNumaralari.add(12);
            aksiyonNumaralari.add(13);

            ConsoleHelper.println("Lütfen bir seçim yapınız.");
            ConsoleHelper.println("");
            try {
                String input = scanner.nextLine();
                if (input != null && !input.equals("")) {
                    secim = Integer.parseInt(input);
                    if (!aksiyonNumaralari.contains(secim)) {
                        continue;
                    }
                }

            } catch (Exception ex) {
                secim = 0;
                ConsoleHelper.println("Geçersiz bir seçim yapıldı.", ConsoleHelper.Colors.ANSI_RED);
            }

            switch (secim) {
                case 1:
                    dosyaAdi = UUID.randomUUID().toString().toUpperCase();
                    ConsoleHelper.println("Paket oluşturuluyor.", ConsoleHelper.Colors.ANSI_GREEN);
                    paketOlustur();
                    break;
                case 2:
                    ConsoleHelper.println("Paraf ekleniyor.", ConsoleHelper.Colors.ANSI_GREEN);
                    parafEkle();
                    break;
                case 3:
                    ConsoleHelper.println("İmza ekleniyor.", ConsoleHelper.Colors.ANSI_GREEN);
                    imzaEkle();
                    break;
                case 4:
                    ConsoleHelper.println("Mühür ekleniyor..", ConsoleHelper.Colors.ANSI_GREEN);
                    muhurEkle();
                    break;
                case 5:
                    _paket.kapat();
                    ConsoleHelper.println("Paket kapatıldı.");
                    if (!Araclar.isNullOrWhitespace(paketYolu)) {
                        ConsoleHelper.println(String.format("Paket oluşturuldu. Dosya yolu %s", dosyaAdiGetir(paketYolu)));
                    }
                    paketDurumuBelirle(PaketDurumuEnum.Kapali);
                    break;
                case 6:
                    ConsoleHelper.println("Paket Görüntüleniyor", ConsoleHelper.Colors.ANSI_PURPLE);
                    String[] commands = {"cmd", "/c", "start", "\"Title\"", paketYolu};
                    Process p = Runtime.getRuntime().exec(commands);
                    p.waitFor();
                    break;
                case 7:
                    paketAcOkumaModu(paketYolu);
                    break;
                case 8:
                    paketiGuncellemeModundaAc(paketYolu);
                    break;
                case 9:
                    paketiSifrele();
                    break;
                case 10:
                    paketIceriginiCikart(paketYolu);
                    break;
                case 11:
                    ustveriGuncellemePaketiOlustur();
                    break;
                case 12:
                    paketAc();
                    break;
                case 13:
                    System.exit(0);
                    break;
            }
        } while (secim != 10);
    }

    private void paketAc() throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("E-Yazışma Paketi Dosya Yolu: ");
        String dosyaYolu = scanner.next();
        if (Araclar.isNullOrWhitespace(dosyaYolu)) {
            System.out.println("Dosya yolu boş olamaz");
            return;
        }
        paketAcOkumaModu(dosyaYolu);
    }

    private void paketiGuncellemeModundaAc(String dosyaYolu) throws Exception {
        _paket = Paket.ac(dosyaYolu, PaketModu.Guncelle);
        ConsoleHelper.println("Paket güncelleme amaçlı açıldı.");
        if (_paket.muhurVarMi()) {
            paketDurumuBelirle(PaketDurumuEnum.MuhurEklendi);
        } else if (_paket.imzaVarMi()) {
            paketDurumuBelirle(PaketDurumuEnum.ImzaEklendi);
        } else if (_paket.parafImzaVarMi()) {
            paketDurumuBelirle(PaketDurumuEnum.ParafEklendi);
        } else {
            paketDurumuBelirle(PaketDurumuEnum.YeniOlusturuldu);
        }
    }

    private void paketIceriginiCikart(String dosyaYolu) throws Exception {
        Paket paket = Paket.ac(dosyaYolu, PaketModu.Ac);

        List<OzetDogrulamaHatasi> list = new ArrayList<>();
        List<OzetDogrulamaHatasi> nihaiOzetList = new ArrayList<>();
        List<OzetDogrulamaHatasi> parafOzetiList = new ArrayList<>();

        paket.paketOzetiDogrula(Araclar.paketOzetiAl(Araclar.inputStreamToBytes(paket.paketOzetiAl())), list);
        paket.nihaiOzetDogrula(Araclar.nihaiOzetAl(Araclar.inputStreamToBytes(paket.nihaiOzetAl())), nihaiOzetList);
        paket.parafOzetiDogrula(Araclar.parafOzetiAl(Araclar.inputStreamToBytes(paket.parafOzetiAl())), parafOzetiList);

        ConsoleHelper.println("Paket okuma modunda açıldı", ConsoleHelper.Colors.ANSI_GREEN);

        if (!coreBileseniDogrula(dosyaYolu)) {
            ConsoleHelper.println("Core bileşeni doğrulanamadı.", ConsoleHelper.Colors.ANSI_RED);
        }
        if (list.size() > 0) {
            for (OzetDogrulamaHatasi ozetDogrulamaHatasi : list) {
                ConsoleHelper.println(String.format("%s - %s", ozetDogrulamaHatasi.getHataKodu(), ozetDogrulamaHatasi.getHata()), ConsoleHelper.Colors.ANSI_RED);
            }
        }

        if (nihaiOzetList.size() > 0) {
            for (OzetDogrulamaHatasi ozetDogrulamaHatasi : nihaiOzetList) {
                ConsoleHelper.println(String.format("%s - %s", ozetDogrulamaHatasi.getHataKodu(), ozetDogrulamaHatasi.getHata()), ConsoleHelper.Colors.ANSI_RED);
            }
        }

        if (parafOzetiList.size() > 0) {
            for (OzetDogrulamaHatasi ozetDogrulamaHatasi : parafOzetiList) {
                ConsoleHelper.println(String.format("%s - %s", ozetDogrulamaHatasi.getHataKodu(), ozetDogrulamaHatasi.getHata()), ConsoleHelper.Colors.ANSI_RED);
            }
        }

        String klasor = calismaKlasoru + "\\paketler\\" + new File(dosyaYolu).getName().replaceFirst("[.][^.]+$", "");
        if (!new File(klasor).exists())
            new File(klasor).mkdir();

        if (paket.getUstveri().ekleriAl() != null && paket.getUstveri().ekleriAl().size() > 0) {
            for (int i = 0; i < paket.getUstveri().ekleriAl().size(); i++) {
                if (paket.getUstveri().ekleriAl().get(i).getTur() == STKodEkTuru.DED) {
                    InputStream a3 = paket.ekAl(UUID.fromString(paket.getUstveri().ekleriAl().get(i).getId().getValue()));
                    if (a3 != null) {
                        FileOutputStream o = new FileOutputStream(klasor + "\\" + paket.getUstveri().ekleriAl().get(i).getDosyaAdi());
                        Araclar.copyStream(a3, o);
                        o.close();
                    }
                }
            }
        }

        //Nihai Özet
        NihaiOzet nihaiOzet = paket.getNihaiOzet();
        List<CTReference> nihaiOzetOzetleri = paket.getNihaiOzet().ozetleriAl();
        CTNihaiOzet ctNihaiOzet = paket.getNihaiOzet().getNihaiOzet();
        InputStream inputStreamNihaiOzet = paket.nihaiOzetAl();

        //Paket Özeti
        PaketOzeti paketOzeti = paket.getPaketOzeti();
        List<CTReference> paketOzetiOzetleri = paket.getPaketOzeti().ozetleriAl();
        CTPaketOzeti ctPaketOzeti = paket.getPaketOzeti().getPaketOzeti();
        InputStream inputStreamPaketOzeti = paket.paketOzetiAl();

        //Paraf Özeti
        ParafOzeti parafOzeti = paket.getParafOzeti();
        List<CTReference> parafOzetiOzetleri = paket.getParafOzeti().ozetleriAl();
        CTParafOzeti ctParafOzeti = paket.getParafOzeti().getParafOzeti();
        InputStream inputStreamParafOzeti = paket.parafOzetiAl();


        //Üstveri
        Ustveri ustveri = paket.getUstveri();
        CTUstveri ctUstveri = paket.getUstveri().getCTUstveri();
        List<CTDagitim> dagitimlar = paket.getUstveri().dagitimlariAl();
        CTOlusturan olusturan = paket.getUstveri().olusturanAl();
        String belgeId = paket.getUstveri().belgeIdAl();
        String dil = paket.getUstveri().dilAl();
        String dosyaAdi = paket.getUstveri().dosyaAdiAl();
        Calendar guvenlikGecerlilikTarihi = paket.getUstveri().guvenlikGecerlilikTarihiAl();
        STKodGuvenlikKodu guvenlikKodu = paket.getUstveri().guvenlikKoduAl();
        List<CTIlgi> ilgiler = paket.getUstveri().ilgileriAl();
        List<CTIlgili> ilgililer = paket.getUstveri().ilgilileriAl();
        TextType konu = paket.getUstveri().konuAl();
        String mimeTuru = paket.getUstveri().mimeTuruAl();
        IdentifierType ozId = paket.getUstveri().ozIdAl();
        List<CTEk> ekler = paket.getUstveri().ekleriAl();
        List<CTHEYSK> heysKodlari = paket.getUstveri().heysKodlariniAl();

        //Nihai üstveri
        CTNihaiUstveri nihaiUstveri = paket.getNihaiUstveri().getNihaiUstveri();
        String belgeNo = paket.getNihaiUstveri().belgeNoAl();
        List<CTImza> nihaiUstveriImzalar = paket.getNihaiUstveri().imzalariAl();
        Calendar tarih = paket.getNihaiUstveri().tarihAl();
        CTBelgeImza belgeImzalar = paket.getNihaiUstveri().getNihaiUstveri().getBelgeImzalar();


        OzetModu varsayilanOzetModu = paket.getVarsayilanOzetModu();

        ConsoleHelper.println(paket.paketOzetiVarMi() ? "Paket Ozeti Var" : "Paket Ozeti Yok");
        ConsoleHelper.println(paket.imzaVarMi() ? "İmza Var" : "İmza Yok");
        ConsoleHelper.println(paket.parafImzaVarMi() ? "Paraf İmza Var" : "Paraf İmza Yok");
        ConsoleHelper.println(paket.muhurVarMi() ? "Muhur Var" : "Muhur Yok");
        ConsoleHelper.println(paket.nihaiOzetVarMi() ? "Nihai Ozet Var" : "Nihai Ozet Yok");
        ConsoleHelper.println(paket.nihaiUstveriVarMi() ? "Nihai Ustveri Var" : "Nihai Ustveri Yok");
        ConsoleHelper.println(paket.ustveriVarMi() ? "Üstveri Var" : "Üstveri Yok");
        ConsoleHelper.println(paket.ustYaziVarMi() ? "Üstyazı Var" : "Üstyazı Yok");


        paket.ekleriKontrolEt();
        paket.ilgileriKontrolEt();


        Araclar.copyStream(paket.ustYaziAl(), new FileOutputStream(klasor + "\\" + "ustyazi.pdf"));
        Araclar.copyStream(paket.ustveriAl(), new FileOutputStream(klasor + "\\" + "UstVeri.xml"));
        Araclar.copyStream(paket.parafImzaAl(), new FileOutputStream(klasor + "\\" + "ParafImzaCades.imz"));
        Araclar.copyStream(paket.imzaAl(), new FileOutputStream(klasor + "\\" + "ImzaCades.imz"));
        Araclar.copyStream(paket.muhurAl(), new FileOutputStream(klasor + "\\" + "MuhurCades.imz"));
        Araclar.copyStream(paket.parafOzetiAl(), new FileOutputStream(klasor + "\\" + "ParafOzeti.xml"));
        Araclar.copyStream(paket.paketOzetiAl(), new FileOutputStream(klasor + "\\" + "PaketOzeti.xml"));
        Araclar.copyStream(paket.nihaiOzetAl(), new FileOutputStream(klasor + "\\" + "NihaiOzet.xml"));
        Araclar.copyStream(paket.nihaiUstveriAl(), new FileOutputStream(klasor + "\\" + "NihaiUstveri.xml"));
        paket.kapat();
    }

    private void paketAcOkumaModu(String dosyaYolu) throws Exception {


        Optional<PaketTipi> paketTipi = Araclar.paketTipiGetir(new FileInputStream(dosyaYolu));
        if (!paketTipi.isPresent()) {
            ConsoleHelper.println("Paket tipi belirlenemedi");
            return;
        }
        switch (paketTipi.get()) {
            case NormalPaket: {
                _paket = Paket.ac(dosyaYolu, PaketModu.Ac);

                List<OzetDogrulamaHatasi> paketOzetList = new ArrayList<>();
                List<OzetDogrulamaHatasi> nihaiOzetList = new ArrayList<>();
                List<OzetDogrulamaHatasi> parafOzetiList = new ArrayList<>();

                IdentifierType dagitimIdentifier = new IdentifierType();
                //dagitimIdentifier.setValue("");
                //_paket.ekleriKontrolEt(dagitimIdentifier);

                InputStream paketiOzeti = _paket.paketOzetiAl();
                if (paketiOzeti != null) {
                    _paket.paketOzetiDogrula(Araclar.paketOzetiAl(Araclar.inputStreamToBytes(paketiOzeti)), paketOzetList, dagitimIdentifier);
                }
                InputStream nihaiOzet = _paket.nihaiOzetAl();
                if (nihaiOzet != null) {
                    _paket.nihaiOzetDogrula(Araclar.nihaiOzetAl(Araclar.inputStreamToBytes(nihaiOzet)), nihaiOzetList, dagitimIdentifier);
                }
                InputStream parafOzeti = _paket.parafOzetiAl();
                if (parafOzeti != null) {
                    _paket.parafOzetiDogrula(Araclar.parafOzetiAl(Araclar.inputStreamToBytes(parafOzeti)), parafOzetiList, dagitimIdentifier);
                }

                ConsoleHelper.println("Paket okuma modunda açıldı", ConsoleHelper.Colors.ANSI_GREEN);

                if (!coreBileseniDogrula(dosyaYolu)) {
                    ConsoleHelper.println("Core bileşeni doğrulanamadı.", ConsoleHelper.Colors.ANSI_RED);
                }
                if (paketOzetList.size() > 0) {
                    for (OzetDogrulamaHatasi ozetDogrulamaHatasi : paketOzetList) {
                        ConsoleHelper.println(String.format("%s - %s", ozetDogrulamaHatasi.getHataKodu(), ozetDogrulamaHatasi.getHata()), ConsoleHelper.Colors.ANSI_RED);
                    }

                }
                if (nihaiOzetList.size() > 0) {
                    for (OzetDogrulamaHatasi ozetDogrulamaHatasi : nihaiOzetList) {
                        ConsoleHelper.println(String.format("%s - %s", ozetDogrulamaHatasi.getHataKodu(), ozetDogrulamaHatasi.getHata()), ConsoleHelper.Colors.ANSI_RED);
                    }
                }

                if (parafOzetiList.size() > 0) {
                    for (OzetDogrulamaHatasi ozetDogrulamaHatasi : parafOzetiList) {
                        ConsoleHelper.println(String.format("%s - %s", ozetDogrulamaHatasi.getHataKodu(), ozetDogrulamaHatasi.getHata()), ConsoleHelper.Colors.ANSI_RED);
                    }
                }

                paketDurumuBelirle(PaketDurumuEnum.OkumaModundaAcildi);
                break;
            }
            case SifreliPaket: {
                ConsoleHelper.println("Şifreli paket açılamamaktadır");
                break;
            }
            case GuncellemePaketi: {
                List<OzetDogrulamaHatasi> nihaiOzetList = new ArrayList<>();
                GuncellemePaketi paket = GuncellemePaketi.ac(new FileInputStream(dosyaYolu));
                InputStream nihaiOzet = paket.nihaiOzetAl();
                if (nihaiOzet != null) {
                    paket.nihaiOzetDogrula(Araclar.nihaiOzetAl(Araclar.inputStreamToBytes(nihaiOzet)), nihaiOzetList);
                }
                ConsoleHelper.println("Paket okuma modunda açıldı", ConsoleHelper.Colors.ANSI_GREEN);

                if (!guncellemePaketiCoreBileseniDogrula(dosyaYolu)) {
                    ConsoleHelper.println("Core bileşeni doğrulanamadı.", ConsoleHelper.Colors.ANSI_RED);
                }
                if (nihaiOzetList.size() > 0) {
                    for (OzetDogrulamaHatasi ozetDogrulamaHatasi : nihaiOzetList) {
                        ConsoleHelper.println(String.format("%s - %s", ozetDogrulamaHatasi.getHataKodu(), ozetDogrulamaHatasi.getHata()), ConsoleHelper.Colors.ANSI_RED);
                    }
                }
                paket.kapat();
                paketDurumuBelirle(PaketDurumuEnum.Kapali);
                ConsoleHelper.println("Paket AÇ modunda açıldı ve kapatıldı.");
                break;
            }
        }
    }

    private void paketiSifrele() throws Exception {
        sifreliPaketYolu = String.format("%spaketler\\%s.eyps", calismaKlasoru, dosyaAdi);
        SifreliPaket sifreliPaket = SifreliPaket.ac(sifreliPaketYolu, PaketModu.Olustur);
        Paket paket = Paket.ac(paketYolu, PaketModu.Ac);
        CTOlusturan olusturan = paket.getUstveri().olusturanAl();
        sifreliPaket.nihaiOzetEkle(paket.nihaiOzetAl());
        sifreliPaket.getBelgeHedef().hedefEkle(Araclar.dagitim2Hedef(paket.getUstveri().dagitimlariAl().get(0)));
        sifreliPaket.belgeHedefOlustur();
        paket.kapat();

        File tempFile = File.createTempFile(UUID.randomUUID().toString().toUpperCase(), ".tmp");

        FileOutputStream outputStream = new FileOutputStream(tempFile);
        outputStream.write(paketSifrele(paketYolu));
        FileInputStream inputStream = new FileInputStream(tempFile);
        sifreliPaket.sifreliIcerikEkle(inputStream, UUID.randomUUID());
        sifreliPaket.sifreliIcerikBilgisiOlustur();
        sifreliPaket.kapat(UUID.randomUUID(), olusturan, null);
        ConsoleHelper.println("Şifreli paket ouşturulu.", ConsoleHelper.Colors.ANSI_YELLOW);
        ConsoleHelper.println(String.format("Şifreli Paket Yolu %s", sifreliPaketYolu), ConsoleHelper.Colors.ANSI_YELLOW);

        sifreliPaket = SifreliPaket.ac(sifreliPaketYolu, PaketModu.Ac);
        ConsoleHelper.println("Şifreli paket açıldı.", ConsoleHelper.Colors.ANSI_YELLOW);
        sifreliPaket.kapat(UUID.randomUUID(), olusturan, null);
        ConsoleHelper.println("Şifreli paket kapatıldı.", ConsoleHelper.Colors.ANSI_YELLOW);
    }

    /**
     * Paket Javada core bileşeninin InputStream özelliğini vermediğinden dolayı core bileşeni
     * nihai özet içerisinde doğrulanmamaktadır. Bundan dolayı ayrıca core bileşeninin doğruluğu kontrol edilmelidir.
     * Burada paketi açıp core bileşenin referansDogrula metodu ile doğrulanıyor.
     * NOT: Paket bileşenlerinin doğrulandığı yerlerde coreBileseniDogrula metodu çağırılmalıdır.
     *
     * @param dosyaYolu
     * @return
     * @throws Exception
     */
    public boolean coreBileseniDogrula(String dosyaYolu) throws Exception {
        ZipInputStream zipStream = new ZipInputStream(new FileInputStream(dosyaYolu));
        ZipInputStream zipStream2 = new ZipInputStream(new FileInputStream(dosyaYolu));
        Paket paket = Paket.ac(dosyaYolu, PaketModu.Ac);
        ZipEntry zipEntry;
        String name;
        while (true) {
            zipEntry = zipStream.getNextEntry();
            if (zipEntry == null)
                break;
            name = zipEntry.getName();
            if (!(name.substring(0, 1).equalsIgnoreCase("/"))) {
                name = "/" + name;
            }
            if (name.equalsIgnoreCase(paket.getCorePartUri()))
                break;
        }
        if (zipEntry == null) {
            throw new Exception("Core.xml bulunamadı.");
        }

        ZipEntry zipEntry2;
        String name2;
        while (true) {
            zipEntry2 = zipStream2.getNextEntry();
            if (zipEntry2 == null)
                break;
            name2 = zipEntry2.getName();
            if (!(name2.substring(0, 1).equalsIgnoreCase("/"))) {
                name2 = "/" + name2;
            }
            if (name2.equalsIgnoreCase(paket.getCorePartUri()))
                break;
        }
        if (zipEntry2 == null) {
            throw new Exception("Core.xml bulunamadı.");
        }


        List<CTReference> items = paket.getNihaiOzet().getNihaiOzet().getReference();
        if (items != null && items.size() > 0) {
            for (CTReference item : items) {
                if (item == null)
                    return false;

                if (item.getURI() == null)
                    return false;

                if (!item.getURI().equals(paket.getCorePartUri()))
                    continue;

                if (item.getContent() == null || item.getContent().size() == 0)
                    return false;

                if (item.getContent() == null || item.getContent().size() == 1)
                    return false;


                CTDigestItem digestItem = item.getContent().get(0).getValue();
                CTDigestItem digestItem1 = item.getContent().get(1).getValue();

                if (digestItem.getDigestMethod() == null) {
                    return false;
                }
                if (digestItem1.getDigestMethod() == null) {
                    return false;
                }
                if (digestItem.getDigestMethod().getAlgorithm() == null || digestItem.getDigestMethod().getAlgorithm().isEmpty()) {
                    return false;
                }
                if (digestItem1.getDigestMethod().getAlgorithm() == null || digestItem1.getDigestMethod().getAlgorithm().isEmpty()) {
                    return false;
                }
                if (digestItem.getDigestMethod().getAlgorithm().equals(digestItem1.getDigestMethod().getAlgorithm())) {
                    return false;
                }
                if (!digestItem.getDigestMethod().getAlgorithm().equals(Araclar.ozetModuToString(OzetModu.SHA512))
                        && !digestItem1.getDigestMethod().getAlgorithm().equals(Araclar.ozetModuToString(OzetModu.SHA512))) {
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
                    return false;
                }

                if (item.getType() != null && item.getType().equals(Araclar.HARICI_PAKET_BILESENI_REFERANS_TIPI)) {
                    return true;
                }

                byte[] computedDigestValue;
                byte[] computedDigestValue1;


                try {
                    computedDigestValue = Araclar.ozetHesapla(zipStream, mod);
                    computedDigestValue1 = Araclar.ozetHesapla(zipStream2, mod1);
                } catch (Exception ex) {
                    System.err.println(ex);
                    return false;
                }
                if (!Arrays.equals(computedDigestValue, digestItem.getDigestValue())) {
                    return false;
                }
                if (!Arrays.equals(computedDigestValue1, digestItem1.getDigestValue())) {
                    return false;
                }
            }
        }
        paket.kapat();
        zipStream.close();
        return true;
    }

    private byte[] paketSifrele(String dosyaYolu) throws IOException {
        //Paket şifrelenmeli ve şifreli veri geri döndürülmeli.
        FileInputStream stream = new FileInputStream(dosyaYolu);
        return Araclar.inputStreamToBytes(stream);
    }

    private void paketOlustur() throws Exception {
        paketYolu = String.format("%spaketler\\%s.eyp", calismaKlasoru, dosyaAdi);
        _paket = Paket.ac(paketYolu, PaketModu.Olustur);

        UUID paketId = UUID.randomUUID();
        TextType paketKonu = textTypeOlustur("e-Yazışma Test Paketi");

        CTEk ek1 = ekOlusturDahiliDijitalDosya(ekDosyasi1);
        CTEk ek2 = ekOlusturHariciReferans();
        CTEk ek3 = ekOlusturFiziksel();
        CTEk ek4 = ekOlusturDahiliDijitalDosyaImzasiz(ekDosyasi2);
        CTEk ek5 = ekOlusturDahiliDijitalDosyaImzali(ekDosyasi3);
        CTIlgi ilgiA = ilgiOlustur1(ek1);
        CTIlgi ilgiB = ilgiOlustur2();

        CTIlgili ilgili = new CTIlgili();
        ilgili.setGercekSahis(ilgiliOlustur());
        CTKurumKurulus olusturan = olusturanOlustur();
        CTDagitim dagitim1 = dagitimOlustur1(ek5);
        CTDagitim dagitim2 = dagitimOlustur2();

        CTDogrulamaBilgisi dogrulamaBilgisi = new CTDogrulamaBilgisi();
        dogrulamaBilgisi.setDogrulamaAdresi("https://www.turkiye.gov.tr/belge-dogrulama");

        _paket.getUstveri().belgeIdBelirle(paketId);
        _paket.getUstveri().dogrulamaBilgisiEkle(dogrulamaBilgisi);
        _paket.getUstveri().konuBelirle(paketKonu);
        _paket.getUstveri().guvenlikKoduBelirle(STKodGuvenlikKodu.HZO);

        Calendar calendarGecerlilikTarihi = Calendar.getInstance();
        calendarGecerlilikTarihi.add(Calendar.YEAR, 10);
        _paket.getUstveri().guvenlikGecerlilikTarihiBelirle(calendarGecerlilikTarihi);

        _paket.getUstveri().ozIdBelirle("8CEA7FF7-75F2-4CCF-B3C1-1B9B2054E8E9", "GUID");
        _paket.getUstveri().dagitimEkle(dagitim1);
        _paket.getUstveri().dagitimEkle(dagitim2);

        _paket.getUstveri().ekEkle(ek1);
        _paket.getUstveri().ekEkle(ek2);
        _paket.getUstveri().ekEkle(ek3);
        _paket.getUstveri().ekEkle(ek4);
        _paket.getUstveri().ekEkle(ek5);

        _paket.getUstveri().ilgiEkle(ilgiA);
        _paket.getUstveri().ilgiEkle(ilgiB);
        _paket.getUstveri().dilBelirle("tur");
        _paket.getUstveri().olusturanBelirle(olusturan);

        _paket.getUstveri().ilgiliEkle(ilgili);
        _paket.getUstveri().dosyaAdiBelirle(dosyaAdiGetir(ustYaziDosyasi));
        _paket.getUstveri().sdpEkle(sdpOlustur("İç Genelgeler", "010.06.01", ""));
        _paket.getUstveri().digerSdpEkle(sdpOlustur("Duyurular", "010.07.01", ""));
        _paket.getUstveri().digerSdpEkle(sdpOlustur("Rehber Klavuz", "010.08", ""));

        _paket.getUstveri().heysKoduEkle(heyskOlustur(999999, "Ayrıntılı Harcama Programının Hazırlanması", "Ayrıntılı Harcama Programının Hazırlanması ve Maliye Bakanlığına Gönderilmesi"));
        _paket.getUstveri().heysKoduEkle(heyskOlustur(111111, "Yatırım Programlarının Hazırlanması", "Yatırım Programlarının Hazırlanıp Başbakanlığa Gönderilmesi"));

        _paket.ekEkle(ek1, ekDosyasi1);
        _paket.ekEkle(ek4, ekDosyasi2, OzetModu.Yok);
        _paket.ekEkle(ek5, ekDosyasi3, OzetModu.SHA512);

        ConsoleHelper.println("Ekler eklendi", ConsoleHelper.Colors.ANSI_GREEN);

        _paket.ustYaziEkle(ustYaziDosyasi, "application/pdf", OzetModu.SHA256);
        ConsoleHelper.println("Üstyazı eklendi.", ConsoleHelper.Colors.ANSI_GREEN);

        _paket.ekleriKontrolEt();
        _paket.ilgileriKontrolEt();

        _paket.ustveriOlustur();
        ConsoleHelper.println("Üstveri olutşturuldu", ConsoleHelper.Colors.ANSI_GREEN);
        paketDurumuBelirle(PaketDurumuEnum.YeniOlusturuldu);

    }

    private void muhurEkle() throws Exception {
        _paket.getNihaiUstveri().belgeNoBelirle("1234");
        ConsoleHelper.println("Belge numarası atandı", ConsoleHelper.Colors.ANSI_YELLOW);
        _paket.getNihaiUstveri().tarihBelirle(Calendar.getInstance());
        ConsoleHelper.println("Belge tarihi atandı", ConsoleHelper.Colors.ANSI_YELLOW);
        _paket.getNihaiUstveri().imzaEkle(imzaciOlustur());
        _paket.getNihaiUstveri().imzaEkle(imzaciOlustur2());
        ConsoleHelper.println("Belge imzacıları atandı", ConsoleHelper.Colors.ANSI_YELLOW);
        _paket.nihaiUstVeriOlustur();
        ConsoleHelper.println("NihaiUstveri oluşturuldu", ConsoleHelper.Colors.ANSI_GREEN);
        ConsoleHelper.println("Core Oluşturuldu.", ConsoleHelper.Colors.ANSI_GREEN);
        if (!_paket.nihaiOzetVarMi()) {
            _paket.nihaiOzetOlustur();
            ConsoleHelper.println("NihaiOzet oluşturuldu.", ConsoleHelper.Colors.ANSI_GREEN);
            _paket.kapat();
            ConsoleHelper.println("Paket kapatıldı", ConsoleHelper.Colors.ANSI_GREEN);

            /*
             Kullanılan Apache POI API'sı, 'core' bileşeninin oluşturulması aşamasına
             müdahaleye engel olduğundan dolayı, bu yardımcı metod kullanılarak Core
             bileşeni özeti NihaiOzet'e eklenir.
             */
            ConsoleHelper.println("Paket core eklenmek için açıldı", ConsoleHelper.Colors.ANSI_GREEN);
            nihaiOzeteCoreEkle();
            ConsoleHelper.println("Nihai özete core eklendi.", ConsoleHelper.Colors.ANSI_GREEN);
            _paket = Paket.ac(PaketTest.paketYolu, PaketModu.Guncelle);
        }
        InputStream stream = _paket.nihaiOzetAl();
        byte[] imzaliNihaiOzet = imzala(Araclar.inputStreamToBytes(stream));
        _paket.muhurEkle(imzaliNihaiOzet);
        ConsoleHelper.println("Mühür eklendi.", ConsoleHelper.Colors.ANSI_GREEN);
        paketDurumuBelirle(PaketDurumuEnum.MuhurEklendi);
    }

    private void nihaiOzeteCoreEkle() throws Exception {
        Paket paket = Paket.ac(PaketTest.paketYolu, PaketModu.Guncelle);
        ZipInputStream zipStream = new ZipInputStream(new FileInputStream(PaketTest.paketYolu));
        ZipInputStream zipStream2 = new ZipInputStream(new FileInputStream(PaketTest.paketYolu));
        String corePartUri = paket.getCorePartUri();

        ZipEntry zipEntry;
        String name;
        while (true) {
            zipEntry = zipStream.getNextEntry();
            if (zipEntry == null)
                break;

            name = zipEntry.getName();
            if (!(name.substring(0, 1).equalsIgnoreCase("/"))) {
                name = "/" + name;
            }
            if (name.equalsIgnoreCase(corePartUri))
                break;
        }
        if (zipEntry == null) {
            throw new Exception("Core.xml bulunamadı.");
        }
        ZipEntry zipEntry2;
        String name2;
        while (true) {
            zipEntry2 = zipStream2.getNextEntry();

            if (zipEntry2 == null)
                break;
            name2 = zipEntry2.getName();
            if (!(name2.substring(0, 1).equalsIgnoreCase("/"))) {
                name2 = "/" + name2;
            }
            if (name2.equalsIgnoreCase(corePartUri))
                break;
        }
        if (zipEntry2 == null) {
            throw new Exception("Core.xml bulunamadı.");
        }

        byte[] ozet = Araclar.ozetHesapla(zipStream, paket.getVarsayilanOzetModu());
        byte[] ozetSha512 = Araclar.ozetHesapla(zipStream2, OzetModu.SHA512);
        paket.getNihaiOzet().ekle(paket.getVarsayilanOzetModu(), ozet, ozetSha512, new URI(corePartUri));
        paket.nihaiOzetOlustur();
        paket.kapat();
    }

    private void imzaEkle() throws Exception {
        if (!_paket.paketOzetiVarMi()) {
            _paket.paketOzetiOlustur();
            ConsoleHelper.println("Paket özeti oluşturuldu.", ConsoleHelper.Colors.ANSI_GREEN);
        }
        if (!_paket.imzaVarMi()) {
            InputStream stream = _paket.paketOzetiAl();
            byte[] imzaliPaketOzeti = imzala(Araclar.inputStreamToBytes(stream));
            _paket.imzaEkle(imzaliPaketOzeti);
            ConsoleHelper.println("İmza eklendi.", ConsoleHelper.Colors.ANSI_GREEN);
        } else {
            InputStream stream = _paket.imzaAl();
            byte[] imzaliImza = imzala(Araclar.inputStreamToBytes(stream));
            _paket.imzaEkle(imzaliImza);
            ConsoleHelper.println("Seri imza eklendi.", ConsoleHelper.Colors.ANSI_GREEN);
        }
        paketDurumuBelirle(PaketDurumuEnum.ImzaEklendi);
    }

    private void parafEkle() throws Exception {
        if (!_paket.parafOzetiVarMi()) {
            _paket.parafOzetiOlustur();
            ConsoleHelper.println("Paraf özeti oluşturuldu.", ConsoleHelper.Colors.ANSI_GREEN);
        }
        if (!_paket.parafImzaVarMi()) {
            InputStream stream = _paket.parafOzetiAl();
            byte[] imzaliVeri = imzala(Araclar.inputStreamToBytes(stream));
            _paket.parafImzaEkle(imzaliVeri);
            ConsoleHelper.println("Paraf eklendi", ConsoleHelper.Colors.ANSI_GREEN);
        } else {
            InputStream stream = _paket.parafImzaAl();
            byte[] imzaliParafImza = imzala(Araclar.inputStreamToBytes(stream));
            _paket.parafImzaEkle(imzaliParafImza);
            ConsoleHelper.println("Paraf eklendi", ConsoleHelper.Colors.ANSI_GREEN);
        }
        paketDurumuBelirle(PaketDurumuEnum.ParafEklendi);
    }

    private byte[] imzala(byte[] imzalanacakVeri) {
        return new byte[]{1, 2, 3};
    }

    private CTHEYSK heyskOlustur(int kod, String ad, String tanim) {
        CTHEYSK heysk = new CTHEYSK();
        heysk.setAd(ad);
        heysk.setKod(kod);
        heysk.setTanim(tanim);
        return heysk;
    }

    private CTSDP sdpOlustur(String ad, String kod, String aciklama) {
        CTSDP sdp = new CTSDP();
        sdp.setAd(ad);
        sdp.setAciklama(aciklama);
        sdp.setKod(kod);
        return sdp;
    }

    private CTId idOlustur() {
        CTId id = new CTId();
        id.setValue(UUID.randomUUID().toString()); // Zorunlu Alan
        return id;
    }

    private IdentifierType ozIdOlustur(String guid) {
        IdentifierType ozId = new IdentifierType();
        ozId.setValue(guid);
        ozId.setSchemeID("GUID"); //Zorunlu alan
        return ozId;
    }

    private String dosyaAdiGetir(String dosyaYolu) {
        return new File(dosyaYolu).getName();
    }

    private CTEk ekOlusturDahiliDijitalDosya(String dosyaYolu) {
        CTEk ek = new CTEk();
        ek.setId(idOlustur());
        ek.setBelgeNo("72131250-010.03-936");
        ek.setTur(STKodEkTuru.DED); // Zorunlu Alan
        ek.setDosyaAdi(dosyaAdiGetir(dosyaYolu));
        ek.setMimeTuru("application/pdf");
        ek.setAd(textTypeOlustur("Birinci ek"));
        ek.setSiraNo(1); // Zorunlu Alan
        ek.setAd(textTypeOlustur("Birinci ek açıklaması"));
        ek.setOzId(ozIdOlustur("A14A4DCC-AE6A-4FD5-AAB3-8A33DC6125DD"));
        ek.setImzaliMi(true);
        return ek;
    }

    private CTEk ekOlusturFiziksel() {
        CTEk ek = new CTEk();
        ek.setId(idOlustur());
        ek.setBelgeNo("48154937-612.01.03-942");
        ek.setTur(STKodEkTuru.FZK);
        ek.setAd(textTypeOlustur("Fiziksel ek"));
        ek.setSiraNo(3); // Zorunlu Alan
        ek.setAciklama(textTypeOlustur("İki adet CD"));
        ek.setOzId(ozIdOlustur("45A778BA-B943-4B68-8504-71854DF0DDFF"));
        ek.setImzaliMi(false);
        return ek;
    }

    private CTEk ekOlusturDahiliDijitalDosyaImzasiz(String dosyaYolu) {
        CTEk ek = new CTEk();
        ek.setId(idOlustur());
        ek.setBelgeNo("65410891-020-408");
        ek.setTur(STKodEkTuru.DED);
        ek.setDosyaAdi(dosyaAdiGetir(dosyaYolu));
        ek.setMimeTuru("application/MSWord");
        ek.setAd(textTypeOlustur("İmzasız ek"));
        ek.setSiraNo(4); // Zorunlu Alan
        ek.setAciklama(textTypeOlustur("İmzasız ek açıklaması"));
        ek.setOzId(ozIdOlustur("76E16DDA-AB44-46DB-B615-42FBE7E9BEE3"));
        ek.setImzaliMi(false);
        return ek;
    }

    private CTEk ekOlusturDahiliDijitalDosyaImzali(String dosyaYolu) {
        CTEk ek = new CTEk();
        CTId id = idOlustur();
        id.setEYazismaIdMi(true);
        ek.setId(id);
        ek.setBelgeNo("65410891-020-444");
        ek.setTur(STKodEkTuru.DED);
        ek.setDosyaAdi(dosyaAdiGetir(dosyaYolu));
        ek.setMimeTuru("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        ek.setAd(textTypeOlustur("İkinci ek"));
        ek.setSiraNo(5); // Zorunlu Alan
        ek.setAciklama(textTypeOlustur("İkinci ek açıklaması"));
        ek.setOzId(ozIdOlustur("D7D16B9C-7419-451B-82B0-34D334572464"));
        ek.setImzaliMi(true);
        return ek;
    }

    private CTIlgi ilgiOlustur1(CTEk ek1) {
        CTIlgi ilgi = new CTIlgi();
        ilgi.setId(idOlustur());
        ilgi.setBelgeNo("65410891-020-480");
        ilgi.setTarih(getDate());
        ilgi.setEtiket("a");
        ilgi.setEkId(ek1.getId().getValue());
        ilgi.setAd(textTypeOlustur("İlgi (a) yazı"));
        ilgi.setAciklama(textTypeOlustur("İlgi (a) yazının açıklaması"));
        ilgi.setOzId(ozIdOlustur("1C26C956-AE45-4DC1-BE86-1B8278A34904"));
        return ilgi;
    }

    private CTIlgi ilgiOlustur2() {
        CTIlgi ilgi = new CTIlgi();
        ilgi.setId(idOlustur());
        ilgi.setBelgeNo("48154937-612.01.03-945");
        ilgi.setTarih(getDate());
        ilgi.setEtiket("b");
        ilgi.setAd(textTypeOlustur("İlgi (b) yazı"));
        ilgi.setAciklama(textTypeOlustur("İlgi (b) yazının açıklaması"));
        ilgi.setOzId(ozIdOlustur("1C26C956-AE45-4DC1-BE86-1B8278A34904"));
        return ilgi;
    }

    private NameType nameTypeOlustur(String value) {
        NameType nameType = new NameType();
        nameType.setValue(value);
        return nameType;
    }

    private TextType textTypeOlustur(String value) {
        TextType textType = new TextType();
        textType.setValue(value);
        return textType;
    }

    private CTImza imzaciOlustur2() {
        CTImza imza = new CTImza();
        CTGercekSahis gercekSahis = new CTGercekSahis();
        CTKisi kisi = new CTKisi();
        kisi.setIlkAdi(nameTypeOlustur("Metin"));
        kisi.setSoyadi(nameTypeOlustur("Demir"));
        kisi.setOnEk(textTypeOlustur("Dr."));
        gercekSahis.setKisi(kisi);
        gercekSahis.setTCKN("98787678902");
        gercekSahis.setGorev(textTypeOlustur("Şube Müdürü"));
        imza.setImzalayan(gercekSahis);

        imza.setMakam(nameTypeOlustur("Daire"));
        imza.setAmac(textTypeOlustur("Onay"));
        imza.setAciklama(textTypeOlustur("İmzanın Açıklaması"));
        imza.setTarih(getDate());
        return imza;
    }

    private CTImza imzaciOlustur() {
        CTImza imza = new CTImza();
        CTGercekSahis gercekSahis = new CTGercekSahis();
        CTKisi kisi = new CTKisi();
        kisi.setIlkAdi(nameTypeOlustur("Osman"));
        kisi.setSoyadi(nameTypeOlustur("Yeşil"));
        kisi.setUnvan(nameTypeOlustur("Büyükelçi"));
        kisi.setIkinciAdi(nameTypeOlustur("Murat"));
        kisi.setOnEk(textTypeOlustur("Dr."));
        gercekSahis.setKisi(kisi);
        gercekSahis.setTCKN("12345678902");
        gercekSahis.setGorev(textTypeOlustur("Daire Başkanı"));
        CTIletisimBilgisi iletisimBilgisi = new CTIletisimBilgisi();
        iletisimBilgisi.setTelefon("0-312-2666666");
        iletisimBilgisi.setTelefonDiger("0-532-4828282");
        iletisimBilgisi.setEPosta("om_yesil@xyz.com.tr");
        iletisimBilgisi.setFaks("0-312-2888888");
        iletisimBilgisi.setAdres(textTypeOlustur("T.C Cumhurbaşkanlığı Külliyesi 06560 Beştepe - Ankara"));
        iletisimBilgisi.setIlce(nameTypeOlustur("Çankaya"));
        iletisimBilgisi.setIl(nameTypeOlustur("Ankara"));
        iletisimBilgisi.setUlke(nameTypeOlustur("Türkiye"));
        iletisimBilgisi.setWebAdresi("https://www.cbddo.gov.tr/");
        gercekSahis.setIletisimBilgisi(iletisimBilgisi);
        imza.setImzalayan(gercekSahis);

        CTGercekSahis yetkiDevreden = new CTGercekSahis();
        CTKisi yetkiDevredenKisi = new CTKisi();
        yetkiDevredenKisi.setIlkAdi(nameTypeOlustur("Hasan"));
        yetkiDevredenKisi.setSoyadi(nameTypeOlustur("Aydın"));
        yetkiDevredenKisi.setUnvan(nameTypeOlustur("Genel Müdür"));
        yetkiDevredenKisi.setIkinciAdi(nameTypeOlustur("Şaban"));
        yetkiDevredenKisi.setOnEk(textTypeOlustur("Uzman"));
        yetkiDevreden.setKisi(yetkiDevredenKisi);
        yetkiDevreden.setTCKN("12345588901");
        yetkiDevreden.setGorev(textTypeOlustur("Genel Müdr"));
        CTIletisimBilgisi iletisimBilgisi1YetkiDereden = new CTIletisimBilgisi();
        iletisimBilgisi1YetkiDereden.setTelefon("0-312-2999999");
        iletisimBilgisi1YetkiDereden.setTelefonDiger("0-532-4827777");
        iletisimBilgisi1YetkiDereden.setEPosta("iletisim@abc.com");
        iletisimBilgisi1YetkiDereden.setFaks("0-312-2946345");
        iletisimBilgisi1YetkiDereden.setAdres(textTypeOlustur("TUİK Necatibey Cad. No:112"));
        iletisimBilgisi1YetkiDereden.setIlce(nameTypeOlustur("Çankaya"));
        iletisimBilgisi1YetkiDereden.setIl(nameTypeOlustur("Ankara"));
        iletisimBilgisi1YetkiDereden.setUlke(nameTypeOlustur("Türkiye"));
        iletisimBilgisi1YetkiDereden.setWebAdresi("www.tuik.gov.tr");
        yetkiDevreden.setIletisimBilgisi(iletisimBilgisi1YetkiDereden);
        imza.setYetkiDevreden(yetkiDevreden);

        CTGercekSahis vekaletVeren = new CTGercekSahis();
        CTKisi vekaletVerenKisi = new CTKisi();
        vekaletVerenKisi.setIlkAdi(nameTypeOlustur("Semih"));
        vekaletVerenKisi.setSoyadi(nameTypeOlustur("Yılmaz"));
        vekaletVerenKisi.setUnvan(nameTypeOlustur("Dr."));
        vekaletVeren.setKisi(vekaletVerenKisi);
        vekaletVeren.setGorev(textTypeOlustur("Müsteşar"));
        imza.setVekaletVeren(vekaletVeren);

        imza.setMakam(nameTypeOlustur("Müsteşar"));
        imza.setAmac(textTypeOlustur("Onay"));
        imza.setAciklama(textTypeOlustur("İmzanın açıklaması"));
        imza.setTarih(getDate());

        return imza;
    }

    private CTGercekSahis ilgiliOlustur() {
        CTGercekSahis ilgili = new CTGercekSahis();
        CTKisi kisi = new CTKisi();
        kisi.setIlkAdi(nameTypeOlustur("Hüseyin"));
        kisi.setSoyadi(nameTypeOlustur("Yetmez"));
        kisi.setOnEk(textTypeOlustur("Uzman"));
        ilgili.setKisi(kisi);
        ilgili.setGorev(textTypeOlustur("Birim Sorumlusu"));
        ilgili.setTCKN("12345678903");

        CTIletisimBilgisi iletisim = new CTIletisimBilgisi();
        iletisim.setTelefon("0-505-4821111");
        iletisim.setTelefonDiger("0-312-2444444");
        ilgili.setIletisimBilgisi(iletisim);

        return ilgili;
    }

    private CTKurumKurulus olusturanOlustur() {
        CTKurumKurulus olusturan = new CTKurumKurulus();
        olusturan.setKKK("82223362");
        olusturan.setBirimKKK("12345678");
        olusturan.setAdi(nameTypeOlustur("Türkiye Cumhuriyeti Cumhurbaşkanlığı Dijital Dönüşüm Ofisi Başkanlığı"));
        CTIletisimBilgisi iletisim = new CTIletisimBilgisi();
        iletisim.setTelefon("0-312-4036400");
        iletisim.setEPosta("dijitaliletisim@cbddo.gov.tr");
        iletisim.setKepAdresi("cbddo@hs01.kep.tr");
        iletisim.setFaks("0-312-4036500");
        iletisim.setAdres(textTypeOlustur("T.C. Cumhurbaşkanlığı Çankaya Yerleşkesi Ziaur Rahman Cad. 06550"));
        iletisim.setIlce(nameTypeOlustur("Çankaya"));
        iletisim.setIl(nameTypeOlustur("Ankara"));
        iletisim.setUlke(nameTypeOlustur("Türkiye"));
        iletisim.setWebAdresi("https://www.cbddo.gov.tr");
        olusturan.setIletisimBilgisi(iletisim);
        return olusturan;
    }

    private CTDagitim dagitimOlustur1(CTEk konulmamisEk) {
        CTDagitim dagitim = new CTDagitim();
        dagitim.setDagitimTuru(STKodDagitimTuru.GRG);
        CTKurumKurulus kurum = new CTKurumKurulus();
        kurum.setKKK("24301050");
        kurum.setAdi(nameTypeOlustur("Adalet Bakanlığı"));

        CTIletisimBilgisi iletisim = new CTIletisimBilgisi();
        iletisim.setTelefon("0-312-2665555");
        iletisim.setEPosta("info@adalet.gov.tr");
        iletisim.setKepAdresi("adaletbakanligi@hs01.kep.tr");
        iletisim.setFaks("0-312-2665556");
        iletisim.setAdres(textTypeOlustur("Kızılay"));
        iletisim.setIlce(nameTypeOlustur("Çankaya"));
        iletisim.setIl(nameTypeOlustur("Ankara"));
        iletisim.setUlke(nameTypeOlustur("Türkiye"));
        iletisim.setWebAdresi("www.adalet.gov.tr");
        kurum.setIletisimBilgisi(iletisim);

        kurum.setBirimKKK("24301051");
        dagitim.setKurumKurulus(kurum);
        CTKonulmamisEkListesi ekListesi = new CTKonulmamisEkListesi();
        CTKonulmamisEk ek = new CTKonulmamisEk();
        ek.setEkId(konulmamisEk.getId().getValue());
        ekListesi.getKonulmamisEk().add(ek);
        dagitim.setKonulmamisEkListesi(ekListesi);
        return dagitim;
    }

    private CTDagitim dagitimOlustur2() {
        CTDagitim dagitim = new CTDagitim();
        dagitim.setDagitimTuru(STKodDagitimTuru.BLG);
        dagitim.setIvedilik(STKodIvedilik.GNL);
        try {
            dagitim.setMiat(DatatypeFactory.newInstance().newDuration("P1D"));
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
        CTKurumKurulus kurum = new CTKurumKurulus();
        kurum.setKKK("24322010");
        kurum.setAdi(nameTypeOlustur("Sağlık Bakanlığı"));
        dagitim.setKurumKurulus(kurum);
        return dagitim;
    }

    private CTEk ekOlusturHariciReferans() {
        CTEk ek = new CTEk();
        CTId id = new CTId();
        id.setValue(UUID.randomUUID().toString()); // Zorunlu Alan
        ek.setId(id);
        ek.setBelgeNo("35826416-020-987");
        ek.setTur(STKodEkTuru.HRF); // Zorunlu Alan
        TextType ad = new TextType();
        ad.setValue("Harici referans");
        ek.setSiraNo(2); // Zorunlu Alan
        TextType aciklama = new TextType();
        aciklama.setValue("Hairici referans açıklaması");
        ek.setAciklama(aciklama);
        ek.setReferans("http://www.bilgitoplumu.gov.tr/Documents/1/Raporlar/Calisma_Raporu_2.pdf");
        IdentifierType ozId = new IdentifierType();
        ozId.setValue("0C9C4D34-D370-46C2-A996-0FADC0FA591F");
        ozId.setSchemeID("GUID"); //Zorunlu alan
        ek.setOzId(ozId);
        ek.setImzaliMi(false);
        CTOzet ozet = new CTOzet();
        CTOzetAlgoritmasi ozetAlgoritmasi = new CTOzetAlgoritmasi();
        ozetAlgoritmasi.setAlgorithm(Araclar.ALGORITHM_SHA256); //Zorunlu alan
        ozet.setOzetAlgoritmasi(ozetAlgoritmasi);
        ozet.setOzetDegeri(Base64.getDecoder().decode("/InKUd7ATLR4PzUCBiWjyD38d9oxbWF72RR1QVSdFMo="));
        ek.setOzet(ozet);
        return ek;
    }

    private XMLGregorianCalendar getDate() {
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void ustveriGuncellemePaketiOlustur() throws Exception {
        UUID belgeId = UUID.randomUUID();
        String konu = "Güncelleme Paketi";
        guncellemePaketiDosyasiYolu = String.format("%spaketler\\%s.eypg", calismaKlasoru, UUID.randomUUID().toString().toUpperCase());
        GuncellemePaketi guncellemePaketi = GuncellemePaketi.ac(guncellemePaketiDosyasiYolu, PaketModu.Olustur);

        ConsoleHelper.println("Güncelleme paketi oluşturuldu");
        Paket orijinalPaket = Paket.ac(paketYolu, PaketModu.Ac);
        CTOlusturan olusturan = orijinalPaket.getUstveri().olusturanAl();
        orijinalPaket.kapat();

        CTGuvenlikKoduDegisiklikBilgisi bilgi = new CTGuvenlikKoduDegisiklikBilgisi();
        bilgi.setYeniGizlilikDerecesi(STKodGuvenlikKodu.YOK);
        bilgi.setDegistirmeTarihi(getDate());
        bilgi.setAciklama("Belgenin gizlilik süresinin dolması nedeni ile Hizmete Özel olan gizlilik derecesi kaldırılmıştır");
        bilgi.setKomisyonKarariBelgeNo("E-68244839-170.01-44181");
        bilgi.setKomisyonKarariBelgeId("85E65A34-F460-4590-8BFA-A2DDB07E1B1D");

        guncellemePaketi.getGuncellemeBilgisi().guncellemeEkle(STGuncellemeTuru.GuvenlikKodu, bilgi);
        guncellemePaketi.guncellemeBilgisiOlustur();
        guncellemePaketi.orijinalPaketEkle(new FileInputStream(paketYolu), PaketTipi.NormalPaket);

        /*
         * Paket belirteçleri nihaiOzetOlustur metodundan önce çağırılmalıdır.
         * BelgeId olarak verilen Paketin Id değeri NihaiOzet içerisine verildiğinden dolayı
         * belirtecleriBelirle metodu çağırılmaz ise veya daha sonra çağırılır ise Nihai Özet oluşturulamayacaktır.
         * */
        guncellemePaketi.belirtecleriBelirle(belgeId, olusturan, konu);

        guncellemePaketi.nihaiOzetOlustur();
        ConsoleHelper.println("NihaiOzet oluşturuldu.", ConsoleHelper.Colors.ANSI_GREEN);

        guncellemePaketi.kapat();

        ConsoleHelper.println("Paket kapatıldı", ConsoleHelper.Colors.ANSI_GREEN);
        /*
             Kullanılan Apache POI API'sı, 'core' bileşeninin oluşturulması aşamasına
             müdahaleye engel olduğundan dolayı, bu yardımcı metod kullanılarak Core
             bileşeni özeti NihaiOzet'e eklenir.
        */
        ConsoleHelper.println("Paket core eklenmek için açıldı", ConsoleHelper.Colors.ANSI_GREEN);

        guncellemePaketiNihaiOzetineCoreEkle(belgeId, olusturan, konu);

        ConsoleHelper.println("Nihai özete core eklendi.", ConsoleHelper.Colors.ANSI_GREEN);

        guncellemePaketi = GuncellemePaketi.ac(PaketTest.guncellemePaketiDosyasiYolu, PaketModu.Guncelle);

        guncellemePaketi.muhurEkle(imzala(Araclar.inputStreamToBytes(guncellemePaketi.nihaiOzetAl())));
        ConsoleHelper.println("Mühür eklendi.", ConsoleHelper.Colors.ANSI_GREEN);
        guncellemePaketi.kapat();
        ConsoleHelper.println("Güncelleme paketi kapatıldı.", ConsoleHelper.Colors.ANSI_GREEN);
        paketDurumuBelirle(PaketDurumuEnum.Kapali);
    }

    private void guncellemePaketiNihaiOzetineCoreEkle(UUID belgeId, CTOlusturan olusturan, String konu) throws Exception {
        GuncellemePaketi paket = GuncellemePaketi.ac(PaketTest.guncellemePaketiDosyasiYolu, PaketModu.Guncelle);
        ZipInputStream zipStream = new ZipInputStream(new FileInputStream(PaketTest.guncellemePaketiDosyasiYolu));
        ZipInputStream zipStream2 = new ZipInputStream(new FileInputStream(PaketTest.guncellemePaketiDosyasiYolu));
        String corePartUri = paket.getCorePartUri();

        ZipEntry zipEntry;
        String name;
        while (true) {
            zipEntry = zipStream.getNextEntry();
            if (zipEntry == null)
                break;

            name = zipEntry.getName();
            if (!(name.substring(0, 1).equalsIgnoreCase("/"))) {
                name = "/" + name;
            }
            if (name.equalsIgnoreCase(corePartUri))
                break;
        }
        if (zipEntry == null) {
            throw new Exception("Core.xml bulunamadı.");
        }
        ZipEntry zipEntry2;
        String name2;
        while (true) {
            zipEntry2 = zipStream2.getNextEntry();

            if (zipEntry2 == null)
                break;
            name2 = zipEntry2.getName();
            if (!(name2.substring(0, 1).equalsIgnoreCase("/"))) {
                name2 = "/" + name2;
            }
            if (name2.equalsIgnoreCase(corePartUri))
                break;
        }
        if (zipEntry2 == null) {
            throw new Exception("Core.xml bulunamadı.");
        }

        byte[] ozet = Araclar.ozetHesapla(zipStream, paket.getVarsayilanOzetModu());
        byte[] ozetSha512 = Araclar.ozetHesapla(zipStream2, OzetModu.SHA512);
        paket.getNihaiOzet().ekle(paket.getVarsayilanOzetModu(), ozet, ozetSha512, new URI(corePartUri));
        paket.nihaiOzetOlustur();
        paket.kapat();
    }

    /**
     * Paket Javada core bileşeninin InputStream özelliğini vermediğinden dolayı core bileşeni
     * nihai özet içerisinde doğrulanmamaktadır. Bundan dolayı ayrıca core bileşeninin doğruluğu kontrol edilmelidir.
     * Burada paketi açıp core bileşenin referansDogrula metodu ile doğrulanıyor.
     * NOT: Paket bileşenlerinin doğrulandığı yerlerde coreBileseniDogrula metodu çağırılmalıdır.
     *
     * @param dosyaYolu
     * @return
     * @throws Exception
     */
    public boolean guncellemePaketiCoreBileseniDogrula(String dosyaYolu) throws Exception {
        ZipInputStream zipStream = new ZipInputStream(new FileInputStream(dosyaYolu));
        ZipInputStream zipStream2 = new ZipInputStream(new FileInputStream(dosyaYolu));
        GuncellemePaketi paket = GuncellemePaketi.ac(dosyaYolu, PaketModu.Ac);
        ZipEntry zipEntry;
        String name;
        while (true) {
            zipEntry = zipStream.getNextEntry();
            if (zipEntry == null)
                break;
            name = zipEntry.getName();
            if (!(name.substring(0, 1).equalsIgnoreCase("/"))) {
                name = "/" + name;
            }
            if (name.equalsIgnoreCase(paket.getCorePartUri()))
                break;
        }
        if (zipEntry == null) {
            throw new Exception("Core.xml bulunamadı.");
        }

        ZipEntry zipEntry2;
        String name2;
        while (true) {
            zipEntry2 = zipStream2.getNextEntry();
            if (zipEntry2 == null)
                break;
            name2 = zipEntry2.getName();
            if (!(name2.substring(0, 1).equalsIgnoreCase("/"))) {
                name2 = "/" + name2;
            }
            if (name2.equalsIgnoreCase(paket.getCorePartUri()))
                break;
        }
        if (zipEntry2 == null) {
            throw new Exception("Core.xml bulunamadı.");
        }


        List<CTReference> items = paket.getNihaiOzet().getNihaiOzet().getReference();
        if (items != null && items.size() > 0) {
            for (CTReference item : items) {
                if (item == null)
                    return false;

                if (item.getURI() == null)
                    return false;

                if (!item.getURI().equals(paket.getCorePartUri()))
                    continue;

                if (item.getContent() == null || item.getContent().size() == 0)
                    return false;

                if (item.getContent() == null || item.getContent().size() == 1)
                    return false;


                CTDigestItem digestItem = item.getContent().get(0).getValue();
                CTDigestItem digestItem1 = item.getContent().get(1).getValue();

                if (digestItem.getDigestMethod() == null) {
                    return false;
                }
                if (digestItem1.getDigestMethod() == null) {
                    return false;
                }
                if (digestItem.getDigestMethod().getAlgorithm() == null || digestItem.getDigestMethod().getAlgorithm().isEmpty()) {
                    return false;
                }
                if (digestItem1.getDigestMethod().getAlgorithm() == null || digestItem1.getDigestMethod().getAlgorithm().isEmpty()) {
                    return false;
                }
                if (digestItem.getDigestMethod().getAlgorithm().equals(digestItem1.getDigestMethod().getAlgorithm())) {
                    return false;
                }
                if (!digestItem.getDigestMethod().getAlgorithm().equals(Araclar.ozetModuToString(OzetModu.SHA512))
                        && !digestItem1.getDigestMethod().getAlgorithm().equals(Araclar.ozetModuToString(OzetModu.SHA512))) {
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
                    return false;
                }

                if (item.getType() != null && item.getType().equals(Araclar.HARICI_PAKET_BILESENI_REFERANS_TIPI)) {
                    return true;
                }

                byte[] computedDigestValue;
                byte[] computedDigestValue1;


                try {
                    computedDigestValue = Araclar.ozetHesapla(zipStream, mod);
                    computedDigestValue1 = Araclar.ozetHesapla(zipStream2, mod1);
                } catch (Exception ex) {
                    System.err.println(ex);
                    return false;
                }
                if (!Arrays.equals(computedDigestValue, digestItem.getDigestValue())) {
                    return false;
                }
                if (!Arrays.equals(computedDigestValue1, digestItem1.getDigestValue())) {
                    return false;
                }
            }
        }
        paket.kapat();
        zipStream.close();
        return true;
    }
}
