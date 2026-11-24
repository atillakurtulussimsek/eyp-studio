package digital.varkhe.eyp.core.opc;

import digital.varkhe.eyp.core.xml.SafeXml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Reads untrusted OPC/ZIP input with hard limits: entry count, per-entry and total uncompressed size
 * (zip bomb), path traversal / absolute names, duplicate names, and XXE-safe XML parsing.
 */
public final class OpcReader {
    private static final String NS_CT = "http://schemas.openxmlformats.org/package/2006/content-types";
    private static final String NS_REL = "http://schemas.openxmlformats.org/package/2006/relationships";

    public record Limits(int maxEntries, long maxEntryBytes, long maxTotalBytes) {
        public static final Limits DEFAULT = new Limits(2_000, 200L << 20, 500L << 20);
    }

    public static final class InvalidPackageException extends Exception {
        public InvalidPackageException(String message) {
            super(message);
        }

        public InvalidPackageException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static OpcPackage read(InputStream in) throws IOException, InvalidPackageException {
        return read(in, Limits.DEFAULT);
    }

    public static OpcPackage read(InputStream in, Limits limits) throws IOException, InvalidPackageException {
        Map<String, byte[]> raw = new LinkedHashMap<>();
        Map<String, String> names = new HashMap<>();
        long total = 0;
        try (ZipInputStream zip = new ZipInputStream(in, StandardCharsets.UTF_8)) {
            ZipEntry e;
            while ((e = zip.getNextEntry()) != null) {
                if (raw.size() >= limits.maxEntries()) throw new InvalidPackageException("Too many ZIP entries");
                if (e.isDirectory()) continue;
                String name = checkName(e.getName());
                String key = PartNames.key(name);
                if (names.putIfAbsent(key, name) != null) throw new InvalidPackageException("Duplicate part name: " + name);
                byte[] data = readLimited(zip, limits.maxEntryBytes(), limits.maxTotalBytes() - total, name);
                total += data.length;
                raw.put(key, data);
            }
        } catch (java.util.zip.ZipException ex) {
            throw new InvalidPackageException("Not a valid ZIP file", ex);
        }
        if (raw.isEmpty()) throw new InvalidPackageException("Empty or non-ZIP input");

        byte[] ct = raw.remove(PartNames.key("/[Content_Types].xml"));
        if (ct == null) throw new InvalidPackageException("[Content_Types].xml missing");
        ContentTypes types = parseContentTypes(ct);

        List<Relationship> rels = new ArrayList<>();
        Map<String, OpcPackage.Part> parts = new LinkedHashMap<>();
        for (var entry : raw.entrySet()) {
            String name = names.get(entry.getKey());
            if (isRelsPart(name)) {
                rels.addAll(parseRels(sourceOf(name), entry.getValue()));
            } else {
                parts.put(entry.getKey(), new OpcPackage.Part(name, types.of(name), entry.getValue()));
            }
        }
        return new OpcPackage(parts, rels);
    }

    private static String checkName(String zipName) throws InvalidPackageException {
        if (zipName.isEmpty() || zipName.startsWith("/") || zipName.contains("\\") || zipName.contains("\0")
                || zipName.matches("^[A-Za-z]:.*")) {
            throw new InvalidPackageException("Illegal entry name: " + zipName);
        }
        for (String seg : zipName.split("/")) {
            if (seg.equals("..") || seg.equals(".")) throw new InvalidPackageException("Path traversal in entry name: " + zipName);
        }
        return "/" + zipName;
    }

    private static byte[] readLimited(InputStream in, long maxEntry, long remainingTotal, String name)
            throws IOException, InvalidPackageException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        long n = 0;
        int r;
        while ((r = in.read(buf)) != -1) {
            n += r;
            if (n > maxEntry) throw new InvalidPackageException("Entry exceeds size limit: " + name);
            if (n > remainingTotal) throw new InvalidPackageException("Package exceeds total size limit");
            out.write(buf, 0, r);
        }
        return out.toByteArray();
    }

    private static boolean isRelsPart(String name) {
        return name.toLowerCase(Locale.ROOT).endsWith(".rels") && name.contains("/_rels/");
    }

    /** "/A/_rels/b.xml.rels" -> "/A/b.xml"; "/_rels/.rels" -> "/". */
    private static String sourceOf(String relsName) {
        int i = relsName.lastIndexOf("/_rels/");
        String dir = relsName.substring(0, i + 1);
        String file = relsName.substring(i + "/_rels/".length(), relsName.length() - ".rels".length());
        return file.isEmpty() ? "/" : dir + file;
    }

    private record ContentTypes(Map<String, String> defaults, Map<String, String> overrides) {
        String of(String partName) {
            String o = overrides.get(PartNames.key(partName));
            if (o != null) return o;
            return defaults.get(OpcWriter.ext(partName));
        }
    }

    private static ContentTypes parseContentTypes(byte[] xml) throws InvalidPackageException {
        Document d = parse(xml, "[Content_Types].xml");
        Map<String, String> defaults = new HashMap<>();
        Map<String, String> overrides = new HashMap<>();
        NodeList dl = d.getElementsByTagNameNS(NS_CT, "Default");
        for (int i = 0; i < dl.getLength(); i++) {
            Element e = (Element) dl.item(i);
            defaults.put(e.getAttribute("Extension").toLowerCase(Locale.ROOT), e.getAttribute("ContentType"));
        }
        NodeList ol = d.getElementsByTagNameNS(NS_CT, "Override");
        for (int i = 0; i < ol.getLength(); i++) {
            Element e = (Element) ol.item(i);
            overrides.put(PartNames.key(e.getAttribute("PartName")), e.getAttribute("ContentType"));
        }
        return new ContentTypes(defaults, overrides);
    }

    private static List<Relationship> parseRels(String source, byte[] xml) throws InvalidPackageException {
        Document d = parse(xml, source + " relationships");
        List<Relationship> out = new ArrayList<>();
        NodeList nl = d.getElementsByTagNameNS(NS_REL, "Relationship");
        for (int i = 0; i < nl.getLength(); i++) {
            Element e = (Element) nl.item(i);
            if ("External".equals(e.getAttribute("TargetMode"))) continue;
            Relationship r = new Relationship(source, e.getAttribute("Id"), e.getAttribute("Type"), e.getAttribute("Target"));
            try {
                r.resolvedTarget();
            } catch (IllegalArgumentException ex) {
                throw new InvalidPackageException("Illegal relationship target: " + r.target(), ex);
            }
            out.add(r);
        }
        return out;
    }

    private static Document parse(byte[] xml, String what) throws InvalidPackageException {
        try {
            return SafeXml.parse(xml);
        } catch (SAXException e) {
            throw new InvalidPackageException("Malformed XML in " + what, e);
        }
    }

    private OpcReader() {
    }
}
