package cbddo.eYazisma.tipler;

import org.apache.poi.openxml4j.opc.internal.ContentType;
import urn.cbddo.eyazisma.schema.xsd.codelist.IdentifierType;
import urn.cbddo.eyazisma.schema.xsd.codelist.TextType;
import urn.cbddo.eyazisma.schema.xsd.koddagitimturu.STKodDagitimTuru;
import urn.cbddo.eyazisma.schema.xsd.kodguvenlik.STKodGuvenlikKodu;
import urn.cbddo.eyazisma.schema.xsd.kodivedilik.STKodIvedilik;
import urn.cbddo.eyazisma.schema.xsd.tipler.*;
import urn.cbddo.eyazisma.schema.xsd.ustveri.CTUstveri;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.*;

class UstveriInternal extends Ustveri {
    CTUstveri _CTUstveri;

    public UstveriInternal() {
        _CTUstveri = new CTUstveri();
    }

    UstveriInternal(CTUstveri ustveri) {
        _CTUstveri = ustveri;
    }

    @Override
    public CTUstveri getCTUstveri() {
        return _CTUstveri;
    }

    @Override
    public void konuBelirle(TextType konu) throws Exception {
        if (konu == null) {
            throw new IllegalArgumentException("konu");
        }
        if (konu.getValue() == null || konu.getValue().length() == 0) {
            throw new Exception("Konu değeri boş olamaz.");
        }
        _CTUstveri.setKonu(konu);
    }

    @Override
    public void belgeIdBelirle(UUID belgeId) {
        if (belgeId == null) {
            throw new IllegalArgumentException("belgeId");
        }
        _CTUstveri.setBelgeId(belgeId.toString().toUpperCase());
    }

    @Override
    void mimeTuruBelirle(String mimeTuru) throws IllegalArgumentException {
        if (mimeTuru == null || mimeTuru.trim().isEmpty()) {
            throw new IllegalArgumentException("mimeTuru");
        }
        _CTUstveri.setMimeTuru(mimeTuru);
    }

    @Override
    public void guvenlikKoduBelirle(STKodGuvenlikKodu guvenlikKodu) throws Exception {
        if (guvenlikKodu == STKodGuvenlikKodu.OZL) {
            throw new Exception("Özel güvenlik kodu kaldırılmıştır.");
        }
        _CTUstveri.setGuvenlikKodu(guvenlikKodu);
    }

    @Override
    public void guvenlikGecerlilikTarihiBelirle(Calendar gecerlilikTarihi) throws DatatypeConfigurationException {
        GregorianCalendar c = (GregorianCalendar) gecerlilikTarihi;
        XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        _CTUstveri.setGuvenlikKoduGecerlilikTarihi(date2);
    }

    @Override
    public void ozIdBelirle(String ozId, String schemeId) throws IllegalArgumentException {
        if (ozId == null || ozId.trim().isEmpty()) {
            throw new IllegalArgumentException("ozId");
        }
        if (schemeId == null || schemeId.trim().isEmpty()) {
            throw new IllegalArgumentException("schemeId");
        }
        IdentifierType i = new IdentifierType();
        i.setValue(ozId);
        i.setSchemeID(schemeId);
        _CTUstveri.setOzId(i);
    }

    @Override
    public void dilBelirle(String dil) {
        if (dil == null || dil.trim().isEmpty() || dil.length() != 3) {
            throw new IllegalArgumentException("dil");
        }
        _CTUstveri.setDil(dil);
    }

    @Override
    public void olusturanBelirle(CTKurumKurulus kurumKurulus) throws IllegalArgumentException {
        if (kurumKurulus == null) {
            throw new IllegalArgumentException("kurumKurulus");
        }
        CTOlusturan o = new CTOlusturan();
        o.setKurumKurulus(kurumKurulus);
        _CTUstveri.setOlusturan(o);
    }

    @Override
    public void olusturanBelirle(CTTuzelSahis tuzelSahis) {
        if (tuzelSahis == null) {
            throw new IllegalArgumentException("tuzelSahis");
        }
        CTOlusturan o = new CTOlusturan();
        o.setTuzelSahis(tuzelSahis);
        _CTUstveri.setOlusturan(o);
    }

