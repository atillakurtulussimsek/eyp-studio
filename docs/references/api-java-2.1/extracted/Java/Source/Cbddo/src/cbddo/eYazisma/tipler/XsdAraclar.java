package cbddo.eYazisma.tipler;

import urn.cbddo.eyazisma.schema.xsd.belgeHedef.CTBelgeHedef;
import urn.cbddo.eyazisma.schema.xsd.guncellemeturu.STGuncellemeTuru;
import urn.cbddo.eyazisma.schema.xsd.kodekturu.STKodEkTuru;
import urn.cbddo.eyazisma.schema.xsd.nihaiOzet.CTNihaiOzet;
import urn.cbddo.eyazisma.schema.xsd.nihaiUstveri.CTNihaiUstveri;
import urn.cbddo.eyazisma.schema.xsd.paketOzeti.CTDigestItem;
import urn.cbddo.eyazisma.schema.xsd.paketOzeti.CTDigestMethod;
import urn.cbddo.eyazisma.schema.xsd.paketOzeti.CTPaketOzeti;
import urn.cbddo.eyazisma.schema.xsd.paketOzeti.CTReference;
import urn.cbddo.eyazisma.schema.xsd.parafOzeti.CTParafOzeti;
import urn.cbddo.eyazisma.schema.xsd.tipler.*;

import javax.xml.bind.JAXBElement;

public class XsdAraclar {

    public static void kontrolEt(CTKisi kisi) {
        if (kisi == null) {
            throw new IllegalArgumentException("kisi");
        }
        if (kisi.getIlkAdi() == null) {
            throw new RuntimeException("Kişi nesnesi, İlk adı alanı boş olamaz.");
        }
        if (kisi.getIlkAdi().getValue() == null || kisi.getIlkAdi().getValue().trim().isEmpty()) {
            throw new RuntimeException("Kişi nesnesi, İlk adı alan değeri boş olamaz.");
        }
        if (kisi.getSoyadi() == null) {
            throw new RuntimeException("Kişi nesnesi, Soyadı alanı boş olamaz.");
        }
        if (kisi.getSoyadi().getValue() == null || kisi.getSoyadi().getValue().trim().isEmpty()) {
            throw new RuntimeException("Kişi nesnesi, Soyadı alan değeri boş olamaz.");
        }
    }

    public static void kontrolEt(CTKurumKurulus kurumKurulus) {
        if (kurumKurulus == null) {
            throw new IllegalArgumentException("kurumKurulus");
        }
        if (kurumKurulus.getKKK() == null || kurumKurulus.getKKK().isEmpty()) {
            throw new RuntimeException("Kurum kimlik kodu boş olamaz.");
        }
    }

    public static void kontrolEt(CTTuzelSahis tuzelSahis) {
        if (tuzelSahis == null) {
            throw new IllegalArgumentException("tuzelSahis");
        }
        if (tuzelSahis.getId() == null) {
            throw new RuntimeException("TuzelSahis nesnesi, ID alanı boş olamaz.");
        }
        if (tuzelSahis.getId().getValue() == null || tuzelSahis.getId().getValue().trim().isEmpty()) {
            throw new RuntimeException("TuzelSahis nesnesi, ID alanı değeri boş olamaz.");
        }
        if (tuzelSahis.getId().getSchemeID() == null || tuzelSahis.getId().getSchemeID().trim().isEmpty()) {
            throw new RuntimeException("TuzelSahis nesnesi, ID alanı SchemeID değeri boş olamaz.");
        }
    }

    public static void kontrolEt(CTGercekSahis gercekSahis) {
        if (gercekSahis == null) {
            throw new IllegalArgumentException("gercekSahis");
        }
        if (gercekSahis.getKisi() == null) {
            throw new RuntimeException("GercekSahis nesnesi, Kişi alanı boş olamaz.");
        }
        kontrolEt(gercekSahis.getKisi());
    }

    public static void kontrolEt(CTIlgili ilgili) {
        if (ilgili == null) {
            throw new IllegalArgumentException("ilgili");
        }
        if (ilgili.getGercekSahis() != null) {
            kontrolEt(ilgili.getGercekSahis());
        } else if (ilgili.getTuzelSahis() != null) {
            kontrolEt(ilgili.getTuzelSahis());
        } else if (ilgili.getKurumKurulus() != null) {
            kontrolEt(ilgili.getKurumKurulus());
        } else {
            throw new RuntimeException("İlgili değeri için bir tane KurumKurulus, TuzelSahis veya GercekSahis değeri verilmelidir.");
        }
    }

