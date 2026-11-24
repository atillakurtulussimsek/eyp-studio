package digital.varkhe.eyp.cli;

import digital.varkhe.eyp.core.model.DigestAlgorithm;
import digital.varkhe.eyp.core.pkg.PackageBuilder;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.util.concurrent.Callable;

@Command(name = "olustur", description = "JSON belge tanımından imzasız bir .eyp (2.x) üretir.")
final class CreateCommand implements Callable<Integer> {

    @Parameters(index = "0", paramLabel = "BELGE_JSON", description = "Belge tanımı (bkz. eyp-cli/README.md).")
    Path input;

    @Option(names = {"-o", "--cikti"}, paramLabel = "KLASOR", description = "Çıktı klasörü (varsayılan: JSON ile aynı klasör).")
    Path outputDir;

    @Option(names = "--ikinci-ozet", defaultValue = "SHA256",
            description = "SHA-512 yanındaki ikinci özet: ${COMPLETION-CANDIDATES} (varsayılan: ${DEFAULT-VALUE}).")
    DigestAlgorithm secondDigest;

    @Override
    public Integer call() throws Exception {
        DocumentInput in;
        try {
            in = DocumentInput.read(input);
        } catch (DocumentInput.InputException e) {
            System.err.println("Hata: " + e.getMessage());
            return 2;
        }
        PackageBuilder b = new PackageBuilder(in.metadata, secondDigest, Clock.systemUTC()).coverLetter(in.coverLetter);
        for (var e : in.attachments.entrySet()) b.attachment(e.getKey(), e.getValue());
        b.buildPaketOzeti();

        Path dir = outputDir != null ? outputDir : input.toAbsolutePath().getParent();
        Files.createDirectories(dir);
        Path out = dir.resolve(b.fileName());
        try (OutputStream os = Files.newOutputStream(out, StandardOpenOption.CREATE_NEW)) {
            b.write(os);
        }
        if (!startsWithPdf(in.coverLetter)) System.err.println("Uyarı: Üst yazı PDF değil; kurumlar PDF/A bekler.");
        System.out.println("Oluşturuldu (imzasız): " + out);
        System.out.println("BelgeId: " + in.metadata.documentId().toString().toUpperCase(java.util.Locale.ROOT));
        return 0;
    }

    private static boolean startsWithPdf(byte[] b) {
        return b.length >= 5 && b[0] == '%' && b[1] == 'P' && b[2] == 'D' && b[3] == 'F' && b[4] == '-';
    }
}