    @Override
    public void olusturanBelirle(CTGercekSahis gercekSahis) {
        if (gercekSahis == null) {
            throw new IllegalArgumentException("gercekSahis");
        }
        CTOlusturan o = new CTOlusturan();
        o.setGercekSahis(gercekSahis);
        _CTUstveri.setOlusturan(o);
    }

    @Override
    public void ekEkle(CTEk ek) throws Exception {
        if (ek == null)
            throw new IllegalArgumentException("ek");

        if (ek.getId() == null || ek.getId().getValue().trim().isEmpty())
            throw new IllegalArgumentException("ek.Id");
        try {
            if (ek.getId() != null && !ek.getId().getValue().isEmpty()) {
                UUID.fromString(ek.getId().getValue());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("ID guid türünde olmalıdır.");
        }
        switch (ek.getTur()) {
            case DED:
                try {
                    new ContentType(ek.getMimeTuru());
                } catch (Exception e) {
                    throw new Exception("MIMETURU formatı uygun değil.", e);
                }
                break;
            case HRF:
                if (ek.getReferans() == null || ek.getReferans().trim().isEmpty())
                    throw new IllegalArgumentException("Referans");
                break;
        }
        ek.getId().setValue(ek.getId().getValue().toUpperCase(Locale.ROOT));

        if (_CTUstveri.getEkler() == null)
            _CTUstveri.setEkler(new CTEkler());
        _CTUstveri.getEkler().getEk().add(ek);
    }


    @Override
    public void ilgiEkle(CTIlgi ilgi) throws IllegalArgumentException {
        if (ilgi == null)
            throw new IllegalArgumentException("ilgi");

        if (ilgi.getId() == null || ilgi.getId().getValue() == null || ilgi.getId().getValue().isEmpty())
            throw new IllegalArgumentException("ilgi.Id");

        ilgi.getId().setValue(ilgi.getId().getValue().toUpperCase(Locale.ROOT));
        if (ilgi.getEkId() != null && !ilgi.getEkId().isEmpty()) {
            ilgi.setEkId(ilgi.getEkId().toUpperCase(Locale.ROOT));
        }

        if (_CTUstveri.getIlgiler() == null)
            _CTUstveri.setIlgiler(new CTIlgiler());
        _CTUstveri.getIlgiler().getIlgi().add(ilgi);
    }

    @Override
    public void ilgiliEkle(CTIlgili ilgili) {
        if (_CTUstveri.getIlgiliListesi() == null)
            _CTUstveri.setIlgiliListesi(new CTIlgiliListesi());
        _CTUstveri.getIlgiliListesi().getIlgili().add(ilgili);
    }

    @Override
    void kontrolEt() throws Exception {
        if (_CTUstveri.getKonu() == null || _CTUstveri.getKonu().getValue() == null || _CTUstveri.getKonu().getValue().trim().isEmpty()) {
            throw new Exception("Üstveri bileşeni, \"Konu\" alanı için değer verilmemiş.");
        }
        if (_CTUstveri.getMimeTuru() == null || _CTUstveri.getMimeTuru().trim().isEmpty()) {
            throw new Exception("Üstveri bileşeni, \"MimeTuru\" alanı için değer verilmemiş.");
        }
        if (_CTUstveri.getBelgeId() == null) {
            throw new Exception("Üstveri bileşeni, \"BelgeId\" alanı için değer verilmemiş.");
        }
        if (_CTUstveri.getDosyaAdi() == null) {
            throw new Exception("Üstveri bileşeni, \"DosyaAdi\" alanı için değer verilmemiş.");
        }
        if (_CTUstveri.getGuvenlikKodu() == null)
            _CTUstveri.setGuvenlikKodu(STKodGuvenlikKodu.YOK);

        if (_CTUstveri.getDogrulamaBilgisi() == null) {
            throw new Exception("Üstveri bileşeni, \"DogrulamaBilgisi\" alanı için değer verilmemiş.");
        }
        if (_CTUstveri.getDogrulamaBilgisi().getDogrulamaAdresi() == null ||
                _CTUstveri.getDogrulamaBilgisi().getDogrulamaAdresi().trim().isEmpty()) {
            throw new Exception("Üstveri bileşeni, \"DogrulamaBilgisi.DogrulamaAdresi\" alanı için değer verilmemiş.");
        }

        if (_CTUstveri.getDagitimListesi() == null || _CTUstveri.getDagitimListesi().getDagitim().size() == 0) {
            throw new Exception("Üstveri bileşeni, \"DagitimListesi\" alanı için değer verilmemiş.");
        }
        for (int i = 0; i < _CTUstveri.getDagitimListesi().getDagitim().size(); i++) {
            XsdAraclar.kontrolEt(_CTUstveri.getDagitimListesi().getDagitim().get(i));
        }

        if (_CTUstveri.getEkler() != null && _CTUstveri.getEkler().getEk().size() > 0) {
            for (int i = 0; i < _CTUstveri.getEkler().getEk().size(); i++) {
                XsdAraclar.kontrolEt(_CTUstveri.getEkler().getEk().get(i));
            }
        }
        if (_CTUstveri.getIlgiler() != null && _CTUstveri.getIlgiler().getIlgi().size() > 0) {
            for (int i = 0; i < _CTUstveri.getIlgiler().getIlgi().size(); i++) {
                XsdAraclar.kontrolEt(_CTUstveri.getIlgiler().getIlgi().get(i));
            }
        }
        if (_CTUstveri.getIlgiliListesi() != null && _CTUstveri.getIlgiliListesi().getIlgili().size() > 0) {
            for (int i = 0; i < _CTUstveri.getIlgiliListesi().getIlgili().size(); i++) {
                XsdAraclar.kontrolEt(_CTUstveri.getIlgiliListesi().getIlgili().get(i));
            }
        }
        if (_CTUstveri.getHeyskListesi() != null && _CTUstveri.getHeyskListesi().getHeysk().size() > 0) {
            for (CTHEYSK heysk : _CTUstveri.getHeyskListesi().getHeysk()) {
                XsdAraclar.kontrolEt(heysk);
            }
        }
        if (_CTUstveri.getOlusturan() == null) {
            throw new Exception("Üstveri bileşeni, \"Olusturan\" alanı için değer verilmemiş.");
        }
        XsdAraclar.kontrolEt(_CTUstveri.getOlusturan());
    }

    @Override
    public void sdpEkle(CTSDP sdp) {
        if (_CTUstveri.getSdpBilgisi() == null) {
            _CTUstveri.setSdpBilgisi(new CTSDPBilgisi());
            _CTUstveri.getSdpBilgisi().setAnaSdp(sdp);
        }
    }

    @Override
    public void digerSdpEkle(CTSDP sdp) {
        if (_CTUstveri.getSdpBilgisi() == null) {
            _CTUstveri.setSdpBilgisi(new CTSDPBilgisi());
        }
        if (_CTUstveri.getSdpBilgisi().getDigerSdpler() == null) {
            _CTUstveri.getSdpBilgisi().setDigerSdpler(new CTSDPListesi());
        }
        CTSDPListesi digerSdpler = _CTUstveri.getSdpBilgisi().getDigerSdpler();
        digerSdpler.getSdpListesi().add(sdp);
        _CTUstveri.getSdpBilgisi().setDigerSdpler(digerSdpler);
    }

    @Override
    public void heysKoduEkle(CTHEYSK heysk) {
        if (heysk == null)
            throw new IllegalArgumentException("heysk");
        if (heysk.getAd() == null || heysk.getAd().isEmpty())
            throw new IllegalArgumentException("heysk.ad");
        if (heysk.getKod() == 0)
            throw new IllegalArgumentException("heysk.kod");

        if (_CTUstveri.getHeyskListesi() == null)
            _CTUstveri.setHeyskListesi(new CTHEYSKListesi());
        _CTUstveri.getHeyskListesi().getHeysk().add(heysk);
    }

    @Override
    public List<CTHEYSK> heysKodlariniAl() {
        if (_CTUstveri.getHeyskListesi() == null)
            return null;
        return _CTUstveri.getHeyskListesi().getHeysk();
    }

    @Override
    public void dogrulamaBilgisiEkle(CTDogrulamaBilgisi dogrulamaBilgisi) {
        if (dogrulamaBilgisi == null)
            throw new IllegalArgumentException("dogrulamaBilgisi");
        if (dogrulamaBilgisi.getDogrulamaAdresi() == null || dogrulamaBilgisi.getDogrulamaAdresi().trim().isEmpty())
            throw new IllegalArgumentException("dogrulamaAdresi");
        _CTUstveri.setDogrulamaBilgisi(dogrulamaBilgisi);
    }

    @Override
    public CTDogrulamaBilgisi dogrulamaBilgisiAl() {
        return _CTUstveri.getDogrulamaBilgisi();
    }

    @Override
    public void dagitimEkle(CTDagitim dagitim) {
        if (dagitim == null) {
            throw new IllegalArgumentException("dagitim");
        }
        if (dagitim.getDagitimTuru() == null)
            dagitim.setDagitimTuru(STKodDagitimTuru.GRG);
        if (dagitim.getIvedilik() == null)
            dagitim.setIvedilik(STKodIvedilik.NRM);
        if (dagitim.getKonulmamisEkListesi() != null && dagitim.getKonulmamisEkListesi().getKonulmamisEk() != null &&
                dagitim.getKonulmamisEkListesi().getKonulmamisEk().size() > 0) {
            for (CTKonulmamisEk ctKonulmamisEk : dagitim.getKonulmamisEkListesi().getKonulmamisEk()) {

                ctKonulmamisEk.setEkId(ctKonulmamisEk.getEkId().toUpperCase(Locale.ROOT));
            }
        }

        if (_CTUstveri.getDagitimListesi() == null)
            _CTUstveri.setDagitimListesi(new CTDagitimListesi());
        _CTUstveri.getDagitimListesi().getDagitim().add(dagitim);
    }

    @Override
    public TextType konuAl() {
        return _CTUstveri.getKonu();
    }

    @Override
    public String belgeIdAl() {
        return _CTUstveri.getBelgeId();
    }

    @Override
    public String mimeTuruAl() {
        return _CTUstveri.getMimeTuru();
    }

    @Override
    public STKodGuvenlikKodu guvenlikKoduAl() {
        return _CTUstveri.getGuvenlikKodu();
    }

    @Override
    public Calendar guvenlikGecerlilikTarihiAl() {
        if (_CTUstveri.getGuvenlikKoduGecerlilikTarihi() == null)
            return null;
        return _CTUstveri.getGuvenlikKoduGecerlilikTarihi().toGregorianCalendar();
    }

    @Override
    public IdentifierType ozIdAl() {
        return _CTUstveri.getOzId();
    }

    @Override
    public String dilAl() {
        return _CTUstveri.getDil();
    }

    @Override
    public CTOlusturan olusturanAl() {
        return _CTUstveri.getOlusturan();
    }

    @Override
    public List<CTEk> ekleriAl() {
        if (_CTUstveri.getEkler() == null)
            return null;
        return _CTUstveri.getEkler().getEk();
    }

    @Override
    public List<CTIlgi> ilgileriAl() {
        if (_CTUstveri.getIlgiler() == null)
            return null;
        return _CTUstveri.getIlgiler().getIlgi();
    }

    @Override
    public List<CTIlgili> ilgilileriAl() {
        if (_CTUstveri.getIlgiliListesi() == null)
            return null;
        return _CTUstveri.getIlgiliListesi().getIlgili();
    }

    @Override
    public List<CTDagitim> dagitimlariAl() {
        if (_CTUstveri.getDagitimListesi() == null)
            return null;
        return _CTUstveri.getDagitimListesi().getDagitim();
    }

    @Override
    public void dosyaAdiBelirle(String dosyaAdi) throws IllegalArgumentException {
        if (dosyaAdi == null) {
            throw new IllegalArgumentException("dosyaAdi");
        }
        _CTUstveri.setDosyaAdi(dosyaAdi);
    }

    @Override
    public String dosyaAdiAl() {
        return _CTUstveri.getDosyaAdi();
    }
}
