package digital.varkhe.eyp.core.validation;

import digital.varkhe.eyp.core.model.DigestAlgorithm;
import digital.varkhe.eyp.core.opc.OpcPackage;
import digital.varkhe.eyp.core.opc.Relationship;
import digital.varkhe.eyp.core.xml.SafeXml;
import digital.varkhe.eyp.core.xml.SchemaValidator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static digital.varkhe.eyp.core.pkg.EypConstants.*;

/**
 * Structural validation of an EYP 2.x package against the rule list of Teknik Rehber 2.1 (Ek-1, K.1–K.116).
 * Signature and seal are checked for presence and binding only; cryptographic validation belongs to eyp-sign.
 */
public final class PackageValidator {
    private static final Pattern GUID_UPPER =
            Pattern.compile("[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}");
    private static final String NS_CP = "http://schemas.openxmlformats.org/package/2006/metadata/core-properties";
    private static final String NS_DC = "http://purl.org/dc/elements/1.1/";

    private final OpcPackage pkg;
    private final List<Finding> findings = new ArrayList<>();

    private PackageValidator(OpcPackage pkg) {
        this.pkg = pkg;
    }

    public static List<Finding> validate(OpcPackage pkg) {
        PackageValidator v = new PackageValidator(pkg);
        v.run();
        return List.copyOf(v.findings);
    }

    // ------------------------------------------------------------------ main flow

