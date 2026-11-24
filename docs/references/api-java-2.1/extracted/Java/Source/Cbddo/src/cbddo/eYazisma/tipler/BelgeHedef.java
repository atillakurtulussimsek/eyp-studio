package cbddo.eYazisma.tipler;


import urn.cbddo.eyazisma.schema.xsd.belgeHedef.CTBelgeHedef;
import urn.cbddo.eyazisma.schema.xsd.tipler.CTHedef;

import java.util.List;

public abstract class BelgeHedef {
    public abstract CTBelgeHedef getCTBelgeHedef();

    public abstract List<CTHedef> hedefleriAl();

    public abstract void hedefEkle(CTHedef hedef);

    public abstract void kontrolEt();
}