    public static void kontrolEt(CTHEYSK heysk) {
        if (heysk == null) {
            throw new IllegalArgumentException("heysk");
        }
        if (heysk.getKod() == 0) {
            throw new IllegalArgumentException("HEYSK nesnesi, Kod değeri 0 olamaz");
        }
        if (heysk.getAd() == null || heysk.getAd().isEmpty()) {
            throw new IllegalArgumentException("HEYSK nesnesi, Ad değeri boş olamaz");
        }
    }

    public static void kontrolEt(CTIlgiliListesi ilgiliListesi) {
        if (ilgiliListesi == null) {
            throw new IllegalArgumentException("ilgiliListesi");
        }
        if (ilgiliListesi.getIlgili() == null || ilgiliListesi.getIlgili().size() == 0) {
            throw new RuntimeException("İlgiliListesi değeri için en az bir tane Ilgili değeri verilmelidir.");
        }
    }

    public static void kontrolEt(CTKonulmamisEk konulmamisEk) {
        if (konulmamisEk == null) {
            throw new IllegalArgumentException("konulmamisEk");
        }
        if (konulmamisEk.getEkId() == null || konulmamisEk.getEkId().trim().isEmpty()) {
            throw new RuntimeException("KonulmamisEk nesnesi, EkId boş olamaz.");
        }
    }

    public static void kontrolEt(CTKonulmamisEkListesi konulmamisEkListesi) {
        if (konulmamisEkListesi == null) {
            throw new IllegalArgumentException("konulmamisEkListesi");
        }
        if (konulmamisEkListesi.getKonulmamisEk() == null || konulmamisEkListesi.getKonulmamisEk().size() == 0) {
            throw new RuntimeException("KonulmamisEkListesi değeri için en az bir tane KonulmamisEk değeri verilmelidir.");
        }
    }

    public static void kontrolEt(CTDagitim dagitim) {
        if (dagitim == null) {
            throw new IllegalArgumentException("dagitim");
        }
        if (dagitim.getGercekSahis() != null) {
            kontrolEt(dagitim.getGercekSahis());
        } else if (dagitim.getTuzelSahis() != null) {
            kontrolEt(dagitim.getTuzelSahis());
        } else if (dagitim.getKurumKurulus() != null) {
            kontrolEt(dagitim.getKurumKurulus());
        } else {
            throw new RuntimeException("Dagitim değeri için bir tane KurumKurulus, TuzelSahis veya GercekSahis değeri verilmelidir.");
        }
        if (dagitim.getKonulmamisEkListesi() != null && dagitim.getKonulmamisEkListesi().getKonulmamisEk() != null && dagitim.getKonulmamisEkListesi().getKonulmamisEk().size() == 0)
            throw new RuntimeException("Dagitim nesnesi, Konulmamış ek listesi alanında en az bir tane ek olmalıdır.");
        if (dagitim.getKonulmamisEkListesi() != null && dagitim.getKonulmamisEkListesi().getKonulmamisEk() != null) {
            for (int i = 0; i < dagitim.getKonulmamisEkListesi().getKonulmamisEk().size(); i++) {
                kontrolEt(dagitim.getKonulmamisEkListesi().getKonulmamisEk().get(i));
            }
        }
    }

    public static void kontrolEt(CTOlusturan olusturan) {
        if (olusturan == null) {
            throw new IllegalArgumentException("olusturan");
        }
        if (olusturan.getGercekSahis() != null) {
            kontrolEt(olusturan.getGercekSahis());
        } else if (olusturan.getTuzelSahis() != null) {
            kontrolEt(olusturan.getTuzelSahis());
        } else if (olusturan.getKurumKurulus() != null) {
            kontrolEt(olusturan.getKurumKurulus());
        } else {
            throw new RuntimeException("Olusturan değeri için bir tane KurumKurulus, TuzelSahis veya GercekSahis değeri verilmelidir.");
        }
    }

    public static void kontrolEt(CTImza imza) {
        if (imza == null) {
            throw new IllegalArgumentException("imza");
        }
        if (imza.getImzalayan() == null) {
            throw new RuntimeException("İmza alanı için imzalayan alanı boş olamaz.");
        }
        kontrolEt(imza.getImzalayan());
        if (imza.getYetkiDevreden() != null)
            kontrolEt(imza.getYetkiDevreden());
        if (imza.getVekaletVeren() != null)
            kontrolEt(imza.getVekaletVeren());
    }

    public static void kontrolEt(CTHedef hedef) {
        if (hedef == null) {
            throw new IllegalArgumentException("hedef");
        }
        if (hedef.getGercekSahis() != null) {
            kontrolEt(hedef.getGercekSahis());
        } else if (hedef.getTuzelSahis() != null) {
            kontrolEt(hedef.getTuzelSahis());
        } else if (hedef.getKurumKurulus() != null) {
            kontrolEt(hedef.getKurumKurulus());
        } else {
            throw new RuntimeException("Hedef değeri için bir tane KurumKurulus, TuzelSahis veya GercekSahis değeri verilmelidir.");
        }
    }

