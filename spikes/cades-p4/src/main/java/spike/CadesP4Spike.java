package spike;

import eu.europa.esig.dss.cades.CAdESSignatureParameters;
import eu.europa.esig.dss.cades.signature.CAdESService;
import eu.europa.esig.dss.cades.validation.CAdESSignature;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.Policy;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.TimestampBinary;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.spi.signature.AdvancedSignature;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.spi.x509.CommonTrustedCertificateSource;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.reports.Reports;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.esf.CrlOcspRef;
import org.bouncycastle.asn1.esf.OcspIdentifier;
import org.bouncycastle.asn1.esf.OcspListID;
import org.bouncycastle.asn1.esf.OcspResponsesID;
import org.bouncycastle.asn1.esf.OtherHash;
import org.bouncycastle.asn1.esf.OtherHashAlgAndValue;
import org.bouncycastle.asn1.esf.RevocationValues;
import org.bouncycastle.asn1.ess.OtherCertID;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.ocsp.BasicOCSPResponse;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.IssuerSerial;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.BasicOCSPRespBuilder;
import org.bouncycastle.cert.ocsp.CertificateID;
import org.bouncycastle.cert.ocsp.CertificateStatus;
import org.bouncycastle.cert.ocsp.RespID;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampResponseGenerator;
import org.bouncycastle.tsp.TimeStampTokenGenerator;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

/**
 * Phase 0 spike: can DSS + BouncyCastle produce a Turkish P4 (CAdES ES X-Long, EPES) signature
 * with a key that lives on a PKCS#11 token?
 *
 * Usage: setup | sign. Everything here is TEST material (local CA, local TSA, local OCSP).
 */
public final class CadesP4Spike {

    // BTK "Elektronik İmza Kullanım Profilleri Rehberi" (2012), P4 policy OID.
    static final String P4_POLICY_OID = "2.16.792.1.61.0.1.5070.3.3.1";
    // SHA-256 of the official profiles guide PDF; same value appears in two open-source MA3-based codebases.
    static final String P4_POLICY_SHA256 = "ff39bd29463383f69b2052ac47439e06ce7c3b8646e888b6e5ae3e46ba08117a";
    // SPURI seen in open-source code (kursadacar/AesobKEP). UNVERIFIED against a real MA3 signature.
    static final String P4_POLICY_SPURI =
            "http://www.tk.gov.tr/bilgi_teknolojileri/elektronik_imza/dosyalar/Elektronik_Imza_Kullanim_Profilleri_Rehberi.pdf";

    // ETSI TS 101 733 / RFC 5126 unsigned attribute OIDs (id-aa 21..24).
    static final ASN1ObjectIdentifier ID_AA_COMPLETE_CERT_REFS = PKCSObjectIdentifiers.id_aa_ets_certificateRefs;
    static final ASN1ObjectIdentifier ID_AA_COMPLETE_REVOCATION_REFS = PKCSObjectIdentifiers.id_aa_ets_revocationRefs;
    static final ASN1ObjectIdentifier ID_AA_CERT_VALUES = PKCSObjectIdentifiers.id_aa_ets_certValues;
    static final ASN1ObjectIdentifier ID_AA_REVOCATION_VALUES = PKCSObjectIdentifiers.id_aa_ets_revocationValues;

    static final String TOKEN_ALIAS = "signer";
    static final char[] TEST_PASS = "test".toCharArray();

    static Path work;
    static String p11Lib;
    static char[] pin;

