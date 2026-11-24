package cbddo.eYazisma.tipler;


import urn.cbddo.eyazisma.schema.xsd.paketOzeti.CTDigestItem;
import urn.cbddo.eyazisma.schema.xsd.paketOzeti.CTDigestMethod;
import urn.cbddo.eyazisma.schema.xsd.paketOzeti.CTPaketOzeti;
import urn.cbddo.eyazisma.schema.xsd.paketOzeti.CTReference;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.net.URI;
import java.util.List;

class PaketOzetiInternal extends PaketOzeti {
    CTPaketOzeti _CTPaketOzeti;

    public PaketOzetiInternal() {
        _CTPaketOzeti = new CTPaketOzeti();
    }

    PaketOzetiInternal(CTPaketOzeti paketOzeti) {
        _CTPaketOzeti = paketOzeti;
    }

    @Override
    public CTPaketOzeti getPaketOzeti() {
        return _CTPaketOzeti;
    }

    @Override
    public void ekle(OzetModu ozetModu, byte[] ozetDegeri, byte[] ozetDegeriSha512, URI uri) {
        ekle(ozetModu, ozetDegeri, ozetDegeriSha512, uri, false);
    }

    @Override
    public void ekle(OzetModu ozetModu, byte[] ozetDegeri, byte[] ozetDegeriSha512, URI uri, Boolean hariciBilesenMi) {
        if (ozetModu == OzetModu.Yok) {
            throw new IllegalArgumentException("\"ozetModu\" değeri \"YOK\" olamaz.");
        }
        for (int i = 0; i <= _CTPaketOzeti.getReference().size() - 1; i++) {
            if (_CTPaketOzeti.getReference().get(i).getURI().compareToIgnoreCase(uri.toString()) == 0) {
                _CTPaketOzeti.getReference().remove(i);
                break;
            }
        }
        CTReference r = new CTReference();

        CTDigestItem digestItemSha512 = new CTDigestItem();
        digestItemSha512.setDigestValue(ozetDegeriSha512);
        CTDigestMethod digestMethodSha512 = new CTDigestMethod();
        digestMethodSha512.setAlgorithm(Araclar.ozetModuToString(OzetModu.SHA512));
        digestItemSha512.setDigestMethod(digestMethodSha512);

        JAXBElement<CTDigestItem> jaxbDigestItemSha512 = new JAXBElement<>(new QName("urn:dpt:eyazisma:schema:xsd:PaketOzeti-2","DigestItem"), CTDigestItem.class, null);
        jaxbDigestItemSha512.setValue(digestItemSha512);
        r.getContent().add(jaxbDigestItemSha512);

        CTDigestItem digestItemOzetModu = new CTDigestItem();
        digestItemOzetModu.setDigestValue(ozetDegeri);
        CTDigestMethod digestMethodForOzetModu = new CTDigestMethod();
        digestMethodForOzetModu.setAlgorithm(Araclar.ozetModuToString(ozetModu));
        digestItemOzetModu.setDigestMethod(digestMethodForOzetModu);

        JAXBElement<CTDigestItem> jaxbDigestItemOzetModu = new JAXBElement<>(new QName("urn:dpt:eyazisma:schema:xsd:PaketOzeti-2","DigestItem"), CTDigestItem.class, null);
        jaxbDigestItemOzetModu.setValue(digestItemOzetModu);
        r.getContent().add(jaxbDigestItemOzetModu);

        r.setURI(uri.toString());
        if (hariciBilesenMi) {
            r.setType(Araclar.HARICI_PAKET_BILESENI_REFERANS_TIPI);
        } else {
            r.setType(Araclar.DAHILI_PAKET_BILESENI_REFERANS_TIPI);
        }
        _CTPaketOzeti.getReference().add(r);
    }

    public List<CTReference> ozetleriAl() {
        return _CTPaketOzeti.getReference();
    }

    @Override
    public void kontrolEt() {
        XsdAraclar.kontrolEt(_CTPaketOzeti);
    }
}
