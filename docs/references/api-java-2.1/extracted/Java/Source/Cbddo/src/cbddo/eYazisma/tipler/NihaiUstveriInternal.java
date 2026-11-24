package cbddo.eYazisma.tipler;

import urn.cbddo.eyazisma.schema.xsd.tipler.CTBelgeImza;
import urn.cbddo.eyazisma.schema.xsd.nihaiUstveri.CTNihaiUstveri;
import urn.cbddo.eyazisma.schema.xsd.tipler.CTImza;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class NihaiUstveriInternal extends NihaiUstveri {

    CTNihaiUstveri _CTNihaiUstveri;

    public NihaiUstveriInternal() {
        _CTNihaiUstveri = new CTNihaiUstveri();
    }

    NihaiUstveriInternal(CTNihaiUstveri nihaiUstveri) {
        _CTNihaiUstveri = nihaiUstveri;
    }


    @Override
    public CTNihaiUstveri getNihaiUstveri() {
        return _CTNihaiUstveri;
    }

    @Override
    public void tarihBelirle(Calendar tarih) throws DatatypeConfigurationException {
        GregorianCalendar c = (GregorianCalendar) tarih;
        XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        _CTNihaiUstveri.setTarih(date2);
    }

    @Override
    public Calendar tarihAl() {
        if (_CTNihaiUstveri.getTarih() == null)
            return null;
        return _CTNihaiUstveri.getTarih().toGregorianCalendar();
    }

    @Override
    public void belgeNoBelirle(String belgeNo) {
        if (belgeNo == null || belgeNo.trim().isEmpty()) {
            throw new IllegalArgumentException("belgeNo");
        }
        _CTNihaiUstveri.setBelgeNo(belgeNo);
    }

    @Override
    public String belgeNoAl() {
        return _CTNihaiUstveri.getBelgeNo();
    }

    @Override
    public List<CTImza> imzalariAl() {
        if (_CTNihaiUstveri.getBelgeImzalar() == null)
            return null;
        return _CTNihaiUstveri.getBelgeImzalar().getImza();
    }

    @Override
    public void imzaEkle(CTImza imza) {
        if (_CTNihaiUstveri.getBelgeImzalar() == null)
            _CTNihaiUstveri.setBelgeImzalar(new CTBelgeImza());
        _CTNihaiUstveri.getBelgeImzalar().getImza().add(imza);
    }

    @Override
    public void kontrolEt() throws Exception {
        if (_CTNihaiUstveri.getBelgeNo() == null || _CTNihaiUstveri.getBelgeNo().trim().isEmpty()) {
            throw new Exception("Üstveri bileşeni, \"BelgeNo\" alanı için değer verilmemiş.");
        }
        if (_CTNihaiUstveri.getTarih() == null)
            throw new Exception("Üstveri bileşeni, \"Tarih\" alanı için değer verilmemiş.");
        if (_CTNihaiUstveri.getBelgeImzalar() != null && _CTNihaiUstveri.getBelgeImzalar().getImza() != null &&
                !_CTNihaiUstveri.getBelgeImzalar().getImza().isEmpty()) {
            for (CTImza ctImza : _CTNihaiUstveri.getBelgeImzalar().getImza()) {
                XsdAraclar.kontrolEt(ctImza);
            }
        }
    }
}
