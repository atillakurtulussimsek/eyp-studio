package digital.varkhe.eyp.cli;

import digital.varkhe.eyp.core.opc.OpcPackage;
import digital.varkhe.eyp.core.opc.OpcReader;
import digital.varkhe.eyp.core.validation.Finding;
import digital.varkhe.eyp.core.validation.PackageValidator;
import digital.varkhe.eyp.core.validation.Severity;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "dogrula", description = "Paketi Teknik Rehber 2.1 kurallarına (K.1–K.116) göre yapısal olarak doğrular. "
        + "İmza/mühür kriptografik doğrulaması bu sürümde yok.")
final class ValidateCommand implements Callable<Integer> {

    @Parameters(index = "0", paramLabel = "PAKET", description = ".eyp dosyası")
    Path file;

    @Override
    public Integer call() throws Exception {
        OpcPackage pkg;
        try {
            pkg = PackageFiles.open(file);
        } catch (OpcReader.InvalidPackageException e) {
            System.out.println("GEÇERSİZ: paket açılamadı – " + e.getMessage());
            return 2;
        }
        List<Finding> findings = PackageValidator.validate(pkg);
        for (Finding f : findings) {
            String label = switch (f.severity()) {
                case ERROR -> "HATA ";
                case WARNING -> "UYARI";
                case INFO -> "BİLGİ";
            };
            System.out.println(label + " " + f.rule() + "  " + f.message());
        }
        long errors = findings.stream().filter(f -> f.severity() == Severity.ERROR).count();
        System.out.println(errors == 0 ? "SONUÇ: yapısal olarak geçerli" : "SONUÇ: " + errors + " hata");
        return errors == 0 ? 0 : 1;
    }
}
