package cbddo.eYazisma.tipler;

import urn.cbddo.eyazisma.schema.xsd.guncellemeturu.STGuncellemeTuru;
import urn.cbddo.eyazisma.schema.xsd.tipler.CTGuncelleme;
import urn.cbddo.eyazisma.schema.xsd.tipler.CTGuncellemeBilgisi;

import java.util.List;

public abstract class GuncellemeBilgisi {
    public abstract CTGuncellemeBilgisi getGuncellemeBilgisi();
    public abstract void guncellemeEkle(STGuncellemeTuru guncellemeTuru,Object guncelleme) throws Exception;
    public abstract List<CTGuncelleme> guncellemeleriAl();
    public abstract void kontrolEt() throws Exception;
}
