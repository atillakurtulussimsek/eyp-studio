package cbddo.eYazisma.tipler;

import urn.cbddo.eyazisma.schema.xsd.paketOzeti.CTReference;
import urn.cbddo.eyazisma.schema.xsd.parafOzeti.CTParafOzeti;

import java.net.URI;
import java.util.List;


public abstract class ParafOzeti {
    public abstract CTParafOzeti getParafOzeti();

    public abstract void ekle(OzetModu ozetModu, byte[] ozetDegeri, byte[] ozetDegeriSha512, URI uri);

    public abstract void ekle(OzetModu ozetModu, byte[] ozetDegeri, byte[] ozetDegeriSha512, URI uri, boolean hariciBilesenMi);

    public abstract List<CTReference> ozetleriAl();

    public abstract void kontrolEt();
}
