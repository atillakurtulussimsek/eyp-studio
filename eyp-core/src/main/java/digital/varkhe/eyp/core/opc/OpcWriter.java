package digital.varkhe.eyp.core.opc;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Minimal OPC (ZIP) writer: parts, relationships and [Content_Types].xml. */
public final class OpcWriter {
    private static final String NS_CT = "http://schemas.openxmlformats.org/package/2006/content-types";
    private static final String NS_REL = "http://schemas.openxmlformats.org/package/2006/relationships";
    private static final String MIME_RELS = "application/vnd.openxmlformats-package.relationships+xml";

    private record Part(String name, String contentType, byte[] data) {
    }

    private final Map<String, Part> parts = new LinkedHashMap<>();
    private final Map<String, List<Relationship>> rels = new LinkedHashMap<>();

    public OpcWriter part(String name, String contentType, byte[] data) {
        String n = PartNames.normalize(name);
        if (parts.putIfAbsent(PartNames.key(n), new Part(n, contentType, data.clone())) != null) {
            throw new IllegalArgumentException("Duplicate part: " + n);
        }
        return this;
    }

    /** Adds a relationship; package-level targets are written absolute, part-level targets relative. */
    public OpcWriter relationship(String source, String id, String type, String targetPart) {
        String target = source.equals("/") ? targetPart : PartNames.relativize(source, targetPart);
        List<Relationship> list = rels.computeIfAbsent(source, k -> new ArrayList<>());
        for (Relationship r : list) if (r.id().equals(id)) throw new IllegalArgumentException("Duplicate relationship id: " + id);
        list.add(new Relationship(source, id, type, target));
        return this;
    }

    public void write(OutputStream out) throws IOException {
        ZipOutputStream zip = new ZipOutputStream(out, StandardCharsets.UTF_8);
        entry(zip, "/[Content_Types].xml", contentTypes());
        for (var e : rels.entrySet()) entry(zip, PartNames.relsPartFor(e.getKey()), relsXml(e.getValue()));
        for (Part p : parts.values()) entry(zip, p.name(), p.data());
        zip.finish();
    }

    private static void entry(ZipOutputStream zip, String partName, byte[] data) throws IOException {
        zip.putNextEntry(new ZipEntry(partName.substring(1)));
        zip.write(data);
        zip.closeEntry();
    }

    private byte[] contentTypes() {
        // Default by extension where unambiguous, Override otherwise.
        Map<String, String> byExt = new HashMap<>();
        Map<String, Boolean> conflict = new HashMap<>();
        for (Part p : parts.values()) {
            String ext = ext(p.name());
            String prev = byExt.putIfAbsent(ext, p.contentType());
            if (prev != null && !prev.equals(p.contentType())) conflict.put(ext, true);
        }
        StringBuilder b = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        b.append("<Types xmlns=\"").append(NS_CT).append("\">");
        b.append("<Default Extension=\"rels\" ContentType=\"").append(MIME_RELS).append("\"/>");
        for (var e : byExt.entrySet()) {
            if (conflict.containsKey(e.getKey()) || e.getKey().isEmpty() || e.getKey().equals("rels")) continue;
            b.append("<Default Extension=\"").append(esc(e.getKey())).append("\" ContentType=\"").append(esc(e.getValue())).append("\"/>");
        }
        for (Part p : parts.values()) {
            String ext = ext(p.name());
            if (conflict.containsKey(ext) || ext.isEmpty()) {
                b.append("<Override PartName=\"").append(esc(p.name())).append("\" ContentType=\"").append(esc(p.contentType())).append("\"/>");
            }
        }
        b.append("</Types>");
        return b.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] relsXml(List<Relationship> list) {
        StringBuilder b = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        b.append("<Relationships xmlns=\"").append(NS_REL).append("\">");
        for (Relationship r : list) {
            b.append("<Relationship Type=\"").append(esc(r.type())).append("\" Target=\"").append(esc(r.target()))
                    .append("\" Id=\"").append(esc(r.id())).append("\"/>");
        }
        b.append("</Relationships>");
        return b.toString().getBytes(StandardCharsets.UTF_8);
    }

    static String ext(String partName) {
        String last = partName.substring(partName.lastIndexOf('/') + 1);
        int dot = last.lastIndexOf('.');
        return dot < 0 ? "" : last.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static String esc(String s) {
        return s.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
