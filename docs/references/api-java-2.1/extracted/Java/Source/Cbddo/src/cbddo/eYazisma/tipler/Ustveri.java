package cbddo.eYazisma.tipler;

import urn.cbddo.eyazisma.schema.xsd.codelist.IdentifierType;
import urn.cbddo.eyazisma.schema.xsd.codelist.TextType;
import urn.cbddo.eyazisma.schema.xsd.kodguvenlik.STKodGuvenlikKodu;
import urn.cbddo.eyazisma.schema.xsd.tipler.*;
import urn.cbddo.eyazisma.schema.xsd.ustveri.CTUstveri;

import javax.xml.datatype.DatatypeConfigurationException;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public abstract class Ustveri {

    public abstract CTUstveri getCTUstveri();

    public abstract void konuBelirle(TextType konu) throws Exception;

    public abstract TextType konuAl();

    public abstract void belgeIdBelirle(UUID belgeId);

    public abstract String belgeIdAl();

    abstract void mimeTuruBelirle(String mimeTuru) throws IllegalArgumentException;

    public abstract String mimeTuruAl();

    public abstract void guvenlikKoduBelirle(STKodGuvenlikKodu guvenlikKodu) throws Exception;

    public abstract STKodGuvenlikKodu guvenlikKoduAl();

    public abstract void guvenlikGecerlilikTarihiBelirle(Calendar gecerlilikTarihi) throws DatatypeConfigurationException;

    public abstract Calendar guvenlikGecerlilikTarihiAl();

    public abstract void ozIdBelirle(String ozId, String schemeId) throws IllegalArgumentException;

    public abstract IdentifierType ozIdAl();

    public abstract void dilBelirle(String dil);

    public abstract String dilAl();

    public abstract void olusturanBelirle(CTKurumKurulus kurumKurulus) throws IllegalArgumentException;

    public abstract void olusturanBelirle(CTTuzelSahis tuzelSahis);

    public abstract void olusturanBelirle(CTGercekSahis gercekSahis);

    public abstract CTOlusturan olusturanAl();

    public abstract void ekEkle(CTEk ek) throws IllegalArgumentException, Exception;

    public abstract List<CTEk> ekleriAl();

    public abstract void ilgiEkle(CTIlgi ilgi) throws IllegalArgumentException;

    public abstract List<CTIlgi> ilgileriAl();

    public abstract void ilgiliEkle(CTIlgili ilgili);

    public abstract List<CTIlgili> ilgilileriAl();

    public abstract void dagitimEkle(CTDagitim dagitim);

    public abstract List<CTDagitim> dagitimlariAl();

    public abstract void dosyaAdiBelirle(String dosyaAdi) throws IllegalArgumentException;

    public abstract String dosyaAdiAl();

    abstract void kontrolEt() throws Exception;

    public abstract void sdpEkle(CTSDP sdp);

    public abstract void digerSdpEkle(CTSDP sdp);

    public abstract void heysKoduEkle(CTHEYSK heysk);

    public abstract List<CTHEYSK> heysKodlariniAl();

    public abstract void dogrulamaBilgisiEkle(CTDogrulamaBilgisi dogrulamaBilgisi);

    public abstract CTDogrulamaBilgisi dogrulamaBilgisiAl();
}
