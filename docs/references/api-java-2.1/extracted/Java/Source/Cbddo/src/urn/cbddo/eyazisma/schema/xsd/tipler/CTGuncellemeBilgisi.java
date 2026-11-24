package urn.cbddo.eyazisma.schema.xsd.tipler;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name="GuncellemeBilgisi",namespace = "urn:dpt:eyazisma:schema:xsd:Tipler-2")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GuncellemeBilgisi", propOrder = {
        "guncellemeler"
})
public class CTGuncellemeBilgisi {
    @XmlElement(name = "Guncellemeler", required = true)
    protected List<CTGuncelleme> guncellemeler;

    public List<CTGuncelleme> getGuncellemeler() {
        return guncellemeler;
    }

    public void setGuncellemeler(List<CTGuncelleme> guncellemeler) {
        this.guncellemeler = guncellemeler;
    }
}
