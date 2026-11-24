package urn.cbddo.eyazisma.schema.xsd.tipler;

import urn.cbddo.eyazisma.schema.xsd.kodguvenlik.STKodGuvenlikKodu;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;

@XmlRootElement(name="GuvenlikKoduDegisiklikBilgisi",namespace = "urn:dpt:eyazisma:schema:xsd:Tipler-2")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GuvenlikKoduDegisiklikBilgisi", propOrder = {
        "yeniGizlilikDerecesi",
        "degistirmeTarihi",
        "aciklama",
        "komisyonKarariBelgeNo",
        "komisyonKarariBelgeId"
})
public class CTGuvenlikKoduDegisiklikBilgisi {
    @XmlElement(name = "YeniGizlilikDerecesi", required = true)
    protected STKodGuvenlikKodu yeniGizlilikDerecesi;

    @XmlElement(name = "DegistirmeTarihi", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar degistirmeTarihi;

    @XmlElement(name = "Aciklama", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String aciklama;

    @XmlElement(name = "KomisyonKarariBelgeNo", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String komisyonKarariBelgeNo;

    @XmlElement(name = "KomisyonKarariBelgeId", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String komisyonKarariBelgeId;

    public STKodGuvenlikKodu getYeniGizlilikDerecesi() {
        return yeniGizlilikDerecesi;
    }

    public void setYeniGizlilikDerecesi(STKodGuvenlikKodu yeniGizlilikDerecesi) {
        this.yeniGizlilikDerecesi = yeniGizlilikDerecesi;
    }

    public XMLGregorianCalendar getDegistirmeTarihi() {
        return degistirmeTarihi;
    }

    public void setDegistirmeTarihi(XMLGregorianCalendar degistirmeTarihi) {
        this.degistirmeTarihi = degistirmeTarihi;
    }

    public String getAciklama() {
        return aciklama;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama;
    }

    public String getKomisyonKarariBelgeNo() {
        return komisyonKarariBelgeNo;
    }

    public void setKomisyonKarariBelgeNo(String komisyonKarariBelgeNo) {
        this.komisyonKarariBelgeNo = komisyonKarariBelgeNo;
    }

    public String getKomisyonKarariBelgeId() {
        return komisyonKarariBelgeId;
    }

    public void setKomisyonKarariBelgeId(String komisyonKarariBelgeId) {
        this.komisyonKarariBelgeId = komisyonKarariBelgeId;
    }
}
