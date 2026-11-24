package digital.varkhe.eyp.core.opc;

import java.text.Normalizer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

/** Part-name helpers (ISO/IEC 29500-2 §9.1.1). */
public final class PartNames {

    /**
     * Turns a user file name into a safe part-name segment: Turkish letters folded to ASCII, whitespace removed,
     * anything outside [A-Za-z0-9._-] dropped (stricter than the official API's {@code encodePath}).
     */
    public static String sanitizeFileName(String fileName) {
        String s = fileName
                .replace('ı', 'i').replace('İ', 'I')
                .replace('ş', 's').replace('Ş', 'S')
                .replace('ğ', 'g').replace('Ğ', 'G')
                .replace('ü', 'u').replace('Ü', 'U')
                .replace('ö', 'o').replace('Ö', 'O')
                .replace('ç', 'c').replace('Ç', 'C');
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        s = s.replaceAll("\\s+", "").replaceAll("[^A-Za-z0-9._-]", "");
        while (s.startsWith(".")) s = s.substring(1);
        if (s.isEmpty() || !s.contains(".") || s.endsWith(".")) {
            throw new IllegalArgumentException("File name must keep a name and an extension after sanitising: " + fileName);
        }
        return s;
    }

    /** Collapses "." and ".." segments; rejects names escaping the package root. */
    public static String normalize(String name) {
        if (!name.startsWith("/")) name = "/" + name;
        Deque<String> out = new ArrayDeque<>();
        for (String seg : name.substring(1).split("/", -1)) {
            if (seg.equals(".")) continue;
            if (seg.equals("..")) {
                if (out.isEmpty()) throw new IllegalArgumentException("Part name escapes package root: " + name);
                out.removeLast();
                continue;
            }
            if (seg.isEmpty()) throw new IllegalArgumentException("Empty segment in part name: " + name);
            out.addLast(seg);
        }
        return "/" + String.join("/", out);
    }

    /** OPC part names are compared case-insensitively (ASCII). */
    public static String key(String partName) {
        return partName.toLowerCase(Locale.ROOT);
    }

    /** "/A/B.xml" -> "/A/_rels/B.xml.rels"; "/" -> "/_rels/.rels". */
    public static String relsPartFor(String source) {
        if (source.equals("/")) return "/_rels/.rels";
        int i = source.lastIndexOf('/');
        return source.substring(0, i + 1) + "_rels/" + source.substring(i + 1) + ".rels";
    }

    /** Relative reference from {@code source} part to {@code target} part (both absolute). */
    public static String relativize(String source, String target) {
        String[] s = source.substring(1).split("/");
        String[] t = target.substring(1).split("/");
        int common = 0;
        while (common < s.length - 1 && common < t.length - 1 && s[common].equals(t[common])) common++;
        StringBuilder b = new StringBuilder();
        for (int i = common; i < s.length - 1; i++) b.append("../");
        for (int i = common; i < t.length; i++) {
            b.append(t[i]);
            if (i < t.length - 1) b.append('/');
        }
        return b.toString();
    }

    private PartNames() {
    }
}
