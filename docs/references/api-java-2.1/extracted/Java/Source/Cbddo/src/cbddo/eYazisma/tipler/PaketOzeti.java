package cbddo.eYazisma.tipler;


import urn.cbddo.eyazisma.schema.xsd.paketOzeti.CTPaketOzeti;
import urn.cbddo.eyazisma.schema.xsd.paketOzeti.CTReference;

import java.net.URI;
import java.util.List;

public abstract class PaketOzeti {
    public abstract CTPaketOzeti getPaketOzeti();

    public abstract void ekle(OzetModu ozetModu, byte[] ozetDegeri, byte[] ozetDegeriSha512, URI uri);

    public abstract void ekle(OzetModu ozetModu, byte[] ozetDegeri, byte[] ozetDegeriSha512, URI uri, Boolean hariciBilesenMi);

    public abstract List<CTReference> ozetleriAl();

    public abstract void kontrolEt();
}