    private void run() {
        Optional<OpcPackage.Part> coverLetter = single("K.2", "Üst Yazı", REL_USTYAZI);
        coverLetter.ifPresent(p -> {
            if (!p.name().startsWith(DIR_USTYAZI)) error("K.4", "Üst Yazı /UstYazi/ altında değil: " + p.name());
            if (!p.name().matches(".*/[^/]+\\.[A-Za-z0-9]+")) error("K.5", "Üst Yazı dosya adında uzantı yok: " + p.name());
            if (!startsWith(p.data(), "%PDF-")) error("K.6", "Üst Yazı PDF değil (PDF/A beklenir).");
        });

        Optional<OpcPackage.Part> ustveriPart = single("K.16", "Üstveri", REL_USTVERI);
        Ustveri ustveri = null;
        if (ustveriPart.isPresent()) {
            OpcPackage.Part p = ustveriPart.get();
            if (!p.name().equals(PART_USTVERI)) error("K.18", "Üstveri yolu " + PART_USTVERI + " olmalı: " + p.name());
            Document d = xmlComponent(p.data(), "Üstveri", "K.19", "K.20", "K.21", "K.22", SchemaValidator.Kind.USTVERI);
            if (d != null) ustveri = Ustveri.from(d);
        }

        String documentId = ustveri == null ? null : ustveri.documentId;
        if (documentId != null && !GUID_UPPER.matcher(documentId).matches()) {
            error("K.80", "BelgeId büyük harfli GUID olmalı: " + documentId);
        }
        if (ustveri != null) checkAttachments(ustveri);

        Map<String, byte[]> paketOzetiCoverage = new HashMap<>();
        Optional<OpcPackage.Part> paketOzeti = single("K.26", "Paket Özeti", REL_PAKET_OZETI);
        paketOzeti.ifPresent(p -> {
            if (!p.name().equals(PART_PAKET_OZETI)) error("K.28", "Paket Özeti yolu " + PART_PAKET_OZETI + " olmalı: " + p.name());
            Document d = xmlComponent(p.data(), "Paket Özeti", "K.29", "K.30", "K.31", "K.32", SchemaValidator.Kind.PAKET_OZETI);
            if (d != null) checkDigests(d, "Paket Özeti", "K.33", "K.34", "K.35", documentId, false, paketOzetiCoverage);
        });
        if (paketOzeti.isPresent()) {
            requireCovered(paketOzetiCoverage, coverLetter, "K.33", "Paket Özeti", "Üst Yazı");
            requireCovered(paketOzetiCoverage, ustveriPart, "K.33", "Paket Özeti", "Üstveri");
            for (Relationship r : pkg.relationships("/", REL_EK)) {
                requireCovered(paketOzetiCoverage, pkg.part(r.resolvedTarget()), "K.10", "Paket Özeti", "Ek " + r.resolvedTarget());
            }
            for (Relationship r : pkg.relationships("/", REL_IMZASIZ_EK)) {
                if (paketOzetiCoverage.containsKey(key(r.resolvedTarget()))) {
                    error("K.33", "İmzasız ek Paket Özeti'nde yer almamalı: " + r.resolvedTarget());
                }
            }
        }

        Optional<OpcPackage.Part> core = checkCore(documentId, ustveri);

        // Signature over Paket Özeti.
        List<Relationship> sigRels = pkg.relationships(PART_PAKET_OZETI, REL_IMZA);
        Optional<OpcPackage.Part> signature = Optional.empty();
        if (sigRels.isEmpty()) {
            if (!pkg.relationships("/", REL_IMZA).isEmpty()) {
                error("EYP-IMZA-ILISKI", "İmza ilişkisi paket kökünde; kaynağı Paket Özeti olmalı.");
            }
            warning("EYP-IMZA-YOK", "Paket imzasız (Elektronik İmza bileşeni yok).");
        } else if (sigRels.size() > 1) {
            error("EYP-IMZA-TEK", "Birden fazla imza bileşeni var; tüm imzacılar tek CAdES bloğunda olmalı.");
        } else {
            signature = pkg.part(sigRels.getFirst().resolvedTarget());
            if (signature.isEmpty()) error("EYP-IMZA-YOK", "İmza ilişkisinin hedefi pakette yok.");
            else if (!signature.get().name().equals(PART_IMZA)) error("EYP-IMZA-YOL", "İmza yolu " + PART_IMZA + " olmalı.");
        }
        if (signature.isPresent() && paketOzeti.isPresent() && !contains(signature.get().data(), paketOzeti.get().data())) {
            error("K.81", "İmza içindeki Paket Özeti, Paket Özeti bileşeniyle aynı değil (ayrık imza olabilir).");
        }

        // Nihai Üstveri.
        boolean signed = signature.isPresent();
        Optional<OpcPackage.Part> nihaiUstveri = signed
                ? single("K.82", "Nihai Üstveri", REL_NIHAI_USTVERI)
                : optionalSingle("K.82", "Nihai Üstveri", REL_NIHAI_USTVERI);
        nihaiUstveri.ifPresent(p -> {
            if (!p.name().equals(PART_NIHAI_USTVERI)) error("K.84", "Nihai Üstveri yolu " + PART_NIHAI_USTVERI + " olmalı.");
            xmlComponent(p.data(), "Nihai Üstveri", "K.85", "K.86", "K.87", "K.88", SchemaValidator.Kind.NIHAI_USTVERI);
        });

        // Nihai Özet.
        Optional<OpcPackage.Part> nihaiOzet = signed
                ? single("K.98", "Nihai Özet", REL_NIHAI_OZET)
                : optionalSingle("K.98", "Nihai Özet", REL_NIHAI_OZET);
        Map<String, byte[]> nihaiCoverage = new HashMap<>();
        nihaiOzet.ifPresent(p -> {
            if (!p.name().equals(PART_NIHAI_OZET)) error("K.38", "Nihai Özet yolu " + PART_NIHAI_OZET + " olmalı.");
            Document d = xmlComponent(p.data(), "Nihai Özet", "K.39", "K.40", "K.41", "K.42", SchemaValidator.Kind.NIHAI_OZET);
            if (d != null) checkDigests(d, "Nihai Özet", "K.43", "K.44", "K.45", documentId, true, nihaiCoverage);
        });
        if (nihaiOzet.isPresent()) {
            requireCovered(nihaiCoverage, coverLetter, "K.43", "Nihai Özet", "Üst Yazı");
            requireCovered(nihaiCoverage, ustveriPart, "K.43", "Nihai Özet", "Üstveri");
            requireCovered(nihaiCoverage, core, "K.43", "Nihai Özet", "Core");
            requireCovered(nihaiCoverage, paketOzeti, "K.43", "Nihai Özet", "Paket Özeti");
            requireCovered(nihaiCoverage, signature, "K.43", "Nihai Özet", "Elektronik İmza");
            requireCovered(nihaiCoverage, nihaiUstveri, "K.43", "Nihai Özet", "Nihai Üstveri");
            for (Relationship r : pkg.relationships("/", REL_EK)) {
                requireCovered(nihaiCoverage, pkg.part(r.resolvedTarget()), "K.43", "Nihai Özet", "Ek " + r.resolvedTarget());
            }
        }

        // Seal over Nihai Özet (mandatory in 2.x, guide §6.2.8 / K.100).
        List<Relationship> sealRels = pkg.relationships(PART_NIHAI_OZET, REL_MUHUR);
        if (signed) {
            if (sealRels.isEmpty()) {
                error("EYP-MUHUR-ZORUNLU", "2.x pakette Elektronik Mühür bileşeni zorunlu; bulunamadı.");
            } else if (sealRels.size() > 1) {
                error("EYP-MUHUR-TEK", "Birden fazla mühür bileşeni var; seri/paralel mühüre izin yok.");
            } else {
                Optional<OpcPackage.Part> seal = pkg.part(sealRels.getFirst().resolvedTarget());
                if (seal.isEmpty()) error("EYP-MUHUR-ZORUNLU", "Mühür ilişkisinin hedefi pakette yok.");
                else {
                    if (!seal.get().name().equals(PART_MUHUR)) error("EYP-MUHUR-YOL", "Mühür yolu " + PART_MUHUR + " olmalı.");
                    if (nihaiOzet.isPresent() && !contains(seal.get().data(), nihaiOzet.get().data())) {
                        error("K.100", "Mühür içindeki Nihai Özet, Nihai Özet bileşeniyle aynı değil.");
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------ attachments

    private void checkAttachments(Ustveri u) {
        Map<String, Relationship> signedRels = new HashMap<>();
        Map<String, Relationship> unsignedRels = new HashMap<>();
        for (Relationship r : pkg.relationships("/", REL_EK)) {
            if (!r.resolvedTarget().startsWith(DIR_EKLER)) error("K.12", "Ek /Ekler/ altında değil: " + r.resolvedTarget());
            if (pkg.part(r.resolvedTarget()).isEmpty()) error("K.1", "İlişki hedefi pakette yok: " + r.resolvedTarget());
            signedRels.put(r.id().toUpperCase(Locale.ROOT), r);
        }
        for (Relationship r : pkg.relationships("/", REL_IMZASIZ_EK)) {
            if (!r.resolvedTarget().startsWith(DIR_IMZASIZ_EKLER)) {
                error("K.12", "İmzasız ek /ImzasizEkler/ altında değil: " + r.resolvedTarget());
            }
            unsignedRels.put(r.id().toUpperCase(Locale.ROOT), r);
        }

        Set<String> ids = new HashSet<>();
        Set<String> declaredDed = new HashSet<>();
        for (Ustveri.Ek ek : u.ekler) {
            if (!ids.add(ek.id.toUpperCase(Locale.ROOT))) error("K.61", "Tekrarlanan Ek Id: " + ek.id);
            if (!GUID_UPPER.matcher(ek.id).matches()) error("K.80", "Ek Id büyük harfli GUID olmalı: " + ek.id);
            if (!"DED".equals(ek.tur)) continue;
            declaredDed.add(ek.id.toUpperCase(Locale.ROOT));
            boolean present = signedRels.containsKey((ID_PREFIX_EK + ek.id).toUpperCase(Locale.ROOT))
                    || unsignedRels.containsKey((ID_PREFIX_IMZASIZ_EK + ek.id).toUpperCase(Locale.ROOT));
            if (!present && !u.notIncluded.contains(ek.id.toUpperCase(Locale.ROOT))) {
                error("K.23", "Üstveri'de DED olarak belirtilen ek pakette yok: " + ek.id);
            }
        }
        for (String relId : signedRels.keySet()) {
            String id = relId.substring(ID_PREFIX_EK.length());
            if (!relId.startsWith(ID_PREFIX_EK.toUpperCase(Locale.ROOT)) || !declaredDed.contains(id)) {
                error("K.24", "Paketteki ek Üstveri'de DED olarak belirtilmemiş: " + signedRels.get(relId).resolvedTarget());
            }
        }
        for (String relId : unsignedRels.keySet()) {
            String id = relId.substring(Math.min(relId.length(), ID_PREFIX_IMZASIZ_EK.length()));
            if (!relId.startsWith(ID_PREFIX_IMZASIZ_EK.toUpperCase(Locale.ROOT)) || !declaredDed.contains(id)) {
                error("K.24", "Paketteki imzasız ek Üstveri'de DED olarak belirtilmemiş: " + unsignedRels.get(relId).resolvedTarget());
            }
        }
        for (String ilgiId : u.ilgiIds) {
            if (!ids.add(ilgiId.toUpperCase(Locale.ROOT))) error("K.61", "Tekrarlanan Id (ek/ilgi): " + ilgiId);
            if (!GUID_UPPER.matcher(ilgiId).matches()) error("K.80", "İlgi Id büyük harfli GUID olmalı: " + ilgiId);
        }
        for (String ekId : u.ilgiEkIds) {
            boolean found = u.ekler.stream().anyMatch(e -> e.id.equalsIgnoreCase(ekId));
            if (!found) error("K.25", "İlgi'de belirtilen EkId'ye sahip ek yok: " + ekId);
        }
    }

    // ------------------------------------------------------------------ digests

    private void checkDigests(Document d, String what, String coverageRule, String idRule, String typeRule,
                              String documentId, boolean finalDigest, Map<String, byte[]> coverage) {
        Element root = d.getDocumentElement();
        String id = root.getAttribute("Id");
        if (documentId != null && !documentId.equals(id)) {
            error(idRule, what + " Id değeri BelgeId ile aynı olmalı (" + id + ").");
        }
        NodeList refs = d.getElementsByTagNameNS(NS_PAKET_OZETI, "Reference");
        for (int i = 0; i < refs.getLength(); i++) {
            Element ref = (Element) refs.item(i);
            String uri = ref.getAttribute("URI");
            String type = ref.getAttribute("Type");
            boolean external = REF_TYPE_EXTERNAL.equals(type);
            if (!external && !REF_TYPE_INTERNAL.equals(type)) {
                error(typeRule, what + " Reference Type değeri geçersiz: '" + type + "' (" + uri + ")");
            }
            if (external) {
                if (finalDigest) error("K.45", "Nihai Özet harici bileşen özeti içeremez: " + uri);
                continue;
            }
            List<Element> items = children(ref, "DigestItem");
            if (items.size() != 2) {
                error(coverageRule, what + ": her referansta iki DigestItem olmalı (" + uri + ").");
                continue;
            }
            Set<DigestAlgorithm> algs = new HashSet<>();
            Optional<OpcPackage.Part> part = pkg.part(uri);
            if (part.isEmpty()) {
                error(coverageRule, what + " pakette olmayan bileşene referans veriyor: " + uri);
                continue;
            }
            for (Element item : items) {
                Element method = children(item, "DigestMethod").stream().findFirst().orElse(null);
                Element value = children(item, "DigestValue").stream().findFirst().orElse(null);
                if (method == null || value == null) {
                    error(coverageRule, what + ": DigestMethod/DigestValue eksik (" + uri + ").");
                    continue;
                }
                Optional<DigestAlgorithm> alg = DigestAlgorithm.fromUri(method.getAttribute("Algorithm"));
                if (alg.isEmpty()) {
                    error(coverageRule, what + ": desteklenmeyen özet algoritması " + method.getAttribute("Algorithm"));
                    continue;
                }
                algs.add(alg.get());
                byte[] expected;
                try {
                    expected = Base64.getMimeDecoder().decode(value.getTextContent().trim());
                } catch (IllegalArgumentException e) {
                    error(coverageRule, what + ": DigestValue Base64 değil (" + uri + ").");
                    continue;
                }
                if (!Arrays.equals(expected, alg.get().digest(part.get().data()))) {
                    error(coverageRule, what + ": özet uyuşmuyor (" + alg.get() + ", " + uri + ").");
                }
            }
            if (algs.size() != 2) error(coverageRule, what + ": iki DigestItem farklı algoritmalarla olmalı (" + uri + ").");
            if (!algs.contains(DigestAlgorithm.SHA512)) error(coverageRule, what + ": özetlerden biri SHA-512 olmalı (" + uri + ").");
            coverage.put(key(part.get().name()), part.get().data());
        }
    }

    private void requireCovered(Map<String, byte[]> coverage, Optional<OpcPackage.Part> part, String rule, String where, String what) {
        if (part.isPresent() && !coverage.containsKey(key(part.get().name()))) {
            error(rule, where + " " + what + " özetini içermiyor.");
        }
    }

    // ------------------------------------------------------------------ core

    private Optional<OpcPackage.Part> checkCore(String documentId, Ustveri u) {
        List<Relationship> rels = pkg.relationships("/", REL_CORE);
        if (rels.size() != 1) {
            error("K.68", "Paket bir adet Core bileşeni içermeli (bulunan: " + rels.size() + ").");
            return Optional.empty();
        }
        Optional<OpcPackage.Part> core = pkg.part(rels.getFirst().resolvedTarget());
        if (core.isEmpty()) {
            error("K.68", "Core ilişkisinin hedefi pakette yok.");
            return core;
        }
        Document d;
        try {
            d = SafeXml.parse(core.get().data());
        } catch (SAXException e) {
            error("K.1", "Core XML okunamadı: " + e.getMessage());
            return core;
        }
        String identifier = text(d, NS_DC, "identifier");
        String subject = text(d, NS_DC, "subject");
        String category = text(d, NS_CP, "category");
        String contentType = text(d, NS_CP, "contentType");
        String version = text(d, NS_CP, "version");
        if (documentId != null && !documentId.equals(identifier)) error("K.68", "Core identifier BelgeId ile aynı olmalı: " + identifier);
        if (u != null && !u.konu.equals(subject)) error("K.69", "Core subject Üstveri Konu ile aynı olmalı.");
        if (!CORE_CATEGORY.equals(category)) error("K.71", "Core category RESMIYAZISMA olmalı: " + category);
        if (!CORE_CONTENT_TYPE.equals(contentType)) error("K.73", "Core contentType application/eyazisma olmalı: " + contentType);
        if (version == null || !version.matches("\\d+\\.\\d+")) error("EYP-CORE-SURUM", "Core version eksik veya hatalı: " + version);
        else if (!version.startsWith("2.")) warning("EYP-CORE-SURUM", "Paket sürümü " + version + "; bu doğrulayıcı 2.x kurallarını uygular.");
        return core;
    }

    // ------------------------------------------------------------------ XML component checks

    private Document xmlComponent(byte[] data, String what, String schemaRule, String encodingRule, String dtdRule,
                                  String nsRule, SchemaValidator.Kind kind) {
        if (!utf8or16(data)) error(encodingRule, what + " UTF-8 veya UTF-16 olmalı.");
        String head = new String(data, 0, Math.min(data.length, 4096), StandardCharsets.UTF_8);
        if (head.contains("<!DOCTYPE")) {
            error(dtdRule, what + " DTD içermemeli.");
            return null;
        }
        Document d;
        try {
            d = SafeXml.parse(data);
        } catch (SAXException e) {
            error(schemaRule, what + " XML okunamadı: " + e.getMessage());
            return null;
        }
        if (usesXmlOrXsiNamespace(d.getDocumentElement())) error(nsRule, what + " xml/xsi namespace'li öğe içermemeli.");
        for (String e : SchemaValidator.validate(kind, data)) error(schemaRule, what + " şemaya uygun değil: " + e);
        return d;
    }

    private static boolean usesXmlOrXsiNamespace(Element e) {
        var attrs = e.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            String ns = attrs.item(i).getNamespaceURI();
            String local = attrs.item(i).getLocalName();
            // xsi:nil is allowed: the schemas declare nillable elements and the official API writes xsi:nil="true".
            if ("http://www.w3.org/2001/XMLSchema-instance".equals(ns) && "nil".equals(local)) continue;
            if ("http://www.w3.org/XML/1998/namespace".equals(ns) || "http://www.w3.org/2001/XMLSchema-instance".equals(ns)) {
                return true;
            }
        }
        for (Node c = e.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (c instanceof Element ce && usesXmlOrXsiNamespace(ce)) return true;
        }
        return false;
    }

    private static boolean utf8or16(byte[] data) {
        if (data.length >= 2 && ((data[0] == (byte) 0xFE && data[1] == (byte) 0xFF) || (data[0] == (byte) 0xFF && data[1] == (byte) 0xFE))) {
            return true;
        }
        String head = new String(data, 0, Math.min(data.length, 200), StandardCharsets.ISO_8859_1);
        var m = Pattern.compile("encoding=[\"']([^\"']+)[\"']").matcher(head);
        if (!m.find()) return true; // XML default is UTF-8
        String enc = m.group(1).toUpperCase(Locale.ROOT);
        return enc.equals("UTF-8") || enc.equals("UTF-16");
    }

    // ------------------------------------------------------------------ helpers

    private Optional<OpcPackage.Part> single(String rule, String what, String relType) {
        List<Relationship> rels = pkg.relationships("/", relType);
        if (rels.size() != 1) {
            error(rule, "Pakette bir adet " + what + " bileşeni olmalı (bulunan: " + rels.size() + ").");
            return rels.isEmpty() ? Optional.empty() : pkg.part(rels.getFirst().resolvedTarget());
        }
        Optional<OpcPackage.Part> p = pkg.part(rels.getFirst().resolvedTarget());
        if (p.isEmpty()) error(rule, what + " ilişkisinin hedefi pakette yok: " + rels.getFirst().resolvedTarget());
        return p;
    }

    private Optional<OpcPackage.Part> optionalSingle(String rule, String what, String relType) {
        return pkg.relationships("/", relType).isEmpty() ? Optional.empty() : single(rule, what, relType);
    }

    private static List<Element> children(Element parent, String localName) {
        List<Element> out = new ArrayList<>();
        for (Node c = parent.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (c instanceof Element e && localName.equals(e.getLocalName()) && NS_PAKET_OZETI.equals(e.getNamespaceURI())) out.add(e);
        }
        return out;
    }

    private static String text(Document d, String ns, String local) {
        NodeList nl = d.getElementsByTagNameNS(ns, local);
        return nl.getLength() == 0 ? null : nl.item(0).getTextContent().trim();
    }

    private static boolean startsWith(byte[] data, String prefix) {
        byte[] p = prefix.getBytes(StandardCharsets.US_ASCII);
        if (data.length < p.length) return false;
        for (int i = 0; i < p.length; i++) if (data[i] != p[i]) return false;
        return true;
    }

    /** True if {@code needle} occurs in {@code haystack} (attached CAdES carries the signed content verbatim). */
    static boolean contains(byte[] haystack, byte[] needle) {
        if (needle.length == 0) return true;
        outer:
        for (int i = 0; i <= haystack.length - needle.length; i++) {
            for (int j = 0; j < needle.length; j++) if (haystack[i + j] != needle[j]) continue outer;
            return true;
        }
        return false;
    }

    private static String key(String partName) {
        return partName.toLowerCase(Locale.ROOT);
    }

    private void error(String rule, String message) {
        findings.add(new Finding(rule, Severity.ERROR, message));
    }

    private void warning(String rule, String message) {
        findings.add(new Finding(rule, Severity.WARNING, message));
    }

    /** The few Üstveri values the structural checks need. */
    private static final class Ustveri {
        record Ek(String id, String tur) {
        }

        String documentId;
        String konu = "";
        final List<Ek> ekler = new ArrayList<>();
        final List<String> ilgiIds = new ArrayList<>();
        final List<String> ilgiEkIds = new ArrayList<>();
        final Set<String> notIncluded = new HashSet<>();

        static Ustveri from(Document d) {
            Ustveri u = new Ustveri();
            Element root = d.getDocumentElement();
            u.documentId = childText(root, "BelgeId");
            String konu = childText(root, "Konu");
            u.konu = konu == null ? "" : konu;
            for (Element ek : all(d, "Ek")) {
                Element id = first(ek, "Id");
                u.ekler.add(new Ek(id == null ? "" : id.getAttribute("Value"), childText(ek, "Tur")));
            }
            for (Element ilgi : all(d, "Ilgi")) {
                Element id = first(ilgi, "Id");
                if (id != null) u.ilgiIds.add(id.getAttribute("Value"));
                String ekId = childText(ilgi, "EkId");
                if (ekId != null) u.ilgiEkIds.add(ekId);
            }
            for (Element k : all(d, "KonulmamisEk")) {
                String ekId = childText(k, "EkId");
                if (ekId != null) u.notIncluded.add(ekId.toUpperCase(Locale.ROOT));
            }
            return u;
        }

        private static List<Element> all(Document d, String local) {
            NodeList nl = d.getElementsByTagNameNS(NS_TIPLER, local);
            List<Element> out = new ArrayList<>();
            for (int i = 0; i < nl.getLength(); i++) out.add((Element) nl.item(i));
            return out;
        }

        private static Element first(Element parent, String local) {
            for (Node c = parent.getFirstChild(); c != null; c = c.getNextSibling()) {
                if (c instanceof Element e && local.equals(e.getLocalName())) return e;
            }
            return null;
        }

        private static String childText(Element parent, String local) {
            Element e = first(parent, local);
            return e == null ? null : e.getTextContent().trim();
        }
    }
}