    public static void main(String[] args) throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        work = Path.of(System.getProperty("spike.work", "work"));
        p11Lib = System.getProperty("spike.p11lib", "/opt/homebrew/lib/softhsm/libsofthsm2.so");
        pin = System.getProperty("spike.pin", "1234").toCharArray();
        Files.createDirectories(work);
        switch (args.length == 0 ? "" : args[0]) {
            case "setup" -> setup();
            case "sign" -> sign();
            default -> System.err.println("usage: setup | sign");
        }
    }

    // ---------------------------------------------------------------- setup

    static void setup() throws Exception {
        Instant now = Instant.now();
        KeyPair rootKp = rsa(null);
        X509Certificate root = cert(new X500Name("CN=EYP Spike Test Root,O=TEST,C=TR"), rootKp, null, null,
                new X500Name("CN=EYP Spike Test Root,O=TEST,C=TR"), now, 3650, true, KeyUsage.keyCertSign | KeyUsage.cRLSign, null, false);

        KeyPair tsaKp = rsa(null);
        X509Certificate tsa = cert(new X500Name("CN=EYP Spike Test TSA,O=TEST,C=TR"), tsaKp, root, rootKp.getPrivate(),
                null, now, 365, false, KeyUsage.digitalSignature, KeyPurposeId.id_kp_timeStamping, false);

        KeyPair ocspKp = rsa(null);
        X509Certificate ocsp = cert(new X500Name("CN=EYP Spike Test OCSP,O=TEST,C=TR"), ocspKp, root, rootKp.getPrivate(),
                null, now, 365, false, KeyUsage.digitalSignature, KeyPurposeId.id_kp_OCSPSigning, true);

        // Signer key pair is generated ON the token; only the public key leaves it.
        Provider p11 = p11Provider();
        KeyStore tokenKs = KeyStore.getInstance("PKCS11", p11);
        tokenKs.load(null, pin); // logs in
        KeyPair signerKp = rsa(p11);
        X509Certificate signer = cert(new X500Name("CN=Test Imzaci,SERIALNUMBER=12345678901,C=TR"), signerKp, root,
                rootKp.getPrivate(), null, now, 365, false, KeyUsage.nonRepudiation, null, false);

        tokenKs.setKeyEntry(TOKEN_ALIAS, signerKp.getPrivate(), null, new java.security.cert.Certificate[]{signer, root});
        System.out.println("Token entries: " + java.util.Collections.list(tokenKs.aliases()));

        // TEST-ONLY software keys for the local CA/TSA/OCSP.
        KeyStore soft = KeyStore.getInstance("PKCS12");
        soft.load(null, null);
        soft.setKeyEntry("root", rootKp.getPrivate(), TEST_PASS, new java.security.cert.Certificate[]{root});
        soft.setKeyEntry("tsa", tsaKp.getPrivate(), TEST_PASS, new java.security.cert.Certificate[]{tsa, root});
        soft.setKeyEntry("ocsp", ocspKp.getPrivate(), TEST_PASS, new java.security.cert.Certificate[]{ocsp, root});
        try (var out = Files.newOutputStream(work.resolve("test-pki.p12"))) {
            soft.store(out, TEST_PASS);
        }
        System.out.println("Setup done: " + work.toAbsolutePath());
    }

    static Provider p11Provider() throws IOException {
        Path cfg = work.resolve("p11.cfg");
        Files.writeString(cfg, "name=Spike\nlibrary=" + p11Lib + "\nslotListIndex=0\n");
        Provider p = Security.getProvider("SunPKCS11").configure(cfg.toString());
        Security.addProvider(p);
        return p;
    }

    static KeyPair rsa(Provider p) throws Exception {
        KeyPairGenerator g = p == null ? KeyPairGenerator.getInstance("RSA") : KeyPairGenerator.getInstance("RSA", p);
        g.initialize(2048);
        return g.generateKeyPair();
    }

    static X509Certificate cert(X500Name subject, KeyPair kp, X509Certificate issuer, PrivateKey issuerKey, X500Name selfIssuer,
                                Instant now, int days, boolean ca, int keyUsage, KeyPurposeId eku, boolean ocspNoCheck) throws Exception {
        X500Name issuerName = issuer == null ? selfIssuer : X500Name.getInstance(issuer.getSubjectX500Principal().getEncoded());
        var b = new JcaX509v3CertificateBuilder(issuerName, new BigInteger(64, new java.security.SecureRandom()),
                Date.from(now.minus(Duration.ofMinutes(5))), Date.from(now.plus(Duration.ofDays(days))), subject, kp.getPublic());
        b.addExtension(Extension.basicConstraints, true, new BasicConstraints(ca));
        b.addExtension(Extension.keyUsage, true, new KeyUsage(keyUsage));
        if (eku != null) b.addExtension(Extension.extendedKeyUsage, true, new ExtendedKeyUsage(eku));
        if (ocspNoCheck) b.addExtension(OCSPObjectIdentifiers.id_pkix_ocsp_nocheck, false, DERNull.INSTANCE);
        PrivateKey signKey = issuer == null ? kp.getPrivate() : issuerKey;
        ContentSigner cs = new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC").build(signKey);
        return new JcaX509CertificateConverter().setProvider("BC").getCertificate(b.build(cs));
    }

    // ---------------------------------------------------------------- sign

    static void sign() throws Exception {
        KeyStore soft = KeyStore.getInstance("PKCS12");
        try (var in = Files.newInputStream(work.resolve("test-pki.p12"))) {
            soft.load(in, TEST_PASS);
        }
        X509Certificate root = (X509Certificate) soft.getCertificate("root");
        X509Certificate tsaCert = (X509Certificate) soft.getCertificate("tsa");
        PrivateKey tsaKey = (PrivateKey) soft.getKey("tsa", TEST_PASS);
        X509Certificate ocspCert = (X509Certificate) soft.getCertificate("ocsp");
        PrivateKey ocspKey = (PrivateKey) soft.getKey("ocsp", TEST_PASS);

        byte[] content = """
                <?xml version="1.0" encoding="UTF-8"?>
                <spike>Phase 0 CAdES P4 test content (stands in for PaketOzeti.xml)</spike>
                """.getBytes(StandardCharsets.UTF_8);
        DSSDocument doc = new InMemoryDocument(content, "PaketOzeti.xml", MimeTypeEnum.XML);

        // 1) DSS: CAdES-EPES + signature-time-stamp (baseline T), enveloping, key on token.
        try (Pkcs11SignatureToken token = new Pkcs11SignatureToken(p11Lib, () -> pin.clone(), -1, 0, null)) {
            List<DSSPrivateKeyEntry> keys = token.getKeys();
            DSSPrivateKeyEntry key = keys.get(0);
            X509Certificate signerCert = key.getCertificate().getCertificate();

            CAdESSignatureParameters p = new CAdESSignatureParameters();
            p.setSignatureLevel(SignatureLevel.CAdES_BASELINE_T);
            p.setSignaturePackaging(SignaturePackaging.ENVELOPING);
            p.setDigestAlgorithm(DigestAlgorithm.SHA256);
            p.setSigningCertificate(key.getCertificate());
            p.setCertificateChain(key.getCertificateChain());
            Policy policy = new Policy();
            policy.setId(P4_POLICY_OID);
            policy.setDigestAlgorithm(DigestAlgorithm.SHA256);
            policy.setDigestValue(HexFormat.of().parseHex(P4_POLICY_SHA256));
            policy.setSpuri(P4_POLICY_SPURI);
            p.bLevel().setSignaturePolicy(policy);

            CAdESService service = new CAdESService(new CommonCertificateVerifier());
            service.setTspSource(new LocalTsa(tsaCert, tsaKey));
            ToBeSigned tbs = service.getDataToSign(doc, p);
            SignatureValue sv = token.sign(tbs, DigestAlgorithm.SHA256, key);
            DSSDocument tLevel = service.signDocument(doc, p, sv);
            byte[] tBytes = tLevel.openStream().readAllBytes();
            Files.write(work.resolve("PaketOzeti-T.imz"), tBytes);

            // 2) BouncyCastle: add ES-C refs + ES-X-Long values (no ES-X timestamps) => CAdES-X-Long.
            BasicOCSPResp ocspResp = ocspGood(signerCert, root, ocspCert, ocspKey);
            byte[] xl = toXLong(tBytes, root, ocspCert, ocspResp);
            Files.write(work.resolve("PaketOzeti-XL.imz"), xl);

            // 3) Checks.
            report(xl, signerCert, root, ocspCert, ocspResp);
        }
    }

    static BasicOCSPResp ocspGood(X509Certificate subject, X509Certificate issuer, X509Certificate responder, PrivateKey responderKey)
            throws Exception {
        var digCalc = new JcaDigestCalculatorProviderBuilder().build().get(CertificateID.HASH_SHA1);
        CertificateID id = new CertificateID(digCalc, new JcaX509CertificateHolder(issuer), subject.getSerialNumber());
        BasicOCSPRespBuilder b = new BasicOCSPRespBuilder(new RespID(new JcaX509CertificateHolder(responder).getSubject()));
        b.addResponse(id, CertificateStatus.GOOD, new Date(), (Date) null);
        ContentSigner cs = new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC").build(responderKey);
        return b.build(cs, new X509CertificateHolder[]{new JcaX509CertificateHolder(responder)}, new Date());
    }

    static byte[] toXLong(byte[] cms, X509Certificate root, X509Certificate ocspCert, BasicOCSPResp ocsp) throws Exception {
        CMSSignedData sd = new CMSSignedData(cms);
        SignerInformation si = sd.getSignerInfos().getSigners().iterator().next();

        // complete-certificate-refs: every cert of the path except the signer's own, plus the OCSP responder.
        ASN1EncodableVector certRefs = new ASN1EncodableVector();
        for (X509Certificate c : List.of(root, ocspCert)) certRefs.add(otherCertId(c));

        // complete-revocation-refs: one CrlOcspRef per path cert (signer). Root is the trust anchor.
        BasicOCSPResponse basic = BasicOCSPResponse.getInstance(ocsp.getEncoded());
        OcspIdentifier ocspId = new OcspIdentifier(basic.getTbsResponseData().getResponderID(),
                ASN1GeneralizedTime.getInstance(basic.getTbsResponseData().getProducedAt()));
        OcspResponsesID respId = new OcspResponsesID(ocspId, sha256OtherHash(
                new org.bouncycastle.cert.ocsp.OCSPRespBuilder().build(org.bouncycastle.cert.ocsp.OCSPRespBuilder.SUCCESSFUL, ocsp).getEncoded()));
        // ^ hash over the full OCSPResponse (RFC 5126 ocspRepHash); hashing only BasicOCSPResponse left the ref orphaned in DSS.
        CrlOcspRef signerRef = new CrlOcspRef(null, new OcspListID(new OcspResponsesID[]{respId}), null);
        DERSequence revRefs = new DERSequence(signerRef);

        DERSequence certValues = new DERSequence(new ASN1EncodableVector() {{
            add(Certificate.getInstance(root.getEncoded()));
            add(Certificate.getInstance(ocspCert.getEncoded()));
        }});
        RevocationValues revValues = new RevocationValues(null, new BasicOCSPResponse[]{basic}, null);

        AttributeTable unsigned = si.getUnsignedAttributes();
        unsigned = unsigned.add(ID_AA_COMPLETE_CERT_REFS, new DERSequence(certRefs));
        unsigned = unsigned.add(ID_AA_COMPLETE_REVOCATION_REFS, revRefs);
        unsigned = unsigned.add(ID_AA_CERT_VALUES, certValues);
        unsigned = unsigned.add(ID_AA_REVOCATION_VALUES, revValues);

        SignerInformation xl = SignerInformation.replaceUnsignedAttributes(si, unsigned);
        return CMSSignedData.replaceSigners(sd, new SignerInformationStore(xl)).getEncoded("DER");
    }

    static OtherCertID otherCertId(X509Certificate c) throws Exception {
        var holder = new JcaX509CertificateHolder(c);
        IssuerSerial is = new IssuerSerial(new GeneralNames(new GeneralName(holder.getIssuer())), c.getSerialNumber());
        return new OtherCertID(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256),
                MessageDigest.getInstance("SHA-256").digest(c.getEncoded()), is);
    }

    static OtherHash sha256OtherHash(byte[] data) throws Exception {
        return new OtherHash(new OtherHashAlgAndValue(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256),
                new org.bouncycastle.asn1.DEROctetString(MessageDigest.getInstance("SHA-256").digest(data))));
    }

    // ---------------------------------------------------------------- report

    static void report(byte[] xl, X509Certificate signerCert, X509Certificate root, X509Certificate ocspCert, BasicOCSPResp ocsp)
            throws Exception {
        CMSSignedData sd = new CMSSignedData(xl);
        SignerInformation si = sd.getSignerInfos().getSigners().iterator().next();
        boolean cryptoOk = si.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(signerCert));
        System.out.println("\n== BouncyCastle");
        System.out.println("Signature cryptographically valid: " + cryptoOk);
        System.out.println("Encapsulated content present: " + (sd.getSignedContent() != null));
        System.out.println("Signed attributes:");
        si.getSignedAttributes().toASN1Structure().getAttributes();
        for (Attribute a : si.getSignedAttributes().toASN1Structure().getAttributes())
            System.out.println("  " + a.getAttrType() + "  " + name(a.getAttrType()));
        System.out.println("Unsigned attributes:");
        for (Attribute a : si.getUnsignedAttributes().toASN1Structure().getAttributes())
            System.out.println("  " + a.getAttrType() + "  " + name(a.getAttrType()));

        System.out.println("\n== DSS");
        CommonTrustedCertificateSource trusted = new CommonTrustedCertificateSource();
        trusted.addCertificate(new CertificateToken(root));
        CommonCertificateVerifier cv = new CommonCertificateVerifier();
        cv.setTrustedCertSources(trusted);
        cv.setOcspSource(null);
        cv.setCrlSource(null);
        cv.setAIASource(null);
        SignedDocumentValidator v = SignedDocumentValidator.fromDocument(new InMemoryDocument(xl));
        v.setCertificateVerifier(cv);
        for (AdvancedSignature s : v.getSignatures()) {
            System.out.println("Data found up to level: " + ((CAdESSignature) s).getDataFoundUpToLevel());
            System.out.println("Policy id: " + (s.getSignaturePolicy() == null ? null : s.getSignaturePolicy().getIdentifier()));
            System.out.println("Mime type (signed attr): " + s.getMimeType());
        }
        Reports r = v.validateDocument();
        var simple = r.getSimpleReport();
        for (String id : simple.getSignatureIdList()) {
            System.out.println("Format: " + simple.getSignatureFormat(id));
            System.out.println("Indication: " + simple.getIndication(id) + " / " + simple.getSubIndication(id));
            var diag = r.getDiagnosticData().getSignatureById(id);
            System.out.println("Found certificate refs: " + diag.foundCertificates().getOrphanCertificateRefs().size() + " orphan, "
                    + diag.foundCertificates().getRelatedCertificateRefs().size() + " related");
            System.out.println("Found revocation refs: " + diag.foundRevocations().getRelatedRevocationRefs().size() + " related, "
                    + diag.foundRevocations().getOrphanRevocationRefs().size() + " orphan");
            for (var c : r.getDiagnosticData().getUsedCertificates())
                System.out.println("  cert " + c.getId().substring(0, 12) + " " + c.getCommonName()
                        + " revocations=" + c.getCertificateRevocationData().size());
            for (var ref : diag.foundRevocations().getRelatedRevocationRefs())
                System.out.println("  revocation ref origins=" + ref.getOrigins());
            for (var ref : diag.foundCertificates().getRelatedCertificateRefs())
                System.out.println("  cert ref origin=" + ref.getOrigin());
            System.out.println("Errors: " + simple.getAdESValidationErrors(id));
            for (var m : simple.getAdESValidationWarnings(id)) System.out.println("Warning: " + m.getValue());
        }
    }

    static String name(ASN1ObjectIdentifier oid) {
        return switch (oid.getId()) {
            case "1.2.840.113549.1.9.3" -> "content-type";
            case "1.2.840.113549.1.9.4" -> "message-digest";
            case "1.2.840.113549.1.9.5" -> "signing-time";
            case "1.2.840.113549.1.9.16.2.47" -> "signing-certificate-v2";
            case "1.2.840.113549.1.9.16.2.15" -> "signature-policy-identifier";
            case "0.4.0.1733.2.1" -> "mime-type";
            case "1.2.840.113549.1.9.16.2.14" -> "signature-time-stamp";
            case "1.2.840.113549.1.9.16.2.21" -> "complete-certificate-refs";
            case "1.2.840.113549.1.9.16.2.22" -> "complete-revocation-refs";
            case "1.2.840.113549.1.9.16.2.23" -> "certificate-values";
            case "1.2.840.113549.1.9.16.2.24" -> "revocation-values";
            case "1.2.840.113549.1.9.52" -> "cms-algorithm-protection";
            default -> "?";
        };
    }

    // ---------------------------------------------------------------- local TSA

    /** In-process RFC 3161 TSA for the spike. Not used in production. */
    static final class LocalTsa implements TSPSource {
        private final X509Certificate cert;
        private final PrivateKey key;

        LocalTsa(X509Certificate cert, PrivateKey key) {
            this.cert = cert;
            this.key = key;
        }

        @Override
        public TimestampBinary getTimeStampResponse(DigestAlgorithm alg, byte[] digest) {
            try {
                TimeStampRequestGenerator rg = new TimeStampRequestGenerator();
                rg.setCertReq(true);
                TimeStampRequest req = rg.generate(new ASN1ObjectIdentifier(alg.getOid()), digest);
                var sig = new JcaSimpleSignerInfoGeneratorBuilder().setProvider("BC").build("SHA256withRSA", key, cert);
                // 1.3.6.1.4.1.99999.1 is a placeholder TSA policy for the local test TSA only.
                TimeStampTokenGenerator tg = new TimeStampTokenGenerator(sig,
                        new JcaDigestCalculatorProviderBuilder().build().get(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256)),
                        new ASN1ObjectIdentifier("1.3.6.1.4.1.99999.1"));
                tg.addCertificates(new org.bouncycastle.cert.jcajce.JcaCertStore(List.of(cert)));
                TimeStampResponse resp = new TimeStampResponseGenerator(tg, Set.of(alg.getOid()).stream()
                        .map(ASN1ObjectIdentifier::new).collect(java.util.stream.Collectors.toSet()))
                        .generate(req, BigInteger.valueOf(System.nanoTime()), new Date());
                return new TimestampBinary(resp.getTimeStampToken().getEncoded());
            } catch (Exception e) {
                throw new eu.europa.esig.dss.model.DSSException(e);
            }
        }
    }

    private CadesP4Spike() {
    }
}
