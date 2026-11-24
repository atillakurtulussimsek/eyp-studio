package cbddo.eYazisma.tipler;

import urn.cbddo.eyazisma.schema.xsd.nihaiOzet.CTNihaiOzet;
import urn.cbddo.eyazisma.schema.xsd.paketOzeti.CTReference;

import java.net.URI;
import java.util.List;


public abstract class NihaiOzet {
	public abstract CTNihaiOzet getNihaiOzet();

	public abstract void ekle(OzetModu ozetModu, byte[] ozetDegeri, byte[] ozetDegeriSha512, URI uri);

	public abstract List<CTReference> ozetleriAl();

	public abstract void kontrolEt();
}
