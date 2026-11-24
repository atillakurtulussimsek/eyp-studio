package digital.varkhe.eyp.core.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

/** Digest algorithms allowed in Paket/Nihai/Paraf Özeti (guide 2.x; SHA-1 and RIPEMD-160 removed). */
public enum DigestAlgorithm {
    SHA256("http://www.w3.org/2001/04/xmlenc#sha256", "SHA-256"),
    SHA384("http://www.w3.org/2001/04/xmldsig-more#sha384", "SHA-384"),
    SHA512("http://www.w3.org/2001/04/xmlenc#sha512", "SHA-512");

    private final String uri;
    private final String jcaName;

    DigestAlgorithm(String uri, String jcaName) {
        this.uri = uri;
        this.jcaName = jcaName;
    }

    public String uri() {
        return uri;
    }

    public byte[] digest(byte[] data) {
        try {
            return MessageDigest.getInstance(jcaName).digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Optional<DigestAlgorithm> fromUri(String uri) {
        for (DigestAlgorithm a : values()) if (a.uri.equals(uri)) return Optional.of(a);
        return Optional.empty();
    }
}