    public static void kontrolEt(CTEk ek) {
        if (ek == null) {
            throw new IllegalArgumentException("ek");
        }
        if (ek.getId() == null || ek.getId().getValue() == null || ek.getId().getValue().trim().isEmpty()) {
            throw new RuntimeException("Ek nesnesi, Id değeri boş olamaz.");
        }
        if (ek.getTur() == null) {
            throw new RuntimeException("Tur değeri boş olamaz.");
        } else if (ek.getTur() == STKodEkTuru.DED) {
            if (ek.getDosyaAdi() == null)
                throw new RuntimeException("DosyaAdi değeri boş olamaz.");
        }
        if (ek.getOzId() != null && (ek.getOzId().getSchemeID() == null || ek.getOzId().getSchemeID().length() == 0)) {
            throw new RuntimeException("OzId değeri verilen ek için, SchemeId değeri boş olamaz.");
        }
        if ((ek.getTur() == STKodEkTuru.HRF || ek.getTur() == STKodEkTuru.FZK) && (ek.isImzaliMi() != null && ek.isImzaliMi())) {
            throw new RuntimeException("Harici Referans ve Fiziksel nesne türündeki ekler, imzalı olarak pakete eklenemezler.");
        }
        if (ek.getTur() == STKodEkTuru.HRF && ek.getOzet() != null) {
            kontrolEt(ek.getOzet());
        }
    }

    private static void kontrolEt(CTOzet ozet) {
        if (ozet == null) {
            throw new IllegalArgumentException("ozet");
        }
        if (ozet.getOzetAlgoritmasi() == null) {
            throw new RuntimeException("Ozet değeri için OzetAlgoritmasi değeri boş olamaz.");
        }
        kontrolEt(ozet.getOzetAlgoritmasi());
        if (ozet.getOzetDegeri() == null || ozet.getOzetDegeri().length == 0) {
            throw new RuntimeException("OzetDegeri boş olamaz.");
        }
    }

    private static void kontrolEt(CTOzetAlgoritmasi ozetAlgoritmasi) {
        if (ozetAlgoritmasi == null) {
            throw new IllegalArgumentException("ozetAlgoritmasi");
        }
        if (ozetAlgoritmasi.getAlgorithm() == null || ozetAlgoritmasi.getAlgorithm().trim().isEmpty()) {
            throw new RuntimeException("OzetAlgoritmasi, Algorithm adı alan değeri boş olamaz.");
        }
    }

    public static void kontrolEt(CTIlgi ilgi) {
        if (ilgi == null) {
            throw new IllegalArgumentException("ilgi");
        }
        if (ilgi.getId() == null || ilgi.getId().getValue() == null || ilgi.getId().getValue().trim().isEmpty()) {
            throw new RuntimeException("Ilgi nesnesi, Id değeri boş olamaz.");
        }
        if (ilgi.getEtiket() == null || ilgi.getEtiket().isEmpty()) {
            throw new RuntimeException("Ilgi nesnesi, Etiket değeri boş olamaz.");
        }
        if (ilgi.getOzId() != null && (ilgi.getOzId().getSchemeID() == null || ilgi.getOzId().getSchemeID().length() == 0))
            throw new RuntimeException("Ilgi nesnesi, OzId SchemeId değeri boş olamaz.");
    }

    public static void kontrolEt(CTBelgeHedef belgeHedef) {
        if (belgeHedef == null) {
            throw new IllegalArgumentException("belgeHedef");
        }
        if (belgeHedef.getHedefListesi() == null || belgeHedef.getHedefListesi().getHedef() == null || belgeHedef.getHedefListesi().getHedef().size() == 0) {
            throw new RuntimeException("BelgeHedef değeri için en az bir tane Hedef değeri verilmelidir.");
        }
        for (CTHedef c : belgeHedef.getHedefListesi().getHedef()) {
            kontrolEt(c);
        }
    }

    public static void kontrolEt(CTReference reference) {
        if (reference == null) {
            throw new IllegalArgumentException("reference");
        }
        if (reference.getContent() == null || reference.getContent().size() == 0)
            throw new RuntimeException("Reference nesnesi, DigestItem değeri boş olamaz.");
        if (reference.getContent().size() != 2)
            throw new RuntimeException("Reference nesnesi, DigestItem değeri 2'den fazla olamaz");

        for (JAXBElement<CTDigestItem> ctDigestItem : reference.getContent()) {
            kontrolEt(ctDigestItem.getValue().getDigestMethod());
        }
    }

    public static void kontrolEt(CTDigestMethod digetMethod) {
        if (digetMethod == null) {
            throw new IllegalArgumentException("digetMethod");
        }
        if (digetMethod.getAlgorithm() == null || digetMethod.getAlgorithm().trim().isEmpty()) {
            throw new RuntimeException("DigestMethod nesnesi, Algorithm adı alan değeri boş olamaz.");
        }
    }

