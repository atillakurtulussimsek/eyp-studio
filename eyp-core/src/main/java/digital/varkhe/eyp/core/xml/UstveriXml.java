package digital.varkhe.eyp.core.xml;

import digital.varkhe.eyp.core.model.Attachment;
import digital.varkhe.eyp.core.model.ContactInfo;
import digital.varkhe.eyp.core.model.Distribution;
import digital.varkhe.eyp.core.model.FilingCode;
import digital.varkhe.eyp.core.model.FinalMetadata;
import digital.varkhe.eyp.core.model.Identifier;
import digital.varkhe.eyp.core.model.Metadata;
import digital.varkhe.eyp.core.model.Party;
import digital.varkhe.eyp.core.model.RelatedDocument;
import digital.varkhe.eyp.core.model.Signer;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

import static digital.varkhe.eyp.core.pkg.EypConstants.NS_NIHAI_USTVERI;
import static digital.varkhe.eyp.core.pkg.EypConstants.NS_TIPLER;
import static digital.varkhe.eyp.core.pkg.EypConstants.NS_USTVERI;

/** Serialises Üstveri and Nihai Üstveri following the EY-Ustveri-2 / EY-NihaiUstveri-2 / EY-Tipler-2 sequences. */
public final class UstveriXml {
    private static final String T = "tipler";

    public static byte[] write(Metadata m) {
        XmlOut x = new XmlOut();
        x.start("", NS_USTVERI, "UstVeri").ns("", NS_USTVERI).ns(T, NS_TIPLER);
        t(x, "BelgeId", id(m.documentId()));
        t(x, "Konu", m.subject());
        t(x, "GuvenlikKodu", m.securityCode().name());
        t(x, "GuvenlikKoduGecerlilikTarihi", dateTime(m.securityCodeValidUntil()));
        t(x, "MimeTuru", m.mimeType());
        identifier(x, "OzId", m.ozId());
        x.start(T, NS_TIPLER, "DagitimListesi");
        for (Distribution d : m.distributions()) distribution(x, d);
        x.end();
        if (!m.attachments().isEmpty()) {
            x.start(T, NS_TIPLER, "Ekler");
            for (Attachment a : m.attachments()) attachment(x, a);
            x.end();
        }
        if (!m.relatedDocuments().isEmpty()) {
            x.start(T, NS_TIPLER, "Ilgiler");
            for (RelatedDocument r : m.relatedDocuments()) related(x, r);
            x.end();
        }
        t(x, "Dil", m.language());
        x.start(T, NS_TIPLER, "Olusturan");
        party(x, m.creator());
        x.end();
        if (!m.concerned().isEmpty()) {
            x.start(T, NS_TIPLER, "IlgiliListesi");
            for (Party p : m.concerned()) {
                x.start(T, NS_TIPLER, "Ilgili");
                party(x, p);
                x.end();
            }
            x.end();
        }
        t(x, "DosyaAdi", m.fileName());
        if (!m.filingCodes().isEmpty()) {
            x.start(T, NS_TIPLER, "SdpBilgisi");
            sdp(x, "AnaSdp", m.filingCodes().getFirst());
            if (m.filingCodes().size() > 1) {
                x.start(T, NS_TIPLER, "DigerSdpler");
                for (FilingCode f : m.filingCodes().subList(1, m.filingCodes().size())) sdp(x, "SdpListesi", f);
                x.end();
            }
            x.end();
        }
        x.start(T, NS_TIPLER, "DogrulamaBilgisi");
        t(x, "DogrulamaAdresi", m.verificationAddress());
        x.end();
        x.end();
        return x.finish();
    }

    public static byte[] write(FinalMetadata f) {
        XmlOut x = new XmlOut();
        x.start("", NS_NIHAI_USTVERI, "NihaiUstVeri").ns("", NS_NIHAI_USTVERI).ns(T, NS_TIPLER);
        t(x, "Tarih", dateTime(f.date()));
        t(x, "BelgeNo", f.documentNo());
        x.start(T, NS_TIPLER, "BelgeImzalar");
        for (Signer s : f.signers()) {
            x.start(T, NS_TIPLER, "Imza");
            x.start(T, NS_TIPLER, "Imzalayan");
            naturalPersonContent(x, s.signer());
            x.end();
            t(x, "Makam", s.office());
            t(x, "Amac", s.purpose());
            t(x, "Aciklama", s.description());
            t(x, "Tarih", dateTime(s.date()));
            x.end();
        }
        x.end();
        x.end();
        return x.finish();
    }

    // ------------------------------------------------------------------ parts

