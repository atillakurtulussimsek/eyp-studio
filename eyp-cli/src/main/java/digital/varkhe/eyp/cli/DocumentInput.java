package digital.varkhe.eyp.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import digital.varkhe.eyp.core.model.Attachment;
import digital.varkhe.eyp.core.model.AttachmentType;
import digital.varkhe.eyp.core.model.ContactInfo;
import digital.varkhe.eyp.core.model.Distribution;
import digital.varkhe.eyp.core.model.DistributionType;
import digital.varkhe.eyp.core.model.FilingCode;
import digital.varkhe.eyp.core.model.Identifier;
import digital.varkhe.eyp.core.model.Metadata;
import digital.varkhe.eyp.core.model.Party;
import digital.varkhe.eyp.core.model.Person;
import digital.varkhe.eyp.core.model.RelatedDocument;
import digital.varkhe.eyp.core.model.SecurityCode;
import digital.varkhe.eyp.core.model.Urgency;
import digital.varkhe.eyp.core.opc.PartNames;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Reads the JSON document description used by {@code eyp olustur}. Keys are Turkish (user facing); see
 * eyp-cli/README.md for the format. File paths are resolved relative to the JSON file.
 */
final class DocumentInput {
    static final ZoneId TR = ZoneId.of("Europe/Istanbul");

    final Metadata metadata;
    final byte[] coverLetter;
    final Map<UUID, byte[]> attachments;

    private DocumentInput(Metadata metadata, byte[] coverLetter, Map<UUID, byte[]> attachments) {
        this.metadata = metadata;
        this.coverLetter = coverLetter;
        this.attachments = attachments;
    }

    static final class InputException extends Exception {
        InputException(String message) {
            super(message);
        }
    }

    static DocumentInput read(Path json) throws IOException, InputException {
        JsonNode root = new ObjectMapper().readTree(json.toFile());
        Path base = json.toAbsolutePath().getParent();

        Path coverPath = base.resolve(req(root, "ustYazi").asText());
        byte[] cover = Files.readAllBytes(coverPath);
        String coverName = sanitize(coverPath.getFileName().toString());

        Map<UUID, byte[]> data = new LinkedHashMap<>();
        List<Attachment> attachments = new ArrayList<>();
        Set<String> usedNames = new HashSet<>();
        int order = 1;
        for (JsonNode e : list(root, "ekler")) {
            Path p = base.resolve(req(e, "dosya").asText());
            String name = sanitize(p.getFileName().toString());
            if (!usedNames.add(name.toLowerCase(Locale.ROOT))) throw new InputException("Aynı adlı iki ek var: " + name);
            UUID id = UUID.randomUUID();
            boolean signed = !e.has("imzali") || e.get("imzali").asBoolean();
            attachments.add(new Attachment(id, false, opt(e, "belgeNo"), AttachmentType.DED, name, mimeOf(name),
                    opt(e, "ad"), order++, opt(e, "aciklama"), null, null, signed));
            data.put(id, Files.readAllBytes(p));
        }

        List<RelatedDocument> related = new ArrayList<>();
        for (JsonNode i : list(root, "ilgiler")) {
            OffsetDateTime date = i.has("tarih") ? LocalDate.parse(i.get("tarih").asText()).atStartOfDay(TR).toOffsetDateTime() : null;
            related.add(new RelatedDocument(UUID.randomUUID(), false, opt(i, "belgeNo"), date, req(i, "etiket").asText(),
                    null, opt(i, "ad"), opt(i, "aciklama"), null));
        }

        List<Distribution> distributions = new ArrayList<>();
        for (JsonNode d : list(root, "dagitimlar")) {
            distributions.add(new Distribution(party(d),
                    enumOf(Urgency.class, d, "ivedilik", "NRM"),
                    enumOf(DistributionType.class, d, "dagitimTuru", "GRG")));
        }
        if (distributions.isEmpty()) throw new InputException("'dagitimlar' en az bir alıcı içermeli.");

        List<FilingCode> filing = new ArrayList<>();
        for (JsonNode s : list(root, "sdp")) filing.add(new FilingCode(req(s, "kod").asText(), req(s, "ad").asText(), opt(s, "aciklama")));

        UUID documentId = root.has("belgeId") ? UUID.fromString(root.get("belgeId").asText()) : UUID.randomUUID();
        Metadata m = new Metadata(documentId, req(root, "konu").asText(),
                enumOf(SecurityCode.class, root, "guvenlikKodu", "YOK"), null, mimeOf(coverName), null,
                distributions, attachments, related, opt(root, "dil"), party(req(root, "olusturan")), null,
                coverName, filing, req(root, "dogrulamaAdresi").asText());
        return new DocumentInput(m, cover, data);
    }

