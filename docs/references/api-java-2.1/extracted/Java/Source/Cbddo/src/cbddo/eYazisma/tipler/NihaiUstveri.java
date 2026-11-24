package cbddo.eYazisma.tipler;

import urn.cbddo.eyazisma.schema.xsd.nihaiUstveri.CTNihaiUstveri;
import urn.cbddo.eyazisma.schema.xsd.tipler.CTImza;

import javax.xml.datatype.DatatypeConfigurationException;
import java.util.Calendar;
import java.util.List;


public abstract class NihaiUstveri {
    public abstract CTNihaiUstveri getNihaiUstveri();

    public abstract void tarihBelirle(Calendar tarih) throws DatatypeConfigurationException;

    public abstract Calendar tarihAl();

    public abstract void belgeNoBelirle(String belgeNo);

    public abstract String belgeNoAl();

    public abstract List<CTImza> imzalariAl();

    public abstract void imzaEkle(CTImza imza);

    public abstract void kontrolEt() throws Exception;
}
