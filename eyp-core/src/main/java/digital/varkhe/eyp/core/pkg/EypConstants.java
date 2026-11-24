package digital.varkhe.eyp.core.pkg;

/**
 * Names fixed by the e-Yazışma Teknik Rehberi 2.1 and the official API 2.1 ({@code cbddo.eYazisma.tipler.Araclar}).
 * Do not add values here that are not taken from one of those sources.
 */
public final class EypConstants {

    public static final String REL_BASE = "http://eyazisma.dpt/iliskiler/";
    public static final String REL_USTYAZI = REL_BASE + "ustyazi";
    public static final String REL_EK = REL_BASE + "ek";
    public static final String REL_IMZASIZ_EK = REL_BASE + "imzasizEk";
    public static final String REL_USTVERI = REL_BASE + "ustveri";
    public static final String REL_NIHAI_USTVERI = REL_BASE + "nihaiustveri";
    public static final String REL_PARAF_OZETI = REL_BASE + "parafozeti";
    public static final String REL_PARAF_IMZA = REL_BASE + "parafimzacades";
    public static final String REL_PAKET_OZETI = REL_BASE + "paketozeti";
    public static final String REL_IMZA = REL_BASE + "imzacades";
    public static final String REL_NIHAI_OZET = REL_BASE + "nihaiozet";
    public static final String REL_MUHUR = REL_BASE + "muhurcades";
    public static final String REL_BELGE_HEDEF = REL_BASE + "belgehedef";
    public static final String REL_CORE =
            "http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties";

    public static final String PART_USTVERI = "/Ustveri/Ustveri.xml";
    public static final String PART_NIHAI_USTVERI = "/NihaiUstveri/NihaiUstveri.xml";
    public static final String PART_PAKET_OZETI = "/PaketOzeti/PaketOzeti.xml";
    public static final String PART_PARAF_OZETI = "/ParafOzeti/ParafOzeti.xml";
    public static final String PART_NIHAI_OZET = "/NihaiOzet/NihaiOzet.xml";
    public static final String PART_IMZA = "/Imzalar/ImzaCades.imz";
    public static final String PART_PARAF_IMZA = "/Paraflar/ParafImzaCades.imz";
    public static final String PART_MUHUR = "/Muhur/MuhurCades.imz";
    public static final String DIR_USTYAZI = "/UstYazi/";
    public static final String DIR_EKLER = "/Ekler/";
    public static final String DIR_IMZASIZ_EKLER = "/ImzasizEkler/";
    /** Core part name used by guide 2.1 §10 example; the reader accepts any name reached via the core relationship. */
    public static final String DIR_CORE = "/package/services/metadata/core-properties/";

    public static final String ID_USTYAZI = "IdUstYazi";
    public static final String ID_USTVERI = "IdUstveri";
    public static final String ID_NIHAI_USTVERI = "IdNihaiUstveri";
    public static final String ID_PAKET_OZETI = "IdPaketOzeti";
    public static final String ID_NIHAI_OZET = "IdNihaiOzet";
    public static final String ID_IMZA = "IdImzaCades";
    public static final String ID_MUHUR = "IdMuhurCades";
    public static final String ID_PREFIX_EK = "IdEk_";
    public static final String ID_PREFIX_IMZASIZ_EK = "IdImzasizEk_";

    public static final String REF_TYPE_INTERNAL = "http://eyazisma.dpt/bilesen#dahili";
    public static final String REF_TYPE_EXTERNAL = "http://eyazisma.dpt/bilesen#harici";

    public static final String NS_USTVERI = "urn:dpt:eyazisma:schema:xsd:Ustveri-2";
    public static final String NS_TIPLER = "urn:dpt:eyazisma:schema:xsd:Tipler-2";
    public static final String NS_PAKET_OZETI = "urn:dpt:eyazisma:schema:xsd:PaketOzeti-2";
    public static final String NS_NIHAI_OZET = "urn:dpt:eyazisma:schema:xsd:NihaiOzet-2";
    public static final String NS_NIHAI_USTVERI = "urn:dpt:eyazisma:schema:xsd:NihaiUstveri-2";

    public static final String CORE_CATEGORY = "RESMIYAZISMA";
    public static final String CORE_CONTENT_TYPE = "application/eyazisma";
    /** Official API 2.1 writes "2.0" for normal (non-update) packages ({@code Araclar.PAKET_VERSIYON}). */
    public static final String CORE_VERSION = "2.0";

    public static final String MIME_XML = "application/xml";
    public static final String MIME_OCTET = "application/octet-stream";
    public static final String MIME_RELS = "application/vnd.openxmlformats-package.relationships+xml";
    public static final String MIME_CORE = "application/vnd.openxmlformats-package.core-properties+xml";

    private EypConstants() {
    }
}