    private static Party party(JsonNode n) throws InputException {
        if (n.has("kurumKurulus")) {
            JsonNode k = n.get("kurumKurulus");
            return new Party.PublicInstitution(req(k, "kkk").asText(), opt(k, "adi"), contact(k), opt(k, "birimKkk"));
        }
        if (n.has("tuzelSahis")) {
            JsonNode t = n.get("tuzelSahis");
            String scheme = opt(t, "idTuru") == null ? "VKN" : opt(t, "idTuru").toUpperCase(Locale.ROOT);
            String id = req(t, "id").asText().trim();
            if (scheme.equals("VKN") && !id.matches("\\d{10}")) throw new InputException("VKN 10 haneli olmalı: " + id);
            return new Party.LegalEntity(new Identifier(id, scheme), opt(t, "adi"), contact(t));
        }
        if (n.has("gercekSahis")) {
            JsonNode g = n.get("gercekSahis");
            return new Party.NaturalPerson(new Person(req(g, "ad").asText(), req(g, "soyad").asText(), null, opt(g, "unvan"), null),
                    opt(g, "tckn"), opt(g, "gorev"), contact(g));
        }
        throw new InputException("Taraf için 'kurumKurulus', 'tuzelSahis' veya 'gercekSahis' gerekli.");
    }

    private static ContactInfo contact(JsonNode n) {
        JsonNode c = n.get("iletisim");
        if (c == null) return null;
        return new ContactInfo(opt(c, "telefon"), null, opt(c, "eposta"), opt(c, "kepAdresi"), opt(c, "faks"),
                opt(c, "webAdresi"), opt(c, "adres"), opt(c, "il"), opt(c, "ilce"), opt(c, "ulke"));
    }

    static String mimeOf(String fileName) {
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        return switch (ext) {
            case "pdf" -> "application/pdf";
            case "xml" -> "application/xml";
            case "txt" -> "text/plain";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "tif", "tiff" -> "image/tiff";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "odt" -> "application/vnd.oasis.opendocument.text";
            case "zip" -> "application/zip";
            default -> "application/octet-stream";
        };
    }

    private static String sanitize(String name) throws InputException {
        try {
            return PartNames.sanitizeFileName(name);
        } catch (IllegalArgumentException e) {
            throw new InputException("Dosya adı kullanılamıyor: " + name);
        }
    }

    private static JsonNode req(JsonNode n, String key) throws InputException {
        JsonNode v = n.get(key);
        if (v == null || v.isNull() || (v.isTextual() && v.asText().isBlank())) throw new InputException("Zorunlu alan eksik: " + key);
        return v;
    }

    private static String opt(JsonNode n, String key) {
        JsonNode v = n.get(key);
        return v == null || v.isNull() || v.asText().isBlank() ? null : v.asText();
    }

    private static List<JsonNode> list(JsonNode n, String key) {
        List<JsonNode> out = new ArrayList<>();
        JsonNode v = n.get(key);
        if (v != null && v.isArray()) v.forEach(out::add);
        return out;
    }

    private static <E extends Enum<E>> E enumOf(Class<E> type, JsonNode n, String key, String dflt) throws InputException {
        String v = opt(n, key);
        try {
            return Enum.valueOf(type, (v == null ? dflt : v).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new InputException("Geçersiz değer '" + v + "' (" + key + ")");
        }
    }
}