    public static void kontrolEt(CTPaketOzeti paketOzeti) {
        if (paketOzeti == null) {
            throw new IllegalArgumentException("paketOzeti");
        }
        if (paketOzeti.getReference() == null || paketOzeti.getReference().size() == 0) {
            throw new RuntimeException("PaketOzeti değeri için en az bir tane Reference değeri verilmelidir.");
        }
        if (paketOzeti.getId() == null)
            throw new RuntimeException("PaketOzeti ID değeri boş olamaz.");
        for (CTReference c : paketOzeti.getReference()) {
            kontrolEt(c);
        }
    }

    public static void kontrolEt(CTNihaiOzet nihaiOzet) {
        if (nihaiOzet == null) {
            throw new IllegalArgumentException("nihaiOzet");
        }
        if (nihaiOzet.getReference() == null || nihaiOzet.getReference().size() == 0) {
            throw new RuntimeException("NihaiOzet değeri için en az bir tane Reference değeri verilmelidir.");
        }
        if (nihaiOzet.getId() == null)
            throw new RuntimeException("NihaiOzet ID değeri boş olamaz.");
        for (CTReference c : nihaiOzet.getReference()) {
            kontrolEt(c);
        }
    }

    public static void kontrolEt(CTParafOzeti parafOzeti) {
        if (parafOzeti == null)
            throw new IllegalArgumentException("parafOzeti");
        if (parafOzeti.getReference() == null || parafOzeti.getReference().size() == 0)
            throw new RuntimeException("ParafOzeti değeri için en az bir tane Reference değeri verilmelidir.");
        if (parafOzeti.getId() == null || parafOzeti.getId().trim().isEmpty())
            throw new RuntimeException("ParafOzeti ID değeri boş olamaz");
        for (CTReference ctReference : parafOzeti.getReference()) {
            kontrolEt(ctReference);
        }
    }

    public static void kontrolEt(CTNihaiUstveri nihaiUstveri) {
        if (nihaiUstveri.getBelgeNo() == null || nihaiUstveri.getBelgeNo().isEmpty()) {
            throw new RuntimeException("NihaiÜstveri bileşeni, \"BelgeNo\" alanı için değer verilmemiş.");
        }
        if (nihaiUstveri.getTarih() == null || !nihaiUstveri.getTarih().isValid()) {
            throw new RuntimeException("NihaiÜstveri bileşeni, \"Tarih\" alanı için değer verilmemiş.");
        }
        if (nihaiUstveri.getBelgeImzalar() != null && nihaiUstveri.getBelgeImzalar().getImza() != null &&
                nihaiUstveri.getBelgeImzalar().getImza().size() > 0) {
            for (CTImza ctImza : nihaiUstveri.getBelgeImzalar().getImza()) {
                kontrolEt(ctImza);
            }
        }
    }

    public static void kontrolEt(CTGuncellemeBilgisi guncellemeBilgisi) throws Exception {
        if (guncellemeBilgisi == null) {
            throw new Exception("CT_GuncellemeBilgisi nesnesi boş");
        }
        if (guncellemeBilgisi.getGuncellemeler() == null || guncellemeBilgisi.getGuncellemeler().isEmpty()) {
            throw new Exception("Güncellemeler eklenmemiş");
        }
        for (CTGuncelleme item : guncellemeBilgisi.getGuncellemeler()) {
            if (item.getGuncellemeTuru() == STGuncellemeTuru.GuvenlikKodu) {
                final CTGuvenlikKoduDegisiklikBilgisi degisiklikBilgisi = item.getGuvenlikKoduDegisiklikBilgisi();
                if (degisiklikBilgisi == null) {
                    throw new Exception("GuvenlikKoduDegisiklikBilgisi boş");
                }
                if (Araclar.isNullOrWhitespace(degisiklikBilgisi.getAciklama())) {
                    throw new Exception("Aciklama boş olamaz");
                }
                if (Araclar.isNullOrWhitespace(degisiklikBilgisi.getKomisyonKarariBelgeId())) {
                    throw new Exception("KomisyonKarariBelgeId boş olamaz");
                }
                if (Araclar.isNullOrWhitespace(degisiklikBilgisi.getKomisyonKarariBelgeNo())) {
                    throw new Exception("KomisyonKarariBelgeNo boş olamaz");
                }
                if (!degisiklikBilgisi.getDegistirmeTarihi().isValid()) {
                    throw new Exception("DegistirmeTarihi geçerli bir tarih değil");
                }
            } else {
                throw new Exception("Eklenen değişikliğin DegisiklikTuru belirlenemedi");
            }
        }
    }
}
