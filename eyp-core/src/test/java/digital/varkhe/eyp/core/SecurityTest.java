package digital.varkhe.eyp.core;

import digital.varkhe.eyp.core.opc.OpcReader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SecurityTest {

    private static byte[] zip(Map<String, byte[]> entries) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream z = new ZipOutputStream(out)) {
            for (var e : entries.entrySet()) {
                z.putNextEntry(new ZipEntry(e.getKey()));
                z.write(e.getValue());
                z.closeEntry();
            }
        }
        return out.toByteArray();
    }

    private static final byte[] CT = ("<?xml version=\"1.0\"?><Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
            + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/></Types>")
            .getBytes(StandardCharsets.UTF_8);

    @Test
    void rejectsPathTraversal() throws IOException {
        byte[] z = zip(Map.of("[Content_Types].xml", CT, "../evil.txt", new byte[1]));
        assertThrows(OpcReader.InvalidPackageException.class, () -> OpcReader.read(new ByteArrayInputStream(z)));
    }

    @Test
    void rejectsAbsoluteAndBackslashNames() throws IOException {
        byte[] abs = zip(Map.of("[Content_Types].xml", CT, "/etc/passwd", new byte[1]));
        assertThrows(OpcReader.InvalidPackageException.class, () -> OpcReader.read(new ByteArrayInputStream(abs)));
        byte[] bs = zip(Map.of("[Content_Types].xml", CT, "a\\..\\..\\b", new byte[1]));
        assertThrows(OpcReader.InvalidPackageException.class, () -> OpcReader.read(new ByteArrayInputStream(bs)));
    }

    @Test
    void rejectsZipBomb() throws IOException {
        byte[] z = zip(Map.of("[Content_Types].xml", CT, "big.bin", new byte[2 << 20]));
        var limits = new OpcReader.Limits(10, 1 << 20, 4 << 20);
        assertThrows(OpcReader.InvalidPackageException.class, () -> OpcReader.read(new ByteArrayInputStream(z), limits));
    }

    @Test
    void rejectsTooManyEntries() throws IOException {
        var entries = new java.util.LinkedHashMap<String, byte[]>();
        entries.put("[Content_Types].xml", CT);
        for (int i = 0; i < 20; i++) entries.put("f" + i, new byte[1]);
        byte[] z = zip(entries);
        var limits = new OpcReader.Limits(10, 1 << 20, 4 << 20);
        assertThrows(OpcReader.InvalidPackageException.class, () -> OpcReader.read(new ByteArrayInputStream(z), limits));
    }

    @Test
    void rejectsXxeInRelationships() throws IOException {
        byte[] rels = ("<?xml version=\"1.0\"?><!DOCTYPE r [<!ENTITY x SYSTEM \"file:///etc/passwd\">]>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"a\" Type=\"&x;\" Target=\"/x\"/></Relationships>").getBytes(StandardCharsets.UTF_8);
        byte[] z = zip(Map.of("[Content_Types].xml", CT, "_rels/.rels", rels));
        assertThrows(OpcReader.InvalidPackageException.class, () -> OpcReader.read(new ByteArrayInputStream(z)));
    }

    @Test
    void rejectsRelationshipTargetOutsidePackage() throws IOException {
        byte[] rels = ("<?xml version=\"1.0\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"a\" Type=\"t\" Target=\"../../x\"/></Relationships>").getBytes(StandardCharsets.UTF_8);
        byte[] z = zip(Map.of("[Content_Types].xml", CT, "_rels/.rels", rels));
        assertThrows(OpcReader.InvalidPackageException.class, () -> OpcReader.read(new ByteArrayInputStream(z)));
    }

    @Test
    void rejectsNonZip() {
        assertThrows(OpcReader.InvalidPackageException.class,
                () -> OpcReader.read(new ByteArrayInputStream("not a zip".getBytes(StandardCharsets.UTF_8))));
    }
}