    private static void distribution(XmlOut x, Distribution d) {
        x.start(T, NS_TIPLER, "Dagitim");
        party(x, d.party());
        t(x, "Ivedilik", d.urgency().name());
        t(x, "DagitimTuru", d.type().name());
        t(x, "Miat", d.deadline() == null ? null : d.deadline().toString());
        if (!d.notIncludedAttachmentIds().isEmpty()) {
            x.start(T, NS_TIPLER, "KonulmamisEkListesi");
            for (UUID id : d.notIncludedAttachmentIds()) {
                x.start(T, NS_TIPLER, "KonulmamisEk");
                t(x, "EkId", id(id));
                x.end();
            }
            x.end();
        }
        x.end();
    }

    private static void attachment(XmlOut x, Attachment a) {
        x.start(T, NS_TIPLER, "Ek");
        idElement(x, a.id(), a.eYazismaId());
        t(x, "BelgeNo", a.documentNo());
        t(x, "Tur", a.type().name());
        t(x, "DosyaAdi", a.fileName());
        t(x, "MimeTuru", a.mimeType());
        t(x, "Ad", a.name());
        t(x, "SiraNo", Integer.toString(a.order()));
        t(x, "Aciklama", a.description());
        t(x, "Referans", a.reference() == null ? null : a.reference().toString());
        identifier(x, "OzId", a.ozId());
        t(x, "ImzaliMi", a.signed() == null ? null : a.signed().toString());
        x.end();
    }

    private static void related(XmlOut x, RelatedDocument r) {
        x.start(T, NS_TIPLER, "Ilgi");
        idElement(x, r.id(), r.eYazismaId());
        t(x, "BelgeNo", r.documentNo());
        t(x, "Tarih", dateTime(r.date()));
        t(x, "Etiket", r.label());
        t(x, "EkId", r.attachmentId() == null ? null : id(r.attachmentId()));
        t(x, "Ad", r.name());
        t(x, "Aciklama", r.description());
        identifier(x, "OzId", r.ozId());
        x.end();
    }

    private static void sdp(XmlOut x, String element, FilingCode f) {
        x.start(T, NS_TIPLER, element);
        t(x, "Kod", f.code());
        t(x, "Ad", f.name());
        t(x, "Aciklama", f.description());
        x.end();
    }

    private static void party(XmlOut x, Party p) {
        switch (p) {
            case Party.PublicInstitution k -> {
                x.start(T, NS_TIPLER, "KurumKurulus");
                t(x, "KKK", k.kkk());
                t(x, "Adi", k.name());
                contact(x, k.contact());
                t(x, "BirimKKK", k.unitKkk());
                x.end();
            }
            case Party.LegalEntity l -> {
                x.start(T, NS_TIPLER, "TuzelSahis");
                identifier(x, "Id", l.id());
                t(x, "Adi", l.name());
                contact(x, l.contact());
                x.end();
            }
            case Party.NaturalPerson n -> {
                x.start(T, NS_TIPLER, "GercekSahis");
                naturalPersonContent(x, n);
                x.end();
            }
        }
    }

    private static void naturalPersonContent(XmlOut x, Party.NaturalPerson n) {
        x.start(T, NS_TIPLER, "Kisi");
        t(x, "IlkAdi", n.person().firstName());
        t(x, "Soyadi", n.person().lastName());
        t(x, "IkinciAdi", n.person().middleName());
        t(x, "Unvan", n.person().title());
        t(x, "OnEk", n.person().prefix());
        x.end();
        t(x, "TCKN", n.tckn());
        t(x, "Gorev", n.duty());
        contact(x, n.contact());
    }

    private static void contact(XmlOut x, ContactInfo c) {
        if (c == null) return;
        x.start(T, NS_TIPLER, "IletisimBilgisi");
        t(x, "Telefon", c.phone());
        t(x, "TelefonDiger", c.otherPhone());
        t(x, "EPosta", c.email());
        t(x, "KepAdresi", c.kepAddress());
        t(x, "Faks", c.fax());
        t(x, "WebAdresi", c.webAddress());
        t(x, "Adres", c.address());
        t(x, "Il", c.city());
        t(x, "Ilce", c.district());
        t(x, "Ulke", c.country());
        x.end();
    }

    private static void idElement(XmlOut x, UUID id, boolean eYazismaId) {
        x.start(T, NS_TIPLER, "Id").attr("Value", id(id)).attr("EYazismaIdMi", Boolean.toString(eYazismaId)).end();
    }

    private static void identifier(XmlOut x, String element, Identifier id) {
        if (id == null) return;
        x.start(T, NS_TIPLER, element).attr("schemeID", id.schemeId()).text(id.value()).end();
    }

    private static void t(XmlOut x, String element, String value) {
        x.leaf(T, NS_TIPLER, element, value);
    }

    /** K.80: all Id values upper case. */
    static String id(UUID id) {
        return id.toString().toUpperCase(Locale.ROOT);
    }

    static String dateTime(OffsetDateTime t) {
        return t == null ? null : t.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private UstveriXml() {
    }
}
