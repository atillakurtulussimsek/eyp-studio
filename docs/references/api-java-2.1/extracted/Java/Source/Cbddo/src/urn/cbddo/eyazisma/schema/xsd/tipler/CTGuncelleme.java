package urn.cbddo.eyazisma.schema.xsd.tipler;

import urn.cbddo.eyazisma.schema.xsd.guncellemeturu.STGuncellemeTuru;

import javax.xml.bind.annotation.*;

@XmlRootElement(name="Guncelleme",namespace = "urn:dpt:eyazisma:schema:xsd:Tipler-2")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Guncelleme", propOrder = {
        "guvenlikKoduDegisiklikBilgisi",
        "guncellemeTuru"
})
public class CTGuncelleme {

    @XmlElement(name = "GuvenlikKoduDegisiklikBilgisi",required = true)
    protected CTGuvenlikKoduDegisiklikBilgisi guvenlikKoduDegisiklikBilgisi;

    @XmlElement(name = "GuncellemeTuru", required = true)
    protected STGuncellemeTuru guncellemeTuru;

    public CTGuvenlikKoduDegisiklikBilgisi getGuvenlikKoduDegisiklikBilgisi() {
        return guvenlikKoduDegisiklikBilgisi;
    }

    public void setGuvenlikKoduDegisiklikBilgisi(CTGuvenlikKoduDegisiklikBilgisi guvenlikKoduDegisiklikBilgisi) {
        this.guvenlikKoduDegisiklikBilgisi = guvenlikKoduDegisiklikBilgisi;
    }

    public STGuncellemeTuru getGuncellemeTuru() {
        return guncellemeTuru;
    }

    public void setGuncellemeTuru(STGuncellemeTuru guncellemeTuru) {
        this.guncellemeTuru = guncellemeTuru;
    }
}
